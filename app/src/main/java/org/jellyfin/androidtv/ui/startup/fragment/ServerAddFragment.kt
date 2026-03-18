package org.jellyfin.androidtv.ui.startup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.startup.server.ServerAddScreen
import org.jellyfin.androidtv.ui.startup.user.UserSelectionFragment

class ServerAddFragment : Fragment() {
	companion object {
		const val ARG_SERVER_ADDRESS = "server_address"
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				ServerAddScreen(
					serverAddress = arguments?.getString(ARG_SERVER_ADDRESS)?.ifBlank { null },
					onConnected = { serverId ->
						parentFragmentManager.commit {
							replace<UserSelectionFragment>(
								R.id.content_view,
								null,
								bundleOf(
									UserSelectionFragment.ARG_SERVER_ID to serverId.toString(),
								),
							)
						}
					},
				)
			}
		}
	}
}
