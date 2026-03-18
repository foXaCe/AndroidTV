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
| 5 | Live TV | `VegafoXIcons.LiveTv` | `Destinations.liveTvGuide` (direct) | Toujours (no-op si absent) |
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

### Menu principal (refonte 2026-03-16 — 10 sections plates)

| # | Section | Options |
|---|---------|---------|
| 1 | **Apparence** | Theme focus, Fonds, Flou details, Flou navigation, Indicateur vu, Horloge |
| 2 | **Accueil** | Taille affiches, Type image rangees, Sections Home, Fusionner Continue/Next Up |
| 3 | **Navigation** | Shuffle (fusionne), Genres, Favoris, Libraries toolbar, Vue dossiers, Confirmer sortie |
| 4 | **Lecture** | Lecteur video, Episode suivant, Sous-titres, Sous-titres off, Segments, Cinema mode, Inactivite, Pause description, Skip forward, Unpause rewind, Resume preroll |
| 5 | **Video & Audio** | Bitrate max, Resolution max, Zoom, Refresh rate, Delai demarrage, Audio output, Night mode, AC3, Live TV direct, PGS, ASS |
| 6 | **Ecran de veille** | Activer, Delai, Mode, Assombrissement, Classification, Horloge veille |
| 7 | **Jellyseerr** | → `/jellyseerr` (configuration, rangees, sync) |
| 8 | **SyncPlay** | → `/vegafox/syncplay` |
| 9 | **Comptes** | → `/authentication`, Controles parentaux, Librairies |
| 10 | **A propos & Avance** | Version, MAJ, TrickPlay, Debug, FFmpeg, Cache, Reset |

Options supprimees :
- ~~Section Plugin VegafoX~~ (contenu distribue dans les 10 sections)
- ~~cardFocusExpansion~~ (preference jamais lue — supprimee)
- ~~Telemetrie~~ (VegafoX ne collecte pas de donnees)
- ~~Support VegafoX / DonateDialog~~ (vestige Jellyfin)

### Authentification (`/authentication`)

| Label | Route |
|-------|-------|
| Connexion automatique | `/authentication/auto-sign-in` |
| Tri des comptes | `/authentication/sort-by` |
| [Serveurs dynamiques] | `/authentication/server/{serverId}` |
| Code PIN | `/authentication/pin-code` |

### Sous-ecrans de navigation (inchanges, accessibles depuis le menu principal flat)

| Route | Ecran | Accede depuis |
|-------|-------|---------------|
| `/customization/theme` | Focus Color | Apparence |
| `/customization/clock` | Horloge | Apparence |
| `/customization/watch-indicators` | Indicateur vu | Apparence |
| `/vegafox/details-blur` | Flou details | Apparence |
| `/vegafox/browsing-blur` | Flou navigation | Apparence |
| `/home/poster-size` | Taille affiches | Accueil |
| `/home/rows-image-type` | Type image rangees | Accueil |
| `/home` | Sections Home | Accueil |
| `/vegafox/shuffle-content-type` | Type contenu shuffle | Navigation |
| `/playback/player` | Lecteur video | Lecture |
| `/playback/next-up` | Episode suivant | Lecture |
| `/customization/subtitles` | Sous-titres | Lecture |
| `/playback/media-segments` | Segments media | Lecture |
| `/playback/inactivity-prompt` | Inactivite | Lecture |
| `/playback/resume-subtract-duration` | Resume preroll | Lecture |
| `/playback/max-bitrate` | Bitrate max | Video & Audio |
| `/playback/max-resolution` | Resolution max | Video & Audio |
| `/playback/zoom-mode` | Mode zoom | Video & Audio |
| `/playback/refresh-rate-switching-behavior` | Refresh rate | Video & Audio |
| `/playback/audio-behavior` | Sortie audio | Video & Audio |
| `/jellyseerr` | Jellyseerr | Jellyseerr |
| `/vegafox/syncplay` | SyncPlay | SyncPlay |
| `/authentication` | Comptes | Comptes |
| `/vegafox/parental-controls` | Controles parentaux | Comptes |
| `/libraries` | Librairies | Comptes |
| `/about` | A propos | A propos & Avance |
| `/licenses` | Licences | A propos & Avance |

### Ecrans legacy (routes conservees mais plus navigues depuis le menu principal)

| Route | Ecran | Note |
|-------|-------|------|
| `/customization` | SettingsCustomizationScreen | Contenu distribue dans Apparence + Navigation + Accueil |
| `/playback` | SettingsPlaybackScreen | Contenu distribue dans Lecture + Video & Audio |
| `/playback/advanced` | SettingsPlaybackAdvancedScreen | Contenu distribue dans Lecture + Video & Audio |
| `/plugin` | SettingsPluginScreen | Contenu distribue dans toutes les sections |

---

## 4. Anomalies

### Destinations registrees mais absentes des menus

| Destination | Fragment | Observation |
|-------------|----------|-------------|
| `nowPlaying` | `AudioNowPlayingComposeFragment` | Pas dans la sidebar ni settings — acces uniquement via lecture audio en cours |
| `liveTvGuide` | `LiveTvGuideFragment` | Destination directe depuis la sidebar — boutons dans le header vers Recordings/Schedule/Series |
| `liveTvSchedule` | `ScheduleBrowseFragment` | Acces depuis le guide (bouton header) ou depuis Recordings |
| `liveTvRecordings` | `RecordingsBrowseFragment` | Acces depuis le guide (bouton header) |
| `liveTvSeriesRecordings` | `SeriesRecordingsBrowseFragment` | Acces depuis le guide (bouton header) ou depuis Recordings |
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

_Aucune anomalie — toutes les routes ont un ecran confirme._

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
