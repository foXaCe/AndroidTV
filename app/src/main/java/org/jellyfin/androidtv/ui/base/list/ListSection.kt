package org.jellyfin.androidtv.ui.base.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.theme.SettingsDimensions
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
		Column(modifier = modifier.padding(top = SettingsDimensions.sectionDividerTopPadding)) {
			HorizontalDivider(
				modifier = Modifier.fillMaxWidth(),
				thickness = SettingsDimensions.sectionDividerThickness,
				color = VegafoXColors.Divider,
			)
			ListItemContent(
				headingContent = headingContent,
				leadingContent = leadingContent,
				trailingContent = trailingContent,
				footerContent = footerContent,
				headingStyle =
					JellyfinTheme.typography.labelMedium.copy(
						color = VegafoXColors.TextHint,
						letterSpacing = SettingsDimensions.sectionLetterSpacing,
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
