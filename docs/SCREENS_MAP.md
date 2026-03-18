# VegafoX Android TV — Cartographie des Ecrans

> Generee a partir des audits du dossier `docs/audits/`.
> Derniere mise a jour : 2026-03-14

---

## Derniere verification

- **Date** : 2026-03-14
- **Methode** : Lecture directe du code source Kotlin de chaque ecran
- **Statuts corriges** : 47 sur 97 ecrans (48.5%)
- **Tendance** : Migration massive des Settings (30 ecrans, presque tous fully VegafoX), Live TV composables migres, 5 fragments Startup legacy supprimes du code, 4 Jellyseerr migres vers Compose
- **Fichiers supprimes** : 5 fichiers n'existent plus (`ServerFragment`, `UserLoginFragment`, `UserLoginCredentialsFragment`, `UserLoginQuickConnectFragment`, `SelectServerFragment`)

---

## Legende

| Statut | Description |
|--------|-------------|
| **fully VegafoX** | 100% Compose TV + design system VegafoX |
| **partial** | Compose present mais design ou architecture mixte (Leanback/View) |
| **legacy** | Leanback, XML Views ou Java non migre |
| **inconnu** | Fichier absent ou introuvable dans le code source |

| Priorite | Description |
|----------|-------------|
| **P0** | Chemin critique — ecrans vus par 100% des utilisateurs |
| **P1** | Important — ecrans frequemment utilises |
| **P2** | Souhaitable — ecrans secondaires ou de polish |
| **P3** | Futur — faible priorite, refactoring long terme |

---

## 1. Home

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 1 | Home Screen (principal) | `ui/home/compose/HomeScreen.kt` | fully VegafoX | P0 |
| 2 | Hero Backdrop (carrousel hero) | `ui/home/compose/HomeHeroBackdrop.kt` | fully VegafoX | P0 |
| 3 | Rows (lignes de contenu) | `ui/base/tv/TvRowList.kt` | fully VegafoX | P0 |
| 4 | Sidebar Navigation | `ui/home/compose/sidebar/PremiumSideBar.kt` | fully VegafoX | P0 |
| 5 | Main Toolbar | `ui/shared/toolbar/MainToolbar.kt` | fully VegafoX | P0 |
| 6 | Trailer Preview (YouTube) | `ui/home/mediabar/ExoPlayerTrailerView.kt` | partial | P2 |

## 2. Detail

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 8 | Detail Screen (routeur) | `ui/itemdetail/compose/ItemDetailScreen.kt` | fully VegafoX | P1 |
| 9 | Detail Film | `ui/itemdetail/v2/content/MovieDetailsContent.kt` | fully VegafoX | P1 |
| 10 | Detail Serie | `ui/itemdetail/v2/content/SeriesDetailsContent.kt` | fully VegafoX | P1 |
| 11 | Detail Saison | `ui/itemdetail/v2/content/SeasonDetailsContent.kt` | fully VegafoX | P1 |
| 12 | Detail Episode | `ui/itemdetail/v2/content/MovieDetailsContent.kt` | fully VegafoX | P1 |
| 13 | Detail Personne | `ui/itemdetail/v2/content/PersonDetailsContent.kt` | fully VegafoX | P1 |
| 14 | Detail Musique (album/artiste) | `ui/itemdetail/v2/content/MusicDetailsContent.kt` | fully VegafoX | P1 |
| 15 | Detail Live TV (programme) | `ui/itemdetail/v2/content/LiveTvDetailsContent.kt` | fully VegafoX | P1 |
| 16 | Detail Series Timer | `ui/itemdetail/v2/content/SeriesTimerDetailsContent.kt` | fully VegafoX | P1 |
| 17 | Actions (boutons Lecture/Favori/etc.) | `ui/itemdetail/v2/shared/DetailActions.kt` | fully VegafoX | P1 |
| 18 | Sections metadata | `ui/itemdetail/v2/shared/DetailSections.kt` | fully VegafoX | P1 |

## 3. Player

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 19 | Video Player Screen | `ui/player/video/VideoPlayerScreen.kt` | fully VegafoX | P0 |
| 20 | Player Controls (seekbar + boutons) | `ui/player/video/VideoPlayerControls.kt` | fully VegafoX | P0 |
| 21 | Player Overlay (layout complet) | `ui/player/video/VideoPlayerOverlay.kt` | fully VegafoX | P0 |
| 22 | Player Header (titre + horloge) | `ui/player/video/VideoPlayerHeader.kt` | fully VegafoX | P0 |
| 23 | Player Seekbar | `ui/player/base/PlayerSeekbar.kt` | fully VegafoX | P0 |
| 24 | Player Dialogs (Audio/Sous-titres/Vitesse/Qualite/Zoom) | `ui/player/video/VideoPlayerDialogs.kt` | fully VegafoX | P1 |
| 25 | Skip Overlay (Intro/Recap/Pub/Episode) | `ui/playback/overlay/SkipOverlayView.kt` | fully VegafoX | P1 |
| 26 | Next Up (episode suivant) | `ui/playback/nextup/NextUpFragment.kt` | fully VegafoX | P1 |
| 27 | Still Watching (etes-vous la ?) | `ui/playback/stillwatching/StillWatchingFragment.kt` | fully VegafoX | P1 |
| 28 | Media Toast | `ui/player/base/toast/MediaToast.kt` | fully VegafoX | P1 |
| 29 | Photo Player | `ui/player/photo/PhotoPlayerFragment.kt` | fully VegafoX | P3 |

## 4. Browse

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 30 | Browse Media Card | `ui/browsing/compose/BrowseMediaCard.kt` | fully VegafoX | P0 |
| 31 | Library Browse (films/series) | `ui/browsing/compose/LibraryBrowseScreen.kt` | fully VegafoX | P1 |
| 32 | Folder Browse | `ui/browsing/compose/FolderBrowseScreen.kt` | fully VegafoX | P1 |
| 33 | Genres Grid | `ui/browsing/compose/GenresGridScreen.kt` | fully VegafoX | P1 |
| 34 | By Letter Browse | `ui/browsing/compose/ByLetterBrowseScreen.kt` | fully VegafoX | P1 |
| 35 | Collection Browse | `ui/browsing/compose/CollectionBrowseFragment.kt` | fully VegafoX | P1 |
| 36 | Suggested Movies | `ui/browsing/compose/SuggestedMoviesFragment.kt` | fully VegafoX | P2 |
| 37 | Music Browse | `ui/browsing/v2/MusicBrowseFragment.kt` | fully VegafoX | P1 |
| 38 | Audio Now Playing | `ui/playback/audio/AudioNowPlayingScreen.kt` | fully VegafoX | P1 |

## 5. Search

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 39 | Search Screen | `ui/search/compose/SearchScreen.kt` | fully VegafoX | P1 |

## 6. Settings

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 40 | Settings Main | `ui/settings/screen/SettingsMainScreen.kt` | fully VegafoX | P1 |
| 41 | Settings Dialog (overlay) | `ui/settings/composable/SettingsDialog.kt` | fully VegafoX | P1 |
| 42 | Settings Layout (cadre) | `ui/settings/composable/SettingsLayout.kt` | fully VegafoX | P1 |
| 43 | Settings Playback | `ui/settings/screen/playback/SettingsPlaybackScreen.kt` | fully VegafoX | P1 |
| 44 | Settings Playback Advanced | `ui/settings/screen/playback/SettingsPlaybackAdvancedScreen.kt` | fully VegafoX | P2 |
| 45 | Settings Home | `ui/settings/screen/home/SettingsHomeScreen.kt` | fully VegafoX | P1 |
| 46 | Settings Home Section | `ui/settings/screen/home/SettingsHomeSectionScreen.kt` | fully VegafoX | P2 |
| 47 | Settings Home Poster Size | `ui/settings/screen/home/SettingsHomePosterSizeScreen.kt` | fully VegafoX | P2 |
| 48 | Settings Home Rows Image | `ui/settings/screen/vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | fully VegafoX | P2 |
| 49 | Settings Customization | `ui/settings/screen/customization/SettingsCustomizationScreen.kt` | fully VegafoX | P1 |
| 50 | Settings Theme | `ui/settings/screen/customization/SettingsCustomizationThemeScreen.kt` | fully VegafoX | P2 |
| 51 | Settings Clock | `ui/settings/screen/customization/SettingsCustomizationClockScreen.kt` | fully VegafoX | P2 |
| 52 | Settings Clock Behavior | `ui/settings/screen/customization/SettingsClockBehaviorScreen.kt` | fully VegafoX | P3 |
| 53 | Settings Watched Indicator | `ui/settings/screen/customization/SettingsCustomizationWatchedIndicatorScreen.kt` | fully VegafoX | P3 |
| 54 | Settings Subtitles | `ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt` | fully VegafoX | P2 |
| 55 | Settings Plugin (VegafoX) | `ui/settings/screen/vegafox/SettingsPluginScreen.kt` | fully VegafoX | P1 |
| 56 | Settings Jellyseerr | `ui/settings/screen/vegafox/SettingsJellyseerrScreen.kt` | fully VegafoX | P1 |
| 57 | Settings Jellyseerr Rows | `ui/settings/screen/vegafox/SettingsJellyseerrRowsScreen.kt` | fully VegafoX | P2 |
| 58 | Settings Parental Controls | `ui/settings/screen/vegafox/SettingsVegafoXParentalControlsScreen.kt` | fully VegafoX | P3 |
| 59 | Settings SyncPlay | `ui/settings/screen/vegafox/SettingsVegafoXSyncPlayScreen.kt` | fully VegafoX | P2 |
| 60 | Settings Authentication | `ui/settings/screen/authentication/SettingsAuthenticationScreen.kt` | fully VegafoX | P2 |
| 61 | Settings Libraries | `ui/settings/screen/library/SettingsLibrariesScreen.kt` | fully VegafoX | P2 |
| 62 | Settings Libraries Display | `ui/settings/screen/library/SettingsLibrariesDisplayScreen.kt` | fully VegafoX | P2 |
| 63 | Settings Live TV Guide Filters | `ui/settings/screen/livetv/SettingsLiveTvGuideFiltersScreen.kt` | fully VegafoX | P2 |
| 64 | Settings Screensaver | `ui/settings/screen/screensaver/SettingsScreensaverScreen.kt` | fully VegafoX | P2 |
| 65 | Settings About | `ui/settings/screen/about/SettingsAboutScreen.kt` | fully VegafoX | P2 |
| 66 | Settings Telemetry | `ui/settings/screen/SettingsTelemetryScreen.kt` | fully VegafoX | P3 |
| 67 | Settings Developer | `ui/settings/screen/SettingsDeveloperScreen.kt` | fully VegafoX | P3 |
| 68 | Settings Licenses | `ui/settings/screen/license/SettingsLicensesScreen.kt` | fully VegafoX | P3 |
| 69 | Settings License Detail | `ui/settings/screen/license/SettingsLicenseScreen.kt` | fully VegafoX | P3 |

## 7. Live TV

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 70 | Live TV Guide Screen | `ui/livetv/compose/LiveTvGuideScreen.kt` | fully VegafoX | P1 |
| 71 | Guide Grid | `ui/livetv/compose/LiveTvGuideGrid.kt` | fully VegafoX | P1 |
| 72 | Guide Timeline | `ui/livetv/compose/GuideTimeline.kt` | fully VegafoX | P2 |
| 73 | Channel Header | `ui/livetv/compose/ChannelHeaderComposable.kt` | fully VegafoX | P1 |
| 74 | Program Cell | `ui/livetv/compose/ProgramCellComposable.kt` | fully VegafoX | P1 |
| 75 | Program Detail Dialog | `ui/livetv/compose/ProgramDetailDialog.kt` | fully VegafoX | P1 |
| 76 | Record Dialog | `ui/livetv/compose/RecordDialog.kt` | fully VegafoX | P1 |
| 77 | Live TV Browse | `ui/browsing/v2/LiveTvBrowseFragment.kt` | fully VegafoX | P1 |
| 78 | Recordings Browse | `ui/browsing/v2/RecordingsBrowseFragment.kt` | fully VegafoX | P1 |
| 79 | Schedule Browse | `ui/browsing/v2/ScheduleBrowseFragment.kt` | fully VegafoX | P1 |
| 80 | Series Recordings Browse | `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt` | fully VegafoX | P1 |

## 8. Startup

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 81 | Welcome Screen (splash anime) | `ui/startup/compose/WelcomeScreen.kt` | fully VegafoX | P0 |
| 82 | Server Discovery Screen | `ui/startup/server/ServerDiscoveryScreen.kt` | fully VegafoX | P0 |
| 82b | Server Add Screen | `ui/startup/server/ServerAddScreen.kt` | fully VegafoX | P0 |
| 83 | Quick Connect Screen | `ui/startup/server/QuickConnectScreen.kt` | fully VegafoX | P0 |
| 84 | User Selection (grille profils) | `ui/startup/user/UserSelectionScreen.kt` | fully VegafoX | P1 |
| 85 | User Login (identifiants) | `ui/startup/user/UserLoginScreen.kt` | fully VegafoX | P1 |
| 86 | Pin Entry (startup) | `ui/startup/user/PinEntryScreen.kt` | fully VegafoX | P1 |
| 87 | Pin Entry Dialog (settings auth) | `ui/startup/PinEntryDialog.kt` | fully VegafoX | P1 |
| 88 | Splash Screen | `ui/startup/fragment/SplashFragment.kt` | fully VegafoX | P1 |

## 9. Jellyseerr

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 91 | Discover (accueil Jellyseerr) | `ui/jellyseerr/DiscoverFragment.kt` | fully VegafoX | P2 |
| 92 | Discover Rows | `ui/jellyseerr/JellyseerrDiscoverRowsFragment.kt` | fully VegafoX | P2 |
| 93 | Browse By (par genre/studio) | `ui/jellyseerr/JellyseerrBrowseByFragment.kt` | fully VegafoX | P2 |
| 94 | Media Details | `ui/jellyseerr/MediaDetailsFragment.kt` | fully VegafoX | P2 |
| 95 | Person Details | `ui/jellyseerr/PersonDetailsFragment.kt` | fully VegafoX | P2 |
| 96 | Requests | `ui/jellyseerr/RequestsFragment.kt` | fully VegafoX | P2 |

## 10. Autres

| # | Ecran | Fichier Kotlin principal | Statut | Priorite |
|---|-------|--------------------------|--------|----------|
| 97 | Create Playlist Dialog | `ui/playlist/CreatePlaylistDialogFragment.kt` | fully VegafoX | P3 |

---

## Synthese

### Par statut

| Statut | Nombre | % | Evolution |
|--------|--------|---|-----------|
| fully VegafoX | 92 | 98.9% | +72 |
| partial | 1 | 1.1% | -41 |
| legacy | 0 | 0.0% | -34 |
| inconnu | 0 | 0.0% | -1 |
| **Total** | **93** | **100%** | |

### Par priorite

| Priorite | Nombre | % |
|----------|--------|---|
| P0 (critique) | 13 | 14.0% |
| P1 (important) | 39 | 41.9% |
| P2 (souhaitable) | 28 | 30.1% |
| P3 (futur) | 13 | 14.0% |
| **Total** | **93** | **100%** |

### Par categorie

| Categorie | Total | fully VegafoX | partial | legacy | inconnu |
|-----------|-------|---------------|---------|--------|---------|
| Home | 6 | 5 | 1 (Trailer) | 0 | 0 |
| Detail | 11 | 11 | 0 | 0 | 0 |
| Player | 11 | 11 | 0 | 0 | 0 |
| Browse | 9 | 9 | 0 | 0 | 0 |
| Search | 1 | 1 | 0 | 0 | 0 |
| Settings | 30 | 30 | 0 | 0 | 0 |
| Live TV | 11 | 11 | 0 | 0 | 0 |
| Startup | 8 | 8 | 0 | 0 | 0 |
| Jellyseerr | 6 | 6 | 0 | 0 | 0 |
| Autres | 1 | 1 | 0 | 0 | 0 |
| **Total** | **93** | **92** | **1** | **0** | **0** |

### Prochaines priorites de migration

Migration terminee. Tous les ecrans legacy ont ete reecrits en Compose VegafoX.

Ecrans restants non-fully-VegafoX :
1. **Home partial (P2 x1)** — ExoPlayerTrailerView utilise un TextureView natif pour la transparence video (contrainte technique justifiee, pas de migration possible)
