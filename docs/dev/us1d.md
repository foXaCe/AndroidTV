# US1d — Animation d'entree staggered sur UserCard

## Fichier modifie

| Fichier | Modification |
|---------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Animation staggered sur chaque UserCard dans la LazyRow |

## Principe

Chaque carte utilisateur apparait avec un delai progressif (stagger) pour un effet d'entree fluide.

## Implementation

### State et LaunchedEffect

```kotlin
var visible by remember { mutableStateOf(false) }

LaunchedEffect(Unit) {
    delay(80L * index)
    visible = true
}
```

- Chaque carte demarre invisible (`visible = false`)
- Un `LaunchedEffect` attend `80ms * index` avant de passer `visible = true`
- Carte 0 : 0ms, carte 1 : 80ms, carte 2 : 160ms, etc.

### Animations

| Propriete | De | Vers | Spec |
|-----------|----|------|------|
| `scale` (X + Y) | 0.85f | 1f | `spring(DampingRatioMediumBouncy)` |
| `alpha` | 0f | 1f | `tween(400ms)` |

### Application

```kotlin
UserCard(
    modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    },
)
```

`graphicsLayer` est utilise pour appliquer scale et alpha sans recomposition.

## Imports ajoutes

- `androidx.compose.animation.core.Spring`
- `androidx.compose.animation.core.animateFloatAsState`
- `androidx.compose.animation.core.spring`
- `androidx.compose.animation.core.tween`
- `androidx.compose.runtime.LaunchedEffect`
- `androidx.compose.runtime.getValue`
- `androidx.compose.runtime.mutableStateOf`
- `androidx.compose.runtime.remember`
- `androidx.compose.runtime.setValue`
- `androidx.compose.ui.graphics.graphicsLayer`
- `kotlinx.coroutines.delay`

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:compileGithubReleaseKotlin` → BUILD SUCCESSFUL
