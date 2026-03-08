# Audit d'Architecture - Moonfin Android TV

> **Mis à jour le 2026-03-08 — état post-travaux**
> Audit diagnostique — aucune correction architecturale appliquée (toutes classées P3 long terme).
> Toutes les violations de séparation des responsabilités, God objects, et problèmes DI
> restent en l'état. Ce sont des chantiers de refactoring structurel à planifier.
> - ❌ En attente : V01-V16 (God objects, fragments imperatifs)
> - ❌ En attente : DI01-DI06 (sur-singletonisation, KoinComponent)
> - ❌ En attente : N01-N06 (navigation custom, JSON serialisation)
> - ❌ En attente : Gestion d'erreur unifiée dans UiState
> - Nouveaux fichiers d'infrastructure créés : AnimationDefaults.kt, OverlayColors.kt,
>   SkeletonPresets.kt, StateContainer.kt, EmptyState.kt, ErrorState.kt, ShakeModifier.kt,
>   StaggerModifier.kt, ImagePlaceholder.kt — qualité des composants de base améliorée.

## Resume executif

Le projet utilise un pattern **MVVM** (Model-View-ViewModel) globalement coherent, avec Koin pour l'injection de dependances et StateFlow pour la gestion reactive d'etat. La separation ViewModel/Fragment est bien respectee dans les ecrans v2 (Compose), mais le projet souffre de **16 God objects**, d'un systeme de **DI sur-singletonise** (43/64 declarations sont des `single`), d'une **navigation duale** (custom Fragment stack + Compose Router), et d'une **gestion des erreurs non unifiee**. Le module Jellyseerr represente la plus grosse dette technique architecturale avec ses fragments imperatifs (`addView()`).

---

## 1. Schema de l'architecture actuelle

```
┌──────────────────────────────────────────────────────────────────┐
│                        APPLICATION                                │
│                                                                   │
│  ┌─────────────┐     ┌──────────────────┐     ┌──────────────┐   │
│  │  StartupAct. │────▶│   MainActivity   │     │ ExternalPlay │   │
│  │  (auth flow) │     │ (post-login nav) │     │   Activity   │   │
│  └──────┬──────┘     └────────┬─────────┘     └──────────────┘   │
│         │                     │                                    │
│  ┌──────▼──────────────────────▼─────────────────────────┐        │
│  │              NavigationRepository (custom)             │        │
│  │  SharedFlow<NavigationAction> + Stack<Destination>     │        │
│  └──────────────────────┬────────────────────────────────┘        │
│                         │                                         │
│  ┌──────────────────────▼────────────────────────────────┐        │
│  │                    FRAGMENTS (~45)                      │        │
│  │                                                        │        │
│  │  ┌─ Leanback Legacy ──────┐  ┌─ Compose v2 ─────────┐ │        │
│  │  │ HomeRowsFragment       │  │ LibraryBrowseFragment │ │        │
│  │  │ AllFavoritesFragment   │  │ MusicBrowseFragment   │ │        │
│  │  │ BrowseFolderFragment*7 │  │ LiveTvBrowseFragment  │ │        │
│  │  │ CollectionFragment     │  │ GenresGridV2Fragment  │ │        │
│  │  │ SearchFragment         │  │ ItemDetailsFragment   │ │        │
│  │  └────────────────────────┘  │ Settings screens      │ │        │
│  │                              └───────────────────────┘ │        │
│  │  ┌─ Jellyseerr (imperatif) ───────────────────────────┐│        │
│  │  │ MediaDetailsFragment (addView), PersonDetails,     ││        │
│  │  │ DiscoverFragment, RequestsFragment, SettingsFragm. ││        │
│  │  └────────────────────────────────────────────────────┘│        │
│  └────────────────────────────────────────────────────────┘        │
│                         │                                         │
│  ┌──────────────────────▼────────────────────────────────┐        │
│  │                  VIEWMODELS (22)                        │        │
│  │  Pattern : MutableStateFlow → StateFlow (asStateFlow)  │        │
│  │  Updates : _uiState.update { it.copy(...) }            │        │
│  │                                                        │        │
│  │  Unified UiState :  LibraryBrowse, MusicBrowse,        │        │
│  │    GenresGrid, LiveTvBrowse, ItemDetails, Recordings,  │        │
│  │    Schedule, SeriesRecordings                          │        │
│  │                                                        │        │
│  │  Multiple StateFlows : Jellyseerr (10+ flows),         │        │
│  │    MediaBarSlideshow (4 flows), Startup, UserLogin     │        │
│  └────────────────────────────────────────────────────────┘        │
│                         │                                         │
│  ┌──────────────────────▼────────────────────────────────┐        │
│  │                  REPOSITORIES                          │        │
│  │  ServerRepository, SessionRepository, UserRepository,  │        │
│  │  UserViewsRepository, JellyseerrRepository,            │        │
│  │  NotificationsRepository, ItemMutationRepository,      │        │
│  │  SearchRepository, MediaSegmentRepository              │        │
│  └────────────────────────────────────────────────────────┘        │
│                         │                                         │
│  ┌──────────────────────▼────────────────────────────────┐        │
│  │                  SERVICES & DATA                       │        │
│  │  BackgroundService, SocketHandler, SyncPlayManager,    │        │
│  │  PluginSyncService, JellyseerrHttpClient,              │        │
│  │  DataRefreshService, UpdateCheckerService              │        │
│  └────────────────────────────────────────────────────────┘        │
│                         │                                         │
│  ┌──────────────────────▼────────────────────────────────┐        │
│  │             JELLYFIN SDK (ApiClient, REST)             │        │
│  │             KTOR (Jellyseerr HTTP)                     │        │
│  └────────────────────────────────────────────────────────┘        │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐        │
│  │                    KOIN DI                             │        │
│  │  6 modules : App(64), Auth(6), Playback(12),           │        │
│  │    Preference(4), Android(3), Utils(2)                 │        │
│  │  43 singletons, 8 factories, 13 viewModels             │        │
│  └────────────────────────────────────────────────────────┘        │
│                                                                   │
├──────────────────────────────────────────────────────────────────┤
│  MODULES GRADLE                                                   │
│  :app │ :design │ :playback:core │ :playback:jellyfin            │
│       │         │ :playback:media3:exoplayer │ :playback:media3:session │
│       │         │ :preference                                     │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Pattern architectural : MVVM

### 2.1 Coherence du pattern

Le projet utilise **MVVM avec StateFlow** de maniere globalement coherente :

| Composant | Pattern | Coherence |
|-----------|---------|-----------|
| ViewModels v2 (8/22) | Unified UiState data class + `_uiState.update { copy() }` | Excellent |
| ViewModels Jellyseerr (1/22) | 10+ StateFlows individuels + sealed class | Acceptable mais fragmente |
| ViewModels startup (3/22) | Multiple flows + sealed classes | Acceptable |
| ViewModels speciaux (10/22) | Patterns varies (minimal, Flow combinators, etc.) | Correct |
| Fragments v2 | `collectAsState()` pur, aucune logique metier | Excellent |
| Fragments Leanback | Adapters + callbacks, quelques acces directs aux repos | Acceptable |
| Fragments Jellyseerr | UI imperative (`addView()`), logique dans Fragment | Mauvais |

### 2.2 Aucun MVI detecte

Il n'y a **pas de pattern MVI** (Intent/Action → Reducer → State) dans le code. Les sealed classes presentes (`JellyseerrLoadingState`, `LoginState`, `QuickConnectState`, `MediaBarState`, `DreamContent`) sont des **etats** et non des intents/actions.

---

## 3. Violations de separation des responsabilites

### Priorite CRITIQUE

| # | Violation | Fichier | Lignes | Detail |
|---|-----------|---------|--------|--------|
| V01 | UI imperative dans Fragment | `ui/jellyseerr/MediaDetailsFragment.kt` | 2033 lignes | Construit toute l'UI via `addView()`, 31 fonctions privees, 14+ variables d'etat, melange chargement de donnees et construction d'interface |
| V02 | Fragment God-object | `ui/itemdetail/v2/ItemDetailsFragment.kt` | 2047 lignes | 26 fonctions privees, gere 6 types d'items (film, serie, musique, personne, playlist, collection) dans un seul fichier |
| V03 | ViewModel surcharge | `ui/jellyseerr/JellyseerrViewModel.kt` | 919 lignes | 29 StateFlows, 12 fonctions publiques, gere 8 domaines (trending, genres, requetes, recherche, auth, details, pagination, settings) |
| V04 | Client HTTP monolithique | `data/service/jellyseerr/JellyseerrHttpClient.kt` | 1253 lignes | ~30 methodes d'endpoints HTTP dans une seule classe |
| V05 | Repository monolithique | `data/repository/JellyseerrRepository.kt` | 766 lignes | 24+ fonctions suspend couvrant 6 domaines (auth, discovery, requetes, media, services, utilisateurs) |

### Priorite HAUTE

| # | Violation | Fichier | Lignes | Detail |
|---|-----------|---------|--------|--------|
| V06 | UI imperative dans Fragment | `ui/jellyseerr/PersonDetailsFragment.kt` | ~500 lignes | Meme pattern `addView()` que MediaDetailsFragment |
| V07 | Service God-object | `data/service/pluginsync/PluginSyncService.kt` | 857 lignes | 7 dependances injectees, 5+ responsabilites |
| V08 | Helper surcharge | `ui/itemhandling/ItemRowAdapterHelper.kt` | 915 lignes | 20+ fonctions d'extension, 6 responsabilites, utilise le pattern service locator |
| V09 | Manager surcharge | `data/syncplay/SyncPlayManager.kt` | 790 lignes | 8 jobs, 10+ variables d'etat, gestion groupe + lecture + etat + time sync |
| V10 | Manager surcharge | `ui/playback/rewrite/RewriteMediaManager.kt` | 857 lignes | `@Suppress("TooManyFunctions")` — signal d'alerte explicite |
| V11 | Fragment avec 38+ deps | `ui/home/HomeRowsFragment.kt` | 563 lignes | 38+ dependances injectees via Koin `by inject()` |

### Priorite MOYENNE

| # | Violation | Fichier | Lignes | Detail |
|---|-----------|---------|--------|--------|
| V12 | Composable monolithique | `ui/shared/toolbar/LeftSidebarNavigation.kt` | 868 lignes | UI + logique de navigation melees |
| V13 | Composable surcharge | `ui/home/mediabar/MediaBarSlideshowView.kt` | 562 lignes | Gere slideshow, trailers, animations dans un seul composable |
| V14 | Presenter surcharge | `ui/presentation/CardPresenter.kt` | 546 lignes | Gere tous types de cartes (media, personne, live TV, channel, grid button) |
| V15 | Toolbar monolithique | `ui/shared/toolbar/MainToolbar.kt` | 548 lignes | 16+ couleurs hardcodees, UI + logique de theme |
| V16 | Fragment Compose allonge | `ui/browsing/v2/LiveTvBrowseFragment.kt` | 547 lignes | Beaucoup d'UI inline au lieu de composables extraits |

---

## 4. Injection de dependances (Koin)

### 4.1 Structure des modules

| Module | Fichier | Declarations | Type dominant |
|--------|---------|-------------|---------------|
| AppModule | `di/AppModule.kt` | 64 | 43 `single`, 8 `factory`, 13 `viewModel` |
| AuthModule | `di/AuthModule.kt` | 6 | `single` |
| PlaybackModule | `di/PlaybackModule.kt` | 12 | `single` + `viewModel` |
| PreferenceModule | `di/PreferenceModule.kt` | 4 | `single` |
| AndroidModule | `di/AndroidModule.kt` | 3 | `factory` |
| UtilsModule | `di/UtilsModule.kt` | 2 | `single` |

### 4.2 Problemes identifies

| # | Probleme | Severite | Detail |
|---|----------|----------|--------|
| DI01 | Sur-singletonisation | HAUTE | 43/64 declarations sont `single` (application-wide). Des composants stateful comme `SyncPlayManager` restent en memoire toute la vie de l'app meme quand SyncPlay n'est pas utilise |
| DI02 | Pas de scoping | HAUTE | Aucun `scope` Koin utilise (pas de scope Activity, Fragment ou Session). Tous les objets vivent au niveau Application |
| DI03 | Anti-pattern KoinComponent | MOYENNE | 19 fichiers heritent de `KoinComponent` pour acceder a `get()`/`inject()` en dehors du graphe DI — pattern service locator |
| DI04 | Service locator dans helpers | MOYENNE | `ItemRowAdapterHelper.kt:49-53` utilise `get()` directement dans une fonction helper |
| DI05 | ViewModel force en singleton | BASSE | `MediaBarSlideshowViewModel` declare comme singleton au lieu de `viewModel` — ne beneficie pas du lifecycle ViewModel |
| DI06 | 597 occurrences service locator | INFO | `by inject()` / `get()` utilises massivement dans les Fragments (normal pour Koin mais rend le testing plus difficile) |

---

## 5. God objects (classes surchargees)

### Tier critique (>1500 lignes)

| Fichier | Lignes | Responsabilites | Decomposition proposee |
|---------|--------|-----------------|----------------------|
| `ItemDetailsFragment.kt` | 2047 | UI de 6 types d'items, dialogs selection piste/version, navigation, actions favori/vu, metadata | `MovieDetailsScreen`, `SeriesDetailsScreen`, `MusicDetailsScreen`, `PersonDetailsScreen`, `TrackSelectionDialog`, `VersionSelectionDialog` |
| `MediaDetailsFragment.kt` (Jellyseerr) | 2033 | UI imperative, chargement images, construction layout, navigation, gestion statut requete | Migration Compose : `JellyseerrMediaDetailsScreen`, `JellyseerrStatusBadge`, `JellyseerrFactsSection` |
| `JellyseerrHttpClient.kt` | 1253 | 30 endpoints HTTP dans une classe | `JellyseerrDiscoveryApi`, `JellyseerrRequestApi`, `JellyseerrMediaApi`, `JellyseerrUserApi` |

### Tier haut (800-1500 lignes)

| Fichier | Lignes | Responsabilites |
|---------|--------|-----------------|
| `JellyseerrViewModel.kt` | 919 | trending, genres, requetes, recherche, auth, details, pagination, settings |
| `ItemRowAdapterHelper.kt` | 915 | 20+ fonctions d'extension gerant 6 types de rows |
| `LeftSidebarNavigation.kt` | 868 | Navigation sidebar complete + animations |
| `PluginSyncService.kt` | 857 | Synchronisation de plugins multi-serveur |
| `RewriteMediaManager.kt` | 857 | Gestion file audio + controles + position + queue |
| `SyncPlayManager.kt` | 790 | Gestion groupe + lecture synchronisee + time sync |
| `JellyseerrRepository.kt` | 766 | 24+ fonctions suspend multi-domaine |

---

## 6. Gestion des etats (loading / error / success)

### 6.1 Etat actuel — 3 patterns coexistent

**Pattern A — Unified UiState (meilleur)** : 8 ViewModels
```kotlin
data class LibraryBrowseUiState(
    val isLoading: Boolean = true,
    val items: List<BaseItemDto> = emptyList(),
    // ... pas de champ error
)
```
- Utilise par : LibraryBrowse, MusicBrowse, GenresGrid, LiveTvBrowse, ItemDetails, Recordings, Schedule, SeriesRecordings
- **Probleme** : Aucun de ces UiStates n'a de champ `error` — les erreurs sont silencieusement ignorees (`isLoading = false` sans donnees)

**Pattern B — Sealed class** : JellyseerrViewModel, MediaBarSlideshow
```kotlin
sealed class JellyseerrLoadingState {
    data object Idle : JellyseerrLoadingState()
    data object Loading : JellyseerrLoadingState()
    data class Success(val message: String = "") : JellyseerrLoadingState()
    data class Error(val message: String) : JellyseerrLoadingState()
}
```
- Meilleur pour les erreurs mais pas utilise dans les autres ViewModels

**Pattern C — Pas de gestion d'etat** : StartupViewModel, PhotoPlayerViewModel
- Flows exposes directement depuis les repositories
- Pas de loading/error au niveau ViewModel

### 6.2 Inconsistances de gestion d'erreur

| ViewModel | Loading | Error affiché | Error silencieux |
|-----------|---------|---------------|------------------|
| ItemDetailsVM | `isLoading` dans UiState | Non | Oui — catch ApiClientException, juste `isLoading = false` |
| LibraryBrowseVM | `isLoading` dans UiState | Non | Oui — catch ApiClientException, juste `isLoading = false` |
| JellyseerrVM | Sealed class `Loading` | Oui — `Error(message)` | Non |
| MediaBarSlideshowVM | Sealed class `Loading` | Oui — `Error(message)` | Non |
| MusicBrowseVM | `isLoading` dans UiState | Non | Probablement oui |
| StartupVM | Aucun | Non | Erreurs dans repository |

### 6.3 Modele unifie propose

```kotlin
// Base pour tous les UiState
data class UiError(
    val message: String,
    val throwable: Throwable? = null,
    val isRecoverable: Boolean = true,
)

// Exemple d'application
data class LibraryBrowseUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,         // AJOUTER
    val items: List<BaseItemDto> = emptyList(),
    // ...
)

// Composable generique pour afficher les erreurs
@Composable
fun ErrorBanner(
    error: UiError?,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
)
```

---

## 7. Navigation

### 7.1 Systeme actuel

Le projet utilise une **navigation custom a base de Fragments** :

```
NavigationRepository (interface)
  └── NavigationRepositoryImpl
        ├── Stack<Destination.Fragment>    ← backstack manuel
        ├── SharedFlow<NavigationAction>   ← events de navigation
        └── Destinations (object)          ← ~35 destinations typees
```

**Points positifs** :
- Destinations fortement typees avec parametres
- Backstack gere explicitement
- `SharedFlow` avec replay pour les observateurs

**Points negatifs** :

| # | Probleme | Detail |
|---|----------|--------|
| N01 | Pas d'Android Navigation Component | La navigation est entierement custom, perdant les benefices de SafeArgs, deep links, et le backstack automatique |
| N02 | Serialisation JSON des items | Les `BaseItemDto` sont serialises en JSON complet dans les arguments (`Json.Default.encodeToString(item)`) au lieu de passer un ID — fragile et lourd |
| N03 | Backstack desynchronisable | Le `Stack<Destination>` manuel peut diverger de l'etat reel des Fragments si des operations systeme interviennent |
| N04 | Pas de deep linking | Aucun support de liens profonds (pas de `<nav-graph>`, pas d'intent filters de navigation) |
| N05 | Transitions non uniformes | Certains fragments utilisent `TRANSIT_FRAGMENT_FADE`, d'autres n'ont pas de transition |
| N06 | Compose Router coexistant | `ui/navigation/router.kt` contient un second systeme de navigation Compose avec son propre backstack `SnapshotStateList<RouteContext>` |

### 7.2 Gestion du bouton Back (D-pad)

- `OnBackPressedCallback` utilise dans `MainActivity` pour deleguer au `NavigationRepository.goBack()`
- `enableOnBackInvokedCallback` absent du manifest (pas de predictive back gesture Android 14+)
- Les fragments Leanback gerent le back via leur propre mecanisme (`BrowseSupportFragment.setOnItemViewClickedListener`)
- Les fragments Compose delegent au `NavigationRepository`

---

## 8. Plan de refactoring par priorite (ROI)

### Etape 1 — Ajouter la gestion d'erreur unifiee aux UiState

**Quoi** : Ajouter un champ `error: UiError?` a tous les `*UiState` data classes et afficher les erreurs au lieu de les ignorer silencieusement.

**Fichiers** :
- `ItemDetailsViewModel.kt` : ajouter `error` a `ItemDetailsUiState`
- `LibraryBrowseViewModel.kt` : ajouter `error` a `LibraryBrowseUiState`
- `MusicBrowseViewModel.kt`, `GenresGridViewModel.kt`, `LiveTvBrowseViewModel.kt` : idem
- Creer un composable `ErrorBanner` reutilisable

**Effort** : Petit
**Risque** : Tres faible — ajout pur, aucune modification de logique existante
**ROI** : Haut — les erreurs reseau sont actuellement invisibles pour l'utilisateur

---

### Etape 2 — Decloisonner ItemDetailsFragment.kt

**Quoi** : Extraire les composables inline en fichiers separes par type d'item.

**Fichiers a creer** :
- `ItemDetailsMovieContent.kt` : contenu specifique films
- `ItemDetailsSeriesContent.kt` : contenu specifique series (saisons, episodes)
- `ItemDetailsMusicContent.kt` : contenu specifique musique (albums, pistes)
- `ItemDetailsPersonContent.kt` : contenu specifique personnes
- `TrackSelectionDialog.kt` : dialogs de selection audio/sous-titres/version

**Effort** : Moyen
**Risque** : Faible — extraction de composables sans changement de logique
**ROI** : Haut — le fichier de 2047 lignes est le plus modifie et le plus risque lors des merges

---

### Etape 3 — Scoper le DI Koin

**Quoi** : Remplacer les `single` non necessaires par des scopes ou factories.

**Actions** :
- `SyncPlayManager` : scoper a la session SyncPlay (et ajouter un `destroy()`)
- `MediaBarSlideshowViewModel` : convertir de `single` en `viewModel`
- Identifier les singletons stateful qui devraient etre scopes (BackgroundService, SocketHandler)
- Supprimer les heritages `KoinComponent` inutiles (19 fichiers)

**Effort** : Moyen
**Risque** : Moyen — les changements de scope peuvent creer des bugs si des references persistent
**ROI** : Moyen — reduit la consommation memoire et ameliore la testabilite

---

### Etape 4 — Decomposer JellyseerrViewModel

**Quoi** : Eclater le ViewModel de 919 lignes en ViewModels specialises.

**Decomposition** :
- `JellyseerrDiscoveryViewModel` : trending, upcoming, genres (~300 lignes)
- `JellyseerrRequestsViewModel` : requetes utilisateur, soumission (~200 lignes)
- `JellyseerrSearchViewModel` : recherche Jellyseerr (~100 lignes)
- `JellyseerrSettingsViewModel` : configuration, auth, test connexion (~200 lignes)
- `JellyseerrViewModel` : facade legere coordonnant les sous-ViewModels

**Effort** : Moyen
**Risque** : Moyen — les fragments Jellyseerr dependent fortement du ViewModel unique actuel
**ROI** : Moyen — facilite la maintenance et le testing du module Jellyseerr

---

### Etape 5 — Migrer les fragments Jellyseerr imperatifs vers Compose

**Quoi** : Remplacer le code `addView()` de `MediaDetailsFragment` (2033 lignes) et `PersonDetailsFragment` par des ecrans Compose.

**Actions** :
- Creer `JellyseerrMediaDetailsScreen.kt` (Compose)
- Creer `JellyseerrPersonDetailsScreen.kt` (Compose)
- Migrer `QualitySelectionDialog`, `SeasonSelectionDialog`, `AdvancedRequestOptionsDialog` vers Compose
- Utiliser les `ColorTokens` du module `design` au lieu des couleurs Tailwind hardcodees

**Effort** : Grand
**Risque** : Moyen — changement d'UI complet mais la logique reste dans le ViewModel
**ROI** : Haut — elimine ~100 couleurs hardcodees, unifie le look, supprime du code pre-2015

---

### Etape 6 — Decomposer JellyseerrHttpClient et JellyseerrRepository

**Quoi** : Eclater le client HTTP monolithique (1253 lignes) et le repository (766 lignes) en modules par domaine.

**Decomposition** :
- `JellyseerrDiscoveryApi` + `JellyseerrDiscoveryRepository` : trending, upcoming, genres, search
- `JellyseerrRequestApi` + `JellyseerrRequestRepository` : soumission, listing, statut
- `JellyseerrMediaApi` + `JellyseerrMediaRepository` : details films/series, saisons
- `JellyseerrUserApi` + `JellyseerrUserRepository` : auth, profil, quota
- `JellyseerrServiceApi` : settings, test connexion

**Effort** : Moyen
**Risque** : Faible — refactoring interne, l'interface publique peut rester identique via facades
**ROI** : Moyen — facilite le testing, la maintenance et l'ajout de nouveaux endpoints

---

### Etape 7 — Passer les arguments de navigation a des IDs

**Quoi** : Remplacer la serialisation JSON des `BaseItemDto` dans les arguments Fragment par de simples UUIDs.

**Fichiers** :
- `Destinations.kt` : les 8+ TODOs "only pass item id" sont deja marques
- Chaque fragment cible : charger l'item via le ViewModel au lieu de le deserialiser depuis les arguments

**Effort** : Moyen
**Risque** : Faible — necessite un appel API supplementaire mais plus robuste
**ROI** : Moyen — reduit la taille des bundles, elimine les crashes de deserialization

---

### Etape 8 — Consolider la navigation

**Quoi** : Unifier les deux systemes de navigation (NavigationRepository custom + Compose Router).

**Options** :
- Option A : Migrer progressivement vers AndroidX Navigation3 (deja dans les dependances : `navigation3-ui 1.0.0`)
- Option B : Enrichir le NavigationRepository custom pour supporter Compose nativement et supprimer le Router

**Effort** : Grand
**Risque** : Eleve — la navigation touche tous les ecrans, regression possible sur le backstack
**ROI** : Moyen a long terme — prerequis pour les deep links et predictive back

---

### Etape 9 — Reduire HomeRowsFragment (38+ dependances)

**Quoi** : Extraire les responsabilites du fragment qui injecte 38+ dependances.

**Decomposition** :
- `HomeRowsDataLoader` : chargement des rows (latest, resume, next up)
- `HomeRowsEventHandler` : gestion des clicks et selections
- `HomeRowsFocusManager` : gestion du focus D-pad et debouncing

**Effort** : Moyen
**Risque** : Moyen — fragment central de l'app, regression possible sur le home screen
**ROI** : Moyen — ameliore la maintenabilite du point d'entree principal

---

### Etape 10 — Migrer les ecrans Leanback restants vers Compose

**Quoi** : Convertir progressivement les fragments Leanback legacy.

**Ordre de migration** :
1. `AllFavoritesFragment` (simple, bon candidat de depart)
2. `CollectionFragment`
3. `BrowseFolderFragment` et ses 7 sous-classes (le plus complexe)
4. `HomeRowsFragment` (le plus risque, faire en dernier)

**Effort** : Grand
**Risque** : Eleve — les Presenters Leanback ont un comportement de focus D-pad specifique a reproduire en Compose
**ROI** : Haut a long terme — elimine la dualite Leanback/Compose, unifie le look

---

## 9. Tableau recapitulatif

| Etape | Action | Effort | Risque | ROI | Pre-requis |
|-------|--------|--------|--------|-----|------------|
| 1 | Gestion d'erreur unifiee dans UiState | Petit | Tres faible | Haut | Aucun |
| 2 | Decomposer ItemDetailsFragment | Moyen | Faible | Haut | Aucun |
| 3 | Scoper le DI Koin | Moyen | Moyen | Moyen | Aucun |
| 4 | Decomposer JellyseerrViewModel | Moyen | Moyen | Moyen | Aucun |
| 5 | Migrer Jellyseerr vers Compose | Grand | Moyen | Haut | Etape 4 |
| 6 | Decomposer HttpClient + Repository | Moyen | Faible | Moyen | Aucun |
| 7 | Arguments navigation → IDs | Moyen | Faible | Moyen | Aucun |
| 8 | Consolider la navigation | Grand | Eleve | Moyen | Etape 7 |
| 9 | Reduire HomeRowsFragment | Moyen | Moyen | Moyen | Aucun |
| 10 | Migrer Leanback → Compose | Grand | Eleve | Haut (LT) | Etapes 2, 8 |

---

## 10. Bonnes pratiques deja en place

| Aspect | Detail |
|--------|--------|
| MVVM coherent (v2) | 8/22 ViewModels avec UiState unifie + `collectAsState()` |
| Separation Fragment/ViewModel | Aucun appel API direct dans les Fragments |
| StateFlow immutable | Pattern `_state`/`state` avec `asStateFlow()` respecte partout |
| Coroutines lifecycle-aware | `viewModelScope`, `flowWithLifecycle`, `repeatOnLifecycle` |
| Pagination | `derivedStateOf` + chargement par page dans LibraryBrowse |
| DiffUtil | `MutableObjectAdapter.replaceAll()` + `ListAdapter` avec `DiffCallback` |
| Image caching | Coil 3 configure avec cache memoire 25% RAM + disque 250MB |
| Destinations typees | `Destinations` object avec fonctions typees et parametres |

---

*Audit genere le 2026-03-07 — Cible : Android 14, hardware haut de gamme (Ugoos AM9+ Pro)*
