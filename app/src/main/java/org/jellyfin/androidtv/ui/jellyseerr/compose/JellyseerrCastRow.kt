package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrCastMemberDto
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun JellyseerrCastRow(
	cast: List<JellyseerrCastMemberDto>,
	onCastClick: (castId: Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.lbl_cast_section),
			style =
				TextStyle(
					fontFamily = BebasNeue,
					fontSize = 20.sp,
					letterSpacing = 2.sp,
					color = VegafoXColors.TextPrimary,
				),
			modifier = Modifier.padding(bottom = 16.dp),
		)

		if (cast.isNotEmpty()) {
			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(end = 24.dp),
			) {
				items(cast, key = { it.id }) { member ->
					JellyseerrPersonCard(
						castMember = member,
						onClick = { onCastClick(member.id) },
					)
				}
			}
		} else {
			Text(
				text = stringResource(R.string.jellyseerr_cast_not_available),
				style =
					TextStyle(
						fontSize = 14.sp,
						color = VegafoXColors.TextSecondary,
					),
			)
		}
	}
}
