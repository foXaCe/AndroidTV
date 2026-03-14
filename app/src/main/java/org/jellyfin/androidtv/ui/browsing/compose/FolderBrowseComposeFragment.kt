package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.browsing.v2.FolderBrowseViewModel
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class FolderBrowseComposeFragment : Fragment() {
	data class Args(
		val folderJson: String,
		val serverId: UUID? = null,
		val userId: UUID? = null,
	) {
		fun toBundle() =
			bundleOf(
				KEY_FOLDER to folderJson,
				KEY_SERVER_ID to serverId?.toString(),
				KEY_USER_ID to userId?.toString(),
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val folderJson = bundle?.getString(KEY_FOLDER) ?: return null
				return Args(
					folderJson = folderJson,
					serverId = bundle.getString(KEY_SERVER_ID)?.let(UUID::fromString),
					userId = bundle.getString(KEY_USER_ID)?.let(UUID::fromString),
				)
			}
		}
	}

	companion object {
		internal const val KEY_FOLDER = "folder"
		internal const val KEY_SERVER_ID = "ServerId"
		internal const val KEY_USER_ID = "UserId"
	}

	private val viewModel: FolderBrowseViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()
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
					ScreenIdOverlay(ScreenIds.FOLDER_BROWSE_ID, ScreenIds.FOLDER_BROWSE_NAME) {
						FolderBrowseScreen(
							viewModel = viewModel,
							backgroundService = backgroundService,
							onItemClick = ::launchItem,
							onItemFocus = ::onItemFocused,
							onHomeClick = { navigationRepository.navigate(Destinations.home) },
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

		val args = Args.fromBundle(arguments) ?: return
		viewModel.initialize(args.folderJson, args.serverId, args.userId)
	}

	private fun launchItem(item: BaseItemDto) {
		val rowItem = BaseItemDtoBaseRowItem(item)
		itemLauncher.launch(rowItem, requireContext())
	}

	private fun onItemFocused(item: BaseItemDto) {
		viewModel.setFocusedItem(item)
		backgroundService.setBackground(item, BlurContext.BROWSING)
	}
}
