package org.jellyfin.androidtv.ui.browsing.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.Icon
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
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.components.BrowseHeader
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordingsBrowseFragment : Fragment() {
	private val viewModel: RecordingsBrowseViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()
	private val itemLauncher: ItemLauncher by inject()

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
						ScreenIdOverlay(ScreenIds.RECORDINGS_BROWSE_ID, ScreenIds.RECORDINGS_BROWSE_NAME) {
							VegafoXScaffold {
								RecordingsBrowseContent()
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
	private fun RecordingsBrowseContent() {
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
					title = stringResource(R.string.lbl_recorded_tv),
				) {
					LibraryToolbarButton(
						icon = VegafoXIcons.Home,
						contentDescription = stringResource(R.string.home),
						onClick = { navigationRepository.navigate(Destinations.home) },
					)
				}

				Spacer(modifier = Modifier.height(6.dp))

				FocusedItemHud(
					item = uiState.focusedItem,
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
						RecordingsRows(
							uiState = uiState,
							modifier = Modifier.fillMaxSize(),
						)
					},
				)

				LibraryStatusBar(
					statusText = stringResource(R.string.lbl_recorded_tv),
					counterText = "",
				)
			}
		}
	}

	@Composable
	private fun RecordingsRows(
		uiState: RecordingsBrowseUiState,
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
			RecordingsViewsRow()

			if (uiState.scheduledNext24h.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.scheduled_in_next_24_hours),
					items = uiState.scheduledNext24h,
				)
			}

			if (uiState.recentRecordings.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.lbl_recent_recordings),
					items = uiState.recentRecordings,
				)
			}

			if (uiState.seriesRecordings.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.lbl_tv_series),
					items = uiState.seriesRecordings,
				)
			}

			if (uiState.movieRecordings.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.lbl_movies),
					items = uiState.movieRecordings,
				)
			}

			if (uiState.sportsRecordings.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.lbl_sports),
					items = uiState.sportsRecordings,
				)
			}

			if (uiState.kidsRecordings.isNotEmpty()) {
				RecordingItemRow(
					title = stringResource(R.string.lbl_kids),
					items = uiState.kidsRecordings,
				)
			}
		}
	}

	@Composable
	private fun RecordingItemRow(
		title: String,
		items: List<BaseItemDto>,
	) {
		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = BrowseDimensions.rowTopPadding),
		) {
			Text(
				text = title,
				style = JellyfinTheme.typography.titleMedium,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = BrowseDimensions.rowTitleBottomPadding),
			)

			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
				contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			) {
				items(items, key = { it.id }) { item ->
					BrowseMediaCard(
						item = item,
						api = viewModel.api,
						onClick = { launchItem(item) },
						onFocus = {
							viewModel.setFocusedItem(item)
							backgroundService.setBackground(item, BlurContext.BROWSING)
						},
					)
				}
			}
		}
	}

	@Composable
	private fun RecordingsViewsRow() {
		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = 4.dp),
		) {
			Text(
				text = stringResource(R.string.lbl_views),
				style = JellyfinTheme.typography.titleMedium,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = 8.dp),
			)

			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			) {
				item {
					RecordingsNavButton(
						label = stringResource(R.string.lbl_schedule),
						icon = VegafoXIcons.TvTimer,
						onClick = {
							navigationRepository.navigate(Destinations.liveTvSchedule)
						},
					)
				}
				item {
					RecordingsNavButton(
						label = stringResource(R.string.lbl_series_recordings),
						icon = VegafoXIcons.RecordSeries,
						onClick = {
							navigationRepository.navigate(Destinations.liveTvSeriesRecordings)
						},
					)
				}
			}
		}
	}

	@Composable
	private fun RecordingsNavButton(
		label: String,
		icon: ImageVector,
		onClick: () -> Unit,
	) {
		val interactionSource = remember { MutableInteractionSource() }
		val isFocused by interactionSource.collectIsFocusedAsState()

		val bgColor =
			when {
				isFocused -> VegafoXColors.SurfaceBright
				else -> VegafoXColors.Surface
			}

		Column(
			modifier =
				Modifier
					.width(140.dp)
					.clip(JellyfinTheme.shapes.small)
					.background(bgColor)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = onClick,
					).padding(vertical = 20.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(32.dp),
				tint = if (isFocused) VegafoXColors.TextPrimary else VegafoXColors.TextSecondary,
			)

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = label,
				style = JellyfinTheme.typography.bodyMedium,
				fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
				color = if (isFocused) VegafoXColors.TextPrimary else VegafoXColors.TextSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}

	private fun launchItem(item: BaseItemDto) {
		val rowItem = BaseItemDtoBaseRowItem(item)
		itemLauncher.launch(rowItem, requireContext())
	}
}
