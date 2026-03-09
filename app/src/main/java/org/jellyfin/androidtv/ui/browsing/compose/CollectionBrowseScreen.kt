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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.ui.base.JellyfinTheme
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
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber
import java.util.UUID

data class CollectionBrowseUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val title: String = "",
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class CollectionBrowseViewModel(
	val api: ApiClient,
) : ViewModel() {

	private val _uiState = MutableStateFlow(CollectionBrowseUiState())
	val uiState: StateFlow<CollectionBrowseUiState> = _uiState.asStateFlow()

	private var folderId: UUID? = null

	fun initialize(folderJson: String, moviesLabel: String, seriesLabel: String, otherLabel: String) {
		val folder = Json.decodeFromString<BaseItemDto>(folderJson)
		folderId = folder.id
		_uiState.update { it.copy(title = folder.name ?: "", isLoading = true, error = null) }
		loadRows(folder.id, moviesLabel, seriesLabel, otherLabel)
	}

	private fun loadRows(parentId: UUID, moviesLabel: String, seriesLabel: String, otherLabel: String) {
		viewModelScope.launch {
			try {
				val rows = mutableListOf<TvRow<BaseItemDto>>()

				val movies = withContext(Dispatchers.IO) {
					api.itemsApi.getItems(
						parentId = parentId,
						includeItemTypes = setOf(BaseItemKind.MOVIE),
						fields = ItemRepository.itemFields,
						recursive = true,
					).content.items
				}
				if (movies.isNotEmpty()) {
					rows.add(TvRow(title = moviesLabel, items = movies))
				}

				val series = withContext(Dispatchers.IO) {
					api.itemsApi.getItems(
						parentId = parentId,
						includeItemTypes = setOf(BaseItemKind.SERIES),
						fields = ItemRepository.itemFields,
						recursive = true,
					).content.items
				}
				if (series.isNotEmpty()) {
					rows.add(TvRow(title = seriesLabel, items = series))
				}

				val others = withContext(Dispatchers.IO) {
					api.itemsApi.getItems(
						parentId = parentId,
						excludeItemTypes = setOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
						fields = ItemRepository.itemFields,
						recursive = true,
					).content.items
				}
				if (others.isNotEmpty()) {
					rows.add(TvRow(title = otherLabel, items = others))
				}

				_uiState.update { it.copy(isLoading = false, rows = rows) }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load collection")
				_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
			}
		}
	}

	fun retry(moviesLabel: String, seriesLabel: String, otherLabel: String) {
		folderId?.let { loadRows(it, moviesLabel, seriesLabel, otherLabel) }
	}
}

@Composable
fun CollectionBrowseScreen(
	viewModel: CollectionBrowseViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val moviesLabel = stringResource(R.string.lbl_movies)
	val seriesLabel = stringResource(R.string.lbl_tv_series)
	val otherLabel = stringResource(R.string.lbl_other)

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
						onRetry = { viewModel.retry(moviesLabel, seriesLabel, otherLabel) },
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
