package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
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
	cardWidth: Dp = CardDimensions.portraitWidth,
	cardHeight: Dp = CardDimensions.portraitHeight,
) {
	TvFocusCard(
		onClick = onClick,
		modifier =
			modifier
				.width(cardWidth)
				.then(
					if (onFocus != null) {
						Modifier.onFocusChanged { if (it.isFocused) onFocus() }
					} else {
						Modifier
					},
				),
	) {
		Column {
			Box(
				modifier =
					Modifier
						.width(cardWidth)
						.height(cardHeight)
						.clip(JellyfinTheme.shapes.small)
						.background(VegafoXColors.SurfaceDim),
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
				// Status badge overlay
				CardStatusBadge(
					item = item,
					modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			Text(
				text = item.title ?: item.name ?: "",
				style = JellyfinTheme.typography.bodySmall,
				color = VegafoXColors.TextPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			val year = item.releaseDate?.take(4) ?: item.firstAirDate?.take(4)
			if (year != null) {
				Text(
					text = year,
					style = JellyfinTheme.typography.labelSmall,
					color = VegafoXColors.TextSecondary,
					maxLines = 1,
				)
			}
		}
	}
}

@Composable
private fun CardStatusBadge(
	item: JellyseerrDiscoverItemDto,
	modifier: Modifier = Modifier,
) {
	val mediaInfo = item.mediaInfo ?: return
	val status = mediaInfo.status
	val isDeclined =
		mediaInfo.requests?.any {
			it.status == JellyseerrRequestDto.STATUS_DECLINED
		} == true
	val isAvailable = status == 4 || status == 5
	val isPending = status == 2 || status == 3
	val isBlacklisted = status == 6

	if (!isDeclined && !isAvailable && !isPending && !isBlacklisted) return

	val bgColor: Color
	val textColor: Color
	when {
		isDeclined -> {
			bgColor = VegafoXColors.Error.copy(alpha = 0.20f)
			textColor = VegafoXColors.Error
		}
		isAvailable -> {
			bgColor = VegafoXColors.Success.copy(alpha = 0.20f)
			textColor = VegafoXColors.Success
		}
		isPending -> {
			bgColor = VegafoXColors.OrangePrimary.copy(alpha = 0.20f)
			textColor = VegafoXColors.OrangePrimary
		}
		else -> {
			bgColor = VegafoXColors.Error.copy(alpha = 0.20f)
			textColor = VegafoXColors.Error
		}
	}

	val labelText =
		when {
			isDeclined -> stringResource(R.string.jellyseerr_status_declined)
			isAvailable -> stringResource(R.string.jellyseerr_status_available)
			status == 2 -> stringResource(R.string.jellyseerr_status_pending)
			status == 3 -> stringResource(R.string.jellyseerr_status_processing)
			else -> stringResource(R.string.jellyseerr_status_blacklisted)
		}

	Text(
		text = labelText,
		style = JellyfinTheme.typography.labelSmall,
		color = textColor,
		modifier =
			modifier
				.clip(RoundedCornerShape(4.dp))
				.background(bgColor)
				.padding(horizontal = 6.dp, vertical = 2.dp),
	)
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
			modifier =
				Modifier
					.width(cardWidth)
					.height(cardHeight)
					.clip(JellyfinTheme.shapes.small)
					.background(VegafoXColors.SurfaceDim),
			contentAlignment = Alignment.Center,
		) {
			val backdropUrl =
				remember(genre.id) {
					if (genre.backdrops.isNotEmpty()) {
						"$TMDB_BACKDROP${genre.backdrops[Random.nextInt(genre.backdrops.size)]}"
					} else {
						null
					}
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
				modifier =
					Modifier
						.fillMaxSize()
						.background(VegafoXColors.Background.copy(alpha = 0.5f)),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = genre.name,
					style = JellyfinTheme.typography.titleSmall,
					color = VegafoXColors.TextPrimary,
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
			modifier =
				Modifier
					.width(cardWidth)
					.height(cardHeight)
					.clip(JellyfinTheme.shapes.small)
					.background(VegafoXColors.SurfaceDim),
			contentAlignment = Alignment.Center,
		) {
			if (!logoUrl.isNullOrEmpty()) {
				AsyncImage(
					model = logoUrl,
					contentDescription = name,
					modifier =
						Modifier
							.fillMaxSize()
							.padding(12.dp),
					contentScale = ContentScale.Fit,
				)
			} else {
				Text(
					text = name,
					style = JellyfinTheme.typography.titleSmall,
					color = VegafoXColors.TextSecondary,
					textAlign = TextAlign.Center,
				)
			}
		}
	}
}
