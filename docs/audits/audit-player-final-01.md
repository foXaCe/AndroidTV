# Audit Player Final — État complet après tous les fix

**Date** : 2026-03-16
**Scope** : Tout le player vidéo (overlay, trickplay, contrôles, still watching, cohérence visuelle)

---

## Point 1 — Skip Overlay

**Fichier** : `ui/playback/overlay/SkipOverlayView.kt`

### Migration composable pur : ✅ COMPLÈTE
- L'`AbstractComposeView` a été **entièrement supprimé**. Le fichier contient uniquement un `@Composable fun SkipOverlay()` pur (ligne 47).
- Aucune classe Android View ne subsiste.

### Bouton : ✅ CONFORME
- Utilise `VegafoXButton` avec `variant = VegafoXButtonVariant.Primary` (ligne 66-71).
- Icône `VegafoXIcons.SkipNext` en position `iconEnd = true`.

### Focus D-pad : ✅ FONCTIONNEL
- `FocusRequester` créé ligne 55.
- Appliqué au bouton via `Modifier.focusRequester(focusRequester)` (ligne 72).
- `LaunchedEffect(Unit) { focusRequester.requestFocus() }` à l'intérieur de `AnimatedVisibility` (lignes 75-77) — le focus est demandé automatiquement dès que le bouton devient visible.

### Labels français : ✅ CORRECTS
Les labels sont résolus via des string resources. Vérification dans `values-fr/strings.xml` :
| segmentType | String resource | Texte français |
|---|---|---|
| `INTRO` | `skip_intro` | "Passer l'intro" |
| `RECAP` | `skip_recap` | "Passer le récapitulatif" |
| `COMMERCIAL` | `skip_commercial` | "Passer la publicité" |
| `PREVIEW` | `skip_preview` | "Passer l'aperçu" |
| `OUTRO` | `skip_outro` | "Passer le générique" |
| Next episode | `play_next_episode_countdown` | "Épisode suivant (%ds)" |
| Next up | `lbl_play_next_up` | "Lire la prochaine vidéo" |
| Fallback | `segment_action_skip` | "Passer" |

> **Note** : Le texte français utilise "Passer le récapitulatif" et non "PASSER LE RÉCAP" — c'est plus naturel. Le bouton n'est pas en majuscules forcées.

### Intégration dans le player : ⚠️ NON INTÉGRÉ DANS LE NOUVEAU PLAYER
- **`SkipOverlay` n'est appelé nulle part dans le package `ui/player/`**. Une recherche exhaustive dans `ui/player/` ne retourne aucun import.
- Le composable existe uniquement dans `ui/playback/overlay/SkipOverlayView.kt` mais n'est référencé que par l'ancien système de playback (VLC/Leanback).
- **Le nouveau player Compose (`VideoPlayerScreen` → `VideoPlayerOverlay` → `PlayerOverlayLayout`) ne contient aucun appel à `SkipOverlay`.**
- **Sévérité : HAUTE** — Le skip de segments (intro, outro, etc.) ne fonctionne pas dans le nouveau player Compose.

---

## Point 2 — Trickplay

### SeekProvider.currentThumbnail : ✅ IMPLÉMENTÉ
- `_currentThumbnail = MutableStateFlow<Bitmap?>(null)` (ligne 55)
- `val currentThumbnail: StateFlow<Bitmap?> = _currentThumbnail.asStateFlow()` (ligne 56)
- `updatePosition(positionMs: Long)` met à jour le flow (lignes 418-439)
- `clearThumbnail()` remet à null (lignes 441-443)

### Enregistrement Koin : ✅ FAIT
- `AppModule.kt` ligne 181 : `single { SeekProvider(get(), get(), get(), androidContext(), get()) }`

### Seekbar.thumbnailContent : ✅ PARAMÈTRE PRÉSENT
- `Seekbar.kt` : le paramètre `thumbnailContent: (@Composable (fraction: Float) -> Unit)? = null` est présent sur les deux surcharges (lignes 79, 114).
- L'affichage est conditionnel au scrubbing via `AnimatedVisibility(visible = isScrubbing)` (lignes 223-249).
- La taille thumbnail est fixe : 178×100 dp (lignes 220-221).
- Le positionnement X suit la position de scrub, clampé aux bords de la seekbar.

### TrickplayThumbnail.kt : ✅ EXISTE
- `ui/player/base/TrickplayThumbnail.kt` — composable simple qui collecte `seekProvider.currentThumbnail` et affiche un `Image` Compose dans un `Box` avec border orange et fond `VegafoXColors.Surface`.

### VideoPlayerControls intégration : ✅ COMPLÈTE
- `seekProvider: SeekProvider = koinInject()` (ligne 74)
- `trickPlayEnabled = userPreferences[UserPreferences.trickPlayEnabled]` (ligne 80)
- `onScrubPosition` passe `seekProvider.updatePosition(posMs)` / `seekProvider.clearThumbnail()` (lignes 104-109)
- `thumbnailContent` passe `{ _ -> TrickplayThumbnail(seekProvider) }` quand activé (lignes 111-115)

### Préférence trickPlayEnabled : ✅ CORRECTE
- `UserPreferences.kt` ligne 358 : `var trickPlayEnabled = booleanPreference("trick_play_enabled", false)`
- Défaut : **false** (désactivé par défaut).

### Pipeline complète : ✅ FONCTIONNELLE
1. User scrub D-pad gauche/droite → `Seekbar` intercepte `onKeyEvent` → `onSeek` callback
2. `PlayerSeekbar` traduit en `onScrubPosition(posMs)` → `seekProvider.updatePosition(posMs)`
3. `SeekProvider` résout l'index trickplay, charge le bitmap → met à jour `_currentThumbnail`
4. `TrickplayThumbnail` collecte le flow → affiche le bitmap
5. `AnimatedVisibility` n'affiche que pendant le scrubbing

---

## Point 3 — Contrôles Player

**Fichier** : `ui/player/video/VideoPlayerControls.kt`

### Layout des rows

| Row | Contenu |
|---|---|
| **Row 1** | Position (gauche) — Temps restant (droite) |
| **Row 2** | Seekbar (hauteur animée 4dp→8dp au focus) |
| **Row 3** | Boutons de contrôle |

### Boutons de contrôle (Row 3, dans l'ordre)

| # | Icône | Label accessible | Action | Composable | Notes |
|---|---|---|---|---|---|
| 1 | `Play` / `Pause` | `lbl_play` / `lbl_pause` | Toggle play/pause | `PlayerActionBtn` (isPrimary=true) | Orange, icône 28dp, reçoit `focusRequester` + `onVisibilityChanged` pour auto-focus |
| 2 | `Replay10` | `rewind` | Rewind -10s | `PlayerActionBtn` | — |
| 3 | `Forward30` | `fast_forward` | Forward +30s | `PlayerActionBtn` | — |
| 4 | `SkipPrevious` | `lbl_prev_item` | Previous queue entry | `PreviousEntryBtn` (conditionnel) | Visible si `entryIndex > 0` |
| 5 | `SkipNext` | `lbl_next_item` | Next queue entry | `NextEntryBtn` (conditionnel) | Visible si pas dernier |
| — | **Divider** | — | — | `Box` 1×28dp, `VegafoXColors.Divider` | Séparateur visuel |
| 6 | `Subtitles` | `lbl_subtitle_track` | Ouvre dialog sous-titres | `PlayerActionBtn` | isPrimary=true quand sous-titre actif |
| 7 | `Audiotrack` | `lbl_audio_track` | Ouvre dialog piste audio | `PlayerActionBtn` | — |
| 8 | `ListBulleted` | `lbl_chapters` | Ouvre dialog chapitres | `PlayerActionBtn` | — |
| 9 | `Hd` | `lbl_quality_profile` | Ouvre dialog qualité | `PlayerActionBtn` | — |
| 10 | `Speed` | `lbl_playback_speed` | Ouvre dialog vitesse | `PlayerActionBtn` | — |
| 11 | `ZoomOutMap` | `lbl_zoom` | Ouvre dialog zoom | `PlayerActionBtn` | — |

### Composable des boutons
- Tous utilisent `PlayerActionBtn` → `IconButton` du design system (lignes 266-299).
- L'icône fait 22dp pour les boutons standard, 28dp pour les boutons primary.
- Les couleurs primary utilisent `VegafoXColors.OrangePrimary` / `VegafoXColors.OrangeLight`.
- Les couleurs standard utilisent `ButtonDefaults.colors()` (defaults du design system).

### Hauteur et style homogènes : ✅
- Tous les boutons passent par `IconButton` avec les mêmes `ButtonDefaults`. La taille est déterminée par le padding par défaut d'`IconButton` + la taille d'icône. Homogène.

### Navigation D-pad : ✅ CORRECTE
- `Row` englobante avec `Modifier.focusRestorer().focusGroup()` (lignes 132-133).
- L'ordre de focus gauche-droite correspond à l'ordre visuel (Play → Rewind → Forward → Prev → Next → divider → Subtitles → Audio → Chapters → Quality → Speed → Zoom).
- Le bouton Play reçoit le focus initial via `FocusRequester` + `onVisibilityChanged { focusRequester.requestFocus() }`.

---

## Point 4 — StillWatching

**Fichier** : `ui/playback/stillwatching/StillWatchingFragment.kt`

### Focus initial sur Continue Watching : ✅ CORRIGÉ
- `FocusRequester` créé ligne 204.
- Appliqué au bouton `VegafoXButton` "Continue Watching" via `Modifier.focusRequester(focusRequester)` (ligne 323).
- La `Row` de boutons utilise `focusRestorer(focusRequester)` (ligne 287) — le focus est bien restauré sur Continue Watching.
- Le bouton Exit **ne reçoit pas** le focus initial.

### Couleur hardcodée `Color(0x66FF5050)` : ✅ REMPLACÉE
- Ligne 76 : `val ExitProgressColor = VegafoXColors.Error.copy(alpha = 0.4f)` — utilise maintenant un token VegafoX.

### Timeout 10s : ⚠️ TOUJOURS HARDCODÉ
- Ligne 74 : `val TIMEOUT_IN_MS = 10.seconds.inWholeMilliseconds.toInt()` — constante en dur, ne lit aucune préférence utilisateur.
- **Sévérité : BASSE** — Acceptable comme constante de product, pas une vraie préférence utilisateur.

### Autres observations :
- `Color.Black` utilisé ligne 111 comme fond de base (sous le backdrop blur) — acceptable pour un écran player.
- Les `TextStyle()` manuelles utilisent des tokens VegafoX (`VegafoXColors.OrangePrimary`, `VegafoXColors.TextPrimary`, `VegafoXColors.TextSecondary`, `BebasNeue`).

---

## Point 5 — Cohérence visuelle globale du player

### Color.Black dans VideoPlayerScreen : ⚠️ TOUJOURS PRÉSENT
- `VideoPlayerScreen.kt` ligne 61 : `.background(Color.Black)` — couleur hardcodée pour le fond du player.
- **Justification** : Le fond d'un player vidéo doit être noir pur (#000000) pour le contraste cinématique. Utiliser un token de design system n'aurait pas de sens ici.
- **Verdict** : Acceptable tel quel, pas un bug.

### Color(0xFFFFD700) pour badge HDR : ⚠️ TOUJOURS HARDCODÉ
- `VideoPlayerHeader.kt` ligne 210 : `BadgeStyle.HDR -> Color(0xFFFFD700) // Gold`
- N'est pas extrait dans un token VegafoX.
- **Sévérité : TRÈS BASSE** — Couleur spécifique au badge HDR, peu susceptible de changer.

### Color(0xBF060A0F) dans MediaToast : ⚠️ TOUJOURS HARDCODÉ
- `MediaToast.kt` ligne 40 : `backgroundColor: Color = Color(0xBF060A0F)`
- N'est pas un token VegafoX.
- **Sévérité : TRÈS BASSE** — Couleur semi-transparente spécifique au toast.

### Typographies manuelles TextStyle() : ⚠️ PRÉSENTES MAIS COHÉRENTES
- `VideoPlayerControls.kt` : `PositionText` (lignes 362-372) et `RemainingText` (lignes 388-397) utilisent `TextStyle()` manuelles mais avec des tokens VegafoX (`VegafoXColors.TextPrimary`, `VegafoXColors.TextSecondary`).
- `VideoPlayerHeader.kt` : Toutes les `TextStyle()` utilisent des tokens VegafoX pour les couleurs, `BebasNeue` pour la font family.
- **Verdict** : Les typographies sont manuelles mais cohérentes avec le design system. Pas de régression.

### TechBadge extraction : ❌ NON EXTRAIT
- `TechBadge` est toujours un `@Composable private fun` inline dans `VideoPlayerHeader.kt` (lignes 202-231).
- Non disponible dans le design system (`ui/base/`).
- **Sévérité : BASSE** — Composable utilisé uniquement dans le header du player, pas de réutilisation nécessaire actuellement.

---

## Point 6 — Problèmes résiduels non traités

### 6.1 — SkipOverlay non intégré dans le nouveau player
- **Sévérité : HAUTE**
- **Effort : Moyen (~2h)**
- Le composable `SkipOverlay` existe et fonctionne mais n'est appelé nulle part dans `VideoPlayerScreen` / `VideoPlayerOverlay`. Il faut l'intégrer dans `VideoPlayerScreen.kt` dans le `Box` principal, au-dessus de `VideoPlayerOverlay` et `PlayerSubtitles`, en le connectant au `MediaSegmentRepository` pour recevoir les événements de segment.

### 6.2 — Badges techniques pouvant déborder horizontalement
- **Sévérité : BASSE**
- **Effort : Faible (~30min)**
- La `Row` de badges dans `VideoPlayerHeader.kt` (lignes 150-159) n'a pas de `horizontalScroll` ni de `overflow` handling. Si un média a beaucoup de badges (résolution + HDR + codec vidéo + container + canaux audio + codec audio = 6 badges), ils pourraient théoriquement déborder sur la colonne de droite (horloge).
- **Mitigation actuelle** : La `Row` est dans une `Column` avec `Modifier.weight(1f)`, ce qui limite sa largeur.

### 6.3 — Sous-titres masquables par le gradient des contrôles
- **Sévérité : MOYENNE**
- **Effort : Moyen (~1-2h)**
- Dans `VideoPlayerScreen.kt`, `PlayerSubtitles` est dessiné **après** `VideoPlayerOverlay` (lignes 78-85), donc au-dessus du gradient des contrôles. Cependant, quand les contrôles sont visibles, le gradient bottom (45% de la hauteur, de transparent à 95% opaque) peut chevaucher les sous-titres situés en bas de la zone vidéo.
- **Note** : L'ordre z-index place les sous-titres au-dessus du gradient, donc ils restent lisibles. Le problème est esthétique uniquement.

### 6.4 — Seek initial brut avec 20 commandes
- **Sévérité : MOYENNE**
- **Effort : Faible (~1h)**
- `VideoPlayerFragment.kt` lignes 55-63 : Le seek initial utilise `repeat(20)` avec un `delay(50)` pour attendre que le backend soit prêt. C'est un polling brut qui envoie 20 commandes de seek.
- **Suggestion** : Remplacer par un `flow.first { backendReady }` ou un callback du backend.

### 6.5 — Absence de contrôle volume via D-pad haut/bas
- **Sévérité : BASSE**
- **Effort : Moyen (~2h)**
- `PlayerOverlayLayout.kt` : Les touches `Key.DirectionUp` et `Key.DirectionDown` ne sont pas interceptées quand l'overlay est masqué. Aucune logique de volume n'est implémentée.
- **Note** : Sur Android TV, le système gère nativement le volume via les touches hardware. L'absence de contrôle via D-pad vertical n'est problématique que pour les télécommandes sans boutons volume dédiés.

### 6.6 — Absence d'animation sur la seekbar lors du scrubbing D-pad
- **Sévérité : TRÈS BASSE**
- **Effort : Faible (~1h)**
- La seekbar utilise `animateFloatAsState` pour le knob alpha (apparition/disparition), mais la progression elle-même saute directement de valeur en valeur lors du scrub D-pad. Il n'y a pas d'`animateFloatAsState` sur la position de progression.
- **Note** : C'est un choix UX cohérent avec les players TV standards qui montrent la position exacte pendant le scrub.

---

## Résumé

| # | Point | Status | Sévérité |
|---|---|---|---|
| 1 | SkipOverlay — composable pur | ✅ OK | — |
| 1 | SkipOverlay — VegafoXButton + focus | ✅ OK | — |
| 1 | SkipOverlay — labels français | ✅ OK | — |
| 1 | **SkipOverlay — intégration nouveau player** | ❌ MANQUANT | **HAUTE** |
| 2 | Trickplay — pipeline complète | ✅ OK | — |
| 2 | Trickplay — Koin + préférence | ✅ OK | — |
| 3 | Contrôles — boutons homogènes | ✅ OK | — |
| 3 | Contrôles — navigation D-pad | ✅ OK | — |
| 4 | StillWatching — focus initial | ✅ OK | — |
| 4 | StillWatching — couleur token | ✅ OK | — |
| 4 | StillWatching — timeout hardcodé | ⚠️ | Basse |
| 5 | HDR badge gold hardcodé | ⚠️ | Très basse |
| 5 | MediaToast bgcolor hardcodé | ⚠️ | Très basse |
| 5 | TechBadge non extrait | ⚠️ | Basse |
| 6.1 | **SkipOverlay non connecté** | ❌ | **HAUTE** |
| 6.2 | Badges overflow horizontal | ⚠️ | Basse |
| 6.3 | Sous-titres vs gradient contrôles | ⚠️ | Moyenne |
| 6.4 | **Seek initial repeat(20)** | ⚠️ | **Moyenne** |
| 6.5 | Volume D-pad haut/bas | ⚠️ | Basse |
| 6.6 | Animation seekbar scrub | ⚠️ | Très basse |

### Priorités de correction recommandées

1. **P0 — SkipOverlay** : Intégrer `SkipOverlay` dans `VideoPlayerScreen.kt`, connecté au `MediaSegmentRepository`
2. **P1 — Seek initial** : Remplacer le `repeat(20)` par une attente propre du backend
3. **P2 — Sous-titres/gradient** : Évaluer si un décalage des sous-titres quand les contrôles sont visibles est nécessaire
4. **P3 — Tokens résiduels** : Extraire les 2-3 couleurs hardcodées restantes dans VegafoXColors (optionnel)
