# Fix Dialog Buttons — Centrage texte ManualAddressDialog

## Probleme

Les boutons "Annuler" et "Continuer" du ManualAddressDialog avaient un texte
mal centre. Le Row utilisait `Arrangement.End` sans `weight`, ce qui donnait
des boutons de largeurs inegales.

## Solution

### Avant

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
) {
    TvSecondaryButton(text = "Annuler", onClick = onDismiss)
    Spacer(Modifier.width(12.dp))
    TvPrimaryButton(text = "Continuer", onClick = { onConfirm(address) }, ...)
}
```

### Apres

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
) {
    TvSecondaryButton(
        text = "Annuler",
        onClick = onDismiss,
        modifier = Modifier.weight(1f),
    )
    TvPrimaryButton(
        text = "Continuer",
        onClick = { onConfirm(address) },
        modifier = Modifier.weight(1f),
        ...
    )
}
```

## Pourquoi ca fonctionne

- `weight(1f)` sur chaque bouton = largeurs egales (50/50)
- `Arrangement.spacedBy(12.dp)` = espacement uniforme, pas de Spacer manuel
- Le `ButtonRow` interne de `Button.kt` utilise `Arrangement.Center` +
  `Alignment.CenterVertically` → le texte est centre dans chaque bouton

## Fichier modifie

- `app/src/main/java/org/jellyfin/androidtv/ui/startup/server/ServerDiscoveryScreen.kt`

## Validation

- BUILD SUCCESSFUL : debug + release
