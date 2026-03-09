# Phase 10c — Nettoyage final Leanback (player + XML + audit)

## Objectif

Éliminer les derniers imports Leanback possibles : CustomAction, TransportControlManager, XML layouts. Évaluer la suppression de la dépendance Gradle `leanback-core`.

## ÉTAPE 1 — CustomAction.kt : CONSERVÉ

### Analyse
`CustomAction` étend `PlaybackControlsRow.MultiAction` qui fournit :
- `setDrawables(Array<Drawable?>)` — tableau d'icônes par état
- `setLabels(Array<String>)` — accessibilité
- `icon` / `index` — état courant

### Raison de conservation
Les instances de `CustomAction` sont stockées dans `ArrayObjectAdapter` (Leanback) et rendues par `PlaybackTransportRowPresenter` (Leanback). Le presenter attend des objets `Action` pour obtenir les icônes et labels. Migrer `CustomAction` vers une classe Kotlin pure casserait toute la chaîne de rendu.

**Les 3 composants (CustomAction, ArrayObjectAdapter, PlaybackTransportRowPresenter) doivent être remplacés ensemble** lors d'une future migration Compose du player overlay.

### 13 subclasses concernées
SelectAudioAction, ClosedCaptionsAction, SelectQualityAction, PlaybackSpeedAction, ZoomAction, ChapterAction, CastAction, SubtitleDelayAction, AudioDelayAction, PreviousLiveTvChannelAction, ChannelBarChannelAction, GuideAction, RecordAction.

## ÉTAPE 2 — TransportControlManager.kt : CONSERVÉ

### Analyse des 8 imports Leanback

| Import | Usage | Éliminable ? |
|--------|-------|:---:|
| `AbstractDetailsDescriptionPresenter` | Description vide (placeholder) | NON — requis par le presenter |
| `Action` | `notifyActionChanged()` type parameter | NON — requis par les adapters |
| `ArrayObjectAdapter` | Stockage des actions primary/secondary | NON — requis par `PlaybackControlsRow` |
| `PlaybackControlsRow` | Seekbar state (currentTime, duration, buffered) + standard actions | NON — cœur du transport |
| `PlaybackTransportRowPresenter` | Rendu seekbar + boutons actions | NON — renderer visuel |
| `PlaybackTransportRowView` | Cast interne dans `createRowViewHolder()` | NON — manipulation DOM |
| `Presenter` | `ViewHolder` type pour `transportViewHolder` | NON — lifecycle binding |
| `RowPresenter` | `ViewHolder` + `setOnItemViewClickedListener` | NON — routing des clics |

+ 1 référence style : `androidx.leanback.R.style.Widget_Leanback_PlaybackControlsTimeStyle`

### Raison de conservation
Les 8 imports forment un système cohérent de rendu des transport controls (seekbar + boutons + temps). Remplacement = créer un overlay Compose complet (~300-400 LOC) avec :
- Seekbar Compose avec D-pad seek
- Boutons d'actions avec gestion focus TV
- Affichage temps fin
- Gestion état play/pause, recording
- PopupMenu anchoring

**Risque** : ÉLEVÉ — le player est l'UI la plus critique. Un bug casse la lecture pour tous les utilisateurs.
**Effort** : ~2-3 jours de développement + test intensif sur devices.
**Recommandation** : planifier comme projet dédié "Compose Player Overlay".

## ÉTAPE 3 — XML Leanback

### horizontal_grid.xml — SUPPRIMÉ
- **Widget** : `HorizontalGridView` (Leanback)
- **Références** : aucune (`R.layout.horizontal_grid` absent du code)
- **Action** : suppression — dead code depuis Phase 7 (migration BrowseGrid)

### fragment_server.xml — MIGRÉ
- **Widget** : `HorizontalGridView` pour la liste des utilisateurs
- **Fragment** : `ServerFragment.kt` (startup login)
- **Action** : `HorizontalGridView` → `RecyclerView` + `LinearLayoutManager(HORIZONTAL)`
- **Compatibilité** : l'adapter `UserAdapter` étend déjà `RecyclerView.Adapter` via `ListAdapter`. Toutes les API utilisées (`adapter`, `setPadding`, `requestFocus`) sont des API `RecyclerView`/`View`.

### view_lb_title.xml — DÉJÀ SUPPRIMÉ (Phase 10b)

## ÉTAPE 4 — Audit final Leanback

### Résultat grep `androidx.leanback` dans `app/src/main/`

| Fichier | Imports | Rôle |
|---------|:---:|------|
| `TransportControlManager.kt` | 8 + 1 style | Renderer transport controls player |
| `CustomAction.kt` | 1 | Base class actions player |
| `CardPresenter.kt` | 1 | Presenter cartes Jellyseerr |

**Total source : 3 fichiers, 10 imports**
**Total XML : 0 fichiers** (vs 2 avant phase 10c)

### Fichiers restants — analyse détaillée

1. **TransportControlManager + CustomAction** (9 imports, 2 fichiers) : système de rendu du player overlay. Migration = réécrire l'overlay en Compose. Projet dédié recommandé.

2. **CardPresenter** (1 import) : étend `Presenter` (Leanback). Utilisé par 3 écrans Jellyseerr via AndroidView bridge (`JellyseerrCastRow`, `JellyseerrPersonDetailsScreen`, `JellyseerrMediaDetailsScreen`). Migration = réécrire les lignes horizontales Jellyseerr en pur Compose (TvFocusCard/BrowseMediaCard).

## ÉTAPE 5 — Suppression dépendance Gradle

**NON** — impossible tant que 3 fichiers source utilisent `androidx.leanback`.

Lignes Gradle concernées :
- `gradle/libs.versions.toml` : `androidx-leanback = "1.2.0"` + `androidx-leanback-core = { ... }`
- `app/build.gradle.kts` : `implementation(libs.androidx.leanback.core)`

## ÉTAPE 6 — Score

### Gains phase 10c
- XML Leanback : 2 → 0 (−2)
- Fichiers source Leanback : 3 → 3 (inchangé — conservation justifiée)
- `horizontal_grid.xml` dead code supprimé
- `fragment_server.xml` migré vers RecyclerView (suppression dernière ref Leanback XML)

### Score
Le score reste **97/100**. Les 3 points manquants :
- **−1 Architecture** : dépendance Gradle `leanback-core` encore présente (3 fichiers)
- **−1 Code Quality** : CardPresenter Leanback Presenter (Jellyseerr)
- **−1 Modernité** : transport controls player encore Leanback-based

### Chemin vers 100/100
1. **Compose Player Overlay** — remplacer TransportControlManager + CustomAction (effort : grand, risque : élevé)
2. **Jellyseerr Compose pur** — remplacer CardPresenter + AndroidView bridges (effort : moyen, risque : faible)
3. **Supprimer dépendance Gradle** — quand 1+2 terminés

## Compteur Leanback — Historique complet

| Phase | Fichiers source | Imports source | XML Leanback | Gradle deps |
|-------|:-:|:-:|:-:|:-:|
| V4H (audit initial) | 32 | 106 | 10+ | 2 |
| V6 Phase 6 (audio) | 28 | ~90 | 10+ | 2 |
| V6 Phase 7 (browsing) | 22 | ~60 | 8 | 2 |
| V6 Phase 8 (LiveTV) | 18 | ~40 | 5 | 2 |
| V6 Phase 9a-c (player) | 9 | 26 | 3 | 2 |
| V6 Phase 9d (final) | 9 | 26 | 3 | 1 |
| V6 Phase 10a (adapters) | 5 | 12 | 3 | 1 |
| V6 Phase 10b (misc) | 3 | 10 | 3 | 1 |
| **V6 Phase 10c (final)** | **3** | **10** | **0** | **1** |

## Fichiers modifiés/supprimés

| Fichier | Action |
|---------|--------|
| `res/layout/horizontal_grid.xml` | SUPPRIMÉ (dead code) |
| `res/layout/fragment_server.xml` | MODIFIÉ (HorizontalGridView → RecyclerView) |
| `ui/startup/fragment/ServerFragment.kt` | MODIFIÉ (+LinearLayoutManager) |

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug
```
