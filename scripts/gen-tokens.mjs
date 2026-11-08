import { existsSync, mkdirSync, readFileSync, realpathSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { parseScreens } from './lib/screens.mjs';
import { generateKotlin, generateSwift, mergeSupplement, TokenError, validateSupplement, validateTokens } from './lib/tokens.mjs';

const DEFAULT_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const SWIFT_PATH = 'ios/Baupilot/DesignSystem/Tokens.swift';
const KOTLIN_PATH = 'android/app/src/main/java/de/baupilot/designsystem/Tokens.kt';

export function run({ root = DEFAULT_ROOT, tokensPath, supplementPath, check = false } = {}) {
  const tokensFile = tokensPath ? resolve(tokensPath) : join(root, 'design', 'tokens.json');
  const json = JSON.parse(readFileSync(tokensFile, 'utf8'));
  const screenNames = parseScreens(readFileSync(join(root, 'design', 'screens.md'), 'utf8')).map((s) => s.name);
  const supplementFile = supplementPath ? resolve(supplementPath) : join(root, 'scripts', 'tokens-supplement.json');
  const hasSupplement = existsSync(supplementFile);
  if (supplementPath && !hasSupplement) {
    throw new TokenError('supplement-missing', supplementFile, `gen-tokens: supplement file not found: ${supplementFile}`);
  }
  const supplement = hasSupplement ? JSON.parse(readFileSync(supplementFile, 'utf8')) : null;
  let merged = json;
  try {
    validateTokens(json, screenNames);
    if (supplement) {
      validateSupplement(supplement, json);
      merged = mergeSupplement(json, supplement);
    }
  } catch (error) {
    if (error instanceof TokenError) return { code: 2, written: [], stale: [], error };
    throw error;
  }
  const outputs = [
    [join(root, SWIFT_PATH), generateSwift(merged, { supplement: hasSupplement })],
    [join(root, KOTLIN_PATH), generateKotlin(merged, { supplement: hasSupplement })],
  ];
  if (check) {
    const stale = outputs
      .filter(([file, content]) => !existsSync(file) || readFileSync(file, 'utf8') !== content)
      .map(([file]) => file);
    return { code: stale.length > 0 ? 1 : 0, written: [], stale };
  }
  for (const [file, content] of outputs) {
    mkdirSync(dirname(file), { recursive: true });
    writeFileSync(file, content);
  }
  return { code: 0, written: outputs.map(([file]) => file), stale: [] };
}

function main() {
  const { values } = parseArgs({
    options: {
      check: { type: 'boolean', default: false },
      tokens: { type: 'string' },
      supplement: { type: 'string' },
      root: { type: 'string' },
    },
  });
  const root = values.root ? resolve(values.root) : DEFAULT_ROOT;
  let result;
  try {
    result = run({ root, tokensPath: values.tokens, supplementPath: values.supplement, check: values.check });
  } catch (error) {
    if (!(error instanceof TokenError)) throw error;
    console.error(error.message);
    process.exitCode = 2;
    return;
  }
  if (result.error) {
    console.error(result.error.message);
  } else if (result.stale.length > 0) {
    console.error('gen-tokens: generated files are out of date:');
    for (const file of result.stale) console.error(`  ${relative(root, file)}`);
  } else {
    for (const file of result.written) console.log(`gen-tokens: wrote ${relative(root, file)}`);
  }
  process.exitCode = result.code;
}

if (process.argv[1] && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url))) {
  try {
    main();
  } catch (error) {
    console.error(`gen-tokens: ${error.message}`);
    process.exitCode = 2;
  }
}
