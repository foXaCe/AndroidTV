package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrPersonDetailsDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.JellyseerrDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun JellyseerrPersonDetailsScreen(
	personName: String,
	personDetails: JellyseerrPersonDetailsDto?,
	appearances: List<JellyseerrDiscoverItemDto>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
) {
	val scrollState = rememberScrollState()

	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep)
				.verticalScroll(scrollState)
				.padding(horizontal = JellyseerrDimensions.screenPaddingHorizontal, vertical = 24.dp),
	) {
		// Header: photo + name + birth info
		PersonHeader(personName = personName, personDetails = personDetails)

		// Biography
		personDetails?.biography?.takeIf { it.isNotBlank() }?.let { bio ->
			Spacer(modifier = Modifier.height(16.dp))
			BiographySection(biography = bio)
		}

		// Appearances grid
		if (appearances.isNotEmpty()) {
			Spacer(modifier = Modifier.height(24.dp))
			AppearancesSection(appearances = appearances, onItemClick = onItemClick)
		}

		Spacer(modifier = Modifier.height(48.dp))
	}
}

@Composable
private fun PersonHeader(
	personName: String,
	personDetails: JellyseerrPersonDetailsDto?,
) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Profile photo (circular)
		val profilePath = personDetails?.profilePath
		AsyncImage(
			model =
				if (profilePath != null) {
					ImageRequest
						.Builder(LocalContext.current)
						.data("https://image.tmdb.org/t/p/w185$profilePath")
						.build()
				} else {
					null
				},
			contentDescription = personDetails?.name ?: personName,
			contentScale = ContentScale.Crop,
			modifier =
				Modifier
					.size(120.dp)
					.border(2.dp, VegafoXColors.OrangePrimary, CircleShape)
					.clip(CircleShape)
					.background(VegafoXColors.Surface),
		)

		Spacer(modifier = Modifier.width(24.dp))

		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = personDetails?.name ?: personName,
				style =
					TextStyle(
						fontFamily = BebasNeue,
						fontSize = 36.sp,
						letterSpacing = 2.sp,
					),
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(bottom = 8.dp),
			)

			val birthInfo =
				remember(personDetails) {
					buildList {
						personDetails?.birthday?.let { bday ->
							formatDate(bday)?.let { add("Born $it") }
						}
						personDetails?.placeOfBirth?.let { add("in $it") }
					}.joinToString(" ")
				}

			if (birthInfo.isNotEmpty()) {
				Text(
					text = birthInfo,
					style = JellyfinTheme.typography.bodyMedium,
					color = VegafoXColors.TextSecondary,
				)
			}
		}
	}
}

@Composable
private fun BiographySection(biography: String) {
	var isExpanded by remember { mutableStateOf(false) }

	Text(
		text = stringResource(R.string.lbl_biography),
		style = JellyfinTheme.typography.titleLarge,
		color = VegafoXColors.TextPrimary,
		modifier = Modifier.padding(bottom = 12.dp),
	)

	Text(
		text = biography,
		style = JellyfinTheme.typography.bodyMedium,
		color = VegafoXColors.TextSecondary,
		maxLines = if (isExpanded) Int.MAX_VALUE else 6,
		overflow = TextOverflow.Ellipsis,
		modifier =
			Modifier
				.fillMaxWidth()
				.animateContentSize()
				.padding(bottom = 8.dp),
	)

	// Toggle button
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Text(
		text = stringResource(if (isExpanded) R.string.btn_show_less else R.string.btn_show_more),
		style = JellyfinTheme.typography.bodyMedium,
		color = VegafoXColors.Info,
		modifier =
			Modifier
				.background(if (isFocused) VegafoXColors.SurfaceBright else Color.Transparent)
				.clickable(interactionSource = interactionSource, indication = null) {
					isExpanded = !isExpanded
				}.focusable(interactionSource = interactionSource)
				.padding(8.dp),
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearancesSection(
	appearances: List<JellyseerrDiscoverItemDto>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
) {
	Text(
		text = stringResource(R.string.lbl_appearances),
		style = JellyfinTheme.typography.titleLarge,
		color = VegafoXColors.TextPrimary,
		modifier = Modifier.padding(bottom = 16.dp),
	)

	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
		maxItemsInEachRow = 5,
	) {
		appearances.forEach { item ->
			AppearanceCard(item = item, onClick = { onItemClick(item) })
		}
	}
}

@Composable
private fun AppearanceCard(
	item: JellyseerrDiscoverItemDto,
	onClick: () -> Unit,
) {
	JellyseerrPosterCard(
		item = item,
		onClick = onClick,
	)
}

private fun formatDate(dateString: String): String? =
	try {
		val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
		val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
		val date = inputFormat.parse(dateString)
		date?.let { outputFormat.format(it) }
	} catch (_: Exception) {
		null
	}
