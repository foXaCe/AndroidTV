package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
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
	val onPlayTrailers: () -> Unit,
	val onToggleWatched: () -> Unit,
	val onToggleFavorite: () -> Unit,
	val onConfirmDelete: () -> Unit,
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

	// Focus restore after dialog close
	val audioFocusRequester = remember { FocusRequester() }
	val subtitleFocusRequester = remember { FocusRequester() }
	val versionFocusRequester = remember { FocusRequester() }

	Column {
		// ─── Primary action buttons ───
		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier =
				Modifier
					.focusProperties { right = FocusRequester.Cancel }
					.focusGroup(),
		) {
			if (hasPlaybackPosition && canPlay) {
				VegafoXButton(
					text = stringResource(R.string.lbl_resume),
					onClick = callbacks.onResume,
					variant = VegafoXButtonVariant.Primary,
					icon = VegafoXIcons.Play,
					iconEnd = false,
					expandOnFocus = true,
					modifier = Modifier.focusRequester(playButtonFocusRequester),
				)
				VegafoXButton(
					text = stringResource(R.string.lbl_restart),
					onClick = callbacks.onPlay,
					variant = VegafoXButtonVariant.Secondary,
					icon = VegafoXIcons.Refresh,
					iconEnd = false,
					expandOnFocus = true,
				)
			} else if (canPlay) {
				VegafoXButton(
					text = stringResource(R.string.lbl_play),
					onClick = callbacks.onPlay,
					variant = VegafoXButtonVariant.Primary,
					icon = VegafoXIcons.Play,
					iconEnd = false,
					expandOnFocus = true,
					modifier = Modifier.focusRequester(playButtonFocusRequester),
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		// ─── Secondary action buttons (scrollable LazyRow) ───
		val hasMediaGroup = hasMultipleVersions || audioStreams.size > 1 || subtitleStreams.isNotEmpty() || callbacks.hasPlayableTrailers
		val hasStateGroup = item.userData != null
		val goToSeries = callbacks.onGoToSeries
		val hasNavGroup = (item.type == BaseItemKind.EPISODE && item.seriesId != null && goToSeries != null) || item.canDelete == true

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalAlignment = Alignment.CenterVertically,
			contentPadding = PaddingValues(horizontal = 0.dp),
			modifier = Modifier.focusRestorer().focusGroup(),
		) {
			// ── Media group ──
			if (hasMultipleVersions) {
				item {
					VegafoXButton(
						text = stringResource(R.string.select_version),
						onClick = { showVersionDialog = true },
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.Guide,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
						modifier = Modifier.focusRequester(versionFocusRequester),
					)
				}
			}
			if (audioStreams.size > 1) {
				item {
					VegafoXButton(
						text = stringResource(R.string.pref_audio),
						onClick = { showAudioDialog = true },
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.Audiotrack,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
						modifier = Modifier.focusRequester(audioFocusRequester),
					)
				}
			}
			if (subtitleStreams.isNotEmpty()) {
				item {
					VegafoXButton(
						text = stringResource(R.string.pref_subtitles),
						onClick = { showSubtitleDialog = true },
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.Subtitles,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
						modifier = Modifier.focusRequester(subtitleFocusRequester),
					)
				}
			}
			if (callbacks.hasPlayableTrailers) {
				item {
					VegafoXButton(
						text = stringResource(R.string.lbl_trailers),
						onClick = callbacks.onPlayTrailers,
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.Trailer,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
					)
				}
			}

			// ── Separator ──
			if (hasMediaGroup && hasStateGroup) {
				item {
					Box(
						modifier =
							Modifier
								.width(1.dp)
								.height(20.dp)
								.background(VegafoXColors.Divider),
					)
				}
			}

			// ── State group ──
			if (item.userData != null) {
				item {
					VegafoXButton(
						text = stringResource(R.string.lbl_favorite),
						onClick = callbacks.onToggleFavorite,
						variant = VegafoXButtonVariant.Outlined,
						icon = if (item.userData?.isFavorite == true) VegafoXIcons.Favorite else VegafoXIcons.FavoriteOutlined,
						iconEnd = false,
						compact = true,
						iconTint = if (item.userData?.isFavorite == true) VegafoXColors.OrangePrimary else null,
						expandOnFocus = true,
					)
				}
			}
			if (item.userData != null && item.type != BaseItemKind.PERSON && item.type != BaseItemKind.MUSIC_ARTIST) {
				item {
					VegafoXButton(
						text = stringResource(R.string.lbl_watched),
						onClick = callbacks.onToggleWatched,
						variant = VegafoXButtonVariant.Outlined,
						icon = if (item.userData?.played == true) VegafoXIcons.VisibilityOff else VegafoXIcons.Visibility,
						iconEnd = false,
						compact = true,
						iconTint = if (item.userData?.played == true) VegafoXColors.OrangePrimary else null,
						expandOnFocus = true,
					)
				}
			}

			// ── Separator ──
			if (hasStateGroup && hasNavGroup) {
				item {
					Box(
						modifier =
							Modifier
								.width(1.dp)
								.height(20.dp)
								.background(VegafoXColors.Divider),
					)
				}
			}

			// ── Nav group ──
			if (item.type == BaseItemKind.EPISODE && item.seriesId != null && goToSeries != null) {
				item {
					VegafoXButton(
						text = stringResource(R.string.lbl_goto_series),
						onClick = goToSeries,
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.VideoLibrary,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
					)
				}
			}
			if (item.canDelete == true) {
				item {
					VegafoXButton(
						text = stringResource(R.string.lbl_delete),
						onClick = callbacks.onConfirmDelete,
						variant = VegafoXButtonVariant.Outlined,
						icon = VegafoXIcons.Delete,
						iconEnd = false,
						compact = true,
						expandOnFocus = true,
					)
				}
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
	// Restore focus to audio button after dialog closes
	LaunchedEffect(showAudioDialog) {
		if (!showAudioDialog) {
			try {
				audioFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
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
	// Restore focus to subtitle button after dialog closes
	LaunchedEffect(showSubtitleDialog) {
		if (!showSubtitleDialog) {
			try {
				subtitleFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
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
	// Restore focus to version button after dialog closes
	LaunchedEffect(showVersionDialog) {
		if (!showVersionDialog) {
			try {
				versionFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
	}
}
