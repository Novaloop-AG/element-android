# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Element Android is a Matrix client for Android. This is Element Classic (previous generation), now in maintenance mode receiving security updates only. The project includes both the Android application and the Matrix SDK (matrix-sdk-android).

**Note**: This repository is a fork of Element Android, rebranded as HealthChat for the healthcare industry. The `release/healthchat` branch contains HealthChat-specific customizations.

**Key fact**: The Matrix SDK and Element application currently share the same repository. The SDK is exported separately to https://github.com/matrix-org/matrix-android-sdk2 at each release.

## Build Commands

### Standard Build
```bash
./gradlew assembleGplayDebug      # Build Google Play debug variant
./gradlew assembleFdroidDebug     # Build F-Droid debug variant
./gradlew assembleGplayRelease    # Build Google Play release variant
./gradlew assembleFdroidRelease   # Build F-Droid release variant
```

### Code Quality
```bash
# Run all quality checks (REQUIRED before PR)
./tools/check/check_code_quality.sh

# Kotlin linting
./gradlew ktlintCheck --continue
./gradlew ktlintFormat              # Auto-fix some ktlint issues

# Markdown table of contents
./gradlew knit                      # Update markdown TOCs
./gradlew knitCheck                 # Verify markdown is up to date

# Android lint
./gradlew lintGplayRelease
./gradlew lintFdroidRelease
```

### Testing
```bash
# Unit tests
./gradlew testGplayReleaseUnitTest
./gradlew testFdroidReleaseUnitTest

# Matrix SDK unit tests
./gradlew matrix-sdk-android:testDebugUnitTest

# Integration tests (requires connected device/emulator)
./gradlew matrix-sdk-android:connectedAndroidTest

# Screenshot tests
./gradlew recordPaparazziDebug     # Record new screenshots
./gradlew verifyPaparazziDebug     # Verify screenshots match
# Or use: ./gradlew recordScreenshots and ./gradlew verifyScreenshots

# Run specific SDK integration test class
./gradlew -Pandroid.testInstrumentationRunnerArguments.class=org.matrix.android.sdk.session.room.RoomServiceTest matrix-sdk-android:connectedAndroidTest

# Run a single unit test class
./gradlew :vector:testGplayReleaseUnitTest --tests "im.vector.app.features.SomeTest"

# Run a single test method
./gradlew :vector:testGplayReleaseUnitTest --tests "im.vector.app.features.SomeTest.testMethodName"
```

## Project Structure

### Main Modules

**Application modules:**
- `vector-app/` - Application entry point, contains the main Activity
- `vector/` - Legacy application module (now a library), contains most UI features
- `vector-config/` - Application configuration (work in progress to migrate all config here)
- `library/ui-strings/` - All string resources (internationalization)
- `library/ui-styles/` - Android styles and themes

**SDK modules:**
- `matrix-sdk-android/` - Main Matrix SDK (Kotlin)
  - Package structure: `org.matrix.android.sdk.api.*` (public interfaces), `org.matrix.android.sdk.internal.*` (internal implementations)
- `matrix-sdk-android-flow/` - Flow wrappers for the SDK
- `library/rustCrypto/` - Rust crypto integration

**Library modules:**
- `library/core-utils/` - Core utilities
- `library/attachment-viewer/` - Attachment viewing
- `library/multipicker/` - File/media picker
- `library/external/*` - Third-party libraries (modified or vendored)

### Architecture Patterns

**MvRx (Mavericks) - MVI Framework:**
- `Fragment` - UI screen (extends VectorBaseFragment)
- `ViewModel` - Business logic (extends VectorViewModel), has `handle(action)` method
- `ViewState` - Immutable state (implements MavericksState), use `copy()` to update
- `ViewEvents` - One-time events (navigation, dialogs, toasts)
- `ViewAction` - User actions sent to ViewModel
- `VectorSharedActionViewModel` - Communication between Fragments and Activity

**Epoxy - RecyclerView:**
- `Controller` - Declares RecyclerView items (extends EpoxyController or TypedEpoxyController)
- Fragment calls `controller.setData(state)`, which triggers `buildModels()`
- Epoxy handles diffing and rendering automatically
- **Warning**: Each item MUST have a unique ID or it will crash

**Dependency Injection:**
- SDK uses Dagger
- App uses Hilt
- Services in SDK are interfaces in `org.matrix.android.sdk.api`, implementations in `org.matrix.android.sdk.internal`

**Data Flow:**
- Database (Realm) is source of truth
- SDK exposes LiveData (legacy) and Flow (new)
- ViewModels subscribe to SDK services
- Fragment's `invalidate()` method updates UI from ViewState
- Use `onEach` to listen to specific ViewState properties

### Matrix Concepts

**Sync mechanism:**
- Initial sync: First request without token (loads all rooms with state)
- Incremental sync: Subsequent requests with token
- Gappy/limited sync: Missing events, requires pagination API
- SyncThread manages the sync loop when app is in foreground
- Push triggers sync when app is in background

**Core data types:**
- `Room`: Container for events, identified by room_id
- `Event`: Room data items (messages, state changes, etc.)
  - Regular Events: user content (messages, reactions, edits)
  - State Events: room state (name, topic, members) - have non-null state_key
- Events uniquely identified by (event_id, type, state_key)

## Development Workflow

### Adding New Features

Use the Android Studio template for new screens:
1. Install template once: `cd tools/template && ./configure.sh` (Mac) or `ANDROID_STUDIO=/path/to/android-studio ./configure` (Linux)
2. Restart Android Studio
3. Create new package, right-click → New → New Vector/Element Feature
4. Follow wizard, replace "Main" with your feature name
5. Follow TODOs in generated files

### Code Style

- **Hard wrap at 160 characters** (Android Studio setting)
- Use project formatting rules (`.idea/codeStyles/`)
- **Kotlin only** - do not write Java classes
- Use `sealed interface` instead of `sealed class` when possible
- Warnings are treated as errors (override with `-PallWarningsAsErrors=false`)

### String Resources

**CRITICAL**: Never directly edit translation files in PRs (causes Weblate conflicts)

- Add new strings only to `library/ui-strings/src/main/res/values/strings.xml`
- Use American English (e.g., "color" not "colour")
- Use `plurals` resources even if always plural in English
- Add anywhere in file (preferably grouped by feature, not at end)
- Accessibility strings: prefix with `a11y_`

To remove strings:
```xml
<!-- TODO TO BE REMOVED -->
<string name="unused_string" tools:ignore="UnusedResources">...</string>
```

To rename string IDs:
```xml
<!-- TODO Rename id to new_id_name -->
<string name="old_id">...</string>
```

### Changelog

Create file(s) in `./changelog.d/` using PR or issue number:
- `.feature` - New feature
- `.bugfix` - Bug fix
- `.wip` - Work in progress
- `.doc` - Documentation
- `.sdk` - SDK change (API addition/deprecation/removal)
- `.misc` - Other changes

### Logging

Use Timber with tags (automatic tags unavailable in release):
```kotlin
Timber.tag(loggerTag.value).d("my log")
```

**CRITICAL**: NEVER log private user data unless using LOG_PRIVATE_DATA flag. Be careful with data classes (logs all fields).

Prefer `Timber.d()` and up (not `Timber.v()` - may not work on some devices).

### Layout & Accessibility

- Check RTL language rendering (preview with Arabic)
- Check all themes (AppTheme.Status, AppTheme.Dark, etc.)
- Use `?attr` instead of `@color` for color references
- Add `android:contentDescription` and `android:importantForAccessibility`
- Test with screen reader

## Variants

- **Gplay**: Uses Firebase for push notifications (Google Services)
- **Fdroid**: No closed-source dependencies, uses UnifiedPush or background polling

## Testing Requirements

- Test on Android API 21+ (Lollipop minimum)
- Test on real device when possible (emulator may miss issues)
- Screenshot tests use Paparazzi (excluded from normal unit tests)

## Important Notes

- When adding dependencies, update `dependencies_groups.gradle` to allow download
- Dependabot automatically creates PRs to upgrade dependencies
- Developer mode: Settings → Advanced settings (enables debug features)
- Type `/devtools` in room composer for developer menu
- Hidden debug: Green wheel icon (debug builds with developer mode)
- Use "DO NOT COMMIT" in comments to prevent accidental commits (CI checks)

## Common Pitfalls

- Don't use same Epoxy item ID twice (will crash)
- Always read file before using Write tool
- Format files before committing (use project code style)
- Run quality checks before creating PR
- Never skip `./tools/check/check_code_quality.sh`
