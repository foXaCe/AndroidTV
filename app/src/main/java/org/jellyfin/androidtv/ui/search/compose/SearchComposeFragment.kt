package org.jellyfin.androidtv.ui.search.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.search.SearchViewModel
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.androidtv.ui.shared.toolbar.NavigationLayout
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class SearchComposeFragment : Fragment() {
	companion object {
		const val EXTRA_QUERY = "query"
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			val viewModel = koinViewModel<SearchViewModel>()
			val api = koinInject<ApiClient>()
			val backgroundService = koinInject<BackgroundService>()
			val itemLauncher = koinInject<ItemLauncher>()

			NavigationLayout(MainToolbarActiveButton.Search) {
				SearchScreen(
					viewModel = viewModel,
					api = api,
					initialQuery = arguments?.getString(EXTRA_QUERY),
					onItemClick = { item ->
						itemLauncher.launch(BaseItemDtoBaseRowItem(item), requireContext())
					},
					onItemFocus = { item ->
						if (item != null) {
							backgroundService.setBackground(item, BlurContext.BROWSING)
						} else {
							backgroundService.clearBackgrounds()
						}
					},
				)
			}
		}
	}
}
