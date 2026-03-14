package org.jellyfin.androidtv.ui

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.databinding.ItemRowBinding
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.sdk.getFullName
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaType
import java.util.UUID

class ItemRowView : FrameLayout {
	private lateinit var wholeRow: RelativeLayout
	private lateinit var indexNo: TextView
	private lateinit var itemName: TextView
	private lateinit var extraName: TextView
	private lateinit var runTime: TextView
	private lateinit var watchedMark: TextView
	private lateinit var chevronContainer: LinearLayout
	private lateinit var chevronUp: ImageView
	private lateinit var chevronDown: ImageView
	private var normalBackground: android.graphics.drawable.Drawable? = null

	private var ourIndex = 0
	private var totalCount = 0
	private var formattedTime: String = ""
	private var reorderingEnabled = false

	var item: BaseItemDto? = null
		private set

	var rowSelectedListener: RowSelectedListener? = null
	private var rowClickedListener: RowClickedListener? = null

	fun interface RowSelectedListener {
		fun onRowSelected(row: ItemRowView)
	}

	fun interface RowClickedListener {
		fun onRowClicked(row: ItemRowView)
	}

	constructor(context: Context) : super(context) {
		inflateView(context)
	}

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
		inflateView(context)
	}

	constructor(
		context: Context,
		song: BaseItemDto,
		ndx: Int,
		rowSelectedListener: RowSelectedListener?,
		rowClickedListener: RowClickedListener?,
	) : super(context) {
		inflateView(context)
		this.rowSelectedListener = rowSelectedListener
		this.rowClickedListener = rowClickedListener
		setItem(song, ndx)
		setOnClickListener { rowClickedListener?.onRowClicked(this) }
	}

	private fun inflateView(context: Context) {
		val binding = ItemRowBinding.inflate(LayoutInflater.from(context), this, true)
		wholeRow = binding.wholeRow
		indexNo = binding.indexNo
		itemName = binding.songName
		extraName = binding.artistName
		runTime = binding.runTime
		watchedMark = binding.watchedMark
		chevronContainer = binding.chevronContainer
		chevronUp = binding.chevronUp
		chevronDown = binding.chevronDown
		normalBackground = wholeRow.background
		isFocusable = true
	}

	override fun onFocusChanged(
		gainFocus: Boolean,
		direction: Int,
		previouslyFocusedRect: Rect?,
	) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
		if (gainFocus) {
			wholeRow.setBackgroundResource(R.drawable.jellyfin_button)
			rowSelectedListener?.onRowSelected(this)
			updateChevronColors(true)
		} else {
			wholeRow.background = normalBackground
			updateChevronColors(false)
		}
	}

	fun setItem(
		item: BaseItemDto,
		ndx: Int,
	) {
		this.item = item
		ourIndex = ndx + 1
		indexNo.text = ourIndex.toString()
		when (item.type) {
			org.jellyfin.sdk.model.api.BaseItemKind.AUDIO -> {
				itemName.text = item.name
				val artist =
					when {
						!item.artists.isNullOrEmpty() -> item.artists!!.first()
						!TextUtils.isEmpty(item.albumArtist) -> item.albumArtist
						else -> null
					}
				if (!TextUtils.isEmpty(artist)) {
					extraName.text = artist
				} else {
					extraName.visibility = GONE
				}
			}
			else -> {
				val series = if (item.seriesName != null) item.getFullName(context) else null
				if (!TextUtils.isEmpty(series)) {
					itemName.text = series
					extraName.text = item.name
				} else {
					itemName.text = item.name
					extraName.visibility = GONE
				}
				updateWatched()
			}
		}
		formattedTime =
			TimeUtils.formatRuntimeHoursMinutes(
				context,
				if (item.runTimeTicks != null) item.runTimeTicks!! / 10000 else 0,
			)
		runTime.text = formattedTime
	}

	fun updateWatched() {
		val baseItem = item ?: return
		if (MediaType.VIDEO == baseItem.mediaType && baseItem.userData?.played == true) {
			watchedMark.text = "✓"
		} else {
			watchedMark.text = ""
		}
	}

	fun updateCurrentTime(pos: Long) {
		if (pos < 0) {
			runTime.text = formattedTime
		} else {
			runTime.text = "${TimeUtils.formatMillis(pos)} / $formattedTime"
		}
	}

	fun getIndex(): Int = ourIndex - 1

	fun updateIndex(ndx: Int) {
		ourIndex = ndx + 1
		indexNo.text = ourIndex.toString()
		updateChevronColors(hasFocus())
	}

	fun setTotalCount(count: Int) {
		totalCount = count
		updateChevronColors(hasFocus())
	}

	fun setReorderingEnabled(enabled: Boolean) {
		reorderingEnabled = enabled
		chevronContainer.visibility = if (enabled) VISIBLE else GONE

		val lp = runTime.layoutParams as RelativeLayout.LayoutParams
		if (enabled) {
			lp.removeRule(RelativeLayout.ALIGN_PARENT_END)
			lp.addRule(RelativeLayout.START_OF, chevronContainer.id)
		} else {
			lp.removeRule(RelativeLayout.START_OF)
			lp.addRule(RelativeLayout.ALIGN_PARENT_END)
		}
		runTime.layoutParams = lp

		updateChevronColors(hasFocus())
	}

	private fun updateChevronColors(isFocused: Boolean) {
		if (!reorderingEnabled) return

		val canMoveUp = ourIndex > 1
		val canMoveDown = ourIndex < totalCount

		val activeColor = Color.WHITE
		val inactiveColor = Color.GRAY

		val upColor = if (canMoveUp && isFocused) activeColor else inactiveColor
		chevronUp.setColorFilter(upColor, PorterDuff.Mode.SRC_IN)
		chevronUp.alpha = if (canMoveUp) 1.0f else 0.3f

		val downColor = if (canMoveDown && isFocused) activeColor else inactiveColor
		chevronDown.setColorFilter(downColor, PorterDuff.Mode.SRC_IN)
		chevronDown.alpha = if (canMoveDown) 1.0f else 0.3f
	}

	fun setPlaying(playing: Boolean): Boolean {
		if (playing) {
			indexNo.setBackgroundResource(R.drawable.ic_play)
			indexNo.text = ""
		} else {
			indexNo.setBackgroundResource(R.drawable.blank10x10)
			indexNo.text = ourIndex.toString()
		}
		return playing
	}

	fun setPlaying(id: UUID?): Boolean = setPlaying(id != null && item?.id == id)
}
