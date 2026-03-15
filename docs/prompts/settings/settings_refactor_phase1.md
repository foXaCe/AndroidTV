# Settings Refactor Phase 1

**Date** : 2026-03-09

---

## Résumé

| Refactor | Statut | LOC supprimés | Notes |
|----------|--------|---------------|-------|
| R1 rememberPreference unifié | ✓ | ~18 | 2 overloads → 1, + fix PreferenceStore.get |
| R2 injection paramètres | ✓ | 0 (refactor) | 5 écrans : Main, Customization, Playback, Home, Auth |
| R3 SettingsEntry DSL créé | ✓ | 0 (nouveau) | `SettingsEntry<T>` + `OptionListScreen` + 10 entries |
| R4 10 écrans → OptionListScreen | ✓ | ~510 | 10 fichiers supprimés |
| R5 routes.kt splitté | ✓ | ~330 (routes map) | 8 fichiers modules, routes.kt réduit à ~80 LOC |

- **Fichiers supprimés** : 10 écrans mono-préférence
- **Fichiers créés** : 10 (SettingsEntry.kt, VegafoXSettingsEntries.kt, 8 route modules)
- **LOC supprimés net** : ~530
- **BUILD SUCCESSFUL** ✓ (github + playstore)

---

## Détails

### R1 — rememberPreference unifié

- `PreferenceStore.get(Preference<T : Any>)` étendu pour gérer les enums via `EnumDispatchHelper`
- L'overload `@JvmName("rememberEnumPreference")` supprimé
- Résultat : 1 seule fonction `rememberPreference` pour tous les types

### R2 — Injection via paramètres composables

5 écrans modifiés (koinInject → paramètre avec default) :
- `SettingsMainScreen(userPreferences)`
- `SettingsCustomizationScreen(userPreferences, userRepository)`
- `SettingsPlaybackScreen(userPreferences, externalAppRepository)`
- `SettingsHomeScreen(userSettingPreferences, userPreferences)`
- `SettingsAuthenticationScreen(launchedFromLogin, serverRepository, serverUserRepository, authenticationPreferences)`

### R3 — SettingsEntry DSL

- `SettingsEntry<T : Any>` : data class avec `preference`, `options`, `label` (@Composable)
- `OptionListScreen<T>` : composable générique SettingsColumn + RadioButton
- `VegafoXSettingsEntries.kt` : 10 entrées déclaratives

### R4 — 10 écrans mono-préférence supprimés

Remplacés par `OptionListScreen(entry, store)` dans les routes :

| Écran supprimé | Entry | Store |
|---|---|---|
| NavbarPositionScreen | navbarPositionEntry | UserPreferences |
| ShuffleContentTypeScreen | shuffleContentTypeEntry | UserPreferences |
| MediaBarContentTypeScreen | mediaBarContentTypeEntry | UserSettingPreferences |
| MediaBarItemCountScreen | mediaBarItemCountEntry | UserSettingPreferences |
| MediaBarOpacityScreen | mediaBarOpacityEntry | UserSettingPreferences |
| MediaBarColorScreen | mediaBarColorEntry | UserSettingPreferences |
| ThemeMusicVolumeScreen | themeMusicVolumeEntry | UserSettingPreferences |
| SeasonalSurpriseScreen | seasonalSurpriseEntry | UserPreferences |
| DetailsBlurScreen | detailsBlurEntry | UserSettingPreferences |
| BrowsingBlurScreen | browsingBlurEntry | UserSettingPreferences |

**Non remplacés** (trop complexes pour le DSL) :
- `HomeRowsImageScreen` (toggle conditionnel + état per-row)
- `ParentalControlsScreen` (chargement async + checkboxes)

### R5 — routes.kt splitté

```
routes/
├─ AuthenticationRoutes.kt   (7 routes)
├─ CustomizationRoutes.kt    (13 routes, incl. screensaver + subtitles)
├─ LibraryRoutes.kt          (5 routes)
├─ HomeRoutes.kt             (4 routes)
├─ LiveTvRoutes.kt           (3 routes)
├─ PlaybackRoutes.kt         (15 routes + 5 SyncPlay numeric)
├─ VegafoXRoutes.kt          (16 routes, incl. Jellyseerr + DSL entries)
└─ AllRoutes.kt              (merge + 6 routes root/misc)
```

`routes.kt` conservé avec :
- `object Routes` (toutes les constantes — backward compatible, 0 import cassé)
- `val routes = allSettingsRoutes` (délègue à AllRoutes)
