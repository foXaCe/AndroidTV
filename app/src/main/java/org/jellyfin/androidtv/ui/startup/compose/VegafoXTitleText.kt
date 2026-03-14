package org.jellyfin.androidtv.ui.startup.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.theme.StartupDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

/**
 * Titre "VegafoX" avec "Vega" en bleu et "foX" en orange.
 */
@Composable
fun VegafoXTitleText(
	modifier: Modifier = Modifier,
	fontSize: TextUnit = StartupDimensions.titleFontSize,
) {
	Text(
		text =
			buildAnnotatedString {
				withStyle(SpanStyle(color = VegafoXColors.BlueAccent)) {
					append("Vega")
				}
				withStyle(SpanStyle(color = VegafoXColors.OrangePrimary)) {
					append("foX")
				}
			},
		style =
			TextStyle(
				fontSize = fontSize,
				fontWeight = FontWeight.Bold,
				letterSpacing = (-1).sp,
				lineHeight = fontSize,
			),
		modifier = modifier,
	)
}

/**
 * Sous-titre "MEDIA CENTER" espacé et muted.
 */
@Composable
fun VegafoXSubtitle(modifier: Modifier = Modifier) {
	Text(
		text = stringResource(R.string.lbl_app_tagline),
		style =
			TextStyle(
				fontSize = 13.sp,
				fontWeight = FontWeight.Medium,
				letterSpacing = 4.sp,
				color = VegafoXColors.TextSecondary,
			),
		modifier = modifier,
	)
}
