package org.jellyfin.androidtv.ui.home.compose

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.HomeSectionType
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.base.state.toUiError
import org.jellyfin.androidtv.ui.base.tv.TvRow
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
data class HomeUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class HomeViewModel(
	val api: ApiClient,
	private val userRepository: UserRepository,
	private val userViewsRepository: UserViewsRepository,
	private val userPreferences: UserPreferences,
	private val userSettingPreferences: UserSettingPreferences,
	private val homePrefetchService: HomePrefetchService,
) : ViewModel() {

	private val _uiState = MutableStateFlow(HomeUiState())
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

	private val _focusedItem = MutableStateFlow<BaseItemDto?>(null)
	val focusedItem: StateFlow<BaseItemDto?> = _focusedItem.asStateFlow()

	init {
		Log.d("STARTUP", "HomeViewModel init: ${System.currentTimeMillis()}")
		loadRows()
	}

	fun setFocusedItem(item: BaseItemDto) {
		_focusedItem.value = item
	}

	fun loadRows() {
		// Check for prefetched data — show immediately while loading the rest
		val prefetched = homePrefetchService.consume()
		if (prefetched != null) {
			Log.d("STARTUP", "Using prefetched rows (${prefetched.size}): ${System.currentTimeMillis()}")
			_uiState.update { it.copy(isLoading = false, rows = prefetched) }
			// Set initial focused item from prefetched data
			if (_focusedItem.value == null) {
				prefetched.firstOrNull()?.items?.firstOrNull()?.let { _focusedItem.value = it }
			}
		} else {
			_uiState.update { it.copy(isLoading = true, error = null) }
		}

		viewModelScope.launch {
			try {
				val currentUser = withTimeout(30.seconds) {
					userRepository.currentUser.filterNotNull().first()
				}
				val homesections = userSettingPreferences.activeHomesections

				// Pre-fetch views if needed (for latest media)
				val viewsDeferred = if (homesections.contains(HomeSectionType.LATEST_MEDIA)) {
					async(Dispatchers.IO) { userViewsRepository.views.first() }
				} else null

				// Pre-check live TV access
				val liveTvDeferred = if (homesections.contains(HomeSectionType.LIVE_TV) &&
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
				} else null

				val views = viewsDeferred?.await()
				val hasLiveTv = liveTvDeferred?.await() ?: false

				// Determine merged continue watching mode
				val mergeContinueWatching = userPreferences[UserPreferences.mergeContinueWatchingNextUp]

				// Load all sections in parallel
				val rows = withContext(Dispatchers.IO) {
					val deferredRows = mutableListOf<kotlinx.coroutines.Deferred<List<TvRow<BaseItemDto>>>>()
					var mergedRowAdded = false

					for (section in homesections) {
						val deferred = async {
							try {
								when (section) {
									HomeSectionType.MEDIA_BAR -> emptyList()
									HomeSectionType.RESUME -> {
										if (mergeContinueWatching && !mergedRowAdded) {
											mergedRowAdded = true
											loadMergedContinueWatching()
										} else if (!mergeContinueWatching) {
											loadContinueWatching()
										} else emptyList()
									}
									HomeSectionType.NEXT_UP -> {
										if (!mergeContinueWatching) {
											loadNextUp()
										} else if (!mergedRowAdded) {
											mergedRowAdded = true
											loadMergedContinueWatching()
										} else emptyList()
									}
									HomeSectionType.LATEST_MEDIA -> loadLatestMedia(views ?: emptyList())
									HomeSectionType.RECENTLY_RELEASED -> loadRecentlyReleased()
									HomeSectionType.LIBRARY_TILES_SMALL,
									HomeSectionType.LIBRARY_BUTTONS -> loadLibraryViews()
									HomeSectionType.RESUME_AUDIO -> loadResumeAudio()
									HomeSectionType.RESUME_BOOK -> emptyList()
									HomeSectionType.ACTIVE_RECORDINGS -> loadActiveRecordings()
									HomeSectionType.LIVE_TV -> if (hasLiveTv) loadLiveTv() else emptyList()
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

					deferredRows.flatMap { it.await() }
				}

				Log.d("STARTUP", "Full rows loaded (${rows.size}): ${System.currentTimeMillis()}")
				_uiState.update { it.copy(isLoading = false, rows = rows) }

				// Set initial focused item from first row
				if (_focusedItem.value == null) {
					rows.firstOrNull()?.items?.firstOrNull()?.let { _focusedItem.value = it }
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to load home rows")
				// Only show error if we don't already have prefetched data
				if (_uiState.value.rows.isEmpty()) {
					_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
				}
			}
		}
	}

	private suspend fun loadContinueWatching(): List<TvRow<BaseItemDto>> {
		val query = GetResumeItemsRequest(
			limit = ROW_MAX_ITEMS,
			fields = ItemRepository.itemFields,
			imageTypeLimit = 1,
			enableTotalRecordCount = false,
			mediaTypes = listOf(MediaType.VIDEO),
			excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
		)
		val items = api.itemsApi.getResumeItems(query).content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = "Continue Watching", items = items, key = "resume"))
	}

	private suspend fun loadNextUp(): List<TvRow<BaseItemDto>> {
		val query = GetNextUpRequest(
			imageTypeLimit = 1,
			limit = ROW_MAX_ITEMS,
			enableResumable = false,
			fields = ItemRepository.itemFields,
		)
		val items = api.tvShowsApi.getNextUp(query).content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = "Next Up", items = items, key = "nextup"))
	}

	private suspend fun loadMergedContinueWatching(): List<TvRow<BaseItemDto>> = coroutineScope {
		val resumeDeferred = async {
			val query = GetResumeItemsRequest(
				limit = ROW_MAX_ITEMS,
				fields = ItemRepository.itemFields,
				imageTypeLimit = 1,
				enableTotalRecordCount = false,
				mediaTypes = listOf(MediaType.VIDEO),
				excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
			)
			api.itemsApi.getResumeItems(query).content.items
		}
		val nextUpDeferred = async {
			val query = GetNextUpRequest(
				imageTypeLimit = 1,
				limit = ROW_MAX_ITEMS,
				enableResumable = false,
				fields = ItemRepository.itemFields,
			)
			api.tvShowsApi.getNextUp(query).content.items
		}

		val resumeItems = resumeDeferred.await()
		val nextUpItems = nextUpDeferred.await()

		// Merge: resume items first, then next up items not already in resume
		val resumeIds = resumeItems.map { it.id }.toSet()
		val merged = resumeItems + nextUpItems.filter { it.id !in resumeIds }

		if (merged.isEmpty()) return@coroutineScope emptyList()
		listOf(TvRow(title = "Continue Watching", items = merged, key = "continue_watching"))
	}

	private suspend fun loadLatestMedia(userViews: Collection<BaseItemDto>): List<TvRow<BaseItemDto>> {
		val configuration = userRepository.currentUser.value?.configuration
		val excludes = configuration?.latestItemsExcludes.orEmpty()

		val eligibleViews = userViews.filter { view ->
			view.collectionType !in EXCLUDED_COLLECTION_TYPES && view.id !in excludes
		}

		return coroutineScope {
			eligibleViews.map { view ->
				async {
					try {
						val items by api.userLibraryApi.getLatestMedia(
							fields = ItemRepository.itemFields,
							imageTypeLimit = 1,
							parentId = view.id,
							groupItems = true,
							limit = ROW_CHUNK_SIZE,
						)
						if (items.isEmpty()) null
						else TvRow(
							title = "Latest in ${view.name}",
							items = items,
							key = "latest_${view.id}",
						)
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
		return listOf(TvRow(title = "Recently Released", items = items, key = "recently_released"))
	}

	private suspend fun loadLibraryViews(): List<TvRow<BaseItemDto>> {
		val views = userViewsRepository.views.first()
		if (views.isEmpty()) return emptyList()
		return listOf(TvRow(title = "My Media", items = views.toList(), key = "libraries"))
	}

	private suspend fun loadResumeAudio(): List<TvRow<BaseItemDto>> {
		val query = GetResumeItemsRequest(
			limit = ROW_MAX_ITEMS,
			fields = ItemRepository.itemFields,
			imageTypeLimit = 1,
			enableTotalRecordCount = false,
			mediaTypes = listOf(MediaType.AUDIO),
			excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
		)
		val items = api.itemsApi.getResumeItems(query).content.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = "Continue Listening", items = items, key = "resume_audio"))
	}

	private suspend fun loadActiveRecordings(): List<TvRow<BaseItemDto>> {
		val response by api.liveTvApi.getRecordings(
			fields = ItemRepository.itemFields,
			enableImages = true,
			limit = ROW_MAX_ITEMS,
		)
		val items = response.items
		if (items.isEmpty()) return emptyList()
		return listOf(TvRow(title = "Recordings", items = items, key = "recordings"))
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
		return listOf(TvRow(title = "On Now", items = items, key = "live_tv"))
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
		return listOf(TvRow(title = "Playlists", items = items, key = "playlists"))
	}

	companion object {
		private const val ROW_CHUNK_SIZE = 15
		private const val ROW_MAX_ITEMS = 50

		private val EXCLUDED_COLLECTION_TYPES = arrayOf(
			CollectionType.PLAYLISTS,
			CollectionType.LIVETV,
			CollectionType.BOXSETS,
			CollectionType.BOOKS,
		)
	}
}
