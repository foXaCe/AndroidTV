package org.jellyfin.androidtv.ui.playlist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.shuffle.GlassDialogRow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.playlistsApi
import org.jellyfin.sdk.model.api.CreatePlaylistDto
import timber.log.Timber
import java.util.UUID

@Composable
fun CreatePlaylistDialog(
	itemId: UUID,
	apiClient: ApiClient,
	onDismiss: () -> Unit,
	onBack: () -> Unit,
	onPlaylistCreated: () -> Unit,
) {
	var playlistName by remember { mutableStateOf("") }
	var isPublic by remember { mutableStateOf(false) }
	var isCreating by remember { mutableStateOf(false) }
	val context = LocalContext.current
	val nameInputFocusRequester = remember { FocusRequester() }

	Dialog(
		onDismissRequest = onBack,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 340.dp, max = 440.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogScrim)
					.border(1.dp, Color.White.copy(alpha = 0.1f), JellyfinTheme.shapes.dialog)
					.padding(vertical = 20.dp),
			) {
				// Title row with back button
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					val backInteraction = remember { MutableInteractionSource() }
					val backFocused by backInteraction.collectIsFocusedAsState()
					Box(
						modifier = Modifier
							.size(32.dp)
							.clip(JellyfinTheme.shapes.small)
							.background(if (backFocused) JellyfinTheme.colorScheme.listButtonFocused else Color.Transparent)
							.clickable(
								interactionSource = backInteraction,
								indication = null,
							) { onBack() }
							.focusable(interactionSource = backInteraction),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = "\u276E",
							style = JellyfinTheme.typography.titleMedium,
							color = if (backFocused) JellyfinTheme.colorScheme.textPrimary else JellyfinTheme.colorScheme.textSecondary,
						)
					}
					Spacer(modifier = Modifier.width(12.dp))

					Text(
						text = stringResource(R.string.lbl_create_new_playlist),
						style = JellyfinTheme.typography.titleLarge,
						color = JellyfinTheme.colorScheme.textPrimary,
					)
				}

				// Divider
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Playlist name label
				Text(
					text = stringResource(R.string.lbl_playlist_name),
					style = JellyfinTheme.typography.bodySmall,
					color = JellyfinTheme.colorScheme.textHint,
					modifier = Modifier.padding(horizontal = 24.dp),
				)

				Spacer(modifier = Modifier.height(8.dp))

				// Playlist name input
				val nameInteraction = remember { MutableInteractionSource() }
				val nameFieldFocused by nameInteraction.collectIsFocusedAsState()
				BasicTextField(
					value = playlistName,
					onValueChange = { playlistName = it },
					textStyle = JellyfinTheme.typography.titleMedium.copy(
						color = JellyfinTheme.colorScheme.textPrimary,
						fontWeight = FontWeight.W400,
					),
					cursorBrush = SolidColor(JellyfinTheme.colorScheme.primary),
					singleLine = true,
					interactionSource = nameInteraction,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
						.clip(JellyfinTheme.shapes.medium)
						.background(JellyfinTheme.colorScheme.divider)
						.border(
							width = 1.dp,
							color = if (nameFieldFocused) JellyfinTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
							shape = JellyfinTheme.shapes.medium,
						)
						.padding(horizontal = 16.dp, vertical = 14.dp)
						.focusRequester(nameInputFocusRequester),
					decorationBox = { innerTextField ->
						Box {
							if (playlistName.isEmpty()) {
								Text(
									text = stringResource(R.string.lbl_playlist_name),
									color = JellyfinTheme.colorScheme.textDisabled,
									style = JellyfinTheme.typography.titleMedium,
								)
							}
							innerTextField()
						}
					},
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Public playlist toggle row
				val switchInteraction = remember { MutableInteractionSource() }
				val switchFocused by switchInteraction.collectIsFocusedAsState()
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable(
							interactionSource = switchInteraction,
							indication = null,
						) { isPublic = !isPublic }
						.focusable(interactionSource = switchInteraction)
						.background(
							if (switchFocused) JellyfinTheme.colorScheme.listButtonFocused else Color.Transparent,
						)
						.padding(horizontal = 24.dp, vertical = 14.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(R.string.lbl_public_playlist),
						style = JellyfinTheme.typography.titleMedium,
						fontWeight = FontWeight.W400,
						color = if (switchFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textPrimary,
						modifier = Modifier.weight(1f),
					)
					Switch(
						checked = isPublic,
						onCheckedChange = null,
						colors = SwitchDefaults.colors(
							checkedThumbColor = JellyfinTheme.colorScheme.onPrimary,
							checkedTrackColor = JellyfinTheme.colorScheme.primary,
							uncheckedThumbColor = JellyfinTheme.colorScheme.textSecondary,
							uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
							uncheckedBorderColor = JellyfinTheme.colorScheme.textDisabled,
						),
					)
				}

				Spacer(modifier = Modifier.height(8.dp))

				// Divider
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(4.dp))

				// Create & Add button
				if (isCreating) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(24.dp),
						horizontalArrangement = Arrangement.Center,
					) {
						CircularProgressIndicator(
							strokeWidth = 2.dp,
							color = JellyfinTheme.colorScheme.primary,
						)
					}
				} else {
					GlassDialogRow(
						icon = ImageVector.vectorResource(R.drawable.ic_add),
						label = stringResource(R.string.lbl_create_and_add),
						onClick = {
							val name = playlistName.trim()
							if (name.isBlank()) {
								Toast.makeText(
									context,
									context.getString(R.string.msg_enter_playlist_name),
									Toast.LENGTH_SHORT,
								).show()
								return@GlassDialogRow
							}
							isCreating = true
							CoroutineScope(Dispatchers.Main).launch {
								try {
									withContext(Dispatchers.IO) {
										val createRequest = CreatePlaylistDto(
											name = name,
											ids = listOf(itemId),
											users = emptyList(),
											isPublic = isPublic,
										)
										apiClient.playlistsApi.createPlaylist(createRequest)
									}
									Toast.makeText(
										context,
										context.getString(R.string.msg_playlist_created),
										Toast.LENGTH_SHORT,
									).show()
									onPlaylistCreated()
								} catch (e: Exception) {
									Timber.e(e, "Failed to create playlist")
									Toast.makeText(
										context,
										context.getString(R.string.msg_failed_to_create_playlist),
										Toast.LENGTH_SHORT,
									).show()
									isCreating = false
								}
							}
						},
					)

					// Cancel
					GlassDialogRow(
						label = stringResource(R.string.lbl_cancel),
						onClick = onDismiss,
						contentColor = JellyfinTheme.colorScheme.textHint,
					)
				}
			}
		}

		LaunchedEffect(Unit) {
			nameInputFocusRequester.requestFocus()
		}
	}
}
