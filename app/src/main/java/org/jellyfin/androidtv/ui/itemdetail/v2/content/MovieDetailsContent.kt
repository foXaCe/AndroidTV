package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowMultipleRatings
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.PosterImage
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionButtonsRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCollectionItemsGrid
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailEpisodesHorizontalSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailInfoRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailMetadataSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getLogoUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Detail content for Movie, Episode, Video, Recording, Trailer, MusicVideo, and BoxSet types.
 */
@Composable
fun MovieDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val playButtonFocusRequester = remember { FocusRequester() }
	val collectionFirstItemFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester

	val isEpisode = item.type == BaseItemKind.EPISODE
	val isBoxSet = item.type == BaseItemKind.BOX_SET

	val posterUrl = getPosterUrl(item, api)
	val logoUrl = getLogoUrl(item, api)

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			state = listState,
			contentPadding = PaddingValues(top = 100.dp, start = 48.dp, end = 48.dp, bottom = 48.dp),
			modifier = Modifier.fillMaxSize(),
		) {
			// ---- Header + Action buttons in same item so they stay together ----
			item {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.focusRequester(titleFocusRequester)
						.focusable()
						.onKeyEvent { keyEvent ->
							if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
								when (keyEvent.key) {
									Key.DirectionDown -> {
										val focused = if (isBoxSet) {
											try { collectionFirstItemFocusRequester.requestFocus(); true } catch (_: Exception) { false }
										} else {
											try { playButtonFocusRequester.requestFocus(); true } catch (_: Exception) { false }
										}
										focused
									}
									else -> false
								}
							} else false
						},
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Column(
							modifier = Modifier.weight(1f).padding(end = if (posterUrl != null) 24.dp else 0.dp),
						) {
							if (isEpisode) {
								Row(verticalAlignment = Alignment.CenterVertically) {
									item.seriesName?.let { seriesName ->
										Text(
											text = seriesName,
											style = JellyfinTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
											color = JellyfinTheme.colorScheme.textSecondary,
										)
									}
									if (item.parentIndexNumber != null && item.indexNumber != null) {
										Spacer(modifier = Modifier.width(8.dp))
										Text(
											text = stringResource(R.string.lbl_season_episode, item.parentIndexNumber ?: 0, item.indexNumber ?: 0),
											style = JellyfinTheme.typography.bodySmall,
											color = JellyfinTheme.colorScheme.textHint,
											modifier = Modifier
												.background(
													JellyfinTheme.colorScheme.outlineVariant,
													JellyfinTheme.shapes.extraSmall,
												)
												.padding(horizontal = 8.dp, vertical = 2.dp),
										)
									}
								}
								Spacer(modifier = Modifier.height(8.dp))
							}

							if (logoUrl != null) {
								AsyncImage(
									model = logoUrl,
									contentDescription = item.name,
									modifier = Modifier
										.width(300.dp)
										.height(80.dp),
									contentScale = ContentScale.Fit,
									alignment = Alignment.CenterStart,
								)
							} else {
								Text(
									text = item.name ?: "",
									style = JellyfinTheme.typography.headlineLargeBold,
									color = JellyfinTheme.colorScheme.onSurface,
									maxLines = 2,
									overflow = TextOverflow.Ellipsis,
									lineHeight = 38.sp,
								)
							}

							Spacer(modifier = Modifier.height(10.dp))
							DetailInfoRow(item, isSeries = false, uiState.badges)
							Spacer(modifier = Modifier.height(6.dp))
							InfoRowMultipleRatings(item = item)
							Spacer(modifier = Modifier.height(10.dp))

							item.taglines?.firstOrNull()?.let { tagline ->
								Text(
									text = "\u201C$tagline\u201D",
									style = JellyfinTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
									color = JellyfinTheme.colorScheme.onSurfaceVariant,
									lineHeight = 22.sp,
								)
								Spacer(modifier = Modifier.height(8.dp))
							}

							item.overview?.let { overview ->
								Text(
									text = overview,
									style = JellyfinTheme.typography.bodyMedium,
									color = JellyfinTheme.colorScheme.textPrimary,
									lineHeight = 24.sp,
									maxLines = 4,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}

						if (posterUrl != null) {
							PosterImage(
								imageUrl = posterUrl,
								isLandscape = isEpisode,
								isSquare = false,
								item = item,
							)
						}
					}
				}

				if (!isBoxSet) {
					Spacer(modifier = Modifier.height(24.dp))
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.focusRestorer(playButtonFocusRequester),
						horizontalArrangement = Arrangement.Center,
					) {
						DetailActionButtonsRow(
							item = item,
							uiState = uiState,
							playButtonFocusRequester = playButtonFocusRequester,
							callbacks = actionCallbacks,
						)
					}
				}
			}

			// ---- Metadata ----
			item {
				Spacer(modifier = Modifier.height(24.dp))
				DetailMetadataSection(item, uiState)
			}

			// ---- Episodes (for episode type) ----
			if (isEpisode && uiState.episodes.isNotEmpty()) {
				item {
					DetailEpisodesHorizontalSection(
						title = item.parentIndexNumber?.let { stringResource(R.string.lbl_season_episodes, it) }
							?: stringResource(R.string.lbl_episodes),
						episodes = uiState.episodes,
						currentEpisodeId = item.id,
						api = api,
						onNavigateToItem = onNavigateToItem,
					)
				}
			}

			// ---- Collection items (BoxSet) ----
			if (isBoxSet && uiState.collectionItems.isNotEmpty()) {
				item {
					DetailCollectionItemsGrid(
						items = uiState.collectionItems,
						api = api,
						onNavigateToItem = onNavigateToItem,
						firstItemFocusRequester = collectionFirstItemFocusRequester,
					)
				}
			}

			// ---- Cast & Crew ----
			if (uiState.cast.isNotEmpty()) {
				item {
					DetailCastSection(uiState.cast, api, onNavigateToItem)
				}
			}

			// ---- More Like This ----
			if (uiState.similar.isNotEmpty()) {
				item {
					DetailSectionWithCards(
						title = stringResource(R.string.lbl_more_like_this),
						items = uiState.similar,
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
				if (!isBoxSet) {
					playButtonFocusRequester.requestFocus()
				} else {
					titleFocusRequester.requestFocus()
				}
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
