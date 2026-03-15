# SIDEBAR_B — NavItem avec états selected/unselected

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Objectif

Créer le composable `NavItem` et remplacer les 7 Box gris par les vrais items de navigation avec icônes VegafoXIcons.

---

## NavItem — Spécifications

### Paramètres

| Paramètre | Type | Description |
|-----------|------|-------------|
| `icon` | `ImageVector` | Icône VegafoXIcons |
| `label` | `String` | Texte descriptif (contentDescription) |
| `isSelected` | `Boolean` | État sélectionné |
| `onSelect` | `() -> Unit` | Callback de sélection |

### Layout

```
Row(72dp × 52dp, verticalAlignment = CenterVertically) {
    if (isSelected) Box(3dp × 20dp, rounded 2dp, OrangePrimary)
    else            Spacer(3dp)
    Box(weight=1f, center) {
        Box(44dp circle if selected, bg OrangePrimary@14%) {
            Icon(22dp)
        }
    }
}
```

### États visuels

| État | Icône | Fond | Indicateur |
|------|-------|------|------------|
| Normal | 22dp `#5A5A6E` | Transparent | Spacer 3dp |
| Sélectionné | 22dp `OrangePrimary` | `OrangePrimary` alpha 0.14, CircleShape | Rectangle 3×20dp arrondi 2dp `OrangePrimary` |

### Constantes (NavItem.kt)

| Constante | Valeur |
|-----------|--------|
| `IconSize` | 22dp |
| `IconCircleSize` | 44dp |
| `IndicatorWidth` | 3dp |
| `IndicatorHeight` | 20dp |
| `IndicatorShape` | RoundedCornerShape(2dp) |
| `NormalIconColor` | `#5A5A6E` |
| `SelectedBackground` | OrangePrimary alpha 0.14 |

---

## Items de la sidebar

| Index | Icône | Label | Position |
|-------|-------|-------|----------|
| 0 | `VegafoXIcons.Search` | Recherche | Top |
| 1 | `VegafoXIcons.Movie` | Films | Top |
| 2 | `VegafoXIcons.Tv` | Séries | Top |
| 3 | `VegafoXIcons.LiveTv` | Live TV | Top |
| 4 | `VegafoXIcons.VideoLibrary` | Médiathèque | Top |
| 5 | `VegafoXIcons.Settings` | Paramètres | Bottom |
| 6 | `VegafoXIcons.Person` | Profil | Bottom |

État sélectionné : `mutableIntStateOf(-1)` (rien sélectionné par défaut).

---

## Fichiers créés

| Fichier | Description |
|---------|-------------|
| `ui/home/compose/sidebar/NavItem.kt` | Composable NavItem — icône + indicateur gauche, deux états visuels |

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/PremiumSideBar.kt` | 7 Box gris → 7 NavItem avec VegafoXIcons et selectedIndex local |

---

## Non implémenté (phases suivantes)

- Focus D-pad
- Animations (couleur, scale)
- Tooltip
- Navigation réelle (Destination)

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```
