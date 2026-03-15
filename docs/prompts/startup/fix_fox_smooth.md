# fix_fox_smooth — Animation renard sans saccade (graphicsLayer only)

## Problème
`AnimatedVisibility` avec `scaleIn` + `fadeIn` autour du renard dans WelcomeContent
provoquait des remesures layout à chaque frame, causant des saccades visuelles.
Le halo Canvas pulsant relisait `glowScale` dans le drawScope, forçant un redraw complet.

## Solution

### 1. `VegafoXFoxLogo.kt` — Halo Canvas via graphicsLayer

**Avant** : `glowScale` lu dans le drawScope → redraw Canvas chaque frame
```kotlin
Canvas(modifier = Modifier.matchParentSize()) {
    val glowRadius = (size.toPx() * 0.8f) * glowScale
    drawCircle(...)
}
```

**Après** : `glowScale` appliqué via `graphicsLayer` → transformation GPU, pas de redraw
```kotlin
Canvas(
    modifier = Modifier
        .matchParentSize()
        .graphicsLayer {
            scaleX = glowScale
            scaleY = glowScale
        },
) {
    val glowRadius = size.toPx() * 0.8f  // rayon fixe
    drawCircle(...)
}
```

Le renard (Image) utilise déjà `graphicsLayer` avec `Animatable` pour scale et alpha — inchangé.

### 2. `WelcomeScreen.kt` — Suppression AnimatedVisibility autour du renard

**Avant** :
```kotlin
AnimatedVisibility(
    visible = showFox,
    enter = scaleIn(...) + fadeIn(...),
) {
    VegafoXFoxLogo(animated = false)
}
```

**Après** :
```kotlin
VegafoXFoxLogo(animated = true)
```

- Variable `showFox` supprimée
- Timing séquentiel ajusté : `delay(600)` avant `showTitle` (au lieu de `delay(200) + delay(400)`)
- Imports inutilisés supprimés : `scaleIn`, `FastOutSlowInEasing`

### Principe
- Le composable `VegafoXFoxLogo` est présent dans le layout dès le premier frame
- Scale (`Animatable` 0.7f → 1f) : `spring(DampingRatioMediumBouncy, StiffnessLow)` via `graphicsLayer`
- Alpha (`Animatable` 0f → 1f) : `tween(600ms)` via `graphicsLayer`
- Halo pulsant : `infiniteRepeatable` via `graphicsLayer` (scaleX/scaleY)
- Aucun impact layout — zero remesure, zero recomposition

## Build
BUILD SUCCESSFUL — debug + release
