package org.jellyfin.androidtv.ui.base.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VegafoXGradients {
	/** Orbe central derriere le renard — fond des ecrans d'auth */
	val OrbCenter =
		Brush.radialGradient(
			colors = listOf(VegafoXColors.OrangeGlow, Color.Transparent),
			radius = 600f,
		)

	/** Ligne horizon — separateur decoratif horizontal */
	val HorizonLine =
		Brush.horizontalGradient(
			colors =
				listOf(
					Color.Transparent,
					VegafoXColors.OrangePrimary,
					Color.Transparent,
				),
		)

	/** Bouton Orange — fond du bouton principal */
	val OrangeButton =
		Brush.linearGradient(
			colors =
				listOf(
					VegafoXColors.OrangePrimary,
					VegafoXColors.OrangeWarm,
				),
			start = Offset(0f, 0f),
			end = Offset(Float.POSITIVE_INFINITY, 0f),
		)

	/** Fond ecran — degrade radial subtil depuis le haut */
	val ScreenBackground =
		Brush.radialGradient(
			colors =
				listOf(
					Color(0xFF12080A), // legere teinte chaude au centre
					VegafoXColors.Background,
				),
			radius = 1200f,
		)
}
