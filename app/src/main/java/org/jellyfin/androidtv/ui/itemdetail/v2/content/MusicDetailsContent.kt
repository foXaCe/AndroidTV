package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.graphicsLayer
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
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowMultipleRatings
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.PosterImage
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackAction
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackActionDialog
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackItemCard
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionButtonsRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailInfoRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailMetadataSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailPlaylistHint
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getLogoUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Detail content for MusicAlbum, MusicArtist, and Playlist types.
 * Handles playlist rotating backdrop, track lists with reorder, and albums.
 */
@Composable
fun MusicDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
	onPlayFromHere: (List<UUID>) -> Unit,
	onPlaySingle: (UUID) -> Unit,
	onPlayInstantMix: (BaseItemDto) -> Unit,
	onQueueAudioItem: (BaseItemDto) -> Unit,
	onMovePlaylistItem: (Int, Int) -> Unit,
	onRemoveFromPlaylist: (Int) -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val playButtonFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester

	val isMusicAlbum = item.type == BaseItemKind.MUSIC_ALBUM
	val isPlaylist = item.type == BaseItemKind.PLAYLIST

	val posterUrl = getPosterUrl(item, api)
	val logoUrl = getLogoUrl(item, api)

	// Playlist rotating backdrop state
	var focusedBackdropUrl by remember { mutableStateOf<String?>(null) }
	val playlistBackdropUrls =
		if (isPlaylist) {
			remember(uiState.tracks) {
				uiState.tracks
					.mapNotNull { getBackdropUrl(it, api) }
					.distinct()
					.take(10)
			}
		} else {
			emptyList()
		}
	var playlistBackdropIndex by remember { mutableStateOf(0) }

	if (isPlaylist) {
		LaunchedEffect(playlistBackdropUrls) {
			if (playlistBackdropUrls.size > 1) {
				while (true) {
					delay(8000)
					if (focusedBackdropUrl == null) {
						playlistBackdropIndex = (playlistBackdropIndex + 1) % playlistBackdropUrls.size
					}
				}
			}
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		// Playlist/Artist backdrop
		if (isPlaylist) {
			val displayUrl = focusedBackdropUrl ?: playlistBackdropUrls.getOrNull(playlistBackdropIndex)
			if (displayUrl != null) {
				Crossfade(
					targetState = displayUrl,
					animationSpec = tween(1000),
					label = "playlist_backdrop_slideshow",
				) { url ->
					val placeholder = rememberGradientPlaceholder()
					AsyncImage(
						model = url,
						contentDescription = null,
						modifier =
							Modifier
								.fillMaxSize()
								.graphicsLayer { alpha = 0.6f },
						contentScale = ContentScale.Crop,
						placeholder = placeholder,
					)
					Box(
						modifier =
							Modifier
								.fillMaxSize()
								.background(
									brush =
										androidx.compose.ui.graphics.Brush.verticalGradient(
											colors =
												listOf(
													VegafoXColors.Background.copy(alpha = 0.3f),
													VegafoXColors.Background.copy(alpha = 0.7f),
												),
										),
								),
					)
				}
			} else {
				Box(
					modifier =
						Modifier
							.fillMaxSize()
							.background(
								brush =
									androidx.compose.ui.graphics.Brush.linearGradient(
										colors =
											listOf(
												JellyfinTheme.colorScheme.gradientEnd,
												JellyfinTheme.colorScheme.gradientMid,
												JellyfinTheme.colorScheme.gradientStart,
											),
									),
							),
				)
			}
		}

		// Track action dialog state
		var trackActionIndex by remember { mutableStateOf<Int?>(null) }

		val trackActionIndex2 = trackActionIndex
		if (trackActionIndex2 != null && trackActionIndex2 in uiState.tracks.indices) {
			val actionTrack = uiState.tracks[trackActionIndex2]
			val canRemoveFromPlaylist = isPlaylist && item.canDelete == true
			val actions =
				buildList {
					if (actionTrack.type != BaseItemKind.AUDIO) {
						add(
							TrackAction(
								label = stringResource(R.string.lbl_open),
								onClick = { onNavigateToItem(actionTrack.id) },
							),
						)
					}
					add(
						TrackAction(
							label = stringResource(R.string.lbl_play_from_here),
							onClick = {
								val trackIds = uiState.tracks.subList(trackActionIndex2, uiState.tracks.size).map { it.id }
								onPlayFromHere(trackIds)
							},
						),
					)
					add(
						TrackAction(
							label = stringResource(R.string.lbl_play),
							onClick = { onPlaySingle(actionTrack.id) },
						),
					)
					if (actionTrack.type == BaseItemKind.AUDIO) {
						add(
							TrackAction(
								label = stringResource(R.string.lbl_add_to_queue),
								onClick = { onQueueAudioItem(actionTrack) },
							),
						)
					}
					if (actionTrack.type == BaseItemKind.AUDIO) {
						add(
							TrackAction(
								label = stringResource(R.string.lbl_instant_mix),
								onClick = { onPlayInstantMix(actionTrack) },
							),
						)
					}
					if (canRemoveFromPlaylist && actionTrack.playlistItemId != null) {
						add(
							TrackAction(
								label = stringResource(R.string.lbl_remove_from_playlist),
								onClick = { onRemoveFromPlaylist(trackActionIndex2) },
							),
						)
					}
				}

			TrackActionDialog(
				trackTitle = actionTrack.name ?: "",
				actions = actions,
				onDismiss = { trackActionIndex = null },
			)
		}

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
			// ---- Header + Action buttons ----
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
							modifier = Modifier.weight(1f).padding(end = if (posterUrl != null) 24.dp else 0.dp),
						) {
							if (logoUrl != null) {
								AsyncImage(
									model = logoUrl,
									contentDescription = item.name,
									modifier =
										Modifier
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
								isLandscape = false,
								isSquare = isMusicAlbum || isPlaylist,
								item = item,
							)
						}
					}
				}

				Spacer(modifier = Modifier.height(24.dp))
				Row(
					modifier =
						Modifier
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

			// ---- Metadata or Playlist hint ----
			item {
				Spacer(modifier = Modifier.height(24.dp))
				if (isPlaylist) {
					DetailPlaylistHint()
				} else {
					DetailMetadataSection(item, uiState)
				}
			}

			// ---- Albums (Music Artist) ----
			if (uiState.albums.isNotEmpty()) {
				item {
					DetailSectionWithCards(
						title = stringResource(R.string.lbl_albums),
						items = uiState.albums,
						api = api,
						onNavigateToItem = onNavigateToItem,
					)
				}
			}

			// ---- Tracks (Music Album / Playlist) ----
			if (uiState.tracks.isNotEmpty()) {
				val canReorder = isPlaylist && item.canDelete == true

				item {
					Text(
						text = stringResource(R.string.lbl_tracks),
						style = JellyfinTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W600),
						color = JellyfinTheme.colorScheme.onSurface,
						modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
					)
				}

				items(uiState.tracks.size, key = { uiState.tracks[it].id }) { index ->
					val track = uiState.tracks[index]
					TrackItemCard(
						trackNumber = track.indexNumber ?: (index + 1),
						title = track.name ?: "",
						artist = track.artists?.firstOrNull() ?: track.albumArtist,
						runtime = track.runTimeTicks?.let { TimeUtils.formatMillis(it / 10_000) },
						onClick = { trackActionIndex = index },
						onMenuAction = { onPlaySingle(track.id) },
						onFocused =
							if (isPlaylist) {
								{ focusedBackdropUrl = getBackdropUrl(track, api) }
							} else {
								null
							},
						onMoveUp =
							if (canReorder && index > 0) {
								{ onMovePlaylistItem(index, index - 1) }
							} else {
								null
							},
						onMoveDown =
							if (canReorder && index < uiState.tracks.size - 1) {
								{ onMovePlaylistItem(index, index + 1) }
							} else {
								null
							},
						isFirst = index == 0,
						isLast = index == uiState.tracks.size - 1,
						modifier = Modifier.padding(bottom = 12.dp),
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
						onItemFocused =
							if (isPlaylist) {
								{ focusItem -> focusedBackdropUrl = getBackdropUrl(focusItem, api) }
							} else {
								null
							},
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
