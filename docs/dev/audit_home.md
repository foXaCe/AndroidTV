# Audit Home Screen — VegafoX Android TV

**Date** : 2026-03-10
**Objectif** : Inventaire exhaustif avant redesign

---

## 1. Fichiers impliques

### 1.1 Ecran Home — Coeur Compose

| Fichier | Role |
|---------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeComposeFragment.kt` | Fragment wrapper, injecte HomeViewModel + ItemLauncher, cree ComposeView |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeScreen.kt` | Composable principal : Box(Backdrop + Column(Toolbar + TvRowList)) |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeViewModel.kt` | ViewModel : charge sections, emet `uiState` + `focusedItem` |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeHeroBackdrop.kt` | Backdrop plein ecran, crossfade 400ms, blur 8px, alpha 40% |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomePrefetchService.kt` | Prefetch Continue Watching + Next Up avant MainActivity |

### 1.2 Composants UI partages

| Fichier | Role |
|---------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvRowList.kt` | LazyColumn de LazyRow — pattern Netflix, remplace Leanback BrowseSupportFragment |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvFocusCard.kt` | Wrapper focus TV (tv-material3 Surface), scale 1.06x, border 2dp |
| `app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` | Card media : poster 150x225dp + titre + annee |
| `app/src/main/java/org/jellyfin/androidtv/ui/shared/toolbar/MainToolbar.kt` | Barre horizontale : avatar, boutons expandables, horloge |
| `app/src/main/java/org/jellyfin/androidtv/ui/shared/toolbar/ExpandableIconButton.kt` | Bouton icone qui s'expand en label au focus |
| `app/src/main/java/org/jellyfin/androidtv/ui/shared/toolbar/Toolbar.kt` | Layout toolbar (start / center / end) |
| `app/src/main/java/org/jellyfin/androidtv/ui/shared/toolbar/ToolbarButtons.kt` | Conteneur boutons avec overlay configurable |

### 1.3 Modele de donnees / Configuration

| Fichier | Role |
|---------|------|
| `app/src/main/java/org/jellyfin/androidtv/constant/HomeSectionType.kt` | Enum 13 types de sections (RESUME, NEXT_UP, LATEST_MEDIA, etc.) |
| `app/src/main/java/org/jellyfin/androidtv/preference/HomeSectionConfig.kt` | Config serialisable par section (type, enabled, order) |
| `app/src/main/java/org/jellyfin/androidtv/preference/UserSettingPreferences.kt` | Stockage homeSectionsJson + `activeHomesections` |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvRow.kt` | `data class TvRow<T>(title, items, key)` — structure d'une rangee |

### 1.4 Navigation

| Fichier | Role |
|---------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/navigation/Destinations.kt` | `val home = fragmentDestination<HomeComposeFragment>()` |
| `app/src/main/java/org/jellyfin/androidtv/ui/navigation/NavigationRepository.kt` | Stack<Destination>, SharedFlow<NavigationAction> |
| `app/src/main/java/org/jellyfin/androidtv/ui/browsing/MainActivity.kt` | Activity hote, observe NavigationRepository, DestinationFragmentView |
| `app/src/main/java/org/jellyfin/androidtv/ui/browsing/DestinationFragmentView.kt` | FrameLayout custom qui manage les fragments (fade in/out) |
| `app/src/main/res/layout/activity_main.xml` | Layout : background + content_view + settings + screensaver + exit_dialog |

### 1.5 Effets saisonniers (View system legacy)

| Fichier | Techno | Role |
|---------|--------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/home/SnowfallView.kt` | View (Canvas) | Flocons de neige animes |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/HalloweenView.kt` | View (Canvas) | Chauve-souris + citrouilles |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/PetalfallView.kt` | View (Canvas) | Petales (printemps) |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/LeaffallView.kt` | View (Canvas) | Feuilles (automne) |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/SummerView.kt` | View (Canvas) | Bulles d'ete |

**Note** : Les vues saisonniers sont configurees via `UserPreferences.seasonalSurprise` mais ne sont PAS integrees dans le flux HomeScreen actuel. Elles semblent etre un vestige ou un overlay non connecte.

### 1.6 Media Bar (Trailer YouTube)

| Fichier | Techno | Role |
|---------|--------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/home/mediabar/ExoPlayerTrailerView.kt` | Compose + ExoPlayer | Lecteur trailer YouTube |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/mediabar/TrailerResolver.kt` | Kotlin | Resoud URL trailer depuis metadonnees item |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/mediabar/YouTubeStreamResolver.kt` | Kotlin | Extrait stream URL via NewPipe |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/mediabar/SponsorBlockApi.kt` | Kotlin | Skip segments sponsor YouTube |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/mediabar/NewPipeDownloader.kt` | Kotlin | HTTP downloader pour NewPipe Extractor |

**Note** : Le MEDIA_BAR est un HomeSectionType mais `loadRows()` retourne `emptyList()` pour ce type. L'integration de la media bar est geree separement (pas dans le flow standard des rows).

### 1.7 Info Row (View system legacy)

| Fichier | Techno | Role |
|---------|--------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/home/SimpleInfoRowView.kt` | View (LinearLayout) | Affiche metadonnees (annee, rating, resolution, MDBList, TMDB) |

### 1.8 Ecrans Settings Home

| Fichier | Role |
|---------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/settings/screen/home/SettingsHomeScreen.kt` | Page settings home (sections, taille poster) |
| `app/src/main/java/org/jellyfin/androidtv/ui/settings/screen/home/SettingsHomeSectionScreen.kt` | Enable/disable + reorder sections |
| `app/src/main/java/org/jellyfin/androidtv/ui/settings/screen/home/SettingsHomePosterSizeScreen.kt` | Config taille poster |
| `app/src/main/java/org/jellyfin/androidtv/ui/settings/screen/vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | Override type image par section |
| `app/src/main/java/org/jellyfin/androidtv/ui/settings/routes/HomeRoutes.kt` | Routes navigation settings home |

---

## 2. Architecture technique actuelle

### 2.1 Technologie par composant

| Composant | Technologie | Leanback ? | View system ? |
|-----------|-------------|-----------|---------------|
| HomeScreen | **Compose** | Non | Non |
| HomeHeroBackdrop | **Compose** | Non | Non |
| TvRowList | **Compose** (LazyColumn/LazyRow) | Non | Non |
| TvFocusCard | **Compose** (tv-material3) | Non | Non |
| BrowseMediaCard | **Compose** | Non | Non |
| MainToolbar | **Compose** | Non | Non |
| ExpandableIconButton | **Compose** | Non | Non |
| SimpleInfoRowView | **View** (LinearLayout) | Non | **OUI** |
| Seasonal Views (5) | **View** (Canvas) | Non | **OUI** |
| MainActivity layout | **XML** (RelativeLayout) | Non | **OUI** |
| DestinationFragmentView | **View** (FrameLayout) | Non | **OUI** |

**Verdict** : L'ecran Home est a **~85% Compose**. Aucun composant Leanback ne subsiste dans le flux Home. Les restes View sont :
- `SimpleInfoRowView` (info row metadata)
- 5 vues saisonniers (non connectees)
- Layout `activity_main.xml` (conteneur minimal)
- `DestinationFragmentView` (infrastructure navigation)

### 2.2 Flux de navigation

```
StartupActivity (auth)
  -> navigationRepository.reset(Destinations.home)
  -> startActivity(MainActivity)

MainActivity.onCreate()
  -> SessionRepositoryState.READY
  -> setupActivity()
  -> observe navigationRepository.currentAction
  -> handleNavigationAction(NavigateFragment)
  -> DestinationFragmentView.navigate()
  -> HomeComposeFragment instancie

HomeComposeFragment.onCreateView()
  -> ComposeView + JellyfinTheme
  -> HomeScreen(viewModel, onItemClick)
```

### 2.3 Flux de donnees

```
Session Start
  |
  v
HomePrefetchService.prefetch()  [background, avant MainActivity]
  |-- loadContinueWatching()    -> API getResumeItems(VIDEO, max 50)
  |-- loadNextUp()              -> API getNextUp(max 50)
  v
HomeViewModel.init() -> loadRows()
  |
  |-- consume() prefetched data -> affichage immediat
  |-- Load activeHomesections en parallele :
  |     |-- RESUME         -> getResumeItems(VIDEO)
  |     |-- NEXT_UP        -> getNextUp()
  |     |-- LATEST_MEDIA   -> getLatestMedia() x N views
  |     |-- RECENTLY_RELEASED -> getItems(MOVIE|SERIES, sort PREMIERE_DATE)
  |     |-- LIBRARY_*      -> userViewsRepository.views
  |     |-- RESUME_AUDIO   -> getResumeItems(AUDIO)
  |     |-- ACTIVE_RECORDINGS -> getRecordings()
  |     |-- LIVE_TV        -> getRecommendedPrograms(isAiring)
  |     |-- PLAYLISTS      -> getItems(PLAYLIST)
  |     |-- MEDIA_BAR      -> emptyList() (gere separement)
  v
HomeUiState(rows: List<TvRow<BaseItemDto>>)
  |
  v
HomeScreen compose
  |-- HomeHeroBackdrop(focusedItem) -> crossfade backdrop
  |-- MainToolbar(Home)
  |-- TvRowList(rows) -> LazyColumn
        |-- per row: header + LazyRow
              |-- per item: BrowseMediaCard
                    |-- onFocus -> setFocusedItem() -> backdrop update
                    |-- onClick -> ItemLauncher.launch()
```

---

## 3. Inventaire visuel

### 3.1 Layout general

```
+------------------------------------------------------------------+
|                    HERO BACKDROP (plein ecran)                     |
|  (image floue 40% alpha, blur 8px, gradients V+H)               |
|                                                                   |
|  +--------------------------------------------------------------+|
|  | TOOLBAR : [Avatar] [Home] [Search] [Shuffle] [Genres]        ||
|  |           [Favorites] [Jellyseerr] [Folders] [SyncPlay]      ||
|  |           [Libraries...] [Settings]          [Clock]         ||
|  +--------------------------------------------------------------+|
|                                                                   |
|  Row 1 : "Continue Watching"                                     |
|  +-------+ +-------+ +-------+ +-------+ +-------+              |
|  | 150x  | | 150x  | | 150x  | | 150x  | | 150x  |  -> scroll  |
|  | 225dp | | 225dp | | 225dp | | 225dp | | 225dp |              |
|  +-------+ +-------+ +-------+ +-------+ +-------+              |
|  Titre     Titre     Titre     Titre     Titre                   |
|  2024      2023      2025      2024      2023                    |
|                                                                   |
|  Row 2 : "Next Up"                                               |
|  +-------+ +-------+ +-------+ ...                               |
|                                                                   |
|  Row N : "Latest in Movies"                                      |
|  +-------+ +-------+ +-------+ ...                               |
+------------------------------------------------------------------+
```

### 3.2 Detail des composants visuels

| Composant | Layout | Dimensions | Style |
|-----------|--------|-----------|-------|
| Hero Backdrop | `Box(fillMaxSize)` | Plein ecran | Crossfade 400ms, blur 8px (API 31+), alpha 0.4, gradient V (4 stops) + gradient H (3 stops) |
| Toolbar | `Row` horizontal | Largeur ecran, hauteur auto | Overlay configurable (couleur + opacite utilisateur) |
| Avatar button | `IconButton` | 36x36dp | Scale 1.06x au focus, border 2dp focus |
| Nav buttons | `ExpandableIconButton` | 36dp unfocused -> expand focused | Icone + label au focus, 150ms animation |
| Row header | `Text` | Auto | `titleMedium`, `textPrimary`, 1 ligne ellipsis |
| Card poster | `Box` + `AsyncImage` | 150x225dp (3:4) | Clip `small` (8dp), bg `surfaceDim`, placeholder gradient |
| Card titre | `Text` | Auto | `bodySmall`, `textPrimary`, 1 ligne ellipsis |
| Card annee | `Text` | Auto | `labelSmall`, `textSecondary`, 1 ligne |
| Card focus | `TvFocusCard` | Wrapper | Scale 1.06x focus / 0.95x press, border 2dp `focusRing` |
| Row spacing | Entre cards | 16dp (`spaceMd`) | Via `Arrangement.spacedBy` |
| Row separator | Entre rows | 16dp (`spaceMd`) | Via Spacer |
| Content padding | TvRowList | start=48dp, top=8dp, bottom=24dp | Tokens Space |

### 3.3 Absence de hero info

L'ecran Home actuel n'a **PAS** d'encart d'information hero (titre, description, bouton Play) au-dessus des rows. Le backdrop est purement decoratif — il change selon l'item focuse mais n'affiche aucune info textuelle sur l'item.

---

## 4. Navigation D-pad

| Action | Comportement |
|--------|-------------|
| Gauche/Droite | Scroll horizontal dans LazyRow (entre cards) |
| Haut/Bas | Scroll vertical entre rows dans LazyColumn |
| Haut depuis row 1 | Focus remonte vers Toolbar |
| Centre/Enter | `onClick` -> `ItemLauncher.launch()` |
| Back | `NavigationRepository.goBack()` ou exit dialog si home |
| Focus card | `onFocusChanged` -> `setFocusedItem()` -> backdrop crossfade |
| Focus toolbar button | Scale 1.06x + label expand |

**Accessibilite** : `TvFocusCard` respecte `rememberReducedMotion()` — scale desactivee si motion reduite.

---

## 5. Palette couleurs (VegafoX Dark Premium)

Source : `ui/base/theme/VegafoXColors.kt`

| Token | Valeur | Usage Home |
|-------|--------|-----------|
| `background` | `#0A0A0F` | Fond general, gradients backdrop |
| `surfaceDim` | `#0F0F14` | Fond cards (fallback sans image) |
| `textPrimary` | `#F5F0EB` | Titres cards, headers rows |
| `textSecondary` | `#9E9688` | Annee production |
| `textDisabled` | `#5C584F` | Icone fallback card sans image |
| `focusRing` | `#FF6B00` (orange) | Border focus cards + toolbar |
| `button` | Token theme | Fond boutons toolbar |
| `onButton` | Token theme | Texte boutons toolbar |
| `buttonActive` | Token theme | Bouton actif (Home quand sur Home) |
| `buttonFocused` | = focusColor | Bouton toolbar au focus |

**Focus color customisable** : `focusBorderColor()` lit `UserSettingPreferences` pour permettre une couleur focus personnalisee par utilisateur.

---

## 6. Animations

| Animation | Duree | Easing | Composant |
|-----------|-------|--------|-----------|
| Backdrop crossfade | 400ms | `tween` linear | `HomeHeroBackdrop` |
| Card focus scale | 150ms | `FastOutSlowInEasing` | `TvFocusCard` |
| Card press scale | 150ms | `FastOutSlowInEasing` | `TvFocusCard` |
| Button expand label | 150ms | Default | `ExpandableIconButton` |
| Avatar focus scale | Default | Default | `MainToolbar` |
| Image crossfade (Coil) | 200ms | N/A | `AnimationDefaults.IMAGE_CROSSFADE` |
| Skeleton shimmer | Continue | Gradient animation | `SkeletonCardRow` |
| Navigation fragment | Fade in/out | System | `DestinationFragmentView` |

---

## 7. Sections Home configurables

Source : `HomeSectionType.kt` + `HomeSectionConfig.kt`

| Type | Nom affiche (hardcode) | Defaut | Max items | API |
|------|----------------------|--------|-----------|-----|
| `RESUME` | "Continue Watching" | **ON** (order 0) | 50 | `getResumeItems(VIDEO)` |
| `NEXT_UP` | "Next Up" | **ON** (order 1) | 50 | `getNextUp()` |
| `LIVE_TV` | "On Now" | **ON** (order 2) | 50 | `getRecommendedPrograms(isAiring)` |
| `LATEST_MEDIA` | "Latest in {name}" | **ON** (order 3) | 15/view | `getLatestMedia()` |
| `RECENTLY_RELEASED` | "Recently Released" | off (order 4) | 15 | `getItems(MOVIE\|SERIES, PREMIERE_DATE)` |
| `LIBRARY_TILES_SMALL` | "My Media" | off (order 5) | All | `userViewsRepository.views` |
| `LIBRARY_BUTTONS` | "My Media" | off (order 6) | All | `userViewsRepository.views` |
| `RESUME_AUDIO` | "Continue Listening" | off (order 7) | 50 | `getResumeItems(AUDIO)` |
| `RESUME_BOOK` | *(non implemente)* | off (order 8) | - | `emptyList()` |
| `ACTIVE_RECORDINGS` | "Recordings" | off (order 9) | 50 | `getRecordings()` |
| `PLAYLISTS` | "Playlists" | off (order 10) | 15 | `getItems(PLAYLIST)` |
| `MEDIA_BAR` | *(gere separement)* | - | - | `emptyList()` dans loadRows |
| `NONE` | - | - | - | `emptyList()` |

**Mode merge** : `UserPreferences.mergeContinueWatchingNextUp` fusionne RESUME + NEXT_UP en une seule row "Continue Watching" (dedup par ID).

---

## 8. Dette technique

### 8.1 CRITIQUE — Textes hardcodes non traduits

Les titres de rows dans `HomeViewModel.kt` et `HomePrefetchService.kt` sont des **strings hardcodees en anglais**, pas des string resources :

| Fichier | Ligne | Texte hardcode |
|---------|-------|---------------|
| `HomeViewModel.kt:204` | `"Continue Watching"` | Devrait utiliser `R.string.home_section_resume` |
| `HomeViewModel.kt:216` | `"Next Up"` | Devrait utiliser `R.string.home_section_next_up` |
| `HomeViewModel.kt:249` | `"Continue Watching"` | Idem |
| `HomeViewModel.kt:273` | `"Latest in ${view.name}"` | Format non traduit |
| `HomeViewModel.kt:299` | `"Recently Released"` | Devrait utiliser string resource |
| `HomeViewModel.kt:305` | `"My Media"` | Devrait utiliser string resource |
| `HomeViewModel.kt:319` | `"Continue Listening"` | Devrait utiliser string resource |
| `HomeViewModel.kt:330` | `"Recordings"` | Devrait utiliser string resource |
| `HomeViewModel.kt:343` | `"On Now"` | Devrait utiliser string resource |
| `HomeViewModel.kt:358` | `"Playlists"` | Devrait utiliser string resource |
| `HomePrefetchService.kt:80` | `"Continue Watching"` | Duplique de HomeViewModel |
| `HomePrefetchService.kt:92` | `"Next Up"` | Duplique de HomeViewModel |

**Impact** : Toutes les langues affichent les titres en anglais.

### 8.2 HAUTE — Composants View system a migrer Compose

| Composant | Fichier | Priorite | Raison |
|-----------|---------|----------|--------|
| `SimpleInfoRowView` | `ui/home/SimpleInfoRowView.kt` | Haute | 288 lignes, View LinearLayout avec TextViews pre-allouees, utilise KoinComponent directement |
| Seasonal Views (5) | `ui/home/Snowfall\|Halloween\|Petalfall\|Leaffall\|SummerView.kt` | Moyenne | Canvas-based, non connectees au flux Home actuel |
| `activity_main.xml` | `res/layout/activity_main.xml` | Basse | RelativeLayout minimal, sert de conteneur — peu d'interet a migrer |

### 8.3 MOYENNE — Problemes architecturaux

| Probleme | Detail |
|----------|--------|
| **Duplication prefetch/VM** | `HomePrefetchService` duplique la logique de `loadContinueWatching()` et `loadNextUp()` de `HomeViewModel` |
| **ViewModel accede ApiClient directement** | `HomeViewModel.api` est public, expose dans les composables — devrait passer par un repository |
| **Log.d STARTUP** | 4 `Log.d("STARTUP", ...)` restants dans code production (HomeViewModel, HomePrefetchService, HomeComposeFragment) |
| **Coroutine scope leak** | `HomePrefetchService` cree un `CoroutineScope(SupervisorJob())` sans jamais le cancel |
| **LIBRARY_TILES_SMALL et LIBRARY_BUTTONS** identiques | Les deux appellent `loadLibraryViews()` et retournent le meme resultat — pas de differentiation visuelle |
| **RESUME_BOOK** non implemente | Retourne `emptyList()` — devrait etre retire ou implemente |
| **CoroutineScope dans Toolbar** | `MainToolbar.kt:389,499` cree `CoroutineScope(Dispatchers.Main)` hors lifecycle — risque de leak |
| **Pas de pagination** | Les rows chargent max 50 items en une fois, pas de lazy loading supplementaire |

### 8.4 BASSE — Couleurs / tokens

| Probleme | Detail |
|----------|--------|
| `SimpleInfoRowView` hardcode | `android.R.color.white`, `android.R.color.black`, `14sp`, `3f shadowLayer` — hors design tokens VegafoX |
| `AppBackground` utilise `R.color.background_filter` | Resource XML au lieu de token Compose |
| `Spacer(6.dp)` dans BrowseMediaCard | Valeur inline au lieu de `Tokens.Space.spaceXs` (4dp) ou `spaceSm` (8dp) |

### 8.5 BASSE — Manques fonctionnels

| Manque | Detail |
|--------|--------|
| **Pas de hero info** | Aucun titre/description/bouton Play sur le backdrop — purement decoratif |
| **Pas de badge "Non lu"** | Les cards n'indiquent pas si l'item est lu/non-lu |
| **Pas de barre de progression** | Continue Watching n'affiche pas la progression de lecture |
| **Pas de type-specific cards** | Toutes les cards sont identiques (poster + titre + annee) — pas de difference visuelle entre films, series, live TV, playlists |
| **Pas de long-press** | Les cards ne reagissent pas au long-press (pas de menu contextuel) |

---

## 9. Changements priorises

### P0 — Bloquant i18n

1. **Externaliser les titres de rows** dans `strings.xml` (HomeViewModel + HomePrefetchService)
2. Verifier que toutes les string resources `home_section_*` existent dans les traductions FR, ES, DE, etc.

### P1 — UX critique

3. **Ajouter hero info overlay** : titre, description, genres, bouton Play/Resume sur le backdrop quand un item est focuse
4. **Ajouter barre de progression** sur les cards "Continue Watching"
5. **Ajouter badge non-lu/nouveau** sur les cards pertinentes

### P2 — Qualite code

6. **Supprimer les `Log.d("STARTUP")`** en production
7. **Corriger les CoroutineScope leaks** dans HomePrefetchService et MainToolbar
8. **Deduire la logique prefetch** pour eviter la duplication avec HomeViewModel
9. **Differencier LIBRARY_TILES_SMALL vs LIBRARY_BUTTONS** visuellement ou fusionner

### P3 — Migration View -> Compose

10. **Migrer SimpleInfoRowView** vers un composable (si utilise sur Home)
11. **Migrer ou supprimer les Seasonal Views** — soit les connecter au flux Home, soit les supprimer
12. **Migrer les tokens hardcodes** de SimpleInfoRowView vers VegafoXColors

### P4 — Ameliorations futures

13. Cards specifiques par type (film, serie, live TV, playlist, musique)
14. Long-press menu contextuel sur les cards
15. Pagination / infinite scroll des rows
16. Animation d'entree stagger sur les rows au premier chargement

---

## 10. Resume

| Metrique | Valeur |
|----------|--------|
| Fichiers Home total | ~35 |
| % Compose | ~85% |
| % Leanback | **0%** (aucun) |
| % View system residuel | ~15% (SimpleInfoRowView, saisonnier, layout XML) |
| Strings hardcodees | **12** (critique) |
| CoroutineScope leaks | 2 |
| Sections configurables | 13 (dont 2 non impl, 1 geree separement) |
| Sections actives par defaut | 4 (Resume, Next Up, Live TV, Latest) |

L'ecran Home a deja ete largement migre vers Compose avec une architecture propre (ViewModel + StateFlow + Compose). Les principaux chantiers restants sont l'internationalisation des titres de rows, l'ajout d'un hero info overlay, et la migration des quelques composants View residuels.
