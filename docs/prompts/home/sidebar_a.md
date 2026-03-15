# SIDEBAR_A — Structure Column vide avec placeholders gris

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Objectif

Simplifier PremiumSideBar.kt en squelette structurel minimal :
Column 72dp + 7 Box gris #333333 de 52dp + séparateur droit 1dp.

---

## PremiumSideBar — Spécifications

### Layout

```
Row(fillMaxHeight) {
    Column(72dp, fillMaxHeight, bg=#060A0F) {
        Spacer 24dp
        Box #333333 52dp  ← placeholder 1
        Spacer 6dp
        Box #333333 52dp  ← placeholder 2
        Spacer 6dp
        Box #333333 52dp  ← placeholder 3
        Spacer 6dp
        Box #333333 52dp  ← placeholder 4
        Spacer 6dp
        Box #333333 52dp  ← placeholder 5
        Spacer weight(1f)
        Box #333333 52dp  ← placeholder 6 (settings)
        Spacer 16dp
        Box #333333 52dp  ← placeholder 7 (profile)
        Spacer 24dp
    }
    Box(1dp, fillMaxHeight, bg=OrangePrimary@0.10)  ← séparateur
}
```

### Constantes

| Constante | Valeur |
|-----------|--------|
| `PREMIUM_SIDEBAR_WIDTH` | 72dp |
| `SidebarBackground` | `#060A0F` |
| `SeparatorColor` | `OrangePrimary` alpha 0.10 |
| `PlaceholderColor` | `#333333` |
| `ITEM_SIZE` | 52dp |

---

## Fichier modifié

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Réécriture — 7 placeholders gris 52dp, suppression NavItemPlaceholder/ProfileItemPlaceholder |

## Fichier non modifié

| Fichier | Raison |
|---------|--------|
| `ui/home/compose/HomeScreen.kt` | Déjà en layout Row depuis sidebar_01, aucun changement requis |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```
