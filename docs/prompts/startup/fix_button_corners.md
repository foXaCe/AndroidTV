# Fix — Coins carrés sur ConnectServerButton

## Problème

Les coins du bouton `ConnectServerButton` apparaissent carrés pendant les premières frames, puis deviennent arrondis. Le bug se reproduit aussi au retour arrière depuis QuickConnectScreen (recomposition du WelcomeScreen).

## Causes identifiées

### 1. `graphicsLayer` entre focus modifiers et `clip`

Dans ConnectServerButton.kt, `graphicsLayer` (pour le scale au focus) était placé juste avant `clip(RoundedCornerShape(14.dp))`, après les focus modifiers. La création du render layer par `graphicsLayer` pouvait retarder l'application du `clip` d'un frame, provoquant un flash de coins carrés.

### 2. `slideInVertically` dans AnimatedVisibility (WelcomeScreen)

Le bouton était wrappé dans `AnimatedVisibility` avec `slideInVertically + fadeIn`. L'animation `slideInVertically` applique un **clip rectangulaire** sur le wrapper pendant la durée du slide — les coins arrondis du bouton sont coupés en carré par ce clip externe.

## Corrections

### ConnectServerButton.kt

**`graphicsLayer` déplacé en premier** dans la chaîne Modifier (avant les focus modifiers), et `defaultMinSize` déplacé avant `clip` pour que le sizing soit résolu avant le clipping :

```kotlin
Box(
    modifier = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }  // ← premier
        .focusRequester(focusRequester)
        .onFocusChanged { isFocused = it.isFocused }
        .focusable(enabled)
        .defaultMinSize(minWidth = 280.dp, minHeight = 56.dp)
        .clip(RoundedCornerShape(14.dp))    // ← avant background
        .background(...)                     // ← après clip
        .clickable(...)                      // ← après background
        .drawBehind { ... },
)
```

L'ordre `clip → background → clickable` est garanti propre, sans aucun modifier intermédiaire qui pourrait interférer.

### WelcomeScreen.kt

**`slideInVertically` remplacé par `scaleIn + fadeIn`** pour l'AnimatedVisibility du bouton :

```kotlin
// Avant
enter = slideInVertically(tween(400)) { it / 2 }
    + fadeIn(tween(400))

// Après
enter = scaleIn(
    initialScale = 0.95f,
    animationSpec = tween(400),
) + fadeIn(tween(400))
```

`scaleIn` n'applique pas de clip rectangulaire — il transforme via `graphicsLayer` sans couper les bords. Les coins arrondis sont préservés pendant toute l'animation.

### Cas disabled

Le même modifier chain s'applique dans les deux cas (`enabled = true/false`). Le `clip(RoundedCornerShape(14.dp))` est toujours avant `background()`, quel que soit la couleur (OrangePrimary ou Surface).

### Retour arrière depuis QuickConnectScreen

Le bug se reproduisait au retour car WelcomeScreen recompose et rejoue l'AnimatedVisibility. Avec `scaleIn` au lieu de `slideInVertically`, le problème est corrigé dans les deux directions de navigation.

## Fichiers modifiés

- `app/src/main/java/org/jellyfin/androidtv/ui/startup/compose/ConnectServerButton.kt`
- `app/src/main/java/org/jellyfin/androidtv/ui/startup/compose/WelcomeScreen.kt`

## Build

```
./gradlew :app:compileGithubDebugKotlin → BUILD SUCCESSFUL
0 erreur, 0 warning
```
