package org.jellyfin.androidtv.ui.playback.overlay.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

@Composable
fun PlayerDialog(
	title: String,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit,
) {
	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = modifier
					.widthIn(min = 380.dp, max = 500.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogSurface)
					.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
					.padding(vertical = 24.dp),
			) {
				Text(
					text = title,
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.textPrimary,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(start = 32.dp, end = 32.dp, bottom = 16.dp),
				)

				content()
			}
		}
	}
}

@Composable
fun PlayerDialogItem(
	text: String,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val backgroundColor = when {
		isFocused -> JellyfinTheme.colorScheme.surfaceContainer
		else -> JellyfinTheme.colorScheme.dialogSurface
	}

	val textColor = when {
		isSelected -> JellyfinTheme.colorScheme.primary
		else -> JellyfinTheme.colorScheme.textPrimary
	}

	val scale = if (isFocused) 1.04f else 1f

	Row(
		modifier = modifier
			.fillMaxWidth()
			.height(56.dp)
			.scale(scale)
			.background(backgroundColor)
			.then(
				if (isFocused) Modifier.border(
					2.dp,
					JellyfinTheme.colorScheme.focusRing,
					JellyfinTheme.shapes.small,
				) else Modifier
			)
			.clip(JellyfinTheme.shapes.small)
			.clickable(interactionSource = interactionSource, indication = null) { onClick() }
			.focusable(interactionSource = interactionSource)
			.padding(horizontal = 32.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = text,
				style = JellyfinTheme.typography.bodyLarge,
				color = textColor,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.bodySmall,
					color = JellyfinTheme.colorScheme.textSecondary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}

		if (isSelected) {
			Spacer(Modifier.width(12.dp))
			Text(
				text = "\u2713",
				style = JellyfinTheme.typography.titleMedium,
				color = JellyfinTheme.colorScheme.primary,
			)
		}
	}
}

@Composable
fun PlayerDialogStepper(
	title: String,
	value: String,
	onDecrement: () -> Unit,
	onIncrement: () -> Unit,
	onReset: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = value,
			style = JellyfinTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
			color = JellyfinTheme.colorScheme.primary,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(vertical = 24.dp),
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
			verticalAlignment = Alignment.CenterVertically,
		) {
			StepperButton(
				text = "\u2212",
				onClick = onDecrement,
				modifier = Modifier.size(48.dp),
			)

			StepperButton(
				text = "Reset",
				onClick = onReset,
				modifier = Modifier
					.height(48.dp)
					.widthIn(min = 80.dp),
			)

			StepperButton(
				text = "+",
				onClick = onIncrement,
				modifier = Modifier.size(48.dp),
			)
		}
	}
}

@Composable
private fun StepperButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val backgroundColor = when {
		isFocused -> JellyfinTheme.colorScheme.surfaceContainer
		else -> JellyfinTheme.colorScheme.surfaceBright
	}

	Box(
		modifier = modifier
			.clip(JellyfinTheme.shapes.small)
			.background(backgroundColor)
			.then(
				if (isFocused) Modifier.border(
					2.dp,
					JellyfinTheme.colorScheme.focusRing,
					JellyfinTheme.shapes.small,
				) else Modifier
			)
			.clickable(interactionSource = interactionSource, indication = null) { onClick() }
			.focusable(interactionSource = interactionSource),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = JellyfinTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
			color = JellyfinTheme.colorScheme.textPrimary,
		)
	}
}
