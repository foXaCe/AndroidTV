package org.jellyfin.androidtv.ui.itemdetail.v2

import android.view.KeyEvent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.WatchedIndicatorBehavior
import org.jellyfin.androidtv.ui.base.Badge
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Seekbar
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.button.IconButtonDefaults
import org.jellyfin.androidtv.ui.base.focusBorderColor
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.DialogDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowColors
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.design.Tokens
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.compose.koinInject

@Composable
fun DetailActionButton(
	label: String,
	icon: ImageVector,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	detail: String? = null,
	isActive: Boolean = false,
	activeColor: Color = Color.Unspecified,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val focusColor = focusBorderColor()
	val resolvedActiveColor = if (activeColor == Color.Unspecified) focusColor else activeColor
	val focusContentColor = if (focusColor.luminance() > 0.4f) JellyfinTheme.colorScheme.surface else JellyfinTheme.colorScheme.onSurface

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier.width(80.dp),
	) {
		IconButton(
			onClick = onClick,
			shape = JellyfinTheme.shapes.large,
			colors =
				IconButtonDefaults.colors(
					containerColor = JellyfinTheme.colorScheme.outlineVariant,
					contentColor = if (isActive) resolvedActiveColor else JellyfinTheme.colorScheme.onSurface,
					focusedContainerColor = focusColor.copy(alpha = 0.95f),
					focusedContentColor = if (isActive) resolvedActiveColor else focusContentColor,
				),
			contentPadding = PaddingValues(16.dp),
			interactionSource = interactionSource,
			modifier =
				Modifier.border(
					1.dp,
					JellyfinTheme.colorScheme.outlineVariant,
					JellyfinTheme.shapes.large,
				),
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(22.dp),
			)
		}

		Spacer(modifier = Modifier.height(6.dp))

		Text(
			text = label,
			style = JellyfinTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
			color = JellyfinTheme.colorScheme.textPrimary,
			textAlign = TextAlign.Center,
			maxLines = 1,
		)

		if (detail != null) {
			Text(
				text = detail,
				style = JellyfinTheme.typography.labelSmall,
				color = if (isFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textHint,
				textAlign = TextAlign.Center,
				maxLines = if (isFocused) 2 else 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
fun MediaBadgeChip(
	badge: MediaBadge,
	modifier: Modifier = Modifier,
) {
	Text(
		text = badge.label,
		modifier =
			modifier
				.background(
					JellyfinTheme.colorScheme.textSecondary,
					JellyfinTheme.shapes.extraSmall,
				).padding(horizontal = 6.dp, vertical = 2.dp),
		style = JellyfinTheme.typography.labelSmall,
		fontWeight = FontWeight.W700,
		color = VegafoXColors.Background,
		letterSpacing = 0.5.sp,
	)
}

@Composable
fun InfoItemText(
	text: String,
	modifier: Modifier = Modifier,
) {
	Text(
		text = text,
		modifier = modifier,
		style = JellyfinTheme.typography.listHeader,
		color = JellyfinTheme.colorScheme.textSecondary,
	)
}

@Composable
fun InfoItemBadge(
	text: String,
	bgColor: Color = InfoRowColors.Default.first,
	color: Color = VegafoXColors.Background,
) {
	Text(
		text = text,
		modifier =
			Modifier
				.background(
					bgColor,
					JellyfinTheme.shapes.extraSmall,
				).padding(horizontal = 6.dp, vertical = 2.dp),
		style = JellyfinTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W900),
		color = color,
	)
}

@Composable
fun RuntimeInfo(ticks: Long) {
	val context = LocalContext.current
	Row(verticalAlignment = Alignment.CenterVertically) {
		Icon(
			imageVector = VegafoXIcons.Schedule,
			contentDescription = null,
			tint = JellyfinTheme.colorScheme.textSecondary,
			modifier = Modifier.size(15.dp).padding(end = 4.dp),
		)
		InfoItemText(TimeUtils.formatRuntimeHoursMinutes(context, ticks / 10_000))
	}
}

@Composable
fun InfoItemSeparator() {
	Text(
		text = "\u2022",
		modifier = Modifier.padding(horizontal = 8.dp),
		style = JellyfinTheme.typography.labelSmall,
		color = JellyfinTheme.colorScheme.textDisabled,
	)
}

@Composable
fun MetadataGroup(
	items: List<Pair<String, String>>,
	modifier: Modifier = Modifier,
) {
	if (items.isEmpty()) return

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.outlineVariant)
				.border(
					1.dp,
					JellyfinTheme.colorScheme.outlineVariant,
					JellyfinTheme.shapes.small,
				).padding(vertical = 12.dp),
		verticalAlignment = Alignment.Top,
	) {
		items.forEachIndexed { index, (label, value) ->
			Column(
				modifier =
					Modifier
						.weight(1f)
						.padding(horizontal = 18.dp),
			) {
				Text(
					text = label.uppercase(),
					style = JellyfinTheme.typography.labelSmall.copy(fontWeight = FontWeight.W600),
					color = JellyfinTheme.colorScheme.textDisabled,
					letterSpacing = 0.5.sp,
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = value,
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.onSurface,
				)
			}
			if (index < items.lastIndex) {
				Box(
					Modifier
						.width(1.dp)
						.height(36.dp)
						.background(JellyfinTheme.colorScheme.outlineVariant),
				)
			}
		}
	}
}

@Composable
fun CastCard(
	name: String,
	role: String?,
	imageUrl: String?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier =
			modifier
				.width(100.dp)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
	) {
		Box(
			modifier =
				Modifier
					.size(72.dp)
					.clip(CircleShape)
					.then(
						if (isFocused) {
							Modifier.border(2.dp, focusBorderColor(), CircleShape)
						} else {
							Modifier.border(1.dp, VegafoXColors.Divider, CircleShape)
						},
					).background(JellyfinTheme.colorScheme.outlineVariant),
			contentAlignment = Alignment.Center,
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = name,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			} else {
				Text(
					text = name.firstOrNull()?.toString() ?: "",
					style = JellyfinTheme.typography.headlineLarge,
					fontWeight = FontWeight.W600,
					color = JellyfinTheme.colorScheme.textDisabled,
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		Text(
			text = name,
			style = JellyfinTheme.typography.labelMedium.copy(fontSize = 12.sp),
			color = JellyfinTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		if (role != null) {
			Text(
				text = role,
				style = JellyfinTheme.typography.labelSmall.copy(fontSize = 11.sp),
				color = JellyfinTheme.colorScheme.textHint,
				textAlign = TextAlign.Center,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

@Composable
fun SeasonCard(
	name: String,
	imageUrl: String?,
	isWatched: Boolean,
	unplayedCount: Int?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	item: BaseItemDto? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier =
			modifier.clickable(
				interactionSource = interactionSource,
				indication = null,
				onClick = onClick,
			),
	) {
		Box(
			modifier =
				Modifier
					.width(170.dp)
					.height(255.dp)
					.clip(JellyfinTheme.shapes.button)
					.then(
						if (isFocused) {
							Modifier.border(2.dp, focusBorderColor(), JellyfinTheme.shapes.button)
						} else {
							Modifier.border(2.dp, Color.Transparent, JellyfinTheme.shapes.button)
						},
					).background(JellyfinTheme.colorScheme.outlineVariant),
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = name,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			} else {
				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = name,
						style = JellyfinTheme.typography.titleMedium,
						color = JellyfinTheme.colorScheme.textSecondary,
						textAlign = TextAlign.Center,
						modifier = Modifier.padding(12.dp),
					)
				}
			}

			if (item != null) {
				DetailsWatchIndicator(
					item = item,
					modifier =
						Modifier
							.align(Alignment.TopEnd)
							.padding(6.dp),
				)
			} else {
				SeasonCardLegacyBadge(
					isWatched = isWatched,
					unplayedCount = unplayedCount,
					modifier = Modifier.align(Alignment.TopEnd),
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		Text(
			text = name,
			style = JellyfinTheme.typography.bodyMedium,
			color = JellyfinTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.width(140.dp),
		)
	}
}

@Composable
private fun SeasonCardLegacyBadge(
	isWatched: Boolean,
	unplayedCount: Int?,
	modifier: Modifier = Modifier,
) {
	val userPreferences = koinInject<UserPreferences>()
	val watchedIndicatorBehavior = userPreferences[UserPreferences.watchedIndicatorBehavior]
	if (watchedIndicatorBehavior == WatchedIndicatorBehavior.NEVER) return

	if (isWatched) {
		Badge(
			modifier =
				modifier
					.padding(6.dp)
					.size(22.dp),
		) {
			Icon(
				imageVector = VegafoXIcons.Visibility,
				contentDescription = null,
				modifier = Modifier.size(12.dp),
			)
		}
	} else if (unplayedCount != null && unplayedCount > 0) {
		if (watchedIndicatorBehavior == WatchedIndicatorBehavior.HIDE_UNWATCHED) return

		Badge(
			modifier =
				modifier
					.padding(6.dp)
					.sizeIn(minWidth = 22.dp, minHeight = 22.dp),
		) {
			Text(
				text = unplayedCount.toString(),
			)
		}
	}
}

@Composable
fun EpisodeCard(
	episodeNumber: Int?,
	title: String,
	runtime: String?,
	imageUrl: String?,
	progress: Double,
	isCurrent: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isPlayed: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val borderColor = focusBorderColor()

	Column(
		modifier =
			modifier
				.width(CardDimensions.landscapeWidth)
				.clip(JellyfinTheme.shapes.button)
				.then(
					if (isCurrent) {
						Modifier.border(
							2.dp,
							borderColor.copy(alpha = 0.4f),
							JellyfinTheme.shapes.small,
						)
					} else if (isFocused) {
						Modifier.border(
							2.dp,
							borderColor,
							JellyfinTheme.shapes.small,
						)
					} else {
						Modifier.border(2.dp, Color.Transparent, JellyfinTheme.shapes.small)
					},
				).then(
					if (isCurrent) {
						Modifier.background(borderColor.copy(alpha = 0.08f))
					} else {
						Modifier
					},
				).clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(CardDimensions.landscapeHeight)
					.background(JellyfinTheme.colorScheme.surfaceDim),
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}

			if (isPlayed && progress <= 0) {
				EpisodeWatchedBadge(
					modifier =
						Modifier
							.align(Alignment.TopEnd)
							.padding(4.dp),
				)
			}

			if (progress > 0) {
				Box(
					modifier =
						Modifier
							.align(Alignment.BottomStart)
							.fillMaxWidth()
							.height(2.dp)
							.background(VegafoXColors.Background.copy(alpha = 0.5f)),
				) {
					Box(
						modifier =
							Modifier
								.fillMaxWidth(fraction = (progress / 100.0).toFloat().coerceIn(0f, 1f))
								.height(2.dp)
								.background(JellyfinTheme.colorScheme.primary),
					)
				}
			}
		}

		Row(
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = stringResource(R.string.lbl_episode_number_str, episodeNumber?.toString() ?: "?"),
				style = JellyfinTheme.typography.labelMedium.copy(fontWeight = FontWeight.W700),
				color = JellyfinTheme.colorScheme.textHint,
			)
			Spacer(modifier = Modifier.width(6.dp))
			Text(
				text = title,
				style = JellyfinTheme.typography.bodySmall,
				color = JellyfinTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f),
			)
			if (runtime != null) {
				Spacer(modifier = Modifier.width(6.dp))
				Text(
					text = runtime,
					style = JellyfinTheme.typography.labelSmall,
					color = JellyfinTheme.colorScheme.textDisabled,
				)
			}
		}
	}
}

@Composable
fun SeasonEpisodeItem(
	episodeNumber: Int?,
	title: String,
	overview: String?,
	runtime: String?,
	imageUrl: String?,
	progress: Double,
	isPlayed: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val seasonBorderColor = focusBorderColor()

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.clip(JellyfinTheme.shapes.small)
				.background(
					if (isFocused) {
						JellyfinTheme.colorScheme.outlineVariant
					} else {
						JellyfinTheme.colorScheme.outlineVariant
					},
				).then(
					if (isFocused) {
						Modifier.border(
							2.dp,
							seasonBorderColor.copy(alpha = 0.4f),
							JellyfinTheme.shapes.small,
						)
					} else {
						Modifier.border(2.dp, Color.Transparent, JellyfinTheme.shapes.small)
					},
				).clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
	) {
		Box(
			modifier =
				Modifier
					.width(240.dp)
					.height(135.dp)
					.background(JellyfinTheme.colorScheme.surfaceDim),
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}

			if (progress > 0) {
				Box(
					modifier =
						Modifier
							.align(Alignment.BottomStart)
							.fillMaxWidth()
							.height(3.dp)
							.background(VegafoXColors.Background.copy(alpha = 0.5f)),
				) {
					Box(
						modifier =
							Modifier
								.fillMaxWidth(fraction = (progress / 100.0).toFloat().coerceIn(0f, 1f))
								.height(3.dp)
								.background(JellyfinTheme.colorScheme.primary),
					)
				}
			}

			if (isPlayed && progress <= 0) {
				EpisodeWatchedBadge(
					modifier =
						Modifier
							.align(Alignment.TopEnd)
							.padding(6.dp),
				)
			}
		}

		Spacer(modifier = Modifier.width(14.dp))

		Column(
			modifier =
				Modifier
					.weight(1f)
					.padding(vertical = 10.dp, horizontal = 0.dp)
					.padding(end = 14.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(
					text = stringResource(R.string.lbl_episode_label, episodeNumber?.toString() ?: "?"),
					style = JellyfinTheme.typography.bodySmall.copy(fontWeight = FontWeight.W600),
					color = JellyfinTheme.colorScheme.textHint,
				)
				if (runtime != null) {
					Text(
						text = runtime,
						style = JellyfinTheme.typography.bodySmall,
						color = JellyfinTheme.colorScheme.textDisabled,
					)
				}
			}

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = title,
				style = JellyfinTheme.typography.titleMedium,
				color = JellyfinTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)

			if (overview != null) {
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = overview,
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textHint,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
					lineHeight = 20.sp,
				)
			}
		}
	}
}

@Composable
fun SectionHeader(
	title: String,
	modifier: Modifier = Modifier,
) {
	Text(
		text = title,
		modifier = modifier.padding(bottom = 10.dp),
		style = JellyfinTheme.typography.titleLarge,
		color = JellyfinTheme.colorScheme.onSurface,
	)
}

@Composable
fun PosterImage(
	imageUrl: String?,
	isLandscape: Boolean = false,
	isSquare: Boolean = false,
	modifier: Modifier = Modifier,
	item: BaseItemDto? = null,
) {
	Box(
		modifier =
			modifier
				.then(
					when {
						isSquare ->
							Modifier
								.width(200.dp)
								.height(200.dp)
						isLandscape ->
							Modifier
								.width(280.dp)
								.height(158.dp)
						else ->
							Modifier
								.width(165.dp)
								.height(248.dp)
					},
				).clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.outlineVariant),
		contentAlignment = Alignment.Center,
	) {
		if (imageUrl != null) {
			val placeholder = rememberGradientPlaceholder()
			val errorFallback = rememberErrorPlaceholder()
			AsyncImage(
				model = imageUrl,
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				placeholder = placeholder,
				error = errorFallback,
			)
		} else {
			Icon(
				imageVector = VegafoXIcons.Movie,
				contentDescription = null,
				modifier = Modifier.size(64.dp),
				tint = JellyfinTheme.colorScheme.outlineVariant,
			)
		}

		if (item != null) {
			ItemCardOverlays(item = item, iconSize = 20.dp, padding = 6.dp)
		}
	}
}

@Composable
fun DetailBackdrop(
	imageUrl: String?,
	modifier: Modifier = Modifier,
	blurAmount: Int = 0,
) {
	Box(modifier = modifier.fillMaxSize()) {
		if (imageUrl != null) {
			val placeholder = rememberGradientPlaceholder()
			AsyncImage(
				model = imageUrl,
				contentDescription = null,
				modifier =
					Modifier
						.fillMaxSize()
						.then(if (blurAmount > 0) Modifier.blur(blurAmount.dp) else Modifier),
				contentScale = ContentScale.Crop,
				alpha = 0.8f,
				placeholder = placeholder,
			)
		}

		Box(
			modifier =
				Modifier
					.fillMaxSize()
					.background(
						Brush.verticalGradient(
							colorStops =
								arrayOf(
									0.0f to Color.Transparent,
									0.3f to Color.Transparent,
									0.5f to JellyfinTheme.colorScheme.gradientEnd.copy(alpha = 0.25f),
									0.65f to JellyfinTheme.colorScheme.gradientEnd.copy(alpha = 0.63f),
									0.8f to JellyfinTheme.colorScheme.gradientEnd.copy(alpha = 0.88f),
									1.0f to JellyfinTheme.colorScheme.gradientEnd,
								),
						),
					),
		)
	}
}

@Composable
fun SimilarItemCard(
	title: String,
	imageUrl: String?,
	year: Int?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocused: (() -> Unit)? = null,
	item: BaseItemDto? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	LaunchedEffect(isFocused) {
		if (isFocused) onFocused?.invoke()
	}

	Column(
		modifier =
			modifier
				.width(140.dp)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(200.dp)
					.clip(JellyfinTheme.shapes.button)
					.then(
						if (isFocused) {
							Modifier.border(2.dp, focusBorderColor(), JellyfinTheme.shapes.button)
						} else {
							Modifier.border(2.dp, Color.Transparent, JellyfinTheme.shapes.button)
						},
					).background(JellyfinTheme.colorScheme.outlineVariant),
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}

			if (item != null) {
				ItemCardOverlays(item = item)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		Text(
			text = title,
			style = JellyfinTheme.typography.labelMedium,
			color = JellyfinTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		if (year != null) {
			Text(
				text = year.toString(),
				style = JellyfinTheme.typography.labelSmall,
				color = JellyfinTheme.colorScheme.textHint,
			)
		}
	}
}

@Composable
fun LandscapeItemCard(
	title: String,
	imageUrl: String?,
	subtitle: String? = null,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocused: (() -> Unit)? = null,
	item: BaseItemDto? = null,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	LaunchedEffect(isFocused) {
		if (isFocused) onFocused?.invoke()
	}

	Column(
		modifier =
			modifier
				.width(CardDimensions.landscapeWidth)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.height(CardDimensions.landscapeHeight)
					.clip(JellyfinTheme.shapes.button)
					.then(
						if (isFocused) {
							Modifier.border(2.dp, focusBorderColor(), JellyfinTheme.shapes.button)
						} else {
							Modifier.border(2.dp, Color.Transparent, JellyfinTheme.shapes.button)
						},
					).background(JellyfinTheme.colorScheme.outlineVariant),
		) {
			if (imageUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = imageUrl,
					contentDescription = title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}

			if (item != null) {
				ItemCardOverlays(item = item)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		Text(
			text = title,
			style = JellyfinTheme.typography.labelMedium,
			color = JellyfinTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)

		if (subtitle != null) {
			Text(
				text = subtitle,
				style = JellyfinTheme.typography.labelSmall,
				color = JellyfinTheme.colorScheme.textHint,
			)
		}
	}
}

/**
 * Track item card for music album/playlist track lists.
 * When [onMoveUp]/[onMoveDown] are provided, shows reorder chevrons and handles DPAD left/right.
 */
@Composable
fun TrackItemCard(
	trackNumber: Int,
	title: String,
	artist: String?,
	runtime: String?,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onFocused: (() -> Unit)? = null,
	onMenuAction: (() -> Unit)? = null,
	onMoveUp: (() -> Unit)? = null,
	onMoveDown: (() -> Unit)? = null,
	isFirst: Boolean = false,
	isLast: Boolean = false,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()
	val canReorder = onMoveUp != null || onMoveDown != null
	val focusColor = if (canReorder) focusBorderColor() else JellyfinTheme.colorScheme.onSurface

	LaunchedEffect(isFocused) {
		if (isFocused) onFocused?.invoke()
	}

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.onKeyEvent { event ->
					if (event.nativeKeyEvent.action != KeyEvent.ACTION_DOWN) return@onKeyEvent false
					when {
						// Menu key opens the track action dialog
						event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU ||
							event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_INFO -> {
							onMenuAction?.invoke()
							onMenuAction != null
						}
						// Long-press center/enter also opens the menu
						event.nativeKeyEvent.isLongPress &&
							(
								event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
									event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER
							) -> {
							onMenuAction?.invoke()
							onMenuAction != null
						}
						// Reorder with left/right
						canReorder && event.key == Key.DirectionLeft -> {
							if (!isFirst) {
								onMoveUp?.invoke()
								true
							} else {
								false
							}
						}
						canReorder && event.key == Key.DirectionRight -> {
							if (!isLast) {
								onMoveDown?.invoke()
								true
							} else {
								false
							}
						}
						else -> false
					}
				}.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				).focusable(interactionSource = interactionSource)
				.background(
					color = if (isFocused) JellyfinTheme.colorScheme.outlineVariant else Color.Transparent,
					shape = JellyfinTheme.shapes.small,
				).border(
					width = if (isFocused) 2.dp else 0.dp,
					color = if (isFocused) focusColor else Color.Transparent,
					shape = JellyfinTheme.shapes.small,
				).padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = trackNumber.toString(),
			style = JellyfinTheme.typography.titleMedium,
			color = JellyfinTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.width(40.dp),
		)

		Column(
			modifier = Modifier.weight(1f),
		) {
			Text(
				text = title,
				style = JellyfinTheme.typography.titleLarge.copy(fontWeight = FontWeight.W500),
				color = JellyfinTheme.colorScheme.onSurface,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			if (artist != null) {
				Text(
					text = artist,
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}

		if (runtime != null) {
			Text(
				text = runtime,
				style = JellyfinTheme.typography.titleMedium,
				color = JellyfinTheme.colorScheme.onSurfaceVariant,
			)
		}

		if (canReorder && isFocused) {
			Spacer(modifier = Modifier.width(12.dp))

			Column(
				verticalArrangement = Arrangement.spacedBy(2.dp),
			) {
				Icon(
					imageVector = VegafoXIcons.KeyboardArrowUp,
					contentDescription = stringResource(R.string.cd_move_up),
					modifier = Modifier.size(18.dp),
					tint = if (!isFirst) focusColor else JellyfinTheme.colorScheme.textDisabled,
				)
				Icon(
					imageVector = VegafoXIcons.KeyboardArrowDown,
					contentDescription = stringResource(R.string.cd_move_down),
					modifier = Modifier.size(18.dp),
					tint = if (!isLast) focusColor else JellyfinTheme.colorScheme.textDisabled,
				)
			}
		}
	}
}

/**
 * Modern track/version selector dialog styled to match the detail page refresh.
 * Renders a rounded, glass-morphism panel with focusable list items.
 */
@Composable
fun TrackSelectorDialog(
	title: String,
	options: List<String>,
	selectedIndex: Int,
	onSelect: (Int) -> Unit,
	onDismiss: () -> Unit,
) {
	val initialFocusRequester = remember { FocusRequester() }

	val selectorFocusColor = focusBorderColor()

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = DialogDimensions.standardMinWidth, max = DialogDimensions.standardMaxWidth)
						.clip(JellyfinTheme.shapes.dialog)
						.background(JellyfinTheme.colorScheme.dialogScrim)
						.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
						.padding(vertical = 20.dp),
			) {
				Text(
					text = title,
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.onSurface,
					modifier =
						Modifier
							.padding(horizontal = 24.dp)
							.padding(bottom = 12.dp),
				)

				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(JellyfinTheme.colorScheme.outlineVariant),
				)

				Spacer(modifier = Modifier.height(8.dp))

				LazyColumn {
					itemsIndexed(options) { index, option ->
						val interactionSource = remember { MutableInteractionSource() }
						val isFocused by interactionSource.collectIsFocusedAsState()
						val isSelected = index == selectedIndex

						val focusModifier =
							if (index == selectedIndex.coerceIn(0, options.lastIndex)) {
								Modifier.focusRequester(initialFocusRequester)
							} else {
								Modifier
							}

						Row(
							modifier =
								focusModifier
									.fillMaxWidth()
									.clickable(
										interactionSource = interactionSource,
										indication = null,
									) { onSelect(index) }
									.focusable(interactionSource = interactionSource)
									.background(
										when {
											isFocused -> JellyfinTheme.colorScheme.outlineVariant
											else -> Color.Transparent
										},
									).padding(horizontal = 24.dp, vertical = 14.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							Box(
								modifier =
									Modifier
										.size(18.dp)
										.border(
											width = 2.dp,
											color = if (isSelected) focusBorderColor() else JellyfinTheme.colorScheme.textDisabled,
											shape = CircleShape,
										),
								contentAlignment = Alignment.Center,
							) {
								if (isSelected) {
									Box(
										modifier =
											Modifier
												.size(10.dp)
												.background(focusBorderColor(), CircleShape),
									)
								}
							}

							Spacer(modifier = Modifier.width(16.dp))

							Text(
								text = option,
								style =
									JellyfinTheme.typography.titleMedium.copy(
										fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400,
									),
								color =
									when {
										isSelected -> focusBorderColor()
										isFocused -> JellyfinTheme.colorScheme.onSurface
										else -> JellyfinTheme.colorScheme.textPrimary
									},
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
								modifier = Modifier.weight(1f),
							)
						}
					}
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}

/**
 * Data class representing an action in the track context menu.
 */
@Stable
data class TrackAction(
	val label: String,
	val onClick: () -> Unit,
)

/**
 * Dialog showing available actions for a track item (play from here, play, add to queue, etc.).
 * Mirrors the old PopupMenu from ItemListFragment.
 */
@Composable
fun TrackActionDialog(
	trackTitle: String,
	actions: List<TrackAction>,
	onDismiss: () -> Unit,
) {
	val initialFocusRequester = remember { FocusRequester() }

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = DialogDimensions.standardMinWidth, max = DialogDimensions.standardMaxWidth)
						.clip(JellyfinTheme.shapes.dialog)
						.background(JellyfinTheme.colorScheme.dialogScrim)
						.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
						.padding(vertical = 20.dp),
			) {
				Text(
					text = trackTitle,
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier =
						Modifier
							.padding(horizontal = 24.dp)
							.padding(bottom = 12.dp),
				)

				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(JellyfinTheme.colorScheme.outlineVariant),
				)

				Spacer(modifier = Modifier.height(8.dp))

				actions.forEachIndexed { index, action ->
					val interactionSource = remember { MutableInteractionSource() }
					val isFocused by interactionSource.collectIsFocusedAsState()

					val focusModifier =
						if (index == 0) {
							Modifier.focusRequester(initialFocusRequester)
						} else {
							Modifier
						}

					Row(
						modifier =
							focusModifier
								.fillMaxWidth()
								.clickable(
									interactionSource = interactionSource,
									indication = null,
								) {
									action.onClick()
									onDismiss()
								}.focusable(interactionSource = interactionSource)
								.background(
									if (isFocused) JellyfinTheme.colorScheme.outlineVariant else Color.Transparent,
								).padding(horizontal = 24.dp, vertical = 14.dp),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = action.label,
							style = JellyfinTheme.typography.bodyLarge,
							color = if (isFocused) JellyfinTheme.colorScheme.onSurface else JellyfinTheme.colorScheme.textPrimary,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)
					}
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}

/**
 * Shared overlay layer for item cards showing favorite, watched, and progress indicators.
 * Must be called from a [BoxScope] receiver.
 */
@Composable
private fun BoxScope.ItemCardOverlays(
	item: BaseItemDto,
	iconSize: Dp = 18.dp,
	padding: Dp = 4.dp,
) {
	if (item.userData?.isFavorite == true) {
		Icon(
			imageVector = VegafoXIcons.Favorite,
			contentDescription = null,
			tint = VegafoXColors.Error,
			modifier =
				Modifier
					.align(Alignment.TopStart)
					.padding(padding)
					.size(iconSize),
		)
	}

	DetailsWatchIndicator(
		item = item,
		modifier =
			Modifier
				.align(Alignment.TopEnd)
				.padding(padding),
	)

	val playedPercentage =
		item.userData
			?.playedPercentage
			?.toFloat()
			?.div(100f)
			?.coerceIn(0f, 1f)
			?.takeIf { it > 0f && it < 1f }
	if (playedPercentage != null) {
		Box(
			modifier =
				Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.padding(Tokens.Space.spaceXs),
		) {
			Seekbar(
				progress = playedPercentage,
				enabled = false,
				modifier =
					Modifier
						.fillMaxWidth()
						.height(4.dp),
			)
		}
	}
}

/**
 * Watched/unplayed indicator for detail screen cards.
 * Mirrors the behavior of [org.jellyfin.androidtv.ui.composable.item.ItemCardBaseItemOverlay]'s
 * WatchIndicator, respecting the [WatchedIndicatorBehavior] user preference.
 */
@Composable
private fun DetailsWatchIndicator(
	item: BaseItemDto,
	modifier: Modifier = Modifier,
) {
	val userPreferences = koinInject<UserPreferences>()
	val watchedIndicatorBehavior = userPreferences[UserPreferences.watchedIndicatorBehavior]

	if (watchedIndicatorBehavior == WatchedIndicatorBehavior.NEVER) return
	if (watchedIndicatorBehavior == WatchedIndicatorBehavior.EPISODES_ONLY && item.type != BaseItemKind.EPISODE) return

	val isPlayed = item.userData?.played == true
	val unplayedItems = item.userData?.unplayedItemCount?.takeIf { it > 0 }

	if (isPlayed) {
		Badge(
			modifier = modifier.size(20.dp),
		) {
			Icon(
				imageVector = VegafoXIcons.Visibility,
				contentDescription = null,
				modifier = Modifier.size(10.dp),
			)
		}
	} else if (unplayedItems != null) {
		if (watchedIndicatorBehavior == WatchedIndicatorBehavior.HIDE_UNWATCHED) return

		Badge(
			modifier = modifier.sizeIn(minWidth = 20.dp, minHeight = 20.dp),
		) {
			Text(
				text = unplayedItems.toString(),
			)
		}
	}
}

/**
 * Simple watched badge for episode cards.
 * Respects the [WatchedIndicatorBehavior] user preference.
 */
@Composable
private fun EpisodeWatchedBadge(modifier: Modifier = Modifier) {
	val userPreferences = koinInject<UserPreferences>()
	val watchedIndicatorBehavior = userPreferences[UserPreferences.watchedIndicatorBehavior]

	if (watchedIndicatorBehavior == WatchedIndicatorBehavior.NEVER) return

	Badge(
		modifier = modifier.size(20.dp),
	) {
		Icon(
			imageVector = VegafoXIcons.Visibility,
			contentDescription = null,
			modifier = Modifier.size(10.dp),
		)
	}
}

// =====================================================================
// Cinema Immersive composables
// =====================================================================

private val CinemaEaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Genre tag line with orange dash and uppercase text.
 */
@Composable
fun CinemaGenreTag(
	text: String,
	modifier: Modifier = Modifier,
) {
	if (text.isBlank()) return
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier,
	) {
		Box(
			Modifier
				.width(20.dp)
				.height(2.dp)
				.background(VegafoXColors.OrangePrimary),
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = text.uppercase(),
			style =
				JellyfinTheme.typography.labelSmall.copy(
					fontSize = 11.sp,
					fontWeight = FontWeight.W700,
					letterSpacing = 2.sp,
				),
			color = VegafoXColors.OrangePrimary,
		)
	}
}

/**
 * Metadata pill with configurable border and text colors.
 */
@Composable
fun CinemaPill(
	text: String,
	modifier: Modifier = Modifier,
	borderColor: Color = VegafoXColors.TextHint,
	textColor: Color = VegafoXColors.TextSecondary,
) {
	Text(
		text = text,
		modifier =
			modifier
				.border(1.dp, borderColor, RoundedCornerShape(4.dp))
				.padding(horizontal = 10.dp, vertical = 4.dp),
		style =
			JellyfinTheme.typography.labelSmall.copy(
				fontSize = 11.sp,
				fontWeight = FontWeight.W700,
			),
		color = textColor,
	)
}

/**
 * Icon + label action chip for secondary detail actions.
 * Focus: scale 1.06, glow + border OrangePrimary.
 * Active state: border + icon in activeColor, background OrangeSoft.
 */
@Composable
fun CinemaActionChip(
	icon: ImageVector,
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	isActive: Boolean = false,
	activeColor: Color = VegafoXColors.OrangePrimary,
) {
	var isFocused by remember { mutableStateOf(false) }
	var navigating by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.06f else 1f,
		animationSpec = tween(150, easing = CinemaEaseOutCubic),
		label = "cinemaChipScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = tween(150, easing = CinemaEaseOutCubic),
		label = "cinemaChipGlow",
	)

	val safeClick: () -> Unit = {
		if (!navigating) {
			navigating = true
			onClick()
		}
	}

	LaunchedEffect(navigating) {
		if (navigating) {
			kotlinx.coroutines.delay(400L)
			navigating = false
		}
	}

	val bgColor =
		when {
			isActive -> VegafoXColors.OrangeSoft
			else -> VegafoXColors.Divider
		}

	val chipBorderColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary
			isActive -> activeColor
			else -> VegafoXColors.Divider
		}

	val iconTint =
		when {
			isActive -> activeColor
			isFocused -> VegafoXColors.OrangePrimary
			else -> VegafoXColors.TextPrimary
		}

	Box(
		modifier =
			modifier
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
											VegafoXColors.OrangeGlow.copy(
												alpha = VegafoXColors.OrangeGlow.alpha * glowAlpha,
											),
											Color.Transparent,
										),
									radius = size.maxDimension * 1.0f,
								),
							cornerRadius = CornerRadius(12.dp.toPx()),
						)
					}
				}.border(1.dp, chipBorderColor, RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.background(bgColor)
				.onFocusChanged { isFocused = it.isFocused }
				.focusable()
				.onKeyEvent { event ->
					if ((event.key == Key.Enter || event.key == Key.DirectionCenter) &&
						event.type == KeyEventType.KeyUp
					) {
						safeClick()
						true
					} else {
						false
					}
				}.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = safeClick,
				).padding(12.dp),
		contentAlignment = Alignment.Center,
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(20.dp),
				tint = iconTint,
			)
			Text(
				text = label,
				style = JellyfinTheme.typography.labelSmall.copy(fontSize = 10.sp),
				color = if (isFocused) VegafoXColors.OrangePrimary else VegafoXColors.TextSecondary,
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}
	}
}

/**
 * Poster 2:3 with progress bar and watched time for cinema layout.
 */
@Composable
fun CinemaPosterColumn(
	posterUrl: String?,
	playedPercentage: Double,
	watchedMinutes: Int,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier.width(CardDimensions.landscapeWidth),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.aspectRatio(2f / 3f)
					.clip(RoundedCornerShape(16.dp))
					.border(1.dp, VegafoXColors.Divider, RoundedCornerShape(16.dp))
					.background(JellyfinTheme.colorScheme.surfaceDim),
		) {
			if (posterUrl != null) {
				val placeholder = rememberGradientPlaceholder()
				val errorFallback = rememberErrorPlaceholder()
				AsyncImage(
					model = posterUrl,
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop,
					placeholder = placeholder,
					error = errorFallback,
				)
			}
		}

		if (playedPercentage > 0) {
			Spacer(modifier = Modifier.height(8.dp))

			Box(
				modifier =
					Modifier
						.fillMaxWidth()
						.height(3.dp)
						.clip(RoundedCornerShape(1.5.dp))
						.background(VegafoXColors.Divider),
			) {
				Box(
					modifier =
						Modifier
							.fillMaxWidth(fraction = (playedPercentage / 100.0).toFloat().coerceIn(0f, 1f))
							.height(3.dp)
							.background(VegafoXColors.OrangePrimary, RoundedCornerShape(1.5.dp)),
				)
			}

			Spacer(modifier = Modifier.height(6.dp))

			Text(
				text = "${watchedMinutes}m " + stringResource(R.string.lbl_watched).lowercase(),
				style = JellyfinTheme.typography.labelSmall.copy(fontSize = 12.sp),
				color = VegafoXColors.TextHint,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}
}
