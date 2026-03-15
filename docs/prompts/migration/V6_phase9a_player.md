# Phase 9a — Migration Actions Player (overlay/action)

## Cartographie des dépendances

```
PlaybackController (bridge ExoPlayer)
        ↓
VideoPlayerAdapter (extends Leanback PlayerAdapter)
        ↓
CustomPlaybackTransportControlGlue (extends Leanback PlaybackTransportControlGlue)
        ↓ ←→ Actions (5 simples supprimées + 13 CustomAction conservées)
CustomPlaybackFragmentGlueHost (extends Leanback PlaybackSupportFragmentGlueHost)
        ↓
LeanbackOverlayFragment (extends Leanback PlaybackSupportFragment)
        ↓
CustomPlaybackOverlayFragment (master overlay UI)
        ↓
CustomSeekProvider (extends Leanback PlaybackSeekDataProvider) — trickplay thumbnails
```

### Imports Leanback par fichier (avant phase 9a)

| Fichier | Imports Leanback | Rôle Leanback |
|---------|-----------------|---------------|
| CustomPlaybackTransportControlGlue | 9 | Transport, seekbar, actions, presenters |
| CustomPlaybackFragmentGlueHost | 4 | Fragment hosting, click routing |
| LeanbackOverlayFragment | 1 | PlaybackSupportFragment (overlay show/hide/fade) |
| VideoPlayerAdapter | 1 | PlayerAdapter (bridge play/pause/seek/callbacks) |
| CustomSeekProvider | 1 | PlaybackSeekDataProvider (seek positions + thumbnails) |
| 5 AndroidAction wrappers | 1 chacun (5 total) | PlaybackControlsRow.*Action (icônes, multi-state) |
| AndroidAction interface | 0 | Interface pure (pas d'import Leanback) |
| CustomAction base | 1 | PlaybackControlsRow.MultiAction (icône + popup) |

## Stratégie choisie : B (Encapsulation) + migration incrémentale

### Justification

Le player overlay est le composant Leanback le plus profondément couplé du projet :

1. **Chaque fichier de la chaîne étend une classe Leanback** — pas de simple wrapper, mais des `extends` directs sur `PlaybackTransportControlGlue`, `PlayerAdapter`, `PlaybackSupportFragment`, `PlaybackSeekDataProvider`, etc.

2. **Le Glue fournit un système complet** — transport seekbar avec scrubbing + trickplay thumbnails + layout d'actions (primary/secondary rows) + overlay management (show/hide/fade) + key event routing + description presenter. Remplacer tout = reconstruire un player UI complet.

3. **Risque maximal** — Le player vidéo est la fonctionnalité CORE. Un remplacement total pourrait casser la lecture.

4. **Les 5 actions simples sont le point de découplage propre** — Les wrappers `AndroidAction` (PlayPause, Rewind, FF, SkipPrev, SkipNext) ne font qu'ajouter 1-3 lignes de logique sur les classes Leanback de base. Leur code est trivial et peut être inliné dans le Glue.

5. **CustomAction NE PEUT PAS être supprimé** — C'est la classe de base pour 13 sous-classes avec des PopupMenus complexes (audio, subtitles, quality, speed, zoom, chapters, cast, recording, delays...).

## Fichiers créés

| Fichier | Rôle |
|---------|------|
| `overlay/action/PlayerAction.kt` | Sealed class pour représenter les actions player indépendamment de Leanback. Prépare la future couche Compose. |

## Fichiers supprimés

| Fichier | Import Leanback |
|---------|-----------------|
| `overlay/action/PlayPauseAction.kt` | 1 (`PlaybackControlsRow.PlayPauseAction`) |
| `overlay/action/RewindAction.kt` | 1 (`PlaybackControlsRow.RewindAction`) |
| `overlay/action/FastForwardAction.kt` | 1 (`PlaybackControlsRow.FastForwardAction`) |
| `overlay/action/SkipPreviousAction.kt` | 1 (`PlaybackControlsRow.SkipPreviousAction`) |
| `overlay/action/SkipNextAction.kt` | 1 (`PlaybackControlsRow.SkipNextAction`) |
| `overlay/action/AndroidAction.kt` | 0 (interface pure) |
| **Total** | **5 imports Leanback** |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `CustomPlaybackTransportControlGlue.java` | Types changés de wrappers custom vers `PlaybackControlsRow.*Action` directement ; `onActionClicked()` utilise `instanceof` au lieu de l'interface `AndroidAction` ; 6 imports supprimés (5 wrappers + AndroidAction) |

## Compteur Leanback

| Métrique | Avant Phase 9a | Après Phase 9a | Delta |
|----------|----------------|----------------|-------|
| Imports Leanback | 39 | 34 | **-5** |
| Fichiers avec Leanback | 18 | 13 | **-5** |

## Fichiers Leanback restants (13 fichiers, 34 imports)

### Player overlay (17 imports, 6 fichiers) — Leanback structurel
- `CustomPlaybackTransportControlGlue.java` — 9 imports
- `CustomPlaybackFragmentGlueHost.kt` — 4 imports
- `LeanbackOverlayFragment.java` — 1 import
- `VideoPlayerAdapter.java` — 1 import
- `CustomSeekProvider.kt` — 1 import
- `CustomAction.kt` — 1 import

### Autres (17 imports, 7 fichiers)
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

Installé et testé sur Ugoos AM9 Pro — app démarre sans crash, logs propres.
