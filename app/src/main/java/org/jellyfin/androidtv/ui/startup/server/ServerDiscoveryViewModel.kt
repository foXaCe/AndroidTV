package org.jellyfin.androidtv.ui.startup.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.auth.store.AuthenticationStore
import timber.log.Timber

data class ServerDiscoveryUiState(
	val isScanning: Boolean = true,
	val servers: List<DiscoveredServer> = emptyList(),
	val selectedServer: DiscoveredServer? = null,
	val error: String? = null,
	val quickConnectAvailable: Boolean = false,
	val showTimeoutHint: Boolean = false,
	val showManualDialog: Boolean = false,
	val manualCheckInProgress: Boolean = false,
	val isCheckingManual: Boolean = false,
	val manualError: String? = null,
	val noServerDetected: Boolean = false,
)

class ServerDiscoveryViewModel(
	private val discoveryRepository: ServerDiscoveryRepository,
	private val authStore: AuthenticationStore,
) : ViewModel() {
	private val _uiState = MutableStateFlow(ServerDiscoveryUiState())
	val uiState: StateFlow<ServerDiscoveryUiState> = _uiState.asStateFlow()

	private var timeoutJob: Job? = null

	val lastKnownAddress: String?
		get() =
			authStore
				.getServers()
				.values
				.maxByOrNull { it.lastUsed }
				?.address

	init {
		startDiscovery()
	}

	fun startDiscovery() {
		timeoutJob?.cancel()

		viewModelScope.launch {
			_uiState.update {
				it.copy(
					isScanning = true,
					servers = emptyList(),
					error = null,
					showTimeoutHint = false,
					noServerDetected = false,
				)
			}

			// Phase 1 : UDP broadcast scan
			discoveryRepository
				.discoverServers()
				.onEach { server ->
					_uiState.update { s -> s.copy(servers = s.servers + server) }
					// Ping en parallèle (pour RTT + version)
					launch {
						val pinged = discoveryRepository.pingServer(server)
						_uiState.update { s ->
							s.copy(servers = s.servers.map { if (it.id == pinged.id) pinged else it })
						}
					}
				}.catch { e ->
					Timber.e(e, "UDP scan error")
				}.collect {}

			// Phase 1.5 : Known servers from history
			if (_uiState.value.servers.isEmpty()) {
				val knownAddresses = authStore.getServers().values.map { it.address }
				if (knownAddresses.isNotEmpty()) {
					discoveryRepository
						.probeKnownServers(knownAddresses)
						.onEach { server ->
							_uiState.update { s -> s.copy(servers = s.servers + server) }
						}.catch { e ->
							Timber.e(e, "Known servers probe error")
						}.collect {}
				}
			}

			_uiState.update { it.copy(isScanning = false) }
		}

		// Timeout hint après 8 secondes sans résultat
		timeoutJob =
			viewModelScope.launch {
				delay(8_000)
				if (_uiState.value.servers.isEmpty()) {
					_uiState.update { it.copy(showTimeoutHint = true, noServerDetected = true) }
				}
			}
	}

	fun selectServerAndNavigate(
		server: DiscoveredServer,
		onResult: (Boolean) -> Unit,
	) {
		_uiState.update { it.copy(selectedServer = server) }
		viewModelScope.launch {
			val qcAvailable = discoveryRepository.isQuickConnectEnabled(server.address)
			_uiState.update { it.copy(quickConnectAvailable = qcAvailable) }
			onResult(qcAvailable)
		}
	}

	fun showManualDialog() {
		_uiState.update { it.copy(showManualDialog = true) }
	}

	fun dismissManualDialog() {
		_uiState.update { it.copy(showManualDialog = false) }
	}

	fun checkManualAddress(
		address: String,
		onResult: (DiscoveredServer, Boolean) -> Unit,
	) {
		_uiState.update { it.copy(showManualDialog = false, manualCheckInProgress = true) }

		viewModelScope.launch {
			// Normaliser l'adresse
			val normalized =
				buildString {
					val trimmed = address.trim()
					if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
						append("http://")
					}
					append(trimmed)
					// Ajouter port 8096 si absent
					val afterScheme = trimmed.removePrefix("http://").removePrefix("https://")
					if (!afterScheme.contains(":")) {
						append(":8096")
					}
				}

			val stub = DiscoveredServer(id = "manual", name = normalized, address = normalized)
			val server = discoveryRepository.pingServer(stub)
			val qcAvailable =
				if (server.isReachable) {
					discoveryRepository.isQuickConnectEnabled(normalized)
				} else {
					false
				}

			_uiState.update { it.copy(manualCheckInProgress = false) }
			onResult(
				if (server.isReachable) server else stub,
				qcAvailable,
			)
		}
	}

	fun probeManualAddress(
		address: String,
		callback: (DiscoveredServer, Boolean) -> Unit,
	) {
		viewModelScope.launch {
			_uiState.update { it.copy(isCheckingManual = true, manualError = null) }

			// Normaliser l'adresse : ajouter http:// si absent, :8096 si pas de port
			val normalized =
				buildString {
					val trimmed = address.trim()
					if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
						append("http://")
					}
					append(trimmed)
					val afterScheme = trimmed.removePrefix("http://").removePrefix("https://")
					if (!afterScheme.contains(":")) {
						append(":8096")
					}
				}
			val stub = DiscoveredServer(id = "manual", name = normalized, address = normalized)
			val server = discoveryRepository.pingServer(stub)

			if (!server.isReachable) {
				_uiState.update { it.copy(isCheckingManual = false, manualError = address) }
				return@launch
			}

			val qcAvailable = discoveryRepository.isQuickConnectEnabled(server.address)
			_uiState.update { it.copy(isCheckingManual = false) }
			callback(server, qcAvailable)
		}
	}
}
