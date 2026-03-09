package org.jellyfin.androidtv.ui.itemdetail.v2.content

import android.view.KeyEvent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.rememberErrorPlaceholder
import org.jellyfin.androidtv.ui.composable.rememberGradientPlaceholder
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getPosterUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

@Composable
fun PersonDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	showBackdrop: Boolean,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val listState = rememberLazyListState()
	val titleFocusRequester = contentFocusRequester
	val filmographyFocusRequester = remember { FocusRequester() }

	val personMovies = uiState.similar.filter { it.type == BaseItemKind.MOVIE }
	val personSeries = uiState.similar.filter { it.type == BaseItemKind.SERIES }

	val backdropUrls = remember(uiState.similar) {
		uiState.similar
			.mapNotNull { filmItem -> getBackdropUrl(filmItem, api) }
			.distinct()
			.take(10)
	}

	var currentBackdropIndex by remember { mutableStateOf(0) }
	var focusedBackdropUrl by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(backdropUrls) {
		if (backdropUrls.size > 1) {
			while (true) {
				delay(8000)
				if (focusedBackdropUrl == null) {
					currentBackdropIndex = (currentBackdropIndex + 1) % backdropUrls.size
				}
			}
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		if (showBackdrop && backdropUrls.isNotEmpty()) {
			val displayUrl = focusedBackdropUrl ?: backdropUrls.getOrNull(currentBackdropIndex)
			Crossfade(
				targetState = displayUrl,
				animationSpec = tween(1000),
				label = "person_backdrop_slideshow"
			) { backdropUrl ->
				if (backdropUrl != null) {
					AsyncImage(
						model = backdropUrl,
						contentDescription = null,
						modifier = Modifier
							.fillMaxSize()
							.graphicsLayer { alpha = 0.6f },
						contentScale = ContentScale.Crop,
					)
					Box(
						modifier = Modifier
							.fillMaxSize()
							.background(
								brush = Brush.verticalGradient(
									colors = listOf(
										Color.Black.copy(alpha = 0.3f),
										Color.Black.copy(alpha = 0.6f),
									),
								)
							)
					)
				}
			}
		} else if (showBackdrop) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						brush = Brush.linearGradient(
							colors = listOf(
								JellyfinTheme.colorScheme.gradientEnd,
								JellyfinTheme.colorScheme.gradientMid,
								JellyfinTheme.colorScheme.gradientStart,
							),
						)
					)
			)
		}

		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(top = 100.dp, start = 48.dp, end = 48.dp, bottom = 80.dp),
		) {
			item {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.focusRequester(titleFocusRequester)
						.focusable()
						.onKeyEvent { keyEvent ->
							if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
								when (keyEvent.key) {
									Key.DirectionDown -> {
										try {
											filmographyFocusRequester.requestFocus()
										} catch (_: Exception) {
										}
										true
									}
									else -> false
								}
							} else false
						},
				) {
					Row(modifier = Modifier.padding(bottom = 24.dp)) {
						Box(modifier = Modifier.width(160.dp)) {
							val personPhotoUrl = getPosterUrl(item, api)
							if (personPhotoUrl != null) {
								val placeholder = rememberGradientPlaceholder()
								val errorFallback = rememberErrorPlaceholder()
								AsyncImage(
									model = personPhotoUrl,
									contentDescription = item.name,
									modifier = Modifier
										.fillMaxWidth()
										.height(240.dp)
										.background(
											JellyfinTheme.colorScheme.outlineVariant,
											JellyfinTheme.shapes.medium,
										),
									contentScale = ContentScale.Crop,
									placeholder = placeholder,
									error = errorFallback,
								)
							} else {
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.height(240.dp)
										.background(
											JellyfinTheme.colorScheme.outlineVariant,
											JellyfinTheme.shapes.medium,
										),
									contentAlignment = Alignment.Center,
								) {
									Text(
										text = item.name?.firstOrNull()?.toString() ?: "",
										style = JellyfinTheme.typography.display,
										color = JellyfinTheme.colorScheme.textDisabled,
									)
								}
							}
						}

						Spacer(modifier = Modifier.width(32.dp))

						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = item.name ?: "",
								style = JellyfinTheme.typography.headlineLargeBold,
								color = JellyfinTheme.colorScheme.onSurface,
								lineHeight = 40.sp,
							)

							Spacer(modifier = Modifier.height(8.dp))

							item.premiereDate?.let { birthDate ->
								val age = java.time.temporal.ChronoUnit.YEARS.between(
									birthDate,
									item.endDate ?: java.time.LocalDateTime.now(),
								)
								val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")
								Text(
									text = stringResource(R.string.lbl_born_date, birthDate.toLocalDate().format(formatter), age),
									style = JellyfinTheme.typography.titleLarge,
									color = JellyfinTheme.colorScheme.textSecondary,
								)
								Spacer(modifier = Modifier.height(4.dp))
							}

							item.productionLocations?.firstOrNull()?.let { birthPlace ->
								Text(
									text = birthPlace,
									style = JellyfinTheme.typography.titleLarge,
									color = JellyfinTheme.colorScheme.textSecondary,
								)
								Spacer(modifier = Modifier.height(8.dp))
							}

							item.overview?.let { overview ->
								Text(
									text = overview,
									style = JellyfinTheme.typography.titleLarge,
									color = JellyfinTheme.colorScheme.textPrimary,
									lineHeight = 26.sp,
									maxLines = 4,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}
					}
				}
			}

			if (personMovies.isNotEmpty()) {
				item {
					DetailSectionWithCards(
						title = stringResource(R.string.lbl_movies_count, personMovies.size),
						items = personMovies,
						api = api,
						onNavigateToItem = onNavigateToItem,
						firstItemFocusRequester = filmographyFocusRequester,
						onItemFocused = { focusItem -> focusedBackdropUrl = getBackdropUrl(focusItem, api) },
					)
				}
			}
			if (personSeries.isNotEmpty()) {
				item {
					Spacer(modifier = Modifier.height(24.dp))
					DetailSectionWithCards(
						title = stringResource(R.string.lbl_series_count, personSeries.size),
						items = personSeries,
						api = api,
						onNavigateToItem = onNavigateToItem,
						onItemFocused = { focusItem -> focusedBackdropUrl = getBackdropUrl(focusItem, api) },
						firstItemFocusRequester = if (personMovies.isEmpty()) filmographyFocusRequester else null,
					)
				}
			}
		}
	}

	LaunchedEffect(Unit) {
		for (attempt in 1..5) {
			delay(if (attempt == 1) 300L else 200L)
			try {
				titleFocusRequester.requestFocus()
				break
			} catch (_: Exception) {
			}
		}
	}
}
