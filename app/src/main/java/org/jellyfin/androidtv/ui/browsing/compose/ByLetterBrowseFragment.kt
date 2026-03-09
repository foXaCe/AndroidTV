package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ByLetterBrowseFragment : Fragment() {

	private val viewModel: ByLetterBrowseViewModel by viewModel()
	private val itemLauncher: ItemLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				ByLetterBrowseScreen(
					viewModel = viewModel,
					onItemClick = ::launchItem,
				)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val folderJson = requireArguments().getString(Extras.Folder)!!
		val includeType = arguments?.getString(Extras.IncludeType)
		viewModel.initialize(
			folderJson = folderJson,
			includeType = includeType,
			letters = getString(R.string.byletter_letters),
		)
	}

	private fun launchItem(item: BaseItemDto) {
		itemLauncher.launch(BaseItemDtoBaseRowItem(item), requireContext())
	}
}
