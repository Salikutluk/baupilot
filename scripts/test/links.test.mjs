import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseLinks, validateCoverage, readSources } from '../lib/links.mjs';
import { parseScreens } from '../lib/screens.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const docs = [2, 3, 4, 5, 6].map((m) => readFileSync(join(root, 'docs', 'milestones', `M${m}.md`), 'utf8'));

test('parseLinks finds 100 pairs over 34 screens in M2-M6', () => {
  const pairs = parseLinks(docs);
  assert.equal(pairs.length, 100);
  assert.equal(new Set(pairs.map((p) => p.from)).size, 34);
  assert.deepEqual(pairs.filter((p) => p.from === 1), [{ from: 1, to: 2 }]);
});

test('parseLinks throws on differing platform lines', () => {
  const md = [
    '### 7 · Dokument',
    '**iOS**',
    '- Links: Dokumente (Nr. 6), Vergleich (Nr. 8)',
    '**Android**',
    '- Links: Dokumente (Nr. 6)',
  ].join('\n');
  assert.throws(() => parseLinks([md]), /links: screen 7 has differing iOS\/Android link lines/);
});

test('validateCoverage reports a missing pair, an unknown pair and a missing test method', () => {
  const pairs = [{ from: 3, to: 2 }, { from: 3, to: 4 }];
  const manifest = {
    links: [
      { from: 3, to: 4, ios: 'LinkCoverageUITests/testX', android: 'LinkCoverageUiTest#x' },
      { from: 3, to: 9, ios: 'LinkCoverageUITests/testY', android: 'LinkCoverageUiTest#y' },
    ],
    placeholders: [{ element: 'login.forgot', ios: 'PlaceholderUITests/testNope', android: 'PlaceholderUiTest#login' }],
  };
  const sources = {
    ios: { LinkCoverageUITests: ['testY'], PlaceholderUITests: [] },
    android: { LinkCoverageUiTest: ['y'], PlaceholderUiTest: ['login'] },
  };
  assert.deepEqual(validateCoverage(pairs, manifest, sources), [
    'missing pair 3→2',
    'unknown pair 3→9',
    'ios test LinkCoverageUITests/testX not found',
    'android test LinkCoverageUiTest#x not found',
    'placeholder login.forgot has no ios test',
  ]);
});

test('the real manifest covers every pair and placeholder', () => {
  const manifest = JSON.parse(readFileSync(join(root, 'scripts', 'link-coverage.json'), 'utf8'));
  const problems = validateCoverage(parseLinks(docs), manifest, readSources(root, manifest));
  assert.deepEqual(problems, []);
  assert.equal(manifest.links.length, 100);
});

const platformFixture = (iosEnabled) => ({
  pairs: [{ from: 3, to: 4 }],
  manifest: {
    platforms: { ios: iosEnabled, android: true },
    links: [{ from: 3, to: 4, ios: 'LinkCoverageUITests/testX', android: 'LinkCoverageUiTest#x' }],
    placeholders: [{ element: 'login.forgot', ios: 'PlaceholderUITests/testNope', android: 'PlaceholderUiTest#login' }],
  },
  sources: { ios: {}, android: { LinkCoverageUiTest: ['x'], PlaceholderUiTest: ['login'] } },
});

test('validateCoverage ignores a disabled platform', () => {
  const off = platformFixture(false);
  assert.deepEqual(validateCoverage(off.pairs, off.manifest, off.sources), []);
  const on = platformFixture(true);
  assert.deepEqual(validateCoverage(on.pairs, on.manifest, on.sources), [
    'ios test LinkCoverageUITests/testX not found',
    'placeholder login.forgot has no ios test',
  ]);
});

test('validateCoverage without a platforms flag checks both platforms', () => {
  const { pairs, manifest, sources } = platformFixture(false);
  delete manifest.platforms;
  sources.android = {};
  assert.deepEqual(validateCoverage(pairs, manifest, sources), [
    'ios test LinkCoverageUITests/testX not found',
    'android test LinkCoverageUiTest#x not found',
    'placeholder login.forgot has no ios test',
    'placeholder login.forgot has no android test',
  ]);
});

const tagFixture = (element, androidMainTags) => ({
  pairs: [],
  manifest: {
    links: [],
    placeholders: [{ element, ios: 'PlaceholderUITests/testNope', android: 'PlaceholderUiTest#login', platforms: ['android'] }],
  },
  sources: { ios: {}, android: { PlaceholderUiTest: ['login'] }, androidMainTags },
});

test('validateCoverage reports a placeholder element that no android main source contains', () => {
  const f = tagFixture('ph.Login.forgot', ['ph.Login.create', 'login.forgot']);
  assert.deepEqual(validateCoverage(f.pairs, f.manifest, f.sources), ['android element ph.Login.forgot not found in sources']);
  f.sources.androidMainTags.push('ph.Login.forgot');
  assert.deepEqual(validateCoverage(f.pairs, f.manifest, f.sources), []);
  delete f.sources.androidMainTags;
  assert.deepEqual(validateCoverage(f.pairs, f.manifest, f.sources), []);
});

test('validateCoverage matches interpolated tags segment-wise', () => {
  const ok = (element, tags) => {
    const f = tagFixture(element, tags);
    return validateCoverage(f.pairs, f.manifest, f.sources).length === 0;
  };
  assert.ok(!ok('ph.Profil.row.0.1', ['ph.Profil.row.$position']));
  assert.ok(ok('ph.Profil.row.0', ['ph.Profil.row.$position']));
  assert.ok(ok('ph.AbnElemente.more.1', ['ph.AbnElemente.more.$floorIndex']));
  assert.ok(ok('ph.AbnMaengel.remind.<id>', ['ph.AbnMaengel.remind.${card.id}']));
  assert.ok(ok('ph.AbnMaengel.remind.<id>', ['ph.AbnMaengel.remind.$id']));
  assert.ok(ok('overview.row.0.1', ['overview.row.$groupIndex.$rowIndex']));
  assert.ok(ok('overview.row.<g>.<r>', ['overview.row.$groupIndex.$rowIndex']));
  assert.ok(!ok('overview.row.0', ['overview.row.$groupIndex.$rowIndex']));
  assert.ok(!ok('ph.Erd.pdf', ['ph.Erd.pdfx', 'ph.Zimmerer.pdf']));
});

const iosEnabled = JSON.parse(readFileSync(join(root, 'scripts', 'link-coverage.json'), 'utf8')).platforms?.ios !== false;

test('audit screen list equals screens.md order', { skip: !iosEnabled && 'iOS disabled in manifest' }, () => {
  const screens = parseScreens(readFileSync(join(root, 'design', 'screens.md'), 'utf8')).map((s) => s.name);
  const source = readFileSync(join(root, 'ios', 'BaupilotUITests', 'AccessibilityAuditUITests.swift'), 'utf8');
  const block = /static let screens: \[String\] = \[([^\]]*)\]/.exec(source);
  assert.ok(block, 'screens literal not found in AccessibilityAuditUITests.swift');
  const listed = [...block[1].matchAll(/"([A-Za-z0-9]+)"/g)].map((m) => m[1]);
  assert.deepEqual(listed, screens);
});

test('every HitShapeAllowlist test exists', { skip: !iosEnabled && 'iOS disabled in manifest' }, () => {
  const source = readFileSync(join(root, 'ios', 'BaupilotUITests', 'TouchTargetUITests.swift'), 'utf8');
  const block = /static let entries: \[String: String\] = \[([^\]]*)\]/.exec(source);
  assert.ok(block, 'HitShapeAllowlist.entries not found in TouchTargetUITests.swift');
  const named = [...block[1].matchAll(/"[^"]*":\s*"([A-Za-z0-9]+)\.(test[A-Za-z0-9]+)"/g)].map((m) => ({ cls: m[1], method: m[2] }));
  const dir = join(root, 'ios', 'BaupilotTests');
  const files = readdirSync(dir, { recursive: true }).filter((f) => String(f).endsWith('.swift'));
  for (const { cls, method } of named) {
    const file = files.find((f) => new RegExp(`class ${cls}\\b`).test(readFileSync(join(dir, String(f)), 'utf8')));
    assert.ok(file, `class ${cls} not found in ios/BaupilotTests`);
    assert.match(readFileSync(join(dir, String(file)), 'utf8'), new RegExp(`func ${method}\\(`), `${cls}.${method} not found`);
  }
});

test('every IntendedEllipsis entry cites an existing prototype line', { skip: !iosEnabled && 'iOS disabled in manifest' }, () => {
  const source = readFileSync(join(root, 'ios', 'BaupilotUITests', 'LargeTextUITests.swift'), 'utf8');
  const block = /static let entries: \[\(screen: String, identifier: String, prototypeRef: String\)\] = \[([^\]]*)\]/.exec(source);
  assert.ok(block, 'IntendedEllipsis.entries not found in LargeTextUITests.swift');
  const entries = [...block[1].matchAll(/\(screen: "([A-Za-z0-9]+)", identifier: "([^"]+)", prototypeRef: "([^"]+)"\)/g)];
  const written = block[1].split('\n').filter((l) => l.trim().startsWith('(screen:')).length;
  assert.equal(entries.length, written, 'an IntendedEllipsis entry is not in the literal syntax');
  for (const [, , identifier, ref] of entries) {
    const cite = /^(Ios[A-Za-z0-9]+\.dc\.html):(\d+)$/.exec(ref);
    assert.ok(cite, `${identifier}: prototypeRef "${ref}" is not Ios<Screen>.dc.html:<line>`);
    const lines = readFileSync(join(root, 'design', 'prototype', cite[1]), 'utf8').split('\n');
    const line = lines[Number(cite[2]) - 1];
    assert.ok(line !== undefined, `${ref}: line does not exist`);
    assert.match(line, /text-overflow:\s*ellipsis|white-space:\s*nowrap/, `${ref}: line has neither text-overflow: ellipsis nor white-space: nowrap`);
  }
});
