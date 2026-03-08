package org.jellyfin.androidtv.ui.jellyseerr

sealed class JellyseerrLoadingState {
	data object Idle : JellyseerrLoadingState()
	data object Loading : JellyseerrLoadingState()
	data class Success(val message: String = "") : JellyseerrLoadingState()
	data class Error(val message: String) : JellyseerrLoadingState()
}
