package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

/**
 * Unified header for all browse screens.
 *
 * Style: BebasNeue 40sp Bold, letterSpacing 2sp, VegafoXColors.TextPrimary.
 * Optional subtitle in 14sp TextSecondary.
 * Optional trailing actions slot (toolbar buttons).
 */
@Composable
fun BrowseHeader(
	title: String,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
	actions: @Composable RowScope.() -> Unit = {},
) {
	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.padding(
					top = BrowseDimensions.headerPaddingTop,
					start = BrowseDimensions.gridPaddingHorizontal,
					end = BrowseDimensions.gridPaddingHorizontal,
				),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style =
					JellyfinTheme.typography.headlineLarge.copy(
						fontSize = BrowseDimensions.headerFontSize,
						fontWeight = FontWeight.Bold,
						fontFamily = BebasNeue,
						letterSpacing = BrowseDimensions.headerLetterSpacing,
					),
				color = VegafoXColors.TextPrimary,
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.bodyMedium.copy(fontSize = BrowseDimensions.sectionSubtitleFontSize),
					color = VegafoXColors.TextSecondary,
				)
			}
		}
		actions()
	}
}
