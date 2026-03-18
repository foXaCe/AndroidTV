# Audit ciblé — Rebond film + Navigation série + Code mort

**Date** : 2026-03-15

---

## Section 1 — Causes du rebond en bas de page Film

### 1.1 — Overscroll

| Vérification | Résultat |
|-------------|----------|
| `overscrollEffect` sur LazyColumn | **Non** — aucun paramètre `overscrollEffect` passé au `LazyColumn` (MovieDetailsContent.kt:157) |
| `rememberOverscrollEffect` | **Non** — absent de tous les fichiers détail |
| Overscroll global thème | **Désactivé** — `JellyfinTheme.kt:20` : `LocalOverscrollFactory provides null`. L'overscroll natif Android est neutralisé. |

**L'overscroll natif n'est PAS la cause du rebond.**

### 1.2 — Espace invisible après le Spacer bottom

| Vérification | Résultat |
|-------------|----------|
| Spacer bottom | `MovieDetailsContent.kt:539-547` — `Spacer(Modifier.height(80.dp).fillMaxWidth().background(BackgroundDeep))`. C'est le **dernier item** du LazyColumn, rien après. |
| `contentPadding` du LazyColumn | **Non spécifié** → `PaddingValues(0.dp)` par défaut. Aucun padding bottom ajouté. |
| WindowInsets / imePadding / navigationBarsPadding | **Absent** — aucun `WindowInsets`, `imePadding`, `navigationBarsPadding`, `systemBarsPadding` dans aucun fichier de `ui/itemdetail/`. |
| `fitSystemWindows` | **Absent** du Fragment hôte Compose. |
| Composable invisible après Spacer | **Non** — le Spacer est bien le dernier `item {}` du LazyColumn. |

**Aucun espace fantôme sous le Spacer.**

### 1.3 — NestedScroll / vélocité renvoyée

| Vérification | Résultat |
|-------------|----------|
| `nestedScroll` modifier | **Absent** — aucune occurrence dans les fichiers détail |
| NestedScrollConnection parent | **Non** — le `Box(fillMaxSize)` parent n'a pas de `nestedScroll` |

### 1.4 — CAUSE IDENTIFIÉE : Le `LaunchedEffect(targetScrollIndex)` avec `animateScrollBy` négatif

**MovieDetailsContent.kt:143-152** :
```kotlin
val targetScrollIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
LaunchedEffect(targetScrollIndex) {
    if (targetScrollIndex > 0 && listState.firstVisibleItemScrollOffset > 0) {
        listState.animateScrollBy(
            value = -listState.firstVisibleItemScrollOffset.toFloat(),
            animationSpec = tween(280, easing = EaseOutCubic),
        )
    }
}
```

**Mécanisme du rebond** :

1. L'utilisateur D-pad vers le bas, le focus atteint le dernier élément focusable (dernière carte Similar ou dernier cast)
2. Le bring-into-view scroll le LazyColumn pour montrer l'item contenant cette carte
3. Le `firstVisibleItemIndex` change → le `LaunchedEffect` se déclenche
4. `firstVisibleItemScrollOffset > 0` est vrai (l'item est partiellement scrollé)
5. `animateScrollBy(-offset)` scroll **vers le haut** de `offset` pixels avec une animation de 280ms
6. Ce scroll vers le haut ramène l'item au bord supérieur du viewport
7. **Visuellement** : le contenu descend (bring-into-view), puis remonte (animateScrollBy négatif) → c'est le **rebond**

**Ce LaunchedEffect est la cause directe du rebond.** Il crée systématiquement un mouvement descendant suivi d'un mouvement ascendant à chaque changement d'item visible. Le pattern descend-remonte est perçu comme un rebond élastique.

**Aggravation en fin de page** : quand le dernier item est le Spacer de 80dp, le LazyColumn atteint la fin du contenu. Le bring-into-view s'arrête au maximum scroll possible, puis le `animateScrollBy(-offset)` tire le contenu vers le haut. Comme il n'y a plus de contenu en dessous, le LazyColumn revient en position de fin → le rebond est encore plus visible.

### 1.5 — Résumé

| # | Cause | Sévérité | Fichier:ligne |
|---|-------|----------|---------------|
| 1 | **`animateScrollBy` négatif dans LaunchedEffect(targetScrollIndex)** | **CRITIQUE** — cause unique et directe du rebond | MovieDetailsContent:143-152, SeriesDetailsContent:96-105, SeasonDetailsContent:97-106 |

---

## Section 2 — Causes du blocage navigation D-pad vers le bas sur Série

### 2.1 — Items LazyColumn sous le hero : focusable/pointerInput

| Vérification | Résultat |
|-------------|----------|
| `focusable(false)` sur items sous hero | **Non** — aucun `focusable(false)` explicite sur les Box des sections |
| `pointerInput` capturant événements | **Non** — aucun `pointerInput` dans SeriesDetailsContent ni DetailSections |

### 2.2 — LazyRow internes : capture D-pad vertical

| Section | Composable LazyRow | Capture D-pad vertical |
|---------|-------------------|----------------------|
| Next Up | `DetailSectionWithCards` → `LazyRow` | **Non** — LazyRow est horizontal, ne capture que D-pad gauche/droite. Les événements D-pad vertical propagent au parent. |
| Seasons | `DetailSeasonsSection` → `LazyRow` | **Non** — même comportement, LazyRow horizontal |
| Cast | `DetailCastSection` → `LazyRow` | **Non** — idem |
| Similar | `DetailSectionWithCards` → `LazyRow` | **Non** — idem |

**Les LazyRow ne capturent PAS le D-pad vertical.**

### 2.3 — onKeyEvent / onPreviewKeyEvent

| Fichier | Occurrence | Bloque D-pad bas ? |
|---------|-----------|-------------------|
| SeriesDetailsContent.kt | **Aucun** `onKeyEvent` ni `onPreviewKeyEvent` | Non |
| DetailActions.kt | **Aucun** | Non |
| DetailSections.kt | **Aucun** | Non |
| ItemDetailsComponents.kt:1079 | `onKeyEvent` sur `MusicTrackRow` — capture D-pad left/right pour réordonner | **Non** — pas utilisé dans Series |
| ItemDetailsComponents.kt:1709 | `onKeyEvent` sur `CinemaActionChip` — capture Enter/Center uniquement | **Non** |

**Aucun onKeyEvent ne consomme le D-pad bas.**

### 2.4 — HorizontalPager / nestedScroll

| Vérification | Résultat |
|-------------|----------|
| `HorizontalPager` dans Series | **Non** — aucun Pager dans SeriesDetailsContent |
| `nestedScroll` dans sections | **Non** — absent de tous les fichiers |

### 2.5 — focusProperties bloquant la sortie vers le bas

| Fichier | Composable | focusProperties | Bloque bas ? |
|---------|-----------|----------------|-------------|
| DetailActions.kt:100 | Row (boutons primaires) | `right = FocusRequester.Cancel` | **Non** — seul `right` est bloqué, `down` n'est pas affecté |
| MovieDetailsContent.kt:268 | Row (boutons primaires) | `right = FocusRequester.Cancel` | **Non** — idem |
| SeasonDetailsContent.kt:201 | Row (boutons primaires) | `right = FocusRequester.Cancel` | **Non** — idem |

**Aucun `focusProperties { down = ... }` n'est défini nulle part. Le D-pad bas n'est PAS bloqué par des focusProperties.**

### 2.6 — CAUSE IDENTIFIÉE : Le `animateScrollBy` négatif empêche le scroll de progresser

**SeriesDetailsContent.kt:96-105** — même `LaunchedEffect(targetScrollIndex)` que pour Movie :

```kotlin
LaunchedEffect(targetScrollIndex) {
    if (targetScrollIndex > 0 && listState.firstVisibleItemScrollOffset > 0) {
        listState.animateScrollBy(
            value = -listState.firstVisibleItemScrollOffset.toFloat(),
            ...
        )
    }
}
```

**Mécanisme du blocage** :

1. D-pad bas depuis le dernier bouton du hero (ou les boutons secondaires via `DetailActionButtonsRow`)
2. Le focus cherche le prochain élément focusable vers le bas
3. Les items `SectionHeader` ne sont PAS focusables (simples Text)
4. Le prochain élément focusable est la première carte de la première section visible (Next Up, Seasons, Cast, ou Similar)
5. Le bring-into-view scroll le LazyColumn pour montrer cette carte
6. **Immédiatement**, le `LaunchedEffect(targetScrollIndex)` se déclenche car `firstVisibleItemIndex` a changé
7. `animateScrollBy(-offset)` scroll vers le HAUT, ramenant l'item visible précédent en haut
8. Ce scroll vers le haut peut ramener l'item du hero en position visible, repoussant les sections hors écran
9. **Le focus reste sur la carte de la section (qui est maintenant hors écran), mais le LazyColumn est scrollé en arrière**
10. Au prochain D-pad bas, le même cycle se répète → **la navigation semble bloquée** car le scroll est systématiquement annulé

**Le `animateScrollBy` négatif est la cause unique du blocage.** Il annule le scroll de bring-into-view à chaque transition d'item, empêchant la progression vers le bas.

### 2.7 — Confirmation

La navigation D-pad vers le bas n'est PAS bloquée par :
- ~~focusable(false)~~ → absent
- ~~pointerInput~~ → absent
- ~~LazyRow capture~~ → axes orthogonaux
- ~~onKeyEvent~~ → absent dans Series
- ~~HorizontalPager~~ → absent
- ~~focusProperties down~~ → absent
- ~~nestedScroll~~ → absent

Elle est bloquée par :
- **Le `LaunchedEffect(targetScrollIndex)` qui scroll en arrière après chaque progression** (SeriesDetailsContent:96-105)

---

## Section 3 — Code mort et inutile — Inventaire fichier par fichier

### 3.1 — ItemDetailScreen.kt

| Ligne | Type | Description |
|-------|------|-------------|
| 76 | **Paramètre gaspillé** | `contentFocusRequester = remember { FocusRequester() }` — créé et passé à tous les content composables, mais **jamais utilisé dans son corps** pour aucun d'entre eux (Movie, Series, Season l'ignorent tous). Le paramètre existe dans les signatures mais n'est rattaché à aucun composable. |

### 3.2 — MovieDetailsContent.kt

| Ligne | Type | Description |
|-------|------|-------------|
| 80-84 | **Commentaire doc obsolète** | Le KDoc mentionne "580dp hero zone" mais la hauteur est maintenant 360dp (changée dans VegafoXDimensions). |
| 89 | **Paramètre inutilisé** | `contentFocusRequester: FocusRequester` — déclaré dans la signature mais jamais utilisé dans le corps du composable. |
| 97 | **Variable inutilisée** | `val density = LocalDensity.current` — utilisé uniquement pour `slideOffsetPx` à la ligne 154. Pourrait être inline mais n'est pas mort. *(conservation acceptable)* |
| 143-152 | **Code nuisible** | Le `LaunchedEffect(targetScrollIndex)` avec `animateScrollBy(-offset)` cause le rebond. À supprimer. |
| 21 | **Import à supprimer** | `import androidx.compose.foundation.gestures.animateScrollBy` — inutile après suppression du LaunchedEffect. |
| 26 | **Import à supprimer** | `import androidx.compose.runtime.derivedStateOf` — inutile après suppression du LaunchedEffect. |
| 365 | **Condition toujours vraie** | `item.type != BaseItemKind.PERSON` — MovieDetailsContent n'est jamais appelé pour PERSON (dispatch dans ItemDetailScreen.kt:118). Ce test est toujours vrai ici. |

### 3.3 — SeriesDetailsContent.kt

| Ligne | Type | Description |
|-------|------|-------------|
| 69 | **Paramètre inutilisé** | `contentFocusRequester: FocusRequester` — jamais utilisé dans le corps. |
| 96-105 | **Code nuisible** | Le `LaunchedEffect(targetScrollIndex)` — cause unique du blocage navigation. À supprimer. |
| 18 | **Import à supprimer** | `import androidx.compose.foundation.gestures.animateScrollBy` |
| 23 | **Import à supprimer** | `import androidx.compose.runtime.derivedStateOf` |

### 3.4 — SeasonDetailsContent.kt

| Ligne | Type | Description |
|-------|------|-------------|
| 69 | **Paramètre inutilisé** | `contentFocusRequester: FocusRequester` — jamais utilisé dans le corps. |
| 70 | **Paramètre inutilisé** | `showBackdrop: Boolean` — déclaré mais jamais lu dans le corps. |
| 72 | **Paramètre inutilisé** | `blurAmount: Int` — déclaré mais jamais lu dans le corps. |
| 97-106 | **Code nuisible** | Le `LaunchedEffect(targetScrollIndex)` — même problème de rebond. À supprimer. |
| 20 | **Import à supprimer** | `import androidx.compose.foundation.gestures.animateScrollBy` |
| 25 | **Import à supprimer** | `import androidx.compose.runtime.derivedStateOf` |

### 3.5 — DetailActions.kt

| Ligne | Type | Description |
|-------|------|-------------|
| — | **Duplication logique** | Les boutons d'action + dialogs Audio/Subtitle/Version sont dupliqués entre `DetailActions.kt` (lignes 95-377) et `MovieDetailsContent.kt` (lignes 265-672). MovieDetailsContent inline ses propres boutons et dialogs au lieu d'utiliser `DetailActionButtonsRow`. Les deux implémentations divergent légèrement : DetailActions a Shuffle, Instant Mix, Playlist, GoToSeries que Movie n'a pas ; Movie manque ces boutons. |

### 3.6 — DetailSections.kt

| Ligne | Type | Description |
|-------|------|-------------|
| — | **Aucun code mort** | Tous les composables et imports sont utilisés. |

### 3.7 — DetailUtils.kt

| Ligne | Type | Description |
|-------|------|-------------|
| — | **Aucun code mort** | `translateGenreUpper` utilisé dans `HomeHeroBackdrop.kt`. `getEndsAt` utilisé dans `DetailInfoRow.kt`. `getLogoUrl` utilisé dans `MusicDetailsContent.kt`. Toutes les fonctions sont actives. |

### 3.8 — Résumé du code à supprimer

| Priorité | Fichier | Lignes | Action |
|----------|---------|--------|--------|
| **CRITIQUE** | MovieDetailsContent.kt | 143-152 | Supprimer le `LaunchedEffect(targetScrollIndex)` + `derivedStateOf` + `animateScrollBy` |
| **CRITIQUE** | SeriesDetailsContent.kt | 96-105 | Supprimer le même bloc |
| **CRITIQUE** | SeasonDetailsContent.kt | 97-106 | Supprimer le même bloc |
| **CRITIQUE** | MovieDetailsContent.kt | 21, 26 | Supprimer imports `animateScrollBy`, `derivedStateOf` |
| **CRITIQUE** | SeriesDetailsContent.kt | 18, 23 | Supprimer imports `animateScrollBy`, `derivedStateOf` |
| **CRITIQUE** | SeasonDetailsContent.kt | 20, 25 | Supprimer imports `animateScrollBy`, `derivedStateOf` |
| MODÉRÉ | MovieDetailsContent.kt | 80-84 | Corriger le KDoc "580dp" → "360dp" |
| MINEUR | MovieDetailsContent.kt | 89 | Paramètre `contentFocusRequester` inutilisé (signature publique, risqué à supprimer) |
| MINEUR | SeriesDetailsContent.kt | 69 | Idem |
| MINEUR | SeasonDetailsContent.kt | 69, 70, 72 | `contentFocusRequester`, `showBackdrop`, `blurAmount` inutilisés |
| MINEUR | ItemDetailScreen.kt | 76 | `contentFocusRequester` créé mais gaspillé |
| INFO | MovieDetailsContent.kt | 365 | Condition `type != PERSON` toujours vraie (pas de bug, juste redondant) |
| INFO | DetailActions.kt + MovieDetailsContent.kt | — | Duplication boutons/dialogs entre les deux fichiers |
