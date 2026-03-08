# Audit 18 — Couleurs résiduelles

**Date** : 2026-03-08
**Statut** : ✅ Résolu (8/8)

---

## Corrections immédiates (hardcodées)

| # | Fichier | Valeur avant | Valeur après | Token DS |
|---|---------|-------------|-------------|----------|
| 1 | `GenreCardPresenter.kt:67` | `Color.parseColor("#1a1a1a")` | `ContextCompat.getColor(context, R.color.ds_surface)` | `ds_surface` (#12121A) |
| 2 | `ItemDetailsFragment.kt:257` | `AndroidColor.parseColor("#0A0A0A")` | `ContextCompat.getColor(requireContext(), R.color.ds_background)` | `ds_background` (#0A0A0F) |
| 3 | `theme_mutedpurple.xml:30` | `#212121` (inline hex) | `@color/ds_surface_bright` | `ds_surface_bright` (#1C1C28) |

## Alignement couleurs legacy (teinte blue-purple)

| # | Fichier | Couleur | Valeur avant | Valeur après | Justification |
|---|---------|---------|-------------|-------------|---------------|
| 4 | `colors.xml` | `channel_scroller_bg` | `#201b1c` | `#1A1418` | Garde chaleur + cohérence teinte DS |
| 5 | `colors.xml` | `timeline_bg` | `#201b1c` | `#1A1418` | Même traitement que channel_scroller |
| 6 | `colors.xml` | `background_filter` | `#94101010` | `#940A0A0F` | Même opacité, fond aligné sur ds_background |
| 7 | `colors.xml` | `lb_basic_card_info_bg_color` | `@color/grey` (#303030) | `#2A2A3A` | Aligné sur ds_surface_container |
| 8 | `colors.xml` | `popup_menu_background` | `#202124` | `#1C1C28` | Aligné sur ds_surface_bright |

## Vérification

- `Color.parseColor` dans le Kotlin : **0 occurrence** ✅
- Toutes les couleurs legacy du guide TV alignées sur la teinte blue-purple ✅
- Import `android.graphics.Color as AndroidColor` supprimé de `ItemDetailsFragment.kt` ✅
- Import `android.graphics.Color` supprimé de `GenreCardPresenter.kt` ✅
