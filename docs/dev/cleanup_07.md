# Cleanup 07 — Ressources XML orphelines

**Date** : 2026-03-13
**Scope** : Colors, dimens, styles, attrs, images raster, type.xml
**Basé sur** : audit_final.md

---

## Groupe 1 — Colors orphelins

**Fichier** : `app/src/main/res/values/colors.xml`

### Supprimés (73 couleurs)

**DS orphelins (52)** :
- Primary variantes : ds_primary_dark, ds_primary_light, ds_primary_container, ds_on_primary, ds_on_primary_container
- Secondary variantes : ds_secondary_container, ds_on_secondary, ds_on_secondary_container
- Tertiary : ds_tertiary, ds_tertiary_variant, ds_on_tertiary
- Surface : ds_surface_container
- Text/On : ds_on_background, ds_on_surface, ds_on_surface_variant, ds_text_disabled, ds_text_hint
- Error : ds_error, ds_error_container, ds_on_error, ds_on_error_container
- Semantic : ds_success, ds_success_container, ds_on_success, ds_warning, ds_warning_container, ds_on_warning
- Info : ds_info, ds_info_container, ds_on_info
- Outlines : ds_outline, ds_outline_variant, ds_divider
- Overlays : ds_scrim, ds_dialog_scrim, ds_overlay_gradient_start, ds_overlay_gradient_end
- Focus : ds_focus_ring, ds_focus_glow
- Toolbar : ds_toolbar_bg, ds_toolbar_divider
- Ratings : ds_rating, ds_rating_empty
- Boutons DS : ds_button_bg, ds_button_bg_focused, ds_button_text, ds_button_text_focused, ds_button_disabled_bg, ds_button_disabled_text
- Gradients : ds_gradient_blue_start, ds_gradient_blue_mid, ds_gradient_blue_end

**Legacy orphelins (19)** :
- not_quite_black
- dark_green_gradient_start/end, green_gradient_start/end, red_gradient_start/end (6)
- black_opaque
- channel_scroller_bg, program_scroller_bg, timeline_bg, guide_movie_bg, guide_sports_bg, guide_news_bg, guide_kids_bg (7)
- lb_basic_card_info_bg_color
- login_text_muted, login_error_bg, login_error_border, login_error_text, login_qc_border, login_qc_bg (6)

**Cascade attrs (Groupe 5) (2)** :
- input_default_stroke (libéré par suppression inputDefaultStrokeColor attr)
- input_default_highlight_background (libéré par suppression inputDefaultHighlightBackground attr)

### Faux positifs de l'audit — conservés (11 couleurs)

| Couleur | Réfs | Raison |
|---------|------|--------|
| ds_surface_bright | 2 | theme_mutedpurple.xml (popupMenuBackground, tile_port_person_bg) |
| jellyfin_purple | 1 | theme_jellyfin.xml (progressSecondary) |
| midnight_blue | 4 | theme_jellyfin.xml (tile_audio_bg, etc.) |
| indigo_dye | 10 | theme_jellyfin.xml (tile_port_person_bg, etc.) |
| spanish_blue | 6 | theme_jellyfin.xml (tile_artists_bg, etc.) |
| grey_transparent | 1 | theme_jellyfin.xml (progressBackground) |
| button_default_normal_background | 2 | theme_jellyfin.xml (buttonDefaultNormalBackground, controlIconBackground) |
| popup_menu_background | 1 | theme_jellyfin.xml (popupMenuBackground) |

**Résultat** : 193 → 84 couleurs (−109 lignes)

---

## Groupe 2 — Dimens orphelins

**Fichier** : `app/src/main/res/values/dimens.xml`

### Supprimés (53 dimens, incluant cascade Groupe 3)

**Phase 1 (44 — orphelins directs)** :
- Spacing : ds_space_2xs, ds_space_6, ds_space_10, ds_space_14, ds_space_20, ds_space_2xl, ds_space_3xl, ds_space_4xl (8)
- Radii : ds_radius_none/xs/sm/md/lg/xl/2xl/full (8)
- Icons : ds_icon_sm/md/lg/xl (4)
- Buttons : ds_button_height, ds_button_height_sm (2)
- Cards : ds_card_width_lg (1)
- Toolbar : ds_toolbar_height (1)
- Dialog : ds_dialog_max_width, ds_dialog_padding (2)
- Focus : ds_focus_border_width (1)
- Divider : ds_divider_thickness (1)
- Elevation : ds_elevation_sm/md/lg/xl (4)
- Animation : ds_anim_fast, ds_anim_medium, ds_anim_slow (3)
- Legacy : home_row_spacing, home_row_spacing_large, lb_browse_padding_start, card_scale_default, card_scale_focus, lb_focus_zoom_factor_small/xsmall/medium/large (9)

**Phase 2 (9 — cascade post-styles)** :
- Typography : ds_text_display, ds_text_headline_lg, ds_text_title_lg/md/sm, ds_text_label_md (6)
- Cards : ds_card_width_xl, ds_card_width_md, ds_card_width_sm (3)

**Résultat** : 105 → 34 lignes (−71 lignes)

---

## Groupe 3 — Styles + Type orphelins

### styles.xml — Supprimés (16 styles)

- Style, Style.DS (racines vides)
- Style.DS.Screen
- Style.DS.Card + Large + Medium + Small (4)
- Style.DS.Text + Title + Body + Label + Meta (5)
- player_progress, overlay_progress
- WindowAnimation.SlideTop, WindowAnimation.SlideRight
- Widget.Jellyfin.Row.Header (cascade de rowHeaderStyle)

**Conservés** :
- WindowAnimation + WindowAnimation.Fade (utilisé par ExpandableTextView.kt — faux positif audit)
- Style.DS.Button.Primary/Secondary/Icon/Icon.Animated (alias actifs)
- Style.DS.Input, Style.DS.PopupMenu (alias actifs)
- DatePickerCustom (1 ref)
- Tous les aliases (Button.Default, Button.Icon, Input.Default, PopupMenu, PopupWindow)

**Résultat** : 161 → 88 lignes (−73 lignes)

### type.xml — Supprimés (10 TextAppearance styles)

- TextAppearance.DS.Display, HeadlineLarge, HeadlineMedium
- TextAppearance.DS.TitleLarge, TitleMedium, TitleSmall
- TextAppearance.DS.BodyLarge, BodyMedium, BodySmall
- TextAppearance.DS.LabelMedium, LabelSmall

**Conservés** :
- TextAppearance.DS (parent implicite requis)
- TextAppearance.DS.LabelLarge (ref'd par Style.DS.Button.Primary)

**Résultat** : 95 → 16 lignes (−79 lignes)

---

## Groupe 4 — Images raster orphelines

| Fichier | Taille | Raison |
|---------|--------|--------|
| vegafox_login_background.png | 871 KB | Remplacé par gradient Compose |
| banner_edge_disc.png | 8.3 KB | Ancien guide TV |
| banner_edge_future.png | 8.6 KB | Ancien guide TV |
| banner_edge_missing.png | 12 KB | Ancien guide TV |
| blank20x20.png | 165 B | Redondant |
| blank30x30.png | 156 B | Redondant |

**Taille récupérée** : ~900 KB

---

## Groupe 5 — Attrs orphelins

### attrs.xml — Supprimés (8 attrs + 1 styleable)

**JellyfinTheme attrs supprimés** :
- cardImageBackground, cardViewBackground
- inputDefaultHighlightBackground, inputDefaultStrokeWidth, inputDefaultStrokeColor
- defaultSearchColor, rowHeaderStyle, headerTextColor

**Styleable supprimé** : StrokeTextView (classe supprimée)

**Conservé** : AsyncImageView (classe AsyncImageView.kt toujours active)

### Cascade thèmes

**theme_jellyfin.xml** : −14 lignes (items pour les attrs supprimés)
**theme_emerald.xml** : −1 ligne (defaultSearchColor)
**theme_mutedpurple.xml** : −1 ligne (defaultSearchColor)

---

## Faux positifs importants de l'audit

1. **WindowAnimation.Fade** — listé orphelin mais utilisé par ExpandableTextView.kt
2. **11 couleurs** — listées orphelines mais référencées dans les fichiers de thème
3. **8 attrs JellyfinTheme** — listés "0 réf" dans l'audit, mais référencés dans les thèmes. La nuance : ils sont DÉCLARÉS et SET dans les thèmes, mais jamais LUES depuis le code (sauf defaultBackground)
4. **AsyncImageView styleable** — listé orphelin mais classe toujours active

---

## Résumé

| Catégorie | Supprimés | Lignes |
|-----------|-----------|--------|
| Colors | 73 | 109 |
| Dimens | 53 | 71 |
| Styles | 16 | 73 |
| Type styles | 10 | 79 |
| Attrs + thèmes | 8 attrs + 1 styleable | 29 |
| Images raster | 6 fichiers | ~900 KB |
| **Total** | | **~361 lignes + 900 KB** |

### LOC total tous cleanups

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3,124 |
| 05 | ~1,581 + 6,751 traductions |
| 06 | ~369 |
| **07** | **~361 + 900 KB images** |
| **Total** | **~6,010 LOC + 6,751 traductions + 900 KB images** |

### Lint final

`./gradlew :app:lintGithubDebug` → **0 UnusedResources**

### Build

- Debug (github + playstore) : BUILD SUCCESSFUL
- Release (github) : BUILD SUCCESSFUL
- Installé sur AM9 Pro : Success
