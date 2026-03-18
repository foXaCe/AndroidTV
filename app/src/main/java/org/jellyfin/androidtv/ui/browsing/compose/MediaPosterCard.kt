package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.shared.components.CachedAsyncImage

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Unified poster card for browse screens — replaces FolderItemCard and MusicSquareCard.
 *
 * Uses [TvFocusCard] for consistent TV focus handling:
 * - Scale 1.06× animated natively (no manual graphicsLayer)
 * - 2dp orange border on focus
 * - No alpha dimming on unfocused state
 *
 * @param imageUrl URL of the poster image, or null for placeholder.
 * @param title Item title displayed below the poster.
 * @param cardWidth Card width in dp.
 * @param cardHeight Card height in dp.
 * @param onClick Called on D-pad center / enter.
 * @param onFocused Called when the card gains focus.
 * @param subtitle Optional subtitle below the title.
 * @param placeholderIcon Icon shown when imageUrl is null (default: VegafoXIcons.Movie).
 */
@Composable
fun MediaPosterCard(
	imageUrl: String?,
	title: String,
	cardWidth: Dp,
	cardHeight: Dp,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
	placeholderIcon: ImageVector? = null,
) {
	var isFocused by remember { mutableStateOf(false) }

	// 3D relief animation
	val cardShape = JellyfinTheme.shapes.medium
	val animatedElevation by animateFloatAsState(
		targetValue = if (isFocused) 28f else 16f,
		animationSpec = tween(200, easing = EaseOutCubic),
		label = "cardElevation",
	)
	val animatedTranslationY by animateFloatAsState(
		targetValue = if (isFocused) -3f else 0f,
		animationSpec = tween(200, easing = EaseOutCubic),
		label = "cardTranslationY",
	)

	TvFocusCard(
		onClick = onClick,
		modifier =
			modifier
				.width(cardWidth)
				.onFocusChanged { state ->
					isFocused = state.isFocused
					if (state.isFocused) onFocused()
				},
	) {
		Column {
			Box(
				modifier =
					Modifier
						.size(cardWidth, cardHeight)
						.graphicsLayer {
							shadowElevation = animatedElevation.dp.toPx()
							translationY = animatedTranslationY
							shape = cardShape
							clip = true
							spotShadowColor = Color.Black
							ambientShadowColor = VegafoXColors.PosterShadowDark
						}.border(
							width = 1.dp,
							brush =
								Brush.linearGradient(
									colors =
										listOf(
											VegafoXColors.PosterBorderLight,
											VegafoXColors.PosterBorderDark,
										),
								),
							shape = cardShape,
						).clip(cardShape)
						.background(JellyfinTheme.colorScheme.surfaceDim),
				contentAlignment = Alignment.Center,
			) {
				if (imageUrl != null) {
					val density = LocalDensity.current
					val widthPx = remember(cardWidth) { with(density) { cardWidth.roundToPx() } }
					val heightPx = remember(cardHeight) { with(density) { cardHeight.roundToPx() } }
					CachedAsyncImage(
						model = imageUrl,
						contentDescription = title,
						modifier = Modifier.fillMaxSize(),
						maxWidth = widthPx,
						maxHeight = heightPx,
					)
				} else {
					Icon(
						imageVector = placeholderIcon ?: VegafoXIcons.Movie,
						contentDescription = null,
						modifier = Modifier.size(48.dp),
						tint = JellyfinTheme.colorScheme.textDisabled,
					)
				}

				// Relief: left edge reflection
				Box(
					modifier =
						Modifier
							.fillMaxSize()
							.background(
								Brush.horizontalGradient(
									0f to VegafoXColors.PosterReflectLight,
									0.15f to Color.Transparent,
									1f to Color.Transparent,
								),
							),
				)

				// Relief: right edge shadow
				Box(
					modifier =
						Modifier
							.fillMaxSize()
							.background(
								Brush.horizontalGradient(
									0f to Color.Transparent,
									0.8f to Color.Transparent,
									1f to VegafoXColors.PosterShadowDark,
								),
							),
				)
			}

			Spacer(modifier = Modifier.height(5.dp))

			Text(
				text = title,
				style = JellyfinTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				color = VegafoXColors.TextSecondary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			if (subtitle != null) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.labelSmall,
					fontWeight = FontWeight.Normal,
					color = VegafoXColors.TextHint,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}
