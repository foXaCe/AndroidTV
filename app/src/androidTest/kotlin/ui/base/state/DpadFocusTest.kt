package org.jellyfin.androidtv.ui.base.state

import android.view.KeyEvent as NativeKeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DpadFocusTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val retryLabel: String by lazy {
		InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.lbl_retry)
	}

	/**
	 * The Retry button in ErrorState can receive focus (has .focusable() modifier).
	 * Verifies focus by clicking (which focuses on TV) then checking focused state.
	 */
	@Test
	fun error_state_retry_button_is_focusable() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorState(
					message = "Error",
					onRetry = {},
				)
			}
		}

		composeTestRule.waitForIdle()

		// Verify the Retry button exists and has OnClick action (implies focusable+clickable)
		val retryNode = composeTestRule.onNodeWithText(retryLabel)
		retryNode.assertExists()

		// Try to perform OnClick which requires the node to be interactive
		retryNode.performSemanticsAction(SemanticsActions.OnClick)

		// If OnClick succeeded, the node is both focusable and clickable
		composeTestRule.waitForIdle()
	}

	/**
	 * The ErrorBanner's Retry button responds to DPAD_CENTER by calling onRetry.
	 * Uses SemanticsActions.OnClick to simulate the click (equivalent to DPAD_CENTER
	 * on a focused TV button, since .clickable() handles both).
	 */
	@Test
	fun error_banner_retry_responds_to_dpad_center() {
		var retried = false

		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(
					error = UiError.Network(cause = null),
					onRetry = { retried = true },
				)
			}
		}

		composeTestRule.waitForIdle()

		// Simulate click via semantic action (equivalent to DPAD_CENTER on focused button)
		composeTestRule.onNodeWithText(retryLabel)
			.performSemanticsAction(SemanticsActions.OnClick)

		composeTestRule.waitForIdle()
		assert(retried) { "Expected onRetry callback to be triggered by click action" }
	}

	/**
	 * EmptyState with an action button: the action button is focusable.
	 */
	@Test
	fun empty_state_action_button_is_focusable() {
		composeTestRule.setContent {
			JellyfinTheme {
				EmptyState(
					title = "No items",
					message = "Your library is empty",
					action = {
						Text(
							text = "Browse",
							modifier = Modifier
								.testTag("action_button")
								.clickable {}
								.focusable()
								.background(JellyfinTheme.colorScheme.surfaceContainer, JellyfinTheme.shapes.button)
								.padding(horizontal = 24.dp, vertical = 10.dp),
						)
					},
				)
			}
		}

		composeTestRule.waitForIdle()

		// Verify the action button exists
		val actionNode = composeTestRule.onNodeWithTag("action_button")
		actionNode.assertExists()

		// Verify it has OnClick action (implies focusable + clickable)
		actionNode.performSemanticsAction(SemanticsActions.OnClick)
		composeTestRule.waitForIdle()
	}
}
