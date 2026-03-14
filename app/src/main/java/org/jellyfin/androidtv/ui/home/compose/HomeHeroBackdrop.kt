package org.jellyfin.androidtv.ui.home.compose

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.home.mediabar.ExoPlayerTrailerView
import org.jellyfin.androidtv.ui.home.mediabar.SponsorBlockApi
import org.jellyfin.androidtv.ui.home.mediabar.YouTubeStreamResolver
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.formatDuration
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaStreamType
import timber.log.Timber

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

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
	val shouldPlayTrailer = trailerState.isPlaying && trailerState.streamInfo != null
	var videoReady by remember { mutableStateOf(false) }

	// Keep ExoPlayerTrailerView in composition during fade-out
	var showTrailerView by remember { mutableStateOf(false) }
	var cachedStreamInfo by remember { mutableStateOf<YouTubeStreamResolver.StreamInfo?>(null) }
	var cachedStartSeconds by remember { mutableStateOf(0.0) }
	var cachedSegments by remember { mutableStateOf<List<SponsorBlockApi.Segment>>(emptyList()) }

	LaunchedEffect(shouldPlayTrailer) {
		if (shouldPlayTrailer) {
			cachedStreamInfo = trailerState.streamInfo
			cachedStartSeconds = trailerState.startSeconds
			cachedSegments = trailerState.segments
			showTrailerView = true
		} else if (showTrailerView) {
			// Wait for fade-out animation before removing from composition
			delay(TRAILER_CROSSFADE_OUT_MS.toLong() + 50)
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
				AsyncImage(
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
					contentScale = ContentScale.Crop,
				)
			}
		}

		// Layer 2: Trailer video player (between backdrop image and gradients)
		if (showTrailerView && cachedStreamInfo != null) {
			ExoPlayerTrailerView(
				streamInfo = cachedStreamInfo!!,
				startSeconds = cachedStartSeconds,
				segments = cachedSegments,
				muted = trailerState.isMuted,
				isVisible = shouldPlayTrailer && videoReady,
				onVideoReady = { videoReady = true },
				onVideoEnded = onTrailerEnded,
				crossfadeInMs = TRAILER_CROSSFADE_IN_MS,
				crossfadeOutMs = TRAILER_CROSSFADE_OUT_MS,
				modifier = Modifier.fillMaxSize(),
			)
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
		// Genre / type tag line
		val tagLine = buildHeroTagLine(item)
		Timber.d(
			"HeroLine1 | kind=${item.type} | name=${item.name} | genre=${item.genres?.firstOrNull()} | seriesName=${item.seriesName} | season=${item.parentIndexNumber} | episode=${item.indexNumber} | line1computed=$tagLine",
		)
		if (tagLine.isNotEmpty()) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				Box(
					modifier =
						Modifier
							.width(20.dp)
							.height(2.dp)
							.background(VegafoXColors.OrangePrimary),
				)
				Text(
					text = tagLine,
					fontSize = 11.sp,
					fontWeight = FontWeight.SemiBold,
					color = VegafoXColors.OrangePrimary,
					letterSpacing = 1.sp,
				)
			}
			Spacer(Modifier.height(6.dp))
		}

		// Title — BebasNeue for cinematic look
		Text(
			text = item.name.orEmpty(),
			fontSize = 32.sp,
			fontWeight = FontWeight.Normal,
			fontFamily = BebasNeue,
			color = VegafoXColors.TextPrimary,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			letterSpacing = 1.sp,
		)

		Spacer(Modifier.height(8.dp))

		// Pills row: year, duration, rating, officialRating, resolution
		HeroPillsRow(item)

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

@Composable
private fun HeroPillsRow(item: BaseItemDto) {
	val pills =
		buildList {
			// Year
			item.productionYear?.let { add(PillData(it.toString())) }

			// Duration
			item.runTimeTicks?.let { ticks ->
				if (ticks > 0) add(PillData(formatDuration(ticks)))
			}

			// Community rating (star icon)
			item.communityRating?.let { rating ->
				if (rating > 0f) {
					val formatted =
						if (rating == rating.toLong().toFloat()) {
							rating.toLong().toString()
						} else {
							String.format("%.1f", rating).replace(",", ".")
						}
					add(PillData("\u2605 $formatted", highlight = true))
				}
			}

			// Resolution
			getResolutionLabel(item)?.let { add(PillData(it)) }
		}

	if (pills.isEmpty()) return

	Row(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		pills.forEach { pill ->
			HeroPill(text = pill.text, highlight = pill.highlight)
		}
	}
}

private data class PillData(
	val text: String,
	val highlight: Boolean = false,
)

@Composable
private fun HeroPill(
	text: String,
	highlight: Boolean = false,
) {
	val bgColor = if (highlight) VegafoXColors.OrangeSoft else VegafoXColors.Surface.copy(alpha = 0.4f)
	val borderColor = if (highlight) VegafoXColors.OrangeBorder else VegafoXColors.Divider
	val textColor = if (highlight) VegafoXColors.OrangePrimary else VegafoXColors.TextSecondary

	Box(
		modifier =
			Modifier
				.clip(RoundedCornerShape(4.dp))
				.background(bgColor)
				.padding(horizontal = 8.dp, vertical = 3.dp),
	) {
		Text(
			text = text,
			fontSize = 12.sp,
			fontWeight = FontWeight.Medium,
			color = textColor,
		)
	}
}

// -- Helpers --

private fun buildHeroTagLine(item: BaseItemDto): String {
	val genre = (item.genres?.firstOrNull() ?: item.genreItems?.firstOrNull()?.name)?.uppercase()
	val parts = mutableListOf<String>()

	when (item.type) {
		BaseItemKind.MOVIE -> {
			parts.add("FILM")
			genre?.let { parts.add(it) }
		}
		BaseItemKind.SERIES -> {
			parts.add("SÉRIE")
			genre?.let { parts.add(it) }
		}
		BaseItemKind.EPISODE -> {
			val seriesTitle =
				item.seriesName
					?.uppercase()
					?: "ÉPISODE"
			parts.add(seriesTitle)
			val s = item.parentIndexNumber
			val e = item.indexNumber
			if (s != null && e != null) parts.add("S${s}E$e")
		}
		BaseItemKind.PROGRAM -> {
			val seriesTitle =
				item.seriesName
					?.uppercase()
			if (seriesTitle != null) {
				parts.add(seriesTitle)
				val s = item.parentIndexNumber
				val e = item.indexNumber
				if (s != null && e != null) parts.add("S${s}E$e")
			} else {
				parts.add("TV EN DIRECT")
				genre?.let { parts.add(it) }
			}
		}
		BaseItemKind.TV_CHANNEL -> {
			parts.add("TV EN DIRECT")
		}
		BaseItemKind.PLAYLIST -> {
			// Hidden — no tag line
		}
		BaseItemKind.MUSIC_ALBUM -> {
			parts.add("MUSIQUE")
		}
		else -> {
			parts.add(item.type?.name?.uppercase() ?: "")
			genre?.let { parts.add(it) }
		}
	}

	return parts.filter { it.isNotEmpty() }.joinToString("  \u2022  ")
}

private fun getResolutionLabel(item: BaseItemDto): String? {
	val videoStream =
		item.mediaSources
			?.firstOrNull()
			?.mediaStreams
			?.firstOrNull { it.type == MediaStreamType.VIDEO }
			?: return null

	val width = videoStream.width ?: return null
	return when {
		width >= 3800 -> "4K"
		width >= 1900 -> "1080p"
		width >= 1260 -> "720p"
		width >= 700 -> "480p"
		else -> null
	}
}

private const val CROSSFADE_DURATION_MS = 400
private const val BACKDROP_ALPHA = 0.50f
private const val BLUR_RADIUS = 3f
private const val TRAILER_CROSSFADE_IN_MS = 800
private const val TRAILER_CROSSFADE_OUT_MS = 400
private const val TRAILER_COUNTDOWN_MS = 5_000
