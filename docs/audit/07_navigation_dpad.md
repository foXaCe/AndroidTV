# Audit 07 - Navigation D-pad Android TV

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Toutes les corrections ont été vérifiées dans le code :
> - Focus scales standardisées (AnimationDefaults.FOCUS_SCALE = 1.06f)
> - switch_background.xml visible sans focus (ds_surface_dim)
> - Tous les layouts XML avec nextFocus* complets
> - Dialogs Jellyseerr avec setupFocusNavigation()
> - Aucun cul-de-sac de navigation restant

## Resume

Audit systematique et correction de la navigation D-pad sur l'ensemble de l'application Moonfin Android TV.
Objectif : garantir une experience premium a la telecommande sur tous les ecrans.

**Date** : 2026-03-07
**Compilation** : BUILD SUCCESSFUL apres toutes les corrections

---

## 1. Mecanismes de focus identifies

L'application utilise 5 mecanismes de focus coexistants :

| Mecanisme | Contexte | Fichiers |
|-----------|----------|----------|
| **Border focus** (`focusBorderColor()`) | Compose DS | Cards, toolbar, settings |
| **Scale zoom** (`AnimationDefaults.FOCUS_SCALE`) | Compose | UserCard, ColorSwatch, poster cards |
| **Background change** (`setOnFocusChangeListener`) | View/Dialog | Jellyseerr dialogs |
| **StateListDrawable** | XML/View | SeasonSelectionDialog, switch_background |
| **Leanback focus** (`FocusAwareCardContainer`) | Leanback rows | Home, browse |

### Constantes standardisees (AnimationDefaults.kt)

```kotlin
FOCUS_SCALE = 1.06f          // Compose elements
LEANBACK_FOCUS_SCALE = 1.15f // Leanback cards (CardPresenter)
DURATION_FAST = 150           // Quick transitions
DURATION_MEDIUM = 300         // Standard animations
DURATION_SLOW = 500           // Complex transitions
```

---

## 2. Corrections appliquees

### 2.1 Standardisation du focus scale

| Fichier | Avant | Apres | Justification |
|---------|-------|-------|---------------|
| `UserCardView.kt` | `1.1f` | `AnimationDefaults.FOCUS_SCALE` (1.06f) | Coherence DS |
| `MainToolbar.kt` (avatar) | `1.1f` | `AnimationDefaults.FOCUS_SCALE` (1.06f) | Coherence DS |
| `SubtitleColorPresetsControl.kt` | `1.25f` | `AnimationDefaults.FOCUS_SCALE` (1.06f) | Coherence DS |
| `SeasonSelectionDialog.kt` (checkboxes) | `1.1f` | `1.06f` | Coherence DS |
| `LibraryBrowseComponents.kt` (poster card) | `1.08f` | `AnimationDefaults.FOCUS_SCALE` (1.06f) | Coherence DS |

**Non modifies intentionnellement :**
- `RangeControl.kt` knob : `1.75f` — sert a indiquer la position du curseur, pas un focus D-pad
- `LyricsBox.kt` : `1.1f` — highlight de la lyrique active, pas un focus interactif
- `CardPresenter.kt` : utilise `LEANBACK_FOCUS_SCALE` (1.15f) — standard Leanback

### 2.2 Corrections XML : routes de focus (nextFocus*)

#### `fragment_server_add.xml`
- EditText `address` : ajoute `nextFocusUp="@id/address"` (auto-reference pour ne pas s'echapper)
- Button `confirm` : ajoute `nextFocusUp="@id/address"` et `nextFocusDown="@id/confirm"`

#### `dialog_create_playlist.xml`
- `public_playlist_container` : ajoute `nextFocusUp="@id/playlist_name_input"` et `nextFocusDown="@id/button_cancel"`
- `button_cancel` : ajoute `nextFocusDown="@id/button_cancel"`, `nextFocusLeft="@id/button_cancel"`
- `button_back` : ajoute `nextFocusLeft="@id/button_cancel"`, `nextFocusRight="@id/button_create"`
- `button_create` : ajoute `nextFocusDown="@id/button_create"`, `nextFocusLeft="@id/button_back"`, `nextFocusRight="@id/button_create"`

#### `genres_grid_browse.xml`
- `libraryFilterContainer` : ajoute `nextFocusRight="@id/sortContainer"`, `nextFocusLeft` (auto), `nextFocusDown="@id/gridContainer"`
- `sortContainer` : ajoute `nextFocusLeft="@id/libraryFilterContainer"`, `nextFocusRight` (auto), `nextFocusDown="@id/gridContainer"`

#### `fragment_select_server.xml`
- `enter_server_address` : ajoute `nextFocusUp="@id/stored_servers"`, `nextFocusDown="@id/discovery_servers"`

### 2.3 Focus visibility

#### `switch_background.xml`
- Etat non-focus : change de `@android:color/transparent` a `@color/ds_surface_dim`
- Le switch est maintenant visible meme sans focus (important pour TV ou pas de curseur)

### 2.4 Focus navigation programmatique (Dialogs Jellyseerr)

#### `SeasonSelectionDialog.kt`
- Ajoute methode `setupFocusNavigation()` apres creation du contenu
- Navigation verticale chainee : selectAll → checkboxes → cancel/confirm
- Navigation horizontale entre cancel et confirm
- Chaque vue recoit un `View.generateViewId()` pour les references nextFocus*

#### `AdvancedRequestOptionsDialog.kt`
- Ajoute `setupButtonFocusNavigation()` pour cancel/confirm (navigation horizontale)
- Ajoute `setupContentFocusNavigation()` appelee apres `buildContent()` pour chainer :
  - profileButtons → rootFolderButtons → cancel/confirm (vertical)
  - cancel ↔ confirm (horizontal)
- Pattern copie de `QualitySelectionDialog.kt` qui avait deja un bon `setupFocusNavigation()`

#### `QualitySelectionDialog.kt` (deja correct)
- Possede deja `setupFocusNavigation()` avec pattern View.generateViewId() + nextFocus*
- Focus initial sur le premier bouton actif
- Sert de reference pour les autres dialogs

---

## 3. Ecrans audites

### 3.1 Ecrans corriges

| Ecran | Problemes | Corrections |
|-------|-----------|-------------|
| Server selection | Pas de nextFocus entre stored_servers, button, discovery_servers | Ajoute nextFocusUp/Down |
| Server add | EditText/Button sans nextFocus | Ajoute routing complet |
| Create playlist dialog | 3 boutons sans nextFocus horizontal | Chaine completo horizontale + verticale |
| Genres grid | Filter/Sort sans nextFocus vers grid | Ajoute nextFocus bi-directionnel |
| Season selection dialog | Pas de navigation chainee, scale 1.1f | setupFocusNavigation(), scale 1.06f |
| Advanced request dialog | Pas de navigation chainee | setupButtonFocusNavigation() + setupContentFocusNavigation() |
| Library browse (poster cards) | Scale 1.08f | AnimationDefaults.FOCUS_SCALE |
| User card | Scale 1.1f | AnimationDefaults.FOCUS_SCALE |
| Toolbar (avatar) | Scale 1.1f | AnimationDefaults.FOCUS_SCALE |
| Subtitle color presets | Scale 1.25f | AnimationDefaults.FOCUS_SCALE |
| Switch background | Invisible sans focus | ds_surface_dim |

### 3.2 Ecrans deja corrects

| Ecran | Raison |
|-------|--------|
| Home (HomeFragment/HomeRowsFragment) | Leanback BrowseSupportFragment gere le focus nativement |
| Playback overlay | CustomPlaybackTransportControlGlue avec navigation Leanback |
| Search | SearchSupportFragment Leanback natif |
| Settings (toutes) | Compose settings screens avec `focusRestorer()` et `autoFocus` |
| Item details (v2) | Compose avec focusBorderColor() et theme DS |
| Discover (Jellyseerr) | Leanback RowsSupportFragment |
| Quality selection dialog | setupFocusNavigation() deja implemente |
| Sidebar navigation | `LeftSidebarNavigation` avec focus chain Compose |

---

## 4. Gestion du bouton Back

### Pattern existant (deja correct)
- **HomeFragment** : `OnBackPressedCallback` avec confirmation de sortie (double Back)
- **Dialogs** : `onKeyDown(KEYCODE_BACK)` → `dismiss()` (pas de sortie app)
- **Playback** : Back retourne aux details, pas a l'ecran d'accueil
- **Search** : Back retourne au Home (comportement Leanback natif)

### Bouton Menu
- `UserCardView.kt` : `KEYCODE_MENU` → `performLongClick()` (popup menu)
- `CardPresenter` : long press sur cartes pour menu contextuel

---

## 5. Parcours de navigation documentes

### Flow 1 : Home → Film → Lecture

```
[Home]
  ↓ D-pad Down : focus premiere carte du premier row
  ↓ D-pad Down : rows suivants (Leanback scroll vertical)
  → D-pad Right : cartes dans le row (Leanback scroll horizontal)
  ↵ Enter : ouvre ItemDetailsFragment

[Details Film]
  ↓ Focus initial : bouton Play (autoFocus)
  ↓ D-pad Down : boutons secondaires (shuffle, trailer, etc.)
  ↓ D-pad Down : info rows (casting, similar, etc.)
  ← D-pad Left/Right : navigation horizontale dans les rows
  ↵ Enter sur Play : lance VideoPlayerActivity

[Playback]
  ↓ Focus initial : barre de transport (Leanback)
  ← → : seek dans la timeline
  ↑ ↓ : actions secondaires (CC, audio, etc.)
  ⮐ Back : retour aux details
```

### Flow 2 : Home → Recherche

```
[Home]
  ↑ D-pad Up depuis premiere row : focus toolbar
  → Navigation toolbar : search icon → libraries → user avatar
  ↵ Enter sur search : ouvre SearchFragment

[Search]
  ↓ Focus initial : champ de recherche (Leanback SearchBar)
  ↓ D-pad Down : resultats en rows
  → D-pad Right : cartes dans les resultats
  ↵ Enter sur carte : ouvre details
  ⮐ Back : retour Home
```

### Flow 3 : Home → Settings

```
[Home]
  ↑ D-pad Up : toolbar
  → Navigation vers icone settings
  ↵ Enter : ouvre SettingsMainScreen

[Settings]
  ↓ Focus initial : premier item (focusRestorer)
  ↓ D-pad Down : parcours vertical des options
  ↵ Enter : ouvre sous-ecran
  ⮐ Back : retour ecran parent

[Sous-ecrans Settings]
  ↓ Meme pattern : liste verticale + Enter pour actions
  ↓ Toggles/switches : Enter pour basculer
  ⮐ Back : retour settings parent
```

---

## 6. Cas particuliers et limitations connues

### Live TV Guide
- `overlay_tv_guide.xml` : containers avec `focusable="false"` — c'est intentionnel car le focus est gere par le EPG custom (programmatic)
- La grille de programmes utilise sa propre logique de navigation (scroll temporel + canal)

### Leanback vs Compose bridge
- `FocusAwareCardContainer` dans `CardPresenter` fait le pont entre focus Leanback et Compose
- Le focus Leanback (1.15x scale) est different du focus Compose (1.06x) — c'est voulu pour respecter les conventions de chaque framework

### Ecrans Jellyseerr (addView imperatif)
- Ces dialogs n'utilisent pas le Design System Compose, ils construisent les vues programmatiquement
- Le focus est gere par `setOnFocusChangeListener` avec changement de background color
- Les `setupFocusNavigation()` ajoutees utilisent `View.generateViewId()` + `nextFocus*Id`

---

## 7. Metriques finales

| Metrique | Avant | Apres |
|----------|-------|-------|
| Valeurs de scale non-standard | 5 | 0 |
| Dialogs sans focus chain | 2 | 0 |
| Layouts XML sans nextFocus | 4 | 0 |
| Elements invisibles sans focus | 1 | 0 |
| Compilation | OK | OK |
