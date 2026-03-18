# Audit Trickplay — audit-player-trickplay-01

**Date** : 2026-03-16
**Scope** : Implémentation trickplay (preview images seekbar) dans VegafoX Android TV

---

## Point 1 — Implémentation côté seekbar

### Seekbar Compose (`ui/base/Seekbar.kt`)

Le composable `Seekbar` est une **barre de progression pure graphique**. Il dessine :
- une barre de fond (`backgroundColor`)
- une barre de buffer (`bufferColor`)
- une barre de progression (`progressColor`)
- un knob circulaire (`knobColor`)

**Aucune logique trickplay n'est présente** :
- Pas de paramètre pour recevoir un `Bitmap` ou `ImagePainter`
- Pas de composable enfant pour afficher une image preview
- Pas de callback `onThumbnailRequest` ou similaire
- Le scrubbing (flèches gauche/droite) ne déclenche aucun chargement d'image

### PlayerSeekbar (`ui/player/base/PlayerSeekbar.kt`)

Wrapper Compose qui connecte `Seekbar` au `PlaybackManager`. Il passe :
- `progress`, `buffer`, `duration` depuis `positionInfo`
- `onScrubbing` → `playbackManager.state.setScrubbing()`
- `onSeek` → `playbackManager.state.seek()`

**Aucune référence à `SeekProvider`, `trickplay`, `thumbnail`, ou `preview`.**

### VideoPlayerControls (`ui/player/video/VideoPlayerControls.kt`)

Composable parent qui inclut `PlayerSeekbar`. Contient les boutons play/pause, rewind, forward, sous-titres, audio, chapitres, qualité, vitesse, zoom.

**Aucune intégration trickplay** : pas d'import de `SeekProvider`, pas de state pour les thumbnails, pas de composable d'affichage de preview au-dessus de la seekbar.

### Verdict Point 1

> **Le player Compose actuel n'a AUCUNE implémentation de trickplay dans la seekbar.** La seekbar est une barre graphique simple sans capacité d'affichage d'images preview.

---

## Point 2 — API Jellyfin trickplay

### SDK Jellyfin utilisé

**Version** : `1.8.6` (défini dans `gradle/libs.versions.toml`)

Le SDK **expose les endpoints trickplay** via l'extension `trickplayApi` :

```kotlin
import org.jellyfin.sdk.api.client.extensions.trickplayApi
```

### Méthode API utilisée dans le code

**`getTrickplayTileImageUrl()`** — génère l'URL d'une sprite sheet trickplay :

```kotlin
api.trickplayApi.getTrickplayTileImageUrl(
    itemId: UUID,        // ID de l'item vidéo
    width: Int,          // Largeur d'une tile individuelle (px)
    index: Int,          // Index de la sprite sheet (0, 1, 2...)
    mediaSourceId: UUID, // ID de la source média
)
```

### Endpoints Jellyfin standard

| Endpoint | Description |
|----------|-------------|
| `GET /Videos/{itemId}/Trickplay` | Métadonnées trickplay (dimensions, intervalle, résolutions) |
| `GET /Videos/{itemId}/Trickplay/{width}/tiles.jpg?index={n}&mediaSourceId={id}` | Image sprite sheet |

### Données trickplay dans BaseItemDto

Le SDK expose les métadonnées via `BaseItemDto.trickplay` :

```kotlin
item.trickplay?: Map<String, Map<Int, TrickplayInfo>>
```

- Clé 1 : `mediaSourceId` (String)
- Clé 2 : résolution/largeur (Int)
- Valeur : `TrickplayInfo` avec :
  - `interval` — intervalle en ms entre chaque frame
  - `tileWidth` — nombre de colonnes dans la sprite sheet
  - `tileHeight` — nombre de lignes dans la sprite sheet
  - `width` — largeur d'une tile (px)
  - `height` — hauteur d'une tile (px)

### Fichiers contenant des appels API trickplay

| Fichier | Lignes | Utilisation |
|---------|--------|-------------|
| `ui/playback/overlay/SeekProvider.kt` | 149, 261, 367 | `getTrickplayTileImageUrl()` — 3 appels |

**Aucun autre fichier ne fait d'appel API trickplay.**

### Verdict Point 2

> **Le SDK 1.8.6 supporte pleinement le trickplay.** Les endpoints sont exposés et utilisés dans `SeekProvider.kt`. Cependant, `SeekProvider` n'est instancié nulle part dans le code actif.

---

## Point 3 — ViewModel et state

### SeekProvider — État non-réactif

`SeekProvider.kt` gère l'état trickplay en interne avec des structures mutables :

| Champ | Type | Rôle |
|-------|------|------|
| `diskCacheReady` | `@Volatile Boolean` | Indique si le cache disque est prêt |
| `preloadedThumbnails` | `ConcurrentHashMap<Int, Bitmap>` | Cache mémoire des thumbnails extraits |
| `pendingPreloads` | `ConcurrentHashMap<Int, Boolean>` | Préchargements en cours |
| `lastPreloadCenter` | `Int` | Dernière position centrale de préchargement |
| `lastSeekDirection` | `Int` | Direction du scrub (1=avant, -1=arrière) |
| `imageRequests` | `MutableMap<Int, Disposable>` | Requêtes Coil actives |

### Aucun StateFlow/LiveData

- **Pas de `StateFlow`** pour les données trickplay
- **Pas de `LiveData`**
- **Pas de `MutableState` Compose**
- L'état est entièrement **callback-based** via `ThumbnailCallback`

### PlaybackManager / PlayerState (playback:core)

Le module `playback/core` (`PlayerState.kt`) expose :
- `playState: StateFlow<PlayState>`
- `scrubbing: StateFlow<Boolean>`
- `positionInfo: PositionInfo`

**Aucune propriété trickplay** dans `PlayerState`.

### Préférence utilisateur

```kotlin
// UserPreferences.kt:364
var trickPlayEnabled = booleanPreference("trick_play_enabled", false)
```

Default : **`false`** (désactivé).

### Verdict Point 3

> **Aucun ViewModel ni StateFlow n'expose de données trickplay.** `SeekProvider` gère tout en interne avec des callbacks et des maps concurrentes. L'état trickplay est complètement découplé du système réactif Compose.

---

## Point 4 — SubsetTransformation

### Fichier : `util/coil/SubsetTransformation.kt`

**Transformation Coil 3** qui extrait une région rectangulaire d'un bitmap (sprite sheet → tile individuelle).

### Constructeur

```kotlin
class SubsetTransformation(
    private val x: Int,      // Coordonnée X de départ (pixels depuis la gauche)
    private val y: Int,      // Coordonnée Y de départ (pixels depuis le haut)
    private val width: Int,  // Largeur de la région à extraire (px)
    private val height: Int, // Hauteur de la région à extraire (px)
) : Transformation()
```

### Méthode transform

1. **Clamp** les coordonnées pour rester dans les limites du bitmap source
2. **Valide** que la région clampée a des dimensions positives
3. **Retourne le bitmap original** si la région est invalide (avec log Timber)
4. **Extrait le sous-bitmap** via `Bitmap.createBitmap(input, x, y, w, h)`

### Cache key

```kotlin
override val cacheKey: String = "$x,$y,$width,$height"
```

Chaque combinaison `(x, y, width, height)` est cachée séparément par Coil.

### Utilisation dans le projet

**Utilisé uniquement dans `SeekProvider.kt`** — 2 occurrences :

1. **Ligne 276** — `preloadThumbnailsAroundPosition()` : préchargement de thumbnails en arrière-plan
2. **Ligne 385** — `getThumbnail()` : chargement à la demande lors du scrubbing

### Logique d'extraction depuis la sprite sheet

```kotlin
// Calcul de la position dans la grille
val currentTile = currentTimeMs.floorDiv(trickPlayInfo.interval).toInt()
val tileSize = trickPlayInfo.tileWidth * trickPlayInfo.tileHeight
val tileOffset = currentTile % tileSize
val tileIndex = currentTile / tileSize  // Index de la sprite sheet

val tileOffsetX = tileOffset % trickPlayInfo.tileWidth   // Colonne
val tileOffsetY = tileOffset / trickPlayInfo.tileWidth    // Ligne
val offsetX = tileOffsetX * trickPlayInfo.width           // Pixel X
val offsetY = tileOffsetY * trickPlayInfo.height          // Pixel Y

// Extraction
transformations(SubsetTransformation(offsetX, offsetY, trickPlayInfo.width, trickPlayInfo.height))
```

**Exemple** : sprite sheet 500×500px, grille 5×5, tiles 100×100px :
- Tile à la position (2,1) → `SubsetTransformation(200, 100, 100, 100)`

### Verdict Point 4

> **`SubsetTransformation` est un outil de crop de sprite sheet trickplay.** Il fonctionne correctement et est bien implémenté. Son seul consommateur (`SeekProvider`) n'est pas connecté au player Compose actuel.

---

## Point 5 — Configuration serveur

### Endpoints Jellyfin trickplay

Le serveur Jellyfin expose :

| Paramètre | Source | Description |
|-----------|--------|-------------|
| URL sprite sheet | `GET /Videos/{id}/Trickplay/{width}/tiles.jpg` | Image grille de thumbnails |
| Nombre de colonnes | `TrickplayInfo.tileWidth` | Ex: 10 tiles par ligne |
| Nombre de lignes | `TrickplayInfo.tileHeight` | Ex: 10 tiles par colonne |
| Largeur tile | `TrickplayInfo.width` | Ex: 320px |
| Hauteur tile | `TrickplayInfo.height` | Ex: 180px |
| Intervalle | `TrickplayInfo.interval` | Ex: 10000ms (10s) |

### Vérification serveur JellyMox

Le serveur JellyMox (192.168.1.61:8096 ou .60:8096) doit avoir le trickplay activé dans :
**Dashboard > Bibliothèques > Options avancées > Trickplay**

Les métadonnées trickplay sont incluses dans la réponse `BaseItemDto` quand on récupère un item avec les champs `Trickplay` :
```
GET /Users/{userId}/Items/{itemId}?Fields=Trickplay
```

Si `item.trickplay` est `null` pour un item donné, soit :
1. Le trickplay n'est pas activé sur le serveur
2. Le trickplay n'a pas encore été généré pour cet item (tâche planifiée)
3. L'item n'est pas une vidéo

### Verdict Point 5

> **À vérifier manuellement** si le serveur JellyMox a le trickplay activé et si les items contiennent des métadonnées trickplay. Le code client est prêt à les consommer.

---

## Point 6 — Cause probable de l'absence d'affichage

### Diagnostic

| Couche | État | Détail |
|--------|------|--------|
| SDK API | ✅ Disponible | `trickplayApi` exposé dans SDK 1.8.6 |
| SeekProvider | ✅ Implémenté | Chargement, cache disque, cache mémoire, extraction sprite sheet |
| SubsetTransformation | ✅ Implémenté | Crop de sprite sheet fonctionnel |
| Préférence utilisateur | ✅ Existe | `trickPlayEnabled`, default `false` |
| UI Settings | ✅ Existe | Toggle dans Developer Settings |
| **Instanciation SeekProvider** | ❌ **ABSENT** | Aucun fichier n'instancie `SeekProvider` |
| **Intégration Seekbar** | ❌ **ABSENT** | `Seekbar.kt` n'a aucun paramètre pour afficher un thumbnail |
| **Composable preview** | ❌ **ABSENT** | Aucun composable n'affiche un `Bitmap` au-dessus de la seekbar |
| **State reactif** | ❌ **ABSENT** | Pas de `StateFlow` pour les thumbnails trickplay |

### Cause racine

**Le trickplay n'est PAS connecté au player Compose actuel.**

`SeekProvider` a été implémenté pour un ancien player Leanback (XML) qui a été entièrement remplacé par le player Compose. Lors de la migration :

1. Le player Leanback a été supprimé
2. Un nouveau `Seekbar` Compose a été créé sans aucune capacité trickplay
3. `SeekProvider` est resté dans le code mais **n'est instancié nulle part**
4. Aucun composable d'affichage de preview n'a été créé pour le player Compose

### Preuves dans le git

- `77ef06de3` — "Implement TrickPlay images" (implémentation originale, ère Leanback)
- `ce2023a4b` — "Show placeholder thumbnail while Trickplay thumbnail loads"
- `37276955b` — "Trickplay Thumbnail Performance" (optimisations)
- Migration Compose ultérieure → SeekProvider orpheliné

### Fichiers confirmant l'absence de connexion

| Fichier | Preuve |
|---------|--------|
| `SeekProvider.kt` | 0 importeurs (seulement le test) |
| `PlayerSeekbar.kt` | Aucun import de SeekProvider |
| `VideoPlayerControls.kt` | Aucune référence trickplay |
| `Seekbar.kt` | Pas de paramètre Bitmap/Image |
| Modules Koin (`di/AppModule.kt`) | Pas d'enregistrement de SeekProvider |

### Verdict final

> **Le trickplay est un code orphelin.** L'implémentation backend (SeekProvider + SubsetTransformation) est complète et fonctionnelle, mais elle n'est connectée à aucun composant UI. Pour activer le trickplay dans le player Compose, il faudrait :
>
> 1. Ajouter un paramètre `thumbnailPreview: @Composable (() -> Unit)?` à `Seekbar`
> 2. Créer un composable `TrickplayPreview` qui affiche un `Bitmap` au-dessus de la position du knob
> 3. Instancier `SeekProvider` dans le player (ou le refactorer en ViewModel avec StateFlow)
> 4. Connecter le callback `getThumbnail()` au composable pendant le scrubbing
> 5. S'assurer que la préférence `trickPlayEnabled` est lue et transmise

---

## Résumé

| # | Point | Résultat |
|---|-------|----------|
| 1 | Seekbar UI | ❌ Aucune logique trickplay |
| 2 | API Jellyfin | ✅ SDK 1.8.6 supporte trickplay |
| 3 | ViewModel/State | ❌ Pas de StateFlow, état callback-only dans SeekProvider orphelin |
| 4 | SubsetTransformation | ✅ Crop de sprite sheet fonctionnel, utilisé uniquement par SeekProvider |
| 5 | Config serveur | ⚠️ À vérifier manuellement sur JellyMox |
| 6 | Cause absence | **Code orphelin** — SeekProvider existe mais n'est instancié nulle part |
