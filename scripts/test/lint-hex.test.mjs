import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdirSync, mkdtempSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { run, scanSource } from '../lint-hex.mjs';

const REPO = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const SWIFT = 'ios/Baupilot/Screens/Probe.swift';
const KOTLIN = 'android/app/src/main/java/de/baupilot/Probe.kt';
const XML = 'android/app/src/main/res/values/colors.xml';
const SWIFT_TEST = 'ios/BaupilotTests/ProbeTests.swift';
const KOTLIN_TEST = 'android/app/src/test/java/de/baupilot/ProbeTest.kt';

const rules = (text, path) => scanSource(text, { path }).map((f) => f.rule);

test('flags #RRGGBB, #RRGGBBAA, #RGB in swift/kt/xml', () => {
  for (const path of [SWIFT, KOTLIN, XML]) {
    for (const hex of ['#C2410C', '#C2410CFF', '#c41', '#c41f']) {
      assert.deepEqual(rules(`let c = "${hex}"`, path), ['hex-literal'], `${path} ${hex}`);
    }
  }
  const [finding] = scanSource('<color name="a">#C2410C</color>', { path: XML });
  assert.deepEqual(finding, { path: XML, line: 1, col: 17, rule: 'hex-literal', match: '#C2410C' });
});

test('flags 0xFFC2410C and Color(0xFF…) in kotlin and 0xC2410CFF in swift', () => {
  assert.deepEqual(rules('val a = 0xFFC2410C', KOTLIN), ['int-color']);
  assert.deepEqual(rules('val a = 0xFFC2410CUL', KOTLIN), ['int-color']);
  assert.deepEqual(rules('val a = 0xC2410CL', KOTLIN), ['int-color']);
  assert.deepEqual(rules('val a = Color(0xFFC2410C)', KOTLIN).sort(), ['color-ctor', 'int-color']);
  assert.deepEqual(rules('let a = 0xC2410CFF', SWIFT), ['int-color']);
  assert.deepEqual(rules('0xC2410C', SWIFT), ['int-color']);
});

test('flags Color(red:), UIColor(white:), Color.White, Color.white', () => {
  assert.deepEqual(rules('Color(red: 1, green: 0, blue: 0)', SWIFT), ['color-ctor']);
  assert.deepEqual(rules('UIColor(white: 1, alpha: 1)', SWIFT), ['color-ctor']);
  assert.deepEqual(rules('Color(hue: 1, saturation: 1, brightness: 1)', SWIFT), ['color-ctor']);
  assert.deepEqual(rules('Color(.sRGB, red: 1, green: 1, blue: 1)', SWIFT), ['color-ctor']);
  assert.deepEqual(rules('Color(red = 1f, green = 0f, blue = 0f)', KOTLIN), ['color-ctor']);
  assert.deepEqual(rules('Color.rgb(1, 2, 3)', KOTLIN), ['color-ctor']);
  assert.deepEqual(rules('Color.argb(1, 2, 3, 4)', KOTLIN), ['color-ctor']);
  assert.deepEqual(rules('Color.parseColor("x")', KOTLIN), ['color-ctor']);
  assert.deepEqual(rules('val c = Color.White', KOTLIN), ['named-color']);
  assert.deepEqual(rules('val c = Color.DarkGray', KOTLIN), ['named-color']);
  assert.deepEqual(rules('let c = Color.white', SWIFT), ['named-color']);
  assert.deepEqual(rules('let c = UIColor.systemRed; let d = UIColor.black', SWIFT), ['named-color']);
});

test('does not flag #if, #Preview, #filePath, #available, 0x2265, byteArrayOf(0x00, 0x01), "Größe 10x10", Color.Transparent, .clear', () => {
  const swift = [
    '#if DEBUG',
    '#Preview { Text("x") }',
    'let p = #filePath',
    'if #available(iOS 17, *) {}',
    'let s = "≥ 0x2265"',
    'let c = Color.clear',
    'let d = UIColor.clear',
    'view.background = .clear',
  ].join('\n');
  assert.deepEqual(scanSource(swift, { path: SWIFT }), []);
  const kotlin = [
    'val b = byteArrayOf(0x00, 0x01, 0x02, 0x03)',
    'val s = "Größe 10x10"',
    '// 0x2265 is ≥',
    'val c = Color.Transparent',
    'val u = Color.Unspecified',
    'val e = "&#123;"',
  ].join('\n');
  assert.deepEqual(scanSource(kotlin, { path: KOTLIN }), []);
});

test('hex digit counts other than 3, 4, 6, 8 and longer words are not colours', () => {
  assert.deepEqual(scanSource('#12345 #1234567 #abcdefg #ab #abc12345678', { path: XML }), []);
});

test('line and column are 1-based', () => {
  const [finding] = scanSource('a\n  let c = Color.white', { path: SWIFT });
  assert.equal(finding.line, 2);
  assert.equal(finding.col, 11);
  assert.equal(finding.match, 'Color.white');
});

test('allow marker with reason is honoured under test dirs', () => {
  for (const [path, text] of [
    [SWIFT_TEST, 'let c = "#C2410C" // lint-hex: allow expected token value'],
    [KOTLIN_TEST, 'val c = 0xFFC2410C // lint-hex: allow expected token value'],
    [KOTLIN_TEST, '/* lint-hex: allow expected token value */ val c = Color.White'],
  ]) {
    assert.deepEqual(scanSource(text, { path }), [], path);
  }
  // only the marked line is skipped
  const text = 'let a = "#C2410C" // lint-hex: allow reason\nlet b = "#C2410C"';
  assert.deepEqual(scanSource(text, { path: SWIFT_TEST }).map((f) => f.line), [2]);
});

test('allow marker without a reason is a finding', () => {
  assert.deepEqual(rules('let c = 1 // lint-hex: allow', SWIFT_TEST), ['allow-without-reason']);
  assert.deepEqual(rules('let c = 1 // lint-hex: allow   ', SWIFT_TEST), ['allow-without-reason']);
  assert.deepEqual(rules('<!-- lint-hex: allow -->', 'android/app/src/test/res/a.xml'), ['allow-without-reason']);
  assert.deepEqual(rules('val c = 1 /* lint-hex: allow */', KOTLIN_TEST), ['allow-without-reason']);
});

test('allow marker in production source is a finding', () => {
  assert.deepEqual(rules('let c = 1 // lint-hex: allow some reason', SWIFT), ['allow-outside-tests']);
  // the marker does not shield colours in production code either
  assert.deepEqual(rules('let c = "#C2410C" // lint-hex: allow some reason', SWIFT).sort(), [
    'allow-outside-tests',
    'hex-literal',
  ]);
  assert.deepEqual(rules('<!-- lint-hex: allow x -->', XML), ['allow-outside-tests']);
});

test('generated token files are skipped', () => {
  const text = 'let c = "#C2410C"; val d = 0xFFC2410C; Color.White';
  assert.deepEqual(scanSource(text, { path: 'ios/Baupilot/DesignSystem/Tokens.swift' }), []);
  assert.deepEqual(scanSource(text, { path: 'android/app/src/main/java/de/baupilot/designsystem/Tokens.kt' }), []);
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'lint-hex-')));
  try {
    plant(root, 'ios/Baupilot/DesignSystem/Tokens.swift', text);
    plant(root, 'android/app/src/main/java/de/baupilot/designsystem/Tokens.kt', text);
    assert.deepEqual(run({ root }), { findings: [], exitCode: 0 });
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

function plant(root, rel, text) {
  const file = join(root, rel);
  mkdirSync(dirname(file), { recursive: true });
  writeFileSync(file, text);
}

test('build, .gradle, .idea, xcuserdata and *.xcodeproj directories are skipped', () => {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'lint-hex-')));
  try {
    const skipped = [
      'android/app/build/generated/Probe.kt',
      'android/.gradle/Probe.kt',
      'android/.idea/colors.xml',
      'ios/Baupilot.xcodeproj/xcuserdata/me.xcuserdatad/Probe.swift',
      'ios/Baupilot.xcodeproj/Probe.swift',
      'ios/Baupilot.xcworkspace/Probe.swift',
      'ios/xcuserdata/Probe.swift',
      'ios/DerivedData/Probe.swift',
      'ios/.swiftpm/Probe.swift',
      'ios/build/Probe.swift',
    ];
    for (const rel of skipped) plant(root, rel, 'let c = "#C2410C"');
    assert.deepEqual(run({ root }), { findings: [], exitCode: 0 });

    plant(root, 'ios/Baupilot/Probe.swift', 'let c = "#C2410C"');
    plant(root, 'android/app/src/main/res/values/colors.xml', '<color>#C2410C</color>');
    plant(root, 'android/app/Probe.kts', 'val c = 0xFFC2410C');
    plant(root, 'android/app/Probe.java', 'String c = "#C2410C";');
    plant(root, 'ios/Probe.md', '#C2410C');
    const result = run({ root });
    assert.equal(result.exitCode, 1);
    assert.deepEqual(
      result.findings.map((f) => f.path),
      ['android/app/Probe.kts', 'android/app/src/main/res/values/colors.xml', 'ios/Baupilot/Probe.swift'],
    );
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test('repo tree is clean', () => {
  assert.deepEqual(run({ root: REPO }).findings, []);
});
