# Audit Settings Final — Inventaire exhaustif pour refonte complète

**Date** : 2026-03-16
**Scope** : Tous les écrans Settings, toutes les préférences, architecture de navigation

---

## Point 1 — Inventaire exhaustif de toutes les options

### 1.1 Menu principal (`SettingsMainScreen.kt`)

| # | Label | Route | Type | Condition |
|---|-------|-------|------|-----------|
| 1 | Personnalisation | `/customization` | Navigation | Toujours |
| 2 | Écran de veille | `/customization/screensaver` | Navigation | Toujours |
| 3 | Lecture | `/playback` | Navigation | Toujours |
| 4 | Plugin VegafoX | `/plugin` | Navigation | Toujours |
| 5 | Authentification | `/authentication` | Navigation | Toujours |
| 6 | Vérifier MAJ | — | Action (dialog) | `ENABLE_OTA_UPDATES` |
| 7 | Notifications MAJ | `updateNotificationsEnabled` | Toggle | `ENABLE_OTA_UPDATES` |
| 8 | Développeur | `/developer` | Navigation | Toujours |
| 9 | À propos | `/about` | Navigation | Toujours |

---

### 1.2 Personnalisation (`/customization` — `SettingsCustomizationScreen.kt`)

#### Section : Navigation

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 1 | Librairies | — | Navigation | — | `/libraries` |
| 2 | Accueil | — | Navigation | — | `/home` |
| 3 | Thème de focus | `UserSettingPreferences.focusColor` | Navigation + valeur | `AppTheme` enum | `/customization/theme` |
| 4 | Affichage horloge | `UserPreferences.clockBehavior` | Navigation + valeur | `ALWAYS` | `/customization/clock` |
| 5 | Indicateur vu | `UserPreferences.watchedIndicatorBehavior` | Navigation + valeur | `ALWAYS` | `/customization/watch-indicators` |
| 6 | Afficher les fonds | `UserPreferences.backdropEnabled` | Toggle | `true` | — |
| 7 | Vignettes séries | `UserPreferences.seriesThumbnailsEnabled` | Toggle | `false` | — |
| 8 | Expansion au focus | `UserPreferences.cardFocusExpansion` | Toggle | `false` | — |

#### Section : Toolbar

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 9 | Position navbar | `UserPreferences.navbarPosition` | Navigation + valeur | `TOP` | `/vegafox/navbar-position` |
| 10 | Bouton Shuffle | `UserPreferences.showShuffleButton` | Toggle | `true` | — |
| 11 | Bouton Genres | `UserPreferences.showGenresButton` | Toggle | `true` | — |
| 12 | Bouton Favoris | `UserPreferences.showFavoritesButton` | Toggle | `true` | — |
| 13 | Librairies dans toolbar | `UserPreferences.showLibrariesInToolbar` | Toggle | `true` | — |
| 14 | Type contenu Shuffle | `UserPreferences.shuffleContentType` | Navigation + valeur | `"both"` | `/vegafox/shuffle-content-type` |

#### Section : Comportement Home

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 15 | Fusionner Continue/Next Up | `UserPreferences.mergeContinueWatchingNextUp` | Toggle | `false` | — |
| 16 | Multi-serveur | `UserPreferences.enableMultiServerLibraries` | Toggle | `false` | — |
| 17 | Vue dossiers | `UserPreferences.enableFolderView` | Toggle | `false` | — |
| 18 | Confirmer la sortie | `UserPreferences.confirmExit` | Toggle | `true` | — |

#### Section : Apparence

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 19 | Flou détails | `UserSettingPreferences.detailsBackgroundBlurAmount` | Navigation + valeur | `0` | `/vegafox/details-blur` |
| 20 | Flou navigation | `UserSettingPreferences.browsingBackgroundBlurAmount` | Navigation + valeur | `0` | `/vegafox/browsing-blur` |

---

### 1.3 Accueil (`/home` — `SettingsHomeScreen.kt`)

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 1 | Taille des affiches | `UserPreferences.posterSize` | Navigation + valeur | `DEFAULT` | `/home/poster-size` |
| 2 | Type d'image rangées | — | Navigation | — | `/home/rows-image-type` |
| 3 | Sections Home | `UserSettingPreferences.homeSectionsConfig` | Liste réordonnée + toggles | `HomeSectionConfig.defaults()` | — |
| 4 | Réinitialiser sections | — | Action | — | — |

---

### 1.4 Écran de veille (`/customization/screensaver` — `SettingsScreensaverScreen.kt`)

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 1 | Activer dans l'app | `screensaverInAppEnabled` | Toggle | `true` | — |
| 2 | Délai d'inactivité | `screensaverInAppTimeout` | Navigation + valeur | `5min` | `/customization/screensaver/timeout` |
| 3 | Mode | `screensaverMode` | Navigation + valeur | `"library"` | `/customization/screensaver/mode` |
| 4 | Niveau assombrissement | `screensaverDimmingLevel` | Navigation + valeur | `0` | `/customization/screensaver/dimming` |
| 5 | Exiger classification | `screensaverAgeRatingRequired` | Toggle | `true` | — |
| 6 | Classification max | `screensaverAgeRatingMax` | Navigation + valeur | `13` | `/customization/screensaver/age-rating` |
| 7 | Horloge sur écran de veille | `screensaverShowClock` | Toggle | `true` | — |

---

### 1.5 Lecture (`/playback` — `SettingsPlaybackScreen.kt`)

| # | Label | Pref Key | Type | Défaut | Route |
|---|-------|----------|------|--------|-------|
| 1 | Lecteur vidéo | (externe ou interne) | Navigation + icône app | Interne | `/playback/player` |
| 2 | Épisode suivant | — | Navigation | — | `/playback/next-up` |
| 3 | Inactivité | `stillWatchingBehavior` | Navigation + valeur | `DISABLED` | `/playback/inactivity-prompt` |
| 4 | Pré-rolls | — | Navigation | — | `/playback/prerolls` |
| 5 | Sous-titres | — | Navigation | — | `/customization/subtitles` |
| 6 | Sous-titres off par défaut | `subtitlesDefaultToNone` | Toggle | `false` | — |
| 7 | Segments média | — | Navigation | — | `/playback/media-segments` |
| 8 | SyncPlay | — | Navigation | — | `/vegafox/syncplay` |
| 9 | Avancé | — | Navigation | — | `/playback/advanced` |

---

### 1.6 Épisode suivant (`/playback/next-up` — `SettingsPlaybackNextUpScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | File d'attente média | `mediaQueuingEnabled` | Toggle | `true` |
| 2 | Comportement Next Up | `nextUpBehavior` | Navigation + valeur | `EXTENDED` |
| 3 | Délai Next Up | `nextUpTimeout` | Slider | `7000ms` (0-30s) |

---

### 1.7 Pré-rolls (`/playback/prerolls` — `SettingsPlaybackPrerollsScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Mode cinéma | `cinemaModeEnabled` | Toggle | `true` |

---

### 1.8 Inactivité (`/playback/inactivity-prompt` — `SettingsPlaybackInactivityPromptScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Comportement "Êtes-vous là ?" | `stillWatchingBehavior` | Radio list | `DISABLED` |

---

### 1.9 Segments média (`/playback/media-segments` — `SettingsPlaybackMediaSegmentsScreen.kt`)

| # | Label | Type |
|---|-------|------|
| 1-N | [Type de segment dynamique] | Navigation vers action par segment |

---

### 1.10 Sous-titres (`/customization/subtitles` — `SettingsSubtitlesScreen.kt`)

| # | Label | Pref Key | Type | Défaut | Range |
|---|-------|----------|------|--------|-------|
| 0 | Preview live | — | Composant | — | — |
| 1 | Taille du texte | `subtitlesTextSize` | Slider | `24f` | 8-32 |
| 2 | Poids du texte | `subtitlesTextWeight` | Slider | `400` | 100-900 |
| 3 | Couleur du texte | `subtitlesTextColor` | Navigation + ColorSwatch | `0xFFFFFFFF` | `/customization/subtitles/text-color` |
| 4 | Couleur de fond | `subtitlesBackgroundColor` | Navigation + ColorSwatch | `0x00FFFFFF` | `/customization/subtitles/background-color` |
| 5 | Couleur contour | `subtitleTextStrokeColor` | Navigation + ColorSwatch | `0xFF000000` | `/customization/subtitles/edge-color` |
| 6 | Position (offset bas) | `subtitlesOffsetPosition` | Slider | `0.08f` | 0-0.8 |

---

### 1.11 Lecture avancée (`/playback/advanced` — `SettingsPlaybackAdvancedScreen.kt`)

#### Section : Personnalisation

| # | Label | Pref Key | Type | Défaut | Range |
|---|-------|----------|------|--------|-------|
| 1 | Pré-lecture reprise | `resumeSubtractDuration` | Navigation + valeur | `"0"` | — |
| 2 | Durée avance rapide | `UserSettingPreferences.skipForwardLength` | Slider | — | 5-30s (pas 5) |
| 3 | Retour après dépause | `UserSettingPreferences.unpauseRewindDuration` | Slider | — | 0-10s (pas 1) |
| 4 | Description en pause | `UserSettingPreferences.showDescriptionOnPause` | Toggle | — | — |

#### Section : Vidéo

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 5 | Bitrate max | `maxBitrate` | Navigation + valeur | `"100"` |
| 6 | Résolution max | `maxVideoResolution` | Navigation + valeur | `AUTO` |
| 7 | Changement refresh rate | `refreshRateSwitchingBehavior` | Navigation + valeur | `DISABLED` |
| 8 | Délai démarrage vidéo | `videoStartDelay` | Slider | `0` (0-5s, pas 0.25) |
| 9 | Mode zoom | `playerZoomMode` | Navigation + valeur | `FIT` |
| 10 | PGS lecture directe | `pgsDirectPlay` | Toggle | `true` |
| 11 | ASS/SSA (LibAss) | `assDirectPlay` | Toggle | `false` |

#### Section : Audio / Live TV

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 12 | Sortie audio | `audioBehaviour` | Navigation + valeur | `DIRECT_STREAM` |
| 13 | Mode nuit audio | `audioNightMode` | Toggle | `false` |
| 14 | Live TV lecture directe | `liveTvDirectPlayEnabled` | Toggle | `true` |
| 15 | AC3 bitstream | `ac3Enabled` | Toggle | `true` |

#### Section : Dépannage

| # | Label | Type |
|---|-------|------|
| 16 | Rapport profil appareil | Action |

---

### 1.12 SyncPlay (`/vegafox/syncplay` — `SettingsVegafoXSyncPlayScreen.kt`)

| # | Label | Pref Key | Type | Défaut | Range |
|---|-------|----------|------|--------|-------|
| 1 | Activer SyncPlay | `syncPlayEnabled` | Toggle | `false` | — |
| 2 | Correction sync | `syncPlayEnableSyncCorrection` | Toggle | — | — |
| 3 | Vitesse pour sync | `syncPlayUseSpeedToSync` | Toggle | — | — |
| 4 | Délai min vitesse | `syncPlayMinDelaySpeedToSync` | Navigation | — | 10-1000 (pas 10) |
| 5 | Délai max vitesse | `syncPlayMaxDelaySpeedToSync` | Navigation | — | 10-1000 (pas 10) |
| 6 | Durée vitesse sync | `syncPlaySpeedToSyncDuration` | Navigation | — | 500-5000 (pas 100) |
| 7 | Saut pour sync | `syncPlayUseSkipToSync` | Toggle | — | — |
| 8 | Délai min saut | `syncPlayMinDelaySkipToSync` | Navigation | — | 10-5000 (pas 10) |
| 9 | Offset temps extra | `syncPlayExtraTimeOffset` | Navigation | — | -1000 à 1000 (pas 10) |

---

### 1.13 Plugin VegafoX (`/plugin` — `SettingsPluginScreen.kt`)

#### Section : Plugin Sync

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Sync avec serveur plugin | `pluginSyncEnabled` | Toggle | `false` |

#### Section : Media Bar

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 2 | Activer Media Bar | `UserSettingPreferences.mediaBarEnabled` | Toggle | — |
| 3 | Type contenu | `UserSettingPreferences.mediaBarContentType` | Navigation | `"movies"` |
| 4 | Nombre d'items | `UserSettingPreferences.mediaBarItemCount` | Navigation | `"10"` |
| 5 | Opacité overlay | `UserSettingPreferences.mediaBarOverlayOpacity` | Navigation | `50` |
| 6 | Couleur overlay | `UserSettingPreferences.mediaBarOverlayColor` | Navigation | `"black"` |
| 7 | Aperçu bande-annonce | `UserSettingPreferences.mediaBarTrailerPreview` | Toggle | — |
| 8 | Aperçu épisode | `UserSettingPreferences.episodePreviewEnabled` | Toggle | — |
| 9 | Audio aperçu | `UserSettingPreferences.previewAudioEnabled` | Toggle | — |

#### Section : Musique de thème

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 10 | Activer musique thème | `UserSettingPreferences.themeMusicEnabled` | Toggle | — |
| 11 | Sur rangées Home | `UserSettingPreferences.themeMusicOnHomeRows` | Toggle | — |
| 12 | Volume | `UserSettingPreferences.themeMusicVolume` | Navigation | `50` |

#### Section : Évaluations

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 13 | Évaluations supplémentaires | `UserSettingPreferences.enableAdditionalRatings` | Toggle | — |
| 14 | Labels d'évaluation | `UserSettingPreferences.showRatingLabels` | Toggle | — |
| 15 | Évaluations épisodes | `UserSettingPreferences.enableEpisodeRatings` | Toggle | — |

#### Section : Navigation

| # | Label | Type | Route |
|---|-------|------|-------|
| 16 | Jellyseerr | Navigation | `/jellyseerr` |
| 17 | Contrôles parentaux | Navigation | `/vegafox/parental-controls` |

---

### 1.14 Authentification (`/authentication` — `SettingsAuthenticationScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Connexion auto | `AuthenticationPreferences.autoLoginUserBehavior` | Navigation | `DISABLED` |
| 2 | Tri des comptes | `AuthenticationPreferences.sortBy` | Navigation | — |
| 3 | [Serveurs dynamiques] | — | Liste Navigation | — |
| 4 | Toujours authentifier | `AuthenticationPreferences.alwaysAuthenticate` | Toggle | — |
| 5 | Code PIN | — | Navigation | — |

---

### 1.15 Développeur (`/developer` — `SettingsDeveloperScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Mode debug | `debuggingEnabled` | Toggle | `false` |
| 2 | Désactiver alerte UI mode | `SystemPreferences.disableUiModeWarning` | Toggle | — |
| 3 | TrickPlay | `trickPlayEnabled` | Toggle | `false` |
| 4 | Préférer FFmpeg | `preferExoPlayerFfmpeg` | Toggle | `false` |
| 5 | Vider cache images | — | Action + taille | — |

---

### 1.16 Télémétrie (`/telemetry` — `SettingsTelemetryScreen.kt`)

| # | Label | Pref Key | Type | Défaut |
|---|-------|----------|------|--------|
| 1 | Rapports de crash | `TelemetryPreferences.crashReportEnabled` | Toggle | — |
| 2 | Inclure logs | `TelemetryPreferences.crashReportIncludeLogs` | Toggle | — |

**Note** : La télémétrie n'est PAS dans le menu principal mais la route et l'écran existent.

---

### 1.17 À propos (`/about` — `SettingsAboutScreen.kt`)

| # | Label | Type |
|---|-------|------|
| 1 | Logo + titre VegafoX | Affichage |
| 2 | Version app | Copiable |
| 3 | Modèle appareil | Copiable |
| 4 | Licences | Navigation → `/licenses` |

---

### 1.18 Live TV Guide Options (`/livetv/guide/options`)

| # | Label | Pref Key | Type |
|---|-------|----------|------|
| 1 | Ordre chaînes | `LiveTvPreferences.channelOrder` | Navigation |
| 2 | Favoris en haut | `LiveTvPreferences.favsAtTop` | Toggle |
| 3 | Code couleur guide | `LiveTvPreferences.colorCodeGuide` | Toggle |
| 4 | Indicateur HD | `LiveTvPreferences.showHDIndicator` | Toggle |
| 5 | Indicateur Live | `LiveTvPreferences.showLiveIndicator` | Toggle |
| 6 | Indicateur Nouveaux | `LiveTvPreferences.showNewIndicator` | Toggle |
| 7 | Indicateur Premières | `LiveTvPreferences.showPremiereIndicator` | Toggle |
| 8 | Indicateur Rediffusions | `LiveTvPreferences.showRepeatIndicator` | Toggle |

---

## Point 2 — Options obsolètes ou sans effet

### 2.1 Préférences mortes (jamais lues dans le code actif)

| Préférence | Clé | Problème | Recommandation |
|-----------|-----|----------|----------------|
| `cardFocusExpansion` | `pref_card_focus_expansion` | Définie et affichée dans Settings, mais **jamais lue** nulle part dans le code. L'effet d'expansion au focus n'est pas implémenté. | **SUPPRIMER** l'option des Settings et la préférence |
| `externalPlayerComponentName` | `external_player_component` | Jamais lue, jamais écrite, aucun UI | **SUPPRIMER** la préférence |
| `useExternalPlayer` | `external_player` | Lue dans `PlaybackLauncher.kt` mais **aucun UI pour l'écrire** — écrite uniquement par `SettingsPlaybackPlayerScreen.kt` via le mécanisme de sélection externe | Vérifier si le mécanisme d'external player est toujours fonctionnel |

### 2.2 Écrans dupliqués

| Écran 1 | Écran 2 | Problème | Recommandation |
|---------|---------|----------|----------------|
| `SettingsCustomizationClockScreen.kt` | `SettingsClockBehaviorScreen.kt` | Les deux font exactement la même chose : RadioButton list pour `ClockBehavior` | **SUPPRIMER** un des deux (garder `SettingsCustomizationClockScreen`) |
| `SettingsCustomizationWatchedIndicatorScreen.kt` | `SettingsWatchedIndicatorBehaviorScreen.kt` | Même duplication que l'horloge | **SUPPRIMER** un des deux |

### 2.3 Routes orphelines

| Route | Constante | Problème |
|-------|-----------|----------|
| `/telemetry` | `Routes.TELEMETRY` | Route définie dans `routes.kt` mais **aucun mapping** dans les fichiers de routes — jamais accessible |
| `/syncplay` | `Routes.SYNCPLAY` | Route définie mais **non mappée** — doublon avec `/vegafox/syncplay` |

### 2.4 Options à effet questionnable

| Option | Problème |
|--------|----------|
| `enableFolderView` | Lue dans `LeftSidebarNavigation.kt` pour afficher le bouton Dossiers — mais la feature est-elle testée et complète ? Actuellement masquée par défaut (`false`). |
| `seriesThumbnailsEnabled` | Lue uniquement dans `LeanbackChannelWorker.kt` — composant legacy. Possiblement sans effet dans l'UI Compose actuelle. |
| TrickPlay (`trickPlayEnabled`) | Marqué "Developer" mais c'est une feature utilisateur légitime. Pourrait être promu dans Lecture. |

---

## Point 3 — Analyse de la logique de classement actuel

### 3.1 Structure actuelle

```
Settings Main
├── Personnalisation              (19 options + 6 sous-écrans)
│   ├── Navigation (libraries, home, theme, clock, watched)
│   ├── Toolbar (navbar, shuffle, genres, favorites, libraries)
│   ├── Comportement Home (merge, multi-server, folder, exit)
│   └── Apparence (blur x2)
├── Écran de veille               (7 options + 4 sous-écrans)
├── Lecture                       (9 entrées)
│   ├── Lecteur vidéo
│   ├── Épisode suivant (3 options)
│   ├── Inactivité (1 option)
│   ├── Pré-rolls (1 option)
│   ├── Sous-titres (7 options + 3 sous-écrans)
│   ├── Segments média (dynamique)
│   ├── SyncPlay (9 options)
│   └── Avancé (16 options + 6 sous-écrans)
├── Plugin VegafoX               (17 options + 6 sous-écrans)
│   ├── Plugin Sync
│   ├── Media Bar (7 options)
│   ├── Aperçus & Musique (5 options)
│   ├── Évaluations (3 options)
│   ├── Jellyseerr (complexe)
│   └── Contrôles parentaux
├── Authentification              (5 options + serveurs dynamiques)
├── Développeur                   (5 options)
└── À propos                      (3 affichages + licences)
```

### 3.2 Incohérences identifiées

| Problème | Détail |
|----------|--------|
| **Personnalisation trop chargé** | 19 options réparties en 4 sections hétérogènes. Mélange toolbar, home, blur, navigation. Un utilisateur cherchant "position de la barre" doit fouiller dans "Personnalisation" alors que c'est un paramètre de navigation. |
| **"Plugin VegafoX" fourre-tout** | Mélange Media Bar (héros), musique de thème, évaluations, Jellyseerr, et contrôles parentaux. Aucun lien logique entre "couleur overlay Media Bar" et "contrôles parentaux". |
| **Sous-titres dans Lecture ET Personnalisation** | La route est `/customization/subtitles` mais le bouton est dans `/playback`. Incohérence de nommage. |
| **SyncPlay dans Lecture** | SyncPlay est une feature sociale, pas un paramètre de lecture. Se retrouve enterré au même niveau que "Bitrate max". |
| **Écran de veille au top-level** | L'écran de veille est promu au menu principal alors que c'est un sous-ensemble de l'apparence. |
| **Avancé = décharge** | 16 options mélangent vidéo (bitrate, résolution, zoom), audio (AC3, night mode), Live TV, et dépannage. |
| **TrickPlay en Développeur** | C'est une feature utilisateur (aperçu en seekbar) pas un réglage dev. |
| **Confirmer sortie dans Home** | "Confirmer la sortie de l'app" est dans la section "Comportement Home" de Personnalisation — pas intuitif. |
| **Pré-rolls seul** | Un sous-écran entier pour un seul toggle ("Mode cinéma"). |
| **Télémétrie invisible** | L'écran existe mais n'est lié à aucun menu. |

---

## Point 4 — Proposition de nouvelle architecture

### 4.1 Architecture redesignée

Inspiration : Netflix (simple, peu de niveaux), Apple TV (catégories claires), Disney+ (options pertinentes uniquement).

```
Settings
│
├── 1. APPARENCE
│   ├── Thème de focus (couleur accent)
│   ├── Flou arrière-plan détails
│   ├── Flou arrière-plan navigation
│   ├── Afficher les fonds d'écran
│   ├── Indicateur vu (toujours/home/jamais)
│   └── Horloge (toujours/lecture/jamais)
│
├── 2. ACCUEIL
│   ├── Taille des affiches
│   ├── Type d'image des rangées
│   ├── Sections Home (réordonnement + toggles)
│   ├── Fusionner Continue/Next Up
│   └── Réinitialiser sections
│
├── 3. NAVIGATION
│   ├── Position navbar (haut/gauche)
│   ├── Bouton Shuffle (toggle + type contenu)
│   ├── Bouton Genres
│   ├── Bouton Favoris
│   ├── Librairies dans toolbar
│   ├── Vue dossiers
│   └── Confirmer la sortie
│
├── 4. MEDIA BAR (héros)
│   ├── Activer
│   ├── Type contenu
│   ├── Nombre d'items
│   ├── Opacité overlay
│   ├── Couleur overlay
│   ├── Aperçu bande-annonce
│   ├── Aperçu épisode
│   └── Audio aperçu
│
├── 5. LECTURE
│   ├── Lecteur vidéo (interne/externe)
│   ├── Mode cinéma (pré-rolls)          ← fusionné, plus d'écran séparé
│   ├── Épisode suivant
│   │   ├── File d'attente
│   │   ├── Comportement Next Up
│   │   └── Délai Next Up
│   ├── Segments média
│   ├── Inactivité ("Êtes-vous là ?")
│   ├── Description en pause
│   ├── Durée avance rapide
│   └── Retour après dépause
│
├── 6. SOUS-TITRES
│   ├── Preview live
│   ├── Taille du texte
│   ├── Poids du texte
│   ├── Couleur du texte
│   ├── Couleur de fond
│   ├── Couleur contour
│   ├── Position (offset)
│   ├── Sous-titres off par défaut
│   ├── PGS lecture directe
│   └── ASS/SSA (LibAss)
│
├── 7. VIDÉO & AUDIO (ex-Avancé)
│   ├── Bitrate max
│   ├── Résolution max
│   ├── Mode zoom
│   ├── Changement refresh rate
│   ├── Délai démarrage vidéo
│   ├── Pré-lecture reprise
│   ├── Sortie audio
│   ├── Mode nuit audio
│   ├── AC3 bitstream
│   └── Live TV lecture directe
│
├── 8. MUSIQUE & AMBIANCE
│   ├── Musique de thème (toggle)
│   ├── Sur rangées Home
│   ├── Volume
│   ├── Évaluations supplémentaires
│   ├── Labels d'évaluation
│   └── Évaluations épisodes
│
├── 9. ÉCRAN DE VEILLE
│   ├── Activer dans l'app
│   ├── Délai
│   ├── Mode (bibliothèque/logo)
│   ├── Assombrissement
│   ├── Classification requise
│   ├── Classification max
│   └── Horloge
│
├── 10. LIBRAIRIES
│   ├── [Librairies dynamiques]
│   │   ├── Taille image
│   │   ├── Type image
│   │   ├── Direction grille
│   │   └── Masquer de la navbar
│   ├── Multi-serveur
│   └── Vignettes séries
│
├── 11. JELLYSEERR
│   ├── Activer
│   ├── URL serveur
│   ├── Connexion (Jellyfin/locale/API key)
│   ├── Bloquer NSFW
│   ├── Rangées Jellyseerr
│   └── Sync plugin VegafoX
│
├── 12. SYNCPLAY
│   ├── Activer
│   ├── Correction sync
│   ├── Vitesse pour sync + paramètres
│   ├── Saut pour sync + paramètres
│   └── Offset temps
│
├── 13. CONTRÔLES PARENTAUX
│   └── [Classifications dynamiques]
│
├── 14. COMPTES & CONNEXION
│   ├── Connexion auto
│   ├── Tri des comptes
│   ├── [Serveurs]
│   ├── Toujours authentifier
│   └── Code PIN
│
├── 15. AVANCÉ & DÉVELOPPEUR
│   ├── Mode debug
│   ├── TrickPlay (aperçu seekbar)
│   ├── Préférer FFmpeg
│   ├── Désactiver alerte UI mode
│   ├── Vider cache images
│   ├── Rapport profil appareil
│   ├── Rapports de crash
│   └── Notifications MAJ
│
└── 16. À PROPOS
    ├── Version
    ├── Appareil
    └── Licences
```

### 4.2 Décisions option par option

| Option actuelle | Décision | Destination nouvelle |
|----------------|----------|---------------------|
| Thème focus | **Conserver** | Apparence |
| Flou détails | **Conserver** | Apparence |
| Flou navigation | **Conserver** | Apparence |
| Afficher fonds | **Conserver** | Apparence |
| Indicateur vu | **Conserver** | Apparence |
| Horloge | **Conserver** | Apparence |
| Vignettes séries | **Déplacer** | Librairies (vérifier si actif) |
| **cardFocusExpansion** | **SUPPRIMER** | — (jamais lue) |
| Position navbar | **Déplacer** | Navigation |
| Shuffle (toggle + type) | **Fusionner** | Navigation (1 entrée avec sous-options) |
| Genres / Favoris / Libraries | **Déplacer** | Navigation |
| Vue dossiers | **Déplacer** | Navigation |
| Confirmer sortie | **Déplacer** | Navigation |
| Fusionner Continue/Next Up | **Déplacer** | Accueil |
| Multi-serveur | **Déplacer** | Librairies |
| Taille affiches | **Conserver** | Accueil |
| Media Bar (7 options) | **Promouvoir** | Section propre "Media Bar" |
| Aperçus + audio | **Déplacer** | Media Bar |
| Musique thème + volume | **Grouper** | Musique & Ambiance |
| Évaluations (3 options) | **Déplacer** | Musique & Ambiance |
| Mode cinéma | **Fusionner** dans Lecture | Plus d'écran séparé |
| PGS / ASS | **Déplacer** | Sous-titres (logique) |
| Description en pause | **Déplacer** | Lecture |
| Durée avance rapide | **Déplacer** | Lecture |
| TrickPlay | **Promouvoir** | Avancé & Développeur |
| Télémétrie | **Intégrer** | Avancé & Développeur |
| **externalPlayerComponentName** | **SUPPRIMER** | — (jamais utilisée) |
| **SettingsClockBehaviorScreen** | **SUPPRIMER** | Doublon |
| **SettingsWatchedIndicatorBehaviorScreen** | **SUPPRIMER** | Doublon |
| Route `/telemetry` | **SUPPRIMER** ou mapper | — |
| Route `/syncplay` | **SUPPRIMER** | Doublon de `/vegafox/syncplay` |

---

## Point 5 — Options manquantes

### 5.1 Sous-titres & Audio

| Option manquante | Justification | Priorité |
|-----------------|---------------|----------|
| **Langue préférée sous-titres** | L'utilisateur devrait pouvoir définir sa langue préférée (fr, en, etc.) sans devoir choisir à chaque lecture. Jellyfin API supporte `SubtitleLanguagePreference`. | P0 |
| **Langue préférée audio** | Idem pour l'audio. API : `AudioLanguagePreference`. | P0 |
| **Forcer sous-titres si audio différente** | Netflix le fait : si l'audio n'est pas dans la langue préférée, activer automatiquement les sous-titres. | P1 |
| **Style de contour sous-titres** | Actuellement outline ou none. Ajouter : drop shadow, raised, depressed (supportés par `CaptionStyleCompat`). | P2 |

### 5.2 Qualité & Réseau

| Option manquante | Justification | Priorité |
|-----------------|---------------|----------|
| **Qualité par défaut (WiFi vs mobile)** | Différencier bitrate max selon le type de connexion. | P1 |
| **Transcodage HDR→SDR** | Forcer le tone-mapping si l'écran ne supporte pas HDR. | P2 |
| **Codec vidéo préféré** | Permettre de préférer H.264 vs HEVC vs AV1 selon le matériel. | P3 |

### 5.3 Accessibilité

| Option manquante | Justification | Priorité |
|-----------------|---------------|----------|
| **Taille globale de la police** | Certains utilisateurs TV sont loin de l'écran. Scaling global des textes UI. | P1 |
| **Mode contraste élevé** | Augmenter le contraste des éléments UI pour les utilisateurs malvoyants. | P2 |
| **Audiodescription par défaut** | Préférer automatiquement les pistes audiodescription si disponibles. | P2 |
| **Navigation vocale / TalkBack hints** | Améliorer les content descriptions pour TalkBack. | P3 |

### 5.4 Expérience utilisateur

| Option manquante | Justification | Priorité |
|-----------------|---------------|----------|
| **Spoiler mode** | Masquer les synopsis, les miniatures d'épisodes non vus, et les durées restantes. Netflix/Plex le proposent. | P1 |
| **Ordre de tri par défaut** | Chaque librairie pourrait avoir un tri par défaut (titre, date ajout, date sortie, note). | P1 |
| **Grille vs liste** | Choix global d'affichage des médias. | P2 |
| **Animations réduites** | Pour les appareils lents (Fire Stick) ou les utilisateurs sensibles aux mouvements. | P2 |
| **Réinitialiser tous les paramètres** | Bouton de reset global dans À propos / Avancé. | P3 |

### 5.5 Réseau & Serveur

| Option manquante | Justification | Priorité |
|-----------------|---------------|----------|
| **Timeout de connexion** | Configurable pour les serveurs distants lents. | P2 |
| **Cache hors-ligne** | Permettre de mettre en cache des métadonnées pour un usage offline. | P3 |
| **Serveur de secours** | URL alternative si le serveur principal est inaccessible. | P3 |

---

## Synthèse chiffrée

| Métrique | Valeur |
|----------|--------|
| **Total options settings** | ~110 |
| **Options à supprimer** | 3 (cardFocusExpansion, externalPlayerComponentName, écrans dupliqués) |
| **Options à déplacer** | ~25 |
| **Options à fusionner** | 3 (pré-rolls dans Lecture, PGS/ASS dans Sous-titres, shuffle toggle+type) |
| **Sections actuelles** | 7 (+ 2 masquées) |
| **Sections proposées** | 16 |
| **Options manquantes identifiées** | 16 |
| **Routes totales** | 73 mappées + 2 orphelines |
| **Préférences mortes** | 2-3 confirmées |
