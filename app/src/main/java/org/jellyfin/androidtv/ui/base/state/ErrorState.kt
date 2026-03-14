package org.jellyfin.androidtv.ui.base.state

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons

/**
 * A standardized error state display with icon, message, and retry button.
 *
 * @param message User-friendly error message
 * @param modifier Modifier for the container
 * @param onRetry Callback for retry action. If null, no retry button is shown.
 */
@Composable
fun ErrorState(
	message: String,
	modifier: Modifier = Modifier,
	onRetry: (() -> Unit)? = null,
) {
	Column(
		modifier = modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Icon(
			imageVector = VegafoXIcons.Error,
			contentDescription = null,
			modifier = Modifier.size(48.dp),
			tint = JellyfinTheme.colorScheme.error,
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = message,
			style = JellyfinTheme.typography.bodyLarge,
			color = JellyfinTheme.colorScheme.textSecondary,
			textAlign = TextAlign.Center,
		)

		if (onRetry != null) {
			Spacer(modifier = Modifier.height(24.dp))
			RetryButton(onClick = onRetry)
		}
	}
}

/**
 * A retry button that follows the design system focus patterns.
 */
@Composable
private fun RetryButton(onClick: () -> Unit) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val scale by animateFloatAsState(
		targetValue = if (isFocused) AnimationDefaults.FOCUS_SCALE else 1f,
		animationSpec = AnimationDefaults.focusSpec(),
		label = "RetryButtonScale",
	)

	val bgColor =
		if (isFocused) {
			JellyfinTheme.colorScheme.primary
		} else {
			JellyfinTheme.colorScheme.surfaceContainer
		}
	val textColor =
		if (isFocused) {
			JellyfinTheme.colorScheme.onPrimary
		} else {
			JellyfinTheme.colorScheme.textPrimary
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
				.padding(horizontal = 24.dp, vertical = 10.dp),
	)
}
