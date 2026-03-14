package org.jellyfin.androidtv.ui.settings.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.RadioButton
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListControlDefaults
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.preference.Preference
import org.jellyfin.preference.store.PreferenceStore

/**
 * Declarative entry for a single-preference settings screen with a list of options.
 */
class SettingsEntry<T : Any>(
	val preference: Preference<T>,
	@StringRes val titleRes: Int,
	val options: List<T>,
	val label: @Composable (T) -> String,
	@StringRes val overlineRes: Int? = null,
	@StringRes val descriptionRes: Int? = null,
)

/**
 * Generic composable that renders a full settings screen from a [SettingsEntry].
 * Displays a section header followed by radio-button options that write back to the preference store.
 */
@Composable
fun <ME, MV, T : Any> OptionListScreen(
	entry: SettingsEntry<T>,
	store: PreferenceStore<ME, MV>,
) {
	val router = LocalRouter.current
	var value by rememberPreference(store, entry.preference)

	SettingsColumn {
		item {
			ListSection(
				overlineContent = entry.overlineRes?.let { res -> { Text(stringResource(res).uppercase()) } },
				headingContent = { Text(stringResource(entry.titleRes)) },
				captionContent = entry.descriptionRes?.let { res -> { Text(stringResource(res)) } },
			)
		}

		items(entry.options) { option ->
			val isSelected = value == option
			val optionLabel = entry.label(option)
			ListButton(
				headingContent = {
					Text(
						optionLabel,
						color = if (isSelected) VegafoXColors.OrangePrimary else Color.Unspecified,
					)
				},
				trailingContent = {
					RadioButton(
						checked = isSelected,
						containerColor = VegafoXColors.OrangePrimary,
					)
				},
				colors =
					if (isSelected) {
						ListControlDefaults.colors(
							containerColor = VegafoXColors.OrangeSoft,
						)
					} else {
						ListControlDefaults.colors()
					},
				onClick = {
					value = option
					router.back()
				},
			)
		}
	}
}
