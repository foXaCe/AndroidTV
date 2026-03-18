# Audit — Detail Scroll State (audit-detail-scroll-03)

Date : 2026-03-16
Fichiers audités :
- `ui/itemdetail/v2/content/MovieDetailsContent.kt`
- `ui/itemdetail/v2/content/SeriesDetailsContent.kt`
- `ui/itemdetail/v2/content/SeasonDetailsContent.kt`
- `ui/itemdetail/v2/shared/DetailSections.kt`
- `ui/itemdetail/v2/shared/DetailUtils.kt`
- `ui/itemdetail/v2/ItemDetailsComponents.kt`
- `ui/base/theme/VegafoXDimensions.kt`

---

## Point 1 — État de la LazyColumn

### MovieDetailsContent.kt

```kotlin
LazyColumn(
    state = listState,           // rememberLazyListState()
    modifier = Modifier.fillMaxSize(),
)
```

- **Pas de `flingBehavior`** custom — utilise le fling par défaut de Compose.
- **`disableBringIntoView()`** : Appliqué sur le `Box` du hero zone (l.155), **pas sur la LazyColumn elle-même**. Le hero item a `.disableBringIntoView()` via un `BringIntoViewResponder` custom `NoOpBringIntoViewResponder` (DetailUtils.kt:22-30) qui retourne `Rect.Zero` et ne fait rien dans `bringChildIntoView()`.
- **Pas de `LaunchedEffect` avec `animateScrollBy` négatif** — confirmé supprimé. Aucun appel `animateScrollBy` ou `animateScrollToItem` dans tout le dossier `v2/`.
- Le `LaunchedEffect(item.id)` restant (l.131-139) ne fait que déclencher l'animation d'entrée et `requestFocus()` sur le bouton Play.

### SeriesDetailsContent.kt

Configuration identique :
```kotlin
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
)
```
- `disableBringIntoView()` sur le Box du hero (l.108).
- Pas de flingBehavior custom.
- Pas de animateScrollBy.

### SeasonDetailsContent.kt

Configuration identique :
```kotlin
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
)
```
- `disableBringIntoView()` sur le Box du hero (l.107).
- Pas de flingBehavior custom.
- Pas de animateScrollBy.

### Verdict Point 1

`disableBringIntoView()` est correctement appliqué sur le hero zone dans les 3 fichiers. Cependant, **il n'est appliqué que sur le hero item**, pas sur les items de section en dessous. Quand une carte dans un LazyRow reçoit le focus par navigation D-pad, le mécanisme natif `BringIntoView` de Compose va tenter de scroller la LazyColumn parente pour rendre l'item focalisé visible — c'est le comportement attendu pour les sections sous le hero, donc OK.

---

## Point 2 — Structure des items

### MovieDetailsContent.kt — Items dans l'ordre

| # | Type | Hauteur | Notes |
|---|------|---------|-------|
| 1 | Hero zone | **360dp fixe** (`HeroDimensions.backdropHeight`) | `disableBringIntoView()` |
| 2 | Gradient | **40dp fixe** (`DetailDimensions.gradientHeight`) | Transition transparente → fond |
| 3 | Episodes header (si EPISODE) | **Dynamique** — `SectionHeader` wraps content | Texte seul |
| 4 | Episodes LazyRow (si EPISODE) | **Dynamique** — `DetailEpisodesHorizontalSection` wraps | ~155dp (124dp image + padding + texte) |
| 5 | BoxSet grid (si BOX_SET) | **Dynamique** — `FlowRow` wraps content | Hauteur variable selon nombre d'items |
| 6 | Cast header | **Dynamique** — `SectionHeader` | Texte seul |
| 7 | Cast LazyRow | **Dynamique** — `DetailCastSection` wraps | ~110dp (hauteur fixe de CastCard) |
| 8 | Similar header | **Dynamique** — `SectionHeader` | Texte seul |
| 9 | Similar LazyRow | **Dynamique** — `DetailSectionWithCards` wraps | ~240dp (200dp image + titre + année) |
| 10 | Bottom padding | **80dp fixe** (`DetailDimensions.bottomPadding`) | Spacer |

### SeriesDetailsContent.kt — Items dans l'ordre

| # | Type | Hauteur | Notes |
|---|------|---------|-------|
| 1 | Hero zone | **360dp fixe** | `disableBringIntoView()` |
| 2 | Gradient | **40dp fixe** | |
| 3 | Next Up header (si présent) | **Dynamique** | |
| 4 | Next Up LazyRow (si présent) | **Dynamique** | LandscapeItemCard ~155dp |
| 5 | Seasons header (si présent) | **Dynamique** | |
| 6 | Seasons LazyRow (si présent) | **Dynamique** | SeasonCard ~280dp (255dp image + spacer + texte) |
| 7 | Cast header | **Dynamique** | |
| 8 | Cast LazyRow | **Dynamique** | ~110dp |
| 9 | Similar header | **Dynamique** | |
| 10 | Similar LazyRow | **Dynamique** | ~240dp |
| 11 | Bottom padding | **80dp fixe** | |

### SeasonDetailsContent.kt — Items dans l'ordre

| # | Type | Hauteur | Notes |
|---|------|---------|-------|
| 1 | Hero zone | **360dp fixe** | `disableBringIntoView()` |
| 2 | Gradient | **40dp fixe** | |
| 3 | Episodes header (si présent) | **Dynamique** | |
| 4 | Episodes LazyRow (si présent) | **Dynamique** | ~155dp |
| 5 | Bottom padding | **80dp fixe** | |

### Découpage header + LazyRow

**Appliqué correctement.** Chaque section est découpée en 2 items séparés dans la LazyColumn :
1. Un item pour le `SectionHeader` (encapsulé dans un `Box`)
2. Un item séparé pour la `LazyRow` (encapsulé dans un `Box`)

Ce découpage permet à la LazyColumn de mesurer et recycler chaque partie indépendamment.

### Hauteurs dynamiques non contraintes

**Problème identifié :** Aucune des `LazyRow` ni des `SectionHeader` n'a de hauteur fixe sur leur item `Box` conteneur. La hauteur est wrap-content. Cependant :
- `CastCard` a une hauteur fixe de **110dp** (hardcodée dans le modifier `.height(110.dp)`)
- `EpisodeCard` a une largeur fixe de **220dp** (`CardDimensions.landscapeWidth`), la hauteur de la Column est wrap-content mais déterministe (image 124dp fixe + padding + texte 1 ligne)
- `SimilarItemCard` a une largeur fixe de **140dp**, image **200dp fixe**, le reste est wrap-content (titre 1 ligne + année optionnelle)
- `SeasonCard` a une image **170x255dp fixe**, le reste est wrap-content
- `LandscapeItemCard` a une image **220x124dp fixe** (`CardDimensions.landscapeWidth/Height`)
- `FlowRow` pour BoxSet a une hauteur entièrement dynamique qui dépend du nombre d'items et de la largeur disponible

Les hauteurs sont **déterministes** car le contenu texte a toujours `maxLines = 1` ou des contraintes fixes. Le seul cas problématique est le **FlowRow de BoxSet** dont la hauteur change si les items ne rentrent pas tous sur une ligne.

---

## Point 3 — Sections et leurs LazyRow

### DetailSeasonsSection (SeasonCard LazyRow)

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(horizontal = 0.dp),
)
```

| Critère | État | Problème |
|---------|------|----------|
| `focusGroup()` | **ABSENT** | La LazyRow n'a pas de focusGroup |
| `focusRestorer()` | **ABSENT** | Pas de focusRestorer |
| Hauteur fixe des cartes | **Partiel** — SeasonCard image = 170x255dp fixe, mais la Column conteneur est wrap-content | Hauteur ~280dp déterministe |
| URLs image `remember` | **NON** — `getPosterUrl(season, api)` appelé directement dans le `items` lambda | Recalculé à chaque recomposition |
| Calculs dynamiques | Aucun `LocalDateTime.now()` | OK |

### DetailEpisodesHorizontalSection (EpisodeCard LazyRow)

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(horizontal = 0.dp),
)
```

| Critère | État | Problème |
|---------|------|----------|
| `focusGroup()` | **ABSENT** | |
| `focusRestorer()` | **ABSENT** | |
| Hauteur fixe des cartes | **Partiel** — EpisodeCard image = 220x124dp fixe, Column wrap-content | Hauteur ~155dp déterministe |
| URLs image `remember` | **NON** — `getPosterUrl(ep, api)` direct | Recalculé à chaque recomposition |
| `formatDuration(it)` | **NON** wrappé dans `remember` | Recalculé mais pure function, impact négligeable |
| Calculs dynamiques | Aucun | OK |

### DetailCastSection (CastCard LazyRow)

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    contentPadding = PaddingValues(horizontal = 0.dp),
)
```

| Critère | État | Problème |
|---------|------|----------|
| `focusGroup()` | **ABSENT** | |
| `focusRestorer()` | **ABSENT** | |
| Hauteur fixe des cartes | **OUI** — CastCard = `width(100.dp).height(110.dp)` | OK |
| URLs image `remember` | **OUI** — `remember(person.id, person.primaryImageTag) { ... }` | OK |
| Calculs dynamiques | Aucun | OK |

### DetailSectionWithCards — Similar / Next Up (SimilarItemCard / LandscapeItemCard LazyRow)

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(horizontal = 0.dp),
)
```

| Critère | État | Problème |
|---------|------|----------|
| `focusGroup()` | **ABSENT** | |
| `focusRestorer()` | **ABSENT** | |
| Hauteur fixe des cartes | **Partiel** — SimilarItemCard image 200dp fixe, LandscapeItemCard image 124dp fixe, columns wrap-content | Déterministe |
| URLs image `remember` | **NON** — `getPosterUrl(item, api)` et `getEpisodeThumbnailUrl(item, api)` directs | Recalculé |
| `LaunchedEffect(isFocused)` | OUI dans SimilarItemCard et LandscapeItemCard pour `onFocused` callback | Léger coût par carte |
| Calculs dynamiques | Aucun | OK |

### DetailCollectionItemsGrid (FlowRow, BoxSet uniquement)

| Critère | État | Problème |
|---------|------|----------|
| `focusGroup()` | **OUI** — `Modifier.focusGroup()` sur FlowRow | OK |
| Hauteur fixe | **NON** — FlowRow hauteur dynamique selon contenu | **Problème potentiel** si le nombre d'items change |
| URLs image `remember` | **NON** — `getPosterUrl(item, api)` direct | Recalculé |

---

## Point 4 — Causes probables des saccades résiduelles

### Cause 1 — CRITIQUE : Absence de `focusGroup()` et `focusRestorer()` sur les LazyRow

**Aucune des 4 LazyRow** de section (Seasons, Episodes, Cast, Similar) n'a de `focusGroup()` ni de `focusRestorer()`. Conséquence :

Quand l'utilisateur navigue au D-pad vers le bas depuis une section et revient, la LazyRow ne sait pas quel item avait le focus précédemment. Le focus system de Compose tente de trouver un item focusable, ce qui peut déclencher :
1. Un repositionnement de la LazyRow (scroll horizontal vers le début)
2. Une remesure de la LazyColumn pour accommoder le bring-into-view de l'item trouvé

### Cause 2 — MODÉRÉE : URLs d'image non mémorisées dans les LazyRow items

`getPosterUrl()`, `getEpisodeThumbnailUrl()` et `formatDuration()` sont appelés à chaque recomposition des items de LazyRow. Bien que ce soient des pure functions (simple string concatenation), l'absence de `remember` signifie que l'objet `String` retourné est une nouvelle instance à chaque recomposition, ce qui pourrait déclencher des rechargements d'image par Coil si l'instance change (equality check).

**Impact** : Potentiellement significatif pour `getPosterUrl` et `getEpisodeThumbnailUrl` car Coil utilise l'URL comme clé de cache. Si la string est recréée identique, Coil devrait la retrouver en cache mémoire. Impact réel probablement faible mais non nul.

**Exception notable** : `DetailCastSection` mémorise correctement l'URL via `remember(person.id, person.primaryImageTag)` — c'est le seul cas correct.

### Cause 3 — FAIBLE : Hauteur wrap-content des items de LazyColumn

Les items de section (header + LazyRow) n'ont pas de hauteur fixe sur leur `Box` conteneur. La hauteur est déterminée par le contenu. Comme le contenu est déterministe (texte 1 ligne, images taille fixe), la remesure ne devrait pas causer de saut visible. Cependant, lors du premier affichage d'un item, la LazyColumn doit mesurer l'item complet pour déterminer sa hauteur, ce qui crée un micro-délai.

### Cause 4 — POTENTIELLE : FlowRow pour BoxSet

Le `DetailCollectionItemsGrid` utilise un `FlowRow` dont la hauteur est entièrement dynamique et dépend du nombre d'items et de la largeur d'écran. Si la collection est grande, cet item peut être très grand et causer des remesures coûteuses quand il entre/sort du viewport de la LazyColumn.

### Cause 5 — NULLE : BringIntoView du hero

`disableBringIntoView()` est correctement implémenté via un `BringIntoViewResponder` no-op appliqué sur le Box du hero. Le `requestFocus()` du LaunchedEffect ne cause plus de scroll parasite. **Ce point est résolu.**

### Cause 6 — NULLE : animateScrollBy négatif

Confirmé supprimé. Aucun appel `animateScrollBy` ou `animateScrollToItem` dans tout le dossier `ui/itemdetail/v2/`. **Ce point est résolu.**

---

## Résumé des actions recommandées

| Priorité | Action | Fichier | Impact attendu |
|----------|--------|---------|----------------|
| P0 | Ajouter `.focusGroup()` et `.focusRestorer()` sur chaque `LazyRow` dans `DetailSections.kt` | `DetailSections.kt` | Élimine les sauts de scroll lors de la navigation D-pad verticale entre sections |
| P1 | Wrapper les appels `getPosterUrl()` / `getEpisodeThumbnailUrl()` dans `remember(item.id)` dans les lambdas `items()` | `DetailSections.kt` | Évite recréation d'URLs et rechargements Coil inutiles |
| P2 | Mettre une hauteur fixe sur les `Box` conteneurs des items LazyRow (`.height(Xdp)`) | `Movie/Series/SeasonDetailsContent.kt` | Pré-alloue l'espace, évite micro-remesure à l'entrée dans le viewport |
| P3 | Évaluer si le `FlowRow` de BoxSet devrait être remplacé par un `LazyVerticalGrid` ou une LazyRow | `DetailSections.kt` | Évite item gigantesque dans la LazyColumn |
