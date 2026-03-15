package org.jellyfin.androidtv.ui.itemdetail.v2.content

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaActionChip
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaGenreTag
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaPosterColumn
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackSelectorDialog
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCollectionItemsGrid
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailEpisodesHorizontalSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.formatDuration
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.translateGenre
import org.jellyfin.androidtv.ui.shared.components.MediaMetadataBadges
import org.jellyfin.androidtv.util.sdk.compat.canResume
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaStreamType
import java.util.UUID

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Cinema Immersive detail content for Movie, Episode, Video, Recording,
 * Trailer, MusicVideo, and BoxSet types.
 *
 * Layout:
 * - 580dp hero zone: backdrop visible through, genre tag, large title,
 *   metadata pills, ratings, synopsis, action buttons, poster column
 * - Below hero: episodes, cast, similar sections on deep background
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
	val context = LocalContext.current
	val density = LocalDensity.current
	val titleFocusRequester = contentFocusRequester

	val isEpisode = item.type == BaseItemKind.EPISODE
	val isBoxSet = item.type == BaseItemKind.BOX_SET
	val playButtonFocusRequester = remember { FocusRequester() }

	val posterUrl = getPosterUrl(item, api)

	// Playback state
	val hasPlaybackPosition = item.canResume
	val playbackPositionTicks = item.userData?.playbackPositionTicks ?: 0L
	val playedPercentage = item.userData?.playedPercentage ?: 0.0
	val watchedMinutes = (playbackPositionTicks / 10_000_000 / 60).toInt()

	// Genre tag
	val genreTag = buildGenreTag(context, item)

	// Media streams for action chips
	val firstSource = item.mediaSources?.firstOrNull()
	val audioStreams =
		firstSource
			?.mediaStreams
			?.filter { it.type == MediaStreamType.AUDIO } ?: emptyList()
	val subtitleStreams =
		firstSource
			?.mediaStreams
			?.filter { it.type == MediaStreamType.SUBTITLE } ?: emptyList()
	val hasMultipleVersions = (item.mediaSources?.size ?: 0) > 1

	// Dialog state
	var showAudioDialog by remember { mutableStateOf(false) }
	var showSubtitleDialog by remember { mutableStateOf(false) }
	var showVersionDialog by remember { mutableStateOf(false) }

	// Enter animation
	var showContent by remember { mutableStateOf(false) }
	LaunchedEffect(item.id) {
		kotlinx.coroutines.delay(100)
		showContent = true
		kotlinx.coroutines.delay(500)
		try {
			playButtonFocusRequester.requestFocus()
		} catch (_: Exception) {
			try {
				titleFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
	}

	val slideOffsetPx = with(density) { 20.dp.roundToPx() }

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize(),
		) {
			// ═══ HERO ZONE ═══
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(HeroDimensions.backdropHeight),
					contentAlignment = Alignment.CenterStart,
				) {
					AnimatedVisibility(
						visible = showContent,
						enter =
							fadeIn(
								animationSpec =
									tween(
										durationMillis = 350,
										easing = EaseOutCubic,
									),
							) +
								slideInVertically(
									initialOffsetY = { slideOffsetPx },
									animationSpec =
										tween(
											durationMillis = 350,
											easing = EaseOutCubic,
										),
								),
					) {
						Row(
							modifier =
								Modifier
									.fillMaxSize()
									.padding(horizontal = HeroDimensions.horizontalPadding),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(DetailDimensions.actionsSpacing),
						) {
							// ─── Left column ───
							Column(
								modifier = Modifier.weight(1f),
							) {
								// Focusable title area — DPAD anchor for scroll-to-top
								Box(
									modifier =
										Modifier
											.focusRequester(titleFocusRequester)
											.focusable(),
								) {
									Column {
										// Episode sub-header
										if (isEpisode) {
											val seriesName = item.seriesName ?: ""
											val epLabel =
												if (item.parentIndexNumber != null && item.indexNumber != null) {
													"$seriesName  \u2022  S${item.parentIndexNumber}E${item.indexNumber}"
												} else {
													seriesName
												}
											CinemaGenreTag(text = epLabel)
										} else {
											CinemaGenreTag(text = genreTag)
										}

										Spacer(modifier = Modifier.height(12.dp))

										// Title
										Text(
											text = item.name ?: "",
											style =
												JellyfinTheme.typography.displayBold.copy(
													fontSize = HeroDimensions.titleFontSize,
													fontWeight = FontWeight.Black,
													fontFamily = BebasNeue,
													letterSpacing = 2.sp,
												),
											color = VegafoXColors.TextPrimary,
											maxLines = 2,
											overflow = TextOverflow.Ellipsis,
											lineHeight = HeroDimensions.titleLineHeight,
										)
									}
								}

								Spacer(modifier = Modifier.height(16.dp))

								// Metadata badges (year, duration, rating, RT, quality)
								MediaMetadataBadges(item = item)

								Spacer(modifier = Modifier.height(16.dp))

								// Synopsis
								item.overview?.let { overview ->
									Text(
										text = overview,
										style =
											JellyfinTheme.typography.bodyMedium.copy(
												fontSize = 15.sp,
											),
										color = VegafoXColors.TextSecondary,
										maxLines = 3,
										overflow = TextOverflow.Ellipsis,
										lineHeight = 26.sp,
									)
								}

								Spacer(modifier = Modifier.height(32.dp))

								// ─── Primary action buttons ───
								Row(
									horizontalArrangement = Arrangement.spacedBy(12.dp),
									modifier = Modifier.focusGroup(),
								) {
									if (hasPlaybackPosition) {
										val resumeTime = formatDuration(playbackPositionTicks)
										VegafoXButton(
											text = "${stringResource(R.string.lbl_resume)} \u2014 $resumeTime",
											onClick = actionCallbacks.onResume,
											variant = VegafoXButtonVariant.Primary,
											icon = VegafoXIcons.Play,
											iconEnd = false,
											modifier = Modifier.focusRequester(playButtonFocusRequester),
										)
										VegafoXButton(
											text = stringResource(R.string.lbl_restart),
											onClick = actionCallbacks.onPlay,
											variant = VegafoXButtonVariant.Secondary,
											icon = VegafoXIcons.Refresh,
											iconEnd = false,
											compact = true,
										)
									} else {
										VegafoXButton(
											text = stringResource(R.string.lbl_play),
											onClick = { actionCallbacks.onPlay() },
											variant = VegafoXButtonVariant.Primary,
											icon = VegafoXIcons.Play,
											iconEnd = false,
											modifier = Modifier.focusRequester(playButtonFocusRequester),
										)
									}
								}

								Spacer(modifier = Modifier.height(12.dp))

								// ─── Secondary action chips ───
								Row(
									horizontalArrangement = Arrangement.spacedBy(10.dp),
									modifier = Modifier.focusGroup(),
								) {
									if (audioStreams.size > 1) {
										CinemaActionChip(
											icon = VegafoXIcons.Audiotrack,
											label = stringResource(R.string.pref_audio),
											onClick = { showAudioDialog = true },
										)
									}

									if (subtitleStreams.isNotEmpty()) {
										CinemaActionChip(
											icon = VegafoXIcons.Subtitles,
											label = stringResource(R.string.pref_subtitles),
											onClick = { showSubtitleDialog = true },
										)
									}

									if (hasMultipleVersions) {
										CinemaActionChip(
											icon = VegafoXIcons.Guide,
											label = stringResource(R.string.select_version),
											onClick = { showVersionDialog = true },
										)
									}

									if (actionCallbacks.hasPlayableTrailers) {
										CinemaActionChip(
											icon = VegafoXIcons.Trailer,
											label = stringResource(R.string.lbl_trailers),
											onClick = actionCallbacks.onPlayTrailers,
										)
									}

									if (item.userData != null) {
										CinemaActionChip(
											icon = VegafoXIcons.Favorite,
											label =
												if (item.userData?.isFavorite == true) {
													stringResource(R.string.lbl_favorited)
												} else {
													stringResource(R.string.lbl_favorite)
												},
											onClick = actionCallbacks.onToggleFavorite,
											isActive = item.userData?.isFavorite == true,
											activeColor = VegafoXColors.Error,
										)
									}

									if (item.userData != null && item.type != BaseItemKind.PERSON) {
										CinemaActionChip(
											icon = VegafoXIcons.Visibility,
											label =
												if (item.userData?.played == true) {
													stringResource(R.string.lbl_watched)
												} else {
													stringResource(R.string.lbl_unwatched)
												},
											onClick = actionCallbacks.onToggleWatched,
											isActive = item.userData?.played == true,
											activeColor = VegafoXColors.Info,
										)
									}

									if (item.canDelete == true) {
										CinemaActionChip(
											icon = VegafoXIcons.Delete,
											label = stringResource(R.string.lbl_delete),
											onClick = actionCallbacks.onConfirmDelete,
											activeColor = VegafoXColors.Error,
										)
									}
								}
							}

							// ─── Right column: Poster ───
							if (posterUrl != null || playedPercentage > 0) {
								CinemaPosterColumn(
									posterUrl = posterUrl,
									playedPercentage = playedPercentage,
									watchedMinutes = watchedMinutes,
								)
							}
						}
					}
				}
			}

			// ═══ CONTENT BELOW HERO ═══

			// Gradient transition
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(DetailDimensions.gradientHeight)
							.background(
								Brush.verticalGradient(
									colors = listOf(Color.Transparent, VegafoXColors.BackgroundDeep),
								),
							),
				)
			}

			// Episodes section (for EPISODE type — same season)
			if (isEpisode && uiState.episodes.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 16.dp),
					) {
						DetailEpisodesHorizontalSection(
							title =
								item.parentIndexNumber?.let {
									stringResource(R.string.lbl_season_episodes, it)
								} ?: stringResource(R.string.lbl_episodes),
							episodes = uiState.episodes,
							currentEpisodeId = item.id,
							api = api,
							onNavigateToItem = onNavigateToItem,
						)
					}
				}
			}

			// BoxSet collection items
			if (isBoxSet && uiState.collectionItems.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 16.dp),
					) {
						DetailCollectionItemsGrid(
							items = uiState.collectionItems,
							api = api,
							onNavigateToItem = onNavigateToItem,
						)
					}
				}
			}

			// Cast & Crew
			if (uiState.cast.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 24.dp),
					) {
						DetailCastSection(uiState.cast, api, onNavigateToItem)
					}
				}
			}

			// More Like This
			if (uiState.similar.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 24.dp),
					) {
						DetailSectionWithCards(
							title = stringResource(R.string.lbl_more_like_this),
							items = uiState.similar,
							api = api,
							onNavigateToItem = onNavigateToItem,
						)
					}
				}
			}

			// Bottom padding
			item {
				Spacer(
					modifier =
						Modifier
							.height(DetailDimensions.bottomPadding)
							.fillMaxWidth()
							.background(VegafoXColors.BackgroundDeep),
				)
			}
		}
	}

	// ═══ DIALOGS ═══

	// Audio track selector
	if (showAudioDialog) {
		val trackSelector = actionCallbacks.trackSelector
		val audioTracks = trackSelector.getAudioTracks(item)
		if (audioTracks.isEmpty()) {
			LaunchedEffect(Unit) {
				Toast.makeText(context, context.getString(R.string.playback_no_audio_tracks), Toast.LENGTH_SHORT).show()
				showAudioDialog = false
			}
		} else {
			val selectedAudioIndex = trackSelector.getSelectedAudioTrack(item.id.toString())
			val trackNames =
				audioTracks.map { trackSelector.getAudioTrackDisplayName(it) } +
					listOf(stringResource(R.string.lbl_default))
			val checkedIndex =
				audioTracks
					.indexOfFirst { it.index == selectedAudioIndex }
					.let { if (it == -1) trackNames.size - 1 else it }

			TrackSelectorDialog(
				title = stringResource(R.string.lbl_audio_track),
				options = trackNames,
				selectedIndex = checkedIndex,
				onSelect = { which ->
					if (which < audioTracks.size) {
						val track = audioTracks[which]
						trackSelector.setSelectedAudioTrack(item.id.toString(), track.index)
						Toast
							.makeText(
								context,
								context.getString(R.string.playback_audio_track, trackSelector.getAudioTrackDisplayName(track)),
								Toast.LENGTH_SHORT,
							).show()
					} else {
						trackSelector.setSelectedAudioTrack(item.id.toString(), null)
						Toast.makeText(context, context.getString(R.string.playback_audio_default), Toast.LENGTH_SHORT).show()
					}
					showAudioDialog = false
				},
				onDismiss = { showAudioDialog = false },
			)
		}
	}

	// Subtitle track selector
	if (showSubtitleDialog) {
		val trackSelector = actionCallbacks.trackSelector
		val subtitleTracks = trackSelector.getSubtitleTracks(item)
		val selectedSubIndex = trackSelector.getSelectedSubtitleTrack(item.id.toString())
		val noneLabel = stringResource(R.string.lbl_none)
		val defaultLabel = stringResource(R.string.lbl_default)
		val trackNames =
			listOf(noneLabel) +
				subtitleTracks.map { trackSelector.getSubtitleTrackDisplayName(it) } +
				listOf(defaultLabel)
		val checkedIndex =
			when {
				selectedSubIndex == -1 -> 0
				selectedSubIndex == null -> trackNames.size - 1
				else ->
					subtitleTracks
						.indexOfFirst { it.index == selectedSubIndex }
						.let { if (it == -1) trackNames.size - 1 else it + 1 }
			}

		TrackSelectorDialog(
			title = stringResource(R.string.lbl_subtitle_track),
			options = trackNames,
			selectedIndex = checkedIndex,
			onSelect = { which ->
				when (which) {
					0 -> {
						trackSelector.setSelectedSubtitleTrack(item.id.toString(), -1)
						Toast.makeText(context, context.getString(R.string.playback_subtitles_none), Toast.LENGTH_SHORT).show()
					}
					trackNames.size - 1 -> {
						trackSelector.setSelectedSubtitleTrack(item.id.toString(), null)
						Toast.makeText(context, context.getString(R.string.playback_subtitles_default), Toast.LENGTH_SHORT).show()
					}
					else -> {
						val track = subtitleTracks[which - 1]
						trackSelector.setSelectedSubtitleTrack(item.id.toString(), track.index)
						Toast
							.makeText(
								context,
								context.getString(R.string.playback_subtitles_track, trackSelector.getSubtitleTrackDisplayName(track)),
								Toast.LENGTH_SHORT,
							).show()
					}
				}
				showSubtitleDialog = false
			},
			onDismiss = { showSubtitleDialog = false },
		)
	}

	// Version selector
	if (showVersionDialog) {
		val versions = item.mediaSources ?: emptyList()
		val versionNames =
			versions.mapIndexed { i, source ->
				source.name ?: stringResource(R.string.lbl_version_number, i + 1)
			}

		TrackSelectorDialog(
			title = stringResource(R.string.lbl_select_version),
			options = versionNames,
			selectedIndex = versions.indexOfFirst { it.id == item.mediaSources?.firstOrNull()?.id },
			onSelect = { which ->
				val selectedSource = versions[which]
				val sourceId = selectedSource.id
				if (sourceId != null) {
					val sourceUUID = UUID.fromString(sourceId)
					actionCallbacks.onLoadItem(sourceUUID)
				}
				showVersionDialog = false
			},
			onDismiss = { showVersionDialog = false },
		)
	}
}

// ─── Helpers ───

private fun buildGenreTag(
	context: android.content.Context,
	item: org.jellyfin.sdk.model.api.BaseItemDto,
): String {
	val type =
		when (item.type) {
			BaseItemKind.MOVIE -> "FILM"
			BaseItemKind.EPISODE -> "S\u00C9RIE"
			BaseItemKind.SERIES -> "S\u00C9RIE"
			BaseItemKind.RECORDING -> "ENREGISTREMENT"
			BaseItemKind.TRAILER -> "BANDE-ANNONCE"
			BaseItemKind.MUSIC_VIDEO -> "CLIP"
			BaseItemKind.BOX_SET -> "COLLECTION"
			else -> null
		}
	val genres = item.genres?.take(2)?.map { translateGenre(context, it).uppercase() }
	val parts = listOfNotNull(type) + (genres ?: emptyList())
	return parts.joinToString("  \u2022  ")
}
