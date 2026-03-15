# Fix — Bouton "Se connecter" nécessite plusieurs clics

## Problème
Le bouton `ConnectServerButton` sur le `WelcomeScreen` nécessite parfois plusieurs clics D-pad pour déclencher la navigation.

## Causes identifiées

1. **requestFocus trop tôt** — Le `LaunchedEffect` appelait `requestFocus()` avant que le layout soit attaché
2. **Double navigation** — Pas de guard contre les clics rapides, la navigation pouvait être déclenchée plusieurs fois
3. **Touche Enter D-pad** — Seul `clickable` était utilisé, pas d'interception explicite de `Key.Enter`

## Corrections appliquées

### FIX 1 — requestFocus avec delay (ConnectServerButton.kt)

```kotlin
if (autoFocus) {
    LaunchedEffect(Unit) {
        delay(50)
        focusRequester.requestFocus()
    }
}
```

Ajout de `delay(50)` pour laisser le temps au layout d'être attaché avant de demander le focus.

### FIX 2 — Guard anti-double navigation (WelcomeScreen.kt)

```kotlin
var navigating by remember { mutableStateOf(false) }

ConnectServerButton(
    onClick = {
        if (!navigating) {
            navigating = true
            onConnectClick()
        }
    },
)
```

State `navigating` dans `WelcomeContent` — se reset naturellement quand le fragment est recréé (retour arrière via back stack).

### FIX 3 — Interception Key.Enter D-pad (ConnectServerButton.kt)

```kotlin
.onKeyEvent { event ->
    if (enabled && event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
        onClick()
        true
    } else {
        false
    }
}
.clickable(enabled = enabled, onClick = onClick)
```

`onKeyEvent` placé avant `clickable` dans la chaîne de modifiers pour intercepter `Key.Enter` / `KeyUp`. Le `clickable` existant est conservé pour le touch.

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ConnectServerButton.kt` | FIX 1 (delay 50ms) + FIX 3 (onKeyEvent Enter) |
| `WelcomeScreen.kt` | FIX 2 (guard navigating dans WelcomeContent) |

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- 0 erreur, 0 warning
