package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.base.state.toUiError
import org.jellyfin.androidtv.ui.base.tv.TvHeader
import org.jellyfin.androidtv.ui.base.tv.TvRow
import org.jellyfin.androidtv.ui.base.tv.TvRowList
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.personsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFilter
import org.jellyfin.sdk.model.api.ItemSortBy
import timber.log.Timber

data class AllFavoritesUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class AllFavoritesViewModel(
	val api: ApiClient,
) : ViewModel() {

	private val _uiState = MutableStateFlow(AllFavoritesUiState())
	val uiState: StateFlow<AllFavoritesUiState> = _uiState.asStateFlow()

	fun initialize(labels: FavoriteLabels) {
		_uiState.update { it.copy(isLoading = true, error = null) }
		loadFavorites(labels)
	}

	private fun loadFavorites(labels: FavoriteLabels) {
		viewModelScope.launch {
			try {
				val rows = mutableListOf<TvRow<BaseItemDto>>()

				withContext(Dispatchers.IO) {
					val castDef = async {
						api.personsApi.getPersons(
							isFavorite = true,
							fields = ItemRepository.itemFields,
						).content.items
					}
					val moviesDef = async {
						api.itemsApi.getItems(
							sortBy = setOf(ItemSortBy.SORT_NAME),
							filters = setOf(ItemFilter.IS_FAVORITE),
							includeItemTypes = setOf(BaseItemKind.MOVIE),
							recursive = true,
							fields = ItemRepository.itemFields,
						).content.items
					}
					val showsDef = async {
						api.itemsApi.getItems(
							sortBy = setOf(ItemSortBy.SORT_NAME),
							filters = setOf(ItemFilter.IS_FAVORITE),
							includeItemTypes = setOf(BaseItemKind.SERIES),
							recursive = true,
							fields = ItemRepository.itemFields,
						).content.items
					}
					val episodesDef = async {
						api.itemsApi.getItems(
							sortBy = setOf(ItemSortBy.SORT_NAME),
							filters = setOf(ItemFilter.IS_FAVORITE),
							includeItemTypes = setOf(BaseItemKind.EPISODE),
							recursive = true,
							fields = ItemRepository.itemFields,
						).content.items
					}
					val playlistsDef = async {
						api.itemsApi.getItems(
							sortBy = setOf(ItemSortBy.SORT_NAME),
							filters = setOf(ItemFilter.IS_FAVORITE),
							includeItemTypes = setOf(BaseItemKind.PLAYLIST),
							recursive = true,
							fields = ItemRepository.itemFields,
						).content.items
					}

					val cast = castDef.await()
					val movies = moviesDef.await()
					val shows = showsDef.await()
					val episodes = episodesDef.await()
					val playlists = playlistsDef.await()

					if (cast.isNotEmpty()) rows.add(TvRow(title = labels.cast, items = cast, key = "cast"))
					if (movies.isNotEmpty()) rows.add(TvRow(title = labels.movies, items = movies, key = "movies"))
					if (shows.isNotEmpty()) rows.add(TvRow(title = labels.shows, items = shows, key = "shows"))
					if (episodes.isNotEmpty()) rows.add(TvRow(title = labels.episodes, items = episodes, key = "episodes"))
					if (playlists.isNotEmpty()) rows.add(TvRow(title = labels.playlists, items = playlists, key = "playlists"))
				}

				_uiState.update { it.copy(isLoading = false, rows = rows) }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load favorites")
				_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
			}
		}
	}

	fun retry(labels: FavoriteLabels) {
		initialize(labels)
	}
}

data class FavoriteLabels(
	val cast: String,
	val movies: String,
	val shows: String,
	val episodes: String,
	val playlists: String,
)

@Composable
fun AllFavoritesScreen(
	viewModel: AllFavoritesViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val title = stringResource(R.string.lbl_favorites)
	val labels = FavoriteLabels(
		cast = stringResource(R.string.lbl_cast),
		movies = stringResource(R.string.lbl_movies),
		shows = stringResource(R.string.lbl_tv_series),
		episodes = stringResource(R.string.lbl_episodes),
		playlists = stringResource(R.string.lbl_playlists),
	)

	TvScaffold {
		Column(modifier = Modifier.fillMaxSize()) {
			TvHeader(title = title)

			Spacer(modifier = Modifier.height(16.dp))

			val displayState = when {
				uiState.isLoading -> DisplayState.LOADING
				uiState.error != null -> DisplayState.ERROR
				uiState.rows.isEmpty() -> DisplayState.EMPTY
				else -> DisplayState.CONTENT
			}

			StateContainer(
				state = displayState,
				modifier = Modifier.weight(1f),
				loadingContent = {
					Column {
						repeat(3) {
							SkeletonCardRow()
							Spacer(modifier = Modifier.height(28.dp))
						}
					}
				},
				emptyContent = {
					EmptyState(title = stringResource(R.string.lbl_empty))
				},
				errorContent = {
					ErrorState(
						message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
						onRetry = { viewModel.retry(labels) },
					)
				},
				content = {
					TvRowList(
						rows = uiState.rows,
						contentPadding = PaddingValues(bottom = 27.dp),
					) { item ->
						BrowseMediaCard(
							item = item,
							api = viewModel.api,
							onClick = { onItemClick(item) },
						)
					}
				},
			)
		}
	}
}
