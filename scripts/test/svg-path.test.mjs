import { test } from 'node:test';
import assert from 'node:assert/strict';
import { arcToCubics, shapeToPath, SvgPathError, tokenize, toAbsolute } from '../lib/svg-path.mjs';

const SEARCH = 'M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM21 21l-4.3-4.3';
const BELL = 'M10.3 21a1.94 1.94 0 0 0 3.4 0';

const bezier = (p0, c) => {
  const [c1x, c1y, c2x, c2y, x, y] = c;
  return (t) => {
    const u = 1 - t;
    return [
      u ** 3 * p0[0] + 3 * u * u * t * c1x + 3 * u * t * t * c2x + t ** 3 * x,
      u ** 3 * p0[1] + 3 * u * u * t * c1y + 3 * u * t * t * c2y + t ** 3 * y,
    ];
  };
};

// Samples every cubic of an absolute path (arcs converted) and returns the points.
function sample(d, steps = 24) {
  const pts = [];
  let cur = [0, 0];
  let start = [0, 0];
  for (const { cmd, args } of toAbsolute(tokenize(d))) {
    if (cmd === 'M') {
      cur = [args[0], args[1]];
      start = cur;
      pts.push(cur);
    } else if (cmd === 'L') {
      cur = [args[0], args[1]];
      pts.push(cur);
    } else if (cmd === 'A') {
      for (const c of arcToCubics(cur[0], cur[1], ...args)) {
        const f = bezier(cur, c);
        for (let i = 1; i <= steps; i += 1) pts.push(f(i / steps));
        cur = [c[4], c[5]];
      }
    } else if (cmd === 'Z') {
      cur = start;
    }
  }
  return pts;
}

const bounds = (pts) => ({
  minX: Math.min(...pts.map((p) => p[0])),
  maxX: Math.max(...pts.map((p) => p[0])),
  minY: Math.min(...pts.map((p) => p[1])),
  maxY: Math.max(...pts.map((p) => p[1])),
});

const close = (a, b, eps = 1e-6) => assert.ok(Math.abs(a - b) <= eps, `${a} != ${b}`);

test('T-IC-1 tokenize handles packed numbers and arc flags', () => {
  assert.deepEqual(tokenize(SEARCH), [
    { cmd: 'M', args: [11, 18] },
    { cmd: 'a', args: [7, 7, 0, 1, 0, 0, -14] },
    { cmd: 'a', args: [7, 7, 0, 0, 0, 0, 14] },
    { cmd: 'z', args: [] },
    { cmd: 'M', args: [21, 21] },
    { cmd: 'l', args: [-4.3, -4.3] },
  ]);
  assert.deepEqual(tokenize(BELL), [
    { cmd: 'M', args: [10.3, 21] },
    { cmd: 'a', args: [1.94, 1.94, 0, 0, 0, 3.4, 0] },
  ]);
  // packed flags without separators, leading dots, exponents, implicit M -> L repetition
  assert.deepEqual(tokenize('M0 0a1 1 0 1022 0'), [
    { cmd: 'M', args: [0, 0] },
    { cmd: 'a', args: [1, 1, 0, 1, 0, 22, 0] },
  ]);
  assert.deepEqual(tokenize('m.01.02 1e1-2E-1 3 4'), [
    { cmd: 'm', args: [0.01, 0.02] },
    { cmd: 'l', args: [10, -0.2] },
    { cmd: 'l', args: [3, 4] },
  ]);
  assert.deepEqual(tokenize('M1,2 3 4h5 6'), [
    { cmd: 'M', args: [1, 2] },
    { cmd: 'L', args: [3, 4] },
    { cmd: 'h', args: [5] },
    { cmd: 'h', args: [6] },
  ]);
});

test('tokenize rejects malformed path data with bad-token and the offset', () => {
  for (const [d, at] of [
    ['L1 1', 0],
    ['M1', 2],
    ['M1 1 X', 5],
    ['M1 1a1 1 0 2 0 1 1', 11],
    ['M1 1z5', 5],
  ]) {
    assert.throws(
      () => tokenize(d),
      (e) => e instanceof SvgPathError && e.code === 'bad-token' && e.at === at,
      d,
    );
  }
});

test('toAbsolute resolves relative commands and expands H V S T', () => {
  assert.deepEqual(toAbsolute(tokenize('M1 1l2 3h4v-1z m1 1')), [
    { cmd: 'M', args: [1, 1] },
    { cmd: 'L', args: [3, 4] },
    { cmd: 'L', args: [7, 4] },
    { cmd: 'L', args: [7, 3] },
    { cmd: 'Z', args: [] },
    { cmd: 'M', args: [2, 2] },
  ]);
  assert.deepEqual(toAbsolute(tokenize('M0 0C1 1 2 1 3 0S5-1 6 0s1 1 2 0')), [
    { cmd: 'M', args: [0, 0] },
    { cmd: 'C', args: [1, 1, 2, 1, 3, 0] },
    { cmd: 'C', args: [4, -1, 5, -1, 6, 0] },
    { cmd: 'C', args: [7, 1, 7, 1, 8, 0] },
  ]);
  assert.deepEqual(toAbsolute(tokenize('M0 0Q1 2 2 0T4 0t2 0')), [
    { cmd: 'M', args: [0, 0] },
    { cmd: 'Q', args: [1, 2, 2, 0] },
    { cmd: 'Q', args: [3, -2, 4, 0] },
    { cmd: 'Q', args: [5, 2, 6, 0] },
  ]);
  // S without a preceding curve uses the current point as first control point
  assert.deepEqual(toAbsolute(tokenize('M1 1S2 2 3 3'))[1], { cmd: 'C', args: [1, 1, 2, 2, 3, 3] });
  // arcs are never dropped; the end point becomes absolute
  assert.deepEqual(toAbsolute(tokenize(BELL)), [
    { cmd: 'M', args: [10.3, 21] },
    { cmd: 'A', args: [1.94, 1.94, 0, 0, 0, 13.7, 21] },
  ]);
  // after z the current point is the sub-path start
  assert.deepEqual(toAbsolute(tokenize('M2 2l1 1zl1 0'))[3], { cmd: 'L', args: [3, 2] });
});

test('T-IC-2 arcToCubics endpoints match and a full circle stays within 0.01 of radius', () => {
  const cubics = arcToCubics(11, 18, 7, 7, 0, 1, 0, 11, 4);
  assert.deepEqual(cubics[cubics.length - 1].slice(4), [11, 4]);
  // half circle, <= 90 degrees per segment
  assert.equal(cubics.length, 2);

  // full circle as two half arcs around (12, 12), radius 7
  const pts = sample('M5 12A7 7 0 1 0 19 12A7 7 0 1 0 5 12Z');
  assert.ok(pts.length > 40);
  for (const [x, y] of pts) assert.ok(Math.abs(Math.hypot(x - 12, y - 12) - 7) < 0.01, `${x},${y}`);

  // sweep flag chooses the side; large-arc flag the size
  const small = arcToCubics(0, 0, 5, 5, 0, 0, 1, 5, 5);
  assert.equal(small.length, 1);
  const large = arcToCubics(0, 0, 5, 5, 0, 1, 1, 5, 5);
  assert.equal(large.length, 3);
  const mid = bezier([0, 0], small[0])(0.5);
  assert.ok(mid[0] > 2.5 && mid[1] < 2.5, 'sweep 1 bulges to the upper right (y down)');

  // radii too small are scaled up so the arc still reaches its end point
  const scaled = arcToCubics(0, 0, 1, 1, 0, 0, 1, 10, 0);
  assert.deepEqual(scaled[scaled.length - 1].slice(4), [10, 0]);
  // degenerate cases
  assert.deepEqual(arcToCubics(3, 3, 5, 5, 0, 0, 1, 3, 3), []);
  assert.deepEqual(arcToCubics(0, 0, 0, 5, 0, 0, 1, 4, 4), [[0, 0, 4, 4, 4, 4]]);
});

test('T-IC-3 shapeToPath circle/rect rx/line give closed paths with expected bounds', () => {
  const circle = shapeToPath({ tag: 'circle', attrs: { cx: '12', cy: '12', r: '3' } });
  assert.match(circle, /Z$/);
  let b = bounds(sample(circle));
  close(b.minX, 9, 0.01);
  close(b.maxX, 15, 0.01);
  close(b.minY, 9, 0.01);
  close(b.maxY, 15, 0.01);

  const rect = shapeToPath({ tag: 'rect', attrs: { x: '3', y: '4', width: '18', height: '14', rx: '2' } });
  assert.match(rect, /Z$/);
  assert.match(rect, /A2 2 0 0 1/);
  b = bounds(sample(rect));
  close(b.minX, 3, 0.01);
  close(b.maxX, 21, 0.01);
  close(b.minY, 4, 0.01);
  close(b.maxY, 18, 0.01);
  assert.equal(shapeToPath({ tag: 'rect', attrs: { x: 1, y: 2, width: 3, height: 4 } }), 'M1 2H4V6H1Z');

  assert.equal(shapeToPath({ tag: 'line', attrs: { x1: 5, y1: 12, x2: '19', y2: 12 } }), 'M5 12L19 12');
  assert.equal(shapeToPath({ tag: 'polyline', attrs: { points: '4 12 9,17 20 6' } }), 'M4 12L9 17L20 6');
  assert.equal(shapeToPath({ tag: 'polygon', attrs: { points: '12 3 21 20 3 20' } }), 'M12 3L21 20L3 20Z');
  assert.equal(shapeToPath({ tag: 'path', attrs: { d: 'M1 1' } }), 'M1 1');
});

test('shapeToPath rejects unsupported elements', () => {
  assert.throws(
    () => shapeToPath({ tag: 'ellipse', attrs: {} }),
    (e) => e instanceof SvgPathError && e.code === 'unsupported-element' && e.at === 'ellipse',
  );
  assert.throws(
    () => shapeToPath({ tag: 'polygon', attrs: { points: '1 2 3' } }),
    (e) => e instanceof SvgPathError && e.code === 'unsupported-element',
  );
});
