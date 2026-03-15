# SIDEBAR_C — Focus D-pad sur NavItem

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Objectif

Ajouter le support focus D-pad sur NavItem avec scale, glow, et transitions de couleur animées.

---

## Implémentation

### Focus detection

`MutableInteractionSource` + `collectIsFocusedAsState()` sur le Box icône, qui est `.focusable(interactionSource = interactionSource)`.

### États visuels (3 états)

| État | Icône | Fond cercle | Scale | Glow |
|------|-------|-------------|-------|------|
| Normal | 22dp `#5A5A6E` | Transparent | 1.0 | Non |
| Sélectionné | 22dp `OrangePrimary` | `OrangePrimary` alpha 0.14 | 1.0 | Non |
| Focalisé | 22dp `TextPrimary` (#F5F0EB) | `#1C1C22` alpha 0.9 | 1.10 | Oui |

Priorité : Focalisé > Sélectionné > Normal.

### Glow

`drawBehind` avec `drawCircle` + `Brush.radialGradient` :
- Couleur : `OrangePrimary` alpha 0.25
- Rayon : `size.maxDimension * 0.8f`
- Apparaît/disparaît via `glowAlpha` animé

### Animations

| Propriété | Type | Durée | Easing |
|-----------|------|-------|--------|
| `iconColor` | `animateColorAsState` | 140ms | EaseOutCubic |
| `circleBg` | `animateColorAsState` | 140ms | EaseOutCubic |
| `scale` | `animateFloatAsState` | 140ms | EaseOutCubic |
| `glowAlpha` | `animateFloatAsState` | 140ms | EaseOutCubic |

### Input D-pad

- `onKeyEvent` intercepte `Key.Enter` et `Key.DirectionCenter` (KeyUp) → `onSelect()`
- `clickable` pour compatibilité souris/touch → `onSelect()`

---

## Constantes ajoutées

| Constante | Valeur |
|-----------|--------|
| `FocusedBackground` | `#1C1C22` alpha 0.9 |
| `GlowColor` | `OrangePrimary` alpha 0.25 |
| `EaseOutCubic` | `CubicBezierEasing(0.33, 1, 0.68, 1)` |
| `ANIM_MS` | 140 |

---

## Fichier modifié

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/NavItem.kt` | Ajout focus D-pad, animations couleur/scale/glow, input key + clickable |

## Fichier non modifié

| Fichier | Raison |
|---------|--------|
| `PremiumSideBar.kt` | Inchangé — utilise déjà NavItem depuis sidebar_b |

---

## Non implémenté (phases suivantes)

- Tooltip (label affiché au focus)

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```
