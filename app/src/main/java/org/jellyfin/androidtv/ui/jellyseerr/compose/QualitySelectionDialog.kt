package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

@Composable
fun QualitySelectionDialog(
	title: String,
	canRequestHd: Boolean,
	canRequest4k: Boolean,
	hdStatus: Int?,
	status4k: Int?,
	onSelect: (is4k: Boolean) -> Unit,
	onDismiss: () -> Unit,
) {
	val focusRequester = remember { FocusRequester() }

	val isHdBlocked = !canRequestHd || isStatusBlocked(hdStatus)
	val is4kBlocked = !canRequest4k || isStatusBlocked(status4k)

	val firstFocusTarget = when {
		!isHdBlocked -> FocusTarget.HD
		!is4kBlocked -> FocusTarget.FOUR_K
		else -> FocusTarget.CANCEL
	}

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
					.widthIn(min = 380.dp, max = 500.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.surface)
					.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
					.padding(horizontal = 32.dp, vertical = 24.dp),
			) {
				Text(
					text = stringResource(R.string.jellyseerr_select_quality),
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.textPrimary,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
				)

				Text(
					text = title,
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textSecondary,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
				)

				// HD button
				QualityButton(
					text = getQualityLabel(false, hdStatus),
					enabled = !isHdBlocked,
					isPrimary = true,
					focusRequester = if (firstFocusTarget == FocusTarget.HD) focusRequester else null,
					onClick = { onSelect(false); onDismiss() },
				)

				Spacer(modifier = Modifier.height(12.dp))

				// 4K button
				QualityButton(
					text = getQualityLabel(true, status4k),
					enabled = !is4kBlocked,
					isPrimary = true,
					focusRequester = if (firstFocusTarget == FocusTarget.FOUR_K) focusRequester else null,
					onClick = { onSelect(true); onDismiss() },
				)

				Spacer(modifier = Modifier.height(24.dp))

				// Cancel button
				QualityButton(
					text = stringResource(R.string.btn_cancel),
					enabled = true,
					isPrimary = false,
					focusRequester = if (firstFocusTarget == FocusTarget.CANCEL) focusRequester else null,
					onClick = onDismiss,
				)
			}
		}

		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}
	}
}

@Composable
private fun QualityButton(
	text: String,
	enabled: Boolean,
	isPrimary: Boolean,
	focusRequester: FocusRequester?,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val backgroundColor = when {
		!enabled -> JellyfinTheme.colorScheme.surfaceBright
		isFocused && isPrimary -> JellyfinTheme.colorScheme.secondary
		isFocused && !isPrimary -> JellyfinTheme.colorScheme.surfaceContainer
		isPrimary -> JellyfinTheme.colorScheme.secondary
		else -> JellyfinTheme.colorScheme.surfaceContainer
	}

	val focusModifier = if (focusRequester != null) {
		Modifier.focusRequester(focusRequester)
	} else {
		Modifier
	}

	Box(
		modifier = focusModifier
			.fillMaxWidth()
			.clip(JellyfinTheme.shapes.small)
			.background(backgroundColor)
			.then(
				if (enabled) Modifier
					.clickable(interactionSource = interactionSource, indication = null) { onClick() }
					.focusable(interactionSource = interactionSource)
				else Modifier
			)
			.padding(horizontal = 24.dp, vertical = 16.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = JellyfinTheme.typography.titleMedium,
			color = if (enabled) JellyfinTheme.colorScheme.textPrimary else JellyfinTheme.colorScheme.textDisabled,
			textAlign = TextAlign.Center,
			modifier = if (!enabled) Modifier.then(Modifier) else Modifier,
		)
	}
}

@Composable
private fun getQualityLabel(is4k: Boolean, status: Int?): String {
	val qualityPrefix = if (is4k) "4K" else "HD"
	return when (status) {
		2 -> stringResource(R.string.jellyseerr_quality_pending, qualityPrefix)
		3 -> stringResource(R.string.jellyseerr_quality_processing, qualityPrefix)
		4 -> stringResource(R.string.jellyseerr_quality_request_more, qualityPrefix)
		5 -> stringResource(R.string.jellyseerr_quality_available, qualityPrefix)
		6 -> stringResource(R.string.jellyseerr_quality_blacklisted, qualityPrefix)
		else -> stringResource(R.string.jellyseerr_quality_request, qualityPrefix)
	}
}

private fun isStatusBlocked(status: Int?): Boolean {
	return status != null && status >= 2 && status != 4
}

private enum class FocusTarget { HD, FOUR_K, CANCEL }
