import { test } from 'node:test';
import assert from 'node:assert/strict';
import { dirname, join } from 'node:path';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { listPrototypes, readPrototype, evalRenderVals, extractTemplateText } from '../lib/prototype.mjs';

const dir = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'design', 'prototype');
const all = listPrototypes(dir);

test('listPrototypes maps 136 files, Main.dc.html is iOS Login', () => {
  assert.equal(all.length, 136);
  assert.deepEqual(all.find((p) => p.file === 'Main.dc.html'), { file: 'Main.dc.html', platform: 'ios', screen: 'Login', dark: false });
  assert.equal(all.filter((p) => p.dark).length, 68);
});

test('evalRenderVals succeeds for all 68 light files', () => {
  const light = all.filter((p) => !p.dark);
  assert.equal(light.length, 68);
  for (const p of light) {
    const vals = evalRenderVals(join(dir, p.file));
    assert.equal(typeof vals, 'object', p.file);
  }
});

test('every dark wrapper returns isDark: true', () => {
  for (const p of all.filter((x) => x.dark)) {
    assert.equal(evalRenderVals(join(dir, p.file)).isDark, true, p.file);
  }
});

test('IosRohbauMasse applies data-props defaults and constructor state', () => {
  const file = join(dir, 'IosRohbauMasse.dc.html');
  const vals = evalRenderVals(file);
  assert.equal(vals.dims[0].diff, '−5 mm');
  const { props, schema } = readPrototype(file);
  assert.equal(props.toleranz, 12);
  assert.equal(props.dark, false);
  assert.equal(schema.toleranz.max, 30);
  assert.equal('$preview' in schema, false);
});

test('readPrototype template excludes helmet', () => {
  const { template } = readPrototype(join(dir, 'Main.dc.html'));
  assert.ok(!template.includes('<helmet'));
  assert.ok(template.includes('Ihr Bauvorhaben.'));
});

test('extractTemplateText on Main.dc.html', () => {
  const texts = extractTemplateText(readPrototype(join(dir, 'Main.dc.html')).template);
  assert.ok(texts.includes('Vergessen?'));
  assert.ok(texts.includes('Ihr Bauvorhaben.'));
  assert.ok(texts.every((t) => !t.includes('{{')));
});

test('extractTemplateText splits at <br>, bindings, collects attributes, decodes entities', () => {
  assert.deepEqual(extractTemplateText('<h1>Ihr Bauvorhaben.<br>Ein Plan.</h1>'), ['Ihr Bauvorhaben.', 'Ein Plan.']);
  assert.deepEqual(
    extractTemplateText('<button aria-label="{{m.label}} verringern" onClick="{{m.down}}" hint-x="Nein">A &amp; B</button><style>x{}</style><svg><text>7</text></svg>'),
    ['verringern', 'A & B', '7'],
  );
});
