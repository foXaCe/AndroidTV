package org.jellyfin.androidtv.ui.jellyseerr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.repository.JellyseerrRepository
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.data.service.jellyseerr.Seasons
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class JellyseerrDetailsViewModel(
	private val jellyseerrRepository: JellyseerrRepository,
) : ViewModel() {
	private var userPreferences: JellyseerrPreferences? = null

	private suspend fun getPreferences(): JellyseerrPreferences? {
		if (userPreferences == null) {
			userPreferences = jellyseerrRepository.getPreferences()
		}
		return userPreferences
	}

	private val _loadingState = MutableStateFlow<JellyseerrLoadingState>(JellyseerrLoadingState.Idle)
	val loadingState: StateFlow<JellyseerrLoadingState> = _loadingState.asStateFlow()

	private val _userRequests = MutableStateFlow<List<JellyseerrRequestDto>>(emptyList())
	val userRequests: StateFlow<List<JellyseerrRequestDto>> = _userRequests.asStateFlow()

	val isAvailable: StateFlow<Boolean> = jellyseerrRepository.isAvailable

	// --- Media details ---

	suspend fun getMovieDetails(tmdbId: Int) = jellyseerrRepository.getMovieDetails(tmdbId)

	suspend fun getTvDetails(tmdbId: Int) = jellyseerrRepository.getTvDetails(tmdbId)

	suspend fun getSimilarMovies(
		tmdbId: Int,
		page: Int = 1,
	) = jellyseerrRepository.getSimilarMovies(tmdbId, page)

	suspend fun getSimilarTv(
		tmdbId: Int,
		page: Int = 1,
	) = jellyseerrRepository.getSimilarTv(tmdbId, page)

	suspend fun getRecommendationsMovies(
		tmdbId: Int,
		page: Int = 1,
	) = jellyseerrRepository.getRecommendationsMovies(tmdbId, page)

	suspend fun getRecommendationsTv(
		tmdbId: Int,
		page: Int = 1,
	) = jellyseerrRepository.getRecommendationsTv(tmdbId, page)

	// --- Person details ---

	suspend fun getPersonDetails(personId: Int) = jellyseerrRepository.getPersonDetails(personId)

	suspend fun getPersonCombinedCredits(personId: Int) = jellyseerrRepository.getPersonCombinedCredits(personId)

	// --- Requests ---

	fun loadRequests() {
		viewModelScope.launch {
			if (!isAvailable.value) return@launch
			loadRequestsSuspend()
		}
	}

	private suspend fun loadRequestsSuspend() {
		_loadingState.emit(JellyseerrLoadingState.Loading)
		try {
			val currentUserResult = jellyseerrRepository.getCurrentUser()
			if (currentUserResult.isFailure) {
				val error = currentUserResult.exceptionOrNull()?.message ?: "Failed to get current user"
				Timber.e("JellyseerrDetailsViewModel: Error getting current user: $error")
				_loadingState.emit(JellyseerrLoadingState.Error(error))
				return
			}

			val currentUser = currentUserResult.getOrNull()!!

			val result =
				jellyseerrRepository.getRequests(
					filter = "all",
					requestedBy = currentUser.id,
					limit = 20,
				)

			if (result.isSuccess) {
				val userRequests = result.getOrNull()?.results ?: emptyList()

				val filteredRequests =
					userRequests.filter { request ->
						when (request.status) {
							1 -> true // Pending
							2 -> true // Approved/Processing
							3 -> isWithinDays(request.updatedAt, 3) // Declined - recent only
							4 -> true // Available
							else -> true
						}
					}

				val movieCache = mutableMapOf<Int, JellyseerrMediaDto?>()
				val tvCache = mutableMapOf<Int, JellyseerrMediaDto?>()
				val semaphore = kotlinx.coroutines.sync.Semaphore(5)

				val enrichedRequests =
					coroutineScope {
						filteredRequests
							.map { request ->
								async {
									val tmdbId = request.media?.tmdbId
									if (tmdbId == null) {
										Timber.w("JellyseerrDetailsViewModel: Request ${request.id} has no tmdbId, skipping enrichment")
										return@async request
									}

									val enrichedMedia =
										when (request.type) {
											"movie" -> {
												movieCache.getOrPut(tmdbId) {
													semaphore.acquire()
													try {
														val result = jellyseerrRepository.getMovieDetails(tmdbId)
														if (result.isSuccess) {
															val movieDetails = result.getOrNull()
															request.media?.copy(
																title = movieDetails?.title,
																posterPath = movieDetails?.posterPath,
																backdropPath = movieDetails?.backdropPath,
																overview = movieDetails?.overview,
															)
														} else {
															Timber.w("JellyseerrDetailsViewModel: Failed to fetch movie details for tmdbId: $tmdbId")
															request.media
														}
													} finally {
														semaphore.release()
													}
												}
											}
											"tv" -> {
												tvCache.getOrPut(tmdbId) {
													semaphore.acquire()
													try {
														val result = jellyseerrRepository.getTvDetails(tmdbId)
														if (result.isSuccess) {
															val tvDetails = result.getOrNull()
															request.media?.copy(
																name = tvDetails?.name ?: tvDetails?.title,
																posterPath = tvDetails?.posterPath,
																backdropPath = tvDetails?.backdropPath,
																overview = tvDetails?.overview,
															)
														} else {
															Timber.w("JellyseerrDetailsViewModel: Failed to fetch TV details for tmdbId: $tmdbId")
															request.media
														}
													} finally {
														semaphore.release()
													}
												}
											}
											else -> {
												Timber.w("JellyseerrDetailsViewModel: Unknown media type: ${request.type}")
												request.media
											}
										}

									request.copy(media = enrichedMedia)
								}
							}.awaitAll()
					}

				_userRequests.emit(enrichedRequests)
				_loadingState.emit(JellyseerrLoadingState.Success())
			} else {
				val error = result.exceptionOrNull()?.message ?: "Failed to load requests"
				Timber.e("JellyseerrDetailsViewModel: Error loading requests: $error")
				_loadingState.emit(JellyseerrLoadingState.Error(error))
			}
		} catch (error: Exception) {
			Timber.e(error, "Failed to load requests - Exception")
			_loadingState.emit(JellyseerrLoadingState.Error(error.message ?: "Unknown error"))
		}
	}

	suspend fun requestMedia(
		item: JellyseerrDiscoverItemDto,
		seasons: List<Int>? = null,
		is4k: Boolean = false,
		advancedOptions: AdvancedRequestOptions? = null,
	): Result<Unit> {
		return try {
			val mediaType = item.mediaType ?: return Result.failure(Exception("Unknown media type"))
			val mediaId = item.id
			requestContent(mediaId, mediaType, seasons, is4k, advancedOptions)
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	private suspend fun requestContent(
		mediaId: Int,
		mediaType: String,
		seasons: List<Int>?,
		is4k: Boolean = false,
		advancedOptions: AdvancedRequestOptions? = null,
	) {
		Timber.d("JellyseerrDetailsViewModel: Requesting media - ID: $mediaId, Type: $mediaType, Seasons: $seasons, 4K: $is4k")

		val seasonsParam =
			when {
				mediaType != "tv" -> null
				seasons == null -> Seasons.All
				else -> Seasons.List(seasons)
			}

		val prefs = getPreferences()

		val profileId =
			advancedOptions?.profileId ?: when {
				mediaType == "movie" && is4k -> prefs?.get(JellyseerrPreferences.fourKMovieProfileId)?.toIntOrNull()
				mediaType == "movie" && !is4k -> prefs?.get(JellyseerrPreferences.hdMovieProfileId)?.toIntOrNull()
				mediaType == "tv" && is4k -> prefs?.get(JellyseerrPreferences.fourKTvProfileId)?.toIntOrNull()
				mediaType == "tv" && !is4k -> prefs?.get(JellyseerrPreferences.hdTvProfileId)?.toIntOrNull()
				else -> null
			}

		val rootFolderId =
			advancedOptions?.rootFolderId ?: when {
				mediaType == "movie" && is4k -> prefs?.get(JellyseerrPreferences.fourKMovieRootFolderId)?.toIntOrNull()
				mediaType == "movie" && !is4k -> prefs?.get(JellyseerrPreferences.hdMovieRootFolderId)?.toIntOrNull()
				mediaType == "tv" && is4k -> prefs?.get(JellyseerrPreferences.fourKTvRootFolderId)?.toIntOrNull()
				mediaType == "tv" && !is4k -> prefs?.get(JellyseerrPreferences.hdTvRootFolderId)?.toIntOrNull()
				else -> null
			}

		val serverId =
			advancedOptions?.serverId ?: when {
				mediaType == "movie" && is4k -> prefs?.get(JellyseerrPreferences.fourKMovieServerId)?.toIntOrNull()
				mediaType == "movie" && !is4k -> prefs?.get(JellyseerrPreferences.hdMovieServerId)?.toIntOrNull()
				mediaType == "tv" && is4k -> prefs?.get(JellyseerrPreferences.fourKTvServerId)?.toIntOrNull()
				mediaType == "tv" && !is4k -> prefs?.get(JellyseerrPreferences.hdTvServerId)?.toIntOrNull()
				else -> null
			}

		val result = jellyseerrRepository.createRequest(mediaId, mediaType, seasonsParam, is4k, profileId, rootFolderId, serverId)
		if (result.isFailure) {
			val error = result.exceptionOrNull()
			Timber.e(error, "Failed to request content")
			throw error ?: Exception("Unknown error while requesting content")
		}
		loadRequestsSuspend()
	}

	suspend fun cancelRequest(requestId: Int): Result<Unit> {
		Timber.d("JellyseerrDetailsViewModel: Cancelling request ID: $requestId")
		val result = jellyseerrRepository.deleteRequest(requestId)
		if (result.isSuccess) {
			loadRequestsSuspend()
		} else {
			Timber.e(result.exceptionOrNull(), "JellyseerrDetailsViewModel: Failed to cancel request $requestId")
		}
		return result
	}

	// --- Server settings ---

	suspend fun getCurrentUser() = jellyseerrRepository.getCurrentUser()

	suspend fun getRadarrServers() = jellyseerrRepository.getRadarrServers()

	suspend fun getRadarrServerDetails(serverId: Int) = jellyseerrRepository.getRadarrServerDetails(serverId)

	suspend fun getSonarrServers() = jellyseerrRepository.getSonarrServers()

	suspend fun getSonarrServerDetails(serverId: Int) = jellyseerrRepository.getSonarrServerDetails(serverId)

	// --- Utilities ---

	private fun isWithinDays(
		dateString: String?,
		days: Int,
	): Boolean {
		if (dateString == null) return false

		return try {
			val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
			val date = dateFormat.parse(dateString) ?: return false

			val now = Date()
			val diffInMillis = now.time - date.time
			val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

			diffInDays <= days
		} catch (e: Exception) {
			Timber.w(e, "Failed to parse date: $dateString")
			false
		}
	}
}
