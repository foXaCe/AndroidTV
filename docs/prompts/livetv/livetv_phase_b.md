# Live TV Phase B — PopupWindow → Compose Dialog Migration

## Objectif
Remplacer les 2 `PopupWindow` (RecordPopup, LiveProgramDetailPopup) et composants simples par des Composables Compose, en utilisant le `DialogBase` du design system.

## Résumé des changements

| Sous-tâche | Statut | Description |
|---|---|---|
| B1 | OK | `RecordPopup` → `RecordDialog` (Compose) |
| B2 | OK | `LiveProgramDetailPopup` → `ProgramDetailDialog` (Compose) |
| B3 | OK | `FriendlyDateButton` + `GuidePagingButton` → Composables |
| B4 | OK | Intégration dans fragments via `ComposeView` + `MutableStateFlow` |
| B5 | OK | BUILD SUCCESSFUL (`assemblePlaystoreDebug`) |

## Fichiers créés

| Fichier | Description |
|---|---|
| `ui/livetv/compose/LiveTvRecordingApi.kt` | Fonctions suspend API LiveTV extraites des helpers popup |
| `ui/livetv/compose/RecordDialog.kt` | Dialog Compose remplaçant RecordPopup (padding cycling, series options) |
| `ui/livetv/compose/ProgramDetailDialog.kt` | Dialog Compose remplaçant LiveProgramDetailPopup (boutons dynamiques) |
| `ui/livetv/compose/FriendlyDateButton.kt` | Composable date button |
| `ui/livetv/compose/GuidePagingButton.kt` | Composable paging button avec `PagingDirection` enum |

## Fichiers modifiés

| Fichier | Changement |
|---|---|
| `res/layout/live_tv_guide.xml` | Ajout `<ComposeView id="composeDialogs">` |
| `res/layout/overlay_tv_guide.xml` | Ajout `<ComposeView id="composeDialogs">` |
| `ui/livetv/LiveTvGuideFragment.kt` | Remplacement `detailPopup` par `programDetailState: MutableStateFlow<ProgramDetailDialogState>` |
| `ui/livetv/LiveTvGuideFragmentHelper.kt` | Ajout `ProgramDetailDialogState` data class + `addProgramDetailDialog()` extension |
| `ui/playback/CustomPlaybackOverlayFragment.kt` | Remplacement `mDetailPopup` par `programDetailState` + setup ComposeView |
| `ui/playback/CustomPlaybackOverlayFragmentHelper.kt` | Import `asTimerInfoDto` depuis nouveau package |
| `ui/itemdetail/v2/ItemDetailsViewModel.kt` | Import `asTimerInfoDto` depuis nouveau package |
| `ui/ProgramGridCell.kt` | Imports explicites `copyWithTimerId`/`copyWithSeriesTimerId` |

## Fichiers supprimés

| Fichier | Raison |
|---|---|
| `ui/RecordPopup.kt` | Remplacé par `RecordDialog` |
| `ui/RecordPopupHelper.kt` | Logique extraite dans `LiveTvRecordingApi.kt` |
| `ui/LiveProgramDetailPopup.kt` | Remplacé par `ProgramDetailDialog` |
| `ui/LiveProgramDetailPopupHelper.kt` | Logique extraite dans `LiveTvRecordingApi.kt` |
| `res/layout/new_program_record_popup.xml` | Layout plus nécessaire (Compose) |
| `res/layout/program_detail_popup.xml` | Layout plus nécessaire (Compose) |

## Architecture

### Pattern dialog state
```kotlin
data class ProgramDetailDialogState(
    val visible: Boolean = false,
    val program: BaseItemDto? = null,
    val selectedView: ProgramGridCell? = null,
)

// Dans le fragment :
val programDetailState = MutableStateFlow(ProgramDetailDialogState())

// Ouverture :
programDetailState.value = ProgramDetailDialogState(visible = true, program = ..., selectedView = ...)

// Fermeture :
programDetailState.value = programDetailState.value.copy(visible = false)
```

### API centralisée
`LiveTvRecordingApi.kt` contient toutes les fonctions suspend (cancel, record, update timer/series) et les helpers de copie DTO (`copyWithTimerId`, `copyWithSeriesTimerId`, `asTimerInfoDto`, etc.), partagés entre `RecordDialog`, `ProgramDetailDialog`, `ProgramGridCell`, et `CustomPlaybackOverlayFragmentHelper`.

### Dialog imbriqué
`ProgramDetailDialog` contient un `RecordDialog` imbriqué pour les "Series Settings". Quand le RecordDialog est visible, le ProgramDetailDialog se masque (`visible && !showRecordDialog`).
