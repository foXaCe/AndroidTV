@file:Suppress("DEPRECATION")

package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import org.jellyfin.androidtv.R
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

private val NoOpBringIntoViewResponder =
	object : BringIntoViewResponder {
		override fun calculateRectForParent(localRect: Rect): Rect = Rect.Zero

		override suspend fun bringChildIntoView(localRect: () -> Rect?) {
			// Intentionally empty — neutralizes the automatic "bring into view" scroll
			// that Compose triggers when an element receives focus via requestFocus().
		}
	}

fun Modifier.disableBringIntoView(): Modifier = this.bringIntoViewResponder(NoOpBringIntoViewResponder)

fun Modifier.smoothScrollToTop(listState: LazyListState): Modifier =
	this.bringIntoViewResponder(
		object : BringIntoViewResponder {
			override fun calculateRectForParent(localRect: Rect): Rect = Rect.Zero

			override suspend fun bringChildIntoView(localRect: () -> Rect?) {
				listState.animateScrollToItem(0)
			}
		},
	)

fun getBackdropUrl(
	item: BaseItemDto,
	api: ApiClient,
): String? {
	val backdropImage =
		item.itemBackdropImages.firstOrNull()
			?: item.parentBackdropImages.firstOrNull()
	return backdropImage?.getUrl(api, maxWidth = 1920, quality = 96)
}

fun getPosterUrl(
	item: BaseItemDto,
	api: ApiClient,
): String? =
	when {
		item.type == BaseItemKind.EPISODE -> {
			val seriesPoster = item.seriesPrimaryImage
			val episodePrimary = item.itemImages[ImageType.PRIMARY]
			(seriesPoster ?: episodePrimary)?.getUrl(api, maxHeight = 600)
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
	return if (hours > 0) "${hours}h%02dm".format(minutes) else "${minutes}min"
}

private val genreResIds =
	mapOf(
		"action" to R.string.genre_action,
		"action & adventure" to R.string.genre_action_adventure,
		"action adventure" to R.string.genre_action_adventure,
		"adventure" to R.string.genre_adventure,
		"animation" to R.string.genre_animation,
		"anime" to R.string.genre_anime,
		"biography" to R.string.genre_biography,
		"comedy" to R.string.genre_comedy,
		"crime" to R.string.genre_crime,
		"documentary" to R.string.genre_documentary,
		"drama" to R.string.genre_drama,
		"family" to R.string.genre_family,
		"fantasy" to R.string.genre_fantasy,
		"history" to R.string.genre_history,
		"horror" to R.string.genre_horror,
		"kids" to R.string.genre_kids,
		"music" to R.string.genre_music,
		"musical" to R.string.genre_musical,
		"mystery" to R.string.genre_mystery,
		"news" to R.string.genre_news,
		"reality" to R.string.genre_reality,
		"romance" to R.string.genre_romance,
		"science fiction" to R.string.genre_science_fiction,
		"sci-fi & fantasy" to R.string.genre_scifi_fantasy,
		"short" to R.string.genre_short,
		"sport" to R.string.genre_sport,
		"superhero" to R.string.genre_superhero,
		"thriller" to R.string.genre_thriller,
		"war" to R.string.genre_war,
		"war & politics" to R.string.genre_war_politics,
		"western" to R.string.genre_western,
	)

fun translateGenre(
	context: Context,
	genre: String,
): String {
	val resId = genreResIds[genre.trim().lowercase()]
	return if (resId != null) context.getString(resId) else genre.trim()
}

fun translateGenreUpper(
	context: Context,
	genre: String,
): String = translateGenre(context, genre).uppercase()

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
