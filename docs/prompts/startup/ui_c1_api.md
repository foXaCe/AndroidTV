# UI C1 — Server Discovery & QuickConnect API Layer

## Fichiers créés

| Fichier | Package | Contenu |
|---------|---------|---------|
| `ServerDiscoveryModels.kt` | `ui.startup.server` | `DiscoveredServer`, `DiscoveryState`, `QuickConnectFlowState` |
| `ServerDiscoveryRepository.kt` | `ui.startup.server` | Interface + impl — découverte LAN via SDK |
| `QuickConnectRepository.kt` | `ui.startup.server` | Interface + impl — flux QuickConnect via SDK |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `di/AppModule.kt` | Enregistrement Koin des 2 repositories |

## Existant réutilisé

Le SDK Jellyfin (`org.jellyfin.sdk` v1.8.6) expose déjà tout le nécessaire :

| Fonctionnalité | API SDK utilisée |
|----------------|-----------------|
| Découverte LAN | `jellyfin.discovery.discoverLocalServers()` — broadcast UDP port 7359, message "Who is JellyfinServer?", réponse JSON `ServerDiscoveryInfo` (id, name, address, endpointAddress) |
| Ping / version | `api.systemApi.getPublicSystemInfo()` — GET `/System/Info/Public` → `PublicSystemInfo` (version, serverName) |
| QuickConnect enabled | `api.quickConnectApi.getQuickConnectEnabled()` — GET `/QuickConnect/Enabled` → `Boolean` |
| QuickConnect initiate | `api.quickConnectApi.initiateQuickConnect()` — POST `/QuickConnect/Initiate` → `QuickConnectResult` (code, secret) |
| QuickConnect poll | `api.quickConnectApi.getQuickConnectState(secret)` — GET `/QuickConnect/Connect?Secret=…` → `QuickConnectResult` (authenticated) |
| QuickConnect auth | `api.userApi.authenticateWithQuickConnect(secret)` — POST `/Users/AuthenticateWithQuickConnect` → `AuthenticationResult` (accessToken) |

**Note** : `QuickConnectState` existait déjà dans `auth/model/` (sealed class, 4 états). La nouvelle `QuickConnectFlowState` dans `ui.startup.server` a un design différent (sealed interface, 5 états) adapté au Welcome screen.

## ServerDiscoveryRepository

### `discoverServers(): Flow<DiscoveredServer>`
- Utilise `jellyfin.discovery.discoverLocalServers()` (SDK natif, UDP broadcast)
- Map `ServerDiscoveryInfo` → `DiscoveredServer` (sans version ni ping initiaux)
- Flow sur `Dispatchers.IO`, timeout ~3s géré par le SDK
- Émet chaque serveur au fur et à mesure de la réception

### `pingServer(server: DiscoveredServer): DiscoveredServer`
- GET `/System/Info/Public` sur l'adresse du serveur
- Mesure le RTT (System.currentTimeMillis delta)
- Retourne le serveur mis à jour avec `pingMs` et `version`
- En cas d'erreur : retourne `pingMs = -1` (serveur marqué `isReachable = false`)

### `isQuickConnectEnabled(address: String): Boolean`
- GET `/QuickConnect/Enabled` via le SDK
- Retourne `false` en cas d'erreur réseau

## QuickConnectRepository

### `initiate(serverAddress: String): QuickConnectFlowState.CodeReady`
- POST `/QuickConnect/Initiate` via le SDK
- Formate le code 6 chiffres : "847392" → "847-392"
- Retourne code formaté + secret

### `checkAuthorized(serverAddress, secret): Boolean`
- GET `/QuickConnect/Connect?Secret=…` via le SDK
- Retourne `result.authenticated`
- Retourne `false` en cas d'erreur

### `connect(serverAddress, secret): String`
- POST `/Users/AuthenticateWithQuickConnect` via le SDK
- Retourne l'accessToken (throw si null)

## Koin

Enregistré dans `appModule` (pas de nouveau module) :
```kotlin
single<ServerDiscoveryRepository> { ServerDiscoveryRepositoryImpl(get()) }
single<QuickConnectRepository> { QuickConnectRepositoryImpl(get(), get(defaultDeviceInfo)) }
```

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
- 0 TODO bloquants
