# Performance Optimizations — VegafoX Android TV

Dossier regroupant toutes les optimisations de performance appliquees au projet.
Les docs de design UI restent dans `docs/dev/`.

---

## Optimisations appliquees

| Fix | Date | Description | Impact |
|-----|------|-------------|--------|
| [h_fix4](h_fix4.md) | 2026-03-11 | Optimisation chargement images Home (Coil ImageRequest, taille cible 220px, cache memoire+disque, prefetch des rows suivantes) | Reduction decodage images (1920px → 440px), prefetch anticipe le scroll |
| [h_fix5](h_fix5.md) | 2026-03-11 | Priorite THUMB au lieu de BACKDROP pour les cards Home (images plus legeres et mieux cadrees) | Images plus petites a telecharger, meilleur rendu sur vignettes |
| [h_fix6](h_fix6.md) | 2026-03-11 | Fond Dark Grid Noise (Canvas 5 couches, seed fixe, aucun state reactif) | Canvas sans recomposition, ~840 draw ops negligeables, backdrop alpha 0.30 |

---

## Convention

- Les fichiers de performance suivent le nommage `h_fix<N>.md`
- Chaque fix documente : probleme, solution, fichiers modifies, mesures avant/apres si disponibles
- Les futurs fix de performance doivent etre crees directement dans ce dossier
