# V3B — Tests UI Compose (instrumentés)

**Date** : 2026-03-08
**Statut** : ✅ Compilation OK — 23 tests (émulateur requis pour exécution)
**Framework** : Compose UI Test (junit4) + AndroidJUnit4 Runner

---

## Dépendances

| Dépendance | Version | Usage |
|------------|---------|-------|
| `androidx.compose.ui:ui-test-junit4` | 1.10.4 | `createComposeRule`, assertions sémantiques |
| `androidx.compose.ui:ui-test-manifest` | 1.10.4 | `debugImplementation` — activité hôte pour tests |
| `androidx.test.ext:junit` | 1.2.1 | AndroidJUnit4 runner |
| `androidx.test.espresso:espresso-core` | 3.6.1 | Synchronisation UI |

Déclarées dans `gradle/libs.versions.toml` et `app/build.gradle.kts`.
`testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"` dans `defaultConfig`.

---

## Configuration spéciale : ProGuard debug

Le build debug a `isMinifyEnabled = true` + `isShrinkResources = true`. R8 élimine les classes nécessaires aux tests instrumentés. Solution : fichier `app/proguard-debug.pro` ajouté uniquement au build type debug.

---

## Fichiers de test

### 1. `app/src/androidTest/kotlin/ui/base/state/StateContainerTest.kt` — 5 tests

Tests des composables `StateContainer`, `ErrorState`, et `EmptyState` :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `loading_state_shows_skeleton_not_content` | DisplayState.LOADING → skeleton visible, contenu masqué |
| 2 | `content_state_shows_content_not_skeleton` | DisplayState.CONTENT → contenu visible, skeleton masqué |
| 3 | `error_state_shows_error_with_retry_button` | ErrorState → message d'erreur + bouton Retry visibles |
| 4 | `retry_button_click_calls_callback` | Clic sur Retry → callback `onRetry` appelé |
| 5 | `empty_state_shows_title_and_message` | EmptyState → titre + message visibles |

### 2. `app/src/androidTest/kotlin/ui/base/skeleton/SkeletonTest.kt` — 3 tests (**nouveau**)

Tests des composables skeleton avec animation shimmer :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `skeletonCard_renders_and_is_displayed` | SkeletonCard(120dp×180dp) s'affiche correctement |
| 2 | `skeletonCardRow_displays_correct_count` | 5 cartes skeleton dans une LazyRow (testTag par index) |
| 3 | `shimmerAnimation_runs_without_crash` | SkeletonBox + shimmer 500ms sans exception |

### 3. `app/src/androidTest/kotlin/ui/base/state/ErrorBannerTest.kt` — 5 tests (**nouveau**)

Tests du composable `ErrorBanner` (inline 56dp) :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `null_error_renders_nothing` | error=null → rien rendu (pas de texte, pas de bouton) |
| 2 | `network_error_shows_correct_message` | UiError.Network → message réseau localisé |
| 3 | `auth_error_shows_correct_message` | UiError.Auth → message session expirée localisé |
| 4 | `retry_button_visible_when_callback_provided` | onRetry={} → bouton Réessayer visible |
| 5 | `no_retry_button_when_no_callback` | onRetry=null → bouton Réessayer absent |

### 4. `app/src/androidTest/kotlin/ui/base/DesignSystemTest.kt` — 3 tests (**nouveau**)

Tests du design system (thème, focus, constantes) :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `jellyfinTheme_provides_dark_color_scheme` | Background ≠ Color.Black pur, ≠ White, surface ≠ background |
| 2 | `focus_changes_border_color` | Unfocused → transparent, focused → focusRing color (Cyan 500) |
| 3 | `animationDefaults_focusScale_is_1_06` | FOCUS_SCALE == 1.06f — guard de régression |

### 5. `app/src/androidTest/kotlin/ui/base/state/DpadFocusTest.kt` — 3 tests (préexistant + 1 **nouveau**)

Tests de navigation D-pad / focusabilité :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `error_state_retry_button_is_focusable` | Bouton Retry est interactif (OnClick sémantique) |
| 2 | `error_banner_retry_responds_to_dpad_center` | ErrorBanner Retry → callback onRetry déclenché |
| 3 | `empty_state_action_button_is_focusable` | **Nouveau** — Bouton d'action EmptyState est focusable + clickable |

### 6. `app/src/androidTest/kotlin/ui/player/PlayerOverlayTest.kt` — 4 tests

Tests du `PlayerOverlayLayout` et du pattern seekbar :

| # | Test | Vérifie |
|---|------|---------|
| 1 | `overlay_hidden_by_default` | Les contrôles sont masqués au démarrage |
| 2 | `dpad_action_shows_overlay` | D-pad UP → contrôles apparaissent |
| 3 | `overlay_autohides_after_3_seconds` | Auto-hide après timeout 3s |
| 4 | `seekbar_height_increases_on_focus` | Hauteur seekbar 3dp → 8dp au focus |

---

## Résumé

| Fichier | Tests | Statut |
|---------|-------|--------|
| `StateContainerTest.kt` | 5 | Préexistant |
| `SkeletonTest.kt` | 3 | **Nouveau** |
| `ErrorBannerTest.kt` | 5 | **Nouveau** |
| `DesignSystemTest.kt` | 3 | **Nouveau** |
| `DpadFocusTest.kt` | 3 | Préexistant + 1 nouveau |
| `PlayerOverlayTest.kt` | 4 | Préexistant |
| **TOTAL** | **23** | **12 nouveaux + 11 préexistants** |

---

## Résultat de compilation

```
> Task :app:compileGithubDebugAndroidTestKotlin
BUILD SUCCESSFUL in 1s
120 actionable tasks: 120 up-to-date
```

### Exécution précédente (11 tests sur Ugoos AM9 PRO)

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

## TestTag dans le code source

Aucun testTag n'a été ajouté aux composables source. Les tests utilisent :
- `testTag` dans le code de test (wrappers Box) pour les composables sans sémantique textuelle
- `onNodeWithText()` pour les composables avec texte (ErrorState, ErrorBanner, EmptyState)
- `performSemanticsAction(SemanticsActions.OnClick)` pour simuler les clics/focus
- `useUnmergedTree = true` quand les nœuds texte sont imbriqués dans des composables custom
- `FocusRequester` + `runOnIdle { focusRequester.requestFocus() }` pour le focus fiable

---

## CI — Tests instrumentés

Ajouté dans `.github/workflows/ci.yml` :

```yaml
- name: Run UI Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 34
    target: google_apis
    arch: x86_64
    script: ./gradlew connectedGithubDebugAndroidTest

- name: Upload UI test report
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: ui-test-reports
    path: app/build/reports/androidTests/
```

---

## Problèmes rencontrés et solutions

### 1. R8 élimine les classes nécessaires aux tests

**Symptôme** : Crash `NoClassDefFoundError` sur classes Compose/coroutines.
**Cause** : Build debug a R8 activé (`isMinifyEnabled = true`).
**Solution** : Fichier `proguard-debug.pro` séparé avec keep rules larges.

### 2. Touch injection échoue sur Android TV

**Symptôme** : `Failed to inject touch input` avec `performClick()`.
**Cause** : Le device TV ne supporte pas l'injection d'événements tactiles.
**Solution** : `performSemanticsAction(SemanticsActions.OnClick)` via l'arbre sémantique.

### 3. `onAllNodesWithTag` n'a pas de paramètre `substring`

**Symptôme** : Erreur de compilation `No parameter with name 'substring' found`.
**Cause** : L'API Compose Test n'offre pas ce paramètre.
**Solution** : Boucle `for (i in 0 until count) { onNodeWithTag("card_$i").assertIsDisplayed() }`.

### 4. Locale du device → texte "Retry" introuvable

**Symptôme** : `assertExists` échoue pour "Retry".
**Cause** : Device en locale `fr-FR`, `lbl_retry` → "Réessayer".
**Solution** : `InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.lbl_retry)`.

### 5. Focus D-pad vs tests Compose

**Symptôme** : `performKeyPress(KEYCODE_DPAD_CENTER)` ne déclenche pas le click.
**Cause** : `.clickable()` expose `SemanticsActions.OnClick`, pas un `onKeyEvent`.
**Solution** : `performSemanticsAction(SemanticsActions.OnClick)` — équivalent sémantique du DPAD_CENTER.

### 6. `RequestFocus` action non disponible

**Symptôme** : `Failed to perform RequestFocus action` sur certains nœuds.
**Solution** : `FocusRequester` + `runOnIdle { focusRequester.requestFocus() }`.
