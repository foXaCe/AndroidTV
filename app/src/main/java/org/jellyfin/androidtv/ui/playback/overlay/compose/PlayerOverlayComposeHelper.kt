package org.jellyfin.androidtv.ui.playback.overlay.compose

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.jellyfin.androidtv.ui.base.JellyfinTheme

fun setupPlayerOverlayComposeView(
	composeView: ComposeView,
	viewModel: PlayerOverlayViewModel,
	onAction: (PlayerOverlayAction) -> Unit,
) {
	composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
	composeView.setContent {
		JellyfinTheme {
			val state by viewModel.state.collectAsState()
			PlayerOverlayScreen(
				state = state,
				onAction = onAction,
			)
			PlayerPopupHost(
				state = state,
				onAction = onAction,
			)
		}
	}
}
