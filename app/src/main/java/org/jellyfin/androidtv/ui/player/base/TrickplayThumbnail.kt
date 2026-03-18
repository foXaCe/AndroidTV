package org.jellyfin.androidtv.ui.player.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.playback.overlay.SeekProvider

@Composable
fun TrickplayThumbnail(
	seekProvider: SeekProvider,
	modifier: Modifier = Modifier,
) {
	val bitmap by seekProvider.currentThumbnail.collectAsState()
	val shape = RoundedCornerShape(4.dp)

	Box(
		modifier =
			modifier
				.clip(shape)
				.background(VegafoXColors.Surface)
				.border(1.dp, VegafoXColors.OrangePrimary, shape),
	) {
		if (bitmap != null) {
			Image(
				bitmap = bitmap!!.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Fit,
				modifier = Modifier.fillMaxSize(),
			)
		}
	}
}
