# Block Booster

A polished offline block-placement puzzle game for Android.

## Gameplay

- 8x8 puzzle board.
- Select one of three generated pieces, then tap a board position to place it.
- Complete rows and columns to clear them.
- Earn combo bonuses for consecutive clears.
- Local high-score persistence.
- Game-over detection and one-tap restart.
- Vibration feedback for placement and line clears.
- No account or network access required in v1.0.0.

## Technology

- Kotlin
- Jetpack Compose + Material 3
- Android Gradle Plugin 9.4.0
- Kotlin 2.4.10
- Compose BOM 2026.09.00
- minSdk 23 / targetSdk 36
- Java 17

The current Android tooling versions follow the September 2026 Android/Compose documentation. See the official Android developer documentation for compatibility details.

## Build

Open the repository in a current Android Studio release, allow Gradle to sync, then run the `app` configuration on an emulator or physical device.

For a Play release, configure a private release keystore and build a signed Android App Bundle (AAB). Never commit signing keys or passwords to GitHub.

## Play Store preparation

See `PLAY_STORE_CHECKLIST.md` for the release checklist and `privacy-policy.md` for the initial privacy policy.

The production application ID is currently `com.riere.blockbooster`. If you want to publish under a different permanent ID, change it before the first Play Store release.

## Repository

https://github.com/Riere02/android-puzzle-game---Block-Booster
