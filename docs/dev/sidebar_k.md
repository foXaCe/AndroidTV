# Sidebar K — Alignement icônes + Avatar profil réduit

## Corrections appliquées

### CORRECTION 1 — Alignement icônes sur axe vertical

**Problème** : avec `horizontalAlignment = CenterHorizontally` sur la Column
et des Row de largeurs différentes (labels de taille variable), les icônes
se décalaient horizontalement lors de l'expansion de la sidebar.

**Solution appliquée** :

1. **NavItem.kt** — Row racine utilise `.fillMaxWidth().height(52.dp)` au lieu
   de `.height(52.dp)` seul. Chaque item occupe toute la largeur de la sidebar,
   garantissant que le Box 72×52dp contenant l'icône est toujours collé à gauche.

2. **NavItem.kt** — L'indicateur orange (4×20dp, RoundedCornerShape 2dp) est
   désormais positionné **dans** le Box 72×52dp du NavItem via
   `Modifier.align(Alignment.CenterStart)`. Il se superpose au bord gauche
   sans affecter la position de l'icône centrée (Alignment.Center).

3. **ProfileItem.kt** — Même pattern : Row avec `.fillMaxWidth().height(52.dp)`,
   Box icône/avatar de 72×52dp avec avatar centré via `contentAlignment = Center`.

4. **PremiumSideBar.kt** — Suppression de l'indicateur global animé et de tout
   le tracking `onGloballyPositioned` / `itemCenterYPx` / `indicatorY`.
   Nettoyage des imports inutilisés (offset, RoundedCornerShape, clip,
   mutableStateMapOf, onGloballyPositioned, positionInParent).

### CORRECTION 2 — Avatar profil réduit

**Changements dans ProfileItem.kt** :
- `ProfileSize` : 52dp → **36dp**
- Bordure normale : 1.5dp → **1dp**
- Bordure focus : **2dp** OrangePrimary (inchangé)
- Glow : inchangé
- En mode étendu : nom + "Changer de profil" alignés verticalement sur le
  centre du cercle 36dp (via `verticalAlignment = CenterVertically` du Row)

## Fichiers modifiés

| Fichier | Modifications |
|---------|--------------|
| `sidebar/NavItem.kt` | +imports fillMaxWidth, RoundedCornerShape ; fillMaxWidth sur Row ; indicateur 4×20dp dans Box 72×52dp |
| `sidebar/ProfileItem.kt` | ProfileSize 36dp ; bordure 1dp ; +imports fillMaxWidth, height ; fillMaxWidth+height sur Row ; height sur Box icône |
| `sidebar/PremiumSideBar.kt` | Suppression indicateur global, onGloballyPositioned ×7, imports inutilisés, KDoc mis à jour |

### CORRECTION 3 — Gap invisible entre sidebar et contenu

**Problème** : `overscanPaddingValues = PaddingValues(48.dp, 27.dp)` conçu pour
un layout plein-écran sans sidebar. Avec la sidebar de 72dp, les 48dp de
padding gauche ajoutaient 121dp de zone morte avant le contenu visible (~13% écran).
`DarkGridNoiseBackground` ajoutait un vignettage gauche de 15% à 70% noir.

**Solution appliquée** :
- **HomeScreen.kt** : HeroInfoOverlay `start` padding : 48dp → **16dp**
- **HomeScreen.kt** : TvRowList `contentPadding.start` : `Tokens.Space.space3xl` (48dp) → **16dp**
- **HomeScreen.kt** : DarkGridNoiseBackground vignette gauche : 15% width à 70% noir → **5% width à 35% noir**

## Fichiers modifiés

| Fichier | Modifications |
|---------|--------------|
| `sidebar/NavItem.kt` | +imports fillMaxWidth, RoundedCornerShape ; fillMaxWidth sur Row ; indicateur 4×20dp dans Box 72×52dp |
| `sidebar/ProfileItem.kt` | ProfileSize 36dp ; bordure 1dp ; +imports fillMaxWidth, height ; fillMaxWidth+height sur Row ; height sur Box icône |
| `sidebar/PremiumSideBar.kt` | Suppression indicateur global, onGloballyPositioned ×7, imports inutilisés, KDoc mis à jour |
| `HomeScreen.kt` | Padding start hero+rows 48→16dp ; vignette gauche 15%/70% → 5%/35% |

## Build & Install

- `assembleDebug` : BUILD SUCCESSFUL
- `assembleRelease` : BUILD SUCCESSFUL
- Debug installé sur AM9 Pro (192.168.1.152)
- Release installé sur AM9 Pro (192.168.1.152)
