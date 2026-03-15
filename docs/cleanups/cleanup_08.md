# Cleanup 08 — Audit code + suppression orphelins

**Date** : 2026-03-13
**Scope** : Code dupliqué, magic numbers, assets, thèmes legacy, destinations mortes, Koin beans orphelins
**Basé sur** : cleanup_07.md, audit_final.md

---

## ÉTAPE 1 — Code dupliqué (audit)

### Duplications confirmées

| Fonction | Sévérité | Type | Fichiers | Action recommandée |
|----------|----------|------|----------|-------------------|
| `getBackdropUrl()` | HIGH | Logique dupliquée | DetailUtils.kt (public) + HomeHeroBackdrop.kt:459 (private) | Supprimer la version privée, utiliser DetailUtils |
| `withServerId()` | HIGH | Duplication exacte | SearchRepository.kt:40 + MultiServerRepository.kt:101 | Extraire en extension partagée BaseItemExtensions.kt |
| `copyWithTimerId()` | MEDIUM | Collision de nom | JavaCompat.kt:30 (seriesTimerId) + LiveTvRecordingApi.kt:15 (timerId) | Renommer JavaCompat → `copyWithSeriesTimerId()` |
| `dpToPx()` | LOW | Doublon local | SimpleInfoRowView.kt:285 (private) vs Utils.convertDpToPixel/NumberExtensions.dp() | Remplacer par `Int.dp(context)` |

### Non-dupliqués (bien organisés)

- **TimeUtils.kt** : `formatMillis()`, `formatRuntimeHoursMinutes()`, `formatSeconds()`, `getFriendlyDate()` — centralisé
- **DetailUtils.kt** : `formatDuration(ticks)`, `getEndsAt(ticks)` — spécifique Live TV, pas de doublon
- **BaseItemImageExtensions.kt** : `getPrimaryImage()`, `getLogoImage()`, etc. — consolidé proprement

---

## ÉTAPE 2 — Magic numbers (audit)

### Hotspots critiques

#### 2.1 Dialog max height — 400.dp × 5 (VideoPlayerDialogs.kt)

| Ligne | Valeur | Contexte |
|-------|--------|----------|
| 141 | 400.dp | Subtitle dialog |
| 175 | 400.dp | Audio track dialog |
| 209 | 400.dp | Quality dialog |
| 243 | 400.dp | Speed dialog |
| 271 | 400.dp | Chapter dialog |

**Action** : Extraire `DialogDimensions.maxHeight = 400.dp`

#### 2.2 Dream screensaver (DreamContent*.kt)

| Fichier | Ligne | Valeur | Signification |
|---------|-------|--------|---------------|
| DreamContentLogo.kt | 27 | 400.dp | Largeur logo |
| DreamContentLogo.kt | 28 | 200.dp | Hauteur logo |
| DreamContentNowPlaying.kt | 94 | 250.dp | Fading edges vertical |
| DreamContentNowPlaying.kt | 113 | 128.dp | Album cover size |
| DreamHeader.kt | 32 | 150.dp | Clock widget width |
| DreamContentLibraryShowcase.kt | 60 | 75.dp | Carousel max height |

**Action** : Créer `DreamDimensions` object dans `ui/base/theme/`

#### 2.3 Boutons (VegafoXButton.kt)

| Ligne | Valeur | Condition |
|-------|--------|-----------|
| 164 | 200.dp | minWidth (normal) |
| 164 | 120.dp | minWidth (compact) |
| 165 | 52.dp | minHeight (normal) |
| 165 | 40.dp | minHeight (compact) |

**Action** : Consolider dans `TvSpacing`

#### 2.4 Live TV Guide incohérence

- `Spacing.kt` définit : `programCellHeight = 55.dp`
- `LiveTvGuideScreen.kt:258` utilise : `120.dp`
- Différence de 65dp à auditer

### Typographie (SP >= 20sp)

| Fichier | Ligne | Valeur | Recommandation |
|---------|-------|--------|----------------|
| VideoPlayerHeader.kt | 85 | 36.sp | Utiliser HeadlineLarge (32.sp) |
| AllFavoritesScreen.kt | 198 | 40.sp | Créer DisplaySmall (40.sp) |

### Statistiques

- **73 fichiers** avec dp >= 50dp
- **~227 occurrences** de valeurs hardcodées > 50dp
- **TvSpacing** actuel : incomplet (manque button widths, dialog heights, card widths)

---

## ÉTAPE 3 — Assets orphelins (audit)

### Assets directory
- **Statut** : N'EXISTE PAS — 0 orphelin

### Raw resources (res/raw/)
- **Statut** : N'EXISTE PAS — 0 orphelin

### Fonts (res/font/)

| Fichier | Statut | Réfs | Usage |
|---------|--------|------|-------|
| bebas_neue.ttf | ACTIF | 104 | VegafoXTypography.kt + 43 fichiers Kotlin |
| jetbrains_mono.ttf | ACTIF | 5 | VegafoXTypography.kt + AudioNowPlayingScreen.kt |

**Résultat** : 0 orphelin. Tous les assets sont utilisés.

---

## ÉTAPE 4 — Thèmes Jellyfin legacy (audit)

### Inventaire des thèmes

| Thème | Fichier | Parent | Classification |
|-------|---------|--------|---------------|
| Theme.Jellyfin | theme_jellyfin.xml | Theme.AppCompat.NoActionBar | **ACTIF** |
| Theme.Jellyfin.Splash | theme_jellyfin.xml | Theme.SplashScreen | **ACTIF** |
| Theme.Jellyfin.Dialog | theme_jellyfin.xml | Theme.AppCompat.Dialog | **ACTIF** |
| Theme.Jellyfin.Emerald | theme_emerald.xml | Theme.Jellyfin | **ORPHELIN** |
| Theme.Jellyfin.MutedPurple | theme_mutedpurple.xml | Theme.Jellyfin | **ORPHELIN** |

### Détails des références

**Theme.Jellyfin** (ACTIF — 3 réfs) :
- AndroidManifest.xml ligne 62 : `android:theme="@style/Theme.Jellyfin"` (global app)
- ActivityThemeExtensions.kt : `setTheme(R.style.Theme_Jellyfin)`
- CreatePlaylistDialogFragment.kt : `R.style.Theme_Jellyfin_Dialog`

**Theme.Jellyfin.Splash** (ACTIF — 2 réfs) :
- AndroidManifest.xml ligne 112 : StartupActivity
- theme_jellyfin.xml : postSplashScreenTheme

**Theme.Jellyfin.Dialog** (ACTIF — 2 réfs) :
- theme_jellyfin.xml : dialogTheme
- CreatePlaylistDialogFragment.kt

**Theme.Jellyfin.Emerald** (ORPHELIN — 0 réf Kotlin) :
- Aucune référence code. Seules les traductions `pref_theme_emerald` existent (46 langues)
- Fichier theme_emerald.xml : 47 lignes

**Theme.Jellyfin.MutedPurple** (ORPHELIN — 0 réf Kotlin) :
- Aucune référence code. Seules les traductions `pref_theme_muted_purple` existent (46 langues)
- Fichier theme_mutedpurple.xml : 31 lignes

### Contexte

Le système moderne de thèmes utilise `AppTheme` (13 couleurs focus : WHITE, BLACK, GRAY, DARK_BLUE, PURPLE, TEAL, etc.) via `UserSettingPreferences.focusColor`. Le theming est appliqué dynamiquement en Compose. Les thèmes Emerald et MutedPurple sont des vestiges de l'ancien système de sélection de thèmes qui n'existe plus.

**Recommandation** : Supprimer theme_emerald.xml (47 lignes) et theme_mutedpurple.xml (31 lignes) + couleurs associées + traductions dans un futur cleanup.

---

## ÉTAPE 5 — Destinations mortes (supprimées)

### Destinations supprimées (10)

| Destination | Fragment cible | Raison |
|-------------|---------------|--------|
| `collectionBrowser()` | CollectionBrowseFragment | Jamais navigué via Destinations |
| `libraryByLetter()` | ByLetterBrowseFragment | Jamais navigué via Destinations |
| `librarySuggestions()` | SuggestedMoviesComposeFragment | Jamais navigué via Destinations |
| `musicFavorites()` | MusicFavoritesListFragment | Jamais navigué via Destinations |
| `nextUp()` | NextUpFragment | Destination jamais appelée |
| `stillWatching()` | StillWatchingFragment | Destination jamais appelée |
| `serverDiscovery` | ServerDiscoveryFragment | Navigation directe par fragment transactions |
| `quickConnect()` | QuickConnectFragment | Navigation directe par fragment transactions |
| `jellyseerrRequests` | RequestsFragment | Destination jamais appelée |
| `jellyseerrSettings` | JellyseerrSettingsFragment | Destination jamais appelée |

**Imports supprimés** : 11 imports morts nettoyés

### Destinations actives (22 restantes)

home, search, libraryBrowser (×2), liveTvBrowser, musicBrowser, folderBrowser, allGenres, allFavorites, folderView, genreBrowse, libraryByGenres, itemDetails, channelDetails, seriesTimerDetails, itemList, trailerPlayer, liveTvGuide, liveTvSchedule, liveTvRecordings, liveTvSeriesRecordings, nowPlaying, photoPlayer, videoPlayer, jellyseerrDiscover, jellyseerrBrowseBy, jellyseerrBrowseByGenre, jellyseerrBrowseByNetwork, jellyseerrBrowseByStudio, jellyseerrMediaDetails, jellyseerrPersonDetails

**Build** : BUILD SUCCESSFUL

---

## ÉTAPE 6 — Koin beans orphelins (supprimés)

### Modules audités (6 fichiers)

| Module | Fichier |
|--------|---------|
| appModule | di/AppModule.kt |
| playbackModule | di/PlaybackModule.kt |
| utilsModule | di/UtilsModule.kt |
| androidModule | di/AndroidModule.kt |
| authModule | di/AuthModule.kt |
| preferenceModule | di/PreferenceModule.kt |

### Beans supprimés (3)

| Bean | Scope | Module | Raison |
|------|-------|--------|--------|
| `MarkdownRenderer` | single | AppModule | Jamais injecté. Classe utilisée uniquement dans sa propre définition. |
| `LocalWatchlistRepository` | single | AppModule | Jamais injecté. Classe utilisée uniquement dans sa propre définition. |
| `KeyProcessor` | factory | AppModule | Jamais injecté. KeyProcessorHelper.kt ne le référence pas via Koin. |

### Beans actifs (~70+ déclarations)

Tous les autres beans sont actifs et injectés :
- **AppModule** : 53 beans (SDK, Coil, repositories, ViewModels, services)
- **PlaybackModule** : 7 beans (LegacyPlaybackManager, VideoQueueManager, etc.)
- **UtilsModule** : 1 bean (ImageHelper)
- **AndroidModule** : 3 beans (UiModeManager, AudioManager, WorkManager)
- **AuthModule** : 7 beans (AuthenticationStore, repositories, SessionRepository)
- **PreferenceModule** : 7 beans (PluginSyncService, PreferencesRepository, etc.)

**Build** : BUILD SUCCESSFUL

---

## Résumé

| Étape | Type | Résultat |
|-------|------|----------|
| 1. Code dupliqué | Audit | 4 duplications identifiées (2 HIGH, 1 MEDIUM, 1 LOW) |
| 2. Magic numbers | Audit | ~227 valeurs > 50dp dans 73 fichiers. 5 hotspots critiques. |
| 3. Assets orphelins | Audit | 0 orphelin. 2 fonts actives. |
| 4. Thèmes legacy | Audit | 3 actifs, 2 orphelins (Emerald + MutedPurple = 78 lignes) |
| 5. Destinations mortes | Suppression | **10 destinations supprimées + 11 imports** |
| 6. Koin beans orphelins | Suppression | **3 beans supprimés** (MarkdownRenderer, LocalWatchlistRepository, KeyProcessor) |

### LOC supprimées ce round

~95 LOC (10 destinations + 3 beans Koin + imports associés)

### LOC total tous cleanups

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3,124 |
| 05 | ~1,581 + 6,751 traductions |
| 06 | ~369 |
| 07 | ~361 + 900 KB images |
| **08** | **~95** |
| **Total** | **~6,105 LOC + 6,751 traductions + 900 KB images** |

### Build

- Debug (github) : BUILD SUCCESSFUL
- Release (github) : BUILD SUCCESSFUL
- Installé sur AM9 Pro : Success
