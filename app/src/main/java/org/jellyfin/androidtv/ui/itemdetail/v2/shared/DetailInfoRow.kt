package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.browsing.composable.inforow.InfoRowColors
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemBadge
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemSeparator
import org.jellyfin.androidtv.ui.itemdetail.v2.InfoItemText
import org.jellyfin.androidtv.ui.itemdetail.v2.MediaBadge
import org.jellyfin.androidtv.ui.itemdetail.v2.MediaBadgeChip
import org.jellyfin.androidtv.ui.itemdetail.v2.RuntimeInfo
import org.jellyfin.sdk.model.api.BaseItemDto

@Composable
fun DetailInfoRow(
	item: BaseItemDto,
	isSeries: Boolean,
	badges: List<MediaBadge>,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(2.dp),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(0.dp),
		) {
			val metadataItems =
				buildList<@Composable () -> Unit> {
					// Year
					item.productionYear?.let { add { InfoItemText(text = it.toString()) } }

					// Runtime + Ends At (movies only)
					if (!isSeries) {
						item.runTimeTicks?.let {
							add { RuntimeInfo(it) }
							add {
								InfoItemText(
									text =
										stringResource(
											R.string.lbl_playback_control_ends,
											getEndsAt(it),
										),
								)
							}
						}
					}

					// Series-specific: Season count + status badge
					if (isSeries) {
						val seasonCount = item.childCount ?: 0
						if (seasonCount > 0) {
							add {
								InfoItemText(
									text =
										pluralStringResource(
											R.plurals.season_count,
											seasonCount,
											seasonCount,
										),
								)
							}
						}

						item.status?.lowercase()?.let { status ->
							if (status == "continuing" || status == "ended") {
								val labelRes =
									if (status == "continuing") {
										R.string.lbl__continuing
									} else {
										R.string.lbl_ended
									}

								val bgColor =
									if (status == "continuing") {
										InfoRowColors.Green.first
									} else {
										InfoRowColors.Red.first
									}

								add {
									InfoItemBadge(
										text = stringResource(labelRes),
										bgColor = bgColor,
										color = JellyfinTheme.colorScheme.onSurface,
									)
								}
							}
						}
					}

					// Rating
					item.officialRating?.let { rating ->
						add { InfoItemBadge(text = rating) }
					}
				}
			metadataItems.forEachIndexed { index, content ->
				content()
				if (index < metadataItems.size - 1) {
					InfoItemSeparator()
				}
			}
			if (badges.isNotEmpty()) {
				if (metadataItems.isNotEmpty()) InfoItemSeparator()
				Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
					badges.forEach { badge ->
						MediaBadgeChip(badge = badge)
					}
				}
			}
		}
	}
}
