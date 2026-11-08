import { test } from 'node:test';
import assert from 'node:assert/strict';
import { cpSync, existsSync, mkdirSync, mkdtempSync, realpathSync, rmSync, symlinkSync, writeFileSync, readFileSync } from 'node:fs';
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

test('lint-hex through a symlinked path runs main() and exits 1 on a planted hex file', () => {
  const { dir, root, link } = linkedCopy();
  try {
    mkdirSync(join(root, 'android', 'app'), { recursive: true });
    writeFileSync(join(root, 'android', 'app', 'Probe.kt'), 'val c = "#C2410C"\n');
    const result = exec(join(link, 'scripts', 'lint-hex'), []);
    assert.equal(result.status, 1, result.stdout + result.stderr);
    assert.match(result.stdout, /android\/app\/Probe\.kt:1:10 hex-literal #C2410C/);
    assert.match(result.stdout, /lint-hex: 1 Fund\(e\)/);
    const unknown = exec(join(link, 'scripts', 'lint-hex'), ['--nope']);
    assert.equal(unknown.status, 2, unknown.stdout + unknown.stderr);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test('gen-icons through a symlinked path runs main(): generates, checks clean, then reports a stale drawable with exit 1', () => {
  const { dir, root, link } = linkedCopy();
  try {
    const generated = exec(join(link, 'scripts', 'gen-icons'), []);
    assert.equal(generated.status, 0, generated.stdout + generated.stderr);
    assert.match(generated.stdout, /^gen-icons: wrote \d+ files/);
    const drawable = join(root, 'android', 'app', 'src', 'main', 'res', 'drawable');
    assert.ok(existsSync(join(drawable, 'ic_check_w30.xml')));
    assert.equal(exec(join(link, 'scripts', 'gen-icons'), ['--check']).status, 0);
    writeFileSync(join(drawable, 'ic_old.xml'), '<vector/>\n');
    const stale = exec(join(link, 'scripts', 'gen-icons'), ['--check']);
    assert.equal(stale.status, 1, stale.stdout + stale.stderr);
    assert.match(stale.stderr, /ic_old\.xml/);
    const invalid = exec(join(link, 'scripts', 'gen-icons'), ['--check', '--manifest', '/dev/null']);
    assert.equal(invalid.status, 2, invalid.stdout + invalid.stderr);
    assert.match(invalid.stderr, /^gen-icons: /);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
