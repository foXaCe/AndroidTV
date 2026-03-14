package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaInfoDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons

@Composable
fun JellyseerrRequestButtons(
	mediaInfo: JellyseerrMediaInfoDto?,
	onRequestClick: (canRequestHd: Boolean, canRequest4k: Boolean, hdStatus: Int?, status4k: Int?) -> Unit,
	onCancelClick: (pendingRequests: List<JellyseerrRequestDto>) -> Unit,
	onTrailerClick: () -> Unit,
	onPlayClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val hdStatus = mediaInfo?.status
	val status4k = mediaInfo?.status4k
	val requests = mediaInfo?.requests

	val hdDeclined = requests?.any { !it.is4k && it.status == JellyseerrRequestDto.STATUS_DECLINED } == true
	val fourKDeclined = requests?.any { it.is4k && it.status == JellyseerrRequestDto.STATUS_DECLINED } == true

	val isHdBlocked = (hdStatus != null && hdStatus >= 2 && hdStatus != 4) || hdDeclined
	val is4kBlocked = (status4k != null && status4k >= 2 && status4k != 4) || fourKDeclined

	val canRequestHd = !isHdBlocked
	val canRequest4k = !is4kBlocked
	val canRequestAny = canRequestHd || canRequest4k

	val requestLabel = resolveRequestLabel(hdStatus, status4k, hdDeclined, fourKDeclined, canRequestAny)
	val pendingRequests = requests?.filter { it.status == JellyseerrRequestDto.STATUS_PENDING } ?: emptyList()
	val showPlayButton = hdStatus == 5 || hdStatus == 4

	Row(
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = modifier,
	) {
		// Request button — Primary if requestable, Secondary disabled otherwise
		if (canRequestAny) {
			VegafoXButton(
				text = requestLabel,
				variant = VegafoXButtonVariant.Primary,
				icon = VegafoXIcons.HighQuality,
				iconEnd = false,
				autoFocus = true,
				compact = true,
				onClick = { onRequestClick(canRequestHd, canRequest4k, hdStatus, status4k) },
			)
		} else {
			VegafoXButton(
				text = requestLabel,
				variant = VegafoXButtonVariant.Secondary,
				icon = VegafoXIcons.HighQuality,
				iconEnd = false,
				enabled = false,
				compact = true,
				onClick = {},
			)
		}

		// Cancel pending requests
		if (pendingRequests.isNotEmpty()) {
			VegafoXButton(
				text = stringResource(R.string.lbl_cancel),
				variant = VegafoXButtonVariant.Outlined,
				icon = VegafoXIcons.Delete,
				iconEnd = false,
				compact = true,
				onClick = { onCancelClick(pendingRequests) },
			)
		}

		// Trailer
		VegafoXButton(
			text = stringResource(R.string.lbl_trailers),
			variant = VegafoXButtonVariant.Ghost,
			icon = VegafoXIcons.Trailer,
			iconEnd = false,
			compact = true,
			onClick = onTrailerClick,
		)

		// Play on Jellyfin
		if (showPlayButton) {
			VegafoXButton(
				text = stringResource(R.string.lbl_play),
				variant = VegafoXButtonVariant.Secondary,
				icon = VegafoXIcons.Play,
				iconEnd = false,
				compact = true,
				onClick = onPlayClick,
			)
		}
	}
}

@Composable
private fun resolveRequestLabel(
	hdStatus: Int?,
	status4k: Int?,
	hdDeclined: Boolean,
	fourKDeclined: Boolean,
	canRequestAny: Boolean,
): String {
	if (canRequestAny) {
		return when {
			hdStatus == 4 || status4k == 4 -> stringResource(R.string.jellyseerr_request_more)
			else -> stringResource(R.string.jellyseerr_request)
		}
	}
	return when {
		hdDeclined && fourKDeclined -> stringResource(R.string.jellyseerr_status_declined)
		fourKDeclined -> "4K ${stringResource(R.string.jellyseerr_status_declined)}"
		hdDeclined -> "HD ${stringResource(R.string.jellyseerr_status_declined)}"
		hdStatus == 5 && status4k == 5 -> stringResource(R.string.jellyseerr_status_available)
		status4k == 5 -> "4K ${stringResource(R.string.jellyseerr_status_available)}"
		hdStatus == 5 -> "HD ${stringResource(R.string.jellyseerr_status_available)}"
		hdStatus == 3 && status4k == 3 -> stringResource(R.string.jellyseerr_status_processing)
		status4k == 3 -> "4K ${stringResource(R.string.jellyseerr_status_processing)}"
		hdStatus == 3 -> "HD ${stringResource(R.string.jellyseerr_status_processing)}"
		hdStatus == 2 && status4k == 2 -> stringResource(R.string.jellyseerr_status_pending)
		status4k == 2 -> "4K ${stringResource(R.string.jellyseerr_status_pending)}"
		hdStatus == 2 -> "HD ${stringResource(R.string.jellyseerr_status_pending)}"
		hdStatus == 6 || status4k == 6 -> stringResource(R.string.jellyseerr_status_blacklisted)
		else -> stringResource(R.string.jellyseerr_status_unavailable)
	}
}
