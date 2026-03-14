package org.jellyfin.androidtv.ui.base.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.button.ButtonColors
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton

private val TvButtonMinHeight = 48.dp
private val TvIconButtonContentPadding = PaddingValues(12.dp)

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
