# H_FIX2 — Cards paysage 16:9 + artworks haute qualite

**Date** : 2026-03-10

---

## 1. Cards paysage 16:9 (style Netflix)

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Avant

| Propriete | Valeur |
|-----------|--------|
| Largeur | 150dp |
| Hauteur | 225dp |
| Ratio | 2:3 (portrait poster) |
| Coins | `JellyfinTheme.shapes.small` |

### Apres

| Propriete | Valeur |
|-----------|--------|
| Largeur | 220dp |
| Hauteur | calculee = `cardWidth * (9f / 16f)` = 123.75dp |
| Ratio | 16:9 (paysage) |
| Coins | `RoundedCornerShape(10.dp)` |

### Detail des changements

- Parametre `cardWidth` passe de `150.dp` a `220.dp`
- Parametre `cardHeight` supprime — remplace par `val cardImageHeight = cardWidth * (9f / 16f)` calcule dynamiquement
- Clip shape change de `JellyfinTheme.shapes.small` a `RoundedCornerShape(10.dp)`
- La zone image utilise `cardImageHeight` au lieu de `cardHeight`
- Aucun caller ne passait `cardHeight` explicitement, donc aucun autre fichier a modifier

### Callers (tous utilisent les defauts, aucun changement requis)

| Fichier | Ligne |
|---------|-------|
| `HomeScreen.kt:167` | `BrowseMediaCard(item, api, onFocus, onClick)` |
| `SearchScreen.kt:174` | `BrowseMediaCard(item, api, onClick, modifier)` |
| `ByLetterBrowseScreen.kt:184` | `BrowseMediaCard(item, api, onClick)` |
| `AllFavoritesScreen.kt:200` | `BrowseMediaCard(item, api, onClick)` |
| `FolderViewScreen.kt:167` | `BrowseMediaCard(item, api, onClick)` |
| `SuggestedMoviesScreen.kt:175` | `BrowseMediaCard(item, api, onClick)` |
| `CollectionBrowseScreen.kt:171` | `BrowseMediaCard(item, api, onClick)` |

---

## 2. Artworks haute qualite — priorite des types d'images

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` — fonction `getItemImageUrl()`

### Avant (portrait-first)

```
PRIMARY → THUMB → parent PRIMARY → parent THUMB
```

Toutes les images demandees via `maxHeight`, pas de parametre `quality`.

### Apres (paysage-first pour films/series)

**Movies, Series et autres (non-episodes)** :

```
BACKDROP (itemBackdropImages) → THUMB → PRIMARY → parent BACKDROP → parent THUMB → parent PRIMARY
```

**Episodes** :

```
PRIMARY (screenshot episode, deja 16:9) → THUMB → parent THUMB → parent PRIMARY
```

### Sources d'images utilisees

| Source | Extension | Type d'image |
|--------|-----------|-------------|
| `item.itemBackdropImages` | `backdropImageTags` (liste) | BACKDROP 16:9 HD |
| `item.itemImages[ImageType.THUMB]` | `imageTags` (map) | THUMB 16:9 |
| `item.itemImages[ImageType.PRIMARY]` | `imageTags` (map) | PRIMARY (poster ou screenshot) |
| `item.parentBackdropImages` | `parentBackdropImageTags` (liste) | BACKDROP parent |
| `item.parentImages[ImageType.THUMB]` | `parentThumbItemId` + tag | THUMB parent |
| `item.parentImages[ImageType.PRIMARY]` | `parentPrimaryImageItemId` + tag | PRIMARY parent |

### Distinction BACKDROP vs THUMB

- **BACKDROP** : Stocke dans `backdropImageTags` (liste, un item peut en avoir plusieurs). Acces via `itemBackdropImages.firstOrNull()`. Haute resolution, 16:9.
- **THUMB** : Stocke dans `imageTags[ImageType.THUMB]` (unique). Acces via `itemImages[ImageType.THUMB]`. Plus petite resolution que BACKDROP, 16:9.
- **PRIMARY** : Poster portrait 2:3 pour films/series, screenshot 16:9 pour episodes.

---

## 3. Parametre quality=90

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` — `getItemImageUrl()`

### Avant

```kotlin
primary.getUrl(api, maxHeight = maxHeight)
```

### Apres

```kotlin
backdrop.getUrl(api, maxWidth = maxWidth, quality = 90)
```

### Detail

- Tous les appels `getUrl()` dans `getItemImageUrl` passent desormais `quality = 90`
- Le parametre `quality` existait deja dans `JellyfinImage.getUrl()` mais n'etait pas utilise par les cards
- Le SDK transmet `quality` dans l'URL Jellyfin : `/Items/{id}/Images/{type}?quality=90&maxWidth=220`
- Changement de `maxHeight` a `maxWidth` car les images paysage sont mieux dimensionnees par largeur

---

## 4. Imports modifies

### Ajoutes

```kotlin
import org.jellyfin.androidtv.util.apiclient.itemBackdropImages
import org.jellyfin.androidtv.util.apiclient.parentBackdropImages
import org.jellyfin.sdk.model.api.BaseItemKind
```

### Supprimes

```kotlin
import org.jellyfin.androidtv.util.ImageHelper  // inutilise
```

---

## 5. Build

```
./gradlew :app:compileGithubDebugKotlin   -> BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin -> BUILD SUCCESSFUL
```

## 6. Resume des changements

| Aspect | Avant | Apres |
|--------|-------|-------|
| Format card | 150x225dp (portrait 2:3) | 220x124dp (paysage 16:9) |
| Coins | `JellyfinTheme.shapes.small` | `RoundedCornerShape(10.dp)` |
| Image film/serie | PRIMARY (poster) | BACKDROP → THUMB → PRIMARY |
| Image episode | PRIMARY (poster) | PRIMARY (screenshot) → THUMB |
| Quality | non specifie (defaut serveur) | 90 |
| Dimension request | `maxHeight` | `maxWidth` |
