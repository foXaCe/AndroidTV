package org.jellyfin.androidtv.ui.player.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerHeader(
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit,
) {
	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.padding(top = 32.dp, start = 56.dp, end = 56.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.Top,
		content = content,
	)
}
