package org.jellyfin.androidtv.ui.browsing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.focusBorderColor

@Composable
fun ExitConfirmationDialog(
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	val initialFocusRequester = remember { FocusRequester() }
	val accentColor = focusBorderColor()

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 340.dp, max = 440.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogScrim)
					.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
					.padding(vertical = 20.dp),
			) {
				Text(
					text = stringResource(R.string.exit_confirmation_title),
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.onSurface,
					modifier = Modifier
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
				)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Text(
					text = stringResource(R.string.exit_confirmation_message),
					style = JellyfinTheme.typography.bodyLarge,
					color = JellyfinTheme.colorScheme.textSecondary,
					modifier = Modifier
						.padding(horizontal = 24.dp)
						.padding(top = 16.dp, bottom = 16.dp),
				)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(8.dp))

				val exitInteractionSource = remember { MutableInteractionSource() }
				val exitFocused by exitInteractionSource.collectIsFocusedAsState()

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.focusRequester(initialFocusRequester)
						.clickable(
							interactionSource = exitInteractionSource,
							indication = null,
						) { onConfirm() }
						.focusable(interactionSource = exitInteractionSource)
						.background(
							if (exitFocused) JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent,
						)
						.padding(horizontal = 24.dp, vertical = 14.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(R.string.lbl_exit),
						style = JellyfinTheme.typography.titleMedium,
						color = if (exitFocused) accentColor else JellyfinTheme.colorScheme.textSecondary,
					)
				}

				val cancelInteractionSource = remember { MutableInteractionSource() }
				val cancelFocused by cancelInteractionSource.collectIsFocusedAsState()

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable(
							interactionSource = cancelInteractionSource,
							indication = null,
						) { onDismiss() }
						.focusable(interactionSource = cancelInteractionSource)
						.background(
							if (cancelFocused) JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent,
						)
						.padding(horizontal = 24.dp, vertical = 14.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = stringResource(android.R.string.cancel),
						style = JellyfinTheme.typography.titleMedium,
						color = if (cancelFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textSecondary,
					)
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}
