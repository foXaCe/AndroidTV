# SIDEBAR_H — Alignement vertical + renommage label

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release — Installé AM9 Pro

---

## Objectif

Deux corrections ciblées sur la sidebar premium :
1. Aligner verticalement le `ProfileItem` (avatar) sur le même axe que les `NavItem` (icônes)
2. Renommer le label "Médiathèque" en "Média"

---

## Correction 1 — Alignement vertical

### Problème

Les `NavItem` utilisent un conteneur `Box` de 72dp (`PREMIUM_SIDEBAR_WIDTH_COLLAPSED`) avec `contentAlignment = Alignment.Center` pour centrer l'icône (44dp) dans la largeur de la sidebar rétractée. Le `ProfileItem` n'avait pas ce conteneur — l'avatar (52dp) était directement dans le `Row`, décalé par rapport aux icônes.

### Vérifications

| Composant | Propriété | Valeur | Statut |
|-----------|-----------|--------|--------|
| `NavItem` Row | `verticalAlignment` | `CenterVertically` | ✓ déjà correct |
| `NavItem` icon Box | largeur | `PREMIUM_SIDEBAR_WIDTH_COLLAPSED` (72dp) | ✓ déjà correct |
| `NavItem` icon Box | sizing | `.width()` fixe (pas `weight(1f)`) | ✓ déjà correct |
| Indicateur orange | positionnement | overlay dans le `Box` parent (pas dans le flow) | ✓ pas de décalage |
| `ProfileItem` avatar | conteneur 72dp | **MANQUANT** | ✗ corrigé |

### Correction dans `ProfileItem.kt`

Enveloppé l'avatar `Box` dans un conteneur identique à `NavItem` :

```kotlin
// Avant
Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(ProfileSize)...) { /* avatar */ }
    // label
}

// Après
Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Box(
        modifier = Modifier.width(PREMIUM_SIDEBAR_WIDTH_COLLAPSED), // 72dp
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(ProfileSize)...) { /* avatar */ }
    }
    // label
}
```

L'avatar (52dp) est maintenant centré dans les 72dp, aligné sur le même axe X que les icônes (44dp centrées dans 72dp).

---

## Correction 2 — Label Médiathèque → Média

### Changement

Dans `PremiumSideBar.kt`, le `NavItem` VideoLibrary (index 4) :

```kotlin
// Avant
label = "Médiathèque",

// Après
label = "Média",
```

### Tableau des labels mis à jour

| Index | Icône | Label |
|-------|-------|-------|
| 0 | Search | Recherche |
| 1 | Movie | Films |
| 2 | Tv | Séries |
| 3 | LiveTv | Live TV |
| 4 | VideoLibrary | **Média** |
| 5 | Settings | Paramètres |

---

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/ProfileItem.kt` | Avatar enveloppé dans un Box de 72dp (`PREMIUM_SIDEBAR_WIDTH_COLLAPSED`) avec `contentAlignment = Center` |
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Label VideoLibrary : "Médiathèque" → "Média" |

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
