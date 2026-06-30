# Latido

Android app that reads a car's OBD2 fault codes (ELM327) and turns them into a clear,
plain-language diagnosis for people who know nothing about mechanics.

Built in phases (see `promptAppOBD_mejorado.md`), compiling after each one.

## Status — Phase 1 (architecture, minimalist UI, mock data)

No hardware or network yet. The whole UI is built against the real data contract
(`DiagnosisResult`) using mock providers, so later phases swap implementations without
touching the UI.

What's in:

- **Stack:** Kotlin, Jetpack Compose (Material 3), Hilt, Navigation Compose, Coroutines/Flow,
  kotlinx.serialization, Room (wired for later). `minSdk 26`, `compileSdk/targetSdk 35`.
- **i18n from day 1:** English base in `values/`, Spanish in `values-es/`, no hardcoded text,
  `locales_config.xml` listing all 12 launch languages, `MainActivity` is an `AppCompatActivity`
  so per-app language switching works. `Locale.ROOT` is used for all internal code logic.
- **Three screens:** Home (connection + big "Analyze my car" + health summary), Diagnosis,
  History (in-memory for Phase 1; Room arrives in Phase 4).
- **Free basic view:** code list with short name + severity colour + "Clear faults", built as
  a surface separate from the AI verdict.
- **AI verdict (mock):** big single verdict block, probable faults, the three key answers,
  estimated cost, and a "For your mechanic" card with a (stubbed) Share button.
- **Inspection card** (ITV/MOT/…): localized label, mock status (real logic in Phase 5).
- **Design system:** sober palette, single blue accent, green/amber/red reserved exclusively
  for severity, large type, rounded cards, friendly loading skeleton.

### Architecture highlights

- `DiagnosisProvider` — single abstraction (`MockDiagnosisProvider` now, LLM proxy in Phase 3).
- `VehicleRepository` — source of car data (`MockVehicleRepository` now, real ELM327 in Phase 2).
- `DiagnosisResult` — the data contract (prompt section 4). JSON keys are fixed; only values are
  localized. `DiagnosisContractTest` locks the mapping.
- `DtcDictionary` — local asset dictionary: neutral metadata (`dtc_dictionary.json`: severity +
  safety flag) separated from translatable names (`dtc_names_<lang>.json`).

## Build

Open in Android Studio (Ladybug+) and let it sync — it provides the Android SDK and creates
`local.properties`. Or from the CLI with the SDK installed:

```
./gradlew :app:assembleDebug      # build
./gradlew :app:testDebugUnitTest  # run unit tests (data-contract test)
```

The diagnosis proxy URL is read from `local.properties` (`PROXY_URL_DEBUG` / `PROXY_URL_RELEASE`)
into `BuildConfig.PROXY_URL`. Not used in Phase 1 (mocks), wired for Phase 3.

## Not yet (by design)

- Bluetooth / real OBD reads (Phase 2)
- Real LLM diagnosis via the Cloudflare proxy (Phase 3)
- Room persistence, share/report, clear-history (Phase 4)
- Inspection readiness monitors, backup, legal (Phase 5)
- Billing, free-tier funnel, reviews (Phase 6)
- In-app language selector **screen** (infra is ready; the picker UI lands with Settings)
