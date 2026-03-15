# UI Strings — Extraction & Traductions FR/EN

## Fichiers modifiés

| Fichier | Strings remplacées | Count |
|---------|-------------------|-------|
| `VegafoXTitleText.kt` | `"MEDIA CENTER"` → `stringResource(R.string.lbl_app_tagline)` | 1 |
| `WelcomeScreen.kt` | `"v${BuildConfig.VERSION_NAME}"` → `stringResource(R.string.lbl_version, BuildConfig.VERSION_NAME)` | 1 |
| `ServerDiscoveryScreen.kt` | `"mDNS · LAN"` → `stringResource(R.string.lbl_mdns_lan)` | 1 |
| `values/strings.xml` | Ajout 3 clés (lbl_app_tagline, lbl_version, lbl_mdns_lan) | 3 |
| `values-fr/strings.xml` | Ajout 3 traductions FR | 3 |

## Strings déjà extraites (avant cette tâche)

Les écrans startup utilisaient déjà `stringResource()` pour la majorité des textes :

| Clé | EN | FR |
|-----|----|----|
| `lbl_connect_to_server` | Connect to Server | Se connecter |
| `discovery_title` | Quick Connect | Connexion rapide |
| `discovery_subtitle` | Jellyfin servers detected on the network | Serveurs Jellyfin détectés sur le réseau |
| `discovery_scanning` | Searching… | Recherche en cours… |
| `discovery_servers_found` | %d server(s) found | %d serveur(s) trouvé(s) |
| `discovery_or` | or | ou |
| `discovery_manual_entry` | Enter address manually | Entrer l'adresse manuellement |
| `quickconnect_title` | Quick Connect | Connexion rapide |
| `quickconnect_connecting` | Connecting to %s… | Connexion à %s… |
| `quickconnect_instructions` | Open your Jellyfin server's web interface… | Ouvrez l'interface web de votre serveur… |
| `quickconnect_confirm` | I entered the code | J'ai entré le code |
| `quickconnect_waiting` | Waiting for confirmation… | En attente de confirmation… |
| `quickconnect_connected` | Connected! | Connecté ! |
| `quickconnect_back` | Back | Retour |
| `quickconnect_timeout` | Timed out (5 min) | Délai d'attente dépassé (5 min) |

## Clés ajoutées dans cette tâche

| Clé | EN | FR | Notes |
|-----|----|----|-------|
| `lbl_app_tagline` | MEDIA CENTER | MEDIA CENTER | Identique EN/FR (terme universel) |
| `lbl_version` | v%s | v%s | Format string, identique EN/FR |
| `lbl_mdns_lan` | mDNS · LAN | mDNS · LAN | Terme technique, identique EN/FR |

## Strings volontairement hardcodées

| String | Fichier | Raison |
|--------|---------|--------|
| `"Vega"` / `"foX"` | VegafoXTitleText.kt | Nom de marque, ne se traduit pas |
| `"VegafoX"` | ServerDiscoveryScreen.kt | Nom de marque (header) |
| `"J"` | ServerDiscoveryScreen.kt | Lettre icône serveur Jellyfin |
| `"✓"` (U+2713) | QuickConnectScreen.kt | Symbole unicode, pas du texte |

## Résultat des builds

| Flavor | Debug | Release |
|--------|-------|---------|
| GitHub | BUILD SUCCESSFUL | BUILD SUCCESSFUL |
| Playstore | BUILD SUCCESSFUL | BUILD SUCCESSFUL |
