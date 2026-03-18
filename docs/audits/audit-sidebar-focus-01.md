# Audit — Sidebar Focus & Navigation première carte

**Date** : 2026-03-16
**Périmètre** : `ui/home/compose/HomeScreen.kt`, `ui/home/compose/sidebar/PremiumSideBar.kt`, `ui/base/tv/TvRowList.kt`, `ui/browsing/compose/BrowseMediaCard.kt`
**Statut** : Analyse uniquement — aucun fichier modifié

---

## Point 1 — Déclenchement sidebar : mécanisme exact

### Mécanisme identifié : `onKeyEvent` sur le Box contenu

**Fichier** : `HomeScreen.kt:178-188`

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
                sidebarState = SidebarState.COMPACT
                true  // CONSOMME l'événement
            } else {
                false
            }
        },
)
```

### Nature du déclencheur

| Type de déclencheur | Utilisé ? |
|---|---|
| `onKeyEvent` D-pad gauche | **OUI** — c'est le mécanisme principal |
| `onFocusChanged` | Non |
| `focusProperties { left = ... }` | Non — aucun `focusProperties` avec `left` dans tout le home |
| `BringIntoView` | Non |

### Niveau d'interception dans la hiérarchie

```
Row (fullscreen)
├── PremiumSideBar (onPreviewKeyEvent — priority haute)
└── Box (onKeyEvent — priority basse)          ← ICI, ligne 178
    ├── DarkGridNoiseBackground
    ├── HomeHeroBackdrop
    └── Column
        ├── HeroInfoOverlay
        └── StateContainer
            └── TvRowList
                └── LazyRow
                    └── BrowseMediaCard items
```

Le handler est sur le **Box racine du contenu**, PAS sur une carte individuelle ni sur la LazyRow. Il utilise `onKeyEvent` (et non `onPreviewKeyEvent`), ce qui signifie qu'il ne s'exécute qu'**après** que le système de focus Compose ait tenté de gérer l'événement directionnel.

### Chaîne d'événement D-pad gauche (état HIDDEN)

1. `onPreviewKeyEvent` de `PremiumSideBar` → state == HIDDEN → retourne `false` → ne consomme pas
2. `onPreviewKeyEvent` de `BrowseMediaCard` → ne gère que DPAD_CENTER/ENTER → retourne `false`
3. **FocusManager Compose** tente de déplacer le focus à gauche
4. Si FocusManager réussit (ex: carte 2 → carte 1) → événement **consommé** → Box ne le voit jamais
5. Si FocusManager échoue (ex: déjà sur carte 1, rien à gauche) → événement **non consommé** → `onKeyEvent` du Box s'exécute → sidebar passe en COMPACT

### Constat Point 1

Le D-pad gauche sur la première carte d'une row déclenche la sidebar. C'est **voulu par le design** (commentaire `HomeScreen.kt:164` : "appears on D-pad Left"). Mais le déclenchement est **inconditionnel** : il n'y a aucun seuil (nombre d'appuis consécutifs, délai, ou confirmation). Un **seul** appui D-pad gauche suffit.

**Aucune référence à "cosmetic-sidebar-01"** ni à un seuil multi-pression trouvée dans le code.

---

## Point 2 — Impossible d'atteindre la première carte

### Configuration de la LazyRow

**Fichier** : `TvRowList.kt:139-152`

```kotlin
LazyRow(
    state = rememberLazyListState(),
    horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
    contentPadding = PaddingValues(
        start = Tokens.Space.spaceSm,   // = 8.dp
        end = Tokens.Space.spaceSm,     // = 8.dp
        bottom = 8.dp,
    ),
) {
    items(items = row.items, key = itemKey) { item ->
        itemContent(item)
    }
}
```

### Problèmes identifiés

| Élément | Valeur | Impact |
|---|---|---|
| `focusGroup()` | **ABSENT** | Le système de focus ne traite pas la row comme un groupe cohérent |
| `focusRestorer()` | **ABSENT** | Pas de mémoire de la dernière carte focusée au retour |
| `contentPadding.start` | 8dp | Faible — ne masque pas la première carte |
| `focusRequester` sur items | **ABSENT** | Aucun focusRequester attaché aux cartes individuelles |

### Analyse du problème de focus

**Sans `focusGroup()`**, le FocusManager Compose ne sait pas que les cartes appartiennent au même groupe horizontal. Quand l'utilisateur appuie D-pad gauche depuis la carte 2 :

1. Le FocusManager cherche le **focusable le plus proche géométriquement à gauche** dans TOUT l'arbre composable
2. Sans boundary de groupe, il peut :
   - Trouver la carte 1 → OK (cas normal)
   - Échouer à trouver un candidat dans la direction → l'événement bubble vers le Box → sidebar s'ouvre
   - Trouver un candidat dans une autre row (si alignment vertical coïncide) → focus saute dans une row voisine

Le comportement sans `focusGroup()` est **non-déterministe** selon la géométrie des cartes à l'écran.

### Composants vérifiés

| Composant | Fichier | Focus D-pad gauche | Problème |
|---|---|---|---|
| `BrowseMediaCard` | `BrowseMediaCard.kt:113-158` | Ne consomme PAS D-pad gauche (seulement DPAD_CENTER/ENTER) | Aucun |
| `TvFocusCard` | `TvFocusCard.kt:44-97` | Aucun key handler custom | Aucun |
| `MediaPosterCard` | `MediaPosterCard.kt:56-144` | Aucun key handler | Aucun |
| `BrowseHeader` | `BrowseHeader.kt:26-66` | Non-interactif, pas focusable | Aucun |

### Constat Point 2

La première carte EST focusable (via `TvFocusCard` → tv-material3 `Surface`). Le `contentPadding` de 8dp ne la masque pas. Le problème réside dans l'**absence de `focusGroup()`** sur la `LazyRow`, ce qui rend la navigation horizontale intra-row non-fiable sur TV.

---

## Point 3 — Ordre de priorité des événements D-pad gauche

### Chaîne complète de gestion des événements

| Priorité | Handler | Fichier:Ligne | Type modifier | Consomme D-pad gauche quand... |
|---|---|---|---|---|
| 1 (haute) | `PremiumSideBar` | `PremiumSideBar.kt:236` | `onPreviewKeyEvent` | state == COMPACT ou EXPANDED |
| 2 | `BrowseMediaCard` | `BrowseMediaCard.kt:119` | `onPreviewKeyEvent` | JAMAIS (ne gère que DPAD_CENTER/ENTER) |
| 3 | **FocusManager Compose** | (interne) | Focus navigation | Focus peut se déplacer à gauche dans la row |
| 4 (basse) | Content `Box` | `HomeScreen.kt:178` | `onKeyEvent` | state == HIDDEN ET FocusManager n'a pas consommé |

### Scénarios détaillés

**Scénario A — Sidebar HIDDEN, focus sur carte 2+ :**
1. PremiumSideBar.onPreviewKeyEvent → state HIDDEN → `false` (passe)
2. BrowseMediaCard.onPreviewKeyEvent → pas D-pad center → `false` (passe)
3. FocusManager → trouve carte précédente → déplace focus → **CONSOMMÉ**
4. Box.onKeyEvent → jamais atteint

**Scénario B — Sidebar HIDDEN, focus sur carte 1 (première) :**
1. PremiumSideBar.onPreviewKeyEvent → state HIDDEN → `false`
2. BrowseMediaCard.onPreviewKeyEvent → `false`
3. FocusManager → rien à gauche → échec → **NON CONSOMMÉ**
4. Box.onKeyEvent → condition match → sidebar → COMPACT → **CONSOMMÉ**

**Scénario C — Sidebar COMPACT, focus dans sidebar :**
1. PremiumSideBar.onPreviewKeyEvent → state COMPACT → sidebar → EXPANDED → **CONSOMMÉ**
2. Rien d'autre n'exécute

**Scénario D — Sidebar EXPANDED, focus dans sidebar :**
1. PremiumSideBar.onPreviewKeyEvent → state EXPANDED → `true` → **CONSOMMÉ** (max atteint)

### `onPreviewKeyEvent` capture-t-il avant la LazyRow ?

**NON pour le contenu.** Le Box utilise `onKeyEvent` (pas preview). Le seul `onPreviewKeyEvent` pertinent est celui de PremiumSideBar, qui retourne `false` quand state == HIDDEN, laissant le FocusManager agir en premier.

### Seuil multi-pression

**INEXISTANT.** Aucune logique de seuil, compteur d'appuis, ou timer. Le passage HIDDEN → COMPACT se fait en un **seul** appui D-pad gauche non consommé par le FocusManager.

---

## Point 4 — État initial de la sidebar au chargement

### Initialisation

**Fichier** : `HomeScreen.kt:147-150`

```kotlin
var sidebarState by remember { mutableStateOf(SidebarState.HIDDEN) }
val sidebarHomeFocusRequester = remember { FocusRequester() }
var prevSidebarState by remember { mutableStateOf(SidebarState.HIDDEN) }
```

**Valeur initiale hardcodée** : `SidebarState.HIDDEN`

### Enum SidebarState

**Fichier** : `PremiumSideBar.kt:63-73`

```kotlin
enum class SidebarState {
    /** Sidebar invisible (0dp width) — default at launch and after navigation. */
    HIDDEN,
    /** Sidebar shows icons only (72dp width). */
    COMPACT,
    /** Sidebar fully open with icons and labels (220dp width). */
    EXPANDED,
}
```

### Mécanisme de stockage

| Type de persistence | Utilisé ? | Détail |
|---|---|---|
| `remember { mutableStateOf }` | **OUI** | Seul mécanisme — mémoire composable volatile |
| `rememberSaveable` | **NON** | Utilisé uniquement pour `hasRequestedInitialFocus`, pas pour sidebar |
| `SavedStateHandle` | **NON** | Aucune dépendance ViewModel pour la sidebar |
| `SharedPreferences` / `DataStore` | **NON** | Aucune entrée sidebar dans `UserPreferences` |

### Scénarios de reset

| Scénario | Reset à HIDDEN ? | Raison |
|---|---|---|
| Lancement de l'app | **OUI** | Valeur initiale hardcodée |
| Recreation du Fragment | **OUI** | `remember` scoped à HomeScreen |
| Retour arrière vers Home | **OUI** | Nouvelle composition HomeScreen |
| Changement de configuration (rotation) | **OUI** | `remember` ne survit pas (pas `rememberSaveable`) |
| Mort du processus | **OUI** | Aucune persistence |
| Ouverture settings overlay | **NON** | HomeScreen reste composé |

### Race conditions

**AUCUNE détectée.** Raisons :

1. Valeur initiale hardcodée — pas d'init asynchrone
2. Pas de souscription ViewModel/Flow à l'initialisation
3. Pas de `LaunchedEffect` racine modifiant l'état avant le premier rendu
4. L'état n'est pas dérivé d'un état serveur

### Restauration du focus au masquage

**Fichier** : `HomeScreen.kt:152-161`

```kotlin
LaunchedEffect(sidebarState) {
    if (sidebarState == SidebarState.HIDDEN && prevSidebarState != SidebarState.HIDDEN) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) { }
    }
    prevSidebarState = sidebarState
}
```

Le focus est restauré sur la zone de contenu quand la sidebar passe de visible à HIDDEN.

### Constat Point 4

La sidebar démarre **toujours en HIDDEN**. Aucun mécanisme de persistence ne peut la faire démarrer en COMPACT. Pas de race condition possible.

---

## Résumé des constats

| # | Constat | Sévérité | Action suggérée |
|---|---|---|---|
| 1 | Sidebar déclenchée par `onKeyEvent` D-pad gauche sur Box contenu — un seul appui suffit, aucun seuil | **INFO** | Envisager un seuil si le déclenchement est jugé trop facile |
| 2 | `LazyRow` sans `focusGroup()` — navigation horizontale non-fiable sur TV | **MAJEUR** | Ajouter `.focusGroup()` sur la LazyRow dans TvRowList |
| 3 | `LazyRow` sans `focusRestorer()` — pas de mémoire du focus au retour dans la row | **MODÉRÉ** | Ajouter `.focusRestorer()` pour restaurer la position |
| 4 | Aucun `onPreviewKeyEvent` parent ne bloque le D-pad gauche avant le FocusManager | **OK** | Chaîne d'événements correcte |
| 5 | Sidebar toujours HIDDEN au démarrage — pas de persistence parasite | **OK** | Aucun problème détecté |
| 6 | Focus restauré correctement au masquage de la sidebar | **OK** | Mécanisme fonctionnel |

### Cause racine probable du bug "première carte inaccessible"

L'absence de `focusGroup()` sur la `LazyRow` (`TvRowList.kt:139`) est la cause la plus probable. Sans ce modifier, le FocusManager Compose ne traite pas les cartes comme un groupe horizontal cohérent. Résultat : le D-pad gauche depuis la carte 2 peut **échouer à trouver la carte 1** comme cible de focus (le FocusManager cherche géométriquement dans tout l'arbre), et l'événement bubble jusqu'au Box qui déclenche la sidebar.

### Fichiers clés

| Fichier | Lignes clés | Rôle |
|---|---|---|
| `HomeScreen.kt` | 148, 153-161, 178-188 | State holder sidebar, focus restore, trigger HIDDEN→COMPACT |
| `PremiumSideBar.kt` | 63-73, 236-268 | Enum SidebarState, key handler COMPACT→EXPANDED |
| `TvRowList.kt` | 139-152 | LazyRow sans focusGroup — **problème** |
| `BrowseMediaCard.kt` | 113-158 | Key handler cartes (DPAD_CENTER uniquement) |
| `NavItem.kt` | 181-194 | Key handler sidebar items (Enter uniquement) |
| `TvFocusCard.kt` | 44-97 | Base card — pas de key handler custom |
