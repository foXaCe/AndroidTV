# V4D — Migration Phase 3 : Écrans intermédiaires

> **Date : 2026-03-08**
> **Objectif** : Migrer les écrans intermédiaires (Search, Genres, Library Browser) vers Compose TV
> **Statut** : EN COURS

---

## 1. Search — Migration vers Compose TV

### Fichiers créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/search/compose/SearchScreen.kt` | 187 | Screen Compose : TvScaffold + TvHeader + SearchTextInput + TvCardGrid 5 colonnes + StateContainer + EmptyState |
| `ui/search/compose/SearchComposeFragment.kt` | 55 | Wrapper Fragment : NavigationLayout + JellyfinTheme |
| **Total** | **242** | |

### Fichiers supprimés

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `ui/search/SearchFragment.kt` | 54 | Remplacé par `SearchComposeFragment` |
| `ui/search/SearchScreen.kt` | 167 | Remplacé par `compose/SearchScreen.kt` |
| **Total** | **221** | |

### Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ui/navigation/Destinations.kt` | `search()` → `SearchComposeFragment` |
| `app/src/main/res/values/strings.xml` | +2 strings : `search_empty_hint`, `search_no_results` |

### Fichiers conservés (inchangés)

| Fichier | Rôle |
|---------|------|
| `ui/search/SearchViewModel.kt` | ViewModel inchangé (debounce 300ms, 14 groupes, multi-serveur) |
| `ui/search/SearchRepository.kt` | Repository inchangé |
| `ui/search/SearchResultGroup.kt` | Data class inchangée |
| `ui/search/composable/SearchTextInput.kt` | Composant input réutilisé |
| `ui/search/composable/SearchVoiceInput.kt` | Composant voix réutilisé |

---

## Architecture

```
SearchComposeFragment (wrapper)
├── NavigationLayout (sidebar/top nav)
│   └── JellyfinTheme
│       └── SearchScreen (Composable)
│           ├── TvScaffold (background + overscan + focus group)
│           │   ├── TvHeader (titre "Search")
│           │   ├── Row (input)
│           │   │   ├── SearchVoiceInput (optionnel)
│           │   │   └── SearchTextInput (focus au démarrage)
│           │   └── StateContainer
│           │       ├── Empty (query vide): "Start typing to search…"
│           │       ├── Empty (query non vide): "No results"
│           │       └── Content: TvCardGrid (5 colonnes)
│           │           └── BrowseMediaCard (TvFocusCard)
└── SearchViewModel (Koin, inchangé)
    ├── searchResultsFlow: StateFlow<Collection<SearchResultGroup>>
    ├── searchDebounced(query, 300ms)
    └── searchImmediately(query)
```

### Composants TV fondamentaux utilisés

| Composant | Usage |
|-----------|-------|
| `TvScaffold` | Base écran (background + overscan 5% + focus group) |
| `TvHeader` | Titre "Search" |
| `TvCardGrid` | Grille 5 colonnes (résultats aplatis) |
| `StateContainer` | Transition animée Empty ↔ Content |
| `EmptyState` | Messages vide/aucun résultat |
| `BrowseMediaCard` | Carte média avec TvFocusCard |

### Zéro import Leanback dans les nouveaux fichiers

---

## Différences vs. ancien SearchScreen

| Aspect | Ancien | Nouveau |
|--------|--------|---------|
| Scaffold | Aucun (Column directe) | TvScaffold (background + overscan + focus) |
| Header | Aucun | TvHeader "Search" |
| Résultats | TvRowList (rows par type) | TvCardGrid 5 colonnes (résultats aplatis) |
| États vides | Aucun affichage | EmptyState avec icône + message contextuel |
| Gestion d'état | Logique inline | StateContainer avec transitions animées |
| Padding | Manuelle (48dp/80dp) | TvScaffold overscan automatique |

---

## Build

```
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
./gradlew checkAll → BUILD SUCCESSFUL
  - Lint: 8 warnings, 3 hints
  - 11 entrées baseline obsolètes (fichiers supprimés phases précédentes)
```

---

## 2. Genres Grid — Migration vers Compose TV

### Fichiers créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/browsing/compose/GenresGridScreen.kt` | 491 | Screen Compose : TvScaffold + TvHeader (subtitle count) + TvCardGrid 5 colonnes + GenreCard (TvFocusCard + AsyncImage + gradient + titre) + StateContainer + Skeleton 20×(180×100dp) + Sort/Filter dialogs + DialogRadioItem réutilisable |
| `ui/browsing/compose/GenresGridComposeFragment.kt` | 84 | Wrapper Fragment : Koin VM + NavigationRepository + BackgroundService + navigation genre |
| **Total** | **575** | |

### Fichiers supprimés

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `ui/browsing/v2/GenresGridV2Fragment.kt` | 837 | Remplacé par `GenresGridScreen` + `GenresGridComposeFragment` |

### Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ui/navigation/Destinations.kt` | `allGenres` + `libraryByGenres` → `GenresGridComposeFragment` |

### Fichiers conservés (inchangés)

| Fichier | Rôle |
|---------|------|
| `ui/browsing/v2/GenresGridViewModel.kt` | ViewModel inchangé (302 lignes, 8 tests unitaires) |
| `ui/browsing/genre/JellyfinGenreItem.kt` | Data class inchangée |

### Architecture

```
GenresGridComposeFragment (wrapper)
├── JellyfinTheme
│   └── GenresGridScreen (Composable)
│       ├── TvScaffold (background + overscan + focus group)
│       │   ├── TvHeader (titre + subtitle genre count + actions toolbar)
│       │   │   ├── LibraryToolbarButton Home
│       │   │   ├── LibraryToolbarButton Sort
│       │   │   └── LibraryToolbarButton Filter (si multi-library)
│       │   └── StateContainer
│       │       ├── Loading: Skeleton 20 cartes (180×100dp, 5 colonnes)
│       │       ├── Empty: EmptyState "No genres found"
│       │       ├── Error: ErrorState + retry
│       │       └── Content: TvCardGrid (5 colonnes)
│       │           └── GenreCard (TvFocusCard)
│       │               ├── AsyncImage backdrop (maxWidth=480)
│       │               ├── Gradient bas (40%→75% black)
│       │               └── Titre centré en bas
│       ├── GenreSortDialog (5 options)
│       └── LibraryFilterDialog (All + per-library)
└── GenresGridViewModel (Koin, inchangé)
    ├── uiState: StateFlow<GenresGridUiState>
    ├── setSortOption / setLibraryFilter / setFocusedGenre
    └── retry()
```

### Composants TV fondamentaux utilisés

| Composant | Usage |
|-----------|-------|
| `TvScaffold` | Base écran (background + overscan 5% + focus group) |
| `TvHeader` | Titre + subtitle (genre count) + actions (toolbar) |
| `TvCardGrid` | Grille 5 colonnes de GenreCards |
| `TvFocusCard` | GenreCard focusable (scale 1.06× + focus ring) |
| `StateContainer` | Transition animée Loading/Empty/Error/Content |
| `EmptyState` / `ErrorState` | États vide/erreur |
| `SkeletonBox` | Skeleton loading (20 cartes) |

### Zéro import Leanback dans les nouveaux fichiers

### Différences vs. ancien GenresGridV2Fragment

| Aspect | Ancien (837 LOC) | Nouveau (575 LOC) |
|--------|-------------------|-------------------|
| Layout | FrameLayout + ComposeView + AppBackground + overlay | TvScaffold (overscan + focus) |
| Header | Custom Column (titre centré + FocusedGenreHud + toolbar) | TvHeader (titre + subtitle + actions slot) |
| Grille | LazyVerticalGrid 4 colonnes, cartes 280×158dp | TvCardGrid 5 colonnes, cartes fill×120dp |
| Carte | Custom Box + graphicsLayer (scale/alpha) + border | TvFocusCard (tv-material3 Surface, auto scale/ring) |
| Skeleton | SkeletonGenreGrid (4×3 = 12 cartes, 80dp) | 20 SkeletonBox (5×4, 180×100dp) |
| Dialogs | Private composables dans Fragment | Top-level composables + DialogRadioItem réutilisable |
| FocusedGenreHud | Oui (marquee + item count) | Supprimé (simplifié) |
| LibraryStatusBar | Oui | Supprimé (simplifié) |
| LOC | 837 | 575 (−31%) |

### Tests

```
./gradlew testGithubDebugUnitTest --tests "*GenresGridViewModelTests*" → 8 tests PASSED
```

---

## Build global

```
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
./gradlew checkAll → BUILD SUCCESSFUL
```

---

## 3. Library Browser — Migration vers Compose TV

### Fichiers créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/browsing/compose/LibraryBrowseScreen.kt` | 518 | Screen Compose : TvScaffold + TvHeader + FocusedItemHud + LazyRow filter chips (sort, favorites, played, series status, A-Z letters) + StateContainer + LazyVerticalGrid paginé (adaptive columns, skeleton 5 cartes en bas, key= sur chaque item) + FilterSortDialog |
| `ui/browsing/compose/LibraryBrowseComposeFragment.kt` | 160 | Wrapper Fragment : Koin VM + NavigationRepository + BackgroundService + ItemLauncher + settings overlay (ProvideRouter + SettingsDialog) + init library/genre mode |
| **Total** | **678** | |

### Fichiers supprimés

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `ui/browsing/v2/LibraryBrowseFragment.kt` | 557 | Remplacé par `LibraryBrowseScreen` + `LibraryBrowseComposeFragment` |

### Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ui/navigation/Destinations.kt` | `libraryBrowser` + `genreBrowse` → `LibraryBrowseComposeFragment` |

### Fichiers conservés (inchangés)

| Fichier | Rôle |
|---------|------|
| `ui/browsing/v2/LibraryBrowseViewModel.kt` | ViewModel inchangé (454 lignes, 11 tests unitaires) |
| `ui/browsing/v2/LibraryBrowseComponents.kt` | Composants réutilisés : LibraryPosterCard, LibraryToolbarButton, FocusedItemHud, FilterSortDialog, AlphaPickerBar |

### Architecture

```
LibraryBrowseComposeFragment (wrapper)
├── JellyfinTheme
│   └── Box
│       ├── LibraryBrowseScreen (Composable)
│       │   ├── TvScaffold (background + overscan + focus group)
│       │   │   ├── TvHeader (libraryName + subtitle itemCount + home/settings buttons)
│       │   │   ├── FocusedItemHud (marquee title + metadata)
│       │   │   ├── LazyRow (filter chips)
│       │   │   │   ├── FilterChip Sort (nom du tri actuel + icône)
│       │   │   │   ├── FilterChip Favorites (toggle)
│       │   │   │   ├── FilterChip Played Status (cycle ALL→UNWATCHED→WATCHED)
│       │   │   │   ├── FilterChip Series Status (cycle ALL→CONTINUING→ENDED, si tvshows)
│       │   │   │   └── LetterChip # A B C … Z (si tri par nom)
│       │   │   └── StateContainer
│       │   │       ├── Loading: SkeletonCardGrid
│       │   │       ├── Empty: EmptyState
│       │   │       ├── Error: ErrorState + retry
│       │   │       └── Content: LazyVerticalGrid (adaptive columns)
│       │   │           ├── LibraryPosterCard × N (key=item.id)
│       │   │           └── SkeletonCard × 5 (si hasMoreItems, pagination)
│       │   └── FilterSortDialog (sort + filters + favorites toggle)
│       └── SettingsDialog overlay (ProvideRouter + SettingsRouterContent)
└── LibraryBrowseViewModel (Koin, inchangé)
    ├── uiState: StateFlow<LibraryBrowseUiState>
    ├── initialize() / initializeGenre() (deux modes)
    ├── setSortOption / toggleFavorites / setPlayedFilter / setSeriesStatusFilter / setStartLetter
    ├── loadMore() (pagination pageSize=100)
    ├── setFocusedItem / refreshDisplayPreferences / retry
    └── effectiveApi / serverId / sortOptions
```

### Composants TV fondamentaux utilisés

| Composant | Usage |
|-----------|-------|
| `TvScaffold` | Base écran (background + overscan 5% + focus group) |
| `TvHeader` | Titre bibliothèque + subtitle (count) + actions (home, settings) |
| `StateContainer` | Transition animée Loading/Empty/Error/Content |
| `EmptyState` / `ErrorState` | États vide/erreur |
| `SkeletonCardGrid` | Skeleton initial (chargement complet) |
| `SkeletonCard` | Skeleton pagination (5 cartes en bas) |
| `LibraryPosterCard` | Carte poster réutilisée (v2 components) |
| `FocusedItemHud` | Info item focusé réutilisé (v2 components) |
| `FilterSortDialog` | Dialog tri/filtres réutilisé (v2 components) |

### Zéro import Leanback dans les nouveaux fichiers

### Différences vs. ancien LibraryBrowseFragment

| Aspect | Ancien (557 LOC) | Nouveau (678 LOC) |
|--------|-------------------|-------------------|
| Layout | FrameLayout + ComposeView + AppBackground + overlay | TvScaffold (overscan + focus) |
| Header | Custom Column (library pill + FocusedItemHud + toolbar) | TvHeader + FocusedItemHud + LazyRow chips |
| Filtres | LibraryToolbarButton → FilterSortDialog | LazyRow chips D-pad (sort, favorites, played, series, A-Z) + FilterSortDialog |
| A-Z | AlphaPickerBar (toolbar right) | LetterChip dans la même LazyRow |
| Grille | LazyVerticalGrid adaptive + itemsIndexed | LazyVerticalGrid adaptive + itemsIndexed + skeleton pagination |
| Pagination | derivedStateOf (last 10 visible) | Identique + 5 SkeletonCard en bas |
| Settings | ProvideRouter + SettingsDialog inline | Identique, déplacé dans Fragment |
| LOC | 557 | 678 (+22%, mais séparation screen/fragment) |

### Tests

```
./gradlew testGithubDebugUnitTest --tests "*LibraryBrowseViewModelTest*" → 11 tests (inchangés)
```

---

## Build global

```
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
```

---

## Imports Leanback restants

| Type | Fichiers | Imports |
|------|----------|---------|
| Kotlin (.kt) | 48 | 129 |
| Java (.java) | 12 | 80 |
| **Total** | **60** | **209** |

Principaux domaines Leanback restants :
- Playback overlay (CustomPlaybackOverlayFragment, transport controls, actions)
- Home screen (HomeRowsFragment, row adapters, presenters)
- Item details (FullDetailsFragment)
- Browsing legacy (BrowseFolderFragment, BrowseGridFragment, EnhancedBrowseFragment)
- Presentation layer (CardPresenter, ListRowPresenter, etc.)
- Jellyseerr (JellyseerrDiscoverRowsFragment, JellyseerrBrowseByFragment)

---

## Bilan LOC cumulé (Phase 3)

| Métrique | Search | Genres | Library | Total |
|----------|--------|--------|---------|-------|
| LOC créées | 242 | 575 | 678 | 1495 |
| LOC supprimées | 221 | 837 | 557 | 1615 |
| Delta net | +21 | −262 | +121 | **−120** |
