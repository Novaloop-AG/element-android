# HealthChat Play Store Publication Plan

**Last Updated:** 2024-11-26
**Branch:** `release/healthchat`
**Package:** `ch.healthchat.element`
**Version:** 1.6.48 (code: 40106480)

---

## Completed Phases

### Phase 1: External Account Setup
- [x] Google Play Console developer account
- [x] Firebase project (healthchat-production) configured
- [x] Identity verification (can be postponed)

### Phase 2: Codebase Configuration
- [x] `fastlane/Appfile` - Updated package name to `ch.healthchat.element`
- [x] `fastlane/metadata/android/en-US/` - HealthChat metadata (German)
- [x] `fastlane/metadata/android/de-DE/` - German locale created
- [x] Firebase config already at `vector-app/src/gplay/release/google-services.json`
- [x] App name set to "HealthChat" in build.gradle

### Phase 3: Build Configuration
- [x] Release keystore created: `vector-app/signature/healthchat-release.keystore`
  - Alias: `healthchat-upload`
  - Password: `healthchat-upload-2024`
- [x] Signing enabled in `vector-app/build.gradle` (line 268)
- [x] `gradle.properties` configured with signing credentials
- [x] JVM memory increased to 8GB for R8 optimization
- [x] Version suffix fixed for release branches (no -dev suffix)
- [x] AAB splits conflict fixed (auto-disables splits for bundle builds)

### Phase 4: Store Assets
- [x] `fastlane/private/` directory created for API key
- [x] Existing assets available:
  - `images/icon.png` (512x512)
  - `images/featureGraphic.png` (1024x500)
  - `images/phoneScreenshots/` (7 screenshots)
- [ ] **TODO:** Verify assets have HealthChat branding (may still be Element)

### Phase 5: Pre-publication Verification
- [x] Package name: `ch.healthchat.element`
- [x] App name: "HealthChat"
- [x] Version: 1.6.48 (no -dev suffix)
- [x] AAB signed with HEALTHCH.RSA

---

## Pending: Phase 6 - Play Store Publication

### Build Commands
```bash
# Build signed AAB for Play Store
./gradlew bundleGplayRelease

# Output: vector-app/build/outputs/bundle/gplayRelease/vector-app-gplay-release.aab

# Build signed APKs (for testing/sideloading)
./gradlew assembleGplayRelease
```

### Manual Steps Required

#### 1. Play Console API Key (for Fastlane automation)
1. Google Cloud Console → APIs & Services → Credentials
2. Create Service Account → Name: "healthchat-play-deploy"
3. Download JSON key → Save as `fastlane/private/healthchat-play-api-key.json`
4. Play Console → Users & permissions → Invite user
5. Add service account email with "Release Manager" role

#### 2. Create App in Play Console
1. Go to https://play.google.com/console
2. Create app → Name: "HealthChat"
3. Category: Communication or Medical
4. Default language: German (de-DE)

#### 3. Set Up Play App Signing
1. Release → Setup → App signing
2. Choose "Let Google manage my app signing key"
3. Export upload key certificate if needed:
   ```bash
   keytool -export -alias healthchat-upload \
     -keystore vector-app/signature/healthchat-release.keystore \
     -file healthchat-upload.der
   ```

#### 4. Upload AAB
```bash
# Manual upload via Play Console:
# Release → Internal testing → Create new release → Upload AAB

# Or with Fastlane (once API key configured):
cd fastlane
bundle exec fastlane supply --track internal \
  --aab ../vector-app/build/outputs/bundle/gplayRelease/vector-app-gplay-release.aab
```

#### 5. Complete Store Listing
- [ ] Privacy Policy URL (required)
- [ ] Content Rating questionnaire
- [ ] App access declarations
- [ ] Contact email/phone
- [ ] Set pricing (Free)

#### 6. Submit for Review
1. Internal testing first (1-2 days review)
2. Test with internal testers
3. Promote to Production when ready

---

## Key Files Modified

| File | Change |
|------|--------|
| `gradle.properties` | Signing config, memory settings |
| `vector-app/build.gradle` | Signing enabled, version suffix, AAB splits fix |
| `vector-app/signature/healthchat-release.keystore` | New upload keystore |
| `fastlane/Appfile` | Package name updated |
| `fastlane/metadata/android/en-US/*` | HealthChat metadata |
| `fastlane/metadata/android/de-DE/*` | German locale |

---

## Important Notes

### Keystore Security
The keystore password (`healthchat-upload-2024`) is stored in `gradle.properties`.
For production, consider:
- Using environment variables
- Using a secrets manager
- Storing credentials in `~/.gradle/gradle.properties` (not in repo)

### Store Assets
Current assets in `fastlane/metadata/android/en-US/images/` may still have Element branding.
Replace with HealthChat-branded versions before production release.

### Version Management
- Version name auto-generated from `versionMajor.versionMinor.versionPatch`
- Release branches (main, release/*) get clean version (e.g., "1.6.48")
- Other branches get "-dev" suffix (e.g., "1.6.48-dev")

---

## Quick Reference

```bash
# Build AAB for Play Store
./gradlew bundleGplayRelease

# Build APKs for testing
./gradlew assembleGplayRelease

# Clean build
./gradlew clean bundleGplayRelease

# Check version
cat vector-app/build/outputs/bundle/gplayRelease/output-metadata.json | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['elements'][0]['versionName'])"
```
