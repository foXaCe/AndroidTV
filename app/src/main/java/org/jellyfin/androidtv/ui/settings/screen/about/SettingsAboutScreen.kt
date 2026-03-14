package org.jellyfin.androidtv.ui.settings.screen.about

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.jellyfin.androidtv.ui.settings.util.copyAction

@Composable
fun SettingsAboutScreen(launchedFromLogin: Boolean = false) {
	val router = LocalRouter.current

	SettingsColumn {
		item {
			Column(
				modifier =
					Modifier
						.fillMaxWidth()
						.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Image(
					painter = painterResource(R.drawable.ic_vegafox_fox),
					contentDescription = null,
					modifier = Modifier.size(80.dp),
				)
				Spacer(Modifier.height(12.dp))
				Text(
					text = "VegafoX",
					fontFamily = BebasNeue,
					fontSize = 32.sp,
					color = VegafoXColors.OrangePrimary,
				)
				Text(
					text = "vegafox-androidtv ${BuildConfig.VERSION_NAME}",
					fontSize = 14.sp,
					color = VegafoXColors.TextSecondary,
				)
				Spacer(Modifier.height(4.dp))
				Text(
					text = stringResource(R.string.powered_by_jellyfin),
					fontSize = 13.sp,
					color = VegafoXColors.TextHint,
				)
			}
		}

		item {
			val heading = stringResource(R.string.lbl_app_version_heading)
			val caption = "vegafox-androidtv ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}"
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_vegafox_fox), contentDescription = null) },
				headingContent = { Text(heading) },
				captionContent = { Text(caption) },
				onClick = copyAction(ClipData.newPlainText(heading, caption)),
			)
		}

		item {
			val heading = stringResource(R.string.pref_device_model)
			val caption = "${Build.MANUFACTURER} ${Build.MODEL}"
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Tv), contentDescription = null) },
				headingContent = { Text(heading) },
				captionContent = { Text(caption) },
				onClick = copyAction(ClipData.newPlainText(heading, caption)),
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Guide), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.licenses_link)) },
				onClick = { router.push(Routes.LICENSES) },
			)
		}
	}
}
