# Cartographie du projet Moonfin Android TV

> **Mis à jour le 2026-03-08 — état post-travaux**
> Audit descriptif. Les problèmes identifiés (couleurs hardcodées, textes, tokens sous-utilisés) ont été traités dans les audits 02-15.
> - ✅ Couleurs hardcodées : 131 → 2 restantes (Color.parseColor legacy)
> - ✅ Textes hardcodés : ~150 → 27 restants (Jellyseerr statuts)
> - ✅ Design tokens sous-utilisés → Design system implémenté à 95%+
> - ✅ TypographyTokens dp bug → Corrigé (dp → sp)
> - ✅ RadiusTokens 128dp → Remplacé par radii sémantiques (4-999dp)
> - ⚠️ Partiel : Theme MutedPurple (#212121 inline), 3 paradigmes UI coexistent toujours
> - ⚠️ Partiel : GenreCardPresenter Color.parseColor, ItemDetailsFragment Color.parseColor
> - En attente : Migration Jellyseerr vers Compose (P3 long terme)

## Resume executif

Moonfin est un fork de Jellyfin Android TV (package `org.moonfin.androidtv`) ciblant Android 14+ sur hardware haut de gamme (Ugoos AM9+ Pro). Le projet comprend **7 modules Gradle**, **599 fichiers Kotlin** + 32 Java legacy, **193 fichiers Compose** (458 `@Composable`), et 50 layouts XML. L'architecture repose sur Koin (DI), Leanback + Jetpack Compose (migration en cours), et ExoPlayer/Media3 avec FFmpeg. Le module `design` fournit des tokens (couleurs, typographie, espacement, rayons) mais ils sont **massivement ignores** : le code Jellyseerr utilise ~100+ couleurs hardcodees `Color.parseColor("#...")`, MainToolbar en ajoute 16+, et 37+ fichiers ignorent le systeme typographique. On recense **~150+ textes UI hardcodes** repartis sur 20+ fichiers Kotlin et 7 layouts XML. Le theme Muted Purple est incomplet (1/20 attributs). L'heterogeneite entre ecrans Leanback legacy, ecrans Compose v2, et ecrans Jellyseerr imperatifs (`addView()`) constitue le probleme visuel principal. **4 sources de couleurs** coexistent sans lien entre elles.

---

## 1. Modules Gradle

| Module | Type | Role | Dependencies cles |
|--------|------|------|-------------------|
| `:app` | Application | Module principal, toute l'UI, la logique metier, les ecrans | Tous les modules, Jellyfin SDK, Koin, Compose, Leanback, Media3, Coil, Ktor, ACRA |
| `:design` | Library | Design tokens (couleurs, typographie, espacement, rayons) | `androidx-compose-ui-graphics` uniquement |
| `:playback:core` | Library | Abstraction du lecteur multimedia, modeles de lecture | Coroutines, AndroidX Core, Timber |
| `:playback:jellyfin` | Library | Implementation Jellyfin du lecteur (API, sessions) | `:playback:core`, Jellyfin SDK, Lifecycle |
| `:playback:media3:exoplayer` | Library | Implementation ExoPlayer/Media3 + FFmpeg + libass (ASS/SSA) | `:playback:core`, Media3, FFmpeg decoder, libass (`ass-media`, `ass-kt`) |
| `:playback:media3:session` | Library | Session multimedia MediaSession | `:playback:core`, Media3 Session |
| `:preference` | Library | Systeme de preferences avec ViewBinding | Coroutines, Timber |

**Configuration globale :**
- `compileSdk` / `targetSdk` = **36** (Android 16)
- `minSdk` = **23** (Android 6.0)
- Kotlin **2.3.10**, Java **21**
- Detekt pour l'analyse statique
- ProGuard/R8 active en debug ET release (minifyEnabled = true)
- 2 flavors : `github` (OTA updates ON) et `playstore` (OTA OFF)

---

## 2. Bibliotheques tierces

### Core
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| Jellyfin SDK | 1.8.6 | Communication serveur Jellyfin/Emby |
| Kotlin | 2.3.10 | Langage principal |
| Kotlinx Coroutines | 1.10.2 | Asynchrone |
| Kotlinx Serialization | 1.10.0 | JSON |

### UI
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| AndroidX Leanback | 1.2.0 | UI Android TV (BrowseFragment, RowsFragment, Presenters) |
| Jetpack Compose Foundation | 1.10.2 | UI declarative (migration) |
| Compose Material3 | 1.3.1 | Composants Material Design 3 |
| AndroidX TV Material | 1.0.0 | Composants TV specifiques |
| AndroidX Navigation3 UI | 1.0.0 | Navigation (nouveau systeme) |
| Accompanist Permissions | 0.37.3 | Gestion permissions runtime |

### Media
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| Media3 ExoPlayer | 1.9.2 | Lecteur video/audio principal |
| Media3 HLS | 1.9.2 | Streaming HLS |
| Media3 FFmpeg Decoder | 1.9.0+1 | Decodage FFmpeg (fork Jellyfin) |
| libass (ass-media/ass-kt) | 0.4.0-beta01 | Sous-titres ASS/SSA |
| NewPipe Extractor | v0.26.0 | Resolution YouTube (n-parameter descrambling) |

### Image & Reseau
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| Coil 3 | 3.3.0 | Chargement images (compose, gif, svg, okhttp) |
| Ktor | 3.0.3 | Client HTTP pour Jellyseerr |
| Markwon | 4.6.2 | Rendu Markdown |

### DI & Utilitaires
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| Koin | 4.1.1 | Injection de dependances |
| Timber | 5.0.1 | Logging |
| ACRA | 5.13.1 | Crash reporting |
| AboutLibraries | 13.2.1 | Licences |

### Tests
| Bibliotheque | Version | Usage |
|-------------|---------|-------|
| Kotest | 6.1.3 | Framework de test |
| MockK | 1.14.9 | Mocking |

---

## 3. Packages principaux

```
org.jellyfin.androidtv/
├── auth/           Authentification, modeles serveur, sessions, QuickConnect
│   ├── model/      MediaServerType, Server, AuthenticationStoreServer
│   └── repository/ ServerRepository, SessionRepository
├── constant/       Constantes applicatives
├── data/           Couche donnees
│   ├── eventhandling/  WebSocket (SocketHandler)
│   ├── model/          Modeles de donnees
│   ├── repository/     Repositories (items, users, notifications, updates)
│   ├── service/        Services (BackgroundService)
│   └── syncplay/       SyncPlay (lecture synchronisee)
├── di/             Modules Koin (AppModule, etc.)
├── integration/    Integrations systeme
│   └── dream/      DreamService (ecran de veille Android TV)
├── preference/     Preferences utilisateur (SharedPreferences wrappers)
├── telemetry/      Telemetrie (ACRA, crash reports)
├── ui/             Interface utilisateur (detail ci-dessous)
└── util/           Utilitaires (extensions, formatters, SDK helpers)
```

### Sous-packages `ui/`

```
ui/
├── background/        Arriere-plan anime (AppBackground)
├── base/              Composants Compose de base (Button, Text, Icon, Badge, Dialog, List, Seekbar, Popover, JellyfinTheme)
├── browsing/          Navigation bibliotheque
│   ├── v2/            Nouvelle version (Compose: Library, Music, LiveTV, Genres, Recordings, Schedule)
│   ├── genre/         Grilles de genres (legacy)
│   └── composable/    Composables partages (InfoRow, rating)
├── card/              Cartes utilisateur (UserCardView)
├── composable/        Composables partages (AsyncImage, ZoomBox, LyricsBox, ItemCard, ItemPreview)
├── home/              Ecran d'accueil
│   └── mediabar/      Barre media hero (slideshow, trailers YouTube, SponsorBlock)
├── itemdetail/        Details d'un element
│   └── v2/            Nouvelle version Compose (ItemDetailsFragment, ItemDetailsComponents)
├── itemhandling/      Gestion des items (adapters, row models)
├── jellyseerr/        Integration Jellyseerr complete (Discover, Details, Requests, Settings, Person)
├── livetv/            TV en direct (guide, programmes)
├── navigation/        Routeur de navigation (Destinations, router.kt)
├── notification/      Notifications in-app
├── playback/          Lecture video/audio
│   ├── overlay/       Controles de lecture (actions, seek, trickplay)
│   ├── nextup/        Ecran "Next Up"
│   ├── stillwatching/ Ecran "Still Watching?"
│   └── rewrite/       Nouveau MediaManager
├── player/            Nouveau lecteur
│   ├── video/         Lecteur video (VideoPlayerFragment, Controls, Overlay)
│   ├── photo/         Lecteur photo
│   └── base/          Composants partages (PlayerSurface, Seekbar, Subtitles, Toast)
├── playlist/          Gestion playlists (AddToPlaylist, CreatePlaylist)
├── preference/        Ecrans de preferences (legacy Leanback)
├── presentation/      Presenters Leanback (Card, Row, Grid, Details)
├── screensaver/       Ecran de veille in-app
├── search/            Recherche (SearchFragment, voice input)
├── settings/          Parametres (Compose)
│   ├── screen/        Tous les ecrans de parametres
│   └── composable/    Composants settings partages
├── shared/            Composants partages
│   └── toolbar/       Barre d'outils (MainToolbar, LeftSidebar, StartupToolbar)
├── shuffle/           Options de lecture aleatoire
├── startup/           Ecrans de demarrage (Splash, SelectServer, Login, ServerAdd)
│   └── fragment/      Fragments de startup
└── syncplay/          SyncPlay UI (dialog, controls)
```

---

## 4. Activities, Fragments et ViewModels

### Activities (3)

| Classe | Fichier | Role |
|--------|---------|------|
| `StartupActivity` | `ui/startup/StartupActivity.kt:51` | Point d'entree, splash, selection serveur, login |
| `MainActivity` | `ui/browsing/MainActivity.kt:50` | Activite principale post-login, navigation, OTA updates |
| `ExternalPlayerActivity` | `ui/playback/ExternalPlayerActivity.kt:46` | Lecture via lecteur externe |

### Fragments principaux (~45)

#### Home & Navigation
| Classe | Fichier | Role |
|--------|---------|------|
| `HomeFragment` | `ui/home/HomeFragment.kt:38` | Ecran d'accueil principal |
| `HomeRowsFragment` | `ui/home/HomeRowsFragment.kt:75` | Lignes de contenu (RowsSupportFragment) |

#### Browsing - Legacy (Leanback)
| Classe | Fichier | Role |
|--------|---------|------|
| `BrowseFolderFragment` | `ui/browsing/BrowseFolderFragment.kt:31` | Base abstraite (BrowseSupportFragment) |
| `EnhancedBrowseFragment` | `ui/browsing/` | Extension de BrowseFolderFragment |
| `CollectionFragment` | `ui/browsing/CollectionFragment.kt:9` | Collections |
| `AllFavoritesFragment` | `ui/browsing/AllFavoritesFragment.kt:24` | Favoris |
| `AllGenresFragment` | `ui/browsing/AllGenresFragment.kt:20` | Tous les genres |
| `FolderViewFragment` | `ui/browsing/FolderViewFragment.kt:18` | Vue dossier |
| `GenericFolderFragment` | `ui/browsing/GenericFolderFragment.kt:13` | Dossier generique |
| `SuggestedMoviesFragment` | `ui/browsing/SuggestedMoviesFragment.kt:18` | Films suggeres |
| `ByLetterFragment` | `ui/browsing/ByLetterFragment.kt:10` | Navigation par lettre |
| `ByGenreFragment` | `ui/browsing/ByGenreFragment.kt:13` | Navigation par genre |

#### Browsing - v2 (Compose)
| Classe | Fichier | Role |
|--------|---------|------|
| `LibraryBrowseFragment` | `ui/browsing/v2/LibraryBrowseFragment.kt:76` | Navigateur bibliotheque v2 |
| `MusicBrowseFragment` | `ui/browsing/v2/MusicBrowseFragment.kt:76` | Navigateur musique |
| `LiveTvBrowseFragment` | `ui/browsing/v2/LiveTvBrowseFragment.kt:72` | TV en direct |
| `RecordingsBrowseFragment` | `ui/browsing/v2/RecordingsBrowseFragment.kt:70` | Enregistrements |
| `SeriesRecordingsBrowseFragment` | `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt:62` | Enregistrements series |
| `ScheduleBrowseFragment` | `ui/browsing/v2/ScheduleBrowseFragment.kt:70` | Programmation |
| `GenresGridV2Fragment` | `ui/browsing/v2/GenresGridV2Fragment.kt:84` | Grille genres v2 |
| `GenresGridFragment` | `ui/browsing/genre/GenresGridFragment.kt:61` | Grille genres legacy |
| `GenreBrowseFragment` | `ui/browsing/genre/GenreBrowseFragment.kt:64` | Navigation genre |

#### Details
| Classe | Fichier | Role |
|--------|---------|------|
| `ItemDetailsFragment` | `ui/itemdetail/v2/ItemDetailsFragment.kt:130` | Details element (Compose, 2000+ lignes) |
| `TrailerPlayerFragment` | `ui/itemdetail/v2/TrailerPlayerFragment.kt:36` | Lecteur trailers |

#### Jellyseerr
| Classe | Fichier | Role |
|--------|---------|------|
| `DiscoverFragment` | `ui/jellyseerr/DiscoverFragment.kt:34` | Decouverte Jellyseerr |
| `JellyseerrDiscoverRowsFragment` | `ui/jellyseerr/JellyseerrDiscoverRowsFragment.kt:40` | Lignes decouverte (RowsSupportFragment) |
| `JellyseerrBrowseByFragment` | `ui/jellyseerr/JellyseerrBrowseByFragment.kt:58` | Navigation par filtre |
| `MediaDetailsFragment` | `ui/jellyseerr/MediaDetailsFragment.kt:69` | Details media Jellyseerr |
| `PersonDetailsFragment` | `ui/jellyseerr/PersonDetailsFragment.kt:49` | Details personne |
| `RequestsFragment` | `ui/jellyseerr/RequestsFragment.kt:14` | Liste des requetes |
| `SettingsFragment` (Jellyseerr) | `ui/jellyseerr/SettingsFragment.kt:23` | Parametres Jellyseerr |

#### Playback
| Classe | Fichier | Role |
|--------|---------|------|
| `VideoPlayerFragment` | `ui/player/video/VideoPlayerFragment.kt:20` | Lecteur video |
| `PhotoPlayerFragment` | `ui/player/photo/PhotoPlayerFragment.kt:16` | Lecteur photo |
| `NextUpFragment` | `ui/playback/nextup/NextUpFragment.kt:236` | Ecran "Next Up" |
| `StillWatchingFragment` | `ui/playback/stillwatching/StillWatchingFragment.kt:243` | Ecran "Still Watching?" |

#### Startup
| Classe | Fichier | Role |
|--------|---------|------|
| `SplashFragment` | `ui/startup/fragment/SplashFragment.kt:47` | Ecran splash |
| `SelectServerFragment` | `ui/startup/fragment/SelectServerFragment.kt:56` | Selection serveur |
| `ServerAddFragment` | `ui/startup/fragment/ServerAddFragment.kt:24` | Ajout serveur |
| `ServerFragment` | `ui/startup/fragment/ServerFragment.kt:48` | Vue serveur |
| `UserLoginFragment` | `ui/startup/fragment/UserLoginFragment.kt:27` | Login utilisateur |
| `UserLoginCredentialsFragment` | `ui/startup/fragment/UserLoginCredentialsFragment.kt:27` | Saisie credentials |
| `UserLoginQuickConnectFragment` | `ui/startup/fragment/UserLoginQuickConnectFragment.kt:31` | QuickConnect |
| `ConnectHelpAlertFragment` | `ui/startup/fragment/ConnectHelpAlertFragment.kt:110` | Aide connexion |

#### Divers
| Classe | Fichier | Role |
|--------|---------|------|
| `SearchFragment` | `ui/search/SearchFragment.kt:43` | Recherche globale |
| `CreatePlaylistDialogFragment` | `ui/playlist/CreatePlaylistDialogFragment.kt:34` | Creation playlist |
| `StartupToolbarFragment` | `ui/startup/fragment/StartupToolbarFragment.kt:17` | Toolbar startup |
| `BaseFragment` | `ui/base/BaseFragment.kt:18` | Base abstraite pour tous les fragments |

### ViewModels (22)

| Classe | Fichier | Role |
|--------|---------|------|
| `ThemeViewModel` | `util/ActivityThemeExtensions.kt:16` | Gestion theme applicatif |
| `InteractionTrackerViewModel` | `ui/InteractionTrackerViewModel.kt:21` | Tracking interactions utilisateur |
| `StartupViewModel` | `ui/startup/StartupViewModel.kt:24` | Logique startup |
| `ServerAddViewModel` | `ui/startup/ServerAddViewModel.kt:12` | Ajout serveur |
| `UserLoginViewModel` | `ui/startup/UserLoginViewModel.kt:38` | Login (credentials + QuickConnect) |
| `SearchViewModel` | `ui/search/SearchViewModel.kt:23` | Logique recherche |
| `ItemDetailsViewModel` | `ui/itemdetail/v2/ItemDetailsViewModel.kt:53` | Details element (API, badges, favoris) |
| `PhotoPlayerViewModel` | `ui/player/photo/PhotoPlayerViewModel.kt:25` | Lecteur photo |
| `SyncPlayViewModel` | `ui/syncplay/SyncPlayViewModel.kt:7` | Lecture synchronisee |
| `NextUpViewModel` | `ui/playback/nextup/NextUpViewModel.kt:8` | Donnees "Next Up" |
| `StillWatchingViewModel` | `ui/playback/stillwatching/StillWatchingViewModel.kt:9` | Donnees "Still Watching" |
| `PlaybackPromptViewModel` | `ui/playback/common/PlaybackPromptViewModel.kt:40` | Base abstraite prompts lecture |
| `JellyseerrViewModel` | `ui/jellyseerr/JellyseerrViewModel.kt:36` | Logique Jellyseerr complete |
| `SettingsViewModel` | `ui/settings/compat/SettingsViewModel.kt:10` | Preferences (compat) |
| `DreamViewModel` | `integration/dream/DreamViewModel.kt:45` | Ecran de veille |
| `MediaBarSlideshowViewModel` | `ui/home/mediabar/MediaBarSlideshowViewModel.kt:42` | Slideshow barre hero |
| `LibraryBrowseViewModel` | `ui/browsing/v2/LibraryBrowseViewModel.kt:74` | Navigation bibliotheque v2 |
| `MusicBrowseViewModel` | `ui/browsing/v2/MusicBrowseViewModel.kt:35` | Navigation musique |
| `GenresGridViewModel` | `ui/browsing/v2/GenresGridViewModel.kt:55` | Grille genres |
| `LiveTvBrowseViewModel` | `ui/browsing/v2/LiveTvBrowseViewModel.kt:40` | TV en direct |
| `RecordingsBrowseViewModel` | `ui/browsing/v2/RecordingsBrowseViewModel.kt:35` | Enregistrements |
| `ScheduleBrowseViewModel` | `ui/browsing/v2/ScheduleBrowseViewModel.kt:36` | Programmation |
| `SeriesRecordingsBrowseViewModel` | `ui/browsing/v2/SeriesRecordingsBrowseViewModel.kt:24` | Enregistrements series |

---

## 5. Composants Android TV / Leanback

**53 fichiers** importent `androidx.leanback`. Principaux usages :

### Fragments Leanback
- `BrowseSupportFragment` : `BrowseFolderFragment.kt` (base pour 7 fragments de navigation)
- `RowsSupportFragment` : `HomeRowsFragment.kt`, `JellyseerrDiscoverRowsFragment.kt`
- `DetailsSupportFragment` : Non utilise directement (remplacement Compose v2)

### Presenters Leanback (package `ui/presentation/`)
| Classe | Role |
|--------|------|
| `CardPresenter` | Carte media standard (poster, titre, progression) |
| `ChannelCardPresenter` | Carte chaine TV |
| `GridButtonPresenter` | Bouton grille |
| `InfoCardPresenter` | Carte info |
| `TextItemPresenter` | Element texte |
| `CustomListRowPresenter` | Ligne de liste personnalisee |
| `CustomRowHeaderPresenter` | En-tete de ligne |
| `PositionableListRowPresenter` | Ligne avec position |
| `MyDetailsOverviewRowPresenter` | Details overview legacy |
| `MutableObjectAdapter` | Adapter mutable |

### Presenters Jellyseerr (package `ui/jellyseerr/`)
| Classe | Role |
|--------|------|
| `DetailsOverviewRowPresenter` | Details Jellyseerr |
| `GenreCardPresenter` | Carte genre |
| `NetworkStudioCardPresenter` | Carte reseau/studio |
| `MediaContentAdapter` | Adapter contenu media |
| `RequestsAdapter` | Adapter requetes |

### Actions playback (package `ui/playback/overlay/action/`)
- `PlayPauseAction`, `FastForwardAction`, `RewindAction`
- `SkipNextAction`, `SkipPreviousAction`
- `ClosedCaptionsAction`, `SubtitleDelayAction`
- `CustomAction` (base)

### Autres composants TV
- `DreamService` : Ecran de veille Android TV (`integration/dream/`)
- `TvProviderChannel` : Integration chaines recommandees (`integration/`)
- Navigation D-pad : Geree par Leanback et Compose `tv-material`

---

## 6. Textes hardcodes

### 6.1 Textes hardcodes dans le code Kotlin

#### `ui/itemdetail/v2/ItemDetailsFragment.kt` (fichier le plus problematique, 2000+ lignes)
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 770 | `"Next Up"` | Titre de section |
| 788 | `"Season $it Episodes"` / `"Episodes"` | Titre de section |
| 809 | `"Albums"` | Titre de section |
| 871 | `"More Like This"` | Titre de section |
| 1165 | `"Default"` | Option piste audio |
| 1170 | `"Audio Track"` | Titre dialogue selection |
| 1193 | `"None"` / `"Default"` | Options sous-titres |
| 1201 | `"Subtitle Track"` | Titre dialogue selection |
| 1232 | `"Select Version"` | Titre dialogue selection |
| 1258 | `"Genres"` | Label metadonnees |
| 1261 | `"Director"` | Label metadonnees |
| 1264 | `"Writers"` | Label metadonnees |
| 1268 | `"Studio"` | Label metadonnees |
| 1280 | `"Seasons"` | Titre de section |
| 1334 | `"Cast & Crew"` | Titre de section |
| 1806 | `"Movies (${personMovies.size})"` | Titre de section personne |
| 1817 | `"Series (${personSeries.size})"` | Titre de section personne |

#### `ui/itemdetail/v2/ItemDetailsViewModel.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 476 | `"Stereo"` | Badge audio |
| 487 | `"TrueHD"` | Nom codec |

#### `ui/jellyseerr/JellyseerrBrowseByFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 96-100 | `"Popularity"`, `"Rating"`, `"Release Date"`, `"Title"`, `"Revenue"` | Options de tri |
| 208-210 | `"Show All"`, `"Available Only"`, `"Requested Only"` | Options de filtre |
| 372 | `"Movie"` | Type media |
| 384 | `"Partially Available"` | Statut disponibilite |
| 418-420 | `"Network"`, `"Studio"`, `"Keyword"` | Types de filtre |

#### `ui/jellyseerr/MediaDetailsFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 569-618 | `"HD + 4K DECLINED"`, `"4K DECLINED"`, `"HD DECLINED"`, `"HD + 4K BLACKLISTED"`, `"4K BLACKLISTED"`, `"HD BLACKLISTED"`, `"HD + 4K AVAILABLE"`, `"4K AVAILABLE"`, `"HD AVAILABLE"`, `"HD + 4K PARTIAL"`, `"4K PARTIAL"`, `"HD PARTIAL"`, `"HD + 4K PROCESSING"`, `"4K PROCESSING"`, `"HD PROCESSING"`, `"HD + 4K PENDING"`, `"4K PENDING"`, `"HD PENDING"`, `"HD + 4K UNKNOWN"`, `"4K UNKNOWN"`, `"HD UNKNOWN"`, `"NOT REQUESTED"` | Statuts Jellyseerr |
| 723-726 | `"Request More"`, `"Request"` | Bouton action |
| 803 | `"Blacklisted"` | Statut |
| 996-1039 | `"Status"`, `"First Air Date"`, `"Last Air Date"`, `"Seasons"`, `"Release Date"`, `"Revenue"` | Labels facts |

#### `ui/jellyseerr/AdvancedRequestOptionsDialog.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 114 | `"Movie"` / `"TV Show"` | Type media |
| 305 | `"Quality Profile"` | Titre section |
| 310 | `"Server Default"` | Option par defaut |
| 338 | `"Root Folder"` | Titre section |
| 346 | `"Server Default"` | Option par defaut |

#### `ui/jellyseerr/SettingsFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 189 | `"Enter your Jellyfin password"` | Hint champ mot de passe |

#### `ui/jellyseerr/DiscoverFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 171-172 | `"Movie"`, `"TV Series"` | Types media |
| 194-198 | `"${hours}h ${minutes}m"`, `"${hours}h"` | Format duree |
| 216 | `"1 Season"` / `"$seasons Seasons"` | Label saisons |

#### `ui/jellyseerr/DetailsOverviewRowPresenter.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 36-37 | `"Movie"`, `"TV Series"` | Types media |
| 73 | `"  •  "` | Separateur |
| 182 | `"Partially Available"` | Statut |
| 184 | `"Blacklisted"` | Statut |

#### `ui/jellyseerr/RequestsAdapter.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 63 | `"Partially Available"` | Statut |

#### `ui/card/MediaInfoCardView.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 25-48 | `"Language"`, `"Codec"`, `"Profile"`, `"Level"`, `"Layout"`, `"Resolution"`, `"Anamorphic"`, `"Interlaced"`, `"Aspect"`, `"Framerate"`, `"Channels"`, `"Sample rate"`, `"Bit depth"`, `"kbps"`, `"Default"`, `"Forced"`, `"External"` | Labels info media (17 strings) |

#### `ui/itemdetail/FullDetailsFragment.java`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 725 | `"Media Details"` | Titre en-tete |

#### `ui/browsing/genre/GenreBrowseFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 358 | `" • "` | Separateur |
| 392-393 | `"Movie"`, `"Series"` | Types filtres |

#### `util/sdk/BaseItemExtensions.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 173-174 | `"Movie"`, `"TV Series"` | Noms types media |

#### `ui/startup/fragment/SelectServerFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 194 | `"Moonfin version ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}"` | Texte version |

#### `ui/startup/fragment/UserLoginFragment.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 78 | `"Jellyfin"` | Nom serveur par defaut |

#### `ui/browsing/v2/GenresGridViewModel.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 44 | `"Genres"` | Titre par defaut |
| 79 | `"Genres — $libraryName"` / `"Genres"` | Titre avec nom bibliotheque |

#### `ui/browsing/composable/inforow/rating.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 244-254 | `"Rotten Tomatoes"`, `"Metacritic"`, `"Metacritic User"`, `"Trakt"`, `"Letterboxd"`, `"Roger Ebert"`, `"MyAnimeList"`, `"AniList"` | Noms de sources de notation |

#### `ui/playback/overlay/action/SubtitleDelayAction.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 21 | `"No Delay"` | Option delai |
| 62 | `"Subtitle delay reset"` | Message toast |

#### `ui/playback/AudioDelayController.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 18 | `"No Delay"` | Label delai |

#### `ui/settings/screen/about/SettingsAboutScreen.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 37 | `"Moonfin app version"` | Label version |

#### `ui/settings/screen/screensaver/SettingsScreensaverScreen.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 73 | `"Off"` | Caption quand dimming = 0 |

#### `ui/settings/screen/screensaver/SettingsScreensaverDimmingScreen.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 18 | `"Off"` | Label pour valeur 0 |

#### `ui/home/mediabar/MediaBarSlideshowViewModel.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 372 | `"No items found"` | Message d'erreur |
| 378 | `"Server temporarily unavailable"` | Message d'erreur |
| 381 | `"Failed to load items: ..."` | Message d'erreur |

#### `ui/jellyseerr/JellyseerrViewModel.kt`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 139, 163 | `"Unknown"` | Titre par defaut |
| 211 | `"Jellyseerr initialized successfully"` | Message succes |
| 343 | `"Failed to load trending content"` | Message erreur |
| 371 | `"Failed to get current user"` | Message erreur |
| 482 | `"Failed to load requests"` | Message erreur |
| 488 | `"Unknown error"` | Message erreur generique |
| 689 | `"Request submitted successfully"` | Message succes |
| 693 | `"Failed to create request"` | Message erreur |

### 6.2 Textes hardcodes dans les layouts XML

#### `fragment_jellyseerr_requests.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 17 | `"My Requests"` | Titre |
| 37 | `"Jellyseerr Not Connected"` | Message d'etat |
| 45 | `"Go to Settings to configure your server"` | Instruction |
| 70 | `"Loading requests..."` | Message chargement |
| 95 | `"No Requests Yet"` | Message vide |
| 105 | `"Submit requests from the Discover tab"` | Instruction |
| 126 | `"Refresh"` | Bouton |

#### `view_jellyseerr_details_row.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 134 | `"Genres"` | Label |
| 161 | `"Director"` | Label |
| 188 | `"Studio"` | Label |
| 215 | `"Release Date"` | Label |
| 242 | `"Runtime"` | Label |
| 269 | `"Status"` | Label |
| 296 | `"Availability"` | Label |

#### `view_row_details.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 247 | `"Genres"` | Label |
| 275 | `"Director"` | Label |
| 303 | `"Writers"` | Label |
| 331 | `"Studios"` | Label |
| 359 | `"Runs"` | Label |
| 387 | `"Ends"` | Label |

#### `fragment_jellyseerr_settings.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 18 | `"Jellyseerr Settings"` | Titre |
| 27 | `"Configure your Jellyseerr server connection"` | Sous-titre |
| 44 | `"Enable Jellyseerr Integration"` | Label toggle |
| 67 | `"Server URL"` | Label champ |
| 77 | `"https://jellyseerr.example.com"` | Hint URL |
| 90 | `"Connect with Current Jellyfin Account"` | Label toggle |
| 110 | `"Connection status icon"` | Content description |
| 117 | `"Not tested"` | Statut |
| 135 | `"Test Connection"` | Bouton |
| 144 | `"Save Settings"` | Bouton |
| 197 | `"How to Connect:"` | Titre aide |
| 206 | Instructions multi-lignes | Texte d'aide |

#### `item_jellyseerr_content.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 35 | `"MOVIE"` | Badge type |

#### `fragment_server_add.xml`
| Ligne | Texte | Contexte |
|-------|-------|----------|
| 73 | `"192.168.1.100 or jellyfin.example.com"` | Hint adresse serveur |

#### Layouts avec textes placeholder (OK pour preview, mais a nettoyer)
- `overlay_tv_guide.xml:80` : `"Medium Text"`
- `channel_header.xml:11,22` : `"Small Text"` (x2)
- `friendly_date_button.xml:12,21` : `"Medium Text"`, `"Small Text"`

---

## 7. Incoherences visuelles

### 7.1 Double systeme UI : Leanback vs Compose

Le projet est en **migration active** de Leanback vers Compose. Cela cree deux mondes visuels distincts :

| Aspect | Ecrans Leanback (legacy) | Ecrans Compose (v2) |
|--------|------------------------|---------------------|
| **Style** | Sombre, look Android TV classique | Plus moderne, Material3 |
| **Navigation** | D-pad natif Leanback | `tv-material` + focus manual |
| **Cartes** | `CardPresenter` (XML) | `ItemCard` Composable |
| **Lignes** | `RowsSupportFragment` | `LazyRow` / `TvLazyRow` |
| **Details** | `view_row_details.xml` | `ItemDetailsFragment` (Compose) |
| **Ecrans concernes** | Home rows, Favorites, By Genre/Letter, Collections | Library v2, Music, LiveTV, Details v2, Settings, Genres v2 |

**Impact** : Un utilisateur naviguant de l'accueil (Leanback) vers une bibliotheque (Compose v2) ressent un changement de "look" abrupt.

### 7.2 Couleurs hardcodees massives dans Jellyseerr

Le module Jellyseerr ignore completement le design system (`design/token/ColorTokens.kt`) et utilise **~100+ couleurs hardcodees** via `Color.parseColor("#...")`. Palette utilisee de facto :

| Couleur | Hex | Usage Jellyseerr |
|---------|-----|-----------------|
| Gray-900 | `#111827` | Fond principal |
| Gray-800 | `#1F2937` | Fond cartes/dialogs |
| Gray-700 | `#374151` | Fond secondaire |
| Gray-600 | `#4B5563` | Fond survol |
| Gray-500 | `#6B7280` | Texte desactive |
| Gray-400 | `#9CA3AF` | Texte secondaire |
| Gray-300 | `#D1D5DB` | Texte principal |
| Indigo-500 | `#6366F1` | Boutons primaires |
| Indigo-400 | `#818CF8` | Boutons survol |
| Purple-600 | `#7C3AED` | Actions |
| Purple-400 | `#A78BFA` | Selections |
| Yellow-500 | `#EAB308` | Statut pending |
| Green-500 | `#22C55E` | Statut available |
| Red-500/600 | `#EF4444` / `#DC2626` | Statut declined/erreur |
| Blue-400 | `#60A5FA` / `#3B82F6` | Liens |

**Problemes** :
- Ces couleurs sont du **Tailwind CSS** porte tel quel, pas un design system TV
- Aucune correspondance avec les `ColorTokens` du module `design`
- Pas de support theme clair/sombre
- Fichiers les plus touches :
  - `MediaDetailsFragment.kt` : ~40 `Color.parseColor()`
  - `QualitySelectionDialog.kt` : ~12 `Color.parseColor()`
  - `SeasonSelectionDialog.kt` : ~10 `Color.parseColor()`
  - `AdvancedRequestOptionsDialog.kt` : ~15 `Color.parseColor()`
  - `PersonDetailsFragment.kt` : ~10 `Color.parseColor()`
  - `GenreCardPresenter.kt` : 1 `Color.parseColor()`
  - `ItemDetailsFragment.kt:256` : 1 `Color.parseColor("#0A0A0A")`

### 7.3 Design Tokens sous-utilises

Le module `design` fournit un systeme de tokens complet :
- **ColorTokens** : ~170 couleurs (blue, grey, green, red, cyan, lime, etc.)
- **TypographyTokens** : 9 tailles (10dp a 32dp), 9 poids (100-900)
- **SpaceTokens** : 8 valeurs (2dp a 48dp)
- **RadiusTokens** : 3 valeurs (0, 128dp par defaut, 15984dp cercle)

**Mais** : Le `radiusDefault = 128.dp` est anormalement grand (probablement un arrondi excessif), les tokens typographiques utilisent `dp` au lieu de `sp` (probleme d'accessibilite), et la plupart du code n'utilise pas ces tokens.

### 7.4 Couleurs hardcodees hors Jellyseerr

En plus du module Jellyseerr, d'autres fichiers contiennent des couleurs hardcodees :

#### `ui/shared/toolbar/MainToolbar.kt` (lignes 280-289) — 16+ couleurs overlay
```
Color(0xFF1A2332) "dark_blue", Color(0xFF4A148C) "purple", Color(0xFF00695C) "teal",
Color(0xFF0D1B2A) "navy", Color(0xFF36454F) "charcoal", Color(0xFF3E2723) "brown",
Color(0xFF8B0000) "dark_red", Color(0xFF0B4F0F) "dark_green", Color(0xFF475569) "slate",
Color(0xFF1E3A8A) "indigo"
```

#### `ui/preference/category/DonateDialog.kt`
- `Color(0xE6141414)` (fond), `Color(0xFF00A4DC)` (Jellyfin blue, multiple instances)

#### `ui/browsing/ExitConfirmationDialog.kt`
- `Color(0xE6141414)` (fond sombre)

#### `ui/base/colorScheme.kt` (lignes 11-22) — Duplication de colors.xml en Kotlin
- 12 couleurs boutons/inputs dupliquees : `Color(0xB3747474)`, `Color(0xFFDDDDDD)`, etc.

#### `ui/browsing/composable/inforow/InfoRowColors.kt`
- `Color(0xB3FFFFFF)`, `Color(0xB3089562)` (vert), `Color(0xB3F2364D)` (rouge)

#### Layouts XML avec couleurs hardcodees
- `jellyfin_genre_card.xml:23` : `android:background="#1a1a1a"`
- `jellyfin_genre_card.xml:47` : `android:textColor="#FFFFFF"`
- `jellyfin_genre_card.xml:62` : `android:textColor="#CCCCCC"`
- `jellyseerr_genre_card.xml:39` : `android:textColor="#FFFFFF"`
- `vlc_player_interface.xml:116` : `android:textColor="#B3FFFFFF"`

### 7.5 Themes incomplets

3 themes XML existent mais avec des niveaux de completude tres differents :
- `theme_jellyfin.xml` : **20 attributs tile color** definis (complet)
- `theme_emerald.xml` : **18 attributs** definis (quasi-complet)
- `theme_mutedpurple.xml` : **1 seul attribut** defini (`tile_port_person_bg`) — **severement incomplet**, fallback sur les couleurs par defaut pour 19 autres attributs

Le theme Muted Purple utilise aussi `popupMenuBackground = #384873` hardcode au lieu d'une reference `@color/`.

### 7.6 Unites CSS mal utilisees dans les layouts XML

Plusieurs layouts utilisent `sp` (scaled pixels, reserve au texte) pour des dimensions de layout :

#### `program_grid_cell.xml`
- Ligne 7 : `android:padding="2sp"` → devrait etre `2dp`
- Ligne 16 : `android:layout_marginHorizontal="5sp"` → devrait etre `5dp`
- Lignes 17-18 : margins en `sp` → `dp`

#### `channel_header.xml`
- Ligne 5 : `android:padding="2sp"` → `2dp`
- Lignes 9, 20 : `android:layout_width="100sp"` → `100dp`
- Lignes 32-33 : `android:layout_width="100sp"`, `android:layout_height="50sp"` → `dp`

### 7.7 Systeme typographique non respecte

Un systeme `Typography` est defini dans `ui/base/typography.kt` avec 5 styles (ListHeader 15sp, ListOverline 10sp, ListHeadline 14sp, ListCaption 11sp, Badge 11sp), mais **37+ fichiers Compose** hardcodent leurs propres `fontSize` au lieu de l'utiliser :
- `DonateDialog.kt` : 20.sp, 15.sp, 14.sp, 13.sp
- `PhotoPlayerHeader.kt` / `VideoPlayerHeader.kt` : 22.sp, 18.sp
- `LeftSidebarNavigation.kt` : 16.sp
- `Toolbar.kt` : 20.sp

### 7.8 Dimensions de cartes genre incoherentes

- `jellyseerr_genre_card.xml` : dimensions fixes `300dp x 150dp`
- `jellyfin_genre_card.xml` : `wrap_content` (pas de dimensions fixes)
→ Les cartes genre Jellyfin et Jellyseerr ont des tailles visuellement differentes.

### 7.9 Deux systemes de couleurs XML coexistent

- `colors.xml` definit des couleurs "Jellyfin" (`jellyfin_blue`, `midnight_blue`, etc.)
- Le theme est applique par code dans `JellyfinTheme.kt` (Compose) et `styles.xml` (Leanback)
- Les ecrans Compose Jellyseerr utilisent un troisieme jeu de couleurs (Tailwind)
- `colorScheme.kt` duplique certaines couleurs de `colors.xml` en Kotlin
→ **4 sources de couleurs** au total, sans lien entre elles.

### 7.10 Autres incoherences specifiques

| Probleme | Localisation | Detail |
|----------|-------------|--------|
| Mix `dp`/`sp` dans les tokens typographiques | `design/token/TypographyTokens.kt` | Toutes les tailles de police en `dp` au lieu de `sp` — ne respecte pas les preferences d'accessibilite |
| Fond `#0A0A0A` hardcode | `ItemDetailsFragment.kt:256` | Devrait utiliser un token ou attribut theme |
| Fond `#1a1a1a` hardcode | `GenreCardPresenter.kt:67` | Devrait utiliser un token |
| Styles de boutons differents | `styles.xml` (XML) vs `ui/base/button/` (Compose) | Deux systemes de boutons coexistent |
| Barre de progression | `styles.xml:12-22` | Deux styles identiques (`player_progress` et `overlay_progress`) avec les memes valeurs |
| Layouts Jellyseerr construits en code | `MediaDetailsFragment.kt`, `PersonDetailsFragment.kt`, etc. | UI construite entierement par `addView()` en code imperatif, pas en Compose ni XML |
| Leanback header style | `styles.xml:8` | Reference `?attr/headerTextColor` qui depend du theme Leanback |
| PopupMenu couleur fixe | `styles.xml:84,89` | `textColor="@color/indigo_dye"` ignore le theme actif |

---

## 8. Points d'attention

### Architecture
- **Migration Leanback -> Compose incomplete** : Le home screen, les favoris, les collections, et la navigation by genre/letter sont encore en Leanback. Les bibliotheques v2, les details v2, la musique, le live TV, et les settings sont en Compose.
- **Fragment-heavy** : 45+ fragments. Meme les ecrans Compose sont encapsules dans des `Fragment` conteneurs plutot que d'utiliser Compose Navigation directement.
- **`ItemDetailsFragment.kt` est un mega-fichier** : 2000+ lignes, melange UI Compose et logique metier, gere films/series/musique/personnes/playlists dans un seul fichier.
- **`JellyseerrViewModel.kt` est egalement tres gros** : 850+ lignes, gere toute la logique Jellyseerr.

### UI / UX
- **Jellyseerr entierement imperatif** : Les ecrans `MediaDetailsFragment`, `PersonDetailsFragment`, `QualitySelectionDialog`, `SeasonSelectionDialog`, `AdvancedRequestOptionsDialog` construisent l'UI via `addView()` — ni Compose, ni XML layouts. C'est du code Android pre-2015.
- **Pas de theme unifie** : 3 systemes de couleurs coexistent (design tokens, colors.xml, couleurs hardcodees Tailwind).
- **Pas de `themes.xml`** : Le theming est fait en code, ce qui rend difficile la coherence visuelle.
- **`minSdk = 23` vs hardware cible (Android 14)** : Le minSdk tres bas force l'utilisation de desugaring et empeche d'utiliser certaines APIs modernes.

### Localisation
- **~150+ textes hardcodes** repartis dans 20+ fichiers : Jellyseerr (~60 strings dans 8 fichiers), ItemDetailsFragment v2 (~17), MediaInfoCardView (~17), ItemDetailsViewModel, GenreBrowseFragment, ratings, delays, settings.
- **7 layouts XML** avec ~30 strings hardcodees (fragment_jellyseerr_settings, fragment_jellyseerr_requests, view_jellyseerr_details_row, view_row_details, etc.).
- **Fichiers `strings.xml`** existants avec traduction FR (`values-fr/strings.xml`), mais les nouveaux ecrans (Jellyseerr, Details v2, MediaInfoCard) n'utilisent pas les ressources string.

### Performance
- **NewPipe Extractor** inclus avec un JAR strip custom (suppression de `Utils.class` remplace par un shadow local) — solution fragile qui casse a chaque mise a jour de la lib.
- **ProGuard en debug** : `isMinifyEnabled = true` meme en debug, ce qui ralentit le build et rend le debugging plus difficile.

### Code Date
- Les presenters Leanback (`CardPresenter`, `MyDetailsOverviewRowPresenter`, etc.) datent de l'architecture Jellyfin originale.
- `ExternalPlayerActivity` gere la lecture externe de maniere legacy.
- `BrowseFolderFragment` et ses sous-classes utilisent le pattern deprecated de Leanback `BrowseSupportFragment`.

---

*Audit genere le 2026-03-07*
