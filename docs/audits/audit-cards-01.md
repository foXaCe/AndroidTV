# Audit exhaustif — Cartes media (4 composables)

**Date** : 2026-03-15
**Scope** : `BrowseMediaCard`, `LibraryPosterCard`, `FolderItemCard`, `MusicSquareCard`

---

## Fichiers audites

| Composable | Fichier | Lignes |
|---|---|---|
| `BrowseMediaCard` | `app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` | L85-302 |
| `LibraryPosterCard` | `app/src/main/java/org/jellyfin/androidtv/ui/browsing/v2/LibraryBrowseComponents.kt` | L157-333 |
| `FolderItemCard` | `app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/FolderBrowseScreen.kt` | L228-315 |
| `MusicSquareCard` | `app/src/main/java/org/jellyfin/androidtv/ui/browsing/v2/MusicBrowseFragment.kt` | L356-454 |

### Fichiers de reference du design system

| Fichier | Role |
|---|---|
| `ui/base/theme/VegafoXDimensions.kt` | `CardDimensions` — tokens officiels |
| `ui/base/tv/TvFocusCard.kt` | Composable de focus TV standard |
| `ui/base/theme/VegafoXColors.kt` | Palette VegafoX |
| `ui/base/AnimationDefaults.kt` | Constantes d'animation |
| `ui/base/shapes.kt` | Shapes tokenises (`extraSmall=4dp`, `medium=12dp`) |
| `ui/composable/ImagePlaceholder.kt` | Placeholders image (gradient, shimmer, error) |

---

## Point 1 — Dimensions et ratio

### BrowseMediaCard

| Propriete | Valeur | Source | Ligne |
|---|---|---|---|
| Largeur | `220.dp` (defaut) | `CardDimensions.landscapeWidth` | L94 |
| Hauteur | Calculee `cardWidth * 9/16` = **123.75dp** | Dynamique | L97 |
| Ratio | **16:9** | Calcul L97 | L97 |
| Largeur parametre | Oui (`cardWidth: Dp`) | Parametre | L94 |
| Coins arrondis | `RoundedCornerShape(10.dp)` | 🔴 **Hardcode** — ni `JellyfinTheme.shapes.*` ni token | L164 |
| Taille image reseau | `440x248 px` (constantes) | `CARD_IMAGE_WIDTH_PX` / `CARD_IMAGE_HEIGHT_PX` | L69-70 |

### LibraryPosterCard

| Propriete | Valeur | Source | Ligne |
|---|---|---|---|
| Largeur | Variable — parametre `cardWidth: Int` (dp) | Parametre appelant | L160 |
| Hauteur | Variable — parametre `cardHeight: Int` (dp) | Parametre appelant | L161 |
| Ratio | **Depend de l'appelant** (typiquement 2:3 portrait) | Externe | N/A |
| Coins arrondis | `JellyfinTheme.shapes.extraSmall` = **4dp** | 🟢 Tokenise | L215, L217 |

### FolderItemCard

| Propriete | Valeur | Source | Ligne |
|---|---|---|---|
| Largeur | `140.dp` (defaut hardcode) | Parametre `cardWidth: Int = 140` | L233 |
| Hauteur | `210.dp` (defaut hardcode) | Parametre `cardHeight: Int = 210` | L234 |
| Ratio | **2:3** (140/210) | Hardcode | L233-234 |
| Coins arrondis | `JellyfinTheme.shapes.extraSmall` = **4dp** | 🟢 Tokenise | L280, L283 |

### MusicSquareCard

| Propriete | Valeur | Source | Ligne |
|---|---|---|---|
| Largeur | `140.dp` (defaut hardcode) | Parametre `cardSize: Int = 140` | L361 |
| Hauteur | `140.dp` (= cardSize) | Identique a largeur | L391 |
| Ratio | **1:1** (carre) | `Modifier.size(cardSize.dp)` | L391 |
| Coins arrondis | `JellyfinTheme.shapes.extraSmall` = **4dp** | 🟢 Tokenise | L392 |

### Synthese Point 1

| Carte | Largeur | Hauteur | Ratio | Coins | Token dims |
|---|---|---|---|---|---|
| BrowseMediaCard | 220dp | ~124dp | 16:9 | 🔴 10dp hardcode | 🟢 `CardDimensions` |
| LibraryPosterCard | variable | variable | variable | 🟢 4dp token | 🔴 Aucun token |
| FolderItemCard | 140dp | 210dp | 2:3 | 🟢 4dp token | 🔴 Hardcode |
| MusicSquareCard | 140dp | 140dp | 1:1 | 🟢 4dp token | 🔴 Hardcode |

🔴 **Incoherence majeure** : `BrowseMediaCard` utilise `RoundedCornerShape(10.dp)` hardcode au lieu de `JellyfinTheme.shapes`. Les 3 autres utilisent `extraSmall` (4dp). Aucune carte ne partage le meme rayon de coin.

🔴 **Incoherence** : seule `BrowseMediaCard` utilise `CardDimensions`. Les 3 autres hardcodent leurs dimensions sans utiliser les tokens du design system.

---

## Point 2 — Style de focus

### BrowseMediaCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Composable focus | 🟢 **`TvFocusCard`** (tv-material3 `Surface`) | L112 |
| Bordure focus | `2.dp`, couleur `focusRing` = `OrangePrimary` (#FF6B00) | TvFocusCard.kt L79-84 |
| Glow focus | Aucune glow custom — uniquement la bordure du `Surface` | N/A |
| Scale focus | `AnimationDefaults.FOCUS_SCALE` = **1.06f** | TvFocusCard.kt L48, AnimationDefaults.kt L17 |
| Scale press | `AnimationDefaults.PRESS_SCALE` = **0.95f** | TvFocusCard.kt L68 |
| Animation | Geree par tv-material3 `Surface` (native) | TvFocusCard.kt L70-94 |
| Shape focus | `JellyfinTheme.shapes.medium` = **12dp** | TvFocusCard.kt L50 |

### LibraryPosterCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Composable focus | 🔴 **Custom** (`clickable` + `MutableInteractionSource.collectIsFocusedAsState`) | L168-169 |
| Bordure focus | `2.dp`, couleur `VegafoXColors.OrangePrimary` | L217 |
| Glow focus | 🟡 `drawBehind` radialGradient orange 25% alpha, rayon `maxDimension * 0.8` | L188-201 |
| Scale focus | `AnimationDefaults.FOCUS_SCALE` = **1.06f** | L176-179 |
| Scale press | Aucune | N/A |
| Animation | `animateFloatAsState` + `AnimationDefaults.focusSpec()` (tween 150ms) | L175-179 |
| Shape bordure | `JellyfinTheme.shapes.extraSmall` = **4dp** | L217 |

### FolderItemCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Composable focus | 🔴 **Custom** (`clickable` + `MutableInteractionSource.collectIsFocusedAsState`) | L236-237 |
| Bordure focus | `2.dp`, couleur `VegafoXColors.OrangePrimary` | L283 |
| Glow focus | 🟡 `drawBehind` radialGradient orange 20% alpha, rayon `maxDimension * 0.8` | L255-268 |
| Scale focus | 🔴 **1.08f hardcode** (pas `AnimationDefaults.FOCUS_SCALE`) | L243 |
| Scale press | Aucune | N/A |
| Animation | 🔴 **Aucune animation** — valeur appliquee directement via `graphicsLayer` | L250-253 |
| Alpha unfocused | 🔴 **0.75f** quand pas focus (dimming) | L244 |
| Shape bordure | `JellyfinTheme.shapes.extraSmall` = **4dp** | L283 |

### MusicSquareCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Composable focus | 🔴 **Custom** (`clickable` + `MutableInteractionSource.collectIsFocusedAsState`) | L362-363 |
| Bordure focus | 🔴 **Aucune bordure** — uniquement background alpha change | L394-399 |
| Glow focus | 🔴 **Aucune glow** | N/A |
| Scale focus | 🔴 **1.08f hardcode** (pas `AnimationDefaults.FOCUS_SCALE`) | L369 |
| Scale press | Aucune | N/A |
| Animation | 🔴 **Aucune animation** — valeur appliquee directement via `graphicsLayer` | L376-380 |
| Alpha unfocused | 🔴 **0.75f** quand pas focus (dimming) | L370 |
| Shape bordure | N/A | N/A |

### Synthese Point 2

| Carte | Focus wrapper | Bordure | Glow | Scale | Scale animee | Alpha dim | Press |
|---|---|---|---|---|---|---|---|
| BrowseMediaCard | 🟢 `TvFocusCard` | 2dp orange | Non | 1.06f | 🟢 Oui (native) | Non | 0.95f |
| LibraryPosterCard | 🔴 Custom | 2dp orange | 🟡 Oui (25%) | 1.06f | 🟢 Oui (tween) | Non | Non |
| FolderItemCard | 🔴 Custom | 2dp orange | 🟡 Oui (20%) | 🔴 1.08f | 🔴 Non | 🔴 0.75f | Non |
| MusicSquareCard | 🔴 Custom | 🔴 Aucune | 🔴 Non | 🔴 1.08f | 🔴 Non | 🔴 0.75f | Non |

🔴 **Incoherence critique** : 4 approches de focus differentes. Seule `BrowseMediaCard` utilise le composable standard `TvFocusCard`. Les 3 autres reimplementent le focus a la main avec des parametres differents.

🔴 **Scale inconsistante** : `BrowseMediaCard` et `LibraryPosterCard` utilisent `1.06f` (token), `FolderItemCard` et `MusicSquareCard` utilisent `1.08f` (hardcode).

🔴 **Alpha dimming** : `FolderItemCard` et `MusicSquareCard` appliquent `alpha=0.75f` sur les cartes non-focusees. Les 2 autres n'ont pas ce dimming. Cela cree une incoherence visuelle forte.

🔴 **Pas d'animation de transition** : `FolderItemCard` et `MusicSquareCard` n'animent pas le scale — le changement est instantane (`graphicsLayer { scaleX = scale }`), ce qui parait saccade.

---

## Point 3 — Contenu affiche

### BrowseMediaCard

| Element | Present | Details | Ligne |
|---|---|---|---|
| Image principale | Oui | THUMB > BACKDROP > parent THUMB > parent BACKDROP > PRIMARY > parent PRIMARY | L304-353 |
| ContentScale | `ContentScale.Crop` | L186 |
| Titre | 🔴 **Non** | Pas de titre sous la carte | N/A |
| Sous-titre | Non | N/A | N/A |
| Progress bar | 🟢 Oui | 2dp, `VegafoXColors.Divider` (fond) + `VegafoXColors.BlueAccent` (rempli), bottom 3dp | L217-236 |
| Badge NEW | 🟢 Oui | TopEnd, fond `OrangePrimary`, texte `Background`, shape 4dp, 10sp Bold | L239-263 |
| Badge watched | 🔴 Non | N/A | N/A |
| Badge qualite | Non | N/A | N/A |
| Overlay focus | 🟢 Oui | Play overlay apres 5s, fond 65% `Background`, icone ▶ + texte Play/Resume | L266-299 |
| Placeholder sans image | `VegafoXIcons.Movie` (48dp), tint `textDisabled` | L207-213 |
| Favorite | Non | N/A | N/A |

### LibraryPosterCard

| Element | Present | Details | Ligne |
|---|---|---|---|
| Image principale | Oui | URL fournie en parametre (`imageUrl: String?`) | L159 |
| ContentScale | `ContentScale.Crop` | L229 |
| Titre | 🟢 Oui | `bodyMedium`, `FontWeight.Medium`, `onSurface`, 1 ligne, ellipsis | L310-317 |
| Sous-titre | 🟢 Oui | Metadata (`buildMetadataString`) : annee, rating, duree, ★ | L320-330 |
| Progress bar | 🟢 Oui | Via `Seekbar` composable, 4dp hauteur, bottom, padding `Tokens.Space.spaceXs` | L279-303 |
| Badge type | 🟡 Conditionnel | TopStart, `showBadge=false` par defaut, genre (MOVIE, SERIES, etc.) | L237-256 |
| Badge watched | 🟢 Oui | Via `PosterWatchIndicator` — icone Visibility ou compteur unplayed, TopEnd | L271-277 |
| Badge qualite | Non | N/A | N/A |
| Overlay focus | Non | N/A | N/A |
| Placeholder sans image | 🟡 Rien affiche si `imageUrl == null` (pas de Box de fallback) | L223 |
| Favorite | 🟢 Oui | Icone coeur `VegafoXIcons.Favorite`, tint `Error`, TopStart 4dp, 20dp | L258-269 |
| Badge NEW | 🔴 Non | N/A | N/A |

### FolderItemCard

| Element | Present | Details | Ligne |
|---|---|---|---|
| Image principale | Oui | `PRIMARY` uniquement, `maxHeight=400` | L289 |
| ContentScale | `ContentScale.Crop` | L298 |
| Titre | 🟢 Oui | `bodySmall`, `FontWeight.Medium`, `VegafoXColors.TextSecondary`, 1 ligne | L306-313 |
| Sous-titre | 🔴 Non | N/A | N/A |
| Progress bar | 🔴 Non | N/A | N/A |
| Badge type | Non | N/A | N/A |
| Badge watched | 🔴 Non | N/A | N/A |
| Badge qualite | Non | N/A | N/A |
| Overlay focus | Non | N/A | N/A |
| Placeholder sans image | 🔴 Rien (Box vide avec fond 6% alpha) | L287 |
| Favorite | Non | N/A | N/A |
| Badge NEW | Non | N/A | N/A |

### MusicSquareCard

| Element | Present | Details | Ligne |
|---|---|---|---|
| Image principale | Oui | Album art > PRIMARY > parent PRIMARY, `maxHeight=300` | L615-631 |
| ContentScale | `ContentScale.Crop` | L409 |
| Titre | 🟢 Oui | `bodySmall`, `FontWeight.Medium`, `onSurface`, 1 ligne | L432-439 |
| Sous-titre | 🟢 Oui | Artiste (albums/audio), item count (playlists), album count (artistes) | L442-452 |
| Progress bar | 🔴 Non | N/A | N/A |
| Badge type | Non | N/A | N/A |
| Badge watched | 🔴 Non | N/A | N/A |
| Badge qualite | Non | N/A | N/A |
| Overlay focus | Non | N/A | N/A |
| Placeholder sans image | 🟢 `VegafoXIcons.Album` (48dp), tint `onSurface` 20% alpha | L415-426 |
| Favorite | Non | N/A | N/A |
| Badge NEW | Non | N/A | N/A |

### Synthese Point 3

| Element | BrowseMedia | LibraryPoster | FolderItem | MusicSquare |
|---|---|---|---|---|
| Image source | Multi-fallback (6 types) | Externe | PRIMARY seul | Multi (3 types) |
| Titre | 🔴 Non | 🟢 Oui | 🟢 Oui | 🟢 Oui |
| Sous-titre | Non | 🟢 Metadata riche | Non | 🟢 Artiste |
| Progress bar | 🟢 Custom Box | 🟢 Seekbar | 🔴 Non | 🔴 Non |
| Badge NEW | 🟢 Oui | 🔴 Non | Non | Non |
| Badge watched | 🔴 Non | 🟢 Oui | 🔴 Non | Non |
| Favorite | Non | 🟢 Oui | Non | Non |
| Overlay play | 🟢 5s delay | Non | Non | Non |
| Placeholder | 🟢 Icon Movie | 🔴 Rien | 🔴 Rien | 🟢 Icon Album |

🔴 **`LibraryPosterCard`** n'a aucun placeholder quand `imageUrl == null` — affiche un fond transparent vide.
🔴 **`FolderItemCard`** n'a aucun placeholder sans image non plus.
🟡 Les progress bars utilisent des composables differents (`Box` custom vs `Seekbar`).

---

## Point 4 — Chargement image

### BrowseMediaCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Bibliotheque | 🟢 **Coil 3** (`coil3.compose.AsyncImage`) | L42, L182 |
| ImageRequest | 🟢 Builder complet : `size(440, 248)`, `Scale.FILL`, memory+disk cache, `crossfade(false)` | L171-180 |
| Placeholder | 🔴 **Aucun** placeholder (pas de `placeholder = ...`) | L182-198 |
| Error fallback | 🟢 `rememberErrorPlaceholder()` = `ColorPainter(surfaceContainer 50%)` | L181, L187 |
| Dimensions reseau | `fillWidth=440` (fixe dans l'URL via `getUrl`) | L316-349 |
| URL memoised | 🔴 Non — `getItemImageUrl()` est appelee a chaque recomposition | L167 |
| Logging | 🟡 Verbose Timber logging (onLoading, onSuccess, onError) | L188-197 |

### LibraryPosterCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Bibliotheque | 🟢 **Coil 3** (`coil3.compose.AsyncImage`) | L60, L226 |
| ImageRequest | 🔴 Passe en `model = imageUrl` (String simple, pas de Builder) | L227 |
| Placeholder | 🟢 `rememberGradientPlaceholder()` = gradient diagonal surfaceBright→surfaceDim | L224, L230 |
| Error fallback | 🟢 `rememberErrorPlaceholder()` | L225, L231 |
| Dimensions reseau | Geree par l'appelant (url externe) | N/A |
| URL memoised | N/A (parametre) | N/A |

### FolderItemCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Bibliotheque | 🟢 **Coil 3** (`coil3.compose.AsyncImage`) | L42, L293 |
| ImageRequest | 🔴 Passe en `model = imageUrl` (String simple) | L295 |
| Placeholder | 🟢 `rememberGradientPlaceholder()` | L291, L298 |
| Error fallback | 🟢 `rememberErrorPlaceholder()` | L292, L299 |
| Dimensions reseau | `maxHeight=400` dans `getUrl()` | L289 |
| URL memoised | 🔴 Non — construit inline a chaque recomposition | L289 |

### MusicSquareCard

| Propriete | Valeur | Ligne |
|---|---|---|
| Bibliotheque | 🟢 **Coil 3** (`coil3.compose.AsyncImage`) | L46, L405 |
| ImageRequest | 🔴 Passe en `model = imageUrl` (String simple) | L407 |
| Placeholder | 🟢 `rememberGradientPlaceholder()` | L403, L410 |
| Error fallback | 🟢 `rememberErrorPlaceholder()` | L404, L411 |
| Dimensions reseau | `maxHeight=300` dans `getUrl()` | L619, L624, L628 |
| URL memoised | 🔴 Non — `getMusicImageUrl()` est une fonction membre | L401 |

### Synthese Point 4

| Propriete | BrowseMedia | LibraryPoster | FolderItem | MusicSquare |
|---|---|---|---|---|
| Coil version | 🟢 Coil 3 | 🟢 Coil 3 | 🟢 Coil 3 | 🟢 Coil 3 |
| ImageRequest builder | 🟢 Complet | 🔴 String seul | 🔴 String seul | 🔴 String seul |
| Placeholder loading | 🔴 Aucun | 🟢 Gradient | 🟢 Gradient | 🟢 Gradient |
| Error fallback | 🟢 Oui | 🟢 Oui | 🟢 Oui | 🟢 Oui |
| Cache policy | 🟢 Explicite | 🟡 Implicite | 🟡 Implicite | 🟡 Implicite |
| Crossfade | Desactive | 🟡 Defaut Coil | 🟡 Defaut Coil | 🟡 Defaut Coil |
| URL remember | 🔴 Non | N/A | 🔴 Non | 🔴 Non |
| Dims reseau | 440px fixe | Externe | 400px max | 300px max |

🔴 **`BrowseMediaCard`** est la seule carte sans `placeholder` visible pendant le chargement — l'utilisateur voit le fond `surfaceDim` brut, pas le gradient.

🟡 Seule `BrowseMediaCard` utilise un `ImageRequest.Builder` avec `size()`, `scale()`, et cache policies explicites. Les 3 autres passent un simple String, dependant des defaults de Coil.

---

## Point 5 — Interactions

### BrowseMediaCard

| Callback | Present | Details | Ligne |
|---|---|---|---|
| `onClick` | 🟢 Oui | Via `TvFocusCard` Surface (D-pad center) | L88, L113 |
| `onPlayClick` | 🟢 Oui | Via long press key event handler | L92, L125 |
| `onFocus` | 🟢 Oui (optionnel) | Via `onFocusChanged` modifier | L90, L148-149 |
| `onBlur` | 🟢 Oui (optionnel) | Via `onFocusChanged` modifier | L91, L151 |
| Long press | 🟢 **Implemente** — `onPreviewKeyEvent` intercepte `ACTION_DOWN isLongPress` | L118-137 |
| Menu contextuel | 🔴 Non | N/A | N/A |
| Focus requester | 🟢 Oui (parametre `initialFocusRequester`) | L95, L139-143 |

### LibraryPosterCard

| Callback | Present | Details | Ligne |
|---|---|---|---|
| `onClick` | 🟢 Oui | Via `Modifier.clickable` | L163, L205 |
| `onFocused` | 🟢 Oui | Via `LaunchedEffect(isFocused)` | L163, L171-173 |
| Long press | 🔴 Non | N/A | N/A |
| Menu contextuel | 🔴 Non | N/A | N/A |
| Focus requester | 🔴 Non | N/A | N/A |

### FolderItemCard

| Callback | Present | Details | Ligne |
|---|---|---|---|
| `onClick` | 🟢 Oui | Via `Modifier.clickable` | L231, L271 |
| `onFocused` | 🟢 Oui | Via `LaunchedEffect(isFocused)` | L232, L239-241 |
| Long press | 🔴 Non | N/A | N/A |
| Menu contextuel | 🔴 Non | N/A | N/A |
| Focus requester | 🔴 Non | N/A | N/A |

### MusicSquareCard

| Callback | Present | Details | Ligne |
|---|---|---|---|
| `onClick` | 🟢 Oui | Via `Modifier.clickable` | L358, L382 |
| `onFocused` | 🟢 Oui | Via `LaunchedEffect(isFocused)` | L359, L365-367 |
| Long press | 🔴 Non | N/A | N/A |
| Menu contextuel | 🔴 Non | N/A | N/A |
| Focus requester | 🔴 Non | N/A | N/A |

### Synthese Point 5

| Interaction | BrowseMedia | LibraryPoster | FolderItem | MusicSquare |
|---|---|---|---|---|
| onClick | 🟢 | 🟢 | 🟢 | 🟢 |
| onFocus | 🟢 | 🟢 | 🟢 | 🟢 |
| onBlur | 🟢 | 🔴 | 🔴 | 🔴 |
| Long press play | 🟢 | 🔴 | 🔴 | 🔴 |
| Menu contextuel | 🔴 | 🔴 | 🔴 | 🔴 |
| FocusRequester | 🟢 | 🔴 | 🔴 | 🔴 |
| Play overlay 5s | 🟢 | 🔴 | 🔴 | 🔴 |

🟡 Seule `BrowseMediaCard` supporte le long press pour la lecture directe. Les 3 autres cartes n'ont aucune interaction secondaire.

🔴 Aucune carte n'implemente de menu contextuel (long press menu pour ajouter aux favoris, marquer vu, etc.).

---

## Point 6 — Proposition d'unification

### Tableau comparatif global

| Dimension | BrowseMediaCard | LibraryPosterCard | FolderItemCard | MusicSquareCard |
|---|---|---|---|---|
| **Focus wrapper** | `TvFocusCard` | Custom | Custom | Custom |
| **Scale** | 1.06 (token) | 1.06 (token) | 1.08 (hardcode) | 1.08 (hardcode) |
| **Scale animee** | Oui (native) | Oui (tween) | Non | Non |
| **Bordure focus** | 2dp orange (via Surface) | 2dp orange (manual) | 2dp orange (manual) | Aucune |
| **Glow** | Non | drawBehind 25% | drawBehind 20% | Non |
| **Alpha dim** | Non | Non | 0.75 | 0.75 |
| **Ratio** | 16:9 | Variable | 2:3 | 1:1 |
| **Coins** | 10dp hardcode | 4dp token | 4dp token | 4dp token |
| **Titre** | Non | Oui | Oui | Oui |
| **Progress** | Oui (custom) | Oui (Seekbar) | Non | Non |
| **Badges** | NEW | Watched/Favorite/Type | Aucun | Aucun |
| **Placeholder img** | Icon | Rien | Rien | Icon |
| **Placeholder loading** | Aucun | Gradient | Gradient | Gradient |
| **ImageRequest** | Builder complet | String simple | String simple | String simple |
| **Long press** | Oui | Non | Non | Non |

### Cartes fusionnables

Les 3 cartes **LibraryPosterCard**, **FolderItemCard**, et **MusicSquareCard** partagent une structure quasi-identique :

```
Column (width, scale, clickable)
  └── Box (image, clip, border focus)
       └── AsyncImage
  └── Spacer(5dp)
  └── Text (titre)
  └── Text? (sous-titre optionnel)
```

Differences fonctionnelles entre ces 3 :

| Difference | LibraryPosterCard | FolderItemCard | MusicSquareCard |
|---|---|---|---|
| Ratio | Variable (appelant) | 2:3 (140x210) | 1:1 (140x140) |
| Badges | Watched + Favorite + Type | Aucun | Aucun |
| Progress | Seekbar | Non | Non |
| Sous-titre | Metadata riche | Non | Artiste/count |
| Glow | Oui | Oui | Non |

**Recommandation** : Fusionner ces 3 en un **`MediaPosterCard`** parametre :

```kotlin
@Composable
fun MediaPosterCard(
    item: BaseItemDto,
    imageUrl: String?,
    cardWidth: Dp,
    cardHeight: Dp,
    onClick: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    subtitle: String? = null,
    showBadges: Boolean = false,
    showProgress: Boolean = false,
    placeholderIcon: ImageVector? = null,
)
```

### Cartes devant rester distinctes

**`BrowseMediaCard`** doit rester separe pour les raisons suivantes :
- Ratio 16:9 (paysage) vs portrait/carre pour les autres
- Resolution d'image multi-fallback complexe (6 types d'images)
- Play overlay temporise (5 secondes)
- Long press pour lecture directe
- Utilise `TvFocusCard` (le wrapper officiel) — les autres non

Cependant, `BrowseMediaCard` devrait devenir le **modele de reference** pour le style de focus, et les autres devraient migrer vers `TvFocusCard`.

### Style de focus de reference

🟢 **Reference** : `TvFocusCard` (utilise par `BrowseMediaCard`)

Justification :
- Utilise `tv-material3 Surface` — le composable officiel de focus pour Android TV
- Gestion native du D-pad, de l'accessibilite, et de la semantique
- Scale animee nativement (pas de `graphicsLayer` manual)
- Press scale (0.95) pour le feedback tactile
- Bordure 2dp orange via `Border` API propre
- Respect de `reducedMotion` (accessibilite)

**Action** : Migrer `LibraryPosterCard`, `FolderItemCard`, et `MusicSquareCard` vers `TvFocusCard`.

### Dimensions de reference

🟢 **Reference** : `CardDimensions` dans `VegafoXDimensions.kt`

Tokens existants :
- `landscapeWidth = 220.dp`, `landscapeHeight = 124.dp` (16:9)
- `portraitWidth = 150.dp`, `portraitHeight = 225.dp` (2:3)

**Tokens manquants a ajouter** :
- `squareSize = 140.dp` (pour la musique)
- `folderWidth = 140.dp`, `folderHeight = 210.dp` (si different de portrait)

### Tableau de decision final

| Carte | Recommandation | Priorite | Effort |
|---|---|---|---|
| **BrowseMediaCard** | 🟢 Conserver — c'est la reference pour le focus. Corriger le coin `10dp` → token. Ajouter `placeholder` au chargement. | Faible | Mineur |
| **LibraryPosterCard** | 🟡 **Adapter** — migrer vers `TvFocusCard`, supprimer le custom focus. Conserver les badges et progress car fonctionnellement riches. | Haute | Moyen |
| **FolderItemCard** | 🔴 **Fusionner** dans `MediaPosterCard` — c'est une version simplifiee de `LibraryPosterCard` sans badges. Migrer vers `TvFocusCard`. Supprimer l'alpha dimming (0.75). | Haute | Faible |
| **MusicSquareCard** | 🔴 **Fusionner** dans `MediaPosterCard` — meme structure que `FolderItemCard` avec ratio 1:1 et sous-titre. Migrer vers `TvFocusCard`. Supprimer l'alpha dimming (0.75). | Haute | Faible |

### Corrections immediates (quick wins)

1. **`BrowseMediaCard` L164** : Remplacer `RoundedCornerShape(10.dp)` par `JellyfinTheme.shapes.small` (8dp) ou `medium` (12dp)
2. **`BrowseMediaCard` L182** : Ajouter `placeholder = rememberGradientPlaceholder()` dans `AsyncImage`
3. **`FolderItemCard` L243 / `MusicSquareCard` L369** : Remplacer `1.08f` par `AnimationDefaults.FOCUS_SCALE` (1.06f)
4. **`FolderItemCard` L244 / `MusicSquareCard` L370** : Supprimer l'alpha dimming `0.75f` (cree un contraste visuel incoherent avec les autres ecrans)
5. **`FolderItemCard`** : Ajouter un placeholder icon quand pas d'image (comme `BrowseMediaCard` et `MusicSquareCard`)
6. **`LibraryPosterCard` L223** : Ajouter un placeholder icon quand `imageUrl == null`
7. **Toutes** : Ajouter les dimensions manquantes dans `CardDimensions` (`squareSize`, etc.)

---

## Annexe — Arbre d'appels

```
HomeScreen
  └── BrowseMediaCard  ← 16:9 landscape, TvFocusCard

LibraryBrowseScreen
  └── LibraryPosterCard  ← portrait variable, custom focus

FolderBrowseScreen
  └── FolderItemRow
       └── FolderItemCard  ← 2:3 portrait, custom focus

MusicBrowseFragment
  └── MusicItemRow
       └── MusicSquareCard  ← 1:1 square, custom focus
```
