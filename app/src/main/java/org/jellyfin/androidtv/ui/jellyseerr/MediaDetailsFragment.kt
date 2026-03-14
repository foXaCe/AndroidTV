package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMovieDetailsDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrTvDetailsDto
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.NavbarPosition
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.jellyseerr.compose.JellyseerrMediaDetailsScreen
import org.jellyfin.androidtv.ui.jellyseerr.compose.ServerDetailsData
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.toolbar.LeftSidebarNavigation
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.jellyfin.androidtv.ui.jellyseerr.compose.AdvancedRequestOptionsDialog as ComposeAdvancedDialog
import org.jellyfin.androidtv.ui.jellyseerr.compose.QualitySelectionDialog as ComposeQualityDialog
import org.jellyfin.androidtv.ui.jellyseerr.compose.SeasonSelectionDialog as ComposeSeasonDialog

class MediaDetailsFragment : Fragment() {
	data class Args(
		val itemJson: String,
	) {
		fun toBundle() = bundleOf(KEY_ITEM to itemJson)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val itemJson = bundle?.getString(KEY_ITEM) ?: return null
				return Args(itemJson = itemJson)
			}
		}
	}

	companion object {
		internal const val KEY_ITEM = "item"
	}

	private val viewModel: JellyseerrDetailsViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val apiClient: ApiClient by inject()
	private val userPreferences: UserPreferences by inject()

	private var selectedItem: JellyseerrDiscoverItemDto? = null

	// Observable state for Compose recomposition
	private var movieDetails by mutableStateOf<JellyseerrMovieDetailsDto?>(null)
	private var tvDetails by mutableStateOf<JellyseerrTvDetailsDto?>(null)

	// Compose dialog states (Phase 2)
	private var showQualityDialog by mutableStateOf(false)
	private var qualityDialogTitle by mutableStateOf("")
	private var qualityDialogHdStatus by mutableStateOf<Int?>(null)
	private var qualityDialog4kStatus by mutableStateOf<Int?>(null)
	private var qualityDialogOnSelect: ((Boolean) -> Unit)? = null

	private var showSeasonDialog by mutableStateOf(false)
	private var seasonDialogShowName by mutableStateOf("")
	private var seasonDialogNumberOfSeasons by mutableStateOf(1)
	private var seasonDialogIs4k by mutableStateOf(false)
	private var seasonDialogUnavailable by mutableStateOf<Set<Int>>(emptySet())
	private var seasonDialogOnConfirm: ((List<Int>) -> Unit)? = null

	private var showAdvancedDialog by mutableStateOf(false)
	private var advancedDialogTitle by mutableStateOf("")
	private var advancedDialogIs4k by mutableStateOf(false)
	private var advancedDialogIsMovie by mutableStateOf(true)
	private var advancedDialogOnLoadData: (suspend () -> ServerDetailsData?)? = null
	private var advancedDialogOnConfirm: ((AdvancedRequestOptions) -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val itemJson = Args.fromBundle(arguments)?.itemJson
		if (itemJson != null) {
			try {
				selectedItem = Json.decodeFromString<JellyseerrDiscoverItemDto>(itemJson)
			} catch (e: Exception) {
				Timber.e(e, "Failed to deserialize item from arguments")
			}
		}

		if (selectedItem == null) {
			Timber.e("MediaDetailsFragment: No item data found in arguments")
			Toast.makeText(requireContext(), getString(R.string.item_error_not_found), Toast.LENGTH_SHORT).show()
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.JELLYSEERR_MEDIA_ID, ScreenIds.JELLYSEERR_MEDIA_NAME) {
						Box(modifier = Modifier.fillMaxSize()) {
							// Main screen content
							JellyseerrMediaDetailsScreen(
								selectedItem = selectedItem,
								movieDetails = movieDetails,
								tvDetails = tvDetails,
								onRequestClick = { canHd, can4k, hdStatus, status4k ->
									handleRequestClick(canHd, can4k, hdStatus, status4k)
								},
								onCancelClick = { pendingRequests ->
									showCancelRequestDialog(pendingRequests)
								},
								onTrailerClick = { playTrailer() },
								onPlayClick = { playInVegafoX() },
								onCastClick = { castId ->
									navigationRepository.navigate(Destinations.jellyseerrPersonDetails(castId))
								},
								onItemClick = { item ->
									val itemJson = Json.encodeToString(JellyseerrDiscoverItemDto.serializer(), item)
									navigationRepository.navigate(Destinations.jellyseerrMediaDetails(itemJson))
								},
								onKeywordClick = { keywordId, keywordName, mediaType ->
									navigationRepository.navigate(
										Destinations.jellyseerrBrowseBy(
											filterId = keywordId,
											filterName = keywordName,
											mediaType = mediaType,
											filterType = BrowseFilterType.KEYWORD,
										),
									)
								},
								fetchRecommendations = { page ->
									val result =
										when {
											movieDetails != null -> viewModel.getRecommendationsMovies(selectedItem!!.id, page)
											tvDetails != null -> viewModel.getRecommendationsTv(selectedItem!!.id, page)
											else -> null
										}
									result?.getOrNull()?.results ?: emptyList()
								},
								fetchSimilar = { page ->
									val result =
										when {
											movieDetails != null -> viewModel.getSimilarMovies(selectedItem!!.id, page)
											tvDetails != null -> viewModel.getSimilarTv(selectedItem!!.id, page)
											else -> null
										}
									result?.getOrNull()?.results ?: emptyList()
								},
							)

							// Toolbar overlay
							val navbarPosition = userPreferences[UserPreferences.navbarPosition]
							when (navbarPosition) {
								NavbarPosition.LEFT -> {
									LeftSidebarNavigation(
										activeButton = MainToolbarActiveButton.Jellyseerr,
									)
								}
								NavbarPosition.TOP -> {
									MainToolbar(
										activeButton = MainToolbarActiveButton.Jellyseerr,
									)
								}
							}

							// Dialog overlays (Phase 2)
							if (showQualityDialog) {
								ComposeQualityDialog(
									title = qualityDialogTitle,
									canRequestHd = true,
									canRequest4k = true,
									hdStatus = qualityDialogHdStatus,
									status4k = qualityDialog4kStatus,
									onSelect = { is4k ->
										qualityDialogOnSelect?.invoke(is4k)
									},
									onDismiss = { showQualityDialog = false },
								)
							}

							if (showSeasonDialog) {
								ComposeSeasonDialog(
									showName = seasonDialogShowName,
									numberOfSeasons = seasonDialogNumberOfSeasons,
									is4k = seasonDialogIs4k,
									unavailableSeasons = seasonDialogUnavailable,
									onConfirm = { seasons ->
										seasonDialogOnConfirm?.invoke(seasons)
									},
									onDismiss = { showSeasonDialog = false },
								)
							}

							if (showAdvancedDialog) {
								ComposeAdvancedDialog(
									title = advancedDialogTitle,
									is4k = advancedDialogIs4k,
									isMovie = advancedDialogIsMovie,
									onLoadData = {
										advancedDialogOnLoadData?.invoke()
									},
									onConfirm = { options ->
										advancedDialogOnConfirm?.invoke(options)
									},
									onDismiss = { showAdvancedDialog = false },
								)
							}
						}
					}
				}
			}
		}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		loadFullDetails()
	}

	private fun loadFullDetails() {
		val item = selectedItem ?: return

		lifecycleScope.launch {
			try {
				if (item.mediaType == "movie") {
					movieDetails = viewModel.getMovieDetails(item.id).getOrNull()
				} else if (item.mediaType == "tv") {
					tvDetails = viewModel.getTvDetails(item.id).getOrNull()
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to load full details")
			}
		}
	}

	// --- Business logic (unchanged) ---

	private fun handleRequestClick(
		canRequestHd: Boolean,
		canRequest4k: Boolean,
		hdStatus: Int?,
		status4k: Int?,
	) {
		val item = selectedItem ?: return
		val mediaType = item.mediaType ?: return
		val title =
			when (mediaType) {
				"movie" -> movieDetails?.title ?: item.title ?: item.name ?: getString(R.string.lbl_unknown)
				else -> tvDetails?.name ?: item.name ?: item.title ?: getString(R.string.lbl_unknown)
			}

		lifecycleScope.launch {
			val (userCan4k, has4kServer, hasHdServer) = checkQualityAvailability(mediaType)
			val hdAvailable = canRequestHd && hasHdServer
			val fourKAvailable = canRequest4k && userCan4k && has4kServer

			if (hdAvailable && fourKAvailable) {
				qualityDialogTitle = title
				qualityDialogHdStatus = hdStatus
				qualityDialog4kStatus = status4k
				qualityDialogOnSelect = { is4k -> requestContent(is4k) }
				showQualityDialog = true
			} else if (fourKAvailable) {
				requestContent(true)
			} else if (hdAvailable) {
				requestContent(false)
			} else {
				if (!isAdded) return@launch
				val mediaTypeName = if (mediaType == "movie") getString(R.string.lbl_movies) else getString(R.string.lbl_tv_series)
				Toast.makeText(requireContext(), getString(R.string.jellyseerr_no_server_configured, mediaTypeName), Toast.LENGTH_LONG).show()
			}
		}
	}

	private suspend fun checkQualityAvailability(mediaType: String): Triple<Boolean, Boolean, Boolean> =
		try {
			val userResult = viewModel.getCurrentUser()
			val user = userResult.getOrNull()
			val userCan4k =
				when (mediaType) {
					"movie" -> user?.canRequest4kMovies() ?: false
					"tv" -> user?.canRequest4kTv() ?: false
					else -> user?.canRequest4k() ?: false
				}
			val (has4kServer, hasHdServer) =
				when (mediaType) {
					"movie" -> {
						val servers = viewModel.getRadarrServers().getOrNull() ?: emptyList()
						Pair(servers.any { it.is4k }, servers.any { !it.is4k })
					}
					"tv" -> {
						val servers = viewModel.getSonarrServers().getOrNull() ?: emptyList()
						Pair(servers.any { it.is4k }, servers.any { !it.is4k })
					}
					else -> Pair(false, false)
				}
			Triple(userCan4k, has4kServer, hasHdServer)
		} catch (e: Exception) {
			Timber.e(e, "Failed to check quality availability")
			Triple(true, true, true)
		}

	private fun requestContent(is4k: Boolean = false) {
		val item = selectedItem ?: return

		lifecycleScope.launch {
			val userResult = viewModel.getCurrentUser()
			val user = userResult.getOrNull()
			val hasAdvanced = user?.hasAdvancedRequestPermission() ?: false

			if (hasAdvanced) {
				showAdvancedOptionsDialog(item, is4k)
			} else {
				proceedWithRequest(item, is4k, null)
			}
		}
	}

	private fun showAdvancedOptionsDialog(
		item: JellyseerrDiscoverItemDto,
		is4k: Boolean,
	) {
		val isMovie = item.mediaType == "movie"
		val title =
			when {
				isMovie -> movieDetails?.title ?: item.title ?: item.name ?: getString(R.string.lbl_unknown)
				else -> tvDetails?.name ?: item.name ?: item.title ?: getString(R.string.lbl_unknown)
			}

		lifecycleScope.launch {
			val serverExists =
				try {
					if (isMovie) {
						viewModel.getRadarrServers().getOrNull()?.any { it.is4k == is4k } ?: false
					} else {
						viewModel.getSonarrServers().getOrNull()?.any { it.is4k == is4k } ?: false
					}
				} catch (e: Exception) {
					Timber.e(e, "Failed to check server availability")
					false
				}

			if (!serverExists) {
				if (!isAdded) return@launch
				val quality = if (is4k) "4K" else getString(R.string.jellyseerr_quality_hd_full)
				val mediaTypeName = if (isMovie) getString(R.string.jellyseerr_media_movies) else getString(R.string.jellyseerr_media_tv_shows)
				Toast.makeText(requireContext(), getString(R.string.jellyseerr_no_quality_server, quality, mediaTypeName), Toast.LENGTH_LONG).show()
				return@launch
			}

			advancedDialogTitle = title
			advancedDialogIs4k = is4k
			advancedDialogIsMovie = isMovie
			advancedDialogOnLoadData = { loadServerDetailsForAdvancedOptions(isMovie, is4k) }
			advancedDialogOnConfirm = { options -> proceedWithRequest(item, is4k, options) }
			showAdvancedDialog = true
		}
	}

	private suspend fun loadServerDetailsForAdvancedOptions(
		isMovie: Boolean,
		is4k: Boolean,
	): ServerDetailsData? {
		return try {
			if (isMovie) {
				val servers = viewModel.getRadarrServers().getOrNull() ?: return null
				val server = servers.find { it.is4k == is4k } ?: return null
				val details = viewModel.getRadarrServerDetails(server.id).getOrNull() ?: return null
				ServerDetailsData(
					serverId = server.id,
					profiles = details.profiles,
					rootFolders = details.rootFolders,
					defaultProfileId = server.activeProfileId,
					defaultRootFolder = server.activeDirectory,
				)
			} else {
				val servers = viewModel.getSonarrServers().getOrNull() ?: return null
				val server = servers.find { it.is4k == is4k } ?: return null
				val details = viewModel.getSonarrServerDetails(server.id).getOrNull() ?: return null
				ServerDetailsData(
					serverId = server.id,
					profiles = details.profiles,
					rootFolders = details.rootFolders,
					defaultProfileId = server.activeProfileId,
					defaultRootFolder = server.activeDirectory,
				)
			}
		} catch (e: Exception) {
			Timber.e(e, "Failed to load server details for advanced options")
			null
		}
	}

	private fun proceedWithRequest(
		item: JellyseerrDiscoverItemDto,
		is4k: Boolean,
		advancedOptions: AdvancedRequestOptions?,
	) {
		if (item.mediaType == "tv") {
			val numberOfSeasons = tvDetails?.numberOfSeasons ?: 1
			val showName = tvDetails?.name ?: item.name ?: item.title ?: getString(R.string.lbl_unknown)
			val unavailableSeasons = getUnavailableSeasons(is4k)

			seasonDialogShowName = showName
			seasonDialogNumberOfSeasons = numberOfSeasons
			seasonDialogIs4k = is4k
			seasonDialogUnavailable = unavailableSeasons
			seasonDialogOnConfirm = { selectedSeasons ->
				submitRequest(item, selectedSeasons, is4k, advancedOptions)
			}
			showSeasonDialog = true
		} else {
			submitRequest(item, null, is4k, advancedOptions)
		}
	}

	private fun getUnavailableSeasons(is4k: Boolean): Set<Int> {
		val unavailableSeasons = mutableSetOf<Int>()
		val mediaInfo = tvDetails?.mediaInfo ?: return unavailableSeasons
		mediaInfo.requests?.forEach { request ->
			if (request.is4k == is4k && request.status != JellyseerrRequestDto.STATUS_DECLINED) {
				request.seasons?.forEach { seasonRequest ->
					unavailableSeasons.add(seasonRequest.seasonNumber)
				}
			}
		}
		return unavailableSeasons
	}

	private fun submitRequest(
		item: JellyseerrDiscoverItemDto,
		seasons: List<Int>?,
		is4k: Boolean,
		advancedOptions: AdvancedRequestOptions? = null,
	) {
		lifecycleScope.launch {
			try {
				val result = viewModel.requestMedia(item, seasons, is4k, advancedOptions)
				if (!isAdded) return@launch

				result
					.onSuccess {
						val quality = if (is4k) "4K" else "HD"
						val seasonInfo =
							if (seasons != null) {
								if (seasons.size == tvDetails?.numberOfSeasons) {
									getString(R.string.jellyseerr_all_seasons_suffix)
								} else {
									resources.getQuantityString(R.plurals.jellyseerr_season_count_suffix, seasons.size, seasons.size)
								}
							} else {
								""
							}
						Toast.makeText(requireContext(), getString(R.string.jellyseerr_request_submitted, quality, seasonInfo), Toast.LENGTH_SHORT).show()
						loadFullDetails()
					}.onFailure { error ->
						Toast.makeText(requireContext(), getString(R.string.jellyseerr_request_error, error.message ?: ""), Toast.LENGTH_LONG).show()
					}
			} catch (e: Exception) {
				Timber.e(e, "Request failed")
				if (isAdded) {
					Toast.makeText(requireContext(), getString(R.string.jellyseerr_request_error, e.message ?: ""), Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun showCancelRequestDialog(pendingRequests: List<JellyseerrRequestDto>) {
		if (pendingRequests.isEmpty()) return
		val item = selectedItem ?: return
		val title =
			when (item.mediaType) {
				"movie" -> movieDetails?.title ?: item.title ?: item.name ?: getString(R.string.lbl_unknown)
				else -> tvDetails?.name ?: item.name ?: item.title ?: getString(R.string.lbl_unknown)
			}

		val description =
			if (pendingRequests.size == 1) {
				val req = pendingRequests.first()
				val quality = if (req.is4k) "4K" else "HD"
				resources.getQuantityString(R.plurals.jellyseerr_cancel_confirm, 1, quality, title)
			} else {
				val hdCount = pendingRequests.count { !it.is4k }
				val fourKCount = pendingRequests.count { it.is4k }
				val parts = mutableListOf<String>()
				if (hdCount > 0) parts.add("$hdCount HD")
				if (fourKCount > 0) parts.add("$fourKCount 4K")
				val separator = getString(R.string.jellyseerr_cancel_and_separator)
				resources.getQuantityString(R.plurals.jellyseerr_cancel_confirm, pendingRequests.size, parts.joinToString(separator), title)
			}

		android.app.AlertDialog
			.Builder(requireContext())
			.setTitle(getString(R.string.jellyseerr_cancel_request))
			.setMessage(description)
			.setPositiveButton(getString(R.string.jellyseerr_cancel_request)) { _, _ ->
				cancelPendingRequests(pendingRequests)
			}.setNegativeButton(getString(R.string.jellyseerr_keep_request), null)
			.show()
	}

	private fun cancelPendingRequests(requests: List<JellyseerrRequestDto>) {
		lifecycleScope.launch {
			try {
				var successCount = 0
				var failCount = 0
				for (request in requests) {
					val result = viewModel.cancelRequest(request.id)
					if (result.isSuccess) successCount++ else failCount++
				}
				if (!isAdded) return@launch

				val message =
					when {
						failCount == 0 && successCount == 1 -> getString(R.string.jellyseerr_request_cancelled)
						failCount == 0 -> getString(R.string.jellyseerr_requests_cancelled, successCount)
						successCount == 0 ->
							if (failCount >
								1
							) {
								getString(R.string.jellyseerr_cancel_failed_plural)
							} else {
								getString(R.string.jellyseerr_cancel_failed)
							}
						else -> {
							val cancelled = resources.getQuantityString(R.plurals.jellyseerr_cancelled_count, successCount, successCount)
							val failed = resources.getQuantityString(R.plurals.jellyseerr_failed_count, failCount, failCount)
							"$cancelled, $failed"
						}
					}
				Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
				loadFullDetails()
			} catch (e: Exception) {
				Timber.e(e, "Error cancelling requests")
				if (isAdded) {
					Toast.makeText(requireContext(), getString(R.string.jellyseerr_error_generic, e.message ?: ""), Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun playTrailer() {
		val item = selectedItem ?: return
		val year =
			when {
				item.mediaType == "movie" -> item.releaseDate?.take(4)
				else -> item.firstAirDate?.take(4)
			}
		val title = item.title ?: item.name ?: getString(R.string.lbl_unknown)
		val searchQuery = "$title ${year ?: ""} official trailer"

		try {
			val youtubeSearchUrl = "https://www.youtube.com/results?search_query=${android.net.Uri.encode(searchQuery)}"
			val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(youtubeSearchUrl))
			val chooser = android.content.Intent.createChooser(intent, getString(R.string.lbl_play_trailer))
			startActivity(chooser)
		} catch (e: Exception) {
			Timber.e(e, "Error opening trailer")
			Toast.makeText(requireContext(), getString(R.string.playback_trailer_error), Toast.LENGTH_SHORT).show()
		}
	}

	private fun playInVegafoX() {
		lifecycleScope.launch {
			try {
				val externalIds = movieDetails?.externalIds ?: tvDetails?.externalIds
				val tmdbId = externalIds?.tmdbId
				val tvdbId = externalIds?.tvdbId
				val imdbId = externalIds?.imdbId
				val title = movieDetails?.title ?: tvDetails?.name ?: tvDetails?.title ?: selectedItem?.title ?: selectedItem?.name
				val mediaType = movieDetails?.mediaType ?: tvDetails?.mediaType ?: selectedItem?.mediaType

				val jellyfinItem = searchForItemByProviderIds(tmdbId, tvdbId, imdbId, title, mediaType)

				if (jellyfinItem != null) {
					navigationRepository.navigate(Destinations.itemDetails(jellyfinItem.id))
				} else {
					Toast.makeText(requireContext(), getString(R.string.playback_item_not_found), Toast.LENGTH_SHORT).show()
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to search for item in VegafoX")
				Toast.makeText(requireContext(), getString(R.string.playback_search_error), Toast.LENGTH_SHORT).show()
			}
		}
	}

	private suspend fun searchForItemByProviderIds(
		tmdbId: Int?,
		tvdbId: Int?,
		imdbId: String?,
		title: String?,
		mediaType: String?,
	): BaseItemDto? =
		withContext(Dispatchers.IO) {
			try {
				if (title == null) return@withContext null

				val includeItemTypes =
					when (mediaType) {
						"movie" -> setOf(BaseItemKind.MOVIE)
						"tv" -> setOf(BaseItemKind.SERIES)
						else -> setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES)
					}

				val response by apiClient.itemsApi.getItems(
					searchTerm = title,
					includeItemTypes = includeItemTypes,
					recursive = true,
					limit = 50,
				)

				if (tmdbId != null) {
					response.items.firstOrNull { it.providerIds?.get("Tmdb") == tmdbId.toString() }?.let { return@withContext it }
				}
				if (tvdbId != null) {
					response.items.firstOrNull { it.providerIds?.get("Tvdb") == tvdbId.toString() }?.let { return@withContext it }
				}
				if (imdbId != null) {
					response.items.firstOrNull { it.providerIds?.get("Imdb") == imdbId }?.let { return@withContext it }
				}
				response.items.firstOrNull { it.name.equals(title, ignoreCase = true) }
					?: response.items.firstOrNull()
			} catch (e: Exception) {
				Timber.e(e, "Failed to search Jellyfin library")
				null
			}
		}
}
