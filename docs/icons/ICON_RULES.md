# Regles d'utilisation des icones — VegafoX

> Ce fichier fait autorite pour tous les futurs developpements de VegafoX.
> Derniere mise a jour : 2026-03-11

---

## Regle #1 — Toujours passer par VegafoXIcons

Dans tout composable Compose, ne JAMAIS utiliser directement :

- `Icons.Default.X`
- `Icons.Rounded.X`
- `Icons.Outlined.X`
- `painterResource(R.drawable.ic_X)` pour une icone de navigation/action

Toujours utiliser : `VegafoXIcons.X`

Fichier source : `app/src/main/java/org/jellyfin/androidtv/ui/base/icons/VegafoXIcons.kt`

---

## Regle #2 — Ajouter dans VegafoXIcons

Si une nouvelle icone est necessaire et n'existe pas encore dans VegafoXIcons :

1. L'ajouter dans `VegafoXIcons.kt`
2. Choisir l'icone sur https://fonts.google.com/icons?icon.set=Material+Symbols
3. Utiliser le style **Filled** en priorite (`Icons.Default.*` = `Icons.Filled.*`)
4. Nommer la propriete de facon **fonctionnelle** (pas le nom technique Material)
   - `VegafoXIcons.Play` plutot que `VegafoXIcons.PlayArrow`
   - `VegafoXIcons.Rewind` plutot que `VegafoXIcons.FastRewind`
   - `VegafoXIcons.Genres` plutot que `VegafoXIcons.TheaterComedy`

---

## Regle #3 — Drawables XML custom uniquement

Les fichiers `ic_*.xml` / `ic_*.png` dans `res/drawable/` sont RESERVES aux cas suivants :

- **Logos custom** : ic_vegafox, ic_vegafox_fox, ic_jellyfin, ic_jellyseerr_jellyfish, ic_seer
- **Icones specifiques** : ic_rt_fresh, ic_rt_rotten (Rotten Tomatoes)
- **Code View legacy** (pas Compose) : ic_play, ic_add, ic_shuffle, ic_heart, etc.
- **Layouts XML** : ic_arrow_back, ic_house, ic_down, ic_up, etc.
- **Icones animees** (Animated Vector Drawable)

Tout le reste passe par `VegafoXIcons`.

---

## Regle #4 — Pas de nouvelle dependance icone

Ne pas ajouter de nouvelle lib d'icones.
`VegafoXIcons` via `material-icons-extended:1.7.8` est l'unique source de verite.

Si une icone manque dans Material Symbols :
1. Creer un drawable XML custom nomme `ic_custom_[nom].xml`
2. L'ajouter dans `VegafoXIcons` via `ImageVector.vectorResource(R.drawable.ic_custom_nom)`

---

## Regle #5 — Review checklist

Avant chaque PR touchant l'UI :

- [ ] Aucun `Icons.Default/Rounded/Outlined` direct dans le code Compose (sauf VegafoXIcons.kt)
- [ ] Aucun `painterResource(R.drawable.ic_X)` dans le code Compose sauf logos custom
- [ ] Tout nouvel icone ajoute a `VegafoXIcons.kt`
- [ ] Drawable XML custom uniquement si justification documentee
- [ ] 0 import `androidx.compose.material.icons` en dehors de VegafoXIcons.kt

---

## Reference rapide — 20 icones les plus utilisees

| VegafoXIcons | Material Symbol | Usage principal |
|-------------|-----------------|-----------------|
| `Play` | `PlayArrow` | Lecture media |
| `Favorite` | `Favorite` | Favoris / coeur |
| `Home` | `Home` | Accueil / navigation |
| `Pause` | `Pause` | Pause lecture |
| `Settings` | `Settings` | Parametres |
| `Check` | `Check` | Validation / selection |
| `Search` | `Search` | Recherche |
| `SkipNext` | `SkipNext` | Piste suivante |
| `SkipPrevious` | `SkipPrevious` | Piste precedente |
| `FastForward` | `FastForward` | Avance rapide |
| `Rewind` | `FastRewind` | Retour rapide |
| `Shuffle` | `Shuffle` | Lecture aleatoire |
| `Record` | `FiberManualRecord` | Enregistrement live TV |
| `RecordSeries` | `FiberSmartRecord` | Enregistrement serie |
| `Tv` | `Tv` | Chaines TV |
| `LiveTv` | `LiveTv` | TV en direct / guide |
| `Visibility` | `Visibility` | Marquer comme vu |
| `Delete` | `Delete` | Suppression |
| `Filter` | `FilterList` | Filtre / tri |
| `Folder` | `Folder` | Dossier / bibliotheque |

---

## Drawables conserves — reference complete

### Logos custom (6 XML + 1 PNG)

| Drawable | Usage |
|----------|-------|
| `ic_vegafox` | Logo principal VegafoX (settings, toolbar, layouts XML) |
| `ic_vegafox_fox.png` | Renard anime (MainToolbar, VegafoXFoxLogo) |
| `ic_jellyfin` | Logo Jellyfin (settings, notification) |
| `ic_jellyseerr_jellyfish` | Logo Jellyseerr (settings, sidebar) |
| `ic_seer` | Logo Overseerr (sidebar) |
| `ic_rt_fresh` | Tomate fraiche Rotten Tomatoes |
| `ic_rt_rotten` | Tomate pourrie Rotten Tomatoes |

### Code View legacy (9 XML)

| Drawable | Usage View |
|----------|-----------|
| `ic_play` | TextUnderButton, ItemRowView.setBackgroundResource |
| `ic_add` | TextUnderButton (ItemListFragment) |
| `ic_shuffle` | TextUnderButton + clock_user_bug.xml |
| `ic_heart` | TextUnderButton (ItemListFragment) |
| `ic_mix` | TextUnderButton (ItemListFragment) |
| `ic_user` | TextUnderButton (ItemListFragment) |
| `ic_album` | NowPlayingView placeholder, poster |
| `ic_check` | SettingsFragment.setImageResource |
| `ic_star` | RatingIcon.LocalDrawable |

### Jellyseerr RequestsAdapter (5 XML)

| Drawable | Usage |
|----------|-------|
| `ic_available` | Statut disponible |
| `ic_partially_available` | Statut partiellement disponible |
| `ic_declined` | Statut refuse |
| `ic_pending` | Statut en attente |
| `ic_indigo_spinner` | Chargement |

### Layouts XML (8 XML)

| Drawable | Layout |
|----------|--------|
| `ic_arrow_back` | fragment_user_login.xml |
| `ic_pencil` | fragment_server.xml |
| `ic_user_add` | fragment_server.xml |
| `ic_house` | fragment_server.xml, clock_user_bug.xml |
| `ic_down` | item_row.xml, genres_grid_browse.xml |
| `ic_up` | item_row.xml |
| `ic_folder` | genres_grid_browse.xml |
| `ic_control_select` | popup_expandable_text_view.xml |
| `ic_tv` | tile_land_tv.xml (LeanbackChannelWorker) |
