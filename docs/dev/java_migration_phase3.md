# Phase 3 — Migration Java → Kotlin (Complexes isolés)

**Date** : 2026-03-09
**BUILD SUCCESSFUL** ✓

## Résumé

| Fichier | Statut | LOC supprimés | Notes |
|---------|--------|---------------|-------|
| `RecordPopup.java` | **Migré → .kt** | 256 | `KoinComponent` + `by inject()`, 4 anon → lambdas, `ContextCompat.getColor` |
| `KeyProcessor.java` | **Migré → .kt** | 367 | Inner class → `handleMenuItemClick()` privée, `KoinComponent`, `when` exhaustif |
| `MusicFavoritesListFragment.java` | **Migré → .kt** | 290 | `by inject()`, 8 anon → lambdas, `AudioEventListener` object expression |
| `Utils.java` (NewPipe) | **Ignoré** | 0 | Shadow class API 23+ — NE PAS TOUCHER (voir ci-dessous) |

## Détails des migrations

### 3.1 — RecordPopup

- `KoinJavaComponent.inject` → `KoinComponent` interface + `by inject()`
- 4 `AdapterView.OnItemSelectedListener` anon classes → `object` expressions (SAM impossible : 2 méthodes)
- 2 `View.OnClickListener` anon classes → lambdas `setOnClickListener { }`
- `RecordPopupHelperKt.xxx(RecordPopup.this, ...)` → appels directs Kotlin `this.xxx(...)`
- `Arrays.asList(...)` → `mutableListOf(...)`
- `ArrayList<Integer>` → `mutableListOf<Int>()`
- `mContext.getResources().getColor(...)` → `ContextCompat.getColor(mContext, ...)`
- Champs `mContext`, `lifecycle` restent publics pour les extensions dans `RecordPopupHelper.kt`
- Caller Java `LiveProgramDetailPopup.java` : interop ✓ (`isShowing` → `isShowing()` getter)
- Nullabilité SDK : `prePaddingSeconds ?: 0`, `recordAnyChannel == true`

### 3.2 — KeyProcessor

- `KoinJavaComponent.inject` → `KoinComponent` interface + `by inject()` (5 dépendances)
- 14 constantes `public static final` → `companion object` avec `const val`
- Inner class `KeyProcessorItemMenuClickListener` → méthode privée `handleMenuItemClick()` avec `when` expression
- `switch/case` imbriqués → `when` Kotlin exhaustifs
- `instanceof` → `is`
- `KeyProcessorHelperKt.playFirstUnwatchedItem(activity, ...)` → `activity.playFirstUnwatchedItem(...)`
- `CoroutineUtils.runOnLifecycle(...)` → `runOnLifecycle(lifecycle) { ... }`
- `Response<>` anonymous class conservée (abstract class, pas SAM-convertible)
- Nullabilité `baseItem: BaseItemDto?` → `?: return false` / safe calls
- Nullabilité `itemId: UUID?` → `?: return false`
- Caller Kotlin `AppModule.kt` : `factory { KeyProcessor() }` — inchangé

### 3.3 — MusicFavoritesListFragment

- `extends Fragment implements View.OnKeyListener` → `: Fragment(), View.OnKeyListener`
- `inject(MediaManager.class)` → `by inject<MediaManager>()` (4 dépendances)
- `RowSelectedListener` anon → lambda SAM `setRowSelectedListener { }`
- `RowClickedListener` anon → lambda SAM `setRowClickedListener { }`
- 2 `View.OnClickListener` anon → lambdas (boutons Play/Shuffle)
- 5 `MenuItem.OnMenuItemClickListener` anon → lambdas chaînées `.setOnMenuItemClickListener { }`
- `AudioEventListener` anonymous class → `object` expression
- `Function1<List<BaseItemDto>, Unit>` → `(List<BaseItemDto>) -> Unit`
- `ItemListFragmentHelperKt.getFavoritePlaylist(this, ...)` → `getFavoritePlaylist(...)` (extension)
- `UUIDSerializerKt.toUUIDOrNull(...)` → `?.toUUIDOrNull()` (extension)
- Nullabilité `ItemRowView.item: BaseItemDto?` → safe calls
- Callers Kotlin (`Destinations.kt`, `ItemListFragmentHelper.kt`) : accès direct ✓

### 3.4 — Utils.java (NewPipe) — IGNORÉ

Ce fichier est un **shadow class** intentionnel qui remplace `org.schabi.newpipe.extractor.utils.Utils`
de la dépendance NewPipe Extractor. Le mécanisme est défini dans `app/build.gradle.kts` :

1. La tâche `stripPipeExtractorUtils` supprime `Utils.class` du JAR NewPipe Extractor
2. Le fichier local fournit une version compatible API 23+ (vs API 33+ dans l'original)
3. Le fichier **DOIT** rester en Java pour que le bytecode `.class` corresponde exactement

**Ne pas migrer, ne pas supprimer.**

### Corrections collatérales

- `ItemListView.kt` : `updatePlaying(id: UUID)` → `updatePlaying(id: UUID?)` (le code Java appelait avec `null`)
- `ItemRowView.kt` : `setPlaying(id: UUID)` → `setPlaying(id: UUID?)` avec vérification `id != null`

## Compteur

| Métrique | Avant (fin phase 2) | Après (fin phase 3) |
|----------|---------------------|---------------------|
| Fichiers Java | 11 | 8 (7 hors shadow NewPipe) |
| LOC Java supprimés (cumulé) | 1 423 | 2 336 |
| LOC Java restants | ~6 820 | ~5 907 |

## Fichiers Java restants

| # | Fichier | LOC | Phase prévue |
|---|---------|-----|-------------|
| 1 | `LiveProgramDetailPopup.java` | 322 | Phase 4 (Live TV) |
| 2 | `LiveTvGuideFragment.java` | 790 | Phase 4 (Live TV) |
| 3 | `ItemLauncher.java` | 308 | Phase 5 (Playback) |
| 4 | `ItemListFragment.java` | 580 | Phase 5 (Playback) |
| 5 | `VideoManager.java` | 810 | Phase 5 (Playback) |
| 6 | `CustomPlaybackOverlayFragment.java` | 1 427 | Phase 5 (Playback) |
| 7 | `PlaybackController.java` | 1 670 | Phase 5 (Playback) |
| — | `Utils.java` (NewPipe shadow) | 438 | Ignoré (shadow class) |
