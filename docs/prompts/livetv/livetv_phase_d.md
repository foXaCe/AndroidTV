# Live TV Phase D — Grille Compose native (LazyColumn + ScrollState partagé)

## Objectif
Remplacer la grille LinearLayout + ObservableScrollView par une grille 100% Compose :
- `LazyColumn` pour la virtualisation verticale des chaînes
- `ScrollState` partagé pour le scroll horizontal synchronisé (timeline + toutes les rangées)
- Focus D-pad natif Compose (`focusable()` + `onFocusChanged`)
- Écran complet `LiveTvGuideScreen` remplaçant `live_tv_guide.xml`
- `LiveTvGuideFragment` réduit à un wrapper ComposeView minimal

## Stratégie

### Scroll synchronisé
Toutes les rangées de programmes et la timeline partagent un seul `rememberScrollState()`. Chaque rangée utilise `horizontalScroll(sharedScrollState)` — quand l'utilisateur scroll une rangée, toutes suivent automatiquement.

### Virtualisation
- **Verticale** : `LazyColumn` ne rend que les chaînes visibles (~8-10 rangées)
- **Horizontale** : `Row` non-lazy par rangée (cellules de largeur variable = durée en minutes × 7dp). La largeur totale est bornée (9h × 60min × 7dp = 3780dp max)

### Focus D-pad
Remplacement de `TvManager.setFocusParams()` (chaînage manuel `nextFocusUpId`/`nextFocusDownId`) par le focus natif Compose :
- `focusable()` sur chaque cellule programme et en-tête chaîne
- `onFocusChanged` pour notifier le ViewModel de la sélection
- `onPreviewKeyEvent` pour gérer Enter (click) et long-press
- `focusGroup()` sur chaque rangée pour grouper le focus horizontal

## Fichiers créés

| Fichier | LOC | Description |
|---------|-----|-------------|
| `ui/livetv/compose/LiveTvGuideScreen.kt` | 364 | Écran complet : header (image programme + infos + boutons), timeline, grille, dialogs (détail programme, options, filtres) |
| `ui/livetv/compose/LiveTvGuideGrid.kt` | 383 | Grille : `LazyColumn` de `GuideRow` (en-tête chaîne fixe + programmes scrollables), `buildProgramCells` (gaps, clipping, placeholders), focus D-pad natif |
| `ui/livetv/compose/GuideTimeline.kt` | 71 | Timeline horizontale synchronisée avec `horizontalScroll(scrollState, enabled = false)`, intervalles 30/60 min avec gestion des minutes partielles |

## Fichiers modifiés

| Fichier | LOC | Changement |
|---------|-----|------------|
| `ui/livetv/LiveTvGuideFragment.kt` | 49 | Réduit de 704 → 49 LOC. Wrapper ComposeView minimal, délègue tout à `LiveTvGuideScreen` |
| `ui/livetv/LiveTvGuideFragmentHelper.kt` | 31 | Réduit de 143 → 31 LOC. Garde uniquement `createNoProgramDataBaseItem()` et `ProgramDetailDialogState` |
| `ui/livetv/LiveTvGuideViewModel.kt` | 209 | Enrichi : `loadGuide()`, `pageGuideTo()`, `toggleFavorite()`, `refreshFavorite()`, `updateProgram()`, `forceReload()` — remplace la logique éparpillée dans l'ancien fragment |

## Fichiers NON modifiés (compatibilité overlay)

| Fichier | Raison |
|---------|--------|
| `ui/livetv/LiveTvGuide.kt` | Interface encore implémentée par `CustomPlaybackOverlayFragment` |
| `ui/livetv/LiveTvGuideLogic.kt` | `buildProgramRow` encore utilisé par overlay |
| `ui/livetv/TvManager.kt` | Singleton encore utilisé par overlay + `PlaybackController` |
| `ui/livetv/compose/ProgramCellView.kt` | `AbstractComposeView` encore utilisé par overlay |
| `ui/livetv/compose/ChannelHeaderView.kt` | `AbstractComposeView` encore utilisé par overlay |
| `ui/playback/CustomPlaybackOverlayFragment.kt` | Guide overlay pas encore migré (D6 reporté) |
| `res/layout/overlay_tv_guide.xml` | Encore utilisé par overlay |

## Architecture

### LiveTvGuideScreen (composable principal)

```
LiveTvGuideScreen(viewModel, onTuneToChannel, onDismiss)
├── GuideHeader
│   ├── AsyncImage (programme sélectionné)
│   ├── Text (nom + overview)
│   └── Row (boutons : reset, date, filtres, options)
├── Row
│   ├── Box (date, 160dp)
│   └── GuideTimeline (synchro scrollState)
├── GuideStatusBar (channels count, filters, hours)
├── LiveTvGuideGrid
│   └── LazyColumn
│       ├── [PageUp button]
│       ├── items(channels) → GuideRow
│       │   ├── GuideChannelHeaderCell (160dp, focusable)
│       │   └── Row(horizontalScroll(sharedState))
│       │       └── forEach(buildProgramCells) → GuideProgramCell (focusable)
│       └── [PageDown button]
├── ProgramDetailDialog (si showProgramDetail)
├── SettingsDialog (options, route LIVETV_GUIDE_OPTIONS)
└── SettingsDialog (filtres, route LIVETV_GUIDE_FILTERS)
```

### buildProgramCells (logique de construction des cellules)

Réplique `LiveTvGuideLogic.buildProgramRow` en Compose :
1. Si aucun programme → placeholders 30 min
2. Pour chaque programme : clip start/end au range guide, insert gaps si nécessaire
3. Remplir le reste si `prevEnd < guideEnd`
4. Largeur cellule = `durationMinutes × guideRowWidthPerMinDp` (7dp/min)

### Focus D-pad natif

```kotlin
Box(
    modifier = Modifier
        .focusable()
        .onFocusChanged { if (it.isFocused) onFocus() }
        .onPreviewKeyEvent { event ->
            // KeyUp Enter → onClick
            // KeyDown long-press Enter → onLongClick
        }
)
```

- Pas de `nextFocusUpId`/`nextFocusDownId` manuels
- Compose gère le focus traversal automatiquement dans le `LazyColumn`
- `focusGroup()` sur chaque `GuideRow` pour navigation horizontale

### ViewModel (LiveTvGuideViewModel)

```kotlin
data class LiveTvGuideUiState(
    val channels: List<BaseItemDto>,
    val filteredChannels: List<BaseItemDto>,
    val programs: Map<UUID, List<BaseItemDto>>,
    val selectedChannel: BaseItemDto?,
    val selectedProgram: BaseItemDto?,
    val guideStart: LocalDateTime,
    val guideEnd: LocalDateTime,
    val guideHours: Int,
    val filters: GuideFilters,
    val isLoading: Boolean,
)
```

- `loadGuide()` : charge toutes les chaînes puis tous les programmes en un appel
- `selectChannel()`/`selectProgram()` : met à jour la sélection + synchronise `TvManager` pour compatibilité overlay
- `toggleFavorite()` : appel API + refresh local
- `forceReload()` : recharge complète
- `pageGuideTo(date)` : change la fenêtre temporelle

## Bugs corrigés pendant le développement

| Bug | Cause | Fix |
|-----|-------|-----|
| `Cannot access 'val Int.dp: Dp': it is private in file` | Extension privée `Int.dp` dans `LiveTvGuideGrid.kt` masquait `androidx.compose.ui.unit.dp` dans tout le package | Supprimé l'extension privée, ajouté `import androidx.compose.ui.unit.dp` |
| `Unresolved reference 'CircularProgressIndicator'` | `androidx.compose.material` pas dans les dépendances | Remplacé par `Text(stringResource(R.string.lbl_loading_elipses))` |

## Bilan

| Métrique | Valeur |
|----------|--------|
| Composables créés | 3 (`LiveTvGuideScreen`, `LiveTvGuideGrid`, `GuideTimeline`) + sous-composables privés |
| LOC créés | 818 (3 fichiers compose) |
| LOC réduits | LiveTvGuideFragment 704 → 49 (-655), FragmentHelper 143 → 31 (-112) |
| LOC ViewModel | 209 (enrichi, était ~80) |
| Focus D-pad | Natif Compose (remplacement `TvManager.setFocusParams()`) |
| Scroll synchronisé | `ScrollState` partagé (remplacement `ObservableScrollView` listeners) |
| D6 overlay | Reporté — `CustomPlaybackOverlayFragment` garde l'ancienne grille View |
| BUILD | **SUCCESSFUL** (github + playstore) |

## TODO restant

- **D6** : Migrer `CustomPlaybackOverlayFragment` pour utiliser `LiveTvGuideGrid` composable au lieu de `overlay_tv_guide.xml` + Views
- **Nettoyage post-D6** : Supprimer `LiveTvGuide.kt` (interface), `LiveTvGuideLogic.kt`, `TvManager.kt`, `ProgramCellView.kt`, `ChannelHeaderView.kt`, `ObservableScrollView.kt`, `ObservableHorizontalScrollView.kt`, `overlay_tv_guide.xml`
- **D7/D8** : Test manuel sur device TV (Ugoos AM9 Pro) — performance, focus D-pad, scroll fluide
