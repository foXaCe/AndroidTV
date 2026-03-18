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
	val height = 48.dp
	val heightCompact = 40.dp
	val cornerRadius = 10.dp
}

// endregion

// region — Hero / Detail backdrop

object HeroDimensions {
	val backdropHeight = 360.dp
	val heroHeight = 400.dp
	val horizontalPadding = 80.dp
	val titleFontSize = 56.sp
	val titleLineHeight = 60.sp
	val actionsBarHeight = 80.dp
	val contentTopPadding = 100.dp
	val posterTopPadding = 24.dp
	val posterWidth = 240.dp
	val columnGap = 32.dp
}

// endregion

// region — Detail screens

object DetailDimensions {
	val contentPaddingHorizontal = 24.dp
	val sectionPaddingTop = 16.dp
	val sectionSpacing = 24.dp
	val bottomPadding = 80.dp
	val gradientHeight = 40.dp
	val actionsSpacing = 48.dp
}

// endregion

// region — Detail section rows (fixed heights for LazyColumn item pre-allocation)

object DetailSectionDimensions {
	val headerHeight = 48.dp
	val episodesRowHeight = 195.dp
	val castRowHeight = 170.dp
	val castCardWidth = 130.dp
	val castCardHeight = 145.dp
	val castPhotoSize = 80.dp
	val castCardGap = 16.dp
	val similarRowHeight = 270.dp
	val seasonsRowHeight = 300.dp
}

// endregion

// region — Cards

object CardDimensions {
	val landscapeWidth = 220.dp
	val landscapeHeight = 124.dp
	val episodeCardTextHeight = 72.dp
	val portraitWidth = 150.dp
	val portraitHeight = 225.dp
	val folderWidth = 140.dp
	val folderHeight = 210.dp
	val squareSize = 140.dp
}

// endregion

// region — Browse / Library layout

object BrowseDimensions {
	val contentPaddingHorizontal = 60.dp
	val gridPaddingHorizontal = 56.dp

	// Header
	val headerPaddingTop = 32.dp
	val headerBottomSpacing = 16.dp
	val headerFontSize = 40.sp
	val headerLetterSpacing = 2.sp
	val sectionSubtitleFontSize = 14.sp

	// Cards / Grid
	val cardGap = 16.dp
	val gridBottomPadding = 27.dp
	val skeletonRowSpacing = 28.dp
	val rowTopPadding = 12.dp
	val rowTitleBottomPadding = 8.dp

	// Filter chips
	val chipCornerRadius = 50.dp
	val chipPaddingHorizontal = 16.dp
	val chipPaddingVertical = 8.dp
	val chipFontSize = 13.sp
	val chipIconTextGap = 6.dp
	val letterChipSize = 32.dp
	val chipSpacing = 8.dp
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

// region — Settings

object SettingsDimensions {
	val panelWidth = 350.dp
	val panelBorderWidth = 1.dp
	val headerLogoSize = 36.dp
	val headerHeight = 48.dp
	val headerPaddingHorizontal = 24.dp
	val headerPaddingVertical = 20.dp
	val headerGap = 14.dp
	val titlePaddingHorizontal = 12.dp
	val titlePaddingVertical = 16.dp
	val sectionDividerTopPadding = 24.dp
	val sectionDividerThickness = 1.dp
	val sectionLetterSpacing = 2.sp
	val sliderMinValueWidth = 32.dp
	val sliderMinValueWidthWide = 48.dp
	val sliderHeight = 4.dp
	val playerIconSize = 24.dp
}

// endregion

// region — Startup

object StartupDimensions {
	val foxLogoSize = 160.dp
	val titleFontSize = 52.sp
	val dialogWidth = 420.dp
}

// endregion
