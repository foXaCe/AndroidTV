package org.jellyfin.androidtv.ui.player.base.toast

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class MediaToastData(
	val icon: ImageVector,
	val progress: Float? = null,
)
