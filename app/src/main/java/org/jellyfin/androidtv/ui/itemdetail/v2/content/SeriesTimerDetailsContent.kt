package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.itemdetail.v2.DetailActionButton
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.util.apiclient.getSeriesOverview
import org.jellyfin.sdk.api.client.ApiClient
import java.util.UUID

/**
 * Detail content for Live TV series timer details.
 * Shows timer overview with schedule and management buttons.
 */
@Composable
fun SeriesTimerDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	onCancelSeriesTimer: () -> Unit,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val seriesTimerInfo = uiState.seriesTimerInfo ?: return
	val listState = rememberLazyListState()
	val cancelButtonFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester
	val context = LocalContext.current

	// Build overview from timer info
	val overview = remember(seriesTimerInfo) { seriesTimerInfo.getSeriesOverview(context) }

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			state = listState,
			contentPadding =
				PaddingValues(
					top = HeroDimensions.contentTopPadding,
					start = DetailDimensions.contentPaddingHorizontal,
					end = DetailDimensions.contentPaddingHorizontal,
					bottom = DetailDimensions.contentPaddingHorizontal,
				),
			modifier = Modifier.fillMaxSize(),
		) {
			// Header
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.focusRequester(titleFocusRequester)
							.focusable()
							.onKeyEvent { keyEvent ->
								if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
									when (keyEvent.key) {
										Key.DirectionDown -> {
											try {
												cancelButtonFocusRequester.requestFocus()
												true
											} catch (_: Exception) {
												false
											}
										}

										else -> false
									}
								} else {
									false
								}
							},
				) {
					Column(modifier = Modifier.fillMaxWidth()) {
						// Timer name
						Text(
							text = item.name ?: seriesTimerInfo.name ?: "",
							style = JellyfinTheme.typography.headlineLargeBold,
							color = JellyfinTheme.colorScheme.onSurface,
							maxLines = 2,
							overflow = TextOverflow.Ellipsis,
							lineHeight = 38.sp,
						)

						Spacer(modifier = Modifier.height(10.dp))

						// Overview
						if (overview.isNotBlank()) {
							Text(
								text = overview,
								style = JellyfinTheme.typography.bodyMedium,
								color = JellyfinTheme.colorScheme.textPrimary,
								lineHeight = 24.sp,
							)
						}
					}
				}

				// Action buttons
				Spacer(modifier = Modifier.height(24.dp))
				Row(
					modifier =
						Modifier
							.fillMaxWidth()
							.focusRestorer(cancelButtonFocusRequester),
					horizontalArrangement = Arrangement.Center,
				) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(16.dp),
					) {
						// Cancel Series
						DetailActionButton(
							label = stringResource(R.string.lbl_cancel_series),
							icon = VegafoXIcons.Delete,
							onClick = onCancelSeriesTimer,
							modifier = Modifier.focusRequester(cancelButtonFocusRequester),
						)
					}
				}
			}

			// Schedule items
			if (uiState.scheduleItems.isNotEmpty()) {
				item {
					Spacer(modifier = Modifier.height(24.dp))
					DetailSectionWithCards(
						title = stringResource(R.string.lbl_schedule),
						items = uiState.scheduleItems,
						api = api,
						onNavigateToItem = onNavigateToItem,
					)
				}
			}
		}
	}

	LaunchedEffect(item.id) {
		for (attempt in 1..5) {
			delay(if (attempt == 1) 300L else 200L)
			try {
				cancelButtonFocusRequester.requestFocus()
				delay(16)
				listState.scroll(MutatePriority.UserInput) { scrollBy(0f) }
				listState.scrollToItem(0)
				break
			} catch (_: Exception) {
				// Composable not yet laid out, retry
			}
		}
	}
}
