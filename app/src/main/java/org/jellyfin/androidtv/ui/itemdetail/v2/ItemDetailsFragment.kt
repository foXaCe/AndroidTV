package org.jellyfin.androidtv.ui.itemdetail.v2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil3.request.crossfade
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.CircularProgressIndicator
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.home.mediabar.TrailerResolver
import org.jellyfin.androidtv.ui.itemdetail.v2.content.LiveTvDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MovieDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MusicDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.PersonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeasonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeriesDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeriesTimerDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.getBackdropUrl
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.ui.playback.PrePlaybackTrackSelector
import org.jellyfin.androidtv.ui.playback.ThemeMusicPlayer
import org.jellyfin.androidtv.ui.shared.components.DarkGridNoiseBackground
import org.jellyfin.androidtv.util.BitmapBlur
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.sdk.TrailerUtils.getExternalTrailerIntent
import org.jellyfin.androidtv.util.sdk.TrailerUtils.hasPlayableTrailers
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.UUID

class ItemDetailsFragment : Fragment() {
	data class Args(
		val itemId: UUID,
		val serverId: UUID? = null,
		val channelId: UUID? = null,
		val programInfoJson: String? = null,
		val seriesTimerJson: String? = null,
	) {
		fun toBundle() =
			bundleOf(
				KEY_ITEM_ID to itemId.toString(),
				KEY_SERVER_ID to serverId?.toString(),
				KEY_CHANNEL_ID to channelId?.toString(),
				KEY_PROGRAM_INFO to programInfoJson,
				KEY_SERIES_TIMER to seriesTimerJson,
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val itemId =
					bundle
						?.getString(KEY_ITEM_ID)
						?.let(UUID::fromString) ?: return null
				return Args(
					itemId = itemId,
					serverId = bundle.getString(KEY_SERVER_ID)?.let(UUID::fromString),
					channelId = bundle.getString(KEY_CHANNEL_ID)?.let(UUID::fromString),
					programInfoJson = bundle.getString(KEY_PROGRAM_INFO),
					seriesTimerJson = bundle.getString(KEY_SERIES_TIMER),
				)
			}
		}
	}

	companion object {
		internal const val KEY_ITEM_ID = "ItemId"
		internal const val KEY_SERVER_ID = "ServerId"
		internal const val KEY_CHANNEL_ID = "ChannelId"
		internal const val KEY_PROGRAM_INFO = "ProgramInfo"
		internal const val KEY_SERIES_TIMER = "SeriesTimer"
	}

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

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		val mainContainer =
			FrameLayout(requireContext()).apply {
				layoutParams =
					ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT,
					)
				// Background color handled by DarkGridNoiseBackground
			}

		// Grid noise background (z-order: grid → backdrop → gradient → content)
		val gridBackgroundView =
			ComposeView(requireContext()).apply {
				layoutParams =
					FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT,
					)
				setContent {
					JellyfinTheme {
						DarkGridNoiseBackground(modifier = Modifier.fillMaxSize())
					}
				}
			}
		mainContainer.addView(gridBackgroundView)

		backdropImage =
			ImageView(requireContext()).apply {
				layoutParams =
					FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT,
					)
				scaleType = ImageView.ScaleType.CENTER_CROP
				alpha = 0.75f
			}
		mainContainer.addView(backdropImage)

		gradientView =
			View(requireContext()).apply {
				layoutParams =
					FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT,
					)
				setBackgroundResource(R.drawable.detail_backdrop_gradient)
			}
		mainContainer.addView(gradientView)

		val contentView =
			ComposeView(requireContext()).apply {
				layoutParams =
					FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT,
						FrameLayout.LayoutParams.MATCH_PARENT,
					)
				setContent {
					JellyfinTheme {
						ScreenIdOverlay(ScreenIds.ITEM_DETAIL_ID, ScreenIds.ITEM_DETAIL_NAME) {
							ItemDetailsContent()
						}
					}
				}
			}
		mainContainer.addView(contentView)

		return mainContainer
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)

		val args = Args.fromBundle(arguments) ?: return

		when {
			// Channel/Program details (Live TV)
			args.channelId != null && args.programInfoJson != null -> {
				val programInfo =
					try {
						Json.Default.decodeFromString<BaseItemDto>(args.programInfoJson)
					} catch (e: Exception) {
						Timber.e(e, "Failed to parse ProgramInfo JSON")
						return
					}
				viewModel.loadChannelProgram(args.channelId, programInfo)
			}
			// Series timer details (Live TV)
			args.seriesTimerJson != null -> {
				val seriesTimer =
					try {
						Json.Default.decodeFromString<SeriesTimerInfoDto>(args.seriesTimerJson)
					} catch (e: Exception) {
						Timber.e(e, "Failed to parse SeriesTimer JSON")
						return
					}
				viewModel.loadSeriesTimer(seriesTimer)
			}
			// Standard item details — skip reload if ViewModel already has this item
			else -> {
				val currentItem = viewModel.uiState.value.item
				if (currentItem == null || currentItem.id != args.itemId) {
					viewModel.loadItem(args.itemId, args.serverId)
				}
			}
		}

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
							val blurAmount = 4
							val imageLoader = coil3.SingletonImageLoader.get(requireContext())
							lifecycleScope.launch {
								val result =
									imageLoader.execute(
										coil3.request.ImageRequest
											.Builder(requireContext())
											.data(backdropUrl)
											.size(1920, 1080)
											.crossfade(400)
											.build(),
									)
								val bitmap = result.image?.toBitmap()
								if (bitmap != null) {
									val useComposeBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
									val finalBitmap: android.graphics.Bitmap =
										if (!useComposeBlur && blurAmount > 0) {
											BitmapBlur.blur(bitmap, blurAmount)
										} else {
											if (useComposeBlur && blurAmount > 0) {
												// On Android 12+, apply RenderEffect blur
												backdropImage?.setRenderEffect(
													android.graphics.RenderEffect.createBlurEffect(
														blurAmount.toFloat(),
														blurAmount.toFloat(),
														android.graphics.Shader.TileMode.CLAMP,
													),
												)
											}
											bitmap
										}
									backdropImage?.setImageBitmap(finalBitmap)
									backdropImage?.alpha = 0.75f
								}
							}
						}
					}
				}
			}.launchIn(lifecycleScope)
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
			val blurAmount = 4

			val onNavigateToItem: (UUID) -> Unit = { id ->
				navigationRepository.navigate(Destinations.itemDetails(id, viewModel.serverId))
			}
			when (item.type) {
				BaseItemKind.PERSON ->
					PersonDetailsContent(
						uiState = uiState,
						contentFocusRequester = contentFocusRequester,
						showBackdrop = true,
						api = api,
						onNavigateToItem = onNavigateToItem,
					)

				BaseItemKind.SEASON -> {
					val actionCallbacks = createActionCallbacks(item, uiState, context)
					SeasonDetailsContent(
						uiState = uiState,
						contentFocusRequester = contentFocusRequester,
						api = api,
						actionCallbacks = actionCallbacks,
						onNavigateToItem = onNavigateToItem,
					)
				}

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

				else ->
					when {
						// Series timer details (detected by presence of seriesTimerInfo)
						uiState.seriesTimerInfo != null ->
							SeriesTimerDetailsContent(
								uiState = uiState,
								contentFocusRequester = contentFocusRequester,
								api = api,
								onCancelSeriesTimer = { viewModel.cancelSeriesTimer() },
								onNavigateToItem = onNavigateToItem,
							)
						// Live TV program details
						item.type == BaseItemKind.PROGRAM ->
							LiveTvDetailsContent(
								uiState = uiState,
								contentFocusRequester = contentFocusRequester,
								api = api,
								onPlay = { play(item, 0, false) },
								onToggleRecord = { viewModel.toggleRecord() },
								onToggleRecordSeries = { viewModel.toggleRecordSeries() },
								onNavigateToItem = onNavigateToItem,
							)
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
	}

	private fun createActionCallbacks(
		item: BaseItemDto,
		uiState: ItemDetailsUiState,
		context: android.content.Context,
	): DetailActionCallbacks =
		DetailActionCallbacks(
			trackSelector = trackSelector,
			hasPlayableTrailers = hasPlayableTrailers(context, item),
			onPlay = { handlePlay(item, uiState) },
			onResume = { handleResume(item) },
			onPlayTrailers = { playTrailers(item) },
			onToggleWatched = { viewModel.toggleWatched() },
			onToggleFavorite = { viewModel.toggleFavorite() },
			onConfirmDelete = { confirmDeleteItem(item) },
			onGoToSeries =
				if (item.type == BaseItemKind.EPISODE && item.seriesId != null) {
					{ item.seriesId?.let { seriesId -> navigationRepository.navigate(Destinations.itemDetails(seriesId, viewModel.serverId)) } }
				} else {
					null
				},
			onLoadItem = { id -> viewModel.loadItem(id) },
		)

	// ---- Playback helpers ----

	private fun play(
		item: BaseItemDto,
		positionMs: Int,
		shuffle: Boolean,
	) {
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
			},
		)
	}

	private fun handlePlay(
		item: BaseItemDto,
		uiState: ItemDetailsUiState,
	) {
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
					val trailerInfo =
						withContext(Dispatchers.IO) {
							TrailerResolver.resolveTrailerFromItem(item)
						}

					if (trailerInfo != null) {
						val segmentsJson =
							trailerInfo.segments.joinToString(",", "[", "]") { seg ->
								"""{"start":${seg.startTime},"end":${seg.endTime},"category":"${seg.category}","action":"${seg.actionType}"}"""
							}
						navigationRepository.navigate(
							Destinations.trailerPlayer(
								videoId = trailerInfo.youtubeVideoId,
								startSeconds = trailerInfo.startSeconds,
								segmentsJson = segmentsJson,
							),
						)
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
					val trailers =
						withContext(Dispatchers.IO) {
							viewModel.effectiveApi.userLibraryApi
								.getLocalTrailers(itemId = item.id)
								.content
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
		android.app.AlertDialog
			.Builder(requireContext())
			.setTitle(R.string.item_delete_confirm_title)
			.setMessage(R.string.item_delete_confirm_message)
			.setNegativeButton(R.string.lbl_no, null)
			.setPositiveButton(R.string.lbl_delete) { _, _ ->
				deleteItem(item)
			}.show()
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
			if (navigationRepository.canGoBack) {
				navigationRepository.goBack()
			} else {
				navigationRepository.navigate(Destinations.home)
			}
			Toast.makeText(requireContext(), getString(R.string.item_deleted, item.name), Toast.LENGTH_LONG).show()
		}
	}
}
