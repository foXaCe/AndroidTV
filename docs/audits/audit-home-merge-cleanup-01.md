# Audit — Options et menus liés au merge Continue/Next Up

**Date** : 2026-03-16
**Scope** : Toutes les références à `mergeContinueWatchingNextUp`, `NEXT_UP`, `RESUME` dans le contexte Home

---

## 1 — Préférence `mergeContinueWatchingNextUp`

### Définition

| Fichier | Ligne | Code | Rôle |
|---------|-------|------|------|
| `preference/UserPreferences.kt` | 259 | `var mergeContinueWatchingNextUp = booleanPreference("pref_merge_continue_watching_next_up", true)` | Définition de la préférence, default `true` |

### Lectures de la préférence

| Fichier | Ligne | Code | Rôle |
|---------|-------|------|------|
| `ui/home/compose/HomeViewModel.kt` | 293 | `val mergeContinueWatching = userPreferences[UserPreferences.mergeContinueWatchingNextUp]` | Variable locale dans `loadRows()` |
| `ui/home/compose/HomePrefetchService.kt` | 48 | `val mergeContinueWatching = userPreferences[...mergeContinueWatchingNextUp]` | Variable locale dans `prefetch()` |
| `ui/settings/screen/home/SettingsHomeScreen.kt` | 53 | `val isMerged = userPreferences[...mergeContinueWatchingNextUp]` | Conditionne l'affichage des sections |
| `ui/settings/screen/SettingsMainScreen.kt` | 217 | `var mergeContinueWatchingNextUp by rememberPreference(...)` | Toggle dans Settings principal |
| `ui/settings/screen/customization/SettingsCustomizationScreen.kt` | 204 | `var mergeContinueWatchingNextUp by rememberPreference(...)` | Toggle dupliqué dans Customization |

### Plugin Sync

| Fichier | Ligne | Code | Rôle |
|---------|-------|------|------|
| `data/service/pluginsync/PluginSyncConstants.kt` | 45 | `SyncablePreference(UserPreferences.mergeContinueWatchingNextUp, SyncType.BOOLEAN, "mergeContinueWatchingNextUp")` | Sync serveur |

---

## 2 — Toggles UI dans les Settings

### Toggle dans SettingsMainScreen

**Fichier** : `ui/settings/screen/SettingsMainScreen.kt`, lignes 217-224

```kotlin
item {
    var mergeContinueWatchingNextUp by rememberPreference(userPreferences, UserPreferences.mergeContinueWatchingNextUp)
    ListButton(
        headingContent = { Text(stringResource(R.string.lbl_merge_continue_watching_next_up)) },
        captionContent = { Text(stringResource(R.string.lbl_merge_continue_watching_next_up_description)) },
        trailingContent = { Checkbox(checked = mergeContinueWatchingNextUp) },
        onClick = { mergeContinueWatchingNextUp = !mergeContinueWatchingNextUp },
    )
}
```

**Action** : SUPPRIMER tout le bloc `item { ... }`.

### Toggle dans SettingsCustomizationScreen

**Fichier** : `ui/settings/screen/customization/SettingsCustomizationScreen.kt`, lignes 204-211

```kotlin
item {
    var mergeContinueWatchingNextUp by rememberPreference(userPreferences, UserPreferences.mergeContinueWatchingNextUp)
    ListButton(
        headingContent = { Text(stringResource(R.string.lbl_merge_continue_watching_next_up)) },
        captionContent = { Text(stringResource(R.string.lbl_merge_continue_watching_next_up_description)) },
        trailingContent = { Checkbox(checked = mergeContinueWatchingNextUp) },
        onClick = { mergeContinueWatchingNextUp = !mergeContinueWatchingNextUp },
    )
}
```

**Action** : SUPPRIMER tout le bloc `item { ... }`.

---

## 3 — Logique conditionnelle dans HomeViewModel

### Variable `mergeContinueWatching` et flag `mergedRowAdded`

| Ligne | Code | Rôle |
|-------|------|------|
| 293 | `val mergeContinueWatching = userPreferences[UserPreferences.mergeContinueWatchingNextUp]` | Lecture de la pref |
| 301 | `var mergedRowAdded = false` | Flag pour éviter double insertion |

### Branche RESUME (lignes 309-317)

```kotlin
HomeSectionType.RESUME -> {
    if (mergeContinueWatching && !mergedRowAdded) {
        mergedRowAdded = true
        loadMergedContinueWatching()
    } else if (!mergeContinueWatching) {
        loadContinueWatching()
    } else {
        emptyList()
    }
}
```

**Action** : Simplifier en :
```kotlin
HomeSectionType.RESUME -> {
    if (!mergedRowAdded) {
        mergedRowAdded = true
        loadMergedContinueWatching()
    } else {
        emptyList()
    }
}
```

### Branche NEXT_UP (lignes 319-327)

```kotlin
HomeSectionType.NEXT_UP -> {
    if (!mergeContinueWatching) {
        loadNextUp()
    } else if (!mergedRowAdded) {
        mergedRowAdded = true
        loadMergedContinueWatching()
    } else {
        emptyList()
    }
}
```

**Action** : Simplifier en `HomeSectionType.NEXT_UP -> emptyList()` ou supprimer entièrement si NEXT_UP est retiré de `HomeSectionConfig.defaults()`.

### Row pinning (lignes 371-375)

```kotlin
val isMergedSection = mergeContinueWatching &&
    sectionIndex < homesections.size &&
    homesections[sectionIndex] in setOf(HomeSectionType.RESUME, HomeSectionType.NEXT_UP) &&
    sectionRows.any { it.key == "continue_watching" || it.key == "continue_watching_fallback" }
```

**Action** : Simplifier en :
```kotlin
val isMergedSection = sectionIndex < homesections.size &&
    homesections[sectionIndex] == HomeSectionType.RESUME &&
    sectionRows.any { it.key == "continue_watching" || it.key == "continue_watching_fallback" }
```

### Fonctions `loadContinueWatching()` et `loadNextUp()` (lignes 445-479)

**Action** : SUPPRIMER les deux fonctions. Elles ne sont plus appelées si le merge est toujours actif. Seule `loadMergedContinueWatching()` reste.

---

## 4 — Logique conditionnelle dans HomePrefetchService

### Branche conditionnelle (lignes 48, 63-80)

```kotlin
val mergeContinueWatching = userPreferences[...mergeContinueWatchingNextUp]
// ...
if (mergeContinueWatching) {
    // merge + dedup logic
    listOf(TvRow(..., key = "continue_watching"))
} else {
    listOfNotNull(resume, nextUp)
}
```

**Action** : Supprimer la variable `mergeContinueWatching`, supprimer le `else`, garder uniquement le path merge.

### Fonction `loadNextUp()` dans HomePrefetchService (lignes 120-140)

**Action** : CONSERVER — elle est appelée dans le path merge pour charger les items Next Up avant déduplication. Ce n'est pas une section séparée.

### Fonction `loadContinueWatching()` dans HomePrefetchService (lignes 104-118)

**Action** : CONSERVER — même raison.

### Constructeur `HomePrefetchService`

**Action** : Le paramètre `userPreferences` peut être SUPPRIMÉ du constructeur si on supprime la lecture de la pref. Mettre à jour la DI dans `AppModule.kt` ligne 301.

---

## 5 — SettingsHomeScreen conditionnels

| Ligne | Code | Action |
|-------|------|--------|
| 53 | `val isMerged = userPreferences[...mergeContinueWatchingNextUp]` | SUPPRIMER la variable |
| 109 | `.filter { !isMerged \|\| it.type != HomeSectionType.NEXT_UP }` | Remplacer par `.filter { it.type != HomeSectionType.NEXT_UP }` |
| 115 | `val isPinned = isMerged && section.type == HomeSectionType.RESUME` | Remplacer par `val isPinned = section.type == HomeSectionType.RESUME` |
| 118-121 | `section = if (isMerged && section.type == HomeSectionType.RESUME) { section.copy(enabled = true) } else { section }` | Remplacer par `section = if (section.type == HomeSectionType.RESUME) { section.copy(enabled = true) } else { section }` |
| 123-126 | `labelOverride = if (isMerged && section.type == HomeSectionType.RESUME) { stringResource(R.string.home_section_continue_and_next) } else { null }` | Remplacer par `labelOverride = if (section.type == HomeSectionType.RESUME) { stringResource(R.string.home_section_continue_and_next) } else { null }` |
| 47 | `userPreferences: ...UserPreferences = koinInject()` | SUPPRIMER le paramètre |

---

## 6 — HomeSectionType enum et HomeSectionConfig defaults

### HomeSectionType.kt

| Ligne | Code | Action |
|-------|------|--------|
| 20 | `RESUME("resume", R.string.home_section_resume)` | CONSERVER — devient la ligne fusionnée |
| 24 | `NEXT_UP("nextup", R.string.home_section_next_up)` | CONSERVER mais marquer `@Deprecated` — nécessaire pour la désérialisation des configs existantes |

**Risque suppression** : Les utilisateurs ont `NEXT_UP` sérialisé dans `home_sections_config` (SharedPreferences JSON). Si l'enum est supprimé, la désérialisation échoue. Le `HomeSectionTypeSerializer` retourne `NONE` pour les types inconnus, donc c'est sûr, mais mieux vaut garder l'enum deprecated.

### HomeSectionConfig.kt defaults()

| Ligne | Code | Action |
|-------|------|--------|
| 54 | `HomeSectionConfig(HomeSectionType.RESUME, enabled = true, order = 0)` | CONSERVER |
| 55 | `HomeSectionConfig(HomeSectionType.NEXT_UP, enabled = true, order = 1)` | SUPPRIMER — plus utile dans les defaults |

---

## 7 — Strings à supprimer

### Strings du toggle merge (orphelines si toggle supprimé)

| Clé | Fichiers | Action |
|-----|----------|--------|
| `lbl_merge_continue_watching_next_up` | `values/strings.xml` L306, `values-fr/strings.xml` L299 | SUPPRIMER dans EN et FR |
| `lbl_merge_continue_watching_next_up_description` | `values/strings.xml` L307, `values-fr/strings.xml` L300 | SUPPRIMER dans EN et FR |

### Strings qui deviennent orphelines si `loadNextUp()` est supprimé

| Clé | Fichiers | Action |
|-----|----------|--------|
| `home_section_next_up` | 44+ fichiers de traduction | CONSERVER — référencé par `HomeSectionType.NEXT_UP.nameRes` qui reste pour compatibilité désérialisation |

### Strings qui restent utilisées

| Clé | Utilisée par | Action |
|-----|-------------|--------|
| `home_section_resume` | `HomeViewModel.loadMergedContinueWatching()` ligne 542, `HomePrefetchService` | CONSERVER — label de la ligne fusionnée |
| `home_section_continue_and_next` | `SettingsHomeScreen.kt` ligne 124 | CONSERVER — label dans Settings |
| `home_section_latest_media` | `HomeViewModel` ligne 562 (fallback) | CONSERVER — label du fallback |

---

## 8 — Plugin Sync

| Fichier | Ligne | Code | Action |
|---------|-------|------|--------|
| `data/service/pluginsync/PluginSyncConstants.kt` | 45 | `SyncablePreference(UserPreferences.mergeContinueWatchingNextUp, ...)` | SUPPRIMER la ligne |

Note : Incrémenter `SNAPSHOT_VERSION` (ligne 29) pour forcer un re-sync.

---

## 9 — Références à NE PAS toucher

### Playback Next Up (feature séparée)

Les fichiers suivants concernent le "Next Up" de lecture (prompt entre épisodes), PAS la section Home :

- `preference/constant/NextUpBehavior.kt` — enum DISABLED/MINIMAL/EXTENDED
- `preference/UserPreferences.kt:169` — `nextUpBehavior`
- `preference/UserPreferences.kt:175` — `nextUpTimeout`
- `ui/playback/common/PlaybackPromptViewModel.kt:90`
- `ui/playback/PlaybackController.kt:1509`
- `ui/settings/screen/playback/nextup/SettingsPlaybackNextUpBehaviorScreen.kt`
- `ui/settings/screen/playback/nextup/SettingsPlaybackNextUpScreen.kt:60`

**Action** : NE RIEN MODIFIER.

### ItemDetailsViewModel.loadNextUp()

- `ui/itemdetail/v2/ItemDetailsViewModel.kt:149,229` — charge le prochain épisode dans la page détail d'une série

**Action** : NE RIEN MODIFIER — contexte différent.

---

## 10 — HomeScreen.kt

| Ligne | Code | Action |
|-------|------|--------|
| 96 | `uiState.rows.firstOrNull { it.key == "resume" \|\| it.key == "continue_watching" }` | Simplifier en `it.key == "continue_watching" \|\| it.key == "continue_watching_fallback"` |

---

## Résumé des actions

### Fichiers à modifier (10 fichiers)

| # | Fichier | Action | Complexité |
|---|---------|--------|------------|
| 1 | `UserPreferences.kt` | Supprimer `mergeContinueWatchingNextUp` | Basse |
| 2 | `HomeViewModel.kt` | Supprimer conditionnels merge, supprimer `loadContinueWatching()` et `loadNextUp()` | Haute |
| 3 | `HomePrefetchService.kt` | Supprimer conditionnel merge, supprimer param `userPreferences` | Moyenne |
| 4 | `SettingsHomeScreen.kt` | Supprimer `isMerged`, simplifier conditionnels, supprimer param `userPreferences` | Moyenne |
| 5 | `SettingsMainScreen.kt` | Supprimer bloc toggle merge (L217-224) | Basse |
| 6 | `SettingsCustomizationScreen.kt` | Supprimer bloc toggle merge (L204-211) | Basse |
| 7 | `HomeSectionConfig.kt` | Supprimer NEXT_UP des defaults | Basse |
| 8 | `PluginSyncConstants.kt` | Supprimer ligne sync merge | Basse |
| 9 | `AppModule.kt` | Retirer `get()` du constructeur HomePrefetchService | Basse |
| 10 | `HomeScreen.kt` | Simplifier check row key (optionnel) | Basse |

### Strings à supprimer (2 clés, 2 fichiers)

| Clé | Fichiers |
|-----|----------|
| `lbl_merge_continue_watching_next_up` | `values/strings.xml`, `values-fr/strings.xml` |
| `lbl_merge_continue_watching_next_up_description` | `values/strings.xml`, `values-fr/strings.xml` |

### Enum à deprecate (1 valeur)

| Enum | Action |
|------|--------|
| `HomeSectionType.NEXT_UP` | Ajouter `@Deprecated("Merged with RESUME")` — ne pas supprimer pour compatibilité JSON |

### ~150 lignes de code supprimées/simplifiées au total
