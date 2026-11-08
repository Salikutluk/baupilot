# M1 – Maßprüfung (AC2)

Abnahmekriterium AC2: Je Plattform mindestens drei Komponenten Maß für Maß gegen das Inline-CSS des Prototyps geprüft. Hier stehen vier je Plattform. Jede Zeile nennt den Prototypwert (`Datei:Zeile` in `design/prototype/`), die Konstante in der Implementierung und den Test, der das Maß prüft.

**Wie gemessen wird.** Die Maßtests rendern die Komponente in-process und lesen die Größe aus.

- iOS: `UIHostingController.sizeThatFits` (Helfer in `ios/BaupilotTests/DesignSystem/MetricHelpers.swift`), Toleranz 0,5 pt, wo nicht anders vermerkt.
- Android: Paparazzi mit No-op-Handler und `onGloballyPositioned`. Die Werte stehen in px, 1 dp = 3 px.

**Boxmodell.** Keine Prototyp-Seite setzt ein globales `box-sizing`. Die Maße unten sind die gerenderten Außenmaße: `<button>` ist per Chrome-Standard `border-box`, `div`/`span`/`a`/`label` nur mit `box-sizing: border-box` im Inline-Style. `Rahmen` und `Padding` sind in den Zeilen jeweils mitgezählt, wo sie die Höhe bestimmen.

**Textzeilenhöhe.** Der Prototyp setzt kein `line-height`, also gilt `normal`. Inter: 1,21 (12 pt → 14,52 pt, 11 pt → 13,31 pt). Roboto: 1,171875.

Die Abschnitte nennen nur Maße, die ein Test prüft, und Maße, die dieselbe Zeile im Prototyp festlegt. Eine Zeile mit „kein Maßtest“ wird nur über die Galerie-Goldens abgesichert.

## iOS

### StatusBadge – `ios/Baupilot/DesignSystem/Components/StatusBadge.swift`

Test: `ComponentMetricsStatusInputTests.testStatusBadgeHeights` (`ios/BaupilotTests/DesignSystem/ComponentMetricsStatusInputTests.swift`).

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Schrift | 12 / 600 (`IosAbnGewerke.dc.html:36`) | `Size.fontSize` = 12 (`.compact`, `.roomy`), Standardgewicht `.semibold` | indirekt über die Höhe |
| Padding und Radius, Standard | 4 × 8, r7 (`IosAbnGewerke.dc.html:36`, ebenso neun weitere Boards) | `Size.compact`: `vertical` 4, `horizontal` 8, `radius` 7 | Höhe 22,52 = 14,52 + 2 × 4 |
| Padding und Radius, großzügig | 5 × 9, r8 (`IosHome.dc.html:58`, der Wert aus M1.md) | `Size.roomy`: `vertical` 5, `horizontal` 9, `radius` 8 | Höhe 24,52 = 14,52 + 2 × 5 |
| Fälligkeit fett | 12 / 700 (`IosAbnMaengel.dc.html:33`) | Parameter `weight: .bold` | kein Maßtest |
| „Günstigstes“ | 11 / 700, 3 × 7, r6 (`IosGewerk.dc.html:70`) | `Size.mini`: `fontSize` 11, `vertical` 3, `horizontal` 7, `radius` 6 | Höhe 19,31 = 13,31 + 2 × 3 |

### SegmentedControl – `ios/Baupilot/DesignSystem/Components/SegmentedControl.swift`

Test: `ComponentMetricsStatusInputTests.testSegmentedControlHeights`. Die Höhe ist die des Containers: Segmenthöhe + 2 × 2 Padding.

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Container | Padding 2, r10, Hintergrund `chip` (`IosAbnGewerke.dc.html:24`) | `.padding(2)`, `RoundedRectangle(cornerRadius: 10)`, `Background.chip` | in der Höhe enthalten |
| Segment 34 (Standard) | `height: 34px` (`IosAbnGewerke.dc.html:25`) | `SegmentHeight.compact.points` = 34 | Gesamthöhe 38 |
| Segment 44 (Wert aus M1.md) | `height: 44px` (`IosAbnMaengel.dc.html:25`) | `SegmentHeight.regular.points` = 44 | Gesamthöhe 48 |
| Segment 36, Container `seg` | `height: 36px` (`IosAbnMangel.dc.html:57`), Container `seg` (`:55`) | `SegmentHeight.medium.points` = 36, `Background.seg` | Gesamthöhe 40 |
| Segment-Radius | 8 (`IosAbnGewerke.dc.html:25`) | `RoundedRectangle(cornerRadius: 8)` | kein Maßtest |
| Schatten aktiv | `0 1px 3px rgba(0,0,0,0.12)` (`IosAbnGewerke.dc.html:25`) | `.shadow(color: Tokens.segShadow.color, radius: 1.5, x: 0, y: 1)` (Regel siehe Schattenabbildung) | kein Maßtest |

### IosToggle – `ios/Baupilot/DesignSystem/Components/IosToggle.swift`

Test: `ComponentMetricsStatusInputTests.testToggleIs51x31`.

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Spur | 51 × 31, r16, Padding 2 (`IosProfil.dc.html:38`) | `.frame(width: 51, height: 31)`, `RoundedRectangle(cornerRadius: 16)`, Knopf mit `.padding(2)` | Breite 51, Höhe 31 |
| Knopf | 27 × 27, r14, `#FFFFFF` (`IosProfil.dc.html:39`) | `Circle()` mit `.frame(width: 27, height: 27)`, Farbe `Tokens.knob` | kein Maßtest |
| Knopfschatten | `0 2px 4px rgba(0,0,0,0.2)` (`IosProfil.dc.html:39`) | `.shadow(color: Tokens.knobShadow.color, radius: 2, x: 0, y: 2)` | kein Maßtest |

### TabBar – `ios/Baupilot/DesignSystem/Components/TabBar.swift`

Test: `ComponentMetricsBarTests.testTabBarIs84Tall` (`ios/BaupilotTests/DesignSystem/ComponentMetricsBarTests.swift`). Der Test prüft nur die Höhe 84 bei Breite 390, ohne Toleranz (exakter Vergleich).

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Höhe, border-box, inkl. 1 Rahmen oben | `height: 84px; box-sizing: border-box; border-top: 1px solid` (`IosHome.dc.html:81`) | `.frame(height: 84, alignment: .top)`, oben `Rectangle` mit `.frame(height: 1)` | Höhe 84 |
| Innenabstand | `padding: 8px 4px 0` (`IosHome.dc.html:81`) | `.padding(.top, 8)`, `.padding(.horizontal, 4)`, `.padding(.bottom, 0)` | in der Höhe enthalten |
| Spalten | `repeat(5, minmax(0, 1fr))` (`IosHome.dc.html:81`) | `HStack` mit `ForEach(Tab.allCases)`, je `.frame(maxWidth: .infinity)` | kein Maßtest |
| Eintrag | Spalte, gap 4, 10 / 600, `min-height: 44px` (`IosHome.dc.html:83`) | `VStack(spacing: 4)`, `.font(.inter(10, .semibold))`, `minHeight: 44` | kein Maßtest |
| Icon | 24 × 24 (`IosHome.dc.html:84`) | `Icon(tab.icon, size: 24, …)` | kein Maßtest |

## Android

### BaupilotNavigationBar – `android/app/src/main/java/de/baupilot/designsystem/components/NavigationBar.kt`

Test: `ComponentMetricsBarsTest.navigationBarIs80AndPillIs56x32` (`android/app/src/test/java/de/baupilot/designsystem/ComponentMetricsBarsTest.kt`).

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Höhe | `height: 80px; box-sizing: border-box` (`AndHome.dc.html:78`) | `Tokens.Layout.navigationBar` = 80.dp (aus `design/tokens.json` `layout.navigationBar`, generiert in `Tokens.kt`) | 240 px |
| Abstand oben | `padding-top: 12px` (`AndHome.dc.html:78`) | `.padding(top = 12.dp)` | in der Höhe enthalten |
| Pille | 56 × 32, r16 (`AndHome.dc.html:81`) | `Tokens.Layout.navPill` = `DpSize(56.dp, 32.dp)` (aus `layout.navPill`), `RoundedCornerShape(16.dp)` | 168 × 96 px |
| Abstand Pille–Label | gap 4 (`AndHome.dc.html:80`) | `Arrangement.spacedBy(4.dp)` | kein Maßtest |
| Icon, Label | 24 × 24 (`AndHome.dc.html:82`), 12 px (`:80`) | `Icon(…, size = 24.dp, …)`, `BaupilotText(size = 12.sp, …)` | kein Maßtest |

### BaupilotSwitch – `android/app/src/main/java/de/baupilot/designsystem/components/Switch.kt`

Test: `ComponentMetricsOtherTest.switchIs52x32AndThumbPositions` (`android/app/src/test/java/de/baupilot/designsystem/ComponentMetricsOtherTest.kt`).

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Spur | 52 × 32, `border-box`, r16, Rahmen 2, Padding `0 4px` (`AndProfil.dc.html:44`) | `.size(width = 52.dp, height = 32.dp)`, `RoundedCornerShape(16.dp)`, `.border(2.dp, …)` | 156 × 96 px, an und aus |
| Innenabstand | Rahmen 2 + Padding 4 = 6 je Seite | `.padding(horizontal = 6.dp)` (ein Compose-Rahmen nimmt keinen Platz ein) | Knopf aus: 6 dp = 18 px vom linken Rand |
| Knopf an | 24 × 24 (`AndProfil.dc.html:45`, `:75`), am Ende | `thumbSize` 24.dp, `Alignment.CenterEnd` | 72 × 72 px, 22 dp = 66 px vom linken Rand (52 − 6 − 24) |
| Knopf aus | 16 × 16 (`AndProfil.dc.html:75`), am Anfang | `thumbSize` 16.dp, `Alignment.CenterStart` | 48 × 48 px |

Der Standard-Switch von Material 3 liegt um 2 dp daneben (gemessen an `Profil_light.png`), deshalb die eigene Geometrie.

### ExtendedFab – `android/app/src/main/java/de/baupilot/designsystem/components/ExtendedFab.kt`

Test: `ComponentMetricsButtonsFabTest.extendedFabIs56Tall` (`android/app/src/test/java/de/baupilot/designsystem/ComponentMetricsButtonsFabTest.kt`). Der Test prüft nur die Höhe.

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Höhe | `height: 56px; box-sizing: border-box` (`AndHome.dc.html:73`) | `.height(56.dp)` | 168 px |
| Padding | `0 20px 0 16px` (`AndHome.dc.html:73`) | `PaddingValues(start = 16.dp, end = 20.dp)` | kein Maßtest |
| Radius | 16 (`AndHome.dc.html:73`) | `Tokens.Radius.fab` = 16.dp | kein Maßtest |
| Abstand Icon–Text | gap 10 (`AndHome.dc.html:73`) | `Arrangement.spacedBy(10.dp)` | kein Maßtest |
| Schrift | 15 / 500 (`AndHome.dc.html:73`) | `BaupilotText(size = 15.sp, weight = 500, …)` | kein Maßtest |
| Icon | 22 × 22, Strich 2,2 (`AndHome.dc.html:74`) | `Icon(icon.icon, 22.dp, …)`, `FabIcon.Plus` = `BaupilotIcon.PlusW22` | kein Maßtest |
| Schatten | `0 3px 8px rgba(0,0,0,0.18)` (`AndHome.dc.html:73`) | `cssShadow(offsetY = 3.dp, blur = 8.dp, color = Tokens.fabShadow, …)` | kein Maßtest |

### SearchBar – `android/app/src/main/java/de/baupilot/designsystem/components/SearchBar.kt`

Test: `ComponentMetricsOtherTest.searchBarIs56`. Der Test prüft nur die Höhe.

| Eigenschaft | Prototyp | Implementierung | Maßtest |
|---|---|---|---|
| Höhe | `height: 56px; box-sizing: border-box` (`AndDokumente.dc.html:19`) | `.height(56.dp)` | 168 px |
| Padding, Radius, Abstand | `padding: 0 16px; border-radius: 28px; gap: 16px` (`AndDokumente.dc.html:19`) | `.padding(horizontal = 16.dp)`, `Tokens.Radius.search` = 28.dp, `Arrangement.spacedBy(16.dp)` | kein Maßtest |
| Icon | 24 × 24 (`AndDokumente.dc.html:20`) | `Icon(BaupilotIcon.Search, 24.dp, …)` | kein Maßtest |
| Eingabe | 16 px (`AndDokumente.dc.html:21`) | `TextStyle(fontSize = 16.sp, …)`, Platzhalter `BaupilotText(size = 16.sp, …)` | kein Maßtest |
| Zähler rechts | 13 px (`AndDokumente.dc.html:22`) | `BaupilotText(size = 13.sp, …)` für `trailingText` | kein Maßtest |

## Rahmen im Prototyp (gemessen)

**Chrome rendert `border: 1.5px` als 1 pt.** Gemessen an den Referenzbildern (Faktor 3, Spalten und Zeilen als Pixelläufe):

| Element | Prototyp | Messung | Umsetzung |
|---|---|---|---|
| „Mangel“-Knopf | `<a>` `height: 50px; border: 1.5px solid` (`IosAbnPruefung.dc.html:55`), content-box | `AbnPruefung_light.png` x = 190: Rahmen y 1548–1551 und 1701–1704 (je 3 px = 1 pt), Gesamthöhe 156 px = 52 pt | `DefectButton`: 50 pt Inhalt plus 1 pt Rahmen außen = 52 |
| „Foto seitlich“-Kachel | `<button>` `height: 84px; border: 1.5px dashed` (`IosAbnPruefung.dc.html:49`) | Spalte x = 880: Rahmenlauf 3 px = 1 pt; Zeile y = 1255: Strich 9–10 px, Lücke 5–6 px, Periode 15 px | `PhotoTile.add`, `PhotoButton`: Breite 1, Strichmuster `[3, 2]` |
| Überfällige Karte | `<article>` `border: 1.5px solid err` (`IosAbnMaengel.dc.html:73`) | `AbnMaengel_light.png` Zeile y = 700: Rahmenlauf x 60–62 = 3 px = 1 pt | `Card.Border.overdue`: Breite 1, außerhalb des Paddings |
| Aktuelle Schrittkarte | `<div>` `border: 2px solid accent; padding: 16px` (`IosAbnPruefung.dc.html:43`) | `AbnPruefung_light.png` Zeile y = 900: Lauf x 60–65 = 6 px = 2 pt | `Card.Border.current`: Breite 2, außerhalb des Paddings |

Der Rahmen liegt bei diesen Elementen außerhalb des Paddings: der Inhalt sitzt um die Rahmenbreite weiter innen, die Box wächst um 2 × Breite. Test: `ComponentMetricsStepTests.testCardBorderAddsToThePaddedBox`, `ComponentMetricsButtonTests.testDefectButtonIs52TallWithOuterBorder`.

## Schattenabbildung (D12)

CSS `box-shadow: 0 y B c` ist eine Gauß-Unschärfe mit σ = B / 2.

- **iOS:** `.shadow(color:radius:x:y:)` bekommt `radius = B / 2`. Beispiele: Segment `0 1px 3px` → Radius 1,5; Knopf `0 2px 4px` → Radius 2.
- **Android:** `Modifier.cssShadow(offsetY, blur, color, shape)` in `CssShadow.kt` zeichnet hinter den Inhalt mit `Paint.setShadowLayer`. Der Skia-Radius ist `(σ − 0.5) / 0.57735` für σ > 0,5, sonst σ unverändert. FAB: `0 3px 8px` → σ = 4 dp = 12 px, Skia-Radius (12 − 0,5) / 0,57735 = 19,92 px (die Rechnung läuft in px).

Die Abbildung ist hergeleitet, nicht gemessen. Sie ist nur über die Galerie-Goldens festgenagelt. Bekannte Lücke: Auf API 26–27 zeichnen Hardware-Layer keinen Schatten-Layer auf Formen, der FAB zeigt dort keinen Schatten (siehe `docs/m1-offene-punkte.md`).

## Platzhalterfarbe (D5)

Der Prototyp setzt in den Suchfeldern keine Platzhalterfarbe (`IosDokumente.dc.html:31`, `AndDokumente.dc.html:21`). Die Referenzbilder zeigen deshalb den Chrome-Standardwert. Er ist in `scripts/tokens-supplement.json` als Token `placeholder` für beide Plattformen eingetragen, hell und dunkel `#757575`.

**Messung.** In jedem der vier Referenzbilder wurde der Text des Platzhalters ausgeschnitten (iOS „Firma, Gewerk oder Betrag suchen“, x 175–975, y 425–495; Android „Dokumente durchsuchen“, x 215–760, y 170–245) und dort das Pixel mit dem größten Helligkeitsabstand zum Hintergrund gesucht. Hell ist das das dunkelste, dunkel das hellste Pixel. Der Wert kam in allen vier Bildern tausendfach vor (Spalte „Pixel mit diesem Wert“); die Nachbarwerte `#747474`, `#767676` und `#777777` stammen aus der Kantenglättung.

| Plattform | Bild | Hintergrund (RGB) | Gemessenes Grau | Pixel mit diesem Wert | Ergänzungswert | Stimmt überein |
|---|---|---|---|---|---|---|
| iOS | `design/reference/ios/Dokumente_light.png` | 236, 234, 229 | `#757575` | 5795 | `#757575` | ja |
| iOS | `design/reference/ios/Dokumente_dark.png` | 38, 42, 47 | `#757575` | 5775 | `#757575` | ja |
| Android | `design/reference/android/Dokumente_light.png` | 241, 230, 223 | `#757575` | 3946 | `#757575` | ja |
| Android | `design/reference/android/Dokumente_dark.png` | 46, 41, 37 | `#757575` | 3922 | `#757575` | ja |

Alle vier Messwerte entsprechen dem Ergänzungswert. Es musste kein Wert in `scripts/tokens-supplement.json` geändert werden.

**Nachmessen.** Die PNGs sind 8 Bit RGB, nicht interlaced. Ein Entpacker aus der Python-Standardbibliothek (`zlib`, PNG-Filter 0–4 zurückrechnen) reicht, Aufruf mit `python3 -I`:

```python
import zlib, struct
from collections import Counter

def decode(path):
    d = open(path, 'rb').read()
    p, idat = 8, b''
    while p < len(d):
        n, = struct.unpack('>I', d[p:p + 4]); t = d[p + 4:p + 8]; c = d[p + 8:p + 8 + n]; p += 12 + n
        if t == b'IHDR': w, h = struct.unpack('>II', c[:8])
        elif t == b'IDAT': idat += c
    raw, bpp, stride = zlib.decompress(idat), 3, w * 3
    rows, prev, pos = [], bytearray(stride), 0
    for _ in range(h):
        f, line = raw[pos], bytearray(raw[pos + 1:pos + 1 + stride]); pos += 1 + stride
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0; b = prev[i]; c = prev[i - bpp] if i >= bpp else 0
            if f == 1: line[i] = (line[i] + a) & 255
            elif f == 2: line[i] = (line[i] + b) & 255
            elif f == 3: line[i] = (line[i] + ((a + b) >> 1)) & 255
            elif f == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else (b if pb <= pc else c))) & 255
        rows.append(line); prev = line
    return rows

lum = lambda p: 0.2126 * p[0] + 0.7152 * p[1] + 0.0722 * p[2]
rows = decode('design/reference/ios/Dokumente_light.png')
px = [tuple(rows[y][x * 3:x * 3 + 3]) for y in range(425, 495) for x in range(175, 975)]
bg = Counter(px).most_common(1)[0][0]
print('#%02X%02X%02X' % max(px, key=lambda p: abs(lum(p) - lum(bg))))
```
