# Screen ID Debug Overlay

**Date** : 2026-03-12
**Branche** : main
**Guard** : `BuildConfig.DEBUG` — aucun badge en release

## Principe

En mode debug uniquement, un badge semi-transparent s'affiche en haut-droite
de chaque ecran avec son numero et son nom court.

```
┌─────────────────────────────────┐
│                    ┌───────────┐│
│                    │ 011·Home  ││
│                    └───────────┘│
│                                 │
│        (contenu ecran)          │
│                                 │
└─────────────────────────────────┘
```

**Style du badge** :
- Fond : `BackgroundDeep` alpha 0.85
- Bordure : 1dp `OrangePrimary` alpha 0.50
- Coins : 6dp rounded
- Padding : 4dp vertical, 8dp horizontal
- Texte : `FontFamily.Monospace` 11sp `OrangePrimary`
- Position : `Alignment.TopEnd` + padding 8dp
- Z-index : `Float.MAX_VALUE` (au-dessus de tout)
- Non-focusable (ne perturbe pas le D-pad)

## Fichiers cles

| Fichier | Role |
|---------|------|
| `ui/base/debug/ScreenIdOverlay.kt` | Composable overlay (Box + badge conditionnel) |
| `ui/base/debug/ScreenIds.kt` | Constantes ID + NAME pour chaque ecran |

## Integration

L'overlay est applique au **niveau de l'appel** (Fragment `setContent{}` ou route Settings),
pas a l'interieur des composables Screen eux-memes. Cela evite de modifier les signatures.

Pattern Fragment :
```kotlin
setContent {
    JellyfinTheme {
        ScreenIdOverlay(ScreenIds.HOME_ID, ScreenIds.HOME_NAME) {
            HomeScreen(...)
        }
    }
}
```

Pattern route Settings :
```kotlin
Routes.MAIN to {
    ScreenIdOverlay(ScreenIds.SETTINGS_MAIN_ID, ScreenIds.SETTINGS_MAIN_NAME) {
        SettingsMainScreen()
    }
},
```

---

## Liste complete des ecrans avec IDs

### Startup / Auth (001-009)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 001 | Splash | `ui/startup/fragment/SplashFragment.kt` | SplashScreen |
| 002 | ServerDiscovery | `ui/startup/server/ServerDiscoveryFragment.kt` | ServerDiscoveryScreen |
| 004 | UserSelect | `ui/startup/user/UserSelectionFragment.kt` | UserSelectionScreen |
| 005 | UserLogin | `ui/startup/user/UserLoginFragment.kt` | UserLoginScreen |
| 006 | QuickConnect | `ui/startup/server/QuickConnectFragment.kt` | QuickConnectScreen |
| 008 | Welcome | `ui/startup/fragment/WelcomeFragment.kt` | WelcomeScreen |
| 009 | ConnectHelp | `ui/startup/fragment/ConnectHelpAlertFragment.kt` | ConnectHelpAlert |

> Note : ServerAddFragment (003) et PinEntryScreen (007) sont XML/Dialog, non wrappables.

### Home (011)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 011 | Home | `ui/home/compose/HomeComposeFragment.kt` | HomeScreen |

### Browse (021-035)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 021 | LibraryBrowse | `ui/browsing/compose/LibraryBrowseComposeFragment.kt` | LibraryBrowseScreen |
| 022 | GenresGrid | `ui/browsing/compose/GenresGridComposeFragment.kt` | GenresGridScreen |
| 023 | FolderBrowse | `ui/browsing/compose/FolderBrowseComposeFragment.kt` | FolderBrowseScreen |
| 024 | ByLetter | `ui/browsing/compose/ByLetterBrowseFragment.kt` | ByLetterBrowseScreen |
| 025 | AllFavorites | `ui/browsing/compose/AllFavoritesFragment.kt` | AllFavoritesScreen |
| 027 | Search | `ui/search/compose/SearchComposeFragment.kt` | SearchScreen |
| 028 | CollectionBrowse | `ui/browsing/compose/CollectionBrowseFragment.kt` | CollectionBrowseScreen |
| 029 | SuggestedMovies | `ui/browsing/compose/SuggestedMoviesFragment.kt` | SuggestedMoviesScreen |

### Browse v2 (031-035)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 031 | LiveTV·Browse | `ui/browsing/v2/LiveTvBrowseFragment.kt` | LiveTvBrowseContent |
| 032 | MusicBrowse | `ui/browsing/v2/MusicBrowseFragment.kt` | MusicBrowseContent |
| 033 | ScheduleBrowse | `ui/browsing/v2/ScheduleBrowseFragment.kt` | ScheduleBrowseContent |
| 034 | RecordingsBrowse | `ui/browsing/v2/RecordingsBrowseFragment.kt` | RecordingsBrowseContent |
| 035 | SeriesRecordings | `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt` | SeriesRecordingsContent |

### Detail (041-043)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 041 | ItemDetail·v2 | `ui/itemdetail/v2/ItemDetailsFragment.kt` | ItemDetailsContent |

> Note : ItemListFragment (042) et MusicFavoritesListFragment (043) sont XML, non wrappables.

### Player (051-056)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 051 | VideoPlayer | `ui/player/video/VideoPlayerFragment.kt` | VideoPlayerScreen |
| 052 | NextUp | `ui/playback/nextup/NextUpFragment.kt` | NextUpScreen |
| 053 | StillWatching | `ui/playback/stillwatching/StillWatchingFragment.kt` | StillWatchingScreen |
| 054 | AudioPlayer | `ui/playback/audio/AudioNowPlayingComposeFragment.kt` | AudioNowPlayingScreen |
| 055 | PhotoPlayer | `ui/player/photo/PhotoPlayerFragment.kt` | PhotoPlayerScreen |

> Note : TrailerPlayerFragment (056) est XML/ExoPlayer natif, non wrappable.

### Settings (071-089)

| ID | Nom | Fichier route | Ecran |
|----|-----|---------------|-------|
| 071 | Settings·Main | `routes/AllRoutes.kt` | SettingsMainScreen |
| 072 | Settings·Auth | `routes/AuthenticationRoutes.kt` | SettingsAuthenticationScreen |
| 073 | Settings·Custom | `routes/CustomizationRoutes.kt` | SettingsCustomizationScreen |
| 074 | Settings·Plugin | `routes/VegafoXRoutes.kt` | SettingsPluginScreen |
| 075 | Settings·Screensaver | `routes/CustomizationRoutes.kt` | SettingsScreensaverScreen |
| 076 | Settings·Playback | `routes/PlaybackRoutes.kt` | SettingsPlaybackScreen |
| 077 | Settings·PlaybackAdv | `routes/PlaybackRoutes.kt` | SettingsPlaybackAdvancedScreen |
| 078 | Settings·Telemetry | `routes/AllRoutes.kt` | SettingsTelemetryScreen |
| 079 | Settings·Developer | `routes/AllRoutes.kt` | SettingsDeveloperScreen |
| 080 | Settings·About | `routes/AllRoutes.kt` | SettingsAboutScreen |
| 081 | Settings·Libraries | `routes/LibraryRoutes.kt` | SettingsLibrariesScreen |
| 082 | Settings·Jellyseerr | `routes/VegafoXRoutes.kt` | SettingsJellyseerrScreen |
| 083 | Settings·Home | `routes/HomeRoutes.kt` | SettingsHomeScreen |
| 084 | Settings·Subtitles | `routes/CustomizationRoutes.kt` | SettingsSubtitlesScreen |
| 085 | Settings·Parental | `routes/VegafoXRoutes.kt` | SettingsVegafoXParentalControlsScreen |
| 086 | Settings·SyncPlay | `routes/VegafoXRoutes.kt` | SettingsVegafoXSyncPlayScreen |
| 087 | Settings·HomeRowsImg | `routes/HomeRoutes.kt` | SettingsVegafoXHomeRowsImageScreen |
| 088 | Settings·JellyseerrRows | `routes/VegafoXRoutes.kt` | SettingsJellyseerrRowsScreen |
| 089 | Settings·HomePoster | `routes/HomeRoutes.kt` | SettingsHomePosterSizeScreen |

### Live TV (091)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 091 | LiveTV·Guide | `ui/livetv/LiveTvGuideFragment.kt` | LiveTvGuideScreen |

### Jellyseerr (101-103)

| ID | Nom | Fichier integration | Ecran |
|----|-----|---------------------|-------|
| 101 | Jellyseerr·BrowseBy | `ui/jellyseerr/JellyseerrBrowseByFragment.kt` | JellyseerrBrowseByScreen |
| 102 | Jellyseerr·Media | `ui/jellyseerr/MediaDetailsFragment.kt` | JellyseerrMediaDetailsScreen |
| 103 | Jellyseerr·Person | `ui/jellyfin/PersonDetailsFragment.kt` | JellyseerrPersonDetailsScreen |

---

## Ecrans non couverts (XML / pas Compose)

| Ecran | Raison |
|-------|--------|
| ServerAddFragment | Fragment XML traditionnel (DataBinding) |
| PinEntryScreen | Dialog Compose (pas un ecran plein) |
| ItemListFragment | Fragment XML (ViewBinding) |
| MusicFavoritesListFragment | Fragment XML (ViewBinding) |
| TrailerPlayerFragment | Fragment XML/ExoPlayer natif |
| OptionListScreen (x10 routes) | Ecrans generiques sans ID individuel |
| Sous-ecrans Settings (x40+) | Non prioritaires, pas d'ID assigne |

---

## Verification BuildConfig.DEBUG

Le guard est dans `ScreenIdOverlay.kt` :
```kotlin
if (BuildConfig.DEBUG) {
    Text(
        text = "$id·$name",
        // ...badge styling...
    )
}
```

- **Debug build** : badge visible sur tous les ecrans instrumentes
- **Release build** : le `if (BuildConfig.DEBUG)` est elimine par R8, zero overhead
- **Compile verifie** : `compileGithubDebugKotlin` OK, `compileGithubReleaseKotlin` OK

## Total ecrans instrumentes

| Categorie | Count |
|-----------|-------|
| Startup/Auth | 7 |
| Home | 1 |
| Browse | 8 |
| Browse v2 | 5 |
| Detail | 1 |
| Player | 5 |
| Settings | 19 |
| LiveTV | 1 |
| Jellyseerr | 3 |
| **Total** | **50** |
