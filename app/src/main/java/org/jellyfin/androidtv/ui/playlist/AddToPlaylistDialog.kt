package org.jellyfin.androidtv.ui.playlist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ServerUserSession
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.shuffle.GlassDialogRow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber
import java.util.UUID

internal enum class PlaylistDialogMode {
	SELECT_SERVER, MAIN, SELECT_PLAYLIST
}

@Composable
fun AddToPlaylistDialog(
	itemId: UUID,
	api: ApiClient,
	onDismiss: () -> Unit,
	onAddToPlaylist: (playlistId: UUID, serverApi: ApiClient) -> Unit,
	onCreateNewPlaylist: (serverApi: ApiClient) -> Unit,
	enableMultiServer: Boolean = false,
	serverSessions: List<ServerUserSession> = emptyList(),
) {
	val initialMode = if (enableMultiServer && serverSessions.size > 1) {
		PlaylistDialogMode.SELECT_SERVER
	} else {
		PlaylistDialogMode.MAIN
	}

	var mode by remember { mutableStateOf(initialMode) }
	var playlists by remember { mutableStateOf<List<BaseItemDto>>(emptyList()) }
	var loadingPlaylists by remember { mutableStateOf(false) }
	var selectedServerSession by remember {
		mutableStateOf(serverSessions.firstOrNull())
	}
	val activeApi = selectedServerSession?.apiClient ?: api
	val initialFocusRequester = remember { FocusRequester() }

	LaunchedEffect(mode, selectedServerSession) {
		if (mode == PlaylistDialogMode.SELECT_PLAYLIST) {
			playlists = emptyList()
			loadingPlaylists = true
			try {
				val response = withContext(Dispatchers.IO) {
					activeApi.itemsApi.getItems(
						includeItemTypes = setOf(BaseItemKind.PLAYLIST),
						recursive = true,
						sortBy = setOf(ItemSortBy.SORT_NAME),
						sortOrder = setOf(SortOrder.ASCENDING),
						fields = setOf(ItemFields.CAN_DELETE),
					)
				}
				playlists = response.content.items.filter { it.canDelete == true }
			} catch (e: Exception) {
				Timber.e(e, "Failed to load playlists")
			}
			loadingPlaylists = false
		}
	}

	Dialog(
		onDismissRequest = {
			when {
				mode == PlaylistDialogMode.SELECT_PLAYLIST -> mode = PlaylistDialogMode.MAIN
				mode == PlaylistDialogMode.MAIN && initialMode == PlaylistDialogMode.SELECT_SERVER -> mode = PlaylistDialogMode.SELECT_SERVER
				else -> onDismiss()
			}
		},
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
				// Title row with optional back button
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (mode != initialMode) {
						val backInteraction = remember { MutableInteractionSource() }
						val backFocused by backInteraction.collectIsFocusedAsState()
						val backMode = when (mode) {
							PlaylistDialogMode.SELECT_PLAYLIST -> PlaylistDialogMode.MAIN
							PlaylistDialogMode.MAIN -> PlaylistDialogMode.SELECT_SERVER
							else -> PlaylistDialogMode.MAIN
						}
						Box(
							modifier = Modifier
								.size(32.dp)
								.clip(JellyfinTheme.shapes.small)
								.background(if (backFocused) JellyfinTheme.colorScheme.listButtonFocused else Color.Transparent)
								.clickable(
									interactionSource = backInteraction,
									indication = null,
								) { mode = backMode }
								.focusable(interactionSource = backInteraction),
							contentAlignment = Alignment.Center,
						) {
							androidx.compose.material3.Text(
								text = "\u276E",
								style = JellyfinTheme.typography.titleMedium,
								color = if (backFocused) JellyfinTheme.colorScheme.textPrimary else JellyfinTheme.colorScheme.textSecondary,
							)
						}
						Spacer(modifier = Modifier.width(12.dp))
					}

					androidx.compose.material3.Text(
						text = when (mode) {
							PlaylistDialogMode.SELECT_SERVER -> stringResource(R.string.lbl_select_server)
							PlaylistDialogMode.MAIN -> stringResource(R.string.lbl_add_to_playlist)
							PlaylistDialogMode.SELECT_PLAYLIST -> stringResource(R.string.lbl_select_playlist)
						},
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

				Spacer(modifier = Modifier.height(8.dp))

				// Content
				when (mode) {
					PlaylistDialogMode.SELECT_SERVER -> {
						LazyColumn {
							items(serverSessions.size) { index ->
								val session = serverSessions[index]
								GlassDialogRow(
									label = session.server.name,
									onClick = {
										selectedServerSession = session
										mode = PlaylistDialogMode.MAIN
									},
									focusRequester = if (index == 0) initialFocusRequester else null,
								)
							}
						}
					}
					PlaylistDialogMode.MAIN -> {
						GlassDialogRow(
							icon = ImageVector.vectorResource(R.drawable.ic_folder),
							label = stringResource(R.string.lbl_select_playlist),
							onClick = { mode = PlaylistDialogMode.SELECT_PLAYLIST },
							focusRequester = initialFocusRequester,
						)
						GlassDialogRow(
							icon = ImageVector.vectorResource(R.drawable.ic_add),
							label = stringResource(R.string.lbl_create_new_playlist),
							onClick = { onCreateNewPlaylist(activeApi) },
						)
					}
					PlaylistDialogMode.SELECT_PLAYLIST -> {
						if (loadingPlaylists) {
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
						} else if (playlists.isEmpty()) {
							androidx.compose.material3.Text(
								text = stringResource(R.string.lbl_no_playlists_found),
								color = JellyfinTheme.colorScheme.textHint,
								style = JellyfinTheme.typography.bodyMedium,
								modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
							)
						} else {
							LazyColumn {
								items(playlists.size) { index ->
									val playlist = playlists[index]
									GlassDialogRow(
										label = playlist.name ?: stringResource(R.string.lbl_unknown),
										onClick = { onAddToPlaylist(playlist.id, activeApi) },
										focusRequester = if (index == 0) initialFocusRequester else null,
									)
								}
							}
						}
					}
				}

				// Cancel
				Spacer(modifier = Modifier.height(4.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)
				Spacer(modifier = Modifier.height(4.dp))

				GlassDialogRow(
					label = stringResource(R.string.lbl_cancel),
					onClick = onDismiss,
					contentColor = JellyfinTheme.colorScheme.textHint,
				)
			}
		}

		LaunchedEffect(mode) {
			initialFocusRequester.requestFocus()
		}
	}
}

