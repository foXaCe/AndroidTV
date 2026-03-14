package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaInfoDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun JellyseerrStatusBadge(mediaInfo: JellyseerrMediaInfoDto?) {
	val (statusText, statusColor) = resolveStatusBadge(mediaInfo)

	Text(
		text = statusText,
		style = JellyfinTheme.typography.labelSmall,
		color = statusColor,
		modifier =
			Modifier
				.clip(JellyfinTheme.shapes.full)
				.background(statusColor.copy(alpha = 0.20f))
				.padding(horizontal = 16.dp, vertical = 6.dp),
	)
}

@Composable
private fun resolveStatusBadge(mediaInfo: JellyseerrMediaInfoDto?): Pair<String, Color> {
	val status = mediaInfo?.status
	val status4k = mediaInfo?.status4k
	val requests = mediaInfo?.requests
	val hdDeclined = requests?.any { !it.is4k && it.status == JellyseerrRequestDto.STATUS_DECLINED } == true
	val fourKDeclined = requests?.any { it.is4k && it.status == JellyseerrRequestDto.STATUS_DECLINED } == true

	return when {
		hdDeclined && fourKDeclined -> stringResource(R.string.jellyseerr_status_hd_4k_declined) to VegafoXColors.Error
		fourKDeclined -> stringResource(R.string.jellyseerr_status_4k_declined) to VegafoXColors.Error
		hdDeclined -> stringResource(R.string.jellyseerr_status_hd_declined) to VegafoXColors.Error

		status == 6 && status4k == 6 -> stringResource(R.string.jellyseerr_status_hd_4k_blacklisted) to VegafoXColors.Error
		status4k == 6 -> stringResource(R.string.jellyseerr_status_4k_blacklisted) to VegafoXColors.Error
		status == 6 -> stringResource(R.string.jellyseerr_status_hd_blacklisted) to VegafoXColors.Error

		status == 5 && status4k == 5 -> stringResource(R.string.jellyseerr_status_hd_4k_available) to VegafoXColors.Success
		status4k == 5 -> stringResource(R.string.jellyseerr_status_4k_available) to VegafoXColors.Success
		status == 5 -> stringResource(R.string.jellyseerr_status_hd_available) to VegafoXColors.Success

		status == 4 && status4k == 4 -> stringResource(R.string.jellyseerr_status_hd_4k_partial) to VegafoXColors.Success
		status4k == 4 -> stringResource(R.string.jellyseerr_status_4k_partial) to VegafoXColors.Success
		status == 4 -> stringResource(R.string.jellyseerr_status_hd_partial) to VegafoXColors.Success

		status == 3 && status4k == 3 -> stringResource(R.string.jellyseerr_status_hd_4k_processing) to VegafoXColors.OrangePrimary
		status4k == 3 -> stringResource(R.string.jellyseerr_status_4k_processing) to VegafoXColors.OrangePrimary
		status == 3 -> stringResource(R.string.jellyseerr_status_hd_processing) to VegafoXColors.OrangePrimary

		status == 2 && status4k == 2 -> stringResource(R.string.jellyseerr_status_hd_4k_pending) to VegafoXColors.OrangePrimary
		status4k == 2 -> stringResource(R.string.jellyseerr_status_4k_pending) to VegafoXColors.OrangePrimary
		status == 2 -> stringResource(R.string.jellyseerr_status_hd_pending) to VegafoXColors.OrangePrimary

		status == 1 && status4k == 1 -> stringResource(R.string.jellyseerr_status_hd_4k_unknown) to VegafoXColors.TextSecondary
		status4k == 1 -> stringResource(R.string.jellyseerr_status_4k_unknown) to VegafoXColors.TextSecondary
		status == 1 -> stringResource(R.string.jellyseerr_status_hd_unknown) to VegafoXColors.TextSecondary

		else -> stringResource(R.string.jellyseerr_status_not_requested) to VegafoXColors.TextDisabled
	}
}
