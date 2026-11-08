// Pure SVG path helpers: tokenizer, absolute normaliser, arc to cubic conversion, basic shapes to path data.
//
// A parsed command is `{ cmd, args }`. After `toAbsolute` the commands are M, L, C, Q, A and Z only
// (H, V, S and T are expanded; A keeps its absolute end point). A cubic is `[c1x, c1y, c2x, c2y, x, y]`.

const ARG_COUNT = { M: 2, L: 2, H: 1, V: 1, C: 6, S: 4, Q: 4, T: 2, A: 7, Z: 0 };
const NUMBER_RE = /[+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?/y;

export class SvgPathError extends Error {
  constructor(code, at, message) {
    super(message);
    this.name = 'SvgPathError';
    this.code = code;
    this.at = at;
  }
}

export function formatNumber(n) {
  const rounded = Math.round(n * 1e6) / 1e6;
  return String(Object.is(rounded, -0) ? 0 : rounded);
}

export function tokenize(d) {
  if (typeof d !== 'string') throw new SvgPathError('bad-token', 0, 'path data must be a string');
  const out = [];
  let i = 0;

  const skipSeparators = () => {
    while (i < d.length && /[\s,]/.test(d[i])) i += 1;
  };
  const readNumber = () => {
    NUMBER_RE.lastIndex = i;
    const m = NUMBER_RE.exec(d);
    if (!m) throw new SvgPathError('bad-token', i, `expected a number at ${i}`);
    i += m[0].length;
    return Number(m[0]);
  };
  const readFlag = () => {
    if (d[i] !== '0' && d[i] !== '1') throw new SvgPathError('bad-token', i, `expected an arc flag (0 or 1) at ${i}`);
    i += 1;
    return Number(d[i - 1]);
  };
  const readArgs = (upper) => {
    const args = [];
    for (let k = 0; k < ARG_COUNT[upper]; k += 1) {
      skipSeparators();
      args.push(upper === 'A' && (k === 3 || k === 4) ? readFlag() : readNumber());
    }
    return args;
  };

  skipSeparators();
  if (i < d.length && !/[Mm]/.test(d[i])) throw new SvgPathError('bad-token', i, 'path data must start with M');
  while (i < d.length) {
    const letter = d[i];
    const upper = letter.toUpperCase();
    if (!/[A-Za-z]/.test(letter) || !(upper in ARG_COUNT)) {
      throw new SvgPathError('bad-token', i, `unexpected "${letter}" at ${i}`);
    }
    i += 1;
    let cmd = letter;
    out.push({ cmd, args: readArgs(upper) });
    if (upper === 'Z') {
      skipSeparators();
      continue;
    }
    // Implicit repetition: further argument groups repeat the command (M/m repeat as L/l).
    for (;;) {
      skipSeparators();
      if (i >= d.length || !/[+\-.\d]/.test(d[i])) break;
      if (upper === 'M') cmd = letter === 'M' ? 'L' : 'l';
      out.push({ cmd, args: readArgs(cmd.toUpperCase()) });
    }
  }
  return out;
}

// Absolute coordinates are sums of decimals; trim the float noise (13.700000000000001) at 1e-9.
const clean = (n) => Math.round(n * 1e9) / 1e9;

export function toAbsolute(cmds) {
  const out = [];
  let cx = 0;
  let cy = 0;
  let sx = 0;
  let sy = 0;
  let prev = null; // { type, x, y } control point of the previous C/S or Q/T, for reflection
  for (const { cmd, args } of cmds) {
    const rel = cmd !== cmd.toUpperCase();
    const upper = cmd.toUpperCase();
    const ox = rel ? cx : 0;
    const oy = rel ? cy : 0;
    let next = null;
    switch (upper) {
      case 'M':
        out.push({ cmd: 'M', args: [args[0] + ox, args[1] + oy] });
        cx = args[0] + ox;
        cy = args[1] + oy;
        sx = cx;
        sy = cy;
        break;
      case 'L':
        out.push({ cmd: 'L', args: [args[0] + ox, args[1] + oy] });
        cx = args[0] + ox;
        cy = args[1] + oy;
        break;
      case 'H':
        cx = args[0] + ox;
        out.push({ cmd: 'L', args: [cx, cy] });
        break;
      case 'V':
        cy = args[0] + oy;
        out.push({ cmd: 'L', args: [cx, cy] });
        break;
      case 'C': {
        const a = [args[0] + ox, args[1] + oy, args[2] + ox, args[3] + oy, args[4] + ox, args[5] + oy];
        out.push({ cmd: 'C', args: a });
        next = { type: 'C', x: a[2], y: a[3] };
        cx = a[4];
        cy = a[5];
        break;
      }
      case 'S': {
        const c1x = prev?.type === 'C' ? 2 * cx - prev.x : cx;
        const c1y = prev?.type === 'C' ? 2 * cy - prev.y : cy;
        const a = [c1x, c1y, args[0] + ox, args[1] + oy, args[2] + ox, args[3] + oy];
        out.push({ cmd: 'C', args: a });
        next = { type: 'C', x: a[2], y: a[3] };
        cx = a[4];
        cy = a[5];
        break;
      }
      case 'Q': {
        const a = [args[0] + ox, args[1] + oy, args[2] + ox, args[3] + oy];
        out.push({ cmd: 'Q', args: a });
        next = { type: 'Q', x: a[0], y: a[1] };
        cx = a[2];
        cy = a[3];
        break;
      }
      case 'T': {
        const qx = prev?.type === 'Q' ? 2 * cx - prev.x : cx;
        const qy = prev?.type === 'Q' ? 2 * cy - prev.y : cy;
        out.push({ cmd: 'Q', args: [qx, qy, args[0] + ox, args[1] + oy] });
        next = { type: 'Q', x: qx, y: qy };
        cx = args[0] + ox;
        cy = args[1] + oy;
        break;
      }
      case 'A':
        out.push({ cmd: 'A', args: [args[0], args[1], args[2], args[3], args[4], args[5] + ox, args[6] + oy] });
        cx = args[5] + ox;
        cy = args[6] + oy;
        break;
      case 'Z':
        out.push({ cmd: 'Z', args: [] });
        cx = sx;
        cy = sy;
        break;
      default:
        throw new SvgPathError('bad-token', 0, `unknown command ${cmd}`);
    }
    prev = next;
  }
  return out.map(({ cmd, args }) => ({ cmd, args: args.map(clean) }));
}

const angleBetween = (ux, uy, vx, vy) => {
  const sign = ux * vy - uy * vx < 0 ? -1 : 1;
  const dot = (ux * vx + uy * vy) / (Math.hypot(ux, uy) * Math.hypot(vx, vy));
  return sign * Math.acos(Math.max(-1, Math.min(1, dot)));
};

// SVG implementation notes F.6.5 / F.6.6. Returns cubics `[c1x, c1y, c2x, c2y, x, y]`, each spanning at most 90 degrees.
export function arcToCubics(x1, y1, rx, ry, phi, fa, fs, x2, y2) {
  if (x1 === x2 && y1 === y2) return [];
  rx = Math.abs(rx);
  ry = Math.abs(ry);
  if (rx === 0 || ry === 0) return [[x1, y1, x2, y2, x2, y2]];

  const rad = (phi * Math.PI) / 180;
  const cosPhi = Math.cos(rad);
  const sinPhi = Math.sin(rad);
  const dx = (x1 - x2) / 2;
  const dy = (y1 - y2) / 2;
  const x1p = cosPhi * dx + sinPhi * dy;
  const y1p = -sinPhi * dx + cosPhi * dy;

  const lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry);
  if (lambda > 1) {
    const s = Math.sqrt(lambda);
    rx *= s;
    ry *= s;
  }

  const rx2 = rx * rx;
  const ry2 = ry * ry;
  const num = rx2 * ry2 - rx2 * y1p * y1p - ry2 * x1p * x1p;
  const den = rx2 * y1p * y1p + ry2 * x1p * x1p;
  const coef = (fa === fs ? -1 : 1) * Math.sqrt(Math.max(0, num / den));
  const cxp = (coef * rx * y1p) / ry;
  const cyp = (-coef * ry * x1p) / rx;
  const cx = cosPhi * cxp - sinPhi * cyp + (x1 + x2) / 2;
  const cy = sinPhi * cxp + cosPhi * cyp + (y1 + y2) / 2;

  const theta1 = angleBetween(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry);
  let delta = angleBetween((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry);
  if (!fs && delta > 0) delta -= 2 * Math.PI;
  else if (fs && delta < 0) delta += 2 * Math.PI;

  const count = Math.max(1, Math.ceil(Math.abs(delta) / (Math.PI / 2) - 1e-9));
  const step = delta / count;
  const k = (4 / 3) * Math.tan(step / 4);
  const point = (theta) => [Math.cos(theta), Math.sin(theta)];
  const map = (ux, uy) => [cosPhi * rx * ux - sinPhi * ry * uy + cx, sinPhi * rx * ux + cosPhi * ry * uy + cy];

  const cubics = [];
  for (let n = 0; n < count; n += 1) {
    const a = theta1 + n * step;
    const b = a + step;
    const [ax, ay] = point(a);
    const [bx, by] = point(b);
    const [c1x, c1y] = map(ax - k * ay, ay + k * ax);
    const [c2x, c2y] = map(bx + k * by, by - k * bx);
    const end = n === count - 1 ? [x2, y2] : map(bx, by);
    cubics.push([c1x, c1y, c2x, c2y, end[0], end[1]]);
  }
  return cubics;
}

const num = (attrs, name, fallback, tag) => {
  const raw = attrs[name];
  if (raw === undefined || raw === null || raw === '') return fallback;
  const n = Number(raw);
  if (!Number.isFinite(n)) throw new SvgPathError('unsupported-element', tag, `<${tag}> has a non-numeric ${name}`);
  return n;
};

const points = (attrs, tag) => {
  const nums = String(attrs.points ?? '')
    .split(/[\s,]+/)
    .filter(Boolean)
    .map(Number);
  if (nums.length < 4 || nums.length % 2 !== 0 || nums.some((n) => !Number.isFinite(n))) {
    throw new SvgPathError('unsupported-element', tag, `<${tag}> has bad points`);
  }
  const out = [];
  for (let i = 0; i < nums.length; i += 2) out.push([nums[i], nums[i + 1]]);
  return out;
};

// `element` is `{ tag, attrs }` (or the attributes flat next to `tag`). Returns path data with absolute commands.
export function shapeToPath(element) {
  const tag = element?.tag ?? element?.name;
  const attrs = element?.attrs ?? element ?? {};
  const f = formatNumber;
  switch (tag) {
    case 'path':
      return String(attrs.d ?? '');
    case 'circle': {
      const cx = num(attrs, 'cx', 0, tag);
      const cy = num(attrs, 'cy', 0, tag);
      const r = num(attrs, 'r', 0, tag);
      return `M${f(cx - r)} ${f(cy)}A${f(r)} ${f(r)} 0 1 0 ${f(cx + r)} ${f(cy)}A${f(r)} ${f(r)} 0 1 0 ${f(cx - r)} ${f(cy)}Z`;
    }
    case 'rect': {
      const x = num(attrs, 'x', 0, tag);
      const y = num(attrs, 'y', 0, tag);
      const w = num(attrs, 'width', 0, tag);
      const h = num(attrs, 'height', 0, tag);
      let rx = num(attrs, 'rx', num(attrs, 'ry', 0, tag), tag);
      let ry = num(attrs, 'ry', rx, tag);
      rx = Math.min(rx, w / 2);
      ry = Math.min(ry, h / 2);
      if (rx <= 0 || ry <= 0) return `M${f(x)} ${f(y)}H${f(x + w)}V${f(y + h)}H${f(x)}Z`;
      const arc = (ex, ey) => `A${f(rx)} ${f(ry)} 0 0 1 ${f(ex)} ${f(ey)}`;
      return (
        `M${f(x + rx)} ${f(y)}H${f(x + w - rx)}${arc(x + w, y + ry)}V${f(y + h - ry)}${arc(x + w - rx, y + h)}` +
        `H${f(x + rx)}${arc(x, y + h - ry)}V${f(y + ry)}${arc(x + rx, y)}Z`
      );
    }
    case 'line':
      return `M${f(num(attrs, 'x1', 0, tag))} ${f(num(attrs, 'y1', 0, tag))}L${f(num(attrs, 'x2', 0, tag))} ${f(num(attrs, 'y2', 0, tag))}`;
    case 'polyline':
    case 'polygon': {
      const pts = points(attrs, tag).map(([x, y]) => `${f(x)} ${f(y)}`);
      return `M${pts[0]}${pts.slice(1).map((p) => `L${p}`).join('')}${tag === 'polygon' ? 'Z' : ''}`;
    }
    default:
      throw new SvgPathError('unsupported-element', String(tag), `unsupported element <${tag}>`);
  }
}
