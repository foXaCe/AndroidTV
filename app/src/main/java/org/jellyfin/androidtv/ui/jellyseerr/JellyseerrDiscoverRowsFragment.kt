package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.androidtv.ui.jellyseerr.compose.JellyseerrDiscoverRows
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import timber.log.Timber

class JellyseerrDiscoverRowsFragment : Fragment() {
	private val viewModel: JellyseerrDiscoverViewModel by viewModel()
	private val detailsViewModel: JellyseerrDetailsViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val jellyseerrPreferences: JellyseerrPreferences by inject(named("global"))

	// Flow to track selected item for display in parent fragment
	private val _selectedItemStateFlow = MutableStateFlow<JellyseerrDiscoverItemDto?>(null)
	val selectedItemStateFlow: StateFlow<JellyseerrDiscoverItemDto?> = _selectedItemStateFlow.asStateFlow()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupObservers()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setContent {
				JellyseerrDiscoverRows(
					discoverViewModel = viewModel,
					detailsViewModel = detailsViewModel,
					activeRows = jellyseerrPreferences.activeRows,
					onItemClick = { item -> onContentSelected(item) },
					onItemFocused = { item -> _selectedItemStateFlow.value = item },
					onGenreClick = { genre, mediaType -> onGenreSelected(genre, mediaType) },
					onStudioClick = { studio -> onStudioSelected(studio) },
					onNetworkClick = { network -> onNetworkSelected(network) },
				)
			}
		}

	override fun onResume() {
		super.onResume()
		if (!viewModel.hasContent()) {
			loadContent()
		}
	}

	private fun setupObservers() {
		// Retry loading when Jellyseerr becomes available
		lifecycleScope.launch {
			viewModel.isAvailable.collect { available ->
				if (available && !viewModel.hasContent()) {
					loadContent()
				}
			}
		}

		// Show errors
		lifecycleScope.launch {
			viewModel.loadingState.collect { state ->
				if (state is JellyseerrLoadingState.Error) {
					Timber.e("Jellyseerr connection error: ${state.message}")
					Toast.makeText(requireContext(), R.string.jellyseerr_connection_error, Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun loadContent() {
		viewModel.loadTrendingContent()
		viewModel.loadGenres()
		detailsViewModel.loadRequests()
	}

	private fun onContentSelected(item: JellyseerrDiscoverItemDto) {
		val itemJson = Json.encodeToString(JellyseerrDiscoverItemDto.serializer(), item)
		navigationRepository.navigate(Destinations.jellyseerrMediaDetails(itemJson))
	}

	private fun onGenreSelected(
		genre: org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto,
		mediaType: String,
	) {
		navigationRepository.navigate(Destinations.jellyseerrBrowseByGenre(genre.id, genre.name, mediaType))
	}

	private fun onStudioSelected(studio: org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrStudioDto) {
		navigationRepository.navigate(Destinations.jellyseerrBrowseByStudio(studio.id, studio.name))
	}

	private fun onNetworkSelected(network: org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrNetworkDto) {
		navigationRepository.navigate(Destinations.jellyseerrBrowseByNetwork(network.id, network.name))
	}
}
