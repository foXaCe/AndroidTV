# Phase 2-4 — Migration drawables XML vers Material Symbols Compose

> Date : 2026-03-11
> Objectif : Remplacer les drawables XML par des icones Material Symbols via VegafoXIcons.kt

---

## Architecture

### Fichier central : `VegafoXIcons.kt`

```
app/src/main/java/org/jellyfin/androidtv/ui/base/icons/VegafoXIcons.kt
```

Object singleton contenant ~80 proprietes `ImageVector` mappees vers `Icons.Default.*` (material-icons-extended 1.7.8).

Usage : `VegafoXIcons.Play`, `VegafoXIcons.Home`, `VegafoXIcons.Settings`, etc.

### Dependance ajoutee

```toml
# gradle/libs.versions.toml
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version = "1.7.8" }
```

Ajoutee au bundle `androidx-compose`. R8 tree-shake les icones non utilisees — impact APK quasi nul.

---

## Tableau de migration complet

### Drawables migres vers VegafoXIcons (63 supprimes)

| Drawable XML supprime | VegafoXIcons | Material Symbol |
|----------------------|-------------|-----------------|
| `ic_abc` | `Abc` | `SortByAlpha` |
| `ic_adjust` | `Tune` | `Tune` |
| `ic_arrow_forward` | `ArrowForward` | `ArrowForward` (AutoMirrored) |
| `ic_artist` | `Artist` | `Person` |
| `ic_aspect_ratio` | `AspectRatio` | `AspectRatio` |
| `ic_audio_sync` | `AudioSync` | `Sync` |
| `ic_calendar` | `Calendar` | `CalendarToday` |
| `ic_cast_list` | `CastList` | `Cast` |
| `ic_channel_bar` | `ChannelBar` | `LiveTv` |
| `ic_clapperboard` | `Clapperboard` | `MovieCreation` |
| `ic_delete` | `Delete` | `Delete` |
| `ic_error` | `Error` | `Error` |
| `ic_fast_forward` | `FastForward` | `FastForward` |
| `ic_filter` | `Filter` | `FilterList` |
| `ic_flask` | `Science` | `Science` |
| `ic_get_app` | `Download` | `Download` |
| `ic_grid` | `GridView` | `GridView` |
| `ic_guide` | `Guide` | `MenuBook` |
| `ic_heart_red` | `Favorite` (tint red) | `Favorite` |
| `ic_help` | `Help` | `Help` (AutoMirrored) |
| `ic_house_edit` | `HomeEdit` | `EditLocation` |
| `ic_lightbulb` | `Lightbulb` | `Lightbulb` |
| `ic_lock` | `Lock` | `Lock` |
| `ic_logout` | `Logout` | `Logout` (AutoMirrored) |
| `ic_loop` | `Loop` | `Loop` |
| `ic_masks` | `Genres` | `TheaterComedy` |
| `ic_microphone` | `Microphone` | `Mic` |
| `ic_more` | `MoreHoriz` | `MoreHoriz` |
| `ic_movie` | `Movie` | `Movie` |
| `ic_music_album` | `MusicLibrary` | `LibraryMusic` |
| `ic_next` | `SkipNext` | `SkipNext` |
| `ic_next_up` | `NextUp` | `Upcoming` |
| `ic_pause` | `Pause` | `Pause` |
| `ic_photo` | `Photo` | `Photo` |
| `ic_photos` | `PhotoLibrary` | `PhotoLibrary` |
| `ic_playback_speed` | `Speed` | `Speed` |
| `ic_previous` | `SkipPrevious` | `SkipPrevious` |
| `ic_record` | `Record` | `FiberManualRecord` |
| `ic_record_red` | `Record` (tint red) | `FiberManualRecord` |
| `ic_record_series` | `RecordSeries` | `FiberSmartRecord` |
| `ic_record_series_red` | `RecordSeries` (tint red) | `FiberSmartRecord` |
| `ic_refresh` | `Refresh` | `Refresh` |
| `ic_rewind` | `Rewind` | `FastRewind` |
| `ic_search` | `Search` | `Search` |
| `ic_select_audio` | `Audiotrack` | `Audiotrack` |
| `ic_select_chapter` | `Chapter` | `Bookmark` |
| `ic_select_quality` | `HighQuality` | `HighQuality` |
| `ic_select_subtitle` | `Subtitles` | `Subtitles` |
| `ic_settings` | `Settings` | `Settings` |
| `ic_sort` | `Sort` | `Sort` (AutoMirrored) |
| `ic_subtitle_sync` | `SubtitleSync` | `Sync` |
| `ic_subtitles` | `Subtitles` | `Subtitles` |
| `ic_syncplay` | `SyncPlay` | `Groups` |
| `ic_time` | `Schedule` | `Schedule` |
| `ic_trailer` | `Trailer` | `Videocam` |
| `ic_tv_guide` | `LiveTv` | `LiveTv` |
| `ic_tv_play` | `TvPlay` | `SmartDisplay` |
| `ic_tv_timer` | `TvTimer` | `Timer` |
| `ic_upload` | `Upload` | `Upload` |
| `ic_users` | `Group` | `Group` |
| `ic_watch` | `Visibility` | `Visibility` |
| `ic_zzz` | `Sleep` | `Bedtime` |

### Drawables conserves (29 fichiers)

| Drawable | Raison |
|----------|--------|
| **Custom / logos** | |
| `ic_vegafox` | Logo VegafoX (XML layouts + settings) |
| `ic_jellyfin` | Logo Jellyfin + notification smallIcon |
| `ic_jellyseerr_jellyfish` | Logo Jellyseerr |
| `ic_seer` | Logo Overseerr |
| `ic_rt_fresh` | Rotten Tomatoes |
| `ic_rt_rotten` | Rotten Tomatoes |
| **View-based code** | |
| `ic_play` | TextUnderButton, ItemRowView.setBackgroundResource |
| `ic_add` | TextUnderButton (ItemListFragment) |
| `ic_shuffle` | TextUnderButton + XML layout (clock_user_bug) |
| `ic_heart` | TextUnderButton (ItemListFragment) |
| `ic_mix` | TextUnderButton (ItemListFragment) |
| `ic_user` | TextUnderButton (ItemListFragment) |
| `ic_album` | NowPlayingView placeholder, ItemListFragment poster |
| `ic_check` | SettingsFragment.setImageResource |
| `ic_star` | RatingIcon.LocalDrawable |
| **Jellyseerr RequestsAdapter (View)** | |
| `ic_available` | Status icon |
| `ic_partially_available` | Status icon |
| `ic_declined` | Status icon |
| `ic_pending` | Status icon |
| `ic_indigo_spinner` | Loading spinner |
| **XML layouts** | |
| `ic_arrow_back` | fragment_user_login.xml |
| `ic_pencil` | fragment_server.xml |
| `ic_user_add` | fragment_server.xml |
| `ic_house` | fragment_server.xml, clock_user_bug.xml |
| `ic_down` | item_row.xml, genres_grid_browse.xml |
| `ic_up` | item_row.xml |
| `ic_folder` | genres_grid_browse.xml |
| `ic_control_select` | popup_expandable_text_view.xml |
| `ic_tv` | tile_land_tv.xml (LeanbackChannelWorker) |

---

## Refactoring des APIs

### Parametres `iconRes: Int` → `icon: ImageVector`

| Composable | Fichier |
|-----------|---------|
| `SidebarNavIcon` | MainToolbar.kt |
| `LibraryToolbarButton` | LibraryBrowseComponents.kt |
| `RecordingsNavButton` | RecordingsBrowseFragment.kt |
| `LiveTvNavButton` | LiveTvBrowseFragment.kt |
| `MusicNavButton` | MusicBrowseFragment.kt |
| `SecondaryButton` | PlayerOverlayScreen.kt |
| `FilterChip` | LibraryBrowseScreen.kt |

### MediaToastData refactore

`MediaToastData.icon` passe de `@DrawableRes Int` a `ImageVector`.
Callers (`VideoPlayerOverlay`, `rememberPlaybackManagerMediaToastEmitter`) passent maintenant `VegafoXIcons.Play`/`Pause` etc.

---

## Fichiers Kotlin modifies (~50 fichiers)

### LOT 1 — Navigation (13 fichiers)
MainToolbar, LeftSidebarNavigation, ExpandableLibrariesButton, StartupToolbar, LibraryBrowseComponents, RecordingsBrowseFragment, ScheduleBrowseFragment, LiveTvBrowseFragment, MusicBrowseFragment, SeriesRecordingsBrowseFragment, FolderBrowseScreen, GenresGridScreen, LibraryBrowseScreen

### LOT 2 — Playback (10 fichiers)
AudioNowPlayingScreen, VideoPlayerControls, PlayerOverlayScreen, PhotoPlayerControls, SkipOverlayView, VideoPlayerOverlay, rememberPlaybackManagerMediaToastEmitter, MediaToastData, MediaToastRegistry, MediaToasts

### LOT 3 — Actions (24 fichiers)
ItemCardBaseItemOverlay, DetailActions, SeasonDetailsContent, LiveTvDetailsContent, SeriesTimerDetailsContent, ItemDetailsComponents, JellyseerrRequestButtons, JellyseerrBrowseByScreen, SyncPlayDialog, AddToPlaylistDialog, CreatePlaylistDialog, ShuffleOptionsDialog, SearchScreen, SearchTextInput, SearchVoiceInput, ConnectHelpAlertFragment, ErrorState, ErrorBanner, ServerButtonView, ProfilePicture, Checkbox, RadioButton, BrowseMediaCard, SettingsAsyncActionListButton, BaseItemInfoRow, rating.kt

### LOT 4 — Settings (12 fichiers)
SettingsMainScreen, SettingsPlaybackScreen, SettingsHomeScreen, SettingsCustomizationScreen, SettingsAuthenticationScreen, SettingsAuthenticationServerUserScreen, SettingsAuthenticationServerScreen, SettingsJellyseerrScreen, SettingsPluginScreen, SettingsJellyseerrRowsScreen, SettingsAboutScreen, SettingsLibrariesScreen

### LOT 5 — LiveTV + reste (6 fichiers)
LiveTvGuideScreen, ChannelHeaderComposable, ProgramCellComposable, ProgramDetailDialog, ItemCardJellyseerrOverlay, WelcomeScreen

---

## Verification build

| Variante | Resultat | Taille APK |
|----------|----------|-----------|
| `assembleGithubDebug` | BUILD SUCCESSFUL | 55 MB |
| `assembleGithubRelease` | BUILD SUCCESSFUL | 33 MB |

Debug + Release installes sur AM9 Pro (192.168.1.152).

---

## Bilan

| Metrique | Avant | Apres |
|----------|-------|-------|
| Drawables `ic_*.xml` total | 90 actifs | 29 actifs |
| Drawables supprimes (phase 2-4) | — | **63** (dont 2 morts restants) |
| Drawables supprimes (phase 0) | — | **48** |
| **Total drawables supprimes** | — | **111** |
| Fichiers Kotlin modifies | — | **~50** |
| Dependance ajoutee | — | `material-icons-extended:1.7.8` |
| Dependance supprimee (phase 1) | `material-icons-extended` (inutilisee) | — |
| APK debug | 56 MB | 55 MB |
| APK release | 33 MB | 33 MB |
| Drawables custom conserves | 8 | 8 |
| Drawables saisonniers conserves | 13 | 13 |
| Drawables View-based conserves | — | 21 |

> **Note** : Le gain APK est minimal car R8 eliminait deja les vector XML inutilises.
> Le gain reel est en **maintenabilite** : toutes les icones Compose passent par `VegafoXIcons.*`,
> coherentes, centralisees, et independantes des fichiers XML.
