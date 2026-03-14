package org.jellyfin.androidtv.ui.browsing.v2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.livetv.compose.LiveTvBrowseScreen
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.koin.android.ext.android.inject

class LiveTvBrowseFragment : Fragment() {
	data class Args(
		val folderJson: String,
	) {
		fun toBundle() =
			bundleOf(
				KEY_FOLDER to folderJson,
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val folderJson = bundle?.getString(KEY_FOLDER) ?: return null
				return Args(folderJson = folderJson)
			}
		}
	}

	companion object {
		internal const val KEY_FOLDER = "folder"
	}

	private val navigationRepository: NavigationRepository by inject()
	private val userRepository: UserRepository by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setContent {
				JellyfinTheme {
					val canManage =
						userRepository.currentUser.value
							?.policy
							?.enableLiveTvManagement == true

					ScreenIdOverlay(
						ScreenIds.LIVE_TV_BROWSE_ID,
						ScreenIds.LIVE_TV_BROWSE_NAME,
					) {
						LiveTvBrowseScreen(
							canManageRecordings = canManage,
							onNavigateGuide = {
								navigationRepository.navigate(Destinations.liveTvGuide)
							},
							onNavigateRecordings = {
								navigationRepository.navigate(Destinations.liveTvRecordings)
							},
							onNavigateSchedule = {
								navigationRepository.navigate(Destinations.liveTvSchedule)
							},
							onNavigateSeriesRecordings = {
								navigationRepository.navigate(Destinations.liveTvSeriesRecordings)
							},
						)
					}
				}
			}
		}
}
