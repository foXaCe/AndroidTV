package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.constant.JellyseerrFetchLimit
import org.jellyfin.androidtv.databinding.FragmentJellyseerrSettingsBinding
import org.jellyfin.androidtv.preference.JellyseerrPreferences
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.BaseFragment
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import timber.log.Timber

class SettingsFragment : BaseFragment(R.layout.fragment_jellyseerr_settings) {
	private val viewModel: JellyseerrAuthViewModel by viewModel()
	private val globalPreferences: JellyseerrPreferences by inject(named("global"))
	private val apiClient: ApiClient by inject()
	private val userPreferences: UserPreferences by inject()
	private val userRepository: UserRepository by inject()

	private var _binding: FragmentJellyseerrSettingsBinding? = null
	private val binding get() = _binding!!
	
	// Per-user Jellyseerr preferences (lazy initialized with current user ID)
	private val userJellyseerrPrefs: JellyseerrPreferences by lazy {
		val userId = userRepository.currentUser.value?.id?.toString()
			?: throw IllegalStateException("No user logged in")
		JellyseerrPreferences(requireContext(), userId)
	}

	override fun setupUI(view: View, savedInstanceState: Bundle?) {
		_binding = FragmentJellyseerrSettingsBinding.bind(view)

		binding.connectJellyfinButton.setOnClickListener {
			connectWithJellyfin()
		}

		binding.testConnectionButton.setOnClickListener {
			testConnection()
		}

		// Enable/disable toggle
		binding.enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
			globalPreferences[JellyseerrPreferences.enabled] = isChecked
			binding.settingsGroup.alpha = if (isChecked) 1f else 0.5f
		}

		// Setup fetch limit spinner
		setupFetchLimitSpinner()
	}

	private fun setupFetchLimitSpinner() {
		val fetchLimitOptions = JellyseerrFetchLimit.values()
		val displayNames = fetchLimitOptions.map { getString(it.nameRes) }
		
		val adapter = ArrayAdapter(
			requireContext(),
			android.R.layout.simple_spinner_item,
			displayNames
		).apply {
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		}
		
		binding.fetchLimitSpinner.adapter = adapter
		
		// Set initial selection
		val currentLimit = globalPreferences[JellyseerrPreferences.fetchLimit]
		val currentIndex = fetchLimitOptions.indexOf(currentLimit)
		if (currentIndex >= 0) {
			binding.fetchLimitSpinner.setSelection(currentIndex)
		}
		
		// Handle selection changes
		binding.fetchLimitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val selectedLimit = fetchLimitOptions[position]
				globalPreferences[JellyseerrPreferences.fetchLimit] = selectedLimit
				Timber.d("Fetch limit changed to: ${selectedLimit.limit} items")
			}
			
			override fun onNothingSelected(parent: AdapterView<*>?) {
				// Do nothing
			}
		}
	}

	override fun setupObservers() {
		// Monitor connection state
		collectFlow(viewModel.isAvailable) { isAvailable ->
			updateConnectionStatus(isAvailable)
		}

		// Monitor loading state for user feedback
		collectFlow(viewModel.loadingState) { state ->
			when (state) {
				is JellyseerrLoadingState.Loading -> {
					binding.testConnectionButton.isEnabled = false
					binding.statusText.text = getString(R.string.jellyseerr_testing_connection)
				}
				is JellyseerrLoadingState.Success -> {
					binding.testConnectionButton.isEnabled = true
					binding.statusText.text = "✓ ${getString(R.string.jellyseerr_connected_successfully)}"
					binding.statusIcon.setImageResource(R.drawable.ic_check)
					showSuccess(getString(R.string.jellyseerr_connected_successfully))
				}
				is JellyseerrLoadingState.Error -> {
					binding.testConnectionButton.isEnabled = true
					binding.statusText.text = "✗ ${getString(R.string.jellyseerr_connection_failed, state.message)}"
					showError(getString(R.string.jellyseerr_connection_error_detail, state.message))
				}
				is JellyseerrLoadingState.Idle -> {
					binding.testConnectionButton.isEnabled = true
					binding.statusText.text = getString(R.string.jellyseerr_not_tested)
				}
			}
		}
		
		loadSavedSettings()
	}

	private fun loadSavedSettings() {
		// Load saved URL
		val savedUrl = globalPreferences[JellyseerrPreferences.serverUrl]
		val isEnabled = globalPreferences[JellyseerrPreferences.enabled]

		binding.serverUrlInput.setText(savedUrl)
		binding.enabledSwitch.isChecked = isEnabled

		// Update UI based on enabled state
		binding.settingsGroup.alpha = if (isEnabled) 1f else 0.5f

		// Show last connection status
		if (savedUrl.isNotBlank()) {
			val wasSuccessful = globalPreferences[JellyseerrPreferences.lastConnectionSuccess]
			if (wasSuccessful) {
				binding.statusText.text = "✓ ${getString(R.string.jellyseerr_connected)}"
				binding.statusIcon.setImageResource(R.drawable.ic_check)
			}
		}
	}

	private fun testConnection() {
		val serverUrl = globalPreferences[JellyseerrPreferences.serverUrl]

		// Validate that connection was set up
		if (serverUrl.isNullOrEmpty()) {
			showError(getString(R.string.jellyseerr_please_connect_jellyfin))
			return
		}

		// Test connection via ViewModel (using cookie auth)
		viewModel.initializeJellyseerr(serverUrl, "")

		Timber.d("Testing Jellyseerr connection to: $serverUrl")
	}

	private fun connectWithJellyfin() {
		val jellyseerrServerUrl = binding.serverUrlInput.text.toString().trim()
		
		if (jellyseerrServerUrl.isEmpty()) {
			showError(getString(R.string.jellyseerr_enter_server_url))
			return
		}

		// Get the Jellyfin server URL from current connection
		val jellyfinServerUrl = apiClient.baseUrl ?: run {
			showError(getString(R.string.jellyseerr_cannot_determine_server))
			return
		}

		// Get the current logged-in user's username
		val currentUser = userRepository.currentUser.value
		val username = currentUser?.name ?: run {
			showError(getString(R.string.jellyseerr_cannot_determine_user))
			return
		}

		// Prompt only for password (username is pre-filled from current session)
		val passwordInput = android.widget.EditText(requireContext()).apply {
			hint = "Enter your Jellyfin password"
			inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
			setPadding(48, 0, 48, 0)
		}

		android.app.AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.jellyseerr_login_with_jellyfin))
			.setMessage(getString(R.string.jellyseerr_connecting_as, username))
			.setView(passwordInput)
			.setPositiveButton(getString(R.string.btn_connect)) { _, _ ->
				val password = passwordInput.text.toString().trim()

				if (password.isEmpty()) {
					showError(getString(R.string.jellyseerr_password_required))
					return@setPositiveButton
				}

				performJellyfinLogin(jellyseerrServerUrl, username, password, jellyfinServerUrl)
			}
			.setNegativeButton(getString(R.string.btn_cancel), null)
			.show()
	}

	private fun performJellyfinLogin(
		jellyseerrServerUrl: String,
		username: String,
		password: String,
		jellyfinServerUrl: String
	) {
		binding.connectJellyfinButton.isEnabled = false
		binding.statusText.text = getString(R.string.jellyseerr_connecting)

		lifecycleScope.launch {
			try {
				val result = viewModel.loginWithJellyfin(username, password, jellyfinServerUrl, jellyseerrServerUrl)
				
				result.onSuccess { user ->
					// Save credentials (using cookie-based auth)
					globalPreferences[JellyseerrPreferences.serverUrl] = jellyseerrServerUrl
					globalPreferences[JellyseerrPreferences.enabled] = true
					binding.enabledSwitch.isChecked = true
					
				// Initialize connection (using cookie-based auth)
				viewModel.initializeJellyseerr(jellyseerrServerUrl, "")
				
				showSuccess(getString(R.string.jellyseerr_connected_session_cookie))
				
				Timber.d("Jellyseerr: Jellyfin authentication successful using cookie authentication")
			}.onFailure { error ->
				showError(getString(R.string.jellyseerr_connection_failed, error.message ?: ""), error)
					binding.statusText.text = "✗ ${getString(R.string.jellyseerr_connection_failed, "")}"
					Timber.e(error, "Jellyseerr: Jellyfin authentication failed")
				}
			} finally {
				binding.connectJellyfinButton.isEnabled = true
			}
		}
	}



	private fun updateConnectionStatus(isAvailable: Boolean) {
		if (isAvailable) {
			// Update preferences to record successful connection
			globalPreferences[JellyseerrPreferences.lastConnectionSuccess] = true
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
