# Cleanup 09 — Thèmes orphelins, duplications, magic numbers

**Date** : 2026-03-13
**Scope** : Thèmes Emerald/MutedPurple, code dupliqué (getBackdropUrl, dpToPx, copyWithTimerId), magic numbers → VegafoXDimensions.kt
**Basé sur** : cleanup_08.md (audit)

---

## GROUPE 1 — Thèmes Emerald + MutedPurple (supprimés)

### Fichiers supprimés

| Fichier | Lignes |
|---------|--------|
| `res/values/theme_emerald.xml` | 47 |
| `res/values/theme_mutedpurple.xml` | 31 |

### Couleurs supprimées (en cascade avec les fichiers)

| Couleur | Définie dans | Autres réfs |
|---------|-------------|-------------|
| `theme_emerald_light` | theme_emerald.xml | 0 |
| `theme_emerald_dark` | theme_emerald.xml | 0 |
| `theme_emerald_black` | theme_emerald.xml | 0 |
| `theme_muted_purple_accent` | theme_mutedpurple.xml | 0 |
| `light_grey_transparent` | theme_mutedpurple.xml | 0 |

### Strings de traduction supprimées

| String | Fichiers affectés |
|--------|------------------|
| `pref_theme_emerald` | 47 fichiers values-*/strings.xml |
| `pref_theme_muted_purple` | 0 (n'existait pas dans les traductions) |

**Vérification** : `grep -rn` → 0 référence résiduelle

---

## GROUPE 2 — Duplication getBackdropUrl (résolue)

| Avant | Après |
|-------|-------|
| `HomeHeroBackdrop.kt:459` — version privée (14 lignes) | Supprimée |
| `DetailUtils.kt:14` — version publique | Réutilisée via import |

**Changements** :
- Import ajouté : `import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl`
- Imports supprimés : `getUrl`, `itemImages`, `parentImages`, `ImageType` (devenus inutiles)
- Fonction privée `getBackdropUrl()` supprimée (14 lignes)

---

## GROUPE 3 — withServerId (SKIP)

Fonction `withServerId()` introuvable dans le codebase — déjà nettoyée dans un cleanup précédent.

---

## GROUPE 4 — Magic numbers → VegafoXDimensions.kt

### Fichier créé : `ui/base/theme/VegafoXDimensions.kt`

```kotlin
object DialogDimensions {
    val maxListHeight = 400.dp
}

object ButtonDimensions {
    val minWidth = 200.dp
    val minWidthCompact = 120.dp
    val height = 52.dp
    val heightCompact = 40.dp
}

object DreamDimensions {
    val logoWidth = 400.dp
    val logoHeight = 200.dp
    val albumCoverSize = 128.dp
    val clockWidth = 150.dp
    val carouselMaxHeight = 75.dp
    val fadingEdgeVertical = 250.dp
}

object LiveTvDimensions {
    val guideHeaderHeight = 120.dp
}
```

### 4a — VideoPlayerDialogs.kt

5 occurrences de `400.dp` → `DialogDimensions.maxListHeight`
- AudioTrackDialog, SubtitleTrackDialog, ChaptersDialog, QualityDialog, SpeedDialog

### 4b — VegafoXButton.kt

| Avant | Après |
|-------|-------|
| `120.dp` (compact minWidth) | `ButtonDimensions.minWidthCompact` |
| `200.dp` (normal minWidth) | `ButtonDimensions.minWidth` |
| `40.dp` (compact height) | `ButtonDimensions.heightCompact` |
| `52.dp` (normal height) | `ButtonDimensions.height` |

### 4c — DreamContent (4 fichiers)

| Fichier | Avant | Après |
|---------|-------|-------|
| DreamContentLogo.kt | `400.dp`, `200.dp` | `DreamDimensions.logoWidth`, `.logoHeight` |
| DreamContentNowPlaying.kt | `128.dp`, `250.dp` | `DreamDimensions.albumCoverSize`, `.fadingEdgeVertical` |
| DreamHeader.kt | `150.dp` | `DreamDimensions.clockWidth` |
| DreamContentLibraryShowcase.kt | `75.dp` | `DreamDimensions.carouselMaxHeight` |

### 4d — LiveTvGuideScreen.kt

`120.dp` → `LiveTvDimensions.guideHeaderHeight`

---

## GROUPE 5 — dpToPx doublon (résolu)

| Avant | Après |
|-------|-------|
| `SimpleInfoRowView.kt:285` — `private fun dpToPx()` (3 lignes) | Supprimée |
| 6 appels `dpToPx(X)` | Remplacés par `X.dp(context)` via `NumberExtensions.kt` |

---

## GROUPE 6 — copyWithTimerId collision (résolu)

| Fichier | Avant | Après |
|---------|-------|-------|
| `JavaCompat.kt:30` | `copyWithTimerId(seriesTimerId)` | `copyWithSeriesTimerId(seriesTimerId)` |
| `LiveTvRecordingApi.kt:15` | `copyWithTimerId(timerId)` — inchangé | Inchangé (pas de collision) |

**Contexte** : La version JavaCompat n'avait 0 appelant (jamais importée). Le renommage clarifie la distinction :
- `copyWithTimerId()` → met à jour `timerId` (LiveTvRecordingApi)
- `copyWithSeriesTimerId()` → met à jour `seriesTimerId` (JavaCompat + LiveTvRecordingApi)

---

## LOC ce round

| Action | LOC |
|--------|-----|
| theme_emerald.xml supprimé | -47 |
| theme_mutedpurple.xml supprimé | -31 |
| Strings traduction (47 fichiers × 1 ligne) | -47 |
| getBackdropUrl privée supprimée | -14 |
| Imports nettoyés (HomeHeroBackdrop) | -3 |
| dpToPx privée supprimée | -3 |
| VegafoXDimensions.kt créé | +28 |
| Imports ajoutés (7 fichiers) | +7 |
| **Net** | **~-110** |

## LOC total tous cleanups

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3,124 |
| 05 | ~1,581 + 6,751 traductions |
| 06 | ~369 |
| 07 | ~361 + 900 KB images |
| 08 | ~95 |
| **09** | **~110 + 47 traductions** |
| **Total** | **~6,215 LOC + 6,798 traductions + 900 KB images** |

## Build

- Debug (github) : BUILD SUCCESSFUL
- Release (github) : BUILD SUCCESSFUL
- Installé sur AM9 Pro : Success
