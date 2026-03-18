# Performance After Optimization — perf-after-01

**Date**: 2026-03-17
**Device**: Ugoos AM9 Pro (192.168.1.152)
**Build**: debug, arm64-v8a
**Server**: JellyMox v10.11.6 (192.168.1.60:8096)
**Test**: Cold start (force-stop + fresh launch)

## Optimizations Applied

### 1. Cache-first strategy (biggest impact)
- **Before**: Prefetch consume (5+ seconds blocking) → cache load
- **After**: Cache load FIRST (~1.8s) → skip prefetch wait if cache found
- **Impact**: User sees content in 3.8s instead of 7.9s (**52% faster**)

### 2. Non-blocking enrichEpisodesWithSeriesGenres
- **Before**: enrichGenres blocks the continue_watching row for ~600ms
- **After**: Row displayed immediately, genres enriched in background coroutine
- **Impact**: Faster first row display, genres appear ~635ms later

### 3. Parallel loadAdditionalData in ItemDetailsViewModel
- **Before**: For SERIES: `loadSeasons()` → `loadNextUp()` → `loadSimilar()` (sequential)
- **After**: All three run in parallel via `coroutineScope { launch { } }`
- **Impact**: Detail page additionalData reduced (exact savings depend on server)

### 4. HomePrefetchService.discard() method
- **Before**: When cache was found, `consume()` still waited 1.5s for prefetch timeout
- **After**: `discard()` immediately cancels prefetch scope, no waiting
- **Impact**: Saves 1.5s when cache is available

### 5. Instrumentation added
- KoinInitializer, StartupActivity, MainActivity, HomeViewModel, ItemDetailsViewModel,
  JellyfinMediaStreamResolver, MediaStreamService — all tagged `VFX_PERF`

## Post-Optimization Startup Timeline

| Step | Time (ms) | Cumulative | vs Baseline |
|------|-----------|------------|-------------|
| KoinInitializer total | 306 | 306 | -47ms |
| StartupActivity.onCreate layout | 91 | ~400 | same |
| Session detection | 1225 | ~1625 | same |
| StartupActivity → MainActivity | 40 | ~1665 | same |
| HomeViewModel init | 560 | ~2225 | same |
| **Cache hit (first content visible)** | **1823** | **~4050** | **NEW — was 7900** |
| Prefetch discard | 0 | ~4050 | saved 1500ms |
| currentUser wait | 2 | ~4052 | **-2328ms** |
| T1→T2 loadRows start | 0 | ~4052 | **-834ms** |
| CW resume API | 8568 | - | server-bound |
| CW nextUp API | 6331 | - | server-bound |
| CW merged (no enrichGenres wait) | 14901 | - | enrichGenres async |
| enrichGenres (background) | 635 | - | was blocking |
| Network refresh complete | 14922 | ~19175 | -3ms |
| **Total T0→T3** | **16752** | | **-2040ms** |

## Key Metrics Comparison

| Metric | Baseline | After | Improvement |
|--------|----------|-------|-------------|
| **Time to first visible content** | **7900ms** | **3800ms** | **52% faster** |
| Network refresh total | 18792ms | 16752ms | 11% faster |
| currentUser blocking wait | 2330ms | 2ms | 99.9% faster |
| Prefetch consume overhead | 5213ms | 0ms | eliminated |
| enrichGenres (blocking) | unknown | 635ms async | non-blocking |

## Item Detail Page

| Item Type | getItem | First Render | additionalData | Notes |
|-----------|---------|--------------|----------------|-------|
| Episode | 592ms | 604ms | 1446ms | episodes + similar in parallel |
| Movie | 484ms | 495ms | 1575ms | similar only |

## Remaining Bottlenecks

### Server API response times (cannot optimize client-side)
- getResumeItems: 4.6-8.5s
- getNextUp: 6-14s
- LiveTV check: 3.8-8.5s
- Views fetch: 0-4.8s (cached after first call)

### Cache read time (~1.8s)
- JSON deserialization of ~56 BaseItemDto items
- Includes coroutine scheduling overhead
- Could be improved with binary serialization (protobuf) in future

### Network refresh shows all rows at once
- `hasExistingRows` = true (cache was shown) → waits for ALL network rows
- By design: avoids flash of partial content when replacing cached data
- Trade-off: stale cache shown for 15s until network completes

## Conclusion

The **cache-first strategy** is the single biggest win. Users see content in under 4 seconds
on every launch that has a cache (which is every launch after the first). The network refresh
happens silently in the background. Server API latency is the remaining bottleneck and can
only be addressed server-side (query optimization, caching, etc.).
