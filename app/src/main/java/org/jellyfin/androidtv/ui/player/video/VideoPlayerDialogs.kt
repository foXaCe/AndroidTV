package org.jellyfin.androidtv.ui.player.video

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.base.theme.DialogDimensions
import org.jellyfin.androidtv.ui.playback.overlay.compose.PlayerDialog
import org.jellyfin.androidtv.ui.playback.overlay.compose.PlayerDialogItem
import org.jellyfin.androidtv.ui.playback.overlay.compose.QualityOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.SpeedOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.TrackOption
import org.jellyfin.androidtv.ui.playback.overlay.compose.ZoomOption

/**
 * Sealed class representing dialog types available in the new video player.
 */
sealed class VideoPlayerDialogType {
	data class Subtitles(
		val tracks: List<TrackOption>,
		val selectedIndex: Int,
	) : VideoPlayerDialogType()

	data class Audio(
		val tracks: List<TrackOption>,
		val selectedIndex: Int,
	) : VideoPlayerDialogType()

	data class Chapters(
		val chapters: List<ChapterOption>,
		val currentIndex: Int,
	) : VideoPlayerDialogType()

	data class Quality(
		val profiles: List<QualityOption>,
		val selectedIndex: Int,
	) : VideoPlayerDialogType()

	data class Speed(
		val speeds: List<SpeedOption>,
		val selectedIndex: Int,
	) : VideoPlayerDialogType()

	data class Zoom(
		val modes: List<ZoomOption>,
		val selectedIndex: Int,
	) : VideoPlayerDialogType()
}

data class ChapterOption(
	val index: Int,
	val title: String,
	val startPositionTicks: Long,
)

/**
 * Host composable that renders the appropriate dialog based on the current dialog type.
 */
@Composable
fun VideoPlayerDialogHost(
	dialogType: VideoPlayerDialogType?,
	onDismiss: () -> Unit,
	onSelectSubtitle: (Int) -> Unit,
	onSelectAudio: (Int) -> Unit,
	onSelectChapter: (Long) -> Unit,
	onSelectQuality: (String) -> Unit,
	onSelectSpeed: (Float) -> Unit,
	onSelectZoom: (ZoomMode) -> Unit,
) {
	when (dialogType) {
		is VideoPlayerDialogType.Subtitles ->
			VideoSubtitleTrackDialog(
				tracks = dialogType.tracks,
				selectedIndex = dialogType.selectedIndex,
				onSelect = onSelectSubtitle,
				onDismiss = onDismiss,
			)
		is VideoPlayerDialogType.Audio ->
			VideoAudioTrackDialog(
				tracks = dialogType.tracks,
				selectedIndex = dialogType.selectedIndex,
				onSelect = onSelectAudio,
				onDismiss = onDismiss,
			)
		is VideoPlayerDialogType.Chapters ->
			VideoChaptersDialog(
				chapters = dialogType.chapters,
				currentIndex = dialogType.currentIndex,
				onSelect = onSelectChapter,
				onDismiss = onDismiss,
			)
		is VideoPlayerDialogType.Quality ->
			VideoQualityDialog(
				profiles = dialogType.profiles,
				selectedIndex = dialogType.selectedIndex,
				onSelect = onSelectQuality,
				onDismiss = onDismiss,
			)
		is VideoPlayerDialogType.Speed ->
			VideoSpeedDialog(
				speeds = dialogType.speeds,
				selectedIndex = dialogType.selectedIndex,
				onSelect = onSelectSpeed,
				onDismiss = onDismiss,
			)
		is VideoPlayerDialogType.Zoom ->
			VideoZoomDialog(
				modes = dialogType.modes,
				selectedIndex = dialogType.selectedIndex,
				onSelect = onSelectZoom,
				onDismiss = onDismiss,
			)
		null -> Unit
	}
}

@Composable
private fun VideoAudioTrackDialog(
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
			modifier = Modifier.heightIn(max = DialogDimensions.maxListHeight),
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
private fun VideoSubtitleTrackDialog(
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
			modifier = Modifier.heightIn(max = DialogDimensions.maxListHeight),
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
private fun VideoChaptersDialog(
	chapters: List<ChapterOption>,
	currentIndex: Int,
	onSelect: (Long) -> Unit,
	onDismiss: () -> Unit,
) {
	PlayerDialog(
		title = stringResource(R.string.lbl_chapters),
		onDismiss = onDismiss,
	) {
		val listState = rememberLazyListState()
		LaunchedEffect(currentIndex) {
			if (currentIndex > 0) listState.scrollToItem(currentIndex)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.heightIn(max = DialogDimensions.maxListHeight),
		) {
			itemsIndexed(chapters) { index, chapter ->
				PlayerDialogItem(
					text = chapter.title,
					isSelected = index == currentIndex,
					onClick = {
						onSelect(chapter.startPositionTicks)
						onDismiss()
					},
				)
			}
		}
	}
}

@Composable
private fun VideoQualityDialog(
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
			modifier = Modifier.heightIn(max = DialogDimensions.maxListHeight),
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
private fun VideoSpeedDialog(
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
			modifier = Modifier.heightIn(max = DialogDimensions.maxListHeight),
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
private fun VideoZoomDialog(
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
