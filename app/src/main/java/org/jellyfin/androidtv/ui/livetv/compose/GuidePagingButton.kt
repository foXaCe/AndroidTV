package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.Button

enum class PagingDirection { UP, DOWN }

@Composable
fun GuidePagingButton(
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Button(
		onClick = onClick,
		modifier = modifier.fillMaxWidth(),
	) {
		Text(
			text = label,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
