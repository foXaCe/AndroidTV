# Settings Architecture Audit

**Date** : 2026-03-09
**Scope** : `ui/settings/` + `preference/`

---

## Résumé exécutif

| Métrique | Valeur |
|---|---|
| Fichiers Settings UI (`.kt`) | **91** |
| LOC Settings UI | **8 604** |
| Fichiers Preference (app) | **25** (stores + constants) |
| LOC Preference (app) | **1 788** |
| LOC TOTAL Settings | **~10 400** |
| Écrans routés | **63** (dans `routes.kt`) |
| Routes déclarées | **55** constantes dans `Routes` |
| XML Preference files | **0** (aucun) |
| PreferenceFragmentCompat | **0** usage |
| Architecture | **100% Compose** |

---

## 1. Inventaire des fichiers Settings UI

### 1.1 Infrastructure (12 fichiers, 1 367 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `routes.kt` | 483 | Routes + map route→composable (63 entrées) |
| `screen/UpdateDialogs.kt` | 391 | Dialogs de mise à jour OTA |
| `composable/SettingsColumn.kt` | 98 | LazyColumn wrapper avec gestion save/discard |
| `composable/SettingsAsyncActionListButton.kt` | 98 | Bouton async avec états loading/error |
| `composable/SettingsNumericScreen.kt` | 61 | Écran générique slider numérique |
| `composable/SettingsRouterContent.kt` | 52 | Navigation via Router |
| `composable/SettingsDialog.kt` | 46 | Modal dialog container |
| `composable/SettingsLayout.kt` | 31 | Box layout wrapper |
| `compat/rememberPreference.kt` | 49 | Bridge PreferenceStore ↔ Compose State |
| `compat/MainActivitySettings.kt` | ~20 | Entry point (JellyfinTheme + Router) |
| `compat/SettingsViewModel.kt` | ~20 | Visibility state ViewModel |
| `util/ListColorChannelRangeControl.kt` | 100 | Color picker RGB |
| `util/copyAction.kt` | 30 | Clipboard utility |

### 1.2 Écrans — par catégorie

#### Root (3 fichiers, 375 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsMainScreen.kt` | 202 | Menu principal Settings |
| `SettingsDeveloperScreen.kt` | 118 | Options développeur |
| `SettingsTelemetryScreen.kt` | 55 | Télémétrie on/off |

#### Authentication (6 fichiers, ~600 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsAuthenticationScreen.kt` | 140 | Liste serveurs/utilisateurs |
| `SettingsAuthenticationServerScreen.kt` | 121 | Gestion d'un serveur |
| `SettingsAuthenticationServerUserScreen.kt` | ~80 | Settings par utilisateur |
| `SettingsAuthenticationPinCodeScreen.kt` | 152 | Saisie code PIN |
| `SettingsAuthenticationAutoSignInScreen.kt` | ~50 | Auto sign-in toggle |
| `SettingsAuthenticationSortByScreen.kt` | ~50 | Tri des serveurs |

#### Customization (6 fichiers + 8 subtitle, ~850 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsCustomizationScreen.kt` | 270 | Menu personnalisation |
| `SettingsCustomizationThemeScreen.kt` | ~60 | Choix thème |
| `SettingsCustomizationClockScreen.kt` | ~50 | Horloge display |
| `SettingsClockBehaviorScreen.kt` | ~50 | Comportement horloge |
| `SettingsCustomizationWatchedIndicatorScreen.kt` | ~50 | Indicateurs vus |
| `SettingsWatchedIndicatorBehaviorScreen.kt` | ~50 | Style indicateurs |
| **subtitle/** | | |
| `SettingsSubtitlesScreen.kt` | 201 | Menu sous-titres |
| `SettingsSubtitlesTextColorScreen.kt` | ~60 | Couleur texte |
| `SettingsSubtitlesBackgroundColorScreen.kt` | ~60 | Couleur fond |
| `SettingsSubtitleTextStrokeColorScreen.kt` | ~60 | Couleur contour |
| `SubtitleColorPresetsControl.kt` | ~40 | Presets couleur |
| `SubtitleStylePreview.kt` | ~40 | Preview |
| `presets.kt` | ~30 | Définitions presets |

#### Screensaver (6 fichiers, ~350 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsScreensaverScreen.kt` | 117 | Menu screensaver |
| `SettingsScreensaverModeScreen.kt` | ~50 | Type screensaver |
| `SettingsScreensaverTimeoutScreen.kt` | ~50 | Délai inactivité |
| `SettingsScreensaverAgeRatingScreen.kt` | ~50 | Filtrage contenu |
| `SettingsScreensaverDimmingScreen.kt` | ~50 | Niveau dimming |
| `strings.kt` | ~30 | String helpers |

#### Playback (15 fichiers, ~1 200 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsPlaybackScreen.kt` | 136 | Menu lecture |
| `SettingsPlaybackAdvancedScreen.kt` | 326 | Options avancées |
| `SettingsPlaybackPlayerScreen.kt` | ~70 | Choix lecteur |
| `SettingsPlaybackMaxBitrateScreen.kt` | ~70 | Limite bitrate |
| `SettingsPlaybackMaxResolutionScreen.kt` | ~60 | Limite résolution |
| `SettingsPlaybackRefreshRateSwitchingBehaviorScreen.kt` | ~50 | Refresh rate |
| `SettingsPlaybackZoomModeScreen.kt` | ~50 | Mode zoom |
| `SettingsPlaybackAudioBehaviorScreen.kt` | ~50 | Piste audio |
| `SettingsPlaybackInactivityPromptScreen.kt` | ~50 | Timeout inactivité |
| `SettingsPlaybackPrerollsScreen.kt` | ~50 | Prerolls |
| `SettingsPlaybackResumeSubtractDurationScreen.kt` | ~50 | Durée rewind resume |
| `nextup/SettingsPlaybackNextUpScreen.kt` | ~60 | Menu NextUp |
| `nextup/SettingsPlaybackNextUpBehaviorScreen.kt` | ~50 | Comportement NextUp |
| `mediasegment/SettingsPlaybackMediaSegmentsScreen.kt` | ~60 | Menu segments média |
| `mediasegment/SettingsPlaybackMediaSegmentScreen.kt` | ~60 | Config par segment |

#### Library (7 fichiers, ~550 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsLibrariesScreen.kt` | 119 | Liste bibliothèques |
| `SettingsLibrariesDisplayScreen.kt` | 180 | Affichage par bibliothèque |
| `SettingsLibrariesDisplayImageSizeScreen.kt` | ~50 | Taille images |
| `SettingsLibrariesDisplayImageTypeScreen.kt` | ~50 | Type images |
| `SettingsLibrariesDisplayGridScreen.kt` | ~50 | Layout grille |
| `LibraryPreferencesHelper.kt` | ~50 | State management |
| `helpers.kt` | ~30 | Utilitaires |

#### Home (3 fichiers, ~350 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsHomeScreen.kt` | 242 | Sections accueil |
| `SettingsHomeSectionScreen.kt` | ~60 | Config par section |
| `SettingsHomePosterSizeScreen.kt` | ~50 | Taille posters |

#### Live TV (3 fichiers, ~150 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsLiveTvGuideFiltersScreen.kt` | ~50 | Filtres guide |
| `SettingsLiveTvGuideOptionsScreen.kt` | ~50 | Options guide |
| `SettingsLiveTvGuideChannelOrderScreen.kt` | ~50 | Ordre chaînes |

#### License (2 fichiers, ~100 LOC)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsLicensesScreen.kt` | ~50 | Liste licences |
| `SettingsLicenseScreen.kt` | ~50 | Détail licence |

#### About (1 fichier)

| Fichier | LOC | Rôle |
|---|---|---|
| `SettingsAboutScreen.kt` | ~80 | Infos app, serveur |

#### VegafoX (17 fichiers, 2 139 LOC)

| Fichier | LOC | Prefs | Rôle |
|---|---|---|---|
| `SettingsJellyseerrScreen.kt` | 745 | 15+ | Intégration Jellyseerr (le plus gros) |
| `SettingsPluginScreen.kt` | 256 | 3 | Sync plugins |
| `SettingsJellyseerrRowsScreen.kt` | 219 | 5 | Config rows Jellyseerr |
| `SettingsVegafoXSyncPlayScreen.kt` | 138 | 6 | SyncPlay |
| `SettingsVegafoXHomeRowsImageScreen.kt` | 109 | 1 | Type image home rows |
| `SettingsVegafoXParentalControlsScreen.kt` | 97 | 2 | Contrôle parental |
| `SettingsLabelHelpers.kt` | 59 | — | Helpers labels |
| `SettingsVegafoXMediaBarColorScreen.kt` | 59 | 1 | Couleur media bar |
| `SettingsVegafoXSeasonalSurpriseScreen.kt` | 54 | 1 | Surprise saisonnière |
| `SettingsVegafoXBrowsingBlurScreen.kt` | 53 | 1 | Blur navigation |
| `SettingsVegafoXDetailsBlurScreen.kt` | 53 | 1 | Blur détails |
| `SettingsVegafoXNavbarPositionScreen.kt` | 51 | 1 | Position navbar (TOP/LEFT) |
| `SettingsVegafoXShuffleContentTypeScreen.kt` | 50 | 1 | Type contenu shuffle |
| `SettingsVegafoXMediaBarContentTypeScreen.kt` | 50 | 1 | Contenu media bar |
| `SettingsVegafoXMediaBarItemCountScreen.kt` | 50 | 1 | Nombre items media bar |
| `SettingsVegafoXMediaBarOpacityScreen.kt` | 48 | 1 | Opacité media bar |
| `SettingsVegafoXThemeMusicVolumeScreen.kt` | 48 | 1 | Volume musique thème |

---

## 2. Inventaire Preference Stores

### 2.1 Stores principaux

| Fichier | LOC | Prefs | Scope |
|---|---|---|---|
| `UserPreferences.kt` | 437 | 66 | Prefs utilisateur globales |
| `UserSettingPreferences.kt` | 332 | ~30 | Per-user ou global |
| `JellyseerrPreferences.kt` | 185 | ~10 | Jellyseerr API |
| `DisplayPreferencesStore.kt` | 156 | ~5 | Sync serveur (library display) |
| `SystemPreferences.kt` | 71 | ~8 | État système (non éditable) |
| `HomeSectionConfig.kt` | 63 | — | Config sections home (JSON) |
| `JellyseerrRowConfig.kt` | 57 | — | Config rows Jellyseerr (JSON) |
| `PreferencesRepository.kt` | 53 | — | Agrégation/injection Koin |
| `LibraryPreferences.kt` | 37 | ~5 | Display per-library |
| `LiveTvPreferences.kt` | 25 | ~3 | Live TV |
| `TelemetryPreferences.kt` | 21 | 1 | Télémétrie |

### 2.2 Enums de préférences (`preference/constant/`)

| Fichier | LOC |
|---|---|
| `StillWatchingBehavior.kt` | 63 |
| `MaxVideoResolution.kt` | 40 |
| `AppTheme.kt` | 28 |
| `ClockBehavior.kt` | 28 |
| `WatchedIndicatorBehavior.kt` | 28 |
| `NextUpBehavior.kt` | 25 |
| `ZoomMode.kt` | 24 |
| `RatingType.kt` | 23 |
| `RefreshRateSwitchingBehavior.kt` | 21 |
| `LiveTvChannelOrder.kt` | 19 |
| `AudioBehavior.kt` | 18 |
| `PosterSize.kt` | 15 |
| `NavbarPosition.kt` | 12 |
| `UserSelectBehavior.kt` | 7 |

### 2.3 Framework de préférences (`preference/` module)

| Fichier | LOC | Rôle |
|---|---|---|
| `Preference.kt` | 18 | Data class `Preference<T>(key, default)` |
| `PreferenceEnum.kt` | 21 | Interface pour enums sérialisables |
| `store/PreferenceStore.kt` | 74 | Classe abstraite, opérateurs `get`/`set` |
| `store/SharedPreferenceStore.kt` | 135 | Impl Android SharedPreferences |
| `store/AsyncPreferenceStore.kt` | 42 | Wrapper async avec `commit()` |

---

## 3. XML Preference files

**Aucun.** Les 5 fichiers en `res/xml/` sont :
- `network_security_config.xml`
- `file_provider_paths.xml`
- `backup_content.xml` / `backup_rules.xml`
- `searchable.xml`

→ Zéro fichier de préférences XML. Tout est code Kotlin.

---

## 4. Patterns problématiques

### 4.1 Patterns legacy absents (bon signe)

| Pattern recherché | Résultat |
|---|---|
| `PreferenceFragmentCompat` | **0 match** |
| `findPreference()` | **0 match** |
| `addPreferencesFromResource()` | **0 match** |
| `preferenceScreen` (XML) | **0 match** |

→ Migration Compose **terminée à 100%**.

### 4.2 Patterns actuels identifiés

#### P1 — Un fichier par valeur de préférence (prolifération)
- 12 écrans VegafoX ne gèrent qu'**une seule préférence** chacun (~50 LOC chacun)
- Pattern quasi-identique : `rememberPreference` + `SettingsColumn` + `items` + `RadioButton`
- **~600 LOC de boilerplate** pour 12 préférences simples

#### P2 — `routes.kt` monolithique (483 LOC)
- 55 constantes + 63 entrées map dans un seul fichier
- Couplé à tous les écrans (63 imports)
- Chaque nouvel écran nécessite 3 ajouts : constante, import, map entry

#### P3 — Duplication `rememberPreference` overloads
- 2 overloads quasi-identiques dans `rememberPreference.kt` (lignes 19 et 34)
- Seule différence : contrainte `Enum<T>` pour le JVM name

#### P4 — `SettingsJellyseerrScreen.kt` surdimensionné (745 LOC)
- Mélange logique métier (API calls, validation) et UI
- Devrait être splitté : ViewModel + Screen

#### P5 — Couplage direct Koin dans les composables
- Tous les écrans font `koinInject<UserPreferences>()` directement
- Pas de paramètre composable → difficile à tester/prévisualiser

#### P6 — Pas de `SettingsEntry` déclaratif
- Recherche `SettingsEntry` : 0 résultat dans le code settings
- Chaque écran est un composable impératif, pas de DSL déclaratif

---

## 5. Architecture actuelle

```
MainActivitySettings (entry point)
  └─ JellyfinTheme
     └─ SettingsDialog (modal overlay)
        └─ SettingsRouterContent (Router-based nav)
           └─ routes map (63 entries)
              └─ Screen Composables
                 ├─ rememberPreference() ← bridge PreferenceStore↔State
                 ├─ koinInject<UserPreferences>()
                 ├─ SettingsColumn { items { ListButton + RadioButton } }
                 └─ router.back() on selection
```

**State flow** :
```
SharedPreferences ← PreferenceStore ← rememberPreference() → MutableState<T>
                                    → LaunchedEffect (write-back)
                                    → AsyncPreferenceStore.commit() (IO)
```

---

## 6. Proposition d'architecture cible

### 6.1 `SettingsEntry` déclaratif

Remplacer les 12 écrans mono-préférence par un DSL :

```kotlin
// Déclaration
val navbarPositionEntry = enumSettingsEntry(
    preference = UserPreferences.navbarPosition,
    titleRes = R.string.pref_navbar_position,
    descriptionRes = R.string.pref_navbar_position_description,
    sectionTitleRes = R.string.vegafox_settings,
    options = NavbarPosition.entries,
    labelRes = mapOf(
        NavbarPosition.TOP to R.string.pref_navbar_position_top,
        NavbarPosition.LEFT to R.string.pref_navbar_position_left,
    ),
)

// Rendu automatique via un composable générique
@Composable
fun <T : Enum<T>> EnumSettingsScreen(entry: EnumSettingsEntry<T>) { ... }
```

**Gain estimé** : ~500 LOC supprimés, 12 fichiers remplacés par 1 fichier de déclarations.

### 6.2 Routes modulaires

Splitter `routes.kt` en modules par catégorie :
```
routes/
├─ authenticationRoutes.kt
├─ customizationRoutes.kt
├─ playbackRoutes.kt
├─ vegafoxRoutes.kt
├─ ...
└─ allRoutes.kt  (merge)
```

### 6.3 ViewModel pour écrans complexes

Extraire la logique de `SettingsJellyseerrScreen.kt` dans un `JellyseerrSettingsViewModel` :
- API calls, validation, état loading/error → ViewModel
- UI pure → Screen composable

### 6.4 Injection via paramètres composables

```kotlin
// Avant
@Composable fun MyScreen() {
    val prefs = koinInject<UserPreferences>()
}

// Après
@Composable fun MyScreen(prefs: UserPreferences = koinInject()) {
    // testable avec Preview
}
```

---

## 7. Priorités de refactor

| Priorité | Action | Impact | Effort |
|---|---|---|---|
| **P1** | `SettingsEntry` DSL pour écrans mono-pref | -500 LOC, -12 fichiers | Moyen |
| **P2** | Splitter `routes.kt` | Maintenabilité | Faible |
| **P3** | ViewModel pour JellyseerrScreen | Séparation concerns | Moyen |
| **P4** | Unifier les 2 overloads `rememberPreference` | Clean code | Faible |
| **P5** | Injection composable parameters | Testabilité | Faible |
