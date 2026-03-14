# Audit complet des icones VegafoX Android TV

> Date : 2026-03-11
> Objectif : Recenser toutes les icones avant migration vers Material Symbols

---

## 1. Icones Material Icons Extended (Compose)

### Dependance actuelle

```toml
# gradle/libs.versions.toml
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version.ref = "androidx-compose-foundation" }
# version = 1.10.4 (via androidx-compose-foundation)
```

### Usage dans le code

**Seule 1 icone Material Icons Extended est utilisee dans tout le projet :**

| Icone | Style | Fichier | Ligne |
|-------|-------|---------|-------|
| `Icons.AutoMirrored.Outlined.ArrowForward` | AutoMirrored Outlined | `ui/startup/compose/WelcomeScreen.kt` | 208 |

> **Constat** : L'app utilise quasi exclusivement des **drawables XML vectoriels** charges via `ImageVector.vectorResource(R.drawable.ic_xxx)` ou `painterResource(R.drawable.ic_xxx)` au lieu de `Icons.Xxx.Yyy`.

---

## 2. Drawables XML — Inventaire complet

### 2.1 Drawables ic_*.xml ACTIFS (90 fichiers)

| Drawable | Refs | Fichiers principaux |
|----------|------|-------------------|
| `ic_add` | 3 | DetailActions, SyncPlayDialog, ItemListFragment, CreatePlaylistDialog |
| `ic_adjust` | 1 | SettingsMainScreen |
| `ic_album` | 6 | AudioNowPlayingScreen, MusicBrowseFragment, NowPlayingView |
| `ic_arrow_back` | 1 | fragment_user_login.xml |
| `ic_artist` | 3 | MusicBrowseFragment, tile_artists.xml |
| `ic_aspect_ratio` | 2 | PlayerOverlayScreen, SettingsHomeScreen |
| `ic_audio_sync` | 1 | PlayerOverlayScreen |
| `ic_available` | 2 | ItemCardJellyseerrOverlay, RequestsAdapter |
| `ic_calendar` | 1 | LiveTvGuideScreen |
| `ic_cast_list` | 1 | PlayerOverlayScreen |
| `ic_channel_bar` | 1 | PlayerOverlayScreen |
| `ic_check` | 5 | RadioButton, Checkbox, DetailActions, SeasonDetailsContent, SettingsAsyncActionListButton, ConnectHelpAlertFragment |
| `ic_clapperboard` | 3 | LeftSidebarNavigation, ExpandableLibrariesButton, SettingsPlaybackScreen, tile_port_video/tile_chapter |
| `ic_control_select` | 1 | popup_expandable_text_view.xml |
| `ic_declined` | 1 | RequestsAdapter |
| `ic_delete` | 3 | DetailActions, SeriesTimerDetailsContent, SettingsAuthenticationServerScreen/UserScreen, JellyseerrRequestButtons |
| `ic_down` | 6 | ItemDetailsComponents, genres_grid_browse.xml, item_row.xml, SettingsHomeScreen, SettingsJellyseerrRowsScreen |
| `ic_error` | 3 | ErrorBanner, ErrorState, ServerButtonView, SettingsMainScreen, SettingsAsyncActionListButton |
| `ic_fast_forward` | 4 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, VideoPlayerOverlay |
| `ic_filter` | 2 | LiveTvGuideScreen, GenresGridScreen, JellyseerrBrowseByScreen |
| `ic_flask` | 1 | SettingsMainScreen |
| `ic_folder` | 4 | LeftSidebarNavigation, AddToPlaylistDialog, ShuffleOptionsDialog, SettingsLibrariesScreen |
| `ic_get_app` | 1 | SettingsMainScreen |
| `ic_grid` | 3 | SettingsCustomizationScreen, SettingsHomeScreen, SettingsJellyseerrScreen |
| `ic_guide` | 3 | PlayerOverlayScreen, DetailActions, SettingsAboutScreen |
| `ic_heart` | 8 | ItemCardBaseItemOverlay, ItemDetailsComponents, DetailActions, LeftSidebarNavigation, LibraryBrowseComponents, SeasonDetailsContent, ProgramDetailDialog, SettingsMainScreen |
| `ic_heart_red` | 2 | ProgramDetailDialog, ChannelHeaderComposable |
| `ic_help` | 1 | StartupToolbar |
| `ic_house` | 8 | LeftSidebarNavigation, MainToolbar, ServerButtonView, clock_user_bug.xml, fragment_server.xml, SettingsCustomizationScreen, SettingsAuthenticationScreen, divers BrowseFragments |
| `ic_house_edit` | 1 | ServerButtonView |
| `ic_indigo_spinner` | 3 | ItemCardJellyseerrOverlay, RequestsAdapter, ic_indigo_spinner_animated.xml |
| `ic_jellyfin` | 2 | UpdateCheckWorker, SettingsMainScreen, SettingsAuthenticationScreen |
| `ic_jellyseerr_jellyfish` | 3 | LeftSidebarNavigation, SettingsPluginScreen, SettingsJellyseerrScreen |
| `ic_lightbulb` | 1 | SettingsJellyseerrScreen |
| `ic_lock` | 3 | SettingsPluginScreen, SettingsJellyseerrScreen, SettingsAuthenticationScreen |
| `ic_logout` | 3 | SyncPlayDialog, SettingsJellyseerrScreen, SettingsAuthenticationServerUserScreen |
| `ic_loop` | 2 | AudioNowPlayingScreen, DetailActions |
| `ic_masks` | 3 | LeftSidebarNavigation, ShuffleOptionsDialog, MusicBrowseFragment |
| `ic_microphone` | 1 | SearchVoiceInput |
| `ic_mix` | 2 | DetailActions, ItemListFragment |
| `ic_more` | 2 | VideoPlayerControls, SettingsPlaybackScreen |
| `ic_movie` | 3 | MainToolbar, ItemDetailsComponents, BrowseMediaCard |
| `ic_music_album` | 1 | MainToolbar, tile_audio.xml |
| `ic_next` | 4 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, PhotoPlayerControls, SkipOverlayView, SettingsMainScreen |
| `ic_next_up` | 1 | SettingsPlaybackScreen |
| `ic_partially_available` | 2 | ItemCardJellyseerrOverlay, RequestsAdapter |
| `ic_pause` | 5 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, VideoPlayerOverlay, PhotoPlayerControls |
| `ic_pencil` | 1 | fragment_server.xml |
| `ic_pending` | 1 | RequestsAdapter |
| `ic_photo` | 2 | tile_land_photo.xml |
| `ic_photos` | 1 | SettingsMainScreen |
| `ic_play` | 12 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, VideoPlayerOverlay, PhotoPlayerControls, ItemListFragment, MusicFavoritesListFragment, DetailActions, SeasonDetailsContent, LiveTvDetailsContent, JellyseerrRequestButtons, ItemRowView |
| `ic_playback_speed` | 1 | PlayerOverlayScreen |
| `ic_previous` | 4 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, PhotoPlayerControls |
| `ic_record` | 5 | LiveTvDetailsContent, PlayerOverlayScreen, LiveTvBrowseFragment, RecordingsBrowseFragment |
| `ic_record_red` | 2 | PlayerOverlayScreen, ProgramCellComposable |
| `ic_record_series` | 4 | ItemCardBaseItemOverlay, DetailActions, LiveTvBrowseFragment, RecordingsBrowseFragment, SeriesRecordingsBrowseFragment |
| `ic_record_series_red` | 1 | ProgramCellComposable |
| `ic_refresh` | 3 | SyncPlayDialog, SettingsHomeScreen, SettingsJellyseerrRowsScreen |
| `ic_rewind` | 4 | AudioNowPlayingScreen, PlayerOverlayScreen, VideoPlayerControls, VideoPlayerOverlay |
| `ic_rt_fresh` | 1 | RatingIconProvider |
| `ic_rt_rotten` | 1 | RatingIconProvider |
| `ic_search` | 2 | LeftSidebarNavigation, SearchTextInput |
| `ic_seer` | 1 | LeftSidebarNavigation |
| `ic_select_audio` | 2 | PlayerOverlayScreen, DetailActions |
| `ic_select_chapter` | 1 | PlayerOverlayScreen |
| `ic_select_quality` | 2 | PlayerOverlayScreen, JellyseerrRequestButtons |
| `ic_select_subtitle` | 2 | PlayerOverlayScreen, DetailActions |
| `ic_settings` | 4 | LiveTvGuideScreen, LeftSidebarNavigation, StartupToolbar, MainToolbar, SettingsJellyseerrScreen |
| `ic_shuffle` | 4 | AudioNowPlayingScreen, DetailActions, ItemListFragment, MusicBrowseFragment, clock_user_bug.xml |
| `ic_sort` | 2 | GenresGridScreen, LibraryBrowseScreen, JellyseerrBrowseByScreen |
| `ic_star` | 3 | rating.kt, SettingsPluginScreen |
| `ic_subtitle_sync` | 1 | PlayerOverlayScreen |
| `ic_subtitles` | 1 | SettingsPlaybackScreen |
| `ic_syncplay` | 2 | LeftSidebarNavigation, SettingsPlaybackScreen, SyncPlayDialog |
| `ic_time` | 2 | ItemDetailsComponents, BaseItemInfoRow, LiveTvGuideScreen |
| `ic_trailer` | 2 | DetailActions, JellyseerrRequestButtons, SettingsPlaybackScreen |
| `ic_tv` | 4 | MainToolbar, DetailActions, LiveTvBrowseFragment, tile_port_tv/tile_tv/tile_land_tv |
| `ic_tv_guide` | 2 | MainToolbar, LiveTvBrowseFragment |
| `ic_tv_play` | 2 | SettingsPlaybackScreen, tile_port_record.xml |
| `ic_tv_timer` | 5 | ScheduleBrowseFragment, LiveTvBrowseFragment, RecordingsBrowseFragment |
| `ic_up` | 5 | ItemDetailsComponents, item_row.xml, SettingsHomeScreen, SettingsJellyseerrRowsScreen |
| `ic_upload` | 1 | SettingsAsyncActionListButton |
| `ic_user` | 3 | AudioNowPlayingScreen, ItemListFragment, SettingsJellyseerrScreen, ProfilePicture |
| `ic_user_add` | 1 | fragment_server.xml |
| `ic_users` | 1 | SettingsMainScreen |
| `ic_vegafox` | 12 | Toolbar, SettingsMainScreen, SettingsPluginScreen, SettingsJellyseerrScreen, fragment_server/server_add/user_login/select_server.xml |
| `ic_vegafox_fox` | 2 | MainToolbar, VegafoXFoxLogo |
| `ic_watch` | 4 | ItemCardBaseItemOverlay, ItemDetailsComponents, LibraryBrowseComponents |
| `ic_zzz` | 1 | SettingsPlaybackScreen |
| `ic_abc` | 1 | tile_letters.xml |

### 2.2 Drawables ic_*.xml MORTS (16 fichiers) — a supprimer

| Drawable | Raison |
|----------|--------|
| `ic_4k` | Aucune reference |
| `ic_camera` | Aucune reference |
| `ic_decrease` | Aucune reference |
| `ic_increase` | Aucune reference |
| `ic_indigo_spinner_animated` | Aucune reference |
| `ic_jellyseerr_logo` | Aucune reference |
| `ic_jump_letter` | Aucune reference |
| `ic_key` | Aucune reference |
| `ic_movie_badge` | Aucune reference |
| `ic_previous_episode` | Aucune reference |
| `ic_resume` | Aucune reference |
| `ic_series_badge` | Aucune reference |
| `ic_switch_users` | Aucune reference |
| `ic_trash` | Aucune reference |
| `ic_unwatch` | Aucune reference |
| `ic_vegafox_white` | Aucune reference |

### 2.3 Autres drawables XML MORTS (23 fichiers) — a supprimer

| Drawable | Type |
|----------|------|
| `audio_now_playing_album_background` | Background |
| `block_text_bg` | Background |
| `button_bar_back` | UI component |
| `button_icon_back` | UI component |
| `button_icon_ripple` | UI component |
| `button_icon_tint_animated` | UI component |
| `chevron_right` | Navigation |
| `circle_accent` | Shape |
| `circle_background` | Shape |
| `dark_green_gradient` | Gradient |
| `default_genre_backdrop` | Background |
| `green_gradient` | Gradient |
| `ica_play_pause` | Animation |
| `input_default_back` | UI component |
| `input_default_ripple` | UI component |
| `input_default_text` | UI component |
| `layer_background` | Background |
| `popup_menu_back` | UI component |
| `progress_bar` | UI component |
| `red_gradient` | Gradient |
| `shape_card_circle` | Shape |
| `shape_card_image_background` | Shape |
| `subtitle_background` | Background |

### 2.4 Tile drawables MORTS (20 fichiers) — a supprimer

| Drawable |
|----------|
| `tile_album_artists` |
| `tile_artists` |
| `tile_audio` |
| `tile_chapter` |
| `tile_genres` |
| `tile_land_folder` |
| `tile_land_photo` |
| `tile_land_series_timer` |
| `tile_letters` |
| `tile_port_folder` |
| `tile_port_grid` |
| `tile_port_guide` |
| `tile_port_person` |
| `tile_port_record` |
| `tile_port_series_timer` |
| `tile_port_time` |
| `tile_port_tv` |
| `tile_port_video` |
| `tile_suggestions` |
| `tile_tv` |

> **Note** : `tile_land_tv` est encore reference dans LeanbackChannelWorker et MediaContentProvider — il est ACTIF.

---

## 3. Assets PNG/WebP

### 3.1 PNG (25 fichiers)

| Asset | Emplacement | Densites | Actif |
|-------|-------------|----------|-------|
| `app_banner.png` | mipmap | mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Oui |
| `app_icon.png` | mipmap | mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Oui |
| `vegafox_ic_banner.png` | mipmap-xhdpi | xhdpi | Oui |
| `vegafox_ic_banner_foreground.png` | mipmap-xhdpi | xhdpi | Oui |
| `vegafox_ic_channel.png` | mipmap-xhdpi | xhdpi | Oui |
| `vegafox_channel_foreground.png` | mipmap-xhdpi | xhdpi | Oui |
| `vegafox_login_background.png` | drawable | - | Oui (bg_login_gradient) |
| `vegafox_splash.png` | drawable | - | Oui (SplashFragment) |
| `ic_vegafox_fox.png` | drawable | - | Oui (MainToolbar, VegafoXFoxLogo) |
| `vegafox_launcher-playstore.png` | src/main | - | Non (store asset) |
| `qr_code.png` | drawable | - | Oui (DonateDialog) |
| `blank10x10.png` | drawable | - | Oui (item_row.xml, ItemRowView) |
| `blank20x20.png` | drawable | - | ? |
| `blank30x30.png` | drawable | - | ? |
| `banner_edge_future.png` | drawable | - | ? |
| `banner_edge_disc.png` | drawable | - | ? |
| `banner_edge_missing.png` | drawable | - | ? |

### 3.2 WebP (15 fichiers)

| Asset | Emplacement | Densites | Actif |
|-------|-------------|----------|-------|
| `vegafox_launcher.webp` | mipmap | mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Oui |
| `vegafox_launcher_foreground.webp` | mipmap | mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Oui |
| `vegafox_launcher_round.webp` | mipmap | mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Oui |

---

## 4. Tableau de migration Icons Extended -> Material Symbols

### 4.1 Icone Compose unique

| Ancien (Material Icons Extended) | Equivalent Material Symbols | Style | Fichier | Ligne |
|----------------------------------|---------------------------|-------|---------|-------|
| `Icons.AutoMirrored.Outlined.ArrowForward` | `arrow_forward` | Outlined | WelcomeScreen.kt | 208 |

### 4.2 Drawables XML -> Equivalents Material Symbols

La majorite des icones sont des drawables XML vectoriels. Voici le tableau de correspondance complet :

| Drawable actuel | Equivalent Material Symbols | Correspondance |
|-----------------|---------------------------|----------------|
| `ic_add` | `add` | Exact |
| `ic_adjust` | `tune` | Exact |
| `ic_album` | `album` | Exact |
| `ic_arrow_back` | `arrow_back` | Exact |
| `ic_artist` | `person` | Proche |
| `ic_aspect_ratio` | `aspect_ratio` | Exact |
| `ic_audio_sync` | `sync` | Proche |
| `ic_available` | `check_circle` | Proche |
| `ic_calendar` | `calendar_today` | Exact |
| `ic_cast_list` | `cast` | Exact |
| `ic_channel_bar` | `live_tv` | Proche |
| `ic_check` | `check` | Exact |
| `ic_clapperboard` | `movie` | Proche |
| `ic_control_select` | `expand_more` | Proche |
| `ic_declined` | `cancel` | Proche |
| `ic_delete` | `delete` | Exact |
| `ic_down` | `keyboard_arrow_down` / `expand_more` | Exact |
| `ic_error` | `error` | Exact |
| `ic_fast_forward` | `fast_forward` | Exact |
| `ic_filter` | `filter_list` | Exact |
| `ic_flask` | `science` | Exact |
| `ic_folder` | `folder` | Exact |
| `ic_get_app` | `download` | Exact |
| `ic_grid` | `grid_view` | Exact |
| `ic_guide` | `menu_book` | Proche |
| `ic_heart` | `favorite` | Exact |
| `ic_heart_red` | `favorite` (tint red) | Exact |
| `ic_help` | `help` | Exact |
| `ic_house` | `home` | Exact |
| `ic_house_edit` | `edit_location` | Proche |
| `ic_indigo_spinner` | `hourglass_empty` | Proche |
| `ic_jellyfin` | **Custom** (logo Jellyfin) | Aucun |
| `ic_jellyseerr_jellyfish` | **Custom** (logo Jellyseerr) | Aucun |
| `ic_lightbulb` | `lightbulb` | Exact |
| `ic_lock` | `lock` | Exact |
| `ic_logout` | `logout` | Exact |
| `ic_loop` | `loop` | Exact |
| `ic_masks` | `theater_comedy` | Exact |
| `ic_microphone` | `mic` | Exact |
| `ic_mix` | `auto_awesome` | Proche |
| `ic_more` | `more_horiz` | Exact |
| `ic_movie` | `movie` | Exact |
| `ic_music_album` | `library_music` | Proche |
| `ic_next` | `skip_next` | Exact |
| `ic_next_up` | `upcoming` | Proche |
| `ic_partially_available` | `downloading` | Proche |
| `ic_pause` | `pause` | Exact |
| `ic_pencil` | `edit` | Exact |
| `ic_pending` | `pending` | Exact |
| `ic_photo` | `photo` | Exact |
| `ic_photos` | `photo_library` | Exact |
| `ic_play` | `play_arrow` | Exact |
| `ic_playback_speed` | `speed` | Exact |
| `ic_previous` | `skip_previous` | Exact |
| `ic_record` | `fiber_manual_record` | Exact |
| `ic_record_red` | `fiber_manual_record` (tint red) | Exact |
| `ic_record_series` | `fiber_smart_record` | Exact |
| `ic_record_series_red` | `fiber_smart_record` (tint red) | Exact |
| `ic_refresh` | `refresh` | Exact |
| `ic_rewind` | `fast_rewind` | Exact |
| `ic_rt_fresh` | **Custom** (Rotten Tomatoes) | Aucun |
| `ic_rt_rotten` | **Custom** (Rotten Tomatoes) | Aucun |
| `ic_search` | `search` | Exact |
| `ic_seer` | **Custom** (Overseerr logo) | Aucun |
| `ic_select_audio` | `audiotrack` | Exact |
| `ic_select_chapter` | `bookmark` | Proche |
| `ic_select_quality` | `high_quality` | Exact |
| `ic_select_subtitle` | `subtitles` | Exact |
| `ic_settings` | `settings` | Exact |
| `ic_shuffle` | `shuffle` | Exact |
| `ic_sort` | `sort` | Exact |
| `ic_star` | `star` | Exact |
| `ic_subtitle_sync` | `sync` | Proche |
| `ic_subtitles` | `subtitles` | Exact |
| `ic_syncplay` | `groups` | Proche |
| `ic_time` | `schedule` | Exact |
| `ic_trailer` | `videocam` | Proche |
| `ic_tv` | `tv` | Exact |
| `ic_tv_guide` | `live_tv` | Exact |
| `ic_tv_play` | `smart_display` | Proche |
| `ic_tv_timer` | `timer` | Proche |
| `ic_up` | `keyboard_arrow_up` / `expand_less` | Exact |
| `ic_upload` | `upload` | Exact |
| `ic_user` | `person` | Exact |
| `ic_user_add` | `person_add` | Exact |
| `ic_users` | `group` | Exact |
| `ic_vegafox` | **Custom** (logo VegafoX) | Aucun |
| `ic_vegafox_fox` | **Custom** (renard VegafoX) | Aucun |
| `ic_watch` | `visibility` | Exact |
| `ic_zzz` | `bedtime` | Exact |
| `ic_abc` | `sort_by_alpha` | Exact |

---

## 5. Drawables a conserver / remplacer / supprimer

### 5.1 A CONSERVER (custom, aucun equivalent Material Symbols) — 8 fichiers

| Drawable | Raison |
|----------|--------|
| `ic_jellyfin` | Logo Jellyfin specifique |
| `ic_jellyseerr_jellyfish` | Logo Jellyseerr specifique |
| `ic_rt_fresh` | Logo Rotten Tomatoes (tomate fraiche) |
| `ic_rt_rotten` | Logo Rotten Tomatoes (tomate pourrie) |
| `ic_seer` | Logo Overseerr specifique |
| `ic_vegafox` | Logo VegafoX principal |
| `ic_vegafox_fox` | Icone renard VegafoX (PNG) |
| `qr_code.png` | QR code donation |
| `qr_jellyfin_docs` | QR code docs Jellyfin |

### 5.2 A REMPLACER par Material Symbols — 82 drawables actifs

Tous les drawables listes dans le tableau 4.2 avec correspondance "Exact" ou "Proche" peuvent etre remplaces par Material Symbols.

- **Correspondance exacte** : ~55 icones
- **Correspondance proche** : ~17 icones (verifier visuellement)
- **Custom a conserver** : 8 icones (voir 5.1)

### 5.3 A SUPPRIMER (morts) — 59 fichiers

- **16** drawables `ic_*.xml` morts (section 2.2)
- **23** drawables non-ic XML morts (section 2.3)
- **20** drawables tile morts (section 2.4)

### 5.4 Drawables saisonniers — a conserver

| Drawable | Saison | Actif |
|----------|--------|-------|
| `seasonal_cherry_blossom` | Printemps | Oui (PetalfallView) |
| `seasonal_bee` | Printemps | Oui (PetalfallView) |
| `seasonal_sun` | Ete | Oui (SummerView) |
| `seasonal_beach_ball` | Ete | Oui (SummerView) |
| `seasonal_beach_umbrella` | Ete | Oui (SummerView) |
| `seasonal_maple_leaf` | Automne | Oui (LeaffallView) |
| `seasonal_pumpkin` | Automne | Oui (LeaffallView) |
| `seasonal_ghost` | Halloween | Oui (HalloweenView) |
| `seasonal_jack_o_lantern` | Halloween | Oui (HalloweenView) |
| `seasonal_spider` | Halloween | Oui (HalloweenView) |
| `seasonal_candy` | Halloween | Oui (HalloweenView) |
| `seasonal_snowflake` | Hiver | Oui (SnowfallView) |
| `seasonal_snowman` | Hiver | Oui (SnowfallView) |

---

## 6. Resume et estimation effort

### Chiffres cles

| Categorie | Nombre |
|-----------|--------|
| Icones Material Icons Extended (Compose) | **1** (`Icons.AutoMirrored.Outlined.ArrowForward`) |
| Drawables XML ic_*.xml totaux | **106** |
| Drawables XML ic_*.xml actifs | **90** |
| Drawables XML ic_*.xml morts | **16** |
| Autres drawables XML morts | **43** (23 UI + 20 tiles) |
| **Total drawables morts a supprimer** | **59** |
| Assets PNG | **25** |
| Assets WebP | **15** |
| Drawables saisonniers (a conserver) | **13** |
| Drawables custom sans equivalent (a conserver) | **8** |
| Drawables remplacables par Material Symbols | **~82** |

### Dependances icones actuelles

| Dependance | Version | Module |
|------------|---------|--------|
| `androidx.compose.material:material-icons-extended` | 1.10.4 | app |
| `androidx.compose.material3:material3` | 1.4.0 | app |
| `androidx.tv:tv-material` | 1.0.1 | app |

> **Note** : `material-icons-extended` est une dependance lourde (~36 MB) pour **1 seule icone** utilisee. La migration vers Material Symbols permettrait de la supprimer.

### Estimation effort migration

| Tache | Fichiers | Effort |
|-------|----------|--------|
| Supprimer 59 drawables morts | 59 fichiers | Trivial |
| Remplacer 1 Icons.Extended par Symbols | 1 fichier | Trivial |
| Migrer 82 drawables XML vers Material Symbols | ~82 XML + ~50 .kt | Moyen |
| Supprimer dependance material-icons-extended | build.gradle.kts | Trivial |
| Verifier visuellement les ~17 correspondances "Proches" | 17 icones | Faible |
| **Total fichiers Kotlin a toucher** | **~50 fichiers** | |
| **Total fichiers XML a toucher** | **~82 fichiers** | |

### Strategie recommandee

1. **Phase 0** : Supprimer les 59 drawables morts (aucun risque)
2. **Phase 1** : Ajouter dependance `material-symbols-extended` + remplacer `Icons.AutoMirrored.Outlined.ArrowForward` + supprimer `material-icons-extended`
3. **Phase 2** : Migrer les drawables XML avec correspondance exacte (~55) par lots fonctionnels (playback, settings, navigation, etc.)
4. **Phase 3** : Migrer les drawables avec correspondance proche (~17) apres validation visuelle
5. **Phase 4** : Nettoyage final — supprimer les XML remplaces, verifier la regression
