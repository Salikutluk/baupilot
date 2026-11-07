import { deflateSync, inflateSync } from 'node:zlib';

const SIGNATURE = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);

export class PngError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'PngError';
    this.code = code;
  }
}

const CHANNELS = { 2: 3, 6: 4 };

function paeth(a, b, c) {
  const p = a + b - c;
  const pa = Math.abs(p - a);
  const pb = Math.abs(p - b);
  const pc = Math.abs(p - c);
  if (pa <= pb && pa <= pc) return a;
  return pb <= pc ? b : c;
}

function unfilter(raw, width, height, bpp) {
  const stride = width * bpp;
  const out = new Uint8Array(stride * height);
  for (let y = 0; y < height; y++) {
    const filter = raw[y * (stride + 1)];
    const src = y * (stride + 1) + 1;
    const dst = y * stride;
    const prev = dst - stride;
    if (filter > 4) throw new PngError('unsupported', `unsupported: filter ${filter}`);
    for (let i = 0; i < stride; i++) {
      const left = i >= bpp ? out[dst + i - bpp] : 0;
      const up = y > 0 ? out[prev + i] : 0;
      const upLeft = y > 0 && i >= bpp ? out[prev + i - bpp] : 0;
      let add = 0;
      if (filter === 1) add = left;
      else if (filter === 2) add = up;
      else if (filter === 3) add = (left + up) >> 1;
      else if (filter === 4) add = paeth(left, up, upLeft);
      out[dst + i] = (raw[src + i] + add) & 0xff;
    }
  }
  return out;
}

export function decodePng(buf) {
  if (buf.length < 8 || !SIGNATURE.equals(buf.subarray(0, 8))) {
    throw new PngError('not-png', 'not-png: missing PNG signature');
  }
  let header = null;
  const idat = [];
  let pos = 8;
  while (pos + 8 <= buf.length) {
    const length = buf.readUInt32BE(pos);
    const type = buf.toString('latin1', pos + 4, pos + 8);
    const body = buf.subarray(pos + 8, pos + 8 + length);
    if (body.length < length) throw new PngError('not-png', 'not-png: truncated chunk ' + type);
    pos += 12 + length;
    if (type === 'IHDR') {
      header = {
        width: body.readUInt32BE(0),
        height: body.readUInt32BE(4),
        depth: body[8],
        colour: body[9],
        interlace: body[12],
      };
    } else if (type === 'IDAT') {
      idat.push(body);
    } else if (type === 'IEND') {
      break;
    }
  }
  if (!header) throw new PngError('not-png', 'not-png: missing IHDR');
  if (header.interlace !== 0) throw new PngError('unsupported', `unsupported: interlace ${header.interlace}`);
  if (!(header.colour in CHANNELS)) throw new PngError('unsupported', `unsupported: colour type ${header.colour}`);
  if (header.depth !== 8 && header.depth !== 16) throw new PngError('unsupported', `unsupported: bit depth ${header.depth}`);

  const { width, height, depth, colour } = header;
  const channels = CHANNELS[colour];
  const bytes = depth / 8;
  const bpp = channels * bytes;
  let raw;
  try {
    raw = inflateSync(Buffer.concat(idat));
  } catch (e) {
    throw new PngError('not-png', `not-png: ${e.message}`);
  }
  if (raw.length < (width * bpp + 1) * height) throw new PngError('not-png', 'not-png: image data too short');
  const pixels = unfilter(raw, width, height, bpp);

  const data = new Uint8Array(width * height * 4);
  for (let p = 0; p < width * height; p++) {
    for (let c = 0; c < 4; c++) {
      data[p * 4 + c] = c < channels ? pixels[p * bpp + c * bytes] : 255;
    }
  }
  return { width, height, data };
}

const CRC_TABLE = new Uint32Array(256).map((_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, body) {
  const out = Buffer.alloc(12 + body.length);
  out.writeUInt32BE(body.length, 0);
  out.write(type, 4, 'latin1');
  Buffer.from(body.buffer, body.byteOffset, body.length).copy(out, 8);
  out.writeUInt32BE(crc32(out.subarray(4, 8 + body.length)), 8 + body.length);
  return out;
}

export function encodePng({ width, height, data }) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const stride = width * 4;
  const raw = Buffer.alloc((stride + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (stride + 1)] = 0;
    Buffer.from(data.buffer, data.byteOffset + y * stride, stride).copy(raw, y * (stride + 1) + 1);
  }
  return Buffer.concat([
    SIGNATURE,
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
