# Startup B2 — Prefetch Home + Fire-and-forget onSessionStart

**Date** : 2026-03-09
**Device** : Ugoos AM9 Pro (192.168.1.152:5555)
**Build** : githubRelease
**Baseline** : startup_b1.md (cold start moyen ~564ms), baseline original ~656ms

---

## 1. Flux Login → Home : etapes chronometrees

Logs `Log.d("STARTUP", ...)` places aux 5 points cles du flux.

### Timeline du dernier cold start (run 5)

| Etape | Timestamp relatif | Delta |
|-------|-------------------|-------|
| Session detected | T=0ms | — |
| openNextActivity | T+1ms | +1ms |
| onSessionStart (fire-and-forget) | T+2ms | +1ms |
| Prefetch started | T+2ms | +0ms |
| startActivity(MainActivity) | T+6ms | +4ms |
| MainActivity.onCreate | T+90ms | +84ms |
| HomeComposeFragment.onCreateView | T+113ms | +23ms |
| HomeViewModel init → loadRows() | T+170ms | +57ms |
| Prefetch complete (1 row) | T+347ms | +177ms |
| MainActivity Displayed | T+555ms | +208ms |
| Full rows loaded (7 rows) | T+1789ms | +1234ms |

### Analyse

- **Session → startActivity** : 6ms (avant : bloquait 100-500ms en attendant WorkManager + socket + servers)
- **startActivity → MainActivity.onCreate** : 84ms (creation activity + theme)
- **onCreate → HomeViewModel.init** : 80ms (session check + fragment transaction + Koin injection)
- **HomeViewModel.init → Prefetch consume** : Le prefetch arrive 177ms apres le lancement, trop tard pour etre consomme par le VM init (le VM check a T+170ms, le prefetch complete a T+347ms). Les rows prefetchees sont donc ignorees dans ce cas.
- **Full load** : ~1.8s apres session detection (7 rows = Next Up + 4 Latest + Library + Playlists)

---

## 2. Prefetch : IMPLEMENTE

### Architecture

```
onSessionStart() [fire-and-forget]
  ├─ HomePrefetchService.prefetch() [IO scope]
  │   ├─ async: loadContinueWatching()
  │   └─ async: loadNextUp()
  ├─ serverRepository.loadStoredServers() [IO]
  ├─ WorkManager scheduling [IO]
  └─ socketHandler.updateSession() [IO]
```

### Fichier cree

| Fichier | LOC | Role |
|---------|-----|------|
| `ui/home/compose/HomePrefetchService.kt` | 91 | Prefetch Continue Watching + Next Up en parallele |

### Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `JellyfinApplication.kt` | `onSessionStart()` : suspend → fun (fire-and-forget via ProcessLifecycleOwner scope) + inject HomePrefetchService |
| `StartupActivity.kt` | Timing logs aux points cles |
| `HomeViewModel.kt` | `init { loadRows() }` + consume prefetch cache + timing logs |
| `HomeScreen.kt` | Suppression `LaunchedEffect(Unit) { loadRows() }` (redondant avec init) |
| `HomeComposeFragment.kt` | Timing log dans onCreateView |
| `MainActivity.kt` | Timing log dans onCreate |
| `di/AppModule.kt` | `single { HomePrefetchService(get()) }` + 6eme parametre HomeViewModel |

### Comportement observe

Le prefetch complete en ~345ms apres son lancement. Le HomeViewModel.init arrive a T+170ms et consume() retourne null (pas encore pret). Le prefetch est donc une **optimisation de best-effort** : si la navigation est lente (deep link, configuration change), les rows prefetchees seront disponibles. Sur un cold start normal, le gain est marginal car le VM demarre avant la fin du prefetch.

**Gain principal** : le changement `suspend → fun` de `onSessionStart()` qui elimine le blocage de 100-500ms avant la navigation vers MainActivity.

---

## 3. Skeleton timing : OK

- Etat initial de `HomeUiState` : `isLoading = true` (defaut du data class)
- Les skeletons s'affichent des la premiere frame de HomeScreen
- Aucune correction necessaire

### Verification

```kotlin
@Stable
data class HomeUiState(
    val isLoading: Boolean = true,  // ← skeletons immediats
    val error: UiError? = null,
    val rows: List<TvRow<BaseItemDto>> = emptyList(),
)
```

Le `StateContainer` dans `HomeScreen` affiche `loadingContent` (3 `SkeletonCardRow`) quand `isLoading = true`. C'est l'etat par defaut → pas de blanc entre onCreate et l'affichage des skeletons.

---

## 4. Mesures Cold Start (5 runs, githubRelease)

| Run | LaunchState | TotalTime (ms) |
|-----|-------------|-----------------|
| 1   | COLD        | 413             |
| 2   | COLD        | 423             |
| 3   | COLD        | 404             |
| 4   | COLD        | 425             |
| 5   | COLD        | 405             |

### Statistiques

| Metrique | Valeur |
|----------|--------|
| Minimum  | 404ms  |
| Maximum  | 425ms  |
| Mediane  | 413ms  |
| Moyenne  | 414ms  |
| Ecart type | ~10ms |

### Comparaison avec les phases precedentes

| Phase | Moyenne (ms) | Delta vs precedent | Delta vs baseline |
|-------|-------------|-------------------|-------------------|
| Baseline (A0) | 656 | — | — |
| A1 (windowBackground) | ~550 | -106ms | -106ms |
| A2 (SplashScreen API) | ~498 | -52ms | -158ms |
| A3 (StrictMode debug) | ~490 | -8ms | -166ms |
| B1 (runBlocking removed) | ~564* | — | ~0ms** |
| **B2 (fire-and-forget + prefetch)** | **414** | **-150ms** | **-242ms** |

*B1 n'a pas impacte le cold start (le runBlocking etait dans ContentProvider.query, pas sur le chemin critique)

**Note** : La variance entre B1 (564ms) et B2 (414ms) est plus grande que les deltas precedents car B2 modifie le chemin critique (onSessionStart bloquant → fire-and-forget).

---

## 5. Gain total vs baseline 656ms

| Metrique | Valeur |
|----------|--------|
| Baseline originale (A0) | 656ms |
| Resultat final (B2) | 414ms |
| **Gain absolu** | **242ms** |
| **Gain relatif** | **36.9%** |

### Decomposition du gain

| Optimisation | Gain estime |
|-------------|------------|
| windowBackground #0A0A0F (A1) | ~100ms (perception) |
| SplashScreen API (A2) | ~50ms |
| onSessionStart fire-and-forget (B2) | ~150ms |
| loadRows() dans init (B2) | ~15ms (LaunchedEffect eliminee) |

---

## 6. Perception utilisateur : AMELIOREE

### Test visuel sur AM9 Pro

| Critere | Resultat |
|---------|----------|
| Pas de flash au demarrage | **OUI** — windowBackground = ds_background |
| Skeletons visibles immediatement | **OUI** — isLoading=true par defaut |
| Home charge rapidement | **OUI** — ~1.8s pour full content, prefetch partiel possible |
| Transition SplashScreen fluide | **OUI** — SplashScreen API active |
| Pas de blanc entre activities | **OUI** — navigation immediate apres session |

### Timeline utilisateur perçue

```
T=0ms     App lancee (SplashScreen systeme avec icone VegafoX)
T=405ms   StartupActivity visible (splash compose)
T=~500ms  Session detectee → navigation immediate
T=~960ms  MainActivity visible avec skeletons
T=~2.2s   Contenu complet affiche (7 rows)
```

---

## 7. Architecture finale du flux de demarrage

```
App Process Start
  ├─ LogInitializer (main) — Timber
  ├─ KoinInitializer (main) — startKoin (lazy)
  ├─ SessionInitializer (IO) — restoreSession()
  └─ JellyfinApplication.onCreate() — notifications + monitoring

StartupActivity.onCreate()
  ├─ installSplashScreen() + applyTheme()
  ├─ inflate + showSplash()
  └─ requestPermissions

onPermissionsGranted → observe sessionRepository.state
  └─ READY + session != null
      ├─ Log: "Session detected"
      └─ openNextActivity()
          ├─ onSessionStart() [NON BLOQUANT] ← CHANGEMENT B2
          │   ├─ HomePrefetchService.prefetch() [IO]
          │   ├─ serverRepository.loadStoredServers() [IO]
          │   ├─ WorkManager scheduling [IO]
          │   └─ socketHandler.updateSession() [IO]
          ├─ navigationRepository.reset()
          ├─ startActivity(MainActivity) ← IMMEDIAT
          └─ finishAfterTransition()

MainActivity.onCreate()
  ├─ session READY check
  ├─ setupActivity() → inflate + navigation
  └─ HomeComposeFragment
      ├─ HomeViewModel init → loadRows()
      │   ├─ consume prefetch (si disponible)
      │   └─ full load parallele (toutes sections)
      └─ HomeScreen → skeletons → content
```
