# Audit complet — Ecran de detail media

**Date** : 2026-03-11
**Build** : v1.6.2 debug
**Appareil** : Ugoos AM9 Pro (192.168.1.152:5555), 1920x1080
**Contexte** : Cartographie avant refonte visuelle

---

## 1. Composants identifies

### Fragments (points d'entree)

| Fichier | Role | Utilise par |
|---------|------|-------------|
| `ui/itemdetail/compose/ItemDetailComposeFragment.kt` | Fragment principal Compose-first, utilise pour la navigation standard | `Destinations.itemDetails()` |
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | Fragment legacy avec backdrop ImageView + ComposeView superpose | `Destinations.itemDetailsV2()` |

**Navigation active** : `ItemDetailComposeFragment` est le point d'entree par defaut (`Destinations.itemDetails()`).

### ViewModel

| Fichier | Role |
|---------|------|
| `ui/itemdetail/v2/ItemDetailsViewModel.kt` | Chargement item, seasons, episodes, tracks, similar, cast, badges, Live TV |

### Ecrans Compose (type-specifiques)

| Fichier | Types geres |
|---------|-------------|
| `ui/itemdetail/compose/ItemDetailScreen.kt` | Dispatcher principal — route vers le bon content selon `item.type` |
| `ui/itemdetail/v2/content/MovieDetailsContent.kt` | MOVIE, EPISODE, VIDEO, RECORDING, TRAILER, MUSIC_VIDEO, BOX_SET |
| `ui/itemdetail/v2/content/SeriesDetailsContent.kt` | SERIES |
| `ui/itemdetail/v2/content/SeasonDetailsContent.kt` | SEASON |
| `ui/itemdetail/v2/content/PersonDetailsContent.kt` | PERSON |
| `ui/itemdetail/v2/content/MusicDetailsContent.kt` | MUSIC_ALBUM, MUSIC_ARTIST, PLAYLIST |
| `ui/itemdetail/v2/content/LiveTvDetailsContent.kt` | Live TV programs |
| `ui/itemdetail/v2/content/SeriesTimerDetailsContent.kt` | Live TV series timers |

### Composants partages

| Fichier | Contenu |
|---------|---------|
| `ui/itemdetail/v2/ItemDetailsComponents.kt` | DetailActionButton, MediaBadgeChip, CastCard, SeasonCard, EpisodeCard, SeasonEpisodeItem, SimilarItemCard, LandscapeItemCard, TrackItemCard, PosterImage, DetailBackdrop, TrackSelectorDialog, TrackActionDialog |
| `ui/itemdetail/v2/shared/DetailActions.kt` | DetailActionButtonsRow — row horizontale de tous les boutons d'action |
| `ui/itemdetail/v2/shared/DetailSections.kt` | DetailMetadataSection, DetailSeasonsSection, DetailEpisodesHorizontalSection, DetailCastSection, DetailSectionWithCards, DetailCollectionItemsGrid |
| `ui/itemdetail/v2/shared/DetailUtils.kt` | getBackdropUrl, getPosterUrl, getLogoUrl, getEpisodeThumbnailUrl, formatDuration |

---

## 2. Inventaire du contenu affiche

### Film (MovieDetailsContent) — Screenshot `detail_film.png`

| Element | Affiche | Detail |
|---------|---------|--------|
| Backdrop | OUI | Image plein ecran derriere le contenu, alpha 0.8, blur configurable, gradient vertical (transparent → fond) |
| Logo | OUI | Image logo si disponible (ex: AVATAR), sinon titre texte headlineLargeBold |
| Titre | OUI | headlineLargeBold, max 2 lignes, overflow Ellipsis |
| Info row | OUI | Annee (2009) • Duree (2h 42m) • "Se termine a HH:MM" • Rating officiel badge (FR-TP) |
| Media badges | OUI | 4K, HDR10, HEVC, MKV, 5.1, AC3 — fond textSecondary, texte noir, bold |
| Note communaute | OUI | ★ 7,6 avec label "Note de la communaute" |
| Rotten Tomatoes | OUI | 🍅 81% via InfoRowMultipleRatings |
| Tagline | OUI | Italique, guillemets ("Entrez dans le monde de Pandora.") |
| Synopsis | OUI | bodyMedium, max 4 lignes, lineHeight 24sp |
| Poster | OUI | 165×248dp, coin arrondi, cote droit |
| Boutons action | OUI | Row horizontale centree (voir section 3) |
| Metadata | OUI | Genres, Realisateur, Scenaristes, Studio — dans MetadataGroup (fond outlineVariant, separateurs) |
| Cast | OUI | LazyRow horizontale, photos circulaires 90dp, nom + role |
| Similaires | OUI | "Plus comme ceci" — LazyRow de SimilarItemCard (poster 140×200dp) |

### Episode (MovieDetailsContent, isEpisode=true) — Screenshot `detail_episode.png`

| Element | Affiche | Detail |
|---------|---------|--------|
| Nom serie | OUI | "Monarch : L'Heritage des monstres" en titleMedium textSecondary |
| Badge S/E | OUI | "S1 E7" dans badge fond outlineVariant |
| Titre episode | OUI | "MONARCH LEGACY OF MONSTERS" (logo image) |
| Info row | OUI | 2023 • 47m • Se termine a 5:17 PM |
| Media badges | OUI | 4K, HDR, HEVC, MKV, 5.1, EAC |
| Note communaute | OUI | ★ 5,9 |
| Synopsis | OUI | Description de l'episode |
| Poster | OUI | Image thumbnail episode (paysage pour episodes) |
| Boutons action | OUI | Reprendre (20m), Recommencer, Audio, Sous-titres, Non vu, Favori, Liste de, Aller vers les, Supprimer |
| Realisateur | OUI | "Hiromi Kamata" dans MetadataGroup |
| Episodes saison | OUI | "Saison 1 - Episodes" — LazyRow EpisodeCard avec thumbnail, numero, titre |
| Cast | OUI | "Distribution et equipe" — photos circulaires |

### Serie (SeriesDetailsContent)

| Element | Affiche | Detail code |
|---------|---------|-------------|
| Logo / Titre | OUI | Logo si dispo, sinon headlineLargeBold |
| Info row | OUI | Annee, status serie, genres |
| Ratings | OUI | Communaute + externes |
| Tagline | OUI | Italique |
| Synopsis | OUI | Max 4 lignes |
| Poster | OUI | 165×248dp |
| Boutons action | OUI | Play, Restart, Shuffle, Watched, Favorite, Playlist, Delete |
| Metadata | OUI | Genres, realisateur, scenaristes, studio |
| Next Up | OUI | Section avec LandscapeItemCard |
| Saisons | OUI | LazyRow de SeasonCard (170×255dp, poster portrait) |
| Cast | OUI | Photos circulaires |
| Similaires | OUI | "Plus comme ceci" |

### Saison (SeasonDetailsContent) — Screenshot `detail_episode_bottom.png`

| Element | Affiche | Detail code |
|---------|---------|-------------|
| Poster saison | OUI | 220dp large, poster pleine hauteur |
| Nom serie | OUI | headlineMedium |
| Nom saison | OUI | displayBold, 55sp lineHeight |
| Nombre episodes | OUI | "X episodes" |
| Boutons action | OUI | Play, Vu/Non vu, Favori |
| Episodes | OUI | Liste verticale SeasonEpisodeItem (thumbnail 240×135dp + synopsis 2 lignes) |

---

## 3. Inventaire des boutons d'action

### Boutons communs (DetailActionButtonsRow)

| Libelle | Icone | Action | Condition |
|---------|-------|--------|-----------|
| Reprendre | VegafoXIcons.Play | Resume a la position sauvegardee (- preroll) | `item.canResume == true` |
| Lecture / Recommencer | VegafoXIcons.Play / Loop | Play depuis le debut | `canPlay` (MOVIE, EPISODE, VIDEO, etc.) |
| Lecture aleatoire | VegafoXIcons.Shuffle | Shuffle all | `isFolder || MUSIC_ARTIST` (sauf BOX_SET) |
| Mix instantane | VegafoXIcons.Mix | playInstantMix | `MUSIC_ARTIST` uniquement |
| Version | VegafoXIcons.Guide | Dialog selection version | `mediaSources.size > 1` |
| Audio | VegafoXIcons.Audiotrack | Dialog selection piste audio | `audioStreams.size > 1` |
| Sous-titres | VegafoXIcons.Subtitles | Dialog selection sous-titres | `subtitleStreams.isNotEmpty()` |
| Bandes-annonces | VegafoXIcons.Trailer | Play trailer (YouTube WebView ou local) | `hasPlayableTrailers` |
| Vu / Non vu | VegafoXIcons.Check | Toggle watched (actif = bleu info) | `userData != null && type != PERSON && type != MUSIC_ARTIST` |
| Favori | VegafoXIcons.Favorite | Toggle favorite (actif = rouge error) | `userData != null` |
| Liste de | VegafoXIcons.Add | Dialog ajout playlist | `userData != null && type != PERSON` |
| Aller vers les | VegafoXIcons.Tv | Navigate to series detail | `EPISODE && seriesId != null` |
| Supprimer | VegafoXIcons.Delete | Confirmation dialog puis delete | `canDelete == true` |

### Boutons saison (SeasonDetailsContent)

| Libelle | Action | Condition |
|---------|--------|-----------|
| Lecture | Play premier episode non vu | `episodes.isNotEmpty()` |
| Vu / Non vu | Toggle watched saison | Toujours |
| Favori | Toggle favorite saison | Toujours |

### Boutons Live TV (LiveTvDetailsContent)

| Libelle | Action | Condition |
|---------|--------|-----------|
| Ecouter la chaine | Play live channel | Toujours |
| Enregistrer | Toggle recording (actif = rouge) | `programInfo != null` |
| Enregistrer serie | Toggle series recording (actif = rouge) | `programInfo.isSeries == true` |

### Boutons Series Timer (SeriesTimerDetailsContent)

| Libelle | Action | Condition |
|---------|--------|-----------|
| Annuler la serie | Cancel series timer | Toujours |

---

## 4. Architecture technique

### Stack

| Couche | Detail |
|--------|--------|
| UI | 100% Jetpack Compose (pas de XML layout) |
| Fragment | `ItemDetailComposeFragment` → `ComposeView` → `ItemDetailScreen` |
| ViewModel | `ItemDetailsViewModel` (Koin inject) |
| DI | Koin (`by viewModel()`, `by inject()`) |
| API | Jellyfin SDK (`userLibraryApi`, `tvShowsApi`, `itemsApi`, `libraryApi`, `liveTvApi`, `playlistsApi`, `playStateApi`) |
| Images | Coil 3 (`AsyncImage`) |
| Navigation | `NavigationRepository` → `Destinations.itemDetails(itemId, serverId)` |

### Donnees chargees depuis Jellyfin

| Appel API | Donnee | Utilise par |
|-----------|--------|-------------|
| `userLibraryApi.getItem()` | Item principal + userData + mediaSources + people | Tous |
| `tvShowsApi.getSeasons()` | Saisons d'une serie | SeriesDetailsContent |
| `tvShowsApi.getEpisodes()` | Episodes d'une saison | SeasonDetailsContent, MovieDetailsContent (episode) |
| `tvShowsApi.getNextUp()` | Prochain episode a regarder | SeriesDetailsContent |
| `libraryApi.getSimilarItems()` | Items similaires (limit 12) | MovieDetailsContent, SeriesDetailsContent |
| `itemsApi.getItems(artistIds)` | Albums d'un artiste | MusicDetailsContent |
| `itemsApi.getItems(parentId)` | Tracks d'un album / items collection | MusicDetailsContent, BoxSet |
| `playlistsApi.getPlaylistItems()` | Items d'une playlist | MusicDetailsContent |
| `liveTvApi.getDefaultTimer()` | Timer defaut pour enregistrement | LiveTvDetailsContent |
| `liveTvApi.getTimers()` | Timers programmes pour series timer | SeriesTimerDetailsContent |

### Flux de donnees

```
ItemDetailComposeFragment.onViewCreated()
  → viewModel.loadItem(itemId, serverId)
    → userLibraryApi.getItem()
    → extract cast, directors, writers, badges
    → loadAdditionalData(item) selon item.type
      → loadSeasons / loadEpisodes / loadSimilar / loadTracks / etc.
  → uiState.collect → Compose recompose
  → themeMusicPlayer.playThemeMusicForItem(item)
```

---

## 5. Screenshots captures

| Fichier | Description |
|---------|-------------|
| `docs/screenshots/detail_film.png` | Detail film Avatar — header complet : logo, metadata, badges (4K HDR10 HEVC MKV 5.1 AC3), ratings (★7.6, 🍅81%), tagline, synopsis, poster, boutons d'action, metadata section |
| `docs/screenshots/detail_film_bottom.png` | Detail film Avatar — bas de page : metadata studios, Distribution et equipe (cast circulaire), Plus comme ceci (similaires, posters en chargement) |
| `docs/screenshots/detail_episode.png` | Detail episode Monarch S1E7 — header : nom serie + badge S1E7, logo, metadata, badges, rating, synopsis, thumbnail episode, boutons d'action |
| `docs/screenshots/detail_episode_bottom.png` | Detail episode Monarch — bas : realisateur, Saison 1 - Episodes (cards horizontales), Distribution et equipe |
| `docs/screenshots/check_focus.png` | Detail film Alien — bas : cast (Tom Skerritt, Sigourney Weaver...), Plus comme ceci |

---

## 6. Points faibles identifies

### P0 — Critique

| # | Zone | Probleme | Detail |
|---|------|----------|--------|
| 1 | Boutons action | Style Jellyfin, pas VegafoX | Les boutons sont des cercles gris (`outlineVariant`) avec icones blanches. Aucune couleur VegafoX (orange). Le bouton principal (Reprendre/Play) n'est pas differencie visuellement des autres — meme taille, meme couleur, meme forme. Sur Netflix/Disney+, le bouton Play est un CTA large et colore, bien distinct. |
| 2 | Boutons action | Trop de boutons | 9 boutons sur Avatar (Reprendre, Recommencer, Audio, Sous-titres, Bandes-anno, Vu, Favori, Liste de, Supprimer). L'utilisateur TV doit scroller horizontalement entre les boutons avec le D-pad. Netflix en a 3-4 max visibles. |
| 3 | Poster similaires | Posters non charges | Les cards "Plus comme ceci" affichent des placeholders marron sans image. Possible probleme de prefetch ou de timing de chargement. |
| 4 | Backdrop | Identique a v2 legacy | Le `ItemDetailsFragment` utilise encore un ImageView Android pour le backdrop (pas Compose). Le `ItemDetailComposeFragment` n'a pas de backdrop integre — il delegue au content. Resultat : pas de fond dynamique coherent avec le design system VegafoX. |

### P1 — Important

| # | Zone | Probleme | Detail |
|---|------|----------|--------|
| 5 | Metadata section | Design datee | Le `MetadataGroup` (genres, realisateur, scenaristes, studio) utilise un fond `outlineVariant` avec separateurs. C'est fonctionnel mais visuellement plat — pas de hierarchie, pas d'icones, tout le meme poids. Netflix utilise des liens cliquables colores. |
| 6 | Info row | Trop dense | Annee + duree + "Se termine a" + rating officiel + 6 badges techniques sur une seule zone. L'information est compacte mais difficile a scanner rapidement. Les badges techniques (4K, HDR10, HEVC, MKV, 5.1, AC3) sont utiles aux power users mais encombrent la vue pour l'utilisateur moyen. |
| 7 | Cast section | Photos manquantes | Certains acteurs n'ont pas de photo (placeholder avec initiale). Le circle 90dp est petit pour un ecran TV — difficile de reconnaitre les visages. |
| 8 | Absence de topbar/sidebar | Pas de navigation | L'ecran detail film n'affiche ni la sidebar premium ni la topbar. L'utilisateur ne peut revenir qu'avec le bouton Back. Incoherent avec le home screen qui a toujours la sidebar. |
| 9 | Synopsis | Tronque a 4 lignes | Pas de bouton "Lire plus" pour voir le synopsis complet. Sur un ecran TV 1080p, 4 lignes de bodyMedium c'est court pour des descriptions longues. |

### P2 — Mineur

| # | Zone | Probleme | Detail |
|---|------|----------|--------|
| 10 | Typographie | Melange de styles | Le titre utilise `headlineLargeBold` ou un logo image. La tagline est en italique `bodyLarge`. Le synopsis en `bodyMedium`. Les labels de boutons en `labelMedium W600`. Pas de hierarchie typographique claire VegafoX. |
| 11 | Couleurs | Palette Jellyfin | Les couleurs dominantes sont `outlineVariant` (gris), `textSecondary` (beige), `textHint` (gris clair). Pas d'accent orange VegafoX sauf quand un element est focused. Le design est neutre, pas branded. |
| 12 | Episodes horizontaux | Cards petites | Les EpisodeCard dans la row horizontale font 220×124dp pour le thumbnail. C'est petit sur un ecran TV. Netflix utilise des cards beaucoup plus larges pour les episodes. |
| 13 | Animations | Aucune animation d'entree | Le contenu apparait d'un coup quand les donnees sont chargees. Pas de stagger, pas de fadeIn, pas de transition. Contraste fort avec le home screen qui a des animations soignees. |
| 14 | Focus initial | Bouton Play | Le focus auto va sur le bouton Play/Resume, ce qui est correct. Mais si l'utilisateur scrolle vers le haut, il arrive sur un `Box focusable` invisible (le header), ce qui est confus. |

---

## 7. Comparaison avec Netflix / Apple TV+ / Disney+

| Element | Netflix | Apple TV+ | VegafoX actuel | Ecart |
|---------|---------|-----------|----------------|-------|
| Bouton Play | Large, rouge, prominent | Large, blanc | Petit cercle gris identique aux autres | Fort |
| Nombre boutons visibles | 3-4 (Play, +Liste, Pouce, Info) | 3-4 | 9 boutons tous egaux | Fort |
| Backdrop | Plein ecran, crossfade anime | Plein ecran avec parallaxe | ImageView statique alpha 0.8 | Moyen |
| Synopsis | Expandable, bouton "Plus" | Expandable | Tronque 4 lignes, pas expandable | Moyen |
| Cast | Photos larges, navigables | Photos larges avec roles | Circles 90dp petits | Moyen |
| Episodes | Cards larges avec description | Cards larges | Cards 220×124dp petites | Moyen |
| Badges techniques | Petits icones discrets | Absent | 6 badges proeminents | Faible |
| Navigation retour | Header avec logo/nav | Header avec nav | Aucun header, Back button seul | Fort |
| Animation d'entree | Slide + fade stagger | Fade + scale | Aucune | Moyen |

---

## 8. Estimation de la refonte

### Phase A — Boutons d'action (priorite haute)
- Remplacer les cercles gris par un CTA principal VegafoX (bouton orange large pour Play/Resume)
- Regrouper les boutons secondaires (Audio, Sous-titres, Version) dans un menu "..." ou les masquer
- Max 4-5 boutons visibles : Play, Trailer, Watched, Favorite, More
- **Fichiers** : `DetailActions.kt`, `ItemDetailsComponents.kt` (DetailActionButton)

### Phase B — Design system VegafoX (priorite haute)
- Appliquer les couleurs VegafoX (OrangePrimary, Background, Surface, TextPrimary/Secondary)
- Remplacer le backdrop ImageView par un backdrop Compose avec crossfade + blur (comme le hero home)
- Ajouter la sidebar premium + topbar pour coherence navigation
- **Fichiers** : `ItemDetailComposeFragment.kt`, `ItemDetailsFragment.kt`, `ItemDetailsComponents.kt`

### Phase C — Mise en page moderne (priorite moyenne)
- Agrandir les cards episodes (360×200dp min)
- Agrandir les photos cast (120dp circles)
- Synopsis expandable ("Lire plus")
- Animation d'entree stagger sur les sections
- Badges techniques relegues dans un detail expandable
- **Fichiers** : `MovieDetailsContent.kt`, `SeriesDetailsContent.kt`, `SeasonDetailsContent.kt`, `DetailSections.kt`

### Phase D — Polish (priorite basse)
- Crossfade anime backdrop quand on navigue entre similaires (comme PersonDetailsContent)
- Parallaxe leger sur scroll
- Theme music fade in/out plutot que cut
- Prefetch des posters similaires pour eviter les placeholders vides

---

## 9. Arborescence des fichiers

```
ui/itemdetail/
  compose/
    ItemDetailComposeFragment.kt   ← Fragment principal (Compose-first)
    ItemDetailScreen.kt            ← Dispatcher type → content
  v2/
    ItemDetailsFragment.kt         ← Fragment legacy (ImageView backdrop)
    ItemDetailsViewModel.kt        ← ViewModel unique partage
    ItemDetailsComponents.kt       ← Composants UI reutilisables
    content/
      MovieDetailsContent.kt       ← Film, episode, video, boxset
      SeriesDetailsContent.kt      ← Serie
      SeasonDetailsContent.kt      ← Saison (liste episodes verticale)
      PersonDetailsContent.kt      ← Personne (slideshow backdrop)
      MusicDetailsContent.kt       ← Album, artiste, playlist
      LiveTvDetailsContent.kt      ← Programme Live TV
      SeriesTimerDetailsContent.kt ← Timer serie Live TV
    shared/
      DetailActions.kt             ← Row de boutons d'action
      DetailSections.kt            ← Sections partagees (cast, similar, etc.)
      DetailUtils.kt               ← Helpers URL images + formatage
```
