import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync, readFileSync, existsSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { decodePng, encodePng } from '../lib/png.mjs';
import { run } from '../diff-report.mjs';

const SCRIPTS = fileURLToPath(new URL('../', import.meta.url));

function solid(w, h, [r, g, b, a] = [200, 200, 200, 255]) {
  const data = new Uint8Array(w * h * 4);
  for (let i = 0; i < w * h; i++) data.set([r, g, b, a], i * 4);
  return { width: w, height: h, data };
}
function setPx(img, x, y, rgba) {
  img.data.set(rgba, (y * img.width + x) * 4);
}
function px(img, x, y) {
  return [...img.data.subarray((y * img.width + x) * 4, (y * img.width + x) * 4 + 4)];
}
function fixture() {
  const root = mkdtempSync(join(tmpdir(), 'diffreport-'));
  const dirs = { root, actual: join(root, 'actual'), reference: join(root, 'reference'), out: join(root, 'out') };
  mkdirSync(dirs.actual);
  mkdirSync(dirs.reference);
  return dirs;
}
function opts(d, extra = {}) {
  return { platform: 'ios', actualDir: d.actual, referenceDir: d.reference, out: d.out, ...extra };
}
function capture(fn) {
  const origErr = process.stderr.write;
  const origOut = process.stdout.write;
  let err = '';
  let out = '';
  process.stderr.write = (s) => ((err += s), true);
  process.stdout.write = (s) => ((out += s), true);
  try {
    return { code: fn(), err, out };
  } finally {
    process.stderr.write = origErr;
    process.stdout.write = origOut;
  }
}

test('one differing pixel: panel layout, red pixel in third panel, count 1', () => {
  const d = fixture();
  const ref = solid(4, 4);
  const act = solid(4, 4);
  setPx(act, 2, 1, [0, 0, 0, 255]);
  writeFileSync(join(d.reference, 'Login_light.png'), encodePng(ref));
  writeFileSync(join(d.actual, 'Login_light.png'), encodePng(act));
  const { code } = capture(() => run(opts(d)));
  assert.equal(code, 0);
  const out = decodePng(readFileSync(join(d.out, 'Login_light.png')));
  assert.equal(out.width, 3 * 4 + 2 * 8);
  assert.equal(out.height, 4);
  const third = 2 * (4 + 8);
  assert.deepEqual(px(out, third + 2, 1), [255, 0, 0, 255]);
  assert.notDeepEqual(px(out, third + 1, 1), [255, 0, 0, 255]);
  assert.deepEqual(px(out, 4, 0), [255, 255, 255, 255]); // gap
  assert.deepEqual(px(out, 2, 1), [200, 200, 200, 255]); // reference panel
  assert.deepEqual(px(out, 4 + 8 + 2, 1), [0, 0, 0, 255]); // actual panel
  const summary = JSON.parse(readFileSync(join(d.out, 'summary.json'), 'utf8'));
  assert.equal(summary.length, 1);
  assert.deepEqual(summary[0], {
    name: 'Login', mode: 'light', reference: { w: 4, h: 4 }, actual: { w: 4, h: 4 },
    sizeMismatch: false, differentPixels: 1, percentDifferent: 6.25,
  });
  rmSync(d.root, { recursive: true });
});

test('missing reference: exit 1, other pair written, summary lists it', () => {
  const d = fixture();
  writeFileSync(join(d.reference, 'Login_light.png'), encodePng(solid(4, 4)));
  writeFileSync(join(d.actual, 'Login_light.png'), encodePng(solid(4, 4)));
  writeFileSync(join(d.actual, 'Ghost_dark.png'), encodePng(solid(4, 4)));
  const { code, err } = capture(() => run(opts(d)));
  assert.equal(code, 1);
  assert.match(err, /Ghost_dark/);
  assert.ok(existsSync(join(d.out, 'Login_light.png')));
  assert.ok(!existsSync(join(d.out, 'Ghost_dark.png')));
  const summary = JSON.parse(readFileSync(join(d.out, 'summary.json'), 'utf8'));
  assert.deepEqual(summary.map((s) => `${s.name}_${s.mode}`), ['Ghost_dark', 'Login_light']);
  assert.deepEqual(summary[0], { name: 'Ghost', mode: 'dark', error: 'reference-missing' });
  rmSync(d.root, { recursive: true });
});

test('size mismatch: flagged, padded to max height', () => {
  const d = fixture();
  writeFileSync(join(d.reference, 'Login_light.png'), encodePng(solid(4, 4)));
  writeFileSync(join(d.actual, 'Login_light.png'), encodePng(solid(4, 6)));
  const { code } = capture(() => run(opts(d)));
  assert.equal(code, 0);
  const out = decodePng(readFileSync(join(d.out, 'Login_light.png')));
  assert.equal(out.height, 6);
  const summary = JSON.parse(readFileSync(join(d.out, 'summary.json'), 'utf8'));
  assert.equal(summary[0].sizeMismatch, true);
  assert.deepEqual(summary[0].reference, { w: 4, h: 4 });
  assert.deepEqual(summary[0].actual, { w: 4, h: 6 });
  rmSync(d.root, { recursive: true });
});

test('empty or missing actual dir prints keine Ist-Bilder and exits 0', () => {
  const d = fixture();
  let r = capture(() => run(opts(d)));
  assert.equal(r.code, 0);
  assert.match(r.out + r.err, /keine Ist-Bilder/);
  r = capture(() => run(opts(d, { actualDir: join(d.root, 'nope') })));
  assert.equal(r.code, 0);
  assert.match(r.out + r.err, /keine Ist-Bilder/);
  rmSync(d.root, { recursive: true });
});

test('single pair via name/mode/actual', () => {
  const d = fixture();
  writeFileSync(join(d.reference, 'Login_dark.png'), encodePng(solid(4, 4)));
  const file = join(d.root, 'somewhere.png');
  writeFileSync(file, encodePng(solid(4, 4)));
  const { code } = capture(() => run(opts(d, { name: 'Login', mode: 'dark', actual: file })));
  assert.equal(code, 0);
  assert.ok(existsSync(join(d.out, 'Login_dark.png')));
  rmSync(d.root, { recursive: true });
});

test('invalid arguments exit 2 via the CLI wrapper; PngError exits 2', () => {
  const wrapper = join(SCRIPTS, 'diff-report');
  assert.equal(spawnSync(wrapper, []).status, 2);
  assert.equal(spawnSync(wrapper, ['--platform', 'windows']).status, 2);
  assert.equal(spawnSync(wrapper, ['--platform', 'ios', '--bogus']).status, 2);
  const d = fixture();
  writeFileSync(join(d.reference, 'Login_light.png'), encodePng(solid(4, 4)));
  writeFileSync(join(d.actual, 'Login_light.png'), 'not a png');
  assert.equal(capture(() => run(opts(d))).code, 2);
  rmSync(d.root, { recursive: true });
});

// ---- --all / --baseline / --unstable ----

const SCREENS = [
  { nr: 1, name: 'Alpha', title: 'Alpha', iosFile: 'a', androidFile: 'a', route: '/a', milestone: 'M1' },
  { nr: 2, name: 'Beta', title: 'Beta', iosFile: 'b', androidFile: 'b', route: '/b', milestone: 'M1' },
];

function allOpts(root, extra = {}) {
  return {
    all: true,
    screens: SCREENS,
    actualDirs: { ios: join(root, 'actual', 'ios'), android: join(root, 'actual', 'android') },
    referenceDirs: { ios: join(root, 'reference', 'ios'), android: join(root, 'reference', 'android') },
    outDirs: { ios: join(root, 'out', 'ios'), android: join(root, 'out', 'android') },
    out: join(root, 'reports'),
    ...extra,
  };
}

function allTree(screens = SCREENS, { sidecar = true, img = () => solid(2, 2) } = {}) {
  const root = mkdtempSync(join(tmpdir(), 'diffall-'));
  for (const p of ['ios', 'android']) {
    for (const k of ['actual', 'reference']) mkdirSync(join(root, k, p), { recursive: true });
    for (const s of screens) {
      for (const m of ['light', 'dark']) {
        writeFileSync(join(root, 'reference', p, `${s.name}_${m}.png`), encodePng(solid(2, 2)));
        writeFileSync(join(root, 'actual', p, `${s.name}_${m}.png`), encodePng(img(p, s, m)));
        if (sidecar) writeSidecar(root, p, s.name, m, { passed: true, value: null });
      }
    }
  }
  return root;
}

function writeSidecar(root, platform, name, mode, { passed, value, message = '' }) {
  const body = {
    platform, name, mode, passed, message, value,
    required: platform === 'ios' ? 0.995 : 0.5,
    unit: platform === 'ios' ? 'precision' : 'percent',
  };
  writeFileSync(join(root, 'actual', platform, `${name}_${mode}.result.json`), JSON.stringify(body));
}

const readSummary = (root) => JSON.parse(readFileSync(join(root, 'reports', 'summary.json'), 'utf8'));
const rowOf = (s, name, platform, mode) => s.rows.find((r) => r.name === name && r.platform === platform && r.mode === mode);

test('--all reports missing-actual for an absent screen and exits 3', () => {
  const root = allTree();
  rmSync(join(root, 'actual', 'ios', 'Beta_dark.png'));
  const { code } = capture(() => run(allOpts(root)));
  assert.equal(code, 3);
  const s = readSummary(root);
  const row = rowOf(s, 'Beta', 'ios', 'dark');
  assert.equal(row.status, 'missing-actual');
  assert.equal(row.actualSha256, null);
  assert.equal(row.percentDifferent, null);
  assert.equal(s.totals.ios.missing, 1);
  rmSync(root, { recursive: true });
});

test('--all reads the sidecar verdict and value', () => {
  const root = allTree();
  writeSidecar(root, 'ios', 'Alpha', 'light', { passed: false, value: 0.9876, message: 'm' });
  writeSidecar(root, 'android', 'Alpha', 'dark', { passed: false, value: 1.25 });
  const { code } = capture(() => run(allOpts(root)));
  assert.equal(code, 0);
  const s = readSummary(root);
  const ios = rowOf(s, 'Alpha', 'ios', 'light');
  assert.equal(ios.status, 'ok');
  assert.deepEqual(ios.test, { passed: false, value: 0.9876, required: 0.995, unit: 'precision' });
  assert.equal(ios.offByTwoPercent, null);
  assert.equal(ios.nr, 1);
  const and = rowOf(s, 'Alpha', 'android', 'dark');
  assert.deepEqual(and.test, { passed: false, value: 1.25, required: 0.5, unit: 'percent' });
  assert.equal(and.offByTwoPercent, 0);
  assert.match(and.actualSha256, /^[0-9a-f]{64}$/);
  assert.deepEqual(s.totals.ios, { green: 3, red: 1, missing: 0 });
  assert.deepEqual(s.totals.android, { green: 3, red: 1, missing: 0 });
  rmSync(root, { recursive: true });
});

test('--all writes summary.md with 136 rows for a full synthetic tree', () => {
  const screens = Array.from({ length: 34 }, (_, i) => ({
    nr: i + 1, name: `S${String(i + 1).padStart(2, '0')}`, title: 't', iosFile: 'f', androidFile: 'f', route: '/', milestone: 'M1',
  }));
  const root = allTree(screens);
  const { code } = capture(() => run(allOpts(root, { screens })));
  assert.equal(code, 0);
  const s = readSummary(root);
  assert.equal(s.rows.length, 136);
  assert.ok(typeof s.generatedAt === 'string');
  const md = readFileSync(join(root, 'reports', 'summary.md'), 'utf8').split('\n');
  const header = md.findIndex((l) => l.startsWith('| Nr '));
  assert.ok(header >= 0);
  assert.match(md[header], /\| Nr \| Screen \| Plattform \| Modus \| Test \| Diff % \| OffByTwo % \| Lagenanteil \| Bänder \|/);
  const body = md.slice(header + 2).filter((l) => l.startsWith('|'));
  assert.equal(body.length, 136);
  assert.match(body[0], /^\| 1 \| S01 \| android \| dark \|/);
  assert.match(body[1], /^\| 1 \| S01 \| android \| light \|/);
  assert.match(body[2], /^\| 1 \| S01 \| ios \| dark \|/);
  assert.ok(existsSync(join(root, 'out', 'ios', 'S01_light.png')));
  rmSync(root, { recursive: true });
});

test('--baseline lists a changed value and exits 4; identical inputs exit 0', () => {
  const root = allTree();
  assert.equal(capture(() => run(allOpts(root))).code, 0);
  const baseline = join(root, 'baseline.json');
  writeFileSync(baseline, readFileSync(join(root, 'reports', 'summary.json')));
  assert.equal(capture(() => run(allOpts(root, { baseline }))).code, 0);
  assert.deepEqual(readSummary(root).changed, []);
  writeSidecar(root, 'ios', 'Beta', 'light', { passed: false, value: 0.9 });
  assert.equal(capture(() => run(allOpts(root, { baseline }))).code, 4);
  const changed = readSummary(root).changed;
  assert.deepEqual(changed.map((c) => [c.name, c.platform, c.mode, c.field]).sort(), [
    ['Beta', 'ios', 'light', 'test.passed'],
    ['Beta', 'ios', 'light', 'test.value'],
  ]);
  const v = changed.find((c) => c.field === 'test.value');
  assert.equal(v.before, null);
  assert.equal(v.after, 0.9);
  rmSync(root, { recursive: true });
});

// An actual that differs from the 200-grey reference everywhere; `tweak` moves one pixel by one level.
function writeGrey(root, tweak) {
  const img = solid(2, 2, [202, 202, 202, 255]);
  if (tweak) setPx(img, 0, 0, [203, 202, 202, 255]);
  writeFileSync(join(root, 'actual', 'ios', 'Alpha_light.png'), encodePng(img));
}
function keepBaseline(root, file) {
  const target = join(root, file);
  writeFileSync(target, readFileSync(join(root, 'reports', 'summary.json')));
  return target;
}

test('--baseline reports actualSha256 for a one-pixel change that keeps every count', () => {
  const root = allTree();
  writeGrey(root, false);
  capture(() => run(allOpts(root)));
  const baseline = keepBaseline(root, 'baseline.json');
  writeGrey(root, true);
  const { code } = capture(() => run(allOpts(root, { baseline })));
  assert.equal(code, 4);
  const changed = readSummary(root).changed;
  assert.deepEqual(changed.map((c) => [c.name, c.platform, c.mode, c.field]), [['Alpha', 'ios', 'light', 'actualSha256']]);
  rmSync(root, { recursive: true });
});

test('--unstable skips the hash of listed rows but still reports their value changes', () => {
  const root = allTree();
  writeGrey(root, false);
  capture(() => run(allOpts(root)));
  const baseline = keepBaseline(root, 'baseline.json');
  // The second run only moves the hash: that is the unstable list.
  writeGrey(root, true);
  capture(() => run(allOpts(root, { baseline })));
  const unstable = keepBaseline(root, 'unstable.json');
  assert.equal(capture(() => run(allOpts(root, { baseline, unstable }))).code, 0);
  assert.deepEqual(readSummary(root).changed, []);
  // The same row now also changes a value: only the value is reported.
  writeSidecar(root, 'ios', 'Alpha', 'light', { passed: false, value: 0.5 });
  assert.equal(capture(() => run(allOpts(root, { baseline, unstable }))).code, 4);
  assert.deepEqual(readSummary(root).changed.map((c) => c.field).sort(), ['test.passed', 'test.value']);
  // A row that is not listed is still hash-compared.
  writeFileSync(join(root, 'actual', 'ios', 'Beta_dark.png'), encodePng(solid(2, 2, [1, 2, 3, 255])));
  capture(() => run(allOpts(root, { baseline, unstable })));
  assert.ok(readSummary(root).changed.some((c) => c.name === 'Beta' && c.field === 'actualSha256'));
  rmSync(root, { recursive: true });
});

test('--all reports size-mismatch before missing-result', () => {
  const root = allTree();
  writeFileSync(join(root, 'actual', 'ios', 'Alpha_light.png'), encodePng(solid(2, 3)));
  rmSync(join(root, 'actual', 'ios', 'Alpha_light.result.json'));
  writeSidecar(root, 'ios', 'Alpha', 'dark', { passed: true, value: null });
  rmSync(join(root, 'actual', 'ios', 'Alpha_dark.result.json'));
  writeFileSync(join(root, 'actual', 'ios', 'Alpha_dark.png'), encodePng(solid(2, 2)));
  const { code } = capture(() => run(allOpts(root)));
  assert.equal(code, 3);
  const s = readSummary(root);
  assert.equal(rowOf(s, 'Alpha', 'ios', 'light').status, 'size-mismatch');
  assert.equal(rowOf(s, 'Alpha', 'ios', 'dark').status, 'missing-result');
  // A size mismatch still reads the sidecar when present.
  writeSidecar(root, 'ios', 'Alpha', 'light', { passed: false, value: null });
  capture(() => run(allOpts(root)));
  const row = rowOf(readSummary(root), 'Alpha', 'ios', 'light');
  assert.equal(row.status, 'size-mismatch');
  assert.equal(row.test.passed, false);
  assert.equal(row.offByTwoPercent, null);
  rmSync(root, { recursive: true });
});

test('usage errors exit 2', () => {
  const root = allTree();
  const bad = (extra, re) => {
    const r = capture(() => run(allOpts(root, extra)));
    assert.equal(r.code, 2);
    assert.match(r.err, re);
  };
  bad({ name: 'Alpha' }, /^diff-report: /);
  bad({ mode: 'light' }, /^diff-report: /);
  bad({ actual: 'x.png' }, /^diff-report: /);
  bad({ baseline: join(root, 'nope.json') }, /^diff-report: .*baseline/);
  const ok = join(root, 'ok.json');
  writeFileSync(ok, JSON.stringify({ rows: [], changed: [] }));
  bad({ baseline: ok, unstable: join(root, 'nope.json') }, /^diff-report: .*unstable/);
  const noAll = capture(() => run({ platform: 'ios', actualDir: join(root, 'actual', 'ios'), referenceDir: join(root, 'reference', 'ios'), out: join(root, 'o'), baseline: ok }));
  assert.equal(noAll.code, 2);
  assert.match(noAll.err, /^diff-report: .*--all/);
  const noBase = capture(() => run(allOpts(root, { unstable: ok })));
  assert.equal(noBase.code, 2);
  assert.match(noBase.err, /^diff-report: .*--baseline/);
  const wrapper = join(SCRIPTS, 'diff-report');
  assert.equal(spawnSync(wrapper, ['--all', '--name', 'X']).status, 2);
  assert.equal(spawnSync(wrapper, ['--platform', 'ios', '--baseline', ok]).status, 2);
  assert.equal(spawnSync(wrapper, ['--all', '--unstable', ok]).status, 2);
  rmSync(root, { recursive: true });
});

// ---- --all --platform <p> ----

const readPlatformSummary = (root, p) => JSON.parse(readFileSync(join(root, 'reports', `summary-${p}.json`), 'utf8'));

test('--all --platform android writes summary-android.md with 68 rows for a full synthetic tree', () => {
  const screens = Array.from({ length: 34 }, (_, i) => ({
    nr: i + 1, name: `S${String(i + 1).padStart(2, '0')}`, title: 't', iosFile: 'f', androidFile: 'f', route: '/', milestone: 'M1',
  }));
  const root = allTree(screens);
  const { code } = capture(() => run(allOpts(root, { screens, platform: 'android' })));
  assert.equal(code, 0);
  const s = readPlatformSummary(root, 'android');
  assert.equal(s.rows.length, 68);
  assert.ok(s.rows.every((r) => r.platform === 'android'));
  assert.deepEqual(Object.keys(s.totals), ['android']);
  assert.deepEqual(s.totals.android, { green: 68, red: 0, missing: 0 });
  const md = readFileSync(join(root, 'reports', 'summary-android.md'), 'utf8').split('\n');
  const header = md.findIndex((l) => l.startsWith('| Nr '));
  assert.ok(header >= 0);
  assert.equal(md.slice(header + 2).filter((l) => l.startsWith('|')).length, 68);
  assert.ok(existsSync(join(root, 'out', 'android', 'S01_light.png')));
  assert.ok(existsSync(join(root, 'out', 'android', 'summary.json')));
  assert.ok(!existsSync(join(root, 'reports', 'summary.json')));
  assert.ok(!existsSync(join(root, 'out', 'ios', 'S01_light.png')));
  rmSync(root, { recursive: true });
});

test('--all --platform android ignores missing ios actuals and references', () => {
  const root = allTree();
  rmSync(join(root, 'actual', 'ios'), { recursive: true });
  rmSync(join(root, 'reference', 'ios'), { recursive: true });
  const { code } = capture(() => run(allOpts(root, { platform: 'android' })));
  assert.equal(code, 0);
  const s = readPlatformSummary(root, 'android');
  assert.equal(s.rows.length, 4);
  assert.ok(!s.rows.some((r) => r.platform === 'ios'));
  rmSync(root, { recursive: true });
});

test('--all --platform android --baseline ignores ios rows of a 136-row baseline', () => {
  const root = allTree();
  assert.equal(capture(() => run(allOpts(root))).code, 0);
  const baseline = keepBaseline(root, 'baseline.json');
  assert.equal(readSummary(root).rows.length, 8);
  // An ios change and an ios deletion do not matter for an android run.
  writeSidecar(root, 'ios', 'Beta', 'light', { passed: false, value: 0.9 });
  rmSync(join(root, 'actual', 'ios', 'Alpha_dark.png'));
  const same = capture(() => run(allOpts(root, { baseline, platform: 'android' })));
  assert.equal(same.code, 0);
  assert.deepEqual(readPlatformSummary(root, 'android').changed, []);
  writeSidecar(root, 'android', 'Beta', 'dark', { passed: false, value: 1.5 });
  const changedRun = capture(() => run(allOpts(root, { baseline, platform: 'android' })));
  assert.equal(changedRun.code, 4);
  assert.deepEqual(
    readPlatformSummary(root, 'android').changed.map((c) => [c.name, c.platform, c.mode, c.field]).sort(),
    [['Beta', 'android', 'dark', 'test.passed'], ['Beta', 'android', 'dark', 'test.value']],
  );
  // A selected-platform row missing from the baseline is a change.
  const partial = join(root, 'partial.json');
  const full = JSON.parse(readFileSync(baseline, 'utf8'));
  full.rows = full.rows.filter((r) => !(r.platform === 'android' && r.name === 'Alpha' && r.mode === 'light'));
  writeFileSync(partial, JSON.stringify(full));
  writeSidecar(root, 'android', 'Beta', 'dark', { passed: true, value: null });
  const missingRow = capture(() => run(allOpts(root, { baseline: partial, platform: 'android' })));
  assert.equal(missingRow.code, 4);
  assert.deepEqual(readPlatformSummary(root, 'android').changed, [
    { name: 'Alpha', platform: 'android', mode: 'light', field: 'row', before: null, after: rowOf(readPlatformSummary(root, 'android'), 'Alpha', 'android', 'light').status },
  ]);
  rmSync(root, { recursive: true });
});

test('--all --platform android refuses a baseline whose rows carry no platform', () => {
  const root = allTree();
  assert.equal(capture(() => run(allOpts(root))).code, 0);
  const baseline = keepBaseline(root, 'baseline.json');
  const old = JSON.parse(readFileSync(baseline, 'utf8'));
  for (const r of old.rows) delete r.platform;
  writeFileSync(baseline, JSON.stringify(old));
  const r = capture(() => run(allOpts(root, { baseline, platform: 'android' })));
  assert.equal(r.code, 2);
  assert.match(r.err, /^diff-report: --platform needs a baseline/);
  rmSync(root, { recursive: true });
});

test('--all --platform xx exits 2', () => {
  const root = allTree();
  const r = capture(() => run(allOpts(root, { platform: 'xx' })));
  assert.equal(r.code, 2);
  assert.match(r.err, /^diff-report: --platform ios\|android is required/);
  rmSync(root, { recursive: true });
});
