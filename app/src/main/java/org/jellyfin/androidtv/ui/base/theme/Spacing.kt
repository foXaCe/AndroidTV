package org.jellyfin.androidtv.ui.base.theme

import androidx.compose.ui.unit.dp
import org.jellyfin.design.Tokens

object TvSpacing {
	val screenHorizontal = Tokens.Space.space3xl // 48dp
	val screenVertical = Tokens.Space.spaceLg // 24dp
	val cardGap = Tokens.Space.spaceMd // 16dp — between cards in grids (was 12dp in some places)
	val sectionGap = Tokens.Space.spaceLg // 24dp
	val buttonHeight = Tokens.Space.space3xl // 48dp
	val iconSize = Tokens.Space.spaceLg // 24dp
	val iconSizeLarge = Tokens.Space.spaceXl // 32dp

	// Live TV guide
	val programCellHeight = 55.dp // matches GUIDE_ROW_HEIGHT_DP
	val channelHeaderWidth = 160.dp // matches header width in dp
	val timelineHeight = 40.dp // 40dp — improved readability
	val guideRowWidthPerMinDp = 7.dp // matches GUIDE_ROW_WIDTH_PER_MIN_DP
}
