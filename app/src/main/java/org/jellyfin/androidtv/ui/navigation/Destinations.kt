package org.jellyfin.androidtv.ui.navigation

import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.ui.browsing.compose.FolderBrowseComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.AllFavoritesComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.ByLetterBrowseFragment
import org.jellyfin.androidtv.ui.browsing.compose.CollectionBrowseFragment
import org.jellyfin.androidtv.ui.browsing.compose.FolderViewComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.SuggestedMoviesComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.GenresGridComposeFragment
import org.jellyfin.androidtv.ui.browsing.compose.LibraryBrowseComposeFragment
import org.jellyfin.androidtv.ui.browsing.v2.LiveTvBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.MusicBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.RecordingsBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.ScheduleBrowseFragment
import org.jellyfin.androidtv.ui.browsing.v2.SeriesRecordingsBrowseFragment
import org.jellyfin.androidtv.ui.home.compose.HomeComposeFragment
import org.jellyfin.androidtv.ui.itemdetail.ItemListFragment
import org.jellyfin.androidtv.ui.itemdetail.compose.ItemDetailComposeFragment
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsFragment
import org.jellyfin.androidtv.ui.itemdetail.v2.TrailerPlayerFragment
import org.jellyfin.androidtv.ui.itemdetail.MusicFavoritesListFragment
import org.jellyfin.androidtv.ui.jellyseerr.BrowseFilterType
import org.jellyfin.androidtv.ui.jellyseerr.DiscoverFragment
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrBrowseByFragment
import org.jellyfin.androidtv.ui.jellyseerr.MediaDetailsFragment
import org.jellyfin.androidtv.ui.jellyseerr.PersonDetailsFragment
import org.jellyfin.androidtv.ui.jellyseerr.RequestsFragment
import org.jellyfin.androidtv.ui.jellyseerr.SettingsFragment as JellyseerrSettingsFragment
import org.jellyfin.androidtv.ui.livetv.LiveTvGuideFragment
import org.jellyfin.androidtv.ui.playback.audio.AudioNowPlayingComposeFragment
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment
import org.jellyfin.androidtv.ui.playback.nextup.NextUpFragment
import org.jellyfin.androidtv.ui.playback.stillwatching.StillWatchingFragment
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
	fun search(query: String? = null) = fragmentDestination<SearchComposeFragment>(
		SearchComposeFragment.EXTRA_QUERY to query,
	)

	// Browsing
	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmOverloads
	fun libraryBrowser(item: BaseItemDto, serverId: UUID? = null, userId: UUID? = null) = fragmentDestination<LibraryBrowseComposeFragment>(
		Extras.Folder to Json.Default.encodeToString(item),
		"ServerId" to serverId?.toString(),
		"UserId" to userId?.toString(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmName("libraryBrowserWithType")
	fun libraryBrowser(item: BaseItemDto, includeType: String) =
		fragmentDestination<LibraryBrowseComposeFragment>(
			Extras.Folder to Json.Default.encodeToString(item),
			Extras.IncludeType to includeType,
		)

	fun liveTvBrowser(item: BaseItemDto) = fragmentDestination<LiveTvBrowseFragment>(
		Extras.Folder to Json.Default.encodeToString(item),
	)

	@JvmOverloads
	fun musicBrowser(item: BaseItemDto, serverId: UUID? = null, userId: UUID? = null) = fragmentDestination<MusicBrowseFragment>(
		Extras.Folder to Json.Default.encodeToString(item),
		"ServerId" to serverId?.toString(),
		"UserId" to userId?.toString(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmOverloads
	fun collectionBrowser(item: BaseItemDto, serverId: UUID? = null, userId: UUID? = null) = fragmentDestination<CollectionBrowseFragment>(
		Extras.Folder to Json.Default.encodeToString(item),
		"ServerId" to serverId?.toString(),
		"UserId" to userId?.toString(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	@JvmOverloads
	fun folderBrowser(item: BaseItemDto, serverId: UUID? = null, userId: UUID? = null) = fragmentDestination<FolderBrowseComposeFragment>(
		Extras.Folder to Json.Default.encodeToString(item),
		"ServerId" to serverId?.toString(),
		"UserId" to userId?.toString(),
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
		LibraryBrowseComposeFragment.ARG_GENRE_NAME to genreName,
		LibraryBrowseComposeFragment.ARG_PARENT_ID to parentId?.toString(),
		LibraryBrowseComposeFragment.ARG_INCLUDE_TYPE to includeType,
		LibraryBrowseComposeFragment.ARG_SERVER_ID to serverId?.toString(),
		LibraryBrowseComposeFragment.ARG_DISPLAY_PREFS_ID to displayPreferencesId,
		LibraryBrowseComposeFragment.ARG_PARENT_ITEM_ID to parentItemId?.toString(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun libraryByGenres(item: BaseItemDto, includeType: String) =
		fragmentDestination<GenresGridComposeFragment>(
			Extras.Folder to Json.Default.encodeToString(item),
			Extras.IncludeType to includeType,
		)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun libraryByLetter(item: BaseItemDto, includeType: String) =
		fragmentDestination<ByLetterBrowseFragment>(
			Extras.Folder to Json.Default.encodeToString(item),
			Extras.IncludeType to includeType,
		)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun librarySuggestions(item: BaseItemDto) =
		fragmentDestination<SuggestedMoviesComposeFragment>(
			Extras.Folder to Json.Default.encodeToString(item),
		)

	// Item details
	@JvmOverloads
	fun itemDetails(item: UUID, serverId: UUID? = null) = fragmentDestination<ItemDetailComposeFragment>(
		"ItemId" to item.toString(),
		"ServerId" to serverId?.toString(),
	)

	@JvmOverloads
	fun itemDetailsV2(item: UUID, serverId: UUID? = null) = fragmentDestination<ItemDetailsFragment>(
		"ItemId" to item.toString(),
		"ServerId" to serverId?.toString(),
	)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun channelDetails(item: UUID, channel: UUID, programInfo: BaseItemDto) =
		fragmentDestination<ItemDetailComposeFragment>(
			"ItemId" to item.toString(),
			"ChannelId" to channel.toString(),
			"ProgramInfo" to Json.Default.encodeToString(programInfo),
		)

	// TODO only pass item id instead of complete JSON to browsing destinations
	fun seriesTimerDetails(item: UUID, seriesTimer: SeriesTimerInfoDto) =
		fragmentDestination<ItemDetailComposeFragment>(
			"ItemId" to item.toString(),
			"SeriesTimer" to Json.Default.encodeToString(seriesTimer),
		)

	@JvmOverloads
	fun itemList(item: UUID, serverId: UUID? = null) = fragmentDestination<ItemListFragment>(
		"ItemId" to item.toString(),
		"ServerId" to serverId?.toString(),
	)

	fun musicFavorites(parent: UUID) = fragmentDestination<MusicFavoritesListFragment>(
		"ParentId" to parent.toString(),
	)

	// Trailer player
	fun trailerPlayer(
		videoId: String,
		startSeconds: Double = 0.0,
		segmentsJson: String = "[]",
	) = fragmentDestination<TrailerPlayerFragment>(
		TrailerPlayerFragment.ARG_VIDEO_ID to videoId,
		TrailerPlayerFragment.ARG_START_SECONDS to startSeconds,
		TrailerPlayerFragment.ARG_SEGMENTS_JSON to segmentsJson,
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
		PhotoPlayerFragment.ARGUMENT_ITEM_ID to item.toString(),
		PhotoPlayerFragment.ARGUMENT_ALBUM_SORT_BY to albumSortBy?.serialName,
		PhotoPlayerFragment.ARGUMENT_ALBUM_SORT_ORDER to albumSortOrder?.serialName,
		PhotoPlayerFragment.ARGUMENT_AUTO_PLAY to autoPlay,
	)

	fun videoPlayer(position: Int?) = fragmentDestination<CustomPlaybackOverlayFragment>(
		"Position" to (position ?: 0)
	)

	fun videoPlayerNew(position: Int?) = fragmentDestination<VideoPlayerFragment>(
		VideoPlayerFragment.EXTRA_POSITION to position
	)

	fun nextUp(item: UUID) = fragmentDestination<NextUpFragment>(
		NextUpFragment.ARGUMENT_ITEM_ID to item.toString()
	)

	fun stillWatching(item: UUID) = fragmentDestination<StillWatchingFragment>(
		NextUpFragment.ARGUMENT_ITEM_ID to item.toString()
	)

	// Jellyseerr
	val jellyseerrDiscover = fragmentDestination<DiscoverFragment>()
	val jellyseerrRequests = fragmentDestination<RequestsFragment>()
	val jellyseerrSettings = fragmentDestination<JellyseerrSettingsFragment>()
	
	fun jellyseerrBrowseBy(
		filterId: Int, 
		filterName: String, 
		mediaType: String,
		filterType: BrowseFilterType = BrowseFilterType.GENRE
	) = fragmentDestination<JellyseerrBrowseByFragment>(
		"filter_id" to filterId,
		"filter_name" to filterName,
		"media_type" to mediaType,
		"filter_type" to filterType.name,
	)
	
	// Convenience methods for specific filter types
	fun jellyseerrBrowseByGenre(genreId: Int, genreName: String, mediaType: String) = 
		jellyseerrBrowseBy(genreId, genreName, mediaType, BrowseFilterType.GENRE)
	
	fun jellyseerrBrowseByNetwork(networkId: Int, networkName: String) = 
		jellyseerrBrowseBy(networkId, networkName, "tv", BrowseFilterType.NETWORK)
	
	fun jellyseerrBrowseByStudio(studioId: Int, studioName: String) = 
		jellyseerrBrowseBy(studioId, studioName, "movie", BrowseFilterType.STUDIO)
	
	fun jellyseerrMediaDetails(itemJson: String) = fragmentDestination<MediaDetailsFragment>(
		"item" to itemJson
	)
	
	fun jellyseerrPersonDetails(personId: Int) = fragmentDestination<PersonDetailsFragment>(
		"personId" to personId.toString()
	)
}
