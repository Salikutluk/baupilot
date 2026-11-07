import { test } from 'node:test';
import assert from 'node:assert/strict';
import { cpSync, mkdtempSync, rmSync, writeFileSync, readFileSync, unlinkSync, mkdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { run } from '../design-freeze.mjs';

const realRoot = join(dirname(fileURLToPath(import.meta.url)), '..', '..');

function tempRoot() {
  const root = mkdtempSync(join(tmpdir(), 'design-freeze-'));
  cpSync(join(realRoot, 'design'), join(root, 'design'), { recursive: true });
  mkdirSync(join(root, 'scripts'));
  cpSync(join(realRoot, 'scripts', 'design-v1.sha256'), join(root, 'scripts', 'design-v1.sha256'));
  return root;
}

test('--check passes on the real tree', () => {
  const result = run({ root: realRoot, mode: 'check' });
  assert.equal(result.exit, 0, result.output);
});

test('--check names a changed file', () => {
  const root = tempRoot();
  try {
    const file = join(root, 'design', 'README.md');
    writeFileSync(file, Buffer.concat([readFileSync(file), Buffer.from('x')]));
    const result = run({ root, mode: 'check' });
    assert.equal(result.exit, 1);
    assert.deepEqual(result.changed, ['README.md']);
    assert.match(result.output, /README\.md/);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test('--check names an added file', () => {
  const root = tempRoot();
  try {
    writeFileSync(join(root, 'design', 'new-file.txt'), 'new');
    const result = run({ root, mode: 'check' });
    assert.equal(result.exit, 1);
    assert.deepEqual(result.added, ['new-file.txt']);
    assert.match(result.output, /new-file\.txt/);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test('--check names a removed file', () => {
  const root = tempRoot();
  try {
    unlinkSync(join(root, 'design', 'README.md'));
    const result = run({ root, mode: 'check' });
    assert.equal(result.exit, 1);
    assert.deepEqual(result.removed, ['README.md']);
    assert.match(result.output, /README\.md/);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test('.DS_Store files are ignored', () => {
  const root = tempRoot();
  try {
    writeFileSync(join(root, 'design', '.DS_Store'), 'junk');
    assert.equal(run({ root, mode: 'check' }).exit, 0);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test('--write produces sorted sha256sum lines that --check accepts', () => {
  const root = tempRoot();
  try {
    writeFileSync(join(root, 'design', 'A-first.txt'), 'a');
    assert.equal(run({ root, mode: 'write' }).exit, 0);
    const lines = readFileSync(join(root, 'scripts', 'design-v1.sha256'), 'utf8').trimEnd().split('\n');
    for (const line of lines) assert.match(line, /^[0-9a-f]{64} {2}\S/);
    const paths = lines.map((l) => l.slice(66));
    assert.deepEqual(paths, [...paths].sort((a, b) => Buffer.compare(Buffer.from(a), Buffer.from(b))));
    assert.ok(paths.includes('A-first.txt'));
    assert.equal(run({ root, mode: 'check' }).exit, 0);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});
