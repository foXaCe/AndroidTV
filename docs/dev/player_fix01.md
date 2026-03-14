# Player Fix 01 — Bandes grises + Nouveau player inactif

## Statut : TERMINE

Date : 2026-03-12

## Bugs corriges

### BUG 1 — Bandes grises haut et bas

**Cause :** `VideoPlayerScreen.kt` utilisait `.background(JellyfinTheme.colorScheme.background)` comme fond du Box racine. `colorScheme.background` = `VegafoXColors.Background` = `Color(0xFF0A0A0F)` — bleu-noir tres sombre mais PAS noir pur. Les gradients du `PlayerOverlayLayout.kt` utilisent `VegafoXColors.BackgroundDeep` (`0xFF07070B`) avec alpha < 1. Quand l'alpha du gradient est < 1, le fond `0x0A0A0F` transparait a travers → apparence grisatre dans les zones de letterbox.

**Fix :** Remplace le fond par `Color.Black` (`0xFF000000`) — noir pur, opaque, standard pour les players video.

**Fichier modifie :** `app/.../ui/player/video/VideoPlayerScreen.kt`
- `.background(JellyfinTheme.colorScheme.background)` → `.background(Color.Black)`
- Import `JellyfinTheme` supprime (plus utilise)
- Import `Color` ajoute

### BUG 2 — Ratio image (zoom mode)

**Analyse :** Pas de bug dans le code. La valeur par defaut de `playerZoomMode` dans `UserPreferences.kt:365` est `ZoomMode.FIT` (mode normal, sans deformation). Le dialog Zoom dans `VideoPlayerControls.kt` lit et sauvegarde correctement la preference. Si le ratio etait modifie sur le device, c'etait une preference persistee lors de tests precedents.

**Fix :** Aucun changement necessaire.

### BUG 3 — Nouveau player ne s'ouvrait pas

**Cause :** `playbackRewriteVideoEnabled` avait un defaut `false` dans `UserPreferences.kt:253`. La preference n'avait jamais ete explicitement sauvee sur le device (absente des SharedPreferences), donc le code utilisait le defaut `false` → le vieux player (`CustomPlaybackOverlayFragment`) etait lance au lieu du nouveau (`VideoPlayerFragment`).

Le doc `player_01c.md` indiquait que ce defaut devait etre `true` mais le changement n'avait pas ete applique.

**Fix :** Change le defaut de `booleanPreference("playback_new", false)` → `booleanPreference("playback_new", true)`.

**Fichier modifie :** `app/.../preference/UserPreferences.kt`

## Fichiers modifies (resume)

| Fichier | Modification |
|---|---|
| `app/.../ui/player/video/VideoPlayerScreen.kt` | Fond Box racine `Color.Black` au lieu de `colorScheme.background` |
| `app/.../preference/UserPreferences.kt` | `playbackRewriteVideoEnabled` defaut `true` |

## Verification

- ExoPlayer demarre correctement : `Codec2-OutputBufferQueue`, `MediaStreamService`, `PlaybackInfo`
- Navigation : `NavigationRepositoryImpl` → `VideoPlayerFragment` (et non plus `CustomPlaybackOverlayFragment`)
- Overlay fonctionnel : header (titre, badges, horloge), seekbar (position/restant), boutons (play/pause, rewind, forward, skip, subtitles, audio, chapters, quality, speed, zoom)
- `screencap` ne capture pas le SurfaceView ExoPlayer (comportement normal Android)

## Build

- **Debug** : `assembleGithubDebug` BUILD SUCCESSFUL
- **Release** : `assembleGithubRelease` BUILD SUCCESSFUL
- **APK installee** sur Ugoos AM9 Pro (192.168.1.152:5555)

## Captures

- `docs/screenshots/player_fix01.png` — Overlay du nouveau player avec controles visibles
