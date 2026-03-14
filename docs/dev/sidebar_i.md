# SIDEBAR_I — Suppression rebond + item Accueil + sélection par défaut

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release — Installé AM9 Pro

---

## Objectif

Trois corrections ciblées sur la sidebar premium :
1. Supprimer l'effet rebond des animations spring (ouverture/fermeture)
2. Ajouter un item Accueil en première position
3. Initialiser la sélection par défaut sur Accueil (index 0)

---

## Correction 1 — Suppression de l'effet rebond

### Problème

L'animation `spring(DampingRatioMediumBouncy, StiffnessMedium)` provoquait un rebond trop prononcé à l'ouverture et à la fermeture de la sidebar. L'effet n'était pas assez premium.

### Solution

Remplacement des 3 `spring()` par `tween(250, easing = EaseOutCubic)` :

| Animation | Avant | Après |
|-----------|-------|-------|
| `sidebarWidth` | `spring(MediumBouncy, Medium)` | `tween(250, EaseOutCubic)` |
| `indicatorWidth` | `spring(MediumBouncy, Medium)` | `tween(250, EaseOutCubic)` |
| `indicatorY` | `spring(MediumBouncy, Medium)` | `tween(250, EaseOutCubic)` |

`EaseOutCubic` défini comme `CubicBezierEasing(0.33f, 1f, 0.68f, 1f)` — identique à celui utilisé dans `NavItem.kt`.

Imports nettoyés : `Spring` et `spring` supprimés, `CubicBezierEasing` ajouté.

---

## Correction 2 — Item Accueil ajouté

### Changement

Nouvel item `NavItem` ajouté en toute première position (index 0) :

```kotlin
NavItem(
    icon = VegafoXIcons.Home,
    label = "Accueil",
    isSelected = selectedIndex == 0,
    onSelect = { selectedIndex = 0 },
    ...
)
```

`VegafoXIcons.Home` existait déjà (`Icons.Default.Home`), aucun ajout nécessaire.

`NAV_ITEM_COUNT` mis à jour : 6 → 7 (pour inclure le shimmer du nouvel item).

### Tableau des items mis à jour

| Index | Icône | Label | Position |
|-------|-------|-------|----------|
| 0 | Home | Accueil | Top |
| 1 | Search | Recherche | Top |
| 2 | Movie | Films | Top |
| 3 | Tv | Séries | Top |
| 4 | LiveTv | Live TV | Top |
| 5 | VideoLibrary | Média | Top |
| 6 | Settings | Paramètres | Bottom |
| — | ProfileItem | (avatar) | Bottom |

---

## Correction 3 — Sélection par défaut cohérente

### Changement

```kotlin
// Avant
var selectedIndex by remember { mutableIntStateOf(-1) }

// Après
var selectedIndex by remember { mutableIntStateOf(0) }
```

Au démarrage du HomeScreen, l'item Accueil (index 0) est sélectionné par défaut. L'indicateur orange et le style visuel reflètent immédiatement cette sélection.

---

## Fichier modifié

| Fichier | Changements |
|---------|-------------|
| `ui/home/compose/sidebar/PremiumSideBar.kt` | 3× spring → tween EaseOutCubic, +NavItem Home index 0, réindexation 1→6, selectedIndex 0 par défaut, NAV_ITEM_COUNT 7, imports nettoyés |

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
