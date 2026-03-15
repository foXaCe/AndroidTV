# Phase 9d — Dernier héritage Leanback + nettoyage inline refs

## Objectif

Supprimer le dernier héritage de Fragment Leanback (`PlaybackSupportFragment`) dans le player overlay et nettoyer les 5 références inline Leanback dispersées dans 4 fichiers.

## ÉTAPE 1 — LeanbackOverlayFragment.java

### Avant
- Héritait de `PlaybackSupportFragment` (Leanback)
- 1 import Leanback : `androidx.leanback.app.PlaybackSupportFragment`
- Utilisait 6 méthodes Leanback : `setBackgroundType`, `setPlaybackRow`, `setPlaybackRowPresenter`, `setOnPlaybackItemViewClickedListener`, `showControlsOverlay`, `hideControlsOverlay`

### Migration
- **Héritage** : `extends PlaybackSupportFragment` → `extends Fragment`
- **Import** : supprimé — zéro import Leanback dans le fichier
- **Layout** : `onCreateView()` crée un `FrameLayout` custom avec `dispatchKeyEvent` override (remplace `setOnKeyInterceptListener`)
- **Transport controls** : déléguées à `TransportControlManager.createTransportView()` qui encapsule toute la création/bind du ViewHolder Leanback
- **Action clicks** : `RowPresenter.ViewHolder.setOnItemViewClickedListener()` sur le ViewHolder (remplace le routing interne de `RowsSupportFragment`)
- **Show/hide overlay** : animation alpha 200ms + `View.VISIBLE`/`View.GONE` (remplace le mécanisme interne de `PlaybackSupportFragment`)
- **isControlsOverlayVisible()** : simple boolean (remplace l'état interne de `PlaybackSupportFragment`)

### TransportControlManager.kt — Changements
- Ajout `fun interface ActionClickListener` (SAM-compatible Java)
- Ajout `createTransportView(parent, onActionClick)` : crée le ViewHolder, wire les clicks, bind, retourne la View
- Ajout `unbindTransportView()` : unbind propre dans onDestroyView
- Ajout import `Presenter` (8 imports Leanback total, contre 7 avant)

## ÉTAPE 2 — Inline refs (4 fichiers)

### LeftSidebarNavigation.kt
- `instanceof VerticalGridView` dans recherche de focus pour SearchFragment
- Supprimé : le SearchFragment est maintenant Compose, la VerticalGridView n'existe plus
- Remplacé par simple `focusSearch(FOCUS_RIGHT)`

### FriendlyDateButton.java
- `androidx.leanback.R.color.lb_default_brand_color` → `R.color.ds_primary`

### ProgramGridCell.java
- `androidx.leanback.R.color.lb_default_brand_color` → `R.color.ds_primary`

### CustomPlaybackOverlayFragment.java
- `instanceof PlaybackTransportRowView` (2 occurrences)
- Remplacé par `overlayView.findFocus() != null` (vérifie si le focus est dans le fragment overlay)

## ÉTAPE 3 — Dépendance Gradle partielle

- `leanback-preference` : **supprimée** (inutilisée)
- `leanback-core` : **conservée** (9 fichiers source + 3 XML l'utilisent encore)

## Compteur Leanback

| Métrique | Avant Phase 9d | Après Phase 9d | Delta |
|----------|----------------|----------------|-------|
| Imports Leanback (fichiers source) | 26 | 26 | 0 (−1 +1) |
| Fichiers avec imports Leanback | 10 | 9 | **−1** |
| Inline refs Leanback | 5 (4 fichiers) | 0 | **−5** |
| Fichiers avec inline refs | 4 | 0 | **−4** |
| Héritages Fragment Leanback | 1 | **0** | **−1** |
| Dépendances Gradle | 2 | 1 | **−1** |
| XML widgets Leanback | 3 fichiers | 3 fichiers | 0 |

### Fichiers Leanback restants (9 fichiers, 26 imports)

#### Player overlay (10 imports, 2 fichiers)
- `TransportControlManager.kt` — 8 imports (widgets uniquement, aucun héritage)
- `CustomAction.kt` — 1 import (`PlaybackControlsRow.MultiAction`)
- + 1 inline style ref dans TransportControlManager (`Widget_Leanback_PlaybackControlsTimeStyle`)

#### Autres (16 imports, 7 fichiers)
- `ItemRowAdapter.java` — 6 imports
- `TvManager.java` — 4 imports
- `MutableObjectAdapter.kt` — 3 imports
- `AggregatedItemRowAdapter.kt` — 1 import
- `TitleView.kt` — 1 import
- `TextItemPresenter.kt` — 1 import
- `CardPresenter.kt` — 1 import

#### XML (3 fichiers)
- `horizontal_grid.xml` — `HorizontalGridView`
- `fragment_server.xml` — `HorizontalGridView`
- `view_lb_title.xml` — `SearchOrbView`

## Score final

**Score global : 97/100** (inchangé — cette phase est du nettoyage technique)

Points clés :
- **Zéro héritage Leanback** dans toute la codebase
- **Zéro ref inline Leanback** en dehors des fichiers avec imports
- Dépendance `leanback-preference` supprimée
- Leanback réduit à des **widgets data** (Presenter, ObjectAdapter, Row) et **transport controls** (PlaybackControlsRow, PlaybackTransportRowPresenter)

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
BUILD SUCCESSFUL — assembleGithubRelease
```

Test lecture/pause/seek/overlay show-hide à confirmer sur Ugoos AM9 Pro.
