# V2C — Optimisations mineures P13-P20

**Date** : 2026-03-08
**Build** : assembleRelease BUILD SUCCESSFUL
**Branche** : main

---

## Tableau de synthèse

| P# | Problème | Correction appliquée | Statut |
|----|----------|---------------------|--------|
| P13 | Layouts imbriqués excessifs | Suppression des RelativeLayout redondants autour des ConstraintLayout dans `view_row_details.xml` et `view_jellyseerr_details_row.xml` (gain d'1 passe de layout) | ✅ Corrigé |
| P14 | Nested ScrollViews guide TV | Pattern intentionnel pour la grille TV guide (scroll V+H simultané). Remplacement impossible sans réécriture complète du guide TV en Compose. | ✅ RAS |
| P15 | Overdraw Jellyseerr | 1 seul background redondant trouvé : `android:background="@android:color/transparent"` supprimé dans `fragment_jellyseerr_discover_new.xml`. Tous les autres backgrounds sont intentionnels (badges, statuts, gradients). | ✅ Corrigé |
| P16 | postDelayed sans cleanup | 3 fragments corrigés : `HomeRowsFragment.kt` (focusRunnable + removeCallbacks dans onDestroyView), `DiscoverFragment.kt` (idem), `JellyseerrDiscoverRowsFragment.kt` (restoreRunnable + clearFlagsRunnable + onDestroyView ajouté) | ✅ Corrigé |
| P17 | @Stable/@Immutable manquants | 10 data classes annotées : `MediaBadge` (@Immutable), `TimedLine` (@Immutable), `MediaToastData` (@Immutable), `GroupItem` (@Immutable), `SortOption` (@Immutable), `LibrarySelection` (@Stable), `SearchResultGroup` (@Stable), `SelectedItemState` (@Stable), `TrackAction` (@Stable), `PlayerOverlayVisibilityState` (@Stable) | ✅ Corrigé |
| P18 | Predictive Back Android 14 | Non applicable sur Android TV. L'app cible le Leanback launcher et utilise correctement `OnBackPressedCallback` pour le bouton retour de la télécommande. `enableOnBackInvokedCallback` est un feature phone/tablette uniquement. | ✅ RAS |
| P19 | Quality param CardPresenter | Paramètre `quality` ajouté à `JellyfinImage.getUrl()` + `quality = 80` passé dans CardPresenter. Réduction estimée ~30% bande passante sur les cartes Leanback. | ✅ Corrigé |
| P20 | MutableStateFlow exposé | Pattern légitime pour les 4 instances trouvées (LiveTvGuideFragmentHelper ×2, BrowseGridFragmentHelper ×2). Le MutableStateFlow est partagé intentionnellement entre le helper Kotlin et le fragment Java pour le toggle bidirectionnel de visibilité des dialogs. Les ViewModels suivent déjà correctement le pattern `_field`/`asStateFlow()`. | ✅ RAS |

---

## Détail des corrections

### P13 — Layouts imbriqués

**Fichiers modifiés** :
- `app/src/main/res/layout/view_row_details.xml` — RelativeLayout root supprimé, ConstraintLayout promu en root
- `app/src/main/res/layout/view_jellyseerr_details_row.xml` — idem

**Fichiers non modifiés (acceptables)** :
- `fragment_server_add.xml` — 3 niveaux (acceptable)
- `fragment_select_server.xml` — 4 niveaux avec ScrollView (pattern standard)
- `fragment_user_login.xml` — 4 niveaux avec ScrollView (pattern standard)

### P14 — Nested ScrollViews guide TV

**Analyse** : `overlay_tv_guide.xml` et `live_tv_guide.xml` utilisent ObservableScrollView (V) contenant ObservableHorizontalScrollView (H) pour la grille de programmes. C'est un pattern volontaire et fonctionnel pour le guide TV (scroll simultané vertical par chaînes + horizontal par temps). Le remplacement nécessiterait une réécriture complète en Compose — hors scope.

### P15 — Overdraw Jellyseerr

**Analyse exhaustive** :
- `fragment_jellyseerr_settings.xml` — Backgrounds noirs/gris intentionnels (sections visuelles)
- `fragment_jellyseerr_requests.xml` — Backgrounds intentionnels (indicateurs de statut)
- `item_jellyseerr_content.xml` — Badges positionnés en absolu, pas d'overdraw
- `fragment_jellyseerr_discover_new.xml` — **1 background `@android:color/transparent` redondant supprimé** (ConstraintLayout est transparent par défaut)

### P16 — postDelayed sans cleanup

**3 fragments corrigés** :

1. **HomeRowsFragment.kt** — `focusRunnable` stocké, `removeCallbacks()` dans `onDestroyView()`
2. **DiscoverFragment.kt** — `focusRunnable` stocké, `removeCallbacks()` dans `onDestroyView()`
3. **JellyseerrDiscoverRowsFragment.kt** — `restoreRunnable` + `clearFlagsRunnable` stockés, `onDestroyView()` ajouté avec cleanup des deux

**Déjà propres** : TrailerPlayerFragment, ExoPlayerTrailerView, les 5 vues saisonnières (LeaffallView, SnowfallView, SummerView, PetalfallView, HalloweenView)

### P17 — @Stable/@Immutable manquants

**10 data classes annotées** :

| Classe | Fichier | Annotation | Raison |
|--------|---------|------------|--------|
| `MediaBadge` | ItemDetailsViewModel.kt | @Immutable | String vals uniquement |
| `TimedLine` | LyricsBox.kt | @Immutable | Duration + String vals |
| `MediaToastData` | MediaToastData.kt | @Immutable | Int + Float? vals |
| `GroupItem` | SyncPlayDialog.kt | @Immutable | UUID + String + Int vals |
| `SortOption` | LibraryBrowseViewModel.kt | @Immutable | Int + enums vals |
| `LibrarySelection` | ShuffleOptionsDialog.kt | @Stable | Contient BaseItemDto (pas garanti @Immutable) |
| `SearchResultGroup` | SearchResultGroup.kt | @Stable | Contient Collection\<BaseItemDto> |
| `SelectedItemState` | SelectedItemState.kt | @Stable | Contient BaseItemDto? |
| `TrackAction` | ItemDetailsComponents.kt | @Stable | Contient lambda onClick |
| `PlayerOverlayVisibilityState` | PlayerOverlayLayout.kt | @Stable | Contient lambdas toggle/show/hide |

### P18 — Predictive Back Gesture

**Non applicable** : Android TV utilise le bouton retour de la télécommande, pas les gestes. L'app déclare `android.software.leanback` comme required et utilise correctement `OnBackPressedCallback` (MainActivity, CustomPlaybackOverlayFragment) et `BackHandler` Compose (speechRecognizer). `enableOnBackInvokedCallback` est spécifique aux appareils tactiles.

### P19 — Quality param CardPresenter

**Fichiers modifiés** :
- `util/apiclient/JellyfinImage.kt` — Paramètre `quality: Int? = null` ajouté à `getUrl()`
- `ui/presentation/CardPresenter.kt` — `quality = 80` passé dans l'appel `image.getUrl()`

Cohérent avec `GenresGridViewModel` (quality=80) et `MediaBarSlideshowViewModel` (quality=90).

### P20 — MutableStateFlow exposé

**4 instances analysées, toutes légitimes** :

Les helpers `addSettingsOptions()`, `addSettingsFilters()` (LiveTvGuideFragmentHelper) et `createSettingsVisibility()`, `addSettings()` (BrowseGridFragmentHelper) utilisent un `MutableStateFlow<Boolean>` partagé entre :
- Le helper Kotlin (set `.value = false` dans `onDismissRequest` du dialog)
- Le fragment Java appelant (set `.setValue(true)` sur click du bouton settings)

C'est un toggle bidirectionnel intentionnel. Tous les ViewModels suivent déjà correctement le pattern `private _field` / `public asStateFlow()`.

---

## Compilation

```
BUILD SUCCESSFUL in 4m 16s
356 actionable tasks: 50 executed, 306 up-to-date
```
