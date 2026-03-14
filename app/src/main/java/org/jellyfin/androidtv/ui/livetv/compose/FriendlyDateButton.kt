package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.getDateFormatter
import java.time.LocalDateTime

@Composable
fun FriendlyDateButton(
	date: LocalDateTime,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current

	Button(
		onClick = onClick,
		modifier = modifier,
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
		) {
			Text(
				text = TimeUtils.getFriendlyDate(context, date, true),
				fontSize = 14.sp,
			)
			Text(
				text = context.getDateFormatter().format(date),
				fontSize = 12.sp,
				color = VegafoXColors.TextPrimary.copy(alpha = 0.7f),
			)
		}
	}
}
