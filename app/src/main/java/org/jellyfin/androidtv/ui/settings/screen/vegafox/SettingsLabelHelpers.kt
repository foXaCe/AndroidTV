package org.jellyfin.androidtv.ui.settings.screen.vegafox

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R

@Composable
fun getShuffleContentTypeLabel(type: String): String =
	when (type) {
		"movies" -> stringResource(R.string.pref_shuffle_movies)
		"tv" -> stringResource(R.string.pref_shuffle_tv)
		"both" -> stringResource(R.string.pref_shuffle_both)
		else -> type
	}

@Composable
fun getMediaBarItemCountLabel(count: String): String =
	when (count) {
		"5" -> stringResource(R.string.pref_media_bar_5_items)
		"10" -> stringResource(R.string.pref_media_bar_10_items)
		"15" -> stringResource(R.string.pref_media_bar_15_items)
		else -> count
	}

@Composable
fun getOverlayColorLabel(color: String): String =
	when (color) {
		"black" -> stringResource(R.string.pref_media_bar_color_black)
		"gray" -> stringResource(R.string.pref_media_bar_color_gray)
		"dark_blue" -> stringResource(R.string.pref_media_bar_color_dark_blue)
		"purple" -> stringResource(R.string.pref_media_bar_color_purple)
		"teal" -> stringResource(R.string.pref_media_bar_color_teal)
		"navy" -> stringResource(R.string.pref_media_bar_color_navy)
		"charcoal" -> stringResource(R.string.pref_media_bar_color_charcoal)
		"brown" -> stringResource(R.string.pref_media_bar_color_brown)
		"dark_red" -> stringResource(R.string.pref_media_bar_color_dark_red)
		"dark_green" -> stringResource(R.string.pref_media_bar_color_dark_green)
		"slate" -> stringResource(R.string.pref_media_bar_color_slate)
		"indigo" -> stringResource(R.string.pref_media_bar_color_indigo)
		else -> color
	}

@Composable
fun getBlurLabel(value: Int): String =
	when (value) {
		0 -> stringResource(R.string.pref_blur_none)
		5 -> stringResource(R.string.pref_blur_light)
		10 -> stringResource(R.string.pref_blur_medium)
		15 -> stringResource(R.string.pref_blur_strong)
		20 -> stringResource(R.string.pref_blur_extra_strong)
		else -> "${value}dp"
	}
