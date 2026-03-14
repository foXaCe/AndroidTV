# H_FIX5 — Cards Home : priorite THUMB au lieu de BACKDROP

**Date** : 2026-03-11

---

## Probleme

Les cards de la Home affichaient les images **BACKDROP** (grand format cinematographique, haute resolution, pas optimise pour les grilles) au lieu des **THUMB** (vignettes 16:9 optimisees, plus nettes et mieux cadrees pour le petit format).

## Solution

Changement de la priorite d'images dans `getItemImageUrl()` :

### Avant (h_fix2)

**Films/Series** :
```
BACKDROP → THUMB → PRIMARY → parent BACKDROP → parent THUMB → parent PRIMARY
```

**Episodes** :
```
PRIMARY → THUMB → parent THUMB → parent PRIMARY
```

### Apres (h_fix5)

**Tous les types de media (unifie)** :
```
THUMB → PRIMARY → parent THUMB → parent PRIMARY
```

- **BACKDROP** : completement retire de la chaine de priorite des cards
- **Episodes** : THUMB en premier aussi (capture d'episode = meilleur rendu grille)
- **Logique unifiee** : plus de branche `if (EPISODE) / else`, meme priorite pour tout

## Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Fonction `getItemImageUrl()` — avant

```kotlin
internal fun getItemImageUrl(item: BaseItemDto, api: ApiClient, maxWidth: Int): String? {
    if (item.type == BaseItemKind.EPISODE) {
        val primary = item.itemImages[ImageType.PRIMARY]
        if (primary != null) return primary.getUrl(api, maxWidth = maxWidth, quality = 90)
        val thumb = item.itemImages[ImageType.THUMB]
        if (thumb != null) return thumb.getUrl(api, maxWidth = maxWidth, quality = 90)
        val parentThumb = item.parentImages[ImageType.THUMB]
        if (parentThumb != null) return parentThumb.getUrl(api, maxWidth = maxWidth, quality = 90)
        val parentPrimary = item.parentImages[ImageType.PRIMARY]
        if (parentPrimary != null) return parentPrimary.getUrl(api, maxWidth = maxWidth, quality = 90)
    } else {
        val backdrop = item.itemBackdropImages.firstOrNull()
        if (backdrop != null) return backdrop.getUrl(api, maxWidth = maxWidth, quality = 90)
        val thumb = item.itemImages[ImageType.THUMB]
        if (thumb != null) return thumb.getUrl(api, maxWidth = maxWidth, quality = 90)
        val primary = item.itemImages[ImageType.PRIMARY]
        if (primary != null) return primary.getUrl(api, maxWidth = maxWidth, quality = 90)
        val parentBackdrop = item.parentBackdropImages.firstOrNull()
        if (parentBackdrop != null) return parentBackdrop.getUrl(api, maxWidth = maxWidth, quality = 90)
        val parentThumb = item.parentImages[ImageType.THUMB]
        if (parentThumb != null) return parentThumb.getUrl(api, maxWidth = maxWidth, quality = 90)
        val parentPrimary = item.parentImages[ImageType.PRIMARY]
        if (parentPrimary != null) return parentPrimary.getUrl(api, maxWidth = maxWidth, quality = 90)
    }
    return null
}
```

### Fonction `getItemImageUrl()` — apres

```kotlin
internal fun getItemImageUrl(item: BaseItemDto, api: ApiClient, maxWidth: Int): String? {
    // Priority: THUMB → PRIMARY (poster). Never BACKDROP for cards.
    // THUMB = optimized 16:9 thumbnail, sharper and better framed for grids.

    val thumb = item.itemImages[ImageType.THUMB]
    if (thumb != null) return thumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)

    val primary = item.itemImages[ImageType.PRIMARY]
    if (primary != null) return primary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)

    // Parent fallbacks: THUMB → PRIMARY
    val parentThumb = item.parentImages[ImageType.THUMB]
    if (parentThumb != null) return parentThumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)

    val parentPrimary = item.parentImages[ImageType.PRIMARY]
    if (parentPrimary != null) return parentPrimary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)

    return null
}
```

## Changements de parametres URL

| Parametre | Avant | Apres |
|-----------|-------|-------|
| Dimensionnement | `maxWidth = maxWidth` (220) | `fillWidth = CARD_IMAGE_WIDTH_PX` (440) |
| URL generee | `/Items/{id}/Images/Thumb?quality=90&maxWidth=220` | `/Items/{id}/Images/Thumb?quality=90&fillWidth=440` |

- `fillWidth` demande au serveur de remplir exactement la largeur specifiee (meilleure nettete)
- `CARD_IMAGE_WIDTH_PX = 440` correspond a 220dp × 2x densite, deja defini dans le fichier

## Imports retires

```kotlin
import org.jellyfin.androidtv.util.apiclient.itemBackdropImages   // plus utilise
import org.jellyfin.androidtv.util.apiclient.parentBackdropImages  // plus utilise
import org.jellyfin.sdk.model.api.BaseItemKind                     // plus utilise
```

## Build

```
./gradlew :app:compileGithubDebugKotlin   -> BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin -> BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        -> BUILD SUCCESSFUL
./gradlew :app:assembleGithubRelease      -> BUILD SUCCESSFUL
```

## Installation AM9 Pro

```
adb install -r vegafox-androidtv-v1.6.2-github-debug.apk   -> Success
adb install -r vegafox-androidtv-v1.6.2-github-release.apk -> Success
```

## Resume

| Aspect | Avant (h_fix2) | Apres (h_fix5) |
|--------|----------------|----------------|
| Priorite films/series | BACKDROP → THUMB → PRIMARY | THUMB → PRIMARY |
| Priorite episodes | PRIMARY → THUMB | THUMB → PRIMARY |
| BACKDROP dans cards | Oui (1er choix) | Non (jamais) |
| Logique | Branche episode/non-episode | Unifiee |
| Dimensionnement URL | `maxWidth=220` | `fillWidth=440` |
| Imports inutiles | 3 (backdrop, BaseItemKind) | Retires |
