# Phase A — Refactoring état Live TV (sans toucher l'UI)

**Date** : 2026-03-09
**Statut** : BUILD SUCCESSFUL

---

## Fichiers modifiés / créés

| Fichier | Statut | Notes |
|---------|--------|-------|
| `ui/livetv/LiveTvGuideViewModel.kt` | Créé | ViewModel + `LiveTvGuideUiState` data class |
| `ui/livetv/LiveTvGuideLogic.kt` | Créé | `buildProgramRow()` partagé — élimine ~160 LOC dupliqués |
| `ui/livetv/TvManager.kt` | Modifié | +StateFlows (`channelsFlow`, `programsFlow`, `forceReloadFlow`), +`setChannels()` |
| `ui/livetv/TvManagerHelper.kt` | Modifié | +`loadLiveTvChannelsSuspend()`, +`getProgramsSuspend()` |
| `ui/livetv/LiveTvGuideFragment.kt` | Modifié | `getProgramRow` → délègue à `LiveTvGuideLogic` |
| `ui/playback/CustomPlaybackOverlayFragment.kt` | Modifié | `getProgramRow` → délègue à `LiveTvGuideLogic`, nettoyage imports |
| `di/AppModule.kt` | Modifié | +`viewModel { LiveTvGuideViewModel(get(), get()) }` |

---

## Détail des changements

### A1 — LiveTvGuideViewModel

- `LiveTvGuideUiState` : `isLoading`, `channels`, `filteredChannels`, `programs`, `selectedChannel`, `selectedProgram`, `guideStart`, `guideEnd`, `filters`, `error`
- Fonctions : `loadChannels()`, `loadPrograms()`, `selectChannel()`, `selectProgram()`, `setGuideTimeRange()`, `updateFilters()`, `forceReload()`, `clearError()`
- Utilise les nouvelles suspend functions (`loadLiveTvChannelsSuspend`, `getProgramsSuspend`)
- Enregistré dans Koin : `viewModel { LiveTvGuideViewModel(get(), get()) }`

### A2 — TvManager StateFlows

- `channelsFlow: StateFlow<List<BaseItemDto>>` — miroir de `allChannels`
- `programsFlow: StateFlow<Map<UUID, List<BaseItemDto>>>` — miroir de `programsDict`
- `forceReloadFlow: StateFlow<Boolean>` — miroir de `forceReloadFlag`
- `setChannels(channels)` — met à jour `allChannels` + `_channelsFlow` + `channelIds`
- `loadAllChannels()` — reset `forceReloadFlag`/`_forceReloadFlow`, émet dans `_channelsFlow`
- `buildProgramsDict()` — émet dans `_programsFlow`
- `forceReload()` — émet dans `_forceReloadFlow`
- **Non-breaking** : les champs `@Volatile` existants restent en place

### A3 — Logique commune factorisée

- `LiveTvGuideLogic.buildProgramRow()` — construit une `LinearLayout` de `ProgramGridCell` à partir des programmes, plage horaire et dimensions
- `LiveTvGuideLogic.CellIdCounter` — compteur thread-safe pour les IDs de cellules
- Remplace le code dupliqué dans `LiveTvGuideFragment.getProgramRow()` et `CustomPlaybackOverlayFragment.getProgramRow()`

### A4 — Fragments connectés

- Les deux fragments délèguent `getProgramRow` à `LiveTvGuideLogic.buildProgramRow`
- Le ViewModel est enregistré et prêt à être observé (migration graduelle dans les phases suivantes)
- Le flow existant (`TvManager.loadAllChannels` → `displayChannels` → `displayProgramsAsync`) reste intact

---

## Ce qui n'a PAS changé

- Aucun layout XML modifié
- Aucun changement UI visible
- Les fragments continuent d'utiliser `TvManager` directement pour le flow load → display
- `LiveTvGuide` interface inchangée
- `GuideFilters` inchangé
- `ProgramGridCell`, `GuideChannelHeader` inchangés
- Settings screens Live TV inchangés

---

## Build

```
./gradlew :app:compileGithubDebugKotlin
BUILD SUCCESSFUL in 11s
```

Warnings uniquement préexistants (deprecated `EmptyResponse`, `Response`, `showToast`, `requestAudioFocus`).

---

## Test plan

- [ ] Ouvrir guide TV plein écran — chaînes et programmes affichés
- [ ] Filtres fonctionnels (movies, news, sports, etc.)
- [ ] Guide overlay pendant playback — affichage correct
- [ ] Changement de chaîne via guide overlay
- [ ] Date picker — navigation dans le temps
- [ ] Pagination haut/bas des chaînes
- [ ] Focus D-pad inter-rangées fonctionnel
- [ ] Long press → popup détail programme
- [ ] Long press sur chaîne → toggle favori
