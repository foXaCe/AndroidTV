# Fix Focus Visible — D-pad focus premium sur ecrans startup

## Probleme

Sur TV, le focus D-pad n'etait pas assez visible sur les boutons des ecrans
de bienvenue, decouverte serveur et Quick Connect. L'utilisateur ne pouvait
pas facilement identifier le bouton selectionne.

## Effets visuels appliques au focus

| Effet | Valeur |
|-------|--------|
| Scale | 1.06f (anime tween 150ms EaseOutCubic) |
| Border | 2dp OrangePrimary |
| Glow externe | drawBehind radialGradient OrangeGlow, radius 1.2x, blendMode Normal |
| Texte | FontWeight.ExtraBold |
| Icone fleche | graphicsLayer scale 1.2f |
| Glow alpha | anime tween 150ms EaseOutCubic (transition fluide) |

### ManualEntryButton highlighted + focused

Effet supplementaire : `shadowElevation = 8dp` via graphicsLayer.

## Easing

`EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)` — defini dans
ConnectServerButton.kt comme `internal val`, importe dans les autres fichiers.

## Boutons modifies

### ConnectServerButton (ConnectServerButton.kt)

- Scale 1.05 → 1.06 + EaseOutCubic
- Glow: BlendMode.Screen → Normal, radius x1.2, alpha animee
- Border 2dp OrangePrimary au focus (transparente sinon)
- drawBehind AVANT clip pour glow externe
- Texte ExtraBold au focus
- Icone fleche scale 1.2 au focus

### ManualEntryButton (ServerDiscoveryScreen.kt)

- Ajout scale 1.06 + EaseOutCubic (absent avant)
- Ajout glow drawBehind (absent avant)
- Border 1dp → 2dp OrangePrimary au focus
- Texte ExtraBold au focus
- highlighted+focused: shadowElevation 8dp

### ConfirmButton "J'ai entre le code" (QuickConnectScreen.kt)

- Scale 1.03 → 1.06 + EaseOutCubic
- Glow: BlendMode.Screen → Normal, radius x1.2, alpha animee
- Border 2dp OrangePrimary au focus
- drawBehind AVANT clip
- Texte ExtraBold au focus

### Boutons ManualAddressDialog (ServerDiscoveryScreen.kt)

- TvPrimaryButton/TvSecondaryButton remplaces par DialogButton custom
- Meme traitement focus: scale 1.06, glow, border 2dp, ExtraBold
- Prise en charge enabled/disabled + isPrimary (couleurs orange/surface)

## Correction annexe (ButtonRow)

`Button.kt` ButtonRow: ajout `fillMaxWidth()` pour centrer le texte
quand le bouton est elargi par weight.

## Fichiers modifies

| Fichier | Changements |
|---------|-------------|
| `ConnectServerButton.kt` | Scale, glow, border, ExtraBold, icon scale |
| `ServerDiscoveryScreen.kt` | ManualEntryButton + DialogButton custom |
| `QuickConnectScreen.kt` | ConfirmButton refait |
| `Button.kt` | ButtonRow fillMaxWidth |

## Validation

- BUILD SUCCESSFUL : debug + release
- Installe sur Ugoos (192.168.1.152)
