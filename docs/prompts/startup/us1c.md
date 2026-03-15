# US1c — UserSelectionViewModel + UserSelectionFragment

## Fichiers crees

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserSelectionViewModel.kt` | Cree | ViewModel pour l'ecran de selection utilisateur |
| `app/.../ui/startup/user/UserSelectionFragment.kt` | Cree | Fragment Compose wrappant UserSelectionScreen |

## Fichiers modifies

| Fichier | Modification |
|---------|-------------|
| `app/.../ui/startup/StartupActivity.kt` | `showServer()` utilise `UserSelectionFragment` au lieu de `ServerFragment` |
| `app/.../di/AppModule.kt` | Ajout `viewModel { UserSelectionViewModel(...) }` dans Koin |

## UserSelectionViewModel

### Package
`org.jellyfin.androidtv.ui.startup.user`

### Dependencies injectees (Koin)
- `ServerRepository` — acces aux serveurs stockes
- `ServerUserRepository` — chargement utilisateurs (stored + public)
- `AuthenticationRepository` — URL image utilisateur
- `AuthenticationPreferences` — tri par derniere utilisation

### UiState

```kotlin
data class UserSelectionUiState(
    val serverName: String = "",
    val users: List<UserUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class UserUiModel(
    val user: User,
    val imageUrl: String?,
)
```

### Methodes publiques

| Methode | Role |
|---------|------|
| `getServer(id: UUID): Server?` | Recupere un serveur par ID |
| `loadUsers(server: Server)` | Charge stored users puis public users, tri par lastUsed/nom |
| `getUserAtIndex(index: Int): User?` | Recupere le modele User a un index donne |

### Comportement au chargement

1. `isLoading = true`, `error = null`
2. Charge `storedServerUsers` → affichage immediat (isLoading = false)
3. Charge `publicServerUsers` → fusion sans doublons, tri, mise a jour
4. En cas d'erreur → `error` rempli, `isLoading = false`

## UserSelectionFragment

### Package
`org.jellyfin.androidtv.ui.startup.user`

### Argument
- `ARG_SERVER_ID` (`"server_id"`) — UUID du serveur en String

### Architecture
- Etend `Fragment`, retourne un `ComposeView`
- `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed`
- Contenu entoure de `JellyfinTheme { ... }`
- Collecte `uiState` via `collectAsState()` et mappe vers `UserSelectionScreen`

### Callbacks

| Callback | Action |
|----------|--------|
| `onUserSelected(index)` | Recupere User, verifie PIN si actif, sinon authentifie |
| `onAddAccount` | Navigation vers `UserLoginFragment` (username = null) |
| `onChangeServer` | Navigation vers `SelectServerFragment` + `StartupToolbarFragment` |

### Authentification
- PIN : `PinEntryDialog.show()` → verification hash SHA-256 → retry si incorrect
- Sans PIN : `authenticationRepository.authenticate(server, AutomaticAuthenticateMethod(user))`
- Gestion des etats : `RequireSignInState` → login, `ServerUnavailableState` → toast erreur

### onResume
- `backgroundService.clearBackgrounds()`
- Recharge les utilisateurs si le serveur existe

## StartupActivity — Modification

```kotlin
// Avant
replace<ServerFragment>(R.id.content_view, null, bundleOf(
    ServerFragment.ARG_SERVER_ID to id.toString()
))

// Apres
replace<UserSelectionFragment>(R.id.content_view, null, bundleOf(
    UserSelectionFragment.ARG_SERVER_ID to id.toString()
))
```

## AppModule — Ajout Koin

```kotlin
viewModel { UserSelectionViewModel(get(), get(), get(), get()) }
```

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:compileGithubReleaseKotlin` → BUILD SUCCESSFUL

## Ce qui n'est PAS dans ce ticket

- Suppression de `ServerFragment.kt` / `fragment_server.xml` (sera fait quand tout est valide)
- Migration de `UserLoginFragment` vers Compose
- Migration de `PinEntryDialog` vers Compose
- Tests unitaires du ViewModel
