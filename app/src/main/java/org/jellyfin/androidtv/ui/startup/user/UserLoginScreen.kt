package org.jellyfin.androidtv.ui.startup.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.components.VegafoXIconButton
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.startup.compose.VegafoXFoxLogo
import org.jellyfin.androidtv.ui.startup.compose.VegafoXTitleText

// Material "Visibility" icon — 24×24 viewport
private val IconVisibility: ImageVector by lazy {
	ImageVector
		.Builder(
			name = "Visibility",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f,
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(12f, 4.5f)
				curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
				curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
				curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
				curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
				close()
				moveTo(12f, 17f)
				curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
				curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
				curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
				curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
				close()
				moveTo(12f, 9f)
				curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
				curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
				curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
				curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
				close()
			}
		}.build()
}

// Material "VisibilityOff" icon — 24×24 viewport
private val IconVisibilityOff: ImageVector by lazy {
	ImageVector
		.Builder(
			name = "VisibilityOff",
			defaultWidth = 24.dp,
			defaultHeight = 24.dp,
			viewportWidth = 24f,
			viewportHeight = 24f,
		).apply {
			path(fill = SolidColor(Color.White)) {
				moveTo(12f, 7f)
				curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
				curveTo(17f, 12.65f, 16.87f, 13.26f, 16.64f, 13.83f)
				lineTo(19.56f, 16.75f)
				curveTo(21.07f, 15.49f, 22.26f, 13.86f, 23f, 12f)
				curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
				curveTo(10.6f, 4.5f, 9.26f, 4.75f, 8f, 5.2f)
				lineTo(10.17f, 7.36f)
				curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
				close()
				moveTo(2f, 4.27f)
				lineTo(4.28f, 6.55f)
				lineTo(4.74f, 7.01f)
				curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
				curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
				curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f)
				lineTo(16.81f, 19.08f)
				lineTo(19.73f, 22f)
				lineTo(21f, 20.73f)
				lineTo(3.27f, 3f)
				lineTo(2f, 4.27f)
				close()
				moveTo(7.53f, 9.8f)
				lineTo(9.08f, 11.35f)
				curveTo(9.03f, 11.56f, 9f, 11.78f, 9f, 12f)
				curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
				curveTo(12.22f, 15f, 12.44f, 14.97f, 12.65f, 14.92f)
				lineTo(14.2f, 16.47f)
				curveTo(13.53f, 16.8f, 12.79f, 17f, 12f, 17f)
				curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
				curveTo(7f, 11.21f, 7.2f, 10.47f, 7.53f, 9.8f)
				close()
				moveTo(11.84f, 9.02f)
				lineTo(14.99f, 12.17f)
				lineTo(15.01f, 12.01f)
				curveTo(15.01f, 10.35f, 13.67f, 9.01f, 12.01f, 9.01f)
				lineTo(11.84f, 9.02f)
				close()
			}
		}.build()
}

@Composable
fun UserLoginScreen(
	serverName: String,
	isLoading: Boolean,
	error: String?,
	onLogin: (username: String, password: String) -> Unit,
	onCancel: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var username by rememberSaveable { mutableStateOf("") }
	var password by rememberSaveable { mutableStateOf("") }
	var passwordVisible by remember { mutableStateOf(false) }

	val textFieldColors =
		OutlinedTextFieldDefaults.colors(
			focusedContainerColor = Color.Transparent,
			unfocusedContainerColor = Color.Transparent,
			focusedBorderColor = VegafoXColors.OrangePrimary,
			unfocusedBorderColor = VegafoXColors.Divider,
			focusedLabelColor = VegafoXColors.OrangePrimary,
			unfocusedLabelColor = VegafoXColors.TextSecondary,
			cursorColor = VegafoXColors.OrangePrimary,
			focusedTextColor = VegafoXColors.TextPrimary,
			unfocusedTextColor = VegafoXColors.TextPrimary,
		)

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(VegafoXColors.Background),
		contentAlignment = Alignment.Center,
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// Header: Fox logo + title
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				VegafoXFoxLogo(size = 48.dp, animated = false)
				Spacer(modifier = Modifier.width(8.dp))
				VegafoXTitleText(fontSize = 28.sp)
			}

			Spacer(modifier = Modifier.height(24.dp))

			// Card
			Box(
				modifier =
					Modifier
						.width(640.dp)
						.clip(RoundedCornerShape(20.dp))
						.background(VegafoXColors.Surface)
						.padding(32.dp),
			) {
				Column {
					// Title
					Text(
						text = stringResource(R.string.btn_login),
						style =
							TextStyle(
								fontSize = 28.sp,
								fontWeight = FontWeight.Bold,
								color = VegafoXColors.TextPrimary,
							),
					)

					Spacer(modifier = Modifier.height(4.dp))

					// Subtitle: server name
					Text(
						text = serverName,
						style =
							TextStyle(
								fontSize = 16.sp,
								fontWeight = FontWeight.Normal,
								color = VegafoXColors.TextSecondary,
							),
					)

					Spacer(modifier = Modifier.height(24.dp))

					// Username field
					OutlinedTextField(
						value = username,
						onValueChange = { username = it },
						modifier = Modifier.fillMaxWidth(),
						label = {
							Text(
								text = stringResource(R.string.input_username),
								style = TextStyle(color = VegafoXColors.TextSecondary),
							)
						},
						singleLine = true,
						keyboardOptions =
							KeyboardOptions(
								imeAction = ImeAction.Next,
							),
						colors = textFieldColors,
					)

					Spacer(modifier = Modifier.height(16.dp))

					// Password field
					OutlinedTextField(
						value = password,
						onValueChange = { password = it },
						modifier = Modifier.fillMaxWidth(),
						label = {
							Text(
								text = stringResource(R.string.input_password),
								style = TextStyle(color = VegafoXColors.TextSecondary),
							)
						},
						singleLine = true,
						visualTransformation =
							if (passwordVisible) {
								VisualTransformation.None
							} else {
								PasswordVisualTransformation()
							},
						keyboardOptions =
							KeyboardOptions(
								keyboardType = KeyboardType.Password,
								imeAction = ImeAction.Done,
							),
						keyboardActions =
							KeyboardActions(
								onDone = { onLogin(username, password) },
							),
						trailingIcon = {
							VegafoXIconButton(
								icon = if (passwordVisible) IconVisibilityOff else IconVisibility,
								contentDescription = stringResource(R.string.input_password),
								onClick = { passwordVisible = !passwordVisible },
							)
						},
						colors = textFieldColors,
					)

					// Error message
					if (error != null) {
						Spacer(modifier = Modifier.height(12.dp))

						Text(
							text = error,
							style =
								TextStyle(
									fontSize = 14.sp,
									color = VegafoXColors.Error,
								),
						)
					}

					Spacer(modifier = Modifier.height(24.dp))

					// Sign in button
					VegafoXButton(
						text = stringResource(R.string.action_login),
						onClick = { onLogin(username, password) },
						variant = VegafoXButtonVariant.Primary,
						enabled = !isLoading,
						modifier = Modifier.fillMaxWidth(),
					)

					Spacer(modifier = Modifier.height(12.dp))

					// Cancel button
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
					) {
						VegafoXButton(
							text = stringResource(R.string.btn_cancel),
							onClick = onCancel,
							variant = VegafoXButtonVariant.Ghost,
						)
					}
				}
			}
		}
	}
}
