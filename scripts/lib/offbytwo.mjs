// Node replica of the Android OffByTwo differ: a pixel whose largest channel difference is
// at most 2 levels is ignored; otherwise all three channel differences are summed.
// Alpha is ignored (references are RGB8).

export function offByTwoPercent(refRGBA, actRGBA, w, h) {
  let delta = 0;
  for (let i = 0; i < w * h * 4; i += 4) {
    const dr = Math.abs(refRGBA[i] - actRGBA[i]);
    const dg = Math.abs(refRGBA[i + 1] - actRGBA[i + 1]);
    const db = Math.abs(refRGBA[i + 2] - actRGBA[i + 2]);
    if (Math.max(dr, dg, db) <= 2) continue;
    delta += dr + dg + db;
  }
  return (delta * 100) / (w * h * 3 * 256);
}
