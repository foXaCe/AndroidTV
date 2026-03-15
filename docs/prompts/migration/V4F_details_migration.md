# V4F — Migration ItemDetailsFragment → Compose

**Date** : 2026-03-08
**Statut** : Phase 1 terminee (Compose wrapper + backdrop)
**Build** : BUILD SUCCESSFUL (compileGithubReleaseKotlin)
**Tests** : testGithubDebugUnitTest PASS

---

## Resume

Migration de l'ecran de details (ItemDetailsFragment) vers une architecture Compose-first. Le nouveau `ItemDetailComposeFragment` est un wrapper leger qui delegue 100% du rendu a `ItemDetailScreen` (Compose), avec un `DetailHeroBackdrop` Compose qui remplace l'ImageView du Fragment.

### Changements cles

1. **Backdrop Compose** : `DetailHeroBackdrop.kt` remplace l'ImageView + drawable gradient du Fragment. Crossfade 400ms, blur RenderEffect (API 31+), double gradient (vertical + horizontal).

2. **Router Compose** : `ItemDetailScreen.kt` dispatche par type (Movie/Series/Music/Person/Season) vers les composables de contenu existants de `v2/content/`.

3. **Fragment wrapper** : `ItemDetailComposeFragment.kt` (306 LOC) est un wrapper qui possede uniquement les callbacks playback (play, resume, trailers, delete) et le lifecycle theme music.

4. **Elimination du FrameLayout custom** : Le v2 Fragment avait un FrameLayout custom avec `dispatchKeyEvent` pour intercepter D-pad et gerer sidebar/toolbar. Le nouveau Fragment utilise un simple ComposeView.

---

## Fichiers crees (3 fichiers, 607 LOC)

| Fichier | LOC | Role |
|---------|-----|------|
| `ui/itemdetail/compose/DetailHeroBackdrop.kt` | 120 | Backdrop Compose plein ecran avec blur + gradients |
| `ui/itemdetail/compose/ItemDetailScreen.kt` | 181 | Router Compose — state handling + type dispatch |
| `ui/itemdetail/compose/ItemDetailComposeFragment.kt` | 306 | Fragment wrapper — playback, trailers, theme music |

## Fichiers modifies (1 fichier)

| Fichier | Modification |
|---------|-------------|
| `ui/navigation/Destinations.kt` | `itemDetails` → `ItemDetailComposeFragment`, ancien garde comme `itemDetailsV2` |

---

## Comparaison avant → apres

### Ancien systeme (v2 Fragment)

| Fichier | LOC | Responsabilites |
|---------|-----|-----------------|
| `v2/ItemDetailsFragment.kt` | 629 | FrameLayout custom, ImageView backdrop, blur bitmap, 3 ComposeViews, D-pad dispatch, sidebar/toolbar, playback, trailers, delete, theme music |

### Nouveau systeme (Compose-first)

| Fichier | LOC | Responsabilites |
|---------|-----|-----------------|
| `compose/ItemDetailComposeFragment.kt` | 306 | ComposeView simple, playback callbacks, theme music lifecycle |
| `compose/ItemDetailScreen.kt` | 181 | Router Compose, state handling, type dispatch |
| `compose/DetailHeroBackdrop.kt` | 120 | Backdrop Compose avec blur + gradients |
| **Total** | **607** | |

### Code reutilise (inchange)

| Fichier | LOC | Role |
|---------|-----|------|
| `v2/ItemDetailsViewModel.kt` | 510 | Data loading, state management |
| `v2/ItemDetailsComponents.kt` | 1459 | Cards, dialogs, UI components |
| `v2/content/MovieDetailsContent.kt` | 294 | Contenu Movie/Episode/BoxSet |
| `v2/content/SeriesDetailsContent.kt` | 243 | Contenu Series |
| `v2/content/MusicDetailsContent.kt` | 438 | Contenu MusicAlbum/MusicArtist/Playlist |
| `v2/content/PersonDetailsContent.kt` | 295 | Contenu Person |
| `v2/content/SeasonDetailsContent.kt` | 243 | Contenu Season |
| `v2/shared/DetailActions.kt` | 306 | Action buttons row + dialogs |
| `v2/shared/DetailInfoRow.kt` | 116 | Info row (year, runtime, badges) |
| `v2/shared/DetailSections.kt` | 262 | Sections (metadata, seasons, cast, similar) |
| `v2/shared/DetailUtils.kt` | 64 | URL helpers, formatDuration |

---

## Ce qui a ete elimine

| Element | Avant (v2 Fragment) | Apres (Compose) |
|---------|---------------------|-----------------|
| ImageView backdrop | Android View | `DetailHeroBackdrop` Compose |
| `detail_backdrop_gradient.xml` | XML drawable | Brush.verticalGradient inline |
| BitmapBlur (pre-API 31) | Java bitmap manipulation | Non necessaire (RenderEffect seul) |
| FrameLayout custom + dispatchKeyEvent | 120 lignes de D-pad interception | Elimine (Compose gere le focus nativement) |
| 3 ComposeViews separees | content + sidebar + toolbar | 1 ComposeView unique |
| Coil bitmap loading manuel | imageLoader.execute() + toBitmap() | AsyncImage Compose |

---

## Architecture du backdrop Compose

```
+-------------------------------------+
|  Box (fillMaxSize)                  |
|  +-------------------------------+  |
|  | Crossfade (400ms)             |  |
|  |  +- AsyncImage backdrop      |  |
|  |  |  +- graphicsLayer         |  |
|  |  |  |  alpha = 0.8f          |  |
|  |  |  |  blur = blurAmount     |  |
|  |  |  |  (RenderEffect API31+) |  |
|  |  |  +- ContentScale.Crop     |  |
|  |  +- (null → empty Box)       |  |
|  +-------------------------------+  |
|  | Gradient vertical              |  |
|  |  0%→30%→50%→65%→80%→100%      |  |
|  |  transparent → background     |  |
|  +-------------------------------+  |
|  | Gradient horizontal            |  |
|  |  90%→30%→transparent           |  |
|  |  background → transparent     |  |
|  +-------------------------------+  |
+-------------------------------------+
```

---

## Imports Leanback dans itemdetail/

| Metrique | Avant V4F | Apres V4F |
|----------|-----------|-----------|
| Fichiers avec imports Leanback (itemdetail/) | 2 | 2 |
| Total imports Leanback (itemdetail/) | 2 | 2 |

> Les 2 fichiers restants sont `FullDetailsFragment.java` et `MyDetailsOverviewRow.kt` — anciens fichiers legacy non utilises par la navigation actuelle.

| Metrique | Avant V4F | Apres V4F |
|----------|-----------|-----------|
| Fichiers avec imports Leanback (global) | 60 | 60 |
| Total imports Leanback (global) | 209 | 209 |

---

## Prochaines etapes

- [ ] Test AM9 Pro : Movie
- [ ] Test AM9 Pro : Series avec saisons
- [ ] Test AM9 Pro : Music album
- [ ] Test AM9 Pro : Person
- [ ] Test AM9 Pro : Season
- [ ] Ajouter toolbar/sidebar dans le Compose tree (actuellement non inclus — le v2 Fragment les avait)
- [ ] Supprimer `v2/ItemDetailsFragment.kt` (629 LOC) quand la migration est validee
- [ ] Supprimer `FullDetailsFragment.java` + legacy files
- [ ] JellyseerrStatusBadge composable
