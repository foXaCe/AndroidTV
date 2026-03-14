package org.jellyfin.androidtv.data.syncplay

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

object SyncPlayQueueFetcher {
	@JvmStatic
	fun fetchQueueAsync(
		itemIds: List<UUID>,
		startIndex: Int,
		startPositionTicks: Long,
		callback: SyncPlayQueueHelper.QueueCallback,
	) {
		ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
			try {
				val result = SyncPlayQueueHelper.fetchQueue(itemIds, startIndex, startPositionTicks)
				withContext(Dispatchers.Main) {
					if (result != null) {
						callback.onQueueReady(result.items, result.startIndex, result.startPositionMs)
					} else {
						callback.onError()
					}
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to fetch SyncPlay queue")
				withContext(Dispatchers.Main) { callback.onError() }
			}
		}
	}
}
