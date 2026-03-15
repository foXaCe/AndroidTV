# Startup Cleanup — Phase 1

## Résumé

Suppression des 5 anciens fragments startup Jellyfin (XML/ViewBinding) et redirection
vers les nouveaux écrans VegafoX (Compose). Correction du splash et du focus profils.

## Fichiers Kotlin supprimés

| Fichier | Rôle (ancien) |
|---------|--------------|
| `ui/startup/fragment/SelectServerFragment.kt` | Sélection serveur (RecyclerView + ViewBinding) |
| `ui/startup/fragment/ServerFragment.kt` | Sélection profil par serveur (UserCardView) |
| `ui/startup/fragment/UserLoginFragment.kt` | Login (container QuickConnect + Credentials) |
| `ui/startup/fragment/UserLoginCredentialsFragment.kt` | Formulaire credentials (ViewBinding) |
| `ui/startup/fragment/UserLoginQuickConnectFragment.kt` | QuickConnect code (ViewBinding) |

## Layouts XML supprimés

| Layout | Utilisé par |
|--------|------------|
| `fragment_select_server.xml` | SelectServerFragment |
| `fragment_server.xml` | ServerFragment |
| `fragment_user_login.xml` | UserLoginFragment (fragment/) |
| `fragment_user_login_credentials.xml` | UserLoginCredentialsFragment |
| `fragment_user_login_quick_connect.xml` | UserLoginQuickConnectFragment |

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/startup/user/UserSelectionFragment.kt` | `navigateToSelectServer()` → `ServerDiscoveryFragment` (suppression import ancien) |
| `ui/startup/fragment/ServerAddFragment.kt` | Après connexion → `UserSelectionFragment` (user/) au lieu de `ServerFragment` |
| `ui/startup/server/QuickConnectFragment.kt` | Après connexion → `UserSelectionFragment` (user/) au lieu de `ServerFragment` |
| `ui/startup/StartupActivity.kt` | `showSplash()` cache aussi `toolbar_view` (fix barre parasite) |
| `ui/startup/user/UserSelectionScreen.kt` | `FocusRequester` sur la 1ère SpotCard pour focus initial D-pad |
| `app/lint-baseline.xml` | 7 blocs `<issue>` retirés (fichiers supprimés) |
| `gradle.properties` | `org.gradle.jvmargs=-Xmx4g` (fix OOM R8) |

## Références nettoyées

Après suppression, **0 référence orpheline** trouvée dans le code Kotlin.
Les seules mentions restantes étaient dans `lint-baseline.xml` (7 blocs supprimés).

## Flux testés

### Flux 1 — Première ouverture
WelcomeScreen → ServerDiscoveryScreen → (saisie adresse) → UserSelectionScreen → Home
**Résultat : OK** — Le nouveau design VegafoX s'affiche à chaque étape.

### Flux 2 — Changer de serveur
UserSelectionScreen → "Changer de serveur" → ServerDiscoveryScreen (nouveau VegafoX)
**Résultat : OK** — Navigue vers le nouveau ServerDiscoveryScreen, pas l'ancien SelectServerFragment.

## Corrections supplémentaires

1. **Splash sans barre parasite** : `showSplash()` cache maintenant `toolbar_view` (la vieille
   barre avec l'heure n'apparaît plus pendant le splash d'authentification)

2. **Focus D-pad sur profils** : `FocusRequester` ajouté sur la première `SpotCard` dans
   `UserSelectionScreen`. Le premier profil reçoit le focus système au chargement.

## Doublon ItemDetail v1

`Destinations.itemDetails()` (v1, `ItemDetailComposeFragment`) est **encore très utilisé** :

- `SocketHandler.kt` (notification push)
- `AudioNowPlayingViewModel.kt` (artiste)
- `CustomPlaybackOverlayFragment.kt` (personne)
- `ItemDetailsFragment.kt` (navigation parent)
- `ItemListFragment.kt` (artiste album)
- `ItemLauncher.kt` (6 appels)
- `MediaDetailsFragment.kt` (jellyseerr → jellyfin)
- `ShuffleManager.kt` (shuffle)
- `StartupActivity.kt` (deep link)

**Statut : NE PAS supprimer** — Migration v1→v2 nécessaire avant suppression.

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Installé sur AM9 Pro (debug + release)
