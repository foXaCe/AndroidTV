package org.jellyfin.androidtv.util

import android.content.Context
import org.jellyfin.sdk.model.api.UserDto
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import java.util.UUID
import kotlin.math.roundToInt

/**
 * A collection of utility methods, all static.
 */
object Utils {
	@JvmStatic
	fun convertDpToPixel(
		ctx: Context,
		dp: Int,
	): Int = (dp * ctx.resources.displayMetrics.density).roundToInt()

	@JvmStatic
	fun isTrue(value: Boolean?): Boolean = value == true

	@JvmStatic
	fun <T> getSafeValue(
		value: T?,
		defaultValue: T,
	): T = value ?: defaultValue

	@JvmStatic
	fun isEmpty(value: String?): Boolean = value.isNullOrEmpty()

	@JvmStatic
	fun getSafeSeekPosition(
		position: Long,
		duration: Long,
	): Long =
		when {
			position >= duration -> (duration - 1000).coerceAtLeast(0)
			else -> position.coerceAtLeast(0)
		}

	@JvmStatic
	fun canManageRecordings(user: UserDto?): Boolean = user?.policy?.enableLiveTvManagement == true

	@JvmStatic
	fun uuidOrNull(string: String?): UUID? = string?.toUUIDOrNull()
}
