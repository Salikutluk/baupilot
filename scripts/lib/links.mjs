import { readFileSync, existsSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

const HEADING = /^### (\d+) · /;
const LINK_REF = /\(Nr\. (\d+)\)/g;

// markdownByMilestone: string[] (or an object whose values are strings) holding the M2..M6 milestone docs.
// Returns the unique [{ from, to }] pairs; each screen has one "- Links:" line per platform.
export function parseLinks(markdownByMilestone) {
  const docs = Array.isArray(markdownByMilestone) ? markdownByMilestone : Object.values(markdownByMilestone);
  const pairs = [];
  for (const md of docs) {
    let screen = null;
    let lines = [];
    const flush = () => {
      if (screen === null) return;
      const targets = lines.map((l) => [...l.matchAll(LINK_REF)].map((m) => Number(m[1])));
      if (targets.some((t) => t.join(',') !== targets[0].join(','))) {
        throw new Error(`links: screen ${screen} has differing iOS/Android link lines`);
      }
      for (const to of targets[0] ?? []) pairs.push({ from: screen, to });
    };
    for (const line of md.split('\n')) {
      const h = HEADING.exec(line);
      if (h) {
        flush();
        screen = Number(h[1]);
        lines = [];
      } else if (screen !== null && line.startsWith('- Links:')) {
        lines.push(line);
      }
    }
    flush();
  }
  return pairs;
}

// Splits a tag on `.`, keeping a `${…}` interpolation (which may contain dots) in one segment.
function tagSegments(tag) {
  const segments = [];
  let depth = 0;
  let current = '';
  for (const ch of tag) {
    if (ch === '{') depth++;
    else if (ch === '}') depth--;
    if (ch === '.' && depth === 0) {
      segments.push(current);
      current = '';
    } else {
      current += ch;
    }
  }
  segments.push(current);
  return segments;
}

// A source segment `$name` / `${…}` and a manifest segment `<…>` each match exactly one segment.
function tagMatches(element, tag) {
  const want = tagSegments(element);
  const have = tagSegments(tag);
  if (want.length !== have.length) return false;
  return want.every((w, i) => {
    const h = have[i];
    return w === h || /^<.*>$/.test(w) || /^\$(\w+|\{.*\})$/.test(h);
  });
}

const key = (p) => `${p.from}→${p.to}`;

// sources: { ios: { <Class>: string[] of func names }, android: { <Class>: string[] of fun names }, androidMainTags?: string[] }
// `androidMainTags` (tag literals in android/app/src/main) switches the element drift check on.
export function validateCoverage(pairs, manifest, sources) {
  const problems = [];
  const docKeys = new Set(pairs.map(key));
  const manifestKeys = new Set(manifest.links.map(key));
  for (const p of pairs) if (!manifestKeys.has(key(p))) problems.push(`missing pair ${key(p)}`);
  for (const l of manifest.links) if (!docKeys.has(key(l))) problems.push(`unknown pair ${key(l)}`);

  const hasIos = (ref) => {
    if (typeof ref !== 'string') return false;
    const [cls, name] = ref.split('/');
    return Boolean(sources.ios?.[cls]?.includes(name));
  };
  const hasAndroid = (ref) => {
    if (typeof ref !== 'string') return false;
    const [cls, name] = ref.split('#');
    return Boolean(sources.android?.[cls]?.includes(name));
  };
  // `platforms` switches a whole platform's checks on or off; a manifest without it checks both.
  const enabled = manifest.platforms ?? { ios: true, android: true };
  for (const l of manifest.links) {
    if (enabled.ios && !hasIos(l.ios)) problems.push(`ios test ${l.ios} not found`);
    if (enabled.android && !hasAndroid(l.android)) problems.push(`android test ${l.android} not found`);
  }
  for (const p of manifest.placeholders ?? []) {
    // `platforms` lists where the element exists; it defaults to both.
    const platforms = p.platforms ?? ['ios', 'android'];
    if (enabled.ios && platforms.includes('ios') && !hasIos(p.ios)) problems.push(`placeholder ${p.element} has no ios test`);
    if (enabled.android && platforms.includes('android') && !hasAndroid(p.android)) problems.push(`placeholder ${p.element} has no android test`);
    if (enabled.android && platforms.includes('android') && sources.androidMainTags && !sources.androidMainTags.some((t) => tagMatches(p.element, t))) {
      problems.push(`android element ${p.element} not found in sources`);
    }
  }
  return problems;
}

const TAG_LITERAL = /"([A-Za-z][A-Za-z0-9_-]*(?:\.(?:\$\{[^}"]*\}|\$?[A-Za-z0-9_-]+))+)"/g;

function kotlinFiles(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir, { withFileTypes: true }).flatMap((e) =>
    e.isDirectory() ? kotlinFiles(join(dir, e.name)) : e.name.endsWith('.kt') ? [join(dir, e.name)] : [],
  );
}

function funcNames(text, re) {
  return [...text.matchAll(re)].map((m) => m[1]);
}

// Reads the test method names per class from the repo's UI-test sources (each class = its own file).
export function readSources(root, manifest) {
  const sources = { ios: {}, android: {} };
  const classes = (refs, sep) => [...new Set(refs.filter((r) => typeof r === 'string').map((r) => r.split(sep)[0]))];
  const entries = [...manifest.links, ...(manifest.placeholders ?? [])];
  for (const cls of classes(entries.map((e) => e.ios), '/')) {
    const file = join(root, 'ios', 'BaupilotUITests', `${cls}.swift`);
    if (existsSync(file)) sources.ios[cls] = funcNames(readFileSync(file, 'utf8'), /\bfunc\s+([A-Za-z0-9_]+)\s*\(/g);
  }
  for (const cls of classes(entries.map((e) => e.android), '#')) {
    const file = join(root, 'android', 'app', 'src', 'test', 'java', 'de', 'baupilot', 'app', `${cls}.kt`);
    if (existsSync(file)) sources.android[cls] = funcNames(readFileSync(file, 'utf8'), /\bfun\s+`?([A-Za-z0-9_]+)`?\s*\(/g);
  }
  // src/test is excluded on purpose: PlaceholderUiTest holds every literal and would make the drift check vacuous.
  const tags = new Set();
  for (const file of kotlinFiles(join(root, 'android', 'app', 'src', 'main'))) {
    for (const m of readFileSync(file, 'utf8').matchAll(TAG_LITERAL)) tags.add(m[1]);
  }
  sources.androidMainTags = [...tags];
  return sources;
}
