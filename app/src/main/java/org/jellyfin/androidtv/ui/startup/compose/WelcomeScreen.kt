package org.jellyfin.androidtv.ui.startup.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.BuildConfig
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.theme.VegafoXGradients

@Composable
fun WelcomeScreen(
	onConnectClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier =
			modifier
				.fillMaxSize()
				.background(VegafoXColors.Background),
		contentAlignment = Alignment.Center,
	) {
		// Layer 1: warm radial background
		Canvas(Modifier.fillMaxSize()) {
			drawRect(
				brush =
					Brush.radialGradient(
						colors = listOf(Color(0xFF12080A), VegafoXColors.Background),
						center = Offset(size.width / 2f, size.height * 0.4f),
						radius = size.maxDimension * 0.7f,
					),
			)
		}

		// Layer 2: decorative grid at 3% opacity
		SubtleGrid()

		// Layer 3: horizon line at 35% from bottom
		HorizonLine()

		// Layer 4: animated content
		WelcomeContent(onConnectClick = onConnectClick)
	}
}

@Composable
private fun SubtleGrid() {
	Canvas(Modifier.fillMaxSize()) {
		val step = 60.dp.toPx()
		val color = Color.White.copy(alpha = 0.03f)
		var x = 0f
		while (x < size.width) {
			drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
			x += step
		}
		var y = 0f
		while (y < size.height) {
			drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
			y += step
		}
	}
}

@Composable
private fun HorizonLine() {
	BoxWithConstraints(Modifier.fillMaxSize()) {
		Box(
			Modifier
				.fillMaxWidth()
				.offset(y = maxHeight * 0.65f)
				.height(1.dp)
				.background(VegafoXGradients.HorizonLine),
		)
	}
}

@Composable
private fun WelcomeContent(onConnectClick: () -> Unit) {
	var navigating by remember { mutableStateOf(false) }
	var showOrb by rememberSaveable { mutableStateOf(false) }
	var showTitle by rememberSaveable { mutableStateOf(false) }
	var showDivider by rememberSaveable { mutableStateOf(false) }
	var showButton by rememberSaveable { mutableStateOf(false) }
	var showVersion by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		delay(200)
		showOrb = true
		delay(600)
		showTitle = true
		delay(200)
		showDivider = true
		delay(100)
		showButton = true
		delay(200)
		showVersion = true
	}

	Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		// Orange orb behind everything
		AnimatedVisibility(
			visible = showOrb,
			enter = fadeIn(tween(600)),
		) {
			Canvas(Modifier.fillMaxSize()) {
				drawCircle(
					brush =
						Brush.radialGradient(
							listOf(VegafoXColors.OrangeGlow, Color.Transparent),
							radius = size.minDimension * 0.45f,
						),
					radius = size.minDimension * 0.45f,
				)
			}
		}

		// Central column
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier.fillMaxSize(),
		) {
			// Fox logo — animation gérée par graphicsLayer dans VegafoXFoxLogo
			VegafoXFoxLogo(animated = true)

			Spacer(Modifier.height(24.dp))

			// Title + subtitle
			AnimatedVisibility(
				visible = showTitle,
				enter =
					slideInVertically(tween(500)) { it / 2 } +
						fadeIn(tween(500)),
			) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					VegafoXTitleText()
					Spacer(Modifier.height(6.dp))
					VegafoXSubtitle()
				}
			}

			Spacer(Modifier.height(28.dp))

			// Divider
			AnimatedVisibility(
				visible = showDivider,
				enter = expandHorizontally(tween(400)) + fadeIn(tween(400)),
			) {
				Box(
					Modifier
						.width(60.dp)
						.height(1.dp)
						.background(VegafoXGradients.HorizonLine),
				)
			}

			Spacer(Modifier.height(28.dp))

			// Connect button — fadeIn spring (pas de scaleIn/slideIn qui clippe en rectangle)
			AnimatedVisibility(
				visible = showButton,
				enter =
					fadeIn(
						animationSpec =
							spring(
								dampingRatio = Spring.DampingRatioNoBouncy,
								stiffness = Spring.StiffnessLow,
							),
					),
			) {
				VegafoXButton(
					text = stringResource(R.string.lbl_connect_to_server),
					onClick = {
						if (!navigating) {
							navigating = true
							onConnectClick()
						}
					},
					variant = VegafoXButtonVariant.Primary,
					autoFocus = true,
					icon = VegafoXIcons.ArrowForward,
				)
			}

			Spacer(Modifier.height(24.dp))

			// Version
			AnimatedVisibility(
				visible = showVersion,
				enter = fadeIn(tween(300)),
			) {
				Text(
					text = stringResource(R.string.lbl_version, BuildConfig.VERSION_NAME),
					style =
						TextStyle(
							fontSize = 11.sp,
							color = VegafoXColors.TextHint,
							letterSpacing = 1.sp,
						),
				)
			}
		}
	}
}
