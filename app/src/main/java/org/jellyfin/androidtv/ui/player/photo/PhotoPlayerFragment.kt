package org.jellyfin.androidtv.ui.player.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class PhotoPlayerFragment : Fragment() {
	data class Args(
		val itemId: UUID,
		val albumSortBy: ItemSortBy? = null,
		val albumSortOrder: SortOrder? = null,
		val autoPlay: Boolean = false,
	) {
		fun toBundle() =
			bundleOf(
				ARGUMENT_ITEM_ID to itemId.toString(),
				ARGUMENT_ALBUM_SORT_BY to albumSortBy?.serialName,
				ARGUMENT_ALBUM_SORT_ORDER to albumSortOrder?.serialName,
				ARGUMENT_AUTO_PLAY to autoPlay,
			)

		companion object {
			fun fromBundle(bundle: Bundle?): Args? {
				val itemId =
					bundle?.getString(ARGUMENT_ITEM_ID)?.let {
						try {
							UUID.fromString(it)
						} catch (_: Exception) {
							null
						}
					} ?: return null
				return Args(
					itemId = itemId,
					albumSortBy =
						bundle.getString(ARGUMENT_ALBUM_SORT_BY)?.let {
							ItemSortBy.fromNameOrNull(it)
						},
					albumSortOrder =
						bundle.getString(ARGUMENT_ALBUM_SORT_ORDER)?.let {
							SortOrder.fromNameOrNull(it)
						},
					autoPlay = bundle.getBoolean(ARGUMENT_AUTO_PLAY, false),
				)
			}
		}
	}

	companion object {
		internal const val ARGUMENT_ITEM_ID = "item_id"
		internal const val ARGUMENT_ALBUM_SORT_BY = "album_sort_by"
		internal const val ARGUMENT_ALBUM_SORT_ORDER = "album_sort_order"
		internal const val ARGUMENT_AUTO_PLAY = "auto_play"
	}

	private val viewModel by viewModel<PhotoPlayerViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Load requested item in viewmodel
		lifecycleScope.launch {
			val args = requireNotNull(Args.fromBundle(arguments)) { "Missing PhotoPlayer arguments" }
			viewModel.loadItem(args.itemId, setOf(args.albumSortBy ?: ItemSortBy.SORT_NAME), args.albumSortOrder ?: SortOrder.ASCENDING)

			if (args.autoPlay) viewModel.startPresentation()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			ScreenIdOverlay(ScreenIds.PHOTO_PLAYER_ID, ScreenIds.PHOTO_PLAYER_NAME) {
				PhotoPlayerScreen()
			}
		}
	}
}
