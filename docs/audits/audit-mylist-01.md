# Audit "Ma liste" vs "Favoris" -- 2026-03-16

## Contexte

Determiner si "Ma liste" et "Favoris" sont des fonctions distinctes ou un doublon dans l'app VegafoX Android TV.

---

## Point 1 : Bouton Favoris -- endpoint, SDK, stockage serveur

### Bouton dans l'UI detail

Le bouton Favoris est present dans les ecrans de detail, rendu par deux chemins :

**Chemin 1 -- `DetailActions.kt` (composant reutilisable)**
- Fichier : `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/shared/DetailActions.kt`
- Lignes 205-218 : Bouton `VegafoXButton` avec icone `VegafoXIcons.Favorite`
- Label dynamique : `R.string.lbl_favorite` ("Favorite" / "Favori") ou `R.string.lbl_favorited` ("Favorited" / "En favori")
- Callback : `callbacks.onToggleFavorite`

**Chemin 2 -- `MovieDetailsContent.kt` (inline dans le hero)**
- Fichier : `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/content/MovieDetailsContent.kt`
- Lignes 337-350 : Bouton identique, appelle `actionCallbacks.onToggleFavorite`

### Implementation du toggle

**ViewModel** : `ItemDetailsViewModel.kt` lignes 415-447
```
fun toggleFavorite() {
    val newFavorite = !(item.userData?.isFavorite ?: false)
    // Optimistic update local
    _uiState.value = _uiState.value.copy(item = item.copy(userData = item.userData?.copy(isFavorite = newFavorite)))
    // Appel serveur
    if (newFavorite) effectiveApi.userLibraryApi.markFavoriteItem(itemId = item.id)
    else effectiveApi.userLibraryApi.unmarkFavoriteItem(itemId = item.id)
}
```

**Repository** : `ItemMutationRepository.kt` lignes 28-39
```
override suspend fun setFavorite(item: UUID, favorite: Boolean): UserItemDataDto {
    val response by when {
        favorite -> api.ioCall { userLibraryApi.markFavoriteItem(itemId = item) }
        else -> api.ioCall { userLibraryApi.unmarkFavoriteItem(itemId = item) }
    }
    dataRefreshService.lastFavoriteUpdate = Instant.now()
    return response
}
```

### Endpoints Jellyfin SDK

| Action | Methode SDK | Endpoint HTTP |
|--------|-------------|---------------|
| Marquer favori | `userLibraryApi.markFavoriteItem(itemId)` | `POST /Users/{userId}/FavoriteItems/{itemId}` |
| Retirer favori | `userLibraryApi.unmarkFavoriteItem(itemId)` | `DELETE /Users/{userId}/FavoriteItems/{itemId}` |

### Stockage

**Cote serveur** : Le flag `isFavorite` est stocke dans `UserItemData` sur le serveur Jellyfin, associe au couple (userId, itemId). C'est une donnee utilisateur standard du serveur.

### Ecran de consultation des favoris

- Sidebar : `PremiumSideBar.kt` ligne 399-410, NavItem "Favoris" -> `Destinations.allFavorites`
- Ecran : `AllFavoritesScreen.kt` -- requete `ItemFilter.IS_FAVORITE` sur les categories (films, series, episodes, playlists, cast)
- ViewModel : `AllFavoritesViewModel` -- utilise `api.itemsApi.getItems(filters = setOf(ItemFilter.IS_FAVORITE))`

---

## Point 2 : "Ma liste" -- endpoint, SDK, stockage

### Etat actuel : FONCTIONNALITE SUPPRIMEE

"Ma liste" n'existe plus dans le code actif de l'application. Voici les vestiges :

**Code mort** : `LocalWatchlistRepository.kt`
- Fichier : `app/src/main/java/org/jellyfin/androidtv/data/repository/LocalWatchlistRepository.kt`
- Classe complete (170 lignes) toujours presente dans le codebase
- Stockage : **SharedPreferences local** (`local_watchlist`), PAS le serveur Jellyfin
- Format : Liste de `WatchlistEntry(itemId, serverId, addedAt)` serialisee en JSON
- **Non injectee dans Koin** : aucune reference dans `AppModule.kt`
- **Aucun import** dans un fichier actif (seule reference : sa propre definition)

**String supprimee** : `lbl_my_list`
- La string resource `lbl_my_list` ("My List" / "Ma liste") a ete supprimee des fichiers `strings.xml`
- Aucune reference a `lbl_my_list` dans le code Kotlin
- Documentee comme supprimee dans `docs/audits/audit-strings-01.md` ligne 112

**Bouton hero supprime** :
- Les documents de design (`docs/prompts/home/h2_hero.md`, `docs/prompts/home/hero_fix1.md`) mentionnent un bouton "+ Ma liste" dans le hero de l'ecran d'accueil
- Ce bouton n'existe plus dans `HomeHeroBackdrop.kt` (aucun bouton d'action present)
- Le composant `HeroInfoOverlay` n'affiche que metadata + description, sans boutons

**Migration** : Le `HomeSectionType` a remplace `watchlist` par `PLAYLISTS` (voir `HomeSectionConfig.kt` ligne 29 et `UserSettingPreferences.kt` ligne 315).

### Historique de la fonctionnalite

"Ma liste" etait une **watchlist locale** (app-only) qui :
- Stockait les items dans SharedPreferences (pas sur le serveur)
- N'utilisait AUCUN endpoint Jellyfin pour le stockage
- Recuperait les metadonnees via `api.itemsApi.getItems(ids = itemIds)` pour l'affichage
- N'avait aucun lien avec le systeme de favoris serveur

---

## Point 3 : Difference fonctionnelle reelle

### AUCUN doublon actuel

Il n'y a **pas** de doublon car "Ma liste" n'existe plus dans l'app active.

### Comparaison historique (avant suppression)

| Critere | Favoris | Ma liste (supprimee) |
|---------|---------|----------------------|
| **Stockage** | Serveur Jellyfin (`UserItemData.isFavorite`) | Local SharedPreferences |
| **Endpoint** | `POST/DELETE /Users/{userId}/FavoriteItems/{itemId}` | Aucun (local uniquement) |
| **Synchronisation** | Multi-device via serveur | Perdue si reinstallation / autre device |
| **SDK** | `userLibraryApi.markFavoriteItem()` / `unmarkFavoriteItem()` | `itemsApi.getItems(ids=...)` (lecture seule) |
| **Filtrage serveur** | `ItemFilter.IS_FAVORITE` | Impossible (pas cote serveur) |
| **Semantique** | "J'aime cet item" | "A regarder plus tard" |
| **UI** | Bouton dans detail + ecran dedie sidebar | Bouton hero accueil (supprime) |

### Les deux fonctions etaient **conceptuellement distinctes** :
1. **Favoris** = preference utilisateur synchronisee serveur ("j'aime")
2. **Ma liste** = watchlist locale temporaire ("a voir plus tard")

---

## Point 4 : Recommandation

### A court terme : Supprimer le code mort

Le fichier `LocalWatchlistRepository.kt` est du code mort :
- Non injecte dans Koin (bean supprime dans cleanup_08)
- Aucun import actif
- String `lbl_my_list` deja supprimee

**Action** : Supprimer `app/src/main/java/org/jellyfin/androidtv/data/repository/LocalWatchlistRepository.kt`

### A moyen terme : Pas de reintroduction necessaire

Le bouton "Favoris" dans l'ecran de detail couvre le besoin principal. L'ecran "Favoris" de la sidebar (`AllFavoritesScreen`) permet de retrouver tous les items marques.

Une "watchlist" locale distincte apporterait de la confusion UX sans benefice clair :
- Les utilisateurs ne comprennent pas la difference entre "Favori" et "Ma liste"
- Le stockage local uniquement (SharedPreferences) est fragile et non synchronise
- Les plateformes de streaming (Netflix, Disney+) ont fusionne ces concepts

### Si une watchlist est souhaitee a l'avenir

Utiliser le systeme de **playlists Jellyfin** (deja implemente via `AddToPlaylistDialog`) plutot qu'un stockage local. Cela offre :
- Synchronisation serveur native
- UI existante (`AddToPlaylistDialog.kt`, `AddToPlaylistDialogLauncher.kt`)
- Le bouton "Playlist" est deja present dans l'ecran de detail (ligne 237-244 de `DetailActions.kt`)

---

## Fichiers cles references

| Fichier | Role |
|---------|------|
| `app/.../ui/itemdetail/v2/shared/DetailActions.kt` | Boutons d'action detail (Favoris L207, Playlist L237) |
| `app/.../ui/itemdetail/v2/content/MovieDetailsContent.kt` | Detail film (Favoris L339, Playlist L369) |
| `app/.../ui/itemdetail/v2/ItemDetailsViewModel.kt` | `toggleFavorite()` L415-447 |
| `app/.../data/repository/ItemMutationRepository.kt` | `setFavorite()` via SDK L28-39 |
| `app/.../data/repository/LocalWatchlistRepository.kt` | CODE MORT -- ancienne watchlist locale |
| `app/.../ui/browsing/compose/AllFavoritesScreen.kt` | Ecran de consultation des favoris |
| `app/.../ui/home/compose/sidebar/PremiumSideBar.kt` | NavItem "Favoris" L399-410 |
| `app/.../ui/playlist/AddToPlaylistDialog.kt` | Dialog ajout a playlist (alternative a watchlist) |
| `app/.../util/KeyProcessor.kt` | Toggle favori via menu contextuel L316-321, L373-382 |
