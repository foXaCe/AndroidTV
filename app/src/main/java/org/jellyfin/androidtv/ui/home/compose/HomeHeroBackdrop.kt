package org.jellyfin.androidtv.ui.home.compose

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.home.mediabar.ExoPlayerTrailerView
import org.jellyfin.androidtv.ui.home.mediabar.SponsorBlockApi
import org.jellyfin.androidtv.ui.home.mediabar.YouTubeStreamResolver
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.translateGenreUpper
import org.jellyfin.androidtv.ui.shared.components.CachedAsyncImage
import org.jellyfin.androidtv.ui.shared.components.MediaMetadataBadges
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import timber.log.Timber

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

/**
 * Full-screen hero backdrop that changes with D-pad navigation.
 * Displays the backdrop image of the currently focused item with:
 * - Crossfade transition (400ms)
 * - Dynamic horizontal gradient (stronger when item is focused)
 * - Vertical gradient (transparent -> ds_background) at bottom
 * - Blur effect (API 31+, radius 8px)
 * - 40% opacity to avoid distracting from content
 * - YouTube trailer auto-play after 5s focus (with crossfade)
 */
@Composable
fun HomeHeroBackdrop(
	item: BaseItemDto?,
	api: ApiClient,
	trailerState: TrailerState = TrailerState(),
	onTrailerEnded: () -> Unit = {},
	onStopTrailer: () -> Unit = {},
	modifier: Modifier = Modifier,
) {
	val bgColor = VegafoXColors.Background

	// Derive the backdrop URL to avoid unnecessary recompositions
	val backdropUrl by remember(item?.id) {
		derivedStateOf {
			item?.let { getBackdropUrl(it, api) }
		}
	}

	// Dynamic horizontal gradient alpha — stronger when an item is focused
	val hasItem = item != null
	val horizontalAlpha by animateFloatAsState(
		targetValue = if (hasItem) 0.75f else 0.4f,
		animationSpec = tween(durationMillis = CROSSFADE_DURATION_MS, easing = EaseOutCubic),
		label = "hero_gradient_alpha",
	)

	// -- Trailer state --
	Timber.d(
		"TRAILER_DBG: HomeHeroBackdrop recompose → isPlaying=${trailerState.isPlaying}, isCountingDown=${trailerState.isCountingDown}, streamInfo=${trailerState.streamInfo != null}",
	)
	val shouldPlayTrailer = trailerState.isPlaying && trailerState.streamInfo != null
	var videoReady by remember { mutableStateOf(false) }

	// Keep ExoPlayerTrailerView in composition during fade-out
	var showTrailerView by remember { mutableStateOf(false) }
	var cachedStreamInfo by remember { mutableStateOf<YouTubeStreamResolver.StreamInfo?>(null) }
	var cachedStartSeconds by remember { mutableStateOf(0.0) }
	var cachedSegments by remember { mutableStateOf<List<SponsorBlockApi.Segment>>(emptyList()) }

	// Stop trailer immediately when lifecycle pauses/stops (prevents audio leak)
	val lifecycleOwner = LocalLifecycleOwner.current
	DisposableEffect(lifecycleOwner) {
		val observer =
			LifecycleEventObserver { _, event ->
				if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
					onStopTrailer()
				}
			}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}

	LaunchedEffect(shouldPlayTrailer) {
		if (shouldPlayTrailer) {
			cachedStreamInfo = trailerState.streamInfo
			cachedStartSeconds = trailerState.startSeconds
			cachedSegments = trailerState.segments
			showTrailerView = true
		} else if (showTrailerView) {
			// Wait for fade-out animation before removing from composition
			delay(TRAILER_FADE_OUT_MS.toLong() + 50)
			showTrailerView = false
			videoReady = false
		}
	}

	// Timer bar countdown progress
	val timerProgress by animateFloatAsState(
		targetValue = if (trailerState.isCountingDown) 1f else 0f,
		animationSpec =
			if (trailerState.isCountingDown) {
				tween(durationMillis = TRAILER_COUNTDOWN_MS, easing = LinearEasing)
			} else {
				tween(durationMillis = 200)
			},
		label = "trailer_timer",
	)

	Box(modifier = modifier.fillMaxSize()) {
		// Layer 1: Crossfade between backdrop images
		Crossfade(
			targetState = backdropUrl,
			animationSpec = tween(durationMillis = CROSSFADE_DURATION_MS),
			label = "home_hero_backdrop",
		) { url ->
			if (url != null) {
				CachedAsyncImage(
					model = url,
					contentDescription = null,
					modifier =
						Modifier
							.fillMaxSize()
							.graphicsLayer {
								alpha = BACKDROP_ALPHA
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
									renderEffect =
										RenderEffect
											.createBlurEffect(BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.CLAMP)
											.asComposeRenderEffect()
								}
							},
					maxWidth = 1920,
					placeholder = null,
					error = null,
				)
			}
		}

		// Layer 2: Trailer video player (top-end, faded into backdrop)
		if (showTrailerView && cachedStreamInfo != null) {
			val videoAlpha by animateFloatAsState(
				targetValue = if (shouldPlayTrailer && videoReady) 1f else 0f,
				animationSpec =
					if (shouldPlayTrailer && videoReady) {
						tween(durationMillis = TRAILER_FADE_IN_MS, easing = EaseInOutCubic)
					} else {
						tween(durationMillis = TRAILER_FADE_OUT_MS, easing = EaseInCubic)
					},
				label = "videoAlpha",
			)

			Box(
				modifier =
					Modifier
						.align(Alignment.TopEnd)
						.width(380.dp)
						.height(214.dp)
						.graphicsLayer {
							alpha = videoAlpha
							compositingStrategy = CompositingStrategy.Offscreen
						}.drawWithContent {
							drawContent()
							// Left edge — ease-in curve (slow start, fast finish)
							drawRect(
								brush =
									Brush.horizontalGradient(
										colorStops =
											arrayOf(
												0.00f to Color.Transparent,
												0.25f to Color(0x08000000),
												0.50f to Color(0x30000000),
												0.75f to Color(0x80000000),
												0.90f to Color(0xD0000000),
												1.00f to Color.Black,
											),
										startX = 0f,
										endX = size.width * 0.55f,
									),
								blendMode = BlendMode.DstIn,
							)
							// Top edge — ease-in curve
							drawRect(
								brush =
									Brush.verticalGradient(
										colorStops =
											arrayOf(
												0.00f to Color.Transparent,
												0.30f to Color(0x10000000),
												0.60f to Color(0x50000000),
												0.85f to Color(0xC0000000),
												1.00f to Color.Black,
											),
										startY = 0f,
										endY = size.height * 0.4f,
									),
								blendMode = BlendMode.DstIn,
							)
							// Bottom edge — ease-out curve (inverted)
							drawRect(
								brush =
									Brush.verticalGradient(
										colorStops =
											arrayOf(
												0.00f to Color.Black,
												0.15f to Color(0xC0000000),
												0.40f to Color(0x50000000),
												0.70f to Color(0x10000000),
												1.00f to Color.Transparent,
											),
										startY = size.height * 0.55f,
										endY = size.height,
									),
								blendMode = BlendMode.DstIn,
							)
							// Right edge — ease-out curve
							drawRect(
								brush =
									Brush.horizontalGradient(
										colorStops =
											arrayOf(
												0.00f to Color.Black,
												0.20f to Color(0xC0000000),
												0.50f to Color(0x50000000),
												0.80f to Color(0x10000000),
												1.00f to Color.Transparent,
											),
										startX = size.width * 0.75f,
										endX = size.width,
									),
								blendMode = BlendMode.DstIn,
							)
						},
			) {
				ExoPlayerTrailerView(
					streamInfo = cachedStreamInfo!!,
					startSeconds = cachedStartSeconds,
					segments = cachedSegments,
					onVideoReady = { videoReady = true },
					onVideoEnded = onTrailerEnded,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}

		// Layer 3: Vertical gradient: transparent at top -> background at bottom
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(
						Brush.verticalGradient(
							colors =
								listOf(
									Color.Transparent,
									Color.Transparent,
									bgColor.copy(alpha = 0.4f),
									bgColor.copy(alpha = 0.8f),
									bgColor,
								),
						),
					),
		)

		// Layer 4: Horizontal gradient: background at left -> transparent at right (dynamic alpha)
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(
						Brush.horizontalGradient(
							colors =
								listOf(
									bgColor.copy(alpha = horizontalAlpha),
									bgColor.copy(alpha = horizontalAlpha * 0.5f),
									Color.Transparent,
								),
						),
					),
		)

		// Layer 5: Timer bar (visible during countdown, hidden during playback)
		if (trailerState.isCountingDown && timerProgress > 0f) {
			Box(
				modifier =
					Modifier
						.fillMaxWidth()
						.height(2.dp)
						.align(Alignment.BottomCenter),
			) {
				Box(
					modifier =
						Modifier
							.fillMaxHeight()
							.fillMaxWidth(timerProgress)
							.background(VegafoXColors.OrangePrimary.copy(alpha = 0.5f)),
				)
			}
		}
	}
}

/**
 * Animated info overlay for the hero area — shows metadata of the focused item.
 * Uses fadeIn/fadeOut crossfade animation (200ms/150ms).
 */
@Composable
fun HeroInfoOverlay(
	item: BaseItemDto?,
	modifier: Modifier = Modifier,
) {
	AnimatedContent(
		targetState = item,
		contentKey = { it?.id },
		transitionSpec = {
			fadeIn(
				animationSpec = tween(200),
				initialAlpha = 0f,
			) togetherWith
				fadeOut(
					animationSpec = tween(150),
				)
		},
		label = "hero_info",
		modifier = modifier,
	) { currentItem ->
		if (currentItem != null) {
			HeroInfoContent(
				item = currentItem,
			)
		} else {
			Spacer(Modifier)
		}
	}
}

@Composable
private fun HeroInfoContent(item: BaseItemDto) {
	Column(
		modifier = Modifier.widthIn(max = 600.dp),
	) {
		// Line 1: type (blue) + genres (orange)
		val context = androidx.compose.ui.platform.LocalContext.current
		val typeLabel = getHeroTypeLabel(item)
		val genreLabels = getHeroGenres(context, item)
		if (typeLabel != null || genreLabels.isNotEmpty()) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.widthIn(max = 600.dp),
			) {
				val parts = buildAnnotatedHeroLine1(typeLabel, genreLabels)
				Text(
					text = parts,
					fontSize = 11.sp,
					fontWeight = FontWeight.Bold,
					letterSpacing = 1.sp,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
			Spacer(Modifier.height(6.dp))
		}

		// Line 2: title only
		Text(
			text = getHeroTitle(item),
			fontSize = 32.sp,
			fontFamily = BebasNeue,
			fontWeight = FontWeight.Normal,
			color = VegafoXColors.TextPrimary,
			letterSpacing = 1.sp,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		// Line 3: season/episode info (if applicable)
		val heroSuffix = getHeroTitleSuffix(item)
		if (heroSuffix != null) {
			Spacer(Modifier.height(2.dp))
			Text(
				text = heroSuffix,
				fontSize = 20.sp,
				fontFamily = BebasNeue,
				fontWeight = FontWeight.Normal,
				color = VegafoXColors.TextSecondary,
				letterSpacing = 1.sp,
				maxLines = 1,
			)
		}

		Spacer(Modifier.height(8.dp))

		// Metadata badges: year, duration, rating, RT, quality
		MediaMetadataBadges(item = item)

		Spacer(Modifier.height(8.dp))

		// Description — 2 lines max, hidden if empty
		val overview = item.overview?.takeIf { it.isNotBlank() }
		if (overview != null) {
			Text(
				text = overview,
				fontSize = 14.sp,
				fontWeight = FontWeight.W300,
				color = VegafoXColors.TextSecondary,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				lineHeight = 20.sp,
				modifier = Modifier.defaultMinSize(minHeight = 44.dp),
			)
		}
	}
}

// -- Helpers --

private fun getHeroTitle(item: BaseItemDto): String =
	when (item.type) {
		BaseItemKind.EPISODE -> item.seriesName ?: item.name.orEmpty()
		else -> item.name.orEmpty()
	}

private fun getHeroTitleSuffix(item: BaseItemDto): String? =
	when (item.type) {
		BaseItemKind.EPISODE -> {
			val s = item.parentIndexNumber
			val e = item.indexNumber
			if (s != null || e != null) {
				val parts = mutableListOf<String>()
				if (s != null) parts.add("SAISON $s")
				if (e != null) parts.add("ÉPISODE $e")
				parts.joinToString("  \u00B7  ")
			} else {
				null
			}
		}
		else -> null
	}

private fun getHeroTypeLabel(item: BaseItemDto): String? =
	when (item.type) {
		BaseItemKind.MOVIE -> "FILM"
		BaseItemKind.SERIES -> "SÉRIE"
		BaseItemKind.EPISODE -> "SÉRIE"
		BaseItemKind.SEASON -> "SAISON"
		BaseItemKind.BOX_SET -> "COLLECTION"
		BaseItemKind.MUSIC_ALBUM -> "ALBUM"
		BaseItemKind.MUSIC_ARTIST -> "ARTISTE"
		BaseItemKind.AUDIO -> "MUSIQUE"
		BaseItemKind.TV_CHANNEL -> "TV EN DIRECT"
		BaseItemKind.PROGRAM -> "ÉMISSION"
		BaseItemKind.RECORDING -> "ENREGISTREMENT"
		BaseItemKind.TRAILER -> "BANDE-ANNONCE"
		BaseItemKind.MUSIC_VIDEO -> "CLIP"
		BaseItemKind.BOOK -> "LIVRE"
		BaseItemKind.PHOTO -> "PHOTO"
		BaseItemKind.PHOTO_ALBUM -> "ALBUM PHOTO"
		else -> null
	}

private fun getHeroGenres(
	context: Context,
	item: BaseItemDto,
): List<String> {
	val genres =
		item.genres?.takeIf { it.isNotEmpty() }
			?: item.genreItems?.mapNotNull { it.name }
			?: return emptyList()
	return genres.take(2).map { translateGenreUpper(context, it) }
}

private fun buildAnnotatedHeroLine1(
	typeLabel: String?,
	genres: List<String>,
): AnnotatedString =
	buildAnnotatedString {
		if (typeLabel != null) {
			withStyle(SpanStyle(color = VegafoXColors.BlueAccent)) {
				append(typeLabel)
			}
			if (genres.isNotEmpty()) {
				withStyle(SpanStyle(color = VegafoXColors.TextSecondary)) {
					append("  \u00B7  ")
				}
			}
		}
		genres.forEachIndexed { index, genre ->
			withStyle(SpanStyle(color = VegafoXColors.OrangePrimary)) {
				append(genre)
			}
			if (index < genres.lastIndex) {
				withStyle(SpanStyle(color = VegafoXColors.TextSecondary)) {
					append("  \u00B7  ")
				}
			}
		}
	}

private const val CROSSFADE_DURATION_MS = 400
private const val BACKDROP_ALPHA = 0.60f
private const val BLUR_RADIUS = 1f
private const val TRAILER_FADE_IN_MS = 600
private const val TRAILER_FADE_OUT_MS = 300
private const val TRAILER_COUNTDOWN_MS = 5_000
