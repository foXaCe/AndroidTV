# Live TV DirectStream & DeviceProfile — Audit

**Date** : 2026-03-13
**Statut** : Aucune modification requise — les trois correctifs sont déjà en place.

---

## CORRECTIF 1 — enableDirectStream Live TV

### Logique actuelle (`PlaybackController.kt:712-722`)

```kotlin
if (isLiveTv) {
    // DirectPlay disabled si user l'a désactivé OU si retry
    if (!directStreamLiveTv || playbackRetries > 0) internalOptions.enableDirectPlay = false
    // DirectStream disabled seulement en dernier recours (2e retry)
    if (playbackRetries > 1) internalOptions.enableDirectStream = false
} else {
    if (playbackRetries > 0) internalOptions.enableDirectPlay = false
    if (playbackRetries > 1) internalOptions.enableDirectStream = false
}
```

### Comportement garanti

| `directStreamLiveTv` | `playbackRetries` | DirectPlay | DirectStream | Résultat          |
|-----------------------|-------------------|------------|--------------|-------------------|
| `true`                | 0                 | ON         | ON           | DirectPlay        |
| `true`                | 1                 | OFF        | ON           | DirectStream      |
| `false`               | 0                 | OFF        | ON           | DirectStream      |
| `false`               | 1                 | OFF        | ON           | DirectStream      |
| any                   | 2+                | OFF        | OFF          | Transcoding       |

`AudioOptions.enableDirectStream` est initialisé à `true` (`AudioOptions.kt:9`).
Le code ne l'écrase jamais sauf si `playbackRetries > 1`.

### Gestion d'erreur Live TV (`PlaybackController.kt:1581-1583`)

```kotlin
if (isLiveTv && directStreamLiveTv) {
    Utils.showToast(frag.context, frag.getString(R.string.msg_error_live_stream))
    directStreamLiveTv = false  // Fallback: désactive DirectPlay pour retry en DirectStream
}
```

**Verdict** : La logique est correcte. DirectStream est toujours garanti comme fallback pour Live TV.

---

## CORRECTIF 2 — Valeur par défaut `directStreamLiveTv`

### Définition (`UserPreferences.kt:244`)

```kotlin
var liveTvDirectPlayEnabled = booleanPreference("pref_live_direct", true)
```

**Valeur par défaut : `true`** (DirectPlay activé par défaut pour Live TV).

### Chargement (`PlaybackController.kt:290`)

```kotlin
directStreamLiveTv = userPreferences[UserPreferences.liveTvDirectPlayEnabled]
```

**Verdict** : La valeur par défaut est déjà `true`. Sur AM9 Pro (Android 14, ExoPlayer + MPEG-TS H264/HEVC natif), DirectPlay fonctionnera directement.

---

## CORRECTIF 3 — DeviceProfile containers TS

### DirectPlayProfiles vidéo (`deviceProfile.kt:205-223`)

```kotlin
directPlayProfile {
    type = DlnaProfileType.VIDEO
    container(
        Codec.Container.ASF,
        Codec.Container.HLS,
        Codec.Container.M4V,
        Codec.Container.MKV,
        Codec.Container.MOV,
        Codec.Container.MP4,
        Codec.Container.MPEGTS,   // <-- présent
        Codec.Container.OGM,
        Codec.Container.OGV,
        Codec.Container.TS,       // <-- présent
        Codec.Container.VOB,
        Codec.Container.WEBM,
        Codec.Container.WMV,
        Codec.Container.XVID,
    )
    // ...
}
```

**Verdict** : `TS` et `MPEGTS` sont tous deux déclarés dans les DirectPlayProfiles. Le serveur Jellyfin autorisera le DirectPlay/DirectStream pour les flux MPEG-TS.

### TranscodingProfile Live TV (`deviceProfile.kt:195-201`)

```kotlin
container = Codec.Container.TS
protocol = MediaStreamProtocol.HLS
```

Le profil de transcodage Live TV cible déjà le container TS via HLS, cohérent avec l'architecture.

---

## Résumé

| Correctif | Fichier | État | Modification |
|-----------|---------|------|-------------|
| DirectStream fallback | `PlaybackController.kt:712-722` | OK | Aucune |
| Default `liveTvDirectPlayEnabled` | `UserPreferences.kt:244` | `true` | Aucune |
| Container TS dans DirectPlayProfile | `deviceProfile.kt:215,218` | Présent | Aucune |

**LOC modifiées : 0** — Le code implémente déjà correctement la stratégie de fallback Live TV.
