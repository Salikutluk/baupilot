// Prints { "<file>": { props, vals } } for all light prototypes as JSON (theme key `t` removed).
// Used while assembling the fixture; nothing is written to disk.
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { listPrototypes, readPrototype, evalRenderVals } from './lib/prototype.mjs';

const dir = join(dirname(fileURLToPath(import.meta.url)), '..', 'design', 'prototype');
const result = {};
for (const p of listPrototypes(dir).filter((x) => !x.dark)) {
  const path = join(dir, p.file);
  const { t, ...vals } = evalRenderVals(path);
  result[p.file] = { props: readPrototype(path).props, vals };
}
process.stdout.on('error', (e) => { if (e.code !== 'EPIPE') throw e; });
process.stdout.write(`${JSON.stringify(result, null, 2)}\n`);
