# US3 — PinEntryScreen Compose

## Fichiers crees

| Fichier | Description |
|---------|-------------|
| `app/.../ui/startup/user/PinEntryScreen.kt` | Ecran PIN Compose (dialog + pave numerique) |

## Fichiers modifies

| Fichier | Description |
|---------|-------------|
| `app/.../ui/startup/user/UserSelectionScreen.kt` | Ajout `PinEntryState`, parametres PIN, affichage conditionnel |
| `app/.../ui/startup/user/UserSelectionFragment.kt` | Remplacement `PinEntryDialog.show()` par etat Compose `PinEntryState` |

## PinEntryScreen — Composable

### Parametres

```kotlin
fun PinEntryScreen(
    userName: String,
    error: String?,
    onPinEntered: (String) -> Unit,
    onCancel: () -> Unit,
    onForgotPin: () -> Unit,
)
```

### Structure visuelle

- **Dialog** Compose avec `usePlatformDefaultWidth = false`
- **Fond** : Box avec `VegafoXColors.Surface`, coins 20dp, largeur 360dp, padding 28dp
- **Titre** : "Entrer le PIN" — `TextPrimary` 22sp centre bold
- **Nom utilisateur** : `TextSecondary` 14sp centre
- **4 cercles indicateurs** 16dp : rempli `OrangePrimary` si chiffre saisi, contour `Divider` si vide
- **Pave numerique** 3 colonnes : touches 1-9, puis vide / 0 / effacer (⌫)
- **Chaque touche** : Box 72dp CircleShape, fond `SurfaceBright` normal / `OrangeSoft` focus, border `OrangePrimary` au focus
- **Chiffre** : `TextPrimary` 24sp bold
- **VegafoXButton Ghost** "Annuler" en bas
- **Text** "PIN oublie ?" `OrangePrimary` 13sp cliquable

### Comportement

- **State local** : `mutableStateListOf<Int>()` max 10 chiffres
- **Auto-submit** : quand longueur atteint 4, appelle `onPinEntered`
- **Shake on error** : si `error != null`, animation translateX (±12, ±8, ±4, 0) puis efface les chiffres
- **Auto-focus** : premiere touche (1) recoit le focus au lancement
- **D-pad** : `onKeyEvent` sur Enter/DirectionCenter + `clickable` pour touch

## Integration UserSelectionScreen

### PinEntryState data class

```kotlin
data class PinEntryState(
    val userName: String = "",
    val error: String? = null,
    val visible: Boolean = false,
)
```

### Nouveaux parametres UserSelectionScreen

- `pinEntryState: PinEntryState`
- `onPinEntered: (String) -> Unit`
- `onPinCancel: () -> Unit`
- `onPinForgot: () -> Unit`

### UserSelectionFragment

- Etat Compose : `pinEntryState`, `pinEntryUser`, `pinEntryServer`
- `verifyPin(pin)` : verifie le hash, authentifie ou met `error`
- `dismissPinEntry()` : remet l'etat a zero
- `PinEntryDialog.show()` supprime du fragment (remplace par etat Compose)

## Note sur PinEntryDialog.kt

`PinEntryDialog.kt` et `dialog_pin_entry.xml` ne sont **pas supprimes** car encore utilises par :
- `ServerFragment.kt` (ancien ecran Leanback)
- `SettingsAuthenticationPinCodeScreen.kt` (ecran settings PIN)

Migration a prevoir dans un ticket futur.

## Build

- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL
