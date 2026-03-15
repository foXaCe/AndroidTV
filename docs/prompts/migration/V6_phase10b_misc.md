# Phase 10b — Nettoyage fichiers misc Leanback

## Objectif

Éliminer les imports Leanback restants dans les fichiers "misc" (presenters, title view) identifiés en phase 10a.

## Analyse et résultats par fichier

### TextItemPresenter.kt — SUPPRIMÉ

- **Import Leanback** : `Presenter` (1 import)
- **Référencé par** : aucun fichier externe (uniquement lui-même)
- **Action** : suppression pure — dead code
- **LOC supprimées** : 34

### CardPresenter.kt — CONSERVÉ

- **Import Leanback** : `Presenter` (1 import)
- **Référencé par** : 3 fichiers Jellyseerr Compose actifs
  - `JellyseerrCastRow.kt` — `CardPresenter(true, 130)` via AndroidView bridge
  - `JellyseerrPersonDetailsScreen.kt` — `CardPresenter()` via AndroidView bridge
  - `JellyseerrMediaDetailsScreen.kt` — `CardPresenter()` via AndroidView bridge
- **Raison conservation** : ces écrans Jellyseerr utilisent encore le pattern AndroidView + Leanback Presenter pour afficher des cartes dans des lignes horizontales. La migration nécessiterait de réécrire ces composants avec TvFocusCard/BrowseMediaCard pur Compose (hors scope phase 10b).

### TitleView.kt — SUPPRIMÉ

- **Import Leanback** : `TitleViewAdapter` (1 import)
- **Référencé par** : `view_lb_browse_title.xml` → `theme_jellyfin.xml` (`browseTitleViewLayout`)
- **Analyse** : l'attribut `browseTitleViewLayout` est spécifique à Leanback `BrowseSupportFragment`. Aucun fragment du projet n'étend `BrowseSupportFragment` (tous migré vers `Fragment()` + Compose). Chaîne entière = dead code.
- **Action** : suppression de la chaîne complète
- **Fichiers supprimés** :
  - `ui/shared/TitleView.kt` (40 LOC)
  - `res/layout/view_lb_browse_title.xml` (6 LOC)
  - `res/layout/view_lb_title.xml` (32 LOC) — layout interne inflé par TitleView
- **Fichier modifié** :
  - `res/values/theme_jellyfin.xml` — suppression ligne `browseTitleViewLayout`
- **Remplacement Compose** : `TvHeader` (`ui/base/tv/TvHeader.kt`) — déjà en place

### TvManager.java — CONSERVÉ (0 import Leanback)

- **Imports Leanback** : 0 (nettoyés en phase 10a)
- **Référencé par** : 7 fichiers actifs (Live TV guide + playback)
  - `CustomPlaybackOverlayFragment.java` — channels, programs, guide grid
  - `LiveTvGuideFragment.java` — channels, programs, guide grid
  - `PlaybackController.java` — `setLastLiveTvChannel()`
  - `PreviousLiveTvChannelAction.kt` — `getPrevLiveTvChannel()`
  - `CustomPlaybackOverlayFragmentHelper.kt` — `forceReload()`
  - `LiveTvGuideFragmentHelper.kt` — `forceReload()`
  - `LiveProgramDetailPopup.java` — `setTimelineRow()`, `getChannel()`
- **Méthodes actives** :
  - Channel state : `loadAllChannels()`, `getAllChannels()`, `getChannel()`, `getAllChannelsIndex()`
  - Last/prev channel : `getLastLiveTvChannel()`, `setLastLiveTvChannel()`, `getPrevLiveTvChannel()`
  - Programs cache : `getProgramsAsync()`, `getProgramsForChannel()`
  - Reload flag : `forceReload()`, `shouldForceReload()`
  - UI helpers : `setTimelineRow()`, `setFocusParams()`
- **Raison conservation** : TvManager est un singleton statique Java gérant l'état partagé Live TV (channels + programs cache). Migration vers un Repository Koin impacterait 7 fichiers Java avec refactoring significatif. Hors scope phase 10b — sera adressé lors de la migration Live TV guide vers Compose.

## Fichiers supprimés

| Fichier | LOC | Imports Leanback |
|---------|-----|-----------------|
| `ui/presentation/TextItemPresenter.kt` | 34 | 1 |
| `ui/shared/TitleView.kt` | 40 | 1 |
| `res/layout/view_lb_browse_title.xml` | 6 | 0 |
| `res/layout/view_lb_title.xml` | 32 | 0 |
| **Total supprimé** | **112** | **2** |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `res/values/theme_jellyfin.xml` | Suppression `browseTitleViewLayout` |

## Compteur Leanback

| Métrique | Avant Phase 10b | Après Phase 10b | Delta |
|----------|-----------------|-----------------|-------|
| Imports Leanback (source) | 12 | 10 | **−2** |
| Fichiers avec imports Leanback | 5 | 3 | **−2** |
| LOC supprimées | — | 112 | **−112** |

### Fichiers Leanback restants (3 fichiers, 10 imports)

#### Player overlay (9 imports, 2 fichiers)
- `TransportControlManager.kt` — 8 imports (widgets transport controls)
- `CustomAction.kt` — 1 import (`PlaybackControlsRow.MultiAction`)

#### Présentation (1 import, 1 fichier)
- `CardPresenter.kt` — 1 import (`Presenter`) — utilisé par Jellyseerr

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
```
