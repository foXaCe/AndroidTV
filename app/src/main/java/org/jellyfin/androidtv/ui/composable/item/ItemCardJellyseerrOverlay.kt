package org.jellyfin.androidtv.ui.composable.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

/**
 * Compose overlay for Jellyseerr media cards, replacing the View-based PosterBadges.
 * Renders media type badge (top-left) and availability indicator (top-right).
 */
@Composable
@Stable
fun ItemCardJellyseerrOverlay(
	item: JellyseerrDiscoverItemDto,
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(6.dp)
	) {
		MediaTypeBadge(
			mediaType = item.mediaType,
			modifier = Modifier.align(Alignment.TopStart),
		)

		AvailabilityIndicator(
			status = item.mediaInfo?.status,
			modifier = Modifier.align(Alignment.TopEnd),
		)
	}
}

@Composable
@Stable
private fun MediaTypeBadge(
	mediaType: String?,
	modifier: Modifier = Modifier,
) {
	if (mediaType == null) return

	val (text, bgColor) = when (mediaType) {
		"movie" -> stringResource(R.string.lbl_movie_type_upper) to JellyfinTheme.colorScheme.info
		"tv" -> stringResource(R.string.lbl_series_type_upper) to JellyfinTheme.colorScheme.secondary
		else -> return
	}

	Text(
		text = text,
		style = JellyfinTheme.typography.labelSmall,
		color = JellyfinTheme.colorScheme.onPrimary,
		letterSpacing = 0.8.sp,
		modifier = modifier
			.background(bgColor.copy(alpha = 0.85f), JellyfinTheme.shapes.extraSmall)
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}

@Composable
@Stable
private fun AvailabilityIndicator(
	status: Int?,
	modifier: Modifier = Modifier,
) {
	if (status == null) return

	val iconRes = when (status) {
		5 -> R.drawable.ic_available
		4 -> R.drawable.ic_partially_available
		3 -> R.drawable.ic_indigo_spinner
		else -> return
	}

	Icon(
		imageVector = ImageVector.vectorResource(iconRes),
		contentDescription = null,
		tint = Color.Unspecified,
		modifier = modifier.size(20.dp),
	)
}
