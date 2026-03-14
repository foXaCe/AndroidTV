package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonBox
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.DialogDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvCardGrid
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.base.tv.TvHeader
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.androidtv.ui.browsing.genre.JellyfinGenreItem
import org.jellyfin.androidtv.ui.browsing.v2.GenreSortOption
import org.jellyfin.androidtv.ui.browsing.v2.GenresGridViewModel
import org.jellyfin.androidtv.ui.browsing.v2.LibraryToolbarButton
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.design.Tokens
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

/**
 * Compose TV screen for the genre grid browser.
 *
 * Layout:
 * - TvScaffold with overscan safe area
 * - TvHeader: title + genre count subtitle + toolbar actions (home, sort, filter)
 * - TvCardGrid 5 columns of GenreCards (TvFocusCard + backdrop + gradient + title)
 * - StateContainer: loading skeleton / empty / error / content
 */
@Composable
fun GenresGridScreen(
	viewModel: GenresGridViewModel,
	showLibraryFilter: Boolean,
	onGenreClick: (JellyfinGenreItem) -> Unit,
	onGenreFocus: (JellyfinGenreItem) -> Unit,
	onHomeClick: () -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	var showSortDialog by remember { mutableStateOf(false) }
	var showLibraryDialog by remember { mutableStateOf(false) }

	val displayState =
		when {
			uiState.isLoading -> DisplayState.LOADING
			uiState.error != null && uiState.genres.isEmpty() -> DisplayState.ERROR
			uiState.genres.isEmpty() -> DisplayState.EMPTY
			else -> DisplayState.CONTENT
		}

	TvScaffold {
		Column(modifier = Modifier.fillMaxSize()) {
			TvHeader(
				title = uiState.title,
				subtitle =
					if (uiState.totalGenres > 0) {
						stringResource(R.string.lbl_genres_count, uiState.totalGenres)
					} else {
						null
					},
			) {
				LibraryToolbarButton(
					icon = VegafoXIcons.Home,
					contentDescription = stringResource(R.string.home),
					onClick = onHomeClick,
				)
				LibraryToolbarButton(
					icon = VegafoXIcons.Sort,
					contentDescription = stringResource(R.string.lbl_sort_by),
					onClick = { showSortDialog = true },
				)
				if (uiState.libraries.size > 1 && showLibraryFilter) {
					LibraryToolbarButton(
						icon = VegafoXIcons.Filter,
						contentDescription = stringResource(R.string.lbl_filter_by_library),
						isActive = uiState.selectedLibraryId != null,
						onClick = { showLibraryDialog = true },
					)
				}
			}

			Spacer(modifier = Modifier.height(Tokens.Space.spaceMd))

			StateContainer(
				state = displayState,
				modifier = Modifier.weight(1f),
				loadingContent = {
					// Skeleton: 20 cards 180×100dp in 5 columns
					LazyVerticalGrid(
						columns = GridCells.Fixed(5),
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(bottom = Tokens.Space.spaceLg),
						horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
						verticalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
						userScrollEnabled = false,
					) {
						items(20) {
							SkeletonBox(
								modifier =
									Modifier
										.width(180.dp)
										.height(100.dp),
								shape = JellyfinTheme.shapes.small,
							)
						}
					}
				},
				emptyContent = {
					EmptyState(title = stringResource(R.string.state_empty_genres))
				},
				errorContent = {
					ErrorState(
						message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
						onRetry = { viewModel.retry() },
					)
				},
				content = {
					TvCardGrid(
						items = uiState.genres,
						columns = 5,
						contentPadding = PaddingValues(bottom = Tokens.Space.spaceLg),
						key = { it.id },
					) { genre ->
						GenreCard(
							genre = genre,
							onClick = { onGenreClick(genre) },
							onFocused = { onGenreFocus(genre) },
						)
					}
				},
			)
		}
	}

	// Sort dialog
	if (showSortDialog) {
		GenreSortDialog(
			currentSort = uiState.currentSort,
			sortOptions = viewModel.sortOptions,
			onSortSelected = {
				viewModel.setSortOption(it)
				showSortDialog = false
			},
			onDismiss = { showSortDialog = false },
		)
	}

	// Library filter dialog
	if (showLibraryDialog) {
		LibraryFilterDialog(
			libraries = uiState.libraries,
			selectedLibraryId = uiState.selectedLibraryId,
			libraryServerNames = uiState.libraryServerNames,
			onLibrarySelected = {
				viewModel.setLibraryFilter(it)
				showLibraryDialog = false
			},
			onDismiss = { showLibraryDialog = false },
		)
	}
}

// ──────────────────────────────────────────────
// Genre Card
// ──────────────────────────────────────────────

@Composable
private fun GenreCard(
	genre: JellyfinGenreItem,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	modifier: Modifier = Modifier,
) {
	TvFocusCard(
		onClick = onClick,
		modifier =
			modifier
				.fillMaxWidth()
				.height(120.dp)
				.onFocusChanged { if (it.isFocused) onFocused() },
		shape = JellyfinTheme.shapes.small,
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			// Backdrop image
			if (genre.backdropUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = genre.backdropUrl,
					contentDescription = genre.name,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}

			// Gradient overlay at bottom for text readability
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.background(
							Brush.verticalGradient(
								0.4f to Color.Transparent,
								1.0f to VegafoXColors.BackgroundDeep.copy(alpha = 0.80f),
							),
						),
			)

			// Genre name centered at bottom
			Text(
				text = genre.name,
				style = JellyfinTheme.typography.titleMedium,
				color = VegafoXColors.TextPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier =
					Modifier
						.align(Alignment.BottomCenter)
						.padding(horizontal = 12.dp, vertical = 8.dp),
			)
		}
	}
}

// ──────────────────────────────────────────────
// Genre Sort Dialog
// ──────────────────────────────────────────────

@Composable
private fun GenreSortDialog(
	currentSort: GenreSortOption,
	sortOptions: List<GenreSortOption>,
	onSortSelected: (GenreSortOption) -> Unit,
	onDismiss: () -> Unit,
) {
	val initialFocusRequester = remember { FocusRequester() }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = DialogDimensions.standardMinWidth, max = DialogDimensions.standardMaxWidth)
						.clip(JellyfinTheme.shapes.dialog)
						.background(VegafoXColors.Surface)
						.border(1.dp, VegafoXColors.Outline, JellyfinTheme.shapes.dialog)
						.padding(vertical = 20.dp),
			) {
				Text(
					text = stringResource(R.string.lbl_sort_genres),
					style =
						JellyfinTheme.typography.titleLarge.copy(
							fontSize = 18.sp,
							fontWeight = FontWeight.Bold,
						),
					color = VegafoXColors.TextPrimary,
					modifier =
						Modifier
							.padding(horizontal = Tokens.Space.spaceLg)
							.padding(bottom = 12.dp),
				)

				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(VegafoXColors.Divider),
				)

				Spacer(modifier = Modifier.height(Tokens.Space.spaceXs))

				LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
					itemsIndexed(sortOptions) { _, option ->
						DialogRadioItem(
							text = stringResource(option.labelRes),
							isSelected = option == currentSort,
							shouldRequestFocus = option == currentSort,
							focusRequester = if (option == currentSort) initialFocusRequester else null,
							onClick = { onSortSelected(option) },
						)
					}
				}

				// Action buttons
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(VegafoXColors.Divider),
				)
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
				) {
					VegafoXButton(
						text = stringResource(R.string.lbl_cancel),
						onClick = onDismiss,
						variant = VegafoXButtonVariant.Ghost,
						compact = true,
					)
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}

// ──────────────────────────────────────────────
// Library Filter Dialog
// ──────────────────────────────────────────────

@Composable
private fun LibraryFilterDialog(
	libraries: List<BaseItemDto>,
	selectedLibraryId: UUID?,
	libraryServerNames: Map<UUID, String>,
	onLibrarySelected: (BaseItemDto?) -> Unit,
	onDismiss: () -> Unit,
) {
	val initialFocusRequester = remember { FocusRequester() }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = DialogDimensions.standardMinWidth, max = DialogDimensions.standardMaxWidth)
						.clip(JellyfinTheme.shapes.dialog)
						.background(VegafoXColors.Surface)
						.border(1.dp, VegafoXColors.Outline, JellyfinTheme.shapes.dialog)
						.padding(vertical = 20.dp),
			) {
				Text(
					text = stringResource(R.string.lbl_filter_by_library),
					style =
						JellyfinTheme.typography.titleLarge.copy(
							fontSize = 18.sp,
							fontWeight = FontWeight.Bold,
						),
					color = VegafoXColors.TextPrimary,
					modifier =
						Modifier
							.padding(horizontal = Tokens.Space.spaceLg)
							.padding(bottom = 12.dp),
				)

				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(VegafoXColors.Divider),
				)

				Spacer(modifier = Modifier.height(Tokens.Space.spaceXs))

				LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
					// "All Libraries" option
					item {
						DialogRadioItem(
							text = stringResource(R.string.all_libraries),
							isSelected = selectedLibraryId == null,
							shouldRequestFocus = selectedLibraryId == null,
							focusRequester = if (selectedLibraryId == null) initialFocusRequester else null,
							onClick = { onLibrarySelected(null) },
						)
					}

					// Individual libraries
					itemsIndexed(libraries) { _, library ->
						val isSelected = library.id == selectedLibraryId
						val serverName = libraryServerNames[library.id]
						val displayText =
							if (serverName != null) {
								"${library.name ?: ""} ($serverName)"
							} else {
								library.name ?: ""
							}

						DialogRadioItem(
							text = displayText,
							isSelected = isSelected,
							shouldRequestFocus = isSelected && selectedLibraryId != null,
							focusRequester = if (isSelected && selectedLibraryId != null) initialFocusRequester else null,
							onClick = { onLibrarySelected(library) },
						)
					}
				}

				// Action buttons
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(VegafoXColors.Divider),
				)
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
				) {
					VegafoXButton(
						text = stringResource(R.string.lbl_cancel),
						onClick = onDismiss,
						variant = VegafoXButtonVariant.Ghost,
						compact = true,
					)
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}

// ──────────────────────────────────────────────
// Shared dialog radio item
// ──────────────────────────────────────────────

@Composable
private fun DialogRadioItem(
	text: String,
	isSelected: Boolean,
	shouldRequestFocus: Boolean,
	focusRequester: FocusRequester?,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Row(
		modifier =
			Modifier
				.then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
				.fillMaxWidth()
				.clickable(
					interactionSource = interactionSource,
					indication = null,
				) { onClick() }
				.focusable(interactionSource = interactionSource)
				.background(
					if (isFocused) JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent,
				).padding(horizontal = Tokens.Space.spaceLg, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier =
				Modifier
					.size(18.dp)
					.border(
						width = 2.dp,
						color = if (isSelected) VegafoXColors.OrangePrimary else VegafoXColors.TextHint,
						shape = CircleShape,
					),
			contentAlignment = Alignment.Center,
		) {
			if (isSelected) {
				Box(
					modifier =
						Modifier
							.size(10.dp)
							.background(VegafoXColors.OrangePrimary, CircleShape),
				)
			}
		}

		Spacer(modifier = Modifier.width(Tokens.Space.spaceMd))

		Text(
			text = text,
			style = JellyfinTheme.typography.titleMedium,
			fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400,
			color =
				when {
					isSelected -> VegafoXColors.OrangePrimary
					isFocused -> VegafoXColors.TextPrimary
					else -> VegafoXColors.TextPrimary
				},
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)
	}
}
