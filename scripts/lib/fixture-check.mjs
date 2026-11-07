// Helpers that prove fixtures/baupilot.json is complete and invents nothing:
// collect the display values of every light prototype's renderVals(), list the
// fixture's leaves and build the text corpus of the prototypes.
//
// SanBad (iOS and Android) builds its checks with chk(label, hint, ok, i). The third
// argument is `ok` in the source: true, true, false, false, false. For the fixture it
// means "answered in order" - `false` is "not answered yet", never "Mangel".
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { listPrototypes, readPrototype, evalRenderVals, extractTemplateText } from './prototype.mjs';

// Keys whose values are presentation (colours, icons, geometry, links) or CSS flags and
// never display content. Geometry keys found while calibrating against the prototypes:
// `left`, `width`, `askLeft` (Zeitplan bars), `todayFrac`/`todayPct` (today marker),
// `y2` (Gefälle line end), `h`/`floorH`/`gapLine` (Ebenheit gauge), `bar` (Belegreife
// chart bar height), `line` (separator colour); `mark` is not skipped (FlBad "hohl").
export const PRESENTATION_KEYS = new Set([
  'icon', 'href', 'color', 'bg', 'fg', 'border', 'divider', 'badgeBg', 'badgeFg', 'priceColor',
  'iconBg', 'iconFg', 'iconColor', 'rightColor', 'dot', 'weight', 'metaWeight', 'ring', 'fill',
  'tick', 'track', 'trackBorder', 'align', 'thumb', 'thumbColor', 'left', 'width', 'askLeft',
  'pill', 'avBg', 'avFg', 'rowBg', 'cardBg', 'line', 'metaColor', 'tagColor', 'c', 'swatch',
  'okAria', 'mAria', 'y2', 'h', 'floorH', 'gapLine', 'sel', 'selected', 'checked', 'bar',
  'okIcon', 'mIcon', 'okBg', 'okFg', 'okBorder', 'mBg', 'mBorder', 'todayFrac', 'todayPct',
]);

// Computed from the interactive inputs; the fixture stores the inputs and the label literals.
export const COMPUTED_KEYS = new Set([
  'diff', 'res', 'limitLabel', 'gapLabel', 'mangelLabel', 'lenLabel', 'dropLabel', 'pct',
  'count', 'nextId', 'points',
]);

const INTERACTIVE = /^(Ios|And)(RohbauMasse|EsEbenheit|SanGefaelle)\.dc\.html$/;

const HEX = /^#[0-9a-fA-F]{3,8}$/;
const CSS_FN = /^(rgb|rgba|var)\(/;
const CSS_LENGTH = /(^|\s)-?[\d.]+px(\s|$)/;
const SVG_PATH = /^[MmLlHhVvCcZz][\d\s.,-]/;
const CSS_WORD = /^(transparent|true|false)$/;

function isChrome(text) {
  return (
    text === '' ||
    HEX.test(text) ||
    CSS_FN.test(text) ||
    CSS_LENGTH.test(text) ||
    SVG_PATH.test(text) ||
    CSS_WORD.test(text) ||
    text.endsWith('.dc.html')
  );
}

function collect(value, skip, key, onValue) {
  if (typeof value === 'function' || value === null || value === undefined) return;
  if (Array.isArray(value)) {
    value.forEach((item) => collect(item, skip, key, onValue));
    return;
  }
  if (typeof value === 'object') {
    for (const [k, item] of Object.entries(value)) {
      if (!skip.has(k)) collect(item, skip, k, onValue);
    }
    return;
  }
  if (typeof value === 'boolean') return;
  const text = String(value);
  if (!isChrome(text)) onValue(text, key);
}

export function collectRenderValsValues(protoDir) {
  const out = new Map();
  for (const proto of listPrototypes(protoDir).filter((p) => !p.dark)) {
    const { t, ...vals } = evalRenderVals(join(protoDir, proto.file));
    const skip = INTERACTIVE.test(proto.file)
      ? new Set([...PRESENTATION_KEYS, ...COMPUTED_KEYS])
      : PRESENTATION_KEYS;
    for (const [key, value] of Object.entries(vals)) {
      if (skip.has(key)) continue;
      collect(value, skip, key, (text, leafKey) => {
        if (!out.has(text)) out.set(text, []);
        out.get(text).push(`${proto.file}:${leafKey}`);
      });
    }
  }
  return out;
}

export function fixtureValues(fixture) {
  const out = new Set();
  const walk = (value) => {
    if (typeof value === 'string') out.add(value);
    else if (typeof value === 'number') out.add(String(value));
    else if (Array.isArray(value)) value.forEach(walk);
    else if (value && typeof value === 'object') Object.values(value).forEach(walk);
  };
  walk(fixture);
  return out;
}

const ENTITIES = { '&amp;': '&', '&lt;': '<', '&gt;': '>', '&quot;': '"', '&#39;': "'", '&nbsp;': ' ' };
const decode = (s) => s.replace(/&(?:amp|lt|gt|quot|#39|nbsp);/g, (e) => ENTITIES[e]);

// Decoded text of every light file. A template's literal texts are appended with
// collapsed whitespace, so a sentence the source wraps over several lines still matches.
export function prototypeCorpus(protoDir) {
  return listPrototypes(protoDir)
    .filter((p) => !p.dark)
    .map((p) => {
      const path = join(protoDir, p.file);
      const segments = extractTemplateText(readPrototype(path).template);
      return `${decode(readFileSync(path, 'utf8'))}\n${segments.join('\n')}`;
    })
    .join('\0');
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const dir = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'design', 'prototype');
  const values = collectRenderValsValues(dir);
  for (const [text, where] of values) process.stdout.write(`${JSON.stringify(text)}\t${where.join(' ')}\n`);
}
