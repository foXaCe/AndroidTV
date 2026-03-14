package org.jellyfin.androidtv.ui.player.video

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.rememberCurrentTime
import org.jellyfin.androidtv.ui.composable.rememberPlayerPositionInfo
import org.jellyfin.androidtv.ui.player.base.PlayerHeader
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.VideoRangeType
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val BadgeShape = RoundedCornerShape(4.dp)

@Composable
@Stable
fun VideoPlayerHeader(
	item: BaseItemDto?,
	playbackManager: PlaybackManager = koinInject(),
) {
	PlayerHeader {
		if (item != null) {
			val isSeries = item.type == BaseItemKind.EPISODE

			// Left column: title + badges
			Column(
				modifier = Modifier.weight(1f),
			) {
				// Series name
				if (isSeries) {
					Text(
						text = item.seriesName.orEmpty(),
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style =
							TextStyle(
								fontSize = 13.sp,
								fontWeight = FontWeight.Bold,
								color = VegafoXColors.OrangePrimary,
								letterSpacing = 1.sp,
							),
					)
				}

				// Main title
				Text(
					text = item.name.orEmpty(),
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style =
						TextStyle(
							fontSize = 36.sp,
							fontWeight = FontWeight.Bold,
							fontFamily = BebasNeue,
							color = VegafoXColors.TextPrimary,
							letterSpacing = 2.sp,
							lineHeight = 36.sp,
						),
				)

				// Episode info
				if (isSeries) {
					val seasonNum = item.parentIndexNumber
					val episodeNum = item.indexNumber
					val episodeTitle = item.name.orEmpty()
					val episodeLabel =
						buildString {
							if (seasonNum != null) append("S${seasonNum.toString().padStart(2, '0')}")
							if (episodeNum != null) append("E${episodeNum.toString().padStart(2, '0')}")
							if (episodeTitle.isNotEmpty()) append(" — $episodeTitle")
						}
					Text(
						text = episodeLabel,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style =
							TextStyle(
								fontSize = 13.sp,
								color = VegafoXColors.TextSecondary,
							),
						modifier = Modifier.padding(top = 4.dp),
					)
				}

				// Technical badges
				val badges = remember(item) { getPlayerBadges(item) }
				val videoSize by playbackManager.state.videoSize.collectAsState()
				val resolutionBadge =
					remember(videoSize) {
						val w = videoSize.width
						when {
							w >= 3800 -> "4K"
							w >= 1900 -> "1080p"
							w >= 1260 -> "720p"
							w > 0 -> "${w}x${videoSize.height}"
							else -> null
						}
					}

				val allBadges =
					remember(badges, resolutionBadge) {
						buildList {
							if (resolutionBadge != null) {
								add(BadgeInfo(resolutionBadge, BadgeStyle.RESOLUTION))
							} else {
								badges.resolution?.let { add(BadgeInfo(it, BadgeStyle.RESOLUTION)) }
							}
							badges.hdr?.let { add(BadgeInfo(it, BadgeStyle.HDR)) }
							badges.videoCodec?.let { add(BadgeInfo(it, BadgeStyle.TECH)) }
							badges.container?.let { add(BadgeInfo(it, BadgeStyle.TECH)) }
							badges.audioChannels?.let { add(BadgeInfo(it, BadgeStyle.TECH)) }
							badges.audioCodec?.let { add(BadgeInfo(it, BadgeStyle.TECH)) }
						}
					}

				if (allBadges.isNotEmpty()) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(top = 8.dp),
					) {
						allBadges.forEach { badge ->
							TechBadge(text = badge.text, style = badge.style)
						}
					}
				}
			}

			// Right column: clock + end time
			Column(
				horizontalAlignment = Alignment.End,
			) {
				val currentTime by rememberCurrentTime()
				Text(
					text = currentTime,
					style =
						TextStyle(
							fontSize = 15.sp,
							fontFamily = FontFamily.Monospace,
							color = VegafoXColors.TextSecondary,
							letterSpacing = 1.sp,
						),
				)

				// End time estimate
				val context = LocalContext.current
				val positionInfo by rememberPlayerPositionInfo(playbackManager, precision = 1.seconds)
				val endTimeText =
					remember(positionInfo) {
						computeEndTime(context, positionInfo.duration, positionInfo.active)
					}
				if (endTimeText != null) {
					Text(
						text = endTimeText,
						style =
							TextStyle(
								fontSize = 12.sp,
								color = VegafoXColors.TextHint,
								textAlign = TextAlign.End,
							),
						modifier = Modifier.padding(top = 4.dp),
					)
				}
			}
		}
	}
}

@Composable
private fun TechBadge(
	text: String,
	style: BadgeStyle,
) {
	val badgeColor =
		when (style) {
			BadgeStyle.RESOLUTION -> VegafoXColors.OrangePrimary
			BadgeStyle.HDR -> Color(0xFFFFD700) // Gold
			BadgeStyle.TECH -> VegafoXColors.TextHint
		}

	Box(
		modifier =
			Modifier
				.border(1.dp, badgeColor, BadgeShape)
				.padding(horizontal = 8.dp, vertical = 3.dp),
	) {
		Text(
			text = text,
			style =
				TextStyle(
					fontSize = 10.sp,
					fontWeight = FontWeight.Bold,
					color = badgeColor,
					letterSpacing = 1.sp,
				),
		)
	}
}

private fun computeEndTime(
	context: Context,
	duration: Duration,
	position: Duration,
): String? {
	if (duration == Duration.ZERO) return null
	val msLeft = (duration - position).inWholeMilliseconds
	if (msLeft <= 0) return null
	val endTime = LocalDateTime.now().plus(msLeft, ChronoUnit.MILLIS)
	val formatted = context.getTimeFormatter().format(endTime)
	return context.getString(R.string.lbl_playback_control_ends, formatted)
}

private enum class BadgeStyle { RESOLUTION, HDR, TECH }

private data class BadgeInfo(
	val text: String,
	val style: BadgeStyle,
)

private data class PlayerBadges(
	val resolution: String?,
	val videoCodec: String?,
	val hdr: String?,
	val audioChannels: String?,
	val audioCodec: String?,
	val container: String?,
)

private fun getPlayerBadges(item: BaseItemDto): PlayerBadges {
	val mediaSource = item.mediaSources?.firstOrNull()
	val streams = mediaSource?.mediaStreams
	val video = streams?.firstOrNull { it.type == MediaStreamType.VIDEO }
	val audio = streams?.firstOrNull { it.type == MediaStreamType.AUDIO }

	val resolution =
		video?.let {
			val w = it.width ?: 0
			when {
				w >= 3800 -> "4K"
				w >= 1900 -> "1080p"
				w >= 1260 -> "720p"
				else -> null
			}
		}

	val videoCodec =
		video?.codec?.uppercase()?.let { c ->
			when (c) {
				"HEVC" -> "HEVC"
				"AV1" -> "AV1"
				"H264" -> "H.264"
				"VP9" -> "VP9"
				else -> c
			}
		}

	val hdr =
		video?.videoRangeType?.let { range ->
			when {
				range == VideoRangeType.DOVI_WITH_HDR10 || range == VideoRangeType.DOVI -> "DV"
				range == VideoRangeType.HDR10_PLUS -> "HDR10+"
				range == VideoRangeType.HDR10 -> "HDR10"
				range != VideoRangeType.SDR -> "HDR"
				else -> null
			}
		}

	val audioChannels =
		audio?.let {
			val channels = it.channels ?: 0
			when {
				channels > 6 -> "${channels - 1}.1"
				channels == 6 -> "5.1"
				channels == 2 -> "Stereo"
				else -> null
			}
		}

	val audioCodec =
		audio?.codec?.uppercase()?.let { c ->
			when (c) {
				"AC3" -> "AC3"
				"EAC3" -> "EAC3"
				"AAC" -> "AAC"
				"FLAC" -> "FLAC"
				"DTS" -> "DTS"
				"TRUEHD" -> "TrueHD"
				else -> null
			}
		}

	val container =
		mediaSource?.container?.uppercase()?.let { c ->
			when (c) {
				"MKV" -> "MKV"
				"MP4" -> "MP4"
				"AVI" -> "AVI"
				"TS" -> "TS"
				else -> null
			}
		}

	return PlayerBadges(resolution, videoCodec, hdr, audioChannels, audioCodec, container)
}
