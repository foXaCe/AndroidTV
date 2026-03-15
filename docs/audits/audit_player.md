# Audit — Nouveau lecteur vidéo (VideoPlayerFragment)

> Date : 2026-03-11
> Scope : `ui/player/video/` (nouveau player Compose) + `ui/playback/overlay/compose/` (legacy overlay Compose)
> Hors scope : `CustomPlaybackOverlayFragment` (legacy fragment Java/Kotlin)

---

## 1. Architecture des fichiers

### Nouveau player (100% Compose)

| Fichier | Rôle | LOC | % Compose |
|---------|------|-----|-----------|
| `ui/player/video/VideoPlayerFragment.kt` | Fragment hôte, crée la queue, gère lifecycle | 87 | 100% (content{}) |
| `ui/player/video/VideoPlayerScreen.kt` | Écran racine : surface vidéo + overlay + sous-titres | 78 | 100% |
| `ui/player/video/VideoPlayerOverlay.kt` | Orchestre header + controls + toasts + gestion D-pad | 70 | 100% |
| `ui/player/video/VideoPlayerControls.kt` | Boutons de contrôle + seekbar + temps | 303 | 100% |
| `ui/player/video/VideoPlayerHeader.kt` | Titre, sous-titre, badges techniques (4K, HEVC, HDR, 5.1) | 195 | 100% |
| `ui/player/video/toast/rememberPlaybackManagerMediaToastEmitter.kt` | Émetteur toast play/pause automatique | 44 | 100% |

### Composants partagés (ui/player/base/)

| Fichier | Rôle | LOC | % Compose |
|---------|------|-----|-----------|
| `ui/player/base/PlayerOverlayLayout.kt` | Layout overlay avec animations slide/fade + gestion clavier D-pad | 209 | 100% |
| `ui/player/base/PlayerSeekbar.kt` | Wrapper seekbar lié au PlaybackManager | 46 | 100% |
| `ui/player/base/PlayerHeader.kt` | Layout header avec horloge | 33 | 100% |
| `ui/player/base/PlayerSubtitles.kt` | Sous-titres via AndroidView (PlayerSubtitleView) | 20 | ~50% (AndroidView) |
| `ui/player/base/PlayerSurface.kt` | Surface vidéo (AndroidView) | ~20 | ~50% (AndroidView) |
| `ui/player/base/toast/MediaToast.kt` | Toast visuel centré (icône + progress ring) | 105 | 100% |
| `ui/player/base/toast/MediaToastRegistry.kt` | Registre des toasts avec auto-dismiss 700ms | 46 | N/A (Kotlin) |
| `ui/player/base/toast/MediaToastData.kt` | Data class toast (icon + progress) | 10 | N/A |

### Legacy overlay Compose (ui/playback/overlay/compose/)

| Fichier | Rôle | LOC | % Compose |
|---------|------|-----|-----------|
| `PlayerOverlayScreen.kt` | Overlay legacy complet : time, seekbar, contrôles primaires + secondaires | 344 | 100% |
| `PlayerOverlayState.kt` | State + Actions du player legacy (sealed classes) | 137 | N/A |
| `PlayerOverlayViewModel.kt` | ViewModel legacy (polling 500ms, popups, delays) | 351 | N/A |
| `PlayerOverlayComposeHelper.kt` | Bridge ComposeView pour le legacy fragment | 28 | 100% |
| `PlayerDialogs.kt` | Dialogs : Audio, Subtitles, Quality, Speed, Zoom, Delays | 293 | 100% |
| `PlayerDialogDefaults.kt` | Composants Dialog/Item/Stepper réutilisables | 235 | 100% |

### Écrans complémentaires

| Fichier | Rôle | LOC | % Compose |
|---------|------|-----|-----------|
| `ui/playback/nextup/NextUpFragment.kt` | Écran "Next Up" avec timer auto-play | 266 | 100% |
| `ui/playback/stillwatching/StillWatchingFragment.kt` | Écran "Still Watching?" avec timer | 273 | 100% |
| `ui/playback/overlay/SkipOverlayView.kt` | Bouton skip intro/recap/outro/next episode | 231 | 100% (via AbstractComposeView) |

---

## 2. Inventaire UI actuel

### 2.1 Nouveau player (VideoPlayerFragment)

#### Header (haut)
- **Titre** du média (`item.name`)
- **Sous-titre** : série / année / durée (format "SeriesName · 2024 · 1h30")
- **Badges techniques** : résolution (4K/1080p/720p), codec (HEVC/AV1/H.264/VP9), HDR (DV/HDR10+/HDR10/HDR), audio (5.1/Stereo)
- **Horloge** (ToolbarClock, alignée à droite)

#### Contrôles (bas)
- **Row 1 — Boutons principaux** (gauche → droite) :
  - Play/Pause (animé, auto-focus à l'apparition)
  - Rewind
  - Fast Forward
  - Spacer (pousse "More" à droite)
  - More Options (⋯) → Popover contenant Previous/Next Entry

- **Row 2 — Seekbar** :
  - Hauteur animée : 3dp (normal) → 8dp (focusé)
  - Couleurs : fond `VegafoXColors.Outline`, progression `VegafoXColors.OrangePrimary`, buffer `TextSecondary`, knob `TextPrimary`
  - Navigation D-pad gauche/droite pour scrub
  - Knob apparaît en alpha animée au focus

- **Row 3 — Temps** :
  - Position courante (gauche, `onSurface`)
  - Temps restant (droite, `-HH:MM:SS`, `onSurfaceVariant`)

#### Toasts (centre écran)
- Icône centrée dans cercle semi-transparent (96dp)
- Animations : fadeIn + scaleIn(0.8f) / fadeOut + scaleOut(1.2f)
- Auto-dismiss après 700ms
- Émis automatiquement pour : Play, Pause, FastForward, Rewind

#### Overlay Layout
- **Gradient haut** : background 85% → 40% → transparent (1/3 hauteur)
- **Gradient bas** : transparent → 40% → 85% background (1/3 hauteur)
- **Animations** : slideIn/Out + fadeIn/Out
- **Auto-hide** : 3 secondes d'inactivité

### 2.2 Legacy overlay (PlayerOverlayScreen — CustomPlaybackOverlayFragment)

#### Time Row (haut des contrôles)
- Position formatée (gauche, blanc)
- End time ou durée (droite, blanc 70%)

#### Seekbar
- Hauteur fixe 6dp
- Utilise le composant `Seekbar` du design system
- Inclut buffer visuel

#### Contrôles primaires (centrés)
- Previous (si hasPreviousItem)
- Rewind (si canSeek)
- **Play/Pause** (56dp, plus grand)
- FastForward (si canSeek)
- Next (si hasNextItem)
- Couleurs : blanc 15% bg, blanc content, 30% bg focused

#### Contrôles secondaires (centrés, 40dp chacun)
- Sous-titres (si hasSubs)
- Audio (si hasMultiAudio)
- Channels (si isLiveTv)
- Guide (si isLiveTv)
- Record (si isLiveTv + canRecord)
- Chapitres (si hasChapters)
- Cast (si hasCast)
- Vitesse (si !isLiveTv)
- Qualité (si !isLiveTv)
- Audio Delay (si !isLiveTv)
- Subtitle Delay (si hasSubs)
- Zoom (toujours)

#### Dialogs/Popups (PlayerDialogs.kt)
- Audio Track : liste scrollable, item 56dp, checkmark sélection
- Subtitle Track : idem avec option "None"
- Quality : liste des profils bitrate
- Speed : liste des vitesses (0.25x → 3.00x)
- Zoom : modes (Normal, Stretch, Crop, etc.)
- Subtitle Delay : stepper ±50ms avec Reset
- Audio Delay : idem
- Chapters : liste scrollable avec auto-scroll
- Cast : liste des personnes

### 2.3 Skip Overlay (SkipOverlayView)
- Positionné en bas-droite (padding 60dp, 80dp)
- Fond blanc 95%, bordure blanche 2dp, coins arrondis
- Texte noir + icône SkipNext noir
- Textes contextuels : "Skip Intro", "Skip Recap", "Skip Commercial", etc.
- Countdown pour next episode
- Auto-hide configurable
- Auto-play à expiration du timer

### 2.4 Next Up (NextUpFragment)
- Background flou (BlurContext.DETAILS)
- Logo en haut-gauche
- Thumbnail en bas-gauche
- Texte "Next Up" + titre en bas-droite
- Boutons : Cancel + Watch Now (ProgressButton avec timer)
- Timer configurable (UserPreferences.nextUpTimeout)

### 2.5 Still Watching (StillWatchingFragment)
- Structure similaire à Next Up
- Texte "Are you still watching?" + titre
- Boutons : Exit (ProgressButton 10s) + Continue Watching
- Auto-exit à expiration du timer

---

## 3. États du player

| État | Visuel | Transition |
|------|--------|-----------|
| **Lecture** | Vidéo en plein écran, pas de contrôles | Auto-hide après 3s |
| **Contrôles visibles** | Overlay header (haut) + controls (bas) avec gradients | Apparition : slide + fade |
| **Pause** | Toast Pause centré, puis contrôles visibles | Play/Pause toggle |
| **Buffering** | Pas d'indicateur dédié visible dans le code | — |
| **Scrubbing** | Seekbar agrandie (3→8dp), knob visible | Focus D-pad sur seekbar |
| **Skip Intro/Outro** | Bouton blanc en bas-droite | Automatique via MediaSegmentRepository |
| **Next Episode** | Bouton avec countdown en bas-droite | Automatique en fin d'épisode |
| **Next Up** | Écran dédié (NextUpFragment) | Remplace le player |
| **Still Watching** | Écran dédié (StillWatchingFragment) | Remplace le player |
| **Popup ouverte** | Dialog centré (Audio/Subtitles/Quality/Speed/Zoom/Delay) | Via contrôles secondaires |
| **Popover More** | Petit menu au-dessus du bouton ⋯ | Clic sur More Options |

### Gestion D-pad (quand contrôles cachés)
- **Center/Enter** → Play/Pause (avec toast)
- **Droite** → Seek forward (avec toast)
- **Gauche** → Seek backward (avec toast)
- **Toute autre touche** → Affiche les contrôles
- **Back** (quand contrôles visibles) → Cache les contrôles

---

## 4. Screenshots

> **Note** : Les captures ont été prises alors qu'aucun film n'était en lecture sur le Ugoos AM9 Pro.
> Les images montrent l'écran d'accueil/screensaver au lieu du lecteur vidéo.

| Fichier | Contenu réel |
|---------|-------------|
| `docs/screenshots/player_controls.png` | Écran d'accueil (La Mif) — pas le player |
| `docs/screenshots/player_clean.png` | Écran d'accueil (Harry Potter) — pas le player |
| `docs/screenshots/player_pause.png` | Écran d'accueil (The White Princess) — pas le player |

**TODO** : Relancer les captures pendant une lecture vidéo active.

---

## 5. Problèmes identifiés

### 5.1 Deux systèmes de player coexistent

| Aspect | Nouveau (VideoPlayerFragment) | Legacy (CustomPlaybackOverlayFragment) |
|--------|-------------------------------|---------------------------------------|
| Architecture | PlaybackManager (playback-core) | PlaybackController (Java legacy) |
| Contrôles secondaires | **ABSENTS** (pas de sous-titres, audio, speed, quality, zoom, delay) | Complets (12 boutons) |
| Dialogs | **ABSENTS** | 9 types de dialogs |
| Info header | Badges techniques | Titre + sous-titre seulement |
| Seekbar | Animée (hauteur variable) | Fixe 6dp |
| More Options | Popover (Previous/Next seulement) | N/A |

**Constat** : Le nouveau player est visuellement supérieur mais fonctionnellement incomplet. Il manque la majorité des contrôles secondaires.

### 5.2 Couleurs non VegafoX

| Élément | Actuel | Attendu VegafoX |
|---------|--------|-----------------|
| Seekbar progression | `OrangePrimary` | OK ✅ |
| Seekbar fond | `Outline` | OK ✅ |
| Seekbar knob | `TextPrimary` | OK ✅ |
| Header titre | `onSurface` (theme) | OK ✅ |
| Header sous-titre | `onSurfaceVariant` | OK ✅ |
| Badge chips | `surface @ 60%` | OK ✅ |
| Legacy overlay gradient | `Color.Black @ 85%` | ⚠️ Hardcodé, pas via theme |
| Legacy boutons primaires | `Color.White @ 15%/30%` | ⚠️ Hardcodé, pas via theme |
| Legacy boutons secondaires | `Color.White @ 80%` tint | ⚠️ Hardcodé, pas via theme |
| Legacy temps | `Color.White` / `White @ 70%` | ⚠️ Hardcodé, pas via theme |
| Skip overlay | `Color.White @ 95%` fond, `Color.Black` texte | ⚠️ Style Jellyfin, pas VegafoX |

### 5.3 Problèmes de design

1. **Skip overlay** : Fond blanc sur fond sombre = style Jellyfin vanilla, pas VegafoX. Devrait utiliser les couleurs du design system.

2. **Absence d'indicateur de buffering** : Aucun spinner ou indicateur visible pendant le chargement.

3. **Legacy overlay** : Utilise `TvIconButton` (composant spécifique) au lieu de `IconButton` du design system → inconsistance.

4. **PlayerSubtitles** : Toujours en `AndroidView` (PlayerSubtitleView natif) — pas de contrôle Compose sur le style.

5. **Pas de title/subtitle dans le nouveau player pour les épisodes** : Le header montre `item.name` mais pas le format "S01E05 - Episode Title" de manière optimale pour les séries.

6. **ToolbarClock** dans le header player : Potentiellement superflu/distrayant pendant la lecture.

### 5.4 Problèmes fonctionnels du nouveau player

1. **Pas de sélection audio** — critique pour les films multi-langues
2. **Pas de sélection sous-titres** — critique
3. **Pas de sélection qualité** — important pour le streaming
4. **Pas de contrôle de vitesse**
5. **Pas de chapitres**
6. **Pas de zoom/aspect ratio**
7. **Pas de delay audio/subtitle**
8. **Pas de cast list**
9. **Pas de support Live TV** (channels, guide, record)

---

## 6. Résumé

### Fichiers principaux (5)
1. `ui/player/video/VideoPlayerScreen.kt` — Écran racine
2. `ui/player/video/VideoPlayerControls.kt` — Contrôles (incomplets)
3. `ui/player/video/VideoPlayerOverlay.kt` — Orchestrateur overlay
4. `ui/player/base/PlayerOverlayLayout.kt` — Layout + D-pad
5. `ui/playback/overlay/compose/PlayerOverlayScreen.kt` — Legacy overlay (référence fonctionnelle)

### % Compose actuel
- **Nouveau player** : 100% Compose (sauf PlayerSurface et PlayerSubtitles = AndroidView)
- **Legacy overlay** : 100% Compose (via ComposeView bridge)
- **Skip/NextUp/StillWatching** : 100% Compose

### Éléments UI présents (nouveau player)
- ✅ Surface vidéo
- ✅ Sous-titres
- ✅ Header (titre, sous-titre, badges, horloge)
- ✅ Play/Pause, Rewind, FastForward
- ✅ Previous/Next (via More Options popover)
- ✅ Seekbar animée
- ✅ Position + temps restant
- ✅ Toasts visuels
- ✅ Overlay auto-hide 3s
- ✅ Gestion D-pad complète
- ❌ Sélection audio
- ❌ Sélection sous-titres
- ❌ Sélection qualité
- ❌ Contrôle vitesse
- ❌ Chapitres
- ❌ Zoom / Aspect ratio
- ❌ Délais audio/subtitle
- ❌ Cast
- ❌ Live TV (channels, guide, record)
- ❌ Indicateur buffering

### Boutons de contrôle disponibles
| Bouton | Nouveau | Legacy |
|--------|---------|--------|
| Play/Pause | ✅ | ✅ |
| Rewind | ✅ | ✅ |
| FastForward | ✅ | ✅ |
| Previous | ✅ (popover) | ✅ |
| Next | ✅ (popover) | ✅ |
| Subtitles | ❌ | ✅ |
| Audio | ❌ | ✅ |
| Channels | ❌ | ✅ |
| Guide | ❌ | ✅ |
| Record | ❌ | ✅ |
| Chapters | ❌ | ✅ |
| Cast | ❌ | ✅ |
| Speed | ❌ | ✅ |
| Quality | ❌ | ✅ |
| Audio Delay | ❌ | ✅ |
| Subtitle Delay | ❌ | ✅ |
| Zoom | ❌ | ✅ |

### Problèmes prioritaires
1. **P0** — Contrôles secondaires absents du nouveau player (audio, sous-titres, qualité)
2. **P1** — Deux systèmes de player parallèles → dette technique
3. **P1** — Skip overlay avec style Jellyfin (blanc) non VegafoX
4. **P2** — Legacy overlay avec couleurs hardcodées (pas de theme tokens)
5. **P2** — Absence d'indicateur de buffering
6. **P3** — PlayerSubtitles reste en AndroidView

### Estimation effort redesign

| Tâche | Effort |
|-------|--------|
| Ajouter contrôles secondaires au nouveau player | **L** |
| Migrer les 9 types de dialogs | **M** |
| Redesign skip overlay VegafoX | **S** |
| Ajouter indicateur buffering | **S** |
| Thématiser les couleurs hardcodées du legacy | **M** |
| Unifier les deux systèmes en un seul | **XL** |
| **TOTAL redesign complet** | **XL** |

**Recommandation** : Enrichir le nouveau player avec les contrôles secondaires du legacy (portage des dialogs existants) plutôt que de maintenir deux systèmes parallèles. Les dialogs Compose du legacy (`PlayerDialogs.kt`, `PlayerDialogDefaults.kt`) sont réutilisables directement.
