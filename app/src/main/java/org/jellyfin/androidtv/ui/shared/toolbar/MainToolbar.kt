package org.jellyfin.androidtv.ui.shared.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.SidebarDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.settings.compat.SettingsViewModel
import org.jellyfin.androidtv.ui.syncplay.SyncPlayDialog
import org.jellyfin.androidtv.ui.syncplay.SyncPlayViewModel
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel
import java.util.UUID

enum class MainToolbarActiveButton {
	User,
	Home,
	Library,
	Search,
	Jellyseerr,

	None,
}

/** Sidebar width — used by callers to offset content. */
val SIDEBAR_WIDTH = SidebarDimensions.widthCollapsed

private val ICON_BOX_SIZE = 48.dp
private val ICON_SIZE = 24.dp
private val CORNER_RADIUS = 12.dp
private val ACTIVE_LINE_WIDTH = 3.dp

private val TAB_COLLECTION_TYPES: List<CollectionType?> =
	listOf(
		null,
		CollectionType.MOVIES,
		CollectionType.TVSHOWS,
		CollectionType.MUSIC,
		CollectionType.LIVETV,
	)

@Composable
fun MainToolbar(
	activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
	activeLibraryId: UUID? = null,
) {
	val userViewsRepository = koinInject<UserViewsRepository>()
	val navigationRepository = koinInject<NavigationRepository>()
	val itemLauncher = koinInject<ItemLauncher>()
	val settingsViewModel = koinActivityViewModel<SettingsViewModel>()
	val syncPlayViewModel = koinActivityViewModel<SyncPlayViewModel>()

	var userViews by remember { mutableStateOf<List<BaseItemDto>>(emptyList()) }
	LaunchedEffect(Unit) {
		userViewsRepository.views.collect { views ->
			userViews = views.toList()
		}
	}

	// Active tab tracking
	var selectedTab by remember { mutableStateOf(0) }
	LaunchedEffect(activeButton, activeLibraryId, userViews) {
		selectedTab =
			when {
				activeButton == MainToolbarActiveButton.Home -> 0
				activeButton == MainToolbarActiveButton.Library && activeLibraryId != null -> {
					val lib = userViews.firstOrNull { it.id == activeLibraryId }
					when (lib?.collectionType) {
						CollectionType.MOVIES -> 1
						CollectionType.TVSHOWS -> 2
						CollectionType.MUSIC -> 3
						CollectionType.LIVETV -> 4
						else -> 0
					}
				}
				else -> 0
			}
	}

	val tabIcons =
		listOf(
			VegafoXIcons.Home,
			VegafoXIcons.Movie,
			VegafoXIcons.Tv,
			VegafoXIcons.MusicLibrary,
			VegafoXIcons.LiveTv,
		)

	val tabDescriptions =
		listOf(
			stringResource(R.string.lbl_home),
			stringResource(R.string.lbl_movies),
			stringResource(R.string.lbl_series),
			stringResource(R.string.lbl_music),
			stringResource(R.string.lbl_live_tv),
		)

	// Sidebar container
	Box(
		modifier =
			Modifier
				.width(SIDEBAR_WIDTH)
				.fillMaxHeight()
				.background(
					brush =
						Brush.horizontalGradient(
							colors =
								listOf(
									VegafoXColors.BackgroundDeep.copy(alpha = 0.98f),
									Color.Transparent,
								),
						),
				),
	) {
		Column(
			modifier =
				Modifier
					.fillMaxHeight()
					.padding(vertical = 16.dp)
					.align(Alignment.Center),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			// Top: Fox icon only (no text)
			Image(
				painter = painterResource(R.drawable.ic_vegafox_fox),
				contentDescription = "VegafoX",
				modifier = Modifier.size(36.dp),
			)

			Spacer(modifier = Modifier.weight(1f))

			// Middle: 5 navigation icons
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				tabIcons.forEachIndexed { index, icon ->
					SidebarNavIcon(
						icon = icon,
						contentDescription = tabDescriptions[index],
						isActive = selectedTab == index,
						onClick = {
							selectedTab = index
							val collectionType = TAB_COLLECTION_TYPES[index]
							if (collectionType == null) {
								navigationRepository.reset(Destinations.home)
							} else {
								val view = userViews.firstOrNull { it.collectionType == collectionType }
								if (view != null) {
									navigationRepository.navigate(
										itemLauncher.getUserViewDestination(view),
									)
								}
							}
						},
					)
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			// Bottom: Settings icon
			SidebarNavIcon(
				icon = VegafoXIcons.Settings,
				contentDescription = stringResource(R.string.settings),
				isActive = false,
				onClick = { settingsViewModel.show() },
			)
		}
	}

	// SyncPlay dialog — triggered externally via SyncPlayViewModel
	val syncPlayVisible by syncPlayViewModel.visible.collectAsState()
	if (syncPlayVisible) {
		SyncPlayDialog(
			visible = true,
			onDismissRequest = { syncPlayViewModel.hide() },
		)
	}
}

@Composable
private fun SidebarNavIcon(
	icon: ImageVector,
	contentDescription: String,
	isActive: Boolean,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val bgColor =
		when {
			isActive -> VegafoXColors.OrangeSoft
			isFocused -> VegafoXColors.Surface
			else -> Color.Transparent
		}

	val iconColor =
		when {
			isActive -> VegafoXColors.OrangePrimary
			isFocused -> VegafoXColors.TextPrimary
			else -> VegafoXColors.TextHint
		}

	val shape = RoundedCornerShape(CORNER_RADIUS)

	Box(
		modifier =
			Modifier
				.size(ICON_BOX_SIZE)
				.clip(shape)
				.background(bgColor)
				.drawWithContent {
					drawContent()
					if (isActive) {
						drawRect(
							color = VegafoXColors.OrangePrimary,
							topLeft = Offset.Zero,
							size = Size(ACTIVE_LINE_WIDTH.toPx(), size.height),
						)
					}
				}.focusable(interactionSource = interactionSource)
				.onKeyEvent { keyEvent ->
					if ((keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) &&
						keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP
					) {
						onClick()
						true
					} else if ((keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) &&
						keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN
					) {
						true
					} else {
						false
					}
				}.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = icon,
			contentDescription = contentDescription,
			modifier = Modifier.size(ICON_SIZE),
			tint = iconColor,
		)
	}
}

fun setupMainToolbarComposeView(
	composeView: androidx.compose.ui.platform.ComposeView,
	activeButton: MainToolbarActiveButton = MainToolbarActiveButton.None,
	activeLibraryId: UUID? = null,
) {
	composeView.setContent {
		JellyfinTheme {
			MainToolbar(
				activeButton = activeButton,
				activeLibraryId = activeLibraryId,
			)
		}
	}
}
