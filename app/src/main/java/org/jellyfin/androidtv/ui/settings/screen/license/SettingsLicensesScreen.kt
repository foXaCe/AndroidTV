package org.jellyfin.androidtv.ui.settings.screen.license

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn

@Composable
fun SettingsLicensesScreen() {
	val context = LocalContext.current
	val router = LocalRouter.current

	val libraries =
		remember(context) {
			val libs =
				Libs
					.Builder()
					.withContext(context)
					.build()

			libs.libraries.sortedBy { it.name.lowercase() }
		}

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.licenses_link),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		items(libraries) { library ->
			ListButton(
				headingContent = { Text("${library.name} ${library.artifactVersion}") },
				captionContent = { Text(library.licenses.joinToString(", ") { license -> license.name }) },
				onClick = { router.push(Routes.LICENSE, mapOf("artifactId" to library.artifactId)) },
			)
		}
	}
}
