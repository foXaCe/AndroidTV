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
import androidx.core.os.bundleOf
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
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.browsing.BrowsingUtils
import org.jellyfin.androidtv.ui.browsing.compose.MediaPosterCard
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.components.BrowseHeader
import org.jellyfin.androidtv.util.apiclient.getCardImageUrl
import org.jellyfin.androidtv.util.sdk.compat.copyWithDisplayPreferencesId
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class MusicBrowseFragment : Fragment() {
	data class Args(
		val folderJson: String,
		val serverId: UUID? = null,
		val userId: UUID? = null,
	) {
		fun toBundle() =
			bundleOf(
				KEY_FOLDER to folderJson,
				KEY_SERVER_ID to serverId?.toString(),
				KEY_USER_ID to userId?.toString(),
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val folderJson = bundle?.getString(KEY_FOLDER) ?: return null
				return Args(
					folderJson = folderJson,
					serverId = bundle.getString(KEY_SERVER_ID)?.let(UUID::fromString),
					userId = bundle.getString(KEY_USER_ID)?.let(UUID::fromString),
				)
			}
		}
	}

	companion object {
		internal const val KEY_FOLDER = "folder"
		internal const val KEY_SERVER_ID = "ServerId"
		internal const val KEY_USER_ID = "UserId"
	}

	private val viewModel: MusicBrowseViewModel by viewModel()
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
				setContent { JellyfinTheme { ScreenIdOverlay(ScreenIds.MUSIC_BROWSE_ID, ScreenIds.MUSIC_BROWSE_NAME) { MusicBrowseContent() } } }
			}
		mainContainer.addView(contentView)

		return mainContainer
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)

		val args = Args.fromBundle(arguments) ?: return
		viewModel.initialize(args.folderJson, args.serverId, args.userId)
	}

	// ──────────────────────────────────────────────
	// Composable content
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicBrowseContent() {
		val uiState by viewModel.uiState.collectAsState()

		val folderJson = Args.fromBundle(arguments)?.folderJson
		val folder =
			remember(folderJson) {
				folderJson?.let {
					kotlinx.serialization.json.Json
						.decodeFromString(BaseItemDto.serializer(), it)
				}
			}

		Box(modifier = Modifier.fillMaxSize()) {
			// Activity background
			AppBackground()

			// Dark overlay
			val currentBg by backgroundService.currentBackground.collectAsState()
			val overlayAlpha = if (currentBg != null) 0.45f else 0.75f
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.background(JellyfinTheme.colorScheme.surfaceDim.copy(alpha = overlayAlpha)),
			)

			Column(modifier = Modifier.fillMaxSize()) {
				// ── Header ──
				BrowseHeader(title = uiState.libraryName) {
					LibraryToolbarButton(
						icon = VegafoXIcons.Home,
						contentDescription = stringResource(R.string.home),
						onClick = { navigationRepository.navigate(Destinations.home) },
					)
				}

				FocusedItemHud(
					item = uiState.focusedItem,
					modifier = Modifier.fillMaxWidth(),
				)

				// ── Content ──
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
						Column(verticalArrangement = Arrangement.spacedBy(BrowseDimensions.skeletonRowSpacing)) {
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

	// MusicHeader removed — uses BrowseHeader from ui/shared/components

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
			modifier =
				modifier
					.fillMaxWidth()
					.verticalScroll(scrollState)
					.padding(bottom = BrowseDimensions.cardGap),
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
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = BrowseDimensions.rowTopPadding),
		) {
			// Row title
			Text(
				text = title,
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = BrowseDimensions.rowTitleBottomPadding),
			)

			// Horizontal list of square cards
			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
				contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			) {
				items(items, key = { it.id }) { item ->
					MediaPosterCard(
						imageUrl = item.getCardImageUrl(viewModel.effectiveApi),
						title = item.name ?: "",
						cardWidth = CardDimensions.squareSize,
						cardHeight = CardDimensions.squareSize,
						onClick = { launchItem(item) },
						onFocused = {
							viewModel.setFocusedItem(item)
							backgroundService.setBackground(item, BlurContext.BROWSING)
						},
						subtitle = getMusicSubtitle(item),
						placeholderIcon = VegafoXIcons.Album,
					)
				}
			}
		}
	}

	// MusicSquareCard removed — uses MediaPosterCard from ui/browsing/compose

	// ──────────────────────────────────────────────
	// Navigation buttons: Albums, Album Artists, Artists, Genres, Random
	// ──────────────────────────────────────────────

	@Composable
	private fun MusicViewsRow(folder: BaseItemDto) {
		Column(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = 4.dp),
		) {
			Text(
				text = stringResource(R.string.lbl_views),
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = BrowseDimensions.rowTitleBottomPadding),
			)

			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
				contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			) {
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_albums),
						icon = VegafoXIcons.Album,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AL")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, BaseItemKind.MUSIC_ALBUM.serialName),
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_album_artists),
						icon = VegafoXIcons.Artist,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AR")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, "AlbumArtist"),
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_artists),
						icon = VegafoXIcons.Artist,
						onClick = {
							val navFolder = folder.copyWithDisplayPreferencesId(folder.id.toString() + "AR")
							navigationRepository.navigate(
								Destinations.libraryBrowser(navFolder, "Artist"),
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.lbl_genres),
						icon = VegafoXIcons.Genres,
						onClick = {
							navigationRepository.navigate(
								Destinations.libraryByGenres(folder, BaseItemKind.MUSIC_ALBUM.serialName),
							)
						},
					)
				}
				item {
					MusicNavButton(
						label = stringResource(R.string.random),
						icon = VegafoXIcons.Shuffle,
						onClick = {
							BrowsingUtils.getRandomItem(
								viewModel.effectiveApi,
								viewLifecycleOwner,
								folder,
								BaseItemKind.MUSIC_ALBUM,
							) { randomItem ->
								if (randomItem != null) {
									navigationRepository.navigate(
										Destinations.itemList(randomItem.id),
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
		icon: ImageVector,
		onClick: () -> Unit,
	) {
		val interactionSource = remember { MutableInteractionSource() }
		val isFocused by interactionSource.collectIsFocusedAsState()

		val bgColor =
			when {
				isFocused -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.20f)
				else -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.08f)
			}

		val borderColor =
			when {
				isFocused -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.4f)
				else -> JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
	 * Build a subtitle string for music items.
	 */
	private fun getMusicSubtitle(item: BaseItemDto): String =
		when (item.type) {
			BaseItemKind.AUDIO,
			BaseItemKind.MUSIC_ALBUM,
			-> {
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

	private fun launchItem(item: BaseItemDto) {
		val rowItem = BaseItemDtoBaseRowItem(item)
		itemLauncher.launch(rowItem, requireContext())
	}
}
