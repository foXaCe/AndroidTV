package org.jellyfin.androidtv.ui.shared.toolbar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons

@Composable
fun StartupToolbar(
	openHelp: () -> Unit,
	openSettings: () -> Unit,
) {
	val focusRequester = remember { FocusRequester() }
	Toolbar(
		start = {},
		end = {
			ToolbarButtons {
				IconButton(
					onClick = openHelp,
					modifier = Modifier.focusRequester(focusRequester),
				) {
					Icon(
						imageVector = VegafoXIcons.Help,
						contentDescription = stringResource(R.string.help),
					)
				}

				IconButton(onClick = openSettings) {
					Icon(
						imageVector = VegafoXIcons.Settings,
						contentDescription = stringResource(R.string.lbl_settings),
					)
				}

				Spacer(Modifier.width(8.dp))

				ToolbarClock()

				Spacer(Modifier.width(12.dp))
			}
		},
	)
}
