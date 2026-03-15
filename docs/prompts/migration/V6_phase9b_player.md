# Phase 9b — Migration SeekProvider + VideoPlayerController

## Objectif

Supprimer les deux dernières classes player qui étendaient directement des classes Leanback simples :
- `CustomSeekProvider.kt` (extends `PlaybackSeekDataProvider`)
- `VideoPlayerAdapter.java` (extends `PlayerAdapter`)

## ÉTAPE 1 — CustomSeekProvider → SeekProvider

### Analyse

`CustomSeekProvider` étendait `PlaybackSeekDataProvider` (1 import Leanback) pour fournir :
- `getSeekPositions()` — positions de seek calculées depuis la durée et le pas de skip
- `getThumbnail()` — thumbnails trickplay avec préchargement intelligent
- `reset()` — nettoyage des ressources

La logique interne n'utilisait aucune API Leanback — seulement l'héritage de la classe de base.

### Migration

| Avant | Après |
|-------|-------|
| `CustomSeekProvider.kt` extends `PlaybackSeekDataProvider` | `SeekProvider.kt` — classe Kotlin pure, zéro Leanback |
| Prend `VideoPlayerAdapter` en paramètre | Prend `PlaybackController` directement |
| Utilisé via `playerGlue.setSeekProvider(new CustomSeekProvider(...))` | Adapté dans `CustomPlaybackTransportControlGlue.setSeekProvider(SeekProvider)` |

**Fichier créé :** `overlay/SeekProvider.kt`
- Même logique trickplay (disk cache, preload directionnel, placeholders)
- Interface callback `ThumbnailCallback` propre (remplace `PlaybackSeekDataProvider.ResultCallback`)
- Calcule `duration` directement depuis `PlaybackController`

**Adaptateur Leanback :** Méthode `setSeekProvider(SeekProvider)` ajoutée au Glue, crée un `PlaybackSeekDataProvider` anonyme qui délègue.

**Fichier supprimé :** `overlay/CustomSeekProvider.kt`

**Tests :** `SeekProviderTests.kt` — 3 tests migrés pour mocker `PlaybackController` au lieu de `VideoPlayerAdapter`.

## ÉTAPE 2 — VideoPlayerAdapter → VideoPlayerController

### Analyse

`VideoPlayerAdapter.java` (196 LOC) étendait `PlayerAdapter` (1 import Leanback) et servait de bridge entre :
- Le Glue (transport controls Leanback)
- Les 13 `CustomAction` subclasses (UI popups)
- `CustomSeekProvider` (seek data)

Usage par les actions : principalement `videoPlayerAdapter.leanbackOverlayFragment.setFading()` / `.hideOverlay()` et `videoPlayerAdapter.masterOverlayFragment.showX()`.

### Migration

| Avant | Après |
|-------|-------|
| `VideoPlayerAdapter.java` extends `PlayerAdapter` | **Supprimé** |
| Actions prennent `VideoPlayerAdapter` | Actions prennent `VideoPlayerController` (interface pure) |
| Logique mélangée Leanback + domaine | Séparée : interface pure + impl Leanback |

**Fichiers créés :**

1. `overlay/VideoPlayerController.kt` — Interface Kotlin pure, zéro Leanback
   - Transport : `play()`, `pause()`, `seekTo()`, `rewind()`, `fastForward()`, `next()`, `previous()`
   - État : `getDuration()`, `getCurrentPosition()`, `isPlaying()`, `getBufferedPosition()`
   - Queries : `hasSubs()`, `hasMultiAudio()`, `canSeek()`, `isLiveTv()`, `hasChapters()`, `hasCast()`, etc.
   - Overlay : `setOverlayFading()`, `hideOverlay()`, `masterOverlayFragment`
   - Données : `currentlyPlayingItem`, `currentMediaSource`

2. `overlay/VideoPlayerControllerImpl.kt` — Implémente `VideoPlayerController` + étend `PlayerAdapter`
   - Wraps `PlaybackController` pour toutes les méthodes domaine
   - Fournit les overrides `PlayerAdapter` requis par le Glue
   - Gère les callbacks Leanback (`updateCurrentPosition`, `updatePlayState`, `updateDuration`)

**Fichier supprimé :** `overlay/VideoPlayerAdapter.java`

**Fichiers modifiés :**
- `CustomPlaybackTransportControlGlue.java` — `<VideoPlayerAdapter>` → `<VideoPlayerControllerImpl>`
- `CustomAction.kt` — `videoPlayerAdapter: VideoPlayerAdapter` → `videoPlayerController: VideoPlayerController`
- 13 action subclasses — import + paramètre + appels overlay migrés :
  - `videoPlayerAdapter.leanbackOverlayFragment.setFading(x)` → `videoPlayerController.setOverlayFading(x)`
  - `videoPlayerAdapter.leanbackOverlayFragment.hideOverlay()` → `videoPlayerController.hideOverlay()`
  - `videoPlayerAdapter.masterOverlayFragment` → `videoPlayerController.masterOverlayFragment`
  - `videoPlayerAdapter.toggleRecording()` → `videoPlayerController.toggleRecording()`
- `LeanbackOverlayFragment.java` — `VideoPlayerAdapter` → `VideoPlayerControllerImpl`

## Compteur Leanback

| Métrique | Avant Phase 9b | Après Phase 9b | Delta |
|----------|----------------|----------------|-------|
| Imports Leanback | 34 | 34 | 0 |
| Fichiers avec Leanback | 13 | 12 | **-1** |

Détail :
- `CustomSeekProvider.kt` supprimé : -1 import, -1 fichier
- `VideoPlayerAdapter.java` supprimé : -1 import, -1 fichier
- `VideoPlayerControllerImpl.kt` créé : +1 import, +1 fichier
- `CustomPlaybackTransportControlGlue.java` : +1 import (`PlaybackSeekDataProvider`)

## Fichiers Leanback restants (12 fichiers, 34 imports)

### Player overlay (16 imports, 5 fichiers)
- `CustomPlaybackTransportControlGlue.java` — 10 imports
- `CustomPlaybackFragmentGlueHost.kt` — 4 imports
- `LeanbackOverlayFragment.java` — 1 import
- `VideoPlayerControllerImpl.kt` — 1 import
- `CustomAction.kt` — 1 import

### Autres (18 imports, 7 fichiers)
- `ItemRowAdapter.java` — 6 imports
- `TvManager.java` — 4 imports
- `MutableObjectAdapter.kt` — 3 imports
- `AggregatedItemRowAdapter.kt` — 1 import
- `TitleView.kt` — 1 import
- `TextItemPresenter.kt` — 1 import
- `CardPresenter.kt` — 1 import

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
SeekProviderTests — 3 tests PASSED
```

Installé et testé sur Ugoos AM9 Pro — lecture + seek vidéo confirmé.
