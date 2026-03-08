package org.jellyfin.androidtv.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import org.jellyfin.androidtv.ui.base.JellyfinTheme

/**
 * Returns an elegant gradient painter for image placeholders.
 * Uses theme surface colors for a subtle dark-to-darker diagonal gradient,
 * much better than a flat grey rectangle.
 */
@Composable
fun rememberGradientPlaceholder(): Painter {
	val startColor = JellyfinTheme.colorScheme.surfaceBright
	val endColor = JellyfinTheme.colorScheme.surfaceDim
	return BrushPainter(
		Brush.linearGradient(listOf(startColor, endColor))
	)
}

/**
 * Returns a subtle solid painter for image error fallback.
 * Slightly lighter than the placeholder to hint at a different state.
 */
@Composable
fun rememberErrorPlaceholder(): Painter {
	return ColorPainter(JellyfinTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
}
