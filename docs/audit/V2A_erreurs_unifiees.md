# V2A — Gestion d'erreur unifiée dans les UiState

**Date** : 2026-03-08
**Statut** : ✅ Terminé — BUILD SUCCESSFUL (assembleRelease)
**Source** : Audit 05 (Architecture), item #11

---

## Problème

Les erreurs réseau étaient silencieuses dans les 8 ViewModels v2 : `isLoading` passait à `false` sans données et l'utilisateur voyait un écran vide sans explication.

## Solution

3 fichiers créés + 8 ViewModels modifiés + 8 écrans avec affichage d'erreur.

---

## Fichiers créés

### 1. `ui/base/state/UiError.kt`

Modèle d'erreur unifié — sealed class avec mapping automatique des exceptions :

```kotlin
sealed class UiError {
    abstract val cause: Throwable?
    abstract val messageRes: Int  // string resource pour le message

    data class Network(...)   → R.string.state_error_network
    data class Timeout(...)   → R.string.state_error_timeout
    data class Server(...)    → R.string.state_error_server
    data class Auth(...)      → R.string.state_error_auth
    data class NotFound(...)  → R.string.state_error_not_found
    data class Unknown(...)   → R.string.state_error_generic
}

fun Throwable.toUiError(): UiError
```

Mapping des exceptions :
- `SocketTimeoutException` → `UiError.Timeout`
- `UnknownHostException`, `IOException` → `UiError.Network`
- `ApiClientException` → inspecte la cause (Network/Timeout/Server)
- Autres → `UiError.Unknown`

### 2. `ui/base/state/ErrorBanner.kt`

Composable compact (56dp) qui s'affiche en overlay au-dessus du contenu :

```kotlin
@Composable
fun ErrorBanner(
    error: UiError?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
)
```

- Fond `ds_error_container`, texte `ds_on_error_container`
- Icône d'erreur + message humain via `stringResource(error.messageRes)`
- Bouton "Réessayer" avec focus D-pad et animation 1.06x
- Ne s'affiche pas si `error == null`

### 3. Réutilisation de `ui/base/state/ErrorState.kt` (existant)

Composable plein écran déjà existant, utilisé via `StateContainer` pour les écrans browse quand il n'y a aucun contenu partiel.

---

## ViewModels modifiés (8)

| ViewModel | Fichier | Changements |
|-----------|---------|-------------|
| `ItemDetailsViewModel` | `ui/itemdetail/v2/ItemDetailsViewModel.kt` | `error: UiError?` ajouté à `ItemDetailsUiState`, catch → `err.toUiError()`, `retry()` ajouté, `lastItemId` stocké |
| `LibraryBrowseViewModel` | `ui/browsing/v2/LibraryBrowseViewModel.kt` | `error: UiError?` ajouté à `LibraryBrowseUiState`, catch → `err.toUiError()`, `error = null` au reset, `retry()` → `loadItems(reset = true)` |
| `MusicBrowseViewModel` | `ui/browsing/v2/MusicBrowseViewModel.kt` | `error: UiError?` ajouté à `MusicBrowseUiState`, catch → `err.toUiError()`, `retry()` clear error + `loadAllRows()` |
| `GenresGridViewModel` | `ui/browsing/v2/GenresGridViewModel.kt` | `error: UiError?` ajouté à `GenresGridUiState`, 2 catch → `e.toUiError()`, `error = null` dans `loadGenres()`, `retry()` → `loadData()` |
| `LiveTvBrowseViewModel` | `ui/browsing/v2/LiveTvBrowseViewModel.kt` | `error: UiError?` ajouté à `LiveTvBrowseUiState`, catch → `err.toUiError()`, `retry()` préserve libraryName/canManage |
| `RecordingsBrowseViewModel` | `ui/browsing/v2/RecordingsBrowseViewModel.kt` | `error: UiError?` ajouté à `RecordingsBrowseUiState`, catch → `err.toUiError()`, `retry()` → `initialize()` |
| `ScheduleBrowseViewModel` | `ui/browsing/v2/ScheduleBrowseViewModel.kt` | `error: UiError?` ajouté à `ScheduleBrowseUiState`, catch → `err.toUiError()`, `lastContext` stocké, `retry()` |
| `SeriesRecordingsBrowseViewModel` | `ui/browsing/v2/SeriesRecordingsBrowseViewModel.kt` | `error: UiError?` ajouté à `SeriesRecordingsBrowseUiState`, catch → `err.toUiError()`, `retry()` → `initialize()` |

---

## Écrans avec ErrorState/ErrorBanner affiché (8)

| Écran | Fichier | Type d'affichage | Condition |
|-------|---------|-----------------|-----------|
| ItemDetails | `ui/itemdetail/v2/ItemDetailsFragment.kt` | `ErrorState` (plein écran) | `error != null && item == null` |
| LibraryBrowse | `ui/browsing/v2/LibraryBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null && items.isEmpty()` |
| GenresGrid | `ui/browsing/v2/GenresGridV2Fragment.kt` | `ErrorState` via `StateContainer` | `error != null && genres.isEmpty()` |
| MusicBrowse | `ui/browsing/v2/MusicBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null` |
| LiveTvBrowse | `ui/browsing/v2/LiveTvBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null` |
| RecordingsBrowse | `ui/browsing/v2/RecordingsBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null` |
| ScheduleBrowse | `ui/browsing/v2/ScheduleBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null` |
| SeriesRecordings | `ui/browsing/v2/SeriesRecordingsBrowseFragment.kt` | `ErrorState` via `StateContainer` | `error != null` |

Tous les écrans proposent un bouton "Réessayer" qui appelle `viewModel.retry()`.

---

## Strings utilisées (préexistantes)

| Clé | EN | FR |
|-----|----|----|
| `state_error_network` | Unable to connect. Check your network connection. | Impossible de se connecter. Vérifie ta connexion réseau. |
| `state_error_timeout` | Connection timed out. The server is not responding. | Connexion expirée. Le serveur ne répond pas. |
| `state_error_server` | A server error occurred. Please try again later. | Une erreur serveur est survenue. Réessaie plus tard. |
| `state_error_not_found` | This content is no longer available. | Ce contenu n'est plus disponible. |
| `state_error_auth` | Your session has expired. Please sign in again. | Ta session a expiré. Reconnecte-toi. |
| `state_error_generic` | Something went wrong. Please try again. | Une erreur est survenue. Réessaie. |
| `lbl_retry` | Retry | Réessayer |

---

## Compilation

```
BUILD SUCCESSFUL in 4m 22s
356 actionable tasks: 54 executed, 302 up-to-date
```

Aucune régression. Aucun nouveau warning de compilation.
