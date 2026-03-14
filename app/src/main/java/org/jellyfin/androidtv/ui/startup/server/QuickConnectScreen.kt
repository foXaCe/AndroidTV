package org.jellyfin.androidtv.ui.startup.server

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.StartupDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun QuickConnectScreen(
	server: DiscoveredServer,
	onAuthenticated: (DiscoveredServer, String) -> Unit,
	onBack: () -> Unit,
	viewModel: QuickConnectViewModel = koinViewModel { parametersOf(server) },
) {
	val uiState by viewModel.uiState.collectAsState()

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.Background),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.width(StartupDimensions.dialogWidth),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			when {
				uiState.isLoading -> LoadingState(serverName = uiState.server.name)
				uiState.isAuthenticated ->
					AuthenticatedState(
						server = uiState.server,
						accessToken = uiState.accessToken.orEmpty(),
						onAuthenticated = onAuthenticated,
					)
				else ->
					CodeReadyState(
						uiState = uiState,
						onStartPolling = viewModel::startPolling,
						onBack = onBack,
					)
			}
		}
	}
}

// -- Etat 1 : Loading --

@Composable
private fun LoadingState(serverName: String) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxWidth(),
	) {
		Spinner()
		Spacer(Modifier.height(20.dp))
		Text(
			text = stringResource(R.string.quickconnect_connecting, serverName),
			style =
				TextStyle(
					fontSize = 14.sp,
					color = VegafoXColors.TextSecondary,
				),
		)
	}
}

@Composable
private fun Spinner() {
	val transition = rememberInfiniteTransition(label = "spin")
	val rotation by transition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(1000),
			),
		label = "spinRotation",
	)
	Box(
		modifier =
			Modifier
				.size(32.dp)
				.graphicsLayer { rotationZ = rotation }
				.border(
					width = 3.dp,
					brush =
						Brush.sweepGradient(
							listOf(VegafoXColors.OrangePrimary, Color.Transparent),
						),
					shape = CircleShape,
				),
	)
}

// -- Etat 2 : Code pret --

@Composable
private fun CodeReadyState(
	uiState: QuickConnectUiState,
	onStartPolling: () -> Unit,
	onBack: () -> Unit,
) {
	// Brand
	Text(
		text =
			buildAnnotatedString {
				withStyle(SpanStyle(color = VegafoXColors.BlueAccent)) {
					append("Vega")
				}
				withStyle(SpanStyle(color = VegafoXColors.OrangePrimary)) {
					append("foX")
				}
			},
		style =
			TextStyle(
				fontSize = 13.sp,
				letterSpacing = 3.sp,
			),
	)

	Spacer(Modifier.height(12.dp))

	// Title
	Text(
		text = stringResource(R.string.quickconnect_title),
		style =
			TextStyle(
				fontSize = 26.sp,
				fontWeight = FontWeight.Bold,
				color = VegafoXColors.TextPrimary,
			),
	)

	Spacer(Modifier.height(12.dp))

	// Instructions
	Text(
		text = stringResource(R.string.quickconnect_instructions),
		style =
			TextStyle(
				fontSize = 14.sp,
				color = VegafoXColors.TextSecondary,
				textAlign = TextAlign.Center,
				lineHeight = 20.sp,
			),
	)

	Spacer(Modifier.height(28.dp))

	// Code box
	CodeBox(code = uiState.code, isWaiting = uiState.isWaiting)

	Spacer(Modifier.height(24.dp))

	// Action button
	if (uiState.isWaiting) {
		VegafoXButton(
			text = stringResource(R.string.quickconnect_waiting),
			onClick = {},
			variant = VegafoXButtonVariant.Secondary,
			enabled = false,
			modifier = Modifier.fillMaxWidth(),
		)
	} else {
		VegafoXButton(
			text = stringResource(R.string.quickconnect_confirm),
			onClick = onStartPolling,
			variant = VegafoXButtonVariant.Primary,
			modifier = Modifier.fillMaxWidth(),
		)
	}

	// Error
	if (uiState.error != null) {
		val errorText =
			when (uiState.error) {
				TIMEOUT_ERROR -> stringResource(R.string.quickconnect_timeout)
				CONNECTION_ERROR -> stringResource(R.string.quickconnect_timeout)
				else -> uiState.error
			}
		Spacer(Modifier.height(12.dp))
		Text(
			text = errorText,
			style =
				TextStyle(
					fontSize = 13.sp,
					color = VegafoXColors.Error,
					textAlign = TextAlign.Center,
				),
		)
	}

	Spacer(Modifier.height(20.dp))

	// Back button
	VegafoXButton(
		text = stringResource(R.string.quickconnect_back),
		onClick = onBack,
		variant = VegafoXButtonVariant.Ghost,
	)
}

@Composable
private fun CodeBox(
	code: String,
	isWaiting: Boolean,
) {
	val parts = code.split("-")
	val left = parts.getOrElse(0) { code }
	val right = parts.getOrElse(1) { "" }

	Box(
		modifier =
			Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(16.dp))
				.background(VegafoXColors.Surface)
				.border(
					width = 1.dp,
					color = if (isWaiting) VegafoXColors.OrangeBorder else VegafoXColors.Divider,
					shape = RoundedCornerShape(16.dp),
				).then(
					if (isWaiting) {
						Modifier.drawBehind {
							drawRoundRect(
								color = VegafoXColors.OrangeGlow,
								cornerRadius = CornerRadius(16.dp.toPx()),
								blendMode = BlendMode.Screen,
							)
						}
					} else {
						Modifier
					},
				).padding(vertical = 20.dp, horizontal = 40.dp),
		contentAlignment = Alignment.Center,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Text(
				text = left,
				style =
					TextStyle(
						fontSize = 44.sp,
						fontWeight = FontWeight.Bold,
						color = VegafoXColors.OrangePrimary,
						fontFamily = FontFamily.Monospace,
					),
			)
			if (right.isNotEmpty()) {
				Text(
					text = "-",
					style =
						TextStyle(
							fontSize = 28.sp,
							color = VegafoXColors.TextHint,
						),
					modifier = Modifier.padding(horizontal = 4.dp),
				)
				Text(
					text = right,
					style =
						TextStyle(
							fontSize = 44.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.OrangePrimary,
							fontFamily = FontFamily.Monospace,
						),
				)
			}
		}
	}
}

// -- Etat 3 : Authentifie --

@Composable
private fun AuthenticatedState(
	server: DiscoveredServer,
	accessToken: String,
	onAuthenticated: (DiscoveredServer, String) -> Unit,
) {
	var visible by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		visible = true
	}

	LaunchedEffect(Unit) {
		delay(800)
		onAuthenticated(server, accessToken)
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth(),
	) {
		// Success icon
		AnimatedVisibility(
			visible = visible,
			enter =
				fadeIn(tween(400)) +
					scaleIn(
						initialScale = 0.9f,
						animationSpec = tween(400),
					),
		) {
			Box(
				modifier =
					Modifier
						.size(72.dp)
						.clip(CircleShape)
						.background(VegafoXColors.SuccessGlow)
						.border(2.dp, VegafoXColors.Success, CircleShape),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "\u2713",
					style =
						TextStyle(
							fontSize = 32.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.Success,
						),
				)
			}
		}

		Spacer(Modifier.height(24.dp))

		Text(
			text = stringResource(R.string.quickconnect_connected),
			style =
				TextStyle(
					fontSize = 24.sp,
					fontWeight = FontWeight.Bold,
					color = VegafoXColors.TextPrimary,
				),
		)

		Spacer(Modifier.height(8.dp))

		Text(
			text = "${server.name} · ${server.host}",
			style =
				TextStyle(
					fontSize = 14.sp,
					color = VegafoXColors.TextSecondary,
				),
		)
	}
}
