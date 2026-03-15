# H Fix 9 — Hero content remonté + Synopsis multi-lignes

## Corrections appliquées

### CORRECTION 1 — Remonter le contenu hero

**Problème** : le HeroInfoOverlay était aligné en bas du Box hero
(`Alignment.BottomStart`), le contenu (titre, pills, synopsis, boutons)
se retrouvait trop bas, collé aux rows.

**Solution appliquée** :

1. **HomeScreen.kt** — Alignement du HeroInfoOverlay changé de
   `Alignment.BottomStart` vers `Alignment.CenterStart`. Suppression du
   `bottom = 16.dp` du padding (inutile avec CenterStart).

2. **HomeHeroBackdrop.kt** — Column de `HeroInfoContent` reçoit
   `.fillMaxHeight()` et un `Spacer(Modifier.fillMaxHeight(0.20f))`
   en tête. Le contenu démarre à ~20% de la hauteur du Box hero,
   soit environ 40% depuis le haut de l'écran (le Box hero occupe
   56% de la hauteur totale, centré à mi-hauteur).

### CORRECTION 2 — Synopsis sur plusieurs lignes

**Problème** : le Text du synopsis était limité à `maxLines = 1` avec
`fontSize = 14.sp`, tronquant les descriptions.

**Solution appliquée** :

- `maxLines` : 1 → **4**
- `fontSize` : 14.sp → **13.sp**
- `lineHeight` : 20.sp → **18.sp** (adapté à la taille réduite)

## Fichiers modifiés

| Fichier | Modifications |
|---------|--------------|
| `HomeScreen.kt` | Alignement hero BottomStart → CenterStart ; suppression bottom padding ; contentPadding start 16→32dp (anti-troncature vignette) |
| `HomeHeroBackdrop.kt` | +import fillMaxHeight ; Column +fillMaxHeight() ; +Spacer 20% en tête ; synopsis maxLines 1→4, fontSize 14→13sp, lineHeight 20→18sp |
| `TvRowList.kt` | LazyRow +contentPadding horizontal spaceSm (8dp) pour éviter la troncature de la première/dernière carte au focus |

## Build & Install

- `assembleDebug` : BUILD SUCCESSFUL
- `assembleRelease` : BUILD SUCCESSFUL
- Debug installé sur AM9 Pro (192.168.1.152)
- Release installé sur AM9 Pro (192.168.1.152)
