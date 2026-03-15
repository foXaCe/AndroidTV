# Audit final — Ressources orphelines post-cleanup 01-06

**Date** : 2026-03-13
**Scope** : Tout ce qui n'a pas été couvert par cleanup_01 à cleanup_06
**Action** : Audit uniquement — aucune modification

---

## Rappel : ce qui a déjà été nettoyé (cleanup 01-06)

| Phase | Focus | LOC supprimées |
|-------|-------|----------------|
| 01 | Dead code, ViewModels orphelins, couleurs timeline/guide | ~337 |
| 02 | Tokens.Color → VegafoX, 61 couleurs ds_*, 5 drawables | ~169 |
| 03 | 71 imports inutilisés, lint baseline | ~69 |
| 04 | Helpers Java cassés, overlay, fragments startup | ~3,124 |
| 05 | 310 strings orphelines, 6,751 traductions, layouts restaurés | ~1,581 |
| 06 | 2 dépendances Gradle, 6 layouts, 3 animations | ~369 |
| **Total** | | **~5,649 LOC + 6,751 traductions** |

---

## ÉTAPE 1 — Ressources non-string orphelines

### 1a. Dimens (58/70 orphelins — 83% de déchet)

**Fichier** : `app/src/main/res/values/dimens.xml` — 70 entrées

**Utilisés (12)** :
| Nom | Réfs | Usage |
|-----|------|-------|
| ds_space_xs | 5 | Padding, marges |
| ds_space_sm | 19 | Espacement le plus courant |
| ds_space_md | 18 | Espacement moyen |
| ds_space_lg | 16 | Grand espacement |
| ds_space_xl | 1 | Rare |
| ds_text_headline_md | 4 | Typographie |
| ds_text_body_lg | 8 | Corps de texte |
| ds_text_body_md | 26 | Taille de texte la plus utilisée |
| ds_text_body_sm | 3 | Petit texte |
| ds_text_label_sm | 2 | Labels |
| overscan_horizontal | 2 | Legacy TV overscan |
| overscan_vertical | 2 | Legacy TV overscan |

**Orphelins (58)** :
- 9 variantes d'espacement : ds_space_2xs, ds_space_6/10/12/14/20, ds_space_2xl/3xl/4xl
- 8 rayons de coins : ds_radius_none/xs/sm/md/lg/xl/2xl/full
- 7 tailles typographiques : ds_text_display, headline_lg, title_lg/md/sm, label_lg/md
- 29 tokens composants : ds_icon_*, ds_button_*, ds_card_width_*, ds_toolbar_*, ds_dialog_*, ds_divider_*, ds_elevation_*, ds_anim_*
- 5 legacy : home_row_spacing, home_row_spacing_large, lb_browse_padding_start, card_scale_*, lb_focus_zoom_factor_*

**Priorité : P1** — Design system surdimensionné, migration Compose terminée

---

### 1b. Styles / Themes (17-20/32 orphelins — 53-63%)

**Fichier** : `app/src/main/res/values/styles.xml` — 32 définitions

**Utilisés (actifs via référence directe ou alias)** :
| Nom | Réfs | Usage |
|-----|------|-------|
| DatePickerCustom | 1 | Spinner date picker |
| Widget.Jellyfin.Row.Header | 1 | Row header theming |
| WindowAnimation.SlideBottom | 1 | Animation fenêtre |
| Button.Default | multiple | Alias → Style.DS.Button.Primary |
| Button.Icon / Button.Icon.Small | multiple | Alias → Style.DS.Button.Icon |
| Input.Default | 1 | Alias → Style.DS.Input |
| PopupMenu / PopupWindow | 1 chacun | Alias → Style.DS.PopupMenu |

**Orphelins confirmés** :
- Style, Style.DS, Style.DS.Screen — racines non référencées
- Style.DS.Card (4 variantes : root, Large, Medium, Small)
- Style.DS.Text (5 variantes : root, Title, Body, Label, Meta)
- Style.DS.Button.Primary, Style.DS.Button.Secondary — accédés uniquement via alias
- player_progress, overlay_progress — ProgressBar styles inutilisés
- WindowAnimation.SlideTop, WindowAnimation.SlideRight, WindowAnimation.Fade — animations inutilisées

**Note** : Les Style.DS.* parents sont techniquement requis par leurs alias (Button.Default → Style.DS.Button.Primary). Seuls les styles sans alias ET sans référence directe sont réellement suppressibles.

**Priorité : P1** — Nettoyer les styles vraiment orphelins, garder les parents d'alias

---

### 1c. Attrs (8/28 orphelins — 29%)

**Fichier** : `app/src/main/res/values/attrs.xml` — 28 attributs custom

**Orphelins confirmés (8)** :
| Attribut | Styleable | Raison |
|----------|-----------|--------|
| cardImageBackground | JellyfinTheme | Card image background, 0 réf |
| cardViewBackground | JellyfinTheme | Card view background, 0 réf |
| inputDefaultHighlightBackground | JellyfinTheme | Input highlight, 0 réf |
| inputDefaultStrokeWidth | JellyfinTheme | Input stroke, 0 réf |
| inputDefaultStrokeColor | JellyfinTheme | Input stroke color, 0 réf |
| defaultBackground | JellyfinTheme | Default bg, 0 réf |
| defaultSearchColor | JellyfinTheme | Search color, 0 réf |
| rowHeaderStyle | JellyfinTheme | Row header ref, 0 réf |

**Styleables entièrement orphelins** :
| Styleable | Attrs | Réfs |
|-----------|-------|------|
| StrokeTextView | strokeWidth | 0 réf — classe supprimée |
| AsyncImageView | crossfadeDuration, circleCrop | 0 réf — classe supprimée |

**Priorité : P2** — Impact faible, mais nettoyage facile

---

### 1d. Colors (82/123 orphelins — 67%)

**Fichier** : `app/src/main/res/values/colors.xml` — 123 entrées

**Utilisés (41)** :
| Catégorie | Couleurs actives | Réfs typiques |
|-----------|-----------------|---------------|
| Design System | ds_primary, ds_secondary, ds_secondary_dark, ds_background, ds_surface_dim, ds_surface, ds_text_primary (17), ds_text_secondary (7) | 6 sur 60 |
| Legacy neutres | black (18), white (18), grey (12), grey_light (5), transparent (2) | Très utilisés |
| Legacy overlays | black_transparent_dark/light/normal | 5 |
| Boutons | button_default_disabled/highlight_background, _normal/_disabled/_highlight/_activated_text | 16 |
| Inputs | input_default_disabled_background, _normal/_disabled/_highlight_text | 4 |
| Login UI | login_bg_start/mid/end, login_card_bg/border, login_input_bg/focus_border, login_text_secondary | 13 |
| App | vegafox_launcher_background (4), background_filter (1), jellyfin_blue (3), red (5) | Divers |

**Orphelins — Design System (54/60 ds_*)** :
- Primary variantes (5) : ds_primary_dark/light/container, ds_on_primary/container
- Secondary variantes (3) : ds_secondary_container, ds_on_secondary/container
- Tertiary (3) : ds_tertiary, ds_tertiary_variant, ds_on_tertiary
- Surface (2) : ds_surface_bright, ds_surface_container
- Text helpers (5) : ds_on_background, ds_on_surface/variant, ds_text_disabled/hint
- Error (4) : ds_error, ds_error_container, ds_on_error/container
- Semantic (6) : ds_success/container/on_success, ds_warning/container/on_warning
- Info (3) : ds_info, ds_info_container, ds_on_info
- Outlines (3) : ds_outline, ds_outline_variant, ds_divider
- Overlays (4) : ds_scrim, ds_dialog_scrim, ds_overlay_gradient_start/end
- Focus (2) : ds_focus_ring, ds_focus_glow
- Toolbar (2) : ds_toolbar_bg, ds_toolbar_divider
- Ratings (2) : ds_rating, ds_rating_empty
- Boutons DS (6) : ds_button_bg/focused/text/focused/disabled_bg/disabled_text
- Gradients (3) : ds_gradient_blue_start/mid/end

**Orphelins — Legacy (28)** :
- Palette thème (5) : jellyfin_purple, midnight_blue, indigo_dye, spanish_blue, not_quite_black
- Gradients (6) : dark_green_gradient_start/end, green_gradient_start/end, red_gradient_start/end
- Semi-transparent (1) : black_opaque
- Progressbar (1) : grey_transparent
- Live TV (7) : channel_scroller_bg, program_scroller_bg, timeline_bg, guide_movie/sports/news/kids_bg
- Button/input legacy (3) : button_default_normal_background, input_default_stroke/highlight_background
- Card/popup (2) : popup_menu_background, lb_basic_card_info_bg_color
- Login (5) : login_text_muted, login_error_bg/border/text, login_qc_border/bg

**Priorité : P0** — 82 couleurs inutiles, le plus gros gisement de nettoyage

---

## ÉTAPE 2 — Packages / dossiers vides

**Résultat** : 0 dossier vide dans `app/src/main/java/` et `app/src/main/res/`

**Priorité : skip**

---

## ÉTAPE 3 — Fichiers Kotlin isolés (< 30 lignes)

**Résultat** : 137 fichiers trouvés

| Tranche | Fichiers |
|---------|----------|
| 5-10 lignes | 34 (24.8%) |
| 11-20 lignes | 48 (35.0%) |
| 21-29 lignes | 55 (40.1%) |

**Diagnostic** : Tous légitimes — sealed classes, enums, data classes, extensions, interfaces fonctionnelles. Aucun stub ni migration incomplète détectée.

Exemples représentatifs :
- `HorizontalScrollViewListener.kt` (5 lignes) — fun interface, utilisé par ObservableHorizontalScrollView
- `PlaybackControllerContainer.kt` (5 lignes) — conteneur DI, 4 références
- `AuthenticateMethod.kt` (6 lignes) — sealed ADT pour authentification
- `VegafoXTypography.kt` (8 lignes) — config typographie, 10+ références

**Priorité : skip**

---

## ÉTAPE 4 — Drawables non vectoriels (raster)

**Total images raster** : 12 fichiers (8 PNG + 4 JPG)

**Utilisés (6)** :
| Fichier | Taille | Réfs |
|---------|--------|------|
| blank10x10.png | 182 B | 2 |
| favorites.jpg | 25 KB | Browsing UI |
| ic_vegafox_fox.png | 274 KB | 10+ |
| moviebg.jpg | 95 KB | Movie background |
| qr_code.png | 22 KB | Quick connect |
| vegafox_splash.png | 932 KB | Splash screen |

**Orphelins (6 fichiers, ~909 KB)** :
| Fichier | Taille | Raison |
|---------|--------|--------|
| vegafox_login_background.png | 871 KB | Remplacé par gradient Compose |
| banner_edge_disc.png | 8.3 KB | Ancien guide TV |
| banner_edge_future.png | 8.6 KB | Ancien guide TV |
| banner_edge_missing.png | 12 KB | Ancien guide TV |
| blank20x20.png | 165 B | Redondant (blank10x10 existe) |
| blank30x30.png | 156 B | Redondant (blank10x10 existe) |

**Priorité : P1** — 909 KB récupérables, suppression triviale

---

## ÉTAPE 5 — Modules Gradle

**Modules déclarés** (settings.gradle.kts) : 7

| Module | Dépendant(s) | Statut |
|--------|-------------|--------|
| :app | — (application) | actif |
| :design | :app | actif |
| :playback:core | :app, :playback:jellyfin, :playback:media3:exoplayer, :playback:media3:session | actif |
| :playback:jellyfin | :app | actif |
| :playback:media3:exoplayer | :app | actif |
| :playback:media3:session | :app | actif |
| :preference | :app | actif |

**Priorité : skip** — Tous les modules sont utilisés

---

## ÉTAPE 6 — ProGuard / R8 rules

**Fichiers** : `app/proguard-rules.pro`, `app/proguard-debug.pro`
**consumer-rules.pro** : Aucun dans les sous-modules

**Règles vérifiées** :
- Toutes les règles -keep ciblent des packages larges (`org.jellyfin.androidtv.**`) ou des libs externes (ktor, koin, acra, coil3, markwon, newpipe, leanback)
- Aucune référence à une classe supprimée
- Les règles kotlinx.serialization et databinding sont génériques

**Priorité : skip** — Aucune règle obsolète

---

## ÉTAPE 7 — AndroidManifest

**Composants déclarés** : 10

| Type | android:name | Classe | Statut |
|------|-------------|--------|--------|
| Application | .JellyfinApplication | JellyfinApplication.kt | existe |
| Service | .integration.dream.LibraryDreamService | LibraryDreamService.kt | existe |
| Provider | androidx.startup.InitializationProvider | Framework | OK |
| Provider | .integration.MediaContentProvider | MediaContentProvider.kt | existe |
| Provider | .integration.provider.ImageProvider | ImageProvider.kt | existe |
| Provider | androidx.core.content.FileProvider | Framework | OK |
| Activity | .ui.startup.StartupActivity | StartupActivity.kt | existe |
| Activity-Alias | .startup.StartupActivity | alias du précédent | OK |
| Activity | .ui.browsing.MainActivity | MainActivity.kt | existe |
| Activity | .ui.playback.ExternalPlayerActivity | ExternalPlayerActivity.kt | existe |

**Priorité : skip** — 10/10 composants vérifiés

---

## ÉTAPE 8 — Scripts et fichiers racine

**Racine du projet** : Standard Android/Gradle, rien de suspect
- Gradle : gradlew, build.gradle.kts, settings.gradle.kts, gradle.properties
- Config : .editorconfig, detekt.yaml, android-lint.xml, renovate.json
- Scripts légitimes : scripts/setup-hooks.sh, scripts/pre-commit
- Docs : README.md, CONTRIBUTORS.md, PRIVACY_POLICY.md, SECURITY.md
- Release : vegafox-release.jks, fastlane/

**Répertoire app/** : Structure standard
- proguard-rules.pro, proguard-debug.pro, lint-baseline.xml
- src/main/, src/debug/, src/test/, src/androidTest/, src/playstore/

**Priorité : skip** — Aucun fichier temporaire, backup, ou script suspect

---

## Synthèse et plan d'action

| Priorité | Catégorie | Éléments | Estimation |
|----------|-----------|----------|------------|
| **P0** | Colors orphelins | 82 couleurs à supprimer | ~200 lignes |
| **P1** | Dimens orphelins | 58 dimens à supprimer | ~120 lignes |
| **P1** | Styles orphelins | ~12-15 styles à supprimer | ~80 lignes |
| **P1** | Drawables raster | 6 images à supprimer | ~909 KB |
| **P2** | Attrs orphelins | 8 attrs + 2 styleables | ~30 lignes |
| skip | Dossiers vides | 0 | — |
| skip | Petits fichiers Kotlin | Tous légitimes | — |
| skip | Modules Gradle | Tous utilisés | — |
| skip | ProGuard/R8 | Aucune règle obsolète | — |
| skip | Manifest | 10/10 composants valides | — |
| skip | Fichiers racine | Propre | — |

**Estimation totale** : ~430 lignes XML + 909 KB d'images raster à supprimer

**Prochaine phase recommandée** : cleanup_07 (ressources XML orphelines)
- Supprimer 82 couleurs orphelines (P0)
- Supprimer 58 dimens orphelines (P1)
- Supprimer ~15 styles orphelins (P1)
- Supprimer 6 images raster orphelines (P1)
- Supprimer 8 attrs + 2 styleables orphelins (P2)
- Vérifier le build après chaque catégorie
