# V3B — Tests UI Compose (instrumentés)

**Date** : 2026-03-08
**Statut** : ✅ Terminé — 11 tests, 0 failures
**Framework** : Compose UI Test (junit4) + AndroidJUnit4 Runner
**Device** : Ugoos AM9 PRO — Android 14 (API 34), locale fr-FR

---

## Dépendances ajoutées

| Dépendance | Version | Usage |
|------------|---------|-------|
| `androidx.compose.ui:ui-test-junit4` | 1.10.2 | `createComposeRule`, assertions sémantiques |
| `androidx.compose.ui:ui-test-manifest` | 1.10.2 | `debugImplementation` — activité hôte pour tests |

Ajout de `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"` dans `defaultConfig`.

---

## Configuration spéciale : ProGuard debug

Le build debug a `isMinifyEnabled = true` + `isShrinkResources = true`, ce qui est inhabituel.
R8 élimine les classes nécessaires aux tests instrumentés. Solution : fichier `app/proguard-debug.pro` ajouté uniquement au build type debug.

**Fichier** : `app/proguard-debug.pro`

```
-dontoptimize
-keep class org.jellyfin.androidtv.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.compose.** { *; }
-keep class **$DefaultImpls { *; }
```

**Fichier** : `app/src/androidTest/AndroidManifest.xml` — désactive `InitializationProvider` pour éviter le crash au démarrage des tests.

---

## Fichiers de test créés

### 1. `app/src/androidTest/kotlin/ui/base/state/StateContainerTest.kt` — 5 tests

Tests des composables `StateContainer`, `ErrorState`, et `EmptyState` :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `loading_state_shows_skeleton_not_content` | DisplayState.LOADING → skeleton visible, contenu masqué |
| 2 | `content_state_shows_content_not_skeleton` | DisplayState.CONTENT → contenu visible, skeleton masqué |
| 3 | `error_state_shows_error_with_retry_button` | ErrorState → message d'erreur + bouton Retry visibles |
| 4 | `retry_button_click_calls_callback` | Clic sur Retry → callback `onRetry` appelé |
| 5 | `empty_state_shows_title_and_message` | EmptyState → titre + message visibles |

### 2. `app/src/androidTest/kotlin/ui/player/PlayerOverlayTest.kt` — 4 tests

Tests du `PlayerOverlayLayout` et du pattern seekbar :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `overlay_hidden_by_default` | Les contrôles sont masqués au démarrage |
| 2 | `dpad_action_shows_overlay` | D-pad UP → contrôles apparaissent |
| 3 | `overlay_autohides_after_3_seconds` | Auto-hide après timeout 3s |
| 4 | `seekbar_height_increases_on_focus` | Hauteur seekbar 3dp → 8dp au focus |

### 3. `app/src/androidTest/kotlin/ui/base/state/DpadFocusTest.kt` — 2 tests

Tests de navigation D-pad sur les composants d'erreur :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `error_state_retry_button_is_focusable` | Bouton Retry est interactif (OnClick) |
| 2 | `error_banner_retry_responds_to_dpad_center` | ErrorBanner Retry → callback déclenché |

---

## Résultat : `./gradlew :app:connectedGithubDebugAndroidTest`

```
Finished 11 tests on AM9 PRO - 14

StateContainerTest.loading_state_shows_skeleton_not_content  PASSED
StateContainerTest.content_state_shows_content_not_skeleton  PASSED
StateContainerTest.error_state_shows_error_with_retry_button PASSED
StateContainerTest.empty_state_shows_title_and_message       PASSED
StateContainerTest.retry_button_click_calls_callback         PASSED
PlayerOverlayTest.overlay_hidden_by_default                  PASSED
PlayerOverlayTest.dpad_action_shows_overlay                  PASSED
PlayerOverlayTest.overlay_autohides_after_3_seconds          PASSED
PlayerOverlayTest.seekbar_height_increases_on_focus          PASSED
DpadFocusTest.error_state_retry_button_is_focusable          PASSED
DpadFocusTest.error_banner_retry_responds_to_dpad_center     PASSED

BUILD SUCCESSFUL in 2m 7s
```

---

## Problèmes rencontrés et solutions

### 1. R8 élimine les classes nécessaires aux tests

**Symptôme** : Crash `NoClassDefFoundError` sur `MainDispatcherFactory$DefaultImpls`, `LazyKt`, `DelayWithTimeoutDiagnostics`, `InfiniteAnimationPolicy$DefaultImpls`, `ViewRootForTest`, `ErrorBannerKt`.

**Cause** : Le build debug a R8 activé (`isMinifyEnabled = true`). Les tests instrumentés référencent les classes par leur nom original, que R8 élimine ou renomme.

**Solution** : Fichier `proguard-debug.pro` séparé avec des keep rules larges, ajouté uniquement au build type debug. Le build release reste inchangé.

### 2. Touch injection échoue sur Android TV

**Symptôme** : `Failed to inject touch input` avec `performClick()`.

**Cause** : Le device TV (Ugoos AM9 PRO) ne supporte pas l'injection d'événements tactiles.

**Solution** : Utiliser `performSemanticsAction(SemanticsActions.OnClick)` au lieu de `performClick()`. Cette approche passe par l'arbre sémantique plutôt que par l'injection tactile.

### 3. `RequestFocus` action non disponible

**Symptôme** : `Failed to perform RequestFocus action` sur certains nœuds.

**Cause** : L'action sémantique `RequestFocus` n'est pas exposée par tous les composants.

**Solution** : Utiliser `FocusRequester` + `runOnIdle { focusRequester.requestFocus() }` pour un contrôle de focus fiable dans les tests.

### 4. Locale du device → texte "Retry" introuvable

**Symptôme** : `assertExists` échoue pour le texte "Retry" dans ErrorState et ErrorBanner.

**Cause** : Le device est en locale `fr-FR`. `stringResource(R.string.lbl_retry)` retourne "Réessayer", pas "Retry".

**Solution** : Résoudre le label localisé via `InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.lbl_retry)` au lieu d'un texte hardcodé.

### 5. Auto-hide overlay timing

**Symptôme** : Test instable — le overlay ne disparaît pas dans le timeout attendu.

**Cause** : Le timeout réel est 3s (coroutine delay) + durée de l'animation de sortie.

**Solution** : Utiliser `waitUntil(timeoutMillis = 6000)` pour laisser suffisamment de temps au delay + animation.
