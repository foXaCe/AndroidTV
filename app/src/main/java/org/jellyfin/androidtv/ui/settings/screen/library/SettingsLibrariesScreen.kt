package org.jellyfin.androidtv.ui.settings.screen.library

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.repository.MultiServerRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.koin.compose.koinInject
import java.util.UUID

private data class LibraryDisplayItem(
	val library: BaseItemDto,
	val displayName: String,
	val serverId: UUID,
	val userId: UUID,
)

@Composable
fun SettingsLibrariesScreen() {
	val router = LocalRouter.current
	val userViewsRepository = koinInject<UserViewsRepository>()
	val multiServerRepository = koinInject<MultiServerRepository>()
	val sessionRepository = koinInject<SessionRepository>()
	val userPreferences = koinInject<UserPreferences>()
	
	var libraries by remember { mutableStateOf<List<LibraryDisplayItem>>(emptyList()) }
	val currentSession by sessionRepository.currentSession.collectAsState()
	
	LaunchedEffect(Unit) {
		val loggedInServers = multiServerRepository.getLoggedInServers()
		val enableMultiServer = userPreferences[UserPreferences.enableMultiServerLibraries]
		
		if (enableMultiServer && loggedInServers.size > 1) {
			val aggregatedLibraries = multiServerRepository.getAggregatedLibraries(includeHidden = true)
			libraries =
				aggregatedLibraries.map { aggregated ->
					LibraryDisplayItem(
						library = aggregated.library,
						displayName = aggregated.displayName,
						serverId = aggregated.server.id,
						userId = aggregated.userId,
					)
				}
		} else {
			val session = currentSession ?: return@LaunchedEffect
			userViewsRepository.allViews.collect { views ->
				libraries =
					views.map { library ->
						LibraryDisplayItem(
							library = library,
							displayName = library.name ?: "",
							serverId = session.serverId,
							userId = session.userId,
						)
					}
			}
		}
	}

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.pref_libraries),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		items(libraries) { item ->
			val allowGridView = userViewsRepository.allowGridView(item.library.collectionType)
			val displayPreferencesId = item.library.displayPreferencesId

			if (item.library.collectionType == CollectionType.LIVETV) {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Guide), contentDescription = null) },
					headingContent = { Text(item.displayName) },
					onClick = { router.push(Routes.LIVETV_GUIDE_OPTIONS) },
				)
			} else {
				val canOpen = allowGridView && displayPreferencesId != null

				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Folder), contentDescription = null) },
					headingContent = { Text(item.displayName) },
					enabled = canOpen,
					onClick = {
						if (canOpen) {
							router.push(
								Routes.LIBRARIES_DISPLAY,
								mapOf(
									"itemId" to item.library.id.toString(),
									"displayPreferencesId" to item.library.displayPreferencesId!!,
									"serverId" to item.serverId.toString(),
									"userId" to item.userId.toString(),
								),
							)
						}
					},
				)
			}
		}
	}
}
