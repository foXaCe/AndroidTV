package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.JellyseerrRowType
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.NavbarPosition
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.jellyseerr.compose.JellyseerrDiscoverRows
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.toolbar.LeftSidebarNavigation
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class DiscoverFragment : Fragment() {
	private val discoverViewModel: JellyseerrDiscoverViewModel by viewModel()
	private val detailsViewModel: JellyseerrDetailsViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()
	private val userPreferences: UserPreferences by inject()
	private val jellyseerrPreferences: JellyseerrPreferences by inject(named("global"))

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			DiscoverScreen(
				discoverViewModel = discoverViewModel,
				detailsViewModel = detailsViewModel,
				navbarPosition = userPreferences[UserPreferences.navbarPosition],
				activeRows = jellyseerrPreferences.activeRows,
				onItemClick = { item -> onContentSelected(item) },
				onItemFocused = { item -> onItemFocused(item) },
				onGenreClick = { genre, mediaType ->
					navigationRepository.navigate(
						Destinations.jellyseerrBrowseByGenre(genre.id, genre.name, mediaType),
					)
				},
				onStudioClick = { studio ->
					navigationRepository.navigate(
						Destinations.jellyseerrBrowseByStudio(studio.id, studio.name),
					)
				},
				onNetworkClick = { network ->
					navigationRepository.navigate(
						Destinations.jellyseerrBrowseByNetwork(network.id, network.name),
					)
				},
			)
		}
	}

	override fun onResume() {
		super.onResume()
		if (!discoverViewModel.hasContent()) {
			discoverViewModel.loadTrendingContent()
			discoverViewModel.loadGenres()
			detailsViewModel.loadRequests()
		}
	}

	private fun onContentSelected(item: JellyseerrDiscoverItemDto) {
		val itemJson = Json.encodeToString(JellyseerrDiscoverItemDto.serializer(), item)
		navigationRepository.navigate(Destinations.jellyseerrMediaDetails(itemJson))
	}

	private fun onItemFocused(item: JellyseerrDiscoverItemDto?) {
		if (item == null) return
		val imageUrl =
			item.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
				?: item.posterPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
		if (imageUrl != null) {
			backgroundService.setBackgroundUrl(imageUrl, BlurContext.BROWSING)
		} else {
			backgroundService.clearBackgrounds()
		}
	}
}

@Composable
private fun DiscoverScreen(
	discoverViewModel: JellyseerrDiscoverViewModel,
	detailsViewModel: JellyseerrDetailsViewModel,
	navbarPosition: NavbarPosition,
	activeRows: List<JellyseerrRowType>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	onItemFocused: (JellyseerrDiscoverItemDto?) -> Unit,
	onGenreClick: (org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto, String) -> Unit,
	onStudioClick: (org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrStudioDto) -> Unit,
	onNetworkClick: (org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrNetworkDto) -> Unit,
) {
	var focusedItem by remember { mutableStateOf<JellyseerrDiscoverItemDto?>(null) }

	// Error handling
	val loadingState by discoverViewModel.loadingState.collectAsState()
	LaunchedEffect(loadingState) {
		if (loadingState is JellyseerrLoadingState.Error) {
			// Handled by Toast in onResume flow
		}
	}

	// Auto-load on availability
	val isAvailable by discoverViewModel.isAvailable.collectAsState()
	LaunchedEffect(isAvailable) {
		if (isAvailable && !discoverViewModel.hasContent()) {
			discoverViewModel.loadTrendingContent()
			discoverViewModel.loadGenres()
			detailsViewModel.loadRequests()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		Column(modifier = Modifier.fillMaxSize().background(VegafoXColors.BackgroundDeep)) {
			// Info header — shows focused item details
			Box(
				modifier =
					Modifier
						.fillMaxWidth()
						.height(180.dp)
						.padding(start = if (navbarPosition == NavbarPosition.LEFT) 80.dp else 0.dp)
						.padding(horizontal = 24.dp),
				contentAlignment = Alignment.BottomStart,
			) {
				Column(modifier = Modifier.padding(bottom = 8.dp)) {
					val item = focusedItem
					if (item != null) {
						// Title
						Text(
							text = item.title ?: item.name ?: "",
							fontFamily = BebasNeue,
							fontSize = 32.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.TextPrimary,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)

						// Metadata row
						val parts = mutableListOf<String>()
						(item.releaseDate?.take(4) ?: item.firstAirDate?.take(4))?.let { parts.add(it) }
						item.voteAverage?.let { if (it > 0) parts.add("\u2605 %.1f".format(it)) }
						when (item.mediaType) {
							"movie" -> parts.add("Film")
							"tv" -> parts.add("S\u00E9rie TV")
						}
						if (parts.isNotEmpty()) {
							Text(
								text = parts.joinToString("  \u2022  "),
								fontSize = 14.sp,
								color = VegafoXColors.TextSecondary,
								maxLines = 1,
							)
						}

						// Synopsis
						item.overview?.let { overview ->
							Spacer(modifier = Modifier.height(4.dp))
							Text(
								text = overview,
								fontSize = 13.sp,
								color = VegafoXColors.TextHint,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis,
							)
						}
					}
				}
			}

			// Rows
			Box(modifier = Modifier.weight(1f)) {
				JellyseerrDiscoverRows(
					discoverViewModel = discoverViewModel,
					detailsViewModel = detailsViewModel,
					activeRows = activeRows,
					onItemClick = onItemClick,
					onItemFocused = { item ->
						focusedItem = item
						onItemFocused(item)
					},
					onGenreClick = onGenreClick,
					onStudioClick = onStudioClick,
					onNetworkClick = onNetworkClick,
				)
			}
		}

		// Toolbar overlay
		when (navbarPosition) {
			NavbarPosition.TOP -> MainToolbar(activeButton = MainToolbarActiveButton.Jellyseerr)
			NavbarPosition.LEFT -> LeftSidebarNavigation(activeButton = MainToolbarActiveButton.Jellyseerr)
		}
	}
}
