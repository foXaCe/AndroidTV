# Audit Scroll Vertical — Page Détail VegafoX

**Date** : 2026-03-15
**Fichiers audités** :
- `ui/itemdetail/compose/ItemDetailScreen.kt`
- `ui/itemdetail/v2/content/MovieDetailsContent.kt`
- `ui/itemdetail/v2/content/SeriesDetailsContent.kt`
- `ui/itemdetail/v2/content/SeasonDetailsContent.kt`
- `ui/itemdetail/v2/shared/DetailSections.kt`
- `ui/itemdetail/v2/shared/DetailActions.kt`
- `ui/itemdetail/compose/DetailHeroBackdrop.kt`
- `ui/base/theme/VegafoXDimensions.kt`

---

## 1 — Type de conteneur de scroll

### 1.1 — Architecture de la hiérarchie de scroll

```
ItemDetailScreen (Box fillMaxSize — pas de scroll)
 ├── DetailHeroBackdrop (Box fillMaxSize — fixe, pas de scroll, SOUS le contenu en z-order)
 └── *DetailsContent (Box fillMaxSize)
      └── LazyColumn (state = listState, fillMaxSize — SEUL conteneur de scroll vertical)
           ├── item { Hero zone Box }
           ├── item { Gradient Box }
           ├── item { Section 1 (contient LazyRow horizontal) }
           ├── item { Section 2 (contient LazyRow horizontal) }
           ├── ...
           └── item { Spacer bottom padding }
```

### 1.2 — Conteneur principal

| Propriété | Valeur |
|-----------|--------|
| **Type** | `LazyColumn` |
| **Fichier** | `MovieDetailsContent.kt:143`, `SeriesDetailsContent.kt:96`, `SeasonDetailsContent.kt:97` |
| **State** | `rememberLazyListState()` — pas de position initiale configurée |
| **flingBehavior** | **Non spécifié** → `ScrollableDefaults.flingBehavior()` (par défaut) |
| **reverseLayout** | **Non spécifié** → `false` (par défaut) |
| **verticalArrangement** | **Non spécifié** → `Arrangement.Top` (par défaut) |
| **contentPadding** | **Non spécifié** → `PaddingValues(0.dp)` (par défaut) |
| **userScrollEnabled** | **Non spécifié** → `true` (par défaut) |

### 1.3 — Conteneurs de scroll imbriqués

| Niveau | Type | Direction | Conflit |
|--------|------|-----------|---------|
| Parent | `LazyColumn` | Vertical | — |
| Enfant | `LazyRow` (dans chaque section) | Horizontal | **Aucun conflit** — axes orthogonaux |
| Enfant | `FlowRow` (dans `DetailCollectionItemsGrid`) | Aucun scroll | **Aucun conflit** — pas scrollable |

**Pas de scroll vertical imbriqué** : aucun `Column(Modifier.verticalScroll())` ni `LazyColumn` imbriqué dans un item du `LazyColumn` parent. Architecture correcte.

---

## 2 — Sections et leurs composables

### 2.1 — MovieDetailsContent (Movie / Episode / Video / BoxSet)

| # | Section | Composable | Hauteur | Scroll interne | Notes |
|---|---------|-----------|---------|---------------|-------|
| 1 | **Hero zone** | `Box` | **Fixe : 440dp** (`HeroDimensions.backdropHeight`) | Non | Contient AnimatedVisibility, Row(titre+poster), boutons. **C'est un seul item LazyColumn.** |
| 2 | **Gradient** | `Box` | **Fixe : 40dp** (`DetailDimensions.gradientHeight`) | Non | Transition transparent → BackgroundDeep. |
| 3 | **Episodes** (si EPISODE) | `Box` > `DetailEpisodesHorizontalSection` > `Column` > `LazyRow` | **Dynamique** : SectionHeader (~30dp) + LazyRow (CardDimensions.landscapeHeight 124dp + texte ~30dp) ≈ **~184dp** | Oui — `LazyRow` horizontal | Conditionnel |
| 4 | **Collection** (si BOX_SET) | `Box` > `DetailCollectionItemsGrid` > `Column` > `FlowRow` | **Dynamique** : dépend du nombre d'items, FlowRow wrap | Non — FlowRow n'est pas scrollable | Conditionnel. Peut être très haute avec beaucoup d'items. |
| 5 | **Cast & Crew** | `Box` > `DetailCastSection` > `Column` > `LazyRow` | **Dynamique** : SectionHeader + LazyRow (cercle 72dp + texte ~50dp) ≈ **~160dp** | Oui — `LazyRow` horizontal | Conditionnel |
| 6 | **More Like This** | `Box` > `DetailSectionWithCards` > `Column` > `LazyRow` | **Dynamique** : SectionHeader + LazyRow (poster 200dp + texte ~30dp) ≈ **~260dp** | Oui — `LazyRow` horizontal | Conditionnel |
| 7 | **Bottom padding** | `Spacer` | **Fixe : 80dp** (`DetailDimensions.bottomPadding`) | Non | |

**Hauteur totale estimée (film avec cast + similar)** :
440 + 40 + 160 + 260 + 80 = **~980dp** (hors épisodes/collection)

### 2.2 — SeriesDetailsContent

| # | Section | Hauteur | Scroll interne |
|---|---------|---------|---------------|
| 1 | Hero zone | **Fixe : 440dp** | Non |
| 2 | Gradient | **Fixe : 40dp** | Non |
| 3 | Next Up | **Dynamique ~184dp** | Oui — `LazyRow` (landscape cards) |
| 4 | Seasons | **Dynamique ~300dp** | Oui — `LazyRow` (poster 255dp + texte) |
| 5 | Cast & Crew | **Dynamique ~160dp** | Oui — `LazyRow` |
| 6 | More Like This | **Dynamique ~260dp** | Oui — `LazyRow` |
| 7 | Bottom padding | **Fixe : 80dp** | Non |

**Hauteur totale estimée** : ~1460dp

### 2.3 — SeasonDetailsContent

| # | Section | Hauteur | Scroll interne |
|---|---------|---------|---------------|
| 1 | Hero zone | **Fixe : 440dp** | Non |
| 2 | Gradient | **Fixe : 40dp** | Non |
| 3 | Episodes | **Dynamique ~184dp** | Oui — `LazyRow` |
| 4 | Bottom padding | **Fixe : 80dp** | Non |

**Hauteur totale estimée** : ~744dp

### 2.4 — Effet de saut potentiel lié aux hauteurs fixes

| Composable | Hauteur fixe | Risque de saut |
|-----------|-------------|---------------|
| Hero zone Box | 440dp | **OUI** — Cette hauteur est plus grande que le viewport TV typique (~540dp en densité 2x pour 1080p). Le premier item du LazyColumn fait 440dp, ce qui occupe ~81% de l'écran. Quand on D-pad vers le bas au-delà du hero, le LazyColumn scrolle d'un coup pour afficher le prochain item (gradient 40dp + section suivante). Ce saut de 440dp minimum est la source probable de la saccade perçue. |
| Gradient Box | 40dp | Non — petite hauteur, transition douce |
| Bottom padding | 80dp | Non |

---

## 3 — FlingBehavior et snapBehavior

### 3.1 — Recherche de snap/fling personnalisé

| Pattern recherché | Présent | Fichier |
|-------------------|---------|---------|
| `SnapFlingBehavior` | **Non** | — |
| `rememberSnapFlingBehavior` | **Non** | — |
| `PagerSnapDistance` | **Non** | — |
| `SnapLayoutInfoProvider` | **Non** | — |
| `flingBehavior` (paramètre LazyColumn) | **Non spécifié** | — |
| `nestedScroll` | **Non** | — |

**Aucun snap behavior personnalisé n'est appliqué.** Le LazyColumn utilise le `flingBehavior` par défaut de Compose, qui est un défilement libre avec décélération naturelle.

### 3.2 — Comportement par défaut de LazyColumn

Le `ScrollableDefaults.flingBehavior()` par défaut utilise un `AndroidFlingDecaySpec` basé sur le `ViewConfiguration.getScaledMaximumFlingVelocity()` du device. Sur Android TV avec D-pad :
- **Pas de fling** : le D-pad ne produit pas de vélocité de fling, seulement des événements de focus
- **Le scroll est piloté par le focus** : quand le focus passe d'un élément visible à un élément hors écran, le LazyColumn scrolle pour rendre l'élément visible via le mécanisme "bring into view"
- **Ce n'est PAS un fling** — c'est un scroll programmatique déclenché par le système de focus

### 3.3 — Conséquence sur les saccades

Le "saut" perçu n'est pas un snap intentionnel mais le résultat du mécanisme natif :
1. D-pad ↓ depuis le dernier bouton du hero
2. Le focus passe à la première carte de la section suivante (épisode/cast/similar)
3. Cette carte est dans un item LazyColumn **complètement hors écran**
4. Le LazyColumn scrolle d'un coup pour rendre visible cet item
5. Ce scroll programmatique déplace la vue de **~250-440dp** d'un coup — c'est la saccade

---

## 4 — focusGroup et scroll automatique

### 4.1 — Inventaire des focusGroup

| Fichier | Ligne | Composable | Position dans LazyColumn | Impact scroll |
|---------|-------|-----------|------------------------|---------------|
| MovieDetailsContent | 255 | Row (boutons primaires) | Item 0 (hero zone) | **Non** — dans le viewport initial |
| MovieDetailsContent | 290 | Row (boutons secondaires) | Item 0 (hero zone) | **Non** — dans le viewport initial |
| SeasonDetailsContent | 188 | Row (boutons primaires) | Item 0 (hero zone) | **Non** — dans le viewport initial |
| SeasonDetailsContent | 211 | Row (boutons secondaires) | Item 0 (hero zone) | **Non** — dans le viewport initial |
| DetailActions | 101 | Row (boutons primaires) | Item 0 (hero zone, via Series) | **Non** — dans le viewport initial |
| DetailActions | 158 | Row (boutons secondaires) | Item 0 (hero zone, via Series) | **Non** — dans le viewport initial |
| DetailSections | 223 | FlowRow (CollectionItemsGrid) | Item séparé du LazyColumn | **OUI** — quand le focus entre dans ce groupe, le LazyColumn scrolle pour le rendre visible |

### 4.2 — Analyse du mécanisme de scroll par focus

Les `focusGroup()` sur les Rows de boutons dans le hero ne posent pas de problème car ils sont dans le viewport initial.

Le **vrai problème** est le pattern de chaque section sous le hero :
```kotlin
// Chaque section est UN SEUL item du LazyColumn
item {
    Box(modifier = Modifier.fillMaxWidth().background(...).padding(...)) {
        DetailCastSection(...)  // contient Column { SectionHeader + LazyRow }
    }
}
```

Quand le focus D-pad descend vers une section hors écran :
1. Compose cherche le prochain élément focusable dans la direction
2. Il trouve la première carte `clickable()` dans la `LazyRow` de la section suivante
3. Le LazyColumn scrolle pour rendre visible l'**item entier** (pas juste la carte)
4. L'item entier inclut le padding, le header, et toute la LazyRow — typiquement 160-300dp
5. **Le scroll est proportionnel à la taille de l'item, pas à la taille de la carte focusée**

### 4.3 — Effet d'amplification par le padding

Chaque section a un `padding(top = 24.dp)` sur son Box wrapper. Ce padding est INCLUS dans la taille de l'item LazyColumn. Quand le LazyColumn "bring into view" cet item, il scroll pour montrer le top du Box (incluant le padding), pas le top de la carte focusée. Cela amplifie le saut perçu de ~24dp.

---

## 5 — Performances de composition

### 5.1 — Calculs dans le corps du composable (hors remember)

| Fichier | Ligne | Calcul | Impact |
|---------|-------|--------|--------|
| MovieDetailsContent | 100 | `getPosterUrl(item, api)` | **Faible** — calcul d'URL pur (concaténation de strings). Pas de réseau. Recalculé à chaque recomposition mais O(1). |
| MovieDetailsContent | 109 | `buildGenreTag(context, item)` | **Faible** — lookup dans une map + joinToString. O(n) avec n ≤ 3. |
| MovieDetailsContent | 112-121 | `item.mediaSources?.firstOrNull()?.mediaStreams?.filter { ... }` | **Faible** — filtrage de listes petites (typiquement < 20 streams). Recalculé à chaque recomposition mais négligeable. |
| SeriesDetailsContent | 76 | `getPosterUrl(item, api)` | **Faible** — identique |
| SeasonDetailsContent | 80 | `getPosterUrl(item, api)` | **Faible** — identique |

### 5.2 — Appels dans les LazyRow (par carte)

| Section | Calcul par carte | Impact |
|---------|-----------------|--------|
| DetailCastSection | `api.imageApi.getItemImageUrl(...)` | **Faible** — construction d'URL locale, pas d'appel réseau. Mais recalculé pour chaque carte à chaque recomposition de la LazyRow. Pourrait être wrappé dans `remember(person.id)`. |
| DetailSectionWithCards | `getPosterUrl(item, api)` ou `getEpisodeThumbnailUrl(item, api)` | **Faible** — idem, construction d'URL pure. |
| DetailEpisodesHorizontalSection | `getPosterUrl(ep, api)` + `formatDuration(it)` | **Faible** — idem |
| DetailSeasonsSection | `getPosterUrl(season, api)` | **Faible** — idem |

### 5.3 — `remember` et `derivedStateOf`

| Fichier | Utilisation | Correct |
|---------|-------------|---------|
| MovieDetailsContent | `remember { FocusRequester() }` | ✅ Clé implicite (Unit) — stable |
| MovieDetailsContent | `remember { mutableStateOf(false) }` × 4 | ✅ |
| SeriesDetailsContent | Identique | ✅ |
| SeasonDetailsContent | Identique | ✅ |
| DetailHeroBackdrop | `remember(item?.id) { derivedStateOf { ... } }` | ✅ Correct — évite de recalculer l'URL à chaque recomposition |
| DetailSections | **Aucun `remember`** | ⚠️ Les fonctions `getPosterUrl()`, `getEpisodeThumbnailUrl()`, `api.imageApi.getItemImageUrl()` sont appelées directement dans le corps des `items {}` lambdas sans `remember`. Sur des listes longues (20+ épisodes), cela recalcule toutes les URLs visibles à chaque recomposition du LazyRow. Impact négligeable car ce sont des calculs O(1) mais non-idiomatique. |

### 5.4 — Recomposition pendant le scroll

Quand l'utilisateur scrolle le LazyColumn avec D-pad :
1. Le focus change → l'état de focus des cartes change (`isFocused` via `interactionSource.collectIsFocusedAsState()`)
2. Seules les cartes dont le focus change sont recomposées (granularité correcte grâce aux `interactionSource` locaux)
3. **Pas de recomposition de toute la page** — les sections utilisent des `item {}` séparés dans le LazyColumn, ce qui isole les recompositions

### 5.5 — Point d'attention : AnimatedVisibility dans un item LazyColumn

| Fichier | Composable | Impact |
|---------|-----------|--------|
| MovieDetailsContent:157 | `AnimatedVisibility` dans l'item hero | **Aucun impact sur le scroll** — l'animation ne se joue qu'au chargement initial. Pendant le scroll, `showContent = true` est stable et ne trigger pas de recomposition. |

---

## Résumé des causes de saccade identifiées

### Causes confirmées

| # | Sévérité | Cause | Description |
|---|----------|-------|-------------|
| 1 | **CRITIQUE** | **Item hero de 440dp** | Le premier item du LazyColumn fait 440dp sur un viewport de ~540dp. Quand le focus quitte le hero vers la section suivante, le LazyColumn doit scroller de 440dp + 40dp (gradient) minimum d'un coup. C'est un "page jump" intrinsèque à la structure actuelle. |
| 2 | **MODÉRÉ** | **Sections entières comme items uniques** | Chaque section (cast, similar, etc.) est UN SEUL item du LazyColumn. Le "bring into view" scroll pour montrer l'item entier, pas juste l'élément focusé à l'intérieur. Si l'item fait 300dp, le scroll est de 300dp. |
| 3 | **MODÉRÉ** | **Pas de flingBehavior adapté au D-pad** | Le LazyColumn utilise le fling par défaut (conçu pour le tactile). Sur TV, le scroll est piloté par le focus, pas par le fling. Le scroll "bring into view" n'est pas animé de façon fluide — il utilise un `animateScrollToItem` avec une durée par défaut qui peut sembler brusque. |

### Causes non confirmées (absentes)

| Pattern | Présent | Notes |
|---------|---------|-------|
| SnapFlingBehavior | Non | Pas de points d'ancrage intentionnels |
| Scroll vertical imbriqué | Non | Architecture correcte |
| nestedScroll | Non | |
| Recomposition lourde pendant scroll | Non | Les calculs sont O(1) et les recompositions sont isolées par item |
| `remember` manquant critique | Non | Les URLs sont des calculs trivials, l'absence de `remember` n'impacte pas les performances |

### Recommandations architecturales

1. **Réduire la hauteur du hero** de 440dp à ~360dp pour qu'il ne dépasse pas le viewport TV. Cela éliminerait le saut de 440dp et le remplacerait par un scroll proportionnel plus doux.

2. **Découper les sections en items séparés** : au lieu d'un seul `item { Box { DetailCastSection() } }`, utiliser deux items :
   ```kotlin
   item { SectionHeader("Cast") }
   item { LazyRow { /* cast cards */ } }
   ```
   Cela permettrait au "bring into view" de scroller juste assez pour montrer le header, puis la row.

3. **Ajouter `animateScrollToItem` avec une courbe personnalisée** : au lieu de laisser le système "bring into view" gérer le scroll, intercepter les changements de focus et utiliser `animateScrollToItem` avec une `tween(300, easing = EaseOutCubic)` pour un scroll plus fluide.
