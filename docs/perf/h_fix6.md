# H_FIX6 — Dark Grid Noise Background

**Date** : 2026-03-11
**Statut** : Termine — BUILD SUCCESSFUL debug + release

---

## Resume

Remplacement du fond `PanoramicBackground` (gradients directionnels + scanlines) par un nouveau fond
"Dark Grid Noise" style dashboard trading/terminal. Le fond est un Canvas 5 couches : base solide,
grain fractal, grille geometrique, glow radial elliptique, vignette bords. Le backdrop Coil existant
reste par-dessus avec alpha reduit a 0.30.

---

## Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `app/.../ui/home/compose/HomeScreen.kt` | Remplacement `PanoramicBackground` par `DarkGridNoiseBackground` (5 couches Canvas) |
| `app/.../ui/home/compose/HomeHeroBackdrop.kt` | `BACKDROP_ALPHA` : 0.35 → 0.30 |

---

## DarkGridNoiseBackground — 5 couches Canvas

### Couche 1 — Fond de base

Couleur unie `#060A0F` (bleu tres sombre, plus profond que `VegafoXColors.BackgroundDeep`).

### Couche 2 — Grain fractal noise

800 petits `drawRect` aleatoires disperses sur toute la surface.

| Propriete | Valeur |
|-----------|--------|
| Seed | 42 (fixe, pattern stable entre recompositions) |
| Nombre de points | 800 |
| Taille de chaque point | 1.5dp |
| Couleur | Blanc, alpha aleatoire entre 0 et 0.025 |

Le pattern est deterministe grace au seed fixe — pas besoin de `remember`.

### Couche 3 — Grille geometrique

Lignes horizontales et verticales formant une grille reguliere.

| Propriete | Valeur |
|-----------|--------|
| Espacement | 60dp |
| Couleur | `VegafoXColors.OrangePrimary` alpha 0.03 |
| Epaisseur | 0.5dp |

Tres subtil, quasi invisible mais perceptible inconsciemment.

### Couche 4 — Degrade radial central (elliptique)

Glow central elliptique simule via `nativeCanvas.scale()`.

| Propriete | Valeur |
|-----------|--------|
| Centre | milieu de l'ecran |
| Forme | Ellipse 70% largeur x 50% hauteur |
| Couleur | `#0A1525` (bleu marine) → transparent |
| Alpha global | 0.6 |

Implementation : gradient radial circulaire (rayon = 35% largeur) + scale Y via
`drawContext.canvas.nativeCanvas.scale(1f, scaleY, cx, cy)` pour deformer en ellipse.

### Couche 5 — Vignette bords

4 rectangles de gradient (gauche, droite, haut, bas) qui assombrissent les bords.

| Propriete | Valeur |
|-----------|--------|
| Profondeur | 15% de chaque dimension |
| Couleur | Noir alpha 0.7 → transparent |
| Effet | L'ecran s'evanouit vers les bords |

---

## Constantes

```kotlin
private val GridBaseColor = Color(0xFF060A0F)
private val RadialCenterColor = Color(0xFF0A1525)
private const val NOISE_SEED = 42
private const val NOISE_POINT_COUNT = 800
```

---

## Backdrop Coil — Alpha reduit

| Propriete | Avant | Apres |
|-----------|-------|-------|
| `BACKDROP_ALPHA` | 0.35 | 0.30 |

Le backdrop dynamique reste par-dessus le fond mais avec opacite reduite
pour laisser mieux voir la grille en dessous.

---

## Ordre des layers

```
Box (fillMaxSize) {
    DarkGridNoiseBackground()        // z=0 : fond fixe (Canvas 5 couches)
    HomeHeroBackdrop(focusedItem)    // z=1 : image dynamique (Coil, alpha 0.30, blur 8px)
    Column {                         // z=2 : contenu interactif
        MainToolbar(Home)
        Box(weight=0.52) { HeroInfoOverlay + HeroPaginationDots }
        StateContainer(weight=0.48) { TvRowList(staggerEntrance=true) }
    }
}
```

---

## Performance

- **Canvas sans state** : aucun state reactive dans le drawScope, pas de recomposition
- **800 drawRect** : negligeable (chaque point est un micro-rect de 1.5dp)
- **~36 drawLine** : grille 60dp sur ecran 1080p (~18 verticales + ~18 horizontales)
- **1 nativeCanvas save/scale/restore** : operation native triviale
- **4 drawRect gradient** : vignette bords (standard)

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```

0 erreur, 0 warning.
