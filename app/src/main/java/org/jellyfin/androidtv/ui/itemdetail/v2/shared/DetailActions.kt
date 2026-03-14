package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import android.widget.Toast
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaActionChip
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.TrackSelectorDialog
import org.jellyfin.androidtv.ui.playback.PrePlaybackTrackSelector
import org.jellyfin.androidtv.util.sdk.compat.canResume
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaStreamType
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

/**
 * Cinema Immersive action buttons row.
 * Primary row: VegafoXButton (Play/Resume + Restart).
 * Secondary row: CinemaActionChip icons.
 */
@Composable
fun DetailActionButtonsRow(
	item: BaseItemDto,
	uiState: ItemDetailsUiState,
	playButtonFocusRequester: FocusRequester,
	callbacks: DetailActionCallbacks,
) {
	val context = LocalContext.current
	val hasPlaybackPosition = item.canResume
	val mediaSources = item.mediaSources
	val firstSource = mediaSources?.firstOrNull()
	val audioStreams = firstSource?.mediaStreams?.filter { it.type == MediaStreamType.AUDIO } ?: emptyList()
	val subtitleStreams = firstSource?.mediaStreams?.filter { it.type == MediaStreamType.SUBTITLE } ?: emptyList()
	val hasMultipleVersions = (mediaSources?.size ?: 0) > 1
	val canPlay =
		item.type in
			listOf(
				BaseItemKind.MOVIE,
				BaseItemKind.EPISODE,
				BaseItemKind.VIDEO,
				BaseItemKind.RECORDING,
				BaseItemKind.TRAILER,
				BaseItemKind.MUSIC_VIDEO,
				BaseItemKind.SERIES,
				BaseItemKind.SEASON,
				BaseItemKind.PROGRAM,
				BaseItemKind.MUSIC_ALBUM,
				BaseItemKind.PLAYLIST,
				BaseItemKind.MUSIC_ARTIST,
			)

	// Dialog state
	var showAudioDialog by remember { mutableStateOf(false) }
	var showSubtitleDialog by remember { mutableStateOf(false) }
	var showVersionDialog by remember { mutableStateOf(false) }

	Column {
		// ─── Primary action buttons ───
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier.focusGroup(),
		) {
			if (hasPlaybackPosition && canPlay) {
				val resumeTime = item.userData?.playbackPositionTicks?.let { formatDuration(it) } ?: ""
				VegafoXButton(
					text = "${stringResource(R.string.lbl_resume)} \u2014 $resumeTime",
					onClick = callbacks.onResume,
					variant = VegafoXButtonVariant.Primary,
					icon = VegafoXIcons.Play,
					iconEnd = false,
					modifier = Modifier.focusRequester(playButtonFocusRequester),
				)
				VegafoXButton(
					text = stringResource(R.string.lbl_restart),
					onClick = callbacks.onPlay,
					variant = VegafoXButtonVariant.Secondary,
					icon = VegafoXIcons.Refresh,
					iconEnd = false,
					compact = true,
				)
			} else if (canPlay) {
				VegafoXButton(
					text = stringResource(R.string.lbl_play),
					onClick = callbacks.onPlay,
					variant = VegafoXButtonVariant.Primary,
					icon = VegafoXIcons.Play,
					iconEnd = false,
					modifier = Modifier.focusRequester(playButtonFocusRequester),
				)
			}

			if ((item.isFolder == true || item.type == BaseItemKind.MUSIC_ARTIST) && item.type != BaseItemKind.BOX_SET) {
				VegafoXButton(
					text = stringResource(R.string.lbl_shuffle_all),
					onClick = callbacks.onShuffle,
					variant = VegafoXButtonVariant.Secondary,
					icon = VegafoXIcons.Shuffle,
					iconEnd = false,
					compact = true,
				)
			}

			if (item.type == BaseItemKind.MUSIC_ARTIST) {
				VegafoXButton(
					text = stringResource(R.string.lbl_instant_mix),
					onClick = callbacks.onPlayInstantMix,
					variant = VegafoXButtonVariant.Secondary,
					icon = VegafoXIcons.Mix,
					iconEnd = false,
					compact = true,
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		// ─── Secondary action chips ───
		Row(
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			modifier = Modifier.focusGroup(),
		) {
			if (hasMultipleVersions) {
				CinemaActionChip(
					icon = VegafoXIcons.Guide,
					label = stringResource(R.string.select_version),
					onClick = { showVersionDialog = true },
				)
			}

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

			if (callbacks.hasPlayableTrailers) {
				CinemaActionChip(
					icon = VegafoXIcons.Trailer,
					label = stringResource(R.string.lbl_trailers),
					onClick = callbacks.onPlayTrailers,
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
					onClick = callbacks.onToggleFavorite,
					isActive = item.userData?.isFavorite == true,
					activeColor = VegafoXColors.Error,
				)
			}

			if (item.userData != null && item.type != BaseItemKind.PERSON && item.type != BaseItemKind.MUSIC_ARTIST) {
				CinemaActionChip(
					icon = VegafoXIcons.Visibility,
					label =
						if (item.userData?.played == true) {
							stringResource(R.string.lbl_watched)
						} else {
							stringResource(R.string.lbl_unwatched)
						},
					onClick = callbacks.onToggleWatched,
					isActive = item.userData?.played == true,
					activeColor = VegafoXColors.Info,
				)
			}

			if (item.userData != null && item.type != BaseItemKind.PERSON) {
				CinemaActionChip(
					icon = VegafoXIcons.Add,
					label = stringResource(R.string.lbl_playlist),
					onClick = callbacks.onAddToPlaylist,
				)
			}

			val goToSeries = callbacks.onGoToSeries
			if (item.type == BaseItemKind.EPISODE && item.seriesId != null && goToSeries != null) {
				CinemaActionChip(
					icon = VegafoXIcons.Tv,
					label = stringResource(R.string.lbl_goto_series),
					onClick = goToSeries,
				)
			}

			if (item.canDelete == true) {
				CinemaActionChip(
					icon = VegafoXIcons.Delete,
					label = stringResource(R.string.lbl_delete),
					onClick = callbacks.onConfirmDelete,
					activeColor = VegafoXColors.Error,
				)
			}
		}
	}

	// Audio track selector dialog
	if (showAudioDialog) {
		val trackSelector = callbacks.trackSelector
		val audioTracks = trackSelector.getAudioTracks(item)
		if (audioTracks.isEmpty()) {
			LaunchedEffect(Unit) {
				Toast.makeText(context, context.getString(R.string.playback_no_audio_tracks), Toast.LENGTH_SHORT).show()
				showAudioDialog = false
			}
		} else {
			val selectedAudioIndex = trackSelector.getSelectedAudioTrack(item.id.toString())
			val trackNames = audioTracks.map { trackSelector.getAudioTrackDisplayName(it) } + listOf(stringResource(R.string.lbl_default))
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

	// Subtitle track selector dialog
	if (showSubtitleDialog) {
		val trackSelector = callbacks.trackSelector
		val subtitleTracks = trackSelector.getSubtitleTracks(item)
		val selectedSubIndex = trackSelector.getSelectedSubtitleTrack(item.id.toString())
		val noneLabel = stringResource(R.string.lbl_none)
		val defaultLabel = stringResource(R.string.lbl_default)
		val trackNames = listOf(noneLabel) + subtitleTracks.map { trackSelector.getSubtitleTrackDisplayName(it) } + listOf(defaultLabel)
		val checkedIndex =
			when {
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
					callbacks.onLoadItem(sourceUUID)
				}
				showVersionDialog = false
			},
			onDismiss = { showVersionDialog = false },
		)
	}
}
