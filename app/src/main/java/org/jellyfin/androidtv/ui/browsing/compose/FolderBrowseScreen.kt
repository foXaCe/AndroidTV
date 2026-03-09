package org.jellyfin.androidtv.ui.browsing.compose

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.browsing.v2.FolderBrowseRow
import org.jellyfin.androidtv.ui.browsing.v2.FolderBrowseViewModel
import org.jellyfin.androidtv.ui.browsing.v2.FocusedItemHud
import org.jellyfin.androidtv.ui.browsing.v2.LibraryStatusBar
import org.jellyfin.androidtv.ui.browsing.v2.LibraryToolbarButton
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
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

	val displayState = when {
		uiState.isLoading -> DisplayState.LOADING
		uiState.error != null -> DisplayState.ERROR
		uiState.rows.isEmpty() -> DisplayState.EMPTY
		else -> DisplayState.CONTENT
	}

	Box(modifier = Modifier.fillMaxSize()) {
		AppBackground()

		val currentBg by backgroundService.currentBackground.collectAsState()
		val overlayAlpha = if (currentBg != null) 0.45f else 0.75f
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(JellyfinTheme.colorScheme.surfaceDim.copy(alpha = overlayAlpha)),
		)

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
					Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
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

@Composable
private fun FolderHeader(
	folderName: String,
	focusedItem: BaseItemDto?,
	onHomeClick: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = 60.dp, end = 60.dp, top = 12.dp, bottom = 4.dp),
	) {
		Box(
			modifier = Modifier.fillMaxWidth(),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = folderName,
				style = JellyfinTheme.typography.headlineMedium,
				fontWeight = FontWeight.Light,
				color = JellyfinTheme.colorScheme.onSurface,
			)
		}

		Spacer(modifier = Modifier.height(6.dp))

		FocusedItemHud(
			item = focusedItem,
			modifier = Modifier.fillMaxWidth(),
		)

		Spacer(modifier = Modifier.height(6.dp))

		LibraryToolbarButton(
			iconRes = R.drawable.ic_house,
			contentDescription = stringResource(R.string.home),
			onClick = onHomeClick,
		)
	}
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
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState)
			.padding(bottom = 16.dp),
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
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 12.dp),
	) {
		Text(
			text = title,
			style = JellyfinTheme.typography.titleLarge,
			color = JellyfinTheme.colorScheme.onSurface,
			modifier = Modifier.padding(start = 60.dp, bottom = 8.dp),
		)

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			contentPadding = PaddingValues(horizontal = 60.dp),
		) {
			items(items, key = { it.id }) { item ->
				FolderItemCard(
					item = item,
					api = api,
					onClick = { onItemClick(item) },
					onFocused = { onItemFocus(item) },
				)
			}
		}
	}
}

@Composable
private fun FolderItemCard(
	item: BaseItemDto,
	api: ApiClient,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	cardWidth: Int = 140,
	cardHeight: Int = 210,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	LaunchedEffect(isFocused) {
		if (isFocused) onFocused()
	}

	val scale = if (isFocused) 1.08f else 1.0f
	val alpha = if (isFocused) 1.0f else 0.75f

	Column(
		modifier = Modifier
			.width(cardWidth.dp)
			.graphicsLayer {
				scaleX = scale
				scaleY = scale
				this.alpha = alpha
			}
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				onClick = onClick,
			),
		horizontalAlignment = Alignment.Start,
	) {
		Box(
			modifier = Modifier
				.size(cardWidth.dp, cardHeight.dp)
				.clip(JellyfinTheme.shapes.extraSmall)
				.then(
					if (isFocused) Modifier.background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.08f))
					else Modifier
				)
				.background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
		) {
			val imageUrl = item.itemImages[JellyfinImageType.PRIMARY]?.getUrl(api, maxHeight = 400)
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
	}
}
