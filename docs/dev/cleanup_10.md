# Cleanup 10 — Magic numbers ≥ 50dp/sp → VegafoXDimensions.kt

**Date** : 2026-03-13
**Scope** : Centralisation des valeurs hardcodées ≥ 50dp/sp dans VegafoXDimensions.kt
**Basé sur** : cleanup_09.md (VegafoXDimensions.kt initial), audit cleanup_08.md

---

## Inventaire initial

| Zone | Occurrences ≥ 50dp/sp |
|------|----------------------|
| ui/itemdetail | 48 |
| ui/browsing | 37 |
| ui/base | 34 |
| ui/jellyseerr | 29 |
| ui/startup | 18 |
| ui/playback | 13 |
| ui/home | 11 |
| ui/livetv | 8 |
| ui/settings | 5 |
| ui/shared | 3 |
| ui/player | 2 |
| ui/composable | 2 |
| integration/dream | 2 |
| ui/search | 1 |
| **Total** | **223** |

Top valeurs : `60.dp` (36×), `80.dp` (22×), `50.dp` (16×), `56.dp` (14×), `340.dp` (11×), `440.dp` (9×)

---

## Objets ajoutés dans VegafoXDimensions.kt

### Existant (cleanup_09)

| Objet | Propriétés |
|-------|-----------|
| `DialogDimensions` | `maxListHeight = 400.dp` |
| `ButtonDimensions` | `minWidth = 200.dp`, `minWidthCompact = 120.dp`, `height = 52.dp`, `heightCompact = 40.dp` |
| `DreamDimensions` | `logoWidth = 400.dp`, `logoHeight = 200.dp`, `albumCoverSize = 128.dp`, `clockWidth = 150.dp`, `carouselMaxHeight = 75.dp`, `fadingEdgeVertical = 250.dp` |
| `LiveTvDimensions` | `guideHeaderHeight = 120.dp` |

### Nouveau (cleanup_10)

| Objet | Propriété | Valeur | Fichiers |
|-------|-----------|--------|----------|
| `DialogDimensions` | `standardMinWidth` | 340.dp | 8 fichiers (ItemDetails ×2, GenresGrid ×2, LibraryBrowse, ExitConfirmation, CreatePlaylist, AddToPlaylist, ShuffleOptions) |
| `DialogDimensions` | `standardMaxWidth` | 440.dp | idem |
| `HeroDimensions` | `backdropHeight` | 580.dp | Movie, Series, Season DetailsContent |
| `HeroDimensions` | `horizontalPadding` | 80.dp | Movie, Series, Season DetailsContent |
| `HeroDimensions` | `titleFontSize` | 68.sp | Movie, Series, Season DetailsContent |
| `HeroDimensions` | `titleLineHeight` | 72.sp | Movie, Series, Season DetailsContent |
| `HeroDimensions` | `actionsBarHeight` | 80.dp | (sémantique, pas encore substitué) |
| `HeroDimensions` | `contentTopPadding` | 100.dp | Music, LiveTv, SeriesTimer, Person DetailsContent |
| `CardDimensions` | `landscapeWidth` | 220.dp | ItemDetailsComponents ×3, BrowseMediaCard, SkeletonPresets ×2 |
| `CardDimensions` | `landscapeHeight` | 124.dp | ItemDetailsComponents ×2, SkeletonPresets ×2 |
| `CardDimensions` | `portraitWidth` | 150.dp | JellyseerrCards, SkeletonPresets ×3 |
| `CardDimensions` | `portraitHeight` | 225.dp | JellyseerrCards, SkeletonPresets ×3 |
| `BrowseDimensions` | `contentPaddingHorizontal` | 60.dp | 10 fichiers (FolderBrowse, MusicBrowse, RecordingsBrowse, ScheduleBrowse, SeriesRecordingsBrowse, LibraryBrowseComponents, SkeletonPresets ×4) |
| `BrowseDimensions` | `gridPaddingHorizontal` | 56.dp | AllFavorites, LibraryBrowse, ByLetterBrowse, SearchScreen |
| `SidebarDimensions` | `widthCollapsed` | 72.dp | PremiumSideBar, MainToolbar |
| `SidebarDimensions` | `widthExpanded` | 220.dp | PremiumSideBar |
| `SidebarDimensions` | `navItemHeight` | 52.dp | NavItem ×2, ProfileItem ×2 |
| `JellyseerrDimensions` | `screenPaddingHorizontal` | 50.dp | JellyseerrMediaDetailsScreen ×5, JellyseerrPersonDetailsScreen |
| `JellyseerrDimensions` | `mediaBackdropHeight` | 400.dp | JellyseerrMediaDetailsScreen ×3 |
| `JellyseerrDimensions` | `posterWidth` | 208.dp | JellyseerrMediaDetailsScreen |
| `JellyseerrDimensions` | `posterHeight` | 312.dp | JellyseerrMediaDetailsScreen |
| `JellyseerrDimensions` | `factsColumnWidth` | 320.dp | JellyseerrFactsSection |
| `ToolbarDimensions` | `height` | 95.dp | Toolbar |
| `LiveTvDimensions` | `programDetailDialogWidth` | 640.dp | ProgramDetailDialog |
| `LiveTvDimensions` | `recordDialogWidth` | 500.dp | RecordDialog |
| `LiveTvDimensions` | `browseScreenPadding` | 80.dp | LiveTvBrowseScreen |
| `StartupDimensions` | `foxLogoSize` | 160.dp | VegafoXFoxLogo |
| `StartupDimensions` | `titleFontSize` | 52.sp | VegafoXTitleText |
| `StartupDimensions` | `dialogWidth` | 420.dp | QuickConnectScreen, ServerDiscoveryScreen |

---

## Remplacements par zone

| Zone | Fichiers | Remplacements |
|------|----------|---------------|
| A — Home/Sidebar | 3 | 6 (sidebar: widths, navItemHeight) |
| B — ItemDetail | 8 | 22 (hero: backdrop, padding, font; cards; dialogs; contentTopPadding) |
| C — Browse/Search | 13 | ~30 (contentPadding, gridPadding, dialog widths, card default) |
| D — LiveTV | 3 | 4 (dialog widths, browse padding) |
| E — Jellyseerr | 4 | 14 (screen padding, backdrop height, poster, facts) |
| F — Base/Skeleton/Toolbar | 3 | 18 (card defaults, padding, toolbar height, sidebar width) |
| G — Playlist/Shuffle | 3 | 3 (dialog standard widths) |
| H — Startup | 4 | 5 (fox logo, title fontSize, dialog widths) |
| **Total** | **43 fichiers** | **~96 remplacements** |

---

## Résiduel accepté (127 occurrences)

| Catégorie | Exemples | Raison |
|-----------|----------|--------|
| Valeurs uniques par contexte | `170×255.dp`, `280×158.dp`, `165×248.dp` (ItemDetails card sizes par aspect ratio) | Usage unique, spécifique au composant |
| Tailles d'icônes variées | `72.dp` (PinEntry, QuickConnect), `64.dp`, `96.dp` | Même valeur mais sémantiques différentes |
| Layouts spécifiques startup | `480.dp` (ServerDiscovery), `640.dp` (UserLogin), `360.dp` (PinEntry) | Usage unique |
| Shapes/constants | `999.dp` (shapes.kt Full), `50.dp` (RoundedCornerShape pill) | Constants de forme, pas des dimensions |
| Paddings contextuels | `80.dp` dans playback, person details, etc. | Sémantiques différentes de HeroDimensions |
| Typography lineHeight | `56.sp` | Défini dans typography.kt, pas un magic number |
| Dialogs non-standard | `460.dp`, `480.dp`, `500/800.dp` (UpdateDialogs), `380/500.dp` (PlayerDialogs) | Dimensions spécifiques à ces dialogs |
| Composants isolés | `145.dp` (StillWatching), `260.dp` (DonateDialog), `350.dp` (SettingsLayout) | Usage unique |

---

## VegafoXDimensions.kt final

11 objets, 30 propriétés, 122 lignes.

```
DialogDimensions     — maxListHeight, standardMinWidth, standardMaxWidth
ButtonDimensions     — minWidth, minWidthCompact, height, heightCompact
HeroDimensions       — backdropHeight, horizontalPadding, titleFontSize, titleLineHeight, actionsBarHeight, contentTopPadding
CardDimensions       — landscapeWidth, landscapeHeight, portraitWidth, portraitHeight
BrowseDimensions     — contentPaddingHorizontal, gridPaddingHorizontal
SidebarDimensions    — widthCollapsed, widthExpanded, navItemHeight
JellyseerrDimensions — screenPaddingHorizontal, mediaBackdropHeight, posterWidth, posterHeight, factsColumnWidth
ToolbarDimensions    — height
DreamDimensions      — logoWidth, logoHeight, albumCoverSize, clockWidth, carouselMaxHeight, fadingEdgeVertical
LiveTvDimensions     — guideHeaderHeight, programDetailDialogWidth, recordDialogWidth, browseScreenPadding
StartupDimensions    — foxLogoSize, titleFontSize, dialogWidth
```

---

## Correctif additionnel

- **Splash screen** : `vegafox_splash.png` remplacé par `Asset 3 — Splash Screen.png` (fox orange correct au lieu de l'ancien logo bleu/violet)

---

## LOC ce round

| Action | LOC |
|--------|-----|
| VegafoXDimensions.kt étendu (28→122) | +94 |
| Imports ajoutés (46 lignes dans 43 fichiers) | +46 |
| Remplacements (neutre en LOC) | 0 |
| **Net** | **+140** |

## LOC total tous cleanups

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3,124 |
| 05 | ~1,581 + 6,751 traductions |
| 06 | ~369 |
| 07 | ~361 + 900 KB images |
| 08 | ~95 |
| 09 | ~110 + 47 traductions |
| **10** | **+140 (centralisation, pas suppression)** |
| **Total net** | **~6,075 LOC + 6,798 traductions + 900 KB images** |

Note : Ce round ajoute du code net car la centralisation crée le fichier de constantes et les imports. Le gain est en maintenabilité, pas en réduction LOC.

---

## Build

- Debug (github) : BUILD SUCCESSFUL
- Release (github) : BUILD SUCCESSFUL
- Installé sur AM9 Pro : Success
