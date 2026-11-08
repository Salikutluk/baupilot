// Minimal deterministic PDF writer: one 24 x 24 page holding one icon path. No compression, dates or IDs.

import { arcToCubics, formatNumber } from './svg-path.mjs';

const CAPS = { butt: 0, round: 1, square: 2 };
const JOINS = { miter: 0, round: 1, bevel: 2 };

const enumValue = (table, value, label) => {
  if (value === undefined) return 0;
  if (typeof value === 'number' && Object.values(table).includes(value)) return value;
  if (value in table) return table[value];
  throw new Error(`unknown ${label} ${value}`);
};

// `commands` are absolute: M, L, C, Q, A and Z (see toAbsolute). Q and A are written as cubics.
function pathOps(commands) {
  const f = formatNumber;
  const ops = [];
  let cx = 0;
  let cy = 0;
  let sx = 0;
  let sy = 0;
  for (const { cmd, args } of commands) {
    switch (cmd) {
      case 'M':
        ops.push(`${f(args[0])} ${f(args[1])} m`);
        [cx, cy] = args;
        [sx, sy] = args;
        break;
      case 'L':
        ops.push(`${f(args[0])} ${f(args[1])} l`);
        [cx, cy] = args;
        break;
      case 'C':
        ops.push(`${args.map(f).join(' ')} c`);
        [cx, cy] = args.slice(4);
        break;
      case 'Q': {
        const [qx, qy, x, y] = args;
        const c = [cx + (2 / 3) * (qx - cx), cy + (2 / 3) * (qy - cy), x + (2 / 3) * (qx - x), y + (2 / 3) * (qy - y), x, y];
        ops.push(`${c.map(f).join(' ')} c`);
        [cx, cy] = [x, y];
        break;
      }
      case 'A': {
        const [rx, ry, phi, fa, fs, x, y] = args;
        for (const c of arcToCubics(cx, cy, rx, ry, phi, fa, fs, x, y)) ops.push(`${c.map(f).join(' ')} c`);
        [cx, cy] = [x, y];
        break;
      }
      case 'Z':
        ops.push('h');
        [cx, cy] = [sx, sy];
        break;
      default:
        throw new Error(`unsupported path command ${cmd}`);
    }
  }
  return ops;
}

export function iconPdf({ commands, stroke = 1, fill = false, lineCap = 'butt', lineJoin = 'miter' }) {
  const content = [
    '1 0 0 -1 0 24 cm',
    `${formatNumber(stroke)} w`,
    `${enumValue(CAPS, lineCap, 'lineCap')} J`,
    `${enumValue(JOINS, lineJoin, 'lineJoin')} j`,
    '0 G',
    '0 g',
    ...pathOps(commands),
    fill ? 'f' : 'S',
  ].join('\n');

  const objects = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 24 24] /Contents 4 0 R /Resources << >> >>',
    `<< /Length ${content.length + 1} >>\nstream\n${content}\nendstream`,
  ];

  let out = '%PDF-1.4\n';
  const offsets = [];
  objects.forEach((body, index) => {
    offsets.push(out.length);
    out += `${index + 1} 0 obj\n${body}\nendobj\n`;
  });
  const xrefAt = out.length;
  out += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  for (const offset of offsets) out += `${String(offset).padStart(10, '0')} 00000 n \n`;
  out += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefAt}\n%%EOF\n`;
  return Buffer.from(out, 'latin1');
}
