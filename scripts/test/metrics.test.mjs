import { test } from 'node:test';
import assert from 'node:assert/strict';
import { offByTwoPercent } from '../lib/offbytwo.mjs';
import { bandShift } from '../lib/band-shift.mjs';

function solid(w, h, [r, g, b] = [100, 100, 100]) {
  const data = new Uint8Array(w * h * 4);
  for (let i = 0; i < w * h; i++) data.set([r, g, b, 255], i * 4);
  return data;
}
function setPx(data, w, x, y, rgb) {
  data.set([...rgb, 255], (y * w + x) * 4);
}

// Deterministic image whose rows are all different (seeded LCG), constant along x.
function rowImage(w, h, seed) {
  const data = new Uint8Array(w * h * 4);
  let s = seed;
  for (let y = 0; y < h; y++) {
    const row = [];
    for (let c = 0; c < 3; c++) {
      s = (s * 1103515245 + 12345) % 2147483648;
      row.push((s >> 8) % 256);
    }
    for (let x = 0; x < w; x++) data.set([...row, 255], (y * w + x) * 4);
  }
  return data;
}

test('offByTwo ignores pixels within 2 levels on every channel', () => {
  const ref = solid(4, 4);
  const act = solid(4, 4);
  setPx(act, 4, 0, 0, [102, 98, 102]);
  setPx(act, 4, 1, 1, [100, 101, 99]);
  assert.equal(offByTwoPercent(ref, act, 4, 4), 0);
});

test('offByTwo sums all three channels of a differing pixel', () => {
  const ref = solid(2, 1);
  const act = solid(2, 1);
  setPx(act, 2, 0, 0, [103, 100, 90]); // max diff 10 > 2 -> counts 3 + 0 + 10
  assert.equal(offByTwoPercent(ref, act, 2, 1), (13 * 100) / (2 * 1 * 3 * 256));
});

test('offByTwo on a known 4x4 pair equals the hand-computed percentage', () => {
  // ref: 16 pixels of (100,100,100). act differs in four pixels:
  //   (103,100,100): max diff 3 > 2 -> counts 3
  //   (110, 95,102): max diff 10    -> counts 10+5+2 = 17
  //   (102,102,102): max diff 2     -> skipped entirely
  //   (100,100,140): max diff 40    -> counts 40
  // delta = 3 + 17 + 40 = 60; percent = 60*100 / (16*3*256) = 6000/12288 = 0.48828125
  const ref = solid(4, 4);
  const act = solid(4, 4);
  setPx(act, 4, 0, 0, [103, 100, 100]);
  setPx(act, 4, 1, 1, [110, 95, 102]);
  setPx(act, 4, 2, 2, [102, 102, 102]);
  setPx(act, 4, 3, 3, [100, 100, 140]);
  assert.equal(offByTwoPercent(ref, act, 4, 4), 0.48828125);
});

test('bandShift finds a 3 px vertical shift and reports positionShare 1 for a pure shift', () => {
  const w = 4;
  const h = 120;
  const ref = rowImage(w, h, 7);
  const act = new Uint8Array(ref.length);
  // act row y+3 equals ref row y; the first three act rows are never compared at s = 3.
  for (let y = 0; y < h; y++) {
    const src = y >= 3 ? y - 3 : 0;
    act.set(ref.subarray(src * w * 4, (src + 1) * w * 4), y * w * 4);
  }
  const r = bandShift(ref, act, w, h, { bandHeight: 30 });
  assert.equal(r.bands.length, 4);
  assert.deepEqual(r.bands.map((b) => b.best), [3, 3, 3, 3]);
  assert.deepEqual(r.bands.map((b) => b.y0), [0, 30, 60, 90]);
  assert.ok(r.bands.every((b) => b.devBest === 0 && b.dev0 > 0));
  assert.equal(r.positionShare, 1);
  assert.equal(r.bandsImproved, 4);
});

test('bandShift reports 0 for pure noise', () => {
  const w = 4;
  const h = 120;
  const ref = rowImage(w, h, 11);
  const act = new Uint8Array(ref);
  // Same-position noise: every pixel moves by 4 levels, alternating sign by parity.
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const sign = (x + y) % 2 === 0 ? 1 : -1;
      for (let c = 0; c < 3; c++) {
        const i = (y * w + x) * 4 + c;
        act[i] = Math.min(255, Math.max(0, ref[i] + sign * 4));
      }
    }
  }
  const r = bandShift(ref, act, w, h, { bandHeight: 30 });
  assert.ok(r.bands.every((b) => b.best === 0));
  assert.equal(r.positionShare, 0);
  assert.equal(r.bandsImproved, 0);
});
