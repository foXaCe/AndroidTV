# Phase 11a — Migration Jellyseerr Compose pur (CardPresenter éliminé)

## Objectif

Supprimer `CardPresenter.kt` (dernier import Leanback hors player) en migrant les 3 écrans Jellyseerr qui utilisaient des AndroidView bridges vers du Compose pur.

## ÉTAPE 1 — Analyse des 3 bridges AndroidView

| Fichier | Composable | Data | CardPresenter params | onClick |
|---------|-----------|------|---------------------|---------|
| `JellyseerrCastRow.kt` | `CastCardItem` | `JellyseerrCastMemberDto` | `(true, 130)` — person, 130dp | `onCastClick(id)` |
| `JellyseerrPersonDetailsScreen.kt` | `AppearanceCard` | `JellyseerrDiscoverItemDto` | `()` — poster 2:3, 150dp | `onItemClick(item)` |
| `JellyseerrMediaDetailsScreen.kt` | `CardItem` | `JellyseerrDiscoverItemDto` | `()` — poster 2:3, 150dp | `onItemClick(item)` |

Chaque bridge instanciait `CardPresenter` (Leanback `Presenter`), créait un `ViewHolder` dans un `LinearLayout` factice, puis injectait la View dans `AndroidView`. Approche lourde et fragile.

## ÉTAPE 2 — JellyseerrPersonCard.kt créé

**Fichier** : `ui/jellyseerr/compose/JellyseerrPersonCard.kt`

Composable réutilisable pour les cartes cast/crew :
- `TvFocusCard` (tv-material3 Surface) — focus natif TV, scale 1.06×
- Portrait photo TMDB (2:3 aspect ratio, 130dp largeur)
- Nom + rôle (character) sous l'image
- Placeholder gradient pendant le chargement
- Zéro dépendance Leanback

## ÉTAPE 3 — 3 fichiers migrés

### JellyseerrCastRow.kt
- `CastCardItem` → `JellyseerrPersonCard`
- Supprimé : `AndroidView`, `CardPresenter`, `JellyseerrPersonBaseRowItem`, `ViewGroup`, `LinearLayout`
- LazyRow conservée (déjà Compose)

### JellyseerrPersonDetailsScreen.kt
- `AppearanceCard` → `JellyseerrPosterCard` (existant dans `JellyseerrCards.kt`)
- Supprimé : `AndroidView`, `CardPresenter`, `JellyseerrMediaBaseRowItem`, `ViewGroup`, `LinearLayout`
- FlowRow conservée (déjà Compose)

### JellyseerrMediaDetailsScreen.kt
- `CardItem` → `JellyseerrPosterCard`
- Supprimé : `AndroidView`, `CardPresenter`, `JellyseerrMediaBaseRowItem`, `ViewGroup`, `LinearLayout`
- `PaginatedCardRow` + LazyRow conservées (déjà Compose)

## ÉTAPE 4 — CardPresenter.kt supprimé

```
grep -r "CardPresenter" --include="*.kt" --include="*.java" -l
→ 0 résultats de code (seuls des commentaires KDoc subsistent)
```

**Supprimé** : `ui/presentation/CardPresenter.kt` + répertoire `ui/presentation/` (vide)

## ÉTAPE 5 — Dead code supplémentaire supprimé

| Fichier supprimé | Raison |
|-----------------|--------|
| `ui/browsing/BrowseRowDef.kt` | Dead code (identifié phase 10a) |
| `ui/itemhandling/JellyseerrPersonBaseRowItem.kt` | Plus utilisé (était wrapper CardPresenter) |
| `ui/itemhandling/JellyseerrMediaBaseRowItem.kt` | Plus utilisé (était wrapper CardPresenter) |
| `data/querying/GetUserViewsRequest.kt` | Dead code |
| `data/querying/GetAdditionalPartsRequest.kt` | Dead code |
| `data/querying/GetTrailersRequest.kt` | Dead code |
| `data/querying/GetSeriesTimersRequest.kt` | Dead code |
| `data/querying/GetSpecialsRequest.kt` | Dead code |

Répertoires supprimés : `ui/presentation/`, `data/querying/`

## Compteur Leanback

| Métrique | Avant (10c) | Après (11a) | Delta |
|----------|:-:|:-:|:-:|
| Fichiers source Leanback | 3 | 2 | −1 |
| Imports `androidx.leanback` | ~11 | 10 | −1 |
| XML Leanback | 0 | 0 | — |
| Gradle deps Leanback | 1 | 1 | — |

### Fichiers Leanback restants (player overlay uniquement)

| Fichier | Imports | Rôle |
|---------|:-:|------|
| `TransportControlManager.kt` | 9 | Renderer transport controls (seekbar + actions) |
| `CustomAction.kt` | 1 | Base class actions player |

### Historique complet

| Phase | Fichiers source | XML | Gradle deps |
|-------|:-:|:-:|:-:|
| V4H (audit initial) | 32 | 10+ | 2 |
| V6 Phase 6 (audio) | 28 | 10+ | 2 |
| V6 Phase 7 (browsing) | 22 | 8 | 2 |
| V6 Phase 8 (LiveTV) | 18 | 5 | 2 |
| V6 Phase 9a-c (player) | 9 | 3 | 2 |
| V6 Phase 9d (final) | 9 | 3 | 1 |
| V6 Phase 10a (adapters) | 5 | 3 | 1 |
| V6 Phase 10b (misc) | 3 | 3 | 1 |
| V6 Phase 10c (final) | 3 | 0 | 1 |
| **V6 Phase 11a (jellyseerr)** | **2** | **0** | **1** |

## Dead code total supprimé

- 1 fichier Leanback (`CardPresenter.kt`)
- 2 wrappers BaseRowItem (`JellyseerrPersonBaseRowItem`, `JellyseerrMediaBaseRowItem`)
- 6 requêtes mortes (`BrowseRowDef`, `GetUserViewsRequest`, `GetAdditionalPartsRequest`, `GetTrailersRequest`, `GetSeriesTimersRequest`, `GetSpecialsRequest`)
- 2 packages vidés et supprimés (`ui/presentation/`, `data/querying/`)

## BUILD

```
BUILD SUCCESSFUL — assembleGithubDebug (×4 : après chaque étape)
```
