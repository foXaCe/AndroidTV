package org.jellyfin.androidtv.util

import android.view.Gravity
import android.view.KeyEvent
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.CustomMessage
import org.jellyfin.androidtv.data.repository.CustomMessageRepository
import org.jellyfin.androidtv.data.repository.ItemMutationRepository
import org.jellyfin.androidtv.ui.itemhandling.AudioQueueBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.BaseRowType
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.sdk.canPlay
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.SortOrder
import org.jellyfin.sdk.model.api.UserItemDataDto
import java.util.UUID

class KeyProcessor(
	private val mediaManager: MediaManager,
	private val navigationRepository: NavigationRepository,
	private val itemMutationRepository: ItemMutationRepository,
	private val customMessageRepository: CustomMessageRepository,
	private val playbackHelper: PlaybackHelper,
) {
	companion object {
		const val MENU_MARK_FAVORITE = 0
		const val MENU_UNMARK_FAVORITE = 1
		const val MENU_MARK_PLAYED = 2
		const val MENU_UNMARK_PLAYED = 3
		const val MENU_PLAY = 4
		const val MENU_PLAY_SHUFFLE = 5
		const val MENU_PLAY_FIRST_UNWATCHED = 6
		const val MENU_ADD_QUEUE = 7
		const val MENU_ADVANCE_QUEUE = 8
		const val MENU_REMOVE_FROM_QUEUE = 9
		const val MENU_GOTO_NOW_PLAYING = 10
		const val MENU_INSTANT_MIX = 11
		const val MENU_CLEAR_QUEUE = 12
		const val MENU_TOGGLE_SHUFFLE = 13
	}

	fun handleKey(
		key: Int,
		rowItem: BaseRowItem?,
		activity: FragmentActivity,
	): Boolean {
		if (rowItem == null) return false

		when (key) {
			KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
				if (mediaManager.isPlayingAudio && (rowItem.baseRowType != BaseRowType.BaseItem || rowItem.baseItem?.type != BaseItemKind.PHOTO)) {
					return false
				}

				when (rowItem.baseRowType) {
					BaseRowType.BaseItem -> {
						val item = rowItem.baseItem ?: return false
						if (!item.canPlay()) return false
						when (item.type) {
							BaseItemKind.AUDIO -> {
								if (rowItem is AudioQueueBaseRowItem) {
									createItemMenu(rowItem, item.userData, activity)
									return true
								}
								playbackHelper.retrieveAndPlay(item.id, false, activity)
								return true
							}
							BaseItemKind.MOVIE,
							BaseItemKind.EPISODE,
							BaseItemKind.TV_CHANNEL,
							BaseItemKind.VIDEO,
							BaseItemKind.PROGRAM,
							BaseItemKind.TRAILER,
							-> {
								playbackHelper.retrieveAndPlay(item.id, false, activity)
								return true
							}
							BaseItemKind.SERIES,
							BaseItemKind.SEASON,
							BaseItemKind.BOX_SET,
							-> {
								createPlayMenu(rowItem, false, activity)
								return true
							}
							BaseItemKind.MUSIC_ALBUM,
							BaseItemKind.MUSIC_ARTIST,
							-> {
								createPlayMenu(rowItem, true, activity)
								return true
							}
							BaseItemKind.PLAYLIST -> {
								createPlayMenu(rowItem, MediaType.AUDIO == item.mediaType, activity)
								return true
							}
							BaseItemKind.PHOTO -> {
								navigationRepository.navigate(
									Destinations.photoPlayer(
										item.id,
										true,
										ItemSortBy.SORT_NAME,
										SortOrder.ASCENDING,
									),
								)
								return true
							}
							else -> {}
						}
					}
					BaseRowType.LiveTvChannel, BaseRowType.LiveTvRecording -> {
						val itemId = rowItem.itemId ?: return false
						playbackHelper.retrieveAndPlay(itemId, false, activity)
						return true
					}
					BaseRowType.LiveTvProgram -> {
						val channelId = rowItem.baseItem?.channelId ?: return false
						playbackHelper.retrieveAndPlay(channelId, false, activity)
						return true
					}
					else -> {}
				}

				if (mediaManager.hasAudioQueueItems()) return false
			}
			KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BUTTON_Y -> {
				when (rowItem.baseRowType) {
					BaseRowType.BaseItem -> {
						val item = rowItem.baseItem ?: return true
						when (item.type) {
							BaseItemKind.MOVIE,
							BaseItemKind.EPISODE,
							BaseItemKind.TV_CHANNEL,
							BaseItemKind.VIDEO,
							BaseItemKind.PROGRAM,
							BaseItemKind.SERIES,
							BaseItemKind.SEASON,
							BaseItemKind.BOX_SET,
							BaseItemKind.MUSIC_ALBUM,
							BaseItemKind.MUSIC_ARTIST,
							BaseItemKind.PLAYLIST,
							BaseItemKind.AUDIO,
							BaseItemKind.TRAILER,
							->
								createItemMenu(rowItem, item.userData, activity)
							else -> {}
						}
					}
					else -> {}
				}
				return true
			}
		}
		return false
	}

	fun createItemMenu(
		rowItem: BaseRowItem,
		userData: UserItemDataDto?,
		activity: FragmentActivity,
	): PopupMenu {
		val item = rowItem.baseItem
		val menu = PopupMenu(activity, activity.currentFocus, Gravity.END)
		var order = 0

		if (rowItem is AudioQueueBaseRowItem) {
			if (item != mediaManager.currentAudioItem) {
				menu.menu.add(0, MENU_ADVANCE_QUEUE, order++, R.string.lbl_play_from_here)
			}
			menu.menu.add(0, MENU_GOTO_NOW_PLAYING, order++, R.string.lbl_goto_now_playing)

			if (mediaManager.currentAudioQueueSize > 1) {
				menu.menu.add(0, MENU_TOGGLE_SHUFFLE, order++, R.string.lbl_shuffle_queue)
			}

			if (userData != null) {
				if (userData.isFavorite) {
					menu.menu.add(0, MENU_UNMARK_FAVORITE, order++, activity.getString(R.string.lbl_remove_favorite))
				} else {
					menu.menu.add(0, MENU_MARK_FAVORITE, order++, activity.getString(R.string.lbl_add_favorite))
				}
			}

			menu.menu.add(0, MENU_REMOVE_FROM_QUEUE, order++, R.string.lbl_remove_from_queue)

			if (mediaManager.hasAudioQueueItems()) {
				menu.menu.add(0, MENU_CLEAR_QUEUE, order++, R.string.lbl_clear_queue)
			}
		} else if (item != null) {
			val isFolder = item.isFolder == true
			if (item.canPlay()) {
				if (isFolder &&
					item.type != BaseItemKind.MUSIC_ALBUM &&
					item.type != BaseItemKind.PLAYLIST &&
					item.type != BaseItemKind.MUSIC_ARTIST &&
					userData != null &&
					(userData.unplayedItemCount ?: 0) > 0
				) {
					menu.menu.add(0, MENU_PLAY_FIRST_UNWATCHED, order++, R.string.lbl_play_first_unwatched)
				}
				menu.menu.add(0, MENU_PLAY, order++, if (isFolder) R.string.lbl_play_all else R.string.lbl_play)
				if (isFolder) {
					menu.menu.add(0, MENU_PLAY_SHUFFLE, order++, R.string.lbl_shuffle_all)
				}
			}

			val isMusic =
				item.type == BaseItemKind.MUSIC_ALBUM ||
					item.type == BaseItemKind.MUSIC_ARTIST ||
					item.type == BaseItemKind.AUDIO ||
					(item.type == BaseItemKind.PLAYLIST && MediaType.AUDIO == item.mediaType)

			if (isMusic) {
				menu.menu.add(0, MENU_ADD_QUEUE, order++, R.string.lbl_add_to_queue)
			}

			if (isMusic) {
				if (item.type != BaseItemKind.PLAYLIST) {
					menu.menu.add(0, MENU_INSTANT_MIX, order++, R.string.lbl_instant_mix)
				}
			} else {
				if (userData != null && userData.played) {
					menu.menu.add(0, MENU_UNMARK_PLAYED, order++, activity.getString(R.string.lbl_mark_unplayed))
				} else {
					menu.menu.add(0, MENU_MARK_PLAYED, order++, activity.getString(R.string.lbl_mark_played))
				}
			}

			if (userData != null) {
				if (userData.isFavorite) {
					menu.menu.add(0, MENU_UNMARK_FAVORITE, order++, activity.getString(R.string.lbl_remove_favorite))
				} else {
					menu.menu.add(0, MENU_MARK_FAVORITE, order++, activity.getString(R.string.lbl_add_favorite))
				}
			}
		}

		menu.setOnMenuItemClickListener { menuItem ->
			handleMenuItemClick(menuItem.itemId, rowItem, item, activity)
		}
		menu.show()
		return menu
	}

	private fun createPlayMenu(
		rowItem: BaseRowItem,
		isMusic: Boolean,
		activity: FragmentActivity,
	) {
		val menu = PopupMenu(activity, activity.currentFocus, Gravity.END)
		var order = 0
		if (!isMusic && rowItem.baseItem?.type != BaseItemKind.PLAYLIST) {
			menu.menu.add(0, MENU_PLAY_FIRST_UNWATCHED, order++, R.string.lbl_play_first_unwatched)
		}
		menu.menu.add(0, MENU_PLAY, order++, R.string.lbl_play_all)
		menu.menu.add(0, MENU_PLAY_SHUFFLE, order++, R.string.lbl_shuffle_all)
		if (isMusic) {
			menu.menu.add(0, MENU_ADD_QUEUE, order, R.string.lbl_add_to_queue)
		}

		menu.setOnMenuItemClickListener { menuItem ->
			handleMenuItemClick(menuItem.itemId, rowItem, rowItem.baseItem, activity)
		}
		menu.show()
	}

	private fun handleMenuItemClick(
		menuItemId: Int,
		rowItem: BaseRowItem,
		item: BaseItemDto?,
		activity: FragmentActivity,
	): Boolean {
		if (item == null) return false

		return when (menuItemId) {
			MENU_PLAY -> {
				playbackHelper.retrieveAndPlay(item.id, false, activity)
				true
			}
			MENU_PLAY_SHUFFLE -> {
				playbackHelper.retrieveAndPlay(item.id, true, activity)
				true
			}
			MENU_ADD_QUEUE -> {
				playbackHelper.getItemsToPlay(
					activity,
					item,
					false,
					false,
					object : Response<List<BaseItemDto>>(activity.lifecycle) {
						override fun onResponse(response: List<BaseItemDto>) {
							if (!isActive) return
							mediaManager.addToAudioQueue(response)
						}

						override fun onError(exception: Exception) {
							if (!isActive) return
							Toast.makeText(activity, R.string.msg_cannot_play_time, Toast.LENGTH_LONG).show()
						}
					},
				)
				true
			}
			MENU_PLAY_FIRST_UNWATCHED -> {
				activity.playFirstUnwatchedItem(item.id)
				true
			}
			MENU_MARK_FAVORITE -> {
				toggleFavorite(activity, item.id, true)
				true
			}
			MENU_UNMARK_FAVORITE -> {
				toggleFavorite(activity, item.id, false)
				true
			}
			MENU_MARK_PLAYED -> {
				togglePlayed(activity, item.id, true)
				true
			}
			MENU_UNMARK_PLAYED -> {
				togglePlayed(activity, item.id, false)
				true
			}
			MENU_GOTO_NOW_PLAYING -> {
				navigationRepository.navigate(Destinations.nowPlaying)
				true
			}
			MENU_TOGGLE_SHUFFLE -> {
				mediaManager.shuffleAudioQueue()
				true
			}
			MENU_REMOVE_FROM_QUEUE -> {
				if (rowItem is AudioQueueBaseRowItem) {
					mediaManager.removeFromAudioQueue(rowItem.queueEntry)
				}
				true
			}
			MENU_ADVANCE_QUEUE -> {
				mediaManager.playFrom((rowItem as AudioQueueBaseRowItem).queueEntry)
				true
			}
			MENU_CLEAR_QUEUE -> {
				mediaManager.clearAudioQueue()
				true
			}
			MENU_INSTANT_MIX -> {
				playbackHelper.playInstantMix(activity, item)
				true
			}
			else -> false
		}
	}

	private fun togglePlayed(
		lifecycleOwner: LifecycleOwner,
		itemId: UUID,
		played: Boolean,
	) {
		runOnLifecycle(lifecycleOwner.lifecycle) {
			itemMutationRepository.setPlayed(itemId, played)
		}
		customMessageRepository.pushMessage(CustomMessage.RefreshCurrentItem)
	}

	private fun toggleFavorite(
		lifecycleOwner: LifecycleOwner,
		itemId: UUID,
		favorite: Boolean,
	) {
		runOnLifecycle(lifecycleOwner.lifecycle) {
			itemMutationRepository.setFavorite(itemId, favorite)
		}
		customMessageRepository.pushMessage(CustomMessage.RefreshCurrentItem)
	}
}
