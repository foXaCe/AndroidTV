package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
private const val PILL_SHOW_DELAY_MS = 1200L

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * A reusable media item card for browse screens.
 * Uses [TvFocusCard] for D-pad focus handling and displays a 16:9 landscape image.
 * Includes a play pill below the card that appears on focus (D-pad navigable).
 *
 * @param onPlayClick Direct playback action (pill OK). Falls back to [onClick] if null.
 * @param initialFocusRequester Optional requester for initial focus on startup.
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
	cardWidth: Dp = CardDimensions.landscapeWidth,
	initialFocusRequester: FocusRequester? = null,
) {
	val cardImageHeight = cardWidth * (9f / 16f)
	var isGroupFocused by remember { mutableStateOf(false) }
	var showPill by remember { mutableStateOf(false) }
	var isPillFocused by remember { mutableStateOf(false) }
	val hasProgress = (item.userData?.playedPercentage ?: 0.0) > 0
	val cardFocusRequester = remember { FocusRequester() }
	val pillFocusRequester = remember { FocusRequester() }

	// Debounce: show pill only after dwelling on the card for 600ms
	LaunchedEffect(isGroupFocused) {
		if (isGroupFocused) {
			delay(PILL_SHOW_DELAY_MS)
			showPill = true
		} else {
			showPill = false
		}
	}

	Column(
		modifier =
			modifier
				.width(cardWidth)
				.onFocusChanged { isGroupFocused = it.hasFocus },
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		TvFocusCard(
			onClick = onClick,
			modifier =
				Modifier
					.focusRequester(cardFocusRequester)
					.focusProperties { down = pillFocusRequester }
					.then(
						if (initialFocusRequester != null) {
							Modifier.focusRequester(initialFocusRequester)
						} else {
							Modifier
						},
					).then(
						if (onFocus != null || onBlur != null) {
							Modifier.onFocusChanged { state ->
								if (state.isFocused) {
									onFocus?.invoke()
								} else {
									onBlur?.invoke()
								}
							}
						} else {
							Modifier
						},
					),
			focusColor =
				if (isPillFocused) {
					VegafoXColors.OrangePrimary.copy(alpha = 0.3f)
				} else {
					JellyfinTheme.colorScheme.focusRing
				},
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

				// Progress bar for partially watched items (padded above focus border)
				val playedPercentage = item.userData?.playedPercentage
				if (playedPercentage != null && playedPercentage > 0) {
					Box(
						modifier =
							Modifier
								.align(Alignment.BottomCenter)
								.fillMaxWidth()
								.padding(bottom = 3.dp)
								.height(2.dp)
								.background(Color(0x1AFFFFFF)),
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
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		// Pill below card — always reserves 32dp height for consistent row sizing
		PlayPillZone(
			visible = showPill,
			hasProgress = hasProgress,
			onClick = { onPlayClick?.invoke() ?: onClick() },
			onPillFocusChanged = { isPillFocused = it },
			pillFocusRequester = pillFocusRequester,
			cardFocusRequester = cardFocusRequester,
		)
	}
}

/**
 * Play pill zone — extracted to avoid ColumnScope.AnimatedVisibility resolution conflict.
 * Always reserves 32dp height to prevent layout shift between focused/unfocused states.
 */
@Composable
private fun PlayPillZone(
	visible: Boolean,
	hasProgress: Boolean,
	onClick: () -> Unit,
	onPillFocusChanged: (Boolean) -> Unit,
	pillFocusRequester: FocusRequester,
	cardFocusRequester: FocusRequester,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier =
			modifier
				.fillMaxWidth()
				.height(32.dp),
		contentAlignment = Alignment.Center,
	) {
		AnimatedVisibility(
			visible = visible,
			enter =
				fadeIn(tween(200, easing = EaseOutCubic)) +
					slideInVertically(
						animationSpec = tween(200, easing = EaseOutCubic),
						initialOffsetY = { it / 2 },
					),
			exit =
				fadeOut(tween(150, easing = EaseInCubic)) +
					slideOutVertically(
						animationSpec = tween(150, easing = EaseInCubic),
						targetOffsetY = { it / 2 },
					),
		) {
			TvFocusCard(
				onClick = onClick,
				focusedScale = 1f,
				focusColor = Color.White,
				shape = RoundedCornerShape(20.dp),
				modifier =
					Modifier
						.focusRequester(pillFocusRequester)
						.focusProperties { up = cardFocusRequester }
						.onFocusChanged { onPillFocusChanged(it.isFocused) },
			) {
				Row(
					modifier =
						Modifier
							.background(VegafoXColors.OrangePrimary, RoundedCornerShape(20.dp))
							.padding(horizontal = 14.dp, vertical = 4.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(6.dp),
				) {
					Text(
						text = "\u25B6",
						fontSize = 11.sp,
						color = Color.White,
					)
					Text(
						text =
							if (hasProgress) {
								stringResource(R.string.lbl_resume)
							} else {
								stringResource(R.string.lbl_play)
							},
						fontSize = 13.sp,
						fontWeight = FontWeight.Medium,
						color = Color.White,
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
		return thumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	val backdrop = item.itemBackdropImages.firstOrNull()
	if (backdrop != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=BACKDROP item=${item.name} type=${item.type}")
		return backdrop.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	// Before falling to PRIMARY (possibly portrait), try parent landscape images
	val parentThumb = item.parentImages[ImageType.THUMB]
	if (parentThumb != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=PARENT_THUMB item=${item.name} type=${item.type}")
		return parentThumb.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	val parentBackdrop = item.parentBackdropImages.firstOrNull()
	if (parentBackdrop != null) {
		Timber.tag("VFX_IMG").d("VFX_IMG image_type=PARENT_BACKDROP item=${item.name} type=${item.type}")
		return parentBackdrop.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	// Last resort: PRIMARY (may be portrait poster — will be cropped in 16:9 card)
	val primary = item.itemImages[ImageType.PRIMARY]
	if (primary != null) {
		val aspect = item.primaryImageAspectRatio
		Timber.tag("VFX_IMG").w("VFX_IMG image_type=PRIMARY(portrait?) item=${item.name} type=${item.type} aspect=$aspect")
		return primary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	val parentPrimary = item.parentImages[ImageType.PRIMARY]
	if (parentPrimary != null) {
		Timber.tag("VFX_IMG").w("VFX_IMG image_type=PARENT_PRIMARY item=${item.name} type=${item.type}")
		return parentPrimary.getUrl(api, fillWidth = CARD_IMAGE_WIDTH_PX, quality = 90)
	}

	return null
}
