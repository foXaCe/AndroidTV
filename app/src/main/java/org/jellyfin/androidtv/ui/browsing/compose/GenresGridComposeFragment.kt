package org.jellyfin.androidtv.ui.browsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.browsing.genre.JellyfinGenreItem
import org.jellyfin.androidtv.ui.browsing.v2.GenresGridViewModel
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GenresGridComposeFragment : Fragment() {

	private val viewModel: GenresGridViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val backgroundService: BackgroundService by inject()

	private var folder: BaseItemDto? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			JellyfinTheme {
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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val folderJson = arguments?.getString(Extras.Folder)
		folder = folderJson?.let {
			try {
				Json.decodeFromString(BaseItemDto.serializer(), it)
			} catch (_: Exception) {
				null
			}
		}
		val includeType = arguments?.getString(Extras.IncludeType)
		viewModel.initialize(folder, includeType)
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
			)
		)
	}

	private fun onGenreFocused(genre: JellyfinGenreItem) {
		viewModel.setFocusedGenre(genre)
		genre.backdropUrl?.let { url ->
			backgroundService.setBackgroundUrl(url, BlurContext.BROWSING)
		}
	}
}
