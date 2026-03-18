# Audit — Refresh Rate Switching Behavior

**Date** : 2026-03-16
**Scope** : Vérifier que la préférence `refreshRateSwitchingBehavior` est correctement respectée (notamment `DISABLED`).

---

## Point 1 — Préférence `refreshRateSwitchingBehavior`

### Définition

| Fichier | Ligne |
|---------|-------|
| `preference/UserPreferences.kt` | 206 |

```kotlin
var refreshRateSwitchingBehavior = enumPreference(
    "refresh_rate_switching_behavior",
    RefreshRateSwitchingBehavior.DISABLED
)
```

- **Clé SharedPreferences** : `"refresh_rate_switching_behavior"`
- **Type** : `RefreshRateSwitchingBehavior` (enum)
- **Valeur par défaut** : `DISABLED`

### Enum `RefreshRateSwitchingBehavior`

| Fichier | `preference/constant/RefreshRateSwitchingBehavior.kt` |
|---------|------------------------------------------------------|

| Valeur | String resource | Signification |
|--------|----------------|---------------|
| `DISABLED` | `R.string.state_disabled` | Pas de changement de refresh rate |
| `SCALE_ON_TV` | `R.string.pref_refresh_rate_scale_on_tv` ("Scale on TV") | Cherche le mode display le plus proche du framerate source, y compris résolution différente |
| `SCALE_ON_DEVICE` | `R.string.pref_refresh_rate_scale_on_device` ("Scale on device") | Privilégie la résolution native, ne change que le refresh rate |

**Verdict** : L'enum est bien définie avec 3 valeurs claires. `DISABLED` = aucun changement.

---

## Point 2 — Lecture de la préférence dans le player

### Toutes les références trouvées

#### 1. `PlaybackController.kt:127` — Champ privé

```kotlin
private var refreshRateSwitchingBehavior = RefreshRateSwitchingBehavior.DISABLED
```

Initialisation par défaut à `DISABLED`.

#### 2. `PlaybackController.kt:137` — Init bloc

```kotlin
refreshRateSwitchingBehavior = userPreferences[UserPreferences.refreshRateSwitchingBehavior]
if (refreshRateSwitchingBehavior != RefreshRateSwitchingBehavior.DISABLED) {
    getDisplayModes()
}
```

La préférence est lue depuis `UserPreferences` au démarrage du `PlaybackController`. Si `DISABLED`, `getDisplayModes()` n'est **pas** appelé → la liste `mDisplayModes` reste `null`.

#### 3. `PlaybackController.kt:473-476` — `findBestDisplayMode()`

```kotlin
refreshRateSwitchingBehavior == RefreshRateSwitchingBehavior.SCALE_ON_DEVICE && ...
refreshRateSwitchingBehavior == RefreshRateSwitchingBehavior.SCALE_ON_TV
```

Utilisé pour déterminer le scoring des modes d'affichage. Cette méthode n'est appelée que depuis `setRefreshRate()`.

#### 4. `PlaybackController.kt:914` — Avant l'appel à `setRefreshRate()`

```kotlin
if (refreshRateSwitchingBehavior != RefreshRateSwitchingBehavior.DISABLED) {
    setRefreshRate(response.mediaSource?.getVideoStream())
}
```

**Guard clause correcte** : `setRefreshRate()` n'est appelé que si la préférence n'est pas `DISABLED`.

#### 5. Écrans de settings (lecture/écriture UI uniquement)

| Fichier | Ligne | Usage |
|---------|-------|-------|
| `SettingsPlaybackRefreshRateSwitchingBehaviorScreen.kt` | 24, 37, 39 | Affichage et modification de la préférence (écran dédié) |
| `SettingsPlaybackAdvancedScreen.kt` | 189, 193 | Affichage de la valeur courante (caption) |
| `SettingsMainScreen.kt` | 469, 472 | Affichage de la valeur courante (caption) |

Ces fichiers n'affectent pas le comportement du player.

**Verdict** : La préférence est lue correctement et les guard clauses `!= DISABLED` sont présentes aux deux endroits critiques (init et playback start).

---

## Point 3 — Mécanisme de changement de refresh rate

### Unique point de changement : `setRefreshRate()` (PlaybackController.kt:501-538)

```kotlin
private fun setRefreshRate(videoStream: MediaStream?) {
    // ...
    val best = findBestDisplayMode(videoStream)
    if (best != null) {
        if (current.modeId != best.modeId) {
            val params: WindowManager.LayoutParams = frag.requireActivity().window.attributes
            params.preferredDisplayModeId = best.modeId    // ← ligne 530
            frag.requireActivity().window.attributes = params
        }
    }
}
```

| Aspect | Détail |
|--------|--------|
| **Fichier** | `PlaybackController.kt:530` |
| **API Android** | `WindowManager.LayoutParams.preferredDisplayModeId` |
| **Protection** | Oui — appelé uniquement si `refreshRateSwitchingBehavior != DISABLED` (ligne 914) |
| **Appelant unique** | Ligne 915 : `setRefreshRate(response.mediaSource?.getVideoStream())` |

### Autres mécanismes vérifiés

| Mécanisme | Trouvé ? |
|-----------|----------|
| `Surface.setFrameRate()` (API 30+) | **NON** |
| `Surface.FRAME_RATE_COMPATIBILITY_*` | **NON** |
| `Display.Mode` API directe (hors PlaybackController) | **NON** |
| ExoPlayer `videoFrameRateHint` | **NON** |
| Module `playback/media3/exoplayer/ExoPlayerBackend.kt` | **Aucune** référence au refresh rate |
| `playback/core/PlayerState.kt` | **Aucune** référence au refresh rate |

**Verdict** : Le seul point de changement de refresh rate est `PlaybackController.kt:530`, et il est correctement protégé par la guard clause à la ligne 914.

---

## Point 4 — AndroidManifest.xml

Recherche de `preferredRefreshRate` et `preferredDisplayModeId` dans le manifest :

**Résultat : AUCUNE occurrence trouvée.**

Il n'y a aucun attribut XML dans le manifest qui forcerait un changement de refresh rate au niveau Activity. Le manifest ne contient aucune déclaration de fréquence d'affichage préférée.

**Verdict** : Pas de forçage manifest.

---

## Point 5 — Cause probable

### Analyse du code : aucun bug trouvé dans le code VegafoX

Le code respecte correctement la préférence `DISABLED` :

1. **Init** (L137-140) : si `DISABLED`, les modes d'affichage ne sont même pas listés → `mDisplayModes` = `null`
2. **Playback start** (L914) : guard clause `!= DISABLED` avant `setRefreshRate()`
3. **`findBestDisplayMode()`** : retournerait `null` de toute façon si `mDisplayModes` est `null`
4. **`setRefreshRate()`** : ne fait rien si `findBestDisplayMode()` retourne `null`
5. **Manifest** : aucun forçage
6. **ExoPlayer/Media3** : aucune logique de refresh rate

### Causes probables hors code VegafoX

Si le refresh rate change malgré `DISABLED`, les causes possibles sont **externes au code de l'application** :

| # | Cause probable | Détail |
|---|---------------|--------|
| 1 | **Paramètre système Android TV** | "Match content frame rate" dans les Settings Android TV (Settings → Display → Match content frame rate). Ce paramètre est indépendant de l'app et agit au niveau framework. Sur Fire TV : Settings → Display & Sounds → Match Original Frame Rate. |
| 2 | **HDMI CEC / Auto Low Latency Mode** | Certains TV négocient automatiquement le refresh rate via HDMI 2.1 ALLM ou VRR. |
| 3 | **Paramètre TV** | Le téléviseur lui-même peut avoir un "Auto Frame Rate" ou "Cinema Mode" qui change la fréquence indépendamment du signal Android. |
| 4 | **Firmware Ugoos AM9** | Ugoos a parfois un "Automatic Frame Rate" dans ses propres settings (pas les settings Android standard). |

### Recommandation

Vérifier sur le boîtier Ugoos AM9 :
- **Android Settings** → Display → "Match content frame rate" → doit être sur "Never"
- **Ugoos Settings** (app Ugoos) → "Automatic Frame Rate (AFR)" → doit être désactivé
- **TV** → Désactiver tout "Auto Frame Rate" / "Cinema Mode"

---

## Résumé

| Point | Status | Détail |
|-------|--------|--------|
| 1 — Préférence | OK | Enum 3 valeurs, défaut `DISABLED` |
| 2 — Lecture player | OK | Guard clauses correctes aux 2 points critiques |
| 3 — Mécanisme changement | OK | Unique point (`L530`), correctement protégé |
| 4 — Manifest | OK | Aucun attribut refresh rate |
| 5 — Cause probable | **EXTERNE** | Paramètre système Android TV, firmware Ugoos, ou TV |

**Conclusion** : Le code VegafoX respecte correctement la préférence `DISABLED`. Si le refresh rate change malgré tout, la cause est un paramètre système (Android TV, firmware Ugoos, ou TV).
