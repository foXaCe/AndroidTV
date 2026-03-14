# H_FIX4 — Optimisation chargement images Home

**Date** : 2026-03-11

---

## 1. ImageRequest Coil optimise (BrowseMediaCard)

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Avant

```kotlin
AsyncImage(
    model = imageUrl,       // URL brute, pas de controle taille/cache
    contentScale = ContentScale.Crop,
    placeholder = rememberGradientPlaceholder(),  // gradient statique
)
```

Problemes :
- Coil decode l'image a la taille originale du serveur (souvent 1920x1080)
- Le crossfade global de 200ms ralentit la perception sur les grilles denses
- Le placeholder statique ne donne aucun feedback de chargement

### Apres

```kotlin
val imageRequest = ImageRequest.Builder(context)
    .data(imageUrl)
    .size(Size(440, 248))                // taille pixel exacte des cards 16:9
    .scale(Scale.FILL)                   // crop pour remplir le cadre
    .memoryCachePolicy(CachePolicy.ENABLED)
    .diskCachePolicy(CachePolicy.ENABLED)
    .crossfade(false)                    // pas de fondu sur les cards
    .build()
AsyncImage(
    model = imageRequest,
    contentScale = ContentScale.Crop,
    placeholder = rememberShimmerPlaceholder(),  // shimmer anime
)
```

### Detail des parametres

| Parametre | Valeur | Raison |
|-----------|--------|--------|
| `size(440, 248)` | Pixels fixes | Evite de decoder des images 1920px pour des cards de 220dp |
| `scale(Scale.FILL)` | Crop | Remplit le cadre 16:9 sans bandes noires |
| `memoryCachePolicy(ENABLED)` | Explicite | Cache memoire actif (defaut Coil, rendu explicite) |
| `diskCachePolicy(ENABLED)` | Explicite | Cache disque actif (250MB configure dans AppModule) |
| `crossfade(false)` | Desactive | Supprime le delai de 200ms entre placeholder et image |

### Constantes ajoutees

```kotlin
private const val CARD_IMAGE_WIDTH_PX = 440
private const val CARD_IMAGE_HEIGHT_PX = 248
```

---

## 2. Placeholder shimmer anime

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/composable/ImagePlaceholder.kt`

### Avant

Seul `rememberGradientPlaceholder()` existait — un gradient diagonal statique `surfaceBright -> surfaceDim`.

### Apres

Ajout de `rememberShimmerPlaceholder()` — un BrushPainter anime qui balaie un highlight de gauche a droite.

```kotlin
@Composable
fun rememberShimmerPlaceholder(): Painter {
    val baseColor = JellyfinTheme.colorScheme.surfaceBright
    val highlightColor = JellyfinTheme.colorScheme.surfaceContainer
    val transition = rememberInfiniteTransition(label = "img_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -500f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    return BrushPainter(
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(translateX, 0f),
            end = Offset(translateX + 500f, 0f),
        )
    )
}
```

### Animation

| Propriete | Valeur |
|-----------|--------|
| Duree cycle | 1200ms |
| Easing | LinearEasing |
| Mode | Restart infini |
| Couleur base | `surfaceBright` (~#1A1A20) |
| Couleur highlight | `surfaceContainer` (~#252530) |
| Largeur gradient | 500px |

### Coherence

Le shimmer reutilise les memes tokens couleur et la meme duree que `rememberShimmerBrush()` du skeleton system (`SkeletonShimmer.kt`), mais sous forme de `Painter` au lieu de `Brush` pour etre compatible avec le parametre `placeholder` d'`AsyncImage`.

### Impact

`rememberGradientPlaceholder()` n'est PAS supprime — il reste utilise par ~15 autres fichiers (detail pages, library browse, etc.). Seules les cards Home utilisent le shimmer.

---

## 3. Prefetch images row suivante

### Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvRowList.kt` | Ajout parametre `prefetchContent` |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeScreen.kt` | Fournit le callback de prefetch Coil |
| `app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` | `getItemImageUrl` passe de `private` a `internal` |

### TvRowList — nouveau parametre

```kotlin
fun <T> TvRowList(
    rows: List<TvRow<T>>,
    ...
    prefetchContent: ((items: List<T>) -> Unit)? = null,  // NOUVEAU
    itemContent: @Composable (T) -> Unit,
)
```

Quand une row a l'index N est composee, si `prefetchContent` est fourni et que la row N+1 existe, les items de la row N+1 sont passes au callback via `LaunchedEffect`.

### HomeScreen — callback de prefetch

```kotlin
val imageLoader = remember { SingletonImageLoader.get(context) }
val prefetchCallback = remember<(List<BaseItemDto>) -> Unit>(api) {
    { items ->
        for (nextItem in items) {
            val url = getItemImageUrl(nextItem, api, 220) ?: continue
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .size(Size(440, 248))
                    .scale(Scale.FILL)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(false)
                    .build()
            )
        }
    }
}
```

### Fonctionnement

1. L'utilisateur scroll vers le bas, la row 1 ("Continue Watching") est composee
2. Le `LaunchedEffect` de la row 1 declenche le prefetch des images de la row 2 ("Next Up")
3. Les images sont chargees en arriere-plan par Coil avec priorite normale
4. Quand la row 2 entre dans le viewport, les images sont deja en memory cache
5. Resultat : affichage quasi-instantane sans shimmer visible

### Parametres prefetch identiques aux cards

Les requetes de prefetch utilisent exactement les memes parametres (`size`, `scale`, `cachePolicy`) que les `ImageRequest` des `BrowseMediaCard`, ce qui garantit un cache hit quand la card est composee.

### Compatibilite

Le parametre `prefetchContent` est optionnel (`null` par defaut). Les 6 autres callers de `TvRowList` (ByLetterBrowseScreen, CollectionBrowseScreen, etc.) ne passent pas ce parametre et ne sont pas affectes.

---

## 4. Visibilite getItemImageUrl

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Avant

```kotlin
private fun getItemImageUrl(item: BaseItemDto, api: ApiClient, maxWidth: Int): String?
```

### Apres

```kotlin
internal fun getItemImageUrl(item: BaseItemDto, api: ApiClient, maxWidth: Int): String?
```

Passe de `private` a `internal` pour que `HomeScreen` puisse l'utiliser pour construire les URLs de prefetch. La fonction reste invisible hors du module `app`.

---

## 5. Imports modifies

### BrowseMediaCard.kt

**Ajoutes** :
```kotlin
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import androidx.compose.ui.platform.LocalContext
```

**Remplaces** :
```kotlin
// Avant
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
// Apres
import org.jellyfin.androidtv.ui.composable.rememberShimmerPlaceholder
```

### HomeScreen.kt

**Ajoutes** :
```kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import org.jellyfin.androidtv.ui.browsing.compose.getItemImageUrl
```

### ImagePlaceholder.kt

**Ajoutes** :
```kotlin
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
```

---

## 6. Build

```
./gradlew :app:compileGithubDebugKotlin   -> BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin -> BUILD SUCCESSFUL
```

## 7. Resume des changements

| Aspect | Avant | Apres |
|--------|-------|-------|
| Image decode | Taille originale serveur | 440x248px (taille exacte card) |
| Crossfade cards | 200ms (global ImageLoader) | Desactive (instantane) |
| Cache policies | Implicites (defaut Coil) | Explicites dans chaque request |
| Placeholder | Gradient statique diagonal | Shimmer anime gauche→droite 1200ms |
| Prefetch row+1 | Aucun | `ImageLoader.enqueue()` via `TvRowList.prefetchContent` |
| getItemImageUrl | `private` | `internal` (pour prefetch HomeScreen) |

### Impact performance attendu

| Metrique | Avant | Apres (estime) |
|----------|-------|----------------|
| Memoire par image | ~6MB (1920x1080 ARGB) | ~0.4MB (440x248 ARGB) |
| Temps decode | ~80ms/image | ~8ms/image |
| Temps percu (crossfade) | +200ms | 0ms |
| Row suivante | Chargement au scroll | Pre-chargee en cache |
