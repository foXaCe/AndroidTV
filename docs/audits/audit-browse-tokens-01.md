# Audit : valeurs dp/sp hardcodees dans les ecrans Browse & Audio

**Date** : 2026-03-15
**Scope** : 9 fichiers (browsing/compose, browsing/v2, playback/audio, search/compose)
**Objectif** : Inventorier toutes les valeurs dp/sp hardcodees ne passant pas par un token existant

---

## Tokens existants de reference

| Source | Token | Valeur |
|--------|-------|--------|
| `Tokens.Space.space2xs` | 2.dp | 2.dp |
| `Tokens.Space.spaceXs` | 4.dp | 4.dp |
| `Tokens.Space.spaceSm` | 8.dp | 8.dp |
| `Tokens.Space.spaceMd` | 16.dp | 16.dp |
| `Tokens.Space.spaceLg` | 24.dp | 24.dp |
| `Tokens.Space.spaceXl` | 32.dp | 32.dp |
| `Tokens.Space.space2xl` | 40.dp | 40.dp |
| `Tokens.Space.space3xl` | 48.dp | 48.dp |
| `TvSpacing.screenHorizontal` | 48.dp | = space3xl |
| `TvSpacing.screenVertical` | 24.dp | = spaceLg |
| `TvSpacing.cardGap` | 16.dp | = spaceMd |
| `TvSpacing.sectionGap` | 24.dp | = spaceLg |
| `TvSpacing.buttonHeight` | 48.dp | = space3xl |
| `TvSpacing.iconSize` | 24.dp | = spaceLg |
| `TvSpacing.iconSizeLarge` | 32.dp | = spaceXl |
| `BrowseDimensions.contentPaddingHorizontal` | 60.dp | |
| `BrowseDimensions.gridPaddingHorizontal` | 56.dp | |
| `CardDimensions.landscapeWidth` | 220.dp | |
| `CardDimensions.landscapeHeight` | 124.dp | |
| `CardDimensions.portraitWidth` | 150.dp | |
| `CardDimensions.portraitHeight` | 225.dp | |
| `CardDimensions.folderWidth` | 140.dp | |
| `CardDimensions.folderHeight` | 210.dp | |
| `CardDimensions.squareSize` | 140.dp | |
| `DialogDimensions.standardMinWidth` | 340.dp | |
| `DialogDimensions.standardMaxWidth` | 440.dp | |
| `AnimationDefaults.FOCUS_SCALE` | 1.06f | |

---

## 1. LibraryBrowseScreen.kt

**Chemin** : `ui/browsing/compose/LibraryBrowseScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 148 | `8.dp` | Spacer entre FocusedItemHud et FilterChipsRow |
| 160 | `12.dp` | Spacer entre FilterChipsRow et StateContainer |
| 228 | `8.dp` | `Arrangement.spacedBy(8.dp)` — espacement horizontal entre filter chips |
| 357 | `1.dp` | `.border(1.dp, ...)` — bordure du FilterChip |
| 358 | `50.dp` | `RoundedCornerShape(50.dp)` — coins arrondis du FilterChip (pill shape) |
| 365 | `16.dp` | `padding(horizontal = 16.dp)` — padding horizontal interne du FilterChip |
| 365 | `8.dp` | `padding(vertical = 8.dp)` — padding vertical interne du FilterChip |
| 370 | `6.dp` | `Arrangement.spacedBy(6.dp)` — espacement icon/text dans FilterChip |
| 376 | `16.dp` | `Modifier.size(16.dp)` — taille de l'icone dans FilterChip |
| 382 | `13.sp` | `fontSize = 13.sp` — taille du texte dans FilterChip |
| 435 | `32.dp` | `Modifier.size(32.dp)` — taille du LetterChip |
| 436 | `1.dp` | `.border(1.dp, ...)` — bordure du LetterChip |
| 437 | `50.dp` | `RoundedCornerShape(50.dp)` — coins arrondis du LetterChip |
| 448 | `13.sp` | `fontSize = 13.sp` — taille du texte dans LetterChip |
| 470 | `16` | `(cardWidth + 16).dp` — marge ajoutee pour calcul de colonnes (valeur Int convertie en dp) |
| 500 | `27.dp` | `PaddingValues(bottom = 27.dp)` — padding bottom de la grille |
| 501 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement horizontal grille |
| 502 | `16.dp` | `Arrangement.spacedBy(16.dp)` — espacement vertical grille |

**Valeurs Int dans `imageTypeToCardDimensions` (lignes 553-590)** :
Ces valeurs sont des Int convertis en .dp dans les appels `SkeletonCard(width = cardWidth.dp, ...)`. Elles definissent les dimensions de cartes selon PosterSize/ImageType. Ce sont des valeurs de configuration, pas des spacings, mais elles sont hardcodees :

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 559-564 | 100/150, 120/180, 140/210, 180/270, 220/330 | POSTER dimensions (w/h) par taille |
| 568-572 | 160/90, 190/107, 220/124, 280/158, 340/191 | THUMB dimensions (w/h) par taille |
| 576-580 | 300/52, 360/62, 420/72, 500/86, 600/103 | BANNER dimensions (w/h) par taille |
| 584-588 | 100/100, 120/120, 140/140, 180/180, 220/220 | SQUARE dimensions (w/h) par taille |

---

## 2. FolderBrowseScreen.kt

**Chemin** : `ui/browsing/compose/FolderBrowseScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 83 | `28.dp` | `Arrangement.spacedBy(28.dp)` — espacement vertical entre SkeletonCardRow (loading) |
| 152 | `16.dp` | `padding(bottom = 16.dp)` — padding bottom de la colonne de rows |
| 179 | `12.dp` | `padding(top = 12.dp)` — padding top de chaque FolderItemRow |
| 185 | `8.dp` | `padding(bottom = 8.dp)` — padding bottom du titre de row |
| 189 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement horizontal entre cartes dans LazyRow |

---

## 3. GenresGridScreen.kt

**Chemin** : `ui/browsing/compose/GenresGridScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 157 | `180.dp` | `.width(180.dp)` — largeur du skeleton card genre |
| 158 | `100.dp` | `.height(100.dp)` — hauteur du skeleton card genre |
| 235 | `120.dp` | `.height(120.dp)` — hauteur de la GenreCard |
| 277 | `12.dp` | `padding(horizontal = 12.dp)` — padding horizontal du nom de genre |
| 277 | `8.dp` | `padding(vertical = 8.dp)` — padding vertical du nom de genre |
| 310 | `1.dp` | `.border(1.dp, ...)` — bordure du GenreSortDialog |
| 311 | `20.dp` | `padding(vertical = 20.dp)` — padding vertical du dialog |
| 317 | `18.sp` | `fontSize = 18.sp` — taille du titre du GenreSortDialog |
| 324 | `12.dp` | `padding(bottom = 12.dp)` — padding bottom du titre du dialog |
| 331 | `1.dp` | `.height(1.dp)` — hauteur du separateur (divider) |
| 350 | `12.dp` | `Modifier.height(12.dp)` — spacer avant le bloc d'actions |
| 355 | `1.dp` | `.height(1.dp)` — hauteur du separateur bas |
| 358 | `16.dp` | `Modifier.height(16.dp)` — spacer avant boutons d'action |
| 363 | `24.dp` | `padding(horizontal = 24.dp)` — padding horizontal des boutons d'action |
| 364 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement entre boutons d'action |
| 410 | `1.dp` | `.border(1.dp, ...)` — bordure du LibraryFilterDialog |
| 411 | `20.dp` | `padding(vertical = 20.dp)` — padding vertical du dialog |
| 417 | `18.sp` | `fontSize = 18.sp` — taille du titre du LibraryFilterDialog |
| 424 | `12.dp` | `padding(bottom = 12.dp)` — padding bottom du titre du dialog |
| 431 | `1.dp` | `.height(1.dp)` — hauteur du separateur |
| 471 | `12.dp` | `Modifier.height(12.dp)` — spacer avant le bloc d'actions |
| 476 | `1.dp` | `.height(1.dp)` — hauteur du separateur bas |
| 479 | `16.dp` | `Modifier.height(16.dp)` — spacer avant boutons d'action |
| 484 | `24.dp` | `padding(horizontal = 24.dp)` — padding horizontal des boutons d'action |
| 485 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement entre boutons d'action |
| 530 | `12.dp` | `padding(vertical = 12.dp)` — padding vertical du DialogRadioItem |
| 536 | `18.dp` | `Modifier.size(18.dp)` — taille du radio button outer |
| 537 | `2.dp` | `.border(width = 2.dp, ...)` — bordure du radio button |
| 548 | `10.dp` | `Modifier.size(10.dp)` — taille du radio button inner dot |

---

## 4. ByLetterBrowseScreen.kt

**Chemin** : `ui/browsing/compose/ByLetterBrowseScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 180 | `16.dp` | `Modifier.height(16.dp)` — spacer entre header et contenu |
| 197 | `28.dp` | `Modifier.height(28.dp)` — spacer entre SkeletonCardRow (loading) |
| 213 | `27.dp` | `PaddingValues(bottom = 27.dp)` — padding bottom du TvRowList |

---

## 5. CollectionBrowseScreen.kt

**Chemin** : `ui/browsing/compose/CollectionBrowseScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 165 | `16.dp` | `Modifier.height(16.dp)` — spacer entre header et contenu |
| 181 | `28.dp` | `Modifier.height(28.dp)` — spacer entre SkeletonCardRow (loading) |
| 199 | `27.dp` | `PaddingValues(bottom = 27.dp)` — padding bottom du TvRowList |

---

## 6. SuggestedMoviesScreen.kt

**Chemin** : `ui/browsing/compose/SuggestedMoviesScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 163 | `16.dp` | `Modifier.height(16.dp)` — spacer entre header et contenu |
| 179 | `28.dp` | `Modifier.height(28.dp)` — spacer entre SkeletonCardRow (loading) |
| 181 | `28.dp` | `Modifier.height(28.dp)` — spacer entre SkeletonCardRow (loading) |
| 199 | `27.dp` | `PaddingValues(bottom = 27.dp)` — padding bottom du TvRowList |

---

## 7. MusicBrowseFragment.kt

**Chemin** : `ui/browsing/v2/MusicBrowseFragment.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 216 | `28.dp` | `Arrangement.spacedBy(28.dp)` — espacement vertical entre SkeletonCardRow (loading) |
| 270 | `16.dp` | `padding(bottom = 16.dp)` — padding bottom de la colonne scrollable |
| 324 | `12.dp` | `padding(top = 12.dp)` — padding top de chaque MusicItemRow |
| 331 | `8.dp` | `padding(bottom = 8.dp)` — padding bottom du titre de row |
| 336 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement horizontal entre cartes |
| 370 | `4.dp` | `padding(top = 4.dp)` — padding top de MusicViewsRow |
| 376 | `8.dp` | `padding(bottom = 8.dp)` — padding bottom du titre "Views" |
| 380 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement horizontal entre nav buttons |
| 482 | `140.dp` | `.width(140.dp)` — largeur du MusicNavButton |
| 489 | `20.dp` | `padding(vertical = 20.dp)` — padding vertical du MusicNavButton |
| 495 | `32.dp` | `Modifier.size(32.dp)` — taille de l'icone dans MusicNavButton |
| 499 | `8.dp` | `Modifier.height(8.dp)` — spacer entre icone et texte dans MusicNavButton |

---

## 8. AudioNowPlayingScreen.kt

**Chemin** : `ui/playback/audio/AudioNowPlayingScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 83 | `16.dp` | `RoundedCornerShape(16.dp)` — coins de l'artwork (ArtworkShape) |
| 130 | `16.sp` | `fontSize = 16.sp` — taille texte nom artiste |
| 140 | `14.sp` | `fontSize = 14.sp` — taille texte genres |
| 157 | `36.sp` | `fontSize = 36.sp` — taille texte titre chanson (BebasNeue) |
| 160 | `2.sp` | `letterSpacing = 2.sp` — espacement lettres titre chanson |
| 169 | `14.sp` | `fontSize = 14.sp` — taille texte titre album |
| 179 | `14.sp` | `fontSize = 14.sp` — taille texte track info |
| 226 | `24.dp` | `.shadow(24.dp, ...)` — elevation shadow artwork |
| 229 | `1.dp` | `.border(1.dp, ...)` — bordure artwork |
| 244 | `128.dp` | `Modifier.size(128.dp)` — taille icone album placeholder |
| 264 | `50.dp` | `fadingEdges(vertical = 50.dp)` — fading edges pour les lyrics |
| 265 | `15.dp` | `padding(horizontal = 15.dp)` — padding horizontal lyrics |
| 289 | `4.dp` | `.height(4.dp)` — hauteur de la seekbar |
| 291 | `4.dp` | `Modifier.height(4.dp)` — spacer sous seekbar |
| 301 | `13.sp` | `fontSize = 13.sp` — taille texte temps ecoule |
| 310 | `13.sp` | `fontSize = 13.sp` — taille texte temps restant |
| 337 | `12.dp` | `Modifier.width(12.dp)` — spacer entre shuffle et previous |
| 345 | `12.dp` | `Modifier.width(12.dp)` — spacer entre previous et play |
| 360 | `0.dp` | `PaddingValues(0.dp)` — contentPadding du bouton play |
| 364 | `72.dp` | `.size(72.dp)` — taille du bouton play/pause |
| 386 | `36.dp` | `Modifier.size(36.dp)` — taille icone play/pause |
| 389 | `12.dp` | `Modifier.width(12.dp)` — spacer entre play et next |
| 397 | `12.dp` | `Modifier.width(12.dp)` — spacer entre next et loop |
| 405 | `16.dp` | `Modifier.width(16.dp)` — spacer entre loop et album |
| 414 | `8.dp` | `Modifier.width(8.dp)` — spacer entre album et artist |
| 443 | `20.sp` | `fontSize = 20.sp` — taille texte "Current Queue" (BebasNeue) |
| 445 | `1.sp` | `letterSpacing = 1.sp` — espacement lettres "Current Queue" |
| 453 | `13.sp` | `fontSize = 13.sp` — taille texte nombre d'items queue |
| 458 | `8.dp` | `Modifier.height(8.dp)` — spacer entre titre queue et liste |
| 497 | `140.dp` | `Modifier.width(140.dp)` — largeur de l'AudioQueueCard |
| 502 | `140.dp` | `.size(140.dp)` — taille image de l'AudioQueueCard |
| 520 | `48.dp` | `Modifier.size(48.dp)` — taille icone album placeholder dans queue |
| 532 | `4.dp` | `Modifier.height(4.dp)` — spacer entre image et texte dans queue card |

---

## 9. SearchScreen.kt

**Chemin** : `ui/search/compose/SearchScreen.kt`

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 122 | `12.dp` | `Modifier.height(12.dp)` — spacer entre header et search input |
| 126 | `12.dp` | `Arrangement.spacedBy(12.dp)` — espacement entre voice input et text input |
| 162 | `16.dp` | `Modifier.height(16.dp)` — spacer entre search input et resultats |
| 193 | `27.dp` | `PaddingValues(bottom = 27.dp)` — padding bottom du TvCardGrid |

---

## Tableau de regroupement

| Valeur | Occurrences | Fichiers | Token propose |
|--------|-------------|----------|---------------|
| `27.dp` | 5 | LibraryBrowseScreen, ByLetterBrowseScreen, CollectionBrowseScreen, SuggestedMoviesScreen, SearchScreen | `BrowseDimensions.gridBottomPadding` |
| `28.dp` | 6 | FolderBrowseScreen, ByLetterBrowseScreen, CollectionBrowseScreen, SuggestedMoviesScreen (x2), MusicBrowseFragment | `BrowseDimensions.skeletonRowSpacing` |
| `12.dp` (spacing between cards/items/buttons) | 14 | LibraryBrowseScreen, FolderBrowseScreen, GenresGridScreen (x4), MusicBrowseFragment (x3), AudioNowPlayingScreen (x4), SearchScreen (x2) | `BrowseDimensions.itemSpacing` |
| `16.dp` (spacer header-to-content) | 5 | ByLetterBrowseScreen, CollectionBrowseScreen, SuggestedMoviesScreen, MusicBrowseFragment, SearchScreen | `BrowseDimensions.headerBottomSpacing` ou utiliser `Tokens.Space.spaceMd` |
| `8.dp` (padding bottom titre / petits spacers) | 8 | LibraryBrowseScreen (x2), FolderBrowseScreen, GenresGridScreen, MusicBrowseFragment (x3), AudioNowPlayingScreen (x2) | Utiliser `Tokens.Space.spaceSm` |
| `1.dp` (border width) | 7 | LibraryBrowseScreen (x2), GenresGridScreen (x5), AudioNowPlayingScreen | `BrowseDimensions.borderWidth` ou `Tokens.Space.space2xs` (nota: 1dp != 2dp) |
| `13.sp` | 6 | LibraryBrowseScreen (x2), AudioNowPlayingScreen (x4) | `BrowseTypography.captionSmall` |
| `14.sp` | 3 | AudioNowPlayingScreen (x3) | `AudioTypography.metaFontSize` |
| `18.sp` | 2 | GenresGridScreen (x2) — titres de dialogs | `DialogTypography.titleFontSize` |
| `50.dp` (RoundedCornerShape pill) | 3 | LibraryBrowseScreen (FilterChip x2, LetterChip x1) | `BrowseDimensions.chipCornerRadius` |
| `20.dp` (dialog padding vertical) | 3 | GenresGridScreen (x2), MusicBrowseFragment (x1) | `DialogDimensions.verticalPadding` |
| `24.dp` (dialog buttons horizontal padding) | 2 | GenresGridScreen (x2) | `DialogDimensions.actionPaddingHorizontal` ou utiliser `Tokens.Space.spaceLg` |
| `140.dp` (card/button width) | 3 | MusicBrowseFragment, AudioNowPlayingScreen (x2) | Deja disponible via `CardDimensions.squareSize` / `CardDimensions.folderWidth` |
| `16.dp` (FilterChip padding H / icon size) | 2 | LibraryBrowseScreen (x2) | Utiliser `Tokens.Space.spaceMd` |
| `32.dp` (icon/chip size) | 2 | LibraryBrowseScreen, MusicBrowseFragment | Utiliser `Tokens.Space.spaceXl` ou `TvSpacing.iconSizeLarge` |
| `36.sp` | 1 | AudioNowPlayingScreen — titre chanson | `AudioTypography.songTitleFontSize` |
| `36.dp` | 1 | AudioNowPlayingScreen — icone play/pause | `AudioDimensions.playIconSize` |
| `72.dp` | 1 | AudioNowPlayingScreen — bouton play/pause | `AudioDimensions.playButtonSize` |
| `128.dp` | 1 | AudioNowPlayingScreen — icone album placeholder | `AudioDimensions.albumPlaceholderSize` |
| `48.dp` (icon placeholder queue) | 1 | AudioNowPlayingScreen | Utiliser `TvSpacing.buttonHeight` ou `Tokens.Space.space3xl` |
| `4.dp` (seekbar/spacers) | 4 | AudioNowPlayingScreen (x3), MusicBrowseFragment | Utiliser `Tokens.Space.spaceXs` |
| `120.dp` | 1 | GenresGridScreen — hauteur GenreCard | `BrowseDimensions.genreCardHeight` |
| `180.dp` | 1 | GenresGridScreen — largeur skeleton genre | `BrowseDimensions.genreCardWidth` |
| `100.dp` | 1 | GenresGridScreen — hauteur skeleton genre | `BrowseDimensions.genreSkeletonHeight` |
| `18.dp` | 1 | GenresGridScreen — radio button outer size | `DialogDimensions.radioSize` |
| `10.dp` | 1 | GenresGridScreen — radio button inner dot size | `DialogDimensions.radioDotSize` |
| `2.dp` (border radio) | 1 | GenresGridScreen | `DialogDimensions.radioBorderWidth` ou `Tokens.Space.space2xs` |
| `6.dp` | 1 | LibraryBrowseScreen — espacement icon/label chip | `BrowseDimensions.chipIconTextGap` |
| `15.dp` | 1 | AudioNowPlayingScreen — padding horizontal lyrics | `AudioDimensions.lyricsPaddingH` |
| `50.dp` (fading edges) | 1 | AudioNowPlayingScreen | `AudioDimensions.fadingEdgeSize` |
| `20.sp` | 1 | AudioNowPlayingScreen — titre "Current Queue" | `AudioTypography.sectionTitleFontSize` |
| `16.sp` | 1 | AudioNowPlayingScreen — nom artiste | `AudioTypography.artistFontSize` |
| `1.sp` / `2.sp` | 2 | AudioNowPlayingScreen — letterSpacing | `AudioTypography.sectionLetterSpacing` / `AudioTypography.titleLetterSpacing` |

---

## Synthese

### Nombre total de valeurs hardcodees par fichier

| Fichier | dp hardcodes | sp hardcodes | Total |
|---------|-------------|-------------|-------|
| LibraryBrowseScreen.kt | 16 (+40 card dims) | 2 | 18 (+40) |
| FolderBrowseScreen.kt | 5 | 0 | 5 |
| GenresGridScreen.kt | 18 | 2 | 20 |
| ByLetterBrowseScreen.kt | 3 | 0 | 3 |
| CollectionBrowseScreen.kt | 3 | 0 | 3 |
| SuggestedMoviesScreen.kt | 4 | 0 | 4 |
| MusicBrowseFragment.kt | 12 | 0 | 12 |
| AudioNowPlayingScreen.kt | 18 | 10 | 28 |
| SearchScreen.kt | 4 | 0 | 4 |

**Total general : 83 occurrences de valeurs hardcodees** (+ 40 card dimensions dans le mapping PosterSize/ImageType)

### Tokens a creer en priorite (par nombre d'occurrences)

1. **`BrowseDimensions.itemSpacing` = 12.dp** -- 14 occurrences dans 6 fichiers
2. **`BrowseDimensions.skeletonRowSpacing` = 28.dp** -- 6 occurrences dans 5 fichiers
3. **`BrowseDimensions.headerBottomSpacing` = 16.dp** -- 5 occurrences dans 5 fichiers (ou remplacer par `Tokens.Space.spaceMd`)
4. **`BrowseDimensions.gridBottomPadding` = 27.dp** -- 5 occurrences dans 5 fichiers
5. **Remplacer `8.dp` par `Tokens.Space.spaceSm`** -- 8 occurrences dans 5 fichiers
6. **`BrowseDimensions.borderWidth` = 1.dp** -- 7 occurrences dans 3 fichiers
7. **`BrowseTypography.captionSmall` = 13.sp** -- 6 occurrences dans 2 fichiers
8. **Remplacer `4.dp` par `Tokens.Space.spaceXs`** -- 4 occurrences dans 2 fichiers
9. **`BrowseDimensions.chipCornerRadius` = 50.dp** -- 3 occurrences dans 1 fichier
10. **`AudioDimensions.*` (playButtonSize, playIconSize, etc.)** -- specfiques a AudioNowPlayingScreen

### Remplacement directs possibles (tokens existants)

| Valeur hardcodee | Token existant utilisable |
|-----------------|--------------------------|
| `8.dp` | `Tokens.Space.spaceSm` |
| `4.dp` | `Tokens.Space.spaceXs` |
| `16.dp` (quand utilise comme spacing) | `Tokens.Space.spaceMd` |
| `24.dp` (quand utilise comme spacing) | `Tokens.Space.spaceLg` |
| `32.dp` (icon size) | `TvSpacing.iconSizeLarge` / `Tokens.Space.spaceXl` |
| `48.dp` (icon/button size) | `TvSpacing.buttonHeight` / `Tokens.Space.space3xl` |
| `140.dp` (card size) | `CardDimensions.squareSize` / `CardDimensions.folderWidth` |
