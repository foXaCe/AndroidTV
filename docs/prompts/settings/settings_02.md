# Settings VegafoX — Phase 2 : Sous-ecrans complexes + batch headers

**Date** : 2026-03-12
**Branche** : main

---

## Resume

Migration des sous-ecrans complexes (Jellyseerr, Subtitles, PlaybackAdvanced, Home, JellyseerrRows, Licenses) et batch de sous-ecrans simples vers le design system VegafoX. Focus sur les headers BebasNeue, les AlertDialog VegafoX, les tokens couleur, et la coherence du design system.

---

## Fichiers modifies par groupe

### FICHIER 1 — SettingsHomeScreen.kt

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/home/SettingsHomeScreen.kt` | Header "Accueil" 22sp BebasNeue TextPrimary, arrow icons VegafoXColors.TextPrimary/TextHint |

### FICHIER 2 — SettingsSubtitlesScreen.kt

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt` | Header "Sous-titres" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/customization/subtitle/composable/SubtitleStylePreview.kt` | Fond preview VegafoXColors.BackgroundDeep (remplace Tokens.Color.colorBluegrey800) |

**Note RangeControl** : Deja theme VegafoX via le bridge colorScheme — fill=OrangePrimary, knob=TextPrimary. Aucune modification necessaire.

**Note ColorSwatch** : Composant neutre (affiche la couleur selectionnee), pas de theming necessaire.

### FICHIER 3 — SettingsPlaybackAdvancedScreen.kt

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/playback/SettingsPlaybackAdvancedScreen.kt` | Header "Lecture avancee" 22sp BebasNeue TextPrimary |
| `ui/settings/composable/SettingsAsyncActionListButton.kt` | Icone success → VegafoXColors.OrangePrimary, icone error → VegafoXColors.Error (remplace Tokens.Color.colorLime300/colorRed300) |

**Note RangeControl x4** : Skip forward, unpause rewind, video start delay — tous deja themes via bridge. Aucune modification necessaire.

### FICHIER 4 — SettingsJellyseerrScreen.kt (L — le plus complexe)

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/vegafox/SettingsJellyseerrScreen.kt` | Migration complete des AlertDialog et boutons |

**Details AlertDialog migration :**

| Dialog | Avant | Apres |
|--------|-------|-------|
| Logout Confirmation | TvPrimaryButton + TvSecondaryButton, AlertDialog default | VegafoXAlertDialog helper (Surface, TextPrimary 18sp Bold, TextSecondary 15sp, VegafoXButton Primary/Ghost) |
| VegafoX Disconnect | TvPrimaryButton + TvSecondaryButton | VegafoXAlertDialog helper |
| Server URL | TvPrimaryButton + TvSecondaryButton + OutlinedTextField default | VegafoXButton + OutlinedTextField avec vegafoXTextFieldColors (bordure OrangePrimary focus, curseur OrangePrimary, fond Surface, coins 12dp) |
| Jellyfin Login | TvPrimaryButton + TvSecondaryButton | VegafoXButton + vegafoXTextFieldColors |
| Local Login | TvPrimaryButton + TvSecondaryButton (2 champs) | VegafoXButton + vegafoXTextFieldColors (email + password) |
| API Key Login | TvPrimaryButton + TvSecondaryButton | VegafoXButton + vegafoXTextFieldColors |

**Composants ajoutes :**
- `VegafoXAlertDialog()` — helper prive pour les dialogs simples (titre+texte+confirm+cancel)
- `vegafoXTextFieldColors()` — colors OutlinedTextField VegafoX (bordure OrangePrimary focus, fond Surface, curseur OrangePrimary)

### FICHIER 5 — SettingsJellyseerrRowsScreen.kt

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/vegafox/SettingsJellyseerrRowsScreen.kt` | Header "Rows Jellyseerr" 22sp BebasNeue TextPrimary, arrow icons VegafoXColors.TextPrimary/TextHint |

### FICHIER 6 — Batch sous-ecrans simples (10 ecrans)

| Fichier | Header |
|---------|--------|
| `authentication/SettingsAuthenticationSortByScreen.kt` | "Trier par" 22sp BebasNeue |
| `authentication/SettingsAuthenticationPinCodeScreen.kt` | "Code PIN" 22sp BebasNeue |
| `customization/SettingsCustomizationClockScreen.kt` | "Horloge" 22sp BebasNeue |
| `customization/SettingsCustomizationWatchedIndicatorScreen.kt` | "Contenus vus" 22sp BebasNeue |
| `home/SettingsHomePosterSizeScreen.kt` | "Taille des posters" 22sp BebasNeue |
| `vegafox/SettingsVegafoXHomeRowsImageScreen.kt` | "Images des rows" 22sp BebasNeue |
| `vegafox/SettingsVegafoXParentalControlsScreen.kt` | "Controle parental" 22sp BebasNeue |
| `vegafox/SettingsVegafoXSyncPlayScreen.kt` | "SyncPlay" 22sp BebasNeue |
| `livetv/SettingsLiveTvGuideOptionsScreen.kt` | "Guide TV" 22sp BebasNeue |

### FICHIER 7 — SettingsLicensesScreen.kt

| Fichier | Modification |
|---------|-------------|
| `license/SettingsLicensesScreen.kt` | Header "Licences" 22sp BebasNeue TextPrimary |
| `license/SettingsLicenseScreen.kt` | Header nom bibliotheque 22sp BebasNeue TextPrimary |

---

## Statut AlertDialog migration Jellyseerr

| Composant | Statut |
|-----------|--------|
| `TvPrimaryButton` | **REMPLACE** par `VegafoXButton(variant=Primary, compact=true)` dans les 5 dialogs |
| `TvSecondaryButton` | **REMPLACE** par `VegafoXButton(variant=Ghost, compact=true)` dans les 5 dialogs |
| `AlertDialog containerColor` | **MIGRE** vers `VegafoXColors.Surface` (#141418) |
| `AlertDialog title style` | **MIGRE** vers TextPrimary 18sp Bold |
| `AlertDialog text style` | **MIGRE** vers TextSecondary 15sp |
| `OutlinedTextField colors` | **MIGRE** vers bordure OrangePrimary focus, curseur OrangePrimary, fond Surface, coins 12dp |
| Import `TvPrimaryButton` | **SUPPRIME** du fichier |
| Import `TvSecondaryButton` | **SUPPRIME** du fichier |

---

## Statut RangeControl migration

| Ecran | Nb RangeControl | Migration necessaire |
|-------|----------------|---------------------|
| SettingsSubtitlesScreen | 3 (size, weight, position) | **NON** — deja theme via bridge colorScheme (fill=OrangePrimary, knob=TextPrimary) |
| SettingsPlaybackAdvancedScreen | 4 (skip forward, unpause rewind, video start delay, resume subtract) | **NON** — idem bridge |

**Conclusion** : Les RangeControl utilisent `RangeControlDefaults.colors()` qui lit `JellyfinTheme.colorScheme.rangeControlFill` = `VegafoXColors.OrangePrimary`. La migration est deja faite au niveau du bridge Phase 1.

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Installe sur Ugoos AM9 Pro (192.168.1.152)

---

## Screenshots

- `docs/screenshots/settings_home_v2.png` — Home Settings avec header BebasNeue
- `docs/screenshots/settings_jellyseerr_v2.png` — Jellyseerr Settings avec header BebasNeue
- `docs/screenshots/settings_subtitles_v2.png` — Subtitles Settings avec header BebasNeue

---

## Impact total Phase 2

| Metrique | Valeur |
|----------|--------|
| Fichiers modifies | **17** |
| Sous-ecrans avec header BebasNeue | **+19** (total avec Phase 1 : ~29) |
| AlertDialog migres VegafoX | **5** (tous ceux du JellyseerrScreen) |
| Tokens Jellyfin supprimes | 2 (Tokens.Color.colorLime300, Tokens.Color.colorBluegrey800) |
| TvPrimaryButton/TvSecondaryButton supprimes | **10 usages** (5 dialogs x 2 boutons) |
| RangeControl a migrer | **0** (deja fait via bridge Phase 1) |
