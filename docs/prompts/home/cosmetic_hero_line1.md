# Hero Ligne 1 & Ligne 2 — Règle unifiée

## Fichier modifié

`app/src/main/java/org/jellyfin/androidtv/ui/home/compose/HomeHeroBackdrop.kt`

## Changements

### Ligne 1 — Tag line (orange, caps, petite)

**Avant** :
- MOVIE → "FILM · {genre}"
- SERIES → "SÉRIE · {genre}"
- EPISODE → "{seriesName} · S{s}E{e}"
- PLAYLIST / MUSIC_ALBUM → masqué
- Autre → genre uniquement (ou rien)

**Après** :
| Type | Format | Exemple |
|------|--------|---------|
| MOVIE | `FILM · {genre[0]}` | FILM · THRILLER |
| SERIES | `SÉRIE · {genre[0]}` | SÉRIE · SCIENCE FICTION |
| EPISODE | `ÉPISODE · S{s}E{e}` | ÉPISODE · S4E2 |
| TV_CHANNEL | `TV EN DIRECT` | TV EN DIRECT |
| PLAYLIST | *(masqué)* | |
| MUSIC_ALBUM | `MUSIQUE` | MUSIQUE |
| Autre (ex: PROGRAM) | `{kind.name}` | PROGRAM |

- Fallback genre : `item.genres?.firstOrNull() ?: item.genreItems?.firstOrNull()?.name`
- Si genre null/vide → type seul sans séparateur

### Ligne 2 — Titre (BebasNeue, grande)

**Avant** : `item.name`, fontSize=26sp, FontWeight.W800, police par défaut

**Après** : `item.name`, fontSize=32sp, FontWeight.Normal, **BebasNeue**, letterSpacing=1sp

La ligne 2 est TOUJOURS `item.name` — jamais le nom de série parente ni de saison.

## Corrections associées (même session)

### Pill play/reprendre — fix complet

Voir `docs/dev/cosmetic_pill_fix.md` pour les détails complets (gap, animation premium, troncature LazyRow, reprise position).

## Screenshots

| Contexte | Fichier |
|----------|---------|
| Hero — Film (Thriller) | `docs/dev/audit_screens/hero_film.png` |
| Hero — Épisode (Monarch S2E2) | `docs/dev/audit_screens/hero_episode.png` |
| Hero — Série (Science Fiction) | `docs/dev/audit_screens/hero_series.png` |
| Hero — Programme TV | `docs/dev/audit_screens/hero_tv_program.png` |
| Pill "Recommencer" visible | `docs/dev/audit_screens/hero_pill_recommencer.png` |

## Build

- Debug GitHub : OK
- Release GitHub : OK
- Installé sur AM9 Pro (192.168.1.152) : OK
