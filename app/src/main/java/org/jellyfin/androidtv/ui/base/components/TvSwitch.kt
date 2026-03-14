package org.jellyfin.androidtv.ui.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text

@Composable
fun TvSettingsToggle(
	title: String,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
	subtitle: String? = null,
	enabled: Boolean = true,
) {
	var isFocused by remember { mutableStateOf(false) }

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.defaultMinSize(minHeight = 56.dp)
				.onFocusChanged { isFocused = it.isFocused }
				.onKeyEvent { event ->
					if (event.type == KeyEventType.KeyUp &&
						(event.key == Key.DirectionCenter || event.key == Key.Enter)
					) {
						onCheckedChange(!checked)
						true
					} else {
						false
					}
				}.background(
					color = if (isFocused) JellyfinTheme.colorScheme.surfaceBright else Color.Transparent,
					shape = JellyfinTheme.shapes.small,
				).padding(horizontal = 16.dp, vertical = 12.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.Center,
		) {
			Text(
				text = title,
				style = JellyfinTheme.typography.bodyLarge,
				color = if (enabled) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.onSurfaceVariant,
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = JellyfinTheme.typography.bodySmall,
					color = JellyfinTheme.colorScheme.onSurfaceVariant,
				)
			}
		}

		Switch(
			checked = checked,
			onCheckedChange = null,
			enabled = enabled,
			modifier = Modifier.scale(1.2f),
			colors =
				SwitchDefaults.colors(
					checkedThumbColor = JellyfinTheme.colorScheme.onPrimary,
					checkedTrackColor = JellyfinTheme.colorScheme.primary,
					uncheckedThumbColor = JellyfinTheme.colorScheme.onSurfaceVariant,
					uncheckedTrackColor = JellyfinTheme.colorScheme.surfaceContainer,
					uncheckedBorderColor = JellyfinTheme.colorScheme.onSurfaceVariant,
				),
		)
	}
}
