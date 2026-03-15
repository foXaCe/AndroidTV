# Live TV Phase C — ProgramGridCell & GuideChannelHeader → Compose (AbstractComposeView)

## Objectif
Migrer `ProgramGridCell` et `GuideChannelHeader` vers Compose via `AbstractComposeView`, en gardant la grille View (LinearLayout) existante.

## Strategie
- Composables purement visuels (`ProgramCell`, `ChannelHeader`)
- Wrappers `AbstractComposeView` (`ProgramCellView`, `ChannelHeaderView`) qui gerent le focus View-level et l'interface `RecordingIndicatorView`
- Le focus D-pad reste gere par le systeme View Android (`nextFocusUpId`/`nextFocusDownId` via `TvManager.setFocusParams()`)
- L'interface `LiveTvGuide.setSelectedProgram()` accepte `View` au lieu de `RelativeLayout`

## Fichiers crees

| Fichier | Description |
|---------|-------------|
| `ui/livetv/compose/ProgramCellComposable.kt` | Composable reproduisant ProgramGridCell : nom, indicateurs (New/Premiere/Repeat/HD/Rating), rec timer, couleurs par categorie |
| `ui/livetv/compose/ChannelHeaderComposable.kt` | Composable reproduisant GuideChannelHeader : nom, numero, image async, favori |
| `ui/livetv/compose/ProgramCellView.kt` | `AbstractComposeView` + `RecordingIndicatorView`, focus View-level, `program`/`isFirst`/`isLast` |
| `ui/livetv/compose/ChannelHeaderView.kt` | `AbstractComposeView`, focus View-level, `channel`/`loadImage()`/`refreshFavorite()` |

## Fichiers modifies

| Fichier | Changement |
|---------|------------|
| `ui/base/theme/Spacing.kt` | Ajout constantes Live TV (`programCellHeight`, `channelHeaderWidth`, `timelineHeight`, `guideRowWidthPerMinDp`) |
| `ui/livetv/LiveTvGuide.kt` | `setSelectedProgram(RelativeLayout)` → `setSelectedProgram(View)` |
| `ui/livetv/LiveTvGuideLogic.kt` | `ProgramGridCell` → `ProgramCellView` |
| `ui/livetv/TvManager.kt` | `setFocusParams`/`getOtherCell` : `ProgramGridCell` → `ProgramCellView` |
| `ui/livetv/LiveTvGuideFragment.kt` | `ProgramGridCell`/`GuideChannelHeader` → `ProgramCellView`/`ChannelHeaderView`, type `mSelectedProgramView: View?` |
| `ui/livetv/LiveTvGuideFragmentHelper.kt` | `ProgramDetailDialogState.selectedView: RecordingIndicatorView?`, `toggleFavorite()` via `ChannelHeaderView` |
| `ui/playback/CustomPlaybackOverlayFragment.kt` | Meme migration que LiveTvGuideFragment |
| `ui/playback/CustomPlaybackOverlayFragmentHelper.kt` | `GuideChannelHeader` → `ChannelHeaderView` |
| `ui/livetv/compose/ProgramDetailDialog.kt` | `ProgramGridCell?` → `RecordingIndicatorView?` |
| `ui/GuidePagingButton.kt` | Supprime dependance `ProgramGridCellBinding`, layout inline |

## Fichiers supprimes

| Fichier | LOC supprimes | Raison |
|---------|---------------|--------|
| `ui/ProgramGridCell.kt` | 168 | Remplace par `ProgramCellView` + `ProgramCell` composable |
| `ui/GuideChannelHeader.kt` | 73 | Remplace par `ChannelHeaderView` + `ChannelHeader` composable |
| `res/layout/program_grid_cell.xml` | 54 | Layout plus necessaire (Compose) |
| `res/layout/channel_header.xml` | 49 | Layout plus necessaire (Compose) |

## Architecture

### Focus D-pad
Le focus reste au niveau View Android :
- `ProgramCellView.isFocusable = true`
- `onFocusChanged()` → met a jour l'etat Compose (`_focused`) + appelle `guide.setSelectedProgram(this)`
- `TvManager.setFocusParams()` chain `nextFocusUpId`/`nextFocusDownId` entre les `ProgramCellView` IDs
- Le composable lit `isFocused` pour rendre le fond accent

### Recording indicators
- `ProgramCellView` implemente `RecordingIndicatorView`
- `setRecTimer()`/`setRecSeriesTimer()` mettent a jour `_program` (MutableState) → recomposition automatique
- `ProgramDetailDialog` accepte `RecordingIndicatorView?` au lieu de `ProgramGridCell?`

## Bilan

| Metrique | Valeur |
|----------|--------|
| AbstractComposeView crees | 2 |
| Composables crees | 2 |
| TvSpacing Live TV ajoute | 4 constantes |
| Focus D-pad | Inchange (View-level) |
| LOC supprimes | ~344 (code + XML) |
| BUILD | SUCCESSFUL |
