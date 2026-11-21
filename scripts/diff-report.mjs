import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readdirSync, readFileSync, realpathSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { decodePng, encodePng, PngError } from './lib/png.mjs';
import { bandShift } from './lib/band-shift.mjs';
import { offByTwoPercent } from './lib/offbytwo.mjs';
import { parseScreens } from './lib/screens.mjs';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const GAP = 8;
const FADE = 0.3;
const NAME_RE = /^(.+)_(light|dark)\.png$/;
const PLATFORMS = ['ios', 'android'];
const MODES = ['light', 'dark'];
const BAND_HEIGHT = { ios: 60, android: Math.ceil(2532 / 14) };
const EPSILON = 1e-6;
const COMPARED_FIELDS = ['actualSha256', 'test.passed', 'test.value', 'percentDifferent', 'offByTwoPercent'];

class UsageError extends Error {}

function readImage(file) {
  try {
    return decodePng(readFileSync(file));
  } catch (e) {
    if (e instanceof PngError) e.message = `${file}: ${e.message}`;
    throw e;
  }
}

function pixelAt(img, x, y) {
  if (x >= img.width || y >= img.height) return null;
  const o = (y * img.width + x) * 4;
  return img.data.subarray(o, o + 4);
}

function compose(reference, actual) {
  const w = Math.max(reference.width, actual.width);
  const h = Math.max(reference.height, actual.height);
  const width = 3 * w + 2 * GAP;
  const data = new Uint8Array(width * h * 4).fill(255);
  const put = (x, y, r, g, b, a) => data.set([r, g, b, a], (y * width + x) * 4);
  let differentPixels = 0;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const r = pixelAt(reference, x, y);
      const a = pixelAt(actual, x, y);
      if (r) put(x, y, r[0], r[1], r[2], r[3]);
      if (a) put(w + GAP + x, y, a[0], a[1], a[2], a[3]);
      const same = r && a && r[0] === a[0] && r[1] === a[1] && r[2] === a[2] && r[3] === a[3];
      if (same) {
        const lum = 0.299 * r[0] + 0.587 * r[1] + 0.114 * r[2];
        const v = Math.round(255 - (255 - lum) * FADE);
        put(2 * (w + GAP) + x, y, v, v, v, 255);
      } else {
        differentPixels++;
        put(2 * (w + GAP) + x, y, 255, 0, 0, 255);
      }
    }
  }
  const percentDifferent = Math.round((differentPixels / (w * h)) * 1e6) / 1e4;
  return { image: { width, height: h, data }, differentPixels, percentDifferent };
}

function listPairs(opts) {
  if (opts.name !== undefined || opts.mode !== undefined || opts.actual !== undefined) {
    if (opts.name === undefined || opts.mode === undefined || opts.actual === undefined) {
      throw new UsageError('--name, --mode and --actual must be given together');
    }
    if (opts.mode !== 'light' && opts.mode !== 'dark') throw new UsageError(`invalid --mode: ${opts.mode}`);
    if (!existsSync(opts.actual)) throw new UsageError(`actual image not found: ${opts.actual}`);
    return [{ name: opts.name, mode: opts.mode, actualFile: opts.actual }];
  }
  if (!existsSync(opts.actualDir) || !statSync(opts.actualDir).isDirectory()) return [];
  return readdirSync(opts.actualDir)
    .map((f) => ({ f, m: NAME_RE.exec(f) }))
    .filter(({ m }) => m)
    .map(({ f, m }) => ({ name: m[1], mode: m[2], actualFile: join(opts.actualDir, f) }));
}

function runSingle(options) {
  const { platform } = options;
  if (platform !== 'ios' && platform !== 'android') throw new UsageError('--platform ios|android is required');
  const opts = {
    ...options,
    actualDir: options.actualDir ?? join(ROOT, 'build', 'snapshots', platform),
    referenceDir: options.referenceDir ?? join(ROOT, 'design', 'reference', platform),
    out: options.out ?? join(ROOT, 'reports', platform),
  };
  const pairs = listPairs(opts);
  if (pairs.length === 0) {
    process.stdout.write('keine Ist-Bilder\n');
    return 0;
  }
  mkdirSync(opts.out, { recursive: true });
  const summary = [];
  const missing = [];
  for (const { name, mode, actualFile } of pairs) {
    const referenceFile = join(opts.referenceDir, `${name}_${mode}.png`);
    if (!existsSync(referenceFile)) {
      summary.push({ name, mode, error: 'reference-missing' });
      missing.push(`${name}_${mode}`);
      continue;
    }
    const reference = readImage(referenceFile);
    const actual = readImage(actualFile);
    const { image, differentPixels, percentDifferent } = compose(reference, actual);
    writeFileSync(join(opts.out, `${name}_${mode}.png`), encodePng(image));
    summary.push({
      name,
      mode,
      reference: { w: reference.width, h: reference.height },
      actual: { w: actual.width, h: actual.height },
      sizeMismatch: reference.width !== actual.width || reference.height !== actual.height,
      differentPixels,
      percentDifferent,
    });
  }
  summary.sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : a.mode < b.mode ? -1 : a.mode > b.mode ? 1 : 0));
  writeFileSync(join(opts.out, 'summary.json'), JSON.stringify(summary, null, 2) + '\n');
  for (const s of summary) {
    if (s.error) continue;
    process.stdout.write(`${s.name}_${s.mode}: ${s.differentPixels} px (${s.percentDifferent} %)${s.sizeMismatch ? ' size mismatch' : ''}\n`);
  }
  if (missing.length > 0) {
    process.stderr.write(`Referenz fehlt für: ${missing.join(', ')}\n`);
    return 1;
  }
  return 0;
}

function readJson(file, what) {
  try {
    return JSON.parse(readFileSync(file, 'utf8'));
  } catch {
    throw new UsageError(`unreadable ${what} file: ${file}`);
  }
}

function readSidecar(file) {
  if (!existsSync(file)) return null;
  try {
    const j = JSON.parse(readFileSync(file, 'utf8'));
    return { passed: j.passed === true, value: typeof j.value === 'number' ? j.value : null, required: j.required ?? null, unit: j.unit ?? null };
  } catch {
    return null;
  }
}

const emptyMetrics = { percentDifferent: null, offByTwoPercent: null, positionShare: null, bandsImproved: null };

function measureRow(platform, reference, actual, panelFile) {
  const { image, differentPixels, percentDifferent } = compose(reference, actual);
  writeFileSync(panelFile, encodePng(image));
  const sizeMismatch = reference.width !== actual.width || reference.height !== actual.height;
  const summary = {
    reference: { w: reference.width, h: reference.height },
    actual: { w: actual.width, h: actual.height },
    sizeMismatch,
    differentPixels,
    percentDifferent,
  };
  if (sizeMismatch) return { summary, metrics: { ...emptyMetrics, percentDifferent } };
  const { width: w, height: h } = actual;
  const shift = bandShift(reference.data, actual.data, w, h, { bandHeight: BAND_HEIGHT[platform] });
  return {
    summary,
    metrics: {
      percentDifferent,
      offByTwoPercent: platform === 'android' ? offByTwoPercent(reference.data, actual.data, w, h) : null,
      positionShare: shift.positionShare,
      bandsImproved: shift.bandsImproved,
    },
  };
}

function differs(a, b) {
  if (typeof a === 'number' && typeof b === 'number') return Math.abs(a - b) > EPSILON;
  return a !== b;
}

function fieldValue(row, field) {
  if (field === 'test.passed') return row.test?.passed ?? null;
  if (field === 'test.value') return row.test?.value ?? null;
  return row[field] ?? null;
}

const rowKey = (r) => `${r.platform}/${r.name}/${r.mode}`;

function compareToBaseline(rows, baseline, unstable, platform) {
  const kept = platform ? baseline.rows.filter((r) => r.platform === platform) : baseline.rows;
  const before = new Map(kept.map((r) => [rowKey(r), r]));
  const skipHash = new Set(unstable.map(rowKey));
  const changed = [];
  for (const row of rows) {
    const old = before.get(rowKey(row));
    if (!old) {
      if (platform) changed.push({ name: row.name, platform: row.platform, mode: row.mode, field: 'row', before: null, after: row.status ?? null });
      continue;
    }
    for (const field of COMPARED_FIELDS) {
      if (field === 'actualSha256' && skipHash.has(rowKey(row))) continue;
      const a = fieldValue(old, field);
      const b = fieldValue(row, field);
      if (differs(a, b)) changed.push({ name: row.name, platform: row.platform, mode: row.mode, field, before: a, after: b });
    }
  }
  return changed;
}

const fixed = (n, digits) => (n === null || n === undefined ? '–' : String(Number(n.toFixed(digits))));

function testCell(row) {
  if (!row.test) return row.status;
  const mark = row.test.passed ? '✓' : '✗';
  return row.test.value === null ? mark : `${mark} ${row.test.value}`;
}

function summaryMarkdown(generatedAt, rows) {
  const lines = [
    '# Snapshot-Ist-Soll-Vergleich',
    '',
    `Erzeugt: ${generatedAt}`,
    '',
    '| Nr | Screen | Plattform | Modus | Test | Diff % | OffByTwo % | Lagenanteil | Bänder |',
    '| --- | --- | --- | --- | --- | --- | --- | --- | --- |',
  ];
  for (const r of rows) {
    lines.push(
      `| ${r.nr} | ${r.name} | ${r.platform} | ${r.mode} | ${testCell(r)} | ${fixed(r.percentDifferent, 4)} | ${fixed(r.offByTwoPercent, 4)} | ${fixed(r.positionShare, 3)} | ${r.bandsImproved ?? '–'} |`,
    );
  }
  return lines.join('\n') + '\n';
}

function runAll(options) {
  if (options.name !== undefined || options.mode !== undefined || options.actual !== undefined) {
    throw new UsageError('--all cannot be combined with --name, --mode or --actual');
  }
  if (options.unstable !== undefined && options.baseline === undefined) throw new UsageError('--unstable requires --baseline');
  if (options.platform !== undefined && !PLATFORMS.includes(options.platform)) throw new UsageError('--platform ios|android is required');
  const platforms = options.platform === undefined ? PLATFORMS : [options.platform];
  const baseline = options.baseline === undefined ? null : readJson(options.baseline, 'baseline');
  if (baseline && !Array.isArray(baseline.rows)) throw new UsageError(`unreadable baseline file: ${options.baseline}`);
  if (baseline && options.platform !== undefined && baseline.rows.some((r) => r.platform === undefined)) {
    throw new UsageError(`--platform needs a baseline whose rows carry a platform: ${options.baseline}`);
  }
  const unstable = options.unstable === undefined ? [] : readJson(options.unstable, 'unstable');
  if (options.unstable !== undefined && !Array.isArray(unstable.changed)) {
    throw new UsageError(`unreadable unstable file: ${options.unstable}`);
  }
  const screens = options.screens ?? parseScreens(readFileSync(join(ROOT, 'design', 'screens.md'), 'utf8'));
  const out = options.out ?? join(ROOT, 'reports');
  const rows = [];
  const missingReferences = [];
  for (const platform of platforms) {
    const actualDir = options.actualDirs?.[platform] ?? join(ROOT, 'build', 'snapshots', platform);
    const referenceDir = options.referenceDirs?.[platform] ?? join(ROOT, 'design', 'reference', platform);
    const platformOut = options.outDirs?.[platform] ?? join(out, platform);
    mkdirSync(platformOut, { recursive: true });
    const platformSummary = [];
    for (const { nr, name } of screens) {
      for (const mode of MODES) {
        const actualFile = join(actualDir, `${name}_${mode}.png`);
        const referenceFile = join(referenceDir, `${name}_${mode}.png`);
        const row = { nr, name, platform, mode, status: 'ok', test: readSidecar(join(actualDir, `${name}_${mode}.result.json`)), ...emptyMetrics, actualSha256: null };
        rows.push(row);
        if (!existsSync(actualFile)) {
          row.status = 'missing-actual';
          row.test = null;
          continue;
        }
        const actual = readImage(actualFile);
        row.actualSha256 = createHash('sha256').update(actual.data).digest('hex');
        if (!existsSync(referenceFile)) {
          platformSummary.push({ name, mode, error: 'reference-missing' });
          missingReferences.push(`${platform}/${name}_${mode}`);
        } else {
          const { summary, metrics } = measureRow(platform, readImage(referenceFile), actual, join(platformOut, `${name}_${mode}.png`));
          platformSummary.push({ name, mode, ...summary });
          Object.assign(row, metrics);
          if (summary.sizeMismatch) row.status = 'size-mismatch';
        }
        if (row.status === 'ok' && row.test === null) row.status = 'missing-result';
      }
    }
    platformSummary.sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : a.mode < b.mode ? -1 : a.mode > b.mode ? 1 : 0));
    writeFileSync(join(platformOut, 'summary.json'), JSON.stringify(platformSummary, null, 2) + '\n');
  }
  rows.sort((a, b) => a.nr - b.nr || (a.platform < b.platform ? -1 : a.platform > b.platform ? 1 : a.mode < b.mode ? -1 : a.mode > b.mode ? 1 : 0));
  const totals = Object.fromEntries(
    platforms.map((p) => {
      const mine = rows.filter((r) => r.platform === p);
      return [p, {
        green: mine.filter((r) => r.test?.passed === true).length,
        red: mine.filter((r) => r.test?.passed === false).length,
        missing: mine.filter((r) => r.test === null).length,
      }];
    }),
  );
  const generatedAt = new Date().toISOString();
  const report = { generatedAt, rows, totals };
  const changed = baseline ? compareToBaseline(rows, baseline, unstable.changed ?? [], options.platform) : null;
  if (changed) report.changed = changed;
  mkdirSync(out, { recursive: true });
  const suffix = options.platform === undefined ? '' : `-${options.platform}`;
  writeFileSync(join(out, `summary${suffix}.json`), JSON.stringify(report, null, 2) + '\n');
  writeFileSync(join(out, `summary${suffix}.md`), summaryMarkdown(generatedAt, rows));
  for (const p of platforms) {
    process.stdout.write(`${p}: ${totals[p].green} grün, ${totals[p].red} rot, ${totals[p].missing} fehlt\n`);
  }
  for (const c of changed ?? []) {
    process.stdout.write(`geändert: ${c.platform}/${c.name}_${c.mode} ${c.field}: ${c.before} -> ${c.after}\n`);
  }
  if (missingReferences.length > 0) {
    process.stderr.write(`Referenz fehlt für: ${missingReferences.join(', ')}\n`);
    return 1;
  }
  if (rows.some((r) => r.status === 'missing-actual' || r.test === null)) return 3;
  return changed && changed.length > 0 ? 4 : 0;
}

export function run(options) {
  try {
    if (options.all) return runAll(options);
    if (options.baseline !== undefined) throw new UsageError('--baseline requires --all');
    if (options.unstable !== undefined) throw new UsageError('--unstable requires --baseline');
    return runSingle(options);
  } catch (e) {
    if (e instanceof UsageError) {
      process.stderr.write(`diff-report: ${e.message}\n`);
      return 2;
    }
    if (e instanceof PngError) {
      process.stderr.write(`${e.message}\n`);
      return 2;
    }
    throw e;
  }
}

function parseCli(argv) {
  let values;
  try {
    ({ values } = parseArgs({
      args: argv,
      allowPositionals: false,
      options: {
        platform: { type: 'string' },
        'actual-dir': { type: 'string' },
        'reference-dir': { type: 'string' },
        out: { type: 'string' },
        name: { type: 'string' },
        mode: { type: 'string' },
        actual: { type: 'string' },
        all: { type: 'boolean' },
        baseline: { type: 'string' },
        unstable: { type: 'string' },
      },
    }));
  } catch (e) {
    throw new UsageError(e.message);
  }
  return {
    platform: values.platform,
    actualDir: values['actual-dir'],
    referenceDir: values['reference-dir'],
    out: values.out,
    name: values.name,
    mode: values.mode,
    actual: values.actual,
    all: values.all,
    baseline: values.baseline,
    unstable: values.unstable,
  };
}

const isMain = process.argv[1] && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url));
if (isMain) {
  let code;
  try {
    code = run(parseCli(process.argv.slice(2)));
  } catch (e) {
    if (!(e instanceof UsageError)) throw e;
    process.stderr.write(`diff-report: ${e.message}\n`);
    code = 2;
  }
  process.exit(code);
}
