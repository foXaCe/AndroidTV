package org.jellyfin.androidtv.ui.startup.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.model.AuthenticationSortBy
import org.jellyfin.androidtv.auth.model.PrivateUser
import org.jellyfin.androidtv.auth.model.Server
import org.jellyfin.androidtv.auth.model.User
import org.jellyfin.androidtv.auth.repository.AuthenticationRepository
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.auth.repository.ServerUserRepository
import org.jellyfin.androidtv.auth.store.AuthenticationPreferences
import java.util.UUID

data class UserSelectionUiState(
	val serverName: String = "",
	val users: List<UserUiModel> = emptyList(),
	val isLoading: Boolean = true,
	val error: String? = null,
)

data class UserUiModel(
	val user: User,
	val imageUrl: String?,
)

class UserSelectionViewModel(
	private val serverRepository: ServerRepository,
	private val serverUserRepository: ServerUserRepository,
	private val authenticationRepository: AuthenticationRepository,
	private val authenticationPreferences: AuthenticationPreferences,
) : ViewModel() {
	private val _uiState = MutableStateFlow(UserSelectionUiState())
	val uiState: StateFlow<UserSelectionUiState> = _uiState.asStateFlow()

	private var server: Server? = null

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

	fun getServer(id: UUID): Server? = serverRepository.storedServers.value.find { it.id == id }

	fun loadUsers(server: Server) {
		this.server = server
		_uiState.update { it.copy(serverName = server.name, isLoading = true, error = null) }

		viewModelScope.launch {
			try {
				val storedUsers = serverUserRepository.getStoredServerUsers(server)
				val sorted = storedUsers.sortedWith(userComparator)
				_uiState.update { it.copy(users = sorted.map { user -> user.toUiModel(server) }, isLoading = false) }

				val storedUserIds = storedUsers.map { it.id }
				val publicUsers =
					serverUserRepository
						.getPublicServerUsers(server)
						.filterNot { it.id in storedUserIds }
				val allUsers = (storedUsers + publicUsers).sortedWith(userComparator)
				_uiState.update { it.copy(users = allUsers.map { user -> user.toUiModel(server) }) }
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun getUserAtIndex(index: Int): User? =
		_uiState.value.users
			.getOrNull(index)
			?.user

	private fun User.toUiModel(server: Server) =
		UserUiModel(
			user = this,
			imageUrl = authenticationRepository.getUserImageUrl(server, this),
		)
}
