# Pill Play/Resume — Fix complet (gap, animation, troncature)

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt` | Structure Column, gap 6dp, animation slide+fade, texte Reprendre |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvRowList.kt` | contentPadding bottom=42dp sur LazyRow |
| `app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeComposeFragment.kt` | playItem() reprend à la position sauvegardée |

---

## Problème 1 — Gap entre carte et pill

**Avant** : Le pill était collé à la carte (0dp gap) ou utilisait `offset(y = 15.dp)` qui sortait des bounds mesurées.

**Après** : Structure Column avec `Spacer(Modifier.height(6.dp))` entre `TvFocusCard` et `PlayPillZone`.

```
Column {
    TvFocusCard { ... }      // Carte image
    Spacer(6.dp)             // Gap visuel
    PlayPillZone(32.dp)      // Zone fixe pour le pill
}
```

La zone pill réserve toujours 32dp de hauteur, même quand le pill est invisible, évitant tout décalage de layout.

---

## Problème 2 — Animation premium

**Avant** : `scaleIn` / `scaleOut` — animation basique de zoom.

**Après** : `slideInVertically` + `fadeIn` / `slideOutVertically` + `fadeOut`

### Specs animation

| Phase | Durée | Easing | Détail |
|-------|-------|--------|--------|
| Entrée | 200ms | EaseOutCubic `(0.33, 1, 0.68, 1)` | Slide up depuis 50% hauteur + fade in |
| Sortie | 150ms | EaseInCubic `(0.32, 0, 0.67, 0)` | Slide down vers 50% hauteur + fade out |
| Délai d'apparition | 1200ms | — | Le pill n'apparaît qu'après 1.2s de focus sur la carte |

### Code animation

```kotlin
AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(200, easing = EaseOutCubic)) +
        slideInVertically(
            animationSpec = tween(200, easing = EaseOutCubic),
            initialOffsetY = { it / 2 },
        ),
    exit = fadeOut(tween(150, easing = EaseInCubic)) +
        slideOutVertically(
            animationSpec = tween(150, easing = EaseInCubic),
            targetOffsetY = { it / 2 },
        ),
)
```

---

## Problème 3 — Troncature carte par LazyRow

**Option choisie** : B — `contentPadding(bottom = 42dp)` sur le `LazyRow` dans `TvRowList.kt`

**Raison** : Solution la plus propre et non-invasive. Le padding bottom donne à chaque row suffisamment d'espace pour que la zone pill (32dp) + le gap (6dp) + la marge de sécurité ne soient jamais clippés par le LazyRow. Aucun changement de structure nécessaire, compatible avec le système BringIntoView de Compose.

**Fichier** : `TvRowList.kt` ligne 141-145

```kotlin
LazyRow(
    contentPadding = PaddingValues(
        start = Tokens.Space.spaceSm,
        end = Tokens.Space.spaceSm,
        bottom = 42.dp,
    ),
)
```

---

## Correction associée — Reprendre au lieu de Recommencer

Le pill affiche maintenant :
- **"Reprendre"** (`lbl_resume`) si l'item a une progression sauvegardée
- **"Lecture"** (`lbl_play`) sinon

`HomeComposeFragment.playItem()` passe la position sauvegardée :

```kotlin
val resumeMs = item.userData?.playbackPositionTicks?.let { ticks ->
    if (ticks > 0) (ticks / 10_000).toInt() else null
}
playbackLauncher.launch(requireContext(), listOf(item), resumeMs)
```

---

## Screenshots

| Contexte | Fichier |
|----------|---------|
| Card focusée, pill visible (gap) | `docs/dev/audit_screens/pill_visible_gap.png` |
| Pill focusé, carte non tronquée | `docs/dev/audit_screens/pill_focused_no_truncation.png` |
| Card focusée après retour | `docs/dev/audit_screens/pill_card_return.png` |

## Build

- Debug GitHub : OK
- Release GitHub : OK
- Installé sur AM9 Pro (192.168.1.152) : OK
