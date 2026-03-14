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
import org.jellyfin.androidtv.constant.JellyseerrFetchLimit
import org.jellyfin.androidtv.data.repository.JellyseerrRepository
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrNetworkDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrStudioDto
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.androidtv.util.ErrorHandler
import timber.log.Timber

class JellyseerrDiscoverViewModel(
	private val jellyseerrRepository: JellyseerrRepository,
) : ViewModel() {
	// Cache for user preferences (loaded asynchronously)
	private var userPreferences: JellyseerrPreferences? = null

	private suspend fun getPreferences(): JellyseerrPreferences? {
		if (userPreferences == null) {
			userPreferences = jellyseerrRepository.getPreferences()
		}
		return userPreferences
	}

	companion object {
		// Popular TV networks (from Seerr - using duotone filtered URLs)
		val POPULAR_NETWORKS =
			listOf(
				JellyseerrNetworkDto(
					id = 213,
					name = "Netflix",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/wwemzKWzjKYJFfCeiB57q3r4Bcm.png",
				),
				JellyseerrNetworkDto(
					id = 2739,
					name = "Disney+",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/gJ8VX6JSu3ciXHuC2dDGAo2lvwM.png",
				),
				JellyseerrNetworkDto(
					id = 1024,
					name = "Prime Video",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/ifhbNuuVnlwYy5oXA5VIb2YR8AZ.png",
				),
				JellyseerrNetworkDto(
					id = 2552,
					name = "Apple TV+",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/4KAy34EHvRM25Ih8wb82AuGU7zJ.png",
				),
				JellyseerrNetworkDto(
					id = 453,
					name = "Hulu",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/pqUTCleNUiTLAVlelGxUgWn1ELh.png",
				),
				JellyseerrNetworkDto(
					id = 49,
					name = "HBO",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/tuomPhY2UtuPTqqFnKMVHvSb724.png",
				),
				JellyseerrNetworkDto(
					id = 4353,
					name = "Discovery+",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/1D1bS3Dyw4ScYnFWTlBOvJXC3nb.png",
				),
				JellyseerrNetworkDto(
					id = 2,
					name = "ABC",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/ndAvF4JLsliGreX87jAc9GdjmJY.png",
				),
				JellyseerrNetworkDto(
					id = 19,
					name = "FOX",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/1DSpHrWyOORkL9N2QHX7Adt31mQ.png",
				),
				JellyseerrNetworkDto(
					id = 359,
					name = "Cinemax",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/6mSHSquNpfLgDdv6VnOOvC5Uz2h.png",
				),
				JellyseerrNetworkDto(
					id = 174,
					name = "AMC",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/pmvRmATOCaDykE6JrVoeYxlFHw3.png",
				),
				JellyseerrNetworkDto(
					id = 67,
					name = "Showtime",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/Allse9kbjiP6ExaQrnSpIhkurEi.png",
				),
				JellyseerrNetworkDto(
					id = 318,
					name = "Starz",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/8GJjw3HHsAJYwIWKIPBPfqMxlEa.png",
				),
				JellyseerrNetworkDto(
					id = 71,
					name = "The CW",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/ge9hzeaU7nMtQ4PjkFlc68dGAJ9.png",
				),
				JellyseerrNetworkDto(
					id = 6,
					name = "NBC",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/o3OedEP0f9mfZr33jz2BfXOUK5.png",
				),
				JellyseerrNetworkDto(
					id = 16,
					name = "CBS",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/nm8d7P7MJNiBLdgIzUK0gkuEA4r.png",
				),
				JellyseerrNetworkDto(
					id = 4330,
					name = "Paramount+",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/fi83B1oztoS47xxcemFdPMhIzK.png",
				),
				JellyseerrNetworkDto(
					id = 4,
					name = "BBC One",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/mVn7xESaTNmjBUyUtGNvDQd3CT1.png",
				),
				JellyseerrNetworkDto(
					id = 56,
					name = "Cartoon Network",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/c5OC6oVCg6QP4eqzW6XIq17CQjI.png",
				),
				JellyseerrNetworkDto(
					id = 80,
					name = "Adult Swim",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/9AKyspxVzywuaMuZ1Bvilu8sXly.png",
				),
				JellyseerrNetworkDto(
					id = 13,
					name = "Nickelodeon",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/ikZXxg6GnwpzqiZbRPhJGaZapqB.png",
				),
				JellyseerrNetworkDto(
					id = 3353,
					name = "Peacock",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/gIAcGTjKKr0KOHL5s4O36roJ8p7.png",
				),
			)
		
		// Popular movie studios (from Seerr - using duotone filtered URLs)
		val POPULAR_STUDIOS =
			listOf(
				JellyseerrStudioDto(
					id = 2,
					name = "Disney",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/wdrCwmRnLFJhEoH8GSfymY85KHT.png",
				),
				JellyseerrStudioDto(
					id = 127928,
					name = "20th Century Studios",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/h0rjX5vjW5r8yEnUBStFarjcLT4.png",
				),
				JellyseerrStudioDto(
					id = 34,
					name = "Sony Pictures",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/GagSvqWlyPdkFHMfQ3pNq6ix9P.png",
				),
				JellyseerrStudioDto(
					id = 174,
					name = "Warner Bros. Pictures",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/ky0xOc5OrhzkZ1N6KyUxacfQsCk.png",
				),
				JellyseerrStudioDto(
					id = 33,
					name = "Universal",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/8lvHyhjr8oUKOOy2dKXoALWKdp0.png",
				),
				JellyseerrStudioDto(
					id = 4,
					name = "Paramount",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/fycMZt242LVjagMByZOLUGbCvv3.png",
				),
				JellyseerrStudioDto(
					id = 3,
					name = "Pixar",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/1TjvGVDMYsj6JBxOAkUHpPEwLf7.png",
				),
				JellyseerrStudioDto(
					id = 521,
					name = "DreamWorks",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/kP7t6RwGz2AvvTkvnI1uteEwHet.png",
				),
				JellyseerrStudioDto(
					id = 420,
					name = "Marvel Studios",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/hUzeosd33nzE5MCNsZxCGEKTXaQ.png",
				),
				JellyseerrStudioDto(
					id = 9993,
					name = "DC",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/2Tc1P3Ac8M479naPp1kYT3izLS5.png",
				),
				JellyseerrStudioDto(
					id = 41077,
					name = "A24",
					logoPath = "https://image.tmdb.org/t/p/w780_filter(duotone,ffffff,bababa)/1ZXsGaFPgrgS6ZZGS37AqD5uU12.png",
				),
			)
	}

	private val _loadingState = MutableStateFlow<JellyseerrLoadingState>(JellyseerrLoadingState.Idle)
	val loadingState: StateFlow<JellyseerrLoadingState> = _loadingState.asStateFlow()

	private val _trendingMovies = MutableStateFlow<List<JellyseerrDiscoverItemDto>>(emptyList())
	val trendingMovies: StateFlow<List<JellyseerrDiscoverItemDto>> = _trendingMovies.asStateFlow()

	private val _trendingTv = MutableStateFlow<List<JellyseerrDiscoverItemDto>>(emptyList())
	val trendingTv: StateFlow<List<JellyseerrDiscoverItemDto>> = _trendingTv.asStateFlow()

	private val _trending = MutableStateFlow<List<JellyseerrDiscoverItemDto>>(emptyList())
	val trending: StateFlow<List<JellyseerrDiscoverItemDto>> = _trending.asStateFlow()

	private val _upcomingMovies = MutableStateFlow<List<JellyseerrDiscoverItemDto>>(emptyList())
	val upcomingMovies: StateFlow<List<JellyseerrDiscoverItemDto>> = _upcomingMovies.asStateFlow()

	private val _upcomingTv = MutableStateFlow<List<JellyseerrDiscoverItemDto>>(emptyList())
	val upcomingTv: StateFlow<List<JellyseerrDiscoverItemDto>> = _upcomingTv.asStateFlow()

	private val _movieGenres = MutableStateFlow<List<JellyseerrGenreDto>>(emptyList())
	val movieGenres: StateFlow<List<JellyseerrGenreDto>> = _movieGenres.asStateFlow()

	private val _tvGenres = MutableStateFlow<List<JellyseerrGenreDto>>(emptyList())
	val tvGenres: StateFlow<List<JellyseerrGenreDto>> = _tvGenres.asStateFlow()
	
	private val _networks = MutableStateFlow(POPULAR_NETWORKS)
	val networks: StateFlow<List<JellyseerrNetworkDto>> = _networks.asStateFlow()
	
	private val _studios = MutableStateFlow(POPULAR_STUDIOS)
	val studios: StateFlow<List<JellyseerrStudioDto>> = _studios.asStateFlow()

	private var trendingCurrentPage = 3
	private var trendingMoviesCurrentPage = 3
	private var trendingTvCurrentPage = 3
	private var upcomingMoviesCurrentPage = 3
	private var upcomingTvCurrentPage = 3
	private var isLoadingMoreTrending = false
	private var isLoadingMoreTrendingMovies = false
	private var isLoadingMoreTrendingTv = false
	private var isLoadingMoreUpcomingMovies = false
	private var isLoadingMoreUpcomingTv = false

	private fun List<JellyseerrDiscoverItemDto>.filterNsfw(blockNsfw: Boolean): List<JellyseerrDiscoverItemDto> {
		return if (blockNsfw) {
			val filtered =
				filter { item ->
					// Always block if marked as adult by TMDB
					if (item.adult) {
						val title = item.title ?: item.name ?: "Unknown"
						Timber.d("Jellyseerr Filter: Blocked '$title' (marked as adult)")
						return@filter false
					}
				
					// Apply keyword filtering
					val displayTitle = (item.title ?: item.name ?: "").lowercase()
					val overview = (item.overview ?: "").lowercase()
					val combinedText = "$displayTitle $overview"
				
					// NSFW content keywords
					val matureKeywords =
						listOf(
							"\\bsex\\b",
							"sexual",
							"\\bporn\\b",
							"erotic",
							"\\bnude\\b",
							"nudity",
							"\\bxxx\\b",
							"adult film",
							"prostitute",
							"stripper",
							"\\bescort\\b",
							"seduction",
							"\\baffair\\b",
							"threesome",
							"\\borgy\\b",
							"kinky",
							"fetish",
							"\\bbdsm\\b",
							"dominatrix",
						)
				
					// Block if any mature keyword is found (using regex for word boundaries)
					val matchedKeyword =
						matureKeywords.firstOrNull { keyword ->
							combinedText.contains(Regex(keyword))
						}
				
					if (matchedKeyword != null) {
						val title = item.title ?: item.name ?: "Unknown"
						Timber.d("Jellyseerr Filter: Blocked '$title' (keyword: ${matchedKeyword.replace("\\\\b", "")})")
						return@filter false
					}
				
					true
				}
			
			filtered
		} else {
			this
		}
	}

	val isAvailable: StateFlow<Boolean> = jellyseerrRepository.isAvailable

	/** Returns true if discover content has already been loaded (avoids redundant API calls on resume). */
	fun hasContent(): Boolean =
		_trending.value.isNotEmpty() ||
			_trendingMovies.value.isNotEmpty() ||
			_trendingTv.value.isNotEmpty() ||
			_upcomingMovies.value.isNotEmpty() ||
			_upcomingTv.value.isNotEmpty()

	fun loadTrendingContent() {
		viewModelScope.launch {
			if (!isAvailable.value) return@launch
			_loadingState.emit(JellyseerrLoadingState.Loading)
			try {
				// Get preferences for fetch limit and NSFW filter
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
			
				var hasPermissionError = false
			
				val results =
					coroutineScope {
						listOf(
							async { jellyseerrRepository.getTrending(limit = itemsPerPage, offset = 0) },
							async { jellyseerrRepository.getTrendingMovies(limit = itemsPerPage, offset = 0) },
							async { jellyseerrRepository.getTrendingTv(limit = itemsPerPage, offset = 0) },
							async { jellyseerrRepository.getUpcomingMovies(limit = itemsPerPage, offset = 0) },
							async { jellyseerrRepository.getUpcomingTv(limit = itemsPerPage, offset = 0) },
						).awaitAll()
					}
			
				val trendingResult = results[0]
				val trendingMoviesResult = results[1]
				val trendingTvResult = results[2]
				val upcomingMoviesResult = results[3]
				val upcomingTvResult = results[4]
			
				if (trendingResult.isFailure && trendingResult.exceptionOrNull()?.message?.contains("403") == true) {
					hasPermissionError = true
				}

				val allTrending = trendingResult.getOrNull()?.results ?: emptyList()
				val allTrendingMovies = trendingMoviesResult.getOrNull()?.results ?: emptyList()
				val allTrendingTv = trendingTvResult.getOrNull()?.results ?: emptyList()
				val allUpcomingMovies = upcomingMoviesResult.getOrNull()?.results ?: emptyList()
				val allUpcomingTv = upcomingTvResult.getOrNull()?.results ?: emptyList()

				if (allTrending.isNotEmpty() || allTrendingMovies.isNotEmpty() || allTrendingTv.isNotEmpty()) {
					// Filter out already-available content, blacklisted items (server-side status), NSFW content
					val trending =
						allTrending
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() in listOf("movie", "tv") }
							.filterNsfw(blockNsfw)
					val trendingMovies =
						allTrendingMovies
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "movie" }
							.filterNsfw(blockNsfw)
					val trendingTv =
						allTrendingTv
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "tv" }
							.filterNsfw(blockNsfw)
					val upcomingMovies =
						allUpcomingMovies
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "movie" }
							.filterNsfw(blockNsfw)
					val upcomingTv =
						allUpcomingTv
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "tv" }
							.filterNsfw(blockNsfw)
				
					_trending.emit(trending)
					_trendingMovies.emit(trendingMovies)
					_trendingTv.emit(trendingTv)
					_upcomingMovies.emit(upcomingMovies)
					_upcomingTv.emit(upcomingTv)
					_loadingState.emit(JellyseerrLoadingState.Success())
				
					trendingCurrentPage = 1
					trendingMoviesCurrentPage = 1
					trendingTvCurrentPage = 1
					upcomingMoviesCurrentPage = 1
					upcomingTvCurrentPage = 1
				} else if (hasPermissionError) {
					val errorMessage =
						"Permission Denied: Your Jellyfin account needs Jellyseerr permissions.\n\n" +
							"To fix this:\n" +
							"1. Open Jellyseerr web UI (http://your-server:5055)\n" +
							"2. Go to Settings → Users\n" +
							"3. Find your Jellyfin account\n" +
							"4. Enable 'REQUEST' permission\n" +
							"5. Restart this app"
					_loadingState.emit(JellyseerrLoadingState.Error(errorMessage))
				} else {
					_loadingState.emit(
						JellyseerrLoadingState.Error("Failed to load trending content"),
					)
				}
			} catch (error: Exception) {
				val errorMessage = ErrorHandler.handle(error, "load trending content")
				_loadingState.emit(JellyseerrLoadingState.Error(errorMessage))
			}
		}
	}

	fun loadGenres() {
		viewModelScope.launch {
			if (!isAvailable.value) return@launch
			try {
				coroutineScope {
					val movieGenresDeferred = async { jellyseerrRepository.getGenreSliderMovies() }
					val tvGenresDeferred = async { jellyseerrRepository.getGenreSliderTv() }
					
					val movieGenresResult = movieGenresDeferred.await()
					val tvGenresResult = tvGenresDeferred.await()
					
					if (movieGenresResult.isSuccess) {
						val genres = movieGenresResult.getOrNull() ?: emptyList()
						_movieGenres.emit(genres)
					}
					if (tvGenresResult.isSuccess) {
						val genres = tvGenresResult.getOrNull() ?: emptyList()
						_tvGenres.emit(genres)
					}
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load genres")
			}
		}
	}

	fun loadNextTrendingPage() {
		if (isLoadingMoreTrending) return
		viewModelScope.launch {
			isLoadingMoreTrending = true
			try {
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
				trendingCurrentPage++
				val offset = (trendingCurrentPage - 1) * itemsPerPage
				val result = jellyseerrRepository.getTrending(limit = itemsPerPage, offset = offset)
				
				if (result.isSuccess) {
					val newItems = result.getOrNull()?.results ?: emptyList()
					val filtered =
						newItems
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() in listOf("movie", "tv") }
							.filterNsfw(blockNsfw)
					val currentList = _trending.value.toMutableList()
					currentList.addAll(filtered)
					_trending.emit(currentList)
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load more trending")
			} finally {
				isLoadingMoreTrending = false
			}
		}
	}

	fun loadNextTrendingMoviesPage() {
		if (isLoadingMoreTrendingMovies) return
		viewModelScope.launch {
			isLoadingMoreTrendingMovies = true
			try {
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
				trendingMoviesCurrentPage++
				val offset = (trendingMoviesCurrentPage - 1) * itemsPerPage
				val result = jellyseerrRepository.getTrendingMovies(limit = itemsPerPage, offset = offset)
				
				if (result.isSuccess) {
					val newItems = result.getOrNull()?.results ?: emptyList()
					val filtered =
						newItems
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "movie" }
							.filterNsfw(blockNsfw)
					val currentList = _trendingMovies.value.toMutableList()
					currentList.addAll(filtered)
					_trendingMovies.emit(currentList)
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load more trending movies")
			} finally {
				isLoadingMoreTrendingMovies = false
			}
		}
	}

	fun loadNextTrendingTvPage() {
		if (isLoadingMoreTrendingTv) return
		viewModelScope.launch {
			isLoadingMoreTrendingTv = true
			try {
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
				trendingTvCurrentPage++
				val offset = (trendingTvCurrentPage - 1) * itemsPerPage
				val result = jellyseerrRepository.getTrendingTv(limit = itemsPerPage, offset = offset)
				
				if (result.isSuccess) {
					val newItems = result.getOrNull()?.results ?: emptyList()
					val filtered =
						newItems
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "tv" }
							.filterNsfw(blockNsfw)
					val currentList = _trendingTv.value.toMutableList()
					currentList.addAll(filtered)
					_trendingTv.emit(currentList)
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load more trending TV")
			} finally {
				isLoadingMoreTrendingTv = false
			}
		}
	}

	fun loadNextUpcomingMoviesPage() {
		if (isLoadingMoreUpcomingMovies) return
		viewModelScope.launch {
			isLoadingMoreUpcomingMovies = true
			try {
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
				upcomingMoviesCurrentPage++
				val offset = (upcomingMoviesCurrentPage - 1) * itemsPerPage
				val result = jellyseerrRepository.getUpcomingMovies(limit = itemsPerPage, offset = offset)
				
				if (result.isSuccess) {
					val newItems = result.getOrNull()?.results ?: emptyList()
					val filtered =
						newItems
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "movie" }
							.filterNsfw(blockNsfw)
					val currentList = _upcomingMovies.value.toMutableList()
					currentList.addAll(filtered)
					_upcomingMovies.emit(currentList)
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load more upcoming movies")
			} finally {
				isLoadingMoreUpcomingMovies = false
			}
		}
	}

	fun loadNextUpcomingTvPage() {
		if (isLoadingMoreUpcomingTv) return
		viewModelScope.launch {
			isLoadingMoreUpcomingTv = true
			try {
				val prefs = getPreferences()
				val itemsPerPage = prefs?.get(JellyseerrPreferences.fetchLimit)?.limit ?: JellyseerrFetchLimit.MEDIUM.limit
				val blockNsfw = prefs?.get(JellyseerrPreferences.blockNsfw) ?: false
				upcomingTvCurrentPage++
				val offset = (upcomingTvCurrentPage - 1) * itemsPerPage
				val result = jellyseerrRepository.getUpcomingTv(limit = itemsPerPage, offset = offset)
				
				if (result.isSuccess) {
					val newItems = result.getOrNull()?.results ?: emptyList()
					val filtered =
						newItems
							.filterNot { it.isAvailable() }
							.filterNot { it.isBlacklisted() }
							.filter { (it.mediaType ?: "").lowercase() == "tv" }
							.filterNsfw(blockNsfw)
					val currentList = _upcomingTv.value.toMutableList()
					currentList.addAll(filtered)
					_upcomingTv.emit(currentList)
				}
			} catch (error: Exception) {
				Timber.e(error, "Failed to load more upcoming TV")
			} finally {
				isLoadingMoreUpcomingTv = false
			}
		}
	}

	suspend fun discoverMovies(
		page: Int = 1,
		sortBy: String = "popularity.desc",
		genreId: String? = null,
		studioId: String? = null,
		keywords: String? = null,
		language: String = "en",
	) = jellyseerrRepository.discoverMovies(
		page = page,
		sortBy = sortBy,
		genre = genreId?.toIntOrNull(),
		studio = studioId?.toIntOrNull(),
		keywords = keywords?.toIntOrNull(),
		language = language,
	)

	suspend fun discoverTv(
		page: Int = 1,
		sortBy: String = "popularity.desc",
		genreId: String? = null,
		networkId: String? = null,
		keywords: String? = null,
		language: String = "en",
	) = jellyseerrRepository.discoverTv(
		page = page,
		sortBy = sortBy,
		genre = genreId?.toIntOrNull(),
		network = networkId?.toIntOrNull(),
		keywords = keywords?.toIntOrNull(),
		language = language,
	)
}
