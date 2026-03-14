# Audit Live TV — Guide EPG & composants

**Date** : 2026-03-12
**Branche** : main
**Methode** : Lecture exhaustive du code + screenshots ADB sur Ugoos (192.168.1.152)

---

## Screenshots

| Ecran | Fichier |
|-------|---------|
| Browse Live TV (LiveTvBrowseFragment) | `docs/screenshots/livetv_browse.png` |
| Guide EPG (LiveTvGuideScreen) | `docs/screenshots/livetv_guide.png` |
| Program Detail Dialog | *(pas de channels sur le serveur — capture impossible)* |

> **Note** : Le serveur JellyMox n'a pas de chaines Live TV configurees (0 of 0 channels).
> L'analyse est donc basee sur le code source + la structure visible du guide vide.

---

## Architecture des fichiers

```
ui/livetv/
├── LiveTvGuideFragment.kt          # Fragment hote (ComposeView + JellyfinTheme)
├── LiveTvGuideViewModel.kt          # ViewModel: channels, programs, filters, paging
├── GuideFilters.kt                  # Filtres guide (favs, movies, sports, news, kids, series)
├── LiveTvGuideFragmentHelper.kt     # Helper legacy (fonctions bridgees)
├── TvManager.kt                     # Singleton: cache channels/programs globaux
├── TvManagerHelper.kt               # Helper pour TvManager
└── compose/
    ├── LiveTvGuideScreen.kt         # Ecran principal: Header + Timeline + Grid + Dialogs
    ├── LiveTvGuideGrid.kt           # Grille: LazyColumn(channels) × Row(programs)
    ├── GuideTimeline.kt             # Barre horaire scrollable
    ├── ChannelHeaderComposable.kt   # Header de chaine (nom, numero, logo, favori)
    ├── ProgramCellComposable.kt     # Cellule programme (nom, indicateurs, couleur categorie)
    ├── ProgramDetailDialog.kt       # Dialog detail: info + boutons record/tune/favorite
    ├── RecordDialog.kt              # Dialog enregistrement: padding, series options
    ├── FriendlyDateButton.kt        # Bouton date pour le date picker
    ├── GuidePagingButton.kt         # Bouton page up/down
    └── LiveTvRecordingApi.kt        # Fonctions suspend API: record, cancel, toggle favorite
```

---

## ETAPE 1 — Analyse de chaque composant

### 1. LiveTvGuideScreen.kt

**Ce qui est affiche** :
- **Header** : Image programme selectionne (150×150dp) + nom (25sp) + overview (16sp, 5 lignes max) + boutons action (Reset, Calendar, Filters, Settings)
- **Timeline** : Date a gauche (dans une Box de `channelHeaderWidth=160dp`) + `GuideTimeline` scrollable
- **Status bar** : "X of Y channels" + filtre info + duree (12sp, gris)
- **Grid** : `LiveTvGuideGrid` en weight(1f)
- **Dialogs** : ProgramDetailDialog, SettingsDialog (options + filters via SettingsRouterContent)

**Structure** :
```
Box(fillMaxSize)
└── Column(fillMaxSize)
    ├── GuideHeader (Row: image + info + action buttons)
    ├── Row: date box + GuideTimeline
    ├── if loading: Box centered "Loading..."
    ├── else: GuideStatusBar + LiveTvGuideGrid(weight 1f)
    ├── ProgramDetailDialog (overlay)
    ├── SettingsDialog options (overlay)
    └── SettingsDialog filters (overlay)
```

**Couleurs utilisees** :
- `colorResource(R.color.timeline_bg)` = `#1A1418` — fond timeline (hardcode dans colors.xml)
- `colorResource(R.color.black_transparent)` = `#97000000` — fond loading
- `Color.Gray` / `Color.White` — texte status bar (hardcode)
- Pas de VegafoXColors, pas de VegafoXGradients

**Composants** :
- `Icon`, `Text` = composants VegafoX base
- `IconButton` = composant VegafoX base
- `VegafoXIcons.Schedule/Calendar/Filter/Settings` = icones VegafoX
- `AsyncImage` = composant interne (pas VegafoX specifique)
- `SettingsDialog/SettingsRouterContent` = systeme settings VegafoX
- **DatePicker** : Utilise `AlertDialog.Builder` Android natif + layout XML `horizontal_scroll_pane` + `FriendlyDateButton` (view Android legacy, pas le composable!)

**Problemes identifies** :
- Le DatePicker utilise `AlertDialog.Builder` natif Android (pas Compose, pas VegafoX)
- Le `FriendlyDateButton` dans `showDatePicker()` est le **View Android** (`ui.FriendlyDateButton`), pas le composable `compose.FriendlyDateButton` — doublon fonctionnel
- Header n'utilise pas VegafoXGradients ni fond stylise

---

### 2. LiveTvGuideGrid.kt

**Ce qui est affiche** :
- LazyColumn de rows, chaque row = ChannelHeader fixe + Row scrollable de ProgramCell

**Structure** :
```
LazyColumn
├── item: GuidePagingButton (page up, optionnel)
├── items(channels): GuideRow
│   └── Row(fillMaxWidth, height=programCellHeight)
│       ├── GuideChannelHeaderCell(width=channelHeaderWidth)
│       └── Row(weight 1f, horizontalScroll)
│           └── forEach(cells): GuideProgramCell(width=calculated)
└── item: GuidePagingButton (page down, optionnel)
```

**Dimensions** :
- `programCellHeight` = **55.dp** — hauteur de chaque ligne (channel + programmes)
- `channelHeaderWidth` = **160.dp** — largeur colonne chaines
- `guideRowWidthPerMinDp` = **7.dp/min** — largeur proportionnelle a la duree

**Largeurs programmes calculees** :
- Programme de 30 min = 30 × 7 = **210dp**
- Programme de 60 min = 60 × 7 = **420dp**
- Programme de 120 min = 120 × 7 = **840dp**
- Grille 9h = 540 × 7 = **3780dp** total scrollable

**Couleurs** :
- `Color(Utils.getThemeColor(context, android.R.attr.colorAccent))` — accent couleur focus (heritee du theme Android, **pas VegafoXColors**)

**Navigation D-pad** :
- `focusable()` sur chaque cellule (channel header + programme)
- `onFocusChanged` pour callbacks focus
- `onPreviewKeyEvent` pour Enter/LongPress (click/longClick)
- `focusGroup()` sur chaque GuideRow pour grouper le focus
- Le scroll horizontal est synchronise entre toutes les rows via `horizontalScrollState` partage
- **LazyColumn** scroll vertical standard : haut/bas entre channels
- **Pas de scroll auto vers programme en cours** a l'ouverture
- **Pas de snap/alignement** du scroll horizontal

**Problemes identifies** :
- Couleur accent via `Utils.getThemeColor()` (theme Android) au lieu de `VegafoXColors`
- Pas de distinction visuelle programme en cours / passe / futur (meme fond transparent ou categoryBg)
- Pas de barre "now" (indicateur temps actuel sur la timeline)
- Le scroll horizontal ne se positionne pas automatiquement sur "maintenant"

---

### 3. ProgramCellComposable.kt

**Ce qui est affiche** :
- Nom programme (18sp, SansSerif Light, blanc)
- Prefixe `<< ` si le programme a commence avant le guide start
- Heure de debut (12sp) si programme demarre avant maintenant
- Indicateurs : NEW (vert), PREMIERE (vert), REPEAT (bleu `ds_primary`), Rating (gris), HD (gris)
- Indicateur enregistrement (icone record rouge/blanc, 10dp)

**Couleurs** :
- `borderColor` = `Color(0xFF373233)` — bordure cellule (hardcode)
- `bgColor` = `accentColor` si focus, sinon `categoryBg` :
  - Movie: `#328E24AA` (violet translucide 20%)
  - Sports: `#323949AB` (bleu translucide 20%)
  - News: `#3243A047` (vert translucide 20%)
  - Kids: `#32039BE5` (bleu clair translucide 20%)
  - Default: `Color.Transparent`
- NEW/PREMIERE: `dark_green_gradient_start` = `#c306610a` (vert fonce 76%)
- REPEAT: `ds_primary` = `#00A4DC` (bleu Jellyfin)
- Rating/HD: `Color(0xFF808080)` gris
- Texte: `Color.White` (hardcode)
- Texte indicateurs: `Color.Gray` ou `Color.Black`

**Problemes identifies** :
- **Pas de distinction visuelle entre programme en cours et programme passe/futur** — meme fond transparent
- Couleur `ds_primary` (#00A4DC) est la couleur **Jellyfin** historique, pas VegafoX
- Toutes les couleurs sont hardcodees, aucune reference a VegafoXColors
- Pas d'animation/effet de focus (pas de glow, pas de scale, pas d'elevation)

---

### 4. ChannelHeaderComposable.kt

**Ce qui est affiche** :
- Nom chaine (18sp, blanc, 1 ligne)
- Numero chaine (14sp, blanc, 1 ligne)
- Logo chaine (AsyncImage, 100×50dp)
- Icone favori (coeur rouge 16dp) si isFavorite

**Couleurs** :
- `borderColor` = `Color(0xFF373233)` — bordure (hardcode, identique a ProgramCell)
- `bgColor` = `accentColor` si focus, `Color.Transparent` sinon
- Texte: `Color.White` (hardcode)
- Favori: `Color.Red` (hardcode)

**Problemes identifies** :
- Pas de fond distinct pour la colonne chaines (transparent = se confond avec la grille)
- Pas d'effet de focus VegafoX (glow, elevation)
- Couleurs hardcodees

---

### 5. ProgramDetailDialog.kt

**Ce qui est affiche** :
- Titre programme (22sp, 1 ligne)
- Overview (14sp, 8 lignes max, centre)
- Timeline row : "on [channel] [date] @ [time] ([relative])"
- Info enregistrement (rouge `#FF6B6B`)
- Boutons FlowRow : Tune, Record/Cancel, Series record/cancel/settings, Favorite (coeur)

**Structure** :
```
DialogBase(visible, onDismiss)
└── Column(width=600dp, surface bg, medium shape, padding 24dp, verticalScroll)
    ├── Text titre
    ├── Text overview
    ├── TimelineRow
    ├── Text recording info
    └── FlowRow boutons
        ├── TuneButton (si en cours ou termine)
        ├── Record/Cancel button
        ├── Series record/cancel/settings buttons
        ├── TuneButton (si pas commence)
        └── IconButton favorite
```

**Couleurs** :
- `JellyfinTheme.colorScheme.surface` — fond dialog (bridge VegafoX via colorScheme)
- `JellyfinTheme.colorScheme.onSurface` — texte
- `JellyfinTheme.colorScheme.primary` — nom de la chaine
- `Color(0xFFFF6B6B)` — info enregistrement (hardcode)
- `Color.Red` / `Color.White` — favori

**Composants** :
- `DialogBase` — composant VegafoX base
- `Button` — composant VegafoX base
- `IconButton` — composant VegafoX base
- `VegafoXIcons.Favorite/Record/RecordSeries`

**Problemes identifies** :
- Le dialog utilise `JellyfinTheme.colorScheme` (bridge) mais pas directement VegafoXColors
- Le dialog de confirmation serie utilise `AlertDialog.Builder` Android natif
- Pas de design "premium" VegafoX (pas de gradient, pas de glow sur les boutons)
- Largeur fixe 600dp — pas responsive

---

### 6. RecordDialog.kt

**Ce qui est affiche** :
- Titre programme (22sp)
- TimelineRow
- Padding selectors (pre/post padding, bouton cyclique)
- Options serie (checkboxes) : Only new, Any time, Any channel
- Boutons Save/Cancel

**Couleurs** :
- `JellyfinTheme.colorScheme.surface/onSurface/primary` — bridge VegafoX
- Pas de couleurs VegafoX directes

**Composants** :
- `DialogBase`, `Button`, `Checkbox` — composants VegafoX base
- Largeur fixe 500dp

---

### 7. GuideTimeline.kt

**Ce qui est affiche** :
- Row scrollable synchro avec la grille
- Cellules de 30 min (ou partielle si debut non aligne)
- Texte heure (14sp, padding start 4dp)

**Couleurs** :
- `colorResource(R.color.timeline_bg)` = `#1A1418` — fond (hardcode)

**Problemes** :
- Pas de marqueur "maintenant" (barre verticale rouge typique des EPG)
- Le fond est un gris tres sombre, peu distinctif

---

### 8. GuidePagingButton.kt / FriendlyDateButton.kt

**Composants** :
- `GuidePagingButton` : bouton VegafoX basique (`Button` + `Text`)
- `FriendlyDateButton` (compose) : `Button` + `Column(date friendly + date format)` — utilise `JellyfinTheme.colorScheme.onButton`
- **Mais** le DatePicker dans `LiveTvGuideScreen.showDatePicker()` utilise la **View Android** `FriendlyDateButton`, pas le composable !

---

## ETAPE 2 — Screenshots via ADB

| Screenshot | Observation |
|------------|-------------|
| `livetv_browse.png` | Page "TV en direct" : design Leanback/Jellyfin non migre (**B**). Cards "Guide des programmes", "TV enregistree", "Programmation", "Series" en design Material gris opaque. Fond image floue d'une emission. Pas de VegafoXColors visible. |
| `livetv_guide.png` | Guide EPG vide (0 of 0 channels). Visible : Timeline "Aujourd'hui, 16:45, 17:00, 17:30, 18:00". Boutons Calendar/Filter/Settings en haut droite. Status "0 of 0 channels / Showing all programs for 9 hours". Fond = image Leanback floue, pas de fond VegafoX. |

> **Important** : Le fond du guide est l'image floue du Leanback Fragment (`LiveTvBrowseFragment`),
> pas un fond VegafoX sombre. La grille EPG est affichee **par-dessus** ce fond flou.

---

## ETAPE 3 — Resume detaille

### Dimensions

| Element | Dimension | Source |
|---------|-----------|--------|
| Hauteur cellule programme | **55dp** | `TvSpacing.programCellHeight` |
| Largeur colonne chaine | **160dp** | `TvSpacing.channelHeaderWidth` |
| Largeur par minute | **7dp/min** | `TvSpacing.guideRowWidthPerMinDp` |
| Hauteur timeline | **32dp** | `TvSpacing.timelineHeight` |
| Logo chaine | **100×50dp** | `ChannelHeaderComposable.kt:69` |
| Image programme header | **150×150dp** | `LiveTvGuideScreen.kt:257` |
| Icone action | **25dp** | `LiveTvGuideScreen.kt:287` |
| Dialog detail | **600×auto dp** | `ProgramDetailDialog.kt:107` |
| Dialog record | **500×auto dp** | `RecordDialog.kt:99` |

### Couleurs actuelles

| Element | Couleur | Source | VegafoX? |
|---------|---------|--------|----------|
| Fond timeline | `#1A1418` | `R.color.timeline_bg` | NON (hardcode XML) |
| Bordure cellule | `#373233` | `Color(0xFF373233)` hardcode | NON |
| Focus cellule/header | `android.R.attr.colorAccent` | `Utils.getThemeColor()` | NON (theme Android) |
| Programme movie | `#328E24AA` (violet 20%) | `R.color.guide_movie_bg` | NON |
| Programme sports | `#323949AB` (bleu 20%) | `R.color.guide_sports_bg` | NON |
| Programme news | `#3243A047` (vert 20%) | `R.color.guide_news_bg` | NON |
| Programme kids | `#32039BE5` (bleu 20%) | `R.color.guide_kids_bg` | NON |
| Programme default | `Transparent` | hardcode | NON |
| Indicateur NEW/PREMIERE | `#c306610a` | `R.color.dark_green_gradient_start` | NON |
| Indicateur REPEAT | `#00A4DC` | `R.color.ds_primary` | NON (Jellyfin) |
| Rating/HD badge | `#808080` | hardcode | NON |
| Info enregistrement | `#FF6B6B` | hardcode | NON |
| Texte general | `Color.White` | hardcode | NON |
| Dialog fond | `colorScheme.surface` | bridge VegafoX | OUI (indirect) |
| Dialog texte | `colorScheme.onSurface` | bridge VegafoX | OUI (indirect) |
| Dialog channel name | `colorScheme.primary` | bridge VegafoX | OUI (indirect) |

### Style dialog detail programme

- **Fond** : `JellyfinTheme.colorScheme.surface` (sombre via bridge VegafoX)
- **Shape** : `JellyfinTheme.shapes.medium` (coins arrondis)
- **Padding** : 24dp uniforme
- **Largeur** : 600dp fixe
- **Boutons** : `Button` VegafoX base (pas de style premium/glow)
- **Disposition** : `FlowRow` centre avec espacement 8dp
- **Pas de fond gradient**, pas d'image backdrop, pas de VegafoXGradients

### Navigation D-pad actuelle

| Action | Comportement |
|--------|-------------|
| Haut/Bas | Scroll vertical LazyColumn (channel to channel) |
| Gauche/Droite | Deplace focus entre cellules programmes OU vers channel header |
| Enter | Programme en cours → tune. Programme futur → dialog detail |
| Long press Enter | Toujours → dialog detail |
| Long press sur channel | Toggle favori |
| Scroll horizontal | Synchronise entre toutes les rows (ScrollState partage) |

**Manques navigation** :
- Pas de scroll auto vers "maintenant" a l'ouverture
- Pas de Page Up / Page Down hardware (les boutons paging existent mais ne sont pas branches dans le screen)
- Pas de raccourci pour revenir a "maintenant"
- Le focus initial n'est pas gere (tombe sur le premier element par defaut)

### Problemes visuels identifies

#### Critiques (P0)

1. **Pas de distinction programme en cours / passe / futur** — Tous les programmes non-focuses ont le meme fond (transparent ou categoryBg). Il devrait y avoir un fond different pour les programmes en cours (ex: teinte plus claire ou barre de progression).

2. **Pas de barre "Now"** — Aucun indicateur visuel du temps actuel sur la timeline ou la grille. Les EPG typiques affichent une ligne verticale rouge au temps present.

3. **Fond du guide = image floue Leanback** — Le guide EPG s'affiche par-dessus le fond du `LiveTvBrowseFragment` (image floue). Il n'y a pas de fond opaque propre au guide. Avec des channels, la grille sera semi-transparente sur ce fond flou.

#### Importants (P1)

4. **Couleur accent = theme Android** — Le focus utilise `android.R.attr.colorAccent` au lieu de VegafoXColors. Ca marche mais c'est pas coherent avec le design system.

5. **Toutes les couleurs sont hardcodees** — Aucune reference a VegafoXColors dans les composants grille (ProgramCell, ChannelHeader, Timeline). Le design system est ignore.

6. **DatePicker = AlertDialog natif Android** — Le selecteur de date utilise `AlertDialog.Builder` + la View Android `FriendlyDateButton`, pas le composable Compose. Incohérent avec le reste de l'UI Compose.

7. **Pas d'effets de focus VegafoX** — Pas de glow, pas de scale, pas d'elevation sur les cellules programmes ou headers quand focus. Seule la couleur accent change.

8. **Browse "TV en direct" = design Leanback (B)** — La page d'entree `LiveTvBrowseFragment` affiche des cards Leanback Material grises, completement hors design VegafoX.

#### Mineurs (P2)

9. **Texte "0 of 0 channels" en anglais** — Le status bar n'est pas traduit (strings hardcodees dans le composable).

10. **Texte "Showing all programs for 9 hours" en anglais** — Idem.

11. **Dialog confirmation serie = AlertDialog natif** — Dans ProgramDetailDialog, l'annulation de serie utilise `AlertDialog.Builder` au lieu d'un `DialogBase` Compose.

12. **Pas de loading skeleton** — Le loading affiche juste "Loading..." sans skeleton pour la grille.

---

## Classification design

| Composant | Design | Notes |
|-----------|--------|-------|
| LiveTvGuideScreen | **C** | VegafoXIcons utilises, mais couleurs hardcodees, pas de VegafoXColors/Gradients |
| LiveTvGuideGrid | **C** | Structure Compose OK, couleurs accent = theme Android |
| ProgramCellComposable | **C** | VegafoXIcons (record), couleurs hardcodees, pas de VegafoXColors |
| ChannelHeaderComposable | **C** | VegafoXIcons (favorite), couleurs hardcodees |
| ProgramDetailDialog | **C** | DialogBase + Button VegafoX, mais pas de design premium |
| RecordDialog | **C** | Idem ProgramDetailDialog |
| GuideTimeline | **B** | Aucune reference VegafoX, tout hardcode |
| LiveTvBrowseFragment | **B** | Leanback design complet, non migre |

**Resume** : Tous les composants Live TV sont en etat **C** (mix) ou **B** (original). Le code Compose est propre et fonctionnel, mais le design VegafoX n'est pas applique. La migration necessiterait :
1. Remplacer toutes les couleurs hardcodees par VegafoXColors
2. Ajouter VegafoXGradients pour les fonds
3. Ajouter les effets de focus (glow, scale)
4. Ajouter un indicateur "maintenant" sur la timeline
5. Distinguer visuellement programmes en cours / passes / futurs
6. Remplacer les AlertDialog natifs par des DialogBase Compose
7. Migrer le DatePicker vers un composable
8. Traduire les textes hardcodes
