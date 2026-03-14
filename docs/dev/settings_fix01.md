# Fix crash ouverture Settings

**Date** : 2026-03-12
**Branche** : main

---

## Cause exacte

**Exception** :
```
java.lang.IllegalArgumentException: Only VectorDrawables and rasterized asset types are supported ex. PNG, JPG, WEBP
    at SettingsMainScreen.kt:68
```

**Analyse** : `R.drawable.ic_vegafox` est defini dans `ic_vegafox.xml` comme un `<bitmap>` wrapper :
```xml
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/vegafox_launcher_foreground"
    android:gravity="center" />
```

Ce type de drawable (`BitmapDrawable` via XML) n'est **pas supporte** par `painterResource()` de Compose, qui n'accepte que :
- VectorDrawable (`<vector>`)
- Images raster directes (PNG, JPG, WEBP)

Le crash se produisait des l'ouverture du `SettingsDialog` car `SettingsMainScreen` tentait de charger ce drawable a la ligne 68.

---

## Fix applique

Remplacement de `R.drawable.ic_vegafox` par `R.drawable.ic_vegafox_fox` (PNG 1024x1024 du logo VegafoX) dans **5 fichiers** :

| Fichier | Lignes |
|---------|--------|
| `ui/settings/screen/SettingsMainScreen.kt` | 68 |
| `ui/settings/screen/about/SettingsAboutScreen.kt` | 42 |
| `ui/settings/screen/vegafox/SettingsPluginScreen.kt` | 49 |
| `ui/settings/screen/vegafox/SettingsJellyseerrScreen.kt` | 100, 120 |
| `ui/shared/toolbar/Toolbar.kt` | 40 |

Total : 6 occurrences corrigees.

---

## Verification

- App lancee en debug sur Ugoos AM9 Pro (192.168.1.152)
- Settings ouverts depuis la sidebar sans crash
- Navigation vers Playback (Lecture) OK
- BUILD SUCCESSFUL debug + release

---

## Screenshots

- `docs/screenshots/settings_main.png` — Ecran principal settings (Parametres)
- `docs/screenshots/settings_playback.png` — Ecran Playback (Lecture)

---

## Note cosmétique

L'icone `ic_vegafox_fox.png` (1024x1024) est affichee dans les `Icon()` de ListButton. Elle est un peu grande mais ne cause pas de probleme fonctionnel — le composant `Icon` la contraint. Un asset plus petit (48dp/96px) serait ideal pour un polish futur.
