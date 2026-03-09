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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.browsing.BrowsingUtils
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.albumPrimaryImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.androidtv.util.sdk.compat.copyWithDisplayPreferencesId
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType as JellyfinImageType
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MusicBrowseFragment : Fragment() {

	private val viewModel: MusicBrowseViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()
	private val itemLauncher: ItemLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		val mainContainer = FrameLayout(requireContext()).apply {
			layoutParams = ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
			)
		}

		val contentView = ComposeView(requireContext()).apply {
			layoutParams = FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT,
			)
			setContent { JellyfinTheme { MusicBrowseContent() } }
		}
		mainContainer.addView(contentView)

		return mainContainer
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val folderJson = arguments?.getString(Extras.Folder) ?: return
		val serverId = Utils.uuidOrNull(arguments?.getString("ServerId"))
		val userId = Utils.uuidOrNull(arguments?.getString("UserId"))
		viewModel.initialize(folderJson, serverId, userId)
	}

	// ──────────────────────────────────────────────
	// Composable content
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicBrowseContent() {
		val uiState by viewModel.uiState.collectAsState()

		val folderJson = arguments?.getString(Extras.Folder)
		val folder = remember(folderJson) {
			folderJson?.let {
				kotlinx.serialization.json.Json.decodeFromString(BaseItemDto.serializer(), it)
			}
		}

		Box(modifier = Modifier.fillMaxSize()) {
			// Activity background
			AppBackground()

			// Dark overlay
			val currentBg by backgroundService.currentBackground.collectAsState()
			val overlayAlpha = if (currentBg != null) 0.45f else 0.75f
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(JellyfinTheme.colorScheme.surfaceDim.copy(alpha = overlayAlpha)),
			)

			Column(modifier = Modifier.fillMaxSize()) {
				// ── Header ──
				MusicHeader(uiState = uiState, folder = folder)

				// ── Content ──
				val displayState = when {
					uiState.isLoading -> DisplayState.LOADING
					uiState.error != null -> DisplayState.ERROR
					else -> DisplayState.CONTENT
				}
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
							title = stringResource(R.string.state_empty_music),
							message = stringResource(R.string.state_empty_music_message),
						)
					},
					errorContent = {
						ErrorState(
							message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
							onRetry = { viewModel.retry() },
						)
					},
					content = {
						MusicRows(
							uiState = uiState,
							folder = folder,
							modifier = Modifier.fillMaxSize(),
						)
					},
				)

				// ── Status bar ──
				LibraryStatusBar(
					statusText = uiState.libraryName,
					counterText = "",
				)
			}
		}
	}

	// ──────────────────────────────────────────────
	// Header: library name centered, focused item HUD, toolbar
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicHeader(
		uiState: MusicBrowseUiState,
		folder: BaseItemDto?,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 60.dp, end = 60.dp, top = 12.dp, bottom = 4.dp),
		) {
			// Row 0: Centered library name
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = uiState.libraryName,
					style = JellyfinTheme.typography.headlineMedium,
					fontWeight = FontWeight.Light,
					color = JellyfinTheme.colorScheme.onSurface,
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			// Row 1: Focused item HUD
			FocusedItemHud(
				item = uiState.focusedItem,
				modifier = Modifier.fillMaxWidth(),
			)

			Spacer(modifier = Modifier.height(6.dp))

			// Row 2: Toolbar — Home button
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
			) {
				LibraryToolbarButton(
					iconRes = R.drawable.ic_house,
					contentDescription = stringResource(R.string.home),
					onClick = { navigationRepository.navigate(Destinations.home) },
				)
			}
		}
	}

	// ──────────────────────────────────────────────
	// Scrollable rows content
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicRows(
		uiState: MusicBrowseUiState,
		folder: BaseItemDto?,
		modifier: Modifier = Modifier,
	) {
		val scrollState = rememberScrollState()

		Column(
			modifier = modifier
				.fillMaxWidth()
				.verticalScroll(scrollState)
				.padding(bottom = 16.dp),
		) {
			// Navigation buttons row (at the top)
			if (folder != null) {
				MusicViewsRow(folder = folder)
			}

			// Latest Audio
			if (uiState.latestAudio.isNotEmpty()) {
				MusicItemRow(
					title = stringResource(R.string.lbl_latest),
					items = uiState.latestAudio,
				)
			}

			// Last Played
			if (uiState.lastPlayed.isNotEmpty()) {
				MusicItemRow(
					title = stringResource(R.string.lbl_last_played).trim(),
					items = uiState.lastPlayed,
				)
			}

			// Favorite Albums
			if (uiState.favoriteAlbums.isNotEmpty()) {
				MusicItemRow(
					title = stringResource(R.string.lbl_favorites),
					items = uiState.favoriteAlbums,
				)
			}

			// Playlists
			if (uiState.playlists.isNotEmpty()) {
				MusicItemRow(
					title = stringResource(R.string.lbl_playlists),
					items = uiState.playlists,
				)
			}
		}
	}

	// ──────────────────────────────────────────────
	// A single horizontal row of music items
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicItemRow(
		title: String,
		items: List<BaseItemDto>,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 12.dp),
		) {
			// Row title
			Text(
				text = title,
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = 60.dp, bottom = 8.dp),
			)

			// Horizontal list of square cards
			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 60.dp),
			) {
				items(items, key = { it.id }) { item ->
					MusicSquareCard(
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

	// ──────────────────────────────────────────────
	// Square card for music items (albums, audio, playlists)
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicSquareCard(
		item: BaseItemDto,
		onClick: () -> Unit,
		onFocused: () -> Unit,
		cardSize: Int = 140,
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
				.width(cardSize.dp)
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
			// Square image
			Box(
				modifier = Modifier
					.size(cardSize.dp)
					.clip(JellyfinTheme.shapes.extraSmall)
					.then(
						if (isFocused) Modifier.background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.08f))
						else Modifier
					)
					.background(JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
			) {
				val imageUrl = getMusicImageUrl(item)
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
					// Placeholder icon for items without artwork
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center,
					) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.ic_album),
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							tint = JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.2f),
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(5.dp))

			// Title
			Text(
				text = item.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = JellyfinTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			// Subtitle: artist name for albums/audio, playlist item count, etc.
			val subtitle = getMusicSubtitle(item)
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

	// ──────────────────────────────────────────────
	// Navigation buttons: Albums, Album Artists, Artists, Genres, Random
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicViewsRow(folder: BaseItemDto) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 4.dp),
		) {
			Text(
				text = stringResource(R.string.lbl_views),
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = 60.dp, bottom = 8.dp),
			)

			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 60.dp),
			) {
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_albums),
						iconRes = R.drawable.ic_album,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AL")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, BaseItemKind.MUSIC_ALBUM.serialName)
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_album_artists),
						iconRes = R.drawable.ic_artist,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AR")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, "AlbumArtist")
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_artists),
						iconRes = R.drawable.ic_artist,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AR")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, "Artist")
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_genres),
						iconRes = R.drawable.ic_masks,
						onClick = {
							navigationRepository.navigate(
								Destinations.libraryByGenres(folder, BaseItemKind.MUSIC_ALBUM.serialName)
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.random),
						iconRes = R.drawable.ic_shuffle,
						onClick = {
							BrowsingUtils.getRandomItem(
								viewModel.effectiveApi,
								viewLifecycleOwner,
								folder,
								BaseItemKind.MUSIC_ALBUM,
							) { randomItem ->
								if (randomItem != null) {
									navigationRepository.navigate(
										Destinations.itemList(randomItem.id)
									)
								}
							}
						},
					)
				}
			}
		}
	}

	// ──────────────────────────────────────────────
	// Navigation button card
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicNavButton(
		label: String,
		iconRes: Int,
		onClick: () -> Unit,
	) {
		val interactionSource = remember { MutableInteractionSource() }
		val isFocused by interactionSource.collectIsFocusedAsState()

		val bgColor = when {
			isFocused -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.20f)
			else -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.08f)
		}

		val borderColor = when {
			isFocused -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.4f)
			else -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f)
		}

		Column(
			modifier = Modifier
				.width(140.dp)
				.clip(JellyfinTheme.shapes.small)
				.background(bgColor)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				)
				.padding(vertical = 20.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Icon(
				imageVector = ImageVector.vectorResource(iconRes),
				contentDescription = label,
				modifier = Modifier.size(32.dp),
				tint = if (isFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textSecondary,
			)

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = label,
				style = JellyfinTheme.typography.bodyMedium,
				fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal,
				color = if (isFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}

	// ──────────────────────────────────────────────
	// Helpers
	// ──────────────────────────────────────────────

	/**
	 * Get the best image URL for a music item (album art, audio album art, or primary).
	 */
	private fun getMusicImageUrl(item: BaseItemDto): String? {
		// For audio items, prefer the album's primary image
		if (item.type == BaseItemKind.AUDIO) {
			val albumImage = item.albumPrimaryImage
			if (albumImage != null) return albumImage.getUrl(viewModel.effectiveApi, maxHeight = 300)
		}

		// Standard primary image
		val primary = item.itemImages[JellyfinImageType.PRIMARY]
		if (primary != null) return primary.getUrl(viewModel.effectiveApi, maxHeight = 300)

		// Parent primary image fallback
		val parentPrimary = item.parentImages[JellyfinImageType.PRIMARY]
		if (parentPrimary != null) return parentPrimary.getUrl(viewModel.effectiveApi, maxHeight = 300)

		return null
	}

	/**
	 * Build a subtitle string for music items.
	 */
	private fun getMusicSubtitle(item: BaseItemDto): String {
		return when (item.type) {
			BaseItemKind.AUDIO,
			BaseItemKind.MUSIC_ALBUM -> {
				item.artists?.joinToString(", ")
					?: item.albumArtists?.joinToString(", ") { it.name ?: "" }
					?: item.albumArtist
					?: ""
			}
			BaseItemKind.PLAYLIST -> {
				val count = item.childCount ?: 0
				if (count > 0) resources.getQuantityString(R.plurals.items, count, count) else ""
			}
			BaseItemKind.MUSIC_ARTIST -> {
				val count = item.albumCount ?: 0
				if (count > 0) resources.getQuantityString(R.plurals.albums, count, count) else ""
			}
			else -> ""
		}
	}

	private fun launchItem(item: BaseItemDto) {
		val rowItem = BaseItemDtoBaseRowItem(item)
		itemLauncher.launch(rowItem, requireContext())
	}
}
