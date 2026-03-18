# Audit — Cycle de vie ExoPlayer Trailer Hero

**Date** : 2026-03-16
**Statut** : CRITIQUE — Fuites audio et mémoire confirmées

---

## Synthèse

Le cycle de vie ExoPlayer du trailer Hero présente **plusieurs fuites audio/mémoire critiques**. Le problème principal est l'**absence totale de gestion du lifecycle au niveau du Fragment/Activity**, combinée à une **absence de pause/stop avant release** de l'ExoPlayer.

---

## Point 1 — Cycle de vie ExoPlayer trailer

### Fichier : `ui/home/mediabar/ExoPlayerTrailerView.kt`

### Instanciation (ligne ~61-118)

L'instanciation se fait dans un `DisposableEffect(streamInfo.videoUrl)` :

```kotlin
DisposableEffect(streamInfo.videoUrl) {
    val exoPlayer = buildTrailerPlayer(
        context = context,
        streamInfo = streamInfo,
        startSeconds = startSeconds,
        muted = muted,
        ...
    )
    player = exoPlayer

    onDispose {
        skipRunnable.value?.let { mainHandler.removeCallbacks(it) }
        exoPlayer.release()  // release() sans pause() ni stop()
        player = null
        videoSize = null
    }
}
```

- `buildTrailerPlayer()` appelle `player.prepare()` et `player.playWhenReady = true` — l'audio commence immédiatement
- Le `onDispose` appelle `exoPlayer.release()` mais **sans pause() ni stop() préalable**

### Pause/Arrêt

| Appel | Présent | Commentaire |
|-------|---------|-------------|
| `player.pause()` | **NON** | Jamais appelé nulle part |
| `player.stop()` | **NON** | Jamais appelé nulle part |
| `player.release()` | OUI | Dans `onDispose` uniquement, sans pause avant |

### Verdict Point 1

`release()` est appelé directement sans `pause()` ni `stop()` préalables. C'est une pratique dangereuse qui peut laisser des décodeurs, buffers et connexions HTTP actifs.

---

## Point 2 — Gestion dans HomeScreen / HomeHeroBackdrop

### HomeHeroBackdrop.kt (ligne ~85-281)

```kotlin
val shouldPlayTrailer = trailerState.isPlaying && trailerState.streamInfo != null

LaunchedEffect(shouldPlayTrailer) {
    if (shouldPlayTrailer) {
        showTrailerView = true
    } else if (showTrailerView) {
        delay(TRAILER_FADE_OUT_MS.toLong() + 50)  // 350ms délai
        showTrailerView = false
        videoReady = false
    }
}
```

- Quand `shouldPlayTrailer = false`, un **délai de 350ms** est introduit avant suppression du composable
- **Pendant ces 350ms, ExoPlayer continue de jouer** (audio audible)
- Aucun `DisposableEffect` pour observer `Lifecycle.Event.ON_PAUSE` ou `ON_STOP`

### HomeScreen.kt (ligne ~91-356)

Callbacks existants :
```kotlin
onTrailerEnded = { viewModel.stopTrailer() }   // fin naturelle du trailer
onBlur = { viewModel.stopTrailer() }            // perte de focus sur une card
```

Lifecycle Observer existant (ligne ~129-138) :
```kotlin
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && hasRequestedInitialFocus) {
            needsRestoreFocus = true
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

**CRITIQUE** : Le LifecycleObserver n'observe que `ON_RESUME` pour le focus. **Aucune observation de `ON_PAUSE` ou `ON_STOP` pour stopper le trailer.**

### Verdict Point 2

| Mécanisme | Présent | Impact |
|-----------|---------|--------|
| DisposableEffect(shouldPlayTrailer) | OUI | Masque le composable après 350ms |
| DisposableEffect(Lifecycle ON_PAUSE/ON_STOP) | **NON** | **FUITE AUDIO** |
| onVideoEnded callback | OUI | Appelle stopTrailer() |
| onBlur callback | OUI | Appelle stopTrailer() |
| Pause ExoPlayer avant suppression | **NON** | **FUITE AUDIO** |

---

## Point 3 — Callbacks Activity/Fragment

### HomeComposeFragment.kt (ligne ~1-60)

```kotlin
class HomeComposeFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(...): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { ... }
        }
}
```

**Absence totale de** :
- `onPause()` — pas d'appel à `stopTrailer()`
- `onStop()` — pas d'appel à `stopTrailer()`
- `onDestroy()` — pas d'appel à `stopTrailer()`
- `LifecycleObserver` / `DefaultLifecycleObserver`

### HomeViewModel.kt (ligne ~72-685)

```kotlin
class HomeViewModel(...) : ViewModel() {
    // Aucune override de onCleared()

    fun stopTrailer() {
        trailerJob?.cancel()
        trailerJob = null
        _trailerState.value = TrailerState()
    }
}
```

**CRITIQUE** : `onCleared()` n'est pas overridé. Si le ViewModel est détruit, `trailerJob` n'est jamais annulé et l'ExoPlayer continue à jouer.

### Verdict Point 3

| Callback | Fragment | ViewModel | Statut |
|----------|----------|-----------|--------|
| `onPause()` | NON | N/A | **MANQUANT** |
| `onStop()` | NON | N/A | **MANQUANT** |
| `onDestroy()` | NON | N/A | **MANQUANT** |
| `onCleared()` | N/A | NON | **MANQUANT** |
| `LifecycleObserver` | NON | NON | **MANQUANT** |

---

## Point 4 — Cause probable de la fuite audio

### Scénarios de fuite identifiés

#### Scénario A : Navigation HomeScreen → Detail (cas principal)

```
1. User navigue vers ItemDetailScreen
2. HomeComposeFragment passe en onPause() puis onStop()
3. onPause() n'appelle PAS stopTrailer()
4. ExoPlayer continue à jouer en arrière-plan → AUDIO LEAK
5. Le composable finit par être disposé (DisposeOnViewTreeLifecycleDestroyed)
6. release() est appelé SANS pause() → décodeurs restent alloués
```

#### Scénario B : App en arrière-plan

```
1. User quitte l'app (Home button)
2. onStop() du Fragment n'appelle PAS stopTrailer()
3. ExoPlayer continue à jouer même si l'app n'est pas visible
4. ViewModel gardé en mémoire → trailerJob jamais annulé
5. Audio joue indéfiniment en arrière-plan
```

#### Scénario C : Navigation rapide entre items

```
1. User focus Item A → trailer démarre
2. User focus Item B rapidement (< 350ms)
3. Trailer A en fade-out (350ms) → ExoPlayer A joue toujours
4. Trailer B démarre son compte à rebours
5. Deux ExoPlayer actifs simultanément pendant le crossfade
```

### Cause racine

| Cause | Sévérité | Justification |
|-------|----------|---------------|
| Pas d'observer `Lifecycle.ON_PAUSE` | CRITIQUE | Audio continue quand Fragment est invisible |
| Pas de `pause()` avant `release()` | CRITIQUE | Décodeurs/buffers restent alloués |
| Pas de `onCleared()` dans ViewModel | CRITIQUE | trailerJob survit au ViewModel |
| Délai de 350ms avant removal composable | MAJEUR | Permet lectures simultanées et race conditions |
| Pas de `stop()` sur `onStop()` | MAJEUR | Audio continue quand app est en background |

---

## Fichiers concernés

| Fichier | Lignes clés | Problème |
|---------|-------------|----------|
| `ui/home/mediabar/ExoPlayerTrailerView.kt` | 61-118 | `release()` sans `pause()`/`stop()` |
| `ui/home/compose/HomeHeroBackdrop.kt` | 122-134, 178-281 | Pas d'observer Lifecycle, délai 350ms |
| `ui/home/compose/HomeScreen.kt` | 128-138 | Observer Lifecycle uniquement pour ON_RESUME |
| `ui/home/compose/HomeComposeFragment.kt` | 1-60 | Aucun override de onPause/onStop/onDestroy |
| `ui/home/compose/HomeViewModel.kt` | 72-685 | Pas d'override onCleared() |

---

## Conclusion

Le cycle de vie du trailer Hero est **critiquement compromis** sur 4 niveaux :

1. **ExoPlayer** — pas de `pause()`/`stop()` avant `release()`
2. **Composable** — pas de `DisposableEffect` observant le lifecycle pour ON_PAUSE/ON_STOP
3. **Fragment** — aucun callback lifecycle pour arrêter le trailer
4. **ViewModel** — pas de `onCleared()` pour annuler trailerJob

La solution requiert une intervention à chaque niveau de la stack.
