package org.jellyfin.androidtv.ui.navigation

import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.ui.browsing.compose.AllFavoritesComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.FolderBrowseComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.FolderViewComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.GenresGridComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.LibraryBrowseComposeFragment
import org.jellyfin.androidtv.ui.browsing.v2.LiveTvBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.MusicBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.RecordingsBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.ScheduleBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.SeriesRecordingsBrowseFragment
import org.jellyfin.androidtv.ui.home.compose.HomeComposeFragment
import org.jellyfin.androidtv.ui.itemdetail.ItemListFragment
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsFragment
import org.jellyfin.androidtv.ui.itemdetail.v2.TrailerPlayerFragment
import org.jellyfin.androidtv.ui.jellyseerr.BrowseFilterType
import org.jellyfin.androidtv.ui.jellyseerr.DiscoverFragment
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrBrowseByFragment
import org.jellyfin.androidtv.ui.jellyseerr.MediaDetailsFragment
import org.jellyfin.androidtv.ui.jellyseerr.PersonDetailsFragment
import org.jellyfin.androidtv.ui.livetv.LiveTvGuideFragment
import org.jellyfin.androidtv.ui.playback.audio.AudioNowPlayingComposeFragment
import org.jellyfin.androidtv.ui.player.photo.PhotoPlayerFragment
import org.jellyfin.androidtv.ui.player.video.VideoPlayerFragment
import org.jellyfin.androidtv.ui.search.compose.SearchComposeFragment
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.jellyfin.sdk.model.api.SortOrder
import java.util.UUID

@Suppress("TooManyFunctions")
object Destinations {
	// General
	val home = fragmentDestination<HomeComposeFragment>()

	fun search(query: String? = null) =
		fragmentDestination<SearchComposeFragment>(
			SearchComposeFragment.Args(query = query).toBundle(),
		)

	// Browsing
	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmOverloads
	fun libraryBrowser(
		item: BaseItemDto,
		serverId: UUID? = null,
		userId: UUID? = null,
	) = fragmentDestination<LibraryBrowseComposeFragment>(
		LibraryBrowseComposeFragment.Args
			.LibraryArgs(
				folderJson = Json.Default.encodeToString(item),
				serverId = serverId,
				userId = userId,
			).toBundle(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmName("libraryBrowserWithType")
	fun libraryBrowser(
		item: BaseItemDto,
		includeType: String,
	) = fragmentDestination<LibraryBrowseComposeFragment>(
		LibraryBrowseComposeFragment.Args
			.LibraryArgs(
				folderJson = Json.Default.encodeToString(item),
				includeType = includeType,
			).toBundle(),
	)

	fun liveTvBrowser(item: BaseItemDto) =
		fragmentDestination<LiveTvBrowseFragment>(
			LiveTvBrowseFragment
				.Args(
					folderJson = Json.Default.encodeToString(item),
				).toBundle(),
		)

	@JvmOverloads
	fun musicBrowser(
		item: BaseItemDto,
		serverId: UUID? = null,
		userId: UUID? = null,
	) = fragmentDestination<MusicBrowseFragment>(
		MusicBrowseFragment
			.Args(
				folderJson = Json.Default.encodeToString(item),
				serverId = serverId,
				userId = userId,
			).toBundle(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmOverloads
	fun folderBrowser(
		item: BaseItemDto,
		serverId: UUID? = null,
		userId: UUID? = null,
	) = fragmentDestination<FolderBrowseComposeFragment>(
		FolderBrowseComposeFragment
			.Args(
				folderJson = Json.Default.encodeToString(item),
				serverId = serverId,
				userId = userId,
			).toBundle(),
	)

	// All genres across all libraries (new grid view)
	val allGenres = fragmentDestination<GenresGridComposeFragment>()

	// All favorites across all libraries
	val allFavorites = fragmentDestination<AllFavoritesComposeFragment>()

	// Folder view - browse by folder structure
	val folderView = fragmentDestination<FolderViewComposeFragment>()

	// Browse items by genre (using the V2 library browser)
	fun genreBrowse(
		genreName: String,
		parentId: UUID? = null,
		includeType: String? = null,
		serverId: UUID? = null,
		displayPreferencesId: String? = null,
		parentItemId: UUID? = null,
	) = fragmentDestination<LibraryBrowseComposeFragment>(
		LibraryBrowseComposeFragment.Args
			.GenreArgs(
				genreName = genreName,
				parentId = parentId,
				includeType = includeType,
				serverId = serverId,
				displayPrefsId = displayPreferencesId,
				parentItemId = parentItemId,
			).toBundle(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun libraryByGenres(
		item: BaseItemDto,
		includeType: String,
	) = fragmentDestination<GenresGridComposeFragment>(
		GenresGridComposeFragment
			.Args(
				folderJson = Json.Default.encodeToString(item),
				includeType = includeType,
			).toBundle(),
	)

	// Item details
	@JvmOverloads
	fun itemDetails(
		item: UUID,
		serverId: UUID? = null,
	) = fragmentDestination<ItemDetailsFragment>(
		ItemDetailsFragment
			.Args(
				itemId = item,
				serverId = serverId,
			).toBundle(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun channelDetails(
		item: UUID,
		channel: UUID,
		programInfo: BaseItemDto,
	) = fragmentDestination<ItemDetailsFragment>(
		ItemDetailsFragment
			.Args(
				itemId = item,
				channelId = channel,
				programInfoJson = Json.Default.encodeToString(programInfo),
			).toBundle(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun seriesTimerDetails(
		item: UUID,
		seriesTimer: SeriesTimerInfoDto,
	) = fragmentDestination<ItemDetailsFragment>(
		ItemDetailsFragment
			.Args(
				itemId = item,
				seriesTimerJson = Json.Default.encodeToString(seriesTimer),
			).toBundle(),
	)

	@JvmOverloads
	fun itemList(
		item: UUID,
		serverId: UUID? = null,
	) = fragmentDestination<ItemListFragment>(
		ItemListFragment
			.Args(
				itemId = item,
				serverId = serverId,
			).toBundle(),
	)

	// Trailer player
	fun trailerPlayer(
		videoId: String,
		startSeconds: Double = 0.0,
		segmentsJson: String = "[]",
	) = fragmentDestination<TrailerPlayerFragment>(
		TrailerPlayerFragment
			.Args(
				videoId = videoId,
				startSeconds = startSeconds,
				segmentsJson = segmentsJson,
			).toBundle(),
	)

	// Live TV
	val liveTvGuide = fragmentDestination<LiveTvGuideFragment>()
	val liveTvSchedule = fragmentDestination<ScheduleBrowseFragment>()
	val liveTvRecordings = fragmentDestination<RecordingsBrowseFragment>()
	val liveTvSeriesRecordings = fragmentDestination<SeriesRecordingsBrowseFragment>()

	// Playback
	val nowPlaying = fragmentDestination<AudioNowPlayingComposeFragment>()

	fun photoPlayer(
		item: UUID,
		autoPlay: Boolean,
		albumSortBy: ItemSortBy?,
		albumSortOrder: SortOrder?,
	) = fragmentDestination<PhotoPlayerFragment>(
		PhotoPlayerFragment
			.Args(
				itemId = item,
				albumSortBy = albumSortBy,
				albumSortOrder = albumSortOrder,
				autoPlay = autoPlay,
			).toBundle(),
	)

	fun videoPlayer(position: Int?) =
		fragmentDestination<VideoPlayerFragment>(
			VideoPlayerFragment.Args(position = position).toBundle(),
		)

	// Jellyseerr
	val jellyseerrDiscover = fragmentDestination<DiscoverFragment>()

	fun jellyseerrBrowseBy(
		filterId: Int,
		filterName: String,
		mediaType: String,
		filterType: BrowseFilterType = BrowseFilterType.GENRE,
	) = fragmentDestination<JellyseerrBrowseByFragment>(
		JellyseerrBrowseByFragment
			.Args(
				filterId = filterId,
				filterName = filterName,
				mediaType = mediaType,
				filterType = filterType,
			).toBundle(),
	)

	// Convenience methods for specific filter types
	fun jellyseerrBrowseByGenre(
		genreId: Int,
		genreName: String,
		mediaType: String,
	) = jellyseerrBrowseBy(genreId, genreName, mediaType, BrowseFilterType.GENRE)

	fun jellyseerrBrowseByNetwork(
		networkId: Int,
		networkName: String,
	) = jellyseerrBrowseBy(networkId, networkName, "tv", BrowseFilterType.NETWORK)

	fun jellyseerrBrowseByStudio(
		studioId: Int,
		studioName: String,
	) = jellyseerrBrowseBy(studioId, studioName, "movie", BrowseFilterType.STUDIO)

	fun jellyseerrMediaDetails(itemJson: String) =
		fragmentDestination<MediaDetailsFragment>(
			MediaDetailsFragment.Args(itemJson = itemJson).toBundle(),
		)

	fun jellyseerrPersonDetails(personId: Int) =
		fragmentDestination<PersonDetailsFragment>(
			PersonDetailsFragment.Args(personId = personId).toBundle(),
		)
}
