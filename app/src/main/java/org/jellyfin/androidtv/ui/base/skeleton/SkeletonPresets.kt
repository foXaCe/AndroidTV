package org.jellyfin.androidtv.ui.base.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.ButtonDimensions
import org.jellyfin.androidtv.ui.base.theme.CardDimensions

/**
 * Skeleton for a media card (poster or landscape thumbnail).
 */
@Composable
fun SkeletonCard(
	width: Dp = CardDimensions.portraitWidth,
	height: Dp = CardDimensions.portraitHeight,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.width(width)) {
		SkeletonBox(
			modifier =
				Modifier
					.width(width)
					.height(height),
			shape = JellyfinTheme.shapes.small,
		)
		Spacer(modifier = Modifier.height(8.dp))
		SkeletonTextLine(width = width * 0.8f, height = 12.dp)
		Spacer(modifier = Modifier.height(4.dp))
		SkeletonTextLine(width = width * 0.5f, height = 10.dp)
	}
}

/**
 * Skeleton for a landscape card (e.g., episode, music album).
 *
 * @param showTextLines When false, only the image box is shown (matches BrowseMediaCard on home).
 */
@Composable
fun SkeletonLandscapeCard(
	width: Dp = CardDimensions.landscapeWidth,
	height: Dp = CardDimensions.landscapeHeight,
	showTextLines: Boolean = true,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.width(width)) {
		SkeletonBox(
			modifier =
				Modifier
					.width(width)
					.height(height),
			shape = JellyfinTheme.shapes.small,
		)
		if (showTextLines) {
			Spacer(modifier = Modifier.height(8.dp))
			SkeletonTextLine(width = width * 0.7f, height = 12.dp)
			Spacer(modifier = Modifier.height(4.dp))
			SkeletonTextLine(width = width * 0.4f, height = 10.dp)
		}
	}
}

/**
 * Skeleton for a horizontal row of cards (home screen rows).
 */
@Composable
fun SkeletonCardRow(
	cardCount: Int = 7,
	cardWidth: Dp = CardDimensions.portraitWidth,
	cardHeight: Dp = CardDimensions.portraitHeight,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		// Row header skeleton
		SkeletonTextLine(
			width = 140.dp,
			height = 16.dp,
			modifier = Modifier.padding(start = BrowseDimensions.contentPaddingHorizontal, bottom = 12.dp),
		)
		LazyRow(
			contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			userScrollEnabled = false,
		) {
			items(cardCount) {
				SkeletonCard(width = cardWidth, height = cardHeight)
			}
		}
	}
}

/**
 * Skeleton for a horizontal row of landscape cards.
 *
 * @param showCardTextLines When false, cards show only the image box (matches home screen cards).
 */
@Composable
fun SkeletonLandscapeCardRow(
	cardCount: Int = 5,
	cardWidth: Dp = CardDimensions.landscapeWidth,
	cardHeight: Dp = CardDimensions.landscapeHeight,
	showCardTextLines: Boolean = true,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		// Matches TvRowList: title with bottom = 8dp (spaceSm)
		SkeletonTextLine(
			width = 160.dp,
			height = 16.dp,
			modifier = Modifier.padding(bottom = 8.dp),
		)
		LazyRow(
			// Matches TvRowList inner contentPadding: horizontal = spaceSm (8dp)
			contentPadding = PaddingValues(horizontal = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			userScrollEnabled = false,
		) {
			items(cardCount) {
				SkeletonLandscapeCard(
					width = cardWidth,
					height = cardHeight,
					showTextLines = showCardTextLines,
				)
			}
		}
		// Matches TvRowList: spacer height = 16dp (spaceMd) after each row
		Spacer(modifier = Modifier.height(16.dp))
	}
}

/**
 * Skeleton for a grid of cards (library browse grid).
 */
@Composable
fun SkeletonCardGrid(
	columns: Int = 7,
	rows: Int = 3,
	cardWidth: Dp = CardDimensions.portraitWidth,
	cardHeight: Dp = CardDimensions.portraitHeight,
	modifier: Modifier = Modifier,
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(minSize = cardWidth),
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal, vertical = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalArrangement = Arrangement.spacedBy(24.dp),
		userScrollEnabled = false,
	) {
		items(columns * rows) {
			SkeletonCard(width = cardWidth, height = cardHeight)
		}
	}
}

/**
 * Skeleton for genre grid items.
 */
@Composable
fun SkeletonGenreGrid(
	columns: Int = 4,
	rows: Int = 3,
	modifier: Modifier = Modifier,
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(columns),
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(horizontal = BrowseDimensions.contentPaddingHorizontal, vertical = 16.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		userScrollEnabled = false,
	) {
		items(columns * rows) {
			SkeletonBox(
				modifier =
					Modifier
						.fillMaxWidth()
						.height(80.dp),
				shape = JellyfinTheme.shapes.medium,
			)
		}
	}
}

/**
 * Skeleton for item detail page (backdrop + info).
 */
@Composable
fun SkeletonItemDetail(modifier: Modifier = Modifier) {
	Box(modifier = modifier.fillMaxSize()) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(start = BrowseDimensions.contentPaddingHorizontal, end = BrowseDimensions.contentPaddingHorizontal, top = 80.dp),
		) {
			Row {
				// Poster
				SkeletonBox(
					modifier =
						Modifier
							.width(200.dp)
							.height(300.dp),
					shape = JellyfinTheme.shapes.medium,
				)
				Spacer(modifier = Modifier.width(32.dp))
				// Info
				Column(modifier = Modifier.weight(1f)) {
					SkeletonTextLine(width = 300.dp, height = 28.dp)
					Spacer(modifier = Modifier.height(12.dp))
					SkeletonTextLine(width = 180.dp, height = 14.dp)
					Spacer(modifier = Modifier.height(24.dp))
					SkeletonTextBlock(lines = 4, maxWidth = 400.dp)
					Spacer(modifier = Modifier.height(24.dp))
					// Buttons
					Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
						SkeletonBox(
							modifier = Modifier.width(120.dp).height(ButtonDimensions.heightCompact),
							shape = JellyfinTheme.shapes.button,
						)
						SkeletonBox(
							modifier = Modifier.width(120.dp).height(ButtonDimensions.heightCompact),
							shape = JellyfinTheme.shapes.button,
						)
					}
				}
			}
		}
	}
}

/**
 * Skeleton for multiple home-screen rows.
 */
@Composable
fun SkeletonHomeRows(
	rowCount: Int = 3,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier.fillMaxSize(),
		verticalArrangement = Arrangement.spacedBy(28.dp),
	) {
		repeat(rowCount) {
			SkeletonCardRow()
		}
	}
}
