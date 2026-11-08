import { test } from 'node:test';
import assert from 'node:assert/strict';
import { iconPdf } from '../lib/pdf.mjs';
import { toAbsolute, tokenize } from '../lib/svg-path.mjs';

const input = (d, extra = {}) => ({
  commands: toAbsolute(tokenize(d)),
  stroke: 2,
  fill: false,
  lineCap: 'round',
  lineJoin: 'round',
  ...extra,
});

test('T-IC-4 pdf xref offsets point at "n 0 obj"', () => {
  const pdf = iconPdf(input('M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM21 21l-4.3-4.3'));
  const text = pdf.toString('latin1');
  assert.ok(text.startsWith('%PDF-1.4\n'));
  assert.ok(text.endsWith('%%EOF\n'));
  assert.ok(text.includes('/MediaBox [0 0 24 24]'));

  const xrefAt = Number(/startxref\n(\d+)\n/.exec(text)[1]);
  assert.ok(text.slice(xrefAt).startsWith('xref\n'));
  const header = /xref\n0 (\d+)\n/.exec(text.slice(xrefAt));
  const size = Number(header[1]);
  const entries = text
    .slice(xrefAt)
    .split('\n')
    .filter((l) => /^\d{10} \d{5} [nf] $/.test(l));
  assert.equal(entries.length, size);
  assert.equal(entries[0], '0000000000 65535 f ');
  entries.slice(1).forEach((entry, i) => {
    const offset = Number(entry.slice(0, 10));
    assert.ok(text.slice(offset).startsWith(`${i + 1} 0 obj\n`), `entry ${i + 1}`);
    assert.equal(entry.slice(11), '00000 n ');
  });
  assert.match(text, /\/Size 5 /);
});

test('pdf content stream flips y, sets stroke state, converts arcs to cubics and strokes', () => {
  const text = iconPdf(input('M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM21 21l-4.3-4.3')).toString('latin1');
  const stream = /stream\n([\s\S]*)\nendstream/.exec(text)[1];
  const lines = stream.split('\n');
  assert.deepEqual(lines.slice(0, 6), ['1 0 0 -1 0 24 cm', '2 w', '1 J', '1 j', '0 G', '0 g']);
  assert.equal(lines[lines.length - 1], 'S');
  assert.ok(lines.includes('h'));
  assert.ok(lines.includes('11 18 m'));
  assert.ok(lines.includes('21 21 m'));
  assert.ok(lines.includes('16.7 16.7 l'));
  assert.ok(lines.some((l) => l.endsWith(' c')));
  assert.ok(!/[aA]\b/.test(stream.replace(/\d+/g, '')), 'no arc operators');
  assert.equal(Number(/\/Length (\d+)/.exec(text)[1]), Buffer.byteLength(`${stream}\n`));
});

test('fill icons end with f; quadratic curves become cubics', () => {
  const text = iconPdf({ commands: toAbsolute(tokenize('M0 0Q3 0 3 3z')), fill: true }).toString('latin1');
  const lines = /stream\n([\s\S]*)\nendstream/.exec(text)[1].split('\n');
  assert.equal(lines[lines.length - 1], 'f');
  assert.ok(lines.includes('2 0 3 1 3 3 c'));
});

test('pdf output is deterministic and carries no date, id or random bytes', () => {
  const a = iconPdf(input('M5 12l5 5 9-10'));
  const b = iconPdf(input('M5 12l5 5 9-10'));
  assert.ok(a.equals(b));
  const text = a.toString('latin1');
  for (const needle of ['/CreationDate', '/ModDate', '/ID', '/Producer', '/Creator', '/Info']) {
    assert.ok(!text.includes(needle), needle);
  }
  assert.throws(() => iconPdf(input('M0 0', { lineCap: 'pointy' })), /lineCap/);
});
