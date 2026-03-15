# Audit des fichiers Java restants

**Date** : 2026-03-09
**Total** : 21 fichiers — 8 681 LOC

---

## Tableau complet

| # | Fichier | LOC | Rôle | Extends | Implements | Patterns Java-only | Réf. | Complexité | Priorité |
|---|---------|-----|------|---------|------------|--------------------|------|------------|----------|
| 1 | `data/compat/StreamInfo.java` | 108 | DTO stream playback (URL, method, media source) | — | — | Getter/setter PascalCase | 7+ | Simple | HAUTE |
| 2 | `ui/card/ChannelCardView.java` | 58 | Card live TV channel | FrameLayout | — | View binding | 0 (orphelin) | Simple | BASSE |
| 3 | `ui/FriendlyDateButton.java` | 47 | Bouton date picker pour guide TV | FrameLayout | — | Focus override | 1 | Simple | MOYENNE |
| 4 | `ui/GuideChannelHeader.java` | 89 | Header canal dans le guide TV | RelativeLayout | — | Focus override, Koin DI | 4 | Simple | HAUTE |
| 5 | `ui/ItemListView.java` | 125 | Container scrollable de ItemRowView | FrameLayout | — | Listener callbacks, instanceof | 4 | Moyenne | HAUTE |
| 6 | `ui/ItemRowView.java` | 231 | Ligne individuelle dans ItemListView | FrameLayout | — | 1 anon class, 2 interfaces internes | 4 | Moyenne | HAUTE |
| 7 | `ui/LiveProgramDetailPopup.java` | 322 | Popup détail programme live TV | — | — | 8 anon classes, AlertDialog | 4 | Complexe | HAUTE |
| 8 | `ui/ProgramGridCell.java` | 172 | Cellule programme dans grille TV | RelativeLayout | RecordingIndicatorView | 1 anon class | 4 | Moyenne | HAUTE |
| 9 | `ui/RecordPopup.java` | 256 | Popup planification enregistrement | — | — | 4 anon listeners, Spinner | 1 | Complexe | MOYENNE |
| 10 | `ui/itemdetail/ItemListFragment.java` | 580 | Fragment playlist/album avec controles | Fragment | View.OnKeyListener | 15+ anon classes | Nav | Complexe | HAUTE |
| 11 | `ui/itemdetail/MusicFavoritesListFragment.java` | 290 | Fragment favoris musicaux | Fragment | View.OnKeyListener | 8+ anon classes | Nav | Complexe | MOYENNE |
| 12 | `ui/itemhandling/ItemLauncher.java` | 308 | Router central lancement items | — | — | 5 anon Response callbacks, instanceof | 2 | Complexe | HAUTE |
| 13 | `ui/livetv/LiveTvGuideFragment.java` | 790 | Fragment principal guide TV | Fragment | LiveTvGuide, OnKeyListener | 13+ anon classes, AsyncTask | 2 | Complexe | HAUTE |
| 14 | `ui/livetv/TvManager.java` | 220 | Manager données live TV (static) | — | — | Champs static mutables, pas de sync | 4 | Moyenne | HAUTE |
| 15 | `ui/playback/CustomPlaybackOverlayFragment.java` | 1 427 | Overlay principal playback + guide TV | Fragment | LiveTvGuide, OnKeyListener | 11 anon classes, AsyncTask | 9 | Complexe | HAUTE |
| 16 | `ui/playback/overlay/LeanbackOverlayFragment.java` | 242 | Overlay Compose pour player | Fragment | — | 1 anon FrameLayout | 4 | Moyenne | HAUTE |
| 17 | `ui/playback/PlaybackController.java` | 1 670 | Orchestration core du playback | — | PlaybackControllerNotifiable | 10 anon classes | 24+ | Complexe | HAUTE |
| 18 | `ui/playback/SubtitleDelayHandler.java` | 131 | Délai sous-titres ExoPlayer | — | Player.Listener | 1 static inner, 1 anon | 1 | Moyenne | MOYENNE |
| 19 | `ui/playback/VideoManager.java` | 810 | Config/gestion ExoPlayer | — | — | 4 anon classes, anon subclass | 4 | Complexe | HAUTE |
| 20 | `util/KeyProcessor.java` | 367 | Handler input remote/clavier | — | — | 1 inner class privée, PopupMenu | 2 | Complexe | HAUTE |
| 21 | `newpipe/extractor/utils/Utils.java` | 438 | Utilitaires URL (shadow NewPipe) | — | — | 4 checked exceptions, final class | 0 (mort?) | Complexe | BASSE |

> Tous les chemins sont relatifs à `app/src/main/java/org/jellyfin/androidtv/` sauf #21 (`org/schabi/`).

---

## Répartition par complexité

| Complexité | Fichiers | LOC total |
|------------|----------|-----------|
| Simple (< 50 LOC ou pas de patterns) | 4 | 302 |
| Moyenne (50-200 LOC, quelques patterns) | 6 | 1 120 |
| Complexe (> 200 LOC ou patterns lourds) | 11 | 7 259 |

---

## Dépendances entrantes détaillées

### Fichiers les plus référencés (> 5 réf.)
- **PlaybackController** — 24+ fichiers (.kt, .java, tests, DI)
- **CustomPlaybackOverlayFragment** — 9 fichiers
- **StreamInfo** — 7+ fichiers (playback, reporting, trailers)

### Clusters de dépendances mutuelles

**Cluster Playback** (couplage fort) :
```
PlaybackController ↔ VideoManager ↔ CustomPlaybackOverlayFragment ↔ LeanbackOverlayFragment
                  ↔ SubtitleDelayHandler
                  ↔ StreamInfo
```

**Cluster Live TV** (couplage fort) :
```
LiveTvGuideFragment ↔ TvManager ↔ ProgramGridCell
                    ↔ GuideChannelHeader ↔ FriendlyDateButton
                    ↔ LiveProgramDetailPopup ↔ RecordPopup
```

**Cluster Item Detail** :
```
ItemListFragment ↔ ItemListView ↔ ItemRowView
                 ↔ ItemLauncher
MusicFavoritesListFragment ↔ ItemListView ↔ ItemRowView
                           ↔ ItemLauncher
```

### Fichiers isolés
- **ChannelCardView** — 0 références, probablement orphelin → candidat suppression
- **Utils (NewPipe)** — 0 références directes → vérifier si utilisé via newpipe-extractor, sinon supprimer
- **KeyProcessor** — seulement Koin DI + lint

---

## Patterns Java-only détectés

| Pattern | Occurrences | Fichiers concernés |
|---------|-------------|-------------------|
| Anonymous classes | ~80+ | 15 fichiers sur 21 |
| AsyncTask (deprecated) | 2 | CustomPlaybackOverlayFragment, LiveTvGuideFragment |
| Static inner classes | 2 | SubtitleDelayHandler (DelayedCue), LiveTvGuideFragment (DisplayProgramsTask) |
| Private inner classes | 1 | KeyProcessor (KeyProcessorItemMenuClickListener) |
| Static interfaces | 2 | ItemRowView (RowSelectedListener, RowClickedListener) |
| Checked exceptions | 4 méthodes | Utils.java uniquement |
| Static mutable state | 5 champs | TvManager (problème thread-safety) |
| KoinJavaComponent | 8+ | GuideChannelHeader, LiveProgramDetailPopup, ItemLauncher, KeyProcessor... |

---

## Ordre de migration recommandé

### Phase 1 — Quick wins (302 LOC, 4 fichiers)
Fichiers simples, migration directe sans risque.

| Ordre | Fichier | LOC | Raison |
|-------|---------|-----|--------|
| 1.1 | ChannelCardView | 58 | Orphelin → vérifier puis supprimer ou migrer |
| 1.2 | FriendlyDateButton | 47 | Petit, 1 seule réf. (LiveTvGuideFragment) |
| 1.3 | GuideChannelHeader | 89 | Petit, 4 réf. Kotlin existantes |
| 1.4 | StreamInfo | 108 | DTO pur, conversion auto en data class |

### Phase 2 — Moyenne complexité (1 120 LOC, 6 fichiers)
Patterns Java légers, conversion semi-automatique.

| Ordre | Fichier | LOC | Raison |
|-------|---------|-----|--------|
| 2.1 | SubtitleDelayHandler | 131 | 1 seule réf. (VideoManager), static inner → data class |
| 2.2 | ProgramGridCell | 172 | Anon class → lambda, interface simple |
| 2.3 | TvManager | 220 | Static → object/singleton Kotlin, fix thread-safety |
| 2.4 | ItemListView | 125 | Dépend de ItemRowView → migrer ensemble |
| 2.5 | ItemRowView | 231 | Interfaces internes → typealias/fun interface |
| 2.6 | LeanbackOverlayFragment | 242 | Anon FrameLayout → objet expression Kotlin |

### Phase 3 — Complexe, isolés (994 LOC, 3 fichiers)
Fichiers complexes mais avec peu de dépendances.

| Ordre | Fichier | LOC | Raison |
|-------|---------|-----|--------|
| 3.1 | RecordPopup | 256 | 4 anon → lambdas, 1 seule réf. |
| 3.2 | KeyProcessor | 367 | Inner class → classe top-level, PopupMenu rewrite |
| 3.3 | MusicFavoritesListFragment | 290 | Similaire à ItemListFragment, migrer après |
| 3.4 | Utils (NewPipe) | 438 | Vérifier utilisation, supprimer si mort |

### Phase 4 — Complexe, couplé Live TV (1 112 LOC, 2 fichiers)
Migrer ensemble car fortement couplés.

| Ordre | Fichier | LOC | Raison |
|-------|---------|-----|--------|
| 4.1 | LiveProgramDetailPopup | 322 | 8 anon classes → lambdas, AlertDialog builder |
| 4.2 | LiveTvGuideFragment | 790 | AsyncTask → coroutines, 13+ anon → lambdas |

### Phase 5 — Complexe, couplé Playback (3 215 LOC, 3 fichiers)
Le coeur du player. Migrer en dernier, tester intensivement.

| Ordre | Fichier | LOC | Raison |
|-------|---------|-----|--------|
| 5.1 | ItemLauncher | 308 | Router central, 5 anon Response → coroutines/suspend |
| 5.2 | ItemListFragment | 580 | 15+ anon → lambdas, AudioEventListener |
| 5.3 | VideoManager | 810 | ExoPlayer config, anon subclass → objet Kotlin |
| 5.4 | CustomPlaybackOverlayFragment | 1 427 | Le plus gros, AsyncTask → coroutines, 11 anon |
| 5.5 | PlaybackController | 1 670 | Le plus critique, 24+ réf., 10 anon → coroutines |

---

## Risques et recommandations

### Risques
1. **PlaybackController** (1 670 LOC, 24+ réf.) — migration la plus risquée, touche tout le playback
2. **AsyncTask** dans 2 fichiers — deprecated depuis API 30, remplacer par coroutines
3. **TvManager** — état statique mutable sans synchronisation → race conditions potentielles
4. **KoinJavaComponent** → remplacer par `by inject()` natif Kotlin

### Recommandations
- Migrer par cluster (Live TV ensemble, Playback ensemble)
- Écrire des tests avant migration pour les fichiers critiques (PlaybackController, VideoManager)
- Utiliser la conversion automatique Android Studio (Ctrl+Alt+Shift+K) puis nettoyer
- Remplacer les anonymous classes par des lambdas Kotlin
- Remplacer AsyncTask par `viewModelScope.launch` / `lifecycleScope.launch`
- Convertir StreamInfo en `data class`
- Convertir TvManager en `object` Kotlin avec `@Volatile` / `Mutex`
