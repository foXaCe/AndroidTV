# Audit — Section episodes page detail

**Date** : 2026-03-17
**Scope** : `DetailSections.kt`, `ItemDetailsComponents.kt`, `MovieDetailsContent.kt`, `SeasonDetailsContent.kt`, `ItemDetailsViewModel.kt`, `DetailUtils.kt`

---

## Point 1 — Header de section

### Contexte d'affichage

La section episodes apparait dans deux contextes :

**A) Page Episode (MovieDetailsContent.kt:56-58, 74-85)**

```kotlin
val episodesTitle = item.parentIndexNumber?.let {
    stringResource(R.string.lbl_season_episodes, it)
} ?: stringResource(R.string.lbl_episodes)
```

- Si `parentIndexNumber` disponible : `lbl_season_episodes` = `"Season %1$d Episodes"` (EN) / `"Saison %1$d - Épisodes"` (FR)
- Sinon fallback : `lbl_episodes` = `"Episodes"` (EN) / `"Épisodes"` (FR)
- Rendu via `detailSectionHeader(title = episodesTitle)` (DetailContentScaffold.kt:300-311)

**B) Page Season (SeasonDetailsContent.kt:39-40, 60)**

```kotlin
val episodesLabel = stringResource(R.string.lbl_episodes).uppercase()
val episodesHeaderTitle = item.name?.let { "${it.uppercase()} — $episodesLabel" } ?: episodesLabel
```

- Format : `"SAISON 1 — ÉPISODES"` (nom de saison uppercased + em-dash + label uppercased)
- Rendu via `detailSectionHeader(title = episodesHeaderTitle)` puis `showHeader = false` sur le composable interne

### Typographie du header

`SectionHeader` (ItemDetailsComponents.kt:764-774) :
- Style : `JellyfinTheme.typography.titleLarge` = `fontSize = 20.sp, lineHeight = 28.sp, fontWeight = W600`
- Couleur : `JellyfinTheme.colorScheme.onSurface`
- Font : **defaut systeme (Roboto)**, PAS BebasNeue
- Padding bottom : 10.dp

### Le probleme du "É" suivi d'un chiffre

Le caractere `É` n'est PAS un probleme de font. Il vient de la string resource FR :

```xml
<!-- values-fr/strings.xml:887 -->
<string name="lbl_episode_number_str">É%1$s</string>
```

En francais, le label d'episode est `"É1"` / `"É2"` etc. Ce n'est pas un rendu incorrect de `"E"` — c'est une **decision de traduction** qui abrege "Épisode" en "É" au lieu de "E". La font Roboto rend parfaitement le `É`.

En anglais la string est `"E%1$s"` (values/strings.xml:896), donc `"E1"`.

**Le vrai probleme** : `"É1"` est visuellement confus — personne ne lit ca comme "Episode 1". La convention standard pour les numeros d'episodes est `"E1"` dans toutes les langues (notation IMDb/TVDB universelle). L'accent est une sur-adaptation de la traduction FR.

---

## Point 2 — Image des episodes

### Fonction utilisee

`DetailEpisodesHorizontalSection` (DetailSections.kt:117) :

```kotlin
val imageUrl = remember(ep.id) { getPosterUrl(ep, api) }
```

### Logique de getPosterUrl pour les episodes (DetailUtils.kt:62-65)

```kotlin
item.type == BaseItemKind.EPISODE -> {
    val seriesPoster = item.seriesPrimaryImage
    val episodePoster = item.itemImages[ImageType.PRIMARY]
    (seriesPoster ?: episodePoster)?.getUrl(api, maxHeight = 600)
}
```

**Priorite** : `seriesPrimaryImage` (poster portrait de la SERIE) > `episodePoster` (image PRIMARY de l'episode)

### Probleme

L'image affichee est le **poster portrait 2:3 de la serie parente**, PAS la vignette specifique de l'episode. Raisons :

1. `seriesPrimaryImage` est presque toujours disponible (c'est le poster de la serie) et a la priorite
2. `ImageType.PRIMARY` pour un episode peut etre soit la vignette paysage de l'episode soit null — mais elle est ignoree si `seriesPrimaryImage` existe
3. L'image `ImageType.THUMB` (vignette paysage specifique a l'episode, typiquement un screenshot) n'est **jamais demandee**

**Consequence** : toutes les cartes d'episodes affichent le meme poster de serie, rendant impossible de distinguer visuellement les episodes. C'est le bug le plus impactant de la section.

### Correction necessaire

Pour les episodes dans la row horizontale, il faudrait utiliser :
1. `item.itemImages[ImageType.PRIMARY]` (vignette episode-specifique)
2. Fallback sur `item.itemImages[ImageType.THUMB]` (screenshot)
3. Dernier fallback sur `seriesPrimaryImage`

---

## Point 3 — Contenu de la EpisodeCard

### Composable : EpisodeCard (ItemDetailsComponents.kt:492-617)

**Champs affiches :**

| Champ | Source | Typographie | Couleur |
|-------|--------|-------------|---------|
| Numero d'episode | `R.string.lbl_episode_number_str` + `episodeNumber` | `labelMedium`, W700 (~12sp) | `textHint` |
| Titre | `ep.name` | `bodySmall` (12sp) | `onSurface` |
| Duree | `formatDuration(ep.runTimeTicks)` | `labelSmall` (10sp) | `textDisabled` |

**Layout** : `Row` horizontale (padding 8dp H, 6dp V) sous l'image. Les 3 champs sont sur une seule ligne.

**Dimensions carte** :
- Largeur : `CardDimensions.landscapeWidth` = 220.dp
- Hauteur image : `CardDimensions.landscapeHeight` = 124.dp
- Shape : `JellyfinTheme.shapes.button` = `RoundedCornerShape(6.dp)`

**Elements visuels supplementaires :**
- Badge "vu" (checkmark) en TopEnd si `isPlayed && progress <= 0` (ligne 560-566)
- Barre de progression 2dp en BottomStart si `progress > 0` (lignes 569-586) — track `Background 50%`, fill `primary`
- Bordure orange 2dp si `isCurrent` (alpha 0.4f) ou `isFocused` (opaque)
- Background orange 8% si `isCurrent`

**Ce qui manque :**
- **Synopsis court** : `ep.overview` n'est jamais affiche
- Le numero et le titre sont sur la meme ligne, ce qui compresse le titre si le numero est large

---

## Point 4 — Compteur XX/XX

### Donnees disponibles dans ItemDetailsUiState

```kotlin
data class ItemDetailsUiState(
    val episodes: List<BaseItemDto> = emptyList(),
    // ...
)
```

| Donnee | Disponible ? | Source |
|--------|-------------|--------|
| Numero de saison | Oui | `item.parentIndexNumber` (depuis l'episode) ou `item.indexNumber` (depuis la saison) |
| Nombre total d'episodes de la saison | Oui | `uiState.episodes.size` (toute la liste est chargee) |
| Index de l'episode en cours dans la saison | Oui | `item.indexNumber` + position dans `uiState.episodes` via `ep.id == currentEpisodeId` |

### Usage actuel

**Page Season** (SeasonDetailsContent.kt:35-38) :

```kotlin
val episodeCountText = stringResource(
    if (uiState.episodes.size == 1) R.string.lbl_episode_count_singular else R.string.lbl_episodes_count,
    uiState.episodes.size,
)
```

Affiche `"10 Episodes"` / `"10 épisodes"` dans les metadata, PAS dans le header.

**Page Episode** (MovieDetailsContent.kt:56-58) :

```kotlin
val episodesTitle = item.parentIndexNumber?.let {
    stringResource(R.string.lbl_season_episodes, it)
} ?: stringResource(R.string.lbl_episodes)
```

Affiche `"Saison 1 - Épisodes"` dans le header. **Pas de compteur d'episodes ni d'index courant.**

### Ce qui manque pour "Saison XX · Épisodes XX/XX"

Toutes les donnees sont disponibles :
- `item.parentIndexNumber` → numero de saison
- `item.indexNumber` → numero de l'episode courant
- `uiState.episodes.size` → total episodes

Aucun calcul supplementaire n'est necessaire, il suffit de formater le header differemment.

---

## Resume des constats

| # | Constat | Severite | Impact |
|---|---------|----------|--------|
| 1 | String FR `"É%s"` au lieu de `"E%s"` pour le numero d'episode | Mineur | Confusion visuelle, non-standard |
| 2 | `getPosterUrl` retourne le poster SERIE (portrait) au lieu de la vignette EPISODE (paysage) | **Majeur** | Toutes les cartes sont identiques, episodes indistinguables |
| 3 | Synopsis court (`overview`) absent des cartes | Mineur | Manque d'information pour choisir un episode |
| 4 | Pas de compteur "Episode X/Y" dans le header malgre donnees disponibles | Mineur | Manque de contexte de navigation |
| 5 | Barre de progression EpisodeCard utilise `primary` au lieu de `BlueAccent` (incoherent avec Home) | Cosmetique | Incoherence design system |
| 6 | Image dans EpisodeCard utilise `AsyncImage` brut au lieu de `CachedAsyncImage` | Performance | Pas de cache memoire partage |
