# Audit — Badges techniques tronqués dans le player vidéo

**Date** : 2026-03-18
**Fichiers analysés** :
- `ui/player/video/VideoPlayerHeader.kt`
- `ui/player/base/PlayerHeader.kt`
- `ui/player/base/PlayerOverlayLayout.kt`
- `ui/base/Text.kt`
- `ui/composable/modifier/overscan.kt`

---

## 1. Architecture du layout (de l'extérieur vers l'intérieur)

### 1.1 PlayerOverlayLayout (couche racine)

```
Box(fillMaxSize)
  └─ AnimatedVisibility (aligné TopCenter)
       └─ Box(fillMaxWidth, fillMaxHeight(0.35f), overscan())   ← CONTENEUR HEADER
            └─ header()  → VideoPlayerHeader → PlayerHeader
```

Le conteneur header occupe **100% de la largeur** et **35% de la hauteur** de l'écran.

Le modifier `overscan()` ajoute un padding de **48dp horizontal + 27dp vertical**.

### 1.2 PlayerHeader (Row parent)

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 32.dp, start = 56.dp, end = 56.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
)
```

**Largeur réelle disponible** = largeur écran − 2×48dp (overscan) − 2×56dp (padding PlayerHeader) = **largeur écran − 208dp**.

Sur un écran 1920dp : ~1712dp disponibles. Sur un écran 1280dp : ~1072dp.

### 1.3 Contenu de la Row (deux colonnes)

| Position | Composable | Modifier | Rôle |
|----------|-----------|----------|------|
| Gauche | `Column(Modifier.weight(1f))` | **weight(1f)** | Titre + épisode + badges |
| Droite | `Column(horizontalAlignment = End)` | **aucun weight, wrap-content** | Horloge + heure de fin |

**Mécanisme** : La colonne droite (horloge) est mesurée en premier en wrap-content, puis la colonne gauche reçoit **tout l'espace restant** via `weight(1f)`.

La colonne horloge affiche typiquement "22:45" (15sp monospace) + "Fin à 00:12" (12sp) → environ **60-80dp de large**.

**Largeur effective de la colonne gauche** ≈ largeur écran − 208dp − ~80dp = **~1424dp sur 1920** / **~784dp sur 1280**.

---

## 2. Structure de la Row de badges

```kotlin
Row(
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(top = 8.dp),
)
```

**Observations critiques** :
- **Pas de `fillMaxWidth()`** : la Row prend sa largeur naturelle (wrap-content, limitée par le parent `weight(1f)`)
- **Pas de `horizontalScroll`** : aucun défilement horizontal
- **Pas de `LazyRow`** : tous les badges sont mesurés et composés simultanément
- **Pas de `Modifier.horizontalScroll()`** : overflow invisible/clippé

### 2.1 Nombre et ordre des badges

Jusqu'à **6 badges** possibles, dans cet ordre :
1. Résolution (ex: "4K", "1080p", "720p") — style RESOLUTION (orange)
2. HDR (ex: "DV", "HDR10+", "HDR10") — style HDR (gold)
3. Codec vidéo (ex: "HEVC", "AV1", "H.264") — style TECH (gris)
4. Container (ex: "MKV", "MP4") — style TECH (gris)
5. Canaux audio (ex: "5.1", "7.1", "Stereo") — style TECH (gris)
6. Codec audio (ex: "EAC3", "TrueHD", "FLAC") — style TECH (gris)

---

## 3. Composable TechBadge — analyse de la troncature

```kotlin
@Composable
private fun TechBadge(text: String, style: BadgeStyle) {
    Box(
        modifier = Modifier
            .border(1.dp, badgeColor, BadgeShape)    // RoundedCornerShape(4.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                letterSpacing = 1.sp,
            ),
        )
    }
}
```

### 3.1 Propriétés du Text dans TechBadge

| Propriété | Valeur | Impact |
|-----------|--------|--------|
| `overflow` | **TextOverflow.Clip** (défaut de `ui.base.Text`) | Le texte est **clippé** sans ellipsis si l'espace manque |
| `softWrap` | **true** (défaut) | Le texte peut passer à la ligne (mais maxLines=MAX_VALUE) |
| `maxLines` | **Int.MAX_VALUE** (défaut) | Pas de limite de lignes |
| `maxWidth` sur Box | **Aucun** | Le badge prend toute la largeur qu'il veut |

### 3.2 Largeur d'un badge individuel

Chaque badge est en **wrap-content** (pas de largeur fixe, pas de `widthIn`, pas de `weight`). La largeur dépend du texte :

- Texte "4K" à 10sp + letterSpacing 1sp + padding 8dp×2 ≈ **30dp**
- Texte "1080p" ≈ **46dp**
- Texte "HDR10+" ≈ **54dp**
- Texte "HEVC" ≈ **42dp**
- Texte "MKV" ≈ **36dp**
- Texte "5.1" ≈ **30dp**
- Texte "TrueHD" ≈ **50dp**

**Total pour 6 badges + 5 gaps** : ~288dp + 30dp = **~318dp** dans le pire cas.

---

## 4. Diagnostic — Pourquoi les badges sont tronqués

### 4.1 Cause primaire : **PAS de troncature par manque de largeur globale**

Avec ~318dp de badges et ~1424dp disponibles (1920px), la largeur est **largement suffisante**. Même sur 1280dp (~784dp disponibles), il n'y a pas de problème d'espace global.

**Les badges individuels NE SONT PAS tronqués par manque d'espace horizontal dans la Row.**

### 4.2 Cause probable : **Clipping par le conteneur parent (fillMaxHeight 0.35f)**

Le conteneur header dans `PlayerOverlayLayout` est limité à **35% de la hauteur de l'écran** :
- Sur 1080dp : 35% = **378dp**
- Sur 720dp : 35% = **252dp**

Moins le padding overscan vertical (27dp) et le padding top PlayerHeader (32dp), il reste **~319dp** (1080) / **~193dp** (720) de hauteur.

Le contenu vertical de la colonne gauche (pire cas avec série) :
- Nom de la série : 13sp ≈ 16dp
- Titre principal : 36sp lineHeight = 36dp (≈ 48dp avec ascender)
- Info épisode : 13sp + 4dp padding top ≈ 20dp
- Badges : 8dp padding top + ~20dp badge height = **28dp**

**Total** ≈ **112dp** — ça rentre, même sur 720p.

### 4.3 Cause la plus probable : **Troncature visuelle perçue, pas réelle**

Étant donné que :
1. La Row de badges n'a **aucune contrainte** qui pourrait la tronquer dans les conditions normales
2. Le `Text` dans `TechBadge` a `overflow = TextOverflow.Clip` par défaut mais **aucun maxWidth** n'est appliqué
3. Il n'y a **pas de `Modifier.clipToBounds()`** explicite

La troncature perçue pourrait venir de :
- **Le `graphicsLayer { compositingStrategy = Offscreen }` dans `ui.base.Text`** — Ce modifier force le rendu dans un layer offscreen, ce qui peut causer un clipping inattendu si le layer est dimensionné différemment du composable parent
- **Le gradient du Box parent** (`Brush.verticalGradient` de 0.9 alpha à transparent à 35%) qui fait **disparaître visuellement** les badges s'ils sont positionnés trop bas dans la zone de gradient
- **Le `fillMaxHeight(0.35f)` du Box header** combiné avec l'absence de `clipToBounds(false)` — par défaut les Box clippent leur contenu

### 4.4 Scénario de troncature latérale possible

Si la colonne droite (horloge) prend plus d'espace que prévu (par exemple avec une locale qui formate l'heure plus longue, ou avec le texte "Fin à" traduit dans une langue verbose), la colonne gauche avec `weight(1f)` se réduit proportionnellement. La Row de badges n'ayant **aucun mécanisme de scroll ni de wrap**, les derniers badges seraient simplement **clippés par le bord droit de la Column parent**.

---

## 5. Problèmes identifiés

| # | Sévérité | Problème | Localisation |
|---|----------|----------|-------------|
| 1 | **MOYENNE** | La Row de badges n'a **aucun mécanisme de gestion de l'overflow** (pas de scroll, pas de LazyRow, pas de FlowRow). Si l'espace est insuffisant, les badges à droite sont simplement clippés silencieusement | `VideoPlayerHeader.kt:149-157` |
| 2 | **MOYENNE** | `Text` dans `TechBadge` utilise le défaut `overflow = TextOverflow.Clip` — si un badge individuel est compressé, le texte est **coupé net** sans indication visuelle (pas d'ellipsis) | `VideoPlayerHeader.kt:219-227` via `Text.kt:38` |
| 3 | **BASSE** | `softWrap = true` par défaut dans TechBadge — un badge compressé pourrait théoriquement wrapper son texte sur 2 lignes, cassant le layout vertical du badge | `VideoPlayerHeader.kt:219` via `Text.kt:39` |
| 4 | **INFO** | Le `graphicsLayer(compositingStrategy = Offscreen)` dans `ui.base.Text` peut interférer avec le rendu des petits textes à 10sp dans les badges, causant un anti-aliasing dégradé ou un clipping subtil au niveau sous-pixel | `Text.kt:49` |
| 5 | **INFO** | Le gradient vertical du conteneur header (0.90α → transparent à 35%) peut rendre les badges visuellement "effacés" s'ils sont positionnés dans la zone de transition du gradient | `PlayerOverlayLayout.kt:149-153` |
| 6 | **INFO** | Aucun `maxLines = 1` ni `softWrap = false` sur le Text des badges — comportement dépendant de la taille disponible | `VideoPlayerHeader.kt:219-227` |

---

## 6. Recommandations (non implémentées)

1. **Ajouter `softWrap = false` et `maxLines = 1`** au Text dans TechBadge pour garantir un rendu single-line
2. **Ajouter `Modifier.horizontalScroll(rememberScrollState())`** à la Row de badges pour permettre le scroll en cas d'overflow
3. **Ou utiliser `FlowRow`** si on préfère un wrap sur 2 lignes plutôt qu'un scroll
4. **Considérer `overflow = TextOverflow.Ellipsis`** dans TechBadge comme filet de sécurité
5. **Vérifier visuellement** le positionnement des badges par rapport au gradient de fond — éventuellement remonter les badges ou étendre la zone opaque du gradient

---

## 7. Schéma de la hiérarchie complète

```
PlayerOverlayLayout
  └─ Box(fillMaxSize)
       └─ AnimatedVisibility(TopCenter)
            └─ Box(fillMaxWidth, fillMaxHeight=0.35, gradient + overscan)   ← CLIP BOUNDARY
                 └─ PlayerHeader
                      └─ Row(fillMaxWidth, padding H=56dp, SpaceBetween)
                           ├─ Column(weight=1f)                            ← ESPACE FLEXIBLE
                           │    ├─ Text(seriesName)        13sp, ellipsis
                           │    ├─ Text(title)             36sp, BebasNeue, ellipsis
                           │    ├─ Text(episodeInfo)       13sp, ellipsis
                           │    └─ Row(spacedBy=6dp)       ← BADGES ROW (wrap-content, PAS de scroll)
                           │         ├─ TechBadge(resolution)  RESOLUTION style
                           │         ├─ TechBadge(hdr)         HDR style
                           │         ├─ TechBadge(videoCodec)  TECH style
                           │         ├─ TechBadge(container)   TECH style
                           │         ├─ TechBadge(audioChans)  TECH style
                           │         └─ TechBadge(audioCodec)  TECH style
                           │
                           └─ Column(align=End)                            ← WRAP-CONTENT
                                ├─ Text(clock)             15sp monospace
                                └─ Text(endTime)           12sp
```
