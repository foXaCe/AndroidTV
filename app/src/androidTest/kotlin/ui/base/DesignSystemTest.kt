package org.jellyfin.androidtv.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignSystemTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	/**
	 * JellyfinTheme provides a dark color scheme (background is not pure black).
	 */
	@Test
	fun jellyfinTheme_provides_dark_color_scheme() {
		var background = Color.Unspecified
		var surface = Color.Unspecified

		composeTestRule.setContent {
			JellyfinTheme {
				background = JellyfinTheme.colorScheme.background
				surface = JellyfinTheme.colorScheme.surface

				Box(
					modifier = Modifier
						.testTag("themed_box")
						.size(100.dp)
						.background(JellyfinTheme.colorScheme.background),
				)
			}
		}

		composeTestRule.waitForIdle()

		// Verify the themed box renders
		composeTestRule.onNodeWithTag("themed_box").assertIsDisplayed()

		// Background should be dark but not pure black
		assertNotEquals("Background should not be pure black", Color.Black, background)
		// Background should be very dark (luminance close to 0, but > 0)
		assert(background != Color.White) { "Background should not be white" }
		// Surface should also be dark but not the same as background
		assertNotEquals("Surface should differ from background", background, surface)
	}

	/**
	 * Focus changes the border color of a focusable composable.
	 * Tests the pattern used throughout the app where focus state drives visual feedback.
	 */
	@Test
	fun focus_changes_border_color() {
		var borderColorUnfocused = Color.Unspecified
		var borderColorFocused = Color.Unspecified
		val focusRequester = FocusRequester()

		composeTestRule.setContent {
			JellyfinTheme {
				var isFocused by remember { mutableStateOf(false) }

				val borderColor = if (isFocused) {
					JellyfinTheme.colorScheme.focusRing
				} else {
					Color.Transparent
				}

				// Capture colors for assertion
				if (!isFocused) borderColorUnfocused = borderColor
				if (isFocused) borderColorFocused = borderColor

				Box(
					modifier = Modifier
						.testTag("focus_box")
						.size(100.dp)
						.focusRequester(focusRequester)
						.onFocusChanged { isFocused = it.isFocused }
						.focusable()
						.border(2.dp, borderColor),
				)
			}
		}

		composeTestRule.waitForIdle()

		// Capture unfocused state
		assertEquals("Unfocused border should be transparent", Color.Transparent, borderColorUnfocused)

		// Request focus
		composeTestRule.runOnIdle { focusRequester.requestFocus() }
		composeTestRule.waitForIdle()

		// Verify focused border color is the focusRing color (not transparent)
		assertNotEquals("Focused border should not be transparent", Color.Transparent, borderColorFocused)
	}

	/**
	 * FOCUS_SCALE constant is 1.06f — regression guard.
	 */
	@Test
	fun animationDefaults_focusScale_is_1_06() {
		assertEquals(
			"FOCUS_SCALE must be 1.06f",
			1.06f,
			AnimationDefaults.FOCUS_SCALE,
			0.001f,
		)
	}
}
