# Audit Performance Chargement Video — audit-player-perf-01

Date : 2026-03-16

---

## Point 1 — Chaine complete de resolution du stream

### Sequence (clic Lecture → premier frame)

```
T0   User clic Lecture
│
├─ PlaybackLauncher.launch()                          < 1 ms
│  └─ VideoQueueManager stocke la queue (synchrone)
│
├─ Navigation → VideoPlayerFragment                   ~20-50 ms
│  └─ onCreate() : BaseItemQueueSupplier cree,
│     playbackManager.queue.addSupplier()
│     player demarre en PAUSED
│
├─ MediaStreamService.onInitialize()                  immediate
│  └─ observe queue.entry flow → nouveau QueueEntry
│
├─ ensureMediaStream()                                ★ BLOQUANT
│  └─ JellyfinMediaStreamResolver.getStream()
│     └─ getPlaybackInfo()                            ~200-800 ms (LAN)
│        └─ api.mediaInfoApi.getPostedPlaybackInfo()  ~1000-3000 ms (WAN)
│     └─ construction URL stream                      < 1 ms
│
├─ ExoPlayerBackend.playItem()                        < 5 ms
│  └─ addMediaItem + prepare() + play()
│
├─ ExoPlayer STATE_BUFFERING                          ~200-1500 ms
│  └─ buffer initial (bufferForPlaybackMs = 2500 ms default)
│
├─ ExoPlayer STATE_READY → premier frame              ★ isBuffering = false
│  └─ onPlaybackStateChanged(READY)
│  └─ onVideoSizeChanged()
│
T_end  Video visible a l'ecran
```

### Durees estimees (reseau local 1 Gbps)

| Etape | Duree estimee | Parallelisable |
|-------|--------------|----------------|
| Navigation + Fragment.onCreate | 20-50 ms | Non |
| Queue entry creation | < 5 ms | Non |
| `getPostedPlaybackInfo()` API | 200-800 ms | Oui (prefetch) |
| Construction URL stream | < 1 ms | Non |
| ExoPlayer prepare + buffer | 500-2000 ms | Chevauche API si prefetch |
| **Total estime** | **~1-3 secondes** | |

Le goulot d'etranglement principal est **getPostedPlaybackInfo()** suivi du **buffer initial ExoPlayer**. Ces deux etapes sont sequentielles : le buffer ne peut pas commencer avant que l'URL soit resolue.

---

## Point 2 — Appels reseau sequentiels vs paralleles

### Appels pendant le demarrage de la lecture

| Appel API | Fichier | Timing | Bloquant |
|-----------|---------|--------|----------|
| `getPostedPlaybackInfo()` | JellyfinMediaStreamResolver:99 | **Sequentiel, avant playback** | **OUI** |
| `getItemSegments()` | MediaSegmentRepository | Parallele, apres item change | Non |
| `reportPlaybackStart()` | PlaySessionService:119 | Async, apres PLAYING atteint | Non |
| `reportPlaybackProgress()` | PlaySessionService | Async, periodique | Non |

### Analyse

- `getPostedPlaybackInfo()` est le **seul appel bloquant** — rien ne peut demarrer avant.
- `getItemSegments()` est lance en parallele (via `LaunchedEffect(item?.id)` dans VideoPlayerScreen) mais avec **3 retries et 1s delay entre chaque** — potentiellement 3s de latence pour les segments (non bloquant pour la lecture).
- `reportPlaybackStart()` est correctement asynchrone et non-bloquant.

### Opportunites de parallelisation

1. **`getPostedPlaybackInfo()` pourrait etre prefetche** depuis la page detail (l'utilisateur a deja navigue vers l'item).
2. **Les segments pourraient etre prefetches** en meme temps que le playback info au lieu d'attendre le player screen.

---

## Point 3 — Cache existant

### MediaStreamService.kt

```kotlin
// ensureMediaStream() ligne 45-57
mediaStream = mediaStream ?: mediaStreamResolvers.firstNotNullOfOrNull { ... }
```

- **Cache au niveau QueueEntry** : si `entry.mediaStream` est deja defini, pas de re-resolution.
- **Pas de cache cross-entry** : si un nouveau QueueEntry est cree pour le meme item (retour Home + relecture), l'API est rappelee.
- **Pas de cache MediaInfo** : `getPostedPlaybackInfo()` est appele a chaque resolution.

### JellyfinMediaStreamResolver.kt

- **Aucun cache** — chaque appel a `getStream()` declenche un nouvel appel API `getPostedPlaybackInfo()`.

### Passage de donnees detail → player

- **Inexistant** — la page detail ne pre-resolve aucune info de stream.
- Le `BaseItemDto` est passe via `VideoQueueManager` mais **sans playback info**.
- Le player recree tout depuis zero.

### Risque du cache MediaInfo

Un cache `PlayableMediaStream` par item ID a ete teste et cause un crash : `ExoPlayerBackend.playItem()` compare `currentStream == stream` et skip la lecture si le meme objet est reutilise. Un cache au niveau `MediaInfo` (reponse API) est plus sur mais peut causer des 404 si la session serveur expire.

---

## Point 4 — Prefetch depuis la page detail et la Home

### HomePrefetchService.kt

Prefetche **uniquement les metadata des rows** (Continue Watching + Next Up) au demarrage de session :

```kotlin
// Ligne 49-50
val resumeDeferred = async { loadContinueWatching() }  // getResumeItems()
val nextUpDeferred = async { loadNextUp() }              // getNextUp()
```

- **Ne prefetche PAS les stream info** — seulement les `BaseItemDto` pour l'affichage.
- **Ne pourrait pas facilement etre etendu** pour les streams car le `DeviceProfile` depend de `UserPreferences` et `ServerVersion`, et les sessions playback expirent rapidement.

### Page detail

- Charge backdrop, poster, logo, overview, cast, similar items.
- **Ne charge jamais `getPostedPlaybackInfo()`** — cette donnee n'est utile qu'au moment de la lecture.
- Le `BaseItemDto` de la page detail inclut deja `MediaSources` et `MediaStreams` (via les `ItemFields` demandes), mais le resolver les ignore au profit d'un appel `getPostedPlaybackInfo()` qui retourne des infos plus completes (supportsDirectPlay, transcodingUrl, etc.).

### Contraintes du prefetch de streams

1. **Session serveur** : `getPostedPlaybackInfo()` cree une session de lecture cote serveur. Si le prefetch est fait trop tot et que l'utilisateur ne lance pas la lecture, la session est gaspillee.
2. **DeviceProfile** : doit etre reconstruit a chaque appel (depend des codecs du device).
3. **Cache invalidation** : les URLs de transcodage expirent avec la session → pas cacheable de maniere fiable.

---

## Point 5 — Configuration ExoPlayer pour TV

### DefaultLoadControl (ExoPlayerBackend.kt:100-107)

```kotlin
DefaultLoadControl.Builder()
    .setBufferDurationsMs(
        minBufferMs = 15_000,          // DEFAULT_MIN_BUFFER_MS
        maxBufferMs = 60_000,          // DEFAULT_MAX_BUFFER_MS
        bufferForPlaybackMs = 2_500,   // DEFAULT_BUFFER_FOR_PLAYBACK_MS
        bufferForPlaybackAfterRebufferMs = 3_000,  // custom
    )
```

**Analyse** :
- `bufferForPlaybackMs = 2500` ms : ExoPlayer attend 2.5s de buffer avant de commencer la lecture. Sur LAN (100+ Mbps), c'est **trop conservateur** — pourrait etre reduit a 500-1000 ms.
- `bufferForPlaybackAfterRebufferMs = 3000` ms : raisonnable pour eviter les re-buffers.

### DefaultTrackSelector (ExoPlayerBackend.kt:114-128)

- Audio offload active : `AUDIO_OFFLOAD_MODE_ENABLED`
- `setAllowInvalidateSelectionsOnRendererCapabilitiesChange(true)` — correct.
- **Pas de contrainte sur la resolution maximale** — le player choisit la meilleure qualite disponible.

### Renderers (ExoPlayerBackend.kt:89-98)

- `setEnableDecoderFallback(true)` — bon, evite les crashes codec.
- Extension renderer mode configurable (FFmpeg prefer ou on).

### Wake mode

- `setWakeMode(C.WAKE_MODE_NETWORK)` — actif, maintient la connexion reseau.

### Elements absents

| Configuration | Statut | Impact |
|---------------|--------|--------|
| `setForegroundMode(true)` | **Absent** | Priorise la lecture vs le buffer. Reduirait le temps au premier frame. |
| `DefaultBandwidthMeter` | **Default** | Pas de custom — utilise l'estimation par defaut. Sur LAN, on pourrait forcer une bande passante elevee initiale. |
| `setPauseAtEndOfMediaItems(true)` | Present (ligne 130) | Correct pour le queue management. |

---

## Point 6 — Temps de demarrage estime et optimisations

### Estimation actuelle (LAN, direct play)

| Phase | Duree |
|-------|-------|
| Navigation + UI setup | ~50 ms |
| `getPostedPlaybackInfo()` | ~300-600 ms |
| ExoPlayer prepare | ~50 ms |
| Buffer initial (2500 ms min) | ~800-2500 ms |
| **Total** | **~1.2 - 3.2 secondes** |

Note : l'overlay logo (2.5s fixe) masque deja ce temps dans la plupart des cas.

### Goulot d'etranglement principal

**Le buffer initial ExoPlayer (2500 ms)** est le facteur dominant sur LAN ou l'API repond en < 500 ms. Sur WAN, c'est **l'appel API** qui domine.

### Optimisations proposees (ratio effort/gain)

| # | Optimisation | Gain estime | Effort | Risque |
|---|-------------|-------------|--------|--------|
| 1 | **Reduire `bufferForPlaybackMs` a 500 ms** | -1500 ms au premier frame | Trivial (1 ligne) | Faible — LAN est fiable |
| 2 | **`setForegroundMode(true)` sur ExoPlayer** | -100-300 ms (priorise decoder) | Trivial (1 ligne) | Aucun |
| 3 | **Forcer bande passante initiale elevee** | -200-500 ms (skip estimation) | Faible (DefaultBandwidthMeter custom) | Faible sur LAN |
| 4 | **Prefetch `getPostedPlaybackInfo()` depuis la page detail** | -300-600 ms (API deja en cache) | Moyen (passage de donnees detail→player) | Moyen — sessions expirent |
| 5 | **Paralleliser segments + playback info** | -0 ms (deja parallele) | N/A | N/A |
| 6 | **Etendre HomePrefetchService aux streams** | -300-600 ms (streams pre-resolus) | Eleve (architecture) | Eleve — sessions, cache invalidation |

### Recommandation prioritaire

**Optimisation #1 (bufferForPlaybackMs = 500)** offre le meilleur ratio gain/effort :
- Gain : ~1.5s de reduction du temps au premier frame
- Effort : changer une seule valeur
- Risque : minimal sur reseau local (la Ugoos box est en ethernet)
- Le `bufferForPlaybackAfterRebufferMs` reste a 3000 ms pour proteger contre les re-buffers

Combinee avec **#2 (foreground mode)**, on obtient ~1.5-2s de gain total pour 2 lignes de code.
