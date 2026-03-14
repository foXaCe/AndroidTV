package org.jellyfin.androidtv.ui.navigation

import android.os.Bundle
import java.util.UUID

/**
 * Get a UUID from the bundle, or null if the key is missing or invalid.
 */
fun Bundle.getUUID(key: String): UUID? =
	getString(key)?.let { str ->
		try {
			UUID.fromString(str)
		} catch (_: IllegalArgumentException) {
			null
		}
	}

/**
 * Get a required UUID from the bundle.
 * @throws IllegalArgumentException if the key is missing or the value is not a valid UUID.
 */
fun Bundle.requireUUID(key: String): UUID = requireNotNull(getUUID(key)) { "Missing required UUID argument: $key" }
