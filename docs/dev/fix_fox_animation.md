# Fix — Animation spring saccadée du renard (VegafoXFoxLogo)

## Problème

L'animation d'entrée du renard (scale 0.7→1.0 + fade-in) était saccadée sur certains appareils Android TV.

### Cause racine

`animateFloatAsState` est piloté par un état (`visible`).
Le flip `visible = true` provoque une **recomposition complète** du composable au frame exact où l'animation démarre, ce qui crée un saut visuel (1 frame à 0.7f rendu, recomposition, puis démarrage progressif).

De plus, `graphicsLayer` était placé **après** `.size()` dans la chaîne de Modifier, forçant un recalcul de layout à chaque frame d'animation au lieu d'opérer uniquement sur la couche graphique.

## Corrections appliquées

### 1. `Animatable` au lieu de `animateFloatAsState`

**Avant** :
```kotlin
var visible by remember { mutableStateOf(!animated) }
LaunchedEffect(Unit) {
    if (animated) { delay(200); visible = true }
}
val scale by animateFloatAsState(
    targetValue = if (visible) 1f else 0.7f, ...
)
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f, ...
)
```

**Après** :
```kotlin
val scale = remember { Animatable(if (animated) 0.7f else 1f) }
val alpha = remember { Animatable(if (animated) 0f else 1f) }

LaunchedEffect(Unit) {
    if (animated) {
        delay(200)
        launch { scale.animateTo(1f, spring(DampingRatioMediumBouncy, StiffnessLow)) }
        launch { alpha.animateTo(1f, tween(600)) }
    }
}
```

**Pourquoi** : `Animatable` est impératif — il ne dépend d'aucun state composable. Les deux animations démarrent en parallèle via `launch` sans provoquer de recomposition. Seul le `graphicsLayer` lambda est réévalué à chaque frame (lecture de `.value`), pas le composable parent.

### 2. `graphicsLayer` en premier dans la chaîne Modifier

**Avant** :
```kotlin
Modifier.size(size).graphicsLayer { scaleX = scale; scaleY = scale; alpha = alpha }
```

**Après** :
```kotlin
Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value; alpha = alpha.value }.size(size)
```

**Pourquoi** : `graphicsLayer` en premier signifie que le scale/alpha s'applique **sur la couche rendu** sans invalider le layout. Si `.size()` est avant, le layout est résolu d'abord puis le graphicsLayer agit dessus — ce qui fonctionne, mais si le framework détecte un changement potentiel de bounds il peut re-mesurer inutilement.

### 3. Suppression du state `visible`

Le `mutableStateOf(visible)` n'existe plus. Aucun state ne change pendant les 800ms d'animation, donc le parent `Box` et le `Canvas` (halo) ne recomposent pas.

## Fichier modifié

- `app/src/main/java/org/jellyfin/androidtv/ui/startup/compose/VegafoXFoxLogo.kt`

## Build

```
./gradlew :app:compileGithubDebugKotlin → BUILD SUCCESSFUL
0 erreur, 0 warning
```
