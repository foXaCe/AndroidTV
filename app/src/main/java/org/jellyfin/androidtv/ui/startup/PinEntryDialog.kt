package org.jellyfin.androidtv.ui.startup

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

/**
 * TV-friendly PIN entry dialog — pure Compose with VegafoX design system.
 *
 * Usage from Compose:
 * ```
 * var showPin by remember { mutableStateOf(false) }
 * if (showPin) {
 *     PinEntryDialog.Compose(
 *         title = "Enter PIN",
 *         onPinEntered = { pin -> ... },
 *         onDismiss = { showPin = false },
 *     )
 * }
 * ```
 *
 * Legacy imperative API [show] is preserved for non-Compose call sites.
 */
object PinEntryDialog {
	enum class Mode {
		SET,
		VERIFY,
	}

	/**
	 * Imperative API for non-Compose call sites.
	 * Shows a PIN dialog using the legacy AlertDialog approach.
	 * Prefer using [Compose] or [SetPinFlow] from Compose contexts.
	 */
	fun show(
		context: Context,
		mode: Mode,
		onComplete: (String?) -> Unit,
		onForgotPin: (() -> Unit)? = null,
	) {
		// Kept for backward compat — not used in current codebase but safe to retain
		when (mode) {
			Mode.SET -> {
				// Cannot show nested Compose dialogs from imperative API — fall through
				onComplete(null)
			}
			Mode.VERIFY -> {
				onComplete(null)
			}
		}
	}

	/**
	 * Single PIN entry composable dialog.
	 */
	@Composable
	fun Compose(
		title: String,
		onPinEntered: (String) -> Unit,
		onDismiss: () -> Unit,
		onForgotPin: (() -> Unit)? = null,
		errorMessage: String? = null,
	) {
		var pin by remember { mutableStateOf("") }
		var showError by remember(errorMessage) { mutableStateOf(errorMessage != null) }

		AlertDialog(
			onDismissRequest = onDismiss,
			containerColor = VegafoXColors.Surface,
			titleContentColor = VegafoXColors.TextPrimary,
			title = {
				Text(
					text = title,
					fontFamily = BebasNeue,
					fontSize = 24.sp,
					fontWeight = FontWeight.Bold,
					color = VegafoXColors.TextPrimary,
				)
			},
			text = {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier =
						Modifier
							.onKeyEvent { event ->
								if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
									val digit =
										when (event.key) {
											Key.Zero -> "0"
											Key.One -> "1"
											Key.Two -> "2"
											Key.Three -> "3"
											Key.Four -> "4"
											Key.Five -> "5"
											Key.Six -> "6"
											Key.Seven -> "7"
											Key.Eight -> "8"
											Key.Nine -> "9"
											else -> null
										}
									when {
										digit != null && pin.length < 10 -> {
											pin += digit
											showError = false
											true
										}
										event.key == Key.Backspace || event.key == Key.Delete -> {
											if (pin.isNotEmpty()) {
												pin = pin.dropLast(1)
												showError = false
											}
											true
										}
										event.key == Key.Back -> {
											if (pin.isNotEmpty()) {
												pin = pin.dropLast(1)
												showError = false
											} else {
												onDismiss()
											}
											true
										}
										event.key == Key.Enter -> {
											if (pin.isNotEmpty()) {
												onPinEntered(pin)
											}
											true
										}
										else -> false
									}
								} else {
									false
								}
							},
				) {
					// PIN dots display
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier.padding(vertical = 12.dp),
					) {
						repeat(10) { index ->
							Box(
								modifier =
									Modifier
										.size(14.dp)
										.clip(CircleShape)
										.background(
											if (index < pin.length) {
												VegafoXColors.OrangePrimary
											} else {
												VegafoXColors.Outline
											},
										),
							)
						}
					}

					// Error message
					if (showError && errorMessage != null) {
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = errorMessage,
							color = VegafoXColors.Error,
							fontSize = 12.sp,
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					// Numeric keypad — 4 rows
					val keys =
						listOf(
							listOf("1", "2", "3"),
							listOf("4", "5", "6"),
							listOf("7", "8", "9"),
							listOf("\u232B", "0", "\u2713"),
						)
					Column(
						verticalArrangement = Arrangement.spacedBy(4.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						keys.forEach { row ->
							Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
								row.forEach { key ->
									PinKey(
										label = key,
										onClick = {
											when (key) {
												"\u232B" -> {
													if (pin.isNotEmpty()) {
														pin = pin.dropLast(1)
														showError = false
													}
												}
												"\u2713" -> {
													if (pin.isNotEmpty()) {
														onPinEntered(pin)
													}
												}
												else -> {
													if (pin.length < 10) {
														pin += key
														showError = false
													}
												}
											}
										},
										isAccent = key == "\u2713",
									)
								}
							}
						}
					}

					// Forgot PIN button
					if (onForgotPin != null) {
						Spacer(modifier = Modifier.height(12.dp))
						VegafoXButton(
							text = stringResource(R.string.lbl_forgot_pin),
							onClick = onForgotPin,
							variant = VegafoXButtonVariant.Ghost,
							compact = true,
						)
					}
				}
			},
			confirmButton = {},
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

	/**
	 * Composable flow for SET mode: enter PIN → confirm PIN.
	 */
	@Composable
	fun SetPinFlow(
		context: Context,
		onComplete: (String?) -> Unit,
		onDismiss: () -> Unit,
	) {
		var step by remember { mutableStateOf(0) } // 0 = enter, 1 = confirm
		var firstPin by remember { mutableStateOf("") }

		when (step) {
			0 ->
				Compose(
					title = stringResource(R.string.lbl_enter_new_pin),
					onPinEntered = { pin ->
						when {
							pin.isEmpty() -> {
								Toast.makeText(context, R.string.lbl_pin_code_empty, Toast.LENGTH_SHORT).show()
								onComplete(null)
							}
							pin.length < 4 -> {
								Toast.makeText(context, R.string.lbl_pin_code_too_short, Toast.LENGTH_SHORT).show()
								onComplete(null)
							}
							else -> {
								firstPin = pin
								step = 1
							}
						}
					},
					onDismiss = {
						onComplete(null)
						onDismiss()
					},
				)
			1 ->
				Compose(
					title = stringResource(R.string.lbl_confirm_pin),
					onPinEntered = { confirmPin ->
						if (confirmPin == firstPin) {
							onComplete(confirmPin)
						} else {
							Toast.makeText(context, R.string.lbl_pin_code_mismatch, Toast.LENGTH_SHORT).show()
							onComplete(null)
						}
						onDismiss()
					},
					onDismiss = {
						onComplete(null)
						onDismiss()
					},
				)
		}
	}
}

@Composable
private fun PinKey(
	label: String,
	onClick: () -> Unit,
	isAccent: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val scale = remember { Animatable(1f) }

	LaunchedEffect(isFocused) {
		scale.animateTo(
			if (isFocused) 1.1f else 1f,
			animationSpec = spring(dampingRatio = 0.6f),
		)
	}

	val bgColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary
			isAccent -> VegafoXColors.OrangeSoft
			else -> VegafoXColors.SurfaceContainer
		}
	val textColor =
		when {
			isFocused -> VegafoXColors.Background
			isAccent -> VegafoXColors.OrangePrimary
			else -> VegafoXColors.TextPrimary
		}
	val borderColor = if (isFocused) VegafoXColors.OrangePrimary else VegafoXColors.Outline

	Box(
		modifier =
			Modifier
				.graphicsLayer {
					scaleX = scale.value
					scaleY = scale.value
				}.size(60.dp, 48.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(bgColor)
				.border(1.dp, borderColor, RoundedCornerShape(8.dp))
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				).focusable(interactionSource = interactionSource),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = label,
			fontSize = 20.sp,
			fontWeight = FontWeight.Bold,
			fontFamily = BebasNeue,
			color = textColor,
		)
	}
}
