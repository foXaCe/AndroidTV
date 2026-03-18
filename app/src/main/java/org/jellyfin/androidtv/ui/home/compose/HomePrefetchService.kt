package org.jellyfin.androidtv.ui.home.compose

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
 * for the most important sections (Continue Watching + Next Up merged).
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

						// Merge into single row with dedup by itemId + seriesId
						val resumeItems = resume?.items.orEmpty()
						val nextUpItems = nextUp?.items.orEmpty()
						val resumeIds = resumeItems.map { it.id }.toSet()
						val resumeSeriesIds = resumeItems.mapNotNull { it.seriesId }.toSet()
						val dedupedNextUp =
							nextUpItems
								.filter { it.id !in resumeIds }
								.filter { item ->
									val sid = item.seriesId
									sid == null || sid !in resumeSeriesIds
								}
						val merged = resumeItems + dedupedNextUp
						if (merged.isNotEmpty()) {
							listOf(TvRow(title = application.getString(R.string.home_section_resume), items = merged, key = "continue_watching"))
						} else {
							emptyList()
						}
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
	 * Discard any in-flight prefetch without waiting. Call when cache was already displayed.
	 */
	fun discard() {
		_prefetchedRows.value = null
		scope?.cancel()
		scope = null
	}

	/**
	 * Consume prefetched data, waiting up to [CONSUME_TIMEOUT] for in-flight prefetch to complete.
	 * Data is cleared after consumption to avoid stale data on re-load.
	 */
	suspend fun consume(): List<TvRow<BaseItemDto>>? {
		// If data is already available, return immediately
		val immediate = _prefetchedRows.value
		if (immediate != null) {
			Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH consume: immediate hit")
			_prefetchedRows.value = null
			scope?.cancel()
			scope = null
			return immediate
		}

		// Wait up to CONSUME_TIMEOUT for in-flight prefetch to complete
		if (scope != null) {
			val t0 = System.currentTimeMillis()
			val data =
				withTimeoutOrNull(CONSUME_TIMEOUT) {
					_prefetchedRows.filterNotNull().first()
				}
			val t1 = System.currentTimeMillis()
			Timber.tag("VFX_PERF_PREFETCH").d("VFX_PERF_PREFETCH consume: waited ${t1 - t0}ms, data=${data != null}")
			_prefetchedRows.value = null
			scope?.cancel()
			scope = null
			return data
		}

		return null
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
		private val CONSUME_TIMEOUT = 1500L // ms — short: prefer cache over waiting
	}
}
