package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.LiveTvPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.androidtv.util.sdk.isNew
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDateTime

private val cellBorderColor = Color.White.copy(alpha = 0.06f)

private enum class ProgramTimeState { PAST, CURRENT, FUTURE }

@Composable
fun ProgramCell(
	program: BaseItemDto,
	isFocused: Boolean,
	accentColor: Color,
	modifier: Modifier = Modifier,
) {
	val liveTvPreferences = koinInject<LiveTvPreferences>()
	val context = LocalContext.current
	val now = LocalDateTime.now()

	// Determine time state
	val startDate = program.startDate
	val endDate = program.endDate
	val timeState =
		when {
			endDate != null && !endDate.isAfter(now) -> ProgramTimeState.PAST
			startDate != null && !startDate.isAfter(now) && (endDate == null || endDate.isAfter(now)) -> ProgramTimeState.CURRENT
			else -> ProgramTimeState.FUTURE
		}

	// Background based on state
	val bgColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary.copy(alpha = 0.20f)
			timeState == ProgramTimeState.PAST -> Color.White.copy(alpha = 0.02f)
			timeState == ProgramTimeState.CURRENT -> VegafoXColors.OrangePrimary.copy(alpha = 0.12f)
			else -> Color.Transparent
		}

	// Text color based on state
	val textColor = if (timeState == ProgramTimeState.PAST) VegafoXColors.TextHint else VegafoXColors.TextPrimary

	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(bgColor)
				.border(1.dp, cellBorderColor),
	) {
		// Focus: left border accent
		if (isFocused) {
			Box(
				modifier =
					Modifier
						.align(Alignment.CenterStart)
						.width(3.dp)
						.fillMaxHeight()
						.background(VegafoXColors.OrangePrimary),
			)
		}

		Column(
			modifier =
				Modifier
					.padding(start = if (isFocused) 8.dp else 5.dp, end = 5.dp, top = 2.dp, bottom = 2.dp)
					.fillMaxSize(),
		) {
			// Program name (with early-start prefix)
			val displayName =
				buildString {
					if (startDate != null) {
						val guideStart = LocalDateTime.now()
						if (startDate.plusMinutes(1).isBefore(guideStart)) {
							append("<< ")
						}
					}
					append(program.name ?: stringResource(R.string.no_program_data))
				}

			Text(
				text = displayName,
				fontSize = 16.sp,
				fontFamily = FontFamily.SansSerif,
				fontWeight = FontWeight.Normal,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = textColor,
			)

			// Info row: start time + indicators
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (startDate != null && startDate.plusMinutes(1).isBefore(now)) {
					Text(
						text = context.getTimeFormatter().format(startDate),
						fontSize = 12.sp,
						fontFamily = FontFamily.SansSerif,
						fontWeight = FontWeight.Light,
						color = VegafoXColors.TextSecondary,
					)
					Spacer(Modifier.width(4.dp))
				}

				// New indicator
				if (liveTvPreferences[LiveTvPreferences.showNewIndicator] &&
					program.isNew() &&
					(!liveTvPreferences[LiveTvPreferences.showPremiereIndicator] || !Utils.isTrue(program.isPremiere))
				) {
					BlockText(
						text = stringResource(R.string.lbl_new),
						textColor = VegafoXColors.OrangePrimary,
						bgColor = VegafoXColors.OrangePrimary.copy(alpha = 0.20f),
					)
					Spacer(Modifier.width(4.dp))
				}

				// Premiere indicator
				if (liveTvPreferences[LiveTvPreferences.showPremiereIndicator] && Utils.isTrue(program.isPremiere)) {
					BlockText(
						text = stringResource(R.string.lbl_premiere),
						textColor = VegafoXColors.OrangePrimary,
						bgColor = VegafoXColors.OrangePrimary.copy(alpha = 0.20f),
					)
					Spacer(Modifier.width(4.dp))
				}

				// Repeat indicator
				if (liveTvPreferences[LiveTvPreferences.showRepeatIndicator] && Utils.isTrue(program.isRepeat)) {
					BlockText(
						text = stringResource(R.string.lbl_repeat),
						textColor = VegafoXColors.TextSecondary,
						bgColor = Color.White.copy(alpha = 0.08f),
					)
					Spacer(Modifier.width(4.dp))
				}

				// Official rating
				val rating = program.officialRating
				if (rating != null && rating != "0") {
					BlockText(
						text = rating,
						textColor = VegafoXColors.TextHint,
						bgColor = Color.White.copy(alpha = 0.06f),
					)
					Spacer(Modifier.width(4.dp))
				}

				// HD indicator
				if (liveTvPreferences[LiveTvPreferences.showHDIndicator] && Utils.isTrue(program.isHd)) {
					BlockText(
						text = "HD",
						textColor = VegafoXColors.TextSecondary,
						bgColor = Color.White.copy(alpha = 0.06f),
					)
					Spacer(Modifier.width(4.dp))
				}

				// Recording indicator
				RecordingIndicator(program)
			}
		}

		// Progress bar for current programs
		if (timeState == ProgramTimeState.CURRENT && startDate != null && endDate != null) {
			val totalDuration = Duration.between(startDate, endDate).seconds.toFloat()
			val elapsed = Duration.between(startDate, now).seconds.toFloat()
			val progress = if (totalDuration > 0f) (elapsed / totalDuration).coerceIn(0f, 1f) else 0f

			Box(
				modifier =
					Modifier
						.align(Alignment.BottomStart)
						.fillMaxWidth()
						.height(3.dp)
						.background(Color.White.copy(alpha = 0.08f)),
			) {
				Box(
					modifier =
						Modifier
							.fillMaxWidth(progress)
							.height(3.dp)
							.background(VegafoXColors.OrangePrimary),
				)
			}
		}
	}
}

@Composable
private fun BlockText(
	text: String,
	textColor: Color,
	bgColor: Color,
) {
	Text(
		text = " $text ",
		fontSize = 10.sp,
		color = textColor,
		modifier = Modifier.background(bgColor),
	)
}

@Composable
private fun RecordingIndicator(program: BaseItemDto) {
	val (icon: ImageVector, tint: Color) =
		when {
			program.seriesTimerId != null && program.timerId != null -> VegafoXIcons.RecordSeries to Color.Red
			program.seriesTimerId != null -> VegafoXIcons.RecordSeries to Color.White
			program.timerId != null -> VegafoXIcons.Record to Color.Red
			else -> return
		}

	Icon(
		painter = rememberVectorPainter(icon),
		tint = tint,
		contentDescription = null,
		modifier = Modifier.size(10.dp),
	)
}
