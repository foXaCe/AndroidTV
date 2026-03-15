# Cleanup 12 — Analyse APK, audit permissions, audit Workers, fix DisplayPreferencesStore

**Date** : 2026-03-13
**Scope** : Analyse taille APK release, audit permissions AndroidManifest, audit Workers WorkManager, fix TODO critique DisplayPreferencesStore
**Basé sur** : cleanup_11.md (audit qualité, Timber, coroutines)

---

## ÉTAPE 1 — Analyse taille APK

### Taille APK release

| Mesure | Valeur |
|--------|--------|
| APK compressé | **33 046 994 bytes (31.5 MB)** |
| APK décompressé | 85 235 672 bytes (81.3 MB) |
| Fichiers dans l'APK | 922 |

### Décomposition par catégorie

| Catégorie | Taille (bytes) | Taille (MB) | % du total |
|-----------|---------------|-------------|------------|
| lib/ (natives) | 21 893 980 | 20.9 MB | 25.7% |
| classes*.dex | 15 763 020 | 15.0 MB | 18.5% |
| res/ | 2 292 458 | 2.2 MB | 2.7% |
| resources.arsc | 2 099 672 | 2.0 MB | 2.5% |
| assets/ | 229 218 | 0.2 MB | 0.3% |
| META-INF/ | 242 180 | 237 KB | 0.3% |

### Détail DEX

| Fichier | Taille (bytes) | Taille (MB) |
|---------|---------------|-------------|
| classes.dex | 10 391 236 | 9.9 MB |
| classes3.dex | 4 810 672 | 4.6 MB |
| classes2.dex | 561 112 | 0.5 MB |
| **Total DEX** | **15 763 020** | **15.0 MB** |

### Détail bibliothèques natives (lib/)

| Bibliothèque | arm64-v8a | armeabi-v7a | x86 | x86_64 |
|-------------|-----------|-------------|-----|--------|
| libass.so | 3.0 MB | 2.1 MB | 3.2 MB | 3.1 MB |
| libffmpegJNI.so | 1.4 MB | 1.3 MB | 1.4 MB | 1.5 MB |
| libc++_shared.so | 1.2 MB | 0.8 MB | 1.1 MB | 1.2 MB |
| libasskt.so | 16.9 KB | — | — | — |

**Note** : Les bibliothèques natives (FFmpeg + libass) représentent **25.7%** de l'APK. Un AAB (App Bundle) réduirait la taille par device en ne livrant qu'une seule ABI (~6 MB au lieu de 21 MB).

### Top 10 fichiers les plus lourds

| # | Fichier | Taille | Type |
|---|---------|--------|------|
| 1 | classes.dex | 10.4 MB | Code compilé |
| 2 | classes3.dex | 4.8 MB | Code compilé |
| 3 | lib/x86/libass.so | 3.2 MB | Native (sous-titres ASS) |
| 4 | lib/x86_64/libass.so | 3.1 MB | Native (sous-titres ASS) |
| 5 | lib/arm64-v8a/libass.so | 3.0 MB | Native (sous-titres ASS) |
| 6 | lib/armeabi-v7a/libass.so | 2.1 MB | Native (sous-titres ASS) |
| 7 | resources.arsc | 2.0 MB | Table de ressources |
| 8 | lib/x86_64/libffmpegJNI.so | 1.5 MB | Native (FFmpeg) |
| 9 | lib/x86/libffmpegJNI.so | 1.4 MB | Native (FFmpeg) |
| 10 | lib/arm64-v8a/libffmpegJNI.so | 1.4 MB | Native (FFmpeg) |

**Référence baseline** : Pas de baseline antérieure aux cleanups. Taille actuelle (31.5 MB) notée comme référence post-cleanup 01→12.

---

## ÉTAPE 2 — Permissions AndroidManifest

### Inventaire complet

| Permission | Classification | Justification |
|-----------|---------------|---------------|
| `WRITE_EPG_DATA` | **ACTIF** | LeanbackChannelWorker — insertion channels/programs via TvContractCompat |
| `INTERNET` | **ACTIF** | Appels API Jellyfin, streaming, TMDB, Jellyseerr, OTA updates |
| `ACCESS_NETWORK_STATE` | **ACTIF** | Demandé dans StartupActivity + requis par WorkManager |
| `ACCESS_WIFI_STATE` | **FRAMEWORK** | Requis par `androidx.tvprovider:tvprovider` (transitive) |
| `FOREGROUND_SERVICE` | **FRAMEWORK** | Requis par `androidx.work:work-runtime` pour Workers background |
| `REQUEST_INSTALL_PACKAGES` | **ACTIF** | UpdateCheckerService — installation APK OTA |
| `RECORD_AUDIO` | **ACTIF** | SpeechRecognizer — recherche vocale (runtime permission) |
| ~~`SEND_DATA_TO_ALEXA`~~ | **ORPHELIN** | ~~Aucun code Alexa, aucune intégration Fire TV spécifique~~ |

### Action

- **Supprimé** : `amazon.speech.permission.SEND_DATA_TO_ALEXA` — aucun usage dans le code, aucun SDK Alexa, commentaire "Fire TV Alexa focus management" sans implémentation.

---

## ÉTAPE 3 — WorkManager Workers

### Inventaire

| Worker | Classe | Classification | Enqueue |
|--------|--------|---------------|---------|
| LeanbackChannelWorker | `CoroutineWorker` | **ACTIF** | `JellyfinApplication.onSessionStart()` (periodic 1h) + `MainActivity.onStop()` (one-time) |
| UpdateCheckWorker | `CoroutineWorker` | **ACTIF** | `JellyfinApplication.onSessionStart()` (periodic 24h, conditionné par `ENABLE_OTA_UPDATES`) |

**Résultat** : 2 Workers, 2 actifs, **0 orphelin**. Aucun Worker déclaré directement dans le Manifest (auto-registration WorkManager moderne). Aucune suppression nécessaire.

---

## ÉTAPE 4 — Fix DisplayPreferencesStore (CRITIQUE)

### Analyse du risque

Le `TODO("The DisplayPreferencesStore does not support migrations")` en ligne 139 est un `kotlin.TODO()` qui throw `NotImplementedError` au runtime.

**Chaîne d'appel analysée** :
- `runMigrations()` est défini dans `PreferenceStore` (abstract)
- Implémenté concrètement dans `SharedPreferenceStore` (ligne 87-97)
- Appelé **uniquement** dans les blocs `init {}` de `UserPreferences`, `UserSettingPreferences`, et `AuthenticationPreferences`
- `DisplayPreferencesStore` et ses sous-classes (`LibraryPreferences`, `LiveTvPreferences`) n'ont **aucun bloc `init {}`** qui appelle `runMigrations()`

**Verdict** : Le TODO est **jamais atteint** en usage normal. Le code est sûr *actuellement*. Cependant, c'est une bombe à retardement : un développeur ajoutant un `init {}` avec migration dans une sous-classe provoquerait un crash runtime sans avertissement à la compilation.

### Option choisie : Option B — No-op sécurisé

**Justification** :
- `DisplayPreferencesStore` stocke ses données **sur le serveur** (via l'API DisplayPreferences), pas localement
- Les migrations de préférences locales (`SharedPreferences`) n'ont aucun sens pour ce store
- Un no-op avec commentaire explicatif est la solution la plus sûre et la plus claire
- Un `throw` ou un crash n'apporte aucune valeur vs un no-op documenté

**Modification** :
```kotlin
// Avant (CRASH au runtime)
override fun runMigrations(body: MigrationContext<Unit, Unit>.() -> Unit) {
    TODO("The DisplayPreferencesStore does not support migrations")
}

// Après (no-op sécurisé)
override fun runMigrations(body: MigrationContext<Unit, Unit>.() -> Unit) {
    // No-op: DisplayPreferencesStore stores data on the server via the API,
    // not locally, so local preference migrations do not apply.
}
```

---

## LOC ce round

| Action | LOC |
|--------|-----|
| Permission SEND_DATA_TO_ALEXA supprimée (2 lignes) | -2 |
| Fix TODO → no-op (1 ligne modifiée, 1 commentaire ajouté) | +1 |
| **Net** | **-1** |

---

## LOC total FINAL tous cleanups 01→12

| Phase | LOC supprimées |
|-------|----------------|
| 01 | ~337 |
| 02 | ~169 |
| 03 | ~69 |
| 04 | ~3 124 |
| 05 | ~1 581 + 6 751 traductions |
| 06 | ~369 |
| 07 | ~361 + 900 KB images |
| 08 | ~95 |
| 09 | ~110 + 47 traductions |
| 10 | +140 (centralisation) |
| 11 | ~330 |
| **12** | **~1** |
| **Total net** | **~6 406 LOC + 6 798 traductions + 900 KB images** |

---

## Build

- Debug (github) : **BUILD SUCCESSFUL**
- Release (github) : **BUILD SUCCESSFUL**
- Installé sur AM9 Pro : **Success**
