package org.jellyfin.androidtv.ui.itemdetail.v2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.VideoRangeType
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailsViewModelTests : FunSpec({
	val testDispatcher = StandardTestDispatcher()

	// Jellyfin SDK data classes use kotlinx.serialization generated constructors
	// that require ALL parameters (no defaults in the Kotlin constructor).
	// JSON deserialization handles defaults correctly via internal bit masks.
	val testJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

	beforeTest {
		Dispatchers.setMain(testDispatcher)
	}

	afterTest {
		Dispatchers.resetMain()
		unmockkAll()
	}

	fun createVM() = ItemDetailsViewModel(
		api = mockk<ApiClient>(relaxed = true),
		apiClientFactory = mockk<ApiClientFactory>(relaxed = true),
	)

	@Suppress("UNCHECKED_CAST")
	fun getMutableState(vm: ItemDetailsViewModel): MutableStateFlow<ItemDetailsUiState> {
		val field = vm.javaClass.getDeclaredField("_uiState")
		field.isAccessible = true
		return field.get(vm) as MutableStateFlow<ItemDetailsUiState>
	}

	// Creates a real BaseItemDto via JSON deserialization (needed for .copy() in toggle tests)
	fun realItem(
		id: UUID = UUID.randomUUID(),
		isFavorite: Boolean = false,
		played: Boolean = false,
		playedPercentage: Double? = null,
	): BaseItemDto = testJson.decodeFromString(
		BaseItemDto.serializer(),
		buildString {
			append("""{"Id":"$id","Type":"Movie","UserData":{""")
			append(""""IsFavorite":$isFavorite,"Played":$played""")
			append(""","PlaybackPositionTicks":0,"PlayCount":0,"Key":"","ItemId":"$id"""")
			if (playedPercentage != null) append(""","PlayedPercentage":$playedPercentage""")
			append("}}")
		},
	)

	// MockK-based helpers for tests that don't need .copy()
	fun mockItem(
		id: UUID = UUID.randomUUID(),
		name: String? = null,
		playlistItemId: String? = null,
		mediaSources: List<MediaSourceInfo>? = null,
	) = mockk<BaseItemDto>(relaxed = true) {
		every { this@mockk.id } returns id
		every { this@mockk.name } returns name
		every { this@mockk.playlistItemId } returns playlistItemId
		every { this@mockk.mediaSources } returns mediaSources
	}

	fun mockVideoStream(
		width: Int? = null,
		codec: String? = null,
		videoRangeType: VideoRangeType? = null,
	) = mockk<MediaStream>(relaxed = true) {
		every { type } returns MediaStreamType.VIDEO
		every { this@mockk.width } returns width
		every { this@mockk.codec } returns codec
		if (videoRangeType != null) every { this@mockk.videoRangeType } returns videoRangeType
	}

	fun mockAudioStream(
		channels: Int? = null,
		codec: String? = null,
	) = mockk<MediaStream>(relaxed = true) {
		every { type } returns MediaStreamType.AUDIO
		every { this@mockk.channels } returns channels
		every { this@mockk.codec } returns codec
	}

	fun mockMediaSource(
		container: String? = null,
		streams: List<MediaStream> = emptyList(),
	) = mockk<MediaSourceInfo>(relaxed = true) {
		every { mediaStreams } returns streams
		every { this@mockk.container } returns container
	}

	// --- Initial state ---

	test("initial state has default values") {
		val vm = createVM()
		val state = vm.uiState.value

		state.isLoading shouldBe true
		state.error shouldBe null
		state.item shouldBe null
		state.seasons.shouldBeEmpty()
		state.episodes.shouldBeEmpty()
		state.tracks.shouldBeEmpty()
		state.albums.shouldBeEmpty()
		state.similar.shouldBeEmpty()
		state.cast.shouldBeEmpty()
		state.nextUp.shouldBeEmpty()
		state.collectionItems.shouldBeEmpty()
		state.directors.shouldBeEmpty()
		state.writers.shouldBeEmpty()
		state.badges.shouldBeEmpty()
	}

	// --- toggleFavorite (uses real instances for .copy() support) ---

	test("toggleFavorite sets isFavorite to true when currently false") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(isLoading = false, item = realItem(isFavorite = false))

		vm.toggleFavorite()

		vm.uiState.value.item?.userData?.isFavorite shouldBe true
	}

	test("toggleFavorite sets isFavorite to false when currently true") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(isLoading = false, item = realItem(isFavorite = true))

		vm.toggleFavorite()

		vm.uiState.value.item?.userData?.isFavorite shouldBe false
	}

	test("toggleFavorite is no-op when item is null") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(isLoading = false, item = null)

		vm.toggleFavorite()

		vm.uiState.value.item shouldBe null
	}

	// --- toggleWatched (uses real instances for .copy() support) ---

	test("toggleWatched sets played to true and percentage to 100 when currently false") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(isLoading = false, item = realItem(played = false))

		vm.toggleWatched()

		vm.uiState.value.item?.userData?.played shouldBe true
		vm.uiState.value.item?.userData?.playedPercentage shouldBe 100.0
	}

	test("toggleWatched sets played to false and percentage to 0 when currently true") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = realItem(played = true, playedPercentage = 100.0),
		)

		vm.toggleWatched()

		vm.uiState.value.item?.userData?.played shouldBe false
		vm.uiState.value.item?.userData?.playedPercentage shouldBe 0.0
	}

	test("toggleWatched is no-op when item is null") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(isLoading = false, item = null)

		vm.toggleWatched()

		vm.uiState.value.item shouldBe null
	}

	// --- retry ---

	test("retry is no-op when lastItemId is null") {
		val vm = createVM()
		val stateBefore = vm.uiState.value

		vm.retry()
		testDispatcher.scheduler.runCurrent()

		vm.uiState.value shouldBe stateBefore
	}

	// --- movePlaylistItem (uses mocks — no .copy() on tracks) ---

	test("movePlaylistItem reorders tracks correctly") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(
				mockItem(name = "Track 1", playlistItemId = "pid1"),
				mockItem(name = "Track 2", playlistItemId = "pid2"),
				mockItem(name = "Track 3", playlistItemId = "pid3"),
			),
		)

		vm.movePlaylistItem(0, 2)

		vm.uiState.value.tracks.map { it.name } shouldBe listOf("Track 2", "Track 3", "Track 1")
	}

	test("movePlaylistItem swaps adjacent tracks") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(
				mockItem(name = "A", playlistItemId = "pid1"),
				mockItem(name = "B", playlistItemId = "pid2"),
			),
		)

		vm.movePlaylistItem(0, 1)

		vm.uiState.value.tracks.map { it.name } shouldBe listOf("B", "A")
	}

	test("movePlaylistItem is no-op with invalid indices") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(mockItem(name = "Track 1", playlistItemId = "pid1")),
		)

		vm.movePlaylistItem(0, 5)

		vm.uiState.value.tracks shouldHaveSize 1
		vm.uiState.value.tracks.first().name shouldBe "Track 1"
	}

	test("movePlaylistItem is no-op without item") {
		val vm = createVM()

		vm.movePlaylistItem(0, 1)

		vm.uiState.value.tracks.shouldBeEmpty()
	}

	test("movePlaylistItem is no-op without playlistItemId") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(mockItem(name = "A"), mockItem(name = "B")),
		)

		vm.movePlaylistItem(0, 1)

		vm.uiState.value.tracks.map { it.name } shouldBe listOf("A", "B")
	}

	// --- removeFromPlaylist ---

	test("removeFromPlaylist removes track at index") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(
				mockItem(name = "Track 1", playlistItemId = "pid1"),
				mockItem(name = "Track 2", playlistItemId = "pid2"),
			),
		)

		vm.removeFromPlaylist(0)

		vm.uiState.value.tracks shouldHaveSize 1
		vm.uiState.value.tracks.first().name shouldBe "Track 2"
	}

	test("removeFromPlaylist is no-op with invalid index") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(mockItem(playlistItemId = "pid1")),
		)

		vm.removeFromPlaylist(5)

		vm.uiState.value.tracks shouldHaveSize 1
	}

	test("removeFromPlaylist is no-op without item") {
		val vm = createVM()

		vm.removeFromPlaylist(0)

		vm.uiState.value.tracks.shouldBeEmpty()
	}

	test("removeFromPlaylist is no-op without playlistItemId") {
		val vm = createVM()
		val stateFlow = getMutableState(vm)
		stateFlow.value = ItemDetailsUiState(
			isLoading = false,
			item = mockItem(),
			tracks = listOf(mockItem(name = "No PID")),
		)

		vm.removeFromPlaylist(0)

		vm.uiState.value.tracks shouldHaveSize 1
		vm.uiState.value.tracks.first().name shouldBe "No PID"
	}

	// --- getMediaBadges (companion object, pure function) ---

	test("getMediaBadges returns 4K badge for width >= 3800") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 3840)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "4K" } shouldBe true
	}

	test("getMediaBadges returns 1080p badge for width >= 1900") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 1920)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "1080p" } shouldBe true
	}

	test("getMediaBadges returns 720p badge for width >= 1260") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 1280)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "720p" } shouldBe true
	}

	test("getMediaBadges returns no resolution badge for width below 1260") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 640)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.none { it.type == "badge4k" || it.type == "badgeHd" } shouldBe true
	}

	test("getMediaBadges returns HDR10 badge") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 3840, videoRangeType = VideoRangeType.HDR10)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "HDR10" } shouldBe true
	}

	test("getMediaBadges returns HDR10+ badge") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 3840, videoRangeType = VideoRangeType.HDR10_PLUS)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "HDR10+" } shouldBe true
	}

	test("getMediaBadges returns DV badge for Dolby Vision") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 3840, videoRangeType = VideoRangeType.DOVI)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "DV" } shouldBe true
	}

	test("getMediaBadges returns DV + HDR10 for DOVI_WITH_HDR10") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 3840, videoRangeType = VideoRangeType.DOVI_WITH_HDR10)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "DV" } shouldBe true
		badges.any { it.label == "HDR10" } shouldBe true
	}

	test("getMediaBadges returns no HDR badge for SDR") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(width = 1920, videoRangeType = VideoRangeType.SDR)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.none { it.type == "badgeHdr" || it.type == "badgeDv" } shouldBe true
	}

	test("getMediaBadges extracts HEVC codec") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(codec = "hevc")),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "HEVC" } shouldBe true
	}

	test("getMediaBadges extracts AV1 codec") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(codec = "av1")),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "AV1" } shouldBe true
	}

	test("getMediaBadges extracts container format") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			container = "mkv",
			streams = listOf(mockVideoStream()),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "MKV" } shouldBe true
	}

	test("getMediaBadges extracts 5.1 surround sound") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(), mockAudioStream(channels = 6)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "5.1" } shouldBe true
	}

	test("getMediaBadges extracts 7.1 surround sound") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(), mockAudioStream(channels = 8)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "7.1" } shouldBe true
	}

	test("getMediaBadges extracts stereo") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(), mockAudioStream(channels = 2)),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "Stereo" } shouldBe true
	}

	test("getMediaBadges extracts audio codec") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(), mockAudioStream(codec = "ac3")),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "AC3" } shouldBe true
	}

	test("getMediaBadges extracts TrueHD audio codec") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			streams = listOf(mockVideoStream(), mockAudioStream(codec = "truehd")),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "TrueHD" } shouldBe true
	}

	test("getMediaBadges returns empty list when no media sources") {
		val item = mockItem()

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.shouldBeEmpty()
	}

	test("getMediaBadges returns empty list when media streams are null") {
		val item = mockItem(mediaSources = listOf(mockk<MediaSourceInfo>(relaxed = true) {
			every { mediaStreams } returns null
		}))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.shouldBeEmpty()
	}

	test("getMediaBadges complete 4K HDR10 HEVC 5.1 AC3 MKV item") {
		val item = mockItem(mediaSources = listOf(mockMediaSource(
			container = "mkv",
			streams = listOf(
				mockVideoStream(width = 3840, videoRangeType = VideoRangeType.HDR10, codec = "hevc"),
				mockAudioStream(channels = 6, codec = "ac3"),
			),
		)))

		val badges = ItemDetailsViewModel.getMediaBadges(item)

		badges.any { it.label == "4K" } shouldBe true
		badges.any { it.label == "HDR10" } shouldBe true
		badges.any { it.label == "HEVC" } shouldBe true
		badges.any { it.label == "MKV" } shouldBe true
		badges.any { it.label == "5.1" } shouldBe true
		badges.any { it.label == "AC3" } shouldBe true
	}
})
