# VegafoX Android TV — Cartographie de la Navigation

> Generee par lecture directe du code source.
> Derniere mise a jour : 2026-03-14

---

## 1. Sidebar (PremiumSideBar)

Navigation principale via la barre laterale gauche (72dp repliee, 220dp depliee).

| # | Label | Icone | Destination | Visibilite |
|---|-------|-------|-------------|------------|
| 0 | Accueil | `VegafoXIcons.Home` | `Destinations.home` | Toujours |
| 1 | Recherche | `VegafoXIcons.Search` | `Destinations.search()` | Toujours |
| 2 | Films | `VegafoXIcons.Movie` | Premiere librairie MOVIES | Toujours (no-op si absent) |
| 3 | Series | `VegafoXIcons.Tv` | Premiere librairie TVSHOWS | Toujours (no-op si absent) |
| 4 | Musique | `VegafoXIcons.MusicLibrary` | Premiere librairie MUSIC | Toujours (no-op si absent) |
| 5 | Live TV | `VegafoXIcons.LiveTv` | Premiere librairie LIVETV | Toujours (no-op si absent) |
| 6 | Genres | `VegafoXIcons.Genres` | `Destinations.allGenres` | Toujours |
| 7 | Favoris | `VegafoXIcons.Favorite` | `Destinations.allFavorites` | Toujours |
| 8 | Parametres | `VegafoXIcons.Settings` | `settingsViewModel.show()` | Toujours |
| — | Profil utilisateur | Avatar/initiale | Deconnexion → `ActivityDestinations.startup()` | Toujours (bas de sidebar) |

### Sidebar etendue (LeftSidebarNavigation)

Items supplementaires presents dans la variante etendue :

| # | Label | Icone | Destination | Condition |
|---|-------|-------|-------------|-----------|
| + | Shuffle | `VegafoXIcons.Shuffle` | `shuffleManager.quickShuffle()` | `showShuffleButton` && `shuffleContentType != "disabled"` |
| + | Jellyseerr | `ic_jellyseerr_jellyfish` | `Destinations.jellyseerrDiscover` | `jellyseerrEnabled` |
| + | Dossiers | `VegafoXIcons.Folder` | `Destinations.folderView` | `enableFolderView` |
| + | SyncPlay | `VegafoXIcons.SyncPlay` | `syncPlayViewModel.show()` (dialog) | `syncPlayEnabled` |
| + | Librairies | `VegafoXIcons.Clapperboard` | Liste dynamique des librairies utilisateur | `showLibrariesInToolbar` |

---

## 2. Graphe de navigation (Destinations.kt)

Architecture : navigation Fragment programmatique (pas de NavGraph XML). Toutes les destinations sont centralisees dans l'objet `Destinations`.

### Destinations top-level (sans arguments)

| Destination | Fragment | Description |
|-------------|----------|-------------|
| `home` | `HomeComposeFragment` | Ecran d'accueil |
| `allGenres` | `GenresGridComposeFragment` | Grille de tous les genres |
| `allFavorites` | `AllFavoritesComposeFragment` | Tous les favoris |
| `folderView` | `FolderViewComposeFragment` | Vue par dossiers |
| `nowPlaying` | `AudioNowPlayingComposeFragment` | Lecteur audio en cours |
| `liveTvGuide` | `LiveTvGuideFragment` | Guide TV en direct |
| `liveTvSchedule` | `ScheduleBrowseFragment` | Programmes planifies |
| `liveTvRecordings` | `RecordingsBrowseFragment` | Enregistrements |
| `liveTvSeriesRecordings` | `SeriesRecordingsBrowseFragment` | Enregistrements de series |
| `jellyseerrDiscover` | `DiscoverFragment` | Decouverte Jellyseerr |

### Destinations avec arguments

| Destination | Fragment | Arguments |
|-------------|----------|-----------|
| `search()` | `SearchComposeFragment` | `query: String?` |
| `libraryBrowser()` | `LibraryBrowseComposeFragment` | `folderJson`, `serverId?`, `userId?`, `includeType?` |
| `liveTvBrowser()` | `LiveTvBrowseFragment` | `folderJson` |
| `musicBrowser()` | `MusicBrowseFragment` | `folderJson`, `serverId?`, `userId?` |
| `folderBrowser()` | `FolderBrowseComposeFragment` | `folderJson`, `serverId?`, `userId?` |
| `genreBrowse()` | `LibraryBrowseComposeFragment` | `genreName`, `parentId?`, `includeType?`, `serverId?` |
| `libraryByGenres()` | `GenresGridComposeFragment` | `folderJson`, `includeType` |
| `itemDetails()` | `ItemDetailsFragment` | `itemId: UUID`, `serverId?` |
| `channelDetails()` | `ItemDetailsFragment` | `itemId`, `channelId`, `programInfoJson` |
| `seriesTimerDetails()` | `ItemDetailsFragment` | `itemId`, `seriesTimerJson` |
| `itemList()` | `ItemListFragment` | `itemId`, `serverId?` |
| `videoPlayer()` | `VideoPlayerFragment` | `position?` |
| `photoPlayer()` | `PhotoPlayerFragment` | `itemId`, `autoPlay`, `albumSortBy?`, `albumSortOrder?` |
| `trailerPlayer()` | `TrailerPlayerFragment` | `videoId`, `startSeconds`, `segmentsJson` |
| `jellyseerrBrowseBy()` | `JellyseerrBrowseByFragment` | `filterId`, `filterName`, `mediaType`, `filterType` |
| `jellyseerrMediaDetails()` | `MediaDetailsFragment` | `itemJson` |
| `jellyseerrPersonDetails()` | `PersonDetailsFragment` | `personId` |

### Destinations Activity (Intent)

| Destination | Activity | Arguments |
|-------------|----------|-----------|
| `externalPlayer()` | `ExternalPlayerActivity` | `position: Duration` |
| `startup()` | `StartupActivity` | `hideSplash: Boolean` |

---

## 3. Settings (routes internes)

L'ecran Settings utilise un systeme de routage interne (`Routes` + `SettingsRouterContent`).

### Menu principal

| # | Section | Label | Icone | Route |
|---|---------|-------|-------|-------|
| 1 | Apparence | Personnalisation | `Tune` | `/customization` |
| 2 | Apparence | Ecran de veille | `PhotoLibrary` | `/customization/screensaver` |
| 3 | Lecture | Lecture | `SkipNext` | `/playback` |
| 4 | Extensions | Plugin VegafoX | `Tune` | `/plugin` |
| 5 | Compte | Authentification | `Group` | `/authentication` |
| 6 | Informations | Mises a jour | `Download` | Dialog (conditionnel OTA) |
| 7 | Informations | Developpeur | `Science` | `/developer` |
| 8 | Informations | A propos | Jellyfin icon | `/about` |

Options supprimees :
- ~~Telemetrie~~ (VegafoX ne collecte pas de donnees)
- ~~Support VegafoX / DonateDialog~~ (vestige Jellyfin)
- ~~Sous-titres dans Lecture~~ (ecran introuvable)

### Authentification (`/authentication`)

| Label | Route |
|-------|-------|
| Connexion automatique | `/authentication/auto-sign-in` |
| Tri des comptes | `/authentication/sort-by` |
| [Serveurs dynamiques] | `/authentication/server/{serverId}` |
| Code PIN | `/authentication/pin-code` |

### Personnalisation (`/customization`)

| Section | Label | Route |
|---------|-------|-------|
| Navigation | Librairies | `/libraries` |
| Navigation | Accueil | `/home` |
| Navigation | Theme de focus | `/customization/theme` |
| Navigation | Horloge | `/customization/clock` |
| Navigation | Indicateur vu | `/customization/watch-indicators` |
| Toolbar | Position navbar | `/vegafox/navbar-position` |
| Toolbar | Contenu shuffle | `/vegafox/shuffle-content-type` |
| Apparence | Flou details | `/vegafox/details-blur` |
| Apparence | Flou navigation | `/vegafox/browsing-blur` |

### Lecture (`/playback`)

| Label | Route |
|-------|-------|
| Lecteur video | `/playback/player` |
| Episode suivant | `/playback/next-up` |
| Inactivite | `/playback/inactivity-prompt` |
| Pre-rolls | `/playback/prerolls` |
| Sous-titres | `/customization/subtitles` |
| Segments media | `/playback/media-segments` |
| SyncPlay | `/vegafox/syncplay` |
| Avance | `/playback/advanced` |

### Plugin VegafoX (`/plugin`)

| Label | Route |
|-------|-------|
| Jellyseerr | `/jellyseerr` |
| Rangees Jellyseerr | `/jellyseerr/rows` |
| Controles parentaux | `/vegafox/parental-controls` |
| Media Bar (type, nombre, opacite, couleur) | `/vegafox/media-bar-*` |
| Volume musique de theme | `/vegafox/theme-music-volume` |
| Rangees Home (type image) | `/home/rows-image-type` |

---

## 4. Anomalies

### Destinations registrees mais absentes des menus

| Destination | Fragment | Observation |
|-------------|----------|-------------|
| `nowPlaying` | `AudioNowPlayingComposeFragment` | Pas dans la sidebar ni settings — acces uniquement via lecture audio en cours |
| `liveTvGuide` | `LiveTvGuideFragment` | Pas dans la sidebar — acces via `liveTvBrowser()` uniquement |
| `liveTvSchedule` | `ScheduleBrowseFragment` | Acces uniquement depuis l'ecran Live TV Browse |
| `liveTvRecordings` | `RecordingsBrowseFragment` | Acces uniquement depuis l'ecran Live TV Browse |
| `liveTvSeriesRecordings` | `SeriesRecordingsBrowseFragment` | Acces uniquement depuis l'ecran Live TV Browse |
| `trailerPlayer()` | `TrailerPlayerFragment` | Acces uniquement depuis les details d'un item |
| `itemList()` | `ItemListFragment` | Acces uniquement depuis un lien interne (playlists, music) |
| `externalPlayer()` | `ExternalPlayerActivity` | Acces uniquement depuis le player interne (fallback) |

### Destinations conditionnelles

| Destination | Condition | Observation |
|-------------|-----------|-------------|
| `jellyseerrDiscover` | `jellyseerrEnabled` | N'apparait que si Jellyseerr est configure dans les settings |
| `folderView` | `enableFolderView` | N'apparait que si active dans Personnalisation |
| Shuffle | `showShuffleButton` + `contentType != disabled` | Sidebar etendue uniquement |
| SyncPlay | `syncPlayEnabled` | Sidebar etendue uniquement |
| Librairies dans sidebar | `showLibrariesInToolbar` | Sidebar etendue uniquement |

### Route Settings sans ecran confirme

| Route | Observation |
|-------|-------------|
| `/customization/subtitles` | Pointe vers `SettingsSubtitlesScreen` — fichier marque "inconnu" dans SCREENS_MAP (introuvable) |

### Coherence sidebar/toolbar

| Fonctionnalite | PremiumSideBar | LeftSidebarNavigation | MainToolbar |
|----------------|----------------|----------------------|-------------|
| Home | Oui | Oui | Oui |
| Recherche | Oui | Oui | Non |
| Films | Oui | Non (via Librairies) | Oui |
| Series | Oui | Non (via Librairies) | Oui |
| Musique | Oui | Non (via Librairies) | Oui |
| Live TV | Oui | Non (via Librairies) | Oui |
| Genres | Oui | Oui (conditionnel) | Non |
| Favoris | Oui | Oui (conditionnel) | Non |
| Jellyseerr | Non | Oui (conditionnel) | Non |
| Shuffle | Non | Oui (conditionnel) | Non |
| Dossiers | Non | Oui (conditionnel) | Non |
| SyncPlay | Non | Oui (conditionnel) | Non |
| Parametres | Oui | Oui | Oui |
| Profil/Logout | Oui | Oui | Non |

---

## Synthese

| Categorie | Nombre |
|-----------|--------|
| Destinations Fragment (top-level) | 10 |
| Destinations Fragment (avec arguments) | 17 |
| Destinations Activity | 2 |
| **Total destinations navigation** | **29** |
| Routes Settings (total) | 55 |
| Items sidebar (PremiumSideBar) | 9 + profil |
| Items sidebar etendue | 14 + profil |
| Items Settings principal | 10 |
