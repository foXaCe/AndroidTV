package org.jellyfin.androidtv.ui.jellyseerr.compose

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrQualityProfileDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRootFolderDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.focusBorderColor
import org.jellyfin.androidtv.ui.jellyseerr.AdvancedRequestOptions

data class ServerDetailsData(
	val serverId: Int,
	val profiles: List<JellyseerrQualityProfileDto>,
	val rootFolders: List<JellyseerrRootFolderDto>,
	val defaultProfileId: Int,
	val defaultRootFolder: String,
)

@Composable
fun AdvancedRequestOptionsDialog(
	title: String,
	is4k: Boolean,
	isMovie: Boolean,
	onLoadData: suspend () -> ServerDetailsData?,
	onConfirm: (AdvancedRequestOptions) -> Unit,
	onDismiss: () -> Unit,
) {
	val accentColor = focusBorderColor()
	val firstOptionFocusRequester = remember { FocusRequester() }

	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var serverData by remember { mutableStateOf<ServerDetailsData?>(null) }

	var selectedProfileId by remember { mutableIntStateOf(-1) }
	var selectedRootFolderId by remember { mutableIntStateOf(-1) }
	var serverId by remember { mutableStateOf<Int?>(null) }
	var defaultProfileId by remember { mutableIntStateOf(-1) }
	var defaultRootFolderId by remember { mutableStateOf<Int?>(null) }

	val failedLoadMessage = stringResource(R.string.jellyseerr_failed_load_config)

	LaunchedEffect(Unit) {
		try {
			val data = withContext(Dispatchers.IO) { onLoadData() }
			if (data != null) {
				serverData = data
				serverId = data.serverId
				defaultProfileId = data.defaultProfileId
				selectedProfileId = data.defaultProfileId
				val defFolder = data.rootFolders.find { it.path == data.defaultRootFolder }
				defaultRootFolderId = defFolder?.id
				selectedRootFolderId = defFolder?.id ?: -1
			} else {
				errorMessage = failedLoadMessage
			}
		} catch (e: Exception) {
			errorMessage = e.message ?: failedLoadMessage
		}
		isLoading = false
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 450.dp, max = 650.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.surface)
					.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
					.padding(24.dp),
			) {
				// Title
				Text(
					text = stringResource(R.string.jellyseerr_request_options),
					style = JellyfinTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					color = JellyfinTheme.colorScheme.textPrimary,
					modifier = Modifier.padding(bottom = 6.dp),
				)

				// Subtitle
				val mediaType = if (isMovie) stringResource(R.string.lbl_movie_type) else stringResource(R.string.lbl_tv_show_type)
				val quality = if (is4k) "4K" else "HD"
				Text(
					text = "$title ($quality $mediaType)",
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textSecondary,
					modifier = Modifier.padding(bottom = 16.dp),
				)

				// Content
				when {
					isLoading -> {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 24.dp),
							contentAlignment = Alignment.Center,
						) {
							CircularProgressIndicator(
								strokeWidth = 2.dp,
								color = JellyfinTheme.colorScheme.secondary,
								modifier = Modifier.size(32.dp),
							)
						}
					}
					errorMessage != null -> {
						Text(
							text = errorMessage!!,
							style = JellyfinTheme.typography.bodyMedium,
							color = JellyfinTheme.colorScheme.error,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 16.dp),
						)
					}
					serverData != null -> {
						val data = serverData!!
						val scrollState = rememberScrollState()

						Column(
							modifier = Modifier
								.height(320.dp)
								.verticalScroll(scrollState),
						) {
							// Quality Profile section
							SectionHeader(stringResource(R.string.jellyseerr_quality_profile))

							// Server Default option
							OptionRadioButton(
								text = stringResource(R.string.jellyseerr_server_default),
								isSelected = selectedProfileId == defaultProfileId,
								focusRequester = firstOptionFocusRequester,
								onClick = { selectedProfileId = defaultProfileId },
							)

							data.profiles.forEach { profile ->
								OptionRadioButton(
									text = profile.name,
									isSelected = selectedProfileId == profile.id,
									onClick = { selectedProfileId = profile.id },
								)
							}

							// Separator
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.padding(vertical = 8.dp)
									.height(1.dp)
									.background(JellyfinTheme.colorScheme.outlineVariant),
							)

							// Root Folder section
							SectionHeader(stringResource(R.string.jellyseerr_root_folder))

							val defaultFolder = data.rootFolders.find { it.path == data.defaultRootFolder }
							val defaultLabel = if (defaultFolder != null) {
								stringResource(R.string.jellyseerr_server_default_path, getDisplayPath(defaultFolder.path))
							} else {
								stringResource(R.string.jellyseerr_server_default)
							}

							OptionRadioButton(
								text = defaultLabel,
								isSelected = selectedRootFolderId == (defaultRootFolderId ?: -1),
								onClick = { selectedRootFolderId = defaultRootFolderId ?: -1 },
							)

							data.rootFolders.forEach { folder ->
								val isDefault = folder.path == data.defaultRootFolder
								if (!isDefault) {
									OptionRadioButton(
										text = getDisplayPath(folder.path),
										isSelected = selectedRootFolderId == folder.id,
										onClick = { selectedRootFolderId = folder.id },
									)
								}
							}
						}

						LaunchedEffect(serverData) {
							firstOptionFocusRequester.requestFocus()
						}
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Action buttons
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center,
				) {
					DialogActionButton(
						text = stringResource(R.string.btn_cancel),
						backgroundColor = JellyfinTheme.colorScheme.surfaceBright,
						focusedBackgroundColor = JellyfinTheme.colorScheme.surfaceContainer,
						accentColor = accentColor,
						onClick = onDismiss,
					)

					Spacer(modifier = Modifier.width(16.dp))

					DialogActionButton(
						text = stringResource(R.string.btn_request),
						backgroundColor = JellyfinTheme.colorScheme.secondary,
						focusedBackgroundColor = JellyfinTheme.colorScheme.secondaryContainer,
						accentColor = accentColor,
						onClick = {
							onConfirm(
								AdvancedRequestOptions(
									profileId = selectedProfileId,
									rootFolderId = if (selectedRootFolderId == -1) null else selectedRootFolderId,
									serverId = serverId,
								)
							)
							onDismiss()
						},
					)
				}
			}
		}
	}
}

@Composable
private fun SectionHeader(text: String) {
	Text(
		text = text,
		style = JellyfinTheme.typography.titleMedium,
		fontWeight = FontWeight.Bold,
		color = JellyfinTheme.colorScheme.textPrimary,
		modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
	)
}

@Composable
private fun OptionRadioButton(
	text: String,
	isSelected: Boolean,
	focusRequester: FocusRequester? = null,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val focusModifier = if (focusRequester != null) {
		Modifier.focusRequester(focusRequester)
	} else {
		Modifier
	}

	Row(
		modifier = focusModifier
			.fillMaxWidth()
			.clickable(interactionSource = interactionSource, indication = null) { onClick() }
			.focusable(interactionSource = interactionSource)
			.background(if (isFocused) JellyfinTheme.colorScheme.surfaceBright else Color.Transparent)
			.padding(horizontal = 16.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = if (isSelected) "\u25CF" else "\u25CB",
			style = JellyfinTheme.typography.bodyLarge,
			color = if (isSelected) JellyfinTheme.colorScheme.secondary else JellyfinTheme.colorScheme.textPrimary,
		)

		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = text,
			style = JellyfinTheme.typography.bodyMedium,
			color = if (isSelected) JellyfinTheme.colorScheme.secondary else JellyfinTheme.colorScheme.textPrimary,
		)
	}
}

private fun getDisplayPath(path: String): String = path.trimEnd('/')
