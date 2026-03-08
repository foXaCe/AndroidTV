package org.jellyfin.androidtv.ui.search

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import org.jellyfin.sdk.model.api.BaseItemDto

@Stable
data class SearchResultGroup(
	@StringRes val labelRes: Int,
	val items: Collection<BaseItemDto>,
)
