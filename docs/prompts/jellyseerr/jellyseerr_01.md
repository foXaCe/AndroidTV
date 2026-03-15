# Jellyseerr — Migration VegafoX Premium

## Fichiers dans ui/jellyseerr/

### Fragments
| Fichier | Rôle |
|---|---|
| `JellyseerrBrowseByFragment.kt` | Grille filtrée par genre/network/studio/keyword |
| `MediaDetailsFragment.kt` | Détails film/série + demande contenu |
| `PersonDetailsFragment.kt` | Fiche personne (acteur/réalisateur) |
| `DiscoverFragment.kt` | Page découverte principale |
| `JellyseerrDiscoverRowsFragment.kt` | Rows de découverte (recommandations) |
| `RequestsFragment.kt` | Liste des demandes utilisateur |
| `SettingsFragment.kt` | Paramètres Jellyseerr |

### Composables (compose/)
| Fichier | Rôle |
|---|---|
| `JellyseerrBrowseByScreen.kt` | Écran grille avec sort/filter toolbar |
| `JellyseerrMediaDetailsScreen.kt` | Écran détails média (backdrop + poster + overview) |
| `JellyseerrPersonDetailsScreen.kt` | Écran fiche personne |
| `JellyseerrCards.kt` | PosterCard, GenreCard, LogoCard |
| `JellyseerrStatusBadge.kt` | Badge pill statut requête |
| `JellyseerrRequestButtons.kt` | Boutons d'action (Demander, Annuler, Trailer, Lire) |
| `JellyseerrCastRow.kt` | LazyRow d'acteurs |
| `JellyseerrPersonCard.kt` | Card portrait acteur |
| `JellyseerrFactsSection.kt` | Tableau de faits (score, runtime, budget, etc.) |
| `JellyseerrDiscoverRows.kt` | Rows de découverte composables |
| `SeasonSelectionDialog.kt` | Dialog sélection saisons |
| `AdvancedRequestOptionsDialog.kt` | Dialog options avancées demande |
| `QualitySelectionDialog.kt` | Dialog sélection qualité HD/4K |

### ViewModels
| Fichier | Rôle |
|---|---|
| `JellyseerrDetailsViewModel.kt` | Détails, requêtes, annulation, serveurs |
| `JellyseerrDiscoverViewModel.kt` | Discover TMDB, genres, search |
| `JellyseerrAuthViewModel.kt` | Authentification Jellyseerr |

### Relation entre les 3 écrans migrés
```
BrowseByScreen
  └→ clic poster card → MediaDetailsScreen
                           └→ clic cast member → PersonDetailsScreen
                           └→ clic recommendation → MediaDetailsScreen (récursif)
```

---

## Avant / Après par écran

### 1. JellyseerrBrowseByScreen

| Élément | Avant | Après |
|---|---|---|
| Fond | `JellyfinTheme.colorScheme.background` (0xFF0A0A0F) | `VegafoXColors.BackgroundDeep` (0xFF07070B) |
| Titre header | `JellyfinTheme.typography.titleLarge` (font système) | `BebasNeue 32sp, letterSpacing 2sp` |
| Couleur titre | `JellyfinTheme.colorScheme.textPrimary` | `VegafoXColors.TextPrimary` direct |
| Menu déroulants | Fond Material3 par défaut (gris) | `VegafoXColors.SurfaceBright` (0xFF1C1C22) |
| Cards poster | Pas de badge statut | Badge overlay bas-droite coloré par statut |

### 2. JellyseerrMediaDetailsScreen

| Élément | Avant | Après |
|---|---|---|
| Fond principal | `JellyfinTheme.colorScheme.background` | `VegafoXColors.BackgroundDeep` |
| Backdrop | Pleine opacité + scrim noir 67% | Alpha 0.25 + gradient horizontal BackgroundDeep 97%→transparent |
| Gradient bas | `JellyfinTheme.colorScheme.background` | `VegafoXColors.BackgroundDeep` |
| Titre | `headlineMedium` (font système) | `BebasNeue 52sp, letterSpacing 2sp` |
| Couleur titre | `JellyfinTheme.colorScheme.textPrimary` | `VegafoXColors.TextPrimary` direct |
| Synopsis | Pas de maxLines | `maxLines = 4`, couleur `VegafoXColors.TextSecondary` |
| StatusBadge | Fond opaque uni, texte blanc | Fond alpha 20% de la couleur statut, texte coloré |

### 3. JellyseerrPersonDetailsScreen

| Élément | Avant | Après |
|---|---|---|
| Fond | `JellyfinTheme.colorScheme.background` | `VegafoXColors.BackgroundDeep` |
| Photo | Cercle 120dp sans bordure | Cercle 120dp + bordure 2dp `VegafoXColors.OrangePrimary` |
| Nom | `headlineMedium` (font système) | `BebasNeue 36sp, letterSpacing 2sp` |
| Couleur nom | `JellyfinTheme.colorScheme.textPrimary` | `VegafoXColors.TextPrimary` direct |
| Info naissance | `JellyfinTheme.colorScheme.textSecondary` | `VegafoXColors.TextSecondary` direct |
| Biographie | `textPrimary`, maxLines 4 | `VegafoXColors.TextSecondary`, maxLines 6 |

---

## Badges statut requête (JellyseerrPosterCard)

| Statut | Fond | Texte |
|---|---|---|
| Available (4, 5) | `Success.copy(alpha=0.20)` vert translucide | `VegafoXColors.Success` vert |
| Pending (2) | `OrangePrimary.copy(alpha=0.20)` orange translucide | `VegafoXColors.OrangePrimary` |
| Processing (3) | `OrangePrimary.copy(alpha=0.20)` | `VegafoXColors.OrangePrimary` |
| Declined | `Error.copy(alpha=0.20)` rouge translucide | `VegafoXColors.Error` rouge |
| Blacklisted (6) | `Error.copy(alpha=0.20)` | `VegafoXColors.Error` |

## StatusBadge détail (JellyseerrStatusBadge)

Changement : le badge utilise maintenant la couleur du statut comme texte, et le fond est la même couleur à 20% d'opacité (au lieu d'un fond opaque avec texte blanc).

---

## Testabilité

Pour tester ces écrans, Jellyseerr doit être configuré dans **Paramètres > VegafoX > Jellyseerr** avec :
- URL du serveur Jellyseerr
- Authentification (login Jellyseerr ou Jellyfin)

Sans configuration, l'icône Jellyseerr dans la sidebar mène aux écrans Discover/Browse qui affichent des données TMDB via Jellyseerr API.

## Fichiers modifiés

1. `ui/jellyseerr/compose/JellyseerrCards.kt` — Badge statut overlay + imports VegafoX
2. `ui/jellyseerr/compose/JellyseerrBrowseByScreen.kt` — BackgroundDeep, BebasNeue header, dropdown styling
3. `ui/jellyseerr/compose/JellyseerrMediaDetailsScreen.kt` — Backdrop alpha+gradient, BebasNeue titre, synopsis maxLines
4. `ui/jellyseerr/compose/JellyseerrStatusBadge.kt` — Fond alpha translucide, texte coloré par statut
5. `ui/jellyseerr/compose/JellyseerrPersonDetailsScreen.kt` — BackgroundDeep, photo bordure OrangePrimary, BebasNeue nom

## Build

- Debug : OK
- Release : OK
- Installé sur AM9 Pro : OK
