# Audit 11 - UI States (Loading, Empty, Error, Transitions)

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Tous les composants créés et vérifiés :
> - SkeletonShimmer.kt, SkeletonPresets.kt existants
> - StateContainer.kt, EmptyState.kt, ErrorState.kt existants
> - 7 écrans browse V2 avec skeleton + empty state + transitions animées

## Composants reutilisables crees

### 1. Skeleton Shimmer (`ui/base/skeleton/`)

#### `SkeletonShimmer.kt`
- `rememberShimmerBrush()` - Brush anime de gauche a droite (1200ms, lineaire)
- `SkeletonBox` - Placeholder rectangulaire avec shimmer
- `SkeletonTextLine` - Ligne de texte skeleton
- `SkeletonTextBlock` - Bloc de texte multi-lignes (largeurs variees)

#### `SkeletonPresets.kt`
- `SkeletonCard(width, height)` - Carte media portrait (poster + titre)
- `SkeletonLandscapeCard(width, height)` - Carte paysage (episode, album)
- `SkeletonCardRow(cardCount)` - Ligne horizontale de cartes (home rows)
- `SkeletonLandscapeCardRow(cardCount)` - Ligne horizontale paysage
- `SkeletonCardGrid(columns, rows)` - Grille de cartes (library browse)
- `SkeletonGenreGrid(columns, rows)` - Grille de genres
- `SkeletonItemDetail` - Page detail item (poster + info + boutons)
- `SkeletonHomeRows(rowCount)` - Plusieurs rows home

### 2. State Container (`ui/base/state/`)

#### `StateContainer.kt`
- `DisplayState` enum: `LOADING`, `EMPTY`, `ERROR`, `CONTENT`
- `StateContainer` composable avec `AnimatedContent` pour transitions fluides
- Transitions:
  - Loading -> Content : fadeIn(300ms) + fadeOut(150ms) (crossfade)
  - Loading -> Error/Empty : fadeIn(300ms) + fadeOut(300ms)
  - Error -> Loading (retry) : fadeIn/Out(150ms) rapide
  - Defaut : crossfade 300ms

#### `EmptyState.kt`
- Icone optionnelle (48dp, couleur `textDisabled`)
- Titre (`titleLarge`, couleur `textHint`)
- Message optionnel (`bodyMedium`, couleur `textDisabled`)
- Action optionnelle (slot composable pour bouton CTA)
- Centre vertical, espacement aerien

#### `ErrorState.kt`
- Icone erreur (`ic_error`, couleur `error`)
- Message (`bodyLarge`, couleur `textSecondary`)
- Bouton "Reessayer" optionnel avec focus D-pad
  - Focus: primary bg + onPrimary text + scale 1.06x
  - Defaut: surfaceContainer bg + textPrimary text

### 3. Strings ajoutees

#### EN (`values/strings.xml`)
| Cle | Valeur |
|-----|--------|
| `lbl_retry` | Retry |
| `state_error_network` | Unable to connect. Check your network connection. |
| `state_error_timeout` | Connection timed out. The server is not responding. |
| `state_error_server` | A server error occurred. Please try again later. |
| `state_error_not_found` | This content is no longer available. |
| `state_error_auth` | Your session has expired. Please sign in again. |
| `state_error_generic` | Something went wrong. Please try again. |
| `state_error_loading_data` | Failed to load content |
| `state_empty_library` | Your library is empty |
| `state_empty_library_message` | Add media to your server to see it here |
| `state_empty_recordings` | No recordings |
| `state_empty_recordings_message` | Recorded programs will appear here |
| `state_empty_schedule` | No scheduled recordings |
| `state_empty_schedule_message` | Schedule recordings from the TV guide |
| `state_empty_music` | No music found |
| `state_empty_music_message` | Add music to your server to browse it here |
| `state_empty_live_tv` | No live TV channels |
| `state_empty_live_tv_message` | Configure live TV in your server settings |
| `state_empty_genres` | No genres found |
| `state_empty_search` | No results found |
| `state_empty_search_message` | Try a different search term |

#### FR (`values-fr/strings.xml`)
Toutes les strings ci-dessus traduites en francais.

---

## Ecrans et leurs etats traites

| Ecran | Loading | Empty | Error | Transition |
|-------|---------|-------|-------|------------|
| **LibraryBrowseFragment** | SkeletonCardGrid | EmptyState (library vide) | - | StateContainer (fade) |
| **GenresGridV2Fragment** | SkeletonGenreGrid | EmptyState (aucun genre) | - | StateContainer (fade) |
| **RecordingsBrowseFragment** | 2x SkeletonLandscapeCardRow | EmptyState (aucun enregistrement) | - | StateContainer (fade) |
| **MusicBrowseFragment** | 2x SkeletonCardRow | EmptyState (aucune musique) | - | StateContainer (fade) |
| **LiveTvBrowseFragment** | 2x SkeletonLandscapeCardRow | EmptyState (aucune chaine) | - | StateContainer (fade) |
| **ScheduleBrowseFragment** | 2x SkeletonLandscapeCardRow | EmptyState (aucun programme) | - | StateContainer (fade) |
| **SeriesRecordingsBrowseFragment** | 2x SkeletonLandscapeCardRow | EmptyState (aucun enregistrement) | - | StateContainer (fade) |

### Ecrans non modifies (deja geres ou hors scope Compose V2)

| Ecran | Raison |
|-------|--------|
| RequestsFragment (Jellyseerr) | Deja a un empty state XML complet + loading state |
| HomeFragment/HomeRowsFragment | Leanback rows (gestion dynamique des lignes vides) |
| ItemDetailsFragment | Loading < 300ms typiquement |
| ServerFragment/SelectServerFragment | Auth flow, etats deja geres via sealed classes |
| SearchFragment | Delegue a SearchFragmentDelegate |
| TV Guide (live_tv_guide.xml) | Legacy XML, hors scope |
| PlayerOverlay | Pas de chargement visible |

---

## Regles de transition entre etats

### Loading -> Content
```
AnimatedContent:
  fadeIn(tween(300ms)) togetherWith fadeOut(tween(150ms))
```
Le skeleton se dissout pendant que le contenu reel apparait.
Le contenu reel a un temps de fade-in plus long que le fade-out du skeleton
pour eviter un flash de fond vide.

### Loading -> Error
```
AnimatedContent:
  fadeIn(tween(300ms)) togetherWith fadeOut(tween(300ms))
```
Transition douce, pas de saut brutal.

### Loading -> Empty
```
AnimatedContent:
  fadeIn(tween(300ms)) togetherWith fadeOut(tween(300ms))
```
Meme pattern que l'erreur.

### Error -> Retry -> Loading
```
AnimatedContent:
  fadeIn(tween(150ms)) togetherWith fadeOut(tween(150ms))
```
Transition rapide pour donner un feedback immediat au retry.

### Seuil de 300ms
Les skeletons ne sont affiches que pour les chargements reseau (typiquement > 300ms).
Les composants a chargement instantane (< 300ms) ne montrent rien de special.

---

## Architecture des fichiers

```
app/src/main/java/org/jellyfin/androidtv/ui/base/
  skeleton/
    SkeletonShimmer.kt      # Brush shimmer + composables de base
    SkeletonPresets.kt       # Presets par type d'ecran
  state/
    StateContainer.kt        # Container avec transitions animees
    EmptyState.kt            # Etat vide standardise
    ErrorState.kt            # Etat erreur avec retry
```

---

## Impact

| Metrique | Avant | Apres |
|----------|-------|-------|
| Spinners generiques (CircularProgressIndicator) dans browse V2 | 7 ecrans | 0 |
| Skeleton screens | 0 | 7 ecrans |
| Ecrans sans empty state | 4 (Recordings, Music, LiveTV, SeriesRecordings) | 0 |
| Transitions animees loading->content | 0 | 7 ecrans |
| Composants reutilisables crees | 0 | 8 (skeleton) + 3 (state) |
