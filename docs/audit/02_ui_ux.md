# Audit 02 — Coherence UI/UX & Navigation D-pad

> **Mis à jour le 2026-03-08 — état post-travaux**
> - ✅ Résolu : Couleurs hardcodées Compose 131 → 0 (hors définitions)
> - ✅ Résolu : Couleurs hardcodées XML 16 → 0
> - ✅ Résolu : Color.parseColor Jellyseerr 100+ → 1 (GenreCardPresenter)
> - ✅ Résolu : RoundedCornerShape inline 70+ → 3 (utilitaires)
> - ✅ Résolu : fontSize inline 187 → 0
> - ✅ Résolu : JellyfinBlue constant supprimée
> - ✅ Résolu : Toolbar colors dédupliquées → OverlayColors.kt
> - ✅ Résolu : Dialog pattern → shapes.dialog partout
> - ✅ Résolu : TypographyTokens dp → sp
> - ✅ Résolu : RadiusTokens 128dp → sémantiques
> - ✅ Résolu : Focus scales standardisées → AnimationDefaults.FOCUS_SCALE
> - ⚠️ Partiel : theme_mutedpurple.xml (#212121 inline)
> - ⚠️ Partiel : 3 RoundedCornerShape restantes (Popover, SearchTextInput, ListColorChannelRangeControl)
> - ❌ En attente : GenreCardPresenter.kt:67 Color.parseColor("#1a1a1a")
> - ❌ En attente : ItemDetailsFragment.kt:257 Color.parseColor("#0A0A0A")
> - ❌ En attente : 3 paradigmes UI coexistent (Compose themed, Leanback, Jellyseerr imperatif)
> - **Score de cohérence visuelle mis à jour : 8.5/10** (était 3.5/10)

**Projet** : VegafoX for Android TV (fork Jellyfin)
**Date** : 2026-03-07
**Version auditee** : branche `main` (commit 8b15bd57d)

---

## Resume executif

L'application presente **trois paradigmes UI coexistants** qui creent une incoherence visuelle majeure :

1. **Compose v2** (ItemDetails, Library, Home) — le plus moderne, utilise partiellement JellyfinTheme
2. **Leanback legacy** (BrowseSupportFragment, grilles) — systeme de cartes/zoom propre a Android TV
3. **Imperatif addView()** (Jellyseerr) — 161 appels addView/LinearLayout dans 9 fichiers, zero design system

Le design system (`design` module + `JellyfinTheme` + `Shapes`) est **bien concu mais massivement ignore**. Sur ~200 fichiers Compose, seuls 36 utilisent `JellyfinTheme` et 17 utilisent les `Tokens`.

**Score de coherence visuelle : 3/10**

---

## 1. Inventaire des incoherences visuelles

### 1.1 Couleurs hardcodees (131 occurrences dans 17+ fichiers)

Le ColorScheme de JellyfinTheme definit ~30 couleurs semantiques, mais la majorite des fichiers hardcodent directement les couleurs :

| Couleur hardcodee | Occurrences | Fichiers principaux |
|---|---|---|
| `Color(0xFF00A4DC)` (jellyfin_blue) | ~15 | DonateDialog, CreatePlaylistDialog, AddToPlaylistDialog, LibraryBrowseComponents, ItemDetailsComponents |
| `Color(0xE6141414)` (fond dialog) | ~8 | ShuffleOptionsDialog, DonateDialog, AddToPlaylistDialog, ExitConfirmationDialog, GenresGridV2Fragment |
| `Color(0xFF1A2332)` et variantes toolbar | 10+10 | MainToolbar:280-289, MediaBarSlideshowView:105-114 (duplique!) |
| `Color(0xFF111111)` (fond sombre) | 2 | ItemDetailsComponents:516, 626 |
| `Color(0xFF0F3460)` (gradient bleu) | 4 | ItemDetailsFragment:510-512, 1683-1685 |
| `Color(0xFF2196F3)` / `Color(0xFFFF4757)` | 4 | ItemDetailsFragment:1111, 1121, 1559, 1567 |
| `Color(0xFFFFD700)` / `Color(0xFFFFC107)` | 2 | MediaBarSlideshowView:533, rating.kt:358 |

**Probleme critique** : La couleur principale `jellyfin_blue` (#00A4DC) est definie a 4 endroits differents :
- `colors.xml:3` → `@color/jellyfin_blue`
- `colorScheme.kt` → via Tokens
- `LibraryBrowseComponents.kt:74` → `val JellyfinBlue = Color(0xFF00A4DC)` (constante locale!)
- Chaque dialog la redefine inline

**Couleurs XML dupliquees** :
- `colors.xml` : 84 couleurs definies
- `theme_emerald.xml` : 3 couleurs supplementaires
- `theme_mutedpurple.xml` : 2 couleurs supplementaires + 1 hex inline (`#384873`)
- **Aucun lien** entre `colors.xml` et le module `design/Tokens.ColorTokens`

**InfoRowColors** (inforow/InfoRowColors.kt) definit ses propres couleurs hardcodees au lieu d'utiliser le theme :
```kotlin
val Default = Color(0xB3FFFFFF) to Color.Black
val Green = Color(0xB3089562) to Color.White
val Red = Color(0xB3F2364D) to Color.White
```

### 1.2 Typographie incoherente (187 fontSize hardcodees dans 37 fichiers)

Le systeme typographique (`typography.kt`) definit 5 styles : ListHeader(15sp), ListOverline(10sp), ListHeadline(14sp), ListCaption(11sp), Badge(11sp). Le module `design` definit TypographyTokens (2xs=10, xs=12, sm=14, md=16, lg=18, xl=20, 2xl=24, 3xl=32).

**Aucun des deux n'est respecte.** Les tailles de police sont systematiquement hardcodees :

| Fichier | Nombre de fontSize hardcodees |
|---|---|
| ItemDetailsComponents.kt | 33 |
| ItemDetailsFragment.kt | 15 |
| LibraryBrowseComponents.kt | 14 |
| GenresGridV2Fragment.kt | 12 |
| ScheduleBrowseFragment.kt | 5 |
| MediaBarSlideshowView.kt | 5 |
| DonateDialog.kt | 5 |
| RecordingsBrowseFragment.kt | 6 |
| LiveTvBrowseFragment.kt | 6 |
| MusicBrowseFragment.kt | 6 |
| SeriesRecordingsBrowseFragment.kt | 7 |

Tailles utilisees dans le code (echantillon non exhaustif) : 8sp, 10sp, 11sp, 12sp, 13sp, 14sp, 15sp, 16sp, 17sp, 18sp, 20sp, 22sp, 32sp, 34sp, 36sp, 48sp — soit **16 tailles differentes** sans echelle typographique coherente.

**BUG critique dans TypographyTokens** : Toutes les tailles utilisent `dp` au lieu de `sp`, ce qui empeche le scaling d'accessibilite.

### 1.3 Dimensions hardcodees (1 320 .dp/.sp dans 40+ fichiers Compose + 387 dans 30 XML)

`dimens.xml` ne definit que **5 dimensions** :
- `overscan_horizontal` = 27dp
- `overscan_vertical` = 48dp
- `home_row_spacing` = 24dp / 32dp
- `lb_browse_padding_start` = 61dp

Tout le reste est hardcode. Les fichiers les plus problematiques (Compose) :

| Fichier | Nombre de .dp/.sp |
|---|---|
| ItemDetailsComponents.kt | 169 |
| ItemDetailsFragment.kt | 77 |
| LibraryBrowseComponents.kt | 71 |
| GenresGridV2Fragment.kt | 60 |
| MediaDetailsFragment.kt (Jellyseerr) | 50 |
| MediaBarSlideshowView.kt | 37 |
| SettingsMainScreen.kt | 34 |
| RecordingsBrowseFragment.kt | 34 |
| DonateDialog.kt | 33 |
| CreatePlaylistDialog.kt | 33 |
| LiveTvBrowseFragment.kt | 32 |
| LeftSidebarNavigation.kt | 31 |
| MusicBrowseFragment.kt | 31 |

**SpaceTokens ignore** : Le module design definit 8 tokens d'espacement (2xs=2, xs=4, sm=8, md=16, lg=24, xl=32, 2xl=40, 3xl=48), mais seuls 17 fichiers les utilisent (56 occurrences) contre 40 fichiers avec des valeurs hardcodees (313 occurrences).

### 1.4 Formes (shapes) incoherentes — 70+ RoundedCornerShape hardcodees

Le systeme `Shapes` definit 5 niveaux : ExtraSmall(4dp), Small(8dp), Medium(12dp), Large(16dp), ExtraLarge(28dp).

Mais dans le code, on trouve des `RoundedCornerShape()` avec **au moins 8 rayons differents** :

| Rayon | Occurrences | Usage semantique |
|---|---|---|
| 3.dp | 2 | InfoRowItem, ServerButtonView |
| 4.dp | 12 | SkipOverlay, CardOverlay, badges, tabs |
| 6.dp | 8 | ItemDetailsComponents (boutons action) |
| 8.dp | 15 | ItemDetailsComponents, RecordingsBrowse, playlists |
| 12.dp | 5 | MediaBarSlideshow, CreatePlaylistDialog |
| 14.dp | 2 | ItemDetailsComponents (bouton principal) |
| 20.dp | 10 | Tous les dialogs (fond arrondi) |
| 28.dp | 2 | LibraryBrowseFragment (search bar) |

**Mapping vers Shapes** : 4dp=ExtraSmall, 8dp=Small, 12dp=Medium, 28dp=ExtraLarge. Mais 3dp, 6dp, 14dp, 20dp n'ont aucun equivalent et ne sont jamais utilises via `JellyfinTheme.shapes`.

**RadiusTokens defectueux** : `radiusDefault = 128.dp` est anormalement grand et inutilisable.

### 1.5 Trois paradigmes UI — comparaison detaillee

#### Paradigme A : Compose + JellyfinTheme (recommande)
- **Fichiers** : ItemCard.kt, Button.kt, IconButton.kt, Badge.kt, Seekbar.kt, ListItemContent.kt (~15 fichiers `ui/base/`)
- **Focus** : `focusBorderColor()` → bordure 2dp configurable
- **Couleurs** : `JellyfinTheme.colorScheme.*`
- **Formes** : `JellyfinTheme.shapes.*`
- **Verdict** : Coherent en interne, mais sous-utilise

#### Paradigme B : Compose sans design system
- **Fichiers** : ItemDetailsFragment.kt (1800+ lignes), ItemDetailsComponents.kt (1300+ lignes), LibraryBrowseComponents.kt (800+ lignes), tous les dialogs, MediaBarSlideshowView.kt
- **Focus** : Mix de `focusBorderColor()` et couleurs hardcodees
- **Couleurs** : 92 Color(0x...) hardcodees
- **Formes** : 70+ RoundedCornerShape() inline
- **Verdict** : Chaque fichier reinvente son propre style

#### Paradigme C : Imperatif addView() (Jellyseerr)
- **Fichiers** : 9 fichiers dans `ui/jellyseerr/` (161 appels addView/Layout)
- **Focus** : Gestion propre via Android View system
- **Couleurs** : Style "Tailwind CSS" avec couleurs web (#7C3AED violet, etc.)
- **Formes** : Rayon via `GradientDrawable`
- **Verdict** : Completement deconnecte du reste de l'app

### 1.6 Couleurs XML layouts hardcodees

16 couleurs hex directement dans les layouts XML :

| Fichier | Couleurs |
|---|---|
| jellyseerr_genre_card.xml | `#000000`, `#FFFFFF` |
| jellyfin_genre_card.xml | `#1a1a1a`, `#FFFFFF`, `#CCCCCC`, `#000000` |
| fragment_jellyseerr_requests.xml | `#7C3AED` |
| fragment_home.xml | `#000000` (shadowColor) |
| vlc_player_interface.xml | `#B3FFFFFF` |
| fragment_jellyseerr_discover_new.xml | `#000000` (4x shadowColor) |

### 1.7 Themes XML : divergences entre variants

3 themes coexistent : Jellyfin (defaut), Emerald, MutedPurple.

| Propriete | Jellyfin | Emerald | MutedPurple |
|---|---|---|---|
| cardRounding | 4dp | 4dp (herite) | **5dp** |
| buttonRounding | 10dp | 10dp (herite) | **7dp** |
| inputRounding | 10dp | 10dp (herite) | **7dp** |
| popupMenuRounding | 10dp | 10dp (herite) | **7dp** |
| popupMenuBackground | #202124 | #000000 | **#384873 (inline!)** |
| tiles | 3 couleurs variees | Tout `theme_emerald_light` | 1 seul override (#212121) |

MutedPurple utilise des arrondis differents (7dp vs 10dp) et une couleur hex inline au lieu d'une reference.

### 1.8 Styles XML dupliques

`styles.xml` definit `player_progress` et `overlay_progress` qui sont **strictement identiques** :
```xml
<style name="player_progress" parent="android:Widget.ProgressBar.Horizontal">
    <item name="android:progressDrawable">@drawable/progress_bar</item>
    <item name="android:minHeight">5sp</item>
    <item name="android:maxHeight">5sp</item>
</style>
<style name="overlay_progress" parent="android:Widget.ProgressBar.Horizontal">
    <!-- identique -->
</style>
```

`PopupMenu` et `PopupWindow` partagent la meme definition et hardcodent `@color/indigo_dye` comme textColor.

### 1.9 Couleur dupliquee MainToolbar / MediaBarSlideshowView

Les couleurs de toolbar background sont definies **identiquement** a deux endroits :

- `MainToolbar.kt:280-289` — 10 couleurs hardcodees
- `MediaBarSlideshowView.kt:105-114` — les memes 10 couleurs, copier-coller

### 1.10 Animations incoherentes

206 drawables dans `res/drawable/`, dont 2 animated drawables et 6 fichiers `res/anim/`.

**Durees** : Globalement coherentes (300ms pour transitions, 200ms pour fades d'etat).

**Facteurs de scale au focus** — non standardises :

| Composant | Scale au focus | Fichier |
|---|---|---|
| Leanback cards | 1.15x (115%) | `dimens.xml` (card_scale_focus) |
| User avatar (MainToolbar) | 1.10x | `MainToolbar.kt` |
| Subtitle color swatches | 1.25x | `SubtitleColorPresetsControl.kt` |
| RangeControl knob | 1.75x | `RangeControl.kt` |
| User card | 1.10x | `UserCardView.kt` |

**Recommandation** : Definir un `AnimationDefaults` object avec les constantes de scale et duree.

**Drawables avec duplication** :
- `jellyfin_button.xml`, `jellyfin_button_minimal.xml`, `button_bar_back.xml`, `button_default_back.xml` partagent des patterns similaires
- 20+ fichiers `tile_*.xml` partagent la meme structure (layer-list avec icone sur fond colore)
- `button_icon_tint.xml` et `button_icon_tint_animated.xml` ne different que par la presence d'animation (200ms fade)

### 1.11 Icones vectorielles

106 icones vectorielles dans `res/drawable/ic_*.xml` :
- 96 fichiers (90%) en 24x24dp — **standard respecte**
- 4 fichiers en 20x20dp
- 6 fichiers de tailles variables (logos, banners)
- **Couleur hardcodee** : Toutes en blanc (`#FFFFFF`) — non theme-aware
- **Pas de variantes** outlined/filled — un seul style

---

## 2. Composants a unifier

### 2.1 Systeme de cartes

| Composant actuel | Fichier source | Probleme |
|---|---|---|
| `ItemCard` (Compose) | `ui/composable/item/ItemCard.kt` | Bien concu, utilise JellyfinTheme |
| `CardPresenter` (Leanback→Compose) | `ui/presentation/CardPresenter.kt` | Bridge correct, utilise ItemCard |
| `GridButtonPresenter` | `ui/presentation/GridButtonPresenter.kt` | Inline Compose, RoundedCornerShape(4.dp) hardcode |
| Cards Jellyseerr | `ui/jellyseerr/MediaContentAdapter.kt` | XML layout `item_jellyseerr_content.xml`, zero integration |
| Genre cards Jellyfin | Layout `jellyfin_genre_card.xml` | Couleurs hardcodees, pas de lien avec ItemCard |
| Genre cards Jellyseerr | Layout `jellyseerr_genre_card.xml` | Style different des genre cards Jellyfin |

**Action** : Unifier toutes les cartes vers `ItemCard` avec des variantes parametriques.

### 2.2 Systeme de boutons

| Composant actuel | Fichier source | Probleme |
|---|---|---|
| `Button` (Compose) | `ui/base/button/Button.kt` | Utilise JellyfinTheme — reference |
| `IconButton` (Compose) | `ui/base/button/IconButton.kt` | Utilise JellyfinTheme — reference |
| `Button.Default` (XML style) | `styles.xml:41-53` | Padding different (24dp vs 16dp Compose) |
| `Button.Icon` (XML style) | `styles.xml:55-65` | Systeme completement separe |
| Boutons Jellyseerr | Inline dans chaque Fragment | `GradientDrawable` avec couleurs Tailwind |
| Boutons ItemDetails | `ItemDetailsComponents.kt:91-120` | `focusBorderColor()` + `RoundedCornerShape(14.dp)` au lieu de JellyfinTheme.shapes |

**Action** : Migrer vers le composant `Button` de `ui/base/button/` partout.

### 2.3 Systeme de dialogs

| Dialog | Fichier source | Fond | Rayon |
|---|---|---|---|
| ShuffleOptionsDialog | `ui/shuffle/ShuffleOptionsDialog.kt` | `Color(0xE6141414)` | 20.dp |
| DonateDialog | `ui/preference/category/DonateDialog.kt` | `Color(0xE6141414)` | 20.dp |
| AddToPlaylistDialog | `ui/playlist/AddToPlaylistDialog.kt` | `Color(0xE6141414)` | 20.dp |
| CreatePlaylistDialog | `ui/playlist/CreatePlaylistDialog.kt` | `Color(0xE6141414)` | 20.dp |
| ExitConfirmationDialog | `ui/browsing/ExitConfirmationDialog.kt` | `Color(0xE6141414)` | 20.dp |
| ItemDetailsComponents (2 dialogs) | `ui/itemdetail/v2/ItemDetailsComponents.kt:1122, 1252` | `Color(0xE6141414)` | 20.dp |
| GenresGridV2Fragment (2 dialogs) | `ui/browsing/v2/GenresGridV2Fragment.kt:512, 636` | `Color(0xE6141414)` | 20.dp |
| QualitySelectionDialog (Jellyseerr) | `ui/jellyseerr/QualitySelectionDialog.kt` | Via XML/addView() | Differents |
| SeasonSelectionDialog (Jellyseerr) | `ui/jellyseerr/SeasonSelectionDialog.kt` | Via XML/addView() | Differents |

Tous les dialogs Compose copient-collent le meme pattern `Color(0xE6141414)` + `RoundedCornerShape(20.dp)`.

**Action** : Creer un composant `DialogBase` (existe deja dans `ui/base/dialog/DialogBase.kt` !) et l'utiliser partout.

### 2.4 Systeme de focus — 5 mecanismes differents

| Mecanisme | Utilise dans | Effet visuel |
|---|---|---|
| `focusBorderColor()` + `border(2.dp)` | ItemCard, ItemDetailsComponents, certains boutons | Bordure coloree configurable par l'utilisateur |
| Leanback `ZOOM_FACTOR` 115% | Toutes les lignes Leanback (dimens.xml) | Scale up sans bordure |
| Background color change (Compose) | Button, IconButton, ListControl, GlassDialogButton | Fond change (gris→blanc ou couleur accent) |
| XML `state_focused` drawables | 13 drawables (button_default_back, input_default_back, etc.) | Changement de couleur de fond + bordure optionnelle |
| Scale animation (Compose) | UserCardView (1.1x), RangeControl knob (1.75x) | Agrandissement anime |

**Tableau de comparaison detaille :**

| Type de composant | Focus = bordure | Focus = fond | Focus = scale | Focus = texte seul |
|---|---|---|---|---|
| Cards (Compose/ItemCard) | 2dp bordure coloree | - | - | - |
| Cards (Leanback rows) | - | - | 115% zoom | - |
| Buttons (Compose base) | - | buttonFocused color | - | - |
| Buttons (XML/legacy) | optionnel | highlight background | - | - |
| GlassDialogButton | bordure alpha 0.3 | fond blanc 0.15 | - | - |
| Icon buttons (XML) | - | - | - | tint color change |
| Input fields (XML) | 2dp border color | - | - | - |
| Switch (XML) | - | **transparent!** | - | - |
| User cards | 2dp border | - | 1.1x scale | marquee text |
| Seekbar knob | - | - | 1.75x scale | - |
| List items (settings) | - | colorBluegrey800 | - | - |
| Dialog rows | - | white 0.12 alpha | - | - |

**Incoherence majeure** : Dans une meme page (Home), les cartes zooment a 115% (Leanback) tandis que dans la page Details, les memes types de cartes utilisent une bordure coloree (Compose). L'utilisateur voit deux systemes de focus differents pour le meme type de contenu.

**Point positif** : `FocusAwareCardContainer` (CardPresenter.kt:91-108) est une solution sophistiquee qui resout correctement le conflit Leanback/Compose ou `FocusHighlightHelper` ecrase les listeners de focus.

### 2.5 Overlays de cartes

| Overlay | Fichier source | Usage |
|---|---|---|
| `ItemCardBaseItemOverlay` | `ui/composable/item/ItemCardBaseItemOverlay.kt` | Progress, badges, etat de lecture |
| `ItemCardJellyseerrOverlay` | `ui/composable/item/ItemCardJellyseerrOverlay.kt` | Badge "MOVIE"/"SERIES" avec couleurs hardcodees |
| `EpisodePreviewOverlay` | `ui/composable/item/EpisodePreviewOverlay.kt` | Preview video au focus |
| `SeriesTrailerOverlay` | `ui/composable/item/SeriesTrailerOverlay.kt` | Preview trailer au focus |

Les couleurs des overlays Jellyseerr (`Color(0xFF3B82F6)`, `Color(0xFF8B5CF6)`) sont completement differentes de la palette du theme.

---

## 3. Problemes de navigation D-pad

**Evaluation globale** : La gestion du focus est **globalement bonne** dans les zones Compose et Leanback grace a des solutions comme `FocusAwareCardContainer`, `focusRestorer()`, `focusGroup()` et le pattern `OnBackPressedCallback`. Les points faibles sont concentres dans Jellyseerr (imperatif) et les layouts XML sans routage explicite.

### 3.1 Points forts (a preserver)

| Mecanisme | Localisation | Qualite |
|---|---|---|
| **FocusAwareCardContainer** | `CardPresenter.kt:91-108` | Solution elegante au conflit Leanback/Compose |
| **focusRestorer()** | `VideoPlayerControls.kt`, `Toolbar.kt`, `SearchFragment.kt` | Preservation du focus lors des transitions de visibilite |
| **focusGroup()** dans DialogBase | `ui/base/dialog/DialogBase.kt` | Contention correcte du focus dans les dialogs |
| **OnBackPressedCallback** | `MainActivity.kt` | Pattern moderne avec confirmation de sortie |
| **autoFocus modifier** | `ui/base/modifier/autoFocus.kt` | Focus automatique a l'affichage |
| **D-pad dans RangeControl** | `ui/base/form/RangeControl.kt` | Gestion Left/Right pour ajuster les valeurs |
| **FocusBorderColor** configurable | `ui/base/FocusBorderColor.kt` | Couleur de focus personnalisable par utilisateur |

### 3.2 CRITIQUE

| Probleme | Localisation | Impact |
|---|---|---|
| **Jellyseerr UI entierement en addView()** | `ui/jellyseerr/` (9 fichiers) | Boutons crees programmatiquement avec `isFocusable = true` mais **sans nextFocus equivalent** |
| **MediaDetailsFragment : 79 addView()** | `ui/jellyseerr/MediaDetailsFragment.kt` | Page complexe construite imperativement — risque de focus trap |
| **SeasonSelectionDialog : 13 addView()** | `ui/jellyseerr/SeasonSelectionDialog.kt` | CheckBox focusability definie programmatiquement (lignes 91-92, 111-112) |
| **AdvancedRequestOptionsDialog : 21 addView()** | `ui/jellyseerr/AdvancedRequestOptionsDialog.kt` | Boutons avec `isFocusable = true` (l:172, 200) mais pas de routage D-pad |
| **Live TV Guide : focus bloque** | `live_tv_guide.xml`, `overlay_tv_guide.xml` | Tous les containers ont `focusable="false"` — grille 2D sans gestion de focus declarative |

### 3.3 MAJEUR

| Probleme | Localisation | Impact |
|---|---|---|
| **nextFocus incomplet dans dialog_create_playlist.xml** | `dialog_create_playlist.xml:101-134` | `button_create` n'a que `nextFocusUp` — impossible de naviguer vers le bas ou lateralement |
| **nextFocus manquant dans fragment_server_add.xml** | `fragment_server_add.xml:76-77` | `nextFocusDown/Forward` definis mais pas `nextFocusUp` pour la navigation inverse |
| **fragment_select_server.xml sans routage** | `fragment_select_server.xml` | 2 RecyclerViews + 1 Button sans aucun nextFocus — navigation imprevisible |
| **Switch focusable=false dans container focusable** | `dialog_create_playlist.xml:77-78` | Switch non-focusable dans un LinearLayout focusable — conflit de hierarchie |
| **KeyEvent dispatch manuel** | `CustomPlaybackOverlayFragment.java:150-151` | Synthese manuelle de DPAD_CENTER events — workaround fragile |
| **Compose clickable() sans focusable() explicite** | GenresGridV2, MusicBrowse, LiveTvBrowse, SeriesRecordingsBrowse | `.clickable()` rend focusable implicitement mais devrait etre explicite pour Android TV |

### 3.4 MINEUR

| Probleme | Localisation | Impact |
|---|---|---|
| **dimens.xml minimaliste** | `res/values/dimens.xml` (5 entrees) | Pas de qualificateurs TV-specifiques (sw720dp, sw1080dp) |
| **Marquee infini au focus** | `CardPresenter.kt:475-478, 513-516` | `basicMarquee(iterations = Int.MAX_VALUE)` — peut etre distrayant |
| **Styles identiques player/overlay_progress** | `styles.xml:12-22` | Code mort ou doublon |
| **Overscan fixe** | `dimens.xml` (27x48dp) | Valeur unique pour toutes les resolutions TV |

---

## 4. Synthese et metriques

### 4.1 Metriques de coherence

| Metrique | Valeur | Cible |
|---|---|---|
| Fichiers Compose utilisant JellyfinTheme | 36 / ~200 | 200 / 200 |
| Fichiers Compose utilisant Tokens | 17 / ~200 | 200 / 200 |
| Couleurs hardcodees (Compose) | 131 | 0 |
| fontSize hardcodees (Compose) | 187 | 0 |
| Dimensions hardcodees (Compose .dp/.sp) | 1 320 | 0 |
| Dimensions hardcodees (XML) | 387 | < 50 |
| RoundedCornerShape inline | 70+ | 0 |
| Rayons de coins differents | 8 valeurs | 5 (Shapes system) |
| Tailles de police differentes | 16 valeurs | 5-7 (echelle typographique) |
| Paradigmes UI coexistants | 3 | 1 |
| Couleurs de dialog identiques copy-paste | 8 fichiers | 1 composant partage |
| Couleurs toolbar dupliquees | 2 fichiers (copier-coller) | 1 source de verite |
| Drawables avec focus states | 13 fichiers | coherent |
| Mecanismes de focus visuels differents | 5 types | 1-2 types |
| Facteurs de scale au focus | 4 valeurs (1.1x, 1.15x, 1.25x, 1.75x) | 1 valeur standard |

### 4.2 Fichiers les plus problematiques (dette technique UI)

1. **ItemDetailsComponents.kt** (1300+ lignes) — 169 .dp, 33 fontSize, 12 Color(0x), 38 RoundedCornerShape
2. **ItemDetailsFragment.kt** (1800+ lignes) — 77 .dp, 15 fontSize, 10 Color(0x), gradients hardcodes
3. **LibraryBrowseComponents.kt** (800+ lignes) — 71 .dp, 14 fontSize, 4 Color(0x), constantes locales JellyfinBlue/NavyBackground
4. **GenresGridV2Fragment.kt** — 60 .dp, 12 fontSize, 2 Color(0x)
5. **MediaDetailsFragment.kt** (Jellyseerr) — 50 .dp, 79 addView(), zero design system
6. **MediaBarSlideshowView.kt** — 37 .dp, 5 fontSize, 11 Color(0x), 10 couleurs toolbar dupliquees
7. **MainToolbar.kt** — 11 Color(0x), 10 couleurs toolbar dupliquees
8. **DonateDialog.kt** — 33 .dp, 5 fontSize, 4 Color(0x), pattern dialog copie-colle
9. **CreatePlaylistDialog.kt** — 33 .dp, 6 fontSize, 5 Color(0x), Color(0xFF00A4DC) reutilise 3x
10. **view_row_details.xml** (XML) — 65 dimensions hardcodees, padding repete

### 4.3 Score de coherence visuelle : 3.5/10

**Justification :**

- **+2 points** : Le design system existe et est bien concu (JellyfinTheme, Shapes, ColorScheme, 165 Tokens)
- **+1 point** : Les composants de base (ItemCard, Button, IconButton, Badge) respectent le theme
- **+0.5 point** : La gestion du focus D-pad est globalement solide (FocusAwareCardContainer, focusRestorer, focusGroup)
- **-2 points** : 131 couleurs hardcodees, aucune source de verite respectee
- **-2 points** : 187 fontSize + 1 320 .dp sans tokens — aucune echelle respectee
- **-1 point** : 3 paradigmes UI coexistants (Compose themed, Compose inline, Imperatif)
- **-1 point** : 5 mecanismes de focus visuels differents (bordure, zoom, background, scale, texte)
- **-1 point** : Jellyseerr completement deconnecte du langage visuel (161 addView, couleurs Tailwind)
- **-1 point** : Themes XML divergents (MutedPurple change les arrondis, Emerald tous tiles monochrome)
- **-0.5 point** : BUG TypographyTokens en `dp` au lieu de `sp`
- **-0.5 point** : Massive duplication (dialogs, toolbar colors, drawables boutons)

### 4.4 Priorites de remediation

#### P0 — Blocant
1. **Corriger TypographyTokens** : `dp` → `sp` pour l'accessibilite
2. **Corriger RadiusTokens** : `radiusDefault = 128.dp` est inutilisable

#### P1 — Critique (coherence visuelle)
1. **Centraliser les couleurs** : Supprimer les 131 Color(0x) inline → utiliser ColorScheme. Ajouter `dialogBackground`, `dialogBorder`, `accentColor` au ColorScheme
2. **Centraliser les fontSize** : Definir une echelle typographique complete (ex: 10, 12, 14, 16, 20, 24, 32, 48sp) dans typography.kt et l'utiliser partout
3. **Utiliser JellyfinTheme.shapes** : Remplacer les 70+ RoundedCornerShape inline. Ajouter `dialog` (20dp) a Shapes
4. **Creer un DialogBase standard** : Factoriser le pattern 0xE6141414 + RoundedCornerShape(20.dp) — `DialogBase` existe deja, l'etendre avec GlassDialogButton/GlassDialogRow
5. **Dedupliquer les couleurs toolbar** : MainToolbar.kt + MediaBarSlideshowView.kt → un seul objet `ToolbarColors`
6. **Standardiser les scales de focus** : Definir un `AnimationDefaults` object (ex: focusScale = 1.1f, focusDuration = 200ms)

#### P2 — Majeur (unification)
1. **Migrer Jellyseerr vers Compose** : 161 addView() → composants du design system
2. **Unifier le systeme de focus** : Choisir entre zoom et bordure, pas les deux. Documenter la strategie dans un design spec
3. **Migrer les layouts XML** vers Compose avec tokens de dimensions (387 hardcoded dans 30 fichiers XML)
4. **Enrichir SpaceTokens** : Ajouter les valeurs manquantes (12dp, 14dp, 20dp) et les utiliser
5. **Ajouter nextFocus explicite** dans dialog_create_playlist.xml, fragment_select_server.xml, fragment_server_add.xml
6. **Ajouter `.focusable()` explicite** dans GenresGridV2Fragment, MusicBrowseFragment, LiveTvBrowseFragment, SeriesRecordingsBrowseFragment
7. **Corriger le focus du Switch** dans `switch_background.xml` (transparent au focus → invisible)

#### P3 — Mineur (nettoyage)
1. Supprimer le style `overlay_progress` (doublon de `player_progress`)
2. Supprimer la constante locale `JellyfinBlue` dans LibraryBrowseComponents.kt
3. Corriger le hex inline dans theme_mutedpurple.xml (`#384873`)
4. Aligner les arrondis de MutedPurple (7dp) sur le systeme standard (10dp ou Shapes)
5. Ajouter qualificateurs TV-specifiques dans `dimens.xml` (sw720dp, sw1080dp)
6. Consolider les drawables boutons dupliques (jellyfin_button.xml, button_bar_back.xml, button_default_back.xml)
7. MutedPurple : overrider les 17 tiles manquants (actuellement fallback au parent)
