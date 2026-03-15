# H4 — Fond Panoramique Immersif + Animation Stagger

**Date** : 2026-03-10
**Statut** : Termine — BUILD SUCCESSFUL debug + release, installe sur AM9 Pro

---

## Resume

Remplacement du fond uni `Background` par un fond atmospherique immersif sur l'ecran d'accueil.
Le fond est un `Canvas` plein ecran qui dessine un gradient directionnel, trois gradients radiaux
et des scanlines subtiles. Le backdrop Coil existant reste par-dessus avec alpha reduit a 0.35.
Animation d'entree stagger ajoutee sur les rows au premier chargement.

---

## Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `app/.../ui/home/compose/HomeScreen.kt` | Ajout composable `PanoramicBackground`, insere comme layer 0 dans le Box, `staggerEntrance = true` sur TvRowList |
| `app/.../ui/home/compose/HomeHeroBackdrop.kt` | `BACKDROP_ALPHA` : 0.40 → 0.35 |
| `app/.../ui/base/tv/TvRowList.kt` | Ajout parametre `staggerEntrance`, restructuration 3 items → 1 item par row, animation translateY + alpha avec stagger |

---

## PanoramicBackground — Fond atmospherique fixe

### Gradient directionnel 160°

Direction CSS 160° (de haut-gauche vers bas-droite, principalement vertical avec leger decalage vers la droite).

| Stop | Couleur | Hex |
|------|---------|-----|
| 0% | Bleu marine profond | `#0D1A35` |
| 35% | Violet sombre | `#150A28` |
| 60% | Bleu nuit | `#0A0F1F` |
| 100% | Background VegafoX | `#0A0A0F` |

Calcul des offsets start/end via trigonometrie :
- `dx = sin(160°)` ≈ 0.342
- `dy = -cos(160°)` ≈ 0.940
- Longueur gradient = `|W × dx| + |H × dy|`
- Start = centre - (longueur/2) × direction
- End = centre + (longueur/2) × direction

### Gradients radiaux (par-dessus le gradient directionnel)

| Gradient | Centre | Rayon | Couleur | Core solide | Fondu |
|----------|--------|-------|---------|-------------|-------|
| Bleu marine | 65%, 40% | 35% largeur | `rgba(20,50,120,0.35)` = `#59143278` | 0–30% | 30–100% |
| Violet | 70%, 20% | 20% largeur | `rgba(80,20,120,0.2)` = `#33501478` | 0–40% | 40–100% |
| Orange | 20%, 80% | 15% largeur | `rgba(255,107,0,0.05)` = `#0DFF6B00` | 0–40% | 40–100% |

Utilisation de `Brush.radialGradient` circulaire (Compose ne supporte pas les gradients elliptiques
natifs). L'effet visuel est suffisamment proche pour un fond atmospherique decoratif.

### Scanlines

- Espace entre bandes : 4dp
- Couleur : `rgba(255,255,255,0.008)` = `#02FFFFFF`
- Seules les bandes paires sont dessinees (alternance visible/invisible)
- Effet : texture CRT tres subtile, quasi invisible mais ajoute de la profondeur

### Proprietes

- **Fixe** : le fond ne reagit pas a l'item focuse
- **Pas de recomposition** : le Canvas ne depend d'aucun state variable
- **Performance** : ~52 drawRect pour les scanlines sur ecran 1080p (negligeable)

---

## Backdrop Coil — Alpha reduit

| Propriete | Avant | Apres |
|-----------|-------|-------|
| `BACKDROP_ALPHA` | 0.40 | 0.35 |

Le backdrop dynamique (image serveur de l'item focuse) reste par-dessus le fond panoramique
mais avec une opacite legerement reduite pour laisser transparaitre le fond atmospherique.

---

## Ordre des layers

```
Box (fillMaxSize) {
    PanoramicBackground()            // z=0 : fond fixe (Canvas)
    HomeHeroBackdrop(focusedItem)    // z=1 : image dynamique (Coil, alpha 0.35, blur 8px)
    Column {                         // z=2 : contenu interactif
        MainToolbar(Home)
        Box(weight=0.45) { HeroInfoOverlay + HeroPaginationDots }
        StateContainer(weight=0.55) { TvRowList(staggerEntrance=true) }
    }
}
```

---

## Animation stagger des rows

### Comportement

Au premier chargement de l'ecran Home, chaque row apparait avec une animation d'entree :
- **translateY** : +30dp → 0dp
- **alpha** : 0 → 1
- **Duree** : 400ms par row
- **Easing** : EaseOutCubic (0.33, 1, 0.68, 1)
- **Stagger** : 80ms de delai entre chaque row

L'animation ne se rejoue pas quand l'utilisateur revient sur Home apres navigation
(`rememberSaveable` persiste l'etat `entrancePlayed`).

### Implementation dans TvRowList

Parametre `staggerEntrance: Boolean = false` (opt-in).

Restructuration interne de LazyColumn :
- **Avant** : 3 items par row (header, content, spacer) avec cles `"${key}_header"`, `key`, `"${key}_spacer"`
- **Apres** : 1 item par row (Column contenant header + LazyRow + Spacer) avec cle `key`

Avantage : l'animation `graphicsLayer { alpha, translationY }` s'applique a l'ensemble
de la row (titre + contenu + espacement) comme une unite.

### Constantes

```kotlin
private val EntranceEasing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val STAGGER_DELAY_MS = 80L
private const val ENTRANCE_DURATION_MS = 400
private const val ENTRANCE_OFFSET_DP = 30f
```

### Compatibilite

Le parametre `staggerEntrance` a une valeur par defaut `false`. Les 5 autres ecrans
qui utilisent TvRowList (ByLetterBrowseScreen, CollectionBrowseScreen, SuggestedMoviesScreen,
AllFavoritesScreen, FolderViewScreen) ne sont pas affectes.

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
./gradlew :app:assembleGithubRelease      → BUILD SUCCESSFUL
```

## Installation

```
adb install debug   → Success
adb install release → Success
```

0 erreur, 0 warning.
