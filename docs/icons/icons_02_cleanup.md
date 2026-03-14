# Phase 0 — Suppression des drawables morts

> Date : 2026-03-11
> Objectif : Supprimer les drawables XML sans aucune reference dans le code

---

## Resultats

| Phase | Prevus | Supprimes | Non supprimes | Inexistants |
|-------|--------|-----------|---------------|-------------|
| 0A — ic_*.xml morts | 16 | 16 | 0 | 0 |
| 0B — autres XML morts (audit) | 23 | 12 | 11 | 0 |
| 0B — autres XML morts (user) | 23 | 3 (deja comptes) | 2 | 18 |
| 0C — tiles morts | 20 | 20 | 0 | 0 |
| **TOTAL** | **59 (audit)** | **48** | **11** | **0** |

---

## Phase 0A — 16 ic_*.xml supprimes

Tous confirmes 0 reference dans app/src/ :

| Fichier | Statut |
|---------|--------|
| `ic_4k.xml` | Supprime |
| `ic_camera.xml` | Supprime |
| `ic_decrease.xml` | Supprime |
| `ic_increase.xml` | Supprime |
| `ic_indigo_spinner_animated.xml` | Supprime |
| `ic_jellyseerr_logo.xml` | Supprime |
| `ic_jump_letter.xml` | Supprime |
| `ic_key.xml` | Supprime |
| `ic_movie_badge.xml` | Supprime |
| `ic_previous_episode.xml` | Supprime |
| `ic_resume.xml` | Supprime |
| `ic_series_badge.xml` | Supprime |
| `ic_switch_users.xml` | Supprime |
| `ic_trash.xml` | Supprime |
| `ic_unwatch.xml` | Supprime |
| `ic_vegafox_white.xml` | Supprime |

## Phase 0B — 12 autres XML supprimes

Confirmes 0 reference dans app/src/ :

| Fichier | Statut |
|---------|--------|
| `audio_now_playing_album_background.xml` | Supprime |
| `block_text_bg.xml` | Supprime |
| `button_bar_back.xml` | Supprime |
| `chevron_right.xml` | Supprime |
| `circle_accent.xml` | Supprime |
| `circle_background.xml` | Supprime |
| `default_genre_backdrop.xml` | Supprime |
| `ica_play_pause.xml` | Supprime |
| `layer_background.xml` | Supprime |
| `shape_card_circle.xml` | Supprime |
| `shape_card_image_background.xml` | Supprime |
| `subtitle_background.xml` | Supprime |

## Phase 0B — 11 fichiers NON supprimes (references actives)

| Fichier | Reference trouvee |
|---------|------------------|
| `button_icon_back.xml` | `res/values/styles.xml` (Style.DS.Button.Icon) |
| `button_icon_ripple.xml` | `res/values/styles.xml` (Style.DS.Button.Icon) |
| `button_icon_tint_animated.xml` | `res/values/styles.xml` (Style.DS.Button.Icon.Animated) |
| `input_default_back.xml` | `res/values/styles.xml` (Style.DS.Input) |
| `input_default_ripple.xml` | `res/values/styles.xml` (Style.DS.Input) |
| `input_default_text.xml` | `res/values/styles.xml` (Style.DS.Input) |
| `popup_menu_back.xml` | `res/values/styles.xml` (Style.DS.PopupMenu) |
| `progress_bar.xml` | `res/values/styles.xml` (player_progress) |
| `dark_green_gradient.xml` | `ProgramCellComposable.kt` (live TV) |
| `green_gradient.xml` | `ProgramCellComposable.kt` (live TV) |
| `red_gradient.xml` | `res/values/colors.xml` |

> **Note** : L'audit (section 2.3) listait 23 fichiers comme morts, mais 11 sont en fait references dans `styles.xml` ou du code Kotlin actif. L'audit initial etait incorrect pour ces 11 fichiers.

## Phase 0B — 18 fichiers de la liste utilisateur inexistants

Les fichiers suivants n'existent plus dans le repo (deja supprimes precedemment) :

`card_background_focused`, `card_background_unfocused`, `card_view_background`, `default_background_gradient`, `dimmer`, `drawable_item_tv_default_background`, `focused_border`, `jellyfin_gradient`, `logo_gradient_start`, `rounded_rectangle`, `rounded_rectangle_focused`, `search_edit_text_bg`, `settings_item_background`, `settings_item_background_focused`, `shimmer_placeholder`, `side_panel_background`, `tab_background`, `text_item_background`

## Phase 0C — 20 tiles supprimes

Tous confirmes 0 reference dans le code Kotlin/Java :

| Fichier | Statut |
|---------|--------|
| `tile_album_artists.xml` | Supprime |
| `tile_artists.xml` | Supprime |
| `tile_audio.xml` | Supprime |
| `tile_chapter.xml` | Supprime |
| `tile_genres.xml` | Supprime |
| `tile_land_folder.xml` | Supprime |
| `tile_land_photo.xml` | Supprime |
| `tile_land_series_timer.xml` | Supprime |
| `tile_letters.xml` | Supprime |
| `tile_port_folder.xml` | Supprime |
| `tile_port_grid.xml` | Supprime |
| `tile_port_guide.xml` | Supprime |
| `tile_port_person.xml` | Supprime |
| `tile_port_record.xml` | Supprime |
| `tile_port_series_timer.xml` | Supprime |
| `tile_port_time.xml` | Supprime |
| `tile_port_tv.xml` | Supprime |
| `tile_port_video.xml` | Supprime |
| `tile_suggestions.xml` | Supprime |
| `tile_tv.xml` | Supprime |

> **Note** : `tile_land_tv.xml` est ACTIF (LeanbackChannelWorker, MediaContentProvider) — non supprime.

---

## Correction build : dependance material-icons-extended

La dependance `androidx.compose.material:material-icons-extended:1.10.4` (ajoutee dans le working tree mais non commitee) cassait le build car la version 1.10.4 n'existe pas sur Maven. Elle a ete retiree de `app/build.gradle.kts` et `gradle/libs.versions.toml`.

---

## Verification build

| Variante | Resultat |
|----------|----------|
| `assembleGithubDebug` | BUILD SUCCESSFUL |
| `assembleGithubRelease` | BUILD SUCCESSFUL |

---

## Gain

| Metrique | Valeur |
|----------|--------|
| Fichiers supprimes | **48** |
| Lignes XML supprimees | **~806** |
| Fichiers non supprimes (actifs) | **11** |
| Fichiers inexistants (deja supprimes) | **18** |

---

## Methode de verification

Chaque fichier a ete verifie avec `grep -rn "nom_du_drawable" app/src/` avant suppression. Seuls les fichiers avec 0 reference dans le code (.kt, .java, .xml actifs) ont ete supprimes.
