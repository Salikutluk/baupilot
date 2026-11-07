# Screen-Inventar Baupilot

34 Screens je Plattform, je hell + dunkel = 136 Referenzbilder (1170 × 2532 px, 390 × 844 @3x).

| Nr. | Screen | iOS-Quelle | Android-Quelle | Referenz | Route | Milestone |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | Login | prototype/Main.dc.html | prototype/AndLogin.dc.html | reference/{ios,android}/Login_{light,dark}.png | `login` | M2 |
| 2 | Übersicht | prototype/IosHome.dc.html | prototype/AndHome.dc.html | reference/{ios,android}/Home_{light,dark}.png | `home (Tab 1)` | M2 |
| 3 | Zeitplan | prototype/IosZeitplan.dc.html | prototype/AndZeitplan.dc.html | reference/{ios,android}/Zeitplan_{light,dark}.png | `schedule (Tab 2)` | M2 |
| 4 | Profil & Einstellungen | prototype/IosProfil.dc.html | prototype/AndProfil.dc.html | reference/{ios,android}/Profil_{light,dark}.png | `profile (Tab 5)` | M2 |
| 5 | Gewerk-Detail Estrich | prototype/IosGewerk.dc.html | prototype/AndGewerk.dc.html | reference/{ios,android}/Gewerk_{light,dark}.png | `trade/{id}` | M3 |
| 6 | Dokumente | prototype/IosDokumente.dc.html | prototype/AndDokumente.dc.html | reference/{ios,android}/Dokumente_{light,dark}.png | `documents (Tab 4)` | M3 |
| 7 | Dokument ansehen | prototype/IosDokViewer.dc.html | prototype/AndDokViewer.dc.html | reference/{ios,android}/DokViewer_{light,dark}.png | `document/{id}` | M3 |
| 8 | Angebote vergleichen | prototype/IosVergleich.dc.html | prototype/AndVergleich.dc.html | reference/{ios,android}/Vergleich_{light,dark}.png | `trade/{id}/compare` | M3 |
| 9 | Mitteilungen | prototype/IosMitteilungen.dc.html | prototype/AndMitteilungen.dc.html | reference/{ios,android}/Mitteilungen_{light,dark}.png | `notifications` | M3 |
| 10 | Abnahmen je Gewerk | prototype/IosAbnGewerke.dc.html | prototype/AndAbnGewerke.dc.html | reference/{ios,android}/AbnGewerke_{light,dark}.png | `inspections (Tab 3)` | M4 |
| 11 | Fenster – Elemente | prototype/IosAbnElemente.dc.html | prototype/AndAbnElemente.dc.html | reference/{ios,android}/AbnElemente_{light,dark}.png | `inspection/windows` | M4 |
| 12 | Prüfschritt F8 | prototype/IosAbnPruefung.dc.html | prototype/AndAbnPruefung.dc.html | reference/{ios,android}/AbnPruefung_{light,dark}.png | `inspection/windows/{el}/step/{n}` | M4 |
| 13 | Mangel erfassen | prototype/IosAbnMangel.dc.html | prototype/AndAbnMangel.dc.html | reference/{ios,android}/AbnMangel_{light,dark}.png | `defect/new` | M4 |
| 14 | Abnahme & Unterschrift | prototype/IosAbnSignatur.dc.html | prototype/AndAbnSignatur.dc.html | reference/{ios,android}/AbnSignatur_{light,dark}.png | `inspection/{id}/sign` | M4 |
| 15 | Offene Mängel & Fristen | prototype/IosAbnMaengel.dc.html | prototype/AndAbnMaengel.dc.html | reference/{ios,android}/AbnMaengel_{light,dark}.png | `defects` | M4 |
| 16 | Erdarbeiten – Protokoll | prototype/IosErd.dc.html | prototype/AndErd.dc.html | reference/{ios,android}/Erd_{light,dark}.png | `inspection/earthworks` | M5 |
| 17 | Rohbau – Übersicht | prototype/IosRohbau.dc.html | prototype/AndRohbau.dc.html | reference/{ios,android}/Rohbau_{light,dark}.png | `inspection/shell` | M5 |
| 18 | Rohbau – Maße & Lot | prototype/IosRohbauMasse.dc.html | prototype/AndRohbauMasse.dc.html | reference/{ios,android}/RohbauMasse_{light,dark}.png | `inspection/shell/measure` | M5 |
| 19 | Zimmerer – Dachstuhl | prototype/IosZimmerer.dc.html | prototype/AndZimmerer.dc.html | reference/{ios,android}/Zimmerer_{light,dark}.png | `inspection/carpentry` | M5 |
| 20 | Elektro – Übersicht | prototype/IosElUebersicht.dc.html | prototype/AndElUebersicht.dc.html | reference/{ios,android}/ElUebersicht_{light,dark}.png | `inspection/electrical` | M5 |
| 21 | Raumprüfung Küche | prototype/IosElRaum.dc.html | prototype/AndElRaum.dc.html | reference/{ios,android}/ElRaum_{light,dark}.png | `inspection/electrical/room/{id}` | M5 |
| 22 | Zählerschrank & FI-Test | prototype/IosElVerteiler.dc.html | prototype/AndElVerteiler.dc.html | reference/{ios,android}/ElVerteiler_{light,dark}.png | `inspection/electrical/panel` | M5 |
| 23 | Rauchwarnmelder | prototype/IosElRauchmelder.dc.html | prototype/AndElRauchmelder.dc.html | reference/{ios,android}/ElRauchmelder_{light,dark}.png | `inspection/electrical/smoke` | M5 |
| 24 | Sanitär & Heizung – Übersicht | prototype/IosSanUebersicht.dc.html | prototype/AndSanUebersicht.dc.html | reference/{ios,android}/SanUebersicht_{light,dark}.png | `inspection/plumbing` | M6 |
| 25 | Gefälle Abwasser | prototype/IosSanGefaelle.dc.html | prototype/AndSanGefaelle.dc.html | reference/{ios,android}/SanGefaelle_{light,dark}.png | `inspection/plumbing/slope/{sec}` | M6 |
| 26 | Druckprobe Trinkwasser | prototype/IosSanDruck.dc.html | prototype/AndSanDruck.dc.html | reference/{ios,android}/SanDruck_{light,dark}.png | `inspection/plumbing/pressure` | M6 |
| 27 | Bad OG – Objekte | prototype/IosSanBad.dc.html | prototype/AndSanBad.dc.html | reference/{ios,android}/SanBad_{light,dark}.png | `inspection/plumbing/room/{id}` | M6 |
| 28 | Estrich – Übersicht | prototype/IosEsUebersicht.dc.html | prototype/AndEsUebersicht.dc.html | reference/{ios,android}/EsUebersicht_{light,dark}.png | `inspection/screed` | M6 |
| 29 | Ebenheit messen | prototype/IosEsEbenheit.dc.html | prototype/AndEsEbenheit.dc.html | reference/{ios,android}/EsEbenheit_{light,dark}.png | `inspection/screed/flatness` | M6 |
| 30 | Aufheizen & Belegreife | prototype/IosEsBelegreife.dc.html | prototype/AndEsBelegreife.dc.html | reference/{ios,android}/EsBelegreife_{light,dark}.png | `inspection/screed/readiness` | M6 |
| 31 | Fliesen – Übersicht | prototype/IosFlUebersicht.dc.html | prototype/AndFlUebersicht.dc.html | reference/{ios,android}/FlUebersicht_{light,dark}.png | `inspection/tiles` | M6 |
| 32 | Raumprüfung Bad | prototype/IosFlBad.dc.html | prototype/AndFlBad.dc.html | reference/{ios,android}/FlBad_{light,dark}.png | `inspection/tiles/room/{id}` | M6 |
| 33 | Maler – Übersicht | prototype/IosMaUebersicht.dc.html | prototype/AndMaUebersicht.dc.html | reference/{ios,android}/MaUebersicht_{light,dark}.png | `inspection/painting` | M6 |
| 34 | Raumprüfung Wohnen | prototype/IosMaRaum.dc.html | prototype/AndMaRaum.dc.html | reference/{ios,android}/MaRaum_{light,dark}.png | `inspection/painting/room/{id}` | M6 |

Dunkel-Varianten (`*Dark.dc.html`) sind Hüllen: derselbe Screen mit `dark = true`.

Interaktive Parameter im Prototyp: `RohbauMasse.toleranz` (12 mm, 1–30), `EsEbenheit.grenzwert` (4 mm, 1–15), `SanGefaelle.soll` (1,0 %, 0,5–3).
