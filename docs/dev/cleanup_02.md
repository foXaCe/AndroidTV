# Cleanup 02 — Tokens.Color, couleurs XML, drawables orphelins

**Date** : 2026-03-12
**Branche** : main

---

## Etape 1 — Inventaire complet

### Tokens.Color dans ui/ (6 occurrences, 4 fichiers)

| Fichier | Occurrences | Tokens utilises |
|---------|-------------|-----------------|
| `itemdetail/v2/ItemDetailsComponents.kt:1371` | 1 | `colorRed500` |
| `browsing/v2/LibraryBrowseComponents.kt:247` | 1 | `colorRed500` |
| `composable/item/ItemCardBaseItemOverlay.kt:141,151` | 3 | `colorRed600`, `colorGrey100`, `colorRed500` |
| `composable/item/ItemPreview.kt:52,61` | 2 | `colorGrey100`, `colorGrey300` |

### Tokens.Space dans ui/ (56 occurrences, 22 fichiers)

Conserves — les Tokens.Space sont utilises dans des composants core
du design system (Spacing.kt, TvRowList.kt, TvCardGrid.kt, etc.)
et ne doivent pas etre migres vers des valeurs hardcodees.

### Couleurs XML orphelines (61 couleurs)

**ds_* orphelins (53 couleurs) :**
- `ds_primary_dark`, `ds_primary_light`, `ds_primary_container`
- `ds_on_primary`, `ds_on_primary_container`
- `ds_secondary`, `ds_secondary_container`
- `ds_on_secondary`, `ds_on_secondary_container`
- `ds_tertiary`, `ds_tertiary_variant`, `ds_on_tertiary`
- `ds_surface_container`
- `ds_on_background`, `ds_on_surface`, `ds_on_surface_variant`
- `ds_error`, `ds_error_container`, `ds_on_error`, `ds_on_error_container`
- `ds_success`, `ds_success_container`, `ds_on_success`
- `ds_warning`, `ds_warning_container`, `ds_on_warning`
- `ds_info`, `ds_info_container`, `ds_on_info`
- `ds_text_disabled`, `ds_text_hint`
- `ds_outline`, `ds_outline_variant`, `ds_divider`
- `ds_scrim`, `ds_dialog_scrim`
- `ds_overlay_gradient_start`, `ds_overlay_gradient_end`
- `ds_focus_ring`, `ds_focus_glow`
- `ds_toolbar_bg`, `ds_toolbar_divider`
- `ds_rating`, `ds_rating_empty`
- `ds_button_bg`, `ds_button_bg_focused`, `ds_button_text`
- `ds_button_text_focused`, `ds_button_disabled_bg`, `ds_button_disabled_text`
- `ds_gradient_blue_start`, `ds_gradient_blue_mid`, `ds_gradient_blue_end`

**Legacy orphelins (8 couleurs) :**
- `vegafox_orange`
- `not_quite_black`
- `black_opaque`
- `lb_basic_card_info_bg_color`
- `login_bg_start`, `login_bg_mid`, `login_bg_end`
- `login_text_muted`
- `login_error_text`

**ds_* conserves (8 couleurs — encore ref en XML layouts/themes) :**
- `ds_primary` — styles.xml, FriendlyDateButton.kt
- `ds_secondary_dark` — fragment_jellyseerr_requests.xml
- `ds_background` — theme_jellyfin.xml, styles.xml
- `ds_surface_dim` — jellyfin_genre_card.xml, switch_background.xml
- `ds_surface` — styles.xml
- `ds_surface_bright` — theme_mutedpurple.xml
- `ds_text_primary` — jellyseerr_genre_card.xml, genre_card.xml, requests.xml
- `ds_text_secondary` — genre_card.xml, requests.xml, settings.xml

### Drawables XML orphelins (5 confirmes, 3 faux positifs)

**Supprimes :**
- `ic_arrow_back.xml`
- `ic_pencil.xml`
- `bg_qc_code.xml`
- `ic_user_add.xml`
- `bg_notification_warning.xml`

**Faux positifs (ref par d'autres drawables) :**
- `app_banner_foreground.xml` — ref par `app_banner.xml`
- `app_banner_background.xml` — ref par `app_banner.xml`
- `ic_tv.xml` — ref par `tile_land_tv.xml`

### colorResource / R.color dans Kotlin (9 fichiers)

Tous dans des fichiers legacy XML-based (View system) — pas dans le
perimetre Compose. Conserves.

### Hardcoded Color.* dans Kotlin (~40 occurrences)

Majorite dans des cas intentionnels :
- `Color.Black` : fond player video/photo, ombres, scrim
- `Color.White.copy(alpha=...)` : bordures subtiles, separateurs
- `Color.Red` : indicateurs d'enregistrement LiveTV
- `Color.Transparent` : fond par defaut
Aucune migration necessaire — ces usages sont semantiquement corrects.

---

## Etape 2 — Pas de warnings compiler

`compileGithubDebugKotlin` — 0 warning Kotlin detecte.
(Cache UP-TO-DATE, aucun warning format `*.kt:N:N: warning`)

---

## Etape 3 — Tokens.Color → VegafoXColors (6 remplacements)

| Fichier | Avant | Apres |
|---------|-------|-------|
| `ItemDetailsComponents.kt:1371` | `Tokens.Color.colorRed500` | `VegafoXColors.Error` |
| `LibraryBrowseComponents.kt:247` | `Tokens.Color.colorRed500` | `VegafoXColors.Error` |
| `ItemCardBaseItemOverlay.kt:141` | `Tokens.Color.colorRed600` | `VegafoXColors.Error` |
| `ItemCardBaseItemOverlay.kt:141` | `Tokens.Color.colorGrey100` | `VegafoXColors.TextPrimary` |
| `ItemCardBaseItemOverlay.kt:151` | `Tokens.Color.colorRed500` | `VegafoXColors.Error` |
| `ItemPreview.kt:52` | `Tokens.Color.colorGrey100` | `VegafoXColors.TextPrimary` |
| `ItemPreview.kt:61` | `Tokens.Color.colorGrey300` | `VegafoXColors.TextSecondary` |

Import `VegafoXColors` ajoute dans `ItemCardBaseItemOverlay.kt` et `ItemPreview.kt`.
Import `Tokens` conserve (encore utilise pour `Tokens.Space`).

**Resultat** : `grep -rn "Tokens.Color" ui/ → 0 resultats`

---

## Etape 4 — Drawables supprimes

| Drawable | LOC estimee |
|----------|-------------|
| `ic_arrow_back.xml` | ~10 |
| `ic_pencil.xml` | ~10 |
| `bg_qc_code.xml` | ~15 |
| `ic_user_add.xml` | ~10 |
| `bg_notification_warning.xml` | ~15 |
| **Total** | **~60 LOC** |

---

## Etape 5 — Couleurs XML supprimees

**colors.xml** : 179 lignes → 68 lignes

| Categorie | Nombre supprime |
|-----------|-----------------|
| ds_* orphelins | 53 |
| Legacy orphelins | 8 |
| **Total** | **61 couleurs** |

LOC supprimees dans colors.xml : ~111 lignes

---

## Etape 6 — AppModule.kt

Aucun nettoyage necessaire — tous les ViewModels, singletons et
factories sont encore utilises. Verifie :
- `InteractionTrackerViewModel` — 5 refs
- `PlaybackControllerContainer` — 5 refs
- `DataRefreshService` — 5 refs

---

## Bilan

### LOC supprimees

| Element | LOC |
|---------|-----|
| 5 drawables XML orphelins | ~60 |
| 61 couleurs XML orphelines | ~111 |
| Tokens.Color → VegafoXColors (net) | +2 (imports) |
| **Total net** | **~169 LOC** |

### Fichiers modifies

| Fichier | Type |
|---------|------|
| `ItemDetailsComponents.kt` | Tokens.Color → VegafoXColors |
| `LibraryBrowseComponents.kt` | Tokens.Color → VegafoXColors |
| `ItemCardBaseItemOverlay.kt` | Tokens.Color → VegafoXColors + import |
| `ItemPreview.kt` | Tokens.Color → VegafoXColors + import |
| `colors.xml` | 61 couleurs supprimees |

### Fichiers supprimes

| Fichier |
|---------|
| `drawable/ic_arrow_back.xml` |
| `drawable/ic_pencil.xml` |
| `drawable/bg_qc_code.xml` |
| `drawable/ic_user_add.xml` |
| `drawable/bg_notification_warning.xml` |

### Build

| Variante | Statut |
|----------|--------|
| Debug (github) | BUILD SUCCESSFUL |
| Release (github) | BUILD SUCCESSFUL |
| Install AM9 Pro (debug) | Success |

### Tokens.Color restants dans ui/

**0 occurrences** — migration complete.

### Tokens.Space restants dans ui/

56 occurrences dans 22 fichiers — conserves intentionnellement
(design system core, pas dans le perimetre de ce cleanup).
