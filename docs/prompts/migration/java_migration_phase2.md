# Phase 2 — Migration Java → Kotlin (Complexité moyenne)

**Date** : 2026-03-09
**BUILD SUCCESSFUL** ✓

## Résumé

| Fichier | Statut | LOC supprimés | Notes |
|---------|--------|---------------|-------|
| `SubtitleDelayHandler.java` | **Migré → .kt** | 131 | `DelayedCue` → `data class` top-level, `Player.Listener` SAM |
| `ProgramGridCell.java` | **Migré → .kt** | 172 | `RecordingIndicatorView` interface, anon `OnClickListener` → lambda |
| `TvManager.java` | **Migré → object** | 220 | `@JvmStatic` pour interop Java, `@Volatile` sur champs partagés |
| `ItemRowView.java` | **Migré → .kt** | 231 | 2 interfaces → `fun interface`, anon class → lambda |
| `ItemListView.java` | **Migré → .kt** | 125 | `@JvmOverloads` constructor, `@JvmField` pour accès helper |
| `LeanbackOverlayFragment.java` | **Migré → .kt** | 242 | `by inject()` Koin natif, anon `FrameLayout` → `object` expression |

## Détails des migrations

### 2.1 — SubtitleDelayHandler

- `static class DelayedCue` → `private data class DelayedCue` top-level
- `implements Player.Listener` → `: Player.Listener`
- `new Runnable() { ... }` → `object : Runnable { ... }`
- Getter `getOffsetMs()` → propriété `val offsetMs` avec `private set`
- Setter explicite `setOffsetMs(long)` conservé pour la logique de clear
- Caller Java unique : `VideoManager.java` — interop ✓

### 2.2 — ProgramGridCell

- `extends RelativeLayout implements RecordingIndicatorView` → `: RelativeLayout(...), RecordingIndicatorView`
- `new OnClickListener() { ... }` → `setOnClickListener { }`
- `KoinJavaComponent.get<>()` conservé (callers Java encore présents)
- `getProgram()` → propriété `val program` avec `private set`
- `isLast()`/`isFirst()` → propriétés `var isLast`/`var isFirst` avec `private set`
- `setLast()`/`setFirst()` conservés comme méthodes (pas d'argument)
- Import `DateTimeExtensionsKt.getTimeFormatter()` → `Context.getTimeFormatter()` (extension Kotlin)
- Callers Java (`LiveTvGuideFragment`, `CustomPlaybackOverlayFragment`, `LiveProgramDetailPopup`) : interop ✓

### 2.3 — TvManager → object

- `public class TvManager` avec champs `static` → `object TvManager`
- 5 champs static mutables → propriétés `@Volatile`
- Toutes les méthodes publiques annotées `@JvmStatic` pour interop Java
- `Arrays.copyOfRange()` → `Array.copyOfRange()`
- `HashMap<>` Kotlin natif
- `fillChannelIds()` : `indexOfFirst` au lieu de boucle manuelle
- `buildProgramsDict()` : `getOrPut()` idiomatique
- `DateTimeExtensionsKt.getTimeFormatter()` → `Context.getTimeFormatter()`
- Callers Java (`LiveTvGuideFragment`, `PlaybackController`, `CustomPlaybackOverlayFragment`, `LiveProgramDetailPopup`) : interop ✓
- Callers Kotlin (`LiveTvGuideFragmentHelper`, `CustomPlaybackOverlayFragmentHelper`, `LeanbackOverlayFragment`) : accès direct ✓

### 2.4 — ItemRowView

- 2 interfaces internes `RowSelectedListener`/`RowClickedListener` → `fun interface`
- `new OnClickListener() { ... }` → `setOnClickListener { }`
- `getItem()` → propriété `var item` avec `private set`
- `getIndex()` → `fun getIndex()` (pas de propriété pour garder la sémantique)
- `mContext` supprimé (utilise `context` hérité)
- `Integer.toString()` → `.toString()`
- `instanceof` → `is`
- Callers Java (`ItemListFragment`, `MusicFavoritesListFragment`) : interop ✓
- Caller Kotlin (`ItemListViewHelper.kt`) : accès direct ✓

### 2.5 — ItemListView

- `@JvmOverloads constructor` pour les 2 constructeurs View
- `mItemIds` et `mList` conservés avec `@JvmField` (accédés par `ItemListViewHelper.kt`)
- `instanceof` → `is`
- `Math.min/max` → `minOf/maxOf`
- Callers Java (`ItemListFragment`, `MusicFavoritesListFragment`) : interop ✓

### 2.6 — LeanbackOverlayFragment

- `extends Fragment` → `: Fragment()`
- `Lazy<PlaybackControllerContainer> = inject(...)` → `val ... by inject<>()`
- `Lazy<UserPreferences> = inject(...)` → `val ... by inject<>()`
- Anon `FrameLayout` subclass → `object : FrameLayout(requireContext()) { ... }`
- `LifecycleOwnerKt.getLifecycleScope()` → `viewLifecycleOwner.lifecycleScope`
- `setupPlayerOverlayComposeView()` : appel fonction top-level (pas extension)
- `instanceof` chain → `when (action)` exhaustif
- Callers Java (`CustomPlaybackOverlayFragment`) : interop ✓
- Callers Kotlin (`VideoPlayerController.kt`, `VideoPlayerControllerImpl.kt`) : accès direct ✓

## Compteur

| Métrique | Avant (fin phase 1) | Après (fin phase 2) |
|----------|---------------------|---------------------|
| Fichiers Java | 17 | 11 |
| LOC Java supprimés (cumulé) | 302 | 1 423 |
| LOC Java restants | ~8 379 | ~6 820 |
