package org.jellyfin.androidtv.ui.home.compose

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.base.state.toUiError
import org.jellyfin.androidtv.ui.base.tv.TvRow
import org.jellyfin.androidtv.ui.home.mediabar.SponsorBlockApi
import org.jellyfin.androidtv.ui.home.mediabar.TrailerResolver
import org.jellyfin.androidtv.ui.home.mediabar.YouTubeStreamResolver
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.liveTvApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.SortOrder
import org.jellyfin.sdk.model.api.request.GetNextUpRequest
import org.jellyfin.sdk.model.api.request.GetResumeItemsRequest
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@Stable
data class TrailerState(
	val isCountingDown: Boolean = false,
	val isPlaying: Boolean = false,
	val isMuted: Boolean = true,
	val streamInfo: YouTubeStreamResolver.StreamInfo? = null,
	val startSeconds: Double = 0.0,
	val segments: List<SponsorBlockApi.Segment> = emptyList(),
)

@Stable
data class HomeUiState(
	val isLoading: Boolean = true,
	val isRefreshing: Boolean = false,
	val error: UiError? = null,
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class HomeViewModel(
	val api: ApiClient,
	private val application: Application,
	private val userRepository: UserRepository,
	private val userViewsRepository: UserViewsRepository,
	private val userPreferences: UserPreferences,
	private val userSettingPreferences: UserSettingPreferences,
	private val homePrefetchService: HomePrefetchService,
	private val homeRowsCache: HomeRowsCache,
) : ViewModel() {
	private val _uiState = MutableStateFlow(HomeUiState())
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

	private val _focusedItem = MutableStateFlow<BaseItemDto?>(null)
	val focusedItem: StateFlow<BaseItemDto?> = _focusedItem.asStateFlow()

	private val _trailerState = MutableStateFlow(TrailerState())
	val trailerState: StateFlow<TrailerState> = _trailerState.asStateFlow()

	private var trailerJob: Job? = null

	private val t0 = System.currentTimeMillis()

	init {
		Timber.tag("VFX_PERF").d("VFX_PERF T0 HomeViewModel init at $t0")
		loadRows()
	}

	fun setFocusedItem(item: BaseItemDto) {
		_focusedItem.value = item
		startTrailerCountdown(item)
	}

	fun stopTrailer() {
		trailerJob?.cancel()
		trailerJob = null
		_trailerState.value = TrailerState()
	}

	fun toggleMute() {
		_trailerState.update { it.copy(isMuted = !it.isMuted) }
	}

	private fun startTrailerCountdown(item: BaseItemDto) {
		trailerJob?.cancel()
		_trailerState.value = TrailerState()

		val trailers = item.remoteTrailers.orEmpty()
		if (trailers.isEmpty()) return

		// Quick check: at least one YouTube trailer exists (instant, no I/O)
		val hasYoutubeTrailer =
			trailers.any { trailer ->
				trailer.url?.let { TrailerResolver.extractYoutubeVideoId(it) } != null
			}
		if (!hasYoutubeTrailer) return

		trailerJob =
			viewModelScope.launch {
				_trailerState.value = TrailerState(isCountingDown = true)

				// Start resolution in parallel with countdown
				val resolveDeferred =
					async(Dispatchers.IO) {
						try {
							TrailerResolver.resolveTrailerFromItem(item)
						} catch (e: Exception) {
							Timber.w(e, "TrailerResolver: Failed to resolve trailer for hero")
							null
						}
					}

				// 5-second countdown
				delay(TRAILER_COUNTDOWN_MS)

				// Wait for resolution result (may already be done)
				val preview = resolveDeferred.await()
				if (preview?.streamInfo == null) {
					Timber.d("TrailerResolver: No stream resolved, cancelling hero trailer")
					_trailerState.value = TrailerState()
					return@launch
				}

				Timber.d("TrailerResolver: Hero trailer ready — starting playback")
				_trailerState.value =
					TrailerState(
						isCountingDown = false,
						isPlaying = true,
						isMuted = true,
						streamInfo = preview.streamInfo,
						startSeconds = preview.startSeconds,
						segments = preview.segments,
					)
			}
	}

	fun invalidateCache() {
		homeRowsCache.invalidate()
	}

	fun loadRows() {
		// Check for prefetched data — show immediately while loading the rest
		val prefetched = homePrefetchService.consume()
		val t1 = System.currentTimeMillis()
		Timber.tag("VFX_PERF").d("VFX_PERF T0→T1 prefetch consume: ${t1 - t0}ms (data=${prefetched != null})")

		if (prefetched != null) {
			_uiState.update { it.copy(isLoading = false, rows = prefetched) }
			if (_focusedItem.value == null) {
				prefetched
					.firstOrNull()
					?.items
					?.firstOrNull()
					?.let { _focusedItem.value = it }
			}
		}

		viewModelScope.launch {
			// Load cache on IO thread (fast: slimmed data, ~50-100KB)
			if (prefetched == null) {
				val cacheResult = withContext(Dispatchers.IO) { homeRowsCache.load() }
				if (cacheResult != null) {
					val (cachedRows, cacheTime) = cacheResult
					val cacheAge = System.currentTimeMillis() - cacheTime
					val isFresh = cacheAge < HomeRowsCache.FRESH_DURATION_MS
					val tCacheHit = System.currentTimeMillis()
					Timber.tag("VFX_PERF").d("VFX_PERF T_cache_hit: ${tCacheHit - t0}ms (age=${cacheAge}ms, fresh=$isFresh, rows=${cachedRows.size})")
					_uiState.update { it.copy(isLoading = false, isRefreshing = !isFresh, rows = cachedRows) }
					if (_focusedItem.value == null) {
						cachedRows
							.firstOrNull()
							?.items
							?.firstOrNull()
							?.let { _focusedItem.value = it }
					}
				} else {
					_uiState.update { it.copy(isLoading = true, error = null) }
				}
			}

			val t2 = System.currentTimeMillis()
			Timber.tag("VFX_PERF").d("VFX_PERF T1→T2 loadRows coroutine start: ${t2 - t1}ms")
			try {
				val currentUser =
					withTimeout(30.seconds) {
						userRepository.currentUser.filterNotNull().first()
					}
				val homesections = userSettingPreferences.activeHomesections

				// Pre-fetch views if needed (for latest media)
				val viewsDeferred =
					if (homesections.contains(HomeSectionType.LATEST_MEDIA)) {
						async(Dispatchers.IO) { userViewsRepository.views.first() }
					} else {
						null
					}

				// Pre-check live TV access
				val liveTvDeferred =
					if (homesections.contains(HomeSectionType.LIVE_TV) &&
						currentUser.policy?.enableLiveTvAccess == true
					) {
						async(Dispatchers.IO) {
							try {
								val programs by api.liveTvApi.getRecommendedPrograms(
									enableTotalRecordCount = false,
									imageTypeLimit = 1,
									isAiring = true,
									limit = 1,
								)
								programs.items.isNotEmpty()
							} catch (e: Exception) {
								Timber.w(e, "Failed to check live TV")
								false
							}
						}
					} else {
						null
					}

				// Determine merged continue watching mode
				val mergeContinueWatching = userPreferences[UserPreferences.mergeContinueWatchingNextUp]

				// Load all sections in parallel — emit progressively
				// NOTE: viewsDeferred and liveTvDeferred are NOT awaited here.
				// Sections that need them (LATEST_MEDIA, LIVE_TV) await individually,
				// so independent sections (RESUME, NEXT_UP) start immediately.
				withContext(Dispatchers.IO) {
					val deferredRows = mutableListOf<kotlinx.coroutines.Deferred<List<TvRow<BaseItemDto>>>>()
					var mergedRowAdded = false

					for (section in homesections) {
						val deferred =
							async {
								try {
									when (section) {
										HomeSectionType.MEDIA_BAR -> emptyList()
										HomeSectionType.RESUME -> {
											if (mergeContinueWatching && !mergedRowAdded) {
												mergedRowAdded = true
												loadMergedContinueWatching()
											} else if (!mergeContinueWatching) {
												loadContinueWatching()
											} else {
												emptyList()
											}
										}
										HomeSectionType.NEXT_UP -> {
											if (!mergeContinueWatching) {
												loadNextUp()
											} else if (!mergedRowAdded) {
												mergedRowAdded = true
												loadMergedContinueWatching()
											} else {
												emptyList()
											}
										}
										HomeSectionType.LATEST_MEDIA -> {
											val tViewsStart = System.currentTimeMillis()
											val views = viewsDeferred?.await() ?: emptyList()
											val tViewsEnd = System.currentTimeMillis()
											Timber.tag("VFX_PERF").d("VFX_PERF T_views = ${tViewsEnd - tViewsStart}ms")
											loadLatestMedia(views)
										}
										HomeSectionType.RECENTLY_RELEASED -> loadRecentlyReleased()
										HomeSectionType.LIBRARY_TILES_SMALL,
										HomeSectionType.LIBRARY_BUTTONS,
										-> loadLibraryViews()
										HomeSectionType.RESUME_AUDIO -> loadResumeAudio()
										HomeSectionType.RESUME_BOOK -> emptyList()
										HomeSectionType.ACTIVE_RECORDINGS -> loadActiveRecordings()
										HomeSectionType.LIVE_TV -> {
											val tLiveTvStart = System.currentTimeMillis()
											val hasLiveTv = liveTvDeferred?.await() ?: false
											val tLiveTvEnd = System.currentTimeMillis()
											Timber.tag("VFX_PERF").d("VFX_PERF T_livetv = ${tLiveTvEnd - tLiveTvStart}ms")
											if (hasLiveTv) loadLiveTv() else emptyList()
										}
										HomeSectionType.PLAYLISTS -> loadPlaylists()
										HomeSectionType.NONE -> emptyList()
									}
								} catch (e: Exception) {
									Timber.w(e, "Failed to load section $section")
									emptyList()
								}
							}
						deferredRows.add(deferred)
					}

					// If we already have rows displayed (cache/prefetch), wait for ALL
					// network rows before replacing — avoids flash of partial content.
					// Otherwise, emit progressively so user sees content ASAP.
					val hasExistingRows = _uiState.value.rows.isNotEmpty()
					val progressiveRows = mutableListOf<TvRow<BaseItemDto>>()
					var rowIndex = 0

					for (deferred in deferredRows) {
						val sectionRows = deferred.await()
						if (sectionRows.isNotEmpty()) {
							progressiveRows.addAll(sectionRows)

							if (!hasExistingRows) {
								// No cached data — show rows progressively
								_uiState.update { it.copy(isLoading = false, rows = progressiveRows.toList()) }
							}

							// Set initial focused item from first available row
							if (_focusedItem.value == null) {
								sectionRows
									.firstOrNull()
									?.items
									?.firstOrNull()
									?.let { _focusedItem.value = it }
							}

							// Perf logging per row
							sectionRows.forEach { row ->
								val tNow = System.currentTimeMillis()
								if (rowIndex == 0) {
									Timber.tag("VFX_PERF").d("VFX_PERF T_row_0 first row visible: ${tNow - t2}ms")
								}
								Timber.tag("VFX_PERF").d("VFX_PERF   row[$rowIndex] key=${row.key} items=${row.items.size} at +${tNow - t2}ms")
								rowIndex++
							}
						}
					}

					// If we had cached rows, replace them all at once now
					if (hasExistingRows && progressiveRows.isNotEmpty()) {
						_uiState.update { it.copy(isLoading = false, rows = progressiveRows.toList()) }
					}

					val t3 = System.currentTimeMillis()
					val totalItems = progressiveRows.sumOf { it.items.size }
					Timber.tag("VFX_PERF").d("VFX_PERF T_row_last all rows complete: ${t3 - t2}ms ($rowIndex rows, $totalItems items)")
					Timber.tag("VFX_PERF").d("VFX_PERF T0→T3 total: ${t3 - t0}ms")

					// Save to local cache for next cold start
					if (progressiveRows.isNotEmpty()) {
						homeRowsCache.save(progressiveRows.toList())
						val tSave = System.currentTimeMillis()
						Timber.tag("VFX_PERF").d("VFX_PERF T_cache_save: ${tSave - t3}ms")
					}

					// Clear refreshing indicator
					_uiState.update { it.copy(isRefreshing = false) }

					// Handle case where all sections returned empty
					if (progressiveRows.isEmpty()) {
						_uiState.update { it.copy(isLoading = false) }
					}
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to load home rows")
				// Only show error if we don't already have cached/prefetched data
				if (_uiState.value.rows.isEmpty()) {
					_uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.toUiError()) }
				} else {
					_uiState.update { it.copy(isRefreshing = false) }
				}
			}
		}
	}

	private suspend fun loadContinueWatching(): List<TvRow<BaseItemDto>> {
		val query =
			GetResumeItemsRequest(
				limit = ROW_MAX_ITEMS,
				fields = ItemRepository.itemFields,
				imageTypeLimit = 1,
				enableTotalRecordCount = false,
				mediaTypes = listOf(MediaType.VIDEO),
				excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
			)
		val items =
			api.itemsApi
				.getResumeItems(query)
				.content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_resume), items = items, key = "resume"))
	}

	private suspend fun loadNextUp(): List<TvRow<BaseItemDto>> {
		val query =
			GetNextUpRequest(
				imageTypeLimit = 1,
				limit = ROW_MAX_ITEMS,
				enableResumable = false,
				fields = ItemRepository.itemFields,
			)
		val items =
			api.tvShowsApi
				.getNextUp(query)
				.content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_next_up), items = items, key = "nextup"))
	}

	private suspend fun loadMergedContinueWatching(): List<TvRow<BaseItemDto>> =
		coroutineScope {
			val resumeDeferred =
				async {
					val query =
						GetResumeItemsRequest(
							limit = ROW_MAX_ITEMS,
							fields = ItemRepository.itemFields,
							imageTypeLimit = 1,
							enableTotalRecordCount = false,
							mediaTypes = listOf(MediaType.VIDEO),
							excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
						)
					api.itemsApi
						.getResumeItems(query)
						.content.items
				}
			val nextUpDeferred =
				async {
					val query =
						GetNextUpRequest(
							imageTypeLimit = 1,
							limit = ROW_MAX_ITEMS,
							enableResumable = false,
							fields = ItemRepository.itemFields,
						)
					api.tvShowsApi
						.getNextUp(query)
						.content.items
				}

			val resumeItems = resumeDeferred.await()
			val nextUpItems = nextUpDeferred.await()

			// Merge: resume items first, then next up items not already in resume
			val resumeIds = resumeItems.map { it.id }.toSet()
			val merged = resumeItems + nextUpItems.filter { it.id !in resumeIds }

			if (merged.isEmpty()) return@coroutineScope emptyList()
			listOf(TvRow(title = application.getString(R.string.home_section_resume), items = merged, key = "continue_watching"))
		}

	private suspend fun loadLatestMedia(userViews: Collection<BaseItemDto>): List<TvRow<BaseItemDto>> {
		val configuration = userRepository.currentUser.value?.configuration
		val excludes = configuration?.latestItemsExcludes.orEmpty()

		val eligibleViews =
			userViews.filter { view ->
				view.collectionType !in EXCLUDED_COLLECTION_TYPES && view.id !in excludes
			}

		return coroutineScope {
			eligibleViews
				.map { view ->
					async {
						try {
							val items by api.userLibraryApi.getLatestMedia(
								fields = ItemRepository.itemFields,
								imageTypeLimit = 1,
								parentId = view.id,
								groupItems = true,
								limit = ROW_CHUNK_SIZE,
							)
							if (items.isEmpty()) {
								null
							} else {
								TvRow(
									title = application.getString(R.string.home_section_latest, view.name),
									items = items,
									key = "latest_${view.id}",
								)
							}
						} catch (e: Exception) {
							Timber.w(e, "Failed to load latest for ${view.name}")
							null
						}
					}
				}.mapNotNull { it.await() }
		}
	}

	private suspend fun loadRecentlyReleased(): List<TvRow<BaseItemDto>> {
		val response by api.itemsApi.getItems(
			fields = ItemRepository.itemFields,
			includeItemTypes = setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
			sortBy = setOf(ItemSortBy.PREMIERE_DATE),
			sortOrder = setOf(SortOrder.DESCENDING),
			recursive = true,
			imageTypeLimit = 1,
			enableTotalRecordCount = false,
			limit = ROW_CHUNK_SIZE,
		)
		val items = response.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_recently_released), items = items, key = "recently_released"))
	}

	private suspend fun loadLibraryViews(): List<TvRow<BaseItemDto>> {
		val views = userViewsRepository.views.first()
		if (views.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_my_media), items = views.toList(), key = "libraries"))
	}

	private suspend fun loadResumeAudio(): List<TvRow<BaseItemDto>> {
		val query =
			GetResumeItemsRequest(
				limit = ROW_MAX_ITEMS,
				fields = ItemRepository.itemFields,
				imageTypeLimit = 1,
				enableTotalRecordCount = false,
				mediaTypes = listOf(MediaType.AUDIO),
				excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
			)
		val items =
			api.itemsApi
				.getResumeItems(query)
				.content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_resume_audio), items = items, key = "resume_audio"))
	}

	private suspend fun loadActiveRecordings(): List<TvRow<BaseItemDto>> {
		val response by api.liveTvApi.getRecordings(
			fields = ItemRepository.itemFields,
			enableImages = true,
			limit = ROW_MAX_ITEMS,
		)
		val items = response.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_recordings), items = items, key = "recordings"))
	}

	private suspend fun loadLiveTv(): List<TvRow<BaseItemDto>> {
		val response by api.liveTvApi.getRecommendedPrograms(
			isAiring = true,
			fields = ItemRepository.itemFields,
			imageTypeLimit = 1,
			enableTotalRecordCount = false,
			limit = ROW_MAX_ITEMS,
		)
		val items = response.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_live_tv), items = items, key = "live_tv"))
	}

	private suspend fun loadPlaylists(): List<TvRow<BaseItemDto>> {
		val response by api.itemsApi.getItems(
			includeItemTypes = setOf(BaseItemKind.PLAYLIST),
			recursive = true,
			sortBy = setOf(ItemSortBy.DATE_CREATED),
			sortOrder = setOf(SortOrder.DESCENDING),
			fields = ItemRepository.itemFields + ItemFields.CAN_DELETE,
			imageTypeLimit = 1,
			limit = ROW_CHUNK_SIZE,
		)
		val items = response.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = application.getString(R.string.home_section_playlists), items = items, key = "playlists"))
	}

	companion object {
		private const val ROW_CHUNK_SIZE = 15
		private const val ROW_MAX_ITEMS = 50
		private const val TRAILER_COUNTDOWN_MS = 5_000L

		private val EXCLUDED_COLLECTION_TYPES =
			arrayOf(
				CollectionType.PLAYLISTS,
				CollectionType.LIVETV,
				CollectionType.BOXSETS,
				CollectionType.BOOKS,
			)
	}
}
