# Audit — Première ligne Home : Continue Watching + Next Up

**Date** : 2026-03-16
**Scope** : `HomeScreen.kt`, `HomeViewModel.kt`, `HomeSectionConfig.kt`, `UserPreferences.kt`, `SettingsHomeScreen.kt`

---

## Point 1 — Structure actuelle des sections Home

### Définition des sections

Les sections sont définies par l'enum `HomeSectionType` dans `constant/HomeSectionType.kt` (13 valeurs) :

| Type | `serializedName` | Activé par défaut | Ordre par défaut |
|------|-------------------|--------------------|-----------------|
| `RESUME` | `"resume"` | oui | 0 |
| `NEXT_UP` | `"nextup"` | oui | 1 |
| `LIVE_TV` | `"livetv"` | oui | 2 |
| `LATEST_MEDIA` | `"latestmedia"` | oui | 3 |
| `RECENTLY_RELEASED` | `"recentlyreleased"` | non | 4 |
| `LIBRARY_TILES_SMALL` | `"smalllibrarytiles"` | non | 5 |
| `LIBRARY_BUTTONS` | `"librarybuttons"` | non | 6 |
| `RESUME_AUDIO` | `"resumeaudio"` | non | 7 |
| `RESUME_BOOK` | `"resumebook"` | non | 8 |
| `ACTIVE_RECORDINGS` | `"activerecordings"` | non | 9 |
| `PLAYLISTS` | `"playlists"` | non | 10 |
| `MEDIA_BAR` | `"mediabar"` | séparé | contrôle propre |
| `NONE` | `"none"` | — | placeholder |

### Configuration par section

Chaque section est un `HomeSectionConfig` (`preference/HomeSectionConfig.kt`) :

```kotlin
@Serializable
data class HomeSectionConfig(
    val type: HomeSectionType,
    val enabled: Boolean = true,
    val order: Int = 0,
)
```

Stockage : JSON sérialisé dans `UserSettingPreferences.homeSectionsJson` (SharedPreferences).

### CONTINUE_WATCHING et NEXT_UP : séparées ou fusionnées ?

**Par défaut, ce sont deux sections séparées** (RESUME order=0, NEXT_UP order=1).

L'option `mergeContinueWatchingNextUp` existe dans `UserPreferences.kt:259` :

```kotlin
var mergeContinueWatchingNextUp = booleanPreference("pref_merge_continue_watching_next_up", false)
```

**Valeur par défaut : `false`** — les deux lignes restent séparées.

### Comportement quand merge activé

Dans `HomeViewModel.loadRows()` (lignes 292-327) :

- **RESUME** : si merge activé → appelle `loadMergedContinueWatching()` (une seule fois via flag `mergedRowAdded`)
- **NEXT_UP** : si merge activé → retourne `emptyList()` (la section disparaît)
- Le flag `mergedRowAdded` garantit qu'une seule ligne fusionnée est créée, peu importe lequel (RESUME ou NEXT_UP) apparaît en premier dans l'ordre configuré

---

## Point 2 — Sources de données

### Continue Watching (Resume Items)

**Endpoint** : `api.itemsApi.getResumeItems(query)` → Jellyfin API `Items/Resume`

**Paramètres** (`HomeViewModel.kt:436-451`) :
```kotlin
GetResumeItemsRequest(
    limit = 50,                                  // ROW_MAX_ITEMS
    fields = ItemRepository.itemFields,          // voir ci-dessous
    imageTypeLimit = 1,
    enableTotalRecordCount = false,
    mediaTypes = listOf(MediaType.VIDEO),         // VIDEO uniquement
    excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
)
```

**Row key** : `"resume"`

### Next Up

**Endpoint** : `api.tvShowsApi.getNextUp(query)` → Jellyfin API `Shows/NextUp`

**Paramètres** (`HomeViewModel.kt:454-469`) :
```kotlin
GetNextUpRequest(
    imageTypeLimit = 1,
    limit = 50,                                  // ROW_MAX_ITEMS
    enableResumable = false,                     // Exclut les épisodes déjà commencés
    fields = ItemRepository.itemFields,
)
```

**Row key** : `"nextup"`

### Champs demandés (ItemRepository.itemFields)

Définis dans `data/repository/ItemRepository.kt` :

`CAN_DELETE`, `CHANNEL_INFO`, `CHAPTERS`, `CHILD_COUNT`, `CUMULATIVE_RUN_TIME_TICKS`, `DATE_CREATED`, `DISPLAY_PREFERENCES_ID`, `GENRES`, `ITEM_COUNTS`, `MEDIA_SOURCE_COUNT`, `MEDIA_SOURCES`, `MEDIA_STREAMS`, `OVERVIEW`, `PATH`, `PRIMARY_IMAGE_ASPECT_RATIO`, `PROVIDER_IDS`, `REMOTE_TRAILERS`, `TAGLINES`, `TRICKPLAY`

### Comment les doublons peuvent apparaître

Un épisode peut être à la fois :
- **Dans Resume** : l'utilisateur a commencé à le regarder (position > 0, non terminé)
- **Dans Next Up** : c'est le prochain épisode d'une série dont l'épisode précédent est marqué comme vu

**Cas typique** : l'utilisateur commence l'épisode S02E05 → il apparaît dans Resume (en cours). Si S02E04 est marqué vu, S02E05 apparaît aussi dans Next Up. Le même `itemId` est donc dans les deux listes.

Note : `enableResumable = false` dans la requête Next Up est censé exclure les épisodes déjà commencés, mais selon les versions de Jellyfin Server ce flag n'est pas toujours respecté.

---

## Point 3 — Logique de déduplication

### Déduplication existante : uniquement en mode fusionné

La déduplication existe **seulement** dans `loadMergedContinueWatching()` (`HomeViewModel.kt:472-515`) :

```kotlin
val resumeItems = resumeDeferred.await()
val nextUpItems = nextUpDeferred.await()

val resumeIds = resumeItems.map { it.id }.toSet()
val merged = enrichEpisodesWithSeriesGenres(
    resumeItems + nextUpItems.filter { it.id !in resumeIds },
)
```

**Stratégie** :
- Clé de déduplication : **`itemId`** (UUID de l'item via `it.id`)
- Priorité : Resume items en premier, puis Next Up items dont l'`id` n'est pas déjà dans Resume
- Row key fusionnée : `"continue_watching"`

### Limitation : pas de dédup par `seriesId`

La déduplication utilise uniquement `itemId`. Si un épisode différent de la même série apparaît dans les deux listes (ex: S02E05 en Resume et S02E06 en Next Up), les deux apparaissent. Ce n'est pas un bug — ce sont bien deux épisodes distincts.

### En mode séparé (par défaut) : AUCUNE déduplication

Quand `mergeContinueWatchingNextUp = false`, les deux sections sont chargées indépendamment par `loadContinueWatching()` et `loadNextUp()`. **Aucune logique de déduplication n'est appliquée**. Le même épisode peut apparaître dans les deux lignes.

---

## Point 4 — Gestion du cas vide

### Comportement actuel

Chaque loader de section retourne `emptyList()` si la requête API ne retourne aucun item :

```kotlin
// loadContinueWatching() ligne 450
if (items.isEmpty()) return emptyList()

// loadNextUp() ligne 468
if (items.isEmpty()) return emptyList()

// loadMergedContinueWatching() ligne 513
if (merged.isEmpty()) return@coroutineScope emptyList()
```

Dans `loadRows()` (ligne 370), seules les sections non-vides sont ajoutées :

```kotlin
for (deferred in deferredRows) {
    val sectionRows = deferred.await()
    if (sectionRows.isNotEmpty()) {
        progressiveRows.addAll(sectionRows)
    }
}
```

**Résultat : une section vide disparaît silencieusement de la Home.** Aucun placeholder, aucun fallback.

### Quand TOUTES les sections sont vides

`HomeScreen.kt` (lignes 209-229) utilise un `StateContainer` :

```kotlin
val displayState = when {
    uiState.isLoading -> DisplayState.LOADING
    uiState.error != null -> DisplayState.ERROR
    uiState.rows.isEmpty() -> DisplayState.EMPTY
    else -> DisplayState.CONTENT
}
```

État EMPTY → affiche un message centré "Empty" via `EmptyState(title = stringResource(R.string.lbl_empty))`.

### Fallback vers contenu récent : INEXISTANT

Il n'existe **aucun mécanisme de fallback** pour afficher du contenu alternatif (récemment ajouté, recommandations, tendances) quand Continue Watching et Next Up sont tous les deux vides. La première ligne de la Home sera simplement la prochaine section activée qui a du contenu (typiquement LIVE_TV ou LATEST_MEDIA).

---

## Point 5 — Position dans la Home

### Ordre configurable par l'utilisateur

L'ordre des sections est **entièrement configurable** via Settings > Home (`SettingsHomeScreen.kt:45-176`).

L'UI propose :
- Flèches haut/bas pour réordonner les sections
- Toggle pour activer/désactiver chaque section
- Bouton "Reset" pour revenir aux defaults

### Mécanisme d'ordre

```kotlin
// UserSettingPreferences.kt:182-187
val activeHomesections: List<HomeSectionType>
    get() = homeSectionsConfig
        .filter { it.enabled }
        .sortedBy { it.order }
        .map { it.type }
```

Le champ `order` (entier) détermine la position. Le swap d'ordre entre deux sections échange simplement leurs valeurs `order`.

### RESUME en première position : NON GARANTI

Par défaut, RESUME est à `order = 0` et NEXT_UP à `order = 1`. Mais l'utilisateur peut :
- Déplacer RESUME plus bas
- Mettre LATEST_MEDIA en première position
- Désactiver RESUME complètement

**Aucune section n'est marquée comme "non-déplaçable" ou "fixée en première position".**

### MEDIA_BAR : cas particulier

La MEDIA_BAR (hero backdrop + trailer) est contrôlée par un toggle séparé (`mediaBarEnabled`) et n'apparaît pas dans la liste de configuration des sections. Elle est toujours en position 0 quand activée, avant toutes les autres sections.

---

## Résumé des constats

| # | Aspect | État actuel | Risque/Amélioration |
|---|--------|-------------|---------------------|
| 1 | Sections séparées par défaut | RESUME et NEXT_UP sont 2 lignes distinctes | Doublons possibles entre les deux lignes |
| 2 | Option merge | Existe, désactivée par défaut | Pourrait être activée par défaut |
| 3 | Déduplication | Uniquement en mode fusionné, par `itemId` | Absente en mode séparé |
| 4 | Cas vide | Section disparaît silencieusement | Pas de fallback, pas de placeholder |
| 5 | Fallback récent | Inexistant | Si les deux sont vides, la première ligne visible dépend de la config |
| 6 | Position fixe | Non garantie, configurable par l'utilisateur | RESUME peut être déplacé n'importe où |
| 7 | Prefetch | Continue Watching et Next Up sont prefetch au démarrage | Bon pour la perf cold start |
| 8 | Cache local | `home_rows_cache.json`, 8 items/row, 5 min freshness | Assure un affichage instantané |

---

## Fichiers clés

| Fichier | Rôle |
|---------|------|
| `constant/HomeSectionType.kt` | Enum des 13 types de sections |
| `preference/HomeSectionConfig.kt` | Config sérialisable (type, enabled, order) + defaults |
| `preference/UserSettingPreferences.kt:163-187` | Getter/setter JSON + `activeHomesections` |
| `preference/UserPreferences.kt:259` | Préférence `mergeContinueWatchingNextUp` |
| `ui/home/compose/HomeViewModel.kt:292-515` | Chargement des sections, merge, dédup |
| `ui/home/compose/HomeScreen.kt:209-291` | Rendu UI, StateContainer, TvRowList |
| `ui/home/compose/HomePrefetchService.kt` | Prefetch au démarrage de session |
| `ui/home/compose/HomeRowsCache.kt` | Cache disque des lignes Home |
| `ui/settings/screen/home/SettingsHomeScreen.kt` | UI réglages sections Home |
