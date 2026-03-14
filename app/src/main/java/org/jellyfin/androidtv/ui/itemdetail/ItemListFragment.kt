package org.jellyfin.androidtv.ui.itemdetail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.data.service.BlurContext
import org.jellyfin.androidtv.databinding.FragmentItemListBinding
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.AsyncImageView
import org.jellyfin.androidtv.ui.ItemListView
import org.jellyfin.androidtv.ui.ItemRowView
import org.jellyfin.androidtv.ui.TextUnderButton
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.AudioEventListener
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.ui.refresh
import org.jellyfin.androidtv.ui.shuffle.executeGenreShuffle
import org.jellyfin.androidtv.ui.shuffle.showShuffleDialog
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.InfoLayoutHelper
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.UUIDUtils
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.sdk.canPlay
import org.jellyfin.androidtv.util.stripHtml
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaType
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.time.Instant
import java.util.UUID

class ItemListFragment :
	Fragment(),
	View.OnKeyListener {
	data class Args(
		val itemId: UUID,
		val serverId: UUID? = null,
	) {
		fun toBundle() =
			bundleOf(
				KEY_ITEM_ID to itemId.toString(),
				KEY_SERVER_ID to serverId?.toString(),
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
				)
			}
		}
	}

	companion object {
		internal const val KEY_ITEM_ID = "ItemId"
		internal const val KEY_SERVER_ID = "ServerId"
	}

	private var buttonSize = 0

	private lateinit var mTitle: TextView
	private lateinit var mGenreRow: TextView
	private lateinit var mPoster: AsyncImageView
	private lateinit var mSummary: TextView
	private lateinit var mButtonRow: android.widget.LinearLayout
	private lateinit var mItemList: ItemListView
	private lateinit var mScrollView: ScrollView
	private var mCurrentRow: ItemRowView? = null

	private var mCurrentlyPlayingRow: ItemRowView? = null

	var mBaseItem: BaseItemDto? = null
	private var mItems: MutableList<BaseItemDto> = mutableListOf()

	private var mBottomScrollThreshold = 0
	private lateinit var mMetrics: DisplayMetrics

	private var firstTime = true
	private var lastUpdated: Instant = Instant.now()

	private val dataRefreshService: DataRefreshService by inject()
	private val backgroundService: BackgroundService by inject()
	private val mediaManager: MediaManager by inject()
	private val navigationRepository: NavigationRepository by inject()
	private val itemLauncher: ItemLauncher by inject()
	private val playbackHelper: PlaybackHelper by inject()
	private val imageHelper: ImageHelper by inject()
	private val shuffleManager: org.jellyfin.androidtv.ui.shuffle.ShuffleManager by inject()
	private val playbackLauncher: PlaybackLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		val binding = FragmentItemListBinding.inflate(layoutInflater, container, false)

		buttonSize = Utils.convertDpToPixel(requireContext(), 35)

		val detailsBinding = binding.details.binding
		mTitle = detailsBinding.fdTitle
		mTitle.text = getString(R.string.loading)
		mGenreRow = detailsBinding.fdGenreRow
		mPoster = detailsBinding.mainImage
		mButtonRow = detailsBinding.fdButtonRow
		mSummary = detailsBinding.fdSummaryText
		mItemList = binding.songs
		mScrollView = binding.scrollView

		mMetrics = DisplayMetrics()
		@Suppress("DEPRECATION")
		requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
		mBottomScrollThreshold = (mMetrics.heightPixels * .6).toInt()

		// Item list listeners
		mItemList.setRowSelectedListener { row ->
			mCurrentRow = row
			// Keep selected row in center of screen
			val location = IntArray(2)
			row.getLocationOnScreen(location)
			val y = location[1]
			if (y > mBottomScrollThreshold) {
				mScrollView.smoothScrollBy(0, y - mBottomScrollThreshold)
			}
			val selectedItem = row.item
			if (selectedItem != null) {
				backgroundService.setBackground(selectedItem, BlurContext.DETAILS)
			}
		}

		mItemList.setRowClickedListener { row ->
			showMenu(row, row.item?.type != BaseItemKind.AUDIO)
		}

		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		val args = Args.fromBundle(arguments) ?: return
		loadItem(args.itemId)
	}

	override fun onKey(
		v: View?,
		keyCode: Int,
		event: KeyEvent,
	): Boolean {
		if (event.action != KeyEvent.ACTION_UP) return false

		// Handle playlist item reordering with DPAD_LEFT/RIGHT
		val currentRow = mCurrentRow
		val baseItem = mBaseItem
		if (currentRow != null &&
			baseItem != null &&
			baseItem.type == BaseItemKind.PLAYLIST &&
			baseItem.canDelete == true
		) {
			val currentIndex = currentRow.getIndex()
			when (keyCode) {
				KeyEvent.KEYCODE_DPAD_LEFT -> {
					if (currentIndex > 0) {
						movePlaylistItem(currentIndex, currentIndex - 1)
						return true
					}
				}
				KeyEvent.KEYCODE_DPAD_RIGHT -> {
					if (currentIndex < mItems.size - 1) {
						movePlaylistItem(currentIndex, currentIndex + 1)
						return true
					}
				}
			}
		}

		if (mediaManager.isPlayingAudio) {
			when (keyCode) {
				KeyEvent.KEYCODE_MEDIA_PAUSE,
				KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
				-> {
					mediaManager.togglePlayPause()
					return true
				}
				KeyEvent.KEYCODE_MEDIA_NEXT,
				KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
				-> {
					mediaManager.nextAudioItem()
					return true
				}
				KeyEvent.KEYCODE_MEDIA_PREVIOUS,
				KeyEvent.KEYCODE_MEDIA_REWIND,
				-> {
					mediaManager.prevAudioItem()
					return true
				}
				KeyEvent.KEYCODE_MENU -> {
					showMenu(mCurrentRow, false)
					return true
				}
			}
		} else if (mCurrentRow != null) {
			when (keyCode) {
				KeyEvent.KEYCODE_MEDIA_PLAY,
				KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
				KeyEvent.KEYCODE_MENU,
				-> {
					showMenu(mCurrentRow, false)
					return true
				}
			}
		}

		return false
	}

	private fun movePlaylistItem(
		fromIndex: Int,
		toIndex: Int,
	) {
		val baseItem = mBaseItem ?: return
		if (mItems.isEmpty()) return

		val item = mItems[fromIndex]
		val playlistItemId = item.playlistItemId ?: return

		movePlaylistItem(baseItem.id, playlistItemId, toIndex) {
			mItems.removeAt(fromIndex)
			mItems.add(toIndex, item)
			mItemList.moveItem(fromIndex, toIndex)
			mItemList.focusItemAt(toIndex)
		}
	}

	override fun onResume() {
		super.onResume()
		mediaManager.addAudioEventListener(mAudioEventListener)
		// Fire it to be sure we're updated
		mAudioEventListener.onPlaybackStateChange(
			if (mediaManager.isPlayingAudio) PlaybackController.PlaybackState.PLAYING else PlaybackController.PlaybackState.IDLE,
			mediaManager.currentAudioItem,
		)

		if (!firstTime && dataRefreshService.lastPlayback?.isAfter(lastUpdated) == true) {
			if (MediaType.VIDEO == mBaseItem?.mediaType) {
				Handler(Looper.getMainLooper()).postDelayed({
					if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@postDelayed
					mItemList.refresh()
					lastUpdated = Instant.now()
				}, 500)
			}
		}

		firstTime = false
	}

	override fun onPause() {
		super.onPause()
		mediaManager.removeAudioEventListener(mAudioEventListener)
	}

	private val mAudioEventListener =
		object : AudioEventListener {
			override fun onPlaybackStateChange(
				newState: PlaybackController.PlaybackState,
				currentItem: BaseItemDto?,
			) {
				Timber.i("Got playback state change event %s for item %s", newState.toString(), currentItem?.name ?: "<unknown>")

				if (newState != PlaybackController.PlaybackState.PLAYING || currentItem == null) {
					mCurrentlyPlayingRow?.updateCurrentTime(-1)
					mCurrentlyPlayingRow = mItemList.updatePlaying(null)
				} else {
					mCurrentlyPlayingRow = mItemList.updatePlaying(currentItem.id)
				}
			}

			override fun onProgress(
				pos: Long,
				duration: Long,
			) {
				mCurrentlyPlayingRow?.updateCurrentTime(pos)
			}

			override fun onQueueStatusChanged(hasQueue: Boolean) {}

			override fun onQueueReplaced() {}
		}

	private fun showMenu(
		row: ItemRowView?,
		showOpen: Boolean,
	) {
		val anchor = row ?: requireActivity().currentFocus ?: return
		val menu = PopupMenu(requireContext(), anchor, Gravity.END)
		var order = 0

		if (showOpen && row != null) {
			val rowItem = row.item ?: return
			menu.menu.add(0, 0, order++, R.string.lbl_open).setOnMenuItemClickListener {
				itemLauncher.launch(BaseItemDtoBaseRowItem(rowItem), requireContext())
				true
			}
		}

		if (row != null) {
			menu.menu.add(0, 0, order++, R.string.lbl_play_from_here).setOnMenuItemClickListener {
				play(mItems, row.getIndex(), false)
				true
			}
			menu.menu.add(0, 1, order++, R.string.lbl_play).setOnMenuItemClickListener {
				play(mItems.subList(row.getIndex(), row.getIndex() + 1), false)
				true
			}

			val rowItem = row.item
			if (rowItem?.type == BaseItemKind.AUDIO) {
				menu.menu.add(0, 2, order++, R.string.lbl_add_to_queue).setOnMenuItemClickListener {
					mediaManager.queueAudioItem(rowItem)
					true
				}
				menu.menu.add(0, 1, order++, R.string.lbl_instant_mix).setOnMenuItemClickListener {
					playbackHelper.playInstantMix(requireContext(), rowItem)
					true
				}
			}

			// Remove from playlist option
			val baseItem = mBaseItem
			if (baseItem != null &&
				baseItem.type == BaseItemKind.PLAYLIST &&
				rowItem?.playlistItemId != null &&
				baseItem.canDelete == true
			) {
				menu.menu.add(0, 3, order++, R.string.lbl_remove_from_playlist).setOnMenuItemClickListener {
					removeFromPlaylist(baseItem.id, rowItem.playlistItemId!!) {
						mItems.removeAt(row.getIndex())
						if (mItems.isEmpty()) {
							navigationRepository.goBack()
						} else {
							mItemList.clear()
							mItems.forEachIndexed { i, listItem ->
								mItemList.addItem(listItem, i)
							}
						}
					}
					true
				}
			}
		}

		menu.show()
	}

	fun setBaseItem(item: BaseItemDto) {
		mBaseItem = item

		val mainInfoRow = requireActivity().findViewById<android.widget.LinearLayout>(R.id.fdMainInfoRow)
		val ratingsRow = requireActivity().findViewById<android.widget.LinearLayout>(R.id.fdRatingsRow)

		InfoLayoutHelper.addInfoRow(requireContext(), item, mainInfoRow, false)
		InfoLayoutHelper.addRatingsRow(requireContext(), item, ratingsRow)
		addGenres(mGenreRow)
		addButtons(buttonSize)

		val overview = mBaseItem?.overview
		mSummary.text = overview?.stripHtml() ?: ""

		val aspect = imageHelper.getImageAspectRatio(item, false)
		val primaryImageUrl = imageHelper.getPrimaryImageUrl(item, null, ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT)
		mPoster.setPadding(0, 0, 0, 0)
		mPoster.load(primaryImageUrl, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_album), aspect, 0)

		getPlaylist(mBaseItem!!, itemResponse)
	}

	private val itemResponse: (List<BaseItemDto>) -> Unit = { items ->
		mTitle.text = mBaseItem?.name
		mTitle.isSelected = true
		if (items.isNotEmpty()) {
			val baseItem = mBaseItem
			val canReorder = baseItem?.type == BaseItemKind.PLAYLIST && baseItem.canDelete == true
			mItemList.setReorderingEnabled(canReorder)

			mItems = mutableListOf()
			items.forEachIndexed { i, item ->
				mItemList.addItem(item, i)
				mItems.add(item)
			}
			if (mediaManager.isPlayingAudio) {
				mAudioEventListener.onPlaybackStateChange(PlaybackController.PlaybackState.PLAYING, mediaManager.currentAudioItem)
			}

			updateBackdrop()
		}
	}

	private fun addGenres(textView: TextView) {
		val genres = mBaseItem?.genres
		if (genres != null) {
			textView.text = TextUtils.join(" / ", genres)
		} else {
			textView.text = null
		}
	}

	private fun play(
		items: List<BaseItemDto>,
		shuffle: Boolean,
	) {
		play(items, 0, shuffle)
	}

	private fun play(
		items: List<BaseItemDto>,
		ndx: Int,
		shuffle: Boolean,
	) {
		Timber.i("play items: %d, ndx: %d, shuffle: %b", items.size, ndx, shuffle)

		var pos = 0
		val item = if (items.isNotEmpty()) items[ndx] else null
		if (item?.userData != null) {
			pos = Math.toIntExact(item.userData!!.playbackPositionTicks / 10000)
		}
		playbackLauncher.launch(requireContext(), items, pos, false, ndx, shuffle)
	}

	private fun addButtons(buttonSize: Int) {
		val baseItem = mBaseItem ?: return

		if (baseItem.canPlay()) {
			val play =
				TextUnderButton.create(
					requireContext(),
					R.drawable.ic_play,
					buttonSize,
					2,
					getString(if (baseItem.isFolder == true) R.string.lbl_play_all else R.string.lbl_play),
				) {
					if (mItems.isNotEmpty()) {
						play(mItems, false)
					} else {
						Utils.showToast(requireContext(), R.string.msg_no_playable_items)
					}
				}
			play.onFocusChangeListener =
				View.OnFocusChangeListener { _, hasFocus ->
					if (hasFocus) mScrollView.smoothScrollTo(0, 0)
				}
			mButtonRow.addView(play)

			var hidePlayButton = false
			var queueButton: TextUnderButton? = null

			if (baseItem.type == BaseItemKind.MUSIC_ALBUM && mediaManager.hasAudioQueueItems()) {
				queueButton =
					TextUnderButton.create(
						requireContext(),
						R.drawable.ic_add,
						buttonSize,
						2,
						getString(R.string.lbl_add_to_queue),
					) {
						mediaManager.addToAudioQueue(mItems)
					}
				hidePlayButton = true
				mButtonRow.addView(queueButton)
				queueButton.onFocusChangeListener =
					View.OnFocusChangeListener { _, hasFocus ->
						if (hasFocus) mScrollView.smoothScrollTo(0, 0)
					}
			}

			if (hidePlayButton) {
				play.visibility = View.GONE
				queueButton?.requestFocus()
			} else {
				play.requestFocus()
			}

			if (baseItem.isFolder == true && baseItem.type != BaseItemKind.BOX_SET) {
				val shuffle =
					TextUnderButton.create(
						requireContext(),
						R.drawable.ic_shuffle,
						buttonSize,
						2,
						getString(R.string.lbl_shuffle_all),
					) {
						if (baseItem.type == BaseItemKind.GENRE) {
							val serverId = UUIDUtils.parseUUID(baseItem.serverId)
							val userPreferences: UserPreferences by inject()
							executeGenreShuffle(
								requireContext(),
								baseItem.name,
								baseItem.parentId,
								serverId,
								userPreferences,
								navigationRepository,
								shuffleManager,
							)
						} else if (mItems.isNotEmpty()) {
							playbackHelper.retrieveAndPlay(baseItem.id, true, requireContext())
						} else {
							Utils.showToast(requireContext(), R.string.msg_no_playable_items)
						}
					}
				shuffle.setOnLongClickListener {
					showShuffleDialog(requireContext(), navigationRepository)
					true
				}
				mButtonRow.addView(shuffle)
				shuffle.onFocusChangeListener =
					View.OnFocusChangeListener { _, hasFocus ->
						if (hasFocus) mScrollView.smoothScrollTo(0, 0)
					}
			}
		}

		if (baseItem.type == BaseItemKind.MUSIC_ALBUM) {
			val mix =
				TextUnderButton.create(
					requireContext(),
					R.drawable.ic_mix,
					buttonSize,
					2,
					getString(R.string.lbl_instant_mix),
				) {
					playbackHelper.playInstantMix(requireContext(), baseItem)
				}
			mButtonRow.addView(mix)
			mix.onFocusChangeListener =
				View.OnFocusChangeListener { _, hasFocus ->
					if (hasFocus) mScrollView.smoothScrollTo(0, 0)
				}
		}

		// Favorite
		val fav =
			TextUnderButton.create(
				requireContext(),
				R.drawable.ic_heart,
				buttonSize,
				2,
				getString(R.string.lbl_favorite),
			) { v ->
				toggleFavorite(baseItem) { updatedItem ->
					mBaseItem = updatedItem
					v.isActivated = updatedItem.userData?.isFavorite == true
					dataRefreshService.lastFavoriteUpdate = Instant.now()
				}
			}
		fav.isActivated = baseItem.userData?.isFavorite == true
		mButtonRow.addView(fav)
		fav.onFocusChangeListener =
			View.OnFocusChangeListener { _, hasFocus ->
				if (hasFocus) mScrollView.smoothScrollTo(0, 0)
			}

		val albumArtists = baseItem.albumArtists
		if (!albumArtists.isNullOrEmpty()) {
			val artist =
				TextUnderButton.create(
					requireContext(),
					R.drawable.ic_user,
					buttonSize,
					4,
					getString(R.string.lbl_open_artist),
				) {
					navigationRepository.navigate(Destinations.itemDetails(albumArtists[0].id))
				}
			mButtonRow.addView(artist)
			artist.onFocusChangeListener =
				View.OnFocusChangeListener { _, hasFocus ->
					if (hasFocus) mScrollView.smoothScrollTo(0, 0)
				}
		}
	}

	private fun updateBackdrop() {
		var item = mBaseItem ?: return

		if (item.backdropImageTags.isNullOrEmpty() && mItems.isNotEmpty()) {
			item = mItems[0]
		}

		backgroundService.setBackground(item, BlurContext.DETAILS)
	}
}
