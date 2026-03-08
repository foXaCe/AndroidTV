package org.jellyfin.androidtv.ui.jellyseerr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import org.jellyfin.androidtv.R
import android.view.View
import org.jellyfin.androidtv.databinding.ItemJellyseerrRequestBinding
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto

class RequestsAdapter :
	ListAdapter<JellyseerrRequestDto, RequestsAdapter.ViewHolder>(DiffCallback()) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = ItemJellyseerrRequestBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	class ViewHolder(
		private val binding: ItemJellyseerrRequestBinding,
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(item: JellyseerrRequestDto) {
			binding.apply {
				// Poster
				val posterPath = item.media?.posterPath
				if (posterPath != null) {
					val posterUrl = "https://image.tmdb.org/t/p/w200$posterPath"
					posterImage.load(posterUrl)
				} else {
					posterImage.setImageResource(R.drawable.app_logo)
				}

				val displayTitle = item.media?.title ?: item.media?.name ?: itemView.context.getString(R.string.lbl_unknown)
				titleText.text = displayTitle

				typeText.text = item.type.uppercase()

				// Media status: 1=Unknown, 2=Pending, 3=Processing/Downloading, 4=Partial, 5=Available, 6=Blacklisted
				// Request status: 1=Pending, 2=Approved, 3=Declined, 4=Available
				val mediaStatus = item.media?.status
				val isDownloading = mediaStatus == 3
				val isPartial = mediaStatus == 4
				val isAvailable = mediaStatus == 5

				// Determine status text and icon
				val (statusTextValue, statusColor, iconRes) = when {
					item.status == 1 -> Triple(itemView.context.getString(R.string.jellyseerr_status_pending), R.color.grey_light, R.drawable.ic_pending)
					item.status == 3 -> Triple(itemView.context.getString(R.string.jellyseerr_status_declined), R.color.red, R.drawable.ic_declined)
					isAvailable -> Triple(itemView.context.getString(R.string.jellyseerr_status_available), R.color.white, R.drawable.ic_available)
					isPartial -> Triple(itemView.context.getString(R.string.jellyseerr_status_partially_available), R.color.white, R.drawable.ic_partially_available)
					isDownloading -> Triple(itemView.context.getString(R.string.jellyseerr_status_downloading), R.color.white, R.drawable.ic_indigo_spinner)
					item.status == 2 -> Triple(itemView.context.getString(R.string.jellyseerr_status_approved), R.color.white, null)
					else -> Triple(itemView.context.getString(R.string.jellyseerr_status_unknown), R.color.grey_light, null)
				}

				statusText.text = statusTextValue
				statusText.setTextColor(ContextCompat.getColor(root.context, statusColor))

				// Handle status icon
				if (iconRes != null) {
					statusIcon.setImageResource(iconRes)
					statusIcon.visibility = View.VISIBLE
					statusIcon.clearAnimation()
				} else {
					statusIcon.visibility = View.GONE
					statusIcon.clearAnimation()
				}

				val requesterName = item.requestedBy?.username ?: itemView.context.getString(R.string.lbl_unknown)
				requestedByText.text = itemView.context.getString(R.string.jellyseerr_requested_by, requesterName)

				val dateStr = item.createdAt?.substringBefore("T") ?: itemView.context.getString(R.string.lbl_unknown)
				dateText.text = itemView.context.getString(R.string.jellyseerr_request_date, dateStr)
			}
		}
	}

	private class DiffCallback : DiffUtil.ItemCallback<JellyseerrRequestDto>() {
		override fun areItemsTheSame(
			oldItem: JellyseerrRequestDto,
			newItem: JellyseerrRequestDto,
		) = oldItem.id == newItem.id

		override fun areContentsTheSame(
			oldItem: JellyseerrRequestDto,
			newItem: JellyseerrRequestDto,
		) = oldItem == newItem
	}
}
