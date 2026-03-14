package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import org.jellyfin.androidtv.util.apiclient.getLogoImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemBackdropImages
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.apiclient.parentBackdropImages
import org.jellyfin.androidtv.util.apiclient.seriesPrimaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType

fun getBackdropUrl(
	item: BaseItemDto,
	api: ApiClient,
): String? {
	val backdropImage =
		item.itemBackdropImages.firstOrNull()
			?: item.parentBackdropImages.firstOrNull()
	return backdropImage?.getUrl(api, maxWidth = 1920)
}

fun getPosterUrl(
	item: BaseItemDto,
	api: ApiClient,
): String? =
	when {
		item.type == BaseItemKind.EPISODE -> {
			val thumbImage = item.itemImages[ImageType.THUMB]
			val primaryImage = item.itemImages[ImageType.PRIMARY]
			(thumbImage ?: primaryImage)?.getUrl(api, maxWidth = 500)
		}
		item.type == BaseItemKind.SEASON -> {
			val seasonImage = item.itemImages[ImageType.PRIMARY]
			val fallback = seasonImage ?: item.seriesPrimaryImage
			fallback?.getUrl(api, maxHeight = 600)
		}
		else -> {
			item.itemImages[ImageType.PRIMARY]?.getUrl(api, maxHeight = 600)
		}
	}

fun getLogoUrl(
	item: BaseItemDto,
	api: ApiClient,
): String? {
	val logoImage = item.getLogoImage()
	return logoImage?.getUrl(api, maxWidth = 400)
}

fun getEpisodeThumbnailUrl(
	ep: BaseItemDto,
	api: ApiClient,
): String? {
	val primaryImage = ep.itemImages[ImageType.PRIMARY]
	return primaryImage?.getUrl(api, maxWidth = 400)
}

fun formatDuration(ticks: Long): String {
	val totalMinutes = (ticks / 10_000_000 / 60).toInt()
	val hours = totalMinutes / 60
	val minutes = totalMinutes % 60
	return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun getEndsAt(ticks: Long): String {
	val endTime = java.util.Date(System.currentTimeMillis() + ticks / 10_000)
	val cal =
		java.util.Calendar
			.getInstance()
			.apply { time = endTime }
	val hours = cal.get(java.util.Calendar.HOUR)
	val minutes = cal.get(java.util.Calendar.MINUTE)
	val ampm = if (cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
	val h = if (hours == 0) 12 else hours
	val m = if (minutes < 10) "0$minutes" else "$minutes"
	return "$h:$m $ampm"
}
