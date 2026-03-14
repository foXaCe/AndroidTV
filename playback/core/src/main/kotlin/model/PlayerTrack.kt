package org.jellyfin.playback.core.model

data class PlayerTrack(
	val index: Int,
	val label: String?,
	val language: String?,
	val codec: String?,
	val isSelected: Boolean,
)
