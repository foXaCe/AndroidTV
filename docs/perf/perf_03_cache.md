# Perf Fix #03 — Home Rows API Cache

**Date** : 2026-03-11
**Build** : VegafoX github-debug + github-release

---

## Problème

Identifié dans `perf_01_diagnostic.md` et `perf_02_progressive.md` :
chaque démarrage (froid ou tiède) refait **tous les appels réseau** depuis zéro.
Même avec le chargement progressif (#02), l'utilisateur attend 6-12 secondes
avant de voir la première row.

**Cause** : aucun cache local des données API. Le `HomePrefetchService`
ne termine jamais avant `consume()` (confirmé : `data=false` à 100%).

---

## Solution implémentée

### Architecture : cache-then-network

```
Cold start (pas de cache)     Cold start (avec cache)
─────────────────────────     ──────────────────────────
T0  HomeViewModel init        T0  HomeViewModel init
T1  prefetch=false            T1  prefetch=false
    → isLoading=true              → load cache (<50ms)
    → skeletons visibles          → rows affichées immédiatement
T2  coroutines réseau         T2  coroutines réseau (background)
... 6-12s ...                 ... 6-12s (invisible) ...
T3  rows réseau affichées     T3  rows réseau remplacent le cache
    → cache sauvegardé            → cache mis à jour
```

### Fichiers créés

#### `HomeRowsCache.kt`

Cache disque basé sur SharedPreferences + kotlinx.serialization.
- `save(rows)` — sérialise `List<TvRow<BaseItemDto>>` en JSON via `@Serializable` wrappers
- `load()` — retourne `(rows, timestamp)` ou `null`
- `invalidate()` — supprime le cache

**BaseItemDto** est `@Serializable` nativement (Jellyfin SDK `$$serializer`).
Pas besoin de modèle intermédiaire — sérialisation directe, complète, sûre.

### Fichiers modifiés

#### `HomeViewModel.kt`

- Ajout paramètre `homeRowsCache: HomeRowsCache` au constructeur
- Ajout champ `isRefreshing: Boolean` au `HomeUiState`
- `loadRows()` modifié :
  1. Vérifie prefetch (priorité 1)
  2. Si pas de prefetch → charge le cache local (priorité 2)
  3. Si cache < 5 min → affiche silencieusement, refresh réseau en arrière-plan
  4. Si cache > 5 min → affiche + indicateur "Mise à jour...", refresh réseau
  5. Si pas de cache → skeletons (comportement original)
  6. Après chargement réseau → sauvegarde dans le cache
- `invalidateCache()` — exposé pour invalider sur lecture

#### `HomeScreen.kt`

- Ajout indicateur "Mise à jour..." (texte discret orange, en haut des rows)
  quand `isRefreshing = true` (cache stale > 5 min)

#### `HomeComposeFragment.kt`

- `launchItem()` appelle `homeViewModel.invalidateCache()` avant de lancer la lecture
- Le prochain retour au home fera un chargement réseau frais

#### `AppModule.kt`

- Enregistrement Koin : `single { HomeRowsCache(androidContext()) }`
- HomeViewModel reçoit `HomeRowsCache` via injection

#### Strings

- `home_refreshing` ajouté en EN ("Updating…") et FR ("Mise à jour…")

### Timers VFX_PERF ajoutés

- `T_cache_hit` — temps depuis T0 jusqu'à l'affichage du cache (attendu <50ms)
- `T_cache_save` — temps de sérialisation et écriture du cache
- Logs existants préservés (T0, T1, T2, T_row_0, T_row_last, T3)

---

## Comportement attendu

### Scénario 1 : Premier lancement (pas de cache)

```
T0  HomeViewModel init
T1  prefetch consume (data=false)
    → pas de cache → isLoading=true → skeletons
T2  coroutines réseau
T_row_0  première row visible (~6-12s)
T_row_last  toutes rows complètes
T_cache_save  cache sauvegardé (~10-50ms)
```

### Scénario 2 : Relancement < 5 min après (cache frais)

```
T0  HomeViewModel init
T1  prefetch consume (data=false)
T_cache_hit  cache chargé (attendu <50ms, rows affichées)
    → isLoading=false, isRefreshing=false
T2  coroutines réseau (silencieux)
T_row_0  réseau remplace le cache
T_cache_save  cache mis à jour
```

### Scénario 3 : Relancement > 5 min après (cache stale)

```
T0  HomeViewModel init
T_cache_hit  cache chargé (<50ms, rows affichées)
    → isLoading=false, isRefreshing=true → "Mise à jour..." visible
T2  coroutines réseau
T_row_0  réseau remplace le cache
    → isRefreshing=false → indicateur disparaît
T_cache_save  cache mis à jour
```

### Scénario 4 : Après lecture d'un item

```
launchItem() → invalidateCache()
→ retour au home → pas de cache → chargement réseau normal
→ nouvelles données resume/progress reflétées
```

---

## Durée de vie du cache

| Condition | Comportement |
|---|---|
| Cache < 5 min | Affichage immédiat, refresh silencieux |
| Cache > 5 min | Affichage + "Mise à jour...", refresh visible |
| Après lecture | Cache invalidé, prochain load = réseau |
| Erreur réseau + cache | Cache affiché, pas d'erreur |
| Erreur réseau + pas cache | Écran d'erreur avec retry |

---

## Impact attendu sur les performances

| Métrique | Avant (sans cache) | Après (avec cache) | Delta |
|---|---|---|---|
| T_row_0 cold start (1er) | ~6-12s | ~6-12s | = (pas de cache encore) |
| T_row_0 cold start (2ème+) | ~6-12s | **<100ms** | **-99%** |
| Skeleton visible | 6-12s | 0ms (sauf 1er lancement) | **éliminé** |
| Données stale possibles | Non | Oui (max 5 min) | acceptable |

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
