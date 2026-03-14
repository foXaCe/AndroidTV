package org.jellyfin.androidtv.ui.settings.screen.authentication

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.model.AuthenticationSortBy
import org.jellyfin.androidtv.auth.store.AuthenticationPreferences
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.RadioButton
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject

@Composable
fun SettingsAuthenticationSortByScreen() {
	val router = LocalRouter.current
	val authenticationPreferences = koinInject<AuthenticationPreferences>()
	var sortBy by rememberPreference(authenticationPreferences, AuthenticationPreferences.sortBy)

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.sort_accounts_by),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		items(AuthenticationSortBy.entries) { entry ->
			ListButton(
				headingContent = { Text(stringResource(entry.nameRes)) },
				trailingContent = { RadioButton(checked = sortBy == entry) },
				onClick = {
					sortBy = entry
					router.back()
				},
			)
		}
	}
}
