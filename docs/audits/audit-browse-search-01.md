# Audit visuel Browse + Search

**Date** : 2026-03-15
**Scope** : 9 fichiers (browse screens, search, audio player)
**Methode** : Lecture exhaustive de chaque composable, imports, valeurs hardcodees

---

## Reference Design System

| Composant | Fichier |
|-----------|---------|
| Couleurs | `VegafoXColors` (0xFF0A0A0F background, 0xFFFF6B00 orange, etc.) |
| Typographies | `BebasNeue`, `JetBrainsMono` (VegafoXTypography.kt) |
| Dimensions | `BrowseDimensions`, `CardDimensions`, `ButtonDimensions`, etc. |
| Boutons | `VegafoXButton` / `VegafoXIconButton` (glass dark premium) |
| Icones | `VegafoXIcons` (registre centralise) |
| Carte media | `BrowseMediaCard` (compose/) utilisant `TvFocusCard` |
| Theme wrapper | `JellyfinTheme.colorScheme` = proxy vers `VegafoXColors` |

**Convention** : Toute couleur doit venir de `VegafoXColors.*` ou `JellyfinTheme.colorScheme.*` (qui en est un proxy). Les `Tokens.Space.*` du module `design` sont acceptables comme spacing tokens systeme.

---

## 1. LibraryBrowseScreen.kt

**Fichier** : `ui/browsing/compose/LibraryBrowseScreen.kt` (609 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | Toutes via `VegafoXColors.*` ou `JellyfinTheme.colorScheme.*` |
| Typographies | :green_circle: OK | `JellyfinTheme.typography.*` + `BebasNeue` pour le titre |
| Dimensions | :yellow_circle: Moyen | Utilise `BrowseDimensions.gridPaddingHorizontal` mais hardcode `32.dp` (top padding header), `8.dp`, `12.dp`, `16.dp` (spacers), `50.dp` (chip RoundedCornerShape), `44.dp` (toolbar button) |
| Boutons | :yellow_circle: Moyen | `LibraryToolbarButton` custom (pas `VegafoXIconButton`) ; `FilterChip` et `LetterChip` sont custom et non des `VegafoXButton` |
| Icones | :green_circle: OK | Toutes via `VegafoXIcons.*` (Sort, Home, Settings) |
| dp literals | :yellow_circle: Moyen | `32.dp`, `8.dp`, `12.dp`, `16.dp`, `50.dp` (chip shapes), `6.dp`, `16.dp`, `13.sp`, `40.sp`, `14.sp`, `2.sp` hardcodes |
| sp literals | :yellow_circle: Moyen | `fontSize = 40.sp`, `13.sp`, `14.sp`, `2.sp` -- pas de tokens typo centralises pour ces tailles |

**Valeurs hardcodees residuelles :**
- `top = 32.dp` (header padding) -- pas dans `BrowseDimensions`
- `fontSize = 40.sp` (titre) -- pas dans `VegafoXDimensions`
- `fontSize = 14.sp` (sous-titre count) -- pas de token
- `fontSize = 13.sp` (chips) -- pas de token
- `letterSpacing = 2.sp` -- pas de token
- Chip shape `RoundedCornerShape(50.dp)` -- pas de token
- Chip padding `16.dp, 8.dp` -- pas de token
- Letter chip size `32.dp` -- pas de token

### Point 2 -- Headers et titres de page

| Element | Valeur | Coherence |
|---------|--------|-----------|
| Titre composable | `Text` inline | Direct dans `LibraryBrowseScreen` (pas `TvHeader`) |
| Typographie | `headlineLarge.copy(fontSize=40.sp, fontFamily=BebasNeue, fontWeight=Bold, letterSpacing=2.sp)` | :green_circle: Coherent avec les autres ecrans browse (ByLetter, Search) |
| Couleur | `VegafoXColors.TextPrimary` | :green_circle: OK |
| Padding | `top=32.dp, start/end=BrowseDimensions.gridPaddingHorizontal` | :green_circle: OK, meme pattern que ByLetter et Search |

:yellow_circle: Le titre n'utilise **pas** `TvHeader` (composant design system). Les ecrans CollectionBrowse et SuggestedMovies utilisent `TvHeader`, causant une **inconsistance visuelle** : `TvHeader` utilise `titleLarge` sans BebasNeue, alors que LibraryBrowse utilise `headlineLarge + BebasNeue 40sp`.

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable grille | `LazyVerticalGrid` |
| Colonnes | `GridCells.Adaptive(minSize = (cardWidth + 16).dp)` -- adaptatif |
| Carte | `LibraryPosterCard` (pas `BrowseMediaCard`) |
| Dimensions carte | Dynamiques selon `imageTypeToCardDimensions()` (100-340dp largeur) |
| Espacement H | `12.dp` (hardcode) |
| Espacement V | `16.dp` (hardcode) |
| contentPadding bottom | `27.dp` (TV safe area match) |

:yellow_circle: `LibraryPosterCard` et `BrowseMediaCard` sont **deux composants de carte differents** qui coexistent. `LibraryPosterCard` est plus riche (badge type, watched indicator, seekbar, labels) tandis que `BrowseMediaCard` est plus simple (landscape 16:9 uniquement). Pas d'unification.

:yellow_circle: Espacement `12.dp` horizontal ne correspond pas a `TvSpacing.cardGap` (16.dp) du design system.

### Point 4 -- Etats vides et etats de chargement

| Etat | Composant | Strings |
|------|-----------|---------|
| Loading | `SkeletonCardGrid()` | :green_circle: Pas de string |
| Empty | `EmptyState(title=R.string.state_empty_library, message=R.string.state_empty_library_message)` | :green_circle: strings.xml |
| Error | `ErrorState(message=R.string.state_error_generic, onRetry)` | :green_circle: strings.xml |
| Pagination skeleton | `SkeletonCard(width, height)` -- 5 items en fin de grille | :green_circle: OK |

:green_circle: Tous les etats sont geres, avec `StateContainer` et `DisplayState` enum.

### Point 5 -- Filtres et tri

| Filtre | Type | Composant |
|--------|------|-----------|
| Sort | Chip + Dialog (`FilterSortDialog`) | `FilterChip` custom + icone `VegafoXIcons.Sort` |
| Favorites | Toggle chip | `FilterChip` custom |
| Played status | Cycle chip (All/Unwatched/Watched) | `FilterChip` custom |
| Series status | Cycle chip (All/Continuing/Ended) | `FilterChip` custom |
| Letter picker | Inline A-Z | `LetterChip` custom |

:yellow_circle: Les chips de filtre sont des composants **custom inline** (pas `VegafoXButton`). Ils ont un style coherent entre eux (spring scale, border orange au focus, OrangeSoft background quand actif) mais ne suivent pas le pattern `VegafoXButton` du design system.

:green_circle: Le `FilterSortDialog` utilise bien `VegafoXButton` (Ghost + Primary) pour les actions Cancel/OK.

### Point 8 -- Navigation et focus

| Element | Status |
|---------|--------|
| Focus initial | `firstItemFocusRequester.requestFocus()` sur le premier item de la grille | :green_circle: OK |
| D-pad | `LazyVerticalGrid` gere nativement | :green_circle: OK |
| focusGroup | Via `TvScaffold` (qui applique `.focusGroup().focusRestorer()`) | :green_circle: OK |

:yellow_circle: Pas de `focusGroup` explicite sur la `FilterChipsRow` (LazyRow) -- le focus pourrait sauter entre la toolbar et les chips de maniere inattendue.

---

## 2. FolderBrowseScreen.kt

**Fichier** : `ui/browsing/compose/FolderBrowseScreen.kt` (347 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :yellow_circle: Moyen | Mix de `VegafoXColors.*` et `JellyfinTheme.colorScheme.*` dans le meme fichier |
| Typographies | :green_circle: OK | `JellyfinTheme.typography.*` + `BebasNeue` pour le titre |
| Dimensions | :yellow_circle: Moyen | `BrowseDimensions.contentPaddingHorizontal` utilise, mais `12.dp`, `4.dp`, `6.dp`, `5.dp`, `140`, `210` hardcodes |
| Boutons | :green_circle: OK | `LibraryToolbarButton` (coherent avec LibraryBrowse) |
| Icones | :green_circle: OK | `VegafoXIcons.Home` |

**Valeurs hardcodees residuelles :**
- `FolderItemCard` : `cardWidth = 140`, `cardHeight = 210` (Int, pas dp tokens ni `CardDimensions`) -- :red_circle: ces dimensions ne viennent pas du design system (`CardDimensions.portraitWidth = 150.dp`, `portraitHeight = 225.dp`)
- `scale = 1.08f` / `alpha = 0.75f` -- pas de token d'animation
- `Color.Transparent` utilise directement
- `JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.06f)` et `.copy(alpha = 0.20f)` -- opacites hardcodees
- Row title : `JellyfinTheme.colorScheme.onSurface` au lieu de `VegafoXColors.TextPrimary` (semantiquement identique mais inconsistant avec les autres ecrans)

### Point 2 -- Headers et titres de page

| Element | Valeur |
|---------|--------|
| Titre composable | `Text` dans `FolderHeader` |
| Typographie | `headlineLarge.copy(fontSize=40.sp, fontFamily=BebasNeue, fontWeight=Bold, letterSpacing=2.sp)` |
| Couleur | `VegafoXColors.TextPrimary` |
| Alignement | Centre (`Alignment.Center`) |
| Padding | `start/end=BrowseDimensions.contentPaddingHorizontal, top=12.dp, bottom=4.dp` |

:yellow_circle: Padding top `12.dp` est different de `32.dp` dans LibraryBrowse et Search -- **inconsistance entre ecrans browse**.

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable | `Column + verticalScroll` + `LazyRow` par row |
| Colonnes | N/A -- layout horizontal en rows |
| Carte | `FolderItemCard` custom (pas `BrowseMediaCard` ni `LibraryPosterCard`) |
| Dimensions carte | `140.dp x 210.dp` hardcode (portrait) |
| Espacement entre cartes | `12.dp` dans `LazyRow` |

:red_circle: **Troisieme type de carte** : `FolderItemCard` est un composant inline custom, different de `BrowseMediaCard` et `LibraryPosterCard`. Pas de focus border (utilise `drawBehind` avec glow a la place de border orange). Pas de progress bar, pas de badge, pas de watched indicator.

:yellow_circle: `Column + verticalScroll` au lieu de `LazyColumn` -- pas de recyclage, probleme potentiel avec de longues listes.

### Point 4 -- Etats vides et etats de chargement

| Etat | Composant |
|------|-----------|
| Loading | `SkeletonCardRow()` x2 | :green_circle: OK |
| Empty | `EmptyState(R.string.state_empty_library)` | :green_circle: OK |
| Error | `ErrorState` avec retry | :green_circle: OK |

:green_circle: Etats bien geres.

### Point 8 -- Navigation et focus

:yellow_circle: Pas de `focusRequester` pour le focus initial -- le focus va au premier element focusable (probablement le bouton Home dans le header, pas la premiere carte).

:yellow_circle: Le `LibraryToolbarButton` Home est place apres `FocusedItemHud` et apres le Spacer, en bas du header -- position inhabituelle (dans LibraryBrowse, il est en haut a droite du titre).

---

## 3. GenresGridScreen.kt

**Fichier** : `ui/browsing/compose/GenresGridScreen.kt` (572 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | `VegafoXColors.*` + `JellyfinTheme.colorScheme.*` |
| Typographies | :green_circle: OK | `JellyfinTheme.typography.*` |
| Dimensions | :yellow_circle: Moyen | `Tokens.Space.*` utilise au lieu de `TvSpacing.*` ; `180.dp x 100.dp` (skeleton), `120.dp` (card height), `12.dp/8.dp` (padding) hardcodes |
| Boutons | :green_circle: OK | `VegafoXButton` pour les Cancel dialogs, `LibraryToolbarButton` pour la toolbar |
| Icones | :green_circle: OK | `VegafoXIcons.Home`, `.Sort`, `.Filter` |

**Valeurs hardcodees residuelles :**
- Genre card height : `120.dp` -- pas dans `CardDimensions`
- Skeleton size : `180.dp x 100.dp` -- pas dans `CardDimensions`
- Gradient : `0.4f to Color.Transparent, 1.0f to ...copy(alpha=0.80f)` -- pas de token gradient
- Dialog padding : `12.dp`, `16.dp`, `24.dp`, `20.dp` -- pas de token
- `18.sp` (dialog title fontSize) -- pas de token
- `Color.Transparent` utilise directement (acceptable)
- `JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f)` -- opacite hardcodee

:green_circle: Bonne utilisation de `DialogDimensions.standardMinWidth/standardMaxWidth` pour les dialogs.

### Point 2 -- Headers et titres de page

| Element | Valeur |
|---------|--------|
| Titre composable | `TvHeader` |
| Typographie | `titleLarge` (via TvHeader) -- **pas BebasNeue** |
| Couleur | `JellyfinTheme.colorScheme.textPrimary` (via TvHeader) |

:red_circle: **Inconsistance critique** : `GenresGridScreen` utilise `TvHeader` qui affiche en `titleLarge` sans BebasNeue, alors que `LibraryBrowseScreen`, `FolderBrowseScreen`, `ByLetterBrowseScreen` et `SearchScreen` utilisent tous un header custom en `headlineLarge + BebasNeue 40sp`. Le titre Genres sera visuellement different (plus petit, police differente).

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable grille | `TvCardGrid` (composant design system) |
| Colonnes | `5` fixe |
| Carte | `GenreCard` custom (fond + backdrop + gradient + titre) |
| Dimensions carte | `fillMaxWidth` x `120.dp` hauteur fixe |
| Espacement | Via `TvCardGrid` (probablement `TvSpacing.cardGap = 16.dp`) |

:green_circle: Bonne utilisation de `TvCardGrid` et `TvFocusCard`.

:yellow_circle: La carte genre est specifique (pas de media card standard) -- acceptable car c'est un type de contenu different.

### Point 4 -- Etats vides et etats de chargement

| Etat | Composant |
|------|-----------|
| Loading | `LazyVerticalGrid` avec 20 `SkeletonBox(180x100)` en 5 colonnes | :green_circle: Bon skeleton |
| Empty | `EmptyState(R.string.state_empty_genres)` | :green_circle: OK |
| Error | `ErrorState` avec retry | :green_circle: OK |

### Point 5 -- Filtres et tri

| Filtre | Type | Composant |
|--------|------|-----------|
| Sort | Dialog `GenreSortDialog` | `DialogRadioItem` custom + `VegafoXButton` Ghost Cancel |
| Library filter | Dialog `LibraryFilterDialog` | `DialogRadioItem` custom + `VegafoXButton` Ghost Cancel |

:green_circle: Dialogs coherents, utilisent `VegafoXButton`, `VegafoXColors`, `DialogDimensions`.

### Point 8 -- Navigation et focus

:green_circle: Focus initial dans les dialogs via `FocusRequester` sur l'option selectionnee.
:green_circle: `TvScaffold` + `TvFocusCard` gerent le D-pad.
:yellow_circle: Pas de `focusGroup` explicite sur la grille de genres.

---

## 4. ByLetterBrowseScreen.kt

**Fichier** : `ui/browsing/compose/ByLetterBrowseScreen.kt` (246 lignes, inclut ViewModel)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | `VegafoXColors.BackgroundDeep`, `.TextPrimary` |
| Typographies | :green_circle: OK | `headlineLarge + BebasNeue 40sp` |
| Dimensions | :green_circle: OK | `BrowseDimensions.gridPaddingHorizontal` |
| Boutons | N/A | Pas de boutons sur cet ecran |
| Icones | N/A | Pas d'icones directes |

**Valeurs hardcodees residuelles :**
- `top = 32.dp` (header padding) -- meme valeur que LibraryBrowse (coherent, pas tokenise)
- `16.dp` (spacer)
- `28.dp` (spacer entre skeleton rows)
- `27.dp` (contentPadding bottom -- TV safe area)

### Point 2 -- Headers et titres de page

:green_circle: Header custom inline, identique au pattern LibraryBrowse/Search : `headlineLarge + BebasNeue 40sp + VegafoXColors.TextPrimary`.

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable | `TvRowList` (composant design system) |
| Layout | Horizontal rows groupees par lettre |
| Carte | `BrowseMediaCard` | :green_circle: Utilise la carte media standard |

:green_circle: Bonne utilisation de `TvRowList` et `BrowseMediaCard`.

### Point 4 -- Etats vides et etats de chargement

| Etat | Composant |
|------|-----------|
| Loading | `SkeletonCardRow()` x3 | :green_circle: OK |
| Empty | `EmptyState(R.string.lbl_empty)` | :green_circle: OK |
| Error | `ErrorState` avec retry | :green_circle: OK |

### Point 8 -- Navigation et focus

:yellow_circle: Pas de `focusRequester` explicite pour le focus initial.
:green_circle: `TvScaffold` fournit `focusGroup` et `focusRestorer`.
:yellow_circle: Pas de bouton Home ni de navigation retour -- l'utilisateur depend du bouton Back de la telecommande.

---

## 5. CollectionBrowseScreen.kt (Fragment + Screen)

**Fragment** : `CollectionBrowseFragment.kt` (81 lignes)
**Screen** : `CollectionBrowseScreen.kt` (201 lignes, inclut ViewModel)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | Via `JellyfinTheme.colorScheme.*` (proxy VegafoX) |
| Typographies | :yellow_circle: Moyen | Utilise `TvHeader` (`titleLarge`) -- pas BebasNeue |
| Dimensions | :green_circle: OK | Pas de hardcode visible |
| Boutons | N/A | Pas de boutons |
| Icones | N/A | Pas d'icones |

### Point 2 -- Headers et titres de page

:red_circle: **Inconsistance** : Utilise `TvHeader(title = uiState.title)` qui affiche en `titleLarge` sans BebasNeue. Visuellement different des ecrans LibraryBrowse, FolderBrowse, ByLetter et Search.

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable | `TvRowList` (design system) |
| Carte | `BrowseMediaCard` | :green_circle: Carte standard |

:green_circle: Coherent avec ByLetterBrowse.

### Point 4 -- Etats

:green_circle: Tous les etats geres avec `StateContainer`, `SkeletonCardRow`, `EmptyState`, `ErrorState`.

### Point 8 -- Navigation et focus

:yellow_circle: Pas de bouton Home ni toolbar.
:yellow_circle: Pas de `focusRequester` explicite.

---

## 6. SuggestedMoviesScreen.kt (Fragment + Screen)

**Fragment** : `SuggestedMoviesFragment.kt` (80 lignes)
**Screen** : `SuggestedMoviesScreen.kt` (201 lignes, inclut ViewModel)

### Point 1 -- Coherence design system

:green_circle: Identique a CollectionBrowseScreen -- meme pattern exact.

### Point 2 -- Headers et titres de page

:red_circle: **Meme probleme** que CollectionBrowse : `TvHeader(title=uiState.title)` sans BebasNeue.

### Point 3 -- Grilles et listes de cartes

:green_circle: `TvRowList` + `BrowseMediaCard` -- coherent.

### Point 4 -- Etats

:green_circle: 3 `SkeletonCardRow()` en loading -- plus que CollectionBrowse (2) -- acceptable pour les suggestions.

### Point 8 -- Navigation et focus

:yellow_circle: Memes lacunes que CollectionBrowse (pas de Home, pas de focusRequester).

---

## 7. MusicBrowseFragment.kt

**Fichier** : `ui/browsing/v2/MusicBrowseFragment.kt` (705 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :red_circle: Critique | Utilise massivement `JellyfinTheme.colorScheme.onSurface` et `.textSecondary` au lieu de `VegafoXColors.*` -- acceptable car proxy, MAIS utilise `.onSurface.copy(alpha=0.08f)`, `.copy(alpha=0.20f)`, `.copy(alpha=0.12f)`, `.copy(alpha=0.2f)` -- opacites custom partout |
| Typographies | :yellow_circle: Moyen | `JellyfinTheme.typography.*` mais **PAS de BebasNeue** pour le titre (utilise `headlineMedium` + `FontWeight.Light`) |
| Dimensions | :yellow_circle: Moyen | `BrowseDimensions.contentPaddingHorizontal` OK mais `140.dp` (card size), `12.dp`, `4.dp`, `5.dp`, `8.dp`, `20.dp`, `32.dp`, `48.dp` hardcodes |
| Boutons | :yellow_circle: Moyen | `LibraryToolbarButton` pour Home OK ; `MusicNavButton` custom (pas `VegafoXButton`) |
| Icones | :green_circle: OK | `VegafoXIcons.*` (Home, Album, Artist, Genres, Shuffle) |

**Valeurs hardcodees residuelles critiques :**
- `cardSize = 140` (Int, pas dp token ni `CardDimensions`) -- :red_circle: devrait utiliser `CardDimensions.portraitWidth` (150dp) ou un token
- `scale = 1.08f` / `alpha = 0.75f` -- tokens d'animation manquants (meme que FolderBrowse)
- `MusicNavButton` width `140.dp` -- pas de token
- Pas de border orange au focus sur `MusicSquareCard` et `MusicNavButton` -- inconsistant avec le reste du design system

### Point 2 -- Headers et titres de page

| Element | Valeur |
|---------|--------|
| Titre composable | `Text` dans `MusicHeader` |
| Typographie | `headlineMedium` + `FontWeight.Light` |
| Couleur | `JellyfinTheme.colorScheme.onSurface` |
| Alignement | Centre |

:red_circle: **Inconsistance critique** : Style `headlineMedium + Light` est tres different de `headlineLarge + BebasNeue Bold 40sp` utilise partout ailleurs. L'ecran Music ne ressemblera pas aux autres ecrans browse.

### Point 3 -- Grilles et listes de cartes

| Element | Valeur |
|---------|--------|
| Composable | `Column + verticalScroll` + `LazyRow` par section |
| Carte | `MusicSquareCard` custom (carree, 140x140dp) |
| MusicNavButton | Inline custom (140dp, icone + label) |
| Espacement | `12.dp` entre cartes |

:red_circle: **Quatrieme type de carte** custom : `MusicSquareCard` -- pas de border au focus (seulement scale+alpha), pas de `TvFocusCard`, pas de glow orange.

:yellow_circle: `MusicNavButton` n'a **aucune indication de focus visuelle orange** -- utilise juste `.copy(alpha=0.20f)` background gris. Inconsistant avec le langage visuel orange VegafoX.

### Point 4 -- Etats

:green_circle: `StateContainer` avec `SkeletonCardRow`, `EmptyState`, `ErrorState`.

### Point 7 -- AudioNowPlayingScreen (N/A ici)

N/A.

### Point 8 -- Navigation et focus

:yellow_circle: Le `LibraryToolbarButton` Home navigue via `navigationRepository.navigate(Destinations.home)` -- OK.
:red_circle: Pas de `focusRequester` pour le focus initial.
:yellow_circle: `Column + verticalScroll` au lieu de `LazyColumn` -- meme probleme potentiel que FolderBrowse.
:yellow_circle: `MusicNavButton` et `MusicSquareCard` utilisent `clickable(interactionSource)` mais **pas** `.focusable()` explicite -- le focus fonctionne car `clickable` rend focusable, mais pas de `focusGroup` sur les rows.

---

## 8. AudioNowPlayingScreen.kt

**Fichier** : `ui/playback/audio/AudioNowPlayingScreen.kt` (539 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | Tout via `VegafoXColors.*` |
| Typographies | :green_circle: OK | `BebasNeue` pour le titre de chanson, `JetBrainsMono` pour les timestamps |
| Dimensions | :yellow_circle: Moyen | Pas de tokens pour les tailles specifiques du player |
| Boutons | :yellow_circle: Moyen | Mix de `TvIconButton` (controles secondaires) et `IconButton` + `ButtonDefaults` (play/pause) -- pas de `VegafoXButton` ni `VegafoXIconButton` |
| Icones | :green_circle: OK | Tout via `VegafoXIcons.*` |

**Valeurs hardcodees residuelles :**
- `TextStyle(fontSize=16.sp)` (artiste), `14.sp` (genres, album, trackInfo), `36.sp` (titre chanson), `20.sp` (queue title), `13.sp` (timestamps, queue count) -- aucun token typo
- `letterSpacing = 2.sp`, `1.sp` -- pas de token
- `72.dp` (bouton play/pause), `36.dp` (icone play/pause), `128.dp` (placeholder album icon), `140.dp` (queue card width) -- pas de tokens
- `RoundedCornerShape(16.dp)` pour artwork -- pas de token
- `Spacer(width=12.dp)`, `16.dp`, `8.dp` entre controles -- pas de token
- `4.dp` (seekbar height) -- pas de token
- `Color.Transparent` -- acceptable

### Point 2 -- Headers et titres de page

N/A -- pas de header classique, c'est un ecran player.

### Point 7 -- AudioNowPlayingScreen specifique

**Pochette :**
- :green_circle: `AsyncImage` avec fallback `VegafoXIcons.Album` et tint `VegafoXColors.TextHint`
- :green_circle: Shadow `24.dp`, clip `RoundedCornerShape(16.dp)`, border `VegafoXColors.Divider`
- :green_circle: Lyrics overlay avec `fadingEdges(50.dp)` et couleur `VegafoXColors.TextPrimary`

**Controles de lecture :**

| Controle | Composant | Style |
|----------|-----------|-------|
| Shuffle | `TvIconButton` | tint conditionnel `OrangePrimary`/`TextHint` | :green_circle: |
| Previous | `TvIconButton` | tint `TextSecondary` | :green_circle: |
| Play/Pause | `IconButton` (design system) | Container `OrangePrimary`, 72dp circle, glow | :green_circle: |
| Next | `TvIconButton` | tint `TextSecondary` | :green_circle: |
| Loop | `TvIconButton` | tint conditionnel | :green_circle: |
| Album | `TvIconButton` | tint `TextSecondary` | :green_circle: |
| Artist | `TvIconButton` | tint `TextSecondary` | :green_circle: |

:yellow_circle: Le bouton Play/Pause utilise `IconButton` du design system mais avec `ButtonDefaults.colors()` inline au lieu d'un preset. C'est correct mais pas `VegafoXButton`.

**Seekbar :**
- :green_circle: `PlayerSeekbar` avec `SeekbarColors` utilisant `VegafoXColors` (Outline, TextSecondary, OrangePrimary)
- :green_circle: Timestamps en `JetBrainsMono 13sp`

**Queue :**
- :green_circle: `LazyRow` avec `TvFocusCard` pour chaque element
- :green_circle: Item actif en `VegafoXColors.OrangePrimary` (texte + overlay 12% alpha)
- :green_circle: `focusRestorer()` sur la LazyRow
- `TvSpacing.cardGap` pour l'espacement -- :green_circle: token design system

**Metadonnees :**
- :yellow_circle: `TextStyle()` inline partout au lieu de `JellyfinTheme.typography.*` -- 7 occurrences de `TextStyle(fontSize=..., color=...)` au lieu d'utiliser les styles typo du theme

### Point 8 -- Navigation et focus

:green_circle: `TvScaffold` fournit focus group.
:green_circle: `focusRestorer()` sur la queue LazyRow.
:yellow_circle: Pas de `focusRequester` pour le focus initial (devrait aller sur Play/Pause).

---

## 9. SearchScreen.kt

**Fichier** : `ui/search/compose/SearchScreen.kt` (225 lignes)

### Point 1 -- Coherence design system

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | `VegafoXColors.*` |
| Typographies | :green_circle: OK | `headlineLarge + BebasNeue 40sp` pour le titre |
| Dimensions | :green_circle: OK | `BrowseDimensions.gridPaddingHorizontal` |
| Boutons | :green_circle: OK | Pas de bouton custom -- `SearchVoiceInput` utilise `IconButton` + `IconButtonDefaults` |
| Icones | :green_circle: OK | `VegafoXIcons.Search`, `.Microphone` |

**Valeurs hardcodees residuelles :**
- `32.dp` (top padding) -- coherent avec LibraryBrowse
- `12.dp`, `16.dp`, `27.dp` (spacers/padding) -- memes valeurs que les autres ecrans
- `40.sp`, `2.sp` -- memes valeurs titre que les autres ecrans

### Point 2 -- Headers et titres de page

:green_circle: Header inline identique au pattern LibraryBrowse : `headlineLarge + BebasNeue 40sp + Bold + letterSpacing 2sp + VegafoXColors.TextPrimary`.

### Point 6 -- SearchScreen specifique

**Champ de recherche :**

| Element | Valeur | Status |
|---------|--------|--------|
| Composant | `SearchTextInput` (composable dedié) | :green_circle: |
| Background | `VegafoXColors.Surface` | :green_circle: |
| Border | `VegafoXColors.OrangePrimary` au focus, `White.copy(0.10f)` sinon | :yellow_circle: `White.copy(0.10f)` est hardcode, devrait etre `VegafoXColors.Divider` ou un token |
| Texte | `VegafoXColors.TextPrimary` 16sp | :green_circle: |
| Icone | `VegafoXIcons.Search`, tint `OrangePrimary` au focus / `TextSecondary` sinon | :green_circle: |
| Curseur | `SolidColor(VegafoXColors.OrangePrimary)` | :green_circle: |
| Placeholder | **Aucun placeholder texte** | :red_circle: Pas de hint text quand le champ est vide |
| Shape | `RoundedCornerShape(12.dp)` | :yellow_circle: Pas de token (devrait etre `ButtonDimensions.cornerRadius` = 10dp ou un token dédié) |

**Voice input :**
- :green_circle: `SearchVoiceInput` utilise `VegafoXIcons.Microphone`, `IconButton` du design system
- :green_circle: Animation pulse pendant l'ecoute avec `recording` color

**Resultats :**

| Element | Valeur |
|---------|--------|
| Grille | `TvCardGrid` avec 5 colonnes | :green_circle: Coherent avec GenresGrid |
| Carte | `BrowseMediaCard` | :green_circle: Carte standard |
| contentPadding | `PaddingValues(bottom=27.dp)` | :green_circle: TV safe area |

:yellow_circle: Les resultats sont un **flat grid** -- pas de headers de categories (Movies, Series, etc.). Le ViewModel flatten tous les groupes : `val allItems = results.flatMap { it.items }`. Les separateurs/headers de categories sont perdus.

**Gestion clavier/D-pad :**
- :green_circle: `isEditing` state gere le basculement lecture seule / edition avec clavier
- :green_circle: Sur submit : `resultsFocusRequester.requestFocus()` deplace le focus vers la grille (dismiss keyboard)
- :green_circle: `onKeyEvent` pour detecter Enter/DirectionCenter et basculer en mode edition
- :green_circle: `onFocusChanged` pour masquer le clavier quand le champ perd le focus
- :green_circle: `focusGroup()` + `focusRestorer()` sur le container de resultats

### Point 4 -- Etats vides et etats de chargement

| Etat | Composant |
|------|-----------|
| Query vide | `EmptyState(R.string.search_empty_hint, icon=VegafoXIcons.Search)` | :green_circle: |
| Pas de resultats | `EmptyState(R.string.search_no_results)` | :green_circle: |
| Loading | **Aucun etat loading** | :red_circle: Pas de skeleton/spinner pendant la recherche |

:red_circle: **Pas d'etat loading** : quand une recherche est en cours, il n'y a pas d'indicateur visuel. Si la requete prend du temps, l'utilisateur ne voit rien changer jusqu'a ce que les resultats arrivent ou l'etat empty s'affiche.

### Point 8 -- Navigation et focus

:green_circle: Focus initial : `textInputFocusRequester.requestFocus()` si pas d'`initialQuery`, sinon attend les resultats et `resultsFocusRequester.requestFocus()`.
:green_circle: `focusGroup()` sur le container de resultats.
:green_circle: `focusRestorer()` sur le container input.

---

## BrowseMediaCard.kt (composant partage)

**Fichier** : `ui/browsing/compose/BrowseMediaCard.kt` (354 lignes)

### Analyse composant

| Element | Status | Detail |
|---------|--------|--------|
| Couleurs | :green_circle: OK | `VegafoXColors.*` et `JellyfinTheme.colorScheme.*` (surfaceDim, textDisabled) |
| Shape | :yellow_circle: | `RoundedCornerShape(10.dp)` hardcode -- pas de token |
| Badge NEW | :green_circle: | `VegafoXColors.OrangePrimary` bg, `RoundedCornerShape(4.dp)`, `stringResource(R.string.lbl_new)` |
| Progress bar | :green_circle: | `VegafoXColors.Divider` bg, `VegafoXColors.BlueAccent` fill |
| Play overlay | :green_circle: | `VegafoXColors.Background.copy(alpha=0.65f)`, unicode play triangle |
| Typography | :yellow_circle: | Badge : `JellyfinTheme.typography.labelSmall.copy(fontSize=10.sp)` ; Overlay : `fontSize=12.sp`, `13.sp` inline |
| Icones | :green_circle: | `VegafoXIcons.Movie` pour le placeholder |
| Focus | :green_circle: | `TvFocusCard` + long press detection |
| Dimensions | :green_circle: | `CardDimensions.landscapeWidth` (220dp) par defaut |

**Valeurs hardcodees :**
- `RoundedCornerShape(10.dp)` -- devrait utiliser un token shape
- `RoundedCornerShape(20.dp)` pour l'overlay pill -- pas de token
- `RoundedCornerShape(4.dp)` pour le badge -- pas de token
- `10.sp` (badge font), `12.sp` (play icon), `13.sp` (play text) -- pas de tokens typo
- `CARD_IMAGE_WIDTH_PX = 440`, `CARD_IMAGE_HEIGHT_PX = 248` -- dimensions pixel hardcodees (acceptable pour le reseau)
- `quality = 96` -- hardcode (acceptable)
- `Modifier.size(48.dp)` pour le placeholder icon -- pas de token

---

## Resume global des problemes

### :red_circle: Critiques (a corriger en priorite)

| # | Probleme | Fichiers concernes |
|---|----------|--------------------|
| 1 | **Inconsistance header titre** : 3 styles differents coexistent -- (a) `headlineLarge + BebasNeue 40sp Bold` (LibraryBrowse, FolderBrowse, ByLetter, Search), (b) `titleLarge` via TvHeader (CollectionBrowse, SuggestedMovies, GenresGrid), (c) `headlineMedium + Light` (MusicBrowse) | Tous sauf AudioNowPlaying |
| 2 | **4 types de cartes media** non unifies : `BrowseMediaCard`, `LibraryPosterCard`, `FolderItemCard`, `MusicSquareCard` -- chacune avec un style de focus, des dimensions et des fonctionnalites differents | LibraryBrowse, FolderBrowse, MusicBrowse |
| 3 | **MusicBrowseFragment** utilise `JellyfinTheme.colorScheme.onSurface` partout avec des opacites hardcodees au lieu de couleurs semantiques, pas de border orange au focus, titre en `headlineMedium Light` | MusicBrowseFragment |
| 4 | **Pas d'etat loading** dans SearchScreen -- pas de skeleton/spinner pendant la recherche | SearchScreen |
| 5 | **Pas de placeholder** texte dans le champ de recherche quand il est vide | SearchTextInput |

### :yellow_circle: Moyens (a planifier)

| # | Probleme | Fichiers concernes |
|---|----------|--------------------|
| 6 | **dp/sp literals** non tokenises : `32.dp`, `40.sp`, `13.sp`, `14.sp`, `12.dp`, `16.dp`, `120.dp`, `140.dp` etc. recurrents -- pas de centralisation des spacings/tailles browse | Tous |
| 7 | **FilterChip / LetterChip / MusicNavButton** sont des composants custom qui ne reposent pas sur `VegafoXButton` | LibraryBrowse, MusicBrowse |
| 8 | **FolderItemCard dimensions** `140x210` ne correspondent pas a `CardDimensions.portraitWidth/Height` (`150x225`) | FolderBrowse |
| 9 | **Espacement cartes** `12.dp` utilise dans plusieurs ecrans alors que `TvSpacing.cardGap = 16.dp` | LibraryBrowse, FolderBrowse, MusicBrowse |
| 10 | **`Column + verticalScroll`** au lieu de `LazyColumn` pour les listes de rows | FolderBrowse, MusicBrowse |
| 11 | **Padding top header** inconsistant : `32.dp` (LibraryBrowse, ByLetter, Search) vs `12.dp` (FolderBrowse, MusicBrowse) | FolderBrowse, MusicBrowse |
| 12 | **Resultats recherche** en flat grid sans headers de categories -- les groupes (movies, series, etc.) sont aplatis | SearchScreen |
| 13 | **AudioNowPlayingScreen** utilise `TextStyle()` inline 7 fois au lieu de `JellyfinTheme.typography.*` | AudioNowPlaying |
| 14 | **Pas de focus initial** explicite sur certains ecrans (FolderBrowse, CollectionBrowse, SuggestedMovies, MusicBrowse, AudioNowPlaying) | 5 fichiers |
| 15 | **SearchTextInput** border unfocused `Color.White.copy(0.10f)` hardcode au lieu de `VegafoXColors.Divider` ou token | SearchTextInput |
| 16 | **Pas de bouton Home** dans CollectionBrowse et SuggestedMovies | CollectionBrowse, SuggestedMovies |

### :green_circle: Points positifs

| # | Point |
|---|-------|
| 1 | `StateContainer` + `DisplayState` enum utilises partout -- pattern solide |
| 2 | Skeleton loaders presents sur tous les ecrans (sauf Search loading) |
| 3 | Toutes les strings user-facing viennent de `strings.xml` (via `stringResource`) |
| 4 | `VegafoXIcons.*` utilise systematiquement -- aucun `Icons.Default.*` direct dans les fichiers audites |
| 5 | `VegafoXButton` utilise dans les dialogs (FilterSort, GenreSort, LibraryFilter) |
| 6 | `TvScaffold` + `TvFocusCard` + `focusRestorer()` -- bonne base D-pad |
| 7 | `BrowseMediaCard` bien concu (long press, play overlay, progress, NEW badge, image fallback) |
| 8 | SearchScreen gestion clavier/D-pad robuste (basculement edit/readonly, dismiss keyboard sur submit) |
| 9 | AudioNowPlayingScreen visuellement coherent avec la palette VegafoX (orange/noir) |

---

## Priorites de correction

### Priorite 1 -- Unifier les headers (impact visuel maximal)
Creer un composant `BrowseHeader` qui encapsule le style `headlineLarge + BebasNeue 40sp Bold`. Remplacer les usages de `TvHeader` dans CollectionBrowse, SuggestedMovies, GenresGrid, et le header custom de MusicBrowse.

### Priorite 2 -- Reduire les types de cartes
Au minimum unifier `FolderItemCard` et `MusicSquareCard` vers un composant partage (ou vers `LibraryPosterCard` en mode portrait/carre). `BrowseMediaCard` (landscape) et `LibraryPosterCard` (portrait/flexible) peuvent coexister mais devraient partager le style de focus (border orange + glow).

### Priorite 3 -- Ajouter l'etat loading et le placeholder dans Search
Ajouter un `DisplayState.LOADING` avec skeleton dans `SearchScreen`. Ajouter un `placeholder` texte dans `SearchTextInput`.

### Priorite 4 -- Homogeneiser les spacings
Utiliser `TvSpacing.cardGap` (16dp) partout. Creer un token `BrowseDimensions.headerPaddingTop` pour le `32.dp` recurrent.

### Priorite 5 -- MusicBrowse alignment
Aligner le header (BebasNeue), ajouter la border orange au focus, et harmoniser les opacites/couleurs avec le reste.
