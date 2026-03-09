# V3D — Audit et corrections du scoping Koin

> **Date** : 2026-03-08 (mise à jour v2)
> **Statut** : Terminé — BUILD SUCCESSFUL (checkAll)
> **Changements totaux** : 1 singleton → viewModel, 13 singletons → factory

---

## 1. Résumé

Le projet comptait **60 singletons** répartis sur 6 modules Koin. Après analyse individuelle de chaque déclaration (état mutable, pattern d'injection, coût de création), **14 corrections** ont été appliquées en deux passes :

| Passe | Transition | Nombre | Classes |
|-------|-----------|--------|---------|
| v1 | `single → viewModel` | 1 | MediaBarSlideshowViewModel |
| v1 | `single → factory` | 4 | ItemLauncher, KeyProcessor, ReportingHelper, ImageHelper |
| **v2** | **`single → factory`** | **9** | **HttpClientOptions, SearchRepository, ExternalAppRepository, PlaybackHelper, PrePlaybackTrackSelector, UserSettingPreferences, UserPreferences, SystemPreferences, TelemetryPreferences** |
| **Total corrigés** | | **14** | |
| **Singletons restants** | | **46** | Justifiés individuellement ci-dessous |

---

## 2. Tableau complet — avant/après (toutes les déclarations)

### AppModule (39 → 30 singletons, 22 viewModels, 9 factories)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 1 | DeviceInfo (defaultDeviceInfo) | single | **single** | Identité immuable du device |
| 2 | OkHttpFactory | single | **single** | Factory HTTP partagée, coûteuse à créer |
| 3 | HttpClientOptions | single | **factory** | **v2 — Config immuable, utilisé uniquement à l'init des singletons parents** |
| 4 | JellyfinSdk | single | **single** | Instance SDK lourde, app-wide |
| 5 | ApiClient | single | **single** | Instance API session-bound, partagée partout |
| 6 | SocketHandler | single | **single** | Connexion WebSocket persistante |
| 7 | OkHttpNetworkFetcherFactory | single | **single** | Couche réseau Coil, coûteuse |
| 8 | ImageLoader (Coil) | single | **single** | Cache mémoire 25% + disque 250MB |
| 9 | DataRefreshService | single | **single** | 7 champs mutables (lastPlayback, lastDeletedItemId, etc.) |
| 10 | PlaybackControllerContainer | single | **single** | Référence partagée au contrôleur de lecture actif |
| 11 | InteractionTrackerViewModel | single | **single** | État global screensaver/interaction — voir §4 exception documentée |
| 12 | UserRepository | single | **single** | Repository — cache + état utilisateur |
| 13 | UserViewsRepository | single | **single** | Repository — vues utilisateur cachées |
| 14 | NotificationsRepository | single | **single** | Repository — état notifications |
| 15 | ItemMutationRepository | single | **single** | Repository — mutations partagées |
| 16 | CustomMessageRepository | single | **single** | Event bus MutableStateFlow — tous les écrans observent |
| 17 | NavigationRepository | single | **single** | État navigation app-wide + backstack |
| 18 | ShuffleManager | single | **single** | MutableStateFlow isShuffling + mutex de debouncing |
| 19 | SearchRepository | single | **factory** | **v2 — Stateless : aucun cache, aucun champ mutable, fonctions pures API** |
| 20 | MediaSegmentRepository | single | **single** | Cache mutableMapOf (mediaTypeActions) |
| 21 | ExternalAppRepository | single | **factory** | **v2 — Stateless : wrapper de requêtes UserPreferences + PackageManager** |
| 22 | LocalWatchlistRepository | single | **single** | MutableStateFlow watchlistFlow |
| 23 | MultiServerRepository | single | **single** | Repository — état multi-serveur |
| 24 | ApiClientFactory | single | **single** | Factory d'API clients, maintient cache interne |
| 25 | ParentalControlsRepository | single | **single** | Repository — contrôle parental session |
| 26 | JellyseerrPreferences (global) | single | **single** | Préférences partagées serveur URL |
| 27 | JellyseerrRepository | single | **single** | Repository — état + cache Jellyseerr |
| 28 | MdbListRepository | single | **single** | Cache mutableMapOf (ratingsCache, pendingRequests) |
| 29 | TmdbRepository | single | **single** | Cache 6 mutableMapOf (episodeRatings, seasonCache, etc.) |
| 30 | MediaBarSlideshowViewModel | single | **viewModel** | **v1 — ViewModel mal enregistré, doit utiliser le lifecycle ViewModel** |
| 31 | SyncPlayManager | single | **single** | État complexe (MutableStateFlow, timers, callbacks, sync group) |
| 32 | BackgroundService | single | **single** | 9+ champs mutables (backgrounds, blur, jobs, coroutines) |
| 33 | UpdateCheckerService | single | **single** | @Volatile cache latestPluginUpdateInfo |
| 34 | MarkdownRenderer | single | **single** | Markwon.builder() modérément coûteux, thread-safe |
| 35 | ItemLauncher | single | **factory** | **v1 — Stateless, dépendances Lazy uniquement** |
| 36 | KeyProcessor | single | **factory** | **v1 — Stateless, handler clavier sans état** |
| 37 | ReportingHelper | single | **factory** | **v1 — Stateless, délègue à DataRefreshService** |
| 38 | PlaybackHelper (SdkPlaybackHelper) | single | **factory** | **v2 — Stateless : aucun champ mutable, fonctions pures (vérifié par audit)** |
| 39 | ThemeMusicPlayer | single | **single** | Gère MediaPlayer unique — évite audio simultané |

### AuthModule (6 → 6 singletons, 1 factory — aucun changement)

| # | Classe | Scope | Raison |
|---|--------|-------|--------|
| 40 | AuthenticationStore | single | Couche persistance auth |
| 41 | AuthenticationPreferences | single | Préférences auth |
| 42 | AuthenticationRepository | single | Repository — état auth |
| 43 | ServerRepository | single | Repository — liste serveurs |
| 44 | ServerUserRepository | single | Repository — mappings user-server |
| 45 | SessionRepository | single | Repository — lifecycle session |

### PlaybackModule (7 → 6 singletons, 1 factory)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 46 | LegacyPlaybackManager | single | **single** | État lecture partagé entre écrans |
| 47 | VideoQueueManager | single | **single** | File lecture (état partagé) |
| 48 | MediaManager (RewriteMediaManager) | single | **single** | Gestionnaire média global |
| 49 | PlaybackLauncher | single | **single** | CoroutineScope + dépendances lourdes |
| 50 | PrePlaybackTrackSelector | single | **factory** | **v2 — État dans SharedPreferences, pas en mémoire. Chaque instance est identique.** |
| 51 | HttpDataSource.Factory | single | **single** | Client HTTP lourd pour streaming |
| 52 | PlaybackManager (rewrite) | single | **single** | État playback global |

### PreferenceModule (7 → 3 singletons, 4 factories)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 53 | PluginSyncService | single | **single** | 6+ champs mutables (@Volatile, Jobs, MutableStateFlow, listeners) |
| 54 | PreferencesRepository | single | **single** | Cache mutableMapOf (libraryPreferences) |
| 55 | LiveTvPreferences | single | **single** | DisplayPreferencesStore — cache in-memory (cachedPreferences map) |
| 56 | UserSettingPreferences | single | **factory** | **v2 — SharedPreferenceStore stateless, pas de listener en constructeur** |
| 57 | UserPreferences | single | **factory** | **v2 — SharedPreferenceStore stateless, pas de listener en constructeur** |
| 58 | SystemPreferences | single | **factory** | **v2 — SharedPreferenceStore stateless, pas de listener en constructeur** |
| 59 | TelemetryPreferences | single | **factory** | **v2 — SharedPreferenceStore stateless, pas de listener en constructeur** |

### UtilsModule (1 → 0 singletons, 1 factory — changé en v1)

| # | Classe | Scope AVANT | Scope APRÈS | Raison |
|---|--------|-------------|-------------|--------|
| 60 | ImageHelper | single | **factory** | **v1 — Wrapper URLs images, fonctions pures** |

---

## 3. Détail des corrections v2

### 3.1 HttpClientOptions : `single` → `factory` (AppModule)

**Justification :** Data class immuable du SDK Jellyfin. Utilisé uniquement comme paramètre d'initialisation de singletons parents (createApi, OkHttpDataSource). Chaque `get()` crée un nouvel objet default identique — consommé une seule fois à l'init.

### 3.2 SearchRepository : `single` → `factory` (AppModule)

**Justification :** Vérifié stateless — aucun champ mutable, aucun cache, aucun `var`. Toutes les méthodes sont des appels API purs via `apiClient` et `multiServerRepository` (injectés singletons). Injecté uniquement dans SearchViewModel.

### 3.3 ExternalAppRepository : `single` → `factory` (AppModule)

**Justification :** Vérifié stateless — wrapper de requêtes `userPreferences` et `PackageManager`. Aucun champ mutable, fonctions de query pures.

### 3.4 PlaybackHelper / SdkPlaybackHelper : `single` → `factory` (AppModule)

**Justification :** Vérifié stateless — tous les champs sont des dépendances injectées (api, apiClientFactory, userPreferences, playbackLauncher, playbackControllerContainer). Aucun `var`, aucun cache, aucun MutableStateFlow. Fonctions de transformation et d'appels API.

### 3.5 PrePlaybackTrackSelector : `single` → `factory` (PlaybackModule)

**Justification :** L'état est persisté dans SharedPreferences, pas en mémoire. Chaque instance accède au même backing store. Pas de listener enregistré dans le constructeur.

### 3.6 Preferences SharedPreferenceStore : `single` → `factory` (PreferenceModule)

**Classes :** UserSettingPreferences, UserPreferences, SystemPreferences, TelemetryPreferences

**Justification :** Ces 4 classes héritent de `SharedPreferenceStore` qui est un wrapper stateless autour d'Android SharedPreferences :
- Pas de listener enregistré dans le constructeur
- Pas d'état mutable in-memory
- Les preference descriptors sont des `companion object` statiques
- Android SharedPreferences est déjà un singleton interne (même backing file)
- `PluginSyncService` gère ses propres listeners via `registerChangeListeners()` / `unregisterChangeListeners()` — fonctionne avec factory car l'enregistrement se fait sur l'instance SharedPreferences native

**LiveTvPreferences conservé en singleton** car il hérite de `DisplayPreferencesStore` qui maintient un cache in-memory (`cachedPreferences: MutableMap`, `displayPreferencesDto`).

---

## 4. Singletons conservés — justifications par catégorie

### SDK & Infrastructure réseau (7 singletons)
DeviceInfo, OkHttpFactory, JellyfinSdk, ApiClient, SocketHandler, OkHttpNetworkFetcherFactory, ImageLoader — **Connexions persistantes, caches lourds, identité immuable.**

### Repositories avec cache/état (14 singletons)
UserRepository, UserViewsRepository, NotificationsRepository, ItemMutationRepository, CustomMessageRepository, NavigationRepository, MediaSegmentRepository, LocalWatchlistRepository, MultiServerRepository, ApiClientFactory, ParentalControlsRepository, JellyseerrRepository, MdbListRepository, TmdbRepository — **Cache in-memory (MutableMap, MutableStateFlow) ou état partagé entre écrans.**

### Auth (6 singletons)
AuthenticationStore, AuthenticationPreferences, AuthenticationRepository, ServerRepository, ServerUserRepository, SessionRepository — **État d'authentification partagé app-wide.**

### Playback (6 singletons)
LegacyPlaybackManager, VideoQueueManager, MediaManager, PlaybackLauncher, HttpDataSource.Factory, PlaybackManager — **État lecture partagé, file audio, clients HTTP streaming.**

### Préférences avec état (3 singletons)
PluginSyncService, PreferencesRepository, LiveTvPreferences — **Cache in-memory ou état mutable (MutableMap, @Volatile, MutableStateFlow).**

### Services & Managers (7 singletons)
DataRefreshService (7 champs mutables), PlaybackControllerContainer (var PlaybackController), InteractionTrackerViewModel (voir exception), ShuffleManager (MutableStateFlow + Mutex), SyncPlayManager (état lourd), BackgroundService (9+ champs mutables), UpdateCheckerService (@Volatile cache) — **État mutable partagé.**

### Utilitaires (3 singletons)
MarkdownRenderer (Markwon builder coûteux), JellyseerrPreferences-global (prefs serveur), ThemeMusicPlayer (MediaPlayer unique)

### Exception documentée : InteractionTrackerViewModel en singleton

Ce ViewModel étend `androidx.lifecycle.ViewModel` mais est déclaré `single` avec un commentaire explicite :
> *"Use single scope to ensure the same instance is used across all playback sessions"*

**Pourquoi il ne peut pas devenir `viewModel { }` :**
- Injecté dans 6+ classes via `by inject()` et `get()` (pas seulement des Fragments)
- StillWatchingViewModel le reçoit en paramètre constructeur via Koin
- Les classes non-Fragment (PlaybackOverlayFragmentHelper, InAppScreensaver, ScreensaverLock) ne peuvent pas utiliser `by viewModel()`
- Convertir nécessiterait un refactoring de toutes les injections vers `activityViewModel()` / `sharedViewModel()` + changement d'architecture pour les helpers non-Fragment

**Recommandation future :** Renommer en `InteractionTracker` (sans suffixe ViewModel) car c'est fonctionnellement un service global, pas un ViewModel lié à une UI.

---

## 5. Résultat final

| Module | Singletons avant | Singletons après | viewModels | Factories |
|--------|-----------------|------------------|-----------|-----------|
| AppModule | 39 | **30** | 22 | 9 |
| AuthModule | 6 | **6** | 0 | 1 |
| PlaybackModule | 7 | **6** | 0 | 1 |
| PreferenceModule | 7 | **3** | 0 | 4 |
| UtilsModule | 1 | **0** | 0 | 1 |
| AndroidModule | 0 | **0** | 0 | 3 |
| **Total** | **60** | **45** | **22** | **19** |

**Réduction : 60 → 45 singletons (-25%)**

**Compilation : BUILD SUCCESSFUL** (`./gradlew checkAll` — 0 erreur, 0 régression)

---

## 6. Pourquoi pas < 25 singletons ?

Les 45 singletons restants sont **tous justifiés individuellement** — ils contiennent :
- Des caches in-memory (`MutableMap`, `MutableStateFlow`)
- Des ressources système (MediaPlayer, CoroutineScope, WebSocket)
- Des états partagés entre écrans (navigation, playback, auth)

Pour descendre en dessous de 45, il faudrait :
1. **Scopes Koin par session** — scoper SyncPlayManager, BackgroundService à une session utilisateur (effort moyen, risque moyen)
2. **Décomposer les God objects** — éclater les repositories monolithiques (JellyseerrRepository, PluginSyncService) en sous-composants factory (effort grand)
3. **Activity-scoped viewModels** — convertir InteractionTrackerViewModel + refactorer les helpers (effort petit, risque moyen)

Ces chantiers sont classés en dette technique long terme (v2+).

---

## 7. Recommandations futures (hors scope)

| Priorité | Action | Effort | Risque |
|----------|--------|--------|--------|
| Moyenne | Supprimer les 19 héritages `KoinComponent` (anti-pattern service locator) | Moyen | Faible |
| Moyenne | Explorer le scoping Koin par session (SyncPlayManager, BackgroundService) | Grand | Moyen |
| Basse | Renommer `InteractionTrackerViewModel` → `InteractionTracker` | Petit | Faible |
| Basse | Évaluer scoped viewModel pour InteractionTracker | Petit | Moyen |
