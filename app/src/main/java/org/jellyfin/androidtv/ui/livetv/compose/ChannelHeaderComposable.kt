package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.shared.components.CachedAsyncImage
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun ChannelHeader(
	channel: BaseItemDto,
	imageUrl: String?,
	isFavorite: Boolean,
	isFocused: Boolean,
	accentColor: Color,
	modifier: Modifier = Modifier,
) {
	val bgColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary.copy(alpha = 0.15f)
			else -> VegafoXColors.BackgroundDeep
		}

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(bgColor),
	) {
		// Right separator
		Box(
			modifier =
				Modifier
					.align(Alignment.CenterEnd)
					.width(1.dp)
					.fillMaxHeight()
					.background(VegafoXColors.PanelBorder),
		)

		// Focus: right border accent (grid side)
		if (isFocused) {
			Box(
				modifier =
					Modifier
						.align(Alignment.CenterEnd)
						.width(3.dp)
						.fillMaxHeight()
						.background(VegafoXColors.OrangePrimary),
			)
		}

		// Content
		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(horizontal = 4.dp, vertical = 2.dp),
			contentAlignment = Alignment.Center,
		) {
			if (imageUrl != null) {
				// Channel logo
				CachedAsyncImage(
					model = imageUrl,
					contentDescription = channel.name,
					modifier =
						Modifier
							.size(width = 90.dp, height = 44.dp)
							.clip(RoundedCornerShape(4.dp)),
				)
			} else {
				// Fallback: number + name centered
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						text = channel.number ?: "",
						fontSize = 14.sp,
						color = VegafoXColors.TextSecondary,
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
					Text(
						text = channel.name ?: "",
						fontSize = 14.sp,
						color = VegafoXColors.TextSecondary,
						textAlign = TextAlign.Center,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}

		// Channel number (bottom-right, discreet)
		if (imageUrl != null) {
			Text(
				text = channel.number ?: "",
				fontSize = 12.sp,
				color = VegafoXColors.TextHint,
				modifier =
					Modifier
						.align(Alignment.BottomEnd)
						.padding(end = 6.dp, bottom = 2.dp),
			)
		}

		// Favorite icon (visible only on focus)
		if (isFavorite && isFocused) {
			Icon(
				painter = rememberVectorPainter(VegafoXIcons.Favorite),
				tint = VegafoXColors.OrangePrimary,
				contentDescription = null,
				modifier =
					Modifier
						.size(14.dp)
						.align(Alignment.TopEnd)
						.padding(end = 4.dp, top = 2.dp),
			)
		}
	}
}
