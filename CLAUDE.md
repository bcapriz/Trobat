# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Setup

Requires `MAPS_API_KEY` in `local.properties` (not committed):
```
MAPS_API_KEY=your_key_here
```

Build and run via Android Studio Hedgehog+ or:
```bash
./gradlew assembleDebug
./gradlew connectedAndroidTest   # instrumented tests (emulator/device required)
./gradlew test                   # unit tests
```

Min SDK: 26. Target SDK: 36.

## Architecture

**MVVM + MVI** with a two-level navigation graph:

1. **Outer graph** (`AppNavigation`): `splash` → `main`
2. **Inner graph** (`TrobatMainScreen`): bottom-nav tabs (`cases`, `heatmap`, `camera`, `notifications`, `profile`) plus `confirm_report` as a non-tab destination

Each screen has its own ViewModel with three correlated files:
- `FooUiState` — `data class`, exposed as `StateFlow` (persistent UI state)
- `FooEvent` — `sealed interface`, sent from screen to VM via `onEvent()`
- `FooEffect` — `sealed interface`, emitted as `SharedFlow` (one-shot navigation/side-effects)

**Data layer** uses `RepositoryProvider` (singleton object) that currently wires `FakeCaseRepository` and `FakeCitizenReportRepository`. These use in-memory `StateFlow` and are the only implementations (H2 real backend not yet built). Swapping real implementations means editing `RepositoryProvider`.

**`CapturedEvidenceHolder`** is a global singleton `object` that bridges `CaptureEvidenceScreen` → `ConfirmReportScreen` since photo URI + GPS coordinates can't be passed as nav args. Clear it with `CapturedEvidenceHolder.clear()` on retake or cancel.

## Key conventions

- ViewModels are instantiated by Compose `viewModel()` — no DI framework. Repositories are injected manually via `RepositoryProvider`.
- No Hilt/Dagger. Dependency graph is flat and manual.
- CameraX preview runs in-app (not via `ACTION_IMAGE_CAPTURE` intent). Do not change this.
- Retrofit (`TrobatApi`) and OkHttp logging interceptor are already declared as dependencies but not wired — reserved for H2.
- All strings in the UI are in Spanish (Argentine).
