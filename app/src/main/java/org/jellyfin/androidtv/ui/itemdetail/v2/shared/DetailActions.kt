package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.itemdetail.v2.DetailActionButton
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackSelectorDialog
import org.jellyfin.androidtv.ui.playback.PrePlaybackTrackSelector
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.androidtv.util.sdk.compat.canResume
import java.util.UUID

data class DetailActionCallbacks(
	val trackSelector: PrePlaybackTrackSelector,
	val hasPlayableTrailers: Boolean,
	val onPlay: () -> Unit,
	val onResume: () -> Unit,
	val onShuffle: () -> Unit,
	val onPlayTrailers: () -> Unit,
	val onPlayInstantMix: () -> Unit,
	val onToggleWatched: () -> Unit,
	val onToggleFavorite: () -> Unit,
	val onConfirmDelete: () -> Unit,
	val onAddToPlaylist: () -> Unit,
	val onGoToSeries: (() -> Unit)?,
	val onLoadItem: (UUID) -> Unit,
)

@Composable
fun DetailActionButtonsRow(
	item: BaseItemDto,
	uiState: ItemDetailsUiState,
	playButtonFocusRequester: FocusRequester,
	callbacks: DetailActionCallbacks,
) {
	val trackSelector = callbacks.trackSelector
	val hasPlayableTrailers = callbacks.hasPlayableTrailers
	val onPlay = callbacks.onPlay
	val onResume = callbacks.onResume
	val onShuffle = callbacks.onShuffle
	val onPlayTrailers = callbacks.onPlayTrailers
	val onPlayInstantMix = callbacks.onPlayInstantMix
	val onToggleWatched = callbacks.onToggleWatched
	val onToggleFavorite = callbacks.onToggleFavorite
	val onConfirmDelete = callbacks.onConfirmDelete
	val onAddToPlaylist = callbacks.onAddToPlaylist
	val onGoToSeries = callbacks.onGoToSeries
	val onLoadItem = callbacks.onLoadItem
	val context = LocalContext.current
	val hasPlaybackPosition = item.canResume
	val mediaSources = item.mediaSources
	val firstSource = mediaSources?.firstOrNull()
	val audioStreams = firstSource?.mediaStreams?.filter { it.type == MediaStreamType.AUDIO } ?: emptyList()
	val subtitleStreams = firstSource?.mediaStreams?.filter { it.type == MediaStreamType.SUBTITLE } ?: emptyList()
	val hasMultipleVersions = (mediaSources?.size ?: 0) > 1
	val canPlay = item.type in listOf(
		BaseItemKind.MOVIE, BaseItemKind.EPISODE, BaseItemKind.VIDEO,
		BaseItemKind.RECORDING, BaseItemKind.TRAILER, BaseItemKind.MUSIC_VIDEO,
		BaseItemKind.SERIES, BaseItemKind.SEASON, BaseItemKind.PROGRAM,
		BaseItemKind.MUSIC_ALBUM, BaseItemKind.PLAYLIST, BaseItemKind.MUSIC_ARTIST,
	)

	// Dialog state
	var showAudioDialog by remember { mutableStateOf(false) }
	var showSubtitleDialog by remember { mutableStateOf(false) }
	var showVersionDialog by remember { mutableStateOf(false) }

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(16.dp),
		) {
			if (hasPlaybackPosition && canPlay) {
				val resumeTime = item.userData?.playbackPositionTicks?.let { formatDuration(it) } ?: ""
				DetailActionButton(
					label = stringResource(R.string.lbl_resume),
					icon = ImageVector.vectorResource(R.drawable.ic_play),
					onClick = onResume,
					detail = resumeTime,
					modifier = Modifier.focusRequester(playButtonFocusRequester),
				)
			}

			if (canPlay) {
				DetailActionButton(
					label = if (hasPlaybackPosition) stringResource(R.string.lbl_restart) else stringResource(R.string.lbl_play),
					icon = if (hasPlaybackPosition)
						ImageVector.vectorResource(R.drawable.ic_loop)
					else
						ImageVector.vectorResource(R.drawable.ic_play),
					onClick = onPlay,
					modifier = if (!hasPlaybackPosition) Modifier.focusRequester(playButtonFocusRequester) else Modifier,
				)
			}

			if ((item.isFolder == true || item.type == BaseItemKind.MUSIC_ARTIST) && item.type != BaseItemKind.BOX_SET) {
				DetailActionButton(
					label = stringResource(R.string.lbl_shuffle_all),
					icon = ImageVector.vectorResource(R.drawable.ic_shuffle),
					onClick = onShuffle,
				)
			}

			if (item.type == BaseItemKind.MUSIC_ARTIST) {
				DetailActionButton(
					label = stringResource(R.string.lbl_instant_mix),
					icon = ImageVector.vectorResource(R.drawable.ic_mix),
					onClick = onPlayInstantMix,
				)
			}

			if (hasMultipleVersions) {
				DetailActionButton(
					label = stringResource(R.string.select_version),
					icon = ImageVector.vectorResource(R.drawable.ic_guide),
					onClick = { showVersionDialog = true },
				)
			}

			if (audioStreams.size > 1) {
				DetailActionButton(
					label = stringResource(R.string.pref_audio),
					icon = ImageVector.vectorResource(R.drawable.ic_select_audio),
					onClick = { showAudioDialog = true },
				)
			}

			if (subtitleStreams.isNotEmpty()) {
				DetailActionButton(
					label = stringResource(R.string.pref_subtitles),
					icon = ImageVector.vectorResource(R.drawable.ic_select_subtitle),
					onClick = { showSubtitleDialog = true },
				)
			}

			if (hasPlayableTrailers) {
				DetailActionButton(
					label = stringResource(R.string.lbl_trailers),
					icon = ImageVector.vectorResource(R.drawable.ic_trailer),
					onClick = onPlayTrailers,
				)
			}

			if (item.userData != null && item.type != BaseItemKind.PERSON && item.type != BaseItemKind.MUSIC_ARTIST) {
				DetailActionButton(
					label = if (item.userData?.played == true) stringResource(R.string.lbl_watched) else stringResource(R.string.lbl_unwatched),
					icon = ImageVector.vectorResource(R.drawable.ic_check),
					onClick = onToggleWatched,
					isActive = item.userData?.played == true,
					activeColor = JellyfinTheme.colorScheme.info,
				)
			}

			if (item.userData != null) {
				DetailActionButton(
					label = if (item.userData?.isFavorite == true) stringResource(R.string.lbl_favorited) else stringResource(R.string.lbl_favorite),
					icon = ImageVector.vectorResource(R.drawable.ic_heart),
					onClick = onToggleFavorite,
					isActive = item.userData?.isFavorite == true,
					activeColor = JellyfinTheme.colorScheme.error,
				)
			}

			if (item.userData != null && item.type != BaseItemKind.PERSON) {
				DetailActionButton(
					label = stringResource(R.string.lbl_playlist),
					icon = ImageVector.vectorResource(R.drawable.ic_add),
					onClick = onAddToPlaylist,
				)
			}

			if (item.type == BaseItemKind.EPISODE && item.seriesId != null && onGoToSeries != null) {
				DetailActionButton(
					label = stringResource(R.string.lbl_goto_series),
					icon = ImageVector.vectorResource(R.drawable.ic_tv),
					onClick = onGoToSeries,
				)
			}

			if (item.canDelete == true) {
				DetailActionButton(
					label = stringResource(R.string.lbl_delete),
					icon = ImageVector.vectorResource(R.drawable.ic_delete),
					onClick = onConfirmDelete,
				)
			}
		}
	}

	// Audio track selector dialog
	if (showAudioDialog) {
		val audioTracks = trackSelector.getAudioTracks(item)
		if (audioTracks.isEmpty()) {
			LaunchedEffect(Unit) {
				Toast.makeText(context, context.getString(R.string.playback_no_audio_tracks), Toast.LENGTH_SHORT).show()
				showAudioDialog = false
			}
		} else {
			val selectedAudioIndex = trackSelector.getSelectedAudioTrack(item.id.toString())
			val trackNames = audioTracks.map { trackSelector.getAudioTrackDisplayName(it) } + listOf(stringResource(R.string.lbl_default))
			val checkedIndex = audioTracks.indexOfFirst { it.index == selectedAudioIndex }
				.let { if (it == -1) trackNames.size - 1 else it }

			TrackSelectorDialog(
				title = stringResource(R.string.lbl_audio_track),
				options = trackNames,
				selectedIndex = checkedIndex,
				onSelect = { which ->
					if (which < audioTracks.size) {
						val track = audioTracks[which]
						trackSelector.setSelectedAudioTrack(item.id.toString(), track.index)
						Toast.makeText(context, context.getString(R.string.playback_audio_track, trackSelector.getAudioTrackDisplayName(track)), Toast.LENGTH_SHORT).show()
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

	// Subtitle track selector dialog
	if (showSubtitleDialog) {
		val subtitleTracks = trackSelector.getSubtitleTracks(item)
		val selectedSubIndex = trackSelector.getSelectedSubtitleTrack(item.id.toString())
		val noneLabel = stringResource(R.string.lbl_none)
		val defaultLabel = stringResource(R.string.lbl_default)
		val trackNames = listOf(noneLabel) + subtitleTracks.map { trackSelector.getSubtitleTrackDisplayName(it) } + listOf(defaultLabel)
		val checkedIndex = when {
			selectedSubIndex == -1 -> 0
			selectedSubIndex == null -> trackNames.size - 1
			else -> subtitleTracks.indexOfFirst { it.index == selectedSubIndex }.let { if (it == -1) trackNames.size - 1 else it + 1 }
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
						Toast.makeText(context, context.getString(R.string.playback_subtitles_track, trackSelector.getSubtitleTrackDisplayName(track)), Toast.LENGTH_SHORT).show()
					}
				}
				showSubtitleDialog = false
			},
			onDismiss = { showSubtitleDialog = false },
		)
	}

	// Version selector dialog
	if (showVersionDialog) {
		val versions = item.mediaSources ?: emptyList()
		val versionNames = versions.mapIndexed { i, source -> source.name ?: stringResource(R.string.lbl_version_number, i + 1) }

		TrackSelectorDialog(
			title = stringResource(R.string.lbl_select_version),
			options = versionNames,
			selectedIndex = versions.indexOfFirst { it.id == item.mediaSources?.firstOrNull()?.id },
			onSelect = { which ->
				val selectedSource = versions[which]
				val sourceId = selectedSource.id
				if (sourceId != null) {
					val sourceUUID = UUID.fromString(sourceId)
					onLoadItem(sourceUUID)
				}
				showVersionDialog = false
			},
			onDismiss = { showVersionDialog = false },
		)
	}
}
