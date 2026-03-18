package org.jellyfin.androidtv.ui.livetv.compose

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.CustomMessage
import org.jellyfin.androidtv.data.repository.CustomMessageRepository
import org.jellyfin.androidtv.ui.RecordingIndicatorView
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.dialog.DialogBase
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.LiveTvDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.getQuantityString
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset

private val PADDING_VALUES = listOf(0, 60, 300, 900, 1800, 3600, 5400, 7200, 10800)

@Composable
fun RecordDialog(
	visible: Boolean,
	program: BaseItemDto,
	seriesTimerInfo: SeriesTimerInfoDto,
	isSeries: Boolean,
	selectedProgramView: RecordingIndicatorView?,
	onDismiss: () -> Unit,
	onRecordingUpdated: () -> Unit,
) {
	val context = LocalContext.current
	val api = koinInject<ApiClient>()
	val customMessageRepository = koinInject<CustomMessageRepository>()
	val scope = rememberCoroutineScope()

	val paddingLabels =
		remember {
			listOf(
				context.getString(R.string.lbl_on_schedule),
				context.getQuantityString(R.plurals.minutes, 1),
				context.getQuantityString(R.plurals.minutes, 5),
				context.getQuantityString(R.plurals.minutes, 15),
				context.getQuantityString(R.plurals.minutes, 30),
				context.getQuantityString(R.plurals.minutes, 60),
				context.getQuantityString(R.plurals.minutes, 90),
				context.getQuantityString(R.plurals.hours, 2),
				context.getQuantityString(R.plurals.hours, 3),
			)
		}

	var currentOptions by remember(seriesTimerInfo) { mutableStateOf(seriesTimerInfo) }
	var prePaddingIndex by remember(seriesTimerInfo) {
		mutableIntStateOf(findPaddingIndex(seriesTimerInfo.prePaddingSeconds ?: 0))
	}
	var postPaddingIndex by remember(seriesTimerInfo) {
		mutableIntStateOf(findPaddingIndex(seriesTimerInfo.postPaddingSeconds ?: 0))
	}
	var onlyNew by remember(seriesTimerInfo) { mutableStateOf(seriesTimerInfo.recordNewOnly == true) }
	var anyTime by remember(seriesTimerInfo) { mutableStateOf(seriesTimerInfo.recordAnyTime == true) }
	var anyChannel by remember(seriesTimerInfo) { mutableStateOf(seriesTimerInfo.recordAnyChannel == true) }

	DialogBase(
		visible = visible,
		onDismissRequest = onDismiss,
	) {
		Column(
			modifier =
				Modifier
					.width(LiveTvDimensions.recordDialogWidth)
					.background(VegafoXColors.Surface, RoundedCornerShape(16.dp))
					.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// Title
			Text(
				text = program.name ?: "",
				fontSize = 22.sp,
				fontFamily = BebasNeue,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = VegafoXColors.TextPrimary,
			)

			Spacer(Modifier.height(8.dp))

			// Timeline
			TimelineRow(program)

			Spacer(Modifier.height(20.dp))

			// Padding selectors
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly,
			) {
				PaddingSelector(
					label = stringResource(R.string.lbl_begin_padding),
					currentLabel = paddingLabels[prePaddingIndex],
					onNext = {
						prePaddingIndex = (prePaddingIndex + 1) % PADDING_VALUES.size
						currentOptions = currentOptions.copyWithPrePaddingSeconds(PADDING_VALUES[prePaddingIndex])
					},
				)

				PaddingSelector(
					label = stringResource(R.string.lbl_end_padding),
					currentLabel = paddingLabels[postPaddingIndex],
					onNext = {
						postPaddingIndex = (postPaddingIndex + 1) % PADDING_VALUES.size
						currentOptions = currentOptions.copyWithPostPaddingSeconds(PADDING_VALUES[postPaddingIndex])
					},
				)
			}

			// Series options
			if (isSeries) {
				Spacer(Modifier.height(16.dp))

				Text(
					text = stringResource(R.string.lbl_repeat_options),
					fontSize = 14.sp,
					color = VegafoXColors.TextSecondary,
				)

				Spacer(Modifier.height(8.dp))

				CheckboxRow(
					label = stringResource(R.string.lbl_only_new_episodes),
					checked = onlyNew,
					onToggle = { onlyNew = !onlyNew },
				)
				CheckboxRow(
					label = stringResource(R.string.lbl_record_any_time),
					checked = anyTime,
					onToggle = { anyTime = !anyTime },
				)
				CheckboxRow(
					label = stringResource(R.string.lbl_record_any_channel),
					checked = anyChannel,
					onToggle = { anyChannel = !anyChannel },
				)
			}

			Spacer(Modifier.height(20.dp))

			// Buttons
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				VegafoXButton(
					text = stringResource(R.string.lbl_save),
					compact = true,
					autoFocus = true,
					onClick = {
						scope.launch {
							runCatching {
								if (isSeries) {
									val finalOptions = currentOptions.copyWithFilters(onlyNew, anyChannel, anyTime)
									updateLiveTvSeriesTimer(api, finalOptions)
								} else {
									val timer = createProgramTimerInfo(program.id, currentOptions)
									updateLiveTvTimer(api, timer)
								}
							}.onSuccess {
								onDismiss()
								customMessageRepository.pushMessage(CustomMessage.ActionComplete)
								if (isSeries) {
									Toast.makeText(context, R.string.msg_settings_updated, Toast.LENGTH_LONG).show()
								} else {
									val updatedProgram = getLiveTvProgram(api, program.id)
									selectedProgramView?.setRecTimer(updatedProgram.timerId)
									selectedProgramView?.setRecSeriesTimer(updatedProgram.seriesTimerId)
									Toast.makeText(context, R.string.msg_set_to_record, Toast.LENGTH_LONG).show()
								}
								onRecordingUpdated()
							}
						}
					},
				)

				VegafoXButton(
					text = stringResource(R.string.lbl_cancel),
					variant = VegafoXButtonVariant.Ghost,
					compact = true,
					onClick = onDismiss,
				)
			}
		}
	}
}

@Composable
internal fun TimelineRow(program: BaseItemDto) {
	val context = LocalContext.current
	val startDate = program.startDate ?: return

	Row(
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = stringResource(R.string.lbl_on),
			fontSize = 14.sp,
			color = VegafoXColors.TextSecondary,
		)
		Text(
			text = program.channelName ?: "",
			fontSize = 14.sp,
			fontWeight = FontWeight.Bold,
			color = VegafoXColors.OrangePrimary,
		)
		Text(
			text =
				buildString {
					append(TimeUtils.getFriendlyDate(context, startDate))
					append(" @ ")
					append(context.getTimeFormatter().format(startDate))
					append(" (")
					append(
						DateUtils.getRelativeTimeSpanString(
							startDate.toInstant(ZoneOffset.UTC).toEpochMilli(),
							Instant.now().toEpochMilli(),
							0,
						),
					)
					append(")")
				},
			fontSize = 14.sp,
			color = VegafoXColors.TextSecondary,
		)
	}
}

@Composable
private fun PaddingSelector(
	label: String,
	currentLabel: String,
	onNext: () -> Unit,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = label,
			fontSize = 14.sp,
			color = VegafoXColors.TextSecondary,
		)
		Spacer(Modifier.height(8.dp))
		VegafoXButton(
			text = currentLabel,
			variant = VegafoXButtonVariant.Secondary,
			compact = true,
			onClick = onNext,
		)
	}
}

@Composable
private fun CheckboxRow(
	label: String,
	checked: Boolean,
	onToggle: () -> Unit,
) {
	VegafoXButton(
		text = label,
		variant = VegafoXButtonVariant.Ghost,
		compact = true,
		onClick = onToggle,
		icon = null,
	)
}

private fun findPaddingIndex(seconds: Int): Int {
	for (i in PADDING_VALUES.indices) {
		if (PADDING_VALUES[i] > seconds) return (i - 1).coerceAtLeast(0)
	}
	return 0
}
