package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType

/**
 * A reusable media item card for browse screens.
 * Uses [TvFocusCard] for D-pad focus handling and displays a poster image with title.
 */
@Composable
fun BrowseMediaCard(
	item: BaseItemDto,
	api: ApiClient,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocus: (() -> Unit)? = null,
	cardWidth: Dp = 150.dp,
	cardHeight: Dp = 225.dp,
) {
	TvFocusCard(
		onClick = onClick,
		modifier = modifier
			.width(cardWidth)
			.then(
				if (onFocus != null) Modifier.onFocusChanged { if (it.isFocused) onFocus() }
				else Modifier
			),
	) {
		Column {
			Box(
				modifier = Modifier
					.width(cardWidth)
					.height(cardHeight)
					.clip(JellyfinTheme.shapes.small)
					.background(JellyfinTheme.colorScheme.surfaceDim),
			) {
				val imageUrl = getItemImageUrl(item, api, cardHeight.value.toInt())
				if (imageUrl != null) {
					val placeholder = rememberGradientPlaceholder()
					val errorFallback = rememberErrorPlaceholder()
					AsyncImage(
						model = imageUrl,
						contentDescription = item.name,
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop,
						placeholder = placeholder,
						error = errorFallback,
					)
				} else {
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center,
					) {
						Icon(
							imageVector = ImageVector.vectorResource(R.drawable.ic_movie),
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							tint = JellyfinTheme.colorScheme.textDisabled,
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(6.dp))

			Text(
				text = item.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				color = JellyfinTheme.colorScheme.textPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			val year = item.productionYear?.toString()
			if (year != null) {
				Text(
					text = year,
					style = JellyfinTheme.typography.labelSmall,
					color = JellyfinTheme.colorScheme.textSecondary,
					maxLines = 1,
				)
			}
		}
	}
}

private fun getItemImageUrl(item: BaseItemDto, api: ApiClient, maxHeight: Int): String? {
	val primary = item.itemImages[ImageType.PRIMARY]
	if (primary != null) return primary.getUrl(api, maxHeight = maxHeight)

	val thumb = item.itemImages[ImageType.THUMB]
	if (thumb != null) return thumb.getUrl(api, maxHeight = maxHeight)

	val parentPrimary = item.parentImages[ImageType.PRIMARY]
	if (parentPrimary != null) return parentPrimary.getUrl(api, maxHeight = maxHeight)

	val parentThumb = item.parentImages[ImageType.THUMB]
	if (parentThumb != null) return parentThumb.getUrl(api, maxHeight = maxHeight)

	return null
}
