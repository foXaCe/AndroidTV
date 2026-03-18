# audit-player-crash-01 — Diagnostic : le player ne lit rien

**Date** : 2026-03-16
**Statut** : DIAGNOSTIC COMPLET — AUCUN FICHIER MODIFIÉ

---

## Point 1 — Derniers chantiers touchant au player

### Fichiers modifiés par les chantiers récents

| Chantier | Fichier | Nature du changement |
|----------|---------|---------------------|
| cosmetic-player-02 (trickplay) | `SeekProvider.kt` | Refactoring complet : renommé de `CustomSeekProvider`, ajout `StateFlow<Bitmap?>` pour trickplay, `CoroutineExceptionHandler`, lazy disk cache |
| cosmetic-player-02 (trickplay) | `VideoPlayerControls.kt` | Intégration trickplay : `onScrubPosition` callback, `TrickplayThumbnail` composable |
| cosmetic-player-02 (trickplay) | `TrickplayThumbnail.kt` | **NOUVEAU FICHIER** — Composable wrapper autour de `SeekProvider.currentThumbnail` |
| cosmetic-player-03 (skip + seek initial) | `VideoPlayerFragment.kt` | Remplacement du `repeat(20)` par `withTimeout(2000) { playState.first { it != STOPPED } }` |
| cosmetic-player-04 (seekbar optimiste) | `PlayerSeekbar.kt` | Ajout `optimisticTarget` avec `delay(500)` pour feedback visuel immédiat |
| cosmetic-player-05 (SeekParameters + LoadControl) | `ExoPlayerBackend.kt` | Ajout `DefaultLoadControl` custom + `SeekParameters.CLOSEST_SYNC` dans `seekTo()` |
| cleanup-22 (dépendances) | `build.gradle.kts` (exoplayer) | Modifications de dépendances |

### Détail par fichier critique

#### `ExoPlayerBackend.kt` — Changements de cosmetic-player-05
1. **DefaultLoadControl ajouté** (lignes 100-107) :
   ```kotlin
   DefaultLoadControl.Builder()
       .setBufferDurationsMs(
           DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,      // 50000
           DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,      // 50000
           DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, // 2500
           3000, // bufferForPlaybackAfterRebufferMs (au lieu de 5000 par défaut)
       ).build()
   ```
2. **SeekParameters.CLOSEST_SYNC** ajouté dans `seekTo()` (ligne 324) — appliqué uniquement lors des seeks, pas à l'initialisation

#### `VideoPlayerFragment.kt` — Changements de cosmetic-player-03
Le `repeat(20)` a été remplacé par (lignes 58-70) :
```kotlin
Args.fromBundle(arguments).position?.milliseconds?.let { seekPosition ->
    lifecycleScope.launch {
        try {
            withTimeout(2000) {
                playbackManager.state.playState.first { it != PlayState.STOPPED }
            }
            playbackManager.state.seek(seekPosition)
        } catch (_: TimeoutCancellationException) {
            Timber.w("Player backend not ready after 2s, skipping initial seek to $seekPosition")
        }
    }
}
```

#### `AppModule.kt` — Injection SeekProvider (ligne 181)
```kotlin
single { SeekProvider(get(), get(), get(), androidContext(), get()) }
```
5 paramètres positionnels : `PlaybackControllerContainer`, `ImageLoader`, `ApiClient`, `Context`, `UserPreferences`. Aucun changement structurel récent.

#### `PlaybackController.kt` — Legacy, non utilisé par le nouveau player
Le `PlaybackController` legacy n'est **pas injecté** dans `VideoPlayerFragment`. Le fragment utilise uniquement `PlaybackManager` du module `playback/core`. Cependant, `SeekProvider` dépend toujours de `PlaybackControllerContainer` pour obtenir la durée et les infos trickplay.

---

## Point 2 — État actuel d'ExoPlayerBackend

### DefaultLoadControl — VERDICT : ✅ CORRECT

Les valeurs sont toutes valides :
- `minBufferMs` = 50000 (défaut ExoPlayer)
- `maxBufferMs` = 50000 (défaut ExoPlayer)
- `bufferForPlaybackMs` = 2500 (défaut ExoPlayer)
- `bufferForPlaybackAfterRebufferMs` = 3000 (réduit de 5000 → OK, plus réactif)

**Aucune valeur ne bloque le démarrage de la lecture.** Le seul changement est un rebuffer légèrement plus rapide (3s au lieu de 5s).

### SeekParameters.CLOSEST_SYNC — VERDICT : ✅ CORRECT

Appliqué **uniquement** dans `seekTo()` (ligne 324), pas dans le constructeur global. N'affecte que les seeks explicites, pas la lecture initiale. Import correct : `androidx.media3.exoplayer.SeekParameters`.

### Injection Koin de SeekProvider — VERDICT : ⚠️ ATTENTION INDIRECTE

`SeekProvider` est injecté dans `VideoPlayerControls` via `koinInject()` (ligne 83). Ceci est correct. **Mais** `SeekProvider` dépend de `PlaybackControllerContainer` qui contient un `playbackController` nullable. Si le `PlaybackController` legacy n'est jamais assigné au container (ce qui arrive dans le nouveau player Compose), alors :
- `SeekProvider.duration` retourne `-1`
- `SeekProvider.updatePosition()` ne fait rien (condition `dur <= 0`)
- Les thumbnails trickplay sont toujours `null`

**Ce n'est pas un crash** — c'est un échec silencieux du trickplay uniquement.

---

## Point 3 — État actuel du seek initial

### Analyse du withTimeout — VERDICT : ⚠️ RACE CONDITION POTENTIELLE

**Flux complet tracé :**

```
VideoPlayerFragment.onCreate():
  ① playbackManager.queue.clear()                          // sync
  ② playbackManager.queue.addSupplier(queueSupplier)       // sync, mais lance coroutine interne
  ③ lifecycleScope.launch { withTimeout(2000) { ... } }    // async - attend playState != STOPPED
  ④ playbackManager.state.pause()                          // sync → exoPlayer.pause()
```

**Chaîne asynchrone déclenchée par ② :**
```
QueueService.addSupplier()
  → coroutineScope.launch { setIndex(0) }         // async
    → _entry.value = firstEntry                    // déclenche MediaStreamService
      → MediaStreamService.onEach
        → ensureMediaStream()                      // résolution réseau async
          → backend.playItem(item)                 // prépare ExoPlayer + play()
            → exoPlayer.prepare() + exoPlayer.play()
              → onPlaybackStateChanged(BUFFERING)
                → onIsPlayingChanged(false)
                  → _playState.value = PAUSED      // ≠ STOPPED → timeout satisfait
```

**Condition du `first {}`** : `it != PlayState.STOPPED`

Cette condition est **correcte en principe** car :
1. Quand ExoPlayer commence à buffer, il émet `PAUSED` (pas `STOPPED`)
2. `PAUSED` satisfait la condition `!= STOPPED`
3. Le seek est alors exécuté

**Mais il y a un scénario d'échec :**
- Si `ensureMediaStream()` prend plus de 2s (réseau lent, DNS, etc.)
- OU si la queue est vide / le supplier ne retourne rien
- → `TimeoutCancellationException` attrapé silencieusement
- → Le seek initial est **ignoré** avec seulement un `Timber.w`

**Impact** : La lecture reprend à 0:00 au lieu de la position sauvegardée. Pas un crash, mais une régression fonctionnelle.

### La ligne `pause()` (ligne 73) — VERDICT : ✅ PAS BLOQUANTE

`playbackManager.state.pause()` appelle `exoPlayer.pause()` avant que le media soit chargé. Ceci est inoffensif car :
1. ExoPlayer est en `STATE_IDLE` sans media → `pause()` n'a pas d'effet
2. Quand `playItem()` est appelé plus tard, il appelle explicitement `exoPlayer.play()` qui override le pause
3. `onResume()` (lifecycle) appelle `unpause()` → `backend.play()` en backup

---

## Point 4 — Mécanismes de logging et points d'échec silencieux

### Points de logging existants dans la chaîne de lecture

| Fichier | Ligne | Niveau | Message | Point d'échec |
|---------|-------|--------|---------|---------------|
| `VideoPlayerFragment.kt` | 54 | INFO | `Created a queue with N items` | Queue vide → N=0 |
| `VideoPlayerFragment.kt` | 67 | **WARN** | `Player backend not ready after 2s, skipping initial seek` | Seek initial ignoré |
| `MediaStreamService.kt` | 20 | DEBUG | `Queue entry changed to $entry` | entry=null si queue vide |
| `MediaStreamService.kt` | 31 | **ERROR** | `Unable to resolve stream for entry $entry` | Stream non résolu |
| `MediaStreamService.kt` | 52 | **ERROR** | `Media stream resolver failed for $this` | Exception réseau/API |
| `ExoPlayerBackend.kt` | 300 | INFO | `Playing ${item.mediaStream?.url}` | URL null = problème |
| `ExoPlayerBackend.kt` | 321 | WARN | `Trying to seek but ExoPlayer doesn't support it` | Seek impossible |
| `PlaybackController.kt` | 827 | **ERROR** | `Unable to get stream info for internal player` | Legacy : stream info fail |
| `PlaybackController.kt` | 836 | **ERROR** | `Error getting playback stream info` | Legacy : stream info fail |
| `PlaybackController.kt` | 1588 | **ERROR** | `Playback error - $msg` | Legacy : erreur lecture |

### Points d'échec SANS logging (échecs silencieux)

| Fichier | Ligne | Problème |
|---------|-------|----------|
| `PlayerState.kt` | 127 | `backendService.backend?.play()` — si backend null, rien ne se passe, **aucun log** |
| `PlayerState.kt` | 132 | `backendService.backend?.pause()` — idem, **aucun log** |
| `PlayerState.kt` | 151 | `backendService.backend?.seekTo(to)` — idem, **aucun log** |
| `QueueService.kt` | 84-86 | `if (_entryIndex.value == Queue.INDEX_NONE)` — si queue déjà à un index, pas de setIndex, **aucun log** |
| `ExoPlayerBackend.kt` | 258 | `if (currentStream == stream) return` — skip silencieux si même stream |
| `SeekProvider.kt` | 105-112 | `duration` getter retourne -1 si container.playbackController est null, **aucun log** |

### Timber absent dans les composants UI

Les fichiers suivants n'ont **aucun Timber** :
- `VideoPlayerControls.kt`
- `VideoPlayerScreen.kt`
- `VideoPlayerOverlay.kt`
- `VideoPlayerHeader.kt`
- `PlayerSeekbar.kt`
- `TrickplayThumbnail.kt`

---

## Synthèse des causes probables

### Hypothèse 1 — HAUTE PROBABILITÉ : Stream non résolu
**Symptôme** : Player s'ouvre, écran noir, rien ne se passe
**Cause** : `MediaStreamService.ensureMediaStream()` échoue silencieusement
**Log attendu** : `Timber.e("Unable to resolve stream for entry $entry")` ou `Timber.e("Media stream resolver failed")`
**Vérification** : Filtrer Logcat sur `MediaStreamService`

### Hypothèse 2 — MOYENNE PROBABILITÉ : ExoPlayer error pendant prepare
**Symptôme** : Player s'ouvre, écran noir, puis se ferme
**Cause** : `onPlayerError()` → `PlayState.ERROR`
**Log attendu** : Aucun Timber dans `onPlayerError` — l'erreur est propagée via state mais **jamais loggée** par le backend !
**Problème critique** : `ExoPlayerBackend.PlayerListener.onPlayerError()` émet `ERROR` mais **ne logge pas l'erreur**

### Hypothèse 3 — FAIBLE PROBABILITÉ : LoadControl bloquant
**Cause** : Valeurs invalides dans DefaultLoadControl
**Verdict** : Éliminée — les valeurs sont correctes

### Hypothèse 4 — FAIBLE PROBABILITÉ : SeekParameters bloquant
**Cause** : CLOSEST_SYNC empêche la lecture
**Verdict** : Éliminée — appliqué uniquement dans seekTo(), pas à l'init

### Hypothèse 5 — MOYENNE PROBABILITÉ : Queue vide
**Symptôme** : `Created a queue with 0 items`
**Cause** : `videoQueueManager.getCurrentVideoQueue()` retourne une liste vide
**Vérification** : Vérifier le log `Created a queue with N items`

---

## Bugs confirmés (indépendants du crash principal)

### Bug 1 — `onPlayerError` ne logge pas l'erreur (CRITIQUE pour diagnostic)
**Fichier** : `ExoPlayerBackend.kt:176`
```kotlin
override fun onPlayerError(error: PlaybackException) {
    listener?.onPlayStateChange(PlayState.ERROR)
    // ⚠️ PAS DE TIMBER.E(error) — l'erreur ExoPlayer est perdue !
}
```
**Impact** : Impossible de diagnostiquer les erreurs de lecture dans Logcat.

### Bug 2 — SeekProvider silencieusement cassé avec le nouveau player
**Fichier** : `SeekProvider.kt:105-112`
Le `PlaybackControllerContainer.playbackController` est null quand le nouveau `VideoPlayerFragment` (Compose) est utilisé au lieu de l'ancien `CustomPlaybackOverlayFragment`. Résultat : trickplay désactivé silencieusement.

### Bug 3 — Aucun fallback si seek initial timeout
**Fichier** : `VideoPlayerFragment.kt:66-68`
Si le timeout expire, la lecture reprend à 0:00 sans notification à l'utilisateur.

---

## Actions recommandées pour diagnostic immédiat

1. **Ajouter `Timber.e` dans `onPlayerError`** pour capturer les erreurs ExoPlayer
2. **Vérifier Logcat** avec les filtres :
   - `MediaStreamService` — pour voir si le stream est résolu
   - `ExoPlayerBackend` — pour voir si `playItem` est appelé
   - `Queue entry changed` — pour voir si la queue reçoit un item
   - `Created a queue with` — pour voir si la queue est vide
3. **Tester sans position** (sans seek initial) pour isoler si le problème est le seek ou la lecture elle-même
