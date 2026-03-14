# Design System — VegafoXButton & VegafoXIconButton

## Fichier

`app/src/main/java/org/jellyfin/androidtv/ui/base/components/VegafoXButton.kt`

## VegafoXButton — Signature

```kotlin
@Composable
fun VegafoXButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: VegafoXButtonVariant = Primary,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    icon: ImageVector? = null,
    iconEnd: Boolean = true,
)
```

## Variantes (VegafoXButtonVariant)

| Variante | Fond | Border | Texte |
|----------|------|--------|-------|
| **Primary** | OrangePrimary | aucune | Background (noir) |
| **Secondary** | transparent | 1dp Divider | TextSecondary |
| **Outlined** | OrangeSoft (10%) | 1dp OrangeBorder (30%) | OrangePrimary |
| **Ghost** | transparent | aucune | TextSecondary |

## Etats focus (communs)

| Effet | Valeur | Animation |
|-------|--------|-----------|
| Scale | 1f → 1.06f | tween 150ms EaseOutCubic |
| Border | 2dp OrangePrimary | remplace border existante |
| Glow | drawBehind radialGradient OrangeGlow | alpha animee 150ms |
| Texte | FontWeight.ExtraBold | immediat |
| Icone | graphicsLayer scale 1.2f | immediat |

`EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)`

## Etat disabled

- `graphicsLayer { alpha = 0.4f }` sur tout le composable
- `focusable(false)` — ne recoit pas le focus D-pad

## Dimensions

| Propriete | Valeur |
|-----------|--------|
| minHeight | 52dp |
| minWidth | 200dp |
| cornerRadius | 14dp (RoundedCornerShape) |
| padding horizontal | 32dp |
| padding vertical | 14dp |
| fontSize | 16sp |
| iconSize | 20dp |

## Regles internes

- **FocusRequester** unique par bouton
- **onKeyEvent** intercepte `Key.Enter` + `Key.DirectionCenter` (KeyUp)
- **Guard navigating** : booleen + delay 400ms pour eviter double clic
- **contentAlignment = Center** garanti via Box
- **autoFocus** : LaunchedEffect delay 500ms + requestFocus

## VegafoXIconButton — Signature

```kotlin
@Composable
fun VegafoXIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = VegafoXColors.TextPrimary,
)
```

## VegafoXIconButton — Specs

| Propriete | Valeur |
|-----------|--------|
| size | 48dp |
| shape | CircleShape |
| iconSize | 24dp |
| fond normal | SurfaceBright |
| fond focused | OrangeSoft |
| border focused | 2dp OrangePrimary CircleShape |
| glow | drawCircle radialGradient OrangeGlow |
| scale focused | 1.06f |
| icon scale focused | 1.2f |
| disabled | alpha 0.4f, non focusable |
| guard double clic | meme mecanisme navigating 400ms |

## Validation

- BUILD SUCCESSFUL : debug + release
