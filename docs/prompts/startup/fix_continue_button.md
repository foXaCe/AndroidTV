# fix_continue_button — Bouton Continuer dans ManualAddressDialog

## Problème
Le bouton **Continuer** dans le `ManualAddressDialog` de `ServerDiscoveryScreen`
ne faisait rien quand on appuyait dessus avec une adresse comme `192.168.1.60`.

## Cause identifiée : Cause A — Pas de normalisation d'adresse

`probeManualAddress()` utilisait l'adresse brute saisie par l'utilisateur
(ex: `192.168.1.60`) sans ajouter le schéma `http://` ni le port `:8096`.

Le `pingServer()` appelait `jellyfin.createApi(baseUrl = "192.168.1.60")`
qui échouait silencieusement → `pingMs = -1` → `isReachable = false`
→ `manualError` était set, le dialog restait ouvert avec l'erreur.

La fonction `checkManualAddress()` (ancien code) contenait déjà la normalisation
correcte, mais `probeManualAddress()` (nouveau code) ne l'avait pas.

## Fix appliqué

### Fichier : `ServerDiscoveryViewModel.kt`
- Ajout de la normalisation dans `probeManualAddress()` :
  - Si l'adresse ne commence pas par `http://` ou `https://` → préfixe `http://`
  - Si l'adresse ne contient pas de `:` après le schéma → suffixe `:8096`
- Ajout de logs Timber `VFX_MANUAL` pour tracer l'exécution

### Fichier : `ServerDiscoveryScreen.kt`
- Ajout de logs Timber `VFX_MANUAL` dans le `onConfirm` du dialog et le callback

## Logs ADB confirmant le fix

```
VFX_MANUAL onConfirm appelé avec adresse : 192.168.1.61:8096
VFX_MANUAL probeManualAddress démarré : 192.168.1.61:8096
VFX_MANUAL adresse normalisée : http://192.168.1.61:8096
Ping: http://192.168.1.61:8096 → 101ms
QuickConnect enabled: true for http://192.168.1.61:8096
VFX_MANUAL probe terminé qcAvailable=true
VFX_MANUAL callback reçu server=http://192.168.1.61:8096 qcAvailable=true
```

Toute la chaîne s'exécute correctement : normalisation → ping → QuickConnect → navigation.

## Build & Install
- BUILD SUCCESSFUL — debug + release
- Installé sur AM9 Pro (192.168.1.152) — debug + release
- Test 1 : saisir `192.168.1.61:8096` (sans http) → normalisé en `http://192.168.1.61:8096` → OK, QuickConnect
- Test 2 : saisir adresse sans http ni port → normalisation OK, navigation OK
- Confirmé fonctionnel par l'utilisateur
