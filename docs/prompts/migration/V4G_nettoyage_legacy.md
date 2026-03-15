# V4G — Nettoyage code mort legacy

> **Date : 2026-03-08**
> **Objectif** : Supprimer tout le code mort Leanback non référencé par la navigation active
> **Statut** : TERMINÉ

---

## Fichiers supprimés

### Browsing legacy (6 fichiers, 1572 LOC)

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/browsing/BrowseFolderFragment.kt` | 110 | Abstract class sans subclasses restantes (ByGenre/ByLetter supprimés en V4D) |
| `ui/browsing/BrowseViewFragment.java` | 210 | Non référencé dans Destinations.kt ni ailleurs |
| `ui/browsing/BrowseViewFragmentHelper.kt` | 82 | Uniquement utilisé par BrowseViewFragment |
| `ui/browsing/genre/GenresGridFragment.kt` | 503 | Remplacé par GenresGridComposeFragment (V4D) |
| `ui/browsing/genre/GenreBrowseFragment.kt` | 580 | Non référencé dans Destinations.kt ni ailleurs |
| `ui/browsing/genre/JellyfinGenreCardPresenter.kt` | 87 | Uniquement utilisé par GenresGridFragment |

### Home legacy (21 fichiers + 1 layout, 2251 LOC)

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/home/HomeFragment.kt` | 348 | Remplacé par HomeComposeFragment dans Destinations |
| `ui/home/HomeRowsFragment.kt` | 587 | Child fragment de HomeFragment uniquement |
| `ui/home/HomeFragmentHelper.kt` | 197 | Uniquement utilisé par HomeRowsFragment |
| `ui/home/HomeFragmentRow.kt` | 10 | Interface uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentLatestRow.kt` | 64 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentAggregatedLatestRow.kt` | 94 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentAggregatedNextUpRow.kt` | 91 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentAggregatedResumeRow.kt` | 100 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentPlaylistsRow.kt` | 75 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentWatchlistRow.kt` | 61 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentNowPlayingRow.kt` | 44 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentLiveTVRow.kt` | 56 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentMediaBarRow.kt` | 67 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentViewsRow.kt` | 32 | Row uniquement pour HomeRowsFragment |
| `ui/home/HomeFragmentBrowseRowDefRow.kt` | 55 | Row uniquement pour HomeRowsFragment |
| `ui/home/NotificationsHomeFragmentRow.kt` | 58 | Row uniquement pour HomeRowsFragment |
| `ui/home/MediaBarPresenter.kt` | 81 | Presenter uniquement pour HomeRowsFragment |
| `ui/home/SelectedItemState.kt` | 22 | Data class uniquement pour HomeFragment↔HomeRowsFragment |
| `ui/home/MediaBarRow.kt` | 12 | Data class uniquement pour HomeRowsFragment |
| `ui/notification/AppNotificationPresenter.kt` | 27 | Uniquement pour NotificationsHomeFragmentRow |
| `res/layout/fragment_home.xml` | 197 | Layout uniquement pour HomeFragment |

### Home mediabar (3 fichiers, 1529 LOC)

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/home/mediabar/MediaBarSlideshowView.kt` | 548 | Uniquement utilisé par MediaBarPresenter (supprimé) |
| `ui/home/mediabar/MediaBarSlideshowViewModel.kt` | 905 | Uniquement utilisé par legacy home (supprimé) |
| `ui/home/mediabar/MediaBarSlideshow.kt` | 76 | Data classes uniquement pour View/ViewModel ci-dessus |

### Nettoyage DI

| Fichier | Modification |
|---------|-------------|
| `di/AppModule.kt` | Supprimé import + viewModel `MediaBarSlideshowViewModel` |

---

## Total supprimé

| Catégorie | Fichiers | LOC |
|-----------|----------|-----|
| Browsing legacy | 6 | 1 572 |
| Home legacy | 22 | 2 251 |
| Home mediabar | 3 | 1 529 |
| **Total** | **31** | **5 352** |

---

## Fichiers conservés (avec raison)

### Browsing — actif dans Destinations.kt

| Fichier | LOC | Raison |
|---------|-----|--------|
| `BrowseGridFragment.java` | 1 002 | `Destinations.libraryBrowserWithType()` — musique (Albums, Artists) |
| `BrowseGridFragmentHelper.kt` | ~55 | Extensions pour BrowseGridFragment |
| `EnhancedBrowseFragment.java` | 530 | Base class de GenericFolderFragment (actif via `Destinations.folderBrowser`) |
| `GenericFolderFragment.kt` | ~40 | `Destinations.folderBrowser()` — navigation dossiers |

### Item Details — actif dans Destinations.kt

| Fichier | LOC | Raison |
|---------|-----|--------|
| `FullDetailsFragment.java` | 1 390 | `channelDetails()` + `seriesTimerDetails()` — Live TV |
| `FullDetailsFragmentHelper.kt` | ~200 | Extensions pour FullDetailsFragment |

### Presenters — tous liés à des fragments actifs

| Presenter | LOC | Utilisé par |
|-----------|-----|-------------|
| `CardPresenter.kt` | 547 | BrowseGridFragment, FullDetailsFragment, EnhancedBrowseFragment |
| `InfoCardPresenter.kt` | 32 | FullDetailsFragment |
| `MyDetailsOverviewRowPresenter.kt` | 133 | FullDetailsFragment |
| `HorizontalGridPresenter.java` | 298 | BrowseGridFragment |
| `CustomListRowPresenter.kt` | 41 | FullDetailsFragment |
| `CustomRowHeaderPresenter.kt` | 7 | CustomListRowPresenter |
| `TextItemPresenter.kt` | 34 | ItemRowAdapter |
| `PositionableListRowPresenter.kt` | 82 | AudioNowPlayingFragment, EnhancedBrowseFragment |
| `GridButtonPresenter.kt` | 91 | EnhancedBrowseFragment, BrowseViewFragment (Live TV) |

### Presenters — code mort (après migrations Jellyseerr + Player popup)

| Presenter | LOC | Raison |
|-----------|-----|--------|
| `GenreCardPresenter.kt` | 76 | Plus importé — remplacé par `JellyseerrGenreCard` Compose |
| `NetworkStudioCardPresenter.kt` | 87 | Plus importé — remplacé par `JellyseerrLogoCard` Compose |
| `ChannelCardPresenter.kt` | 32 | Plus importé — remplacé par `PlayerPopupView` Compose (channels row) |

### Mediabar — actif (item details / trailers)

| Fichier | Raison |
|---------|--------|
| `mediabar/TrailerResolver.kt` | Utilisé par ItemDetailComposeFragment, ItemDetailsFragment |
| `mediabar/YouTubeStreamResolver.kt` | Utilisé par TrailerResolver, TrailerPlayerFragment |
| `mediabar/ExoPlayerTrailerView.kt` | Utilisé par SeriesTrailerOverlay, TrailerPlayerFragment |
| `mediabar/SponsorBlockApi.kt` | Utilisé par TrailerPlayerFragment |
| `mediabar/NewPipeDownloader.kt` | Utilisé par YouTubeStreamResolver |

---

## Imports Leanback

| Métrique | Avant V4G | Après V4G | Après Jellyseerr Compose | Après Player Popup | Delta total |
|----------|-----------|-----------|--------------------------|--------------------| ------------|
| Fichiers Kotlin | 48 | 27 | 25 | 25 | −23 |
| Fichiers Java | 12 | 11 | 11 | 10 | −2 |
| **Total fichiers** | **60** | **38** | **36** | **35** | **−25** |
| Imports Kotlin | 129 | 59 | 41 | 41 | −88 |
| Imports Java | 80 | 76 | 76 | 68 | −12 |
| **Total imports** | **209** | **135** | **117** | **109** | **−100** |

### Répartition des imports restants (après Player Popup)

| Domaine | Fichiers | Imports | Supprimable ? |
|---------|----------|---------|---------------|
| Playback overlay (transport) | 9 | 28 | Non — Glue/Host/Adapter/Actions profondément couplés |
| Jellyseerr (code mort) | 2 | 2 | Oui — GenreCardPresenter + NetworkStudioCardPresenter |
| Playback (code mort) | 1 | 1 | Oui — ChannelCardPresenter (plus utilisé) |
| Browsing legacy actif | 5 | 26 | Quand BrowseGridFragment/EnhancedBrowse migrés |
| Item details legacy | 2 | 11 | Quand FullDetailsFragment migré |
| Presenters | 7 | 13 | Quand fragments parents migrés |
| Shared/ItemHandling | 5 | 12 | Quand fragments parents migrés |
| Live TV | 2 | 22 | Quand AudioNowPlaying/TvManager migrés |

---

## Migration Jellyseerr → Compose (zéro Leanback)

### Fichiers modifiés

| Fichier | Avant | Après | Imports Leanback |
|---------|-------|-------|------------------|
| `ui/jellyseerr/JellyseerrDiscoverRowsFragment.kt` | 622L, `RowsSupportFragment`, 12 imports Leanback | 95L, `Fragment` + `ComposeView` | **0** |
| `ui/jellyseerr/JellyseerrBrowseByFragment.kt` | 532L, `VerticalGridPresenter`, 8 imports Leanback | 128L, `Fragment` + `ComposeView` | **0** |

### Fichiers créés

| Fichier | LOC | Rôle |
|---------|-----|------|
| `ui/jellyseerr/compose/JellyseerrCards.kt` | 175 | `JellyseerrPosterCard`, `JellyseerrGenreCard`, `JellyseerrLogoCard` |
| `ui/jellyseerr/compose/JellyseerrDiscoverRows.kt` | 225 | Composable rows (LazyColumn + LazyRow) avec pagination |
| `ui/jellyseerr/compose/JellyseerrBrowseByScreen.kt` | 260 | Composable grid avec toolbar filter/sort |

### Code mort généré

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/jellyseerr/GenreCardPresenter.kt` | 76 | Plus importé (remplacé par `JellyseerrGenreCard`) |
| `ui/jellyseerr/NetworkStudioCardPresenter.kt` | 87 | Plus importé (remplacé par `JellyseerrLogoCard`) |

### Impact

| Métrique | Avant | Après |
|----------|-------|-------|
| Imports Leanback (Jellyseerr) | 20 | 2 (code mort) |
| Fichiers avec Leanback (Jellyseerr) | 4 | 2 (code mort) |
| `RowsSupportFragment` | 1 | 0 |
| `VerticalGridPresenter` | 1 | 0 |
| `ArrayObjectAdapter` | 2 | 0 |
| LOC fragments | 1 154 | 223 (−931) |

---

## Migration Player Popup → Compose (zéro Leanback dans CustomPlaybackOverlayFragment)

### Option choisie : B — Remplacement ciblé du popup panel

**Justification** : Le système de transport controls (Glue/Host/Adapter/Actions — 31 fichiers, 28 imports Leanback) est profondément couplé au pattern `PlaybackSupportFragment → GlueHost → TransportControlGlue → PlayerAdapter`. Le migrer nécessiterait une réécriture complète du player legacy, avec risque majeur de régression vidéo. Le popup panel (chapitres/cast/chaînes) était le seul composant isolable : il utilisait `RowsSupportFragment` comme conteneur de `ListRow` sans lien avec le transport.

Option A (full Compose PlayerScreen) rejetée car le player Compose complet existe déjà (`ui/player/video/`) avec `PlaybackManager` (moteur différent de `PlaybackController`). Migrer le legacy reviendrait à le réécrire, pas le nettoyer.

### Fichiers modifiés

| Fichier | Avant | Après | Imports LB retirés |
|---------|-------|-------|---------------------|
| `ui/playback/CustomPlaybackOverlayFragment.java` | 1483L, `RowsSupportFragment` + `ArrayObjectAdapter` + 6 autres | 1420L, `PlayerPopupView` (Compose) | **−8** (RowsSupportFragment, ArrayObjectAdapter, HeaderItem, ListRow, OnItemViewClickedListener, Presenter, Row, RowPresenter) |
| `res/layout/vlc_player_interface.xml` | `<LinearLayout id="rows_area">` | `<PlayerPopupView id="popup_content">` | N/A |

### Fichier créé

| Fichier | LOC | Rôle |
|---------|-----|------|
| `ui/playback/overlay/PlayerPopupView.kt` | 321 | `AbstractComposeView` — popup Compose pour chapitres, chaînes Live TV, cast |

### Architecture PlayerPopupView

```kotlin
sealed class PlayerPopupContent {
    data class Chapters(itemId, chapters, chapterImages, scrollToIndex)
    data class Channels(channels, scrollToIndex)
    data class Cast(people)
}

class PlayerPopupView : AbstractComposeView {
    var content: PlayerPopupContent?  // MutableStateFlow
    fun interface ChapterClickListener  // SAM pour Java interop
    fun interface ChannelClickListener
    fun interface PersonClickListener
}
```

- **Pattern** : `AbstractComposeView` (même pattern que `SkipOverlayView`) — bridge Compose↔Java
- **UI** : `LazyRow` + `TvFocusCard` (Design System) + `AsyncImage` (Coil3)
- **Scroll** : `rememberLazyListState` + `LaunchedEffect(scrollToIndex)` pour scroll-to-position
- **Callbacks** : `fun interface` (SAM) pour compatibilité Java lambda

### Imports retirés de CustomPlaybackOverlayFragment.java

| Import retiré | Remplacé par |
|---------------|-------------|
| `RowsSupportFragment` | `PlayerPopupView` (AbstractComposeView) |
| `ArrayObjectAdapter` | `PlayerPopupContent` sealed class |
| `HeaderItem` | — (titre géré par Compose) |
| `ListRow` | `LazyRow` items |
| `OnItemViewClickedListener` | `fun interface` callbacks (SAM) |
| `Presenter` | — (plus de presenter pattern) |
| `Row` | — |
| `RowPresenter` | — |
| `CardPresenter` | `TvFocusCard` + `AsyncImage` |
| `ChannelCardPresenter` | `ChannelsRow` Composable |
| `PositionableListRowPresenter` | `rememberLazyListState` scroll |
| `ItemRowAdapter` | `PlayerPopupContent` data classes |
| `BaseItemPersonBaseRowItem` | `BaseItemPerson` directement |
| `ChapterItemInfoBaseRowItem` | `ChapterInfo` directement |

### Code mort généré

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/presentation/ChannelCardPresenter.kt` | 32 | Plus importé (remplacé par `ChannelsRow` Compose dans `PlayerPopupView`) |

### Impact

| Métrique | Avant | Après |
|----------|-------|-------|
| Imports Leanback (CustomPlaybackOverlayFragment) | 8 | **0** |
| `RowsSupportFragment` (playback) | 1 | 0 |
| `ArrayObjectAdapter` (playback) | 1 | 0 |
| Presenters utilisés (playback popup) | 3 | 0 |
| LOC CustomPlaybackOverlayFragment | 1 483 | 1 420 (−63) |

### Test AM9 Pro

- **Install** : `adb install` → Success
- **Lancement** : OK — pas de crash au démarrage
- **Test vidéo** : En attente de validation utilisateur

---

## Build

```
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
```
