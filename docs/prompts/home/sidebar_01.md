# SIDEBAR_01 — Structure de base PremiumSideBar (sans débordement)

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Problème résolu

### Débordement sidebar → cards

**Avant** : La sidebar (`MainToolbar`) était positionnée en overlay `Box` au-dessus du contenu. Le contenu principal utilisait `padding(start = SIDEBAR_WIDTH)` pour se décaler. Résultat : la sidebar chevauchait techniquement les cards et le backdrop, et le fond gradient semi-transparent laissait transparaître les éléments en dessous.

**Après** : Layout `Row` propre — la sidebar et le contenu sont côte à côte, chacun dans son propre slot. Aucun chevauchement possible.

---

## Architecture — Avant / Après

### Avant (overlay)

```
Box(fillMaxSize) {
    DarkGridNoiseBackground()           // Layer 0
    HomeHeroBackdrop(focusedItem)       // Layer 1
    Column(padding start=72dp) {        // Layer 2: contenu décalé
        Hero (weight=0.56)
        Rows (weight=0.44)
    }
    MainToolbar(Home)                   // Layer 3: sidebar OVERLAY
}
```

### Après (Row side-by-side)

```
Row(fillMaxSize) {
    PremiumSideBar(fillMaxHeight)       // Gauche: 72dp fixe
    Box(weight=1f, fillMaxHeight) {     // Droite: contenu
        DarkGridNoiseBackground()       //   Layer 0
        HomeHeroBackdrop(item)          //   Layer 1
        Column(fillMaxSize) {           //   Layer 2
            Hero (weight=0.56)
            Rows (weight=0.44)
        }
    }
}
```

---

## Fichiers créés

| Fichier | Description |
|---------|-------------|
| `ui/home/compose/sidebar/PremiumSideBar.kt` | Nouveau composable sidebar 72dp — placeholders structurels |

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/HomeScreen.kt` | `Box` root → `Row` root ; `MainToolbar` overlay → `PremiumSideBar` side-by-side ; suppression `padding(start = SIDEBAR_WIDTH)` |

## Fichiers NON modifiés

| Fichier | Raison |
|---------|--------|
| `MainToolbar.kt` | Conservé intact — utilisé par les autres écrans (details, browsing, etc.) |
| `NavigationLayout.kt` | Inchangé — gère les cas LEFT/TOP pour les écrans non-home |

---

## PremiumSideBar — Spécifications

### Dimensions

- Largeur : **72dp** fixe (`PREMIUM_SIDEBAR_WIDTH`)
- Hauteur : `fillMaxHeight`
- Fond : `Color(0xFF060A0F)` — même couleur que `DarkGridNoiseBackground` base
- Séparation droite : **1dp** verticale, `OrangePrimary.copy(alpha = 0.10f)`

### Structure Column interne

```
┌──────────┐
│ Spacer   │  24dp
│ ■ Search │  56dp box rouge (#FF0000)
│ 6dp      │
│ ■ Films  │  56dp box rouge
│ 6dp      │
│ ■ Séries │  56dp box rouge
│ 6dp      │
│ ■ LiveTV │  56dp box rouge
│ 6dp      │
│ ■ Média  │  56dp box rouge
│          │
│ (flex)   │  Spacer weight(1f)
│          │
│ ■ Params │  56dp box rouge
│ 16dp     │
│ ● Profil │  48dp cercle bleu (#0000FF)
│ Spacer   │  24dp
└──────────┘
  72dp │1dp separator
```

### Placeholders (Phase 1)

| Élément | Forme | Taille | Couleur |
|---------|-------|--------|---------|
| NavItem (×6) | Box carré | 56dp | Rouge `#FF0000` |
| ProfileItem (×1) | Box cercle (`CircleShape`) | 48dp | Bleu `#0000FF` |

---

## Vérifications

- [x] Le contenu principal (hero + rows) commence après la sidebar — pas de `padding(start=...)` nécessaire
- [x] Aucun élément de la sidebar ne chevauche les cards
- [x] Le backdrop et la grid noise remplissent uniquement la zone `weight(1f)` à droite
- [x] BUILD SUCCESSFUL debug
- [x] BUILD SUCCESSFUL release
- [x] 0 erreur, 0 warning

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```
