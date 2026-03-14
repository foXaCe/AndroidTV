# Audit complet des ecrans VegafoX Android TV

**Date** : 2026-03-11
**Branche** : main
**Methode** : Analyse exhaustive des Fragments, Compose screens, Destinations, Routes settings

---

## Architecture de navigation

- **Pas de navigation XML** : systeme 100% programmatique
- **3 Activities** : StartupActivity (auth), MainActivity (app principale), ExternalPlayerActivity (lecteur externe)
- **Destinations.kt** : ~35 destinations pour l'app principale (MainActivity)
- **Routes settings** : ~74 routes dans un NavHost Compose interne
- **StartupActivity** : gere le flux d'authentification avant MainActivity

### Flux startup — Entremelage ancien/nouveau

```
StartupActivity
├─ Session existe → SplashFragment → MainActivity
├─ Dernier serveur connu → UserSelectionFragment (NOUVEAU Compose)
│   ├─ Utilisateur clic → authenticateUser → ok → MainActivity
│   ├─ "Ajouter compte" → UserLoginFragment (NOUVEAU user/)
│   └─ "Changer serveur" → SelectServerFragment (ANCIEN XML!) ⚠️
│       └─ Serveur selectionne → ServerFragment (ANCIEN XML!) ⚠️
│           └─ Login → UserLoginFragment (ANCIEN fragment/) ⚠️
└─ Aucun serveur → WelcomeFragment (NOUVEAU Compose)
    └─ ServerDiscoveryFragment (NOUVEAU Compose)
        └─ QuickConnectFragment (NOUVEAU Compose)
```

> **PROBLEME** : quand l'utilisateur fait "Changer de serveur" depuis
> l'ecran profil VegafoX (UserSelectionScreen), il tombe dans l'ancien
> design Jellyfin (SelectServerFragment → ServerFragment). Les deux
> systemes UI sont entremeles dans le meme flux.

---

## TABLEAU COMPLET DES ECRANS

### Legende

| Code | Signification |
|------|---------------|
| **A** | Nouveau design VegafoX applique (VegafoXColors, VegafoXButton, VegafoXGradients) |
| **B** | Design Jellyfin original non touche (colorScheme / JellyfinTheme seul) |
| **C** | Mix des deux (partiellement migre, ex: VegafoXIcons mais pas VegafoXColors) |

---

### 1. HOME / NAVIGATION

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 1 | Home Screen | `ui/home/compose/HomeScreen.kt` | HomeComposeFragment | **A** | NON | - |
| 2 | Home Hero Backdrop | `ui/home/compose/HomeHeroBackdrop.kt` | (compose HomeScreen) | **A** | NON | - |
| 3 | Sidebar Navigation | `ui/home/compose/sidebar/` | (compose HomeScreen) | **A** | NON | - |
| 4 | Main Toolbar | `ui/shared/toolbar/MainToolbar.kt` | (compose embed) | **A** | NON | - |
| 5 | Left Sidebar Nav | `ui/shared/toolbar/LeftSidebarNavigation.kt` | (compose embed) | **A** | NON | - |

### 2. BROWSING / BIBLIOTHEQUES

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 6 | Library Browse | `ui/browsing/compose/LibraryBrowseScreen.kt` | LibraryBrowseComposeFragment | **C** | NON | P1 |
| 7 | Folder Browse | `ui/browsing/compose/FolderBrowseScreen.kt` | FolderBrowseComposeFragment | **C** | NON | P1 |
| 8 | Genres Grid | `ui/browsing/compose/GenresGridScreen.kt` | GenresGridComposeFragment | **C** | NON | P1 |
| 9 | By Letter Browse | `ui/browsing/compose/ByLetterBrowseScreen.kt` | ByLetterBrowseFragment | **B** | NON | P1 |
| 10 | Collection Browse | `ui/browsing/compose/CollectionBrowseScreen.kt` | CollectionBrowseFragment | **B** | NON | P2 |
| 11 | All Favorites | `ui/browsing/compose/AllFavoritesScreen.kt` | AllFavoritesComposeFragment | **B** | NON | P2 |
| 12 | Folder View | `ui/browsing/compose/FolderViewScreen.kt` | FolderViewComposeFragment | **B** | NON | P2 |
| 13 | Suggested Movies | `ui/browsing/compose/SuggestedMoviesScreen.kt` | SuggestedMoviesComposeFragment | **B** | NON | P2 |
| 14 | Live TV Browse | `ui/browsing/v2/LiveTvBrowseFragment.kt` | LiveTvBrowseFragment | **C** | NON | P1 |
| 15 | Music Browse | `ui/browsing/v2/MusicBrowseFragment.kt` | MusicBrowseFragment | **C** | NON | P1 |
| 16 | Schedule Browse | `ui/browsing/v2/ScheduleBrowseFragment.kt` | ScheduleBrowseFragment | **C** | NON | P1 |
| 17 | Recordings Browse | `ui/browsing/v2/RecordingsBrowseFragment.kt` | RecordingsBrowseFragment | **C** | NON | P1 |
| 18 | Series Recordings | `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt` | SeriesRecordingsBrowseFragment | **C** | NON | P2 |
| 19 | Browse Components | `ui/browsing/v2/LibraryBrowseComponents.kt` | (compose shared) | **C** | NON | P1 |

### 3. DETAIL MEDIA

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 20 | Item Detail (v1) | `ui/itemdetail/compose/ItemDetailComposeFragment.kt` | ItemDetailComposeFragment | **C** | SUSPECT | P0 |
| 21 | Item Detail (v2) | `ui/itemdetail/v2/ItemDetailsFragment.kt` | ItemDetailsFragment | **C** | SUSPECT | P0 |
| 22 | Movie Details Content | `ui/itemdetail/v2/content/MovieDetailsContent.kt` | (compose v2) | **C** | NON | P0 |
| 23 | Season Details Content | `ui/itemdetail/v2/content/SeasonDetailsContent.kt` | (compose v2) | **C** | NON | P0 |
| 24 | LiveTv Details Content | `ui/itemdetail/v2/content/LiveTvDetailsContent.kt` | (compose v2) | **C** | NON | P1 |
| 25 | SeriesTimer Details | `ui/itemdetail/v2/content/SeriesTimerDetailsContent.kt` | (compose v2) | **C** | NON | P1 |
| 26 | Detail Actions | `ui/itemdetail/v2/shared/DetailActions.kt` | (compose shared) | **C** | NON | P0 |
| 27 | Detail Hero Backdrop | `ui/itemdetail/compose/DetailHeroBackdrop.kt` | (compose v1) | **B** | NON | P1 |
| 28 | Item List (albums) | `ui/itemdetail/ItemListFragment.kt` | ItemListFragment | **B** | NON | P2 |
| 29 | Music Favorites List | `ui/itemdetail/MusicFavoritesListFragment.kt` | MusicFavoritesListFragment | **B** | NON | P2 |

> **DOUBLON SUSPECT** : ItemDetailComposeFragment (v1) et ItemDetailsFragment (v2) font la meme chose. Les deux sont references dans Destinations.kt (`itemDetails` et `itemDetailsV2`).

### 4. PLAYER

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 30 | Video Player (legacy) | `ui/playback/CustomPlaybackOverlayFragment.kt` | CustomPlaybackOverlayFragment | **C** | SUSPECT | P1 |
| 31 | Video Player (new) | `ui/player/video/VideoPlayerFragment.kt` | VideoPlayerFragment | **C** | SUSPECT | P0 |
| 32 | Video Player Controls | `ui/player/video/VideoPlayerControls.kt` | (compose) | **C** | NON | P0 |
| 33 | Video Player Overlay | `ui/player/video/VideoPlayerOverlay.kt` | (compose) | **C** | NON | P0 |
| 34 | Photo Player | `ui/player/photo/PhotoPlayerFragment.kt` | PhotoPlayerFragment | **C** | NON | P1 |
| 35 | Photo Player Controls | `ui/player/photo/PhotoPlayerControls.kt` | (compose) | **C** | NON | P1 |
| 36 | Audio Now Playing | `ui/playback/audio/AudioNowPlayingScreen.kt` | AudioNowPlayingComposeFragment | **C** | NON | P1 |
| 37 | Player Overlay (legacy) | `ui/playback/overlay/compose/PlayerOverlayScreen.kt` | (compose) | **C** | NON | P1 |
| 38 | Leanback Overlay | `ui/playback/overlay/LeanbackOverlayFragment.kt` | LeanbackOverlayFragment | **B** | NON | P2 |
| 39 | Skip Overlay | `ui/playback/overlay/SkipOverlayView.kt` | (compose embed) | **C** | NON | P2 |
| 40 | Next Up | `ui/playback/nextup/NextUpFragment.kt` | NextUpFragment | **B** | NON | P1 |
| 41 | Still Watching | `ui/playback/stillwatching/StillWatchingFragment.kt` | StillWatchingFragment | **B** | NON | P2 |
| 42 | Trailer Player | `ui/itemdetail/v2/TrailerPlayerFragment.kt` | TrailerPlayerFragment | **B** | NON | P2 |

> **DOUBLON SUSPECT** : CustomPlaybackOverlayFragment (`videoPlayer`) et VideoPlayerFragment (`videoPlayerNew`) sont deux implementations du lecteur video. Les deux existent dans Destinations.kt.

### 5. LIVE TV

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 43 | Live TV Guide | `ui/livetv/compose/LiveTvGuideScreen.kt` | LiveTvGuideFragment | **C** | NON | P1 |
| 44 | Program Detail Dialog | `ui/livetv/compose/ProgramDetailDialog.kt` | (compose dialog) | **C** | NON | P1 |
| 45 | Program Cell | `ui/livetv/compose/ProgramCellComposable.kt` | (compose) | **C** | NON | P1 |
| 46 | Channel Header | `ui/livetv/compose/ChannelHeaderComposable.kt` | (compose) | **C** | NON | P2 |

### 6. RECHERCHE

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 47 | Search Screen | `ui/search/compose/SearchScreen.kt` | SearchComposeFragment | **C** | NON | P1 |
| 48 | Search Text Input | `ui/search/composable/SearchTextInput.kt` | (compose) | **C** | NON | P1 |
| 49 | Search Voice Input | `ui/search/composable/SearchVoiceInput.kt` | (compose) | **C** | NON | P2 |

### 7. STARTUP / AUTHENTIFICATION

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 50 | Splash | `ui/startup/fragment/SplashFragment.kt` | SplashFragment | **B** | NON | P2 |
| 51 | Welcome Screen | `ui/startup/compose/WelcomeScreen.kt` | WelcomeFragment | **A** | NON | - |
| 52 | Server Discovery | `ui/startup/server/ServerDiscoveryScreen.kt` | ServerDiscoveryFragment | **A** | NON | - |
| 53 | Quick Connect | `ui/startup/server/QuickConnectScreen.kt` | QuickConnectFragment | **A** | NON | - |
| 54 | User Selection | `ui/startup/user/UserSelectionScreen.kt` | UserSelectionFragment | **A** | NON | - |
| 55 | User Login | `ui/startup/user/UserLoginScreen.kt` | UserLoginFragment (user/) | **A** | SUSPECT | - |
| 56 | Pin Entry | `ui/startup/user/PinEntryScreen.kt` | (compose) | **A** | NON | - |
| 57 | Startup Toolbar | `ui/shared/toolbar/StartupToolbar.kt` | StartupToolbarFragment | **C** | NON | P2 |
| 58 | Select Server (legacy) | `ui/startup/fragment/SelectServerFragment.kt` | SelectServerFragment | **B** | SUSPECT | SKIP |
| 59 | Server Add | `ui/startup/fragment/ServerAddFragment.kt` | ServerAddFragment | **B** | NON | P2 |
| 60 | Server Fragment | `ui/startup/fragment/ServerFragment.kt` | ServerFragment | **B** | NON | P2 |
| 61 | User Login (legacy) | `ui/startup/fragment/UserLoginFragment.kt` | UserLoginFragment (fragment/) | **B** | OUI | SKIP |
| 62 | Login Credentials | `ui/startup/fragment/UserLoginCredentialsFragment.kt` | UserLoginCredentialsFragment | **B** | NON | P2 |
| 63 | Login Quick Connect | `ui/startup/fragment/UserLoginQuickConnectFragment.kt` | UserLoginQuickConnectFragment | **B** | NON | P2 |
| 64 | Connect Help Alert | `ui/startup/fragment/ConnectHelpAlertFragment.kt` | ConnectHelpAlertFragment | **C** | NON | P2 |

> **DOUBLON CONFIRME** : `ui/startup/user/UserLoginFragment.kt` (nouveau Compose) et `ui/startup/fragment/UserLoginFragment.kt` (ancien XML) coexistent. Le nouveau remplace l'ancien.

### 8. JELLYSEERR

| # | Ecran | Fichier | Fragment | UI | Doublon | Priorite |
|---|-------|---------|----------|-----|---------|----------|
| 65 | Jellyseerr Discover | `ui/jellyseerr/DiscoverFragment.kt` | DiscoverFragment | **B** | NON | P2 |
| 66 | Jellyseerr Discover Rows | `ui/jellyseerr/JellyseerrDiscoverRowsFragment.kt` | JellyseerrDiscoverRowsFragment | **B** | NON | P2 |
| 67 | Jellyseerr Requests | `ui/jellyseerr/RequestsFragment.kt` | RequestsFragment | **B** | NON | P2 |
| 68 | Jellyseerr Browse By | `ui/jellyseerr/compose/JellyseerrBrowseByScreen.kt` | JellyseerrBrowseByFragment | **C** | NON | P2 |
| 69 | Jellyseerr Media Details | `ui/jellyseerr/MediaDetailsFragment.kt` | MediaDetailsFragment | **B** | NON | P2 |
| 70 | Jellyseerr Person Details | `ui/jellyseerr/PersonDetailsFragment.kt` | PersonDetailsFragment | **B** | NON | P2 |
| 71 | Jellyseerr Settings | `ui/jellyseerr/SettingsFragment.kt` | SettingsFragment (jellyseerr) | **B** | NON | P2 |
| 72 | Jellyseerr Request Buttons | `ui/jellyseerr/compose/JellyseerrRequestButtons.kt` | (compose) | **C** | NON | P2 |

### 9. DIALOGS (non-ecrans, mais visibles)

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 73 | Add to Playlist | `ui/playlist/AddToPlaylistDialog.kt` | **C** | NON | P2 |
| 74 | Create Playlist | `ui/playlist/CreatePlaylistDialog.kt` | **C** | NON | P2 |
| 75 | Create Playlist Fragment | `ui/playlist/CreatePlaylistDialogFragment.kt` | **B** | SUSPECT | SKIP |
| 76 | Shuffle Options | `ui/shuffle/ShuffleOptionsDialog.kt` | **C** | NON | P2 |
| 77 | SyncPlay Dialog | `ui/syncplay/SyncPlayDialog.kt` | **C** | NON | P2 |

### 10. SETTINGS — Ecrans principaux

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 78 | Settings Main | `ui/settings/screen/SettingsMainScreen.kt` | **C** | NON | P1 |
| 79 | Settings Authentication | `ui/settings/screen/authentication/SettingsAuthenticationScreen.kt` | **C** | NON | P1 |
| 80 | Settings Auth Server | `ui/settings/screen/authentication/SettingsAuthenticationServerScreen.kt` | **C** | NON | P2 |
| 81 | Settings Auth Server User | `ui/settings/screen/authentication/SettingsAuthenticationServerUserScreen.kt` | **C** | NON | P2 |
| 82 | Settings Auth SortBy | `ui/settings/screen/authentication/SettingsAuthenticationSortByScreen.kt` | **B** | NON | P2 |
| 83 | Settings Auth AutoSignIn | `ui/settings/screen/authentication/SettingsAuthenticationAutoSignInScreen.kt` | **B** | NON | P2 |
| 84 | Settings Auth PinCode | `ui/settings/screen/authentication/SettingsAuthenticationPinCodeScreen.kt` | **B** | NON | P2 |
| 85 | Settings Customization | `ui/settings/screen/customization/SettingsCustomizationScreen.kt` | **C** | NON | P1 |
| 86 | Settings Custom Theme | `ui/settings/screen/customization/SettingsCustomizationThemeScreen.kt` | **B** | NON | P2 |
| 87 | Settings Custom Clock | `ui/settings/screen/customization/SettingsCustomizationClockScreen.kt` | **B** | NON | P2 |
| 88 | Settings Clock Behavior | `ui/settings/screen/customization/SettingsClockBehaviorScreen.kt` | **B** | NON | P2 |
| 89 | Settings Watched Indicator | `ui/settings/screen/customization/SettingsCustomizationWatchedIndicatorScreen.kt` | **B** | NON | P2 |
| 90 | Settings Watched Behavior | `ui/settings/screen/customization/SettingsWatchedIndicatorBehaviorScreen.kt` | **B** | NON | P2 |

### 11. SETTINGS — Screensaver

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 91 | Screensaver | `ui/settings/screen/screensaver/SettingsScreensaverScreen.kt` | **B** | NON | P2 |
| 92 | Screensaver Mode | `ui/settings/screen/screensaver/SettingsScreensaverModeScreen.kt` | **B** | NON | P2 |
| 93 | Screensaver Timeout | `ui/settings/screen/screensaver/SettingsScreensaverTimeoutScreen.kt` | **B** | NON | P2 |
| 94 | Screensaver Dimming | `ui/settings/screen/screensaver/SettingsScreensaverDimmingScreen.kt` | **B** | NON | P2 |
| 95 | Screensaver Age Rating | `ui/settings/screen/screensaver/SettingsScreensaverAgeRatingScreen.kt` | **B** | NON | P2 |

### 12. SETTINGS — Subtitles

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 96 | Subtitles | `ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt` | **B** | NON | P2 |
| 97 | Subtitles Text Color | `ui/settings/screen/customization/subtitle/SettingsSubtitlesTextColorScreen.kt` | **B** | NON | P2 |
| 98 | Subtitles Bg Color | `ui/settings/screen/customization/subtitle/SettingsSubtitlesBackgroundColorScreen.kt` | **B** | NON | P2 |
| 99 | Subtitles Edge Color | `ui/settings/screen/customization/subtitle/SettingsSubtitleTextStrokeColorScreen.kt` | **B** | NON | P2 |

### 13. SETTINGS — Libraries

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 100 | Libraries | `ui/settings/screen/library/SettingsLibrariesScreen.kt` | **C** | NON | P1 |
| 101 | Libraries Display | `ui/settings/screen/library/SettingsLibrariesDisplayScreen.kt` | **B** | NON | P2 |
| 102 | Libraries Image Type | `ui/settings/screen/library/SettingsLibrariesDisplayImageTypeScreen.kt` | **B** | NON | P2 |
| 103 | Libraries Image Size | `ui/settings/screen/library/SettingsLibrariesDisplayImageSizeScreen.kt` | **B** | NON | P2 |
| 104 | Libraries Grid | `ui/settings/screen/library/SettingsLibrariesDisplayGridScreen.kt` | **B** | NON | P2 |

### 14. SETTINGS — Home

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 105 | Home Settings | `ui/settings/screen/home/SettingsHomeScreen.kt` | **C** | NON | P1 |
| 106 | Home Section | `ui/settings/screen/home/SettingsHomeSectionScreen.kt` | **B** | NON | P2 |
| 107 | Home Poster Size | `ui/settings/screen/home/SettingsHomePosterSizeScreen.kt` | **B** | NON | P2 |

### 15. SETTINGS — Live TV

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 108 | LiveTV Guide Filters | `ui/settings/screen/livetv/SettingsLiveTvGuideFiltersScreen.kt` | **B** | NON | P2 |
| 109 | LiveTV Guide Options | `ui/settings/screen/livetv/SettingsLiveTvGuideOptionsScreen.kt` | **B** | NON | P2 |
| 110 | LiveTV Channel Order | `ui/settings/screen/livetv/SettingsLiveTvGuideChannelOrderScreen.kt` | **B** | NON | P2 |

### 16. SETTINGS — Playback

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 111 | Playback | `ui/settings/screen/playback/SettingsPlaybackScreen.kt` | **C** | NON | P1 |
| 112 | Playback Player | `ui/settings/screen/playback/SettingsPlaybackPlayerScreen.kt` | **B** | NON | P2 |
| 113 | Playback Next Up | `ui/settings/screen/playback/nextup/SettingsPlaybackNextUpScreen.kt` | **B** | NON | P2 |
| 114 | Playback Next Up Behavior | `ui/settings/screen/playback/nextup/SettingsPlaybackNextUpBehaviorScreen.kt` | **B** | NON | P2 |
| 115 | Playback Inactivity | `ui/settings/screen/playback/SettingsPlaybackInactivityPromptScreen.kt` | **B** | NON | P2 |
| 116 | Playback Prerolls | `ui/settings/screen/playback/SettingsPlaybackPrerollsScreen.kt` | **B** | NON | P2 |
| 117 | Playback Media Segments | `ui/settings/screen/playback/mediasegment/SettingsPlaybackMediaSegmentsScreen.kt` | **B** | NON | P2 |
| 118 | Playback Media Segment | `ui/settings/screen/playback/mediasegment/SettingsPlaybackMediaSegmentScreen.kt` | **B** | NON | P2 |
| 119 | Playback Advanced | `ui/settings/screen/playback/SettingsPlaybackAdvancedScreen.kt` | **B** | NON | P2 |
| 120 | Playback Resume Subtract | `ui/settings/screen/playback/SettingsPlaybackResumeSubtractDurationScreen.kt` | **B** | NON | P2 |
| 121 | Playback Max Bitrate | `ui/settings/screen/playback/SettingsPlaybackMaxBitrateScreen.kt` | **B** | NON | P2 |
| 122 | Playback Max Resolution | `ui/settings/screen/playback/SettingsPlaybackMaxResolutionScreen.kt` | **B** | NON | P2 |
| 123 | Playback Refresh Rate | `ui/settings/screen/playback/SettingsPlaybackRefreshRateSwitchingBehaviorScreen.kt` | **B** | NON | P2 |
| 124 | Playback Zoom Mode | `ui/settings/screen/playback/SettingsPlaybackZoomModeScreen.kt` | **B** | NON | P2 |
| 125 | Playback Audio Behavior | `ui/settings/screen/playback/SettingsPlaybackAudioBehaviorScreen.kt` | **B** | NON | P2 |

### 17. SETTINGS — VegafoX specifiques

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 126 | Jellyseerr Settings | `ui/settings/screen/vegafox/SettingsJellyseerrScreen.kt` | **C** | NON | P1 |
| 127 | Jellyseerr Rows | `ui/settings/screen/vegafox/SettingsJellyseerrRowsScreen.kt` | **C** | NON | P2 |
| 128 | Plugin Settings | `ui/settings/screen/vegafox/SettingsPluginScreen.kt` | **C** | NON | P2 |
| 129 | VegafoX Parental Controls | `ui/settings/screen/vegafox/SettingsVegafoXParentalControlsScreen.kt` | **B** | NON | P2 |
| 130 | VegafoX SyncPlay | `ui/settings/screen/vegafox/SettingsVegafoXSyncPlayScreen.kt` | **B** | NON | P2 |
| 131 | VegafoX Home Rows Image | `ui/settings/screen/vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | **B** | NON | P2 |
| 132-141 | VegafoX OptionList (x10) | `ui/settings/composable/OptionListScreen` (generique) | **B** | NON | P2 |

> Les routes NavbarPosition, ShuffleContentType, MediaBarContentType, MediaBarItemCount, MediaBarOpacity, MediaBarColor, ThemeMusicVolume, SeasonalSurprise, DetailsBlur, BrowsingBlur utilisent toutes le composant generique `OptionListScreen`.

### 18. SETTINGS — Autres

| # | Ecran | Fichier | UI | Doublon | Priorite |
|---|-------|---------|-----|---------|----------|
| 142 | About | `ui/settings/screen/about/SettingsAboutScreen.kt` | **C** | NON | P2 |
| 143 | Telemetry | `ui/settings/screen/SettingsTelemetryScreen.kt` | **B** | NON | P2 |
| 144 | Developer | `ui/settings/screen/SettingsDeveloperScreen.kt` | **B** | NON | P2 |
| 145 | Licenses | `ui/settings/screen/license/SettingsLicensesScreen.kt` | **B** | NON | P2 |
| 146 | License Detail | `ui/settings/screen/license/SettingsLicenseScreen.kt` | **B** | NON | P2 |

---

## DOUBLONS CONFIRMES

### Startup — 3 doublons entremeles

| Fonction | NOUVEAU (Compose/VegafoX) | ANCIEN (XML/Jellyfin) | Statut |
|----------|--------------------------|----------------------|--------|
| **Selection serveur** | `ServerDiscoveryFragment` → `ServerDiscoveryScreen` (A) | `SelectServerFragment` (B, ViewBinding + RecyclerView) | **DOUBLON** — L'ancien est encore appele depuis UserSelectionFragment.navigateToSelectServer() |
| **Selection profil** | `UserSelectionFragment` (user/) → `UserSelectionScreen` (A) | `ServerFragment` (B, ViewBinding + UserCardView) | **DOUBLON** — L'ancien est encore appele depuis SelectServerFragment |
| **Login** | `UserLoginFragment` (user/) → `UserLoginScreen` (A) | `UserLoginFragment` (fragment/) + `UserLoginCredentialsFragment` + `UserLoginQuickConnectFragment` (B) | **DOUBLON** — L'ancien est appele depuis ServerFragment |

> **Impact UX** : Un utilisateur qui arrive via "Changer de serveur" traverse TOUT l'ancien flux XML
> (SelectServerFragment → ServerFragment → UserLoginFragment ancien). Il passe du design VegafoX
> au design Jellyfin original, puis revient au VegafoX apres authentification. C'est un P0 a corriger.

### Autres doublons

| Ecran ancien | Ecran nouveau | Statut |
|-------------|---------------|--------|
| `ItemDetailComposeFragment` (v1) | `ItemDetailsFragment` (v2) | **COEXISTENCE** — deux systemes de detail, les deux dans Destinations (`itemDetails` et `itemDetailsV2`) |
| `CustomPlaybackOverlayFragment` (videoPlayer) | `VideoPlayerFragment` (videoPlayerNew) | **COEXISTENCE** — deux lecteurs video, les deux dans Destinations (`videoPlayer` et `videoPlayerNew`) |
| `CreatePlaylistDialogFragment` | `CreatePlaylistDialog` (Compose) | **SUSPECT** — verifier si l'ancien est encore utilise |

---

## RESUME EXECUTIF

### Statistiques globales

| Metrique | Valeur |
|----------|--------|
| **Total ecrans uniques** | **~146** |
| **Ecrans A (nouveau design VegafoX)** | **10** (7%) |
| **Ecrans B (design Jellyfin original)** | **~99** (68%) |
| **Ecrans C (mix partiellement migre)** | **~37** (25%) |
| **Doublons startup confirmes** | **3** (SelectServer, ServerFragment/UserSelection, UserLogin) |
| **Coexistences v1/v2** | **2** (ItemDetail, VideoPlayer) |
| **Doublons dialog suspect** | **1** (CreatePlaylistDialog) |

### Repartition par categorie

| Categorie | Total | A | B | C |
|-----------|-------|---|---|---|
| Home/Navigation | 5 | 5 | 0 | 0 |
| Browsing | 14 | 0 | 5 | 9 |
| Detail Media | 10 | 0 | 4 | 6 |
| Player | 13 | 0 | 5 | 8 |
| Live TV | 4 | 0 | 0 | 4 |
| Recherche | 3 | 0 | 0 | 3 |
| Startup | 15 | 6 | 7 | 2 |
| Jellyseerr | 8 | 0 | 6 | 2 |
| Dialogs | 5 | 0 | 1 | 4 |
| Settings | 69 | 0 | 57 | 12 |
| **TOTAL** | **146** | **11** | **85** | **50** |

> Nota : Apres recomptage precis (A=11 incluant Home toolbar/sidebar), les proportions sont :
> - **A** : ~8% — Nouveau design VegafoX complet
> - **B** : ~58% — Design Jellyfin original
> - **C** : ~34% — Partiellement migre

### Ce qui est deja fait (A = VegafoX complet)

1. Home Screen + Hero + Sidebar + Toolbar
2. Welcome Screen (premiere ouverture)
3. Server Discovery + Quick Connect
4. User Selection + User Login + Pin Entry

**Conclusion** : Le flux "premiere experience utilisateur" (startup → home) est entierement VegafoX.

### Ce qui est partiellement migre (C = mix)

- **Browsing** : VegafoXIcons utilises, mais pas VegafoXColors ni VegafoXButton
- **Details** : Certains contenus migres (MovieDetails, SeasonDetails), mais pas le cadre
- **Player** : Controles partiellement migres, infrastructure legacy
- **Search** : VegafoXIcons mais pas les couleurs
- **Settings top-level** : VegafoXColors sur les 12 ecrans principaux, pas les sous-ecrans
- **Live TV** : Composants Compose avec VegafoXColors, mais structure legacy

### Priorites redesign recommandees

**P0 — Ecrans les plus visibles et critiques** (6 ecrans)
- Item Detail v2 (MovieDetails, SeasonDetails, cadre)
- Video Player New (controles, overlay)
- Detail Actions (boutons play, favoris, etc.)

**P1 — Frequemment utilises** (~20 ecrans)
- Library Browse, Folder Browse, Genres Grid
- Search Screen
- Live TV Guide
- Audio Now Playing, Next Up
- Settings Main, Authentication, Customization, Playback, Home, Libraries
- Browsing v2 components (Schedule, Recordings, Music, LiveTV)

**P2 — Secondaires** (~55 ecrans)
- Sous-ecrans settings (screensaver, subtitles, playback sub-options)
- Jellyseerr (discover, requests, details)
- Legacy fragments (ItemListFragment, MusicFavorites)
- Dialogs, overlays secondaires

**SKIP — A supprimer apres migration** (5 ecrans)
- `ui/startup/fragment/UserLoginFragment.kt` (remplace par `user/UserLoginFragment.kt`)
- `ui/startup/fragment/UserLoginCredentialsFragment.kt` (integre dans UserLoginScreen)
- `ui/startup/fragment/UserLoginQuickConnectFragment.kt` (integre dans UserLoginScreen)
- `ui/startup/fragment/SelectServerFragment.kt` (remplace par ServerDiscoveryFragment)
- `ui/startup/fragment/ServerFragment.kt` (remplace par UserSelectionFragment)

> **ACTION REQUISE** : Avant suppression, il faut que `UserSelectionFragment.navigateToSelectServer()`
> navigue vers `ServerDiscoveryFragment` au lieu de `SelectServerFragment`. C'est le lien qui
> maintient l'ancien flux en vie.

### Estimation de la dette UI restante

| Phase | Ecrans | Effort estime |
|-------|--------|---------------|
| P0 (Detail + Player) | ~6 | 3-4 sessions |
| P1 (Browse + Search + Settings top) | ~20 | 8-10 sessions |
| P2 (Settings sub + Jellyseerr + Legacy) | ~55 | 15-20 sessions |
| Cleanup doublons | ~4 | 1-2 sessions |
| **Total** | **~85 ecrans a migrer** | **~27-36 sessions** |

### Point cle architectural

Le `colorScheme.kt` a ete modifie pour mapper VegafoXColors vers le ColorScheme Material. Cela signifie que **tous les ecrans B qui utilisent `JellyfinTheme.colorScheme` recoivent deja les couleurs VegafoX de base** (fond sombre, etc.). La migration C→A consiste surtout a :
1. Remplacer les boutons par `VegafoXButton`
2. Utiliser `VegafoXGradients` pour les fonds
3. Adopter `VegafoXIcons` au lieu des drawables XML
4. Ajouter les effets visuels (glow, animations de focus)

---

## Design System VegafoX — Tokens disponibles

| Token | Fichier | Usage actuel |
|-------|---------|-------------|
| VegafoXColors | `ui/base/theme/VegafoXColors.kt` | 22 fichiers |
| VegafoXGradients | `ui/base/theme/VegafoXGradients.kt` | 5 fichiers |
| VegafoXButton | `ui/base/components/VegafoXButton.kt` | 9 fichiers |
| VegafoXIcons | `ui/base/icons/VegafoXIcons.kt` | 67 fichiers |
| colorScheme (bridge) | `ui/base/colorScheme.kt` | 141+ fichiers (herite VegafoX) |
