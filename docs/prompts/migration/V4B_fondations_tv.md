# V4B — Fondations TV Compose

> **Date : 2026-03-08**
> **Objectif** : Créer les briques fondamentales TV Compose pour la migration Leanback
> **Statut** : TERMINÉ — BUILD SUCCESSFUL, checkAll PASSED

---

## 1. Fichiers créés

| Fichier | Lignes | Rôle |
|---------|--------|------|
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvScaffold.kt` | 47 | Scaffold TV : background DS, overscan 5%, focus group + restorer |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvCardGrid.kt` | 52 | Grille de cartes : LazyVerticalGrid + focus restorer |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvRowList.kt` | 94 | Liste de rows : LazyColumn + LazyRow par section |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvFocusCard.kt` | 94 | Carte focusable : tv-material3 Surface, scale 1.06×/0.95×, focus ring |
| `app/src/main/java/org/jellyfin/androidtv/ui/base/tv/TvHeader.kt` | 50 | Header d'écran : titre + sous-titre + actions |
| `app/src/androidTest/kotlin/ui/base/tv/TvFoundationTest.kt` | 162 | 5 tests instrumentés |
| **Total** | **499** | |

---

## 2. Dépendances

| Bibliothèque | Version | Statut | Rôle |
|-------------|---------|--------|------|
| `androidx.tv:tv-material` | 1.0.1 | Déjà présente | Surface/Card avec focus natif TV |
| `androidx.compose.foundation:foundation` | 1.10.4 | Déjà présente | LazyColumn, LazyRow, LazyVerticalGrid |
| `androidx.tv:tv-foundation` | — | **Non ajoutée** | Seulement IME options — inutile pour lazy lists |

### Note sur tv-foundation

`androidx.tv:tv-foundation` (dernière version : `1.0.0-alpha12`) ne contient **PAS** de
`TvLazyColumn`/`TvLazyRow`/`TvLazyVerticalGrid`. Ces composables n'existent pas dans
l'écosystème TV Compose stable. Le JAR ne contient que :
- `ExperimentalTvFoundationApi`
- `TvImeOptionsKt` / `TvKeyboardAlignment`

Les lazy lists standard de `compose-foundation` gèrent correctement le D-pad :
- Focus automatique via `Modifier.focusable()` / `clickable()`
- Scroll-into-view natif (BringIntoView)
- Focus restoration via `Modifier.focusRestorer()`

---

## 3. Approche de navigation

Navigation **custom Fragment-based** (pas de Jetpack Navigation Compose) :

| Composant | Fichier | Rôle |
|-----------|---------|------|
| `NavigationRepository` | `ui/navigation/NavigationRepository.kt` | Stack<Destination> + SharedFlow<NavigationAction> |
| `Destinations` | `ui/navigation/Destinations.kt` | Factory object : 40+ destinations |
| `DestinationFragmentView` | `ui/browsing/DestinationFragmentView.kt` | FrameLayout gère les FragmentTransactions |
| `MainActivity` | `ui/browsing/MainActivity.kt` | Hôte : collecte NavigationAction |

**Stratégie retenue** : Les nouveaux écrans Compose TV seront des Fragments wrapper
(`content { ComposeScreen() }`) pointés depuis `Destinations.kt`. Aucun changement
au système de navigation existant.

---

## 4. Architecture des composants TV

```
TvScaffold (background + overscan + focus group)
├── TvHeader (titre + actions)
├── TvRowList (LazyColumn de rows)
│   ├── Section header (titleMedium)
│   └── LazyRow
│       └── TvFocusCard (tv-material3 Surface)
│           └── CardItem (DS existant)
└── TvCardGrid (LazyVerticalGrid)
    └── TvFocusCard
        └── CardItem (DS existant)
```

### Composants fondamentaux

| Composant | Base | Remplace Leanback |
|-----------|------|-------------------|
| `TvScaffold` | Box + focusGroup + focusRestorer | Structure écran Leanback |
| `TvCardGrid` | LazyVerticalGrid | HorizontalGridPresenter, VerticalGrid |
| `TvRowList` | LazyColumn + LazyRow | RowsSupportFragment + ListRowPresenter |
| `TvFocusCard` | tv-material3 Surface | ImageCardView + CardPresenter |
| `TvHeader` | Row + Column | TitleViewAdapter, BrowseFragment title area |

### Composants DS réutilisés

| Composant | Usage dans les fondations TV |
|-----------|------------------------------|
| `JellyfinTheme.colorScheme` | background, textPrimary, textSecondary, focusRing |
| `JellyfinTheme.typography` | titleLarge (header), titleMedium (section), bodyMedium (subtitle) |
| `JellyfinTheme.shapes` | medium (cards) |
| `AnimationDefaults.FOCUS_SCALE` | 1.06× au focus |
| `AnimationDefaults.PRESS_SCALE` | 0.95× au press |
| `rememberReducedMotion()` | Désactive les animations si accessibility activée |
| `focusRestorer()` | Restauration du focus après navigation |

---

## 5. Tests V4B

| # | Test | Vérifie |
|---|------|---------|
| 1 | `tvFocusCard_scales_on_focus` | Scale sémantique = 1.06f après RequestFocus |
| 2 | `tvFocusCard_scales_on_press` | Scale sémantique = 0.95f pendant touch down |
| 3 | `tvCardGrid_first_item_receives_focus` | Premier item focusable, scale = 1.06f après focus |
| 4 | `tvRowList_renders_row_titles` | Titres "Continue Watching" et "Recently Added" visibles |
| 5 | `tvScaffold_provides_background_color` | Contenu rendu dans le scaffold |

**Note** : Tests 1-3 utilisent une propriété sémantique custom (`TvFocusCardScaleKey`)
pour exposer le target scale (avant animation) et le vérifier dans les assertions.

---

## 6. Résultats de build

```
./gradlew checkAll → BUILD SUCCESSFUL
./gradlew assembleGithubDebug → BUILD SUCCESSFUL
./gradlew testGithubDebugUnitTest → BUILD SUCCESSFUL
./gradlew lintGithubDebug → BUILD SUCCESSFUL (0 new warnings)
```

Tests instrumentés (`TvFoundationTest.kt`) : compilent, exécution requiert device/émulateur TV.

---

## 7. Impact sur l'existant

**Zéro impact** — aucun fichier existant modifié (sauf `libs.versions.toml` et `build.gradle.kts`
qui n'ont finalement pas changé par rapport à l'état initial, car `tv-foundation` n'a pas été ajouté).

Les nouveaux composants ne sont référencés par aucun écran existant.
Ils seront utilisés à partir de la Phase 2 (migration des écrans simples).
