# H3 — Cards : barre de progression, badge NEW, strings i18n

**Date** : 2026-03-10

---

## 1. Barre de progression (Continue Watching)

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Implementation

Ajout dans le `Box` poster (apres l'image, avant la fermeture) d'une barre de progression conditionnelle :

```kotlin
val playedPercentage = item.userData?.playedPercentage
if (playedPercentage != null && playedPercentage > 0) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0x1AFFFFFF)),  // rgba(255,255,255,0.1)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = (playedPercentage / 100.0).toFloat())
                .background(VegafoXColors.OrangePrimary),
        )
    }
}
```

| Propriete | Valeur |
|-----------|--------|
| Hauteur | 2dp |
| Position | `Alignment.BottomCenter` dans le Box poster |
| Fond piste | `Color(0x1AFFFFFF)` — blanc 10% opacite |
| Remplissage | `VegafoXColors.OrangePrimary` (#FF6B00) |
| Fraction | `playedPercentage / 100.0` |

---

## 2. Badge "NEW" (items non vus)

### Fichier modifie

`app/src/main/java/org/jellyfin/androidtv/ui/browsing/compose/BrowseMediaCard.kt`

### Implementation

Badge affiche en haut a droite du poster quand l'item n'a jamais ete lu :

```kotlin
val isNew = item.userData?.played == false &&
    (item.userData?.playCount == null || item.userData?.playCount == 0)
if (isNew) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(4.dp)
            .background(
                color = VegafoXColors.OrangePrimary,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            text = "NEW",
            style = JellyfinTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = VegafoXColors.Background,
        )
    }
}
```

| Propriete | Valeur |
|-----------|--------|
| Position | `Alignment.TopEnd` + 4dp padding externe |
| Fond | `VegafoXColors.OrangePrimary` (#FF6B00) |
| Coins | `RoundedCornerShape(4.dp)` |
| Padding interne | 7dp horizontal, 2dp vertical |
| Texte | "NEW", 10sp, Bold |
| Couleur texte | `VegafoXColors.Background` (#0A0A0F) |

### Condition d'affichage

- `item.userData?.played == false`
- ET `item.userData?.playCount` est null ou 0

Les items "Continue Watching" (avec `playedPercentage > 0`) n'affichent pas le badge car `played` reste `false` mais la barre de progression est plus pertinente.

### Imports ajoutes

```kotlin
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
```

---

## 3. Strings i18n — titres de rows

### Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `app/src/main/res/values/strings.xml` | Mise a jour casing + ajout 4 nouvelles strings |
| `app/src/main/res/values-fr/strings.xml` | Mise a jour traductions + ajout 4 nouvelles strings |
| `app/.../ui/home/compose/HomeViewModel.kt` | Remplacement 10 strings hardcodees par `application.getString()` |
| `app/.../ui/home/compose/HomePrefetchService.kt` | Remplacement 2 strings hardcodees par `application.getString()` |
| `app/.../di/AppModule.kt` | Ajout `androidApplication()` dans constructeurs |

### Strings resources

**values/strings.xml** — mises a jour :

| Cle | Avant | Apres |
|-----|-------|-------|
| `home_section_resume` | Continue watching | Continue Watching |
| `home_section_next_up` | Next up | Next Up |
| `home_section_recently_released` | Recently released | Recently Released |
| `home_section_resume_audio` | Continue listening | Continue Listening |

**values/strings.xml** — ajouts :

| Cle | Valeur |
|-----|--------|
| `home_section_live_tv` | On Now |
| `home_section_latest` | Latest in %1$s |
| `home_section_my_media` | My Media |
| `home_section_recordings` | Recordings |

**values-fr/strings.xml** — mises a jour :

| Cle | Avant | Apres |
|-----|-------|-------|
| `home_section_resume` | Continuer de regarder | Continuer a regarder |
| `home_section_next_up` | A suivre | Prochains episodes |
| `home_section_resume_audio` | Poursuivre l'ecoute | Continuer l'ecoute |
| `home_section_playlists` | Listes de lecture | Playlists |

**values-fr/strings.xml** — ajouts :

| Cle | Valeur |
|-----|--------|
| `home_section_live_tv` | En ce moment |
| `home_section_latest` | Derniers ajouts — %1$s |
| `home_section_my_media` | Mes medias |
| `home_section_recordings` | Enregistrements |

### HomeViewModel — injection Application

```kotlin
class HomeViewModel(
    val api: ApiClient,
    private val application: Application,  // NOUVEAU
    private val userRepository: UserRepository,
    ...
)
```

Tous les titres hardcodes remplaces par :
- `application.getString(R.string.home_section_resume)`
- `application.getString(R.string.home_section_next_up)`
- `application.getString(R.string.home_section_latest, view.name)`
- `application.getString(R.string.home_section_recently_released)`
- `application.getString(R.string.home_section_my_media)`
- `application.getString(R.string.home_section_resume_audio)`
- `application.getString(R.string.home_section_recordings)`
- `application.getString(R.string.home_section_live_tv)`
- `application.getString(R.string.home_section_playlists)`

### HomePrefetchService — injection Application + fix scope leak

```kotlin
class HomePrefetchService(
    private val application: Application,  // NOUVEAU
    private val api: ApiClient,
)
```

Titres remplaces par :
- `application.getString(R.string.home_section_resume)`
- `application.getString(R.string.home_section_next_up)`

### Koin AppModule

```kotlin
single { HomePrefetchService(androidApplication(), get()) }
viewModel { HomeViewModel(get(), androidApplication(), get(), get(), get(), get(), get()) }
```

Import ajoute : `import org.koin.android.ext.koin.androidApplication`

---

## 4. Suppression Log.d STARTUP

6 appels `Log.d("STARTUP", ...)` supprimes :

| Fichier | Ligne (avant) | Contenu |
|---------|---------------|---------|
| `HomeViewModel.kt:68` | `Log.d("STARTUP", "HomeViewModel init: ...")` |
| `HomeViewModel.kt:80` | `Log.d("STARTUP", "Using prefetched rows ...")` |
| `HomeViewModel.kt:176` | `Log.d("STARTUP", "Full rows loaded ...")` |
| `HomePrefetchService.kt:39` | `Log.d("STARTUP", "Prefetch started: ...")` |
| `HomePrefetchService.kt:51` | `Log.d("STARTUP", "Prefetch complete ...")` |
| `HomeComposeFragment.kt:28` | `Log.d("STARTUP", "HomeComposeFragment.onCreateView: ...")` |

Import `android.util.Log` retire des 3 fichiers.

---

## 5. Fix CoroutineScope leak (HomePrefetchService)

### Avant (fuite)

```kotlin
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

Le `SupervisorJob` n'etait jamais annule — la scope et ses references (ApiClient) persistaient indefiniment.

### Apres (corrige)

```kotlin
private var scope: CoroutineScope? = null

fun prefetch() {
    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    scope?.launch { ... }
}

fun consume(): List<TvRow<BaseItemDto>>? {
    val data = _prefetchedRows.value
    _prefetchedRows.value = null
    scope?.cancel()
    scope = null
    return data
}
```

La scope est creee a la demande dans `prefetch()` et annulee proprement dans `consume()`.

---

## 6. Build

```
./gradlew :app:compileGithubDebugKotlin   -> BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin -> BUILD SUCCESSFUL
```

## 7. Resume des fichiers modifies

| Fichier | Parties |
|---------|---------|
| `BrowseMediaCard.kt` | P1 (progress bar) + P2 (badge NEW) |
| `HomeViewModel.kt` | P3 (strings i18n) + Log.d cleanup |
| `HomePrefetchService.kt` | P3 (strings i18n) + Log.d cleanup + scope leak fix |
| `HomeComposeFragment.kt` | Log.d cleanup |
| `AppModule.kt` | Koin injection Application |
| `values/strings.xml` | 4 updates + 4 ajouts |
| `values-fr/strings.xml` | 4 updates + 4 ajouts |
