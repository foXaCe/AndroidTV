# Audit homogénéité visuelle + saccades acteurs — Page Détail

**Date** : 2026-03-15

---

## Point 1 — Hero trop haut sur certains médias

### 1.1 — Positionnement vertical du contenu hero

Les trois fichiers de contenu (Movie, Series, Season) utilisent une structure **identique** pour le hero :

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(HeroDimensions.backdropHeight)    // 360dp fixe
        .disableBringIntoView(),
    contentAlignment = Alignment.BottomStart,      // ancrage en bas
) {
    AnimatedVisibility(...) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HeroDimensions.horizontalPadding)  // 80dp
                .padding(bottom = 16.dp),
            verticalAlignment = ...,
        ) { ... }
    }
}
```

**La hauteur du hero (360dp) est fixe et identique pour tous les types.** Le contenu est ancré en `BottomStart`, ce qui signifie qu'il se positionne en bas de la zone de 360dp. **Aucun padding top, statusBarsPadding, windowInsetsPadding, ou offset conditionnel** n'est appliqué.

### 1.2 — Différence d'alignement vertical dans la Row

| Fichier | `verticalAlignment` de la Row | Effet |
|---------|------------------------------|-------|
| MovieDetailsContent:183 | `Alignment.Top` | Le poster et le texte s'alignent par le haut. Si le contenu texte est court (titre court, pas de synopsis), il y a un espace vide en dessous. |
| SeriesDetailsContent:128 | `Alignment.Bottom` | Le poster et le texte s'alignent par le bas. Le contenu est collé au bas de la zone. |
| SeasonDetailsContent:128 | `Alignment.Bottom` | Idem que Series. |

**C'est la cause de la différence perçue** : Movie utilise `Alignment.Top` dans la Row, ce qui fait que le contenu commence plus haut dans la zone hero. Series et Season utilisent `Alignment.Bottom`, collant le contenu en bas. Pour un film avec un titre long + synopsis + boutons, la différence est minime. Mais pour un film avec un titre court et pas de synopsis, le contenu est positionné plus haut dans le hero, laissant un espace vide en dessous.

### 1.3 — Backdrop image

`DetailHeroBackdrop.kt` :
- Le backdrop est un `Box(fillMaxSize)` avec `AsyncImage(fillMaxSize, ContentScale.Crop)` — **hauteur identique quel que soit le type de média**
- Le backdrop est une couche SOUS le contenu (z-index inférieur dans le Box parent d'ItemDetailScreen)
- **La présence ou l'absence d'image backdrop ne change pas la hauteur** — quand `url == null`, le Crossfade montre simplement rien (pas de composable émis), mais le Box garde sa taille fillMaxSize

### 1.4 — AnimatedVisibility offset

Les trois fichiers utilisent le même `slideInVertically(initialOffsetY = { slideOffsetPx })` avec `slideOffsetPx = 20.dp.roundToPx()`. **Identique pour tous les types**, pas de condition par type de contenu.

### 1.5 — Résumé

| Cause | Impact |
|-------|--------|
| **`verticalAlignment = Alignment.Top` dans MovieDetailsContent vs `Alignment.Bottom` dans Series/Season** | **CAUSE UNIQUE** — Le contenu Movie est ancré en haut de la Row, créant un espace variable en dessous selon la longueur du contenu. Series/Season sont ancrés en bas, collés au bord inférieur. |

**Correction suggérée** : Passer MovieDetailsContent à `Alignment.Bottom` pour homogénéiser avec Series et Season.

---

## Point 2 — Bouton Vu/Non-vu non homogène

### 2.1 — Inventaire du bouton Vu/Non-vu selon le contexte

| Contexte | Fichier | Ligne | Composable | Type | Forme | Taille |
|----------|---------|-------|-----------|------|-------|--------|
| Movie (row secondaire) | MovieDetailsContent.kt | 352-362 | `VegafoXIconButton` | Icon seul, pas de texte | **Cercle** (CircleShape) | **48×48dp** fixe (`.size(48.dp)` dans VegafoXButton.kt:414) |
| Series (via DetailActions) | DetailActions.kt | 219-229 | `VegafoXIconButton` | Icon seul, pas de texte | **Cercle** | **48×48dp** |
| Season (row secondaire) | SeasonDetailsContent.kt | 212-223 | `CinemaActionChip` | Icon **+ texte label** | **RoundedCornerShape(12dp)** | **Dynamique** : `padding(12.dp)` autour du contenu (icon 20dp + gap 4dp + text ~12sp) ≈ ~68dp hauteur |

### 2.2 — Comparaison visuelle

```
Movie/Series row secondaire :
┌──────────┐ ┌──────────┐ ┌──────────┐  ⬤   ┌──────────┐
│ AUDIO    │ │ S-TITRES │ │ FAVORI   │ 👁   │ SUPPR.   │
│ 40dp     │ │ 40dp     │ │ 40dp     │48dp  │ 40dp     │
└──────────┘ └──────────┘ └──────────┘  ⬤   └──────────┘
VegafoXButton  VegafoXButton  VegafoXButton  VegafoXIconButton  VegafoXButton
compact=true   compact=true   compact=true   (cercle)           compact=true

Season row secondaire :
┌─────┐  ┌─────┐
│ 👁  │  │  ♥  │
│ VU  │  │FAV  │
│68dp │  │68dp │
└─────┘  └─────┘
CinemaActionChip  CinemaActionChip
(vertical icon+label)
```

### 2.3 — Problèmes identifiés

| # | Problème | Impact |
|---|---------|--------|
| 1 | **Hauteur incohérente dans la row secondaire Movie/Series** : les VegafoXButton compact font 40dp, mais le VegafoXIconButton (Vu) fait 48dp de diamètre → **8dp plus haut** que ses voisins | Le bouton rond dépasse visuellement de la row |
| 2 | **Forme incohérente** : tous les boutons sont des rectangles arrondis (10dp radius) sauf Vu qui est un cercle | Rupture visuelle |
| 3 | **Pas de texte sur Vu dans Movie/Series** : tous les autres boutons ont icon + texte, Vu n'a que l'icône | L'utilisateur doit deviner la fonction |
| 4 | **Composable différent sur Season** : Season utilise `CinemaActionChip` (icon au-dessus du texte, layout vertical) au lieu de `VegafoXButton` → forme et taille complètement différentes | Incohérence entre types de page |

---

## Point 3 — Incohérence de taille des boutons secondaires

### 3.1 — Inventaire complet de la row secondaire

#### Dans MovieDetailsContent.kt (row ligne 289-375)

| Bouton | Composable | Variant | compact | Hauteur effective | Largeur |
|--------|-----------|---------|---------|------------------|---------|
| Audio | `VegafoXButton` | Outlined | `true` | **40dp** (`ButtonDimensions.heightCompact`) | wrap content + `padding(horizontal = 24.dp)` |
| Sous-titres | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Versions | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Bandes-annonces | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Favori | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| **Vu** | **`VegafoXIconButton`** | — | — | **48dp** (`.size(48.dp)`) | **48dp** |
| Supprimer | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |

#### Dans DetailActions.kt (row ligne 156-265) — utilisé par Series

| Bouton | Composable | Variant | compact | Hauteur effective | Largeur |
|--------|-----------|---------|---------|------------------|---------|
| Versions | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Audio | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Sous-titres | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Bandes-annonces | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Favori | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| **Vu** | **`VegafoXIconButton`** | — | — | **48dp** | **48dp** |
| Playlist | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Aller à la série | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |
| Supprimer | `VegafoXButton` | Outlined | `true` | **40dp** | wrap content |

#### Dans SeasonDetailsContent.kt (row ligne 208-237)

| Bouton | Composable | Hauteur effective |
|--------|-----------|------------------|
| Vu | `CinemaActionChip` | **~68dp** (12dp padding × 2 + 20dp icon + 4dp gap + ~12dp text) |
| Favori | `CinemaActionChip` | **~68dp** |

### 3.2 — Causes de l'incohérence

| Cause | Explication |
|-------|-------------|
| **VegafoXIconButton utilise `.size(48.dp)`** | Taille fixe non configurable — pas de paramètre `compact`. Le composable a été conçu pour un bouton icône indépendant, pas pour une row de boutons compacts. |
| **VegafoXButton utilise `.height(40dp)` en mode compact** | Via `ButtonDimensions.heightCompact = 40.dp`. La largeur est dynamique (wrap content avec padding 24dp horizontal). |
| **Pas de `Modifier.align(Alignment.CenterVertically)` dans la Row** | La Row utilise le `verticalAlignment` par défaut (`Top`) via `Arrangement.spacedBy(10.dp)` sans alignement vertical explicite → le bouton de 48dp dépasse par le bas. |
| **CinemaActionChip a un layout vertical** | Icon au-dessus du label, avec padding 12dp → hauteur bien plus grande. Composable différent = look différent. |

### 3.3 — Résumé

| # | Problème | Correction suggérée |
|---|---------|-------------------|
| 1 | VegafoXIconButton (48dp) dans une row de VegafoXButton compact (40dp) | Remplacer par un `VegafoXButton(compact=true, icon=Visibility, text="Vu")` |
| 2 | Season utilise CinemaActionChip au lieu de VegafoXButton | Uniformiser avec VegafoXButton Outlined compact |
| 3 | Row secondaire n'a pas d'alignement vertical centré | Ajouter `verticalAlignment = Alignment.CenterVertically` à la Row, ou utiliser des composants de même hauteur |

---

## Point 4 — Saccades sur la section Cast

### 4.1 — Structure du CastCard

`ItemDetailsComponents.kt:293-371` — `CastCard` :

```kotlin
Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
        .width(100.dp)                    // largeur FIXE
        .clickable(...)
) {
    Box(
        modifier = Modifier
            .size(72.dp)                   // taille FIXE pour l'image
            .clip(CircleShape)
            .border(...)
            .background(...)
    ) {
        AsyncImage(
            model = imageUrl,
            modifier = Modifier.fillMaxSize(),   // remplit le Box 72×72
            contentScale = ContentScale.Crop,
            placeholder = rememberGradientPlaceholder(),
            error = rememberErrorPlaceholder(),
        )
    }
    Spacer(Modifier.height(6.dp))
    Text(name, maxLines = 1)                      // hauteur fixe (1 ligne)
    if (role != null) Text(role, maxLines = 1)     // CONDITIONNEL — 0 ou 1 ligne
}
```

### 4.2 — Analyse des sources de saccade

| Vérification | Résultat | Impact |
|-------------|----------|--------|
| **Image Box taille fixe** | ✅ `Modifier.size(72.dp)` — fixe | **Pas de relayout** au chargement d'image. L'AsyncImage remplit le Box fixe. |
| **Placeholder pendant chargement** | ✅ `placeholder = rememberGradientPlaceholder()` | **Pas de saut** — le placeholder occupe l'espace pendant le chargement. |
| **Error fallback** | ✅ `error = rememberErrorPlaceholder()` | **Pas de saut** en cas d'erreur. |
| **Hauteur CastCard** | ⚠️ **DYNAMIQUE** — pas de `height()` fixe sur le Column racine | La hauteur dépend de la présence ou non du `role` : `if (role != null) Text(role)`. **Si un acteur a un rôle et un autre non, ils ont des hauteurs différentes.** |
| **`wrapContentHeight`** | ⚠️ **Implicite** — Column sans `.height()` utilise `wrapContentHeight` par défaut | La LazyRow doit s'adapter à l'item le plus grand de la fenêtre visible. |
| **Relayout au chargement d'image** | ❌ Non — taille fixe du Box + placeholder | Pas de saccade liée au chargement d'image. |
| **`remember` sur les URLs** | ⚠️ **ABSENT** | Les URLs sont calculées via `api.imageApi.getItemImageUrl(...)` dans le lambda `items` de la LazyRow (DetailSections.kt:148-155). Ce calcul est exécuté pour chaque item à chaque recomposition de la LazyRow. |

### 4.3 — URL d'image recalculée à chaque recomposition

`DetailSections.kt:144-156` — dans le corps de `items(cast, key = { it.id })` :

```kotlin
CastCard(
    imageUrl =
        person.primaryImageTag?.let { tag ->
            api.imageApi.getItemImageUrl(
                itemId = person.id,
                imageType = ImageType.PRIMARY,
                tag = tag,
                maxHeight = 280,
            )
        },
    ...
)
```

**Ce calcul d'URL est exécuté dans le lambda `items` de la LazyRow**, PAS dans un `remember`. Chaque recomposition de la LazyRow (par exemple quand le focus change entre les cartes) recalcule l'URL pour tous les items visibles.

**Impact** : `api.imageApi.getItemImageUrl()` est une construction d'URL pure (concaténation de strings, pas d'appel réseau). Le coût CPU est négligeable. **Cependant**, si l'URL générée est structurellement identique mais crée un nouvel objet String à chaque appel, Coil (le chargeur d'images) pourrait invalider son cache mémoire et recharger l'image depuis le cache disque, causant un flash de placeholder → image à chaque recomposition. Cela dépend de l'implémentation de `getItemImageUrl` et de la politique de cache de Coil.

### 4.4 — Cause probable des saccades Cast

| # | Cause | Sévérité | Explication |
|---|-------|----------|-------------|
| 1 | **Hauteur dynamique des CastCard** | **MODÉRÉ** | Le `if (role != null)` crée des cartes de hauteurs différentes. Quand la fenêtre visible de la LazyRow change (scroll horizontal ou recomposition), la hauteur intrinsèque de la LazyRow peut changer, forçant le LazyColumn parent à re-mesurer et re-positionner les items en dessous. |
| 2 | **URL d'image non-mémorisée** | **FAIBLE** | Risque théorique de flash placeholder si Coil invalide le cache, mais en pratique l'URL générée est stable (même tag = même string). |
| 3 | **LazyRow `horizontalArrangement = spacedBy(24.dp)`** | **AUCUN** | Le spacing est statique, pas de repositionnement dynamique. |
| 4 | **`contentPadding = PaddingValues(horizontal = 0.dp)`** | **AUCUN** | Pas de padding, pas d'impact. |

### 4.5 — Correction suggérée

1. **Fixer la hauteur des CastCard** : ajouter `.height(fixedHeight)` sur le Column racine (par exemple 110dp = 72dp image + 6dp spacer + 16dp nom + 14dp rôle + 2dp marge). Cela évite le re-mesure de la LazyRow quand des cartes avec/sans rôle entrent/sortent de la fenêtre visible.

2. **Toujours afficher le rôle** : remplacer le `if (role != null)` par un affichage systématique avec un texte vide ou un espace réservé. Cela garantit une hauteur uniforme.

3. **Mémoriser l'URL** : wrapper l'appel `api.imageApi.getItemImageUrl()` dans `remember(person.id, tag)` pour éviter de recréer l'objet String à chaque recomposition.
