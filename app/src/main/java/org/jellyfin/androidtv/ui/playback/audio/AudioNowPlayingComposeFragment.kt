package org.jellyfin.androidtv.ui.playback.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioNowPlayingComposeFragment : Fragment() {
	private val viewModel: AudioNowPlayingViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.AUDIO_PLAYER_ID, ScreenIds.AUDIO_PLAYER_NAME) {
						AudioNowPlayingScreen(viewModel = viewModel)
					}
				}
			}
		}
}
