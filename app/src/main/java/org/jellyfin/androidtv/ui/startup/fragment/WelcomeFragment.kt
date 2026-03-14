package org.jellyfin.androidtv.ui.startup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.startup.compose.WelcomeScreen
import org.jellyfin.androidtv.ui.startup.server.ServerDiscoveryFragment

class WelcomeFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				ScreenIdOverlay(ScreenIds.WELCOME_ID, ScreenIds.WELCOME_NAME) {
					WelcomeScreen(
						onConnectClick = {
							parentFragmentManager.commit {
								replace<ServerDiscoveryFragment>(R.id.content_view)
								addToBackStack(null)
							}
						},
					)
				}
			}
		}
	}
}
