package org.jellyfin.androidtv.ui.startup.user

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.CircularProgressIndicator
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.startup.compose.VegafoXFoxLogo
import org.jellyfin.androidtv.ui.startup.compose.VegafoXTitleText

data class UserInfo(
	val name: String,
	val imageUrl: String?,
)

data class PinEntryState(
	val userName: String = "",
	val error: String? = null,
	val visible: Boolean = false,
)

@Composable
fun UserSelectionScreen(
	serverName: String,
	users: List<UserInfo>,
	isLoading: Boolean,
	onUserSelected: (index: Int) -> Unit,
	onAddAccount: () -> Unit,
	onChangeServer: () -> Unit,
	pinEntryState: PinEntryState = PinEntryState(),
	onPinEntered: (String) -> Unit = {},
	onPinCancel: () -> Unit = {},
	onPinForgot: () -> Unit = {},
	modifier: Modifier = Modifier,
) {
	BoxWithConstraints(
		modifier =
			modifier
				.fillMaxSize()
				.background(VegafoXColors.Background),
	) {
		val screenHeight = maxHeight
		val density = LocalDensity.current

		// Decorative background gradients
		Canvas(modifier = Modifier.fillMaxSize()) {
			val w = size.width
			val h = size.height
			val center = Offset(w * 0.5f, h * 0.9f)

			drawRect(
				brush =
					Brush.radialGradient(
						colorStops =
							arrayOf(
								0.0f to VegafoXColors.OrangePrimary.copy(alpha = 0.10f),
								0.4f to VegafoXColors.OrangePrimary.copy(alpha = 0.10f),
								1.0f to Color.Transparent,
							),
						center = center,
						radius = w * 0.4f,
					),
			)

			drawRect(
				brush =
					Brush.radialGradient(
						colorStops =
							arrayOf(
								0.0f to VegafoXColors.OrangeWarm.copy(alpha = 0.06f),
								1.0f to Color.Transparent,
							),
						center = center,
						radius = w * 0.25f,
					),
			)

			drawLine(
				brush =
					Brush.horizontalGradient(
						colors =
							listOf(
								Color.Transparent,
								VegafoXColors.OrangeBorder,
								Color.Transparent,
							),
					),
				start = Offset(0f, h * 0.7f),
				end = Offset(w, h * 0.7f),
				strokeWidth = 1f,
			)
		}

		// Header top-left: Fox logo 28dp + Title 16sp + serverName 10sp uppercase
		Row(
			modifier =
				Modifier
					.align(Alignment.TopStart)
					.padding(start = 24.dp, top = 20.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			VegafoXFoxLogo(size = 28.dp, animated = false)
			Spacer(modifier = Modifier.width(8.dp))
			VegafoXTitleText(fontSize = 16.sp)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = serverName.uppercase(),
				style =
					TextStyle(
						fontSize = 10.sp,
						fontWeight = FontWeight.Normal,
						color = VegafoXColors.TextHint,
					),
			)
		}

		// Title "Who's watching?" at 20% from top
		Text(
			text = stringResource(R.string.who_is_watching),
			style =
				TextStyle(
					fontSize = 36.sp,
					fontWeight = FontWeight.Bold,
					color = VegafoXColors.TextPrimary,
					textAlign = TextAlign.Center,
				),
			modifier =
				Modifier
					.align(Alignment.TopCenter)
					.offset(y = screenHeight * 0.20f),
		)

		// User cards centered
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.align(Alignment.Center),
			contentAlignment = Alignment.Center,
		) {
			if (isLoading) {
				CircularProgressIndicator(
					color = VegafoXColors.OrangePrimary,
				)
			} else {
				var focusedIndex by remember { mutableIntStateOf(0) }
				val firstCardFocusRequester = remember { FocusRequester() }

				LaunchedEffect(users) {
					if (users.isNotEmpty()) {
						delay(150)
						firstCardFocusRequester.requestFocus()
					}
				}

				Row(
					horizontalArrangement = Arrangement.spacedBy(32.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					val easeOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

					users.forEachIndexed { index, user ->
						var appeared by remember { mutableStateOf(false) }

						LaunchedEffect(Unit) {
							delay(60L * index)
							appeared = true
						}

						val entryOffsetY by animateDpAsState(
							targetValue = if (appeared) 0.dp else 40.dp,
							animationSpec = tween(durationMillis = 500, easing = easeOutCubic),
							label = "spotEntryY",
						)
						val entryAlpha by animateFloatAsState(
							targetValue = if (appeared) 1f else 0f,
							animationSpec = tween(durationMillis = 400, easing = easeOutCubic),
							label = "spotEntryAlpha",
						)

						val entryOffsetYPx = with(density) { entryOffsetY.toPx() }

						SpotCard(
							name = user.name,
							imageUrl = user.imageUrl,
							isFocused = focusedIndex == index,
							onClick = { onUserSelected(index) },
							modifier =
								Modifier
									.graphicsLayer {
										translationY = entryOffsetYPx
										alpha = entryAlpha
									}.then(
										if (index == 0) {
											Modifier.focusRequester(firstCardFocusRequester)
										} else {
											Modifier
										},
									).onFocusChanged { state ->
										if (state.isFocused) focusedIndex = index
									},
						)
					}
				}
			}
		}

		// Bottom buttons centered, padding bottom 5%
		Row(
			modifier =
				Modifier
					.align(Alignment.BottomCenter)
					.padding(bottom = screenHeight * 0.05f),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
		) {
			VegafoXButton(
				text = stringResource(R.string.add_user),
				onClick = onAddAccount,
				variant = VegafoXButtonVariant.Secondary,
			)
			VegafoXButton(
				text = stringResource(R.string.change_server),
				onClick = onChangeServer,
				variant = VegafoXButtonVariant.Ghost,
			)
		}

		// PIN entry dialog
		if (pinEntryState.visible) {
			PinEntryScreen(
				userName = pinEntryState.userName,
				error = pinEntryState.error,
				onPinEntered = onPinEntered,
				onCancel = onPinCancel,
				onForgotPin = onPinForgot,
			)
		}
	}
}
