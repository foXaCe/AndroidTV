package org.jellyfin.androidtv.ui.startup.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.model.AuthenticationSortBy
import org.jellyfin.androidtv.auth.model.AutomaticAuthenticateMethod
import org.jellyfin.androidtv.auth.model.LoginState
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.model.Server
import org.jellyfin.androidtv.auth.model.User
import org.jellyfin.androidtv.auth.repository.AuthenticationRepository
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.ServerUserRepository
import org.jellyfin.androidtv.auth.store.AuthenticationPreferences
import org.jellyfin.androidtv.util.PinCodeUtil
import java.util.UUID

data class UserSelectionUiState(
	val serverName: String = "",
	val users: List<User> = emptyList(),
	val isLoading: Boolean = true,
	val error: String? = null,
)

class UserSelectionViewModel(
	private val serverRepository: ServerRepository,
	private val serverUserRepository: ServerUserRepository,
	private val authenticationRepository: AuthenticationRepository,
	private val authenticationPreferences: AuthenticationPreferences,
) : ViewModel() {
	private val _uiState = MutableStateFlow(UserSelectionUiState())
	val uiState = _uiState.asStateFlow()

	private val userComparator =
		compareByDescending<User> { user ->
			if (
				authenticationPreferences[AuthenticationPreferences.sortBy] == AuthenticationSortBy.LAST_USE &&
				user is PrivateUser
			) {
				user.lastUsed
			} else {
				null
			}
		}.thenBy { user -> user.name }

	fun getServer(id: UUID): Server? =
		serverRepository.storedServers.value
			.find { it.id == id }

	fun loadUsers(server: Server) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }

			try {
				val storedUsers = serverUserRepository.getStoredServerUsers(server)
				_uiState.update {
					it.copy(
						serverName = server.name,
						users = storedUsers.sortedWith(userComparator),
						isLoading = false,
					)
				}

				val storedUserIds = storedUsers.map { it.id }
				val publicUsers =
					serverUserRepository
						.getPublicServerUsers(server)
						.filterNot { it.id in storedUserIds }
				_uiState.update {
					it.copy(
						users = (storedUsers + publicUsers).sortedWith(userComparator),
					)
				}
			} catch (e: Exception) {
				_uiState.update {
					it.copy(isLoading = false, error = e.message)
				}
			}
		}
	}

	fun getUserImageUrl(
		server: Server,
		user: User,
	): String? = authenticationRepository.getUserImageUrl(server, user)

	fun authenticate(
		server: Server,
		user: User,
	): Flow<LoginState> = authenticationRepository.authenticate(server, AutomaticAuthenticateMethod(user))

	fun isPinEnabled(
		context: Context,
		user: User,
	): Boolean = PinCodeUtil.isPinEnabled(context, user.id)

	suspend fun updateServer(server: Server): Boolean = serverRepository.updateServer(server)

	fun reloadStoredServers() {
		viewModelScope.launch { serverRepository.loadStoredServers() }
	}
}
