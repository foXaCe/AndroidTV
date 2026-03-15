# V6 — Phase 7 : BrowseGrid/EnhancedBrowse → Compose TV

> **Date : 2026-03-09**
> **Objectif** : Migrer BrowseGridFragment et EnhancedBrowseFragment de Leanback vers Compose TV
> **Statut** : TERMINÉ — BUILD SUCCESSFUL (debug + release)

---

## Décision : CAS A + CAS B

### CAS A — BrowseGridFragment → Réutilisation de LibraryBrowseScreen

**Justification** : BrowseGridFragment est fonctionnellement identique à LibraryBrowseScreen
(grille paginée + sort/filter/letter/settings) mais avec un paramètre `includeType` supplémentaire
pour filtrer par type (Albums, Artists, AlbumArtists). Plutôt que créer un écran dédié, on étend
le `LibraryBrowseViewModel` avec `initializeWithType()`.

Différences clés gérées :
- `"AlbumArtist"` → `artistsApi.getAlbumArtists()` (endpoint dédié)
- `"Artist"` → `artistsApi.getArtists()` (endpoint dédié)
- Autres types → `itemsApi.getItems()` avec `BaseItemKind` parsé et `recursive = true`

### CAS B — EnhancedBrowseFragment → FolderBrowseScreen dédié

**Justification** : EnhancedBrowseFragment affiche des **rows** (resume, latest, by name, specials)
dans un `RowsSupportFragment`. C'est un layout fondamentalement différent de la grille de
LibraryBrowseScreen. Un écran dédié est nécessaire.

GenericFolderFragment (seule sous-classe restante) est utilisé pour :
- `FOLDER` : dossiers génériques
- `SEASON` : saisons (épisodes + specials)

---

## Fichiers créés

| Fichier | LOC | Rôle |
|---------|-----|------|
| `ui/browsing/v2/FolderBrowseViewModel.kt` | 219 | ViewModel : rows (resume, latest, by name, specials) |
| `ui/browsing/compose/FolderBrowseScreen.kt` | ~310 | Écran Compose TV : AppBackground + rows LazyRow |
| `ui/browsing/compose/FolderBrowseComposeFragment.kt` | ~68 | Fragment wrapper ComposeView |
| **Total créé** | **~597** | |

## Fichiers modifiés

| Fichier | Changement |
|---------|------------|
| `ui/browsing/v2/LibraryBrowseViewModel.kt` | Ajout `initializeWithType()` + branch explicit type dans `loadItems()` |
| `ui/browsing/compose/LibraryBrowseComposeFragment.kt` | Ajout handling `Extras.IncludeType` → `initializeWithType()` |
| `ui/navigation/Destinations.kt` | `libraryBrowserWithType` → `LibraryBrowseComposeFragment`, `folderBrowser` → `FolderBrowseComposeFragment` |
| `di/AppModule.kt` | Ajout `viewModel { FolderBrowseViewModel(get(), get()) }` |
| `ui/itemhandling/ItemRowAdapter.java` | Suppression `setSortBy()`, imports BrowseGridFragment/EnhancedBrowseFragment, inline FAVSONGS=9 |
| `ui/itemhandling/ItemRowAdapterHelper.kt` | Suppression `SortOption` import + 3 fonctions sort (setAlbumArtistsSorting, setArtistsSorting, setItemsSorting) |

## Fichiers supprimés

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/browsing/BrowseGridFragment.java` | 1 002 | Remplacé par LibraryBrowseScreen + initializeWithType |
| `ui/browsing/BrowseGridFragmentHelper.kt` | 55 | Extensions Compose pour BrowseGridFragment — intégrées dans LibraryBrowseComposeFragment |
| `ui/browsing/EnhancedBrowseFragment.java` | 530 | Remplacé par FolderBrowseScreen |
| `ui/browsing/GenericFolderFragment.kt` | 74 | Remplacé par FolderBrowseViewModel |
| `ui/browsing/CompositeClickedListener.kt` | 25 | Wrapper Leanback OnItemViewClickedListener — plus nécessaire |
| `ui/browsing/CompositeSelectedListener.kt` | 25 | Wrapper Leanback OnItemViewSelectedListener — plus nécessaire |
| `ui/browsing/RowLoader.kt` | 5 | Interface pour EnhancedBrowseFragment — plus nécessaire |
| `ui/presentation/HorizontalGridPresenter.java` | 298 | Presenter Leanback grille horizontale — remplacé par LazyVerticalGrid Compose |
| `ui/presentation/PositionableListRowPresenter.kt` | 82 | Presenter Leanback rows — remplacé par LazyRow Compose |
| `ui/presentation/GridButtonPresenter.kt` | 91 | Presenter boutons navigation — remplacé par Compose buttons |
| `res/layout/horizontal_grid_browse.xml` | 129 | Layout BrowseGridFragment |
| `res/layout/enhanced_detail_browse.xml` | 77 | Layout EnhancedBrowseFragment |
| `res/layout/popup_empty.xml` | 9 | Layout popup lettre JumpList |
| **Total supprimé** | **2 402** | |

---

## Presenters supprimés

| Presenter | LOC | Était utilisé par |
|-----------|-----|-------------------|
| `HorizontalGridPresenter.java` | 298 | BrowseGridFragment uniquement |
| `PositionableListRowPresenter.kt` | 82 | EnhancedBrowseFragment uniquement |
| `GridButtonPresenter.kt` | 91 | EnhancedBrowseFragment uniquement |

---

## Imports Leanback — Évolution

| Métrique | Avant V7 (V6) | Après V7 | Delta |
|----------|---------------|----------|-------|
| Total imports | 97 | 57 | **−40** |
| Total fichiers | 31 | 24 | **−7** |

### Imports éliminés (40) :

**BrowseGridFragment (8 imports × 1 fichier)** :
1. `androidx.leanback.widget.BaseGridView`
2. `androidx.leanback.widget.FocusHighlight`
3. `androidx.leanback.widget.OnItemViewClickedListener`
4. `androidx.leanback.widget.OnItemViewSelectedListener`
5. `androidx.leanback.widget.Presenter`
6. `androidx.leanback.widget.Row`
7. `androidx.leanback.widget.RowPresenter`
8. `androidx.leanback.widget.VerticalGridPresenter`

**EnhancedBrowseFragment (10 imports × 1 fichier)** :
9. `androidx.leanback.app.RowsSupportFragment`
10. `androidx.leanback.widget.ArrayObjectAdapter`
11. `androidx.leanback.widget.ClassPresenterSelector`
12. `androidx.leanback.widget.HeaderItem`
13. `androidx.leanback.widget.ListRow`
14. `androidx.leanback.widget.OnItemViewClickedListener`
15. `androidx.leanback.widget.OnItemViewSelectedListener`
16. `androidx.leanback.widget.Presenter`
17. `androidx.leanback.widget.Row`
18. `androidx.leanback.widget.RowPresenter`

**CompositeClickedListener (4 imports × 1 fichier)** :
19–22. OnItemViewClickedListener, Presenter, Row, RowPresenter

**CompositeSelectedListener (4 imports × 1 fichier)** :
23–26. OnItemViewSelectedListener, Presenter, Row, RowPresenter

**HorizontalGridPresenter (10 imports × 1 fichier)** :
27–36. BaseGridView, FocusHighlight, HorizontalGridView, ObjectAdapter, OnItemViewClickedListener, OnItemViewSelectedListener, Presenter, PresenterSelector, ShadowOverlayHelper, ViewHolderTask

**PositionableListRowPresenter (3 imports × 1 fichier)** :
37–39. FocusHighlightHandler, ListRowPresenter, RowPresenter

**GridButtonPresenter (1 import × 1 fichier)** :
40. `androidx.leanback.widget.Presenter`

---

## Build validation

| Commande | Résultat |
|----------|----------|
| `./gradlew assembleGithubDebug` | BUILD SUCCESSFUL |
| `./gradlew assembleGithubRelease` | BUILD SUCCESSFUL |

---

## Architecture

### CAS A : LibraryBrowseScreen avec includeType

```
LibraryBrowseComposeFragment
├── Extras.IncludeType présent?
│   ├── Oui → viewModel.initializeWithType(folder, includeType, ...)
│   │   ├── "AlbumArtist" → artistsApi.getAlbumArtists()
│   │   ├── "Artist" → artistsApi.getArtists()
│   │   └── autre → itemsApi.getItems(includeItemTypes = parsedKind)
│   └── Non → viewModel.initialize(folder, ...) (mode standard)
└── LibraryBrowseScreen (inchangé)
```

### CAS B : FolderBrowseScreen

```
FolderBrowseComposeFragment (Fragment wrapper)
└── FolderBrowseScreen (Compose)
    ├── AppBackground + dark overlay
    ├── FolderHeader (nom, FocusedItemHud, bouton Home)
    └── FolderRows (verticalScroll)
        ├── "Continue Watching" (LazyRow) — si container type
        ├── "Latest" (LazyRow) — si container type
        ├── "By Name" / season name (LazyRow)
        └── "Specials" (LazyRow) — si season
```

---

## Plan de suppression Leanback — Mise à jour

| Phase | Fichiers | Imports | Effort | Statut |
|-------|----------|---------|--------|--------|
| ~~Phase 6 : AudioNowPlaying~~ | ~~1 + 1 presenter~~ | ~~9~~ | ~~Petit~~ | **FAIT** |
| ~~Phase 7 : BrowseGrid/EnhancedBrowse~~ | ~~10 + 3 presenters + 3 layouts~~ | ~~40~~ | ~~Moyen~~ | **FAIT** |
| Phase 8 : FullDetailsFragment → Compose | 2 + 3 presenters | 16 | Moyen | À faire |
| Phase 9 : Playback transport → Compose | 11 | 22 | Grand | À faire |
| Phase 10 : UI misc (couleurs, refs inline) | 5 | 5 | Petit | À faire |
