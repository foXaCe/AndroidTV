package org.jellyfin.androidtv.ui.home.compose

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.ui.base.tv.TvRow
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber
import java.io.File

@Serializable
private data class CachedRow(
	val title: String,
	val key: String,
	val items: List<BaseItemDto>,
)

@Serializable
private data class CachedHomeData(
	val timestamp: Long,
	val rows: List<CachedRow>,
)

/**
 * Local disk cache for home screen rows.
 * Stores slimmed-down [BaseItemDto] data as JSON in a file so the home screen
 * can display content instantly on cold start while the network refreshes.
 *
 * Heavy fields (mediaSources, chapters, people, etc.) are stripped before
 * serialization to keep the cache small and fast to deserialize (~50-100KB
 * instead of 1.7MB).
 */
class HomeRowsCache(
	context: Context,
) {
	private val cacheFile = File(context.filesDir, CACHE_FILE_NAME)

	private val json =
		Json {
			ignoreUnknownKeys = true
			encodeDefaults = false
		}

	fun save(rows: List<TvRow<BaseItemDto>>) {
		val data =
			CachedHomeData(
				timestamp = System.currentTimeMillis(),
				rows =
					rows.map { row ->
						CachedRow(
							title = row.title,
							key = row.key,
							items = row.items.take(CACHED_ITEMS_PER_ROW).map { it.slimForCache() },
						)
					},
			)
		try {
			val encoded = json.encodeToString(data)
			cacheFile.writeText(encoded)
			Timber.tag(TAG).d("Saved ${rows.size} rows (${encoded.length} chars)")
		} catch (e: Exception) {
			Timber.tag(TAG).w(e, "Failed to save home cache")
		}
	}

	/**
	 * Load cached rows. Returns pair of (rows, cacheTimestamp) or null if no cache.
	 */
	fun load(): Pair<List<TvRow<BaseItemDto>>, Long>? {
		if (!cacheFile.exists()) return null
		return try {
			val raw = cacheFile.readText()
			val data = json.decodeFromString<CachedHomeData>(raw)
			val rows = data.rows.map { TvRow(title = it.title, items = it.items, key = it.key) }
			rows to data.timestamp
		} catch (e: Exception) {
			Timber.tag(TAG).w(e, "Failed to deserialize home cache")
			invalidate()
			null
		}
	}

	fun invalidate() {
		cacheFile.delete()
		Timber.tag(TAG).d("Cache invalidated")
	}

	companion object {
		private const val TAG = "HomeRowsCache"
		private const val CACHE_FILE_NAME = "home_rows_cache.json"
		const val FRESH_DURATION_MS = 5 * 60 * 1000L // 5 minutes
		private const val CACHED_ITEMS_PER_ROW = 8 // Only cache visible items
	}
}

/**
 * Strip heavy fields from [BaseItemDto] that are not needed for home screen display.
 * Keeps: identity, images, user data, basic metadata for cards and hero backdrop.
 * Strips: mediaSources, chapters, people, studios, tags, etc.
 */
private fun BaseItemDto.slimForCache(): BaseItemDto =
	copy(
		mediaSources = null,
		mediaStreams = null,
		chapters = null,
		people = null,
		remoteTrailers = null,
		tags = null,
		studios = null,
		genreItems = null,
		artistItems = null,
		albumArtists = null,
		mediaType = mediaType, // keep
		lockedFields = null,
		providerIds = null,
		externalUrls = null,
	)
