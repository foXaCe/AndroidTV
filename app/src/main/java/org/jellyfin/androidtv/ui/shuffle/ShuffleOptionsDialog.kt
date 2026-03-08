package org.jellyfin.androidtv.ui.shuffle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.model.AggregatedLibrary
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.genresApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemSortBy
import timber.log.Timber
import java.util.UUID

internal enum class ShuffleMode {
	MAIN, LIBRARIES, GENRES
}

@Stable
internal data class LibrarySelection(
	val library: BaseItemDto,
	val serverId: UUID?,
	val displayName: String
)

@Composable
fun ShuffleOptionsDialog(
	userViews: List<BaseItemDto>,
	aggregatedLibraries: List<AggregatedLibrary>,
	enableMultiServer: Boolean,
	shuffleContentType: String,
	api: ApiClient,
	onDismiss: () -> Unit,
	onShuffle: (libraryId: UUID?, serverId: UUID?, genreName: String?, contentType: String, collectionType: CollectionType?) -> Unit,
) {
	var mode by remember { mutableStateOf(ShuffleMode.MAIN) }
	var genres by remember { mutableStateOf<List<BaseItemDto>>(emptyList()) }
	var loadingGenres by remember { mutableStateOf(false) }
	val initialFocusRequester = remember { FocusRequester() }

	val libraryOptions = remember(userViews, aggregatedLibraries, enableMultiServer) {
		if (enableMultiServer && aggregatedLibraries.isNotEmpty()) {
			aggregatedLibraries.map { aggLib ->
				LibrarySelection(aggLib.library, aggLib.server.id, aggLib.displayName)
			}
		} else {
			userViews.map { lib ->
				LibrarySelection(lib, null, lib.name ?: "")
			}
		}
	}

	LaunchedEffect(mode) {
		if (mode == ShuffleMode.GENRES && genres.isEmpty()) {
			loadingGenres = true
			try {
				val response = withContext(Dispatchers.IO) {
					api.genresApi.getGenres(
						sortBy = setOf(ItemSortBy.SORT_NAME),
					)
				}
				genres = response.content.items
			} catch (e: Exception) {
				Timber.e(e, "Failed to load genres")
			}
			loadingGenres = false
		}
	}

	Dialog(
		onDismissRequest = {
			if (mode != ShuffleMode.MAIN) mode = ShuffleMode.MAIN else onDismiss()
		},
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier
					.widthIn(min = 340.dp, max = 440.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.dialogScrim)
					.border(1.dp, Color.White.copy(alpha = 0.1f), JellyfinTheme.shapes.dialog)
					.padding(vertical = 20.dp),
			) {
				// Title row with optional back button
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
						.padding(bottom = 12.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (mode != ShuffleMode.MAIN) {
						val backInteraction = remember { MutableInteractionSource() }
						val backFocused by backInteraction.collectIsFocusedAsState()
						Box(
							modifier = Modifier
								.size(32.dp)
								.clip(JellyfinTheme.shapes.small)
								.background(if (backFocused) JellyfinTheme.colorScheme.listButtonFocused else Color.Transparent)
								.clickable(
									interactionSource = backInteraction,
									indication = null,
								) { mode = ShuffleMode.MAIN }
								.focusable(interactionSource = backInteraction),
							contentAlignment = Alignment.Center,
						) {
							androidx.compose.material3.Text(
								text = "\u276E",
								style = JellyfinTheme.typography.titleMedium,
								color = if (backFocused) JellyfinTheme.colorScheme.textPrimary else JellyfinTheme.colorScheme.textSecondary,
							)
						}
						Spacer(modifier = Modifier.width(12.dp))
					}

					androidx.compose.material3.Text(
						text = when (mode) {
							ShuffleMode.MAIN -> stringResource(R.string.lbl_shuffle_by)
							ShuffleMode.LIBRARIES -> stringResource(R.string.lbl_select_library)
							ShuffleMode.GENRES -> stringResource(R.string.lbl_select_genre)
						},
						style = JellyfinTheme.typography.titleLarge,
						color = JellyfinTheme.colorScheme.textPrimary,
					)
				}

				// Divider
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)

				Spacer(modifier = Modifier.height(8.dp))

				// Content
				when (mode) {
					ShuffleMode.MAIN -> {
						GlassDialogRow(
							icon = ImageVector.vectorResource(R.drawable.ic_folder),
							label = stringResource(R.string.lbl_library),
							onClick = { mode = ShuffleMode.LIBRARIES },
							focusRequester = initialFocusRequester,
						)
						GlassDialogRow(
							icon = ImageVector.vectorResource(R.drawable.ic_masks),
							label = stringResource(R.string.lbl_genre),
							onClick = { mode = ShuffleMode.GENRES },
						)
					}
					ShuffleMode.LIBRARIES -> {
						LazyColumn {
							items(libraryOptions.size) { index ->
								val libOption = libraryOptions[index]
								GlassDialogRow(
									label = libOption.displayName,
									onClick = {
										onShuffle(libOption.library.id, libOption.serverId, null, shuffleContentType, libOption.library.collectionType)
									},
									focusRequester = if (index == 0) initialFocusRequester else null,
								)
							}
						}
					}
					ShuffleMode.GENRES -> {
						if (loadingGenres) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(24.dp),
								horizontalArrangement = Arrangement.Center,
							) {
								CircularProgressIndicator(
									strokeWidth = 2.dp,
									color = JellyfinTheme.colorScheme.primary,
								)
							}
						} else {
							LazyColumn {
								items(genres.size) { index ->
									val genre = genres[index]
									GlassDialogRow(
										label = genre.name ?: "",
										onClick = {
											onShuffle(null, null, genre.name, shuffleContentType, null)
										},
										focusRequester = if (index == 0) initialFocusRequester else null,
									)
								}
							}
						}
					}
				}

				// Cancel
				Spacer(modifier = Modifier.height(4.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.divider),
				)
				Spacer(modifier = Modifier.height(4.dp))

				GlassDialogRow(
					label = stringResource(R.string.lbl_cancel),
					onClick = onDismiss,
					contentColor = JellyfinTheme.colorScheme.textHint,
				)
			}
		}

		LaunchedEffect(mode) {
			initialFocusRequester.requestFocus()
		}
	}
}

/**
 * Reusable focusable row for glass-morphism dialogs matching the TrackSelectorDialog style.
 */
@Composable
internal fun GlassDialogRow(
	label: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	focusRequester: FocusRequester? = null,
	contentColor: Color = JellyfinTheme.colorScheme.textPrimary,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val focusModifier = if (focusRequester != null) {
		Modifier.focusRequester(focusRequester)
	} else {
		Modifier
	}

	Row(
		modifier = focusModifier
			.then(modifier)
			.fillMaxWidth()
			.clickable(
				interactionSource = interactionSource,
				indication = null,
			) { onClick() }
			.focusable(interactionSource = interactionSource)
			.background(
				if (isFocused) JellyfinTheme.colorScheme.listButtonFocused else Color.Transparent,
			)
			.padding(horizontal = 24.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (icon != null) {
			Icon(
				imageVector = icon,
				contentDescription = null,
				tint = if (isFocused) JellyfinTheme.colorScheme.onSurface else contentColor,
				modifier = Modifier.size(20.dp),
			)
			Spacer(modifier = Modifier.width(16.dp))
		}

		androidx.compose.material3.Text(
			text = label,
			style = JellyfinTheme.typography.titleMedium,
			fontWeight = FontWeight.W400,
			color = if (isFocused) JellyfinTheme.colorScheme.onSurface else contentColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)
	}
}
