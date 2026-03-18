package org.jellyfin.androidtv.ui.playback

import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.data.compat.PlaybackException
import org.jellyfin.androidtv.data.compat.StreamInfo
import org.jellyfin.androidtv.data.compat.VideoOptions
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.data.syncplay.SyncPlayManager
import org.jellyfin.androidtv.data.syncplay.SyncPlayQueueFetcher
import org.jellyfin.androidtv.data.syncplay.SyncPlayQueueHelper
import org.jellyfin.androidtv.data.syncplay.SyncPlayUtils
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.preference.constant.NextUpBehavior
import org.jellyfin.androidtv.preference.constant.RefreshRateSwitchingBehavior
import org.jellyfin.androidtv.preference.constant.StillWatchingBehavior
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.InteractionTrackerViewModel
import org.jellyfin.androidtv.ui.livetv.TvManager
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.UUIDUtils
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.ReportingHelper
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.profile.createDeviceProfile
import org.jellyfin.androidtv.util.sdk.ApiClientFactory
import org.jellyfin.androidtv.util.sdk.compat.getVideoStream
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.ServerVersion
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.GroupStateType
import org.jellyfin.sdk.model.api.LocationType
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.PlaybackErrorCode
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.serializer.toUUIDOrNull
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

class PlaybackController(
	items: List<BaseItemDto>,
	fragment: androidx.fragment.app.Fragment?,
	startIndex: Int = 0,
	private val playbackManager: PlaybackManager,
	private val userPreferences: UserPreferences,
	private val videoQueueManager: VideoQueueManager,
	private val api: ApiClient,
	private val apiClientFactory: ApiClientFactory,
	private val sessionRepository: SessionRepository,
	private val dataRefreshService: DataRefreshService,
	private val reportingHelper: ReportingHelper,
	private val lazyInteractionTracker: InteractionTrackerViewModel,
	private val trackSelector: PrePlaybackTrackSelector,
	private val syncPlayManager: SyncPlayManager,
	private val userSettingPreferences: UserSettingPreferences,
	private val playbackLauncher: PlaybackLauncher,
	private val applicationContext: android.content.Context,
	private val serverVersionProvider: () -> ServerVersion,
) : PlaybackControllerNotifiable {
	companion object {
		// Frequency to report playback progress
		private val PROGRESS_REPORTING_INTERVAL = TimeUtils.secondsToMillis(3.0)

		// Frequency to report paused state
		private val PROGRESS_REPORTING_PAUSE_INTERVAL = TimeUtils.secondsToMillis(15.0)
	}

	internal var mItems: List<BaseItemDto> = items
	internal var mVideoManager: VideoManager? = null
	internal var mCurrentIndex: Int = 0
	internal var mCurrentPosition: Long = 0L
	private var mPlaybackState = PlaybackState.IDLE

	internal var mCurrentStreamInfo: StreamInfo? = null

	private val interactionTracker: InteractionTrackerViewModel

	var fragment: androidx.fragment.app.Fragment? = fragment
		private set

	private var spinnerOff = false

	internal var mCurrentOptions: VideoOptions? = null
	private var mDefaultAudioIndex = -1
	internal var burningSubs = false
	private var mRequestedPlaybackSpeed = -1.0f

	// Flag to prevent echoing SyncPlay commands back to the group
	private var isRespondingToSyncPlayCommand = false

	private var mReportLoop: Runnable? = null
	private val mHandler: Handler = Handler(Looper.getMainLooper())

	private var mStartPosition = 0L

	// tmp position used when seeking
	private var mSeekPosition = -1L
	private var wasSeeking = false
	private var finishedInitialSeek = false

	private var mCurrentProgramEnd: LocalDateTime? = null
	private var mCurrentProgramStart: LocalDateTime? = null
	private var mCurrentTranscodeStartTime = 0L
	var isLiveTv = false
		private set
	private var directStreamLiveTv = false
	private var playbackRetries = 0
	private var lastPlaybackError = 0L

	private var mDisplayModes: Array<Display.Mode>? = null
	private var refreshRateSwitchingBehavior = RefreshRateSwitchingBehavior.DISABLED

	init {
		mCurrentIndex = 0
		if (startIndex > 0 && startIndex < items.size) {
			mCurrentIndex = startIndex
		}

		interactionTracker = lazyInteractionTracker

		refreshRateSwitchingBehavior = userPreferences[UserPreferences.refreshRateSwitchingBehavior]
		if (refreshRateSwitchingBehavior != RefreshRateSwitchingBehavior.DISABLED) {
			getDisplayModes()
		}

		// Register SyncPlay callback to handle incoming commands
		setupSyncPlayCallback()
	}

	private fun setupSyncPlayCallback() {
		syncPlayManager.playbackCallback =
			object : SyncPlayManager.SyncPlayPlaybackCallback {
				override fun onPlay(positionMs: Long) {
					Timber.i("SyncPlay onPlay callback: positionMs=%d, currentState=%s", positionMs, mPlaybackState)
					if (mPlaybackState == PlaybackState.PAUSED || mPlaybackState == PlaybackState.BUFFERING || mPlaybackState == PlaybackState.IDLE) {
						isRespondingToSyncPlayCommand = true
						// If position is 0 and we're paused, resume from current position instead of seeking to start
						if (positionMs <= 0 && mPlaybackState == PlaybackState.PAUSED && hasInitializedVideoManager()) {
							Timber.i("SyncPlay: Resuming from current position")
							mVideoManager?.play()
							mPlaybackState = PlaybackState.PLAYING
						} else {
							play(positionMs)
						}
						isRespondingToSyncPlayCommand = false
					} else {
						Timber.w("SyncPlay onPlay ignored: state=%s", mPlaybackState)
					}
				}

				override fun onPause(positionMs: Long) {
					Timber.i("SyncPlay onPause callback: positionMs=%d, currentState=%s", positionMs, mPlaybackState)
					if (mPlaybackState == PlaybackState.PLAYING || mPlaybackState == PlaybackState.BUFFERING) {
						isRespondingToSyncPlayCommand = true
						pause()
						isRespondingToSyncPlayCommand = false
					} else {
						Timber.w("SyncPlay onPause ignored: state=%s", mPlaybackState)
					}
				}

				override fun onSeek(positionMs: Long) {
					isRespondingToSyncPlayCommand = true
					seek(positionMs)
					isRespondingToSyncPlayCommand = false
				}

				override fun onStop() {
					stop()
				}

				override fun onLoadQueue(
					itemIds: List<UUID>,
					startIndex: Int,
					startPositionTicks: Long,
				) {
					loadSyncPlayQueue(itemIds, startIndex, startPositionTicks)
				}

				override fun getCurrentPositionMs(): Long = this@PlaybackController.currentPosition

				override fun isPlaying(): Boolean = mPlaybackState == PlaybackState.PLAYING

				override fun setPlaybackSpeed(speed: Float) {
					this@PlaybackController.playbackSpeed = speed
				}

				override fun getPlaybackSpeed(): Float = this@PlaybackController.playbackSpeed
			}
	}

	private fun executeSyncPlayCommand(command: Runnable) {
		if (isRespondingToSyncPlayCommand) return
		Thread(command).start()
	}

	private fun loadSyncPlayQueue(
		itemIds: List<UUID>,
		startIndex: Int,
		startPositionTicks: Long,
	) {
		if (itemIds.isEmpty()) return

		val frag = fragment
		if (frag == null) {
			Timber.i("Fragment is null, launching playback without lifecycle")

			SyncPlayQueueFetcher.fetchQueueAsync(
				itemIds,
				startIndex,
				startPositionTicks,
				object : SyncPlayQueueHelper.QueueCallback {
					override fun onQueueReady(
						items: List<BaseItemDto>,
						actualStartIndex: Int,
						startPositionMs: Long,
					) {
						videoQueueManager.setCurrentVideoQueue(items, null)
						videoQueueManager.setCurrentMediaPosition(actualStartIndex)

						playbackLauncher.launch(applicationContext, items, startPositionMs.toInt(), false, actualStartIndex, false)
					}

					override fun onError() {
						Timber.e("Failed to load SyncPlay queue")
					}
				},
			)
			return
		}

		SyncPlayQueueHelper.fetchQueue(
			frag,
			itemIds,
			startIndex,
			startPositionTicks,
			object : SyncPlayQueueHelper.QueueCallback {
				override fun onQueueReady(
					items: List<BaseItemDto>,
					actualStartIndex: Int,
					startPositionMs: Long,
				) {
					if (fragment == null || fragment?.activity == null) return

					mItems = items
					mCurrentIndex = actualStartIndex
					mStartPosition = startPositionMs
					videoQueueManager.setCurrentVideoQueue(items, null)
					videoQueueManager.setCurrentMediaPosition(actualStartIndex)

					// Play the first item to initialize playback
					// The server will send commands to control playback state
					val firstItem = items[actualStartIndex]
					val options = buildExoPlayerOptions(null, null, firstItem)
					playInternal(firstItem, mStartPosition, options)
				}

				override fun onError() {
					Timber.e("Failed to load SyncPlay queue")
				}
			},
		)
	}

	fun hasFragment(): Boolean = fragment != null

	fun init(
		mgr: VideoManager,
		fragment: androidx.fragment.app.Fragment,
	) {
		mVideoManager = mgr
		mVideoManager?.subscribe(this)
		mVideoManager?.setZoom(userPreferences[UserPreferences.playerZoomMode])
		this.fragment = fragment
		directStreamLiveTv = userPreferences[UserPreferences.liveTvDirectPlayEnabled]
	}

	fun setItems(items: List<BaseItemDto>) {
		mItems = items
		mCurrentIndex = 0
	}

	var playbackSpeed: Float
		get() =
			if (hasInitializedVideoManager()) {
				// Actually poll the video manager, since exoplayer can revert back
				// to 1x if it can't go faster, so we should check directly
				mVideoManager!!.playbackSpeed
			} else {
				mRequestedPlaybackSpeed
			}
		set(speed) {
			mRequestedPlaybackSpeed = speed
			if (hasInitializedVideoManager()) {
				mVideoManager?.playbackSpeed = speed
			}
		}

	fun setAudioDelay(delayMs: Long) {
		if (hasInitializedVideoManager()) {
			mVideoManager?.setAudioDelay(delayMs)
		}
	}

	val currentlyPlayingItem: BaseItemDto?
		get() = if (mItems.size > mCurrentIndex) mItems[mCurrentIndex] else null

	fun hasInitializedVideoManager(): Boolean = mVideoManager != null && mVideoManager!!.isInitialized()

	val videoManager: VideoManager?
		get() = mVideoManager

	val currentMediaSource: MediaSourceInfo?
		get() {
			if (mCurrentStreamInfo?.mediaSource != null) {
				return mCurrentStreamInfo!!.mediaSource
			}
			val item = currentlyPlayingItem ?: return null
			val mediaSources = item.mediaSources
			if (mediaSources.isNullOrEmpty()) {
				return null
			}
			// Prefer the media source with the same id as the item
			for (mediaSource in mediaSources) {
				if (mediaSource.id?.toUUIDOrNull() == item.id) {
					return mediaSource
				}
			}
			// Or fallback to the first media source if none match
			return mediaSources[0]
		}

	val currentStreamInfo: StreamInfo?
		get() = mCurrentStreamInfo

	fun canSeek(): Boolean = !isLiveTv || !directStreamLiveTv

	val subtitleStreamIndex: Int
		get() = mCurrentOptions?.subtitleStreamIndex ?: -1

	val isTranscoding: Boolean
		get() {
			// use or here so that true is the default since
			// this method is used to exclude features that may break unless we are sure playback is direct
			return mCurrentStreamInfo == null || mCurrentStreamInfo?.playMethod == PlayMethod.TRANSCODE
		}

	/**
	 * Check if subtitles are being burned/encoded into the video stream.
	 * When true, subtitle delay cannot be applied because the subtitles
	 * are part of the video frames, not separate text tracks.
	 */
	val isBurningSubtitles: Boolean
		get() = burningSubs

	fun hasNextItem(): Boolean = mItems.size > mCurrentIndex + 1

	val nextItem: BaseItemDto?
		get() = if (hasNextItem()) mItems[mCurrentIndex + 1] else null

	fun hasPreviousItem(): Boolean = mCurrentIndex - 1 >= 0

	val isPlaying: Boolean
		get() {
			// since playbackController is so closely tied to videoManager, check if it is playing too since they can fall out of sync
			return mPlaybackState == PlaybackState.PLAYING && hasInitializedVideoManager() && mVideoManager!!.isPlaying()
		}

	fun playerErrorEncountered() {
		// reset the retry count if it's been more than 30s since previous error
		if (playbackRetries > 0 && Instant.now().toEpochMilli() - lastPlaybackError > 30000) {
			Timber.i("playback stabilized - retry count reset to 0 from %s", playbackRetries)
			playbackRetries = 0
		}

		playbackRetries++
		lastPlaybackError = Instant.now().toEpochMilli()

		if (playbackRetries < 3) {
			fragment?.let { frag ->
				Toast.makeText(frag.context, frag.getString(R.string.player_error), Toast.LENGTH_LONG).show()
			}
			Timber.i("Player error encountered - retrying")
			stop()
			play(mCurrentPosition)
		} else {
			mPlaybackState = PlaybackState.ERROR
			fragment?.let { frag ->
				Toast.makeText(frag.context, frag.getString(R.string.too_many_errors), Toast.LENGTH_LONG).show()
			}
		}
	}

	private fun getDisplayModes() {
		val frag = fragment ?: return
		val display = frag.requireActivity().windowManager.defaultDisplay
		mDisplayModes = display.supportedModes
		Timber.i("** Available display refresh rates:")
		mDisplayModes?.forEach { mode ->
			Timber.i(
				"display mode %s - %dx%d@%f",
				mode.modeId,
				mode.physicalWidth,
				mode.physicalHeight,
				mode.refreshRate,
			)
		}
	}

	private fun findBestDisplayMode(videoStream: org.jellyfin.sdk.model.api.MediaStream): Display.Mode? {
		val frag = fragment ?: return null
		val displayModes = mDisplayModes ?: return null
		val realFrameRate = videoStream.realFrameRate ?: return null

		var curWeight = 0
		var bestMode: Display.Mode? = null
		val sourceRate = (realFrameRate * 100).roundToInt()

		val defaultMode =
			frag
				.requireActivity()
				.windowManager.defaultDisplay.mode

		Timber.d("trying to find display mode for video: %dx%d@%f", videoStream.width, videoStream.height, realFrameRate)
		for (mode in displayModes) {
			Timber.d(
				"considering display mode: %s - %dx%d@%f",
				mode.modeId,
				mode.physicalWidth,
				mode.physicalHeight,
				mode.refreshRate,
			)

			// Skip unwanted display modes
			if (mode.physicalWidth < 1280 || mode.physicalHeight < 720) continue // Skip non-HD
			// Disallow resolution downgrade
			if (mode.physicalWidth < (videoStream.width ?: 0) || mode.physicalHeight < (videoStream.height ?: 0)) continue

			val rate = (mode.refreshRate * 100).roundToInt()
			if (rate != sourceRate && rate != sourceRate * 2 && rate != (sourceRate * 2.5).roundToInt()) continue // Skip inappropriate rates

			Timber.i(
				"qualifying display mode: %s - %dx%d@%f",
				mode.modeId,
				mode.physicalWidth,
				mode.physicalHeight,
				mode.refreshRate,
			)

			// if scaling on-device, keep native resolution modes at diff 0 (best score)
			// for other resolutions when scaling on device, or if scaling on tv, score based on distance from media resolution

			// use -1 as the default so, with SCALE_ON_DEVICE, a mode at native resolution will rank higher than
			// a mode with equal refresh rate and the same resolution as the media
			var resolutionDifference = -1
			if ((
					refreshRateSwitchingBehavior == RefreshRateSwitchingBehavior.SCALE_ON_DEVICE &&
						!(mode.physicalWidth == defaultMode.physicalWidth && mode.physicalHeight == defaultMode.physicalHeight)
				) ||
				refreshRateSwitchingBehavior == RefreshRateSwitchingBehavior.SCALE_ON_TV
			) {
				resolutionDifference = abs(mode.physicalWidth - (videoStream.width ?: 0))
			}
			val refreshRateDifference = rate - sourceRate

			// use 100,000 to account for refresh rates 120Hz+ (at 120Hz rate == 12,000)
			val weight = 100000 - refreshRateDifference + 100000 - resolutionDifference

			if (weight > curWeight) {
				Timber.d(
					"preferring mode: %s - %dx%d@%f",
					mode.modeId,
					mode.physicalWidth,
					mode.physicalHeight,
					mode.refreshRate,
				)
				curWeight = weight
				bestMode = mode
			}
		}

		return bestMode
	}

	private fun setRefreshRate(videoStream: org.jellyfin.sdk.model.api.MediaStream?) {
		if (videoStream == null || fragment == null) {
			Timber.e("Null video stream attempting to set refresh rate")
			return
		}

		val frag = fragment!!
		val current =
			frag
				.requireActivity()
				.windowManager.defaultDisplay.mode
		val best = findBestDisplayMode(videoStream)
		if (best != null) {
			Timber.i(
				"*** Best refresh mode is: %s - %dx%d/%f",
				best.modeId,
				best.physicalWidth,
				best.physicalHeight,
				best.refreshRate,
			)
			if (current.modeId != best.modeId) {
				Timber.i(
					"*** Attempting to change refresh rate from: %s - %dx%d@%f",
					current.modeId,
					current.physicalWidth,
					current.physicalHeight,
					current.refreshRate,
				)
				val params: WindowManager.LayoutParams = frag.requireActivity().window.attributes
				params.preferredDisplayModeId = best.modeId
				frag.requireActivity().window.attributes = params
			} else {
				Timber.i("Display is already in best mode")
			}
		} else {
			Timber.i("*** Unable to find display mode for refresh rate: %f", videoStream.realFrameRate)
		}
	}

	// central place to update mCurrentPosition
	private fun refreshCurrentPosition() {
		var newPos = -1L

		if (isLiveTv && mCurrentProgramStart != null) {
			newPos = getRealTimeProgress()
			// live tv
		} else if (hasInitializedVideoManager()) {
			if (currentSkipPos != 0L || (!isPlaying && mSeekPosition != -1L)) {
				newPos = mSeekPosition
				// use seekPosition until playback starts
			} else if (isPlaying) {
				if (finishedInitialSeek) {
					// playback has started following initial seek for direct play and hls
					// get current position and reset seekPosition
					newPos = mVideoManager!!.getCurrentPosition()
					mSeekPosition = -1
				} else if (wasSeeking) {
					// the initial seek for direct play and hls completed
					finishedInitialSeek = true
				} else if (mSeekPosition != -1L) {
					// the initial seek for direct play and hls hasn't happened yet
					newPos = mSeekPosition
				}
				wasSeeking = false
			}
		}
		// use original value if new one isn't available
		mCurrentPosition = if (newPos != -1L) newPos else mCurrentPosition
	}

	fun play(position: Long) {
		play(position, null)
	}

	internal fun play(
		position: Long,
		forcedSubtitleIndex: Int?,
	) {
		var pos = position
		val forcedAudioLanguage = videoQueueManager.getLastPlayedAudioLanguageIsoCode()
		Timber.i(
			"Play called from state: %s with pos: %d, sub index: %d and forced audio: %s",
			mPlaybackState,
			pos,
			forcedSubtitleIndex,
			forcedAudioLanguage,
		)

		val frag = fragment
		if (frag == null) {
			Timber.w("mFragment is null, returning")
			return
		}

		if (pos < 0) {
			Timber.i("Negative start requested - adjusting to zero")
			pos = 0
		}

		when (mPlaybackState) {
			PlaybackState.PLAYING -> {
				// do nothing
			}
			PlaybackState.SEEKING -> {
				if (!hasInitializedVideoManager()) return
				mVideoManager?.play()
				mPlaybackState = PlaybackState.PLAYING
				startReportLoop()
			}
			PlaybackState.PAUSED -> {
				if (!hasInitializedVideoManager()) return
				// Apply unpause rewind if configured
				val prefs = userSettingPreferences
				val unpauseRewindMs = prefs[UserSettingPreferences.unpauseRewindDuration]
				if (unpauseRewindMs > 0) {
					val rewindPosition = (currentPosition - unpauseRewindMs).coerceAtLeast(0)
					if (isTranscoding) {
						// For transcoded content, only seek if within buffer to avoid server restart
						Timber.i("Attempting buffer seek for transcoded content, rewind %d ms", unpauseRewindMs)
						mVideoManager?.seekWithinBuffer(rewindPosition)
					} else {
						// For direct play, always seek
						Timber.i("Rewinding %d ms on unpause, seeking to: %d", unpauseRewindMs, rewindPosition)
						mVideoManager?.seekTo(rewindPosition)
					}
				}
				// resume playback
				mVideoManager?.play()
				mPlaybackState = PlaybackState.PLAYING // won't get another onPrepared call
				startReportLoop()
			}
			PlaybackState.BUFFERING -> {
				// onPrepared should take care of it
			}
			PlaybackState.IDLE -> {
				// start new playback

				// set mSeekPosition so the seekbar will not default to 0:00
				mSeekPosition = pos
				mCurrentPosition = 0

				val item = currentlyPlayingItem

				if (item == null) {
					Timber.w("item is null - aborting play")
					Toast.makeText(frag.context, frag.getString(R.string.msg_cannot_play), Toast.LENGTH_LONG).show()
					// no-op
					return
				}

				// make sure item isn't missing
				if (item.locationType == LocationType.VIRTUAL) {
					if (hasNextItem()) {
						AlertDialog
							.Builder(frag.context)
							.setTitle(R.string.episode_missing)
							.setMessage(R.string.episode_missing_message)
							.setPositiveButton(R.string.lbl_yes) { _, _ -> next() }
							.setNegativeButton(R.string.lbl_no) { _, _ -> /* no-op */ }
							.create()
							.show()
					} else {
						AlertDialog
							.Builder(frag.context)
							.setTitle(R.string.episode_missing)
							.setMessage(R.string.episode_missing_message_2)
							.setPositiveButton(R.string.lbl_ok) { _, _ -> /* no-op */ }
							.create()
							.show()
					}
					return
				}

				isLiveTv = item.type == BaseItemKind.TV_CHANNEL
				startSpinner()

				// undo setting mSeekPosition for liveTV
				if (isLiveTv) mSeekPosition = -1

				val internalOptions = buildExoPlayerOptions(forcedSubtitleIndex, forcedAudioLanguage, item)

				playInternal(currentlyPlayingItem!!, pos, internalOptions)
				mPlaybackState = PlaybackState.BUFFERING

				val itemDuration = if (currentlyPlayingItem?.runTimeTicks != null) currentlyPlayingItem!!.runTimeTicks!! / 10000 else -1L
				mVideoManager?.setMetaDuration(itemDuration)
			}
			else -> {
				// UNDEFINED, ERROR - do nothing
			}
		}
	}

	private fun buildExoPlayerOptions(
		forcedSubtitleIndex: Int?,
		forcedAudioLanguage: String?,
		item: BaseItemDto,
	): VideoOptions {
		val internalOptions = VideoOptions()
		internalOptions.itemId = item.id
		internalOptions.mediaSources = item.mediaSources

		// Extract serverId for multi-server support
		// Only set serverId when the item is from a different server than the current session.
		// This avoids creating a separate API client (with a different device ID) for same-server
		// items, which can cause transcoding session mismatches on the server.
		val parsedServerId = UUIDUtils.parseUUID(item.serverId)
		val currentServerId = sessionRepository.currentSession.value?.serverId
		if (parsedServerId != null && parsedServerId != currentServerId) {
			internalOptions.serverId = parsedServerId
			Timber.i("PlaybackController: Using cross-server API for playback: item server %s, current server %s", parsedServerId, currentServerId)
		}

		if (isLiveTv) {
			// Live TV playback strategy:
			// - DirectPlay (raw URL, no server processing) only if user enabled it
			// - DirectStream (remux without transcoding) always stays enabled as fallback
			// - Full transcoding only as last resort (playbackRetries > 1)
			if (!directStreamLiveTv || playbackRetries > 0) internalOptions.enableDirectPlay = false
			if (playbackRetries > 1) internalOptions.enableDirectStream = false
		} else {
			if (playbackRetries > 0) internalOptions.enableDirectPlay = false
			if (playbackRetries > 1) internalOptions.enableDirectStream = false
		}

		// Check for pre-selected tracks from the details screen
		val itemIdString =
			item.id
				.toString()
				.toUUIDOrNull()
				.toString()
		val preSelectedAudio = trackSelector.getSelectedAudioTrack(itemIdString)
		val preSelectedSubtitle = trackSelector.getSelectedSubtitleTrack(itemIdString)

		if (mCurrentOptions != null) {
			internalOptions.subtitleStreamIndex = mCurrentOptions!!.subtitleStreamIndex
			internalOptions.audioStreamIndex = mCurrentOptions!!.audioStreamIndex
		}

		if (preSelectedAudio != null && preSelectedAudio >= 0) {
			Timber.i("Applying pre-selected audio track: %d", preSelectedAudio)
			internalOptions.audioStreamIndex = preSelectedAudio
		} else if (forcedAudioLanguage != null) {
			val source = currentMediaSource
			if (source?.mediaStreams != null) {
				for (stream in source.mediaStreams!!) {
					if (stream.type == MediaStreamType.AUDIO && forcedAudioLanguage == stream.language) {
						internalOptions.audioStreamIndex = stream.index
						break
					}
				}
			}
		}

		if (preSelectedSubtitle != null) {
			Timber.i("Applying pre-selected subtitle track: %d", preSelectedSubtitle)
			internalOptions.subtitleStreamIndex = preSelectedSubtitle
		} else if (forcedSubtitleIndex != null) {
			internalOptions.subtitleStreamIndex = forcedSubtitleIndex
		}

		if (preSelectedAudio != null || preSelectedSubtitle != null) {
			Timber.i("Clearing track pre-selections for next playback")
			trackSelector.clearSelections()
		}

		val source = currentMediaSource
		if (!isLiveTv && source != null) {
			internalOptions.mediaSourceId = source.id
		}
		val internalProfile =
			createDeviceProfile(
				fragment!!.requireContext(),
				userPreferences,
				serverVersionProvider(),
			)
		internalOptions.profile = internalProfile
		return internalOptions
	}

	private fun playInternal(
		item: BaseItemDto,
		position: Long,
		internalOptions: VideoOptions,
	) {
		val frag = fragment ?: return
		if (isLiveTv) {
			updateTvProgramInfo()
			TvManager.setLastLiveTvChannel(item.id)
			// internal/exo player
			Timber.i("Using internal player for Live TV")
			playbackManager.getVideoStreamInfo(
				frag,
				internalOptions,
				position * 10000,
				object : Response<StreamInfo>(frag.lifecycle) {
					override fun onResponse(response: StreamInfo) {
						if (!isActive) return
						if (mVideoManager == null) return
						mCurrentOptions = internalOptions
						startItem(item, position, response)
					}

					override fun onError(exception: Exception) {
						if (!isActive) return
						handlePlaybackInfoError(exception)
					}
				},
			)
		} else {
			playbackManager.getVideoStreamInfo(
				frag,
				internalOptions,
				position * 10000,
				object : Response<StreamInfo>(frag.lifecycle) {
					override fun onResponse(response: StreamInfo) {
						if (!isActive) return
						Timber.i("Internal player would %s", if (response.playMethod == PlayMethod.TRANSCODE) "transcode" else "direct stream")
						if (mVideoManager == null) return
						mCurrentOptions = internalOptions
						if (internalOptions.subtitleStreamIndex == null) burningSubs = response.subtitleDeliveryMethod == SubtitleDeliveryMethod.ENCODE
						startItem(item, position, response)
					}

					override fun onError(exception: Exception) {
						if (!isActive) return
						Timber.e(exception, "Unable to get stream info for internal player")
						if (mVideoManager == null) return
					}
				},
			)
		}
	}

	private fun handlePlaybackInfoError(exception: Exception?) {
		Timber.e(exception, "Error getting playback stream info")
		val frag = fragment ?: return
		if (exception is PlaybackException) {
			when (exception.errorCode) {
				PlaybackErrorCode.NOT_ALLOWED ->
					Toast.makeText(frag.context, frag.getString(R.string.msg_playback_not_allowed), Toast.LENGTH_LONG).show()
				PlaybackErrorCode.NO_COMPATIBLE_STREAM ->
					Toast.makeText(frag.context, frag.getString(R.string.msg_playback_incompatible), Toast.LENGTH_LONG).show()
				PlaybackErrorCode.RATE_LIMIT_EXCEEDED ->
					Toast.makeText(frag.context, frag.getString(R.string.msg_playback_restricted), Toast.LENGTH_LONG).show()
			}
		} else {
			Toast.makeText(frag.context, frag.getString(R.string.msg_cannot_play), Toast.LENGTH_LONG).show()
		}
	}

	private fun startItem(
		item: BaseItemDto,
		position: Long,
		response: StreamInfo,
	) {
		if (!hasInitializedVideoManager() || !hasFragment()) {
			Timber.w(
				"Error - attempting to play without:%s%s",
				if (hasInitializedVideoManager()) "" else " [videoManager]",
				if (hasFragment()) "" else " [overlay fragment]",
			)
			return
		}

		val preSelectedAudioIndex = mCurrentOptions?.audioStreamIndex
		mCurrentOptions?.audioStreamIndex = null

		mStartPosition = position
		mCurrentStreamInfo = response
		mCurrentOptions?.mediaSourceId = response.mediaSource?.id

		if (response.mediaUrl == null) {
			// If baking subtitles doesn't work (e.g. no permissions to transcode), disable them
			if (response.subtitleDeliveryMethod == SubtitleDeliveryMethod.ENCODE &&
				(response.mediaSource?.defaultSubtitleStreamIndex == null || response.mediaSource?.defaultSubtitleStreamIndex != -1)
			) {
				burningSubs = false
				stop()
				play(position, -1)
			} else {
				handlePlaybackInfoError(null)
			}
			return
		}

		// get subtitle info
		val preSelectedSubtitleIndex = mCurrentOptions?.subtitleStreamIndex
		val subtitlesDefaultToNone = userPreferences[UserPreferences.subtitlesDefaultToNone]
		if (preSelectedSubtitleIndex != null) {
			mCurrentOptions?.subtitleStreamIndex = preSelectedSubtitleIndex
			Timber.i("Using pre-selected subtitle index: %s", preSelectedSubtitleIndex)
		} else if (subtitlesDefaultToNone) {
			mCurrentOptions?.subtitleStreamIndex = -1
			Timber.i("default sub index set to -1 (None) - server default was %s", response.mediaSource?.defaultSubtitleStreamIndex)
		} else {
			mCurrentOptions?.subtitleStreamIndex = response.mediaSource?.defaultSubtitleStreamIndex
			Timber.i(
				"default sub index set to %s remote default %s",
				mCurrentOptions?.subtitleStreamIndex,
				response.mediaSource?.defaultSubtitleStreamIndex,
			)
		}
		setDefaultAudioIndex(response)
		if (preSelectedAudioIndex != null) {
			mCurrentOptions?.audioStreamIndex = preSelectedAudioIndex
			Timber.i("Restoring pre-selected audio index: %s (default was %s)", preSelectedAudioIndex, mDefaultAudioIndex)
		}
		Timber.i("default audio index set to %s remote default %s", mDefaultAudioIndex, response.mediaSource?.defaultAudioStreamIndex)

		val mbPos = position * 10000

		// set refresh rate
		if (refreshRateSwitchingBehavior != RefreshRateSwitchingBehavior.DISABLED) {
			setRefreshRate(response.mediaSource?.getVideoStream())
		}

		// set playback speed to user selection, or 1 if we're watching live-tv
		mVideoManager?.playbackSpeed = if (isLiveTv) 1.0f else mRequestedPlaybackSpeed

		val vm = mVideoManager
		if (vm != null) {
			// Get server-specific API client for subtitle URLs with current user context
			var subtitleApi = api
			val serverId = mCurrentOptions?.serverId
			if (serverId != null) {
				val currentUserId = sessionRepository.currentSession.value?.userId
				val serverApi =
					if (currentUserId != null) {
						apiClientFactory.getApiClient(serverId, currentUserId)
					} else {
						apiClientFactory.getApiClientForServer(serverId)
					}
				if (serverApi != null) {
					subtitleApi = serverApi
					Timber.d("PlaybackController: Using server-specific API for subtitles: %s user: %s", serverId, currentUserId)
				}
			}
			vm.setMediaStreamInfo(subtitleApi, response)
		}

		applyMediaSegments(item) {
			// Set video start delay
			val videoStartDelay = userPreferences[UserPreferences.videoStartDelay]
			if (videoStartDelay > 0) {
				mHandler.postDelayed({
					mVideoManager?.start()
				}, videoStartDelay)
			} else {
				mVideoManager?.start()
			}

			dataRefreshService.lastPlayedItem = item
			reportingHelper.reportStart(fragment!!, this@PlaybackController, item, response, mbPos, false)
		}
	}

	fun startSpinner() {
		spinnerOff = false
	}

	fun stopSpinner() {
		spinnerOff = true
	}

	val audioStreamIndex: Int
		get() {
			var currIndex = -1

			// Use stream index from mCurrentOptions if it's set.
			// This should be null until the player has been queried at least once after playback starts
			//
			// Use DefaultAudioStreamIndex for transcoding since they are encoded with only one stream
			//
			// Otherwise, query the players
			if (mCurrentOptions?.audioStreamIndex != null) {
				currIndex = mCurrentOptions!!.audioStreamIndex!!
			} else if (isTranscoding && currentMediaSource?.defaultAudioStreamIndex != null) {
				currIndex = currentMediaSource!!.defaultAudioStreamIndex!!
			} else if (hasInitializedVideoManager() && !isTranscoding) {
				currIndex = mVideoManager!!.getExoPlayerTrack(MediaStreamType.AUDIO, currentlyPlayingItem?.mediaStreams)
			}
			return currIndex
		}

	private fun bestGuessAudioTrack(info: MediaSourceInfo?): Int? {
		if (info == null) return null

		var videoFound = false
		for (track in info.mediaStreams.orEmpty()) {
			if (track.type == MediaStreamType.VIDEO) {
				videoFound = true
			} else {
				if (videoFound && track.type == MediaStreamType.AUDIO) return track.index
			}
		}
		return null
	}

	private fun lastChosenLanguageAudioTrack(info: MediaSourceInfo?): Int? {
		if (info == null) return null

		var videoFound = false
		for (track in info.mediaStreams.orEmpty()) {
			if (track.type == MediaStreamType.VIDEO) {
				videoFound = true
			} else {
				if (videoFound &&
					track.type == MediaStreamType.AUDIO &&
					track.language != null &&
					track.language == videoQueueManager.getLastPlayedAudioLanguageIsoCode()
				) {
					return track.index
				}
			}
		}
		return null
	}

	private fun setDefaultAudioIndex(info: StreamInfo) {
		if (mDefaultAudioIndex != -1) return

		val lastChosenLanguage = lastChosenLanguageAudioTrack(info.mediaSource)
		val remoteDefault = info.mediaSource?.defaultAudioStreamIndex
		val bestGuess = bestGuessAudioTrack(info.mediaSource)

		if (lastChosenLanguage != null) {
			mDefaultAudioIndex = lastChosenLanguage
		} else if (remoteDefault != null) {
			mDefaultAudioIndex = remoteDefault
		} else if (bestGuess != null) {
			mDefaultAudioIndex = bestGuess
		}
	}

	fun switchAudioStream(index: Int) {
		if (!(isPlaying || isPaused) || index < 0) return

		val source = currentMediaSource
		if (source?.mediaStreams == null || index >= source.mediaStreams!!.size) return

		val lastAudioIsoCode = videoQueueManager.getLastPlayedAudioLanguageIsoCode()
		val currentAudioIsoCode = source.mediaStreams!![index].language

		if (currentAudioIsoCode != null && (lastAudioIsoCode == null || lastAudioIsoCode != currentAudioIsoCode)) {
			videoQueueManager.setLastPlayedAudioLanguageIsoCode(currentAudioIsoCode)
		}

		val currAudioIndex = audioStreamIndex
		Timber.i("trying to switch audio stream from %s to %s", currAudioIndex, index)
		if (currAudioIndex == index) {
			if (mCurrentOptions?.audioStreamIndex == null || mCurrentOptions?.audioStreamIndex != index) {
				Timber.i("setting mCurrentOptions audio stream index from %s to %s", mCurrentOptions?.audioStreamIndex, index)
				mCurrentOptions?.audioStreamIndex = index
			}
			return
		}

		// get current timestamp first
		refreshCurrentPosition()

		if (!isTranscoding && mVideoManager!!.setExoPlayerTrack(index, MediaStreamType.AUDIO, source.mediaStreams)) {
			mCurrentOptions?.mediaSourceId = source.id
			mCurrentOptions?.audioStreamIndex = index
		} else {
			startSpinner()
			mCurrentOptions?.mediaSourceId = source.id
			mCurrentOptions?.audioStreamIndex = index
			stop()
			playInternal(currentlyPlayingItem!!, mCurrentPosition, mCurrentOptions!!)
			mPlaybackState = PlaybackState.BUFFERING
		}
	}

	fun pause() {
		Timber.i("pause called at %s", mCurrentPosition)
		// if playback is paused and the seekbar is scrubbed, it will call pause even if already paused
		if (mPlaybackState == PlaybackState.PAUSED) {
			return
		}
		mPlaybackState = PlaybackState.PAUSED
		if (hasInitializedVideoManager()) mVideoManager?.pause()

		stopReportLoop()
		startPauseReportLoop()

		val manager = syncPlayManager
		val state = manager.state.value
		if (state.groupInfo != null && state.groupState == GroupStateType.PLAYING) {
			executeSyncPlayCommand {
				try {
					kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
						manager.requestPause()
					}
				} catch (e: Exception) {
					Timber.e(e, "Failed to sync pause with SyncPlay")
				}
			}
		}
	}

	fun playPause() {
		when (mPlaybackState) {
			PlaybackState.PLAYING -> pause()
			PlaybackState.PAUSED, PlaybackState.IDLE -> {
				stopReportLoop()
				play(currentPosition)

				val manager = syncPlayManager
				val state = manager.state.value
				if (state.groupInfo != null && state.groupState == GroupStateType.PAUSED) {
					executeSyncPlayCommand {
						try {
							kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
								manager.requestPlay()
							}
						} catch (e: Exception) {
							Timber.e(e, "Failed to sync play with SyncPlay")
						}
					}
				}
			}
			else -> { /* do nothing */ }
		}
	}

	fun stop() {
		refreshCurrentPosition()
		Timber.i("stop called at %s", mCurrentPosition)
		stopReportLoop()
		if (mPlaybackState != PlaybackState.IDLE && mPlaybackState != PlaybackState.UNDEFINED) {
			mPlaybackState = PlaybackState.IDLE

			if (mVideoManager != null && mVideoManager!!.isPlaying()) mVideoManager?.stopPlayback()
			val item = currentlyPlayingItem
			val streamInfo = mCurrentStreamInfo
			if (item != null && streamInfo != null) {
				val mbPos = mCurrentPosition * 10000
				val frag = fragment
				if (frag != null) {
					reportingHelper.reportStopped(frag, item, streamInfo, mbPos)
				}
			}
			clearPlaybackSessionOptions()
		}
	}

	fun refreshStream() {
		// get current timestamp first
		refreshCurrentPosition()

		stop()
		play(mCurrentPosition)
	}

	fun endPlayback(closeActivity: Boolean = false) {
		stop()
		mVideoManager?.destroy()
		fragment = null
		mVideoManager = null
		resetPlayerErrors()
	}

	fun onResume() {
		syncPlayManager.onAppResume()
	}

	fun onPause() {
		syncPlayManager.onAppPause()
	}

	private fun resetPlayerErrors() {
		playbackRetries = 0
	}

	private fun clearPlaybackSessionOptions() {
		mDefaultAudioIndex = -1
		mSeekPosition = -1
		finishedInitialSeek = false
		wasSeeking = false
		burningSubs = false
		mCurrentStreamInfo = null
	}

	fun next() {
		if (mCurrentIndex < mItems.size - 1) {
			stop()
			resetPlayerErrors()
			mCurrentIndex++
			videoQueueManager.setCurrentMediaPosition(mCurrentIndex)
			Timber.i("Moving to index: %d out of %d total items.", mCurrentIndex, mItems.size)
			spinnerOff = false
			play(0)
		}
	}

	fun prev() {
		if (mCurrentIndex > 0 && mItems.isNotEmpty()) {
			stop()
			resetPlayerErrors()
			mCurrentIndex--
			videoQueueManager.setCurrentMediaPosition(mCurrentIndex)
			Timber.i("Moving to index: %d out of %d total items.", mCurrentIndex, mItems.size)
			spinnerOff = false
			play(0)
		}
	}

	fun fastForward() {
		val prefs = userSettingPreferences
		skip(prefs[UserSettingPreferences.skipForwardLength])
	}

	fun rewind() {
		val prefs = userSettingPreferences
		skip(-prefs[UserSettingPreferences.skipBackLength])
	}

	fun seek(pos: Long) {
		seek(pos, false)
	}

	fun seek(
		pos: Long,
		skipToNext: Boolean,
	) {
		var position = pos.coerceAtLeast(0)

		Timber.i("Trying to seek from %s to %d", mCurrentPosition, position)
		Timber.d("Container: %s", mCurrentStreamInfo?.container ?: "unknown")

		if (!hasInitializedVideoManager()) return

		if (wasSeeking) {
			if (isPaused) {
				refreshCurrentPosition()
				play(mCurrentPosition)
			}
			return
		}
		wasSeeking = true

		// If in SyncPlay group and not responding to a SyncPlay command, send seek request to server
		val manager = syncPlayManager
		if (manager.state.value.groupInfo != null && !isRespondingToSyncPlayCommand) {
			Timber.i("SyncPlay: Sending seek request to server for position %d", position)
			val positionTicks = SyncPlayUtils.msToTicks(position)
			executeSyncPlayCommand {
				try {
					kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
						manager.requestSeek(positionTicks)
					}
				} catch (e: Exception) {
					Timber.e(e, "Failed to sync seek with SyncPlay")
				}
			}
		}

		// Stop playback when the requested seek position is at the end of the video
		if (skipToNext && position >= (duration - 100)) {
			// Since we've skipped ahead, set the current position so the PlaybackStopInfo will report the correct end time
			mCurrentPosition = duration
			// Make sure we also set the seek positions so mCurrentPosition won't get overwritten in refreshCurrentPosition()
			currentSkipPos = mCurrentPosition
			mSeekPosition = mCurrentPosition
			// Finalize item playback
			itemComplete()
			return
		}

		if (position >= duration) position = duration

		// set seekPosition so real position isn't used until playback starts again
		mSeekPosition = position

		if (mCurrentStreamInfo == null) return

		val frag = fragment

		// rebuild the stream
		// if an older device uses exoplayer to play a transcoded stream but falls back to the generic http stream instead of hls, rebuild the stream
		if (!mVideoManager!!.isSeekable()) {
			Timber.d("Seek method - rebuilding the stream")
			// mkv transcodes require re-start of stream for seek
			mVideoManager?.stopPlayback()
			mPlaybackState = PlaybackState.BUFFERING

			if (frag != null) {
				playbackManager.changeVideoStream(
					frag,
					mCurrentStreamInfo!!,
					mCurrentOptions!!,
					position * 10000,
					object : Response<StreamInfo>(frag.lifecycle) {
						override fun onResponse(response: StreamInfo) {
							if (!isActive) return
							mCurrentStreamInfo = response
							if (mVideoManager != null) {
								// Get server-specific API client for subtitle URLs with current user context
								var subtitleApi = api
								val serverId = mCurrentOptions?.serverId
								if (serverId != null) {
									val currentUserId = sessionRepository.currentSession.value?.userId
									val serverApi =
										if (currentUserId != null) {
											apiClientFactory.getApiClient(serverId, currentUserId)
										} else {
											apiClientFactory.getApiClientForServer(serverId)
										}
									if (serverApi != null) {
										subtitleApi = serverApi
										Timber.d("PlaybackController: Using server-specific API for subtitles (changeVideoStream): %s user: %s", serverId, currentUserId)
									}
								}
								mVideoManager?.setMediaStreamInfo(subtitleApi, response)
								mVideoManager?.start()
							}
							wasSeeking = false
						}

						override fun onError(exception: Exception) {
							if (!isActive) return
							wasSeeking = false
							if (fragment != null) {
								Toast.makeText(fragment!!.requireContext(), R.string.msg_video_playback_error, Toast.LENGTH_LONG).show()
							}
							Timber.e(exception, "Error trying to seek transcoded stream")
							// call stop so playback can be retried by the user
							stop()
						}
					},
				)
			}
		} else {
			val shouldResumeAfterSeek = mPlaybackState == PlaybackState.PLAYING
			mPlaybackState = PlaybackState.SEEKING
			if (mVideoManager!!.seekTo(position) < 0) {
				wasSeeking = false
				if (frag != null) {
					Toast.makeText(frag.context, frag.getString(R.string.seek_error), Toast.LENGTH_LONG).show()
				}
				pause()
			} else {
				wasSeeking = false
				if (isRespondingToSyncPlayCommand) {
					mPlaybackState = PlaybackState.PAUSED
				} else if (shouldResumeAfterSeek) {
					mVideoManager?.play()
					mPlaybackState = PlaybackState.PLAYING
					startReportLoop()
				} else {
					mPlaybackState = PlaybackState.PAUSED
				}
			}
		}
	}

	internal var currentSkipPos = 0L
		private set

	private val skipRunnable =
		Runnable {
			if (!(isPlaying || isPaused || mPlaybackState == PlaybackState.SEEKING)) return@Runnable // in case we completed since this was requested

			seek(currentSkipPos)
			currentSkipPos = 0
		}

	private fun skip(msec: Int) {
		if (hasInitializedVideoManager() &&
			(isPlaying || isPaused || mPlaybackState == PlaybackState.SEEKING) &&
			spinnerOff &&
			mVideoManager!!.getCurrentPosition() > 0
		) {
			// guard against skipping before playback has truly begun
			mHandler.removeCallbacks(skipRunnable)
			refreshCurrentPosition()
			currentSkipPos = Utils.getSafeSeekPosition((if (currentSkipPos == 0L) mCurrentPosition else currentSkipPos) + msec, duration)

			Timber.i("Skip amount requested was %s. Calculated position is %s", msec, currentSkipPos)
			Timber.i("Duration reported as: %s current pos: %s", duration, mCurrentPosition)

			mSeekPosition = currentSkipPos
			mHandler.postDelayed(skipRunnable, 800)
		}
	}

	fun updateTvProgramInfo() {
		// Get the current program info when playing a live TV channel
		val channel = currentlyPlayingItem ?: return
		if (channel.type == BaseItemKind.TV_CHANNEL) {
			getLiveTvChannel(channel.id) { updatedChannel ->
				val program = updatedChannel.currentProgram
				if (program != null) {
					mCurrentProgramEnd = program.endDate
					mCurrentProgramStart = program.startDate
				}
			}
		}
	}

	private fun getRealTimeProgress(): Long =
		if (mCurrentProgramStart != null) {
			Duration.between(mCurrentProgramStart, LocalDateTime.now()).toMillis()
		} else {
			0
		}

	private fun getTimeShiftedProgress(): Long {
		refreshCurrentPosition()
		return if (!directStreamLiveTv) {
			mCurrentPosition + (mCurrentTranscodeStartTime - (mCurrentProgramStart?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: 0L))
		} else {
			getRealTimeProgress()
		}
	}

	private fun startReportLoop() {
		if (mCurrentStreamInfo == null) return

		stopReportLoop()
		val frag = fragment ?: return
		reportingHelper.reportProgress(frag, this, currentlyPlayingItem!!, currentStreamInfo!!, mCurrentPosition * 10000, false)
		mReportLoop =
			object : Runnable {
				override fun run() {
					if (isPlaying) {
						refreshCurrentPosition()
						val currentTime = if (isLiveTv) getTimeShiftedProgress() else mCurrentPosition
						val f = fragment ?: return
						reportingHelper.reportProgress(f, this@PlaybackController, currentlyPlayingItem!!, currentStreamInfo!!, currentTime * 10000, false)
					}
					if (mPlaybackState != PlaybackState.UNDEFINED && mPlaybackState != PlaybackState.IDLE) {
						mHandler.postDelayed(this, PROGRESS_REPORTING_INTERVAL)
					}
				}
			}
		mHandler.postDelayed(mReportLoop!!, PROGRESS_REPORTING_INTERVAL)
	}

	private fun startPauseReportLoop() {
		stopReportLoop()
		if (mCurrentStreamInfo == null) return
		val frag = fragment ?: return
		reportingHelper.reportProgress(frag, this, currentlyPlayingItem!!, mCurrentStreamInfo!!, mCurrentPosition * 10000, true)
		mReportLoop =
			object : Runnable {
				override fun run() {
					val currentItem = currentlyPlayingItem
					if (currentItem == null) {
						// Loop was called while nothing was playing!
						stopReportLoop()
						return
					}

					if (mPlaybackState != PlaybackState.PAUSED) {
						// Playback is not paused anymore, stop reporting
						return
					}
					refreshCurrentPosition()
					val currentTime = if (isLiveTv) getTimeShiftedProgress() else mCurrentPosition
					val f = fragment ?: return
					reportingHelper.reportProgress(f, this@PlaybackController, currentItem, currentStreamInfo!!, currentTime * 10000, true)
					mHandler.postDelayed(this, PROGRESS_REPORTING_PAUSE_INTERVAL)
				}
			}
		mHandler.postDelayed(mReportLoop!!, PROGRESS_REPORTING_PAUSE_INTERVAL)
	}

	private fun stopReportLoop() {
		val loop = mReportLoop
		if (loop != null) {
			mHandler.removeCallbacks(loop)
		}
	}

	private fun initialSeek(position: Long) {
		mHandler.post(
			object : Runnable {
				override fun run() {
					if (mVideoManager == null) return
					if (mVideoManager!!.getDuration() <= 0) {
						// use mVideoManager.getDuration here for accurate results
						// wait until we have valid duration
						mHandler.postDelayed(this, 25)
					} else if (mVideoManager!!.isSeekable()) {
						seek(position)
					} else {
						finishedInitialSeek = true
					}
				}
			},
		)
	}

	private fun itemComplete() {
		interactionTracker.onEpisodeWatched()
		stop()
		resetPlayerErrors()

		val nextItem = nextItem
		val curItem = currentlyPlayingItem
		if (nextItem == null || curItem == null) {
			endPlayback(true)
			return
		}

		Timber.i("Moving to next queue item. Index: %s", mCurrentIndex + 1)
		val stillWatchingEnabled = userPreferences[UserPreferences.stillWatchingBehavior] != StillWatchingBehavior.DISABLED
		val nextUpEnabled = userPreferences[UserPreferences.nextUpBehavior] != NextUpBehavior.DISABLED
		if ((stillWatchingEnabled || nextUpEnabled) && curItem.type != BaseItemKind.TRAILER) {
			mCurrentIndex++
			videoQueueManager.setCurrentMediaPosition(mCurrentIndex)
			spinnerOff = false

			endPlayback()
		} else {
			next()
		}
	}

	override fun onPlaybackSpeedChange(newSpeed: Float) {
		// TODO, implement speed change handling
	}

	override fun onPrepared() {
		// Guard against race condition where playback is stopped before onPrepared callback
		// This can happen during rapid playlist navigation
		if (mCurrentStreamInfo == null) {
			Timber.w("onPrepared called but mCurrentStreamInfo is null - playback may have been stopped")
			return
		}

		if (mPlaybackState == PlaybackState.BUFFERING) {
			mPlaybackState = PlaybackState.PLAYING
			interactionTracker.notifyStart(currentlyPlayingItem!!)
			mCurrentTranscodeStartTime = if (mCurrentStreamInfo?.playMethod == PlayMethod.TRANSCODE) Instant.now().toEpochMilli() else 0
			startReportLoop()
		}

		Timber.i("Play method: %s", if (mCurrentStreamInfo?.playMethod == PlayMethod.TRANSCODE) "Trans" else "Direct")

		if (mPlaybackState == PlaybackState.PAUSED) {
			mPlaybackState = PlaybackState.PLAYING
		} else {
			if (!burningSubs) {
				// Make sure the requested subtitles are enabled when external/embedded
				val currentSubtitleIndex = mCurrentOptions?.subtitleStreamIndex ?: -1
				setSubtitleIndex(currentSubtitleIndex, true)
			} else {
				disableDefaultSubtitles()
			}

			// select an audio track
			var eligibleAudioTrack = mDefaultAudioIndex

			// if track switching is done without rebuilding the stream, mCurrentOptions is updated
			// otherwise, use the server default
			if (mCurrentOptions?.audioStreamIndex != null) {
				eligibleAudioTrack = mCurrentOptions!!.audioStreamIndex!!
			} else if (currentMediaSource?.defaultAudioStreamIndex != null) {
				eligibleAudioTrack = currentMediaSource!!.defaultAudioStreamIndex!!
			}
			// Clear pre-set index so switchAudioStream queries ExoPlayer's actual track
			mCurrentOptions?.audioStreamIndex = null
			switchAudioStream(eligibleAudioTrack)
		}

		// Force disable subtitles if preference is enabled and default is None
		val subtitlesDefaultToNone = userPreferences[UserPreferences.subtitlesDefaultToNone]
		if (subtitlesDefaultToNone && (mCurrentOptions?.subtitleStreamIndex == null || mCurrentOptions?.subtitleStreamIndex == -1)) {
			disableDefaultSubtitles()
		}
	}

	override fun onError() {
		val frag = fragment
		if (frag == null) {
			playerErrorEncountered()
			return
		}

		if (isLiveTv && directStreamLiveTv) {
			Toast.makeText(frag.context, frag.getString(R.string.msg_error_live_stream), Toast.LENGTH_LONG).show()
			directStreamLiveTv = false
		} else {
			val msg = frag.getString(R.string.video_error_unknown_error)
			Timber.e("Playback error - %s", msg)
		}
		playerErrorEncountered()
	}

	override fun onCompletion() {
		Timber.i("On Completion fired")
		itemComplete()
	}

	override fun onProgress() {
		refreshCurrentPosition()
		if (isPlaying) {
			if (!spinnerOff) {
				if (mStartPosition > 0) {
					initialSeek(mStartPosition)
					mStartPosition = 0
				} else {
					finishedInitialSeek = true
					stopSpinner()
				}
			}
		}
	}

	val duration: Long
		get() {
			var dur = 0L

			if (hasInitializedVideoManager()) {
				dur = mVideoManager!!.getDuration()
			} else if (currentMediaSource?.runTimeTicks != null) {
				dur = currentMediaSource!!.runTimeTicks!! / 10000
			} else if (currentlyPlayingItem?.runTimeTicks != null) {
				dur = currentlyPlayingItem!!.runTimeTicks!! / 10000
			}
			return if (dur > 0) dur else 0
		}

	val bufferedPosition: Long
		get() {
			var buffered = -1L

			if (hasInitializedVideoManager()) {
				buffered = mVideoManager!!.getBufferedPosition()
			}

			if (buffered < 0) {
				buffered = duration
			}

			return buffered
		}

	val currentPosition: Long
		get() {
			if (currentSkipPos > 0) return currentSkipPos
			return if (!isPlaying && mSeekPosition != -1L) mSeekPosition else mCurrentPosition
		}

	val isPaused: Boolean
		get() = mPlaybackState == PlaybackState.PAUSED

	val zoomMode: ZoomMode
		get() = if (hasInitializedVideoManager()) mVideoManager!!.zoomMode else ZoomMode.FIT

	fun setZoom(mode: ZoomMode) {
		if (hasInitializedVideoManager()) {
			mVideoManager?.setZoom(mode)
		}
	}

	/**
	 * List of various states that we can be in
	 */
	enum class PlaybackState {
		PLAYING,
		PAUSED,
		BUFFERING,
		IDLE,
		SEEKING,
		UNDEFINED,
		ERROR,
	}
}
