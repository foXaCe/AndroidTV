# Audit: Home Screen Background Grid — audit-home-background-01

**Date**: 2026-03-18
**Fichier principal**: `ui/home/compose/HomeScreen.kt` (lignes 355-474)
**Composable**: `DarkGridNoiseBackground()`
**Statut**: Audit lecture seule, aucun fichier modifie

---

## 1. Architecture et technique d'implementation

Le fond est un **Canvas Compose pur** (pas de drawable XML, pas de PNG/WebP tile, pas de shader). Il est dessine
algorithmiquement a chaque composition initiale via `Canvas(modifier = modifier.fillMaxSize())`.

**Aucun bitmap** n'est implique — le rendu est entierement vectoriel/procedural.
**Aucun `drawWithCache`** n'est utilise — c'est un Canvas direct.
**Aucun `drawBehind`** — le composable est un `Canvas` standalone.
**Aucun `RuntimeShader`** — pas de shader AGSL/GLSL.

Le composable est **sans etat** (`@Composable private fun DarkGridNoiseBackground(modifier: Modifier = Modifier)`) et ne recompose jamais apres le premier rendu.

---

## 2. Structure en 5 couches

### Couche 1 — Base solide
- **Technique**: `drawRect(color = GridBaseColor)`
- **Couleur**: `#060A0F` (RGB 6, 10, 15) — noir quasi-pur a legere teinte bleue
- **Couverture**: Plein ecran

### Couche 2 — Bruit fractal (noise)
- **Technique**: 800 appels `drawRect()` pour des points individuels
- **Seed**: `NOISE_SEED = 42` (deterministe, reproductible)
- **Nombre de points**: `NOISE_POINT_COUNT = 800`
- **Taille de chaque point**: `1.5.dp` (converti en px via `LocalDensity`)
- **Couleur**: `Color.White` avec alpha aleatoire entre 0 et `0.025f` (max 2.5%)
- **Distribution**: Aleatoire uniforme sur toute la surface (position x/y)

### Couche 3 — Grille geometrique
- **Technique**: Lignes horizontales + verticales via `drawLine()`
- **Pas de grille (tile size)**: `60.dp` (converti en px)
- **Epaisseur de ligne**: `0.5.dp`
- **Couleur**: `VegafoXColors.OrangePrimary.copy(alpha = 0.03f)` = `#FF6B00` a 3% d'opacite
- **Origine**: `(0, 0)`, couvre toute la surface

### Couche 4 — Lueur radiale (glow elliptique)
- **Technique**: `Brush.radialGradient()` avec transformation native Canvas (scale Y)
- **Forme**: Ellipse 70% largeur x 50% hauteur, centree
- **Couleur centre**: `RadialCenterColor = #0A1525` (RGB 10, 21, 37) a 60% opacite
- **Couleur bord**: `Color.Transparent`
- **Transformation**: `nativeCanvas.scale(1f, scaleY, cx, cy)`

### Couche 5 — Vignettage (4 bords)
- **Technique**: 4 `drawRect()` avec gradients (horizontal/vertical)
- **Opacite de base**: 0.7f (70%)
- **Gauche**: 5% de la largeur (reduit car sidebar presente), 35% noir
- **Droite**: 15% de la largeur, 70% noir
- **Haut**: 15% de la hauteur, 70% noir
- **Bas**: 15% de la hauteur, 70% noir

---

## 3. Palette de couleurs exacte

| Couche | Composant | Hex | Alpha | Description |
|--------|-----------|-----|-------|-------------|
| 1 | Base | `#060A0F` | 100% | Noir bleu tres sombre |
| 2 | Points bruit | `#FFFFFF` | 0-2.5% | Blanc quasi-invisible |
| 3 | Lignes grille | `#FF6B00` | 3% | Orange VegafoX tres subtil |
| 4 | Centre radial | `#0A1525` | 60% | Bleu sombre |
| 5 | Vignette | `#000000` | 35-70% | Noir variable par bord |

---

## 4. Parametres de qualite et de rendu

### FilterQuality
- **Aucun `FilterQuality` n'est utilise** sur le Canvas background — il n'y a pas de bitmap donc pas de reechantillonnage.
- Pas de `FilterQuality.None`, `.Low`, `.Medium` ou `.High` implique.

### Rendu GPU
- Les primitives Canvas (`drawRect`, `drawLine`, `drawRect+Brush`) sont rendues en mode vectoriel par Skia/HWUI.
- La precision est native au GPU — pas de downscaling ni de texture intermediaire.

### Densite
- Toutes les dimensions utilisent `LocalDensity.current` pour la conversion `dp -> px`.
- Sur un ecran TV 1080p (mdpi/tvdpi), `60.dp` = ~60-80px, `0.5.dp` = ~0.5-0.75px, `1.5.dp` = ~1.5-2px.

---

## 5. Diagnostic : pourquoi l'aspect "low quality"

### Cause principale : le bruit est trop clairseme et trop grossier

| Parametre | Valeur actuelle | Probleme |
|-----------|----------------|----------|
| `NOISE_POINT_COUNT` | 800 | Sur un ecran 1920x1080 (~2M pixels), 800 points = 0.04% de couverture. Le bruit est quasiment invisible et n'apporte pas de texture credible. |
| Taille point | `1.5.dp` | Chaque point fait ~2px carres — visible comme un "pixel mort" plutot que du grain fin. |
| Alpha max | `0.025f` | A 2.5% d'opacite max sur blanc, meme les points les plus brillants sont a peine perceptibles. Le bruit ne contribue presque rien visuellement. |

### Cause secondaire : la grille est trop fine et trop transparente

| Parametre | Valeur actuelle | Probleme |
|-----------|----------------|----------|
| Epaisseur ligne | `0.5.dp` | A ~0.5px reel, les lignes sont sub-pixel et le GPU antialiase fortement, ce qui les rend floues/fantomatiques au lieu de nettes. |
| Alpha grille | `0.03f` | A 3% d'opacite, les lignes sont a peine visibles. Combine avec le sub-pixel, elles apparaissent comme un artefact plutot qu'un motif intentionnel. |
| Pas | `60.dp` | Acceptable, mais les lignes sub-pixel a cette distance donnent un quadrillage "bave". |

### Cause tertiaire : interaction avec le hero backdrop

Le `DarkGridNoiseBackground` est la couche 0 (fond), mais le `HomeHeroBackdrop` (couche 1) se superpose avec son propre gradient et blur (`RenderEffect.createBlurEffect(1f, 1f, CLAMP)`). Dans les zones ou le backdrop est visible, la grille est invisible. Dans les zones sans backdrop (bas de l'ecran), la grille apparait seule mais trop faible pour etre qualitative.

### Pas de probleme de FilterQuality

Le diagnostic **ne** montre **pas** de probleme de `FilterQuality` — il n'y a pas de bitmap a reechantillonner. Le probleme est purement dans les parametres algorithmiques du Canvas.

---

## 6. Recommandations d'amelioration

### R1 — Augmenter la densite du bruit
```
NOISE_POINT_COUNT : 800 -> 3000-5000
Taille point : 1.5.dp -> 1.dp (grain plus fin)
Alpha max : 0.025f -> 0.04f-0.06f (plus visible)
```
Cela donnerait un grain cinematique type "film grain" au lieu de pixels morts epars.

### R2 — Epaissir les lignes de grille au-dessus du seuil sub-pixel
```
Epaisseur : 0.5.dp -> 1.dp (au minimum 1px physique)
Alpha : 0.03f -> 0.05f-0.08f (suffisamment visible pour etre intentionnel)
```
A 1dp minimum, les lignes seront nettes et non antialiasees de facon degrade.

### R3 — Utiliser `drawWithCache` pour eviter les re-calculs
Remplacer `Canvas { ... }` par `Modifier.drawWithCache { onDrawBehind { ... } }` pour que les positions de bruit et les calculs de grille soient caches. Actuellement le composable ne recompose jamais (pas d'etat), mais `drawWithCache` ajouterait une garantie supplementaire de performance.

### R4 — Alternative bitmap : generer une texture de bruit haute qualite
Generer un `ImageBitmap` de bruit Perlin/simplex en une seule passe au lieu de 800 `drawRect()` individuels. Un bitmap 256x256 tile avec `FilterQuality.High` et `TileMode.Repeat` donnerait un grain plus riche et plus performant (un seul draw call au lieu de 800+).

### R5 — Optionnel : shader AGSL pour le bruit
Sur API 33+, un `RuntimeShader` AGSL pourrait generer du bruit procedural GPU-natif avec une densite infinie et zero allocation. Fallback sur bitmap pour API < 33.

---

## 7. Hierarchie de rendu (Z-order)

```
Z0  DarkGridNoiseBackground()    <- Grille + bruit (analyse ci-dessus)
Z1  HomeHeroBackdrop()           <- Image backdrop + trailer video
Z2  Column { content }           <- Sidebar, rangees, cartes
```

Le fond est rendu dans `HomeScreen` a la ligne ~292, avant le backdrop et le contenu.

---

## 8. Fichiers references

| Fichier | Role |
|---------|------|
| `ui/home/compose/HomeScreen.kt` L355-474 | `DarkGridNoiseBackground()` — implementation principale |
| `ui/shared/components/VegafoXScaffold.kt` | Scaffold parent (Row avec sidebar + content Box) |
| `ui/home/compose/HomeHeroBackdrop.kt` | Couche au-dessus du fond (backdrop + blur API 31+) |
| `ui/base/theme/VegafoXColors.kt` | `OrangePrimary=#FF6B00`, `Background=#0A0A0F` |

---

## 9. Conclusion

L'aspect "low quality" du quadrillage n'est **pas** cause par un probleme de `FilterQuality` sur bitmap (il n'y a pas de bitmap). Il est cause par :

1. **Bruit trop clairseme** (800 points sur ~2M pixels = 0.04% couverture)
2. **Lignes sub-pixel** (0.5dp = antialiasing GPU degrade)
3. **Opacites trop basses** (3% grille, 2.5% bruit = quasi-invisible)

Le fond donne une impression de "presque rien" plutot que d'un motif graphique intentionnel et qualitatif. Les fixes R1 (densite bruit x5) et R2 (epaisseur ligne x2) suffiraient a ameliorer significativement le rendu sans changer l'architecture.
