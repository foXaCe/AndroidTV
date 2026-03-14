package org.jellyfin.androidtv.ui.playback.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.AudioEventListener
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.queue.QueueEntry
import org.jellyfin.playback.core.queue.queue
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.sdk.model.api.BaseItemDto

data class AudioNowPlayingUiState(
	val item: BaseItemDto? = null,
	val artistName: String = "",
	val songTitle: String = "",
	val albumTitle: String = "",
	val trackInfo: String = "",
	val genres: String = "",
	val currentPosition: Long = 0L,
	val duration: Long = 0L,
	val isPlaying: Boolean = false,
	val isRepeatMode: Boolean = false,
	val isShuffleMode: Boolean = false,
	val hasPrevious: Boolean = false,
	val hasNext: Boolean = false,
	val canOpenAlbum: Boolean = false,
	val canOpenArtist: Boolean = false,
	val queueItems: List<AudioQueueItem> = emptyList(),
	val queueSize: Int = 0,
)

data class AudioQueueItem(
	val entry: QueueEntry,
	val baseItem: BaseItemDto,
	val isCurrent: Boolean = false,
)

class AudioNowPlayingViewModel(
	private val mediaManager: MediaManager,
	private val playbackManager: PlaybackManager,
	private val backgroundService: BackgroundService,
	private val navigationRepository: NavigationRepository,
) : ViewModel() {
	private val _uiState = MutableStateFlow(AudioNowPlayingUiState())
	val uiState: StateFlow<AudioNowPlayingUiState> = _uiState.asStateFlow()

	private val audioEventListener =
		object : AudioEventListener {
			override fun onPlaybackStateChange(
				newState: PlaybackController.PlaybackState,
				currentItem: BaseItemDto?,
			) {
				if (currentItem != _uiState.value.item) loadItem()
				updateButtons()
			}

			override fun onProgress(
				pos: Long,
				duration: Long,
			) {
				_uiState.update { it.copy(currentPosition = pos, duration = duration) }
			}

			override fun onQueueStatusChanged(hasQueue: Boolean) {
				loadItem()
				if (mediaManager.isAudioPlayerInitialized) updateButtons()
			}

			override fun onQueueReplaced() = Unit
		}

	init {
		mediaManager.addAudioEventListener(audioEventListener)
		loadItem()
		updateButtons()

		// Watch queue changes for the queue list
		playbackManager.queue.entry
			.onEach { updateQueue() }
			.launchIn(viewModelScope)
		playbackManager.queue.entries
			.onEach { updateQueue() }
			.launchIn(viewModelScope)
		playbackManager.state.playbackOrder
			.onEach { updateQueue() }
			.launchIn(viewModelScope)
	}

	override fun onCleared() {
		super.onCleared()
		mediaManager.removeAudioEventListener(audioEventListener)
	}

	private fun loadItem() {
		val item = mediaManager.currentAudioItem
		if (item != null) {
			val artistName = item.artists?.firstOrNull() ?: item.albumArtist ?: ""
			val albumTitle = item.album ?: ""
			val genres = item.genres?.joinToString(" / ") ?: ""
			val trackInfo = "${mediaManager.currentAudioQueueDisplayPosition} of ${mediaManager.currentAudioQueueDisplaySize}"

			_uiState.update {
				it.copy(
					item = item,
					artistName = artistName,
					songTitle = item.name ?: "",
					albumTitle = albumTitle,
					trackInfo = trackInfo,
					genres = genres,
				)
			}
			backgroundService.setBackground(item, BlurContext.DETAILS)
		} else {
			if (navigationRepository.canGoBack) {
				navigationRepository.goBack()
			} else {
				navigationRepository.navigate(Destinations.home)
			}
		}
	}

	private fun updateButtons() {
		val playing = mediaManager.isPlayingAudio
		val item = _uiState.value.item

		_uiState.update {
			it.copy(
				isPlaying = playing,
				isRepeatMode = mediaManager.isRepeatMode,
				isShuffleMode = mediaManager.isShuffleMode,
				hasPrevious = mediaManager.hasPrevAudioItem(),
				hasNext = mediaManager.hasNextAudioItem(),
				canOpenAlbum = item?.albumId != null,
				canOpenArtist = !item?.albumArtists.isNullOrEmpty(),
			)
		}
	}

	private suspend fun updateQueue() {
		val currentEntry = playbackManager.queue.entry.value
		val currentItem =
			currentEntry?.let { entry ->
				entry.baseItem?.let { AudioQueueItem(entry, it, isCurrent = true) }
			}
		val upcoming =
			playbackManager.queue
				.peekNext(100)
				.mapNotNull { entry -> entry.baseItem?.let { AudioQueueItem(entry, it) } }

		val items = listOfNotNull(currentItem) + upcoming

		_uiState.update {
			it.copy(
				queueItems = items,
				queueSize = items.size,
			)
		}
	}

	// Player controls
	fun playPause() {
		mediaManager.togglePlayPause()
	}

	fun next() {
		mediaManager.nextAudioItem()
	}

	fun previous() {
		mediaManager.prevAudioItem()
	}

	fun rewind() {
		mediaManager.rewind()
		updateButtons()
	}

	fun fastForward() {
		mediaManager.fastForward()
		updateButtons()
	}

	fun toggleRepeat() {
		mediaManager.toggleRepeat()
		updateButtons()
	}

	fun toggleShuffle() {
		mediaManager.shuffleAudioQueue()
		updateButtons()
	}

	fun openAlbum() {
		val albumId = _uiState.value.item?.albumId ?: return
		navigationRepository.navigate(Destinations.itemList(albumId))
	}

	fun openArtist() {
		val artistId =
			_uiState.value.item
				?.albumArtists
				?.firstOrNull()
				?.id ?: return
		navigationRepository.navigate(Destinations.itemDetails(artistId))
	}

	fun playAt(entry: QueueEntry) {
		mediaManager.playFrom(entry)
	}
}
