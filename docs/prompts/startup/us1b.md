# US1b — UserSelectionScreen

## Fichier cree

| Fichier | Statut | Description |
|---------|--------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Cree | Ecran de selection d'utilisateur |

## Package

`org.jellyfin.androidtv.ui.startup.user`

## Data class

```kotlin
data class UserInfo(
    val name: String,
    val imageUrl: String?,
)
```

## Signature

```kotlin
@Composable
fun UserSelectionScreen(
    serverName: String,
    users: List<UserInfo>,
    isLoading: Boolean,
    onUserSelected: (index: Int) -> Unit,
    onAddAccount: () -> Unit,
    onChangeServer: () -> Unit,
    modifier: Modifier = Modifier,
)
```

## Layout (de haut en bas)

| Zone | Composable | Specs |
|------|-----------|-------|
| Header | `Row` : `VegafoXFoxLogo(48dp, animated=false)` + `VegafoXTitleText(28sp)` | espacement 8dp |
| Server | `Text(serverName)` | 14sp, Normal, TextSecondary |
| Espacement | `Spacer` | 32dp |
| Titre | `Text("Who's watching?")` | 36sp, Bold, TextPrimary, stringResource |
| Espacement | `Spacer` | 32dp |
| Contenu | `CircularProgressIndicator` si isLoading, sinon `LazyRow` de `UserCard` | spacing 16dp, padding horizontal 48dp |
| Espacement | `Spacer` | 40dp |
| Actions | `Row` : VegafoXButton Secondary "Add account" + VegafoXButton Ghost "Change server" | spacing 16dp |

## Fond

`VegafoXColors.Background` (`#0A0A0F`) plein ecran via `fillMaxSize()`

## Strings utilisees

- `R.string.who_is_watching` — "Who's watching?" (traduit en 40+ langues)
- `R.string.add_user` — "Add account"
- `R.string.change_server` — "Change server"

## Dependencies internes

- `UserCard` (US1a)
- `VegafoXFoxLogo`, `VegafoXTitleText` (ui.startup.compose)
- `VegafoXButton`, `VegafoXButtonVariant` (ui.base.components)
- `VegafoXColors` (ui.base.theme)
- `CircularProgressIndicator` (ui.base)
- Aucun ViewModel, aucun Koin

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:compileGithubReleaseKotlin` → BUILD SUCCESSFUL
