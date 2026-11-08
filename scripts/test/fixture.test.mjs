import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseScreens, parseInteractiveParams } from '../lib/screens.mjs';
import { readPrototype, extractTemplateText } from '../lib/prototype.mjs';
import { collectRenderValsValues, fixtureValues, prototypeCorpus } from '../lib/fixture-check.mjs';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const PROTO = join(ROOT, 'design', 'prototype');
const SCREENS_MD = readFileSync(join(ROOT, 'design', 'screens.md'), 'utf8');
const SCREENS = parseScreens(SCREENS_MD);
const fixture = JSON.parse(readFileSync(join(ROOT, 'fixtures', 'baupilot.json'), 'utf8'));

const TRADE_IDS = ['earthworks', 'shell', 'carpentry', 'roofing', 'windows', 'electrical', 'plumbing', 'screed', 'tiles', 'painting', 'landscaping'];
const SCHEDULE_STATES = ['done', 'running', 'planned'];
const URGENCIES = ['late', 'soon', 'later'];
const DOC_KINDS = ['offer', 'contract', 'invoice', 'plan'];
const NOTIFICATION_KINDS = ['frist', 'geld', 'angebot', 'termin'];
const CHECK_KINDS = ['step', 'grid', 'form', 'signature', 'measurement'];
const CHECKS = {
  windows: { elements: ['AbnElemente', 'grid'], stepF8: ['AbnPruefung', 'step'], defectF2: ['AbnMangel', 'form'], signature: ['AbnSignatur', 'signature'] },
  electrical: { kitchen: ['ElRaum', 'step'], panel: ['ElVerteiler', 'step'], smoke: ['ElRauchmelder', 'step'] },
  plumbing: { pressure: ['SanDruck', 'step'], bathUpper: ['SanBad', 'step'], slope: ['SanGefaelle', 'measurement'] },
  screed: { readiness: ['EsBelegreife', 'step'], flatness: ['EsEbenheit', 'measurement'] },
  tiles: { bath: ['FlBad', 'step'] },
  painting: { living: ['MaRaum', 'step'] },
  shell: { measure: ['RohbauMasse', 'measurement'] },
};
const MEASUREMENTS = { RohbauMasse: 'shellOpening', EsEbenheit: 'screedFlatness', SanGefaelle: 'drainSlope' };

const isObject = (v) => v !== null && typeof v === 'object' && !Array.isArray(v);

// Visits every value with its key (array items inherit the array's key) and path.
function walk(value, visit, key = '', path = '') {
  visit(value, key, path);
  if (Array.isArray(value)) value.forEach((item, i) => walk(item, visit, key, `${path}[${i}]`));
  else if (isObject(value)) for (const [k, v] of Object.entries(value)) walk(v, visit, k, path ? `${path}.${k}` : k);
}

const get = (obj, path) => path.split('.').reduce((o, k) => o?.[k], obj);
const plain = (text) => (isObject(text) ? [text.ios, text.android] : [text]);

test('structure: top-level keys, counts, vocabularies, checks', () => {
  for (const key of ['meta', 'project', 'trades', 'schedule', 'offers', 'comparisons', 'documents', 'inspections', 'measurements', 'defects', 'resolvedDefects', 'notifications', 'screens']) {
    assert.ok(key in fixture, `missing top-level key ${key}`);
  }
  assert.equal(fixture.meta.now, '2026-12-18');
  assert.equal(fixture.trades.length, 11);
  assert.deepEqual(fixture.trades.map((t) => t.phase), [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]);
  assert.deepEqual(fixture.trades.map((t) => t.id), TRADE_IDS);
  assert.deepEqual(fixture.documents.counts, { all: 26, offer: 7, contract: 3, invoice: 4, plan: 12, trades: 11 });
  assert.equal(fixture.documents.items.length, 5);

  assert.equal(fixture.defects.length, 3);
  for (const d of fixture.defects) {
    const [, y, m, day] = /^(\d{4})-(\d{2})-(\d{2})$/.exec(d.deadline);
    for (const meta of plain(d.meta)) assert.ok(meta.includes(`Frist ${day}.${m}.${y}`), `${d.id}: ${meta} vs ${d.deadline}`);
  }
  assert.deepEqual(Object.keys(fixture.screens).sort(), SCREENS.map((s) => s.name).sort());

  for (const t of fixture.trades) assert.ok(SCHEDULE_STATES.includes(t.scheduleState), t.id);
  for (const d of fixture.defects) assert.ok(URGENCIES.includes(d.urgency), d.id);
  for (const d of fixture.documents.items) assert.ok(DOC_KINDS.includes(d.kind), d.id);
  for (const g of fixture.notifications) for (const n of g.items) assert.ok(NOTIFICATION_KINDS.includes(n.kind), n.id);

  const statusCodes = Object.keys(fixture.inspections.overview.statusLabels);
  const seenChecks = [];
  for (const [tradeId, entry] of Object.entries(fixture.inspections.byTrade)) {
    for (const [checkId, check] of Object.entries(entry.checks ?? {})) {
      const expected = CHECKS[tradeId]?.[checkId];
      assert.ok(expected, `unexpected check ${tradeId}.${checkId}`);
      assert.deepEqual([check.screen, check.kind], expected, `${tradeId}.${checkId}`);
      assert.ok(CHECK_KINDS.includes(check.kind));
      seenChecks.push(`${tradeId}.${checkId}`);
      if (check.kind === 'grid') for (const item of check.items ?? check.floors.flatMap((f) => f.items)) assert.ok(statusCodes.includes(item.status), `${checkId}: ${item.status}`);
    }
  }
  const allChecks = Object.entries(CHECKS).flatMap(([t, c]) => Object.keys(c).map((id) => `${t}.${id}`));
  assert.equal(allChecks.length, 15);
  assert.deepEqual(seenChecks.sort(), allChecks.sort());
  for (const row of fixture.inspections.overview.rows) assert.ok(statusCodes.includes(row.status), row.name);

  for (const platform of ['ios', 'android']) {
    const rows = fixture.screens.Profil[platform].groups[1].rows;
    assert.deepEqual(rows.map((r) => r.checked), [true, true, false], `Profil ${platform}`);
    const filters = fixture.screens.Dokumente[platform].filters;
    assert.deepEqual(filters.map((f) => f.selected), [true, false, false, false, false]);
    assert.equal(filters[0].text, 'Alle 26');
  }
  const walls = fixture.inspections.byTrade.painting.checks.living.walls;
  assert.deepEqual(walls.filter((w) => w.selected).map((w) => w.name), ['Süd']);
  const objects = fixture.inspections.byTrade.plumbing.checks.bathUpper.objects;
  assert.deepEqual(objects.filter((o) => o.selected).map((o) => o.name), ['Waschtisch']);

  // SanBad `ok` = third chk(...) argument of each platform's source.
  const entry = SCREENS.find((s) => s.name === 'SanBad');
  for (const file of [entry.iosFile, entry.androidFile]) {
    const src = readFileSync(join(PROTO, file), 'utf8');
    const oks = [...src.matchAll(/^\s*chk\('[^']*', '[^']*', (true|false), \d\)/gm)].map((m) => m[1] === 'true');
    assert.deepEqual(oks, [true, true, false, false, false], file);
    assert.deepEqual(fixture.inspections.byTrade.plumbing.checks.bathUpper.checks.map((c) => c.ok), oks, file);
  }
});

test('PlatformText: objects keyed ios/android have both keys, string or null', () => {
  walk(fixture, (value, key, path) => {
    if (!isObject(value)) return;
    const keys = Object.keys(value);
    if (keys.length === 0 || !keys.every((k) => k === 'ios' || k === 'android')) return;
    assert.deepEqual([...keys].sort(), ['android', 'ios'], `${path} must carry both platforms`);
    if (/^screens\.[A-Za-z0-9]+$/.test(path)) return; // per-platform screen blocks
    for (const k of keys) assert.ok(value[k] === null || typeof value[k] === 'string', `${path}.${k}`);
  });
});

const routePatterns = SCREENS.map((s) => s.route).map((r) => new RegExp(`^${r.replace(/\{[^}]+\}/g, '[^/{}]+')}$`));

test('referential integrity', () => {
  const ids = {
    trades: fixture.trades.map((t) => t.id),
    offers: fixture.offers.map((o) => o.id),
    documents: fixture.documents.items.map((d) => d.id),
    defects: fixture.defects.map((d) => d.id),
    notifications: fixture.notifications.flatMap((g) => g.items.map((n) => n.id)),
  };
  for (const [name, list] of Object.entries(ids)) assert.equal(new Set(list).size, list.length, `duplicate ids in ${name}`);

  const tradeIds = new Set(ids.trades);
  const offerIds = new Set(ids.offers);
  const documentIds = new Set(ids.documents);
  const measurementKeys = new Set(Object.keys(fixture.measurements));
  assert.deepEqual([...Object.keys(fixture.inspections.byTrade)].filter((t) => !tradeIds.has(t)), []);

  walk(fixture, (value, key, path) => {
    if (value === null || typeof value !== 'string') return;
    if (key === 'tradeId') assert.ok(tradeIds.has(value), `${path}: unknown trade ${value}`);
    if (key === 'tradeIds') assert.ok(tradeIds.has(value), `${path}: unknown trade ${value}`);
    if (key === 'offerIds') assert.ok(offerIds.has(value), `${path}: unknown offer ${value}`);
    if (key === 'documentId') assert.ok(documentIds.has(value), `${path}: unknown document ${value}`);
    if (key === 'measurementKey') assert.ok(measurementKeys.has(value), `${path}: unknown measurement ${value}`);
    if (key === 'route' || key === 'target') {
      assert.ok(routePatterns.some((re) => re.test(value)), `${path}: ${value} is not a screens.md route`);
      assert.ok(!value.endsWith('.dc.html'), `${path}: prototype file name`);
    }
  });
  assert.equal(fixture.project.phase.tradeId && tradeIds.has(fixture.project.phase.tradeId), true);
});

test('interactive parameters equal screens.md and both prototype schemas', () => {
  const params = parseInteractiveParams(SCREENS_MD);
  assert.equal(params.length, 3);
  for (const { screen, ...expected } of params) {
    const m = fixture.measurements[MEASUREMENTS[screen]];
    const { key, default: def, min, max, unit } = m.parameter;
    assert.deepEqual({ key, default: def, min, max, unit }, expected, screen);
    const entry = SCREENS.find((s) => s.name === screen);
    for (const file of [entry.iosFile, entry.androidFile]) {
      const schema = readPrototype(join(PROTO, file)).schema[expected.key];
      assert.equal(m.parameter.default, schema.default, file);
      assert.equal(m.parameter.min, schema.min, file);
      assert.equal(m.parameter.max, schema.max, file);
      assert.equal(m.parameter.step, schema.step, file);
    }
  }
  assert.equal(fixture.measurements.drainSlope.parameter.step, 0.1);
  assert.equal('step' in fixture.measurements.shellOpening.parameter, false);
});

test('screedHeating constants equal the source plan array and its day thresholds (R3)', () => {
  const { planCelsius, recordedDays, todayDay } = fixture.measurements.screedHeating;
  for (const file of ['IosEsBelegreife.dc.html', 'AndEsBelegreife.dc.html']) {
    const text = readFileSync(join(PROTO, file), 'utf8');
    const plan = /const plan = \[([^\]]*)\];/.exec(text);
    assert.ok(plan, `${file}: no plan array`);
    assert.deepEqual(plan[1].split(',').map((n) => Number(n.trim())), planCelsius, file);
    const bars = /i < (\d+) \? [^:]+ : i === (\d+) \?/.exec(text);
    assert.ok(bars, `${file}: no recorded/today bar rule`);
    assert.equal(Number(bars[1]), recordedDays, file);
    assert.equal(Number(bars[2]) + 1, todayDay, file);
  }
});

test('renderVals coverage (AC3): every content value of all 34 screens is in the fixture', () => {
  const have = fixtureValues(fixture);
  const missing = [...collectRenderValsValues(PROTO)].filter(([text]) => !have.has(text));
  assert.deepEqual(missing.map(([text, where]) => `${JSON.stringify(text)} <- ${where.join(', ')}`), []);
});

const EXEMPT_KEY = /^(status|kind|scheduleState|urgency|id|route|target|measurementKey|deadline|screen|key)$|Ids?$/;

// The nearest ancestor key that is not the ios/android side of a PlatformText.
const semanticKey = (key, path) => (key === 'ios' || key === 'android'
  ? path.split('.').map((seg) => seg.replace(/\[\d+\]/g, '')).filter((seg) => seg !== 'ios' && seg !== 'android').pop() ?? key
  : key);

function findInvented(data) {
  const corpus = prototypeCorpus(PROTO);
  const known = new Set(collectRenderValsValues(PROTO).keys());
  const vocabulary = new Set([
    ...Object.keys(fixture.inspections.overview.statusLabels),
    ...SCHEDULE_STATES, ...URGENCIES, ...DOC_KINDS, ...NOTIFICATION_KINDS, ...CHECK_KINDS,
  ]);
  const interactiveKeys = new Set(parseInteractiveParams(SCREENS_MD).map((p) => p.key));
  const screenNames = new Set(SCREENS.map((s) => s.name));
  const bad = [];
  walk(data, (value, rawKey, path) => {
    if (typeof value !== 'string' || path === 'meta' || path.startsWith('meta.')) return;
    const key = semanticKey(rawKey, path);
    if (key !== rawKey && EXEMPT_KEY.test(key)) {
      bad.push(`${path}: display text under the code/id key ${key}`);
    } else if (['status', 'kind', 'scheduleState', 'urgency'].includes(key)) {
      if (!vocabulary.has(value)) bad.push(`${path}: ${value} not in a declared vocabulary`);
    } else if (key === 'id' || /Ids?$/.test(key)) {
      if (!/^[A-Za-z0-9][A-Za-z0-9_-]*$/.test(value)) bad.push(`${path}: ${value} is not an id`);
    } else if (key === 'route' || key === 'target' || key === 'measurementKey') {
      // checked by referential integrity
    } else if (key === 'deadline') {
      if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) bad.push(`${path}: ${value} is not an ISO date`);
    } else if (key === 'screen') {
      if (!screenNames.has(value)) bad.push(`${path}: ${value} is not a screen`);
    } else if (key === 'key') {
      if (!interactiveKeys.has(value)) bad.push(`${path}: ${value} is not an interactive parameter`);
    } else if (!corpus.includes(value) && !known.has(value)) {
      bad.push(`${path}: ${JSON.stringify(value)}`);
    }
  });
  return bad;
}

test('anti-invention: every string leaf comes from the prototypes', () => {
  assert.deepEqual(findInvented(fixture), []);
});

test('anti-invention: display text wrapped in a PlatformText under an exempt key is rejected (I4)', () => {
  const mutated = structuredClone(fixture);
  mutated.trades[0].status = { ios: 'Beauftragt', android: 'Beauftragt' };
  mutated.trades[1].id = { ios: 'Beauftragt', android: 'Beauftragt' };
  const bad = findInvented(mutated);
  assert.ok(bad.some((b) => b.startsWith('trades[0].status.ios') && b.includes('status')), bad.join('\n'));
  assert.ok(bad.some((b) => b.startsWith('trades[1].id.android')), bad.join('\n'));
});

// UI chrome of the sampled screens that is not data: headings, button and field labels.
const CHROME = {
  'IosHome.dc.html': ['Aktuelle Phase', 'Als Nächstes benötigt', 'Hauptnavigation'],
  'AndHome.dc.html': ['Aktuelle Phase', 'Als Nächstes benötigt', 'Hauptnavigation'],
  'IosDokViewer.dc.html': ['Teilen', 'Gesamt brutto', 'Im Vergleich ansehen'],
  'AndDokViewer.dc.html': ['Zurück', 'Teilen', 'Gesamt brutto', 'Gewerk oder Typ ändern', 'Im Vergleich ansehen'],
  'IosAbnSignatur.dc.html': ['Unterschrift Handwerker', 'Hier unterschreiben', 'Protokoll abschließen'],
  'AndAbnSignatur.dc.html': ['Zurück', 'Vorbehaltene Mängel', 'Unterschrift Handwerker', 'Zum Unterschreiben tippen', 'Protokoll abschließen'],
  'IosElUebersicht.dc.html': [],
  'AndElUebersicht.dc.html': ['Zurück'],
  'IosSanDruck.dc.html': ['Manometer fotografieren'],
  'AndSanDruck.dc.html': ['Manometer fotografieren'],
};

test('sampled screens: every template text is in the fixture or listed UI chrome', () => {
  const strings = [...fixtureValues(fixture)];
  for (const [file, chrome] of Object.entries(CHROME)) {
    const segments = extractTemplateText(readPrototype(join(PROTO, file)).template);
    const missing = segments.filter((s) => !chrome.includes(s) && !strings.some((f) => f.includes(s)));
    assert.deepEqual(missing, [], `${file}: texts neither in the fixture nor in CHROME`);
    const stale = chrome.filter((c) => !segments.includes(c));
    assert.deepEqual(stale, [], `${file}: CHROME entries that do not occur`);
  }
});

test('single source: exactly one baupilot.json, at fixtures/', () => {
  const files = execFileSync('git', ['-C', ROOT, 'ls-files', '--cached', '--others', '--exclude-standard'], { encoding: 'utf8' })
    .split('\n')
    .filter((f) => f.split('/').pop() === 'baupilot.json');
  assert.deepEqual(files, ['fixtures/baupilot.json']);
});
