import { createHash } from 'node:crypto';
import { readdirSync, readFileSync, realpathSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptsDir = dirname(fileURLToPath(import.meta.url));
const defaultRoot = dirname(scriptsDir);
const MANIFEST = join('scripts', 'design-v1.sha256');

function listFiles(dir, prefix = '') {
  const out = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === '.DS_Store') continue;
    const rel = prefix + entry.name;
    if (entry.isDirectory()) out.push(...listFiles(join(dir, entry.name), rel + '/'));
    else if (entry.isFile()) out.push(rel);
  }
  return out;
}

function byteOrder(a, b) {
  return Buffer.compare(Buffer.from(a), Buffer.from(b));
}

function computeManifest(root) {
  const designDir = join(root, 'design');
  return new Map(
    listFiles(designDir)
      .sort(byteOrder)
      .map((rel) => [rel, createHash('sha256').update(readFileSync(join(designDir, rel))).digest('hex')]),
  );
}

function parseManifest(text) {
  const map = new Map();
  for (const line of text.split('\n')) {
    if (line === '') continue;
    map.set(line.slice(66), line.slice(0, 64));
  }
  return map;
}

export function run({ root = defaultRoot, mode } = {}) {
  const current = computeManifest(root);
  const manifestPath = join(root, MANIFEST);

  if (mode === 'write') {
    const text = [...current].map(([rel, sha]) => `${sha}  ${rel}\n`).join('');
    writeFileSync(manifestPath, text);
    return { exit: 0, added: [], removed: [], changed: [], output: `wrote ${current.size} entries to ${MANIFEST}\n` };
  }
  if (mode !== 'check') {
    return { exit: 2, added: [], removed: [], changed: [], output: 'usage: design-freeze (--write | --check)\n' };
  }

  const frozen = parseManifest(readFileSync(manifestPath, 'utf8'));
  const added = [...current.keys()].filter((p) => !frozen.has(p));
  const removed = [...frozen.keys()].filter((p) => !current.has(p));
  const changed = [...current].filter(([p, sha]) => frozen.has(p) && frozen.get(p) !== sha).map(([p]) => p);

  if (!added.length && !removed.length && !changed.length) {
    return { exit: 0, added, removed, changed, output: `design/ matches ${MANIFEST} (${current.size} files)\n` };
  }
  const lines = [
    ...added.map((p) => `added:   ${p}`),
    ...removed.map((p) => `removed: ${p}`),
    ...changed.map((p) => `changed: ${p}`),
  ];
  return { exit: 1, added, removed, changed, output: `design/ differs from ${MANIFEST}:\n${lines.join('\n')}\n` };
}

if (process.argv[1] && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url))) {
  const flag = process.argv[2];
  const mode = flag === '--write' ? 'write' : flag === '--check' ? 'check' : undefined;
  const result = run({ mode });
  (result.exit === 0 ? process.stdout : process.stderr).write(result.output);
  process.exitCode = result.exit;
}
