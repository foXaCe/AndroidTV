package org.jellyfin.androidtv.ui.base.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import kotlin.math.sin

/**
 * State holder for a shake animation. Call [shake] to trigger a horizontal shake
 * (3 oscillations, 200ms) — useful for indicating an invalid action.
 */
class ShakeState {
	internal val offset = Animatable(0f)

	suspend fun shake() {
		offset.animateTo(
			targetValue = 1f,
			animationSpec = tween(durationMillis = 200),
		)
		offset.snapTo(0f)
	}
}

@Composable
fun rememberShakeState(): ShakeState = remember { ShakeState() }

/**
 * Applies a horizontal shake effect when [ShakeState.shake] is called.
 * 3 oscillations with 6dp amplitude over 200ms.
 */
fun Modifier.shake(state: ShakeState): Modifier = graphicsLayer {
	val progress = state.offset.value
	if (progress > 0f) {
		// 3 oscillations using sin: sin(3 * 2PI * progress) with decaying amplitude
		val amplitude = 6f * (1f - progress)
		translationX = amplitude * sin(progress * 3f * 2f * Math.PI.toFloat())
	}
}
