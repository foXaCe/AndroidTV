package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme

/**
 * TV overscan safe area insets.
 * 5% of 1920×1080 ≈ 48dp horizontal, 27dp vertical.
 */
object TvSafeArea {
	val horizontal = 48.dp
	val vertical = 27.dp
}

/**
 * Base scaffold for all TV Compose screens.
 *
 * Provides:
 * - Full-screen background from the design system ([JellyfinTheme.colorScheme.background])
 * - TV overscan safe area padding (5% insets)
 * - Focus group with optional focus restoration
 *
 * @param focusRestorer When true, restores focus to the last focused child
 *   when focus re-enters this screen (e.g. after navigating back).
 */
@Composable
fun TvScaffold(
	modifier: Modifier = Modifier,
	focusRestorer: Boolean = true,
	content: @Composable BoxScope.() -> Unit,
) {
	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(JellyfinTheme.colorScheme.background)
				.padding(horizontal = TvSafeArea.horizontal, vertical = TvSafeArea.vertical)
				.then(if (focusRestorer) Modifier.focusRestorer() else Modifier)
				.focusGroup(),
		content = content,
	)
}
