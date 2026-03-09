package org.jellyfin.androidtv.ui.home.compose

import androidx.compose.foundation.layout.Box
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
import org.jellyfin.androidtv.R
import org.jellyfin.design.Tokens
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.tv.TvRowList
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * Home screen — the root browsing experience (Netflix-style).
 *
 * Architecture:
 * ```
 * Box (fullscreen) {
 *     HomeHeroBackdrop(focusedItem)  // crossfade backdrop
 *     Column {
 *         MainToolbar(Home)          // existing toolbar with all features
 *         TvRowList(rows) {          // vertical list of horizontal rows
 *             BrowseMediaCard(item)  // focusable media cards
 *         }
 *     }
 * }
 * ```
 *
 * The hero backdrop changes on D-pad focus: as the user navigates through
 * rows, the background image crossfades (400ms) to the focused item's backdrop.
 */
@Composable
fun HomeScreen(
	viewModel: HomeViewModel,
	onItemClick: (BaseItemDto) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val focusedItem by viewModel.focusedItem.collectAsState()

	Box(modifier = Modifier.fillMaxSize()) {
		// Layer 1: Hero backdrop — full-screen image of the focused item
		HomeHeroBackdrop(
			item = focusedItem,
			api = viewModel.api,
		)

		// Layer 2: Content over the backdrop
		Column(modifier = Modifier.fillMaxSize()) {
			// Top bar: existing MainToolbar with all navigation features
			MainToolbar(activeButton = MainToolbarActiveButton.Home)

			Spacer(modifier = Modifier.height(Tokens.Space.spaceSm))

			// Rows content with state handling
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
							Spacer(modifier = Modifier.height(Tokens.Space.spaceLg))
						}
					}
				},
				emptyContent = {
					EmptyState(title = stringResource(R.string.lbl_empty))
				},
				errorContent = {
					ErrorState(
						message = stringResource(
							uiState.error?.messageRes ?: R.string.state_error_generic
						),
						onRetry = { viewModel.loadRows() },
					)
				},
				content = {
					TvRowList(
						rows = uiState.rows,
						contentPadding = PaddingValues(
							start = Tokens.Space.space3xl,
							top = Tokens.Space.spaceSm,
							bottom = Tokens.Space.spaceLg,
						),
					) { item ->
						BrowseMediaCard(
							item = item,
							api = viewModel.api,
							onFocus = { viewModel.setFocusedItem(item) },
							onClick = { onItemClick(item) },
						)
					}
				},
			)
		}
	}
}
