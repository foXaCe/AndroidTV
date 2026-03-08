# V3C Decomposition JellyseerrViewModel — Plan & Rapport

> **Date : 2026-03-08 — Etape 4 du plan d'architecture (05_architecture.md)**
> **Objectif** : Decomposer JellyseerrViewModel.kt (919 lignes, 29 StateFlows, 8 domaines)

---

## 1. Cartographie des responsabilites actuelles

### 1.1 StateFlows exposes (14)

| StateFlow | Domaine | Consommateurs |
|-----------|---------|---------------|
| `loadingState` | Partage | SettingsFragment, RequestsFragment, DiscoverRowsFragment |
| `trending` | Discover | DiscoverRowsFragment |
| `trendingMovies` | Discover | DiscoverRowsFragment |
| `trendingTv` | Discover | DiscoverRowsFragment |
| `upcomingMovies` | Discover | DiscoverRowsFragment |
| `upcomingTv` | Discover | DiscoverRowsFragment |
| `movieGenres` | Discover | DiscoverRowsFragment |
| `tvGenres` | Discover | DiscoverRowsFragment |
| `networks` | Discover | DiscoverRowsFragment |
| `studios` | Discover | DiscoverRowsFragment |
| `userRequests` | Details | DiscoverRowsFragment, RequestsFragment |
| `searchResults` | **CODE MORT** | Aucun consommateur |
| `isAvailable` | Auth | SettingsFragment, RequestsFragment, DiscoverRowsFragment |
| `isMoonfinMode` | **CODE MORT** | Aucun (SettingsJellyseerrScreen lit directement repository) |

### 1.2 Fonctions publiques (30) — par domaine

**AUTH (4 actives, 2 mortes) :**
| Fonction | Consommateurs | Etat |
|----------|---------------|------|
| `initializeJellyseerr(serverUrl, apiKey)` | SettingsFragment | ACTIVE |
| `loginWithJellyfin(username, password, jellyfinUrl, jellyseerrUrl)` | SettingsFragment | ACTIVE |
| `loginLocal(email, password, jellyseerrUrl)` | Aucun | **CODE MORT** |
| `regenerateApiKey()` | Aucun | **CODE MORT** |

**DISCOVER (12 actives) :**
| Fonction | Consommateurs |
|----------|---------------|
| `hasContent()` | DiscoverRowsFragment |
| `loadTrendingContent()` | DiscoverRowsFragment |
| `loadGenres()` | DiscoverRowsFragment |
| `loadNextTrendingPage()` | DiscoverRowsFragment |
| `loadNextTrendingMoviesPage()` | DiscoverRowsFragment |
| `loadNextTrendingTvPage()` | DiscoverRowsFragment |
| `loadNextUpcomingMoviesPage()` | DiscoverRowsFragment |
| `loadNextUpcomingTvPage()` | DiscoverRowsFragment |
| `discoverMovies(...)` | BrowseByFragment |
| `discoverTv(...)` | BrowseByFragment |

**DETAILS (14 actives, 1 morte) :**
| Fonction | Consommateurs | Etat |
|----------|---------------|------|
| `getMovieDetails(tmdbId)` | MediaDetailsFragment, DiscoverFragment | ACTIVE |
| `getTvDetails(tmdbId)` | MediaDetailsFragment, DiscoverFragment | ACTIVE |
| `getSimilarMovies(tmdbId, page)` | MediaDetailsFragment | ACTIVE |
| `getSimilarTv(tmdbId, page)` | MediaDetailsFragment | ACTIVE |
| `getRecommendationsMovies(tmdbId, page)` | MediaDetailsFragment | ACTIVE |
| `getRecommendationsTv(tmdbId, page)` | MediaDetailsFragment | ACTIVE |
| `getPersonDetails(personId)` | PersonDetailsFragment | ACTIVE |
| `getPersonCombinedCredits(personId)` | PersonDetailsFragment | ACTIVE |
| `requestMedia(item, seasons, is4k, advancedOptions)` | MediaDetailsFragment | ACTIVE |
| `cancelRequest(requestId)` | MediaDetailsFragment | ACTIVE |
| `loadRequests()` | RequestsFragment, DiscoverRowsFragment | ACTIVE |
| `getCurrentUser()` | MediaDetailsFragment | ACTIVE |
| `getRadarrServers()` / `getRadarrServerDetails()` | MediaDetailsFragment | ACTIVE |
| `getSonarrServers()` / `getSonarrServerDetails()` | MediaDetailsFragment | ACTIVE |
| `createRequest(mediaId, mediaType, seasons)` | Aucun | **CODE MORT** |

**SEARCH (morte) :**
| Fonction | Consommateurs | Etat |
|----------|---------------|------|
| `search(query, mediaType)` | Aucun | **CODE MORT** |

> Note : SearchFragment utilise SearchViewModel qui appelle `jellyseerrRepository.search()` directement.

**SETTINGS (mortes) :**
| Fonction | Consommateurs | Etat |
|----------|---------------|------|
| `getRadarrSettings()` | Aucun | **CODE MORT** |
| `getSonarrSettings()` | Aucun | **CODE MORT** |

### 1.3 Code mort identifie — 7 elements (~150 lignes)

1. `search()` + `_searchResults` / `searchResults` (lignes 185-186, 822-868)
2. `loginLocal()` (lignes 228-230)
3. `regenerateApiKey()` (lignes 232-234)
4. `createRequest()` (lignes 683-701) — `requestMedia()` est utilise a la place
5. `getRadarrSettings()` (ligne 891)
6. `getSonarrSettings()` (ligne 893)
7. `isMoonfinMode` (ligne 189) — `SettingsJellyseerrScreen` lit directement depuis le repository

### 1.4 Dependances entre domaines

```
AUTH ──────────────────────────────── isAvailable ──▶ DISCOVER (guard)
                                                  ──▶ DETAILS (guard dans loadRequests)

DETAILS ── requestContent() appelle loadRequestsSuspend() ── interne

DISCOVER ── aucune dependance vers AUTH/DETAILS

JellyseerrDiscoverRowsFragment utilise DISCOVER + DETAILS (userRequests, loadRequests)
RequestsFragment utilise DETAILS uniquement
MediaDetailsFragment utilise DETAILS uniquement
SettingsFragment utilise AUTH uniquement
BrowseByFragment utilise DISCOVER uniquement (discoverMovies/discoverTv)
DiscoverFragment utilise DETAILS uniquement (getMovieDetails/getTvDetails)
PersonDetailsFragment utilise DETAILS uniquement
```

---

## 2. Decomposition proposee — 3 ViewModels (pas 4)

> **Changement vs proposition initiale** : `JellyseerrSearchViewModel` est inutile car
> `search()`/`searchResults` sont du code mort. Le code est supprime.

### 2.1 JellyseerrAuthViewModel (~70 lignes)

**Responsabilite** : Authentification, initialisation, lifecycle

| Element | Type |
|---------|------|
| `loadingState` | StateFlow (propre) |
| `isAvailable` | StateFlow (pass-through repository) |
| `initializeJellyseerr(serverUrl, apiKey)` | Fonction |
| `loginWithJellyfin(username, password, jellyfinUrl, jellyseerrUrl)` | Fonction |
| init block | Auto-initialisation du repository |
| `onCleared()` | `repository.close()` |

**Consommateur** : SettingsFragment

### 2.2 JellyseerrDetailsViewModel (~290 lignes)

**Responsabilite** : Details media, details personne, demandes (CRUD), configuration serveurs

| Element | Type |
|---------|------|
| `loadingState` | StateFlow (propre) |
| `userRequests` | StateFlow |
| `isAvailable` | StateFlow (pass-through repository) |
| `getMovieDetails(tmdbId)` | suspend fun |
| `getTvDetails(tmdbId)` | suspend fun |
| `getSimilarMovies(tmdbId, page)` | suspend fun |
| `getSimilarTv(tmdbId, page)` | suspend fun |
| `getRecommendationsMovies(tmdbId, page)` | suspend fun |
| `getRecommendationsTv(tmdbId, page)` | suspend fun |
| `getPersonDetails(personId)` | suspend fun |
| `getPersonCombinedCredits(personId)` | suspend fun |
| `requestMedia(item, seasons, is4k, advancedOptions)` | suspend fun |
| `cancelRequest(requestId)` | suspend fun |
| `loadRequests()` | fun |
| `getCurrentUser()` | suspend fun |
| `getRadarrServers()` / `getRadarrServerDetails()` | suspend fun |
| `getSonarrServers()` / `getSonarrServerDetails()` | suspend fun |
| `requestContent()` | private suspend fun |
| `loadRequestsSuspend()` | private suspend fun |
| `getPreferences()` | private suspend fun |
| `isWithinDays()` | private fun |

**Consommateurs** : MediaDetailsFragment, PersonDetailsFragment, DiscoverFragment, RequestsFragment, JellyseerrDiscoverRowsFragment

### 2.3 JellyseerrDiscoverViewModel (~460 lignes)

**Responsabilite** : Contenu trending, upcoming, genres, pagination, browse-by

| Element | Type |
|---------|------|
| `loadingState` | StateFlow (propre) |
| `trending` | StateFlow |
| `trendingMovies` | StateFlow |
| `trendingTv` | StateFlow |
| `upcomingMovies` | StateFlow |
| `upcomingTv` | StateFlow |
| `movieGenres` | StateFlow |
| `tvGenres` | StateFlow |
| `networks` | StateFlow |
| `studios` | StateFlow |
| `isAvailable` | StateFlow (pass-through repository) |
| `hasContent()` | fun |
| `loadTrendingContent()` | fun |
| `loadGenres()` | fun |
| `loadNextTrendingPage()` | fun |
| `loadNextTrendingMoviesPage()` | fun |
| `loadNextTrendingTvPage()` | fun |
| `loadNextUpcomingMoviesPage()` | fun |
| `loadNextUpcomingTvPage()` | fun |
| `discoverMovies(...)` | suspend fun |
| `discoverTv(...)` | suspend fun |
| `POPULAR_NETWORKS` | companion val |
| `POPULAR_STUDIOS` | companion val |
| `filterNsfw()` | private fun |
| `getPreferences()` | private suspend fun |

**Consommateurs** : JellyseerrDiscoverRowsFragment, JellyseerrBrowseByFragment

### 2.4 JellyseerrLoadingState (fichier separe)

La sealed class `JellyseerrLoadingState` est extraite dans son propre fichier car utilisee par les 3 ViewModels et les fragments.

---

## 3. Mapping consommateurs → ViewModels

| Fragment | Ancien VM | Nouveau(x) VM |
|----------|-----------|----------------|
| SettingsFragment | JellyseerrViewModel | **JellyseerrAuthViewModel** |
| MediaDetailsFragment | JellyseerrViewModel | **JellyseerrDetailsViewModel** |
| PersonDetailsFragment | JellyseerrViewModel | **JellyseerrDetailsViewModel** |
| DiscoverFragment | JellyseerrViewModel | **JellyseerrDetailsViewModel** |
| RequestsFragment | JellyseerrViewModel | **JellyseerrDetailsViewModel** |
| JellyseerrDiscoverRowsFragment | JellyseerrViewModel | **JellyseerrDiscoverViewModel** + **JellyseerrDetailsViewModel** |
| JellyseerrBrowseByFragment | JellyseerrViewModel | **JellyseerrDiscoverViewModel** |

---

## 4. Plan d'execution

| Etape | Action | Impact |
|-------|--------|--------|
| 0 | Supprimer le code mort (~150 lignes) | 919 → ~770 lignes |
| 1 | Extraire JellyseerrLoadingState dans son propre fichier | Prerequis pour les 3 VMs |
| 2 | Extraire JellyseerrAuthViewModel | SettingsFragment mis a jour |
| 3 | Extraire JellyseerrDetailsViewModel | 5 fragments mis a jour |
| 4 | Renommer JellyseerrViewModel → JellyseerrDiscoverViewModel | 2 fragments mis a jour |
| 5 | Mise a jour Koin (AppModule) | 1 `viewModel` → 3 `viewModel` |

BUILD SUCCESSFUL requis a chaque etape.

---

## 5. Rapports d'execution

### Etape 0 — Suppression code mort (BUILD SUCCESSFUL)

**Code supprime :**
- `search()` + `_searchResults` / `searchResults` (~50 lignes)
- `loginLocal()` (3 lignes)
- `regenerateApiKey()` (3 lignes)
- `createRequest()` (19 lignes)
- `getRadarrSettings()` (1 ligne)
- `getSonarrSettings()` (1 ligne)
- `isMoonfinMode` (1 ligne)

**Impact** : 919 → 835 lignes (-84 lignes de code mort)

### Etape 1 — Extraction JellyseerrLoadingState (BUILD SUCCESSFUL)

**Fichier cree :** `JellyseerrLoadingState.kt` (8 lignes)
Sealed class extraite dans son propre fichier, partagee par les 3 ViewModels.

### Etape 2 — Extraction JellyseerrAuthViewModel (BUILD SUCCESSFUL)

**Fichier cree :** `JellyseerrAuthViewModel.kt` (62 lignes)
- `loadingState`, `isAvailable` (StateFlows)
- `initializeJellyseerr()`, `loginWithJellyfin()` (fonctions)
- init block (auto-initialisation), `onCleared()` (lifecycle)

**Fragment mis a jour :** SettingsFragment → `JellyseerrAuthViewModel`
**Koin mis a jour :** `viewModel { JellyseerrAuthViewModel(get()) }` ajoute

### Etape 3 — Extraction JellyseerrDetailsViewModel (BUILD SUCCESSFUL)

**Fichier cree :** `JellyseerrDetailsViewModel.kt` (312 lignes)
- `loadingState`, `userRequests`, `isAvailable` (StateFlows)
- 16 fonctions publiques (details media, personne, requetes, serveurs)
- 4 fonctions privees (requestContent, loadRequestsSuspend, getPreferences, isWithinDays)

**Fragments mis a jour :**
- MediaDetailsFragment → `JellyseerrDetailsViewModel`
- PersonDetailsFragment → `JellyseerrDetailsViewModel`
- DiscoverFragment → `JellyseerrDetailsViewModel`
- RequestsFragment → `JellyseerrDetailsViewModel`
- JellyseerrDiscoverRowsFragment → ajoute `detailsViewModel` (pour `userRequests` et `loadRequests`)

**Koin mis a jour :** `viewModel { JellyseerrDetailsViewModel(get()) }` ajoute

### Etape 4 — Renommage JellyseerrViewModel → JellyseerrDiscoverViewModel (BUILD SUCCESSFUL)

**Fichier renomme :** `JellyseerrViewModel.kt` → `JellyseerrDiscoverViewModel.kt` (510 lignes)
- Classe renommee `JellyseerrDiscoverViewModel`
- Contenu : trending, upcoming, genres, pagination, browse-by, companion (networks/studios)

**Fragments mis a jour :**
- JellyseerrDiscoverRowsFragment → `JellyseerrDiscoverViewModel`
- JellyseerrBrowseByFragment → `JellyseerrDiscoverViewModel`

**Koin mis a jour :** `viewModel { JellyseerrDiscoverViewModel(get()) }`

---

## 6. Bilan final

| Metrique | Avant | Apres |
|----------|-------|-------|
| JellyseerrViewModel.kt | 919 lignes, 1 fichier | Supprime (renomme) |
| JellyseerrLoadingState.kt | (inline) | 8 lignes |
| JellyseerrAuthViewModel.kt | — | 62 lignes |
| JellyseerrDetailsViewModel.kt | — | 312 lignes |
| JellyseerrDiscoverViewModel.kt | — | 510 lignes |
| Total lignes ViewModel | 919 | 892 (3 fichiers + sealed class) |
| Code mort supprime | 0 | 84 lignes (7 elements) |
| Fichier le plus gros | 919 lignes | 510 lignes (DiscoverVM) |
| Domaines par ViewModel | 8 (tous melanges) | 1-2 max par ViewModel |
| Declarations Koin | 1 viewModel | 3 viewModels |
| Compilation | BUILD SUCCESSFUL | BUILD SUCCESSFUL (toutes etapes) |

### Mapping final consommateurs → ViewModels

| Fragment | ViewModel(s) |
|----------|-------------|
| SettingsFragment | JellyseerrAuthViewModel |
| MediaDetailsFragment | JellyseerrDetailsViewModel |
| PersonDetailsFragment | JellyseerrDetailsViewModel |
| DiscoverFragment | JellyseerrDetailsViewModel |
| RequestsFragment | JellyseerrDetailsViewModel |
| JellyseerrDiscoverRowsFragment | JellyseerrDiscoverViewModel + JellyseerrDetailsViewModel |
| JellyseerrBrowseByFragment | JellyseerrDiscoverViewModel |
