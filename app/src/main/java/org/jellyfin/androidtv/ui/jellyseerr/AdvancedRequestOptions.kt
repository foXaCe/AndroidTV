package org.jellyfin.androidtv.ui.jellyseerr

/**
 * Data class containing the user's advanced request options selections.
 * Used by JellyseerrViewModel, MediaDetailsFragment, and Compose dialogs.
 */
data class AdvancedRequestOptions(
	val profileId: Int?,
	val rootFolderId: Int?,
	val serverId: Int?,
)
