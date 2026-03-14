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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil3.compose.AsyncImage
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
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.jellyfin.sdk.model.api.ImageType as JellyfinImageType

class ScheduleBrowseFragment : Fragment() {
	private val viewModel: ScheduleBrowseViewModel by viewModel()
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
						ScreenIdOverlay(
							ScreenIds.SCHEDULE_BROWSE_ID,
							ScreenIds.SCHEDULE_BROWSE_NAME,
						) { ScheduleBrowseContent() }
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
		viewModel.initialize(requireContext())
	}

	@Composable
	private fun ScheduleBrowseContent() {
		val uiState by viewModel.uiState.collectAsState()

		Box(modifier = Modifier.fillMaxSize()) {
			AppBackground()

			val currentBg by backgroundService.currentBackground.collectAsState()
			val overlayAlpha = if (currentBg != null) 0.45f else 0.75f
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.background(JellyfinTheme.colorScheme.surfaceDim.copy(alpha = overlayAlpha)),
			)

			Column(modifier = Modifier.fillMaxSize()) {
				ScheduleHeader(uiState = uiState)

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
							title = stringResource(R.string.state_empty_schedule),
							message = stringResource(R.string.state_empty_schedule_message),
						)
					},
					errorContent = {
						ErrorState(
							message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
							onRetry = { viewModel.retry() },
						)
					},
					content = {
						ScheduleRows(
							uiState = uiState,
							modifier = Modifier.fillMaxSize(),
						)
					},
				)

				LibraryStatusBar(
					statusText = stringResource(R.string.lbl_schedule),
					counterText = "",
				)
			}
		}
	}

	@Composable
	private fun ScheduleHeader(uiState: ScheduleBrowseUiState) {
		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(
						start = BrowseDimensions.contentPaddingHorizontal,
						end = BrowseDimensions.contentPaddingHorizontal,
						top = 12.dp,
						bottom = 4.dp,
					),
		) {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = stringResource(R.string.lbl_schedule),
					style = JellyfinTheme.typography.headlineMedium,
					fontWeight = FontWeight.Light,
					color = JellyfinTheme.colorScheme.onSurface,
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			FocusedItemHud(
				item = uiState.focusedItem,
				modifier = Modifier.fillMaxWidth(),
			)

			Spacer(modifier = Modifier.height(6.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				LibraryToolbarButton(
					icon = VegafoXIcons.Home,
					contentDescription = stringResource(R.string.home),
					onClick = { navigationRepository.navigate(Destinations.home) },
				)
			}
		}
	}

	@Composable
	private fun ScheduleRows(
		uiState: ScheduleBrowseUiState,
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
			if (uiState.scheduleGroups.isEmpty()) {
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
						color = JellyfinTheme.colorScheme.textHint,
					)
				}
			} else {
				for (group in uiState.scheduleGroups) {
					ScheduleItemRow(
						title = group.dateLabel,
						items = group.items,
					)
				}
			}
		}
	}

	@Composable
	private fun ScheduleItemRow(
		title: String,
		items: List<BaseItemDto>,
	) {
		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = 12.dp),
		) {
			Text(
				text = title,
				style = JellyfinTheme.typography.titleMedium,
				color = JellyfinTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = 8.dp),
			)

			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			) {
				items(items, key = { it.id }) { item ->
					ScheduleCard(
						item = item,
						onClick = { launchItem(item) },
						onFocused = {
							viewModel.setFocusedItem(item)
							backgroundService.setBackground(item, BlurContext.BROWSING)
						},
					)
				}
			}
		}
	}

	@Composable
	private fun ScheduleCard(
		item: BaseItemDto,
		onClick: () -> Unit,
		onFocused: () -> Unit,
		cardWidth: Int = 200,
		cardHeight: Int = 112,
	) {
		val interactionSource = remember { MutableInteractionSource() }
		val isFocused by interactionSource.collectIsFocusedAsState()

		LaunchedEffect(isFocused) {
			if (isFocused) onFocused()
		}

		val scale = if (isFocused) 1.08f else 1.0f
		val alpha = if (isFocused) 1.0f else 0.75f

		Column(
			modifier =
				Modifier
					.width(cardWidth.dp)
					.graphicsLayer {
						scaleX = scale
						scaleY = scale
						this.alpha = alpha
					}.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = onClick,
					),
			horizontalAlignment = Alignment.Start,
		) {
			Box(
				modifier =
					Modifier
						.width(cardWidth.dp)
						.height(cardHeight.dp)
						.clip(JellyfinTheme.shapes.extraSmall)
						.then(
							if (isFocused) {
								Modifier.background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.08f))
							} else {
								Modifier
							},
						).background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
			) {
				val imageUrl = getScheduleImageUrl(item)
				if (imageUrl != null) {
					val placeholder = rememberGradientPlaceholder()
					val errorFallback = rememberErrorPlaceholder()
					AsyncImage(
						model = imageUrl,
						contentDescription = item.name,
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop,
						placeholder = placeholder,
						error = errorFallback,
					)
				} else {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center,
					) {
						Icon(
							imageVector = VegafoXIcons.TvTimer,
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							tint = JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.2f),
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(5.dp))

			Text(
				text = item.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = JellyfinTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			val subtitle = getScheduleSubtitle(item)
			if (subtitle.isNotEmpty()) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.labelSmall,
					fontWeight = FontWeight.Normal,
					color = JellyfinTheme.colorScheme.textHint,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}

	private fun getScheduleImageUrl(item: BaseItemDto): String? {
		val thumb = item.itemImages[JellyfinImageType.THUMB]
		if (thumb != null) return thumb.getUrl(viewModel.api, maxHeight = 300)

		val primary = item.itemImages[JellyfinImageType.PRIMARY]
		if (primary != null) return primary.getUrl(viewModel.api, maxHeight = 300)

		val parentThumb = item.parentImages[JellyfinImageType.THUMB]
		if (parentThumb != null) return parentThumb.getUrl(viewModel.api, maxHeight = 300)

		val parentPrimary = item.parentImages[JellyfinImageType.PRIMARY]
		if (parentPrimary != null) return parentPrimary.getUrl(viewModel.api, maxHeight = 300)

		return null
	}

	private fun getScheduleSubtitle(item: BaseItemDto): String {
		val parts = mutableListOf<String>()
		item.channelName?.let { if (it.isNotBlank()) parts.add(it) }
		item.episodeTitle?.let { if (it.isNotBlank()) parts.add(it) }
		return parts.joinToString(" • ")
	}

	private fun launchItem(item: BaseItemDto) {
		val rowItem = BaseItemDtoBaseRowItem(item)
		itemLauncher.launch(rowItem, requireContext())
	}
}
