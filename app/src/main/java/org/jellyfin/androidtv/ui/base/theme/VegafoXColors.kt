package org.jellyfin.androidtv.ui.base.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette Dark Premium VegafoX
 * Identite : renard orange sur fond noir profond
 */
object VegafoXColors {
	// -- Fonds --
	/** Fond principal de tous les ecrans */
	val Background = Color(0xFF0A0A0F)

	/** Fond plus profond (derriere les surfaces) */
	val BackgroundDeep = Color(0xFF07070B)

	/** Surface des cards et composants */
	val Surface = Color(0xFF141418)

	/** Surface dim (legere variation) */
	val SurfaceDim = Color(0xFF0F0F14)

	/** Surface bright (survol / elevated) */
	val SurfaceBright = Color(0xFF1C1C22)

	/** Surface container (elements eleves) */
	val SurfaceContainer = Color(0xFF242428)

	/** Separateurs */
	val Divider = Color(0x0FFFFFFF) // 6% blanc

	// -- Orange VegafoX --
	/** Orange principal — boutons, highlights */
	val OrangePrimary = Color(0xFFFF6B00)

	/** Orange sombre — variante pressed/dark */
	val OrangeDark = Color(0xFFCC5500)

	/** Orange clair — variante light */
	val OrangeLight = Color(0xFFFF8C00)

	/** Orange chaud — gradients secondaires */
	val OrangeWarm = Color(0xFFFF8C00)

	/** Orange glow — halos Canvas (34% opacite) */
	val OrangeGlow = Color(0x55FF6B00)

	/** Orange soft — surfaces tintees (10% opacite) */
	val OrangeSoft = Color(0x1AFF6B00)

	/** Orange border — bordures au focus (30% opacite) */
	val OrangeBorder = Color(0x4DFF6B00)

	/** Orange container — fonds de containers primaires */
	val OrangeContainer = Color(0xFF331500)

	// -- Bleu accent (titre VegafoX) --
	val BlueAccent = Color(0xFF4FC3F7)

	// -- Texte --
	/** Texte primaire */
	val TextPrimary = Color(0xFFF5F0EB)

	/** Texte secondaire (muted) */
	val TextSecondary = Color(0xFF9E9688)

	/** Texte desactive */
	val TextDisabled = Color(0xFF5C584F)

	/** Texte hint / placeholder */
	val TextHint = Color(0xFF7A756B)

	// -- Etats --
	/** Succes (ping faible, connexion OK) */
	val Success = Color(0xFF22C55E)
	val SuccessContainer = Color(0xFF003214)
	val SuccessGlow = Color(0x3322C55E)

	/** Avertissement */
	val Warning = Color(0xFFEAB308)
	val WarningContainer = Color(0xFF2E2600)

	/** Erreur */
	val Error = Color(0xFFEF4444)
	val ErrorContainer = Color(0xFF3D0005)
	val ErrorGlow = Color(0x33EF4444)

	/** Info */
	val Info = Color(0xFF4E98F9)
	val InfoContainer = Color(0xFF032767)

	// -- Focus --
	/** Couleur de l'anneau de focus */
	val FocusRing = OrangePrimary

	/** Glow du focus */
	val FocusGlow = Color(0x33FF6B00) // 20% opacite

	// -- Outlines --
	val Outline = Color(0xFF3E3528)
	val OutlineVariant = Color(0xFF2E2A20)

	// -- Toolbar --
	val ToolbarBackground = Color(0xCC0A0A0F)
	val ToolbarDivider = Color(0x1AFFFFFF) // 10% blanc

	// -- Rating --
	val Rating = Color(0xFFFFD700)
	val RatingEmpty = Color(0xFF3E3528)

	// -- Recording --
	val Recording = Color(0xFFFB7E7E)
	val OnRecording = Color(0xFFFFF6F6)

	// -- Dialog --
	val DialogScrim = Color(0xE6141414)
	val DialogSurface = Color(0xFF1C1C22)

	// -- Gradient (details pages) --
	val GradientStart = Color(0xFF3D1E00)
	val GradientMid = Color(0xFF1F0F00)
	val GradientEnd = Color(0xFF0A0A0F)
}
