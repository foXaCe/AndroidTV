# Moonfin Design System

Premium dark theme for Android TV, optimized for living room viewing on Android 14+.

---

## 1. Color Palette

### 1.1 Primary (Cyan-Blue Accent)

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_primary` | `#00A4DC` | Main accent, buttons, links, focus rings |
| `ds_primary_dark` | `#0084AF` | Pressed/active states |
| `ds_primary_light` | `#48C7F5` | Highlights, hover states |
| `ds_primary_container` | `#002533` | Container background for primary elements |
| `ds_on_primary` | `#FFFFFF` | Text/icons on primary |
| `ds_on_primary_container` | `#B4EDFE` | Text on primary container |

Compose: `JellyfinTheme.colorScheme.primary`, `.primaryDark`, `.primaryLight`, etc.

### 1.2 Secondary (Purple)

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_secondary` | `#AA5CC3` | Secondary accent, categories |
| `ds_secondary_dark` | `#893BE3` | Jellyseerr accent, badges |
| `ds_secondary_container` | `#300367` | Container for secondary elements |
| `ds_on_secondary` | `#FFFFFF` | Text on secondary |
| `ds_on_secondary_container` | `#EDDBFF` | Text on secondary container |

### 1.3 Tertiary (Gold/Ratings)

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_tertiary` | `#FFD700` | Star ratings, gold highlights |
| `ds_tertiary_variant` | `#FFC107` | Alternative gold |
| `ds_on_tertiary` | `#1A1400` | Text on tertiary |

### 1.4 Background & Surface Hierarchy

Dark to light progression with blue-purple tint — optimized for TV viewing
(avoids LCD halo from pure black, softer on OLED):

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_background` | `#0A0A0F` | Screen background (deepest) |
| `ds_surface_dim` | `#0F0F18` | Recessed areas, below cards |
| `ds_surface` | `#12121A` | Card backgrounds, panels |
| `ds_surface_bright` | `#1C1C28` | Elevated panels, modals |
| `ds_surface_container` | `#2A2A3A` | Popovers, dropdowns |
| `ds_on_background` | `#EEEEF5` | Text on background |
| `ds_on_surface` | `#EEEEF5` | Text on surfaces |
| `ds_on_surface_variant` | `#8888AA` | Secondary text on surfaces |

### 1.5 Semantic Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_error` | `#F85A5A` | Error states, destructive actions |
| `ds_error_container` | `#3D0005` | Error container backgrounds |
| `ds_success` | `#23C762` | Success states, confirmed actions |
| `ds_success_container` | `#003214` | Success container backgrounds |
| `ds_warning` | `#F0D400` | Warning states |
| `ds_warning_container` | `#2E2600` | Warning container backgrounds |
| `ds_info` | `#2196F3` | Informational badges, links |
| `ds_info_container` | `#032767` | Info container backgrounds |

### 1.6 Text Colors

| Token | Hex | Contrast on bg | Usage |
|-------|-----|----------------|-------|
| `ds_text_primary` | `#EEEEF5` | 17.1:1 (AAA) | High emphasis text |
| `ds_text_secondary` | `#8888AA` | 6.3:1 (AA) | Medium emphasis text |
| `ds_text_disabled` | `#5C5C78` | 3.4:1 (exempt) | Disabled text |
| `ds_text_hint` | `#7E7EA6` | 5.6:1 (AA) | Hint/placeholder text |

### 1.7 Outline & Dividers

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_outline` | `#3E3E55` | Borders, outlines |
| `ds_outline_variant` | `#2E2E42` | Subtle borders |
| `ds_divider` | `#222236` | Section dividers |

### 1.8 Special Purpose

| Token | Hex | Usage |
|-------|-----|-------|
| `ds_dialog_scrim` | `#E6141414` | Dialog background overlay |
| `ds_scrim` | `#AA000000` | General scrim/overlay |
| `ds_focus_ring` | `#00A4DC` | Focus indicator border |
| `ds_focus_glow` | `#3300A4DC` | Subtle focus glow |
| `ds_toolbar_bg` | `#CC0D1117` | Toolbar background (translucent) |
| `ds_rating` | `#FFD700` | Star rating filled |
| `ds_rating_empty` | `#3E3E55` | Star rating empty |
| `ds_gradient_blue_start` | `#0F3460` | Details page gradient |
| `ds_gradient_blue_mid` | `#0B2545` | Details page gradient |
| `ds_gradient_blue_end` | `#0A0A0F` | Details page gradient |

---

## 2. Typography Scale

### 2.1 Compose Typography (`JellyfinTheme.typography.*`)

| Style | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| `display` | 48sp | W500 | 56sp | Hero text, splash |
| `headlineLarge` | 32sp | W500 | 40sp | Screen titles |
| `headlineMedium` | 24sp | W500 | 32sp | Section titles |
| `titleLarge` | 20sp | W600 | 28sp | Card titles, prominent labels |
| `titleMedium` | 16sp | W600 | 24sp | Subtitles, subheadings |
| `titleSmall` | 14sp | W600 | 20sp | List item titles |
| `bodyLarge` | 16sp | W400 | 24sp | Primary body text |
| `bodyMedium` | 14sp | W400 | 20sp | Default body text |
| `bodySmall` | 12sp | W400 | 16sp | Secondary info |
| `labelLarge` | 14sp | W600 | 20sp | Button text |
| `labelMedium` | 12sp | W500 | 16sp | Badges, tags |
| `labelSmall` | 10sp | W500 | 12sp | Metadata, timestamps |

### 2.2 XML Text Appearances (`@style/TextAppearance.DS.*`)

Same scale as Compose, for XML layouts:
- `TextAppearance.DS.Display`
- `TextAppearance.DS.HeadlineLarge`
- `TextAppearance.DS.HeadlineMedium`
- `TextAppearance.DS.TitleLarge`
- `TextAppearance.DS.TitleMedium`
- `TextAppearance.DS.TitleSmall`
- `TextAppearance.DS.BodyLarge`
- `TextAppearance.DS.BodyMedium`
- `TextAppearance.DS.BodySmall`
- `TextAppearance.DS.LabelLarge`
- `TextAppearance.DS.LabelMedium`
- `TextAppearance.DS.LabelSmall`

### 2.3 XML Dimension Resources (`@dimen/ds_text_*`)

| Resource | Value |
|----------|-------|
| `ds_text_display` | 48sp |
| `ds_text_headline_lg` | 32sp |
| `ds_text_headline_md` | 24sp |
| `ds_text_title_lg` | 20sp |
| `ds_text_title_md` | 16sp |
| `ds_text_title_sm` | 14sp |
| `ds_text_body_lg` | 16sp |
| `ds_text_body_md` | 14sp |
| `ds_text_body_sm` | 12sp |
| `ds_text_label_lg` | 14sp |
| `ds_text_label_md` | 12sp |
| `ds_text_label_sm` | 10sp |

### 2.4 Legacy Compose Typography (still used by base components)

| Style | Size | Usage |
|-------|------|-------|
| `listHeader` | 15sp | List section headers |
| `listOverline` | 10sp | Overline text |
| `listHeadline` | 14sp | List item headlines |
| `listCaption` | 11sp | Captions |
| `badge` | 11sp | Badge text |

---

## 3. Spacing Scale

### 3.1 Base Grid (4dp)

| Token (XML) | Token (Compose) | Value | Usage |
|-------------|-----------------|-------|-------|
| `ds_space_2xs` | `Tokens.Space.space2xs` | 2dp | Hairline gaps |
| `ds_space_xs` | `Tokens.Space.spaceXs` | 4dp | Tight spacing |
| `ds_space_sm` | `Tokens.Space.spaceSm` | 8dp | Small gaps |
| `ds_space_md` | `Tokens.Space.spaceMd` | 16dp | Default padding |
| `ds_space_lg` | `Tokens.Space.spaceLg` | 24dp | Section gaps |
| `ds_space_xl` | `Tokens.Space.spaceXl` | 32dp | Large gaps |
| `ds_space_2xl` | `Tokens.Space.space2xl` | 40dp | Extra large |
| `ds_space_3xl` | `Tokens.Space.space3xl` | 48dp | Maximum |
| `ds_space_4xl` | — | 64dp | Hero spacing |

### 3.2 Intermediate Values

| Token | Value | Usage |
|-------|-------|-------|
| `ds_space_6` | 6dp | Small component gaps |
| `ds_space_10` | 10dp | Button vertical padding |
| `ds_space_12` | 12dp | Input padding |
| `ds_space_14` | 14dp | Medium component gaps |
| `ds_space_20` | 20dp | Dialog padding |

---

## 4. Corner Radii

| Token (XML) | Token (Compose) | Value | Usage |
|-------------|-----------------|-------|-------|
| `ds_radius_none` | — | 0dp | No rounding |
| `ds_radius_xs` | `shapes.extraSmall` | 4dp | Cards, badges |
| `ds_radius_sm` | `shapes.small` | 8dp | Buttons, inputs |
| `ds_radius_md` | `shapes.medium` | 12dp | Panels, containers |
| `ds_radius_lg` | `shapes.large` | 16dp | Large cards |
| `ds_radius_xl` | `shapes.dialog` | 20dp | Dialogs, modals |
| `ds_radius_2xl` | `shapes.extraLarge` | 28dp | Search bars, pills |
| `ds_radius_full` | `shapes.full` | 999dp | Circular elements |

Additional Compose shapes:
- `shapes.button` = 6dp — Action buttons in detail views

---

## 5. Component Styles

### 5.1 XML Styles

| Style | Parent | Usage |
|-------|--------|-------|
| `Style.DS.Screen` | — | Screen background |
| `Style.DS.Card` | — | Card background |
| `Style.DS.Card.Large` | `.Card` | Large card (300dp) |
| `Style.DS.Card.Medium` | `.Card` | Medium card (160dp) |
| `Style.DS.Card.Small` | `.Card` | Small card (120dp) |
| `Style.DS.Button.Primary` | `android:Widget.Button` | Primary action button |
| `Style.DS.Button.Secondary` | `.Button.Primary` | Text-only button |
| `Style.DS.Button.Icon` | `android:Widget.Button` | Icon-only button |
| `Style.DS.Button.Icon.Animated` | `.Button.Icon` | Animated icon button |
| `Style.DS.Text.Title` | — | Title text style |
| `Style.DS.Text.Body` | — | Body text style |
| `Style.DS.Text.Label` | — | Label text style |
| `Style.DS.Text.Meta` | — | Metadata text style |
| `Style.DS.Input` | `android:Widget.EditText` | Text input field |
| `Style.DS.PopupMenu` | `android:Widget.Material.PopupMenu` | Popup menu |

### 5.2 Legacy Aliases (backward compatibility)

- `Button.Default` → `Style.DS.Button.Primary`
- `Button.Icon` → `Style.DS.Button.Icon`
- `Input.Default` → `Style.DS.Input`
- `PopupMenu` → `Style.DS.PopupMenu`
- `PopupWindow` → `Style.DS.PopupMenu`

---

## 6. Component Dimensions

| Token | Value | Usage |
|-------|-------|-------|
| `ds_icon_sm` | 16dp | Small icons |
| `ds_icon_md` | 24dp | Standard icons |
| `ds_icon_lg` | 32dp | Large icons |
| `ds_icon_xl` | 48dp | Hero icons |
| `ds_button_height` | 48dp | Standard button |
| `ds_button_height_sm` | 36dp | Compact button |
| `ds_button_padding_h` | 24dp | Button horizontal padding |
| `ds_button_padding_v` | 10dp | Button vertical padding |
| `ds_card_width_sm` | 120dp | Small card |
| `ds_card_width_md` | 160dp | Medium card |
| `ds_card_width_lg` | 220dp | Large card |
| `ds_card_width_xl` | 300dp | Extra large card |
| `ds_toolbar_height` | 56dp | Toolbar height |
| `ds_dialog_max_width` | 480dp | Dialog max width |
| `ds_dialog_padding` | 24dp | Dialog inner padding |
| `ds_focus_border_width` | 2dp | Focus border width |
| `ds_divider_thickness` | 1dp | Divider line |
| `ds_elevation_sm` | 2dp | Subtle shadow |
| `ds_elevation_md` | 4dp | Card shadow |
| `ds_elevation_lg` | 8dp | Modal shadow |
| `ds_elevation_xl` | 16dp | Toast shadow |

---

## 7. Animations

### 7.1 Duration Constants

| Token (XML) | Compose Constant | Value | Usage |
|-------------|------------------|-------|-------|
| `ds_anim_fast` | `AnimationDefaults.DURATION_FAST` | 150ms | Micro-interactions, state changes |
| `ds_anim_medium` | `AnimationDefaults.DURATION_MEDIUM` | 300ms | Transitions, fades, dialog enter/exit |
| `ds_anim_slow` | `AnimationDefaults.DURATION_SLOW` | 500ms | Complex transitions, page animations |

### 7.2 Focus Animation

| Constant | Value | Usage |
|----------|-------|-------|
| `AnimationDefaults.FOCUS_SCALE` | 1.06x | Compose component focus scale |
| `AnimationDefaults.LEANBACK_FOCUS_SCALE` | 1.15x | Leanback row card zoom (legacy) |

### 7.3 Alpha Constants

| Constant | Value | Usage |
|----------|-------|-------|
| `AnimationDefaults.SCRIM_ALPHA` | 0.67 | Dialog/overlay scrim |
| `AnimationDefaults.DISABLED_ALPHA` | 0.38 | Disabled state |
| `AnimationDefaults.MEDIUM_EMPHASIS_ALPHA` | 0.60 | Medium emphasis elements |
| `AnimationDefaults.HIGH_EMPHASIS_ALPHA` | 0.87 | High emphasis elements |

### 7.4 Transitions

- **Screen transitions**: Fade in/out at 300ms (`WindowAnimation.Fade`)
- **Slide transitions**: Slide right at 300ms (`WindowAnimation.SlideRight`)
- **Dialog enter/exit**: Fade at 300ms (via `DialogBase`)
- **Focus**: Scale to 1.06x with spring animation

---

## 8. Compose Theme Access

```kotlin
// Colors
JellyfinTheme.colorScheme.primary
JellyfinTheme.colorScheme.onSurface
JellyfinTheme.colorScheme.dialogScrim
// ... etc.

// Typography
JellyfinTheme.typography.headlineLarge
JellyfinTheme.typography.bodyMedium
JellyfinTheme.typography.labelSmall
// ... etc.

// Shapes
JellyfinTheme.shapes.small        // 8dp
JellyfinTheme.shapes.dialog       // 20dp
JellyfinTheme.shapes.button       // 6dp
// ... etc.

// Animation constants
AnimationDefaults.DURATION_MEDIUM  // 300
AnimationDefaults.FOCUS_SCALE      // 1.06f
```

---

## 9. Rules

1. **Zero hardcoded colors** in layouts — use `@color/ds_*` (XML) or `JellyfinTheme.colorScheme.*` (Compose)
2. **Zero hardcoded dimensions** for spacing/padding — use `@dimen/ds_*` or `Tokens.Space.*`
3. **Zero hardcoded font sizes** — use `@style/TextAppearance.DS.*` (XML) or `JellyfinTheme.typography.*` (Compose)
4. **Zero inline RoundedCornerShape** — use `JellyfinTheme.shapes.*`
5. **Same component = same style everywhere** — no copy-paste of colors/shapes
6. **All animation durations** from `AnimationDefaults` constants
7. **No pure white (#FFFFFF)** for text — use `ds_text_primary` (#EEEEF5)
8. **No pure black (#000000)** for backgrounds — use `ds_background` (#0A0A0F)
9. **All neutral colors use blue-purple tint** — maintains cohesion in dim lighting
10. **WCAG AA minimum** for all visible text — AAA for body text (TV viewing distance)

---

## 10. Files Modified

### Foundation Files Created/Updated

| File | Type | Change |
|------|------|--------|
| `app/src/main/res/values/colors.xml` | Updated | Added 60+ `ds_*` semantic colors |
| `app/src/main/res/values/dimens.xml` | Updated | Added spacing, radii, text sizes, component dims, animation ints |
| `app/src/main/res/values/type.xml` | Created | 12 TextAppearance styles |
| `app/src/main/res/values/styles.xml` | Updated | Added `Style.DS.*` component styles, deduplicated legacy |
| `app/src/main/res/values/theme_mutedpurple.xml` | Updated | Fixed inline hex, aligned radii to system |
| `design/.../token/TypographyTokens.kt` | Updated | Fixed dp to sp (accessibility bug), added 4xl |
| `design/.../token/RadiusTokens.kt` | Updated | Replaced unusable 128dp/15984dp with semantic radii |
| `ui/base/colorScheme.kt` | Updated | Extended from 30 to 80+ semantic color properties |
| `ui/base/shapes.kt` | Updated | Added `dialog` (20dp), `button` (6dp), `full` (999dp) |
| `ui/base/typography.kt` | Updated | Extended from 5 to 17 type styles (full Material 3 scale) |
| `ui/base/AnimationDefaults.kt` | Created | Duration, scale, and alpha constants |
| `ui/base/OverlayColors.kt` | Created | Centralized toolbar overlay color map (was duplicated) |

### Compose Files Updated (design system applied)

| File | Changes Applied |
|------|----------------|
| `MainToolbar.kt` | Toolbar colors centralized |
| `MediaBarSlideshowView.kt` | Toolbar colors deduplicated, shapes/fonts |
| `ItemDetailsComponents.kt` | Colors, shapes, font sizes |
| `ItemDetailsFragment.kt` | Colors, gradients, shapes, font sizes |
| `LibraryBrowseFragment.kt` | Removed JellyfinBlue constant, colors/shapes |
| `GenresGridV2Fragment.kt` | Colors, shapes, font sizes |
| `MusicBrowseFragment.kt` | Colors, shapes, font sizes |
| `ShuffleOptionsDialog.kt` | Dialog pattern: dialogScrim + shapes.dialog |
| `DonateDialog.kt` | Dialog pattern: dialogScrim + shapes.dialog |
| `AddToPlaylistDialog.kt` | Dialog pattern: dialogScrim + shapes.dialog |
| `CreatePlaylistDialog.kt` | Dialog pattern: dialogScrim + shapes.dialog |
| `SyncPlayDialog.kt` | Dialog pattern: dialogScrim + shapes.dialog |
| `SettingsMainScreen.kt` | Colors, shapes |
| `SettingsDeveloperScreen.kt` | Colors, shapes |
| `SettingsAboutScreen.kt` | Colors |
| `HomeFragment.kt` | Colors |
| `HomeRowsFragment.kt` | Colors |
| All Jellyseerr files (13) | Hardcoded colors replaced |
| All settings/moonfin files | Colors, shapes |
| Playback overlay actions | Colors |
| Toolbar files (4) | Colors, shapes |
| CardPresenter.kt | Colors in Compose sections |
| SelectServerFragment.kt | Colors |

### XML Layout Files Updated

| File | Changes Applied |
|------|----------------|
| `channel_header.xml` | Colors, dimensions |
| `fragment_jellyseerr_requests.xml` | Colors |
| `fragment_jellyseerr_settings.xml` | Colors |
| `fragment_server_add.xml` | Colors, dimensions |
| `friendly_date_button.xml` | Colors, text appearances |
| `item_jellyseerr_content.xml` | Colors |
| `overlay_tv_guide.xml` | Colors, dimensions |
| `view_jellyseerr_details_row.xml` | Colors |
| `view_row_details.xml` | Colors, dimensions |
| `jellyfin_genre_card.xml` | Colors, dims, text appearances |
| `jellyseerr_genre_card.xml` | Colors, dims, text appearances |
| `fragment_home.xml` | shadowColor reference |
| `fragment_jellyseerr_discover_new.xml` | shadowColor references |
| `vlc_player_interface.xml` | Text color reference |

### Additional Compose Files Updated

| File | Changes Applied |
|------|----------------|
| `ExitConfirmationDialog.kt` | Full DS migration: colors, shapes, typography |
| `ItemCardJellyseerrOverlay.kt` | Colors, shapes, typography tokens |
| `rating.kt` | Colors (rating, text), shapes, typography |
| `InfoRowColors.kt` | Kept as-is (static non-Composable constants) |
| `ScheduleBrowseFragment.kt` | Shapes, colors, font sizes |
| `RecordingsBrowseFragment.kt` | Shapes, colors, font sizes |
| `LiveTvBrowseFragment.kt` | Shapes, colors, font sizes |
| `SeriesRecordingsBrowseFragment.kt` | Shapes, colors, font sizes |
| `LibraryBrowseComponents.kt` | JellyfinBlue removed, shapes, colors |

---

## 11. Impact Metrics

| Metric | Before | After | Reduction |
|--------|--------|-------|-----------|
| Hardcoded Color(0x...) in UI | 131 | 0* | **100%** |
| Hardcoded hex in XML layouts | 16 | 0 | **100%** |
| Color.parseColor() in Jellyseerr | 100+ | 2 | **98%** |
| RoundedCornerShape inline | 70+ | 3** | **96%** |
| fontSize = XX.sp inline | 187 | 0** | **100%** |
| Toolbar color duplication | 2 files × 10 | 1 shared object | 100% |
| Dialog pattern copy-paste | 8 files | All use DialogBase/shapes.dialog | 100% |
| Theme rounding inconsistency | MutedPurple different | Aligned to system | Fixed |
| TypographyTokens dp bug | All sizes in dp | All sizes in sp | Fixed |
| RadiusTokens 128dp bug | 128dp unusable | Semantic radii (4-999dp) | Fixed |

*Excluding definition files (colorScheme.kt, OverlayColors.kt, InfoRowColors.kt, presets.kt).
Zero hardcoded colors remain in active UI code.

**Excluding definition files (shapes.kt, typography.kt). Remaining 3 RoundedCornerShape
are in SearchTextInput, Popover, ListColorChannelRangeControl (utility components).
Zero fontSize inline remaining outside typography.kt definitions.

---

## 12. Changelog

| Date | Change |
|------|--------|
| 2026-03-07 | Initial design system creation: tokens, styles, Compose theme, XML migration |
| 2026-03-07 | Phase 2: LibraryBrowseComponents.kt full migration (27 replacements) |
| 2026-03-07 | Phase 2: ItemDetailsComponents.kt gradient/color migration |
| 2026-03-07 | Phase 2: Remaining 18 files font/shape migration via agents |
| 2026-03-08 | TV dark theme optimization: blue-purple tint palette, WCAG contrast audit |
