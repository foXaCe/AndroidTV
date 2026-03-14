package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaActionChip
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaGenreTag
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaPosterColumn
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailEpisodesHorizontalSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

@Composable
fun SeasonDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	showBackdrop: Boolean,
	api: ApiClient,
	blurAmount: Int,
	onNavigateToItem: (UUID) -> Unit,
	onPlayEpisode: (BaseItemDto) -> Unit,
	onToggleWatched: () -> Unit,
	onToggleFavorite: () -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val titleFocusRequester = contentFocusRequester
	val density = LocalDensity.current

	val posterUrl = getPosterUrl(item, api)

	// Enter animation
	var showContent by remember { mutableStateOf(false) }
	LaunchedEffect(item.id) {
		kotlinx.coroutines.delay(100)
		showContent = true
		kotlinx.coroutines.delay(500)
		try {
			titleFocusRequester.requestFocus()
		} catch (_: Exception) {
		}
	}

	val slideOffsetPx = with(density) { 20.dp.roundToPx() }

	Box(modifier = Modifier.fillMaxSize()) {
		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize(),
		) {
			// ═══ HERO ZONE ═══
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(HeroDimensions.backdropHeight),
					contentAlignment = Alignment.CenterStart,
				) {
					AnimatedVisibility(
						visible = showContent,
						enter =
							fadeIn(
								animationSpec = tween(350, easing = EaseOutCubic),
							) +
								slideInVertically(
									initialOffsetY = { slideOffsetPx },
									animationSpec = tween(350, easing = EaseOutCubic),
								),
					) {
						Row(
							modifier =
								Modifier
									.fillMaxSize()
									.padding(horizontal = HeroDimensions.horizontalPadding),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(48.dp),
						) {
							// ─── Left column ───
							Column(
								modifier = Modifier.weight(1f),
							) {
								Box(
									modifier =
										Modifier
											.focusRequester(titleFocusRequester)
											.focusable(),
								) {
									Column {
										// Genre tag: series name
										val seriesName = item.seriesName ?: ""
										CinemaGenreTag(text = seriesName)

										Spacer(modifier = Modifier.height(12.dp))

										// Season title
										Text(
											text = item.name ?: "",
											style =
												JellyfinTheme.typography.displayBold.copy(
													fontSize = HeroDimensions.titleFontSize,
													fontWeight = FontWeight.Black,
													fontFamily = BebasNeue,
													letterSpacing = 2.sp,
												),
											color = VegafoXColors.TextPrimary,
											maxLines = 2,
											overflow = TextOverflow.Ellipsis,
											lineHeight = HeroDimensions.titleLineHeight,
										)
									}
								}

								Spacer(modifier = Modifier.height(16.dp))

								// Episode count
								Text(
									text =
										stringResource(
											if (uiState.episodes.size == 1) {
												R.string.lbl_episode_count_singular
											} else {
												R.string.lbl_episodes_count
											},
											uiState.episodes.size,
										),
									style =
										JellyfinTheme.typography.bodyMedium.copy(
											fontSize = 15.sp,
										),
									color = VegafoXColors.TextSecondary,
								)

								Spacer(modifier = Modifier.height(32.dp))

								// ─── Primary action buttons ───
								Row(
									horizontalArrangement = Arrangement.spacedBy(12.dp),
									modifier = Modifier.focusGroup(),
								) {
									if (uiState.episodes.isNotEmpty()) {
										VegafoXButton(
											text = stringResource(R.string.lbl_play),
											onClick = {
												val unwatched = uiState.episodes.firstOrNull { !(it.userData?.played ?: false) }
												val episode = unwatched ?: uiState.episodes.first()
												onPlayEpisode(episode)
											},
											variant = VegafoXButtonVariant.Primary,
											icon = VegafoXIcons.Play,
											iconEnd = false,
										)
									}
								}

								Spacer(modifier = Modifier.height(12.dp))

								// ─── Secondary action chips ───
								Row(
									horizontalArrangement = Arrangement.spacedBy(10.dp),
									modifier = Modifier.focusGroup(),
								) {
									CinemaActionChip(
										icon = VegafoXIcons.Visibility,
										label =
											if (item.userData?.played == true) {
												stringResource(R.string.lbl_watched)
											} else {
												stringResource(R.string.lbl_unwatched)
											},
										onClick = onToggleWatched,
										isActive = item.userData?.played == true,
										activeColor = VegafoXColors.Info,
									)

									CinemaActionChip(
										icon = VegafoXIcons.Favorite,
										label =
											if (item.userData?.isFavorite == true) {
												stringResource(R.string.lbl_favorited)
											} else {
												stringResource(R.string.lbl_favorite)
											},
										onClick = onToggleFavorite,
										isActive = item.userData?.isFavorite == true,
										activeColor = VegafoXColors.Error,
									)
								}
							}

							// ─── Right column: Poster ───
							if (posterUrl != null) {
								CinemaPosterColumn(
									posterUrl = posterUrl,
									playedPercentage = 0.0,
									watchedMinutes = 0,
								)
							}
						}
					}
				}
			}

			// ═══ CONTENT BELOW HERO ═══

			// Gradient transition
			item {
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(40.dp)
							.background(
								Brush.verticalGradient(
									colors = listOf(Color.Transparent, VegafoXColors.BackgroundDeep),
								),
							),
				)
			}

			// Episodes section
			if (uiState.episodes.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = 48.dp)
								.padding(top = 16.dp),
					) {
						DetailEpisodesHorizontalSection(
							title =
								item.name?.let {
									"${it.uppercase()} \u2014 ${stringResource(R.string.lbl_episodes).uppercase()}"
								} ?: stringResource(R.string.lbl_episodes).uppercase(),
							episodes = uiState.episodes,
							currentEpisodeId = UUID(0, 0), // no current episode for season view
							api = api,
							onNavigateToItem = onNavigateToItem,
						)
					}
				}
			}

			// Bottom padding
			item {
				Spacer(
					modifier =
						Modifier
							.height(80.dp)
							.fillMaxWidth()
							.background(VegafoXColors.BackgroundDeep),
				)
			}
		}
	}
}
