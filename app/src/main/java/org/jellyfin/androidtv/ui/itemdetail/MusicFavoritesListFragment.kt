package org.jellyfin.androidtv.ui.itemdetail

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ScrollView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.databinding.FragmentItemListBinding
import org.jellyfin.androidtv.ui.ItemListView
import org.jellyfin.androidtv.ui.ItemRowView
import org.jellyfin.androidtv.ui.TextUnderButton
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import org.jellyfin.androidtv.ui.playback.AudioEventListener
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class MusicFavoritesListFragment :
	Fragment(),
	View.OnKeyListener {
	data class Args(
		val parentId: UUID,
	) {
		fun toBundle() =
			bundleOf(
				KEY_PARENT_ID to parentId.toString(),
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val parentId =
					bundle
						?.getString(KEY_PARENT_ID)
						?.let {
							try {
								UUID.fromString(it)
							} catch (_: Exception) {
								null
							}
						}
						?: return null
				return Args(parentId = parentId)
			}
		}
	}

	companion object {
		internal const val KEY_PARENT_ID = "ParentId"
	}

	private var mButtonRow: android.widget.LinearLayout? = null
	private var mItemList: ItemListView? = null
	private var mScrollView: ScrollView? = null
	private var mCurrentRow: ItemRowView? = null
	private var mCurrentlyPlayingRow: ItemRowView? = null

	private var mItems: MutableList<BaseItemDto> = mutableListOf()
	private var mBottomScrollThreshold: Int = 0

	private val mediaManager: MediaManager by inject()
	private val itemLauncher: ItemLauncher by inject()
	private val playbackHelper: PlaybackHelper by inject()
	private val playbackLauncher: PlaybackLauncher by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		val binding = FragmentItemListBinding.inflate(layoutInflater, container, false)
		val detailsBinding = binding.details.binding

		detailsBinding.fdTitle.text = getString(R.string.lbl_favorites)
		detailsBinding.mainImage.setImageResource(R.drawable.favorites)
		detailsBinding.fdSummaryText.text = getString(R.string.desc_automatic_fav_songs)
		mButtonRow = detailsBinding.fdButtonRow
		mItemList = binding.songs
		mScrollView = binding.scrollView

		val metrics = DisplayMetrics()
		@Suppress("DEPRECATION")
		requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
		mBottomScrollThreshold = (metrics.heightPixels * .6).toInt()

		mItemList?.setRowSelectedListener { row ->
			mCurrentRow = row
			val location = IntArray(2)
			row.getLocationOnScreen(location)
			val y = location[1]
			if (y > mBottomScrollThreshold) {
				mScrollView?.smoothScrollBy(0, y - mBottomScrollThreshold)
			}
		}

		mItemList?.setRowClickedListener { row ->
			showMenu(row, row.item?.type != BaseItemKind.AUDIO)
		}

		val play =
			TextUnderButton.create(
				requireContext(),
				R.drawable.ic_play,
				Utils.convertDpToPixel(requireContext(), 35),
				2,
				getString(R.string.lbl_play_all),
			) {
				play(mItems, 0, false)
			}
		play.onFocusChangeListener =
			View.OnFocusChangeListener { _, hasFocus ->
				if (hasFocus) mScrollView?.smoothScrollTo(0, 0)
			}
		mButtonRow?.addView(play)
		play.requestFocus()

		val shuffle =
			TextUnderButton.create(
				requireContext(),
				R.drawable.ic_shuffle,
				Utils.convertDpToPixel(requireContext(), 35),
				2,
				getString(R.string.lbl_shuffle_all),
			) {
				play(mItems, 0, true)
			}
		shuffle.onFocusChangeListener =
			View.OnFocusChangeListener { _, hasFocus ->
				if (hasFocus) mScrollView?.smoothScrollTo(0, 0)
			}
		mButtonRow?.addView(shuffle)

		return binding.root
	}

	override fun onKey(
		v: View,
		keyCode: Int,
		event: KeyEvent,
	): Boolean {
		if (event.action != KeyEvent.ACTION_UP) return false

		if (mediaManager.isPlayingAudio) {
			when (keyCode) {
				KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
					mediaManager.togglePlayPause()
					return true
				}
				KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
					mediaManager.nextAudioItem()
					return true
				}
				KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_MEDIA_REWIND -> {
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
				KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MENU -> {
					showMenu(mCurrentRow, false)
					return true
				}
			}
		}

		return false
	}

	override fun onResume() {
		super.onResume()

		mediaManager.addAudioEventListener(mAudioEventListener)
		mAudioEventListener.onPlaybackStateChange(
			if (mediaManager.isPlayingAudio) PlaybackController.PlaybackState.PLAYING else PlaybackController.PlaybackState.IDLE,
			mediaManager.currentAudioItem,
		)

		val parentId = Args.fromBundle(arguments)?.parentId
		getFavoritePlaylist(parentId, itemResponse)
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
					mCurrentlyPlayingRow = mItemList?.updatePlaying(null)
				} else {
					mCurrentlyPlayingRow = mItemList?.updatePlaying(currentItem.id)
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
		val menu = PopupMenu(requireContext(), row ?: requireActivity().currentFocus, Gravity.END)
		var order = 0

		if (showOpen) {
			menu.menu.add(0, 0, order++, R.string.lbl_open).setOnMenuItemClickListener {
				row?.item?.let { itemLauncher.launch(BaseItemDtoBaseRowItem(it), requireContext()) }
				true
			}
		}

		menu.menu.add(0, 0, order++, R.string.lbl_play_from_here).setOnMenuItemClickListener {
			row?.let { play(mItems, it.getIndex(), false) }
			true
		}

		menu.menu.add(0, 1, order++, R.string.lbl_play).setOnMenuItemClickListener {
			row?.let { play(mItems.subList(it.getIndex(), it.getIndex() + 1), 0, false) }
			true
		}

		val rowItemDto = row?.item
		if (rowItemDto?.type == BaseItemKind.AUDIO) {
			menu.menu.add(0, 2, order++, R.string.lbl_add_to_queue).setOnMenuItemClickListener {
				mediaManager.queueAudioItem(rowItemDto)
				true
			}

			menu.menu.add(0, 1, order++, R.string.lbl_instant_mix).setOnMenuItemClickListener {
				playbackHelper.playInstantMix(requireContext(), rowItemDto)
				true
			}
		}

		menu.show()
	}

	private val itemResponse: (List<BaseItemDto>) -> Unit = { items ->
		if (items.isNotEmpty()) {
			mItems = mutableListOf()
			items.forEachIndexed { i, item ->
				mItemList?.addItem(item, i)
				mItems.add(item)
			}
			if (mediaManager.isPlayingAudio) {
				mAudioEventListener.onPlaybackStateChange(PlaybackController.PlaybackState.PLAYING, mediaManager.currentAudioItem)
			}
		}
	}

	private fun play(
		items: List<BaseItemDto>,
		ndx: Int,
		shuffle: Boolean,
	) {
		Timber.i("play items: %d, ndx: %d, shuffle: %b", items.size, ndx, shuffle)
		playbackLauncher.launch(requireContext(), items, 0, false, ndx, shuffle)
	}
}
