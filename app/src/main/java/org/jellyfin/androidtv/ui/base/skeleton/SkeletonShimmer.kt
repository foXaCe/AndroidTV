package org.jellyfin.androidtv.ui.base.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme

/**
 * A shimmer brush that animates from left to right.
 * Used by skeleton composables to indicate loading state.
 */
@Composable
fun rememberShimmerBrush(
	baseColor: Color = JellyfinTheme.colorScheme.surfaceBright.copy(alpha = 0.4f),
	highlightColor: Color = JellyfinTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
): Brush {
	val transition = rememberInfiniteTransition(label = "shimmer")
	val translateX by transition.animateFloat(
		initialValue = -500f,
		targetValue = 1500f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(durationMillis = 1200, easing = LinearEasing),
				repeatMode = RepeatMode.Restart,
			),
		label = "shimmer_translate",
	)

	return Brush.linearGradient(
		colors = listOf(baseColor, highlightColor, baseColor),
		start = Offset(translateX, 0f),
		end = Offset(translateX + 500f, 0f),
	)
}

/**
 * A skeleton placeholder box with shimmer animation.
 */
@Composable
fun SkeletonBox(
	modifier: Modifier = Modifier,
	shape: Shape = JellyfinTheme.shapes.small,
) {
	val shimmerBrush = rememberShimmerBrush()
	Box(
		modifier =
			modifier
				.background(brush = shimmerBrush, shape = shape),
	)
}

/**
 * A skeleton text line placeholder.
 */
@Composable
fun SkeletonTextLine(
	width: Dp,
	height: Dp = 14.dp,
	modifier: Modifier = Modifier,
) {
	SkeletonBox(
		modifier =
			modifier
				.width(width)
				.height(height),
		shape = JellyfinTheme.shapes.extraSmall,
	)
}

/**
 * A skeleton text block with multiple lines of varying widths.
 */
@Composable
fun SkeletonTextBlock(
	lines: Int = 3,
	lineHeight: Dp = 14.dp,
	lineSpacing: Dp = 8.dp,
	maxWidth: Dp = 200.dp,
	modifier: Modifier = Modifier,
) {
	val widthFractions = listOf(1f, 0.85f, 0.7f, 0.9f, 0.6f)
	androidx.compose.foundation.layout.Column(modifier = modifier) {
		repeat(lines) { index ->
			val fraction = widthFractions[index % widthFractions.size]
			SkeletonBox(
				modifier =
					Modifier
						.width(maxWidth * fraction)
						.height(lineHeight),
				shape = JellyfinTheme.shapes.extraSmall,
			)
			if (index < lines - 1) {
				Spacer(modifier = Modifier.height(lineSpacing))
			}
		}
	}
}
