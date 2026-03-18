# Audit — Poster film absent sur la page détail

## Résumé

Investigation du problème d'absence de poster dans la colonne droite de la page détail pour les films.

---

## Point 1 — Appel API et champs demandés

### `ItemDetailsViewModel.loadItem()` (ligne 98-104)

```kotlin
val item = withContext(Dispatchers.IO) {
    effectiveApi.userLibraryApi
        .getItem(itemId = itemId)
        .content
}
```

L'appel `userLibraryApi.getItem()` est fait **sans paramètre `fields`**. Contrairement aux endpoints de liste (`getItems`, `getNextUp`, `getResumeItems`) qui utilisent `ItemRepository.itemFields`, l'endpoint single-item retourne normalement l'objet complet par défaut — **y compris `imageTags`**.

**Note** : L'endpoint `getItem()` de Jellyfin retourne tous les champs par défaut. L'absence de `fields` ne devrait PAS être la cause du problème car c'est un endpoint individuel, pas un endpoint de liste. Cependant, ce comportement pourrait varier selon la version du serveur Jellyfin.

### `ItemRepository.itemFields`

Les champs définis incluent `PRIMARY_IMAGE_ASPECT_RATIO` mais pas de champ spécifique "ImageTags" car ce n'est pas un `ItemFields` — `imageTags` fait partie de la réponse standard de `BaseItemDto`.

---

## Point 2 — Condition d'affichage de CinemaPosterColumn

### `MovieDetailsContent.kt` (ligne 101 et 374)

```kotlin
val posterUrl = getPosterUrl(item, api)  // ligne 101

// ligne 374
if (posterUrl != null || playedPercentage > 0) {
    CinemaPosterColumn(
        posterUrl = posterUrl,
        playedPercentage = playedPercentage,
        watchedMinutes = watchedMinutes,
    )
}
```

**Risque identifié** : Si `posterUrl` est `null` ET le film n'a jamais été regardé (`playedPercentage == 0`), la colonne entière n'est **pas rendue**. Aucun placeholder, aucune icône — simplement absente du layout.

**Impact** : Le layout passe de deux colonnes (info + poster) à une seule colonne (info pleine largeur), ce qui change visuellement toute la page.

---

## Point 3 — Pipeline `itemImages` → `getPosterUrl()`

### `JellyfinImage.kt` — propriété `itemImages`

```kotlin
val BaseItemDto.itemImages
    get() = imageTags
        ?.mapValues { (type, tag) ->
            JellyfinImage(item = id, source = ITEM, type = type, tag = tag, ...)
        }.orEmpty()
```

**Point critique** : Si `imageTags` est `null`, `.orEmpty()` retourne une map vide → `itemImages[ImageType.PRIMARY]` retourne `null` → `getPosterUrl()` retourne `null`.

### `DetailUtils.kt` — bloc MOVIE

```kotlin
else -> {
    item.itemImages[ImageType.PRIMARY]?.getUrl(api, maxHeight = 600)
}
```

Dépend entièrement de `item.imageTags` contenant une entrée `PRIMARY`.

---

## Point 4 — Validité de l'URL construite

### `JellyfinImage.getUrl()`

```kotlin
fun JellyfinImage.getUrl(api: ApiClient, ...): String {
    if (tag.startsWith("http")) return tag  // URL externe
    return api.imageApi.getItemImageUrl(
        itemId = item,
        imageType = type,
        tag = tag,
        maxHeight = maxHeight, ...
    )
}
```

Si l'URL est construite, elle est valide tant que `itemId` et `tag` sont corrects. L'URL pointe vers le serveur Jellyfin de l'utilisateur. Pas de risque d'URL invalide ici.

---

## Causes possibles

### Hypothèse A — `imageTags` null/vide (probable)

Si le serveur Jellyfin retourne `imageTags = null` ou `imageTags = {}` pour certains films, la chaîne complète échoue silencieusement :

```
imageTags = null
  → itemImages = emptyMap()
    → itemImages[PRIMARY] = null
      → getPosterUrl() = null
        → condition (null || 0) = false
          → CinemaPosterColumn non rendu
```

**Vérification recommandée** : Ajouter un log temporaire dans `getPosterUrl()` :
```kotlin
Timber.d("POSTER_DBG: type=${item.type} name=${item.name} imageTags=${item.imageTags}")
```

### Hypothèse B — Film sans poster sur le serveur (possible)

Certains films peuvent ne pas avoir de poster uploadé/scrapé sur le serveur Jellyfin. Dans ce cas, `imageTags` ne contient pas d'entrée `PRIMARY` et le comportement est correct mais l'UX est mauvaise (colonne absente).

### Hypothèse C — Problème de désérialisation SDK (peu probable)

Le SDK Jellyfin pourrait ne pas désérialiser correctement `imageTags` dans certaines versions. Peu probable car les saisons et séries utilisent le même mécanisme et fonctionnent.

---

## Recommandations

### Correction immédiate — Afficher toujours la colonne

Changer la condition dans `MovieDetailsContent.kt` pour toujours afficher la colonne poster, avec un placeholder si l'image est absente :

```kotlin
// AVANT
if (posterUrl != null || playedPercentage > 0) {
    CinemaPosterColumn(...)
}

// APRÈS — toujours afficher
CinemaPosterColumn(
    posterUrl = posterUrl,
    playedPercentage = playedPercentage,
    watchedMinutes = watchedMinutes,
)
```

Et dans `CinemaPosterColumn`, afficher une icône placeholder quand `posterUrl` est null :
```kotlin
if (posterUrl != null) {
    AsyncImage(...)
} else {
    Icon(VegafoXIcons.Movie, tint = VegafoXColors.TextSecondary, ...)
}
```

### Diagnostic — Ajouter un log temporaire

Ajouter dans `getPosterUrl()` un log pour capturer les cas où le poster est absent et comprendre pourquoi.

---

## Fichiers concernés

| Fichier | Lignes | Rôle |
|---|---|---|
| `ItemDetailsViewModel.kt` | 98-104 | Appel API `getItem()` sans `fields` |
| `DetailUtils.kt` | 41-43 | `getPosterUrl()` bloc MOVIE — dépend de `itemImages[PRIMARY]` |
| `JellyfinImage.kt` | 86-99 | `itemImages` — dépend de `imageTags` non-null |
| `MovieDetailsContent.kt` | 101, 374 | Condition d'affichage `posterUrl != null \|\| playedPercentage > 0` |
| `ItemDetailsComponents.kt` | 1749-1813 | `CinemaPosterColumn` — pas de placeholder quand `posterUrl` est null |
