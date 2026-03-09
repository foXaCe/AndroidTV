package org.jellyfin.androidtv.ui.itemdetail.compose

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
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * Full-screen hero backdrop for item detail screens.
 *
 * Displays the backdrop image with:
 * - Crossfade transition (400ms) when item changes
 * - Configurable blur (RenderEffect on API 31+)
 * - Vertical gradient: transparent at top -> ds_background at bottom
 * - Horizontal gradient: ds_background at left -> transparent at right
 * - Configurable opacity
 *
 * For Person/Playlist types, the caller should hide this and use
 * the slideshow backdrop from the content composable instead.
 */
@Composable
fun DetailHeroBackdrop(
	item: BaseItemDto?,
	api: ApiClient,
	blurAmount: Int = 0,
	modifier: Modifier = Modifier,
) {
	val bgColor = JellyfinTheme.colorScheme.background

	val backdropUrl by remember(item?.id) {
		derivedStateOf {
			item?.let { getBackdropUrl(it, api) }
		}
	}

	Box(modifier = modifier.fillMaxSize()) {
		Crossfade(
			targetState = backdropUrl,
			animationSpec = tween(durationMillis = AnimationDefaults.DURATION_MEDIUM + 100),
			label = "detail_hero_backdrop",
		) { url ->
			if (url != null) {
				AsyncImage(
					model = url,
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.graphicsLayer {
							alpha = BACKDROP_ALPHA
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurAmount > 0) {
								renderEffect = RenderEffect
									.createBlurEffect(
										blurAmount.toFloat(),
										blurAmount.toFloat(),
										Shader.TileMode.CLAMP,
									)
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
						colorStops = arrayOf(
							0.0f to Color.Transparent,
							0.3f to Color.Transparent,
							0.5f to bgColor.copy(alpha = 0.25f),
							0.65f to bgColor.copy(alpha = 0.63f),
							0.8f to bgColor.copy(alpha = 0.88f),
							1.0f to bgColor,
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
							bgColor.copy(alpha = 0.9f),
							bgColor.copy(alpha = 0.3f),
							Color.Transparent,
						),
					)
				)
		)
	}
}

private const val BACKDROP_ALPHA = 0.8f
