package org.jellyfin.androidtv.ui.player.base.toast

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
data class MediaToastData(
	@DrawableRes val icon: Int,
	val progress: Float? = null,
)
