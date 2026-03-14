package org.jellyfin.androidtv.ui.browsing.v2

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ItemRepository
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.base.state.toUiError
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFilter
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber
import java.util.UUID

@Immutable
data class FolderBrowseRow(
	@StringRes val titleRes: Int = 0,
	val titleOverride: String? = null,
	val items: List<BaseItemDto>,
)

data class FolderBrowseUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val folderName: String = "",
	val rows: List<FolderBrowseRow> = emptyList(),
	val focusedItem: BaseItemDto? = null,
	val isSeason: Boolean = false,
)

class FolderBrowseViewModel(
	private val api: ApiClient,
	private val apiClientFactory: ApiClientFactory,
) : ViewModel() {
	private val _uiState = MutableStateFlow(FolderBrowseUiState())
	val uiState: StateFlow<FolderBrowseUiState> = _uiState.asStateFlow()

	var effectiveApi: ApiClient = api
		private set

	private var folder: BaseItemDto? = null
	private var serverId: UUID? = null

	fun initialize(
		folderJson: String,
		serverId: UUID?,
		userId: UUID?,
	) {
		val folder = Json.decodeFromString(BaseItemDto.serializer(), folderJson)
		this.folder = folder
		this.serverId = serverId

		// Resolve correct API client for multi-server
		if (serverId != null) {
			val serverApi =
				if (userId != null) {
					apiClientFactory.getApiClient(serverId, userId)
				} else {
					apiClientFactory.getApiClientForServer(serverId)
				}
			if (serverApi != null) effectiveApi = serverApi
		}

		val isSeason = folder.type == BaseItemKind.SEASON
		_uiState.value =
			FolderBrowseUiState(
				isLoading = true,
				folderName = folder.name.orEmpty(),
				isSeason = isSeason,
			)

		loadRows()
	}

	fun setFocusedItem(item: BaseItemDto) {
		_uiState.value = _uiState.value.copy(focusedItem = item)
	}

	fun retry() {
		_uiState.value = _uiState.value.copy(error = null, isLoading = true)
		loadRows()
	}

	private fun loadRows() {
		val folder = this.folder ?: return

		// Early return for empty folders that aren't container types
		val isContainerType =
			folder.type in
				setOf(
					BaseItemKind.CHANNEL,
					BaseItemKind.CHANNEL_FOLDER_ITEM,
					BaseItemKind.USER_VIEW,
					BaseItemKind.COLLECTION_FOLDER,
				)
		if ((folder.childCount == null || folder.childCount == 0) && !isContainerType) {
			_uiState.value = _uiState.value.copy(isLoading = false, rows = emptyList())
			return
		}

		viewModelScope.launch {
			_uiState.value = _uiState.value.copy(isLoading = true, error = null)

			try {
				val rows = mutableListOf<FolderBrowseRow>()
				val isSeason = folder.type == BaseItemKind.SEASON
				val showSpecialViews =
					folder.type in
						setOf(
							BaseItemKind.COLLECTION_FOLDER,
							BaseItemKind.FOLDER,
							BaseItemKind.USER_VIEW,
							BaseItemKind.CHANNEL_FOLDER_ITEM,
						)

				// Resume row (not for channel folder items)
				if (showSpecialViews && folder.type != BaseItemKind.CHANNEL_FOLDER_ITEM) {
					val resumeItems =
						withContext(Dispatchers.IO) {
							effectiveApi.itemsApi
								.getItems(
									parentId = folder.id,
									fields = ItemRepository.itemFields,
									limit = 50,
									filters = setOf(ItemFilter.IS_RESUMABLE),
									sortBy = setOf(ItemSortBy.DATE_PLAYED),
									sortOrder = setOf(SortOrder.DESCENDING),
								).content.items
						}
					if (resumeItems.isNotEmpty()) {
						rows.add(
							FolderBrowseRow(
								titleRes = R.string.lbl_continue_watching,
								items = annotateItems(resumeItems),
							),
						)
					}
				}

				// Latest row
				if (showSpecialViews) {
					val latestItems =
						withContext(Dispatchers.IO) {
							effectiveApi.itemsApi
								.getItems(
									parentId = folder.id,
									fields = ItemRepository.itemFields,
									limit = 50,
									filters = setOf(ItemFilter.IS_UNPLAYED),
									sortBy = setOf(ItemSortBy.DATE_CREATED),
									sortOrder = setOf(SortOrder.DESCENDING),
								).content.items
						}
					if (latestItems.isNotEmpty()) {
						rows.add(
							FolderBrowseRow(
								titleRes = R.string.lbl_latest,
								items = annotateItems(latestItems),
							),
						)
					}
				}

				// By Name row (uses folder name for seasons, resource string otherwise)
				val byNameItems =
					withContext(Dispatchers.IO) {
						effectiveApi.itemsApi
							.getItems(
								parentId = folder.id,
								fields = ItemRepository.itemFields,
							).content.items
					}
				if (byNameItems.isNotEmpty()) {
					if (isSeason) {
						rows.add(
							FolderBrowseRow(
								titleOverride = folder.name.orEmpty(),
								items = annotateItems(byNameItems),
							),
						)
					} else {
						rows.add(
							FolderBrowseRow(
								titleRes = R.string.lbl_by_name,
								items = annotateItems(byNameItems),
							),
						)
					}
				}

				// Specials row (only for seasons)
				if (isSeason) {
					try {
						val specialItems =
							withContext(Dispatchers.IO) {
								effectiveApi.userLibraryApi
									.getSpecialFeatures(
										itemId = folder.id,
									).content
							}
						if (specialItems.isNotEmpty()) {
							rows.add(
								FolderBrowseRow(
									titleRes = R.string.lbl_specials,
									items = annotateItems(specialItems),
								),
							)
						}
					} catch (err: ApiClientException) {
						Timber.w(err, "Failed to load specials for season %s", folder.id)
					}
				}

				_uiState.value =
					_uiState.value.copy(
						isLoading = false,
						rows = rows,
						focusedItem = rows.firstOrNull()?.items?.firstOrNull(),
					)
			} catch (err: ApiClientException) {
				Timber.e(err, "Failed to load folder rows")
				_uiState.value = _uiState.value.copy(isLoading = false, error = err.toUiError())
			}
		}
	}

	private fun annotateItems(items: List<BaseItemDto>): List<BaseItemDto> {
		val sid = serverId ?: return items
		return items.map { it.copy(serverId = sid.toString()) }
	}
}
