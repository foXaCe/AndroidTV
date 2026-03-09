# V4A — Inventaire complet Leanback & Cartographie des écrans

> **Date : 2026-03-08**
> **Objectif** : Cartographier tout le code Leanback pour planifier la migration 100% Compose
> **Fichiers Leanback** : 52 fichiers Kotlin + 18 fichiers Java = **70 fichiers**
> **Total LOC Leanback** : ~17 500 lignes

---

## 1. Inventaire des imports Leanback (52 fichiers Kotlin)

### 1.1 Fragments Leanback (héritage direct)

| Fichier | Lignes | Classe parente | Complexité | Dépendances |
|---------|--------|---------------|------------|-------------|
| `ui/home/HomeRowsFragment.kt` | 587 | `RowsSupportFragment` | **Haute** | 38+ inject(), 13 HomeFragmentRow helpers, CardPresenter, ItemRowAdapter |
| `ui/browsing/BrowseFolderFragment.kt` | 110 | `BrowseSupportFragment` | Moyenne | ByGenreFragment, ByLetterFragment héritent |
| `ui/browsing/AllFavoritesFragment.kt` | 130 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/AllGenresFragment.kt` | 54 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/CollectionFragment.kt` | 34 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/FolderViewFragment.kt` | 66 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/GenericFolderFragment.kt` | 74 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/SuggestedMoviesFragment.kt` | 50 | `EnhancedBrowseFragment` | Faible | EnhancedBrowseFragment (Java) |
| `ui/browsing/ByLetterFragment.kt` | 45 | `BrowseFolderFragment` | Faible | BrowseFolderFragment |
| `ui/browsing/ByGenreFragment.kt` | 42 | `BrowseFolderFragment` | Faible | BrowseFolderFragment |
| `ui/jellyseerr/JellyseerrDiscoverRowsFragment.kt` | 622 | `RowsSupportFragment` | **Haute** | JellyseerrViewModel, CardPresenter, GenreCardPresenter |
| `ui/jellyseerr/JellyseerrBrowseByFragment.kt` | 532 | `Fragment` (Leanback widgets) | **Haute** | VerticalGridPresenter, NetworkStudioCardPresenter, HorizontalGridPresenter |
| `ui/search/SearchFragment.kt` | 150 | `Fragment` (AndroidFragment<RowsSupportFragment>) | Moyenne | RowsSupportFragment embedded, SearchFragmentDelegate |
| `ui/browsing/genre/GenresGridFragment.kt` | 503 | `Fragment` (Leanback adapters) | **Haute** | HorizontalGridPresenter, JellyfinGenreCardPresenter |
| `ui/browsing/genre/GenreBrowseFragment.kt` | 580 | `Fragment` (Leanback rows) | **Haute** | RowsSupportFragment via XML, CardPresenter, custom adapters |

### 1.2 Fragments Leanback Java (héritage direct)

| Fichier | Lignes | Classe parente | Complexité |
|---------|--------|---------------|------------|
| `ui/browsing/EnhancedBrowseFragment.java` | 530 | `RowsSupportFragment` | **Haute** — base class pour 6+ sous-classes |
| `ui/browsing/BrowseViewFragment.java` | 210 | `EnhancedBrowseFragment` | Moyenne |
| `ui/browsing/BrowseScheduleFragment.java` | 17 | `EnhancedBrowseFragment` | Faible |
| `ui/browsing/BrowseRecordingsFragment.java` | 109 | `EnhancedBrowseFragment` | Faible |
| `ui/browsing/BrowseGridFragment.java` | 1002 | `EnhancedBrowseFragment` | **Haute** — grille custom |
| `ui/itemdetail/FullDetailsFragment.java` | 1390 | `RowsSupportFragment` | **Haute** — legacy details, remplacé par v2/ |
| `ui/itemdetail/ItemListFragment.java` | 580 | `RowsSupportFragment` | **Haute** |
| `ui/itemdetail/MusicFavoritesListFragment.java` | 290 | `RowsSupportFragment` | Moyenne |
| `ui/playback/CustomPlaybackOverlayFragment.java` | 1483 | `Fragment` (Leanback glue) | **Haute** — overlay complet |
| `ui/playback/overlay/LeanbackOverlayFragment.java` | 132 | `PlaybackSupportFragment` | Moyenne |
| `ui/playback/AudioNowPlayingFragment.java` | 406 | `RowsSupportFragment` | **Haute** |
| `ui/livetv/LiveTvGuideFragment.java` | 790 | `Fragment` (Leanback widgets) | **Haute** — guide TV complet |

### 1.3 Presenters Leanback (héritent de `Presenter` / `RowPresenter`)

| Fichier | Lignes | Classe parente | Complexité | Utilisé par |
|---------|--------|---------------|------------|-------------|
| `ui/presentation/CardPresenter.kt` | 547 | `Presenter` | **Haute** | Tous les écrans browse + home |
| `ui/presentation/GridButtonPresenter.kt` | 91 | `Presenter` | Faible | EnhancedBrowseFragment |
| `ui/presentation/ChannelCardPresenter.kt` | 32 | `Presenter` | Faible | LiveTV |
| `ui/presentation/InfoCardPresenter.kt` | 32 | `Presenter` | Faible | Browse |
| `ui/presentation/TextItemPresenter.kt` | 34 | `Presenter` | Faible | Browse |
| `ui/presentation/CustomRowHeaderPresenter.kt` | 7 | `RowHeaderPresenter` | Faible | Rows header |
| `ui/presentation/CustomListRowPresenter.kt` | 41 | `ListRowPresenter` | Faible | Home, Browse |
| `ui/presentation/PositionableListRowPresenter.kt` | 82 | `RowPresenter` | Moyenne | Home rows avec position |
| `ui/presentation/MutableObjectAdapter.kt` | 95 | `ObjectAdapter` | Moyenne | Partout |
| `ui/presentation/MyDetailsOverviewRowPresenter.kt` | 133 | `RowPresenter` | Moyenne | FullDetailsFragment |
| `ui/presentation/HorizontalGridPresenter.java` | 298 | `Presenter` (Java) | **Haute** | GenresGrid, BrowseBy |
| `ui/home/MediaBarPresenter.kt` | 81 | `RowPresenter` | Moyenne | Home media bar |
| `ui/jellyseerr/GenreCardPresenter.kt` | 76 | `Presenter` | Faible | JellyseerrDiscoverRows |
| `ui/jellyseerr/NetworkStudioCardPresenter.kt` | 87 | `Presenter` | Faible | JellyseerrBrowseBy |
| `ui/browsing/genre/JellyfinGenreCardPresenter.kt` | 87 | `Presenter` | Faible | GenresGrid |
| `ui/notification/AppNotificationPresenter.kt` | 27 | `Presenter` | Faible | Home notifications |

### 1.4 Adapters Leanback (héritent de `ObjectAdapter` / `ArrayObjectAdapter`)

| Fichier | Lignes | Type | Utilisé par |
|---------|--------|------|-------------|
| `ui/presentation/MutableObjectAdapter.kt` | 95 | `ObjectAdapter` custom | Partout |
| `ui/itemhandling/AggregatedItemRowAdapter.kt` | 107 | `MutableObjectAdapter` | Home aggregated rows |
| `ui/playback/AudioQueueBaseRowAdapter.kt` | 50 | `MutableObjectAdapter` | AudioNowPlaying |
| `ui/itemhandling/ItemRowAdapter.java` | 826 | `MutableObjectAdapter` (Java) | **Central** — tous les browse |

### 1.5 Home Row Helpers (dépendent de Leanback adapters)

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/home/HomeFragmentRow.kt` | 10 | Interface base |
| `ui/home/HomeFragmentLatestRow.kt` | 64 | Row "Latest Added" |
| `ui/home/HomeFragmentAggregatedLatestRow.kt` | 94 | Row agrégée latest |
| `ui/home/HomeFragmentAggregatedResumeRow.kt` | 100 | Row agrégée resume |
| `ui/home/HomeFragmentAggregatedNextUpRow.kt` | 91 | Row agrégée next up |
| `ui/home/HomeFragmentBrowseRowDefRow.kt` | 55 | Row browse definition |
| `ui/home/HomeFragmentMediaBarRow.kt` | 67 | Row media bar |
| `ui/home/HomeFragmentNowPlayingRow.kt` | 44 | Row now playing |
| `ui/home/HomeFragmentPlaylistsRow.kt` | 75 | Row playlists |
| `ui/home/HomeFragmentWatchlistRow.kt` | 61 | Row watchlist |
| `ui/home/HomeFragmentViewsRow.kt` | 32 | Row views |
| `ui/home/HomeFragmentLiveTVRow.kt` | 56 | Row Live TV |
| `ui/home/NotificationsHomeFragmentRow.kt` | 58 | Row notifications |
| `ui/home/MediaBarRow.kt` | 12 | Data class media bar |
| **Sous-total** | **819** | |

### 1.6 Playback Overlay (Leanback Glue API)

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/playback/overlay/CustomPlaybackFragmentGlueHost.kt` | 24 | PlaybackSupportFragmentGlueHost |
| `ui/playback/overlay/CustomSeekProvider.kt` | 389 | PlaybackSeekDataProvider |
| `ui/playback/overlay/CustomPlaybackTransportControlGlue.java` | 412 | PlaybackTransportControlGlue |
| `ui/playback/overlay/VideoPlayerAdapter.java` | 196 | PlayerAdapter |
| `ui/playback/overlay/LeanbackOverlayFragment.java` | 132 | PlaybackSupportFragment |
| `ui/playback/overlay/action/*.kt` (20 fichiers) | 805 | Actions Leanback (play, pause, etc.) |
| **Sous-total** | **1 958** | |

### 1.7 Composants auxiliaires Leanback

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/shared/TitleView.kt` | 40 | TitleViewAdapter.Provider |
| `ui/itemdetail/MyDetailsOverviewRow.kt` | 24 | Row data class |
| `ui/home/SimpleInfoRowView.kt` | 288 | Custom info row view |
| `ui/browsing/CompositeClickedListener.kt` | 25 | OnItemViewClickedListener |
| `ui/browsing/CompositeSelectedListener.kt` | 25 | OnItemViewSelectedListener |
| `ui/browsing/DestinationFragmentView.kt` | 195 | Fragment container view |
| `ui/itemhandling/ItemRowAdapterHelper.kt` | 915 | Helper adapter functions |
| `ui/search/SearchFragmentDelegate.kt` | 66 | Search adapter delegate |

### 1.8 Fichiers Java LiveTV/Guide (Leanback-dépendants)

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/livetv/LiveTvGuideFragment.java` | 790 | Guide TV complet |
| `ui/livetv/TvManager.java` | 242 | TV data manager |
| `ui/ProgramGridCell.java` | 172 | Cellule grille guide |
| `ui/GuideChannelHeader.java` | 89 | Header channel guide |
| `ui/LiveProgramDetailPopup.java` | 322 | Popup détails programme |
| `ui/RecordPopup.java` | 256 | Popup enregistrement |
| `ui/FriendlyDateButton.java` | 47 | Bouton date |
| `ui/ItemListView.java` | 125 | Vue liste items |
| `ui/ItemRowView.java` | 231 | Vue row items |
| `ui/card/ChannelCardView.java` | 58 | Carte chaîne |

### 1.9 Layouts XML Leanback

| Fichier | Lignes | Usage |
|---------|--------|-------|
| `res/layout/view_lb_title.xml` | 32 | TitleView Leanback |
| `res/layout/horizontal_grid.xml` | 10 | HorizontalGridView Leanback |
| `res/layout/fragment_server.xml` | 162 | Contient `leanback.widget.SearchOrbView` |
| `res/layout/vlc_player_interface.xml` | — | Référence classes Leanback |

---

## 2. Cartographie des écrans — État actuel

### 2.1 Écrans de navigation principale

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Home (container) | `HomeFragment.kt` | MIXTE | 348 | Moyenne — Compose sidebar + Leanback child |
| Home (rows) | `HomeRowsFragment.kt` | LEANBACK | 587 | **Haute** — 38+ deps, 13 row helpers |
| Search | `SearchFragment.kt` | MIXTE | 150 | Moyenne — Compose UI + RowsSupportFragment embedded |
| Sidebar Navigation | `LeftSidebarNavigation.kt` | COMPOSE | 878 | **Haute** (mais déjà Compose) |

### 2.2 Écrans Browse/Library

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Library Browse | `LibraryBrowseFragment.kt` | COMPOSE | 556 | Moyenne |
| Music Browse | `MusicBrowseFragment.kt` | COMPOSE | 651 | Moyenne |
| Genres Grid v2 | `GenresGridV2Fragment.kt` | COMPOSE | 836 | Moyenne |
| All Favorites | `AllFavoritesFragment.kt` | LEANBACK | 130 | Faible |
| All Genres | `AllGenresFragment.kt` | LEANBACK | 54 | Faible |
| Collection | `CollectionFragment.kt` | LEANBACK | 34 | Faible |
| Folder View | `FolderViewFragment.kt` | LEANBACK | 66 | Faible |
| Generic Folder | `GenericFolderFragment.kt` | LEANBACK | 74 | Faible |
| Suggested Movies | `SuggestedMoviesFragment.kt` | LEANBACK | 50 | Faible |
| Browse View | `BrowseViewFragment.java` | LEANBACK | 210 | Moyenne |
| Browse Grid | `BrowseGridFragment.java` | LEANBACK | 1002 | **Haute** |
| Browse Folder | `BrowseFolderFragment.kt` | LEANBACK | 110 | Moyenne |
| By Letter | `ByLetterFragment.kt` | LEANBACK | 45 | Faible |
| By Genre | `ByGenreFragment.kt` | LEANBACK | 42 | Faible |
| Genres Grid (legacy) | `GenresGridFragment.kt` | LEANBACK | 503 | **Haute** |
| Genre Browse | `GenreBrowseFragment.kt` | LEANBACK | 580 | **Haute** |

### 2.3 Écrans Item Details

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Item Details (v2) | `ItemDetailsFragment.kt` | COMPOSE | 2047 | **Haute** (mais déjà Compose) |
| Full Details (legacy) | `FullDetailsFragment.java` | LEANBACK | 1390 | **Haute** — legacy, peut-être mort |
| Item List | `ItemListFragment.java` | LEANBACK | 580 | **Haute** |
| Music Favorites List | `MusicFavoritesListFragment.java` | LEANBACK | 290 | Moyenne |
| Trailer Player | `TrailerPlayerFragment.kt` | COMPOSE | 204 | Faible |

### 2.4 Écrans Playback

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Video Player (new) | `VideoPlayerFragment.kt` | COMPOSE | 76 | Faible |
| Playback Overlay (legacy) | `CustomPlaybackOverlayFragment.java` | LEANBACK | 1483 | **Haute** — overlay principal |
| Leanback Overlay | `LeanbackOverlayFragment.java` | LEANBACK | 132 | Moyenne |
| Audio Now Playing | `AudioNowPlayingFragment.java` | LEANBACK | 406 | **Haute** |
| Next Up | `NextUpFragment.kt` | COMPOSE | 253 | Faible |
| Still Watching | `StillWatchingFragment.kt` | COMPOSE | 260 | Faible |
| Photo Player | `PhotoPlayerFragment.kt` | COMPOSE | 54 | Faible |

### 2.5 Écrans Live TV

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Live TV Browse | `LiveTvBrowseFragment.kt` | COMPOSE | 566 | Moyenne |
| Live TV Guide | `LiveTvGuideFragment.java` | LEANBACK | 790 | **Haute** — grille guide TV |
| Recordings Browse | `RecordingsBrowseFragment.kt` | COMPOSE | 516 | Faible |
| Schedule Browse | `ScheduleBrowseFragment.kt` | COMPOSE | 406 | Faible |
| Series Recordings | `SeriesRecordingsBrowseFragment.kt` | COMPOSE | 385 | Faible |
| Browse Recordings (legacy) | `BrowseRecordingsFragment.java` | LEANBACK | 109 | Faible |
| Browse Schedule (legacy) | `BrowseScheduleFragment.java` | LEANBACK | 17 | Faible |

### 2.6 Écrans Jellyseerr

| Écran | Fragment | Technologie | Lignes | Complexité |
|-------|---------|-------------|--------|------------|
| Discover | `DiscoverFragment.kt` | MIXTE | 250 | Moyenne |
| Discover Rows | `JellyseerrDiscoverRowsFragment.kt` | LEANBACK | 622 | **Haute** |
| Browse By | `JellyseerrBrowseByFragment.kt` | LEANBACK | 532 | **Haute** |
| Media Details | `MediaDetailsFragment.kt` | COMPOSE | 587 | Moyenne (migré) |
| Person Details | `PersonDetailsFragment.kt` | COMPOSE | 133 | Faible (migré) |
| Requests | `RequestsFragment.kt` | XML | 81 | Faible |
| Settings | `SettingsFragment.kt` | XML | 261 | Faible |

### 2.7 Écrans Startup/Auth (tous COMPOSE — pas de Leanback)

| Écran | Fragment | Lignes |
|-------|---------|--------|
| Splash | `SplashFragment.kt` | 57 |
| Select Server | `SelectServerFragment.kt` | 270 |
| Add Server | `ServerAddFragment.kt` | 118 |
| User Login | `UserLoginFragment.kt` | 113 |
| User Credentials | `UserLoginCredentialsFragment.kt` | 120 |
| Quick Connect | `UserLoginQuickConnectFragment.kt` | 110 |
| Connect Help | `ConnectHelpAlertFragment.kt` | 121 |

### 2.8 Écrans Settings (tous COMPOSE — pas de Leanback)

| Écran | Fragment/Screen | Lignes |
|-------|----------------|--------|
| Settings Main | `SettingsMainScreen.kt` | ~300 |
| Settings Developer | `SettingsDeveloperScreen.kt` | ~200 |
| Settings About | `SettingsAboutScreen.kt` | ~150 |
| Settings Jellyseerr | `SettingsJellyseerrScreen.kt` | ~300 |

---

## 3. Analyse des dépendances Leanback → Équivalents Compose

### 3.1 Composants fondamentaux

| Fonctionnalité Leanback | Utilisée dans | Équivalent Compose TV |
|------------------------|---------------|----------------------|
| `RowsSupportFragment` | HomeRows, Search, FullDetails, AudioNowPlaying, DiscoverRows | `TvLazyColumn` + `TvLazyRow` |
| `BrowseSupportFragment` | BrowseFolderFragment (By*, ByLetter) | `TvScaffold` + `TvLazyColumn` |
| `PlaybackSupportFragment` | LeanbackOverlay | Compose Overlay + `PlayerSurface` |
| `PlaybackTransportControlGlue` | CustomPlaybackTransportControlGlue | Custom Compose controls |
| `ListRowPresenter` / `CustomListRowPresenter` | Home, Browse | `TvLazyRow` avec focus |
| `PositionableListRowPresenter` | Home rows avec position | `TvLazyRow` + `rememberLazyListState` |
| `Presenter` (Card) | CardPresenter (547L) | `@Composable CardItem` (existe dans DS) |
| `ObjectAdapter` / `MutableObjectAdapter` | 95L, partout | `List<T>` + `StateFlow` / `SnapshotStateList` |
| `ArrayObjectAdapter` | ItemRowAdapter (826L) | `List<T>` + `StateFlow` |
| `HeaderItem` + `ListRow` | Home, Browse, Search | Section header + `TvLazyRow` |
| `DiffCallback` (MutableObjectAdapter) | Adapters | `key =` dans `LazyList` |
| `ImageCardView` | CardPresenter | `@Composable` Card avec `AsyncImage` |
| `HorizontalGridPresenter` (Java) | GenresGrid, BrowseBy | `TvLazyVerticalGrid` |
| `FocusHighlight` | DiscoverRows, Search | `Modifier.focusable()` + focus scale |
| `TitleViewAdapter` | TitleView | Compose TopBar composable |
| `SearchOrbView` | fragment_server.xml | `IconButton` Compose |
| `OnItemViewClickedListener` | CompositeClickedListener | `Modifier.clickable()` / onClick lambda |
| `OnItemViewSelectedListener` | CompositeSelectedListener | `Modifier.onFocusChanged()` |
| `PlaybackSeekDataProvider` | CustomSeekProvider | Seekbar composable + ViewModel |
| `Action` (Leanback) | 20 actions playback | Icon buttons Compose |

### 3.2 Composants DS existants réutilisables

| Composant DS existant | Remplace Leanback |
|----------------------|-------------------|
| `CardItem` composable | `ImageCardView` + `CardPresenter` |
| `JellyfinTheme.colorScheme.*` | Colors hardcodées dans les Presenters |
| `AnimationDefaults.FOCUS_SCALE` (1.06x) | `FocusHighlight.ZOOM_FACTOR_SMALL` |
| `ErrorState` composable | Pas d'équivalent Leanback (erreurs silencieuses) |
| `EmptyState` composable | Pas d'équivalent Leanback |
| `SkeletonPresets` | Pas d'équivalent Leanback |

---

## 4. Statistiques de dépendance Leanback

### 4.1 Par type de fichier

| Type | Kotlin | Java | Total | LOC |
|------|--------|------|-------|-----|
| Fragments Leanback | 15 | 12 | 27 | ~10 800 |
| Presenters | 13 | 1 | 14 | ~1 665 |
| Adapters | 3 | 1 | 4 | ~1 078 |
| Home Row Helpers | 14 | 0 | 14 | 819 |
| Playback Actions | 20 | 0 | 20 | 805 |
| Playback Glue | 2 | 3 | 5 | 1 153 |
| Auxiliaires | 8 | 8 | 16 | ~2 500 |
| **TOTAL** | **75** | **25** | **100** | **~18 820** |

### 4.2 Répartition technologique

| Technologie | Écrans | % |
|------------|--------|---|
| COMPOSE (100%) | 22 | 46% |
| LEANBACK (100%) | 20 | 42% |
| MIXTE (Compose + Leanback) | 4 | 8% |
| XML seul | 2 | 4% |
| **Total** | **48** | **100%** |

### 4.3 Dépendances Gradle actuelles

```toml
# gradle/libs.versions.toml
androidx-leanback = "1.2.0"         # ACTIF — à supprimer en Phase 5
androidx-tv-material = "1.0.1"      # DÉCLARÉ mais NON UTILISÉ dans le code
# androidx-tv-foundation             # NON DÉCLARÉ — à ajouter

# app/build.gradle.kts
implementation(libs.androidx.leanback.core)       # leanback:1.2.0
implementation(libs.androidx.leanback.preference)  # leanback-preference:1.2.0
implementation(libs.androidx.tv.material)          # tv-material:1.0.1
```

### 4.4 Librairies TV Compose recommandées

| Lib | Version stable | Rôle | À ajouter |
|-----|---------------|------|-----------|
| `androidx.tv:tv-foundation` | 1.0.0 | TvLazyColumn/Row, focus D-pad | **OUI** |
| `androidx.tv:tv-material` | 1.0.1 | Surfaces TV, NavigationDrawer | Déjà déclaré |

---

## 5. Risques identifiés

### 5.1 Focus D-pad
- **Risque** : Compose TV gère le focus via `Modifier.focusable()` et `FocusRequester`, différent du système `setOnChildLaidOutListener` de Leanback
- **Mitigation** : Le DS a déjà `AnimationDefaults.FOCUS_SCALE` (1.06x) et les tests `DpadFocusTest.kt` existent
- **Impact** : Tous les écrans

### 5.2 Performance scroll
- **Risque** : `RowsSupportFragment` utilise RecyclerView très optimisé. `TvLazyColumn/TvLazyRow` doivent atteindre 60fps
- **Mitigation** : `key =` déjà ajouté sur toutes les LazyLists Compose existantes
- **Impact** : Home (rows multiples), Browse (grilles larges)

### 5.3 Playback overlay
- **Risque** : Le Leanback Glue API (TransportControlGlue, PlayerAdapter, SeekProvider) est très intégré. ~1 958 lignes à réécrire
- **Mitigation** : Le nouveau `VideoPlayerFragment` (Compose) existe déjà comme wrapper
- **Impact** : Expérience lecture complète

### 5.4 EnhancedBrowseFragment (Java)
- **Risque** : Classe de base Java (530L) pour 9+ sous-classes. La migration nécessite de réécrire la base ET toutes les sous-classes
- **Mitigation** : Beaucoup de sous-classes sont < 100 lignes (simple override)
- **Impact** : 11 fragments de browse

### 5.5 ItemRowAdapter / ItemRowAdapterHelper
- **Risque** : `ItemRowAdapter.java` (826L) + `ItemRowAdapterHelper.kt` (915L) = ~1 741L de logique d'adaptation de données Leanback
- **Mitigation** : En Compose, remplacé par des `StateFlow<List<T>>` dans les ViewModels
- **Impact** : Tous les écrans browse + home

### 5.6 Code Java legacy
- **Risque** : 25 fichiers Java (~10 400L) à migrer — nécessite aussi une conversion Java→Kotlin
- **Mitigation** : IntelliJ convertisseur automatique + refactoring
- **Impact** : Playback overlay, LiveTV guide, browse legacy
