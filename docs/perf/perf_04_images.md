# Perf Fix #04 — Optimisation images Coil

**Date** : 2026-03-11
**Build** : VegafoX v1.6.2 github-debug / github-release
**Statut** : BUILD SUCCESSFUL (debug + release)

---

## Problemes identifies (perf_01_diagnostic.md)

| Probleme | Mesure avant fix |
|---|---|
| Temps moyen image (cache chaud) | **1 252ms** |
| Images en erreur (cache froid) | **100%** (Connection reset) |
| Qualite JPEG demandee | 90 (surdimensionne pour TV) |
| Cache disque Coil | 250 MB |
| Requetes image simultanees | Illimite (saturation serveur) |
| Fallback image | PRIMARY (portrait) crope en 16:9 |
| Prefetch par row | Tous les items (jusqu'a 50) |

---

## Fixes appliques

### FIX 1 — Qualite JPEG 90 → 70

**Fichier** : `BrowseMediaCard.kt:getItemImageUrl()`

Toutes les URL image passent de `quality=90` a `quality=70`.
Gain theorique ~40% sur la taille des images, imperceptible sur ecran TV a distance de visionnage.

### FIX 2 — Concurrence limitee a 4 requetes

**Fichier** : `AppModule.kt` (OkHttpNetworkFetcherFactory)

Le client OkHttp dedie a Coil est configure avec un `Dispatcher` limite :
- `maxRequests = 4`
- `maxRequestsPerHost = 4`

Cela empeche la saturation du serveur Jellyfin au demarrage (cause des "Connection reset" en cache froid).

### FIX 3 — Cache disque 250 MB → 512 MB

**Fichier** : `AppModule.kt` (ImageLoader diskCache)

Le cache disque passe de 250 MB a 512 MB. Cela permet de conserver davantage d'images entre les sessions et reduit les re-telechargements.

### FIX 4 — Prefetch limite aux 5 premiers items

**Fichier** : `HomeScreen.kt` (prefetchCallback)

Le prefetch ne charge plus que les 5 premiers items de chaque row (`items.take(5)`) au lieu de tous les items. Cela reduit la charge au demarrage et priorise les images visibles a l'ecran.

### FIX 5 — Fallback BACKDROP avant PRIMARY

**Fichier** : `BrowseMediaCard.kt:getItemImageUrl()`

Nouvelle priorite : **THUMB → BACKDROP → PRIMARY** (item puis parent).
BACKDROP est nativement 16:9 et evite le crop destructif du poster portrait PRIMARY dans les cards paysage.

---

## Comparaison avant / apres (estimations)

| Metrique | Avant | Apres (attendu) |
|---|---|---|
| Taille image moyenne | ~120 KB (q90) | ~72 KB (q70, -40%) |
| Requetes simultanees | Illimite | 4 max |
| Erreurs cache froid | 100% | ~0% (pas de saturation) |
| Temps image (cache chaud) | 1 252ms | < 400ms (cible) |
| Cache disque | 250 MB | 512 MB |
| Prefetch par row | Tous items | 5 premiers |
| Format fallback | PRIMARY crope | BACKDROP natif 16:9 |

---

## Fichiers modifies

| Fichier | Modification |
|---|---|
| `app/.../ui/browsing/compose/BrowseMediaCard.kt` | quality 70, BACKDROP fallback, imports |
| `app/.../di/AppModule.kt` | OkHttp Dispatcher(4), diskCache 512MB |
| `app/.../ui/home/compose/HomeScreen.kt` | `items.take(5)` dans prefetch |

---

## Validation

- [x] `assembleGithubDebug` : BUILD SUCCESSFUL
- [x] `assembleGithubRelease` : BUILD SUCCESSFUL
- [ ] Test perf sur Ugoos (apres `adb shell pm clear com.vegafox.androidtv.debug`)
