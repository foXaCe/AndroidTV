package org.jellyfin.androidtv.ui.home.compose

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
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeComposeFragment : Fragment() {
	private val homeViewModel: HomeViewModel by viewModel()
	private val itemLauncher: ItemLauncher by inject()
	private val playbackLauncher: PlaybackLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.HOME_ID, ScreenIds.HOME_NAME) {
						HomeScreen(
							viewModel = homeViewModel,
							onItemClick = ::launchItem,
							onPlayClick = ::playItem,
						)
					}
				}
			}
		}

	private fun launchItem(item: BaseItemDto) {
		// Invalidate cache — playback will change resume/progress data
		homeViewModel.invalidateCache()
		itemLauncher.launch(BaseItemDtoBaseRowItem(item), requireContext())
	}

	private fun playItem(item: BaseItemDto) {
		// Resume playback — pass saved position (ticks → ms), or null for start
		homeViewModel.invalidateCache()
		val resumeMs =
			item.userData?.playbackPositionTicks?.let { ticks ->
				if (ticks > 0) (ticks / 10_000).toInt() else null
			}
		playbackLauncher.launch(requireContext(), listOf(item), resumeMs)
	}
}
