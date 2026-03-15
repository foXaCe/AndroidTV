# Browse — Phase 2 : Bebas Neue + Ecrans secondaires

> Date : 2026-03-12
> Scope : Bebas Neue, ByLetterBrowseScreen, AllFavoritesScreen, FolderBrowseScreen, SearchScreen, SearchTextInput, titres details/player

---

## Fichier 1 — Bebas Neue

| Element | Statut |
|---------|--------|
| `res/font/` | Cree (n'existait pas) |
| `res/font/bebas_neue.ttf` | Telecharge depuis Google Fonts GitHub (61 KB) |
| `VegafoXTypography.kt` | Cree — `val BebasNeue = FontFamily(Font(R.font.bebas_neue))` |
| Google Fonts provider | Non utilise — font embarquee en local (pas de dependance reseau) |

Bebas Neue appliquee a :

| Fichier | Element | Style |
|---------|---------|-------|
| `LibraryBrowseScreen.kt` | Titre bibliotheque | 40sp Bold BebasNeue letterSpacing 2sp |
| `ByLetterBrowseScreen.kt` | Titre ecran | 40sp Bold BebasNeue letterSpacing 2sp |
| `AllFavoritesScreen.kt` | Titre ecran | 40sp Bold BebasNeue letterSpacing 2sp |
| `FolderBrowseScreen.kt` | Titre dossier | 40sp Bold BebasNeue letterSpacing 2sp |
| `SearchScreen.kt` | Titre "Rechercher" | 40sp Bold BebasNeue letterSpacing 2sp |
| `MovieDetailsContent.kt` | Titre film | 68sp Black BebasNeue letterSpacing 2sp |
| `SeasonDetailsContent.kt` | Titre saison | 68sp Black BebasNeue letterSpacing 2sp |
| `SeriesDetailsContent.kt` | Titre serie | 68sp Black BebasNeue letterSpacing 2sp |
| `VideoPlayerHeader.kt` | Titre player | 36sp Bold BebasNeue letterSpacing 2sp |

---

## Fichier 2 — ByLetterBrowseScreen.kt

| Element | Migration |
|---------|-----------|
| **TvHeader** | Remplace par header custom VegafoX : 40sp Bold BebasNeue TextPrimary |
| **Fond ecran** | BackgroundDeep ajoute via Modifier.background |

---

## Fichier 3 — AllFavoritesScreen.kt

| Element | Migration |
|---------|-----------|
| **TvHeader** | Remplace par header custom VegafoX : 40sp Bold BebasNeue TextPrimary |
| **Fond ecran** | BackgroundDeep ajoute via Modifier.background |

---

## Fichier 4 — FolderBrowseScreen.kt

| Element | Migration |
|---------|-----------|
| **FolderHeader titre** | headlineMedium Light onSurface → headlineLarge 40sp Bold BebasNeue TextPrimary |
| **Fond ecran** | Deja BackgroundDeep (phase 1) |

---

## Fichier 5 — SearchScreen + SearchTextInput

### SearchScreen.kt

| Element | Migration |
|---------|-----------|
| **TvHeader** | Remplace par header custom VegafoX : 40sp Bold BebasNeue TextPrimary |
| **Fond ecran** | BackgroundDeep ajoute |
| **Empty state** | Deja correct : icone VegafoXIcons.Search + texte |
| **Cards resultats** | BrowseMediaCard deja migre phase 1 |

### SearchTextInput.kt

| Element | Migration |
|---------|-----------|
| **Fond champ** | Surface (#141418) via background() |
| **Bordure repos** | rgba(255,255,255,0.10) 1dp |
| **Bordure focus** | OrangePrimary 1dp |
| **Coins** | 12dp (etait percent 30) |
| **Texte saisi** | TextPrimary 16sp |
| **Icone loupe repos** | TextSecondary |
| **Icone loupe focus** | OrangePrimary |
| **Curseur** | OrangePrimary |

---

## Fichiers modifies

1. `app/src/main/res/font/bebas_neue.ttf` (NOUVEAU)
2. `ui/base/theme/VegafoXTypography.kt` (NOUVEAU)
3. `ui/browsing/compose/LibraryBrowseScreen.kt`
4. `ui/browsing/compose/ByLetterBrowseScreen.kt`
5. `ui/browsing/compose/AllFavoritesScreen.kt`
6. `ui/browsing/compose/FolderBrowseScreen.kt`
7. `ui/search/compose/SearchScreen.kt`
8. `ui/search/composable/SearchTextInput.kt`
9. `ui/itemdetail/v2/content/MovieDetailsContent.kt`
10. `ui/itemdetail/v2/content/SeasonDetailsContent.kt`
11. `ui/itemdetail/v2/content/SeriesDetailsContent.kt`
12. `ui/player/video/VideoPlayerHeader.kt`

---

## Build

- Debug : `assembleGithubDebug` OK
- Release : `assembleGithubRelease` OK
- Installe sur AM9 Pro (192.168.1.152) : OK

---

## Screenshots

| Ecran | Fichier |
|-------|---------|
| Recherche — header BebasNeue + champ VegafoX | `docs/screenshots/search_v2.png` |
| Films — titre BebasNeue | `docs/screenshots/browse_byletter.png` |

---

## Notes

- Bebas Neue est une font display sans-serif condensee uppercase — rendu "cinema" naturel sans forcer uppercase dans le code
- Font embarquee localement (pas de Google Fonts provider runtime) pour eviter dependance reseau sur Android TV
- Le fichier TTF vient du repo officiel google/fonts sur GitHub
