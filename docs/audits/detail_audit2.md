# Audit technique detaille — Ecran detail media

**Date** : 2026-03-11
**Base** : audit_detail.md (premiere passe)
**Objectif** : Cartographie fichier par fichier, role, technologie, contenu affiche

---

## 1. Inventaire complet des fichiers (21 fichiers, 7 893 lignes)

### 1.1 Fragments (points d'entree)

| # | Fichier | Lignes | Tech | Role exact |
|---|---------|--------|------|------------|
| 1 | `compose/ItemDetailComposeFragment.kt` | 376 | Compose (ComposeView wrapper) | **Point d'entree principal** (`Destinations.itemDetails()`). Thin Fragment : extrait args (itemId, serverId, channelId, programInfoJson, seriesTimerJson), cree `ComposeView` avec `ItemDetailScreen`, gere le cycle de vie playback (play, resume, shuffle, trailers locaux/YouTube), theme music start/stop, delete confirmation dialog. Supporte 3 modes d'entree : standard, Live TV channel/program, Live TV series timer. |
| 2 | `v2/ItemDetailsFragment.kt` | 650 | Hybride (FrameLayout + ComposeView) | **Fragment legacy** (`Destinations.itemDetailsV2()`). Construit manuellement un FrameLayout avec : ImageView backdrop (alpha 0.8, blur via BitmapBlur ou RenderEffect API 31+), gradient view (drawable), ComposeView pour le contenu, sidebar/toolbar overlay. Gestion DPAD sidebar/toolbar focus manuelle (dispatchKeyEvent). NE supporte PAS les args Live TV (channelId, programInfo, seriesTimer). |
| 3 | `v2/TrailerPlayerFragment.kt` | 226 | View (ExoPlayer + PlayerView) | Lecteur trailer YouTube in-app. Resout le stream via `YouTubeStreamResolver`, joue avec ExoPlayer Media3, gere les segments SponsorBlock (skip auto). Args : videoId, startSeconds, segmentsJson. |

### 1.2 ViewModel

| # | Fichier | Lignes | Role exact |
|---|---------|--------|------------|
| 4 | `v2/ItemDetailsViewModel.kt` | 677 | **ViewModel unique partage par les deux fragments**. Expose `uiState: StateFlow<ItemDetailsUiState>`. Charge l'item principal via `userLibraryApi.getItem()`, extrait cast/directors/writers/badges, puis charge les donnees additionnelles selon `item.type` : seasons, episodes, nextUp, similar (12 max), albums artiste (100 max), tracks album, playlist items, collection items, filmography (100 max). Supporte aussi : `loadChannelProgram()`, `loadSeriesTimer()`, `loadScheduleItems()`, `toggleRecord()`, `toggleRecordSeries()`, `cancelSeriesTimer()`, `toggleFavorite()`, `toggleWatched()`, `movePlaylistItem()`, `removeFromPlaylist()`. Multi-server via `ApiClientFactory.getApiClientForServer()`. |

**`ItemDetailsUiState`** contient : `isLoading`, `error`, `item`, `seasons`, `episodes`, `tracks`, `albums`, `similar`, `cast`, `nextUp`, `collectionItems`, `directors`, `writers`, `badges`, `programInfo`, `seriesTimerInfo`, `scheduleItems`, `isRecording`, `isRecordingSeries`.

**`MediaBadge`** : data class `(type: String, label: String)` — genere par `getMediaBadges()` depuis les MediaStreams (resolution, HDR, codec video, container, canaux audio, codec audio).

### 1.3 Ecran Compose principal

| # | Fichier | Lignes | Role exact |
|---|---------|--------|------------|
| 5 | `compose/ItemDetailScreen.kt` | 205 | **Dispatcher Compose**. Collecte `uiState`, affiche loading/error/content. Compose un `Box(fillMaxSize)` avec : Layer 1 = `DetailHeroBackdrop` (sauf PERSON/PLAYLIST), Layer 2 = dispatch `when(item.type)` vers le bon content composable. Definit aussi `DetailPlaybackCallbacks` (data class avec 9 callbacks : onPlay, onPlayFromHere, onPlaySingle, onPlayInstantMix, onQueueAudioItem, onPlayTrailers, onConfirmDelete, onAddToPlaylist, onNavigateToItem). |
| 6 | `compose/DetailHeroBackdrop.kt` | 120 | **Backdrop hero full-Compose**. `Crossfade(400ms)` sur l'URL backdrop. `AsyncImage` plein ecran, alpha 0.8, blur `RenderEffect` API 31+. Double gradient : vertical (transparent haut → background bas) + horizontal (background gauche → transparent droite). Utilise par `ItemDetailScreen` uniquement (pas par `ItemDetailsFragment`). |

### 1.4 Contenu par type de media (7 fichiers)

| # | Fichier | Lignes | Types geres | Affiche |
|---|---------|--------|-------------|---------|
| 7 | `v2/content/MovieDetailsContent.kt` | 294 | MOVIE, EPISODE, VIDEO, RECORDING, TRAILER, MUSIC_VIDEO, BOX_SET (fallback `else`) | Logo/titre, info row (annee, duree, "se termine a", rating), badges media, ratings communaute + Rotten Tomatoes, tagline italique, synopsis (4 lignes max), poster (165x248 ou 280x158 landscape), boutons action, metadata, episodes saison (si EPISODE), collection items grid (si BOX_SET), cast, similaires |
| 8 | `v2/content/SeriesDetailsContent.kt` | 242 | SERIES | Logo/titre, info row (annee, nb saisons, status continuing/ended, rating), ratings, tagline, synopsis, poster, boutons action, metadata, next up (LandscapeItemCard), saisons (SeasonCard 170x255), cast, similaires |
| 9 | `v2/content/SeasonDetailsContent.kt` | 241 | SEASON | Backdrop propre (DetailBackdrop), poster saison pleine hauteur (220dp), nom serie, nom saison (displayBold 55sp), nb episodes, 3 boutons inline (Play, Vu, Favori), liste verticale episodes (SeasonEpisodeItem : thumbnail 240x135 + synopsis 2 lignes) |
| 10 | `v2/content/PersonDetailsContent.kt` | 294 | PERSON | Slideshow backdrop anime (Crossfade, cycle 6s), photo portrait circulaire (180dp), nom, bio (bodyMedium 22sp lineHeight), filmographie (SimilarItemCard en LazyRow) — pas de `DetailHeroBackdrop`, gere son propre fond |
| 11 | `v2/content/MusicDetailsContent.kt` | 438 | MUSIC_ALBUM, MUSIC_ARTIST, PLAYLIST | Cover art (200x200 carre ou poster), nom artiste/album, info (annee, nb tracks, duree totale), boutons action, tracks (TrackItemCard avec reorder playlist via DPAD left/right), TrackActionDialog (play from here, play, instant mix, add to queue, remove from playlist), albums artiste, similaires, playlist hint reorder |
| 12 | `v2/content/LiveTvDetailsContent.kt` | 251 | PROGRAM (Live TV) | Titre programme, info (horaires debut/fin, duree, genre), synopsis, 3 boutons (Ecouter, Enregistrer, Enregistrer serie), cast si dispo |
| 13 | `v2/content/SeriesTimerDetailsContent.kt` | 170 | SeriesTimer (Live TV) | Nom timer, parametres (jours, chaine, pre/post padding, nb a garder), bouton Annuler, liste programmes planifies |

### 1.5 Composants partages

| # | Fichier | Lignes | Composants exposes |
|---|---------|--------|--------------------|
| 14 | `v2/ItemDetailsComponents.kt` | 1 458 | `DetailActionButton` (icone + label + detail, avec focus state), `MediaBadgeChip`, `InfoItemText`, `InfoItemBadge`, `RuntimeInfo`, `InfoItemSeparator`, `MetadataGroup` (row label/value avec separateurs), `CastCard` (circle 90dp + nom + role), `SeasonCard` (170x255dp poster + badge vu/non vu), `EpisodeCard` (220dp wide, thumbnail + progress bar), `SeasonEpisodeItem` (row horizontale 240x135dp thumbnail + details), `SectionHeader`, `PosterImage` (portrait 165x248 / landscape 280x158 / carre 200x200), `DetailBackdrop` (fallback blur Compose), `SimilarItemCard` (140x200dp poster + annee), `LandscapeItemCard` (220x124dp), `TrackItemCard` (row avec numero, titre, artiste, duree, reorder chevrons), `TrackSelectorDialog` (radio list), `TrackActionDialog` (action list), `ItemCardOverlays` (favori, watched, progress), `DetailsWatchIndicator`, `EpisodeWatchedBadge` |
| 15 | `v2/shared/DetailActions.kt` | 305 | `DetailActionCallbacks` (data class : trackSelector, hasPlayableTrailers, onPlay, onResume, onShuffle, onPlayTrailers, onPlayInstantMix, onToggleWatched, onToggleFavorite, onConfirmDelete, onAddToPlaylist, onGoToSeries, onLoadItem), `DetailActionButtonsRow` (Row horizontale centree avec tous les boutons conditionnels + 3 dialogs : audio, subtitle, version selector) |
| 16 | `v2/shared/DetailSections.kt` | 262 | `DetailMetadataSection` (genres, realisateur, scenaristes, studio dans MetadataGroup), `DetailSeasonsSection` (LazyRow de SeasonCard), `DetailEpisodesHorizontalSection` (LazyRow d'EpisodeCard), `DetailCastSection` (LazyRow de CastCard), `DetailSectionWithCards` (LazyRow generique : portrait SimilarItemCard ou landscape LandscapeItemCard), `DetailCollectionItemsGrid` (FlowRow de SimilarItemCard pour BOX_SET), `DetailPlaylistHint` |
| 17 | `v2/shared/DetailInfoRow.kt` | 116 | `DetailInfoRow` — row horizontale : annee, duree/endsAt (films) ou nb saisons + status badge (series), rating officiel badge, media badges chips |
| 18 | `v2/shared/DetailUtils.kt` | 64 | `getBackdropUrl()`, `getPosterUrl()`, `getLogoUrl()`, `getEpisodeThumbnailUrl()`, `formatDuration()`, `getEndsAt()` — helpers purs, pas de Compose |

### 1.6 Fichiers legacy

| # | Fichier | Lignes | Tech | Role exact |
|---|---------|--------|------|------------|
| 19 | `ItemListFragment.kt` | 543 | View (FragmentItemListBinding XML) | Fragment legacy pour affichage liste de tracks (album/playlist). Utilise `ItemListView`, `ItemRowView`, `ScrollView`, `PopupMenu`. Navigation : `Destinations.itemList()`. Encore reference mais probablement doublon avec `MusicDetailsContent`. |
| 20 | `ItemListFragmentHelper.kt` | 156 | Kotlin helpers | Helpers pour `ItemListFragment` — formatage, utilitaires. |
| 21 | `MusicFavoritesListFragment.kt` | 247 | View (FragmentItemListBinding XML) | Fragment legacy pour favoris musicaux. Similaire a `ItemListFragment` mais filtre sur isFavorite. Navigation : `Destinations.musicFavorites()`. |

### 1.7 Tests

| # | Fichier | Lignes | Couverture |
|---|---------|--------|------------|
| 22 | `ItemDetailsViewModelTests.kt` | 558 | Tests unitaires du ViewModel (mock API, verification des states loading/error/success, chargement additionnel par type) |

---

## 2. Navigation (Destinations.kt)

| Destination | Fragment | Utilisation |
|-------------|----------|-------------|
| `itemDetails(itemId, serverId?)` | `ItemDetailComposeFragment` | **Defaut** — films, series, episodes, personnes, musique |
| `itemDetailsV2(itemId, serverId?)` | `ItemDetailsFragment` | Legacy — jamais appele dans le code actuel sauf si explicitement demande |
| `channelDetails(itemId, channelId, programInfo)` | `ItemDetailComposeFragment` | Live TV — programme en cours |
| `seriesTimerDetails(itemId, seriesTimer)` | `ItemDetailComposeFragment` | Live TV — timer de serie |
| `itemList(itemId, serverId?)` | `ItemListFragment` | Legacy — liste de tracks (album/playlist) |
| `musicFavorites(parentId)` | `MusicFavoritesListFragment` | Legacy — favoris musicaux |
| `trailerPlayer(videoId, startSeconds, segmentsJson)` | `TrailerPlayerFragment` | Lecteur trailer YouTube in-app |

---

## 3. Duplication identifiee

### 3.1 Deux fragments detail (compose vs v2)

`ItemDetailComposeFragment` et `ItemDetailsFragment` sont **quasi-identiques** en logique :
- Memes injections Koin (viewModel, navigationRepository, playbackHelper, mediaManager, etc.)
- Meme `createActionCallbacks()` copie-colle
- Memes helpers play/handlePlay/handleResume/handleShuffle/playTrailers/confirmDeleteItem/deleteItem

**Differences** :
| Aspect | ItemDetailComposeFragment | ItemDetailsFragment |
|--------|--------------------------|---------------------|
| Backdrop | `DetailHeroBackdrop` (Compose, Crossfade) | ImageView + BitmapBlur/RenderEffect |
| Toolbar/Sidebar | Aucun | LeftSidebarNavigation / MainToolbar overlay |
| Live TV support | OUI (channelId, programInfoJson, seriesTimerJson) | NON |
| Focus management | Delegue a Compose | dispatchKeyEvent custom (sidebar/toolbar) |
| Lignes | 376 | 650 |

### 3.2 Legacy vs Compose (musique)

`ItemListFragment.kt` (543L) + `MusicFavoritesListFragment.kt` (247L) = **790 lignes** de code View legacy qui duplique `MusicDetailsContent.kt` (438L Compose). Les deux systemes coexistent.

---

## 4. Architecture visuelle par type

### Film / Episode / Video (MovieDetailsContent)
```
┌──────────────────────────────────────────────────┐
│ DetailHeroBackdrop (plein ecran, blur, gradients) │
│ ┌──────────────────────────────────────────────┐ │
│ │ LazyColumn                                    │ │
│ │  ├─ Spacer (60% ecran = push contenu en bas) │ │
│ │  ├─ Row: [Colonne info | PosterImage]         │ │
│ │  │    ├─ Logo ou Titre (headlineLargeBold)    │ │
│ │  │    ├─ DetailInfoRow (annee, duree, rating) │ │
│ │  │    ├─ MediaBadgeChips                      │ │
│ │  │    ├─ Ratings (communaute, RT)             │ │
│ │  │    ├─ Tagline (italique)                   │ │
│ │  │    └─ Synopsis (4 lignes max)              │ │
│ │  ├─ DetailActionButtonsRow (9 boutons)        │ │
│ │  ├─ DetailMetadataSection                     │ │
│ │  ├─ DetailEpisodesHorizontalSection (si EP)   │ │
│ │  ├─ DetailCollectionItemsGrid (si BOX_SET)    │ │
│ │  ├─ DetailCastSection                         │ │
│ │  └─ DetailSectionWithCards ("Plus comme ceci")│ │
│ └──────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

### Serie (SeriesDetailsContent)
```
Meme layout que Movie, plus :
  ├─ Next Up section (LandscapeItemCard)
  ├─ DetailSeasonsSection (LazyRow SeasonCard)
```

### Saison (SeasonDetailsContent)
```
┌──────────────────────────────────────────────────┐
│ DetailBackdrop (backdrop saison, blur configurable)│
│ ┌──────────────────────────────────────────────┐ │
│ │ Row: [Poster saison 220dp | Colonne info]     │ │
│ │       ├─ Nom serie                            │ │
│ │       ├─ Nom saison (displayBold)             │ │
│ │       ├─ "X episodes"                         │ │
│ │       └─ 3 boutons inline                     │ │
│ │ Liste verticale SeasonEpisodeItem             │ │
│ │   ├─ [Thumbnail 240x135 | Ep# + Titre + Desc]│ │
│ │   └─ ...                                      │ │
│ └──────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

### Personne (PersonDetailsContent)
```
┌──────────────────────────────────────────────────┐
│ Slideshow backdrop (Crossfade, cycle 6s)          │
│ ┌──────────────────────────────────────────────┐ │
│ │ Row: [Photo circulaire 180dp | Bio]           │ │
│ │ Filmographie (LazyRow SimilarItemCard)         │ │
│ └──────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

### Musique (MusicDetailsContent)
```
┌──────────────────────────────────────────────────┐
│ Slideshow backdrop (cover arts, Crossfade)         │
│ ┌──────────────────────────────────────────────┐ │
│ │ Row: [Cover 200x200 | Info album/artiste]     │ │
│ │ Boutons action                                │ │
│ │ Liste tracks (TrackItemCard, reorderable)      │ │
│ │ Albums artiste (si MUSIC_ARTIST)               │ │
│ │ Similaires                                    │ │
│ └──────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

---

## 5. Resume des constats

| Constat | Impact | Detail |
|---------|--------|--------|
| `ItemDetailsFragment` (v2) est un doublon maintenu pour la sidebar/toolbar | Code mort potentiel | 650L de code quasiment identique a `ItemDetailComposeFragment`. La seule valeur ajoutee est la sidebar/toolbar qui manque dans la version Compose. |
| Legacy musique encore en place | 790L de dette technique | `ItemListFragment` + `MusicFavoritesListFragment` font la meme chose que `MusicDetailsContent` en View XML. |
| Backdrop gere de 2 manieres differentes | Incoherence visuelle | Compose fragment → `DetailHeroBackdrop` (Crossfade). V2 fragment → ImageView + BitmapBlur. Person/Music → slideshow propre. Season → `DetailBackdrop` composable inline. |
| 1 458 lignes de composants UI dans un seul fichier | Maintenabilite | `ItemDetailsComponents.kt` contient 20+ composants. Candidat au split. |
| Pas de sidebar/toolbar dans le fragment Compose actif | UX incoherente | L'ecran de detail n'a pas la navigation globale (sidebar/topbar) que le home possede. |
| 7 893 lignes au total pour l'ecran detail | Complexite elevee | C'est le plus gros ecran de l'app apres le home. |
