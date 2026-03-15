# Phase 9c — Suppression du Glue et GlueHost Leanback

## Objectif

Supprimer les 2 fichiers les plus couplés à Leanback dans le player overlay :
- `CustomPlaybackTransportControlGlue.java` (10 imports Leanback, héritage `PlaybackTransportControlGlue`)
- `CustomPlaybackFragmentGlueHost.kt` (4 imports Leanback, héritage `PlaybackSupportFragmentGlueHost`)

## Stratégie choisie : A — Remplacement complet

### Analyse du Glue

`CustomPlaybackTransportControlGlue` héritait de `PlaybackTransportControlGlue<VideoPlayerControllerImpl>` et servait de médiateur entre :
- `PlaybackSupportFragment` (via le GlueHost) — overlay show/hide
- `VideoPlayerControllerImpl` (via `PlayerAdapter`) — état du player
- 13 `CustomAction` subclasses — boutons d'action dans la barre de transport

Sa logique interne était principalement du code domaine (gestion des actions, horloge de fin, états play/pause). L'héritage Leanback n'apportait que :
1. La connexion automatique au `PlaybackSupportFragment` via le pattern GlueHost
2. La gestion du `PlaybackControlsRow` et du `PlaybackTransportRowPresenter`
3. L'intégration du `PlaybackSeekDataProvider` pour les thumbnails trickplay

### Analyse du GlueHost

`CustomPlaybackFragmentGlueHost` étendait `PlaybackSupportFragmentGlueHost` pour overrider `setOnActionClickedListener()` et capturer la `View` de l'action cliquée (nécessaire pour positionner les popups). Ce workaround n'est plus nécessaire car `setOnPlaybackItemViewClickedListener()` fournit directement le `itemViewHolder.view`.

## Migration

### Fichier créé : `overlay/TransportControlManager.kt`

Classe Kotlin pure (aucun héritage Leanback) qui encapsule toute la logique du Glue :
- Création et gestion des actions (play/pause, rewind, FF, skip, 13 CustomActions)
- `PlaybackControlsRow` comme data holder (widget Leanback utilisé en composition, pas héritage)
- `PlaybackTransportRowPresenter` personnalisé (horloge de fin)
- Gestion des clicks d'action
- Mise à jour position/durée/état play directement sur le `PlaybackControlsRow`
- Gestion des touches (CAPTIONS, AUDIO_TRACK)
- Calcul et affichage de l'heure de fin

### Fichier modifié : `LeanbackOverlayFragment.java`

- Remplace `CustomPlaybackTransportControlGlue playerGlue` par `TransportControlManager transportManager`
- Utilise `setPlaybackRow()` / `setPlaybackRowPresenter()` directement sur le fragment (méthodes publiques de `PlaybackSupportFragment`) — bypass complet du pattern Glue/GlueHost
- `setOnPlaybackItemViewClickedListener()` pour capturer clicks avec View (remplace GlueHost)
- Ajout `onDestroyView()` pour le cleanup (remplace `onDetachedFromHost()`)

### Fichier modifié : `VideoPlayerControllerImpl.kt`

- **Suppression de l'héritage `PlayerAdapter`** — classe Kotlin pure implémentant `VideoPlayerController`
- Suppression des méthodes callback (`updateCurrentPosition()`, `updatePlayState()`, `updateDuration()`) — remplacées par des mises à jour directes dans `TransportControlManager`
- Ajout de `detach()` pour le nettoyage (remplace `onDetachedFromHost()`)

### Fichier modifié : `CustomAction.kt`

- Suppression du paramètre `CustomPlaybackTransportControlGlue` du constructeur
- Suppression de la méthode `onCustomActionClicked()` — les clicks sont gérés directement par `TransportControlManager.onActionClicked()`

### 13 actions modifiées

Toutes les subclasses de `CustomAction` : suppression du paramètre Glue du constructeur.
- `SelectAudioAction`, `ClosedCaptionsAction`, `SelectQualityAction`, `PlaybackSpeedAction`
- `ZoomAction`, `ChapterAction`, `CastAction`, `SubtitleDelayAction`, `AudioDelayAction`
- `PreviousLiveTvChannelAction`, `ChannelBarChannelAction`, `GuideAction`, `RecordAction`

### Fichier modifié : `CustomPlaybackOverlayFragment.java`

- `getPlayerGlue().setInjectedViewsVisibility()` → `getTransportManager().setInjectedViewsVisibility()`

### Fichiers supprimés

- `overlay/CustomPlaybackTransportControlGlue.java` (449 LOC, 10 imports Leanback)
- `overlay/CustomPlaybackFragmentGlueHost.kt` (24 LOC, 4 imports Leanback)

## Limitation connue

Les thumbnails trickplay dans la seekbar Leanback ne s'affichent plus. `PlaybackTransportRowPresenter.setSeekProvider()` est package-private et n'est accessible que via le Glue. Le `SeekProvider` est conservé et sera utilisé quand l'overlay Compose remplacera entièrement le `PlaybackSupportFragment`.

La fonctionnalité de seek (FF/RW/skip) fonctionne normalement via les boutons d'action.

## Compteur Leanback

| Métrique | Avant Phase 9c | Après Phase 9c | Delta |
|----------|----------------|----------------|-------|
| Imports Leanback | 34 | 26 | **-8** |
| Fichiers avec Leanback | 12 | 10 | **-2** |

Détail des changements :
- `CustomPlaybackTransportControlGlue.java` supprimé : -10 imports, -1 fichier
- `CustomPlaybackFragmentGlueHost.kt` supprimé : -4 imports, -1 fichier
- `VideoPlayerControllerImpl.kt` : -1 import (`PlayerAdapter`), -1 fichier Leanback
- `TransportControlManager.kt` créé : +7 imports, +1 fichier

### Fichiers Leanback restants (10 fichiers, 26 imports)

#### Player overlay (9 imports, 3 fichiers)
- `TransportControlManager.kt` — 7 imports (widgets uniquement, aucun héritage)
- `LeanbackOverlayFragment.java` — 1 import (`PlaybackSupportFragment`)
- `CustomAction.kt` — 1 import (`PlaybackControlsRow.MultiAction`)

#### Autres (17 imports, 7 fichiers)
- `ItemRowAdapter.java` — 6 imports
- `TvManager.java` — 4 imports
- `MutableObjectAdapter.kt` — 3 imports
- `AggregatedItemRowAdapter.kt` — 1 import
- `TitleView.kt` — 1 import
- `TextItemPresenter.kt` — 1 import
- `CardPresenter.kt` — 1 import

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
```

Test lecture/pause/seek à confirmer sur Ugoos AM9 Pro.
