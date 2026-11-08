import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseScreens, parseInteractiveParams } from '../lib/screens.mjs';

const md = readFileSync(join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'design', 'screens.md'), 'utf8');

test('parseScreens returns 34 entries, entry 1 is Login', () => {
  const screens = parseScreens(md);
  assert.equal(screens.length, 34);
  assert.deepEqual(
    { ...screens[0] },
    { nr: 1, name: 'Login', title: 'Login', iosFile: 'Main.dc.html', androidFile: 'AndLogin.dc.html', route: 'login', milestone: 'M2' },
  );
});

test('parseScreens strips the route suffix', () => {
  assert.equal(parseScreens(md)[1].route, 'home');
});

test('parseInteractiveParams returns the three interactive parameters', () => {
  const params = parseInteractiveParams(md);
  assert.equal(params.length, 3);
  assert.deepEqual(params.find((p) => p.screen === 'SanGefaelle'), { screen: 'SanGefaelle', key: 'soll', default: 1, min: 0.5, max: 3, unit: '%' });
  assert.deepEqual(params.find((p) => p.screen === 'RohbauMasse'), { screen: 'RohbauMasse', key: 'toleranz', default: 12, min: 1, max: 30, unit: 'mm' });
  assert.deepEqual(params.find((p) => p.screen === 'EsEbenheit'), { screen: 'EsEbenheit', key: 'grenzwert', default: 4, min: 1, max: 15, unit: 'mm' });
});

test('wrong row count throws screens.md error', () => {
  const cut = md.split('\n').filter((l) => !l.startsWith('| 34 |')).join('\n');
  assert.throws(() => parseScreens(cut), /^Error: screens\.md: /);
});
