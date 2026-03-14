package org.jellyfin.androidtv.ui.base.state

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons

/**
 * A compact error banner (56dp) that displays inline above content rather than replacing it.
 * Uses the design system error container colors and shows the error message with an optional retry button.
 *
 * @param error The [UiError] to display. If null, nothing is rendered.
 * @param modifier Modifier for the banner
 * @param onRetry Optional callback for retry action. If provided, a "Retry" button is shown.
 */
@Composable
fun ErrorBanner(
	error: UiError?,
	modifier: Modifier = Modifier,
	onRetry: (() -> Unit)? = null,
) {
	if (error == null) return

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.height(56.dp)
				.background(JellyfinTheme.colorScheme.errorContainer)
				.padding(horizontal = 24.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			imageVector = VegafoXIcons.Error,
			contentDescription = null,
			modifier = Modifier.size(20.dp),
			tint = JellyfinTheme.colorScheme.error,
		)

		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = stringResource(error.messageRes),
			style = JellyfinTheme.typography.bodyMedium,
			color = JellyfinTheme.colorScheme.onErrorContainer,
			modifier = Modifier.weight(1f),
		)

		if (onRetry != null) {
			Spacer(modifier = Modifier.width(12.dp))
			BannerRetryButton(onClick = onRetry)
		}
	}
}

@Composable
private fun BannerRetryButton(onClick: () -> Unit) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val scale by animateFloatAsState(
		targetValue = if (isFocused) AnimationDefaults.FOCUS_SCALE else 1f,
		animationSpec = AnimationDefaults.focusSpec(),
		label = "BannerRetryScale",
	)

	val bgColor =
		if (isFocused) {
			JellyfinTheme.colorScheme.error
		} else {
			JellyfinTheme.colorScheme.error.copy(alpha = 0.2f)
		}
	val textColor =
		if (isFocused) {
			JellyfinTheme.colorScheme.onPrimary
		} else {
			JellyfinTheme.colorScheme.error
		}

	Text(
		text = stringResource(R.string.lbl_retry),
		style = JellyfinTheme.typography.labelLarge,
		color = textColor,
		modifier =
			Modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.background(bgColor, JellyfinTheme.shapes.button)
				.clickable(interactionSource = interactionSource, indication = null) { onClick() }
				.focusable(interactionSource = interactionSource)
				.padding(horizontal = 16.dp, vertical = 6.dp),
	)
}
