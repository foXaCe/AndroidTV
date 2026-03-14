package org.jellyfin.androidtv.ui.home.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonLandscapeCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.base.tv.TvRowList
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.browsing.compose.getItemImageUrl
import org.jellyfin.androidtv.ui.home.compose.sidebar.PremiumSideBar
import org.jellyfin.design.Tokens
import org.jellyfin.sdk.model.api.BaseItemDto
import kotlin.random.Random

/** Pixel size used for prefetching card images (matches BrowseMediaCard). */
private const val PREFETCH_IMAGE_WIDTH = 440
private const val PREFETCH_IMAGE_HEIGHT = 248

/**
 * Home screen — the root browsing experience (Netflix-style).
 *
 * Architecture:
 * ```
 * Row (fullscreen) {
 *     PremiumSideBar()               // fixed 72dp, no overlay
 *     Box (weight=1f) {
 *         DarkGridNoiseBackground()  // dark grid noise (trading terminal style)
 *         HomeHeroBackdrop(item)     // crossfade backdrop + dynamic gradient
 *         Column {
 *             Box(weight=0.56) {     // hero info overlay area
 *                 HeroInfoOverlay()  // animated metadata (no buttons)
 *             }
 *             TvRowList(rows)        // vertical list of horizontal rows
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun HomeScreen(
	viewModel: HomeViewModel,
	onItemClick: (BaseItemDto) -> Unit,
	onPlayClick: (BaseItemDto) -> Unit = onItemClick,
) {
	val uiState by viewModel.uiState.collectAsState()
	val focusedItem by viewModel.focusedItem.collectAsState()
	val trailerState by viewModel.trailerState.collectAsState()

	// Initial focus: target "Continue watching" row first, else first row
	val initialFocusRequester = remember { FocusRequester() }
	var hasRequestedInitialFocus by rememberSaveable { mutableStateOf(false) }
	val targetRow =
		uiState.rows.firstOrNull { it.key == "resume" || it.key == "continue_watching" }
			?: uiState.rows.firstOrNull()
	val firstItemId = targetRow?.items?.firstOrNull()?.id

	// Request focus on first card after composition
	LaunchedEffect(firstItemId) {
		if (firstItemId != null && !hasRequestedInitialFocus) {
			delay(400)
			try {
				initialFocusRequester.requestFocus()
				hasRequestedInitialFocus = true
			} catch (_: Exception) {
				// FocusRequester not yet attached
			}
		}
	}

	Row(modifier = Modifier.fillMaxSize()) {
		// Left: Premium sidebar (fixed 72dp, no overlay)
		PremiumSideBar(modifier = Modifier.fillMaxHeight())

		// Right: Main content fills remaining space
		Box(
			modifier =
				Modifier
					.weight(1f)
					.fillMaxHeight(),
		) {
			// Layer 0: Dark grid noise background (trading terminal style)
			DarkGridNoiseBackground()

			// Layer 1: Hero backdrop — full-screen image + trailer video of the focused item
			HomeHeroBackdrop(
				item = focusedItem,
				api = viewModel.api,
				trailerState = trailerState,
				onTrailerEnded = { viewModel.stopTrailer() },
			)

			// Layer 2: Content
			Column(modifier = Modifier.fillMaxSize()) {
				// Hero info area (takes flexible space above rows)
				Box(
					modifier =
						Modifier
							.weight(0.46f)
							.fillMaxWidth(),
				) {
					// Info overlay — center-left
					HeroInfoOverlay(
						item = focusedItem,
						modifier =
							Modifier
								.align(Alignment.CenterStart)
								.padding(start = 16.dp),
					)

					// Mute badge — visible only when trailer is playing
					if (trailerState.isPlaying) {
						MuteBadge(
							isMuted = trailerState.isMuted,
							onToggle = { viewModel.toggleMute() },
							modifier =
								Modifier
									.align(Alignment.TopEnd)
									.padding(12.dp),
						)
					}
				}

				// Rows content with state handling
				val displayState =
					when {
						uiState.isLoading -> DisplayState.LOADING
						uiState.error != null -> DisplayState.ERROR
						uiState.rows.isEmpty() -> DisplayState.EMPTY
						else -> DisplayState.CONTENT
					}

				StateContainer(
					state = displayState,
					modifier = Modifier.weight(0.54f).clipToBounds(),
					loadingContent = {
						// Matches TvRowList contentPadding: start=12dp, top=spaceSm
						Column(modifier = Modifier.padding(start = 12.dp, top = Tokens.Space.spaceSm)) {
							repeat(3) {
								SkeletonLandscapeCardRow(showCardTextLines = false)
							}
						}
					},
					emptyContent = {
						EmptyState(title = stringResource(R.string.lbl_empty))
					},
					errorContent = {
						ErrorState(
							message =
								stringResource(
									uiState.error?.messageRes ?: R.string.state_error_generic,
								),
							onRetry = { viewModel.loadRows() },
						)
					},
					content = {
						val context = LocalContext.current
						val imageLoader = remember { SingletonImageLoader.get(context) }
						val api = viewModel.api
						val prefetchCallback =
							remember<(List<BaseItemDto>) -> Unit>(api) {
								{ items ->
									for (nextItem in items.take(5)) {
										val url = getItemImageUrl(nextItem, api, 220) ?: continue
										val request =
											ImageRequest
												.Builder(context)
												.data(url)
												.size(coil3.size.Size(PREFETCH_IMAGE_WIDTH, PREFETCH_IMAGE_HEIGHT))
												.scale(Scale.FILL)
												.memoryCachePolicy(CachePolicy.ENABLED)
												.diskCachePolicy(CachePolicy.ENABLED)
												.crossfade(false)
												.build()
										imageLoader.enqueue(request)
									}
								}
							}

						TvRowList(
							rows = uiState.rows,
							staggerEntrance = true,
							contentPadding =
								PaddingValues(
									start = 12.dp,
									top = Tokens.Space.spaceSm,
									bottom = Tokens.Space.spaceLg,
								),
							prefetchContent = prefetchCallback,
						) { item ->
							BrowseMediaCard(
								item = item,
								api = viewModel.api,
								onFocus = { viewModel.setFocusedItem(item) },
								onBlur = { viewModel.stopTrailer() },
								onClick = { onItemClick(item) },
								onPlayClick = { onPlayClick(item) },
								initialFocusRequester =
									if (item.id == firstItemId && !hasRequestedInitialFocus) {
										initialFocusRequester
									} else {
										null
									},
							)
						}
					},
				)
			}
		}
	}
}

/**
 * Mute badge — small focusable badge to toggle trailer audio.
 * Visible only when a trailer is playing in the hero backdrop.
 */
@Composable
private fun MuteBadge(
	isMuted: Boolean,
	onToggle: () -> Unit,
	modifier: Modifier = Modifier,
) {
	TvFocusCard(
		onClick = onToggle,
		focusedScale = 1f,
		focusColor = VegafoXColors.OrangePrimary,
		shape = RoundedCornerShape(20.dp),
		modifier = modifier,
	) {
		Box(
			modifier =
				Modifier
					.background(
						Color.Black.copy(alpha = 0.45f),
						shape = RoundedCornerShape(20.dp),
					).border(
						1.dp,
						Color.White.copy(alpha = 0.12f),
						RoundedCornerShape(20.dp),
					).padding(horizontal = 10.dp, vertical = 4.dp),
		) {
			Text(
				text = if (isMuted) "\uD83D\uDD07 sourdine" else "\uD83D\uDD0A son",
				fontSize = 10.sp,
				fontWeight = FontWeight.Medium,
				color = Color.White.copy(alpha = 0.55f),
			)
		}
	}
}

// -- Dark Grid Noise background constants --
private val GridBaseColor = Color(0xFF060A0F)
private val RadialCenterColor = Color(0xFF0A1525)
private const val NOISE_SEED = 42
private const val NOISE_POINT_COUNT = 800

/**
 * Dark Grid Noise background — trading/terminal dashboard style.
 * Layers: solid base, fractal noise, geometric grid, radial glow, vignette.
 * Does NOT depend on any state — drawn once, never recomposes.
 */
@Composable
private fun DarkGridNoiseBackground(modifier: Modifier = Modifier) {
	val density = LocalDensity.current
	val gridStepPx = with(density) { 60.dp.toPx() }
	val dotSizePx = with(density) { 1.5.dp.toPx() }
	val lineWidthPx = with(density) { 0.5.dp.toPx() }
	val gridColor = VegafoXColors.OrangePrimary.copy(alpha = 0.03f)

	Canvas(modifier = modifier.fillMaxSize()) {
		val w = size.width
		val h = size.height
		val cx = w / 2f
		val cy = h / 2f

		// Layer 1: Solid base #060A0F
		drawRect(color = GridBaseColor)

		// Layer 2: Fractal noise — 800 deterministic random dots
		val rng = Random(NOISE_SEED)
		repeat(NOISE_POINT_COUNT) {
			val x = rng.nextFloat() * w
			val y = rng.nextFloat() * h
			val a = rng.nextFloat() * 0.025f
			drawRect(
				color = Color.White.copy(alpha = a),
				topLeft = Offset(x, y),
				size = Size(dotSizePx, dotSizePx),
			)
		}

		// Layer 3: Geometric grid — OrangePrimary lines every 60dp
		var gx = 0f
		while (gx <= w) {
			drawLine(color = gridColor, start = Offset(gx, 0f), end = Offset(gx, h), strokeWidth = lineWidthPx)
			gx += gridStepPx
		}
		var gy = 0f
		while (gy <= h) {
			drawLine(color = gridColor, start = Offset(0f, gy), end = Offset(w, gy), strokeWidth = lineWidthPx)
			gy += gridStepPx
		}

		// Layer 4: Elliptical radial gradient — center glow (70% width × 50% height)
		val radiusX = w * 0.35f
		val scaleY = (h * 0.25f) / radiusX
		drawContext.canvas.nativeCanvas.save()
		drawContext.canvas.nativeCanvas.scale(1f, scaleY, cx, cy)
		drawRect(
			brush =
				Brush.radialGradient(
					colors = listOf(RadialCenterColor, Color.Transparent),
					center = Offset(cx, cy),
					radius = radiusX,
				),
			alpha = 0.6f,
		)
		drawContext.canvas.nativeCanvas.restore()

		// Layer 5: Vignette — dark fading edges
		val vw = w * 0.15f
		val vh = h * 0.15f
		val vAlpha = 0.7f
		// Left — reduced (sidebar already provides left edge)
		val vwLeft = w * 0.05f
		drawRect(
			brush =
				Brush.horizontalGradient(
					colors = listOf(Color.Black.copy(alpha = vAlpha * 0.5f), Color.Transparent),
					startX = 0f,
					endX = vwLeft,
				),
			size = Size(vwLeft, h),
		)
		// Right
		drawRect(
			brush =
				Brush.horizontalGradient(
					colors = listOf(Color.Transparent, Color.Black.copy(alpha = vAlpha)),
					startX = w - vw,
					endX = w,
				),
			topLeft = Offset(w - vw, 0f),
			size = Size(vw, h),
		)
		// Top
		drawRect(
			brush =
				Brush.verticalGradient(
					colors = listOf(Color.Black.copy(alpha = vAlpha), Color.Transparent),
					startY = 0f,
					endY = vh,
				),
			size = Size(w, vh),
		)
		// Bottom
		drawRect(
			brush =
				Brush.verticalGradient(
					colors = listOf(Color.Transparent, Color.Black.copy(alpha = vAlpha)),
					startY = h - vh,
					endY = h,
				),
			topLeft = Offset(0f, h - vh),
			size = Size(w, vh),
		)
	}
}
