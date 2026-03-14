package org.jellyfin.androidtv.ui.base.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.base.theme.ButtonDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val ButtonShape = RoundedCornerShape(14.dp)
private const val FOCUS_ANIM_MS = 150
private const val DOUBLE_CLICK_GUARD_MS = 400L

// ---------------------------------------------------------------------------
// Variant
// ---------------------------------------------------------------------------

enum class VegafoXButtonVariant {
	Primary,
	Secondary,
	Outlined,
	Ghost,
}

private data class VariantColors(
	val container: Color,
	val content: Color,
	val borderColor: Color,
	val borderWidth: Float,
)

private fun variantColors(variant: VegafoXButtonVariant) =
	when (variant) {
		VegafoXButtonVariant.Primary ->
			VariantColors(
				container = VegafoXColors.OrangePrimary,
				content = VegafoXColors.Background,
				borderColor = Color.Transparent,
				borderWidth = 0f,
			)
		VegafoXButtonVariant.Secondary ->
			VariantColors(
				container = Color.Transparent,
				content = VegafoXColors.TextSecondary,
				borderColor = VegafoXColors.Divider,
				borderWidth = 1f,
			)
		VegafoXButtonVariant.Outlined ->
			VariantColors(
				container = VegafoXColors.OrangeSoft,
				content = VegafoXColors.OrangePrimary,
				borderColor = VegafoXColors.OrangeBorder,
				borderWidth = 1f,
			)
		VegafoXButtonVariant.Ghost ->
			VariantColors(
				container = Color.Transparent,
				content = VegafoXColors.TextSecondary,
				borderColor = Color.Transparent,
				borderWidth = 0f,
			)
	}

// ---------------------------------------------------------------------------
// VegafoXButton
// ---------------------------------------------------------------------------

@Composable
fun VegafoXButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	variant: VegafoXButtonVariant = VegafoXButtonVariant.Primary,
	enabled: Boolean = true,
	autoFocus: Boolean = false,
	icon: ImageVector? = null,
	iconEnd: Boolean = true,
	compact: Boolean = false,
) {
	val focusRequester = remember { FocusRequester() }
	var isFocused by remember { mutableStateOf(false) }
	var navigating by remember { mutableStateOf(false) }

	val colors = variantColors(variant)

	val scale by animateFloatAsState(
		targetValue = if (isFocused && enabled) 1.06f else 1f,
		animationSpec = tween(FOCUS_ANIM_MS, easing = EaseOutCubic),
		label = "vfxBtnScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused && enabled) 1f else 0f,
		animationSpec = tween(FOCUS_ANIM_MS, easing = EaseOutCubic),
		label = "vfxBtnGlow",
	)

	val safeClick: () -> Unit = {
		if (enabled && !navigating) {
			navigating = true
			onClick()
		}
	}

	// Reset navigating guard after delay
	LaunchedEffect(navigating) {
		if (navigating) {
			delay(DOUBLE_CLICK_GUARD_MS)
			navigating = false
		}
	}

	Box(
		modifier =
			modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					alpha = if (enabled) 1f else 0.4f
				}.defaultMinSize(
					minWidth = if (compact) ButtonDimensions.minWidthCompact else ButtonDimensions.minWidth,
					minHeight = if (compact) ButtonDimensions.heightCompact else ButtonDimensions.height,
				).drawBehind {
					if (glowAlpha > 0f) {
						drawRoundRect(
							brush =
								Brush.radialGradient(
									colors =
										listOf(
											VegafoXColors.OrangeGlow.copy(
												alpha = VegafoXColors.OrangeGlow.alpha * glowAlpha,
											),
											Color.Transparent,
										),
									radius = size.maxDimension * 1.2f,
								),
							cornerRadius = CornerRadius(14.dp.toPx()),
						)
					}
				}.border(
					width =
						if (isFocused && enabled) {
							2.dp
						} else {
							colors.borderWidth.dp
						},
					color =
						if (isFocused && enabled) {
							VegafoXColors.OrangePrimary
						} else {
							colors.borderColor
						},
					shape = ButtonShape,
				).clip(ButtonShape)
				.background(colors.container)
				.focusRequester(focusRequester)
				.onFocusChanged { isFocused = it.isFocused }
				.focusable(enabled)
				.onKeyEvent { event ->
					if (enabled &&
						(event.key == Key.Enter || event.key == Key.DirectionCenter) &&
						event.type == KeyEventType.KeyUp
					) {
						safeClick()
						true
					} else {
						false
					}
				}.clickable(
					enabled = enabled,
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = safeClick,
				),
		contentAlignment = Alignment.Center,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier =
				Modifier.padding(
					horizontal = if (compact) 20.dp else 32.dp,
					vertical = if (compact) 8.dp else 14.dp,
				),
		) {
			if (icon != null && !iconEnd) {
				ButtonIcon(icon = icon, tint = colors.content, focused = isFocused)
			}
			Text(
				text = text,
				style =
					TextStyle(
						fontSize = if (compact) 14.sp else 16.sp,
						fontWeight =
							if (isFocused && enabled) {
								FontWeight.ExtraBold
							} else {
								FontWeight.Bold
							},
						color = colors.content,
						textAlign = TextAlign.Center,
						letterSpacing = (-0.2).sp,
					),
			)
			if (icon != null && iconEnd) {
				ButtonIcon(icon = icon, tint = colors.content, focused = isFocused)
			}
		}
	}

	if (autoFocus) {
		LaunchedEffect(Unit) {
			delay(500)
			focusRequester.requestFocus()
		}
	}
}

@Composable
private fun ButtonIcon(
	icon: ImageVector,
	tint: Color,
	focused: Boolean,
) {
	Icon(
		imageVector = icon,
		contentDescription = null,
		tint = tint,
		modifier =
			Modifier
				.size(20.dp)
				.graphicsLayer {
					val iconScale = if (focused) 1.2f else 1f
					scaleX = iconScale
					scaleY = iconScale
				},
	)
}

// ---------------------------------------------------------------------------
// VegafoXIconButton
// ---------------------------------------------------------------------------

@Composable
fun VegafoXIconButton(
	icon: ImageVector,
	contentDescription: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	tint: Color = VegafoXColors.TextPrimary,
) {
	var isFocused by remember { mutableStateOf(false) }
	var navigating by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused && enabled) 1.06f else 1f,
		animationSpec = tween(FOCUS_ANIM_MS, easing = EaseOutCubic),
		label = "vfxIconScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused && enabled) 1f else 0f,
		animationSpec = tween(FOCUS_ANIM_MS, easing = EaseOutCubic),
		label = "vfxIconGlow",
	)

	val safeClick: () -> Unit = {
		if (enabled && !navigating) {
			navigating = true
			onClick()
		}
	}

	LaunchedEffect(navigating) {
		if (navigating) {
			delay(DOUBLE_CLICK_GUARD_MS)
			navigating = false
		}
	}

	Box(
		modifier =
			modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
					alpha = if (enabled) 1f else 0.4f
				}.size(48.dp)
				.drawBehind {
					if (glowAlpha > 0f) {
						drawCircle(
							brush =
								Brush.radialGradient(
									colors =
										listOf(
											VegafoXColors.OrangeGlow.copy(
												alpha = VegafoXColors.OrangeGlow.alpha * glowAlpha,
											),
											Color.Transparent,
										),
									radius = size.maxDimension * 0.8f,
								),
						)
					}
				}.border(
					width = if (isFocused && enabled) 2.dp else 0.dp,
					color =
						if (isFocused && enabled) {
							VegafoXColors.OrangePrimary
						} else {
							Color.Transparent
						},
					shape = CircleShape,
				).clip(CircleShape)
				.background(
					if (isFocused && enabled) {
						VegafoXColors.OrangeSoft
					} else {
						VegafoXColors.SurfaceBright
					},
				).onFocusChanged { isFocused = it.isFocused }
				.focusable(enabled)
				.onKeyEvent { event ->
					if (enabled &&
						(event.key == Key.Enter || event.key == Key.DirectionCenter) &&
						event.type == KeyEventType.KeyUp
					) {
						safeClick()
						true
					} else {
						false
					}
				}.clickable(
					enabled = enabled,
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = safeClick,
				),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			tint = tint,
			modifier =
				Modifier
					.size(24.dp)
					.graphicsLayer {
						val iconScale = if (isFocused) 1.2f else 1f
						scaleX = iconScale
						scaleY = iconScale
					},
		)
	}
}
