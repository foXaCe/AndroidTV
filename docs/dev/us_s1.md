# US S1 — Canvas decoratif UserSelectionScreen

## Fichier modifie

| Fichier | Description |
|---------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Ajout Canvas arriere-plan avec gradients et ligne horizon |

## Changements

### Canvas arriere-plan
Le fond uni `VegafoXColors.Background` est conserve comme base. Un `Canvas` plein ecran est ajoute en premiere couche dans le `Box`, avant la `Column` de contenu.

### Orbe primaire
- **Couleur** : `VegafoXColors.OrangePrimary` alpha 0.10
- **Centre** : 50% largeur, 90% hauteur
- **Rayon** : 40% de la largeur (ellipse 80%)
- **Fondu** : couleur pleine de 0% a 40% du rayon, puis fondu vers transparent de 40% a 100% (zone de fondu = 60%)

### Orbe secondaire
- **Couleur** : `VegafoXColors.OrangeWarm` alpha 0.06
- **Centre** : identique (50%, 90%)
- **Rayon** : 25% de la largeur (plus petit)
- **Fondu** : lineaire de centre a bord

### Ligne horizon
- **Position** : 70% de la hauteur
- **Style** : `Brush.horizontalGradient` transparent → `VegafoXColors.OrangeBorder` → transparent
- **Epaisseur** : 1px

## Imports ajoutes
- `androidx.compose.foundation.Canvas`
- `androidx.compose.ui.geometry.Offset`
- `androidx.compose.ui.graphics.Brush`
- `androidx.compose.ui.graphics.Color`

## Build
- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL
