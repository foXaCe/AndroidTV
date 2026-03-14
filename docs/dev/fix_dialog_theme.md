# Fix Dialog Theme — ManualAddressDialog VegafoX

## Probleme

Le `ManualAddressDialog` dans `ServerDiscoveryScreen.kt` utilisait `AlertDialog`
Material3 avec les couleurs systeme par defaut (fond blanc, violet Material).
Incompatible avec le theme dark premium VegafoX.

## Solution

Remplacement de `AlertDialog` par `Dialog` Compose basique avec
`DialogProperties(usePlatformDefaultWidth = false)`.

### Structure du dialog

```
Dialog
  Box (background=Surface, RoundedCornerShape 16dp, padding 24dp)
    Column
      Text titre (TextPrimary, 20sp, Bold)
      Spacer 16dp
      OutlinedTextField (couleurs VegafoX)
      Spacer 20dp
      Row (Arrangement.End)
        TvSecondaryButton "Annuler"
        Spacer 12dp
        TvPrimaryButton "Continuer"
```

### OutlinedTextField colors

| Propriete | Valeur |
|-----------|--------|
| containerColor (focused/unfocused) | Transparent |
| focusedBorderColor | OrangePrimary |
| unfocusedBorderColor | Divider |
| focusedLabelColor | OrangePrimary |
| unfocusedLabelColor | TextSecondary |
| cursorColor | OrangePrimary |
| focusedTextColor | TextPrimary |
| unfocusedTextColor | TextPrimary |

### Autres couleurs

- Titre : `VegafoXColors.TextPrimary`
- Placeholder : `VegafoXColors.TextHint`
- Texte erreur : `VegafoXColors.Error`
- Fond dialog : `VegafoXColors.Surface`

## Fichier modifie

- `app/src/main/java/org/jellyfin/androidtv/ui/startup/server/ServerDiscoveryScreen.kt`

## Imports modifies

- Retire : `androidx.compose.material3.AlertDialog`
- Ajoute : `androidx.compose.material3.OutlinedTextFieldDefaults`, `androidx.compose.ui.window.Dialog`, `androidx.compose.ui.window.DialogProperties`

## Validation

- BUILD SUCCESSFUL : debug + release
- Installe sur Ugoos (192.168.1.152)
