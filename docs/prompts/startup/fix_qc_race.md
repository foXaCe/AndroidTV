# Fix — Race condition QuickConnect dans ServerDiscoveryScreen

## Problème

```kotlin
// ServerDiscoveryScreen.kt:86-89 (AVANT)
onServerClick = { server ->
    viewModel.selectServer(server)                        // lance coroutine async
    onServerSelected(server, uiState.quickConnectAvailable)  // lit l'ANCIEN state (false)
}
```

`selectServer()` lançait une coroutine pour appeler `isQuickConnectEnabled()`, mais `onServerSelected` était appelé immédiatement avec `uiState.quickConnectAvailable` qui valait encore `false` (pas encore mis à jour). QuickConnect n'était **jamais** proposé, même quand le serveur le supportait.

## Fix

### ServerDiscoveryViewModel.kt

`selectServer` remplacé par `selectServerAndNavigate` qui prend un callback appelé **après** le résultat de `isQuickConnectEnabled` :

```kotlin
fun selectServerAndNavigate(
    server: DiscoveredServer,
    onResult: (Boolean) -> Unit,
) {
    _uiState.update { it.copy(selectedServer = server) }
    viewModelScope.launch {
        val qcAvailable = discoveryRepository.isQuickConnectEnabled(server.address)
        _uiState.update { it.copy(quickConnectAvailable = qcAvailable) }
        onResult(qcAvailable)
    }
}
```

### ServerDiscoveryScreen.kt

Le clic appelle `selectServerAndNavigate` et navigue dans le callback :

```kotlin
onServerClick = { server ->
    viewModel.selectServerAndNavigate(server) { qcAvailable ->
        onServerSelected(server, qcAvailable)
    }
}
```

## Séquence corrigée

1. Utilisateur clique sur un serveur
2. `selectServerAndNavigate` → met à jour `selectedServer`
3. Coroutine : `GET /QuickConnect/Enabled` → attend la réponse
4. Callback `onResult(true/false)` → navigue vers QuickConnect ou login

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ServerDiscoveryViewModel.kt` | `selectServer` → `selectServerAndNavigate(server, onResult)` |
| `ServerDiscoveryScreen.kt` | Appel avec callback au lieu de lecture synchrone du state |

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
