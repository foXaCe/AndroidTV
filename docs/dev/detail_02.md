# Cinema Immersif — Redesign ecran detail media

**Date** : 2026-03-11
**Build** : v1.6.2 debug + release
**Appareil** : Ugoos AM9 Pro (192.168.1.152:5555), 1920x1080

---

## Fichiers modifies

| Fichier | Type | Resume |
|---------|------|--------|
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | Modifie | Backdrop alpha 0.8 → 0.55 |
| `ui/itemdetail/v2/content/MovieDetailsContent.kt` | Modifie | Hero height fixe 580dp, titre 68sp letterSpacing 2sp |
| `ui/itemdetail/v2/content/SeasonDetailsContent.kt` | Reecrit | Layout cinema immersif complet (hero + episodes LazyRow) |
| `ui/itemdetail/v2/content/SeriesDetailsContent.kt` | Reecrit | Layout cinema immersif complet (hero + sections below) |
| `ui/itemdetail/v2/shared/DetailActions.kt` | Reecrit | VegafoXButton primary + CinemaActionChip secondary (remplace DetailActionButton circles) |
| `ui/itemdetail/compose/DetailHeroBackdrop.kt` | Phase 1 | Gradients cinema (CINEMA_DEEP #060A0F), alpha 0.55, Crossfade 300ms |
| `ui/browsing/composable/inforow/rating.kt` | Phase 1 | `formatRatingClean()` — supprime trailing .0 (7.0→7, 7.6→7.6) |
| `ui/itemdetail/v2/ItemDetailsComponents.kt` | Phase 1 | +4 composables: CinemaGenreTag, CinemaPill, CinemaActionChip, CinemaPosterColumn |

---

## Avant / Apres

### ItemDetailsFragment — Backdrop

| Avant | Apres |
|-------|-------|
| `alpha = 0.8f` ImageView | `alpha = 0.55f` ImageView |
| Backdrop trop lumineux, ecrase le texte | Backdrop subtil, gradients cinema lisibles |

### MovieDetailsContent — Hero Zone

| Avant | Apres |
|-------|-------|
| `fillParentMaxHeight()` — plein ecran | `height(580.dp)` — fixe |
| Titre 52sp letterSpacing 1sp | Titre 68sp letterSpacing 2sp lineHeight 72sp |

### SeriesDetailsContent — Layout complet

| Avant | Apres |
|-------|-------|
| Layout vertical classique : logo/titre, info row, tagline, synopsis, poster a droite | Hero zone 580dp cinema : genre tag, titre 68sp, ratings, synopsis, poster 220x330 |
| `DetailActionButtonsRow` avec cercles gris `DetailActionButton` | VegafoXButton Primary (Lecture) + Secondary (Lecture aleatoire) + CinemaActionChip |
| `DetailInfoRow` + `MetadataGroup` | `InfoRowMultipleRatings` + synopsis directement |
| Sections collees (padding uniforme) | Sections sur BackgroundDeep avec gradient de transition |
| Pas d'animation entree | AnimatedVisibility fadeIn + slideInVertically 350ms EaseOutCubic |

### SeasonDetailsContent — Layout complet

| Avant | Apres |
|-------|-------|
| Layout poster gauche + texte a droite, episodes en liste verticale | Hero zone 580dp cinema : genre tag serie, titre saison 68sp, poster droite |
| `DetailActionButton` cercles (Play, Vu, Favori) | VegafoXButton Primary (Lecture) + CinemaActionChip (Vu, Favori) |
| Episodes en `LazyColumn items()` avec `SeasonEpisodeItem` vertical | Episodes en `DetailEpisodesHorizontalSection` LazyRow |
| Pas d'animation entree | AnimatedVisibility fadeIn + slideInVertically |

### DetailActions — Row d'actions

| Avant | Apres |
|-------|-------|
| Row de `DetailActionButton` : cercles gris `outlineVariant` avec icone seule | Row 1 : `VegafoXButton` Primary (Resume/Play) + Secondary (Restart/Shuffle) |
| Tous les boutons identiques en taille/forme | Row 2 : `CinemaActionChip` avec icone + label, focus glow orange |
| Resume = cercle Play identique aux autres | Resume = bouton orange large "Reprendre — Xm" |
| 9+ boutons tous visibles en ligne | Primaires distingues visuellement, secondaires en chips |

---

## Architecture du hero zone

```
Box (height 580dp)
+-- AnimatedVisibility (fadeIn 350ms + slideInVertically 20dp)
    +-- Row (padding horizontal 80dp)
        +-- Column (weight 1f) — Left column
        |   +-- Box [focusable, contentFocusRequester] — Titre zone
        |   |   +-- CinemaGenreTag (tiret orange + texte)
        |   |   +-- Text (titre 68sp Black letterSpacing 2sp)
        |   +-- CinemaPillsRow (annee, duree, badges colores)
        |   +-- InfoRowMultipleRatings
        |   +-- Text (synopsis 15sp, 3 lignes max)
        |   +-- Row [focusGroup] — Boutons primaires
        |   |   +-- VegafoXButton Primary (Reprendre/Lecture)
        |   |   +-- VegafoXButton Secondary (Recommencer/Aleatoire)
        |   +-- Row [focusGroup] — Chips secondaires
        |       +-- CinemaActionChip (Audio/Sous-titres/Version)
        |       +-- CinemaActionChip (Bandes-annonces)
        |       +-- CinemaActionChip (Favori — actif rouge)
        |       +-- CinemaActionChip (Vu — actif bleu)
        |       +-- CinemaActionChip (Supprimer)
        +-- CinemaPosterColumn (220dp, ratio 2:3, progress bar)
```

## Composables cinema (Phase 1)

### CinemaGenreTag
- Tiret orange (20x2dp) + texte uppercase (11sp Bold, letterSpacing 2dp, OrangePrimary)

### CinemaPill
- Bordure arrondie (4dp), padding 10x4dp, texte 11sp Bold
- 4K → OrangePrimary, HDR/DV → gold #FFD700, autres → Divider/TextHint

### CinemaActionChip
- Icone 20dp + label 10sp, focus scale 1.06 glow orange
- Etat actif : fond OrangeSoft, bordure activeColor

### CinemaPosterColumn
- 220dp, ratio 2:3, coins 16dp, barre progression 3dp OrangePrimary

## Gradients backdrop (DetailHeroBackdrop)

- **BACKDROP_ALPHA** : 0.55
- **CINEMA_DEEP** : `Color(0xFF060A0F)`
- **Horizontal** : 0.97→0.90→0.60→0.20 (gauche opaque → droite transparent)
- **Vertical** : transparent→transparent→0.70→opaque (haut → bas)

---

## Screenshots

| Fichier | Description |
|---------|-------------|
| `docs/screenshots/detail_a_film.png` | Detail film Avatar — hero cinema : genre tag FILM ACTION, titre 68sp, pills (4K HDR10 HEVC MKV 5.1 AC3), ratings (7.6 + RT 81%), synopsis, VegafoXButton Reprendre/Recommencer, CinemaActionChips, poster avec progression |
| `docs/screenshots/detail_a_serie.png` | Detail serie The Darwin Incident — hero cinema : genre tag SERIE ANIMATION, titre 68sp, rating 8.2, synopsis, VegafoXButton Lecture/Aleatoire, CinemaActionChips Favori/Non vu/Liste/Supprimer |

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Install debug + release sur AM9 Pro : Success
