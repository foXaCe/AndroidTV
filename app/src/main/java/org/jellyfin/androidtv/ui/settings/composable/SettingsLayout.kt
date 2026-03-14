package org.jellyfin.androidtv.ui.settings.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.LocalShapes
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.design.Tokens

@Composable
fun SettingsLayout(
	modifier: Modifier = Modifier,
	content: @Composable BoxScope.() -> Unit,
) {
	Box(
		modifier =
			modifier
				.padding(Tokens.Space.spaceMd)
				.clip(LocalShapes.current.large)
				.border(1.dp, Color.White.copy(alpha = 0.08f), LocalShapes.current.large)
				.background(VegafoXColors.Surface)
				.width(350.dp)
				.fillMaxHeight(),
		content = content,
	)
}
