package org.jellyfin.androidtv.ui.startup.compose

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.theme.StartupDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

/**
 * Illustration du renard VegafoX avec halo orange pulsant.
 * Utilisé sur WelcomeScreen et les écrans d'auth.
 *
 * @param size     Taille du renard (défaut 160.dp)
 * @param animated Active l'animation d'entrée transition
 */
@Composable
fun VegafoXFoxLogo(
	modifier: Modifier = Modifier,
	size: Dp = StartupDimensions.foxLogoSize,
	animated: Boolean = true,
) {
	var visible by remember { mutableStateOf(!animated) }

	LaunchedEffect(Unit) {
		if (animated) {
			delay(200)
			visible = true
		}
	}

	val transition = updateTransition(targetState = visible, label = "foxEntry")

	val scale by transition.animateFloat(
		transitionSpec = { tween(durationMillis = 700, easing = FastOutSlowInEasing) },
		label = "scale",
	) { state -> if (state) 1f else 0.82f }

	val alpha by transition.animateFloat(
		transitionSpec = { tween(durationMillis = 500, easing = EaseOut) },
		label = "alpha",
	) { state -> if (state) 1f else 0f }

	val infiniteTransition = rememberInfiniteTransition(label = "glow")
	val glowScale by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 1.15f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(2800, easing = EaseInOutSine),
				repeatMode = RepeatMode.Reverse,
			),
		label = "glowScale",
	)

	Box(
		modifier = modifier.size(size * 1.6f),
		contentAlignment = Alignment.Center,
	) {
		Canvas(
			modifier =
				Modifier
					.matchParentSize()
					.graphicsLayer {
						scaleX = glowScale
						scaleY = glowScale
					},
		) {
			val glowRadius = size.toPx() * 0.8f
			drawCircle(
				brush =
					Brush.radialGradient(
						colors =
							listOf(
								VegafoXColors.OrangeGlow,
								Color.Transparent,
							),
						radius = glowRadius,
					),
				radius = glowRadius,
			)
		}

		Image(
			painter = painterResource(R.drawable.ic_vegafox_fox),
			contentDescription = "VegafoX",
			modifier =
				Modifier
					.graphicsLayer {
						scaleX = scale
						scaleY = scale
						this.alpha = alpha
					}.size(size),
		)
	}
}
