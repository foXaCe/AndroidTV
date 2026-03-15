package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.HeroDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaGenreTag
import org.jellyfin.androidtv.ui.itemdetail.v2.CinemaPosterColumn
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionButtonsRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSeasonsSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.androidtv.ui.shared.components.MediaMetadataBadges
import org.jellyfin.sdk.api.client.ApiClient
import java.util.UUID

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Cinema Immersive detail content for Series type.
 */
@Composable
fun SeriesDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val playButtonFocusRequester = remember { FocusRequester() }
	val titleFocusRequester = contentFocusRequester
	val density = LocalDensity.current

	val posterUrl = getPosterUrl(item, api)

	// Genre tag
	val genreTag = buildSeriesGenreTag(item)

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
							horizontalArrangement = Arrangement.spacedBy(DetailDimensions.actionsSpacing),
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
										CinemaGenreTag(text = genreTag)

										Spacer(modifier = Modifier.height(12.dp))

										// Title
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

								// Metadata badges
								MediaMetadataBadges(item = item)

								Spacer(modifier = Modifier.height(16.dp))

								// Synopsis
								item.overview?.let { overview ->
									Text(
										text = overview,
										style =
											JellyfinTheme.typography.bodyMedium.copy(
												fontSize = 15.sp,
											),
										color = VegafoXColors.TextSecondary,
										maxLines = 3,
										overflow = TextOverflow.Ellipsis,
										lineHeight = 26.sp,
									)
								}

								Spacer(modifier = Modifier.height(32.dp))

								// ─── Action buttons (cinema style) ───
								DetailActionButtonsRow(
									item = item,
									uiState = uiState,
									playButtonFocusRequester = playButtonFocusRequester,
									callbacks = actionCallbacks,
								)
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
							.height(DetailDimensions.gradientHeight)
							.background(
								Brush.verticalGradient(
									colors = listOf(Color.Transparent, VegafoXColors.BackgroundDeep),
								),
							),
				)
			}

			// Next Up
			if (uiState.nextUp.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 16.dp),
					) {
						DetailSectionWithCards(
							title = stringResource(R.string.lbl_next_up),
							items = uiState.nextUp,
							api = api,
							onNavigateToItem = onNavigateToItem,
							isLandscape = true,
						)
					}
				}
			}

			// Seasons
			if (uiState.seasons.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 24.dp),
					) {
						DetailSeasonsSection(uiState.seasons, api, onNavigateToItem)
					}
				}
			}

			// Cast & Crew
			if (uiState.cast.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 24.dp),
					) {
						DetailCastSection(uiState.cast, api, onNavigateToItem)
					}
				}
			}

			// More Like This
			if (uiState.similar.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(VegafoXColors.BackgroundDeep)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 24.dp),
					) {
						DetailSectionWithCards(
							title = stringResource(R.string.lbl_more_like_this),
							items = uiState.similar,
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
							.height(DetailDimensions.bottomPadding)
							.fillMaxWidth()
							.background(VegafoXColors.BackgroundDeep),
				)
			}
		}
	}
}

private fun buildSeriesGenreTag(item: org.jellyfin.sdk.model.api.BaseItemDto): String {
	val genre = item.genres?.firstOrNull()
	return listOfNotNull("S\u00C9RIE", genre?.uppercase()).joinToString("  \u2022  ")
}
