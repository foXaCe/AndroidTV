# SIDEBAR_E — ProfileItem avec avatar utilisateur

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release — Installé AM9 Pro

---

## Objectif

Créer un composable `ProfileItem` qui affiche l'avatar de l'utilisateur connecté dans la sidebar, avec fallback initiale, focus D-pad, et navigation vers la sélection utilisateur.

---

## ProfileItem — Spécifications

### Données utilisateur

- `UserRepository.currentUser` via Koin (`koinInject<UserRepository>()`)
- URL image : `currentUser?.primaryImage?.getUrl(api)`
- Nom : `currentUser?.name`

### Visuels

| État | Bordure | Fond | Contenu |
|------|---------|------|---------|
| Normal (photo) | 1.5dp `#2A2A3A` CircleShape | Photo Coil crop | Image 52dp |
| Normal (pas de photo) | 1.5dp `#2A2A3A` CircleShape | OrangePrimary alpha 0.18 | Initiale 22sp Bold OrangePrimary |
| Focalisé | 2dp OrangePrimary CircleShape | Idem + glow orange | Idem + scale 1.10 |

### Glow

`drawBehind` + `Brush.radialGradient` : OrangePrimary alpha 0.25, rayon 0.8× maxDimension.

### Animations

| Propriété | Type | Durée | Easing |
|-----------|------|-------|--------|
| `scale` | `animateFloatAsState` | 140ms | EaseOutCubic |
| `glowAlpha` | `animateFloatAsState` | 140ms | EaseOutCubic |

### Input

- `onKeyEvent` : Enter / DpadCenter → `onSelect()`
- `clickable` : souris/touch → `onSelect()`

### Action onSelect (dans PremiumSideBar)

Pattern copié de `LeftSidebarNavigation.kt` :
```kotlin
mediaManager.clearAudioQueue()
sessionRepository.destroyCurrentSession()
activity?.startActivity(ActivityDestinations.startup(context))
activity?.finishAfterTransition()
```

---

## Fichiers créés

| Fichier | Description |
|---------|-------------|
| `ui/home/compose/sidebar/ProfileItem.kt` | Avatar utilisateur avec photo Coil, fallback initiale, focus D-pad, glow |

## Fichiers modifiés

| Fichier | Changement |
|---------|-----------|
| `ui/home/compose/sidebar/PremiumSideBar.kt` | NavItem "Profil" remplacé par `ProfileItem` ; ajout imports SessionRepository, MediaManager, ActivityDestinations |

---

## Build & Install

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
./gradlew :app:assembleGithubDebug        → BUILD SUCCESSFUL
adb install → Success (AM9 Pro 192.168.1.152)
```
