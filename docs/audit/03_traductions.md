# Audit 03 — Recensement et traduction des textes hardcodes

> **Mis à jour le 2026-03-08 — état post-travaux**
> - ✅ Résolu : 87 nouvelles entrées strings.xml ajoutées (EN + FR)
> - ✅ Résolu : 53 corrections vouvoiement → tutoiement dans FR
> - ✅ Résolu : Tous les layouts XML externalisés
> - ✅ Résolu : ~105 textes Kotlin externalisés dans 21 fichiers
> - ❌ En attente : 18 statuts combinés HD/4K dans MediaDetailsFragment.kt:569-618
> - ❌ En attente : 3 messages toast/dialog dans MediaDetailsFragment.kt:1614,1768,1810
> - ❌ En attente : "Cookie-based auth" dans SettingsJellyseerrScreen.kt:92
> - ❌ En attente : "SERIES" dans ItemCardJellyseerrOverlay.kt:59
> - ❌ En attente : ~5 autres textes mineurs Jellyseerr
> - strings.xml EN : 1315 entrées | strings.xml FR : 1313 entrées

**Date** : 2026-03-07
**Projet** : VegafoX Android TV (fork Jellyfin)
**Scope** : Tous les fichiers Kotlin, Java et XML du module `app`

---

## Sommaire

1. [Etat des lieux strings.xml](#1-etat-des-lieux-stringsxml)
2. [Textes hardcodes dans le code Kotlin](#2-textes-hardcodes-dans-le-code-kotlin)
3. [Textes hardcodes dans les layouts XML](#3-textes-hardcodes-dans-les-layouts-xml)
4. [Textes hardcodes dans d'autres fichiers](#4-textes-hardcodes-dans-dautres-fichiers)
5. [Tableau complet des traductions](#5-tableau-complet-des-traductions)
6. [Cas ambigus](#6-cas-ambigus)
7. [Statistiques](#7-statistiques)
8. [Etat des fichiers strings.xml](#8-etat-des-fichiers-stringsxml)

---

## 1. Etat des lieux strings.xml

### values/strings.xml
- **1318 lignes**, ~600 entrees `<string>`
- **Langue** : Anglais
- Contient deja des sections ajoutees manuellement (`<!-- Hardcoded strings - ... -->`)
- Toutes les entrees existantes ont des traductions FR correspondantes

### values-fr/strings.xml
- **1316 lignes**, miroir quasi complet du fichier anglais
- Traductions existantes de bonne qualite, tutoiement non encore applique
- Quelques entrees manquantes par rapport a values/strings.xml (sections recentes)

---

## 2. Textes hardcodes dans le code Kotlin

### 2.1 ItemDetailsFragment.kt (v2) — 15 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 770 | `title = "Next Up"` | `lbl_next_up` | Deja existant : "A suivre" |
| 788 | `title = "Season $it Episodes"` / `"Episodes"` | `lbl_season_episodes` / `lbl_episodes` | "Saison %d - Episodes" / existant |
| 809 | `title = "Albums"` | `lbl_albums` | Deja existant : "Albums" |
| 871 | `title = "More Like This"` | `lbl_more_like_this` | Deja existant : "Plus comme ceci" |
| 1165 | `listOf("Default")` | `lbl_default` | "Par defaut" |
| 1170 | `title = "Audio Track"` | `lbl_audio_track` | Deja existant : "Choisir la piste audio" |
| 1193 | `listOf("None")` / `listOf("Default")` | `lbl_none` / `lbl_default` | Existant / "Par defaut" |
| 1201 | `title = "Subtitle Track"` | `lbl_subtitle_track` | Deja existant : "Choisir la piste de sous-titres" |
| 1229 | `"Version ${i + 1}"` | `lbl_version_number` | "Version %d" |
| 1232 | `title = "Select Version"` | `lbl_select_version` | "Choisir la version" |
| 1258 | `"Genres"` | `lbl_genres` | Deja existant : "Genres" |
| 1261 | `"Director"` | `lbl_director` | "Realisateur" |
| 1264 | `"Writers"` | `lbl_writers` | "Scenaristes" |
| 1268 | `"Studio"` | `lbl_studio` | "Studio" |
| 1280 | `SectionHeader(title = "Seasons")` | `lbl_seasons` | Deja existant : "Saisons" |
| 1334 | `SectionHeader(title = "Cast & Crew")` | `lbl_cast_crew` | Deja existant : "Distribution et equipe" |
| 1806 | `"Movies (${personMovies.size})"` | `lbl_movies_count` | "Films (%d)" |
| 1817 | `"Series (${personSeries.size})"` | `lbl_series_count` | "Series (%d)" |

### 2.2 ItemDetailsViewModel.kt — 2 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 476 | `"Stereo"` | `lbl_stereo` | "Stereo" |
| 487 | `"TrueHD"` | (technique, garder tel quel) | — |

### 2.3 JellyseerrBrowseByFragment.kt — 10 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 96 | `"Popularity"` | `jellyseerr_sort_popularity` | "Popularite" |
| 97 | `"Rating"` | `jellyseerr_sort_rating` | "Note" |
| 98 | `"Release Date"` | `jellyseerr_sort_release_date` | "Date de sortie" |
| 99 | `"Title"` | `jellyseerr_sort_title` | "Titre" |
| 100 | `"Revenue"` | `jellyseerr_sort_revenue` | "Recettes" |
| 208 | `"Show All"` | `jellyseerr_filter_show_all` | "Tout afficher" |
| 209 | `"Available Only"` | `jellyseerr_filter_available_only` | "Disponibles uniquement" |
| 210 | `"Requested Only"` | `jellyseerr_filter_requested_only` | "Demandes uniquement" |
| 384 | `"Partially Available"` | `jellyseerr_status_partially_available` | "Partiellement disponible" |
| 418-420 | `"Network"` / `"Studio"` / `"Keyword"` | `jellyseerr_filter_network` / `_studio` / `_keyword` | "Reseau" / "Studio" / "Mot-cle" |

### 2.4 MediaDetailsFragment.kt — 5 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 996 | `"Status"` | `lbl_status_label` | "Statut" |
| 1020 | `"Seasons"` | `lbl_seasons` | Deja existant : "Saisons" |
| 1031 | `"Release Date"` | `lbl_release_date` | "Date de sortie" |
| 1039 | `"Revenue"` | `lbl_revenue` | "Recettes" |
| 1053 | `"Runtime"` | `lbl_runtime` | Deja existant : "Duree de lecture" |

### 2.5 DiscoverFragment.kt — 2 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 171 | `"movie" -> "Movie"` | `lbl_movie_type` | "Film" |
| 156/198 | `"$year •"` / `"$mediaType • $runtimeText"` | (format dynamique) | — |

### 2.6 DetailsOverviewRowPresenter.kt — 3 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 36 | `"movie" -> "Movie"` | `lbl_movie_type` | "Film" |
| 182 | `"Partially Available"` | `jellyseerr_status_partially_available` | "Partiellement disponible" |
| 32 | `"★ ${rating}"` | (format, garder ★) | — |

### 2.7 FullDetailsFragmentHelper.kt — 4 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 461 | `"✓ Default"` / `"Default"` | `lbl_default_checked` / `lbl_default` | "✓ Par defaut" / "Par defaut" |
| 480 | `"✓ None"` / `"None"` | `lbl_none_checked` / `lbl_none` | "✓ Aucun" / "Aucun" |
| 498 | `"✓ Default"` / `"Default"` | (meme que 461) | — |

### 2.8 GenresGridViewModel.kt — 2 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 44 | `title = "Genres"` | `lbl_genres` | Deja existant |
| 79 | `"Genres — $libraryName"` | `lbl_genres_in_library` | "Genres — %s" |

### 2.9 GenreBrowseFragment.kt — 3 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 104 | `"Popularity"` | `sort_popularity` | "Popularite" |
| 108 | `"Release Date"` | `sort_release_date` | "Date de sortie" |
| 392-393 | `"Movie"` / `"Series"` | `lbl_movie_type` / `lbl_series_type` | "Film" / "Serie" |

### 2.10 SelectServerFragment.kt — 2 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 194 | `"VegafoX version ${VERSION_NAME} ${BUILD_TYPE}"` | `app_version_display` | "VegafoX version %1$s %2$s" |
| 254 | `"${displayText} ⚠"` | (format dynamique avec icone) | — |

### 2.11 AudioDelayController.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 18 | `label = "No Delay"` | `audio_delay_none` | "Pas de decalage" |

### 2.12 SubtitleDelayAction.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 21 | `0L to "No Delay"` | `subtitle_delay_none` | "Pas de decalage" |

### 2.13 AdvancedRequestOptionsDialog.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 114 | `"Movie"` / `"TV Show"` | `lbl_movie_type` / `lbl_tv_show_type` | "Film" / "Serie TV" |
| 116 | `"$title ($quality $mediaType)"` | (format dynamique) | — |

### 2.14 SeasonSelectionDialog.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 71 | `"$showName (4K)"` / `"$showName (HD)"` | (format dynamique, 4K/HD techniques) | — |

### 2.15 RequestsAdapter.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 63 | `"Partially Available"` | `jellyseerr_status_partially_available` | "Partiellement disponible" |

### 2.16 JellyseerrViewModel.kt — 3 occurrences (user-visible)

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 333-334 | `"Permission Denied: Your Jellyfin account needs..."` | `jellyseerr_permission_denied_message` | "Acces refuse : ton compte Jellyfin n'a pas les permissions Jellyseerr necessaires.\n\nPour corriger :\n..." |
| 139/163/851 | `"Unknown"` (fallback titres) | `lbl_unknown` | Deja existant : "Inconnu" |

### 2.17 BaseItemExtensions.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 173 | `BaseItemKind.MOVIE -> "Movie"` | `lbl_movie_type` | "Film" |

### 2.18 LibraryBrowseViewModel.kt — 2 occurrences

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 353 | `"Movie"` | `lbl_movie_type` | "Film" |
| 354 | `"Series"` | `lbl_series_type` | "Serie" |

### 2.19 JellyseerrMediaBaseRowItem.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 44 | `"movie" -> "Movie"` | `lbl_movie_type` | "Film" |

### 2.20 MediaInfoCardView.kt — 1 occurrence

| Ligne | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|---------------|----------------------|---------------|
| 46 | `"Default"` | `lbl_default` | "Par defaut" |

---

## 3. Textes hardcodes dans les layouts XML

### 3.1 fragment_jellyseerr_settings.xml — 10 occurrences

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 18 | android:text | `"Jellyseerr Settings"` | `jellyseerr_settings` | Existant : "Jellyseerr" |
| 27 | android:text | `"Configure your Jellyseerr server connection"` | `jellyseerr_settings_description` | Existant |
| 44 | android:text | `"Enable Jellyseerr Integration"` | `jellyseerr_enabled` | Existant |
| 67 | android:text | `"Server URL"` | `jellyseerr_server_url` | Existant |
| 77 | android:hint | `"https://jellyseerr.example.com"` | `jellyseerr_server_url_description` | Existant |
| 90 | android:text | `"Connect with Current Jellyfin Account"` | `jellyseerr_connect_jellyfin` | Existant |
| 110 | android:contentDescription | `"Connection status icon"` | `cd_connection_status` | "Icone de statut de connexion" |
| 117 | android:text | `"Not tested"` | `jellyseerr_not_tested` | Existant |
| 135 | android:text | `"Test Connection"` | `jellyseerr_test_connection` | Existant |
| 144 | android:text | `"Save Settings"` | `btn_save` | Existant : "Enregistrer" |
| 197 | android:text | `"How to Connect:"` | `jellyseerr_setup_title` | Existant |
| 206 | android:text | `"1. Enter your Jellyseerr..."` | `jellyseerr_setup_info` | Existant |

### 3.2 fragment_jellyseerr_requests.xml — 7 occurrences

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 17 | android:text | `"My Requests"` | `jellyseerr_my_requests` | "Mes demandes" |
| 37 | android:text | `"Jellyseerr Not Connected"` | `jellyseerr_not_connected` | "Jellyseerr non connecte" |
| 45 | android:text | `"Go to Settings to configure your server"` | `jellyseerr_go_to_settings` | "Va dans les parametres pour configurer ton serveur" |
| 70 | android:text | `"Loading requests..."` | `jellyseerr_loading_requests` | "Chargement des demandes..." |
| 95 | android:text | `"No Requests Yet"` | `jellyseerr_no_requests` | "Aucune demande pour l'instant" |
| 105 | android:text | `"Submit requests from the Discover tab"` | `jellyseerr_submit_from_discover` | "Soumets des demandes depuis l'onglet Decouvrir" |
| 126 | android:text | `"Refresh"` | `btn_refresh` | "Actualiser" |

### 3.3 view_row_details.xml — 5 occurrences

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 247 | android:text | `"Genres"` | `lbl_genres` | Existant : "Genres" |
| 275 | android:text | `"Director"` | `lbl_director` | "Realisateur" |
| 303 | android:text | `"Writers"` | `lbl_writers` | "Scenaristes" |
| 331 | android:text | `"Studios"` | `lbl_studios` | "Studios" |
| 359 | android:text | `"Runs"` | `lbl_runs` | Existant : "Duree" |
| 387 | android:text | `"Ends"` | `lbl_ends` | Existant : "Fin" |

### 3.4 view_jellyseerr_details_row.xml — 7 occurrences

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 134 | android:text | `"Genres"` | `lbl_genres` | Existant |
| 161 | android:text | `"Director"` | `lbl_director` | "Realisateur" |
| 188 | android:text | `"Studio"` | `lbl_studio` | "Studio" |
| 215 | android:text | `"Release Date"` | `lbl_release_date` | "Date de sortie" |
| 242 | android:text | `"Runtime"` | `lbl_runtime` | Existant |
| 269 | android:text | `"Status"` | `lbl_status_label` | "Statut" |
| 296 | android:text | `"Availability"` | `lbl_availability` | "Disponibilite" |

### 3.5 item_jellyseerr_content.xml — 1 occurrence

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 35 | android:text | `"MOVIE"` | `lbl_movie_type_upper` | "FILM" |

### 3.6 channel_header.xml — 2 occurrences (placeholder design-time)

| Ligne | Attribut | Texte hardcode | Note |
|-------|----------|---------------|------|
| 11 | android:text | `"Small Text"` | Placeholder — remplacer par `tools:text` |
| 22 | android:text | `"Small Text"` | Placeholder — remplacer par `tools:text` |

### 3.7 friendly_date_button.xml — 2 occurrences (placeholder design-time)

| Ligne | Attribut | Texte hardcode | Note |
|-------|----------|---------------|------|
| 12 | android:text | `"Medium Text"` | Placeholder — remplacer par `tools:text` |
| 21 | android:text | `"Small Text"` | Placeholder — remplacer par `tools:text` |

### 3.8 overlay_tv_guide.xml — 1 occurrence (placeholder design-time)

| Ligne | Attribut | Texte hardcode | Note |
|-------|----------|---------------|------|
| 80 | android:text | `"Medium Text"` | Placeholder — remplacer par `tools:text` |

### 3.9 dialog_pin_entry.xml — chiffres et symboles (non traductible)

Chiffres 0-9, symboles ⌫ et ✓ — ne necessitent pas de traduction.

### 3.10 fragment_server_add.xml — 1 occurrence

| Ligne | Attribut | Texte hardcode | Cle R.string proposee | Traduction FR |
|-------|----------|---------------|----------------------|---------------|
| 73 | android:hint | `"192.168.1.100 or jellyfin.example.com"` | `server_address_hint` | "192.168.1.100 ou jellyfin.example.com" |

---

## 4. Textes hardcodes dans d'autres fichiers

### 4.1 tools:text dans les layouts (design-time uniquement)

Ces textes ne sont visibles qu'en preview Android Studio et ne necessitent **pas** de traduction. Environ 50 occurrences identifiees dans :
- `view_row_details.xml` (12 tools:text)
- `view_jellyseerr_details_row.xml` (9 tools:text)
- `fragment_audio_now_playing.xml` (4 tools:text)
- `fragment_server.xml` (2 tools:text)
- `item_jellyseerr_content.xml` (3 tools:text)
- `item_jellyseerr_request.xml` (4 tools:text)
- `horizontal_grid_browse.xml` (1 tools:text)
- `vlc_player_interface.xml` (3 tools:text)
- `fragment_select_server.xml` (1 tools:text)
- Autres (11 tools:text)

### 4.2 Composable labels d'animation (non user-visible)

Labels techniques pour `animateFloatAsState` et transitions Compose — ne necessitent pas de traduction :
- `"BackgroundTransition"`, `"UserCardFocusScale"`, `"ButtonScale"`, etc.

---

## 5. Tableau complet des traductions a ajouter

### 5.1 Nouvelles entrees strings.xml necessaires

| # | Cle R.string | Texte anglais | Traduction FR | Fichier(s) source |
|---|-------------|--------------|---------------|-------------------|
| 1 | `lbl_default` | Default | Par defaut | ItemDetailsFragment, FullDetailsFragmentHelper, MediaInfoCardView |
| 2 | `lbl_default_checked` | ✓ Default | ✓ Par defaut | FullDetailsFragmentHelper |
| 3 | `lbl_none_checked` | ✓ None | ✓ Aucun | FullDetailsFragmentHelper |
| 4 | `lbl_director` | Director | Realisateur | ItemDetailsFragment, view_row_details.xml, view_jellyseerr_details_row.xml |
| 5 | `lbl_writers` | Writers | Scenaristes | ItemDetailsFragment, view_row_details.xml |
| 6 | `lbl_studio` | Studio | Studio | ItemDetailsFragment, view_jellyseerr_details_row.xml |
| 7 | `lbl_studios` | Studios | Studios | view_row_details.xml |
| 8 | `lbl_release_date` | Release Date | Date de sortie | MediaDetailsFragment, view_jellyseerr_details_row.xml |
| 9 | `lbl_revenue` | Revenue | Recettes | MediaDetailsFragment |
| 10 | `lbl_status_label` | Status | Statut | MediaDetailsFragment, view_jellyseerr_details_row.xml |
| 11 | `lbl_availability` | Availability | Disponibilite | view_jellyseerr_details_row.xml |
| 12 | `lbl_movie_type` | Movie | Film | DiscoverFragment, DetailsOverviewRowPresenter, BaseItemExtensions, etc. |
| 13 | `lbl_movie_type_upper` | MOVIE | FILM | item_jellyseerr_content.xml |
| 14 | `lbl_tv_show_type` | TV Show | Serie TV | AdvancedRequestOptionsDialog |
| 15 | `lbl_series_type` | Series | Serie | GenreBrowseFragment, LibraryBrowseViewModel |
| 16 | `lbl_stereo` | Stereo | Stereo | ItemDetailsViewModel |
| 17 | `lbl_season_episodes` | Season %d Episodes | Saison %d - Episodes | ItemDetailsFragment |
| 18 | `lbl_version_number` | Version %d | Version %d | ItemDetailsFragment |
| 19 | `lbl_select_version` | Select Version | Choisir la version | ItemDetailsFragment |
| 20 | `lbl_movies_count` | Movies (%d) | Films (%d) | ItemDetailsFragment |
| 21 | `lbl_series_count` | Series (%d) | Series (%d) | ItemDetailsFragment |
| 22 | `lbl_genres_in_library` | Genres — %s | Genres — %s | GenresGridViewModel |
| 23 | `audio_delay_none` | No Delay | Pas de decalage | AudioDelayController |
| 24 | `subtitle_delay_none` | No Delay | Pas de decalage | SubtitleDelayAction |
| 25 | `jellyseerr_sort_popularity` | Popularity | Popularite | JellyseerrBrowseByFragment |
| 26 | `jellyseerr_sort_rating` | Rating | Note | JellyseerrBrowseByFragment |
| 27 | `jellyseerr_sort_release_date` | Release Date | Date de sortie | JellyseerrBrowseByFragment |
| 28 | `jellyseerr_sort_title` | Title | Titre | JellyseerrBrowseByFragment |
| 29 | `jellyseerr_sort_revenue` | Revenue | Recettes | JellyseerrBrowseByFragment |
| 30 | `jellyseerr_filter_show_all` | Show All | Tout afficher | JellyseerrBrowseByFragment |
| 31 | `jellyseerr_filter_available_only` | Available Only | Disponibles uniquement | JellyseerrBrowseByFragment |
| 32 | `jellyseerr_filter_requested_only` | Requested Only | Demandes uniquement | JellyseerrBrowseByFragment |
| 33 | `jellyseerr_status_partially_available` | Partially Available | Partiellement disponible | JellyseerrBrowseByFragment, DetailsOverviewRowPresenter, RequestsAdapter |
| 34 | `jellyseerr_filter_network` | Network | Reseau | JellyseerrBrowseByFragment |
| 35 | `jellyseerr_filter_studio` | Studio | Studio | JellyseerrBrowseByFragment |
| 36 | `jellyseerr_filter_keyword` | Keyword | Mot-cle | JellyseerrBrowseByFragment |
| 37 | `jellyseerr_my_requests` | My Requests | Mes demandes | fragment_jellyseerr_requests.xml |
| 38 | `jellyseerr_not_connected` | Jellyseerr Not Connected | Jellyseerr non connecte | fragment_jellyseerr_requests.xml |
| 39 | `jellyseerr_go_to_settings` | Go to Settings to configure your server | Va dans les parametres pour configurer ton serveur | fragment_jellyseerr_requests.xml |
| 40 | `jellyseerr_loading_requests` | Loading requests... | Chargement des demandes... | fragment_jellyseerr_requests.xml |
| 41 | `jellyseerr_no_requests` | No Requests Yet | Aucune demande pour l'instant | fragment_jellyseerr_requests.xml |
| 42 | `jellyseerr_submit_from_discover` | Submit requests from the Discover tab | Soumets des demandes depuis l'onglet Decouvrir | fragment_jellyseerr_requests.xml |
| 43 | `btn_refresh` | Refresh | Actualiser | fragment_jellyseerr_requests.xml |
| 44 | `cd_connection_status` | Connection status icon | Icone de statut de connexion | fragment_jellyseerr_settings.xml |
| 45 | `server_address_hint` | 192.168.1.100 or jellyfin.example.com | 192.168.1.100 ou jellyfin.example.com | fragment_server_add.xml |
| 46 | `app_version_display` | VegafoX version %1$s %2$s | VegafoX version %1$s %2$s | SelectServerFragment |
| 47 | `sort_popularity` | Popularity | Popularite | GenreBrowseFragment |
| 48 | `sort_release_date` | Release Date | Date de sortie | GenreBrowseFragment |
| 49 | `jellyseerr_permission_denied_message` | Permission Denied: Your Jellyfin account needs Jellyseerr permissions... | Acces refuse : ton compte Jellyfin n'a pas les permissions Jellyseerr necessaires... | JellyseerrViewModel |
| 50 | `jellyseerr_request` | Request | Demander | MediaDetailsFragment |
| 51 | `jellyseerr_request_more` | Request More | Demander plus | MediaDetailsFragment |
| 52 | `jellyseerr_status_blacklisted` | Blacklisted | Sur liste noire | MediaDetailsFragment, DetailsOverviewRowPresenter |
| 53 | `lbl_budget` | Budget | Budget | MediaDetailsFragment |
| 54 | `lbl_networks` | Networks | Diffuseurs | MediaDetailsFragment |
| 55 | `jellyseerr_quality_pending` | %1$s (Pending) | %1$s (En attente) | QualitySelectionDialog |
| 56 | `jellyseerr_quality_processing` | %1$s (Processing) | %1$s (En cours) | QualitySelectionDialog |
| 57 | `jellyseerr_quality_request_more` | Request More %1$s | Demander plus %1$s | QualitySelectionDialog |
| 58 | `jellyseerr_quality_available` | %1$s (Available) | %1$s (Disponible) | QualitySelectionDialog |
| 59 | `jellyseerr_quality_blacklisted` | %1$s (Blacklisted) | %1$s (Liste noire) | QualitySelectionDialog |
| 60 | `jellyseerr_quality_request` | Request %1$s | Demander %1$s | QualitySelectionDialog |
| 61 | `jellyseerr_select_all_seasons` | Select All Seasons | Selectionner toutes les saisons | SeasonSelectionDialog |
| 62 | `jellyseerr_select_all_available` | Select All Available | Selectionner toutes les disponibles | SeasonSelectionDialog |
| 63 | `jellyseerr_season_number` | Season %1$d | Saison %1$d | SeasonSelectionDialog |
| 64 | `jellyseerr_season_already_requested` | Season %1$d (Already Requested) | Saison %1$d (Deja demandee) | SeasonSelectionDialog |
| 65 | `jellyseerr_quality_profile` | Quality Profile | Profil de qualite | AdvancedRequestOptionsDialog |
| 66 | `jellyseerr_server_default` | Server Default | Par defaut du serveur | AdvancedRequestOptionsDialog |
| 67 | `jellyseerr_server_default_path` | Server Default (%1$s) | Par defaut du serveur (%1$s) | AdvancedRequestOptionsDialog |
| 68 | `jellyseerr_root_folder` | Root Folder | Dossier racine | AdvancedRequestOptionsDialog |
| 69 | `subtitle_delay_reset` | Subtitle delay reset | Delai des sous-titres reinitialise | SubtitleDelayAction |
| 70 | `subtitle_delay_value` | Subtitle delay: %1$s | Delai sous-titres : %1$s | SubtitleDelayAction |
| 71 | `lbl_first_air_date` | First Air Date | Premiere diffusion | MediaDetailsFragment |
| 72 | `lbl_last_air_date` | Last Air Date | Derniere diffusion | MediaDetailsFragment |
| 73 | `jellyseerr_similar_series` | Similar Series | Series similaires | MediaDetailsFragment |
| 74 | `jellyseerr_similar_titles` | Similar Titles | Titres similaires | MediaDetailsFragment |
| 75 | `jellyseerr_no_server_configured` | No Radarr/Sonarr server configured for %1$s in Jellyseerr | Aucun serveur Radarr/Sonarr configure pour les %1$s dans Jellyseerr | MediaDetailsFragment |
| 76 | `lbl_play_trailer` | Play Trailer | Bande-annonce | MediaDetailsFragment |
| 77 | `lbl_shuffle` | Shuffle | Lecture aleatoire | LeftSidebarNavigation, MainToolbar |
| 78 | `lbl_user_fallback` | User | Utilisateur | LeftSidebarNavigation |

### 5.2 Entrees existantes a utiliser (deja dans strings.xml)

Ces textes hardcodes doivent etre remplaces par des references aux cles existantes :

| Texte hardcode | Cle existante | Traduction FR existante |
|---------------|--------------|------------------------|
| `"Next Up"` | `lbl_next_up` | A suivre |
| `"Albums"` | `lbl_albums` | Albums |
| `"More Like This"` | `lbl_more_like_this` | Plus comme ceci |
| `"Audio Track"` | `lbl_audio_track` | Choisir la piste audio |
| `"Subtitle Track"` | `lbl_subtitle_track` | Choisir la piste de sous-titres |
| `"Genres"` | `lbl_genres` | Genres |
| `"Seasons"` | `lbl_seasons` | Saisons |
| `"Cast & Crew"` | `lbl_cast_crew` | Distribution et equipe |
| `"None"` | `lbl_none` | Aucun |
| `"Episodes"` | `lbl_episodes` | Episodes |
| `"Runtime"` | `lbl_runtime` | Duree de lecture |
| `"Unknown"` | `lbl_unknown` / `lbl_bracket_unknown` | Inconnu |
| `"Runs"` | `lbl_runs` | Duree |
| `"Ends"` | `lbl_ends` | Fin |

---

## 6. Cas ambigus

| # | Texte | Probleme | Recommandation |
|---|-------|----------|----------------|
| 1 | `"Default"` dans pistes audio/sous-titres | "Par defaut" = choix du serveur vs "Aucun" = desactive | Utiliser `lbl_default` = "Par defaut" |
| 2 | `"Movie"` utilise dans ~8 fichiers differents | Contexte varie : type de contenu, label, filtre | Creer `lbl_movie_type` = "Film" unifie |
| 3 | `"No Delay"` audio vs sous-titres | Meme concept mais fichiers differents | Deux cles distinctes pour flexibilite future |
| 4 | `"Popularity"` dans GenreBrowseFragment vs JellyseerrBrowseByFragment | Meme mot mais contextes differents | Pourrait etre une seule cle `sort_popularity` |
| 5 | `"Studios"` (pluriel) vs `"Studio"` (singulier) | Coexistent dans des layouts differents | Deux cles distinctes : `lbl_studios` et `lbl_studio` |
| 6 | `"Stereo"` et `"TrueHD"` | Termes techniques audio | `"Stereo"` est universel, garder tel quel |
| 7 | `"4K"` / `"HD"` prefixes dans MediaDetailsFragment | Utilises comme prefixes de statut : "4K Declined" | Garder comme prefixes techniques non traduits |
| 8 | Placeholders design-time (`"Small Text"`, `"Medium Text"`) | Visibles en production car `android:text` au lieu de `tools:text` | **Migrer vers `tools:text`** pour les supprimer de la production |
| 9 | `"Permission Denied..."` long message | Message multi-paragraphe avec instructions techniques | Traduire mais garder les termes techniques (Jellyfin, Jellyseerr, admin) |
| 10 | Tutoiement vs vouvoiement | Le FR existant utilise le vouvoiement dans certains endroits | Harmoniser vers le **tutoiement** selon la consigne |

---

## 7. Statistiques

| Categorie | Nombre |
|-----------|--------|
| **Entrees EN strings.xml (avant audit)** | ~1220 |
| **Entrees EN strings.xml (apres audit)** | 1307 |
| **Nouvelles entrees ajoutees** | ~87 |
| **Entrees FR strings.xml** | 1305 (= EN - 2 noms d'app non traduits) |
| **Textes hardcodes trouves dans Kotlin** | ~130 |
| **Textes hardcodes remplaces dans Kotlin** | ~105 |
| **Textes hardcodes dans XML** | Tous deja externalises (`@string/xxx`) |
| **Fichiers Kotlin modifies** | 21 |
| **Corrections vouvoiement -> tutoiement (values-fr)** | 53 |
| **Textes conserves intentionnellement** | ~25 (termes techniques, noms propres, identifiants API) |

---

## 8. Etat des fichiers strings.xml — TERMINE

### values/strings.xml
- **Statut** : COMPLETE — 68 nouvelles entrees ajoutees dans des sections organisees
- Sections ajoutees : Content descriptions, Media metadata, Audio/Subtitle delay, Jellyseerr sort/filter, Jellyseerr requests UI, Genre sort, Server, Quality selection dialog, Season selection dialog, Advanced request options, Media details labels, Toolbar, Permission error

### values-fr/strings.xml
- **Statut** : COMPLETE — Miroir exact du fichier anglais avec traductions FR
- **Tutoiement** : HARMONISE — 53 corrections vous->tu appliquees dans tout le fichier
- Aucun doublon, aucune cle manquante (verifie par diff)

### Fichiers Kotlin modifies

| Fichier | Remplacements |
|---------|--------------|
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | 18 remplacements |
| `ui/jellyseerr/MediaDetailsFragment.kt` | 9 remplacements (Request, Budget, Networks, dates, Similar, Play Trailer, server error) |
| `ui/jellyseerr/DetailsOverviewRowPresenter.kt` | 4 remplacements (Movie/TV Series, Blacklisted, date formatting localise) |
| `ui/jellyseerr/DiscoverFragment.kt` | 2 remplacements (Movie/TV Series) |
| `ui/jellyseerr/RequestsAdapter.kt` | 1 remplacement (Partially Available) |
| `ui/jellyseerr/AdvancedRequestOptionsDialog.kt` | 5 remplacements (Movie/TV Show, Quality Profile, Root Folder, Server Default x2) |
| `ui/jellyseerr/QualitySelectionDialog.kt` | 6 remplacements (labels qualite HD/4K) |
| `ui/jellyseerr/SeasonSelectionDialog.kt` | 2 remplacements (Select All, Season labels) |
| `ui/itemdetail/FullDetailsFragmentHelper.kt` | 4 remplacements (Default/None checked) |
| `ui/startup/fragment/SelectServerFragment.kt` | 1 remplacement (app version display) |
| `ui/itemhandling/JellyseerrMediaBaseRowItem.kt` | 2 remplacements (Movie/TV Series) |
| `util/sdk/BaseItemExtensions.kt` | 2 remplacements (Movie/TV Series) |
| `ui/playback/overlay/action/SubtitleDelayAction.kt` | 3 remplacements (No Delay, delay message, delay reset) |
| `ui/shared/toolbar/LeftSidebarNavigation.kt` | 2 remplacements (User fallback, Shuffle) |
| `ui/shared/toolbar/MainToolbar.kt` | 1 remplacement (Shuffle) |
| `ui/jellyseerr/JellyseerrBrowseByFragment.kt` | 18 remplacements (sort options, filter labels, status texts) |
| `ui/syncplay/SyncPlayDialog.kt` | 5 remplacements (messages d'erreur create/join/leave) |
| `ui/settings/screen/screensaver/SettingsScreensaverScreen.kt` | 1 remplacement (Off) |
| `ui/settings/screen/about/SettingsAboutScreen.kt` | 1 remplacement (app version heading) |

### Textes conserves intentionnellement (non externalises)

| Texte | Raison |
|-------|--------|
| `"4K"`, `"HD"` | Termes techniques universels |
| `"+100ms"`, `"+1.0s"`, etc. | Notations techniques de delai |
| `"TrueHD"`, `"DTS"`, codecs | Noms techniques audio |
| `"Jellyseerr"`, `"Seerr"` | Noms propres de produit |
| `"Tmdb"`, `"Tvdb"`, `"Imdb"` | Cles d'API internes |
| `"movie"`, `"tv"` | Identifiants internes SDK (pas UI) |
| `"..."` | Indicateur visuel d'animation |
| `"✓"`, `"✗"` | Symboles visuels (checkbox, statut) |
| Timber.d/w/e messages | Logs de debug, non user-visible |
| `"Stereo"` | Terme audio universel |
| Labels ViewModel/companion object sans Context | Architecture non compatible avec getString() |
