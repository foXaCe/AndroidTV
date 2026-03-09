package org.jellyfin.androidtv.data.service.jellyseerr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VegafoXStatusResponse(
	val enabled: Boolean = false,
	val authenticated: Boolean = false,
	val url: String? = null,
	val jellyseerrUserId: Int? = null,
	val displayName: String? = null,
	val avatar: String? = null,
	val permissions: Int = 0,
	val sessionCreated: Long? = null,
	val lastValidated: Long? = null,
)

@Serializable
data class VegafoXLoginRequest(
	@SerialName("username") val username: String,
	@SerialName("password") val password: String,
	@SerialName("authType") val authType: String = "jellyfin",
)

@Serializable
data class VegafoXLoginResponse(
	val success: Boolean = false,
	val error: String? = null,
	val jellyseerrUserId: Int? = null,
	val displayName: String? = null,
	val avatar: String? = null,
	val permissions: Int = 0,
)

@Serializable
data class VegafoXValidateResponse(
	val valid: Boolean = false,
	val lastValidated: Long? = null,
)
