# Audit APK Final — audit-apk-final-01

**Date** : 2026-03-17
**Scope** : Audit complet de performance APK : taille, ressources, DEX, temps de build, R8/ProGuard, dépendances
**Baseline** : cleanup_12 (2026-03-13) — APK playstore-release = 33 046 994 bytes (31.5 MB)

---

## Point 1 — Taille de l'APK

### APK disponibles (v1.6.2)

| Variante | Taille (bytes) | Taille (MB) | Date |
|----------|---------------|-------------|------|
| github-release | **32 690 167** | **31.2 MB** | 17 mars |
| playstore-release | 33 046 028 | 31.5 MB | 13 mars |
| github-debug | 58 161 329 | 55.5 MB | 17 mars |
| playstore-debug | 68 123 333 | 65.0 MB | 14 mars |

### Comparaison avec baseline cleanup_12

| Mesure | Cleanup 12 (playstore) | Actuel (github) | Delta |
|--------|----------------------|-----------------|-------|
| APK compressé | 33 046 994 bytes | 32 690 167 bytes | **-356 KB (-1.1%)** |
| Fichiers dans l'APK | 922 | 899 | -23 fichiers |

**Verdict** : L'APK a légèrement diminué (-356 KB) depuis la baseline, principalement grâce aux suppressions de drawables (cleanup_07 / cleanups récents). La différence github vs playstore est minime (~356 KB), probablement due à l'OTA `buildConfigField`.

### Décomposition par catégorie (github-release, décompressé)

| Catégorie | Taille (bytes) | Taille (MB) | % du total |
|-----------|---------------|-------------|------------|
| lib/ (natives) | 21 893 980 | 20.9 MB | **51.9%** |
| classes*.dex | 15 799 940 | 15.1 MB | **37.4%** |
| resources.arsc | 2 065 844 | 2.0 MB | 4.9% |
| res/ | 1 924 263 | 1.8 MB | 4.6% |
| assets/ | 229 165 | 0.2 MB | 0.5% |
| META-INF/ | 238 244 | 0.2 MB | 0.6% |
| kotlin/ | 53 396 | 52 KB | 0.1% |

### Ratio debug/release

Le debug APK (55.5 MB) est **1.8x** plus gros que le release (31.2 MB), ce qui est normal :
- R8 debug utilise `-dontoptimize` (pas d'optimisation bytecode)
- Le debug garde toutes les classes du projet (`-keep class org.jellyfin.androidtv.** { *; }`)
- Pas de tree-shaking agressif en debug

---

## Point 2 — Analyse des ressources volumineuses

### Top 15 fichiers les plus lourds dans l'APK

| # | Fichier (obfusqué R8) | Taille | Type identifié |
|---|----------------------|--------|----------------|
| 1 | classes.dex | 10.4 MB | Code compilé principal |
| 2 | classes3.dex | 4.8 MB | Code compilé (libs) |
| 3 | lib/x86/libass.so | 3.2 MB | Native — sous-titres ASS |
| 4 | lib/x86_64/libass.so | 3.1 MB | Native — sous-titres ASS |
| 5 | lib/arm64-v8a/libass.so | 3.0 MB | Native — sous-titres ASS |
| 6 | lib/armeabi-v7a/libass.so | 2.1 MB | Native — sous-titres ASS |
| 7 | resources.arsc | 2.0 MB | Table de ressources compilées |
| 8 | lib/x86_64/libffmpegJNI.so | 1.6 MB | Native — FFmpeg |
| 9 | lib/x86/libffmpegJNI.so | 1.5 MB | Native — FFmpeg |
| 10 | lib/arm64-v8a/libffmpegJNI.so | 1.5 MB | Native — FFmpeg |
| 11 | lib/armeabi-v7a/libffmpegJNI.so | 1.4 MB | Native — FFmpeg |
| 12 | lib/arm64-v8a/libc++_shared.so | 1.3 MB | Native — C++ runtime |
| 13 | lib/x86_64/libc++_shared.so | 1.2 MB | Native — C++ runtime |
| 14 | lib/x86/libc++_shared.so | 1.1 MB | Native — C++ runtime |
| 15 | lib/armeabi-v7a/libc++_shared.so | 0.9 MB | Native — C++ runtime |

### Bibliothèques natives totales par lib

| Bibliothèque | Total (4 ABIs) | Usage |
|-------------|---------------|-------|
| libass.so | **10.9 MB** | Rendu sous-titres ASS/SSA |
| libffmpegJNI.so | **5.6 MB** | Décodeur FFmpeg |
| libc++_shared.so | **4.3 MB** | Runtime C++ (requis par libass + FFmpeg) |
| libasskt.so | 0.1 MB | Bridge Kotlin pour libass |
| libandroidx.graphics.path.so | 0.04 MB | AndroidX Graphics Path |
| **Total lib/** | **20.9 MB** | **51.9% de l'APK** |

### Ressources dans les sources (app/src/main/res/)

| Dossier | Taille |
|---------|--------|
| **Total res/** | **5.7 MB** |
| mipmap-xhdpi/ | 872 KB |
| drawable/ | 764 KB |
| mipmap-xxxhdpi/ | 760 KB |
| mipmap-xxhdpi/ | 504 KB |
| font/ | 328 KB |
| mipmap-hdpi/ | 140 KB |
| values/ (strings default) | 132 KB |
| values-fr/ | 92 KB |
| mipmap-mdpi/ | 76 KB |
| layout/ | 76 KB |

### Fonts

| Fichier | Taille |
|---------|--------|
| jetbrains_mono.ttf | 264 KB |
| bebas_neue.ttf | 60 KB |
| **Total fonts** | **324 KB** |

Les fonts ne sont **pas subsettées** — `jetbrains_mono.ttf` (264 KB) inclut probablement tous les glyphes. Un subsetting pourrait réduire à ~80 KB si on ne garde que les caractères Latin + chiffres.

### Images PNG vs WebP

| Type | Nombre dans l'APK | Observation |
|------|-------------------|-------------|
| PNG | 342 | Majoritaires — beaucoup viennent des dépendances (Leanback, etc.) |
| WebP | 15 | Très minoritaires |
| 9-patch (.9.png) | ~20 | Drawables système |
| JPG | 1 | Unique |

La majorité des PNGs sont de petite taille (icônes, mipmaps). Les plus grosses :
- `app_banner.png` : 640 KB (xxxhdpi), 428 KB (xxhdpi), 204 KB (xhdpi)
- `ic_vegafox_fox.png` : 276 KB
- `vegafox_splash.png` : 192 KB

### Assets

**Pas de dossier `assets/` dans les sources.** Les 229 KB dans l'APK proviennent des dépendances :
- `PublicSuffixDatabase.list` (133 KB) — OkHttp
- `shaders/` (78 KB) — shaders GPU (androidx)
- `dexopt/` (19 KB) — AndroidX optimisation

---

## Point 3 — Analyse du code DEX

### Configuration DEX

| Paramètre | Valeur |
|-----------|--------|
| minSdk | **23** (Android 6.0) |
| compileSdk | 36 |
| targetSdk | 36 |
| Multidex | **Automatique** (minSdk >= 21, pas besoin de lib multidex) |
| Nombre de DEX | **3** |
| Desugaring | **Activé** (coreLibraryDesugaring) |

### Détail des fichiers DEX

| Fichier | Taille | Methods | Fields | Classes | Strings |
|---------|--------|---------|--------|---------|---------|
| classes.dex | 10.4 MB | 63 943 | 43 367 | 10 696 | 48 219 |
| classes2.dex | 0.5 MB | 5 694 | 1 198 | 599 | 3 176 |
| classes3.dex | 4.8 MB | 35 691 | 16 969 | 3 155 | 21 170 |
| **Total** | **15.1 MB** | **105 328** | **61 534** | **14 450** | **72 565** |

### Analyse des limites

- `classes.dex` utilise **97.6%** de la limite de 65 536 méthodes — presque plein
- Le multidex est **nécessaire** (105 328 méthodes > 65 536)
- 3 DEX files est raisonnable pour un projet de cette taille avec Compose + Media3 + Ktor + Leanback

### Material Icons Extended

La dépendance `androidx.compose.material:material-icons-extended` est présente dans le build. Le projet utilise **84 icônes uniques** (toutes via `Icons.Default.*`). R8 tree-shake correctement les icônes non utilisées — la présence des fichiers META-INF de version dans l'APK est normale, seules les classes d'icônes référencées sont conservées.

---

## Point 4 — Temps de build

### Build complet (--rerun-tasks, aucun cache)

| Mesure | Valeur |
|--------|--------|
| **Temps total** | **2 min 28 s** |
| Task Execution | 4 min 34 s (parallelisé) |
| Startup | 0.4 s |
| Configuration | 0.2 s |
| Artifact Transforms | 0.005 s |
| Tasks exécutées | **280** |

### Top 10 tâches les plus longues

| # | Tâche | Durée | Commentaire |
|---|-------|-------|-------------|
| 1 | `:app:minifyGithubReleaseWithR8` | **1 min 28 s** | R8 minification/shrinking — **60% du build** |
| 2 | `:app:lintVitalAnalyzeGithubRelease` | 44.6 s | Lint sur module app |
| 3 | `:app:compileGithubReleaseKotlin` | 31.3 s | Compilation Kotlin app |
| 4 | `:playback:core:lintVitalAnalyzeRelease` | 11.0 s | Lint playback:core |
| 5 | `:playback:media3:exoplayer:lintVitalAnalyzeRelease` | 10.2 s | Lint exoplayer |
| 6 | `:playback:jellyfin:lintVitalAnalyzeRelease` | 10.0 s | Lint jellyfin |
| 7 | `:playback:media3:session:lintVitalAnalyzeRelease` | 9.1 s | Lint session |
| 8 | `:app:l8DexDesugarLibGithubRelease` | 8.5 s | Desugaring |
| 9 | `:preference:lintVitalAnalyzeRelease` | 7.7 s | Lint preference |
| 10 | `:design:lintVitalAnalyzeRelease` | 6.8 s | Lint design |

### Configuration Gradle

| Paramètre | Valeur | Observation |
|-----------|--------|-------------|
| JVM heap | `-Xmx4g` | Correct |
| Parallel builds | Non configuré | Défaut Gradle (non parallel) |
| Configuration cache | **Non activé** | Gradle suggère de l'activer |
| Build cache | Non explicitement activé | Utilise le cache local par défaut |

### Observations performance

1. **R8 domine le build** — 1m28s soit 60% du temps total. C'est normal pour un projet avec `isMinifyEnabled=true` + `isShrinkResources=true` en release.
2. **Lint cumulé** — ~100s au total sur 7 modules. Pourrait être désactivé en dev (`lintVitalEnabled = false`) si le build time est un problème.
3. **Parallélisme** — Le build réel (2m28s) est bien inférieur au total cumulé (4m34s), ce qui montre que Gradle parallélise efficacement malgré l'absence de `org.gradle.parallel=true` explicite.
4. **Configuration cache** — Non activé, pourrait économiser ~0.2s sur les builds incrémentaux (marginal).

---

## Point 5 — Configuration R8/ProGuard

### Fichiers de configuration

| Fichier | Build type | Usage |
|---------|-----------|-------|
| `proguard-android-optimize.txt` | Release + Debug | Règles Android par défaut |
| `app/proguard-rules.pro` | Release + Debug | Règles projet (94 lignes) |
| `app/proguard-debug.pro` | Debug uniquement | Désactive optimisation, keep all project classes |

### Configuration R8 par build type

| Paramètre | Release | Debug |
|-----------|---------|-------|
| isMinifyEnabled | **true** | **true** |
| isShrinkResources | **true** | **true** |
| `-dontoptimize` | Non | **Oui** (debug only) |
| Keep all project classes | Non | **Oui** (`-keep class org.jellyfin.androidtv.** { *; }`) |

### Analyse des règles keep (proguard-rules.pro)

| Règle | Scope | Nécessité | Risque |
|-------|-------|-----------|--------|
| `-keep class org.commonmark.** { *; }` | Tout Commonmark | **Requis** — Markwon utilise la réflexion | OK |
| kotlinx.serialization keeps | Serializers + Companion | **Requis** — serialization par réflexion | OK |
| `-keep class org.jellyfin.sdk.model.** { *; }` | Tout le SDK model | **Large** — garde tout le SDK model même les classes non utilisées | Peut-être trop large |
| `-keep class org.jellyfin.sdk.api.** { *; }` | Tout le SDK API | **Large** — garde toutes les API même les non utilisées | Peut-être trop large |
| `-keep class io.ktor.** { *; }` | Tout Ktor | **Large** — Ktor utilise la réflexion | Probablement requis |
| `-keep class org.koin.** { *; }` | Tout Koin | **Requis** — DI par réflexion | OK |
| `-keep class org.acra.** { *; }` | Tout ACRA | **Requis** — crash reporting | OK |
| `-keep class coil3.** { *; }` | Tout Coil | **Large** — Coil a ses propres consumer rules | Potentiellement redondant |
| `-keep class io.noties.markwon.** { *; }` | Tout Markwon | **Requis** | OK |
| `-keep class androidx.leanback.** { *; }` | Tout Leanback | **Large** — Leanback est volumineux | Peut-être trop large |
| `-keep @Composable class * { *; }` | Classes Composable | **Non standard** — Compose n'a pas besoin de ça | **Inutile** |
| `-keep class * extends Fragment/Activity/...` | Tous les composants Android | **Requis** — référencés par nom dans le manifest/navigation | OK |
| `-keep class org.schabi.newpipe.extractor.** { *; }` | Tout NewPipe Extractor | **Requis** — utilise la réflexion | OK |
| `-keep class org.mozilla.javascript.** { *; }` | Tout Rhino JS | **Requis** — utilisé par NewPipe pour le descrambling | OK |

### Constats R8

1. **R8 est activé en release ET en debug** — bon choix pour la cohérence, le debug garde les classes projet
2. **shrinkResources activé** — les ressources non référencées sont supprimées
3. **Pas de R8 full mode explicite** — le projet utilise le mode par défaut (compat mode). Le full mode (`android.enableR8.fullMode=true` dans gradle.properties) pourrait être plus agressif mais risque des cassages
4. **Règle `-keep @Composable class * { *; }`** est inutile — Compose n'a pas besoin de cette règle, les fonctions `@Composable` sont appelées directement par le compiler plugin, pas par réflexion
5. **Plusieurs keeps trop larges** — `org.jellyfin.sdk.**`, `io.ktor.**`, `coil3.**`, `androidx.leanback.**` gardent potentiellement des classes inutilisées. Mais les réduire nécessiterait des tests approfondis

---

## Point 6 — Dépendances volumineuses

### Estimation de contribution des dépendances majeures

| Dépendance | Version | Impact estimé (APK) | Commentaire |
|-----------|---------|-------------------|-------------|
| **libass** (native) | 0.4.0 | **~10.9 MB** | Plus gros contributeur — sous-titres ASS/SSA en 4 ABIs |
| **FFmpeg decoder** (native) | 1.9.0+1 | **~5.6 MB** | Décodeur FFmpeg en 4 ABIs |
| **libc++_shared** (native) | — | **~4.3 MB** | Runtime C++ partagé, requis par libass + FFmpeg |
| **Jetpack Compose** (DEX) | 1.10.4 | ~3-4 MB | Foundation + Material3 + UI + TV Material |
| **Jellyfin SDK** (DEX) | 1.8.6 | ~2-3 MB | Models + API (keep all) |
| **Ktor** (DEX) | 3.4.1 | ~1-2 MB | HTTP client (keep all) |
| **Leanback** (DEX+res) | — | ~1-2 MB | Legacy TV framework |
| **Media3/ExoPlayer** (DEX) | 1.9.2 | ~1-2 MB | Player core + HLS + UI |
| **NewPipe Extractor** (DEX) | v0.26.0 | ~1 MB | YouTube extraction + Rhino JS |
| **Koin** (DEX) | 4.1.1 | ~0.5 MB | DI framework |
| **Coil** (DEX) | 3.4.0 | ~0.5 MB | Image loading (5 modules) |
| **Material Icons Extended** (DEX) | 1.7.8 | ~0.1 MB | Tree-shaken par R8, 84 icônes conservées |
| **OkHttp** (DEX+assets) | transitive | ~0.5 MB | PublicSuffixDatabase.list = 133 KB |
| **ACRA** (DEX) | 5.13.1 | ~0.1 MB | Crash reporting |
| **Markwon** (DEX) | 4.6.2 | ~0.2 MB | Markdown rendering |
| **Timber + SLF4J** (DEX) | 5.0.1 | ~0.05 MB | Logging |

### resConfigs — ABSENT

**`resConfigs` n'est PAS configuré.** Cela signifie que l'APK inclut :
- **65 locales** de strings.xml du projet
- Les ressources de toutes les densités d'écran (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Les ressources de toutes les locales des dépendances (AndroidX, Material, etc.)

**Impact** : `resources.arsc` fait 2.0 MB. Ce fichier contient la table de toutes les ressources compilées incluant les traductions de toutes les dépendances.

### Densités d'écran sources

| Dossier | Présent | Nécessaire pour TV |
|---------|---------|-------------------|
| mipmap-mdpi | Oui (76 KB) | Non — TV utilise xhdpi minimum |
| mipmap-hdpi | Oui (140 KB) | Non — TV utilise xhdpi minimum |
| mipmap-xhdpi | Oui (872 KB) | **Oui** |
| mipmap-xxhdpi | Oui (504 KB) | **Oui** (4K TV) |
| mipmap-xxxhdpi | Oui (760 KB) | Marginal — peu de TV en xxxhdpi |
| drawable-anydpi | Oui | **Oui** — vecteurs adaptatifs |
| mipmap-anydpi-v26 | Oui | **Oui** — adaptive icons |

### ABI natives — 4 architectures incluses

| ABI | Taille | Pertinence Android TV |
|-----|--------|-----------------------|
| arm64-v8a | 5.8 MB | **Oui** — majorité des box TV (Ugoos, Shield, etc.) |
| armeabi-v7a | 4.4 MB | **Marginal** — anciennes box 32-bit |
| x86 | 5.8 MB | **Émulateur** — pas de TV réelle |
| x86_64 | 5.9 MB | **Émulateur** — pas de TV réelle |

---

## Recommandations

### Gains rapides (faible risque)

| # | Action | Gain estimé | Risque |
|---|--------|-------------|--------|
| R1 | Ajouter `resConfigs` pour exclure les densités `mdpi`, `ldpi`, `tvdpi` | ~200 KB | Faible — TV est xhdpi+ |
| R2 | Supprimer la règle ProGuard `-keep @Composable class * { *; }` | ~50-100 KB | Nul — cette règle n'a aucun effet utile |
| R3 | Subsetter `jetbrains_mono.ttf` (Latin+digits seulement) | ~180 KB | Faible |
| R4 | Ajouter `org.gradle.parallel=true` dans gradle.properties | Temps de build | Nul |
| R5 | Activer configuration cache | Temps de build incrémental | Faible |

### Gains moyens (risque modéré)

| # | Action | Gain estimé | Risque |
|---|--------|-------------|--------|
| R6 | Utiliser ABI splits pour ne livrer que `arm64-v8a` + `armeabi-v7a` | **~11.7 MB** | Moyen — perd le support émulateur x86 |
| R7 | Passer en AAB (App Bundle) pour distribution | **~15 MB** par device | Moyen — seulement pour PlayStore, pas GitHub |
| R8 | Réduire les keeps ProGuard (`org.jellyfin.sdk.**` → keeps ciblés) | ~200-500 KB | Élevé — risque de casser la serialization |
| R9 | Désactiver `lintVitalAnalyze` en dev builds | ~100s de build time | Nul — lint reste sur CI |

### Gains majeurs (si applicable)

| # | Action | Gain estimé | Risque |
|---|--------|-------------|--------|
| R10 | Filtrer les ABIs x86/x86_64 pour les APKs de distribution | **~11.7 MB** | La plupart des box TV sont ARM |
| R11 | Évaluer si libass peut être optionnel (sous-titres ASS rares) | **~10.9 MB** | Élevé — perte de fonctionnalité |
| R12 | R8 full mode (`android.enableR8.fullMode=true`) | ~500 KB-1 MB | Élevé — nécessite tests complets |

---

## Résumé

| Métrique | Valeur | Verdict |
|----------|--------|---------|
| Taille APK release | **31.2 MB** | Stable vs baseline (-1.1%) |
| Natives (lib/) | 20.9 MB (51.9%) | Dominé par libass + FFmpeg — incompressible sans ABI splits |
| DEX total | 15.1 MB (37.4%) | 105K méthodes, R8 actif, tree-shaking fonctionnel |
| Ressources (res/) | 1.8 MB (4.6%) | Compact post-cleanups |
| resources.arsc | 2.0 MB (4.9%) | Pourrait bénéficier de resConfigs |
| Assets | 0.2 MB (0.5%) | Minimal (OkHttp PSL, shaders) |
| Temps de build (clean) | **2 min 28 s** | R8 = 60% du temps (1m28s) |
| R8/ProGuard | Activé release+debug | 1 règle inutile, quelques keeps larges |
| resConfigs | **Non configuré** | Opportunité manquée |
| ABI splits | **Non configuré** | Plus gros levier potentiel (-11.7 MB) |
| Material Icons | Tree-shaken | 84/~2500 icônes conservées — R8 fonctionne |
| Fonts | Non subsettées | Gain potentiel ~180 KB |

**Conclusion** : L'APK est dans un état sain à 31.2 MB. Les natives (libass + FFmpeg) représentent la moitié de la taille et sont incompressibles sans ABI splits. Le code Kotlin/Compose est correctement minifié par R8. Les gains les plus impactants seraient les ABI splits (R6/R10, -11.7 MB) et la configuration resConfigs (R1). Le temps de build de 2m28s est raisonnable pour un projet de cette taille.
