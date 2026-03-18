package org.jellyfin.androidtv.ui.player.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.modifier.overscan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Calculate seek acceleration multiplier based on hold repeat count and content duration.
 * Inspired by Wholphin's SeekAcceleration — longer content gets faster acceleration.
 */
fun calculateSeekMultiplier(
	repeatCount: Int,
	durationMinutes: Long,
): Int {
	val scaled = repeatCount / 3 // normalize device-dependent repeat cadence
	return when {
		durationMinutes < 30 -> if (scaled < 30) 1 else 2
		durationMinutes < 90 ->
			when {
				scaled < 13 -> 1
				scaled < 50 -> 2
				scaled < 75 -> 3
				else -> 4
			}
		durationMinutes < 150 ->
			when {
				scaled < 20 -> 1
				scaled < 40 -> 2
				scaled < 60 -> 4
				else -> 6
			}
		else ->
			when {
				scaled < 20 -> 1
				scaled < 40 -> 3
				scaled < 60 -> 6
				else -> 10
			}
	}
}

@Composable
fun PlayerOverlayLayout(
	modifier: Modifier = Modifier,
	visibilityState: PlayerOverlayVisibilityState = rememberPlayerOverlayVisibility(),
	contentDurationMinutes: Long = 0L,
	onPlayPause: (() -> Unit)? = null,
	onSeekForward: ((multiplier: Int) -> Unit)? = null,
	onSeekBackward: ((multiplier: Int) -> Unit)? = null,
	header: (@Composable () -> Unit)? = null,
	controls: (@Composable () -> Unit)? = null,
) {
	var holdRepeatCount by remember { mutableStateOf(0) }

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.focusable()
				.onPreviewKeyEvent {
					if (visibilityState.visible) visibilityState.show()
					false
				}.onKeyEvent {
					if (it.key == Key.Back && visibilityState.visible) {
						visibilityState.hide()
						true
					} else if (!visibilityState.visible && it.type == KeyEventType.KeyDown) {
						when (it.key) {
							Key.DirectionCenter, Key.Enter -> {
								onPlayPause?.invoke()
								true
							}
							Key.DirectionRight -> {
								holdRepeatCount++
								val multiplier = calculateSeekMultiplier(holdRepeatCount, contentDurationMinutes)
								onSeekForward?.invoke(multiplier)
								true
							}
							Key.DirectionLeft -> {
								holdRepeatCount++
								val multiplier = calculateSeekMultiplier(holdRepeatCount, contentDurationMinutes)
								onSeekBackward?.invoke(multiplier)
								true
							}
							else -> {
								if (!it.nativeKeyEvent.isSystem) {
									visibilityState.show()
									true
								} else {
									false
								}
							}
						}
					} else if (!visibilityState.visible && it.type == KeyEventType.KeyUp) {
						if (it.key == Key.DirectionRight || it.key == Key.DirectionLeft) {
							holdRepeatCount = 0
						}
						!it.nativeKeyEvent.isSystem
					} else if (!visibilityState.visible && !it.nativeKeyEvent.isSystem) {
						true
					} else {
						false
					}
				},
	) {
		if (header != null) {
			AnimatedVisibility(
				visible = visibilityState.visible,
				modifier =
					Modifier
						.align(Alignment.TopCenter),
				enter = slideInVertically() + fadeIn(),
				exit = slideOutVertically() + fadeOut(),
			) {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.fillMaxHeight(0.35f)
							.background(
								brush =
									Brush.verticalGradient(
										0.0f to VegafoXColors.BackgroundDeep.copy(alpha = 0.90f),
										0.40f to Color.Transparent,
									),
							).overscan(),
				) {
					header()
				}
			}
		}

		if (controls != null) {
			AnimatedVisibility(
				visible = visibilityState.visible,
				modifier =
					Modifier
						.align(Alignment.BottomCenter),
				enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
				exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
			) {
				Box(
					contentAlignment = Alignment.BottomCenter,
					modifier =
						Modifier
							.fillMaxWidth()
							.fillMaxHeight(0.45f)
							.background(
								brush =
									Brush.verticalGradient(
										0.55f to Color.Transparent,
										1.0f to VegafoXColors.BackgroundDeep.copy(alpha = 0.95f),
									),
							).overscan(),
				) {
					JellyfinTheme(
						colorScheme =
							JellyfinTheme.colorScheme.copy(
								button = Color.Transparent,
							),
					) {
						controls()
					}
				}
			}
		}
	}
}

@Stable
data class PlayerOverlayVisibilityState(
	val visible: Boolean,
	val toggle: () -> Unit,
	val show: () -> Unit,
	val hide: () -> Unit,
)

@Composable
fun rememberPlayerOverlayVisibility(timeout: Duration = 3.seconds): PlayerOverlayVisibilityState {
	val scope = rememberCoroutineScope()
	var timerVisible by remember { mutableStateOf(false) }
	var timerJob by remember { mutableStateOf<Job?>(null) }
	var visible = timerVisible

	fun show() {
		timerVisible = true
		timerJob?.cancel()
		timerJob =
			scope.launch {
				delay(timeout)
				timerVisible = false
			}
	}

	fun hide() {
		timerVisible = false
		timerJob?.cancel()
		timerJob = null
	}

	fun toggle() {
		if (timerVisible) {
			hide()
		} else {
			show()
		}
	}

	// Force visibility when not the active window, reset timer when it changes
	// to make sure popups keep the overlay visible
	val windowInfo = LocalWindowInfo.current
	visible = visible || !windowInfo.isWindowFocused

	var previousIsWindowFocused by remember { mutableStateOf(windowInfo.isWindowFocused) }
	LaunchedEffect(windowInfo.isWindowFocused) {
		if (windowInfo.isWindowFocused != previousIsWindowFocused) show()
		previousIsWindowFocused = windowInfo.isWindowFocused
	}

	return PlayerOverlayVisibilityState(
		visible = visible,
		toggle = ::toggle,
		show = ::show,
		hide = ::hide,
	)
}
