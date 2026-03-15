# Fix: Pluriels (s) + Test découverte Jellyfin

## Partie 1 — Correction singulier/pluriel

### Occurrences trouvées

| String | Fichier(s) | Type de fix |
|--------|-----------|-------------|
| `discovery_servers_found` — "%d server(s) found" | values/strings.xml, values-fr/strings.xml | `<plurals>` resource |
| `lbl_play_trailers` — "Play trailer(s)" | values/*.xml (12 locales) | Retrait du "(s)" |
| `jellyseerr_cancel_partial` — "%1$d annulée(s), %2$d échouée(s)" | values/strings.xml, values-fr/strings.xml | `<plurals>` dual-count |

### Plurals ajoutés

#### `discovery_servers_found` (EN + FR)

```xml
<!-- values/strings.xml -->
<plurals name="discovery_servers_found">
    <item quantity="one">%d server found</item>
    <item quantity="other">%d servers found</item>
</plurals>

<!-- values-fr/strings.xml -->
<plurals name="discovery_servers_found">
    <item quantity="one">%d serveur trouvé</item>
    <item quantity="other">%d serveurs trouvés</item>
</plurals>
```

**Code Kotlin** (`ServerDiscoveryScreen.kt:166`) :
```kotlin
// Avant
stringResource(R.string.discovery_servers_found, serverCount)
// Après
pluralStringResource(R.plurals.discovery_servers_found, serverCount, serverCount)
```

#### `jellyseerr_cancelled_count` + `jellyseerr_failed_count` (EN + FR)

Remplacement de `jellyseerr_cancel_partial` (string avec dual-count impossible en plurals Android)
par deux plurals séparés combinés dans le code :

```xml
<!-- values/strings.xml -->
<plurals name="jellyseerr_cancelled_count">
    <item quantity="one">%d cancelled</item>
    <item quantity="other">%d cancelled</item>
</plurals>
<plurals name="jellyseerr_failed_count">
    <item quantity="one">%d failed</item>
    <item quantity="other">%d failed</item>
</plurals>

<!-- values-fr/strings.xml -->
<plurals name="jellyseerr_cancelled_count">
    <item quantity="one">%d annulée</item>
    <item quantity="other">%d annulées</item>
</plurals>
<plurals name="jellyseerr_failed_count">
    <item quantity="one">%d échouée</item>
    <item quantity="other">%d échouées</item>
</plurals>
```

**Code Kotlin** (`MediaDetailsFragment.kt:507`) :
```kotlin
// Avant
getString(R.string.jellyseerr_cancel_partial, successCount, failCount)
// Après
val cancelled = resources.getQuantityString(R.plurals.jellyseerr_cancelled_count, successCount, successCount)
val failed = resources.getQuantityString(R.plurals.jellyseerr_failed_count, failCount, failCount)
"$cancelled, $failed"
```

#### `lbl_play_trailers` — retrait simple

12 locales corrigées (label de chooser, pas de count) :
- values/ (EN), values-en-rGB/, values-fr/ (déjà OK)
- values-de/, values-nl/, values-sv/
- values-es/, values-es-rMX/, values-b+es+419/
- values-pt-rBR/, values-pt-rPT/, values-vi/

### Commentaire code non modifié

`JellyseerrApiModels.kt:124` — commentaire `"permission(s)"` dans le code, pas user-facing → ignoré.

---

## Partie 2 — Test découverte Jellyfin sur AM9 Pro (Ugoos)

### Builds

| Flavor | Debug | Release |
|--------|-------|---------|
| GitHub | BUILD SUCCESSFUL | BUILD SUCCESSFUL |

### Installation

```
adb install -r vegafox-androidtv-v1.6.2-github-debug.apk   → Success
adb install -r vegafox-androidtv-v1.6.2-github-release.apk → Success
```

### Logs ADB complets

**Étape 1 — App data effacée + lancement**

```
pm clear com.vegafox.androidtv.debug → Success
am start StartupActivity → Welcome screen affiché
```

**Étape 2 — Navigation vers ServerDiscoveryScreen (DPAD_CENTER)**

```
12:11:18.531 D ServerDiscoveryRepositoryImpl$discoverServers: Discovery: starting UDP broadcast scan
12:11:18.532 I LocalServerDiscovery: Starting discovery with timeout of 500ms
12:11:18.534 D LocalServerDiscovery: Discovering via /255.255.255.255
12:11:18.534 D LocalServerDiscovery: Finished sending broadcast, listening for responses
12:11:18.534 D LocalServerDiscovery: Reading reply...
12:11:19.036 D LocalServerDiscovery: Reading reply...
[...16 tentatives de lecture, ~500ms chacune...]
12:11:26.555 D LocalServerDiscovery: End
```

### Résultat

| Question | Réponse |
|----------|---------|
| UDP broadcast émis ? | **OUI** — envoyé à /255.255.255.255 |
| Phase 1.5 (serveurs connus) déclenchée ? | **NON** — données effacées, authStore vide |
| JellyMox 192.168.1.60:8096 trouvé ? | **NON** |
| Ping réseau OK ? | **OUI** — 0.838ms depuis AM9 Pro |
| curl /System/Info/Public depuis host ? | **OUI** — `{"ServerName":"JellyMox","Version":"10.11.6"}` |

### Cause : JellyMox ne répond pas au broadcast UDP

JellyMox tourne dans un **conteneur Docker** sur Proxmox. Le broadcast UDP (port 7359) ne
traverse pas le bridge network Docker par défaut. C'est un comportement **connu et attendu**
pour les installations Jellyfin en conteneur.

**Ce n'est pas un bug de l'app.** La découverte UDP fonctionne correctement (broadcast envoyé,
écoute active pendant 8s). Le serveur ne répond simplement pas car le paquet UDP ne lui parvient pas.

### Workarounds possibles (côté serveur)

1. `--network=host` sur le conteneur Docker
2. Forwarding explicite du port UDP 7359 dans le docker-compose
3. Utiliser la saisie manuelle d'adresse dans l'app (fonctionne correctement)

---

## Partie 3 — Tests unitaires

### Fichier : `app/src/test/kotlin/ui/startup/server/ServerDiscoveryTest.kt`

8 tests couvrant :
- `probeKnownServers` retourne un serveur quand l'endpoint répond
- `probeKnownServers` n'émet rien quand le serveur est injoignable
- `probeKnownServers` probe plusieurs serveurs en parallèle
- `DiscoveredServer.isReachable` vrai quand `pingMs >= 0`
- `DiscoveredServer.isReachable` faux quand `pingMs < 0`
- `DiscoveredServer.host` et `.port` parsing (http, https, port absent)

**Note :** Les tests utilisent MockK au lieu d'un vrai HttpServer car
`org.json.JSONObject` (API Android) n'est pas disponible dans les tests JVM unitaires
(les stubs Android retournent null). Pour des tests d'intégration avec HttpServer, il
faudrait ajouter `testImplementation("org.json:json:20240303")`.

### Résultat

```
8 tests completed, 0 failed
BUILD SUCCESSFUL
```
