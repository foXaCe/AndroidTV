package org.jellyfin.androidtv.ui.startup.user

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val TRANSITION_MS = 200

@Composable
fun SpotCard(
	name: String,
	imageUrl: String?,
	isFocused: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val avatarSize by animateDpAsState(
		targetValue = if (isFocused) 96.dp else 80.dp,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotAvatarSize",
	)
	val borderWidth by animateDpAsState(
		targetValue = if (isFocused) 2.dp else 1.dp,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotBorderWidth",
	)
	val borderColor by animateColorAsState(
		targetValue = if (isFocused) VegafoXColors.OrangePrimary else VegafoXColors.Divider,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotBorderColor",
	)
	val offsetY by animateDpAsState(
		targetValue = if (isFocused) (-12).dp else 0.dp,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotOffsetY",
	)
	val contentAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0.65f,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotAlpha",
	)
	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotGlow",
	)
	val nameSize by animateFloatAsState(
		targetValue = if (isFocused) 14f else 12f,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotNameSize",
	)
	val nameColor by animateColorAsState(
		targetValue = if (isFocused) VegafoXColors.TextPrimary else VegafoXColors.TextSecondary,
		animationSpec = tween(TRANSITION_MS, easing = EaseOutCubic),
		label = "spotNameColor",
	)

	val density = LocalDensity.current
	val offsetYPx = with(density) { offsetY.toPx() }

	Column(
		modifier =
			modifier
				.graphicsLayer {
					translationY = offsetYPx
					alpha = contentAlpha
				}.onKeyEvent { event ->
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
				).focusable(),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		// Avatar area with glow + halo drawn behind
		Box(
			modifier =
				Modifier
					.size(avatarSize)
					.drawBehind {
						if (glowAlpha > 0f) {
							// Radial glow OrangeGlow 120dp
							val glowRadius = 120.dp.toPx()
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
										radius = glowRadius,
									),
								radius = glowRadius,
							)

							// Halo oval under the avatar
							val ovalW = size.width * 0.9f
							val ovalH = 20.dp.toPx()
							drawOval(
								color = VegafoXColors.OrangePrimary.copy(alpha = 0.18f * glowAlpha),
								topLeft =
									Offset(
										(size.width - ovalW) / 2f,
										size.height - ovalH * 0.4f,
									),
								size = Size(ovalW, ovalH),
							)
						}
					},
			contentAlignment = Alignment.Center,
		) {
			AsyncImage(
				model = imageUrl,
				contentDescription = name,
				contentScale = ContentScale.Crop,
				modifier =
					Modifier
						.size(avatarSize)
						.clip(CircleShape)
						.border(borderWidth, borderColor, CircleShape),
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Text(
			text = name,
			style =
				TextStyle(
					fontSize = nameSize.sp,
					fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
					color = nameColor,
					textAlign = TextAlign.Center,
				),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}
