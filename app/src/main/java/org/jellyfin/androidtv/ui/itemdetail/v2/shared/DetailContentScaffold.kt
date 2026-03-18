package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaGenreTag
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaPosterColumn
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.SectionHeader
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Shared scaffold for all detail content types.
 * Provides: hero zone (poster left + info right), gradient transition,
 * optional metadata section, custom sections, bottom padding.
 */
@Composable
fun DetailContentScaffold(
	item: BaseItemDto,
	uiState: ItemDetailsUiState,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	genreTag: String?,
	metadataContent: @Composable () -> Unit = {},
	synopsisText: String? = null,
	metadataSection: (@Composable () -> Unit)? = null,
	sections: LazyListScope.(lazyListState: LazyListState, heroFocus: FocusRequester) -> Unit = { _, _ -> },
	playButtonFocusRequester: FocusRequester,
) {
	val listState = rememberLazyListState()
	val density = LocalDensity.current
	val context = LocalContext.current

	val posterUrl = remember(item.id) { getPosterUrl(item, api) }
	val logoUrl = remember(item.id) { getLogoUrl(item, api) }
	val playedPercentage = item.userData?.playedPercentage ?: 0.0
	val watchedMinutes = ((item.userData?.playbackPositionTicks ?: 0L) / 10_000_000 / 60).toInt()

	// Enter animation — always scroll to top and focus play button (Netflix pattern)
	var showContent by remember { mutableStateOf(false) }
	LaunchedEffect(item.id) {
		kotlinx.coroutines.delay(100)
		showContent = true
		listState.scrollToItem(0)
		kotlinx.coroutines.delay(150)
		try {
			playButtonFocusRequester.requestFocus()
		} catch (_: Exception) {
		}
	}

	val slideOffsetPx = with(density) { 20.dp.roundToPx() }

	// Coroutine scope for smooth scroll
	val scrollScope = androidx.compose.runtime.rememberCoroutineScope()

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize(),
		) {
			// ═══ HERO ZONE ═══
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(HeroDimensions.heroHeight)
							.disableBringIntoView()
							.onPreviewKeyEvent { event ->
								if (event.key == Key.DirectionDown &&
									event.type == KeyEventType.KeyDown &&
									listState.firstVisibleItemIndex == 0
								) {
									scrollScope.launch {
										listState.animateScrollToItem(
											index = 2,
											scrollOffset = 0,
										)
									}
									false // let focus move naturally after animation starts
								} else {
									false
								}
							},
					contentAlignment = Alignment.BottomStart,
				) {
					AnimatedVisibility(
						visible = showContent,
						enter =
							fadeIn(
								animationSpec = tween(350, easing = EaseOutCubic),
							) +
								slideInVertically(
									initialOffsetY = { slideOffsetPx },
									animationSpec = tween(350, easing = EaseOutCubic),
								),
					) {
						Row(
							modifier =
								Modifier
									.fillMaxWidth()
									.padding(horizontal = HeroDimensions.horizontalPadding)
									.padding(bottom = 16.dp),
							verticalAlignment = Alignment.Bottom,
							horizontalArrangement = Arrangement.spacedBy(HeroDimensions.columnGap),
						) {
							// ─── Left column: Poster ───
							CinemaPosterColumn(
								posterUrl = posterUrl,
								playedPercentage = playedPercentage,
								watchedMinutes = watchedMinutes,
								modifier = Modifier.padding(top = HeroDimensions.posterTopPadding),
								item = item,
							)

							// ─── Right column: Info ───
							Column(
								modifier =
									Modifier
										.weight(1f)
										.clipToBounds(),
							) {
								// Genre tag
								if (!genreTag.isNullOrBlank()) {
									CinemaGenreTag(text = genreTag)
									Spacer(modifier = Modifier.height(12.dp))
								}

								// Logo or Title
								DetailTitleOrLogo(
									title = item.name ?: "",
									logoUrl = logoUrl,
									context = context,
								)

								Spacer(modifier = Modifier.height(16.dp))

								// Metadata badges (year, duration, rating, etc.)
								metadataContent()

								// Synopsis
								if (!synopsisText.isNullOrBlank()) {
									Spacer(modifier = Modifier.height(16.dp))
									Text(
										text = synopsisText,
										style =
											JellyfinTheme.typography.bodyMedium.copy(
												fontSize = 15.sp,
											),
										color = VegafoXColors.TextPrimary,
										maxLines = 4,
										overflow = TextOverflow.Ellipsis,
										lineHeight = 26.sp,
									)
								}

								Spacer(modifier = Modifier.height(16.dp))

								// Action buttons (primary + secondary)
								DetailActionButtonsRow(
									item = item,
									uiState = uiState,
									playButtonFocusRequester = playButtonFocusRequester,
									callbacks = actionCallbacks,
								)
							}
						}
					}
				}
			}

			// ═══ GRADIENT TRANSITION ═══
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(DetailDimensions.gradientHeight)
							.background(
								Brush.verticalGradient(
									colors = listOf(Color.Transparent, Color.Transparent),
								),
							),
				)
			}

			// ═══ METADATA (director, writer, studio) ═══
			if (metadataSection != null) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(Color.Transparent)
								.padding(horizontal = HeroDimensions.horizontalPadding)
								.padding(top = 8.dp, bottom = 16.dp),
					) {
						metadataSection()
					}
				}
			}

			// ═══ CUSTOM SECTIONS ═══
			sections(listState, playButtonFocusRequester)

			// ═══ BOTTOM PADDING ═══
			item {
				Spacer(
					modifier =
						Modifier
							.height(DetailDimensions.bottomPadding)
							.fillMaxWidth()
							.background(Color.Transparent),
				)
			}
		}
	}
}

// ─── Title or Logo ───

@Composable
private fun DetailTitleOrLogo(
	title: String,
	logoUrl: String?,
	context: android.content.Context,
) {
	if (logoUrl != null) {
		val imageRequest =
			remember(logoUrl) {
				ImageRequest
					.Builder(context)
					.data(logoUrl)
					.memoryCachePolicy(CachePolicy.ENABLED)
					.crossfade(true)
					.build()
			}
		AsyncImage(
			model = imageRequest,
			contentDescription = title,
			modifier =
				Modifier
					.widthIn(max = 280.dp)
					.heightIn(max = 80.dp),
			contentScale = ContentScale.Fit,
			placeholder = null,
			error = null,
		)
	} else {
		Text(
			text = title,
			style =
				JellyfinTheme.typography.displayBold.copy(
					fontSize = HeroDimensions.titleFontSize,
					fontWeight = FontWeight.Black,
					fontFamily = BebasNeue,
					letterSpacing = 2.sp,
				),
			color = VegafoXColors.TextPrimary,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			lineHeight = HeroDimensions.titleLineHeight,
		)
	}
}

// ─── LazyListScope helpers for sections ───

fun LazyListScope.detailEpisodesHeader(
	seasonNumber: Int?,
	episodeIndex: Int?,
	totalEpisodes: Int,
	topPadding: Dp = 24.dp,
) {
	item {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.background(Color.Transparent)
					.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
					.padding(top = topPadding, bottom = 8.dp),
		) {
			DetailEpisodesHeader(
				seasonNumber = seasonNumber,
				episodeIndex = episodeIndex,
				totalEpisodes = totalEpisodes,
			)
		}
	}
}

fun LazyListScope.detailSectionHeader(
	title: String,
	topPadding: Dp = 24.dp,
) {
	item {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.background(Color.Transparent)
					.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
					.padding(top = topPadding, bottom = 8.dp),
		) {
			SectionHeader(title = title)
		}
	}
}

fun LazyListScope.detailSectionRow(
	height: Dp,
	lazyListState: LazyListState? = null,
	heroFocusRequester: FocusRequester? = null,
	content: @Composable () -> Unit,
) {
	item {
		val scope = androidx.compose.runtime.rememberCoroutineScope()
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(height)
					.background(Color.Transparent)
					.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
					.then(
						if (lazyListState != null && heroFocusRequester != null) {
							Modifier.onPreviewKeyEvent { event ->
								if (event.key == Key.DirectionUp &&
									event.type == KeyEventType.KeyDown
								) {
									scope.launch {
										// Estimate scroll distance from visible items
										val idx = lazyListState.firstVisibleItemIndex
										val offset = lazyListState.firstVisibleItemScrollOffset
										val distance = (idx * 300 + offset).toFloat().coerceAtLeast(100f)
										// Smooth tween scroll
										lazyListState.animateScrollBy(
											value = -distance,
											animationSpec = tween(durationMillis = 500),
										)
										// Snap to exact top if estimate was short
										if (lazyListState.firstVisibleItemIndex > 0) {
											lazyListState.scrollToItem(0)
										}
										try {
											heroFocusRequester.requestFocus()
										} catch (_: Exception) {
										}
									}
									true
								} else {
									false
								}
							}
						} else {
							Modifier
						},
					),
		) {
			content()
		}
	}
}
