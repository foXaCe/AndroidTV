package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.state.UiError
import org.jellyfin.androidtv.ui.base.state.toUiError
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvRow
import org.jellyfin.androidtv.ui.base.tv.TvRowList
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemSortBy
import timber.log.Timber
import java.util.UUID

data class ByLetterBrowseUiState(
	val isLoading: Boolean = true,
	val error: UiError? = null,
	val title: String = "",
	val rows: List<TvRow<BaseItemDto>> = emptyList(),
)

class ByLetterBrowseViewModel(
	val api: ApiClient,
) : ViewModel() {
	private val _uiState = MutableStateFlow(ByLetterBrowseUiState())
	val uiState: StateFlow<ByLetterBrowseUiState> = _uiState.asStateFlow()

	private var folderId: UUID? = null
	private var includeType: String? = null

	fun initialize(
		folderJson: String,
		includeType: String?,
		letters: String,
	) {
		val folder = Json.decodeFromString<BaseItemDto>(folderJson)
		folderId = folder.id
		this.includeType = includeType
		_uiState.update { it.copy(title = folder.name ?: "", isLoading = true, error = null) }
		loadLetterRows(folder.id, includeType, letters)
	}

	private fun loadLetterRows(
		parentId: UUID,
		includeType: String?,
		letters: String,
	) {
		viewModelScope.launch {
			try {
				val itemTypes =
					includeType
						?.let { BaseItemKind.fromNameOrNull(it) }
						?.let { setOf(it) }

				val rows = mutableListOf<TvRow<BaseItemDto>>()

				// Load # (items before first letter)
				val firstLetter = letters.first().toString()
				val numbersItems =
					withContext(Dispatchers.IO) {
						api.itemsApi
							.getItems(
								parentId = parentId,
								sortBy = setOf(ItemSortBy.SORT_NAME),
								includeItemTypes = itemTypes,
								nameLessThan = firstLetter,
								recursive = true,
								fields = ItemRepository.itemFields,
							).content.items
					}
				if (numbersItems.isNotEmpty()) {
					rows.add(TvRow(title = "#", items = numbersItems))
				}

				// Load each letter concurrently
				val letterRows =
					withContext(Dispatchers.IO) {
						letters
							.toList()
							.map { letter ->
								async {
									try {
										val items =
											api.itemsApi
												.getItems(
													parentId = parentId,
													sortBy = setOf(ItemSortBy.SORT_NAME),
													includeItemTypes = itemTypes,
													nameStartsWith = letter.toString(),
													recursive = true,
													fields = ItemRepository.itemFields,
												).content.items
										if (items.isNotEmpty()) {
											TvRow(title = letter.toString(), items = items)
										} else {
											null
										}
									} catch (e: Exception) {
										Timber.w(e, "Failed to load items for letter $letter")
										null
									}
								}
							}.awaitAll()
							.filterNotNull()
					}
				rows.addAll(letterRows)

				_uiState.update { it.copy(isLoading = false, rows = rows) }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load items by letter")
				_uiState.update { it.copy(isLoading = false, error = e.toUiError()) }
			}
		}
	}

	fun retry(letters: String) {
		folderId?.let {
			_uiState.update { it.copy(isLoading = true, error = null) }
			loadLetterRows(it, includeType, letters)
		}
	}
}

@Composable
fun ByLetterBrowseScreen(
	viewModel: ByLetterBrowseViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val letters = stringResource(R.string.byletter_letters)

	TvScaffold {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.background(VegafoXColors.BackgroundDeep),
		) {
			// VegafoX header
			Column(
				modifier =
					Modifier
						.fillMaxWidth()
						.padding(top = 32.dp, start = BrowseDimensions.gridPaddingHorizontal, end = BrowseDimensions.gridPaddingHorizontal),
			) {
				Text(
					text = uiState.title,
					style =
						JellyfinTheme.typography.headlineLarge.copy(
							fontSize = 40.sp,
							fontWeight = FontWeight.Bold,
							fontFamily = BebasNeue,
							letterSpacing = 2.sp,
						),
					color = VegafoXColors.TextPrimary,
				)
			}

			Spacer(modifier = Modifier.height(16.dp))

			val displayState =
				when {
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
						onRetry = { viewModel.retry(letters) },
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
