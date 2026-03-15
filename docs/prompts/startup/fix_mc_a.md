# fix_mc_a — ServerDiscoveryViewModel : nouveaux champs et probeManualAddress

## Fichier modifié
- `app/src/main/java/org/jellyfin/androidtv/ui/startup/server/ServerDiscoveryViewModel.kt`

## Changements

### 1. Nouveaux champs dans `ServerDiscoveryUiState`

| Champ | Type | Défaut | Rôle |
|---|---|---|---|
| `isCheckingManual` | `Boolean` | `false` | Indique qu'un probe manuel est en cours |
| `manualError` | `String?` | `null` | Adresse du serveur injoignable (non-null = erreur) |
| `noServerDetected` | `Boolean` | `false` | `true` après 8s si aucun serveur trouvé |

### 2. Fonction `probeManualAddress(address, callback)`

Logique :
1. Met `isCheckingManual = true`, réinitialise `manualError = null`
2. Construit un `DiscoveredServer` stub depuis l'adresse
3. Appelle `discoveryRepository.pingServer(stub)`
4. Si injoignable → `manualError = address`, `isCheckingManual = false`, return
5. Sinon → appelle `discoveryRepository.isQuickConnectEnabled(server.address)`
6. Met `isCheckingManual = false`
7. Appelle `callback(server, qcAvailable)`

### 3. Timeout `noServerDetected`

Le timeout existant (8000ms dans `startDiscovery()`) met désormais aussi `noServerDetected = true` quand aucun serveur n'est trouvé. Le flag est réinitialisé à `false` au début de chaque scan.

## Build
BUILD SUCCESSFUL — `compileGithubDebugKotlin` OK
