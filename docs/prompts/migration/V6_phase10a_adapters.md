# Phase 10a — Suppression des adapters Leanback

## Objectif

Supprimer les 3 adapters Leanback (`MutableObjectAdapter`, `AggregatedItemRowAdapter`, `ItemRowAdapter`) et leur helper, qui constituaient du dead code après les migrations Compose des phases 5-9.

## Analyse pré-migration

### MutableObjectAdapter.kt (3 imports Leanback)
- Wrapper autour de `MutableList<T>` étendant `ObjectAdapter` (Leanback)
- Utilisait `Presenter`, `PresenterSelector`, DiffUtil pour les notifications
- **Utilisé par** : ItemRowAdapter, AggregatedItemRowAdapter, TvManager (dead code), ItemLauncher (toujours `null`)

### AggregatedItemRowAdapter.kt (1 import Leanback)
- Étendait `MutableObjectAdapter<BaseRowItem>` avec pagination par chunks
- **Zéro appelant externe** — uniquement référencé dans son propre fichier

### ItemRowAdapter.java (6 imports Leanback)
- Étendait `MutableObjectAdapter<Object>`, gérait 20+ types de requêtes
- **Seule instanciation** : `TvManager.addRow()` — dead code, `getScheduleRowsAsync()` n'avait aucun appelant externe

### ItemRowAdapterHelper.kt (0 import Leanback)
- 890 LOC de fonctions d'extension sur `ItemRowAdapter`
- **Uniquement appelé par** ItemRowAdapter.java

## Fichiers supprimés

| Fichier | LOC | Imports Leanback |
|---------|-----|-----------------|
| `ui/presentation/MutableObjectAdapter.kt` | 95 | 3 |
| `ui/itemhandling/AggregatedItemRowAdapter.kt` | 107 | 1 |
| `ui/itemhandling/ItemRowAdapter.java` | 803 | 6 |
| `ui/itemhandling/ItemRowAdapterHelper.kt` | 890 | 0 |
| **Total supprimé** | **1895** | **10** |

## Fichiers modifiés

### TvManager.java (−4 imports Leanback)
- Suppression de `getScheduleRowsAsync()` et `addRow()` (dead code)
- Suppression des imports : `HeaderItem`, `ListRow`, `Presenter`, `Row`
- Suppression des imports : `ItemRowAdapter`, `MutableObjectAdapter`

### ItemLauncher.java (refactoring)
- Signature `launch(BaseRowItem, MutableObjectAdapter<Object>, Context)` → `launch(BaseRowItem, Context)`
- Suppression de tout le code dépendant de l'adapter (toujours `null` en pratique)
- AUDIO : simplifié — queue items via MediaManager, sinon lecture item unique
- PHOTO : ouvre directement le photo player (sans sort params de l'adapter)
- VIDEO : suppression de la vérification photos-in-adapter (dead code)
- Suppression des imports : `MutableObjectAdapter`, `QueryType`

### 15 appelants de ItemLauncher.launch() mis à jour
- Suppression du paramètre `null` dans tous les appels
- Fichiers : HomeComposeFragment, LibraryBrowseComposeFragment, FolderBrowseComposeFragment, AllFavoritesFragment, FolderViewFragment, ByLetterBrowseFragment, SuggestedMoviesFragment, CollectionBrowseFragment, SearchComposeFragment, MusicBrowseFragment, LiveTvBrowseFragment, RecordingsBrowseFragment, ScheduleBrowseFragment, ItemListFragment, MusicFavoritesListFragment

## Compteur Leanback

| Métrique | Avant Phase 10a | Après Phase 10a | Delta |
|----------|-----------------|-----------------|-------|
| Imports Leanback (source) | 26 | 12 | **−14** |
| Fichiers avec imports Leanback | 9 | 5 | **−4** |
| LOC supprimées | — | 1895 | **−1895** |

### Fichiers Leanback restants (5 fichiers, 12 imports)

#### Player overlay (9 imports, 2 fichiers)
- `TransportControlManager.kt` — 8 imports (widgets transport controls)
- `CustomAction.kt` — 1 import (`PlaybackControlsRow.MultiAction`)

#### Présentation (2 imports, 2 fichiers)
- `TextItemPresenter.kt` — 1 import (`Presenter`)
- `CardPresenter.kt` — 1 import (`Presenter`)

#### UI partagé (1 import, 1 fichier)
- `TitleView.kt` — 1 import (`TitleViewAdapter`)

## Dead code additionnel identifié

- `BrowseRowDef.kt` — uniquement référencé dans son propre fichier (0 appelant)
- `GetUserViewsRequest`, `GetAdditionalPartsRequest`, `GetTrailersRequest` — data classes de queries sans importeurs

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
```
