# UI Week 3 — WelcomeScreen

## Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `WelcomeScreen.kt` | `ui.startup.compose` | Écran d'accueil animé : fond radial, grille décorative, horizon line, séquence 6 étapes |
| `WelcomeFragment.kt` | `ui.startup.fragment` | Fragment ComposeView wrappant WelcomeScreen, navigation vers SelectServerFragment |

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `StartupActivity.kt` | `showServerSelection()` → remplace par `WelcomeFragment` (sans toolbar) |
| `strings.xml` | Ajout `lbl_connect_to_server` = "Connect to Server" |

## Architecture WelcomeScreen

### Couches visuelles (z-order)

1. **Fond** — `VegafoXColors.Background` (#0A0A0F) solide
2. **Radial chaud** — `Brush.radialGradient` centré à 40% hauteur, #12080A → Background
3. **Grille décorative** — lignes blanches 3% opacité, pas de 60dp
4. **Horizon line** — 1dp à 65% du haut (35% du bas), `VegafoXGradients.HorizonLine`
5. **Contenu animé** — colonne centrée (renard, titre, séparateur, bouton, version)

### Séquence d'animation

| Temps | Élément | Animation | Durée |
|-------|---------|-----------|-------|
| 200ms | Orbe orange | fadeIn | 600ms |
| 400ms | Renard | scaleIn spring (0.7→1.0, medium bouncy) + fadeIn | 600ms |
| 800ms | "VegafoX" + "MEDIA CENTER" | slideInVertically (½ hauteur) + fadeIn | 500ms |
| 1000ms | Séparateur 60dp | expandHorizontally + fadeIn | 400ms |
| 1100ms | Bouton Connect | slideInVertically + fadeIn, autoFocus D-pad | 400ms |
| 1300ms | Version | fadeIn | 300ms |

### Composables internes (private)

- `SubtleGrid()` — Canvas plein écran, lignes verticales + horizontales
- `HorizonLine()` — BoxWithConstraints pour positionner à 65% du haut
- `WelcomeContent(onConnectClick)` — orchestrateur de la séquence animée

## Navigation startup

```
StartupActivity.onCreate()
  └─ showSplash() → SplashFragment (image vegafox_splash)
  └─ onPermissionsGranted()
       ├─ session existante → splash → MainActivity
       ├─ pas de session, dernier serveur connu → ServerFragment
       └─ pas de session, aucun serveur → WelcomeFragment ← NOUVEAU
            └─ clic "Connect to Server" → SelectServerFragment + toolbar (back stack)
```

## Tokens référencés

- `VegafoXColors` : Background, OrangeGlow, TextHint
- `VegafoXGradients` : HorizonLine
- Composables W2 : `VegafoXFoxLogo(animated=false)`, `VegafoXTitleText`, `VegafoXSubtitle`, `ConnectServerButton(autoFocus=true)`

## Build

- `./gradlew :app:compileGithubDebugKotlin` → BUILD SUCCESSFUL
- 0 erreur, 0 warning
