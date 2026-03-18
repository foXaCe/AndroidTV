# Audit -- Scroll UP violent sur ecran detail (Cast -> Hero)

**Date** : 2026-03-17
**Fichiers analyses** :
- `DetailContentScaffold.kt` -- LazyColumn principal, hero zone, `disableBringIntoView()`, `onFocusEvent`
- `DetailUtils.kt` -- `disableBringIntoView()` et `smoothScrollToTop()` implementations
- `DetailSections.kt` -- Cast LazyRow avec `focusRestorer()` + `focusGroup()`
- `DetailActions.kt` -- Button rows avec `focusGroup()`
- `VegafoXDimensions.kt` -- Dimensions hero (400dp), gradient (40dp), sections

---

## Point 1 -- Chemin du focus D-pad UP depuis le Cast

### Structure du LazyColumn (cas Movie avec cast)

| Index | Composable | Hauteur | Contenu focusable |
|-------|-----------|---------|-------------------|
| 0 | Hero Box (`disableBringIntoView()`) | 400dp | Boutons Play/Resume + boutons secondaires |
| 1 | Gradient transition | 40dp | Aucun |
| 2 | Metadata section (director, writer, studio) | Variable | Aucun |
| 3 | Cast header ("Cast & Crew") | ~48dp | Aucun |
| 4 | Cast row (LazyRow dans Box de 170dp) | 170dp | CastCard focusables |
| 5 | Similar header | ~48dp | Aucun |
| 6 | Similar row | 270dp | SimilarItemCard focusables |
| 7 | Bottom padding | 80dp | Aucun |

### Trajet du focus

Quand l'utilisateur appuie D-pad UP depuis une `CastCard` (index 4) :

1. Le focus quitte la `CastCard` dans le `LazyRow` (qui a `focusRestorer()` + `focusGroup()`).
2. Compose cherche le composable focusable le plus proche vers le HAUT.
3. Les items aux index 3, 2, 1 ne contiennent **aucun element focusable**.
4. Le prochain focusable est dans l'item 0 (hero) : les boutons dans `DetailActionButtonsRow`.
5. **Le focus saute directement de l'index 4 (cast) a l'index 0 (hero boutons)** -- c'est un saut de ~4 items dans le LazyColumn.

Le probleme fondamental : il n'y a **aucun element focusable intermediaire** entre le cast (index 4) et les boutons hero (index 0). Compose doit donc remonter tout le LazyColumn d'un coup.

---

## Point 2 -- BringIntoView sur les boutons hero

### Mecanisme actuel

Le hero Box (index 0) porte `.disableBringIntoView()` qui installe un `BringIntoViewResponder` no-op :

```kotlin
private val NoOpBringIntoViewResponder =
    object : BringIntoViewResponder {
        override fun calculateRectForParent(localRect: Rect): Rect = Rect.Zero
        override suspend fun bringChildIntoView(localRect: () -> Rect?) {
            // Intentionally empty
        }
    }
```

Ceci neutralise le `bringIntoView()` que Compose declenche automatiquement quand un bouton dans le hero recoit le focus. **Mais ceci ne neutralise PAS le scroll interne du LazyColumn.**

### Le double mecanisme de scroll

Il y a **deux** mecanismes qui tentent de scroller simultanement :

1. **LazyColumn interne** : Quand le focus arrive sur un item qui n'est pas visible, le `LazyColumn` effectue son propre scroll pour rendre l'item visible. Ce scroll est **gere par le `LazyListScrollPosition`** interne et n'est PAS un `BringIntoView`. C'est un `scrollToItem` **instantane** (non anime) declenche par le systeme de focus de `LazyColumn`.

2. **`onFocusEvent` explicite** : Sur la Column info du hero (ligne 153-161) :
```kotlin
.onFocusEvent { event ->
    if (event.hasFocus && listState.firstVisibleItemIndex > 0) {
        coroutineScope.launch {
            listState.animateScrollToItem(index = 0)
        }
    }
}
```

### Race condition identifiee

Voici la sequence exacte qui se produit :

1. L'utilisateur appuie UP depuis le cast.
2. Compose determine que le focus doit aller aux boutons hero (item 0).
3. **Le `LazyColumn` effectue un scroll interne** pour rendre l'item 0 visible -- ce scroll est un `scrollToItem(0)` **instantane** (non anime), car le `LazyColumn` standard utilise `scrollToItem` pour la navigation par focus, pas `animateScrollToItem`.
4. **Quasi-simultanement**, le `onFocusEvent` detecte `hasFocus = true` et lance `animateScrollToItem(0)`.
5. Si le `LazyColumn` a deja scroll a l'item 0, le `animateScrollToItem(0)` est un no-op.
6. **Mais** si le timing fait que le `onFocusEvent` se declenche AVANT que le scroll interne ne soit complete, on a deux scrolls concurrents vers la meme destination.

### Conclusion Point 2

Le `disableBringIntoView()` sur le hero Box **n'empeche PAS le LazyColumn de scroller pour rendre l'item 0 visible**. Le BringIntoView est une couche au-dessus (c'est le mecanisme par lequel un composable enfant demande a ses parents de le rendre visible). Le LazyColumn, lui, a son propre mecanisme de scroll lors du changement de focus qui est **independant** du BringIntoView.

Le `onFocusEvent` + `animateScrollToItem(0)` tente de corriger le scroll, mais entre en **competition** avec le scroll interne du LazyColumn.

---

## Point 3 -- Comportement du fling du LazyColumn

### Absence de flingBehavior personnalise

Aucun `flingBehavior` n'est configure sur le `LazyColumn` dans `DetailContentScaffold.kt` :

```kotlin
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
)
```

Le `LazyColumn` utilise donc le `ScrollableDefaults.flingBehavior()` par defaut de `androidx.compose.foundation 1.10.4`.

### Impact sur l'animation violente

Le flingBehavior par defaut n'est **pas** la cause directe de l'animation violente. Le fling s'applique aux gestes de glissement (swipe/drag), pas a la navigation focus D-pad. Sur Android TV avec le D-pad, le scroll est pilote par le mecanisme de focus, pas par le fling.

**La cause de l'animation violente est le mecanisme de scroll du LazyColumn lors du changement de focus** : quand le focus saute de l'item 4 a l'item 0, le LazyColumn doit scroller d'environ **660dp** (gradient 40dp + metadata ~variable + header 48dp + cast row 170dp + les offsets partiels). Ce saut est effectue de maniere **instantanee** (pas anime) par le LazyColumn, ce qui produit un "teleportation" visuelle.

---

## Point 4 -- `animateScrollToItem` vs `scrollToItem` : qui scroll reellement ?

### Analyse du flux reel

Le scroll est declenche par **deux acteurs** avec des timings differents :

| Acteur | Methode | Type | Quand |
|--------|---------|------|-------|
| LazyColumn focus interne | `scrollToItem` (interne) | Instantane | Au moment ou le focus change |
| `onFocusEvent` coroutine | `animateScrollToItem(0)` | Anime | Apres recomposition, dans un `coroutineScope.launch` |

### Sequence detaillee

1. **Frame N** : D-pad UP presse. Compose resout le focus vers les boutons hero.
2. **Frame N** : Le `LazyColumn` detecte que l'item focuse (index 0) n'est pas visible. Il effectue un `scrollToItem(0)` interne -- **scroll instantane**. L'ecran saute brutalement au hero.
3. **Frame N+1** : La recomposition se declenche. Le `onFocusEvent` recoit `hasFocus = true`. Le `coroutineScope.launch` est planifie.
4. **Frame N+2** : Le `animateScrollToItem(0)` s'execute. Comme on est DEJA a l'item 0 (grace au scroll instantane du LazyColumn), cette animation est soit un no-op, soit produit un micro-ajustement inutile.

### Verdict

**C'est le scroll instantane interne du LazyColumn qui produit le saut violent, pas le `animateScrollToItem(0)`.** Le `onFocusEvent` arrive trop tard pour etre utile -- le scroll brutal a deja eu lieu.

Le `disableBringIntoView()` neutralise le `BringIntoView` des boutons enfants, mais **ne neutralise PAS le scroll du LazyColumn pour rendre l'item focuse visible**. Ce sont deux mecanismes distincts dans Compose.

---

## Point 5 -- Approches deja testees et pourquoi elles echouent

### Approche A : `smoothScrollToTop(listState)` sur le hero Box

```kotlin
fun Modifier.smoothScrollToTop(listState: LazyListState): Modifier =
    this.bringIntoViewResponder(
        object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect = Rect.Zero
            override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                listState.animateScrollToItem(0)
            }
        },
    )
```

**Pourquoi ca ne marche pas** : Le `BringIntoViewResponder` intercepte la requete `bringIntoView` des enfants, mais le scroll du `LazyColumn` pour rendre l'item visible est un mecanisme **interne** et **separe**. Le `LazyColumn` scroll instantanement pour rendre l'item 0 visible AVANT que le `BringIntoViewResponder` n'ait le temps d'animer quoi que ce soit. L'animation arrive apres le saut brutal.

### Approche B : `disableBringIntoView()` + `onFocusEvent` (approche actuelle)

**Pourquoi ca ne marche pas** : Meme probleme fondamental. Le `disableBringIntoView()` empeche le `bringIntoView` des boutons, et le `onFocusEvent` lance un `animateScrollToItem(0)`. Mais le scroll instantane du `LazyColumn` se produit avant ces deux mecanismes. On est toujours victimes du **scroll interne du LazyColumn lors du changement de focus**.

---

## Diagnostic final

### Cause racine

Le probleme n'est ni le `BringIntoView`, ni le `flingBehavior`, ni le `onFocusEvent`. **La cause racine est que le `LazyColumn` standard effectue un scroll instantane (non anime) quand le focus se deplace vers un item hors ecran.** Ce comportement est code en dur dans `LazyListScrollPosition` de Compose Foundation.

### Pourquoi c'est particulierement violent ici

1. **Distance enorme** : Le saut de l'item 4 (cast a ~660dp du top) a l'item 0 (hero a 0dp) couvre toute la hauteur visible de l'ecran TV (1080px en general).
2. **Pas d'items focusables intermediaires** : Les items 1, 2, 3 n'ont aucun focusable, donc le focus ne peut pas s'arreter en chemin.
3. **Hero de 400dp** : L'item 0 est immense, donc le scroll doit remonter considerablement.

### Pistes de correction (non implementees)

1. **`scrollToItem` avec `scrollOffset`** : Remplacer le `onFocusEvent` par un `focusProperties { up = ... }` qui intercepte la navigation UP, effectue un `animateScrollToItem(0)` d'abord, PUIS deplace le focus manuellement. Ceci eviterait le scroll instantane du LazyColumn.

2. **`TvLazyColumn`** : Utiliser `TvLazyColumn` de `androidx.tv.foundation` qui a un mecanisme de scroll anime integre pour la navigation D-pad (c'est exactement pour ca qu'il existe).

3. **`focusProperties { up = ... }` avec `FocusRequester`** : Intercepter la direction UP depuis le cast, scroller manuellement avec animation, puis envoyer le focus au bouton play. Ceci bypasse completement le scroll automatique du `LazyColumn`.

4. **Ajouter un element focusable intermediaire** : Placer un composable focusable "invisible" entre le cast et le hero (par exemple dans le gradient ou la metadata section) pour permettre au LazyColumn de scroller par etapes plus petites.

5. **`SnapLayoutInfoProvider` / `snapFlingBehavior`** : Ne resoudrait PAS le probleme car le fling n'est pas en cause. C'est le mecanisme de focus scroll du LazyColumn qui est le coupable.

---

## Fichiers concernes

| Fichier | Chemin |
|---------|--------|
| DetailContentScaffold.kt | `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/shared/DetailContentScaffold.kt` |
| DetailUtils.kt | `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/shared/DetailUtils.kt` |
| DetailSections.kt | `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/shared/DetailSections.kt` |
| DetailActions.kt | `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/shared/DetailActions.kt` |
| VegafoXDimensions.kt | `app/src/main/java/org/jellyfin/androidtv/ui/base/theme/VegafoXDimensions.kt` |
| MovieDetailsContent.kt | `app/src/main/java/org/jellyfin/androidtv/ui/itemdetail/v2/content/MovieDetailsContent.kt` |
| libs.versions.toml | `gradle/libs.versions.toml` (compose-foundation 1.10.4) |
