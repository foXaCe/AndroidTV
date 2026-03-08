package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.ui.itemdetail.v2.DetailActionButton
import org.jellyfin.androidtv.ui.itemdetail.v2.DetailBackdrop
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.SeasonEpisodeItem
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.formatDuration
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getEpisodeThumbnailUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

@Composable
fun SeasonDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	showBackdrop: Boolean,
	api: ApiClient,
	blurAmount: Int,
	onNavigateToItem: (UUID) -> Unit,
	onPlayEpisode: (BaseItemDto) -> Unit,
	onToggleWatched: () -> Unit,
	onToggleFavorite: () -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val playButtonFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester
	val backdropUrl = getBackdropUrl(item, api)
	val posterUrl = getPosterUrl(item, api)

	Box(modifier = Modifier.fillMaxSize()) {
		if (showBackdrop) {
			DetailBackdrop(imageUrl = backdropUrl, blurAmount = blurAmount)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(top = 180.dp, start = 100.dp, end = 100.dp, bottom = 80.dp),
		) {
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
										try {
											playButtonFocusRequester.requestFocus()
										} catch (_: Exception) {
										}
										true
									}

									else -> false
								}
							} else false
						},
				) {
					Row(
						verticalAlignment = Alignment.Bottom,
						modifier = Modifier.padding(bottom = 48.dp),
					) {
						if (posterUrl != null) {
							Box(modifier = Modifier.width(220.dp)) {
								val placeholder = rememberGradientPlaceholder()
								val errorFallback = rememberErrorPlaceholder()
								AsyncImage(
									model = posterUrl,
									contentDescription = null,
									modifier = Modifier
										.fillMaxWidth()
										.background(
											JellyfinTheme.colorScheme.outlineVariant,
											JellyfinTheme.shapes.medium,
										),
									contentScale = ContentScale.FillWidth,
									placeholder = placeholder,
									error = errorFallback,
								)
							}
							Spacer(modifier = Modifier.width(32.dp))
						}

						Column(modifier = Modifier.padding(bottom = 8.dp)) {
							item.seriesName?.let { seriesName ->
								Text(
									text = seriesName,
									style = JellyfinTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W500),
									color = JellyfinTheme.colorScheme.onSurfaceVariant,
								)
								Spacer(modifier = Modifier.height(4.dp))
							}

							Text(
								text = item.name ?: "",
								style = JellyfinTheme.typography.display.copy(fontWeight = FontWeight.W700),
								color = JellyfinTheme.colorScheme.onSurface,
								lineHeight = 55.sp,
							)

							Spacer(modifier = Modifier.height(4.dp))

							Text(
								text = stringResource(
									if (uiState.episodes.size == 1) R.string.lbl_episode_count_singular
									else R.string.lbl_episodes_count,
									uiState.episodes.size,
								),
								style = JellyfinTheme.typography.titleLarge,
								color = JellyfinTheme.colorScheme.textHint,
							)
						}
					}

					if (uiState.episodes.isNotEmpty()) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.Center,
						) {
							Row(
								horizontalArrangement = Arrangement.spacedBy(24.dp),
								modifier = Modifier.focusGroup(),
							) {
								DetailActionButton(
									label = stringResource(R.string.lbl_play),
									icon = ImageVector.vectorResource(R.drawable.ic_play),
									onClick = {
										val unwatched = uiState.episodes.firstOrNull { !(it.userData?.played ?: false) }
										val episode = unwatched ?: uiState.episodes.first()
										onPlayEpisode(episode)
									},
									modifier = Modifier.focusRequester(playButtonFocusRequester),
								)

								DetailActionButton(
									label = if (item.userData?.played == true) stringResource(R.string.lbl_watched)
									else stringResource(R.string.lbl_unwatched),
									icon = ImageVector.vectorResource(R.drawable.ic_check),
									onClick = { onToggleWatched() },
									isActive = item.userData?.played == true,
									activeColor = JellyfinTheme.colorScheme.info,
								)

								DetailActionButton(
									label = if (item.userData?.isFavorite == true) stringResource(R.string.lbl_favorited)
									else stringResource(R.string.lbl_favorite),
									icon = ImageVector.vectorResource(R.drawable.ic_heart),
									onClick = { onToggleFavorite() },
									isActive = item.userData?.isFavorite == true,
									activeColor = JellyfinTheme.colorScheme.error,
								)
							}
						}
					}
				}
			}

			item {
				Spacer(modifier = Modifier.height(36.dp))
			}

			items(uiState.episodes.size) { index ->
				val ep = uiState.episodes[index]
				SeasonEpisodeItem(
					episodeNumber = ep.indexNumber,
					title = ep.name ?: "",
					overview = ep.overview,
					runtime = ep.runTimeTicks?.let { formatDuration(it) },
					imageUrl = getEpisodeThumbnailUrl(ep, api),
					progress = ep.userData?.playedPercentage ?: 0.0,
					isPlayed = ep.userData?.played == true,
					onClick = {
						onNavigateToItem(ep.id)
					},
					modifier = Modifier.padding(bottom = 12.dp),
				)
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
			}
		}
	}
}
