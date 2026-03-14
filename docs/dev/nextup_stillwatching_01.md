# Next Up & Still Watching — Redesign VegafoX

> Date : 2026-03-12

---

## Fichiers modifiés

| Fichier | Action | LOC |
|---------|--------|-----|
| `ui/base/button/ProgressButtonBase.kt` | Ajout paramètre `progressColor` | +3 |
| `ui/base/button/Button.kt` | Propagation `progressColor` dans `ProgressButton` | +2 |
| `ui/playback/nextup/NextUpFragment.kt` | Refonte complète VegafoX | ~270 |
| `ui/playback/stillwatching/StillWatchingFragment.kt` | Refonte complète VegafoX | ~270 |
| `ui/playback/overlay/SkipOverlayView.kt` | **Non modifié** (déjà VegafoX) | — |

---

## NextUpFragment.kt — Avant / Après

### Fond

| Avant | Après |
|-------|-------|
| `AppBackground()` seul (blur plein écran) | `Color.Black` base + `AppBackground()` alpha 0.40 + gradient bas `BackgroundDeep` 95% sur 50% hauteur |

### Thumbnail

| Avant | Après |
|-------|-------|
| `clip(JellyfinTheme.shapes.extraSmall)` | `shadow(16.dp)` + `clip(RoundedCornerShape(12.dp))` + `border(1.dp, White 8%)` |
| Hauteur 145dp | Hauteur 160dp |

### Texte

| Élément | Avant | Après |
|---------|-------|-------|
| Label | "Next up" via `headlineLarge` | Uppercase, 12sp Bold, `OrangePrimary`, letterSpacing 2sp |
| Titre | `bodyLarge`, 1 ligne | `BebasNeue` 40sp, `TextPrimary`, letterSpacing 2sp, max 2 lignes |
| Sous-titre | Absent | Série · S01E05 — 14sp `TextSecondary` |
| Durée | Absente | 13sp `TextHint` (format "1h30" ou "45 min") |

### Boutons

| Bouton | Avant | Après |
|--------|-------|-------|
| Cancel | `Button` design system (Jellyfin) | `VegafoXButton` Ghost compact |
| Watch Now | `ProgressButton` + `Button` Jellyfin | `ProgressButton` avec `containerColor=OrangePrimary`, `progressColor=OrangePrimary@30%` |

---

## StillWatchingFragment.kt — Avant / Après

### Fond

| Avant | Après |
|-------|-------|
| `AppBackground()` seul | `Color.Black` + `AppBackground()` alpha 0.40 + gradient bas `BackgroundDeep` 95% |

### Texte

| Élément | Avant | Après |
|---------|-------|-------|
| Label | "Next up" (incorrect — affichait "Next up" au lieu de "Still watching") + `headlineLarge` (droite) | "ARE YOU STILL WATCHING?" uppercase, 12sp Bold, `OrangePrimary`, letterSpacing 2sp |
| Titre | `bodyLarge` 1 ligne (droite) | `BebasNeue` 40sp `TextPrimary` max 2 lignes |
| Sous-titre | Absent | Série · S01E05 — 14sp `TextSecondary` |

### Boutons

| Bouton | Avant | Après |
|--------|-------|-------|
| Exit | `ProgressButton` Jellyfin (timer 10s, auto-exit) | `ProgressButton` Ghost-like (`containerColor=Transparent`), `progressColor=red(255,80,80)@40%` |
| Continue Watching | `Button` Jellyfin | `VegafoXButton` Primary compact |

### Bug corrigé

Le StillWatchingOverlay affichait `lbl_next_up` ("Next up") comme label au lieu de `still_watching_label` pour la colonne gauche. L'ancien code avait deux labels : "Next up" (gauche, titre section) et "Are you still watching?" (droite). Le nouveau design unifie avec un seul label "Are you still watching?" en orange.

---

## SkipOverlayView.kt — Statut

**Déjà conforme VegafoX.** Vérifié :

| Élément | Style | Statut |
|---------|-------|--------|
| Fond | `OrangePrimary.copy(alpha = 0.15f)` | ✅ |
| Bordure | `1.5.dp`, `OrangePrimary` | ✅ |
| Texte | `OrangePrimary`, 14sp Bold | ✅ |
| Icône | `VegafoXIcons.SkipNext`, tint `OrangePrimary` | ✅ |
| Focus | N/A (overlay non-focusable, action D-pad) | ✅ |

Aucune modification appliquée.

---

## ProgressButtonBase — Amélioration

Ajout d'un paramètre optionnel `progressColor: Color?` permettant de customiser la couleur de la barre de progression sans modifier la ressource globale `button_default_progress_background`. Si `null`, la couleur par défaut est conservée.

---

## Build

| Variante | Résultat |
|----------|----------|
| `assembleGithubDebug` | ✅ BUILD SUCCESSFUL |
| `assembleGithubRelease` | ✅ BUILD SUCCESSFUL |

---

## Installation & Screenshots

**En attente** — AM9 Pro (192.168.1.152) non accessible (No route to host).

- [ ] `adb install` debug
- [ ] Lancer épisode de série → attendre fin → Next Up
- [ ] `adb shell screencap -p /sdcard/nextup_v2.png && adb pull /sdcard/nextup_v2.png docs/screenshots/`
- [ ] Déclencher Still Watching (inactivité)
- [ ] `adb shell screencap -p /sdcard/stillwatching_v2.png && adb pull /sdcard/stillwatching_v2.png docs/screenshots/`
