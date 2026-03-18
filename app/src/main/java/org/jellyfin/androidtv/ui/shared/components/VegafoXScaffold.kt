package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.ui.home.compose.sidebar.PremiumSideBar
import org.jellyfin.androidtv.ui.home.compose.sidebar.SidebarState

/**
 * App-wide scaffold with the premium sidebar.
 *
 * Wraps any screen content in a `Row` with `PremiumSideBar` on the left
 * and the content on the right. The sidebar opens via double-press D-pad left
 * when focus is at the left edge (cannot move further left).
 *
 * Uses [focusGroup] + [focusProperties.exit] so the handler only fires when
 * focus genuinely cannot move left within the content area.
 *
 * @param sidebarEnabled When false, the sidebar mechanism is entirely disabled
 *   (used for player, dialogs, startup screens).
 * @param content The screen content to display.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun VegafoXScaffold(
	sidebarEnabled: Boolean = true,
	content: @Composable () -> Unit,
) {
	if (!sidebarEnabled) {
		content()
		return
	}

	var sidebarState by remember { mutableStateOf(SidebarState.HIDDEN) }
	val sidebarHomeFocusRequester = remember { FocusRequester() }
	var prevSidebarState by remember { mutableStateOf(SidebarState.HIDDEN) }

	// Double-press threshold: sidebar opens only on 2nd consecutive D-pad left within 500ms
	var leftPressCount by remember { mutableIntStateOf(0) }
	var leftPressResetJob by remember { mutableStateOf<Job?>(null) }
	val sidebarScope = rememberCoroutineScope()

	// Content focus requester for restoring focus when sidebar hides
	val contentFocusRequester = remember { FocusRequester() }

	// Restore content focus when sidebar hides
	LaunchedEffect(sidebarState) {
		if (sidebarState == SidebarState.HIDDEN && prevSidebarState != SidebarState.HIDDEN) {
			try {
				contentFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
		prevSidebarState = sidebarState
	}

	Row(modifier = Modifier.fillMaxSize()) {
		PremiumSideBar(
			state = sidebarState,
			onStateChange = { sidebarState = it },
			homeFocusRequester = sidebarHomeFocusRequester,
			modifier = Modifier.fillMaxHeight(),
		)

		Box(
			modifier =
				Modifier
					.weight(1f)
					.fillMaxHeight()
					.focusRequester(contentFocusRequester)
					.focusProperties {
						exit = { direction ->
							if (direction == FocusDirection.Left &&
								sidebarState == SidebarState.HIDDEN
							) {
								leftPressCount++
								if (leftPressCount >= 2) {
									leftPressResetJob?.cancel()
									leftPressCount = 0
									sidebarState = SidebarState.COMPACT
								} else {
									leftPressResetJob?.cancel()
									leftPressResetJob =
										sidebarScope.launch {
											delay(500)
											leftPressCount = 0
										}
								}
								FocusRequester.Cancel
							} else {
								FocusRequester.Default
							}
						}
					}.focusGroup(),
		) {
			content()
		}
	}
}
