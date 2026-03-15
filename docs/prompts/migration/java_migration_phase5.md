# Phase 5 — Migration Java → Kotlin (Playback cluster)

**Date** : 2026-03-09
**BUILD SUCCESSFUL** ✓

## Résumé

| Fichier | Statut | LOC Java supprimés | Notes |
|---------|--------|-------------------|-------|
| `ItemLauncher.java` | **Migré → .kt** | 308 | `KoinComponent` + `by inject()`, 5 `Response<>` → object expressions, `switch/case` → `when`, extension functions (`copyWithServerId`, `copyWithDisplayPreferencesId`) |
| `ItemListFragment.java` | **Migré → .kt** | 580 | `by inject()` (7 dépendances), `AudioEventListener` → object expression, `MenuItem.OnMenuItemClickListener` → lambdas, `Handler(Looper.getMainLooper())` |
| `VideoManager.java` | **Migré → .kt** | 810 | `KoinComponent` + `get()`, `DefaultRenderersFactory` anon subclass → object, 2 `Player.Listener` → object expressions, `AnalyticsListener` → object expression, `mExoPlayer` → nullable `ExoPlayer?` |
| `CustomPlaybackOverlayFragment.java` | **Migré → .kt** | 1 427 | `AsyncTask` → coroutines (`lifecycleScope.launch` + `withContext(Dispatchers.Default)` + `ensureActive()`), `by inject()` (9 dépendances), `EmptyResponse` → object expressions, `Animation.AnimationListener` → object expressions, `ScrollViewListener`/`HorizontalScrollViewListener` → SAM lambdas |
| `PlaybackController.java` | **Migré → .kt** | 1 670 | `KoinJavaComponent.inject()` → delegated properties, `Response<>`/`EmptyResponse` → object expressions, `PlaybackState` enum preserved, `SyncPlayPlaybackCallback` → object expression, nullable safety exhaustive |

## Fichiers collatéraux modifiés

- `PlaybackControllerHelper.kt` — Ajusté pour nullabilité de `mExoPlayer`, `fragment`, `mVideoManager`, `mCurrentOptions`, `currentMediaSource`
- `LeanbackOverlayFragment.kt` — Null-safe `switchChannel(UUID)` avec `?.let`
- `VideoSpeedController.kt` — `setPlaybackSpeed()` → property syntax `playbackSpeed =`
- `CustomPlaybackOverlayFragmentHelper.kt` — Null guards pour `mSelectedProgram` et `binding`
- Tests `VideoSpeedControllerTests.kt` — Mockk patterns mis à jour

## Détails des migrations

### 5.1 — ItemLauncher
- `KoinJavaComponent.<T>inject(T.class)` → `KoinComponent` interface + `by inject<T>()`
- 5 `Response<>` anonymous callbacks → `object : Response<T>() { override fun onResponse(...) }` (abstract class, pas SAM)
- `switch/case` → `when` expressions (exhaustives avec `else`)
- `JavaCompat.copyWithServerId(item, id)` → `item.copyWithServerId(id)` (extension function directe)
- `Destinations.INSTANCE.xxx()` → `Destinations.xxx()`
- `BaseRowItem.SelectAction` → `BaseRowItemSelectAction`
- Null safety : `itemId: UUID?` → `?: return`, `currentAudioItem?.id`

### 5.2 — ItemListFragment
- `static import inject()` → `by inject<T>()` (7 dépendances)
- `AudioEventListener` → `object : AudioEventListener { ... }` (interface avec defaults, pas SAM)
- 7 `MenuItem.OnMenuItemClickListener` anon → lambdas `.setOnMenuItemClickListener { ... }`
- `TextUtilsKt.stripHtml(s)` → `s.stripHtml()` (extension function)
- `BaseItemExtensionsKt.canPlay(item)` → `item.canPlay()` (extension function)
- `KoinJavaComponent.get(PlaybackLauncher.class)` → `val playbackLauncher: PlaybackLauncher by inject()`
- `mediaManager.isPlayingAudio()` → `mediaManager.isPlayingAudio` (propriété Kotlin)
- `row.getIndex()` conservé comme appel de fonction

### 5.3 — VideoManager
- `KoinJavaComponent.get(X.class)` → `KoinComponent` + `get<X>()`
- `mExoPlayer` → `var mExoPlayer: ExoPlayer?` (nullable, private set)
- `DefaultRenderersFactory` anon subclass → `object : DefaultRenderersFactory(context) { ... }`
- 2 `Player.Listener` → object expressions
- 1 `AnalyticsListener` → object expression
- Propriétés : `getZoomMode()` → `var zoomMode`, `getPlaybackSpeed()/setPlaybackSpeed()` → `var playbackSpeed`
- `Long.intValue()` → `.toInt()` pour les préférences `longPreference`
- `streamInfo.mediaSource?.mediaStreams.orEmpty()` pour null safety

### 5.4 — CustomPlaybackOverlayFragment
- **AsyncTask → coroutines** :
  - `DisplayProgramsTask extends AsyncTask<Integer, Integer, Void>` → `displayProgramsAsync()` avec `lifecycleScope.launch`
  - `onPreExecute()` → code inline avant le `launch`
  - `doInBackground()` → `withContext(Dispatchers.Default) { ensureActive() ... }`
  - `runOnUiThread()` → `withContext(Dispatchers.Main) { ... }`
  - `onPostExecute()` → code après le `withContext` block
  - `mDisplayProgramsTask.cancel(true)` → `displayProgramsJob?.cancel()`
- `by inject()` (9 dépendances)
- `EmptyResponse` → object expressions (abstract class)
- `Animation.AnimationListener` (3 méthodes) → object expressions
- `ScrollViewListener` / `HorizontalScrollViewListener` → SAM lambdas (fun interfaces)
- `binding` → nullable après `onDestroyView`
- `mSelectedProgram` et `mSelectedProgramView` → `internal` pour les extension functions

### 5.5 — PlaybackController
- Le plus critique : 24+ callers
- `KoinJavaComponent.inject()` → Koin delegated properties
- 10 `Response<>`/`EmptyResponse` callbacks → object expressions
- `implements PlaybackControllerNotifiable` → `: PlaybackControllerNotifiable`
- `PlaybackState` enum préservé comme inner enum class
- `SyncPlayPlaybackCallback` → object expression avec `this@PlaybackController`
- Null safety exhaustive sur tous les champs mutables
- Propriétés : `isPlaying`, `isPaused`, `isLiveTv`, `currentlyPlayingItem`, etc.

## Compteur final

| Métrique | Avant (fin phase 4) | Après (fin phase 5) |
|----------|---------------------|---------------------|
| Fichiers Java | 6 (5 hors shadow) | **1 (shadow uniquement)** |
| LOC Java supprimés (cumulé) | 3 448 | **8 243** |
| LOC Java restants | ~4 795 | **438 (shadow)** |
| AsyncTask restants | 1 | **0** |
| KoinJavaComponent restants | 0 | **0** |
| Anonymous classes | ~30 | **0** |

## Bilan final Java → Kotlin

- **Fichiers Java** : 21 → **1** (NewPipe shadow uniquement) ✓
- **LOC Java supprimés** : ~8 243 / 8 681 total ✓
- **AsyncTask** : 0 ✓
- **KoinJavaComponent** : 0 ✓
- **Anonymous classes** : 0 ✓
- **`find app/src/main/java -name "*.java"` → 1 seul résultat** ✓
- **BUILD SUCCESSFUL** ✓
- Test AM9 Pro playback complet : **EN ATTENTE** (à confirmer par l'utilisateur)
