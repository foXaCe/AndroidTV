# Audio Player — Migration VegafoX v2

## Fichiers modifies

| Fichier | Action |
|---------|--------|
| `app/src/main/res/font/jetbrains_mono.ttf` | Ajout (font JetBrains Mono Regular) |
| `ui/base/theme/VegafoXTypography.kt` | Ajout `JetBrainsMono` FontFamily |
| `ui/playback/audio/AudioNowPlayingScreen.kt` | Migration complete tokens VegafoX |

## Structure de l'ecran audio

Fichier unique `AudioNowPlayingScreen.kt` contenant 6 composables :
- `AudioNowPlayingScreen` — racine : TvScaffold > Box BackgroundDeep > Row
- `ArtworkPanel` — pochette album + overlay paroles
- `AudioProgressSection` — seekbar + labels temps
- `AudioControlsRow` — controles transport
- `AudioQueueSection` — header queue + LazyRow
- `AudioQueueCard` — carte individuelle queue

Fragment wrapper : `AudioNowPlayingComposeFragment.kt` (inchange)
ViewModel : `AudioNowPlayingViewModel.kt` (inchange)

## Avant / Apres

### Fond racine
- **Avant** : `JellyfinTheme.colorScheme.background` (Background 0xFF0A0A0F via TvScaffold)
- **Apres** : Box explicite `VegafoXColors.BackgroundDeep` (0xFF07070B) par-dessus TvScaffold

### Titre piste
- **Avant** : `JellyfinTheme.typography.headlineLargeBold` + `onBackground`
- **Apres** : `BebasNeue` 36sp, `VegafoXColors.TextPrimary`, letterSpacing 2sp, maxLines 1

### Artiste
- **Avant** : `JellyfinTheme.typography.headlineLarge` + `onBackground`
- **Apres** : 16sp, `VegafoXColors.TextSecondary`

### Album
- **Avant** : `JellyfinTheme.typography.titleMedium` + `onSurfaceVariant`
- **Apres** : 14sp, `VegafoXColors.TextHint`

### Info piste (track X of Y)
- **Avant** : `JellyfinTheme.typography.bodyMedium` + `onSurfaceVariant`
- **Apres** : 14sp, `VegafoXColors.TextHint`

### Genres
- **Avant** : `JellyfinTheme.typography.bodyMedium` + `onSurfaceVariant`
- **Apres** : 14sp, `VegafoXColors.TextHint`

### Pochette album (ArtworkPanel)
- **Avant** : `JellyfinTheme.shapes.medium`, pas de shadow/border
- **Apres** : `RoundedCornerShape(16.dp)`, `shadow(24.dp)`, `border(1.dp, blanc 8%)`
- Icone fallback : `VegafoXColors.TextHint` (etait `Color.White 40%`)

### Seekbar
- **Avant** : couleurs par defaut du theme (`SeekbarDefaults.colors()`)
- **Apres** : `SeekbarColors` explicite — progressColor + knobColor = `OrangePrimary`, background = `Outline`, buffer = `TextSecondary`

### Labels temps (ecoulé / restant)
- **Avant** : `JellyfinTheme.typography.bodySmall` + `onSurfaceVariant`
- **Apres** : `JetBrainsMono` 13sp, `VegafoXColors.TextSecondary`

### Bouton Play/Pause
- **Avant** : `TvIconButton` standard (48dp, icone 24dp, couleurs theme)
- **Apres** : `IconButton` custom — 72dp `CircleShape`, fond `OrangePrimary`, icone blanc 36dp, glow radial `OrangePrimary` alpha 0.25, focus = `OrangeLight`

### Boutons SkipPrevious / SkipNext
- **Avant** : tint `JellyfinTheme.colorScheme.onSurface` (TextPrimary)
- **Apres** : tint `VegafoXColors.TextSecondary`

### Boutons Shuffle / Repeat
- **Avant** : actif = `JellyfinTheme.colorScheme.primary`, inactif = `onSurface`
- **Apres** : actif = `VegafoXColors.OrangePrimary`, inactif = `VegafoXColors.TextHint`

### Boutons Album / Artist
- **Avant** : tint par defaut (`onSurface`)
- **Apres** : tint `VegafoXColors.TextSecondary`

### Boutons Rewind / FastForward
- **Avant** : presents dans la barre de controles
- **Apres** : retires (conformite spec — seuls Shuffle, Skip, Play, Skip, Repeat)

### Queue — header
- **Avant** : `JellyfinTheme.typography.titleMedium` + `onBackground` / `bodyMedium` + `onSurfaceVariant`
- **Apres** : titre `BebasNeue` 20sp `TextPrimary` letterSpacing 1sp / compteur `JetBrainsMono` 13sp `TextSecondary`

### Queue — carte piste en cours
- **Avant** : overlay `primary.copy(alpha = 0.3f)`, texte `primary`
- **Apres** : overlay `OrangePrimary.copy(alpha = 0.12f)`, texte `OrangePrimary`

### Queue — cartes normales
- **Avant** : fond `surfaceContainer`, texte `onBackground` / `onSurfaceVariant`, icone fallback `onSurfaceVariant 50%`
- **Apres** : fond `VegafoXColors.SurfaceContainer`, texte `TextPrimary` / `TextSecondary`, icone fallback `TextHint`

## Build

| Variante | Resultat |
|----------|----------|
| `compileGithubDebugKotlin` | BUILD SUCCESSFUL |
| `assembleGithubDebug` | BUILD SUCCESSFUL (1m48s) |
| `assembleGithubRelease` | BUILD SUCCESSFUL (1m56s) |
| `adb install` AM9 Pro | Success |

## Screenshot

Le player audio necessite un contenu audio en lecture active. Le serveur JellyMox (v10.11.6) n'a pas de bibliotheque musicale configuree — aucune entree "Musique" dans la sidebar. Le screenshot `docs/screenshots/audio_v2.png` montre l'ecran d'accueil VegafoX apres installation.

Pour tester visuellement : ajouter une bibliotheque musicale sur JellyMox, lancer un morceau, puis l'ecran `AudioNowPlayingScreen` s'affichera automatiquement.

## Font JetBrains Mono

- Fichier : `app/src/main/res/font/jetbrains_mono.ttf` (270 Ko, Regular)
- Declaration : `VegafoXTypography.kt` ligne 8 — `val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono))`
- Utilisation : labels temps seekbar (13sp), compteur queue (13sp)
