# V4C — Migration Phase 2 : Écrans simples

> **Date : 2026-03-08**
> **Objectif** : Migrer les 9 écrans leaf nodes de Leanback vers Compose TV
> **Statut** : TERMINÉ

---

## Résumé

| Métrique | Avant Phase 2 | Après Phase 2 |
|----------|---------------|---------------|
| Imports `androidx.leanback` | 222 (64 fichiers) | 216 (62 fichiers) |
| Fichiers supprimés | — | 9 |
| Fichiers créés | — | 11 |
| LOC supprimées | — | ~547 |

---

## Progression par écran

| # | Écran | Fragment ancien | Composable créé | Wrapper | Nav | Nettoyé |
|---|-------|----------------|-----------------|---------|-----|---------|
| 2.1 | Collection | ✅ `CollectionFragment.kt` supprimé | ✅ `CollectionBrowseScreen.kt` | ✅ `CollectionBrowseFragment.kt` | ✅ Destinations.kt | ✅ |
| 2.2 | Suggested Movies | ✅ `SuggestedMoviesFragment.kt` supprimé | ✅ `SuggestedMoviesScreen.kt` | ✅ `SuggestedMoviesComposeFragment` | ✅ Destinations.kt | ✅ |
| 2.3 | All Genres | ✅ `AllGenresFragment.kt` supprimé | — Code mort (GenresGridV2Fragment existe) | — | — | ✅ |
| 2.4 | By Letter | ✅ `ByLetterFragment.kt` supprimé | ✅ `ByLetterBrowseScreen.kt` | ✅ `ByLetterBrowseFragment` | ✅ Destinations.kt | ✅ |
| 2.5 | By Genre | ✅ `ByGenreFragment.kt` supprimé | — Code mort (LibraryBrowseFragment genre mode) | — | — | ✅ |
| 2.6 | Folder View | ✅ `FolderViewFragment.kt` supprimé | ✅ `FolderViewScreen.kt` | ✅ `FolderViewComposeFragment` | ✅ Destinations.kt | ✅ |
| 2.7 | All Favorites | ✅ `AllFavoritesFragment.kt` supprimé | ✅ `AllFavoritesScreen.kt` | ✅ `AllFavoritesComposeFragment` | ✅ Destinations.kt | ✅ |
| 2.8 | Browse Schedule | ✅ `BrowseScheduleFragment.java` supprimé | — Code mort (ScheduleBrowseFragment v2) | — | — | ✅ |
| 2.9 | Browse Recordings | ✅ `BrowseRecordingsFragment.java` supprimé | — Code mort (RecordingsBrowseFragment v2) | — | — | ✅ |

---

## Fichiers créés (Phase 2)

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `ui/browsing/compose/BrowseMediaCard.kt` | ~110 | Carte média réutilisable (TvFocusCard + AsyncImage) |
| `ui/browsing/compose/CollectionBrowseScreen.kt` | ~145 | Screen + ViewModel : 3 rows (Movies/Series/Other) |
| `ui/browsing/compose/CollectionBrowseFragment.kt` | ~50 | Wrapper Fragment |
| `ui/browsing/compose/SuggestedMoviesScreen.kt` | ~155 | Screen + ViewModel : "Because you watched" rows |
| `ui/browsing/compose/SuggestedMoviesFragment.kt` | ~50 | Wrapper Fragment |
| `ui/browsing/compose/ByLetterBrowseScreen.kt` | ~155 | Screen + ViewModel : rows par lettre (#, A-Z) |
| `ui/browsing/compose/ByLetterBrowseFragment.kt` | ~50 | Wrapper Fragment |
| `ui/browsing/compose/FolderViewScreen.kt` | ~130 | Screen + ViewModel : dossiers racine + contenus |
| `ui/browsing/compose/FolderViewFragment.kt` | ~45 | Wrapper Fragment |
| `ui/browsing/compose/AllFavoritesScreen.kt` | ~160 | Screen + ViewModel : 5 catégories de favoris |
| `ui/browsing/compose/AllFavoritesFragment.kt` | ~50 | Wrapper Fragment |

---

## Fichiers supprimés (Phase 2)

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `CollectionFragment.kt` | 34 | Remplacé par `CollectionBrowseScreen` |
| `SuggestedMoviesFragment.kt` | 50 | Remplacé par `SuggestedMoviesScreen` |
| `AllGenresFragment.kt` | 54 | Code mort — remplacé par `GenresGridV2Fragment` |
| `ByLetterFragment.kt` | 45 | Remplacé par `ByLetterBrowseScreen` |
| `ByGenreFragment.kt` | 42 | Code mort — remplacé par `LibraryBrowseFragment` (genre mode) |
| `FolderViewFragment.kt` | 66 | Remplacé par `FolderViewScreen` |
| `AllFavoritesFragment.kt` | 130 | Remplacé par `AllFavoritesScreen` |
| `BrowseScheduleFragment.java` | 17 | Code mort — remplacé par `ScheduleBrowseFragment` (v2) |
| `BrowseRecordingsFragment.java` | 109 | Code mort — remplacé par `RecordingsBrowseFragment` (v2) |
| **Sous-total** | **547** | |

---

## Architecture des nouveaux écrans

```
Fragment (Wrapper)
├── ComposeView + JellyfinTheme
│   └── Screen (Composable)
│       ├── TvScaffold
│       │   ├── TvHeader
│       │   └── StateContainer
│       │       ├── Loading: SkeletonCardRow
│       │       ├── Empty: EmptyState
│       │       ├── Error: ErrorState + retry
│       │       └── Content: TvRowList
│       │           └── BrowseMediaCard (TvFocusCard)
└── ViewModel (Koin)
    ├── StateFlow<UiState>
    └── API calls → Dispatchers.IO
```

### Composants DS utilisés

| Composant | Usage |
|-----------|-------|
| `TvScaffold` | Base de chaque écran (background + overscan + focus) |
| `TvHeader` | Titre d'écran |
| `TvRowList` + `TvRow` | Liste verticale de rows horizontales |
| `TvFocusCard` | Carte focusable (via `BrowseMediaCard`) |
| `StateContainer` | Gestion animée des états Loading/Empty/Error/Content |
| `EmptyState` | Affichage état vide |
| `ErrorState` | Affichage erreur + retry |
| `SkeletonCardRow` | Skeleton loading |

### Zéro import Leanback dans les nouveaux fichiers ✅

---

## Modifications navigation

- `Destinations.kt` : 5 destinations mises à jour
  - `collectionBrowser` → `CollectionBrowseFragment`
  - `librarySuggestions` → `SuggestedMoviesComposeFragment`
  - `libraryByLetter` → `ByLetterBrowseFragment`
  - `folderView` → `FolderViewComposeFragment`
  - `allFavorites` → `AllFavoritesComposeFragment`
- `AppModule.kt` : 5 ViewModels enregistrés

---

## Build

```
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
./gradlew checkAll → BUILD SUCCESSFUL
  - Lint: 2 warnings, 3 hints (0 nouveaux)
  - 6 entrées lint-baseline devenues obsolètes (fichiers supprimés)
```
