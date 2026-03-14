package org.jellyfin.androidtv.ui.browsing.v2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.jellyfin.androidtv.data.repository.MultiServerRepository
import org.jellyfin.androidtv.preference.PreferencesRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryBrowseViewModelTests :
	FunSpec({
		val testDispatcher = StandardTestDispatcher()

		beforeTest {
			Dispatchers.setMain(testDispatcher)
		}

		afterTest {
			Dispatchers.resetMain()
			unmockkAll()
		}

		fun createVM() =
			LibraryBrowseViewModel(
				api = mockk(relaxed = true),
				apiClientFactory = mockk<ApiClientFactory>(relaxed = true),
				preferencesRepository = mockk<PreferencesRepository>(relaxed = true),
				multiServerRepository = mockk<MultiServerRepository>(relaxed = true),
				userPreferences = mockk<UserPreferences>(relaxed = true),
			)

		@Suppress("UNCHECKED_CAST")
		fun getMutableState(vm: LibraryBrowseViewModel): MutableStateFlow<LibraryBrowseUiState> {
			val field = vm.javaClass.getDeclaredField("_uiState")
			field.isAccessible = true
			return field.get(vm) as MutableStateFlow<LibraryBrowseUiState>
		}

		test("initial state has default values") {
			val vm = createVM()
			val state = vm.uiState.value

			state.isLoading shouldBe true
			state.error shouldBe null
			state.items.shouldBeEmpty()
			state.totalItems shouldBe 0
			state.libraryName shouldBe ""
			state.filterFavorites shouldBe false
			state.filterPlayed shouldBe PlayedStatusFilter.ALL
			state.filterSeriesStatus shouldBe SeriesStatusFilter.ALL
			state.startLetter shouldBe null
			state.hasMoreItems shouldBe false
			state.focusedItem shouldBe null
			state.isGenreMode shouldBe false
		}

		test("setFocusedItem updates focused item") {
			val vm = createVM()
			val mockItem = mockk<org.jellyfin.sdk.model.api.BaseItemDto>(relaxed = true)

			vm.setFocusedItem(mockItem)

			vm.uiState.value.focusedItem shouldBe mockItem
		}

		test("setSortOption updates current sort option") {
			val vm = createVM()
			// No folder set: loadItems() returns early, only state change observed
			val newSort =
				SortOption(
					nameRes = org.jellyfin.androidtv.R.string.lbl_date_added,
					sortBy = ItemSortBy.DATE_CREATED,
					sortOrder = SortOrder.DESCENDING,
				)

			vm.setSortOption(newSort)

			vm.uiState.value.currentSortOption shouldBe newSort
		}

		test("toggleFavorites toggles filter state") {
			val vm = createVM()

			vm.uiState.value.filterFavorites shouldBe false

			vm.toggleFavorites()
			vm.uiState.value.filterFavorites shouldBe true

			vm.toggleFavorites()
			vm.uiState.value.filterFavorites shouldBe false
		}

		test("setPlayedFilter updates filter") {
			val vm = createVM()

			vm.setPlayedFilter(PlayedStatusFilter.WATCHED)
			vm.uiState.value.filterPlayed shouldBe PlayedStatusFilter.WATCHED

			vm.setPlayedFilter(PlayedStatusFilter.UNWATCHED)
			vm.uiState.value.filterPlayed shouldBe PlayedStatusFilter.UNWATCHED

			vm.setPlayedFilter(PlayedStatusFilter.ALL)
			vm.uiState.value.filterPlayed shouldBe PlayedStatusFilter.ALL
		}

		test("setSeriesStatusFilter updates filter") {
			val vm = createVM()

			vm.setSeriesStatusFilter(SeriesStatusFilter.CONTINUING)
			vm.uiState.value.filterSeriesStatus shouldBe SeriesStatusFilter.CONTINUING

			vm.setSeriesStatusFilter(SeriesStatusFilter.ENDED)
			vm.uiState.value.filterSeriesStatus shouldBe SeriesStatusFilter.ENDED
		}

		test("setStartLetter updates letter filter") {
			val vm = createVM()

			vm.setStartLetter("A")
			vm.uiState.value.startLetter shouldBe "A"

			vm.setStartLetter(null)
			vm.uiState.value.startLetter shouldBe null
		}

		test("loadMore does nothing when hasMoreItems is false") {
			val vm = createVM()
			val stateBefore = vm.uiState.value

			vm.loadMore()

			vm.uiState.value shouldBe stateBefore
		}

		test("refreshDisplayPreferences returns false when not initialized") {
			val vm = createVM()

			vm.refreshDisplayPreferences() shouldBe false
		}

		test("retry clears error and starts loading when in genre mode") {
			val vm = createVM()

			// Set up genre mode and error state via reflection
			val genreFilterField = vm.javaClass.getDeclaredField("genreFilter")
			genreFilterField.isAccessible = true
			genreFilterField.set(vm, "Action")

			val stateFlow = getMutableState(vm)
			stateFlow.value =
				LibraryBrowseUiState(
					isGenreMode = true,
					genreName = "Action",
					libraryName = "Action",
					error = UiError.Network(),
					isLoading = false,
				)

			// Verify error is set
			vm.uiState.value.error
				.shouldBeInstanceOf<UiError.Network>()
			vm.uiState.value.isLoading shouldBe false

			// Call retry — queues a coroutine via loadItems
			vm.retry()

			// Advance scheduler to run coroutine up to first IO suspension
			testDispatcher.scheduler.runCurrent()

			// loadItems(reset=true) sets error=null, isLoading=true before the API call
			vm.uiState.value.error shouldBe null
			vm.uiState.value.isLoading shouldBe true
			vm.uiState.value.items
				.shouldBeEmpty()
		}

		test("retry is no-op when not initialized and not in genre mode") {
			val vm = createVM()
			val initialState = vm.uiState.value

			vm.retry()
			testDispatcher.scheduler.runCurrent()

			// loadItems returns early because folder is null and isGenreMode is false
			vm.uiState.value shouldBe initialState
		}
	})
