# Phase 1 — Remplacement material-icons-extended par drawable local

> Date : 2026-03-11
> Objectif : Supprimer la dependance lourde material-icons-extended (~36 MB non minifie)

---

## Contexte

Le projet n'utilisait qu'**une seule icone** de `material-icons-extended` :

| Icone | Fichier | Ligne |
|-------|---------|-------|
| `Icons.AutoMirrored.Outlined.ArrowForward` | `WelcomeScreen.kt` | 208 |

La dependance `androidx.compose.material:material-icons-extended` avait deja ete retiree du TOML et build.gradle.kts lors de la phase 0 (icons_02_cleanup.md), mais l'import Kotlin restait present.

---

## Modifications

### 1. Nouveau drawable : `ic_arrow_forward.xml`

Cree dans `app/src/main/res/drawable/` — vector 24dp, `autoMirrored="true"` pour le support RTL.
Path identique a l'icone Material Symbols "arrow_forward" (Outlined, 24dp).

### 2. Migration WelcomeScreen.kt

| Avant | Apres |
|-------|-------|
| `import androidx.compose.material.icons.Icons` | Supprime |
| `import androidx.compose.material.icons.automirrored.outlined.ArrowForward` | Supprime |
| `icon = Icons.AutoMirrored.Outlined.ArrowForward` | `icon = ImageVector.vectorResource(R.drawable.ic_arrow_forward)` |

Nouveaux imports ajoutes :
- `import androidx.compose.ui.graphics.vector.ImageVector`
- `import androidx.compose.ui.res.vectorResource`

### 3. Dependance supprimee (confirme absent)

| Fichier | Etat |
|---------|------|
| `gradle/libs.versions.toml` | Aucune reference material-icons-extended |
| `app/build.gradle.kts` | Aucune reference material-icons-extended |
| Code source (`**/*.kt`) | Aucun import `androidx.compose.material.icons` |

---

## Dependances Compose — Avant / Apres

### Avant (phase 0)

```toml
# gradle/libs.versions.toml
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version.ref = "androidx-compose-foundation" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.androidx.compose.material.icons.extended)
```

### Apres (phase 1)

```toml
# Rien — dependance supprimee
```

```kotlin
// Rien — utilise un drawable XML local a la place
```

Les dependances Compose restantes :
- `androidx.compose.foundation:foundation:1.10.4` (bundle)
- `androidx.compose.material3:material3:1.4.0` (bundle)
- `androidx.compose.ui:ui-graphics:1.10.4` (bundle)
- `androidx.compose.ui:ui-tooling:1.10.4` (bundle)
- `androidx.tv:tv-material:1.0.1`

> Note : `material3` inclut transitvement `material-icons-core` (~300 icones de base).
> Ce module core est leger et ne necessite pas de suppression.

---

## Gain en taille APK

| Metrique | Valeur |
|----------|--------|
| Taille estimee de material-icons-extended | ~36 MB (non minifie), ~2-5 MB apres R8 |
| Impact reel avec R8/shrink | Minimal (R8 elagait deja les classes inutilisees) |
| APK debug actuel | 56 MB |
| APK release actuel | 33 MB |

> Avec R8 + `isShrinkResources = true`, le tree-shaking supprimait deja les icones non utilisees.
> Le gain principal est la **suppression d'une dependance inutile** du graphe de build,
> ce qui ameliore la reproductibilite et la clarte des dependances.

---

## Verification build

| Variante | Resultat |
|----------|----------|
| `assembleGithubDebug` | BUILD SUCCESSFUL |
| `assembleGithubRelease` | BUILD SUCCESSFUL |
