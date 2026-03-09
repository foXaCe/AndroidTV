# V5A — Audit de cohérence visuelle Design System

**Date** : 2026-03-09
**Référence** : `docs/design/DESIGN_SYSTEM.md`
**Périmètre** : Tous les fichiers Compose sous `app/src/main/java/`

---

## Résumé exécutif

| Catégorie | Violations | Priorité |
|-----------|-----------|----------|
| Typographie | 85 | Haute |
| Icônes | 0 | — |
| Boutons | 51 | Haute |
| Switches/Toggles | 36 | Moyenne |
| Espacements | 28 | Moyenne |
| **TOTAL** | **200** | |

---

## 1. Audit typographie

### 1.1 fontSize hardcodé (3 instances) — Critique

| Fichier | Ligne | Valeur hardcodée | Token DS correct |
|---------|-------|-----------------|------------------|
| `dream/composable/DreamContentLibraryShowcase.kt` | 70 | `fontSize = 32.sp` | `JellyfinTheme.typography.headlineLarge` |
| `dream/composable/DreamContentNowPlaying.kt` | 91 | `fontSize = 22.sp` | `JellyfinTheme.typography.headlineMedium` (24sp) |
| `dream/composable/DreamContentNowPlaying.kt` | 127 | `fontSize = 26.sp` | `JellyfinTheme.typography.headlineMedium` (24sp) |
| `dream/composable/DreamContentNowPlaying.kt` | 144 | `fontSize = 18.sp` | `JellyfinTheme.typography.titleLarge` (20sp) |

### 1.2 fontWeight redondant sur style DS (77 instances) — Haute

Le `fontWeight` est surchargé alors que le style DS définit déjà le bon poids.

| Fichier | Instances | Exemple |
|---------|-----------|---------|
| `jellyseerr/compose/JellyseerrMediaDetailsScreen.kt` | 4 | `fontWeight = Bold` sur `headlineMedium` (déjà W500) |
| `jellyseerr/compose/JellyseerrPersonDetailsScreen.kt` | 4 | `fontWeight = Bold` sur `headlineMedium`/`titleLarge` |
| `jellyseerr/compose/QualitySelectionDialog.kt` | 2 | `fontWeight = Bold` sur `titleLarge` (déjà W600) |
| `jellyseerr/compose/AdvancedRequestOptionsDialog.kt` | 2 | `fontWeight = Bold` sur `titleLarge`/`titleMedium` |
| `jellyseerr/compose/SeasonSelectionDialog.kt` | 2 | `fontWeight = Bold` sur `titleLarge`/`labelLarge` |
| `jellyseerr/compose/JellyseerrStatusBadge.kt` | 1 | `fontWeight = Bold` sur `labelSmall` (W500) |
| `jellyseerr/compose/JellyseerrFactsSection.kt` | 1 | `fontWeight = Bold` sur `bodySmall` (W400) |
| `jellyseerr/compose/JellyseerrCastRow.kt` | 1 | `fontWeight = Bold` sur `titleLarge` (W600) |
| `browsing/v2/LibraryBrowseComponents.kt` | 5 | `Bold`/`Medium`/`Normal`/`SemiBold` sur divers styles |
| `browsing/v2/ScheduleBrowseFragment.kt` | 4 | `Light`/`SemiBold`/`Medium`/`Normal` |
| `browsing/v2/RecordingsBrowseFragment.kt` | 5 | `Light`/`SemiBold`/`Medium`/`Normal` |
| `browsing/v2/SeriesRecordingsBrowseFragment.kt` | 6 | `Light`/`SemiBold`/`Normal`/`Medium` |
| `browsing/v2/LiveTvBrowseFragment.kt` | 5 | `Light`/`SemiBold`/`Medium`/`Normal` |
| `browsing/v2/MusicBrowseFragment.kt` | 5 | `Light`/`SemiBold`/`Normal`/`Medium` |
| `browsing/compose/GenresGridScreen.kt` | 1 | `fontWeight = SemiBold` sur `titleMedium` (W600) |
| `playback/overlay/SkipOverlayView.kt` | 1 | `fontWeight = SemiBold` sur `titleMedium` (W600) |
| `NowPlayingView.kt` | 1 | `fontWeight = Bold` (pas de style DS) |
| `shared/toolbar/ExpandableIconButton.kt` | 1 | `.copy(fontWeight = Bold)` |
| `shared/toolbar/ExpandableLibrariesButton.kt` | 1 | `.copy(fontWeight = Bold)` |

### 1.3 .copy(fontWeight) sur style DS (18 instances) — Moyenne

Modifie le poids du style DS via `.copy()`, créant des variantes non-standard.

| Fichier | Instances | Détail |
|---------|-----------|--------|
| `itemdetail/v2/ItemDetailsComponents.kt` | 9 | W500→W900, W600 sur labelMedium/bodySmall/etc. |
| `itemdetail/v2/content/PersonDetailsContent.kt` | 1 | W700 sur `headlineLarge` |
| `itemdetail/v2/content/MovieDetailsContent.kt` | 2 | W500/W700 sur `titleMedium`/`headlineLarge` |
| `itemdetail/v2/content/SeriesDetailsContent.kt` | 1 | W700 sur `headlineLarge` |
| `itemdetail/v2/content/MusicDetailsContent.kt` | 2 | W700/W600 sur `headlineLarge`/`headlineMedium` |
| `itemdetail/v2/content/SeasonDetailsContent.kt` | 2 | W500/W700 sur `headlineMedium`/`display` |

**Récurrence** : Les titres principaux sont systématiquement surchargés en W700 (Bold)
→ **Recommandation** : Créer `JellyfinTheme.typography.headlineLargeBold` si ce poids est voulu partout.

---

## 2. Audit icônes

### Résultat : 0 incohérence

Le projet utilise **exclusivement des icônes custom XML** (`R.drawable.ic_*`).
Aucune utilisation de `Icons.Default`, `Icons.Filled`, `Icons.Rounded`, `Icons.Outlined` ou `Icons.Sharp`.

| Méthode | Usages | Fichiers |
|---------|--------|----------|
| `vectorResource(R.drawable.ic_*)` | 126 | 34 |
| `painterResource(R.drawable.*)` | 109 | 34 |
| **Material Design Icons** | **0** | **0** |

**Conclusion** : Cohérence parfaite. Choix intentionnel pour le contrôle design TV.

---

## 3. Audit boutons

### 3.1 Composants custom TV-friendly (OK)

| Composant | Fichier | Min Height | Focus | D-Pad |
|-----------|---------|-----------|-------|-------|
| `Button` | `base/button/Button.kt` | 40dp | ✅ | ✅ |
| `IconButton` | `base/button/IconButton.kt` | ~40dp | ✅ | ✅ |
| `DetailActionButton` | `itemdetail/v2/ItemDetailsComponents.kt` | 80dp wide | ✅ | ✅ |
| `DialogActionButton` | `jellyseerr/compose/SeasonSelectionDialog.kt` | ~44dp | ✅ | ✅ |
| `QualityButton` | `jellyseerr/compose/QualitySelectionDialog.kt` | ~48dp | ✅ | ✅ |
| `OptionRadioButton` | `jellyseerr/compose/AdvancedRequestOptionsDialog.kt` | ~40dp | ✅ | ✅ |
| `ListButton` | `base/list/ListButton.kt` | ~56dp | ✅ | ✅ |
| `LibraryToolbarButton` | `browsing/v2/LibraryBrowseComponents.kt` | 34dp ⚠️ | ✅ | ✅ |

### 3.2 Material3 non TV-friendly (51 instances) — Haute

| Composant M3 | Fichier | Instances | Problème |
|-------------|---------|-----------|----------|
| `TextButton` | `settings/vegafox/SettingsJellyseerrScreen.kt` | 11 | Feedback focus minimal, pas de styling TV |
| `IconButton` M3 | `jellyseerr/compose/JellyseerrBrowseByScreen.kt` | 2 | Pas de couleurs focus TV custom |
| `RadioButton` M3 | `jellyseerr/compose/JellyseerrBrowseByScreen.kt` | 4 | Dans DropdownMenu, pas de styling TV |
| `Checkbox` M3 | Écrans Settings (35+ fichiers) | 35+ | Pas de focus ring indépendante, 18dp visuel |

**Détail TextButton** (priorité critique) :
- Lignes 402, 415, 429, 442, 477, 482, 517, 522, 567, 578, 610, 621
- Aucun feedback visuel au focus D-pad
- Inconsistant avec les boutons custom du reste de l'app

---

## 4. Audit switches et toggles

### 4.1 Composants custom (OK)

| Composant | Fichier | TV-Friendly |
|-----------|---------|-------------|
| `Checkbox` custom | `base/form/Checkbox.kt` | Dépend du wrapper (18dp visuel) |
| `RadioButton` custom | `base/form/RadioButton.kt` | Dépend du wrapper (18dp visuel) |
| `SeasonCheckboxRow` | `jellyseerr/compose/SeasonSelectionDialog.kt` | ✅ (focus border + bg change) |

### 4.2 Material3 brut (36 instances) — Moyenne

| Composant | Localisation | Instances | Problème |
|-----------|-------------|-----------|----------|
| `Checkbox` M3 | Settings screens (trailing content) | 35+ | Pas de styling TV custom, dépend du ListButton parent |
| `Switch` M3 | `playlist/CreatePlaylistDialog.kt:213` | 1 | Wrapper Row acceptable mais pas de couleurs custom |

**Fichiers Settings avec Checkbox M3 brut** :
- `SettingsAuthenticationPinCodeScreen.kt` (1)
- `SettingsAuthenticationScreen.kt` (1)
- `SettingsCustomizationScreen.kt` (10)
- `SettingsPlaybackAdvancedScreen.kt` (7)
- `SettingsPluginScreen.kt` (10)
- Et ~7 autres écrans Settings

**Note** : Les Checkbox sont dans des `ListButton` qui gèrent le focus → partiellement acceptable, mais incohérent avec le pattern `SeasonCheckboxRow`.

---

## 5. Audit espacements et padding

### 5.1 Valeurs non-standard (hors grille 4dp) — Haute

| Fichier | Ligne | Valeur | Devrait être |
|---------|-------|--------|-------------|
| `base/button/Button.kt` | 22 | `10.dp` vertical | `Tokens.Space.spaceSm` (8dp) ou `ds_space_10` |
| `base/button/IconButton.kt` | 18 | `10.dp` | `Tokens.Space.spaceSm` (8dp) ou `ds_space_10` |
| `base/form/Checkbox.kt` | 46 | `3.dp` | `Tokens.Space.space2xs` (2dp) ou `spaceXs` (4dp) |
| `base/form/RadioButton.kt` | 46 | `3.dp` | `Tokens.Space.space2xs` (2dp) ou `spaceXs` (4dp) |
| `base/form/ColorSwatch.kt` | 32 | `6.dp` | `Tokens.Space.spaceSm` (8dp) ou `ds_space_6` |
| `browsing/compose/BrowseMediaCard.kt` | 96 | `6.dp` spacer | `Tokens.Space.spaceSm` (8dp) ou `ds_space_6` |
| `browsing/compose/GenresGridScreen.kt` | 137 | `27.dp` padding | `Tokens.Space.spaceLg` (24dp) ou `spaceXl` (32dp) |
| `browsing/compose/GenresGridScreen.kt` | 165 | `27.dp` padding | Idem |
| `browsing/compose/GenresGridScreen.kt` | 453 | `12.dp` vertical | `Tokens.Space.spaceSm` (8dp) ou `ds_space_12` |
| `browsing/compose/GenresGridScreen.kt` | 458 | `18.dp` size | `Tokens.Space.spaceMd` (16dp) |
| `browsing/compose/GenresGridScreen.kt` | 469 | `10.dp` size | `Tokens.Space.spaceSm` (8dp) |
| `home/compose/HomeScreen.kt` | 88 | `28.dp` spacer | `Tokens.Space.spaceLg` (24dp) ou `spaceXl` (32dp) |
| `home/compose/HomeScreen.kt` | 110 | `27.dp` bottom | `Tokens.Space.spaceLg` (24dp) ou `spaceXl` (32dp) |
| `base/tv/TvRowList.kt` | 50-51 | `27.dp` top/bottom | `Tokens.Space.spaceLg` (24dp) ou `spaceXl` (32dp) |

### 5.2 Valeurs standard mais hardcodées (14 instances) — Basse

| Fichier | Ligne | Valeur | Token DS |
|---------|-------|--------|----------|
| `browsing/compose/GenresGridScreen.kt` | 127 | `16.dp` | `Tokens.Space.spaceMd` |
| `browsing/compose/GenresGridScreen.kt` | 138-139 | `16.dp` ×2 | `Tokens.Space.spaceMd` |
| `browsing/compose/GenresGridScreen.kt` | 263/304/373 | `24.dp`/`8.dp` | `spaceLg`/`spaceSm` |
| `browsing/compose/GenresGridScreen.kt` | 315/384 | `4.dp` | `Tokens.Space.spaceXs` |
| `browsing/compose/GenresGridScreen.kt` | 475 | `16.dp` | `Tokens.Space.spaceMd` |
| `home/compose/HomeScreen.kt` | 71 | `8.dp` | `Tokens.Space.spaceSm` |
| `home/compose/HomeScreen.kt` | 107 | `48.dp` | `Tokens.Space.space3xl` |
| `base/tv/TvHeader.kt` | 51 | `8.dp` | `Tokens.Space.spaceSm` |
| `base/tv/TvCardGrid.kt` | 36 | `48.dp` | `Tokens.Space.space3xl` |
| `base/tv/TvCardGrid.kt` | 47-48 | `16.dp` ×2 | `Tokens.Space.spaceMd` |
| `base/tv/TvRowList.kt` | 71 | `8.dp` | `Tokens.Space.spaceSm` |
| `base/tv/TvRowList.kt` | 78 | `16.dp` | `Tokens.Space.spaceMd` |
| `base/tv/TvRowList.kt` | 89 | `16.dp` | `Tokens.Space.spaceMd` |

### 5.3 Formes hardcodées — Basse

| Fichier | Ligne | Valeur | Token DS |
|---------|-------|--------|----------|
| `base/popover/Popover.kt` | 31 | `RoundedCornerShape(4.dp)` | `JellyfinTheme.shapes.extraSmall` |

---

## 6. Score de cohérence par écran

| Écran | Typo | Icônes | Boutons | Switches | Spacing | Total |
|-------|------|--------|---------|----------|---------|-------|
| HomeScreen | 9/10 | 10/10 | 10/10 | 10/10 | 6/10 | **9.0/10** |
| SearchScreen | 10/10 | 10/10 | 10/10 | 10/10 | 10/10 | **10/10** |
| GenresGridScreen | 8/10 | 10/10 | 10/10 | 10/10 | 5/10 | **8.6/10** |
| LibraryBrowseScreen | 6/10 | 10/10 | 9/10 | 10/10 | 8/10 | **8.6/10** |
| ItemDetailScreen | 5/10 | 10/10 | 10/10 | 10/10 | 8/10 | **8.6/10** |
| SettingsScreens | 10/10 | 10/10 | 7/10 | 5/10 | 9/10 | **8.2/10** |
| Jellyseerr screens | 4/10 | 10/10 | 6/10 | 8/10 | 8/10 | **7.2/10** |
| LiveTV Browse | 5/10 | 10/10 | 10/10 | 10/10 | 8/10 | **8.6/10** |
| Recordings Browse | 5/10 | 10/10 | 10/10 | 10/10 | 8/10 | **8.6/10** |
| Music Browse | 5/10 | 10/10 | 10/10 | 10/10 | 8/10 | **8.6/10** |
| Dream screens | 2/10 | 10/10 | 10/10 | 10/10 | 10/10 | **8.4/10** |

### Légende scores
- **10** : 100% conforme DS
- **8-9** : Quelques valeurs hardcodées mais standards
- **6-7** : Surcharges fontWeight ou valeurs non-standard
- **4-5** : Nombreuses surcharges ou valeurs hors-grille
- **2-3** : fontSize hardcodés, pas de référence DS

---

## 7. Synthèse et priorités

### Corrections nécessaires : 200 total

| Priorité | Catégorie | Count | Action |
|----------|-----------|-------|--------|
| **Critique** | fontSize hardcodés (Dream) | 4 | Remplacer par `JellyfinTheme.typography.*` |
| **Critique** | TextButton M3 (Jellyseerr Settings) | 11 | Remplacer par bouton custom TV |
| **Haute** | fontWeight redondant | 77 | Supprimer les surcharges inutiles |
| **Haute** | RadioButton M3 dans DropdownMenu | 4 | Wrapper TV custom |
| **Haute** | Spacing 27dp non-standard | 5 | Aligner sur 24dp ou 32dp |
| **Moyenne** | .copy(fontWeight) sur styles DS | 18 | Créer variantes DS ou supprimer |
| **Moyenne** | Checkbox M3 dans Settings | 35 | Accepter (ListButton gère focus) ou wrapper |
| **Moyenne** | Spacing non-standard (3/6/10/18/28dp) | 9 | Aligner sur grille 4dp |
| **Basse** | Spacing standard hardcodé | 14 | Remplacer par `Tokens.Space.*` |
| **Basse** | LibraryToolbarButton 34dp | 1 | Augmenter à 48dp |
| **Basse** | Switch M3 brut | 1 | Wrapper acceptable |
| **Basse** | RoundedCornerShape hardcodé | 1 | Remplacer par `shapes.extraSmall` |

### Recommandations architecturales

1. **Créer `headlineLargeBold`** dans le DS : les titres principaux sont systématiquement surchargés en W700
2. **Supprimer les fontWeight redondants** : quand le poids surchargé = poids DS (ex: SemiBold sur titleMedium déjà W600)
3. **Standardiser 27dp → 24dp** : valeur TV marge verticale, utilisée dans 4+ fichiers
4. **Wrapper `SettingsCheckbox`** : encapsuler Checkbox M3 avec le pattern SeasonCheckboxRow pour consistance
5. **Remplacer TextButton M3** dans JellyseerrSettings par le composant `Button` custom existant
