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
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.startup.fragment.ServerAddFragment
import org.jellyfin.androidtv.ui.startup.fragment.StartupToolbarFragment

class ServerDiscoveryFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				ScreenIdOverlay(ScreenIds.SERVER_DISCOVERY_ID, ScreenIds.SERVER_DISCOVERY_NAME) {
					ServerDiscoveryScreen(
						onServerSelected = { server, quickConnect ->
							if (quickConnect) {
								parentFragmentManager.commit {
									replace<QuickConnectFragment>(
										R.id.content_view,
										null,
										QuickConnectFragment.buildArgs(server),
									)
									addToBackStack(null)
								}
							} else {
								parentFragmentManager.commit {
									replace<ServerAddFragment>(
										R.id.content_view,
										null,
										bundleOf(
											ServerAddFragment.ARG_SERVER_ADDRESS to server.address,
										),
									)
									replace<StartupToolbarFragment>(R.id.toolbar_view)
									addToBackStack(null)
								}
							}
						},
						onManualEntry = {
							parentFragmentManager.commit {
								replace<ServerAddFragment>(R.id.content_view)
								replace<StartupToolbarFragment>(R.id.toolbar_view)
								addToBackStack(null)
							}
						},
					)
				}
			}
		}
	}
}
