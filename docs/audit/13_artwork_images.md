# Audit 13 - Artwork & Image Quality

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Toutes les modifications vérifiées :
> - crossfade(200) global sur ImageLoader (AppModule.kt)
> - ImagePlaceholder.kt créé (rememberGradientPlaceholder, rememberErrorPlaceholder)
> - 11 fichiers avec placeholder gradient + 10 avec error fallback
> - Constante IMAGE_CROSSFADE = 200 dans AnimationDefaults.kt

## Changements implementes

### 1. Crossfade global (200ms)

| Propriete | Valeur |
|-----------|--------|
| Duree | 200ms (`AnimationDefaults.IMAGE_CROSSFADE`) |
| Scope | Global — configure sur `ImageLoader.Builder` dans `AppModule.kt` |
| Effet | Toutes les images chargees via Coil (Compose `AsyncImage` et View `AsyncImageView`) |
| Avant | Aucun crossfade sur les `coil3.compose.AsyncImage`, 100ms sur `AsyncImageView` legacy |
| Apres | 200ms uniforme partout |

Fichier modifie: `di/AppModule.kt` — ajout `crossfade(200)` sur le `ImageLoader.Builder`.

### 2. Placeholder elegant (gradient)

| Propriete | Valeur |
|-----------|--------|
| Type | Gradient lineaire diagonal |
| Couleurs | `surfaceBright` -> `surfaceDim` (theme-aware) |
| Fichier | `ui/composable/ImagePlaceholder.kt` |
| Composable | `rememberGradientPlaceholder(): Painter` |

Remplace le fond gris plat `outlineVariant` qui apparaissait pendant le chargement
par un degrade subtil qui s'integre naturellement au theme sombre.

### 3. Fallback erreur

| Propriete | Valeur |
|-----------|--------|
| Type | `ColorPainter` semi-transparent |
| Couleur | `surfaceContainer` a 50% d'opacite |
| Fichier | `ui/composable/ImagePlaceholder.kt` |
| Composable | `rememberErrorPlaceholder(): Painter` |

Quand le chargement d'une image echoue (404, timeout, etc.), un fond subtil
apparait au lieu d'un trou noir ou d'une icone cassee.

### 4. Fichiers modifies

| Fichier | Modification |
|---------|-------------|
| `di/AppModule.kt` | `crossfade(200)` global + import |
| `ui/base/AnimationDefaults.kt` | `IMAGE_CROSSFADE = 200` constante |
| `ui/composable/ImagePlaceholder.kt` | **Nouveau** — `rememberGradientPlaceholder()`, `rememberErrorPlaceholder()` |
| `ui/itemdetail/v2/ItemDetailsComponents.kt` | placeholder + error sur 8 composants : CastCard, SeasonCard, EpisodeCard, SeasonEpisodeItem, PosterImage, DetailBackdrop, SimilarItemCard, LandscapeItemCard |
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | placeholder sur backdrops (playlist, person), poster music, person photo |
| `ui/browsing/v2/LibraryBrowseComponents.kt` | placeholder + error sur LibraryPosterCard |
| `ui/browsing/v2/GenresGridV2Fragment.kt` | placeholder + error sur GenreCard |
| `ui/browsing/v2/ScheduleBrowseFragment.kt` | placeholder + error sur schedule cards |
| `ui/browsing/v2/RecordingsBrowseFragment.kt` | placeholder + error sur recording cards |
| `ui/browsing/v2/LiveTvBrowseFragment.kt` | placeholder + error sur live TV cards |
| `ui/browsing/v2/MusicBrowseFragment.kt` | placeholder + error sur music cards |
| `ui/shared/toolbar/LeftSidebarNavigation.kt` | placeholder sur library icons |

---

## Elements deja en place (pre-existants)

### BlurHash (placeholder perceptuel)

| Element | Detail |
|---------|--------|
| Decodeur | `util/BlurHashDecoder.kt` |
| Usage View | `ui/AsyncImageView.kt` — decode le blurHash avant le chargement |
| Usage Compose | `ui/composable/AsyncImage.kt` — `blurHashPainter()` disponible |
| Limite | Seul le wrapper View l'utilise; les `coil3.compose.AsyncImage` ne l'utilisent pas |

### Blur backdrop

| Element | Detail |
|---------|--------|
| Pre-API 31 | `util/BitmapBlur.kt` (StackBlur algorithm) |
| API 31+ | `android.graphics.RenderEffect.createBlurEffect()` |
| Compose | `Modifier.blur()` dans `ItemDetailsComponents.kt` (`DetailBackdrop`) |
| Service | `data/service/BackgroundService.kt` avec `BlurContext` enum |
| MediaBar | `ui/home/mediabar/MediaBarSlideshowView.kt` — RenderEffect blur |

### Gradient overlay (detail)

Deja en place dans `DetailBackdrop`:
```kotlin
Brush.verticalGradient(
    0.0f to Color.Transparent,
    0.3f to Color.Transparent,
    0.5f to gradientEnd.copy(alpha = 0.25f),
    0.65f to gradientEnd.copy(alpha = 0.63f),
    0.8f to gradientEnd.copy(alpha = 0.88f),
    1.0f to gradientEnd,
)
```

### Logo shadow adaptatif

`ui/shared/LogoView.kt` — analyse la luminosite du logo pour choisir ombre noire ou blanche.

### Cache images

| Propriete | Valeur |
|-----------|--------|
| RAM cache | 25% de la memoire disponible |
| Disk cache | 250 MB dans `image_cache/` |
| OkHttp | Fetcher authentifie via `OkHttpNetworkFetcherFactory` |

### Aspect ratios des cartes

| Type | Ratio | Fichier |
|------|-------|---------|
| Poster portrait | 2:3 | `ImageHelper.ASPECT_RATIO_2_3` |
| Paysage | 16:9 | `ImageHelper.ASPECT_RATIO_16_9` |
| Carre (musique) | 1:1 | Utilise directement (squareness) |
| Banner | ~5.4:1 | `ImageHelper.ASPECT_RATIO_BANNER` |

### Badges et overlays sur cartes

| Badge | Composant | Fichier |
|-------|-----------|---------|
| Favoris (coeur) | `StateIndicator` | `ItemCardBaseItemOverlay.kt` |
| Vu (check) | `WatchIndicator` | `ItemCardBaseItemOverlay.kt` |
| Non-vu (compteur) | `WatchIndicator` | `ItemCardBaseItemOverlay.kt` |
| Enregistrement | `StateIndicator` | `ItemCardBaseItemOverlay.kt` |
| Serveur multi-serveur | `ServerBadge` | `ItemCardBaseItemOverlay.kt` |
| Progression | `ProgressIndicator` (Seekbar) | `ItemCardBaseItemOverlay.kt` |
| Type badge | `LibraryPosterCard` | `LibraryBrowseComponents.kt` |

---

## Elements NON implementes et justification

### Palette API (couleur dynamique extraite du poster)

**Raison** : La Palette API Android necessite un `Bitmap` decompresse en memoire.
Sur un ecran de detail avec backdrop blur + poster + cast row, cela ajoute une
allocation bitmap supplementaire par navigation. Sur Fire TV Stick (1.5 GB RAM),
c'est un risque de pression memoire.

**Alternative actuelle** : Le `focusBorderColor()` configurable par l'utilisateur
dans les preferences (`AppTheme`) offre une personnalisation suffisante.

**Prerequis pour implementation future** : Utiliser `coil3.toBitmap()` sur l'image
deja cachee en memoire (eviter double decodage), limiter l'extraction a 1 couleur
dominante, et mesurer l'impact memoire sur Fire TV Stick.

### Focus glow (halo lumineux autour des cartes)

**Raison** : Un glow necessiterait `Modifier.shadow()` avec elevation et spread,
ce qui est couteux en GPU sur Android TV. Les ombres portees sont souvent
invisibles sur les TV OLED avec des noirs profonds.

**Alternative actuelle** : Bordure coloree 2dp + scale 1.06x sur focus.
Visuellement suffisant a distance de canape.

### Badges HDR/4K sur les cartes

**Raison** : Les metadata `MediaStream` (resolution, HDR) ne sont pas presentes
dans le `BaseItemDto` retourne par les endpoints de listing (`Items`, `LatestItems`).
Il faudrait un appel supplementaire par item (`/Items/{id}`) pour obtenir
`MediaSources[0].MediaStreams`, ce qui genererait N appels reseau par page.

**Alternative** : Ces badges sont visibles sur l'ecran de detail (`MediaBadgeChip`)
ou les informations completes sont deja chargees.

### Shared element transition (carte -> detail)

Meme justification que dans l'audit 12 : la navigation utilise des Fragments Android,
pas Compose Navigation. Impossible sans migration complete.

---

## Configuration existante du chargement d'images

### Coil 3.3.0 — ImageLoader global

```kotlin
// AppModule.kt
ImageLoader.Builder(context).apply {
    crossfade(200)                           // NOUVEAU
    serviceLoaderEnabled(false)
    memoryCache { 25% RAM }
    diskCache { 250 MB }
    components {
        OkHttpNetworkFetcherFactory          // auth cookies
        AnimatedImageDecoder / GifDecoder
        SvgDecoder
    }
}
```

### Fichiers utilisant `coil3.compose.AsyncImage`

| Fichier | Composant(s) | placeholder | error | crossfade |
|---------|-------------|-------------|-------|-----------|
| `ItemDetailsComponents.kt` | CastCard, SeasonCard, EpisodeCard, SeasonEpisodeItem, PosterImage, DetailBackdrop, SimilarItemCard, LandscapeItemCard | Oui | Oui (sauf backdrop) | Global |
| `ItemDetailsFragment.kt` | Backdrop slideshow, logo, poster, person photo | Oui | Poster/photo oui | Global |
| `LibraryBrowseComponents.kt` | LibraryPosterCard | Oui | Oui | Global |
| `GenresGridV2Fragment.kt` | GenreCard | Oui | Oui | Global |
| `ScheduleBrowseFragment.kt` | Schedule card | Oui | Oui | Global |
| `RecordingsBrowseFragment.kt` | Recording card | Oui | Oui | Global |
| `LiveTvBrowseFragment.kt` | LiveTV card | Oui | Oui | Global |
| `MusicBrowseFragment.kt` | Music card | Oui | Oui | Global |
| `LeftSidebarNavigation.kt` | Library icon | Oui | Non (petit) | Global |
| `rating.kt` | Rating icon | Non (icon) | Non | Global |

### Fichiers utilisant `rememberAsyncImagePainter`

| Fichier | Usage | Justification pas de placeholder |
|---------|-------|----------------------------------|
| `MainToolbar.kt` | Avatar utilisateur | Gere par `ProfilePicture` (icon fallback) |
| `ProfilePicture.kt` | Photo de profil | Affiche `ic_user` quand non charge |
| `LogoView.kt` | Logo media bar | Analyse brightness, pas de placeholder |
| `MediaBarSlideshowView.kt` | Ken Burns backdrop | Crossfade entre slides |

---

## Impact

| Metrique | Avant | Apres |
|----------|-------|-------|
| Fichiers avec crossfade sur AsyncImage | 0/14 Compose | 14/14 (global) |
| Fichiers avec placeholder gradient | 0 | 11 |
| Fichiers avec error fallback | 0 | 10 |
| Duree crossfade standardisee | Varie (0, 100, 400ms) | 200ms global |
| Constante `IMAGE_CROSSFADE` | N/A | `AnimationDefaults.IMAGE_CROSSFADE = 200` |
| Nouveau composable utilitaire | 0 | 2 (`rememberGradientPlaceholder`, `rememberErrorPlaceholder`) |
