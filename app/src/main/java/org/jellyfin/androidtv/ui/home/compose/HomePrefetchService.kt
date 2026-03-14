package org.jellyfin.androidtv.ui.home.compose

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.ui.base.tv.TvRow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.request.GetNextUpRequest
import org.jellyfin.sdk.model.api.request.GetResumeItemsRequest
import timber.log.Timber

/**
 * Prefetches priority home rows during session start, before HomeViewModel is created.
 * This allows the Home screen to display content immediately without loading skeletons
 * for the most important sections (Continue Watching, Next Up).
 */
class HomePrefetchService(
	private val application: Application,
	private val api: ApiClient,
) {
	private var scope: CoroutineScope? = null
	private val _prefetchedRows = MutableStateFlow<List<TvRow<BaseItemDto>>?>(null)

	/**
	 * Start prefetching priority home rows in background.
	 * Called from onSessionStart() before MainActivity is launched.
	 */
	fun prefetch() {
		scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
		scope?.launch {
			val tp0 = System.currentTimeMillis()
			Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH TP0 prefetch start at $tp0")
			try {
				val rows =
					coroutineScope {
						val resumeDeferred = async { loadContinueWatching() }
						val nextUpDeferred = async { loadNextUp() }

						val resume = resumeDeferred.await()
						val tp1 = System.currentTimeMillis()
						Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH TP0→TP1 continueWatching: ${tp1 - tp0}ms (items=${resume?.items?.size ?: 0})")

						val nextUp = nextUpDeferred.await()
						val tp2 = System.currentTimeMillis()
						Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH TP1→TP2 nextUp: ${tp2 - tp1}ms (items=${nextUp?.items?.size ?: 0})")
						Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH TP0→TP2 total: ${tp2 - tp0}ms")

						listOfNotNull(resume, nextUp)
					}

				if (rows.isNotEmpty()) {
					_prefetchedRows.value = rows
				}
			} catch (e: Exception) {
				Timber.w(e, "Home prefetch failed")
			}
		}
	}

	/**
	 * Consume prefetched data. Returns null if no data available yet.
	 * Data is cleared after consumption to avoid stale data on re-load.
	 */
	fun consume(): List<TvRow<BaseItemDto>>? {
		val data = _prefetchedRows.value
		_prefetchedRows.value = null
		scope?.cancel()
		scope = null
		return data
	}

	private suspend fun loadContinueWatching(): TvRow<BaseItemDto>? {
		val items =
			api.itemsApi
				.getResumeItems(
					GetResumeItemsRequest(
						limit = ROW_MAX_ITEMS,
						fields = ItemRepository.itemFields,
						imageTypeLimit = 1,
						enableTotalRecordCount = false,
						mediaTypes = listOf(MediaType.VIDEO),
						excludeItemTypes = setOf(BaseItemKind.AUDIO_BOOK),
					),
				).content.items
		return if (items.isNotEmpty()) TvRow(title = application.getString(R.string.home_section_resume), items = items, key = "resume") else null
	}

	private suspend fun loadNextUp(): TvRow<BaseItemDto>? {
		val items =
			api.tvShowsApi
				.getNextUp(
					GetNextUpRequest(
						imageTypeLimit = 1,
						limit = ROW_MAX_ITEMS,
						enableResumable = false,
						fields = ItemRepository.itemFields,
					),
				).content.items
		return if (items.isNotEmpty()) {
			TvRow(
				title = application.getString(R.string.home_section_next_up),
				items = items,
				key = "nextup",
			)
		} else {
			null
		}
	}

	companion object {
		private const val ROW_MAX_ITEMS = 50
	}
}
