package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.jellyseerr.compose.JellyseerrBrowseByScreen
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Filter type for Jellyseerr browse-by functionality
 */
enum class BrowseFilterType {
	GENRE,
	NETWORK,
	STUDIO,
	KEYWORD,
}

/**
 * Sort options for TMDB/Jellyseerr discover API
 */
data class JellyseerrSortOption(
	val name: String,
	val value: String,
)

/**
 * Fragment for browsing Jellyseerr content filtered by genre, network, or studio.
 * Renders a Compose grid with sort/filter toolbar — zero Leanback dependencies.
 */
class JellyseerrBrowseByFragment : Fragment() {
	private val viewModel: JellyseerrDiscoverViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()

	private var filterId: Int = 0
	private var filterName: String = ""
	private var mediaType: String = "movie"
	private var filterType: BrowseFilterType = BrowseFilterType.GENRE

	// TMDB sort options (lazy because getString requires fragment to be attached)
	val sortOptions: List<JellyseerrSortOption> by lazy {
		listOf(
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_popularity), "popularity.desc"),
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_rating), "vote_average.desc"),
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_release_date), "primary_release_date.desc"),
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_title), "original_title.asc"),
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_revenue), "revenue.desc"),
		)
	}

	data class Args(
		val filterId: Int,
		val filterName: String,
		val mediaType: String = "movie",
		val filterType: BrowseFilterType = BrowseFilterType.GENRE,
	) {
		fun toBundle() =
			bundleOf(
				ARG_FILTER_ID to filterId,
				ARG_FILTER_NAME to filterName,
				ARG_MEDIA_TYPE to mediaType,
				ARG_FILTER_TYPE to filterType.name,
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				if (bundle == null) return null
				return Args(
					filterId = bundle.getInt(ARG_FILTER_ID),
					filterName = bundle.getString(ARG_FILTER_NAME) ?: return null,
					mediaType = bundle.getString(ARG_MEDIA_TYPE) ?: "movie",
					filterType =
						try {
							BrowseFilterType.valueOf(bundle.getString(ARG_FILTER_TYPE) ?: "GENRE")
						} catch (_: Exception) {
							BrowseFilterType.GENRE
						},
				)
			}
		}
	}

	companion object {
		internal const val ARG_FILTER_ID = "filter_id"
		internal const val ARG_FILTER_NAME = "filter_name"
		internal const val ARG_MEDIA_TYPE = "media_type"
		internal const val ARG_FILTER_TYPE = "filter_type"

		fun newInstance(
			filterId: Int,
			filterName: String,
			mediaType: String,
			filterType: BrowseFilterType = BrowseFilterType.GENRE,
		): JellyseerrBrowseByFragment =
			JellyseerrBrowseByFragment().apply {
				arguments = Args(filterId, filterName, mediaType, filterType).toBundle()
			}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val args = Args.fromBundle(arguments)
		if (args != null) {
			filterId = args.filterId
			filterName = args.filterName
			mediaType = args.mediaType
			filterType = args.filterType
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setContent {
				ScreenIdOverlay(ScreenIds.JELLYSEERR_BROWSE_BY_ID, ScreenIds.JELLYSEERR_BROWSE_BY_NAME) {
					JellyseerrBrowseByScreen(
						viewModel = viewModel,
						filterId = filterId,
						filterName = filterName,
						mediaType = mediaType,
						filterType = filterType,
						sortOptions = sortOptions,
						onItemClick = { item -> onItemClicked(item) },
						onItemFocused = { item -> onItemSelected(item) },
					)
				}
			}
		}

	private fun onItemSelected(item: JellyseerrDiscoverItemDto) {
		item.backdropPath?.let { backdropPath ->
			val backdropUrl = "https://image.tmdb.org/t/p/w1280$backdropPath"
			backgroundService.setBackgroundUrl(backdropUrl, BlurContext.BROWSING)
		}
	}

	private fun onItemClicked(item: JellyseerrDiscoverItemDto) {
		val itemJson = Json.encodeToString(JellyseerrDiscoverItemDto.serializer(), item)
		navigationRepository.navigate(Destinations.jellyseerrMediaDetails(itemJson))
	}
}
