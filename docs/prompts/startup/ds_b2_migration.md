# DS Button Migration — Startup Screens → VegafoXButton

## Boutons migres

| Ecran | Ancien composable | VegafoXButton variant | Icon | Modifier |
|-------|-------------------|-----------------------|------|----------|
| WelcomeScreen | `ConnectServerButton` | **Primary** | `ArrowForward` (end) | autoFocus=true |
| ServerDiscoveryScreen | `ManualEntryButton` (normal) | **Secondary** | — | fillMaxWidth |
| ServerDiscoveryScreen | `ManualEntryButton` (highlighted) | **Outlined** | — | fillMaxWidth |
| ServerDiscoveryScreen | `DialogButton` (Cancel) | **Ghost** | — | weight(1f) |
| ServerDiscoveryScreen | `DialogButton` (Confirm) | **Primary** | — | weight(1f) |
| QuickConnectScreen | `ConfirmButton` | **Primary** | — | fillMaxWidth |
| QuickConnectScreen | `WaitingButton` | **Secondary** disabled | — | fillMaxWidth |
| QuickConnectScreen | Back link (Text+clickable) | **Ghost** | — | — |

## Fichiers supprimes

| Fichier | Raison |
|---------|--------|
| `ui/startup/compose/ConnectServerButton.kt` | Remplace par VegafoXButton Primary |

## Composables inline supprimes

| Fichier | Composable | Raison |
|---------|------------|--------|
| `ServerDiscoveryScreen.kt` | `ManualEntryButton()` | Remplace par VegafoXButton |
| `ServerDiscoveryScreen.kt` | `DialogButton()` | Remplace par VegafoXButton |
| `QuickConnectScreen.kt` | `ConfirmButton()` | Remplace par VegafoXButton |
| `QuickConnectScreen.kt` | `WaitingButton()` | Remplace par VegafoXButton |
| `QuickConnectScreen.kt` | `SmallSpinner()` | Plus utilise |

## Imports nettoyes

| Fichier | Imports retires |
|---------|-----------------|
| `ServerDiscoveryScreen.kt` | `drawBehind`, `CornerRadius`, `TextAlign`, `EaseOutCubic` |
| `QuickConnectScreen.kt` | `animateFloatAsState`, `clickable`, `focusable`, `onFocusChanged`, `EaseOutCubic` |

## Build

- `assembleGithubDebug` → BUILD SUCCESSFUL
- `assembleGithubRelease` → BUILD SUCCESSFUL
- Debug APK installe sur AM9 Pro → Success
- Release APK installe sur AM9 Pro → Success

## Test focus D-pad

A valider manuellement sur AM9 Pro :

- [ ] WelcomeScreen : bouton "Se connecter" — focus visible (scale + glow + border orange), Enter declenche navigation
- [ ] ServerDiscoveryScreen : bouton "Entree manuelle" — variant Secondary normal, Outlined quand no server
- [ ] ServerDiscoveryScreen : dialog Cancel (Ghost) / Continuer (Primary) — focus + Enter OK
- [ ] QuickConnectScreen : bouton "J'ai entre le code" (Primary) — focus + Enter OK
- [ ] QuickConnectScreen : bouton "En attente" (Secondary disabled) — 40% opacite, non focusable
- [ ] QuickConnectScreen : bouton "Retour" (Ghost) — focus + Enter OK
- [ ] Pas de double navigation sur aucun bouton (guard 400ms)
