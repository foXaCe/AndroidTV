package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SuggestedMoviesComposeFragment : Fragment() {
	data class Args(
		val folderJson: String,
	) {
		fun toBundle() =
			bundleOf(
				KEY_FOLDER to folderJson,
			)

		companion object {
			fun fromBundle(bundle: Bundle): Args =
				Args(
					folderJson = requireNotNull(bundle.getString(KEY_FOLDER)) { "Missing folder JSON" },
				)
		}
	}

	companion object {
		internal const val KEY_FOLDER = "folder"
	}

	private val viewModel: SuggestedMoviesViewModel by viewModel()
	private val itemLauncher: ItemLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.SUGGESTED_MOVIES_ID, ScreenIds.SUGGESTED_MOVIES_NAME) {
						SuggestedMoviesScreen(
							viewModel = viewModel,
							onItemClick = ::launchItem,
						)
					}
				}
			}
		}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		val args = Args.fromBundle(requireArguments())
		viewModel.initialize(
			folderJson = args.folderJson,
			becauseYouWatchedTemplate = getString(R.string.because_you_watched),
		)
	}

	private fun launchItem(item: BaseItemDto) {
		itemLauncher.launch(BaseItemDtoBaseRowItem(item), requireContext())
	}
}
