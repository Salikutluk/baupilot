// Reads the design prototypes (design/prototype/*.dc.html): template, script, props,
// evaluated renderVals() and the literal texts of a template.
import { readFileSync, readdirSync } from 'node:fs';
import { dirname, join, basename } from 'node:path';
import { fileURLToPath } from 'node:url';
import vm from 'node:vm';
import { parseScreens } from './screens.mjs';

const SCREENS_MD = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'design', 'screens.md');

export function listPrototypes(dir) {
  const screens = parseScreens(readFileSync(SCREENS_MD, 'utf8'));
  const byFile = new Map();
  for (const s of screens) {
    for (const [platform, file] of [['ios', s.iosFile], ['android', s.androidFile]]) {
      byFile.set(file, { platform, screen: s.name, dark: false });
      byFile.set(file.replace(/\.dc\.html$/, 'Dark.dc.html'), { platform, screen: s.name, dark: true });
    }
  }
  return readdirSync(dir)
    .filter((f) => f.endsWith('.dc.html'))
    .sort()
    .map((file) => {
      const info = byFile.get(file);
      if (!info) throw new Error(`prototype: ${file} is not listed in screens.md`);
      return { file, ...info };
    });
}

export function readPrototype(path) {
  const html = readFileSync(path, 'utf8');
  const body = /<x-dc>([\s\S]*?)<\/x-dc>/.exec(html);
  const script = /<script\b[^>]*data-dc-script[^>]*>([\s\S]*?)<\/script>/.exec(html);
  if (!body || !script) throw new Error(`prototype: ${basename(path)} has no <x-dc> or data-dc-script`);
  const propsAttr = /<script\b[^>]*data-props='([^']*)'/.exec(html);
  const schema = propsAttr ? JSON.parse(propsAttr[1]) : {};
  const props = {};
  for (const [key, entry] of Object.entries(schema)) {
    if (key.startsWith('$')) continue;
    if (entry && typeof entry === 'object' && 'default' in entry) props[key] = entry.default;
  }
  return {
    template: body[1].replace(/<helmet>[\s\S]*?<\/helmet>/, '').trim(),
    script: script[1],
    props,
    schema: Object.fromEntries(Object.entries(schema).filter(([k]) => !k.startsWith('$'))),
  };
}

const DC_LOGIC_STUB = `
class DCLogic {
  constructor(props) { this.props = props; this.state = {}; }
  setState() {}
}`;

export function evalRenderVals(path, { dark = false } = {}) {
  const { script, props } = readPrototype(path);
  // Not a security sandbox: the inputs are repo-owned design files. The context only
  // keeps the DCLogic stub from leaking into (or reading from) this module's scope.
  const ctx = vm.createContext({});
  vm.runInContext(DC_LOGIC_STUB, ctx);
  const Component = vm.runInContext(`${script}\n;Component`, ctx, { filename: basename(path) });
  return new Component({ ...props, dark }).renderVals();
}

const ENTITIES = { '&amp;': '&', '&lt;': '<', '&gt;': '>', '&quot;': '"', '&#39;': "'", '&nbsp;': ' ' };
const decode = (s) => s.replace(/&(?:amp|lt|gt|quot|#39|nbsp);/g, (e) => ENTITIES[e]);

const BOUNDARY = new Set(['sc-for', 'sc-if', 'dc-import', 'br']);
const CONTROL = new Set(['sc-for', 'sc-if', 'dc-import']);
const TEXT_ATTRS = new Set(['aria-label', 'placeholder', 'title', 'value']);
const TOKEN = /<!--[\s\S]*?-->|<style\b[\s\S]*?<\/style>|<(\/?)([a-zA-Z][\w-]*)((?:"[^"]*"|'[^']*'|[^>"'])*)>/g;
const ATTR = /([^\s=\/"']+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?/g;

export function extractTemplateText(template) {
  const out = [];
  const push = (raw) => {
    for (const part of raw.split(/\{\{[\s\S]*?\}\}/)) {
      const text = decode(part).replace(/\s+/g, ' ').trim();
      if (/[\p{L}\p{N}]/u.test(text)) out.push(text);
    }
  };
  let last = 0;
  for (const m of template.matchAll(TOKEN)) {
    push(template.slice(last, m.index));
    last = m.index + m[0].length;
    const [, closing, tag, attrs] = m;
    if (!tag || closing) continue;
    const name = tag.toLowerCase();
    if (BOUNDARY.has(name)) continue; // text on both sides is already separate
    for (const a of attrs.matchAll(ATTR)) {
      const key = a[1].toLowerCase();
      if (!TEXT_ATTRS.has(key) || CONTROL.has(name)) continue;
      push(a[2] ?? a[3] ?? a[4] ?? '');
    }
  }
  push(template.slice(last));
  return out;
}
