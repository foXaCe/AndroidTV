package org.jellyfin.androidtv.ui.base.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.design.Tokens

@Composable
fun Checkbox(
	checked: Boolean,
	modifier: Modifier = Modifier,
	shape: Shape = JellyfinTheme.shapes.extraSmall,
	containerColor: Color = VegafoXColors.OrangePrimary,
	contentColor: Color = JellyfinTheme.colorScheme.onButton,
	onCheckedChange: ((Boolean) -> Unit)? = null,
) {
	Box(
		modifier =
			modifier
				.defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
				.background(if (checked) containerColor else Color.Unspecified, shape)
				.border(if (checked) 0.dp else 2.dp, containerColor, shape),
		contentAlignment = Alignment.Center,
	) {
		AnimatedVisibility(
			visible = checked,
			modifier =
				Modifier
					.matchParentSize(),
		) {
			Icon(
				rememberVectorPainter(VegafoXIcons.Check),
				tint = contentColor,
				contentDescription = null,
				modifier =
					Modifier
						.padding(Tokens.Space.spaceXs),
			)
		}
	}
}
