package org.jellyfin.androidtv.ui.browsing.v2

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.DialogDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowColors
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowCompactRatings
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemBadge
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemSeparator
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemText
import org.jellyfin.androidtv.ui.itemdetail.v2.RuntimeInfo
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.design.Tokens
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.compose.koinInject

/**
 * Maps item kind to a short display label for the type badge.
 */
private fun getTypeBadgeLabel(kind: BaseItemKind?): String? =
	when (kind) {
		BaseItemKind.MOVIE -> "MOVIE"
		BaseItemKind.SERIES -> "SERIES"
		BaseItemKind.EPISODE -> "EPISODE"
		BaseItemKind.MUSIC_ALBUM -> "ALBUM"
		BaseItemKind.MUSIC_ARTIST -> "ARTIST"
		BaseItemKind.AUDIO -> "SONG"
		BaseItemKind.BOOK -> "BOOK"
		BaseItemKind.BOX_SET -> "COLLECTION"
		BaseItemKind.PERSON -> "PERSON"
		else -> null
	}

/**
 * Returns the badge background color for a given item kind.
 */
@Composable
private fun getTypeBadgeColor(kind: BaseItemKind?): Color =
	when (kind) {
		BaseItemKind.SERIES -> JellyfinTheme.colorScheme.secondary
		else -> JellyfinTheme.colorScheme.primary
	}

/**
 * Builds a compact metadata string: "2012  R  1h 30m  ★ 6.9"
 */
fun buildMetadataString(
	item: BaseItemDto,
	context: android.content.Context? = null,
): String {
	val parts = mutableListOf<String>()
	item.communityRating?.let { parts.add("★ ${String.format("%.1f", it)}") }
	item.productionYear?.let { parts.add(it.toString()) }
	if (item.type == BaseItemKind.SERIES) {
		item.status?.let { parts.add(it) }
	}
	item.officialRating?.let { if (it.isNotBlank()) parts.add(it) }
	if (item.type == BaseItemKind.MOVIE) {
		item.runTimeTicks?.let { ticks ->
			val runtimeMs = ticks / 10_000
			if (context != null) {
				parts.add(TimeUtils.formatRuntimeHoursMinutes(context, runtimeMs))
			} else {
				val totalMinutes = (runtimeMs / 60_000).toInt()
				val hours = totalMinutes / 60
				val minutes = totalMinutes % 60
				if (hours > 0) {
					parts.add("${hours}h ${minutes}m")
				} else {
					parts.add("${minutes}m")
				}
			}
		}
	}
	return parts.joinToString("  ")
}

/**
 * A poster card for the library grid, matching the Jellyfin web/webOS style.
 * Shows: type badge overlay, poster image, title, year / officialRating / ★ communityRating.
 * @param showLabels Whether to show the title and metadata below the poster image.
 */
@Composable
fun LibraryPosterCard(
	item: BaseItemDto,
	imageUrl: String?,
	cardWidth: Int,
	cardHeight: Int,
	onClick: () -> Unit,
	onFocused: () -> Unit,
	showLabels: Boolean = true,
	showBadge: Boolean = false,
	modifier: Modifier = Modifier,
) {
	TvFocusCard(
		onClick = onClick,
		modifier =
			modifier
				.width(cardWidth.dp)
				.onFocusChanged { state ->
					if (state.isFocused) onFocused()
				},
	) {
		Column(
			horizontalAlignment = Alignment.Start,
		) {
			// Poster image with type badge overlay
			Box(
				modifier =
					Modifier
						.size(width = cardWidth.dp, height = cardHeight.dp)
						.clip(JellyfinTheme.shapes.medium)
						.background(JellyfinTheme.colorScheme.surfaceDim),
			) {
				if (imageUrl != null) {
					val placeholder = rememberGradientPlaceholder()
					val errorFallback = rememberErrorPlaceholder()
					AsyncImage(
						model = imageUrl,
						contentDescription = item.name,
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
						Icon(
							imageVector = VegafoXIcons.Movie,
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							tint = JellyfinTheme.colorScheme.textDisabled,
						)
					}
				}

				// Type badge — top-left corner
				if (showBadge) {
					val badgeLabel = getTypeBadgeLabel(item.type)
					if (badgeLabel != null) {
						Box(
							modifier =
								Modifier
									.align(Alignment.TopStart)
									.padding(5.dp)
									.background(getTypeBadgeColor(item.type), JellyfinTheme.shapes.extraSmall)
									.padding(horizontal = 5.dp, vertical = 2.dp),
						) {
							Text(
								text = badgeLabel,
								style = JellyfinTheme.typography.labelSmall,
								fontWeight = FontWeight.Bold,
								color = JellyfinTheme.colorScheme.onSurface,
							)
						}
					}
				}

				if (item.userData?.isFavorite == true) {
					Icon(
						imageVector = VegafoXIcons.Favorite,
						contentDescription = null,
						tint = VegafoXColors.Error,
						modifier =
							Modifier
								.align(Alignment.TopStart)
								.padding(4.dp)
								.size(20.dp),
					)
				}

				PosterWatchIndicator(
					item = item,
					modifier =
						Modifier
							.align(Alignment.TopEnd)
							.padding(4.dp),
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

			Spacer(modifier = Modifier.height(5.dp))

			if (showLabels) {
				// Title
				Text(
					text = item.name ?: "",
					style = JellyfinTheme.typography.bodyMedium,
					fontWeight = FontWeight.Medium,
					color = JellyfinTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)

				// Metadata: year, officialRating, runtime, ★ communityRating
				val context = androidx.compose.ui.platform.LocalContext.current
				val meta = buildMetadataString(item, context)
				if (meta.isNotEmpty()) {
					Text(
						text = meta,
						style = JellyfinTheme.typography.bodySmall,
						color = JellyfinTheme.colorScheme.textSecondary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				}
			}
		}
	}
}

/**
 * A compact icon-only button for the library toolbar.
 * VegafoX style: transparent, scale 1.08 spring on focus, orange glow.
 */
@Composable
fun LibraryToolbarButton(
	icon: ImageVector,
	contentDescription: String,
	isActive: Boolean = false,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var isFocused by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.08f else 1f,
		animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
		label = "toolbarBtnScale",
	)

	val glowAlpha by animateFloatAsState(
		targetValue = if (isFocused) 1f else 0f,
		animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
		label = "toolbarBtnGlow",
	)

	val tintColor =
		when {
			isFocused -> VegafoXColors.TextPrimary
			isActive -> VegafoXColors.OrangePrimary
			else -> VegafoXColors.TextSecondary
		}

	val bgColor =
		when {
			isFocused -> VegafoXColors.Divider
			else -> Color.Transparent
		}

	Box(
		modifier =
			modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.size(44.dp)
				.drawBehind {
					if (glowAlpha > 0f) {
						drawRoundRect(
							brush =
								Brush.radialGradient(
									colors =
										listOf(
											VegafoXColors.OrangePrimary.copy(alpha = 0.20f * glowAlpha),
											Color.Transparent,
										),
									radius = size.maxDimension * 0.9f,
								),
							cornerRadius = CornerRadius(10.dp.toPx()),
						)
					}
				}.clip(RoundedCornerShape(10.dp))
				.background(bgColor)
				.onFocusChanged { isFocused = it.isFocused }
				.focusable()
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onClick,
				).padding(10.dp),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			modifier = Modifier.size(24.dp),
			tint = tintColor,
		)
	}
}

/**
 * Inline A-Z letter picker.
 */
@Composable
fun AlphaPickerBar(
	selectedLetter: String?,
	onLetterSelected: (String?) -> Unit,
	modifier: Modifier = Modifier,
) {
	val letters = listOf("#") + ('A'..'Z').map { it.toString() }

	LazyRow(
		modifier =
			modifier
				.background(VegafoXColors.Divider, RoundedCornerShape(6.dp)),
		horizontalArrangement = Arrangement.spacedBy(0.dp),
		contentPadding = PaddingValues(horizontal = 2.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		items(letters, key = { it }) { letter ->
			val isSelected =
				when {
					letter == "#" && selectedLetter == null -> true
					letter == selectedLetter -> true
					else -> false
				}

			AlphaPickerLetter(
				letter = letter,
				isSelected = isSelected,
				onClick = {
					if (letter == "#") {
						onLetterSelected(null)
					} else {
						onLetterSelected(letter)
					}
				},
			)
		}
	}
}

@Composable
private fun AlphaPickerLetter(
	letter: String,
	isSelected: Boolean,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val textColor =
		when {
			isSelected -> VegafoXColors.OrangePrimary
			isFocused -> VegafoXColors.OrangePrimary
			else -> VegafoXColors.TextSecondary
		}

	val letterScale by animateFloatAsState(
		targetValue = if (isFocused) 1.15f else 1f,
		animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
		label = "alphaLetterScale",
	)

	Box(
		modifier =
			Modifier
				.size(width = 26.dp, height = 28.dp)
				.graphicsLayer {
					scaleX = letterScale
					scaleY = letterScale
				}.then(
					if (isFocused) {
						Modifier
							.background(VegafoXColors.OrangeSoft, RoundedCornerShape(4.dp))
					} else {
						Modifier
					},
				).clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = letter,
			style = JellyfinTheme.typography.bodyMedium.copy(fontSize = 13.sp),
			fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
			color = textColor,
		)
	}
}

/**
 * Focused item info HUD — shows title and metadata for the currently focused poster.
 * Title auto-scrolls (marquee) if too long.
 */
@Composable
fun FocusedItemHud(
	item: BaseItemDto?,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier =
			modifier
				.defaultMinSize(minHeight = 48.dp)
				.background(VegafoXColors.BackgroundDeep.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
				.padding(horizontal = 12.dp, vertical = 6.dp),
		verticalArrangement = Arrangement.Center,
	) {
		if (item != null) {
			Box(
				modifier =
					Modifier
						.basicMarquee(
							iterations = Int.MAX_VALUE,
							initialDelayMillis = 1200,
						),
			) {
				Text(
					text = item.name ?: "",
					style =
						JellyfinTheme.typography.titleMedium.copy(
							fontSize = 16.sp,
							fontWeight = FontWeight.Bold,
						),
					color = VegafoXColors.TextPrimary,
					maxLines = 1,
					overflow = TextOverflow.Clip,
				)
			}

			// Metadata + compact ratings on the same row
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				val metadataItems =
					buildList<@Composable () -> Unit> {
						item.productionYear?.let { add { InfoItemText(text = it.toString()) } }

						if (item.type != BaseItemKind.SERIES) {
							item.runTimeTicks?.let { add { RuntimeInfo(it) } }
						}

						if (item.type == BaseItemKind.SERIES) {
							val status = item.status?.lowercase()
							if (status == "continuing" || status == "ended") {
								val labelRes = if (status == "continuing") R.string.lbl__continuing_title else R.string.lbl_ended_title
								val color = if (status == "continuing") InfoRowColors.Green.first else InfoRowColors.Red.first
								add {
									InfoItemBadge(
										text = stringResource(labelRes),
										bgColor = color,
										color = JellyfinTheme.colorScheme.onSurface,
									)
								}
							}
						}

						item.officialRating?.let { add { InfoItemBadge(text = it) } }
					}

				metadataItems.forEachIndexed { index, content ->
					content()
					if (index < metadataItems.size - 1) {
						InfoItemSeparator()
					}
				}

				InfoRowCompactRatings(
					item = item,
					leadingContent = {
						if (metadataItems.isNotEmpty()) InfoItemSeparator()
					},
				)
			}
		}
	}
}

/**
 * Library info bar showing filter/sort status and item counter.
 */
@Composable
fun LibraryStatusBar(
	statusText: String,
	counterText: String,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.padding(horizontal = BrowseDimensions.contentPaddingHorizontal, vertical = 4.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = statusText,
			style = JellyfinTheme.typography.bodySmall,
			color = JellyfinTheme.colorScheme.textHint,
		)
		Text(
			text = counterText,
			style = JellyfinTheme.typography.bodyMedium,
			color = JellyfinTheme.colorScheme.textSecondary,
		)
	}
}

/**
 * Glass-morphism filter/sort dialog matching TrackSelectorDialog style.
 * Shows sort options  and played/unplayed as radio-selectable rows, plus toggle row for favorites.
 */
@Composable
fun FilterSortDialog(
	title: String,
	sortOptions: List<SortOption>,
	currentSort: SortOption,
	filterFavorites: Boolean,
	filterPlayedStatus: PlayedStatusFilter,
	filterSeriesStatus: SeriesStatusFilter,
	showPlayedStatus: Boolean,
	showSeriesStatus: Boolean,
	onSortSelected: (SortOption) -> Unit,
	onToggleFavorites: () -> Unit,
	onPlayedStatusSelected: (PlayedStatusFilter) -> Unit,
	onSeriesStatusSelected: (SeriesStatusFilter) -> Unit,
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
						.background(VegafoXColors.Surface)
						.border(1.dp, VegafoXColors.Outline, JellyfinTheme.shapes.dialog)
						.padding(vertical = 20.dp),
			) {
				// Title
				Text(
					text = title,
					style =
						JellyfinTheme.typography.titleMedium.copy(
							fontSize = 18.sp,
							fontWeight = FontWeight.Bold,
						),
					color = VegafoXColors.TextPrimary,
					modifier =
						Modifier
							.padding(horizontal = 24.dp)
							.padding(bottom = 12.dp),
				)

				// Divider
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(4.dp))

				LazyColumn(
					modifier = Modifier.weight(1f, fill = false),
					contentPadding = PaddingValues(bottom = 8.dp),
				) {
					// Sort section
					item {
						Text(
							text = stringResource(R.string.lbl_sort_by),
							style = JellyfinTheme.typography.bodyMedium,
							fontWeight = FontWeight.W500,
							color = VegafoXColors.TextSecondary,
							modifier =
								Modifier
									.padding(horizontal = 24.dp, vertical = 8.dp),
						)
					}

					itemsIndexed(sortOptions, key = { _, option -> option.nameRes }) { index, option ->
						val isSelected = option.sortBy == currentSort.sortBy

						val focusModifier =
							if (index ==
								sortOptions
									.indexOfFirst { it.sortBy == currentSort.sortBy }
									.coerceIn(0, sortOptions.lastIndex)
							) {
								Modifier.focusRequester(initialFocusRequester)
							} else {
								Modifier
							}

						FilterRadioRow(
							label = stringResource(option.nameRes),
							isSelected = isSelected,
							onClick = { onSortSelected(option) },
							modifier = focusModifier,
						)
					}

					// Divider after sort
					item {
						Box(
							modifier =
								Modifier
									.fillMaxWidth()
									.height(1.dp)
									.padding(horizontal = 24.dp)
									.background(JellyfinTheme.colorScheme.divider),
						)
						Spacer(modifier = Modifier.height(4.dp))
					}

					// Filters header + favorites toggle
					item {
						Text(
							text = stringResource(R.string.filters),
							style = JellyfinTheme.typography.bodyMedium,
							fontWeight = FontWeight.W500,
							color = VegafoXColors.TextSecondary,
							modifier =
								Modifier
									.padding(horizontal = 24.dp, vertical = 8.dp),
						)

						FilterToggleRow(
							label = stringResource(R.string.lbl_favorites),
							isActive = filterFavorites,
							onClick = onToggleFavorites,
						)

						Spacer(modifier = Modifier.height(8.dp))
					}

					// Played status section
					if (showPlayedStatus) {
						items(PlayedStatusFilter.entries, key = { it.name }) { filter ->
							val isSelected = filter == filterPlayedStatus

							FilterRadioRow(
								label = stringResource(filter.labelRes),
								isSelected = isSelected,
								onClick = { onPlayedStatusSelected(filter) },
							)
						}

						// Divider only if this section is shown
						item {
							Box(
								modifier =
									Modifier
										.fillMaxWidth()
										.height(1.dp)
										.padding(horizontal = 24.dp)
										.background(JellyfinTheme.colorScheme.divider),
							)
							Spacer(modifier = Modifier.height(4.dp))
						}
					}

					// Series status section
					if (showSeriesStatus) {
						item {
							Text(
								text = stringResource(R.string.lbl_status_title),
								style = JellyfinTheme.typography.bodyMedium,
								fontWeight = FontWeight.W500,
								color = VegafoXColors.TextSecondary,
								modifier =
									Modifier
										.padding(horizontal = 24.dp, vertical = 8.dp),
							)
						}

						items(SeriesStatusFilter.entries, key = { it.name }) { filter ->
							val isSelected = filter == filterSeriesStatus

							FilterRadioRow(
								label = stringResource(filter.labelRes),
								isSelected = isSelected,
								onClick = { onSeriesStatusSelected(filter) },
							)
						}
					}
				}

				// Action buttons
				Spacer(modifier = Modifier.height(12.dp))
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(VegafoXColors.Divider),
				)
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
				) {
					VegafoXButton(
						text = stringResource(R.string.lbl_cancel),
						onClick = onDismiss,
						variant = VegafoXButtonVariant.Ghost,
						compact = true,
					)
					VegafoXButton(
						text = stringResource(R.string.lbl_ok),
						onClick = onDismiss,
						variant = VegafoXButtonVariant.Primary,
						compact = true,
					)
				}
			}
		}

		LaunchedEffect(Unit) {
			initialFocusRequester.requestFocus()
		}
	}
}

/**
 * A radio-selectable row inside the filter dialog.
 */
@Composable
private fun FilterRadioRow(
	label: String,
	isSelected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Row(
		modifier =
			modifier
				.fillMaxWidth()
				.clickable(
					interactionSource = interactionSource,
					indication = null,
				) { onClick() }
				.focusable(interactionSource = interactionSource)
				.background(
					if (isFocused) JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent,
				).padding(horizontal = 24.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Radio circle
		Box(
			modifier =
				Modifier
					.size(18.dp)
					.border(
						width = 2.dp,
						color = if (isSelected) VegafoXColors.OrangePrimary else VegafoXColors.TextHint,
						shape = CircleShape,
					),
			contentAlignment = Alignment.Center,
		) {
			if (isSelected) {
				Box(
					modifier =
						Modifier
							.size(10.dp)
							.background(VegafoXColors.OrangePrimary, CircleShape),
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))

		Text(
			text = label,
			style = JellyfinTheme.typography.bodyLarge,
			fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400,
			color =
				when {
					isSelected -> VegafoXColors.OrangePrimary
					isFocused -> VegafoXColors.TextPrimary
					else -> VegafoXColors.TextPrimary
				},
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)
	}
}

/**
 * A toggle row inside the filter dialog — checkbox-like (filled/empty circle).
 */
@Composable
private fun FilterToggleRow(
	label: String,
	isActive: Boolean,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.clickable(
					interactionSource = interactionSource,
					indication = null,
				) { onClick() }
				.focusable(interactionSource = interactionSource)
				.background(
					if (isFocused) JellyfinTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent,
				).padding(horizontal = 24.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Checkbox-like circle
		Box(
			modifier =
				Modifier
					.size(18.dp)
					.then(
						if (isActive) {
							Modifier
								.background(VegafoXColors.OrangePrimary, JellyfinTheme.shapes.extraSmall)
						} else {
							Modifier
								.border(2.dp, VegafoXColors.TextHint, JellyfinTheme.shapes.extraSmall)
						},
					),
			contentAlignment = Alignment.Center,
		) {
			if (isActive) {
				Text(
					text = "✓",
					style = JellyfinTheme.typography.labelMedium,
					fontWeight = FontWeight.Bold,
					color = VegafoXColors.Background,
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))

		Text(
			text = label,
			style = JellyfinTheme.typography.bodyLarge,
			fontWeight = if (isActive) FontWeight.W600 else FontWeight.W400,
			color =
				when {
					isActive -> VegafoXColors.OrangePrimary
					isFocused -> VegafoXColors.TextPrimary
					else -> VegafoXColors.TextPrimary
				},
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)
	}
}

/**
 * Watched/unplayed indicator for library poster cards
 */
@Composable
private fun PosterWatchIndicator(
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
			modifier = modifier.size(22.dp),
		) {
			Icon(
				imageVector = VegafoXIcons.Visibility,
				contentDescription = null,
				modifier = Modifier.size(12.dp),
			)
		}
	} else if (unplayedItems != null) {
		if (watchedIndicatorBehavior == WatchedIndicatorBehavior.HIDE_UNWATCHED) return

		Badge(
			modifier = modifier.sizeIn(minWidth = 22.dp, minHeight = 22.dp),
		) {
			Text(
				text = unplayedItems.toString(),
			)
		}
	}
}
