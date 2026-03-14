# US S2 — SpotCard (remplace UserCard)

## Fichiers

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/SpotCard.kt` | Cree | Nouvelle card utilisateur avec variantes Main/Side |
| `app/.../ui/startup/user/UserCard.kt` | Supprime | Remplace par SpotCard |
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Modifie | Utilise SpotCard + tracking focusedIndex |

## Signature

```kotlin
@Composable
fun SpotCard(
    name: String,
    imageUrl: String?,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

## Variante Main (isFocused = true)

| Propriete | Valeur |
|-----------|--------|
| Avatar | 96dp, CircleShape |
| Border | 2dp OrangePrimary |
| TranslationY | -12dp (spring vers le haut) |
| Glow | drawBehind radialGradient OrangeGlow rayon 120dp |
| Halo | Oval OrangePrimary alpha 0.18 sous l'avatar |
| Nom | TextPrimary, 14sp, Bold |
| Alpha | 1.0 |

## Variante Side (isFocused = false)

| Propriete | Valeur |
|-----------|--------|
| Avatar | 80dp, CircleShape |
| Border | 1dp Divider |
| TranslationY | 0dp |
| Glow/Halo | Absents (glowAlpha = 0) |
| Nom | TextSecondary, 12sp, Normal |
| Alpha | 0.65 (graphicsLayer) |

## Animations

Toutes les transitions : `tween(200ms, EaseOutCubic)` — 8 proprietes animees :

- `avatarSize` (animateDpAsState)
- `borderWidth` (animateDpAsState)
- `borderColor` (animateColorAsState)
- `offsetY` (animateDpAsState → translationY px)
- `contentAlpha` (animateFloatAsState)
- `glowAlpha` (animateFloatAsState)
- `nameSize` (animateFloatAsState → sp)
- `nameColor` (animateColorAsState)

## Integration UserSelectionScreen

- `focusedIndex` (mutableIntStateOf) suivi par `onFocusChanged` sur chaque SpotCard
- `isFocused = focusedIndex == index` passe a SpotCard
- Animations d'apparition staggered conservees (scale + alpha spring/tween)

## Interaction

- `onKeyEvent` : Enter / DirectionCenter (KeyUp) → onClick
- `clickable` sans ripple pour support tactile
- `focusable()` pour navigation D-pad

## Build

- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL
