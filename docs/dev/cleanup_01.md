# Cleanup 01 — Suppression des orphelins post-migration

**Date** : 2026-03-12
**Branche** : main

---

## Etape 1 — Inventaire complet

### Grep LiveTvBrowseViewModel
- `AppModule.kt:223` — declaration Koin
- `LiveTvBrowseViewModel.kt` — fichier lui-meme
- **ORPHELIN** — plus instancie par aucun fragment

### Grep playbackRewriteVideoEnabled
- **0 resultats** — deja supprime dans player_03

### Grep TvPrimaryButton / TvSecondaryButton
- `TvButton.kt:51,75` — definis mais jamais appeles
- `TvIconButton` (meme fichier) est encore utilise par `AudioNowPlayingScreen.kt`
- **ORPHELINS PARTIELS** — fonctions inutilisees dans un fichier encore actif

### Grep ds_primary
- `FriendlyDateButton.kt:34` (View legacy) — utilise
- `styles.xml:52` — utilise
- **ENCORE UTILISE** — conserve

### Grep timeline_bg / guide_*_bg
- `timeline_bg` : uniquement `colors.xml:145` — **ORPHELIN**
- `guide_movie_bg` : uniquement `colors.xml:147` — **ORPHELIN**
- `guide_sports_bg` : uniquement `colors.xml:148` — **ORPHELIN**
- `guide_news_bg` : uniquement `colors.xml:149` — **ORPHELIN**
- `guide_kids_bg` : uniquement `colors.xml:150` — **ORPHELIN**
- `channel_scroller_bg` : uniquement `colors.xml:143` — **ORPHELIN**
- `program_scroller_bg` : uniquement `colors.xml:144` — **ORPHELIN**

### Grep dark_green_gradient
- `dark_green_gradient_start/end` : uniquement `colors.xml` + `dark_green_gradient.xml`
- `dark_green_gradient.xml` : 0 reference en kt ou xml
- **ORPHELINS** — drawable + couleurs

### Grep green_gradient / red_gradient
- `green_gradient_start/end` : uniquement `colors.xml` + `green_gradient.xml`
- `red_gradient_start/end` : uniquement `colors.xml` + `red_gradient.xml`
- Les deux drawables : 0 reference en kt ou xml
- **ORPHELINS** — drawables + couleurs

### Grep black_transparent
- `black_transparent` utilise par `expanded_text.xml` → `ExpandableTextView.kt`
- `black_transparent_dark` utilise par `popup_expandable_text_view.xml`
- **ENCORE UTILISE** — conserve

### Grep DetailActionButton / QualityButton
- `DetailActionButton` : 6 fichiers dans `itemdetail/v2/` — **ENCORE UTILISE**
- `QualityButton` : **0 resultats** — pas de fichier separe

### Grep DialogActionButton
- `SeasonSelectionDialog.kt` : `internal` composable utilise localement — **UTILISE**

### Grep colorAccent / getThemeColor
- `getThemeColor` : defini dans `Utils.kt:57`, 0 appelants — **ORPHELIN**
- `colorAccent` : 0 resultats — deja nettoye

### Grep JellyfinTheme.colorScheme dans fichiers migres
| Fichier | Occurrences |
|---------|------------|
| `ui/livetv/compose/FriendlyDateButton.kt` | 1 |
| `ui/jellyseerr/compose/JellyseerrCards.kt` | 7 |
| `ui/jellyseerr/compose/JellyseerrFactsSection.kt` | 3 |
| `ui/jellyseerr/compose/JellyseerrPersonDetailsScreen.kt` | 5 |
| `ui/jellyseerr/compose/JellyseerrBrowseByScreen.kt` | 5 |
| `ui/jellyseerr/compose/JellyseerrMediaDetailsScreen.kt` | 11 |
| `ui/playback/overlay/PlayerPopupView.kt` | 8 |
| `ui/playback/overlay/compose/PlayerDialogDefaults.kt` | 11 |
| **Total** | **51 references** |

### Grep TODO / FIXME / HACK (19 resultats)
| TODO | Fichier | Decision |
|------|---------|----------|
| `FIXME: Make getLibraryPreferences suspended` | PreferencesRepository.kt | Travail futur → CONSERVE |
| `TODO("...does not support migrations")` | DisplayPreferencesStore.kt | Exception volontaire → CONSERVE |
| `TODO Add SeriesTimerInfoDto` | JellyfinImage.kt | Depend fix API SDK → CONSERVE |
| `TODO: Add suitable space token` (x2) | ListMessage/ListItemContent.kt | Design tokens futurs → CONSERVE |
| `TODO: ask-to-skip UI removed` | PlaybackControllerHelper.kt | Handler vide documente → CONSERVE |
| `TODO, implement speed change handling` | PlaybackController.kt | Feature manquante → CONSERVE |
| `TODO: add to list` | HomeHeroBackdrop.kt | Feature future → CONSERVE |
| `TODO: Appears to always be null?` | BaseItemInfoRow.kt | Bug API potentiel → CONSERVE |
| `TODO only pass item id` (x9) | Destinations.kt | Refactoring futur → CONSERVE |

---

## Etape 2 — Suppressions effectuees

### GROUPE A — ViewModel orphelin LiveTV

| Action | Fichier | LOC |
|--------|---------|-----|
| Supprime | `ui/browsing/v2/LiveTvBrowseViewModel.kt` | 218 |
| Modifie | `di/AppModule.kt` (ligne 223 retiree) | -1 |

### GROUPE B — Couleurs et drawables XML obsoletes

**Couleurs supprimees de `colors.xml` :**
- `timeline_bg`
- `channel_scroller_bg`
- `program_scroller_bg`
- `guide_movie_bg`
- `guide_sports_bg`
- `guide_news_bg`
- `guide_kids_bg`
- `dark_green_gradient_start`
- `dark_green_gradient_end`
- `green_gradient_start`
- `green_gradient_end`
- `red_gradient_start`
- `red_gradient_end`

**Drawables supprimes :**
- `drawable/dark_green_gradient.xml`
- `drawable/green_gradient.xml`
- `drawable/red_gradient.xml`

**Conserve (encore utilise) :**
- `ds_primary` — `FriendlyDateButton.kt` + `styles.xml`
- `black_transparent*` — `ExpandableTextView.kt`

### GROUPE C — Composants remplaces

| Action | Fichier | Details |
|--------|---------|---------|
| Supprime | `TvPrimaryButton()` dans `TvButton.kt` | Composable jamais appele |
| Supprime | `TvSecondaryButton()` dans `TvButton.kt` | Composable jamais appele |
| Supprime | `TvButtonDefaults` object dans `TvButton.kt` | Utilise uniquement par les 2 ci-dessus |
| Supprime | `getThemeColor()` dans `Utils.kt` | Methode jamais appelee |
| Conserve | `TvIconButton()` dans `TvButton.kt` | Utilise par `AudioNowPlayingScreen.kt` |
| Conserve | `DialogActionButton` dans `SeasonSelectionDialog.kt` | Composable `internal` utilise localement |
| Conserve | `DetailActionButton` dans `ItemDetailsComponents.kt` | 6 fichiers l'utilisent |

### GROUPE D — TODO / FIXME

**0 supprime** — tous les 19 TODO/FIXME sont du travail futur legitime.

### GROUPE E — JellyfinTheme.colorScheme → VegafoXColors

| Fichier | Remplacements | Mapping principal |
|---------|---------------|-------------------|
| `livetv/compose/FriendlyDateButton.kt` | 1 | `onButton` → `TextPrimary` |
| `jellyseerr/compose/JellyseerrCards.kt` | 7 | `surfaceDim`, `textPrimary/Secondary`, `background` |
| `jellyseerr/compose/JellyseerrFactsSection.kt` | 3 | `outlineVariant`, `textPrimary/Secondary` |
| `jellyseerr/compose/JellyseerrPersonDetailsScreen.kt` | 5 | `surface`, `textPrimary`, `info`, `surfaceBright` |
| `jellyseerr/compose/JellyseerrBrowseByScreen.kt` | 5 | `textPrimary/Secondary` |
| `jellyseerr/compose/JellyseerrMediaDetailsScreen.kt` | 11 | `surface`, `textPrimary/Secondary`, `surfaceContainer/Bright` |
| `playback/overlay/PlayerPopupView.kt` | 8 | `surface`, `onSurface`, `onSurfaceVariant` |
| `playback/overlay/compose/PlayerDialogDefaults.kt` | 11 | `dialogSurface`, `outlineVariant`, `textPrimary/Secondary`, `primary`, `focusRing`, `surfaceContainer/Bright` |
| **Total** | **51** | |

Import `JellyfinTheme` conserve dans tous les fichiers (encore utilise pour `.typography` et `.shapes`).

---

## Bilan

### LOC supprimees

| Element | LOC |
|---------|-----|
| `LiveTvBrowseViewModel.kt` | 218 |
| `TvPrimaryButton` + `TvSecondaryButton` + `TvButtonDefaults` | 69 |
| `getThemeColor()` | 6 |
| 13 couleurs XML | 13 |
| 3 drawables XML | ~30 |
| Ligne Koin AppModule | 1 |
| **Total** | **~337 LOC** |

### Grep final (zero reference)

```
grep -rn "LiveTvBrowseViewModel|TvPrimaryButton|TvSecondaryButton|
  timeline_bg|dark_green_gradient|guide_movie_bg|guide_sports_bg|
  guide_news_bg|guide_kids_bg|channel_scroller_bg|program_scroller_bg"
  app/src --include="*.kt" --include="*.xml"
→ 0 resultats
```

### Build

| Variante | Statut |
|----------|--------|
| Debug (github) | BUILD SUCCESSFUL |
| Release (github) | BUILD SUCCESSFUL |
| Install AM9 Pro (debug) | Success |
