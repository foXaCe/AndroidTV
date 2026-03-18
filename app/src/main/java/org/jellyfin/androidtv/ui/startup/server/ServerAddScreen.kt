package org.jellyfin.androidtv.ui.startup.server

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.model.ConnectedState
import org.jellyfin.androidtv.auth.model.ConnectingState
import org.jellyfin.androidtv.auth.model.UnableToConnectState
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.startup.ServerAddViewModel
import org.jellyfin.androidtv.util.getSummary
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun ServerAddScreen(
	serverAddress: String? = null,
	onConnected: (UUID) -> Unit,
	viewModel: ServerAddViewModel = koinViewModel(),
) {
	val state by viewModel.state.collectAsState()
	var address by remember { mutableStateOf(serverAddress.orEmpty()) }
	val context = LocalContext.current
	val isPreFilled = serverAddress != null
	var validationError by remember { mutableStateOf<String?>(null) }

	val addressFocusRequester = remember { FocusRequester() }
	val buttonFocusRequester = remember { FocusRequester() }

	// Auto-submit if pre-filled address
	LaunchedEffect(isPreFilled) {
		if (isPreFilled && serverAddress!!.isNotBlank()) {
			viewModel.addServer(serverAddress)
		}
	}

	// Navigate on connected
	LaunchedEffect(state) {
		if (state is ConnectedState) {
			onConnected((state as ConnectedState).id)
		}
	}

	// Auto-focus address field
	LaunchedEffect(Unit) {
		if (!isPreFilled) {
			addressFocusRequester.requestFocus()
		}
	}

	val isConnecting = state is ConnectingState

	fun submit() {
		if (address.isBlank()) {
			validationError = context.getString(R.string.server_field_empty)
		} else {
			validationError = null
			viewModel.addServer(address)
		}
	}

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.width(480.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// VegafoX header
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					painter = painterResource(R.drawable.ic_vegafox),
					contentDescription = null,
					modifier = Modifier.size(40.dp),
					tint = Color.Unspecified,
				)
				Spacer(Modifier.width(8.dp))
				Text(
					text =
						buildAnnotatedString {
							withStyle(SpanStyle(color = VegafoXColors.BlueAccent)) { append("Vega") }
							withStyle(SpanStyle(color = VegafoXColors.OrangePrimary)) { append("foX") }
						},
					style = TextStyle(fontSize = 13.sp, letterSpacing = 3.sp),
				)
			}

			Spacer(Modifier.height(32.dp))

			// Card
			Column(
				modifier =
					Modifier
						.fillMaxWidth()
						.clip(RoundedCornerShape(16.dp))
						.background(VegafoXColors.Surface)
						.padding(horizontal = 40.dp, vertical = 36.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				// Title
				Text(
					text = stringResource(R.string.lbl_enter_server_address),
					style =
						TextStyle(
							fontSize = 20.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.TextPrimary,
						),
				)

				Spacer(Modifier.height(8.dp))

				// Subtitle
				Text(
					text = stringResource(R.string.lbl_valid_server_address),
					style =
						TextStyle(
							fontSize = 13.sp,
							color = VegafoXColors.TextSecondary,
						),
				)

				Spacer(Modifier.height(24.dp))

				// Address input
				OutlinedTextField(
					value = address,
					onValueChange = {
						address = it
						validationError = null
					},
					modifier =
						Modifier
							.fillMaxWidth()
							.focusRequester(addressFocusRequester),
					enabled = !isConnecting && !isPreFilled,
					placeholder = {
						Text(
							text = stringResource(R.string.server_address_hint),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					isError = validationError != null || state is UnableToConnectState,
					singleLine = true,
					keyboardOptions =
						KeyboardOptions(
							keyboardType = KeyboardType.Uri,
							imeAction = ImeAction.Done,
						),
					keyboardActions =
						KeyboardActions(
							onDone = { submit() },
						),
					colors =
						OutlinedTextFieldDefaults.colors(
							focusedContainerColor = Color.Transparent,
							unfocusedContainerColor = Color.Transparent,
							focusedBorderColor = VegafoXColors.OrangePrimary,
							unfocusedBorderColor = VegafoXColors.Divider,
							errorBorderColor = VegafoXColors.Error,
							focusedLabelColor = VegafoXColors.OrangePrimary,
							unfocusedLabelColor = VegafoXColors.TextSecondary,
							cursorColor = VegafoXColors.OrangePrimary,
							focusedTextColor = VegafoXColors.TextPrimary,
							unfocusedTextColor = VegafoXColors.TextPrimary,
							disabledTextColor = VegafoXColors.TextDisabled,
							disabledBorderColor = VegafoXColors.Divider,
						),
				)

				Spacer(Modifier.height(20.dp))

				// Connect button
				VegafoXButton(
					text = stringResource(R.string.action_connect),
					onClick = { submit() },
					variant = VegafoXButtonVariant.Primary,
					enabled = !isConnecting && address.isNotBlank(),
					modifier = Modifier.fillMaxWidth(),
					autoFocus = isPreFilled,
				)

				// Status / Error area
				val statusText =
					when {
						validationError != null -> validationError
						state is ConnectingState ->
							context.getString(
								R.string.server_connecting,
								(state as ConnectingState).address,
							)
						state is UnableToConnectState -> {
							val candidates = (state as UnableToConnectState).addressCandidates
							context.getString(
								R.string.server_connection_failed_candidates,
								candidates
									.map { "${it.key} - ${it.value.getSummary(context)}" }
									.joinToString(prefix = "\n", separator = "\n"),
							)
						}
						else -> null
					}

				if (statusText != null) {
					Spacer(Modifier.height(16.dp))

					val isError = validationError != null || state is UnableToConnectState
					Text(
						text = statusText,
						style =
							TextStyle(
								fontSize = 13.sp,
								color = if (isError) VegafoXColors.Error else VegafoXColors.TextSecondary,
								textAlign = TextAlign.Center,
							),
						modifier = Modifier.fillMaxWidth(),
					)
				}
			}
		}
	}
}
