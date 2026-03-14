package org.jellyfin.androidtv.ui.startup.user

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private const val MAX_PIN_LENGTH = 10
private const val AUTO_SUBMIT_LENGTH = 4

@Composable
fun PinEntryScreen(
	userName: String,
	error: String?,
	onPinEntered: (String) -> Unit,
	onCancel: () -> Unit,
	onForgotPin: () -> Unit,
) {
	val digits = remember { mutableStateListOf<Int>() }
	val shakeOffset = remember { Animatable(0f) }
	val firstKeyFocusRequester = remember { FocusRequester() }

	// Auto-submit when 4 digits entered
	LaunchedEffect(digits.size) {
		if (digits.size >= AUTO_SUBMIT_LENGTH) {
			onPinEntered(digits.joinToString(""))
		}
	}

	// Shake animation on error, then clear
	LaunchedEffect(error) {
		if (error != null) {
			// Shake: right → left → right → center
			val shake = listOf(12f, -12f, 8f, -8f, 4f, -4f, 0f)
			for (offset in shake) {
				shakeOffset.animateTo(offset, animationSpec = tween(50))
			}
			digits.clear()
		}
	}

	// Auto-focus first key
	LaunchedEffect(Unit) {
		firstKeyFocusRequester.requestFocus()
	}

	Dialog(
		onDismissRequest = onCancel,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier =
				Modifier
					.width(360.dp)
					.clip(RoundedCornerShape(20.dp))
					.background(VegafoXColors.Surface, RoundedCornerShape(20.dp)),
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.padding(28.dp),
			) {
				// Title
				Text(
					text = stringResource(R.string.lbl_enter_pin),
					style =
						TextStyle(
							fontSize = 22.sp,
							fontWeight = FontWeight.Bold,
							color = VegafoXColors.TextPrimary,
							textAlign = TextAlign.Center,
						),
				)

				Spacer(modifier = Modifier.height(8.dp))

				// User name
				Text(
					text = userName,
					style =
						TextStyle(
							fontSize = 14.sp,
							fontWeight = FontWeight.Normal,
							color = VegafoXColors.TextSecondary,
							textAlign = TextAlign.Center,
						),
				)

				Spacer(modifier = Modifier.height(24.dp))

				// PIN dots
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.graphicsLayer { translationX = shakeOffset.value },
				) {
					repeat(AUTO_SUBMIT_LENGTH) { index ->
						val filled = index < digits.size
						Box(
							modifier =
								Modifier
									.size(16.dp)
									.clip(CircleShape)
									.then(
										if (filled) {
											Modifier.background(VegafoXColors.OrangePrimary, CircleShape)
										} else {
											Modifier.border(1.5.dp, VegafoXColors.Divider, CircleShape)
										},
									),
						)
					}
				}

				// Error text
				if (error != null) {
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = error,
						style =
							TextStyle(
								fontSize = 13.sp,
								color = VegafoXColors.Error,
								textAlign = TextAlign.Center,
							),
					)
				}

				Spacer(modifier = Modifier.height(24.dp))

				// Numpad: 3 columns, rows 1-9 then empty/0/clear
				val keys =
					listOf(
						listOf(1, 2, 3),
						listOf(4, 5, 6),
						listOf(7, 8, 9),
						listOf(-1, 0, -2), // -1 = empty, -2 = backspace
					)

				Column(
					verticalArrangement = Arrangement.spacedBy(8.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					keys.forEachIndexed { rowIndex, row ->
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
						) {
							row.forEachIndexed { colIndex, key ->
								when {
									key == -1 -> {
										// Empty placeholder
										Spacer(modifier = Modifier.size(72.dp))
									}
									key == -2 -> {
										// Backspace key
										NumpadKey(
											label = "\u232B",
											onClick = {
												if (digits.isNotEmpty()) digits.removeAt(digits.lastIndex)
											},
										)
									}
									else -> {
										NumpadKey(
											label = key.toString(),
											onClick = {
												if (digits.size < MAX_PIN_LENGTH) {
													digits.add(key)
												}
											},
											focusRequester = if (rowIndex == 0 && colIndex == 0) firstKeyFocusRequester else null,
										)
									}
								}
							}
						}
					}
				}

				Spacer(modifier = Modifier.height(20.dp))

				// Cancel button
				VegafoXButton(
					text = stringResource(R.string.lbl_cancel),
					onClick = onCancel,
					variant = VegafoXButtonVariant.Ghost,
				)

				Spacer(modifier = Modifier.height(8.dp))

				// Forgot PIN
				Text(
					text = stringResource(R.string.lbl_forgot_pin),
					style =
						TextStyle(
							fontSize = 13.sp,
							color = VegafoXColors.OrangePrimary,
							textAlign = TextAlign.Center,
						),
					modifier =
						Modifier.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null,
							onClick = onForgotPin,
						),
				)
			}
		}
	}
}

@Composable
private fun NumpadKey(
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	focusRequester: FocusRequester? = null,
) {
	var isFocused by remember { mutableStateOf(false) }

	Box(
		modifier =
			modifier
				.size(72.dp)
				.then(
					if (isFocused) {
						Modifier
							.border(2.dp, VegafoXColors.OrangePrimary, CircleShape)
							.background(VegafoXColors.OrangeSoft, CircleShape)
					} else {
						Modifier.background(VegafoXColors.SurfaceBright, CircleShape)
					},
				).clip(CircleShape)
				.then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
				.onFocusChanged { isFocused = it.isFocused }
				.focusable()
				.onKeyEvent { event ->
					if ((event.key == Key.Enter || event.key == Key.DirectionCenter) &&
						event.type == KeyEventType.KeyUp
					) {
						onClick()
						true
					} else {
						false
					}
				}.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onClick,
				),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = label,
			style =
				TextStyle(
					fontSize = 24.sp,
					fontWeight = FontWeight.Bold,
					color = VegafoXColors.TextPrimary,
					textAlign = TextAlign.Center,
				),
		)
	}
}
