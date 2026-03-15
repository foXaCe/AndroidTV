# Phase 1 — Migration Java → Kotlin (Quick wins)

**Date** : 2026-03-09
**BUILD SUCCESSFUL** ✓

## Résumé

| Fichier | Statut | LOC supprimés | Notes |
|---------|--------|---------------|-------|
| `ChannelCardView.java` | **Supprimé** | 58 | Orphelin (0 réf.) — layout `view_card_channel.xml` supprimé aussi |
| `FriendlyDateButton.java` | **Migré → .kt** | 47 | `FrameLayout`, `getDate()` → `val date` property |
| `GuideChannelHeader.java` | **Migré → .kt** | 89 | `RelativeLayout`, KoinJavaComponent conservé (callers Java) |
| `StreamInfo.java` | **Migré → .kt** | 108 | `var` properties, computed `val` pour dérivés |

## Détails des migrations

### 1.1 — ChannelCardView (supprimé)
- 0 référence dans le code source (seulement lint-baseline)
- Fichier + layout `view_card_channel.xml` supprimés

### 1.2 — FriendlyDateButton
- `dateVal` + `getDate()` → `val date: LocalDateTime` (constructor param)
- `getResources().getColor()` → `ContextCompat.getColor()`
- Callers Java (`LiveTvGuideFragment.java`) : `getDate()` toujours généré par Kotlin ✓

### 1.3 — GuideChannelHeader
- `mChannel`/`mContext`/`mTvGuide` → propriétés Kotlin directes
- `KoinJavaComponent.get<>()` conservé (callers Java encore présents)
- `userData.isFavorite()` → `userData?.isFavorite == true` (null-safe)
- Callers Java (`LiveTvGuideFragment`, `CustomPlaybackOverlayFragment`) : interop ✓

### 1.4 — StreamInfo
- 6 champs getter/setter → 7 `var` properties
- 3 getters dérivés → `val` computed properties
- `getSelectableStreams(type)` → `fun` avec `filter`
- `ArrayList<MediaStream>` → `List<MediaStream>`
- **Fix PlaybackManager.kt** : 2 ajustements nullabilité Kotlin :
  - `itemId` → `requireNotNull(itemId)` (l.44)
  - `stream.playSessionId` → variable locale pour smart cast (l.101)

## Compteur

| Métrique | Avant | Après |
|----------|-------|-------|
| Fichiers Java | 21 | 17 |
| LOC Java supprimés | — | 302 |
| LOC Java restants | 8 681 | ~8 379 |
