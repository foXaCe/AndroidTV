# V3A — Tests unitaires ViewModels

**Date** : 2026-03-08
**Statut** : ✅ Terminé — 79 tests, 0 failures
**Framework** : Kotest 6.1.3 (FunSpec) + MockK 1.14.9 + JUnit 5 Platform

---

## Dépendances (déjà présentes)

| Dépendance | Version | Usage |
|------------|---------|-------|
| `kotest-runner-junit5` | 6.1.3 | Framework de test (FunSpec) |
| `kotest-assertions` | 6.1.3 | Matchers (`shouldBe`, `shouldBeEmpty`, etc.) |
| `mockk` | 1.14.9 | Mocking (relaxed mocks, `every` blocks) |
| `kotlinx-coroutines-test` | 1.10.2 | `StandardTestDispatcher`, `Dispatchers.setMain` |
| `turbine` | 1.1.0 | Test des `Flow` (disponible pour usage futur) |

Configuration existante dans `app/build.gradle.kts` :
- `unitTests.isReturnDefaultValues = true` — support Android en tests unitaires
- `unitTests.all { it.useJUnitPlatform() }` — exécution via JUnit 5

---

## Fichiers de test

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

### 4. `app/src/test/kotlin/ui/itemdetail/v2/ItemDetailsViewModelTests.kt` — 37 tests

| # | Catégorie | Test | Vérifie |
|---|-----------|------|---------|
| 1 | Initial state | `initial state has default values` | isLoading=true, item=null, listes vides |
| 2 | toggleFavorite | `sets isFavorite to true when currently false` | Mise à jour optimiste false→true |
| 3 | toggleFavorite | `sets isFavorite to false when currently true` | Mise à jour optimiste true→false |
| 4 | toggleFavorite | `is no-op when item is null` | Pas de crash, item reste null |
| 5 | toggleWatched | `sets played to true and percentage to 100` | played=true, playedPercentage=100.0 |
| 6 | toggleWatched | `sets played to false and percentage to 0` | played=false, playedPercentage=0.0 |
| 7 | toggleWatched | `is no-op when item is null` | Pas de crash, item reste null |
| 8 | retry | `is no-op when lastItemId is null` | État inchangé |
| 9 | movePlaylistItem | `reorders tracks correctly` | [1,2,3] → move(0,2) → [2,3,1] |
| 10 | movePlaylistItem | `swaps adjacent tracks` | [A,B] → move(0,1) → [B,A] |
| 11 | movePlaylistItem | `is no-op with invalid indices` | Indices hors limites → pas de changement |
| 12 | movePlaylistItem | `is no-op without item` | item=null → pas de crash |
| 13 | movePlaylistItem | `is no-op without playlistItemId` | Tracks sans playlistItemId → pas de move |
| 14 | removeFromPlaylist | `removes track at index` | Track supprimée de la liste |
| 15 | removeFromPlaylist | `is no-op with invalid index` | Index hors limites → pas de changement |
| 16 | removeFromPlaylist | `is no-op without item` | item=null → pas de crash |
| 17 | removeFromPlaylist | `is no-op without playlistItemId` | Track sans playlistItemId → pas de remove |
| 18-37 | getMediaBadges | 20 tests (résolution, HDR, codec, container, audio) | Extraction correcte des badges médias |

**Détail des tests getMediaBadges :**

| # | Test | Badge vérifié |
|---|------|---------------|
| 18 | `returns 4K badge for width >= 3800` | "4K" |
| 19 | `returns 1080p badge for width >= 1900` | "1080p" |
| 20 | `returns 720p badge for width >= 1260` | "720p" |
| 21 | `returns no resolution badge for width below 1260` | Absence de badge résolution |
| 22 | `returns HDR10 badge` | "HDR10" |
| 23 | `returns HDR10+ badge` | "HDR10+" |
| 24 | `returns DV badge for Dolby Vision` | "DV" |
| 25 | `returns DV + HDR10 for DOVI_WITH_HDR10` | "DV" + "HDR10" |
| 26 | `returns no HDR badge for SDR` | Absence de badge HDR/DV |
| 27 | `extracts HEVC codec` | "HEVC" |
| 28 | `extracts AV1 codec` | "AV1" |
| 29 | `extracts container format` | "MKV" |
| 30 | `extracts 5.1 surround sound` | "5.1" |
| 31 | `extracts 7.1 surround sound` | "7.1" |
| 32 | `extracts stereo` | "Stereo" |
| 33 | `extracts audio codec` | "AC3" |
| 34 | `extracts TrueHD audio codec` | "TrueHD" |
| 35 | `returns empty list when no media sources` | Liste vide |
| 36 | `returns empty list when media streams are null` | Liste vide |
| 37 | `complete 4K HDR10 HEVC 5.1 AC3 MKV item` | Tous les badges combinés |

### 5. `app/src/test/kotlin/ui/playback/VideoSpeedControllerTests.kt` — 8 tests (préexistant)
### 6. `app/src/test/kotlin/ui/playback/overlay/CustomSeekProviderTests.kt` — 3 tests (préexistant)
### 7. `app/src/test/kotlin/util/TimeUtilsTests.kt` — 2 tests (préexistant)

---

## Résultat `./gradlew test`

```
BUILD SUCCESSFUL in 35s
171 actionable tasks: 45 executed, 126 up-to-date

Tests par fichier (githubDebug + playstoreDebug) :
  UiErrorMappingTests           : 10 tests
  LibraryBrowseViewModelTests   : 11 tests
  GenresGridViewModelTests      :  8 tests
  ItemDetailsViewModelTests     : 37 tests
  VideoSpeedControllerTests     :  8 tests
  CustomSeekProviderTests       :  3 tests
  TimeUtilsTests                :  2 tests
  ──────────────────────────────────────
  TOTAL (par flavor)            : 79 tests passed, 0 failed
  TOTAL (2 flavors × 79)        : 158 tests exécutés, 0 failed
```

---

## CI : Tests dans le pipeline

Fichier `.github/workflows/ci.yml` — les tests sont exécutés via :

```yaml
- name: Unit Tests
  run: ./gradlew test

- name: Upload test report
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: test-reports
    path: app/build/reports/tests/
```

---

## Mocks complexes documentés

### Problème : constructeurs kotlinx.serialization du SDK Jellyfin

Les data classes du Jellyfin SDK (`BaseItemDto`, `UserItemDataDto`, `MediaSourceInfo`, `MediaStream`) utilisent `@Serializable` de kotlinx.serialization. Le plugin génère un constructeur interne avec des bitmasks pour les défauts — les paramètres ne sont **pas optionnels** au niveau Kotlin.

**Conséquence** : `BaseItemDto(id = ...)` échoue à la compilation.

### Solution 1 : Désérialisation JSON (`realItem()`)

Pour les tests nécessitant `.copy()` (toggleFavorite, toggleWatched) :

```kotlin
val testJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

fun realItem(
    id: UUID = UUID.randomUUID(),
    isFavorite: Boolean = false,
    played: Boolean = false,
    playedPercentage: Double? = null,
): BaseItemDto = testJson.decodeFromString(
    BaseItemDto.serializer(),
    buildString {
        append("""{"Id":"$id","Type":"Movie","UserData":{""")
        append(""""IsFavorite":$isFavorite,"Played":$played""")
        append(""","PlaybackPositionTicks":0,"PlayCount":0,"Key":"","ItemId":"$id"""")
        if (playedPercentage != null) append(""","PlayedPercentage":$playedPercentage""")
        append("}}")
    },
)
```

**Champs requis découverts** :
- `BaseItemDto` : `Type` (obligatoire, pas de défaut)
- `UserItemDataDto` : `PlaybackPositionTicks`, `PlayCount`, `Key`, `ItemId` (tous obligatoires)

### Solution 2 : MockK relaxed (`mockItem()`, `mockVideoStream()`, etc.)

Pour les tests qui lisent des propriétés sans `.copy()` :

```kotlin
fun mockItem(
    id: UUID = UUID.randomUUID(),
    name: String? = null,
    playlistItemId: String? = null,
    mediaSources: List<MediaSourceInfo>? = null,
) = mockk<BaseItemDto>(relaxed = true) {
    every { this@mockk.id } returns id
    every { this@mockk.name } returns name
    every { this@mockk.playlistItemId } returns playlistItemId
    every { this@mockk.mediaSources } returns mediaSources
}
```

### Accès à l'état privé via réflexion

Tous les ViewModels exposent `uiState: StateFlow` (read-only) mais stockent en interne `_uiState: MutableStateFlow`. Pour injecter un état de test :

```kotlin
fun getMutableState(vm: ItemDetailsViewModel): MutableStateFlow<ItemDetailsUiState> {
    val field = vm.javaClass.getDeclaredField("_uiState")
    field.isAccessible = true
    return field.get(vm) as MutableStateFlow<ItemDetailsUiState>
}
```

Pattern identique utilisé dans `GenresGridViewModelTests` et `LibraryBrowseViewModelTests`.

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
