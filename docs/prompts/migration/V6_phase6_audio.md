# V6 — Phase 6 : AudioNowPlayingFragment → Compose TV

> **Date : 2026-03-09**
> **Objectif** : Migrer l'écran Audio Now Playing de Leanback Java vers Compose TV
> **Statut** : TERMINÉ — BUILD SUCCESSFUL

---

## Fichiers créés

| Fichier | LOC | Rôle |
|---------|-----|------|
| `ui/playback/audio/AudioNowPlayingViewModel.kt` | 201 | ViewModel : encapsule MediaManager + PlaybackManager en StateFlow |
| `ui/playback/audio/AudioNowPlayingScreen.kt` | 442 | Écran Compose TV : artwork/lyrics, info, contrôles, queue |
| `ui/playback/audio/AudioNowPlayingComposeFragment.kt` | 29 | Fragment wrapper ComposeView |
| **Total créé** | **672** | |

## Fichiers supprimés

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/playback/AudioNowPlayingFragment.java` | 406 | Remplacé par AudioNowPlayingScreen Compose |
| `ui/playback/AudioNowPlayingFragmentHelper.kt` | 128 | Helpers initializePreviewView/initializePlayerProgress — intégrés dans ArtworkPanel Compose |
| `ui/playback/AudioQueueBaseRowAdapter.kt` | 50 | Adapter Leanback — remplacé par LazyRow Compose avec StateFlow |
| `res/layout/fragment_audio_now_playing.xml` | 249 | Layout XML — plus nécessaire (full Compose) |
| **Total supprimé** | **833** | |

## Fichiers modifiés

| Fichier | Changement |
|---------|------------|
| `ui/navigation/Destinations.kt` | `nowPlaying` → `AudioNowPlayingComposeFragment` |
| `di/AppModule.kt` | Ajout `viewModel { AudioNowPlayingViewModel(...) }` |

---

## Imports Leanback — Évolution

| Métrique | Avant V6 (V4H) | Après V6 | Delta |
|----------|-----------------|----------|-------|
| Total imports | 106 | 97 | **−9** |
| Total fichiers | 32 | 31 | **−1** |

### Imports éliminés (9) :
1. `androidx.leanback.app.RowsSupportFragment`
2. `androidx.leanback.widget.ArrayObjectAdapter`
3. `androidx.leanback.widget.HeaderItem`
4. `androidx.leanback.widget.ListRow`
5. `androidx.leanback.widget.OnItemViewClickedListener`
6. `androidx.leanback.widget.OnItemViewSelectedListener`
7. `androidx.leanback.widget.Presenter`
8. `androidx.leanback.widget.Row`
9. `androidx.leanback.widget.RowPresenter`

### PositionableListRowPresenter
Non supprimé — toujours utilisé par `EnhancedBrowseFragment.java` (Phase 7).

---

## Architecture

```
AudioNowPlayingComposeFragment (Fragment wrapper)
└── AudioNowPlayingScreen (Compose)
    ├── ArtworkPanel
    │   ├── AsyncImage (artwork)
    │   └── LyricsDtoBox (overlay)
    ├── Song info (titre, artiste, album, track, genres)
    ├── AudioProgressSection (PlayerSeekbar + times)
    ├── AudioControlsRow (prev, rw, play/pause, ff, next, repeat, shuffle, album, artist)
    └── AudioQueueSection (LazyRow de AudioQueueCard)

AudioNowPlayingViewModel
├── MediaManager (contrôles audio legacy)
├── PlaybackManager (queue, seekbar, lyrics)
├── BackgroundService (fond flou)
└── NavigationRepository (navigation album/artiste)
```

---

## Build validation

| Commande | Résultat |
|----------|----------|
| `./gradlew assembleGithubDebug` | BUILD SUCCESSFUL |
| `./gradlew assembleGithubRelease` | BUILD SUCCESSFUL |

---

## Plan de suppression Leanback — Mise à jour

| Phase | Fichiers | Imports | Effort | Statut |
|-------|----------|---------|--------|--------|
| ~~Phase 6 : AudioNowPlaying~~ | ~~1 + 1 presenter~~ | ~~12~~ | ~~Petit~~ | **FAIT** |
| Phase 7 : BrowseGrid/EnhancedBrowse → Compose | 4 + 4 presenters/shared | 37 | Moyen | À faire |
| Phase 8 : FullDetailsFragment → Compose | 2 + 3 presenters | 16 | Moyen | À faire |
| Phase 9 : Playback transport → Compose | 11 | 22 | Grand | À faire |
| Phase 10 : UI misc (couleurs, refs inline) | 5 | 5 | Petit | À faire |
