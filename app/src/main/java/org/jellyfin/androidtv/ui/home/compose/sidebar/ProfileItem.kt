package org.jellyfin.androidtv.ui.home.compose.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.filterNotNull
import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.ui.base.theme.SidebarDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.primaryImage
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.compose.koinInject

private val ProfileSize = 36.dp
private val NormalBorderColor = Color(0xFF2A2A3A)
private val GlowColor = VegafoXColors.OrangePrimary.copy(alpha = 0.25f)
private val FallbackBackground = VegafoXColors.OrangePrimary.copy(alpha = 0.18f)
private val SubtitleColor = Color(0xFF7A7A8E)

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val ANIM_MS = 140
private const val LABEL_FADE_MS = 150

/**
 * Profile avatar item for the bottom of the premium sidebar.
 * Shows user photo (via Coil) or initial letter fallback.
 * When expanded, shows username and "Changer de profil" next to avatar.
 * Click / DpadCenter triggers [onSelect].
 */
@Composable
fun ProfileItem(
	onSelect: () -> Unit,
	modifier: Modifier = Modifier,
	isExpanded: Boolean = false,
	onFocusChanged: ((Boolean) -> Unit)? = null,
	userRepository: UserRepository = koinInject(),
	api: ApiClient = koinInject(),
) {
	val currentUser by remember { userRepository.currentUser.filterNotNull() }.collectAsState(null)
	val userName = currentUser?.name.orEmpty()
	val userImageUrl = remember(currentUser) { currentUser?.primaryImage?.getUrl(api) }

	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	LaunchedEffect(isFocused) {
		onFocusChanged?.invoke(isFocused)
	}

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.10f else 1f,
		animationSpec = tween(ANIM_MS, easing = EaseOutCubic),
		label = "profileScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = tween(ANIM_MS, easing = EaseOutCubic),
		label = "profileGlow",
	)

	Row(
		modifier = modifier.fillMaxWidth().height(SidebarDimensions.navItemHeight),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Avatar container — same 72×52dp as NavItem icon container for vertical alignment
		Box(
			modifier =
				Modifier
					.width(PREMIUM_SIDEBAR_WIDTH_COLLAPSED)
					.height(SidebarDimensions.navItemHeight),
			contentAlignment = Alignment.Center,
		) {
			Box(
				modifier =
					Modifier
						.graphicsLayer {
							scaleX = scale
							scaleY = scale
						}.size(ProfileSize)
						.drawBehind {
							if (glowAlpha > 0f) {
								drawCircle(
									brush =
										Brush.radialGradient(
											colors =
												listOf(
													GlowColor.copy(alpha = GlowColor.alpha * glowAlpha),
													Color.Transparent,
												),
											radius = size.maxDimension * 0.8f,
										),
								)
							}
						}.border(
							width = if (isFocused) 2.dp else 1.dp,
							color = if (isFocused) VegafoXColors.OrangePrimary else NormalBorderColor,
							shape = CircleShape,
						).clip(CircleShape)
						.focusable(interactionSource = interactionSource)
						.onKeyEvent { event ->
							if ((event.key == Key.Enter || event.key == Key.DirectionCenter) &&
								event.type == KeyEventType.KeyUp
							) {
								onSelect()
								true
							} else {
								false
							}
						}.clickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null,
							onClick = onSelect,
						),
				contentAlignment = Alignment.Center,
			) {
				val painter = rememberAsyncImagePainter(userImageUrl)
				val painterState by painter.state.collectAsState()
				val imageLoaded = painterState is AsyncImagePainter.State.Success

				if (imageLoaded) {
					Image(
						painter = painter,
						contentDescription = userName,
						contentScale = ContentScale.Crop,
						modifier = Modifier.size(ProfileSize),
					)
				} else {
					// Fallback: initial letter
					Box(
						modifier =
							Modifier
								.size(ProfileSize)
								.background(FallbackBackground),
						contentAlignment = Alignment.Center,
					) {
						Text(
							text = userName.firstOrNull()?.uppercase().orEmpty(),
							style =
								TextStyle(
									fontSize = 22.sp,
									fontWeight = FontWeight.Bold,
									color = VegafoXColors.OrangePrimary,
								),
						)
					}
				}
			}
		}

		// Expanded: username + subtitle
		AnimatedVisibility(
			visible = isExpanded,
			enter = fadeIn(tween(LABEL_FADE_MS)),
			exit = fadeOut(tween(LABEL_FADE_MS)),
		) {
			Row {
				Spacer(modifier = Modifier.width(16.dp))
				Column {
					Text(
						text = userName,
						style =
							TextStyle(
								fontSize = 14.sp,
								fontWeight = FontWeight.Medium,
								color = VegafoXColors.TextPrimary,
							),
						maxLines = 1,
					)
					Text(
						text = "Changer de profil",
						style =
							TextStyle(
								fontSize = 11.sp,
								fontWeight = FontWeight.Normal,
								color = SubtitleColor,
							),
						maxLines = 1,
					)
				}
			}
		}
	}
}
