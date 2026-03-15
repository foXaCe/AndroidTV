# Cleanup 06 — Dependencies, layouts, animations, code mort

**Date** : 2026-03-12
**Branche** : main

---

## Etape 1 — Dependencies Gradle inutilisees

### Methode

Verification de chaque dependance declaree dans `app/build.gradle.kts`
par recherche d'imports dans le code source (Kotlin, Java, XML).

### Dependances supprimees (0 usage confirme)

| Dependance | Package | Raison |
|-----------|---------|--------|
| `androidx-window` | `androidx.window` | 0 imports, 0 refs XML |
| `koin-android-compat` | `io.insert-koin:koin-android-compat` | 0 imports (dans bundle koin, jamais utilise) |

Fichiers modifies :
- `app/build.gradle.kts` : retrait de `implementation(libs.androidx.window)`
- `gradle/libs.versions.toml` : retrait version `androidx-window`, library `androidx-window`,
  library `koin-android-compat`, entree bundle `koin`

### Dependances conservees (usage confirme)

| Dependance | Fichiers importants | Usage |
|-----------|-------|-------|
| accompanist-permissions | speechRecognizer.kt | Permissions Compose |
| androidx-cardview | dialog_create_playlist.xml | Widget CardView |
| androidx-constraintlayout | 4 layouts XML (jellyseerr, popup, vlc, row_details) | Widget ConstraintLayout |
| androidx-recyclerview | 5 fichiers (adapters Jellyseerr) | RecyclerView |
| androidx-preference | UserPreferences.kt, UserSettingPreferences.kt | Preferences |
| markwon | MarkdownRenderer.kt | Rendu Markdown |
| aboutlibraries | SettingsLicenseScreen.kt, SettingsLicensesScreen.kt | Ecran licences |
| androidx-media3-ui | 6 fichiers (player, subtitles) | Media controls |
| coil-gif | AppModule.kt (GifDecoder.Factory) | Decodeur GIF |
| coil-svg | AppModule.kt (SvgDecoder.Factory) | Decodeur SVG |
| acra | JellyfinApplication.kt, TelemetryService.kt | Crash reporting |
| androidx-tvprovider | LeanbackChannelWorker.kt | Android TV channels |
| androidx-splashscreen | StartupActivity.kt | Splash screen |

### Dependances transitives (conservees)

| Dependance | Raison |
|-----------|--------|
| findbugs-jsr305 | Annotations NewPipe extractor |
| pipeextractor-nanojson | Dep transitive NewPipe |
| pipeextractor-jsoup | Dep transitive NewPipe |
| slf4j-timber | Bridge logging SLF4J → Timber |

---

## Etape 2 — Layouts XML orphelins

### Methode

Inventaire des 28 layouts dans `res/layout/`, verification par
`R.layout.NOM`, `@layout/NOM` et ViewBinding classes dans tout `app/src/`.

### Layouts supprimes (6 fichiers, 331 LOC)

| Layout | LOC | Raison |
|--------|-----|--------|
| `fragment_content_view.xml` | 9 | Remnant startup refactor |
| `genres_grid_browse.xml` | 165 | Remplace par GenresGridScreen Compose |
| `jellyfin_genre_card.xml` | 74 | Remplace par Compose card system |
| `jellyseerr_genre_card.xml` | 47 | Remplace par JellyseerrGenreCard Composable |
| `jellyseerr_network_studio_card.xml` | 19 | Remplace par Compose durant redesign |
| `view_card_notification.xml` | 17 | Jamais integre ; orphelin notification system |

### Layout conserve (legacy)

| Layout | Raison |
|--------|--------|
| `vlc_player_interface.xml` | Marque legacy explicitement — data binding compat |

### Layouts actifs (21 fichiers)

Tous verifies : ViewBinding ou R.layout references dans le code actif.

---

## Etape 3 — Code commente + @Suppress obsoletes

### Code commente

**0 bloc de code mort** trouve dans `app/src/main/java/org/jellyfin/androidtv/ui/`.
Tous les commentaires multi-lignes sont des commentaires explicatifs legitimes.

### @Suppress annotations

**34 annotations @Suppress** trouvees dans le code.
**Toutes justifiees** :

| Categorie | Nombre | Exemples |
|-----------|--------|----------|
| Compatibilite API Android | 14 | DEPRECATION pour multi-API |
| Algorithmes / constantes | 7 | MagicNumber pour bitrates, resolutions |
| Framework Android interne | 5 | RestrictedApi pour TV provider |
| Shadowing intentionnel | 4 | NAME_SHADOWING pour defaults |
| Autres (exceptions, params) | 4 | TooGenericExceptionCaught, UNUSED_PARAMETER |

**Aucun nettoyage necessaire.**

---

## Etape 4 — Menus + animations XML orphelins

### Menus

**Aucun dossier `res/menu/`** dans le projet — 0 fichier a verifier.

### Animations

7 fichiers trouves dans `res/anim/` + `res/animator/`.

| Animation | Refs | Statut |
|-----------|------|--------|
| `fade_in.xml` | 2 | CONSERVE (styles.xml, DestinationFragmentView.kt) |
| `fade_out.xml` | 2 | CONSERVE (styles.xml, DestinationFragmentView.kt) |
| `slide_right_in.xml` | 1 | CONSERVE (styles.xml) |
| `slide_right_out.xml` | 1 | CONSERVE (styles.xml) |
| `slide_bottom_in.xml` | 0 | **SUPPRIME** |
| `slide_top_in.xml` | 0 | **SUPPRIME** |
| `rotate_spinner.xml` | 0 | **SUPPRIME** (animator/) |

3 fichiers supprimes (33 LOC).
Dossier `res/animator/` supprime (vide).

---

## Etape 5 — BuildConfig fields inutilises

### Fields declares

| Field | Type | Usages | Fichiers |
|-------|------|--------|----------|
| `ENABLE_OTA_UPDATES` | boolean | 4 | JellyfinApplication, MainActivity, SettingsMainScreen, PluginSyncService |
| `DEVELOPMENT` | boolean | 1 | TelemetryService |

### Fields auto-generes utilises

| Field | Usages |
|-------|--------|
| `VERSION_NAME` | 12 |
| `DEBUG` | 8 |
| `APPLICATION_ID` | 2 |
| `VERSION_CODE` | 1 |
| `BUILD_TYPE` | 1 |

**Aucun field orphelin. Aucune suppression.**

---

## Etape 6 — Fichiers de test obsoletes

### Unit tests (app/src/test/)

8 fichiers — toutes les classes testees existent encore.

### Instrumented tests (app/src/androidTest/)

7 fichiers — tous les composables testes existent encore.

**0 fichier de test obsolete.**

---

## Verification finale

### Compilation

```
./gradlew :app:compileGithubDebugKotlin → BUILD SUCCESSFUL
./gradlew :app:compileGithubDebugUnitTestKotlin → BUILD SUCCESSFUL
```

### Build

| Variante | Resultat |
|----------|---------|
| `assembleGithubDebug` | BUILD SUCCESSFUL |
| `assembleGithubRelease` | BUILD SUCCESSFUL |
| Install AM9 Pro | Success |

---

## Bilan

### Ce round (cleanup_06)

| Element | Fichiers | LOC supprimees |
|---------|----------|---------------|
| Dependencies Gradle | 2 deps retires | 5 |
| Layouts orphelins | 6 supprimes | 331 |
| Animations orphelines | 3 supprimes | 33 |
| Code commente | 0 | 0 |
| @Suppress | 0 | 0 |
| BuildConfig fields | 0 | 0 |
| Tests obsoletes | 0 | 0 |
| **Total round** | **9 fichiers** | **~369 LOC** |

### Tous les cleanups cumules

| Cleanup | LOC supprimees |
|---------|---------------|
| cleanup_01 — Dead code, drawables, couleurs | ~337 |
| cleanup_02 — Drawables, couleurs, tokens | ~169 |
| cleanup_03 — Imports morts | ~69 |
| cleanup_04 — Java/Kotlin morts, overlay, startup | ~3 124 |
| cleanup_05 — Strings orphelines | ~326 (+6 751 traductions) |
| cleanup_06 — Deps, layouts, anims | ~369 |
| **Total** | **~4 394 LOC** |
