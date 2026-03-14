package org.jellyfin.androidtv.ui.startup.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class QuickConnectUiState(
	val server: DiscoveredServer,
	val code: String = "",
	val secret: String = "",
	val isLoading: Boolean = true,
	val isWaiting: Boolean = false,
	val isAuthenticated: Boolean = false,
	val accessToken: String? = null,
	val error: String? = null,
)

const val TIMEOUT_ERROR = "TIMEOUT"
const val CONNECTION_ERROR = "CONNECTION_ERROR"

class QuickConnectViewModel(
	server: DiscoveredServer,
	private val quickConnectRepository: QuickConnectRepository,
) : ViewModel() {
	private val _uiState = MutableStateFlow(QuickConnectUiState(server = server))
	val uiState: StateFlow<QuickConnectUiState> = _uiState.asStateFlow()

	private var pollingJob: Job? = null

	init {
		initiate()
	}

	fun initiate() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				val result = quickConnectRepository.initiate(_uiState.value.server.address)
				_uiState.update {
					it.copy(
						code = result.code,
						secret = result.secret,
						isLoading = false,
					)
				}
			} catch (e: Exception) {
				Timber.e(e, "QuickConnect initiate failed")
				_uiState.update {
					it.copy(
						isLoading = false,
						error = e.message ?: CONNECTION_ERROR,
					)
				}
			}
		}
	}

	fun startPolling() {
		pollingJob?.cancel()
		pollingJob =
			viewModelScope.launch {
				_uiState.update { it.copy(isWaiting = true, error = null) }
				val address = _uiState.value.server.address
				val secret = _uiState.value.secret
				val maxAttempts = 100 // 100 * 3s = 5 minutes

				for (i in 0 until maxAttempts) {
					delay(3_000)
					try {
						val authorized = quickConnectRepository.checkAuthorized(address, secret)
						if (authorized) {
							val token = quickConnectRepository.connect(address, secret)
							_uiState.update {
								it.copy(
									isWaiting = false,
									isAuthenticated = true,
									accessToken = token,
								)
							}
							return@launch
						}
					} catch (e: Exception) {
						Timber.w(e, "QuickConnect poll error (attempt %d)", i)
					}
				}
				// Timeout
				_uiState.update {
					it.copy(
						isWaiting = false,
						error = TIMEOUT_ERROR,
					)
				}
			}
	}

	fun cancel() {
		pollingJob?.cancel()
		pollingJob = null
		_uiState.update { it.copy(isWaiting = false) }
	}

	override fun onCleared() {
		super.onCleared()
		pollingJob?.cancel()
	}
}
