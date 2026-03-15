package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXIconButton
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.LiveTvDimensions
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.livetv.LiveTvGuideUiState
import org.jellyfin.androidtv.ui.livetv.LiveTvGuideViewModel
import org.jellyfin.androidtv.ui.navigation.ProvideRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsDialog
import org.jellyfin.androidtv.ui.settings.composable.SettingsRouterContent
import org.jellyfin.androidtv.ui.settings.routes
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun LiveTvGuideScreen(
	viewModel: LiveTvGuideViewModel,
	onTuneToChannel: (UUID) -> Unit,
	onDismiss: (() -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	val uiState by viewModel.uiState.collectAsState()
	val context = LocalContext.current
	val imageHelper = koinInject<ImageHelper>()

	val lazyListState = rememberLazyListState()
	val horizontalScrollState = rememberScrollState()

	var showOptionsDialog by remember { mutableStateOf(false) }
	var showFiltersDialog by remember { mutableStateOf(false) }
	var showDateDialog by remember { mutableStateOf(false) }
	var showProgramDetail by remember { mutableStateOf(false) }
	var detailProgram by remember { mutableStateOf<BaseItemDto?>(null) }

	// Load guide on first composition
	LaunchedEffect(Unit) {
		viewModel.loadGuide()
	}

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep),
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			// Header: TiviMate-style full-width banner
			GuideHeader(
				uiState = uiState,
				imageHelper = imageHelper,
				onOptionsClick = { showOptionsDialog = true },
				onFiltersClick = { showFiltersDialog = true },
				onDateClick = { showDateDialog = true },
				onResetClick = { viewModel.pageGuideTo(LocalDateTime.now()) },
			)

			// Timeline
			Row(Modifier.fillMaxWidth()) {
				// Date display aligned with channel headers
				Box(
					modifier =
						Modifier
							.width(TvSpacing.channelHeaderWidth)
							.height(TvSpacing.timelineHeight)
							.background(VegafoXColors.Surface),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = TimeUtils.getFriendlyDate(context, uiState.guideStart),
						fontSize = 14.sp,
						fontWeight = FontWeight.Bold,
						color = VegafoXColors.TextSecondary,
					)
				}

				GuideTimeline(
					startTime = uiState.guideStart,
					endTime = uiState.guideEnd,
					scrollState = horizontalScrollState,
					modifier = Modifier.weight(1f),
				)
			}

			// Grid or loading
			if (uiState.isLoading && uiState.filteredChannels.isEmpty()) {
				Box(
					modifier =
						Modifier
							.fillMaxSize()
							.background(VegafoXColors.BackgroundDeep.copy(alpha = 0.90f)),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(R.string.lbl_loading_elipses),
						fontSize = 18.sp,
						color = VegafoXColors.TextSecondary,
					)
				}
			} else {
				// Status bar
				GuideStatusBar(uiState)

				// Guide grid
				LiveTvGuideGrid(
					channels = uiState.filteredChannels,
					programs = uiState.programs,
					guideStart = uiState.guideStart,
					guideEnd = uiState.guideEnd,
					lazyListState = lazyListState,
					horizontalScrollState = horizontalScrollState,
					onChannelFocus = { channel ->
						viewModel.selectChannel(channel)
						val channelPrograms = uiState.programs[channel.id] ?: emptyList()
						val now = LocalDateTime.now()
						val currentProgram =
							channelPrograms.find { p ->
								p.startDate?.isBefore(now) == true && p.endDate?.isAfter(now) == true
							}
						if (currentProgram != null) viewModel.selectProgram(currentProgram)
					},
					onProgramFocus = { program ->
						viewModel.selectProgram(program)
					},
					onProgramClick = { program ->
						val channelId = program.channelId ?: return@LiveTvGuideGrid
						if (program.startDate?.isBefore(LocalDateTime.now()) == true) {
							onTuneToChannel(channelId)
						} else {
							detailProgram = program
							showProgramDetail = true
						}
					},
					onProgramLongClick = { program ->
						detailProgram = program
						showProgramDetail = true
					},
					onChannelClick = { channel ->
						onTuneToChannel(channel.id)
					},
					onChannelLongClick = { channel ->
						viewModel.toggleFavorite(channel)
					},
					modifier = Modifier.weight(1f),
				)
			}
		}

		// Program detail dialog
		if (showProgramDetail && detailProgram != null) {
			ProgramDetailDialog(
				visible = true,
				program = detailProgram!!,
				selectedProgramView = null,
				onDismiss = { showProgramDetail = false },
				onTune = {
					val channelId = detailProgram?.channelId
					if (channelId != null) onTuneToChannel(channelId)
				},
				onRecordingChanged = {
					viewModel.forceReload()
				},
				onFavoriteChanged = { channelId ->
					viewModel.refreshFavorite(channelId)
				},
			)
		}

		// Settings options dialog
		ProvideRouter(routes, Routes.LIVETV_GUIDE_OPTIONS) {
			SettingsDialog(
				visible = showOptionsDialog,
				onDismissRequest = {
					showOptionsDialog = false
					viewModel.forceReload()
				},
			) {
				SettingsRouterContent()
			}
		}

		// Settings filters dialog
		ProvideRouter(routes, Routes.LIVETV_GUIDE_FILTERS) {
			SettingsDialog(
				visible = showFiltersDialog,
				onDismissRequest = {
					showFiltersDialog = false
					viewModel.forceReload()
				},
			) {
				SettingsRouterContent()
			}
		}

		// Date picker dialog (pure Compose — replaces native AlertDialog+FriendlyDateButton)
		if (showDateDialog) {
			GuideDatePickerDialog(
				onDatePicked = { date ->
					showDateDialog = false
					viewModel.pageGuideTo(date)
				},
				onDismiss = { showDateDialog = false },
			)
		}
	}
}

@Composable
private fun GuideHeader(
	uiState: LiveTvGuideUiState,
	imageHelper: ImageHelper,
	onOptionsClick: () -> Unit,
	onFiltersClick: () -> Unit,
	onDateClick: () -> Unit,
	onResetClick: () -> Unit,
) {
	val selectedProgram = uiState.selectedProgram
	val context = LocalContext.current

	Box(
		modifier =
			Modifier
				.fillMaxWidth()
				.height(LiveTvDimensions.guideHeaderHeight),
	) {
		// Backdrop blurred image
		val backdropUrl =
			selectedProgram?.let {
				imageHelper.getPrimaryImageUrl(it, null, ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT)
			}
		if (backdropUrl != null) {
			AsyncImage(
				url = backdropUrl,
				modifier =
					Modifier
						.fillMaxSize()
						.alpha(0.25f),
				scaleType = android.widget.ImageView.ScaleType.CENTER_CROP,
			)
		}

		// Gradient left
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(
						Brush.horizontalGradient(
							0f to VegafoXColors.BackgroundDeep,
							0.4f to Color.Transparent,
						),
					),
		)

		// Gradient bottom
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(
						Brush.verticalGradient(
							0.7f to Color.Transparent,
							1f to VegafoXColors.BackgroundDeep,
						),
					),
		)

		// Content
		Row(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(horizontal = 24.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			// Thumbnail
			val thumbUrl =
				selectedProgram?.let {
					imageHelper.getPrimaryImageUrl(it, null, ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT)
				}
			if (thumbUrl != null) {
				AsyncImage(
					url = thumbUrl,
					modifier =
						Modifier
							.size(width = 80.dp, height = 60.dp)
							.background(
								VegafoXColors.SurfaceDim,
								shape =
									androidx.compose.foundation.shape
										.RoundedCornerShape(8.dp),
							),
					scaleType = android.widget.ImageView.ScaleType.CENTER_CROP,
				)
				Spacer(Modifier.width(16.dp))
			}

			// Program info
			Column(modifier = Modifier.weight(1f)) {
				// Channel name
				val channelName = selectedProgram?.channelName
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

				// Program title
				Text(
					text = selectedProgram?.name ?: "",
					fontSize = 28.sp,
					fontFamily = BebasNeue,
					color = VegafoXColors.TextPrimary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)

				// Time range
				val startDate = selectedProgram?.startDate
				val endDate = selectedProgram?.endDate
				if (startDate != null) {
					val formatter = context.getTimeFormatter()
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

				// Synopsis
				val overview = selectedProgram?.overview
				if (!overview.isNullOrEmpty()) {
					Text(
						text = overview,
						fontSize = 13.sp,
						color = VegafoXColors.TextHint,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}

			Spacer(Modifier.width(16.dp))

			// Action buttons
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (uiState.guideStart.isAfter(LocalDateTime.now())) {
					VegafoXIconButton(
						icon = VegafoXIcons.Schedule,
						contentDescription = "Reset",
						onClick = onResetClick,
						tint = VegafoXColors.TextSecondary,
					)
					Spacer(Modifier.width(8.dp))
				}

				VegafoXIconButton(
					icon = VegafoXIcons.Calendar,
					contentDescription = stringResource(R.string.lbl_select_date),
					onClick = onDateClick,
					tint = VegafoXColors.TextSecondary,
				)

				Spacer(Modifier.width(8.dp))

				VegafoXIconButton(
					icon = VegafoXIcons.Filter,
					contentDescription = stringResource(R.string.lbl_filters),
					onClick = onFiltersClick,
					tint = VegafoXColors.TextSecondary,
				)

				Spacer(Modifier.width(8.dp))

				VegafoXIconButton(
					icon = VegafoXIcons.Settings,
					contentDescription = stringResource(R.string.lbl_other_options),
					onClick = onOptionsClick,
					tint = VegafoXColors.TextSecondary,
				)
			}
		}
	}
}

@Composable
private fun GuideStatusBar(uiState: LiveTvGuideUiState) {
	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.padding(horizontal = 8.dp, vertical = 2.dp),
	) {
		Text(
			text =
				stringResource(
					R.string.lbl_x_channels,
					uiState.filteredChannels.size,
				),
			fontSize = 12.sp,
			color = VegafoXColors.TextHint,
		)
		Spacer(Modifier.weight(1f))
		Text(
			text =
				stringResource(
					R.string.lbl_guide_hours,
					uiState.guideHours,
				),
			fontSize = 12.sp,
			color = VegafoXColors.TextHint,
		)
	}
}

@Composable
private fun GuideDatePickerDialog(
	onDatePicked: (LocalDateTime) -> Unit,
	onDismiss: () -> Unit,
) {
	val dates = remember { (0L until 15L).map { LocalDateTime.now().plusDays(it) } }
	val formatter =
		remember {
			java.time.format.DateTimeFormatter
				.ofPattern("EEE d MMM")
		}

	androidx.compose.material3.AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		titleContentColor = VegafoXColors.TextPrimary,
		title = {
			Text(
				text = stringResource(R.string.lbl_select_date),
				fontFamily = BebasNeue,
				fontSize = 24.sp,
				fontWeight = FontWeight.Bold,
				color = VegafoXColors.TextPrimary,
			)
		},
		text = {
			androidx.compose.foundation.lazy.LazyColumn(
				modifier = Modifier.height(LiveTvDimensions.guideHeaderHeight * 3),
			) {
				items(dates.size) { index ->
					val date = dates[index]
					val isToday = index == 0
					val label =
						if (isToday) {
							stringResource(R.string.lbl_today)
						} else {
							date.format(formatter).replaceFirstChar { it.uppercase() }
						}
					Row(
						modifier =
							Modifier
								.fillMaxWidth()
								.clickable { onDatePicked(date) }
								.padding(vertical = 12.dp, horizontal = 16.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = label,
							color = if (isToday) VegafoXColors.OrangePrimary else VegafoXColors.TextPrimary,
							fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
							fontSize = 16.sp,
						)
					}
				}
			}
		},
		confirmButton = {},
		dismissButton = {
			androidx.compose.material3.TextButton(onClick = onDismiss) {
				Text(
					text = stringResource(R.string.btn_cancel),
					color = VegafoXColors.TextSecondary,
				)
			}
		},
	)
}
