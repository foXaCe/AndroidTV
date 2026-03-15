# Audit Live TV — Inventaire avant migration Compose

**Date** : 2026-03-09
**Objectif** : Inventaire complet du code Live TV avant planification de la migration Compose

---

## 1. Fichiers source Live TV

### 1.1 Package `ui/livetv/`

| Fichier | LOC | Rôle | Compose ? | Custom View / RecyclerView ? |
|---------|-----|------|-----------|------------------------------|
| `LiveTvGuide.kt` | 13 | Interface commune guide (displayChannels, setSelectedProgram, etc.) | Non | Non |
| `LiveTvGuideFragment.kt` | 704 | Fragment principal du guide TV plein écran | Non | Custom LinearLayout grille |
| `LiveTvGuideFragmentHelper.kt` | 143 | Extensions du fragment (toggleFavorite, refreshSelectedProgram, addSettings*) | **Oui** (SettingsDialog via ComposeView) | Non |
| `TvManager.kt` | 242 | Singleton — état global chaînes/programmes, focus grille, timeline | Non | Non |
| `TvManagerHelper.kt` | 123 | Top-level functions API (loadLiveTvChannels, getPrograms, getScheduleRows) | Non | Non |
| `GuideFilters.kt` | 137 | Gestion filtres guide (movies, news, sports, etc.) + persistence prefs | Non | Non |

### 1.2 Settings screens (`ui/settings/screen/livetv/`)

| Fichier | LOC | Rôle | Compose ? |
|---------|-----|------|-----------|
| `SettingsLiveTvGuideOptionsScreen.kt` | 84 | Options guide (tri, favoris top, couleurs, indicateurs) | **Oui** |
| `SettingsLiveTvGuideFiltersScreen.kt` | 49 | Filtres guide (movies, series, news, etc.) | **Oui** |
| `SettingsLiveTvGuideChannelOrderScreen.kt` | 46 | Sélection ordre chaînes (radio buttons) | **Oui** |

### 1.3 Custom Views Live TV (`ui/`)

| Fichier | LOC | Rôle | Compose ? | Custom View ? |
|---------|-----|------|-----------|---------------|
| `ProgramGridCell.kt` | 166 | Cellule programme dans la grille (RelativeLayout) | Non | **Oui** — RelativeLayout custom avec focus, couleurs, indicateurs |
| `GuideChannelHeader.kt` | 73 | En-tête chaîne à gauche de la grille (RelativeLayout) | Non | **Oui** — RelativeLayout custom avec image async, favoris |
| `LiveProgramDetailPopup.kt` | 274 | Popup détail programme (PopupWindow) | Non | **Oui** — PopupWindow avec boutons dynamiques |
| `RecordPopup.kt` | 229 | Popup options enregistrement (PopupWindow) | Non | **Oui** — PopupWindow avec Spinners, CheckBoxes |
| `FriendlyDateButton.kt` | 37 | Bouton date dans le date picker | Non | **Oui** — Button custom |
| `GuidePagingButton.kt` | 47 | Bouton pagination haut/bas dans la grille | Non | **Oui** — Button custom |
| `ObservableScrollView.kt` | 19 | ScrollView avec listener de scroll | Non | **Oui** — ScrollView custom |
| `ObservableHorizontalScrollView.kt` | 19 | HorizontalScrollView avec listener de scroll | Non | **Oui** — HorizontalScrollView custom |
| `RecordingIndicatorView.kt` | 6 | Interface pour setRecTimer/setRecSeriesTimer | Non | Non |

### 1.4 Guide overlay dans le player (`ui/playback/`)

| Fichier | LOC total | LOC guide estimé | Rôle |
|---------|-----------|------------------|------|
| `CustomPlaybackOverlayFragment.kt` | 1 365 | ~550 | Fragment playback qui **implémente aussi LiveTvGuide** — duplique la logique grille |
| `CustomPlaybackOverlayFragmentHelper.kt` | 253 | ~20 | Extensions (forceReload x3) |
| `overlay/LeanbackOverlayFragment.kt` | 202 | ~5 | Appel `TvManager.getPrevLiveTvChannel()` |

### 1.5 Totaux

| Catégorie | Fichiers | LOC |
|-----------|----------|-----|
| Package `livetv/` | 6 | 1 362 |
| Settings Compose | 3 | 179 |
| Custom Views | 9 | 870 |
| Guide overlay (player) | 3 | ~575 |
| **TOTAL Live TV** | **21** | **~2 986** |

---

## 2. Layouts XML Live TV

| Layout | Root View | Composants principaux | Référencé par |
|--------|-----------|----------------------|---------------|
| `live_tv_guide.xml` | `RelativeLayout` | ObservableScrollView (channels), ObservableScrollView > ObservableHorizontalScrollView (programs), HorizontalScrollView (timeline), AsyncImageView, title/summary TextViews, 4 ImageButtons (options/filter/date/reset), spinner, **2 ComposeViews** (settingsFilters, settingsOptions) | `LiveTvGuideFragment.kt` (via ViewBinding) |
| `overlay_tv_guide.xml` | `RelativeLayout` | Même structure que live_tv_guide mais pour overlay : channels, programs, timeline scrollers, guideTitle, guideCurrentTitle, spinner (pas de ComposeViews) | `CustomPlaybackOverlayFragment.kt` (via ViewBinding) |
| `channel_header.xml` | `RelativeLayout` | channelName (TextView), channelNumber (TextView), channelImage (AsyncImageView), favImage (ImageView) | `GuideChannelHeader.kt` |
| `program_grid_cell.xml` | `RelativeLayout` | programName (TextView), infoRow (LinearLayout), recIndicator (ImageView) | `ProgramGridCell.kt` |
| `program_detail_popup.xml` | `RelativeLayout` | title, infoRow, summary, timeline (LinearLayout), recordLine, buttonRow, similarRow | `LiveProgramDetailPopup.kt` |
| `new_program_record_popup.xml` | `RelativeLayout` | title, timeline, prePadding (Spinner), postPadding (Spinner), onlyNew/anyTime/anyChannel (CheckBoxes), okButton, cancelButton | `RecordPopup.kt` |

---

## 3. TvManager — Usage Map

### 3.1 Lecture d'état

| Méthode | Appelant | Contexte |
|---------|----------|----------|
| `getAllChannels()` | `LiveTvGuideFragment` | Après chargement, stocke dans `allChannels` local |
| `getAllChannels()` | `CustomPlaybackOverlayFragment` | Idem pour overlay guide |
| `getChannel(i)` | `LiveTvGuideFragment` | Itération pour afficher les rangées |
| `getChannel(i)` | `CustomPlaybackOverlayFragment` | Idem |
| `getAllChannelsIndex(id)` | `LiveProgramDetailPopup` | Trouver la chaîne du programme sélectionné |
| `getProgramsForChannel()` | `LiveTvGuideFragment` | Avec filtres, pour chaque chaîne |
| `getProgramsForChannel()` | `CustomPlaybackOverlayFragment` | Sans filtres |
| `getLastLiveTvChannel()` | `LiveTvGuideFragment` | Focus initial sur dernière chaîne vue |
| `getLastLiveTvChannel()` | `CustomPlaybackOverlayFragment` | Idem |
| `getPrevLiveTvChannel()` | `LeanbackOverlayFragment` | Bouton « chaîne précédente » |
| `shouldForceReload()` | `LiveTvGuideFragment` | Vérifier si rechargement nécessaire |

### 3.2 Écriture d'état

| Méthode | Appelant | Contexte |
|---------|----------|----------|
| `setLastLiveTvChannel(id)` | `PlaybackController` | À chaque tune sur une chaîne live |
| `forceReload()` | `LiveTvGuideFragmentHelper` (x2) | Après toggle favorite, après dismiss settings |
| `forceReload()` | `CustomPlaybackOverlayFragmentHelper` (x3) | Après actions enregistrement |
| `forceReload()` | `LiveTvGuideFragment` (x3) | onDestroy si date future, pageGuideTo, doLoad |
| `loadAllChannels()` | `LiveTvGuideFragment` | Chargement initial/refresh |
| `loadAllChannels()` | `CustomPlaybackOverlayFragment` | Chargement guide overlay |

### 3.3 Side effects (UI)

| Méthode | Appelant | Description |
|---------|----------|-------------|
| `setTimelineRow()` | `LiveProgramDetailPopup` | Construit la ligne timeline (TextViews) dans le popup détail |
| `setFocusParams()` | `LiveTvGuideFragment` | Chaîne les focus up/down entre les rangées de programmes |
| `setFocusParams()` | `CustomPlaybackOverlayFragment` | Idem pour overlay |
| `getProgramsAsync()` | `LiveTvGuideFragment` | Charge les programmes puis appelle `displayProgramsAsync()` |
| `getProgramsAsync()` | `CustomPlaybackOverlayFragment` | Idem pour overlay |

---

## 4. Complexité de migration vers Compose

| Composant | LOC | Complexité | Bloquants Compose |
|-----------|-----|------------|-------------------|
| **Grille horaire** (LiveTvGuideFragment + overlay) | ~1 254 | **TRÈS ÉLEVÉE** | Scroll synchronisé H+V, cellules de largeur variable (durée en minutes), focus D-pad inter-rangées, performance 100+ chaînes |
| `ProgramGridCell` | 166 | Élevée | Focus custom (onFocusChanged), couleurs dynamiques, indicateurs conditionnels, largeur proportionnelle au temps |
| `GuideChannelHeader` | 73 | Moyenne | Focus custom, image async, sync scroll vertical avec grille |
| `LiveProgramDetailPopup` | 274 | Élevée | PopupWindow positionnée manuellement, boutons dynamiques, cascade vers RecordPopup |
| `RecordPopup` | 229 | Moyenne | PopupWindow, Spinners, CheckBoxes — migration Compose standard |
| `TvManager` (singleton) | 242 | Élevée | État global mutable, `@Volatile`, pas de StateFlow — migration vers ViewModel/StateFlow nécessaire |
| `GuideFilters` | 137 | Faible | Logique pure + prefs — facile à extraire |
| `TvManagerHelper` | 123 | Faible | Appels API coroutine — déjà idiomatique |
| Settings Compose (3 screens) | 179 | **DÉJÀ MIGRÉ** | — |
| Views auxiliaires (FriendlyDateButton, GuidePagingButton, Observable*) | 128 | Faible | Remplacement trivial en Compose |

### Bloquants critiques identifiés

1. **Grille horaire — scroll synchronisé 2D**
   - La grille actuelle utilise `ObservableScrollView` + `ObservableHorizontalScrollView` avec des listeners bidirectionnels
   - En Compose : pas d'équivalent natif pour un scroll 2D synchronisé avec items de taille variable
   - Options : `LazyColumn` + `LazyRow` imbriqués (problème de performance), `Canvas` custom, ou **conserver la grille en View dans un `AndroidView`**

2. **Focus D-pad sur grille à cellules variables**
   - `TvManager.setFocusParams()` chaîne manuellement `nextFocusUpId`/`nextFocusDownId` entre les rangées
   - En Compose TV (`tv-foundation`), `FocusGroup` + `focusRequester` existent mais la gestion de cellules de largeur variable (durée programme) reste complexe
   - Le focus doit « sauter » à la cellule qui chevauche temporellement la cellule source

3. **Performance : 100+ chaînes × 24h**
   - Actuellement : ajout dynamique de Views dans `LinearLayout` avec coroutine `Dispatchers.Default`
   - En Compose : `LazyColumn` gère la virtualisation, mais chaque rangée est elle-même un `Row` non-lazy de largeur variable
   - Risque de jank si toute la grille est recomposée

4. **Duplication guide plein écran / overlay**
   - `LiveTvGuideFragment` et `CustomPlaybackOverlayFragment` implémentent tous deux `LiveTvGuide`
   - ~550 LOC dupliqués dans le fragment overlay
   - Migration = opportunité de factoriser en un composable réutilisable

5. **PopupWindows**
   - `LiveProgramDetailPopup` et `RecordPopup` utilisent `PopupWindow` Android natif
   - Compose offre `Popup` et `Dialog` mais le positionnement exact (Gravity.NO_GRAVITY + coordonnées) est plus délicat

---

## 5. Recommandation : migration par composant (pas tout d'un coup)

### Approche recommandée : migration incrémentale en 4 étapes

**Étape A — Refactoring état (sans toucher l'UI)**
- Migrer `TvManager` singleton → `LiveTvGuideViewModel` avec `StateFlow`
- Extraire `GuideFilters` dans le ViewModel
- Factoriser la logique commune entre `LiveTvGuideFragment` et `CustomPlaybackOverlayFragment`
- **Risque** : faible (pas de changement UI)

**Étape B — Popups et composants simples → Compose**
- `RecordPopup` → Compose `Dialog`
- `LiveProgramDetailPopup` → Compose `Popup` ou `Dialog`
- `FriendlyDateButton` → Composable
- `GuidePagingButton` → Composable
- **Risque** : modéré (les popups sont isolés)

**Étape C — En-têtes et cellules → Compose**
- `GuideChannelHeader` → Composable
- `ProgramGridCell` → Composable
- Garder la grille en View mais utiliser `ComposeView` pour chaque cellule via `AbstractComposeView`
- **Risque** : élevé (performance, focus D-pad)

**Étape D — Grille complète → Compose**
- Remplacer les `ObservableScrollView` par une solution Compose
- Solution probable : `LazyColumn` pour les chaînes + `Row` non-lazy pour les programmes (taille fixe en dp)
- Factoriser le guide en un seul composable utilisé par les deux fragments
- **Risque** : très élevé — nécessite prototypage et benchmarks

### Pourquoi PAS tout d'un coup ?

1. **La grille est le composant le plus risqué** — la migrer en isolation permet de revenir en arrière
2. **Les popups sont indépendants** — migration rapide et testable séparément
3. **Le ViewModel est un prérequis** — la grille Compose aura besoin de StateFlow
4. **Test D-pad critique** — chaque étape doit être validée sur device TV physique

---

## 6. Risques identifiés

| # | Risque | Impact | Probabilité | Mitigation |
|---|--------|--------|-------------|------------|
| 1 | Performance grille Compose avec 100+ chaînes | Élevé | Moyenne | Prototyper avec `LazyColumn` + benchmark avant commit |
| 2 | Focus D-pad cellules variables en Compose TV | Élevé | Élevée | Implémenter un `FocusManager` custom, garder `setFocusParams` logic |
| 3 | Scroll synchronisé H+V impossible en Compose natif | Élevé | Élevée | Option fallback : `AndroidView` wrapping la grille View existante |
| 4 | Régression overlay guide pendant playback | Élevé | Moyenne | Tests manuels exhaustifs sur Fire TV + Ugoos |
| 5 | PopupWindow positionnement en Compose | Faible | Faible | `Popup` Compose avec `offset` ou `IntOffset` |
| 6 | Duplication LiveTvGuide interface break | Moyen | Faible | Extraire interface avant migration |
| 7 | `TvManager` état global race conditions pendant migration | Moyen | Moyenne | Migrer vers ViewModel en premier (Étape A) |

---

## 7. Dépendances externes

- `org.jellyfin.sdk` : `liveTvApi` (channels, programs, timers) — stable, pas de risque
- `androidx.compose.ui.platform.ComposeView` : déjà utilisé dans `live_tv_guide.xml` pour les settings
- `tv-foundation` (Compose TV) : nécessaire pour l'étape D si on veut un focus D-pad natif
- Pas de RecyclerView dans le code Live TV actuel (tout est LinearLayout dynamique)
