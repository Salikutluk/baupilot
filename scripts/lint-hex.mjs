import { readdirSync, readFileSync, realpathSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

const DEFAULT_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const SCAN_ROOTS = [
  ['ios', ['.swift']],
  ['android', ['.kt', '.kts', '.xml']],
];
const SKIPPED_SEGMENTS = new Set(['build', '.gradle', '.idea', 'xcuserdata', 'DerivedData', '.swiftpm']);
const SKIPPED_SUFFIXES = ['.xcodeproj', '.xcworkspace'];
const GENERATED = new Set([
  'ios/Baupilot/DesignSystem/Tokens.swift',
  'android/app/src/main/java/de/baupilot/designsystem/Tokens.kt',
]);
const TEST_DIRS = ['ios/BaupilotTests/', 'android/app/src/test/'];

const MARKER = /lint-hex: allow(.*)$/;
const HEX_DIGITS = '[0-9a-fA-F]';
const HEX_LITERAL = new RegExp(`(?<![&\\w])#(?:${HEX_DIGITS}{8}|${HEX_DIGITS}{6}|${HEX_DIGITS}{4}|${HEX_DIGITS}{3})(?!\\w)`, 'g');
const INT_COLOR = new RegExp(`(?<!\\w)0[xX](?:${HEX_DIGITS}{8}|${HEX_DIGITS}{6})[uU]?L?(?!\\w)`, 'g');
const COLOR_CTOR =
  /(?<!\w)(?:(?:UI)?Color\((?:red:|white:|hue:|\.sRGB|0x|red\s*=)|Color\.(?:rgb|argb)\()|parseColor\(/g;
const NAMED_SWIFT =
  /(?<!\w)(?:UI)?Color\.(?:white|black|red|green|blue|gray|orange|yellow|pink|purple|cyan|mint|teal|indigo|brown)\b/g;
const NAMED_KOTLIN = /(?<!\w)Color\.(?:White|Black|Red|Green|Blue|Gray|DarkGray|LightGray|Cyan|Magenta|Yellow)\b/g;

function extensionOf(path) {
  const dot = path.lastIndexOf('.');
  return dot < 0 ? '' : path.slice(dot);
}

function isExcluded(path) {
  if (GENERATED.has(path)) return true;
  return path.split('/').some((segment) => SKIPPED_SEGMENTS.has(segment) || SKIPPED_SUFFIXES.some((s) => segment.endsWith(s)));
}

function rulesFor(ext) {
  const rules = [['hex-literal', HEX_LITERAL]];
  if (ext === '.swift' || ext === '.kt' || ext === '.kts') rules.push(['int-color', INT_COLOR]);
  if (ext === '.swift' || ext === '.kt') rules.push(['color-ctor', COLOR_CTOR]);
  if (ext === '.swift') rules.push(['named-color', NAMED_SWIFT]);
  if (ext === '.kt') rules.push(['named-color', NAMED_KOTLIN]);
  return rules;
}

/** Colour literals in one source text. `path` is relative to the repo root, with forward slashes. */
export function scanSource(text, { path }) {
  if (isExcluded(path)) return [];
  const rules = rulesFor(extensionOf(path));
  const inTestDir = TEST_DIRS.some((dir) => path.startsWith(dir));
  const findings = [];
  text.split(/\r?\n/).forEach((lineText, index) => {
    const line = index + 1;
    const marker = MARKER.exec(lineText);
    if (marker) {
      const reason = marker[1].replace(/(?:-->|\*\/)\s*$/, '').trim();
      if (!inTestDir) {
        findings.push({ path, line, col: marker.index + 1, rule: 'allow-outside-tests', match: 'lint-hex: allow' });
      } else if (reason === '') {
        findings.push({ path, line, col: marker.index + 1, rule: 'allow-without-reason', match: 'lint-hex: allow' });
      } else {
        return;
      }
    }
    for (const [rule, pattern] of rules) {
      for (const hit of lineText.matchAll(pattern)) {
        findings.push({ path, line, col: hit.index + 1, rule, match: hit[0] });
      }
    }
  });
  return findings;
}

function* walk(dir, rel, extensions) {
  const entries = readdirSync(dir, { withFileTypes: true }).sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : 0));
  for (const entry of entries) {
    const entryRel = `${rel}/${entry.name}`;
    if (isExcluded(entryRel)) continue;
    if (entry.isDirectory()) yield* walk(join(dir, entry.name), entryRel, extensions);
    else if (entry.isFile() && extensions.includes(extensionOf(entry.name))) yield entryRel;
  }
}

export function run({ root = DEFAULT_ROOT } = {}) {
  const findings = [];
  for (const [top, extensions] of SCAN_ROOTS) {
    let files;
    try {
      files = [...walk(join(root, top), top, extensions)];
    } catch (error) {
      if (error.code === 'ENOENT') continue;
      throw error;
    }
    for (const rel of files) findings.push(...scanSource(readFileSync(join(root, rel), 'utf8'), { path: rel }));
  }
  findings.sort((a, b) => (a.path < b.path ? -1 : a.path > b.path ? 1 : a.line - b.line || a.col - b.col));
  return { findings, exitCode: findings.length > 0 ? 1 : 0 };
}

function main() {
  const { values } = parseArgs({ options: { root: { type: 'string' } } });
  const { findings, exitCode } = run({ root: values.root ? resolve(values.root) : DEFAULT_ROOT });
  for (const f of findings) console.log(`${f.path}:${f.line}:${f.col} ${f.rule} ${f.match}`);
  console.log(findings.length > 0 ? `lint-hex: ${findings.length} Fund(e)` : 'lint-hex: sauber');
  process.exitCode = exitCode;
}

if (process.argv[1] && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url))) {
  try {
    main();
  } catch (error) {
    console.error(`lint-hex: ${error.message}`);
    process.exitCode = 2;
  }
}
