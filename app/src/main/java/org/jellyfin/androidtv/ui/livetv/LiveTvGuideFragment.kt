package org.jellyfin.androidtv.ui.livetv

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
import org.jellyfin.androidtv.ui.livetv.compose.LiveTvGuideScreen
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.util.PlaybackHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LiveTvGuideFragment : Fragment() {
	companion object {
		const val GUIDE_ROW_HEIGHT_DP = 55
		const val GUIDE_ROW_WIDTH_PER_MIN_DP = 7
		const val PAGE_SIZE = 75
		const val NORMAL_HOURS = 9
		const val FILTERED_HOURS = 4
	}

	private val viewModel: LiveTvGuideViewModel by viewModel()
	private val playbackHelper: PlaybackHelper by inject()
	private val navigationRepository: NavigationRepository by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.LIVE_TV_GUIDE_ID, ScreenIds.LIVE_TV_GUIDE_NAME) {
						LiveTvGuideScreen(
							viewModel = viewModel,
							onTuneToChannel = { channelId ->
								playbackHelper.retrieveAndPlay(channelId, false, requireContext())
							},
							onDismiss = { parentFragmentManager.popBackStack() },
							onNavigateRecordings = { navigationRepository.navigate(Destinations.liveTvRecordings) },
							onNavigateSchedule = { navigationRepository.navigate(Destinations.liveTvSchedule) },
							onNavigateSeriesRecordings = { navigationRepository.navigate(Destinations.liveTvSeriesRecordings) },
						)
					}
				}
			}
		}

	override fun onResume() {
		super.onResume()
		viewModel.loadGuide()
	}
}
