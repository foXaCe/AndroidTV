# fix_mc_c — Intégration ManualAddressDialog dans ServerDiscoveryScreen

## Fichiers modifiés
- `app/src/main/java/org/jellyfin/androidtv/ui/startup/server/ServerDiscoveryScreen.kt`
- `app/src/main/res/values/strings.xml`

## Changements

### 1. State local `showManualDialog`

- `var showManualDialog by remember { mutableStateOf(false) }`
- Le bouton "Entrer manuellement" met `showManualDialog = true` au lieu d'appeler `onManualEntry`

### 2. Affichage conditionnel de `ManualAddressDialog`

Quand `showManualDialog == true` :
- `initialAddress` = `viewModel.lastKnownAddress.orEmpty()`
- `isChecking` = `uiState.isCheckingManual`
- `error` = `uiState.manualError`
- `onConfirm` → appelle `viewModel.probeManualAddress(address)` ; dans le callback, ferme le dialog et appelle `onServerSelected(server, qcAvailable)`
- `onDismiss` → `showManualDialog = false`

### 3. Mode `noServerDetected`

Quand `uiState.noServerDetected == true` :
- Texte `lbl_no_server_detected` affiché au-dessus du bouton
- `ManualEntryButton` reçoit `highlighted = true` :
  - Fond `OrangePrimary`
  - Bordure `OrangeBorder`
  - Texte couleur `Background` (contraste sur orange)

### 4. Modification de `ManualEntryButton`

Nouveau paramètre `highlighted: Boolean = false` qui contrôle :
- `backgroundColor` : `OrangePrimary` si highlighted, `OrangeSoft` si focused, `Transparent` sinon
- `textColor` : `Background` si highlighted, `TextSecondary` sinon
- `borderColor` : `OrangeBorder` si highlighted ou focused, `Divider` sinon

### 5. Nouvelle string

| Clé | Valeur |
|---|---|
| `lbl_no_server_detected` | `No server detected on your network` |

## Build & Install
- BUILD SUCCESSFUL — debug + release
- Installé sur AM9 Pro (192.168.1.152) — debug + release
- Test : saisir `192.168.1.60:8096` → doit ouvrir QuickConnect
