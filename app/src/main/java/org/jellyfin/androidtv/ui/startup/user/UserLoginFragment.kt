package org.jellyfin.androidtv.ui.startup.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.model.ApiClientErrorLoginState
import org.jellyfin.androidtv.auth.model.AuthenticatedState
import org.jellyfin.androidtv.auth.model.AuthenticatingState
import org.jellyfin.androidtv.auth.model.RequireSignInState
import org.jellyfin.androidtv.auth.model.ServerUnavailableState
import org.jellyfin.androidtv.auth.model.ServerVersionNotSupported
import org.jellyfin.androidtv.auth.repository.ServerRepository
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.startup.UserLoginViewModel
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class UserLoginFragment : Fragment() {
	companion object {
		const val ARG_SERVER_ID = "server_id"
		const val ARG_USERNAME = "user_name"
	}

	private val userLoginViewModel: UserLoginViewModel by activityViewModel()

	private val serverIdArgument get() = arguments?.getString(ARG_SERVER_ID)?.ifBlank { null }
	private val usernameArgument get() = arguments?.getString(ARG_USERNAME)?.ifBlank { null }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		userLoginViewModel.forcedUsername = usernameArgument
		userLoginViewModel.setServer(serverIdArgument?.toUUIDOrNull())
		userLoginViewModel.clearLoginState()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				val server by userLoginViewModel.server.collectAsState()
				val loginState by userLoginViewModel.loginState.collectAsState()

				val serverName = server?.name ?: getString(R.string.app_name)

				val error =
					when (loginState) {
						AuthenticatingState -> getString(R.string.login_authenticating)
						RequireSignInState -> getString(R.string.login_invalid_credentials)
						ServerUnavailableState,
						is ApiClientErrorLoginState,
						-> getString(R.string.login_server_unavailable)
						is ServerVersionNotSupported ->
							getString(
								R.string.server_issue_outdated_version,
								(loginState as ServerVersionNotSupported).server.version,
								ServerRepository.recommendedServerVersion.toString(),
							)
						AuthenticatedState -> null
						null -> null
					}

				val isLoading = loginState == AuthenticatingState

				ScreenIdOverlay(ScreenIds.USER_LOGIN_ID, ScreenIds.USER_LOGIN_NAME) {
					UserLoginScreen(
						serverName = serverName,
						isLoading = isLoading,
						error = error,
						onLogin = { username, password ->
							userLoginViewModel.login(username, password)
						},
						onCancel = {
							parentFragmentManager.popBackStack()
						},
					)
				}
			}
		}
	}
}
