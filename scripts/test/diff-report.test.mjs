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
