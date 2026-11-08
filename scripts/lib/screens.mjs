// Parser for design/screens.md: the screen table and the interactive-parameter line.

const EXPECTED_ROWS = 34;

function cells(line) {
  return line
    .trim()
    .replace(/^\|/, '')
    .replace(/\|$/, '')
    .split('|')
    .map((c) => c.trim());
}

function base(path) {
  return path.slice(path.lastIndexOf('/') + 1);
}

export function parseScreens(markdown) {
  const rows = markdown.split('\n').filter((l) => /^\|\s*\d+\s*\|/.test(l));
  if (rows.length !== EXPECTED_ROWS) {
    throw new Error(`screens.md: expected ${EXPECTED_ROWS} table rows, found ${rows.length}`);
  }
  return rows.map((line) => {
    const [nr, title, ios, android, referenz, route, milestone] = cells(line);
    const name = /([A-Za-z0-9]+)_\{light,dark\}\.png/.exec(referenz)?.[1];
    const routeText = /`([^`]+)`/.exec(route)?.[1];
    if (!name || !routeText) {
      throw new Error(`screens.md: cannot parse row ${nr}`);
    }
    return {
      nr: Number(nr),
      name,
      title,
      iosFile: base(ios),
      androidFile: base(android),
      route: routeText.replace(/\s*\(.*\)$/, ''),
      milestone,
    };
  });
}

const num = (s) => Number(s.replace(',', '.'));

export function parseInteractiveParams(markdown) {
  const line = markdown.split('\n').find((l) => l.startsWith('Interaktive Parameter'));
  if (!line) {
    throw new Error('screens.md: no "Interaktive Parameter" line');
  }
  const re = /`(\w+)\.(\w+)`\s*\(\s*([\d.,]+)\s*(mm|%)\s*,\s*([\d.,]+)\s*–\s*([\d.,]+)\s*\)/g;
  const out = [];
  for (const m of line.matchAll(re)) {
    out.push({
      screen: m[1],
      key: m[2],
      default: num(m[3]),
      min: num(m[5]),
      max: num(m[6]),
      unit: m[4],
    });
  }
  return out;
}
