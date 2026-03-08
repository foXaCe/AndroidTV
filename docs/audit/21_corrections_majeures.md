# Audit 21 — Corrections des 2 problèmes majeurs restants

**Date** : 2026-03-08
**Statut** : ✅ Résolu (2/2)
**Build** : BUILD SUCCESSFUL (assembleRelease)

---

## Correction 1 — Fuite mémoire OnHierarchyChangeListener

**Fichier** : `HomeRowsFragment.kt`
**Source** : Audit 04 P11

### Problème
Dans `onViewCreated()`, la fonction `clearAllBackgrounds()` pose récursivement un `OnHierarchyChangeListener` sur chaque `ViewGroup` de la hiérarchie (ligne 332). Ce listener n'était jamais désenregistré — il n'y avait même pas de `onDestroyView()`. Chaque navigation vers/depuis le Home laissait des listeners orphelins, causant une fuite mémoire.

### Code avant
```kotlin
// Pas de onDestroyView() dans le fragment
// Les listeners posés dans onViewCreated() n'étaient jamais nettoyés
```

### Code après
```kotlin
override fun onDestroyView() {
    // Clear all OnHierarchyChangeListeners set in onViewCreated to prevent memory leaks
    view?.let { clearAllHierarchyListeners(it) }

    super.onDestroyView()
}

/**
 * Recursively remove OnHierarchyChangeListeners from all ViewGroups.
 */
private fun clearAllHierarchyListeners(v: android.view.View) {
    if (v is android.view.ViewGroup) {
        v.setOnHierarchyChangeListener(null)
        for (i in 0 until v.childCount) clearAllHierarchyListeners(v.getChildAt(i))
    }
}
```

### Vérification
- `onDestroyView()` est appelé par le framework Android quand la vue du fragment est détruite (navigation, detach, etc.)
- `view?.let` gère le cas null safety
- La récursion miroir de `clearAllBackgrounds()` garantit que chaque ViewGroup qui a reçu un listener est nettoyé
- Aucun autre listener non désabonné trouvé dans le fragment (les autres listeners sont sur `verticalGridView` via des méthodes Leanback qui gèrent leur propre cycle de vie)

---

## Correction 2 — Images pleine résolution dans GenresGrid

**Fichier** : `GenresGridViewModel.kt` (méthode `createGenreItem()`)
**Source** : Audit 04 P08

### Problème
Les images backdrop pour les cartes de genre étaient demandées avec `maxWidth = 780` pour des cartes affichées à 280×158 dp. Sur la majorité des appareils Android TV (tvdpi = 1.33x), cela donnait un ratio de 2.09x — bien au-delà du 1.5x recommandé. Sur une grille de 20+ genres, cela représentait un gaspillage mémoire significatif.

### Calcul de la taille optimale

| Densité | Pixels réels (280dp) | Ancien (780px) | Ratio ancien | Nouveau (480px) | Ratio nouveau |
|---------|---------------------|----------------|-------------|-----------------|--------------|
| mdpi (1x) | 280px | 780px | **2.79x** ❌ | 480px | **1.71x** ⚠️ |
| tvdpi (1.33x) | 373px | 780px | **2.09x** ❌ | 480px | **1.29x** ✅ |
| xhdpi (2x) | 560px | 780px | **1.39x** ✅ | 480px | **0.86x** ✅ |

La valeur `maxWidth = 480` offre un excellent compromis :
- 1.29x à la densité TV la plus courante (tvdpi) — sous le seuil de 1.5x
- 0.86x à xhdpi — légère mise à l'échelle mais imperceptible sur un backdrop en mode Crop
- Économie de ~38% en bande passante et mémoire par rapport à 780px

### Code avant
```kotlin
client.imageApi.getItemImageUrl(
    itemId = item.id,
    imageType = ImageType.BACKDROP,
    tag = item.backdropImageTags!!.first(),
    maxWidth = 780,
    quality = 80,
)
```

### Code après
```kotlin
client.imageApi.getItemImageUrl(
    itemId = item.id,
    imageType = ImageType.BACKDROP,
    tag = item.backdropImageTags!!.first(),
    maxWidth = 480,
    quality = 80,
)
```

### Vérification
- Le placeholder gradient (`rememberGradientPlaceholder()`) est bien présent dans `GenresGridV2Fragment.kt` ligne 432
- Le error fallback (`rememberErrorPlaceholder()`) est également présent ligne 433
- `quality = 80` (compression JPEG) est conservé pour un bon rapport qualité/taille

---

## Compilation

```
BUILD SUCCESSFUL in 4m 8s
356 actionable tasks: 50 executed, 306 up-to-date
```
