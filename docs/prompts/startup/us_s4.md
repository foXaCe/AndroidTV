# US S4 — Animation d'entree slide-up sur SpotCard

## Fichiers

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Modifie | Remplace animation stagger scale+alpha par slide-up translationY+alpha |

## Changements

### Animation supprimee (us1d)

| Propriete | Spec |
|-----------|------|
| `scale` (X+Y) | spring(DampingRatioMediumBouncy) 0.85→1 |
| `alpha` | tween(400ms) 0→1 |

### Nouvelle animation

| Propriete | De | Vers | Spec |
|-----------|----|------|------|
| `translationY` | +40dp | 0dp | tween(500ms, EaseOutCubic) |
| `alpha` | 0f | 1f | tween(400ms, EaseOutCubic) |

- EaseOutCubic : `CubicBezierEasing(0.33f, 1f, 0.68f, 1f)`
- Stagger delay : 60ms par index (avant 80ms)
- State : `appeared` (remplace `visible`)
- `animateDpAsState` pour translationY, converti en px via `density.toPx()`
- Application via `graphicsLayer { translationY = ...; alpha = ... }`

### Imports modifies

- Ajoutes : `CubicBezierEasing`, `animateDpAsState`
- Supprimes : `Spring`, `spring`

## Build

- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL

## Deploy

- `assembleGithubDebug` → BUILD SUCCESSFUL → adb install Success (192.168.1.152)
- `assembleGithubRelease` → BUILD SUCCESSFUL → adb install Success (192.168.1.152)
