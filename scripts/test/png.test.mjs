import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';
import { fileURLToPath } from 'node:url';
import { decodePng, encodePng, PngError } from '../lib/png.mjs';

const ROOT = fileURLToPath(new URL('../../', import.meta.url));

const crcTable = new Uint32Array(256).map((_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
function crc32(buf) {
  let c = 0xffffffff;
  for (const b of buf) c = crcTable[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const out = Buffer.alloc(12 + data.length);
  out.writeUInt32BE(data.length, 0);
  out.write(type, 4, 'latin1');
  data.copy(out, 8);
  out.writeUInt32BE(crc32(out.subarray(4, 8 + data.length)), 8 + data.length);
  return out;
}
function rawPng({ width, height, depth, colour, interlace = 0, scanlines }) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = depth;
  ihdr[9] = colour;
  ihdr[12] = interlace;
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(scanlines)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

test('decodes the real reference screenshot as 1170x2532 RGBA', () => {
  const img = decodePng(readFileSync(`${ROOT}design/reference/ios/Login_light.png`));
  assert.equal(img.width, 1170);
  assert.equal(img.height, 2532);
  assert.equal(img.data.length, 1170 * 2532 * 4);
});

test('encode then decode round-trips identically', () => {
  const data = new Uint8Array(3 * 2 * 4).map((_, i) => (i * 37) % 256);
  const out = decodePng(encodePng({ width: 3, height: 2, data }));
  assert.equal(out.width, 3);
  assert.equal(out.height, 2);
  assert.deepEqual([...out.data], [...data]);
});

test('16-bit RGBA decodes to the high bytes', () => {
  const row = Buffer.from([0, 0x12, 0x34, 0x56, 0x78, 0x9a, 0xbc, 0xde, 0xf0]);
  const out = decodePng(rawPng({ width: 1, height: 1, depth: 16, colour: 6, scanlines: row }));
  assert.deepEqual([...out.data], [0x12, 0x56, 0x9a, 0xde]);
});

test('16-bit RGB with Sub and Up filters uses the real bytes-per-pixel', () => {
  // 2x2, 6 bytes per pixel. Row 0 filter 1 (Sub), row 1 filter 2 (Up).
  const row0 = Buffer.from([1, 10, 0, 20, 0, 30, 0, 1, 0, 1, 0, 1, 0]);
  const row1 = Buffer.from([2, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0]);
  const out = decodePng(rawPng({ width: 2, height: 2, depth: 16, colour: 2, scanlines: Buffer.concat([row0, row1]) }));
  assert.deepEqual([...out.data], [10, 20, 30, 255, 11, 21, 31, 255, 11, 21, 31, 255, 12, 22, 32, 255]);
});

test('Average and Paeth filters decode', () => {
  // 2x2 RGB 8-bit. Row 0: Average; row 1: Paeth.
  const row0 = Buffer.from([3, 10, 20, 30, 5, 5, 5]);
  const row1 = Buffer.from([4, 1, 1, 1, 1, 1, 1]);
  const out = decodePng(rawPng({ width: 2, height: 2, depth: 8, colour: 2, scanlines: Buffer.concat([row0, row1]) }));
  // row0: px0 = 10,20,30 ; px1 = 5+floor(10/2)=10, 5+10=15, 5+15=20
  // row1: px0 paeth(left 0, up px0, ul 0) -> up: 11,21,31 ; px1 paeth(left px0', up px1, ul px0)
  assert.deepEqual([...out.data.subarray(0, 8)], [10, 20, 30, 255, 10, 15, 20, 255]);
  assert.deepEqual([...out.data.subarray(8, 12)], [11, 21, 31, 255]);
});

test('interlaced and palette headers throw unsupported', () => {
  const interlaced = rawPng({ width: 1, height: 1, depth: 8, colour: 6, interlace: 1, scanlines: Buffer.alloc(5) });
  assert.throws(() => decodePng(interlaced), (e) => e instanceof PngError && e.code === 'unsupported' && /interlace 1/.test(e.message));
  const palette = rawPng({ width: 1, height: 1, depth: 8, colour: 3, scanlines: Buffer.alloc(2) });
  assert.throws(() => decodePng(palette), (e) => e instanceof PngError && e.code === 'unsupported' && /colour type 3/.test(e.message));
});

test('non-PNG input throws not-png', () => {
  assert.throws(() => decodePng(Buffer.from('hello world, definitely not png')), (e) => e instanceof PngError && e.code === 'not-png');
});

test('multiple IDAT chunks and ancillary chunks are handled', () => {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(2, 0);
  ihdr.writeUInt32BE(1, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const z = deflateSync(Buffer.from([0, 1, 2, 3, 4, 5, 6, 7, 8]));
  const png = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('sRGB', Buffer.from([0])),
    chunk('IDAT', z.subarray(0, 5)),
    chunk('pHYs', Buffer.alloc(9)),
    chunk('IDAT', z.subarray(5)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
  assert.deepEqual([...decodePng(png).data], [1, 2, 3, 4, 5, 6, 7, 8]);
});
