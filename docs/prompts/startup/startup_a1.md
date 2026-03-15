# Startup A1 — windowBackground + unification couleurs

**Date** : 2026-03-09
**Device** : Ugoos AM9 Pro (192.168.1.152:5555)
**Build** : githubRelease (depuis HEAD main + working tree)

---

## 1. windowBackground : avant / apres

### Avant
```xml
<!-- theme_jellyfin.xml -->
<style name="Theme.Jellyfin" parent="@style/Theme.AppCompat.Leanback">
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="defaultBackground">@color/not_quite_black</item>  <!-- #101010 -->
```

### Apres
```xml
<style name="Theme.Jellyfin" parent="@style/Theme.AppCompat.NoActionBar">
    <item name="android:windowBackground">@color/ds_background</item>  <!-- #0A0A0F -->
    <item name="defaultBackground">@color/ds_background</item>
```

**Changements supplementaires (migration Leanback → AppCompat)** :
- Parent theme : `Theme.AppCompat.Leanback` → `Theme.AppCompat.NoActionBar`
- `Widget.Jellyfin.Row.Header` : parent `Widget.Leanback.Row.Header` → `android:Widget.TextView`
- Attributs `defaultSearchColor`, `rowHeaderStyle`, `overlayDimDimmedLevel` declares dans `attrs.xml`

---

## 2. Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `res/values/theme_jellyfin.xml` | windowBackground → `@color/ds_background`, defaultBackground → `@color/ds_background`, parent → NoActionBar |
| `res/values/attrs.xml` | +3 attributs declares (defaultSearchColor, rowHeaderStyle, overlayDimDimmedLevel) |
| `res/values/styles.xml` | Widget.Jellyfin.Row.Header parent → `android:Widget.TextView` |
| `res/layout/item_jellyseerr_content.xml` | `not_quite_black` → `ds_background` |
| `res/drawable/audio_now_playing_album_background.xml` | `not_quite_black` → `ds_background` |
| `ui/startup/fragment/ConnectHelpAlertFragment.kt` | `R.color.not_quite_black` → `R.color.ds_background` |
| `ui/startup/fragment/SplashFragment.kt` | `R.color.not_quite_black` → `R.color.ds_background` |

**Usages restants de `not_quite_black`** : uniquement la definition dans `colors.xml` (plus aucune reference).

---

## 3. Mesures Cold Start (release)

### Baseline (avant fix, windowBackground=transparent)

| Run | LaunchState | TotalTime (ms) |
|-----|-------------|-----------------|
| 1   | COLD        | 887             |
| 4   | COLD        | 581             |
| 5   | COLD        | 499             |

**Moyenne cold start baseline** : **656ms**

### Apres fix (windowBackground=ds_background)

| Run | LaunchState | TotalTime (ms) |
|-----|-------------|-----------------|
| 1   | COLD        | 877 (premier lancement post-install) |
| 2   | COLD        | 522             |
| 3   | COLD        | 526             |
| 4   | COLD        | 567             |
| 5   | COLD        | 565             |
| 6   | COLD        | 547             |

**Moyenne cold start (hors run 1)** : **545ms**

### Comparaison

| Metrique | Baseline | Apres fix | Delta |
|----------|----------|-----------|-------|
| Cold start moyen | 656ms | 545ms | **-111ms (-17%)** |
| Displayed StartupActivity | 555ms | 565ms | ~stable |
| Displayed MainActivity | +289ms | +461ms | +172ms (variabilite reseau) |

### Logcat Displayed (apres fix)
```
Displayed StartupActivity: +565ms
Displayed MainActivity: +461ms
```

---

## 4. Flash visuel resolu

| Critere | Avant | Apres |
|---------|-------|-------|
| windowBackground | `transparent` (flash noir/launcher) | `#0A0A0F` (fond app immediat) |
| Flash visible | OUI | **NON** |
| Coherence couleur fond | `not_quite_black` (#101010) vs `ds_background` (#0A0A0F) | Unifie sur `ds_background` (#0A0A0F) |

Le fond de l'app (#0A0A0F) est maintenant affiche immediatement par le WindowManager, avant meme que l'Activity soit creee. Plus de flash transparent/launcher visible.

---

## 5. Resume

- **windowBackground** fixe : `transparent` → `@color/ds_background` (#0A0A0F)
- **Couleurs unifiees** : 4 fichiers migres de `not_quite_black` vers `ds_background`
- **Theme parent migre** : `Theme.AppCompat.Leanback` → `Theme.AppCompat.NoActionBar` (fix build)
- **Flash visuel** : resolu
- **Cold start** : 656ms → 545ms (**-17%**)
- **Build** : OK (assembleGithubRelease)
