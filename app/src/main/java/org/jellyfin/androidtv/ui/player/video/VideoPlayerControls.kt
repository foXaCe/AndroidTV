package org.jellyfin.androidtv.ui.player.video

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.rememberPlayerPositionInfo
import org.jellyfin.androidtv.ui.playback.VideoSpeedController
import org.jellyfin.androidtv.ui.playback.overlay.compose.QualityOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.SpeedOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.TrackOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.ZoomOption
import org.jellyfin.androidtv.ui.player.base.PlayerSeekbar
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.core.queue.queue
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun VideoPlayerControls(
	playbackManager: PlaybackManager = koinInject(),
	userPreferences: UserPreferences = koinInject(),
	item: BaseItemDto? = null,
) {
	val playState by playbackManager.state.playState.collectAsState()
	val currentSpeed by playbackManager.state.speed.collectAsState()
	var dialogType by remember { mutableStateOf<VideoPlayerDialogType?>(null) }

	Column(
		verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
	) {
		// Row 1: Time — position (left), remaining (right) — ABOVE seekbar
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			PositionText(playbackManager)
			RemainingText(playbackManager)
		}

		// Row 2: Seekbar with animated height (4dp rest, 8dp focus)
		var seekbarFocused by remember { mutableStateOf(false) }
		val seekbarHeight by animateDpAsState(
			targetValue = if (seekbarFocused) 8.dp else 4.dp,
			animationSpec = tween(AnimationDefaults.DURATION_FAST),
			label = "seekbarHeight",
		)

		PlayerSeekbar(
			playbackManager = playbackManager,
			modifier =
				Modifier
					.fillMaxWidth()
					.height(seekbarHeight)
					.onFocusChanged { seekbarFocused = it.hasFocus || it.isFocused },
		)

		// Row 3: Control buttons
		val focusRequester = remember { FocusRequester() }
		Row(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically,
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(top = 4.dp)
					.focusRestorer()
					.focusGroup(),
		) {
			// ── Primary controls ──
			// Play/Pause (primary, autoFocus)
			PlayerActionBtn(
				icon = if (playState == PlayState.PLAYING) VegafoXIcons.Pause else VegafoXIcons.Play,
				label = stringResource(if (playState == PlayState.PLAYING) R.string.lbl_pause else R.string.lbl_play),
				isPrimary = true,
				onClick = {
					when (playState) {
						PlayState.STOPPED, PlayState.ERROR -> playbackManager.state.play()
						PlayState.PLAYING -> playbackManager.state.pause()
						PlayState.PAUSED -> playbackManager.state.unpause()
					}
				},
				modifier =
					Modifier
						.focusRequester(focusRequester)
						.onVisibilityChanged { focusRequester.requestFocus() },
			)

			// Rewind −10s
			PlayerActionBtn(
				icon = VegafoXIcons.Replay10,
				label = stringResource(R.string.rewind),
				onClick = { playbackManager.state.rewind() },
			)

			// Forward +30s
			PlayerActionBtn(
				icon = VegafoXIcons.Forward30,
				label = stringResource(R.string.fast_forward),
				onClick = { playbackManager.state.fastForward() },
			)

			// Previous (conditional)
			PreviousEntryBtn(playbackManager)

			// Next (conditional)
			NextEntryBtn(playbackManager)

			// ── Divider ──
			Spacer(Modifier.width(8.dp))
			Box(
				modifier =
					Modifier
						.width(1.dp)
						.height(28.dp)
						.background(VegafoXColors.Divider),
			)
			Spacer(Modifier.width(8.dp))

			// ── Secondary controls ──
			// Subtitles — active indicator when a subtitle track is selected
			val subtitleTracks = remember(item) { playbackManager.backend.getSubtitleTracks() }
			val hasActiveSubtitle = subtitleTracks.any { it.isSelected }
			PlayerActionBtn(
				icon = VegafoXIcons.Subtitles,
				label = stringResource(R.string.lbl_subtitle_track),
				isPrimary = hasActiveSubtitle,
				onClick = { dialogType = buildSubtitleDialog(playbackManager) },
			)

			PlayerActionBtn(
				icon = VegafoXIcons.Audiotrack,
				label = stringResource(R.string.lbl_audio_track),
				onClick = { dialogType = buildAudioDialog(playbackManager) },
			)

			PlayerActionBtn(
				icon = VegafoXIcons.ListBulleted,
				label = stringResource(R.string.lbl_chapters),
				onClick = { dialogType = buildChapterDialog(item, playbackManager) },
			)

			PlayerActionBtn(
				icon = VegafoXIcons.Hd,
				label = stringResource(R.string.lbl_quality_profile),
				onClick = { dialogType = buildQualityDialog(userPreferences) },
			)

			PlayerActionBtn(
				icon = VegafoXIcons.Speed,
				label = stringResource(R.string.lbl_playback_speed),
				onClick = { dialogType = buildSpeedDialog(currentSpeed) },
			)

			PlayerActionBtn(
				icon = VegafoXIcons.ZoomOutMap,
				label = stringResource(R.string.lbl_zoom),
				onClick = { dialogType = buildZoomDialog(userPreferences) },
			)
		}
	}

	// Dialog host
	VideoPlayerDialogHost(
		dialogType = dialogType,
		onDismiss = { dialogType = null },
		onSelectSubtitle = { index ->
			if (index < 0) {
				playbackManager.backend.disableSubtitles()
			} else {
				playbackManager.backend.selectSubtitleTrack(index)
			}
			dialogType = null
		},
		onSelectAudio = { index ->
			playbackManager.backend.selectAudioTrack(index)
			dialogType = null
		},
		onSelectChapter = { ticks ->
			val duration = (ticks / 10_000).milliseconds
			playbackManager.state.seek(duration)
			dialogType = null
		},
		onSelectQuality = { bitrateKey ->
			userPreferences[UserPreferences.maxBitrate] = bitrateKey
			dialogType = null
		},
		onSelectSpeed = { speed ->
			playbackManager.state.setSpeed(speed)
			dialogType = null
		},
		onSelectZoom = { mode ->
			userPreferences[UserPreferences.playerZoomMode] = mode
			dialogType = null
		},
	)
}

// ── PlayerActionBtn ─────────────────────────────────────────────────────

@Composable
private fun PlayerActionBtn(
	icon: ImageVector,
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isPrimary: Boolean = false,
	enabled: Boolean = true,
) {
	val colors =
		if (isPrimary) {
			ButtonDefaults.colors(
				containerColor = VegafoXColors.OrangePrimary,
				contentColor = Color.White,
				focusedContainerColor = VegafoXColors.OrangeLight,
				focusedContentColor = Color.White,
			)
		} else {
			ButtonDefaults.colors()
		}

	IconButton(
		onClick = onClick,
		enabled = enabled,
		colors = colors,
		modifier = modifier,
	) {
		Icon(
			imageVector = icon,
			contentDescription = label,
			modifier = if (isPrimary) Modifier.size(28.dp) else Modifier.size(22.dp),
		)
	}
}

// ── Previous / Next (conditional) ───────────────────────────────────────

@Composable
private fun PreviousEntryBtn(playbackManager: PlaybackManager) {
	val entryIndex by playbackManager.queue.entryIndex.collectAsState()
	val coroutineScope = rememberCoroutineScope()

	if (entryIndex > 0) {
		PlayerActionBtn(
			icon = VegafoXIcons.SkipPrevious,
			label = stringResource(R.string.lbl_prev_item),
			onClick = {
				coroutineScope.launch { playbackManager.queue.previous() }
			},
		)
	}
}

@Composable
private fun NextEntryBtn(playbackManager: PlaybackManager) {
	val entryIndex by playbackManager.queue.entryIndex.collectAsState()
	val coroutineScope = rememberCoroutineScope()

	if (entryIndex < playbackManager.queue.estimatedSize - 1) {
		PlayerActionBtn(
			icon = VegafoXIcons.SkipNext,
			label = stringResource(R.string.lbl_next_item),
			onClick = {
				coroutineScope.launch { playbackManager.queue.next() }
			},
		)
	}
}

// ── Time display ────────────────────────────────────────────────────────

private fun Duration.formatted(includeHours: Boolean): String {
	val totalSeconds = toInt(DurationUnit.SECONDS)
	val hours = totalSeconds / 3600
	val minutes = (totalSeconds % 3600) / 60
	val seconds = totalSeconds % 60

	return if (includeHours) {
		"%02d:%02d:%02d".format(hours, minutes, seconds)
	} else {
		"%02d:%02d".format(minutes, seconds)
	}
}

@Composable
private fun PositionText(playbackManager: PlaybackManager) {
	val positionInfo by rememberPlayerPositionInfo(playbackManager, precision = 1.seconds)
	if (positionInfo.duration == Duration.ZERO) return

	val text by remember {
		derivedStateOf {
			val includeHours = positionInfo.duration.inWholeMinutes >= 60
			positionInfo.active.formatted(includeHours)
		}
	}

	Text(
		text = text,
		style =
			TextStyle(
				fontSize = 13.sp,
				fontWeight = FontWeight.Bold,
				fontFamily = FontFamily.Monospace,
				color = VegafoXColors.TextPrimary,
				letterSpacing = 0.5.sp,
			),
	)
}

@Composable
private fun RemainingText(playbackManager: PlaybackManager) {
	val positionInfo by rememberPlayerPositionInfo(playbackManager, precision = 1.seconds)
	if (positionInfo.duration == Duration.ZERO) return

	val text by remember {
		derivedStateOf {
			val remaining = positionInfo.duration - positionInfo.active
			val includeHours = positionInfo.duration.inWholeMinutes >= 60
			"-${remaining.formatted(includeHours)}"
		}
	}

	Text(
		text = text,
		style =
			TextStyle(
				fontSize = 13.sp,
				fontFamily = FontFamily.Monospace,
				color = VegafoXColors.TextSecondary,
				letterSpacing = 0.5.sp,
			),
	)
}

// ── Dialog builders ─────────────────────────────────────────────────────

private fun buildSubtitleDialog(playbackManager: PlaybackManager): VideoPlayerDialogType? {
	val playerTracks = playbackManager.backend.getSubtitleTracks()
	if (playerTracks.isEmpty()) return null

	val tracks =
		buildList {
			// "Off" option at index -1
			add(TrackOption(index = -1, title = "Off"))
			playerTracks.forEach { track ->
				add(
					TrackOption(
						index = track.index,
						title =
							buildString {
								append(track.label ?: track.language?.uppercase() ?: "Track ${track.index + 1}")
								track.codec?.let { append(" ($it)") }
							},
					),
				)
			}
		}
	val selectedIdx =
		if (playerTracks.none { it.isSelected }) {
			0 // "Off" selected
		} else {
			playerTracks.indexOfFirst { it.isSelected } + 1 // +1 for "Off" offset
		}
	return VideoPlayerDialogType.Subtitles(tracks = tracks, selectedIndex = selectedIdx)
}

private fun buildAudioDialog(playbackManager: PlaybackManager): VideoPlayerDialogType? {
	val playerTracks = playbackManager.backend.getAudioTracks()
	if (playerTracks.isEmpty()) return null

	val tracks =
		playerTracks.map { track ->
			TrackOption(
				index = track.index,
				title =
					buildString {
						append(track.label ?: track.language?.uppercase() ?: "Track ${track.index + 1}")
						track.codec?.let { append(" ($it)") }
					},
			)
		}
	val selectedIdx = playerTracks.indexOfFirst { it.isSelected }.coerceAtLeast(0)
	return VideoPlayerDialogType.Audio(tracks = tracks, selectedIndex = selectedIdx)
}

private fun buildChapterDialog(
	item: BaseItemDto?,
	playbackManager: PlaybackManager,
): VideoPlayerDialogType? {
	val chapters = item?.chapters ?: return null
	if (chapters.isEmpty()) return null

	val currentPosMs = playbackManager.state.positionInfo.active.inWholeMilliseconds
	val options =
		chapters.mapIndexed { i, chapter ->
			ChapterOption(
				index = i,
				title = chapter.name ?: "Chapter ${i + 1}",
				startPositionTicks = chapter.startPositionTicks,
			)
		}
	val currentIndex =
		chapters
			.indexOfLast {
				it.startPositionTicks / 10_000 <= currentPosMs
			}.coerceAtLeast(0)
	return VideoPlayerDialogType.Chapters(chapters = options, currentIndex = currentIndex)
}

private fun buildQualityDialog(userPreferences: UserPreferences): VideoPlayerDialogType {
	val profiles =
		listOf(
			QualityOption(key = "200", label = "200 Mbps (Max)"),
			QualityOption(key = "120", label = "4K - 120 Mbps"),
			QualityOption(key = "80", label = "4K - 80 Mbps"),
			QualityOption(key = "40", label = "1080p - 40 Mbps"),
			QualityOption(key = "20", label = "1080p - 20 Mbps"),
			QualityOption(key = "10", label = "720p - 10 Mbps"),
			QualityOption(key = "5", label = "480p - 5 Mbps"),
			QualityOption(key = "2", label = "360p - 2 Mbps"),
		)
	val currentKey = userPreferences[UserPreferences.maxBitrate]
	val selectedIdx = profiles.indexOfFirst { it.key == currentKey }.coerceAtLeast(0)
	return VideoPlayerDialogType.Quality(profiles = profiles, selectedIndex = selectedIdx)
}

private fun buildSpeedDialog(currentSpeed: Float): VideoPlayerDialogType {
	val speeds =
		VideoSpeedController.SpeedSteps.entries.map { step ->
			SpeedOption(
				speed = step.speed,
				label = if (step.speed == 1.0f) "1x (Normal)" else "${step.speed}x",
			)
		}
	val selectedIdx =
		speeds.indexOfFirst { it.speed == currentSpeed }.coerceAtLeast(
			speeds.indexOfFirst { it.speed == 1.0f }.coerceAtLeast(0),
		)
	return VideoPlayerDialogType.Speed(speeds = speeds, selectedIndex = selectedIdx)
}

private fun buildZoomDialog(userPreferences: UserPreferences): VideoPlayerDialogType {
	val modes =
		ZoomMode.entries.map { mode ->
			ZoomOption(
				mode = mode,
				label =
					mode.name
						.replace('_', ' ')
						.lowercase()
						.replaceFirstChar { it.uppercase() },
			)
		}
	val currentZoom = userPreferences[UserPreferences.playerZoomMode]
	val selectedIdx = ZoomMode.entries.indexOf(currentZoom).coerceAtLeast(0)
	return VideoPlayerDialogType.Zoom(modes = modes, selectedIndex = selectedIdx)
}
