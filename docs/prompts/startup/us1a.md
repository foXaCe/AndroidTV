# US1a — Composable UserCard

## Fichier cree

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserCard.kt` | Cree | Card utilisateur pour ecran de selection |

## Package

`org.jellyfin.androidtv.ui.startup.user`

## Signature

```kotlin
@Composable
fun UserCard(
    name: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

## Specs visuelles

| Propriete | Valeur |
|-----------|--------|
| Taille | 130dp x 180dp |
| Fond | SurfaceBright (`#1C1C22`) |
| Coins | 20dp RoundedCornerShape |
| Photo profil | 96dp circulaire (CircleShape), coil3 AsyncImage |
| Nom | 14sp, FontWeight.Medium, TextPrimary, 1 ligne, ellipsis |
| Espacement haut | 16dp avant photo |
| Espacement photo-nom | 12dp |

## Etats focus (D-pad)

| Effet | Valeur | Animation |
|-------|--------|-----------|
| Scale | 1f → 1.08f | tween 150ms EaseOutCubic |
| Border | 2dp OrangePrimary | apparait au focus |
| Glow | drawBehind radialGradient OrangeGlow | alpha animee 150ms |

## Interaction

- `onKeyEvent` : Enter + DirectionCenter (KeyUp) → onClick
- `clickable` : support tactile sans ripple
- Pas de guard double-clic (navigation geree par le parent)

## Dependencies

- `coil3.compose.AsyncImage` (deja dans `libs.versions.toml`)
- `VegafoXColors` (tokens existants)
- Aucun ViewModel, aucun Koin

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:compileGithubReleaseKotlin` → BUILD SUCCESSFUL
