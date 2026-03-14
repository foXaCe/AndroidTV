package org.jellyfin.androidtv.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jellyfin.androidtv.util.UUIDUtils
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID

/**
 * Response from the VegafoX plugin's `/VegafoX/Tmdb/EpisodeRating` endpoint.
 */
@Serializable
data class TmdbEpisodeResponse(
	val success: Boolean = false,
	val error: String? = null,
	val voteAverage: Float? = null,
	val voteCount: Int? = null,
	val name: String? = null,
	val airDate: String? = null,
	val seasonNumber: Int? = null,
	val episodeNumber: Int? = null,
	val stillPath: String? = null,
)

/**
 * Single episode entry within a season ratings response.
 */
@Serializable
data class TmdbSeasonEpisode(
	val success: Boolean = false,
	val voteAverage: Float? = null,
	val voteCount: Int? = null,
	val name: String? = null,
	val airDate: String? = null,
	val seasonNumber: Int? = null,
	val episodeNumber: Int? = null,
	val stillPath: String? = null,
)

/**
 * Response from the VegafoX plugin's `/VegafoX/Tmdb/SeasonRatings` endpoint.
 */
@Serializable
data class TmdbSeasonResponse(
	val success: Boolean = false,
	val error: String? = null,
	val seasonName: String? = null,
	val episodes: List<TmdbSeasonEpisode>? = null,
)

/**
 * Fetches TMDB episode ratings via the VegafoX server plugin proxy.
 *
 * Endpoints:
 * - `GET {serverUrl}/VegafoX/Tmdb/EpisodeRating?tmdbId={id}&season={n}&episode={n}`
 * - `GET {serverUrl}/VegafoX/Tmdb/SeasonRatings?tmdbId={id}&season={n}`
 *
 * Auth: `Authorization: MediaBrowser Token="{accessToken}"`
 *
 * The TMDB API key is managed server-side — no client-side key is needed.
 */
class TmdbRepository(
	private val okHttpClient: OkHttpClient,
	private val apiClient: ApiClient,
	private val apiClientFactory: ApiClientFactory,
) {
	private val episodeRatingsCache = mutableMapOf<String, Float>()
	private val seasonCache = mutableMapOf<String, Map<Int, Float>>()
	private val seriesTmdbIdCache = mutableMapOf<String, String?>()
	private val pendingRequests = mutableMapOf<String, CompletableDeferred<Float?>>()
	private val pendingSeasonRequests = mutableMapOf<String, CompletableDeferred<Map<Int, Float>?>>()
	private val seriesCommunityRatingCache = mutableMapOf<String, Float?>()

	private val json =
		Json {
			ignoreUnknownKeys = true
			isLenient = true
		}

	private fun getApiClientForItem(item: BaseItemDto) = resolveApiClient(item.serverId)

	/**
	 * Fetch individual episode rating from the VegafoX TMDB plugin proxy.
	 * Returns the vote_average (0-10 scale) or null.
	 */
	suspend fun getEpisodeRating(item: BaseItemDto): Float? =
		withContext(Dispatchers.IO) {
			if (item.type != BaseItemKind.EPISODE) return@withContext null

			val seriesId = item.seriesId
			if (seriesId == null) {
				Timber.w("Episode ${item.name} has no seriesId")
				return@withContext null
			}

			val tmdbId = getSeriesTmdbId(seriesId, item)
			if (tmdbId == null) {
				Timber.w("Could not get TMDB ID for series ${item.seriesName} ($seriesId)")
				return@withContext null
			}

			val seasonNumber = item.parentIndexNumber
			val episodeNumber = item.indexNumber
			if (seasonNumber == null || episodeNumber == null) {
				Timber.w("Episode ${item.name} missing season/episode numbers: S${seasonNumber}E$episodeNumber")
				return@withContext null
			}

			val cacheKey = "$tmdbId:$seasonNumber:$episodeNumber"

			episodeRatingsCache[cacheKey]?.let { return@withContext it }
			pendingRequests[cacheKey]?.let { return@withContext it.await() }

			val deferred = CompletableDeferred<Float?>()
			pendingRequests[cacheKey] = deferred

			try {
				val effectiveApi = getApiClientForItem(item)
				val baseUrl =
					effectiveApi.baseUrl ?: run {
						Timber.w("TmdbRepository: No server URL available")
						deferred.complete(null)
						pendingRequests.remove(cacheKey)
						return@withContext null
					}
				val accessToken =
					effectiveApi.accessToken ?: run {
						Timber.w("TmdbRepository: No access token available")
						deferred.complete(null)
						pendingRequests.remove(cacheKey)
						return@withContext null
					}

				val url = "$baseUrl/VegafoX/Tmdb/EpisodeRating?tmdbId=$tmdbId&season=$seasonNumber&episode=$episodeNumber"

				val request =
					Request
						.Builder()
						.url(url)
						.addHeader("Authorization", "MediaBrowser Token=\"$accessToken\"")
						.build()
				val response = okHttpClient.newCall(request).execute()

				val result =
					if (response.isSuccessful) {
						val body = response.body?.string()
						if (body != null) {
							try {
								val episodeResponse = json.decodeFromString<TmdbEpisodeResponse>(body)
								if (!episodeResponse.success || episodeResponse.error != null) {
									Timber.w("TmdbRepository: Plugin returned error: ${episodeResponse.error}")
									null
								} else {
									val rating = episodeResponse.voteAverage
									if (rating != null && rating > 0f) {
										episodeRatingsCache[cacheKey] = rating
										Timber.i("Cached TMDB episode rating for $cacheKey: $rating/10")
										rating
									} else {
										null
									}
								}
							} catch (e: Exception) {
								Timber.w(e, "Failed to parse TMDB episode response for $cacheKey")
								null
							}
						} else {
							null
						}
					} else {
						Timber.w("TMDB plugin request failed for episode $cacheKey: ${response.code} ${response.message}")
						null
					}
				deferred.complete(result)
				return@withContext result
			} catch (e: Exception) {
				Timber.e(e, "Error fetching TMDB episode rating for $cacheKey")
				deferred.complete(null)
				return@withContext null
			} finally {
				pendingRequests.remove(cacheKey)
			}
		}

	/**
	 * Fetch all episode ratings for a season from the VegafoX TMDB plugin proxy.
	 * Returns a map of episodeNumber → voteAverage, or null on failure.
	 */
	suspend fun getSeasonEpisodeRatings(
		seriesTmdbId: String,
		seasonNumber: Int,
		serverId: String? = null,
	): Map<Int, Float>? =
		withContext(Dispatchers.IO) {
			val cacheKey = "$seriesTmdbId:$seasonNumber"

			seasonCache[cacheKey]?.let { return@withContext it }
			pendingSeasonRequests[cacheKey]?.let { return@withContext it.await() }

			val deferred = CompletableDeferred<Map<Int, Float>?>()
			pendingSeasonRequests[cacheKey] = deferred

			try {
				val effectiveApi = resolveApiClient(serverId)
				val baseUrl =
					effectiveApi.baseUrl ?: run {
						deferred.complete(null)
						pendingSeasonRequests.remove(cacheKey)
						return@withContext null
					}
				val accessToken =
					effectiveApi.accessToken ?: run {
						deferred.complete(null)
						pendingSeasonRequests.remove(cacheKey)
						return@withContext null
					}

				val url = "$baseUrl/VegafoX/Tmdb/SeasonRatings?tmdbId=$seriesTmdbId&season=$seasonNumber"

				val request =
					Request
						.Builder()
						.url(url)
						.addHeader("Authorization", "MediaBrowser Token=\"$accessToken\"")
						.build()
				val response = okHttpClient.newCall(request).execute()

				val result =
					if (response.isSuccessful) {
						val body = response.body?.string()
						if (body != null) {
							try {
								val seasonResponse = json.decodeFromString<TmdbSeasonResponse>(body)
								if (!seasonResponse.success || seasonResponse.error != null) {
									Timber.w("TmdbRepository: Plugin returned error: ${seasonResponse.error}")
									null
								} else {
									val ratingsMap =
										seasonResponse.episodes
											?.filter { it.voteAverage != null && it.voteAverage > 0f && it.episodeNumber != null }
											?.associate { it.episodeNumber!! to it.voteAverage!! }
											?: emptyMap()

									if (ratingsMap.isNotEmpty()) {
										seasonCache[cacheKey] = ratingsMap
										ratingsMap.forEach { (epNum, rating) ->
											episodeRatingsCache["$seriesTmdbId:$seasonNumber:$epNum"] = rating
										}
									}
									ratingsMap
								}
							} catch (e: Exception) {
								Timber.w(e, "Failed to parse TMDB season response for $cacheKey")
								null
							}
						} else {
							null
						}
					} else {
						Timber.w("TMDB plugin request failed for season $cacheKey: ${response.code}")
						null
					}
				deferred.complete(result)
				return@withContext result
			} catch (e: Exception) {
				Timber.e(e, "Error fetching TMDB season ratings for $cacheKey")
				deferred.complete(null)
				return@withContext null
			} finally {
				pendingSeasonRequests.remove(cacheKey)
			}
		}

	/**
	 * Fetch the parent series' community rating for an episode.
	 * Returns the communityRating (0-10 scale) or null.
	 */
	suspend fun getSeriesCommunityRating(item: BaseItemDto): Float? =
		withContext(Dispatchers.IO) {
			if (item.type != BaseItemKind.EPISODE) return@withContext null
			val seriesId = item.seriesId ?: return@withContext null
			val cacheKey = seriesId.toString()

			seriesCommunityRatingCache[cacheKey]?.let { return@withContext it }

			try {
				val effectiveApi = getApiClientForItem(item)
				val response = effectiveApi.userLibraryApi.getItem(itemId = seriesId)
				val rating = response.content.communityRating
				seriesCommunityRatingCache[cacheKey] = rating
				rating
			} catch (e: Exception) {
				Timber.e(e, "Failed to fetch series community rating for seriesId: $seriesId")
				null
			}
		}

	private fun resolveApiClient(serverId: String?): ApiClient {
		if (serverId != null) {
			val uuid = UUIDUtils.parseUUID(serverId)
			if (uuid != null) {
				val serverApi = apiClientFactory.getApiClientForServer(uuid)
				if (serverApi != null) return serverApi
			}
		}
		return apiClient
	}

	private suspend fun getSeriesTmdbId(
		seriesId: UUID,
		item: BaseItemDto? = null,
	): String? {
		val cacheKey = seriesId.toString()
		seriesTmdbIdCache[cacheKey]?.let { return it }

		try {
			val effectiveApi = if (item != null) getApiClientForItem(item) else apiClient
			val response = effectiveApi.userLibraryApi.getItem(itemId = seriesId)
			val seriesItem = response.content

			val tmdbId = seriesItem.providerIds?.get("Tmdb")
			if (tmdbId != null) {
				Timber.i("Found TMDB ID for series ${seriesItem.name}: $tmdbId")
				seriesTmdbIdCache[cacheKey] = tmdbId
				return tmdbId
			} else {
				Timber.w("Series ${seriesItem.name} has no TMDB provider ID. Available IDs: ${seriesItem.providerIds?.keys}")
				seriesTmdbIdCache[cacheKey] = null
				return null
			}
		} catch (e: Exception) {
			Timber.e(e, "Failed to fetch series info for seriesId: $seriesId")
			return null
		}
	}

	fun clearCache() {
		episodeRatingsCache.clear()
		seasonCache.clear()
		seriesTmdbIdCache.clear()
		seriesCommunityRatingCache.clear()
		pendingRequests.clear()
		pendingSeasonRequests.clear()
	}
}
