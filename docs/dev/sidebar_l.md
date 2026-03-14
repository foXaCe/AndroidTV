# Sidebar L — Expansion par double-appui gauche

## Changement

La sidebar ne s'ouvre plus automatiquement quand le focus entre dedans.
Elle bascule ouvert/fermé uniquement sur **double-appui rapide** de la
touche gauche du D-pad (seuil 400ms).

### Ancien mécanisme (supprimé)

`LaunchedEffect(anyItemFocused)` élargissait la sidebar à 220dp dès
qu'un NavItem recevait le focus, et la rétractait à 72dp (après 200ms)
quand le focus quittait la sidebar.

### Nouveau mécanisme

**Double-tap D-pad Left** :
- Premier appui : timestamp enregistré dans `mutableLongStateOf(0L)`
- Deuxième appui < 400ms : `isExpanded = !isExpanded` (bascule), timestamp reset
- Le premier appui n'est PAS consommé (navigation D-pad normale)
- Le deuxième appui EST consommé (empêche navigation parasite)

**Fermeture** :
- Double-appui gauche (re-bascule)
- `Back` depuis la sidebar (consommé uniquement si sidebar étendue)
- `DirectionRight` depuis la sidebar (non consommé, focus passe au contenu)

**Focus** :
- Glow, scale, couleur active fonctionnent normalement (inchangé)
- Le shimmer continue d'être contrôlé par `anyItemFocused` (inchangé)

### Implémentation

`onPreviewKeyEvent` ajouté sur le Row racine de PremiumSideBar.
Intercepte les événements clavier avant les enfants (NavItems).

```kotlin
.onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown) {
        when (event.key) {
            Key.DirectionLeft -> double-tap detection
            Key.Back -> close if expanded
            Key.DirectionRight -> close if expanded, don't consume
            else -> false
        }
    } else false
}
```

## Fichier modifié

| Fichier | Modifications |
|---------|--------------|
| `sidebar/PremiumSideBar.kt` | Suppression LaunchedEffect expand/collapse + EXPAND_RETRACT_DELAY_MS ; ajout DOUBLE_TAP_THRESHOLD_MS=400, mutableLongStateOf, onPreviewKeyEvent avec double-tap/Back/Right ; +imports Key, KeyEventType, key, type, onPreviewKeyEvent, mutableLongStateOf |

## Build & Install

- `assembleDebug` : BUILD SUCCESSFUL
- `assembleRelease` : BUILD SUCCESSFUL
- Debug installé sur AM9 Pro (192.168.1.152)
- Release installé sur AM9 Pro (192.168.1.152)
