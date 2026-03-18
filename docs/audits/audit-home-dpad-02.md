# Audit — Navigation D-pad gauche cassée sur HomeScreen

**Date** : 2026-03-16
**Fichiers audités** :
- `ui/base/tv/TvRowList.kt`
- `ui/home/compose/HomeScreen.kt`
- `ui/home/compose/sidebar/PremiumSideBar.kt`
- `ui/home/compose/sidebar/NavItem.kt`
- `ui/home/compose/HomeHeroBackdrop.kt`
- `ui/browsing/compose/BrowseMediaCard.kt`
- `ui/base/tv/TvFocusCard.kt`
- `ui/base/state/StateContainer.kt`

---

## Point 1 — État actuel de TvRowList

**Fichier** : `ui/base/tv/TvRowList.kt`

### Modificateurs sur la LazyRow (ligne 140–154)

```kotlin
LazyRow(
    modifier = Modifier.focusRestorer(),
    state = rememberLazyListState(),
    horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
    contentPadding = PaddingValues(
        start = Tokens.Space.spaceSm,
        end = Tokens.Space.spaceSm,
        bottom = 8.dp,
    ),
)
```

**Constat** :
- `focusGroup()` a bien été supprimé — **absent** du code.
- `focusRestorer()` est le seul modificateur sur la `LazyRow` — **correct**.
- **Aucun** `onKeyEvent`, `onPreviewKeyEvent`, ni `focusProperties` sur la `LazyRow` ni sur le `LazyColumn` parent.
- **Aucun** handler clavier dans tout le fichier `TvRowList.kt`.

**Verdict TvRowList** : Le composant est propre. Il ne consomme aucun événement clavier. Le D-pad gauche passe librement au travers.

---

## Point 2 — Mécanisme double appui sidebar dans HomeScreen

**Fichier** : `ui/home/compose/HomeScreen.kt`, lignes 195–216

### Code exact du handler

```kotlin
Box(
    modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown &&
                event.key == Key.DirectionLeft &&
                sidebarState == SidebarState.HIDDEN
            ) {
                leftPressCount++
                if (leftPressCount >= 2) {
                    leftPressResetJob?.cancel()
                    leftPressCount = 0
                    sidebarState = SidebarState.COMPACT
                } else {
                    leftPressResetJob?.cancel()
                    leftPressResetJob = sidebarScope.launch {
                        delay(500)
                        leftPressCount = 0
                    }
                }
                true  // ← TOUJOURS true pour TOUT D-pad gauche
            } else {
                false
            }
        },
)
```

### Analyse du problème

Le handler se situe sur le `Box` qui contient **tout le contenu principal** (backdrop + rows + cartes). C'est un `onKeyEvent` (phase de remontée/bubble), donc il s'exécute **après** que les enfants aient eu la possibilité de traiter l'événement.

**Le bug critique est à la ligne 212** : le handler retourne `true` (consomme l'événement) pour **TOUT** D-pad gauche KeyDown, **sans vérifier si le focus est sur la première carte de la row**.

Le mécanisme vérifie uniquement :
1. `event.type == KeyEventType.KeyDown` ✓
2. `event.key == Key.DirectionLeft` ✓
3. `sidebarState == SidebarState.HIDDEN` ✓

**Il ne vérifie PAS** :
- Si le focus est actuellement sur la première carte d'une row (position 0)
- Si la `LazyRow` peut encore scroller vers la gauche
- Si l'enfant a déjà consommé l'événement (impossible avec `onKeyEvent` — les enfants passent d'abord mais s'ils retournent `false`, ce handler reçoit l'événement)

**Conséquence** : Quand l'utilisateur est sur la carte #3 d'une row et appuie gauche, le système de focus Compose déplace bien le focus sur la carte #2 (le composant `Surface` dans `TvFocusCard` gère ça), **MAIS** l'événement continue de remonter jusqu'au `Box` parent. Le handler le capture, incrémente `leftPressCount`, et retourne `true`.

**Scénario de blocage** :
1. Focus sur carte #3 → appui gauche → focus va sur carte #2 ✓ **mais** `leftPressCount = 1`
2. Appui gauche < 500ms → focus va sur carte #1 ✓ **mais** `leftPressCount = 2` → **sidebar s'ouvre**

L'utilisateur ne peut jamais naviguer rapidement vers la gauche dans une row sans déclencher la sidebar.

---

## Point 3 — Chaîne complète des handlers D-pad gauche

### Hiérarchie (de la racine vers les feuilles)

```
HomeScreen (Row)
├── PremiumSideBar (Row)
│   └── onPreviewKeyEvent (lignes 236-268)          [1]
│       ├── Left + COMPACT → EXPANDED, return true
│       ├── Left + EXPANDED → return true (consume)
│       ├── Left + HIDDEN → return false
│       ├── Back + visible → HIDDEN, return true
│       ├── Right + visible → HIDDEN, return false
│       └── else → return false
│
│   └── NavItem (Box)
│       └── onKeyEvent (lignes 181-189)              [2]
│           ├── Enter/DpadCenter KeyUp → onSelect(), return true
│           └── else → return false
│
└── Box (contenu principal)
    └── onKeyEvent (lignes 195-216)                  [3] ★ LE BUG
        ├── KeyDown + Left + HIDDEN → incrémente compteur, return TRUE
        └── else → return false
    │
    ├── DarkGridNoiseBackground — aucun handler
    ├── HomeHeroBackdrop — aucun handler
    └── Column
        ├── HeroInfoOverlay — aucun handler
        └── StateContainer — aucun handler
            └── TvRowList
                └── LazyColumn — aucun handler
                    └── LazyRow (focusRestorer()) — aucun handler
                        └── BrowseMediaCard
                            └── TvFocusCard (Surface)
                                └── onPreviewKeyEvent (lignes 119-138)  [4]
                                    ├── DpadCenter/Enter LongPress → onPlayClick, return true
                                    ├── DpadCenter/Enter UP after longPress → return true
                                    └── else → return false
```

### Ordre de traitement pour un D-pad gauche

L'événement D-pad gauche traverse les handlers dans cet ordre :

1. **Phase descendante (preview)** :
   - `PremiumSideBar.onPreviewKeyEvent` [1] : quand sidebar est HIDDEN → retourne `false`, laisse passer.
   - `BrowseMediaCard.onPreviewKeyEvent` [4] : ne concerne que Enter/DpadCenter → retourne `false`.

2. **Traitement par le focus system** : Compose TV déplace le focus à la carte précédente (si elle existe).

3. **Phase remontée (bubble)** :
   - `NavItem.onKeyEvent` [2] : ne concerne que Enter/DpadCenter → retourne `false`.
   - **`Box.onKeyEvent` [3]** ★ : intercepte TOUS les D-pad gauche → retourne `true`, **même si le focus a bougé normalement entre cartes**.

### Résumé par handler

| # | Composable | Type | Intercepte Left ? | Retourne true ? |
|---|-----------|------|-------------------|----------------|
| [1] | PremiumSideBar | `onPreviewKeyEvent` | Oui si COMPACT/EXPANDED | Oui |
| [2] | NavItem | `onKeyEvent` | Non | Non |
| [3] | Box contenu | `onKeyEvent` | **Oui, TOUJOURS quand HIDDEN** | **Oui, TOUJOURS** |
| [4] | BrowseMediaCard | `onPreviewKeyEvent` | Non (Enter only) | Non |

---

## Point 4 — Verdict

### Le coupable

**HomeScreen.kt, lignes 195–216** — le `onKeyEvent` sur le `Box` contenu principal.

**Ligne précise** : **ligne 212** — `true` est retourné inconditionnellement pour tout D-pad gauche quand `sidebarState == HIDDEN`.

### Pourquoi c'est cassé

1. Le handler ne distingue pas "le focus est sur la première carte et ne peut plus aller à gauche" de "le focus est au milieu d'une row et peut naviguer normalement".

2. `onKeyEvent` (phase bubble) s'exécute **après** que le focus system ait déjà déplacé le focus. Donc le déplacement de focus fonctionne, mais le handler consomme quand même l'événement et incrémente le compteur sidebar.

3. Deux appuis gauche rapides (< 500ms) dans une row de 10 cartes → sidebar s'ouvre alors que l'utilisateur naviguait juste dans sa row.

### Ce qui devrait être fait (pour info)

Le handler devrait vérifier si le focus est sur le premier élément focusable de la row **avant** de compter les appuis. Il ne devrait retourner `true` que lorsque la navigation gauche est impossible (première carte de la row, pas de scroll restant). Sinon, il doit retourner `false` pour laisser le focus system naviguer normalement.

Options possibles :
- Utiliser `onPreviewKeyEvent` au lieu de `onKeyEvent`, vérifier si le focus enfant peut encore aller à gauche, et ne consommer que s'il ne peut pas.
- Tracker l'index du focus dans la row et ne compter que quand l'index est 0.
- Détecter le scroll position de la `LazyRow` et ne compter que quand `firstVisibleItemIndex == 0` et `firstVisibleItemScrollOffset == 0`.
