# Audit Visuel Complet — Live TV

**Date** : 2026-03-16
**Périmètre** : 11 fichiers Compose Live TV + 4 fragments Browse
**Statut** : Lecture seule — aucun fichier modifié

---

## Point 1 — Cohérence Design System

### Guide TV (compose/)

| Fichier | Couleurs | Typo | Dimensions | Boutons | Icônes | Verdict |
|---------|----------|------|------------|---------|--------|---------|
| `LiveTvGuideScreen.kt` | VegafoXColors partout | BebasNeue + fontSize raw | LiveTvDimensions + TvSpacing | VegafoXButton + VegafoXIconButton | VegafoXIcons | OK |
| `LiveTvGuideGrid.kt` | VegafoXColors.BackgroundDeep, OrangePrimary | — | TvSpacing | — | — | OK |
| `GuideTimeline.kt` | VegafoXColors.Surface, OrangePrimary, TextSecondary | fontSize raw 14.sp | TvSpacing | — | — | OK |
| `ChannelHeaderComposable.kt` | VegafoXColors partout | fontSize raw 12-14.sp | — | — | VegafoXIcons.Favorite | OK |
| `ProgramCellComposable.kt` | VegafoXColors partout | FontFamily.SansSerif | — | — | VegafoXIcons.Record/RecordSeries | OK |
| `ProgramDetailDialog.kt` | VegafoXColors partout | BebasNeue 28sp | LiveTvDimensions | VegafoXButton + VegafoXIconButton | VegafoXIcons | OK |
| `RecordDialog.kt` | VegafoXColors partout | BebasNeue 22sp | LiveTvDimensions | VegafoXButton | — | OK |
| `LiveTvBrowseScreen.kt` | VegafoXColors partout | BebasNeue 24-40sp | LiveTvDimensions | — | VegafoXIcons | OK |
| `GuidePagingButton.kt` | — (hérite) | — | — | `Button` (base) | — | OK |
| `FriendlyDateButton.kt` | VegafoXColors.TextPrimary | fontSize raw | — | `Button` (base) | — | OK |
| `LiveTvRecordingApi.kt` | — (pas de UI) | — | — | — | — | N/A |

### Valeurs hardcodées résiduelles dans compose/

| Fichier | Ligne | Valeur hardcodée | Impact |
|---------|-------|------------------|--------|
| `LiveTvGuideScreen.kt` | 322 | `padding(horizontal = 24.dp)` | Devrait être `LiveTvDimensions` ou `TvSpacing` |
| `LiveTvGuideScreen.kt` | 334 | `size(width = 80.dp, height = 60.dp)` | Taille thumbnail hardcodée |
| `LiveTvGuideScreen.kt` | 339 | `RoundedCornerShape(8.dp)` | Devrait être un token shape |
| `LiveTvGuideScreen.kt` | 119, 387-389, 396, 399 | `fontSize = 12/13/14/28.sp` | Pas de VegafoXTypography |
| `LiveTvGuideGrid.kt` | 119 | `Color.White.copy(alpha = 0.015f)` | Couleur zebra hardcodée |
| `GuideTimeline.kt` | 124 | `Color.White.copy(alpha = 0.08f)` | Séparateur hardcodé |
| `ChannelHeaderComposable.kt` | 30 | `Color.White.copy(alpha = 0.08f)` | Séparateur hardcodé (variable locale) |
| `ChannelHeaderComposable.kt` | 69, 80, 89 | `width(3.dp)`, `padding(4.dp)` | Dimensions focus border hardcodées |
| `ProgramCellComposable.kt` | 43 | `Color.White.copy(alpha = 0.06f)` | Bordure cell hardcodée |
| `ProgramCellComposable.kt` | 72 | `Color.White.copy(alpha = 0.02f)` | Fond PAST hardcodé |
| `ProgramCellComposable.kt` | 169, 179, 189, 211 | `Color.White.copy(alpha = 0.06/0.08f)` | Badges & progress bg hardcodés |
| `ProgramCellComposable.kt` | 244 | `Color.Red` | Enregistrement = `Color.Red` brut, pas VegafoXColors |
| `ProgramDetailDialog.kt` | 114, 132, 308, 326 | `RoundedCornerShape(16.dp)` | Shape hardcodé |
| `ProgramDetailDialog.kt` | 124 | `height(80.dp)` | Hauteur header hardcodée |
| `LiveTvBrowseScreen.kt` | 153 | `Color.White.copy(alpha = 0.06f)` | Divider hardcodé |
| `LiveTvBrowseScreen.kt` | 55-56 | `ANIM_MS = 150` | Durée animation hardcodée |
| `RecordDialog.kt` | 103 | `RoundedCornerShape(16.dp)` | Shape hardcodé |

### Browse fragments (v2/)

| Fichier | Couleurs | Typo | Boutons | Verdict |
|---------|----------|------|---------|---------|
| `LiveTvBrowseFragment.kt` | Via LiveTvBrowseScreen | — | — | OK |
| `RecordingsBrowseFragment.kt` | **JellyfinTheme.colorScheme** partout | **JellyfinTheme.typography** | Aucun VegafoXButton | **INCOHÉRENT** |
| `ScheduleBrowseFragment.kt` | **JellyfinTheme.colorScheme** partout | **JellyfinTheme.typography** | Aucun VegafoXButton | **INCOHÉRENT** |
| `SeriesRecordingsBrowseFragment.kt` | **JellyfinTheme.colorScheme** partout | **JellyfinTheme.typography** | Aucun VegafoXButton | **INCOHÉRENT** |

**Constat majeur** : Les 3 fragments Recordings/Schedule/SeriesRecordings utilisent exclusivement `JellyfinTheme.colorScheme` et `JellyfinTheme.typography` au lieu du design system VegafoX (`VegafoXColors`, `BebasNeue`, `VegafoXButton`). Ils n'ont pas été migrés vers le design system premium.

---

## Point 2 — Guide TV : Layout et Navigation

### Structure du guide

```
LiveTvGuideScreen
├── VegafoXScaffold (sidebar activée)
├── Box (fond BackgroundDeep)
│   └── Column
│       ├── GuideHeader (bannière programme sélectionné, type TiviMate)
│       ├── Row
│       │   ├── Box (date — largeur TvSpacing.channelHeaderWidth)
│       │   └── GuideTimeline (scroll horizontal partagé)
│       ├── GuideStatusBar (nombre chaînes + heures guide)
│       └── LiveTvGuideGrid
│           └── LazyColumn
│               ├── [item page_up]
│               ├── items(channels) → GuideRow
│               │   ├── GuideChannelHeaderCell (fixe, largeur channelHeaderWidth)
│               │   └── Row(horizontalScroll partagé)
│               │       └── forEach ProgramCellData → GuideProgramCell
│               └── [item page_down]
```

**Layout** :
- Vertical : `LazyColumn` avec un item par chaîne — scroll D-pad haut/bas fonctionnel
- Horizontal : `Row` avec `horizontalScroll(horizontalScrollState)` — le `ScrollState` est **partagé** entre la timeline et toutes les rangées de programmes, garantissant la synchronisation horizontale
- Les headers de chaînes sont **fixes** (en dehors du scroll horizontal), seulement les programmes scrollent

**Navigation D-pad** :
- **Haut/Bas** : Géré nativement par `LazyColumn` + `focusable()` sur chaque `GuideChannelHeaderCell` et `GuideProgramCell`
- **Gauche/Droite** : Le scroll horizontal est piloté par le `horizontalScrollState` partagé. Chaque programme et header est `focusable()` avec gestion manuelle de `onPreviewKeyEvent` pour Enter/longPress. **Pas de gestion explicite du D-pad gauche/droite pour naviguer entre programmes** — le comportement repose sur le `focusable()` natif Compose dans un `Row` scrollable
- Les cellules de programmes ne sont **pas** dans un `LazyRow` — elles sont dans un `Row` simple avec `horizontalScroll`. Cela signifie que toutes les cellules de toute la timeline sont composées en même temps

**Focus initial** :
- `LaunchedEffect(guideStart)` scroll automatiquement à "maintenant - 30min" pour centrer la vue sur le programme en cours
- **Pas de `requestFocus` explicite** sur le programme en cours — le focus se pose sur le premier élément focusable de la `LazyColumn`, qui sera la première chaîne

### Problèmes identifiés
1. **Pas de focus initial sur le programme en cours** — l'utilisateur doit naviguer manuellement
2. **Navigation horizontale D-pad** entre programmes repose entièrement sur le focus natif de Compose dans un `Row` — peut être fragile avec beaucoup de cellules

---

## Point 3 — Programme en cours : Indicateur Visuel

Dans `ProgramCellComposable.kt` :

### Distinction visuelle — OK
- **3 états temporels** définis : `PAST`, `CURRENT`, `FUTURE` (enum `ProgramTimeState`, L45)
- **Fond CURRENT** : `VegafoXColors.OrangePrimary.copy(alpha = 0.12f)` (L73) — teinte orange visible
- **Fond PAST** : `Color.White.copy(alpha = 0.02f)` — quasi transparent, programmes passés grisés
- **Fond FUTURE** : `Color.Transparent`
- **Texte PAST** : `VegafoXColors.TextHint` (grisé)

### Barre de progression — OK
- **Présente uniquement pour CURRENT** (L200-221)
- Calcul précis : `elapsed / totalDuration` coerced `0f..1f`
- Barre de 3dp en bas de la cellule, fond `Color.White(0.08)`, remplissage `VegafoXColors.OrangePrimary`

### Indicateur de focus — OK
- Focus = bordure gauche orange 3dp + fond orange 20% alpha (L88-97)

### Now Line — OK
- Ligne verticale orange semi-transparente (`OrangePrimary.copy(alpha = 0.70f)`) dessinée via `drawWithContent` dans chaque `GuideRow` (L196-216 de `LiveTvGuideGrid.kt`)
- Triangle/chevron orange dans la timeline (L72-81 de `GuideTimeline.kt`)

---

## Point 4 — Header Chaînes

Dans `ChannelHeaderComposable.kt` :

### Contenu affiché
- **Logo chaîne** : `AsyncImage` avec l'URL primaire (via `ImageHelper.getPrimaryImageUrl`)
- **Numéro de chaîne** : affiché en bas-droite (12sp, TextHint) quand le logo est présent (L115-125)
- **Fallback sans logo** : numéro centré (14sp) + nom centré (14sp) empilés verticalement (L94-109)
- **Icône favori** : coeur orange VegafoXIcons.Favorite, visible **uniquement au focus** (L128-139)

### AsyncImage — Problème partiel
- Utilise `AsyncImage` (composable custom du projet, pas Coil3), mais **sans placeholder ni error fallback explicite**
- Le fallback est géré par la branche `else` quand `imageUrl == null` — mais si l'URL existe et que le chargement échoue, il n'y a **pas de fallback visuel**

### Synchronisation verticale — OK
- Le header est dans le même `Row` que les programmes, au sein d'un item de `LazyColumn`
- Comme chaque `GuideRow` est un seul item `LazyColumn`, le header scroll verticalement avec les programmes

---

## Point 5 — Dialog Détail Programme

Dans `ProgramDetailDialog.kt` :

### Éléments affichés
| Élément | Ligne | Présent |
|---------|-------|---------|
| Nom de la chaîne | L147-157 | Oui (uppercase, orange, 12sp) |
| Titre du programme | L160-167 | Oui (BebasNeue 28sp) |
| Heure début/fin | L170-188 | Oui (13sp, TextSecondary) |
| Synopsis | L198-206 | Oui (14sp, 4 lignes max) |
| Durée calculée | — | **Non** |
| Classification (rating) | — | **Non** (affichée seulement dans ProgramCell) |
| Acteurs/casting | — | **Non** |
| Info enregistrement | L210-226 | Oui (statut recording, couleur Recording) |

### Boutons — VegafoXButton utilisé partout
| Action | Variant | Condition |
|--------|---------|-----------|
| Tune to Channel | Default (primary) | Si en cours ou terminé |
| Cancel Recording | Outlined | Si timerId existe |
| Record | Secondary | Si pas de timerId |
| Cancel Series | Outlined | Si seriesTimerId existe |
| Series Settings | Ghost | Si seriesTimerId existe |
| Record Series | Secondary | Si isSeries && pas de seriesTimerId |
| Tune to Channel | Default | Si pas encore commencé |
| Favorite toggle | VegafoXIconButton | Si pas terminé |

### Cohérence — Problème mineur
- Utilise `DialogBase` (composable custom) — **OK** pour la cohérence
- **`AlertDialog.Builder(context)`** natif Android à L308 pour la confirmation d'annulation de série — **INCOHÉRENT** avec le reste de l'app qui utilise des dialogs Compose
- Shape `RoundedCornerShape(16.dp)` hardcodé

---

## Point 6 — Dialog Enregistrement

Dans `RecordDialog.kt` :

### Options disponibles
| Option | Type | Détails |
|--------|------|---------|
| Pre-padding | Sélecteur cyclique | 0, 1min, 5min, 15min, 30min, 1h, 1h30, 2h, 3h |
| Post-padding | Sélecteur cyclique | Même liste |
| Only new episodes | Toggle | Visible uniquement si `isSeries` |
| Record any time | Toggle | Visible uniquement si `isSeries` |
| Record any channel | Toggle | Visible uniquement si `isSeries` |
| Timeline row | Info | Chaîne + date + heure + temps relatif |

### Boutons
- **Save** : `VegafoXButton` default, `autoFocus = true`
- **Cancel** : `VegafoXButton` Ghost

### Cohérence visuelle
- `DialogBase` — OK
- `VegafoXColors.Surface` + `RoundedCornerShape(16.dp)` — cohérent mais shape hardcodé
- `BebasNeue` pour le titre — OK
- **CheckboxRow utilise VegafoXButton Ghost** comme toggle au lieu d'un vrai composable checkbox — **Pas de retour visuel de l'état checked/unchecked**. La checkbox ne montre pas visuellement si l'option est activée ou non

---

## Point 7 — Browse Fragments Live TV

### LiveTvBrowseFragment.kt
| Critère | Statut | Détails |
|---------|--------|---------|
| VegafoXScaffold | **NON** — utilisé indirectement via LiveTvBrowseScreen.kt | LiveTvBrowseScreen n'utilise pas VegafoXScaffold |
| BrowseHeader (BebasNeue) | **OUI** mais custom | Utilise `BebasNeue` 40sp directement, pas `BrowseHeader` composant |
| Cards | N/A | Écran de navigation par tuiles, pas de cards media |
| Skeleton/loading | **NON** | Pas de skeleton |

**Problème** : `LiveTvBrowseScreen.kt` n'enveloppe **pas** son contenu dans `VegafoXScaffold`, donc pas de sidebar sur cet écran.

**Note** : `LiveTvGuideScreen.kt` enveloppe dans `VegafoXScaffold` (L87) — OK.

### RecordingsBrowseFragment.kt
| Critère | Statut | Détails |
|---------|--------|---------|
| VegafoXScaffold | **NON** | Pas utilisé |
| BrowseHeader (BebasNeue) | **NON** | Header custom avec `JellyfinTheme.typography.headlineMedium`, pas BebasNeue |
| Cards | **Custom RecordingCard** | Pas `BrowseMediaCard` ni `MediaPosterCard` |
| Skeleton | **OUI** | `SkeletonLandscapeCardRow` pour loading |
| Empty state | **OUI** | `EmptyState` composant |
| Error state | **OUI** | `ErrorState` avec retry |

### ScheduleBrowseFragment.kt
| Critère | Statut | Détails |
|---------|--------|---------|
| VegafoXScaffold | **NON** | Pas utilisé |
| BrowseHeader (BebasNeue) | **NON** | Header custom avec `JellyfinTheme.typography.headlineMedium` |
| Cards | **Custom ScheduleCard** | Pas `BrowseMediaCard` |
| Skeleton | **OUI** | `SkeletonLandscapeCardRow` |
| Empty state | **OUI** | Via StateContainer |
| Error state | **OUI** | Via StateContainer |

### SeriesRecordingsBrowseFragment.kt
| Critère | Statut | Détails |
|---------|--------|---------|
| VegafoXScaffold | **NON** | Pas utilisé |
| BrowseHeader (BebasNeue) | **NON** | Header custom avec `JellyfinTheme.typography.headlineMedium` |
| Cards | **Custom SeriesTimerCard** | Pas `BrowseMediaCard` |
| Skeleton | **OUI** | `SkeletonLandscapeCardRow` |
| Empty state | **OUI** | Via StateContainer |
| Error state | **OUI** | Via StateContainer |

### Résumé Browse Fragments
Les 3 fragments Recordings/Schedule/SeriesRecordings :
- N'utilisent **pas** `VegafoXScaffold` → pas de sidebar
- N'utilisent **pas** `BrowseHeader` → pas de BebasNeue dans les headers
- N'utilisent **pas** `BrowseMediaCard` / `MediaPosterCard` → cards custom non-standardisées
- Utilisent `JellyfinTheme` au lieu de `VegafoXColors`/`VegafoXTypography`
- **Ont** correctement des états loading (skeleton), empty, et error

---

## Point 8 — Problèmes de Performance

Dans `LiveTvGuideGrid.kt` :

### Keys stables
- `LazyColumn items` utilise `key = { it.id.toString() }` (L117) — **OK**, clés stables basées sur l'UUID

### contentType
- **NON DÉFINI** — `items()` n'utilise pas le paramètre `contentType` — les composables ne bénéficient pas de la réutilisation optimisée par type

### Optimisation de la grille
- **Problème majeur** : chaque `GuideRow` utilise un `Row` simple avec `horizontalScroll` au lieu d'un `LazyRow` → **TOUTES les cellules programmes de chaque rangée visible sont composées**, même hors écran
- Pour un guide de 24h avec des programmes de 30min, cela représente ~48 cellules par rangée × nombre de rangées visibles
- **Pas de pagination lazy horizontale**

### Logos chaînes
- Les URLs d'image sont mémorisées via `remember(channel)` dans `GuideRow` — OK
- `AsyncImage` interne gère vraisemblablement le cache

### Recomposition
- `buildProgramCells` est `remember(programs, guideStart, guideEnd)` — OK, ne recalcule pas inutilement
- `LocalDateTime.now()` est appelé dans `ProgramCell` à chaque recomposition (L56) — **devrait être hoisted** ou refresh périodique pour éviter de recalculer l'état temporel inutilement
- Même problème dans `LiveTvGuideGrid.kt` L86 (`val now = LocalDateTime.now()`) — pas dans un `remember`

### indexOf anti-pattern
- `LiveTvGuideGrid.kt` L118 : `val rowIndex = channels.indexOf(channel)` dans le body de `items()` — **O(n) par rangée**, devrait utiliser l'index fourni par `itemsIndexed`

---

## Point 9 — Sélecteur de Date/Heure

Dans `LiveTvGuideScreen.kt` :

### Composable utilisé
- `GuideDatePickerDialog` (L478-544) — **composable custom Compose pur**
- Utilise `androidx.compose.material3.AlertDialog` comme conteneur

### Fonctionnement
- Génère 15 dates à partir d'aujourd'hui (`LocalDateTime.now().plusDays(0..14)`)
- Chaque date est un `Row` cliquable dans un `LazyColumn`
- Aujourd'hui affiché en orange bold (`VegafoXColors.OrangePrimary`)
- Format : `"EEE d MMM"` (ex: "Lun 16 Mar")
- Bouton dismiss : `VegafoXButton` Ghost "Cancel"

### Boutons d'accès
- **Calendrier** : `VegafoXIconButton` avec `VegafoXIcons.Calendar` dans le GuideHeader (L419-423)
- **Reset "Maintenant"** : `VegafoXIconButton` avec `VegafoXIcons.Schedule`, visible seulement si le guide est décalé dans le futur (L409-417)

### Cohérence
- Utilise `VegafoXColors.Surface` pour le container — OK
- Utilise `BebasNeue` 24sp pour le titre — OK
- `VegafoXButton` Ghost pour dismiss — OK
- **Pas de VegafoXButton pour les items de date** — simples `Row` cliquables, pas de retour focus D-pad clair

---

## Résumé des Problèmes Critiques

### Sévérité HAUTE

| # | Problème | Fichiers | Impact |
|---|----------|----------|--------|
| H1 | 3 Browse fragments utilisent JellyfinTheme au lieu de VegafoXColors/Typography | RecordingsBrowse, ScheduleBrowse, SeriesRecordingsBrowse | Incohérence design system complète |
| H2 | 3 Browse fragments n'utilisent pas VegafoXScaffold | RecordingsBrowse, ScheduleBrowse, SeriesRecordingsBrowse | Pas de sidebar premium |
| H3 | LiveTvBrowseScreen n'utilise pas VegafoXScaffold | LiveTvBrowseScreen.kt | Pas de sidebar premium |
| H4 | Row simple au lieu de LazyRow pour les programmes | LiveTvGuideGrid.kt | Performance — toutes les cellules composées |

### Sévérité MOYENNE

| # | Problème | Fichiers | Impact |
|---|----------|----------|--------|
| M1 | AlertDialog.Builder natif Android pour confirmation | ProgramDetailDialog.kt:308 | Incohérence dialog |
| M2 | CheckboxRow sans retour visuel d'état | RecordDialog.kt:291-303 | UX — impossible de voir si activé |
| M3 | Pas de contentType sur LazyColumn items | LiveTvGuideGrid.kt:114 | Performance recyclage |
| M4 | indexOf O(n) dans items body | LiveTvGuideGrid.kt:118 | Performance |
| M5 | Color.Red hardcodé pour enregistrement | ProgramCellComposable.kt:244 | Devrait être VegafoXColors |
| M6 | Pas de focus initial sur programme en cours | LiveTvGuideGrid.kt | UX TV |
| M7 | 3 Browse fragments n'utilisent pas BrowseHeader | Recordings/Schedule/SeriesRecordings | Incohérence header |
| M8 | 3 Browse fragments n'utilisent pas BrowseMediaCard | Recordings/Schedule/SeriesRecordings | Cartes custom non standardisées |

### Sévérité BASSE

| # | Problème | Fichiers | Impact |
|---|----------|----------|--------|
| B1 | ~15 Color.White.copy(alpha=X) hardcodés | Multiple | Devrait être tokens VegafoX |
| B2 | RoundedCornerShape(16.dp) hardcodé ×3 | ProgramDetailDialog, RecordDialog | Devrait être token shape |
| B3 | Dimensions padding hardcodées (24.dp, 80.dp, etc.) | LiveTvGuideScreen header | Devrait être LiveTvDimensions |
| B4 | AsyncImage ChannelHeader sans error fallback | ChannelHeaderComposable.kt | Écran vide si erreur image |
| B5 | LocalDateTime.now() non-mémorisé | ProgramCell L56, GuideGrid L86 | Recalculs inutiles |
| B6 | Items date picker non focusables D-pad | LiveTvGuideScreen GuideDatePickerDialog | Accessibilité TV |
| B7 | ProgramDetailDialog manque durée, rating, casting | ProgramDetailDialog.kt | Infos incomplètes |
