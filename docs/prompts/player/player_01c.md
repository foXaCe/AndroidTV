# Player Redesign — Option A Ultra Minimal Premium

## Statut : TERMINE

Date : 2026-03-11

## Fichiers modifies

### FICHIER 1 — VideoPlayerControls.kt
`app/src/main/java/org/jellyfin/androidtv/ui/player/video/VideoPlayerControls.kt`

Rewrite complet :
- **Seekbar** : hauteur 4dp repos, 8dp focus (etait 3dp/8dp)
- **Temps au-dessus** : position 13sp Bold Monospace a gauche, restant "-HH:MM:SS" 13sp a droite
- **PlayerActionBtn** : composable bouton joueur avec `isPrimary` (fond OrangePrimary) et `enabled`
- **Boutons primaires** : Play/Pause (orange, autoFocus), Replay10, Forward30, SkipPrevious (conditionnel), SkipNext (conditionnel)
- **Divider vertical** : 1dp x 28dp, VegafoXColors.Divider
- **Boutons secondaires** : Subtitles, Audio, Chapters, Quality, Speed, Zoom — ouvrent des dialogs
- **Dialog state** : `VideoPlayerDialogType?` + `VideoPlayerDialogHost`
- **Supprime** : MoreOptionsButton, Popover

### FICHIER 2 — VideoPlayerHeader.kt
`app/src/main/java/org/jellyfin/androidtv/ui/player/video/VideoPlayerHeader.kt`

- **Fix bug titre** : affichait `seriesName` au lieu de `item.name` pour le titre principal des episodes
- **Layout** : serie 13sp OrangePrimary, titre 36sp Bold TextPrimary, episode 13sp TextSecondary
- **Badges** : [4K] [UHD] [HEVC] [MKV] [5.1] [EAC3] — OrangePrimary pour resolution, Gold pour HDR, TextHint pour tech
- **Horloge** : 15sp Monospace TextSecondary + "Se termine a HH:MM" 12sp TextHint

### PlayerHeader.kt (correctif associe)
`app/src/main/java/org/jellyfin/androidtv/ui/player/base/PlayerHeader.kt`

- Fix `content` lambda : `@Composable () -> Unit` → `@Composable RowScope.() -> Unit` pour supporter `Modifier.weight()`

### FICHIER 3 — PlayerOverlayLayout.kt (deja fait, verifie)
`app/src/main/java/org/jellyfin/androidtv/ui/player/base/PlayerOverlayLayout.kt`

- Gradient top : `VegafoXColors.BackgroundDeep.copy(alpha = 0.90f)` → transparent (35%)
- Gradient bottom : transparent → `VegafoXColors.BackgroundDeep.copy(alpha = 0.95f)` (45%)

### FICHIER 4 — SkipOverlayView.kt (deja fait, verifie)
`app/src/main/java/org/jellyfin/androidtv/ui/playback/overlay/SkipOverlayView.kt`

- Style VegafoX : fond `OrangePrimary.copy(alpha=0.15f)`, bordure `1.5dp OrangePrimary`, texte/icone OrangePrimary

### FICHIER 5 — VideoPlayerScreen.kt (buffering)
`app/src/main/java/org/jellyfin/androidtv/ui/player/video/VideoPlayerScreen.kt`

- **Buffering indicator** : `CircularProgressIndicator` centre, couleur `OrangePrimary`, 48dp, AnimatedVisibility fade
- Affiche quand `PlayState.STOPPED` (chargement initial)

### VideoPlayerOverlay.kt (connexion)
`app/src/main/java/org/jellyfin/androidtv/ui/player/video/VideoPlayerOverlay.kt`

- Passe `item` aux `VideoPlayerControls` pour alimenter les dialogs

### VideoPlayerDialogs.kt (deja existant)
`app/src/main/java/org/jellyfin/androidtv/ui/player/video/VideoPlayerDialogs.kt`

- Sealed class `VideoPlayerDialogType` : Subtitles, Audio, Chapters, Quality, Speed, Zoom
- `VideoPlayerDialogHost` route vers les composables dialog individuels

### MediaToast.kt (deja fait, verifie)
`app/src/main/java/org/jellyfin/androidtv/ui/player/base/toast/MediaToast.kt`

- Fond `Color(0xBF060A0F)`, icone `VegafoXColors.TextPrimary`, bordure 1dp blanc 8%

### UserPreferences.kt
`app/src/main/java/org/jellyfin/androidtv/preference/UserPreferences.kt`

- `playbackRewriteVideoEnabled` default change de `false` a `true` pour activer le nouveau player par defaut

## Architecture

```
VideoPlayerScreen
  +-- PlayerSurface (ExoPlayer)
  +-- VideoPlayerOverlay
  |     +-- PlayerOverlayLayout (gradients + animations)
  |           +-- VideoPlayerHeader (titre, badges, horloge)
  |           +-- VideoPlayerControls (seekbar, boutons, dialogs)
  +-- PlayerSubtitles
  +-- BufferingIndicator (CircularProgressIndicator)
  +-- MediaToasts
```

## Captures d'ecran

- `docs/screenshots/player_v2_controls.png` — Overlay complet avec header + controles + seekbar
- `docs/screenshots/player_v2_speed_dialog.png` — Dialog vitesse de lecture ouvert

## Build & Install

- **Variante** : githubDebug
- **APK** : `vegafox-androidtv-v1.6.2-github-debug.apk`
- **Device** : Ugoos AM9 Pro (192.168.1.152:5555)
- **Resultat** : BUILD SUCCESSFUL, installe et teste avec lecture Monarch S01E08

## TODO restants

- Connecter les callbacks dialog au PlaybackManager (track selection, quality, speed, zoom)
- Connecter l'indicateur de buffering a un vrai state buffering (actuellement base sur PlayState.STOPPED)
- Tester la navigation D-pad complete entre les boutons secondaires
- Style NextUp fragment (theme VegafoX)
