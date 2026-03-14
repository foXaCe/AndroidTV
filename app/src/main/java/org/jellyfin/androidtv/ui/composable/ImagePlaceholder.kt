package org.jellyfin.androidtv.ui.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
		Brush.linearGradient(listOf(startColor, endColor)),
	)
}

/**
 * Returns an animated shimmer painter for image placeholders.
 * Sweeps a highlight gradient from left to right over the surface color,
 * giving a perceived-loading effect on dense card grids.
 */
@Composable
fun rememberShimmerPlaceholder(): Painter {
	val baseColor = JellyfinTheme.colorScheme.surfaceBright
	val highlightColor = JellyfinTheme.colorScheme.surfaceContainer

	val transition = rememberInfiniteTransition(label = "img_shimmer")
	val translateX by transition.animateFloat(
		initialValue = -500f,
		targetValue = 1500f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(durationMillis = 1200, easing = LinearEasing),
				repeatMode = RepeatMode.Restart,
			),
		label = "img_shimmer_translate",
	)

	return BrushPainter(
		Brush.linearGradient(
			colors = listOf(baseColor, highlightColor, baseColor),
			start = Offset(translateX, 0f),
			end = Offset(translateX + 500f, 0f),
		),
	)
}

/**
 * Returns a subtle solid painter for image error fallback.
 * Slightly lighter than the placeholder to hint at a different state.
 */
@Composable
fun rememberErrorPlaceholder(): Painter = ColorPainter(JellyfinTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
