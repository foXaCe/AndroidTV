# Phase 12 — Player Popups Migration (PopupMenu → Compose Dialogs)

> **Date : 2026-03-09**
> **Objectif** : Migrer les 7 PopupMenu Android natifs du player overlay vers des Compose Dialogs TV-friendly
> **Statut** : TERMINÉ — BUILD SUCCESSFUL

---

## 1. Résultat

| Métrique | Avant | Après |
|----------|-------|-------|
| PopupMenu Android dans le player | 7 | **0** |
| Fichiers Action popup | 8 (7 + CustomAction) | **0** |
| Compose Dialogs | 0 | **7** |
| Composants réutilisables | 0 | **3** (PlayerDialog, PlayerDialogItem, PlayerDialogStepper) |
| Player overlay Compose | 99% | **100%** |

---

## 2. Fichiers créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `overlay/compose/PlayerDialogDefaults.kt` | ~190 | Composants réutilisables : PlayerDialog, PlayerDialogItem, PlayerDialogStepper |
| `overlay/compose/PlayerDialogs.kt` | ~230 | 7 dialog composables + PlayerPopupHost |

---

## 3. Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `overlay/compose/PlayerOverlayState.kt` | PlayerPopup Audio/Subtitles/Quality/Speed/Zoom → data class avec données ; ajout SubtitleDelay, AudioDelay ; nouvelles actions Show*Delay, Step*Delay, Reset*Delay ; SelectQuality/Speed/Zoom portent les vrais types |
| `overlay/compose/PlayerOverlayViewModel.kt` | Ajout userPreferences param ; builders popup (audio tracks, subtitles, quality, speed, zoom, delays) ; gestion stepper ±50ms ; applySubtitleDelay/applyAudioDelay ; fading control dans showPopup/dismissPopup |
| `overlay/compose/PlayerOverlayScreen.kt` | Boutons audio delay → ShowAudioDelay ; subtitle delay → ShowSubtitleDelay |
| `overlay/compose/PlayerOverlayComposeHelper.kt` | Ajout PlayerPopupHost dans la composition |
| `overlay/LeanbackOverlayFragment.java` | Supprimé 7 champs Action + imports ; simplifié handleComposeAction (plus de PopupMenu) ; simplifié onDestroyView ; onMediaKey utilise ViewModel directement |

---

## 4. Fichiers supprimés

| Fichier | Raison |
|---------|--------|
| `overlay/action/ClosedCaptionsAction.kt` | Remplacé par SubtitleTrackDialog |
| `overlay/action/SelectAudioAction.kt` | Remplacé par AudioTrackDialog |
| `overlay/action/SelectQualityAction.kt` | Remplacé par QualityDialog |
| `overlay/action/PlaybackSpeedAction.kt` | Remplacé par PlaybackSpeedDialog |
| `overlay/action/ZoomAction.kt` | Remplacé par ZoomDialog |
| `overlay/action/SubtitleDelayAction.kt` | Remplacé par SubtitleDelayDialog (stepper) |
| `overlay/action/AudioDelayAction.kt` | Remplacé par AudioDelayDialog (stepper) |
| `overlay/action/CustomAction.kt` | Plus de sous-classes |

---

## 5. Architecture des dialogs

```
PlayerOverlayComposeHelper
  └─ JellyfinTheme
       ├─ PlayerOverlayScreen (overlay contrôles)
       └─ PlayerPopupHost (dispatch popup → dialog)
            ├─ AudioTrackDialog → PlayerDialog + LazyColumn<PlayerDialogItem>
            ├─ SubtitleTrackDialog → PlayerDialog + LazyColumn<PlayerDialogItem>
            ├─ QualityDialog → PlayerDialog + LazyColumn<PlayerDialogItem>
            ├─ PlaybackSpeedDialog → PlayerDialog + LazyColumn<PlayerDialogItem>
            ├─ ZoomDialog → PlayerDialog + Column<PlayerDialogItem>
            ├─ SubtitleDelayDialog → PlayerDialog + PlayerDialogStepper
            └─ AudioDelayDialog → PlayerDialog + PlayerDialogStepper
```

### Composants réutilisables

| Composant | Rôle |
|-----------|------|
| `PlayerDialog` | Dialog wrapper : Dialog + Box centered + Column with title, border, dialog shape |
| `PlayerDialogItem` | Item de liste : 56dp height, focus ring, scale 1.04x, check mark si sélectionné |
| `PlayerDialogStepper` | Stepper ±/Reset : −/+ buttons 48dp, value display bold, reset button |

### Design System

- Background : `dialogSurface` (Color(0xFF1C1C28))
- Border : `outlineVariant` (1dp)
- Shape : `JellyfinTheme.shapes.dialog`
- Focus ring : `focusRing` (cyan 500) avec scale 1.04x
- Selected : texte + check en `primary` (cyan 500)
- Typography : `titleLarge` (titre), `bodyLarge` (items), `headlineMedium` bold (stepper value)

---

## 6. Stepper delays (amélioration UX)

Les anciens délais étaient des listes fixes :
- Subtitle delay : 10 options (0 à +3000ms)
- Audio delay : 15 options (-2000 à +2000ms)

Les nouveaux steppers offrent :
- Pas de 50ms (plus précis)
- Plage ±10 000ms (plus large)
- Bouton Reset → 0ms
- Affichage : "+200ms", "−100ms", "0ms"

---

## 7. Vérification PopupMenu

```
grep -r "PopupMenu\|PopupWindow" --include="*.kt" --include="*.java" \
  app/src/main/java/org/jellyfin/androidtv/ui/playback/ -l
→ 0 résultats
```

---

## 8. Score final

| Composant | Score |
|-----------|-------|
| Player overlay Compose | 100/100 |
| Player popups Compose | 100/100 |
| Leanback dans le player | 0 imports |
| PopupMenu dans le player | 0 |

**Migration player overlay : 100% Compose.**
