# Jellyseerr 02 — Migration VegafoX Premium (Suite)

## Fichiers modifies

### 1. JellyseerrDiscoverRows.kt
Header des rows horizontales

| Element | Avant | Apres |
|---|---|---|
| Titre row | `JellyfinTheme.typography.titleMedium` (font systeme) | `BebasNeue 24sp, letterSpacing 2sp` |
| Couleur titre | `JellyfinTheme.colorScheme.textPrimary` | `VegafoXColors.TextPrimary` direct |
| Import Text | `org.jellyfin.androidtv.ui.base.Text` (wrapper) | `androidx.tv.material3.Text` |

### 2. DiscoverFragment.kt / JellyseerrDiscoverRowsFragment.kt
- Ecran hybride XML + Compose
- Pas de barre de recherche ni de filtres
- Le backdrop est gere par BackgroundService (blur)
- Pas de composable Compose a migrer au-dela du FICHIER 1

### 3. RequestsFragment.kt / RequestsAdapter.kt
- Entierement XML (RecyclerView + ViewBinding)
- Pas de composable Compose associe
- Migration necessiterait reecriture complete en Compose (hors scope)

### 4. SeasonSelectionDialog.kt

| Element | Avant | Apres |
|---|---|---|
| Fond dialog | `JellyfinTheme.colorScheme.surface` + shapes.dialog | `VegafoXColors.Surface` + `RoundedCornerShape(16.dp)` |
| Bordure dialog | `JellyfinTheme.colorScheme.outlineVariant` | `Color.White.copy(alpha = 0.10f)` |
| Titre | `JellyfinTheme.typography.titleLarge` | `BebasNeue 22sp, letterSpacing 2sp, TextPrimary` |
| Sous-titre | `JellyfinTheme.typography.bodyMedium + textSecondary` | `14sp TextSecondary` |
| Checkbox couleur | `focusBorderColor()` (dynamique) | `VegafoXColors.OrangePrimary` |
| Focus item | `JellyfinTheme.colorScheme.surfaceBright` | `Color.White.copy(alpha = 0.05f)` + bordure gauche 3dp OrangePrimary |
| Label checkbox | `bodyMedium textPrimary/textDisabled` | `16sp TextPrimary/TextDisabled` |
| Bouton Demander | `DialogActionButton` (info/primary) | `VegafoXButton Primary compact autoFocus` |
| Bouton Annuler | `DialogActionButton` (surfaceContainer) | `VegafoXButton Ghost compact` |

### 5. QualitySelectionDialog.kt

| Element | Avant | Apres |
|---|---|---|
| Fond dialog | `JellyfinTheme.shapes.dialog + surface` | `RoundedCornerShape(16.dp) + VegafoXColors.Surface` |
| Titre | `JellyfinTheme.typography.titleLarge` | `BebasNeue 22sp, letterSpacing 2sp, TextPrimary` |
| Sous-titre | `bodyMedium textSecondary` | `14sp TextSecondary` |
| Boutons HD/4K | `QualityButton` custom (shapes.small secondary) | `VegafoXButton Primary compact autoFocus` |
| Bouton Annuler | `QualityButton` (surfaceContainer) | `VegafoXButton Ghost compact` |
| FocusRequester | Manuel `LaunchedEffect + requestFocus()` | `VegafoXButton autoFocus` |
| Composant supprime | `QualityButton` private | Remplace par VegafoXButton |

### 6. AdvancedRequestOptionsDialog.kt

| Element | Avant | Apres |
|---|---|---|
| Fond dialog | `shapes.dialog + surface + outlineVariant` | `RoundedCornerShape(16.dp) + Surface + blanc 10%` |
| Titre | `titleLarge textPrimary` | `BebasNeue 22sp, letterSpacing 2sp, TextPrimary` |
| Sous-titre | `bodyMedium textSecondary` | `14sp TextSecondary` |
| Spinner loading | `secondary` color | `VegafoXColors.OrangePrimary` |
| Erreur | `bodyMedium + error` | `14sp + VegafoXColors.Error` |
| Separateur | `outlineVariant` | `Color.White.copy(alpha = 0.10f)` |
| Section header | `titleMedium textPrimary` | `BebasNeue 18sp, letterSpacing 2sp, TextPrimary` |
| RadioButton focus | `surfaceBright` | `blanc 5% + bordure gauche 3dp OrangePrimary` |
| RadioButton selected | `secondary` | `VegafoXColors.OrangePrimary` |
| Bouton Confirmer | `DialogActionButton (secondary)` | `VegafoXButton Primary compact` |
| Bouton Annuler | `DialogActionButton (surfaceBright)` | `VegafoXButton Ghost compact` |

### 7. JellyseerrRequestButtons.kt

| Element | Avant | Apres |
|---|---|---|
| Bouton Demander | `DetailActionButton` (icon + label card) | `VegafoXButton Primary autoFocus compact` avec icon |
| Bouton desactive | `DetailActionButton + Modifier.alpha(0.5f)` | `VegafoXButton Secondary disabled` |
| Bouton Annuler | `DetailActionButton` | `VegafoXButton Outlined compact` |
| Bouton Trailer | `DetailActionButton` | `VegafoXButton Ghost compact` |
| Bouton Lire | `DetailActionButton` | `VegafoXButton Secondary compact` |

### 8. JellyseerrCastRow.kt + JellyseerrPersonCard.kt

**CastRow :**
| Element | Avant | Apres |
|---|---|---|
| Titre section | `titleLarge textPrimary` | `BebasNeue 20sp, letterSpacing 2sp, TextPrimary` |
| Message vide | `bodyMedium textSecondary` | `14sp TextSecondary` |

**PersonCard :**
| Element | Avant | Apres |
|---|---|---|
| Layout | `TvFocusCard` 130dp wide, portrait 2:3 | Column 80dp wide, photo cercle 64dp |
| Photo | Rectangle pleine largeur | Cercle 64dp, `CircleShape` clip |
| Bordure photo | Aucune | 1dp blanc 10% -> 2dp OrangePrimary au focus |
| Focus | TvFocusCard (scale par defaut) | `scale 1.05 spring Medium` + bordure OrangePrimary |
| Nom | `labelMedium` | `13sp TextPrimary maxLines 1 center` |
| Role | `labelSmall onSurfaceVariant` | `12sp TextHint maxLines 1 center` |

---

## Testabilite

Pour tester les ecrans Jellyseerr, il faut que Jellyseerr soit configure dans **Parametres > VegafoX > Jellyseerr** avec :
- URL du serveur Jellyseerr
- Authentification

Les dialogs (Season, Quality, AdvancedOptions) se declenchent lors d'une demande de contenu sur MediaDetailsScreen.

Les RequestButtons apparaissent sur JellyseerrMediaDetailsScreen pour chaque film/serie.

Le CastRow et PersonCard apparaissent sur JellyseerrMediaDetailsScreen sous la section synopsis.

**Sans Jellyseerr configure**, seul l'ecran Discover (rows de tendances TMDB) est visible.

## Screenshots

Screenshot a prendre manuellement depuis l'ecran Jellyseerr (necessite Jellyseerr configure) :
- `docs/screenshots/jellyseerr_discover.png` — Splash screen capture (Jellyseerr non accessible au demarrage)

## Build

- Debug : OK
- Release : OK
- Installe sur AM9 Pro : OK
