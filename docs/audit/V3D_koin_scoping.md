# V3D — Audit et corrections du scoping Koin

> **Date** : 2026-03-08
> **Statut** : Terminé — BUILD SUCCESSFUL
> **Changements** : 1 singleton → viewModel, 4 singletons → factory

---

## 1. Résumé

Le projet comptait **60 singletons** répartis sur 6 modules Koin. Après analyse individuelle de chaque singleton (état mutable, utilisation multi-écrans, coût de création), **5 corrections** ont été appliquées :

| Transition | Nombre | Classes |
|------------|--------|---------|
| `single → viewModel` | 1 | MediaBarSlideshowViewModel |
| `single → factory` | 4 | ItemLauncher, KeyProcessor, ReportingHelper, ImageHelper |
| **Total corrigés** | **5** | |
| **Singletons restants** | **55** | Justifiés individuellement ci-dessous |

---

## 2. Tableau complet des 60 singletons — avant/après

### AppModule (39 singletons → 34 singletons + 1 viewModel + 3 factory)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 1 | DeviceInfo (defaultDeviceInfo) | single | **single** | Identité immuable du device |
| 2 | OkHttpFactory | single | **single** | Factory HTTP partagée, coûteuse à créer |
| 3 | HttpClientOptions | single | **single** | Config partagée immuable |
| 4 | JellyfinSdk | single | **single** | Instance SDK lourde, app-wide |
| 5 | ApiClient | single | **single** | Instance API session-bound, partagée partout |
| 6 | SocketHandler | single | **single** | Connexion WebSocket persistante |
| 7 | OkHttpNetworkFetcherFactory | single | **single** | Couche réseau Coil, coûteuse |
| 8 | ImageLoader (Coil) | single | **single** | Cache mémoire 25% + disque 250MB |
| 9 | DataRefreshService | single | **single** | État partagé (lastPlayback, lastDeletedItemId, etc.) |
| 10 | PlaybackControllerContainer | single | **single** | Référence partagée au contrôleur de lecture actif |
| 11 | InteractionTrackerViewModel | single | **single** | État global screensaver/interaction, commentaire explicite dans le code |
| 12 | UserRepository | single | **single** | Repository — cache + état utilisateur |
| 13 | UserViewsRepository | single | **single** | Repository — vues utilisateur cachées |
| 14 | NotificationsRepository | single | **single** | Repository — état notifications |
| 15 | ItemMutationRepository | single | **single** | Repository — mutations partagées |
| 16 | CustomMessageRepository | single | **single** | Event bus MutableStateFlow — tous les écrans observent |
| 17 | NavigationRepository | single | **single** | État navigation app-wide + backstack |
| 18 | ShuffleManager | single | **single** | MutableStateFlow isShuffling + mutex |
| 19 | SearchRepository | single | **single** | Repository — historique/cache recherche |
| 20 | MediaSegmentRepository | single | **single** | Repository — tracking segments |
| 21 | ExternalAppRepository | single | **single** | Repository — config apps externes |
| 22 | LocalWatchlistRepository | single | **single** | Repository — watchlist persistante |
| 23 | MultiServerRepository | single | **single** | Repository — état multi-serveur |
| 24 | ApiClientFactory | single | **single** | Factory d'API clients, maintient cache interne |
| 25 | ParentalControlsRepository | single | **single** | Repository — contrôle parental session |
| 26 | JellyseerrPreferences (global) | single | **single** | Préférences partagées serveur URL |
| 27 | JellyseerrRepository | single | **single** | Repository — état + cache Jellyseerr |
| 28 | MdbListRepository | single | **single** | Repository — cache service externe |
| 29 | TmdbRepository | single | **single** | Repository — cache TMDB |
| 30 | **MediaBarSlideshowViewModel** | single | **viewModel** | **ViewModel mal enregistré — doit utiliser le lifecycle ViewModel** |
| 31 | SyncPlayManager | single | **single** | État complexe (MutableStateFlow, timers, callbacks, sync group) |
| 32 | BackgroundService | single | **single** | État slideshow background + blur, partagé app-wide |
| 33 | UpdateCheckerService | single | **single** | Cache latestPluginUpdateInfo + OkHttpClient coûteux |
| 34 | MarkdownRenderer | single | **single** | Markwon.builder() modérément coûteux |
| 35 | **ItemLauncher** | single | **factory** | **Stateless — n'injecte que des dépendances Lazy, aucun état mutable** |
| 36 | **KeyProcessor** | single | **factory** | **Stateless — handler clavier sans état mutable** |
| 37 | **ReportingHelper** | single | **factory** | **Stateless — orchestrateur qui délègue à DataRefreshService** |
| 38 | PlaybackHelper (SdkPlaybackHelper) | single | **single** | Helper lecture avec dépendances lourdes |
| 39 | ThemeMusicPlayer | single | **single** | Gère MediaPlayer — doit être unique pour éviter audio simultané |

### AuthModule (6 singletons → 6 singletons, aucun changement)

| # | Classe | Scope | Raison |
|---|--------|-------|--------|
| 40 | AuthenticationStore | single | Couche persistance auth |
| 41 | AuthenticationPreferences | single | Préférences auth |
| 42 | AuthenticationRepository | single | Repository — état auth |
| 43 | ServerRepository | single | Repository — liste serveurs |
| 44 | ServerUserRepository | single | Repository — mappings user-server |
| 45 | SessionRepository | single | Repository — lifecycle session |

### PlaybackModule (7 singletons → 7 singletons, aucun changement)

| # | Classe | Scope | Raison |
|---|--------|-------|--------|
| 46 | LegacyPlaybackManager | single | État lecture partagé entre écrans |
| 47 | VideoQueueManager | single | File lecture (état partagé) |
| 48 | MediaManager (RewriteMediaManager) | single | Gestionnaire média global |
| 49 | PlaybackLauncher | single | Doit survivre aux changements d'écran |
| 50 | PrePlaybackTrackSelector | single | Sélection pistes via SharedPreferences |
| 51 | HttpDataSource.Factory | single | Client HTTP lourd pour streaming |
| 52 | PlaybackManager (rewrite) | single | État playback global |

### PreferenceModule (7 singletons → 7 singletons, aucun changement)

| # | Classe | Scope | Raison |
|---|--------|-------|--------|
| 53 | PluginSyncService | single | Service sync plugins multi-serveur |
| 54 | PreferencesRepository | single | Coordinateur préférences |
| 55 | LiveTvPreferences | single | Préférences Live TV |
| 56 | UserSettingPreferences | single | Préférences utilisateur |
| 57 | UserPreferences | single | Préférences utilisateur |
| 58 | SystemPreferences | single | Préférences système |
| 59 | TelemetryPreferences | single | Préférences télémétrie |

### UtilsModule (1 singleton → 0 singletons + 1 factory)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 60 | **ImageHelper** | single | **factory** | **Stateless — wrapper URLs images sans état** |

---

## 3. Détail des corrections

### 3.1 MediaBarSlideshowViewModel : `single` → `viewModel`

**Fichiers modifiés :**
- `di/AppModule.kt:216` — `single { ... }` → `viewModel { ... }`
- `ui/home/HomeFragment.kt:39` — `by inject<>()` → `by activityViewModel<>()`
- `ui/home/HomeRowsFragment.kt:91` — `by inject<>()` → `by activityViewModel<>()`

**Justification :** C'est un `ViewModel` Android qui doit bénéficier du lifecycle ViewModel (survie aux rotations, nettoyage à la destruction de l'Activity). Utilisation de `activityViewModel` car HomeFragment et HomeRowsFragment (fragment enfant) doivent partager la même instance via le ViewModelStore de l'Activity.

### 3.2 ItemLauncher : `single` → `factory`

**Fichier modifié :** `di/AppModule.kt:225`

**Justification :** Classe Java stateless qui utilise uniquement des dépendances `Lazy<T>` via KoinComponent. Aucune variable mutable, aucun cache. Utilisé dans 6+ fragments mais chaque instance est identique fonctionnellement.

### 3.3 KeyProcessor : `single` → `factory`

**Fichier modifié :** `di/AppModule.kt:226`

**Justification :** Handler d'événements clavier stateless. N'injecte que des dépendances Lazy, crée des menus à la demande sans conserver d'état.

### 3.4 ReportingHelper : `single` → `factory`

**Fichier modifié :** `di/AppModule.kt:227`

**Justification :** Orchestrateur de reporting stateless. Appelle l'API et met à jour `DataRefreshService` (qui lui reste singleton). Aucun état propre.

### 3.5 ImageHelper : `single` → `factory`

**Fichier modifié :** `di/UtilsModule.kt:7`

**Justification :** Wrapper léger autour d'`ApiClient` pour générer des URLs d'images. Fonctions pures, aucun état mutable.

---

## 4. Singletons conservés — justifications par catégorie

### SDK & Infrastructure réseau (8 singletons)
DeviceInfo, OkHttpFactory, HttpClientOptions, JellyfinSdk, ApiClient, SocketHandler, OkHttpNetworkFetcherFactory, ImageLoader — **Tous justifiés** : connexions persistantes, caches lourds, identité immuable.

### Repositories (16 singletons)
UserRepository, UserViewsRepository, NotificationsRepository, ItemMutationRepository, CustomMessageRepository, NavigationRepository, SearchRepository, MediaSegmentRepository, ExternalAppRepository, LocalWatchlistRepository, MultiServerRepository, ParentalControlsRepository, JellyseerrRepository, MdbListRepository, TmdbRepository, ApiClientFactory — **Tous justifiés** : cache + état partagé entre écrans.

### Auth (6 singletons)
AuthenticationStore, AuthenticationPreferences, AuthenticationRepository, ServerRepository, ServerUserRepository, SessionRepository — **Tous justifiés** : état d'authentification partagé app-wide.

### Playback (7 singletons)
LegacyPlaybackManager, VideoQueueManager, MediaManager, PlaybackLauncher, PrePlaybackTrackSelector, HttpDataSource.Factory, PlaybackManager — **Tous justifiés** : état lecture partagé, file audio, clients HTTP streaming.

### Préférences (7 singletons)
PluginSyncService, PreferencesRepository, LiveTvPreferences, UserSettingPreferences, UserPreferences, SystemPreferences, TelemetryPreferences — **Tous justifiés** : préférences partagées SharedPreferences.

### Services & Managers (7 singletons)
DataRefreshService, PlaybackControllerContainer, InteractionTrackerViewModel, ShuffleManager, SyncPlayManager, BackgroundService, UpdateCheckerService — **Tous justifiés** : état mutable partagé (MutableStateFlow, timers, mutex, caches).

### Utilitaires (2 singletons)
MarkdownRenderer, PlaybackHelper — **Justifiés** : initialisation Markwon coûteuse ; SdkPlaybackHelper avec dépendances lourdes.

### Conservé en singleton malgré le nom "ViewModel" (1)
InteractionTrackerViewModel — Commentaire explicite dans le code : *"Use single scope to ensure the same instance is used across all playback sessions"*. Gère le screensaver et le tracking d'interaction globalement.

### ThemeMusicPlayer conservé en singleton (1)
Gère un MediaPlayer unique — le passer en factory créerait des instances multiples avec risque d'audio simultané.

---

## 5. Résultat final

| Module | Singletons avant | Singletons après | viewModels ajoutés | Factories ajoutées |
|--------|-----------------|------------------|-------------------|-------------------|
| AppModule | 39 | 34 | +1 | +3 |
| AuthModule | 6 | 6 | 0 | 0 |
| PlaybackModule | 7 | 7 | 0 | 0 |
| PreferenceModule | 7 | 7 | 0 | 0 |
| UtilsModule | 1 | 0 | 0 | +1 |
| **Total** | **60** | **54** | **+1** | **+4** |

**Compilation : BUILD SUCCESSFUL** (0 erreur, warnings pré-existants uniquement)

---

## 6. Recommandations futures (hors scope)

| Priorité | Action | Effort |
|----------|--------|--------|
| Moyenne | Supprimer les 19 héritages `KoinComponent` (anti-pattern service locator) | Moyen |
| Basse | Explorer le scoping Koin par session (SyncPlayManager, BackgroundService) | Grand |
| Basse | Convertir `InteractionTrackerViewModel` en vrai `viewModel` scoped à Activity | Petit |
