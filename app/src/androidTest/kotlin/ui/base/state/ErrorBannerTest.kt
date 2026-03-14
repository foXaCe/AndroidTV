package org.jellyfin.androidtv.ui.base.state

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorBannerTest {
	@get:Rule
	val composeTestRule = createComposeRule()

	private val context by lazy {
		InstrumentationRegistry.getInstrumentation().targetContext
	}

	private val retryLabel: String by lazy { context.getString(R.string.lbl_retry) }
	private val networkErrorMsg: String by lazy { context.getString(R.string.state_error_network) }
	private val authErrorMsg: String by lazy { context.getString(R.string.state_error_auth) }

	/**
	 * ErrorBanner with null error renders nothing.
	 */
	@Test
	fun null_error_renders_nothing() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(error = null)
			}
		}

		composeTestRule.waitForIdle()

		// Nothing should be rendered — no error text, no retry button
		composeTestRule.onNodeWithText(retryLabel).assertDoesNotExist()
		composeTestRule.onNodeWithText(networkErrorMsg).assertDoesNotExist()
	}

	/**
	 * Network error shows the correct localized message.
	 */
	@Test
	fun network_error_shows_correct_message() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(error = UiError.Network())
			}
		}

		composeTestRule.waitForIdle()

		composeTestRule
			.onNodeWithText(networkErrorMsg, useUnmergedTree = true)
			.assertIsDisplayed()
	}

	/**
	 * Auth error shows the session expired message.
	 */
	@Test
	fun auth_error_shows_correct_message() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(error = UiError.Auth())
			}
		}

		composeTestRule.waitForIdle()

		composeTestRule
			.onNodeWithText(authErrorMsg, useUnmergedTree = true)
			.assertIsDisplayed()
	}

	/**
	 * Retry button is visible when onRetry callback is provided.
	 */
	@Test
	fun retry_button_visible_when_callback_provided() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(
					error = UiError.Network(),
					onRetry = {},
				)
			}
		}

		composeTestRule.waitForIdle()

		composeTestRule
			.onNodeWithText(retryLabel, useUnmergedTree = true)
			.assertExists()
	}

	/**
	 * No retry button when onRetry is null.
	 */
	@Test
	fun no_retry_button_when_no_callback() {
		composeTestRule.setContent {
			JellyfinTheme {
				ErrorBanner(
					error = UiError.Network(),
					onRetry = null,
				)
			}
		}

		composeTestRule.waitForIdle()

		composeTestRule
			.onNodeWithText(retryLabel, useUnmergedTree = true)
			.assertDoesNotExist()
	}
}
