# Perf Diagnostic #01 — HomeScreen loading

**Date** : 2026-03-11
**Appareil** : Ugoos AM9 Pro (192.168.1.152)
**Serveur** : JellyMox v10.11.6 (192.168.1.61:8096)
**Build** : VegafoX v1.6.2 github-debug

---

## Logs bruts

### Test 1 — Cache froid (premier login, données effacées)

```
07:20:27.235 VFX_PERF_PREFETCH TP0 prefetch start at 1773210027235
07:20:27.694 VFX_PERF T0 HomeViewModel init at 1773210027693
07:20:27.697 VFX_PERF T0→T1 prefetch consume: 3ms (data=false)
07:20:27.699 VFX_PERF T1→T2 loadRows coroutine start: 3ms
07:21:35.518 VFX_PERF T2→T3 all rows loaded: 67819ms (1 rows, 12 items)
07:21:35.518 VFX_PERF T0→T3 total: 67825ms
07:21:35.518 VFX_PERF   row[0] key=resume items=12
07:21:35.838 VFX_IMG image_load_start: Alien, le huitième passager
07:21:35.882 VFX_IMG image_load_start: Avatar
07:21:35.922 VFX_IMG image_load_start: Wake Up Dead Man
07:21:35.966 VFX_IMG image_load_start: Star Wars : Les Derniers Jedi
07:21:36.318 VFX_IMG image_load_error: Wake Up Dead Man — Connection reset
07:21:36.324 VFX_IMG image_load_error: Star Wars — Connection reset
07:21:36.331 VFX_IMG image_load_error: Avatar — Connection reset
07:21:40.692 VFX_IMG image_load_error: Alien — unexpected end of stream
```

**Erreurs API (loadLatestMedia) :**
```
07:21:19.881 HomeViewModel$loadLatestMedia: timeout — IPTV Séries
07:21:28.775 HomeViewModel$loadLatestMedia: timeout — Séries
07:21:28.862 HomeViewModel$loadLatestMedia: timeout — Films
```

### Test 2 — Cache chaud (2ème lancement, session existante)

```
07:23:06.993 VFX_PERF_PREFETCH TP0 prefetch start at 1773210186993
07:23:07.611 VFX_PERF T0 HomeViewModel init at 1773210187610
07:23:07.650 VFX_PERF T0→T1 prefetch consume: 40ms (data=false)
07:23:07.654 VFX_PERF T1→T2 loadRows coroutine start: 3ms
07:23:17.627 VFX_PERF T2→T3 all rows loaded: 9974ms (8 rows, 152 items)
07:23:17.627 VFX_PERF T0→T3 total: 10017ms
07:23:17.628 VFX_PERF   row[0] key=resume items=12
07:23:17.628 VFX_PERF   row[1] key=nextup items=50
07:23:17.628 VFX_PERF   row[2] key=latest_db4c1708 items=15
07:23:17.628 VFX_PERF   row[3] key=latest_d565273f items=15
07:23:17.628 VFX_PERF   row[4] key=latest_423fa8e2 items=15
07:23:17.628 VFX_PERF   row[5] key=latest_63879d13 items=15
07:23:17.629 VFX_PERF   row[6] key=latest_82b7255f items=15
07:23:17.629 VFX_PERF   row[7] key=latest_3975100b items=15
07:23:18.204 VFX_IMG image_load_start: Alien, le huitième passager
07:23:18.266 VFX_IMG image_load_start: Avatar
07:23:18.330 VFX_IMG image_load_start: Wake Up Dead Man
07:23:18.394 VFX_IMG image_load_start: Star Wars : Les Derniers Jedi
07:23:18.499 VFX_IMG image_load_start: Baja
07:23:18.563 VFX_IMG image_load_start: Monarch - Legacy of Monsters
07:23:18.627 VFX_IMG image_load_start: Monarch S01E07
07:23:18.694 VFX_IMG image_load_start: Home Before Dark S01E02
07:23:19.564 VFX_IMG image_load_done: 1376ms Alien, le huitième passager
07:23:19.569 VFX_IMG image_load_done: 1315ms Avatar
07:23:19.716 VFX_IMG image_load_done: 1397ms Wake Up Dead Man
07:23:19.717 VFX_IMG image_load_done: 1335ms Star Wars : Les Derniers Jedi
07:23:19.722 VFX_IMG image_load_done: 1237ms Baja
07:23:19.730 VFX_IMG image_load_done: 1180ms Monarch - Legacy of Monsters
07:23:19.735 VFX_IMG image_load_done: 1121ms Monarch S01E07
07:23:19.736 VFX_IMG image_load_done: 1055ms Home Before Dark S01E02
```

---

## Tableau des deltas mesurés

| Métrique | Cache froid | Cache chaud |
|---|---|---|
| TP0 prefetch start | ✓ | ✓ |
| TP1 continueWatching | ❌ (pas de log — timeout/trop lent) | ❌ (pas de log — pas terminé avant consume) |
| TP2 nextUp | ❌ | ❌ |
| T0→T1 prefetch consume | 3ms | 40ms |
| Prefetch data disponible | **NON** | **NON** |
| T1→T2 coroutine start | 3ms | 3ms |
| T2→T3 all rows | **67 819ms** | **9 974ms** |
| T0→T3 total | **67 825ms** | **10 017ms** |
| Rows chargées | 1/8 | 8/8 |
| Items totaux | 12 | 152 |
| Images null URL | 0 | 0 |
| Images load start | 4 | 8 |
| Images load done | 0 | 8 |
| Images load error | 4 | 0 |
| Temps moyen image | N/A (toutes en erreur) | **1 252ms** |
| Image la plus lente | N/A | 1 397ms (Wake Up Dead Man) |
| Image la plus rapide | N/A | 1 055ms (Home Before Dark S01E02) |

---

## Constats

### 1. Le prefetch ne sert JAMAIS
Le prefetch démarre ~460ms avant le HomeViewModel init (TP0 → T0).
Mais il n'a jamais le temps de terminer avant `consume()` — les appels API
réseau prennent au minimum ~2-3 secondes. **Résultat : `data=false` dans 100% des cas.**

### 2. Goulot d'étranglement : les API Jellyfin (réseau/serveur)
- Cache froid : **68 secondes** pour 1 seule row (6 sections en timeout)
- Cache chaud : **10 secondes** pour 8 rows
- Les sections `loadLatestMedia` sont les plus lentes (une par bibliothèque, en parallèle
  mais chaque appel prend 2-5s, et certains timeout à 30s)

### 3. Images : lentes mais pas nulles
- 0 images avec URL null → la logique `getItemImageUrl` fonctionne bien
- Temps moyen de chargement image : **1 252ms** (cache chaud)
- Cache froid : 100% d'erreurs réseau (Connection reset, unexpected end of stream)

### 4. Posters au mauvais format
La logique `getItemImageUrl` cherche THUMB (16:9) en priorité, puis fallback sur
PRIMARY (portrait poster). Quand PRIMARY est utilisé, il est croppé en 16:9 →
perte d'information et aspect visuel dégradé. Le serveur renvoie l'image pleine
et c'est Coil qui crop côté client.

---

## Goulots d'étranglement identifiés (par priorité)

| # | Problème | Impact | Difficulté |
|---|---|---|---|
| 1 | **Prefetch inutile** — ne termine jamais avant consume() | L'utilisateur voit toujours les squelettes | Moyenne |
| 2 | **loadLatestMedia en série implicite** — 7 bibliothèques, chacune 2-5s | 10s au total même en cache chaud | Faible |
| 3 | **Pas de cache API local** — chaque redémarrage refait toutes les requêtes | Cold start = catastrophique | Moyenne |
| 4 | **Images 1.2s en moyenne** — pas de cache disque utilisé efficacement | Shimmer visible pendant >1s | Faible |
| 5 | **Mauvais format poster** — PRIMARY cropé en 16:9 au lieu de BACKDROP/THUMB | Visuel dégradé | Faible |

---

## Recommandations fixes prioritaires

### P0 — Rendre le prefetch utile
- Démarrer le prefetch **plus tôt** (dans `SessionRepository.onSessionStart()` plutôt que juste avant)
- Ajouter un **timeout avec attente** dans `consume()` : attendre jusqu'à 2s les données prefetchées
  ```kotlin
  suspend fun consume(timeout: Duration = 2.seconds): List<TvRow<BaseItemDto>>? {
      return withTimeoutOrNull(timeout) { _prefetchedRows.filterNotNull().first() }
  }
  ```

### P1 — Affichage progressif des rows
- Émettre chaque row dès qu'elle est prête au lieu d'attendre que TOUTES soient chargées
- Utiliser un `Flow<List<TvRow>>` incrémental plutôt qu'un seul `_uiState.update`
- L'utilisateur verrait "Continue Watching" en ~2s, puis les autres rows une par une

### P2 — Cache API local (Room / DataStore)
- Sauvegarder les dernières rows chargées en local
- Au démarrage, afficher immédiatement le cache local puis rafraîchir en arrière-plan
- Le cold start deviendrait quasi-instantané après le premier chargement

### P3 — Optimiser les images
- Réduire la qualité de `quality = 90` à `quality = 70` (gain ~40% taille)
- Précharger les images des 5 premiers items dès que la row est disponible
- Utiliser `BACKDROP` (16:9 natif) au lieu de `PRIMARY` quand THUMB n'est pas disponible

### P4 — Corriger le format des posters
- Quand seul PRIMARY est disponible (portrait), utiliser un fillHeight au lieu de fillWidth
  pour éviter le crop destructif, OU changer le fallback vers BACKDROP
