# Cosmetic: Hero Trailer — Fondu Premium

## Objectif

Intégrer le trailer vidéo dans le backdrop hero de manière organique,
sans bordure visible, sans bande noire, avec un fondu progressif sur
tous les bords.

## Fichiers modifiés

- `ExoPlayerTrailerView.kt` — TextureView (remplace PlayerView/SurfaceView), center-crop, son activé
- `HomeHeroBackdrop.kt` — gradients DstIn larges (55%/40%/45%/25%), CompositingStrategy.Offscreen
- `HomeViewModel.kt` — suppression isMuted/toggleMute, son toujours activé
- `HomeScreen.kt` — suppression composable MuteBadge

## Changements

### ExoPlayerTrailerView — TextureView

Le remplacement clé : `PlayerView` (basé sur `SurfaceView`) → `TextureView` direct.

**Pourquoi** : `SurfaceView` crée une couche window séparée toujours opaque/noire.
Même avec `setBackgroundColor(TRANSPARENT)`, le surface layer reste noir.
`TextureView` se composite normalement dans la hiérarchie de vues,
supportant la transparence et les blendModes.

| Propriété          | Avant (PlayerView)              | Après (TextureView)            |
|-------------------|--------------------------------|--------------------------------|
| Surface type      | SurfaceView (opaque)           | TextureView (transparent)      |
| Resize mode       | RESIZE_MODE_ZOOM via PlayerView | center-crop Matrix transform   |
| `muted` default   | `true`                         | `false`                        |
| Background        | BLACK (even with TRANSPARENT)  | véritablement transparent      |

**Center-crop** via `Matrix.setScale()` : calcule le ratio vidéo vs container,
applique un scale uniforme centré (max des deux axes), identique à RESIZE_MODE_ZOOM.

### HomeHeroBackdrop — Gradients DstIn

Container 380dp × 214dp, `Alignment.TopEnd`, aucun padding.

`CompositingStrategy.Offscreen` isole le buffer, puis `drawWithContent`
applique 4 masques `BlendMode.DstIn` :

| Bord    | Direction                              | Zone masquée |
|---------|----------------------------------------|-------------|
| Gauche  | `horizontalGradient(Transparent→Black)` | 55% largeur |
| Haut    | `verticalGradient(Transparent→Black)`   | 40% hauteur |
| Bas     | `verticalGradient(Black→Transparent)`   | 45% hauteur |
| Droite  | `horizontalGradient(Black→Transparent)` | 25% largeur |

Ces gradients larges font que la vidéo apparaît comme une extension
naturelle du backdrop, sans aucun bord visible.

### HomeViewModel — Son

- Champ `isMuted` supprimé de `TrailerState`
- Fonction `toggleMute()` supprimée
- Son toujours activé (volume 1f)

### HomeScreen

- Composable `MuteBadge` supprimé
- Imports nettoyés

## Screenshot

![Trailer fondu premium](trailer_v2.png)

## Build

- Debug GitHub : OK
- Release GitHub : OK
- Installé sur AM9 Pro (192.168.1.152)
