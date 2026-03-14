package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun QualitySelectionDialog(
	title: String,
	canRequestHd: Boolean,
	canRequest4k: Boolean,
	hdStatus: Int?,
	status4k: Int?,
	onSelect: (is4k: Boolean) -> Unit,
	onDismiss: () -> Unit,
) {
	val isHdBlocked = !canRequestHd || isStatusBlocked(hdStatus)
	val is4kBlocked = !canRequest4k || isStatusBlocked(status4k)

	val firstFocusTarget =
		when {
			!isHdBlocked -> FocusTarget.HD
			!is4kBlocked -> FocusTarget.FOUR_K
			else -> FocusTarget.CANCEL
		}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		val dialogShape =
			androidx.compose.foundation.shape
				.RoundedCornerShape(16.dp)
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = 380.dp, max = 500.dp)
						.clip(dialogShape)
						.background(VegafoXColors.Surface)
						.border(1.dp, Color.White.copy(alpha = 0.10f), dialogShape)
						.padding(horizontal = 32.dp, vertical = 24.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = stringResource(R.string.jellyseerr_select_quality),
					style =
						TextStyle(
							fontFamily = BebasNeue,
							fontSize = 22.sp,
							letterSpacing = 2.sp,
							color = VegafoXColors.TextPrimary,
						),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
				)

				Text(
					text = title,
					style =
						TextStyle(
							fontSize = 14.sp,
							color = VegafoXColors.TextSecondary,
						),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
				)

				// HD button
				VegafoXButton(
					text = getQualityLabel(false, hdStatus),
					variant = VegafoXButtonVariant.Primary,
					enabled = !isHdBlocked,
					autoFocus = firstFocusTarget == FocusTarget.HD,
					compact = true,
					onClick = {
						onSelect(false)
						onDismiss()
					},
				)

				Spacer(modifier = Modifier.height(12.dp))

				// 4K button
				VegafoXButton(
					text = getQualityLabel(true, status4k),
					variant = VegafoXButtonVariant.Primary,
					enabled = !is4kBlocked,
					autoFocus = firstFocusTarget == FocusTarget.FOUR_K,
					compact = true,
					onClick = {
						onSelect(true)
						onDismiss()
					},
				)

				Spacer(modifier = Modifier.height(24.dp))

				// Cancel button
				VegafoXButton(
					text = stringResource(R.string.btn_cancel),
					variant = VegafoXButtonVariant.Ghost,
					autoFocus = firstFocusTarget == FocusTarget.CANCEL,
					compact = true,
					onClick = onDismiss,
				)
			}
		}
	}
}

@Composable
private fun getQualityLabel(
	is4k: Boolean,
	status: Int?,
): String {
	val qualityPrefix = if (is4k) "4K" else "HD"
	return when (status) {
		2 -> stringResource(R.string.jellyseerr_quality_pending, qualityPrefix)
		3 -> stringResource(R.string.jellyseerr_quality_processing, qualityPrefix)
		4 -> stringResource(R.string.jellyseerr_quality_request_more, qualityPrefix)
		5 -> stringResource(R.string.jellyseerr_quality_available, qualityPrefix)
		6 -> stringResource(R.string.jellyseerr_quality_blacklisted, qualityPrefix)
		else -> stringResource(R.string.jellyseerr_quality_request, qualityPrefix)
	}
}

private fun isStatusBlocked(status: Int?): Boolean = status != null && status >= 2 && status != 4

private enum class FocusTarget { HD, FOUR_K, CANCEL }
