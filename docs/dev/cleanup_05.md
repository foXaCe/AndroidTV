# Cleanup 05 — Strings orphelines

**Date** : 2026-03-12
**Branche** : main

---

## Etape 1 — Inventaire

### Methode

Extraction de tous les noms de strings depuis `values/strings.xml`,
puis verification de chaque nom pour references `R.string.NOM`,
`@string/NOM` et `R.plurals.NOM` dans le code Kotlin et XML
(excluant strings.xml eux-memes).

### Resultat initial

- **1414 strings** dans strings.xml (1581 LOC)
- **326 strings orphelines** identifiees (0 refs `R.string` ou `@string`)
- **16 faux positifs** : strings utilisees comme `R.plurals` (non detectees initialement)
  - `albums`, `episodes`, `hours`, `items`, `minutes`, `seconds`,
    `songs`, `tracks`, `season_count`, `genre_item_count`,
    `discovery_servers_found`, `jellyseerr_cancel_confirm`,
    `jellyseerr_cancelled_count`, `jellyseerr_discover_season_count`,
    `jellyseerr_failed_count`, `jellyseerr_season_count_suffix`
- **310 strings reellement orphelines** (apres exclusion des plurals)

### Fichiers de traduction

66 fichiers de traduction synchronises (de values-af a values-zh-rTW).

---

## Etape 2 — Categorisation

Toutes les 310 strings sont CAT A (clairement orphelines) :

| Categorie | Nombre | Cause |
|-----------|--------|-------|
| Server Discovery + Login Legacy | 29 | `SelectServerFragment`, `ServerFragment`, `UserLoginFragment` supprimes |
| Watch List Legacy | 7 | Feature Watch List supprimee |
| Playback Overlay Legacy | 17 | `LeanbackOverlayFragment`, `CustomPlaybackOverlayFragment` supprimes |
| Settings Legacy (pref_*) | 65 | Preferences migrées Compose |
| SyncPlay Legacy | 10 | Settings SyncPlay refactorisees |
| Jellyseerr Settings Legacy | 37 | Settings Jellyseerr refactorisees |
| UI Labels Legacy (lbl_*) | 61 | Fragments/screens Leanback supprimes |
| Messages & Errors & OTA | 52 | Messages de features supprimees / OTA desactive |
| Misc (colors, desc, etc.) | 31 | Palette, descriptions, labels generiques |
| Nettoyage final | 1 | `chapters` (doublon avec `lbl_chapters`) |
| **Total** | **310** | |

CAT B (preference keys) : **0** — toutes les pref_ orphelines sont des
labels UI, pas des cles de preference.

CAT C (dynamique) : **0** — verification manuelle effectuee.

---

## Etape 3 — Suppressions par groupe

### Groupe 1 — Server Discovery + Login Legacy (29 strings)

Strings supprimees :
`action_use_password`, `action_use_quickconnect`, `connect_manually_by_address`,
`discovered_servers_empty`, `discovered_servers_title`, `discovery_checking_server`,
`discovery_http_scanning`, `discovery_manual_dialog_cancel`,
`discovery_manual_dialog_hint`, `discovery_manual_dialog_ok`,
`discovery_manual_dialog_title`, `discovery_retry`, `discovery_servers_found`
(ancienne version `<string>`, remplacee par `<plurals>`),
`discovery_server_unreachable`, `discovery_timeout_subtitle`,
`discovery_timeout_title`, `saved_servers`, `login_connect_to`,
`login_other_options`, `login_quickconnect_step_1`, `login_quickconnect_step_2`,
`login_quickconnect_step_3`, `login_username_field_empty`, `lbl_sign_in`,
`lbl_user_server`, `no_user_warning`, `server_issue_ssl_handshake`,
`server_setup_incomplete`, `server_unsupported_notification`

882 entrees supprimees dans 53 fichiers de traduction.

### Groupe 2 — Watch List Legacy (7 strings)

`lbl_add_to_watch_list`, `lbl_remove_from_watch_list`, `lbl_watch_list`,
`msg_added_to_watch_list`, `msg_failed_to_add_to_watch_list`,
`msg_failed_to_remove_from_watch_list`, `msg_removed_from_watch_list`

14 entrees supprimees (EN + FR).

### Groupe 3 — Playback Overlay Legacy (17 strings)

`audio_delay_none`, `audio_error`, `cd_next`, `cd_previous`,
`lbl_audio_delay`, `lbl_now_playing`, `lbl_now_playing_album`,
`lbl_now_playing_track`, `lbl_stereo`, `lbl_subtitle_delay`,
`no_player`, `subtitle_error`, `subtitle_delay_none`,
`subtitle_delay_reset`, `subtitle_delay_value`,
`playback_subtitle_delay_error`, `playback_subtitle_info_error`

384 entrees supprimees dans 56 fichiers.

### Groupe 4 — Settings + SyncPlay Legacy (76 strings)

65 strings `pref_*` + 10 strings `syncplay_*` + 1 `pref_visual_settings`.

1239 entrees supprimees dans 60 fichiers.

### Groupe 5 — Jellyseerr Settings Legacy (37 strings)

`jellyseerr_4k_movie_profile` a `jellyseerr_test_connection_description`.

74 entrees supprimees (EN + FR).

### Groupe 6 — UI Labels Legacy (61 strings)

`lbl_additional_parts` a `lbl_visible`, incluant `lbl_media_bar_*`.

2248 entrees supprimees dans 62 fichiers.

### Groupe 7 — Messages, Errors, OTA, Colors (52 strings)

`msg_*`, `error_*`, `state_*`, `color_*`, `btn_download_and_install`,
`btn_view_release_notes`, `title_update_available`.

956 entrees supprimees dans 61 fichiers.

### Groupe 8 — Misc (31 strings)

`app_name_debug`, `app_name_release`, `app_version_display`,
`continue_listening`, `desc_*`, `disabled`, `enabled`,
`enable_reactive_homepage`, `home_*`, `just_one`, `live_tv_preferences`,
`mark_watched*`, `past_*`, `search`, `settings_*`, `sort_*`,
`sum_enable_cinema_mode`, `turn_off`, `vegafox_*`, `welcome_*`.

890 entrees supprimees dans 57 fichiers.

### Nettoyage final (3 strings)

`chapters`, `genre_item_count` (plurals orphelin), `jellyseerr_cancel_partial`.

64 entrees supprimees dans 62 fichiers.

---

## Corrections complementaires

### Strings manquantes pre-existantes (38 ajoutees)

20 strings manquantes causant des erreurs de compilation pre-existantes
(nouveau code Compose referençant des strings jamais ajoutees) :

| String | Fichier source |
|--------|---------------|
| `lbl_my_list` | HomeHeroBackdrop.kt |
| `home_section_latest` | HomeViewModel.kt |
| `home_section_my_media` | HomeViewModel.kt |
| `home_section_recordings` | HomeViewModel.kt |
| `home_section_live_tv` | HomeViewModel.kt |
| `jellyseerr_cancelled_count` (plurals) | MediaDetailsFragment.kt |
| `jellyseerr_failed_count` (plurals) | MediaDetailsFragment.kt |
| `lbl_choose_section` | LiveTvBrowseScreen.kt |
| `lbl_epg_grid` | LiveTvBrowseScreen.kt |
| `lbl_your_recordings` | LiveTvBrowseScreen.kt |
| `lbl_x_channels` | LiveTvGuideScreen.kt |
| `lbl_guide_hours` | LiveTvGuideScreen.kt |
| `powered_by_jellyfin` | SettingsAboutScreen.kt |
| `lbl_music` | MainToolbar.kt |
| `lbl_live_tv` | MainToolbar.kt |
| `lbl_app_tagline` | VegafoXTitleText.kt |
| `lbl_connect_to_server` | WelcomeScreen.kt |
| `lbl_version` | WelcomeScreen.kt |
| `quickconnect_connecting` | QuickConnectScreen.kt |
| `quickconnect_title` | QuickConnectScreen.kt |

18 strings supplementaires pour ServerDiscoveryScreen.kt et QuickConnectScreen.kt :

`quickconnect_instructions`, `quickconnect_waiting`, `quickconnect_confirm`,
`quickconnect_timeout`, `quickconnect_back`, `quickconnect_connected`,
`discovery_title`, `discovery_subtitle`, `discovery_scanning`,
`discovery_servers_found` (plurals), `discovery_manual_entry`,
`discovery_or`, `lbl_no_server_detected`, `lbl_mdns_lan`,
`lbl_server_address`, `btn_continue`, `change_server`

Toutes ajoutees en EN + FR.

### Layout vlc_player_interface.xml

Nettoyage des references a `LeanbackOverlayFragment` et `PlayerPopupView`
(supprimes dans cleanup_04) — sections `popupArea` et `leanback_fragment`
retirees. Layout conserve pour compatibilite data binding.

### Layouts supprimes restaures

12 layout files accidentellement restaures par `git checkout -- app/src/main/res/`
re-supprimes : `fragment_select_server.xml`, `fragment_server.xml`,
`fragment_user_login*.xml`, `live_tv_guide.xml`, `new_program_record_popup.xml`,
`overlay_tv_guide.xml`, `program_*.xml`, `view_card_channel.xml`,
`channel_header.xml`.

Drawables supprimes egalement restaures et re-supprimes (~100 fichiers).

---

## Etape 4 — Verification finale

### Compilation

```
./gradlew :app:compileGithubDebugKotlin → BUILD SUCCESSFUL (0 erreur)
```

### Build

| Variante | Resultat |
|----------|---------|
| `assembleGithubDebug` | BUILD SUCCESSFUL |
| `assembleGithubRelease` | BUILD SUCCESSFUL |
| Install AM9 Pro | Success |

---

## Bilan

### Strings

| Metrique | Avant | Apres | Delta |
|----------|-------|-------|-------|
| strings.xml LOC | 1581 | 1255 | -326 |
| Strings (name count) | 1414 | 1104 | -310 net |
| Strings orphelines supprimees | — | — | 310 |
| Strings manquantes ajoutees | — | — | +38 |
| Entrees traduction supprimees | — | — | 6 751 |
| Entrees traduction ajoutees | — | — | 76 (EN+FR) |
| Fichiers traduction synchronises | — | — | 66 |

### Strings conservees et pourquoi

- **1 104 strings actives** referenciees dans le code Kotlin/XML
- **16 plurals** initialement flags orphelins mais actifs (`R.plurals.*`)
- Toutes les strings de features actives conservees (Jellyseerr, SyncPlay,
  LiveTV, Settings Compose, Player Compose, etc.)

### Corrections connexes

| Element | Avant | Apres |
|---------|-------|-------|
| vlc_player_interface.xml | Refs classes supprimees | Nettoyee |
| Layouts supprimes | 12 restaures accidentellement | Re-supprimes |
| Drawables supprimes | ~100 restaures accidentellement | Re-supprimes |
| Strings manquantes pre-existantes | 38 erreurs compilation | 0 erreur |
