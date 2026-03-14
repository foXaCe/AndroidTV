package org.jellyfin.androidtv.ui.startup.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.extensions.quickConnectApi
import org.jellyfin.sdk.api.client.extensions.systemApi
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

interface ServerDiscoveryRepository {
	/**
	 * Émet les serveurs découverts au fur et à mesure via UDP broadcast (port 7359).
	 * Le flow se termine quand le scan est fini (timeout ~3s du SDK).
	 */
	fun discoverServers(): Flow<DiscoveredServer>

	/**
	 * Mesure le ping vers un serveur et récupère sa version.
	 * Appel GET /System/Info/Public — mesure le RTT en ms.
	 * Retourne le serveur mis à jour avec pingMs et version, ou pingMs=-1 si injoignable.
	 */
	suspend fun pingServer(server: DiscoveredServer): DiscoveredServer

	/**
	 * Teste les serveurs déjà connus (historique) via GET /System/Info/Public
	 * avec un timeout de 2 secondes. Émet un DiscoveredServer pour chaque serveur qui répond.
	 */
	fun probeKnownServers(addresses: List<String>): Flow<DiscoveredServer>

	/**
	 * Vérifie si QuickConnect est activé sur le serveur.
	 */
	suspend fun isQuickConnectEnabled(address: String): Boolean
}

class ServerDiscoveryRepositoryImpl(
	private val jellyfin: Jellyfin,
) : ServerDiscoveryRepository {
	override fun discoverServers(): Flow<DiscoveredServer> =
		jellyfin.discovery
			.discoverLocalServers()
			.map { info ->
				DiscoveredServer(
					id = info.id,
					name = info.name,
					address = info.address,
				)
			}.catch { e ->
				Timber.e(e, "Discovery: scan error")
			}.flowOn(Dispatchers.IO)

	override fun probeKnownServers(addresses: List<String>): Flow<DiscoveredServer> =
		channelFlow {
			addresses
				.map { address ->
					launch(Dispatchers.IO) {
						probeKnownServer(address)?.let { server ->
							send(server)
						}
					}
				}.joinAll()
		}.flowOn(Dispatchers.IO)

	private fun probeKnownServer(address: String): DiscoveredServer? {
		val url = URL("${address.trimEnd('/')}/System/Info/Public")
		val start = System.currentTimeMillis()
		return try {
			val conn = url.openConnection() as HttpURLConnection
			conn.connectTimeout = 2000
			conn.readTimeout = 2000
			conn.requestMethod = "GET"
			try {
				if (conn.responseCode == 200) {
					val rtt = System.currentTimeMillis() - start
					val body = conn.inputStream.bufferedReader().use { it.readText() }
					val json = JSONObject(body)
					DiscoveredServer(
						id = json.optString("Id", address),
						name = json.optString("ServerName", address),
						address = address,
						version = json.optString("Version", ""),
						pingMs = rtt,
					)
				} else {
					null
				}
			} finally {
				conn.disconnect()
			}
		} catch (_: Exception) {
			null
		}
	}

	override suspend fun pingServer(server: DiscoveredServer): DiscoveredServer =
		withContext(Dispatchers.IO) {
			val start = System.currentTimeMillis()
			try {
				val api = jellyfin.createApi(baseUrl = server.address)
				val systemInfo by api.systemApi.getPublicSystemInfo()
				val rtt = System.currentTimeMillis() - start
				server.copy(
					pingMs = rtt,
					version = systemInfo.version ?: "",
				)
			} catch (e: Exception) {
				Timber.w(e, "Ping failed for %s", server.address)
				server.copy(pingMs = -1)
			}
		}

	override suspend fun isQuickConnectEnabled(address: String): Boolean =
		withContext(Dispatchers.IO) {
			try {
				val api = jellyfin.createApi(baseUrl = address)
				val result = api.quickConnectApi.getQuickConnectEnabled().content
				result
			} catch (e: Exception) {
				Timber.w(e, "QuickConnect check failed for %s", address)
				false
			}
		}
}
