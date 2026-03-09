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
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber

data class FolderViewUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class FolderViewViewModel(
	val api: ApiClient,
) : ViewModel() {

	private val _uiState = MutableStateFlow(FolderViewUiState())
	val uiState: StateFlow<FolderViewUiState> = _uiState.asStateFlow()

	fun initialize() {
		_uiState.update { it.copy(isLoading = true, error = null) }
		loadFolders()
	}

	private fun loadFolders() {
		viewModelScope.launch {
			try {
				val rootFolders = withContext(Dispatchers.IO) {
					api.itemsApi.getItems(
						includeItemTypes = setOf(BaseItemKind.FOLDER, BaseItemKind.COLLECTION_FOLDER),
						sortBy = setOf(ItemSortBy.SORT_NAME),
						sortOrder = setOf(SortOrder.ASCENDING),
						fields = ItemRepository.itemFields,
						recursive = false,
					).content.items
				}

				if (rootFolders.isEmpty()) {
					_uiState.update { it.copy(isLoading = false, rows = emptyList()) }
					return@launch
				}

				// Load each folder's contents concurrently
				val rows = withContext(Dispatchers.IO) {
					rootFolders.map { folder ->
						async {
							try {
								val items = api.itemsApi.getItems(
									parentId = folder.id,
									fields = ItemRepository.itemFields,
									sortBy = setOf(ItemSortBy.SORT_NAME),
									sortOrder = setOf(SortOrder.ASCENDING),
									limit = 50,
								).content.items
								if (items.isNotEmpty()) {
									TvRow(
										title = folder.name ?: "Folder",
										items = items,
										key = folder.id.toString(),
									)
								} else null
							} catch (e: Exception) {
								Timber.w(e, "Failed to load folder ${folder.name}")
								null
							}
						}
					}.awaitAll().filterNotNull()
				}

				_uiState.update { it.copy(isLoading = false, rows = rows) }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load folders")
				_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
			}
		}
	}

	fun retry() {
		initialize()
	}
}

@Composable
fun FolderViewScreen(
	viewModel: FolderViewViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val title = stringResource(R.string.lbl_folders)

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
						SkeletonCardRow()
						Spacer(modifier = Modifier.height(28.dp))
						SkeletonCardRow()
					}
				},
				emptyContent = {
					EmptyState(title = stringResource(R.string.msg_no_folders))
				},
				errorContent = {
					ErrorState(
						message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
						onRetry = { viewModel.retry() },
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
