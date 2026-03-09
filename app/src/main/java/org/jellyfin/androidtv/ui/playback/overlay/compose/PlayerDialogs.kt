package org.jellyfin.androidtv.ui.playback.overlay.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.constant.ZoomMode

// -- 5 list-style dialogs --

@Composable
fun AudioTrackDialog(
	tracks: List<TrackOption>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_audio_track),
		onDismiss = onDismiss,
	) {
		val listState = rememberLazyListState()
		LaunchedEffect(selectedIndex) {
			if (selectedIndex > 0) listState.scrollToItem(selectedIndex)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.heightIn(max = 400.dp),
		) {
			itemsIndexed(tracks) { index, track ->
				PlayerDialogItem(
					text = track.title,
					isSelected = index == selectedIndex,
					onClick = {
						onSelect(track.index)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
fun SubtitleTrackDialog(
	tracks: List<TrackOption>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_subtitle_track),
		onDismiss = onDismiss,
	) {
		val listState = rememberLazyListState()
		LaunchedEffect(selectedIndex) {
			if (selectedIndex > 0) listState.scrollToItem(selectedIndex)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.heightIn(max = 400.dp),
		) {
			itemsIndexed(tracks) { index, track ->
				PlayerDialogItem(
					text = track.title,
					isSelected = index == selectedIndex,
					onClick = {
						onSelect(track.index)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
fun QualityDialog(
	profiles: List<QualityOption>,
	selectedIndex: Int,
	onSelect: (String) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_quality_profile),
		onDismiss = onDismiss,
	) {
		val listState = rememberLazyListState()
		LaunchedEffect(selectedIndex) {
			if (selectedIndex > 0) listState.scrollToItem(selectedIndex)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.heightIn(max = 400.dp),
		) {
			itemsIndexed(profiles) { index, profile ->
				PlayerDialogItem(
					text = profile.label,
					isSelected = index == selectedIndex,
					onClick = {
						onSelect(profile.key)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
fun PlaybackSpeedDialog(
	speeds: List<SpeedOption>,
	selectedIndex: Int,
	onSelect: (Float) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_playback_speed),
		onDismiss = onDismiss,
	) {
		LazyColumn(
			modifier = Modifier.heightIn(max = 400.dp),
		) {
			itemsIndexed(speeds) { index, speed ->
				PlayerDialogItem(
					text = speed.label,
					isSelected = index == selectedIndex,
					onClick = {
						onSelect(speed.speed)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
fun ZoomDialog(
	modes: List<ZoomOption>,
	selectedIndex: Int,
	onSelect: (ZoomMode) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_zoom),
		onDismiss = onDismiss,
	) {
		modes.forEachIndexed { index, option ->
			PlayerDialogItem(
				text = option.label,
				isSelected = index == selectedIndex,
				onClick = {
					onSelect(option.mode)
					onDismiss()
				},
			)
		}
	}
}

// -- 2 stepper dialogs --

@Composable
fun SubtitleDelayDialog(
	currentDelayMs: Long,
	onStep: (Long) -> Unit,
	onReset: () -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_subtitle_delay),
		onDismiss = onDismiss,
	) {
		PlayerDialogStepper(
			title = stringResource(R.string.lbl_subtitle_delay),
			value = formatDelayMs(currentDelayMs),
			onDecrement = { onStep(-DELAY_STEP_MS) },
			onIncrement = { onStep(DELAY_STEP_MS) },
			onReset = onReset,
		)
		Spacer(Modifier.height(16.dp))
	}
}

@Composable
fun AudioDelayDialog(
	currentDelayMs: Long,
	onStep: (Long) -> Unit,
	onReset: () -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_audio_delay),
		onDismiss = onDismiss,
	) {
		PlayerDialogStepper(
			title = stringResource(R.string.lbl_audio_delay),
			value = formatDelayMs(currentDelayMs),
			onDecrement = { onStep(-DELAY_STEP_MS) },
			onIncrement = { onStep(DELAY_STEP_MS) },
			onReset = onReset,
		)
		Spacer(Modifier.height(16.dp))
	}
}

// -- Host composable --

@Composable
fun PlayerPopupHost(
	state: PlayerOverlayState,
	onAction: (PlayerOverlayAction) -> Unit,
) {
	when (val popup = state.openPopup) {
		is PlayerPopup.Audio -> AudioTrackDialog(
			tracks = popup.tracks,
			selectedIndex = popup.selectedIndex,
			onSelect = { onAction(PlayerOverlayAction.SelectAudioTrack(it)) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.Subtitles -> SubtitleTrackDialog(
			tracks = popup.tracks,
			selectedIndex = popup.selectedIndex,
			onSelect = { onAction(PlayerOverlayAction.SelectSubtitleTrack(it)) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.Quality -> QualityDialog(
			profiles = popup.profiles,
			selectedIndex = popup.selectedIndex,
			onSelect = { onAction(PlayerOverlayAction.SelectQuality(it)) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.Speed -> PlaybackSpeedDialog(
			speeds = popup.speeds,
			selectedIndex = popup.selectedIndex,
			onSelect = { onAction(PlayerOverlayAction.SelectSpeed(it)) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.Zoom -> ZoomDialog(
			modes = popup.modes,
			selectedIndex = popup.selectedIndex,
			onSelect = { onAction(PlayerOverlayAction.SelectZoom(it)) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.SubtitleDelay -> SubtitleDelayDialog(
			currentDelayMs = popup.currentDelayMs,
			onStep = { onAction(PlayerOverlayAction.StepSubtitleDelay(it)) },
			onReset = { onAction(PlayerOverlayAction.ResetSubtitleDelay) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		is PlayerPopup.AudioDelay -> AudioDelayDialog(
			currentDelayMs = popup.currentDelayMs,
			onStep = { onAction(PlayerOverlayAction.StepAudioDelay(it)) },
			onReset = { onAction(PlayerOverlayAction.ResetAudioDelay) },
			onDismiss = { onAction(PlayerOverlayAction.DismissPopup) },
		)

		// Chapters, Cast, Channels handled by PlayerPopupView (legacy)
		is PlayerPopup.Chapters,
		is PlayerPopup.Cast,
		is PlayerPopup.Channels,
		is PlayerPopup.Guide,
		is PlayerPopup.Record,
		is PlayerPopup.PreviousChannel,
		is PlayerPopup.None -> Unit
	}
}

private const val DELAY_STEP_MS = 50L

private fun formatDelayMs(delayMs: Long): String = when {
	delayMs == 0L -> "0ms"
	delayMs > 0 -> "+${delayMs}ms"
	else -> "${delayMs}ms"
}
