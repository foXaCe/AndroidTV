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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
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
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class LibraryBrowseComposeFragment : Fragment() {
	sealed class Args {
		abstract fun toBundle(): Bundle

		data class GenreArgs(
			val genreName: String,
			val parentId: UUID? = null,
			val includeType: String? = null,
			val serverId: UUID? = null,
			val userId: UUID? = null,
			val displayPrefsId: String? = null,
			val parentItemId: UUID? = null,
		) : Args() {
			override fun toBundle() =
				bundleOf(
					ARG_GENRE_NAME to genreName,
					ARG_PARENT_ID to parentId?.toString(),
					ARG_INCLUDE_TYPE to includeType,
					ARG_SERVER_ID to serverId?.toString(),
					KEY_USER_ID to userId?.toString(),
					ARG_DISPLAY_PREFS_ID to displayPrefsId,
					ARG_PARENT_ITEM_ID to parentItemId?.toString(),
				)
		}

		data class LibraryArgs(
			val folderJson: String,
			val serverId: UUID? = null,
			val userId: UUID? = null,
			val includeType: String? = null,
		) : Args() {
			override fun toBundle() =
				bundleOf(
					KEY_FOLDER to folderJson,
					KEY_LIBRARY_SERVER_ID to serverId?.toString(),
					KEY_USER_ID to userId?.toString(),
					KEY_INCLUDE_TYPE_LIBRARY to includeType,
				)
		}

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				if (bundle == null) return null
				val genreName = bundle.getString(ARG_GENRE_NAME)
				if (genreName != null) {
					return GenreArgs(
						genreName = genreName,
						parentId = bundle.getString(ARG_PARENT_ID)?.let(UUID::fromString),
						includeType = bundle.getString(ARG_INCLUDE_TYPE),
						serverId = bundle.getString(ARG_SERVER_ID)?.let(UUID::fromString),
						userId = bundle.getString(KEY_USER_ID)?.let(UUID::fromString),
						displayPrefsId = bundle.getString(ARG_DISPLAY_PREFS_ID),
						parentItemId = bundle.getString(ARG_PARENT_ITEM_ID)?.let(UUID::fromString),
					)
				}
				val folderJson = bundle.getString(KEY_FOLDER) ?: return null
				return LibraryArgs(
					folderJson = folderJson,
					serverId = bundle.getString(KEY_LIBRARY_SERVER_ID)?.let(UUID::fromString),
					userId = bundle.getString(KEY_USER_ID)?.let(UUID::fromString),
					includeType = bundle.getString(KEY_INCLUDE_TYPE_LIBRARY),
				)
			}
		}
	}

	companion object {
		internal const val ARG_GENRE_NAME = "genre_name"
		internal const val ARG_PARENT_ID = "parent_id"
		internal const val ARG_INCLUDE_TYPE = "include_type"
		internal const val ARG_SERVER_ID = "server_id"
		internal const val ARG_DISPLAY_PREFS_ID = "display_prefs_id"
		internal const val ARG_PARENT_ITEM_ID = "parent_item_id"
		private const val KEY_FOLDER = "folder"
		private const val KEY_LIBRARY_SERVER_ID = "ServerId"
		private const val KEY_USER_ID = "UserId"
		private const val KEY_INCLUDE_TYPE_LIBRARY = "type_include"
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
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ScreenIdOverlay(ScreenIds.LIBRARY_BROWSE_ID, ScreenIds.LIBRARY_BROWSE_NAME) {
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
							val displayPrefsId =
								if (uiState.isGenreMode) {
									uiState.displayPreferencesId
								} else {
									folder?.displayPreferencesId
								}
							val settingsItemId =
								if (uiState.isGenreMode) {
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
										"serverId" to (
											currentSession?.serverId?.toString()
												?: UUID(0, 0).toString()
										),
										"userId" to (
											currentSession?.userId?.toString()
												?: UUID(0, 0).toString()
										),
									),
								) {
									SettingsDialog(
										visible = settingsVisible,
										onDismissRequest = {
											settingsVisible = false
											viewModel.refreshDisplayPreferences()
										},
									) {
										SettingsRouterContent()
									}
								}
							}
						}
					}
				}
			}
		}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)

		when (val args = Args.fromBundle(arguments) ?: return) {
			is Args.GenreArgs -> {
				viewModel.initializeGenre(
					args.genreName,
					args.parentId,
					args.includeType,
					args.serverId,
					args.userId,
					args.displayPrefsId,
					args.parentItemId,
				)
			}
			is Args.LibraryArgs -> {
				folder =
					try {
						Json.decodeFromString(BaseItemDto.serializer(), args.folderJson)
					} catch (_: Exception) {
						null
					}
				if (args.includeType != null) {
					viewModel.initializeWithType(args.folderJson, args.includeType, args.serverId, args.userId)
				} else {
					viewModel.initialize(args.folderJson, args.serverId, args.userId)
				}
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
