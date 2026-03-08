# V3A — Tests unitaires ViewModels

**Date** : 2026-03-08
**Statut** : ✅ Terminé — 29 tests, 0 failures
**Framework** : Kotest 6.1.3 (FunSpec) + MockK 1.14.9 + JUnit 5 Platform

---

## Dépendances ajoutées

| Dépendance | Version | Usage |
|------------|---------|-------|
| `kotlinx-coroutines-test` | 1.10.2 | `StandardTestDispatcher`, `Dispatchers.setMain` |
| `turbine` | 1.1.0 | Test des `Flow` (disponible pour usage futur) |

Ajout de `unitTests.isReturnDefaultValues = true` dans `testOptions` pour le support Android en tests unitaires.

---

## Fichiers de test créés

### 1. `app/src/test/kotlin/ui/base/state/UiErrorMappingTests.kt` — 10 tests

Tests de la fonction pure `Throwable.toUiError()` :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `IOException maps to Network error` | IOException → UiError.Network |
| 2 | `SocketTimeoutException maps to Timeout error` | SocketTimeoutException → UiError.Timeout |
| 3 | `UnknownHostException maps to Network error` | UnknownHostException → UiError.Network |
| 4 | `ApiClientException with IOException cause maps to Network` | ApiClientException(cause=IOException) → UiError.Network |
| 5 | `ApiClientException with SocketTimeoutException cause maps to Timeout` | ApiClientException(cause=SocketTimeout) → UiError.Timeout |
| 6 | `ApiClientException with UnknownHostException cause maps to Network` | ApiClientException(cause=UnknownHost) → UiError.Network |
| 7 | `ApiClientException with generic cause maps to Server` | ApiClientException(cause=IllegalState) → UiError.Server |
| 8 | `ApiClientException with null cause maps to Server` | ApiClientException(cause=null) → UiError.Server |
| 9 | `Unknown exception maps to Unknown error` | IllegalArgumentException → UiError.Unknown |
| 10 | `RuntimeException maps to Unknown error` | RuntimeException → UiError.Unknown |

### 2. `app/src/test/kotlin/ui/browsing/v2/LibraryBrowseViewModelTests.kt` — 11 tests

| # | Test | Vérifie |
|---|------|---------|
| 1 | `initial state has default values` | État initial : isLoading=true, error=null, items vide |
| 2 | `setFocusedItem updates focused item` | focusedItem mis à jour |
| 3 | `setSortOption updates current sort option` | currentSortOption mis à jour |
| 4 | `toggleFavorites toggles filter state` | filterFavorites bascule true↔false |
| 5 | `setPlayedFilter updates filter` | filterPlayed: ALL→WATCHED→UNWATCHED→ALL |
| 6 | `setSeriesStatusFilter updates filter` | filterSeriesStatus: CONTINUING, ENDED |
| 7 | `setStartLetter updates letter filter` | startLetter: "A"→null |
| 8 | `loadMore does nothing when no more items` | État inchangé quand hasMoreItems=false |
| 9 | `refreshDisplayPreferences returns false when not initialized` | false sans libraryPreferences |
| 10 | `retry clears error and starts loading when in genre mode` | error→null, isLoading→true via coroutine |
| 11 | `retry is no-op when not initialized` | État inchangé (folder=null, isGenreMode=false) |

**Mocks complexes** :
- `ApiClient`, `ApiClientFactory`, `PreferencesRepository`, `MultiServerRepository`, `UserPreferences` : tous relaxed MockK
- Accès au `_uiState` MutableStateFlow via réflexion pour injecter un état d'erreur avant test de `retry()`
- `genreFilter` field privé accessible via réflexion pour activer le mode genre
- `StandardTestDispatcher` + `runCurrent()` pour contrôler l'exécution des coroutines

### 3. `app/src/test/kotlin/ui/browsing/v2/GenresGridViewModelTests.kt` — 8 tests

| # | Test | Vérifie |
|---|------|---------|
| 1 | `initial state has default values` | isLoading=true, error=null, genres vide |
| 2 | `sortOptions contains all enum entries` | 5 options de tri présentes |
| 3 | `setFocusedGenre updates focused genre` | focusedGenre mis à jour |
| 4 | `setSortOption updates sort and re-sorts genres` | Tri NAME_ASC/DESC, MOST/LEAST_ITEMS vérifié |
| 5 | `setSortOption sets totalGenres correctly` | totalGenres=2, isLoading=false après tri |
| 6 | `retry clears error and sets loading` | error→null, isLoading→true (synchrone) |
| 7 | `retry with server error clears to loading` | UiError.Server→null |
| 8 | `empty genres results in empty state with no error` | genres vide, error=null |

**Mocks complexes** :
- `allGenres` MutableList privée accessible via réflexion pour injecter des genres de test
- `_uiState` MutableStateFlow via réflexion pour injecter un état d'erreur

---

## Résultat `./gradlew :app:testGithubDebugUnitTest`

```
BUILD SUCCESSFUL

Tests par fichier :
  UiErrorMappingTests           : 10 tests, 0 failures, 0 errors
  LibraryBrowseViewModelTests   : 11 tests, 0 failures, 0 errors
  GenresGridViewModelTests      :  8 tests, 0 failures, 0 errors
  ──────────────────────────────────────
  TOTAL NOUVEAUX                : 29 tests passed, 0 failed

Tests existants (inchangés) :
  TimeUtilsTests                :  2 tests
  VideoSpeedControllerTests     :  8 tests
  CustomSeekProviderTests       :  3 tests

TOTAL GLOBAL                   : 42 tests passed, 0 failed
```

---

## Limites et pistes d'amélioration

### Ce qui n'est PAS testé (nécessite mocking SDK avancé)

- **Flow complet API → UiState** : Le Jellyfin SDK utilise des extension properties (`ApiClient.itemsApi`) qui créent des instances réelles en interne. Mocker la chaîne complète `getItems()` → `Response<BaseItemDtoQueryResult>` est complexe sans injecter un client HTTP de test.
- **`loadItems` success/error end-to-end** : Les scénarios success/network-error/server-error nécessitent soit un test d'intégration avec un serveur mock (OkHttp MockWebServer), soit une refactorisation pour injecter un `ItemsRepository` au lieu d'appeler directement le SDK.
- **`GenresGridViewModel.createGenreItem` (maxWidth=480)** : La valeur est hardcodée dans le source et confirmée dans l'audit spot check (audit 21). Un test unitaire nécessiterait de mocker `imageApi.getItemImageUrl()`.

### Refactorisation recommandée pour meilleure testabilité

Extraire les appels SDK dans des **Repository** injectables :
```kotlin
interface ItemsRepository {
    suspend fun getLibraryItems(params: LibraryQuery): LibraryResult
}
```
Cela permettrait de mocker simplement le repository en test unitaire sans toucher au SDK.
