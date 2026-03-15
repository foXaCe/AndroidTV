# Audit complet des Settings VegafoX Android TV

**Date** : 2026-03-12
**Branche** : main
**Methode** : Lecture exhaustive de tous les fichiers `ui/settings/` (screen, composable, routes, compat)

---

## Architecture Settings

### Structure generale

Les Settings ne sont **pas une Activity separee** mais un **dialog Compose** qui se superpose a l'ecran principal :

```
MainActivitySettings.kt (@Composable)
├── JellyfinTheme { }
│   └── ProvideRouter(routes, "/") { }
│       └── SettingsDialog(visible, onDismiss)
│           └── SettingsLayout (Box 350dp, fond surface, clip large)
│               └── SettingsRouterContent (NavHost Compose avec transitions slide)
```

### Navigation interne

- **82 routes** definies dans `Routes.kt` (objet const val)
- Routes assemblees dans `AllRoutes.kt` : `allSettingsRoutes` = fusion de 8 maps
- Navigation par `LocalRouter.current.push(route)` / `router.back()`
- Transitions : slide horizontal + fade (FastOutSlowInEasing, DURATION_MEDIUM)

### Fichiers routes (assemblage)

| Fichier | Domaine | Nb routes |
|---------|---------|-----------|
| `routes/AllRoutes.kt` | Main, Telemetry, Developer, About, Licenses | 6 |
| `routes/AuthenticationRoutes.kt` | Auth, Auto sign-in, Sort by, Pin | 7 |
| `routes/CustomizationRoutes.kt` | Theme, Clock, Watched, Screensaver, Subtitles | 13 |
| `routes/LibraryRoutes.kt` | Libraries display per-library | 5 |
| `routes/HomeRoutes.kt` | Home sections, poster size, rows image type | 3 |
| `routes/LiveTvRoutes.kt` | Guide filters, options, channel order | 3 |
| `routes/PlaybackRoutes.kt` | Playback + SyncPlay numeric | 20 |
| `routes/VegafoXRoutes.kt` | Plugin, Jellyseerr, OptionList generiques | 15 |

---

## Composants partages

### 1. `SettingsColumn` — Composant conteneur (A)

**Fichier** : `composable/SettingsColumn.kt`
**Utilise par** : **TOUS les ecrans settings** (~40+ ecrans)
**Statut** : **A** — Compose pur, utilise Tokens.Space du design system
**Description** : LazyColumn avec focus tracking et restauration. Sauvegarde l'index focus via Saver, restaure apres back-navigation.

### 2. `SettingsLayout` — Cadre du dialog (C)

**Fichier** : `composable/SettingsLayout.kt`
**Utilise par** : `SettingsDialog` (unique)
**Statut** : **C** — Utilise `JellyfinTheme.colorScheme.surface` (bridge VegafoX), formes locales
**Description** : Box 350dp, fond surface, clip large, padding medium

### 3. `SettingsDialog` — Dialog overlay (C)

**Fichier** : `composable/SettingsDialog.kt`
**Utilise par** : `MainActivitySettings` (unique)
**Statut** : **C** — DialogBase + SettingsLayout, animations standard

### 4. `SettingsRouterContent` — Transitions nav (B)

**Fichier** : `composable/SettingsRouterContent.kt`
**Statut** : **B** — Wrapper RouterContent avec transitions slide, pas de VegafoX specifique

### 5. `SettingsEntry` + `OptionListScreen` — Ecran radio generique (B)

**Fichier** : `composable/SettingsEntry.kt`
**Utilise par** : **10 sous-ecrans VegafoX** (navbar, shuffle, media bar x4, blur x2, seasonal, theme music)
**Statut** : **B** — ListSection + items RadioButton, aucun token VegafoX
**Effet cascade** : Migrer ce composant applique VegafoX a 10 ecrans d'un coup

### 6. `SettingsNumericScreen` — Ecran numerique generique (B)

**Fichier** : `composable/SettingsNumericScreen.kt`
**Utilise par** : **5 sous-ecrans SyncPlay** (min/max delay, duration, skip delay, extra offset)
**Statut** : **B** — ListSection + RadioButton, genere les options min→max par step

### 7. `SettingsAsyncActionListButton` — Bouton action async (C)

**Fichier** : `composable/SettingsAsyncActionListButton.kt`
**Utilise par** : `SettingsPlaybackAdvancedScreen` (report device profile)
**Statut** : **C** — Utilise VegafoXIcons (Upload, Check, Error) + Tokens.Color

### 8. `rememberPreference` — Bridge preferences Compose (B)

**Fichier** : `compat/rememberPreference.kt`
**Utilise par** : **TOUS les ecrans settings**
**Statut** : **B** — Utilitaire pur, pas d'UI

### 9. `VegafoXSettingsEntries` — Definitions SettingsEntry VegafoX (B)

**Fichier** : `screen/vegafox/VegafoXSettingsEntries.kt`
**Statut** : **B** — Donnees pures (preference, options, labels)

### 10. `SettingsLabelHelpers` — Helpers label VegafoX (B)

**Fichier** : `screen/vegafox/SettingsLabelHelpers.kt`
**Statut** : **B** — Fonctions @Composable stringResource pures

---

## Ecrans par categorie

### Legende statut

| Code | Signification |
|------|---------------|
| **A** | Nouveau design VegafoX applique (VegafoXColors, VegafoXButton, VegafoXGradients) |
| **B** | Design Jellyfin/neutre — utilise le bridge colorScheme mais aucun token VegafoX explicite |
| **C** | Mix partiel — certains elements VegafoX (VegafoXIcons) mais pas les couleurs/boutons/gradients |

### Quels tokens VegafoX sont utilises dans les settings ?

Analyse de tous les imports :

| Token | Ecrans qui l'utilisent |
|-------|----------------------|
| `VegafoXIcons` | SettingsMainScreen, AuthenticationScreen, CustomizationScreen, PlaybackScreen, HomeScreen, LibrariesScreen, AboutScreen, PluginScreen, JellyseerrScreen, AsyncActionButton |
| `VegafoXColors` | **AUCUN** ecran settings |
| `VegafoXGradients` | **AUCUN** ecran settings |
| `VegafoXButton` | **AUCUN** ecran settings |
| `TvPrimaryButton` / `TvSecondaryButton` | SettingsJellyseerrScreen (dialogs AlertDialog) |
| `JellyfinTheme.colorScheme` | SettingsLayout (fond surface) |
| `Tokens.Space` / `Tokens.Color` | SettingsColumn, AsyncActionButton |

**Conclusion** : Les ecrans settings utilisent VegafoXIcons pour les icones de navigation mais AUCUN autre token VegafoX. Le fond, les couleurs, les boutons viennent tous du bridge `colorScheme` (Jellyfin+VegafoX bridge) ou de Material 3 standard.

---

### CATEGORIE 1 — Ecrans principaux (top-level)

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 1 | **Settings Main** | `/` | `screen/SettingsMainScreen.kt` | **C** | S |
| 2 | **Authentication** | `/authentication` | `screen/authentication/SettingsAuthenticationScreen.kt` | **C** | S |
| 3 | **Customization** | `/customization` | `screen/customization/SettingsCustomizationScreen.kt` | **C** | M |
| 4 | **Plugin** | `/plugin` | `screen/vegafox/SettingsPluginScreen.kt` | **C** | M |
| 5 | **Screensaver** | `/customization/screensaver` | `screen/screensaver/SettingsScreensaverScreen.kt` | **B** | S |
| 6 | **Playback** | `/playback` | `screen/playback/SettingsPlaybackScreen.kt` | **C** | S |
| 7 | **Telemetry** | `/telemetry` | `screen/SettingsTelemetryScreen.kt` | **B** | S |
| 8 | **Developer** | `/developer` | `screen/SettingsDeveloperScreen.kt` | **B** | S |
| 9 | **About** | `/about` | `screen/about/SettingsAboutScreen.kt` | **C** | S |
| 10 | **Libraries** | `/libraries` | `screen/library/SettingsLibrariesScreen.kt` | **C** | S |
| 11 | **Home** | `/home` | `screen/home/SettingsHomeScreen.kt` | **C** | M |
| 12 | **Jellyseerr** | `/jellyseerr` | `screen/vegafox/SettingsJellyseerrScreen.kt` | **C** | L |

**Detail par ecran top-level :**

#### 1. SettingsMainScreen (C)
- **Affiche** : Header VegafoX + 8 boutons navigation (Auth, Custom, Plugin, Screensaver, Playback, Telemetry, Developer, Support+updates, About)
- **Elements VegafoX** : VegafoXIcons pour toutes les icones leading
- **Elements non-VegafoX** : ListButton/ListSection standard, pas de gradient fond, pas de VegafoXButton
- **Particularite** : Section updates conditionnelle (ENABLE_OTA_UPDATES), DonateDialog, UpdateDialogs

#### 2. SettingsAuthenticationScreen (C)
- **Affiche** : Auto sign-in, Sort by, liste des serveurs dynamique, Always authenticate, Pin code
- **Elements VegafoX** : VegafoXIcons (Home, Lock)
- **Elements non-VegafoX** : ListButton/ListSection/Checkbox standard

#### 3. SettingsCustomizationScreen (C)
- **Affiche** : 4 sections (Browsing, Toolbar, Home Behavior, Appearance) avec ~15 items
- **Elements VegafoX** : VegafoXIcons (GridView, Home)
- **Complexite** : Ecran le plus long, beaucoup de preferences, references aux helpers VegafoX

#### 4. SettingsPluginScreen (C)
- **Affiche** : 5 sections (Plugin Sync, Media Bar x5, Theme Music, Ratings x3, Jellyseerr, Parental)
- **Elements VegafoX** : ic_vegafox drawable, VegafoXIcons (Star, Lock)
- **Particularite** : PluginSyncService integration, nombreux sous-reglages conditionels (enabled)

#### 5. SettingsScreensaverScreen (B)
- **Affiche** : In-app toggle, timeout, mode (library/logo), dimming level, age rating, show clock
- **Pas de VegafoXIcons** — pur ListButton/Checkbox/ListSection

#### 6. SettingsPlaybackScreen (C)
- **Affiche** : Video Player, Next Up, Inactivity, Prerolls, Subtitles, Media Segments, SyncPlay, Advanced
- **Elements VegafoX** : VegafoXIcons extensif (TvPlay, NextUp, Sleep, Trailer, Subtitles, etc.)
- **Particularite** : Image Coil du player externe en trailing

#### 7. SettingsTelemetryScreen (B)
- **Affiche** : Crash reports toggle + logs toggle
- **Tres simple** — 2 items seulement, pur standard

#### 8. SettingsDeveloperScreen (B)
- **Affiche** : Debug flag, UI mode, Trick play, FFmpeg, Image cache
- **Pas de VegafoXIcons** — pur standard

#### 9. SettingsAboutScreen (C)
- **Affiche** : Version app, Device model, Licenses link
- **Elements VegafoX** : ic_vegafox drawable, VegafoXIcons (Tv, Guide)
- **Particularite** : copyAction pour copier version dans clipboard

#### 10. SettingsLibrariesScreen (C)
- **Affiche** : Liste dynamique des bibliotheques serveur, multi-server support
- **Elements VegafoX** : VegafoXIcons (Guide, Folder)
- **Particularite** : MultiServerRepository aggregation, LaunchedEffect async

#### 11. SettingsHomeScreen (C)
- **Affiche** : Poster size, Rows image type, Sections ordonnables (checkbox + up/down arrows)
- **Elements VegafoX** : VegafoXIcons (AspectRatio, GridView, Refresh, ArrowUp/Down)
- **Complexite** : Reordonnement avec focus tracking, HomeSectionConfig, key events (Left=up, Right=down)

#### 12. SettingsJellyseerrScreen (C)
- **Affiche** : Mode VegafoX proxy OU mode direct, auth methods, server URL, content prefs, rows config
- **Elements VegafoX** : ic_vegafox, VegafoXIcons extensif, TvPrimaryButton/TvSecondaryButton dans dialogs
- **Complexite** : **L** — ViewModel, dialog states, 5 dialogs AlertDialog, events Flow, mode conditionel
- **Note** : C'est le seul ecran settings utilisant TvPrimaryButton/TvSecondaryButton

---

### CATEGORIE 2 — Sous-ecrans Authentication

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 13 | Auth Server | `/authentication/server/{serverId}` | `authentication/SettingsAuthenticationServerScreen.kt` | **C** | S |
| 14 | Auth Server User | `/authentication/server/{serverId}/user/{userId}` | `authentication/SettingsAuthenticationServerUserScreen.kt` | **C** | S |
| 15 | Auth Sort By | `/authentication/sort-by` | `authentication/SettingsAuthenticationSortByScreen.kt` | **B** | S |
| 16 | Auth Auto Sign-In | `/authentication/auto-sign-in` | `authentication/SettingsAuthenticationAutoSignInScreen.kt` | **B** | S |
| 17 | Auth Pin Code | `/authentication/pin-code` | `authentication/SettingsAuthenticationPinCodeScreen.kt` | **B** | S |

---

### CATEGORIE 3 — Sous-ecrans Customization

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 18 | Theme (Focus Color) | `/customization/theme` | `customization/SettingsCustomizationThemeScreen.kt` | **B** | S |
| 19 | Clock | `/customization/clock` | `customization/SettingsCustomizationClockScreen.kt` | **B** | S |
| 20 | Clock Behavior | (sub-route) | `customization/SettingsClockBehaviorScreen.kt` | **B** | S |
| 21 | Watched Indicator | `/customization/watch-indicators` | `customization/SettingsCustomizationWatchedIndicatorScreen.kt` | **B** | S |
| 22 | Watched Behavior | (sub-route) | `customization/SettingsWatchedIndicatorBehaviorScreen.kt` | **B** | S |

---

### CATEGORIE 4 — Sous-ecrans Screensaver

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 23 | Screensaver Timeout | `/customization/screensaver/timeout` | `screensaver/SettingsScreensaverTimeoutScreen.kt` | **B** | S |
| 24 | Screensaver Mode | `/customization/screensaver/mode` | `screensaver/SettingsScreensaverModeScreen.kt` | **B** | S |
| 25 | Screensaver Dimming | `/customization/screensaver/dimming` | `screensaver/SettingsScreensaverDimmingScreen.kt` | **B** | S |
| 26 | Screensaver Age Rating | `/customization/screensaver/age-rating` | `screensaver/SettingsScreensaverAgeRatingScreen.kt` | **B** | S |

---

### CATEGORIE 5 — Sous-ecrans Subtitles

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 27 | **Subtitles** | `/customization/subtitles` | `subtitle/SettingsSubtitlesScreen.kt` | **B** | M |
| 28 | Subtitles Text Color | `/customization/subtitles/text-color` | `subtitle/SettingsSubtitlesTextColorScreen.kt` | **B** | S |
| 29 | Subtitles Bg Color | `/customization/subtitles/background-color` | `subtitle/SettingsSubtitlesBackgroundColorScreen.kt` | **B** | S |
| 30 | Subtitles Edge Color | `/customization/subtitles/edge-color` | `subtitle/SettingsSubtitleTextStrokeColorScreen.kt` | **B** | S |

**Note** : L'ecran Subtitles utilise des composants avances : `SubtitleStylePreview`, `RangeControl`, `ColorSwatch`, `ListControl` — c'est le sous-ecran le plus complexe visuellement.

---

### CATEGORIE 6 — Sous-ecrans Libraries

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 31 | Library Display | `/libraries/display/{...}` | `library/SettingsLibrariesDisplayScreen.kt` | **B** | S |
| 32 | Library Image Type | `...display/.../image-type` | `library/SettingsLibrariesDisplayImageTypeScreen.kt` | **B** | S |
| 33 | Library Image Size | `...display/.../image-size` | `library/SettingsLibrariesDisplayImageSizeScreen.kt` | **B** | S |
| 34 | Library Grid | `...display/.../grid` | `library/SettingsLibrariesDisplayGridScreen.kt` | **B** | S |

---

### CATEGORIE 7 — Sous-ecrans Home

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 35 | Home Section | `/home/section/{index}` | `home/SettingsHomeSectionScreen.kt` | **B** | S |
| 36 | Home Poster Size | `/home/poster-size` | `home/SettingsHomePosterSizeScreen.kt` | **B** | S |
| 37 | Home Rows Image Type | `/home/rows-image-type` | `vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | **B** | M |

---

### CATEGORIE 8 — Sous-ecrans Live TV

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 38 | Guide Filters | `/livetv/guide/filters` | `livetv/SettingsLiveTvGuideFiltersScreen.kt` | **B** | S |
| 39 | Guide Options | `/livetv/guide/options` | `livetv/SettingsLiveTvGuideOptionsScreen.kt` | **B** | S |
| 40 | Guide Channel Order | `/livetv/guide/channel-order` | `livetv/SettingsLiveTvGuideChannelOrderScreen.kt` | **B** | S |

---

### CATEGORIE 9 — Sous-ecrans Playback

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 41 | Player Selection | `/playback/player` | `playback/SettingsPlaybackPlayerScreen.kt` | **B** | S |
| 42 | Next Up | `/playback/next-up` | `playback/nextup/SettingsPlaybackNextUpScreen.kt` | **B** | S |
| 43 | Next Up Behavior | `/playback/next-up/behavior` | `playback/nextup/SettingsPlaybackNextUpBehaviorScreen.kt` | **B** | S |
| 44 | Inactivity Prompt | `/playback/inactivity-prompt` | `playback/SettingsPlaybackInactivityPromptScreen.kt` | **B** | S |
| 45 | Prerolls | `/playback/prerolls` | `playback/SettingsPlaybackPrerollsScreen.kt` | **B** | S |
| 46 | Media Segments | `/playback/media-segments` | `playback/mediasegment/SettingsPlaybackMediaSegmentsScreen.kt` | **B** | S |
| 47 | Media Segment Detail | `/playback/media-segments/{type}` | `playback/mediasegment/SettingsPlaybackMediaSegmentScreen.kt` | **B** | S |
| 48 | **Playback Advanced** | `/playback/advanced` | `playback/SettingsPlaybackAdvancedScreen.kt` | **B** | L |
| 49 | Resume Subtract | `/playback/resume-subtract-duration` | `playback/SettingsPlaybackResumeSubtractDurationScreen.kt` | **B** | S |
| 50 | Max Bitrate | `/playback/max-bitrate` | `playback/SettingsPlaybackMaxBitrateScreen.kt` | **B** | S |
| 51 | Max Resolution | `/playback/max-resolution` | `playback/SettingsPlaybackMaxResolutionScreen.kt` | **B** | S |
| 52 | Refresh Rate | `/playback/refresh-rate-switching-behavior` | `playback/SettingsPlaybackRefreshRateSwitchingBehaviorScreen.kt` | **B** | S |
| 53 | Zoom Mode | `/playback/zoom-mode` | `playback/SettingsPlaybackZoomModeScreen.kt` | **B** | S |
| 54 | Audio Behavior | `/playback/audio-behavior` | `playback/SettingsPlaybackAudioBehaviorScreen.kt` | **B** | S |

**Note** : Playback Advanced est l'ecran B le plus complexe : RangeControl x4, sous-sections video/audio/troubleshooting, SettingsAsyncActionListButton.

---

### CATEGORIE 10 — Sous-ecrans VegafoX generiques (OptionListScreen)

Tous ces ecrans utilisent le composant generique `OptionListScreen<T>` avec un `SettingsEntry<T>` :

| # | Ecran | Route | Entry | Store | UI | Effort |
|---|-------|-------|-------|-------|-----|--------|
| 55 | Navbar Position | `/vegafox/navbar-position` | navbarPositionEntry | UserPreferences | **B** | S |
| 56 | Shuffle Content | `/vegafox/shuffle-content-type` | shuffleContentTypeEntry | UserPreferences | **B** | S |
| 57 | Media Bar Content | `/vegafox/media-bar-content-type` | mediaBarContentTypeEntry | UserSettingPreferences | **B** | S |
| 58 | Media Bar Count | `/vegafox/media-bar-item-count` | mediaBarItemCountEntry | UserSettingPreferences | **B** | S |
| 59 | Media Bar Opacity | `/vegafox/media-bar-opacity` | mediaBarOpacityEntry | UserSettingPreferences | **B** | S |
| 60 | Media Bar Color | `/vegafox/media-bar-color` | mediaBarColorEntry | UserSettingPreferences | **B** | S |
| 61 | Theme Music Vol | `/vegafox/theme-music-volume` | themeMusicVolumeEntry | UserSettingPreferences | **B** | S |
| 62 | Seasonal Surprise | `/vegafox/seasonal-surprise` | seasonalSurpriseEntry | UserPreferences | **B** | S |
| 63 | Details Blur | `/vegafox/details-blur` | detailsBlurEntry | UserSettingPreferences | **B** | S |
| 64 | Browsing Blur | `/vegafox/browsing-blur` | browsingBlurEntry | UserSettingPreferences | **B** | S |

**Effet cascade** : Migrer `OptionListScreen` = migrer 10 ecrans d'un coup.

---

### CATEGORIE 11 — Sous-ecrans VegafoX specifiques

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 65 | Jellyseerr Rows | `/jellyseerr/rows` | `vegafox/SettingsJellyseerrRowsScreen.kt` | **B** | M |
| 66 | Parental Controls | `/vegafox/parental-controls` | `vegafox/SettingsVegafoXParentalControlsScreen.kt` | **B** | S |
| 67 | SyncPlay | `/vegafox/syncplay` | `vegafox/SettingsVegafoXSyncPlayScreen.kt` | **B** | M |
| 68 | Home Rows Image | `/home/rows-image-type` | `vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | **B** | M |

---

### CATEGORIE 12 — Sous-ecrans SyncPlay numeriques (SettingsNumericScreen)

| # | Ecran | Route | UI | Effort |
|---|-------|-------|-----|--------|
| 69 | Min Delay SpeedToSync | `/vegafox/syncplay/min-delay-speed-to-sync` | **B** | S |
| 70 | Max Delay SpeedToSync | `/vegafox/syncplay/max-delay-speed-to-sync` | **B** | S |
| 71 | Speed Duration | `/vegafox/syncplay/speed-to-sync-duration` | **B** | S |
| 72 | Min Delay SkipToSync | `/vegafox/syncplay/min-delay-skip-to-sync` | **B** | S |
| 73 | Extra Time Offset | `/vegafox/syncplay/extra-time-offset` | **B** | S |

---

### CATEGORIE 13 — Autres

| # | Ecran | Route | Fichier | UI | Effort |
|---|-------|-------|---------|-----|--------|
| 74 | Licenses List | `/licenses` | `license/SettingsLicensesScreen.kt` | **B** | S |
| 75 | License Detail | `/license/{artifactId}` | `license/SettingsLicenseScreen.kt` | **B** | S |

---

## RESUME EXECUTIF

### Statistiques globales

| Metrique | Valeur |
|----------|--------|
| **Total ecrans settings** | **75** |
| **Ecrans A** | **0** (0%) |
| **Ecrans B** | **63** (84%) |
| **Ecrans C** | **12** (16%) |
| **Composants partages** | **10** |

### Repartition detaillee

| Categorie | Total | A | B | C |
|-----------|-------|---|---|---|
| Ecrans principaux (top-level) | 12 | 0 | 3 | 9 |
| Sous-ecrans Authentication | 5 | 0 | 3 | 2 |
| Sous-ecrans Customization | 5 | 0 | 5 | 0 |
| Sous-ecrans Screensaver | 4 | 0 | 4 | 0 |
| Sous-ecrans Subtitles | 4 | 0 | 4 | 0 |
| Sous-ecrans Libraries | 4 | 0 | 4 | 0 |
| Sous-ecrans Home | 3 | 0 | 3 | 0 |
| Sous-ecrans Live TV | 3 | 0 | 3 | 0 |
| Sous-ecrans Playback | 14 | 0 | 14 | 0 |
| VegafoX OptionList generiques | 10 | 0 | 10 | 0 |
| VegafoX specifiques | 4 | 0 | 4 | 0 |
| SyncPlay numeriques | 5 | 0 | 5 | 0 |
| Autres (licenses) | 2 | 0 | 2 | 0 |

### Qu'est-ce que "C" signifie ici ?

Les 12 ecrans "C" (ecrans principaux) utilisent `VegafoXIcons` pour les icones leading dans les ListButton, mais :
- **Pas de VegafoXColors** pour les couleurs de texte ou fond
- **Pas de VegafoXGradients** pour les arriere-plans
- **Pas de VegafoXButton** pour les boutons (sauf JellyseerrScreen avec TvPrimaryButton dans les dialogs)
- Le fond et les couleurs viennent du **bridge colorScheme** qui mappe deja les couleurs VegafoX

Les 63 ecrans "B" n'utilisent meme pas VegafoXIcons.

---

## Composants partages a migrer en premier (effet cascade)

| Priorite | Composant | Impact | Effort |
|----------|-----------|--------|--------|
| **1** | `SettingsLayout` | 1 ecran mais cadre global de tous les settings | S |
| **2** | `OptionListScreen` | **10 ecrans** d'un coup (navbar, shuffle, media bar, blur, seasonal, theme music) | S |
| **3** | `SettingsNumericScreen` | **5 ecrans** SyncPlay | S |
| **4** | `ListButton` / `ListSection` (dans design module) | **75 ecrans** — mais c'est le design system, pas les settings | L |

> **Point cle** : La migration des settings est en fait une question de **design system**. Les ecrans settings n'ont quasiment aucune logique visuelle propre — ils composent des ListButton, ListSection, Checkbox, RadioButton, RangeControl du module `ui/base/list/`. Migrer ces composants de base = migrer automatiquement tous les ecrans settings.

---

## Effort par ecran

| Taille | Critere | Ecrans |
|--------|---------|--------|
| **S** | ListButton/Checkbox standard, < 50 lignes, aucun composant custom | 60 ecrans |
| **M** | Logique de reordonnement, sections conditionnelles, RangeControl, 50-150 lignes | 8 ecrans (Customization, Plugin, Home, JellyseerrRows, SyncPlay, HomeRowsImage, Subtitles, PlaybackAdvanced) |
| **L** | ViewModel, dialogs AlertDialog, logique async, > 150 lignes | 2 ecrans (Jellyseerr, PlaybackAdvanced) |

---

## Ordre de migration recommande

### Phase 1 — Design system settings (cascade maximale)

| Etape | Action | Impact |
|-------|--------|--------|
| 1a | Migrer `SettingsLayout` — appliquer VegafoXGradients au fond du dialog | Fond de tous les settings |
| 1b | Migrer `OptionListScreen` — VegafoXColors dans les RadioButton, header | 10 ecrans generiques |
| 1c | Migrer `SettingsNumericScreen` — meme approche | 5 ecrans SyncPlay |
| 1d | Migrer `SettingsColumn` — spacing/padding VegafoX si necessaire | Tous les ecrans |

**Estimation** : 1 session

### Phase 2 — Ecrans top-level (les plus visibles)

| Etape | Ecran | Detail |
|-------|-------|--------|
| 2a | SettingsMainScreen | Le premier ecran vu — ajouter header VegafoX, gradient |
| 2b | SettingsPlaybackScreen | Frequemment utilise |
| 2c | SettingsCustomizationScreen | Le plus long ecran |
| 2d | SettingsAuthenticationScreen | Important pour nouveau users |
| 2e | SettingsPluginScreen | Identitaire VegafoX |
| 2f | SettingsAboutScreen | Branding VegafoX |

**Estimation** : 1-2 sessions

### Phase 3 — Sous-ecrans complexes

| Etape | Ecran | Detail |
|-------|-------|--------|
| 3a | SettingsJellyseerrScreen | Le plus complexe (L), dialogs a migrer |
| 3b | SettingsPlaybackAdvancedScreen | RangeControl x4, sections multiples |
| 3c | SettingsSubtitlesScreen | Preview custom, RangeControl, ColorSwatch |
| 3d | SettingsHomeScreen | Reordonnement sections |
| 3e | SettingsJellyseerrRowsScreen | Reordonnement rows |

**Estimation** : 2-3 sessions

### Phase 4 — Sous-ecrans simples restants

| Action | Nb ecrans |
|--------|-----------|
| Batch-migrer les ecrans B restants (Screensaver, Customization sub, Library, LiveTV, Playback sub) | ~50 ecrans |

**Estimation** : 2-3 sessions (beaucoup d'ecrans mais tous triviaux, pattern identique)

### Total estime : 6-9 sessions

---

## Notes techniques

### Comment les settings sont ouverts

Le `SettingsViewModel` controle la visibilite :
```kotlin
class SettingsViewModel : ViewModel() {
    val visible = MutableStateFlow(false)
    fun show() { visible.value = true }
    fun hide() { visible.value = false }
}
```

L'icone engrenage dans la sidebar appelle `viewModel.show()`, ce qui affiche le `SettingsDialog` en overlay par-dessus l'ecran principal (Home, Browse, etc.)

### Crash observe

Lors de la tentative de screenshot, l'ouverture des settings a cause un crash : "Oups ! Il y a eu un probleme. Un rapport de plantage a ete envoye a ton serveur Jellyfin." — Cela peut etre lie au build debug actuel ou a un probleme dans le SettingsViewModel/Koin init. A investiguer separement.

### Screenshots

- `docs/screenshots/settings_home_state.png` — Home screen avec sidebar VegafoX (contexte)
- Screenshots des settings non captures (crash a l'ouverture du dialog)
