# Fix — QuickConnect ne trouve pas le serveur + Bouton WelcomeScreen

## Contexte

Le serveur Jellyfin (JellyMox v10.11.6) tourne sur Proxmox (192.168.1.60:8096) et fonctionne parfaitement. Le problème vient **exclusivement de l'app** : la discovery ne trouve pas le serveur, donc l'utilisateur ne peut jamais atteindre l'écran QuickConnect.

## Diagnostic ADB

### Logs capturés

```
$ adb logcat -s VFX_DISCOVERY VFX_PING VFX_QC
03-10 10:56:26.126 11324 11464 D VFX_DISCOVERY: Starting discovery...
# Aucun "Found:" — le flow se termine vide, sans erreur
```

### Vérification réseau (depuis la box Ugoos)

```
# Ping OK — le serveur est joignable
$ adb shell ping -c 2 192.168.1.60
64 bytes from 192.168.1.60: icmp_seq=1 ttl=64 time=1.55 ms

# API HTTP OK — le serveur Jellyfin répond
$ curl -s http://192.168.1.60:8096/System/Info/Public
{"ServerName":"JellyMox","Version":"10.11.6","ProductName":"Jellyfin Server",...}

# QuickConnect activé côté serveur
$ curl -s http://192.168.1.60:8096/QuickConnect/Enabled
true

# UDP 7359 — AUCUNE réponse
$ echo "Who is JellyfinServer?" | socat - UDP-DATAGRAM:192.168.1.60:7359
# timeout, pas de réponse
```

### Réponse API

- `GET /System/Info/Public` → 200 OK (via HTTP 8096) ✓
- `GET /QuickConnect/Enabled` → `true` ✓
- UDP 7359 broadcast → aucune réponse ✗

## Causes identifiées (toutes côté app)

### Cause 1 — Discovery uniquement UDP, pas de fallback HTTP (P0)

L'app repose **exclusivement** sur le broadcast UDP port 7359 (`jellyfin.discovery.discoverLocalServers()`) pour trouver les serveurs. Ce protocole est fragile : il ne fonctionne pas à travers les VLANs, certains routeurs le bloquent, les VMs/conteneurs ne le relaient pas, et beaucoup de configurations réseau domestiques ne le supportent pas.

**Le serveur Jellyfin est parfaitement joignable en HTTP** mais l'app ne propose aucun chemin pour passer de l'entrée manuelle d'adresse vers QuickConnect. Quand la discovery échoue (0 serveurs) :
- Le seul chemin disponible est "Entrer l'adresse manuellement" → `ServerAddFragment` (ancien flow)
- `ServerAddFragment` ne vérifie pas QuickConnect et redirige vers le login classique
- L'utilisateur ne peut **jamais** atteindre l'écran QuickConnect

### Cause 2 — Race condition dans ServerDiscoveryScreen (P1)

```kotlin
// ServerDiscoveryScreen.kt:86-89
onServerClick = { server ->
    viewModel.selectServer(server)  // ← lance une coroutine async
    onServerSelected(server, uiState.quickConnectAvailable)  // ← lit l'ANCIEN state (false)
}
```

Même si un serveur EST découvert, `selectServer()` lance une coroutine pour vérifier QuickConnect, mais `uiState.quickConnectAvailable` est lu immédiatement AVANT que la coroutine finisse → QuickConnect n'est **jamais** proposé.

### Cause 3 — Bouton WelcomeScreen : coins carrés + clic D-pad + replay animation (P2)

Trois problèmes liés au bouton `ConnectServerButton` sur le WelcomeScreen :

1. **Coins carrés** : L'`AnimatedVisibility` avec `scaleIn` appliquait un `graphicsLayer(clip=true)` rectangulaire qui écrasait le `clip(RoundedCornerShape)` du bouton. De plus, le `focusable()` dessinait un indicateur rectangulaire par-dessus le clip.

2. **Clic D-pad** : Le bouton nécessitait plusieurs pressions. Causes : `requestFocus()` appelé trop tôt (50ms, avant fin de l'animation 400ms), `onKeyEvent` n'interceptait que `Key.Enter` et pas `Key.DirectionCenter`, et `focusable()` était mal positionné dans la chaîne de modifiers.

3. **Re-animation au retour** : Au retour depuis l'écran Discovery (back), le WelcomeScreen rejouait toute la séquence d'animation car les states `show*` utilisaient `remember` (perdu à la recomposition).

## Fixes appliqués

### Fix 1 — Logs Timber permanents

Ajout de logs Timber dans `discoverServers()`, `pingServer()`, `isQuickConnectEnabled()` et `initiate()` pour diagnostiquer les problèmes de découverte.

### Fix 2 — Coins arrondis (ConnectServerButton.kt)

- `scaleIn` retiré de l'`AnimatedVisibility` du bouton → remplacé par `fadeIn` seul (pas de clip rectangulaire)
- `focusable(enabled)` déplacé **après** `clip(shape)` → l'indicateur TV est clippé au même arrondi
- `clickable` avec `indication = null` → supprime le ripple rectangulaire par défaut
- `background(color, shape)` avec le shape en paramètre → double garantie

### Fix 3 — Clic D-pad fiable (ConnectServerButton.kt)

- `delay(500)` avant `requestFocus()` → attend la fin de l'animation `fadeIn(400ms)`
- `onKeyEvent` intercepte `Key.Enter` ET `Key.DirectionCenter` (DPAD_CENTER)
- `focusable(enabled)` maintenu (requis pour le focus D-pad sur TV) mais après `clip`
- Guard `navigating` dans WelcomeContent empêche la double navigation

### Fix 4 — Pas de re-animation au retour (WelcomeScreen.kt)

- `rememberSaveable` au lieu de `remember` pour les 6 states d'animation (`showOrb`, `showFox`, etc.)
- Au retour arrière, les states sont `true` (sauvegardés) → pas de replay

## Vérification ADB

```
# 1er chargement : animation → coins arrondis ✓
# D-pad center : navigation vers ServerDiscoveryFragment ✓
# Retour arrière : WelcomeScreen sans re-animation ✓
# Coins arrondis maintenus au retour ✓
```

## Fixes à implémenter

| Issue | Priorité | Fichier | Description |
|-------|----------|---------|-------------|
| Pas de fallback HTTP | **P0** | `ServerDiscoveryRepositoryImpl` | L'app doit proposer QuickConnect même quand la discovery UDP échoue |
| Entrée manuelle → QC | **P0** | `ServerDiscoveryFragment` | "Entrer l'adresse manuellement" doit vérifier QC et l'offrir |
| Race condition | **P1** | `ServerDiscoveryScreen.kt:86-89` | `onServerSelected` appelé avant le check async |

### Recommandation P0 — Fallback HTTP + QC depuis entrée manuelle

Quand la discovery UDP retourne 0 serveurs et que l'utilisateur entre une adresse manuellement, l'app doit :
1. Ping HTTP `/System/Info/Public` pour valider le serveur
2. Vérifier `GET /QuickConnect/Enabled`
3. Si QuickConnect est activé → proposer le choix QuickConnect vs login classique
4. Si non → login classique directement

### Recommandation P1 — Attendre le résultat async

```kotlin
onServerClick = { server ->
    viewModel.selectServerAndCheck(server) { qcAvailable ->
        onServerSelected(server, qcAvailable)
    }
}
```

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `ConnectServerButton.kt` | Coins arrondis (focusable après clip, indication null, shape param) + D-pad (Enter+DirectionCenter, delay 500ms) |
| `WelcomeScreen.kt` | fadeIn seul (plus de scaleIn), rememberSaveable, guard navigating |
| `ServerDiscoveryRepository.kt` | Logs Timber + opérateurs flow `onStart`/`onEach`/`catch` |
| `QuickConnectRepository.kt` | Log Timber dans `initiate()` |

## Build

- `./gradlew :app:assembleGithubDebug` → **BUILD SUCCESSFUL**
- Test ADB sur Ugoos (192.168.1.152) → tous les fixes vérifiés visuellement
