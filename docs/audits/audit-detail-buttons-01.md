# Audit exhaustif — Row secondaire de boutons & écrans cachés

**Date** : 2026-03-15
**Scope** : `ui/itemdetail/` — tous les composables de contenu, row secondaire, bouton Vu/Non-vu, dimensions, icônes

---

## Étape 1 — Cartographie complète des chemins d'affichage

### Dispatcher principal (`ItemDetailScreen.kt`, lignes 117-207)

Le dispatcher utilise un `when` à deux niveaux :

```
when (item.type) {
  PERSON                              → PersonDetailsContent
  SEASON                              → SeasonDetailsContent
  SERIES                              → SeriesDetailsContent
  MUSIC_ALBUM | MUSIC_ARTIST | PLAYLIST → MusicDetailsContent
  else → when {
    seriesTimerInfo != null            → SeriesTimerDetailsContent
    item.type == PROGRAM               → LiveTvDetailsContent
    else (fallback)                    → MovieDetailsContent
  }
}
```

### Table de routage

| BaseItemKind | Composable | Fichier | Row secondaire |
|---|---|---|---|
| `PERSON` | `PersonDetailsContent` | `v2/content/PersonDetailsContent.kt` | Aucune |
| `SEASON` | `SeasonDetailsContent` | `v2/content/SeasonDetailsContent.kt` | Oui — inline |
| `SERIES` | `SeriesDetailsContent` | `v2/content/SeriesDetailsContent.kt` | Oui — via `DetailActionButtonsRow` |
| `MUSIC_ALBUM` | `MusicDetailsContent` | `v2/content/MusicDetailsContent.kt` | Oui — via `DetailActionButtonsRow` |
| `MUSIC_ARTIST` | `MusicDetailsContent` | `v2/content/MusicDetailsContent.kt` | Oui — via `DetailActionButtonsRow` |
| `PLAYLIST` | `MusicDetailsContent` | `v2/content/MusicDetailsContent.kt` | Oui — via `DetailActionButtonsRow` |
| `PROGRAM` | `LiveTvDetailsContent` | `v2/content/LiveTvDetailsContent.kt` | Custom (Record/Record Series) |
| SeriesTimer (non-type) | `SeriesTimerDetailsContent` | `v2/content/SeriesTimerDetailsContent.kt` | Custom (Cancel Series) |
| `MOVIE`, `EPISODE`, `VIDEO`, `RECORDING`, `TRAILER`, `MUSIC_VIDEO`, `BOX_SET`, autres | `MovieDetailsContent` | `v2/content/MovieDetailsContent.kt` | Oui — inline |

**Constat** : Aucun `EpisodeDetailsContent` ni `BoxSetDetailsContent` n'existe. Les épisodes et box sets passent par le fallback `MovieDetailsContent`.

---

## Étape 2 — Inventaire du bouton Vu/Non-vu par composable

### Résumé

| Composable | Bouton Vu/Non-vu | Widget | Variante | Icône | Texte | Condition état | Source |
|---|---|---|---|---|---|---|---|
| **MovieDetailsContent** | Oui | `VegafoXButton` | `Outlined`, `compact=true` | `VegafoXIcons.Visibility` | "Watched" / "Unwatched" | `played == true` | `item.userData?.played` |
| **SeasonDetailsContent** | Oui | `VegafoXButton` | `Outlined`, `compact=true` | `VegafoXIcons.Visibility` | "Watched" / "Unwatched" | `played == true` | `item.userData?.played` |
| **SeriesDetailsContent** | Via `DetailActionButtonsRow` | `VegafoXButton` | `Outlined`, `compact=true` | `VegafoXIcons.Visibility` | "Watched" / "Unwatched" | `played == true` | `item.userData?.played` |
| **MusicDetailsContent** | Via `DetailActionButtonsRow` | `VegafoXButton` | `Outlined`, `compact=true` | `VegafoXIcons.Visibility` | "Watched" / "Unwatched" | `played == true` | `item.userData?.played` |
| **PersonDetailsContent** | **Non** | — | — | — | — | — | — |
| **LiveTvDetailsContent** | **Non** | — | — | — | — | — | — |
| **SeriesTimerDetailsContent** | **Non** | — | — | — | — | — | — |

### Conditions d'affichage du bouton Watched

- **MovieDetailsContent** : `item.userData != null && item.type != BaseItemKind.PERSON`
- **SeasonDetailsContent** : Toujours affiché (pas de condition)
- **DetailActionButtonsRow** : `item.userData != null && item.type != PERSON && item.type != MUSIC_ARTIST`

**Constat** : Le bouton est **exclu** pour PERSON (partout), MUSIC_ARTIST (dans DetailActionButtonsRow uniquement). Le texte et l'icône sont **identiques** partout : même `VegafoXIcons.Visibility`, même strings R.string.lbl_watched / lbl_unwatched. L'icône ne change **jamais** entre état vu et non-vu — seul le texte change.

---

## Étape 3 — Inventaire de la row secondaire par composable

### MovieDetailsContent (lignes 287-377)

- **Container** : `Row`
- **verticalAlignment** : `Alignment.CenterVertically`
- **horizontalArrangement** : `Arrangement.spacedBy(10.dp)`
- **FlowRow** : Non — single-line `Row`

| # | Bouton | Widget | Variante | compact | Icône | Condition | Action |
|---|---|---|---|---|---|---|---|
| 1 | Audio | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Audiotrack` | `audioStreams.size > 1` | Sélecteur piste audio |
| 2 | Subtitles | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Subtitles` | `subtitleStreams.isNotEmpty()` | Sélecteur sous-titres |
| 3 | Version | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Guide` | `hasMultipleVersions` | Sélecteur version |
| 4 | Trailers | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Trailer` | `actionCallbacks.hasPlayableTrailers` | Lire bande-annonce |
| 5 | Favorite | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Favorite` | `item.userData != null` | Toggle favori |
| 6 | Watched | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Visibility` | `userData != null && type != PERSON` | Toggle vu |
| 7 | Delete | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Delete` | `item.canDelete == true` | Supprimer |

### SeasonDetailsContent (lignes 206-237)

- **Container** : `Row`
- **verticalAlignment** : `Alignment.CenterVertically`
- **horizontalArrangement** : `Arrangement.spacedBy(10.dp)`
- **FlowRow** : Non

| # | Bouton | Widget | Variante | compact | Icône | Condition | Action |
|---|---|---|---|---|---|---|---|
| 1 | Watched | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Visibility` | Toujours | Toggle vu |
| 2 | Favorite | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Favorite` | Toujours | Toggle favori |

**Constat** : SeasonDetailsContent n'a que 2 boutons secondaires, pas de Audio/Subtitles/Version/Trailers/Delete.

### DetailActionButtonsRow — Row secondaire (lignes 155-268 de DetailActions.kt)

Utilisée par **SeriesDetailsContent** et **MusicDetailsContent**.

- **Container** : `Row`
- **verticalAlignment** : `Alignment.CenterVertically`
- **horizontalArrangement** : `Arrangement.spacedBy(10.dp)`
- **FlowRow** : Non

| # | Bouton | Widget | Variante | compact | Icône | Condition | Action |
|---|---|---|---|---|---|---|---|
| 1 | Version | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Guide` | `hasMultipleVersions` | Sélecteur version |
| 2 | Audio | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Audiotrack` | `audioStreams.size > 1` | Sélecteur piste audio |
| 3 | Subtitles | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Subtitles` | `subtitleStreams.isNotEmpty()` | Sélecteur sous-titres |
| 4 | Trailers | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Trailer` | `callbacks.hasPlayableTrailers` | Lire bande-annonce |
| 5 | Favorite | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Favorite` | `item.userData != null` | Toggle favori |
| 6 | Watched | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Visibility` | `userData != null && type != PERSON && type != MUSIC_ARTIST` | Toggle vu |
| 7 | Add to Playlist | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Add` | `userData != null && type != PERSON` | Ajouter à playlist |
| 8 | Go to Series | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Tv` | `type == EPISODE && seriesId != null && onGoToSeries != null` | Naviguer vers série |
| 9 | Delete | `VegafoXButton` | `Outlined` | `true` | `VegafoXIcons.Delete` | `item.canDelete == true` | Supprimer |

### DetailActionButtonsRow — Row primaire (lignes 95-151)

| # | Bouton | Widget | Variante | compact | Icône | Condition |
|---|---|---|---|---|---|---|
| 1 | Resume | `VegafoXButton` | `Primary` | `false` | `VegafoXIcons.Play` | `hasPlaybackPosition && canPlay` |
| 2 | Restart | `VegafoXButton` | `Secondary` | `false` | `VegafoXIcons.Refresh` | Affiché avec Resume |
| 3 | Play | `VegafoXButton` | `Primary` | `false` | `VegafoXIcons.Play` | `canPlay && !hasPlaybackPosition` |
| 4 | Shuffle All | `VegafoXButton` | `Secondary` | `true` | `VegafoXIcons.Shuffle` | `isFolder == true && type != BOX_SET` |
| 5 | Instant Mix | `VegafoXButton` | `Secondary` | `true` | `VegafoXIcons.Mix` | `type == MUSIC_ARTIST` |

### LiveTvDetailsContent (lignes 177-217)

- **Container** : `Row`
- **horizontalArrangement** : `Arrangement.Center`
- Uses `DetailActionButton` (not `VegafoXButton`)

| # | Bouton | Widget | Icône | Condition | isActive |
|---|---|---|---|---|---|
| 1 | Tune to Channel | `DetailActionButton` | `VegafoXIcons.Play` | Toujours | — |
| 2 | Record | `DetailActionButton` | `VegafoXIcons.Record` | `programInfo != null` | `isRecording` |
| 3 | Record Series | `DetailActionButton` | `VegafoXIcons.RecordSeries` | `programInfo.isSeries == true` | `isRecordingSeries` |

### SeriesTimerDetailsContent (lignes 131-149)

- **Container** : `Row`
- **horizontalArrangement** : `Arrangement.spacedBy(16.dp)`

| # | Bouton | Widget | Icône | Condition |
|---|---|---|---|---|
| 1 | Cancel Series | `DetailActionButton` | `VegafoXIcons.Delete` | Toujours |

### PersonDetailsContent

**Aucune row de boutons** — filmographie et biographie uniquement.

---

## Étape 4 — Fichiers cachés ou non audités

### Fichiers découverts dans `ui/itemdetail/` (non couverts par la liste initiale)

| Fichier | Rôle |
|---|---|
| `compose/ItemDetailComposeFragment.kt` | Fragment wrapper Compose — gère lifecycle playback, theme music, confirmation suppression |
| `compose/DetailHeroBackdrop.kt` | Backdrop hero plein écran avec blur, gradients, crossfade 400ms |
| `v2/ItemDetailsViewModel.kt` | ViewModel — state management, mutations `toggleWatched()`, `toggleFavorite()` |
| `v2/ItemDetailsUiState.kt` | Data class état UI |
| `v2/TrailerPlayerFragment.kt` | Lecteur YouTube fullscreen via ExoPlayer avec SponsorBlock |
| `v2/shared/DetailInfoRow.kt` | Row métadonnées (année, durée, saisons, badge statut, note, badges média) |
| `ItemListFragment.kt` | Fragment legacy — navigation liste playlist/collection |
| `MusicFavoritesListFragment.kt` | Fragment favoris musique — boutons Play/Shuffle, menu popup |
| `ItemListFragmentHelper.kt` | Extensions pour ItemListFragment (loadItem, toggleFavorite, etc.) |

### Composables de contenu absents

- **`EpisodeDetailsContent`** : N'existe pas. Les épisodes passent par `MovieDetailsContent` (fallback).
- **`BoxSetDetailsContent`** : N'existe pas. Les box sets passent par `MovieDetailsContent` (fallback).
- **Aucun autre composable de contenu** non listé.

### Patterns de dispatch trouvés hors dispatcher principal

| Fichier | Pattern | Usage |
|---|---|---|
| `ItemDetailComposeFragment.kt:201` | `if (item.type == EPISODE && item.seriesId != null)` | Navigation retour vers série |
| `ItemDetailComposeFragment.kt:223` | `if (positionMs == 0 && item.type == MOVIE)` | Logique de lecture |
| `DetailActions.kt:74-88` | Liste `canPlay` par type | MOVIE, EPISODE, VIDEO, RECORDING, TRAILER, MUSIC_VIDEO, SERIES, SEASON, PROGRAM, MUSIC_ALBUM, PLAYLIST, MUSIC_ARTIST |
| `DetailActions.kt:130` | `isFolder && type != BOX_SET` | Condition Shuffle All |
| `DetailActions.kt:141,220,235,247` | Checks par type | Visibilité boutons secondaires |

---

## Étape 5 — Icônes Vu/Non-vu dans VegafoXIcons

**Fichier** : `ui/base/icons/VegafoXIcons.kt`

### Icônes pertinentes trouvées

| Nom propriété | Source | Usage |
|---|---|---|
| `Visibility` | `Icons.Default.Visibility` | **Bouton Watched/Unwatched** dans tous les composables |
| `Check` | `Icons.Default.Check` | Confirmation générique |
| `CheckCircle` | `Icons.Default.CheckCircle` | Statut Jellyseerr "Available" + note Rotten Tomato "Fresh" |

### Constats

1. **Une seule icône pour les deux états** : `VegafoXIcons.Visibility` (œil ouvert) est utilisée que l'item soit vu ou non-vu. Aucune icône `VisibilityOff` (œil barré) n'est définie.
2. **Pas d'icône dédiée Watched/Unwatched** : le basculement visuel repose uniquement sur le changement de texte ("Watched" ↔ "Unwatched"), pas sur l'icône.
3. **`CheckCircle` réutilisé** pour deux contextes différents (Jellyseerr + Rotten Tomato), ce qui peut créer une ambiguïté visuelle.

---

## Étape 6 — Dimensions des boutons

### Valeurs dans VegafoXDimensions.kt

| Objet | Propriété | Valeur |
|---|---|---|
| `ButtonDimensions` | `height` | **48.dp** |
| `ButtonDimensions` | `heightCompact` | **40.dp** |
| `ButtonDimensions` | `minWidth` | 200.dp |
| `ButtonDimensions` | `minWidthCompact` | 120.dp |
| `ButtonDimensions` | `cornerRadius` | **10.dp** |
| `DialogDimensions` | `maxListHeight` | 400.dp |
| `HeroDimensions` | `actionsBarHeight` | 80.dp |

### Hauteurs effectives des boutons dans les rows de détail

Tous les boutons secondaires utilisent `compact = true` → **hauteur effective = 40.dp**.
Les boutons primaires (Play, Resume) utilisent `compact = false` → **hauteur effective = 48.dp**.

### Violations — Hauteurs hardcodées au lieu de ButtonDimensions

| Fichier | Composant | Valeur hardcodée | Devrait être |
|---|---|---|---|
| `ui/base/button/Button.kt:112` | ButtonRow | `minHeight = 40.dp` | `ButtonDimensions.heightCompact` |
| `ui/base/components/TvButton.kt:17` | TvIconButton | `TvButtonMinHeight = 48.dp` (private) | `ButtonDimensions.height` |
| `ui/base/components/TvSwitch.kt:46` | TvSwitch | `minHeight = 56.dp` | Pas de token existant |
| `ui/base/Badge.kt:29` | Badge | `minHeight = 24.dp` | Pas de token existant |
| `ui/base/skeleton/SkeletonPresets.kt:233,237` | Skeleton bouton | `.height(40.dp)` | `ButtonDimensions.heightCompact` |
| `ui/base/state/ErrorBanner.kt:51` | ErrorBanner | `.height(56.dp)` | Pas de token existant |
| `ui/playback/overlay/compose/PlayerDialogDefaults.kt` | Dialog controls | `56.dp` et `48.dp` | Tokens à créer |

### Conformité VegafoXButton

`VegafoXButton.kt:217` — **Conforme** : utilise `.height(if (compact) ButtonDimensions.heightCompact else ButtonDimensions.height)`.

---

## Synthèse et observations

### Cohérence de la row secondaire

| Aspect | MovieDetailsContent | SeasonDetailsContent | DetailActionButtonsRow |
|---|---|---|---|
| Nb boutons max | 7 | 2 | 9 |
| Widget | `VegafoXButton` | `VegafoXButton` | `VegafoXButton` |
| Variante | `Outlined` | `Outlined` | `Outlined` |
| compact | `true` | `true` | `true` |
| Hauteur | 40.dp | 40.dp | 40.dp |
| Arrangement | `spacedBy(10.dp)` | `spacedBy(10.dp)` | `spacedBy(10.dp)` |
| FlowRow | Non | Non | Non |

**Tous les boutons secondaires sont parfaitement cohérents** en widget, variante, hauteur et espacement.

### Différences notables

1. **MovieDetailsContent vs DetailActionButtonsRow** : L'ordre des boutons Audio/Subtitles/Version diffère (Movie: Audio→Subtitles→Version ; Shared: Version→Audio→Subtitles). DetailActionButtonsRow a 2 boutons supplémentaires (Add to Playlist, Go to Series).
2. **SeasonDetailsContent** : Row réduite à 2 boutons (Watched + Favorite) — pas d'accès Audio/Subtitles/Trailers/Delete.
3. **LiveTv et SeriesTimer** : Utilisent `DetailActionButton` au lieu de `VegafoXButton` — design icon-centric avec label en dessous, arrangement `Center` au lieu de `spacedBy(10.dp)`.
4. **PersonDetailsContent** : Aucun bouton d'action, aucune row.

### Problèmes identifiés

1. **Icône Vu/Non-vu identique** : `VegafoXIcons.Visibility` ne change pas entre les deux états — seul le texte bascule. Considérer `VisibilityOff` pour l'état "vu".
2. **Pas de FlowRow** : Si trop de boutons sont affichés simultanément (ex: 9 dans DetailActionButtonsRow), ils dépasseront l'écran sur les petites résolutions. Aucun wrapping prévu.
3. **Duplication** : MovieDetailsContent et DetailActionButtonsRow implémentent la même logique de row secondaire avec un ordre légèrement différent. Factoriser.
4. **6 fichiers avec hauteurs hardcodées** au lieu d'utiliser les tokens de `ButtonDimensions`.
