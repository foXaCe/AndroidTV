# UI Week 1 — Dark Premium Color Tokens

## Fichiers crees/modifies

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/base/theme/VegafoXColors.kt` | Cree | Palette Dark Premium (fonds, orange, texte, etats) |
| `app/.../ui/base/theme/VegafoXGradients.kt` | Cree | Gradients decoratifs (orbe, horizon, bouton, fond) |
| `app/.../ui/base/colorScheme.kt` | Mis a jour | `colorScheme()` utilise VegafoXColors au lieu des tokens Jellyfin |

## Package

`org.jellyfin.androidtv.ui.base.theme`

## Palette VegafoXColors

### Fonds
- `Background` = `#0A0A0F` — fond principal
- `BackgroundDeep` = `#07070B` — fond profond
- `Surface` = `#141418` — cards
- `SurfaceBright` = `#1C1C22` — elevated

### Orange VegafoX
- `OrangePrimary` = `#FF6B00` — accent principal
- `OrangeDark` = `#CC5500` — pressed
- `OrangeLight` / `OrangeWarm` = `#FF8C00` — gradient
- `OrangeGlow` = `#55FF6B00` (34%) — halos
- `OrangeSoft` = `#1AFF6B00` (10%) — surfaces tintees
- `OrangeBorder` = `#4DFF6B00` (30%) — focus borders

### Texte
- `TextPrimary` = `#F5F0EB` — warm off-white
- `TextSecondary` = `#9E9688` — muted
- `TextDisabled` = `#5C584F`
- `TextHint` = `#7A756B`

### Etats
- `Success` = `#22C55E` / `Error` = `#EF4444` / `Warning` = `#EAB308` / `Info` = `#4E98F9`

## Gradients VegafoXGradients

- `OrbCenter` — radial orange glow (ecrans auth)
- `HorizonLine` — separateur decoratif horizontal
- `OrangeButton` — linear gradient bouton principal
- `ScreenBackground` — radial subtil fond chaud

## Integration

`colorScheme()` dans `colorScheme.kt` reference desormais `VegafoXColors.*` pour tous les slots :
- `primary` / `focusRing` / `badge` → `OrangePrimary`
- `buttonFocused` → `OrangePrimary` (texte sur fond sombre)
- `rangeControlFill` → `OrangePrimary`
- Gradients details → tons orange chauds

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- 0 erreur de compilation
