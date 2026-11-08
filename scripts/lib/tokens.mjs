// Pure token validation and Swift/Kotlin code generation for design/tokens.json.

const PLATFORMS = ['ios', 'android'];
const HEX_RE = /^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$/;
const RESERVED_VARIANT_MEMBERS = ['core', 'light', 'dark', 'color', 'resolve'];

const SWIFT_KEYWORDS = new Set([
  'open', 'in', 'is', 'as', 'default', 'case', 'class', 'func', 'let', 'var', 'return', 'true', 'false', 'nil',
  'self', 'Self', 'Type', 'init', 'operator', 'protocol', 'extension', 'import', 'struct', 'enum', 'static',
  'where', 'while', 'for', 'if', 'else', 'switch', 'do', 'try', 'throw', 'repeat', 'break', 'continue', 'guard',
  'defer', 'some', 'any',
]);

const KOTLIN_KEYWORDS = new Set([
  'as', 'break', 'class', 'continue', 'do', 'else', 'false', 'for', 'fun', 'if', 'in', 'interface', 'is', 'null',
  'object', 'package', 'return', 'super', 'this', 'throw', 'true', 'try', 'typealias', 'typeof', 'val', 'var',
  'when', 'while',
]);

export class TokenError extends Error {
  constructor(code, path, message) {
    super(message);
    this.name = 'TokenError';
    this.code = code;
    this.path = path;
  }
}

export function lowerCamel(name) {
  const camel = name
    .split(/[-_]/)
    .filter((p) => p.length > 0)
    .map((p, i) => (i === 0 ? p : p[0].toUpperCase() + p.slice(1)))
    .join('');
  return camel.length > 0 ? camel[0].toLowerCase() + camel.slice(1) : camel;
}

const isNumber = (v) => typeof v === 'number' && Number.isFinite(v);
const isPlainObject = (v) => typeof v === 'object' && v !== null && !Array.isArray(v);

function layoutKind(value) {
  if (isNumber(value)) return 'number';
  if (Array.isArray(value) && value.length === 2 && value.every(isNumber)) return 'pair';
  if (Array.isArray(value) && value.length === 4 && value.every(isNumber)) return 'padding';
  if (isPlainObject(value)) {
    const keys = Object.keys(value);
    if (keys.length === 2 && keys.includes('width') && keys.includes('height') && isNumber(value.width) && isNumber(value.height)) {
      return 'size';
    }
  }
  return null;
}

function checkHex(value, path) {
  if (typeof value !== 'string' || !HEX_RE.test(value)) {
    throw new TokenError(
      'invalid-hex',
      path,
      `gen-tokens: invalid color at ${path}: ${JSON.stringify(value)} (expected #RRGGBB or #RRGGBBAA)`,
    );
  }
}

function checkPair(pair, path) {
  if (!isPlainObject(pair)) {
    throw new TokenError('incomplete-pair', path, `gen-tokens: incomplete color pair at ${path}: expected {light, dark}`);
  }
  for (const side of ['light', 'dark']) {
    if (!(side in pair)) {
      throw new TokenError('incomplete-pair', `${path}.${side}`, `gen-tokens: incomplete color pair at ${path}: missing "${side}"`);
    }
  }
  checkHex(pair.light, `${path}.light`);
  checkHex(pair.dark, `${path}.dark`);
}

function checkCollisions(keys, basePath, reserved = []) {
  const seen = new Map();
  for (const key of keys) {
    const id = lowerCamel(key);
    const path = `${basePath}.${key}`;
    if (reserved.includes(id)) {
      throw new TokenError('name-collision', path, `gen-tokens: "${key}" at ${path} collides with reserved member "${id}"`);
    }
    if (seen.has(id)) {
      throw new TokenError('name-collision', path, `gen-tokens: "${key}" and "${seen.get(id)}" both become "${id}" at ${basePath}`);
    }
    seen.set(id, key);
  }
}

export function validateTokens(json, screenNames) {
  for (const platform of PLATFORMS) {
    const section = json?.[platform];
    if (!section) continue;
    const core = section.colors?.core ?? {};
    const perScreen = section.colors?.perScreen ?? {};
    const corePath = `${platform}.colors.core`;
    const perScreenPath = `${platform}.colors.perScreen`;

    checkCollisions(Object.keys(core), corePath);
    for (const [name, pair] of Object.entries(core)) checkPair(pair, `${corePath}.${name}`);

    checkCollisions(Object.keys(perScreen), perScreenPath, RESERVED_VARIANT_MEMBERS);
    for (const [screen, overrides] of Object.entries(perScreen)) {
      const screenPath = `${perScreenPath}.${screen}`;
      if (!screenNames.includes(screen)) {
        throw new TokenError('unknown-screen', screenPath, `gen-tokens: unknown screen at ${screenPath} (not in design/screens.md)`);
      }
      for (const [token, pair] of Object.entries(overrides)) {
        const tokenPath = `${screenPath}.${token}`;
        if (!(token in core)) {
          throw new TokenError('unknown-token', tokenPath, `gen-tokens: unknown token at ${tokenPath} (not in ${corePath})`);
        }
        checkPair(pair, tokenPath);
      }
    }

    const radius = section.radius ?? {};
    checkCollisions(Object.keys(radius), `${platform}.radius`);
    for (const [key, value] of Object.entries(radius)) {
      if (!isNumber(value)) {
        throw new TokenError('unsupported-layout', `${platform}.radius.${key}`, `gen-tokens: unsupported radius at ${platform}.radius.${key}: ${JSON.stringify(value)} (expected a number)`);
      }
    }

    const layout = section.layout ?? {};
    checkCollisions(Object.keys(layout), `${platform}.layout`);
    for (const [key, value] of Object.entries(layout)) {
      if (layoutKind(value) === null) {
        throw new TokenError(
          'unsupported-layout',
          `${platform}.layout.${key}`,
          `gen-tokens: unsupported layout value at ${platform}.layout.${key}: ${JSON.stringify(value)} (expected number, [w,h], {width,height} or 4-element padding array)`,
        );
      }
    }
  }
}

export function validateSupplement(supplement, tokens) {
  for (const platform of PLATFORMS) {
    const colors = supplement?.[platform]?.colors ?? {};
    const basePath = `supplement.${platform}.colors`;
    for (const [name, entry] of Object.entries(colors)) {
      const path = `${basePath}.${name}`;
      if (typeof entry?.source !== 'string' || entry.source.trim() === '') {
        throw new TokenError('missing-source', path, `gen-tokens: supplement color at ${path} needs a non-empty "source"`);
      }
      checkPair(entry, path);
    }
    const coreKeys = Object.keys(tokens?.[platform]?.colors?.core ?? {});
    checkCollisions([...coreKeys, ...Object.keys(colors)], basePath);
  }
}

export function mergeSupplement(tokens, supplement) {
  const merged = structuredClone(tokens);
  for (const platform of PLATFORMS) {
    const colors = supplement?.[platform]?.colors;
    if (!colors || !merged[platform]) continue;
    const core = merged[platform].colors.core;
    for (const name of Object.keys(colors).sort()) {
      core[name] = { light: colors[name].light, dark: colors[name].dark };
    }
  }
  return merged;
}

// token name -> [{ screen, pair }] in perScreen key order
function variantMap(perScreen) {
  const map = new Map();
  for (const [screen, overrides] of Object.entries(perScreen)) {
    for (const [token, pair] of Object.entries(overrides)) {
      if (!map.has(token)) map.set(token, []);
      map.get(token).push({ screen, pair });
    }
  }
  return map;
}

const hexParts = (hex) => {
  const h = hex.slice(1).toUpperCase();
  return { rgb: h.slice(0, 6), alpha: h.length === 8 ? h.slice(6) : 'FF' };
};
const swiftHex = (hex) => {
  const { rgb, alpha } = hexParts(hex);
  return `0x${rgb}${alpha}`;
};
const kotlinColor = (hex) => {
  const { rgb, alpha } = hexParts(hex);
  return `Color(0x${alpha}${rgb})`;
};

const swiftName = (name) => {
  const id = lowerCamel(name);
  return SWIFT_KEYWORDS.has(id) ? `\`${id}\`` : id;
};
const kotlinName = (name) => {
  const id = lowerCamel(name);
  return KOTLIN_KEYWORDS.has(id) ? `\`${id}\`` : id;
};
const upperCamel = (name) => {
  const id = lowerCamel(name);
  return id[0].toUpperCase() + id.slice(1);
};

const header = (supplement) =>
  supplement
    ? 'GENERATED by scripts/gen-tokens from design/tokens.json + scripts/tokens-supplement.json — do not edit.'
    : 'GENERATED by scripts/gen-tokens from design/tokens.json — do not edit.';

function swiftColorLiteral(pair) {
  return `ColorToken(light: ${swiftHex(pair.light)}, dark: ${swiftHex(pair.dark)})`;
}

function swiftLayoutLine(name, value) {
  const id = swiftName(name);
  switch (layoutKind(value)) {
    case 'number':
      return `static let ${id}: CGFloat = ${value}`;
    case 'pair':
      return `static let ${id} = CGSize(width: ${value[0]}, height: ${value[1]})`;
    case 'size':
      return `static let ${id} = CGSize(width: ${value.width}, height: ${value.height})`;
    default: {
      const [top, right, bottom, left] = value;
      return `static let ${id} = EdgeInsets(top: ${top}, leading: ${left}, bottom: ${bottom}, trailing: ${right})`;
    }
  }
}

export function generateSwift(json, { supplement = false } = {}) {
  const ios = json.ios;
  const core = ios.colors.core;
  const variants = variantMap(ios.colors.perScreen ?? {});
  const out = [];
  out.push(`// ${header(supplement)}`);
  out.push('');
  out.push('import SwiftUI');
  out.push('import UIKit');
  out.push('');
  out.push('/// A light/dark colour pair; each value is RGBA packed as 0xRRGGBBAA.');
  out.push('public struct ColorToken: Sendable, Equatable {');
  out.push('    public let light: UInt32');
  out.push('    public let dark: UInt32');
  out.push('');
  out.push('    public init(light: UInt32, dark: UInt32) {');
  out.push('        self.light = light');
  out.push('        self.dark = dark');
  out.push('    }');
  out.push('');
  out.push('    private static func uiColor(_ rgba: UInt32) -> UIColor {');
  out.push('        UIColor(');
  out.push('            red: CGFloat((rgba >> 24) & 0xFF) / 255,');
  out.push('            green: CGFloat((rgba >> 16) & 0xFF) / 255,');
  out.push('            blue: CGFloat((rgba >> 8) & 0xFF) / 255,');
  out.push('            alpha: CGFloat(rgba & 0xFF) / 255');
  out.push('        )');
  out.push('    }');
  out.push('');
  out.push('    public func color(_ scheme: ColorScheme) -> Color {');
  out.push('        let rgba = scheme == .dark ? dark : light');
  out.push('        return Color(');
  out.push('            .sRGB,');
  out.push('            red: Double((rgba >> 24) & 0xFF) / 255,');
  out.push('            green: Double((rgba >> 16) & 0xFF) / 255,');
  out.push('            blue: Double((rgba >> 8) & 0xFF) / 255,');
  out.push('            opacity: Double(rgba & 0xFF) / 255');
  out.push('        )');
  out.push('    }');
  out.push('');
  out.push('    public var color: Color {');
  out.push('        let lightValue = light');
  out.push('        let darkValue = dark');
  out.push('        return Color(UIColor { traits in');
  out.push('            ColorToken.uiColor(traits.userInterfaceStyle == .dark ? darkValue : lightValue)');
  out.push('        })');
  out.push('    }');
  out.push('}');

  for (const [token, entries] of variants) {
    out.push('');
    out.push('@dynamicMemberLookup');
    out.push(`public struct ${upperCamel(token)}Variants: Sendable {`);
    out.push('    public let core: ColorToken');
    for (const { screen } of entries) out.push(`    public let ${swiftName(screen)}: ColorToken`);
    out.push('');
    out.push('    public init() {');
    out.push(`        core = ${swiftColorLiteral(core[token])}`);
    for (const { screen, pair } of entries) out.push(`        ${swiftName(screen)} = ${swiftColorLiteral(pair)}`);
    out.push('    }');
    out.push('');
    out.push('    public subscript<T>(dynamicMember keyPath: KeyPath<ColorToken, T>) -> T {');
    out.push('        core[keyPath: keyPath]');
    out.push('    }');
    out.push('}');
  }

  out.push('');
  out.push('public enum Tokens {');
  for (const [name, pair] of Object.entries(core)) {
    out.push(
      variants.has(name)
        ? `    public static let ${swiftName(name)} = ${upperCamel(name)}Variants()`
        : `    public static let ${swiftName(name)} = ${swiftColorLiteral(pair)}`,
    );
  }
  out.push('');
  out.push('    public enum FontFamily {');
  out.push(`        public static let name = ${JSON.stringify(ios.font.family)}`);
  out.push(`        public static let weights: [Int] = [${ios.font.weights.join(', ')}]`);
  out.push('    }');
  out.push('');
  out.push('    public enum Radius {');
  for (const [key, value] of Object.entries(ios.radius ?? {})) {
    out.push(`        public static let ${swiftName(key)}: CGFloat = ${value}`);
  }
  out.push('    }');
  out.push('');
  out.push('    public enum Layout {');
  for (const [key, value] of Object.entries(ios.layout ?? {})) {
    out.push(`        public ${swiftLayoutLine(key, value)}`);
  }
  out.push('    }');
  out.push('}');
  out.push('');
  return out.join('\n');
}

function kotlinColorArgs(pair) {
  return `${kotlinColor(pair.light)}, ${kotlinColor(pair.dark)}`;
}

function kotlinLayoutLine(name, value) {
  const id = kotlinName(name);
  switch (layoutKind(value)) {
    case 'number':
      return `val ${id} = ${value}.dp`;
    case 'pair':
      return `val ${id} = DpSize(${value[0]}.dp, ${value[1]}.dp)`;
    case 'size':
      return `val ${id} = DpSize(${value.width}.dp, ${value.height}.dp)`;
    default: {
      const [top, right, bottom, left] = value;
      return `val ${id} = PaddingValues(start = ${left}.dp, top = ${top}.dp, end = ${right}.dp, bottom = ${bottom}.dp)`;
    }
  }
}

export function generateKotlin(json, { supplement = false } = {}) {
  const android = json.android;
  const core = android.colors.core;
  const variants = variantMap(android.colors.perScreen ?? {});
  const out = [];
  out.push(`// ${header(supplement)}`);
  out.push('');
  out.push('package de.baupilot.designsystem');
  out.push('');
  out.push('import androidx.compose.foundation.isSystemInDarkTheme');
  out.push('import androidx.compose.foundation.layout.PaddingValues');
  out.push('import androidx.compose.runtime.Composable');
  out.push('import androidx.compose.runtime.ReadOnlyComposable');
  out.push('import androidx.compose.ui.graphics.Color');
  out.push('import androidx.compose.ui.unit.DpSize');
  out.push('import androidx.compose.ui.unit.dp');
  out.push('');
  out.push('/** A light/dark colour pair; each Color is ARGB (alpha first). */');
  out.push('open class ColorToken(val light: Color, val dark: Color) {');
  out.push('    fun resolve(dark: Boolean): Color = if (dark) this.dark else light');
  out.push('');
  out.push('    @Composable');
  out.push('    @ReadOnlyComposable');
  out.push('    fun color(): Color = resolve(isSystemInDarkTheme())');
  out.push('}');
  out.push('');
  out.push('object Tokens {');
  for (const [name, pair] of Object.entries(core)) {
    const id = kotlinName(name);
    const entries = variants.get(name);
    if (!entries) {
      out.push(`    val ${id} = ColorToken(${kotlinColorArgs(pair)})`);
      continue;
    }
    out.push(`    object ${id} : ColorToken(${kotlinColorArgs(pair)}) {`);
    for (const { screen, pair: variant } of entries) {
      out.push(`        val ${kotlinName(screen)} = ColorToken(${kotlinColorArgs(variant)})`);
    }
    out.push('    }');
  }
  out.push('');
  out.push('    object FontFamilyName {');
  out.push(`        const val NAME = ${JSON.stringify(android.font.family)}`);
  out.push(`        val WEIGHTS = listOf(${android.font.weights.join(', ')})`);
  out.push('    }');
  out.push('');
  out.push('    object Radius {');
  for (const [key, value] of Object.entries(android.radius ?? {})) {
    out.push(`        val ${kotlinName(key)} = ${value}.dp`);
  }
  out.push('    }');
  out.push('');
  out.push('    object Layout {');
  for (const [key, value] of Object.entries(android.layout ?? {})) {
    out.push(`        ${kotlinLayoutLine(key, value)}`);
  }
  out.push('    }');
  out.push('}');
  out.push('');
  return out.join('\n');
}
