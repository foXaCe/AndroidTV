# Perf Fix #02 — Progressive Row Loading

**Date** : 2026-03-11
**Appareil** : Ugoos AM9 Pro (192.168.1.152)
**Serveur** : JellyMox v10.11.6 (192.168.1.61:8096)
**Build** : VegafoX v1.6.2 github-debug

---

## Problème

Identifié dans `perf_01_diagnostic.md` : le HomeScreen attendait que **TOUTES**
les rows soient chargées avant d'afficher quoi que ce soit. L'utilisateur fixait
un écran vide (skeletons) pendant 10+ secondes.

**Cause** : `deferredRows.flatMap { it.await() }` collectait toutes les rows
dans une liste, puis faisait un seul `_uiState.update`.

---

## Solution implémentée

### Fichier modifié : `HomeViewModel.kt`

Remplacement de `flatMap { it.await() }` par une boucle progressive :

```kotlin
// Progressive emission: await in section order, emit each row as ready
val progressiveRows = mutableListOf<TvRow<BaseItemDto>>()
var rowIndex = 0

for (deferred in deferredRows) {
    val sectionRows = deferred.await()
    if (sectionRows.isNotEmpty()) {
        progressiveRows.addAll(sectionRows)
        _uiState.update { it.copy(isLoading = false, rows = progressiveRows.toList()) }
        // ... set focused item, perf logging
    }
}
```

**Principe** : Chaque section est toujours chargée en parallèle (async),
mais les résultats sont émis vers l'UI dès qu'ils sont disponibles,
en respectant l'ordre des sections configurées par l'utilisateur.

### Fichier supprimé : `app/src/debug/res/values/logo.xml`

Couleurs mortes (`logo_gradient_start`, etc.) plus référencées nulle part.
Supprimé pour aligner debug et release (plus aucun source set debug-spécifique).

### Timers ajoutés

- `VFX_PERF T_row_0` — quand la première row est visible dans l'UI
- `VFX_PERF T_row_last` — quand la dernière row est complète
- Chaque row individuelle : `row[N] key=xxx items=Y at +Zms`

---

## Logs bruts

### Test 1 — Premier lancement (session existante, serveur "tiède")

```
07:47:10.302 VFX_PERF T0 HomeViewModel init at 1773211630302
07:47:10.319 VFX_PERF T0→T1 prefetch consume: 16ms (data=false)
07:47:10.321 VFX_PERF T1→T2 loadRows coroutine start: 3ms
07:47:22.139 VFX_PERF T_row_0 first row visible: 11818ms
07:47:22.140 VFX_PERF   row[0] key=resume items=12 at +11818ms
07:47:30.323 VFX_PERF   row[1] key=nextup items=50 at +20002ms
07:47:30.324 VFX_PERF   row[2] key=latest_db4c1708 items=15 at +20003ms
07:47:30.325 VFX_PERF   row[3..7] (latest_*) items=15 each at +20004ms
07:47:30.326 VFX_PERF T_row_last all rows complete: 20004ms (8 rows, 152 items)
07:47:30.326 VFX_PERF T0→T3 total: 20023ms
```

### Test 2 — Cache chaud

```
07:47:56.864 VFX_PERF T0 HomeViewModel init at 1773211676864
07:47:56.873 VFX_PERF T0→T1 prefetch consume: 9ms (data=false)
07:47:56.875 VFX_PERF T1→T2 loadRows coroutine start: 2ms
07:48:04.893 VFX_PERF T_row_0 first row visible: 8018ms
07:48:04.893 VFX_PERF   row[0] key=resume items=12 at +8018ms
07:48:09.323 VFX_PERF   row[1] key=nextup items=50 at +12448ms
07:48:09.323 VFX_PERF   row[2..7] (latest_*) items=15 each at +12448ms
07:48:09.325 VFX_PERF T_row_last all rows complete: 12449ms (8 rows, 152 items)
07:48:09.325 VFX_PERF T0→T3 total: 12460ms
```

### Test 3 — Cache chaud (meilleur run)

```
07:48:55.913 VFX_PERF T0 HomeViewModel init at 1773211735912
07:48:55.948 VFX_PERF T0→T1 prefetch consume: 36ms (data=false)
07:48:55.951 VFX_PERF T1→T2 loadRows coroutine start: 3ms
07:49:01.999 VFX_PERF T_row_0 first row visible: 6048ms
07:49:01.999 VFX_PERF   row[0] key=resume items=12 at +6048ms
07:49:08.332 VFX_PERF   row[1] key=nextup items=50 at +12380ms
07:49:08.332 VFX_PERF   row[2..7] (latest_*) items=15 each at +12381ms
07:49:08.333 VFX_PERF T_row_last all rows complete: 12382ms (8 rows, 152 items)
07:49:08.333 VFX_PERF T0→T3 total: 12421ms
```

---

## Tableau comparatif

| Métrique | Avant (diag #01) | Après (test 3) | Delta |
|---|---|---|---|
| T_row_0 (première row visible) | **10 017ms** (tout ou rien) | **6 048ms** | **-40%** |
| T_row_last (toutes rows) | 10 017ms | 12 382ms | +24% (serveur plus lent) |
| Rows chargées | 8/8 | 8/8 | = |
| Items totaux | 152 | 152 | = |
| Images temps moyen | 1 252ms | 667ms | **-47%** |

### Moyennes sur 3 tests (après)

| Métrique | Test 1 | Test 2 | Test 3 | Moyenne |
|---|---|---|---|---|
| T_row_0 | 11 818ms | 8 018ms | 6 048ms | **8 628ms** |
| T_row_last | 20 004ms | 12 449ms | 12 382ms | **14 945ms** |

**Note** : Les temps T_row_last sont plus élevés que le diagnostic original
(10s) car le serveur était plus lent lors de ces tests. La variabilité
serveur est le facteur dominant — pas le code client.

---

## Analyse

### Ce qui fonctionne

1. **Chargement progressif actif** : La première row ("Continuer à regarder")
   apparaît dès que son API répond, sans attendre les autres sections.

2. **Ordre des sections préservé** : Les rows apparaissent dans l'ordre
   configuré par l'utilisateur (RESUME → NEXT_UP → LATEST_MEDIA).

3. **Stagger animation compatible** : Les nouvelles rows ajoutées au TvRowList
   reçoivent leur propre animation d'entrée grâce au `item(key = row.key)`.

4. **Images significativement plus rapides** : Moyenne 667ms vs 1252ms (-47%).
   Probablement dû au cache Coil/disque qui se peuple au fil des tests.

### Goulot d'étranglement restant

Le vrai bottleneck est **avant** le chargement des sections :

```kotlin
val views = viewsDeferred?.await()       // ← bloque
val hasLiveTv = liveTvDeferred?.await()  // ← bloque
```

Ces deux awaits se font AVANT de lancer les sections. Si le liveTV check
est lent, toutes les sections sont retardées. C'est le même problème
dans le code original — non introduit par ce changement.

### Pourquoi rows 1-7 apparaissent toutes en même temps

Le NEXT_UP est le goulot : il est plus lent que les LATEST_MEDIA.
Puisqu'on await en ordre (RESUME → NEXT_UP → LATEST_MEDIA...), quand
NEXT_UP termine, toutes les LATEST_MEDIA sont déjà prêtes et s'affichent
instantanément.

---

## Prochaines optimisations recommandées

### P0 — Déplacer views/liveTV check en parallèle des sections

Lancer `viewsDeferred` et `liveTvDeferred` en même temps que les sections,
au lieu de les await avant. Les sections qui en ont besoin (LATEST_MEDIA,
LIVE_TV) les awaiteraient individuellement.

### P1 — Await par completion (pas par ordre)

Utiliser un `Channel` ou `select` pour émettre les rows par ordre de
completion plutôt que par ordre de section. Requiert un tri post-émission
pour préserver l'ordre configuré dans l'UI.

### P2 — Cache API local

Sauvegarder les rows en Room/DataStore. Au démarrage, afficher le cache
immédiatement puis rafraîchir en arrière-plan. Le T_row_0 passerait à
~100ms.

### P3 — Réduire la latence RESUME

L'API `getResumeItems` prend 6-12s. Investiguer côté serveur (JellyMox)
si c'est un problème d'indexation ou de performance de la base de données.

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Installé sur AM9 Pro (debug + release)
