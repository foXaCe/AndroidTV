package org.jellyfin.androidtv.ui.startup.server

/**
 * Serveur Jellyfin découvert sur le réseau local.
 */
data class DiscoveredServer(
	val id: String,
	val name: String,
	val address: String,
	val version: String = "",
	val pingMs: Long = -1,
) {
	val host: String
		get() =
			address
				.removePrefix("http://")
				.removePrefix("https://")
				.substringBefore(":")

	val port: Int
		get() =
			address
				.substringAfterLast(":")
				.trimEnd('/')
				.toIntOrNull() ?: 8096

	val isReachable: Boolean get() = pingMs >= 0
}

/**
 * État de la découverte de serveurs.
 */
sealed interface DiscoveryState {
	data object Scanning : DiscoveryState

	data class Found(
		val servers: List<DiscoveredServer>,
	) : DiscoveryState

	data class Error(
		val message: String,
	) : DiscoveryState
}

/**
 * État du flux QuickConnect pour l'écran Welcome.
 */
sealed interface QuickConnectFlowState {
	data object Idle : QuickConnectFlowState

	data class CodeReady(
		val code: String,
		val secret: String,
	) : QuickConnectFlowState

	data object WaitingForUser : QuickConnectFlowState

	data object Authenticated : QuickConnectFlowState

	data class Error(
		val message: String,
	) : QuickConnectFlowState
}
