# V4 — Audit & Refactoring Settings

**Date** : 2026-03-08
**Projet** : VegafoX for Android TV
**Branche** : `main`

---

## Résumé Exécutif

Les settings sont **100% Compose** — aucun `PreferenceFragmentCompat` ni XML `preference_*.xml`.
Le pattern existant (`SettingsColumn` + `ListButton` + `ListSection` + `Checkbox`) est déjà déclaratif et TV-friendly.

**Problème principal** : `SettingsPluginScreen.kt` (386L) est un méga-écran fourre-tout mélangeant 10 domaines.
**Problème secondaire** : `SettingsMainScreen.kt` (604L) embarque les dialogs de mise à jour (250L de UI).

---

## ÉTAPE 1 — Inventaire Complet des Fichiers

### Infrastructure Preferences (module `preference/`)

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `preference/src/main/kotlin/Preference.kt` | 18 | Data class Preference<T> + factory helpers |
| `preference/src/main/kotlin/PreferenceEnum.kt` | 21 | Interface pour enums sérialisables |
| `preference/src/main/kotlin/store/PreferenceStore.kt` | 74 | Classe abstraite get/set |
| `preference/src/main/kotlin/store/AsyncPreferenceStore.kt` | 42 | Store async avec sync serveur |
| `preference/src/main/kotlin/store/SharedPreferenceStore.kt` | 135 | Implémentation SharedPreferences |

### Conteneurs de Préférences (app layer)

| Fichier | Lignes | Clés | Scope |
|---------|--------|------|-------|
| `UserPreferences.kt` | 437 | ~70 | Global (SharedPreferences par défaut) |
| `UserSettingPreferences.kt` | 332 | ~50 | Per-user (userId optionnel) |
| `SystemPreferences.kt` | 71 | 10 | System state |
| `TelemetryPreferences.kt` | 21 | 4 | Crash reporting |
| `AuthenticationPreferences.kt` | 43 | 6 | Auto-login |
| `JellyseerrPreferences.kt` | 185 | ~50 | Per-user Jellyseerr |
| `LiveTvPreferences.kt` | 25 | 7 | Live TV guide (async) |
| `LibraryPreferences.kt` | 37 | 6 | Per-library display (async) |

### Composants Partagés Settings

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `composable/SettingsColumn.kt` | 98 | LazyColumn + focus tracking + restore |
| `composable/SettingsLayout.kt` | 31 | Layout wrapper (padding) |
| `composable/SettingsNumericScreen.kt` | 61 | Écran générique slider (RadioButton list) |
| `composable/SettingsAsyncActionListButton.kt` | 98 | Button avec async loading state |
| `composable/SettingsRouterContent.kt` | 52 | Navigation content router |
| `composable/SettingsDialog.kt` | 46 | Dialog wrapper |
| `compat/rememberPreference.kt` | 48 | Bridge PreferenceStore → MutableState |

### Composants UI Base (réutilisables, pas spécifiques aux settings)

| Fichier | Rôle |
|---------|------|
| `ui/base/list/ListButton.kt` | Item de liste focusable (heading + caption + leading/trailing) |
| `ui/base/list/ListSection.kt` | Header de section (overline + heading + caption) |
| `ui/base/list/ListControl.kt` | Item avec contrôle custom (slider, etc.) |
| `ui/base/form/Checkbox.kt` | Switch toggle TV-friendly |
| `ui/base/form/RadioButton.kt` | Radio button pour sélection |
| `ui/base/form/RangeControl.kt` | Slider TV-friendly (D-pad) |
| `ui/base/form/ColorSwatch.kt` | Swatch couleur |

### Écrans Settings (85 fichiers)

| Catégorie | Fichiers | Lignes totales |
|-----------|----------|----------------|
| Root | `SettingsMainScreen.kt` | 604 |
| Authentication | 6 fichiers | ~650 |
| Customization | 7 + subtitle/(6) | ~750 |
| Home | 3 | ~340 |
| Library | 6 + helpers | ~550 |
| Live TV | 3 | ~180 |
| Playback | 12 + mediasegment/(3) + nextup/(2) | ~1100 |
| Screensaver | 5 + strings.kt | ~380 |
| **VegafoX** | **16** | **~2100** |
| Telemetry | 1 | 55 |
| Developer | 1 | 118 |
| About | 1 | 66 |
| License | 2 | 113 |
| **Total** | **~85 fichiers** | **~7000** |

### Routes (`routes.kt`)

- **154 route constants** dans l'object `Routes`
- **489 lignes** avec mapping route → composable
- 2 routes deprecated/dupliquées

---

## ÉTAPE 2 — Arbre des Paramètres Actuels (AVANT)

```
📱 SETTINGS (SettingsMainScreen — 604L)
│
├── 🔐 Utilisateurs (ACTION → SettingsAuthenticationScreen)
│   ├── Serveur {serverId} (ACTION → liste users)
│   │   └── User {userId} (ACTION → options login)
│   ├── Tri (LIST — LAST_USE/ALPHABETICAL)
│   ├── Auto-login (LIST — LAST_USER/SPECIFIC_USER/DISABLED)
│   └── Code PIN (ACTION → setup/change PIN)
│
├── 🎨 Personnalisation (ACTION → SettingsCustomizationScreen — 179L)
│   ├── 📚 Bibliothèques (ACTION → SettingsLibrariesScreen)
│   │   └── {library} (ACTION → display prefs)
│   │       ├── Type d'image (LIST — Poster/Thumb/Logo)
│   │       ├── Taille d'image (LIST — Small/Med/Large)
│   │       └── Grille (LIST — Horizontal/Vertical)
│   ├── 🏠 Écran d'accueil (ACTION → SettingsHomeScreen — 242L)
│   │   ├── Taille posters (LIST — PosterSize enum)
│   │   ├── Type image par rangée (ACTION → sélection)
│   │   ├── [Sections home] (TOGGLE + réordonnement D-pad gauche/droite)
│   │   └── Réinitialiser sections (ACTION)
│   ├── Couleur de focus (LIST — AppTheme enum)
│   ├── Horloge (LIST — ALWAYS/AUTO/NEVER)
│   ├── Indicateur vu (LIST — ALWAYS/NEVER)
│   ├── Backdrop (TOGGLE)
│   ├── Vignettes séries (TOGGLE)
│   └── Expansion focus cartes (TOGGLE)
│
├── 🌙 VegafoX (ACTION → SettingsPluginScreen — 386L) ⚠️ MÉGA-ÉCRAN
│   ├── [Plugin Sync]
│   │   └── Sync plugin activé (TOGGLE + action initialSync)
│   ├── [Toolbar] ← MAL PLACÉ : pas lié au plugin
│   │   ├── Position navbar (LIST — TOP/LEFT)
│   │   ├── Bouton shuffle (TOGGLE)
│   │   ├── Bouton genres (TOGGLE)
│   │   ├── Bouton favoris (TOGGLE)
│   │   ├── Bibliothèques dans toolbar (TOGGLE)
│   │   └── Type contenu shuffle (LIST — movies/tv/both)
│   ├── [Home] ← MAL PLACÉ : doublon avec Personnalisation > Accueil
│   │   ├── Fusionner continuer/next up (TOGGLE)
│   │   ├── Bibliothèques multi-serveur (TOGGLE)
│   │   ├── Vue dossiers (TOGGLE)
│   │   └── Confirmer sortie (TOGGLE)
│   ├── [Media Bar] ← OK ici mais trop d'items
│   │   ├── Media bar activée (TOGGLE)
│   │   ├── Type contenu (LIST — movies/tv/both)
│   │   ├── Nombre d'items (LIST — 5/10/15)
│   │   ├── Opacité overlay (SLIDER — 0-100%)
│   │   ├── Couleur overlay (LIST — 12 couleurs)
│   │   └── Aperçu trailer (TOGGLE)
│   ├── [Aperçu épisodes]
│   │   ├── Aperçu activé (TOGGLE)
│   │   └── Audio aperçu (TOGGLE)
│   ├── [Theme Music]
│   │   ├── Theme music activée (TOGGLE)
│   │   ├── Theme music sur rangées home (TOGGLE)
│   │   └── Volume (SLIDER — 0-100%)
│   ├── [Apparence] ← MAL PLACÉ : devrait être dans Personnalisation
│   │   ├── Surprise saisonnière (LIST — none/winter/spring/etc.)
│   │   ├── Flou détails (LIST — none/light/medium/strong/extra)
│   │   └── Flou navigation (LIST — idem)
│   ├── [Ratings] ← MAL PLACÉ : pas lié au plugin
│   │   ├── Ratings additionnels (TOGGLE)
│   │   ├── Labels ratings (TOGGLE)
│   │   └── Ratings épisodes (TOGGLE)
│   ├── [Jellyseerr] ← ACTION → SettingsJellyseerrScreen (740L)
│   │   └── Configuration Jellyseerr complète...
│   └── [Contrôle parental] ← ACTION → SettingsVegafoXParentalControlsScreen
│       └── Activation PIN parental
│
├── 🖼️ Écran de veille (ACTION → SettingsScreensaverScreen — 117L)
│   ├── Écran de veille activé (TOGGLE)
│   ├── Mode (LIST — library/logo)
│   ├── Timeout (LIST — durée)
│   ├── Dimming (SLIDER — 0-100%)
│   ├── Horloge sur écran de veille (TOGGLE)
│   └── Classification max (LIST — age rating)
│
├── ▶️ Lecture (ACTION → SettingsPlaybackScreen — 127L)
│   ├── Lecteur vidéo (ACTION → SettingsPlaybackPlayerScreen)
│   │   ├── Utiliser lecteur externe (TOGGLE)
│   │   └── Application externe (ACTION → sélection)
│   ├── Next Up (ACTION → SettingsPlaybackNextUpScreen)
│   │   ├── Timeout (SLIDER)
│   │   ├── Comportement (LIST)
│   │   └── File d'attente TV (TOGGLE)
│   ├── Inactivité (LIST — StillWatchingBehavior)
│   ├── Pré-roll (ACTION → cinema mode + ads)
│   ├── Sous-titres (ACTION → SettingsSubtitlesScreen) ← réutilisé depuis Customization
│   ├── Sous-titres par défaut aucun (TOGGLE)
│   ├── Media Segments (ACTION → liste types segments)
│   │   └── {segmentType} (LIST — Ask/Skip/View)
│   └── Avancé (ACTION → SettingsPlaybackAdvancedScreen — 326L)
│       ├── [Personnalisation]
│       │   ├── Pré-roll résumé (LIST)
│       │   ├── Avance rapide (SLIDER — 5-30s)
│       │   ├── Rewind après pause (SLIDER — 0-10s)
│       │   └── Description sur pause (TOGGLE)
│       ├── [Vidéo]
│       │   ├── Bitrate max (LIST)
│       │   ├── Résolution max (LIST)
│       │   ├── Refresh rate switching (LIST)
│       │   ├── Délai démarrage (SLIDER — 0-5s)
│       │   ├── Zoom mode (LIST)
│       │   ├── PGS direct play (TOGGLE)
│       │   └── ASS/SSA direct play (TOGGLE)
│       ├── [Audio/Live TV]
│       │   ├── Sortie audio (LIST — AudioBehavior)
│       │   ├── Mode nuit (TOGGLE)
│       │   ├── Direct stream live (TOGGLE)
│       │   └── Bitstream AC3 (TOGGLE)
│       └── [Troubleshooting]
│           └── Rapport profil device (ACTION async)
│
├── 🔄 SyncPlay (ACTION → SettingsVegafoXSyncPlayScreen — 138L)
│   ├── SyncPlay activé (TOGGLE)
│   ├── Sync correction activée (TOGGLE)
│   ├── Utiliser vitesse pour sync (TOGGLE)
│   ├── Utiliser saut pour sync (TOGGLE)
│   ├── Délai min vitesse sync (SLIDER — 10-1000ms)
│   ├── Délai max vitesse sync (SLIDER — 10-1000ms)
│   ├── Durée vitesse sync (SLIDER — 500-5000ms)
│   ├── Délai min saut sync (SLIDER — 10-5000ms)
│   └── Offset extra (SLIDER — -1000-1000ms)
│
├── ⚠️ Télémétrie (ACTION → SettingsTelemetryScreen — 55L)
│   ├── Rapports de crash (TOGGLE)
│   └── Inclure logs système (TOGGLE)
│
├── 🧪 Développeur (ACTION → SettingsDeveloperScreen — 118L)
│   ├── Mode debug (TOGGLE)
│   ├── Avertissement mode UI (TOGGLE, si non-TV)
│   ├── Playback rewrite (TOGGLE, si debug build)
│   ├── Trick play (TOGGLE)
│   ├── FFmpeg audio (TOGGLE)
│   └── Vider cache images (ACTION)
│
├── [Support & Mises à jour] ← Section dans le même écran
│   ├── Vérifier mises à jour (ACTION, si OTA activé)
│   ├── Notifications mises à jour (TOGGLE, si OTA activé)
│   └── Soutenir VegafoX (ACTION → DonateDialog)
│
└── ℹ️ À propos (ACTION → SettingsAboutScreen — 66L)
    ├── Version app (INFO — copy on click)
    ├── Modèle appareil (INFO — copy on click)
    └── Licences (ACTION → SettingsLicensesScreen)
        └── {artifactId} (ACTION → détail licence)
```

**Total : ~200 paramètres individuels accessibles**

---

## ÉTAPE 3 — Problèmes Identifiés

### MAL PLACÉ (7)

| # | Paramètre | Emplacement actuel | Emplacement logique |
|---|-----------|-------------------|-------------------|
| M1 | Toolbar (navbar, boutons shuffle/genres/favoris/libraries) | Plugin Screen | Personnalisation (nouveau sous-groupe) |
| M2 | Home settings (merge next up, multi-server, folder view, confirm exit) | Plugin Screen | Personnalisation > Comportement |
| M3 | Apparence (seasonal, blur x2) | Plugin Screen | Personnalisation |
| M4 | Ratings (additional, labels, episode) | Plugin Screen | Nouveau "Ratings" dans Personnalisation |
| M5 | Episode preview (enabled, audio) | Plugin Screen | Playback ou Home |
| M6 | Confirmer sortie | Plugin Screen | Personnalisation ou root |
| M7 | Live TV guide settings | Seulement depuis le guide | Devrait être listable depuis Settings aussi |

### DOUBLON (2)

| # | Description |
|---|-------------|
| D1 | `Routes.SYNCPLAY` et `Routes.VEGAFOX_SYNCPLAY` → même écran |
| D2 | `Routes.VEGAFOX_HOME_ROWS_IMAGE` (deprecated) et `Routes.HOME_ROWS_IMAGE_TYPE` → même écran |

### CODE MORT (1)

| # | Description |
|---|-------------|
| C1 | `@Deprecated Routes.VEGAFOX_HOME_ROWS_IMAGE` — route encore mappée dans `routes.kt` |

### ARCHITECTURE (3)

| # | Description |
|---|-------------|
| A1 | `SettingsMainScreen.kt` (604L) — 250 lignes de dialogs update (UpdateAvailableDialog, ReleaseNotesDialog) qui devraient être dans un fichier séparé |
| A2 | `SettingsPluginScreen.kt` (386L) — méga-écran fourre-tout avec 10 domaines distincts |
| A3 | Helper functions (`getShuffleContentTypeLabel`, `getMediaBarItemCountLabel`, etc.) dans `SettingsCustomizationScreen.kt` mais utilisées par `SettingsPluginScreen.kt` — devraient être dans un fichier utils partagé |

### ORDRE (1)

| # | Description |
|---|-------------|
| O1 | Dans le main screen, "SyncPlay" apparaît entre Lecture et Télémétrie — devrait être sous-section de Lecture ou groupé avec VegafoX |

---

## ÉTAPE 4 — Structure Cible (APRÈS)

### Principe

**Ne pas réécrire** le système existant — il est déjà Compose, déclaratif, et TV-friendly.
**Réorganiser** en splitant le méga-écran Plugin et en nettoyant la hiérarchie.

### Arbre Cible

```
📱 SETTINGS (SettingsMainScreen — nettoyé, ~200L)
│
├── 🔐 Utilisateurs (inchangé)
│
├── 🎨 Personnalisation (enrichi — absorbe toolbar, apparence, home behavior)
│   ├── [Navigation] ← items déplacés depuis Plugin
│   │   ├── Bibliothèques
│   │   ├── Écran d'accueil
│   │   ├── Couleur de focus
│   │   ├── Horloge
│   │   ├── Indicateur vu
│   │   ├── Backdrop
│   │   ├── Vignettes séries
│   │   └── Expansion focus cartes
│   ├── [Toolbar] ← déplacé depuis Plugin
│   │   ├── Position navbar
│   │   ├── Bouton shuffle
│   │   ├── Bouton genres
│   │   ├── Bouton favoris
│   │   ├── Bibliothèques dans toolbar
│   │   └── Type contenu shuffle
│   ├── [Home] ← déplacé depuis Plugin
│   │   ├── Fusionner continuer/next up
│   │   ├── Bibliothèques multi-serveur
│   │   ├── Vue dossiers
│   │   └── Confirmer sortie
│   └── [Apparence] ← déplacé depuis Plugin
│       ├── Surprise saisonnière
│       ├── Flou détails
│       └── Flou navigation
│
├── 🌙 VegafoX (réduit — seulement ce qui est vraiment VegafoX-specific)
│   ├── [Plugin Sync]
│   │   └── Sync plugin activé
│   ├── [Media Bar]
│   │   ├── Media bar activée
│   │   ├── Type contenu / Nombre items / Opacité / Couleur
│   │   ├── Aperçu trailer
│   │   ├── Aperçu épisodes
│   │   └── Audio aperçu
│   ├── [Theme Music]
│   │   ├── Activée / Home rows / Volume
│   ├── [Ratings]
│   │   ├── Ratings additionnels / Labels / Épisodes
│   ├── [Jellyseerr]
│   └── [Contrôle parental]
│
├── 🖼️ Écran de veille (inchangé)
│
├── ▶️ Lecture (inchangé — SyncPlay intégré comme sous-section)
│   ├── ... (existant)
│   └── 🔄 SyncPlay ← déplacé depuis root
│
├── ⚠️ Télémétrie (inchangé)
│
├── 🧪 Développeur (inchangé)
│
├── [Support & Mises à jour] (dialogs extraits dans fichier séparé)
│
└── ℹ️ À propos (inchangé)
```

### Changements de Fichiers

| Action | Fichier | Raison |
|--------|---------|--------|
| **SPLIT** | `SettingsPluginScreen.kt` (386L) | Toolbar/Home/Apparence → `SettingsCustomizationScreen.kt`; reste → `SettingsPluginScreen.kt` réduit |
| **EXTRACT** | `SettingsMainScreen.kt` (604L) | UpdateAvailableDialog + ReleaseNotesDialog → `UpdateDialogs.kt` (~250L) |
| **MOVE** | Helper functions | De `SettingsCustomizationScreen.kt` → `screen/vegafox/SettingsLabelHelpers.kt` |
| **DELETE** | `Routes.VEGAFOX_HOME_ROWS_IMAGE` mapping | Route deprecated, garder la const pour compat |
| **DELETE** | `Routes.SYNCPLAY` mapping | Doublon de `VEGAFOX_SYNCPLAY` |
| **UPDATE** | `SettingsMainScreen.kt` | Retirer SyncPlay du root (accessible depuis Playback) |
| **UPDATE** | `SettingsPlaybackScreen.kt` | Ajouter lien SyncPlay |
| **UPDATE** | `SettingsCustomizationScreen.kt` | Ajouter sections Toolbar, Home, Apparence |

---

## ÉTAPE 5 — Pattern Existant (Documenté)

### Comment fonctionne le système actuel

Le système utilise un pattern **déclaratif Compose** déjà en place :

```kotlin
// Chaque écran settings = 1 fichier Kotlin
@Composable
fun SettingsXxxScreen() {
    val userPreferences = koinInject<UserPreferences>()

    SettingsColumn {
        // Header de section
        item {
            ListSection(
                overlineContent = { Text("SETTINGS") },
                headingContent = { Text("Section Name") },
            )
        }

        // Toggle
        item {
            var myPref by rememberPreference(userPreferences, UserPreferences.myKey)
            ListButton(
                headingContent = { Text("Toggle Title") },
                captionContent = { Text("Description") },
                trailingContent = { Checkbox(checked = myPref) },
                onClick = { myPref = !myPref }
            )
        }

        // Navigation vers sous-écran
        item {
            ListButton(
                headingContent = { Text("Sub-screen") },
                onClick = { router.push(Routes.SUB_SCREEN) }
            )
        }
    }
}
```

### Comment ajouter un paramètre (3 lignes)

```kotlin
// Dans le SettingsColumn de l'écran approprié, ajouter :
item {
    var newPref by rememberPreference(userPreferences, UserPreferences.newKey)
    ListButton(
        headingContent = { Text(stringResource(R.string.new_pref_title)) },
        trailingContent = { Checkbox(checked = newPref) },
        onClick = { newPref = !newPref }
    )
}
```

### Comment supprimer un paramètre (1 ligne)

```kotlin
// Supprimer le bloc item { ... } correspondant dans le SettingsColumn
// La clé de préférence peut rester dans UserPreferences pour compat (valeur par défaut utilisée)
```

### Comment réordonner un paramètre

Déplacer le bloc `item { ... }` à la position souhaitée dans le `SettingsColumn`.

### Comment masquer un paramètre sans supprimer

```kotlin
// Wrapper conditionnel
if (showAdvancedOptions) item {
    // ...
}
```

---

## Résumé des Modifications — COMPLÉTÉES

**BUILD SUCCESSFUL** — `assembleRelease` en 4m24s, 0 erreur.

### Fichiers Créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `screen/UpdateDialogs.kt` | ~280 | UpdateAvailableDialog, ReleaseNotesDialog, checkForUpdates, downloadAndInstall, openUrl |
| `screen/vegafox/SettingsLabelHelpers.kt` | ~60 | getShuffleContentTypeLabel, getMediaBarItemCountLabel, getOverlayColorLabel, getSeasonalLabel, getBlurLabel |

### Fichiers Modifiés

| Fichier | Avant | Après | Changement |
|---------|-------|-------|-----------|
| `SettingsMainScreen.kt` | 604L | ~170L | Dialogs extraits vers UpdateDialogs.kt; SyncPlay retiré du root |
| `SettingsCustomizationScreen.kt` | 179L | ~250L | Ajout sections Toolbar, Home Behavior, Apparence (depuis Plugin) |
| `SettingsPluginScreen.kt` | 386L | ~240L | Retiré Toolbar/Home/Apparence (déplacés vers Customization) |
| `SettingsPlaybackScreen.kt` | 127L | ~137L | Ajout lien SyncPlay |
| `routes.kt` | 489L | ~483L | Suppression routes dupliquées (SYNCPLAY, VEGAFOX_HOME_ROWS_IMAGE) |

### Problèmes Résolus

| # | Problème | Résolution |
|---|----------|-----------|
| M1 | Toolbar dans Plugin | ✅ Déplacé vers Personnalisation |
| M2 | Home settings dans Plugin | ✅ Déplacé vers Personnalisation |
| M3 | Apparence dans Plugin | ✅ Déplacé vers Personnalisation |
| M6 | Confirmer sortie dans Plugin | ✅ Déplacé vers Personnalisation > Home |
| D1 | Routes.SYNCPLAY doublon | ✅ Mapping supprimé (const gardée pour compat) |
| D2/C1 | Routes.VEGAFOX_HOME_ROWS_IMAGE deprecated | ✅ Mapping supprimé (const gardée pour compat) |
| A1 | SettingsMainScreen 604L | ✅ Réduit à ~170L (dialogs extraits) |
| A2 | SettingsPluginScreen méga-écran | ✅ Réduit de 386→~240L |
| A3 | Helpers mal placés | ✅ Extraits vers SettingsLabelHelpers.kt |
| O1 | SyncPlay au root | ✅ Déplacé sous Playback |

### Problèmes Non Traités (hors scope)

| # | Problème | Raison |
|---|----------|--------|
| M4 | Ratings dans Plugin | Conservé dans Plugin (VegafoX-specific feature) |
| M5 | Episode preview dans Plugin | Conservé dans Plugin (Media Bar adjacent) |
| M7 | Live TV guide settings | Accessible uniquement depuis le guide — OK pour UX TV |
