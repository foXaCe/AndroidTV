# Detail v1 → v2 Migration — Suppression ItemDetailComposeFragment

**Date** : 2026-03-11
**Build** : v1.6.2 debug + release
**Appareil** : Ugoos AM9 Pro (192.168.1.152:5555), 1920x1080

---

## Resume

Migration de **tous les appels** `Destinations.itemDetails()` (v1 → `ItemDetailComposeFragment`) vers
`ItemDetailsFragment` (v2 Cinema Immersif). Suppression complete de v1.

Strategie : renommer `itemDetailsV2()` en `itemDetails()` dans Destinations.kt.
Tous les appelants existants pointent automatiquement vers v2 sans modification.

---

## Fichiers supprimes

| Fichier | Role (ancien) |
|---------|--------------|
| `ui/itemdetail/compose/ItemDetailComposeFragment.kt` | Fragment wrapper v1 (ComposeView + ItemDetailScreen) |
| `ui/itemdetail/compose/ItemDetailScreen.kt` | Compose screen v1 (dispatch type + DetailHeroBackdrop) |
| `ui/itemdetail/compose/DetailHeroBackdrop.kt` | Backdrop fullscreen v1 (gradients CINEMA_DEEP) |
| `ui/itemdetail/compose/` (dossier) | Vide apres suppression — supprime |

---

## Fichiers modifies

| Fichier | Changement |
|---------|-----------|
| `ui/navigation/Destinations.kt` | `itemDetails()` pointe vers `ItemDetailsFragment` (v2). `itemDetailsV2()` supprimee. `channelDetails()` et `seriesTimerDetails()` migres vers v2. Import `ItemDetailComposeFragment` retire. |
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | +Args LiveTV (channelId, programInfoJson, seriesTimerJson). +Routing LiveTV dans onViewCreated. +Dispatch LiveTvDetailsContent/SeriesTimerDetailsContent. Sidebar/toolbar overlay supprimee (code mort jamais teste). |

---

## Appels migres (v1 → v2 via renommage)

Aucune modification de code necessaire dans ces fichiers — le renommage `itemDetailsV2 → itemDetails`
fait que tous les appels existants `Destinations.itemDetails(...)` pointent vers v2 :

| Fichier | Ligne | Contexte |
|---------|-------|----------|
| `ui/itemhandling/ItemLauncher.kt` | 68 | Person row item |
| `ui/itemhandling/ItemLauncher.kt` | 147 | Series/MusicArtist/MusicAlbum/Playlist |
| `ui/itemhandling/ItemLauncher.kt` | 168 | BoxSet |
| `ui/itemhandling/ItemLauncher.kt` | 187 | ShowDetails action |
| `ui/itemhandling/ItemLauncher.kt` | 238 | LiveTv recording ShowDetails |
| `data/eventhandling/SocketHandler.kt` | 248 | Notification push |
| `ui/playback/audio/AudioNowPlayingViewModel.kt` | 195 | Navigation artiste |
| `ui/playback/CustomPlaybackOverlayFragment.kt` | 210 | Navigation personne |
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | 444 | Navigation interne (onNavigateToItem) |
| `ui/itemdetail/v2/ItemDetailsFragment.kt` | 559 | Go to series (onGoToSeries) |
| `ui/itemdetail/ItemListFragment.kt` | 525 | Navigation artiste album |
| `ui/jellyseerr/MediaDetailsFragment.kt` | 557 | Jellyseerr → Jellyfin |
| `ui/shuffle/ShuffleManager.kt` | 131 | Shuffle random item |
| `ui/startup/StartupActivity.kt` | 164 | Deep link |

---

## Grep zero v1

```
grep -rn "ItemDetailComposeFragment\|ItemDetailScreen\|DetailHeroBackdrop\|DetailPlaybackCallbacks" \
  app/src --include="*.kt" --include="*.xml"
→ 0 resultats
```

---

## Sidebar supprimee

`ItemDetailsFragment` (v2) contenait un overlay FrameLayout avec `LeftSidebarNavigation`/`MainToolbar`
qui n'etait **jamais atteint** avant (aucun appelant de `itemDetailsV2()`). En activant v2, la sidebar
s'affichait au milieu du contenu Cinema Immersif. Code mort supprime :
- FrameLayout custom avec `dispatchKeyEvent` (sidebar/toolbar key interception)
- ComposeView overlay `LeftSidebarNavigation` (position LEFT)
- ComposeView overlay `MainToolbar` (position TOP)
- Champs `sidebarId`, `toolbarId`, `lastFocusedBeforeSidebar`
- Methode `isDescendantOf()`
- Imports `LeftSidebarNavigation`, `MainToolbar`, `MainToolbarActiveButton`, `NavbarPosition`, `KeyEvent`

---

## LiveTV : migration complete

`channelDetails()` et `seriesTimerDetails()` dans Destinations.kt utilisaient `ItemDetailComposeFragment`.
Migres vers `ItemDetailsFragment` (v2) :

1. **Args** : `channelId`, `programInfoJson`, `seriesTimerJson` ajoutes a `ItemDetailsFragment.Args`
2. **Routing** : `onViewCreated` route vers `viewModel.loadChannelProgram()` ou `viewModel.loadSeriesTimer()`
3. **Dispatch** : `ItemDetailsContent()` dispatch vers `LiveTvDetailsContent` et `SeriesTimerDetailsContent`

---

## Tests manuels

| Test | Resultat |
|------|----------|
| Home → clic card film → detail v2 Cinema Immersif | OK — genre tag, titre 68sp, pills, rating, VegafoXButton, CinemaActionChips, poster |
| Detail sans sidebar parasite | OK — sidebar supprimee, affichage clean |
| Navigation BACK → retour Home | OK |

---

## Screenshot

| Fichier | Description |
|---------|-------------|
| `docs/screenshots/detail_v2_launcher.png` | Detail Monarch episode — Cinema Immersif v2 sans sidebar : genre tag, titre 68sp, pills (4K HDR HEVC MKV 5.1 EAC3), rating 6.5, synopsis, VegafoXButton Reprendre—6m/Recommencer, CinemaActionChips, poster avec progression |

---

## Build

- `assembleGithubDebug` : BUILD SUCCESSFUL
- `assembleGithubRelease` : BUILD SUCCESSFUL
- Install debug + release sur AM9 Pro : Success
