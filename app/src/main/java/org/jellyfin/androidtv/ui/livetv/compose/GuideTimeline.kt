package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.util.getTimeFormatter
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun GuideTimeline(
	startTime: LocalDateTime,
	endTime: LocalDateTime,
	scrollState: ScrollState,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val formatter = context.getTimeFormatter()

	val now = LocalDateTime.now()
	val nowMinutesFromStart =
		if (!now.isBefore(startTime) && now.isBefore(endTime)) {
			Duration.between(startTime, now).seconds.toFloat() / 60f
		} else {
			null
		}

	val nowLineColor = VegafoXColors.OrangePrimary.copy(alpha = 0.90f)

	Column(modifier = modifier.height(TvSpacing.timelineHeight)) {
		Row(
			modifier =
				Modifier
					.weight(1f)
					.background(VegafoXColors.Surface)
					.horizontalScroll(scrollState, enabled = false)
					.drawWithContent {
						drawContent()
						if (nowMinutesFromStart != null) {
							val pxPerMin = TvSpacing.guideRowWidthPerMinDp.toPx()
							val nowX = nowMinutesFromStart * pxPerMin
							// Vertical line
							drawLine(
								color = nowLineColor,
								start = Offset(nowX, 0f),
								end = Offset(nowX, size.height),
								strokeWidth = 2.dp.toPx(),
							)
							// Small triangle/chevron at bottom pointing down
							val triSize = 4.dp.toPx()
							val path =
								Path().apply {
									moveTo(nowX - triSize, size.height - triSize)
									lineTo(nowX + triSize, size.height - triSize)
									lineTo(nowX, size.height)
									close()
								}
							drawPath(path, VegafoXColors.OrangePrimary)
						}
					},
		) {
			val startMinute = startTime.minute
			var intervalMinutes = if (startMinute >= 30) 60 - startMinute else 30 - startMinute
			var current = startTime

			while (current.isBefore(endTime)) {
				val widthDp =
					if (intervalMinutes < 15) {
						TvSpacing.guideRowWidthPerMinDp * 15f
					} else {
						TvSpacing.guideRowWidthPerMinDp * intervalMinutes.toFloat()
					}

				Box(
					modifier =
						Modifier
							.width(widthDp)
							.fillMaxHeight(),
					contentAlignment = Alignment.CenterStart,
				) {
					Text(
						text = formatter.format(current),
						fontSize = 14.sp,
						fontWeight = FontWeight.Bold,
						color = VegafoXColors.TextSecondary,
						modifier = Modifier.padding(start = 4.dp),
					)
				}

				current = current.plusMinutes(intervalMinutes.toLong())
				intervalMinutes = if (intervalMinutes < 30) 30 else 60
			}
		}

		// Bottom separator
		Spacer(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(VegafoXColors.PanelBorder),
		)
	}
}
