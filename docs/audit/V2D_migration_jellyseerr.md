# V2D Migration Jellyseerr — Plan & Rapport

> **Date : 2026-03-08 — Phase 1 (Analyse)**
> **Objectif initial** : Eliminer les addView() imperatifs dans Jellyseerr
> **Constat** : La migration est deja tres avancee — voir section 0

---

## 0. Constat : migration deja effectuee a 94%

L'audit 05 mentionnait "161 addView() imperatifs" dans Jellyseerr. **Ce chiffre correspond a l'ancien etat.**
Depuis, les ecrans principaux ont ete migres vers Compose :

| Migration effectuee | Ancien | Nouveau |
|---------------------|--------|---------|
| MediaDetailsFragment.kt | 2034L, 53 addView | 587L, 0 addView (ComposeView → JellyseerrMediaDetailsScreen) |
| PersonDetailsFragment.kt | 529L, 19 addView | 133L, 0 addView (ComposeView → JellyseerrPersonDetailsScreen) |
| 3 Dialogs | 1113L, 33 addView | compose/ versions actives, anciens fichiers = code mort |

**Resultat** : 108 addView() sur 115 elimines. Il reste **6 addView() actifs** (JellyseerrBrowseByFragment) et **38 addView() dans du code mort**.

---

## 1. Inventaire complet des fichiers Jellyseerr

### 1.1 Fichiers principaux (ui/jellyseerr/) — etat actuel

| Fichier | Lignes | Etat | addView() actifs | Notes |
|---------|--------|------|------------------|-------|
| MediaDetailsFragment.kt | 587 | Compose (ComposeView) | 0 | Migre |
| PersonDetailsFragment.kt | 133 | Compose (ComposeView) | 0 | Migre |
| DiscoverFragment.kt | 250 | Hybride XML + Compose | 0 | findViewById, pas addView |
| RequestsFragment.kt | 81 | ViewBinding | 0 | RecyclerView adapter |
| SettingsFragment.kt | 261 | ViewBinding | 0 | AlertDialog.Builder |
| JellyseerrDiscoverRowsFragment.kt | 621 | Leanback pur | 0 | Adapteurs/Presenters |
| JellyseerrBrowseByFragment.kt | 529 | Leanback + imperatif | **6** | 3 toolbar + 3 info row |
| JellyseerrViewModel.kt | 919 | ViewModel | 0 | NE PAS TOUCHER |
| GenreCardPresenter.kt | 76 | Leanback presenter | 0 | LayoutInflater |
| NetworkStudioCardPresenter.kt | 87 | Leanback presenter | 0 | LayoutInflater |
| MediaContentAdapter.kt | 67 | RecyclerView adapter | 0 | |
| RequestsAdapter.kt | 102 | RecyclerView adapter | 0 | |
| CastRowItem.kt | 34 | Data class | 0 | |

### 1.2 Code mort (pas importe nulle part — confirme par grep)

| Fichier | Lignes | addView() | Preuve |
|---------|--------|-----------|--------|
| QualitySelectionDialog.kt | 244 | 6 | 0 import `jellyseerr.QualitySelectionDialog` |
| SeasonSelectionDialog.kt | 337 | 10 | 0 import `jellyseerr.SeasonSelectionDialog` |
| AdvancedRequestOptionsDialog.kt | 532 | 17 | 0 import `jellyseerr.AdvancedRequestOptionsDialog` |
| DetailsOverviewRowPresenter.kt | 213 | 5 | 0 import ou instanciation |
| DetailsOverviewRow.kt | 16 | 0 | Utilise uniquement par le Presenter (mort) |
| DetailsRowView.kt | 16 | 0 | Utilise uniquement par le Presenter (mort) |

**Total code mort : 1 358 lignes, 38 addView()**

Note : `AdvancedRequestOptions` (data class dans le meme fichier) EST utilisee par le
Fragment migre — la data class est re-importee depuis `compose/AdvancedRequestOptionsDialog.kt`.

### 1.3 Fichiers Compose actifs (ui/jellyseerr/compose/)

| Fichier | Lignes | DS violations |
|---------|--------|---------------|
| JellyseerrMediaDetailsScreen.kt | 543 | 1 `RoundedCornerShape(100.dp)` → shapes.full |
| JellyseerrPersonDetailsScreen.kt | 247 | 0 |
| QualitySelectionDialog.kt | 197 | 0 |
| SeasonSelectionDialog.kt | 296 | 0 |
| AdvancedRequestOptionsDialog.kt | 327 | 0 |
| JellyseerrStatusBadge.kt | 76 | 1 `RoundedCornerShape(100.dp)` → shapes.full |
| JellyseerrFactsSection.kt | 165 | 1 `RoundedCornerShape(...)` → shapes.* |
| JellyseerrCastRow.kt | 88 | 0 |
| JellyseerrRequestButtons.kt | 113 | 0 |

**Total : 2 052 lignes Compose, 134 refs design system, 3 violations mineures**

### 1.4 Layout XML associe au code mort

| Layout | Utilise par | Etat |
|--------|-------------|------|
| view_jellyseerr_details_row.xml | DetailsRowView (CODE MORT) | A supprimer |

---

## 2. Dependencies de JellyseerrViewModel (NE PAS TOUCHER)

- **Constructeur** : JellyseerrRepository (seule dependance)
- **StateFlows exposes** : 14 (loadingState, trending*, upcoming*, genres, networks, studios, requests, searchResults, isAvailable, isMoonfinMode)
- **Fonctions publiques** : ~30 (content loading, search, details, request/cancel, settings/auth, discover, pagination)
- **Consommateurs** : 7 fragments (Discover, Requests, MediaDetails, PersonDetails, Settings, DiscoverRows, BrowseBy)

---

## 3. Plan de migration en 4 phases

### Phase 1 — Supprimer le code mort (1 358 lignes, 38 addView)

**Fichiers a supprimer :**
1. `ui/jellyseerr/QualitySelectionDialog.kt` (244L, 6 addView)
2. `ui/jellyseerr/SeasonSelectionDialog.kt` (337L, 10 addView)
3. `ui/jellyseerr/AdvancedRequestOptionsDialog.kt` (532L, 17 addView)
   - **Attention** : La data class `AdvancedRequestOptions` doit etre preservee ou deja dupliquee
4. `ui/jellyseerr/DetailsOverviewRowPresenter.kt` (213L, 5 addView)
5. `ui/jellyseerr/DetailsOverviewRow.kt` (16L)
6. `ui/jellyseerr/DetailsRowView.kt` (16L)
7. `res/layout/view_jellyseerr_details_row.xml`

**Impact** : -1 358 lignes, -38 addView (tous code mort)
**Risque** : Nul — aucun import confirme par grep exhaustif

### Phase 2 — Polir les fichiers Compose (3 violations DS)

**Corrections :**
1. `compose/JellyseerrMediaDetailsScreen.kt:536` : `RoundedCornerShape(100.dp)` → `JellyfinTheme.shapes.full`
2. `compose/JellyseerrStatusBadge.kt:31` : `RoundedCornerShape(100.dp)` → `JellyfinTheme.shapes.full`
3. `compose/JellyseerrFactsSection.kt:59` : `RoundedCornerShape(...)` → forme semantique du DS

**Impact** : 3 violations DS → 0
**Risque** : Minime

### Phase 3 — Migrer les parties imperatives de JellyseerrBrowseByFragment (6 addView)

**Etat actuel** : Fragment Leanback (529L) avec :
- Toolbar : `removeAllViews()` + 2 `addView(filterButton/sortButton)` — imperatif
- Info row : `removeAllViews()` + `addView(infoTextView)` — imperatif
- Grid : `removeAllViews()` + `addView(holder.view)` — contraint par VerticalGridPresenter

**Sous-taches :**
1. Remplacer `updateInfoRow()` (3 addView) par un composable info bar ou un simple `binding.infoText.text = ...`
2. Toolbar : les 2 ImageButton sont crees 1 fois dans `setupToolbar()` — addView structurel acceptable
3. Grid `container.addView(holder.view)` : contrainte VerticalGridPresenter Leanback — non migrable sans remplacement complet du grid

**Impact realiste** : 6 addView → 3 (les 3 structurels Leanback restent)
**Risque** : Faible pour l'info row, moyen pour le reste

### Phase 4 — Evaluation (hors scope strict)

Fragments sans addView() mais avec XML/ViewBinding :

| Fragment | Lignes | Pattern | Migration utile ? |
|----------|--------|---------|-------------------|
| DiscoverFragment.kt | 250 | Hybride XML + ComposeView | Possible mais 0 addView |
| RequestsFragment.kt | 81 | ViewBinding + RecyclerView | Non — petit, fonctionnel |
| SettingsFragment.kt | 261 | ViewBinding + AlertDialog | Non — doublon avec SettingsJellyseerrScreen |
| JellyseerrDiscoverRowsFragment.kt | 621 | Leanback pur | Non — gros chantier Leanback |

**Recommandation** : Hors scope. Aucun addView a eliminer.

---

## 4. Metriques cibles

| Metrique | Etat actuel | Apres phase 1 | Apres phase 2 | Apres phase 3 |
|----------|-------------|---------------|---------------|---------------|
| addView() actifs | 6 | 6 | 6 | 3* |
| addView() code mort | 38 | 0 | 0 | 0 |
| Lignes code mort | 1 358 | 0 | 0 | 0 |
| Violations DS (compose/) | 3 | 3 | 0 | 0 |
| Fichiers Compose | 9 | 9 | 9 | 9 |

*3 addView structurels restants dans JellyseerrBrowseByFragment (contraintes VerticalGridPresenter Leanback)

---

## 5. Rapports d'execution

### Phase 1 — Suppression code mort (BUILD SUCCESSFUL)

**Fichiers supprimes :**
- `ui/jellyseerr/QualitySelectionDialog.kt` (244L, 6 addView)
- `ui/jellyseerr/SeasonSelectionDialog.kt` (337L, 10 addView)
- `ui/jellyseerr/AdvancedRequestOptionsDialog.kt` (532L, 17 addView)
- `ui/jellyseerr/DetailsOverviewRowPresenter.kt` (213L, 5 addView)
- `ui/jellyseerr/DetailsOverviewRow.kt` (16L)
- `ui/jellyseerr/DetailsRowView.kt` (16L)
- `res/layout/view_jellyseerr_details_row.xml`

**Fichier cree :**
- `ui/jellyseerr/AdvancedRequestOptions.kt` (12L) — data class extraite (utilisee par ViewModel + compose)

**Impact** : -1 358 lignes, -38 addView (code mort)

### Phase 2 — Polish DS (BUILD SUCCESSFUL)

**Corrections appliquees :**
1. `compose/JellyseerrMediaDetailsScreen.kt` : `RoundedCornerShape(100.dp)` → `JellyfinTheme.shapes.full` + import RoundedCornerShape supprime
2. `compose/JellyseerrStatusBadge.kt` : `RoundedCornerShape(100.dp)` → `JellyfinTheme.shapes.full` + import RoundedCornerShape supprime
3. `compose/JellyseerrFactsSection.kt` : `8.dp` hardcode → `RadiusTokens.radiusSm` (RoundedCornerShape conserve car coins dynamiques isFirst/isLast)

**Impact** : 3 violations DS → 0 (le RoundedCornerShape restant utilise un rayon tokenise)

### Phase 3 — Simplification JellyseerrBrowseByFragment (BUILD SUCCESSFUL)

**Modification :**
- `updateInfoRow()` : remplace la boucle `removeAllViews()` + `addView()` a chaque selection par un `TextView` cache cree une seule fois
- Le `addView()` de l'info row ne s'execute plus qu'une seule fois (initialisation) au lieu de chaque navigation

**addView() restants dans JellyseerrBrowseByFragment (structurels) :**
- L207 : `toolbar.addView(button)` x2 — setup unique, layout XML partage
- L327 : `container.addView(holder.view)` — contrainte VerticalGridPresenter Leanback
- L403 : `infoRow.addView(infoTextView)` — setup unique (1 seule fois)

**Impact** : L'info row ne fait plus de removeAllViews/addView a chaque selection d'item

---

## 6. Bilan final

| Metrique | Avant | Apres |
|----------|-------|-------|
| Fichiers avec addView (code mort) | 4 | 0 (supprimes) |
| addView code mort | 38 | 0 |
| addView actifs (total Jellyseerr) | 6 | 4 (structurels) |
| Lignes code mort Jellyseerr | 1 358 | 0 |
| Violations DS (compose/) | 3 | 0 |
| RoundedCornerShape inline (compose/) | 3 | 1 (tokenise RadiusTokens) |
| Fichiers Compose Jellyseerr | 9 | 9 |
| Compilation | — | BUILD SUCCESSFUL |

---

## 7. Statut

- [x] Phase 1 — Analyse et plan
- [x] Phase 1 — Suppression code mort (-1 358 lignes, -38 addView)
- [x] Phase 2 — Polish DS (3 violations → 0)
- [x] Phase 3 — Simplification JellyseerrBrowseByFragment (info row cachee)
- [x] Phase 4 — Evaluation : hors scope (0 addView a eliminer)
