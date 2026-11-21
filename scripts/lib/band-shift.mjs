// Band-shift method of M5/M6: per horizontal band, find the vertical offset of the actual
// image that best matches the reference, and report how much of the deviation is position.

function rowDeviation(refRGBA, actRGBA, w, yRef, yAct) {
  let sum = 0;
  const a = yRef * w * 4;
  const b = yAct * w * 4;
  for (let x = 0; x < w * 4; x += 4) {
    sum +=
      Math.abs(refRGBA[a + x] - actRGBA[b + x]) +
      Math.abs(refRGBA[a + x + 1] - actRGBA[b + x + 1]) +
      Math.abs(refRGBA[a + x + 2] - actRGBA[b + x + 2]);
  }
  return sum;
}

function bandDeviation(refRGBA, actRGBA, w, h, y0, y1, shift) {
  let sum = 0;
  let rows = 0;
  for (let y = y0; y < y1; y++) {
    const ya = y + shift;
    if (ya < 0 || ya >= h) continue;
    sum += rowDeviation(refRGBA, actRGBA, w, y, ya);
    rows++;
  }
  return rows === 0 ? 0 : sum / (rows * w * 3);
}

export function bandShift(refRGBA, actRGBA, w, h, { bandHeight, maxShift = 6 }) {
  const bands = [];
  let sumDev0 = 0;
  let sumGain = 0;
  let bandsImproved = 0;
  for (let y0 = 0; y0 < h; y0 += bandHeight) {
    const y1 = Math.min(y0 + bandHeight, h);
    const dev0 = bandDeviation(refRGBA, actRGBA, w, h, y0, y1, 0);
    let best = 0;
    let devBest = dev0;
    for (let s = -maxShift; s <= maxShift; s++) {
      const d = bandDeviation(refRGBA, actRGBA, w, h, y0, y1, s);
      if (d < devBest) {
        devBest = d;
        best = s;
      }
    }
    bands.push({ y0, best, dev0, devBest });
    sumDev0 += dev0;
    sumGain += dev0 - devBest;
    if (devBest <= 0.8 * dev0) bandsImproved++;
  }
  return { positionShare: sumDev0 === 0 ? 0 : sumGain / sumDev0, bandsImproved, bands };
}
