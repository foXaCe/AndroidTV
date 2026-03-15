# H_FIX7 — Hero : position, rating officiel, format note

**Date** : 2026-03-11
**Statut** : Terminé — BUILD SUCCESSFUL debug + release

---

## Résumé

Trois corrections sur la zone hero de l'écran d'accueil :

1. **Bandeau hero trop bas** — Le weight de la zone hero passe de 0.52 à 0.56 (rows de 0.48 à 0.44), donnant plus d'espace vertical au hero info overlay pour que pills, description et boutons soient visibles sans empiéter sur les rows.

2. **Suppression de la pill restriction d'âge** — La pill `officialRating` (PG-13, TV-MA, 12+, 16, etc.) est retirée de `HeroPillsRow`. Elle ajoutait du bruit visuel sans valeur ajoutée sur Android TV.

3. **Format de la note communautaire** — Les notes entières n'affichent plus de décimale inutile (9.0 → "9", 7.0 → "7"), et le séparateur décimal est toujours un point (7.4 → "7.4", jamais "7,4").

---

## Fichiers modifiés

| Fichier | Changement |
|---------|------------|
| `app/.../ui/home/compose/HomeScreen.kt` | Hero weight 0.52→0.56, rows weight 0.48→0.44 |
| `app/.../ui/home/compose/HomeHeroBackdrop.kt` | Suppression pill officialRating, formatage note (entiers sans ".0", point décimal forcé) |

---

## Détail des changements

### HomeScreen.kt

- `Box(weight=0.56f)` — zone hero (était 0.52)
- `StateContainer(weight=0.44f)` — zone rows (était 0.48)
- Commentaire KDoc mis à jour

### HomeHeroBackdrop.kt — HeroPillsRow

**Avant** :
```kotlin
item.communityRating?.let { rating ->
    if (rating > 0f) add(PillData("★ ${String.format("%.1f", rating)}", highlight = true))
}
item.officialRating?.let { add(PillData(it)) }
```

**Après** :
```kotlin
item.communityRating?.let { rating ->
    if (rating > 0f) {
        val formatted = if (rating == rating.toLong().toFloat()) {
            rating.toLong().toString()
        } else {
            String.format("%.1f", rating).replace(",", ".")
        }
        add(PillData("★ $formatted", highlight = true))
    }
}
// officialRating pill removed
```

### Exemples de formatage note

| Valeur Float | Avant | Après |
|-------------|-------|-------|
| 9.0f | "★ 9,0" | "★ 9" |
| 7.4f | "★ 7,4" | "★ 7.4" |
| 8.5f | "★ 8,5" | "★ 8.5" |
| 10.0f | "★ 10,0" | "★ 10" |

---

## Build

```
./gradlew :app:compileGithubDebugKotlin   → BUILD SUCCESSFUL
./gradlew :app:compileGithubReleaseKotlin → BUILD SUCCESSFUL
```

0 erreur, 0 warning.
