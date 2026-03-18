# Audit — Player Skip + Prebuffering

**Date** : 2026-03-16
**Scope** : Position du bouton Skip, mécanisme de skip, pipeline MediaSegments, faisabilité prebuffering

---

## Point 1 — Position actuelle du bouton Skip

### Placement dans VideoPlayerScreen

**Fichier** : `ui/player/video/VideoPlayerScreen.kt:175-178`

```kotlin
modifier =
    Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 48.dp, bottom = 120.dp),
```

| Propriété | Valeur | Fichier:Ligne |
|-----------|--------|---------------|
| Alignment parent | `Alignment.BottomEnd` | VideoPlayerScreen.kt:177 |
| Padding droit | `48.dp` | VideoPlayerScreen.kt:178 |
| Padding bas | `120.dp` | VideoPlayerScreen.kt:178 |

### Layout interne du SkipOverlay

**Fichier** : `ui/playback/overlay/SkipOverlayView.kt:57-62`

```kotlin
Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = modifier
        .fillMaxSize()
        .padding(60.dp, 80.dp),
)
```

| Propriété | Valeur | Fichier:Ligne |
|-----------|--------|---------------|
| contentAlignment interne | `Alignment.BottomEnd` | SkipOverlayView.kt:58 |
| fillMaxSize | oui | SkipOverlayView.kt:60 |
| Padding interne H/V | `60.dp` / `80.dp` | SkipOverlayView.kt:61 |
| Animation | `fadeIn()` / `fadeOut()` | SkipOverlayView.kt:63 |
| Bouton variant | `VegafoXButtonVariant.Primary` | SkipOverlayView.kt:69 |
| Icône | `VegafoXIcons.SkipNext`, `iconEnd = true` | SkipOverlayView.kt:70-71 |
| Focus auto | `LaunchedEffect(Unit) { focusRequester.requestFocus() }` | SkipOverlayView.kt:75-77 |

### Position relative aux autres overlays (Z-order)

| Élément | Alignment | Padding | Z-order |
|---------|-----------|---------|---------|
| PlayerSurface | `Center` | aspectRatio | 1er (fond) |
| VideoPlayerOverlay (contrôles, header, seekbar) | fillMaxSize | — | 2e |
| SeekFeedbackOverlay (forward) | `CenterEnd` | `end = 120.dp` | 3e |
| SeekFeedbackOverlay (rewind) | `CenterStart` | `start = 120.dp` | 3e |
| **SkipOverlay** | **`BottomEnd`** | **`end = 48.dp, bottom = 120.dp`** | **4e** |
| PlayerSubtitles | `Center` | aspectRatio | 5e |
| BufferingIndicator | `Center` | — | 6e |

### Observation

Le SkipOverlay est positionné bas-droite avec `bottom = 120.dp`. Le padding interne de `SkipOverlayView` (60.dp/80.dp) s'additionne au padding du parent, ce qui place le bouton effectivement à ~108.dp du bord droit et ~200.dp du bas. Le bouton est au-dessus de la seekbar mais bien séparé des contrôles centraux.

**Position cible souhaitée** : `Alignment.BottomEnd` avec un padding bottom suffisant pour être au-dessus de la seekbar mais en dessous des contrôles — c'est déjà le cas actuellement. Le double padding interne (60.dp/80.dp dans SkipOverlayView) est redondant avec celui du parent (48.dp/120.dp dans VideoPlayerScreen) et pourrait être simplifié.

---

## Point 2 — Vérification du mécanisme de skip

### Chaîne d'appel complète

```
Bouton pressé
  → VegafoXButton.onClick (SkipOverlayView.kt:68)
    → onSkip() lambda (VideoPlayerScreen.kt:170-173)
      → playbackManager.state.seek(segment.end)
        → PlayerState.seek(to: Duration) (PlayerState.kt:150-152)
          → backendService.backend?.seekTo(to)
            → ExoPlayerBackend.seekTo(position: Duration) (ExoPlayerBackend.kt:307-313)
              → exoPlayer.seekTo(position.inWholeMilliseconds)
```

### Callback onSkip dans VideoPlayerScreen

**Fichier** : `ui/player/video/VideoPlayerScreen.kt:170-173`

```kotlin
onSkip = {
    activeSegment?.let { segment ->
        playbackManager.state.seek(segment.end)
    }
},
```

- **Valeur passée** : `segment.end` (type `Duration`)
- **Null-safety** : `activeSegment?.let` — ne fait rien si le segment a disparu entre-temps

### Conversion segment.end

**Fichier** : `util/sdk/MediaSegmentExtensions.kt:8`

```kotlin
val MediaSegmentDto.end get() = endTicks.ticks
```

- `endTicks` : `Long` en ticks Jellyfin (unités de 100 nanosecondes)
- `.ticks` : extension SDK qui convertit en `kotlin.time.Duration`

### Seek ExoPlayer

**Fichier** : `playback/media3/exoplayer/src/main/kotlin/ExoPlayerBackend.kt:307-313`

```kotlin
override fun seekTo(position: Duration) {
    if (!exoPlayer.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) ||
        !exoPlayer.isCurrentMediaItemSeekable) {
        Timber.w("Trying to seek but ExoPlayer doesn't support it...")
    }
    exoPlayer.seekTo(position.inWholeMilliseconds)
}
```

- **Conversion finale** : `Duration.inWholeMilliseconds` → `Long` en ms
- **Vérification** : log warning si seek non supporté, mais exécute quand même
- **Pas de SeekParameters** configuré → utilise le défaut Media3

### Validation des durées minimales

**Fichier** : `ui/playback/segment/MediaSegmentRepository.kt:106-115`

```kotlin
override fun getMediaSegmentAction(segment: MediaSegmentDto): MediaSegmentAction {
    val action = getDefaultSegmentTypeAction(segment.type)
    if (action == MediaSegmentAction.SKIP && segment.duration < SkipMinDuration)
        return MediaSegmentAction.NOTHING
    if (action == MediaSegmentAction.ASK_TO_SKIP &&
        segment.duration < AskToSkipMinDuration)
        return MediaSegmentAction.NOTHING
    return action
}
```

| Action | Durée minimum | Constante:Ligne |
|--------|---------------|-----------------|
| SKIP | 1 seconde | MediaSegmentRepository.kt:33 |
| ASK_TO_SKIP | 3 secondes | MediaSegmentRepository.kt:38 |

**Verdict** : Le mécanisme de skip fonctionne correctement. Le seek est bien déclenché vers `segment.end` (endTicks converti en Duration puis en millisecondes).

---

## Point 3 — Pipeline MediaSegments → SkipOverlay

### Diagramme de flux complet

```
┌──────────────────────────────────────────────────────────────┐
│ 1. PLAYBACK STARTS → Queue Entry mise à jour                │
│    rememberQueueEntry(playbackManager)                       │
│    (ui/composable/playback.kt:24-27)                         │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. LaunchedEffect(item?.id) déclenché                        │
│    (VideoPlayerScreen.kt:108-112)                            │
│    → mediaSegmentRepository.getSegmentsForItem(item)         │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. APPEL API JELLYFIN                                        │
│    (MediaSegmentRepository.kt:117-128)                       │
│    effectiveApi.mediaSegmentsApi.getItemSegments(            │
│        itemId = item.id,                                     │
│        includeSegmentTypes = SupportedTypes                  │
│    )                                                         │
│    Endpoint: GET /Items/{id}/MediaSegments                   │
│    Retour: List<MediaSegmentDto> avec startTicks/endTicks    │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 4. STOCKAGE EN STATE COMPOSE                                 │
│    (VideoPlayerScreen.kt:110)                                │
│    var segments by remember { mutableStateOf<List>([]) }     │
│    Pas de StateFlow, pas de ViewModel — state local          │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 5. POLLING POSITION (chaque 1 seconde)                       │
│    (VideoPlayerScreen.kt:115)                                │
│    val positionInfo by rememberPlayerPositionInfo(           │
│        playbackManager, precision = 1.seconds               │
│    )                                                         │
│    (ui/composable/playback.kt:29-50)                         │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 6. DÉTECTION SEGMENT ACTIF (derivedStateOf)                  │
│    (VideoPlayerScreen.kt:116-125)                            │
│    activeSegment = segments.firstOrNull { segment ->         │
│        action ∈ {ASK_TO_SKIP, SKIP}                          │
│        && pos >= segment.start                               │
│        && pos < segment.end                                  │
│    }                                                         │
└───────────────────────────┬──────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────────┐
│ 7. RENDU SKIPOVERLAY                                         │
│    (VideoPlayerScreen.kt:165-179)                            │
│    visible = activeSegment != null                           │
│    segmentType = activeSegment?.type?.name                   │
│    Animation fadeIn/fadeOut                                   │
└──────────────────────────────────────────────────────────────┘
```

### Résolution du client API (serveur multi-serveur)

**Fichier** : `MediaSegmentRepository.kt:119-120`

```kotlin
val serverId = UUIDUtils.parseUUID(item.serverId)
val effectiveApi = if (serverId != null)
    apiClientFactory.getApiClientForServer(serverId) ?: api else api
```

Le bon client API est résolu par serveur, avec fallback sur le client par défaut.

### Préférences utilisateur

**Fichier** : `preference/UserPreferences.kt`

```kotlin
var mediaSegmentActions = stringPreference(
    key = "media_segment_actions",
    defaultValue = mapOf(
        MediaSegmentType.INTRO to MediaSegmentAction.ASK_TO_SKIP,
        MediaSegmentType.OUTRO to MediaSegmentAction.ASK_TO_SKIP,
    ).toMediaSegmentActionsString(),
)
```

| Type | Action par défaut |
|------|-------------------|
| INTRO | ASK_TO_SKIP |
| OUTRO | ASK_TO_SKIP |
| PREVIEW | NOTHING |
| RECAP | NOTHING |
| COMMERCIAL | NOTHING |

### Cas où les segments ne sont pas chargés

| Scénario | Comportement | Résultat UI |
|----------|-------------|-------------|
| Serveur injoignable | `runCatching` → `emptyList()` | Pas de bouton Skip |
| Item sans serverId | Utilise client API par défaut | Fonctionne normalement |
| Serveur ne supporte pas les segments | 404 → `emptyList()` | Pas de bouton Skip |
| Pas d'intro détectée sur le serveur | Liste vide retournée | Pas de bouton Skip |
| Segment < 1s (SKIP) ou < 3s (ASK_TO_SKIP) | Action forcée à NOTHING | Pas de bouton Skip |
| Erreur réseau / timeout | `runCatching` → `emptyList()` | Pas de bouton Skip |
| Préférence utilisateur = NOTHING pour ce type | Action = NOTHING | Pas de bouton Skip |

**Observation** : Pas de mécanisme de retry. Un seul appel API par item. Si l'appel échoue, le bouton Skip ne s'affichera jamais pour cet item.

---

## Point 4 — Faisabilité du prebuffering post-intro

### Configuration actuelle du buffering

**Fichier** : `playback/media3/exoplayer/src/main/kotlin/ExoPlayerBackend.kt:98-141`

**Aucun `LoadControl` custom configuré** → utilise `DefaultLoadControl` de Media3 v1.9.2 :

| Paramètre | Valeur par défaut Media3 |
|-----------|--------------------------|
| minBufferMs | 15 000 ms (15s) |
| maxBufferMs | 30 000 ms (30s) |
| bufferForPlayback | 2 500 ms (2.5s) |
| bufferForPlaybackAfterRebuffer | 5 000 ms (5s) |

**Aucun `SeekParameters`** configuré → utilise le défaut du player.

### Sources media

**Fichier** : `ExoPlayerBackend.kt:67-97`

```kotlin
val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)
```

- Data source HTTP via `OkHttpDataSource.Factory`
- Extracteurs par défaut (HLS, progressive, etc.)
- Pas de manipulation de Range headers custom

### Queue ExoPlayer existante

**Fichier** : `ExoPlayerBackend.kt:223-241`

```kotlin
override fun prepareItem(item: QueueEntry) {
    // ...
    while (exoPlayer.mediaItemCount > MEDIA_ITEM_COUNT_MAX - 1)
        exoPlayer.removeMediaItem(0)
    exoPlayer.addMediaItem(mediaItem)
    exoPlayer.prepare()
}
```

- Limite : `MEDIA_ITEM_COUNT_MAX = 10` items en queue
- ExoPlayer bufferise automatiquement les items en queue

### Timing de disponibilité des segments

```
T(0 ms)   : Utilisateur lance la lecture
T(0 ms)   : Queue entry créée → LaunchedEffect déclenché
T(10 ms)  : getPlaybackInfo() appelé (async)
T(10 ms)  : getSegmentsForItem() appelé en parallèle (async)
T(~100 ms): Réponse segments reçue → endTicks de l'intro connu
T(~100 ms): Réponse playbackInfo → URL du stream prête
T(~500 ms): Premier frame vidéo affiché
T(intro)  : L'intro commence (souvent à 0s)
```

**Conclusion** : Les données de segments sont disponibles **avant le premier frame**. La position post-intro est connue dès T+100ms.

### Évaluation des approches de prebuffering

#### Approche A — Queue-Based (recommandée)

**Principe** : Utiliser le mécanisme de queue ExoPlayer existant pour pré-ajouter un item positionné après l'intro.

| Aspect | Évaluation |
|--------|------------|
| Faisabilité | HAUTE — queue déjà implémentée (10 items max) |
| Effort | 2-3 jours |
| Risque | Faible |
| Modification | `MediaStreamService` + `ExoPlayerBackend` |

**Limitation** : ExoPlayer ne supporte pas nativement le dual-buffering sur **le même stream** à deux positions différentes. La queue bufferise le **prochain item**, pas une position alternative du même item.

#### Approche B — Custom LoadControl

**Principe** : Créer un `SegmentAwareLoadControl` qui augmente le buffer quand la position approche de la fin de l'intro.

| Aspect | Évaluation |
|--------|------------|
| Faisabilité | MOYENNE — nécessite une expertise fine de l'API LoadControl |
| Effort | 5-7 jours |
| Risque | Moyen |
| Modification | Nouveau fichier `SegmentAwareLoadControl.kt` |

**Limitation** : Le LoadControl gère la taille du buffer autour de la position actuelle, pas le prebuffering à une position distante.

#### Approche C — URL avec startTimeTicks

**Principe** : Générer une seconde URL Jellyfin avec `startTimeTicks` pointant après l'intro, et l'ajouter en queue.

| Aspect | Évaluation |
|--------|------------|
| Faisabilité | HAUTE — Jellyfin supporte `startTimeTicks` dans l'URL |
| Effort | 3-5 jours |
| Risque | Moyen (gestion de la transition entre les deux URLs) |
| Modification | `JellyfinMediaStreamResolver` + `ExoPlayerBackend` |

**Avantage** : Le serveur Jellyfin peut commencer le transcode/stream depuis la position post-intro, évitant le buffering client.

#### Approche D — SeekParameters.CLOSEST_SYNC en arrière-plan

**Principe** : Déclencher un `exoPlayer.setSeekParameters(CLOSEST_SYNC)` puis un `seekTo(postIntroMs)` silencieux pendant l'intro pour forcer le buffering.

| Aspect | Évaluation |
|--------|------------|
| Faisabilité | BASSE — seekTo déplace la position de lecture, pas juste le buffer |
| Risque | Élevé — interrompt la lecture de l'intro |
| Conclusion | **Non viable** sans un second player |

### Verdict prebuffering

| Critère | Évaluation |
|---------|------------|
| Segments disponibles assez tôt ? | **OUI** — T+100ms, avant le premier frame |
| ExoPlayer supporte dual-buffering même stream ? | **NON** — pas nativement |
| Queue-based prebuffering ? | **OUI** — mais pour un autre MediaItem, pas la même position |
| URL offset Jellyfin ? | **OUI** — `startTimeTicks` supporté par le serveur |
| Custom LoadControl utile ? | **PEU** — gère le buffer autour de la position courante |
| SeekParameters en background ? | **NON** — seekTo déplace la lecture |

**Recommandation** : L'approche la plus pragmatique est de **ne pas prebuffer** mais d'optimiser le seek :
1. Configurer `SeekParameters.CLOSEST_SYNC` pour que le seek post-skip snape au keyframe le plus proche (évite d'attendre un frame exact)
2. Augmenter `bufferForPlaybackAfterRebuffer` via un `DefaultLoadControl` custom (ex: 8000ms au lieu de 5000ms) pour réduire le délai après le seek
3. Si le rebuffering post-skip est un vrai problème UX, implémenter l'approche C (URL avec startTimeTicks) en pré-chargeant un second stream

---

## Résumé des fichiers audités

| Fichier | Lignes clés | Rôle |
|---------|-------------|------|
| `ui/player/video/VideoPlayerScreen.kt` | 106-125, 165-179 | Chargement segments, détection active, placement SkipOverlay |
| `ui/playback/overlay/SkipOverlayView.kt` | 25-44, 47-80 | Composable Skip : texte, layout, animation, focus |
| `ui/playback/segment/MediaSegmentRepository.kt` | 106-129 | API segments, validation durée, résolution action |
| `ui/playback/segment/MediaSegmentAction.kt` | 6-25 | Enum NOTHING/SKIP/ASK_TO_SKIP |
| `util/sdk/MediaSegmentExtensions.kt` | 7-10 | Extensions start/end/duration sur MediaSegmentDto |
| `ui/composable/playback.kt` | 24-50 | rememberQueueEntry, rememberPlayerPositionInfo |
| `playback/core/src/main/kotlin/PlayerState.kt` | 150-152 | seek(Duration) → backend.seekTo() |
| `playback/core/src/main/kotlin/backend/PlayerBackend.kt` | 43 | Interface seekTo(Duration) |
| `playback/media3/exoplayer/src/main/kotlin/ExoPlayerBackend.kt` | 67-141, 223-241, 307-313 | Builder ExoPlayer, prepareItem, seekTo |
| `playback/jellyfin/src/main/kotlin/mediastream/JellyfinMediaStreamResolver.kt` | 87-125 | Résolution URL stream Jellyfin |
| `preference/UserPreferences.kt` | mediaSegmentActions | Préférences actions par type de segment |
| `di/AppModule.kt` | 193 | Injection Koin MediaSegmentRepository |
