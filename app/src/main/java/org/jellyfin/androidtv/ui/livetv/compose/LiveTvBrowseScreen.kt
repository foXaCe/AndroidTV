package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.LiveTvDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

private val TileShape = RoundedCornerShape(16.dp)
private const val ANIM_MS = 150

@Composable
fun LiveTvBrowseScreen(
	canManageRecordings: Boolean,
	onNavigateGuide: () -> Unit,
	onNavigateRecordings: () -> Unit,
	onNavigateSchedule: () -> Unit,
	onNavigateSeriesRecordings: () -> Unit,
) {
	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep),
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(horizontal = LiveTvDimensions.browseScreenPadding, vertical = 48.dp),
		) {
			// Header
			Text(
				text = stringResource(R.string.pref_live_tv_cat),
				style =
					TextStyle(
						fontFamily = BebasNeue,
						fontSize = 40.sp,
						color = VegafoXColors.TextPrimary,
						letterSpacing = 2.sp,
					),
			)

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = stringResource(R.string.lbl_choose_section),
				style =
					TextStyle(
						fontSize = 14.sp,
						color = VegafoXColors.TextSecondary,
					),
			)

			Spacer(modifier = Modifier.height(32.dp))

			// Tile 1 — Guide
			NavigationTile(
				icon = VegafoXIcons.Schedule,
				title = stringResource(R.string.lbl_live_tv_guide),
				subtitle = stringResource(R.string.lbl_epg_grid),
				onClick = onNavigateGuide,
			)

			TileDivider()

			// Tile 2 — Recordings
			NavigationTile(
				icon = VegafoXIcons.Trailer,
				title = stringResource(R.string.lbl_recorded_tv),
				subtitle = stringResource(R.string.lbl_your_recordings),
				onClick = onNavigateRecordings,
			)

			if (canManageRecordings) {
				TileDivider()

				// Tile 3 — Schedule
				NavigationTile(
					icon = VegafoXIcons.Calendar,
					title = stringResource(R.string.lbl_schedule),
					subtitle = stringResource(R.string.lbl_coming_up),
					onClick = onNavigateSchedule,
				)

				TileDivider()

				// Tile 4 — Series recordings
				NavigationTile(
					icon = VegafoXIcons.VideoLibrary,
					title = stringResource(R.string.lbl_series),
					subtitle = stringResource(R.string.lbl_series_recordings),
					onClick = onNavigateSeriesRecordings,
				)
			}
		}
	}
}

@Composable
private fun TileDivider() {
	Box(
		modifier =
			Modifier
				.fillMaxWidth()
				.height(1.dp)
				.background(Color.White.copy(alpha = 0.06f)),
	)
}

@Composable
private fun NavigationTile(
	icon: ImageVector,
	title: String,
	subtitle: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var isFocused by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.02f else 1f,
		animationSpec = spring(stiffness = Spring.StiffnessMedium),
		label = "tileScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = tween(ANIM_MS),
		label = "tileGlow",
	)

	Box(
		modifier =
			modifier
				.fillMaxWidth()
				.height(120.dp)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.drawBehind {
					if (glowAlpha > 0f) {
						drawRoundRect(
							brush =
								Brush.radialGradient(
									colors =
										listOf(
											VegafoXColors.OrangePrimary.copy(alpha = 0.20f * glowAlpha),
											Color.Transparent,
										),
									radius = size.maxDimension * 0.8f,
								),
							cornerRadius = CornerRadius(16.dp.toPx()),
						)
					}
				}.border(
					width = if (isFocused) 2.dp else 0.dp,
					color = if (isFocused) VegafoXColors.OrangePrimary else Color.Transparent,
					shape = TileShape,
				).clip(TileShape)
				.background(
					if (isFocused) {
						VegafoXColors.OrangePrimary.copy(alpha = 0.08f)
					} else {
						VegafoXColors.Surface
					},
				).onFocusChanged { isFocused = it.isFocused }
				.focusable()
				.onKeyEvent { event ->
					if ((event.key == Key.Enter || event.key == Key.DirectionCenter) &&
						event.type == KeyEventType.KeyUp
					) {
						onClick()
						true
					} else {
						false
					}
				}.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onClick,
				),
		contentAlignment = Alignment.CenterStart,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(24.dp),
		) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.size(48.dp),
				tint = VegafoXColors.OrangePrimary,
			)

			Spacer(modifier = Modifier.width(24.dp))

			Column {
				Text(
					text = title,
					style =
						TextStyle(
							fontFamily = BebasNeue,
							fontSize = 24.sp,
							color = VegafoXColors.TextPrimary,
						),
				)

				Text(
					text = subtitle,
					style =
						TextStyle(
							fontSize = 13.sp,
							color = VegafoXColors.TextSecondary,
						),
				)
			}
		}
	}
}
