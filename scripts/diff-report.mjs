import { existsSync, mkdirSync, readdirSync, readFileSync, realpathSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { decodePng, encodePng, PngError } from './lib/png.mjs';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const GAP = 8;
const FADE = 0.3;
const NAME_RE = /^(.+)_(light|dark)\.png$/;

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

export function run(options) {
  try {
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
  } catch (e) {
    if (e instanceof UsageError || e instanceof PngError) {
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
  };
}

const isMain = process.argv[1] && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url));
if (isMain) {
  let code;
  try {
    code = run(parseCli(process.argv.slice(2)));
  } catch (e) {
    if (!(e instanceof UsageError)) throw e;
    process.stderr.write(`${e.message}\n`);
    code = 2;
  }
  process.exit(code);
}
