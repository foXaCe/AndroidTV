# Fix Missing Translations — ServerDiscovery & QuickConnect

## Audit

Grep de tous les `stringResource` / `pluralStringResource` dans :
- `ServerDiscoveryScreen.kt` (13 clés string + 1 plurals)
- `QuickConnectScreen.kt` (8 clés string)

## Résultat

Sur 21 clés utilisées, 18 avaient déjà une traduction FR.

### 3 traductions manquantes ajoutées

| Clé | EN | FR |
|-----|----|----|
| `btn_continue` | Continue | Continuer |
| `lbl_server_address` | Server address | Adresse du serveur |
| `lbl_no_server_detected` | No server detected on your network | Aucun serveur détecté sur votre réseau |

## Fichier modifié

- `app/src/main/res/values-fr/strings.xml`

## Corrections annexes (VegafoX brand bicolore)

Le texte "VegafoX" était en full orange sur 3 écrans. Corrigé en bicolore
"Vega" bleu + "foX" orange :

| Fichier | Avant | Après |
|---------|-------|-------|
| `VegafoXTitleText.kt` | Vega=bleu, fo=blanc, X=orange | Vega=bleu, foX=orange |
| `ServerDiscoveryScreen.kt` | Vega=bleu, fo=blanc, X=orange | Vega=bleu, foX=orange |
| `QuickConnectScreen.kt` | server.name en full orange | Brand bicolore Vega=bleu, foX=orange |

## Validation

- BUILD SUCCESSFUL : debug + release
