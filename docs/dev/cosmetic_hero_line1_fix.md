# Fix Hero TagLine — EPISODE + PROGRAM

## Fichier modifié
`app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeHeroBackdrop.kt`
Fonction `buildHeroTagLine()`

## Avant / Après

### EPISODE

**Avant :**
```
ÉPISODE · S2E2
```

**Après :**
```
MONARCH : L'HÉRITAGE DES MONSTRES · S2E2
```
→ Affiche `item.seriesName` en uppercase, fallback sur "ÉPISODE" si null.

### PROGRAM (nouveau cas)

**Avant :**
```
PROGRAM · DRAME
```
→ Tombait dans le `else`, affichait le nom brut du type.

**Après :**
```
NOM SÉRIE · S1E3       (si seriesName != null)
TV EN DIRECT · DRAME   (si seriesName == null)
```

### MOVIE (inchangé)
```
FILM · AVENTURE
```

## Code

```kotlin
BaseItemKind.EPISODE -> {
    val seriesTitle = item.seriesName
        ?.uppercase()
        ?: "ÉPISODE"
    parts.add(seriesTitle)
    val s = item.parentIndexNumber
    val e = item.indexNumber
    if (s != null && e != null) parts.add("S${s}E$e")
}
BaseItemKind.PROGRAM -> {
    val seriesTitle = item.seriesName
        ?.uppercase()
    if (seriesTitle != null) {
        parts.add(seriesTitle)
        val s = item.parentIndexNumber
        val e = item.indexNumber
        if (s != null && e != null) parts.add("S${s}E$e")
    } else {
        parts.add("TV EN DIRECT")
        genre?.let { parts.add(it) }
    }
}
```

## Build
- `assembleDebug` : OK
- `assembleGithubRelease` : OK
- APKs installés sur AM9 Pro (192.168.1.152)

## Vérification logs
```bash
adb -s 192.168.1.152:5555 logcat -s "Timber" | grep "HeroLine1"
```
→ À confirmer manuellement après navigation sur l'écran d'accueil.
