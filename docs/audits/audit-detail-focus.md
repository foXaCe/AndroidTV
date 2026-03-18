# Audit Focus & Scroll — Page Détail VegafoX

**Date** : 2026-03-15
**Fichiers audités** :
- `ui/itemdetail/compose/ItemDetailScreen.kt`
- `ui/itemdetail/v2/content/MovieDetailsContent.kt`
- `ui/itemdetail/v2/content/SeriesDetailsContent.kt`
- `ui/itemdetail/v2/content/SeasonDetailsContent.kt`
- `ui/itemdetail/v2/shared/DetailActions.kt`
- `ui/itemdetail/v2/shared/DetailSections.kt`
- (Composants enfants analysés : `VegafoXButton`, `VegafoXIconButton`, `CinemaActionChip`, `EpisodeCard`, `SeasonCard`, `CastCard`, `SimilarItemCard`, `LandscapeItemCard`, `CinemaPosterColumn`, `CinemaGenreTag`, `MediaMetadataBadges`, `TrackSelectorDialog`)

---

## 1 — Inventaire complet des éléments focusables

### 1.1 — ItemDetailScreen.kt

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 76 | `contentFocusRequester = remember { FocusRequester() }` | FocusRequester créé | — | Passé à tous les content composables comme paramètre. N'est pas directement attaché à un composable dans ce fichier. |

**Aucun élément directement focusable dans ce fichier.** Le focus est délégué aux composables enfants.

### 1.2 — MovieDetailsContent.kt

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 94 | `titleFocusRequester = contentFocusRequester` | Alias FocusRequester | — | **Non attaché** à aucun composable (pas de `.focusRequester(titleFocusRequester)` dans ce fichier). Variable déclarée mais inutilisée. |
| 98 | `playButtonFocusRequester` | `remember { FocusRequester() }` | Oui | Attaché au bouton Play/Resume via `.focusRequester()`. |
| 259 | `Row` (primary actions) | `.focusGroup()` | Oui | Groupe de focus contenant Play/Resume + Restart. |
| 262-269 | `VegafoXButton` Resume | `.focusRequester(playButtonFocusRequester)` + `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Oui | Focusable via VegafoXButton interne. |
| 270-276 | `VegafoXButton` Restart | `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Oui | |
| 278-285 | `VegafoXButton` Play | `.focusRequester(playButtonFocusRequester)` + `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Oui | Alternative à Resume quand pas de position de lecture. |
| 294 | `Row` (secondary actions) | `.focusGroup()` | Oui | Groupe de focus contenant Audio, Subtitles, Versions, Trailers, Favorite, Watched, Delete. |
| 297-304 | `VegafoXButton` Audio | `.focusable()` etc. | Conditionnel | Visible seulement si `audioStreams.size > 1`. |
| 308-315 | `VegafoXButton` Subtitles | `.focusable()` etc. | Conditionnel | Visible seulement si `subtitleStreams.isNotEmpty()`. |
| 319-327 | `VegafoXButton` Versions | `.focusable()` etc. | Conditionnel | Visible seulement si `hasMultipleVersions`. |
| 330-338 | `VegafoXButton` Trailers | `.focusable()` etc. | Conditionnel | Visible seulement si `hasPlayableTrailers`. |
| 341-353 | `VegafoXButton` Favorite | `.focusable()` etc. | Conditionnel | Visible si `item.userData != null`. |
| 356-365 | `VegafoXIconButton` Watched | `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Conditionnel | Visible si `userData != null && type != PERSON`. |
| 369-377 | `VegafoXButton` Delete | `.focusable()` etc. | Conditionnel | Visible si `item.canDelete == true`. |

**Sections sous le hero (hors écran initial, scrollables)** :

| Ligne | Section | Composables focusables | Visible au chargement |
|-------|---------|----------------------|----------------------|
| 420 | `DetailEpisodesHorizontalSection` | `EpisodeCard` × N (chacun `.clickable()` donc focusable) | Non — sous la hero zone |
| 445 | `DetailCollectionItemsGrid` | `SimilarItemCard` × N (chacun `.clickable()`) | Non — sous la hero zone |
| 465 | `DetailCastSection` | `CastCard` × N (chacun `.clickable()`) | Non — sous la hero zone |
| 481 | `DetailSectionWithCards` (Similar) | `SimilarItemCard` × N (chacun `.clickable()`) | Non — sous la hero zone |

### 1.3 — SeriesDetailsContent.kt

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 73 | `playButtonFocusRequester` | `remember { FocusRequester() }` | — | Passé à `DetailActionButtonsRow`. |
| 74 | `titleFocusRequester = contentFocusRequester` | Alias | — | **Attaché** à un `Box` (ligne 139-142) via `.focusRequester(titleFocusRequester)`. Ce Box contient le titre mais n'a PAS de `.focusable()` — donc l'attachement est là mais le Box n'est pas focusable par lui-même. |
| 139-142 | `Box` (titre) | `.focusRequester(titleFocusRequester)` | Oui | **PROBLÈME** : FocusRequester attaché à un Box non-focusable. |
| 191 | `DetailActionButtonsRow` | Contient les boutons d'action | Oui | Voir section 1.5. |

**Sections sous le hero** :

| Ligne | Section | Composables focusables | Visible au chargement |
|-------|---------|----------------------|----------------------|
| 238 | `DetailSectionWithCards` (Next Up) | `LandscapeItemCard` × N (`.clickable()`) | Non |
| 260 | `DetailSeasonsSection` | `SeasonCard` × N (`.clickable()`) | Non |
| 276 | `DetailCastSection` | `CastCard` × N (`.clickable()`) | Non |
| 292 | `DetailSectionWithCards` (Similar) | `SimilarItemCard` × N (`.clickable()`) | Non |

### 1.4 — SeasonDetailsContent.kt

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 75 | `titleFocusRequester = contentFocusRequester` | Alias | — | **Attaché** à un `Box` (ligne 136-140) via `.focusRequester(titleFocusRequester)`. Ce Box n'est PAS focusable. |
| 136-140 | `Box` (titre) | `.focusRequester(titleFocusRequester)` | Oui | **PROBLÈME** : FocusRequester attaché à un Box non-focusable. |
| 191 | `Row` (primary actions) | `.focusGroup()` | Oui | |
| 194-204 | `VegafoXButton` Play | `.focusable()` etc. | Conditionnel | Visible si `episodes.isNotEmpty()`. **PAS de FocusRequester** — aucun bouton ne reçoit le focus automatique. |
| 213 | `Row` (secondary actions) | `.focusGroup()` | Oui | |
| 215-226 | `CinemaActionChip` Watched | `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Oui | |
| 228-239 | `CinemaActionChip` Favorite | `.focusable()` + `.onFocusChanged` + `.clickable` + `.onKeyEvent` | Oui | |

**Sections sous le hero** :

| Ligne | Section | Composables focusables | Visible au chargement |
|-------|---------|----------------------|----------------------|
| 282 | `DetailEpisodesHorizontalSection` | `EpisodeCard` × N (`.clickable()`) | Non |

### 1.5 — DetailActions.kt (DetailActionButtonsRow)

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 98 | `Row` (primary) | `.focusGroup()` | Oui | |
| 101-108 | `VegafoXButton` Resume | `.focusRequester(playButtonFocusRequester)` + `.focusable()` | Conditionnel | Si `canResume && canPlay`. |
| 109-115 | `VegafoXButton` Restart | `.focusable()` | Conditionnel | |
| 117-124 | `VegafoXButton` Play | `.focusRequester(playButtonFocusRequester)` + `.focusable()` | Conditionnel | Si `!canResume && canPlay`. |
| 128-136 | `VegafoXButton` Shuffle | `.focusable()` | Conditionnel | Si folder && !BoxSet. |
| 139-147 | `VegafoXButton` Instant Mix | `.focusable()` | Conditionnel | Si MUSIC_ARTIST. |
| 155 | `Row` (secondary) | `.focusGroup()` | Oui | |
| 158-165 | `VegafoXButton` Versions | `.focusable()` | Conditionnel | |
| 169-176 | `VegafoXButton` Audio | `.focusable()` | Conditionnel | |
| 179-187 | `VegafoXButton` Subtitles | `.focusable()` | Conditionnel | |
| 190-199 | `VegafoXButton` Trailers | `.focusable()` | Conditionnel | |
| 201-213 | `VegafoXButton` Favorite | `.focusable()` | Conditionnel | |
| 216-227 | `VegafoXIconButton` Watched | `.focusable()` | Conditionnel | |
| 229-237 | `VegafoXButton` Playlist | `.focusable()` | Conditionnel | |
| 241-249 | `VegafoXButton` Go to Series | `.focusable()` | Conditionnel | |
| 252-260 | `VegafoXButton` Delete | `.focusable()` | Conditionnel | |

### 1.6 — DetailSections.kt

| Ligne | Composable | Type de focus | Visible | Notes |
|-------|-----------|--------------|---------|-------|
| 81-96 | `LazyRow` (Seasons) | `SeasonCard` × N (`.clickable()` = focusable implicite) | Sous hero | |
| 109-126 | `LazyRow` (Episodes) | `EpisodeCard` × N (`.clickable()`) | Sous hero | |
| 137-158 | `LazyRow` (Cast) | `CastCard` × N (`.clickable()`) | Sous hero | |
| 173-209 | `LazyRow` (SectionWithCards) | `SimilarItemCard` ou `LandscapeItemCard` × N (`.clickable()`) | Sous hero | Option `firstItemFocusRequester` pour focus du premier item. |
| 222-245 | `FlowRow` (CollectionItemsGrid) | `.focusGroup()` + `SimilarItemCard` × N (`.clickable()`) | Sous hero | Option `firstItemFocusRequester`. |

### 1.7 — Résumé des composants enfants focusables

| Composant | Mécanisme de focus | Utilisé dans |
|-----------|-------------------|-------------|
| `VegafoXButton` | `.focusRequester()` interne + `.onFocusChanged` + `.focusable(enabled)` + `.onKeyEvent` + `.clickable` | MovieDetails, SeriesDetails (via DetailActionButtonsRow), SeasonDetails |
| `VegafoXIconButton` | `.onFocusChanged` + `.focusable(enabled)` + `.onKeyEvent` + `.clickable` | MovieDetails, DetailActionButtonsRow |
| `CinemaActionChip` | `.onFocusChanged` + `.focusable()` + `.onKeyEvent` + `.clickable` | SeasonDetails |
| `EpisodeCard` | `.clickable(interactionSource)` (focusable implicite via interactionSource) | Sections épisodes |
| `SeasonCard` | `.clickable(interactionSource)` (focusable implicite) | SeriesDetails sections |
| `CastCard` | `.clickable(interactionSource)` (focusable implicite) | Sections cast |
| `SimilarItemCard` | `.clickable(interactionSource)` (focusable implicite) | Sections similar |
| `LandscapeItemCard` | `.clickable(interactionSource)` (focusable implicite) | Sections Next Up |
| `CinemaGenreTag` | **Non focusable** | Tous les content |
| `CinemaPosterColumn` | **Non focusable** | Tous les content |
| `MediaMetadataBadges` | **Non focusable** | MovieDetails, SeriesDetails |
| `SectionHeader` | **Non focusable** | Toutes les sections |

---

## 2 — Inventaire complet des LaunchedEffect et SideEffect

### 2.1 — MovieDetailsContent.kt

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| 130-145 | `LaunchedEffect(item.id)` | 1. `delay(100)` → `showContent = true` (déclenche AnimatedVisibility). 2. `delay(500)` → `playButtonFocusRequester.requestFocus()` (focus le bouton Play). 3. Boucle 5× (`delay(50)` + `listState.scrollToItem(0, 0)`) pour contrer le scroll automatique "bring into view". | Chaque fois que `item.id` change (chargement initial + navigation intra-page). |
| 511 | `LaunchedEffect(Unit)` | Toast + `showAudioDialog = false` | Quand le dialog audio s'ouvre mais la liste de pistes audio est vide. |

**VegafoXButton interne** (dans le composable VegafoXButton.kt) :

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| 181-186 | `LaunchedEffect(isPressed)` | `delay(80)` → `isPressed = false` | Chaque pression sur le bouton. |
| 196-201 | `LaunchedEffect(navigating)` | `delay(400)` → `navigating = false` | Anti double-clic. |
| 338-342 | `LaunchedEffect(Unit)` (si `autoFocus`) | `delay(500)` → `focusRequester.requestFocus()` | Seulement si `autoFocus = true`. **Aucun bouton de la page détail n'utilise `autoFocus = true`.** |

### 2.2 — SeriesDetailsContent.kt

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| 84-96 | `LaunchedEffect(item.id)` | **Identique à MovieDetailsContent** : 1. `delay(100)` → `showContent = true`. 2. `delay(500)` → `playButtonFocusRequester.requestFocus()`. 3. Boucle 5× scroll reset. | Changement de `item.id`. |

### 2.3 — SeasonDetailsContent.kt

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| 82-94 | `LaunchedEffect(item.id)` | 1. `delay(100)` → `showContent = true`. 2. `delay(500)` → **`titleFocusRequester.requestFocus()`** (pas le play button !). 3. Boucle 5× scroll reset. | Changement de `item.id`. |

### 2.4 — DetailActions.kt (DetailActionButtonsRow)

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| 270-271 | `LaunchedEffect(Unit)` | Toast + `showAudioDialog = false` | Dialog audio ouvert mais pistes vides. |

### 2.5 — DetailSections.kt

| Ligne | Clé | Action | Déclenchement |
|-------|-----|--------|---------------|
| — | Aucun LaunchedEffect | — | — |

### 2.6 — ItemDetailsComponents.kt (composants enfants)

| Composant | Ligne | Clé | Action | Déclenchement |
|-----------|-------|-----|--------|---------------|
| `SimilarItemCard` | 902-904 | `LaunchedEffect(isFocused)` | `onFocused?.invoke()` | Quand la carte reçoit le focus. |
| `LandscapeItemCard` | 981-983 | `LaunchedEffect(isFocused)` | `onFocused?.invoke()` | Quand la carte reçoit le focus. |
| `CinemaActionChip` | 1653-1658 | `LaunchedEffect(navigating)` | `delay(400)` → `navigating = false` | Anti double-clic. |
| `TrackSelectorDialog` | 1320-1322 | `LaunchedEffect(Unit)` | `initialFocusRequester.requestFocus()` | À l'ouverture du dialog, focus sur l'option sélectionnée. |

---

## 3 — Inventaire complet des scrollState et LazyListState

### 3.1 — MovieDetailsContent.kt

| Ligne | Type | Variable | Scroll auto | Déclencheur |
|-------|------|----------|-------------|-------------|
| 91 | `LazyListState` | `listState = rememberLazyListState()` | **Oui, forcé à 0** | Le `LaunchedEffect(item.id)` exécute `listState.scrollToItem(0, 0)` 5 fois avec `delay(50)` après le focus du bouton Play. Ceci est un **workaround** pour contrer le scroll automatique "bring into view" déclenché par Compose quand un élément focusé est partiellement hors écran. |

### 3.2 — SeriesDetailsContent.kt

| Ligne | Type | Variable | Scroll auto | Déclencheur |
|-------|------|----------|-------------|-------------|
| 72 | `LazyListState` | `listState = rememberLazyListState()` | **Oui, forcé à 0** | Identique à MovieDetailsContent — boucle 5× `scrollToItem(0, 0)`. |

### 3.3 — SeasonDetailsContent.kt

| Ligne | Type | Variable | Scroll auto | Déclencheur |
|-------|------|----------|-------------|-------------|
| 74 | `LazyListState` | `listState = rememberLazyListState()` | **Oui, forcé à 0** | Identique — boucle 5× `scrollToItem(0, 0)`. |

### 3.4 — DetailSections.kt

| Ligne | Type | Variable | Scroll auto | Déclencheur |
|-------|------|----------|-------------|-------------|
| 81 | `LazyRow` interne (Seasons) | État implicite | Non | Scroll horizontal utilisateur. |
| 109 | `LazyRow` interne (Episodes) | État implicite | Non | Scroll horizontal utilisateur. |
| 137 | `LazyRow` interne (Cast) | État implicite | Non | Scroll horizontal utilisateur. |
| 173 | `LazyRow` interne (SectionWithCards) | État implicite | Non | Scroll horizontal utilisateur. |

### 3.5 — TrackSelectorDialog

| Ligne | Type | Variable | Scroll auto | Déclencheur |
|-------|------|----------|-------------|-------------|
| 1245 | `LazyColumn` interne | État implicite | Non | Mais le `LaunchedEffect(Unit)` focus l'item sélectionné, ce qui peut déclencher un "bring into view" scroll. |

---

## 4 — Ordre de composition et d'activation des focus

### 4.1 — Séquence complète pour MovieDetailsContent

```
T+0ms    : Composition initiale
           - LazyColumn créé avec listState à position 0
           - showContent = false → AnimatedVisibility masque le hero
           - playButtonFocusRequester créé (pas encore attaché car AnimatedVisibility=false)
           - LaunchedEffect(item.id) démarre

T+100ms  : showContent = true
           - AnimatedVisibility entre : fadeIn + slideInVertically (350ms)
           - Les boutons d'action sont maintenant composés et attachés au tree
           - playButtonFocusRequester est attaché au VegafoXButton Play/Resume
           - TOUS les boutons sont maintenant focusables

T+100-450ms : Animation d'entrée en cours (350ms)
           - Les boutons existent dans le tree mais sont en train de slider vers le haut
           - Aucun élément n'a le focus

T+600ms  : playButtonFocusRequester.requestFocus()
           - Le bouton Play/Resume reçoit le focus
           - ⚠️ EFFET SECONDAIRE : Compose déclenche automatiquement "bring into view"
             pour s'assurer que l'élément focusé est visible
           - Si le bouton est en bas de la hero zone (580dp), le LazyColumn
             peut scroller vers le bas pour le rendre visible

T+650ms  : Boucle anti-scroll #1 : listState.scrollToItem(0, 0)
T+700ms  : Boucle anti-scroll #2 : listState.scrollToItem(0, 0)
T+750ms  : Boucle anti-scroll #3 : listState.scrollToItem(0, 0)
T+800ms  : Boucle anti-scroll #4 : listState.scrollToItem(0, 0)
T+850ms  : Boucle anti-scroll #5 : listState.scrollToItem(0, 0)

T+850ms  : ÉTAT FINAL
           - Position scroll : item 0, offset 0 (top)
           - Focus : bouton Play/Resume
           - Hero zone visible avec backdrop + titre + boutons
           - Sections cast/similar hors écran (sous le hero)
```

### 4.2 — Séquence pour SeriesDetailsContent

**Identique à MovieDetailsContent**, sauf :
- Le focus est donné à `playButtonFocusRequester` via `DetailActionButtonsRow`
- Le `titleFocusRequester` est attaché à un Box non-focusable (inutilisé)

### 4.3 — Séquence pour SeasonDetailsContent

```
T+0ms    : Composition initiale
           - showContent = false, listState à 0

T+100ms  : showContent = true → animation d'entrée

T+600ms  : titleFocusRequester.requestFocus()
           - ⚠️ PROBLÈME CRITIQUE : titleFocusRequester est attaché à un Box qui
             n'a PAS de .focusable(). Le requestFocus() est wrappé dans try/catch
             donc il échoue silencieusement.
           - RÉSULTAT : AUCUN élément n'a le focus après le chargement.
           - L'utilisateur doit appuyer sur une touche D-pad pour que Compose
             assigne le focus au premier élément focusable (le bouton Play s'il existe,
             ou le CinemaActionChip Watched).

T+650-850ms : Boucle anti-scroll (inutile car pas de focus = pas de bring-into-view)

T+850ms  : ÉTAT FINAL
           - Position scroll : item 0, offset 0
           - Focus : AUCUN ← PROBLÈME
           - Le premier appui D-pad focus un élément imprévisible
```

---

## 5 — Problèmes identifiés

### 5.1 — CRITIQUE : SeasonDetailsContent n'a pas de focus initial

**Fichier** : `SeasonDetailsContent.kt:82-94`
**Description** : Le `LaunchedEffect` tente `titleFocusRequester.requestFocus()` sur un `Box` qui n'est pas focusable (pas de `.focusable()` ni de `.clickable()`). Le `try/catch` avale l'exception silencieusement.
**Impact** : Aucun élément n'a le focus au chargement de la page Season. L'utilisateur doit appuyer sur D-pad pour initialiser le focus. Le premier élément focusé sera imprévisible (probablement le bouton Play ou le premier CinemaActionChip, mais cela dépend de l'algorithme interne de Compose).
**Correction suggérée** : Créer un `playButtonFocusRequester` dédié et l'attacher au VegafoXButton Play, comme dans MovieDetailsContent.

### 5.2 — CRITIQUE : SeriesDetailsContent — titleFocusRequester attaché à un Box non-focusable

**Fichier** : `SeriesDetailsContent.kt:139-142`
**Description** : `titleFocusRequester` (alias de `contentFocusRequester`) est attaché via `.focusRequester()` à un `Box` sans `.focusable()`. Cependant ce n'est pas celui qui reçoit `requestFocus()` — c'est `playButtonFocusRequester` qui est utilisé à la ligne 89. L'attachement du titleFocusRequester est donc un **code mort** qui n'a aucun effet mais peut être source de confusion.
**Impact** : Faible, mais code trompeur.

### 5.3 — MODÉRÉ : MovieDetailsContent — titleFocusRequester déclaré mais non utilisé

**Fichier** : `MovieDetailsContent.kt:94`
**Description** : `val titleFocusRequester = contentFocusRequester` est déclaré mais jamais attaché à aucun composable et jamais utilisé pour `requestFocus()`. Code mort.
**Impact** : Aucun impact fonctionnel, mais le `contentFocusRequester` passé depuis `ItemDetailScreen` est gaspillé.

### 5.4 — MODÉRÉ : Workaround fragile de scroll reset (5× scrollToItem)

**Fichier** : `MovieDetailsContent.kt:141-144`, `SeriesDetailsContent.kt:92-95`, `SeasonDetailsContent.kt:89-93`
**Description** : Le pattern de boucle 5× `delay(50) + scrollToItem(0, 0)` est un workaround pour contrer le "bring into view" automatique de Compose après `requestFocus()`. Ce workaround est :
1. **Fragile** : dépend du timing. Sur un appareil lent, 5 itérations × 50ms = 250ms pourrait ne pas suffire.
2. **Race condition** : Le "bring into view" scroll est asynchrone et peut se déclencher après les 5 tentatives de reset.
3. **Visible à l'utilisateur** : Sur certains appareils, le scroll vers le bas puis le snap back à 0 est visible comme un "flash" ou un jitter.
**Correction suggérée** : Utiliser `BringIntoViewResponder` personnalisé pour désactiver le comportement, ou utiliser `focusProperties { canFocus = false }` sur le conteneur, ou positionner les boutons dans la partie visible du viewport initial.

### 5.5 — MODÉRÉ : Incohérence du focus cible entre les types de contenu

| Type de contenu | Focus initial | Mécanisme |
|-----------------|--------------|-----------|
| Movie/Episode/Video | Bouton Play/Resume | `playButtonFocusRequester.requestFocus()` ✅ |
| Series | Bouton Play/Resume (via DetailActionButtonsRow) | `playButtonFocusRequester.requestFocus()` ✅ |
| Season | **AUCUN** (titleFocusRequester sur Box non-focusable) | `titleFocusRequester.requestFocus()` ❌ |

**Impact** : Expérience incohérente — tous les types sauf Season ont un focus automatique sur le bouton principal.

### 5.6 — MINEUR : Double implémentation des boutons d'action (MovieDetailsContent vs DetailActionButtonsRow)

**Fichier** : `MovieDetailsContent.kt:257-378` vs `DetailActions.kt:94-263`
**Description** : MovieDetailsContent implémente ses propres boutons d'action inline, tandis que SeriesDetailsContent utilise `DetailActionButtonsRow`. Les deux implémentations sont quasi-identiques mais divergent légèrement :
- MovieDetailsContent n'a PAS de bouton Shuffle, Instant Mix, Playlist, Go to Series
- DetailActionButtonsRow a TOUS les boutons avec conditions
- Les deux gèrent leurs propres dialogs (audio/subtitle/version) séparément
**Impact** : Duplication de code, risque de divergence comportementale.

### 5.7 — MINEUR : VegafoXButton a un FocusRequester interne ET accepte un external via modifier

**Fichier** : `VegafoXButton.kt:89` et usage via `modifier = Modifier.focusRequester(playButtonFocusRequester)`
**Description** : VegafoXButton crée un `FocusRequester` interne (ligne 89) et a aussi `.focusRequester(focusRequester)` (ligne 289). Quand le parent passe `Modifier.focusRequester(playButtonFocusRequester)`, celui-ci est appliqué en premier dans la chaîne de modifiers (via `modifier` paramètre), puis le FocusRequester interne est aussi attaché.
**Impact** : Deux FocusRequesters sont attachés au même composable. Le `requestFocus()` du parent fonctionne car le modifier externe est traité en premier. Le FocusRequester interne n'est utilisé que pour `autoFocus`. Pas de conflit car les deux ciblent le même node. Mais cela pourrait créer de la confusion.

### 5.8 — MINEUR : Navigation D-pad verticale entre focusGroup de boutons

**Description** : Les boutons primaires (Play/Resume) et secondaires (Audio, Subtitles, etc.) sont dans deux `Row` séparées avec `.focusGroup()`. La navigation D-pad bas depuis le bouton Play descend vers la Row secondaire, puis vers les sections (Episodes, Cast, Similar).
**Comportement observé attendu** :
- D-pad ↓ depuis Play → premier bouton de la Row secondaire (Audio ou Versions selon les conditions)
- D-pad ↓ depuis la Row secondaire → première carte de la première section visible (EpisodeCard ou CastCard)
- D-pad ↑ depuis les sections → retour vers les boutons secondaires
- D-pad ↑ depuis les boutons secondaires → bouton Play

**Problème potentiel** : Si la Row secondaire est vide (aucun bouton conditionnel n'est visible), la navigation D-pad ↓ depuis Play saute directement aux sections sous le hero, ce qui déclenche un scroll. Ce n'est pas un bug mais peut surprendre l'utilisateur.

### 5.9 — MINEUR : CinemaPosterColumn non-focusable dans la même Row que les boutons

**Description** : Dans MovieDetailsContent, SeriesDetailsContent et SeasonDetailsContent, le `CinemaPosterColumn` est dans la même `Row` que la colonne de gauche (titre + boutons). Le poster n'est pas focusable, ce qui est correct. Mais cela signifie que la navigation D-pad → depuis le dernier bouton de la Row ne va nulle part (le poster ne capture pas le focus).
**Impact** : Correct du point de vue UX car le poster n'est pas interactif, mais la navigation droite "meurt" silencieusement.

### 5.10 — INFO : Timing total de chargement

| Phase | Durée | Cumul |
|-------|-------|-------|
| Composition initiale | ~0ms | 0ms |
| delay avant animation | 100ms | 100ms |
| Animation entrée | 350ms | 450ms |
| delay avant focus | 500ms | 600ms |
| requestFocus() | ~0ms | 600ms |
| Anti-scroll boucle (5×50ms) | 250ms | 850ms |
| **Total** | — | **~850ms** |

L'utilisateur doit attendre ~850ms avant que la page soit dans son état final stable. Pendant ce temps, toute interaction D-pad peut créer des comportements imprévisibles car le focus et le scroll sont en train d'être manipulés par le LaunchedEffect.

---

## Résumé des priorités

| # | Sévérité | Problème | Fichier |
|---|----------|----------|---------|
| 5.1 | **CRITIQUE** | Season n'a aucun focus initial | SeasonDetailsContent.kt |
| 5.2 | **CRITIQUE** | Series titleFocusRequester sur Box non-focusable (code mort) | SeriesDetailsContent.kt |
| 5.4 | **MODÉRÉ** | Workaround 5× scrollToItem fragile et visible | 3 fichiers |
| 5.5 | **MODÉRÉ** | Incohérence du focus entre types de contenu | Architecture |
| 5.3 | **MODÉRÉ** | titleFocusRequester inutilisé dans Movie | MovieDetailsContent.kt |
| 5.6 | **MINEUR** | Duplication boutons d'action Movie vs shared | MovieDetailsContent.kt + DetailActions.kt |
| 5.7 | **MINEUR** | Double FocusRequester dans VegafoXButton | VegafoXButton.kt |
| 5.8 | **MINEUR** | Navigation D-pad si Row secondaire vide | Architecture |
| 5.9 | **MINEUR** | D-pad → "meurt" sur le poster | Architecture |
