package org.jellyfin.androidtv.ui.base.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun ListSection(
	modifier: Modifier = Modifier,
	headingContent: @Composable () -> Unit,
	overlineContent: (@Composable () -> Unit)? = null,
	captionContent: (@Composable () -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable () -> Unit)? = null,
	footerContent: (@Composable () -> Unit)? = null,
) {
	val isSectionDivider = overlineContent == null && captionContent == null

	if (isSectionDivider) {
		Column(modifier = modifier.padding(top = 24.dp)) {
			HorizontalDivider(
				modifier = Modifier.fillMaxWidth(),
				thickness = 1.dp,
				color = Color.White.copy(alpha = 0.06f),
			)
			ListItemContent(
				headingContent = headingContent,
				leadingContent = leadingContent,
				trailingContent = trailingContent,
				footerContent = footerContent,
				headingStyle =
					TextStyle(
						color = VegafoXColors.TextHint,
						fontSize = 12.sp,
						fontWeight = FontWeight.Medium,
						letterSpacing = 2.sp,
					),
			)
		}
	} else {
		ListItemContent(
			headingContent = headingContent,
			overlineContent = overlineContent,
			captionContent = captionContent,
			leadingContent = leadingContent,
			trailingContent = trailingContent,
			footerContent = footerContent,
			headingStyle =
				JellyfinTheme.typography.listHeader
					.copy(color = JellyfinTheme.colorScheme.listHeader),
			modifier = modifier,
		)
	}
}
