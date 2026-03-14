package org.jellyfin.androidtv.ui.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.player.base.PlayerOverlayLayout
import org.jellyfin.androidtv.ui.player.base.rememberPlayerOverlayVisibility
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.view.KeyEvent as NativeKeyEvent

@RunWith(AndroidJUnit4::class)
class PlayerOverlayTest {
	@get:Rule
	val composeTestRule = createComposeRule()

	/**
	 * Overlay controls are hidden by default (visibility starts false).
	 */
	@Test
	fun overlay_hidden_by_default() {
		composeTestRule.setContent {
			JellyfinTheme {
				val state = rememberPlayerOverlayVisibility()
				PlayerOverlayLayout(
					visibilityState = state,
					controls = {
						Box(Modifier.testTag("controls").size(100.dp))
					},
				)
			}
		}

		composeTestRule.onNodeWithTag("controls").assertDoesNotExist()
	}

	/**
	 * Pressing D-pad UP on the overlay shows the controls.
	 */
	@Test
	fun dpad_action_shows_overlay() {
		composeTestRule.setContent {
			JellyfinTheme {
				val state = rememberPlayerOverlayVisibility()
				PlayerOverlayLayout(
					modifier = Modifier.testTag("overlay"),
					visibilityState = state,
					controls = {
						Box(Modifier.testTag("controls").size(100.dp))
					},
				)
			}
		}

		// Initially hidden
		composeTestRule.onNodeWithTag("controls").assertDoesNotExist()

		// Focus the overlay and send D-pad UP
		composeTestRule
			.onNodeWithTag("overlay")
			.performSemanticsAction(SemanticsActions.RequestFocus)

		composeTestRule
			.onNodeWithTag("overlay")
			.performKeyPress(
				KeyEvent(NativeKeyEvent(NativeKeyEvent.ACTION_DOWN, NativeKeyEvent.KEYCODE_DPAD_UP)),
			)

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithTag("controls").assertExists()
	}

	/**
	 * Overlay auto-hides after the 3-second timeout.
	 */
	@Test
	fun overlay_autohides_after_3_seconds() {
		lateinit var showOverlay: () -> Unit

		composeTestRule.setContent {
			JellyfinTheme {
				val state = rememberPlayerOverlayVisibility()
				showOverlay = state.show
				PlayerOverlayLayout(
					visibilityState = state,
					controls = {
						Box(Modifier.testTag("controls").size(100.dp))
					},
				)
			}
		}

		composeTestRule.waitForIdle()

		// Initially hidden
		composeTestRule.onNodeWithTag("controls").assertDoesNotExist()

		// Show overlay programmatically
		composeTestRule.runOnIdle { showOverlay() }
		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithTag("controls").assertExists()

		// Wait for auto-hide (3s timeout + exit animation)
		composeTestRule.waitUntil(timeoutMillis = 6000) {
			composeTestRule
				.onAllNodesWithTag("controls")
				.fetchSemanticsNodes()
				.isEmpty()
		}
	}

	/**
	 * Validates the seekbar height animation pattern used in VideoPlayerControls:
	 * unfocused = 3dp, focused = 8dp.
	 * Uses a FocusRequester for reliable focus control.
	 */
	@Test
	fun seekbar_height_increases_on_focus() {
		val focusRequester = FocusRequester()

		composeTestRule.setContent {
			JellyfinTheme {
				var seekbarFocused by remember { mutableStateOf(false) }
				val seekbarHeight by animateDpAsState(
					targetValue = if (seekbarFocused) 8.dp else 3.dp,
					animationSpec = tween(AnimationDefaults.DURATION_FAST),
					label = "seekbarHeight",
				)

				Box(
					modifier =
						Modifier
							.testTag("seekbar")
							.fillMaxWidth()
							.height(seekbarHeight)
							.focusRequester(focusRequester)
							.onFocusChanged { seekbarFocused = it.hasFocus || it.isFocused }
							.focusable(),
				)
			}
		}

		composeTestRule.waitForIdle()

		// Initial height is 3dp
		composeTestRule
			.onNodeWithTag("seekbar")
			.assertHeightIsEqualTo(3.dp)

		// Focus the seekbar via FocusRequester
		composeTestRule.runOnIdle { focusRequester.requestFocus() }
		composeTestRule.waitForIdle()

		// Height should increase beyond 3dp (target: 8dp)
		composeTestRule
			.onNodeWithTag("seekbar")
			.assertHeightIsAtLeast(4.dp)
	}
}
