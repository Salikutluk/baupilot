import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import { lowerCamel } from '../lib/tokens.mjs';

// Structural guards for the design system (invariants I2, I4, I7, I8, I10): each scanner takes a repo root,
// returns "file:line: text" violations, and is run on the real tree plus on a planted violation in a temp root.

const REPO = join(dirname(fileURLToPath(import.meta.url)), '..', '..');

const IOS_COMPONENTS = 'ios/Baupilot/DesignSystem/Components';
const ANDROID_COMPONENTS = 'android/app/src/main/java/de/baupilot/designsystem/components';
const RESERVED_MEMBERS = new Set(['core', 'color', 'light', 'dark', 'resolve']);
const SCREEN_GROUPS = ['track', 'border', 'chip', 'err', 'ok', 'okInk', 'wFg', 'eSoft', 'warnFg', 'okBg'];

function walk(root, dir, exts) {
  const base = join(root, dir);
  const out = [];
  const visit = (abs) => {
    let entries;
    try {
      entries = readdirSync(abs, { withFileTypes: true });
    } catch {
      return;
    }
    for (const e of entries) {
      const p = join(abs, e.name);
      if (e.isDirectory()) visit(p);
      else if (exts.some((x) => e.name.endsWith(x))) out.push(p);
    }
  };
  visit(base);
  return out.sort();
}

// Blank out // and /* */ comments but keep line numbers and string contents.
function stripComments(src) {
  let out = '';
  let i = 0;
  let inStr = false;
  while (i < src.length) {
    const c = src[i];
    const n = src[i + 1];
    if (inStr) {
      out += c;
      if (c === '\\') {
        out += n ?? '';
        i += 2;
        continue;
      }
      if (c === '"' || c === '\n') inStr = false;
      i += 1;
    } else if (c === '"') {
      inStr = true;
      out += c;
      i += 1;
    } else if (c === '/' && n === '/') {
      while (i < src.length && src[i] !== '\n') i += 1;
    } else if (c === '/' && n === '*') {
      i += 2;
      while (i < src.length && !(src[i] === '*' && src[i + 1] === '/')) {
        if (src[i] === '\n') out += '\n';
        i += 1;
      }
      i += 2;
    } else {
      out += c;
      i += 1;
    }
  }
  return out;
}

function scanLines(root, files, test, { comments = false } = {}) {
  const hits = [];
  for (const file of files) {
    const raw = readFileSync(file, 'utf8');
    const lines = (comments ? raw : stripComments(raw)).split('\n');
    const rawLines = raw.split('\n');
    lines.forEach((line, i) => {
      if (test(line, file, i)) hits.push(`${relative(root, file)}:${i + 1}: ${rawLines[i].trim()}`);
    });
  }
  return hits;
}

// ---- T-DS-1 ---------------------------------------------------------------------------------------------

function screenVariantNames(platform) {
  const tokens = JSON.parse(readFileSync(join(REPO, 'design', 'tokens.json'), 'utf8'));
  const names = new Map(SCREEN_GROUPS.map((g) => [g, new Set()]));
  for (const [screen, overrides] of Object.entries(tokens[platform].colors.perScreen)) {
    for (const token of Object.keys(overrides)) {
      if (names.has(lowerCamel(token))) names.get(lowerCamel(token)).add(lowerCamel(screen));
    }
  }
  return names;
}

function scanComponentTokens(root) {
  const re = new RegExp(`\\bTokens\\.(${SCREEN_GROUPS.join('|')})\\.(\\w+)`, 'g');
  const files = [
    ...walk(root, IOS_COMPONENTS, ['.swift']),
    ...walk(root, ANDROID_COMPONENTS, ['.kt']),
  ];
  return scanLines(root, files, (line) => {
    for (const m of line.matchAll(re)) if (!RESERVED_MEMBERS.has(m[2])) return true;
    return false;
  });
}

test('T-DS-1: the guarded groups have perScreen screens whose names are not reserved members', () => {
  for (const platform of ['ios', 'android']) {
    const names = screenVariantNames(platform);
    // The platforms override different groups (iOS: border, chip, err...; Android: warnFg, okBg), so only require a few.
    const withScreens = SCREEN_GROUPS.filter((g) => names.get(g).size > 0);
    assert.ok(withScreens.includes('track'), `${platform}: track has no perScreen overrides`);
    assert.ok(withScreens.length >= 3, `${platform}: only ${withScreens.join(', ')} have perScreen overrides`);
    for (const group of withScreens) {
      for (const member of names.get(group)) {
        assert.ok(!RESERVED_MEMBERS.has(member), `${platform}: ${group}.${member} collides with a reserved member`);
      }
    }
  }
});

test('T-DS-1: components use no per-screen token variant (I7)', () => {
  assert.deepEqual(scanComponentTokens(REPO), []);
});

test('T-DS-1: a planted Tokens.<group>.<screen> in a component is reported, reserved members are not', () => {
  const dir = mkdtempSync(join(tmpdir(), 'ds1-'));
  try {
    const ios = names('ios');
    const android = names('android');
    mkdirSync(join(dir, IOS_COMPONENTS), { recursive: true });
    mkdirSync(join(dir, ANDROID_COMPONENTS), { recursive: true });
    writeFileSync(
      join(dir, IOS_COMPONENTS, 'Bad.swift'),
      `let a = Tokens.track.${ios.track}\nlet ok = Tokens.track.core\nlet b = Tokens.border.color\n// Tokens.chip.${ios.chip}\n`,
    );
    writeFileSync(
      join(dir, ANDROID_COMPONENTS, 'Bad.kt'),
      `val a = Tokens.wFg.${android.wFg}\nval ok = Tokens.wFg.core.light\n`,
    );
    assert.deepEqual(scanComponentTokens(dir), [
      `${IOS_COMPONENTS}/Bad.swift:1: let a = Tokens.track.${ios.track}`,
      `${ANDROID_COMPONENTS}/Bad.kt:1: val a = Tokens.wFg.${android.wFg}`,
    ]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }

  function names(platform) {
    const m = screenVariantNames(platform);
    return { track: [...m.get('track')][0], chip: [...m.get('chip')][0], wFg: [...m.get('wFg')][0] };
  }
});

// ---- T-DS-2 ---------------------------------------------------------------------------------------------

const GALLERY_TYPE = /\b(GalleryScreen|GalleryPageContent|GalleryPage|GalleryActivity)\b/;

function scanGalleryIsolation(root) {
  const hits = [];
  const kotlin = [
    ...walk(root, 'android/app/src/main', ['.kt', '.xml', '.kts', '.java']),
    ...walk(root, 'android/app/src/test', ['.kt', '.xml', '.kts', '.java']),
  ];
  hits.push(...scanLines(root, kotlin, (line) => line.includes('de.baupilot.gallery'), { comments: true }));

  // Swift app sources: the gallery types may only appear in Debug/ or inside `#if DEBUG` in BaupilotApp.swift.
  const swift = walk(root, 'ios/Baupilot', ['.swift']).filter((f) => !relative(root, f).startsWith('ios/Baupilot/Debug/'));
  for (const file of swift) {
    const lines = stripComments(readFileSync(file, 'utf8')).split('\n');
    const raw = readFileSync(file, 'utf8').split('\n');
    const isApp = relative(root, file) === 'ios/Baupilot/App/BaupilotApp.swift';
    const stack = []; // true while the current #if branch is the DEBUG one
    lines.forEach((line, i) => {
      const t = line.trim();
      if (/^#if\b/.test(t)) stack.push(t === '#if DEBUG');
      else if (/^#else\b|^#elseif\b/.test(t)) stack[stack.length - 1] = false;
      else if (/^#endif\b/.test(t)) stack.pop();
      else if (GALLERY_TYPE.test(line) && !(isApp && stack.includes(true))) {
        hits.push(`${relative(root, file)}:${i + 1}: ${raw[i].trim()}`);
      }
    });
  }

  for (const file of walk(root, 'ios/Baupilot/Debug', ['.swift'])) {
    const first = readFileSync(file, 'utf8').split('\n').find((l) => l.trim() !== '');
    if (first?.trim() !== '#if DEBUG') hits.push(`${relative(root, file)}:1: first line is not #if DEBUG`);
  }
  return hits;
}

test('T-DS-2: gallery code is isolated from the release targets (I8)', () => {
  assert.deepEqual(scanGalleryIsolation(REPO), []);
});

test('T-DS-2: planted gallery leaks are reported', () => {
  const dir = mkdtempSync(join(tmpdir(), 'ds2-'));
  try {
    const put = (path, text) => {
      mkdirSync(dirname(join(dir, path)), { recursive: true });
      writeFileSync(join(dir, path), text);
    };
    put('android/app/src/main/java/A.kt', 'import de.baupilot.gallery.GalleryPage\n');
    put('android/app/src/test/java/B.kt', 'import de.baupilot.gallery.GalleryActivity\n');
    put('ios/Baupilot/DesignSystem/Leak.swift', 'let p = GalleryPage.allCases\n');
    put('ios/Baupilot/App/BaupilotApp.swift', '#if DEBUG\nlet g = GalleryScreen()\n#else\nlet h = GalleryScreen()\n#endif\n');
    put('ios/Baupilot/Debug/Gallery/Unguarded.swift', 'import SwiftUI\n#if DEBUG\n#endif\n');
    put('ios/Baupilot/Debug/Gallery/Guarded.swift', '\n#if DEBUG\nlet x = GalleryPage.self\n#endif\n');
    assert.deepEqual(scanGalleryIsolation(dir), [
      'android/app/src/main/java/A.kt:1: import de.baupilot.gallery.GalleryPage',
      'android/app/src/test/java/B.kt:1: import de.baupilot.gallery.GalleryActivity',
      'ios/Baupilot/App/BaupilotApp.swift:4: let h = GalleryScreen()',
      'ios/Baupilot/DesignSystem/Leak.swift:1: let p = GalleryPage.allCases',
      'ios/Baupilot/Debug/Gallery/Unguarded.swift:1: first line is not #if DEBUG',
    ]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

// ---- T-DS-3 ---------------------------------------------------------------------------------------------

const CONSTANT_FILES = new Set([
  'android/app/src/test/java/de/baupilot/snapshot/BaupilotPaparazzi.kt',
  'ios/BaupilotTests/Snapshots/ReferenceSnapshot.swift',
]);
const VERIFIER_FILES = [
  'android/app/src/test/java/de/baupilot/snapshot/GoldenSnapshotHandler.kt',
  'android/app/src/test/java/de/baupilot/snapshot/StagedSnapshotHandler.kt',
];
const TOLERANCE_PATTERNS = [
  /\bmaxPercentDifference\b\s*[=:]?\s*[-+]?\d/,
  /\bMAX_PERCENT_DIFFERENCE\s*=/,
  /\bSnapshotVerifier\(/,
  /\bprecision:/,
  /\bperceptualPrecision:/,
];

function scanTolerances(root) {
  const files = [
    ...walk(root, 'ios/BaupilotTests', ['.swift']),
    ...walk(root, 'android/app/src/test', ['.kt']),
    ...walk(root, 'android/app/src/testDebug', ['.kt']),
  ].filter((f) => !CONSTANT_FILES.has(relative(root, f)));
  const hits = scanLines(root, files, (line, file) => {
    const rel = relative(root, file);
    return TOLERANCE_PATTERNS.some((re) => {
      if (!re.test(line)) return false;
      // A SnapshotVerifier( call is allowed in the two handlers; its argument is checked below.
      return !(re.source.startsWith('\\bSnapshotVerifier') && VERIFIER_FILES.includes(rel));
    });
  });

  let verifiers = 0;
  for (const rel of VERIFIER_FILES) {
    let src;
    try {
      src = stripComments(readFileSync(join(root, rel), 'utf8'));
    } catch {
      continue;
    }
    for (const m of src.matchAll(/\bSnapshotVerifier\(\s*([^,)\s]*)/g)) {
      verifiers += 1;
      if (m[1] !== 'MAX_PERCENT_DIFFERENCE') {
        const line = src.slice(0, m.index).split('\n').length;
        hits.push(`${rel}:${line}: SnapshotVerifier first argument is "${m[1]}", not MAX_PERCENT_DIFFERENCE`);
      }
    }
  }
  if (verifiers === 0) hits.push(`${VERIFIER_FILES.join(' | ')}: no SnapshotVerifier( call found`);
  return hits;
}

test('T-DS-3: no tolerance literals outside the harness constants (I4)', () => {
  assert.deepEqual(scanTolerances(REPO), []);
});

test('T-DS-3: planted tolerance literals and a literal verifier threshold are reported', () => {
  const dir = mkdtempSync(join(tmpdir(), 'ds3-'));
  try {
    const put = (path, text) => {
      mkdirSync(dirname(join(dir, path)), { recursive: true });
      writeFileSync(join(dir, path), text);
    };
    put('android/app/src/test/java/de/baupilot/snapshot/BaupilotPaparazzi.kt', 'const val MAX_PERCENT_DIFFERENCE = 0.5\nval c = Cfg(maxPercentDifference = 0.5)\n');
    put('ios/BaupilotTests/Snapshots/ReferenceSnapshot.swift', 'static let precision: Float = 0.995\n');
    put('android/app/src/test/java/de/baupilot/snapshot/StagedSnapshotHandler.kt', 'val v = SnapshotVerifier(2.0, root)\n');
    put('android/app/src/test/java/de/baupilot/snapshot/GoldenSnapshotHandler.kt', 'val v = SnapshotVerifier(MAX_PERCENT_DIFFERENCE, root)\n');
    put('android/app/src/testDebug/java/T1.kt', 'val c = Cfg(maxPercentDifference = 1.0)\nval d = Cfg(maxPercentDifference = MAX_PERCENT_DIFFERENCE)\n');
    put('android/app/src/testDebug/java/T2.kt', 'const val MAX_PERCENT_DIFFERENCE = 9\nval v = SnapshotVerifier(MAX_PERCENT_DIFFERENCE, r)\n');
    put('ios/BaupilotTests/T3.swift', 'assertSnapshot(precision: 0.9, perceptualPrecision: 0.9)\n// precision: 1\n');
    const hits = scanTolerances(dir);
    assert.ok(hits.includes('android/app/src/testDebug/java/T1.kt:1: val c = Cfg(maxPercentDifference = 1.0)'), hits.join('\n'));
    assert.ok(!hits.some((h) => h.includes('T1.kt:2')), hits.join('\n'));
    assert.ok(hits.includes('android/app/src/testDebug/java/T2.kt:1: const val MAX_PERCENT_DIFFERENCE = 9'), hits.join('\n'));
    assert.ok(hits.includes('android/app/src/testDebug/java/T2.kt:2: val v = SnapshotVerifier(MAX_PERCENT_DIFFERENCE, r)'), hits.join('\n'));
    assert.ok(hits.includes('ios/BaupilotTests/T3.swift:1: assertSnapshot(precision: 0.9, perceptualPrecision: 0.9)'), hits.join('\n'));
    assert.ok(!hits.some((h) => h.includes('T3.swift:2')), hits.join('\n'));
    assert.ok(
      hits.includes('android/app/src/test/java/de/baupilot/snapshot/StagedSnapshotHandler.kt:1: SnapshotVerifier first argument is "2.0", not MAX_PERCENT_DIFFERENCE'),
      hits.join('\n'),
    );
    assert.ok(!hits.some((h) => h.includes('GoldenSnapshotHandler.kt')), hits.join('\n'));
    assert.ok(!hits.some((h) => h.includes('BaupilotPaparazzi.kt') || h.includes('ReferenceSnapshot.swift')), hits.join('\n'));
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

// ---- T-DS-4 ---------------------------------------------------------------------------------------------

function scanSystemControls(root) {
  return [
    ...scanLines(root, walk(root, ANDROID_COMPONENTS, ['.kt']), (l) => l.includes('import androidx.compose.material3.')),
    ...scanLines(root, walk(root, IOS_COMPONENTS, ['.swift']), (l) => /\bToggle\(/.test(l) || l.includes('.borderedProminent')),
  ];
}

test('T-DS-4: components import no Material 3 and use no system Toggle or borderedProminent (I10)', () => {
  assert.deepEqual(scanSystemControls(REPO), []);
});

test('T-DS-4: planted material3 import, Toggle( and .borderedProminent are reported', () => {
  const dir = mkdtempSync(join(tmpdir(), 'ds4-'));
  try {
    mkdirSync(join(dir, IOS_COMPONENTS), { recursive: true });
    mkdirSync(join(dir, ANDROID_COMPONENTS), { recursive: true });
    writeFileSync(join(dir, ANDROID_COMPONENTS, 'Bad.kt'), 'import androidx.compose.material3.Button\nimport androidx.compose.foundation.layout.Box\n');
    writeFileSync(join(dir, IOS_COMPONENTS, 'Bad.swift'), 'Toggle(isOn: $x) {}\nButton("a") {}.buttonStyle(.borderedProminent)\n// Toggle(\nlet t = IosToggle(on: x)\n');
    assert.deepEqual(scanSystemControls(dir), [
      `${ANDROID_COMPONENTS}/Bad.kt:1: import androidx.compose.material3.Button`,
      `${IOS_COMPONENTS}/Bad.swift:1: Toggle(isOn: $x) {}`,
      `${IOS_COMPONENTS}/Bad.swift:2: Button("a") {}.buttonStyle(.borderedProminent)`,
    ]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
