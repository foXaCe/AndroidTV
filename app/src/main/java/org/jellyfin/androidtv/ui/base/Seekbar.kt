package org.jellyfin.androidtv.ui.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.ui.player.base.calculateSeekMultiplier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times

@Immutable
data class SeekbarColors(
	val backgroundColor: Color,
	val bufferColor: Color,
	val progressColor: Color,
	val knobColor: Color,
)

object SeekbarDefaults {
	@ReadOnlyComposable
	@Composable
	fun colors(
		backgroundColor: Color = JellyfinTheme.colorScheme.rangeControlBackground,
		bufferColor: Color = JellyfinTheme.colorScheme.seekbarBuffer,
		progressColor: Color = JellyfinTheme.colorScheme.rangeControlFill,
		knobColor: Color = JellyfinTheme.colorScheme.rangeControlKnob,
	) = SeekbarColors(
		backgroundColor = backgroundColor,
		bufferColor = bufferColor,
		progressColor = progressColor,
		knobColor = knobColor,
	)
}

@Composable
fun Seekbar(
	modifier: Modifier = Modifier,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	progress: Duration = Duration.ZERO,
	buffer: Duration = Duration.ZERO,
	duration: Duration = Duration.ZERO,
	seekForwardAmount: Duration = duration / 100,
	seekRewindAmount: Duration = duration / 100,
	onScrubbing: ((scrubbing: Boolean) -> Unit)? = null,
	onSeek: ((progress: Duration) -> Unit)? = null,
	enabled: Boolean = true,
	colors: SeekbarColors = SeekbarDefaults.colors(),
	thumbnailContent: (@Composable (fraction: Float) -> Unit)? = null,
) {
	val durationMs = duration.inWholeMilliseconds.toFloat().coerceAtLeast(1f)
	val progressPercentage = progress.inWholeMilliseconds.toFloat() / durationMs
	val bufferPercentage = buffer.inWholeMilliseconds.toFloat() / durationMs
	val seekForwardPercentage = seekForwardAmount.inWholeMilliseconds.toFloat() / durationMs
	val seekRewindPercentage = seekRewindAmount.inWholeMilliseconds.toFloat() / durationMs

	Seekbar(
		modifier = modifier,
		interactionSource = interactionSource,
		progress = progressPercentage,
		buffer = bufferPercentage,
		seekForwardAmount = seekForwardPercentage,
		seekRewindAmount = seekRewindPercentage,
		contentDurationMinutes = duration.inWholeMinutes,
		onScrubbing = onScrubbing,
		onSeek = if (onSeek == null) null else { progress -> onSeek(progress.toDouble() * duration) },
		enabled = enabled,
		colors = colors,
		thumbnailContent = thumbnailContent,
	)
}

@Composable
fun Seekbar(
	modifier: Modifier = Modifier,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	progress: Float = 0f,
	buffer: Float = 0f,
	seekForwardAmount: Float = 0.01f,
	seekRewindAmount: Float = 0.01f,
	contentDurationMinutes: Long = 0L,
	onScrubbing: ((scrubbing: Boolean) -> Unit)? = null,
	onSeek: ((progress: Float) -> Unit)? = null,
	enabled: Boolean = true,
	colors: SeekbarColors = SeekbarDefaults.colors(),
	thumbnailContent: (@Composable (fraction: Float) -> Unit)? = null,
) {
	val coroutineScope = rememberCoroutineScope()
	val focused by interactionSource.collectIsFocusedAsState()
	var progressOverride by remember { mutableStateOf<Float?>(null) }
	val visibleProgress = progressOverride ?: progress
	val knobAlpha by animateFloatAsState(if (focused) 1f else 0f)
	var scrubCancelJob by remember { mutableStateOf<Job?>(null) }
	val isScrubbing = progressOverride != null
	var seekbarWidth by remember { mutableIntStateOf(0) }
	val density = LocalDensity.current
	var holdRepeatCount by remember { mutableStateOf(0) }

	Box(
		modifier = modifier.onSizeChanged { seekbarWidth = it.width },
	) {
		// Seekbar drawing
		Box(
			modifier =
				Modifier
					.matchParentSize()
					.onKeyEvent {
						if (!enabled) return@onKeyEvent false

						val isForward = it.key == Key.DirectionRight
						val isRewind = it.key == Key.DirectionLeft
						val isScrubKey = isForward || isRewind
						val isKeyUp = it.type == KeyEventType.KeyUp
						val isKeyDown = it.type == KeyEventType.KeyDown

						if (isScrubKey && isKeyDown) holdRepeatCount++
						if (isScrubKey && isKeyUp) holdRepeatCount = 0

						val multiplier = calculateSeekMultiplier(holdRepeatCount, contentDurationMinutes)
						val newProgress =
							when {
								isKeyDown && isForward -> (visibleProgress + seekForwardAmount * multiplier).coerceAtMost(1f)
								isKeyDown && isRewind -> (visibleProgress - seekRewindAmount * multiplier).coerceAtLeast(0f)
								else -> visibleProgress
							}

						if (isScrubKey && isKeyDown && onScrubbing != null) {
							scrubCancelJob?.cancel()
							onScrubbing(true)
						}

						if (visibleProgress != newProgress) {
							progressOverride = newProgress
							if (onSeek != null) onSeek(newProgress)
						}

						if (isScrubKey && isKeyUp && onScrubbing != null) {
							scrubCancelJob?.cancel()
							scrubCancelJob =
								coroutineScope.launch {
									delay(300.milliseconds)
									onScrubbing(false)
									progressOverride = null
								}
						}

						return@onKeyEvent isScrubKey
					}.focusable(interactionSource = interactionSource, enabled = enabled)
					.drawWithContent {
						val barCornerRadius = CornerRadius(size.minDimension, size.minDimension)

						// Background bar
						drawRoundRect(
							color = colors.backgroundColor,
							cornerRadius = barCornerRadius,
						)

						// Buffer bar
						if (buffer > 0f) {
							drawRoundRect(
								color = colors.bufferColor,
								size =
									size.copy(
										width = buffer * size.width,
									),
								cornerRadius = barCornerRadius,
							)
						}

						// Progress bar
						if (visibleProgress > 0f) {
							drawRoundRect(
								color = colors.progressColor,
								size =
									size.copy(
										width = visibleProgress * size.width,
									),
								cornerRadius = barCornerRadius,
							)
						}

						// Progress knob
						drawCircle(
							color = colors.knobColor,
							alpha = knobAlpha,
							center =
								center.copy(
									x = visibleProgress * size.width,
								),
							radius = size.minDimension * 2,
						)
					},
		)

		// Trickplay thumbnail preview above the seekbar
		if (thumbnailContent != null) {
			val thumbnailWidthDp = 178.dp
			val thumbnailHeightDp = 100.dp

			AnimatedVisibility(
				visible = isScrubbing,
				enter = fadeIn(tween(150)),
				exit = fadeOut(tween(150)),
			) {
				val thumbnailWidthPx = with(density) { thumbnailWidthDp.toPx() }
				val xOffsetDp =
					with(density) {
						if (seekbarWidth > 0) {
							(visibleProgress * seekbarWidth - thumbnailWidthPx / 2f)
								.coerceIn(0f, (seekbarWidth - thumbnailWidthPx).coerceAtLeast(0f))
								.toDp()
						} else {
							0.dp
						}
					}

				Box(
					modifier =
						Modifier
							.absoluteOffset(x = xOffsetDp, y = -(thumbnailHeightDp + 12.dp))
							.size(thumbnailWidthDp, thumbnailHeightDp),
				) {
					thumbnailContent(visibleProgress)
				}
			}
		}
	}
}
