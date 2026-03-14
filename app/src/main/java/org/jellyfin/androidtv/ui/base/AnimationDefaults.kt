package org.jellyfin.androidtv.ui.base

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object AnimationDefaults {
	// Duration (ms)
	const val DURATION_FAST = 150
	const val DURATION_MEDIUM = 300
	const val DURATION_SLOW = 500

	// Focus scale factor (uniform across all components)
	const val FOCUS_SCALE = 1.06f

	// Press scale factor (button press feedback)
	const val PRESS_SCALE = 0.95f

	// Leanback card scale (kept at 1.15 for Leanback row compatibility)
	const val LEANBACK_FOCUS_SCALE = 1.15f

	// Alpha values
	const val SCRIM_ALPHA = 0.67f
	const val DISABLED_ALPHA = 0.38f
	const val MEDIUM_EMPHASIS_ALPHA = 0.60f
	const val HIGH_EMPHASIS_ALPHA = 0.87f

	// Image crossfade duration (ms) — set globally on ImageLoader
	const val IMAGE_CROSSFADE = 200

	// Stagger delay (ms) between list items on first load
	const val STAGGER_DELAY = 30

	// Focus animation spec: 150ms FastOutSlowIn
	fun <T> focusSpec() =
		tween<T>(
			durationMillis = DURATION_FAST,
			easing = FastOutSlowInEasing,
		)
}

/**
 * Returns true if the user has disabled or reduced animations in Android settings.
 * When true, all non-essential animations should be skipped.
 */
@Composable
fun rememberReducedMotion(): Boolean {
	val context = LocalContext.current
	return remember {
		val scale =
			Settings.Global.getFloat(
				context.contentResolver,
				Settings.Global.ANIMATOR_DURATION_SCALE,
				1f,
			)
		scale == 0f
	}
}
