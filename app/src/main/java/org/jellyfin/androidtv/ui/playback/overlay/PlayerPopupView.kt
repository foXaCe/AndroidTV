package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.tv.TvFocusCard
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.apiclient.JellyfinImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ChapterInfo
import org.jellyfin.sdk.model.api.ImageType
import org.koin.compose.koinInject
import java.util.UUID

sealed class PlayerPopupContent {
	data class Chapters(
		val itemId: UUID,
		val chapters: List<ChapterInfo>,
		val chapterImages: List<JellyfinImage>,
		val scrollToIndex: Int,
	) : PlayerPopupContent()

	data class Channels(
		val channels: List<BaseItemDto>,
		val scrollToIndex: Int,
	) : PlayerPopupContent()

	data class Cast(
		val people: List<BaseItemPerson>,
	) : PlayerPopupContent()
}

class PlayerPopupView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0,
) : AbstractComposeView(context, attrs, defStyle) {
	private val _content = MutableStateFlow<PlayerPopupContent?>(null)

	var content: PlayerPopupContent?
		get() = _content.value
		set(value) {
			_content.value = value
		}

	fun interface ChapterClickListener {
		fun onChapterClick(positionMs: Long)
	}

	fun interface ChannelClickListener {
		fun onChannelClick(channelId: UUID)
	}

	fun interface PersonClickListener {
		fun onPersonClick(personId: UUID?)
	}

	var onChapterClick: ChapterClickListener? = null
	var onChannelClick: ChannelClickListener? = null
	var onPersonClick: PersonClickListener? = null

	@Composable
	override fun Content() {
		val content by _content.collectAsState()
		JellyfinTheme {
			when (val c = content) {
				is PlayerPopupContent.Chapters -> ChaptersRow(c)
				is PlayerPopupContent.Channels -> ChannelsRow(c)
				is PlayerPopupContent.Cast -> CastRow(c)
				null -> {}
			}
		}
	}

	@Composable
	private fun ChaptersRow(content: PlayerPopupContent.Chapters) {
		val api = koinInject<ApiClient>()
		val listState = rememberLazyListState()

		LaunchedEffect(content.scrollToIndex) {
			if (content.scrollToIndex > 0) {
				listState.scrollToItem(content.scrollToIndex)
			}
		}

		LazyRow(
			state = listState,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			items(content.chapters.size) { index ->
				val chapter = content.chapters[index]
				val image = content.chapterImages.getOrNull(index)
				val imageUrl = image?.takeIf { it.tag.isNotEmpty() }?.getUrl(api, maxWidth = 240)
				val positionMs = chapter.startPositionTicks / 10000

				TvFocusCard(
					onClick = { onChapterClick?.onChapterClick(positionMs) },
					shape = JellyfinTheme.shapes.small,
				) {
					Column(
						modifier = Modifier
							.width(160.dp)
							.fillMaxHeight(),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier
								.width(160.dp)
								.aspectRatio(16f / 9f)
								.clip(JellyfinTheme.shapes.small)
								.background(JellyfinTheme.colorScheme.surface),
						) {
							if (imageUrl != null) {
								AsyncImage(
									model = imageUrl,
									contentDescription = chapter.name,
									contentScale = ContentScale.Crop,
									modifier = Modifier.fillMaxSize(),
								)
							}
						}
						Text(
							text = chapter.name ?: "",
							style = JellyfinTheme.typography.labelSmall,
							color = JellyfinTheme.colorScheme.onSurface,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier.padding(top = 4.dp),
						)
						Text(
							text = TimeUtils.formatMillis(positionMs),
							style = JellyfinTheme.typography.labelSmall,
							color = JellyfinTheme.colorScheme.onSurfaceVariant,
							maxLines = 1,
						)
					}
				}
			}
		}
	}

	@Composable
	private fun ChannelsRow(content: PlayerPopupContent.Channels) {
		val api = koinInject<ApiClient>()
		val listState = rememberLazyListState()

		LaunchedEffect(content.scrollToIndex) {
			if (content.scrollToIndex > 0) {
				listState.scrollToItem(content.scrollToIndex)
			}
		}

		LazyRow(
			state = listState,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			items(content.channels, key = { it.id }) { channel ->
				val imageUrl = channel.imageTags?.get(ImageType.PRIMARY)?.let { tag ->
					api.imageApi.getItemImageUrl(
						itemId = channel.id,
						imageType = ImageType.PRIMARY,
						tag = tag,
						maxHeight = 150,
					)
				}

				TvFocusCard(
					onClick = { onChannelClick?.onChannelClick(channel.id) },
					shape = JellyfinTheme.shapes.small,
				) {
					Column(
						modifier = Modifier
							.width(120.dp)
							.fillMaxHeight(),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier
								.width(120.dp)
								.height(90.dp)
								.clip(JellyfinTheme.shapes.small)
								.background(JellyfinTheme.colorScheme.surface),
							contentAlignment = Alignment.Center,
						) {
							if (imageUrl != null) {
								AsyncImage(
									model = imageUrl,
									contentDescription = channel.name,
									contentScale = ContentScale.Fit,
									modifier = Modifier.fillMaxSize().padding(8.dp),
								)
							} else {
								Text(
									text = channel.channelNumber ?: "",
									style = JellyfinTheme.typography.titleMedium,
									color = JellyfinTheme.colorScheme.onSurface,
									textAlign = TextAlign.Center,
								)
							}
						}
						Text(
							text = channel.name ?: "",
							style = JellyfinTheme.typography.labelSmall,
							color = JellyfinTheme.colorScheme.onSurface,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier.padding(top = 4.dp),
						)
					}
				}
			}
		}
	}

	@Composable
	private fun CastRow(content: PlayerPopupContent.Cast) {
		val api = koinInject<ApiClient>()

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			items(content.people, key = { it.id }) { person ->
				val imageUrl = person.primaryImageTag?.let { tag ->
					api.imageApi.getItemImageUrl(
						itemId = person.id,
						imageType = ImageType.PRIMARY,
						tag = tag,
						maxHeight = 200,
					)
				}

				TvFocusCard(
					onClick = { onPersonClick?.onPersonClick(person.id) },
					shape = JellyfinTheme.shapes.small,
				) {
					Column(
						modifier = Modifier
							.width(110.dp)
							.fillMaxHeight(),
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Box(
							modifier = Modifier
								.width(110.dp)
								.aspectRatio(2f / 3f)
								.clip(JellyfinTheme.shapes.small)
								.background(JellyfinTheme.colorScheme.surface),
						) {
							if (imageUrl != null) {
								AsyncImage(
									model = imageUrl,
									contentDescription = person.name,
									contentScale = ContentScale.Crop,
									modifier = Modifier.fillMaxSize(),
								)
							}
						}
						Text(
							text = person.name ?: "",
							style = JellyfinTheme.typography.labelSmall,
							color = JellyfinTheme.colorScheme.onSurface,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier.padding(top = 4.dp),
						)
						if (!person.role.isNullOrEmpty()) {
							Text(
								text = person.role ?: "",
								style = JellyfinTheme.typography.labelSmall,
								color = JellyfinTheme.colorScheme.onSurfaceVariant,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
						}
					}
				}
			}
		}
	}
}
