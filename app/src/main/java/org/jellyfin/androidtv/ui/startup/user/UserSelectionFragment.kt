package org.jellyfin.androidtv.ui.startup.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.model.ApiClientErrorLoginState
import org.jellyfin.androidtv.auth.model.AuthenticatedState
import org.jellyfin.androidtv.auth.model.AuthenticatingState
import org.jellyfin.androidtv.auth.model.AutomaticAuthenticateMethod
import org.jellyfin.androidtv.auth.model.RequireSignInState
import org.jellyfin.androidtv.auth.model.Server
import org.jellyfin.androidtv.auth.model.ServerUnavailableState
import org.jellyfin.androidtv.auth.model.ServerVersionNotSupported
import org.jellyfin.androidtv.auth.model.User
import org.jellyfin.androidtv.auth.repository.AuthenticationRepository
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.startup.server.ServerDiscoveryFragment
import org.jellyfin.androidtv.util.PinCodeUtil
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserSelectionFragment : Fragment() {
	companion object {
		const val ARG_SERVER_ID = "server_id"
	}

	private val userSelectionViewModel: UserSelectionViewModel by viewModel()
	private val authenticationRepository: AuthenticationRepository by inject()
	private val backgroundService: BackgroundService by inject()

	// Compose state for PIN entry
	private var pinEntryState by mutableStateOf(PinEntryState())
	private var pinEntryUser: User? = null
	private var pinEntryServer: Server? = null

	private val serverIdArgument get() = arguments?.getString(ARG_SERVER_ID)?.ifBlank { null }?.toUUIDOrNull()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): ComposeView? {
		val serverId =
			serverIdArgument ?: run {
				navigateToSelectServer()
				return null
			}

		val server =
			userSelectionViewModel.getServer(serverId) ?: run {
				navigateToSelectServer()
				return null
			}

		userSelectionViewModel.loadUsers(server)

		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					val state by userSelectionViewModel.uiState.collectAsState()

					ScreenIdOverlay(ScreenIds.USER_SELECT_ID, ScreenIds.USER_SELECT_NAME) {
						UserSelectionScreen(
							serverName = state.serverName,
							users = state.users.map { UserInfo(name = it.user.name, imageUrl = it.imageUrl) },
							isLoading = state.isLoading,
							onUserSelected = { index ->
								val userModel = userSelectionViewModel.getUserAtIndex(index) ?: return@UserSelectionScreen
								if (PinCodeUtil.isPinEnabled(requireContext(), userModel.id)) {
									pinEntryUser = userModel
									pinEntryServer = server
									pinEntryState =
										PinEntryState(
											userName = userModel.name,
											error = null,
											visible = true,
										)
								} else {
									authenticateUser(server, userModel)
								}
							},
							onAddAccount = {
								navigateToLogin(server, username = null)
							},
							onChangeServer = {
								navigateToSelectServer()
							},
							pinEntryState = pinEntryState,
							onPinEntered = { pin -> verifyPin(pin) },
							onPinCancel = { dismissPinEntry() },
							onPinForgot = {
								val user = pinEntryUser
								dismissPinEntry()
								if (user != null) navigateToLogin(server, user.name)
							},
						)
					}
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()
		backgroundService.clearBackgrounds()

		// Hide startup toolbar — UserSelectionScreen has its own header
		activity?.findViewById<View>(R.id.toolbar_view)?.visibility = View.GONE

		val server = serverIdArgument?.let(userSelectionViewModel::getServer)
		if (server != null) {
			userSelectionViewModel.loadUsers(server)
		} else {
			navigateToSelectServer()
		}
	}

	override fun onPause() {
		super.onPause()
		// Restore toolbar visibility for other startup screens
		activity?.findViewById<View>(R.id.toolbar_view)?.visibility = View.VISIBLE
	}

	private fun verifyPin(pin: String) {
		val user = pinEntryUser ?: return
		val server = pinEntryServer ?: return
		val userPrefs =
			org.jellyfin.androidtv.preference
				.UserSettingPreferences(requireContext(), user.id)
		val storedHash = userPrefs[org.jellyfin.androidtv.preference.UserSettingPreferences.userPinHash]

		if (PinCodeUtil.hashPin(pin) == storedHash) {
			dismissPinEntry()
			authenticateUser(server, user)
		} else {
			pinEntryState =
				pinEntryState.copy(
					error = getString(R.string.lbl_pin_code_incorrect),
				)
		}
	}

	private fun dismissPinEntry() {
		pinEntryState = PinEntryState()
		pinEntryUser = null
		pinEntryServer = null
	}

	private fun authenticateUser(
		server: Server,
		user: User,
	) {
		authenticationRepository
			.authenticate(server, AutomaticAuthenticateMethod(user))
			.onEach { state ->
				when (state) {
					AuthenticatingState -> Unit
					AuthenticatedState -> Unit
					RequireSignInState -> navigateToLogin(server, user.name)
					ServerUnavailableState,
					is ApiClientErrorLoginState,
					-> Toast.makeText(context, R.string.server_connection_failed, Toast.LENGTH_LONG).show()
					is ServerVersionNotSupported ->
						Toast
							.makeText(
								context,
								getString(
									R.string.server_issue_outdated_version,
									state.server.version,
									ServerRepository.recommendedServerVersion.toString(),
								),
								Toast.LENGTH_LONG,
							).show()
				}
			}.launchIn(lifecycleScope)
	}

	private fun navigateToLogin(
		server: Server,
		username: String?,
	) {
		requireActivity().supportFragmentManager.commit {
			replace<UserLoginFragment>(
				R.id.content_view,
				null,
				bundleOf(
					UserLoginFragment.ARG_SERVER_ID to server.id.toString(),
					UserLoginFragment.ARG_USERNAME to username,
				),
			)
			addToBackStack(null)
		}
	}

	private fun navigateToSelectServer() {
		requireActivity().supportFragmentManager.commit {
			replace<ServerDiscoveryFragment>(R.id.content_view)
		}
	}
}
