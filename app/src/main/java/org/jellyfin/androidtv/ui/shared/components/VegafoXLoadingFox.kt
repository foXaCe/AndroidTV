package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun VegafoXLoadingFox(
	size: Dp = 80.dp,
	showLabel: Boolean = false,
	modifier: Modifier = Modifier,
) {
	val transition = rememberInfiniteTransition(label = "fox_loading")

	val scale by transition.animateFloat(
		initialValue = 1.0f,
		targetValue = 1.15f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(durationMillis = 800, easing = EaseInOutCubic),
				repeatMode = RepeatMode.Reverse,
			),
		label = "fox_scale",
	)

	val alpha by transition.animateFloat(
		initialValue = 0.7f,
		targetValue = 1.0f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(durationMillis = 600, easing = EaseInOutCubic),
				repeatMode = RepeatMode.Reverse,
			),
		label = "fox_alpha",
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier,
	) {
		Image(
			painter = painterResource(R.drawable.ic_vegafox_fox),
			contentDescription = null,
			modifier =
				Modifier
					.size(size)
					.graphicsLayer {
						scaleX = scale
						scaleY = scale
						this.alpha = alpha
					},
		)

		if (showLabel) {
			Text(
				text = "Chargement\u2026",
				style = JellyfinTheme.typography.labelSmall,
				color = VegafoXColors.TextSecondary,
				modifier =
					Modifier
						.padding(top = 12.dp)
						.graphicsLayer { this.alpha = alpha },
			)
		}
	}
}
