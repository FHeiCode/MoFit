# MoFit

Android app written in Kotlin with Jetpack Compose — ein Trainings-Tracker im dunklen Neon-Look.

## Features

- **Heute** – der Plan für den aktuellen Wochentag, Fortschrittsbalken, Sätze per Bottom-Sheet erfassen, Wochen-Streak
- **Plan** – Wochenplan: welche Übung an welchem Tag, mit Sätzen, Wiederholungen und Gewicht
- **Fortschritt** – pro Übung: Rekord, geschätztes 1RM, Diagramm (Gewicht / Wdh / Volumen) und Verlauf
- **Übungen** – eigene Übungsarten mit Muskelgruppe (Brust, Rücken, Beine, Schultern, Bizeps, Trizeps, Unterarm, Bauch, Cardio), Emoji, Farbe und Körpergewicht-Option

Daten liegen lokal als JSON-Datei im App-Speicher und bleiben bei Updates erhalten.

## Tech stack

- Kotlin 2.2, Jetpack Compose (Material 3)
- Gradle 8.14 (Kotlin DSL) with a version catalog in `gradle/libs.versions.toml`
- minSdk 26, target/compileSdk 36, Java 17

## Building locally

```bash
./gradlew assembleDebug            # debug APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest        # unit tests
```

Or open the project in Android Studio.

## Automatic releases

Every commit pushed to `main` triggers `.github/workflows/android.yml`, which:

1. runs the unit tests,
2. builds a **signed release APK**,
3. publishes it as a GitHub Release tagged `v1.0.<run number>` with the APK attached.

Pull requests only build and test; they never publish a release.

### Installing as an update (no uninstall needed)

Android replaces an installed app in place only when the new APK

- is signed with **the same key** as the installed one, and
- has a **higher `versionCode`**.

Both are handled by CI:

- `versionCode` = the workflow's run number, which only ever increases.
  `versionName` = `1.0.<run number>`. (Renaming or recreating the workflow file
  resets the run number — if that happens, add an offset in `app/build.gradle.kts`.)
- The APK is always signed with the release keystore stored in the repository secrets.

Debug builds from Android Studio use a different (debug) key, so switching between
a debug build and a release build on the same phone requires one uninstall.

### Release signing

The workflow needs four repository secrets
(*Settings → Secrets and variables → Actions*):

| Secret | Value |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | the `.jks` keystore, base64-encoded (`base64 -w0 release.jks`) |
| `SIGNING_STORE_PASSWORD` | keystore password |
| `SIGNING_KEY_ALIAS` | key alias |
| `SIGNING_KEY_PASSWORD` | key password |

If they are missing, the release job fails with a clear error instead of producing
an APK signed with a throwaway key.

**Back up the keystore.** If it is lost, new builds can no longer update installed copies.
Never commit it — `*.jks` and `*.keystore` are git-ignored.
