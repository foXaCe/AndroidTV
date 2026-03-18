# Audit Detail — Refonte Complète
**Date** : 2026-03-17
**Scope** : Pages détail Movie, Series, Season (v2)
**Fichiers audités** :
- `ui/itemdetail/v2/content/MovieDetailsContent.kt`
- `ui/itemdetail/v2/content/SeriesDetailsContent.kt`
- `ui/itemdetail/v2/content/SeasonDetailsContent.kt`
- `ui/itemdetail/v2/shared/DetailActions.kt`
- `ui/itemdetail/v2/shared/DetailSections.kt`
- `ui/itemdetail/v2/shared/DetailUtils.kt`
- `ui/itemdetail/v2/ItemDetailsComponents.kt`
- `ui/itemdetail/v2/ItemDetailsFragment.kt`
- `ui/itemdetail/v2/ItemDetailsViewModel.kt`
- `ui/base/components/VegafoXButton.kt`
- `ui/base/theme/VegafoXDimensions.kt`
- `ui/shared/components/MediaMetadataBadges.kt`

---

## Point 1 — Layout général et structure

### 1.1 Architecture actuelle

Le layout de la page détail est un `LazyColumn` plein écran, avec les items suivants :

| # | Item LazyColumn | Contenu | Hauteur |
|---|----------------|---------|---------|
| 1 | Hero Zone | Backdrop (transparent) + Row(info left + poster right) | `HeroDimensions.backdropHeight` = **360.dp** fixe |
| 2 | Gradient | Transition transparent → BackgroundDeep | `DetailDimensions.gradientHeight` = **40.dp** fixe |
| 3 | Section Header | "Épisodes de la saison X" / "Next Up" / etc. | `DetailSectionDimensions.headerHeight` = **48.dp** fixe |
| 4 | Section Row | LazyRow horizontale (épisodes/saisons/cast/similar) | Variable selon type (voir ci-dessous) |
| 5 | Bottom Padding | Spacer | `DetailDimensions.bottomPadding` = **80.dp** fixe |

### 1.2 Structure de la Hero Zone

```
Box(fillMaxWidth, height=360.dp, contentAlignment=BottomStart)
  └── AnimatedVisibility(fadeIn + slideInVertically 20dp, 350ms)
       └── Row(horizontalPadding=80.dp, bottom=16.dp, verticalAlign=Bottom)
            ├── Column(weight=1f)   ← INFO GAUCHE
            │    ├── CinemaGenreTag
            │    ├── Spacer 12.dp
            │    ├── Title (BebasNeue 68sp, maxLines=2)
            │    ├── Spacer 16.dp
            │    ├── MediaMetadataBadges
            │    ├── Spacer 16.dp
            │    ├── Synopsis (15sp, maxLines=3)
            │    ├── Spacer 16.dp
            │    ├── Primary Action Row (focusGroup)
            │    ├── Spacer 12.dp
            │    └── Secondary Action Row (focusGroup)
            │
            └── CinemaPosterColumn   ← POSTER DROIT
                 width=220.dp, ratio 2:3, top padding=24.dp
```

### 1.3 Proportions gauche/droite

- Colonne gauche : `weight(1f)` — prend tout l'espace restant
- Colonne droite : largeur fixe `CardDimensions.landscapeWidth` = **220.dp**
- Padding horizontal total : `HeroDimensions.horizontalPadding` = **80.dp** × 2 = 160.dp
- Gap entre colonnes : **24.dp** (Movie) ou `DetailDimensions.actionsSpacing` = **48.dp** (Series/Season)
- **Espace colonne gauche (sur 1920dp)** : 1920 − 160 − 220 − 24/48 = ~1516dp (Movie) / ~1492dp (Series)

### 1.4 Problèmes identifiés — Layout

| # | Problème | Sévérité | Fichier:Ligne |
|---|----------|----------|---------------|
| L1 | **Incohérence gap gauche/droite** : MovieDetailsContent utilise `Arrangement.spacedBy(24.dp)` alors que SeriesDetailsContent et SeasonDetailsContent utilisent `Arrangement.spacedBy(DetailDimensions.actionsSpacing)` = 48.dp. Le poster est visuellement plus éloigné dans les séries. | Moyen | Movie:185, Series:130, Season:129 |
| L2 | **Hero Zone 360.dp potentiellement trop petite** : Avec titre BebasNeue 68sp sur 2 lignes + synopsis 3 lignes + 2 rows de boutons, le contenu peut overflow en bas si le titre est long. Le `AnimatedVisibility` avec `BottomStart` alignment masque le débordement haut. | Élevé | Movie:155-156 |
| L3 | **Poster ratio fixe 2:3 sur 220.dp = 330.dp de haut**, mais le hero ne fait que 360.dp. Avec `posterTopPadding=24.dp`, le poster occupe 330+24=354dp sur les 360dp disponibles, laissant seulement 6dp de marge en bas. | Faible | Components:1758-1765 |
| L4 | **`disableBringIntoView()` uniquement sur le Hero** : Le BringIntoViewResponder neutralisé empêche le scroll automatique vers les boutons quand ils reçoivent le focus via D-pad, mais ne couvre pas les sections below hero. | Info | DetailUtils:22-33 |
| L5 | **Duplication massive** : MovieDetailsContent (700 lignes) duplique quasi-intégralement le code des boutons et des dialogs que DetailActionButtonsRow gère déjà dans DetailActions.kt. SeriesDetailsContent utilise `DetailActionButtonsRow` correctement, mais Movie et Season ont leur propre implémentation inline. | Élevé | Movie:253-385, Season:183-233, DetailActions:57-340 |

### 1.5 Backdrop (Fragment Layer)

Le backdrop est géré dans `ItemDetailsFragment.kt` (lignes 151-161) comme un `ImageView` Android natif, pas en Compose :
- Alpha fixe **0.55f**
- Blur via `RenderEffect` (Android 12+) ou `BitmapBlur` fallback
- Gradient overlay via `detail_backdrop_gradient` drawable (aussi natif)
- **Problème** : Le blur amount est lu depuis `UserSettingPreferences.detailsBackgroundBlurAmount` mais le chargement est asynchrone via Coil (pas de crossfade, pop visuel au chargement)

---

## Point 2 — Boutons d'action

### 2.1 Inventaire complet des boutons

#### Row Primaire (Primary Actions)

| Bouton | Variant | Compact | Hauteur effective | Icône | Condition |
|--------|---------|---------|-------------------|-------|-----------|
| Reprendre | `Primary` | false | **48.dp** | Play | `hasPlaybackPosition` |
| Reprendre depuis le début | `Secondary` | false | **48.dp** | Refresh | `hasPlaybackPosition` |
| Lecture | `Primary` | false | **48.dp** | Play | `!hasPlaybackPosition` |

#### Row Secondaire (Secondary Actions)

| Bouton | Variant | Compact | Hauteur effective | Icône | Largeur | Condition |
|--------|---------|---------|-------------------|-------|---------|-----------|
| Version | `Outlined` | **true** | **40.dp** | Guide | intrinsèque | `hasMultipleVersions` |
| Audio | `Outlined` | **true** | **40.dp** | Audiotrack | intrinsèque | `audioStreams.size > 1` |
| Sous-titres | `Outlined` | **true** | **40.dp** | Subtitles | intrinsèque | `subtitleStreams.isNotEmpty()` |
| Bandes-annonces | `Outlined` | **true** | **40.dp** | Trailer | intrinsèque | `hasPlayableTrailers` |
| Favori ♥ | `Outlined` | **true** | **40.dp** mais `Modifier.size(48.dp)` | Favorite/FavoriteOutlined | **48.dp × 48.dp** | `userData != null` |
| Vu/Non vu | `Outlined` | **true** | **40.dp** mais `Modifier.size(48.dp)` | Visibility/VisibilityOff | **48.dp × 48.dp** | `userData != null` |
| Aller à la série | `Outlined` | **true** | **40.dp** | Tv | intrinsèque | `EPISODE + seriesId` |
| Supprimer | `Outlined` | **true** | **40.dp** | Delete | intrinsèque | `canDelete` |

### 2.2 Problèmes identifiés — Boutons

| # | Problème | Sévérité | Fichier:Ligne |
|---|----------|----------|---------------|
| B1 | **Conflit hauteur Favori/Vu** : `VegafoXButton` avec `compact=true` a une hauteur intrinsèque de 40.dp (`ButtonDimensions.heightCompact`), mais le modifier externe `.size(48.dp)` force **48×48.dp**. Cela crée un bouton plus haut (48dp) que les autres compact (40dp) sur la même row, causant un **misalignement vertical visible**. | Critique | Movie:347-348, Movie:359-360, DetailActions:190-191, DetailActions:203 |
| B2 | **Row secondaire wrap-content en largeur** : La row secondaire n'a aucune contrainte de largeur max. Avec versions + audio + sous-titres + trailers + favori + vu + aller à la série + supprimer (8 boutons), la row peut dépasser la largeur disponible et être tronquée hors écran à droite. **Aucun scroll horizontal ni wrap.** | Élevé | Movie:290-385, DetailActions:133-228 |
| B3 | **Padding texte incohérent** : Les boutons icon-only (`isIconOnly=true`) ont `padding(horizontal=0.dp)` alors que les boutons texte ont `padding(horizontal=24.dp)`. Avec le `.size(48.dp)` externe, l'icône est centrée dans 48×48 mais ne s'aligne pas visuellement avec les boutons texte de 40.dp de haut. | Moyen | VegafoXButton:324 |
| B4 | **`focusGroup()` séparé sur chaque row** : Les deux rows primaire et secondaire ont chacune leur `focusGroup()`. La navigation D-pad bas depuis la row primaire saute correctement vers la row secondaire, mais **la row primaire a `focusProperties { right = FocusRequester.Cancel }`** qui bloque la navigation droite hors de cette row, ce qui est correct pour éviter le focus sur le poster, mais empêche de naviguer vers d'autres éléments à droite si le layout change. | Info | Movie:256-257, Season:187-188 |
| B5 | **Espacement incohérent** : Row primaire : `spacedBy(12.dp)`. Row secondaire : `spacedBy(10.dp)`. La différence est subtile mais visible quand les deux rows sont affichées l'une sous l'autre. | Faible | Movie:254, Movie:291 |

### 2.3 Analyse VegafoXButton (VegafoXButton.kt)

Dimensions intrinsèques :
- **Normal** : `height = ButtonDimensions.height` = **48.dp**, `minWidth` non appliqué (pas de modifier)
- **Compact** : `height = ButtonDimensions.heightCompact` = **40.dp**
- **Corner radius** : `ButtonDimensions.cornerRadius` = **10.dp** (identique pour tous)
- **Texte** : BebasNeue 15sp Bold, letterSpacing=1.5sp, uppercase
- **Icône** : 20dp (avec texte), 22dp (icon-only)
- **Padding horizontal** : 24dp (avec texte), 0dp (icon-only)

Le bouton utilise `Box` avec `contentAlignment = Center` — la hauteur est strictement fixée par le modifier `.height()`, pas par le contenu. C'est correct et empêche les variations de hauteur liées au contenu.

---

## Point 3 — Textes tronqués casting et équipe

### 3.1 Composant CastCard (ItemDetailsComponents.kt:295-372)

```
CastCard(width=100.dp, height=110.dp)
  ├── Photo circulaire (72.dp)
  ├── Spacer 6.dp
  ├── Nom   → labelMedium 12.sp, maxLines=1, Ellipsis
  └── Rôle  → labelSmall 11.sp, maxLines=1, Ellipsis
```

**Budget vertical** : 72 + 6 + ~15 (nom) + ~14 (rôle) = **~107dp** sur 110dp disponibles = marge quasi nulle.

### 3.2 Causes de troncature identifiées

| # | Cause | Sévérité | Détail |
|---|-------|----------|--------|
| C1 | **Largeur de card insuffisante** : 100.dp pour un nom + rôle, avec font 12sp/11sp et `maxLines=1`. Les noms français accentués (ex: "Jean-Pierre Bacri", "François Cluzet", "Gérard Depardieu") dépassent systématiquement 100dp à 12sp. | Critique | Components:314, Components:356 |
| C2 | **BebasNeue NON utilisée pour le casting** : Le nom et le rôle utilisent `labelMedium` et `labelSmall` du thème (probablement une font variable-width). BebasNeue n'est **pas** utilisée ici — ce n'est donc pas la police qui cause le problème, c'est purement la largeur de 100.dp. | Info | Components:356, Components:365 |
| C3 | **Aucun tooltip/expand on focus** : Quand la card est focusée (border orange via `animateColorAsState`), le texte ne s'agrandit pas et ne montre pas le nom complet. L'utilisateur ne peut jamais lire le nom complet d'un acteur si celui-ci est tronqué. | Élevé | Components:295-372 |
| C4 | **Photo circulaire trop grande proportionnellement** : 72dp de diamètre sur 100dp de large = 72% de la largeur. Cela écrase l'espace texte disponible en dessous. | Moyen | Components:325 |
| C5 | **Gap entre cards** : `DetailCastSection` utilise `spacedBy(24.dp)` — gap très large entre des cards de 100.dp. Réduire à 16dp permettrait d'afficher plus de cards à l'écran ou d'élargir chaque card. | Faible | DetailSections:148 |
| C6 | **Row conteneur trop petite** : `DetailSectionDimensions.castRowHeight = 135.dp` mais la CastCard fait 110dp + 10dp padding bottom du header = 120dp. Marge de 15dp suffisante, mais si la card grandissait (rôle sur 2 lignes), elle serait clippée. | Info | VegafoXDimensions:60 |

### 3.3 Test de troncature

Avec une font proportionnelle à 12sp (densité TV typique 1.0-1.5x) :
- "Jean-Pierre Bacri" ≈ 105dp → **tronqué** (dépasse 100dp)
- "François Cluzet" ≈ 90dp → ok
- "Gérard Depardieu" ≈ 100dp → **limite, tronqué avec Ellipsis**
- "Christopher Lambert" ≈ 115dp → **tronqué**
- Rôles : "Inspecteur Gadget" ≈ 95dp → ok, "Commissaire divisionnaire" ≈ 140dp → **tronqué**

### 3.4 Sections de métadonnées (DetailMetadataSection)

Non affichée dans MovieDetailsContent actuel — le `MetadataGroup` pour genres/réalisateur/scénariste/studio n'est **pas appelé** dans les layouts Movie/Series/Season. Ce composant existe dans `DetailSections.kt:47-73` mais n'est utilisé nulle part dans les content files v2.

---

## Point 4 — Saccades scroll (état post cosmetic-detail-16)

### 4.1 État des focusGroup/focusRestorer

| Section | focusGroup() | focusRestorer() | Fichier:Ligne |
|---------|-------------|-----------------|---------------|
| Row primaire (Movie) | ✅ | ❌ | Movie:257 |
| Row secondaire (Movie) | ✅ | ❌ | Movie:293 |
| Row primaire (Season) | ✅ | ❌ | Season:188 |
| Row secondaire (Season) | ✅ | ❌ | Season:212 |
| DetailActionButtonsRow (Series) | ✅ (×2) | ❌ | DetailActions:99, DetailActions:136 |
| DetailCastSection LazyRow | ✅ + ✅ `focusRestorer()` | ✅ | DetailSections:147 |
| DetailEpisodesSection LazyRow | ✅ + ✅ `focusRestorer()` | ✅ | DetailSections:116 |
| DetailSeasonsSection LazyRow | ✅ + ✅ `focusRestorer()` | ✅ | DetailSections:85 |
| DetailSectionWithCards LazyRow | ✅ + ✅ `focusRestorer()` | ✅ | DetailSections:187 |

**Constat** : Les LazyRow de sections utilisent correctement `focusRestorer().focusGroup()`. Les rows de boutons ont `focusGroup()` mais **pas** de `focusRestorer()` — acceptable car ce ne sont pas des LazyRow.

### 4.2 Hauteurs fixes des conteneurs Box

Toutes les sections below-hero utilisent des Box avec hauteur fixe :

| Section | Hauteur fixe | Suffisante ? |
|---------|-------------|-------------|
| headerHeight | 48.dp | ✅ |
| episodesRowHeight | 175.dp | ✅ (card 124.dp + text ~30.dp + padding) |
| castRowHeight | 135.dp | ⚠️ Limite — CastCard 110.dp + marge |
| similarRowHeight | 270.dp | ✅ (card 200.dp + text ~40.dp + padding) |
| seasonsRowHeight | 300.dp | ✅ (card 255.dp + text ~20.dp + padding) |

### 4.3 Mémorisation des URLs d'image

| Section | URL `remember`-isée ? | Fichier:Ligne |
|---------|----------------------|---------------|
| CinemaPosterColumn | ✅ `posterUrl` calculé une fois en haut du composable via `getPosterUrl()` | Movie:102 |
| DetailCastSection | ✅ `remember(person.id, person.primaryImageTag)` | DetailSections:152-160 |
| DetailEpisodesSection | ✅ `remember(ep.id)` | DetailSections:121 |
| DetailSeasonsSection | ✅ `remember(season.id)` | DetailSections:90 |
| DetailSectionWithCards | ✅ `remember(item.id)` (portrait et landscape) | DetailSections:201, 212 |
| DetailCollectionItemsGrid | ❌ **Pas de remember** — `getPosterUrl(item, api)` appelé à chaque recomposition | DetailSections:253 |

### 4.4 Problèmes scroll restants

| # | Problème | Sévérité |
|---|----------|----------|
| S1 | **CollectionItemsGrid URL non mémorisée** — recomposition de chaque poster à chaque frame scroll | Moyen |
| S2 | **LazyColumn entier sans `focusRestorer()`** — quand on revient sur l'écran détail après un back, le focus ne retourne pas au dernier item focusé dans la colonne | Moyen |
| S3 | **Animation d'entrée 100ms delay + 150ms requestFocus** — perceptible comme un mini-lag avant que les boutons ne soient focusables | Faible |

---

## Point 5 — Proposition de refonte

### 5.1 Vision globale

Objectif : **Une page détail cinéma immersive, fluide, lisible** — inspirée Apple TV+ / Netflix 2024 pour le layout, Disney+ pour les badges, tout en restant dans le design system VegafoX (glass dark, orange accent, BebasNeue).

### 5.2 Nouveau layout Hero

```
┌─────────────────────────────────────────────────────────────────┐
│                         BACKDROP (full width)                     │
│                    blur + gradient overlay                         │
│                                                                   │
│  ┌─ POSTER ─┐                                                    │
│  │           │  GENRE TAG ─── orange line                         │
│  │  240×360  │                                                    │
│  │  16dp rad │  TITRE (BebasNeue 56sp, max 2 lignes)             │
│  │           │                                                    │
│  │           │  BADGES: 2024 · 2h14 · ★ 8.2 · 🍅 93% · [4K]    │
│  │           │                                                    │
│  │           │  Synopsis (14sp, max 4 lignes, fadeout bottom)     │
│  │           │                                                    │
│  │  progress │  ┌─PLAY──┐ ┌──RESTART──┐                          │
│  │  bar      │  │  ▶    │ │   ↻      │                           │
│  └───────────┘  └───────┘ └──────────┘                           │
│                                                                   │
│  [Audio] [Sous-titres] [Versions] [BA] [♥] [✓] [→Série] [🗑]   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

**Changements clés** :
1. **Poster à GAUCHE, infos à DROITE** — inversé par rapport à l'actuel. Standard Netflix/Apple TV+/Disney+. Le regard part naturellement du poster (point d'accroche visuel) puis lit les infos.
2. **Poster plus grand** : 240×360dp (ratio 2:3) au lieu de 220×330dp. Coins arrondis 16dp maintenus.
3. **Titre réduit à 56sp** (actuel 68sp) — meilleur ratio lisibilité/espace. BebasNeue 68sp est excessif sur grand écran TV et laisse peu de place.
4. **Synopsis étendu à 4 lignes** avec gradient fade-out en bas au lieu d'Ellipsis.
5. **Gap fixe poster↔infos** : 32dp partout (plus de disparité 24/48dp).

### 5.3 Nouvelle organisation des boutons

#### Principe : hauteur uniforme, overflow géré

```
Row Primaire (hauteur 48dp, gap 12dp):
  [▶ LECTURE]Primary  [↻ REPRENDRE]Secondary
  OU
  [▶ REPRENDRE]Primary  [↻ DEPUIS LE DÉBUT]Secondary

Row Secondaire (hauteur 40dp, gap 10dp, SCROLLABLE horizontalement):
  [Audio] [Sous-titres] [Versions] [Bandes-annonces] | [♥] [✓] | [→Série] [🗑]
```

**Changements** :
1. **Suppression du `Modifier.size(48.dp)` sur Favori/Vu** — utiliser `compact=true` normalement (40.dp) pour l'alignement
2. **Row secondaire en LazyRow** si > 5 boutons — scroll horizontal avec `focusRestorer()` pour éviter le débordement hors écran
3. **Séparateur visuel** (pipe discret) entre les boutons média (audio/sub/version), les boutons d'état (favori/vu), et les boutons de navigation (série/supprimer)
4. **Extraction du code dupliqué** : MovieDetailsContent et SeasonDetailsContent doivent utiliser `DetailActionButtonsRow` comme SeriesDetailsContent le fait déjà

### 5.4 Nouvelle CastCard

```
┌──────────────────┐
│   ┌──────────┐   │
│   │  Photo   │   │
│   │  80×80   │   │
│   │ circular │   │
│   └──────────┘   │
│                  │
│  Nom Complet     │  ← 13sp, maxLines=2, center
│  Rôle            │  ← 11sp, maxLines=1, center
└──────────────────┘
   Largeur: 130dp
   Hauteur: 145dp
```

**Changements** :
1. **Largeur 130dp** (actuel 100dp) — +30% d'espace texte, élimine la troncature sur 95% des noms
2. **Photo 80dp** (actuel 72dp) — proportionnellement meilleure dans le nouveau cadre (80/130 = 61% vs 72/100 = 72%)
3. **Nom sur 2 lignes** (`maxLines=2`) — les noms longs comme "Jean-Pierre Bacri" s'affichent sur 2 lignes au lieu d'être tronqués
4. **Hauteur 145dp** (actuel 110dp) — pour accommoder le nom sur 2 lignes
5. **Gap réduit à 16dp** (actuel 24dp) — compense l'élargissement des cards
6. **`castRowHeight` augmenté** à 170dp (actuel 135dp)
7. **On-focus : nom complet en tooltip ou expand** — option avancée pour les cas extrêmes

### 5.5 Améliorations UX additionnelles

| # | Amélioration | Priorité | Détail |
|---|-------------|----------|--------|
| U1 | **MetadataGroup affiché** : Réactiver le `DetailMetadataSection` (genres, réalisateur, scénariste, studio) dans une section dédiée entre les boutons et le casting | Haute | Composant existe déjà, jamais appelé |
| U2 | **Logo au lieu du titre texte** : Si un logo (clearlogo) est disponible, l'afficher en Image au lieu du Text BebasNeue. Fallback sur le texte. Apple TV+ et Plex font ça. | Haute | `getLogoUrl()` existe déjà dans DetailUtils.kt:65-71 |
| U3 | **Transition backdrop smooth** : Ajouter un crossfade sur le chargement du backdrop (actuellement pop instantané) | Moyenne | ItemDetailsFragment.kt:244-276 |
| U4 | **Section "Dans votre watchlist"** : Si l'item est dans la watchlist, afficher un badge ou un état visuel distinct | Faible | - |
| U5 | **Refactoring des 3 content files** : Extraire le layout commun Hero+Gradient+Sections dans un composable partagé `DetailContentScaffold` et ne passer que les slots spécifiques (boutons, sections) | Haute | Élimine ~500 lignes de code dupliqué |
| U6 | **Animation focus-to-section** : Quand le D-pad scroll vers le bas et focus un élément below-hero, animer le scroll pour montrer le header de section + la row, pas juste la row | Moyenne | LazyColumn `animateScrollToItem` |

### 5.6 Récapitulatif des dimensions proposées

| Dimension | Actuel | Proposé | Raison |
|-----------|--------|---------|--------|
| Hero height | 360.dp | **400.dp** | Plus d'espace pour poster gauche + infos |
| Title fontSize | 68.sp | **56.sp** | Meilleure lisibilité, moins d'overflow |
| Poster width | 220.dp | **240.dp** | Plus visible, standard TV premium |
| CastCard width | 100.dp | **130.dp** | Noms non tronqués |
| CastCard height | 110.dp | **145.dp** | Nom 2 lignes |
| Cast photo | 72.dp | **80.dp** | Proportionnel |
| Cast gap | 24.dp | **16.dp** | Compense élargissement |
| castRowHeight | 135.dp | **170.dp** | Accommode nouvelles cards |
| Button icon-only height | 48.dp (override) | **40.dp** (compact natif) | Alignement row |
| Synopsis maxLines | 3 | **4** | Plus d'info visible |
| Gap colonnes | 24/48.dp | **32.dp** | Uniformisé |

---

## Matrice de priorités

| Ticket | Problème | Impact UX | Effort | Priorité |
|--------|----------|-----------|--------|----------|
| B1 | Misalignement boutons Favori/Vu (48dp vs 40dp) | Élevé | Faible | **P0** |
| L5 | Duplication code (Movie/Season vs DetailActionButtonsRow) | Maintenabilité | Moyen | **P0** |
| C1 | Troncature noms casting (100dp) | Élevé | Faible | **P1** |
| B2 | Overflow row secondaire (8+ boutons) | Moyen | Moyen | **P1** |
| U5 | Refactoring scaffold commun | Maintenabilité | Élevé | **P1** |
| L1 | Gap incohérent 24/48dp | Faible | Faible | **P2** |
| U2 | Logo clearlogo | Élevé (polish) | Faible | **P2** |
| U1 | MetadataGroup non affiché | Moyen | Faible | **P2** |
| U3 | Crossfade backdrop | Moyen | Faible | **P2** |
| S1 | CollectionItemsGrid URL non remember | Faible | Trivial | **P3** |
| L2 | Hero trop petite si titre long | Faible | Moyen | **P3** |
| B5 | Espacement 12/10dp | Trivial | Trivial | **P3** |

---

*Audit réalisé par lecture statique du code source. Aucun fichier modifié.*
