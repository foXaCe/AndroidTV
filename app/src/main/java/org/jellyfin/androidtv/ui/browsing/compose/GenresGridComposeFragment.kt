package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.browsing.genre.JellyfinGenreItem
import org.jellyfin.androidtv.ui.browsing.v2.GenresGridViewModel
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GenresGridComposeFragment : Fragment() {
	data class Args(
		val folderJson: String? = null,
		val includeType: String? = null,
	) {
		fun toBundle() =
			bundleOf(
				KEY_FOLDER to folderJson,
				KEY_INCLUDE_TYPE to includeType,
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args =
				Args(
					folderJson = bundle?.getString(KEY_FOLDER),
					includeType = bundle?.getString(KEY_INCLUDE_TYPE),
				)
		}
	}

	companion object {
		internal const val KEY_FOLDER = "folder"
		internal const val KEY_INCLUDE_TYPE = "type_include"
	}

	private val viewModel: GenresGridViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()

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
					ScreenIdOverlay(ScreenIds.GENRES_GRID_ID, ScreenIds.GENRES_GRID_NAME) {
						GenresGridScreen(
							viewModel = viewModel,
							showLibraryFilter = folder == null,
							onGenreClick = ::onGenreClicked,
							onGenreFocus = ::onGenreFocused,
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

		val args = Args.fromBundle(arguments)
		folder =
			args.folderJson?.let {
				try {
					Json.decodeFromString(BaseItemDto.serializer(), it)
				} catch (_: Exception) {
					null
				}
			}
		viewModel.initialize(folder, args.includeType)
	}

	private fun onGenreClicked(genre: JellyfinGenreItem) {
		navigationRepository.navigate(
			Destinations.genreBrowse(
				genreName = genre.name,
				parentId = genre.parentId,
				includeType = viewModel.includeType,
				serverId = genre.serverId,
				displayPreferencesId = folder?.displayPreferencesId,
				parentItemId = folder?.id,
			),
		)
	}

	private fun onGenreFocused(genre: JellyfinGenreItem) {
		viewModel.setFocusedGenre(genre)
		genre.backdropUrl?.let { url ->
			backgroundService.setBackgroundUrl(url, BlurContext.BROWSING)
		}
	}
}
