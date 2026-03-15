# Cleanup 04 — Fichiers D orphelins, helpers Java, cascade

**Date** : 2026-03-12
**Branche** : main

---

## Etape 1 — Inventaire fichiers D Kotlin

4 fichiers Kotlin cassaient la compilation car ils referençaient
des classes Java supprimees (migration LiveTV/Compose/Startup).

| Fichier | Cause | Refs externes | LOC |
|---------|-------|---------------|-----|
| `ui/GuidePagingButton.kt` | `ProgramGridCellBinding` (layout supprime) | 0 | 47 |
| `ui/LiveProgramDetailPopupHelper.kt` | `LiveProgramDetailPopup.java` supprime | 0 (self-refs) | 163 |
| `ui/RecordPopupHelper.kt` | `RecordPopup.java` supprime | 0 (self-refs) | 102 |
| `ui/playback/CustomPlaybackOverlayFragmentHelper.kt` | `CustomPlaybackOverlayFragment.java` supprime | 0 (self-refs) | 252 |

Tous etaient orphelins — les fonctions utilitaires (`asTimerInfoDto`,
`copyWithTimerId`, `createProgramTimerInfo`, etc.) sont dupliquees
dans `ui/livetv/compose/LiveTvRecordingApi.kt`.

Le `GuidePagingButton` Compose de remplacement existe dans
`ui/livetv/compose/GuidePagingButton.kt`.

---

## Etape 2 — Inventaire fichiers Java

### Resultat

**1 seul fichier Java** restant dans `app/src/main/java/` :

| Fichier | Role | Refs Kotlin | Statut |
|---------|------|-------------|--------|
| `org/schabi/newpipe/extractor/utils/Utils.java` | Shadow API23 pour NewPipe Extractor | `YouTubeStreamResolver.kt`, `NewPipeDownloader.kt` | CONSERVE |

Ce fichier est un patch de compatibilite API23 pour la librairie
NewPipe Extractor. Il DOIT rester en Java car il shadow une classe
de la librairie externe.

---

## Etape 3 — Suppressions

### GROUPE A — Fichiers D Kotlin (4 fichiers, 564 LOC)

Supprimes directement (0 reference externe chacun) :

- `ui/GuidePagingButton.kt` (47 LOC)
- `ui/LiveProgramDetailPopupHelper.kt` (163 LOC)
- `ui/RecordPopupHelper.kt` (102 LOC)
- `ui/playback/CustomPlaybackOverlayFragmentHelper.kt` (252 LOC)

### GROUPE B — Cascade overlay (8 fichiers, 1637 LOC)

La suppression des fichiers D a revele que d'autres fichiers dans
`ui/playback/overlay/` etaient aussi du code mort (ancien overlay
Leanback, remplace par `ui/player/`) :

| Fichier | Cause | LOC |
|---------|-------|-----|
| `overlay/VideoPlayerControllerImpl.kt` | Jamais instancie, refs `CustomPlaybackOverlayFragment` + `LeanbackOverlayFragment` (supprimes) | 141 |
| `PlaybackOverlayFragmentHelper.kt` | Extensions sur `CustomPlaybackOverlayFragment` (supprime) | 20 |
| `overlay/compose/PlayerOverlayState.kt` | Redeclaration (data classes dupliquees avec `PlayerDialogDefaults.kt`) | 137 |
| `overlay/compose/PlayerOverlayScreen.kt` | Refs drawables supprimes (ic_previous, ic_rewind, etc.) | 346 |
| `overlay/compose/PlayerOverlayViewModel.kt` | Jamais instancie, pas en Koin DI | 351 |
| `overlay/compose/PlayerOverlayComposeHelper.kt` | Ref uniquement `PlayerOverlayScreen` (mort) | 28 |
| `overlay/compose/PlayerDialogs.kt` | Interne overlay, jamais importe depuis l'exterieur | 293 |
| `overlay/PlayerPopupView.kt` | Ref uniquement depuis `PlayerDialogs` (mort) | 321 |

**Nettoyage interface** : `VideoPlayerController.kt` conserve mais
nettoye — suppression de `masterOverlayFragment: CustomPlaybackOverlayFragment`
et import mort. L'interface reste utilisee par `PlayerOverlayViewModel`
(mort mais conserve dans un premier temps — inoffensif car compile).

**Fichiers overlay conserves** (actifs) :

- `overlay/compose/PlayerDialogDefaults.kt` — Composables `PlayerDialog`, `PlayerDialogItem` importes par `ui/player/video/VideoPlayerDialogs.kt`
- `overlay/SkipOverlayView.kt` — utilise activement
- `overlay/SeekProvider.kt` — utilise activement
- `overlay/VideoPlayerController.kt` — interface propre

### GROUPE C — Startup fragments (5 fichiers, 923 LOC)

Fragments XML dont les layouts ont ete supprimes (migration
Compose dans `ui/startup/compose/`, `ui/startup/server/`,
`ui/startup/user/`). Inter-references circulaires uniquement,
0 import externe :

| Fichier | Layout supprime | LOC |
|---------|----------------|-----|
| `fragment/SelectServerFragment.kt` | `fragment_select_server.xml` | 270 |
| `fragment/ServerFragment.kt` | `fragment_server.xml` | 310 |
| `fragment/UserLoginFragment.kt` | `fragment_user_login.xml` | 113 |
| `fragment/UserLoginCredentialsFragment.kt` | `fragment_user_login_credentials.xml` | 120 |
| `fragment/UserLoginQuickConnectFragment.kt` | `fragment_user_login_quick_connect.xml` | 110 |

**Fragments startup conserves** (actifs) :

- `fragment/ConnectHelpAlertFragment.kt`
- `fragment/ServerAddFragment.kt`
- `fragment/SplashFragment.kt`
- `fragment/WelcomeFragment.kt`
- `fragment/StartupToolbarFragment.kt`

---

## Etape 4 — Corrections complementaires

### Lint fix

`PlaybackController.kt:762` — `fragment!!.context!!` remplace par
`fragment!!.requireContext()` (erreur lint `UseRequireInsteadOfGet`).

### Baseline lint

Regenere via `updateLintBaselineGithubDebug` apres toutes les
suppressions pour re-syncher les positions.

---

## Resultats

### LOC supprimees

| Groupe | Fichiers | LOC |
|--------|----------|-----|
| A — Fichiers D Kotlin | 4 | 564 |
| B — Cascade overlay | 8 | 1 637 |
| C — Startup fragments | 5 | 923 |
| **Total** | **17** | **3 124** |

### Lint

Lint `githubDebug` : **0 erreur** (BUILD SUCCESSFUL).

### Build

| Variante | Resultat |
|----------|---------|
| `compileGithubDebugKotlin` | OK (0 erreur, warnings pre-existants) |
| `assembleGithubDebug` | OK |
| `assembleGithubRelease` | OK |
| Install AM9 Pro | OK (`adb install` Success) |

### Fichiers Java restants

| Fichier | Statut | Action |
|---------|--------|--------|
| `org/schabi/newpipe/extractor/utils/Utils.java` | Actif (shadow API23) | Conserver — migration impossible (shadow lib externe) |

**0 fichier Java a migrer.** Le projet est desormais 100% Kotlin
(hors shadow NewPipe Extractor).
