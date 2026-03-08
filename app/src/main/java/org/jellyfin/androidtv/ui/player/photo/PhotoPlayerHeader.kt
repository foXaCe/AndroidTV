package org.jellyfin.androidtv.ui.player.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.style.TextOverflow
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.player.base.PlayerHeader
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
@Stable
fun PhotoPlayerHeader(
	item: BaseItemDto?,
) {
	PlayerHeader {
		if (item != null) {
			Text(
				text = item.name.orEmpty(),
				overflow = TextOverflow.Ellipsis,
				maxLines = 1,
				style = JellyfinTheme.typography.titleMedium.copy(
					color = JellyfinTheme.colorScheme.onSurface,
				)
			)

			item.album?.let { album ->
				Text(
					text = album,
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style = JellyfinTheme.typography.titleSmall.copy(
						color = JellyfinTheme.colorScheme.onSurfaceVariant,
					)
				)
			}
		}
	}
}
