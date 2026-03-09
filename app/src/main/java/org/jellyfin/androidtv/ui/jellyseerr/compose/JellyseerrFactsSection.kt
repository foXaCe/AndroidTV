package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import org.jellyfin.design.token.RadiusTokens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMovieDetailsDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrTvDetailsDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun JellyseerrFactsSection(
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
	selectedItem: JellyseerrDiscoverItemDto?,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val factRows = remember(movieDetails, tvDetails, selectedItem) {
		buildFactRows(context, movieDetails, tvDetails, selectedItem)
	}

	if (factRows.isEmpty()) return

	androidx.compose.foundation.layout.Column(modifier = modifier.width(320.dp)) {
		factRows.forEachIndexed { index, (label, value) ->
			val isFirst = index == 0
			val isLast = index == factRows.size - 1
			FactRow(label = label, value = value, isFirst = isFirst, isLast = isLast)
		}
	}
}

@Composable
private fun FactRow(
	label: String,
	value: String,
	isFirst: Boolean,
	isLast: Boolean,
) {
	val radius = RadiusTokens.radiusSm
	val shape = RoundedCornerShape(
		topStart = if (isFirst) radius else 0.dp,
		topEnd = if (isFirst) radius else 0.dp,
		bottomStart = if (isLast) radius else 0.dp,
		bottomEnd = if (isLast) radius else 0.dp,
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(shape)
			.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, shape)
			.padding(horizontal = 16.dp, vertical = 8.dp),
	) {
		Text(
			text = label,
			style = JellyfinTheme.typography.bodySmall,
			color = JellyfinTheme.colorScheme.textPrimary,
			modifier = Modifier.weight(1f),
		)
		Text(
			text = value,
			style = JellyfinTheme.typography.bodySmall,
			color = JellyfinTheme.colorScheme.textSecondary,
			textAlign = TextAlign.End,
			modifier = Modifier.weight(1f),
		)
	}
}

private fun buildFactRows(
	context: android.content.Context,
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
	selectedItem: JellyseerrDiscoverItemDto?,
): List<Pair<String, String>> {
	val rows = mutableListOf<Pair<String, String>>()

	val voteAverage = movieDetails?.voteAverage ?: tvDetails?.voteAverage ?: selectedItem?.voteAverage
	if (voteAverage != null && voteAverage > 0) {
		rows.add(context.getString(R.string.jellyseerr_tmdb_score) to "${(voteAverage * 10).toInt()}%")
	}

	val status = movieDetails?.status ?: tvDetails?.status
	if (status != null) {
		rows.add(context.getString(R.string.lbl_status_label) to status)
	}

	if (tvDetails != null) {
		tvDetails.firstAirDate?.let { date ->
			formatDate(date)?.let { rows.add(context.getString(R.string.lbl_first_air_date) to it) }
		}
		tvDetails.lastAirDate?.let { date ->
			formatDate(date)?.let { rows.add(context.getString(R.string.lbl_last_air_date) to it) }
		}
		tvDetails.numberOfSeasons?.let { seasons ->
			rows.add(context.getString(R.string.lbl_seasons) to seasons.toString())
		}
	}

	if (movieDetails != null) {
		movieDetails.releaseDate?.let { date ->
			formatDate(date)?.let { rows.add(context.getString(R.string.lbl_release_date) to it) }
		}
		movieDetails.revenue?.let { revenue ->
			if (revenue > 0) {
				rows.add(context.getString(R.string.lbl_revenue) to NumberFormat.getCurrencyInstance(Locale.US).format(revenue))
			}
		}
	}

	movieDetails?.runtime?.let { runtime ->
		val hours = runtime / 60
		val minutes = runtime % 60
		val runtimeText = if (hours > 0) {
			context.getString(R.string.runtime_hours_minutes, hours, minutes)
		} else {
			context.getString(R.string.runtime_minutes, minutes)
		}
		rows.add(context.getString(R.string.lbl_runtime) to runtimeText)
	}

	movieDetails?.budget?.let { budget ->
		if (budget > 0) {
			rows.add(context.getString(R.string.lbl_budget) to NumberFormat.getCurrencyInstance(Locale.US).format(budget))
		}
	}

	val networks = tvDetails?.networks?.take(3)?.map { it.name }
	if (!networks.isNullOrEmpty()) {
		rows.add(context.getString(R.string.lbl_networks) to networks.joinToString(", "))
	}

	return rows
}

private fun formatDate(dateString: String): String? {
	return try {
		val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
		val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
		val date = inputFormat.parse(dateString)
		date?.let { outputFormat.format(it) }
	} catch (_: Exception) {
		dateString
	}
}
