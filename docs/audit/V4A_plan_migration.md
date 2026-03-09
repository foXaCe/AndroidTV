# V4A — Plan de migration Leanback → Compose TV

> **Date : 2026-03-08**
> **Objectif** : Supprimer 100% des dépendances Leanback
> **Critère de succès** : Zéro import `androidx.leanback` dans tout le code
> **Effort total estimé** : ~4-6 semaines

---

## 1. Stratégie de migration d'écran (procédure standard)

Pour chaque écran Leanback, appliquer ces 6 étapes :

1. **Créer `NewScreen.kt`** — Composable pur utilisant le Design System
2. **Créer `NewScreenWrapper`** — Fragment minimal avec `content { NewScreen() }` qui délègue 100% au Composable
3. **Remplacer dans la navigation** — Mettre à jour `Destinations.kt` pour pointer vers le nouveau Fragment wrapper
4. **Tester D-pad + focus** — Vérifier sur Ugoos AM9 Pro (remote physique)
5. **Supprimer l'ancien** — Fragment + Presenters + XML layouts obsolètes
6. **Compiler** — `./gradlew checkAll` sans erreur

**RÈGLE ABSOLUE** : Jamais de Leanback dans les nouveaux fichiers.

---

## 2. Ordre de migration (risque croissant)

### Critères d'ordre appliqués :
1. Écrans sans dépendances vers d'autres Leanback en premier (leaf nodes)
2. Écrans avec ViewModels déjà propres (StateFlow) avant les autres
3. Complexité faible → haute
4. Sous-classes avant classes parentes (bottom-up)

---

## PHASE 1 — Fondations TV Compose (1-2 jours)

### 1.1 Ajouter la dépendance `tv-foundation`

```toml
# gradle/libs.versions.toml
androidx-tv-foundation = "1.0.0"

# [libraries]
androidx-tv-foundation = { module = "androidx.tv:tv-foundation", version.ref = "androidx-tv-foundation" }

# app/build.gradle.kts
implementation(libs.androidx.tv.foundation)
```

### 1.2 Composants TV de base à créer

| Composant | Fichier | Rôle | Remplace |
|-----------|---------|------|----------|
| `TvCardGrid` | `ui/base/tv/TvCardGrid.kt` | Grille de cartes avec D-pad navigation | HorizontalGridPresenter, VerticalGrid |
| `TvRowList` | `ui/base/tv/TvRowList.kt` | Liste verticale de rows horizontaux | RowsSupportFragment + ListRowPresenter |
| `TvMediaRow` | `ui/base/tv/TvMediaRow.kt` | Row horizontale de cartes média | ListRow + CardPresenter |
| `TvSectionHeader` | `ui/base/tv/TvSectionHeader.kt` | Header de section (remplace HeaderItem) | HeaderItem + RowHeaderPresenter |
| `TvBrowseScaffold` | `ui/base/tv/TvBrowseScaffold.kt` | Structure écran browse (toolbar + content) | BrowseSupportFragment layout |
| `TvPlaybackControls` | `ui/base/tv/TvPlaybackControls.kt` | Contrôles lecture (play/pause/seek) | TransportControlGlue |
| `DpadFocusManager` | `ui/base/tv/DpadFocusManager.kt` | Gestion focus global inter-composants | FocusHighlight + Leanback focus system |

### 1.3 Dépendances entre composants

```
TvBrowseScaffold
├── TvSectionHeader
├── TvRowList
│   ├── TvMediaRow
│   │   └── CardItem (existant dans DS)
│   └── TvSectionHeader
└── DpadFocusManager

TvCardGrid
├── CardItem (existant)
└── DpadFocusManager

TvPlaybackControls
├── SeekBar composable
├── Action buttons
└── DpadFocusManager
```

---

## PHASE 2 — Écrans simples leaf nodes (2-3 jours)

### Objectif : Migrer les écrans les moins complexes, sans dépendants

| # | Écran | Fragment actuel | Lignes | VM existant | Dépendants |
|---|-------|----------------|--------|-------------|------------|
| 2.1 | Collection | `CollectionFragment.kt` | 34 | Non (EnhancedBrowseFragment) | 0 |
| 2.2 | Suggested Movies | `SuggestedMoviesFragment.kt` | 50 | Non | 0 |
| 2.3 | All Genres | `AllGenresFragment.kt` | 54 | Non | 0 |
| 2.4 | By Letter | `ByLetterFragment.kt` | 45 | Non | 0 |
| 2.5 | By Genre | `ByGenreFragment.kt` | 42 | Non | 0 |
| 2.6 | Folder View | `FolderViewFragment.kt` | 66 | Non | 0 |
| 2.7 | All Favorites | `AllFavoritesFragment.kt` | 130 | Non | 0 |
| 2.8 | Browse Schedule (legacy) | `BrowseScheduleFragment.java` | 17 | Non | 0 |
| 2.9 | Browse Recordings (legacy) | `BrowseRecordingsFragment.java` | 109 | Non | 0 |

### Checklist par écran :

- [ ] **2.1 CollectionFragment** (34L)
  - [ ] Créer `CollectionBrowseScreen.kt` — TvRowList + CardItem
  - [ ] Créer `CollectionBrowseFragment.kt` — wrapper Compose
  - [ ] Mettre à jour `Destinations.collectionBrowser`
  - [ ] Tester D-pad
  - [ ] Supprimer ancien `CollectionFragment.kt`
  - [ ] Compiler

- [ ] **2.2 SuggestedMoviesFragment** (50L)
  - [ ] Créer `SuggestedMoviesScreen.kt`
  - [ ] Créer wrapper Fragment
  - [ ] Mettre à jour Destinations
  - [ ] Supprimer ancien
  - [ ] Compiler

- [ ] **2.3 AllGenresFragment** (54L)
  - [ ] Créer `AllGenresScreen.kt` — TvCardGrid
  - [ ] Wrapper + navigation + cleanup
  - [ ] Compiler

- [ ] **2.4 ByLetterFragment** (45L)
  - [ ] Créer `ByLetterBrowseScreen.kt` — TvBrowseScaffold
  - [ ] Wrapper + navigation + cleanup
  - [ ] Compiler

- [ ] **2.5 ByGenreFragment** (42L)
  - [ ] Créer `ByGenreBrowseScreen.kt`
  - [ ] Wrapper + navigation + cleanup
  - [ ] Compiler

- [ ] **2.6 FolderViewFragment** (66L)
  - [ ] Créer `FolderViewScreen.kt`
  - [ ] Wrapper + navigation + cleanup
  - [ ] Compiler

- [ ] **2.7 AllFavoritesFragment** (130L)
  - [ ] Créer `AllFavoritesScreen.kt` — TvRowList
  - [ ] Wrapper + navigation + cleanup
  - [ ] Compiler

- [ ] **2.8 BrowseScheduleFragment** (17L Java)
  - [ ] Vérifier si toujours utilisé (v2 `ScheduleBrowseFragment` existe)
  - [ ] Si doublon → supprimer
  - [ ] Sinon → créer wrapper Compose

- [ ] **2.9 BrowseRecordingsFragment** (109L Java)
  - [ ] Vérifier si toujours utilisé (v2 `RecordingsBrowseFragment` existe)
  - [ ] Si doublon → supprimer
  - [ ] Sinon → créer wrapper Compose

### Fichiers supprimables après Phase 2 :

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `CollectionFragment.kt` | 34 | Remplacé |
| `SuggestedMoviesFragment.kt` | 50 | Remplacé |
| `AllGenresFragment.kt` | 54 | Remplacé |
| `ByLetterFragment.kt` | 45 | Remplacé |
| `ByGenreFragment.kt` | 42 | Remplacé |
| `FolderViewFragment.kt` | 66 | Remplacé |
| `AllFavoritesFragment.kt` | 130 | Remplacé |
| `BrowseScheduleFragment.java` | 17 | Doublon probable v2 |
| `BrowseRecordingsFragment.java` | 109 | Doublon probable v2 |
| **Sous-total** | **547** | |

### Classes parentes : vérifier si `EnhancedBrowseFragment` et `BrowseFolderFragment` deviennent inutiles après cette phase (probablement non — `GenericFolderFragment`, `BrowseViewFragment`, `BrowseGridFragment` restent).

---

## PHASE 3 — Écrans médians (3-5 jours)

### Objectif : Migrer les écrans de complexité moyenne

| # | Écran | Fragment | Lignes | Prérequis |
|---|-------|---------|--------|-----------|
| 3.1 | Generic Folder | `GenericFolderFragment.kt` | 74 | Phase 2 (patterns rodés) |
| 3.2 | Browse View | `BrowseViewFragment.java` | 210 | Conversion Java→Kotlin |
| 3.3 | Browse Folder | `BrowseFolderFragment.kt` | 110 | Après migration des enfants |
| 3.4 | Search (Leanback part) | `SearchFragment.kt` | 150 | Remplacer RowsSupportFragment embedded |
| 3.5 | Genres Grid (legacy) | `GenresGridFragment.kt` | 503 | TvCardGrid prêt |
| 3.6 | Genre Browse | `GenreBrowseFragment.kt` | 580 | TvRowList prêt |
| 3.7 | Jellyseerr Requests | `RequestsFragment.kt` | 81 | XML → Compose simple |
| 3.8 | Jellyseerr Settings | `SettingsFragment.kt` | 261 | XML → Compose |
| 3.9 | Music Favorites List | `MusicFavoritesListFragment.java` | 290 | Java→Kotlin + Compose |
| 3.10 | Item List | `ItemListFragment.java` | 580 | Java→Kotlin + Compose |

### Checklist :

- [ ] **3.1-3.3 BrowseFolder hierarchy** — Migrer GenericFolder, BrowseView, puis la base BrowseFolder
- [ ] **3.4 SearchFragment** — Remplacer `AndroidFragment<RowsSupportFragment>` par `TvLazyColumn` natif
- [ ] **3.5 GenresGridFragment** — Remplacer `HorizontalGridPresenter` (Java) par `TvLazyVerticalGrid`
- [ ] **3.6 GenreBrowseFragment** — Remplacer rows Leanback par `TvRowList`
- [ ] **3.7-3.8 Jellyseerr XML** — Migration simple XML → Compose
- [ ] **3.9-3.10 ItemList/MusicFavorites** — Java→Kotlin puis RowsSupportFragment → TvLazyColumn

### Fichiers supprimables après Phase 3 :

| Fichier | Lignes |
|---------|--------|
| `GenericFolderFragment.kt` | 74 |
| `BrowseViewFragment.java` | 210 |
| `BrowseFolderFragment.kt` | 110 |
| `SearchFragmentDelegate.kt` | 66 |
| `GenresGridFragment.kt` | 503 |
| `GenreBrowseFragment.kt` | 580 |
| `JellyfinGenreCardPresenter.kt` | 87 |
| `RequestsFragment.kt` | 81 |
| `SettingsFragment.kt (Jellyseerr)` | 261 |
| `MusicFavoritesListFragment.java` | 290 |
| `ItemListFragment.java` | 580 |
| `HorizontalGridPresenter.java` | 298 |
| **Sous-total** | **3 140** |

---

## PHASE 4 — Écrans complexes (2-3 semaines)

### 4A — Jellyseerr Browse (3-4 jours)

| # | Écran | Fragment | Lignes | Complexité |
|---|-------|---------|--------|------------|
| 4A.1 | Jellyseerr Discover Rows | `JellyseerrDiscoverRowsFragment.kt` | 622 | Haute — rows multiples |
| 4A.2 | Jellyseerr Browse By | `JellyseerrBrowseByFragment.kt` | 532 | Haute — grille + filtres |
| 4A.3 | Jellyseerr Discover (container) | `DiscoverFragment.kt` | 250 | Moyenne — MIXTE |

**Dépendances** :
- `GenreCardPresenter.kt` (76L) → composable genre card
- `NetworkStudioCardPresenter.kt` (87L) → composable network/studio card
- `JellyseerrDiscoverViewModel` existe déjà (510L, StateFlow propre)

- [ ] Créer `JellyseerrDiscoverScreen.kt` — remplace DiscoverFragment + DiscoverRowsFragment
- [ ] Créer `JellyseerrBrowseByScreen.kt` — remplace BrowseByFragment
- [ ] Supprimer Presenters Jellyseerr (GenreCard, NetworkStudio)
- [ ] Tester D-pad sur toutes les sections discover

### 4B — Home Screen (4-5 jours)

| # | Composant | Fichier | Lignes |
|---|-----------|---------|--------|
| 4B.1 | Home Rows | `HomeRowsFragment.kt` | 587 |
| 4B.2 | Enhanced Browse (base) | `EnhancedBrowseFragment.java` | 530 |
| 4B.3 | Browse Grid | `BrowseGridFragment.java` | 1002 |
| 4B.4 | 13 Row Helpers | `HomeFragment*Row.kt` | 819 |
| 4B.5 | Info Row View | `SimpleInfoRowView.kt` | 288 |
| 4B.6 | Media Bar Presenter | `MediaBarPresenter.kt` | 81 |

**C'est le plus risqué** — 38+ dépendances, point d'entrée principal.

- [ ] Créer `HomeRowsScreen.kt` — `TvLazyColumn` avec sections dynamiques
- [ ] Convertir les 13 Row Helpers en fonctions de chargement de données (ViewModel)
- [ ] Créer `HomeRowsViewModel.kt` qui agrège toutes les sources
- [ ] Remplacer `ItemRowAdapter` par des `StateFlow<List<BaseRowItem>>`
- [ ] Supprimer `EnhancedBrowseFragment.java` (plus de sous-classes)
- [ ] Tester performance scroll 60fps sur AM9 Pro
- [ ] Vérifier que le slideshow media bar reste fluide

### 4C — Playback Overlay (3-5 jours)

| # | Composant | Fichier | Lignes |
|---|-----------|---------|--------|
| 4C.1 | Playback Overlay | `CustomPlaybackOverlayFragment.java` | 1483 |
| 4C.2 | Leanback Overlay | `LeanbackOverlayFragment.java` | 132 |
| 4C.3 | Transport Control Glue | `CustomPlaybackTransportControlGlue.java` | 412 |
| 4C.4 | Video Player Adapter | `VideoPlayerAdapter.java` | 196 |
| 4C.5 | Seek Provider | `CustomSeekProvider.kt` | 389 |
| 4C.6 | Fragment Glue Host | `CustomPlaybackFragmentGlueHost.kt` | 24 |
| 4C.7 | 20 Actions | `overlay/action/*.kt` | 805 |
| 4C.8 | Playback Controller | `PlaybackController.java` | 1670 |

**Sous-total : ~5 111 lignes**

- [ ] Créer `VideoPlaybackOverlayScreen.kt` — overlay Compose complet
- [ ] Créer `PlaybackControlsBar.kt` — barre de contrôle (play/pause/seek/progress)
- [ ] Migrer les 20 actions en icônes Compose dans le contrôle bar
- [ ] Remplacer Glue API par des callbacks ViewModel directs
- [ ] Conserver `PlaybackController.java` en l'état (logique pure, pas Leanback-dépendant directement) — migrer Java→Kotlin plus tard
- [ ] Tester seek, chapter skip, audio/subtitle selection

### 4D — Audio Now Playing (1-2 jours)

| # | Composant | Fichier | Lignes |
|---|-----------|---------|--------|
| 4D.1 | Audio Now Playing | `AudioNowPlayingFragment.java` | 406 |
| 4D.2 | Audio Queue Adapter | `AudioQueueBaseRowAdapter.kt` | 50 |

- [ ] Créer `AudioNowPlayingScreen.kt` — lecteur audio Compose
- [ ] Supprimer l'adapter audio queue

### 4E — Live TV Guide (2-3 jours)

| # | Composant | Fichier | Lignes |
|---|-----------|---------|--------|
| 4E.1 | Guide Fragment | `LiveTvGuideFragment.java` | 790 |
| 4E.2 | TV Manager | `TvManager.java` | 242 |
| 4E.3 | Program Grid Cell | `ProgramGridCell.java` | 172 |
| 4E.4 | Guide Channel Header | `GuideChannelHeader.java` | 89 |
| 4E.5 | Live Program Popup | `LiveProgramDetailPopup.java` | 322 |
| 4E.6 | Record Popup | `RecordPopup.java` | 256 |
| 4E.7 | Friendly Date Button | `FriendlyDateButton.java` | 47 |

**Sous-total : ~1 918 lignes**

- [ ] Créer `LiveTvGuideScreen.kt` — grille programme Compose
- [ ] Grille scrollable 2D (heures × chaînes) avec focus D-pad
- [ ] Popups détails/enregistrement en Compose

### 4F — Full Details (legacy) + Item Details (1-2 jours)

| # | Composant | Fichier | Lignes |
|---|-----------|---------|--------|
| 4F.1 | Full Details (legacy) | `FullDetailsFragment.java` | 1390 |

- [ ] Vérifier si `FullDetailsFragment.java` est encore utilisé (le v2 `ItemDetailsFragment.kt` existe)
- [ ] Si code mort → supprimer directement
- [ ] Si encore utilisé → rediriger vers le v2

---

## PHASE 5 — Suppression Leanback et nettoyage (1-2 jours)

### 5.1 Supprimer les classes fondamentales

| Fichier | Lignes | Raison |
|---------|--------|--------|
| `EnhancedBrowseFragment.java` | 530 | Plus de sous-classes |
| `ItemRowAdapter.java` | 826 | Remplacé par StateFlow |
| `ItemRowAdapterHelper.kt` | 915 | Plus d'adapters |
| `MutableObjectAdapter.kt` | 95 | Plus utilisé |
| `AggregatedItemRowAdapter.kt` | 107 | Plus utilisé |
| `CardPresenter.kt` | 547 | Remplacé par CardItem composable |
| `GridButtonPresenter.kt` | 91 | Remplacé |
| `ChannelCardPresenter.kt` | 32 | Remplacé |
| `InfoCardPresenter.kt` | 32 | Remplacé |
| `TextItemPresenter.kt` | 34 | Remplacé |
| `CustomRowHeaderPresenter.kt` | 7 | Remplacé |
| `CustomListRowPresenter.kt` | 41 | Remplacé |
| `PositionableListRowPresenter.kt` | 82 | Remplacé |
| `MyDetailsOverviewRowPresenter.kt` | 133 | Remplacé |
| `MyDetailsOverviewRow.kt` | 24 | Remplacé |
| `CompositeClickedListener.kt` | 25 | Remplacé |
| `CompositeSelectedListener.kt` | 25 | Remplacé |
| `TitleView.kt` | 40 | Remplacé |
| `SimpleInfoRowView.kt` | 288 | Remplacé |
| `DestinationFragmentView.kt` | 195 | Réévaluer |

### 5.2 Supprimer les dépendances Gradle

```kotlin
// app/build.gradle.kts — SUPPRIMER :
implementation(libs.androidx.leanback.core)
implementation(libs.androidx.leanback.preference)
```

```toml
# gradle/libs.versions.toml — SUPPRIMER :
androidx-leanback = "1.2.0"
androidx-leanback-preference = { ... }
```

### 5.3 Supprimer les layouts XML Leanback

| Fichier | Raison |
|---------|--------|
| `view_lb_title.xml` | TitleView supprimé |
| `horizontal_grid.xml` | HorizontalGridView supprimé |

### 5.4 Vérification finale

```bash
# Doit retourner 0 résultats :
grep -r "androidx.leanback" --include="*.kt" --include="*.java" --include="*.xml" -l

# Compilation :
./gradlew checkAll

# Test D-pad :
# Parcourir TOUS les écrans avec remote physique sur AM9 Pro
```

---

## 3. Résumé par phase

| Phase | Description | Écrans | LOC à migrer | Effort | Risque |
|-------|------------|--------|-------------|--------|--------|
| 1 | Fondations TV Compose | 0 | ~500 (création) | 1-2 jours | Faible |
| 2 | Écrans simples (leaf nodes) | 9 | ~547 | 2-3 jours | Faible |
| 3 | Écrans médians | 10 | ~3 140 | 3-5 jours | Moyen |
| 4A | Jellyseerr Browse | 3 | ~1 404 | 3-4 jours | Moyen |
| 4B | Home Screen | 6+ | ~3 307 | 4-5 jours | **Élevé** |
| 4C | Playback Overlay | 8+ | ~5 111 | 3-5 jours | **Élevé** |
| 4D | Audio Now Playing | 2 | ~456 | 1-2 jours | Moyen |
| 4E | Live TV Guide | 7 | ~1 918 | 2-3 jours | **Élevé** |
| 4F | Full Details (legacy) | 1 | ~1 390 | 1-2 jours | Faible (probablement code mort) |
| 5 | Suppression Leanback | — | -3 500 (suppression) | 1-2 jours | Faible |
| **TOTAL** | | **~46 écrans** | **~18 820** | **~4-6 semaines** | |

---

## 4. Métriques de succès

| Métrique | Avant | Cible Phase 5 |
|----------|-------|---------------|
| Imports `androidx.leanback` | 70 fichiers | **0** |
| Fichiers Leanback | ~100 | **0** |
| LOC Leanback | ~18 820 | **0** |
| Dépendances Gradle leanback | 2 | **0** |
| Écrans COMPOSE | 22 (46%) | **48 (100%)** |
| Écrans LEANBACK | 20 (42%) | **0 (0%)** |
| Performance 60fps Home | ✅ | ✅ (maintenu) |
| D-pad navigation | ✅ | ✅ (maintenu) |
| Focus scale 1.06x | ✅ | ✅ (Design System) |

---

## 5. Dépendances entre phases

```
Phase 1 (Fondations)
  ├── Phase 2 (Simples) ← utilise TvRowList, TvCardGrid
  │   └── Phase 3 (Médians) ← patterns rodés
  │       ├── Phase 4A (Jellyseerr) ← TvRowList + TvCardGrid
  │       ├── Phase 4B (Home) ← TvRowList + tous composants
  │       ├── Phase 4C (Playback) ← TvPlaybackControls
  │       ├── Phase 4D (Audio) ← TvRowList simple
  │       └── Phase 4E (LiveTV Guide) ← TvCardGrid + custom grid
  └── Phase 4F (Full Details) ← peut être fait à tout moment (probable suppression)

Phase 5 (Suppression) ← toutes les phases 2-4 terminées
```

**Note** : Les phases 4A-4E sont parallélisables entre elles si plusieurs développeurs.

---

## 6. Statut

- [x] Phase 1 — Fondations TV Compose (V4B — 2026-03-08)
- [x] Phase 2 — Écrans simples (9 écrans — V4C — 2026-03-08)
- [ ] Phase 3 — Écrans médians (10 écrans)
- [ ] Phase 4A — Jellyseerr Browse
- [ ] Phase 4B — Home Screen
- [ ] Phase 4C — Playback Overlay
- [ ] Phase 4D — Audio Now Playing
- [ ] Phase 4E — Live TV Guide
- [ ] Phase 4F — Full Details (legacy)
- [ ] Phase 5 — Suppression Leanback finale
