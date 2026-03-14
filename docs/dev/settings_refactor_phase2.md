# Settings Refactor Phase 2 — ViewModel Extraction

**Date** : 2026-03-09

---

## Résumé

| Refactor | Statut | Notes |
|----------|--------|-------|
| R1 Analyse SettingsJellyseerrScreen | ✓ | 745 LOC → logique métier identifiée |
| R2 JellyseerrSettingsViewModel créé | ✓ | 243 LOC, 7 actions métier, UiState + Events |
| R3 SettingsJellyseerrScreen refactoré | ✓ | 575 LOC (UI pure + dialogs + resolveEvent) |
| R4 Koin module mis à jour | ✓ | AppModule.kt +1 viewModel registration |
| R5 Analyse SettingsPluginScreen | ✓ | 256 LOC — UI pure, pas de ViewModel nécessaire |
| BUILD SUCCESSFUL | ✓ | github + playstore |

---

## Détails

### R1 — Analyse SettingsJellyseerrScreen.kt (avant : 745 LOC)

Classification des blocs :

| Catégorie | Blocs identifiés | Action |
|-----------|-----------------|--------|
| **A) Logique métier** | `performJellyfinLogin()`, `performLocalLogin()`, `performApiKeyLogin()`, reconnect VegafoX, logout, disconnect VegafoX, chargement prefs utilisateur, computation apiKeyStatus | → ViewModel |
| **B) État UI** | isVegafoXMode, vegafoxDisplayName, apiKey, authMethod, serverUrl, isReconnecting, currentUsername | → UiState |
| **C) UI pure** | SettingsColumn items, 4 dialog composables, dialog show/hide states, `rememberPreference` toggles | → Screen composable |

### R2 — JellyseerrSettingsViewModel.kt (243 LOC)

**Chemin** : `ui/settings/screen/vegafox/JellyseerrSettingsViewModel.kt`

#### UiState

```kotlin
data class JellyseerrSettingsUiState(
    val isLoading: Boolean,
    val apiKey: String,
    val authMethod: String,
    val serverUrl: String,
    val vegafoxDisplayName: String,
    val currentUsername: String,
    val isReconnecting: Boolean,
) {
    val apiKeyStatus: ApiKeyStatus  // dérivé (HAS_KEY, NO_KEY_AUTHENTICATED, NOT_LOGGED_IN)
}
```

#### Events (one-shot via Channel)

```kotlin
sealed interface JellyseerrSettingsEvent {
    JellyfinLoginSuccess(hasApiKey)    // → toast auth type
    JellyfinLoginFailed(errorType, message)  // → toast erreur catégorisée
    LocalLoginSuccess(hasApiKey)       // → toast permanent key / cookie
    LocalLoginFailed(message)          // → toast erreur
    ApiKeyLoginSuccess                 // → toast permanent key
    ApiKeyLoginFailed(message)         // → toast erreur
    LogoutSuccess                      // → toast déconnexion
    VegafoXDisconnectSuccess           // → toast déconnexion
    VegafoXReconnectSuccess            // → toast reconnexion OK
    VegafoXNotEnabled                  // → toast non activé
    VegafoXReconnectFailed             // → toast échec
    ServerUrlSaved                     // → toast URL sauvée
    AllFieldsRequired                  // → toast champs requis
}
```

#### Actions métier

| Méthode | Logique extraite |
|---------|------------------|
| `loadState()` | Migration prefs user, chargement initial apiKey/authMethod/serverUrl/vegafoxDisplayName/currentUsername |
| `refreshState()` | Rechargement après opération async |
| `saveServerUrl(url)` | Écriture pref + update UiState + event |
| `reconnectVegafoX()` | ApiClient baseUrl/token → `configureWithVegafoX()` → event |
| `loginWithJellyfin(password)` | Validation, cookie switch, `lastJellyfinUser` pref, repository login → event |
| `loginLocal(email, password)` | Repository login → event |
| `loginWithApiKey(apiKey)` | Repository login → event |
| `logout()` | Repository logout → event |
| `logoutVegafoX()` | Repository logoutVegafoX → event |

#### Injection Koin

```kotlin
viewModel { JellyseerrSettingsViewModel(get(), get(), get(), get(named("global"))) }
```

Dépendances : `JellyseerrRepository`, `UserRepository`, `ApiClient`, `JellyseerrPreferences(global)`

### R3 — SettingsJellyseerrScreen.kt refactoré (575 LOC)

| Section | LOC | Rôle |
|---------|-----|------|
| `SettingsJellyseerrScreen` (main) | ~315 | UI items + dialog states + event collection |
| `resolveEvent()` | ~35 | Conversion événements → message toast + durée |
| `ServerUrlDialog` | ~40 | Dialog URL serveur |
| `JellyfinLoginDialog` | ~40 | Dialog login Jellyfin |
| `LocalLoginDialog` | ~55 | Dialog login local (email + password) |
| `ApiKeyLoginDialog` | ~40 | Dialog login API key |

**Ce qui reste dans le Screen** :
- `rememberPreference` pour `enabled` et `blockNsfw` (toggles directs SharedPreferences — pattern existant)
- Dialog show/hide states (pure UI)
- Toast "please enable first" et "set server URL first" (validation UI sync)
- `resolveEvent()` pour résolution des strings (R.string → Context.getString)

**Ce qui a été supprimé** :
- `rememberCoroutineScope()` + 6 `scope.launch { }` → tout dans le ViewModel
- 3 fonctions `private suspend fun perform*Login()` (~113 LOC) → ViewModel
- Accès direct `jellyseerrRepository`, `userRepository`, `apiClient` → ViewModel
- Computation `apiKeyStatus` with → `UiState.apiKeyStatus` dérivé
- State loading prefs (migration `JellyseerrPreferences.migrateToUserPreferences`) → ViewModel via `repository.getPreferences()`

### R4 — Koin AppModule.kt

Ajout d'une ligne avant les viewModels Jellyseerr existants :
```kotlin
viewModel { JellyseerrSettingsViewModel(get(), get(), get(), get(named("global"))) }
```

### R5 — Analyse SettingsPluginScreen.kt (256 LOC)

| Critère | Résultat |
|---------|----------|
| Appels API async | 1 seul : `pluginSyncService.initialSync()` + `configureJellyseerrProxy()` (2 lignes, on toggle) |
| Error handling | Aucun (fire-and-forget) |
| État loading/error | Aucun |
| Logique métier | Aucune (lecture prefs + toggle + navigation) |
| **Verdict** | **UI pure — pas de ViewModel nécessaire** |

---

## Bilan chiffré

| Fichier | Avant | Après | Delta |
|---------|-------|-------|-------|
| `SettingsJellyseerrScreen.kt` | 745 | 575 | -170 |
| `JellyseerrSettingsViewModel.kt` | — | 243 | +243 (nouveau) |
| `AppModule.kt` | — | +1 | +1 |
| `SettingsPluginScreen.kt` | 256 | 256 | 0 (inchangé) |
| **TOTAL** | 1 001 | 1 074 | +73 |

> Note : Le LOC total augmente légèrement (+73) car le refactor ajoute l'infrastructure ViewModel
> (UiState, Events, Channel, resolveEvent) qui n'existait pas. En contrepartie, la logique métier
> est désormais **testable indépendamment** de l'UI et le Screen ne contient plus aucun `scope.launch`.

---

## Architecture résultante

```
SettingsJellyseerrScreen (Composable — UI pure)
  ├─ koinViewModel<JellyseerrSettingsViewModel>()
  ├─ uiState: StateFlow<JellyseerrSettingsUiState>
  ├─ events: Flow<JellyseerrSettingsEvent> → LaunchedEffect → Toast
  ├─ rememberPreference (enabled, blockNsfw) → SharedPreferences direct
  ├─ Dialog states (showXxxDialog) → local mutableStateOf
  └─ Dialogs (ServerUrl, JellyfinLogin, LocalLogin, ApiKeyLogin, Logout, VegafoXDisconnect)
       └─ onClick → viewModel.xxx()

JellyseerrSettingsViewModel (ViewModel — logique métier)
  ├─ JellyseerrRepository  (login, logout, reconnect, getPreferences)
  ├─ UserRepository         (currentUser)
  ├─ ApiClient              (baseUrl, accessToken pour VegafoX)
  ├─ JellyseerrPreferences  (global — lastJellyfinUser)
  ├─ _uiState: MutableStateFlow → uiState: StateFlow
  ├─ _events: Channel → events: Flow
  └─ Actions: saveServerUrl, reconnectVegafoX, loginWithJellyfin,
              loginLocal, loginWithApiKey, logout, logoutVegafoX
```
