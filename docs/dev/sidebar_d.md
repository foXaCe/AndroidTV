# SIDEBAR_D — Tooltip sur NavItem

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Objectif

Ajouter un tooltip label qui apparaît à droite de la sidebar quand un NavItem est focalisé et non sélectionné.

---

## Implémentation

### Structure NavItem modifiée

Le `Row` existant est maintenant enveloppé dans un `Box` parent qui permet au tooltip de déborder des limites de la sidebar :

```
Box {
    Row(72dp × 52dp) {        // contenu existant (indicateur + icône)
        ...
    }
    AnimatedVisibility(        // tooltip
        visible = isFocused && !isSelected,
        modifier = Modifier
            .align(CenterStart)
            .offset(x = 80dp)
            .zIndex(10f),
    ) {
        Box(bg=#1C1C22, rounded 8dp, pad 12h×6v) {
            Text(label, 12sp Bold TextPrimary)
        }
    }
}
```

### Tooltip — Spécifications

| Propriété | Valeur |
|-----------|--------|
| Fond | `#1C1C22` |
| Coins | `RoundedCornerShape(8dp)` |
| Padding | 12dp horizontal, 6dp vertical |
| Texte | 12sp `FontWeight.Bold` `TextPrimary` |
| Position | `offset(x = 80dp)` depuis le bord gauche |
| Z-order | `zIndex(10f)` pour passer au-dessus des cards |
| Visibilité | `isFocused && !isSelected` |
| Animation | `fadeIn` / `fadeOut` tween 120ms |

### zIndex sidebar

`PremiumSideBar` : ajout de `zIndex(1f)` sur le `Row` racine pour que le tooltip (qui déborde de la sidebar) s'affiche par-dessus le contenu principal dans le `Row` de HomeScreen.

---

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/NavItem.kt` | Row enveloppé dans Box ; ajout AnimatedVisibility tooltip avec fadeIn/fadeOut 120ms |
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Ajout `zIndex(1f)` sur le Row racine |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```
