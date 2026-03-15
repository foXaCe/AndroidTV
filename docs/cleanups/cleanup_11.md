# Cleanup 11 — Audit qualité + nettoyage Timber + audit coroutines

**Date** : 2026-03-13
**Scope** : Audit TODO/FIXME, nettoyage logs Timber, audit hardcoded strings, audit deprecated API, audit/fix coroutines
**Basé sur** : cleanup_10.md (magic numbers centralisés)

---

## ÉTAPE 1 — TODO / FIXME / HACK (audit)

15 occurrences trouvées.

### CRITIQUE (1)

| Fichier | Ligne | Contenu | Risque |
|---------|-------|---------|--------|
| `preference/store/DisplayPreferencesStore.kt` | 139 | `TODO("The DisplayPreferencesStore does not support migrations")` | **kotlin.TODO() throw NotImplementedError au runtime** — crash si la migration est appelée |

### DETTE (10)

| Fichier | Ligne | Contenu | Priorité |
|---------|-------|---------|----------|
| `ui/navigation/Destinations.kt` | 45,60,95,139,164,178 | `// TODO only pass item id instead of complete JSON` (×6) | Basse — fonctionne, refactoring perf |
| `ui/playback/PlaybackController.kt` | 1518 | `// TODO, implement speed change handling` | Moyenne — feature incomplète |
| `ui/playback/PlaybackControllerHelper.kt` | 230 | `// TODO: ask-to-skip UI removed during Fragment migration` | Moyenne — feature manquante (skip intro) |
| `ui/home/compose/HomeHeroBackdrop.kt` | 364 | `onClick = { /* TODO: add to list */ }` | Basse — bouton non fonctionnel |
| `preference/PreferencesRepository.kt` | 29 | `// FIXME: Make suspended when usages are converted to Kotlin` | Basse — tout est Kotlin maintenant |
| `util/apiclient/JellyfinImage.kt` | 250 | `// TODO Add SeriesTimerInfoDto once API types are fixed` | Basse — dépend du SDK |

### INFO (3) — Légitimes, pas de dette

| Fichier | Ligne | Contenu |
|---------|-------|---------|
| `ui/base/list/ListMessage.kt` | 19 | `// TODO: Add suitable space token for this padding` |
| `ui/browsing/composable/inforow/BaseItemInfoRow.kt` | 386 | `// TODO: Appears to always be null? Maybe an API issue?` |
| `ui/base/list/ListItemContent.kt` | 34 | `// TODO: Add suitable space token for this padding` |

---

## ÉTAPE 2 — Logs Timber (nettoyage)

### Compteurs avant/après

| Niveau | Avant | Après | Supprimés | Conservés |
|--------|-------|-------|-----------|-----------|
| Timber.v | 3 | **0** | -3 (100%) | 0 |
| Timber.d | 340 | **41** | **-299** (88%) | 41 |
| Timber.i | 115 | 115 | 0 | 115 |
| Timber.w | 164 | 164 | 0 | 164 |
| Timber.e | 274 | 274 | 0 | 274 |
| **Total** | **928** | **626** | **-302** | **626** |

### Timber.d conservés (41) — justification

| Fichier | Nb | Raison |
|---------|-----|--------|
| PlaybackController.kt | 7 | Display mode matching, seek rebuild, server-specific API |
| SyncPlayManager.kt | 5 | Drift correction (SkipToSync/SpeedToSync), duplicate command |
| AuthenticationRepository.kt | 4 | Jellyseerr auto-login flow (auth diagnostique) |
| VideoManager.kt | 4 | Seekability, speed/subtitle/audio delay settings |
| PluginSyncService.kt | 3 | Sync disabled, merge directions |
| UpdateCheckerService.kt | 2 | Version checking |
| ThemeMusicPlayer.kt | 2 | Config check, item start |
| JellyseerrDetailsViewModel.kt | 2 | Request/cancel actions |
| JellyseerrDiscoverViewModel.kt | 2 | NSFW content filter |
| MainActivity.kt | 2 | Session preserve, theme apply |
| Autres (8 fichiers × 1) | 8 | Divers diagnostiques importants |

### Fichiers nettoyés — top suppressions

| Fichier | Timber.d avant | Supprimés |
|---------|---------------|-----------|
| JellyseerrHttpClient.kt | 69 | 69 |
| MultiServerRepository.kt | 21 | 21 |
| JellyseerrDiscoverViewModel.kt | 17 | 15 |
| PluginSyncService.kt | 17 | 14 |
| ThemeMusicPlayer.kt | 14 | 12 |
| VideoManager.kt | 14 | 10 |
| ParentalControlsRepository.kt | 13 | 13 |
| PlaybackController.kt | 13 | 6 |
| JellyseerrDetailsViewModel.kt | 11 | 9 |
| TmdbRepository.kt | 9 | 9 |

### Imports nettoyés

- `timber.log.Timber` supprimé de : KeyProcessor.kt, ServerDiscoveryScreen.kt, PrePlaybackTrackSelector.kt, PlaybackManager.kt, JellyseerrAuthViewModel.kt
- Import `onStart`/`onEach` supprimé de ServerDiscoveryRepository.kt (rendu inutile)

---

## ÉTAPE 3 — Hardcoded strings (audit)

22+ strings UI visibles hardcodées identifiées, candidates à externalisation dans `strings.xml` :

### Filtres Live TV — GuideFilters.kt

| Ligne | String | Type |
|-------|--------|------|
| 118 | `"Content filtered. Showing channels with ${getFilterString()}"` | Message filtre |
| 120 | `"Showing all programs "` | Message filtre |
| 125-130 | `"movies"`, `"news"`, `"sports"`, `"series"`, `"kids"`, `"ONLY new"` | Labels filtres |

### Labels fallback — VideoPlayerControls.kt

| Ligne | String | Type |
|-------|--------|------|
| 393 | `"Off"` | Option désactivation sous-titres |
| 400, 426 | `"Track ${track.index + 1}"` | Nom fallback piste audio/sous-titre |
| 447 | `"Chapter ${i + 1}"` | Nom fallback chapitre |

### Codecs/formats — VideoPlayerHeader.kt

| Lignes | Strings | Type |
|--------|---------|------|
| 282-332 | HEVC, AV1, H.264, VP9, AC3, EAC3, AAC, FLAC, DTS, TrueHD, MKV, MP4, AVI, TS, DV, HDR10+, HDR10, HDR, Stereo | Badges codecs |

### Branding — VegafoXTitleText.kt, QuickConnectScreen.kt, ServerDiscoveryScreen.kt

| Lignes | Strings | Type |
|--------|---------|------|
| 30, 33 | `"Vega"`, `"foX"` | Titre app (×3 fichiers) |

### Divers

| Fichier | String | Type |
|---------|--------|------|
| ByLetterBrowseScreen.kt | `"#"` | Titre section numérique |
| SettingsScreensaverDimmingScreen.kt | `"Off"`, `"5%"`…`"100%"` | Labels pourcentage |
| PrePlaybackTrackSelector.kt | `"Track ${stream.index ?: 0}"` | Nom fallback piste |

**Action recommandée** : Externaliser les filtres Live TV et labels fallback en priorité. Les codecs et branding sont acceptables hardcodés (invariants linguistiques).

---

## ÉTAPE 4 — Deprecated API (audit)

### @Deprecated dans le code projet (18)

| Fichier | Nb | Raison | Effort |
|---------|-----|--------|--------|
| `UserSettingPreferences.kt` | 12 | Migration homeSectionsJson (préférences legacy) | S — migration auto déjà implémentée |
| `Utils.kt` | 2 | `convertDpToPixel`/`getMaxBitrate` — wrappers legacy | S — remplacer les appelants |
| `Response.kt` | 2 | Classe callback Java — marquée deprecated | M — supprimer quand plus d'appelants Java |
| `routes.kt` | 1 | Route setting renommée | S — alias de compat |
| `PremiumSideBar.kt` | 1 | Constante renommée | S — alias de compat |

### API Android dépréciées

| Pattern | Occurrences | Alternative moderne | Effort |
|---------|------------|-------------------|--------|
| `resources.getColor(R.color.xxx, null)` | 2 (CreatePlaylistDialogFragment) | `ContextCompat.getColor()` | S |
| `ContextCompat.getColor()` | 5 | ✅ Déjà moderne | — |
| `ContextCompat.getDrawable()` | 15+ | ✅ Déjà moderne | — |
| `Handler(Looper.getMainLooper())` | 11 | LifecycleScope / coroutines | L — refactoring profond |
| `ResourcesCompat.getDrawable()` | 1 | ✅ Acceptable | — |

### Résumé

- **Pas d'AsyncTask, pas de View.SYSTEM_UI_FLAG** — bonne modernisation déjà faite
- **Handler(Looper.getMainLooper())** : 11 usages — pattern legacy mais pas deprecated au sens strict. Migration vers coroutines = effort L
- **resources.getColor sans ContextCompat** : 2 occurrences mineures dans CreatePlaylistDialogFragment

---

## ÉTAPE 5 — Coroutines / Flows non annulés

### Patterns trouvés et classifiés

#### CRITIQUE — Scope jamais annulé, fuite mémoire potentielle (10 sites)

| Fichier | Ligne | Pattern | Risque |
|---------|-------|---------|--------|
| ~~LeftSidebarNavigation.kt~~ | ~~459, 656~~ | ~~`CoroutineScope(Main).launch`~~ | **CORRIGÉ** → `scope.launch` |
| ShuffleUtils.kt | 22, 40 | `CoroutineScope(Main).launch` | Legacy Java compat — fire-and-forget |
| ShuffleDialogLauncher.kt | 81 | `CoroutineScope(Main).launch` | Intentionnel (survie dialog dismiss) |
| CreatePlaylistDialogFragment.kt | 174 | `CoroutineScope(Main).launch` | Dialog peut se fermer avant fin |
| CreatePlaylistDialog.kt | 275 | `CoroutineScope(Main).launch` | Idem version Compose |
| UpdateDialogs.kt | 297, 343 | `CoroutineScope(Main).launch` | Download en arrière-plan sans lifecycle |
| SyncPlayManager.kt | 47 | `SupervisorJob + Main` | Scope longue durée, multiple jobs |
| TimeSyncManager.kt | 22 | `SupervisorJob + IO` | Boucle infinie, annulation par flag |
| ParentalControlsRepository.kt | 95 | `SupervisorJob + Main` | Aucun cleanup, init block |
| ThemeMusicPlayer.kt | 33 | `CoroutineScope(Main)` | Dépend de stop() manuel |
| PlaybackLauncher.kt | 30 | `SupervisorJob + Main` | Singleton, jamais annulé |

#### RISQUE — Pattern dangereux mais acceptable (3 sites)

| Fichier | Ligne | Pattern | Raison OK |
|---------|-------|---------|-----------|
| MediaContentProvider.kt | 65 | `SupervisorJob + IO` | ContentProvider = app-scoped |
| HomePrefetchService.kt | 42 | `SupervisorJob + IO` | Explicit cancel in consume() |
| SeekProvider.kt | 60 | `SupervisorJob + IO + handler` | Cancelled in reset() |

#### OK — Bien gérés (3 sites)

| Fichier | Ligne | Pattern | Raison |
|---------|-------|---------|--------|
| ClockUserView.kt | 34 | `SupervisorJob + Main` | Cancelled in onDetachedFromWindow() |
| UserViewsRepository.kt | 32 | `Dispatchers.IO` | Internal via shareIn() |
| AuthenticationRepository.kt | 245 | `Dispatchers.IO` | Fire-and-forget, complète vite |

### Corrections appliquées

1. **LeftSidebarNavigation.kt:459** — `CoroutineScope(Main).launch` → `scope.launch` (rememberCoroutineScope déjà disponible)
2. **LeftSidebarNavigation.kt:656** — `CoroutineScope(Main).launch` → `scope.launch` (idem)

### Risques résiduels (non corrigés — effort M/L)

- **ShuffleUtils.kt** : Fonctions deprecated, devraient être supprimées quand tous les appelants migrent
- **UpdateDialogs.kt** : Nécessite passer un CoroutineScope en paramètre (refactoring M)
- **CreatePlaylistDialog/Fragment** : Nécessite viewModelScope ou lifecycle-aware scope
- **SyncPlayManager/TimeSyncManager** : Singletons Koin à durée de vie app — acceptable mais pas idéal
- **0 GlobalScope** trouvé — bon signe

---

## LOC ce round

| Action | LOC |
|--------|-----|
| Timber.v supprimés (3 appels, 2 fichiers) | -3 |
| Timber.d supprimés (299 appels, ~50 fichiers) | -320 |
| Imports inutilisés nettoyés (6 fichiers) | -7 |
| Coroutine fixes (LeftSidebarNavigation) | 0 (remplacement) |
| **Net** | **~-330** |

## LOC total tous cleanups

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3,124 |
| 05 | ~1,581 + 6,751 traductions |
| 06 | ~369 |
| 07 | ~361 + 900 KB images |
| 08 | ~95 |
| 09 | ~110 + 47 traductions |
| 10 | +140 (centralisation) |
| **11** | **~330** |
| **Total net** | **~6,405 LOC + 6,798 traductions + 900 KB images** |

---

## Build

- Debug (github) : BUILD SUCCESSFUL
- Release (github) : BUILD SUCCESSFUL
- Installé sur AM9 Pro : Success
