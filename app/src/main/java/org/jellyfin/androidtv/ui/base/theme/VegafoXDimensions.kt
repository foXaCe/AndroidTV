package org.jellyfin.androidtv.ui.base.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// region — Dialogs

object DialogDimensions {
	val maxListHeight = 400.dp
	val standardMinWidth = 340.dp
	val standardMaxWidth = 440.dp
}

// endregion

// region — Buttons

object ButtonDimensions {
	val minWidth = 200.dp
	val minWidthCompact = 120.dp
	val height = 52.dp
	val heightCompact = 40.dp
}

// endregion

// region — Hero / Detail backdrop

object HeroDimensions {
	val backdropHeight = 580.dp
	val horizontalPadding = 80.dp
	val titleFontSize = 68.sp
	val titleLineHeight = 72.sp
	val actionsBarHeight = 80.dp
	val contentTopPadding = 100.dp
}

// endregion

// region — Cards

object CardDimensions {
	val landscapeWidth = 220.dp
	val landscapeHeight = 124.dp
	val portraitWidth = 150.dp
	val portraitHeight = 225.dp
}

// endregion

// region — Browse / Library layout

object BrowseDimensions {
	val contentPaddingHorizontal = 60.dp
	val gridPaddingHorizontal = 56.dp
}

// endregion

// region — Sidebar

object SidebarDimensions {
	val widthCollapsed = 72.dp
	val widthExpanded = 220.dp
	val navItemHeight = 52.dp
}

// endregion

// region — Jellyseerr

object JellyseerrDimensions {
	val screenPaddingHorizontal = 50.dp
	val mediaBackdropHeight = 400.dp
	val posterWidth = 208.dp
	val posterHeight = 312.dp
	val factsColumnWidth = 320.dp
}

// endregion

// region — Toolbar

object ToolbarDimensions {
	val height = 95.dp
}

// endregion

// region — Dream / Screensaver

object DreamDimensions {
	val logoWidth = 400.dp
	val logoHeight = 200.dp
	val albumCoverSize = 128.dp
	val clockWidth = 150.dp
	val carouselMaxHeight = 75.dp
	val fadingEdgeVertical = 250.dp
}

// endregion

// region — Live TV

object LiveTvDimensions {
	val guideHeaderHeight = 120.dp
	val programDetailDialogWidth = 640.dp
	val recordDialogWidth = 500.dp
	val browseScreenPadding = 80.dp
}

// endregion

// region — Startup

object StartupDimensions {
	val foxLogoSize = 160.dp
	val titleFontSize = 52.sp
	val dialogWidth = 420.dp
}

// endregion
