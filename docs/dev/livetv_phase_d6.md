# Live TV Phase D6 — Guide overlay Compose + nettoyage final

## D6 — Guide overlay → LiveTvGuideScreen (Compose)

### Objectif
Remplacer le guide overlay View (`overlay_tv_guide.xml` + `OverlayTvGuideBinding`) dans `CustomPlaybackOverlayFragment` par le composable `LiveTvGuideScreen` déjà utilisé par `LiveTvGuideFragment`.

### Changements

#### CustomPlaybackOverlayFragment.kt
- **Supprimé** `implements LiveTvGuide` et `View.OnKeyListener`
- **Supprimé** `OverlayTvGuideBinding` → remplacé par un `ComposeView` programmatique
- **Ajouté** `LiveTvGuideViewModel` via Koin `by viewModel()`
- **Supprimé** tous les champs guide View : `mSelectedProgram`, `mSelectedProgramView`, `mCurrentGuideStart/End`, `mAllChannels`, `mFirstFocusChannelId`, `programDetailState`, `cellIdCounter`, `displayProgramsJob`
- **Supprimé** toutes les méthodes guide View : `loadGuide()`, `displayChannels()`, `launchDisplayPrograms()`, `getProgramRow()`, `getChannelHeader()`, `fillTimeLine()`, `setSelectedProgram()`, `showProgramOptions()`, `dismissProgramOptions()`, `refreshFavorite()`, `getCurrentLocalStartDate()`, `getCurrentLocalEndDate()`, `detailUpdateInternal()`, `detailUpdateTask`, `onKey()`
- **Simplifié** `showGuide()` : montre le ComposeView + appelle `guideViewModel.loadGuide()`
- **Simplifié** `hideGuide()` : cache le ComposeView + remet vidéo plein écran
- **Simplifié** `keyListener` section guide : MEDIA_PLAY lit le programme sélectionné depuis le ViewModel ; D-pad passe au Compose (`return false`)
- **Supprimé** `readCustomMessagesOnLifecycle` et injection `customMessageRepository` (géré par le Compose screen)
- **Supprimé** `tvGuideBinding.guideCurrentTitle` dans `updateDisplay()` (géré par le Compose screen)

#### CustomPlaybackOverlayFragmentHelper.kt
- **Supprimé** `toggleFavorite()` (géré par ViewModel dans LiveTvGuideScreen)
- **Supprimé** `refreshSelectedProgram()` (géré par ViewModel)
- **Nettoyé** imports inutilisés

## D6b — Nettoyage final

### Fichiers supprimés

| Fichier | LOC | Raison |
|---------|-----|--------|
| `ui/livetv/LiveTvGuide.kt` | 13 | Interface — plus aucun implémenteur |
| `ui/livetv/LiveTvGuideLogic.kt` | 134 | `buildProgramRow` — remplacé par `buildProgramCells` Compose |
| `ui/livetv/compose/ProgramCellView.kt` | 73 | `AbstractComposeView` — remplacé par `GuideProgramCell` composable |
| `ui/livetv/compose/ChannelHeaderView.kt` | 70 | `AbstractComposeView` — remplacé par `GuideChannelHeaderCell` composable |
| `ui/ObservableScrollView.kt` | 19 | Plus aucun utilisateur |
| `ui/ObservableHorizontalScrollView.kt` | 19 | Plus aucun utilisateur |
| `ui/ScrollViewListener.kt` | ~10 | Plus aucun utilisateur |
| `ui/HorizontalScrollViewListener.kt` | ~10 | Plus aucun utilisateur |
| `ui/GuidePagingButton.kt` | 58 | View — remplacé par composable `compose/GuidePagingButton.kt` |
| `res/layout/overlay_tv_guide.xml` | 215 | Remplacé par ComposeView |
| `res/layout/live_tv_guide.xml` | 265 | Non référencé (LiveTvGuideFragment utilise ComposeView directement) |

### TvManager.kt nettoyé
- **Supprimé** `setFocusParams()`, `getOtherCell()` — référençaient `ProgramCellView`
- **Supprimé** `setTimelineRow()` — construisait la timeline View
- **Supprimé** `getProgramsAsync()`, `buildProgramsDict()` — logique programme View
- **Supprimé** `getProgramsForChannel()` (2 overloads) — plus d'appelant
- **Supprimé** champs : `programsDict`, `needLoadTime`, `_programsFlow`
- **Conservé** : `getLastLiveTvChannel`, `setLastLiveTvChannel`, `getPrevLiveTvChannel`, `getAllChannels`, `setChannels`, `loadAllChannels`, `forceReload`, `shouldForceReload`, `getAllChannelsIndex`, `getChannel`, `updateLastPlayedDate`, `channelsFlow`, `forceReloadFlow`

### LiveTvGuideFragmentHelper.kt nettoyé
- **Supprimé** `ProgramDetailDialogState` data class (dead code)
- **Conservé** `createNoProgramDataBaseItem()` (utilisé par `buildProgramCells` Compose)

## Bilan Live TV COMPLET (A+B+C+D+D6)

| Métrique | Valeur |
|----------|--------|
| Custom Views supprimées | 9 (ProgramCellView, ChannelHeaderView, ObservableScrollView ×2, GuidePagingButton, ScrollViewListener ×2, LiveTvGuideLogic, LiveTvGuide interface) |
| PopupWindows | 0 |
| ObservableScrollView | 0 |
| LiveTvGuide interface | Supprimée |
| LiveTvGuideLogic | Supprimé |
| Grille plein écran | 100% Compose (LiveTvGuideScreen → LiveTvGuideGrid) |
| Guide overlay player | 100% Compose (LiveTvGuideScreen réutilisé) |
| Guide factorisé | 1 screen composable / 2 usages (fragment + overlay) |
| Layouts XML supprimés | 2 (live_tv_guide.xml, overlay_tv_guide.xml) |
| TvManager | Nettoyé (conserve uniquement channel/last played) |
| BUILD SUCCESSFUL | github + playstore |
