# H2 — Hero Info Overlay

**Date** : 2026-03-10
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Résumé

Ajout d'un overlay d'information animé dans la zone hero de l'écran d'accueil.
Quand l'utilisateur navigue avec le D-pad, les métadonnées de l'item focusé
s'affichent en bas à gauche avec une animation fadeIn + slideInVertically.
Des dots de pagination apparaissent en bas à droite.

---

## Fichiers modifiés

| Fichier | Changement |
|---------|------------|
| `app/.../ui/home/compose/HomeHeroBackdrop.kt` | Ajout `HeroInfoOverlay`, `HeroPaginationDots`, gradient horizontal dynamique |
| `app/.../ui/home/compose/HomeScreen.kt` | Restructuration layout : zone hero (weight 0.45) + rows (weight 0.55) |
| `app/src/main/res/values/strings.xml` | Ajout `lbl_my_list` (session précédente) |
| `app/src/main/res/values-fr/strings.xml` | Ajout `lbl_my_list` (session précédente) |

---

## Nouveaux composables

### `HeroInfoOverlay(item, onPlayClick, modifier)`

Bloc d'information animé sur l'item focusé. Contenu :

1. **Tag line** : type (FILM/SÉRIE) + premier genre, séparés par `•`, en uppercase `OrangePrimary` 11sp avec ligne 20dp
2. **Titre** : `item.name`, 32sp W800, `TextPrimary`, max 2 lignes
3. **Pills row** : année, durée (via `formatDuration`), note communautaire (★ highlight), rating officiel, résolution
4. **Description** : `item.overview`, 14sp W300 `TextSecondary`, max 2 lignes
5. **Boutons d'action** :
   - `VegafoXButton Primary` : "▶ Lecture" ou "▶ Reprendre" (si playbackPositionTicks > 0)
   - `VegafoXButton Ghost` : "+ Ma liste"

Animation :
- Enter : `fadeIn(300ms, EaseOutCubic)` + `slideInVertically(+20dp, 300ms, EaseOutCubic)`
- Exit : `fadeOut(150ms)`
- Clé de transition : `item.id`

### `HeroPaginationDots(totalItems, activeIndex, modifier, maxDots)`

Points de pagination en bas à droite de la zone hero :
- Dot actif : 20×6dp, `RoundedCornerShape(3.dp)`, `OrangePrimary`
- Dot inactif : 6×6dp, `CircleShape`, `Surface * 0.3`
- Max 10 dots affichés
- Masqué si `totalItems <= 1`

### Gradient horizontal dynamique

`animateFloatAsState` sur l'alpha du gradient horizontal :
- Sans item : alpha `0.5`
- Avec item focusé : alpha `0.85`
- Transition : 400ms `EaseOutCubic`

---

## Layout HomeScreen (restructuré)

```
Box (fillMaxSize) {
    HomeHeroBackdrop(focusedItem)     // z=0 : backdrop plein écran
    Column (fillMaxSize) {
        MainToolbar(Home)             // barre de navigation
        Box(weight=0.45f) {          // zone hero
            HeroInfoOverlay(...)      // align = BottomStart + overscan padding
            HeroPaginationDots(...)   // align = BottomEnd + overscan padding
        }
        StateContainer(weight=0.55f)  // rows de contenu
    }
}
```

---

## Pills — Logique de résolution

| Largeur vidéo | Label |
|---------------|-------|
| ≥ 3800 | 4K |
| ≥ 1900 | 1080p |
| ≥ 1260 | 720p |
| ≥ 700 | 480p |

Source : `item.mediaSources?.firstOrNull()?.mediaStreams` → premier stream vidéo.

---

## Tag line — Logique par type

| Type | Format |
|------|--------|
| MOVIE | `FILM  •  ACTION` |
| SERIES | `SÉRIE  •  DRAME` |
| EPISODE | `NOM SÉRIE  •  S1E3` |
| Autre | Premier genre seul |

---

## Strings utilisées

| Clé | EN | FR |
|-----|----|-----|
| `lbl_play` | Play | Lecture |
| `lbl_resume` | Resume | Reprendre |
| `lbl_my_list` | My list | Ma liste |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```

0 erreur, 0 warning.
