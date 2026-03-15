# Browse — Phase 1 : Migration VegafoX

> Date : 2026-03-12
> Scope : LibraryBrowseComponents, LibraryBrowseScreen, GenresGridScreen, FolderBrowseScreen, PremiumSideBar

---

## Fichiers modifies

### 1. `ui/browsing/v2/LibraryBrowseComponents.kt`

| Composant | Migration |
|-----------|-----------|
| **LibraryToolbarButton** | Fond transparent, coins 10dp, icone 24dp TextSecondary. Focus : scale 1.08 spring Medium, icone TextPrimary, fond rgba(255,255,255,0.08), glow OrangePrimary alpha 0.20 blur radial |
| **LibraryPosterCard** | Border 2dp OrangePrimary au focus (etait absent). Glow OrangePrimary alpha 0.25 radial au focus. Progress bar et watched indicator conserves |
| **FocusedItemHud** | Titre 16sp Bold TextPrimary. Fond BackgroundDeep alpha 0.85 + padding. Metadata : composants externes non modifies |
| **FilterSortDialog** | Fond VegafoXColors.Surface (#141418). Titre 18sp Bold TextPrimary. Radio circles OrangePrimary. Texte labels TextPrimary. Boutons VegafoXButton (Ghost Annuler + Primary OK) en bas |
| **AlphaPickerBar** | Fond barre rgba(255,255,255,0.04). Lettres 13sp TextSecondary. Focus : OrangePrimary Bold, scale 1.15 spring, fond OrangeSoft |
| **FilterRadioRow** | Radio tint OrangePrimary. Texte selected OrangePrimary. Texte normal TextPrimary |
| **FilterToggleRow** | Checkbox tint OrangePrimary. Check text Background. Texte selected OrangePrimary |

### 2. `ui/browsing/compose/LibraryBrowseScreen.kt`

| Element | Migration |
|---------|-----------|
| **TvHeader** | Remplace par header custom : titre 40sp Bold letterSpacing 2sp TextPrimary, subtitle 14sp TextSecondary, padding 32dp top 56dp horizontal |
| **FilterChip** | Fond rgba(255,255,255,0.06), bordure 1dp rgba(255,255,255,0.10), coins 50dp, padding 8x16dp, texte 13sp TextSecondary. Actif : fond OrangeSoft, bordure OrangePrimary, texte OrangePrimary. Focus : scale 1.04 spring, bordure OrangePrimary 60% |
| **LetterChip** | Meme style que FilterChip. Selected : fond OrangePrimary, texte blanc Bold |
| **Fond ecran** | BackgroundDeep (#060A0F) via Modifier.background sur Column |

### 3. `ui/browsing/compose/GenresGridScreen.kt`

| Element | Migration |
|---------|-----------|
| **GenreCard gradient** | Color.Black alpha 0.75 → BackgroundDeep alpha 0.80 |
| **GenreCard texte** | Color.White → TextPrimary |
| **GenreSortDialog** | Fond Surface, titre 18sp Bold TextPrimary, dividers VegafoXColors.Divider, radio OrangePrimary, bouton Ghost Fermer |
| **LibraryFilterDialog** | Idem GenreSortDialog |
| **DialogRadioItem** | Radio circle OrangePrimary, texte selected OrangePrimary, texte normal TextPrimary |

### 4. `ui/browsing/compose/FolderBrowseScreen.kt`

| Element | Migration |
|---------|-----------|
| **AppBackground + overlay** | Remplace par fond BackgroundDeep explicite |
| **FolderItemCard focus** | Border 2dp OrangePrimary + glow radial OrangePrimary alpha 0.20 |
| **Card title text** | JellyfinTheme.colorScheme.onSurface → VegafoXColors.TextSecondary |

### 5. `ui/home/compose/sidebar/PremiumSideBar.kt`

| Element | Migration |
|---------|-----------|
| **Genres** | Nouvel item NavItem : VegafoXIcons.Genres, label "Genres", navigation → Destinations.allGenres |
| **Favoris** | Nouvel item NavItem : VegafoXIcons.Favorite, label "Favoris", navigation → Destinations.allFavorites |
| **NAV_ITEM_COUNT** | 7 → 9 |
| **Index shift** | Settings 6→8, Profil reste apres Settings |

Ordre final sidebar :
0. Accueil
1. Recherche
2. Films
3. Series
4. Live TV
5. Media
6. **Genres** (NOUVEAU)
7. **Favoris** (NOUVEAU)
8. Parametres
9. Profil

---

## Screenshots

| Ecran | Fichier |
|-------|---------|
| Home + Sidebar complete | `docs/screenshots/sidebar_genres.png` |
| Films — LibraryBrowseScreen | `docs/screenshots/browse_library_v2.png` |
| Genres — GenresGridScreen | `docs/screenshots/browse_genres_v2.png` |

---

## Build

- Debug : `assembleGithubDebug` OK
- Release : `assembleGithubRelease` OK
- Installe sur AM9 Pro (192.168.1.152) : OK
- Navigation sidebar → Genres : fonctionnel
- Navigation sidebar → Films : fonctionnel

---

## Notes

- Bebas Neue non disponible dans le projet : utilise le systeme Bold + fontSize 40sp + letterSpacing 2sp comme substitut
- Les composants de metadata FocusedItemHud (InfoItemText, InfoItemBadge, etc.) sont externes — non modifies pour eviter impact cascade
- Les V2 browse fragments (LiveTv, Music, Schedule, Recordings, SeriesRecordings) utilisent LibraryBrowseComponents et beneficient automatiquement des migrations
