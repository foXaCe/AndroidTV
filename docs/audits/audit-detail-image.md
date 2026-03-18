# Audit — Image colonne droite page détail

## Résumé

La colonne droite de la page détail affiche une image via `CinemaPosterColumn` avec un ratio 2:3 portrait, coins arrondis 16dp, et `ContentScale.Crop`. L'URL est construite par `getPosterUrl()` dans `DetailUtils.kt` qui route vers différents `ImageType` selon le type de contenu.

---

## État actuel par type de contenu

### Film (MOVIE)

| Champ | Valeur |
|---|---|
| **ImageType demandé** | `PRIMARY` |
| **Fallback** | Aucun — `null` si absent |
| **Sizing** | `maxHeight = 600` |
| **Fonction** | `getPosterUrl()` → `item.itemImages[ImageType.PRIMARY]` |
| **Condition d'affichage** | `posterUrl != null \|\| playedPercentage > 0` |
| **Correction nécessaire** | Non — affiche bien le poster Primary |

### Série (SERIES)

| Champ | Valeur |
|---|---|
| **ImageType demandé** | `PRIMARY` |
| **Fallback** | Aucun — `null` si absent |
| **Sizing** | `maxHeight = 600` |
| **Fonction** | `getPosterUrl()` → `item.itemImages[ImageType.PRIMARY]` |
| **Condition d'affichage** | `posterUrl != null` |
| **Correction nécessaire** | Non — affiche bien le poster Primary |

### Saison (SEASON)

| Champ | Valeur |
|---|---|
| **ImageType demandé** | `PRIMARY` (saison) |
| **Fallback** | `seriesPrimaryImage` (poster de la série parente via `seriesId` + `seriesPrimaryImageTag`) |
| **Sizing** | `maxHeight = 600` |
| **Fonction** | `getPosterUrl()` → `item.itemImages[ImageType.PRIMARY] ?: item.seriesPrimaryImage` |
| **Condition d'affichage** | `posterUrl != null` |
| **Correction nécessaire** | Non — logique correcte (poster saison → poster série en fallback) |

### Épisode (EPISODE)

| Champ | Valeur |
|---|---|
| **ImageType demandé** | **`THUMB` en priorité** |
| **Fallback** | `PRIMARY` (screenshot de l'épisode) |
| **Sizing** | `maxWidth = 500` (paysage, pas portrait !) |
| **Fonction** | `getPosterUrl()` → `item.itemImages[ImageType.THUMB] ?: item.itemImages[ImageType.PRIMARY]` |
| **Condition d'affichage** | `posterUrl != null \|\| playedPercentage > 0` |
| **Correction nécessaire** | **OUI** — affiche un thumbnail paysage dans un cadre portrait 2:3, ce qui est visuellement mauvais |

**Problème** : L'épisode demande `THUMB` (vignette paysage 16:9) en priorité, avec fallback sur `PRIMARY` (qui est aussi souvent un screenshot paysage pour les épisodes). L'image est ensuite croppée dans un cadre 2:3 portrait via `ContentScale.Crop`, ce qui perd une grande partie de l'image.

**Ce qui devrait être affiché** : Le poster `PRIMARY` de la **série parente** (via `seriesId` + `seriesPrimaryImageTag`), identique à la logique utilisée pour les saisons. L'épisode ne possède généralement pas de poster portrait propre — le poster de la série est la seule image adaptée au ratio 2:3.

---

## Tableau récapitulatif

| Type | Image actuelle | Image attendue | Correction |
|---|---|---|---|
| **Film** | PRIMARY (poster) | PRIMARY (poster) | Aucune |
| **Série** | PRIMARY (poster) | PRIMARY (poster) | Aucune |
| **Saison** | PRIMARY saison → PRIMARY série | PRIMARY saison → PRIMARY série | Aucune |
| **Épisode** | THUMB → PRIMARY (paysage) | PRIMARY série (portrait) | **À corriger** |
| **BoxSet** | PRIMARY | PRIMARY | Aucune |
| **Trailer** | PRIMARY | PRIMARY | Aucune |
| **MusicVideo** | PRIMARY | PRIMARY | Aucune |
| **Recording** | PRIMARY | PRIMARY | Aucune |

---

## Correction recommandée

Dans `DetailUtils.kt`, modifier le bloc `EPISODE` de `getPosterUrl()` :

```kotlin
// AVANT (actuel)
item.type == BaseItemKind.EPISODE -> {
    val thumbImage = item.itemImages[ImageType.THUMB]
    val primaryImage = item.itemImages[ImageType.PRIMARY]
    (thumbImage ?: primaryImage)?.getUrl(api, maxWidth = 500)
}

// APRÈS (correction)
item.type == BaseItemKind.EPISODE -> {
    val seriesPoster = item.seriesPrimaryImage
    val seasonPoster = item.itemImages[ImageType.PRIMARY]
    (seriesPoster ?: seasonPoster)?.getUrl(api, maxHeight = 600)
}
```

Cela affichera le poster de la série parente pour les épisodes, avec fallback sur l'image PRIMARY de l'épisode si la série n'a pas de poster. Le sizing passe de `maxWidth=500` (paysage) à `maxHeight=600` (portrait) pour être cohérent avec les autres types.

---

## Pipeline de construction d'URL

```
ContentScreen.kt
  ↓ getPosterUrl(item, api)
DetailUtils.kt
  ↓ item.itemImages[ImageType.XXX] ou item.seriesPrimaryImage
JellyfinImage.kt
  ↓ JellyfinImage(item=UUID, source=ITEM/SERIES, type=PRIMARY/THUMB, tag=...)
  ↓ .getUrl(api, maxHeight/maxWidth)
  ↓ api.imageApi.getItemImageUrl(itemId, imageType, tag, maxHeight, ...)
CinemaPosterColumn (ItemDetailsComponents.kt)
  ↓ AsyncImage(model=url, contentScale=Crop, aspectRatio=2/3)
```

## Comportement quand posterUrl est null

- `CinemaPosterColumn` affiche un `Box` vide avec fond `surfaceDim`
- Pas d'icône placeholder, pas de texte
- Pour Film/Épisode : la colonne est quand même affichée si `playedPercentage > 0` (barre de progression)
- Pour Série/Saison : la colonne n'est pas rendue du tout
