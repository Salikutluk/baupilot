# Baupilot

Native iOS (SwiftUI) and Android (Jetpack Compose) app, built pixel-exact from the frozen design package. Milestone 0 (foundation) provides the token generator, the design freeze, the fixture, the platform skeletons with fonts and data layer, and the snapshot harness. Milestone plan: [`docs/milestones/M0.md`](docs/milestones/M0.md). Open items: [`docs/m0-offene-punkte.md`](docs/m0-offene-punkte.md).

## Layout

```
design/                 frozen design package (tag design-v1; never edited)
docs/                   implementierungsplan.md, milestones/M0.md … M8.md, m0-offene-punkte.md
fixtures/baupilot.json  the only data source until M7 (read through MockRepository)
scripts/                Node ESM, zero npm dependencies
  gen-tokens            sh wrapper → design/tokens.json to Tokens.swift / Tokens.kt
  diff-report           sh wrapper → PNG diff reports
  extract-render-vals   sh wrapper → renderVals() extraction from the prototype
  design-freeze         sh wrapper → checks design/ against design-v1.sha256
  lib/                  shared modules
  test/                 node --test suites
ios/                    XcodeGen project.yml, Baupilot/ (app), BaupilotTests/
android/                Gradle KTS, app/, third_party/roboto/
.github/workflows/ci.yml
```

## Prerequisites

- Xcode 26.3 and XcodeGen: `brew install xcodegen`
- An iPhone 15 simulator on iOS 26.2 (one-time):
  `xcrun simctl create "iPhone 15" com.apple.CoreSimulator.SimDeviceType.iPhone-15 com.apple.CoreSimulator.SimRuntime.iOS-26-2`
- JDK 21. The local default JDK 25 breaks Paparazzi, so pass `JAVA_HOME` explicitly.
- Android SDK 36, with `android/local.properties` containing `sdk.dir=…` (git-ignored)
- Node ≥ 22

## Commands

```sh
# Scripts tests
cd scripts && node --test test/

# Token generation and check
scripts/gen-tokens
scripts/gen-tokens --check

# Design freeze check
scripts/design-freeze --check

# iOS tests
cd ios && xcodegen generate && xcodebuild test -project Baupilot.xcodeproj -scheme Baupilot -destination 'platform=iOS Simulator,name=iPhone 15'

# Android tests
cd android && JAVA_HOME=<jdk21> ./gradlew :app:testDebugUnitTest

# Diff reports (output in reports/<platform>/)
scripts/diff-report --platform ios
scripts/diff-report --platform android
```

The Login example snapshot tests are expected-red until M2 (D17). The reference comparison runs; the failure is tolerated only for pixel differences.

## Font provenance

Fonts are embedded as TTF and also used in the test path.

| File | Source | SHA-256 |
|---|---|---|
| `ios/Baupilot/Resources/Fonts/Inter-Regular.ttf` | Inter v4.1, `Inter-4.1.zip` → `extras/ttf/` | `40d692fce188e4471e2b3cba937be967878f631ad3ebbbdcd587687c7ebe0c82` |
| `ios/Baupilot/Resources/Fonts/Inter-Medium.ttf` | same | `97ad806f526e41546d46365bb3a393145f75b7b1568913db74549ad8b8dba872` |
| `ios/Baupilot/Resources/Fonts/Inter-SemiBold.ttf` | same | `78a843fade9d4612a5567302fb595b56976eb5fcebf4fea5a5912d638bafcde3` |
| `ios/Baupilot/Resources/Fonts/Inter-Bold.ttf` | same | `288316099b1e0a47a4716d159098005eef7c0066921f34e3200393dbdb01947f` |
| `android/app/src/main/res/font/roboto_regular.ttf` | Roboto v3.015, `Roboto_v3.015.zip` → `unhinted/static/Roboto-Regular.ttf` | `867580681a6272f69537e220ae9049f082286daf46c1a4a4d21fa55b2cee89fc` |
| `android/app/src/main/res/font/roboto_medium.ttf` | same, `Roboto-Medium.ttf` | `5f4b733aa0214e1610d2870765f9a68b0380b1bfa5a871f0b1eb20e2a86bee59` |
| `android/app/src/main/res/font/roboto_bold.ttf` | same, `Roboto-Bold.ttf` | `f02bf7a9869c58b9bb61130f55c6b1a489ea85f2ac1de75b3421dcb133bc57f3` |

Download URLs:

- Inter: `https://github.com/rsms/inter/releases/download/v4.1/Inter-4.1.zip`
- Roboto (repository `googlefonts/roboto-3-classic`): `https://github.com/googlefonts/roboto-3-classic/releases/download/v3.015/Roboto_v3.015.zip`

## Licences

Both font families are under the SIL Open Font License 1.1.

- Inter: `ios/Baupilot/Resources/Fonts/OFL.txt`
- Roboto: `android/third_party/roboto/OFL.txt`
