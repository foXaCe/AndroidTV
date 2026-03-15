# Koin Anti-Pattern Refactoring

## Résumé

| Métrique | Avant | Après |
|----------|-------|-------|
| KoinComponent hérités | 19 | 6 (justifiés) |
| `by inject()` dans ViewModels | 2 | 0 |
| `KoinJavaComponent.get/inject` | 7 fichiers | 0 |
| `GlobalContext.get()` | 1 | 0 |
| Modules Koin mis à jour | — | 6 |
| BUILD | — | SUCCESSFUL |

## Corrections appliquées

### Catégorie A — ViewModels + KoinComponent (corrigé → injection constructeur)

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `SettingsViewModel` | ViewModel + KoinComponent + `by inject()` | Injection constructeur `(ThemeMusicPlayer)` |
| `LiveTvGuideViewModel` | `KoinJavaComponent.get<>()` dans méthode | Injection constructeur `(ItemMutationRepository, DataRefreshService, SystemPreferences)` |

### Catégorie B — Classes normales + KoinComponent (corrigé → injection constructeur)

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `KeyProcessor` | KoinComponent + 5× `by inject()` | Injection constructeur (5 params) |
| `ItemLauncher` | KoinComponent + 6× `by inject()` | Injection constructeur (6 params) |
| `GuideFilters` | KoinComponent + 1× `by inject()` | Injection constructeur `(SystemPreferences)` |
| `PlaybackLauncher` | KoinComponent + 1× `by inject()` | ThemeMusicPlayer ajouté au constructeur |
| `ThemeMusicPlayer` | KoinComponent + 3× `by inject()` | Injection constructeur (3 params ajoutés) |
| `VideoManager` | KoinComponent + 3× `get()` | Injection constructeur (3 params ajoutés) |
| `PlaybackController` | 10× `KoinJavaComponent.inject` + 4× `get()` | Injection constructeur (16 params) |
| `VideoPlayerControllerImpl` | `KoinJavaComponent.get<UserRepository>` | Injection constructeur `(UserRepository)` |
| `JellyseerrRepositoryImpl` | KoinComponent + 1× `by inject()` | ApiClient ajouté au constructeur |

### Catégorie F — Objects Kotlin (corrigé → lateinit / suppression)

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `Utils` | `object : KoinComponent` (sans aucun inject!) | KoinComponent supprimé |
| `TvManager` | `KoinJavaComponent.get<SystemPreferences>` 3× | `lateinit var systemPreferences` initialisé au démarrage |
| `ItemLauncherHelper` | `KoinJavaComponent.inject` 2× | `lateinit var defaultApi/apiClientFactory` initialisés au démarrage |

### Catégorie G — Extension functions / free functions

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `ItemListViewHelper` | `KoinJavaComponent.inject<ApiClient>` | Utilise `ItemLauncherHelper.defaultApi` |
| `ShuffleUtils` | `GlobalContext.get().get<ShuffleManager>` 2× | `ShuffleManager` passé en paramètre |

### Catégorie I — Composables + KoinJavaComponent

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `SettingsMainScreen` | `KoinJavaComponent.inject<UpdateCheckerService>` | `koinInject()` en paramètre Composable |

### Catégorie J — Inline inject() dans Fragment

| Fichier | Anti-pattern | Fix |
|---------|-------------|-----|
| `CustomPlaybackOverlayFragment` | `val x: T by inject()` inline | Promu en membre de classe |

## KoinComponent justifiés (conservés)

Ces 6 classes nécessitent `KoinComponent` car le framework Android contrôle leur instanciation :

| Classe | Type Android | Raison |
|--------|-------------|--------|
| `AsyncImageView` | Custom View | Créée par layout inflater XML |
| `SimpleInfoRowView` | Custom View | Créée programmatiquement |
| `ClockUserView` | Custom View | Créée par layout inflater XML |
| `UpdateCheckWorker` | CoroutineWorker | Créé par WorkManager |
| `LeanbackChannelWorker` | CoroutineWorker | Créé par WorkManager |
| `MediaContentProvider` | ContentProvider | Créé par le framework Android |

## Modules Koin modifiés

- `AppModule.kt` — Mis à jour : `SettingsViewModel(get())`, `LiveTvGuideViewModel(get()×5)`, `KeyProcessor(get()×5)`, `ItemLauncher(get()×6)`, `ThemeMusicPlayer(get()×4)`, `JellyseerrRepositoryImpl(get()×4)`
- `PlaybackModule.kt` — Mis à jour : `PlaybackLauncher(get()×6)`
- `KoinInitializer.kt` — Ajout initialisation : `TvManager.systemPreferences`, `ItemLauncherHelper.defaultApi/apiClientFactory`
