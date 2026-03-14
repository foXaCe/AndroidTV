# UI C4 — Navigation Startup (Discovery + QuickConnect)

## Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `ServerDiscoveryFragment.kt` | `ui.startup.server` | Fragment ComposeView wrappant ServerDiscoveryScreen, navigation vers QuickConnectFragment ou ServerAddFragment |
| `QuickConnectFragment.kt` | `ui.startup.server` | Fragment ComposeView wrappant QuickConnectScreen, args via Bundle, ajout serveur + navigation vers ServerFragment |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `WelcomeFragment.kt` | `onConnectClick` → remplace `SelectServerFragment` par `ServerDiscoveryFragment` (sans toolbar) |
| `Destinations.kt` | Ajout `serverDiscovery` et `quickConnect(server)` |

## Flow de navigation complet

```
StartupActivity.onCreate()
  └─ showSplash() → SplashFragment
  └─ onPermissionsGranted()
       ├─ session existante → showSplash() → MainActivity
       ├─ serveur connu → showServer(id) → ServerFragment + toolbar
       └─ aucun serveur → showServerSelection() → WelcomeFragment
            └─ clic "Connect to Server"
                 └─ ServerDiscoveryFragment (scan LAN, pas de toolbar)
                      ├─ serveur sélectionné + QuickConnect disponible
                      │    └─ QuickConnectFragment (code, polling, auth)
                      │         └─ authentifié → addServer → ServerFragment + toolbar
                      ├─ serveur sélectionné sans QuickConnect
                      │    └─ ServerAddFragment + toolbar (adresse pré-remplie)
                      └─ "Entrer manuellement"
                           └─ ServerAddFragment + toolbar (champ vide)
```

## Architecture des fragments

### ServerDiscoveryFragment

- Wrap `ServerDiscoveryScreen` dans `JellyfinTheme`
- Navigation directe via `parentFragmentManager.commit`
- Pas de toolbar (design full-screen Discovery)
- Back stack activé pour retour vers WelcomeFragment

### QuickConnectFragment

- Données serveur passées via Bundle (5 champs : id, name, address, version, pingMs)
- `buildArgs(server)` companion pour créer le Bundle
- Reconstruction `DiscoveredServer` depuis arguments via `lazy`
- Utilise `StartupViewModel.addServer()` pour enregistrer le serveur après auth
- Sur `ConnectedState` → navigation vers `ServerFragment` avec l'ID serveur
- Back stack activé pour retour vers ServerDiscoveryFragment

### Destinations (Destinations.kt)

Ajout de 2 entrées pour référence future :
- `Destinations.serverDiscovery` → `ServerDiscoveryFragment`
- `Destinations.quickConnect(server)` → `QuickConnectFragment` avec args

Note : le flow startup utilise `parentFragmentManager.commit` directement,
pas `NavigationRepository`. Ces destinations sont documentaires.

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- `./gradlew assembleGithubDebug` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
