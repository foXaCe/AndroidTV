package org.jellyfin.androidtv.ui.base.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.jellyfin.androidtv.ui.base.AnimationDefaults

/**
 * Applies a staggered fade-in + slide-up animation on first composition.
 * Each item fades in with a [AnimationDefaults.STAGGER_DELAY]ms offset based on [index].
 *
 * @param index The item index in the list (used to calculate stagger delay)
 * @param maxStagger Maximum number of items to stagger (items beyond this appear instantly)
 */
@Composable
fun Modifier.staggerFadeIn(
	index: Int,
	maxStagger: Int = 15,
): Modifier {
	if (index >= maxStagger) return this

	val alpha = remember { Animatable(0f) }
	val offsetY = remember { Animatable(20f) }

	LaunchedEffect(Unit) {
		val delay = index * AnimationDefaults.STAGGER_DELAY
		alpha.animateTo(
			targetValue = 1f,
			animationSpec = tween(
				durationMillis = AnimationDefaults.DURATION_MEDIUM,
				delayMillis = delay,
				easing = FastOutSlowInEasing,
			),
		)
	}
	LaunchedEffect(Unit) {
		val delay = index * AnimationDefaults.STAGGER_DELAY
		offsetY.animateTo(
			targetValue = 0f,
			animationSpec = tween(
				durationMillis = AnimationDefaults.DURATION_MEDIUM,
				delayMillis = delay,
				easing = FastOutSlowInEasing,
			),
		)
	}

	return graphicsLayer {
		this.alpha = alpha.value
		translationY = offsetY.value
	}
}
