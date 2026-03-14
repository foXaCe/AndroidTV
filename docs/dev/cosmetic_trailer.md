# Hero Trailer Auto-Play

## Description

Quand le focus reste 5 secondes sur une card qui possede une bande-annonce YouTube,
le trailer remplace le backdrop du Hero en fond avec un crossfade.
Aucun cadre, aucune fenetre separee — le trailer joue directement dans le backdrop fullscreen.

## Source trailer

- **remoteTrailers** (BaseItemDto) — URLs YouTube configurees dans Jellyfin
- Resolution via `TrailerResolver.resolveTrailerFromItem()` qui :
  1. Extrait le videoId YouTube
  2. Recupere les segments SponsorBlock (skip intros/sponsors)
  3. Resolve le stream direct via NewPipe Extractor (H.264 >= 1080p + AAC audio)

## Flux

1. Card focusee → check instant `remoteTrailers` non vide + YouTube videoId valide
2. Si oui → `isCountingDown = true` + resolution stream en parallele (async IO)
3. Timer bar orange progresse de 0 a 100% sur 5s (LinearEasing)
4. A 5s → await resolution → `isPlaying = true` si stream OK
5. ExoPlayerTrailerView entre en composition, `onVideoReady` → crossfade IN 800ms
6. Gradients (vertical + horizontal) restent au-dessus du video → texte lisible
7. Badge sourdine (TvFocusCard) apparait en TopEnd du hero
8. Focus quitte la card → `stopTrailer()` → crossfade OUT 400ms → backdrop image
9. Video ends → `onTrailerEnded` → stopTrailer()

## Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/HomeViewModel.kt` | `TrailerState` data class, `_trailerState` StateFlow, `startTrailerCountdown()`, `stopTrailer()`, `toggleMute()` |
| `ui/home/compose/HomeHeroBackdrop.kt` | Accept `trailerState` + `onTrailerEnded`. Layer 2 ExoPlayerTrailerView entre image et gradients. Timer bar en Layer 5. Cached stream info pour fade-out propre. |
| `ui/home/compose/HomeScreen.kt` | Collecte `trailerState`. Passe au backdrop. Badge sourdine `MuteBadge` composable (TvFocusCard focusable D-pad). `onBlur` callback sur BrowseMediaCard. |
| `ui/browsing/compose/BrowseMediaCard.kt` | Nouveau param `onBlur: (() -> Unit)?`. `onFocusChanged` combiné focus/blur. |
| `ui/home/mediabar/ExoPlayerTrailerView.kt` | `crossfadeInMs`/`crossfadeOutMs` asymetrique. `LaunchedEffect(muted)` sync dynamique. Resize mode ZOOM fixe. |

## Player lifecycle

- ExoPlayer cree dans `DisposableEffect(streamInfo.videoUrl)` de ExoPlayerTrailerView
- `onDispose` → player.release() + cleanup SponsorBlock handler
- `showTrailerView` garde le composable en composition pendant le fade-out (delay 450ms)
- Pas de fuite : cancel du `trailerJob` dans `stopTrailer()` + DisposableEffect cleanup

## Comportements

| Action | Resultat |
|--------|----------|
| Focus card 0→5s | Timer bar orange progresse, resolution en parallele |
| 5s atteint | Crossfade 800ms → trailer joue muet en fond |
| Navigation rapide entre cards | Timer annule, aucun trailer ne demarre |
| OK sur badge sourdine | Son active/desactive dynamiquement |
| Focus quitte card (autre card) | stopTrailer → crossfade OUT 400ms → backdrop |
| Focus vers pill (bas) | onBlur → stopTrailer → arret immediat |
| Fin du trailer | onTrailerEnded → stopTrailer |
| Pas de remoteTrailers | Rien ne se passe, pas de timer bar |

## Build

- `assembleDebug` : OK
- `assembleGithubRelease` : OK
- Installe debug + release sur AM9 Pro : OK
