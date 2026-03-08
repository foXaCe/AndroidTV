# Audit 16 — Traductions Jellyseerr (textes hardcodes restants)

> **Date : 2026-03-08 — etat post-travaux**
> ✅ **100% resolu** — 26 textes hardcodes externalises :
> - 22 statuts HD/4K dans MediaDetailsFragment.kt
> - 3 messages toast/dialogue dans MediaDetailsFragment.kt
> - 1 texte auth dans SettingsJellyseerrScreen.kt (reutilisation string existante)
> - 2 badges type media dans ItemCardJellyseerrOverlay.kt (MOVIE reutilise, SERIES cree)
> - 1 message erreur bonus dans MediaDetailsFragment.kt (reutilisation string existante)

---

## 1. Contexte

Apres l'audit 03 (traductions), 24 textes hardcodes en anglais restaient dans les fichiers Jellyseerr.
Ces textes etaient visibles par l'utilisateur et cassaient la coherence francaise de l'app.

---

## 2. Fichiers modifies

| Fichier | Modifications |
|---------|---------------|
| `res/values/strings.xml` | +23 strings, +2 plurals, +1 string (lbl_series_type_upper) |
| `res/values-fr/strings.xml` | +23 strings, +2 plurals, +1 string (lbl_series_type_upper) |
| `ui/jellyseerr/MediaDetailsFragment.kt` | 22 statuts + 3 toasts + 1 dialogue + 1 erreur externalises |
| `ui/settings/screen/moonfin/SettingsJellyseerrScreen.kt` | 1 texte remplace par string existante |
| `ui/composable/item/ItemCardJellyseerrOverlay.kt` | 2 badges remplaces par stringResource |

---

## 3. Table complete des changements

### 3.1 Statuts HD/4K (badges overlay — MediaDetailsFragment.kt)

| Cle string | EN | FR | Couleur |
|------------|----|----|---------|
| `jellyseerr_status_hd_4k_declined` | HD + 4K DECLINED | HD + 4K REFUSÉ | ds_error |
| `jellyseerr_status_4k_declined` | 4K DECLINED | 4K REFUSÉ | ds_error |
| `jellyseerr_status_hd_declined` | HD DECLINED | HD REFUSÉ | ds_error |
| `jellyseerr_status_hd_4k_blacklisted` | HD + 4K BLACKLISTED | HD + 4K BLOQUÉ | ds_error |
| `jellyseerr_status_4k_blacklisted` | 4K BLACKLISTED | 4K BLOQUÉ | ds_error |
| `jellyseerr_status_hd_blacklisted` | HD BLACKLISTED | HD BLOQUÉ | ds_error |
| `jellyseerr_status_hd_4k_available` | HD + 4K AVAILABLE | HD + 4K DISPONIBLE | ds_success |
| `jellyseerr_status_4k_available` | 4K AVAILABLE | 4K DISPONIBLE | ds_success |
| `jellyseerr_status_hd_available` | HD AVAILABLE | HD DISPONIBLE | ds_success |
| `jellyseerr_status_hd_4k_partial` | HD + 4K PARTIAL | HD + 4K PARTIEL | ds_success |
| `jellyseerr_status_4k_partial` | 4K PARTIAL | 4K PARTIEL | ds_success |
| `jellyseerr_status_hd_partial` | HD PARTIAL | HD PARTIEL | ds_success |
| `jellyseerr_status_hd_4k_processing` | HD + 4K PROCESSING | HD + 4K EN COURS | ds_info |
| `jellyseerr_status_4k_processing` | 4K PROCESSING | 4K EN COURS | ds_info |
| `jellyseerr_status_hd_processing` | HD PROCESSING | HD EN COURS | ds_info |
| `jellyseerr_status_hd_4k_pending` | HD + 4K PENDING | HD + 4K EN ATTENTE | ds_warning |
| `jellyseerr_status_4k_pending` | 4K PENDING | 4K EN ATTENTE | ds_warning |
| `jellyseerr_status_hd_pending` | HD PENDING | HD EN ATTENTE | ds_warning |
| `jellyseerr_status_hd_4k_unknown` | HD + 4K UNKNOWN | HD + 4K INCONNU | ds_text_secondary |
| `jellyseerr_status_4k_unknown` | 4K UNKNOWN | 4K INCONNU | ds_text_secondary |
| `jellyseerr_status_hd_unknown` | HD UNKNOWN | HD INCONNU | ds_text_secondary |
| `jellyseerr_status_not_requested` | NOT REQUESTED | NON DEMANDÉ | ds_text_disabled |

### 3.2 Messages toast/dialogue (MediaDetailsFragment.kt)

| Cle string | EN | FR | Contexte |
|------------|----|----|----------|
| `jellyseerr_no_quality_server` | No %1$s server configured for %2$s in Jellyseerr. Please contact your administrator. | Aucun serveur %1$s configure pour les %2$s dans Jellyseerr. Contacte ton administrateur. | Toast erreur serveur manquant |
| `jellyseerr_quality_hd_full` | HD (1080p) | HD (1080p) | Parametre %1$s du toast ci-dessus |
| `jellyseerr_media_movies` | movies | films | Parametre %2$s du toast ci-dessus |
| `jellyseerr_media_tv_shows` | TV shows | series TV | Parametre %2$s du toast ci-dessus |
| `jellyseerr_request_submitted` | %1$s request%2$s submitted successfully! | Demande %1$s%2$s envoyee ! | Toast succes demande |
| `jellyseerr_all_seasons_suffix` | (All seasons) | (Toutes les saisons) | Parametre %2$s du toast succes |
| `jellyseerr_season_count_suffix` (plurals) | (%1$d season) / (%1$d seasons) | (%1$d saison) / (%1$d saisons) | Parametre %2$s du toast succes |
| `jellyseerr_request_error` | Request failed: %1$s | Echec de la demande : %1$s | Toast erreur demande |
| `jellyseerr_cancel_and_separator` | " and " | " et " | Separateur dans dialogue annulation |
| `jellyseerr_cancel_confirm` (plurals) | Cancel %1$s request/requests for "%2$s"? | Annuler la/les demande(s) %1$s pour « %2$s » ? | Dialogue confirmation annulation |

### 3.3 Authentification (SettingsJellyseerrScreen.kt)

| Avant (hardcode) | Apres (string resource) | Notes |
|-------------------|------------------------|-------|
| `"Cookie-based auth (expires ~30 days)"` | `stringResource(R.string.jellyseerr_api_key_absent)` | Reutilisation de la string existante |

### 3.4 Badges type media (ItemCardJellyseerrOverlay.kt)

| Avant (hardcode) | Apres (string resource) | EN | FR |
|-------------------|------------------------|----|----|
| `"MOVIE"` | `stringResource(R.string.lbl_movie_type_upper)` | MOVIE | FILM |
| `"SERIES"` | `stringResource(R.string.lbl_series_type_upper)` | SERIES | SÉRIE |

`lbl_movie_type_upper` existait deja. `lbl_series_type_upper` a ete cree.

### 3.5 Erreur bonus (MediaDetailsFragment.kt)

| Avant (hardcode) | Apres (string resource) | Notes |
|-------------------|------------------------|-------|
| `"Error: ${e.message}"` | `getString(R.string.jellyseerr_error_generic, ...)` | Reutilisation string existante (cancel request catch) |

---

## 4. Decisions de traduction

### Statuts courts pour badges overlay
Les statuts s'affichent en overlay sur les cartes Jellyseerr (10sp, bold, fond colore).
Contrainte : max 3 mots pour rester lisible a distance TV.

| EN | FR | Mots FR |
|----|----|---------|
| DECLINED | REFUSÉ | 1 |
| BLACKLISTED | BLOQUÉ | 1 |
| AVAILABLE | DISPONIBLE | 1 |
| PARTIAL | PARTIEL | 1 |
| PROCESSING | EN COURS | 2 |
| PENDING | EN ATTENTE | 2 |
| UNKNOWN | INCONNU | 1 |
| NOT REQUESTED | NON DEMANDÉ | 2 |

Le plus long : "HD + 4K EN ATTENTE" = 3 unites (HD+4K, EN, ATTENTE). Respecte la contrainte.

### Tutoiement
Coherent avec le reste de l'app : "Contacte ton administrateur" (pas "Contactez votre").

### Guillemets francais
Les dialogues FR utilisent « » au lieu de "" : `« %2$s »` au lieu de `\"%2$s\"`.

### "4K" et "HD" non traduits
Ce sont des abreviations internationales identiques dans toutes les langues.

---

## 5. Textes hardcodes restants (hors scope)

Les textes suivants dans SettingsJellyseerrScreen.kt restent hardcodes mais sont hors du scope
defini (ils n'etaient pas dans les 24 textes cibles) :

| Ligne | Texte | Contexte |
|-------|-------|----------|
| ~661 | `"permanent API key"` | Label auth type dans toast |
| ~663 | `"cookie-based auth (expires ~30 days)"` | Label auth type dans toast |
| ~671 | `"Server Configuration Error\n\n..."` | Toast erreur config |
| ~674 | `"Authentication Failed\n\n..."` | Toast erreur auth |
| ~676 | `"Connection failed: ..."` | Toast erreur connexion |
| ~705 | `"Logged in successfully using cookie-based auth..."` | Toast succes login |

**Impact** : 6 textes restants, tous dans le flow de login/configuration Jellyseerr avance.
Priorite faible car rarement vus par l'utilisateur final.

---

## 6. Impact

| Metrique | Avant | Apres |
|----------|-------|-------|
| Textes hardcodes EN dans Jellyseerr (scope) | 26 | 0 |
| Textes hardcodes EN dans Jellyseerr (total) | ~32 | 6 (hors scope, settings avance) |
| Nouvelles strings ajoutees (EN) | — | 26 |
| Nouvelles strings ajoutees (FR) | — | 26 |
| Plurals ajoutes | — | 2 |
| Strings existantes reutilisees | — | 3 |
