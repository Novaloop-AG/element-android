## Unreleased

🐛 Bugfixes

- Lock the login screen to the HealthChat homeserver and enforce SSO-only authentication client-side. The homeserver-edit control is hidden and any homeserver that does not advertise SSO is treated as unsupported, so a password form can never be reached.

## Changes in 1.6.4805 (2026-04-21)

🙌 Improvements

- Introduce the automated HealthChat release pipeline: tag-triggered Play Store upload with release notes sourced from this file.
