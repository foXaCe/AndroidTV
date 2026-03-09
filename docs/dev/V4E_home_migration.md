# V4E — Migration HomeFragment → Compose TV

**Date** : 2026-03-08
**Statut** : ✅ Terminé
**Build** : BUILD SUCCESSFUL (assembleGithubDebug)
**Tests** : checkAll PASS (lint + unit tests)

---

## Résumé

Migration de l'écran d'accueil (HomeFragment) de Leanback vers Compose TV. Le nouveau `HomeScreen` utilise les fondations V4B (`TvRowList`, `TvFocusCard`, `BrowseMediaCard`) et le pattern Fragment wrapper V4C.

---

## Fichiers créés (4 fichiers, 643 LOC)

| Fichier | LOC | Rôle |
|---------|-----|------|
| `ui/home/compose/HomeViewModel.kt` | 350 | ViewModel — chargement parallèle de toutes les sections home |
| `ui/home/compose/HomeHeroBackdrop.kt` | 129 | Backdrop crossfade avec gradients et blur |
| `ui/home/compose/HomeScreen.kt` | 123 | Écran principal Compose (toolbar + rows + backdrop) |
| `ui/home/compose/HomeComposeFragment.kt` | 41 | Fragment wrapper (ComposeView + JellyfinTheme) |

## Fichiers modifiés (3 fichiers)

| Fichier | Modification |
|---------|-------------|
| `ui/navigation/Destinations.kt` | `home` → `fragmentDestination<HomeComposeFragment>()` |
| `di/AppModule.kt` | Ajout `viewModel { HomeViewModel(...) }` |
| `ui/browsing/compose/BrowseMediaCard.kt` | Ajout paramètre `onFocus` pour backdrop |

---

## Comparaison avant → après

### Ancien système (Leanback)

| Fichier | LOC |
|---------|-----|
| `HomeFragment.kt` | 348 |
| `HomeRowsFragment.kt` | 587 |
| `HomeFragmentHelper.kt` | 197 |
| `fragment_home.xml` | 197 |
| + 6 fichiers HomeFragment*Row.kt | ~500 |
| + presenters (CardPresenter, etc.) | ~300 |
| **Total estimé** | **~2129** |

### Nouveau système (Compose TV)

| Fichier | LOC |
|---------|-----|
| `HomeViewModel.kt` | 350 |
| `HomeHeroBackdrop.kt` | 129 |
| `HomeScreen.kt` | 123 |
| `HomeComposeFragment.kt` | 41 |
| **Total** | **643** |

**Réduction** : ~70% de code en moins (2129 → 643 LOC).

---

## Architecture HomeHeroBackdrop

```
┌─────────────────────────────────┐
│  Box (fillMaxSize)              │
│  ┌───────────────────────────┐  │
│  │ Crossfade (400ms)         │  │
│  │  ├─ AsyncImage backdrop   │  │
│  │  │  ├─ graphicsLayer      │  │
│  │  │  │  alpha = 0.4f       │  │
│  │  │  │  blur = 8px (API31+)│  │
│  │  │  └─ ContentScale.Crop  │  │
│  │  └─ (null → Box vide)     │  │
│  ├───────────────────────────┤  │
│  │ Gradient vertical         │  │
│  │  00%→30%→70%→100%         │  │
│  │  transparent → background │  │
│  ├───────────────────────────┤  │
│  │ Gradient horizontal       │  │
│  │  80%→40%→transparent      │  │
│  │  background → transparent │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### Comportement
- **Source image** : Backdrop > Thumb > Parent backdrop > Parent thumb (fallback chain)
- **Transition** : `Crossfade` 400ms avec `tween(easing = EaseInOut)` via `AnimationDefaults`
- **Blur** : `RenderEffect.createBlurEffect(8f, 8f)` sur API 31+, dégradé seulement en dessous
- **Opacité** : 40% via `graphicsLayer { alpha = 0.4f }` (performant, pas de recomposition)
- **Mise à jour** : `derivedStateOf` sur `item?.id` pour éviter les recompositions inutiles
- **Focus** : `BrowseMediaCard.onFocus` → `HomeViewModel.setFocusedItem()` → backdrop update

---

## Sections home supportées

| HomeSectionType | Implémenté | API |
|-----------------|-----------|-----|
| RESUME | ✅ | `itemsApi.getResumeItems(GetResumeItemsRequest)` |
| NEXT_UP | ✅ | `tvShowsApi.getNextUp(GetNextUpRequest)` |
| LATEST_MEDIA | ✅ | `userLibraryApi.getLatestMedia()` par vue |
| RECENTLY_RELEASED | ✅ | `itemsApi.getItems()` triés par premiere date |
| LIBRARY_TILES_SMALL | ✅ | `userViewsRepository.views` |
| LIBRARY_BUTTONS | ✅ | idem |
| RESUME_AUDIO | ✅ | `itemsApi.getResumeItems()` media=AUDIO |
| ACTIVE_RECORDINGS | ✅ | `liveTvApi.getRecordings()` |
| LIVE_TV | ✅ | `liveTvApi.getRecommendedPrograms()` |
| PLAYLISTS | ✅ | `itemsApi.getItems()` type=PLAYLIST |
| MEDIA_BAR | ⏭️ | Géré séparément (MediaBarSlideshowViewModel) |
| RESUME_BOOK | ⏭️ | Pas de support dans l'app existante |
| NONE | ✅ | Ignoré |

### Merge Continue Watching + Next Up
Quand `UserPreferences.mergeContinueWatchingNextUp` est activé, les items Resume et Next Up sont fusionnés dans une seule row "Continue Watching" avec déduplication par ID.

---

## Imports Leanback restants

| Métrique | Avant V4E | Après V4E |
|----------|-----------|-----------|
| Fichiers avec imports Leanback | 60 | 60 |
| Total imports Leanback | 209 | 209 |

> Note : Le nombre n'a pas changé car les anciens fichiers HomeFragment/HomeRowsFragment sont toujours dans le codebase (non supprimés). La navigation pointe désormais vers `HomeComposeFragment` mais les anciens fichiers restent comme référence pour la migration future de MediaBar.

---

## Optimisations performance

- **`@Stable`** sur `HomeUiState` — évite les recompositions inutiles
- **`key =`** dans `TvRowList` — recyclage correct des items
- **`derivedStateOf`** dans `HomeHeroBackdrop` — dérive l'URL sans recomposition
- **Chargement parallèle** — toutes les sections chargées via `async/await` dans `Dispatchers.IO`
- **Pre-fetch** — vues et live TV vérifiés en parallèle avant le chargement des sections
- **`graphicsLayer { alpha = 0.4f }`** — opacité sans recomposition (layer GPU)

---

## Prochaines étapes

- [ ] Migration du MediaBar (slideshow + trailers) vers Compose
- [ ] Suppression des anciens HomeFragment/HomeRowsFragment/HomeFragmentHelper
- [ ] Localisation des titres de rows (actuellement en anglais hardcodé)
- [ ] Test sur appareil AM9 Pro
