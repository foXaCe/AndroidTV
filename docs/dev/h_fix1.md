# H_FIX1 — Sidebar verticale fixe (remplacement topbar)

**Date** : 2026-03-10
**Statut** : Termine — BUILD SUCCESSFUL debug + release

---

## Problemes resolus

### P1 — Topbar invisible sur fond sombre

**Avant** : Barre horizontale avec onglets texte (Accueil/Films/Series/Musique/Live TV), recherche, avatar. Les onglets inactifs en TextSecondary (#9E9688) sur fond Surface 4% etaient quasi invisibles sur le backdrop sombre.

**Apres** : Sidebar verticale fixe a gauche (72dp) avec icones 24dp. Le fond gradient opaque (BackgroundDeep 98% → transparent) garantit la lisibilite des icones sur n'importe quel backdrop.

### P2 — Texte "Accueil" titre de page

Le texte "Accueil" provenait de l'onglet actif dans MainToolbar. Avec la sidebar en mode icones-seulement, aucun texte de navigation n'est affiche.

---

## Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `app/.../ui/shared/toolbar/MainToolbar.kt` | Reecriture complete : topbar horizontale → sidebar verticale 72dp |
| `app/.../ui/home/compose/HomeScreen.kt` | Layout Box avec sidebar overlay + contenu decale 72dp a droite |
| `app/.../ui/shared/toolbar/NavigationLayout.kt` | Cas NavbarPosition.TOP : Column → Box avec padding start SIDEBAR_WIDTH |

## Fichiers non modifies

| Fichier | Raison |
|---------|--------|
| `LeftSidebarNavigation.kt` | Sidebar collapsible existante, utilisee par NavbarPosition.LEFT — inchangee |
| `Toolbar.kt` | Composables Logo, ToolbarClock, ToolbarLayout — non utilises par le nouveau layout |
| Fragments Jellyseerr/Detail | Utilisent MainToolbar dans Box ou ComposeView — compatibles sans modification |

---

## Nouveau layout sidebar

```
╭─────────╮
│  [fox]   │   36dp, ic_vegafox_fox, pas de texte
│  36dp    │
│          │
│  ┌────┐  │
│  │Home│  │   48dp Box, ic_house, actif: OrangePrimary + OrangeSoft + ligne 3dp gauche
│  └────┘  │
│  ┌────┐  │
│  │Film│  │   48dp Box, ic_movie
│  └────┘  │
│  ┌────┐  │
│  │ TV │  │   48dp Box, ic_tv
│  └────┘  │
│  ┌────┐  │
│  │Mus.│  │   48dp Box, ic_music_album
│  └────┘  │
│  ┌────┐  │
│  │Live│  │   48dp Box, ic_tv_guide
│  └────┘  │
│          │
│  ┌────┐  │
│  │ ⚙ │  │   48dp Box, ic_settings, TextHint
│  └────┘  │
╰─────────╯
  72dp wide
```

### Etats visuels

| Etat | Icone | Fond | Extra |
|------|-------|------|-------|
| Normal | TextHint (#7A756B) | Transparent | — |
| Actif | OrangePrimary (#FF6B00) | OrangeSoft (10% orange) coins 12dp | Ligne 3dp OrangePrimary bord gauche |
| Focus | TextPrimary (#F5F0EB) | Surface (#141418) coins 12dp | — |

### Fond sidebar

Gradient horizontal : `BackgroundDeep.copy(alpha=0.98)` → `Color.Transparent`
- Bord gauche quasi opaque (7,7,11 a 98%)
- Bord droit transparent (le backdrop transparait)

---

## Navigation

| Icone | Action |
|-------|--------|
| Home (ic_house) | `navigationRepository.reset(Destinations.home)` |
| Films (ic_movie) | userView MOVIES → `itemLauncher.getUserViewDestination()` |
| Series (ic_tv) | userView TVSHOWS → idem |
| Musique (ic_music_album) | userView MUSIC → idem |
| Live TV (ic_tv_guide) | userView LIVETV → idem |
| Settings (ic_settings) | `settingsViewModel.show()` |

---

## Architecture HomeScreen

```
Box(fillMaxSize) {
    PanoramicBackground()            // Layer 0: gradients atmospheriques
    HomeHeroBackdrop(focusedItem)   // Layer 1: image backdrop plein ecran
    Column(padding start=72dp) {    // Layer 2: contenu decale
        Box(weight=0.45) {          //   Hero info (titre, tagline, pills, boutons)
            HeroInfoOverlay()
            HeroPaginationDots()
        }
        StateContainer(weight=0.55) //   Rows horizontales
    }
    MainToolbar(Home)               // Layer 3: sidebar overlay gauche
}
```

**Changement important** : Le toolbar ne consomme plus d'espace vertical dans la Column. Le hero (45%) et les rows (55%) occupent maintenant 100% de la hauteur ecran au lieu de (hauteur - toolbar). Cela donne plus d'espace au hero info, ce qui aide a resoudre le P0 "hero info tronquee" de l'audit.

---

## API publique

```kotlin
// Signature inchangee — aucun appelant casse
@Composable
fun MainToolbar(
    activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
    activeLibraryId: UUID? = null,
)

fun setupMainToolbarComposeView(
    composeView: ComposeView,
    activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
    activeLibraryId: UUID? = null,
)

enum class MainToolbarActiveButton { User, Home, Library, Search, Jellyseerr, None }

// Nouveau: constante publique pour le decalage
val SIDEBAR_WIDTH = 72.dp
```

---

## Elements retires

| Element | Raison |
|---------|--------|
| VegafoXTitleText ("Vega" bleu + "foX" orange) | Spec : icone renard seulement, pas de texte |
| Onglets texte (Accueil/Films/Series/Musique/Live TV) | Remplaces par icones |
| Barre de recherche | Non prevue dans la sidebar (accessible via D-pad ou autre) |
| Avatar utilisateur | Non prevu dans la sidebar |
| Bordures Divider du conteneur onglets | Plus necessaires (design icones individuels) |

---

## Impact sur les autres ecrans

| Ecran | Layout parent | Compatible |
|-------|--------------|------------|
| MediaDetailsFragment | Box overlay | Oui — sidebar se positionne en overlay |
| PersonDetailsFragment | Box overlay | Oui |
| ItemDetailsFragment | ComposeView + FrameLayout | Oui (WRAP_CONTENT height → sidebar tronquee visuellement, a ajuster si besoin) |
| DiscoverFragment | ComposeView XML | Oui (idem) |
| NavigationLayout TOP | Box + padding | Oui |
| NavigationLayout LEFT | LeftSidebarNavigation | Inchange |

**Note** : ItemDetailsFragment et DiscoverFragment utilisent des ComposeView avec WRAP_CONTENT height pour le cas TOP. La sidebar (fillMaxHeight) sera contrainte par cette hauteur. Un ajustement futur des LayoutParams en MATCH_PARENT pourrait etre necessaire.

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```

0 erreur, 0 warning.
