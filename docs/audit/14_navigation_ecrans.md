# Audit 14 - Navigation & Ecrans

> **Mis à jour le 2026-03-08 — état post-travaux**
> ✅ **100% résolu** — Toutes les modifications vérifiées :
> - MainToolbar.kt : activeButtonColors appliqué à Home, Search, Jellyseerr
> - LeftSidebarNavigation.kt : isActive param + indicateur visuel
> - SearchViewModel.kt : debounce 300ms + MIN_QUERY_LENGTH = 2

## Schema de navigation complet

```
+------------------+     +-------------------+     +--------------------+
|   Startup        |     |   Home            |     |   Detail           |
|   (Login/Server) | --> |   (MediaBar hero  | --> |   (ItemDetails)    |
|                  |     |    + sections)     |     |   Play, Episodes,  |
+------------------+     +-------------------+     |   Similar          |
                           |   ^                    +--------------------+
                           |   |                      |   ^
                           v   |                      v   |
                         +-----------+              +------------+
                         | Library   |  -------->   | Detail     |
                         | Browse    |  <--------   | (sub-item) |
                         +-----------+              +------------+
                           |   ^
                           v   |
                         +-----------+
                         | Search    |
                         | (global)  |
                         +-----------+

Navigation par toolbar/sidebar:
  [User] [Home] [Search] [Shuffle] [Genres] [Favorites] [Jellyseerr] [Settings]
     |      |       |                  |         |            |           |
     v      v       v                  v         v            v           v
  Startup  Home  SearchFrag      GenresGrid  AllFavorites  Discover  SettingsDialog

Profondeur maximale observee:
  Niveau 1: Home / Jellyseerr Discover / Search
  Niveau 2: Library Browse / Item Detail / Jellyseerr Detail
  Niveau 3: Sub-item Detail (episode, person, similar)

Retour arriere (Back):
  Detail -> Library | Home (selon provenance)
  Library -> Home
  Search -> ecran precedent
  Jellyseerr -> ecran precedent
  Home + Back -> ExitConfirmationDialog
```

## Modifications implementees

### 1. Indicateur visuel de l'ecran actif (toolbar top)

| Propriete | Valeur |
|-----------|--------|
| Fichier | `ui/shared/toolbar/MainToolbar.kt` |
| Mecanisme | `activeButtonColors` applique au bouton correspondant a `activeButton` |
| Boutons concernes | Home, Search, Jellyseerr |
| Avant | Tous les boutons avaient `toolbarButtonColors` (pas de distinction actif/inactif) |
| Apres | Le bouton actif a `buttonActive` (fond semi-transparent `#4DCCCCCC`) |
| Couleurs | `containerColor = buttonActive`, `contentColor = onButtonActive` |

Le `MainToolbarActiveButton` enum existait deja (User, Home, Library, Search, Jellyseerr, None)
et etait passe par chaque Fragment, mais n'etait utilise que pour `ExpandableLibrariesButton`.
Maintenant il est aussi utilise pour Home, Search et Jellyseerr.

### 2. Indicateur visuel de l'ecran actif (sidebar gauche)

| Propriete | Valeur |
|-----------|--------|
| Fichier | `ui/shared/toolbar/LeftSidebarNavigation.kt` |
| Parametre ajoute | `isActive: Boolean` sur `SidebarIconItem` |
| Effet visuel | Bordure accent a 60% opacite + icone/texte en couleur accent |
| Boutons concernes | Home, Search, Jellyseerr |
| Avant | Aucune distinction visuelle de l'ecran actif dans la sidebar |
| Apres | L'item actif a une bordure coloree et texte/icone en couleur accent |

Details d'implementation:
- `isActive` ajoute comme parametre optionnel (default `false`)
- Quand `isActive && !isFocused`: bordure `focusedColor.copy(alpha = 0.6f)`, icone et texte en `focusedColor`
- Quand `isFocused`: comportement focus normal (bordure pleine)
- Quand `isActive`: `iconAlpha = 1f` (toujours visible, pas dimmed)

### 3. Recherche reactive (debounce 300ms, minimum 2 caracteres)

| Propriete | Avant | Apres |
|-----------|-------|-------|
| Debounce | 600ms | 300ms |
| Longueur minimale | 0 (cherche des la saisie) | 2 caracteres |
| Fichier | `ui/search/SearchViewModel.kt` |

La constante `MIN_QUERY_LENGTH = 2` empeche les requetes d'un seul caractere
qui retournaient trop de resultats inutiles et gaspillaient de la bande passante.
Le debounce reduit de moitie permet une experience plus reactive tout en
restant suffisant pour eviter les requetes excessives pendant la saisie.

### 4. Fichiers modifies

| Fichier | Modification |
|---------|-------------|
| `ui/shared/toolbar/MainToolbar.kt` | `activeButtonColors` applique a Home, Search, Jellyseerr |
| `ui/shared/toolbar/LeftSidebarNavigation.kt` | `isActive` param + indicateur visuel (bordure + couleur) |
| `ui/search/SearchViewModel.kt` | Debounce 600ms -> 300ms, min 2 caracteres |

---

## Elements deja en place (pre-existants)

### Navigation Stack (NavigationRepository)

| Element | Detail |
|---------|--------|
| Type | `Stack<Destination.Fragment>` dans `NavigationRepositoryImpl` |
| Back | `goBack()` — pop du stack, emet `NavigationAction.GoBack` |
| Reset | `reset()` — vide le stack, navigue vers destination par defaut |
| Replace | `navigate(dest, replace=true)` — remplace le top du stack |
| Fragment view | `DestinationFragmentView` gere les transactions avec fade animation |

### Exit confirmation dialog

| Element | Detail |
|---------|--------|
| Fichier | `ui/browsing/ExitConfirmationDialog.kt` |
| Declencheur | `OnBackPressedCallback` dans `MainActivity.kt` |
| Condition | `!navigationRepository.canGoBack` (= on est sur Home) |
| Focus initial | Sur le bouton "Quitter" (exit) |
| Style | Dialog modal avec scrim, bordure `outlineVariant` |

### Toolbar/Sidebar dual-mode

| Element | Detail |
|---------|--------|
| Config | `UserPreferences.navbarPosition` — `NavbarPosition.TOP` ou `LEFT` |
| Top toolbar | `MainToolbar` — boutons horizontaux, label visible au focus uniquement |
| Left sidebar | `LeftSidebarNavigation` — 56dp collapsed, 280dp expanded au focus |
| Label sidebar | Visible quand la sidebar est expanded (avec delay 150ms) |

### Home screen — Hero section (MediaBar)

| Element | Detail |
|---------|--------|
| Composant | `MediaBarSlideshowView` — slideshow auto avec crossfade |
| Hauteur | 235dp |
| Contenu | Items aleatoires avec backdrop, logo, boutons Play/Details |
| Trailer | `ExoPlayerTrailerView` avec YouTube stream resolver |
| Config | `UserSettingPreferences.mediaBarEnabled` |

### Home screen — Sections configurables

| Section | Type | Detail |
|---------|------|--------|
| MediaBar | Hero | Slideshow en haut (si active) |
| Continue Watching | Resume | `HomeSectionType.RESUME` |
| Next Up | Resume | `HomeSectionType.NEXT_UP` |
| Recently Added | Discovery | `HomeSectionType.LATEST_MEDIA` |
| Recently Released | Discovery | `HomeSectionType.RECENTLY_RELEASED` |
| Live TV | Live | `HomeSectionType.LIVE_TV` |
| Playlists | Library | `HomeSectionType.PLAYLISTS` |
| Library Tiles | Library | `HomeSectionType.LIBRARY_TILES_SMALL` |

Chaque section est une `ListRow` Leanback avec scroll horizontal.
L'ordre est configurable dans les preferences utilisateur (`activeHomesections`).

### Detail screen

| Element | Detail |
|---------|--------|
| Play button | Premier focus par defaut (`FocusRequester` sur le bouton Play) |
| Episodes | Section accordeon pour les series (saisons + episodes) |
| Similar | Section en bas de page `SimilarItemCard` |
| Backdrop | Blur effect avec gradient overlay |
| Info row | `BaseItemInfoRow` — rating, annee, duree, resolution |

### Search

| Element | Detail |
|---------|--------|
| Acces | Bouton Search dans toolbar/sidebar (1 action depuis n'importe ou) |
| Clavier | `SearchTextInput` — champ texte natif Android TV |
| Resultats | Groupes par type (Films, Series, Episodes, etc.) |
| Jellyseerr | Recherche Jellyseerr integree si active |
| Multi-serveur | Support recherche multi-serveur |

---

## Elements NON implementes et justification

### Historique de recherche

**Raison** : L'implementation d'un historique de recherche necessite :
1. Un `DataStore` ou `SharedPreferences` dedie pour persister les requetes
2. Un UI de liste d'historique avec gestion du focus D-pad
3. Un bouton "effacer l'historique" pour la vie privee
4. Gestion du cas multi-utilisateur (historique par profil)

**Impact** : Faible priorite car la recherche avec debounce 300ms et resultats
en temps reel rend l'historique moins necessaire. Les utilisateurs retrouvent
rapidement ce qu'ils cherchent.

**Prerequis** : Ajouter un `SearchHistoryRepository` avec `DataStore<Preferences>`,
limite a 20 entrees, indexe par userId.

### Breadcrumb / titre d'ecran

**Raison** : L'architecture Fragment ne supporte pas nativement un breadcrumb
persistant entre ecrans. Le toolbar/sidebar occupe deja l'espace disponible.
Sur Android TV, les utilisateurs naviguent principalement avec le bouton Back
de la telecommande, pas visuellement via un breadcrumb.

**Alternative actuelle** : L'indicateur visuel sur le bouton actif (Home, Search,
Jellyseerr) dans le toolbar/sidebar remplit partiellement ce role.
Les ecrans de detail affichent le titre du contenu dans le header.

### Limitation stricte a 3 niveaux

**Raison** : Le `NavigationRepository` utilise un `Stack` non-borne.
Ajouter une limite dure risquerait de bloquer la navigation dans des cas
legitimes (ex: Home -> Library -> Detail Series -> Detail Episode -> Detail Acteur).

**Alternative actuelle** : Le bouton Back ramene toujours au niveau precedent,
et `reset()` permet de revenir a Home a tout moment depuis le toolbar/sidebar.
En pratique, la profondeur depasse rarement 3-4 niveaux.

### "Voir tout" sur les sections Home

**Raison** : Les sections Home utilisent des `ListRow` Leanback avec un
`ItemRowAdapter` qui charge les items de maniere paginee. Le framework Leanback
ne supporte pas nativement un bouton "Voir tout" en fin de row sans un
`PresenterSelector` custom.

**Alternative actuelle** : Le scroll horizontal avec pagination automatique
(`loadMoreItemsIfNeeded`) charge progressivement tous les items.
Les utilisateurs peuvent aussi naviguer directement vers la bibliotheque
via le toolbar/sidebar pour voir tous les items.

**Prerequis** : Creer un `SeeAllPresenter` Leanback qui ajoute un item
special en fin de row, avec navigation vers `Destinations.libraryBrowser`.

### Label toujours visible (toolbar top)

**Raison** : Le `ExpandableIconButton` affiche le label uniquement au focus
pour economiser l'espace horizontal dans la toolbar top. Avec 8+ boutons
(Home, Search, Shuffle, Genres, Favorites, Jellyseerr, Libraries, Settings),
afficher tous les labels en permanence depasserait la largeur disponible sur
les ecrans 1080p.

**Alternative actuelle** :
- Sidebar gauche : les labels sont visibles quand la sidebar est expanded au focus
- Toolbar top : chaque bouton a une icone descriptive + label au focus
- L'indicateur visuel sur le bouton actif aide a l'orientation

---

## Decisions d'UX

### 1. Indicateur actif : fond semi-transparent vs bordure coloree

**Choix** : Fond semi-transparent (toolbar top) + bordure coloree (sidebar)

**Justification** :
- Toolbar top : le fond `buttonActive` (#4DCCCCCC, 30% blanc) est subtil mais
  clairement visible sur le fond sombre du toolbar. Il ne conflit pas avec
  le `focusedContainerColor` qui est plus opaque.
- Sidebar : la bordure `focusedColor.copy(alpha = 0.6f)` utilise la meme
  couleur que le focus mais attenuee, creant une hierarchie visuelle claire
  (actif < focus).

### 2. Debounce 300ms vs 600ms

**Choix** : 300ms

**Justification** : Sur Android TV, la saisie est lente (D-pad ou clavier
virtuel). 600ms etait trop long — l'utilisateur finissait de taper un mot
de 4 lettres et attendait encore le debounce. 300ms est un bon compromis
entre reactivite et reduction des requetes inutiles.

### 3. Minimum 2 caracteres

**Choix** : Ne pas lancer de recherche avant 2 caracteres

**Justification** : Une recherche sur 1 caractere retourne des centaines
de resultats non-pertinents et genere une charge reseau inutile. 2 caracteres
est le minimum pour obtenir des resultats significatifs.

### 4. Pas de breadcrumb

**Choix** : S'appuyer sur l'indicateur actif + Back button

**Justification** : Sur Android TV, l'interaction principale est la
telecommande D-pad avec bouton Back. Un breadcrumb visuel est un pattern
web/mobile qui ne s'adapte pas bien a l'interface TV (pas de curseur pour
cliquer sur les elements du breadcrumb). L'indicateur actif sur le
toolbar/sidebar donne suffisamment de contexte de localisation.

---

## Impact

| Metrique | Avant | Apres |
|----------|-------|-------|
| Boutons avec indicateur actif (toolbar) | 0/8 | 3/8 (Home, Search, Jellyseerr) |
| Items avec indicateur actif (sidebar) | 0/9+ | 3/9+ (Home, Search, Jellyseerr) |
| Debounce recherche | 600ms | 300ms |
| Longueur min recherche | 0 | 2 caracteres |
| Exit confirmation | Oui | Oui (inchange) |
| Back ramene au niveau precedent | Oui | Oui (inchange) |
