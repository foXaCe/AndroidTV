package org.jellyfin.androidtv.ui.base.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateContainerTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	/** Resolve the localized "Retry" label from string resources. */
	private val retryLabel: String by lazy {
		InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.lbl_retry)
	}

	/**
	 * LOADING state shows the skeleton content, not the real content.
	 */
	@Test
	fun loading_state_shows_skeleton_not_content() {
		composeTestRule.setContent {
			JellyfinTheme {
				StateContainer(
					state = DisplayState.LOADING,
					loadingContent = {
						Box(Modifier.testTag("skeleton").size(100.dp))
					},
					content = {
						Box(Modifier.testTag("content").size(100.dp))
					},
				)
			}
		}

		composeTestRule.onNodeWithTag("skeleton").assertExists()
		composeTestRule.onNodeWithTag("content").assertDoesNotExist()
	}

	/**
	 * CONTENT state shows the real content, not the skeleton.
	 */
	@Test
	fun content_state_shows_content_not_skeleton() {
		composeTestRule.setContent {
			JellyfinTheme {
				StateContainer(
					state = DisplayState.CONTENT,
					loadingContent = {
						Box(Modifier.testTag("skeleton").size(100.dp))
					},
					content = {
						Box(Modifier.testTag("content").size(100.dp))
					},
				)
			}
		}

		composeTestRule.onNodeWithTag("content").assertExists()
		composeTestRule.onNodeWithTag("skeleton").assertDoesNotExist()
	}

	/**
	 * ERROR state shows the error message and a Retry button.
	 * Uses testTag on ErrorState wrapper to verify the composable renders.
	 */
	@Test
	fun error_state_shows_error_with_retry_button() {
		composeTestRule.setContent {
			JellyfinTheme {
				Box(Modifier.testTag("error_wrapper")) {
					ErrorState(
						message = "Unable to connect. Check your network connection.",
						onRetry = {},
					)
				}
			}
		}

		composeTestRule.waitForIdle()

		// Check the wrapper exists (composition didn't crash)
		composeTestRule.onNodeWithTag("error_wrapper").assertExists()

		// Search in unmerged tree for text nodes
		composeTestRule
			.onNodeWithText("Unable to connect. Check your network connection.", useUnmergedTree = true)
			.assertExists()
		composeTestRule
			.onNodeWithText(retryLabel, useUnmergedTree = true)
			.assertExists()
	}

	/**
	 * Clicking the Retry button triggers the onRetry callback.
	 */
	@Test
	fun retry_button_click_calls_callback() {
		var retried = false

		composeTestRule.setContent {
			JellyfinTheme {
				ErrorState(
					message = "Error occurred",
					onRetry = { retried = true },
				)
			}
		}

		composeTestRule.waitForIdle()

		// Try merged tree first, fall back to unmerged
		val retryNode = composeTestRule.onNodeWithText(retryLabel, useUnmergedTree = true)
		retryNode.assertExists()
		retryNode.performSemanticsAction(SemanticsActions.OnClick)

		composeTestRule.waitForIdle()
		assert(retried) { "Expected onRetry callback to be called" }
	}

	/**
	 * EmptyState shows both the title and the message.
	 */
	@Test
	fun empty_state_shows_title_and_message() {
		composeTestRule.setContent {
			JellyfinTheme {
				EmptyState(
					title = "Your library is empty",
					message = "Add media to your server to see it here",
				)
			}
		}

		composeTestRule
			.onNodeWithText("Your library is empty")
			.assertExists()
		composeTestRule
			.onNodeWithText("Add media to your server to see it here")
			.assertExists()
	}
}
