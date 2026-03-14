# SIDEBAR_F — 3 animations premium

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release — Installé AM9 Pro

---

## Objectif

Ajouter 3 animations premium à la sidebar : indicateur global animé, shimmer au repos, entrée slide+fade.

---

## 1. Animation indicateur global

### Avant

Chaque `NavItem` contenait son propre indicateur orange (3dp × 20dp) via un `if (isSelected)` dans un `Row`.

### Après

Un seul `Box` indicateur orange dans le `Box` parent de la `Column`. Sa position Y est calculée via `onGloballyPositioned` sur chaque `NavItem`, et animée avec `animateDpAsState` + `spring`.

| Propriété | Valeur |
|-----------|--------|
| Taille | 3.dp × 20.dp |
| Forme | RoundedCornerShape(2.dp) |
| Couleur | VegafoXColors.OrangePrimary |
| Animation | `animateDpAsState` spring |
| Spring damping | `Spring.DampingRatioMediumBouncy` |
| Spring stiffness | `Spring.StiffnessMedium` |
| Position tracking | `onGloballyPositioned` + `positionInParent().y` |

### Changement NavItem

- Suppression du `Row` avec indicateur/spacer individuel
- Layout simplifié : `Box(72dp × 52dp, centered)` → cercle icône

---

## 2. Shimmer au repos

### Comportement

Quand aucun item de la sidebar n'est focalisé, les icônes inactives (ni selected ni focused) pulsent entre opacité 0.40 et 0.55, chaque item décalé de 300ms.

| Propriété | Valeur |
|-----------|--------|
| Plage opacité | 0.40 – 0.55 |
| Durée cycle | 1500ms (aller) + 1500ms (retour) = 3000ms |
| Easing | `FastOutSlowInEasing` |
| Décalage par item | 300ms (`StartOffset`) |
| Mode | `infiniteRepeatable` / `RepeatMode.Reverse` |
| Nombre d'animations | 6 (une par NavItem) |
| Transition on/off | `animateFloatAsState` 500ms (`shimmerStrength`) |
| Blend | `lerp(1f, shimmerRaw, shimmerStrength)` |

### Contrôle focus

- `focusCount` (Int) incrémenté/décrémenté par callbacks `onFocusChanged` de chaque NavItem + ProfileItem
- `anyItemFocused = focusCount > 0`
- Focus détecté → `shimmerActive = false` immédiatement
- Focus perdu → `delay(3000ms)` → `shimmerActive = true`
- Transition douce via `shimmerStrength` (0→1 ou 1→0 en 500ms)

### Application

- `NavItem` reçoit `shimmerAlpha: Float` (default 1f)
- Appliqué via `graphicsLayer { alpha = shimmerAlpha }` sur l'icône
- Seulement quand `!isFocused && !isSelected`

---

## 3. Entrée sidebar

### Comportement

Au premier affichage, la sidebar glisse depuis la gauche avec un fadeIn. Une seule fois.

| Propriété | Valeur |
|-----------|--------|
| Animation | `translationX` + `alpha` via `graphicsLayer` |
| Durée | 400ms |
| Spec | `tween(400)` |
| Direction | Gauche → droite (−width → 0) |
| Déclencheur | `LaunchedEffect(Unit) { hasAppeared = true }` |
| Une seule fois | `remember { mutableStateOf(false) }` |

---

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/NavItem.kt` | Suppression indicateur individuel ; ajout `shimmerAlpha`, `onFocusChanged` ; layout simplifié (Box au lieu de Row) |
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Indicateur global animé spring ; shimmer InfiniteTransition staggered ; entrée slide+fade ; focus tracking |
| `ui/home/compose/sidebar/ProfileItem.kt` | Ajout `onFocusChanged` callback + `LaunchedEffect(isFocused)` |

---

## Screenshots AM9 Pro

| État | Fichier |
|------|---------|
| Normal (shimmer au repos) | `docs/screenshots/sidebar_f_normal.png` |
| Focus D-pad (indicateur + item focalisé) | `docs/screenshots/sidebar_f_focus.png` |

---

## Build & Install

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
adb install → Success (AM9 Pro 192.168.1.152)
```
