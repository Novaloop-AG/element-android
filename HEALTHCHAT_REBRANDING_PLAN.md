# HealthChat Android Rebranding Implementation Plan

**Status**: Ready for implementation
**Based on**: iOS rebranding in `Novaloop-AG/element-ios` branch `release/healthchat`
**Target**: Element Classic Android → HealthChat Android
**Approach**: Sequential 7-phase implementation with verification checkpoints

---

## Table of Contents
- [Overview](#overview)
- [Key Decisions](#key-decisions)
- [iOS Branding Changes Reference](#ios-branding-changes-reference)
- [Phase 1: App Identity & Package Changes](#phase-1-app-identity--package-changes)
- [Phase 2: Server & Infrastructure Configuration](#phase-2-server--infrastructure-configuration)
- [Phase 3: Visual Branding (Colors & Themes)](#phase-3-visual-branding-colors--themes)
- [Phase 4: Asset Extraction & Replacement](#phase-4-asset-extraction--replacement)
- [Phase 5: Feature Flag Configuration](#phase-5-feature-flag-configuration)
- [Phase 6: Build Configuration & Dependencies](#phase-6-build-configuration--dependencies)
- [Phase 7: Final Testing & Quality Checks](#phase-7-final-testing--quality-checks)
- [Troubleshooting](#troubleshooting)

---

## Overview

This document outlines the complete rebranding of Element Classic Android to HealthChat, replicating all changes made in the iOS version. The implementation is divided into 7 sequential phases, each with specific verification steps that must pass before proceeding to the next phase.

### Goals
- Rebrand all user-facing elements from Element to HealthChat
- Point to HealthChat infrastructure (servers, push gateway, Jitsi)
- Simplify UX by disabling non-essential features
- Maintain code quality and functionality throughout

### Execution Model
- **Sequential**: Complete one phase fully before starting the next
- **Verified**: Each phase has mandatory verification steps
- **Reversible**: Git commits after each successful phase for rollback capability

---

## Key Decisions

| Aspect | Decision |
|--------|----------|
| **Scope** | Replicate ALL iOS branding changes exactly |
| **Package ID** | `im.vector.app` → `ch.healthchat.element` |
| **App Name** | "Element Classic" → "HealthChat" |
| **Primary Color** | Element green (#0DBD8B) → HealthChat blue (#00A7D7) |
| **Servers** | Use only HealthChat infrastructure (matrix.healthchat.ch) |
| **Assets** | Extract from iOS repo and convert to Android formats |
| **Versioning** | Keep current Android version (1.6.48+), independent from iOS |
| **Features** | Disable 30+ features matching iOS simplification |
| **Build Variants** | Update both Gplay (with Firebase) and Fdroid (without) |

---

## iOS Branding Changes Reference

### Identity Changes (from iOS)
- **Bundle ID**: `im.vector.app` → `ch.healthchat.element`
- **App Name**: "Element Classic" → "HealthChat"
- **Development Team**: Changed to HealthChat team ID
- **App Group**: `group.im.vector` → `group.ch.healthchat`

### Server Configuration (from iOS)
- **Homeserver**: `https://matrix.org` → `https://matrix.healthchat.ch`
- **Identity Server**: `https://vector.im` → `https://matrix.healthchat.ch`
- **Push Gateway**: `https://matrix.org/_matrix/push/v1/notify` → `https://sygnal.healthchat.ch/_matrix/push/v1/notify`
- **Jitsi**: `https://jitsi.riot.im` → `https://jitsi.healthchat.ch`
- **Web App**: `https://app.element.io` → `https://app.healthchat.ch`

### Color Changes (from iOS)
- **Accent Color**: `#0DBD8B` (Element green) → `#00A7D7` (HealthChat blue)

### Features Disabled (from iOS)
- Stickers, Contact access, Invite external users
- Email/phone 3PID management
- Identity server configuration
- Advanced settings, Lab settings
- Rageshake/crash reporting settings
- Bug reporting, Account deactivation
- Change password, Integrations
- Report content, Registration
- Social login, Forgot password
- Location sharing, Custom server options

### Legal URLs (from iOS)
- All Element.io legal/help URLs removed (copyright, privacy, acceptable use, help)

---

## Phase 1: App Identity & Package Changes

### Objective
Update the application identity, package name, and app name to HealthChat branding.

### Files to Modify

#### 1. `vector-app/build.gradle`

**Line 138** - Namespace:
```gradle
// OLD
namespace "im.vector.application"

// NEW
namespace "ch.healthchat.element"
```

**Line 146** - Application ID:
```gradle
// OLD
applicationId "im.vector.app"

// NEW
applicationId "ch.healthchat.element"
```

**Line 250** - Debug App Name:
```gradle
// OLD
resValue "string", "app_name", "Element Classic - dbg"

// NEW
resValue "string", "app_name", "HealthChat - dbg"
```

**Line 258** - Release App Name:
```gradle
// OLD
resValue "string", "app_name", "Element Classic"

// NEW
resValue "string", "app_name", "HealthChat"
```

#### 2. `vector-config/src/main/res/values/config.xml`

**Line 26** - Pusher App ID:
```xml
<!-- OLD -->
<string name="pusher_app_id" translatable="false">im.vector.app.android</string>

<!-- NEW -->
<string name="pusher_app_id" translatable="false">ch.healthchat.element.android</string>
```

### Verification Steps

1. **Build the debug variant**:
   ```bash
   ./gradlew assembleGplayDebug assembleFdroidDebug
   ```

2. **Install on device/emulator**:
   ```bash
   adb install vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```

3. **Verify**:
   - [ ] App name shows as "HealthChat - dbg" in launcher
   - [ ] App launches without crashes
   - [ ] Can reach login screen
   - [ ] Check logcat for package name errors
   - [ ] Note: This installs as separate app (different package ID)

4. **Git commit**: If all checks pass, commit changes:
   ```bash
   git add -A
   git commit -m "Phase 1: Update app identity and package to HealthChat"
   ```

---

## Phase 2: Server & Infrastructure Configuration

### Objective
Update all server endpoints to point to HealthChat infrastructure and remove Element.io URLs.

### Files to Modify

#### 1. `vector-config/src/main/res/values/config.xml`

**Line 7** - Default Homeserver:
```xml
<!-- OLD -->
<string name="matrix_org_server_url" translatable="false">https://matrix.org</string>

<!-- NEW -->
<string name="matrix_org_server_url" translatable="false">https://matrix.healthchat.ch</string>
```

**Line 10** - Bug Report URL (clear or update):
```xml
<!-- OLD -->
<string name="bug_report_url" translatable="false">https://riot.im/bugreports/submit</string>

<!-- NEW -->
<string name="bug_report_url" translatable="false"></string>
```

**Line 21** - Push Gateway:
```xml
<!-- OLD -->
<string name="pusher_http_url" translatable="false">https://matrix.org/_matrix/push/v1/notify</string>

<!-- NEW -->
<string name="pusher_http_url" translatable="false">https://sygnal.healthchat.ch/_matrix/push/v1/notify</string>
```

**Line 24** - UnifiedPush Gateway:
```xml
<!-- OLD -->
<string name="default_push_gateway_http_url" translatable="false">https://matrix.gateway.unifiedpush.org/_matrix/push/v1/notify</string>

<!-- NEW (verify endpoint with HealthChat team) -->
<string name="default_push_gateway_http_url" translatable="false">https://matrix.gateway.unifiedpush.org/_matrix/push/v1/notify</string>
```

**Line 29** - Jitsi Server:
```xml
<!-- OLD -->
<string name="preferred_jitsi_domain" translatable="false">meet.element.io</string>

<!-- NEW -->
<string name="preferred_jitsi_domain" translatable="false">jitsi.healthchat.ch</string>
```

**Lines 31-34** - Room Directory Servers:
```xml
<!-- OLD -->
<string-array name="room_directory_servers">
    <item>matrix.org</item>
    <item>gitter.im</item>
    <item>libera.chat</item>
</string-array>

<!-- NEW (HealthChat servers only) -->
<string-array name="room_directory_servers">
    <item>matrix.healthchat.ch</item>
</string-array>
```

**Lines 37-45** - Permalink Supported Hosts (add HealthChat):
```xml
<!-- Add to existing array -->
<string-array name="permalink_supported_hosts">
    <!-- Keep existing entries -->
    <item>app.healthchat.ch</item>
</string-array>
```

#### 2. `vector-config/src/main/res/values/urls.xml`

**Line 5** - Threads Learn More URL:
```xml
<!-- OLD -->
<string name="threads_learn_more_url" translatable="false">https://element.io/help#threads</string>

<!-- NEW -->
<string name="threads_learn_more_url" translatable="false"></string>
```

**Line 6** - EMS URL:
```xml
<!-- OLD -->
<string name="ftue_ems_url">https://element.io/ems</string>

<!-- NEW -->
<string name="ftue_ems_url"></string>
```

### Verification Steps

1. **Build and install**:
   ```bash
   ./gradlew assembleGplayDebug
   adb install -r vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```

2. **Verify**:
   - [ ] Login screen shows default homeserver: `matrix.healthchat.ch`
   - [ ] Attempt login to HealthChat server (requires server to be operational)
   - [ ] Check network logs: `adb logcat | grep -i http`
   - [ ] Verify no Element.io URLs are being accessed
   - [ ] Test Jitsi video calls if possible

3. **Git commit**:
   ```bash
   git add -A
   git commit -m "Phase 2: Update server configuration to HealthChat infrastructure"
   ```

---

## Phase 3: Visual Branding (Colors & Themes)

### Objective
Replace all Element green branding (#0DBD8B) with HealthChat blue (#00A7D7).

### Files to Modify

#### 1. `library/ui-styles/src/main/res/values/palette.xml`

**Line 19** - Primary Brand Color:
```xml
<!-- OLD -->
<color name="palette_element_green">#0DBD8B</color>

<!-- NEW -->
<color name="palette_element_green">#00A7D7</color>
```

**Note**: Keep the name `palette_element_green` for now to avoid breaking references. This will now contain HealthChat blue.

#### 2. `vector-app/src/main/res/values/colors.xml`

**Line 3** - Launcher Background:
```xml
<!-- OLD -->
<color name="launcher_background">#0DBD8B</color>

<!-- NEW -->
<color name="launcher_background">#00A7D7</color>
```

#### 3. Additional Color References (51 files total)

Use find and replace for all hardcoded Element green:

**Search**: `#0DBD8B`
**Replace**: `#00A7D7`

**Files affected** (examples):
- `library/ui-styles/src/main/res/drawable/element_logo_green.xml` (4 occurrences - lines 8, 12, 16, 20)
- `library/ui-styles/src/main/res/values/colors.xml` (2 occurrences)
- Multiple icon files in `vector/src/main/res/drawable/*.xml`

**Command to find all occurrences**:
```bash
grep -r "#0DBD8B" --include="*.xml" .
```

**Command to replace** (use with caution):
```bash
find . -name "*.xml" -type f -exec sed -i 's/#0DBD8B/#00A7D7/g' {} +
```

### Verification Steps

1. **Build and install**:
   ```bash
   ./gradlew assembleGplayDebug
   adb install -r vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```

2. **Visual verification**:
   - [ ] Launcher shows blue background instead of green
   - [ ] Login screen uses HealthChat blue for accent colors
   - [ ] Buttons and links are HealthChat blue
   - [ ] Progress indicators are blue
   - [ ] Any visible logos show blue color
   - [ ] Test in both light and dark themes

3. **Take screenshots** for comparison

4. **Git commit**:
   ```bash
   git add -A
   git commit -m "Phase 3: Update brand colors from Element green to HealthChat blue"
   ```

---

## Phase 4: Asset Extraction & Replacement

### Objective
Extract HealthChat branded assets from iOS repository and convert them to Android formats, then replace all Element logos and icons.

### Asset Sources (from iOS repo: Novaloop-AG/element-ios, branch: release/healthchat)

| iOS Asset | Location | Android Usage |
|-----------|----------|---------------|
| App Icon | `Riot/Assets/SharedImages.xcassets/AppIcon.appiconset/` | Launcher icons |
| Launch Screen Logo | `Riot/Assets/Images.xcassets/launch_screen_logo.imageset/` | Splash screen |
| Horizontal Logo | `Riot/Assets/SharedImages.xcassets/horizontal_logo.imageset/` | In-app branding |
| App Symbol | `Riot/Assets/Images.xcassets/Common/app_symbol.imageset/` | UI elements |

### Android Asset Requirements

#### Launcher Icons (PNG format required)

| Density | Size | Location |
|---------|------|----------|
| mdpi | 48x48 | `vector-app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | 72x72 | `vector-app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96x96 | `vector-app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144x144 | `vector-app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192x192 | `vector-app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

**Also create round variants**: `ic_launcher_round.png` for each density.

#### Adaptive Icon (Android 8.0+)

**Files to update**:
- `vector-app/src/main/res/drawable/ic_launcher_background.xml` - Update background color to HealthChat blue (#00A7D7)
- `vector-app/src/main/res/drawable-anydpi-v26/ic_launcher_foreground.xml` - Replace with HealthChat logo as VectorDrawable

#### In-App Logos (VectorDrawable preferred)

**Files to replace**:
- `library/ui-styles/src/main/res/drawable/element_logo_green.xml` - Main logo (now blue)
- `vector/src/main/res/drawable/element_logotype.xml` - Wordmark/text logo
- `vector/src/main/res/drawable/element_logo_stars.xml` - Logo variant (or remove if not used)
- `vector/src/main/res/drawable-*/replacement_app_icon.png` - Replacement app icon (sunset feature)

### Conversion Process

1. **Extract iOS assets**:
   ```bash
   # Clone or access the iOS repo
   git clone https://github.com/Novaloop-AG/element-ios.git
   cd element-ios
   git checkout release/healthchat

   # Assets are in: Riot/Assets/
   ```

2. **Convert to Android formats**:
   - Extract highest resolution images (@3x versions from iOS)
   - Use image editor (GIMP, Photoshop, etc.) or online tool to resize
   - Generate each Android density from the highest resolution source
   - For vector graphics, convert SVG to Android VectorDrawable XML

3. **Replace Android assets**:
   - Copy all launcher icon PNGs to appropriate mipmap-* directories
   - Update VectorDrawable XML files with new logo paths
   - Ensure all files are properly formatted

### Files to Replace

```
vector-app/src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-hdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xxhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-xxxhdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
└── drawable/
    └── ic_launcher_background.xml

library/ui-styles/src/main/res/drawable/
└── element_logo_green.xml

vector/src/main/res/drawable/
├── element_logotype.xml
└── element_logo_stars.xml
```

### Verification Steps

1. **Build and install**:
   ```bash
   ./gradlew assembleGplayDebug
   adb install -r vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```

2. **Visual verification**:
   - [ ] Launcher icon shows HealthChat branding (both round and square)
   - [ ] Adaptive icon displays correctly on Android 8+ devices
   - [ ] Splash/launch screen shows HealthChat logo
   - [ ] Login screen shows correct branding
   - [ ] Settings → About shows HealthChat logo
   - [ ] Check all themes (light, dark, status)

3. **Test on multiple devices** if possible (different Android versions)

4. **Git commit**:
   ```bash
   git add -A
   git commit -m "Phase 4: Replace all Element assets with HealthChat branding"
   ```

---

## Phase 5: Feature Flag Configuration

### Objective
Disable features to match the simplified iOS configuration and hide unnecessary settings.

### Files to Modify

#### 1. `vector-config/src/main/java/im/vector/app/config/Config.kt`

**Line 33** - Location Sharing:
```kotlin
// OLD
const val ENABLE_LOCATION_SHARING = true

// NEW
const val ENABLE_LOCATION_SHARING = false
```

**Lines 70-97** - Analytics Configuration (PostHog & Sentry):
```kotlin
// OLD
val analytics = Analytics.Enabled(
    postHogHost = "https://posthog.element.io",
    // ... more config
)

// NEW - Disable all analytics
val analytics = Analytics.Disabled
```

**Lines 106-110** - Sunset Configuration (Element X migration):
```kotlin
// OLD
val sunset = Sunset(
    // ... sunset config
)

// NEW - Disable sunset messaging
val sunset = null
```

#### 2. Settings Visibility (XML Preferences)

**Note**: Many settings are controlled via XML preference files. Need to investigate and update:

**File**: `vector/src/main/res/xml/vector_settings_general.xml`
- Contact book settings (already hidden via `app:isPreferenceVisible="@bool/false_not_implemented"`)
- May need to hide additional sections

**File**: `vector/src/main/res/xml/vector_settings_advanced_settings.xml`
- Identity server configuration
- Developer options (may want to keep for internal testing)

**File**: `vector/src/main/res/xml/vector_settings_preferences.xml`
- Main settings structure

### Features to Disable (from iOS analysis)

**To investigate and disable** (need to find Android equivalents):

- [ ] Stickers (sending and display)
- [ ] Contact access/sync
- [ ] Invite external users
- [ ] Email/phone (3PID) management in settings
- [ ] Identity server configuration UI
- [ ] Advanced settings panel (or hide specific items)
- [ ] Lab/experimental settings
- [ ] Rageshake settings
- [ ] Bug reporting UI
- [ ] Account deactivation option
- [ ] Change password in settings
- [ ] Room integrations/widgets
- [ ] Report content option
- [ ] Registration flow (login only)
- [ ] Social login options
- [ ] Forgot password flow
- [ ] Custom server options (use default only)

**Timeline preferences**:
- [ ] Set bubble style as default (find preference key)

### Implementation Strategy

1. **Search for feature flags**:
   ```bash
   # Find configuration options
   grep -r "ENABLE_" vector-config/src/main/java/
   grep -r "BuildConfig" vector/src/main/java/
   ```

2. **Find settings XML files**:
   ```bash
   find . -path "*/res/xml/*settings*.xml"
   ```

3. **For each iOS disabled feature**:
   - Search Android codebase for equivalent
   - Determine if it's a build config, feature flag, or XML preference
   - Update appropriately
   - Document if no equivalent found

### Verification Steps

1. **Build and install**:
   ```bash
   ./gradlew assembleGplayDebug
   adb install -r vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```

2. **Navigate through all settings**:
   - [ ] Settings → General (verify contact settings hidden)
   - [ ] Settings → Advanced (verify identity server hidden)
   - [ ] Settings → Help & About (verify no bug report option)
   - [ ] Settings → Security & Privacy (verify appropriate options)
   - [ ] Room creation/settings (verify integrations hidden)
   - [ ] Login flow (verify no registration or social login)

3. **Check network traffic**:
   ```bash
   adb logcat | grep -i "posthog\|sentry\|analytics"
   ```
   - [ ] Verify no analytics requests being sent

4. **Test core functionality**:
   - [ ] Can still log in
   - [ ] Can send/receive messages
   - [ ] Can create rooms
   - [ ] Timeline shows bubble style (if implemented)

5. **Git commit**:
   ```bash
   git add -A
   git commit -m "Phase 5: Disable features to match HealthChat iOS configuration"
   ```

---

## Phase 6: Build Configuration & Dependencies

### Objective
Update build configuration, Firebase integration, and prepare for release builds.

### Files to Modify

#### 1. `vector-app/build.gradle`

**Lines 35-40** - Version Information (keep current):
```gradle
// Current version - no changes needed
ext.versionMajor = 1
ext.versionMinor = 6
ext.versionPatch = 48
```

**Note**: Using independent Android versioning as decided.

#### 2. Firebase Configuration (Gplay variant only)

**Requirement**: Create new Firebase project for HealthChat with package ID `ch.healthchat.element`

**Files to replace**:
1. `vector-app/src/gplay/debug/google-services.json`
2. `vector-app/src/gplay/release/google-services.json`
3. `vector-app/src/gplay/nightly/google-services.json`

**Steps**:
1. Create Firebase project at https://console.firebase.google.com/
2. Add Android app with package name: `ch.healthchat.element`
3. Download `google-services.json`
4. Replace all 3 variant files with the new configuration
5. Verify package name in JSON matches `ch.healthchat.element`

**JSON structure to verify**:
```json
{
  "client": [{
    "client_info": {
      "android_client_info": {
        "package_name": "ch.healthchat.element"
      }
    }
  }]
}
```

#### 3. `gradle.properties` (optional for now)

**Lines 33-41** - Signing Configuration:

Current uses dummy signing for debug. For release builds, will need:
```properties
# HealthChat Release Signing (configure when ready)
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=healthchat
RELEASE_KEY_PASSWORD=your_key_password
```

**Note**: Document that release signing keys need to be created separately.

### Build Variants

Ensure all variants build correctly:

| Variant | Description | Firebase |
|---------|-------------|----------|
| gplayDebug | Google Play debug build | Yes |
| gplayRelease | Google Play release build | Yes |
| fdroidDebug | F-Droid debug build | No |
| fdroidRelease | F-Droid release build | No |
| gplayNightly | Nightly/beta build | Yes |

### Verification Steps

1. **Build all variants**:
   ```bash
   # Debug variants
   ./gradlew assembleGplayDebug assembleFdroidDebug

   # Release variants (may fail if signing not configured)
   ./gradlew assembleGplayRelease assembleFdroidRelease
   ```

2. **Verify Gplay variant with Firebase**:
   ```bash
   adb install -r vector-app/build/outputs/apk/gplay/debug/vector-gplay-arm64-v8a-debug.apk
   ```
   - [ ] App launches successfully
   - [ ] Check logcat for Firebase initialization
   - [ ] Test push notifications if possible

3. **Verify Fdroid variant without Firebase**:
   ```bash
   adb install -r vector-app/build/outputs/apk/fdroid/debug/vector-fdroid-arm64-v8a-debug.apk
   ```
   - [ ] App launches successfully
   - [ ] No Firebase errors in logcat
   - [ ] UnifiedPush fallback works

4. **Check APK size and contents**:
   ```bash
   # Analyze APK
   ./gradlew :vector-app:analyzeGplayDebug
   ```

5. **Git commit**:
   ```bash
   git add -A
   git commit -m "Phase 6: Update build configuration and Firebase for HealthChat"
   ```

---

## Phase 7: Final Testing & Quality Checks

### Objective
Run comprehensive quality checks, testing, and final verification before considering the rebranding complete.

### Code Quality Checks

#### 1. Run Complete Quality Check Suite
```bash
./tools/check/check_code_quality.sh
```

This executes:
- Kotlin linting (ktlint)
- Android lint
- Markdown table of contents verification
- Code style checks

#### 2. Fix Auto-fixable Issues
```bash
# Auto-fix Kotlin lint issues
./gradlew ktlintFormat

# Update markdown tables of contents
./gradlew knit

# Verify formatting
./gradlew knitCheck
```

#### 3. Run Unit Tests
```bash
# App unit tests
./gradlew testGplayReleaseUnitTest
./gradlew testFdroidReleaseUnitTest

# SDK unit tests
./gradlew matrix-sdk-android:testDebugUnitTest
```

**Expected**: All tests should pass. If tests fail due to branding changes (e.g., hardcoded strings), update tests accordingly.

### Functional Testing Checklist

#### Installation & Launch
- [ ] Clean install on Android device/emulator
- [ ] App name shows as "HealthChat" in launcher
- [ ] App icon displays HealthChat branding
- [ ] Splash screen shows HealthChat logo
- [ ] No crashes on launch

#### Login & Authentication
- [ ] Default homeserver is `matrix.healthchat.ch`
- [ ] Can successfully log in with HealthChat credentials
- [ ] Registration flow is disabled/hidden
- [ ] Social login options are hidden
- [ ] Forgot password flow is disabled
- [ ] Cannot change to custom homeserver

#### UI & Branding
- [ ] All UI elements use HealthChat blue (#00A7D7)
- [ ] No Element green visible anywhere
- [ ] Login screen branding is correct
- [ ] Settings screen shows HealthChat branding
- [ ] About section shows HealthChat information
- [ ] No "Element" text visible in UI

#### Core Functionality
- [ ] Can send text messages
- [ ] Can receive messages
- [ ] Can create rooms
- [ ] Can join rooms
- [ ] Can upload attachments
- [ ] Can make voice/video calls (Jitsi)
- [ ] Timeline shows bubble style (if implemented)
- [ ] Notifications work (Gplay variant)

#### Settings Verification
- [ ] Contact book settings are hidden
- [ ] Email/phone (3PID) management is hidden
- [ ] Identity server configuration is hidden
- [ ] Advanced settings panel is simplified
- [ ] Lab settings are hidden
- [ ] Bug reporting is disabled
- [ ] Account deactivation is hidden
- [ ] Change password is hidden (or verify it works if keeping it)

#### Features Disabled
- [ ] Cannot send stickers
- [ ] Cannot access contacts
- [ ] Cannot invite external users
- [ ] Room integrations are hidden
- [ ] Location sharing is disabled
- [ ] Report content option is hidden

#### Network & Analytics
- [ ] No requests to Element.io domains (check logcat)
- [ ] No analytics being sent (PostHog, Sentry)
- [ ] Push notifications use HealthChat gateway
- [ ] Jitsi calls use HealthChat server
- [ ] Homeserver is HealthChat server

#### Multi-variant Testing
- [ ] Gplay variant works with Firebase push
- [ ] Fdroid variant works without Google services
- [ ] Both variants have same branding
- [ ] Both variants have same features

### Accessibility & Internationalization

#### Accessibility
- [ ] Test with TalkBack screen reader enabled
- [ ] All buttons/controls are properly labeled
- [ ] Navigation works with accessibility services
- [ ] Content descriptions are appropriate

#### Internationalization
- [ ] Preview layouts in RTL mode (Arabic)
- [ ] All text displays correctly in RTL
- [ ] No layout issues in RTL mode
- [ ] Verify string placeholders work (${app_name})

#### Theme Testing
- [ ] Test in Light theme
- [ ] Test in Dark theme
- [ ] Test in Black (AMOLED) theme if available
- [ ] Test in Status theme
- [ ] Colors are consistent across themes

### Performance Testing

- [ ] App launches in reasonable time
- [ ] Sync performance is acceptable
- [ ] No memory leaks (check Android Profiler)
- [ ] No excessive battery drain
- [ ] APK size is reasonable

### Security Verification

- [ ] HTTPS used for all connections
- [ ] Certificate pinning works (if implemented)
- [ ] No sensitive data in logs
- [ ] No hardcoded credentials or secrets

### Final Verification

#### Code Review
```bash
# Review all changes
git diff origin/main...HEAD

# Check for remaining "Element" references
grep -r "Element" --include="*.xml" vector/src/main/res/values/
grep -r "element.io" --include="*.xml" .
grep -r "#0DBD8B" --include="*.xml" .
```

#### Verify No Remaining Element Branding
- [ ] No "Element" text in user-facing strings
- [ ] No element.io URLs in configuration
- [ ] No Element green (#0DBD8B) colors
- [ ] No Element logos in assets
- [ ] No matrix.org hardcoded (except as documented fallback)

### Documentation & Changelog

#### Create Changelog Entries

Create files in `./changelog.d/` directory:

**Example**: `./changelog.d/healthchat_rebrand.feature`
```
Rebranded app to HealthChat with new visual identity and simplified feature set
```

**Example**: `./changelog.d/healthchat_rebrand.sdk`
```
Updated SDK configuration to use HealthChat infrastructure
```

#### Update Documentation
- [ ] Document any Android-specific differences from iOS
- [ ] Document features that don't have Android equivalents
- [ ] Document HealthChat-specific build instructions
- [ ] Update README if needed

### Deliverables Checklist

- [ ] All 7 phases completed successfully
- [ ] All verification steps passed
- [ ] All tests passing
- [ ] Code quality checks passing
- [ ] Changelog entries created
- [ ] Documentation updated
- [ ] Git commits for each phase
- [ ] Final commit with complete rebranding

### Final Git Commit

```bash
git add -A
git commit -m "Phase 7: Complete HealthChat rebranding with quality checks and testing"
```

### Optional: Create Release Tag

```bash
git tag -a v1.6.48-healthchat -m "HealthChat 1.6.48 - Initial rebrand"
git push origin release/healthchat --tags
```

---

## Troubleshooting

### Common Issues

#### Build Failures

**Issue**: Gradle build fails after package rename
**Solution**:
- Clean build: `./gradlew clean`
- Invalidate caches in Android Studio
- Check all package names are updated consistently

**Issue**: Firebase initialization fails
**Solution**:
- Verify package name in `google-services.json` matches `ch.healthchat.element`
- Ensure file is in correct variant directory
- Check Firebase project configuration

#### Runtime Crashes

**Issue**: App crashes on launch after rebranding
**Solution**:
- Check logcat for stack trace: `adb logcat`
- Look for ClassNotFoundException or ResourceNotFoundException
- Verify all resources are properly renamed
- Check for hardcoded package name references

**Issue**: Login fails with new homeserver
**Solution**:
- Verify HealthChat server is operational
- Check network logs for connection errors
- Verify homeserver URL is correct (https, no trailing slash)
- Test server accessibility from device

#### Asset Issues

**Issue**: Icons not displaying correctly
**Solution**:
- Verify all density variants are present
- Check file names match exactly (ic_launcher.png)
- Ensure PNG files are valid
- Clear app data and reinstall

**Issue**: Colors not changing
**Solution**:
- Verify all 51 color references updated
- Check theme inheritance
- Clear build cache: `./gradlew clean`
- Check for cached resources on device

#### Feature Flag Issues

**Issue**: Features still visible after disabling
**Solution**:
- Verify correct config file modified
- Check if feature uses multiple flags
- Look for XML preference visibility attributes
- May need to hide via XML preference file

**Issue**: Analytics still sending data
**Solution**:
- Verify `Analytics.Disabled` is set
- Check for multiple analytics configurations
- Monitor network traffic: `adb logcat | grep http`
- Clear app data to reset any cached config

### Verification Commands

```bash
# Find remaining Element references
grep -ri "element" --include="*.xml" vector/src/main/res/values/ | grep -v "<!--"

# Find Element.io URLs
grep -r "element\.io" --include="*.xml" .

# Find Element green color
grep -r "#0DBD8B" --include="*.xml" .

# Find matrix.org references
grep -r "matrix\.org" --include="*.xml" .

# Check package name consistency
grep -r "im\.vector" --include="*.gradle" --include="*.xml" --include="*.kt" .

# Verify Firebase package
grep -r "package_name" --include="google-services.json" .
```

### Getting Help

If issues persist:
1. Check Android Studio Build Output panel
2. Review full logcat output
3. Verify each phase's changes were applied correctly
4. Consider reverting to last successful phase commit
5. Consult Element Android documentation
6. Check Matrix SDK documentation

---

## iOS to Android Feature Mapping

### Features Disabled in iOS (need Android equivalents)

| iOS Feature Flag | Android Equivalent | Location | Status |
|------------------|-------------------|----------|--------|
| `allowSendingStickers` | TBD | TBD | 🔍 To investigate |
| `allowLocalContactsAccess` | Contact settings XML | `vector_settings_general.xml` | ✅ Already hidden |
| `allowInviteExernalUsers` | TBD | TBD | 🔍 To investigate |
| `settingsScreenShowDiscoverySettings` | Identity server UI | Advanced settings | ✅ Can hide |
| `settingsScreenShowAdvancedSettings` | Advanced settings panel | `vector_settings_preferences.xml` | ⚠️ May want to keep some |
| `settingsScreenShowLabSettings` | Lab settings | `vector_settings_labs.xml` | ✅ Can hide |
| `settingsScreenAllowBugReportingManually` | Rageshake | Help & About | ✅ Can hide |
| `settingsScreenAllowDeactivatingAccount` | Deactivate account | Advanced settings | ✅ Can hide |
| `settingsScreenShowChangePassword` | Change password | Security settings | ✅ Can hide |
| `roomScreenAllowStickerAction` | Sticker composer button | Room UI | 🔍 To investigate |
| `roomInfoScreenShowIntegrations` | Integrations/widgets | Room settings | ✅ Can hide |
| `authScreenShowRegister` | Registration flow | Login UI | ✅ Can hide |
| `authScreenShowForgotPassword` | Forgot password | Login UI | ✅ Can hide |
| `authScreenShowCustomServerOptions` | Custom homeserver | Login UI | ✅ Can hide |
| `authScreenShowSocialLoginSection` | Social login (SSO) | Login UI | ✅ Can hide |
| `locationSharingEnabled` | `ENABLE_LOCATION_SHARING` | `Config.kt` | ✅ Done |

**Legend**:
- ✅ Can be implemented
- 🔍 Needs investigation
- ⚠️ Requires decision

---

## Summary

This document provides a complete roadmap for rebranding Element Classic Android to HealthChat. Follow each phase sequentially, ensuring verification passes before moving to the next phase. The result will be a fully rebranded Android app matching the iOS HealthChat configuration.

**Estimated Time**: 2-3 days for complete implementation and testing

**Next Steps**: Begin with Phase 1 and proceed sequentially through all phases.
