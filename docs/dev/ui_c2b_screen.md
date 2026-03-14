# UI C2b — ServerDiscoveryScreen

## Fichier cree

| Fichier | Package | Contenu |
|---------|---------|---------|
| `ServerDiscoveryScreen.kt` | `ui.startup.server` | Composable principal + composants internes |

## Composables

### ServerDiscoveryScreen (public)

Parametres :
- `onServerSelected: (DiscoveredServer, Boolean) -> Unit` — callback serveur selectionne + quickConnect
- `onManualEntry: () -> Unit` — callback saisie manuelle
- `viewModel: ServerDiscoveryViewModel` — injecte via `koinViewModel()`

Observe `uiState` via `collectAsState()`. Box plein ecran fond `VegafoXColors.Background`, colonne centree 480dp.

### Composants internes (private)

| Composable | Description |
|------------|-------------|
| `Header` | 3 textes centres : "VegafoX" 13sp OrangePrimary, "Connexion rapide" 28sp bold, descriptif 14sp TextSecondary |
| `ScanStatusBar` | Row space-between. Point anime orange (800ms pulse) si scanning, point vert fixe sinon. Texte statut + badge "mDNS · LAN" monospace |
| `PulsingDot` | Point avec `rememberInfiniteTransition` alpha 1→0.2 reverse 800ms |
| `ServerList` | 2 skeletons shimmer si scanning+vide, sinon Column gap 8dp de ServerCard |
| `SkeletonCard` | Box 76dp, RoundedCornerShape 14dp, Brush.linearGradient anime (shimmer) |
| `ServerCard` | Card focusable : scale 1→1.02f, fond OrangeSoft/Surface, border OrangeBorder/Divider, icon "J" 40dp, nom+host:port, PingIndicator+version |
| `PingIndicator` | Point 6dp colore selon pingMs (<10=Success, <50=Warning, >=50=Error, <0=TextHint) + texte monospace |
| `OrDivider` | Ligne — "ou" — Ligne |
| `ManualEntryButton` | Box focusable, border 1dp, RoundedCornerShape 12dp, texte 14sp centre |

### Navigation

`onServerClick` → `viewModel.selectServer(server)` → `onServerSelected(server, uiState.quickConnectAvailable)`

## Tokens utilises

Tous proviennent de `VegafoXColors` :
- Fonds : `Background`, `Surface`, `SurfaceBright`, `OrangeSoft`
- Orange : `OrangePrimary`, `OrangeBorder`
- Texte : `TextPrimary`, `TextSecondary`, `TextHint`
- Etats : `Success`, `Error`
- Bordure : `Divider`

Note : le spec mentionne `TextTertiary` et `SurfaceVariant` qui n'existent pas dans VegafoXColors — mappes respectivement sur `TextHint` et `SurfaceBright`.

## Dependances

- `ServerDiscoveryViewModel` (C2a) — `uiState`, `selectServer()`
- `DiscoveredServer` (C1) — modele de donnees
- `VegafoXColors` (W1) — palette Dark Premium
- `koin-androidx-compose` — `koinViewModel()`

## Build

- `./gradlew :app:compileGithubDebugKotlin` → **BUILD SUCCESSFUL**
- 0 erreur, 0 warning
