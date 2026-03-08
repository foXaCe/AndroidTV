package org.jellyfin.androidtv.ui.base.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

/**
 * A standardized empty state display with icon, title, and optional message.
 *
 * @param title Short descriptive title (e.g., "No items")
 * @param modifier Modifier for the container
 * @param message Optional explanatory message
 * @param icon Optional icon resource ID
 * @param action Optional composable for a CTA button
 */
@Composable
fun EmptyState(
	title: String,
	modifier: Modifier = Modifier,
	message: String? = null,
	icon: ImageVector? = null,
	action: (@Composable () -> Unit)? = null,
) {
	Column(
		modifier = modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		if (icon != null) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(48.dp),
				tint = JellyfinTheme.colorScheme.textDisabled,
			)
			Spacer(modifier = Modifier.height(16.dp))
		}

		Text(
			text = title,
			style = JellyfinTheme.typography.titleLarge,
			color = JellyfinTheme.colorScheme.textHint,
			textAlign = TextAlign.Center,
		)

		if (message != null) {
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = message,
				style = JellyfinTheme.typography.bodyMedium,
				color = JellyfinTheme.colorScheme.textDisabled,
				textAlign = TextAlign.Center,
			)
		}

		if (action != null) {
			Spacer(modifier = Modifier.height(24.dp))
			action()
		}
	}
}
