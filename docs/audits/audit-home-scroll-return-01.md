# Audit — Scroll Home au retour depuis détail

**Date** : 2026-03-17
**Fichiers audités** :
- `ui/home/compose/HomeScreen.kt`
- `ui/home/compose/HomeViewModel.kt`
- `ui/home/compose/HomeComposeFragment.kt`
- `ui/base/tv/TvRowList.kt`
- `ui/base/tv/TvFocusCard.kt`
- `ui/browsing/compose/BrowseMediaCard.kt`
- `ui/browsing/DestinationFragmentView.kt`

---

## Point 1 — Mécanisme de sauvegarde de position scroll

### Constat

**Le `LazyListState` de la `LazyColumn` principale n'est PAS sauvegardé de manière fiable entre les navigations.**

Dans `TvRowList.kt:81` :
```kotlin
val columnState = rememberLazyListState()
```

`rememberLazyListState()` utilise `rememberSaveable` en interne, ce qui devrait persister l'état via le mécanisme `SavedState` d'Android. **Cependant**, la survie de cet état dépend du cycle de vie du composable :

1. **Le `HomeComposeFragment` utilise `DisposeOnViewTreeLifecycleDestroyed`** (`HomeComposeFragment.kt:31`). Quand le fragment est détaché/détruit (navigation vers le détail), la composition est détruite.

2. **`DestinationFragmentView`** gère la navigation par fragment. Lors d'un `navigate()` vers le détail (`DestinationFragmentView.kt:96-97`) :
   ```kotlin
   saveCurrentFragmentState()  // Sauve le SavedState du Fragment actuel
   history.push(entry)         // Empile le nouveau fragment
   ```
   Puis au retour (`goBack()`, ligne 103-122), le fragment Home est réactivé via `activateHistoryEntry()` qui reconstruit le fragment avec `setInitialSavedState(entry.savedState)`.

3. **Le `SavedState` du fragment est bien sauvegardé** via `fragmentManager.saveFragmentInstanceState(fragment)` (ligne 129). Cela inclut le `SavedState` Compose, donc le `LazyListState` sauvé via `rememberSaveable` est théoriquement restauré.

### Verdict

Le `LazyListState` est sauvegardé via le chaîne `rememberLazyListState()` → `rememberSaveable` → Fragment `SavedState` → `DestinationFragmentView.saveCurrentFragmentState()`. **Le mécanisme est correct en théorie**, mais la restauration effective dépend du timing de reconstruction de la composition.

**Aucun hoisting dans le ViewModel ni dans un `SavedStateHandle`** — tout repose sur le `SavedState` Compose standard.

---

## Point 2 — Pourquoi la première rangée ne descend pas

### Constat

La première rangée ne produit pas de scroll parasite au retour car :

1. **La `LazyColumn` dans `TvRowList` occupe `weight(0.54f)`** de l'écran (`HomeScreen.kt:219`). Sur un écran 1080p, cela représente ~583px.

2. **La première rangée est dans la zone visible initiale** : avec un `contentPadding.top = spaceSm` (~8dp ≈ 12px), la première rangée est toujours visible au pixel 0 du viewport de la `LazyColumn`. Le titre + la `LazyRow` + le spacer font environ 148dp (~222px), bien dans les 583px disponibles.

3. **Quand le focus est restauré sur un item de la première rangée**, `BringIntoView` détecte que l'item est déjà visible dans le viewport → **aucun scroll ne se déclenche**.

4. **Le scroll se déclenche à partir de la 3ème ou 4ème rangée** (selon la hauteur des cartes). Avec des cartes de 124dp + titre 24dp + spacer 2dp + padding 4dp ≈ 154dp par rangée :
   - Rangée 1 : ~12dp à ~166dp → **visible** (0 scroll)
   - Rangée 2 : ~166dp à ~320dp → **visible** (0 scroll)
   - Rangée 3 : ~320dp à ~474dp → **partiellement visible** (~389dp de viewport)
   - Rangée 4+ : **hors écran** → scroll déclenché par `BringIntoView`

### Verdict

Le comportement est cohérent : les 2 premières rangées sont toujours dans le viewport initial, donc le focus restore ne déclenche pas de scroll. Le scroll parasite n'apparaît qu'à partir de la rangée 3+.

---

## Point 3 — Mécanisme de restauration du focus

### Constat

La restauration du focus utilise un mécanisme explicite en 3 couches :

#### Couche 1 : Sauvegarde de l'ID focusé (`HomeViewModel.kt:86-88`)
```kotlin
private val _lastFocusedItemId = MutableStateFlow<java.util.UUID?>(null)
```
Mis à jour dans `setFocusedItem()` (ligne 124) à chaque changement de focus. Survit à la navigation car le ViewModel vit tant que le scope Koin est actif (pas lié au fragment).

#### Couche 2 : Détection du retour via Lifecycle (`HomeScreen.kt:115-134`)
```kotlin
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            ON_RESUME -> {
                if (hasRequestedInitialFocus) {
                    needsRestoreFocus = true  // Déclenche la restauration
                }
            }
            ...
        }
    }
}
```
Quand le fragment Home repasse en `RESUMED` (retour depuis détail), `needsRestoreFocus` est mis à `true`.

#### Couche 3 : Exécution du focus restore (`HomeScreen.kt:136-145`)
```kotlin
LaunchedEffect(needsRestoreFocus) {
    if (needsRestoreFocus && focusTargetId != null) {
        delay(200)  // Attend que la composition soit stable
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
        needsRestoreFocus = false
    }
}
```

#### Attachement du FocusRequester (`HomeScreen.kt:284-289`)
```kotlin
initialFocusRequester = if (item.id == focusTargetId) {
    focusRequester  // Un seul FocusRequester, attaché à la carte ciblée
} else {
    null
}
```
Le `FocusRequester` est attaché dans `BrowseMediaCard.kt:125-126` via `Modifier.focusRequester(initialFocusRequester)`.

### Problème identifié

**Un seul `FocusRequester` est partagé** et attaché dynamiquement à la carte dont l'ID correspond à `focusTargetId`. Cela fonctionne **SI ET SEULEMENT SI** la carte cible est déjà composée au moment du `requestFocus()`.

Si la carte est dans une rangée hors écran (rangée 3+), elle n'est pas encore composée par la `LazyColumn` → le `FocusRequester` n'est attaché à rien → `requestFocus()` échoue silencieusement (catch vide). **Le focus se perd.**

---

## Point 4 — Interaction focus + BringIntoView

### Constat

**Le scroll est déclenché INCONDITIONNELLEMENT par Compose quand `requestFocus()` est appelé sur un élément.**

Le mécanisme est le suivant :

1. `focusRequester.requestFocus()` est appelé (`HomeScreen.kt:140`)
2. L'élément ciblé (la `Surface` dans `TvFocusCard.kt:70`) reçoit le focus
3. Compose déclenche automatiquement `BringIntoViewRequester.bringIntoView()` sur l'ancêtre scrollable (`LazyColumn`)
4. La `LazyColumn` scrolle pour rendre l'item visible

**Il n'y a AUCUNE vérification** de la visibilité actuelle de l'item avant le scroll. Compose le fait automatiquement via son système `BringIntoView` intégré aux `LazyColumn`/`LazyRow`.

### Comportement par cas

| Cas | Rangée | Carte visible ? | Scroll déclenché ? | Résultat |
|-----|--------|-----------------|---------------------|----------|
| 1 | 1-2 | Oui | Non* | OK — pas de scroll parasite |
| 2 | 3+ | Non | Oui | OK — scroll justifié pour montrer la carte |
| 3 | 3+ | Partiellement | Oui | Problématique — scroll "ajuste" même si la rangée est déjà partiellement visible |

*Compose ne scrolle pas si l'item est déjà entièrement visible dans le viewport.

### Subtilité importante

Le `BringIntoView` dans `LazyColumn` est en réalité **géré au niveau du `LazyColumn` lui-même**, pas au niveau de Compose Foundation. Le `LazyColumn` a un comportement spécifique : il scrolle pour rendre **entièrement visible** l'item qui reçoit le focus. Si l'item n'est que partiellement visible (cas 3), le scroll s'ajuste, ce qui peut causer un léger mouvement perçu comme parasite.

**Aucun mécanisme ne désactive ou conditionne ce comportement** dans le code actuel.

---

## Point 5 — État du LazyListState après navigation

### Constat

Dans `TvRowList.kt:81` :
```kotlin
val columnState = rememberLazyListState()
```

#### Cycle de vie lors de la navigation Home → Détail → Home :

1. **Home → Détail** :
   - `DestinationFragmentView.saveCurrentFragmentState()` est appelé (ligne 96)
   - Le `SavedState` du fragment Home est capturé via `fragmentManager.saveFragmentInstanceState()`
   - Cela inclut le `SavedState` Compose, qui inclut le `LazyListState` (via `rememberSaveable`)
   - Le fragment Home est **détaché** (pas détruit) via `transaction.detach()` (ligne 157)
   - La composition est détruite (`DisposeOnViewTreeLifecycleDestroyed`)

2. **Détail → Home (goBack)** :
   - Le fragment détail est **supprimé** (`transaction.remove()`, ligne 115)
   - Le fragment Home est **réattaché** via `transaction.attach()` (ligne 161) ou **recréé** avec `setInitialSavedState(entry.savedState)` (ligne 143)
   - La composition est recréée depuis zéro
   - `rememberLazyListState()` reconstruit le `LazyListState` depuis le `SavedState` restauré

#### Risque de réinitialisation à zéro

**Le `LazyListState` peut être réinitialisé à zéro si** :
- Le fragment est recréé (pas réattaché) ET le `SavedState` ne contient pas le state Compose → **peu probable** car `saveCurrentFragmentState()` est toujours appelé avant la navigation
- La composition se reconstruit avant que les données (`rows`) ne soient disponibles → le `LazyColumn` se compose avec une liste vide, puis recompose avec les données. **Le `LazyListState` peut perdre sa position** si les clés d'items changent (mais les clés utilisent `row.key` qui est stable)

#### Facteur atténuant

Le `ViewModel` survit à la navigation (scope Koin), donc les `rows` sont immédiatement disponibles via le cache en mémoire. La `LazyColumn` devrait se recomposer avec les mêmes données et les mêmes clés, permettant au `LazyListState` de restaurer sa position.

### Verdict

**Le `LazyListState` persiste correctement dans le cas nominal** grâce à la chaîne `rememberSaveable` → `Fragment.SavedState` → `DestinationFragmentView`. Cependant, **il y a un risque de perte de position** si la recomposition arrive avant que les données du ViewModel ne soient disponibles (flash de liste vide).

---

## Résumé des vulnérabilités

| # | Problème | Sévérité | Fichier |
|---|----------|----------|---------|
| 1 | `FocusRequester` ne peut pas restaurer le focus sur une carte hors-viewport (non composée par `LazyColumn`) | **Haute** | `HomeScreen.kt:284-289` |
| 2 | Scroll `BringIntoView` inconditionnel — pas de vérification de visibilité avant scroll | **Moyenne** | Compose framework (pas de code custom) |
| 3 | `delay(200)` arbitraire pour attendre la stabilisation de la composition — fragile | **Moyenne** | `HomeScreen.kt:138` |
| 4 | Le `catch` vide masque les échecs de `requestFocus()` — pas de fallback | **Moyenne** | `HomeScreen.kt:139-141` |
| 5 | Pas de `focusRestorer()` sur la `LazyColumn` principale (seulement sur les `LazyRow` internes) | **Basse** | `TvRowList.kt:85-89` vs `141` |
| 6 | `entrancePlayed` persiste via `rememberSaveable` — l'animation d'entrée ne rejoue pas au retour (intentionnel mais pourrait masquer un flash) | **Info** | `TvRowList.kt:82` |

---

## Corrections appliquées

Toutes les vulnérabilités ont été corrigées et vérifiées par tests ADB avec captures d'écran.

### Fix 1 — Stockage de l'index de rangée (`HomeViewModel.kt`)
- Ajout de `_lastFocusedRowIndex: MutableStateFlow<Int>` pour suivre la rangée focusée.
- `setFocusedItem()` accepte un `rowIndex` et le stocke.
- `itemRowIndex` map pré-calculé dans `HomeScreen.kt` pour mapper chaque item à sa rangée.

### Fix 2 — Sauvegarde/restauration exacte de la position de scroll (`HomeViewModel.kt` + `HomeScreen.kt`)
- `saveScrollPosition(firstVisibleItemIndex, firstVisibleItemScrollOffset)` sauvegarde la position exacte dans le ViewModel.
- Le lifecycle observer `ON_PAUSE` appelle `saveScrollPosition()` avant la transition.
- Au retour, `scrollToItem(savedIdx, savedOff)` restaure la position exacte AVANT `requestFocus()`.
- Remplacement du `delay(200)` par `snapshotFlow { visibleItemsInfo.isNotEmpty() }.first { it }` pour attendre le layout.

### Fix 3 — Contre-mesure BringIntoView (`HomeScreen.kt`)
- Après `requestFocus()`, ré-application de la position de scroll sauvée pour contrer l'ajustement automatique de `BringIntoView`.
- Cette ré-application n'est faite que si la rangée cible est déjà visible (évite de pousser l'item focusé hors écran).

### Fix 4 — `focusRestorer()` sur LazyColumn (`TvRowList.kt`)
- Ajout de `.focusRestorer()` sur le modifier de la `LazyColumn` principale.
- Paramètre `columnState: LazyListState` optionnel ajouté à `TvRowList` pour permettre au parent de contrôler le scroll.

### Fix 5 — Gel du focus tracking pendant les transitions (`HomeViewModel.kt`)
**Cause racine découverte par tests** : pendant la transition de fragment (Home → Détail), Compose déplace le focus dans la `LazyColumn`, ce qui appelle `setFocusedItem()` avec un item différent. Cela corrompait `lastFocusedItemId`.
- Ajout de `focusTrackingFrozen: Boolean` dans le ViewModel.
- `freezeFocusTracking()` appelé dans `ON_PAUSE` — empêche la mise à jour de `lastFocusedItemId`/`lastFocusedRowIndex`.
- `unfreezeFocusTracking()` appelé dans `ON_RESUME` — reprend le suivi normal.

### Fix 6 — Logging des échecs (`HomeScreen.kt`)
- Le `catch {}` vide remplacé par `Timber.w(e, "Focus restore failed ...")`.
