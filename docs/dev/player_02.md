# Player Dialogs — Connexion PlaybackManager

## Statut : INSTALLE — En attente de tests manuels

Date : 2026-03-12

## Cartographie PlaybackManager

### API identifiee dans playback-core

| Fonctionnalite | Methode | Source |
|---|---|---|
| Changer vitesse | `playbackManager.state.setSpeed(speed: Float)` | PlayerState.kt:154 |
| Vitesse courante | `playbackManager.state.speed: StateFlow<Float>` | PlayerState.kt:69 |
| Seek (chapitres) | `playbackManager.state.seek(duration: Duration)` | PlayerState.kt:132 |
| Position courante | `playbackManager.state.positionInfo.active` | PlayerState.kt:83 |
| Play/Pause/Unpause | `playbackManager.state.play/pause/unpause()` | PlayerState.kt |
| Piste audio (liste) | `playbackManager.backend.getAudioTracks()` | **NOUVEAU** PlayerBackend.kt |
| Piste audio (select) | `playbackManager.backend.selectAudioTrack(index)` | **NOUVEAU** PlayerBackend.kt |
| Piste sous-titre (liste) | `playbackManager.backend.getSubtitleTracks()` | **NOUVEAU** PlayerBackend.kt |
| Piste sous-titre (select) | `playbackManager.backend.selectSubtitleTrack(index)` | **NOUVEAU** PlayerBackend.kt |
| Desactiver sous-titres | `playbackManager.backend.disableSubtitles()` | **NOUVEAU** PlayerBackend.kt |
| Buffering state | `playbackManager.state.isBuffering: StateFlow<Boolean>` | **NOUVEAU** PlayerState.kt |
| Qualite/bitrate | `userPreferences[UserPreferences.maxBitrate]` | Preference (direct play) |
| Zoom/aspect ratio | `userPreferences[UserPreferences.playerZoomMode]` | Preference (UI) |

### Choix d'architecture

- Les pistes audio/sous-titres viennent d'ExoPlayer (`currentTracks.groups`) et non de `BaseItemDto.mediaSources`
  - Avantage : on affiche ce qu'ExoPlayer detecte reellement dans le conteneur
  - Avantage : pas de mapping d'index Jellyfin → ExoPlayer
  - La selection utilise `TrackSelectionOverride` sur le track group ExoPlayer
- Qualite et Zoom sont des preferences utilisateur (pas d'impact en direct play)
- Le buffering utilise un vrai `Player.STATE_BUFFERING` d'ExoPlayer au lieu de `PlayState.STOPPED`

## Fichiers modifies

### playback-core module (3 fichiers)

1. **`playback/core/src/main/kotlin/backend/PlayerBackend.kt`**
   - Ajout de 5 methodes avec defauts : `getAudioTracks()`, `getSubtitleTracks()`, `selectAudioTrack(index)`, `selectSubtitleTrack(index)`, `disableSubtitles()`

2. **`playback/core/src/main/kotlin/model/PlayerTrack.kt`** (NOUVEAU)
   - Data class `PlayerTrack(index, label, language, codec, isSelected)`

3. **`playback/core/src/main/kotlin/backend/PlayerBackendEventListener.kt`**
   - Ajout `onBufferingChange(isBuffering: Boolean)` avec defaut vide

4. **`playback/core/src/main/kotlin/backend/BackendService.kt`**
   - Propagation de `onBufferingChange` aux listeners

5. **`playback/core/src/main/kotlin/PlayerState.kt`**
   - Ajout `isBuffering: StateFlow<Boolean>` dans l'interface et l'implementation

### playback-media3-exoplayer module (1 fichier)

6. **`playback/media3/exoplayer/src/main/kotlin/ExoPlayerBackend.kt`**
   - Implementation `getAudioTracks()` / `getSubtitleTracks()` — itere sur `exoPlayer.currentTracks.groups`
   - Implementation `selectAudioTrack(index)` / `selectSubtitleTrack(index)` — `TrackSelectionOverride`
   - Implementation `disableSubtitles()` — `setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)`
   - Emission `onBufferingChange` dans `onPlaybackStateChanged`

### app module (2 fichiers)

7. **`app/.../ui/player/video/VideoPlayerControls.kt`**
   - Dialog builders reecris : utilisent `playbackManager.backend.getXxxTracks()` au lieu de `BaseItemDto`
   - Callbacks connectes :
     - `onSelectSubtitle` → `backend.selectSubtitleTrack(index)` ou `backend.disableSubtitles()`
     - `onSelectAudio` → `backend.selectAudioTrack(index)`
     - `onSelectChapter` → `state.seek(ticks/10000 milliseconds)` avec position courante pour highlight
     - `onSelectQuality` → `userPreferences[maxBitrate] = key`
     - `onSelectSpeed` → `state.setSpeed(speed)` + vitesse courante lue via `state.speed`
     - `onSelectZoom` → `userPreferences[playerZoomMode] = mode`
   - Bouton Subtitles : `isPrimary = true` quand une piste est active
   - Bouton Speed : dialog pre-selectionne la vitesse courante

8. **`app/.../ui/player/video/VideoPlayerScreen.kt`**
   - Buffering indicator utilise `playbackManager.state.isBuffering` au lieu de `PlayState.STOPPED`

## Statut des dialogs

| Dialog | Donnees | Callback | Selection active | Statut |
|---|---|---|---|---|
| Subtitles | ExoPlayer tracks | `selectSubtitleTrack` / `disableSubtitles` | Checkmark + bouton OrangePrimary | CONNECTE |
| Audio | ExoPlayer tracks | `selectAudioTrack` | Checkmark | CONNECTE |
| Chapters | `BaseItemDto.chapters` | `state.seek(duration)` | Basee sur position courante | CONNECTE |
| Quality | Liste statique + pref | `userPreferences[maxBitrate]` | Basee sur pref courante | CONNECTE |
| Speed | `SpeedSteps` enum | `state.setSpeed(speed)` | Basee sur `state.speed` | CONNECTE |
| Zoom | `ZoomMode` enum | `userPreferences[playerZoomMode]` | Basee sur pref courante | CONNECTE |

## Build

- **Debug** : `assembleGithubDebug` BUILD SUCCESSFUL
- **Release** : `assembleGithubRelease` BUILD SUCCESSFUL
- **APK installee** sur Ugoos AM9 Pro (192.168.1.152:5555)

## Tests manuels

A effectuer :

1. [ ] Lancer un film — verifier buffering indicator au demarrage
2. [ ] Ouvrir dialog Audio — changer la piste → verifier que le son change
3. [ ] Ouvrir dialog Subtitles — activer FR → verifier sous-titres visibles
4. [ ] Ouvrir dialog Speed — passer a 1.5x → verifier acceleration
5. [ ] Ouvrir dialog Chapters — selectionner un chapitre → verifier seek
6. [ ] Ouvrir dialog Zoom — changer mode → verifier que la preference est sauvegardee
7. [ ] Ouvrir dialog Quality — changer bitrate → verifier preference sauvegardee
