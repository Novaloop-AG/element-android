# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Element Android is a Matrix client for Android. This is Element Classic (previous generation), now in maintenance mode receiving security updates only. The project includes both the Android application and the Matrix SDK (matrix-sdk-android).

**Note**: This repository is a fork of Element Android, rebranded as HealthChat for the healthcare industry. The `release/healthchat` branch contains HealthChat-specific customizations. New HealthChat features should be developed on feature branches based on `release/healthchat`.

**Key fact**: The Matrix SDK and Element application currently share the same repository. The SDK is exported separately to https://github.com/matrix-org/matrix-android-sdk2 at each release.

### HealthChat Customizations

HealthChat is a stripped-down, SSO-only deployment for healthcare. Key differences from upstream Element:

**Identity**: Package `ch.healthchat.element`, app name "HealthChat", navy branding (#1f4d85 primary, #12325c navy, #376A91 steel blue).

**Infrastructure** (configured in `vector-config/src/main/res/values/`):
- Home server: `matrix.healthchat.ch`
- Push gateway: `sygnal.healthchat.ch`
- Jitsi: `jitsi.healthchat.ch`
- Permalink host: `app.healthchat.ch`

**Authentication**: SSO-only. Splash screen goes directly to combined login. SSO button labeled "Login".

**Disabled features** (do not re-enable without discussion):
- Location sharing (service and permissions removed from manifest)
- Voice broadcast, polls, stickers, contact sharing (feature flags)
- Invite friends, QR code, contact book integration (UI removed)
- Labs section, legals section, rageshake (hidden in settings)
- Password change, 3PID management, account discovery/deactivation, integrations, identity server selection (hidden)
- Analytics (PostHog/Sentry disabled)
- APK installation capability (security restriction)
- Sunset migration notice

## Prerequisites

- **Git LFS**: Required for screenshot tests. Install via package manager (`brew install git-lfs` or `yay -S git-lfs`), then run `git lfs install --local` in the project root.
- **Java/JDK**: Ensure a compatible JDK is installed (check `./gradlew --version` for requirements).

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

# Run all integration tests (requires local Synapse server and emulator)
./gradlew vector:connectedAndroidTest matrix-sdk-android:connectedAndroidTest
```

### Testing Conventions

**Naming**: Use Gherkin format with backticks:
```kotlin
@Test
fun `given a lowercase label, when uppercasing, then returns label uppercased`()
```

**Assertions**: Use Kluent's fluent API, assert entire objects:
```kotlin
result shouldBeEqualTo Person(age = 100, name = "Gandalf")  // Prefer
result.age shouldBeEqualTo 100                               // Avoid
```

**Mocking**: Use Mockk. Avoid relaxed mocks; use Fakes instead.

**Fakes and Fixtures**:
- Fakes: Reusable test doubles in `${package}.test.fakes`
- Fixtures: Reusable data builders in `${package}.test.fixtures`

**ViewModel Testing**: Use `MavericksTestRule` and the `viewModel.test()` extension:
```kotlin
@get:Rule
val mavericksTestRule = MavericksTestRule(testDispatcher = UnconfinedTestDispatcher())

@Test
fun `when handling action, then emits expected states`() {
    val viewModel = MyViewModel(initialState)
    val test = viewModel.test()
    viewModel.handle(MyAction)
    test.assertStatesChanges(initialState, { copy(loading = true) }).finish()
}
```

**Threading**: Always inject `Dispatchers` and `Clock` instances; provide fakes in tests.

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
  - Entry point: `org.matrix.android.sdk.api.Matrix` (singleton, provides AuthenticationService, etc.)
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
- `Fragment` - UI screen (extends `VectorBaseFragment` in `vector/src/main/java/im/vector/app/core/platform/`)
- `ViewModel` - Business logic (extends `VectorViewModel`), has `handle(action)` method
- `ViewState` - Immutable state (implements `MavericksState`), use `copy()` to update
- `ViewEvents` - One-time events (navigation, dialogs, toasts) - extends `VectorViewEvents`
- `ViewAction` - User actions sent to ViewModel - extends `VectorViewModelAction`
- `VectorSharedActionViewModel` - Communication between Fragments and Activity

Key base classes are in `vector/src/main/java/im/vector/app/core/platform/`.

**Epoxy - RecyclerView:**
- `Controller` - Declares RecyclerView items (extends EpoxyController or TypedEpoxyController)
- Fragment calls `controller.setData(state)`, which triggers `buildModels()`
- Epoxy handles diffing and rendering automatically
- **Warning**: Each item MUST have a unique ID or it will crash
- Common Epoxy items are in `vector/src/main/java/im/vector/app/core/epoxy/`

**Dependency Injection:**
- SDK uses Dagger
- App uses Hilt (DI modules in `vector/src/main/java/im/vector/app/core/di/`)
- Services in SDK are interfaces in `org.matrix.android.sdk.api`, implementations in `org.matrix.android.sdk.internal`

**Network & Serialization:**
- [Retrofit](https://square.github.io/retrofit/) + [OkHttp3](https://square.github.io/okhttp/) for HTTP
- [Moshi](https://github.com/square/moshi) for JSON parsing

**Data Flow:**
- Database (Realm) is source of truth — multiple DB migrations must be handled incrementally (never squash pending migrations on `develop`/`release/healthchat`)
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

## Releasing HealthChat

Release procedure (tagging, Play Store upload, keystore rotation, rebase divergence) lives in the `releasing-healthchat` skill at `.claude/skills/releasing-healthchat/SKILL.md`. Claude surfaces it on demand. Release notes are maintained in `HEALTHCHAT_CHANGES.md` at the repo root.

## Testing Requirements

- Test on Android API 21+ (Lollipop minimum)
- Test on real device when possible (emulator may miss issues)
- Screenshot tests use Paparazzi (excluded from normal unit tests)

## Important Notes

- When adding dependencies, update `dependencies_groups.gradle` to allow download (sub-dependencies may also need adding)
- Dependency versions are centralized in `dependencies.gradle`
- Developer mode: Settings → Advanced settings (enables debug features)
- Type `/devtools` in room composer for developer menu
- Hidden debug: Green wheel icon (debug builds with developer mode)
- Use "DO NOT COMMIT" in comments to prevent accidental commits (CI checks)
- Rageshake (shake phone) sends bug reports with screenshots and logs to an internal server
- Classes added to `org.matrix.android.sdk.internal` must also be declared `internal` in Kotlin

## Common Pitfalls

- Don't use same Epoxy item ID twice (will crash)
- Always read file before using Write tool
- Format files before committing (use project code style)
- Run quality checks before creating PR
- Never skip `./tools/check/check_code_quality.sh`
- File line limit: Kotlin files must be under 2800 lines (enforced by quality checks)
- No PNG files allowed in `/drawable` folder (only in density-specific folders like `drawable-hdpi`)
- Drawable folders must have matching file counts across all density variants
