package org.jellyfin.androidtv.ui.itemdetail.v2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.preference.constant.NavbarPosition
import org.jellyfin.androidtv.ui.base.CircularProgressIndicator
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.home.mediabar.TrailerResolver
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MovieDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MusicDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.PersonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeasonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeriesDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.ui.playback.PrePlaybackTrackSelector
import org.jellyfin.androidtv.ui.playback.ThemeMusicPlayer
import org.jellyfin.androidtv.ui.playlist.showAddToPlaylistDialog
import org.jellyfin.androidtv.ui.shared.toolbar.LeftSidebarNavigation
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbar
import org.jellyfin.androidtv.ui.shared.toolbar.MainToolbarActiveButton
import org.jellyfin.androidtv.util.BitmapBlur
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.sdk.TrailerUtils.getExternalTrailerIntent
import org.jellyfin.androidtv.util.sdk.TrailerUtils.hasPlayableTrailers
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.UUID

class ItemDetailsFragment : Fragment() {

	private val viewModel: ItemDetailsViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val playbackHelper: PlaybackHelper by inject()
	private val mediaManager: MediaManager by inject()
	private val userPreferences: UserPreferences by inject()
	private val userSettingPreferences: UserSettingPreferences by inject()
	private val trackSelector: PrePlaybackTrackSelector by inject()
	private val playbackLauncher: PlaybackLauncher by inject()
	private val dataRefreshService: DataRefreshService by inject()
	private val themeMusicPlayer: ThemeMusicPlayer by inject()

	private var backdropImage: ImageView? = null
	private var gradientView: View? = null
	private var sidebarId: Int = View.NO_ID
	private var contentId: Int = View.NO_ID
	private var toolbarId: Int = View.NO_ID
	private var lastFocusedBeforeSidebar: View? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		sidebarId = View.generateViewId()
		contentId = View.generateViewId()
		toolbarId = View.generateViewId()

		val mainContainer = object : FrameLayout(requireContext()) {
			override fun dispatchKeyEvent(event: KeyEvent): Boolean {
				if (event.action == KeyEvent.ACTION_DOWN) {
					// Intercept RIGHT when focus is in sidebar to redirect to content
					if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						val sidebar = findViewById<View>(sidebarId)
						val focused = findFocus()
						if (sidebar != null && focused != null && isDescendantOf(focused, sidebar)) {
							// Restore focus to where the user was before entering the sidebar
							val restoreTarget = lastFocusedBeforeSidebar
							if (restoreTarget != null && restoreTarget.isAttachedToWindow && restoreTarget.isFocusable) {
								restoreTarget.requestFocus()
								return true
							}
							// Fallback to content ComposeView
							val content = findViewById<View>(contentId)
							if (content != null) {
								content.requestFocus()
								return true
							}
						}
					}

					// Intercept DOWN when focus is in top toolbar to redirect to content
					if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						val toolbar = findViewById<View>(toolbarId)
						val focused = findFocus()
						if (toolbar != null && focused != null && isDescendantOf(focused, toolbar)) {
							val restoreTarget = lastFocusedBeforeSidebar
							if (restoreTarget != null && restoreTarget.isAttachedToWindow && restoreTarget.isFocusable) {
								restoreTarget.requestFocus()
								return true
							}
							val content = findViewById<View>(contentId)
							if (content != null) {
								content.requestFocus()
								return true
							}
						}
					}
				}

				// Consume LEFT when already in sidebar so focus doesn't get trapped
				if (event.action == KeyEvent.ACTION_DOWN &&
					event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					val sidebar = findViewById<View>(sidebarId)
					val focused = findFocus()
					if (sidebar != null && focused != null && isDescendantOf(focused, sidebar)) {
						return true
					}
				}

				// Consume UP when already in toolbar so focus doesn't get trapped
				if (event.action == KeyEvent.ACTION_DOWN &&
					event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					val toolbar = findViewById<View>(toolbarId)
					val focused = findFocus()
					if (toolbar != null && focused != null && isDescendantOf(focused, toolbar)) {
						return true
					}
				}

				// Let children (Compose) process the event first
				val handled = super.dispatchKeyEvent(event)

				// If LEFT wasn't handled by Compose (focus is at left edge), redirect to sidebar
				// Only on fresh press (repeatCount == 0) to avoid triggering when holding left to fast-scroll
				if (!handled && event.action == KeyEvent.ACTION_DOWN &&
					event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.repeatCount == 0) {
					val sidebar = findViewById<View>(sidebarId)
					if (sidebar != null && sidebar.isVisible) {
						// Save current focus before entering sidebar
						lastFocusedBeforeSidebar = findFocus()
						sidebar.requestFocus()
						return true
					}
				}

				// If UP wasn't handled by Compose (focus is at top edge), redirect to toolbar
				// Only on fresh press (repeatCount == 0) to avoid triggering when holding up
				if (!handled && event.action == KeyEvent.ACTION_DOWN &&
					event.keyCode == KeyEvent.KEYCODE_DPAD_UP && event.repeatCount == 0) {
					val toolbar = findViewById<View>(toolbarId)
					if (toolbar != null && toolbar.isVisible) {
						lastFocusedBeforeSidebar = findFocus()
						toolbar.requestFocus()
						return true
					}
				}

				return handled
			}
		}.apply {
			layoutParams = ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
			)
			setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ds_background))
		}

		backdropImage = ImageView(requireContext()).apply {
			layoutParams = FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT
			)
			scaleType = ImageView.ScaleType.CENTER_CROP
			alpha = 0.8f
		}
		mainContainer.addView(backdropImage)

		gradientView = View(requireContext()).apply {
			layoutParams = FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT
			)
			setBackgroundResource(R.drawable.detail_backdrop_gradient)
		}
		mainContainer.addView(gradientView)

		val contentView = ComposeView(requireContext()).apply {
			id = contentId
			layoutParams = FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT
			)
			setContent {
				JellyfinTheme {
					ItemDetailsContent()
				}
			}
		}
		mainContainer.addView(contentView)

		val navbarPosition = userPreferences[UserPreferences.navbarPosition]

		when (navbarPosition) {
			NavbarPosition.LEFT -> {
				val sidebarOverlay = ComposeView(requireContext()).apply {
					id = sidebarId
					layoutParams = FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.WRAP_CONTENT,
						FrameLayout.LayoutParams.MATCH_PARENT
					)
					setContent {
						LeftSidebarNavigation(
							activeButton = MainToolbarActiveButton.None,
						)
					}
				}
				mainContainer.addView(sidebarOverlay)
			}
			NavbarPosition.TOP -> {
				val toolbarOverlay = ComposeView(requireContext()).apply {
					id = toolbarId
					layoutParams = FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.WRAP_CONTENT
					)
					setContent {
						MainToolbar(
							activeButton = MainToolbarActiveButton.None,
						)
					}
				}
				mainContainer.addView(toolbarOverlay)
			}
		}

		return mainContainer
	}

	private fun isDescendantOf(view: View, ancestor: View): Boolean {
		var current: android.view.ViewParent? = view.parent
		while (current != null) {
			if (current === ancestor) return true
			current = current.parent
		}
		return false
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val itemIdStr = arguments?.getString("ItemId")
		val serverIdStr = arguments?.getString("ServerId")

		val itemId = Utils.uuidOrNull(itemIdStr) ?: return
		val serverId = Utils.uuidOrNull(serverIdStr)

		viewModel.loadItem(itemId, serverId)

		viewModel.uiState
			.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
			.onEach { uiState ->
				val item = uiState.item
				if (item != null) {
					// Play theme music for the item
					themeMusicPlayer.playThemeMusicForItem(item)

					if (item.type == BaseItemKind.PERSON || item.type == BaseItemKind.PLAYLIST) {
						backdropImage?.isVisible = false
						gradientView?.isVisible = false
					} else {
						backdropImage?.isVisible = true
						gradientView?.isVisible = true
						val backdropUrl = getBackdropUrl(item, viewModel.effectiveApi)
						if (backdropUrl != null) {
						val blurAmount = userSettingPreferences[UserSettingPreferences.detailsBackgroundBlurAmount]
						val imageLoader = coil3.SingletonImageLoader.get(requireContext())
						lifecycleScope.launch {
							val result = imageLoader.execute(
								coil3.request.ImageRequest.Builder(requireContext())
									.data(backdropUrl)
									.build()
							)
							val bitmap = result.image?.toBitmap()
							if (bitmap != null) {
								val useComposeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
								val finalBitmap: android.graphics.Bitmap = if (!useComposeBlur && blurAmount > 0) {
									BitmapBlur.blur(bitmap, blurAmount)
								} else {
									if (useComposeBlur && blurAmount > 0) {
										// On Android 12+, apply RenderEffect blur
										backdropImage?.setRenderEffect(
											android.graphics.RenderEffect.createBlurEffect(
												blurAmount.toFloat(), blurAmount.toFloat(),
												android.graphics.Shader.TileMode.CLAMP
											)
										)
									}
									bitmap
								}
								backdropImage?.setImageBitmap(finalBitmap)
								backdropImage?.alpha = 0.8f
							}
						}
					}
					}
				}
			}
			.launchIn(lifecycleScope)
	}

	override fun onDestroyView() {
		themeMusicPlayer.stop()
		super.onDestroyView()
	}

	// ---- Compose dispatcher ----

	@Composable
	private fun ItemDetailsContent() {
		val uiState by viewModel.uiState.collectAsState()
		val contentFocusRequester = remember { FocusRequester() }

		if (uiState.isLoading) {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center,
			) {
				CircularProgressIndicator()
			}
		} else if (uiState.error != null && uiState.item == null) {
			ErrorState(
				message = stringResource(uiState.error!!.messageRes),
				onRetry = { viewModel.retry() },
			)
		} else {
			val item = uiState.item ?: return
			val api = viewModel.effectiveApi
			val context = LocalContext.current
			val blurAmount = userSettingPreferences[UserSettingPreferences.detailsBackgroundBlurAmount]

			val onNavigateToItem: (UUID) -> Unit = { id ->
				navigationRepository.navigate(Destinations.itemDetails(id, viewModel.serverId))
			}

			when (item.type) {
				BaseItemKind.PERSON -> PersonDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					showBackdrop = true,
					api = api,
					onNavigateToItem = onNavigateToItem,
				)

				BaseItemKind.SEASON -> SeasonDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					showBackdrop = false,
					api = api,
					blurAmount = blurAmount,
					onNavigateToItem = onNavigateToItem,
					onPlayEpisode = { episode -> play(episode, 0, false) },
					onToggleWatched = { viewModel.toggleWatched() },
					onToggleFavorite = { viewModel.toggleFavorite() },
				)

				BaseItemKind.SERIES -> {
					val actionCallbacks = createActionCallbacks(item, uiState, context)
					SeriesDetailsContent(
						uiState = uiState,
						contentFocusRequester = contentFocusRequester,
						api = api,
						actionCallbacks = actionCallbacks,
						onNavigateToItem = onNavigateToItem,
					)
				}

				BaseItemKind.MUSIC_ALBUM, BaseItemKind.MUSIC_ARTIST, BaseItemKind.PLAYLIST -> {
					val actionCallbacks = createActionCallbacks(item, uiState, context)
					MusicDetailsContent(
						uiState = uiState,
						contentFocusRequester = contentFocusRequester,
						api = api,
						actionCallbacks = actionCallbacks,
						onNavigateToItem = onNavigateToItem,
						onPlayFromHere = { trackIds ->
							playbackHelper.retrieveAndPlay(trackIds, false, null, null, context)
						},
						onPlaySingle = { trackId ->
							playbackHelper.retrieveAndPlay(trackId, false, context)
						},
						onPlayInstantMix = { trackItem ->
							playbackHelper.playInstantMix(context, trackItem)
						},
						onQueueAudioItem = { trackItem ->
							mediaManager.queueAudioItem(trackItem)
						},
						onMovePlaylistItem = { from, to ->
							viewModel.movePlaylistItem(from, to)
						},
						onRemoveFromPlaylist = { index ->
							viewModel.removeFromPlaylist(index)
						},
					)
				}

				else -> {
					val actionCallbacks = createActionCallbacks(item, uiState, context)
					MovieDetailsContent(
						uiState = uiState,
						contentFocusRequester = contentFocusRequester,
						api = api,
						actionCallbacks = actionCallbacks,
						onNavigateToItem = onNavigateToItem,
					)
				}
			}
		}
	}

	private fun createActionCallbacks(
		item: BaseItemDto,
		uiState: ItemDetailsUiState,
		context: android.content.Context,
	): DetailActionCallbacks = DetailActionCallbacks(
		trackSelector = trackSelector,
		hasPlayableTrailers = hasPlayableTrailers(context, item),
		onPlay = { handlePlay(item, uiState) },
		onResume = { handleResume(item) },
		onShuffle = { handleShuffle(item) },
		onPlayTrailers = { playTrailers(item) },
		onPlayInstantMix = { playbackHelper.playInstantMix(context, item) },
		onToggleWatched = { viewModel.toggleWatched() },
		onToggleFavorite = { viewModel.toggleFavorite() },
		onConfirmDelete = { confirmDeleteItem(item) },
		onAddToPlaylist = { showAddToPlaylistDialog(context, item.id) },
		onGoToSeries = if (item.type == BaseItemKind.EPISODE && item.seriesId != null) {
			{ item.seriesId?.let { seriesId -> navigationRepository.navigate(Destinations.itemDetails(seriesId, viewModel.serverId)) } }
		} else null,
		onLoadItem = { id -> viewModel.loadItem(id) },
	)

	// ---- Playback helpers ----

	private fun play(item: BaseItemDto, positionMs: Int, shuffle: Boolean) {
		playbackHelper.getItemsToPlay(
			requireContext(),
			item,
			positionMs == 0 && item.type == BaseItemKind.MOVIE,
			shuffle,
			object : Response<List<BaseItemDto>>(lifecycle) {
				override fun onResponse(response: List<BaseItemDto>) {
					if (!isActive) return
					if (response.isEmpty()) {
						Timber.e("No items to play - ignoring play request.")
						return
					}
					playbackLauncher.launch(requireContext(), response, positionMs, false, 0, shuffle)
				}
			}
		)
	}

	private fun handlePlay(item: BaseItemDto, uiState: ItemDetailsUiState) {
		when (item.type) {
			BaseItemKind.SERIES -> {
				if (uiState.nextUp.isNotEmpty()) {
					play(uiState.nextUp.first(), 0, false)
				} else {
					play(item, 0, false)
				}
			}
			BaseItemKind.SEASON -> {
				if (uiState.episodes.isNotEmpty()) {
					val unwatched = uiState.episodes.firstOrNull { !(it.userData?.played ?: false) }
					val episode = unwatched ?: uiState.episodes.first()
					play(episode, 0, false)
				}
			}
			else -> {
				play(item, 0, false)
			}
		}
	}

	private fun handleResume(item: BaseItemDto) {
		val prerollMs = (userPreferences[UserPreferences.resumeSubtractDuration].toIntOrNull() ?: 0) * 1000
		val posMs = ((item.userData?.playbackPositionTicks ?: 0L) / 10_000).toInt()
		val position = maxOf(posMs - prerollMs, 0)
		play(item, position, false)
	}

	private fun handleShuffle(item: BaseItemDto) {
		play(item, 0, true)
	}

	private fun playTrailers(item: BaseItemDto) {
		val localTrailerCount = item.localTrailerCount ?: 0

		if (localTrailerCount < 1) {
			// External trailer — resolve YouTube video and play in-app WebView
			lifecycleScope.launch {
				try {
					val trailerInfo = withContext(Dispatchers.IO) {
						TrailerResolver.resolveTrailerFromItem(item)
					}

					if (trailerInfo != null) {
						val segmentsJson = trailerInfo.segments.joinToString(",", "[", "]") { seg ->
							"""{"start":${seg.startTime},"end":${seg.endTime},"category":"${seg.category}","action":"${seg.actionType}"}"""
						}
						navigationRepository.navigate(Destinations.trailerPlayer(
							videoId = trailerInfo.youtubeVideoId,
							startSeconds = trailerInfo.startSeconds,
							segmentsJson = segmentsJson,
						))
					} else {
						// No YouTube trailer found — fall back to external intent
						val intent = getExternalTrailerIntent(requireContext(), item)
						if (intent != null) {
							val chooser = Intent.createChooser(intent, getString(R.string.lbl_play_trailers))
							startActivity(chooser)
						} else {
							Toast.makeText(requireContext(), getString(R.string.no_player_message), Toast.LENGTH_LONG).show()
						}
					}
				} catch (e: Exception) {
					Timber.w(e, "Failed to resolve trailer")
					// Fall back to external intent
					try {
						val intent = getExternalTrailerIntent(requireContext(), item)
						if (intent != null) {
							val chooser = Intent.createChooser(intent, getString(R.string.lbl_play_trailers))
							startActivity(chooser)
						}
					} catch (e2: ActivityNotFoundException) {
						Timber.w(e2, "Unable to open external trailer")
						Toast.makeText(requireContext(), getString(R.string.no_player_message), Toast.LENGTH_LONG).show()
					}
				}
			}
		} else {
			// Local trailer
			lifecycleScope.launch {
				try {
					val trailers = withContext(Dispatchers.IO) {
						viewModel.effectiveApi.userLibraryApi.getLocalTrailers(itemId = item.id).content
					}
					if (trailers.isNotEmpty()) {
						val trailerIds = trailers.map { it.id }
						playbackHelper.retrieveAndPlay(trailerIds, false, null, null, requireContext())
					}
				} catch (e: ApiClientException) {
					Timber.e(e, "Error retrieving trailers for playback")
					Toast.makeText(requireContext(), getString(R.string.msg_video_playback_error), Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun confirmDeleteItem(item: BaseItemDto) {
		android.app.AlertDialog.Builder(requireContext())
			.setTitle(R.string.item_delete_confirm_title)
			.setMessage(R.string.item_delete_confirm_message)
			.setNegativeButton(R.string.lbl_no, null)
			.setPositiveButton(R.string.lbl_delete) { _, _ ->
				deleteItem(item)
			}
			.show()
	}

	private fun deleteItem(item: BaseItemDto) {
		lifecycleScope.launch {
			try {
				withContext(Dispatchers.IO) {
					viewModel.effectiveApi.libraryApi.deleteItem(itemId = item.id)
				}
			} catch (e: ApiClientException) {
				Timber.e(e, "Failed to delete item ${item.name} (id=${item.id})")
				Toast.makeText(requireContext(), getString(R.string.item_deletion_failed, item.name), Toast.LENGTH_LONG).show()
				return@launch
			}
			dataRefreshService.lastDeletedItemId = item.id
			if (navigationRepository.canGoBack) navigationRepository.goBack()
			else navigationRepository.navigate(Destinations.home)
			Toast.makeText(requireContext(), getString(R.string.item_deleted, item.name), Toast.LENGTH_LONG).show()
		}
	}
}
