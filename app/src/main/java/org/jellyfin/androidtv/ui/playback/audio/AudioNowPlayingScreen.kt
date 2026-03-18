package org.jellyfin.androidtv.ui.playback.audio

import android.widget.ImageView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.SeekbarColors
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.components.TvIconButton
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.JetBrainsMono
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.composable.LyricsDtoBox
import org.jellyfin.androidtv.ui.composable.modifier.fadingEdges
import org.jellyfin.androidtv.ui.composable.rememberPlayerProgress
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.androidtv.ui.player.base.PlayerSeekbar
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.apiclient.albumPrimaryImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.jellyfin.lyrics
import org.jellyfin.playback.jellyfin.lyricsFlow
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.baseItemFlow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.ImageType
import org.koin.compose.koinInject

private val ArtworkShape = RoundedCornerShape(16.dp)

@Composable
fun AudioNowPlayingScreen(viewModel: AudioNowPlayingViewModel) {
	val uiState by viewModel.uiState.collectAsState()
	val playPauseFocusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		kotlinx.coroutines.delay(150)
		try {
			playPauseFocusRequester.requestFocus()
		} catch (_: Exception) {
		}
	}

	VegafoXScaffold {
		TvScaffold {
			Box(
				modifier =
					Modifier
						.fillMaxSize()
						.background(VegafoXColors.BackgroundDeep),
			) {
				Row(modifier = Modifier.fillMaxSize()) {
					// Left side: Artwork + lyrics
					ArtworkPanel(
						modifier =
							Modifier
								.fillMaxHeight()
								.weight(1f),
					)

					Spacer(modifier = Modifier.width(TvSpacing.sectionGap))

					// Right side: Info + controls + queue
					Column(
						modifier =
							Modifier
								.fillMaxHeight()
								.weight(1.5f),
						verticalArrangement = Arrangement.SpaceBetween,
					) {
						// Artist + genres at top
						Column {
							Text(
								text = uiState.artistName,
								style =
									TextStyle(
										fontSize = 16.sp,
										color = VegafoXColors.TextSecondary,
									),
								maxLines = 1,
							)
							if (uiState.genres.isNotEmpty()) {
								Text(
									text = uiState.genres,
									style =
										TextStyle(
											fontSize = 14.sp,
											color = VegafoXColors.TextHint,
										),
									maxLines = 1,
								)
							}
						}

						// Song info
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier.fillMaxWidth(),
						) {
							Text(
								text = uiState.songTitle,
								style =
									TextStyle(
										fontFamily = BebasNeue,
										fontSize = 36.sp,
										color = VegafoXColors.TextPrimary,
										letterSpacing = 2.sp,
									),
								maxLines = 1,
							)
							if (uiState.albumTitle.isNotEmpty()) {
								Text(
									text = uiState.albumTitle,
									style =
										TextStyle(
											fontSize = 14.sp,
											color = VegafoXColors.TextHint,
										),
									maxLines = 1,
								)
							}
							Text(
								text = uiState.trackInfo,
								style =
									TextStyle(
										fontSize = 14.sp,
										color = VegafoXColors.TextHint,
									),
							)
						}

						// Progress bar with times
						AudioProgressSection(uiState)

						// Transport controls
						AudioControlsRow(viewModel, uiState, playPauseFocusRequester)

						// Queue row
						AudioQueueSection(viewModel, uiState)
					}
				}
			}
		}
	}
}

@Composable
private fun ArtworkPanel(modifier: Modifier = Modifier) {
	val api = koinInject<ApiClient>()
	val playbackManager = koinInject<PlaybackManager>()
	val entry by rememberQueueEntry(playbackManager)
	val baseItem = entry?.run { baseItemFlow.collectAsState(baseItem).value }
	val lyrics = entry?.run { lyricsFlow.collectAsState(lyrics) }?.value
	val cover =
		baseItem?.itemImages?.get(ImageType.PRIMARY)
			?: baseItem?.albumPrimaryImage
			?: baseItem?.parentImages?.get(ImageType.PRIMARY)

	val coverAlpha by animateFloatAsState(
		label = "coverAlpha",
		targetValue = if (lyrics == null) 1f else 0.2f,
	)

	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center,
	) {
		AnimatedContent(cover, label = "artwork") { currentCover ->
			if (currentCover != null) {
				Box(
					modifier =
						Modifier
							.aspectRatio(1f)
							.shadow(24.dp, ArtworkShape)
							.clip(ArtworkShape)
							.background(VegafoXColors.Background)
							.border(1.dp, VegafoXColors.Divider, ArtworkShape),
				) {
					AsyncImage(
						url = currentCover.getUrl(api),
						blurHash = currentCover.blurHash,
						aspectRatio = currentCover.aspectRatio ?: 1f,
						scaleType = ImageView.ScaleType.CENTER_INSIDE,
						modifier = Modifier.alpha(coverAlpha),
					)
				}
			} else if (lyrics == null) {
				Icon(
					VegafoXIcons.Album,
					contentDescription = null,
					tint = VegafoXColors.TextHint,
					modifier = Modifier.size(128.dp),
				)
			}
		}

		// Lyrics overlay
		if (lyrics != null) {
			val playState by remember { playbackManager.state.playState }.collectAsState()
			rememberPlayerProgress(playbackManager)

			LyricsDtoBox(
				lyricDto = lyrics,
				currentTimestamp = playbackManager.state.positionInfo.active,
				duration = playbackManager.state.positionInfo.duration,
				paused = playState != PlayState.PLAYING,
				fontSize = JellyfinTheme.typography.bodySmall.fontSize,
				color = VegafoXColors.TextPrimary,
				modifier =
					Modifier
						.fillMaxSize()
						.fadingEdges(vertical = 50.dp)
						.padding(horizontal = 15.dp),
			)
		}
	}
}

@Composable
private fun AudioProgressSection(uiState: AudioNowPlayingUiState) {
	val playbackManager = koinInject<PlaybackManager>()
	val time = (uiState.currentPosition / 1000L) * 1000L

	Column(modifier = Modifier.fillMaxWidth()) {
		PlayerSeekbar(
			playbackManager = playbackManager,
			colors =
				SeekbarColors(
					backgroundColor = VegafoXColors.Outline,
					bufferColor = VegafoXColors.TextSecondary,
					progressColor = VegafoXColors.OrangePrimary,
					knobColor = VegafoXColors.OrangePrimary,
				),
			modifier =
				Modifier
					.fillMaxWidth()
					.height(4.dp),
		)
		Spacer(modifier = Modifier.height(4.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = TimeUtils.formatMillis(time),
				style =
					TextStyle(
						fontFamily = JetBrainsMono,
						fontSize = 13.sp,
						color = VegafoXColors.TextSecondary,
					),
			)
			if (uiState.duration > 0L) {
				Text(
					text = "-${TimeUtils.formatMillis(uiState.duration - time)}",
					style =
						TextStyle(
							fontFamily = JetBrainsMono,
							fontSize = 13.sp,
							color = VegafoXColors.TextSecondary,
						),
				)
			}
		}
	}
}

@Composable
private fun AudioControlsRow(
	viewModel: AudioNowPlayingViewModel,
	uiState: AudioNowPlayingUiState,
	playPauseFocusRequester: FocusRequester = FocusRequester(),
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically,
	) {
		TvIconButton(
			icon = VegafoXIcons.Shuffle,
			contentDescription = stringResource(R.string.lbl_shuffle_queue),
			onClick = viewModel::toggleShuffle,
			tint = if (uiState.isShuffleMode) VegafoXColors.OrangePrimary else VegafoXColors.TextHint,
		)
		Spacer(modifier = Modifier.width(12.dp))

		TvIconButton(
			icon = VegafoXIcons.SkipPrevious,
			contentDescription = stringResource(R.string.lbl_prev_item),
			onClick = viewModel::previous,
			tint = VegafoXColors.TextSecondary,
		)
		Spacer(modifier = Modifier.width(12.dp))

		// Play/Pause — 72dp circle OrangePrimary, white icon 36dp, glow
		IconButton(
			onClick = viewModel::playPause,
			shape = CircleShape,
			colors =
				ButtonDefaults.colors(
					containerColor = VegafoXColors.OrangePrimary,
					contentColor = VegafoXColors.TextPrimary,
					focusedContainerColor = VegafoXColors.OrangeLight,
					focusedContentColor = VegafoXColors.TextPrimary,
					disabledContainerColor = VegafoXColors.OrangePrimary.copy(alpha = 0.38f),
					disabledContentColor = VegafoXColors.TextDisabled,
				),
			contentPadding = PaddingValues(0.dp),
			modifier =
				Modifier
					.focusRequester(playPauseFocusRequester)
					.size(72.dp)
					.drawBehind {
						drawCircle(
							brush =
								Brush.radialGradient(
									colors =
										listOf(
											VegafoXColors.OrangePrimary.copy(alpha = 0.25f),
											Color.Transparent,
										),
									radius = size.maxDimension * 0.9f,
								),
						)
					},
		) {
			Icon(
				imageVector = if (uiState.isPlaying) VegafoXIcons.Pause else VegafoXIcons.Play,
				contentDescription =
					stringResource(
						if (uiState.isPlaying) R.string.lbl_pause else R.string.lbl_play,
					),
				tint = VegafoXColors.TextPrimary,
				modifier = Modifier.size(36.dp),
			)
		}
		Spacer(modifier = Modifier.width(12.dp))

		TvIconButton(
			icon = VegafoXIcons.SkipNext,
			contentDescription = stringResource(R.string.lbl_next_item),
			onClick = viewModel::next,
			tint = VegafoXColors.TextSecondary,
		)
		Spacer(modifier = Modifier.width(12.dp))

		TvIconButton(
			icon = VegafoXIcons.Loop,
			contentDescription = stringResource(R.string.lbl_repeat),
			onClick = viewModel::toggleRepeat,
			tint = if (uiState.isRepeatMode) VegafoXColors.OrangePrimary else VegafoXColors.TextHint,
		)
		Spacer(modifier = Modifier.width(16.dp))

		if (uiState.canOpenAlbum) {
			TvIconButton(
				icon = VegafoXIcons.Album,
				contentDescription = stringResource(R.string.lbl_open_album),
				onClick = viewModel::openAlbum,
				tint = VegafoXColors.TextSecondary,
			)
			Spacer(modifier = Modifier.width(8.dp))
		}

		if (uiState.canOpenArtist) {
			TvIconButton(
				icon = VegafoXIcons.Person,
				contentDescription = stringResource(R.string.lbl_open_artist),
				onClick = viewModel::openArtist,
				tint = VegafoXColors.TextSecondary,
			)
		}
	}
}

@Composable
private fun AudioQueueSection(
	viewModel: AudioNowPlayingViewModel,
	uiState: AudioNowPlayingUiState,
) {
	Column {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			Text(
				text = stringResource(R.string.current_queue),
				style =
					TextStyle(
						fontFamily = BebasNeue,
						fontSize = 20.sp,
						color = VegafoXColors.TextPrimary,
						letterSpacing = 1.sp,
					),
			)
			Text(
				text = "${uiState.queueItems.size}",
				style =
					TextStyle(
						fontFamily = JetBrainsMono,
						fontSize = 13.sp,
						color = VegafoXColors.TextSecondary,
					),
			)
		}
		Spacer(modifier = Modifier.height(8.dp))

		val listState = rememberLazyListState()
		LazyRow(
			state = listState,
			horizontalArrangement = Arrangement.spacedBy(TvSpacing.cardGap),
			modifier =
				Modifier
					.fillMaxWidth()
					.focusRestorer(),
		) {
			items(
				items = uiState.queueItems,
				key = { it.baseItem.id.toString() + it.isCurrent },
			) { queueItem ->
				AudioQueueCard(
					item = queueItem,
					onClick = { viewModel.playAt(queueItem.entry) },
				)
			}
		}
	}
}

@Composable
private fun AudioQueueCard(
	item: AudioQueueItem,
	onClick: () -> Unit,
) {
	val api = koinInject<ApiClient>()
	val cover =
		item.baseItem.itemImages[ImageType.PRIMARY]
			?: item.baseItem.albumPrimaryImage
			?: item.baseItem.parentImages[ImageType.PRIMARY]

	TvFocusCard(
		onClick = onClick,
	) {
		Column(
			modifier = Modifier.width(140.dp),
		) {
			Box(
				modifier =
					Modifier
						.size(140.dp)
						.clip(JellyfinTheme.shapes.medium)
						.background(VegafoXColors.SurfaceContainer),
				contentAlignment = Alignment.Center,
			) {
				if (cover != null) {
					AsyncImage(
						url = cover.getUrl(api),
						blurHash = cover.blurHash,
						aspectRatio = 1f,
						scaleType = ImageView.ScaleType.CENTER_CROP,
						modifier = Modifier.fillMaxSize(),
					)
				} else {
					Icon(
						VegafoXIcons.Album,
						contentDescription = null,
						tint = VegafoXColors.TextHint,
						modifier = Modifier.size(48.dp),
					)
				}
				if (item.isCurrent) {
					Box(
						modifier =
							Modifier
								.fillMaxSize()
								.background(VegafoXColors.OrangePrimary.copy(alpha = 0.12f)),
					)
				}
			}
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = item.baseItem.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				color =
					if (item.isCurrent) {
						VegafoXColors.OrangePrimary
					} else {
						VegafoXColors.TextPrimary
					},
				maxLines = 1,
			)
			Text(
				text = item.baseItem.artists?.firstOrNull() ?: item.baseItem.albumArtist ?: "",
				style = JellyfinTheme.typography.bodySmall,
				color = VegafoXColors.TextSecondary,
				maxLines = 1,
			)
		}
	}
}
