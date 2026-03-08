package org.jellyfin.androidtv.ui.player.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.player.base.PlayerHeader
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.VideoRangeType
import org.koin.compose.koinInject

@Composable
@Stable
fun VideoPlayerHeader(
	item: BaseItemDto?,
	playbackManager: PlaybackManager = koinInject(),
) {
	PlayerHeader {
		if (item != null) {
			Row(
				modifier = Modifier,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				// Left: title + subtitle
				Column(
					modifier = Modifier.weight(1f),
				) {
					Text(
						text = item.name.orEmpty(),
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style = JellyfinTheme.typography.titleMedium.copy(
							color = JellyfinTheme.colorScheme.onSurface,
						)
					)

					val subtitle = buildSubtitle(item)
					if (subtitle.isNotEmpty()) {
						Text(
							text = subtitle,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1,
							style = JellyfinTheme.typography.bodySmall.copy(
								color = JellyfinTheme.colorScheme.onSurfaceVariant,
							)
						)
					}
				}

				// Right: technical badges
				val badges = remember(item) { getPlayerBadges(item) }
				val videoSize by playbackManager.state.videoSize.collectAsState()
				val resolutionBadge = remember(videoSize) {
					val w = videoSize.width
					when {
						w >= 3800 -> "4K"
						w >= 1900 -> "1080p"
						w >= 1260 -> "720p"
						w > 0 -> "${w}x${videoSize.height}"
						else -> null
					}
				}

				val allBadges = remember(badges, resolutionBadge) {
					buildList {
						if (resolutionBadge != null) add(resolutionBadge)
						else badges.resolution?.let { add(it) }
						badges.codec?.let { add(it) }
						badges.hdr?.let { add(it) }
						badges.audio?.let { add(it) }
					}
				}

				if (allBadges.isNotEmpty()) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(start = 16.dp),
					) {
						allBadges.forEach { badge ->
							MediaBadgeChip(badge)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun MediaBadgeChip(label: String) {
	Text(
		text = label,
		style = JellyfinTheme.typography.labelSmall.copy(
			color = JellyfinTheme.colorScheme.onSurfaceVariant,
		),
		modifier = Modifier
			.background(
				color = JellyfinTheme.colorScheme.surface.copy(alpha = 0.6f),
				shape = JellyfinTheme.shapes.extraSmall,
			)
			.padding(horizontal = 6.dp, vertical = 2.dp),
	)
}

private fun buildSubtitle(item: BaseItemDto): String {
	val parts = mutableListOf<String>()

	item.seriesName?.let { parts.add(it) }
	item.productionYear?.let { parts.add(it.toString()) }

	item.runTimeTicks?.let { ticks ->
		val totalMinutes = (ticks / 600_000_000).toInt()
		if (totalMinutes > 0) {
			val hours = totalMinutes / 60
			val minutes = totalMinutes % 60
			parts.add(
				if (hours > 0) "${hours}h${minutes.toString().padStart(2, '0')}"
				else "${minutes}min"
			)
		}
	}

	return parts.joinToString(" \u00B7 ")
}

private data class PlayerBadges(
	val resolution: String?,
	val codec: String?,
	val hdr: String?,
	val audio: String?,
)

private fun getPlayerBadges(item: BaseItemDto): PlayerBadges {
	val mediaSource = item.mediaSources?.firstOrNull()
	val streams = mediaSource?.mediaStreams
	val video = streams?.firstOrNull { it.type == MediaStreamType.VIDEO }
	val audio = streams?.firstOrNull { it.type == MediaStreamType.AUDIO }

	val resolution = video?.let {
		val w = it.width ?: 0
		when {
			w >= 3800 -> "4K"
			w >= 1900 -> "1080p"
			w >= 1260 -> "720p"
			else -> null
		}
	}

	val codec = video?.codec?.uppercase()?.let { c ->
		when (c) {
			"HEVC" -> "HEVC"
			"AV1" -> "AV1"
			"H264" -> "H.264"
			"VP9" -> "VP9"
			else -> c
		}
	}

	val hdr = video?.videoRangeType?.let { range ->
		when {
			range == VideoRangeType.DOVI_WITH_HDR10 || range == VideoRangeType.DOVI -> "DV"
			range == VideoRangeType.HDR10_PLUS -> "HDR10+"
			range == VideoRangeType.HDR10 -> "HDR10"
			range != VideoRangeType.SDR -> "HDR"
			else -> null
		}
	}

	val audioLabel = audio?.let {
		val channels = it.channels ?: 0
		when {
			channels > 6 -> "${channels - 1}.1"
			channels == 6 -> "5.1"
			channels == 2 -> "Stereo"
			else -> null
		}
	}

	return PlayerBadges(resolution, codec, hdr, audioLabel)
}
