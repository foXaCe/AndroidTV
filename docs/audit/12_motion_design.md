# Audit 12 - Motion Design & Micro-interactions

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Toutes les modifications vérifiées :
> - AnimationDefaults.kt : FOCUS_SCALE, PRESS_SCALE, focusSpec() présents
> - ShakeModifier.kt, StaggerModifier.kt créés
> - ButtonBase.kt : press feedback 0.95x implémenté
> - Transitions settings 400ms → 300ms
> - 7 composants avec focus animé smooth
> - rememberReducedMotion() disponible

## Animations implementees

### 1. Focus Animations

#### Corrections appliquees

| Fichier | Avant | Apres |
|---------|-------|-------|
| `ExpandableIconButton.kt` | scale 1.05f, tween(200ms) sans easing | scale 1.06f, tween(150ms, FastOutSlowIn) via `focusSpec()` |
| `LibraryBrowseComponents.kt` | scale instant (pas d'animation) | `animateFloatAsState` + `focusSpec()` (150ms, FastOutSlowIn) |
| `ErrorState.kt` (RetryButton) | `graphicsLayer` instant | `animateFloatAsState` + `focusSpec()` (150ms, FastOutSlowIn) |

#### Specification uniforme du focus

| Propriete | Valeur |
|-----------|--------|
| Scale | 1.0 -> 1.06 |
| Duree | 150ms (`AnimationDefaults.DURATION_FAST`) |
| Interpolateur | `FastOutSlowInEasing` |
| Retour (unfocus) | Meme spec, smooth retour a 1.0 |
| Methode standard | `AnimationDefaults.focusSpec()` |

#### Toutes les occurrences focus scale dans le codebase

| Composant | Fichier | Methode | Conforme |
|-----------|---------|---------|----------|
| UserCard | `UserCardView.kt:69` | `animateFloatAsState(FOCUS_SCALE)` | Oui (default smooth) |
| MainToolbar avatar | `MainToolbar.kt:297` | `animateFloatAsState(FOCUS_SCALE)` | Oui |
| ExpandableIconButton | `ExpandableIconButton.kt:56` | `animateFloatAsState(FOCUS_SCALE, focusSpec())` | Oui (corrige) |
| SubtitlePresets | `SubtitleColorPresetsControl.kt:39` | `animateFloatAsState(FOCUS_SCALE)` | Oui |
| LibraryPosterCard | `LibraryBrowseComponents.kt:150` | `animateFloatAsState(FOCUS_SCALE, focusSpec())` | Oui (corrige) |
| RetryButton | `ErrorState.kt` | `animateFloatAsState(FOCUS_SCALE, focusSpec())` | Oui (corrige) |
| ButtonBase | `ButtonBase.kt` | `animateFloatAsState(FOCUS_SCALE, focusSpec())` | Oui (ajoute) |

### 2. Press Feedback (nouveau)

| Propriete | Valeur |
|-----------|--------|
| Scale | 1.0 -> 0.95 |
| Duree | 150ms (FastOutSlowIn) |
| Composant | `ButtonBase.kt` — tous les boutons du DS |
| Constante | `AnimationDefaults.PRESS_SCALE = 0.95f` |
| Priorite | pressed > focused > default |

Le `ButtonBase` gere maintenant 3 etats de scale :
- Default: 1.0
- Focused: 1.06 (scale up)
- Pressed: 0.95 (scale down pour feedback tactile)

### 3. Shake Animation (nouveau)

| Propriete | Valeur |
|-----------|--------|
| Type | Translation horizontale sinusoidale |
| Oscillations | 3 |
| Amplitude | 6dp (decroissante) |
| Duree | 200ms |
| Fichier | `ui/base/modifier/ShakeModifier.kt` |
| Usage | `Modifier.shake(shakeState)` + `shakeState.shake()` |

Usage type :
```kotlin
val shakeState = rememberShakeState()
Box(modifier = Modifier.shake(shakeState)) { ... }
// Quand l'action est impossible :
scope.launch { shakeState.shake() }
```

### 4. Stagger Animation (nouveau)

| Propriete | Valeur |
|-----------|--------|
| Type | Fade-in + slide-up par item |
| Delai inter-item | 30ms (`AnimationDefaults.STAGGER_DELAY`) |
| Duree par item | 300ms (FastOutSlowIn) |
| Offset Y initial | 20dp |
| Max items staggers | 15 (au-dela, apparition instantanee) |
| Fichier | `ui/base/modifier/StaggerModifier.kt` |
| Condition | Premier chargement uniquement |

Usage type :
```kotlin
LazyColumn {
    itemsIndexed(items) { index, item ->
        ItemCard(modifier = Modifier.staggerFadeIn(index))
    }
}
```

### 5. Transitions entre ecrans

#### Corrections appliquees

| Fichier | Avant | Apres |
|---------|-------|-------|
| `SettingsRouterContent.kt` | 400ms | 300ms (`AnimationDefaults.DURATION_MEDIUM`) |
| `SettingsDialog.kt` | 400ms | 300ms (`AnimationDefaults.DURATION_MEDIUM`) |

#### Specification des transitions

| Transition | Enter | Exit | Duree | Easing |
|------------|-------|------|-------|--------|
| Navigation settings | slideIn(right) + fadeIn | slideOut(-1/3) + fadeOut | 300ms | FastOutSlowIn |
| Pop settings (retour) | slideIn(-1/3) + fadeIn | slideOut(right) + fadeOut | 300ms | FastOutSlowIn |
| Dialog settings | slideIn(right) + fadeIn | slideOut(right) + fadeOut | 300ms | FastOutSlowIn |
| Dialog generique | fadeIn | fadeOut | 300ms | Linear |
| Player overlay header | slideIn(haut) + fadeIn | slideOut(haut) + fadeOut | 300ms | Default |
| Player overlay controls | slideIn(bas) + fadeIn | slideOut(bas) + fadeOut | 300ms | Default |
| MediaBar logo | Crossfade | Crossfade | 300ms | Default |
| Ecran de veille | fadeIn | fadeOut | 1000ms | Default |

### 6. Reduced Motion (nouveau)

| Propriete | Valeur |
|-----------|--------|
| Detection | `Settings.Global.ANIMATOR_DURATION_SCALE` |
| Composable | `rememberReducedMotion(): Boolean` |
| Fichier | `AnimationDefaults.kt` |
| Logique | Retourne `true` si ANIMATOR_DURATION_SCALE == 0 |

Quand `rememberReducedMotion()` retourne true, les composants peuvent
utiliser cette info pour desactiver les animations non essentielles
(shimmer, stagger, shake).

---

## Animations NON implementees et justification

### Shared Element Transition (carte -> detail)

**Raison** : L'infrastructure `SharedTransitionLayout` existe dans `router.kt` mais la navigation
actuelle passe par des fragments Android (pas Compose Navigation), ce qui rend les shared elements
cross-fragment tres complexes. Les fragments V2 utilisent `NavigationRepository.navigate()` qui
cree un nouveau fragment plutot que de pousser une route Compose.

**Impact perf** : La capture bitmap requise pour le shared element ajouterait un frame drop
sur les appareils bas de gamme (Fire TV Stick).

**Alternative actuelle** : Fade 300ms entre les ecrans, coherent et performant.

**Prerequis pour implementation future** : Migration complete vers Compose Navigation (pas de fragments).

### Animation favoris/coeur

**Raison** : Le systeme de favoris utilise `ItemLauncher` et des callbacks
qui ne passent pas par des composables Compose. L'animation necesiterait
une refonte du flux de gestion des favoris pour etre centralisee.

**Alternative** : Le feedback actuel est un toast de confirmation.

### DefaultItemAnimator personnalise pour RecyclerView

**Raison** : Les ecrans V2 (Compose) n'utilisent plus de RecyclerView.
Les ecrans legacy (Leanback) ont leur propre ItemAnimator geree par
le framework Leanback. Modifier ces animateurs risque de casser le
comportement attendu par Leanback.

### Animation d'elevation/ombre sur focus

**Raison** : Sur Android TV, les ombres portees sont couteuses en GPU
et souvent invisibles sur les TV avec des noirs profonds (OLED).
La bordure de couleur configurable (`focusBorderColor`) + le scale 1.06x
fournissent un feedback visuel suffisant et plus performant.

### Ripple effect sur les boutons

**Raison** : `indication = null` est volontaire dans `ButtonBase` car
le ripple Material n'est pas visible sur un ecran TV a distance de canape.
Le press feedback 0.95x est un meilleur indicateur visuel a distance.

---

## Regles d'animation pour les futurs developpements

### Durees

| Type | Duree max | Constante |
|------|-----------|-----------|
| Micro-interaction (focus, press) | 150ms | `DURATION_FAST` |
| Transition ecran, fade, dialog | 300ms | `DURATION_MEDIUM` |
| Transition complexe | 500ms | `DURATION_SLOW` |
| Toute animation | 500ms max | Regle absolue |

### Interpolateurs

| Type d'animation | Interpolateur |
|------------------|---------------|
| Focus scale | `FastOutSlowInEasing` via `focusSpec()` |
| Transitions ecran | `FastOutSlowInEasing` |
| Shimmer/progres | `LinearEasing` |
| Fade simple | Default (linear) |

### Regles

1. **Toute animation > 500ms est interdite** (sauf screensaver)
2. **Aucune animation ne bloque l'interaction** — utiliser `graphicsLayer` ou `animateAsState`
3. **`focusSpec()` obligatoire** pour toute animation de focus
4. **`AnimationDefaults.FOCUS_SCALE` (1.06f)** — seule valeur de scale focus autorisee
5. **Jamais deux elements au meme niveau de scale** — un seul element focuse a la fois
6. **Retour de focus symetrique** — meme spec pour focus et unfocus
7. **Pas d'animation au scroll** — seulement au premier chargement (`staggerFadeIn`)
8. **Respecter reduced motion** — `rememberReducedMotion()` disponible pour desactiver
9. **Transitions symetriques** — l'animation de retour est l'inverse de l'entree
10. **Pas de spring()** — tween avec FastOutSlowIn donne un resultat plus previsible sur TV

### Fichiers reference

| Fichier | Role |
|---------|------|
| `ui/base/AnimationDefaults.kt` | Constantes, `focusSpec()`, `rememberReducedMotion()` |
| `ui/base/modifier/ShakeModifier.kt` | Animation shake (action impossible) |
| `ui/base/modifier/StaggerModifier.kt` | Stagger fade-in pour listes |
| `ui/base/state/StateContainer.kt` | Transitions loading/content/error/empty |

---

## Impact

| Metrique | Avant | Apres |
|----------|-------|-------|
| Valeurs de focus scale differentes | 3 (1.05, 1.06, instant) | 1 (1.06) |
| Composants avec focus anime (smooth) | 4 | 7 |
| Press feedback sur boutons | 0 | Tous (ButtonBase) |
| Transitions > 300ms | 2 fichiers (400ms) | 0 |
| Durees hardcodees dans le code | 8+ | 0 (toutes via constantes) |
| Check reduced motion | 0 | 1 (helper disponible) |
| Modifiers d'animation reutilisables | 0 | 2 (shake, stagger) |
