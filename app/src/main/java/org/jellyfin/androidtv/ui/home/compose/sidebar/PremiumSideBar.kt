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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
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

/** Three visual states of the premium sidebar. */
enum class SidebarState {
	/** Sidebar invisible (0dp width) — default at launch and after navigation. */
	HIDDEN,

	/** Sidebar shows icons only (72dp width). */
	COMPACT,

	/** Sidebar fully open with icons and labels (220dp width). */
	EXPANDED,
}

/** Width of the premium sidebar in collapsed state — used by callers to know the reserved space. */
val PREMIUM_SIDEBAR_WIDTH_COLLAPSED = SidebarDimensions.widthCollapsed

/** Width of the premium sidebar in expanded state. */
private val PREMIUM_SIDEBAR_WIDTH_EXPANDED = SidebarDimensions.widthExpanded

private val SidebarBackgroundColor = Color(0xFF060A0F)
private val SeparatorColor = VegafoXColors.OrangePrimary.copy(alpha = 0.40f)
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
private const val NAV_ITEM_COUNT = 10
private const val SHIMMER_RESUME_DELAY_MS = 3000L
private const val SHIMMER_CYCLE_MS = 1500
private const val SHIMMER_STAGGER_MS = 300
private const val ANIM_HIDDEN_TO_COMPACT_MS = 180
private const val ANIM_COMPACT_TO_EXPANDED_MS = 220
private const val ANIM_TO_HIDDEN_MS = 150

/**
 * Premium sidebar — left-side navigation for the home screen.
 *
 * Three states: [SidebarState.HIDDEN] (0dp), [SidebarState.COMPACT] (72dp icons only),
 * [SidebarState.EXPANDED] (220dp icons + labels).
 *
 * Transitions:
 * - D-pad Left from content → HIDDEN→COMPACT (handled by caller via [onStateChange])
 * - D-pad Left in sidebar → COMPACT→EXPANDED
 * - D-pad Right / Back / item navigation → any→HIDDEN
 */
@Composable
fun PremiumSideBar(
	state: SidebarState,
	onStateChange: (SidebarState) -> Unit,
	homeFocusRequester: FocusRequester,
	modifier: Modifier = Modifier,
) {
	var selectedIndex by remember { mutableIntStateOf(0) }
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

	// ── Focus tracking (for shimmer control) ──────────────────────────
	var focusCount by remember { mutableIntStateOf(0) }
	val anyItemFocused by remember { derivedStateOf { focusCount > 0 } }

	val onItemFocusChanged: (Boolean) -> Unit =
		remember {
			{ focused: Boolean ->
				focusCount = (focusCount + if (focused) 1 else -1).coerceAtLeast(0)
			}
		}

	// ── Derived flags ─────────────────────────────────────────────────
	val isExpanded = state == SidebarState.EXPANDED
	val isVisible = state != SidebarState.HIDDEN

	// ── Width animation (different specs per transition) ──────────────
	val sidebarWidth by animateDpAsState(
		targetValue =
			when (state) {
				SidebarState.HIDDEN -> 0.dp
				SidebarState.COMPACT -> PREMIUM_SIDEBAR_WIDTH_COLLAPSED
				SidebarState.EXPANDED -> PREMIUM_SIDEBAR_WIDTH_EXPANDED
			},
		animationSpec =
			when (state) {
				SidebarState.HIDDEN -> tween(ANIM_TO_HIDDEN_MS, easing = EaseInCubic)
				SidebarState.COMPACT -> tween(ANIM_HIDDEN_TO_COMPACT_MS, easing = EaseOutCubic)
				SidebarState.EXPANDED -> tween(ANIM_COMPACT_TO_EXPANDED_MS, easing = EaseOutCubic)
			},
		label = "sidebarWidth",
	)

	// ── Background alpha ──────────────────────────────────────────────
	val bgAlpha by animateFloatAsState(
		targetValue = if (isExpanded) 1f else 0.95f,
		animationSpec = tween(200),
		label = "bgAlpha",
	)

	// ── Separator alpha ───────────────────────────────────────────────
	val separatorAlpha by animateFloatAsState(
		targetValue = if (isVisible) 1f else 0f,
		animationSpec = tween(if (isVisible) ANIM_HIDDEN_TO_COMPACT_MS else ANIM_TO_HIDDEN_MS),
		label = "separatorAlpha",
	)

	// ── Focus on Home item when sidebar appears from hidden ───────────
	var previousState by remember { mutableStateOf(SidebarState.HIDDEN) }
	LaunchedEffect(state) {
		if (state == SidebarState.COMPACT && previousState == SidebarState.HIDDEN) {
			delay(50)
			try {
				homeFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
		previousState = state
	}

	// ── Shimmer animation ─────────────────────────────────────────────
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

	// ── Navigate and hide sidebar ─────────────────────────────────────
	val navigateAndHide: (() -> Unit) -> (() -> Unit) = { action ->
		{
			action()
			onStateChange(SidebarState.HIDDEN)
		}
	}

	// ── Layout ────────────────────────────────────────────────────────
	Row(
		modifier =
			modifier
				.fillMaxHeight()
				.onPreviewKeyEvent { event ->
					if (event.type == KeyEventType.KeyDown) {
						when (event.key) {
							Key.DirectionLeft -> {
								when (state) {
									SidebarState.COMPACT -> {
										onStateChange(SidebarState.EXPANDED)
										true
									}
									SidebarState.EXPANDED -> true // consume, already at max
									SidebarState.HIDDEN -> false
								}
							}
							Key.Back -> {
								if (isVisible) {
									onStateChange(SidebarState.HIDDEN)
									true
								} else {
									false
								}
							}
							Key.DirectionRight -> {
								if (isVisible) {
									onStateChange(SidebarState.HIDDEN)
								}
								false // let focus move right naturally
							}
							else -> false
						}
					} else {
						false
					}
				},
	) {
		Box(
			modifier =
				Modifier
					.width(sidebarWidth)
					.fillMaxHeight()
					.clipToBounds()
					.then(
						if (isExpanded) Modifier.shadow(elevation = 8.dp) else Modifier,
					).background(SidebarBackgroundColor.copy(alpha = bgAlpha)),
		) {
			// ── Navigation items ──────────────────────────────────────
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
					onSelect =
						navigateAndHide {
							selectedIndex = 0
							navigationRepository.navigate(Destinations.home)
						},
					isExpanded = isExpanded,
					shimmerAlpha = lerp(1f, shimmerRawAlphas[0].value, shimmerStrength),
					onFocusChanged = onItemFocusChanged,
					focusRequester = homeFocusRequester,
				)
				Spacer(modifier = Modifier.height(6.dp))

				NavItem(
					icon = VegafoXIcons.Search,
					label = "Recherche",
					isSelected = selectedIndex == 1,
					onSelect =
						navigateAndHide {
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
					onSelect =
						navigateAndHide {
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
					onSelect =
						navigateAndHide {
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
					icon = VegafoXIcons.MusicLibrary,
					label = "Musique",
					isSelected = selectedIndex == 4,
					onSelect =
						navigateAndHide {
							selectedIndex = 4
							val library = userViews.firstOrNull { it.collectionType == CollectionType.MUSIC }
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
					icon = VegafoXIcons.LiveTv,
					label = "Live TV",
					isSelected = selectedIndex == 5,
					onSelect =
						navigateAndHide {
							selectedIndex = 5
							val library = userViews.firstOrNull { it.collectionType == CollectionType.LIVETV }
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
					onSelect =
						navigateAndHide {
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
					onSelect =
						navigateAndHide {
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
					onSelect =
						navigateAndHide {
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
						onStateChange(SidebarState.HIDDEN)
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
					.graphicsLayer { alpha = separatorAlpha }
					.background(SeparatorColor),
		)
	}
}
