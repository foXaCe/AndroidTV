# Assets VegafoX — Intégration

**Date:** 2026-03-09

## Sources (images/)

| Fichier source | Dims | Usage |
|---------------|------|-------|
| Asset 1 — Icône launcher (foreground layer).png | 1024x1024 RGBA | Renard glassmorphism (foreground) |
| Asset 2 — Banner Android TV (320x180).png | 1024x1024 RGBA | Banner TV (crop 16:9 centre) |
| Asset 3 — Splash screen / boot logo.png | 1024x1024 RGBA | Splash (renard + texte VegafoX) |
| Asset 4 — Fond d'écran login.png | 1024x1024 RGBA | Login background (crop 16:9 haut) |

## Assets générés

### Launcher foreground (Asset 1 → renard)
| Densité | Fichier | Dims |
|---------|---------|------|
| mdpi | `mipmap-mdpi/vegafox_launcher_foreground.webp` | 108x108 |
| hdpi | `mipmap-hdpi/vegafox_launcher_foreground.webp` | 162x162 |
| xhdpi | `mipmap-xhdpi/vegafox_launcher_foreground.webp` | 216x216 |
| xxhdpi | `mipmap-xxhdpi/vegafox_launcher_foreground.webp` | 324x324 |
| xxxhdpi | `mipmap-xxxhdpi/vegafox_launcher_foreground.webp` | 432x432 |

### Legacy launcher (Asset 1)
| Densité | `vegafox_launcher.webp` | `vegafox_launcher_round.webp` |
|---------|------------------------|-------------------------------|
| mdpi | 48x48 | 48x48 |
| hdpi | 72x72 | 72x72 |
| xhdpi | 96x96 | 96x96 |
| xxhdpi | 144x144 | 144x144 |
| xxxhdpi | 192x192 | 192x192 |

### App icon (Asset 1 → remplace triangle Jellyfin)
| Densité | `app_icon.png` |
|---------|---------------|
| mdpi | 80x80 |
| hdpi | 120x120 |
| xhdpi | 160x160 |
| xxhdpi | 240x240 |
| xxxhdpi | 320x320 |

### Banner TV (Asset 2 → crop 16:9)
| Densité | `app_banner.png` |
|---------|-----------------|
| mdpi | 320x180 |
| hdpi | 480x270 |
| xhdpi | 640x360 |
| xxhdpi | 960x540 |
| xxxhdpi | 1280x720 |

### Channel / Banner spécifiques
| Fichier | Dims |
|---------|------|
| `mipmap-xhdpi/vegafox_ic_banner.png` | 640x360 |
| `mipmap-xhdpi/vegafox_ic_banner_foreground.png` | 640x360 |
| `mipmap-xhdpi/vegafox_ic_channel.png` | 160x160 |
| `mipmap-xhdpi/vegafox_channel_foreground.png` | 480x480 |

### Splash & Login
| Fichier | Dims |
|---------|------|
| `drawable/vegafox_splash.png` | 1920x1080 |
| `drawable/vegafox_login_background.png` | 1920x1080 |

### Play Store
| Fichier | Dims |
|---------|------|
| `app/src/main/vegafox_launcher-playstore.png` | 512x512 |

## XML modifiés

| Fichier | Avant | Après |
|---------|-------|-------|
| `drawable/ic_vegafox.xml` | Paths SVG Jellyfin | Bitmap → vegafox_launcher_foreground |
| `drawable/ic_vegafox_white.xml` | Paths SVG Jellyfin blanc | Bitmap → vegafox_launcher_foreground |
| `drawable/app_icon_foreground.xml` | Triangle Jellyfin vector | Bitmap → vegafox_launcher_foreground |
| `drawable/app_icon_background.xml` | Fond #000B25 vector | Shape solid #0A0A0F |
| `drawable/app_icon_foreground_monochrome.xml` | Triangle Jellyfin | Bitmap tinted |
| `drawable/app_banner_foreground.xml` | Triangle+texte Jellyfin | Bitmap → app_banner |
| `drawable/app_banner_background.xml` | Fond #000B25 | Shape solid #0A0A0F |
| `drawable/vegafox_launcher_background.xml` | Grille verte #3DDC84 | Shape solid #0A0A0F |
| `drawable/vegafox_ic_channel_background.xml` | Grille verte #3DDC84 | Shape solid #0A0A0F |
| `values/vegafox_ic_banner_background.xml` | #FFFFFF | #0A0A0F |
| `values/colors.xml` | — | + vegafox_launcher_background=#0A0A0F |

## Vérification
- 29/29 assets dimensionnés correctement
- BUILD SUCCESSFUL (assembleGithubDebug)
- Aucun path SVG Jellyfin restant dans les drawable VegafoX
