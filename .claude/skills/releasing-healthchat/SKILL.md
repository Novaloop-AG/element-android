---
name: releasing-healthchat
description: Cut a HealthChat Google Play release of element-android by tagging release/healthchat. Use when the user asks to release, publish, tag, deploy, ship, or cut a new HealthChat Android version, asks about the Play Store release process, asks about rotating the Android release keystore, or asks about the required GitHub Actions secrets for the release pipeline.
---

# Releasing HealthChat (element-android)

Releases are driven by git tags of the form `healthchat-v<semver>` pushed to
`origin`. The tag triggers `.github/workflows/healthchat-release-playstore.yml`,
which runs on `ubuntu-latest`, builds a signed AAB, and uploads it to the Play
Store **internal** track. No GitHub Release is created.

**Promotion to production is always a manual step** in Play Console after an
internal build has been validated — see "Promoting to production" below.

## Preflight checklist

Verify each item before tagging. If any fail, fix and commit first.

- [ ] On branch `release/healthchat` with a clean working tree (`git status`).
- [ ] `HEALTHCHAT_CHANGES.md` has a section `## Changes in <new-version> (<date>)`
      at the top of the file. The pipeline's awk extractor matches this exact
      heading format; no match means a fallback placeholder ships as the Play
      Store "What's new" text.
- [ ] `vector-app/build.gradle` `ext.versionMajor`, `ext.versionMinor`, and
      `ext.versionPatch` joined with dots equal the tag's core version. **The
      workflow hard-fails** if they don't match — this is load-bearing, not
      cosmetic. Patch bumps follow the odd/even convention in the gradle
      comment: **even = regular release, odd = hotfix**.
- [ ] If you edited `HEALTHCHAT_CHANGES.md` or `vector-app/build.gradle`, both
      commits are pushed to `release/healthchat` **before** you push the tag.

## Cutting the release (normal path)

```bash
# from release/healthchat, fully committed:
git tag healthchat-v<MAJOR>.<MINOR>.<PATCH>
git push origin healthchat-v<MAJOR>.<MINOR>.<PATCH>
```

Examples:

- `git tag healthchat-v1.6.4806`          (regular release, patch bumped +1 from 4805)
- `git tag healthchat-v1.7.0-rc1`         (release candidate)
- `git tag healthchat-v1.6.4807-hotfix1`  (hotfix on an existing patch)

Watch the workflow with `gh run watch` or in the GitHub Actions UI. When it
finishes, the build appears in Play Console → Internal testing within a few
minutes (Play Store's processing queue).

## Manual / retry path (workflow_dispatch)

Use when retrying a failed run or rebuilding without moving tags.

1. GitHub → Actions → "HealthChat Release to Google Play Store" → **Run workflow**.
2. Fill in:
   - **version** (required): e.g. `1.6.4806`. Same regex validation as tag path.
     Must still match `vector-app/build.gradle`.
   - **track**: default `internal`; pick `alpha`/`beta`/`production` to promote
     an already-uploaded build to a higher track without rebuilding (note:
     Play Console is usually a better UX for promotion).
   - **release_status**: default `completed`; use `draft` for dry-runs that
     land in Play Console but stay unpublished.
   - **release_notes_override** (optional): replaces the
     `HEALTHCHAT_CHANGES.md` lookup. Useful for ad-hoc test builds.
3. Run. The AAB is built and uploaded exactly as the tag path would.

## Tag format

```
healthchat-v<MAJOR>.<MINOR>.<PATCH>[-<suffix>]
```

Suffix conventions (informational; regex accepts any `[0-9A-Za-z.-]+`):

- `-rc1`, `-rc2` — release candidates
- `-hotfix1` — post-release fixes on an existing patch
- `-test` — one-off verification runs

The `healthchat-` prefix keeps these tags distinct from upstream element-hq
`v<semver>` tags.

Regex used for validation (must match):
`^[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$`

### About the patch number

The `versionPatch` value is a large integer (e.g. `4805`, `4806`) rather than
a small one, because Android's `versionCode` is derived deterministically from
it:

```
versionCode = ((major * 10000 + minor * 100 + patch) + 4_000_000) * 10
```

(See `vector-app/build.gradle:57` and the `applicationVariants.all` block at
line 201–212 — the `* 10` is the AAB's ABI multiplier, which the bundle uses
`0` for since splits are disabled when building AAB.)

`versionCode` must be strictly monotonic in Play Store across all uploads;
that's why patch is incremented rather than reset. **Even = regular release,
odd = hotfix** (see `vector-app/build.gradle:37–39`). Example sequence:
`4804` (regular) → `4805` (next regular) → `4806` (next regular), with `4805`
and `4807` reserved for hotfixes.

**Branch matters.** `generateVersionCodeFromVersionName()` only fires on
`main` or branches starting with `release/` (see `build.gradle:60–66`). Tags
pointing at commits on `release/healthchat` satisfy this; the workflow's
ref-verification step rejects any other branch anyway.

## Release notes

`HEALTHCHAT_CHANGES.md` is the single source. The workflow extracts the
section matching the version being released, then writes it to
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` and
`fastlane/metadata/android/de-DE/changelogs/<versionCode>.txt`, which
`r0adkll/upload-google-play` picks up via its `whatsNewDirectory` input.

Format:

```markdown
## Changes in 1.6.4806 (2026-04-25)

🙌 Improvements

- Bullet one.
- Bullet two.
```

Keep the entire section under **500 characters** — Play Store's "What's new"
hard cap per locale. The workflow emits a warning if you exceed it; Play Store
will truncate.

Never hand-edit the generated `fastlane/metadata/android/*/changelogs/<N>.txt`
files for this branch — they'll be overwritten on the next run. Only edit
`HEALTHCHAT_CHANGES.md`. (Older checked-in files from upstream are harmless.)

If no matching section exists, the pipeline emits a workflow warning and
falls back to `HealthChat <version> — automated build from <ref>.`. Prefer
fixing the changelog over relying on the fallback.

## After the release

- [ ] Workflow run is green in GitHub Actions.
- [ ] Build appears under Play Console → Internal testing within ~10 minutes.
- [ ] Internal testers get the update automatically if they're enrolled.
- [ ] AAB + `mapping.txt` are attached to the workflow run, retained 90 days.

## Required GitHub Actions secrets

The pipeline depends on these repository secrets. Missing any causes the
workflow to fail at the corresponding step.

| Secret | Source |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | `base64 -w0 release.keystore` (the HealthChat upload keystore) |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias inside the keystore |
| `RELEASE_KEY_PASSWORD` | Key password |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Play Console → Users and permissions → invite a service account → download JSON. Needs "Release manager" permissions scoped to `ch.healthchat.element`. |
| `GOOGLE_SERVICES_JSON_BASE64` | `base64 -w0 google-services.json` from the Firebase project configured for push notifications |

## Keystore rotation (rare, high-risk)

Unlike Apple Distribution certificates, **Android upload keystores do not
expire** and should not be rotated casually. The upload key is tied to the
app's identity in Play Console; rotating it is a one-way process that uses
Play App Signing's "Upload key reset" flow.

If the keystore is lost or believed to be compromised:

1. Play Console → Release → Setup → App integrity → Upload key → **Request
   upload key reset**. Apple-style instructions follow, requiring you to
   upload a new keystore and wait for Google to rotate.
2. Generate a fresh keystore on the operator's machine:
   ```bash
   keytool -genkey -v -keystore healthchat-upload-new.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias healthchat
   ```
3. Once Google confirms the rotation, re-encode and update secrets:
   `base64 -w0 healthchat-upload-new.jks | wl-copy`, then replace
   `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
   `RELEASE_KEY_PASSWORD` in repo settings.
4. Run the release workflow via `workflow_dispatch` with a throwaway version
   bump and `release_status: draft` to verify end-to-end. Discard the draft.

**Do not attempt rotation without reading Play App Signing docs end-to-end**
and coordinating a maintenance window. A botched rotation means users can't
receive updates until Google completes the reset.

## Promoting internal → production

Internal builds and production builds are the same AAB. To release a
validated internal build:

1. Play Console → HealthChat → Testing → **Internal testing** → find the
   release.
2. Confirm it passed internal testing.
3. **Promote release** → Production → **Release details** → fill in "What's
   new" (Play Console copies it from the internal release — verify) →
   **Review release** → **Start rollout to Production**.
4. Choose staged rollout % if desired.

**This step is deliberately not automated.** Production releases require
human judgment on rollout percentage, Play Store listing changes, and
compliance metadata review.

## Rollback

- **Bad build, release notes fine** → fix the cause, bump `versionPatch` to
  the next **odd** hotfix number, commit, tag `healthchat-v<ver>`. The new
  build supersedes the bad one on the internal track.
- **Bad release notes only** → edit `HEALTHCHAT_CHANGES.md` on
  `release/healthchat`, then update the "What's new" text in the Play Console
  release manually (the pipeline only writes it at upload time).
- **Completely wrong version number** → treat as burned. Bump `versionPatch`
  further, tag a higher `healthchat-v<ver>`. Play Store's `versionCode` rule
  means the next run naturally supersedes.
- **Production rollout gone wrong** → Play Console → Production → **Halt
  rollout** (works during staged rollout), then ship a hotfix as above.

## Known branch divergence on rebase

When rebasing `release/healthchat` onto upstream `develop` or `main`:

- **`.github/workflows/healthchat-release-playstore.yml`**,
  **`HEALTHCHAT_CHANGES.md`**, and
  **`.claude/skills/releasing-healthchat/SKILL.md`** are HealthChat-only —
  any rebase conflict here means something upstream accidentally overlaps;
  keep `--ours`.
- **`vector-app/build.gradle:148`** — `applicationId "ch.healthchat.element"`
  must survive rebases. Upstream uses `im.vector.app`.
- **`vector-app/build.gradle:254, 262`** — `resValue "string", "app_name",
  "HealthChat..."` must survive. Upstream uses `"Element..."`.
- **`vector-app/build.gradle:35–40`** — HealthChat's version numbers
  (`versionMajor`/`Minor`/`Patch`) diverge from upstream; keep `--ours` on
  rebase and resolve the `## Changes in …` changelog section of
  `HEALTHCHAT_CHANGES.md` separately (don't merge upstream's `CHANGES.md`
  into it).
- **`fastlane/metadata/android/*/changelogs/`** — the per-versionCode files
  generated by this pipeline carry HealthChat text; upstream regenerates
  them with upstream text. Conflicts here mean the same `versionCode`
  collided by coincidence — regenerate from `HEALTHCHAT_CHANGES.md` on the
  next release run rather than hand-merging.
