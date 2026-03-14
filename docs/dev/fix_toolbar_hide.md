# Fix: Hide startup toolbar on UserSelectionScreen

## Probleme

`StartupActivity.showServer()` ajoute `StartupToolbarFragment` dans `toolbar_view` en meme temps que `UserSelectionFragment`. Cette toolbar (horloge, bouton parametres, bouton aide) se superpose au header propre de `UserSelectionScreen` (logo VegafoX + titre + nom du serveur).

## Solution

Cacher le conteneur `toolbar_view` quand `UserSelectionFragment` est affiche, le restaurer quand on quitte ce fragment.

## Fichier modifie

| Fichier | Changement |
|---------|-----------|
| `app/.../ui/startup/user/UserSelectionFragment.kt` | `onResume()` : `toolbar_view.visibility = GONE` ; ajout `onPause()` : `toolbar_view.visibility = VISIBLE` |

### Detail

- `onResume()` : apres `clearBackgrounds()`, cache `toolbar_view` via `activity?.findViewById<View>(R.id.toolbar_view)?.visibility = View.GONE`
- `onPause()` : restaure `toolbar_view` a `View.VISIBLE` pour que les autres ecrans startup (SelectServer, ServerAdd, etc.) conservent leur toolbar
- Import `android.view.View` ajoute

## Pourquoi pas supprimer la toolbar globalement

`StartupToolbarFragment` est utilisee par `SelectServerFragment`, `ServerAddFragment`, `ServerDiscoveryFragment`, `QuickConnectFragment` et `ServerFragment`. Seul `UserSelectionScreen` a son propre header et n'a pas besoin de la toolbar.

## Build

- `compileGithubDebugKotlin` → BUILD SUCCESSFUL
- `compileGithubReleaseKotlin` → BUILD SUCCESSFUL
