# US S3 — UserSelectionScreen layout refactor

## Fichiers

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Modifie | Refonte layout : Row centree, header top-left, titre offset 20%, boutons bottom 5% |

## Changements

### LazyRow → Row centree

- `LazyRow` remplacee par `Row` simple avec `Arrangement.spacedBy(32.dp)` et `Arrangement.Center` implicite
- `forEachIndexed` au lieu de `itemsIndexed`
- Animations staggered conservees (scale + alpha spring/tween)

### focusedIndex

- Initialise a `0` (premier utilisateur pre-selectionne visuellement)
- Avant : `-1` (aucune carte focusee au demarrage)

### Header top-left

| Composant | Specs |
|-----------|-------|
| VegafoXFoxLogo | 28dp, animated=false |
| VegafoXTitleText | 16sp |
| serverName | 10sp, uppercase, TextHint (#7A756B) |
| Padding | start=24dp, top=20dp |
| Espacement | 8dp logo→titre, 12dp titre→server |

### Titre "Qui regarde ?"

- 36sp Bold TextPrimary
- Centre horizontalement (`Alignment.TopCenter`)
- Position : `offset(y = screenHeight * 0.20f)` (20% depuis le haut)

### Boutons bottom

- Alignes en bas centre (`Alignment.BottomCenter`)
- Padding bottom : `screenHeight * 0.05f` (5%)
- VegafoXButton Secondary "Ajouter un compte"
- VegafoXButton Ghost "Changer de serveur"
- Espacement 16dp entre les boutons

### Structure Box

- `Box` racine remplacee par `BoxWithConstraints` pour acceder a `maxHeight`
- Layout absolu (align + offset) au lieu de Column sequentiel
- Cards centrees via `Box(Alignment.Center)` intermediaire

## Build

- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL
