# fix_title_colors — Titre VegafoX tricolore (bleu · blanc · orange)

## Schéma de couleurs
| Segment | Couleur | Valeur |
|---|---|---|
| `Vega` | `BlueAccent` | `#4FC3F7` (bleu clair lumineux) |
| `fo` | `TextPrimary` | `#F5F0EB` (blanc cassé) |
| `X` | `OrangePrimary` | `#FF6B00` (orange) |

## Fichiers modifiés

### 1. `VegafoXColors.kt`
- Ajout `val BlueAccent = Color(0xFF4FC3F7)` dans la section avant les couleurs texte

### 2. `VegafoXTitleText.kt`
- `buildAnnotatedString` modifié : 3 segments au lieu de 2
- Avant : "Vega" blanc + "foX" orange
- Après : "Vega" bleu + "fo" blanc + "X" orange
- Utilisé par `WelcomeScreen` (fontSize 52sp)

### 3. `ServerDiscoveryScreen.kt` — `Header()`
- Remplacé `text = "VegafoX"` (texte plat orange) par un `buildAnnotatedString` tricolore identique
- Ajout imports : `SpanStyle`, `buildAnnotatedString`, `withStyle`
- Style conservé : 13sp, letterSpacing 3sp

### QuickConnectScreen
- Non modifié : le header affiche le nom du serveur (`uiState.server.name`), pas "VegafoX"

## Build
BUILD SUCCESSFUL — debug + release
