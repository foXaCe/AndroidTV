# Audit — Browse / Bibliothèques

> Date : 2026-03-12
> Scope : Écrans #6–#19 du master audit (`audit_screens.md` §2)

## Screenshots

| Écran | Fichier |
|-------|---------|
| Home + PremiumSideBar | `docs/screenshots/browse_home_sidebar.png` |
| Films — LibraryBrowseScreen | `docs/screenshots/browse_library_films.png` |
| Films — By Letter (R) | `docs/screenshots/browse_letter_r.png` |
| Genres Grid | ⚠ **Non capturé** — inaccessible depuis la PremiumSideBar (bouton Genres absent) |

---

## Bug corrigé pendant l'audit

**PremiumSideBar — navigation morte** (`PremiumSideBar.kt`)
- **Symptôme** : Cliquer sur n'importe quel bouton de la sidebar ne fait rien
- **Cause** : Chaque `onSelect` ne faisait que `selectedIndex = N` sans appel navigation
- **Fix** : Injection de `NavigationRepository`, `UserViewsRepository`, `ItemLauncher`, `SettingsViewModel` + câblage de chaque bouton vers `Destinations.*`
- **État** : Corrigé, testé, fonctionnel

---

## Résumé par écran

### #6 — LibraryBrowseScreen (Films, Séries)
- **Fichier** : `ui/browsing/compose/LibraryBrowseScreen.kt`
- **Status** : **C** (mix VegafoX / Jellyfin)
- **Ce qui s'affiche** : TvScaffold → TvHeader (nom + count) → FilterChipsRow (tri, favoris, lu, lettre) → LazyVerticalGrid paginée
- **Couleurs** : `JellyfinTheme.colorScheme` partout (bridge VegafoX → Material), pas de VegafoXColors direct
- **Boutons** : `LibraryToolbarButton` custom (pas VegafoXButton), FilterChip/LetterChip avec focus animé
- **Cards** : `LibraryPosterCard` (scale 1.05f, watched indicator, progress bar)
- **Toolbar** : Home + Settings (gear) en haut à droite, icônes VegafoXIcons
- **Focus D-pad** : `collectIsFocusedAsState()` sur chips et cards, scale/alpha animés
- **Non-VegafoX** : FilterChip, LibraryToolbarButton, LibraryPosterCard (pas VegafoXButton/VegafoXGradients)
- **Effort** : **M** — Remplacer FilterChip→VegafoXButton style, LibraryToolbarButton→VegafoXButton ghost, ajouter gradient hero optionnel

### #7 — FolderBrowseScreen
- **Fichier** : `ui/browsing/compose/FolderBrowseScreen.kt`
- **Status** : **C**
- **Ce qui s'affiche** : AppBackground + dark overlay → FolderHeader → FolderRows (verticalScroll + LazyRow par row)
- **Couleurs** : `JellyfinTheme.colorScheme`, `Color.White.copy(alpha=0.7f)` hardcodé sur subtitle
- **Cards** : FolderItemCard custom — scale 1.08f/alpha 0.75f focus, pas de border focus
- **Non-VegafoX** : Background style Jellyfin (AppBackground), cards sans VegafoX focus glow
- **Effort** : **M** — Background→VegafoXGradients, cards→TvFocusCard avec glow orange

### #8 — GenresGridScreen
- **Fichier** : `ui/browsing/compose/GenresGridScreen.kt`
- **Status** : **C**
- **Ce qui s'affiche** : TvScaffold → TvHeader → TvCardGrid (5 colonnes) avec GenreCard
- **Couleurs** : `Color.White` et `Color.Black.copy(alpha=0.75f)` hardcodés dans GenreCard gradient
- **Boutons** : Dialogs (GenreSortDialog, LibraryFilterDialog) avec DialogRadioItem — pas VegafoXButton
- **Cards** : TvFocusCard wrapping genre overlay — Jellyfin focus style
- **Non-VegafoX** : Couleurs hardcodées, dialogs Material, pas de VegafoXButton
- **Effort** : **S** — Remplacer hardcoded colors, dialog buttons→VegafoXButton
- **⚠ Accessibilité** : Non atteignable depuis PremiumSideBar (bouton Genres manquant)

### #9 — ByLetterBrowseScreen
- **Fichier** : `ui/browsing/compose/ByLetterBrowseScreen.kt`
- **Status** : **B** (Jellyfin quasi-original)
- **Ce qui s'affiche** : TvScaffold → TvHeader → TvRowList avec BrowseMediaCard
- **Couleurs** : `JellyfinTheme.colorScheme` via bridge
- **Cards** : `BrowseMediaCard` — utilise `VegafoXColors.OrangePrimary` pour progress bar
- **Toolbar** : Home + Settings en haut à droite (VegafoXIcons)
- **Non-VegafoX** : Pas de toolbar buttons VegafoX, pas de filter chips, TvHeader stock
- **Effort** : **S** — Minimal, principalement cosmétique (toolbar buttons style)

### #10 — CollectionBrowseScreen
- **Fichier** : `ui/browsing/compose/CollectionBrowseFragment.kt` (réutilise LibraryBrowseScreen)
- **Status** : **B**
- **Même layout que LibraryBrowseScreen** avec args différents
- **Effort** : **S** — Suit automatiquement les améliorations de LibraryBrowseScreen

### #11 — LiveTvBrowseFragment
- **Fichier** : `ui/browsing/v2/LiveTvBrowseFragment.kt`
- **Status** : **C**
- **Ce qui s'affiche** : ComposeView → AppBackground + overlay → Header → LazyRow rows → Nav cards (Guide, Recordings, Schedule)
- **Couleurs** : `JellyfinTheme.colorScheme`, VegafoXIcons
- **Cards** : Custom nav cards + content cards (scale 1.08f/alpha 0.75f focus)
- **Non-VegafoX** : AppBackground, pas de VegafoXButton pour nav cards, pas de glow focus
- **Effort** : **M** — Nav cards→VegafoXButton, background→VegafoXGradients, focus→glow

### #12 — MusicBrowseFragment
- **Fichier** : `ui/browsing/v2/MusicBrowseFragment.kt`
- **Status** : **C**
- **Même pattern que LiveTvBrowse** — ComposeView + AppBackground + LazyRow rows
- **Nav cards** : Albums, Artists, Album Artists, Genres, Playlists
- **Non-VegafoX** : Identique à LiveTvBrowse
- **Effort** : **M**

### #13 — ScheduleBrowseFragment
- **Fichier** : `ui/browsing/v2/ScheduleBrowseFragment.kt`
- **Status** : **C**
- **Même pattern V2** — ComposeView + LazyRow
- **Effort** : **M**

### #14 — RecordingsBrowseFragment
- **Fichier** : `ui/browsing/v2/RecordingsBrowseFragment.kt`
- **Status** : **C**
- **Même pattern V2**
- **Effort** : **M**

### #15 — SeriesRecordingsBrowseFragment
- **Fichier** : `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt`
- **Status** : **C**
- **Même pattern V2**
- **Effort** : **M**

### #16 — LibraryBrowseComponents (shared)
- **Fichier** : `ui/browsing/v2/LibraryBrowseComponents.kt`
- **Status** : **C**
- **Composants** : LibraryPosterCard, LibraryToolbarButton, AlphaPickerBar, FocusedItemHud, LibraryStatusBar, FilterSortDialog
- **LibraryPosterCard** : `AnimationDefaults.FOCUS_SCALE` (1.05f), `focusBorderColor`, watched/progress indicators
- **FocusedItemHud** : Marquee titre + metadata — `JellyfinTheme.colorScheme`
- **FilterSortDialog** : Material dialog style
- **Non-VegafoX** : Tous les composants utilisent le bridge JellyfinTheme, aucun VegafoXButton/VegafoXGradients
- **Effort** : **L** — Composants partagés, impact sur tous les browse screens

### #17 — BrowseMediaCard (shared)
- **Fichier** : `ui/browsing/compose/BrowseMediaCard.kt`
- **Status** : **A** ✓ (un des rares browse components pleinement VegafoX)
- **Cards 16:9** avec TvFocusCard, progress bar `VegafoXColors.OrangePrimary`, badge "NOUVEAU"
- **Image priority** : THUMB → BACKDROP → parent fallbacks → PRIMARY
- **Effort** : Aucun

### #18 — SuggestedMoviesFragment
- **Fichier** : `ui/browsing/compose/SuggestedMoviesFragment.kt`
- **Status** : **C**
- **Réutilise** LibraryBrowseScreen avec mode suggestions
- **Effort** : **S** — Suit LibraryBrowseScreen

---

## Synthèse

| Status | Count | Écrans |
|--------|-------|--------|
| **A** | 1 | BrowseMediaCard |
| **B** | 2 | ByLetterBrowse, CollectionBrowse |
| **C** | 11 | LibraryBrowse, FolderBrowse, GenresGrid, LiveTvBrowse, MusicBrowse, Schedule, Recordings, SeriesRecordings, LibraryBrowseComponents, SuggestedMovies, HomeScreen |

### Éléments non-VegafoX récurrents

1. **`JellyfinTheme.colorScheme`** au lieu de `VegafoXColors` direct — OK grâce au bridge, mais les gradients/glows premium sont absents
2. **`LibraryToolbarButton`** — pas VegafoXButton, pas de glow/scale
3. **`FilterChip` / `LetterChip`** — custom mais sans le design system VegafoX
4. **`AppBackground`** — fond Jellyfin classique au lieu de VegafoXGradients.ScreenBackground
5. **`FilterSortDialog`** — dialog Material, pas VegafoX styled
6. **Cards focus** : scale/alpha sans glow orange (sauf BrowseMediaCard)
7. **PremiumSideBar** : manque boutons Genres, Favoris, Shuffle, Jellyseerr, SyncPlay, Dossiers (tous présents dans LeftSidebarNavigation)

### Effort estimé (migration VegafoX complète)

| Taille | Effort | Détail |
|--------|--------|--------|
| **S** | ~2-4h | ByLetterBrowse, CollectionBrowse, GenresGrid, SuggestedMovies |
| **M** | ~4-8h | LibraryBrowse, FolderBrowse, LiveTvBrowse, MusicBrowse, Schedule, Recordings, SeriesRecordings |
| **L** | ~8-12h | LibraryBrowseComponents (composants partagés, effet cascade) |
| **Total** | ~30-40h | Migration complète browse screens |

### Priorité recommandée

1. **P0** — LibraryBrowseComponents (L) — impact cascade sur tous les browse screens
2. **P1** — LibraryBrowseScreen (M) — écran le plus visible/utilisé
3. **P1** — PremiumSideBar complétion — ajouter Genres, Favoris, Shuffle (accessibilité)
4. **P2** — V2 fragments (LiveTv, Music, Schedule, Recordings) — pattern identique, batch possible
5. **P3** — FolderBrowse, GenresGrid, ByLetter — moins fréquentés
