package org.jellyfin.androidtv.ui.playlist

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.playlistsApi
import org.jellyfin.sdk.model.api.CreatePlaylistDto
import timber.log.Timber
import java.util.UUID

/**
 * Pure Compose dialog for creating a new playlist.
 * Replaces the legacy CreatePlaylistDialogFragment XML dialog.
 *
 * Usage:
 * ```
 * if (showCreatePlaylist) {
 *     CreatePlaylistDialog(
 *         itemId = ...,
 *         api = ...,
 *         onCreated = { showCreatePlaylist = false },
 *         onDismiss = { showCreatePlaylist = false },
 *     )
 * }
 * ```
 */
@Composable
fun CreatePlaylistDialog(
	itemId: UUID,
	api: ApiClient,
	onCreated: () -> Unit,
	onDismiss: () -> Unit,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var playlistName by remember { mutableStateOf("") }
	var isPublic by remember { mutableStateOf(false) }
	var isCreating by remember { mutableStateOf(false) }

	AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		titleContentColor = VegafoXColors.TextPrimary,
		title = {
			Text(
				text = stringResource(R.string.lbl_create_new_playlist),
				fontFamily = BebasNeue,
				fontSize = 24.sp,
				fontWeight = FontWeight.Bold,
				color = VegafoXColors.TextPrimary,
			)
		},
		text = {
			Column {
				// Playlist name input
				OutlinedTextField(
					value = playlistName,
					onValueChange = { playlistName = it },
					label = {
						Text(
							text = stringResource(R.string.lbl_playlist_name),
							color = VegafoXColors.TextSecondary,
						)
					},
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
					colors =
						OutlinedTextFieldDefaults.colors(
							focusedTextColor = VegafoXColors.TextPrimary,
							unfocusedTextColor = VegafoXColors.TextPrimary,
							cursorColor = VegafoXColors.OrangePrimary,
							focusedBorderColor = VegafoXColors.OrangePrimary,
							unfocusedBorderColor = VegafoXColors.Outline,
							focusedLabelColor = VegafoXColors.OrangePrimary,
							unfocusedLabelColor = VegafoXColors.TextSecondary,
						),
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Public toggle
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth(),
				) {
					Text(
						text = stringResource(R.string.lbl_public_playlist),
						color = VegafoXColors.TextPrimary,
						fontSize = 14.sp,
						modifier = Modifier.weight(1f),
					)
					Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
				}
			}
		},
		confirmButton = {
			VegafoXButton(
				text = stringResource(R.string.lbl_create_and_add),
				onClick = {
					if (playlistName.isBlank()) {
						Toast.makeText(context, R.string.msg_enter_playlist_name, Toast.LENGTH_SHORT).show()
						return@VegafoXButton
					}
					isCreating = true
					scope.launch {
						try {
							withContext(Dispatchers.IO) {
								api.playlistsApi.createPlaylist(
									CreatePlaylistDto(
										name = playlistName.trim(),
										ids = listOf(itemId),
										users = emptyList(),
										isPublic = isPublic,
									),
								)
							}
							Toast.makeText(context, R.string.msg_playlist_created, Toast.LENGTH_SHORT).show()
							onCreated()
						} catch (e: Exception) {
							Timber.e(e, "Failed to create playlist")
							Toast.makeText(context, R.string.msg_failed_to_create_playlist, Toast.LENGTH_SHORT).show()
							isCreating = false
						}
					}
				},
				variant = VegafoXButtonVariant.Primary,
				enabled = !isCreating,
				compact = true,
			)
		},
		dismissButton = {
			VegafoXButton(
				text = stringResource(R.string.btn_cancel),
				onClick = onDismiss,
				variant = VegafoXButtonVariant.Ghost,
				compact = true,
			)
		},
	)
}
