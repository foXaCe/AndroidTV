# Audit dette technique générale — VegafoX Android TV

**Date** : 2026-03-16
**Scope** : Codebase complet (`app/src/main/`, modules Gradle, manifest, tests)

---

## Point 1 — Fragments legacy résiduels

### Résumé

| Métrique | Valeur |
|----------|--------|
| Total fragments | 45 |
| Compose (ComposeView ou content) | 42 (93%) |
| XML/ViewBinding legacy | 2 (4%) |
| Hybrid (Compose + XML) | 1 (2%) |

### Fragments encore en architecture legacy

| Fragment | Fichier | UI Type | Rôle |
|----------|---------|---------|------|
| `ServerAddFragment` | `ui/startup/fragment/ServerAddFragment.kt` | XML (`FragmentServerAddBinding`) | Formulaire saisie manuelle adresse serveur |
| `SettingsFragment` (Jellyseerr) | `ui/jellyseerr/SettingsFragment.kt` | XML (`fragment_jellyseerr_settings.xml`) | Configuration Jellyseerr (URL, login) |
| `ItemDetailsFragment` | `ui/itemdetail/v2/ItemDetailsFragment.kt` | Hybrid (FrameLayout + ImageView backdrop + ComposeView overlay) | Détails item avec backdrop XML et overlay Compose |

### Fragments Compose par zone

| Zone | Nb fragments | Status audit |
|------|-------------|--------------|
| Startup | 11 | Audité |
| Home | 1 | Audité |
| Browsing/Library | 14 | Audité |
| Item Detail | 5 | Audité |
| Playback/Player | 7 | Audité |
| Live TV | 1 | Audité |
| Search | 1 | Non audité |
| Jellyseerr | 8 | Non audité |

### Patterns Compose utilisés

- `ComposeView(requireContext()).apply { setContent { } }` — 28 fragments
- `Fragment Compose API: content { }` — 10 fragments
- `FrameLayout + ComposeView nested` — 4 fragments
- `ViewBinding.inflate()` — 2 fragments (legacy)

### Actions recommandées

1. **ServerAddFragment** → Migrer vers Compose (formulaire simple)
2. **Jellyseerr SettingsFragment** → Migrer vers Compose (formulaire de config)
3. **ItemDetailsFragment** → Le backdrop XML peut être remplacé par AsyncImage Compose pour éliminer le layout hybrid

---

## Point 2 — Java résiduels

### Résumé

| Métrique | Valeur |
|----------|--------|
| Fichiers Java | 1 |
| Lignes | 438 |
| Migration recommandée | Non |

### Fichier unique

| Fichier | Rôle | Complexité |
|---------|------|------------|
| `org/schabi/newpipe/extractor/utils/Utils.java` | Shadow class NewPipe Extractor — fournit utilitaires URL/parsing compatibles API 23+ | Moyenne |

**Pourquoi en Java** : C'est une shadow class intentionnelle qui doit rester synchronisée avec le upstream NewPipe Extractor (Java). Le commentaire en tête du fichier stipule : "This file MUST be kept in sync with the upstream Utils class". Migrer en Kotlin compliquerait la synchronisation.

**23 méthodes statiques** : encodeUrlUtf8, decodeUrlUtf8, removeNonDigitCharacters, mixedNumberWordToLong, checkUrl, replaceHttpWithHttps, getQueryValue, isHTTP, removeMAndWWWFromUrl, removeUTF8BOM, getBaseUrl, followGoogleRedirectIfNeeded, isNullOrEmpty (3 surcharges), isBlank, join, nonEmptyAndNullJoin, getStringResultFromRegexArray (4 surcharges).

**Verdict** : Ne pas migrer. Conserver en Java pour faciliter la synchronisation upstream.

---

## Point 3 — Dépendances Gradle obsolètes ou inutilisées

### Dépendances obsolètes

| Dépendance | Version actuelle | Version disponible | Retard | Sévérité |
|------------|-----------------|-------------------|--------|----------|
| Material3 | 1.4.0 | 1.7.x+ | 3 mineures | **HAUTE** |
| Kotlin | 2.3.10 | 2.5.x+ | 2 mineures | **HAUTE** |
| Media3 (ExoPlayer) | 1.9.2 | 1.10+ | 1 mineure | Moyenne |
| Ktor | 3.4.1 | 3.5.x+ | 1 mineure | Moyenne |
| Material Icons Extended | 1.7.8 (hardcodé) | Incohérent avec Material3 1.4.0 | — | Moyenne |

### Dépendances probablement inutilisées

| Dépendance | Déclaration | Usage trouvé | Recommandation |
|------------|-------------|-------------|----------------|
| `androidx-cardview` | libs.versions.toml:12, build.gradle.kts:240 | 0 imports Kotlin | Vérifier layouts XML restants, sinon supprimer |

### Dépendances non gérées dans TOML

| Dépendance | Fichier | Version |
|------------|---------|---------|
| `io.github.peerless2012:ass-media` | `playback/media3/exoplayer/build.gradle.kts:42` | 0.4.0 |
| `io.github.peerless2012:ass-kt` | `playback/media3/exoplayer/build.gradle.kts:43` | 0.4.0 |

**Recommandation** : Centraliser dans `libs.versions.toml`.

### Dépendances dupliquées

Aucune duplication. Les bundles sont bien organisés (coil, ktor, lifecycle, compose, markwon, acra).

### Dépendances à jour

- Android Gradle Plugin 9.1.0
- Accompanist 0.37.3
- Compose-UI 1.10.4
- Core AndroidX 1.17.0
- Fragment AndroidX 1.8.9

---

## Point 4 — TODO et FIXME dans le code

### Résumé

| Type | Occurrences |
|------|-------------|
| TODO | 12 |
| FIXME | 0 |
| HACK | 0 |
| XXX | 0 |
| @Deprecated | 21 |

### TODO — Dette technique réelle

| Fichier | Ligne | Contenu | Sévérité |
|---------|-------|---------|----------|
| `ui/navigation/Destinations.kt` | 45, 60, 95, 139, 164, 178 | `TODO only pass item id instead of complete JSON to browsing destinations` | **HAUTE** — 6 occurrences, impact perf/testabilité |
| `ui/playback/PlaybackControllerHelper.kt` | 230 | `TODO: ask-to-skip UI removed during Fragment migration` | **HAUTE** — Régression UX, feature perdue |
| `ui/playback/PlaybackController.kt` | 1522 | `TODO, implement speed change handling` | Moyenne — Feature manquante |
| `ui/browsing/composable/inforow/BaseItemInfoRow.kt` | 386 | `TODO: Appears to always be null? Maybe an API issue?` | Moyenne — Investigation requise |

### TODO — Notes informatives (peuvent rester)

| Fichier | Ligne | Contenu | Status |
|---------|-------|---------|--------|
| `util/apiclient/JellyfinImage.kt` | 268 | `TODO Add SeriesTimerInfoDto once API types are fixed` | Bloqué par SDK Jellyfin |
| `ui/base/list/ListItemContent.kt` | 35 | `TODO: Add suitable space token for this padding` | Attente design system |
| `ui/base/list/ListMessage.kt` | 20 | `TODO: Add suitable space token for this padding` | Attente design system |

### @Deprecated — À migrer

| Fichier | Élément | Message | Action |
|---------|---------|---------|--------|
| `preference/UserSettingPreferences.kt` | `backgroundBlurAmount` (l.67) | Remplacé par `detailsBackgroundBlurAmount`/`browsingBackgroundBlurAmount` | Migrer |
| `preference/UserSettingPreferences.kt` | `homesection0`→`homesection9` (l.89-117) | Remplacé par `homeSectionsJson` | Migrer |
| `preference/UserSettingPreferences.kt` | `homesections` (l.145) | Liste aggrégée dépréciée | Migrer |
| `ui/settings/routes.kt` | `VEGAFOX_HOME_ROWS_IMAGE` (l.65) | Moved to HOME_ROWS_IMAGE_TYPE | Supprimer |
| `ui/settings/routes.kt` | `SYNCPLAY` (l.76) | Unused orphan route | Supprimer |
| `ui/settings/routes.kt` | `TELEMETRY` (l.78) | VegafoX does not collect telemetry | Supprimer |
| `util/Utils.kt` | `showToast` (l.18, 37) | Use Toast.makeText | Remplacer |
| `ui/home/compose/sidebar/PremiumSideBar.kt` | `PREMIUM_SIDEBAR_WIDTH` (l.81) | Use PREMIUM_SIDEBAR_WIDTH_COLLAPSED | Remplacer |

### @Deprecated — À conserver

| Fichier | Élément | Raison |
|---------|---------|--------|
| `util/apiclient/Response.kt` | `Response<T>`, `EmptyResponse` | Bridge Java→Kotlin, support legacy |
| `constant/HomeSectionType.kt` | `NEXT_UP` | Compatibilité JSON désérialisation |

---

## Point 5 — Code dupliqué

### Duplications critiques

#### 1. `getItemImageUrl()` — 4 implémentations

| Fichier | Visibilité |
|---------|------------|
| `ui/browsing/compose/BrowseMediaCard.kt` (l.307-356) | `internal` |
| `ui/browsing/compose/LibraryBrowseScreen.kt` | `private` |
| `ui/browsing/v2/MusicBrowseFragment.kt` | `private` |
| `integration/LeanbackChannelWorker.kt` (l.272) | `private` |

**Logique identique** : THUMB → BACKDROP → PRIMARY avec fallback parent.
**Refactoring** : Créer `BaseItemDto.getCardImageUrl()` dans `BaseItemImageExtensions.kt`.
**Effort** : 2-3 jours.

#### 2. `withServerId()` — Copie exacte

| Fichier |
|---------|
| `ui/search/SearchRepository.kt` (l.39) |
| `data/repository/MultiServerRepository.kt` (l.104) |

**Code identique** : `copy(serverId = serverId.toString())`.
**Refactoring** : Déplacer dans `BaseItemExtensions.kt`.
**Effort** : 1 heure.

#### 3. Pattern AsyncImage + ImageRequest — 5+ places

| Fichier |
|---------|
| `ui/browsing/compose/BrowseMediaCard.kt` (l.172-181) |
| `ui/browsing/compose/MediaPosterCard.kt` (l.91-102) |
| + autres composables avec AsyncImage |

**Logique identique** : ImageRequest.Builder avec cache policies, crossfade, scale.
**Refactoring** : Créer `CachedAsyncImage()` composable réutilisable.
**Effort** : 2-3 jours.

#### 4. Pattern ViewModel State Management — 6+ ViewModels

Tous les ViewModels utilisent le même pattern :
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState = _uiState.asStateFlow()
fun retry() { _uiState.value = _uiState.value.copy(error = null, isLoading = true); loadData() }
```

**Refactoring possible** : `BaseViewModelWithState<T>` abstrait.
**Effort** : 3-5 jours. **Impact** : Réduction ~20-30% code ViewModel.

### Duplications acceptables (ne pas toucher)

- **Headers** : Variations suffisamment différentes
- **Skeleton components** : Déjà bien centralisés via SkeletonPresets
- **StateContainer** : Déjà correctement abstrait

---

## Point 6 — Tests manquants

### Résumé couverture

| Catégorie | Total | Testés | Couverture |
|-----------|-------|--------|------------|
| ViewModels | 32 | 4 | 12.5% |
| Repositories | 22 | 0 | 0% |
| Playback Logic | 6+ classes | 2 | ~33% |
| UI Composables | 200+ fichiers | ~15 | ~7.5% |
| Services | 8+ majeurs | 0 | 0% |
| **Total fichiers test** | — | **17** | — |
| **Total test cases** | — | **~150** | — |

### Tests existants

| Fichier | Type | Ce qu'il teste | Nb tests |
|---------|------|---------------|----------|
| `SeekProviderTests.kt` | Unit (KoTest) | Calcul positions seek | 3 |
| `VideoSpeedControllerTests.kt` | Unit (KoTest+mockk) | Contrôle vitesse playback | 9 |
| `LibraryBrowseViewModelTests.kt` | Unit (KoTest+mockk) | État UI browse library | 12 |
| `GenresGridViewModelTests.kt` | Unit (KoTest+mockk) | État UI grille genres | 11 |
| `ItemDetailsViewModelTests.kt` | Unit (KoTest+mockk) | État détails item + badges média | 40+ |
| `UiErrorMappingTests.kt` | Unit (KoTest) | Mapping exception→UiError | 11 |
| `TimeUtilsTests.kt` | Unit (KoTest) | Formatage temps | 2 |
| `ServerDiscoveryTest.kt` | Unit (KoTest+Flow) | Découverte serveur | 6 |
| `PreferenceStoreTests.kt` | Unit (KoTest) | Stockage préférences | 2 |
| `DesignSystemTest.kt` | Instrumented (Compose) | Thème, couleurs, animations | 3 |
| `SkeletonTest.kt` | Instrumented (Compose) | Composants skeleton | 3 |
| `StateContainerTest.kt` | Instrumented (Compose) | États LOADING/CONTENT/ERROR/EMPTY | 5 |
| `DpadFocusTest.kt` | Instrumented (Compose) | Navigation DPAD focus | 3 |
| `ErrorBannerTest.kt` | Instrumented (Compose) | Bannière erreur | 5 |
| `TvFoundationTest.kt` | Instrumented (Compose) | Composables TV (focus, scaling, grid) | 6 |
| `PlayerOverlayTest.kt` | Instrumented (Compose) | Overlay contrôles vidéo | 5 |

### Lacunes critiques (0% couverture)

| Composant | Priorité | Impact |
|-----------|----------|--------|
| **PlaybackController** | Critique | Logique master playback (seek, play, pause) |
| **SessionRepository** | Critique | Auth session, token refresh |
| **ServerRepository** | Haute | Connexion serveur, stockage |
| **HomeViewModel** | Haute | Page d'accueil, hero, sidebar |
| **SearchViewModel** | Haute | Recherche globale |
| **ItemRepository** | Haute | Fetch et cache items |
| **SocketHandler** | Moyenne | WebSocket events |
| **PluginSyncService** | Moyenne | Synchro plugins |

---

## Point 7 — Permissions et Manifest

### Permissions déclarées

| Permission | Utilisée | Status |
|------------|----------|--------|
| `INTERNET` | Oui | OK |
| `ACCESS_NETWORK_STATE` | Oui (StartupActivity l.99) | OK |
| `ACCESS_WIFI_STATE` | Indirecte (lint ignore `LeanbackUsesWifi`) | OK |
| `RECORD_AUDIO` | Oui (speechRecognizer.kt l.140-151) | OK |
| `FOREGROUND_SERVICE` | Oui (Workers) | OK |
| `WRITE_EPG_DATA` | **Non** | **Supprimer** |
| `REQUEST_INSTALL_PACKAGES` | **Non** | **Vérifier ou supprimer** |

### Composants déclarés

| Type | Nb | Status |
|------|-----|--------|
| Activities | 3 (StartupActivity, MainActivity, ExternalPlayerActivity) | 2 sans `android:exported` explicite |
| Services | 1 (LibraryDreamService) | OK |
| Providers | 4 (InitializationProvider, MediaContentProvider, ImageProvider, FileProvider) | OK |
| Initializers | 3 (LogInitializer, KoinInitializer, SessionInitializer) | OK |

### Problèmes identifiés

| Problème | Sévérité | Action |
|----------|----------|--------|
| `MainActivity` sans `android:exported` explicite | Moyenne | Ajouter `android:exported="false"` |
| `ExternalPlayerActivity` sans `android:exported` explicite | Moyenne | Ajouter `android:exported="false"` |
| Permission `WRITE_EPG_DATA` inutilisée | Haute | Supprimer |
| Permission `REQUEST_INSTALL_PACKAGES` probablement inutilisée | Haute | Vérifier UpdateCheckerService, sinon supprimer |

---

## Point 8 — Synthèse et priorisation

### Tableau récapitulatif

| # | Dette technique | Sévérité | Effort | Catégorie |
|---|----------------|----------|--------|-----------|
| 1 | **Couverture de tests ~7-12%** — 0% sur Repositories et Services | **Bloquante** | Semaines | Tests |
| 2 | **Material3 1.4.0 → 1.7.x** — 3 versions de retard | **Haute** | 2-3 jours | Dépendances |
| 3 | **Kotlin 2.3.10 → 2.5.x** — 2 versions de retard | **Haute** | 1-2 jours | Dépendances |
| 4 | **Destinations.kt — JSON complet au lieu d'ID** — 6 TODOs architecture | **Haute** | 3-5 jours | Architecture |
| 5 | **Permissions manifest inutilisées** (WRITE_EPG_DATA, REQUEST_INSTALL_PACKAGES) | **Haute** | 1 heure | Manifest |
| 6 | **Migration préférences home sections** — @Deprecated homesection0-9 | **Haute** | 2-3 jours | Préférences |
| 7 | **getItemImageUrl dupliqué** — 4 implémentations identiques | **Moyenne** | 2-3 jours | Code dupliqué |
| 8 | **Skip UI perdue** — Feature supprimée lors migration Fragment | **Moyenne** | 2-3 jours | Régression |
| 9 | **Activities sans android:exported** | **Moyenne** | 1 heure | Manifest |
| 10 | **AsyncImage pattern dupliqué** — 5+ copies | **Moyenne** | 2-3 jours | Code dupliqué |
| 11 | **Media3 1.9.2 → 1.10+** | **Moyenne** | 1 jour | Dépendances |
| 12 | **2 fragments XML legacy** (ServerAdd, Jellyseerr Settings) | **Moyenne** | 2-3 jours | Migration |
| 13 | **withServerId copié** — 2 copies exactes | **Basse** | 1 heure | Code dupliqué |
| 14 | **Routes orphelines @Deprecated** (SYNCPLAY, TELEMETRY) | **Basse** | 1 heure | Cleanup |
| 15 | **showToast wrapper inutile** | **Basse** | 1 heure | Cleanup |
| 16 | **cardview probablement inutilisée** | **Basse** | 1 heure | Dépendances |
| 17 | **Dépendances ASS non centralisées dans TOML** | **Basse** | 30 min | Dépendances |
| 18 | **PlaybackController speed change** — TODO non implémenté | **Basse** | 1-2 jours | Feature |
| 19 | **ViewModel state pattern** — Refactoring optionnel base class | **Basse** | 3-5 jours | Architecture |

### Verdict global

Le codebase est en **bon état** avec une migration Compose quasi-complète (93%). La dette technique principale se concentre sur :

1. **Tests** — Priorité absolue, couverture dangereusement basse
2. **Dépendances** — Material3 et Kotlin significativement en retard
3. **Architecture** — Passage de JSON complet dans Destinations.kt
4. **Manifest** — Permissions fantômes à nettoyer

Le reste (code dupliqué, fragments legacy, TODOs) est de la dette gérable qui peut être traitée progressivement sans urgence.
