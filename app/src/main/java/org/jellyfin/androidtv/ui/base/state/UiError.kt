package org.jellyfin.androidtv.ui.base.state

import org.jellyfin.androidtv.R
import org.jellyfin.sdk.api.client.exception.ApiClientException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unified error model for all UiState data classes.
 * Maps SDK/network exceptions to user-facing error types with appropriate string resources.
 */
sealed class UiError {
	abstract val cause: Throwable?
	abstract val messageRes: Int

	data class Network(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_network
	}

	data class Timeout(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_timeout
	}

	data class Server(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_server
	}

	data class Auth(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_auth
	}

	data class NotFound(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_not_found
	}

	data class Unknown(override val cause: Throwable? = null) : UiError() {
		override val messageRes: Int = R.string.state_error_generic
	}
}

/**
 * Maps a [Throwable] to the appropriate [UiError] type.
 *
 * - [UnknownHostException], [IOException] → [UiError.Network]
 * - [SocketTimeoutException] → [UiError.Timeout]
 * - [ApiClientException] → inspects cause chain for network/timeout, otherwise [UiError.Server]
 * - Everything else → [UiError.Unknown]
 */
fun Throwable.toUiError(): UiError = when {
	this is SocketTimeoutException -> UiError.Timeout(this)
	this is UnknownHostException -> UiError.Network(this)
	this is IOException -> UiError.Network(this)
	this is ApiClientException -> {
		val rootCause = cause
		when {
			rootCause is SocketTimeoutException -> UiError.Timeout(this)
			rootCause is UnknownHostException -> UiError.Network(this)
			rootCause is IOException -> UiError.Network(this)
			else -> UiError.Server(this)
		}
	}
	else -> UiError.Unknown(this)
}
