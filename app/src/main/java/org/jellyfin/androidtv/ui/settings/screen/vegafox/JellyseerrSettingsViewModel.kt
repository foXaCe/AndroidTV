package org.jellyfin.androidtv.ui.settings.screen.vegafox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.data.repository.JellyseerrRepository
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrHttpClient
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.sdk.api.client.ApiClient
import timber.log.Timber

data class JellyseerrSettingsUiState(
	val isLoading: Boolean = true,
	val apiKey: String = "",
	val authMethod: String = "",
	val serverUrl: String = "",
	val vegafoxDisplayName: String = "",
	val currentUsername: String = "",
	val isReconnecting: Boolean = false,
) {
	val apiKeyStatus: ApiKeyStatus
		get() =
			when {
				apiKey.isNotEmpty() -> ApiKeyStatus.HAS_KEY
				authMethod.isNotEmpty() -> ApiKeyStatus.NO_KEY_AUTHENTICATED
				else -> ApiKeyStatus.NOT_LOGGED_IN
			}

	enum class ApiKeyStatus { HAS_KEY, NO_KEY_AUTHENTICATED, NOT_LOGGED_IN }
}

sealed interface JellyseerrSettingsEvent {
	data class JellyfinLoginSuccess(
		val hasApiKey: Boolean,
	) : JellyseerrSettingsEvent

	data class LocalLoginSuccess(
		val hasApiKey: Boolean,
	) : JellyseerrSettingsEvent

	data object ApiKeyLoginSuccess : JellyseerrSettingsEvent

	data class JellyfinLoginFailed(
		val errorType: ErrorType,
		val message: String,
	) : JellyseerrSettingsEvent

	data class LocalLoginFailed(
		val message: String,
	) : JellyseerrSettingsEvent

	data class ApiKeyLoginFailed(
		val message: String,
	) : JellyseerrSettingsEvent

	data object LogoutSuccess : JellyseerrSettingsEvent

	data object VegafoXDisconnectSuccess : JellyseerrSettingsEvent

	data object VegafoXReconnectSuccess : JellyseerrSettingsEvent

	data object VegafoXNotEnabled : JellyseerrSettingsEvent

	data object VegafoXReconnectFailed : JellyseerrSettingsEvent

	data object ServerUrlSaved : JellyseerrSettingsEvent

	data object AllFieldsRequired : JellyseerrSettingsEvent

	enum class ErrorType { SERVER_CONFIG, AUTH_FAILED, CONNECTION, EXCEPTION }
}

class JellyseerrSettingsViewModel(
	private val jellyseerrRepository: JellyseerrRepository,
	private val userRepository: UserRepository,
	private val apiClient: ApiClient,
	private val globalJellyseerrPreferences: JellyseerrPreferences,
) : ViewModel() {
	private val _uiState = MutableStateFlow(JellyseerrSettingsUiState())
	val uiState: StateFlow<JellyseerrSettingsUiState> = _uiState.asStateFlow()

	private val _events = Channel<JellyseerrSettingsEvent>(Channel.BUFFERED)
	val events: Flow<JellyseerrSettingsEvent> = _events.receiveAsFlow()

	val isVegafoXMode: StateFlow<Boolean> = jellyseerrRepository.isVegafoXMode

	private val _userPreferences = MutableStateFlow<JellyseerrPreferences?>(null)
	val userPreferences: StateFlow<JellyseerrPreferences?> = _userPreferences.asStateFlow()

	init {
		viewModelScope.launch { loadState() }
	}

	private suspend fun loadState() {
		val prefs = jellyseerrRepository.getPreferences()
		_userPreferences.value = prefs
		val currentUser = userRepository.currentUser.value
		_uiState.update {
			it.copy(
				isLoading = false,
				apiKey = prefs?.get(JellyseerrPreferences.apiKey) ?: "",
				authMethod = prefs?.get(JellyseerrPreferences.authMethod) ?: "",
				serverUrl = prefs?.get(JellyseerrPreferences.serverUrl) ?: "",
				vegafoxDisplayName = prefs?.get(JellyseerrPreferences.vegafoxDisplayName) ?: "",
				currentUsername = currentUser?.name ?: "",
			)
		}
	}

	private suspend fun refreshState() {
		val prefs = jellyseerrRepository.getPreferences()
		_userPreferences.value = prefs
		val currentUser = userRepository.currentUser.value
		_uiState.update {
			it.copy(
				apiKey = prefs?.get(JellyseerrPreferences.apiKey) ?: "",
				authMethod = prefs?.get(JellyseerrPreferences.authMethod) ?: "",
				serverUrl = prefs?.get(JellyseerrPreferences.serverUrl) ?: "",
				vegafoxDisplayName = prefs?.get(JellyseerrPreferences.vegafoxDisplayName) ?: "",
				currentUsername = currentUser?.name ?: "",
			)
		}
	}

	fun saveServerUrl(url: String) {
		_userPreferences.value?.set(JellyseerrPreferences.serverUrl, url)
		_uiState.update { it.copy(serverUrl = url) }
		viewModelScope.launch {
			_events.send(JellyseerrSettingsEvent.ServerUrlSaved)
		}
	}

	fun reconnectVegafoX() {
		if (_uiState.value.isReconnecting) return
		_uiState.update { it.copy(isReconnecting = true) }
		viewModelScope.launch {
			try {
				val baseUrl = apiClient.baseUrl
				val token = apiClient.accessToken
				if (!baseUrl.isNullOrBlank() && !token.isNullOrBlank()) {
					jellyseerrRepository
						.configureWithVegafoX(baseUrl, token)
						.onSuccess { status ->
							if (status.authenticated || status.enabled) {
								_events.send(JellyseerrSettingsEvent.VegafoXReconnectSuccess)
							} else {
								_events.send(JellyseerrSettingsEvent.VegafoXNotEnabled)
							}
							refreshState()
						}.onFailure {
							_events.send(JellyseerrSettingsEvent.VegafoXReconnectFailed)
						}
				}
			} finally {
				_uiState.update { it.copy(isReconnecting = false) }
			}
		}
	}

	fun loginWithJellyfin(password: String) {
		viewModelScope.launch {
			val currentUser = userRepository.currentUser.value
			val username = currentUser?.name ?: ""
			val jellyfinServerUrl = apiClient.baseUrl ?: ""
			val jellyseerrServerUrl = _uiState.value.serverUrl

			if (username.isBlank() || password.isBlank() || jellyfinServerUrl.isBlank() || jellyseerrServerUrl.isBlank()) {
				_events.send(JellyseerrSettingsEvent.AllFieldsRequired)
				return@launch
			}

			try {
				val userId = currentUser?.id?.toString()
				if (userId != null) {
					JellyseerrHttpClient.switchCookieStorage(userId)
				}
				globalJellyseerrPreferences[JellyseerrPreferences.lastJellyfinUser] = username

				jellyseerrRepository
					.loginWithJellyfin(username, password, jellyfinServerUrl, jellyseerrServerUrl)
					.onSuccess { user ->
						_events.send(JellyseerrSettingsEvent.JellyfinLoginSuccess(hasApiKey = !user.apiKey.isNullOrEmpty()))
						refreshState()
					}.onFailure { error ->
						val errorType =
							when {
								error.message?.contains("configuration error") == true -> JellyseerrSettingsEvent.ErrorType.SERVER_CONFIG
								error.message?.contains("Authentication failed") == true -> JellyseerrSettingsEvent.ErrorType.AUTH_FAILED
								else -> JellyseerrSettingsEvent.ErrorType.CONNECTION
							}
						_events.send(JellyseerrSettingsEvent.JellyfinLoginFailed(errorType, error.message ?: ""))
						Timber.e(error, "Jellyseerr: Jellyfin authentication failed")
					}
			} catch (e: Exception) {
				_events.send(
					JellyseerrSettingsEvent.JellyfinLoginFailed(JellyseerrSettingsEvent.ErrorType.EXCEPTION, e.message ?: ""),
				)
				Timber.e(e, "Jellyseerr: Connection failed")
			}
		}
	}

	fun loginLocal(
		email: String,
		password: String,
	) {
		viewModelScope.launch {
			val serverUrl = _uiState.value.serverUrl
			try {
				jellyseerrRepository
					.loginLocal(email, password, serverUrl)
					.onSuccess { user ->
						_events.send(JellyseerrSettingsEvent.LocalLoginSuccess(hasApiKey = user.apiKey?.isNotEmpty() == true))
						refreshState()
					}.onFailure { error ->
						Timber.e(error, "Jellyseerr: Local login failed")
						_events.send(JellyseerrSettingsEvent.LocalLoginFailed(error.message ?: ""))
					}
			} catch (e: Exception) {
				Timber.e(e, "Jellyseerr: Local login exception")
				_events.send(JellyseerrSettingsEvent.LocalLoginFailed(e.message ?: ""))
			}
		}
	}

	fun loginWithApiKey(inputApiKey: String) {
		viewModelScope.launch {
			val serverUrl = _uiState.value.serverUrl
			try {
				jellyseerrRepository
					.loginWithApiKey(inputApiKey, serverUrl)
					.onSuccess {
						_events.send(JellyseerrSettingsEvent.ApiKeyLoginSuccess)
						refreshState()
					}.onFailure { error ->
						Timber.e(error, "Jellyseerr: API key login failed")
						_events.send(JellyseerrSettingsEvent.ApiKeyLoginFailed(error.message ?: ""))
					}
			} catch (e: Exception) {
				Timber.e(e, "Jellyseerr: API key login exception")
				_events.send(JellyseerrSettingsEvent.ApiKeyLoginFailed(e.message ?: ""))
			}
		}
	}

	fun logout() {
		viewModelScope.launch {
			jellyseerrRepository.logout()
			_events.send(JellyseerrSettingsEvent.LogoutSuccess)
			refreshState()
		}
	}

	fun logoutVegafoX() {
		viewModelScope.launch {
			jellyseerrRepository.logoutVegafoX()
			_events.send(JellyseerrSettingsEvent.VegafoXDisconnectSuccess)
			refreshState()
		}
	}
}
