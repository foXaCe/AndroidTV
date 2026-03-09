package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrCastMemberDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
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
	TvFocusCard(
		onClick = onClick,
		modifier = modifier.width(130.dp),
	) {
		Column {
			val profileUrl = castMember.profilePath?.let { "$TMDB_PROFILE$it" }
			val placeholder = rememberGradientPlaceholder()

			AsyncImage(
				model = profileUrl,
				contentDescription = castMember.name,
				contentScale = ContentScale.Crop,
				placeholder = placeholder,
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(2f / 3f)
					.background(JellyfinTheme.colorScheme.surfaceDim),
			)

			Column(
				modifier = Modifier.padding(
					horizontal = TvSpacing.cardGap,
					vertical = 6.dp,
				)
			) {
				Text(
					text = castMember.name,
					style = JellyfinTheme.typography.labelMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)

				castMember.character?.let { role ->
					Text(
						text = role,
						style = JellyfinTheme.typography.labelSmall,
						color = JellyfinTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
	}
}
