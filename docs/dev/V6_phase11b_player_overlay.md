# Phase 11b — Player Overlay Compose (Leanback éliminé)

## Objectif

Réécrire l'overlay du lecteur vidéo en Compose pur, supprimer `TransportControlManager.kt` (449 lignes, 8 imports Leanback), `CustomAction.kt` (héritage `PlaybackControlsRow.MultiAction`), et retirer la dépendance Gradle `androidx.leanback:leanback`.

## Résultat

| Métrique | Avant | Après |
|----------|-------|-------|
| Fichiers avec imports Leanback | 2 | **0** |
| Imports `androidx.leanback` | 10 | **0** |
| Dépendance Gradle `leanback-core` | oui | **supprimée** |
| `TransportControlManager.kt` | 449 lignes | **supprimé** |
| Actions Leanback supprimées | — | **8 fichiers** |

## Architecture

```
LeanbackOverlayFragment.java (Fragment)
  └─ ComposeView
       └─ PlayerOverlayComposeHelper.kt (bridge Java→Compose)
            └─ PlayerOverlayScreen.kt (Compose UI)
                 ├─ PlayerTimeRow (position / durée / heure de fin)
                 ├─ PlayerSeekBar (Seekbar du design system)
                 ├─ PlayerPrimaryControls (play/pause, rewind, FF, skip)
                 └─ PlayerSecondaryControls (subs, audio, quality, speed, zoom, chapitres, cast)
  └─ PlayerOverlayViewModel.kt (StateFlow, polling 500ms)
  └─ PlayerOverlayState.kt (data class + sealed classes)
```

## Fichiers créés

| Fichier | Rôle |
|---------|------|
| `overlay/compose/PlayerOverlayState.kt` | `PlayerOverlayState` data class, `PlayerPopup` sealed class, `PlayerOverlayAction` sealed class |
| `overlay/compose/PlayerOverlayViewModel.kt` | ViewModel avec `MutableStateFlow`, polling 500ms, gestion actions |
| `overlay/compose/PlayerOverlayScreen.kt` | UI Compose : overlay animé, seekbar, contrôles primaires/secondaires |
| `overlay/compose/PlayerOverlayComposeHelper.kt` | Bridge `setupPlayerOverlayComposeView()` pour interop Java→Compose |

## Fichiers supprimés

| Fichier | Raison |
|---------|--------|
| `overlay/TransportControlManager.kt` | Remplacé par PlayerOverlayViewModel + PlayerOverlayScreen |
| `overlay/action/CustomAction.kt` | Réécrit sans Leanback (plus de `MultiAction`) |
| `overlay/action/PlayerAction.kt` | Vestige inutilisé |
| `overlay/action/ChapterAction.kt` | Logique déplacée dans PlayerOverlayViewModel |
| `overlay/action/CastAction.kt` | Logique déplacée dans PlayerOverlayViewModel |
| `overlay/action/RecordAction.kt` | Logique déplacée dans PlayerOverlayViewModel |
| `overlay/action/GuideAction.kt` | Logique déplacée dans LeanbackOverlayFragment |
| `overlay/action/ChannelBarChannelAction.kt` | Logique déplacée dans LeanbackOverlayFragment |
| `overlay/action/PreviousLiveTvChannelAction.kt` | Logique déplacée dans LeanbackOverlayFragment |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `LeanbackOverlayFragment.java` | Remplacé TransportControlManager par ComposeView + ViewModel |
| `CustomPlaybackOverlayFragment.java` | Supprimé `getTransportManager().setInjectedViewsVisibility()` |
| `app/build.gradle.kts` | Supprimé `implementation(libs.androidx.leanback.core)` |
| `gradle/libs.versions.toml` | Supprimé version + alias `androidx-leanback` |

## Actions PopupMenu conservées

7 actions gardent `CustomAction` comme base (sans Leanback) pour afficher des `PopupMenu` Android :

- `ClosedCaptionsAction` — sélection piste sous-titres
- `SelectAudioAction` — sélection piste audio
- `SelectQualityAction` — sélection profil qualité
- `PlaybackSpeedAction` — vitesse de lecture
- `ZoomAction` — mode zoom (fit/crop/stretch)
- `SubtitleDelayAction` — délai sous-titres
- `AudioDelayAction` — délai audio

Ces PopupMenu pourront être migrés vers des Compose Dialogs dans une phase future.

## Score Leanback

| Métrique | Phase 11a | Phase 11b |
|----------|-----------|-----------|
| Fichiers avec imports Leanback | 2 | **0** |
| Imports `androidx.leanback` totaux | 10 | **0** |
| Layouts XML Leanback | 0 | 0 |
| Dépendances Gradle Leanback | 1 | **0** |

**Migration Leanback → Compose : 100% terminée.**
