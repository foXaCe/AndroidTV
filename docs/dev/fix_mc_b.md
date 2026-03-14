# fix_mc_b — ManualAddressDialog composable

## Fichiers modifiés
- `app/src/main/java/org/jellyfin/androidtv/ui/startup/server/ServerDiscoveryScreen.kt`
- `app/src/main/res/values/strings.xml`

## Changements

### 1. Nouveau composable `ManualAddressDialog` (privé)

Paramètres :
| Param | Type | Description |
|---|---|---|
| `initialAddress` | `String` | Adresse pré-remplie dans le champ |
| `isChecking` | `Boolean` | Probe en cours (désactive le bouton Continuer) |
| `error` | `String?` | Non-null = affiche le texte d'erreur sous le champ |
| `onConfirm` | `(String) -> Unit` | Appelé avec l'adresse saisie |
| `onDismiss` | `() -> Unit` | Ferme le dialog |

Composants internes :
- `var address` par `mutableStateOf(initialAddress)` — état local
- `AlertDialog` (Material3) avec titre `lbl_server_address`
- `OutlinedTextField` avec placeholder `server_address_hint`, `isError` lié à `error != null`, `supportingText` affichant `lbl_enter_server_address` en cas d'erreur
- Bouton Annuler (`TvSecondaryButton` + `btn_cancel`)
- Bouton Continuer (`TvPrimaryButton` + `btn_continue`) désactivé si `isChecking` ou `address` vide

Aucune logique réseau dans ce composable.

### 2. Nouvelles strings

| Clé | Valeur |
|---|---|
| `lbl_server_address` | `Server address` |
| `btn_continue` | `Continue` |

### 3. Nouveaux imports dans ServerDiscoveryScreen.kt
- `androidx.compose.material3.AlertDialog`
- `androidx.compose.material3.OutlinedTextField`
- `org.jellyfin.androidtv.ui.base.components.TvPrimaryButton`
- `org.jellyfin.androidtv.ui.base.components.TvSecondaryButton`

## Build
BUILD SUCCESSFUL — `compileGithubDebugKotlin` OK
