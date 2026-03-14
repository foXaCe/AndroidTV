# US2 — UserLoginScreen + UserLoginFragment (Compose)

## Fichiers crees

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserLoginScreen.kt` | Cree | Ecran de login credentials en Compose |
| `app/.../ui/startup/user/UserLoginFragment.kt` | Cree | Fragment Compose wrappant UserLoginScreen |

## Fichiers modifies

| Fichier | Modification |
|---------|-------------|
| `app/.../ui/startup/user/UserSelectionFragment.kt` | Navigation vers `user.UserLoginFragment` au lieu de `fragment.UserLoginFragment` |
| `app/.../res/layout/fragment_user_login_quick_connect.xml` | `@color/jellyfin_blue` → `@color/vegafox_orange` |
| `app/.../res/values/colors.xml` | Ajout `vegafox_orange` (#FF6B00) |

## UserLoginScreen

### Package
`org.jellyfin.androidtv.ui.startup.user`

### Signature

```kotlin
@Composable
fun UserLoginScreen(
    serverName: String,
    isLoading: Boolean,
    error: String?,
    onLogin: (username: String, password: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
)
```

### Layout (de haut en bas)

| Zone | Composable | Specs |
|------|-----------|-------|
| Header | `Row` : `VegafoXFoxLogo(48dp)` + `VegafoXTitleText(28sp)` | spacing 8dp |
| Espacement | `Spacer` | 24dp |
| Carte | `Box` clip `RoundedCornerShape(20dp)` | 640dp large, fond `VegafoXColors.Surface`, padding 32dp |
| Titre | `Text("Connexion")` | 28sp Bold TextPrimary, via `R.string.btn_login` |
| Sous-titre | `Text(serverName)` | 16sp Normal TextSecondary |
| Champ username | `OutlinedTextField` | label `R.string.input_username`, IME Next |
| Champ password | `OutlinedTextField` | label `R.string.input_password`, IME Done → login |
| Toggle visibilite | `VegafoXIconButton` eye/eye-off | trailingIcon du champ password |
| Erreur | `Text(error)` | 14sp VegafoXColors.Error, affiche si error != null |
| Bouton login | `VegafoXButton Primary fillMaxWidth` | `R.string.action_login`, disabled si isLoading |
| Bouton annuler | `VegafoXButton Ghost` centre | `R.string.btn_cancel` |

### Fond
`VegafoXColors.Background` (#0A0A0F) plein ecran

### Couleurs OutlinedTextField

| Propriete | Valeur |
|-----------|--------|
| focusedBorderColor | OrangePrimary |
| unfocusedBorderColor | Divider |
| focusedContainerColor | Transparent |
| unfocusedContainerColor | Transparent |
| focusedLabelColor | OrangePrimary |
| unfocusedLabelColor | TextSecondary |
| cursorColor | OrangePrimary |
| focusedTextColor | TextPrimary |
| unfocusedTextColor | TextPrimary |

### Icones oeil

Deux `ImageVector` privees definies inline (`IconVisibility`, `IconVisibilityOff`) — reproductions des icones Material "Visibility" 24dp. Evite d'ajouter la dependance `material-icons-extended`.

## UserLoginFragment

### Package
`org.jellyfin.androidtv.ui.startup.user`

### Arguments
- `ARG_SERVER_ID` (`"server_id"`) — UUID du serveur
- `ARG_USERNAME` (`"user_name"`) — username pre-rempli (nullable)

### Architecture
- Etend `Fragment`, retourne un `ComposeView`
- Utilise `UserLoginViewModel` (activityViewModel, existant) pour le login
- Collecte `loginState` et `server` via `collectAsState()`
- Mappe les etats de login vers les props du composable

### Mapping loginState → error

| LoginState | error affiche |
|------------|---------------|
| `AuthenticatingState` | `R.string.login_authenticating` |
| `RequireSignInState` | `R.string.login_invalid_credentials` |
| `ServerUnavailableState` / `ApiClientErrorLoginState` | `R.string.login_server_unavailable` |
| `ServerVersionNotSupported` | `R.string.server_issue_outdated_version` |
| `AuthenticatedState` / `null` | null (pas d'erreur) |

### Callbacks

| Callback | Action |
|----------|--------|
| `onLogin` | `userLoginViewModel.login(username, password)` |
| `onCancel` | `parentFragmentManager.popBackStack()` |

## UserSelectionFragment — Modification

Import supprime :
```kotlin
// Avant
import org.jellyfin.androidtv.ui.startup.fragment.UserLoginFragment
// Apres
// (resolution locale — meme package user.UserLoginFragment)
```

## jellyfin_blue — Correction branding

### fragment_user_login_quick_connect.xml
```xml
<!-- Avant -->
android:textColor="@color/jellyfin_blue"
<!-- Apres -->
android:textColor="@color/vegafox_orange"
```

### colors.xml
```xml
<!-- Ajout -->
<color name="vegafox_orange">#FF6B00</color>
```

## Strings utilisees

| Cle | EN | FR |
|-----|----|----|
| `btn_login` | Login | Connexion |
| `action_login` | Sign in | Se connecter |
| `input_username` | Username | Nom d'utilisateur |
| `input_password` | Password | Mot de passe |
| `btn_cancel` | Cancel | Annuler |
| `login_authenticating` | Authenticating… | — |
| `login_invalid_credentials` | Invalid username and/or password | — |
| `login_server_unavailable` | Unable to connect to server | — |

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:compileGithubReleaseKotlin` → BUILD SUCCESSFUL

## Ce qui n'est PAS dans ce ticket

- Migration de Quick Connect vers Compose (reste en XML `UserLoginQuickConnectFragment`)
- Migration de `PinEntryDialog` vers Compose
- Suppression des anciens `UserLoginFragment` / `UserLoginCredentialsFragment` XML
- Pre-remplissage du champ username quand `forcedUsername` est non-null
- Tests unitaires
