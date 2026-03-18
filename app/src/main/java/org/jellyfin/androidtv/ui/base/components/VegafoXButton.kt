package org.jellyfin.androidtv.ui.base.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.ButtonDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private const val GLASS_ANIM_MS = 120
private const val DOUBLE_CLICK_GUARD_MS = 400L
private val FastOutSlowIn = FastOutSlowInEasing
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

// ---------------------------------------------------------------------------
// Variant
// ---------------------------------------------------------------------------

enum class VegafoXButtonVariant {
	Primary,
	Secondary,
	Outlined,
	Ghost,
}

// ---------------------------------------------------------------------------
// VegafoXButton — Glass Dark Premium
// ---------------------------------------------------------------------------

@Composable
fun VegafoXButton(
	text: String = "",
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	variant: VegafoXButtonVariant = VegafoXButtonVariant.Primary,
	enabled: Boolean = true,
	autoFocus: Boolean = false,
	icon: ImageVector? = null,
	iconEnd: Boolean = true,
	compact: Boolean = false,
	iconTint: Color? = null,
	expandOnFocus: Boolean = false,
) {
	val focusRequester = remember { FocusRequester() }
	var isFocused by remember { mutableStateOf(false) }
	var isPressed by remember { mutableStateOf(false) }
	var navigating by remember { mutableStateOf(false) }

	val shape = RoundedCornerShape(ButtonDimensions.cornerRadius)
	val isGhost = variant == VegafoXButtonVariant.Ghost
	val isPrimary = variant == VegafoXButtonVariant.Primary
	val isSecondary = variant == VegafoXButtonVariant.Secondary
	val isOutlined = variant == VegafoXButtonVariant.Outlined

	// No translationY elevation — avoids misalignment between buttons

	// --- Container background color ---
	// Primary is dark/glass at rest, orange only on focus
	val containerTarget =
		when {
			!enabled ->
				when (variant) {
					VegafoXButtonVariant.Primary -> Color(0x14FFFFFF)
					VegafoXButtonVariant.Secondary -> Color(0x08FFFFFF)
					else -> Color.Transparent
				}
			isPressed ->
				when (variant) {
					VegafoXButtonVariant.Ghost -> Color.Transparent
					else -> Color(0xFFCC5500)
				}
			isFocused ->
				when (variant) {
					VegafoXButtonVariant.Primary -> Color(0xE5FF6B00)
					VegafoXButtonVariant.Secondary -> Color(0xE5FF6B00)
					VegafoXButtonVariant.Outlined -> Color(0xE5FF6B00)
					VegafoXButtonVariant.Ghost -> Color(0x33FF6B00)
				}
			else ->
				when (variant) {
					VegafoXButtonVariant.Primary -> Color(0x14FFFFFF)
					VegafoXButtonVariant.Secondary -> Color(0x0DFFFFFF)
					else -> Color.Transparent
				}
		}
	val containerColor by animateColorAsState(
		targetValue = containerTarget,
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
		label = "bg",
	)

	// --- Content (text) color ---
	// All variants: white text on focus (orange bg), light/muted at rest
	val contentTarget =
		when {
			isFocused && enabled -> Color.White
			isPrimary -> VegafoXColors.TextPrimary
			else -> VegafoXColors.TextSecondary
		}
	val contentColor by animateColorAsState(
		targetValue = contentTarget,
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
		label = "fg",
	)

	// --- Border color ---
	// All variants get orange border on focus
	val borderTarget =
		when {
			!enabled -> Color.Transparent
			isFocused -> Color(0xCCFF9632)
			isPrimary -> Color(0x1FFFFFFF)
			isSecondary -> Color(0x1FFFFFFF)
			isOutlined -> Color(0x2EFFFFFF)
			else -> Color.Transparent
		}
	val borderColor by animateColorAsState(
		targetValue = borderTarget,
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
		label = "brd",
	)

	// --- Shadow glow alpha ---
	// Orange glow on focus for all variants, nothing at rest
	val shadowGlowTarget =
		when {
			!enabled -> 0f
			isFocused -> 0.50f
			else -> 0f
		}
	val shadowGlow by animateFloatAsState(
		targetValue = shadowGlowTarget,
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
		label = "glow",
	)

	// --- Ghost press opacity ---
	val pressAlpha by animateFloatAsState(
		targetValue = if (isPressed && isGhost) 0.6f else 1f,
		animationSpec = tween(80, easing = FastOutSlowIn),
		label = "pa",
	)

	LaunchedEffect(isPressed) {
		if (isPressed) {
			delay(80)
			isPressed = false
		}
	}

	val safeClick: () -> Unit = {
		if (enabled && !navigating) {
			navigating = true
			isPressed = true
			onClick()
		}
	}

	LaunchedEffect(navigating) {
		if (navigating) {
			delay(DOUBLE_CLICK_GUARD_MS)
			navigating = false
		}
	}

	val borderW =
		when (variant) {
			VegafoXButtonVariant.Primary -> if (isFocused) 1.5f else 1f
			VegafoXButtonVariant.Secondary -> 1f
			VegafoXButtonVariant.Outlined -> 1.5f
			VegafoXButtonVariant.Ghost -> 0f
		}

	val cr = ButtonDimensions.cornerRadius
	val isExpandable = expandOnFocus && icon != null && text.isNotBlank()
	val effectiveIsIconOnly = if (isExpandable) !isFocused else text.isBlank()
	val compactH = if (compact) ButtonDimensions.heightCompact else ButtonDimensions.height

	Box(
		modifier =
			modifier
				.graphicsLayer {
					alpha = if (enabled) pressAlpha else 0.4f
				}.height(compactH)
				.then(
					if (isExpandable) {
						Modifier.widthIn(min = compactH)
					} else {
						Modifier
					},
				)
				// Diffuse glow shadow drawn behind/below the button
				.drawBehind {
					if (shadowGlow > 0f) {
						val glowColor = Color(0xFFFF6B00)
						// Layer 1: wide diffuse glow
						drawRoundRect(
							brush =
								Brush.verticalGradient(
									0f to glowColor.copy(alpha = shadowGlow * 0.3f),
									1f to Color.Transparent,
								),
							topLeft = Offset(-8.dp.toPx(), size.height * 0.4f),
							size =
								Size(
									size.width + 16.dp.toPx(),
									size.height * 0.9f,
								),
							cornerRadius = CornerRadius(cr.toPx() * 2f),
						)
						// Layer 2: tighter concentrated glow
						drawRoundRect(
							brush =
								Brush.verticalGradient(
									0f to glowColor.copy(alpha = shadowGlow * 0.5f),
									1f to Color.Transparent,
								),
							topLeft = Offset(4.dp.toPx(), size.height * 0.6f),
							size =
								Size(
									size.width - 8.dp.toPx(),
									size.height * 0.6f,
								),
							cornerRadius = CornerRadius(cr.toPx()),
						)
					}
				}.border(
					width = borderW.dp,
					color = borderColor,
					shape = shape,
				).clip(shape)
				.background(containerColor)
				// Glass highlight: top edge bright line + subtle top gradient
				.drawWithContent {
					drawContent()
					// Top highlight: glass reflection on focus for all non-ghost variants
					val showHighlight = isFocused && enabled && !isGhost
					if (showHighlight) {
						val hlAlpha = 0.25f
						// Bright edge line
						drawRect(
							color = Color.White.copy(alpha = hlAlpha),
							topLeft = Offset.Zero,
							size = Size(size.width, 1.dp.toPx()),
						)
						// Subtle glass gradient in top third
						drawRect(
							brush =
								Brush.verticalGradient(
									0f to Color.White.copy(alpha = hlAlpha * 0.4f),
									1f to Color.Transparent,
								),
							topLeft = Offset.Zero,
							size = Size(size.width, size.height * 0.35f),
						)
					}
					// Ghost underline on focus
					if (isGhost && isFocused && enabled) {
						drawRect(
							color = VegafoXColors.OrangePrimary,
							topLeft = Offset(4.dp.toPx(), size.height - 2.dp.toPx()),
							size = Size(size.width - 8.dp.toPx(), 2.dp.toPx()),
						)
					}
				}.focusRequester(focusRequester)
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
		val effectiveIconTint =
			when {
				iconTint != null && isFocused && enabled -> Color.White
				iconTint != null -> iconTint
				else -> contentColor
			}
		val iconSizeDp = if (effectiveIsIconOnly) 22.dp else 20.dp
		val padH =
			when {
				isExpandable && !isFocused -> 9.dp
				effectiveIsIconOnly -> 0.dp
				else -> 24.dp
			}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(horizontal = padH),
		) {
			if (icon != null && !iconEnd) {
				GlassButtonIcon(icon = icon, tint = effectiveIconTint, focused = isFocused, size = iconSizeDp)
			}
			if (isExpandable) {
				AnimatedVisibility(
					visible = isFocused,
					enter =
						expandHorizontally(
							expandFrom = Alignment.Start,
							animationSpec = tween(180, easing = EaseOutCubic),
						) + fadeIn(tween(120, delayMillis = 60)),
					exit =
						shrinkHorizontally(
							shrinkTowards = Alignment.Start,
							animationSpec = tween(160, easing = EaseOutCubic),
						) + fadeOut(tween(80)),
				) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = text.uppercase(),
							maxLines = 1,
							style =
								TextStyle(
									fontFamily = BebasNeue,
									fontSize = 15.sp,
									fontWeight = FontWeight.Bold,
									color = contentColor,
									textAlign = TextAlign.Center,
									letterSpacing = 1.5.sp,
								),
						)
						Spacer(modifier = Modifier.width(6.dp))
					}
				}
			} else if (!effectiveIsIconOnly) {
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = text.uppercase(),
					maxLines = 1,
					style =
						TextStyle(
							fontFamily = BebasNeue,
							fontSize = 15.sp,
							fontWeight = FontWeight.Bold,
							color = contentColor,
							textAlign = TextAlign.Center,
							letterSpacing = 1.5.sp,
						),
				)
			}
			if (icon != null && iconEnd) {
				GlassButtonIcon(icon = icon, tint = effectiveIconTint, focused = isFocused, size = iconSizeDp)
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
private fun GlassButtonIcon(
	icon: ImageVector,
	tint: Color,
	focused: Boolean,
	size: Dp = 20.dp,
) {
	Icon(
		imageVector = icon,
		contentDescription = null,
		tint = tint,
		modifier =
			Modifier
				.size(size)
				.graphicsLayer {
					val iconScale = if (focused) 1.15f else 1f
					scaleX = iconScale
					scaleY = iconScale
				},
	)
}

// ---------------------------------------------------------------------------
// VegafoXIconButton — Glass Dark
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
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
		label = "vfxIconScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused && enabled) 1f else 0f,
		animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowIn),
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
					color = if (isFocused && enabled) VegafoXColors.OrangePrimary else Color.Transparent,
					shape = CircleShape,
				).clip(CircleShape)
				.background(
					if (isFocused && enabled) VegafoXColors.OrangeSoft else VegafoXColors.SurfaceBright,
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
