package org.jellyfin.androidtv.ui.browsing.compose

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.shared.components.CachedAsyncImage
import org.jellyfin.androidtv.util.apiclient.getCardImageUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber

/** Card image size in pixels — matches 220dp x 124dp at ~2x density. */
private const val CARD_IMAGE_WIDTH_PX = 440
private const val CARD_IMAGE_HEIGHT_PX = 248
private const val OVERLAY_SHOW_DELAY_MS = 5_000L

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * A reusable media item card for browse screens.
 * Uses [TvFocusCard] for D-pad focus handling and displays a 16:9 landscape image.
 *
 * - **Short press** (OK/Enter): opens item details via [onClick].
 * - **Long press** (hold OK/Enter): launches direct playback via [onPlayClick].
 * - After 5s of stable focus, a play overlay appears centered on the card.
 */
@Composable
fun BrowseMediaCard(
	item: BaseItemDto,
	api: ApiClient,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocus: (() -> Unit)? = null,
	onBlur: (() -> Unit)? = null,
	onPlayClick: (() -> Unit)? = null,
	hasProgress: Boolean = (item.userData?.playedPercentage ?: 0.0) > 0,
	cardWidth: Dp = CardDimensions.landscapeWidth,
	initialFocusRequester: FocusRequester? = null,
) {
	val cardImageHeight = cardWidth * (9f / 16f)
	var isFocused by remember { mutableStateOf(false) }
	var showOverlay by remember { mutableStateOf(false) }
	var longPressConsumed by remember { mutableStateOf(false) }

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

	// Show play overlay after 5s of stable focus
	LaunchedEffect(isFocused) {
		if (isFocused) {
			delay(OVERLAY_SHOW_DELAY_MS)
			showOverlay = true
		} else {
			showOverlay = false
		}
	}

	TvFocusCard(
		onClick = onClick,
		modifier =
			modifier
				.width(cardWidth)
				// Long press → direct playback
				.onPreviewKeyEvent { event ->
					val keyCode = event.nativeKeyEvent.keyCode
					if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
						when {
							event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
								event.nativeKeyEvent.isLongPress -> {
								longPressConsumed = true
								onPlayClick?.invoke() ?: onClick()
								true
							}
							event.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
								longPressConsumed -> {
								longPressConsumed = false
								true // swallow UP so Surface.onClick doesn't fire
							}
							else -> false
						}
					} else {
						false
					}
				}.then(
					if (initialFocusRequester != null) {
						Modifier.focusRequester(initialFocusRequester)
					} else {
						Modifier
					},
				).then(
					if (onFocus != null || onBlur != null) {
						Modifier.onFocusChanged { state ->
							isFocused = state.isFocused
							if (state.isFocused) {
								onFocus?.invoke()
							} else {
								onBlur?.invoke()
							}
						}
					} else {
						Modifier.onFocusChanged { isFocused = it.isFocused }
					},
				),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(cardImageHeight)
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
		) {
			val imageUrl = item.getCardImageUrl(api)
			if (imageUrl != null) {
				CachedAsyncImage(
					model = imageUrl,
					contentDescription = item.name,
					modifier = Modifier.fillMaxSize(),
					maxWidth = CARD_IMAGE_WIDTH_PX,
					maxHeight = CARD_IMAGE_HEIGHT_PX,
				)
			} else {
				LaunchedEffect(item.id) {
					Timber.tag("VFX_IMG").w("VFX_IMG image_url_null for item: ${item.name} type: ${item.type}")
				}
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center,
				) {
					Icon(
						imageVector = VegafoXIcons.Movie,
						contentDescription = null,
						modifier = Modifier.size(48.dp),
						tint = JellyfinTheme.colorScheme.textDisabled,
					)
				}
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

			// Progress bar for partially watched items
			val playedPercentage = item.userData?.playedPercentage
			if (playedPercentage != null && playedPercentage > 0) {
				val progressHeight by animateFloatAsState(
					targetValue = if (isFocused) 5f else 3f,
					animationSpec = tween(200, easing = EaseOutCubic),
					label = "progressHeight",
				)
				val trackAlpha by animateFloatAsState(
					targetValue = if (isFocused) 0.40f else 0.25f,
					animationSpec = tween(200, easing = EaseOutCubic),
					label = "trackAlpha",
				)
				val glowAlpha by animateFloatAsState(
					targetValue = if (isFocused) 1f else 0f,
					animationSpec = tween(200, easing = EaseOutCubic),
					label = "progressGlow",
				)
				val progressShape = RoundedCornerShape(2.dp)
				val fraction = (playedPercentage / 100.0).toFloat()

				Box(
					modifier =
						Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
							.height(progressHeight.dp)
							.background(
								Color.White.copy(alpha = trackAlpha),
								progressShape,
							),
				) {
					Box(
						modifier =
							Modifier
								.fillMaxHeight()
								.fillMaxWidth(fraction = fraction)
								.drawBehind {
									if (glowAlpha > 0f) {
										val cr = CornerRadius(2.dp.toPx())
										listOf(
											4f to 0.10f,
											2.5f to 0.15f,
											1f to 0.25f,
										).forEach { (spreadDp, alpha) ->
											val spread = spreadDp.dp.toPx()
											drawRoundRect(
												color =
													VegafoXColors.BlueAccent.copy(
														alpha = alpha * glowAlpha,
													),
												topLeft = Offset(-spread, -spread),
												size =
													Size(
														size.width + spread * 2,
														size.height + spread * 2,
													),
												cornerRadius = cr,
											)
										}
									}
								}.background(VegafoXColors.BlueAccent, progressShape),
					)
				}
			}

			// "NEW" badge for unwatched items
			val isNew =
				item.userData?.played == false &&
					(item.userData?.playCount == null || item.userData?.playCount == 0)
			if (isNew) {
				Box(
					modifier =
						Modifier
							.align(Alignment.TopEnd)
							.padding(4.dp)
							.background(
								color = VegafoXColors.OrangePrimary,
								shape = RoundedCornerShape(4.dp),
							).padding(horizontal = 7.dp, vertical = 2.dp),
				) {
					Text(
						text = stringResource(R.string.lbl_new),
						style =
							JellyfinTheme.typography.labelSmall.copy(
								fontSize = 10.sp,
								fontWeight = FontWeight.Bold,
							),
						color = VegafoXColors.Background,
					)
				}
			}

			// Play overlay — appears centered after 5s of stable focus
			AnimatedVisibility(
				visible = showOverlay,
				enter = fadeIn(tween(300, easing = EaseOutCubic)),
				exit = fadeOut(tween(200, easing = EaseInCubic)),
				modifier = Modifier.align(Alignment.Center),
			) {
				Row(
					modifier =
						Modifier
							.background(
								VegafoXColors.Background.copy(alpha = 0.65f),
								RoundedCornerShape(20.dp),
							).padding(horizontal = 14.dp, vertical = 6.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(6.dp),
				) {
					Text(
						text = "\u25B6",
						fontSize = 12.sp,
						color = VegafoXColors.TextPrimary,
					)
					Text(
						text =
							if (hasProgress) {
								stringResource(R.string.lbl_resume)
							} else {
								stringResource(R.string.lbl_play)
							},
						fontSize = 13.sp,
						fontWeight = FontWeight.SemiBold,
						color = VegafoXColors.TextPrimary,
					)
				}
			}
		}
	}
}
