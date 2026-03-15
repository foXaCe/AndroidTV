# Settings VegafoX — Phase 1 : Design System + Ecrans top-level

**Date** : 2026-03-12
**Branche** : main

---

## Resume

Migration des composants partages et ecrans principaux des Settings vers le design system VegafoX (Dark Premium, Orange #FF6B00, BebasNeue).

---

## Fichiers modifies par groupe

### GROUPE 1 — Composants partages cascade

| Fichier | Modification | Impact cascade |
|---------|-------------|----------------|
| `ui/settings/composable/SettingsLayout.kt` | Fond VegafoXColors.Surface, bordure 1dp 8% blanc | **Cadre de tous les 75 ecrans settings** |
| `ui/settings/composable/SettingsEntry.kt` (OptionListScreen) | RadioButton OrangePrimary, item selectionne texte OrangePrimary + fond OrangeSoft, ListControlDefaults custom | **10 ecrans generiques** (navbar, shuffle, media bar x4, blur x2, seasonal, theme music) |
| `ui/settings/composable/SettingsNumericScreen.kt` | Meme pattern que OptionListScreen | **5 ecrans SyncPlay** |

### GROUPE 2 — Design system list (cascade maximale)

| Fichier | Modification | Impact cascade |
|---------|-------------|----------------|
| `ui/base/form/RadioButton.kt` | containerColor default → VegafoXColors.OrangePrimary | **Tous les RadioButton** |
| `ui/base/form/Checkbox.kt` | containerColor default → VegafoXColors.OrangePrimary | **Tous les Checkbox** |
| `ui/base/colorScheme.kt` | listButtonFocused → Color(0x0FFFFFFF) (6% blanc) | **Tous les ListButton focused** |
| `ui/base/list/ListControl.kt` | Scale 1.02 spring, left border 3dp OrangePrimary au focus, drawWithContent | **Tous les 75 ecrans settings** |
| `ui/base/list/ListSection.kt` | Section divider : TextHint 12sp uppercase letterSpacing 2dp, HorizontalDivider 1dp 6% blanc, padding top 24dp. Conserve style original pour les headers avec overline/caption. | **Toutes les sections divider** |

### GROUPE 3 — Ecrans top-level

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/SettingsMainScreen.kt` | Header VegafoX : Row logo 40dp + "VegafoX" 22sp BebasNeue OrangePrimary + "Parametres" 13sp TextSecondary |
| `ui/settings/screen/playback/SettingsPlaybackScreen.kt` | Header "Lecture" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/customization/SettingsCustomizationScreen.kt` | Header "Personnalisation" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/about/SettingsAboutScreen.kt` | Logo 80dp centre + "VegafoX" 32sp BebasNeue OrangePrimary + version 14sp + "Propulse par Jellyfin" 13sp TextHint |
| `ui/settings/screen/vegafox/SettingsPluginScreen.kt` | Header "Plugin" 22sp BebasNeue TextPrimary |

### GROUPE 4 — Ecrans simples batch

| Fichier | Modification |
|---------|-------------|
| `ui/settings/screen/authentication/SettingsAuthenticationScreen.kt` | Header "Se connecter" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/screensaver/SettingsScreensaverScreen.kt` | Header "Economiseur d'ecran" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/SettingsTelemetryScreen.kt` | Header "Telemetrie" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/SettingsDeveloperScreen.kt` | Header "Developpeur" 22sp BebasNeue TextPrimary |
| `ui/settings/screen/library/SettingsLibrariesScreen.kt` | Header "Bibliotheques" 22sp BebasNeue TextPrimary |

### Strings

| Fichier | Ajout |
|---------|-------|
| `res/values/strings.xml` | `powered_by_jellyfin` = "Powered by Jellyfin" |
| `res/values-fr/strings.xml` | `powered_by_jellyfin` = "Propulse par Jellyfin" |

---

## Effet cascade — Nombre d'ecrans couverts

| Composant migre | Ecrans couverts |
|-----------------|-----------------|
| SettingsLayout (fond + bordure) | **75** (tous) |
| ListControl (focus animation + left border) | **75** (tous) |
| ListSection (section divider style) | **~50** (tous ceux avec sections) |
| RadioButton (OrangePrimary) | **~25** (tous avec RadioButton) |
| Checkbox (OrangePrimary) | **~30** (tous avec Checkbox) |
| OptionListScreen | **10** (ecrans generiques VegafoX) |
| SettingsNumericScreen | **5** (ecrans SyncPlay) |
| Headers BebasNeue individuels | **10** (ecrans modifies manuellement) |

**Total couverture** : ~75/75 ecrans beneficient au moins du fond, focus animation et Checkbox/RadioButton OrangePrimary.

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Installe sur Ugoos AM9 Pro (192.168.1.152)

---

## Screenshots

- `docs/screenshots/settings_main_v2.png` — Ecran principal avec header VegafoX
- `docs/screenshots/settings_playback_v2.png` — Ecran Lecture avec header BebasNeue
- `docs/screenshots/settings_about_v2.png` — Ecran About avec logo centre + branding

---

## Notes

1. **Icone renard PNG 1024px** : trop grande dans les ListButton leading icon (Plugin, About version). Un asset 48dp/96px serait ideal.
2. **ListSection heuristique** : si overlineContent == null && captionContent == null → style section divider (petit, hint, uppercase). Sinon → style header original (grand, primary). Fonctionne pour tous les usages actuels.
3. **Checkbox/RadioButton** : le defaut global est maintenant OrangePrimary. Les overrides explicites dans OptionListScreen/SettingsNumericScreen restent par securite.
