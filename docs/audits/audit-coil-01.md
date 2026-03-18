# Audit Coil — Configuration et usages images (audit-coil-01)

**Date** : 2026-03-16
**Scope** : Configuration globale Coil, usages AsyncImage, préchargement, placeholders, URLs Jellyfin, performance

---

## Étape 1 — Configuration globale Coil

### Version et dépendances

- **Version** : Coil 3.4.0 (`io.coil-kt.coil3`)
- **Modules** : `coil-core`, `coil-compose`, `coil-network-okhttp`, `coil-gif`, `coil-svg`

### Fichiers de configuration

| Fichier | Rôle |
|---------|------|
| `di/AppModule.kt` | Configuration principale ImageLoader (cache, decoders, OkHttp) |
| `JellyfinApplication.kt` | `SingletonImageLoader.Factory` → injection Koin |
| `util/coil/CoilTimberLogger.kt` | Logger custom via Timber |
| `util/coil/coilConnectivityChecker.kt` | Checker custom (force `true`) |
| `util/coil/SubsetTransformation.kt` | Crop/subset pour seekbar trickplay |

### Cache mémoire

```kotlin
memoryCache {
    MemoryCache.Builder()
        .maxSizePercent(context, percent = 0.25)
        .strongReferencesEnabled(true)
        .build()
}
```

- **Taille** : 25% de la RAM disponible (dynamique)
- **Strong references** : activées (meilleure perf, risque OOM sur appareils faibles)

### Cache disque

```kotlin
diskCache {
    DiskCache.Builder()
        .directory(context.cacheDir.resolve("image_cache").toOkioPath())
        .maxSizeBytes(512L * 1024 * 1024)
        .build()
}
```

- **Taille** : 512 MB
- **Répertoire** : `<cacheDir>/image_cache/`

### Client HTTP (OkHttp)

```kotlin
OkHttpNetworkFetcherFactory(
    callFactory = {
        okHttpFactory.createClient(httpClientOptions)
            .newBuilder()
            .dispatcher(Dispatcher().apply {
                maxRequests = 4
                maxRequestsPerHost = 4
            }).build()
    },
    connectivityChecker = ::createCoilConnectivityChecker,
)
```

- **Dispatcher** : 4 requêtes max (total et par host)
- **Timeouts** : hérités du SDK Jellyfin (non configurés explicitement pour Coil)
- **Connectivity Checker** : retourne toujours `true` (support serveurs locaux/LAN)

### Interceptors

- Aucun interceptor custom ajouté pour Coil
- Interceptors du SDK Jellyfin hérités via `OkHttpFactory.createClient()`

### Fetchers et Decoders

| Type | Détail |
|------|--------|
| **NetworkFetcher** | OkHttp (via `OkHttpNetworkFetcherFactory`) |
| **AnimatedImageDecoder** | API 28+ (images animées natives) |
| **GifDecoder** | API < 28 (fallback GIF) |
| **SvgDecoder** | Support SVG complet |
| **Service Loader** | Désactivé (`serviceLoaderEnabled(false)`) |

### Crossfade global

```kotlin
crossfade(200)
```

- **Activé** : oui, 200 ms par défaut

### Logger

```kotlin
logger(CoilTimberLogger(if (BuildConfig.DEBUG) Logger.Level.Warn else Logger.Level.Error))
```

- **Debug** : niveau `Warn`
- **Release** : niveau `Error`
- **Implémentation** : Timber avec tag `CoilTimberLogger.<tag>`

### respectCacheHeaders

- Non configuré explicitement → **valeur par défaut Coil 3 : `true`**
- Les headers HTTP `Cache-Control`, `ETag` sont respectés

### Résumé configuration

| Aspect | Valeur |
|--------|--------|
| Cache mémoire | 25% RAM, strongRefs=true |
| Cache disque | 512 MB, `image_cache/` |
| Max requêtes | 4 total, 4 par host |
| Crossfade | 200 ms |
| Logger debug | Warn (Timber) |
| Logger release | Error (Timber) |
| respectCacheHeaders | true (default) |
| Connectivity | Toujours true |
| SVG/GIF | Supportés |

---

## Étape 2 — Usages AsyncImage et ImageRequest

### Par type de contenu

#### Images Hero/Backdrop

| Fichier | Composable | placeholder | error | crossfade | cache | size/scale | memoryCacheKey |
|---------|-----------|-------------|-------|-----------|-------|------------|----------------|
| `HomeHeroBackdrop.kt:155` | `HomeHeroBackdrop` | ✗ | ✗ | ✗ (Crossfade parent) | ✗ | ✗ | ✗ |
| `DetailHeroBackdrop.kt:63` | `DetailHeroBackdrop` | ✗ | ✗ | ✗ (Crossfade parent) | ✗ | ✗ | ✗ |
| `MusicDetailsContent.kt:142` | backdrop slideshow | `rememberGradientPlaceholder()` | ✗ | ✗ (Crossfade 1000ms parent) | ✗ | ✗ | ✗ |
| `PersonDetailsContent.kt:104` | backdrop slideshow | ✗ | ✗ | ✗ (Crossfade 1000ms parent) | ✗ | ✗ | ✗ |

**Observations** :
- Les backdrops Hero (Home et Detail) n'ont **aucun placeholder** → zone vide pendant le chargement
- ContentScale : toujours `Crop`
- Alpha systématique (0.6–0.8) + blur (API 31+) via `graphicsLayer`

#### Cartes Browse (landscape 16:9)

| Fichier | Composable | placeholder | error | crossfade | cache | size/scale | memoryCacheKey |
|---------|-----------|-------------|-------|-----------|-------|------------|----------------|
| `BrowseMediaCard.kt:184` | `BrowseMediaCard` | `rememberGradientPlaceholder()` | `rememberErrorPlaceholder()` | `crossfade(false)` | memory+disk ENABLED | 440×248px, FILL | ✗ |

**Observations** :
- **Seul composable avec `ImageRequest.Builder` complet** : size, scale, cache, crossfade explicite
- Callbacks `onLoading`, `onSuccess`, `onError` avec logging Timber
- Crossfade désactivé volontairement (probablement pour éviter le double-fade avec les transitions de navigation)

#### Cartes Poster (portrait)

| Fichier | Composable | placeholder | error | crossfade | cache | size/scale | memoryCacheKey |
|---------|-----------|-------------|-------|-----------|-------|------------|----------------|
| `MediaPosterCard.kt:82` | `MediaPosterCard` | `rememberGradientPlaceholder()` | `rememberErrorPlaceholder()` | ✗ | ✗ | ✗ | ✗ |

**Observations** :
- URL string directe, pas d'`ImageRequest.Builder`
- Pas de contrainte de size → image décodée à taille native (potentiel gaspillage mémoire)

#### Avatars / Profils

| Fichier | Composable | Type Coil | placeholder | error | size |
|---------|-----------|-----------|-------------|-------|------|
| `ProfilePicture.kt:29` | `ProfilePicture` | `rememberAsyncImagePainter` | fallback Icon `VegafoXIcons.Person` | ✗ | `aspectRatio(1f)` |
| `ProfileItem.kt:162` | `ProfileItem` | `rememberAsyncImagePainter` | fallback initiale lettre | ✗ | `size(36.dp)` circle |
| `JellyseerrPersonCard.kt:72` | `JellyseerrPersonCard` | `AsyncImage` | `rememberGradientPlaceholder()` | ✗ | `size(64.dp)` circle |

**Observations** :
- Les painters utilisent `painterState.collectAsState()` + fallback conditionnel (pas de placeholder Coil natif)
- Pas d'`error` drawable pour les avatars

#### Pochettes musique / Logos

| Fichier | Composable | placeholder | error | crossfade | size |
|---------|-----------|-------------|-------|-----------|------|
| `MusicDetailsContent.kt:297` | album/artist logo | ✗ | ✗ | ✗ | `300×80.dp` |
| `AudioNowPlayingScreen.kt` | artwork panel | custom wrapper | ✗ | ✗ | variable |
| `NowPlayingView.kt:97` | now playing mini | `R.drawable.ic_album` | ✗ | ✗ | `35.dp` circle |

#### Jellyseerr (TMDB)

| Fichier | Composable | placeholder | error | crossfade | cache | size |
|---------|-----------|-------------|-------|-----------|-------|------|
| `JellyseerrCards.kt` poster | `JellyseerrPosterCard` | `rememberGradientPlaceholder()` | ✗ | ✗ | ✗ | ✗ |
| `JellyseerrCards.kt` genre | `JellyseerrGenreCard` | ✗ | ✗ | ✗ | ✗ | ✗ |
| `JellyseerrCards.kt` logo | `JellyseerrLogoCard` | ✗ | ✗ | ✗ | ✗ | ✗ |

#### Logos avec shadow

| Fichier | Composable | Type Coil | Particularité |
|---------|-----------|-----------|---------------|
| `LogoView.kt:52` | `LogoView` | `rememberAsyncImagePainter` + `ImageRequest.Builder` | `allowHardware(false)`, analyse brightness bitmap, shadow adaptatif, blur API 31+ |

### Usages de SubcomposeAsyncImage

**Aucun usage dans tout le projet.**

### Synthèse des patterns

| Pattern | Occurrences | Détail |
|---------|-------------|--------|
| `AsyncImage` (Coil 3) | ~15+ | Standard pour la majorité des images |
| `rememberAsyncImagePainter` | 4 | ProfilePicture, ProfileItem, LogoView (×2) — pour state tracking + fallback |
| `ImageRequest.Builder` | 2 | BrowseMediaCard (complet), LogoView (minimal) |
| `SubcomposeAsyncImage` | 0 | Non utilisé |

---

## Étape 3 — Préchargement des images

### Système de préchargement implémenté

#### 1. HomePrefetchService (préchargement API)

**Fichier** : `ui/home/compose/HomePrefetchService.kt`

- Lancé depuis `JellyfinApplication.onSessionStart()` en arrière-plan (`SupervisorJob + Dispatchers.IO`)
- Précharge **avant l'affichage de MainActivity** :
  1. **Continue Watching** : `getResumeItems()` (max 50 items)
  2. **Next Up** : `getNextUp()` (max 50 items)
- Résultats sauvés dans `MutableStateFlow<_prefetchedRows>`
- Logs de perf : `VFX_PERF_PREFETCH` (T0, T0→T1, T1→T2 timings)

**Note** : Ce service précharge les **données API**, pas les images elles-mêmes.

#### 2. Préchargement images des cartes LazyRow

**Fichier** : `ui/home/compose/HomeScreen.kt` (lignes 253-273)

```kotlin
// prefetchCallback dans HomeScreen
imageLoader.enqueue(
    ImageRequest.Builder(context)
        .data(imageUrl)
        .size(Size(PREFETCH_IMAGE_WIDTH, PREFETCH_IMAGE_HEIGHT))  // 440×248px
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .crossfade(false)
        .build()
)
```

- Précharge les **5 premiers items de la ligne suivante** via `TvRowList.prefetchContent`
- Déclenché dans `LaunchedEffect` quand une ligne devient visible (`rowIndex + 1`)
- Taille : 440×248px (identique aux cartes)

#### 3. Images Hero de la Home

- **PAS de préchargement** des images Hero/Backdrop
- L'image Hero est chargée **à la demande** quand l'item reçoit le focus
- URL obtenue via `getBackdropUrl(item, api)` dans `HomeHeroBackdrop.kt`

#### Résumé préchargement

| Aspect | État |
|--------|------|
| Préchargement données API (Home) | ✅ Implémenté (HomePrefetchService) |
| Préchargement images cartes LazyRow | ✅ Implémenté (5 items ligne suivante) |
| Préchargement images Hero/Backdrop | ❌ Non implémenté |
| `rememberPreloadingData` | ❌ Non utilisé |

---

## Étape 4 — Placeholders et états d'erreur

### Composables de placeholder

**Fichier** : `ui/composable/ImagePlaceholder.kt`

#### 1. `rememberGradientPlaceholder()` (lignes 24-30)

- **Type** : Gradient diagonal statique (Compose `Painter`)
- **Couleurs** : `surfaceBright` → `surfaceDim` (`Brush.linearGradient`)
- **Usage** : Placeholder par défaut pour les images normales

#### 2. `rememberShimmerPlaceholder()` (lignes 38-61)

- **Type** : Shimmer animé (Compose `Painter`)
- **Animation** : translate X infini (−500px → +1500px) sur 1200ms (`LinearEasing`)
- **Couleurs** : gradient mobile `surfaceBright` → `surfaceContainer` → `surfaceBright`
- **Usage** : Disponible mais optionnel (non utilisé massivement)

#### 3. `rememberErrorPlaceholder()` (lignes 64-68)

- **Type** : `ColorPainter` semi-transparente
- **Couleur** : `surfaceContainer.copy(alpha = 0.5f)`
- **Usage** : Fallback en cas d'erreur de chargement

### BlurHash (legacy)

**Fichier** : `util/BlurHashDecoder.kt`

- Décode les blurHash Jellyfin en Bitmap pour placeholder flou
- Utilisé dans `AsyncImageView.load()` (composant legacy non-Compose)
- Désactivé automatiquement sur appareils low-RAM

### Couverture des placeholders

| Composable | placeholder | error | Verdict |
|-----------|-------------|-------|---------|
| `BrowseMediaCard` | ✅ gradient | ✅ error | Complet |
| `MediaPosterCard` | ✅ gradient | ✅ error | Complet |
| `JellyseerrPosterCard` | ✅ gradient | ✗ | Partiel |
| `JellyseerrPersonCard` | ✅ gradient | ✗ | Partiel |
| `PersonDetailsContent` poster | ✅ gradient | ✅ error | Complet |
| `MusicDetailsContent` backdrop | ✅ gradient | ✗ | Partiel |
| `HomeHeroBackdrop` | ❌ | ❌ | **Absent** |
| `DetailHeroBackdrop` | ❌ | ❌ | **Absent** |
| `PersonDetailsContent` backdrop | ❌ | ❌ | **Absent** |
| `JellyseerrGenreCard` | ❌ | ❌ | **Absent** |
| `JellyseerrLogoCard` | ❌ | ❌ | **Absent** |
| `MusicDetailsContent` logo | ❌ | ❌ | **Absent** |
| `ProfilePicture` | fallback Icon | ✗ | Fonctionnel (différent) |
| `ProfileItem` | fallback lettre | ✗ | Fonctionnel (différent) |
| `NowPlayingView` | `R.drawable.ic_album` | ✗ | Fonctionnel (différent) |

**Résumé** : ~60% des AsyncImage Compose ont un placeholder, ~40% affichent du vide pendant le chargement.

---

## Étape 5 — Gestion des URLs Jellyfin

### Architecture centrale

**Fichier** : `util/apiclient/JellyfinImage.kt`

#### Data class `JellyfinImage`

```kotlin
data class JellyfinImage(
    val item: UUID,
    val source: JellyfinImageSource,  // ITEM, PARENT, ALBUM, SERIES, CHANNEL, USER, CHAPTER
    val type: ImageType,              // PRIMARY, BACKDROP, THUMB, LOGO, BANNER, CHAPTER
    val tag: String,                  // Tag serveur OU URL complète (images externes)
    val blurHash: String?,
    val aspectRatio: Float?,
    val index: Int?,
)
```

#### Fonction `getUrl()`

```kotlin
fun getUrl(api: ApiClient, maxWidth/maxHeight/fillWidth/fillHeight/quality) → String
```

- Si `tag` commence par `http` → retourne directement (images TMDB/externes)
- Si source `USER` → `api.imageApi.getUserImageUrl(userId, tag)`
- Sinon → `api.imageApi.getItemImageUrl(itemId, imageType, tag, index, maxWidth, maxHeight, fillWidth, fillHeight, quality)`

### Paramètre tag (invalidation de cache)

- **Toujours inclus** dans les URLs via `getUrl()` → le SDK l'intègre comme paramètre `tag=`
- Fourni par le serveur Jellyfin via `imageTags`, `backdropImageTags`, `primaryImageTag`
- Change quand l'image est modifiée sur le serveur → **invalidation de cache correcte**

### Extensions d'extraction

**Fichier** : `util/apiclient/BaseItemImageExtensions.kt`

```kotlin
BaseItemDto.getPrimaryImage() → itemImages[PRIMARY]
BaseItemDto.getLogoImage()    → itemImages[LOGO] ?: parentImages[LOGO]
BaseItemDto.getThumbImage()   → itemImages[THUMB]
BaseItemDto.getBackdropImage() → itemImages[BACKDROP]
BaseItemDto.getPrimaryImageWithFallback() → itemImages[PRIMARY] ?: parentImages[PRIMARY]
```

### Dimensions demandées vs affichage

| Contexte | Dimensions demandées | Affichage réel | Cohérence |
|----------|---------------------|----------------|-----------|
| Cartes browse (BrowseMediaCard) | `fillWidth=440px`, quality=96 | 440×248px (CARD_IMAGE_WIDTH/HEIGHT_PX) | ✅ Parfait |
| Backdrop Home/Detail | `maxWidth=3840px`, quality=96 | plein écran TV (1920px typ.) | ⚠️ Surdimensionné ×2 |
| Poster détail | `maxHeight=600px` | ~240.dp (~480px @2×) | ✅ Correct |
| Logo détail | `maxWidth=400px` | `300×80.dp` (~600×160px @2×) | ✅ Acceptable |
| Episode thumbnail | `maxWidth=400px` | variable | ✅ OK |
| Cast person photo | `maxHeight=280px` | ~240.dp (~480px @2×) | ✅ Correct |
| MediaPosterCard | **aucune contrainte** | ~180×270.dp | ❌ **Taille native** |

### Images externes (TMDB/Jellyseerr)

| Source | Pattern | Taille |
|--------|---------|--------|
| TMDB poster | `https://image.tmdb.org/t/p/w500{path}` | 500px largeur |
| TMDB backdrop | `https://image.tmdb.org/t/p/w780{path}` | 780px largeur |
| TMDB profile | `https://image.tmdb.org/t/p/w185{path}` | 185px largeur |

- URL complète stockée dans `JellyfinImage.tag`
- Détectée par `tag.startsWith("http")` dans `getUrl()`

### Chaîne de fallback images (BrowseMediaCard)

```
THUMB → BACKDROP → PARENT_THUMB → PARENT_BACKDROP → PRIMARY → PARENT_PRIMARY
```

Toujours avec `fillWidth=440, quality=96`.

---

## Étape 6 — Problèmes potentiels de performance

### P1 — Images backdrop décodées en taille excessive

- **Où** : `DetailSections.getBackdropUrl()` demande `maxWidth=3840`
- **Problème** : Les TV Android typiques affichent en 1920×1080. Demander 3840px fait télécharger et décoder une image 2× trop grande
- **Impact** : Gaspillage mémoire (~16 MB par backdrop 3840×2160 au lieu de ~8 MB), téléchargement plus long
- **Recommandation** : Réduire à `maxWidth=1920` ou `maxWidth=2560` pour les appareils 4K détectés

### P2 — MediaPosterCard sans contrainte de size

- **Où** : `MediaPosterCard.kt` — URL string directe sans `ImageRequest.Builder`
- **Problème** : L'image est décodée à sa taille native serveur (potentiellement 1000+ px) alors que la carte affiche ~180×270dp
- **Impact** : Gaspillage mémoire significatif, surtout dans les grilles avec beaucoup de posters
- **Recommandation** : Ajouter un `ImageRequest.Builder` avec `size()` et `scale(Scale.FILL)` comme `BrowseMediaCard`

### P3 — Absence de placeholder sur les Hero backdrops

- **Où** : `HomeHeroBackdrop.kt`, `DetailHeroBackdrop.kt`, `PersonDetailsContent.kt` backdrop
- **Problème** : Zone complètement vide pendant le chargement → layout shift visible, flash de contenu
- **Impact** : Perception de lenteur, UI instable
- **Recommandation** : Ajouter `rememberGradientPlaceholder()` ou utiliser les blurHash disponibles dans `JellyfinImage`

### P4 — Absence de crossfade sur les cartes browse

- **Où** : `BrowseMediaCard.kt` — `crossfade(false)` explicite
- **Problème** : L'image apparaît instantanément sans transition, ce qui peut causer un flash blanc si le placeholder est affiché brièvement
- **Impact** : Mineur — le gradient placeholder masque en partie le problème
- **Note** : Possiblement voulu pour éviter un double-fade avec la transition de navigation

### P5 — Pas de préchargement des images Hero

- **Où** : `HomeScreen.kt` — les cartes sont préchargées mais pas les backdrops Hero
- **Problème** : Quand l'utilisateur navigue sur un item, le backdrop met un temps perceptible à apparaître
- **Impact** : Latence visible sur la Home, surtout en réseau lent
- **Recommandation** : Précharger le backdrop de l'item focusé + les 2 items adjacents dans le carrousel

### P6 — Pas de memoryCacheKey stable dans les LazyRow

- **Où** : La majorité des `AsyncImage` dans les listes défilables
- **Problème** : Sans `memoryCacheKey` explicite, Coil utilise l'URL comme clé. Si l'URL change (paramètres différents), l'image est rechargée
- **Impact** : Faible dans ce projet car les URLs sont stables grâce au système de `tag`
- **Note** : Le tag Jellyfin sert naturellement de discriminant de cache, donc le risque est limité

### P7 — Cache disque 512 MB pour TV

- **Où** : `AppModule.kt`
- **Évaluation** : 512 MB est **raisonnable** pour un usage TV avec beaucoup d'images haute résolution
- **Impact** : Correct — pas de problème identifié
- **Note** : Les backdrops 3840px occupent ~200-500 KB compressés, donc 512 MB peut stocker ~1000+ images backdrop

### P8 — Dispatcher limité à 4 requêtes parallèles

- **Où** : `AppModule.kt`
- **Évaluation** : 4 requêtes max peut être un goulot d'étranglement lors du scroll rapide dans les grilles (beaucoup d'images à charger simultanément)
- **Impact** : Moyen — les images en bas de page doivent attendre leur tour
- **Recommandation** : Envisager 8 requêtes max si le serveur Jellyfin le supporte

### P9 — respectCacheHeaders activé par défaut

- **Où** : Configuration Coil globale (non configuré → default `true`)
- **Problème** : Si le serveur Jellyfin envoie des headers `Cache-Control: no-cache` ou des TTL courts, les images seront re-téléchargées malgré le tag stable
- **Impact** : Dépend de la configuration du serveur Jellyfin
- **Recommandation** : Vérifier les headers renvoyés par le serveur. Si problématique, passer à `respectCacheHeaders(false)` puisque le système de `tag` gère déjà l'invalidation

### P10 — Incohérence de configuration entre composables

- **Où** : Tout le projet
- **Problème** : `BrowseMediaCard` est le seul composable à configurer correctement size, scale, cache et crossfade via `ImageRequest.Builder`. Tous les autres utilisent des URLs directes sans optimisation
- **Impact** : Performance inégale, certaines images consomment plus de mémoire que nécessaire
- **Recommandation** : Standardiser via un helper composable ou une factory d'`ImageRequest` partagée

---

## Résumé exécutif

| Catégorie | État | Détail |
|-----------|------|--------|
| Configuration globale | ✅ Bonne | Cache 25% RAM + 512 MB disque, crossfade 200ms, SVG/GIF |
| Préchargement API | ✅ Implémenté | HomePrefetchService (Continue Watching + Next Up) |
| Préchargement images cartes | ✅ Implémenté | 5 items ligne suivante via TvRowList |
| Préchargement images Hero | ❌ Absent | Backdrop chargé au focus uniquement |
| Placeholders cartes | ✅ Gradient + error | BrowseMediaCard, MediaPosterCard |
| Placeholders Hero | ❌ Absent | HomeHeroBackdrop, DetailHeroBackdrop |
| URLs avec tag | ✅ Systématique | Invalidation de cache correcte |
| Size constraints | ⚠️ Partiel | BrowseMediaCard=OK, MediaPosterCard=manquant, Backdrop=surdimensionné |
| ImageRequest.Builder | ⚠️ Sous-utilisé | 1 seul composable (BrowseMediaCard) l'utilise complètement |
| memoryCacheKey | ⚠️ Non explicite | Risque faible grâce aux tags stables |

### Priorités de correction

1. **P1 + P2** : Contraindre les tailles d'images (backdrop 1920px, posters avec size explicite) — gain mémoire immédiat
2. **P3** : Ajouter placeholders aux Hero backdrops — amélioration UX perceptible
3. **P5** : Précharger les backdrops Hero — fluidité navigation Home
4. **P10** : Factoriser un helper ImageRequest — cohérence du codebase
5. **P8** : Augmenter le dispatcher à 8 requêtes — scroll rapide dans les grilles
