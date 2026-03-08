package org.jellyfin.androidtv.ui.jellyseerr.compose

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrCastMemberDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.itemhandling.JellyseerrPersonBaseRowItem
import org.jellyfin.androidtv.ui.presentation.CardPresenter

@Composable
fun JellyseerrCastRow(
	cast: List<JellyseerrCastMemberDto>,
	onCastClick: (castId: Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.lbl_cast_section),
			style = JellyfinTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = JellyfinTheme.colorScheme.textPrimary,
			modifier = Modifier.padding(bottom = 16.dp),
		)

		if (cast.isNotEmpty()) {
			LazyRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(end = 24.dp),
			) {
				items(cast, key = { it.id }) { member ->
					CastCardItem(
						member = member,
						onClick = { onCastClick(member.id) },
					)
				}
			}
		} else {
			Text(
				text = stringResource(R.string.jellyseerr_cast_not_available),
				style = JellyfinTheme.typography.bodyMedium,
				color = JellyfinTheme.colorScheme.textSecondary,
			)
		}
	}
}

@Composable
private fun CastCardItem(
	member: JellyseerrCastMemberDto,
	onClick: () -> Unit,
) {
	val context = LocalContext.current
	val rowItem = remember(member) { JellyseerrPersonBaseRowItem(member) }

	AndroidView(
		factory = { ctx ->
			val presenter = CardPresenter(true, 130)
			val parent = LinearLayout(ctx).apply {
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
				)
			}
			val vh = presenter.onCreateViewHolder(parent)
			presenter.onBindViewHolder(vh, rowItem)
			vh.view.apply {
				setOnClickListener { onClick() }
			}
		},
	)
}
