package org.jellyfin.androidtv.ui.home.compose

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType

/**
 * Full-screen hero backdrop that changes with D-pad navigation.
 * Displays the backdrop image of the currently focused item with:
 * - Crossfade transition (400ms)
 * - Vertical gradient (transparent -> ds_background) at bottom
 * - Horizontal gradient (transparent -> ds_background) at left
 * - Blur effect (API 31+, radius 8px)
 * - 40% opacity to avoid distracting from content
 */
@Composable
fun HomeHeroBackdrop(
	item: BaseItemDto?,
	api: ApiClient,
	modifier: Modifier = Modifier,
) {
	val bgColor = JellyfinTheme.colorScheme.background

	// Derive the backdrop URL to avoid unnecessary recompositions
	val backdropUrl by remember(item?.id) {
		derivedStateOf {
			item?.let { getBackdropUrl(it, api) }
		}
	}

	Box(modifier = modifier.fillMaxSize()) {
		// Crossfade between backdrop images
		Crossfade(
			targetState = backdropUrl,
			animationSpec = tween(durationMillis = CROSSFADE_DURATION_MS),
			label = "home_hero_backdrop",
		) { url ->
			if (url != null) {
				AsyncImage(
					model = url,
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.graphicsLayer {
							alpha = BACKDROP_ALPHA
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
								renderEffect = RenderEffect
									.createBlurEffect(BLUR_RADIUS, BLUR_RADIUS, Shader.TileMode.CLAMP)
									.asComposeRenderEffect()
							}
						},
					contentScale = ContentScale.Crop,
				)
			}
		}

		// Vertical gradient: transparent at top -> background at bottom
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.verticalGradient(
						colors = listOf(
							Color.Transparent,
							bgColor.copy(alpha = 0.3f),
							bgColor.copy(alpha = 0.7f),
							bgColor,
						),
					)
				)
		)

		// Horizontal gradient: background at left -> transparent at right
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.horizontalGradient(
						colors = listOf(
							bgColor.copy(alpha = 0.8f),
							bgColor.copy(alpha = 0.4f),
							Color.Transparent,
						),
					)
				)
		)
	}
}

private fun getBackdropUrl(item: BaseItemDto, api: ApiClient): String? {
	val backdrop = item.itemImages[ImageType.BACKDROP]
	if (backdrop != null) return backdrop.getUrl(api, maxWidth = 1920)

	val thumb = item.itemImages[ImageType.THUMB]
	if (thumb != null) return thumb.getUrl(api, maxWidth = 1920)

	val parentBackdrop = item.parentImages[ImageType.BACKDROP]
	if (parentBackdrop != null) return parentBackdrop.getUrl(api, maxWidth = 1920)

	return null
}

private const val CROSSFADE_DURATION_MS = 400
private const val BACKDROP_ALPHA = 0.4f
private const val BLUR_RADIUS = 8f
