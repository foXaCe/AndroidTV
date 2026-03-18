# Audit des drawables - app/src/main/res/

**Date** : 2026-03-15
**Scope** : Tous les fichiers dans `app/src/main/res/drawable*`

---

## 1. Inventaire complet

### Dossiers drawable existants

| Dossier | Fichiers | Taille totale |
|---------|----------|---------------|
| `drawable/` | 87 fichiers | 727 203 octets |
| `drawable-anydpi/` | 1 fichier | 182 octets |
| `drawable-v24/` | 1 fichier | 7 082 octets |
| **Total** | **89 fichiers** | **735 467 octets (718 Ko)** |

### Detail par fichier

| Fichier | Dossier | Extension | Taille (octets) |
|---------|---------|-----------|-----------------|
| app_banner | drawable/ | xml | 214 |
| app_banner | drawable-anydpi/ | xml | 182 |
| app_banner_background | drawable/ | xml | 210 |
| app_banner_foreground | drawable/ | xml | 173 |
| app_icon_background | drawable/ | xml | 180 |
| app_icon_foreground | drawable/ | xml | 192 |
| app_icon_foreground_monochrome | drawable/ | xml | 274 |
| app_logo | drawable/ | xml | 7 082 |
| app_logo | drawable-v24/ | xml | 7 082 |
| bg_login_card | drawable/ | xml | 326 |
| bg_login_gradient | drawable/ | xml | 368 |
| blank10x10 | drawable/ | png | 182 |
| button_default_back | drawable/ | xml | 1 244 |
| button_default_ripple | drawable/ | xml | 421 |
| button_default_text | drawable/ | xml | 577 |
| button_icon_back | drawable/ | xml | 529 |
| button_icon_ripple | drawable/ | xml | 354 |
| button_icon_tint | drawable/ | xml | 767 |
| button_icon_tint_animated | drawable/ | xml | 577 |
| button_text_background | drawable/ | xml | 478 |
| chevron_left | drawable/ | xml | 308 |
| detail_backdrop_gradient | drawable/ | xml | 306 |
| expanded_text | drawable/ | xml | 355 |
| favorites | drawable/ | jpg | 25 243 |
| genre_card_overlay | drawable/ | xml | 305 |
| gradient_backdrop_overlay | drawable/ | xml | 238 |
| ic_add | drawable/ | xml | 301 |
| ic_album | drawable/ | xml | 528 |
| ic_available | drawable/ | xml | 915 |
| ic_badge_4k | drawable/ | xml | 718 |
| ic_badge_hd | drawable/ | xml | 796 |
| ic_badge_sd | drawable/ | xml | 841 |
| ic_check | drawable/ | xml | 364 |
| ic_control_select | drawable/ | xml | 2 007 |
| ic_declined | drawable/ | xml | 1 164 |
| ic_down | drawable/ | xml | 367 |
| ic_folder | drawable/ | xml | 416 |
| ic_heart | drawable/ | xml | 481 |
| ic_house | drawable/ | xml | 398 |
| ic_indigo_spinner | drawable/ | xml | 850 |
| ic_jellyfin | drawable/ | xml | 678 |
| ic_jellyseerr_jellyfish | drawable/ | xml | 4 305 |
| ic_mix | drawable/ | xml | 496 |
| ic_partially_available | drawable/ | xml | 606 |
| ic_pending | drawable/ | xml | 1 010 |
| ic_play | drawable/ | xml | 280 |
| ic_rt_fresh | drawable/ | xml | 1 072 |
| ic_rt_rotten | drawable/ | xml | 1 065 |
| ic_seer | drawable/ | xml | 2 277 |
| ic_shuffle | drawable/ | xml | 451 |
| ic_star | drawable/ | xml | 393 |
| ic_tv | drawable/ | xml | 440 |
| ic_up | drawable/ | xml | 369 |
| ic_user | drawable/ | xml | 391 |
| ic_vegafox | drawable/ | xml | 192 |
| ic_vegafox_fox | drawable/ | png | 279 559 |
| input_default_back | drawable/ | xml | 1 300 |
| input_default_ripple | drawable/ | xml | 420 |
| input_default_text | drawable/ | xml | 572 |
| input_rounded_background | drawable/ | xml | 550 |
| jellyfin_button | drawable/ | xml | 734 |
| jellyfin_button_minimal | drawable/ | xml | 580 |
| jellyseerr_genre_overlay | drawable/ | xml | 305 |
| light_border | drawable/ | xml | 221 |
| moviebg | drawable/ | jpg | 96 484 |
| placeholder_icon | drawable/ | xml | 313 |
| popup_menu_back | drawable/ | xml | 258 |
| progress_bar | drawable/ | xml | 876 |
| qr_code | drawable/ | png | 21 661 |
| qr_jellyfin_docs | drawable/ | xml | 10 634 |
| ripple | drawable/ | xml | 126 |
| seasonal_beach_ball | drawable/ | xml | 3 890 |
| seasonal_beach_umbrella | drawable/ | xml | 3 313 |
| seasonal_bee | drawable/ | xml | 6 458 |
| seasonal_candy | drawable/ | xml | 2 254 |
| seasonal_cherry_blossom | drawable/ | xml | 2 317 |
| seasonal_ghost | drawable/ | xml | 2 675 |
| seasonal_jack_o_lantern | drawable/ | xml | 9 339 |
| seasonal_maple_leaf | drawable/ | xml | 2 846 |
| seasonal_pumpkin | drawable/ | xml | 5 891 |
| seasonal_snowflake | drawable/ | xml | 1 745 |
| seasonal_snowman | drawable/ | xml | 5 236 |
| seasonal_spider | drawable/ | xml | 4 605 |
| seasonal_sun | drawable/ | xml | 2 638 |
| shape_card | drawable/ | xml | 197 |
| switch_background | drawable/ | xml | 475 |
| tile_land_tv | drawable/ | xml | 538 |
| vegafox_ic_channel_background | drawable/ | xml | 179 |
| vegafox_launcher_background | drawable/ | xml | 179 |
| vegafox_splash | drawable/ | png | 193 761 |

---

## 2. Drawables orphelins confirmés (supprimables)

Aucune référence trouvée dans le code source (`app/src/`) via `R.drawable.*`, `@drawable/*`, ni aucune référence indirecte (styles, thèmes, Coil, Manifest, chargement dynamique).

### Raster (images lourdes)

| Fichier | Dossier | Type | Taille | Notes |
|---------|---------|------|--------|-------|
| moviebg.jpg | drawable/ | JPG | 96 484 o (94 Ko) | Image d'arrière-plan inutilisée |

**Sous-total raster : 96 484 octets (94 Ko)**

### Drawables saisonniers (lot complet, jamais chargés)

Mentionnés uniquement dans un commentaire de `VegafoXIcons.kt` (ligne 105) mais aucun code ne les charge -- ni par `R.drawable.*`, ni par `getIdentifier()`, ni par aucun mécanisme dynamique.

| Fichier | Taille | Notes |
|---------|--------|-------|
| seasonal_beach_ball.xml | 3 890 o | |
| seasonal_beach_umbrella.xml | 3 313 o | |
| seasonal_bee.xml | 6 458 o | |
| seasonal_candy.xml | 2 254 o | |
| seasonal_cherry_blossom.xml | 2 317 o | |
| seasonal_ghost.xml | 2 675 o | |
| seasonal_jack_o_lantern.xml | 9 339 o | |
| seasonal_maple_leaf.xml | 2 846 o | |
| seasonal_pumpkin.xml | 5 891 o | |
| seasonal_snowflake.xml | 1 745 o | |
| seasonal_snowman.xml | 5 236 o | |
| seasonal_spider.xml | 4 605 o | |
| seasonal_sun.xml | 2 638 o | |

**Sous-total saisonniers : 53 207 octets (52 Ko)**

### Autres drawables XML orphelins

| Fichier | Dossier | Taille | Notes |
|---------|---------|--------|-------|
| app_logo.xml | drawable/ | 7 082 o | Logo vectoriel Jellyfin "Moonfin" -- plus utilisé |
| app_logo.xml | drawable-v24/ | 7 082 o | Idem (variante API 24) |
| button_text_background.xml | drawable/ | 478 o | Fond de bouton texte, aucune ref en source |
| genre_card_overlay.xml | drawable/ | 305 o | Overlay genre, flaggé aussi par lint |
| gradient_backdrop_overlay.xml | drawable/ | 238 o | Overlay gradient inutilisé |
| ic_available.xml | drawable/ | 915 o | Icône Jellyseerr "available" -- remplacée par VegafoXIcons.Available |
| ic_declined.xml | drawable/ | 1 164 o | Icône Jellyseerr "declined" -- remplacée par VegafoXIcons.Declined |
| ic_folder.xml | drawable/ | 416 o | Icône dossier, flaggé aussi par lint |
| ic_indigo_spinner.xml | drawable/ | 850 o | Spinner indigo inutilisé |
| ic_partially_available.xml | drawable/ | 606 o | Icône Jellyseerr -- remplacée par VegafoXIcons.PartiallyAvailable |
| ic_pending.xml | drawable/ | 1 010 o | Icône Jellyseerr -- remplacée par VegafoXIcons.Pending |
| input_rounded_background.xml | drawable/ | 550 o | Fond arrondi, aucune ref en source |
| jellyseerr_genre_overlay.xml | drawable/ | 305 o | Overlay genre Jellyseerr, flaggé aussi par lint |
| light_border.xml | drawable/ | 221 o | Bordure claire inutilisée |
| progress_bar.xml | drawable/ | 876 o | Barre de progression, flaggé aussi par lint |
| switch_background.xml | drawable/ | 475 o | Fond switch inutilisé |

**Sous-total XML divers : 22 573 octets (22 Ko)**

### Total orphelins

| Catégorie | Fichiers | Taille |
|-----------|----------|--------|
| Raster (JPG) | 1 | 96 484 o (94 Ko) |
| Saisonniers (XML) | 13 | 53 207 o (52 Ko) |
| Autres XML | 16 | 22 573 o (22 Ko) |
| **TOTAL** | **30 fichiers** | **172 264 o (168 Ko)** |

---

## 3. Drawables actifs confirmés

| Drawable | Type | Taille | Référence principale |
|----------|------|--------|----------------------|
| app_banner | xml | 214 o (+182 o anydpi) | `res/layout/view_row_details.xml` |
| app_banner_background | xml | 210 o | `res/drawable/app_banner.xml` |
| app_banner_foreground | xml | 173 o | `res/drawable/app_banner.xml` |
| app_icon_background | xml | 180 o | `res/mipmap-anydpi-v26/app_icon.xml` |
| app_icon_foreground | xml | 192 o | `res/mipmap-anydpi-v26/app_icon.xml`, `PlaybackModule.kt` |
| app_icon_foreground_monochrome | xml | 274 o | `res/mipmap-anydpi-v26/app_icon.xml` |
| bg_login_card | xml | 326 o | `res/layout/fragment_server_add.xml` |
| bg_login_gradient | xml | 368 o | `res/layout/fragment_server_add.xml` |
| blank10x10 | png | 182 o | `ItemRowView.kt`, `res/layout/view_row_details.xml` |
| button_default_back | xml | 1 244 o | `res/values/styles.xml` |
| button_default_ripple | xml | 421 o | `res/values/styles.xml`, `res/layout/item_row.xml` |
| button_default_text | xml | 577 o | `res/values/styles.xml`, `res/layout/item_row.xml` |
| button_icon_back | xml | 529 o | `res/values/styles.xml` |
| button_icon_ripple | xml | 354 o | `res/values/styles.xml` |
| button_icon_tint | xml | 767 o | `res/values/styles.xml`, `res/layout/text_under_button.xml` |
| button_icon_tint_animated | xml | 577 o | `res/values/styles.xml` |
| chevron_left | xml | 308 o | `SyncPlayDialog.kt` |
| detail_backdrop_gradient | xml | 306 o | `ItemDetailsFragment.kt` |
| expanded_text | xml | 355 o | `ExpandableTextView.kt` |
| favorites | jpg | 25 243 o | `MusicFavoritesListFragment.kt` |
| ic_add | xml | 301 o | `ItemListFragment.kt` |
| ic_album | xml | 528 o | `NowPlayingView.kt`, `ItemListFragment.kt` |
| ic_badge_4k | xml | 718 o | `MediaMetadataBadges.kt` |
| ic_badge_hd | xml | 796 o | `MediaMetadataBadges.kt` |
| ic_badge_sd | xml | 841 o | `MediaMetadataBadges.kt` |
| ic_check | xml | 364 o | `jellyseerr/SettingsFragment.kt` |
| ic_control_select | xml | 2 007 o | `res/layout/popup_expandable_text_view.xml` |
| ic_down | xml | 367 o | `res/layout/item_row.xml` |
| ic_heart | xml | 481 o | `ItemListFragment.kt` |
| ic_house | xml | 398 o | `res/layout/clock_user_bug.xml` |
| ic_jellyfin | xml | 678 o | `SettingsAuthenticationScreen.kt`, `SettingsMainScreen.kt`, `UpdateCheckWorker.kt` |
| ic_jellyseerr_jellyfish | xml | 4 305 o | `LeftSidebarNavigation.kt`, `SettingsPluginScreen.kt`, `SettingsJellyseerrScreen.kt` |
| ic_mix | xml | 496 o | `ItemListFragment.kt` |
| ic_play | xml | 280 o | `ItemRowView.kt`, `ItemListFragment.kt`, `res/layout/text_under_button.xml` |
| ic_rt_fresh | xml | 1 072 o | `MediaMetadataBadges.kt`, `RatingIconProvider.kt` |
| ic_rt_rotten | xml | 1 065 o | `MediaMetadataBadges.kt`, `RatingIconProvider.kt` |
| ic_seer | xml | 2 277 o | `LeftSidebarNavigation.kt` |
| ic_shuffle | xml | 451 o | `ItemListFragment.kt`, `MusicFavoritesListFragment.kt`, `res/layout/clock_user_bug.xml` |
| ic_star | xml | 393 o | `browsing/composable/inforow/rating.kt` |
| ic_tv | xml | 440 o | `res/drawable/tile_land_tv.xml` (ref interne) |
| ic_up | xml | 369 o | `res/layout/item_row.xml` |
| ic_user | xml | 391 o | `ItemListFragment.kt` |
| ic_vegafox | xml | 192 o | `res/layout/fragment_server_add.xml` |
| ic_vegafox_fox | png | 279 559 o | `Toolbar.kt`, `MainToolbar.kt`, `SettingsPluginScreen.kt`, `SettingsAboutScreen.kt`, `VegafoXFoxLogo.kt` |
| input_default_back | xml | 1 300 o | `res/values/styles.xml` |
| input_default_ripple | xml | 420 o | `res/values/styles.xml` |
| input_default_text | xml | 572 o | `res/values/styles.xml` |
| jellyfin_button | xml | 734 o | `ItemRowView.kt` |
| jellyfin_button_minimal | xml | 580 o | `res/layout/view_button_alpha_picker.xml` |
| placeholder_icon | xml | 313 o | `ImageProvider.kt` (Coil error drawable) |
| popup_menu_back | xml | 258 o | `res/values/styles.xml` |
| qr_code | png | 21 661 o | `DonateDialog.kt` |
| qr_jellyfin_docs | xml | 10 634 o | `ConnectHelpAlertFragment.kt` |
| ripple | xml | 126 o | `res/layout/view_button_alpha_picker.xml` |
| shape_card | xml | 197 o | `res/layout/view_row_details.xml` |
| tile_land_tv | xml | 538 o | `MediaContentProvider.kt`, `LeanbackChannelWorker.kt` |
| vegafox_ic_channel_background | xml | 179 o | `res/mipmap-anydpi-v26/vegafox_ic_channel.xml` |
| vegafox_launcher_background | xml | 179 o | `res/mipmap-anydpi-v26/vegafox_launcher.xml`, `vegafox_launcher_round.xml` |
| vegafox_splash | png | 193 761 o | `SplashFragment.kt` |

**Total actifs : 59 ressources (88 fichiers physiques en comptant les variantes)**

---

## 4. Drawables suspects (verification manuelle recommandée)

Aucun drawable dans une zone d'incertitude. Tous les drawables ont été classés de manière certaine (orphelin ou actif). Les recherches couvrent :
- `R.drawable.*` dans tous les fichiers Kotlin/Java de `app/src/`
- `@drawable/*` dans tous les fichiers XML de `app/src/`
- Références croisées entre drawables XML (ex: `app_banner.xml` -> `app_banner_background`)
- Références depuis les mipmaps (ex: `app_icon.xml` -> `app_icon_foreground`)
- Chargement dynamique via `getIdentifier()` (aucune occurrence trouvée)
- Utilisation Coil (placeholder/error/fallback)
- AndroidManifest.xml (utilise des mipmaps, pas de drawables directs)
- `lint-baseline.xml` confirme que `genre_card_overlay`, `ic_folder`, `progress_bar`, `jellyseerr_genre_overlay` sont deja flagges comme inutilises

---

## 5. Resume et gain estimé

### Gain par suppression des orphelins

| | Fichiers | Taille |
|---|----------|--------|
| Drawables orphelins supprimables | 30 | 168 Ko |
| Drawables actifs conservés | 59 | 550 Ko |
| **Total actuel** | **89** | **718 Ko** |

**Gain estimé : 168 Ko (23% de la taille totale des drawables)**

### Priorité de suppression

1. **moviebg.jpg** (94 Ko) -- le plus gros gain individuel, image raster inutilisée
2. **seasonal_\*.xml** (13 fichiers, 52 Ko) -- lot complet jamais intégré, code saisonnier abandonné
3. **app_logo.xml** (2 fichiers, 14 Ko) -- ancien logo Jellyfin "Moonfin", remplacé par les assets VegafoX
4. **ic_available/declined/partially_available/pending** (4 fichiers, 4 Ko) -- remplacés par VegafoXIcons (Material Symbols)
5. **Divers** (button_text_background, genre_card_overlay, gradient_backdrop_overlay, ic_folder, ic_indigo_spinner, input_rounded_background, jellyseerr_genre_overlay, light_border, progress_bar, switch_background) -- 10 fichiers, 5 Ko total

### Observations

- Les drawables les plus lourds sont les PNG : `ic_vegafox_fox.png` (273 Ko), `vegafox_splash.png` (189 Ko), `moviebg.jpg` (94 Ko). Seul `moviebg.jpg` est orphelin.
- Les 13 drawables saisonniers semblent etre un feature preparé mais jamais implémenté. `VegafoXIcons.kt` les mentionne en commentaire mais aucun code ne les charge.
- 4 icones Jellyseerr de statut (`ic_available`, `ic_declined`, `ic_partially_available`, `ic_pending`) ont été remplacées par des Material Icons dans `VegafoXIcons` (`Available`, `Declined`, `PartiallyAvailable`, `Pending`).
- `ic_indigo_spinner.xml` est un spinner probablement hérité de Jellyfin, remplacé par `VegafoXIcons.Spinner` (HourglassEmpty).
- `ic_folder.xml` est remplacé par `VegafoXIcons.Folder` (Material Icons.Default.Folder).
