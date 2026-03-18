package org.jellyfin.androidtv.ui.home.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonLandscapeCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.tv.TvRowList
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.shared.components.DarkGridNoiseBackground
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.androidtv.util.apiclient.getCardImageUrl
import org.jellyfin.design.Tokens
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber

/** Pixel size used for prefetching card images (matches BrowseMediaCard). */
private const val PREFETCH_IMAGE_WIDTH = 440
private const val PREFETCH_IMAGE_HEIGHT = 248

/**
 * Home screen — the root browsing experience (Netflix-style).
 *
 * Architecture:
 * ```
 * Row (fullscreen) {
 *     PremiumSideBar()               // fixed 72dp, no overlay
 *     Box (weight=1f) {
 *         DarkGridNoiseBackground()  // dark grid noise (trading terminal style)
 *         HomeHeroBackdrop(item)     // crossfade backdrop + dynamic gradient
 *         Column {
 *             Box(weight=0.56) {     // hero info overlay area
 *                 HeroInfoOverlay()  // animated metadata (no buttons)
 *             }
 *             TvRowList(rows)        // vertical list of horizontal rows
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun HomeScreen(
	viewModel: HomeViewModel,
	onItemClick: (BaseItemDto) -> Unit,
	onPlayClick: (BaseItemDto) -> Unit = onItemClick,
) {
	val uiState by viewModel.uiState.collectAsState()
	val focusedItem by viewModel.focusedItem.collectAsState()
	val trailerState by viewModel.trailerState.collectAsState()
	val progressMap by viewModel.progressMap.collectAsState()

	// Focus management: initial + restore on return from detail
	val focusRequester = remember { FocusRequester() }
	var hasRequestedInitialFocus by rememberSaveable { mutableStateOf(false) }
	val lastFocusedId by viewModel.lastFocusedItemId.collectAsState()
	val lastFocusedRowIndex by viewModel.lastFocusedRowIndex.collectAsState()
	val columnState = rememberLazyListState()

	// Determine focus target: last focused item (return from detail) or default first item
	val defaultRow =
		uiState.rows.firstOrNull { it.key == "continue_watching" || it.key == "continue_watching_fallback" }
			?: uiState.rows.firstOrNull()
	val defaultItemId = defaultRow?.items?.firstOrNull()?.id
	val focusTargetId = lastFocusedId ?: defaultItemId

	// Initial focus on first load
	LaunchedEffect(focusTargetId) {
		if (focusTargetId != null && !hasRequestedInitialFocus) {
			delay(400)
			try {
				focusRequester.requestFocus()
				hasRequestedInitialFocus = true
			} catch (_: Exception) {
				// FocusRequester not yet attached
			}
		}
	}

	// Restore focus when returning from detail screen + stop trailer on pause/stop
	var needsRestoreFocus by remember { mutableStateOf(false) }
	val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
	androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
		val observer =
			androidx.lifecycle.LifecycleEventObserver { _, event ->
				when (event) {
					androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
						viewModel.unfreezeFocusTracking()
						if (hasRequestedInitialFocus) {
							needsRestoreFocus = true
						}
					}
					androidx.lifecycle.Lifecycle.Event.ON_PAUSE,
					androidx.lifecycle.Lifecycle.Event.ON_STOP,
					-> {
						// Freeze focus tracking to prevent corruption during fragment transition
						viewModel.freezeFocusTracking()
						// Save exact scroll position before leaving
						viewModel.saveScrollPosition(
							columnState.firstVisibleItemIndex,
							columnState.firstVisibleItemScrollOffset,
						)
						viewModel.stopTrailer()
					}
					else -> Unit
				}
			}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}

	LaunchedEffect(needsRestoreFocus) {
		if (needsRestoreFocus && focusTargetId != null) {
			val savedIdx = viewModel.savedScrollIndex
			val savedOff = viewModel.savedScrollOffset

			// Step 1: Wait for LazyColumn to complete its initial layout
			snapshotFlow { columnState.layoutInfo.visibleItemsInfo.isNotEmpty() }
				.first { it }

			// Step 2: Restore exact scroll position saved before navigation
			columnState.scrollToItem(savedIdx, savedOff)
			delay(50)

			// Step 3: If target row is not visible (deep rows), scroll to it
			val targetVisible =
				columnState.layoutInfo.visibleItemsInfo
					.any { it.index == lastFocusedRowIndex }
			if (!targetVisible) {
				columnState.scrollToItem(lastFocusedRowIndex)
				delay(50)
			}

			// Step 4: Request focus on the target card
			try {
				focusRequester.requestFocus()
			} catch (e: Exception) {
				Timber.w(e, "Focus restore failed for item $focusTargetId at row $lastFocusedRowIndex")
			}

			// Step 5: Re-apply saved scroll to counteract BringIntoView adjustment
			// Only safe when saved position already shows the target row
			if (targetVisible) {
				delay(50)
				columnState.scrollToItem(savedIdx, savedOff)
			}

			needsRestoreFocus = false
		}
	}

	VegafoXScaffold {
		Box(modifier = Modifier.fillMaxSize()) {
			// Layer 0: Dark grid noise background (trading terminal style)
			DarkGridNoiseBackground()

			// Layer 1: Hero backdrop — full-screen image + trailer video of the focused item
			HomeHeroBackdrop(
				item = focusedItem,
				api = viewModel.api,
				trailerState = trailerState,
				onTrailerEnded = { viewModel.stopTrailer() },
				onStopTrailer = { viewModel.stopTrailer() },
			)

			// Prefetch hero backdrop for focused item + 2 adjacent items
			val prefetchContext = LocalContext.current
			val heroImageLoader = remember { SingletonImageLoader.get(prefetchContext) }
			LaunchedEffect(focusedItem?.id) {
				val item = focusedItem ?: return@LaunchedEffect
				val rows = uiState.rows
				// Find the item's row and index
				for (row in rows) {
					val idx = row.items.indexOfFirst { it.id == item.id }
					if (idx < 0) continue
					// Prefetch current + previous + next backdrop
					val indices = listOf(idx, idx - 1, idx + 1)
					for (i in indices) {
						val adjacentItem = row.items.getOrNull(i) ?: continue
						val url = getBackdropUrl(adjacentItem, viewModel.api) ?: continue
						heroImageLoader.enqueue(
							ImageRequest
								.Builder(prefetchContext)
								.data(url)
								.memoryCachePolicy(CachePolicy.ENABLED)
								.diskCachePolicy(CachePolicy.ENABLED)
								.crossfade(false)
								.build(),
						)
					}
					break
				}
			}

			// Layer 2: Content
			Column(modifier = Modifier.fillMaxSize()) {
				// Hero info area (takes flexible space above rows)
				Box(
					modifier =
						Modifier
							.weight(0.46f)
							.fillMaxWidth(),
				) {
					// Info overlay — center-left
					HeroInfoOverlay(
						item = focusedItem,
						modifier =
							Modifier
								.align(Alignment.CenterStart)
								.padding(start = 16.dp),
					)
				}

				// Rows content with state handling
				val displayState =
					when {
						uiState.isLoading -> DisplayState.LOADING
						uiState.error != null -> DisplayState.ERROR
						uiState.rows.isEmpty() -> DisplayState.EMPTY
						else -> DisplayState.CONTENT
					}

				StateContainer(
					state = displayState,
					modifier = Modifier.weight(0.54f).clipToBounds(),
					loadingContent = {
						// Matches TvRowList contentPadding: start=12dp, top=spaceSm
						Column(modifier = Modifier.padding(start = 12.dp, top = Tokens.Space.spaceSm)) {
							repeat(3) {
								SkeletonLandscapeCardRow(showCardTextLines = false)
							}
						}
					},
					emptyContent = {
						EmptyState(title = stringResource(R.string.lbl_empty))
					},
					errorContent = {
						ErrorState(
							message =
								stringResource(
									uiState.error?.messageRes ?: R.string.state_error_generic,
								),
							onRetry = { viewModel.loadRows() },
						)
					},
					content = {
						val context = LocalContext.current
						val imageLoader = remember { SingletonImageLoader.get(context) }
						val api = viewModel.api
						val prefetchCallback =
							remember<(List<BaseItemDto>) -> Unit>(api) {
								{ items ->
									for (nextItem in items.take(5)) {
										val url = nextItem.getCardImageUrl(api) ?: continue
										val request =
											ImageRequest
												.Builder(context)
												.data(url)
												.size(coil3.size.Size(PREFETCH_IMAGE_WIDTH, PREFETCH_IMAGE_HEIGHT))
												.scale(Scale.FILL)
												.memoryCachePolicy(CachePolicy.ENABLED)
												.diskCachePolicy(CachePolicy.ENABLED)
												.crossfade(false)
												.build()
										imageLoader.enqueue(request)
									}
								}
							}

						// Pre-compute item → row index map for focus tracking
						val itemRowIndex =
							remember(uiState.rows) {
								buildMap {
									uiState.rows.forEachIndexed { rowIdx, row ->
										for (item in row.items) {
											put(item.id, rowIdx)
										}
									}
								}
							}

						TvRowList(
							rows = uiState.rows,
							columnState = columnState,
							staggerEntrance = true,
							contentPadding =
								PaddingValues(
									start = 12.dp,
									top = Tokens.Space.spaceSm,
									bottom = Tokens.Space.spaceLg,
								),
							prefetchContent = prefetchCallback,
							itemKey = { it.id },
						) { item ->
							BrowseMediaCard(
								item = item,
								api = viewModel.api,
								onFocus = {
									viewModel.setFocusedItem(item, itemRowIndex[item.id] ?: 0)
								},
								onBlur = { viewModel.stopTrailer() },
								onClick = { onItemClick(item) },
								onPlayClick = { onPlayClick(item) },
								hasProgress = progressMap[item.id] ?: false,
								initialFocusRequester =
									if (item.id == focusTargetId) {
										focusRequester
									} else {
										null
									},
							)
						}
					},
				)
			}
		}
	}
}
