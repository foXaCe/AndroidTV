# Phase 4 — Migration Java → Kotlin (Live TV cluster)

**Date** : 2026-03-09
**BUILD SUCCESSFUL** ✓

## Résumé

| Fichier | Statut | LOC supprimés | Notes |
|---------|--------|---------------|-------|
| `LiveProgramDetailPopup.java` | **Migré → .kt** | 322 | `KoinComponent` + `by inject()`, 8 anon → lambdas, `AlertDialog.Builder` idiomatique, `ViewTreeLifecycleOwner.set()` → `setViewTreeLifecycleOwner()` |
| `LiveTvGuideFragment.java` | **Migré → .kt** | 790 | `AsyncTask` → coroutines (`lifecycleScope.launch` + `withContext(Dispatchers.Default)`), 13+ anon → lambdas/SAM, `by inject()` (4 dépendances), `Handler(Looper.getMainLooper())` |

## Détails des migrations

### 4.1 — LiveProgramDetailPopup

- `KoinJavaComponent.get(UserRepository.class)` → `KoinComponent` interface + `by inject<UserRepository>()`
- `KoinJavaComponent.get(DataRefreshService.class)` → `by inject<DataRefreshService>()`
- 8 `View.OnClickListener` anon classes → lambdas `setOnClickListener { }`
- 1 `DialogInterface.OnClickListener` anon → lambda `.setPositiveButton(R.string.lbl_yes) { _, _ -> }`
- `AlertDialog.Builder` → builder idiomatique Kotlin avec lambdas
- `ViewTreeLifecycleOwner.set(view, owner)` → `view.setViewTreeLifecycleOwner(owner)` (API AndroidX)
- `new PopupWindow(...)` → `PopupWindow(...).apply { ... }`
- `mProgram.getTimerId()` → null-safe `program.timerId`
- Champs `mContext` et `lifecycle` restent publics pour les extensions dans `LiveProgramDetailPopupHelper.kt`
- Callers :
  - `LiveTvGuideFragment.kt` : accès direct Kotlin ✓
  - `CustomPlaybackOverlayFragment.java` : interop Java ✓ (constructeur, `isShowing()`, `setContent()`, `show()`, `dismiss()`)

### 4.2 — LiveTvGuideFragment

- **AsyncTask → coroutines** :
  - `DisplayProgramsTask extends AsyncTask<Integer, Integer, Void>` → `displayProgramsAsync()` avec `viewLifecycleOwner.lifecycleScope.launch`
  - `onPreExecute()` → code inline avant le `launch`
  - `doInBackground()` → `withContext(Dispatchers.Default) { ... }` avec `ensureActive()` pour la cancellation
  - `runOnUiThread()` dans la boucle → `withContext(Dispatchers.Main) { ... }`
  - `onPostExecute()` → code après le `withContext` block
  - `mDisplayProgramsTask.cancel(true)` → `displayProgramsJob?.cancel()`
- `static import inject()` → `by inject<T>()` (4 dépendances : `CustomMessageRepository`, `PlaybackHelper`, `ImageHelper`, `PlaybackLauncher`)
- `Lazy<T>` wrappers supprimés → `by inject()` natif
- `Handler()` deprecated → `Handler(Looper.getMainLooper())`
- 4 `View.OnClickListener` anon → lambdas
- 2 `ScrollViewListener` anon → SAM lambda (property `scrollViewListener = { ... }`)
- 1 `HorizontalScrollViewListener` anon → SAM lambda
- 1 `View.OnClickListener` (datePickedListener) → val lambda
- 2 `DialogInterface.OnClickListener` → lambdas
- 1 `EmptyResponse` anon class → object expression (conservé car abstract class, pas SAM)
- 1 `Response<>` anon class → object expression (conservé car abstract class)
- `CoroutineUtils.readCustomMessagesOnLifecycle()` → import direct de la top-level function
- `DateTimeExtensionsKt.getTimeFormatter(context)` → `context.getTimeFormatter()` (extension Kotlin)
- `TextUtilsKt.getLoadChannelsLabel(...)` → `getLoadChannelsLabel(...)` (top-level function)
- Null safety : `channelId: UUID?` → `?: return`, `startDate: LocalDateTime?` → safe calls
- `switch/case` → `when` expressions

### Corrections collatérales

- `LiveTvGuideFragmentHelper.kt` : `mSelectedProgram.id` → `mSelectedProgram?.id ?: return@launch` (champ devenu nullable)

## Compteur

| Métrique | Avant (fin phase 3) | Après (fin phase 4) |
|----------|---------------------|---------------------|
| Fichiers Java | 8 (7 hors shadow) | 6 (5 hors shadow) |
| LOC Java supprimés (cumulé) | 2 336 | 3 448 |
| LOC Java restants | ~5 907 | ~4 795 |
| AsyncTask restants | 1 | 1 (CustomPlaybackOverlayFragment) |

## Fichiers Java restants

| # | Fichier | LOC | Phase prévue |
|---|---------|-----|-------------|
| 1 | `ItemLauncher.java` | 308 | Phase 5 (Playback) |
| 2 | `ItemListFragment.java` | 580 | Phase 5 (Playback) |
| 3 | `VideoManager.java` | 810 | Phase 5 (Playback) |
| 4 | `CustomPlaybackOverlayFragment.java` | 1 427 | Phase 5 (Playback) |
| 5 | `PlaybackController.java` | 1 670 | Phase 5 (Playback) |
| — | `Utils.java` (NewPipe shadow) | 438 | Ignoré (shadow class) |
