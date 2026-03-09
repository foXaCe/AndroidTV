package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import kotlin.random.Random

private const val TMDB_POSTER = "https://image.tmdb.org/t/p/w500"
private const val TMDB_BACKDROP = "https://image.tmdb.org/t/p/w780"

/**
 * Poster card for Jellyseerr media items (movies / TV).
 * Uses TMDB poster URLs directly. Replaces Leanback CardPresenter.
 */
@Composable
fun JellyseerrPosterCard(
	item: JellyseerrDiscoverItemDto,
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
				val posterUrl = item.posterPath?.let { "$TMDB_POSTER$it" }
				if (posterUrl != null) {
					val placeholder = rememberGradientPlaceholder()
					AsyncImage(
						model = posterUrl,
						contentDescription = item.title ?: item.name,
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop,
						placeholder = placeholder,
					)
				}
			}

			Spacer(modifier = Modifier.height(6.dp))

			Text(
				text = item.title ?: item.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				color = JellyfinTheme.colorScheme.textPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			val year = item.releaseDate?.take(4) ?: item.firstAirDate?.take(4)
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

/**
 * Landscape card for genre browsing with backdrop image + name overlay.
 * Replaces Leanback GenreCardPresenter.
 */
@Composable
fun JellyseerrGenreCard(
	genre: JellyseerrGenreDto,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	cardWidth: Dp = 300.dp,
	cardHeight: Dp = 150.dp,
) {
	TvFocusCard(
		onClick = onClick,
		modifier = modifier.width(cardWidth),
	) {
		Box(
			modifier = Modifier
				.width(cardWidth)
				.height(cardHeight)
				.clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.surfaceDim),
			contentAlignment = Alignment.Center,
		) {
			val backdropUrl = remember(genre.id) {
				if (genre.backdrops.isNotEmpty()) {
					"$TMDB_BACKDROP${genre.backdrops[Random.nextInt(genre.backdrops.size)]}"
				} else null
			}
			if (backdropUrl != null) {
				AsyncImage(
					model = backdropUrl,
					contentDescription = genre.name,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
				)
			}
			// Dark overlay + centered title
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(JellyfinTheme.colorScheme.background.copy(alpha = 0.5f)),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = genre.name,
					style = JellyfinTheme.typography.titleSmall,
					color = JellyfinTheme.colorScheme.textPrimary,
					textAlign = TextAlign.Center,
				)
			}
		}
	}
}

/**
 * Landscape card for network / studio logos.
 * Replaces Leanback NetworkStudioCardPresenter.
 */
@Composable
fun JellyseerrLogoCard(
	name: String,
	logoUrl: String?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	cardWidth: Dp = 300.dp,
	cardHeight: Dp = 150.dp,
) {
	TvFocusCard(
		onClick = onClick,
		modifier = modifier.width(cardWidth),
	) {
		Box(
			modifier = Modifier
				.width(cardWidth)
				.height(cardHeight)
				.clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.surfaceDim),
			contentAlignment = Alignment.Center,
		) {
			if (!logoUrl.isNullOrEmpty()) {
				AsyncImage(
					model = logoUrl,
					contentDescription = name,
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp),
					contentScale = ContentScale.Fit,
				)
			} else {
				Text(
					text = name,
					style = JellyfinTheme.typography.titleSmall,
					color = JellyfinTheme.colorScheme.textSecondary,
					textAlign = TextAlign.Center,
				)
			}
		}
	}
}
