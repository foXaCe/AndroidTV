package org.jellyfin.androidtv.ui.base.state

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jellyfin.androidtv.ui.base.AnimationDefaults

/**
 * Represents the current display state of a screen.
 */
enum class DisplayState {
	LOADING,
	EMPTY,
	ERROR,
	CONTENT,
}

/**
 * Container that manages animated transitions between Loading, Empty, Error, and Content states.
 *
 * Uses [AnimatedContent] with fade transitions for smooth state changes.
 * The skeleton loading content fades out as real content fades in.
 *
 * @param state Current [DisplayState]
 * @param modifier Modifier for the container
 * @param loadingContent Composable shown during LOADING (typically a skeleton screen)
 * @param emptyContent Composable shown when data is EMPTY
 * @param errorContent Composable shown on ERROR
 * @param content Composable shown when CONTENT is available
 */
@Composable
fun StateContainer(
	state: DisplayState,
	modifier: Modifier = Modifier,
	loadingContent: @Composable () -> Unit = {},
	emptyContent: @Composable () -> Unit = {},
	errorContent: @Composable () -> Unit = {},
	content: @Composable () -> Unit = {},
) {
	AnimatedContent(
		targetState = state,
		modifier = modifier,
		transitionSpec = {
			stateTransitionSpec(initialState, targetState)
		},
		contentAlignment = Alignment.Center,
		label = "StateContainer",
	) { currentState ->
		Box(modifier = Modifier.fillMaxSize()) {
			when (currentState) {
				DisplayState.LOADING -> loadingContent()
				DisplayState.EMPTY -> emptyContent()
				DisplayState.ERROR -> errorContent()
				DisplayState.CONTENT -> content()
			}
		}
	}
}

/**
 * Determines the transition animation spec between two display states.
 * - Loading -> Content: fade out loading, fade in content (medium duration)
 * - Loading -> Error/Empty: fade transition (medium duration)
 * - Any -> Loading: instant (no fade-in for loading, just show skeleton)
 */
private fun stateTransitionSpec(
	from: DisplayState,
	to: DisplayState,
): ContentTransform {
	val duration = AnimationDefaults.DURATION_MEDIUM

	return when {
		// Loading -> Content: smooth crossfade
		from == DisplayState.LOADING && to == DisplayState.CONTENT -> {
			fadeIn(tween(duration)) togetherWith fadeOut(tween(duration / 2))
		}
		// Loading -> Error/Empty: smooth fade
		from == DisplayState.LOADING -> {
			fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
		}
		// Error -> Loading (retry): quick fade
		from == DisplayState.ERROR && to == DisplayState.LOADING -> {
			fadeIn(tween(AnimationDefaults.DURATION_FAST)) togetherWith
				fadeOut(tween(AnimationDefaults.DURATION_FAST))
		}
		// Default: standard crossfade
		else -> {
			fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
		}
	}
}
