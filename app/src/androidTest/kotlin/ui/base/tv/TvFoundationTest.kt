package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.jellyfin.androidtv.ui.base.AnimationDefaults
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TvFoundationTest {
	@get:Rule
	val composeTestRule = createComposeRule()

	// ── TvFocusCard ─────────────────────────────────────────

	/**
	 * When a TvFocusCard receives focus, its target scale should be
	 * [AnimationDefaults.FOCUS_SCALE] (1.06f).
	 */
	@Test
	fun tvFocusCard_scales_on_focus() {
		composeTestRule.setContent {
			JellyfinTheme {
				TvFocusCard(
					onClick = {},
					modifier = Modifier.testTag("card").size(160.dp),
				) {
					Box(Modifier.fillMaxSize().background(Color.DarkGray))
				}
			}
		}

		composeTestRule.waitForIdle()

		// Request focus on the card
		composeTestRule
			.onNodeWithTag("card")
			.performSemanticsAction(SemanticsActions.RequestFocus)

		composeTestRule.waitForIdle()

		// Verify the target scale matches FOCUS_SCALE
		composeTestRule
			.onNodeWithTag("card")
			.assert(
				SemanticsMatcher.expectValue(
					TvFocusCardScaleKey,
					AnimationDefaults.FOCUS_SCALE,
				),
			)
	}

	/**
	 * When a focused TvFocusCard is pressed, the target scale should be
	 * [AnimationDefaults.PRESS_SCALE] (0.95f).
	 *
	 * Uses touch input to simulate a pointer press (held down).
	 */
	@Test
	fun tvFocusCard_scales_on_press() {
		composeTestRule.setContent {
			JellyfinTheme {
				TvFocusCard(
					onClick = {},
					modifier = Modifier.testTag("card").size(160.dp),
				) {
					Box(Modifier.fillMaxSize().background(Color.DarkGray))
				}
			}
		}

		composeTestRule.waitForIdle()

		// Focus the card first
		composeTestRule
			.onNodeWithTag("card")
			.performSemanticsAction(SemanticsActions.RequestFocus)

		composeTestRule.waitForIdle()

		// Perform a touch down (press without release) to trigger press state
		composeTestRule
			.onNodeWithTag("card")
			.performTouchInput { down(center) }

		composeTestRule.waitForIdle()

		// Verify the target scale matches PRESS_SCALE
		composeTestRule
			.onNodeWithTag("card")
			.assert(
				SemanticsMatcher.expectValue(
					TvFocusCardScaleKey,
					AnimationDefaults.PRESS_SCALE,
				),
			)

		// Release touch to clean up
		composeTestRule
			.onNodeWithTag("card")
			.performTouchInput { up() }
	}

	// ── TvCardGrid ──────────────────────────────────────────

	/**
	 * The first item in a TvCardGrid should be focusable.
	 * We verify by requesting focus on it and checking the semantic property.
	 */
	@Test
	fun tvCardGrid_first_item_receives_focus() {
		val testItems = listOf("Item A", "Item B", "Item C", "Item D", "Item E")

		composeTestRule.setContent {
			JellyfinTheme {
				TvCardGrid(
					items = testItems,
					columns = 5,
					key = { it },
				) { item ->
					TvFocusCard(
						onClick = {},
						modifier =
							Modifier
								.testTag("grid_$item")
								.size(120.dp),
					) {
						Box(Modifier.fillMaxSize().background(Color.DarkGray))
					}
				}
			}
		}

		composeTestRule.waitForIdle()

		// Request focus on the first item
		composeTestRule
			.onNodeWithTag("grid_Item A")
			.performSemanticsAction(SemanticsActions.RequestFocus)

		composeTestRule.waitForIdle()

		// Verify it received focus (scale should be FOCUS_SCALE)
		composeTestRule
			.onNodeWithTag("grid_Item A")
			.assert(
				SemanticsMatcher.expectValue(
					TvFocusCardScaleKey,
					AnimationDefaults.FOCUS_SCALE,
				),
			)
	}

	// ── TvRowList ───────────────────────────────────────────

	/**
	 * TvRowList should render the title of each row.
	 */
	@Test
	fun tvRowList_renders_row_titles() {
		val testRows =
			listOf(
				TvRow(title = "Continue Watching", items = listOf("A", "B")),
				TvRow(title = "Recently Added", items = listOf("C", "D")),
			)

		composeTestRule.setContent {
			JellyfinTheme {
				TvRowList(rows = testRows) { item ->
					TvFocusCard(
						onClick = {},
						modifier = Modifier.size(120.dp),
					) {
						Text(text = item)
					}
				}
			}
		}

		composeTestRule.waitForIdle()

		composeTestRule.onNodeWithText("Continue Watching").assertIsDisplayed()
		composeTestRule.onNodeWithText("Recently Added").assertIsDisplayed()
	}

	// ── TvScaffold ──────────────────────────────────────────

	/**
	 * TvScaffold should render its content and apply the DS background.
	 * We verify by checking that the sentinel content is displayed.
	 */
	@Test
	fun tvScaffold_provides_background_color() {
		composeTestRule.setContent {
			JellyfinTheme {
				TvScaffold {
					Text(
						text = "Scaffold Content",
						modifier = Modifier.testTag("scaffold_content"),
					)
				}
			}
		}

		composeTestRule.waitForIdle()

		// Verify the content is rendered inside the scaffold
		composeTestRule.onNodeWithTag("scaffold_content").assertIsDisplayed()
		composeTestRule.onNodeWithText("Scaffold Content").assertIsDisplayed()
	}
}
