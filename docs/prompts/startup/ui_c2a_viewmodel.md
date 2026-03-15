# UI C2a — ServerDiscoveryViewModel

## Fichier créé

| Fichier | Package | Contenu |
|---------|---------|---------|
| `ServerDiscoveryViewModel.kt` | `ui.startup.server` | `ServerDiscoveryUiState` data class + `ServerDiscoveryViewModel` |

## Fichier modifié

Aucun — l'enregistrement Koin existait déjà dans `di/AppModule.kt` (ligne 246) :
```kotlin
viewModel { org.jellyfin.androidtv.ui.startup.server.ServerDiscoveryViewModel(get()) }
```

## Design

### ServerDiscoveryUiState

```kotlin
data class ServerDiscoveryUiState(
    val isScanning: Boolean = true,
    val servers: List<DiscoveredServer> = emptyList(),
    val selectedServer: DiscoveredServer? = null,
    val error: String? = null,
    val quickConnectAvailable: Boolean = false,
)
```

Simplification par rapport à l'ancien design : `isScanning: Boolean` + `error: String?` remplacent la sealed interface `DiscoveryState` (qui reste dans `ServerDiscoveryModels.kt` mais n'est plus utilisée par le ViewModel).

### ServerDiscoveryViewModel

| Méthode | Comportement |
|---------|-------------|
| `init` | Appelle `startDiscovery()` automatiquement |
| `startDiscovery()` | Reset state, lance `discoverServers()` flow. Chaque serveur reçu déclenche un ping en parallèle via `launch {}`. `onCompletion` → `isScanning = false`. `catch` → `error` + `isScanning = false` |
| `selectServer(server)` | Met à jour `selectedServer`, vérifie QuickConnect en async → `quickConnectAvailable` |

### Flux de données

```
init → startDiscovery()
  ├── discoverServers() flow (UDP broadcast, ~3s)
  │   ├── onEach: ajoute serveur à la liste
  │   │   └── launch: pingServer() → met à jour pingMs/version
  │   ├── onCompletion: isScanning = false
  │   └── catch: error = message, isScanning = false
  │
selectServer(server)
  ├── selectedServer = server
  └── launch: isQuickConnectEnabled() → quickConnectAvailable
```

## Dépendances

- `ServerDiscoveryRepository` (C1) — `discoverServers()`, `pingServer()`, `isQuickConnectEnabled()`
- `DiscoveredServer` (C1) — modèle de données

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
- Note : `.collect {}` (avec lambda vide) requis car `Flow.collect()` sans argument n'existe pas
