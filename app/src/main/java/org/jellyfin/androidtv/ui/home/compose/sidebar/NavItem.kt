package org.jellyfin.androidtv.ui.home.compose.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import org.jellyfin.androidtv.ui.base.theme.SidebarDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private val IconSize = 22.dp
private val IconCircleSize = 44.dp
private val NormalIconColor = Color(0xFF5A5A6E)
private val SelectedBackground = VegafoXColors.OrangePrimary.copy(alpha = 0.14f)
private val FocusedBackground = Color(0xFF1C1C22).copy(alpha = 0.9f)
private val GlowColor = VegafoXColors.OrangePrimary.copy(alpha = 0.25f)
private val LabelInactiveColor = Color(0xFF7A7A8E)

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val ANIM_MS = 140
private const val LABEL_FADE_MS = 150

/**
 * Single navigation item for the premium sidebar.
 * Supports D-pad focus with scale, glow, color transitions, inline label, and shimmer.
 *
 * @param shimmerAlpha opacity multiplier applied to inactive icons (0.40–0.55 during shimmer)
 * @param onFocusChanged callback reporting focus state changes to the parent
 * @param isExpanded whether the sidebar is in expanded mode (shows label next to icon)
 */
@Composable
fun NavItem(
	icon: ImageVector,
	label: String,
	isSelected: Boolean,
	onSelect: () -> Unit,
	modifier: Modifier = Modifier,
	shimmerAlpha: Float = 1f,
	isExpanded: Boolean = false,
	onFocusChanged: ((Boolean) -> Unit)? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	// Report focus changes to parent for shimmer control
	LaunchedEffect(isFocused) {
		onFocusChanged?.invoke(isFocused)
	}

	val animSpec = tween<Color>(ANIM_MS, easing = EaseOutCubic)

	val iconColor by animateColorAsState(
		targetValue =
			when {
				isFocused -> VegafoXColors.TextPrimary
				isSelected -> VegafoXColors.OrangePrimary
				else -> NormalIconColor
			},
		animationSpec = animSpec,
		label = "navIconColor",
	)

	val circleBg by animateColorAsState(
		targetValue =
			when {
				isFocused -> FocusedBackground
				isSelected -> SelectedBackground
				else -> Color.Transparent
			},
		animationSpec = animSpec,
		label = "navCircleBg",
	)

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.10f else 1f,
		animationSpec = tween(ANIM_MS, easing = EaseOutCubic),
		label = "navScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = tween(ANIM_MS, easing = EaseOutCubic),
		label = "navGlow",
	)

	// Row: icon circle + optional label
	Row(
		modifier = modifier.fillMaxWidth().height(SidebarDimensions.navItemHeight),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Icon container — centered in collapsed sidebar width
		Box(
			modifier =
				Modifier
					.width(PREMIUM_SIDEBAR_WIDTH_COLLAPSED)
					.height(SidebarDimensions.navItemHeight),
			contentAlignment = Alignment.Center,
		) {
			// Orange selection indicator
			if (isSelected) {
				Box(
					modifier =
						Modifier
							.align(Alignment.CenterStart)
							.width(4.dp)
							.height(20.dp)
							.clip(RoundedCornerShape(2.dp))
							.background(VegafoXColors.OrangePrimary),
				)
			}

			Box(
				modifier =
					Modifier
						.graphicsLayer {
							scaleX = scale
							scaleY = scale
						}.size(IconCircleSize)
						.drawBehind {
							if (glowAlpha > 0f) {
								drawCircle(
									brush =
										Brush.radialGradient(
											colors =
												listOf(
													GlowColor.copy(alpha = GlowColor.alpha * glowAlpha),
													Color.Transparent,
												),
											radius = size.maxDimension * 0.8f,
										),
								)
							}
						}.clip(CircleShape)
						.background(circleBg)
						.focusable(interactionSource = interactionSource)
						.onKeyEvent { event ->
							if ((event.key == Key.Enter || event.key == Key.DirectionCenter) &&
								event.type == KeyEventType.KeyUp
							) {
								onSelect()
								true
							} else {
								false
							}
						}.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null,
							onClick = onSelect,
						),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = icon,
					contentDescription = label,
					tint = iconColor,
					modifier =
						Modifier
							.size(IconSize)
							.graphicsLayer {
								alpha = if (!isFocused && !isSelected) shimmerAlpha else 1f
							},
				)
			}
		}

		// Inline label — visible when sidebar is expanded
		AnimatedVisibility(
			visible = isExpanded,
			enter = fadeIn(tween(LABEL_FADE_MS)),
			exit = fadeOut(tween(LABEL_FADE_MS)),
		) {
			Row {
				Spacer(modifier = Modifier.width(16.dp))
				Text(
					text = label,
					style =
						TextStyle(
							fontSize = 14.sp,
							fontWeight = if (isSelected || isFocused) FontWeight.Medium else FontWeight.Normal,
							color = if (isSelected || isFocused) VegafoXColors.TextPrimary else LabelInactiveColor,
						),
					maxLines = 1,
				)
			}
		}
	}
}
