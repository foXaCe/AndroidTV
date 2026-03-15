# Hero Fix 1 — Layout stable + transition douce

**Date** : 2026-03-11
**Statut** : Termine — BUILD SUCCESSFUL debug + release, installe sur AM9 Pro

---

## Probleme 1 — Sautillement layout hero

### Cause
La `Column` de `HeroInfoContent` utilisait `fillMaxHeight()` sans hauteur fixe.
La description (0 a 4 lignes) et l'affichage conditionnel de l'overview
faisaient varier la hauteur totale du bloc a chaque changement d'item,
provoquant un deplacement vertical des boutons.

### Solution

| Element | Avant | Apres |
|---------|-------|-------|
| Column hauteur | `fillMaxHeight()` | `height(200.dp)` fixe |
| Spacer haut (20%) | `Spacer(fillMaxHeight(0.20f))` | Supprime |
| Synopsis maxLines | 4 | 2 |
| Synopsis fontSize | 13.sp | 14.sp |
| Synopsis lineHeight | 18.sp | 20.sp |
| Synopsis affichage | Conditionnel (`if !null`) | Toujours present (`overview.orEmpty()`) |
| Synopsis minHeight | Aucun | `defaultMinSize(minHeight = 44.dp)` (2 lignes de 20sp) |
| Spacer avant boutons | `Spacer(height(12.dp))` fixe | `Spacer(weight(1f))` flexible |
| Position boutons | Variable selon contenu | Ancres en bas des 200dp |

### Resultat
Les boutons Lecture et Ma liste restent toujours a la meme position verticale,
quel que soit le nombre de lignes du titre ou du synopsis.
Le `Spacer(weight(1f))` absorbe l'espace variable entre le synopsis et les boutons.

---

## Probleme 2 — Transition trop violente

### Cause
L'`AnimatedContent` du `HeroInfoOverlay` utilisait `slideInVertically(+20dp)`
en plus du `fadeIn`, creant un mouvement vertical agite sur ecran TV.
La duree du fadeIn etait 300ms — trop lente par rapport au crossfade backdrop 400ms.

### Solution

| Element | Avant | Apres |
|---------|-------|-------|
| fadeIn | 300ms + EaseOutCubic | 200ms, initialAlpha 0f |
| slideInVertically | +20dp, 300ms | Supprime |
| fadeOut | 150ms | 150ms (inchange) |
| Backdrop crossfade | 400ms | 400ms (inchange) |

### Timing
Le fadeOut du texte precedent (150ms) se joue en meme temps que le crossfade
du backdrop (400ms). Le fadeIn du nouveau texte (200ms) commence apres le
fadeOut, soit un leger decalage naturel pendant que le backdrop est a mi-transition.
Pas de slide — uniquement un fondu enchaine propre et calme.

---

## Fichiers modifies

| Fichier | Changement |
|---------|------------|
| `app/.../ui/home/compose/HomeHeroBackdrop.kt` | `HeroInfoContent` : hauteur fixe 200dp, synopsis 2 lignes avec minHeight 44dp, Spacer weight(1f). `HeroInfoOverlay` : transition fadeIn/fadeOut pure sans slide. Import `defaultMinSize` ajoute, `slideInVertically` retire. |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
./gradlew :app:assembleGithubRelease      → BUILD SUCCESSFUL
```

0 erreur, 0 warning.

## Installation

```
adb install debug  → Success (AM9 Pro 192.168.1.152)
adb install release → Success (AM9 Pro 192.168.1.152)
```
