package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaInfoDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.ui.itemdetail.v2.DetailActionButton

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
		DetailActionButton(
			label = requestLabel,
			icon = ImageVector.vectorResource(R.drawable.ic_select_quality),
			onClick = {
				if (canRequestAny) {
					onRequestClick(canRequestHd, canRequest4k, hdStatus, status4k)
				}
			},
			modifier = if (!canRequestAny) Modifier.alpha(0.5f) else Modifier,
		)

		if (pendingRequests.isNotEmpty()) {
			DetailActionButton(
				label = stringResource(R.string.lbl_cancel),
				icon = ImageVector.vectorResource(R.drawable.ic_delete),
				onClick = { onCancelClick(pendingRequests) },
			)
		}

		DetailActionButton(
			label = stringResource(R.string.lbl_trailers),
			icon = ImageVector.vectorResource(R.drawable.ic_trailer),
			onClick = onTrailerClick,
		)

		if (showPlayButton) {
			DetailActionButton(
				label = stringResource(R.string.lbl_play),
				icon = ImageVector.vectorResource(R.drawable.ic_play),
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
