package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Scale
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder

/**
 * Centralised [AsyncImage] wrapper with Coil caching and optional decode-size constraints.
 *
 * @param model URL or any Coil-compatible data source.
 * @param contentDescription Accessibility description.
 * @param modifier Layout modifier.
 * @param contentScale How the image fills its bounds.
 * @param maxWidth Maximum decoded width in pixels (null = original).
 * @param maxHeight Maximum decoded height in pixels (null = original).
 * @param crossfade Whether to crossfade when the image loads.
 * @param placeholder Painter shown while loading (default: gradient).
 * @param error Painter shown on error (default: subtle surface color).
 */
@Composable
fun CachedAsyncImage(
	model: Any?,
	contentDescription: String?,
	modifier: Modifier = Modifier,
	contentScale: ContentScale = ContentScale.Crop,
	maxWidth: Int? = null,
	maxHeight: Int? = null,
	crossfade: Boolean = false,
	placeholder: Painter? = rememberGradientPlaceholder(),
	error: Painter? = rememberErrorPlaceholder(),
) {
	val context = LocalContext.current
	val imageRequest =
		remember(model, maxWidth, maxHeight, crossfade) {
			ImageRequest
				.Builder(context)
				.data(model)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.diskCachePolicy(CachePolicy.ENABLED)
				.crossfade(crossfade)
				.apply {
					if (maxWidth != null || maxHeight != null) {
						val w: Dimension = if (maxWidth != null) Dimension(maxWidth) else Dimension.Undefined
						val h: Dimension = if (maxHeight != null) Dimension(maxHeight) else Dimension.Undefined
						size(w, h)
						scale(Scale.FILL)
					}
				}.build()
		}

	AsyncImage(
		model = imageRequest,
		contentDescription = contentDescription,
		modifier = modifier,
		contentScale = contentScale,
		placeholder = placeholder,
		error = error,
	)
}
