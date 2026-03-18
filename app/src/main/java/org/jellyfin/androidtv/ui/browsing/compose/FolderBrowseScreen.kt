package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.browsing.v2.FocusedItemHud
import org.jellyfin.androidtv.ui.browsing.v2.FolderBrowseRow
import org.jellyfin.androidtv.ui.browsing.v2.FolderBrowseViewModel
import org.jellyfin.androidtv.ui.browsing.v2.LibraryStatusBar
import org.jellyfin.androidtv.ui.browsing.v2.LibraryToolbarButton
import org.jellyfin.androidtv.ui.shared.components.BrowseHeader
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType as JellyfinImageType

@Composable
fun FolderBrowseScreen(
	viewModel: FolderBrowseViewModel,
	backgroundService: BackgroundService,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto) -> Unit,
	onHomeClick: () -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()

	val displayState =
		when {
			uiState.isLoading -> DisplayState.LOADING
			uiState.error != null -> DisplayState.ERROR
			uiState.rows.isEmpty() -> DisplayState.EMPTY
			else -> DisplayState.CONTENT
		}

	VegafoXScaffold {
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(VegafoXColors.BackgroundDeep),
		) {
			Column(modifier = Modifier.fillMaxSize()) {
				// Header
				FolderHeader(
					folderName = uiState.folderName,
					focusedItem = uiState.focusedItem,
					onHomeClick = onHomeClick,
				)

				// Content
				StateContainer(
					state = displayState,
					modifier = Modifier.weight(1f),
					loadingContent = {
						Column(verticalArrangement = Arrangement.spacedBy(BrowseDimensions.skeletonRowSpacing)) {
							SkeletonCardRow()
							SkeletonCardRow()
						}
					},
					emptyContent = {
						EmptyState(
							title = stringResource(R.string.state_empty_library),
							message = stringResource(R.string.state_empty_library_message),
						)
					},
					errorContent = {
						ErrorState(
							message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
							onRetry = { viewModel.retry() },
						)
					},
					content = {
						FolderRows(
							rows = uiState.rows,
							api = viewModel.effectiveApi,
							onItemClick = onItemClick,
							onItemFocus = onItemFocus,
						)
					},
				)

				LibraryStatusBar(
					statusText = uiState.folderName,
					counterText = "",
				)
			}
		}
	}
}

@Composable
private fun FolderHeader(
	folderName: String,
	focusedItem: BaseItemDto?,
	onHomeClick: () -> Unit,
) {
	BrowseHeader(title = folderName) {
		LibraryToolbarButton(
			icon = VegafoXIcons.Home,
			contentDescription = stringResource(R.string.home),
			onClick = onHomeClick,
		)
	}

	FocusedItemHud(
		item = focusedItem,
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
private fun FolderRows(
	rows: List<FolderBrowseRow>,
	api: ApiClient,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto) -> Unit,
) {
	val scrollState = rememberScrollState()

	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
				.padding(bottom = BrowseDimensions.cardGap),
	) {
		for (row in rows) {
			val title = row.titleOverride ?: stringResource(row.titleRes)
			FolderItemRow(
				title = title,
				items = row.items,
				api = api,
				onItemClick = onItemClick,
				onItemFocus = onItemFocus,
			)
		}
	}
}

@Composable
private fun FolderItemRow(
	title: String,
	items: List<BaseItemDto>,
	api: ApiClient,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto) -> Unit,
) {
	Column(
		modifier =
			Modifier
				.fillMaxWidth()
				.padding(top = BrowseDimensions.rowTopPadding),
	) {
		Text(
			text = title,
			style = JellyfinTheme.typography.titleLarge,
			color = JellyfinTheme.colorScheme.onSurface,
			modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = BrowseDimensions.rowTitleBottomPadding),
		)

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
			contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
		) {
			items(items, key = { it.id }) { item ->
				val imageUrl = item.itemImages[JellyfinImageType.PRIMARY]?.getUrl(api, maxHeight = 400)
				MediaPosterCard(
					imageUrl = imageUrl,
					title = item.name ?: "",
					cardWidth = CardDimensions.folderWidth,
					cardHeight = CardDimensions.folderHeight,
					onClick = { onItemClick(item) },
					onFocused = { onItemFocus(item) },
				)
			}
		}
	}
}
