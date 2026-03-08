# V2B — Décomposition de ItemDetailsFragment.kt

## Contexte

`ItemDetailsFragment.kt` était le plus gros fichier du projet (2058 lignes, violation V02 de l'audit architecture). Il gérait 6+ types de médias (Movie, Series, Music, Person, Episode, Season, BoxSet, Playlist) dans un Fragment monolithique avec 26 fonctions privées.

## Objectif

Décomposer en fichiers spécialisés par type de média, sans toucher au ViewModel (`ItemDetailsViewModel`), ni à `ItemDetailsUiState`, ni au comportement visible.

## Structure finale

```
ui/itemdetail/v2/
├── ItemDetailsFragment.kt        629 lignes  (dispatcher + lifecycle)
├── ItemDetailsComponents.kt     1458 lignes  (inchangé — cards, dialogs)
├── ItemDetailsViewModel.kt       509 lignes  (inchangé)
├── shared/
│   ├── DetailUtils.kt              64 lignes  (URL helpers, formatDuration, getEndsAt)
│   ├── DetailActions.kt           306 lignes  (DetailActionCallbacks, DetailActionButtonsRow)
│   ├── DetailInfoRow.kt           116 lignes  (InfoRow avec badges, ratings, durée)
│   └── DetailSections.kt         262 lignes  (Metadata, Seasons, Episodes, Cast, Cards, Collection, PlaylistHint)
└── content/
    ├── MovieDetailsContent.kt     294 lignes  (Movie, Episode, Video, Recording, Trailer, MusicVideo, BoxSet)
    ├── SeriesDetailsContent.kt    243 lignes  (Series — seasons, next up)
    ├── MusicDetailsContent.kt     438 lignes  (MusicAlbum, MusicArtist, Playlist — tracks, reorder, backdrop)
    ├── PersonDetailsContent.kt    295 lignes  (Person — filmography slideshow)
    └── SeasonDetailsContent.kt    243 lignes  (Season — episode list)
```

## Répartition des lignes

| Fichier | Avant | Après |
|---------|-------|-------|
| `ItemDetailsFragment.kt` | 2058 | 629 |
| `shared/DetailUtils.kt` | — | 64 |
| `shared/DetailActions.kt` | — | 306 |
| `shared/DetailInfoRow.kt` | — | 116 |
| `shared/DetailSections.kt` | — | 262 |
| `content/MovieDetailsContent.kt` | — | 294 |
| `content/SeriesDetailsContent.kt` | — | 243 |
| `content/MusicDetailsContent.kt` | — | 438 |
| `content/PersonDetailsContent.kt` | — | 295 |
| `content/SeasonDetailsContent.kt` | — | 243 |

**Réduction du Fragment** : 2058 → 629 lignes (**-69%**)

## Ce que contient encore le Fragment (629 lignes)

- `onCreateView()` — layout FrameLayout, backdrop ImageView, ComposeView, sidebar/toolbar
- `onViewCreated()` — chargement item, backdrop blur, theme music
- `onDestroyView()` — arrêt theme music
- `ItemDetailsContent()` — dispatcher `when(item.type)` vers les content composables
- `createActionCallbacks()` — factory pour `DetailActionCallbacks`
- `play()`, `handlePlay()`, `handleResume()`, `handleShuffle()` — méthodes de playback (dépendent du lifecycle Fragment)
- `playTrailers()` — résolution YouTube/external trailers
- `confirmDeleteItem()`, `deleteItem()` — suppression avec AlertDialog
- Navigation D-pad (intercept LEFT/RIGHT/UP/DOWN pour sidebar/toolbar)

## Composables extraits

### shared/DetailUtils.kt
- `getBackdropUrl(item, api)` — URL backdrop (item + fallback parent)
- `getPosterUrl(item, api)` — URL poster selon le type
- `getLogoUrl(item, api)` — URL logo
- `getEpisodeThumbnailUrl(ep, api)` — URL thumbnail épisode
- `formatDuration(ticks)` — format "1h 23m"
- `getEndsAt(ticks)` — heure de fin estimée

### shared/DetailActions.kt
- `DetailActionCallbacks` — data class bundlant 13 callbacks + trackSelector
- `DetailActionButtonsRow()` — boutons Play/Resume/Shuffle/Trailers/Audio/Subtitle/Version/Watched/Favorite/Playlist/GoToSeries/Delete
- Dialogs inline : audio track, subtitle track, version selector

### shared/DetailInfoRow.kt
- `DetailInfoRow()` — année, durée, ends-at, season count, status badge, rating, badges médias

### shared/DetailSections.kt
- `DetailMetadataSection()` — genres, directors, writers, studios
- `DetailSeasonsSection()` — grille horizontale de saisons
- `DetailEpisodesHorizontalSection()` — carousel d'épisodes
- `DetailCastSection()` — carousel cast & crew
- `DetailSectionWithCards()` — section générique avec SimilarItemCard/LandscapeItemCard
- `DetailCollectionItemsGrid()` — grille FlowRow pour BoxSet
- `DetailPlaylistHint()` — hint de réorganisation playlist

### content/MovieDetailsContent.kt
Types gérés : Movie, Episode, Video, Recording, Trailer, MusicVideo, BoxSet
- Header avec logo/titre, épisode badge S×E×
- Action buttons (via DetailActionCallbacks)
- Metadata, Episodes horizontal, Collection grid, Cast, Similar

### content/SeriesDetailsContent.kt
Type géré : Series
- Header avec season count + status badge (Continuing/Ended)
- Action buttons, Metadata, Next Up, Seasons, Cast, Similar

### content/MusicDetailsContent.kt
Types gérés : MusicAlbum, MusicArtist, Playlist
- Playlist rotating backdrop slideshow (Crossfade 8s)
- Track action dialog (Play from here, Play, Queue, Instant Mix, Remove)
- Track list avec reorder (Move Up/Down)
- Albums section, Cast, Similar

### content/PersonDetailsContent.kt
Type géré : Person
- Filmography backdrop slideshow
- Photo + bio (nom, date naissance, lieu, overview)
- Movies section, Series section (avec focus → backdrop change)

### content/SeasonDetailsContent.kt
Type géré : Season
- Poster + series name + season name + episode count
- Play/Watched/Favorite buttons
- Episode list (SeasonEpisodeItem cards)

## Règles respectées

- **ViewModel inchangé** : `ItemDetailsViewModel.kt` et `ItemDetailsUiState` non modifiés
- **Comportement identique** : même dispatching par type, mêmes actions, mêmes dialogs
- **Design system** : aucune couleur/dimension hardcodée ajoutée, tout via `JellyfinTheme.*`
- **Compilation** : `BUILD SUCCESSFUL` à chaque étape

## Vérification de compilation

```
> Task :app:compileGithubReleaseKotlin
BUILD SUCCESSFUL in 13s
66 actionable tasks: 1 executed, 65 up-to-date
```
