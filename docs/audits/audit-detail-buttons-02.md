# Audit — Artefact bouton Vu/Non-vu + ordre des boutons secondaires

**Date** : 2026-03-15
**Scope** : `VegafoXButton.kt`, `MovieDetailsContent.kt`, `DetailActions.kt`, `SeasonDetailsContent.kt`, `ItemDetailsViewModel.kt`

---

## Point 1 — Analyse de l'artefact visuel lors du changement d'icône Vu/Non-vu

### 1.1 Comment l'icône est rendue dans VegafoXButton

**Fichier** : `VegafoXButton.kt`, lignes 316-318

```kotlin
if (icon != null && !iconEnd) {
    GlassButtonIcon(icon = icon, tint = contentColor, focused = isFocused)
}
```

**Constats** :
- Pas de `AnimatedContent`, `Crossfade`, ni `AnimatedVisibility` autour de l'icône
- L'icône est conditionnelle uniquement sur `icon != null` (toujours vrai dans notre cas) et `iconEnd`
- Le changement d'icône (`Visibility` → `VisibilityOff` ou inverse) se fait par **recomposition directe** : Compose remplace l'`ImageVector` dans le même `Icon` composable
- `GlassButtonIcon` (lignes 346-362) encapsule un simple `Icon()` avec un `graphicsLayer` pour le scale
- **Aucune animation de transition sur l'icône elle-même** — le swap est instantané

### 1.2 Comment le switch d'icône est codé dans les appelants

Les trois fichiers utilisent un pattern identique :

```kotlin
icon = if (item.userData?.played == true) VegafoXIcons.VisibilityOff else VegafoXIcons.Visibility,
```

**Constats** :
- C'est un `if/else` direct, pas un `when` avec `null` non couvert
- Si `item.userData?.played` est `null`, on tombe dans le `else` → `VegafoXIcons.Visibility` (oeil ouvert)
- Si `item.userData` est `null`, `item.userData?.played` vaut `null`, donc `else` → `Visibility`
- **Aucun état où l'icône serait absente** — il y a toujours un `ImageVector` valide
- Les deux icônes sont des `val` initialisées dans l'`object VegafoXIcons` — elles ne sont jamais null

### 1.3 Lifecycle du toggleWatched() dans le ViewModel

**Fichier** : `ItemDetailsViewModel.kt`, lignes 449-489

```
T0: onClick déclenché
T1: val newPlayed = !(item.userData?.played ?: false)
T2: _uiState.value = _uiState.value.copy(
        item = item.copy(
            userData = item.userData?.copy(played = newPlayed, playedPercentage = ...)
        )
    )                                         ← OPTIMISTIC UPDATE IMMÉDIAT
T3: Compose recompose (StateFlow → collectAsState)
T4: viewModelScope.launch { API call }        ← ASYNCHRONE
T5: Si erreur → revert userData.played        ← REVERT
```

**Constats** :
- **Update optimiste** : `played` est flippé IMMÉDIATEMENT dans le UiState avant l'appel API
- **Pas d'état loading intermédiaire** : aucun flag `isTogglingWatched` ou `userData = null` temporaire
- `userData` n'est **jamais nullifié** — il est copié avec `?.copy(played = newPlayed)`
- `played` ne passe **jamais** par `null` — il passe de `true` à `false` ou inversement, directement
- Si l'API échoue, revert immédiat vers l'état précédent (même pattern `copy`)

### 1.4 Existe-t-il un risque de frame vide pendant la transition ?

| Question | Réponse |
|---|---|
| AnimatedContent/Crossfade autour de l'icône ? | **Non** — swap direct |
| AnimatedVisibility sur le bouton ? | **Non** |
| `userData?.played` peut-il être `null` pendant la transition ? | **Non** — copie atomique via `copy()` |
| `userData` lui-même peut-il devenir `null` ? | **Non** — jamais nullifié dans `toggleWatched()` |
| L'icône peut-elle être `null` ? | **Non** — `VegafoXIcons.Visibility/VisibilityOff` sont des `val` non-nullables |
| Frame intermédiaire entre ancien et nouvel état ? | **Non** — le StateFlow émet un seul nouvel état, Compose recompose une seule fois |
| Flicker si l'API échoue ? | **Oui, léger** — le bouton flip une deuxième fois (revert), mais pas de frame vide |

### 1.5 Conclusion Point 1

**Aucun artefact visuel ne devrait se produire** avec l'implémentation actuelle :

- Le swap d'icône est un remplacement direct d'`ImageVector` sans animation — Compose re-render le `Icon` en une seule frame
- Le `if/else` couvre tous les cas (`true` → VisibilityOff, tout le reste → Visibility)
- L'update optimiste garantit que `userData.played` passe directement de `true` à `false` sans état intermédiaire
- Le seul scénario visible par l'utilisateur est un **double-flip** en cas d'erreur API (flicker de revert), qui est un comportement correct et attendu

**Si un artefact était observé en pratique**, les causes possibles seraient :
1. **Latence de recomposition** sur le device TV (CPU lent) — mais une seule frame, imperceptible
2. **Double-click** malgré le guard `DOUBLE_CLICK_GUARD_MS = 400ms` dans VegafoXButton
3. Un **autre composant parent** qui recompose et re-crée le bouton entièrement (perte de state)

---

## Point 2 — Ordre exact des boutons secondaires dans chaque composable

### 2.1 MovieDetailsContent.kt (lignes 287-400)

| # | Bouton | Icône | Condition |
|---|---|---|---|
| 1 | Version | `Guide` | `hasMultipleVersions` |
| 2 | Audio | `Audiotrack` | `audioStreams.size > 1` |
| 3 | Subtitles | `Subtitles` | `subtitleStreams.isNotEmpty()` |
| 4 | Trailers | `Trailer` | `actionCallbacks.hasPlayableTrailers` |
| 5 | Favorite | `Favorite` | `item.userData != null` |
| 6 | Watched | `Visibility/VisibilityOff` | `userData != null && type != PERSON` |
| 7 | Add to Playlist | `Add` | `userData != null && type != PERSON` |
| 8 | Go to Series | `Tv` | `type == EPISODE && seriesId != null && onGoToSeries != null` |
| 9 | Delete | `Delete` | `item.canDelete == true` |

### 2.2 DetailActionButtonsRow (DetailActions.kt, lignes 155-268)

| # | Bouton | Icône | Condition |
|---|---|---|---|
| 1 | Version | `Guide` | `hasMultipleVersions` |
| 2 | Audio | `Audiotrack` | `audioStreams.size > 1` |
| 3 | Subtitles | `Subtitles` | `subtitleStreams.isNotEmpty()` |
| 4 | Trailers | `Trailer` | `callbacks.hasPlayableTrailers` |
| 5 | Favorite | `Favorite` | `item.userData != null` |
| 6 | Watched | `Visibility/VisibilityOff` | `userData != null && type != PERSON && type != MUSIC_ARTIST` |
| 7 | Add to Playlist | `Add` | `userData != null && type != PERSON` |
| 8 | Go to Series | `Tv` | `type == EPISODE && seriesId != null && onGoToSeries != null` |
| 9 | Delete | `Delete` | `item.canDelete == true` |

### 2.3 SeasonDetailsContent.kt (lignes 206-237)

| # | Bouton | Icône | Condition |
|---|---|---|---|
| 1 | Watched | `Visibility/VisibilityOff` | Toujours |
| 2 | Favorite | `Favorite` | Toujours |

### 2.4 Comparaison

| Position | MovieDetailsContent | DetailActionButtonsRow | SeasonDetailsContent |
|---|---|---|---|
| 1 | Version | Version | **Watched** |
| 2 | Audio | Audio | **Favorite** |
| 3 | Subtitles | Subtitles | — |
| 4 | Trailers | Trailers | — |
| 5 | Favorite | Favorite | — |
| 6 | Watched | Watched | — |
| 7 | Add to Playlist | Add to Playlist | — |
| 8 | Go to Series | Go to Series | — |
| 9 | Delete | Delete | — |

**Movie et DetailActionButtonsRow sont désormais identiques** (après le fix précédent).

**SeasonDetailsContent diffère** :
- Watched est en position 1 au lieu de 6
- Favorite est en position 2 au lieu de 5
- Pas de Version/Audio/Subtitles/Trailers/Playlist/GoToSeries/Delete

### 2.5 Ordre canonique proposé

L'ordre canonique qui devrait être appliqué **partout** :

| # | Bouton | Justification |
|---|---|---|
| 1 | Version | Choix technique à faire en premier (affecte tout le reste) |
| 2 | Audio | Configuration de lecture |
| 3 | Subtitles | Configuration de lecture |
| 4 | Trailers | Contenu additionnel lié |
| 5 | Favorite | Action de curation (fréquente) |
| 6 | Watched | Action de curation (fréquente) |
| 7 | Add to Playlist | Organisation |
| 8 | Go to Series | Navigation contextuelle |
| 9 | Delete | Action destructive en dernier |

Pour **SeasonDetailsContent**, les boutons non pertinents (Version, Audio, Subtitles, Trailers, Playlist, Go to Series, Delete) ne seront pas ajoutés car la saison n'est pas un média jouable directement. **Mais l'ordre relatif Favorite → Watched doit être inversé** pour correspondre à l'ordre canonique (Favorite en 5, Watched en 6 → Favorite d'abord).

**Correction nécessaire dans SeasonDetailsContent** :
- Intervertir : Favorite (position 1) → Watched (position 2) au lieu de Watched (1) → Favorite (2)

---

## Résumé des actions

| # | Action | Priorité |
|---|---|---|
| 1 | **Pas d'artefact visuel** — l'implémentation actuelle est correcte, pas de fix nécessaire | Info |
| 2 | **SeasonDetailsContent** : inverser Watched ↔ Favorite pour Favorite → Watched | Faible |
| 3 | **Movie et DetailActionButtonsRow** : déjà synchronisés, aucun changement | OK |
