package org.jellyfin.androidtv.ui.browsing.v2

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
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
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.browsing.genre.JellyfinGenreItem
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.api.client.ApiClient
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class GenresGridViewModelTests :
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
			GenresGridViewModel(
				api = mockk<ApiClient>(relaxed = true),
				apiClientFactory = mockk<ApiClientFactory>(relaxed = true),
				multiServerRepository = mockk<MultiServerRepository>(relaxed = true),
				userPreferences = mockk<UserPreferences>(relaxed = true),
			)

		@Suppress("UNCHECKED_CAST")
		fun getMutableState(vm: GenresGridViewModel): MutableStateFlow<GenresGridUiState> {
			val field = vm.javaClass.getDeclaredField("_uiState")
			field.isAccessible = true
			return field.get(vm) as MutableStateFlow<GenresGridUiState>
		}

		@Suppress("UNCHECKED_CAST")
		fun setAllGenres(
			vm: GenresGridViewModel,
			genres: List<JellyfinGenreItem>,
		) {
			val field = vm.javaClass.getDeclaredField("allGenres")
			field.isAccessible = true
			val list = field.get(vm) as MutableList<JellyfinGenreItem>
			list.clear()
			list.addAll(genres)
		}

		test("initial state has default values") {
			val vm = createVM()
			val state = vm.uiState.value

			state.isLoading shouldBe true
			state.error shouldBe null
			state.title shouldBe "Genres"
			state.genres.shouldBeEmpty()
			state.totalGenres shouldBe 0
			state.currentSort shouldBe GenreSortOption.NAME_ASC
			state.selectedLibraryId shouldBe null
			state.selectedLibraryName shouldBe null
			state.libraries.shouldBeEmpty()
			state.focusedGenre shouldBe null
		}

		test("sortOptions contains all enum entries") {
			val vm = createVM()

			vm.sortOptions shouldContainExactly GenreSortOption.entries.toList()
		}

		test("setFocusedGenre updates focused genre") {
			val vm = createVM()
			val genre =
				JellyfinGenreItem(
					id = UUID.randomUUID(),
					name = "Action",
					itemCount = 42,
				)

			vm.setFocusedGenre(genre)

			vm.uiState.value.focusedGenre shouldBe genre
		}

		test("setSortOption updates sort and re-sorts genres") {
			val vm = createVM()

			// Populate allGenres via reflection
			val genres =
				listOf(
					JellyfinGenreItem(id = UUID.randomUUID(), name = "Zeta", itemCount = 5),
					JellyfinGenreItem(id = UUID.randomUUID(), name = "Alpha", itemCount = 20),
					JellyfinGenreItem(id = UUID.randomUUID(), name = "Middle", itemCount = 10),
				)
			setAllGenres(vm, genres)

			// Sort by name ascending
			vm.setSortOption(GenreSortOption.NAME_ASC)
			vm.uiState.value.genres
				.map { it.name } shouldBe listOf("Alpha", "Middle", "Zeta")
			vm.uiState.value.currentSort shouldBe GenreSortOption.NAME_ASC

			// Sort by name descending
			vm.setSortOption(GenreSortOption.NAME_DESC)
			vm.uiState.value.genres
				.map { it.name } shouldBe listOf("Zeta", "Middle", "Alpha")

			// Sort by most items
			vm.setSortOption(GenreSortOption.MOST_ITEMS)
			vm.uiState.value.genres
				.map { it.name } shouldBe listOf("Alpha", "Middle", "Zeta")

			// Sort by least items
			vm.setSortOption(GenreSortOption.LEAST_ITEMS)
			vm.uiState.value.genres
				.map { it.name } shouldBe listOf("Zeta", "Middle", "Alpha")
		}

		test("setSortOption sets totalGenres correctly") {
			val vm = createVM()

			val genres =
				listOf(
					JellyfinGenreItem(id = UUID.randomUUID(), name = "A", itemCount = 1),
					JellyfinGenreItem(id = UUID.randomUUID(), name = "B", itemCount = 2),
				)
			setAllGenres(vm, genres)

			vm.setSortOption(GenreSortOption.NAME_ASC)

			vm.uiState.value.totalGenres shouldBe 2
			vm.uiState.value.isLoading shouldBe false
		}

		test("retry clears error and sets loading") {
			val vm = createVM()

			// Set error state
			val stateFlow = getMutableState(vm)
			stateFlow.value =
				GenresGridUiState(
					error = UiError.Network(),
					isLoading = false,
				)

			// Verify error is set
			vm.uiState.value.error
				.shouldBeInstanceOf<UiError.Network>()

			// Call retry — synchronously sets error=null, isLoading=true
			vm.retry()

			// Verify immediate state change (before loadData coroutine runs)
			vm.uiState.value.error shouldBe null
			vm.uiState.value.isLoading shouldBe true
		}

		test("retry with server error clears to loading") {
			val vm = createVM()

			val stateFlow = getMutableState(vm)
			stateFlow.value =
				GenresGridUiState(
					error = UiError.Server(),
					isLoading = false,
					totalGenres = 0,
				)

			vm.retry()

			vm.uiState.value.error shouldBe null
			vm.uiState.value.isLoading shouldBe true
		}

		test("empty genres results in empty state with no error") {
			val vm = createVM()

			// Simulate empty genres loaded
			setAllGenres(vm, emptyList())
			vm.setSortOption(GenreSortOption.NAME_ASC)

			vm.uiState.value.genres
				.shouldBeEmpty()
			vm.uiState.value.totalGenres shouldBe 0
			vm.uiState.value.error shouldBe null
			vm.uiState.value.isLoading shouldBe false
		}
	})
