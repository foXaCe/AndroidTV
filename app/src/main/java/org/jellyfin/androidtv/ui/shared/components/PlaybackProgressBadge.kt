package org.jellyfin.androidtv.ui.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun PlaybackProgressBadge(
	playedPercentage: Float,
	runtimeTicks: Long?,
	modifier: Modifier = Modifier,
) {
	if (playedPercentage <= 0f) return

	val label =
		remember(playedPercentage, runtimeTicks) {
			if (runtimeTicks != null && runtimeTicks > 0) {
				val totalMs = runtimeTicks / 10_000L
				val remainingMs = (totalMs * (1f - playedPercentage / 100f)).toLong()
				val remainingMin = remainingMs / 60_000L
				when {
					remainingMin >= 60 -> {
						val h = remainingMin / 60
						val m = remainingMin % 60
						"${h}h ${m.toString().padStart(2, '0')}min restantes"
					}
					remainingMin >= 1 -> "$remainingMin min restantes"
					else -> "Moins d'1 min"
				}
			} else {
				"${playedPercentage.toInt()}% vu"
			}
		}

	Row(
		modifier =
			modifier
				.background(
					VegafoXColors.BackgroundDeep.copy(alpha = 0.85f),
					RoundedCornerShape(6.dp),
				).padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Icon(
			imageVector = VegafoXIcons.Play,
			contentDescription = null,
			modifier = Modifier.size(12.dp),
			tint = VegafoXColors.OrangePrimary,
		)

		Text(
			text = label,
			fontSize = 12.sp,
			fontFamily = BebasNeue,
			color = VegafoXColors.TextPrimary,
		)
	}
}
