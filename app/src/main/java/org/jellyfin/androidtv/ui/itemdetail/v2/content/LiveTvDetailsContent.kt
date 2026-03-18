package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.foundation.MutatePriority
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
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.PosterImage
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

/**
 * Detail content for Live TV programs (channel details).
 * Shows program info with recording action buttons.
 */
@Composable
fun LiveTvDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	onPlay: () -> Unit,
	onToggleRecord: () -> Unit,
	onToggleRecordSeries: () -> Unit,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val playButtonFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester

	val posterUrl = getPosterUrl(item, api)

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
							.onKeyEvent { keyEvent ->
								if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
									when (keyEvent.key) {
										Key.DirectionDown -> {
											try {
												playButtonFocusRequester.requestFocus()
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
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Column(
							modifier =
								Modifier
									.weight(1f)
									.padding(end = if (posterUrl != null) 24.dp else 0.dp),
						) {
							// Channel name
							item.channelName?.let { channelName ->
								Text(
									text = channelName,
									style = JellyfinTheme.typography.titleMedium,
									color = JellyfinTheme.colorScheme.textSecondary,
								)
								Spacer(modifier = Modifier.height(4.dp))
							}

							// Program title
							Text(
								text = item.name ?: "",
								style = JellyfinTheme.typography.headlineLargeBold,
								color = JellyfinTheme.colorScheme.onSurface,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis,
								lineHeight = 38.sp,
							)

							Spacer(modifier = Modifier.height(10.dp))

							// Time info
							val startEnd = buildLiveTvTimeInfo(item)
							if (startEnd.isNotEmpty()) {
								Text(
									text = startEnd,
									style = JellyfinTheme.typography.bodyMedium,
									color = JellyfinTheme.colorScheme.textSecondary,
								)
								Spacer(modifier = Modifier.height(8.dp))
							}

							// Overview
							item.overview?.let { overview ->
								Text(
									text = overview,
									style = JellyfinTheme.typography.bodyMedium,
									color = JellyfinTheme.colorScheme.textPrimary,
									lineHeight = 24.sp,
									maxLines = 6,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}

						if (posterUrl != null) {
							PosterImage(
								imageUrl = posterUrl,
								isLandscape = true,
								isSquare = false,
								item = item,
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
							.focusRestorer(playButtonFocusRequester),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					// Play / Tune to Channel
					VegafoXButton(
						text = stringResource(R.string.lbl_tune_to_channel),
						onClick = onPlay,
						variant = VegafoXButtonVariant.Primary,
						icon = VegafoXIcons.Play,
						iconEnd = false,
						modifier = Modifier.focusRequester(playButtonFocusRequester),
					)

					// Record
					if (uiState.programInfo != null) {
						VegafoXButton(
							text = stringResource(R.string.lbl_record),
							onClick = onToggleRecord,
							variant = if (uiState.isRecording) VegafoXButtonVariant.Primary else VegafoXButtonVariant.Secondary,
							icon = VegafoXIcons.Record,
							iconEnd = false,
						)

						// Record Series (only if program is a series)
						if (uiState.programInfo.isSeries == true) {
							VegafoXButton(
								text = stringResource(R.string.lbl_record_series),
								onClick = onToggleRecordSeries,
								variant = if (uiState.isRecordingSeries) VegafoXButtonVariant.Primary else VegafoXButtonVariant.Secondary,
								icon = VegafoXIcons.RecordSeries,
								iconEnd = false,
							)
						}
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
				playButtonFocusRequester.requestFocus()
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

@Composable
private fun buildLiveTvTimeInfo(item: BaseItemDto): String {
	val context = LocalContext.current
	val formatter = context.getTimeFormatter()
	val parts = mutableListOf<String>()
	item.startDate?.let { start ->
		parts.add(formatter.format(start))
	}
	item.endDate?.let { end ->
		parts.add(formatter.format(end))
	}
	return parts.joinToString(" \u2013 ")
}
