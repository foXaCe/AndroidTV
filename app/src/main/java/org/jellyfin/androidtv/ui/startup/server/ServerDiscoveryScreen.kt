package org.jellyfin.androidtv.ui.startup.server

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.StartupDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun ServerDiscoveryScreen(
	onServerSelected: (DiscoveredServer, Boolean) -> Unit,
	onManualEntry: () -> Unit,
	viewModel: ServerDiscoveryViewModel = koinViewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	var showManualDialog by remember { mutableStateOf(false) }

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.Background),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.width(480.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// Section 1 — Header
			Header()

			Spacer(Modifier.height(32.dp))

			// Section 2 — ScanStatusBar
			ScanStatusBar(
				isScanning = uiState.isScanning,
				serverCount = uiState.servers.size,
			)

			Spacer(Modifier.height(20.dp))

			// Section 3 — ServerList
			ServerList(
				isScanning = uiState.isScanning,
				servers = uiState.servers,
				onServerClick = { server ->
					viewModel.selectServerAndNavigate(server) { qcAvailable ->
						onServerSelected(server, qcAvailable)
					}
				},
			)

			Spacer(Modifier.height(24.dp))

			// Section 4 — OrDivider
			OrDivider()

			Spacer(Modifier.height(24.dp))

			// Section 5 — NoServerDetected hint + ManualEntryButton
			if (uiState.noServerDetected) {
				Text(
					text = stringResource(R.string.lbl_no_server_detected),
					style =
						TextStyle(
							fontSize = 13.sp,
							color = VegafoXColors.TextSecondary,
						),
				)
				Spacer(Modifier.height(8.dp))
			}

			VegafoXButton(
				text = stringResource(R.string.discovery_manual_entry),
				onClick = { showManualDialog = true },
				variant =
					if (uiState.noServerDetected) {
						VegafoXButtonVariant.Outlined
					} else {
						VegafoXButtonVariant.Secondary
					},
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}

	if (showManualDialog) {
		ManualAddressDialog(
			initialAddress = viewModel.lastKnownAddress.orEmpty(),
			isChecking = uiState.isCheckingManual,
			error = uiState.manualError,
			onConfirm = { address ->
				viewModel.probeManualAddress(address) { server, qcAvailable ->
					showManualDialog = false
					onServerSelected(server, qcAvailable)
				}
			},
			onDismiss = { showManualDialog = false },
		)
	}
}

// -- Section 1 : Header --

@Composable
private fun Header() {
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
	Spacer(Modifier.height(8.dp))
	Text(
		text = stringResource(R.string.discovery_title),
		style =
			TextStyle(
				fontSize = 28.sp,
				fontWeight = FontWeight.Bold,
				color = VegafoXColors.TextPrimary,
			),
	)
	Spacer(Modifier.height(6.dp))
	Text(
		text = stringResource(R.string.discovery_subtitle),
		style =
			TextStyle(
				fontSize = 14.sp,
				color = VegafoXColors.TextSecondary,
			),
	)
}

// -- Section 2 : ScanStatusBar --

@Composable
private fun ScanStatusBar(
	isScanning: Boolean,
	serverCount: Int,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			if (isScanning) {
				PulsingDot(color = VegafoXColors.OrangePrimary)
			} else {
				Box(
					Modifier
						.size(8.dp)
						.clip(CircleShape)
						.background(VegafoXColors.Success),
				)
			}
			Text(
				text =
					if (isScanning) {
						stringResource(R.string.discovery_scanning)
					} else {
						pluralStringResource(R.plurals.discovery_servers_found, serverCount, serverCount)
					},
				style =
					TextStyle(
						fontSize = 13.sp,
						color = VegafoXColors.TextSecondary,
					),
			)
		}
		Text(
			text = stringResource(R.string.lbl_mdns_lan),
			style =
				TextStyle(
					fontSize = 11.sp,
					color = VegafoXColors.TextHint,
					fontFamily = FontFamily.Monospace,
				),
		)
	}
}

@Composable
private fun PulsingDot(color: Color) {
	val transition = rememberInfiniteTransition(label = "pulse")
	val alpha by transition.animateFloat(
		initialValue = 1f,
		targetValue = 0.2f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(800),
				repeatMode = RepeatMode.Reverse,
			),
		label = "dotAlpha",
	)
	Box(
		Modifier
			.size(8.dp)
			.graphicsLayer { this.alpha = alpha }
			.clip(CircleShape)
			.background(color),
	)
}

// -- Section 3 : ServerList --

@Composable
private fun ServerList(
	isScanning: Boolean,
	servers: List<DiscoveredServer>,
	onServerClick: (DiscoveredServer) -> Unit,
) {
	if (isScanning && servers.isEmpty()) {
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			SkeletonCard()
			SkeletonCard()
		}
	} else {
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			servers.forEach { server ->
				ServerCard(server = server, onClick = { onServerClick(server) })
			}
		}
	}
}

@Composable
private fun SkeletonCard() {
	val transition = rememberInfiniteTransition(label = "shimmer")
	val offset by transition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec =
			infiniteRepeatable(
				animation = tween(1200),
				repeatMode = RepeatMode.Reverse,
			),
		label = "shimmerOffset",
	)
	val brush =
		Brush.linearGradient(
			colors =
				listOf(
					VegafoXColors.Surface,
					VegafoXColors.SurfaceBright,
					VegafoXColors.Surface,
				),
			start =
				androidx.compose.ui.geometry
					.Offset(offset * 400f, 0f),
			end =
				androidx.compose.ui.geometry
					.Offset(offset * 400f + 200f, 0f),
		)
	Box(
		Modifier
			.fillMaxWidth()
			.height(76.dp)
			.clip(RoundedCornerShape(14.dp))
			.background(brush),
	)
}

// -- ServerCard --

@Composable
private fun ServerCard(
	server: DiscoveredServer,
	onClick: () -> Unit,
) {
	var focused by remember { mutableStateOf(false) }
	val scale by animateFloatAsState(
		targetValue = if (focused) 1.02f else 1f,
		animationSpec = tween(150),
		label = "cardScale",
	)

	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.clip(RoundedCornerShape(14.dp))
				.background(if (focused) VegafoXColors.OrangeSoft else VegafoXColors.Surface)
				.border(
					width = 1.dp,
					color = if (focused) VegafoXColors.OrangeBorder else VegafoXColors.Divider,
					shape = RoundedCornerShape(14.dp),
				).onFocusChanged { focused = it.isFocused }
				.focusable()
				.clickable(onClick = onClick)
				.padding(horizontal = 24.dp, vertical = 18.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Left: icon + server info
		Box(
			modifier =
				Modifier
					.size(40.dp)
					.clip(RoundedCornerShape(10.dp))
					.background(
						if (focused) {
							VegafoXColors.OrangePrimary
						} else {
							VegafoXColors.SurfaceBright
						},
					),
			contentAlignment = Alignment.Center,
		) {
			Text(
				text = "J",
				style =
					TextStyle(
						fontSize = 18.sp,
						fontWeight = FontWeight.Bold,
						color =
							if (focused) {
								VegafoXColors.Background
							} else {
								VegafoXColors.TextSecondary
							},
					),
			)
		}

		Spacer(Modifier.width(16.dp))

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = server.name,
				style =
					TextStyle(
						fontSize = 16.sp,
						fontWeight = FontWeight.SemiBold,
						color = VegafoXColors.TextPrimary,
					),
			)
			Spacer(Modifier.height(2.dp))
			Text(
				text = "${server.host}:${server.port}",
				style =
					TextStyle(
						fontSize = 12.sp,
						color = VegafoXColors.TextSecondary,
						fontFamily = FontFamily.Monospace,
					),
			)
		}

		// Right: ping + version
		Column(horizontalAlignment = Alignment.End) {
			PingIndicator(pingMs = server.pingMs)
			if (server.version.isNotBlank()) {
				Spacer(Modifier.height(2.dp))
				Text(
					text = server.version,
					style =
						TextStyle(
							fontSize = 10.sp,
							color = VegafoXColors.TextHint,
						),
				)
			}
		}
	}
}

// -- PingIndicator --

@Composable
private fun PingIndicator(pingMs: Long) {
	val dotColor =
		when {
			pingMs < 0 -> VegafoXColors.TextHint
			pingMs < 10 -> VegafoXColors.Success
			pingMs < 50 -> Color(0xFFEAB308)
			else -> VegafoXColors.Error
		}
	val label = if (pingMs >= 0) "${pingMs}ms" else "…"

	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		Box(
			Modifier
				.size(6.dp)
				.clip(CircleShape)
				.background(dotColor),
		)
		Text(
			text = label,
			style =
				TextStyle(
					fontSize = 11.sp,
					color = VegafoXColors.TextHint,
					fontFamily = FontFamily.Monospace,
				),
		)
	}
}

// -- Section 4 : OrDivider --

@Composable
private fun OrDivider() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			Modifier
				.weight(1f)
				.height(1.dp)
				.background(VegafoXColors.Divider),
		)
		Text(
			text = stringResource(R.string.discovery_or),
			modifier = Modifier.padding(horizontal = 16.dp),
			style =
				TextStyle(
					fontSize = 11.sp,
					color = VegafoXColors.TextHint,
				),
		)
		Box(
			Modifier
				.weight(1f)
				.height(1.dp)
				.background(VegafoXColors.Divider),
		)
	}
}

// -- ManualAddressDialog --

@Composable
private fun ManualAddressDialog(
	initialAddress: String,
	isChecking: Boolean,
	error: String?,
	onConfirm: (String) -> Unit,
	onDismiss: () -> Unit,
) {
	var address by remember { mutableStateOf(initialAddress) }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier =
				Modifier
					.width(StartupDimensions.dialogWidth)
					.clip(RoundedCornerShape(16.dp))
					.background(VegafoXColors.Surface)
					.padding(24.dp),
		) {
			Column {
				Text(
					text = stringResource(R.string.lbl_server_address),
					style =
						TextStyle(
							fontSize = 20.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.TextPrimary,
						),
				)

				Spacer(Modifier.height(16.dp))

				OutlinedTextField(
					value = address,
					onValueChange = { address = it },
					modifier = Modifier.fillMaxWidth(),
					placeholder = {
						Text(
							text = stringResource(R.string.server_address_hint),
							style = TextStyle(color = VegafoXColors.TextHint),
						)
					},
					isError = error != null,
					supportingText =
						if (error != null) {
							{
								Text(
									text = stringResource(R.string.lbl_enter_server_address),
									style = TextStyle(color = VegafoXColors.Error),
								)
							}
						} else {
							null
						},
					singleLine = true,
					colors =
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
						),
				)

				Spacer(Modifier.height(20.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					VegafoXButton(
						text = stringResource(R.string.btn_cancel),
						onClick = onDismiss,
						variant = VegafoXButtonVariant.Ghost,
						modifier = Modifier.weight(1f),
					)
					VegafoXButton(
						text = stringResource(R.string.btn_continue),
						onClick = { onConfirm(address) },
						variant = VegafoXButtonVariant.Primary,
						enabled = !isChecking && address.isNotBlank(),
						modifier = Modifier.weight(1f),
					)
				}
			}
		}
	}
}
