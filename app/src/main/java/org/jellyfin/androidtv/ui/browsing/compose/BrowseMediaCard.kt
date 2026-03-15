package org.jellyfin.androidtv.ui.browsing.compose

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemBackdropImages
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentBackdropImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
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
					.clip(RoundedCornerShape(10.dp))
					.background(JellyfinTheme.colorScheme.surfaceDim),
		) {
			val imageUrl = getItemImageUrl(item, api, cardWidth.value.toInt())
			if (imageUrl != null) {
				val composeTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
				val context = LocalContext.current
				val imageRequest =
					ImageRequest
						.Builder(context)
						.data(imageUrl)
						.size(Size(CARD_IMAGE_WIDTH_PX, CARD_IMAGE_HEIGHT_PX))
						.scale(Scale.FILL)
						.memoryCachePolicy(CachePolicy.ENABLED)
						.diskCachePolicy(CachePolicy.ENABLED)
						.crossfade(false)
						.build()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageRequest,
					contentDescription = item.name,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					error = errorFallback,
					onLoading = {
						Timber.tag("VFX_IMG").d("VFX_IMG image_load_start: ${item.name}")
					},
					onSuccess = {
						val delta = System.currentTimeMillis() - composeTime
						Timber.tag("VFX_IMG").d("VFX_IMG image_load_done: ${delta}ms ${item.name}")
					},
					onError = {
						Timber.tag("VFX_IMG").w("VFX_IMG image_load_error: ${item.name} ${it.result.throwable}")
					},
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

			// Progress bar for partially watched items
			val playedPercentage = item.userData?.playedPercentage
			if (playedPercentage != null && playedPercentage > 0) {
				Box(
					modifier =
						Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.padding(bottom = 3.dp)
							.height(2.dp)
							.background(VegafoXColors.Divider),
				) {
					Box(
						modifier =
							Modifier
								.fillMaxHeight()
								.fillMaxWidth(fraction = (playedPercentage / 100.0).toFloat())
								.background(VegafoXColors.BlueAccent),
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

internal fun getItemImageUrl(
	item: BaseItemDto,
	api: ApiClient,
	maxWidth: Int,
): String? {
	// Priority: THUMB → BACKDROP → PRIMARY (item), then parent fallbacks.
	// THUMB = optimized 16:9 thumbnail, sharper and better framed for grids.
	// BACKDROP = native 16:9, avoids destructive crop from portrait PRIMARY.

	val thumb = item.itemImages[ImageType.THUMB]
	if (thumb != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=THUMB item=${item.name} type=${item.type}")
		return thumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	val backdrop = item.itemBackdropImages.firstOrNull()
	if (backdrop != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=BACKDROP item=${item.name} type=${item.type}")
		return backdrop.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	// Before falling to PRIMARY (possibly portrait), try parent landscape images
	val parentThumb = item.parentImages[ImageType.THUMB]
	if (parentThumb != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=PARENT_THUMB item=${item.name} type=${item.type}")
		return parentThumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	val parentBackdrop = item.parentBackdropImages.firstOrNull()
	if (parentBackdrop != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=PARENT_BACKDROP item=${item.name} type=${item.type}")
		return parentBackdrop.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	// Last resort: PRIMARY (may be portrait poster — will be cropped in 16:9 card)
	val primary = item.itemImages[ImageType.PRIMARY]
	if (primary != null) {
		val aspect = item.primaryImageAspectRatio
		Timber.tag("VFX_IMG").w("VFX_IMG image_type=PRIMARY(portrait?) item=${item.name} type=${item.type} aspect=$aspect")
		return primary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	val parentPrimary = item.parentImages[ImageType.PRIMARY]
	if (parentPrimary != null) {
		Timber.tag("VFX_IMG").w("VFX_IMG image_type=PARENT_PRIMARY item=${item.name} type=${item.type}")
		return parentPrimary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 96)
	}

	return null
}
