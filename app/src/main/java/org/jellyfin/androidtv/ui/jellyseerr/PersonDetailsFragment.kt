package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrPersonDetailsDto
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.NavbarPosition
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.jellyseerr.compose.JellyseerrPersonDetailsScreen
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.shared.toolbar.LeftSidebarNavigation
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PersonDetailsFragment : Fragment() {
	private val viewModel: JellyseerrViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val userPreferences: UserPreferences by inject()

	private var personId: Int = -1
	private var personName: String = ""

	private var personDetails by mutableStateOf<JellyseerrPersonDetailsDto?>(null)
	private var appearances by mutableStateOf<List<JellyseerrDiscoverItemDto>>(emptyList())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		personId = arguments?.getString("personId")?.toIntOrNull() ?: -1
		personName = arguments?.getString("personName") ?: ""

		if (personId == -1) {
			Timber.e("PersonDetailsFragment: No person ID found in arguments")
			Toast.makeText(requireContext(), getString(R.string.person_error_not_found), Toast.LENGTH_SHORT).show()
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		return ComposeView(requireContext()).apply {
			setContent {
				JellyfinTheme {
					Box(modifier = Modifier.fillMaxSize()) {
						JellyseerrPersonDetailsScreen(
							personName = personName,
							personDetails = personDetails,
							appearances = appearances,
							onItemClick = { item ->
								val itemJson = Json.encodeToString(JellyseerrDiscoverItemDto.serializer(), item)
								navigationRepository.navigate(Destinations.jellyseerrMediaDetails(itemJson))
							},
						)

						val navbarPosition = userPreferences[UserPreferences.navbarPosition]
						when (navbarPosition) {
							NavbarPosition.LEFT -> {
								LeftSidebarNavigation(activeButton = MainToolbarActiveButton.Jellyseerr)
							}
							NavbarPosition.TOP -> {
								MainToolbar(activeButton = MainToolbarActiveButton.Jellyseerr)
							}
						}
					}
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		loadPersonData()
	}

	private fun loadPersonData() {
		lifecycleScope.launch {
			try {
				val detailsResult = viewModel.getPersonDetails(personId)
				detailsResult.onSuccess { details ->
					personDetails = details
					loadPersonCredits()
				}.onFailure { error ->
					Timber.e(error, "Failed to load person details")
					if (isAdded) {
						Toast.makeText(requireContext(), getString(R.string.person_load_failed), Toast.LENGTH_SHORT).show()
					}
				}
			} catch (e: Exception) {
				Timber.e(e, "Error loading person data")
			}
		}
	}

	private fun loadPersonCredits() {
		lifecycleScope.launch {
			try {
				val creditsResult = viewModel.getPersonCombinedCredits(personId)
				creditsResult.onSuccess { credits ->
					appearances = credits.cast
						.filter { it.posterPath != null }
						.sortedBy { it.title ?: it.name ?: "" }
				}.onFailure { error ->
					Timber.e(error, "Failed to load person credits")
				}
			} catch (e: Exception) {
				Timber.e(e, "Error loading person credits")
			}
		}
	}
}
