# Audit Player Video — audit-player-01

**Date** : 2026-03-16
**Scope** : Player vidéo Compose (ui/player/video/, ui/player/base/, ui/playback/overlay/, nextup, stillwatching, toast)
**Statut** : Lecture seule — aucun fichier modifié

---

## Point 1 — Architecture et flux de données

### State holder principal

Le player n'a **pas de ViewModel dédié**. L'état est entièrement porté par `PlaybackManager` (module `playback/core`), injecté via Koin dans chaque composable.

**StateFlow exposés par `PlaybackManager.state` (PlayerState)** :

| StateFlow | Type | Description |
|-----------|------|-------------|
| `playState` | `StateFlow<PlayState>` | STOPPED, PLAYING, PAUSED, ERROR |
| `isBuffering` | `StateFlow<Boolean>` | État buffering ExoPlayer |
| `speed` | `StateFlow<Float>` | Vitesse de lecture (0.25x–2.0x) |
| `videoSize` | `StateFlow<VideoSize>` | Largeur/hauteur de la vidéo |
| `playbackOrder` | `StateFlow<PlaybackOrder>` | DEFAULT, RANDOM, SHUFFLE |
| `repeatMode` | `StateFlow<RepeatMode>` | NONE, REPEAT_ENTRY_ONCE, REPEAT_ENTRY_INFINITE |
| `scrubbing` | `StateFlow<Boolean>` | État de scrubbing seekbar |

**Propriétés non-réactives** :

| Propriété | Type | Description |
|-----------|------|-------------|
| `positionInfo` | `PositionInfo` | Position active, buffer, durée — getter direct, **pas un Flow** |
| `volume` | `PlayerVolumeState` | Contrôle volume via AudioManager |

### Gestion de la position de lecture

La position est mise à jour par deux mécanismes distincts :

1. **`rememberPlayerPositionInfo(precision)`** (`playback.kt:30-50`) : Composable qui retourne un `MutableState<PositionInfo>`. Lance un `LaunchedEffect(playing, precision)` qui boucle tant que `playing == true`, lisant `positionInfo` toutes les `precision` ms (1s par défaut). La boucle s'aligne sur les bornes de seconde via `delay(precisionMs - (active % precisionMs))`.

2. **`rememberPlayerProgress()`** (`playback.kt:52-96`) : Retourne un `Float` animé (0f–1f) via `Animatable`. Snap à la position courante, puis anime linéairement vers 1f sur la durée restante. Utilisé par `PlayerSeekbar`.

### Instanciation ExoPlayer

ExoPlayer est instancié et géré par le module `playback/core` via `PlayerBackend`. La surface vidéo est un `PlayerSurfaceView` (AndroidView), les sous-titres un `PlayerSubtitleView` (AndroidView). Le `PlaybackManager` est lié au cycle de vie via Koin (scope session).

### Cycle de vie (VideoPlayerFragment)

| Callback | Action |
|----------|--------|
| `onCreate` | Crée la queue (`BaseItemQueueSupplier`), seek position initiale (retry 20×50ms), pause initiale |
| `onResume` | `playbackManager.state.unpause()` |
| `onPause` | `playbackManager.state.pause()` |
| `onStop` | `playbackManager.state.stop()` |

**Problème identifié** : Le seek initial utilise `repeat(20) { seek(); delay(50) }` — un polling brut de 1 seconde qui envoie 20 commandes seek au backend, même si le premier réussit. Ce pattern est fragile et gaspille des appels.

**Problème identifié** : `onStop()` appelle `stop()` mais ne libère pas explicitement le `PlaybackManager` ni ses services. La libération dépend du scope Koin de session — si le fragment est détruit sans fermer la session, le backend peut rester actif.

---

## Point 2 — Cohérence design system

### VideoPlayerScreen.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | **PARTIEL** | `Color.Black` hardcodé (l.62) au lieu de `VegafoXColors.BackgroundDeep`. `VegafoXColors.OrangePrimary` utilisé pour le spinner — OK. |
| Composants | **PARTIEL** | `CircularProgressIndicator` appelé via FQN `org.jellyfin.androidtv.ui.base.CircularProgressIndicator` — import direct serait plus propre. |
| Dimensions | OK | `48.dp` pour le spinner — acceptable pour un indicateur centré. |
| Icônes | N/A | Pas d'icônes dans ce fichier. |

### VideoPlayerControls.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | OK | Utilise `VegafoXColors.OrangePrimary`, `OrangeLight`, `Divider`, `TextPrimary`, `TextSecondary`. |
| Typographie | **NON** | `TextStyle` construits manuellement (l.349-356, l.375-380) avec `fontSize = 13.sp`, `fontFamily = FontFamily.Monospace` — devrait utiliser `VegafoXTypography` ou `JellyfinTheme.typography`. |
| Boutons | OK | Utilise `IconButton` du design system via `PlayerActionBtn`. `ButtonDefaults.colors()` utilisé correctement. |
| Icônes | OK | Toutes via `VegafoXIcons.*` (Play, Pause, Replay10, Forward30, SkipPrevious, SkipNext, Subtitles, Audiotrack, ListBulleted, Hd, Speed, ZoomOutMap). |
| Dimensions | **PARTIEL** | Tailles hardcodées : `6.dp` spacing, `4.dp` seekbar rest, `8.dp` seekbar focus, `28.dp` divider height, `8.dp` divider spacing, `28.dp`/`22.dp` icon sizes. Pas dans `VegafoXDimensions`. |

### VideoPlayerHeader.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | **PARTIEL** | `Color(0xFFFFD700)` hardcodé pour badge HDR (l.210) — devrait être dans `VegafoXColors`. |
| Typographie | **NON** | Multiples `TextStyle` manuels : `fontSize = 36.sp`, `13.sp`, `15.sp`, `12.sp`, `10.sp`. Utilise `BebasNeue` et `FontFamily.Monospace` directement. |
| Badges | **CUSTOM** | `TechBadge` composable custom avec `border()` + `padding()` — pas de composant design system pour les badges. Devrait être extrait dans le design system. |
| Dimensions | **NON** | `32.dp` top padding, `56.dp` horizontal padding dans `PlayerHeader` — hardcodé. `RoundedCornerShape(4.dp)` pour les badges — pas dans les shapes du thème. |

### VideoPlayerDialogs.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Composants | OK | Utilise `PlayerDialog`, `PlayerDialogItem`, `LazyColumn` — cohérent. |
| Dimensions | OK | `DialogDimensions.maxListHeight` (400.dp) — design system respecté. |
| Couleurs | OK | Via `PlayerDialog` interne qui utilise `VegafoXColors.DialogSurface`, `OutlineVariant`. |

### VideoPlayerOverlay.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Tout | OK | Délègue à `PlayerOverlayLayout`, `VideoPlayerHeader`, `VideoPlayerControls`, `MediaToasts`. Pas de style direct. |

### PlayerOverlayLayout.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | OK | `VegafoXColors.BackgroundDeep.copy(alpha=0.90f/0.95f)`, `Color.Transparent`. |
| Dimensions | **NON** | `fillMaxHeight(0.35f)` et `fillMaxHeight(0.45f)` hardcodés — proportions fixes non paramétrables. |
| Boutons | **ATTENTION** | Surcharge du `JellyfinTheme.colorScheme.button = Color.Transparent` dans les contrôles (l.146-148) — modification locale du thème pour rendre les boutons transparents au repos. Fonctionne mais fragile. |

### PlayerSeekbar.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Composants | OK | Délègue au composant `Seekbar` du design system. |
| Couleurs | OK | Via `SeekbarDefaults.colors()` qui lit le thème. |

### SkipOverlayView.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | OK | `VegafoXColors.OrangePrimary` pour texte, bordure, icône. `OrangePrimary.copy(alpha=0.15f)` pour fond. |
| Icônes | OK | `VegafoXIcons.SkipNext`. |
| Typographie | **PARTIEL** | `JellyfinTheme.typography.titleSmall.copy(fontWeight=Bold, fontSize=14.sp)` — modification de style existant, acceptable. |
| Dimensions | **NON** | `RoundedCornerShape(10.dp)`, `60.dp`/`80.dp` padding, `24.dp`/`14.dp` inner padding, `1.5.dp` border — tout hardcodé. |

### NextUpFragment.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | OK | Utilise `VegafoXColors.OrangePrimary`, `OrangeDark`, `Background`, `TextPrimary`, `TextSecondary`, `TextHint`, `BackgroundDeep`. |
| Typographie | **NON** | `TextStyle` manuels avec `BebasNeue`, tailles 12sp, 40sp, 14sp, 13sp. Devrait utiliser les styles typographiques du thème. |
| Boutons | OK | `VegafoXButton` (Ghost variant), `ProgressButton` avec `ButtonDefaults.colors()`. |
| Dimensions | **NON** | `ThumbnailShape = RoundedCornerShape(12.dp)`, `160.dp` thumbnail height, `75.dp` logo height, `24.dp` spacing — hardcodé. |
| Composants | OK | `AsyncImage`, `AppBackground`, `VegafoXButton`, `ProgressButton`. |

### StillWatchingFragment.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | **PARTIEL** | `ExitProgressColor = Color(0x66FF5050)` hardcodé (l.77) — rouge custom non dans VegafoXColors. |
| Typographie | **NON** | Mêmes problèmes que NextUp — `TextStyle` manuels avec `BebasNeue`, multiples tailles. |
| Boutons | OK | `ProgressButton`, `VegafoXButton` (Primary variant). |
| Dimensions | **NON** | `145.dp` thumbnail height, `75.dp` logo height — hardcodé. |

### MediaToast.kt

| Élément | Conforme ? | Détail |
|---------|-----------|--------|
| Couleurs | **PARTIEL** | `Color(0xBF060A0F)` hardcodé pour backgroundColor du toast (l.40). `Color.Black` pour progressBackground. `Color.White.copy(alpha=0.08f)` pour bordure. |
| Composants | OK | Design custom mais cohérent avec le style VegafoX. |
| Dimensions | **NON** | `96.dp` toast size, `4.dp` progress stroke, `16.dp` padding — hardcodé. |

### Résumé design system

| Catégorie | Total violations | Fichiers concernés |
|-----------|-----------------|-------------------|
| Couleurs hardcodées | 5 | VideoPlayerScreen, VideoPlayerHeader, StillWatchingFragment, MediaToast, PlayerOverlayLayout |
| Typographie manuelle | 7 | VideoPlayerControls (×2), VideoPlayerHeader (×5), NextUpFragment (×4), StillWatchingFragment (×3), SkipOverlayView (×1) |
| Dimensions hardcodées | 12+ | Tous sauf VideoPlayerOverlay et PlayerSeekbar |
| Composant manquant | 1 | TechBadge devrait être dans le design system |

---

## Point 3 — Contrôles et navigation D-pad

### Structure des contrôles (VideoPlayerControls.kt)

**Layout vertical (Column)** :
1. **Row 1** : Position (gauche) / Temps restant (droite) — texte non focusable
2. **Row 2** : `PlayerSeekbar` — focusable, hauteur animée 4dp→8dp au focus
3. **Row 3** : Boutons de contrôle — `focusGroup()` + `focusRestorer()`

**Boutons Row 3 (gauche → droite)** :

| # | Bouton | Type | Visibilité |
|---|--------|------|-----------|
| 1 | Play/Pause | `IconButton` primary | Toujours, **autoFocus** via `onVisibilityChanged` |
| 2 | Rewind −10s | `IconButton` | Toujours |
| 3 | Forward +30s | `IconButton` | Toujours |
| 4 | Previous | `IconButton` | Si `entryIndex > 0` |
| 5 | Next | `IconButton` | Si `entryIndex < estimatedSize - 1` |
| — | Divider | `Box` 1×28dp | Toujours |
| 6 | Subtitles | `IconButton` | Toujours (primary si track active) |
| 7 | Audio | `IconButton` | Toujours |
| 8 | Chapters | `IconButton` | Toujours |
| 9 | Quality | `IconButton` | Toujours |
| 10 | Speed | `IconButton` | Toujours |
| 11 | Zoom | `IconButton` | Toujours |

### Focus D-pad

**Focus group** : Row 3 est encadrée par `focusGroup()` avec `focusRestorer()` — le focus est restauré au dernier élément focalisé lors du retour dans la zone.

**AutoFocus** : Le bouton Play/Pause demande le focus via `focusRequester.requestFocus()` dans `onVisibilityChanged` — se déclenche quand l'overlay devient visible.

**Navigation horizontale** : D-pad gauche/droite navigue entre les boutons dans la Row. Le divider (`Box`) n'est pas focusable — il est sauté correctement.

**Navigation verticale** : D-pad haut depuis les boutons monte vers la seekbar, puis vers les textes de temps (non focusables — le focus reste sur la seekbar). D-pad bas depuis la seekbar descend vers les boutons.

**Problème potentiel** : Les boutons Previous/Next sont conditionnels (`if` composables). Quand ils apparaissent/disparaissent, l'ordre de focus peut changer de manière inattendue. Pas de `focusRequester` spécifique pour gérer la transition.

### Raccourcis D-pad hors overlay (PlayerOverlayLayout.kt)

| Touche | Overlay masqué | Overlay visible |
|--------|---------------|-----------------|
| OK / Enter | Play/Pause (toast) | Propagé aux contrôles |
| D-pad Droite | Seek forward (toast) | Propagé aux contrôles |
| D-pad Gauche | Seek backward (toast) | Propagé aux contrôles |
| Back | — | Masque l'overlay |
| Toute autre touche non-système | Affiche l'overlay | Reset timer auto-hide |

**Problème identifié** : D-pad Haut/Bas ne sont pas interceptés quand l'overlay est masqué — pas de contrôle volume via D-pad. Le volume est géré uniquement par les touches système (volume hardware).

**Auto-hide** : L'overlay se masque après 3 secondes (`rememberPlayerOverlayVisibility(timeout = 3.seconds)`). Tout keypress quand l'overlay est visible reset le timer.

---

## Point 4 — Seekbar

### Dimensions

| Propriété | Valeur |
|-----------|--------|
| Largeur | `fillMaxWidth()` |
| Hauteur au repos | `4.dp` |
| Hauteur au focus | `8.dp` (animée via `animateDpAsState`, 200ms) |
| Rayon du knob | `minDimension * 2` (8dp au repos, 16dp au focus) |
| Forme | `RoundedRect` avec `CornerRadius(minDimension)` |

### Couleurs

| Zone | Source |
|------|--------|
| Fond | `JellyfinTheme.colorScheme.rangeControlBackground` |
| Buffer | `JellyfinTheme.colorScheme.seekbarBuffer` |
| Progression | `JellyfinTheme.colorScheme.rangeControlFill` |
| Knob | `JellyfinTheme.colorScheme.rangeControlKnob` |

Toutes les couleurs proviennent du thème — **conforme au design system**.

### Indicateur de position (Knob)

- Cercle dessiné via `drawCircle` dans `drawWithContent`
- Position X = `visibleProgress * size.width`
- Alpha animé : 0f quand non focusé, 1f quand focusé (`animateFloatAsState`)
- Le knob n'est **pas visible** quand la seekbar n'a pas le focus — design épuré

### Marqueurs de chapitres

**NON IMPLÉMENTÉS** : La seekbar ne reçoit pas d'information sur les chapitres. Les chapitres sont uniquement accessibles via le dialog dédié (bouton Chapters).

### Trickplay (aperçu d'image au survol)

**NON IMPLÉMENTÉ** : Aucun mécanisme de preview d'image pendant le scrubbing. Le composable `Seekbar` ne supporte ni `trickplayImages` ni callback de hover/preview.

### Comportement D-pad

| Touche | Action |
|--------|--------|
| D-pad Droite | Avance de `seekForwardAmount` (configurable via `playbackManager.options.defaultFastForwardAmount()`) |
| D-pad Gauche | Recule de `seekRewindAmount` (configurable via `playbackManager.options.defaultRewindAmount()`) |
| KeyDown | Active scrubbing (`onScrubbing(true)`), override visuel du progress |
| KeyUp | Lance un timer de 300ms puis désactive scrubbing (`onScrubbing(false)`) et reset l'override |

Le scrubbing envoie un `seek()` à chaque keyDown, avec un debounce de 300ms sur le cancel. L'incrément est configurable via les options du `PlaybackManager`.

---

## Point 5 — Dialogs Audio, Sous-titres, Vitesse, Qualité, Zoom

### Architecture commune

Tous les dialogs utilisent `PlayerDialog` (wrapper autour de `Dialog`) avec :
- Largeur : `widthIn(min = 380.dp, max = 500.dp)`
- Fond : `VegafoXColors.DialogSurface`
- Bordure : `1.dp VegafoXColors.OutlineVariant`
- Shape : `JellyfinTheme.shapes.dialog`
- Padding vertical : `24.dp`
- Titre centré : `JellyfinTheme.typography.titleLarge`

Chaque item utilise `PlayerDialogItem` :
- Hauteur fixe : `56.dp`
- Focus ring : `2.dp VegafoXColors.FocusRing` au focus
- Scale : `1.04f` au focus, `1f` au repos
- Texte sélectionné : `VegafoXColors.OrangePrimary` + checkmark `✓`
- Fond focusé : `VegafoXColors.SurfaceContainer`

### Dialog Sous-titres

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.Subtitles` dans VideoPlayerControls |
| Options | "Off" (index -1) + pistes du backend (`label ∥ language ∥ Track N`) avec codec |
| Composable liste | `LazyColumn` |
| Composable sélection | `PlayerDialogItem` avec checkmark |
| Hauteur max | `DialogDimensions.maxListHeight` = `400.dp` |
| Auto-scroll | `LaunchedEffect` scrolle à `selectedIndex` à l'ouverture |
| Indicateur actif | Le bouton Subtitles est `isPrimary = true` quand une piste est sélectionnée |

### Dialog Audio

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.Audiotrack` |
| Options | Pistes audio du backend (`label ∥ language ∥ Track N`) avec codec |
| Composable liste | `LazyColumn` |
| Hauteur max | `400.dp` |
| Auto-scroll | Oui |

### Dialog Chapitres

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.ListBulleted` |
| Options | Chapitres de `BaseItemDto.chapters` (`name ∥ Chapter N`) |
| Composable liste | `LazyColumn` |
| Hauteur max | `400.dp` |
| Sélection courante | Basée sur la position de lecture (`startPositionTicks / 10_000 <= currentPosMs`) |
| Auto-scroll | Oui, scroll au chapitre courant |
| Action | Seek à `startPositionTicks / 10_000` ms |

### Dialog Qualité

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.Hd` |
| Options | Liste statique : 200 Mbps, 120 Mbps (4K), 80 Mbps (4K), 40 Mbps (1080p), 20 Mbps (1080p), 10 Mbps (720p), 5 Mbps (480p), 2 Mbps (360p) |
| Composable liste | `LazyColumn` |
| Hauteur max | `400.dp` |
| Persistance | `UserPreferences.maxBitrate` |
| Auto-scroll | Oui |

**Problème identifié** : La liste de qualités est hardcodée dans `buildQualityDialog()` — pas de détection de la résolution réelle ou des profils disponibles sur le serveur.

### Dialog Vitesse

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.Speed` |
| Options | `VideoSpeedController.SpeedSteps` : 0.25x, 0.5x, 0.75x, 1x (Normal), 1.25x, 1.5x, 1.75x, 2.0x |
| Composable liste | `LazyColumn` |
| Hauteur max | `400.dp` |
| **PAS d'auto-scroll** | Le `LazyColumn` n'a pas de `rememberLazyListState` ni de `LaunchedEffect` pour scroller à la sélection |

**Problème identifié** : Le dialog Vitesse ne scroll pas à l'élément sélectionné, contrairement aux autres dialogs.

### Dialog Zoom

| Propriété | Valeur |
|-----------|--------|
| Déclencheur | Bouton `VegafoXIcons.ZoomOutMap` |
| Options | `ZoomMode.entries` (noms formatés : `FIT_SCREEN` → "Fit screen") |
| Composable liste | **`Column` directe** (pas `LazyColumn`) — `modes.forEachIndexed` |
| Hauteur max | **Aucune** — pas de `heightIn` |
| Persistance | `UserPreferences.playerZoomMode` |

**Problème identifié** : Le dialog Zoom utilise un `Column` au lieu de `LazyColumn`, sans contrainte de hauteur. Si le nombre de modes de zoom augmente, le dialog peut dépasser l'écran.

### Cohérence inter-dialogs

| Dialog | LazyColumn ? | Auto-scroll ? | heightIn ? |
|--------|-------------|---------------|------------|
| Subtitles | Oui | Oui | Oui (400dp) |
| Audio | Oui | Oui | Oui (400dp) |
| Chapters | Oui | Oui | Oui (400dp) |
| Quality | Oui | Oui | Oui (400dp) |
| Speed | Oui | **NON** | Oui (400dp) |
| Zoom | **NON** | N/A | **NON** |

---

## Point 6 — Overlays contextuels

### SkipOverlayView.kt

**Architecture** : `AbstractComposeView` (Vue Android classique wrappant du Compose) — pas un composable pur. Utilisé par le legacy `PlaybackController`.

**Types de segments supportés** :

| `segmentType` | Label affiché | String resource |
|---------------|--------------|-----------------|
| `"INTRO"` | "Skip Intro" | `R.string.skip_intro` |
| `"RECAP"` | "Skip Recap" | `R.string.skip_recap` |
| `"COMMERCIAL"` | "Skip Commercial" | `R.string.skip_commercial` |
| `"PREVIEW"` | "Skip Preview" | `R.string.skip_preview` |
| `"OUTRO"` | "Skip Outro" | `R.string.skip_outro` |
| `null` (fallback) | "Skip" | `R.string.segment_action_skip` |

**Mode épisode suivant** : Si `nextEpisodeTitle != null`, affiche "Next episode in Xs" avec compte à rebours basé sur `episodeEndPosition - currentPosition`. Auto-play quand `timeRemaining <= 0`.

**Déclencheur d'affichage** : `visible` est calculé par `skipUiEnabled && targetPosition != null && hasContent && currentPosition <= (targetPosition - SkipMinDuration)`. Mis à jour par le `PlaybackController` legacy qui écrit dans les `MutableStateFlow`.

**Auto-hide** : Pour les skips réguliers (pas épisode suivant), `LaunchedEffect` lance un `delay(AskToSkipAutoHideDuration)` puis met `_targetPosition = null`.

**Style du bouton Skip** :
- Shape : `RoundedCornerShape(10.dp)`
- Fond : `OrangePrimary.copy(alpha=0.15f)`
- Bordure : `1.5.dp OrangePrimary`
- Texte : `OrangePrimary`, `titleSmall` bold 14sp
- Icône : `VegafoXIcons.SkipNext` en `OrangePrimary`
- Animation : `fadeIn()` / `fadeOut()`

**Position** : `Alignment.BottomEnd` avec padding `60.dp, 80.dp`.

**Problème identifié** : Le bouton Skip n'est **pas focusable** — il n'a pas de `focusable()` ni de `clickable()`. L'utilisateur ne peut pas cliquer sur le bouton avec le D-pad. Le skip se fait probablement via une touche physique interceptée par le `PlaybackController` legacy. C'est un pattern de migration incomplète Android View → Compose.

### NextUpFragment.kt

**Composant d'affichage** : `NextUpScreen` (composable plein écran) dans un `Fragment`.

**Layout** :
- Fond noir + backdrop flou à 40% (`AppBackground` dans `graphicsLayer { alpha = 0.40f }`)
- Gradient bottom : transparent → `BackgroundDeep 95%` sur 50% de la hauteur
- Logo série en haut à gauche (75dp)
- Contenu en bas : thumbnail gauche (160dp, 16:9, rounded 12dp avec ombre 16dp) + texte+boutons droite

**Timer compte à rebours** : `Animatable(0f)` anime vers `1f` sur `nextUpTimeout` ms (configurable via `UserPreferences`). Peut être désactivé (`NEXTUP_TIMER_DISABLED`). L'animation est rendue par `ProgressButton.progress`.

**Comportement du timer** : Si l'utilisateur déplace le focus hors du bouton "Watch Now", le timer est reset à 0 via `confirmTimer.snapTo(0f)` dans `onFocusChanged`. Le timer ne progresse que quand "Watch Now" est focusé.

**Actions disponibles** :

| Bouton | Type | Action |
|--------|------|--------|
| Cancel | `VegafoXButton` Ghost | `viewModel.close()` → `goBack()` |
| Watch Now | `ProgressButton` primary + progress | `viewModel.playNext()` → navigate `videoPlayer(0)` |

**Focus** : `focusRequester.requestFocus()` demande le focus initial. `focusRestorer(focusRequester)` sur la Row de boutons et le composant principal.

### StillWatchingFragment.kt

**Composant d'affichage** : `StillWatchingScreen` (composable plein écran).

**Layout** : Identique à NextUp (backdrop flou 40%, gradient bottom, logo, thumbnail+texte).

**Délai d'inactivité** : `TIMEOUT_IN_MS = 10.seconds` — hardcodé (pas configurable par l'utilisateur, contrairement à NextUp).

**Message affiché** : `R.string.still_watching_label` en uppercase, `OrangePrimary`.

**Timer** : `Animatable(0f)` anime vers `1f` sur 10 secondes. Quand terminé, `onCancel()` est appelé (ferme la lecture). Le timer est rendu par `ProgressButton` sur le bouton Exit.

**Comportement inversé vs NextUp** : Le timer mène à l'**arrêt** de la lecture (pas à la continuation). Le bouton primary est "Continue Watching".

**Actions disponibles** :

| Bouton | Type | Action |
|--------|------|--------|
| Exit | `ProgressButton` avec progress rouge | `viewModel.close()` → `goBack()` |
| Continue Watching | `VegafoXButton` Primary | `viewModel.stillWatching()` → navigate `videoPlayer(0)` |

**Problème identifié** : Le timer StillWatching est hardcodé à 10 secondes (`val TIMEOUT_IN_MS = 10.seconds`), déclaré comme `val` au top-level (pas `private`, pas `const`). Devrait être configurable comme NextUp (`UserPreferences`).

**Problème identifié** : Le focus initial va sur "Exit" (premier `focusRequester`), pas sur "Continue Watching". L'utilisateur qui presse OK par réflexe arrêtera la lecture au lieu de la continuer. L'ordre de focus devrait être inversé.

---

## Point 7 — Performance et mémoire

### Libération ExoPlayer

- `VideoPlayerFragment.onStop()` appelle `playbackManager.state.stop()` — arrête la lecture
- Le `PlaybackManager` utilise un `SupervisorJob(parentJob)` — les services sont annulés quand le job parent est annulé
- **Pas de `onDestroy`** explicite dans le fragment pour libérer des ressources
- La libération du backend ExoPlayer dépend du scope Koin et du `SupervisorJob`

**Risque** : Si le fragment est ajouté au back stack sans être détruit, `onStop` est appelé mais pas `onDestroy`. Le `PlaybackManager` reste en scope Koin. Si un nouveau `VideoPlayerFragment` est créé, il réutilise le même `PlaybackManager` (singleton Koin) — OK dans ce cas.

### Annulation des coroutines de position

| Composable | Coroutine | Annulation |
|-----------|-----------|------------|
| `rememberPlayerPositionInfo` | `LaunchedEffect(playing, precision)` | Automatique : annulé quand `playing` change ou composable quitte la composition |
| `rememberPlayerProgress` | `LaunchedEffect(playing, duration)` | Automatique : annulé quand `playing` ou `duration` change |
| `PlayerOverlayLayout` (timer) | `scope.launch { delay(timeout) }` | Annulé manuellement via `timerJob?.cancel()` — **correct** |
| `Seekbar` (scrub cancel) | `scope.launch { delay(300ms) }` | Annulé manuellement via `scrubCancelJob?.cancel()` — **correct** |
| `MediaToastRegistry` | `scope.launch { delay(700ms) }` | Annulé manuellement via `unsetJob?.cancel()` — **correct** |
| `rememberPlaybackManagerMediaToastEmitter` | `LaunchedEffect(playbackManager)` + `launchIn(coroutineScope)` | **PROBLÈME** : le flow est lancé dans `coroutineScope` (pas dans le `LaunchedEffect`), mais il sera quand même annulé quand le composable quitte la composition car `rememberCoroutineScope` est lié à la composition |

### Throttling des recompositions

- **`rememberPlayerPositionInfo`** : Met à jour la position 1×/seconde — throttling correct
- **`rememberPlayerProgress`** : Utilise `Animatable` — pas de recomposition supplémentaire, l'animation est gérée en dehors du cycle de composition
- **`PlayerSeekbar`** : Lit `positionInfo` directement (non-réactif) + `rememberPlayerProgress` pour l'animation — pas de polling excessif
- **`VideoPlayerHeader` badges** : Recalculés via `remember(item)` et `remember(videoSize)` — mémoïsation correcte, pas de recalcul à chaque frame
- **`PositionText` / `RemainingText`** : Utilisent `derivedStateOf` pour ne recomposer que quand le texte formaté change — **correct**

### Fuites mémoire potentielles

| Source | Risque | Détail |
|--------|--------|--------|
| `SkipOverlayView` | **Moyen** | `AbstractComposeView` avec `MutableStateFlow` — si la vue n'est pas détachée, les flows restent actifs. Le `onPlayNext` callback peut capturer un contexte lourd. |
| `MediaToastRegistry` | **Faible** | Créé avec `rememberCoroutineScope()` dans `VideoPlayerScreen` — nettoyé automatiquement. |
| `PlayerSurfaceView` / `PlayerSubtitleView` | **Faible** | `AndroidView` gère la création/destruction. Mais `view.playbackManager = playbackManager` dans `update` crée une référence — si le PlaybackManager survit à la vue, pas de fuite car c'est un singleton Koin. |
| `VideoPlayerFragment.lifecycleScope.launch` (seek) | **Faible** | La coroutine de seek initial (20×50ms) est liée au `lifecycleScope` — annulée quand le fragment est détruit. |

---

## Point 8 — Problèmes visuels potentiels

### Ratio d'aspect vidéo

- `VideoPlayerScreen` : `PlayerSurface` utilise `Modifier.aspectRatio(aspectRatio, videoSize.height < videoSize.width)` — le paramètre `matchHeightConstraintsFirst` adapte le comportement selon l'orientation
- `DefaultVideoAspectRatio = 16f/9f` utilisé quand `aspectRatio.isNaN()` ou `<= 0f`
- **OK** : La surface vidéo s'adapte au ratio réel sans déformation

### Superposition d'overlays

- **Header** : Occupe `fillMaxHeight(0.35f)` depuis le haut avec gradient
- **Controls** : Occupent `fillMaxHeight(0.45f)` depuis le bas avec gradient
- **Overlap** : 0.35 + 0.45 = 0.80 — pas de chevauchement sur les 20% centraux
- **Mais** : Sur des ratios très larges (21:9), la vidéo peut être plus petite et les gradients couvrir une plus grande proportion de la zone utile. Les gradients transparent→opaque atténuent ce problème visuellement.

### Subtitles layer

- `PlayerSubtitles` est positionné **au-dessus** de `VideoPlayerOverlay` dans le z-order (l.78-85 de VideoPlayerScreen)
- Le subtitleView utilise le même `aspectRatio` et `align(Center)` que la surface
- **Problème potentiel** : Les sous-titres en bas d'écran peuvent être masqués par le gradient des contrôles quand l'overlay est visible. Pas de marge de recul dynamique.

### Texte tronqué / débordement

| Composable | Protection | Risque |
|-----------|-----------|--------|
| `VideoPlayerHeader` titre principal | `maxLines = 1`, `TextOverflow.Ellipsis` | OK |
| `VideoPlayerHeader` série | `maxLines = 1`, `TextOverflow.Ellipsis` | OK |
| `VideoPlayerHeader` épisode | `maxLines = 1`, `TextOverflow.Ellipsis` | OK |
| `VideoPlayerHeader` badges | Pas de contrainte de largeur | **RISQUE** : Si beaucoup de badges (resolution + HDR + codec + container + channels + audio codec = 6 badges), la Row peut dépasser la largeur disponible. Pas de `horizontalScroll` ni de wrapping. |
| `NextUpOverlay` titre | `maxLines = 2`, `TextOverflow.Ellipsis` | OK |
| `StillWatchingOverlay` titre | `maxLines = 2`, `TextOverflow.Ellipsis` | OK |
| `PlayerDialogItem` texte | `maxLines = 1`, `TextOverflow.Ellipsis` | OK |
| `PositionText` / `RemainingText` | Pas de `maxLines` | **Faible** : Texte formaté court (`HH:MM:SS`), pas de risque réel. |

### Header positionnement sur ratios vidéo

- `PlayerHeader` : `padding(top = 32.dp, start = 56.dp, end = 56.dp)` — valeurs fixes
- Le conteneur header utilise `.overscan()` (modifier custom pour les TV qui ont un overscan)
- **OK pour la plupart des ratios** : Les valeurs de padding sont suffisantes pour les TV modernes

### Indicateur de buffering

- Centré sur l'écran (`Modifier.align(Alignment.Center)`)
- `AnimatedVisibility` avec `fadeIn`/`fadeOut`
- `48.dp` de diamètre — visible mais discret
- **OK** : Pas d'impact sur le ratio vidéo

---

## Résumé des problèmes identifiés

### Sévérité haute

| # | Problème | Fichier | Ligne |
|---|---------|---------|-------|
| H1 | Le bouton Skip n'est pas focusable/cliquable au D-pad | `SkipOverlayView.kt` | 78-111 |
| H2 | StillWatching : focus initial sur "Exit" au lieu de "Continue Watching" | `StillWatchingFragment.kt` | 288-314 |
| H3 | Seek initial brut : 20 commandes seek en 1 seconde | `VideoPlayerFragment.kt` | 56-63 |

### Sévérité moyenne

| # | Problème | Fichier | Ligne |
|---|---------|---------|-------|
| M1 | Pas de contrôle volume D-pad (haut/bas ignorés) | `PlayerOverlayLayout.kt` | 56-91 |
| M2 | Dialog Speed : pas d'auto-scroll à la sélection | `VideoPlayerDialogs.kt` | 270-285 |
| M3 | Dialog Zoom : Column sans LazyColumn ni heightIn | `VideoPlayerDialogs.kt` | 298-309 |
| M4 | Seekbar : pas de marqueurs de chapitres | `PlayerSeekbar.kt` | — |
| M5 | Seekbar : pas de trickplay (preview images) | `PlayerSeekbar.kt` | — |
| M6 | StillWatching timeout hardcodé 10s, non configurable | `StillWatchingFragment.kt` | 74 |
| M7 | Badges techniques peuvent déborder horizontalement | `VideoPlayerHeader.kt` | 149-159 |
| M8 | Sous-titres masquables par le gradient des contrôles | `VideoPlayerScreen.kt` | 78-85 |
| M9 | Qualité : profils hardcodés, pas de détection serveur | `VideoPlayerControls.kt` | 460-474 |

### Sévérité basse (design system)

| # | Problème | Fichier | Ligne |
|---|---------|---------|-------|
| L1 | `Color.Black` au lieu de `VegafoXColors.BackgroundDeep` | `VideoPlayerScreen.kt` | 62 |
| L2 | `Color(0xFFFFD700)` hardcodé pour badge HDR | `VideoPlayerHeader.kt` | 210 |
| L3 | `Color(0x66FF5050)` hardcodé pour ExitProgressColor | `StillWatchingFragment.kt` | 77 |
| L4 | `Color(0xBF060A0F)` hardcodé pour toast background | `MediaToast.kt` | 40 |
| L5 | Typographie manuelle (TextStyle) au lieu de VegafoXTypography | Multiples fichiers | — |
| L6 | Dimensions de padding/spacing non centralisées | Multiples fichiers | — |
| L7 | `TechBadge` composable devrait être dans le design system | `VideoPlayerHeader.kt` | 203-231 |
| L8 | SkipOverlayView est un `AbstractComposeView` (legacy) | `SkipOverlayView.kt` | 114-241 |
| L9 | `TIMEOUT_IN_MS` est un top-level public val | `StillWatchingFragment.kt` | 74 |
| L10 | `CircularProgressIndicator` appelé via FQN | `VideoPlayerScreen.kt` | 95-98 |
