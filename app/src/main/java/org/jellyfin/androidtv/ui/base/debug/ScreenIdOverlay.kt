package org.jellyfin.androidtv.ui.base.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun ScreenIdOverlay(
	id: String,
	name: String,
	content: @Composable () -> Unit,
) {
	Box(modifier = Modifier.fillMaxSize()) {
		content()

		if (BuildConfig.DEBUG) {
			val shape = RoundedCornerShape(6.dp)
			Text(
				text = "$id·$name",
				fontSize = 11.sp,
				fontFamily = FontFamily.Monospace,
				color = VegafoXColors.OrangePrimary,
				modifier =
					Modifier
						.align(Alignment.TopEnd)
						.padding(8.dp)
						.zIndex(Float.MAX_VALUE)
						.background(
							color = VegafoXColors.BackgroundDeep.copy(alpha = 0.85f),
							shape = shape,
						).border(
							width = 1.dp,
							color = VegafoXColors.OrangePrimary.copy(alpha = 0.50f),
							shape = shape,
						).padding(horizontal = 8.dp, vertical = 4.dp),
			)
		}
	}
}
