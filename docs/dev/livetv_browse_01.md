# Live TV Browse — Migration VegafoX

**Date** : 2026-03-12
**Branche** : main

---

## Objectif

Remplacer l'ecran d'entree Live TV (`LiveTvBrowseFragment`) — anciennement un ecran complexe avec rows de contenu (On Now, Coming Up, Channels, Recordings) et boutons de navigation — par un ecran simple a 4 tuiles de navigation style VegafoX.

---

## Structure AVANT

```
LiveTvBrowseFragment (ui/browsing/v2/)
├── ViewModel: LiveTvBrowseViewModel
│   └── Charge: onNow, comingUp, favoriteChannels, otherChannels,
│       recentRecordings, scheduledNext24h, pastDay, pastWeek
├── Injections: viewModel, navigationRepository, backgroundService,
│   itemLauncher, userRepository
├── ComposeView → JellyfinTheme → ScreenIdOverlay
│   └── LiveTvBrowseContent()
│       ├── AppBackground() + overlay semi-transparent
│       ├── LiveTvHeader (titre + FocusedItemHud + bouton Home)
│       ├── StateContainer (loading/error/content)
│       │   └── LiveTvRows
│       │       ├── LiveTvViewsRow (4 boutons nav: Guide, Recordings, Schedule, Series)
│       │       ├── LiveTvItemRow "On Now" (LazyRow de LiveTvCard)
│       │       ├── LiveTvItemRow "Coming Up"
│       │       ├── LiveTvItemRow "Favorite Channels"
│       │       ├── LiveTvItemRow "Other Channels"
│       │       ├── LiveTvItemRow "Recent Recordings"
│       │       ├── LiveTvItemRow "Past 24h"
│       │       ├── LiveTvItemRow "Past Week"
│       │       └── LiveTvItemRow "Scheduled Next 24h"
│       └── LibraryStatusBar
└── ~580 lignes de code
```

**Design** : JellyfinTheme.colorScheme (bridge), AppBackground floue, pas de VegafoXColors directs.

---

## Structure APRES

```
LiveTvBrowseFragment (ui/browsing/v2/)
├── Injections: navigationRepository, userRepository
├── ComposeView → JellyfinTheme → ScreenIdOverlay
│   └── LiveTvBrowseScreen (ui/livetv/compose/)
│       ├── Fond: VegafoXColors.BackgroundDeep
│       ├── Header: "TV en direct" BebasNeue 40sp + "Choisir une section" 14sp
│       └── 4 tuiles verticales:
│           ├── Guide (VegafoXIcons.Schedule → Destinations.liveTvGuide)
│           ├── Enregistrements (VegafoXIcons.Trailer → Destinations.liveTvRecordings)
│           ├── Programmation (VegafoXIcons.Calendar → Destinations.liveTvSchedule)*
│           └── Series (VegafoXIcons.VideoLibrary → Destinations.liveTvSeriesRecordings)*
└── ~70 lignes de code

* Tuiles 3 et 4 conditionnelles: canManageRecordings
```

---

## Fichiers modifies

| Fichier | Action | Details |
|---------|--------|---------|
| `ui/livetv/compose/LiveTvBrowseScreen.kt` | **Cree** | Nouveau composable: ecran 4 tuiles |
| `ui/browsing/v2/LiveTvBrowseFragment.kt` | **Reecrit** | Simplifie: ~580 → ~70 lignes |
| `res/values/strings.xml` | Modifie | +3 strings: `lbl_choose_section`, `lbl_epg_grid`, `lbl_your_recordings` |
| `res/values-fr/strings.xml` | Modifie | +3 traductions FR correspondantes |

---

## Design des tuiles

Chaque tuile:
- **Fond** : `VegafoXColors.Surface` (#141418) — coins 16dp
- **Hauteur** : 120dp, pleine largeur
- **Layout** : Row — icone 48dp OrangePrimary a gauche + colonne titre/sous-titre
- **Titre** : BebasNeue 24sp TextPrimary
- **Sous-titre** : 13sp TextSecondary
- **Padding** : 24dp interne
- **Separateur** : 1dp blanc alpha 6% entre tuiles

**Focus D-pad** :
- Bordure 2dp OrangePrimary
- Fond OrangePrimary alpha 8%
- Scale 1.02 spring Medium
- Glow radial OrangePrimary alpha 20%

---

## Navigation preservee

| Tuile | Destination | Fragment cible |
|-------|-------------|----------------|
| Guide | `Destinations.liveTvGuide` | `LiveTvGuideFragment` |
| Enregistrements | `Destinations.liveTvRecordings` | `RecordingsBrowseFragment` |
| Programmation | `Destinations.liveTvSchedule` | `ScheduleBrowseFragment` |
| Series | `Destinations.liveTvSeriesRecordings` | `SeriesRecordingsBrowseFragment` |

Les tuiles Programmation et Series ne s'affichent que si `enableLiveTvManagement == true` (comportement identique a l'ancien ecran).

---

## Strings ajoutees

| Cle | EN | FR |
|-----|----|----|
| `lbl_choose_section` | Choose a section | Choisir une section |
| `lbl_epg_grid` | EPG grid | Grille EPG |
| `lbl_your_recordings` | Your recordings | Vos enregistrements |

Strings existantes reutilisees: `pref_live_tv_cat`, `lbl_live_tv_guide`, `lbl_recorded_tv`, `lbl_schedule`, `lbl_coming_up`, `lbl_series`, `lbl_series_recordings`.

---

## Build

| Variante | Statut |
|----------|--------|
| Debug (github + playstore) | OK |
| Release (github + playstore) | OK |
| Install AM9 Pro (debug github) | OK |

---

## Notes

- Le `LiveTvBrowseViewModel` reste declare dans Koin (`AppModule.kt:223`) mais n'est plus instancie par le fragment. Il pourra etre supprime dans un nettoyage futur si aucun autre fragment ne l'utilise.
- Les composables partages (`LibraryStatusBar`, `FocusedItemHud`, `LibraryToolbarButton`, `LiveTvCard`) restent dans `LibraryBrowseComponents.kt` pour les autres fragments V2.
- L'`Args` data class est preservee pour compatibilite avec `Destinations.liveTvBrowser()`.
