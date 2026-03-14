package org.jellyfin.androidtv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.jellyfin.androidtv.databinding.ItemListBinding
import org.jellyfin.sdk.model.api.BaseItemDto
import java.util.UUID

class ItemListView
	@JvmOverloads
	constructor(
		context: Context,
		attrs: AttributeSet? = null,
	) : FrameLayout(context, attrs) {
		// Accessed by ItemListViewHelper.kt extension
		@JvmField
		val mItemIds: MutableList<UUID> = mutableListOf()

		@JvmField
		val mList: LinearLayout

		private var rowSelectedListener: ItemRowView.RowSelectedListener? = null
		private var rowClickedListener: ItemRowView.RowClickedListener? = null
		private var reorderingEnabled = false

		init {
			val binding = ItemListBinding.inflate(LayoutInflater.from(context), this, true)
			mList = binding.songList
		}

		fun setRowSelectedListener(listener: ItemRowView.RowSelectedListener?) {
			rowSelectedListener = listener
		}

		fun setRowClickedListener(listener: ItemRowView.RowClickedListener?) {
			rowClickedListener = listener
		}

		fun clear() {
			mList.removeAllViews()
			mItemIds.clear()
		}

		fun addItem(
			item: BaseItemDto,
			ndx: Int,
		) {
			val row = ItemRowView(context, item, ndx, rowSelectedListener, rowClickedListener)
			row.setReorderingEnabled(reorderingEnabled)
			mList.addView(row)
			mItemIds.add(item.id)
			updateTotalCount()
		}

		fun setReorderingEnabled(enabled: Boolean) {
			reorderingEnabled = enabled
			for (i in 0 until mList.childCount) {
				val child = mList.getChildAt(i)
				if (child is ItemRowView) {
					child.setReorderingEnabled(enabled)
				}
			}
			updateTotalCount()
		}

		private fun updateTotalCount() {
			val count = mList.childCount
			for (i in 0 until count) {
				val child = mList.getChildAt(i)
				if (child is ItemRowView) {
					child.setTotalCount(count)
				}
			}
		}

		fun moveItem(
			fromIndex: Int,
			toIndex: Int,
		) {
			if (fromIndex < 0 ||
				fromIndex >= mList.childCount ||
				toIndex < 0 ||
				toIndex >= mList.childCount
			) {
				return
			}

			val viewToMove = mList.getChildAt(fromIndex)
			mList.removeViewAt(fromIndex)
			mList.addView(viewToMove, toIndex)

			val itemId = mItemIds.removeAt(fromIndex)
			mItemIds.add(toIndex, itemId)

			val minIndex = minOf(fromIndex, toIndex)
			val maxIndex = maxOf(fromIndex, toIndex)
			for (i in minIndex..maxIndex) {
				val child = mList.getChildAt(i)
				if (child is ItemRowView) {
					child.updateIndex(i)
				}
			}

			updateTotalCount()
		}

		fun focusItemAt(index: Int) {
			if (index in 0 until mList.childCount) {
				mList.getChildAt(index)?.requestFocus()
			}
		}

		fun updatePlaying(id: UUID?): ItemRowView? {
			var ret: ItemRowView? = null
			for (i in 0 until mList.childCount) {
				val view = mList.getChildAt(i)
				if (view is ItemRowView) {
					if (view.setPlaying(id)) ret = view
				}
			}
			return ret
		}
	}
