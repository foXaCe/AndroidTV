# Player 03 — Suppression du legacy video player

## Objectif

Supprimer intégralement le legacy video player (`CustomPlaybackOverlayFragment` + Leanback) au profit du nouveau player Compose (`VideoPlayerFragment` + `PlaybackManager`).

## Fichiers supprimés (16 fichiers, 6 121 LOC)

| Fichier | LOC | Description |
|---------|-----|-------------|
| `PlaybackController.java` | 1 670 | Contrôleur legacy, piloté par le fragment |
| `CustomPlaybackOverlayFragment.java` | 1 427 | Fragment principal du legacy player |
| `VideoManager.java` | 810 | Gestion ExoPlayer pour le legacy player |
| `PlayerOverlayViewModel.kt` | 351 | ViewModel de l'overlay Compose legacy |
| `PlayerOverlayScreen.kt` | 346 | UI Compose de l'overlay legacy |
| `PlayerDialogs.kt` | 293 | Dialogs (audio, subtitle, speed, zoom, quality) legacy |
| `CustomPlaybackOverlayFragmentHelper.kt` | 252 | Extensions du fragment legacy |
| `LeanbackOverlayFragment.java` | 242 | Fragment enfant Leanback |
| `overlay_tv_guide.xml` | 210 | Layout XML du guide TV overlay |
| `VideoPlayerControllerImpl.kt` | 141 | Impl interface contrôleur legacy |
| `PlayerOverlayState.kt` | 137 | State + Actions sealed classes legacy |
| `SubtitleDelayHandler.java` | 131 | Gestion délai sous-titres (Java) |
| `VideoPlayerController.kt` | 53 | Interface contrôleur legacy |
| `PlayerOverlayComposeHelper.kt` | 28 | Bridge ComposeView legacy |
| `PlaybackOverlayFragmentHelper.kt` | 20 | Helper screensaver lock |
| `ic_playback_speed.xml` | 10 | Drawable speed (non utilisé) |

## Fichiers modifiés

### Navigation & Préférences

- **`Destinations.kt`** : Suppression de `videoPlayer()` legacy, renommage `videoPlayerNew()` -> `videoPlayer()` pointant vers `VideoPlayerFragment`
- **`PlaybackLauncher.kt`** : Suppression du branchement `if/else` basé sur `playbackRewriteVideoEnabled`. Toujours `Destinations.videoPlayer(position)`
- **`UserPreferences.kt`** : Suppression de la préférence `playbackRewriteVideoEnabled`
- **`SettingsDeveloperScreen.kt`** : Suppression du toggle "Playback Rewrite"

### Adaptations du code conservé

- **`PlaybackController.kt`** : Paramètre `fragment` changé de `CustomPlaybackOverlayFragment?` -> `Fragment?`. Suppression de 15 appels à des méthodes spécifiques au fragment legacy (`setFadingEnabled`, `closePlayer`, `updateDisplay`, etc.)
- **`PlaybackControllerHelper.kt`** : Suppression des appels `clearSkipOverlay()` et `askToSkip()` legacy
- **`VideoManager.kt`** : Remplacement du paramètre `PlaybackOverlayFragmentHelper` par deux lambdas (`onScreensaverLock`, `onFatalError`)
- **`PlayerDialogDefaults.kt`** : Ajout des data classes `TrackOption`, `QualityOption`, `SpeedOption`, `ZoomOption` (extraites de `PlayerOverlayState.kt` supprimé)
- **`vlc_player_interface.xml`** : Simplifié au minimum (seul `PlayerView#exoPlayerView` conservé) car `VideoManager` y fait encore référence

## Vérification zéro-référence

```
grep -r "CustomPlaybackOverlayFragment\|LeanbackOverlayFragment\|PlayerOverlayScreen\|
PlayerOverlayViewModel\|PlayerOverlayComposeHelper\|PlayerOverlayState\|
playbackRewriteVideoEnabled\|videoPlayerNew" --include="*.kt" --include="*.java"
→ 0 résultats
```

Seules références restantes : `lint-baseline.xml` (fichiers `.java` historiques, inoffensif).

## Test manuel

- **Device** : Ugoos AM9 Pro (192.168.1.152:5555)
- **Build debug** : `vegafox-androidtv-v1.6.2-github-debug.apk` (55 Mo) — BUILD SUCCESSFUL
- **Build release** : `vegafox-androidtv-v1.6.2-github-release.apk` (32 Mo) — BUILD SUCCESSFUL
- **Installation** : debug + release installés avec succès
- **Lecture vidéo** : Lancée depuis page détail "Marshals" -> `VideoPlayerFragment` actif (confirmé via `dumpsys activity`)
- **Codec** : Dolby Vision détecté et actif dans les logs
- **Pas de crash** : Aucune exception dans logcat

## Architecture résultante

```
Playback vidéo (nouveau) :
  PlaybackLauncher → Destinations.videoPlayer() → VideoPlayerFragment
    → PlaybackManager (playback-core)
    → ExoPlayerBackend (playback/media3)
    → UI 100% Compose (VideoPlayerScreen, VideoPlayerOverlay, VideoPlayerControls)

Code legacy conservé (non supprimable) :
  PlaybackController.kt — encore utilisé via PlaybackControllerContainer par :
    - SocketHandler (commandes de lecture distantes)
    - SdkPlaybackHelper (reporting de progression)
    - InteractionTrackerViewModel
  VideoManager.kt — référencé par PlaybackController
  vlc_player_interface.xml — référencé par VideoManager (simplifié)
```

## Prochaines étapes

- Migrer `SocketHandler` / `SdkPlaybackHelper` vers `PlaybackManager` pour pouvoir supprimer `PlaybackController` + `VideoManager`
- Supprimer `vlc_player_interface.xml` une fois `VideoManager` supprimé
- Appliquer le theming VegafoX aux fragments `NextUpFragment` et `StillWatchingFragment`
