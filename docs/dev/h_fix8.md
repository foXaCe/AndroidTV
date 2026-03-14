# H_FIX8 — Cards image-only (suppression titre + annee)

**Date** : 2026-03-11

---

## 1. Contexte

Style Netflix/Apple TV : les cards ne montrent QUE l'image.
Le titre et l'annee s'affichent uniquement dans le hero overlay quand la card est focalisee.

---

## 2. Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

---

## 3. Elements supprimes

| Element | Lignes (avant) | Description |
|---------|----------------|-------------|
| `Column { ... }` | wrapper complet | Conteneur qui empilait image + textes |
| `Spacer(6.dp)` | entre image et titre | Espacement reserve pour le texte |
| `Text(item.name)` | sous l'image | Nom du media, 1 ligne, ellipsis |
| `Text(year)` | sous le nom | Annee de production conditionnelle |

### Imports supprimes

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextOverflow
```

---

## 4. Structure avant / apres

### Avant

```
TvFocusCard {
    Column {
        Box (image 16:9) {
            AsyncImage
            Progress bar
            Badge NEW
        }
        Spacer(6.dp)
        Text(item.name)         // titre
        Text(year)              // annee
    }
}
```

### Apres

```
TvFocusCard {
    Box (image 16:9) {
        AsyncImage
        Progress bar
        Badge NEW
    }
}
```

---

## 5. Impact sur la hauteur

| Propriete | Avant | Apres |
|-----------|-------|-------|
| Hauteur image | 124dp (220 * 9/16) | 124dp (inchange) |
| Spacer | 6dp | 0 |
| Titre | ~18dp (bodySmall, 1 ligne) | 0 |
| Annee | ~14dp (labelSmall, 1 ligne) | 0 |
| **Hauteur totale card** | **~162dp** | **124dp** |

Gain : ~38dp par card, la card est maintenant 100% image + indicateurs visuels.

---

## 6. Elements conserves

- AsyncImage en 16:9 (220dp x 124dp)
- Badge "NEW" en TopEnd (items non vus)
- Barre de progression en BottomCenter (playedPercentage > 0)
- Bordure orange au focus (geree par TvFocusCard)
- Coins arrondis 10dp
- Fallback icon (ic_movie) si pas d'image

---

## 7. KDoc mis a jour

```kotlin
// Avant
* Uses [TvFocusCard] for D-pad focus handling and displays a poster image with title.

// Apres
* Uses [TvFocusCard] for D-pad focus handling and displays a 16:9 landscape image only.
```

---

## 8. Callers (aucun changement requis)

Tous les callers utilisaient deja BrowseMediaCard sans acceder au titre/annee dans la card elle-meme. Le titre est affiche via le hero overlay au focus.

| Fichier | Usage |
|---------|-------|
| `HomeScreen.kt` | `BrowseMediaCard(item, api, onFocus, onClick)` |
| `SearchComposeFragment.kt` | `BrowseMediaCard(item, api, onClick, modifier)` |
| `ByLetterBrowseFragment.kt` | `BrowseMediaCard(item, api, onClick)` |
| `FolderBrowseComposeFragment.kt` | `BrowseMediaCard(item, api, onClick)` |
| `SuggestedMoviesFragment.kt` | `BrowseMediaCard(item, api, onClick)` |
| `CollectionBrowseFragment.kt` | `BrowseMediaCard(item, api, onClick)` |
| `GenresGridComposeFragment.kt` | `BrowseMediaCard(item, api, onClick)` |
| `LibraryBrowseComposeFragment.kt` | `BrowseMediaCard(item, api, onClick)` |

---

## 9. Build

```
./gradlew :app:compileGithubDebugKotlin   -> BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin -> BUILD SUCCESSFUL
```
