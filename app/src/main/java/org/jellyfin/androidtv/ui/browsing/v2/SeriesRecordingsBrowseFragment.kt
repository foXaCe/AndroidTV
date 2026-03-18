package org.jellyfin.androidtv.ui.browsing.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonLandscapeCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.browsing.compose.MediaPosterCard
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.components.BrowseHeader
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SeriesRecordingsBrowseFragment : Fragment() {
	private val viewModel: SeriesRecordingsBrowseViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		val mainContainer =
			FrameLayout(requireContext()).apply {
				layoutParams =
					ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT,
					)
			}

		val contentView =
			ComposeView(requireContext()).apply {
				layoutParams =
					FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT,
					)
				setContent {
					JellyfinTheme {
						ScreenIdOverlay(ScreenIds.SERIES_RECORDINGS_ID, ScreenIds.SERIES_RECORDINGS_NAME) {
							VegafoXScaffold {
								SeriesRecordingsContent()
							}
						}
					}
				}
			}
		mainContainer.addView(contentView)

		return mainContainer
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.initialize()
	}

	@Composable
	private fun SeriesRecordingsContent() {
		val uiState by viewModel.uiState.collectAsState()

		Box(modifier = Modifier.fillMaxSize()) {
			AppBackground()

			val currentBg by backgroundService.currentBackground.collectAsState()
			val overlayAlpha = if (currentBg != null) 0.45f else 0.75f
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.background(VegafoXColors.SurfaceDim.copy(alpha = overlayAlpha)),
			)

			Column(modifier = Modifier.fillMaxSize()) {
				BrowseHeader(
					title = stringResource(R.string.lbl_series_recordings),
				) {
					LibraryToolbarButton(
						icon = VegafoXIcons.Home,
						contentDescription = stringResource(R.string.home),
						onClick = { navigationRepository.navigate(Destinations.home) },
					)
				}

				Spacer(modifier = Modifier.height(6.dp))

				SeriesTimerHud(
					timer = uiState.focusedTimer,
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = BrowseDimensions.gridPaddingHorizontal),
				)

				Spacer(modifier = Modifier.height(6.dp))

				val displayState =
					when {
						uiState.isLoading -> DisplayState.LOADING
						uiState.error != null -> DisplayState.ERROR
						else -> DisplayState.CONTENT
					}
				StateContainer(
					state = displayState,
					modifier = Modifier.weight(1f),
					loadingContent = {
						Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
							SkeletonLandscapeCardRow()
							SkeletonLandscapeCardRow()
						}
					},
					emptyContent = {
						EmptyState(
							title = stringResource(R.string.state_empty_recordings),
							message = stringResource(R.string.state_empty_recordings_message),
						)
					},
					errorContent = {
						ErrorState(
							message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
							onRetry = { viewModel.retry() },
						)
					},
					content = {
						SeriesRecordingsRows(
							uiState = uiState,
							modifier = Modifier.fillMaxSize(),
						)
					},
				)

				LibraryStatusBar(
					statusText = stringResource(R.string.lbl_series_recordings),
					counterText = "",
				)
			}
		}
	}

	@Composable
	private fun SeriesTimerHud(
		timer: SeriesTimerInfoDto?,
		modifier: Modifier = Modifier,
	) {
		Box(
			modifier = modifier.height(40.dp),
			contentAlignment = Alignment.CenterStart,
		) {
			if (timer != null) {
				Column {
					Text(
						text = timer.name ?: "",
						style = JellyfinTheme.typography.titleMedium,
						color = VegafoXColors.TextPrimary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					val subtitle = buildSeriesTimerSubtitle(timer)
					if (subtitle.isNotEmpty()) {
						Text(
							text = subtitle,
							style = JellyfinTheme.typography.bodySmall,
							color = VegafoXColors.TextSecondary,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
					}
				}
			}
		}
	}

	@Composable
	private fun SeriesRecordingsRows(
		uiState: SeriesRecordingsBrowseUiState,
		modifier: Modifier = Modifier,
	) {
		val scrollState = rememberScrollState()

		Column(
			modifier =
				modifier
					.fillMaxWidth()
					.verticalScroll(scrollState)
					.padding(bottom = 16.dp),
		) {
			if (uiState.seriesTimers.isEmpty()) {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 40.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(R.string.lbl_no_items),
						style = JellyfinTheme.typography.titleMedium,
						color = VegafoXColors.TextHint,
					)
				}
			} else {
				Column(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = BrowseDimensions.rowTopPadding),
				) {
					Text(
						text = stringResource(R.string.lbl_series_recordings),
						style = JellyfinTheme.typography.titleMedium,
						color = VegafoXColors.TextPrimary,
						modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = BrowseDimensions.rowTitleBottomPadding),
					)

					LazyRow(
						horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
						contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
					) {
						items(uiState.seriesTimers, key = { it.id ?: it.name.orEmpty() }) { timer ->
							MediaPosterCard(
								imageUrl = null,
								title = timer.name ?: "",
								cardWidth = CardDimensions.landscapeWidth,
								cardHeight = CardDimensions.landscapeHeight,
								onClick = { launchSeriesTimer(timer) },
								onFocused = { viewModel.setFocusedTimer(timer) },
								subtitle = buildSeriesTimerSubtitle(timer),
								placeholderIcon = VegafoXIcons.RecordSeries,
							)
						}
					}
				}
			}
		}
	}

	private fun buildSeriesTimerSubtitle(timer: SeriesTimerInfoDto): String {
		val parts = mutableListOf<String>()
		val channelText =
			if (timer.recordAnyChannel == true) {
				getString(R.string.all_channels)
			} else {
				timer.channelName
			}
		channelText?.let { if (it.isNotBlank()) parts.add(it) }
		timer.dayPattern?.let { parts.add(it.toString()) }
		return parts.joinToString(" • ")
	}

	private fun launchSeriesTimer(timer: SeriesTimerInfoDto) {
		val id = timer.id?.toUUIDOrNull() ?: return
		navigationRepository.navigate(Destinations.seriesTimerDetails(id, timer))
	}
}
