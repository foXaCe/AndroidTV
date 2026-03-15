# Safe Args Refactor — Navigation Type-Safe

## Contexte

Le projet utilise un **système de navigation custom** (`NavigationRepository` + `Destinations` object) basé sur des `Destination.Fragment(KClass, Bundle)`. Il n'y a **ni Navigation Component** (pas de nav_graph.xml), **ni Compose Navigation** (pas de NavHost), donc le plugin SafeArgs classique ne s'applique pas.

## Solution implémentée

Chaque fragment recevant des arguments définit une `data class Args` imbriquée avec :
- **`toBundle(): Bundle`** — sérialise les args typés vers un Bundle
- **`companion object { fromBundle(bundle: Bundle?): Args? }`** — désérialise depuis un Bundle

`Destinations.kt` utilise `Fragment.Args(...).toBundle()` au lieu de `bundleOf("key" to value.toString())`.

### Infrastructure ajoutée

| Fichier | Description |
|---------|-------------|
| `ui/navigation/BundleExtensions.kt` | `Bundle.getUUID(key)` et `Bundle.requireUUID(key)` |
| `ui/navigation/Destination.kt` | Surcharge `fragmentDestination(Bundle)` |

## Inventaire des fragments migrés

| Fragment | Args | Champs typés |
|----------|------|-------------|
| ItemDetailComposeFragment | Args | `itemId: UUID`, `serverId: UUID?`, `channelId: UUID?`, `programInfoJson: String?`, `seriesTimerJson: String?` |
| ItemDetailsFragment (v2) | Args | `itemId: UUID`, `serverId: UUID?` |
| ItemListFragment | Args | `itemId: UUID`, `serverId: UUID?` |
| MusicFavoritesListFragment | Args | `parentId: UUID` |
| LibraryBrowseComposeFragment | sealed Args | `GenreArgs(genreName, parentId?, includeType?, serverId?, userId?, displayPrefsId?, parentItemId?)` / `LibraryArgs(folderJson, serverId?, userId?, includeType?)` |
| FolderBrowseComposeFragment | Args | `folderJson: String`, `serverId: UUID?`, `userId: UUID?` |
| GenresGridComposeFragment | Args | `folderJson: String?`, `includeType: String?` |
| ByLetterBrowseFragment | Args | `folderJson: String`, `includeType: String?` |
| CollectionBrowseFragment | Args | `folderJson: String` |
| SuggestedMoviesComposeFragment | Args | `folderJson: String` |
| LiveTvBrowseFragment | Args | `folderJson: String` |
| MusicBrowseFragment | Args | `folderJson: String`, `serverId: UUID?`, `userId: UUID?` |
| CustomPlaybackOverlayFragment | Args | `position: Int` |
| PhotoPlayerFragment | Args | `itemId: UUID`, `albumSortBy: ItemSortBy?`, `albumSortOrder: SortOrder?`, `autoPlay: Boolean` |
| NextUpFragment | Args | `itemId: UUID` |
| StillWatchingFragment | Args | `itemId: UUID` |
| TrailerPlayerFragment | Args | `videoId: String`, `startSeconds: Double`, `segmentsJson: String` |
| VideoPlayerFragment | Args | `position: Int?` |
| SearchComposeFragment | Args | `query: String?` |
| MediaDetailsFragment | Args | `itemJson: String` |
| PersonDetailsFragment | Args | `personId: Int`, `personName: String` |
| JellyseerrBrowseByFragment | Args | `filterId: Int`, `filterName: String`, `mediaType: String`, `filterType: BrowseFilterType` |

## Fragments hors scope (startup navigation)

| Fragment | Raison |
|----------|--------|
| ServerAddFragment | Navigation startup séparée |
| ServerFragment | Navigation startup séparée |
| UserLoginFragment | Navigation startup séparée |

## Résultats

- **Type de navigation** : Custom (`NavigationRepository` + `Destinations`)
- **Arguments migrés** : 22 fragments
- **`getString()` non typés restants** : 0 (dans le scope Destinations)
- **Magic strings dans Destinations.kt** : 0
- **`bundleOf("key" to ...)` dans Destinations.kt** : 0
- **BUILD SUCCESSFUL** ✓
