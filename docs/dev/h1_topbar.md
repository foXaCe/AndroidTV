# H1 — Topbar Immersive (Option C)

**Date** : 2026-03-10
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Résumé

Remplacement complet du layout `MainToolbar` par le design "Option C Immersive" :
barre horizontale avec logo VegafoX à gauche, onglets de navigation au centre,
recherche + avatar à droite.

---

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `app/.../ui/shared/toolbar/MainToolbar.kt` | Réécriture complète du layout |
| `app/src/main/res/values/strings.xml` | Ajout `lbl_music`, `lbl_live_tv` |
| `app/src/main/res/values-fr/strings.xml` | Ajout traductions `lbl_music`, `lbl_live_tv` |

## Fichiers non modifiés (conservés tels quels)

| Fichier | Raison |
|---------|--------|
| `Toolbar.kt` | Composables `Toolbar`, `ToolbarClock`, `Logo` — non utilisés par le nouveau layout mais potentiellement utiles ailleurs |
| `ToolbarButtons.kt` | `ToolbarButtons` — idem |
| `ExpandableIconButton.kt` | — idem |
| `ExpandableLibrariesButton.kt` | — idem |
| `HomeScreen.kt` | Appelle toujours `MainToolbar(activeButton = Home)` — aucun changement nécessaire |

---

## Nouveau layout

```
+------------------------------------------------------------------------+
| [🦊 32dp] [VegafoX 15sp]  | Accueil | Films | Séries | Musique | Live TV |  [🔍 Rechercher]  [S]  |
+------------------------------------------------------------------------+
```

### Gauche
- `Image(ic_vegafox_fox)` 32dp — renard non animé
- `VegafoXTitleText(fontSize = 15.sp)` — "Vega" bleu + "foX" orange

### Centre — Onglets
- Container : `Row` avec `Surface.copy(alpha = 0.04f)`, border `Divider` 1dp, coins 10dp, clip
- 5 onglets : Accueil, Films, Séries, Musique, Live TV
- Chaque onglet : `Button(shape = RoundedCornerShape(0.dp))`, padding 18dp/7dp, texte 13sp
- **Inactif** : fond transparent, couleur `TextSecondary`
- **Actif** : fond `OrangeSoft`, couleur `OrangePrimary`, `fontWeight = Bold`, bordures gauche/droite `OrangeBorder` (drawBehind)
- Focus : fond `focusBorderColor()`, texte `TextPrimary`

### Droite
- **Search box** : `Button(shape = RoundedCornerShape(8.dp))`, fond `Surface * 0.04`, border `Divider`, padding 12dp/6dp — icône `ic_search` 16dp + texte `lbl_search` 12sp, couleur `TextHint`
- **Avatar** : `IconButton(shape = CircleShape)` 32dp, fond `OrangePrimary`, texte blanc Bold 14sp — initiale de l'utilisateur connecté (`userName.firstOrNull()`)

---

## Navigation onglets

| Onglet | Index | Action |
|--------|-------|--------|
| Accueil | 0 | `navigationRepository.reset(Destinations.home)` |
| Films | 1 | `userViews.first { collectionType == MOVIES }` → `itemLauncher.getUserViewDestination()` |
| Séries | 2 | `userViews.first { collectionType == TVSHOWS }` → idem |
| Musique | 3 | `userViews.first { collectionType == MUSIC }` → idem |
| Live TV | 4 | `userViews.first { collectionType == LIVETV }` → idem |

- `selectedTab` : state local initialisé à 0
- Synchronisé avec `activeButton` / `activeLibraryId` via `LaunchedEffect`
- Si la bibliothèque n'existe pas sur le serveur, le clic est ignoré (no-op)

---

## Corrections incluses

### CoroutineScope leak (P2 audit)

**Avant** (lignes 389, 499) :
```kotlin
kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
    shuffleManager.quickShuffle(context)
}
```

**Après** : Code supprimé avec le remplacement du layout. Le composable public utilise
`LaunchedEffect` pour la collecte de flows (pas de scope manuel). Le composable privé
n'utilise aucun `CoroutineScope` — toutes les actions sont synchrones
(`navigationRepository.reset/navigate`, `sessionRepository.destroyCurrentSession`).

---

## Éléments retirés du toolbar

Les boutons suivants ne sont plus dans le toolbar (accès via d'autres mécanismes) :

| Bouton | Ancienne position | Alternative |
|--------|------------------|-------------|
| Shuffle | Centre | Menu contexte futur |
| Genres | Centre | Écran dédié via navigation |
| Favoris | Centre | Écran dédié via navigation |
| Jellyseerr | Centre | Écran dédié via navigation |
| Dossiers | Centre | Écran dédié via navigation |
| SyncPlay | Centre | Dialog toujours actif (SyncPlayViewModel.visible) |
| Bibliothèques | Centre | Remplacé par onglets Films/Séries/Musique/Live TV |
| Paramètres | Centre | Accès via menu système ou futur bouton |
| Horloge | Droite | Retirée |
| NowPlaying | Gauche | Retirée du toolbar |

---

## Strings ajoutées

| Clé | EN | FR |
|-----|----|----|
| `lbl_music` | Music | Musique |
| `lbl_live_tv` | Live TV | TV en direct |

---

## API publique inchangée

```kotlin
// Signature identique — aucun appelant cassé
@Composable
fun MainToolbar(
    activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
    activeLibraryId: UUID? = null,
)

fun setupMainToolbarComposeView(
    composeView: ComposeView,
    activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
    activeLibraryId: UUID? = null,
)

enum class MainToolbarActiveButton { User, Home, Library, Search, Jellyseerr, None }
```

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```

0 erreur, 0 warning.
