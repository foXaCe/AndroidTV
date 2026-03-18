package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.formatDuration
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaStreamType

/**
 * Shared metadata badges row used by both the Home Hero and Detail screens.
 * Displays: year · duration · community rating · Rotten Tomatoes · quality badge.
 */
@Composable
fun MediaMetadataBadges(
	item: BaseItemDto,
	modifier: Modifier = Modifier,
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier,
	) {
		// Year + Duration
		val metaParts = mutableListOf<String>()
		item.productionYear?.let { metaParts.add(it.toString()) }
		item.runTimeTicks?.let { ticks ->
			if (ticks > 0) metaParts.add(formatDuration(ticks))
		}
		if (metaParts.isNotEmpty()) {
			Text(
				text = metaParts.joinToString("  \u00B7  "),
				fontSize = 13.sp,
				color = VegafoXColors.TextPrimary,
			)
		}

		// Community rating — star + number
		item.communityRating?.let { rating ->
			if (rating > 0f) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(3.dp),
				) {
					androidx.compose.material3.Icon(
						imageVector = VegafoXIcons.Star,
						contentDescription = null,
						tint = Color(0xFFF5C518),
						modifier = Modifier.size(14.dp),
					)
					Text(
						text = if (rating % 1f == 0f) rating.toInt().toString() else String.format("%.1f", rating).replace(",", "."),
						fontSize = 13.sp,
						fontWeight = FontWeight.Bold,
						color = VegafoXColors.TextPrimary,
					)
				}
			}
		}

		// Rotten Tomatoes — official tomato icons
		item.criticRating?.let { rating ->
			if (rating > 0f) {
				val isFresh = rating >= 60f
				val iconRes = if (isFresh) R.drawable.ic_rt_fresh else R.drawable.ic_rt_rotten
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(3.dp),
				) {
					Image(
						painter = painterResource(iconRes),
						contentDescription = null,
						modifier = Modifier.size(16.dp),
					)
					Text(
						text = "${rating.toInt()}%",
						fontSize = 12.sp,
						fontWeight = FontWeight.Medium,
						color = VegafoXColors.TextPrimary,
					)
				}
			}
		}

		// Quality badge — Netflix/Disney+ style
		getQualityBadgeRes(item)?.let { badgeRes ->
			Image(
				painter = painterResource(badgeRes),
				contentDescription = null,
				modifier = Modifier.height(18.dp),
				contentScale = ContentScale.FillHeight,
			)
		}
	}
}

private fun getQualityBadgeRes(item: BaseItemDto): Int? {
	val height =
		item.mediaSources
			?.firstOrNull()
			?.mediaStreams
			?.firstOrNull { it.type == MediaStreamType.VIDEO }
			?.height
			?: return null

	return when {
		height >= 2160 -> R.drawable.ic_badge_4k
		height >= 1080 -> R.drawable.ic_badge_hd
		else -> R.drawable.ic_badge_sd
	}
}
