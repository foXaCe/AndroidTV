# Fix Fox Premium — Transition synchronisée

## Problème

L'animation d'entrée du renard VegafoX utilisait deux `Animatable` indépendants
avec des springs bouncy. Cela causait un effet de rebond non premium et une
désynchronisation entre scale et alpha.

## Solution

Remplacement par une `Transition` unique (`updateTransition`) sur un état booléen
`visible`, garantissant une timeline synchronisée pour scale et alpha.

### Avant (Animatable spring)

```kotlin
val scale = remember { Animatable(0.7f) }
val alpha = remember { Animatable(0f) }
// Deux launch coroutines indépendantes avec spring()
```

### Après (Transition tween)

```kotlin
var visible by remember { mutableStateOf(!animated) }
LaunchedEffect(Unit) {
    if (animated) {
        delay(200)
        visible = true
    }
}
val transition = updateTransition(targetState = visible, label = "foxEntry")
val scale by transition.animateFloat(
    transitionSpec = { tween(700, easing = FastOutSlowInEasing) },
) { state -> if (state) 1f else 0.82f }
val alpha by transition.animateFloat(
    transitionSpec = { tween(500, easing = EaseOut) },
) { state -> if (state) 1f else 0f }
```

## Paramètres d'animation

| Propriété | Initial | Final | Durée | Easing             |
|-----------|---------|-------|-------|--------------------|
| scale     | 0.82f   | 1f    | 700ms | FastOutSlowInEasing|
| alpha     | 0f      | 1f    | 500ms | EaseOut            |

- Délai initial unique : 200ms avant `visible = true`
- Pas de spring, pas de bounce
- Halo glow (infiniteTransition) inchangé

## Fichier modifié

- `app/src/main/java/org/jellyfin/androidtv/ui/startup/compose/VegafoXFoxLogo.kt`

## Validation

- BUILD SUCCESSFUL : debug + release
