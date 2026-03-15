# SIDEBAR_G — Expansion YouTube TV style

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release — Installé AM9 Pro

---

## Objectif

Remplacer le système de tooltip flottant (Box avec zIndex) par une expansion de la sidebar elle-même quand le focus D-pad y entre, à la manière de YouTube TV / Google TV.

---

## 1. Expansion / rétraction

### Comportement

Quand le focus D-pad entre dans la sidebar, elle s'élargit de 72dp à 220dp, révélant les labels texte à droite de chaque icône. Quand le focus quitte la sidebar, elle se rétracte après un délai de 200ms.

| Propriété | Valeur |
|-----------|--------|
| Largeur rétractée | 72dp (`PREMIUM_SIDEBAR_WIDTH_COLLAPSED`) |
| Largeur étendue | 220dp (`PREMIUM_SIDEBAR_WIDTH_EXPANDED`) |
| Animation | `animateDpAsState` spring |
| Spring damping | `Spring.DampingRatioMediumBouncy` |
| Spring stiffness | `Spring.StiffnessMedium` |
| Délai rétraction | 200ms (évite clignotement entre items) |
| Déclencheur | `anyItemFocused` (focusCount > 0) |

### Contenu principal

Le contenu principal dans `HomeScreen.kt` utilise déjà `Modifier.weight(1f)` — il se décale naturellement quand la sidebar s'élargit, sans redimensionnement des cards.

---

## 2. Labels inline

### Avant

Chaque `NavItem` affichait un tooltip flottant via `AnimatedVisibility` + `Box` avec `zIndex(10f)` et `offset(x = 80.dp)`.

### Après

Chaque `NavItem` affiche un label inline via `AnimatedVisibility(fadeIn 150ms)` dans un `Row` à droite de l'icône, déclenché quand `isExpanded = true`.

| Propriété | Valeur |
|-----------|--------|
| Espacement icône–label | 16dp |
| Taille texte | 14sp |
| Poids selected/focused | `FontWeight.Medium` |
| Poids inactif | `FontWeight.Normal` |
| Couleur selected/focused | `VegafoXColors.TextPrimary` |
| Couleur inactif | `#7A7A8E` |
| Animation apparition | `fadeIn(tween(150ms))` |
| Animation disparition | `fadeOut(tween(150ms))` |

### Labels

| Index | Icône | Label |
|-------|-------|-------|
| 0 | Search | Recherche |
| 1 | Movie | Films |
| 2 | Tv | Séries |
| 3 | LiveTv | Live TV |
| 4 | VideoLibrary | Médiathèque |
| 5 | Settings | Paramètres |

---

## 3. Profil étendu

### Avant

Le `ProfileItem` affichait uniquement l'avatar circulaire.

### Après

En mode étendu, le `ProfileItem` affiche à droite de l'avatar :
- **Nom utilisateur** : 14sp Medium, TextPrimary
- **"Changer de profil"** : 11sp Normal, #7A7A8E

---

## 4. Fond et ombre

| Propriété | Rétracté | Étendu |
|-----------|----------|--------|
| Background alpha | 0.95 | 1.0 |
| Shadow elevation | aucune | 8dp |
| Transition alpha | `animateFloatAsState` tween 200ms | |

---

## 5. Indicateur

| Propriété | Rétracté | Étendu |
|-----------|----------|--------|
| Largeur | 3dp | 4dp |
| Hauteur | 20dp | 20dp |
| Animation | `animateDpAsState` spring MediumBouncy/Medium | |

---

## 6. Nettoyage

### Supprimé dans NavItem.kt

- Tooltip `AnimatedVisibility` avec `Box(zIndex(10f), offset(80dp))`
- Constantes `TooltipBackground`, `TooltipShape`, `TOOLTIP_ANIM_MS`
- Import `androidx.compose.ui.zIndex`

### Supprimé dans PremiumSideBar.kt

- `zIndex(1f)` sur le `Row` racine
- Import `androidx.compose.ui.zIndex`
- Constante `PROFILE_FOCUS_INDEX` (non utilisée)

### Ajouté

- `PREMIUM_SIDEBAR_WIDTH_COLLAPSED` (72dp) — remplace `PREMIUM_SIDEBAR_WIDTH`
- `PREMIUM_SIDEBAR_WIDTH_EXPANDED` (220dp, privé)
- `PREMIUM_SIDEBAR_WIDTH` conservé avec `@Deprecated` pour compatibilité

---

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/NavItem.kt` | Suppression tooltip flottant ; ajout `isExpanded` param ; label inline via Row + AnimatedVisibility |
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Suppression zIndex ; ajout expand/collapse animé (72↔220dp spring) ; fond alpha animé ; shadow 8dp ; indicateur 3↔4dp ; passage `isExpanded` aux items |
| `ui/home/compose/sidebar/ProfileItem.kt` | Ajout `isExpanded` param ; affichage nom utilisateur + "Changer de profil" en mode étendu |

---

## Screenshots AM9 Pro

| État | Fichier |
|------|---------|
| Sidebar rétractée (icônes seuls) | `docs/screenshots/sidebar_g_closed.png` |
| Sidebar étendue (focus D-pad, labels visibles) | `docs/screenshots/sidebar_g_open.png` |

---

## Build & Install

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
./gradlew :app:assembleGithubRelease      → BUILD SUCCESSFUL
adb install debug   → Success (AM9 Pro 192.168.1.152)
adb install release → Success (AM9 Pro 192.168.1.152)
```
