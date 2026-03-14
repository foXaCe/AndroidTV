# Audit — Écran de sélection utilisateur

## 1. Fichiers concernés

### Écran principal « Who's watching? »

| Fichier | Chemin | Techno |
|---------|--------|--------|
| ServerFragment.kt | `ui/startup/fragment/ServerFragment.kt` | View system (XML + ViewBinding) |
| fragment_server.xml | `res/layout/fragment_server.xml` | XML layout |
| UserCardView.kt | `ui/card/UserCardView.kt` | Hybrid (AbstractComposeView → Compose) |
| ProfilePicture.kt | `ui/base/ProfilePicture.kt` | Compose |

### Écran de login (mot de passe / Quick Connect)

| Fichier | Chemin | Techno |
|---------|--------|--------|
| UserLoginFragment.kt | `ui/startup/fragment/UserLoginFragment.kt` | View system (XML + ViewBinding) |
| fragment_user_login.xml | `res/layout/fragment_user_login.xml` | XML layout |
| UserLoginCredentialsFragment.kt | `ui/startup/fragment/UserLoginCredentialsFragment.kt` | View system |
| fragment_user_login_credentials.xml | `res/layout/fragment_user_login_credentials.xml` | XML layout |
| UserLoginQuickConnectFragment.kt | `ui/startup/fragment/UserLoginQuickConnectFragment.kt` | View system |
| fragment_user_login_quick_connect.xml | `res/layout/fragment_user_login_quick_connect.xml` | XML layout |

### Dialog PIN

| Fichier | Chemin | Techno |
|---------|--------|--------|
| PinEntryDialog.kt | `ui/startup/PinEntryDialog.kt` | View system (AlertDialog + XML) |
| dialog_pin_entry.xml | `res/layout/dialog_pin_entry.xml` | XML layout |
| PinCodeUtil.kt | `util/PinCodeUtil.kt` | Kotlin utility |

### ViewModels

| Fichier | Chemin | Rôle |
|---------|--------|------|
| StartupViewModel.kt | `ui/startup/StartupViewModel.kt` | Chargement utilisateurs, auth auto |
| UserLoginViewModel.kt | `ui/startup/UserLoginViewModel.kt` | Login credentials + Quick Connect |

### Activité hôte

| Fichier | Chemin | Techno |
|---------|--------|--------|
| StartupActivity.kt | `ui/startup/StartupActivity.kt` | FragmentActivity + ViewBinding |
| activity_startup.xml | `res/layout/activity_startup.xml` | XML (RelativeLayout + ComposeView) |

### Composants VegafoX de référence (déjà migrés)

| Fichier | Chemin | Rôle |
|---------|--------|------|
| WelcomeFragment.kt | `ui/startup/fragment/WelcomeFragment.kt` | Premier lancement |
| WelcomeScreen.kt | `ui/startup/compose/WelcomeScreen.kt` | Compose — référence UX VegafoX |
| ServerDiscoveryScreen.kt | `ui/startup/server/ServerDiscoveryScreen.kt` | Compose — découverte serveurs |
| QuickConnectScreen.kt | `ui/startup/server/QuickConnectScreen.kt` | Compose — QC pendant discovery |
| VegafoXFoxLogo.kt | `ui/startup/compose/VegafoXFoxLogo.kt` | Logo animé renard |
| VegafoXTitleText.kt | `ui/startup/compose/VegafoXTitleText.kt` | "Vega" bleu + "foX" orange |
| VegafoXButton.kt | `ui/base/components/VegafoXButton.kt` | Bouton DS (4 variants) |
| VegafoXColors.kt | `ui/base/theme/VegafoXColors.kt` | Palette de couleurs |

---

## 2. Flux de navigation

```
StartupActivity
  ├── SplashFragment (session existante → MainActivity)
  ├── WelcomeFragment (premier lancement → ServerDiscovery)
  └── ServerFragment ← ÉCRAN AUDITÉ
        ├── Clic utilisateur sans PIN → auth auto → MainActivity
        ├── Clic utilisateur avec PIN → PinEntryDialog
        │     ├── PIN OK → auth auto → MainActivity
        │     └── "Forgot PIN" → UserLoginFragment
        ├── Auth échoue (RequireSignIn) → UserLoginFragment
        │     ├── UserLoginCredentialsFragment (username + password)
        │     └── UserLoginQuickConnectFragment (code 6 chiffres)
        ├── "Add account" → UserLoginFragment (username vide)
        └── "Select server" → SelectServerFragment
```

---

## 3. Fonctionnement détaillé

### Chargement des utilisateurs

- `StartupViewModel.loadUsers(server)` → coroutine
- Récupère `storedServerUsers` (utilisateurs sauvegardés localement) + `publicServerUsers` (endpoint public du serveur)
- Tri : par `lastUsed` (si activé dans préférences) puis par nom
- Résultat émis via `MutableStateFlow<List<User>>`
- Types : `PrivateUser` (local, a `lastUsed` + `accessToken`) et `PublicUser` (serveur, pas de token)

### Affichage avatar

- **Composable** `ProfilePicture` utilise **Coil 3** (`rememberAsyncImagePainter`)
- URL construite par `AuthenticationRepository.getUserImageUrl()` → URL Jellyfin SDK avec `userId`, `primaryImageTag`, `ImageType.PRIMARY`
- Fallback : icône vectorielle `ic_user` (silhouette)
- Pas de shimmer de chargement, pas de gestion d'erreur visible

### Gestion mot de passe

- Si `PinCodeUtil.isPinEnabled()` → `PinEntryDialog` (pavé numérique, PIN hashé SHA-256)
- Si pas de PIN : `AutomaticAuthenticateMethod` (token stocké)
- Si RequireSignIn → `UserLoginFragment` → credentials ou Quick Connect
- Credentials : `EditText` classique `inputType="textPassword"`, IME "Done"
- Quick Connect : code 6 chiffres formaté "XXX XXX", polling 5s

### Navigation post-login

- `AuthenticatedState` émis → `StartupActivity` détecte le changement de session → `openNextActivity()` → `MainActivity`

### Utilisateurs sans mot de passe

- Auto-authentifiés via token stocké, aucun dialog affiché

### Bouton « Autre utilisateur »

- "Add account" dans `ServerFragment` → navigation vers `UserLoginFragment` avec username vide
- Permet saisie libre de username + password

---

## 4. Inventaire visuel actuel

### ServerFragment (sélection utilisateur)

| Élément | Détail |
|---------|--------|
| Background | `bg_login_gradient` (image PNG bitmap) |
| Carte | 700dp, fond `#CC111528`, bordure `#33FFFFFF`, coins 20dp |
| Header | Icône VegafoX 40dp + nom app 28sp |
| Nom serveur | 16sp, couleur `login_text_muted` (#80FFFFFF) |
| Titre | "Who's watching?" 32sp bold blanc |
| Liste users | `RecyclerView` horizontal, `LinearLayoutManager` |
| Carte user | `UserCardView` 110dp wide, avatar circulaire, bordure 2dp |
| Avatar | Rond (`CircleShape`), padding 24dp pour icône fallback |
| Nom user | Sous l'avatar, marquee au focus |
| Animation focus | Scale 1.06x, changement couleur bordure |
| Bouton Edit | `AppCompatButton` style `Button.Default` |
| Boutons actions | "Add account" + "Select server" — `AppCompatButton` |
| Disclaimer | `ExpandableTextView` 13sp muted |

### UserLoginFragment (écran login)

| Élément | Détail |
|---------|--------|
| Carte | 600dp (incohérent avec 700dp ci-dessus) |
| Header | Icône 36dp (incohérent avec 40dp ci-dessus) |
| Titre | "Please sign in" 28sp bold |
| Sous-titre | "Connecting to {server}" 16sp muted |
| Boutons | View system `Button` avec `@style/Button.Default` |

### Credentials

| Élément | Détail |
|---------|--------|
| Labels | 16sp, `login_text_secondary` (#B3FFFFFF) |
| Champs | `EditText` avec `@style/Input.Default`, 18sp |
| Erreur | 14sp, `login_error_text` (#ef4444) |
| Bouton login | `@style/Button.Default` 16sp |

### Quick Connect (login)

| Élément | Détail |
|---------|--------|
| Code | 48sp bold monospace, couleur `jellyfin_blue` (!!) |
| Progress | `AppCompat ProgressBar` horizontal 200dp |
| Instructions | 15sp `login_text_secondary` |

### PinEntryDialog

| Élément | Détail |
|---------|--------|
| Dialog | `AlertDialog`, largeur 320dp |
| Champ PIN | `EditText` numberPassword |
| Pavé | GridLayout 3×4, boutons 60×48dp |
| Erreur | `@color/red` hardcodé |
| Forgot PIN | Style `Widget.AppCompat.Button.Borderless` |

---

## 5. Couleurs login (colors.xml)

| Nom | Valeur | Usage |
|-----|--------|-------|
| `login_card_bg` | `#CC111528` | Fond carte login |
| `login_card_border` | `#33FFFFFF` | Bordure carte |
| `login_text_secondary` | `#B3FFFFFF` | Labels, instructions |
| `login_text_muted` | `#80FFFFFF` | Nom serveur, disclaimer |
| `login_error_text` | `#ef4444` | Messages erreur |
| `jellyfin_blue` | `#00A4DC` | Code QC (ancien branding!) |

---

## 6. Comparaison avec écrans VegafoX déjà migrés

| Aspect | ServerFragment (actuel) | WelcomeScreen (référence) |
|--------|------------------------|--------------------------|
| Techno | View system XML | Compose pur |
| Background | PNG bitmap | VegafoXColors.Background + Canvas |
| Boutons | AppCompatButton | VegafoXButton (4 variants) |
| Animations | Scale focus seulement | Staggered entry, spring, glow |
| Logo | ImageView statique | VegafoXFoxLogo animé + pulsing |
| Titre | TextView blanc | VegafoXTitleText bicolore |
| Couleurs | Hardcodées XML | VegafoXColors tokens |
| Focus | RecyclerView basique | Scale + glow + couleur |

---

## 7. Dette technique — Liste priorisée

### P0 — Critique (incohérence branding)

1. **ServerFragment entièrement View system** — WelcomeScreen, ServerDiscoveryScreen et QuickConnectScreen sont en Compose VegafoX. L'écran de sélection utilisateur est le maillon manquant.

2. **UserLoginQuickConnectFragment utilise `@color/jellyfin_blue`** — ancien branding Jellyfin pour le code QC, alors que `QuickConnectScreen.kt` utilise l'orange VegafoX.

3. **PinEntryDialog sans thème VegafoX** — `AlertDialog` brut, aucune cohérence avec le DS.

### P1 — Haute (composants / style)

4. **Tous les boutons sont `AppCompatButton`** — ServerFragment (3 boutons), UserLoginFragment (3), credentials (1), QC (0), PIN dialog (13). Total : ~20 boutons à migrer vers `VegafoXButton`.

5. **`UserCardView` utilise le bridge `AbstractComposeView`** — code complexe de forwarding focus, devrait être Compose pur.

6. **Background PNG vs VegafoXColors** — les écrans View utilisent `bg_login_gradient` (bitmap) vs les effets Canvas des écrans Compose.

7. **Largeur carte incohérente** — 700dp (ServerFragment) vs 600dp (UserLoginFragment).

8. **Taille icône incohérente** — 40dp (ServerFragment) vs 36dp (UserLoginFragment).

9. **Fallback "Jellyfin"** hardcodé dans `UserLoginFragment` ligne 78 — devrait être `R.string.app_name`.

### P2 — Moyenne (animations / UX)

10. **Pas d'animation d'entrée** sur la liste utilisateurs ni les cartes.

11. **Pas d'animation stagger** sur l'apparition des cartes utilisateur.

12. **Pas de transition animée** entre ServerFragment et UserLoginFragment.

13. **Pas de shimmer de chargement** dans `ProfilePicture` pendant le chargement avatar.

14. **Pas de toggle "afficher mot de passe"** sur l'écran credentials.

15. **Pas d'indicateur de chargement** pendant l'authentification.

### P3 — Basse (qualité code)

16. **Valeurs dp hardcodées** dans ServerFragment pour le centrage (lignes 93-99).

17. **PIN max length dupliqué** : XML (`maxLength="10"`) et Kotlin.

18. **`UserAdapter` inner class** couplée au Fragment.

19. **Aucun test unitaire** pour la sélection utilisateur ou le login.

20. **Couleurs hardcodées en XML** — `@color/white`, `@color/login_text_muted`, `@color/login_error_text` au lieu de tokens DS.

---

## 8. Tests existants

- `app/src/test/kotlin/ui/startup/server/ServerDiscoveryTest.kt` — teste la découverte serveurs, PAS la sélection utilisateur.
- Aucun test pour `ServerFragment`, `UserLoginFragment`, `PinEntryDialog`, `UserCardView`.

---

## 9. Recommandation de migration

### Ordre suggéré

1. **ServerFragment → Compose** : créer `UserSelectionScreen.kt` en Compose pur, supprimant `fragment_server.xml` et le bridge `UserCardView`
2. **UserLoginFragment → Compose** : créer `UserLoginScreen.kt` unifiant credentials + QC
3. **PinEntryDialog → Compose** : créer `PinEntryDialog` composable avec thème VegafoX
4. **Supprimer les layouts XML** devenus inutiles
5. **Ajouter tests** pour les ViewModels et les écrans Compose

### Composants VegafoX à réutiliser

- `VegafoXFoxLogo` — logo animé header
- `VegafoXTitleText` — titre bicolore
- `VegafoXButton` — tous les boutons (Primary, Secondary, Outlined, Ghost)
- `VegafoXColors` — palette complète
- `VegafoXGradients` — dégradés background
- `ProfilePicture` — avatar (à améliorer avec shimmer)
