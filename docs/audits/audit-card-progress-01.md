# Audit — Barre de progression sur les cartes Home

**Date** : 2026-03-17
**Scope** : `BrowseMediaCard.kt`, `MediaPosterCard.kt`
**Contexte** : post cosmetic-home-05 (ajout relief 3D sur les cartes)

---

## 1. BrowseMediaCard — Barre de progression

### Implémentation (lignes 241-261)

La barre existe et s'affiche conditionnellement quand `item.userData?.playedPercentage > 0`.

**Structure :**

```
Box (track) — Alignment.BottomCenter, fillMaxWidth, padding(bottom=3dp), height=2dp
  └─ Box (fill) — fillMaxHeight, fillMaxWidth(fraction=percentage/100), pas d'alignement start explicite
```

### Propriétés exactes

| Propriete           | Valeur                                        |
|---------------------|-----------------------------------------------|
| Position            | `Alignment.BottomCenter` dans le Box parent   |
| Padding bottom      | `3.dp` depuis le bord inferieur               |
| Hauteur             | `2.dp`                                        |
| Largeur             | `fillMaxWidth()` (track = toute la largeur)   |
| Couleur track       | `VegafoXColors.Divider` = `0x0FFFFFFF` (6% blanc) |
| Couleur fill        | `VegafoXColors.BlueAccent` = `0xFF4FC3F7`     |
| Shape               | Rectangle (aucun `RoundedCornerShape` applique) |
| Animation au focus  | **Aucune** — pas de `animateFloatAsState` ni transition |
| Alignement fill     | **Implicite start** (par defaut dans un Box, le fill s'aligne en haut-gauche) |

### Dimensions de la carte

- Largeur par defaut : `CardDimensions.landscapeWidth` = **220.dp**
- Hauteur image : `220.dp * 9/16` = **123.75.dp** (~124dp)
- Ratio : 16:9 paysage
- Shape du clip : `JellyfinTheme.shapes.medium` = `RoundedCornerShape(12.dp)`

### Z-order dans le Box parent (de bas en haut)

```
1. CachedAsyncImage (ou placeholder icon)
2. Relief: left edge reflection    — Box fillMaxSize, horizontalGradient PosterReflectLight 0→15%
3. Relief: right edge shadow       — Box fillMaxSize, horizontalGradient PosterShadowDark 80→100%
4. Progress bar (track + fill)     — Box BottomCenter, height=2dp, padding-bottom=3dp
5. Badge "NEW"                     — Box TopEnd, padding=4dp
6. Play overlay (AnimatedVisibility) — Center, apparait apres 5s de focus
```

### Interaction visuelle avec le relief

- La barre est **au-dessus** des deux couches de relief (reflection gauche et shadow droite)
- La barre a un `padding(bottom = 3.dp)` → elle flotte a 3dp du bord inferieur
- Le Box parent est clippe avec `RoundedCornerShape(12.dp)` → les coins arrondis clippent naturellement la barre si elle depasse dans les coins
- **Probleme potentiel** : la couche "right edge shadow" (`PosterShadowDark`, 25% noir) assombrit visuellement le coin inferieur droit. La portion droite du track (6% blanc) est deja quasi-invisible, et sous le shadow elle devient totalement invisible. Le fill `BlueAccent` reste visible car c'est une couleur opaque pleine.
- La couche "left edge reflection" (`PosterReflectLight`, 12% blanc) est transparente et n'impacte pas la visibilite de la barre
- Le `graphicsLayer { shadowElevation }` applique une ombre portee **sous** le Box, donc n'affecte pas le rendu interne de la barre
- Le `border` (1dp, linearGradient PosterBorderLight→PosterBorderDark) est applique **avant** le clip dans la chaine de modifiers → il encadre tout le Box sans interagir avec la barre interieure

### Animation du relief vs barre

Le relief anime au focus :
- `shadowElevation` : 16dp → 28dp (200ms EaseOutCubic)
- `translationY` : 0f → -3f pixels (200ms EaseOutCubic)

La barre de progression **ne reagit pas** au focus. Elle reste statique (hauteur, couleur, opacite inchangees). La carte entiere monte de 3px au focus via `translationY`, ce qui deplace la barre avec le reste du contenu.

---

## 2. MediaPosterCard — Pas de barre de progression

`MediaPosterCard` n'a **aucune barre de progression**. Le composable ne recoit pas `item.userData` ni `playedPercentage` en parametre. Il affiche uniquement :
- Image poster (ou placeholder icon)
- Couches de relief (reflection + shadow)
- Titre
- Subtitle optionnel

Aucun mecanisme n'existe pour afficher l'avancement de lecture sur ce type de carte.

---

## 3. Resume des constats

| Constat | Severite | Detail |
|---------|----------|--------|
| Track quasi-invisible sous le shadow droit | Cosmetique mineur | Le track `Divider` (6% blanc) disparait visuellement sous `PosterShadowDark` (25% noir) dans le quart droit de la carte |
| Barre rectangulaire sans coins arrondis | Cosmetique mineur | Les autres barres de progression dans l'app (detail, player) utilisent des coins arrondis ; ici c'est un rectangle brut |
| Pas d'animation au focus | Design decision | La barre ne change pas d'apparence au focus, contrairement au shadow/elevation qui animent |
| Pas de progress sur MediaPosterCard | Design decision | Les cartes poster (dossiers, musique) n'affichent jamais l'avancement |
| Fill aligne implicitement a gauche | OK | Le Box par defaut aligne les enfants TopStart, donc le fill part bien de la gauche — correct |

---

## 4. Tokens de couleur references

```kotlin
VegafoXColors.Divider          = Color(0x0FFFFFFF)  // 6% blanc — track
VegafoXColors.BlueAccent       = Color(0xFF4FC3F7)  // bleu ciel opaque — fill
VegafoXColors.PosterReflectLight = Color(0x1FFFFFFF) // 12% blanc — reflet gauche
VegafoXColors.PosterShadowDark  = Color(0x40000000)  // 25% noir — ombre droite
VegafoXColors.PosterBorderLight = Color(0x40FFFFFF)  // 25% blanc — bordure haut-gauche
VegafoXColors.PosterBorderDark  = Color(0x0AFFFFFF)  // 4% blanc — bordure bas-droite
```
