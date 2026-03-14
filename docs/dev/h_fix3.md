# H_FIX3 — Badge NEW i18n + Hero overlay espace vertical

**Date** : 2026-03-10
**Statut** : Termine — BUILD SUCCESSFUL debug + release, installe sur AM9 Pro

---

## Probleme 1 : Badge "NEW" non traduit

### Avant

Texte hardcode `"NEW"` dans `BrowseMediaCard.kt` — identique dans toutes les langues.

### Apres

Remplace par `stringResource(R.string.lbl_new)`.

### Fichiers modifies

| Fichier | Changement |
|---------|------------|
| `app/.../ui/browsing/compose/BrowseMediaCard.kt` | `"NEW"` → `stringResource(R.string.lbl_new)`, ajout import `stringResource` |
| `app/src/main/res/values/strings.xml` | `lbl_new` : "New" → "NEW" |
| `app/src/main/res/values-fr/strings.xml` | `lbl_new` : "Nouveau" → "NOUVEAU" |

### Strings

| Cle | EN | FR |
|-----|----|----|
| `lbl_new` | NEW | NOUVEAU |

---

## Probleme 2 : Pills, description et boutons hero absents

### Cause

Le `Box(weight=0.45)` de la zone hero etait trop petit pour contenir :
tag line + titre (32sp, 2 lignes) + pills + description (2 lignes) + boutons (52dp min height, 14dp padding).

### Corrections appliquees

| Fichier | Changement | Avant | Apres |
|---------|------------|-------|-------|
| `HomeScreen.kt` | Weight zone hero | `0.45f` | `0.52f` |
| `HomeScreen.kt` | Weight zone rows | `0.55f` | `0.48f` |
| `HomeHeroBackdrop.kt` | Titre hero fontSize | `32.sp` | `26.sp` |
| `HomeHeroBackdrop.kt` | Description maxLines | `2` | `1` |
| `HomeHeroBackdrop.kt` | Spacer avant boutons | `20.dp` | `12.dp` |
| `HomeHeroBackdrop.kt` | Boutons : `compact = true` | — | Ajoute |
| `HomeHeroBackdrop.kt` | Bouton Play minWidth | `160.dp` | `140.dp` |
| `HomeHeroBackdrop.kt` | Bouton Ma liste minWidth | `140.dp` | `120.dp` |
| `VegafoXButton.kt` | Nouveau parametre `compact` | — | `compact: Boolean = false` |

### VegafoXButton — mode compact

| Propriete | Normal | Compact |
|-----------|--------|---------|
| `minWidth` | 200dp | 120dp |
| `minHeight` | 52dp | 40dp |
| Padding horizontal | 32dp | 20dp |
| Padding vertical | 14dp | 8dp |
| fontSize | 16sp | 14sp |

Le mode compact est utilise dans `HeroActionButtons` pour reduire l'empreinte verticale des boutons dans la zone hero.

---

## Espace vertical gagne

| Element | Avant | Apres | Gain |
|---------|-------|-------|------|
| Zone hero (weight) | 45% | 52% | +7% (~76px sur 1080p) |
| Titre (32sp → 26sp, 2 lignes max) | ~86px | ~70px | ~16px |
| Description (2 lignes → 1 ligne) | ~40px | ~20px | ~20px |
| Spacer boutons | 20dp | 12dp | 8dp |
| Boutons (minHeight 52 → 40, padding 14 → 8) | ~52px | ~40px | ~12px |
| **Total estime** | — | — | **~132px** |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
./gradlew :app:assembleGithubRelease      → BUILD SUCCESSFUL
```

## Installation

```
adb -s 192.168.1.152:5555 install -r vegafox-androidtv-v1.6.2-github-debug.apk   → Success
adb -s 192.168.1.152:5555 install -r vegafox-androidtv-v1.6.2-github-release.apk → Success
```

## Resume des fichiers modifies

| Fichier | Changement |
|---------|------------|
| `BrowseMediaCard.kt` | Badge NEW i18n (`stringResource`) |
| `HomeScreen.kt` | Weights hero 0.52 / rows 0.48 |
| `HomeHeroBackdrop.kt` | Titre 26sp, description 1 ligne, spacer 12dp, boutons compact |
| `VegafoXButton.kt` | Parametre `compact` (padding 8dp, minHeight 40dp, fontSize 14sp) |
| `values/strings.xml` | `lbl_new` → "NEW" |
| `values-fr/strings.xml` | `lbl_new` → "NOUVEAU" |
