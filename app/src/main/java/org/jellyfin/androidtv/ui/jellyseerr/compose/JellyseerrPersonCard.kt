package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrCastMemberDto
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder

private const val TMDB_PROFILE = "https://image.tmdb.org/t/p/w185"

/**
 * Composable card for Jellyseerr cast/crew members.
 * Portrait photo (2:3 aspect) with name and role below.
 * Replaces the AndroidView + CardPresenter bridge.
 */
@Composable
fun JellyseerrPersonCard(
	castMember: JellyseerrCastMemberDto,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.05f else 1f,
		animationSpec = spring(stiffness = Spring.StiffnessMedium),
		label = "castCardScale",
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier =
			modifier
				.width(80.dp)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
				.focusable(interactionSource = interactionSource),
	) {
		val profileUrl = castMember.profilePath?.let { "$TMDB_PROFILE$it" }
		val placeholder = rememberGradientPlaceholder()

		AsyncImage(
			model = profileUrl,
			contentDescription = castMember.name,
			contentScale = ContentScale.Crop,
			placeholder = placeholder,
			modifier =
				Modifier
					.size(64.dp)
					.clip(CircleShape)
					.background(VegafoXColors.SurfaceDim)
					.border(
						width = if (isFocused) 2.dp else 1.dp,
						color = if (isFocused) VegafoXColors.OrangePrimary else Color.White.copy(alpha = 0.10f),
						shape = CircleShape,
					),
		)

		Text(
			text = castMember.name,
			style =
				TextStyle(
					fontSize = 13.sp,
					color = VegafoXColors.TextPrimary,
				),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 6.dp),
		)

		castMember.character?.let { role ->
			Text(
				text = role,
				style =
					TextStyle(
						fontSize = 12.sp,
						color = VegafoXColors.TextHint,
					),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				textAlign = TextAlign.Center,
			)
		}
	}
}
