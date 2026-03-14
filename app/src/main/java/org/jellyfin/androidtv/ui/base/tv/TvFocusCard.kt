package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.rememberReducedMotion

/** Semantic property exposing the current target scale for testing. */
val TvFocusCardScaleKey = SemanticsPropertyKey<Float>("TvFocusCardScale")
var SemanticsPropertyReceiver.tvFocusCardScale by TvFocusCardScaleKey

/**
 * Focus-aware card wrapper for TV — the building block for media items.
 * Replaces Leanback's ImageCardView + CardPresenter.
 *
 * Uses [Surface] from tv-material3 which provides native TV focus handling:
 * - Scale 1.06× on focus ([AnimationDefaults.FOCUS_SCALE])
 * - Scale 0.95× on press ([AnimationDefaults.PRESS_SCALE])
 * - Visible focus border using [focusColor]
 * - Respects reduced-motion accessibility setting
 *
 * @param onClick Called when the card is clicked (D-pad center / enter).
 * @param focusedScale Scale factor when focused (default: [AnimationDefaults.FOCUS_SCALE]).
 * @param focusColor Border color when focused (default: [JellyfinTheme.colorScheme.focusRing]).
 * @param shape Card shape (default: [JellyfinTheme.shapes.medium]).
 * @param content Card content composable (poster image, title overlay, etc.).
 */
@Composable
fun TvFocusCard(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	focusedScale: Float = AnimationDefaults.FOCUS_SCALE,
	focusColor: Color = JellyfinTheme.colorScheme.focusRing,
	shape: CornerBasedShape = JellyfinTheme.shapes.medium,
	content: @Composable () -> Unit,
) {
	val reducedMotion = rememberReducedMotion()
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val isPressed by interactionSource.collectIsPressedAsState()

	// Expose target scale for testing
	val targetScale =
		when {
			reducedMotion -> 1f
			isPressed -> AnimationDefaults.PRESS_SCALE
			isFocused -> focusedScale
			else -> 1f
		}

	val effectiveFocusedScale = if (reducedMotion) 1f else focusedScale
	val effectivePressedScale = if (reducedMotion) 1f else AnimationDefaults.PRESS_SCALE

	Surface(
		onClick = onClick,
		modifier = modifier.semantics { tvFocusCardScale = targetScale },
		scale =
			ClickableSurfaceDefaults.scale(
				focusedScale = effectiveFocusedScale,
				pressedScale = effectivePressedScale,
			),
		border =
			ClickableSurfaceDefaults.border(
				focusedBorder =
					Border(
						border = BorderStroke(2.dp, focusColor),
						shape = shape,
					),
			),
		shape = ClickableSurfaceDefaults.shape(shape = shape),
		colors =
			ClickableSurfaceDefaults.colors(
				containerColor = Color.Transparent,
				focusedContainerColor = Color.Transparent,
				pressedContainerColor = Color.Transparent,
			),
		interactionSource = interactionSource,
	) {
		content()
	}
}
