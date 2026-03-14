# UI Week 2 — Composables réutilisables

## Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `VegafoXFoxLogo.kt` | `ui.startup.compose` | Image renard + halo orange pulsant + animation entrée spring |
| `VegafoXTitleText.kt` | `ui.startup.compose` | "Vega" blanc / "foX" orange (`VegafoXTitleText`) + sous-titre "MEDIA CENTER" (`VegafoXSubtitle`) |
| `ConnectServerButton.kt` | `ui.startup.compose` | Bouton orange + scale au focus D-pad + glow radial + flèche AutoMirrored |

## Drawable utilisé

- **`R.drawable.ic_vegafox`** → bitmap wrappant `@mipmap/vegafox_launcher_foreground` (logo renard)

## Détails des composables

### VegafoXFoxLogo

```kotlin
VegafoXFoxLogo(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,       // Taille du renard
    animated: Boolean = true, // Animation entrée spring
)
```

- Animation entrée : scale 0.7→1.0 (spring medium bouncy) + fade-in 600ms
- Halo : pulsation continue 1.0→1.15 (2800ms, EaseInOutSine)
- Couleur halo : `VegafoXColors.OrangeGlow`

### VegafoXTitleText

```kotlin
VegafoXTitleText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 52.sp,
)
```

- "Vega" en `VegafoXColors.TextPrimary`, "foX" en `VegafoXColors.OrangePrimary`
- FontWeight.Bold, letterSpacing -1.sp

### VegafoXSubtitle

```kotlin
VegafoXSubtitle(modifier: Modifier = Modifier)
```

- "MEDIA CENTER" — 13.sp, letterSpacing 4.sp, `VegafoXColors.TextSecondary`

### ConnectServerButton

```kotlin
ConnectServerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    enabled: Boolean = true,
)
```

- Fond : `VegafoXColors.OrangePrimary` (enabled) / `VegafoXColors.Surface` (disabled)
- Texte : `VegafoXColors.Background` (noir) — 17.sp Bold
- Focus D-pad : scale 1.05x + glow radial orange (BlendMode.Screen)
- Icône flèche `Icons.AutoMirrored.Outlined.ArrowForward`
- Min size : 280×56 dp, coins arrondis 14.dp

## Tokens référencés (de ui_w1_tokens)

- `VegafoXColors` : Background, OrangePrimary, OrangeGlow, Surface, TextPrimary, TextSecondary
- Aucun nouveau token créé

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- 0 erreur, 0 warning
