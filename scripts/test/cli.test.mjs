import { test } from 'node:test';
import assert from 'node:assert/strict';
import { cpSync, mkdtempSync, realpathSync, rmSync, symlinkSync, writeFileSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const REPO = join(dirname(fileURLToPath(import.meta.url)), '..', '..');

// A copy of the repo's tooling, reached only through a symlink: the main-module guard must still run main().
function linkedCopy() {
  const dir = realpathSync(mkdtempSync(join(tmpdir(), 'cli-')));
  const root = join(dir, 'real');
  cpSync(join(REPO, 'design'), join(root, 'design'), { recursive: true });
  cpSync(join(REPO, 'scripts'), join(root, 'scripts'), {
    recursive: true,
    filter: (src) => !src.includes('node_modules') && !src.includes(`${join('scripts', 'test')}`),
  });
  const link = join(dir, 'link');
  symlinkSync(root, link);
  return { dir, root, link };
}

function exec(file, args) {
  return spawnSync(file, args, { encoding: 'utf8' });
}

test('design-freeze through a symlinked path reports drift with exit 1', () => {
  const { dir, root, link } = linkedCopy();
  try {
    const file = join(root, 'design', 'README.md');
    writeFileSync(file, Buffer.concat([readFileSync(file), Buffer.from('x')]));
    const result = exec(join(link, 'scripts', 'design-freeze'), ['--check']);
    assert.equal(result.status, 1, result.stdout + result.stderr);
    assert.match(result.stderr, /README\.md/);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test('gen-tokens through a symlinked path runs main() and exits 2 on an invalid --tokens', () => {
  const { dir, link } = linkedCopy();
  try {
    const result = exec(join(link, 'scripts', 'gen-tokens'), ['--check', '--tokens', '/dev/null']);
    assert.equal(result.status, 2, result.stdout + result.stderr);
    assert.match(result.stderr, /^gen-tokens: /);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
