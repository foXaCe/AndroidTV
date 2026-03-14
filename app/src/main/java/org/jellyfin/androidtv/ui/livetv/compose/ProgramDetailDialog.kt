package org.jellyfin.androidtv.ui.livetv.compose

import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.data.repository.ItemMutationRepository
import org.jellyfin.androidtv.ui.RecordingIndicatorView
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.components.VegafoXIconButton
import org.jellyfin.androidtv.ui.base.dialog.DialogBase
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.LiveTvDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.livetv.TvManager
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgramDetailDialog(
	visible: Boolean,
	program: BaseItemDto,
	selectedProgramView: RecordingIndicatorView?,
	onDismiss: () -> Unit,
	onTune: () -> Unit,
	onRecordingChanged: () -> Unit,
	onFavoriteChanged: (UUID) -> Unit,
) {
	val context = LocalContext.current
	val api = koinInject<ApiClient>()
	val userRepository = koinInject<UserRepository>()
	val dataRefreshService = koinInject<DataRefreshService>()
	val itemMutationRepository = koinInject<ItemMutationRepository>()
	val scope = rememberCoroutineScope()

	var currentProgram by remember(program) { mutableStateOf(program) }
	var showRecordDialog by remember { mutableStateOf(false) }
	var recordDialogSeries by remember { mutableStateOf(false) }
	var seriesTimerInfo by remember { mutableStateOf<SeriesTimerInfoDto?>(null) }

	val now = LocalDateTime.now()
	val canManageRecordings = Utils.canManageRecordings(userRepository.currentUser.value)
	val hasEnded = currentProgram.endDate?.isBefore(now) != false
	val isInProgress = currentProgram.startDate?.isBefore(now) == true && !hasEnded
	val hasNotStarted = currentProgram.startDate?.isAfter(now) == true
	val focusRequester = remember { FocusRequester() }

	// Record dialog (nested)
	if (showRecordDialog && seriesTimerInfo != null) {
		RecordDialog(
			visible = true,
			program = currentProgram,
			seriesTimerInfo = seriesTimerInfo!!,
			isSeries = recordDialogSeries,
			selectedProgramView = selectedProgramView,
			onDismiss = { showRecordDialog = false },
			onRecordingUpdated = onRecordingChanged,
		)
	}

	DialogBase(
		visible = visible && !showRecordDialog,
		onDismissRequest = onDismiss,
	) {
		Column(
			modifier =
				Modifier
					.width(LiveTvDimensions.programDetailDialogWidth)
					.background(
						VegafoXColors.Surface,
						androidx.compose.foundation.shape
							.RoundedCornerShape(16.dp),
					).verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// TiviMate-style header
			Box(
				modifier =
					Modifier
						.fillMaxWidth()
						.height(80.dp)
						.background(VegafoXColors.SurfaceDim),
			) {
				// Gradient bottom
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(80.dp)
							.background(
								Brush.verticalGradient(
									0.5f to Color.Transparent,
									1f to VegafoXColors.Surface,
								),
							),
				)

				Column(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp, vertical = 12.dp),
				) {
					// Channel name
					val channelName = currentProgram.channelName
					if (channelName != null) {
						Text(
							text = channelName.uppercase(),
							fontSize = 12.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.OrangePrimary,
							letterSpacing = 2.sp,
							maxLines = 1,
						)
					}

					// Title
					Text(
						text = currentProgram.name ?: "",
						fontSize = 28.sp,
						fontFamily = BebasNeue,
						color = VegafoXColors.TextPrimary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)

					// Time range
					val startDate = currentProgram.startDate
					if (startDate != null) {
						val formatter = context.getTimeFormatter()
						val endDate = currentProgram.endDate
						val timeText =
							buildString {
								append(formatter.format(startDate))
								if (endDate != null) {
									append(" - ")
									append(formatter.format(endDate))
								}
							}
						Text(
							text = timeText,
							fontSize = 13.sp,
							color = VegafoXColors.TextSecondary,
							maxLines = 1,
						)
					}
				}
			}

			// Body
			Column(
				modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				// Synopsis
				if (!currentProgram.overview.isNullOrEmpty()) {
					Text(
						text = currentProgram.overview!!,
						fontSize = 14.sp,
						maxLines = 4,
						overflow = TextOverflow.Ellipsis,
						color = VegafoXColors.TextSecondary,
					)
					Spacer(Modifier.height(12.dp))
				}

				// Recording info
				val recordingInfo =
					when {
						hasEnded -> stringResource(R.string.lbl_program_ended)
						currentProgram.timerId != null && isInProgress -> stringResource(R.string.msg_recording_now)
						currentProgram.timerId != null -> stringResource(R.string.msg_will_record)
						currentProgram.seriesTimerId != null && currentProgram.timerId == null ->
							stringResource(R.string.lbl_episode_not_record)
						else -> null
					}
				if (recordingInfo != null) {
					Text(
						text = recordingInfo,
						fontSize = 14.sp,
						color = VegafoXColors.Recording,
					)
					Spacer(Modifier.height(8.dp))
				}

				Spacer(Modifier.height(4.dp))

				// Buttons
				FlowRow(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					var isFirstButton = true

					// Tune button (first if in progress or ended)
					if (isInProgress || hasEnded) {
						VegafoXButton(
							text = stringResource(R.string.lbl_tune_to_channel),
							onClick = {
								onTune()
								onDismiss()
							},
							compact = true,
							autoFocus = isFirstButton,
						)
						isFirstButton = false
					}

					if (!hasEnded && canManageRecordings) {
						// Cancel / Record single
						if (currentProgram.timerId != null) {
							VegafoXButton(
								text = stringResource(R.string.lbl_cancel_recording),
								variant = VegafoXButtonVariant.Outlined,
								compact = true,
								autoFocus = isFirstButton,
								onClick = {
									scope.launch {
										runCatching {
											cancelLiveTvTimer(api, currentProgram.timerId!!)
										}.onSuccess {
											selectedProgramView?.setRecTimer(null)
											currentProgram = currentProgram.copyWithTimerId(null)
											onDismiss()
											Utils.showToast(context, R.string.msg_recording_cancelled)
											onRecordingChanged()
										}
									}
								},
							)
							isFirstButton = false
						} else {
							VegafoXButton(
								text = stringResource(R.string.lbl_record),
								variant = VegafoXButtonVariant.Secondary,
								compact = true,
								autoFocus = isFirstButton,
								onClick = {
									scope.launch {
										runCatching {
											recordLiveTvProgram(api, currentProgram.id)
										}.onSuccess { updatedProgram ->
											currentProgram = updatedProgram
											selectedProgramView?.setRecSeriesTimer(updatedProgram.seriesTimerId)
											selectedProgramView?.setRecTimer(updatedProgram.timerId)
											Utils.showToast(context, R.string.msg_set_to_record)
											onDismiss()
											onRecordingChanged()
										}
									}
								},
							)
							isFirstButton = false
						}

						// Series recording controls
						if (Utils.isTrue(currentProgram.isSeries)) {
							if (currentProgram.seriesTimerId != null) {
								// Cancel series
								VegafoXButton(
									text = stringResource(R.string.lbl_cancel_series),
									variant = VegafoXButtonVariant.Outlined,
									compact = true,
									onClick = {
										AlertDialog
											.Builder(context)
											.setTitle(R.string.lbl_cancel_series)
											.setMessage(R.string.msg_cancel_entire_series)
											.setNegativeButton(R.string.lbl_no, null)
											.setPositiveButton(R.string.lbl_yes) { _, _ ->
												scope.launch {
													runCatching {
														cancelLiveTvSeriesTimer(api, currentProgram.seriesTimerId!!)
													}.onSuccess {
														selectedProgramView?.setRecSeriesTimer(null)
														currentProgram = currentProgram.copyWithSeriesTimerId(null)
														onDismiss()
														Utils.showToast(context, R.string.msg_recording_cancelled)
														onRecordingChanged()
													}
												}
											}.show()
									},
								)

								// Series settings
								VegafoXButton(
									text = stringResource(R.string.lbl_series_settings),
									variant = VegafoXButtonVariant.Ghost,
									compact = true,
									onClick = {
										scope.launch {
											runCatching {
												getLiveTvSeriesTimer(api, currentProgram.seriesTimerId!!)
											}.onSuccess { timer ->
												seriesTimerInfo = timer
												recordDialogSeries = true
												showRecordDialog = true
											}
										}
									},
								)
							} else {
								// Record series
								VegafoXButton(
									text = stringResource(R.string.lbl_record_series),
									variant = VegafoXButtonVariant.Secondary,
									compact = true,
									onClick = {
										scope.launch {
											runCatching {
												recordLiveTvSeries(api, currentProgram.id)
											}.onSuccess { updatedProgram ->
												currentProgram = updatedProgram
												selectedProgramView?.setRecSeriesTimer(updatedProgram.seriesTimerId)
												selectedProgramView?.setRecTimer(updatedProgram.timerId)
												Utils.showToast(context, R.string.msg_set_to_record)
												onDismiss()
												onRecordingChanged()
											}
										}
									},
								)
							}
						}
					}

					// Tune button (last if not in progress)
					if (hasNotStarted) {
						VegafoXButton(
							text = stringResource(R.string.lbl_tune_to_channel),
							compact = true,
							autoFocus = isFirstButton,
							onClick = {
								onTune()
								onDismiss()
							},
						)
						isFirstButton = false
					}

					// Favorite button
					if (!hasEnded) {
						val channelIndex = TvManager.getAllChannelsIndex(currentProgram.channelId ?: UUID.randomUUID())
						if (channelIndex >= 0) {
							val channel = TvManager.getChannel(channelIndex)
							var isFav by remember(channel) {
								mutableStateOf(channel.userData?.isFavorite == true)
							}

							VegafoXIconButton(
								icon = if (isFav) VegafoXIcons.Favorite else VegafoXIcons.FavoriteOutlined,
								contentDescription = "Favorite",
								tint = if (isFav) VegafoXColors.OrangePrimary else VegafoXColors.TextHint,
								onClick = {
									scope.launch {
										runCatching {
											toggleLiveTvFavorite(itemMutationRepository, channel)
										}.onSuccess { updatedChannel ->
											isFav = updatedChannel.userData?.isFavorite == true
											onFavoriteChanged(updatedChannel.id)
											dataRefreshService.lastFavoriteUpdate = Instant.now()
										}
									}
								},
							)
						}
					}
				}

				Spacer(Modifier.height(16.dp))
			}
		}
	}
}
