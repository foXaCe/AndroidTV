package org.jellyfin.androidtv.ui.startup.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds

@Composable
fun SplashScreen() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.fillMaxSize(),
	) {
		Image(
			painter = painterResource(R.drawable.vegafox_splash),
			contentDescription = stringResource(R.string.app_name),
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize(),
		)
	}
}

class SplashFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			ScreenIdOverlay(ScreenIds.SPLASH_ID, ScreenIds.SPLASH_NAME) {
				SplashScreen()
			}
		}
	}
}
