package org.jellyfin.androidtv.ui.playback.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import kotlin.time.Duration

@Composable
private fun getSkipButtonText(
	nextEpisodeTitle: String?,
	timeRemaining: Duration?,
	segmentType: String?,
): String =
	when {
		nextEpisodeTitle != null && timeRemaining != null -> {
			val seconds = timeRemaining.inWholeSeconds
			stringResource(R.string.play_next_episode_countdown, seconds)
		}
		nextEpisodeTitle != null -> {
			stringResource(R.string.lbl_play_next_up)
		}
		segmentType == "INTRO" -> stringResource(R.string.skip_intro)
		segmentType == "RECAP" -> stringResource(R.string.skip_recap)
		segmentType == "COMMERCIAL" -> stringResource(R.string.skip_commercial)
		segmentType == "PREVIEW" -> stringResource(R.string.skip_preview)
		segmentType == "OUTRO" -> stringResource(R.string.skip_outro)
		else -> stringResource(R.string.segment_action_skip)
	}

@Composable
fun SkipOverlay(
	visible: Boolean,
	segmentType: String?,
	nextEpisodeTitle: String?,
	timeRemaining: Duration?,
	onSkip: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val focusRequester = remember { FocusRequester() }

	Box(
		contentAlignment = Alignment.BottomEnd,
		modifier =
			modifier
				.fillMaxSize(),
	) {
		AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
			val text = getSkipButtonText(nextEpisodeTitle, timeRemaining, segmentType)

			VegafoXButton(
				text = text,
				onClick = onSkip,
				variant = VegafoXButtonVariant.Primary,
				icon = VegafoXIcons.SkipNext,
				iconEnd = true,
				modifier = Modifier.focusRequester(focusRequester),
			)

			LaunchedEffect(Unit) {
				focusRequester.requestFocus()
			}
		}
	}
}
