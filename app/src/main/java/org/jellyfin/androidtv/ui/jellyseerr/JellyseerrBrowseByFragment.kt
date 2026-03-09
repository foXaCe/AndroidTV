package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
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
	KEYWORD
}

/**
 * Sort options for TMDB/Jellyseerr discover API
 */
data class JellyseerrSortOption(
	val name: String,
	val value: String
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
			JellyseerrSortOption(getString(R.string.jellyseerr_sort_revenue), "revenue.desc")
		)
	}

	companion object {
		private const val ARG_FILTER_ID = "filter_id"
		private const val ARG_FILTER_NAME = "filter_name"
		private const val ARG_MEDIA_TYPE = "media_type"
		private const val ARG_FILTER_TYPE = "filter_type"

		fun newInstance(
			filterId: Int,
			filterName: String,
			mediaType: String,
			filterType: BrowseFilterType = BrowseFilterType.GENRE
		): JellyseerrBrowseByFragment {
			return JellyseerrBrowseByFragment().apply {
				arguments = Bundle().apply {
					putInt(ARG_FILTER_ID, filterId)
					putString(ARG_FILTER_NAME, filterName)
					putString(ARG_MEDIA_TYPE, mediaType)
					putString(ARG_FILTER_TYPE, filterType.name)
				}
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			filterId = it.getInt(ARG_FILTER_ID)
			filterName = it.getString(ARG_FILTER_NAME) ?: ""
			mediaType = it.getString(ARG_MEDIA_TYPE) ?: "movie"
			filterType = try {
				BrowseFilterType.valueOf(it.getString(ARG_FILTER_TYPE) ?: "GENRE")
			} catch (e: Exception) {
				BrowseFilterType.GENRE
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setContent {
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
