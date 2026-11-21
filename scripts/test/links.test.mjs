import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseLinks, validateCoverage, readSources } from '../lib/links.mjs';

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
