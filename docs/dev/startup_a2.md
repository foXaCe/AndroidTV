# Startup A2 — SplashScreen API (core-splashscreen)

**Date** : 2026-03-09
**Device** : Ugoos AM9 Pro (192.168.1.152:5555)
**Build** : githubRelease (depuis HEAD main + working tree)
**Baseline** : startup_a1.md (cold start moyen 545ms)

---

## 1. Changements

### Dependance ajoutee
```toml
# gradle/libs.versions.toml
androidx-core-splashscreen = "1.0.1"
androidx-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "androidx-core-splashscreen" }
```
```kotlin
# app/build.gradle.kts
implementation(libs.androidx.splashscreen)
```

### Theme splash cree
```xml
<!-- res/values/theme_jellyfin.xml -->
<style name="Theme.Jellyfin.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/ds_background</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/vegafox_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.Jellyfin</item>
</style>
```

### Manifest
```xml
<!-- AndroidManifest.xml — StartupActivity -->
android:theme="@style/Theme.Jellyfin.Splash"
```

### installSplashScreen()
```kotlin
// StartupActivity.kt — premiere ligne de onCreate()
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()  // ← avant tout
    applyTheme()
    super.onCreate(savedInstanceState)
    // ...
}
```

---

## 2. Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `gradle/libs.versions.toml` | +version `androidx-core-splashscreen`, +library `androidx-splashscreen` |
| `app/build.gradle.kts` | +`implementation(libs.androidx.splashscreen)` |
| `res/values/theme_jellyfin.xml` | +style `Theme.Jellyfin.Splash` |
| `AndroidManifest.xml` | StartupActivity: +`android:theme="@style/Theme.Jellyfin.Splash"` |
| `StartupActivity.kt` | +import `installSplashScreen`, +appel `installSplashScreen()` en premier dans `onCreate()` |

---

## 3. Mesures Cold Start (release, force-stop only)

### A1 baseline (windowBackground fix)

| Run | LaunchState | TotalTime (ms) |
|-----|-------------|-----------------|
| 2   | COLD        | 522             |
| 3   | COLD        | 526             |
| 4   | COLD        | 567             |
| 5   | COLD        | 565             |
| 6   | COLD        | 547             |

**Moyenne cold start A1** : **545ms**

### A2 (avec SplashScreen API)

| Run | LaunchState | TotalTime (ms) |
|-----|-------------|-----------------|
| 1   | COLD        | 457             |
| 2   | COLD        | 535             |
| 3   | COLD        | 469             |
| 4   | COLD        | 452             |
| 5   | COLD        | 438             |

**Moyenne cold start A2** : **470ms**

### Logcat Displayed (A2)
```
Displayed StartupActivity: +535ms
Displayed StartupActivity: +469ms
Displayed StartupActivity: +452ms
Displayed StartupActivity: +438ms
```

### Comparaison

| Metrique | Baseline (A0) | A1 (windowBg) | A2 (SplashScreen) | Delta A1→A2 | Delta total |
|----------|---------------|---------------|-------------------|-------------|-------------|
| Cold start moyen | 656ms | 545ms | 470ms | **-75ms (-14%)** | **-186ms (-28%)** |

---

## 4. Test visuel AM9 Pro

| Critere | Resultat |
|---------|----------|
| Zero flash noir/blanc au demarrage | **PASS** |
| Logo VegafoX visible pendant le chargement | **PASS** |
| Transition fluide launcher → app | **PASS** |
| Splash natif ne reste pas apres contenu Compose pret | **PASS** |

---

## 5. Resume

- **SplashScreen API** integree : `core-splashscreen:1.0.1`
- **Theme `Theme.Jellyfin.Splash`** cree avec fond `ds_background` et icone `vegafox_launcher_foreground`
- **`installSplashScreen()`** appele en premier dans `onCreate()` de StartupActivity
- **Cold start** : 545ms → 470ms (**-14%**, cumul -28% depuis baseline)
- **Test visuel** : PASS sur les 4 criteres
- **Build** : OK (assembleGithubRelease)
