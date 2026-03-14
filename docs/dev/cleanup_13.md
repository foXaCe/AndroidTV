# Cleanup 13 — Deprecated API, FIXME suspend, hardcoded strings

**Date** : 2026-03-13
**Scope** : 3 groupes identifiés dans cleanup_11 (étapes 3 et 4)
**Basé sur** : cleanup_12.md

---

## Groupe 1 — `resources.getColor` deprecated (2 occurrences)

**Fichier** : `ui/playlist/CreatePlaylistDialogFragment.kt`

| Avant | Après |
|-------|-------|
| `resources.getColor(R.color.button_default_highlight_text, null)` | `ContextCompat.getColor(requireContext(), R.color.button_default_highlight_text)` |
| `resources.getColor(android.R.color.black, null)` | `ContextCompat.getColor(requireContext(), android.R.color.black)` |

Import ajouté : `androidx.core.content.ContextCompat`

---

## Groupe 2 — FIXME `PreferencesRepository.kt` (suspend)

**FIXME supprimé** : `// FIXME: Make [getLibraryPreferences] suspended when usages are converted to Kotlin`

**Fonction rendue `suspend`** : OUI

### Justification

Tous les appelants sont déjà dans des contextes suspend :

| Appelant | Contexte |
|----------|----------|
| `LibraryBrowseViewModel.kt:151,220,272` | `viewModelScope.launch { withContext(Dispatchers.IO) { ... } }` |
| `LibraryPreferencesHelper.kt:55,58` | `LaunchedEffect { withContext(Dispatchers.IO) { ... } }` |

La surcharge 1-arg (`getLibraryPreferences(preferencesId)`) n'a aucun appelant externe.

### Changements

- `fun getLibraryPreferences(...)` → `suspend fun getLibraryPreferences(...)`
- `runBlocking(Dispatchers.IO) { store.update() }` → `store.update()` (appel suspend direct)
- Imports `kotlinx.coroutines.Dispatchers` et `kotlinx.coroutines.runBlocking` supprimés

---

## Groupe 3 — Hardcoded strings `GuideFilters.kt`

### Strings réutilisées (existantes dans strings.xml)

| Hardcoded | String resource | Valeur |
|-----------|----------------|--------|
| `"movies"` | `R.string.lbl_movies` | Movies |
| `"news"` | `R.string.lbl_news` | News |
| `"sports"` | `R.string.lbl_sports` | Sports |
| `"series"` | `R.string.lbl_series` | Series |
| `"kids"` | `R.string.lbl_kids` | Kids |
| `"ONLY new"` | `R.string.lbl_new_only` | New only |

### Strings créées (nouvelles)

| Nom | Valeur |
|-----|--------|
| `guide_filter_active` | `Content filtered. Showing channels with %s` |
| `guide_filter_none` | `Showing all programs` |

### Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `GuideFilters.kt` | Ajout `context: Context` au constructeur, `toString()` + `getFilterString()` externalisés |
| `LiveTvGuideViewModel.kt` | Ajout `context: Context` au constructeur, passé à `GuideFilters` |
| `AppModule.kt` | `androidContext()` ajouté à l'injection `LiveTvGuideViewModel` |
| `strings.xml` | +2 nouvelles strings (`guide_filter_active`, `guide_filter_none`) |

---

## LOC nettes ce round

| Changement | Delta |
|------------|-------|
| Groupe 1 : +1 import ContextCompat | +1 |
| Groupe 2 : -2 imports, -1 FIXME | -3 |
| Groupe 3 : +2 imports, +1 param (GuideFilters) | +3 |
| Groupe 3 : +1 import, +1 param (ViewModel) | +2 |
| Groupe 3 : +2 strings.xml | +2 |
| **Net** | **+5** |

---

## LOC total FINAL tous cleanups 01→13

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
| 12 | ~1 |
| **13** | **+5 (qualité, pas suppression)** |
| **Total net** | **~6 401 LOC + 6 798 traductions + 900 KB images** |

---

## Build

- Debug (github + playstore) : **BUILD SUCCESSFUL**
- Release (github) : **BUILD SUCCESSFUL**
- Installé sur AM9 Pro : **Success**
