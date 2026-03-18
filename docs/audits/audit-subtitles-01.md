# Audit Subtitles — Option couleur sous-titres

**Date** : 2026-03-16
**Scope** : écran de personnalisation des sous-titres, préférences, rendu player

---

## Point 1 — Recherche de l'option couleur sous-titres dans les Settings

### Fichiers trouvés dans `ui/settings/`

| Fichier | Contenu subtitle-related |
|---------|-------------------------|
| `screen/customization/subtitle/SettingsSubtitlesScreen.kt` | Écran principal : taille texte, poids, couleur texte, couleur fond, couleur contour, offset vertical |
| `screen/customization/subtitle/SettingsSubtitlesTextColorScreen.kt` | Sélecteur couleur texte (presets + RGB custom) |
| `screen/customization/subtitle/SettingsSubtitlesBackgroundColorScreen.kt` | Sélecteur couleur fond (presets + RGBA custom) |
| `screen/customization/subtitle/SettingsSubtitleTextStrokeColorScreen.kt` | Sélecteur couleur contour (presets + RGBA custom) |
| `screen/customization/subtitle/composable/SubtitleStylePreview.kt` | Preview live via Media3 `SubtitleView` + `CaptionStyleCompat` |
| `screen/customization/subtitle/composable/SubtitleColorPresetsControl.kt` | Grille de 9 presets couleur |
| `screen/customization/subtitle/presets.kt` | Définition des presets : White, Black, Gray, Red, Green, Blue, Yellow, Magenta, Cyan |
| `util/ListColorChannelRangeControl.kt` | Composant slider RGB(A) générique utilisé par les color pickers |
| `routes.kt` (lignes 23-26) | Routes `/customization/subtitles`, `/customization/subtitles/text-color`, `/customization/subtitles/background-color`, `/customization/subtitles/edge-color` |
| `routes/CustomizationRoutes.kt` (lignes 54-67) | Mapping routes → composables |
| `screen/playback/SettingsPlaybackScreen.kt` (lignes 107-114) | **BOUTON COMMENTÉ** avec TODO erroné |

### Route `/customization/subtitles` — statut lien mort

La route **existe** et est enregistrée dans `CustomizationRoutes.kt`. Cependant le bouton d'accès dans `SettingsPlaybackScreen.kt` est **commenté** (lignes 107-114) avec un TODO incorrect :

```kotlin
// TODO: Subtitles screen needs to be recreated (SettingsSubtitlesScreen.kt not found)
```

Le fichier existe bien — le commentaire est **erroné**. L'écran est inaccessible depuis l'UI mais le code fonctionne.

---

## Point 2 — Recherche du fichier SettingsSubtitlesScreen

### Le fichier EXISTE

**Chemin** : `app/src/main/java/org/jellyfin/androidtv/ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt` (213 lignes)

### Historique de migration

| Date | Commit | Action |
|------|--------|--------|
| 2025-12-21 | `3fbc3a35c` | Migration Compose : suppression de `ui/preference/screen/SubtitlePreferencesScreen.kt` (Leanback) → création de `ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt` (Compose) |
| 2026-03-14 | `7f0ecbfb5` | Refactoring settings — bouton commenté dans `SettingsPlaybackScreen.kt` avec TODO erroné |

L'écran n'a **jamais été supprimé**. L'ancien écran Leanback (`SubtitlePreferencesScreen.kt`) a été remplacé par la version Compose. Le TODO commentant le bouton dans Playback Settings est incorrect.

### Fichiers supprimés (historique git) — tous remplacés ou obsolètes

| Fichier supprimé | Commit | Raison |
|------------------|--------|--------|
| `ui/preference/screen/SubtitlePreferencesScreen.kt` | `3fbc3a35c` | Migré vers Compose |
| `ui/playback/SubtitleDelayHandler.java` | `7f0ecbfb5` | Réécrit en Kotlin |
| `ui/playback/overlay/action/SubtitleDelayAction.kt` | `e81f5efef` | Rebrand VegafoX |
| `data/compat/SubtitleStreamInfo.kt` | `e33d85c4f` | Absorbé par SDK |
| `constant/SubtitleTypes.kt` | `c90539b4b` | Fusionné dans codec constants |
| Drawables `ic_select_subtitle.xml`, `ic_subtitle_sync.xml`, `ic_subtitles.xml`, `subtitle_background.xml` | `7f0ecbfb5` | Remplacés par `VegafoXIcons` |

---

## Point 3 — Préférences sous-titres existantes

### UserPreferences.kt (lignes 268-300)

| Propriété | Clé SharedPreferences | Type | Défaut | Description |
|-----------|----------------------|------|--------|-------------|
| `subtitlesBackgroundColor` | `subtitles_background_color` | Long | `0x00FFFFFF` (transparent) | Couleur de fond des sous-titres |
| `subtitlesTextWeight` | `subtitles_text_weight` | Int | `400` | Poids police (100-900) |
| `subtitlesTextColor` | `subtitles_text_color` | Long | `0xFFFFFFFF` (blanc opaque) | Couleur du texte |
| `subtitleTextStrokeColor` | `subtitles_text_stroke_color` | Long | `0xFF000000` (noir opaque) | Couleur du contour/edge |
| `subtitlesTextSize` | `subtitles_text_size` | Float | `24f` | Taille police (DIP, range 8-32) |
| `subtitlesOffsetPosition` | `subtitles_offset_position` | Float | `0.08f` | Décalage vertical (0.0-0.8) |
| `subtitlesDefaultToNone` | `subtitles_default_to_none` | Boolean | `false` | Forcer sous-titres Off au démarrage |

### Préférences liées (playback avancé)

| Propriété | Clé | Type | Défaut | Description |
|-----------|-----|------|--------|-------------|
| `pgsDirectPlay` | `pgs_enabled` | Boolean | `true` | Lecture directe sous-titres PGS |
| `assDirectPlay` | `ass_enabled` | Boolean | `false` | Rendu client ASS/SSA (LibAss) |

### Migrations de préférences

- **v0.17→0.18** : `subtitles_background_enabled` (bool) → `subtitles_background_color` (long) ; `subtitles_stroke_size` → `subtitles_text_stroke_color` (long)
- **v0.19→0.20** : Reset de `subtitles_text_size` (passage fractionnaire → absolu)

---

## Point 4 — Application dans le player

### Architecture de rendu des sous-titres

```
VideoPlayerScreen.kt
  └─ PlayerSubtitles (Composable)
       └─ AndroidView wrapping PlayerSubtitleView (FrameLayout)
            └─ Media3 SubtitleView (ajoutée par ExoPlayerBackend)
                 └─ CaptionStyleCompat (couleurs, police, contour)
```

### Chaîne d'application des préférences couleur

1. **`VideoManager.kt` (lignes 115-133)** — Lecture des préférences et construction du style :
   ```kotlin
   val subtitleStyle = CaptionStyleCompat(
       userPreferences[UserPreferences.subtitlesTextColor].toInt(),      // foreground
       userPreferences[UserPreferences.subtitlesBackgroundColor].toInt(), // background
       Color.TRANSPARENT,                                                 // window color
       if (alpha(strokeColor) == 0) EDGE_TYPE_NONE else EDGE_TYPE_OUTLINE, // edge type
       strokeColor,                                                        // edge color
       TypefaceCompat.create(activity, Typeface.DEFAULT, textWeight, false), // typeface
   )
   mCustomSubtitleView.setStyle(subtitleStyle)
   mCustomSubtitleView.setFixedTextSize(COMPLEX_UNIT_DIP, subtitlesTextSize)
   mCustomSubtitleView.setBottomPaddingFraction(subtitlesOffsetPosition)
   ```

2. **`ExoPlayerBackend.kt` (lignes 210-221)** — Attachement de la SubtitleView :
   - Crée `SubtitleView(context)` de Media3
   - L'ajoute au `PlayerSubtitleView` (FrameLayout)
   - Reçoit les cues via `onCues(CueGroup)` (ligne 174)

3. **`PlaybackModule.kt` (lignes 78-85)** — Configuration ExoPlayer :
   - `enableLibAssRenderer` ← `UserPreferences.assDirectPlay`
   - `assSubtitleFontScale` ← `subtitlesTextSize / 24f`

### Mécanisme de rendu

- **Sous-titres texte (SRT, VTT, TTML)** : Media3 `SubtitleView` + `CaptionStyleCompat` — couleurs appliquées directement
- **Sous-titres ASS/SSA** : Rendu via LibAss (OpenGL overlay) si `assDirectPlay=true`, sinon via Media3
- **Sous-titres bitmap (PGS, VOBSUB)** : Rendu natif Media3 si `pgsDirectPlay=true`, sinon transcode côté serveur
- **Délai/sync** : `SubtitleDelayHandler.kt` intercepte `onCues()` et retarde l'affichage

### Preview dans les Settings

`SubtitleStylePreview.kt` utilise le **même** mécanisme (`SubtitleView` + `CaptionStyleCompat`) que le player, garantissant la fidélité de la prévisualisation.

---

## Point 5 — Historique des suppressions

### Aucune suppression de l'écran Subtitles lors des cleanups

Recherche effectuée dans tous les fichiers `docs/audits/` et `docs/cleanups/` :

| Fichier | Mentions subtitles | Contexte |
|---------|-------------------|----------|
| `audit_settings.md` (l.253-262) | Cat. 5, complexité B/M | Documente les 4 écrans subtitles comme existants |
| `settings_audit.md` (l.66-84) | Référence SubtitlePreferencesScreen | Audit antérieur à la migration Compose |
| `audit_player.md` (l.280-296) | Subtitle delay, track selection | Documentation du player |
| `cleanup_05.md` (l.92-95) | Mention subtitles | Nettoyage drawables (icônes supprimées, remplacées par VegafoXIcons) |
| `cleanup_06.md` (l.38) | Mention subtitles | Nettoyage mineur |

### Git log — commits clés subtitles

| Commit | Message |
|--------|---------|
| `3fbc3a35c` | "Migrate subtitle customization settings to compose" |
| `f9c219769` | "Add option to default subtitles to None, ignoring server settings" |
| `7f0ecbfb5` | "feat: hero trailer auto-play, UI overhaul, Java→Kotlin migration, settings refactor" — **c'est ici que le bouton a été commenté** |

---

## Diagnostic final

### Problème identifié

L'écran de personnalisation des sous-titres **existe et fonctionne** mais est **inaccessible** depuis l'UI :

- **`SettingsPlaybackScreen.kt:107-114`** — Le bouton "Subtitles" est commenté avec un TODO erroné : *"SettingsSubtitlesScreen.kt not found"*
- Le fichier existe à `ui/settings/screen/customization/subtitle/SettingsSubtitlesScreen.kt`
- Les routes sont enregistrées dans `CustomizationRoutes.kt`
- Les préférences sont lues et appliquées correctement dans le player

### Action requise

**Décommenter le bouton** dans `SettingsPlaybackScreen.kt` (lignes 107-114) et supprimer le TODO erroné. C'est un fix d'une seule ligne — tout le reste du pipeline (écrans, préférences, rendu player) est fonctionnel.

```kotlin
// Remplacer lignes 107-114 par :
item {
    ListButton(
        leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Subtitles), contentDescription = null) },
        headingContent = { Text(stringResource(R.string.pref_customization_subtitles)) },
        onClick = { router.push(Routes.CUSTOMIZATION_SUBTITLES) },
    )
}
```

### Documentation à mettre à jour

- `docs/NAVIGATION_MAP.md` : retirer l'anomalie ligne 189 — le lien n'est plus mort
- `docs/SCREENS_MAP.md` : retirer le statut "inconnu" de SettingsSubtitlesScreen
