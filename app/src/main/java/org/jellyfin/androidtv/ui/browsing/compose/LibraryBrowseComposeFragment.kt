package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.browsing.v2.LibraryBrowseViewModel
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.navigation.ProvideRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsDialog
import org.jellyfin.androidtv.ui.settings.composable.SettingsRouterContent
import org.jellyfin.androidtv.ui.settings.routes
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class LibraryBrowseComposeFragment : Fragment() {

	companion object {
		const val ARG_GENRE_NAME = "genre_name"
		const val ARG_PARENT_ID = "parent_id"
		const val ARG_INCLUDE_TYPE = "include_type"
		const val ARG_SERVER_ID = "server_id"
		const val ARG_DISPLAY_PREFS_ID = "display_prefs_id"
		const val ARG_PARENT_ITEM_ID = "parent_item_id"
	}

	private val viewModel: LibraryBrowseViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()
	private val itemLauncher: ItemLauncher by inject()
	private val sessionRepository: SessionRepository by inject()

	private var folder: BaseItemDto? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
				val uiState by viewModel.uiState.collectAsState()
				var settingsVisible by remember { mutableStateOf(false) }

				Box(modifier = Modifier.fillMaxSize()) {
					LibraryBrowseScreen(
						viewModel = viewModel,
						onItemClick = ::launchItem,
						onItemFocus = ::onItemFocused,
						onHomeClick = { navigationRepository.navigate(Destinations.home) },
						onSettingsClick = { settingsVisible = true },
					)

					// Settings overlay
					val displayPrefsId = if (uiState.isGenreMode) {
						uiState.displayPreferencesId
					} else {
						folder?.displayPreferencesId
					}
					val settingsItemId = if (uiState.isGenreMode) {
						uiState.parentItemId
					} else {
						folder?.id
					}

					if (displayPrefsId != null && settingsItemId != null) {
						val currentSession by sessionRepository.currentSession.collectAsState()
						ProvideRouter(
							routes,
							Routes.LIBRARIES_DISPLAY,
							mapOf(
								"itemId" to settingsItemId.toString(),
								"displayPreferencesId" to displayPrefsId,
								"serverId" to (currentSession?.serverId?.toString()
									?: UUID(0, 0).toString()),
								"userId" to (currentSession?.userId?.toString()
									?: UUID(0, 0).toString()),
							)
						) {
							SettingsDialog(
								visible = settingsVisible,
								onDismissRequest = {
									settingsVisible = false
									viewModel.refreshDisplayPreferences()
								}
							) {
								SettingsRouterContent()
							}
						}
					}
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val genreName = arguments?.getString(ARG_GENRE_NAME)
		if (genreName != null) {
			// Genre mode
			val parentId = Utils.uuidOrNull(arguments?.getString(ARG_PARENT_ID))
			val includeType = arguments?.getString(ARG_INCLUDE_TYPE)
			val serverId = Utils.uuidOrNull(arguments?.getString(ARG_SERVER_ID))
			val userId = Utils.uuidOrNull(arguments?.getString("UserId"))
			val displayPrefsId = arguments?.getString(ARG_DISPLAY_PREFS_ID)
			val parentItemId = Utils.uuidOrNull(arguments?.getString(ARG_PARENT_ITEM_ID))
			viewModel.initializeGenre(
				genreName, parentId, includeType, serverId, userId,
				displayPrefsId, parentItemId,
			)
		} else {
			// Library or explicit type mode
			val folderJson = arguments?.getString(Extras.Folder) ?: return
			val serverId = Utils.uuidOrNull(arguments?.getString("ServerId"))
			val userId = Utils.uuidOrNull(arguments?.getString("UserId"))
			val includeType = arguments?.getString(Extras.IncludeType)
			folder = try {
				Json.decodeFromString(BaseItemDto.serializer(), folderJson)
			} catch (_: Exception) {
				null
			}
			if (includeType != null) {
				viewModel.initializeWithType(folderJson, includeType, serverId, userId)
			} else {
				viewModel.initialize(folderJson, serverId, userId)
			}
		}
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
