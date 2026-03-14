package org.jellyfin.androidtv.ui.home.compose.sidebar

import android.app.Activity
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.SidebarDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

/** Width of the premium sidebar in collapsed state — used by callers to know the reserved space. */
val PREMIUM_SIDEBAR_WIDTH_COLLAPSED = SidebarDimensions.widthCollapsed

/** Width of the premium sidebar in expanded state. */
private val PREMIUM_SIDEBAR_WIDTH_EXPANDED = SidebarDimensions.widthExpanded

@Deprecated("Use PREMIUM_SIDEBAR_WIDTH_COLLAPSED", ReplaceWith("PREMIUM_SIDEBAR_WIDTH_COLLAPSED"))
val PREMIUM_SIDEBAR_WIDTH = PREMIUM_SIDEBAR_WIDTH_COLLAPSED

private val SidebarBackgroundCollapsed = Color(0xFF060A0F).copy(alpha = 0.95f)
private val SidebarBackgroundExpanded = Color(0xFF060A0F)
private val SeparatorColor = VegafoXColors.OrangePrimary.copy(alpha = 0.40f)
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val NAV_ITEM_COUNT = 9
private const val SHIMMER_RESUME_DELAY_MS = 3000L
private const val SHIMMER_CYCLE_MS = 1500
private const val SHIMMER_STAGGER_MS = 300
private const val ENTRANCE_ANIM_MS = 400
private const val DOUBLE_TAP_THRESHOLD_MS = 400L

/**
 * Premium sidebar — fixed left-side navigation for the home screen.
 *
 * Behaviours:
 * - **Expand/collapse** — toggle via double-tap D-pad Left; close via Back or D-pad Right
 * - **Per-item indicator** — orange bar on each selected NavItem, positioned via align(CenterStart)
 * - **Shimmer at rest** — inactive icons pulse opacity 0.40–0.55, staggered 300 ms per item
 * - **Entrance slide** — sidebar slides in from left with fade on first display (400 ms, once)
 */
@Composable
fun PremiumSideBar(modifier: Modifier = Modifier) {
	var selectedIndex by remember { mutableIntStateOf(0) }
	val density = LocalDensity.current
	val context = LocalContext.current
	val activity = context as? Activity
	val sessionRepository = koinInject<SessionRepository>()
	val mediaManager = koinInject<MediaManager>()
	val navigationRepository = koinInject<NavigationRepository>()
	val userViewsRepository = koinInject<UserViewsRepository>()
	val itemLauncher = koinInject<ItemLauncher>()
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()

	// ── User libraries (for Films, Séries, Live TV, Média navigation) ──
	var userViews by remember { mutableStateOf<List<BaseItemDto>>(emptyList()) }
	LaunchedEffect(Unit) {
		userViewsRepository.views.collect { views ->
			userViews = views.toList()
		}
	}

	// ── Focus tracking (for shimmer + expand control) ────────────────────
	var focusCount by remember { mutableIntStateOf(0) }
	val anyItemFocused by remember { derivedStateOf { focusCount > 0 } }

	val onItemFocusChanged: (Boolean) -> Unit =
		remember {
			{ focused: Boolean ->
				focusCount = (focusCount + if (focused) 1 else -1).coerceAtLeast(0)
			}
		}

	// ── Expand / collapse (double-tap D-pad Left to toggle) ───────────
	var isExpanded by remember { mutableStateOf(false) }
	var lastLeftPressTime by remember { mutableLongStateOf(0L) }

	val sidebarWidth by animateDpAsState(
		targetValue = if (isExpanded) PREMIUM_SIDEBAR_WIDTH_EXPANDED else PREMIUM_SIDEBAR_WIDTH_COLLAPSED,
		animationSpec = tween(250, easing = EaseOutCubic),
		label = "sidebarWidth",
	)

	// ── Background alpha ───────────────────────────────────────────────
	val bgAlpha by animateFloatAsState(
		targetValue = if (isExpanded) 1f else 0.95f,
		animationSpec = tween(200),
		label = "bgAlpha",
	)

	// ── Shimmer animation ──────────────────────────────────────────────
	var shimmerActive by remember { mutableStateOf(true) }

	LaunchedEffect(anyItemFocused) {
		if (anyItemFocused) {
			shimmerActive = false
		} else {
			delay(SHIMMER_RESUME_DELAY_MS)
			shimmerActive = true
		}
	}

	val shimmerStrength by animateFloatAsState(
		targetValue = if (shimmerActive) 1f else 0f,
		animationSpec = tween(500),
		label = "shimmerStrength",
	)

	val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
	val shimmerRawAlphas =
		(0 until NAV_ITEM_COUNT).map { index ->
			shimmerTransition.animateFloat(
				initialValue = 0.40f,
				targetValue = 0.55f,
				animationSpec =
					infiniteRepeatable(
						animation = tween(SHIMMER_CYCLE_MS, easing = FastOutSlowInEasing),
						repeatMode = RepeatMode.Reverse,
						initialStartOffset = StartOffset(index * SHIMMER_STAGGER_MS),
					),
				label = "shimmer_$index",
			)
		}

	// ── Entrance animation (once) ──────────────────────────────────────
	var hasAppeared by remember { mutableStateOf(false) }
	LaunchedEffect(Unit) { hasAppeared = true }

	val sidebarTotalWidthPx = with(density) { (PREMIUM_SIDEBAR_WIDTH_COLLAPSED + 1.dp).toPx() }

	val slideProgress by animateFloatAsState(
		targetValue = if (hasAppeared) 1f else 0f,
		animationSpec = tween(ENTRANCE_ANIM_MS),
		label = "sidebarSlide",
	)

	// ── Layout ─────────────────────────────────────────────────────────
	Row(
		modifier =
			modifier
				.fillMaxHeight()
				.onPreviewKeyEvent { event ->
					if (event.type == KeyEventType.KeyDown) {
						when (event.key) {
							Key.DirectionLeft -> {
								val now = System.currentTimeMillis()
								if (now - lastLeftPressTime < DOUBLE_TAP_THRESHOLD_MS) {
									isExpanded = !isExpanded
									lastLeftPressTime = 0L
									true // consume second tap
								} else {
									lastLeftPressTime = now
									false
								}
							}
							Key.Back -> {
								if (isExpanded) {
									isExpanded = false
									true
								} else {
									false
								}
							}
							Key.DirectionRight -> {
								if (isExpanded) {
									isExpanded = false
								}
								false // let focus move right
							}
							else -> false
						}
					} else {
						false
					}
				}.graphicsLayer {
					translationX = -(1f - slideProgress) * sidebarTotalWidthPx
					alpha = slideProgress
				},
	) {
		Box(
			modifier =
				Modifier
					.width(sidebarWidth)
					.fillMaxHeight()
					.then(
						if (isExpanded) Modifier.shadow(elevation = 8.dp) else Modifier,
					).background(Color(0xFF060A0F).copy(alpha = bgAlpha)),
		) {
			// ── Navigation items ───────────────────────────────────────
			Column(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Spacer(modifier = Modifier.height(24.dp))

				// Top group
				NavItem(
					icon = VegafoXIcons.Home,
					label = "Accueil",
					isSelected = selectedIndex == 0,
					onSelect = {
						selectedIndex = 0
						navigationRepository.navigate(Destinations.home)
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[0].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Search,
					label = "Recherche",
					isSelected = selectedIndex == 1,
					onSelect = {
						selectedIndex = 1
						navigationRepository.navigate(Destinations.search())
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[1].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Movie,
					label = "Films",
					isSelected = selectedIndex == 2,
					onSelect = {
						selectedIndex = 2
						val library = userViews.firstOrNull { it.collectionType == CollectionType.MOVIES }
						if (library != null) {
							navigationRepository.navigate(itemLauncher.getUserViewDestination(library))
						}
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[2].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Tv,
					label = "Séries",
					isSelected = selectedIndex == 3,
					onSelect = {
						selectedIndex = 3
						val library = userViews.firstOrNull { it.collectionType == CollectionType.TVSHOWS }
						if (library != null) {
							navigationRepository.navigate(itemLauncher.getUserViewDestination(library))
						}
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[3].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.LiveTv,
					label = "Live TV",
					isSelected = selectedIndex == 4,
					onSelect = {
						selectedIndex = 4
						val library = userViews.firstOrNull { it.collectionType == CollectionType.LIVETV }
						if (library != null) {
							navigationRepository.navigate(itemLauncher.getUserViewDestination(library))
						}
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[4].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.VideoLibrary,
					label = "Média",
					isSelected = selectedIndex == 5,
					onSelect = {
						selectedIndex = 5
						val library =
							userViews.firstOrNull {
								it.collectionType != CollectionType.MOVIES &&
									it.collectionType != CollectionType.TVSHOWS &&
									it.collectionType != CollectionType.LIVETV
							} ?: userViews.firstOrNull()
						if (library != null) {
							navigationRepository.navigate(itemLauncher.getUserViewDestination(library))
						}
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[5].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Genres,
					label = "Genres",
					isSelected = selectedIndex == 6,
					onSelect = {
						selectedIndex = 6
						navigationRepository.navigate(Destinations.allGenres)
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[6].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Favorite,
					label = "Favoris",
					isSelected = selectedIndex == 7,
					onSelect = {
						selectedIndex = 7
						navigationRepository.navigate(Destinations.allFavorites)
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[7].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)

				// Flexible space
				Spacer(modifier = Modifier.weight(1f))

				// Bottom group
				NavItem(
					icon = VegafoXIcons.Settings,
					label = "Paramètres",
					isSelected = selectedIndex == 8,
					onSelect = {
						selectedIndex = 8
						settingsViewModel.show()
					},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[8].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
				)

				Spacer(modifier = Modifier.height(16.dp))

				ProfileItem(
					onSelect = {
						mediaManager.clearAudioQueue()
						sessionRepository.destroyCurrentSession()
						activity?.startActivity(ActivityDestinations.startup(context))
						activity?.finishAfterTransition()
					},
					isExpanded = isExpanded,
					onFocusChanged = onItemFocusChanged,
				)

				Spacer(modifier = Modifier.height(24.dp))
			}
		}

		// Right separator — orange accent line
		Box(
			modifier =
				Modifier
					.width(1.5.dp)
					.fillMaxHeight()
					.background(SeparatorColor),
		)
	}
}
