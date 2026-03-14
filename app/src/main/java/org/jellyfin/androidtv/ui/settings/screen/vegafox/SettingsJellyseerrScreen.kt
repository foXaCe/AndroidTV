package org.jellyfin.androidtv.ui.settings.screen.vegafox

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun SettingsJellyseerrScreen(
	viewModel: JellyseerrSettingsViewModel = koinViewModel(),
	globalJellyseerrPreferences: JellyseerrPreferences = koinInject(named("global")),
	userPreferences: UserPreferences = koinInject(),
) {
	val context = LocalContext.current
	val router = LocalRouter.current
	val uiState by viewModel.uiState.collectAsState()
	val isVegafoXMode by viewModel.isVegafoXMode.collectAsState()
	val userPrefs by viewModel.userPreferences.collectAsState()

	val effectivePrefs = userPrefs ?: globalJellyseerrPreferences
	var enabled by rememberPreference(effectivePrefs, JellyseerrPreferences.enabled)
	var blockNsfw by rememberPreference(effectivePrefs, JellyseerrPreferences.blockNsfw)

	// Dialog states
	var showServerUrlDialog by remember { mutableStateOf(false) }
	var showJellyfinLoginDialog by remember { mutableStateOf(false) }
	var showLocalLoginDialog by remember { mutableStateOf(false) }
	var showApiKeyLoginDialog by remember { mutableStateOf(false) }
	var showLogoutConfirmDialog by remember { mutableStateOf(false) }
	var showVegafoXDisconnectDialog by remember { mutableStateOf(false) }

	val apiKeyStatusText =
		when (uiState.apiKeyStatus) {
			JellyseerrSettingsUiState.ApiKeyStatus.HAS_KEY -> stringResource(R.string.jellyseerr_permanent_api_key)
			JellyseerrSettingsUiState.ApiKeyStatus.NO_KEY_AUTHENTICATED -> stringResource(R.string.jellyseerr_api_key_absent)
			JellyseerrSettingsUiState.ApiKeyStatus.NOT_LOGGED_IN -> stringResource(R.string.jellyseerr_not_logged_in)
		}

	// Collect one-shot events (toasts)
	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			val (message, long) = resolveEvent(context, event)
			Toast.makeText(context, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
		}
	}

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.jellyseerr_settings),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		if (isVegafoXMode) {
			// VegafoX Proxy Status
			item {
				ListSection(
					headingContent = { Text(stringResource(R.string.jellyseerr_vegafox_proxy)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_vegafox_proxy_status)) },
				)
			}

			item {
				val statusCaption =
					if (uiState.vegafoxDisplayName.isNotEmpty()) {
						stringResource(R.string.jellyseerr_vegafox_connected, uiState.vegafoxDisplayName)
					} else {
						stringResource(R.string.jellyseerr_vegafox_not_authenticated)
					}
				ListButton(
					leadingContent = { Icon(painterResource(R.drawable.ic_vegafox_fox), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_vegafox_proxy)) },
					captionContent = { Text(statusCaption) },
					onClick = { },
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Logout), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_vegafox_disconnect)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_vegafox_disconnect_description)) },
					onClick = { showVegafoXDisconnectDialog = true },
				)
			}
		} else {
			// Reconnect via VegafoX Plugin
			if (userPreferences[UserPreferences.pluginSyncEnabled]) {
				item {
					ListButton(
						leadingContent = { Icon(painterResource(R.drawable.ic_vegafox_fox), contentDescription = null) },
						headingContent = { Text(stringResource(R.string.jellyseerr_vegafox_reconnect)) },
						captionContent = { Text(stringResource(R.string.jellyseerr_vegafox_reconnect_description)) },
						onClick = { viewModel.reconnectVegafoX() },
					)
				}
			}

			// Direct Mode — Server Configuration
			item {
				ListSection(
					headingContent = { Text(stringResource(R.string.jellyseerr_server_settings)) },
				)
			}

			item {
				ListButton(
					headingContent = { Text(stringResource(R.string.jellyseerr_enabled)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_enabled_description)) },
					trailingContent = { Checkbox(checked = enabled) },
					onClick = { enabled = !enabled },
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Settings), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_server_url)) },
					captionContent = {
						Text(
							if (uiState.serverUrl.isNotEmpty()) {
								uiState.serverUrl
							} else {
								stringResource(R.string.jellyseerr_server_url_description)
							},
						)
					},
					onClick = { showServerUrlDialog = true },
				)
			}

			// Authentication Methods
			item {
				ListSection(
					headingContent = { Text(stringResource(R.string.jellyseerr_auth_method)) },
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(painterResource(R.drawable.ic_jellyseerr_jellyfish), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_connect_jellyfin)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_connect_jellyfin_description)) },
					onClick = {
						if (enabled) {
							showJellyfinLoginDialog = true
						} else {
							Toast.makeText(context, context.getString(R.string.jellyseerr_please_enable_first), Toast.LENGTH_SHORT).show()
						}
					},
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Person), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_login_local)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_login_local_description)) },
					onClick = {
						if (enabled) {
							showLocalLoginDialog = true
						} else {
							Toast.makeText(context, context.getString(R.string.jellyseerr_please_enable_first), Toast.LENGTH_SHORT).show()
						}
					},
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Lightbulb), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_login_api_key)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_login_api_key_description)) },
					onClick = {
						if (enabled) {
							showApiKeyLoginDialog = true
						} else {
							Toast.makeText(context, context.getString(R.string.jellyseerr_please_enable_first), Toast.LENGTH_SHORT).show()
						}
					},
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Lock), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_api_key_status)) },
					captionContent = { Text(apiKeyStatusText) },
					onClick = { },
				)
			}

			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Logout), contentDescription = null) },
					headingContent = { Text(stringResource(R.string.jellyseerr_logout)) },
					captionContent = { Text(stringResource(R.string.jellyseerr_logout_description)) },
					onClick = { showLogoutConfirmDialog = true },
				)
			}
		}

		// Content Preferences
		item {
			ListSection(
				headingContent = { Text(stringResource(R.string.pref_customization)) },
			)
		}

		item {
			ListButton(
				headingContent = { Text(stringResource(R.string.jellyseerr_block_nsfw)) },
				captionContent = { Text(stringResource(R.string.jellyseerr_block_nsfw_description)) },
				trailingContent = { Checkbox(checked = blockNsfw) },
				onClick = {
					if (enabled) blockNsfw = !blockNsfw
				},
			)
		}

		// Discover Rows Configuration
		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.GridView), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.jellyseerr_rows_title)) },
				captionContent = { Text(stringResource(R.string.jellyseerr_rows_description)) },
				onClick = {
					if (enabled) router.push(Routes.JELLYSEERR_ROWS)
				},
			)
		}
	}

	// Server URL Dialog
	if (showServerUrlDialog) {
		ServerUrlDialog(
			currentUrl = uiState.serverUrl,
			onDismiss = { showServerUrlDialog = false },
			onSave = { url ->
				viewModel.saveServerUrl(url)
				showServerUrlDialog = false
			},
		)
	}

	// Jellyfin Login Dialog
	if (showJellyfinLoginDialog) {
		if (uiState.serverUrl.isBlank()) {
			Toast.makeText(context, context.getString(R.string.jellyseerr_set_server_url_first), Toast.LENGTH_SHORT).show()
			showJellyfinLoginDialog = false
		} else {
			JellyfinLoginDialog(
				username = uiState.currentUsername,
				onDismiss = { showJellyfinLoginDialog = false },
				onConnect = { password ->
					showJellyfinLoginDialog = false
					viewModel.loginWithJellyfin(password)
				},
			)
		}
	}

	// Local Login Dialog
	if (showLocalLoginDialog) {
		if (uiState.serverUrl.isBlank()) {
			Toast.makeText(context, context.getString(R.string.jellyseerr_set_server_url_first), Toast.LENGTH_SHORT).show()
			showLocalLoginDialog = false
		} else {
			LocalLoginDialog(
				onDismiss = { showLocalLoginDialog = false },
				onLogin = { email, password ->
					showLocalLoginDialog = false
					viewModel.loginLocal(email, password)
				},
			)
		}
	}

	// API Key Login Dialog
	if (showApiKeyLoginDialog) {
		if (uiState.serverUrl.isBlank()) {
			Toast.makeText(context, context.getString(R.string.jellyseerr_set_server_url_first), Toast.LENGTH_SHORT).show()
			showApiKeyLoginDialog = false
		} else {
			ApiKeyLoginDialog(
				onDismiss = { showApiKeyLoginDialog = false },
				onLogin = { apiKey ->
					showApiKeyLoginDialog = false
					viewModel.loginWithApiKey(apiKey)
				},
			)
		}
	}

	// Logout Confirmation Dialog
	if (showLogoutConfirmDialog) {
		VegafoXAlertDialog(
			onDismissRequest = { showLogoutConfirmDialog = false },
			title = stringResource(R.string.jellyseerr_logout_confirm_title),
			text = stringResource(R.string.jellyseerr_logout_confirm_message),
			confirmText = stringResource(R.string.btn_log_out),
			dismissText = stringResource(R.string.btn_cancel),
			onConfirm = {
				showLogoutConfirmDialog = false
				viewModel.logout()
			},
			onDismiss = { showLogoutConfirmDialog = false },
		)
	}

	// VegafoX Disconnect Confirmation Dialog
	if (showVegafoXDisconnectDialog) {
		VegafoXAlertDialog(
			onDismissRequest = { showVegafoXDisconnectDialog = false },
			title = stringResource(R.string.jellyseerr_vegafox_disconnect),
			text = stringResource(R.string.jellyseerr_vegafox_disconnect_description),
			confirmText = stringResource(R.string.btn_disconnect),
			dismissText = stringResource(R.string.btn_cancel),
			onConfirm = {
				showVegafoXDisconnectDialog = false
				viewModel.logoutVegafoX()
			},
			onDismiss = { showVegafoXDisconnectDialog = false },
		)
	}
}

private fun resolveEvent(
	context: android.content.Context,
	event: JellyseerrSettingsEvent,
): Pair<String, Boolean> =
	when (event) {
		is JellyseerrSettingsEvent.JellyfinLoginSuccess -> {
			val authType =
				if (event.hasApiKey) {
					context.getString(R.string.jellyseerr_auth_permanent_key)
				} else {
					context.getString(R.string.jellyseerr_auth_cookie_based)
				}
			context.getString(R.string.jellyseerr_connected_auth_type, authType) to true
		}
		is JellyseerrSettingsEvent.JellyfinLoginFailed -> {
			val msg =
				when (event.errorType) {
					JellyseerrSettingsEvent.ErrorType.SERVER_CONFIG -> context.getString(R.string.jellyseerr_error_server_config, event.message)
					JellyseerrSettingsEvent.ErrorType.AUTH_FAILED -> context.getString(R.string.jellyseerr_error_auth_failed, event.message)
					JellyseerrSettingsEvent.ErrorType.CONNECTION -> context.getString(R.string.jellyseerr_error_connection, event.message)
					JellyseerrSettingsEvent.ErrorType.EXCEPTION -> context.getString(R.string.jellyseerr_connection_error_detail, event.message)
				}
			msg to true
		}
		is JellyseerrSettingsEvent.LocalLoginSuccess -> {
			val msg =
				if (event.hasApiKey) {
					context.getString(R.string.jellyseerr_permanent_key_success)
				} else {
					context.getString(R.string.jellyseerr_login_cookie_success)
				}
			msg to true
		}
		is JellyseerrSettingsEvent.LocalLoginFailed -> context.getString(R.string.jellyseerr_login_failed, event.message) to true
		JellyseerrSettingsEvent.ApiKeyLoginSuccess -> context.getString(R.string.jellyseerr_permanent_key_success) to true
		is JellyseerrSettingsEvent.ApiKeyLoginFailed -> context.getString(R.string.jellyseerr_login_failed, event.message) to true
		JellyseerrSettingsEvent.LogoutSuccess -> context.getString(R.string.jellyseerr_logout_success) to false
		JellyseerrSettingsEvent.VegafoXDisconnectSuccess -> context.getString(R.string.jellyseerr_logout_success) to false
		JellyseerrSettingsEvent.VegafoXReconnectSuccess -> context.getString(R.string.jellyseerr_vegafox_reconnect_success) to false
		JellyseerrSettingsEvent.VegafoXNotEnabled -> context.getString(R.string.jellyseerr_vegafox_not_enabled) to false
		JellyseerrSettingsEvent.VegafoXReconnectFailed -> context.getString(R.string.jellyseerr_vegafox_reconnect_failed) to false
		JellyseerrSettingsEvent.ServerUrlSaved -> context.getString(R.string.jellyseerr_server_url_saved) to false
		JellyseerrSettingsEvent.AllFieldsRequired -> context.getString(R.string.jellyseerr_all_fields_required) to false
	}

@Composable
private fun ServerUrlDialog(
	currentUrl: String,
	onDismiss: () -> Unit,
	onSave: (String) -> Unit,
) {
	var url by remember { mutableStateOf(currentUrl) }

	AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		title = {
			Text(
				stringResource(R.string.jellyseerr_server_url),
				style = TextStyle(color = VegafoXColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
			)
		},
		text = {
			Column {
				Text(
					stringResource(R.string.jellyseerr_server_url_description),
					style = TextStyle(color = VegafoXColors.TextSecondary, fontSize = 15.sp),
				)
				OutlinedTextField(
					value = url,
					onValueChange = { url = it },
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 16.dp),
					placeholder = {
						Text(
							"http://192.168.1.100:5055",
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
					shape = RoundedCornerShape(12.dp),
					colors = vegafoXTextFieldColors(),
				)
			}
		},
		confirmButton = {
			VegafoXButton(
				text = stringResource(R.string.btn_save),
				onClick = { onSave(url.trim()) },
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

@Composable
private fun JellyfinLoginDialog(
	username: String,
	onDismiss: () -> Unit,
	onConnect: (password: String) -> Unit,
) {
	var password by remember { mutableStateOf("") }

	AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		title = {
			Text(
				stringResource(R.string.jellyseerr_connect_jellyfin),
				style = TextStyle(color = VegafoXColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
			)
		},
		text = {
			Column {
				Text(
					stringResource(R.string.jellyseerr_connecting_as, username),
					style = TextStyle(color = VegafoXColors.TextSecondary, fontSize = 15.sp),
				)
				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 16.dp),
					placeholder = {
						Text(
							stringResource(R.string.jellyseerr_placeholder_password),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					singleLine = true,
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
					shape = RoundedCornerShape(12.dp),
					colors = vegafoXTextFieldColors(),
				)
			}
		},
		confirmButton = {
			VegafoXButton(
				text = stringResource(R.string.btn_connect),
				onClick = { onConnect(password) },
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

@Composable
private fun LocalLoginDialog(
	onDismiss: () -> Unit,
	onLogin: (email: String, password: String) -> Unit,
) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }

	AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		title = {
			Text(
				stringResource(R.string.jellyseerr_login_local),
				style = TextStyle(color = VegafoXColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
			)
		},
		text = {
			Column {
				Text(
					stringResource(R.string.jellyseerr_login_local_hint),
					style = TextStyle(color = VegafoXColors.TextSecondary, fontSize = 15.sp),
				)
				OutlinedTextField(
					value = email,
					onValueChange = { email = it },
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 16.dp),
					placeholder = {
						Text(
							stringResource(R.string.jellyseerr_placeholder_email),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
					shape = RoundedCornerShape(12.dp),
					colors = vegafoXTextFieldColors(),
				)
				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 8.dp),
					placeholder = {
						Text(
							stringResource(R.string.jellyseerr_placeholder_password),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					singleLine = true,
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
					shape = RoundedCornerShape(12.dp),
					colors = vegafoXTextFieldColors(),
				)
			}
		},
		confirmButton = {
			VegafoXButton(
				text = stringResource(R.string.btn_login),
				onClick = {
					if (email.isNotEmpty() && password.isNotEmpty()) {
						onLogin(email.trim(), password)
					}
				},
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

@Composable
private fun ApiKeyLoginDialog(
	onDismiss: () -> Unit,
	onLogin: (apiKey: String) -> Unit,
) {
	var apiKey by remember { mutableStateOf("") }

	AlertDialog(
		onDismissRequest = onDismiss,
		containerColor = VegafoXColors.Surface,
		title = {
			Text(
				stringResource(R.string.jellyseerr_login_api_key),
				style = TextStyle(color = VegafoXColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
			)
		},
		text = {
			Column {
				Text(
					stringResource(R.string.jellyseerr_api_key_input_description),
					style = TextStyle(color = VegafoXColors.TextSecondary, fontSize = 15.sp),
				)
				OutlinedTextField(
					value = apiKey,
					onValueChange = { apiKey = it },
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(top = 16.dp),
					placeholder = {
						Text(
							stringResource(R.string.jellyseerr_api_key_input),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					singleLine = true,
					shape = RoundedCornerShape(12.dp),
					colors = vegafoXTextFieldColors(),
				)
			}
		},
		confirmButton = {
			VegafoXButton(
				text = stringResource(R.string.btn_login),
				onClick = {
					if (apiKey.isNotEmpty()) {
						onLogin(apiKey.trim())
					}
				},
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

@Composable
private fun vegafoXTextFieldColors() =
	OutlinedTextFieldDefaults.colors(
		focusedBorderColor = VegafoXColors.OrangePrimary,
		unfocusedBorderColor = VegafoXColors.Divider,
		cursorColor = VegafoXColors.OrangePrimary,
		focusedTextColor = VegafoXColors.TextPrimary,
		unfocusedTextColor = VegafoXColors.TextPrimary,
		focusedContainerColor = VegafoXColors.Surface,
		unfocusedContainerColor = VegafoXColors.Surface,
	)

@Composable
private fun VegafoXAlertDialog(
	onDismissRequest: () -> Unit,
	title: String,
	text: String,
	confirmText: String,
	dismissText: String,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	AlertDialog(
		onDismissRequest = onDismissRequest,
		containerColor = VegafoXColors.Surface,
		title = {
			Text(
				title,
				style = TextStyle(color = VegafoXColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold),
			)
		},
		text = {
			Text(
				text,
				style = TextStyle(color = VegafoXColors.TextSecondary, fontSize = 15.sp),
			)
		},
		confirmButton = {
			VegafoXButton(
				text = confirmText,
				onClick = onConfirm,
				compact = true,
			)
		},
		dismissButton = {
			VegafoXButton(
				text = dismissText,
				onClick = onDismiss,
				variant = VegafoXButtonVariant.Ghost,
				compact = true,
			)
		},
	)
}
