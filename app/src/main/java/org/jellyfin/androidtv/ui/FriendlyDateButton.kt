package org.jellyfin.androidtv.ui

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.getDateFormatter
import java.time.LocalDateTime

class FriendlyDateButton(
	context: Context,
	val date: LocalDateTime,
	listener: OnClickListener,
) : FrameLayout(context) {
	init {
		val v = LayoutInflater.from(context).inflate(R.layout.friendly_date_button, this, false)
		addView(v)
		isFocusable = true
		setOnClickListener(listener)

		v.findViewById<TextView>(R.id.friendlyName).text =
			TimeUtils.getFriendlyDate(context, date, true)
		v.findViewById<TextView>(R.id.date).text =
			context.getDateFormatter().format(date)
	}

	override fun onFocusChanged(
		gainFocus: Boolean,
		direction: Int,
		previouslyFocusedRect: Rect?,
	) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
		setBackgroundColor(
			if (gainFocus) ContextCompat.getColor(context, R.color.ds_primary) else 0,
		)
	}
}
