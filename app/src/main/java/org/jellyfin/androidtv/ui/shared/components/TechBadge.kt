package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

enum class TechBadgeStyle {
	RESOLUTION,
	HDR,
	TECH,
}

private val BadgeShape = RoundedCornerShape(4.dp)

@Composable
fun TechBadge(
	text: String,
	style: TechBadgeStyle,
	modifier: Modifier = Modifier,
	colorOverride: Color? = null,
) {
	val badgeColor =
		colorOverride ?: when (style) {
			TechBadgeStyle.RESOLUTION -> VegafoXColors.BadgeResolution
			TechBadgeStyle.HDR -> VegafoXColors.BadgeHdr
			TechBadgeStyle.TECH -> VegafoXColors.BadgeTech
		}

	Box(
		modifier =
			modifier
				.border(1.dp, badgeColor, BadgeShape)
				.padding(horizontal = 8.dp, vertical = 3.dp),
	) {
		Text(
			text = text,
			softWrap = false,
			maxLines = 1,
			overflow = TextOverflow.Clip,
			style =
				TextStyle(
					fontSize = 10.sp,
					fontWeight = FontWeight.Bold,
					color = badgeColor,
					letterSpacing = 1.sp,
				),
		)
	}
}
