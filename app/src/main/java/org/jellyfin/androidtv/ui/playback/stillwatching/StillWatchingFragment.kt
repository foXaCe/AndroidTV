package org.jellyfin.androidtv.ui.playback.stillwatching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.background.AppBackground
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.button.ProgressButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.composable.modifier.overscan
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.common.PlaybackPromptItemData
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

val TIMEOUT_IN_MS = 10.seconds.inWholeMilliseconds.toInt()

private val ExitProgressColor = Color(0x66FF5050) // red rgba(255,80,80,0.40)

@Composable
fun StillWatchingScreen(itemId: UUID) {
	val navigationRepository = koinInject<NavigationRepository>()
	val backgroundService = koinInject<BackgroundService>()
	val viewModel = koinViewModel<StillWatchingViewModel>()

	val state by viewModel.state.collectAsState()

	LaunchedEffect(itemId) {
		viewModel.setItemId(itemId)
	}

	val item by viewModel.item.collectAsState()
	if (item == null) return

	LaunchedEffect(item?.baseItem) {
		backgroundService.setBackground(item?.baseItem, BlurContext.DETAILS)
	}

	LaunchedEffect(state) {
		when (state) {
			StillWatchingState.STILL_WATCHING -> navigationRepository.navigate(Destinations.videoPlayer(0), true)
			StillWatchingState.CLOSE -> navigationRepository.goBack()
			else -> Unit
		}
	}

	val focusRequester = remember { FocusRequester() }

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(Color.Black),
	) {
		// Blur backdrop at 40% opacity
		Box(modifier = Modifier.graphicsLayer { alpha = 0.40f }) {
			AppBackground()
		}

		// Bottom gradient
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.fillMaxHeight(0.5f)
					.align(Alignment.BottomCenter)
					.background(
						Brush.verticalGradient(
							colors =
								listOf(
									Color.Transparent,
									VegafoXColors.BackgroundDeep.copy(alpha = 0.95f),
								),
						),
					),
		)

		// Logo top-left
		item?.logo?.let { logo ->
			val api = koinInject<ApiClient>()
			AsyncImage(
				modifier =
					Modifier
						.align(Alignment.TopStart)
						.overscan()
						.height(75.dp),
				url = logo.getUrl(api),
				blurHash = logo.blurHash,
				aspectRatio = logo.aspectRatio ?: 1f,
			)
		}

		// Content overlay
		StillWatchingOverlay(
			modifier =
				Modifier
					.align(Alignment.BottomCenter)
					.focusRequester(focusRequester),
			item = requireNotNull(item),
			onConfirm = { viewModel.stillWatching() },
			onCancel = { viewModel.close() },
		)
	}

	LaunchedEffect(focusRequester) {
		focusRequester.requestFocus()
	}
}

@Composable
fun StillWatchingOverlay(
	modifier: Modifier = Modifier,
	item: PlaybackPromptItemData,
	onConfirm: () -> Unit,
	onCancel: () -> Unit,
) {
	val api = koinInject<ApiClient>()
	val endWatchingTimer = remember { Animatable(0f) }
	LaunchedEffect(item) {
		endWatchingTimer.animateTo(
			targetValue = 1f,
			animationSpec =
				tween(
					durationMillis = TIMEOUT_IN_MS,
					easing = LinearEasing,
				),
		)
		onCancel()
	}

	// Build subtitle from series info
	val subtitle =
		remember(item) {
			buildString {
				item.baseItem.seriesName?.let { append(it) }
				val season = item.baseItem.parentIndexNumber
				val episode = item.baseItem.indexNumber
				if (season != null || episode != null) {
					if (isNotEmpty()) append(" \u00b7 ")
					if (season != null) append("S${season.toString().padStart(2, '0')}")
					if (episode != null) append("E${episode.toString().padStart(2, '0')}")
				}
			}
		}

	val focusRequester = remember { FocusRequester() }
	Row(
		horizontalArrangement = Arrangement.spacedBy(24.dp),
		verticalAlignment = Alignment.Bottom,
		modifier =
			modifier
				.overscan()
				.fillMaxWidth()
				.focusRestorer(focusRequester),
	) {
		// Thumbnail left
		Column(
			horizontalAlignment = Alignment.Start,
		) {
			item.thumbnail?.let { thumbnail ->
				AsyncImage(
					modifier =
						Modifier
							.height(145.dp)
							.aspectRatio(thumbnail.aspectRatio ?: (16f / 9f))
							.clip(JellyfinTheme.shapes.extraSmall),
					url = thumbnail.getUrl(api),
					blurHash = thumbnail.blurHash,
					aspectRatio = thumbnail.aspectRatio ?: (16f / 9f),
				)
			}
		}

		Spacer(Modifier.weight(1f))

		// Text + buttons right
		Column(
			horizontalAlignment = Alignment.End,
		) {
			// Label
			Text(
				text = stringResource(R.string.still_watching_label).uppercase(),
				style =
					TextStyle(
						fontSize = 12.sp,
						fontWeight = FontWeight.Bold,
						color = VegafoXColors.OrangePrimary,
						letterSpacing = 2.sp,
					),
			)

			Spacer(Modifier.height(8.dp))

			// Title
			Text(
				text = item.title,
				style =
					TextStyle(
						fontFamily = BebasNeue,
						fontSize = 40.sp,
						color = VegafoXColors.TextPrimary,
						letterSpacing = 2.sp,
					),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)

			// Subtitle (series + episode)
			if (subtitle.isNotEmpty()) {
				Spacer(Modifier.height(4.dp))
				Text(
					text = subtitle,
					style =
						TextStyle(
							fontSize = 14.sp,
							color = VegafoXColors.TextSecondary,
						),
				)
			}

			Spacer(Modifier.height(20.dp))

			// Buttons
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				modifier =
					Modifier
						.focusGroup()
						.focusRestorer(focusRequester),
			) {
				// Exit button with red progress (auto-exit on timer expiry)
				val coroutineScope = rememberCoroutineScope()
				ProgressButton(
					progress = endWatchingTimer.value,
					onClick = onCancel,
					colors =
						ButtonDefaults.colors(
							containerColor = Color.Transparent,
							contentColor = VegafoXColors.TextSecondary,
							focusedContainerColor = VegafoXColors.SurfaceBright,
							focusedContentColor = VegafoXColors.TextPrimary,
						),
					progressColor = ExitProgressColor,
					modifier =
						Modifier
							.focusRequester(focusRequester)
							.onFocusChanged { state ->
								if (!state.isFocused) {
									coroutineScope.launch {
										endWatchingTimer.snapTo(0f)
									}
								}
							},
				) {
					Text(stringResource(R.string.lbl_exit))
				}

				Spacer(Modifier.width(4.dp))

				// Continue watching (primary action)
				VegafoXButton(
					text = stringResource(R.string.lbl_continue_watching),
					onClick = onConfirm,
					variant = VegafoXButtonVariant.Primary,
					compact = true,
				)
			}
		}
	}
}

class StillWatchingFragment : Fragment() {
	data class Args(
		val itemId: UUID,
	) {
		fun toBundle() = bundleOf(ARGUMENT_ITEM_ID to itemId.toString())

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val itemId =
					bundle?.getString(ARGUMENT_ITEM_ID)?.let {
						try {
							UUID.fromString(it)
						} catch (_: Exception) {
							null
						}
					} ?: return null
				return Args(itemId = itemId)
			}
		}
	}

	companion object {
		internal const val ARGUMENT_ITEM_ID = "item_id"
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			ScreenIdOverlay(ScreenIds.STILL_WATCHING_ID, ScreenIds.STILL_WATCHING_NAME) {
				val id = remember(arguments) { Args.fromBundle(arguments)?.itemId }
				if (id != null) {
					StillWatchingScreen(
						itemId = id,
					)
				}
			}
		}
	}
}
