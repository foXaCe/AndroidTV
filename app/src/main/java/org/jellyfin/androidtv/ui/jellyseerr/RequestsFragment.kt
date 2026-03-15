package org.jellyfin.androidtv.ui.jellyseerr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestsFragment : Fragment() {
	private val viewModel: JellyseerrDetailsViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			RequestsScreen(viewModel = viewModel)
		}
	}
}

@Composable
private fun RequestsScreen(viewModel: JellyseerrDetailsViewModel) {
	val requests by viewModel.userRequests.collectAsState()
	val loadingState by viewModel.loadingState.collectAsState()
	val isAvailable by viewModel.isAvailable.collectAsState()

	LaunchedEffect(Unit) {
		kotlinx.coroutines.delay(1000)
		viewModel.loadRequests()
	}

	val displayState =
		when {
			loadingState is JellyseerrLoadingState.Loading -> DisplayState.LOADING
			requests.isEmpty() -> DisplayState.EMPTY
			else -> DisplayState.CONTENT
		}

	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep)
				.padding(horizontal = 48.dp),
	) {
		// Header
		Text(
			text = stringResource(R.string.jellyseerr_my_requests),
			fontFamily = BebasNeue,
			fontSize = 32.sp,
			fontWeight = FontWeight.Bold,
			color = VegafoXColors.TextPrimary,
			modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
		)

		// Connection warning
		if (!isAvailable) {
			Box(
				modifier =
					Modifier
						.fillMaxWidth()
						.background(VegafoXColors.ErrorContainer, RoundedCornerShape(8.dp))
						.padding(12.dp),
			) {
				Text(
					text = stringResource(R.string.jellyseerr_not_connected),
					color = VegafoXColors.Error,
					fontSize = 14.sp,
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
		}

		// Content
		StateContainer(
			state = displayState,
			modifier = Modifier.weight(1f),
			emptyContent = {
				EmptyState(
					title = stringResource(R.string.jellyseerr_no_requests),
					icon = VegafoXIcons.Pending,
				)
			},
			content = {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(requests, key = { it.id }) { request ->
						RequestItem(request = request)
					}
				}
			},
		)

		// Refresh button
		Spacer(modifier = Modifier.height(12.dp))
		VegafoXButton(
			text = stringResource(R.string.btn_refresh),
			onClick = { viewModel.loadRequests() },
			variant = VegafoXButtonVariant.Secondary,
			icon = VegafoXIcons.Refresh,
			compact = true,
		)
		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Composable
private fun RequestItem(request: JellyseerrRequestDto) {
	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.background(VegafoXColors.SurfaceContainer, RoundedCornerShape(8.dp))
				.padding(12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Poster
		val posterUrl = request.media?.posterPath?.let { "https://image.tmdb.org/t/p/w200$it" }
		Box(
			modifier =
				Modifier
					.size(60.dp, 90.dp)
					.clip(RoundedCornerShape(6.dp))
					.background(VegafoXColors.SurfaceDim),
		) {
			if (posterUrl != null) {
				AsyncImage(
					model = posterUrl,
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))

		// Info
		Column(modifier = Modifier.weight(1f)) {
			// Title + type
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = request.media?.title ?: request.media?.name ?: stringResource(R.string.lbl_unknown),
					color = VegafoXColors.TextPrimary,
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f),
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = request.type.uppercase(),
					color = VegafoXColors.TextSecondary,
					fontSize = 11.sp,
					fontWeight = FontWeight.W600,
				)
			}

			Spacer(modifier = Modifier.height(4.dp))

			// Status
			val (statusText, statusColor, statusIcon) = getRequestStatus(request)
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (statusIcon != null) {
					Icon(
						imageVector = statusIcon,
						contentDescription = null,
						modifier = Modifier.size(14.dp),
						tint = statusColor,
					)
					Spacer(modifier = Modifier.width(4.dp))
				}
				Text(
					text = statusText,
					color = statusColor,
					fontSize = 13.sp,
				)
			}

			Spacer(modifier = Modifier.height(2.dp))

			// Requester + date
			val requester = request.requestedBy?.username ?: stringResource(R.string.lbl_unknown)
			Text(
				text = stringResource(R.string.jellyseerr_requested_by, requester),
				color = VegafoXColors.TextHint,
				fontSize = 12.sp,
			)
			val dateStr = request.createdAt?.substringBefore("T") ?: ""
			if (dateStr.isNotEmpty()) {
				Text(
					text = stringResource(R.string.jellyseerr_request_date, dateStr),
					color = VegafoXColors.TextHint,
					fontSize = 12.sp,
				)
			}
		}
	}
}

@Composable
private fun getRequestStatus(
	request: JellyseerrRequestDto,
): Triple<String, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.vector.ImageVector?> {
	val mediaStatus = request.media?.status
	return when {
		request.status == 1 -> Triple(stringResource(R.string.jellyseerr_status_pending), VegafoXColors.TextSecondary, VegafoXIcons.Pending)
		request.status == 3 -> Triple(stringResource(R.string.jellyseerr_status_declined), VegafoXColors.Error, VegafoXIcons.Declined)
		mediaStatus == 5 -> Triple(stringResource(R.string.jellyseerr_status_available), VegafoXColors.Success, VegafoXIcons.Available)
		mediaStatus == 4 ->
			Triple(
				stringResource(R.string.jellyseerr_status_partially_available),
				VegafoXColors.Info,
				VegafoXIcons.PartiallyAvailable,
			)
		mediaStatus == 3 -> Triple(stringResource(R.string.jellyseerr_status_downloading), VegafoXColors.Info, VegafoXIcons.Spinner)
		request.status == 2 -> Triple(stringResource(R.string.jellyseerr_status_approved), VegafoXColors.TextPrimary, VegafoXIcons.Check)
		else -> Triple(stringResource(R.string.jellyseerr_status_unknown), VegafoXColors.TextSecondary, null)
	}
}
