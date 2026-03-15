# Phase 8 — Migration Live TV Details (FullDetailsFragment → Compose)

## Décision : CAS A — Extension de ItemDetailScreen

**Justification** : `FullDetailsFragment` n'était plus utilisé que pour 2 cas Live TV (channelDetails + seriesTimerDetails). Tous les autres types d'items étaient déjà migrés vers `ItemDetailComposeFragment` / `ItemDetailScreen`. Plutôt que de créer un écran dédié, nous avons étendu l'architecture existante avec 2 nouveaux composables et des méthodes ViewModel supplémentaires.

## Fichiers créés

| Fichier | Rôle |
|---------|------|
| `ui/itemdetail/v2/content/LiveTvDetailsContent.kt` | Composable pour détails programme TV (channel name, titre, horaires, poster, boutons Tune/Record/Record Series) |
| `ui/itemdetail/v2/content/SeriesTimerDetailsContent.kt` | Composable pour détails timer série (nom, overview, bouton Cancel Series, schedule) |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ui/itemdetail/v2/ItemDetailsViewModel.kt` | Ajout `programInfo`, `seriesTimerInfo`, `scheduleItems`, `isRecording`, `isRecordingSeries` au state ; méthodes `loadChannelProgram()`, `loadSeriesTimer()`, `loadScheduleItems()`, `toggleRecord()`, `toggleRecordSeries()`, `cancelSeriesTimer()` |
| `ui/itemdetail/compose/ItemDetailScreen.kt` | Dispatch `PROGRAM` → `LiveTvDetailsContent`, `seriesTimerInfo != null` → `SeriesTimerDetailsContent` |
| `ui/itemdetail/compose/ItemDetailComposeFragment.kt` | Parse `ChannelId`, `ProgramInfo` (JSON), `SeriesTimer` (JSON) depuis arguments ; appel des méthodes ViewModel appropriées |
| `ui/navigation/Destinations.kt` | `channelDetails` et `seriesTimerDetails` redirigent vers `ItemDetailComposeFragment` au lieu de `FullDetailsFragment` ; suppression de `itemDetailsLegacy` (dead code) |
| `ui/shared/toolbar/LeftSidebarNavigation.kt` | Fix pré-existant : remplacement de `R.id.rowsFragment` (supprimé en phase précédente) par détection via `activeButton` et `focusSearch(FOCUS_RIGHT)` |
| `ui/itemhandling/ItemRowAdapterHelper.kt` | Nettoyage commentaire mentionnant FullDetailsFragment |
| `ui/shuffle/ShuffleDialogLauncher.kt` | Nettoyage commentaire mentionnant FullDetailsFragment |
| `ui/playlist/AddToPlaylistDialogLauncher.kt` | Nettoyage commentaire mentionnant FullDetailsFragment |

## Fichiers supprimés

| Fichier | Imports Leanback |
|---------|-----------------|
| `ui/itemdetail/FullDetailsFragment.java` | 10 |
| `ui/itemdetail/FullDetailsFragmentHelper.kt` | 0 |
| `ui/itemdetail/MyDetailsOverviewRow.kt` | 1 |
| `ui/presentation/MyDetailsOverviewRowPresenter.kt` | 1 |
| `ui/presentation/CustomListRowPresenter.kt` | 3 |
| `ui/presentation/CustomRowHeaderPresenter.kt` | 2 |
| `ui/presentation/InfoCardPresenter.kt` | 1 |
| `res/layout/fragment_full_details.xml` | — |
| **Total** | **18 imports** |

## Presenters supprimés

- `MyDetailsOverviewRowPresenter` — étendait `RowPresenter`
- `CustomListRowPresenter` — étendait `ListRowPresenter`
- `CustomRowHeaderPresenter` — étendait `RowHeaderPresenter`
- `InfoCardPresenter` — étendait `Presenter`

## TvManager

`TvManager.java` n'est **pas supprimé** — encore utilisé par 8 fichiers (playback overlay, LiveTvGuide, etc.). La méthode `getScheduleRowsAsync()` n'est plus appelée (seul appelant = FullDetailsFragment supprimé), mais TvManager conserve d'autres responsabilités actives.

## Compteur Leanback

| Métrique | Avant Phase 8 | Après Phase 8 | Delta |
|----------|---------------|---------------|-------|
| Imports Leanback | 57 | 39 | **-18** |
| Fichiers avec Leanback | 25 | 18 | **-7** |

## Fichiers NON supprimés (encore utilisés)

- `DetailRowView.kt` — utilisé par `ItemListFragment`
- `RecordPopup.java` — utilisé par `ProgramGridCell`, `LiveProgramDetailPopup`
- `RecordingIndicatorView.kt` — utilisé par `ProgramGridCell`, `RecordPopup`

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
```
