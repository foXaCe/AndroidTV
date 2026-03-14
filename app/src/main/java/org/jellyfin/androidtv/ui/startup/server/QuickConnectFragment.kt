package org.jellyfin.androidtv.ui.startup.server

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import org.jellyfin.androidtv.auth.model.ConnectedState
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.startup.StartupViewModel
import org.jellyfin.androidtv.ui.startup.user.UserSelectionFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class QuickConnectFragment : Fragment() {
	companion object {
		private const val ARG_SERVER_ID = "server_id"
		private const val ARG_SERVER_NAME = "server_name"
		private const val ARG_SERVER_ADDRESS = "server_address"
		private const val ARG_SERVER_VERSION = "server_version"
		private const val ARG_SERVER_PING = "server_ping"

		fun buildArgs(server: DiscoveredServer) =
			bundleOf(
				ARG_SERVER_ID to server.id,
				ARG_SERVER_NAME to server.name,
				ARG_SERVER_ADDRESS to server.address,
				ARG_SERVER_VERSION to server.version,
				ARG_SERVER_PING to server.pingMs,
			)
	}

	private val server: DiscoveredServer by lazy {
		DiscoveredServer(
			id = requireArguments().getString(ARG_SERVER_ID, ""),
			name = requireArguments().getString(ARG_SERVER_NAME, ""),
			address = requireArguments().getString(ARG_SERVER_ADDRESS, ""),
			version = requireArguments().getString(ARG_SERVER_VERSION, ""),
			pingMs = requireArguments().getLong(ARG_SERVER_PING, -1),
		)
	}

	private val startupViewModel: StartupViewModel by activityViewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				ScreenIdOverlay(ScreenIds.QUICK_CONNECT_ID, ScreenIds.QUICK_CONNECT_NAME) {
					QuickConnectScreen(
						server = server,
						onAuthenticated = { srv, _ ->
							// Add the server to the auth store, then navigate to user selection
							startupViewModel
								.addServer(srv.address)
								.onEach { state ->
									if (state is ConnectedState) {
										parentFragmentManager.commit {
											replace<UserSelectionFragment>(
												R.id.content_view,
												null,
												bundleOf(
													UserSelectionFragment.ARG_SERVER_ID to state.id.toString(),
												),
											)
										}
									}
								}.launchIn(lifecycleScope)
						},
						onBack = { parentFragmentManager.popBackStack() },
					)
				}
			}
		}
	}
}
