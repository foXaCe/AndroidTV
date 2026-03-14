# Live TV Guide — Phase 1 : Migration Design VegafoX

**Date** : 2026-03-12
**Branche** : main

---

## Fichiers modifies

### Support / preparation

| Fichier | Modification |
|---------|-------------|
| `ui/base/icons/VegafoXIcons.kt` | Ajout `FavoriteOutlined` (FavoriteBorder) |
| `ui/base/theme/Spacing.kt` | `timelineHeight` : 32dp -> 40dp |
| `res/values/strings.xml` | Ajout `lbl_x_channels`, `lbl_guide_hours` |
| `res/values-fr/strings.xml` | Ajout traductions FR correspondantes |

### Fichier 1 — GuideTimeline.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Fond | `colorResource(R.color.timeline_bg)` #1A1418 | `VegafoXColors.Surface` #141418 |
| Hauteur | 32dp (TvSpacing) | 40dp |
| Texte heures | 14sp blanc par defaut | 14sp **Bold** `TextSecondary` |
| Barre "NOW" | **Absente** | Ligne verticale 2dp OrangePrimary alpha 0.90 + triangle 4dp en bas |
| Separateur bas | Absent | 1dp blanc alpha 0.08 |

### Fichier 2 — ProgramCellComposable.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Distinction temporelle | **Aucune** | 3 etats : passe / en cours / futur |
| Fond passe | Transparent | rgba(255,255,255,0.02) |
| Fond en cours | Transparent/category | OrangePrimary alpha 0.12 |
| Fond futur | Transparent/category | Transparent |
| Barre progression | **Absente** | 3dp track blanc 0.08 + fill OrangePrimary |
| Focus | accent Android theme | OrangePrimary alpha 0.20 + bordure gauche 3dp OrangePrimary |
| Bordure cellule | `#373233` hardcode | blanc alpha 0.06 |
| Texte programme | 18sp SansSerif Light blanc | 16sp SansSerif Normal TextPrimary (TextHint si passe) |
| NEW/PREMIERE | vert Jellyfin | OrangePrimary alpha 0.20, texte OrangePrimary |
| REPEAT | bleu ds_primary #00A4DC | blanc alpha 0.08, texte TextSecondary |
| HD | gris #808080 | blanc alpha 0.06, texte TextSecondary |
| Rating | noir sur gris | TextHint sur blanc alpha 0.06 |
| Recording | Conserve (rouge semantique) | Conserve |

### Fichier 3 — ChannelHeaderComposable.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Fond | Transparent | `VegafoXColors.BackgroundDeep` |
| Separateur droit | `#373233` border | 1dp blanc alpha 0.08 |
| Logo | 100x50dp, pas de clip | 90x44dp, coins 4dp |
| Fallback (pas de logo) | Nom + numero en blanc | Numero + nom en TextSecondary centre |
| Numero chaine | 14sp blanc a gauche | 12sp TextHint en bas-droite (discret) |
| Focus | accent Android theme | OrangePrimary alpha 0.15 + bordure droite 3dp OrangePrimary |
| Scale focus | Aucun | 1.02 spring Medium |
| Icone favori | Coeur rouge 16dp toujours visible | Coeur OrangePrimary 14dp visible **uniquement au focus** |

### Fichier 4 — LiveTvGuideGrid.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Fond grille | Transparent (fond Leanback flou) | `VegafoXColors.BackgroundDeep` opaque |
| Barre "NOW" | **Absente** | Ligne verticale 2dp OrangePrimary alpha 0.70 sur chaque row |
| Scroll auto | **Absent** | `LaunchedEffect` scroll horizontal vers now - 30min |
| Alternance rows | Aucune | Rows impaires : blanc alpha 0.015 |
| Accent couleur | `Utils.getThemeColor(colorAccent)` | `VegafoXColors.OrangePrimary` |
| Scale focus | Aucun | 1.02 spring Medium sur cellules et headers |

### Fichier 5 — LiveTvGuideScreen.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Fond ecran | Image floue Leanback | `VegafoXColors.BackgroundDeep` opaque |
| Header | Image 150x150 + nom 25sp + overview 16sp | **Bandeau TiviMate 120dp** : backdrop alpha 0.25 + gradients + thumb 80x60 + nom chaine 12sp OrangePrimary uppercase + titre 28sp BebasNeue + horaire 13sp + synopsis 13sp |
| Boutons action | IconButton base (25dp) | `VegafoXIconButton` (24dp, tint TextSecondary, focus OrangePrimary) |
| Date box fond | `timeline_bg` hardcode | `VegafoXColors.Surface` |
| Status bar | Anglais hardcode : "X of Y channels" | `stringResource(R.string.lbl_x_channels)` + `lbl_guide_hours` traduits |
| Loading fond | `black_transparent` | BackgroundDeep alpha 0.90, texte TextSecondary |

### Fichier 6 — ProgramDetailDialog.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Largeur | 600dp | 640dp |
| Fond | `JellyfinTheme.colorScheme.surface` | `VegafoXColors.Surface` + coins 16dp |
| Header | Titre 22sp centre | **TiviMate-style** : Box 80dp fond SurfaceDim + gradient + nom chaine 12sp OrangePrimary uppercase + titre 28sp BebasNeue + horaire 13sp |
| Synopsis | 14sp centre, 8 lignes | 14sp TextSecondary, 4 lignes |
| Info enregistrement | `#FF6B6B` hardcode | `VegafoXColors.Recording` |
| Bouton Tune | `Button` base | `VegafoXButton` Primary compact |
| Bouton Record | `Button` base | `VegafoXButton` Secondary compact |
| Bouton Cancel | `Button` base | `VegafoXButton` Outlined compact |
| Bouton Series | `Button` base | `VegafoXButton` Ghost compact |
| Icone favori | Coeur rouge/blanc | `VegafoXIconButton` FavoriteOutlined/Favorite tint OrangePrimary/TextHint |

### Fichier 7 — RecordDialog.kt

| Element | Avant | Apres |
|---------|-------|-------|
| Fond | `JellyfinTheme.colorScheme.surface` | `VegafoXColors.Surface` + coins 16dp |
| Titre | 22sp JellyfinTheme | 22sp **BebasNeue** TextPrimary |
| Timeline texte | JellyfinTheme.colorScheme | VegafoXColors.TextSecondary + OrangePrimary |
| Bouton Save | `Button` base | `VegafoXButton` Primary compact autoFocus |
| Bouton Cancel | `Button` base | `VegafoXButton` Ghost compact |
| Bouton padding | `Button` base | `VegafoXButton` Secondary compact |
| Labels | JellyfinTheme | VegafoXColors.TextSecondary |

---

## Statut fonctionnalites

| Fonctionnalite | Statut | Notes |
|----------------|--------|-------|
| Barre "NOW" timeline | OK | Ligne 2dp + triangle 4dp OrangePrimary, via drawWithContent |
| Barre "NOW" grille | OK | Ligne 2dp alpha 0.70 sur chaque row, scrolle avec le contenu |
| Scroll auto "now" | OK | LaunchedEffect scroll vers now - 30min a l'ouverture |
| Distinction passe/en cours/futur | OK | 3 fonds distincts + texte attenue pour passe |
| Barre de progression programme en cours | OK | 3dp track + fill OrangePrimary calcule |
| Alternance rows | OK | Rows impaires blanc alpha 0.015 |
| Scale focus | OK | 1.02 spring Medium sur cellules et headers |
| Boutons VegafoXButton | OK | Primary/Secondary/Outlined/Ghost dans les dialogs |
| Fond opaque guide | OK | BackgroundDeep au lieu d'image floue Leanback |
| Header TiviMate | OK | Bandeau 120dp avec backdrop + gradients + info |
| Status bar traduit | OK | Strings FR ajoutees |

---

## Screenshots

| Ecran | Fichier | Notes |
|-------|---------|-------|
| Live TV guide (0 channels) | `docs/screenshots/livetv_guide_v2.png` | Serveur sans chaines — le guide est vide. Structure visible : fond BackgroundDeep, timeline, status bar |
| Live TV browse | `docs/screenshots/livetv_browse_v2.png` | Page Films (navigation intermediaire) |

> **Note** : Le serveur JellyMox n'a pas de chaines Live TV configurees (0 channels).
> Les composants ne peuvent pas etre testes visuellement avec du contenu reel.
> La validation visuelle complete necessite un serveur avec des chaines Live TV.

---

## Limitations connues

1. **DatePicker** : Toujours `AlertDialog.Builder` natif Android — non migre (hors scope phase 1)
2. **Confirmation annulation serie** : Toujours `AlertDialog.Builder` natif — non migre
3. **Browse "TV en direct"** : `LiveTvBrowseFragment` Leanback non migre (hors scope)
4. **Loading skeleton** : Pas de skeleton, juste "Chargement..."
5. **Category colors** : Les couleurs par categorie (Movie violet, Sports bleu, News vert, Kids bleu) ont ete retirees — le fond est maintenant base sur l'etat temporel uniquement
