package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.design.Tokens

/**
 * Standard TV screen header with title, optional subtitle, and action slot.
 * Replaces Leanback's BrowseFragment title area / TitleViewAdapter.
 *
 * @param title Screen title displayed in [JellyfinTheme.typography.titleLarge].
 * @param subtitle Optional subtitle in [JellyfinTheme.typography.bodyMedium].
 * @param actions Slot for action buttons (e.g. sort, filter, search).
 */
@Composable
fun TvHeader(
	title: String,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
	actions: @Composable RowScope.() -> Unit = {},
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.textPrimary,
			)

			if (subtitle != null) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textSecondary,
				)
			}
		}

		Row(
			horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceSm),
			verticalAlignment = Alignment.CenterVertically,
			content = actions,
		)
	}
}
