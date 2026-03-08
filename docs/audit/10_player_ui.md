# Audit 10 — Player UI Video

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Toutes les modifications vérifiées dans le code :
> - Auto-hide 3s (PlayerOverlayLayout.kt timeout = 3.seconds)
> - D-pad direct (onPlayPause, onSeekForward, onSeekBackward callbacks)
> - Seekbar animée (3dp ↔ 8dp via animateDpAsState)
> - Position split gauche/droite
> - Badges techniques (MediaBadgeChip : resolution, codec, HDR, audio)
> - Gradients DS 3 stops
> - Color.White/Black éliminés du player

## Resume

Audit et refonte de l'interface du player video Compose (`ui/player/video/`).
Objectif : experience premium et irréprochable pour l'ecran ou l'utilisateur passe 90% de son temps.

**Date** : 2026-03-07
**Compilation** : BUILD SUCCESSFUL apres toutes les modifications

---

## 1. Architecture du player

Le player video utilise l'architecture Compose moderne (pas le legacy Leanback) :

```
VideoPlayerFragment (Fragment)
  └─ VideoPlayerScreen (Composable)
       ├─ PlayerSurface (rendu video ExoPlayer)
       ├─ VideoPlayerOverlay
       │    └─ PlayerOverlayLayout (base)
       │         ├─ header → VideoPlayerHeader
       │         └─ controls → VideoPlayerControls
       │              ├─ Boutons (Play/Pause, Rwd, FF, More)
       │              ├─ PlayerSeekbar → Seekbar
       │              └─ Position / Temps restant
       ├─ PlayerSubtitles
       └─ MediaToasts (feedback visuel play/pause/seek)
```

### Systeme de toast existant
- `MediaToastRegistry` gere une file de toasts avec duree (700ms)
- `MediaToast` : cercle semi-transparent avec icone animee (scale in/out)
- Utilise pour play/pause via `rememberPlaybackManagerMediaToastEmitter`

### Systeme de skip existant
- `SkipOverlayView` : overlay pour skip intro/outro/recap + next episode
- Countdown visuel pour episode suivant
- Deja bien implemente et separe

---

## 2. Modifications appliquees

### 2.1 Auto-hide : 5s → 3s

| Fichier | Modification |
|---------|-------------|
| `PlayerOverlayLayout.kt:139` | `timeout: Duration = 5.seconds` → `3.seconds` |

**Justification** : 5 secondes est trop long pour un overlay de player video. 3 secondes est le standard (Netflix, YouTube, Plex). L'overlay se reaffiche instantanement a la moindre touche.

### 2.2 D-pad direct : play/pause et seek sans afficher l'overlay

| Fichier | Modification |
|---------|-------------|
| `PlayerOverlayLayout.kt` | Ajout parametres `onPlayPause`, `onSeekForward`, `onSeekBackward` |
| `PlayerOverlayLayout.kt` | Key handler : OK → play/pause, ←/→ → seek, autre → overlay |
| `VideoPlayerOverlay.kt` | Cable les callbacks au `playbackManager` + toast feedback |

**Avant** :
```
Overlay masque + touche OK → affiche overlay → naviguer → Play
Overlay masque + touche ← → affiche overlay
```

**Apres** :
```
Overlay masque + touche OK → play/pause direct + toast icone
Overlay masque + touche ← → seek rewind + toast icone
Overlay masque + touche → → seek forward + toast icone
Overlay masque + touche ↑/↓ → affiche overlay
```

**Justification** : Sur TV, l'utilisateur s'attend a ce que OK = play/pause et ←/→ = seek, comme sur tous les players majeurs. L'overlay ne doit apparaitre que pour les actions secondaires.

### 2.3 Barre de progression animee

| Fichier | Modification |
|---------|-------------|
| `VideoPlayerControls.kt` | Seekbar : `height(4.dp)` → `animateDpAsState(3.dp ↔ 8.dp)` |

**Avant** :
```
[========== 4dp fixe ==========]
```

**Apres** :
```
[══════════ 3dp au repos ══════════]  ← fine et elegante
[████████████ 8dp au focus ████████████]  ← epaissie pour feedback tactile
```

Animation via `animateDpAsState` avec `tween(DURATION_FAST = 150ms)`. Le focus est detecte via `onFocusChanged { hasFocus }` sur le composable Seekbar.

### 2.4 Position + temps restant

| Fichier | Modification |
|---------|-------------|
| `VideoPlayerControls.kt` | Split `PositionText` en deux : position gauche + restant droite |

**Avant** :
```
                              00:30 / 01:30:00
```

**Apres** :
```
00:30:00                               -01:00:00
```

- Position actuelle a gauche (`onSurface` color, `labelMedium` typo)
- Temps restant a droite avec prefix `-` (`onSurfaceVariant` color, plus discret)
- Les deux utilisent les tokens DS au lieu de `Color.White`

### 2.5 Header : metadonnees techniques

| Fichier | Modification |
|---------|-------------|
| `VideoPlayerHeader.kt` | Refonte complete : titre + sous-titre + badges techniques |

**Avant** :
```
Episode Title
Series Name                          [Clock]
```

**Apres** :
```
Episode Title                    [1080p] [HEVC] [HDR10] [5.1]
Series Name · 2024 · 1h30                        [Clock]
```

#### Sous-titre enrichi
Combine `seriesName`, `productionYear` et `runTimeTicks` formates, separes par ` · `.

#### Badges techniques
Extraits de deux sources :
1. **Resolution en temps reel** : `playbackManager.state.videoSize` (prioritaire, resolution reellement decodee)
2. **Fallback metadata** : `item.mediaSources[0].mediaStreams` (resolution source si videoSize pas encore dispo)
3. **Codec video** : H.264, HEVC, AV1, VP9
4. **HDR** : DV, HDR10+, HDR10, HDR (via `videoRangeType`)
5. **Audio** : 5.1, 7.1, Stereo (via `channels`)

Chaque badge est un `MediaBadgeChip` : texte `labelSmall` sur fond `surface.copy(alpha = 0.6f)` avec shape `extraSmall`.

### 2.6 Gradients DS

| Fichier | Modification |
|---------|-------------|
| `PlayerOverlayLayout.kt` | `Color.Black.copy(0.8f)` → `background.copy(0.85f)` + gradient 3 stops |

**Avant** : Gradient 2 stops `Black(0.8) → Transparent`
**Apres** : Gradient 3 stops `background(0.85) → background(0.4) → Transparent`

Le gradient 3 stops est plus doux et naturel, avec une transition moins brutale. Utilise `JellyfinTheme.colorScheme.background` (`#080808`) au lieu de `Color.Black` pur pour coherence DS.

### 2.7 Couleurs DS : suppression Color.White

| Fichier | Avant | Apres |
|---------|-------|-------|
| `VideoPlayerHeader.kt` | `Color.White` | `colorScheme.onSurface` / `onSurfaceVariant` |
| `VideoPlayerControls.kt` | `Color.White` | `colorScheme.onSurface` / `onSurfaceVariant` |
| `PhotoPlayerHeader.kt` | `Color.White` | `colorScheme.onSurface` / `onSurfaceVariant` |
| `VideoPlayerScreen.kt` | `Color.Black` | `colorScheme.background` |

**Justification** : Regle DS #7 « No pure white for text » et #8 « No pure black for backgrounds ».

---

## 3. Layout ASCII — Avant / Apres

### AVANT

```
┌────────────────────────────────────────────────────┐
│ Episode Title                                      │
│ Series Name                              [Clock]   │
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ gradient ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│                                                    │
│                  (video)                           │
│                                                    │
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ gradient ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│ [▶] [◀◀] [▶▶]                            [···]    │
│ [========== seekbar 4dp fixe ==============]       │
│                                   00:30 / 01:30:00 │
└────────────────────────────────────────────────────┘
```

### APRES

```
┌────────────────────────────────────────────────────┐
│ Episode Title              [1080p] [HEVC] [HDR10]  │
│ Series Name · 2024 · 1h30                 [Clock]  │
│ ░▒▓▓▓▓▓▓▓▓▓ gradient 3 stops doux ▓▓▓▓▓▒░        │
│                                                    │
│                  (video)                           │
│                                                    │
│ ░▒▓▓▓▓▓▓▓▓▓ gradient 3 stops doux ▓▓▓▓▓▒░        │
│ [▶] [◀◀] [▶▶]                            [···]    │
│ [══════ seekbar 3dp→8dp anime ══════════]          │
│ 00:30:00                              -01:00:00    │
└────────────────────────────────────────────────────┘

Overlay masque :
  OK → ▶/⏸ play/pause + toast central
  ← → ◀◀ seek rewind + toast
  → → ▶▶ seek forward + toast
  ↑↓/autre → affiche overlay
```

---

## 4. Fichiers modifies

| Fichier | Lignes changees | Nature |
|---------|----------------|--------|
| `ui/player/base/PlayerOverlayLayout.kt` | ~30 | Timeout 3s, callbacks D-pad, gradients DS |
| `ui/player/video/VideoPlayerOverlay.kt` | ~25 | Callbacks play/pause, seek + toast |
| `ui/player/video/VideoPlayerControls.kt` | ~60 | Layout, seekbar animee, position split |
| `ui/player/video/VideoPlayerHeader.kt` | ~100 | Metadonnees, badges, sous-titre |
| `ui/player/video/VideoPlayerScreen.kt` | 3 | Color.Black → DS background |
| `ui/player/photo/PhotoPlayerHeader.kt` | 4 | Color.White → DS tokens |
| `ui/player/base/PlayerHeader.kt` | 1 | Espacement 12dp → 16dp |

---

## 5. Choix de design justifies

### Pourquoi 3 secondes d'auto-hide ?
Les players de reference (Netflix, YouTube TV, Plex, Apple TV) utilisent 3-4s. 5s est trop long et gene la visibilite du contenu. 3s est le sweet spot : assez pour lire les controles, pas assez pour gener.

### Pourquoi le D-pad agit directement sans overlay ?
Sur une telecommande TV, il n'y a pas de curseur. L'utilisateur s'attend a ce que OK = play/pause immediatement, pas a devoir naviguer dans un menu. C'est le comportement standard de tous les players TV.

### Pourquoi le seekbar change de taille ?
Une barre fine (3dp) est esthetique au repos mais difficile a voir quand l'utilisateur navigue. L'epaississement a 8dp au focus donne un feedback visuel clair sans encombrer l'ecran au repos.

### Pourquoi des badges techniques discrets ?
Les utilisateurs avances veulent connaitre la resolution et le codec en cours de lecture. Les badges `labelSmall` sur fond semi-transparent sont visibles mais ne distraient pas. Ils sont dans le header (zone haute), loin de la zone de lecture.

### Pourquoi le gradient 3 stops ?
Un gradient 2 stops (opaque → transparent) cree une transition trop brutale. Le 3 stops (0.85 → 0.4 → 0) produit un degrade plus naturel qui fond mieux avec la video.

---

## 6. Ameliorations futures (non implementees)

| Feature | Raison du report | Complexite |
|---------|-----------------|------------|
| Long press OK → menu options (audio, sous-titres, qualite) | Necessite un nouveau dialog/popover complet | Haute |
| D-pad ↑/↓ → volume avec barre laterale | Necessite integration AudioManager + UI barre verticale | Moyenne |
| Indicateur "+10s"/"-10s" textuel au seek | Le toast avec icone suffit, le texte ajouterait de la complexite | Faible |
| Buffering : animation discrète dediee | Le systeme de toast existant peut etre etendu | Moyenne |
| Miniature de preview au survol seekbar | Necessite generation de thumbnails cote serveur (trickplay) | Haute |
| Ecran d'erreur avec retry elegant | PlayState.ERROR existe mais pas de UI dediee | Moyenne |

---

## 7. Metriques

| Metrique | Avant | Apres |
|----------|-------|-------|
| Auto-hide timeout | 5s | 3s |
| Color.White dans le player | 5 | 0 |
| Color.Black dans le player | 2 | 0 |
| Seekbar hauteur fixe | 4dp fixe | 3dp→8dp anime |
| Metadonnees dans le header | Titre + serie | Titre + serie + annee + duree + badges |
| D-pad direct sans overlay | Non | Oui (OK, ←, →) |
| Position / temps restant | Position seule (droite) | Position (gauche) + restant (droite) |
| Gradient stops | 2 (brutal) | 3 (doux) |
| Compilation | OK | OK |
