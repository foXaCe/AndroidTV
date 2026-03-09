package org.jellyfin.androidtv.ui.base.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.button.ButtonColors
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton

private val TvButtonMinHeight = 48.dp
private val TvButtonContentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
private val TvIconButtonContentPadding = PaddingValues(12.dp)

object TvButtonDefaults {
	@ReadOnlyComposable
	@Composable
	fun primaryColors() = ButtonDefaults.colors(
		containerColor = JellyfinTheme.colorScheme.primary,
		contentColor = JellyfinTheme.colorScheme.onPrimary,
		focusedContainerColor = JellyfinTheme.colorScheme.primaryLight,
		focusedContentColor = JellyfinTheme.colorScheme.onPrimary,
		disabledContainerColor = JellyfinTheme.colorScheme.primary.copy(alpha = 0.38f),
		disabledContentColor = JellyfinTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
	)

	@ReadOnlyComposable
	@Composable
	fun secondaryColors() = ButtonDefaults.colors(
		containerColor = JellyfinTheme.colorScheme.surfaceContainer,
		contentColor = JellyfinTheme.colorScheme.onSurface,
		focusedContainerColor = JellyfinTheme.colorScheme.surfaceBright,
		focusedContentColor = JellyfinTheme.colorScheme.onSurface,
		disabledContainerColor = JellyfinTheme.colorScheme.surfaceContainer.copy(alpha = 0.38f),
		disabledContentColor = JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.38f),
	)
}

@Composable
fun TvPrimaryButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	enabled: Boolean = true,
) {
	Button(
		onClick = onClick,
		modifier = modifier.defaultMinSize(minHeight = TvButtonMinHeight),
		enabled = enabled,
		shape = JellyfinTheme.shapes.small,
		colors = TvButtonDefaults.primaryColors(),
		contentPadding = TvButtonContentPadding,
	) {
		if (icon != null) {
			Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
			Spacer(modifier = Modifier.width(8.dp))
		}
		Text(text = text, style = JellyfinTheme.typography.labelLarge)
	}
}

@Composable
fun TvSecondaryButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
) {
	Button(
		onClick = onClick,
		modifier = modifier.defaultMinSize(minHeight = TvButtonMinHeight),
		shape = JellyfinTheme.shapes.small,
		colors = TvButtonDefaults.secondaryColors(),
		contentPadding = TvButtonContentPadding,
	) {
		if (icon != null) {
			Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
			Spacer(modifier = Modifier.width(8.dp))
		}
		Text(text = text, style = JellyfinTheme.typography.labelLarge)
	}
}

@Composable
fun TvIconButton(
	icon: ImageVector,
	contentDescription: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	tint: Color = JellyfinTheme.colorScheme.onSurface,
	colors: ButtonColors = ButtonDefaults.colors(),
) {
	IconButton(
		onClick = onClick,
		modifier = modifier.defaultMinSize(minHeight = TvButtonMinHeight, minWidth = TvButtonMinHeight),
		shape = JellyfinTheme.shapes.small,
		colors = colors,
		contentPadding = TvIconButtonContentPadding,
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			tint = tint,
			modifier = Modifier.size(24.dp),
		)
	}
}
