package org.jellyfin.androidtv.ui.jellyseerr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.repository.JellyseerrRepository
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrUserDto
import org.jellyfin.androidtv.util.ErrorHandler
import timber.log.Timber

class JellyseerrAuthViewModel(
	private val jellyseerrRepository: JellyseerrRepository,
) : ViewModel() {

	private val _loadingState = MutableStateFlow<JellyseerrLoadingState>(JellyseerrLoadingState.Idle)
	val loadingState: StateFlow<JellyseerrLoadingState> = _loadingState.asStateFlow()

	val isAvailable: StateFlow<Boolean> = jellyseerrRepository.isAvailable

	init {
		viewModelScope.launch {
			val result = ErrorHandler.catching("initialize Jellyseerr repository") {
				jellyseerrRepository.ensureInitialized()
			}
			if (result.isSuccess) {
				Timber.d("JellyseerrAuthViewModel: Repository initialized successfully")
			}
		}
	}

	fun initializeJellyseerr(serverUrl: String, apiKey: String) {
		viewModelScope.launch {
			_loadingState.emit(JellyseerrLoadingState.Loading)
			val result = ErrorHandler.catching("initialize Jellyseerr") {
				jellyseerrRepository.initialize(serverUrl, apiKey)
			}

			if (result.isSuccess && result.getOrNull()?.isSuccess == true) {
				_loadingState.emit(JellyseerrLoadingState.Success("Jellyseerr initialized successfully"))
			} else {
				val errorMessage = result.getOrNull()?.exceptionOrNull()?.let { error ->
					ErrorHandler.getUserFriendlyMessage(error, "initialize Jellyseerr")
				} ?: ErrorHandler.getUserFriendlyMessage(
					result.exceptionOrNull() ?: Exception("Initialization failed")
				)
				_loadingState.emit(JellyseerrLoadingState.Error(errorMessage))
			}
		}
	}

	suspend fun loginWithJellyfin(username: String, password: String, jellyfinUrl: String, jellyseerrUrl: String): Result<JellyseerrUserDto> {
		return jellyseerrRepository.loginWithJellyfin(username, password, jellyfinUrl, jellyseerrUrl)
	}

	override fun onCleared() {
		super.onCleared()
		jellyseerrRepository.close()
	}
}
