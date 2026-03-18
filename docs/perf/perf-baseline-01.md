# Performance Baseline — perf-baseline-01

**Date**: 2026-03-17
**Device**: Ugoos AM9 Pro (192.168.1.152)
**Build**: debug, arm64-v8a
**Server**: JellyMox v10.11.6 (192.168.1.60:8096)
**Test**: Cold start (force-stop + fresh launch)

## Startup Timeline (before any optimization)

| Step | Time (ms) | Cumulative | Notes |
|------|-----------|------------|-------|
| KoinInitializer startKoin | 139 | 139 | Module loading |
| KoinInitializer singletons | 214 | 353 | TvManager, ItemLauncherHelper |
| StartupActivity.onCreate layout | 89 | ~440 | Splash screen |
| Session detection | 1229 | ~1670 | Wait for SessionRepository READY |
| onSessionStart | 1 | ~1670 | Fire-and-forget |
| Prefetch start | 5 | ~1675 | Background IO |
| StartupActivity → MainActivity | 30 | ~1705 | Intent + finishAfterTransition |
| MainActivity.onCreate | 69 | ~1775 | |
| HomeViewModel init | 449 | ~2225 | Fragment + ComposeView creation |
| **Prefetch consume** | **7** | ~2232 | **data=false — prefetch too slow!** |
| **currentUser wait** | **2330** | ~4562 | **Blocking on userRepository** |
| Views fetch | 4795 | ~9360 | API: user views |
| LiveTV check | 8599 | ~12960 | API: recommended programs |
| **First row visible** | **16454** | ~18680 | All rows arrive simultaneously |
| **Total T0→T3** | **18792** | | |

## Key Bottlenecks Identified

### 1. Prefetch arrives too late (data=false)
- Prefetch starts 550ms before HomeViewModel init
- getResumeItems alone takes 4.6s+ → prefetch never completes in time
- **Impact**: Entire prefetch system is useless

### 2. currentUser blocking wait (2330ms)
- `userRepository.currentUser.filterNotNull().first()` blocks loadRows
- All section loading is delayed by 2.3 seconds
- **Impact**: Adds 2.3s to every cold start

### 3. enrichEpisodesWithSeriesGenres blocks RESUME row
- Called inline in `loadMergedContinueWatching()`
- Blocks the continue_watching row until genre enrichment completes
- **Impact**: Delays the most important row by ~600ms

### 4. Sequential loadAdditionalData in ItemDetailsViewModel
- For SERIES: loadSeasons → loadNextUp → loadSimilar (all sequential)
- For EPISODE: loadEpisodes → loadSimilar (sequential)
- **Impact**: Detail page content loads slower than necessary

### 5. Server API response times
- getResumeItems: 4.6-8.5 seconds
- getNextUp: 4-14 seconds
- LiveTV check: 3.8-8.5 seconds
- **Impact**: Server-bound, cannot be optimized client-side

## Item Detail Page (no optimization)
- No measurements available (instrumentation added during optimization)

## Environment Notes
- All measurements taken on Ugoos AM9 Pro via ADB
- Server on same LAN (192.168.1.60)
- ~68 items in continue_watching, 7 rows total, 157 items
- Cache age varies between runs
