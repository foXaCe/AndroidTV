# Audit visuel — Ecran Home VegafoX

**Date** : 2026-03-10
**Build** : v1.6.2 debug + release
**Appareil** : Ugoos AM9 Pro (192.168.1.152:5555), resolution 1920x1080
**Contexte** : Post H1 (topbar), H2 (hero overlay), H3 (cards i18n/badges), H4 (fond panoramique + stagger)

---

## Screenshots captures

| Fichier | Description |
|---------|-------------|
| `docs/screenshots/home_default.png` | Ecran Home au lancement, focus sur topbar |
| `docs/screenshots/home_focused_card.png` | Focus descendu sur premiere row, hero "Monarch: Legacy of Monsters" |
| `docs/screenshots/home_focused_card2.png` | Row "Derniers ajouts — Films", hero "Le Secret de Kheops" |
| `docs/screenshots/home_card_focus.png` | Row "Derniers ajouts — Series", hero "Baja" (ep La Defense Lincoln) |
| `docs/screenshots/home_scrolled.png` | Scroll vers le bas, hero "1883" |
| `docs/screenshots/home_topbar_focus.png` | Focus sur topbar, hero "Avatar" |
| `docs/screenshots/home_tab_films2.png` | Hero "Alien, le huitieme passager" |
| `docs/screenshots/home_tab_right.png` | Hero "Avatar", card "Avatar" focusee avec bordure orange |
| `docs/screenshots/home_cw_card.png` | Row "Prochains episodes", hero "Home Before Dark" |
| `docs/screenshots/home_first_row.png` | Row "Continuer a regarder", hero "Star Wars: Les Derniers Jedi" |

---

## Bilan zone par zone

### 1. TOPBAR

| Element | Statut | Detail |
|---------|--------|--------|
| Logo VegafoX (renard + texte) | OK | Visible en haut a gauche dans tous les screenshots, "Vega" bleu + "foX" orange, icone renard 32dp |
| Onglet "Accueil" (actif) | OK | Texte orange sur fond OrangeSoft, bordures laterales oranges, clairement identifiable |
| Onglets Films/Series/Musique/Live TV | PROBLEME | Textes en TextSecondary (#9E9688) sur fond quasi transparent — tres peu visibles a l'ecran, se confondent avec le fond sombre du backdrop |
| Barre de recherche | PROBLEME | Texte TextHint (#7A756B) + fond Surface 4% + bordure Divider 1dp — pratiquement invisible sur le backdrop sombre |
| Avatar utilisateur | A VERIFIER | Petit cercle orange 32dp a l'extremite droite — visible dans certains screenshots sous forme de point orange mais difficile a distinguer a cette resolution |
| Conteneur onglets (bordure/fond) | PROBLEME | La bordure Divider 1dp et le fond Surface 4% sont trop subtils pour delimiter visuellement le groupe d'onglets |
| Focus state (Accueil focalise) | PROBLEME | Quand le focus est sur la topbar, l'onglet focalise prend toute l'attention avec une couleur focusColor unie et large — les autres onglets deviennent encore moins visibles par contraste |

**Verdict topbar** : Le layout et le code sont corrects (5 onglets + recherche + avatar tous rendus). Le probleme est un **contraste insuffisant** : les elements inactifs (TextSecondary, Surface 4%, Divider 1dp) sont trop discrets sur un fond sombre avec un backdrop image derriere. En pratique, seul l'onglet actif "Accueil" est clairement visible.

---

### 2. HERO ZONE

| Element | Statut | Detail |
|---------|--------|--------|
| Backdrop image (Coil) | OK | Images serveur affichees correctement, crossfade fonctionnel, blur visible, alpha 0.35 laisse transparaitre le fond panoramique |
| Tag line (type + genre) | OK | "FILM • AVENTURE", "SERIE • DRAMA", "NOM SERIE • S1E2" — orange, 11sp, uppercase, tiret orange 20dp |
| Titre item | OK | 32sp, W800, TextPrimary, max 2 lignes, ellipsis fonctionnel |
| Pills row (annee/duree/note/rating/resolution) | ABSENT | Aucune pill visible dans aucun screenshot — soit les donnees API ne fournissent pas ces champs, soit l'espace vertical est insuffisant pour les afficher |
| Description (overview) | ABSENT | Aucun synopsis visible — meme cause probable |
| Boutons action (Lecture / Ma liste) | ABSENT | Non visibles a l'ecran — l'espace entre le titre hero et le debut des rows est trop restreint |
| Dots de pagination | OK | Dash orange (20x6dp) visible en bas a droite dans tous les screenshots pertinents |
| Gradient vertical (fond → transparent) | OK | Transition douce du bas vers le haut, bonne lisibilite du texte hero |
| Gradient horizontal dynamique | OK | Cote gauche assombri quand un item est focalise, protege la lisibilite du texte hero |

**Verdict hero** : Le backdrop et le titre fonctionnent parfaitement. Les pills, la description et les boutons d'action sont **absents visuellement**. Cause probable : l'overlay HeroInfoContent (Column bottom-aligned dans Box weight=0.45) n'a pas assez d'espace vertical pour afficher tous les elements. Le titre et la tag line remplissent l'espace disponible, et les elements suivants (pills, description, boutons) sont clippes ou superposes par la zone rows en dessous.

**Bug donnees** : Certains episodes affichent le TVDBID dans le nom de serie (ex: `MONARCH - LEGACY OF MONSTERS [TVDBID-422598]`). C'est un probleme de metadata serveur, pas du code client.

---

### 3. ROWS

| Element | Statut | Detail |
|---------|--------|--------|
| Titres en francais | OK | "Continuer a regarder", "Prochains episodes", "Derniers ajouts — Films", "Derniers ajouts — Series" — i18n fonctionnel |
| Cards poster | OK | Images chargees correctement, proportions 3:4, clip corners, fallback fonce quand pas d'image |
| Badge NEW | OK | Badge orange avec texte "NEW" noir, 10sp Bold, position TopEnd + 4dp padding — visible et lisible |
| Barre de progression | A VERIFIER | Non clairement visible dans les screenshots (2dp de hauteur, tres fine) — peut etre presente mais indistinguable a cette resolution/compression |
| Focus D-pad (bordure orange) | OK | Bordure orange 2dp clairement visible sur la card focalisee, scale 1.06x perceptible |
| Espacement entre cards | OK | 16dp (spaceMd) entre les cards dans chaque row |
| Espacement entre rows | OK | 16dp (spaceMd) entre les rows, pas d'espace excessif |
| Scroll horizontal | OK | Les rows defilent horizontalement, les cards en dehors du viewport sont chargees |
| Stagger animation | NON TESTABLE | L'animation d'entree ne se joue qu'au premier load et n'est pas capturable via screenshot statique |
| Content padding start | OK | Decalage gauche 48dp (space3xl) respecte, pas de card collee au bord |

**Verdict rows** : Fonctionnelles et bien presentees. Les titres i18n et les badges NEW sont les ameliorations les plus visibles par rapport a l'ancien ecran.

---

### 4. FOND PANORAMIQUE

| Element | Statut | Detail |
|---------|--------|--------|
| Gradient directionnel 160° | OK | Visible dans les zones sans backdrop (coins, zones sombres) — tons bleu marine/violet perceptibles |
| Radial bleu marine (65%/40%) | OK | Halos bleus subtils dans la zone hero droite |
| Radial violet (70%/20%) | OK | Contribution au ton violet en haut a droite |
| Radial orange (20%/80%) | SUBTIL | Tres discret (alpha 0.05), non distinguable individuellement mais contribue a la chaleur d'ensemble |
| Scanlines (4dp, 0.008 alpha) | NON PERCEPTIBLE | Concu pour etre quasi invisible — ne se voit pas dans les screenshots. L'effet est un ajout de texture tres subtil qui peut ne fonctionner que percu inconsciemment |
| Superposition avec backdrop | OK | Le fond panoramique transparait sous le backdrop (alpha 0.35), creant une atmosphere de profondeur |
| Ambiance dark premium | OK | L'ensemble (fond panoramique + backdrop 35% + gradients) produit une ambiance sombre et immersive coherente |

**Verdict fond** : Le fond panoramique fonctionne comme prevu. Il cree une base atmospherique qui transparait sous le backdrop. Les scanlines et le radial orange sont trop subtils pour etre percus individuellement mais contribuent a la richesse visuelle d'ensemble.

---

## Comparaison avec design cible Option C

### Ce qui correspond (OK)

- [x] Layout general : backdrop + toolbar + hero + rows
- [x] Logo VegafoX en haut a gauche
- [x] Onglets de navigation au centre (Accueil/Films/Series/Musique/Live TV)
- [x] Recherche + avatar a droite
- [x] Zone hero avec tag line, titre, backdrop image
- [x] Rows horizontales style Netflix avec titres
- [x] Focus orange sur les cards
- [x] Badges NEW sur les nouveautes
- [x] Fond sombre premium avec gradients
- [x] Pagination dots
- [x] Titres de rows en francais

### Ce qui ne correspond pas (ecarts)

| # | Zone | Ecart | Gravite |
|---|------|-------|---------|
| 1 | Topbar | Onglets inactifs quasi invisibles (contraste insuffisant sur fond sombre) | P0 |
| 2 | Topbar | Recherche et avatar quasi invisibles | P1 |
| 3 | Hero | Pills (annee, duree, note, rating, resolution) absentes a l'ecran | P0 |
| 4 | Hero | Boutons d'action (Lecture / Ma liste) absents a l'ecran | P0 |
| 5 | Hero | Description (overview) absente a l'ecran | P1 |
| 6 | Topbar | Focus state : l'onglet focalise monopolise l'attention, ecrase les autres visuellement | P1 |
| 7 | Cards | Barre de progression Continue Watching non distinguable (trop fine ?) | P2 |

### Ce qui manque completement

| # | Element design cible | Statut | Gravite |
|---|---------------------|--------|---------|
| 1 | Espace vertical suffisant pour hero info complet | Manquant — le ratio 0.45/0.55 ne laisse pas assez d'espace pour tag + titre + pills + desc + boutons | P0 |
| 2 | Fond semi-opaque derriere la topbar | Manquant — la topbar est directement sur le backdrop sans fond propre | P1 |

---

## Liste priorisee des ecarts a corriger

### P0 — Critique (fonctionnalite absente ou inutilisable)

1. **Hero info tronquee** : Les pills, description et boutons d'action ne sont pas visibles. Le HeroInfoOverlay (Column bottom-aligned) est clip par le manque d'espace dans le Box(weight=0.45). Solutions possibles :
   - Augmenter le weight de la zone hero (0.50 ou 0.55)
   - Reduire la taille du titre (28sp au lieu de 32sp)
   - Rendre le hero scrollable ou adaptatif
   - Passer les boutons a l'exterieur de la zone hero

2. **Topbar contraste** : Les onglets inactifs, la recherche et l'avatar sont quasi invisibles sur le backdrop sombre. Solutions possibles :
   - Ajouter un fond semi-opaque (Surface 15-20%) derriere la Row toolbar
   - Augmenter la luminosite de TextSecondary et TextHint pour les elements toolbar
   - Augmenter l'epaisseur de la bordure du conteneur onglets (2dp au lieu de 1dp)
   - Ajouter un fond Surface 10% pour chaque onglet inactif

### P1 — Important (degrade l'experience)

3. **Bouton recherche invisible** : Le fond Surface 4% et le texte TextHint sont trop discrets. Augmenter le fond a 10% et le texte a TextSecondary.

4. **Avatar peu visible** : Le cercle orange 32dp est petit. Peut etre acceptable mais gagnerait en visibilite avec une taille legerement plus grande (36dp).

5. **Description hero absente** : Meme cause que P0.1, mais la description est moins critique que les boutons d'action.

### P2 — Mineur (amelioration visuelle)

6. **Barre de progression** : Augmenter la hauteur de 2dp a 3dp pour ameliorer la visibilite, ou ajouter un fond de piste legerement plus opaque.

7. **Scanlines non perceptibles** : Alpha 0.008 est probablement trop subtil. Pourrait etre augmente a 0.015-0.02 si l'effet est desire visuellement.

---

## Notes techniques

- L'animation stagger des rows n'est pas testable par screenshot statique. Un test visuel en direct est necessaire.
- Les TVDBID dans les noms de series (ex: `[TVDBID-422598]`) sont des donnees serveur — a corriger cote Jellyfin, pas cote client.
- Le crossfade du backdrop fonctionne correctement (observe en navigant entre les items).
- La resolution de capture ADB (1920x1080) est fidelement la resolution de l'appareil.
