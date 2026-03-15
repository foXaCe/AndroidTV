# Renaming Moonfin → VegafoX

**Date:** 2026-03-09

## Package ID
- `org.moonfin.androidtv` → `com.vegafox.androidtv`
- `applicationId` updated in `app/build.gradle.kts`
- Provider authorities use `${applicationId}` — auto-resolved

## App Name
- `app_name_release`: "Moonfin" → "VegafoX"
- `app_name_debug`: "Moonfin Debug" → "VegafoX Debug"
- All user-visible strings updated in `values/strings.xml` and `values-fr/strings.xml`

## Project Config
- `settings.gradle.kts`: rootProject.name "Moonfin-androidtv" → "VegafoX-androidtv"
- `gradle.properties`: `moonfin.version` → `vegafox.version`
- `buildSrc/VersionUtils.kt`: env var `MOONFIN_VERSION` → `VEGAFOX_VERSION`
- `keystore.properties`: storeFile updated to `vegafox-release.jks`
  - Note: storePassword/keyAlias/keyPassword remain unchanged (baked into JKS)

## Resource Files Renamed (28 files)
- `ic_moonfin.xml` → `ic_vegafox.xml`
- `ic_moonfin_white.xml` → `ic_vegafox_white.xml`
- `moonfin_launcher_background.xml` → `vegafox_launcher_background.xml`
- `moonfin_ic_channel_background.xml` → `vegafox_ic_channel_background.xml`
- `moonfin_ic_banner_background.xml` → `vegafox_ic_banner_background.xml`
- All `moonfin_launcher*.webp` → `vegafox_launcher*.webp` (hdpi/mdpi/xhdpi/xxhdpi/xxxhdpi)
- All `moonfin_ic_banner*.xml/png` → `vegafox_ic_banner*.xml/png`
- All `moonfin_ic_channel*.xml/png` → `vegafox_ic_channel*.xml/png`
- All `moonfin_launcher*.xml` → `vegafox_launcher*.xml` (anydpi-v26)
- Root: `moonfin_launcher-playstore.png` → `vegafox_launcher-playstore.png`

## Kotlin Files Renamed (15 files)
- `MoonfinJellyseerrModels.kt` → `VegafoXJellyseerrModels.kt`
- `MoonfinProxyConfig.kt` → `VegafoXProxyConfig.kt`
- 13x `SettingsMoonfin*.kt` → `SettingsVegafoX*.kt`

## Directory Renamed
- `ui/settings/screen/moonfin/` → `ui/settings/screen/vegafox/`

## Keystore Files Renamed
- `moonfin-release.jks` → `vegafox-release.jks` (root + app/)

## Route Constants
- All `Routes.MOONFIN_*` → `Routes.VEGAFOX_*`

## Content Replacement Summary
- 98 source files had content replaced
- Patterns: `org.moonfin` → `com.vegafox`, `Moonfin` → `VegafoX`, `moonfin` → `vegafox`, `MOONFIN` → `VEGAFOX`

## Verification
- `grep moonfin` → 0 results (except keystore credentials in keystore.properties)
- `find -name "*moonfin*"` → 0 results
- BUILD SUCCESSFUL (assembleGithubDebug)
- APK: `vegafox-androidtv-v1.6.2-github-debug.apk`
- Package: `com.vegafox.androidtv.debug` installed on AM9 Pro
- App starts normally, no crash
