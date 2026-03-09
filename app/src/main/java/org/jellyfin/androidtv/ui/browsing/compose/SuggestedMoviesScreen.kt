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
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber
import java.util.UUID

data class SuggestedMoviesUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val title: String = "",
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class SuggestedMoviesViewModel(
	val api: ApiClient,
) : ViewModel() {

	private val _uiState = MutableStateFlow(SuggestedMoviesUiState())
	val uiState: StateFlow<SuggestedMoviesUiState> = _uiState.asStateFlow()

	private var folderId: UUID? = null

	fun initialize(folderJson: String, becauseYouWatchedTemplate: String) {
		val folder = Json.decodeFromString<BaseItemDto>(folderJson)
		folderId = folder.id
		_uiState.update { it.copy(title = folder.name ?: "", isLoading = true, error = null) }
		loadSuggestions(folder.id, becauseYouWatchedTemplate)
	}

	private fun loadSuggestions(parentId: UUID, becauseTemplate: String) {
		viewModelScope.launch {
			try {
				val recentMovies = withContext(Dispatchers.IO) {
					api.itemsApi.getItems(
						parentId = parentId,
						includeItemTypes = setOf(BaseItemKind.MOVIE),
						sortOrder = setOf(SortOrder.DESCENDING),
						sortBy = setOf(ItemSortBy.DATE_PLAYED),
						limit = 8,
						recursive = true,
					).content.items
				}

				val rows = withContext(Dispatchers.IO) {
					recentMovies.map { movie ->
						async {
							try {
								val similar = api.libraryApi.getSimilarItems(
									itemId = movie.id,
									fields = ItemRepository.itemFields,
									limit = 7,
								).content.items
								if (similar.isNotEmpty()) {
									TvRow(
										title = becauseTemplate.replace("%1\$s", movie.name ?: ""),
										items = similar,
										key = movie.id.toString(),
									)
								} else null
							} catch (e: Exception) {
								Timber.w(e, "Failed to load similar for ${movie.name}")
								null
							}
						}
					}.awaitAll().filterNotNull()
				}

				_uiState.update { it.copy(isLoading = false, rows = rows) }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load suggested movies")
				_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
			}
		}
	}

	fun retry(becauseTemplate: String) {
		folderId?.let {
			_uiState.update { it.copy(isLoading = true, error = null) }
			loadSuggestions(it, becauseTemplate)
		}
	}
}

@Composable
fun SuggestedMoviesScreen(
	viewModel: SuggestedMoviesViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val becauseTemplate = stringResource(R.string.because_you_watched)

	TvScaffold {
		Column(modifier = Modifier.fillMaxSize()) {
			TvHeader(title = uiState.title)

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
						SkeletonCardRow()
						Spacer(modifier = Modifier.height(28.dp))
						SkeletonCardRow()
						Spacer(modifier = Modifier.height(28.dp))
						SkeletonCardRow()
					}
				},
				emptyContent = {
					EmptyState(
						title = stringResource(R.string.lbl_empty),
					)
				},
				errorContent = {
					ErrorState(
						message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
						onRetry = { viewModel.retry(becauseTemplate) },
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
