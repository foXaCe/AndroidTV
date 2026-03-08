package org.jellyfin.androidtv.ui.base

import androidx.compose.ui.graphics.Color

/**
 * Shared overlay/toolbar background color map.
 * Used by MainToolbar and MediaBarSlideshowView for user-selectable background colors.
 */
object OverlayColors {
	private val colorMap = mapOf(
		"black" to Color.Black,
		"dark_blue" to Color(0xFF1A2332),
		"purple" to Color(0xFF4A148C),
		"teal" to Color(0xFF00695C),
		"navy" to Color(0xFF0D1B2A),
		"charcoal" to Color(0xFF36454F),
		"brown" to Color(0xFF3E2723),
		"dark_red" to Color(0xFF8B0000),
		"dark_green" to Color(0xFF0B4F0F),
		"slate" to Color(0xFF475569),
		"indigo" to Color(0xFF1E3A8A),
	)

	fun get(key: String): Color = colorMap[key] ?: Color.Gray
}
