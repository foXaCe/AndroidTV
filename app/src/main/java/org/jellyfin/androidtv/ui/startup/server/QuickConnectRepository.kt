package org.jellyfin.androidtv.ui.startup.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.extensions.authenticateWithQuickConnect
import org.jellyfin.sdk.api.client.extensions.quickConnectApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.model.DeviceInfo
import timber.log.Timber

interface QuickConnectRepository {
	/**
	 * Démarre une session QuickConnect.
	 * Retourne le code à afficher + secret interne.
	 */
	suspend fun initiate(serverAddress: String): QuickConnectFlowState.CodeReady

	/**
	 * Polling : vérifie si l'utilisateur a approuvé
	 * le code dans l'interface web Jellyfin.
	 * Retourne true si authentifié.
	 */
	suspend fun checkAuthorized(
		serverAddress: String,
		secret: String,
	): Boolean

	/**
	 * Échange le secret approuvé contre un token d'auth.
	 */
	suspend fun connect(
		serverAddress: String,
		secret: String,
	): String
}

class QuickConnectRepositoryImpl(
	private val jellyfin: Jellyfin,
	private val deviceInfo: DeviceInfo,
) : QuickConnectRepository {
	override suspend fun initiate(serverAddress: String): QuickConnectFlowState.CodeReady =
		withContext(Dispatchers.IO) {
			val api = jellyfin.createApi(baseUrl = serverAddress)
			api.update(deviceInfo = deviceInfo)

			val result = api.quickConnectApi.initiateQuickConnect().content
			val formattedCode =
				result.code.let { code ->
					if (code.length == 6) {
						"${code.substring(0, 3)}-${code.substring(3)}"
					} else {
						code
					}
				}

			QuickConnectFlowState.CodeReady(
				code = formattedCode,
				secret = result.secret,
			)
		}

	override suspend fun checkAuthorized(
		serverAddress: String,
		secret: String,
	): Boolean =
		withContext(Dispatchers.IO) {
			try {
				val api = jellyfin.createApi(baseUrl = serverAddress)
				val result = api.quickConnectApi.getQuickConnectState(secret = secret).content
				result.authenticated
			} catch (e: Exception) {
				Timber.w(e, "QuickConnect check failed for %s", serverAddress)
				false
			}
		}

	override suspend fun connect(
		serverAddress: String,
		secret: String,
	): String =
		withContext(Dispatchers.IO) {
			val api = jellyfin.createApi(baseUrl = serverAddress)
			val result = api.userApi.authenticateWithQuickConnect(secret).content
			result.accessToken ?: error("QuickConnect authentication returned null accessToken")
		}
}
