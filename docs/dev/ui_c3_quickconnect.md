# UI C3 — QuickConnect (ViewModel + Screen)

## Fichiers crees

| Fichier | Package | Contenu |
|---------|---------|---------|
| `QuickConnectViewModel.kt` | `ui.startup.server` | `QuickConnectUiState` data class + `QuickConnectViewModel` |
| `QuickConnectScreen.kt` | `ui.startup.server` | Composable principal + 3 etats visuels |

## Fichier modifie

| Fichier | Modification |
|---------|-------------|
| `di/AppModule.kt` | Ajout `viewModel { params -> QuickConnectViewModel(params.get(), get()) }` |

## Partie 1 — QuickConnectViewModel

### QuickConnectUiState

```kotlin
data class QuickConnectUiState(
    val server: DiscoveredServer,
    val code: String = "",        // ex: "847-392"
    val secret: String = "",      // interne, jamais affiche
    val isLoading: Boolean = true,
    val isWaiting: Boolean = false,
    val isAuthenticated: Boolean = false,
    val accessToken: String? = null,
    val error: String? = null,
)
```

### Actions

| Methode | Comportement |
|---------|-------------|
| `init` | Appelle `initiate()` automatiquement |
| `initiate()` | `isLoading = true` → `quickConnectRepository.initiate(address)` → met a jour `code` + `secret` → `isLoading = false`. Catch → `error` |
| `startPolling()` | Lance coroutine : `isWaiting = true`, boucle toutes les 3s pendant max 5 min (100 tentatives). Si `checkAuthorized()` → `connect()` → `isAuthenticated = true` + `accessToken` |
| `cancel()` | Annule le job de polling, `isWaiting = false` |
| `onCleared()` | Annule le job de polling |

### Koin

Enregistre avec parametre serveur :
```kotlin
viewModel { params -> QuickConnectViewModel(params.get(), get()) }
```

Cote Screen :
```kotlin
viewModel: QuickConnectViewModel = koinViewModel { parametersOf(server) }
```

## Partie 2 — QuickConnectScreen

### Parametres

- `server: DiscoveredServer` — serveur cible (passe a koinViewModel via parametersOf)
- `onAuthenticated: (DiscoveredServer, String) -> Unit` — callback avec serveur + accessToken
- `onBack: () -> Unit` — retour
- `viewModel` — injecte via `koinViewModel { parametersOf(server) }`

### 3 etats visuels

#### Etat 1 — Loading (`isLoading = true`)
- Spinner anime (sweep gradient orange, rotation 360° en 1s)
- Texte "Connexion a NomServeur…" en TextSecondary

#### Etat 2 — Code pret (`code != ""`, pas authenticated)
- Nom serveur 13sp OrangePrimary, letterSpacing 3sp
- Titre "Connexion rapide" 26sp bold TextPrimary
- Instructions multilignes 14sp TextSecondary
- **CodeBox** : fond Surface, border OrangeBorder si isWaiting sinon Divider, glow OrangeGlow si isWaiting. Chiffres 44sp bold OrangePrimary Monospace, tiret 28sp TextHint
- **Bouton** : si isWaiting → fond Surface + petit spinner + "En attente de confirmation…" ; sinon → fond OrangePrimary + "J'ai entre le code" avec glow au focus
- Erreur en Error 13sp si presente
- Lien "Retour" 13sp TextHint → onBack

#### Etat 3 — Authenticated (`isAuthenticated = true`)
- Icone succes : Box 72dp cercle, border 2dp Success, fond SuccessGlow, checkmark "✓" 32sp
- Animation fadeIn + scaleIn depuis 0.9 (400ms)
- "Connecte !" 24sp bold TextPrimary
- "NomServeur · host" 14sp TextSecondary
- `LaunchedEffect` : delay 800ms → `onAuthenticated(server, accessToken)`

### Composants internes

| Composable | Description |
|------------|-------------|
| `LoadingState` | Spinner + texte connexion |
| `Spinner` | Box 32dp, rotation infinie, sweep gradient orange |
| `CodeReadyState` | Layout complet etat 2 |
| `CodeBox` | Affichage code formate avec split("-") |
| `WaitingButton` | Fond Surface, petit spinner + texte attente |
| `SmallSpinner` | Box 18dp, meme pattern que Spinner |
| `ConfirmButton` | Fond OrangePrimary, focusable avec glow, scale au focus |
| `AuthenticatedState` | Icone succes animee + textes + LaunchedEffect navigation |

## Tokens utilises

- Fonds : `Background`, `Surface`
- Orange : `OrangePrimary`, `OrangeBorder`, `OrangeGlow`
- Texte : `TextPrimary`, `TextSecondary`, `TextHint` (= TextTertiary du spec)
- Etats : `Success`, `SuccessGlow`, `Error`
- Bordure : `Divider`

## Dependances

- `QuickConnectRepository` (C1) — `initiate()`, `checkAuthorized()`, `connect()`
- `DiscoveredServer` (C1) — modele de donnees
- `VegafoXColors` (W1) — palette Dark Premium
- `koin-androidx-compose` — `koinViewModel()` + `parametersOf`

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
