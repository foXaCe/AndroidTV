package org.jellyfin.androidtv.ui.livetv.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.livetv.createNoProgramDataBaseItem
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Composable
fun LiveTvGuideGrid(
	channels: List<BaseItemDto>,
	programs: Map<UUID, List<BaseItemDto>>,
	guideStart: LocalDateTime,
	guideEnd: LocalDateTime,
	onChannelFocus: (BaseItemDto) -> Unit,
	onProgramFocus: (BaseItemDto) -> Unit,
	onProgramClick: (BaseItemDto) -> Unit,
	onProgramLongClick: (BaseItemDto) -> Unit,
	onChannelClick: (BaseItemDto) -> Unit,
	onChannelLongClick: (BaseItemDto) -> Unit,
	modifier: Modifier = Modifier,
	lazyListState: LazyListState = rememberLazyListState(),
	horizontalScrollState: ScrollState = rememberScrollState(),
	pageUpLabel: String? = null,
	pageDownLabel: String? = null,
	onPageUp: (() -> Unit)? = null,
	onPageDown: (() -> Unit)? = null,
) {
	// Auto-scroll to "now - 30min" on first composition
	val density = LocalDensity.current.density
	LaunchedEffect(guideStart) {
		val now = LocalDateTime.now()
		if (!now.isBefore(guideStart) && now.isBefore(guideEnd)) {
			val minutesFromStart = Duration.between(guideStart, now).toMinutes().toFloat()
			val scrollMinutes = (minutesFromStart - 30f).coerceAtLeast(0f)
			val pxPerMin = TvSpacing.guideRowWidthPerMinDp.value * density
			horizontalScrollState.animateScrollTo((scrollMinutes * pxPerMin).toInt())
		}
	}

	// Now line position
	val now = LocalDateTime.now()
	val nowMinutesFromStart =
		if (!now.isBefore(guideStart) && now.isBefore(guideEnd)) {
			Duration.between(guideStart, now).seconds.toFloat() / 60f
		} else {
			null
		}

	Box(modifier = modifier.background(VegafoXColors.BackgroundDeep)) {
		LazyColumn(
			state = lazyListState,
			modifier = Modifier.fillMaxWidth(),
		) {
			// Page up button
			if (pageUpLabel != null && onPageUp != null) {
				item(key = "page_up") {
					Row(Modifier.fillMaxWidth().height(TvSpacing.programCellHeight)) {
						Box(Modifier.width(TvSpacing.channelHeaderWidth))
						GuidePagingButton(
							label = pageUpLabel,
							onClick = onPageUp,
							modifier = Modifier.weight(1f),
						)
					}
				}
			}

			// Channel rows
			items(
				items = channels,
				key = { it.id.toString() },
			) { channel ->
				val rowIndex = channels.indexOf(channel)
				val rowBg = if (rowIndex % 2 == 1) Color.White.copy(alpha = 0.015f) else Color.Transparent

				GuideRow(
					channel = channel,
					programs = programs[channel.id] ?: emptyList(),
					guideStart = guideStart,
					guideEnd = guideEnd,
					horizontalScrollState = horizontalScrollState,
					onChannelFocus = onChannelFocus,
					onProgramFocus = onProgramFocus,
					onProgramClick = onProgramClick,
					onProgramLongClick = onProgramLongClick,
					onChannelClick = onChannelClick,
					onChannelLongClick = onChannelLongClick,
					rowBackground = rowBg,
					nowMinutesFromStart = nowMinutesFromStart,
				)
			}

			// Page down button
			if (pageDownLabel != null && onPageDown != null) {
				item(key = "page_down") {
					Row(Modifier.fillMaxWidth().height(TvSpacing.programCellHeight)) {
						Box(Modifier.width(TvSpacing.channelHeaderWidth))
						GuidePagingButton(
							label = pageDownLabel,
							onClick = onPageDown,
							modifier = Modifier.weight(1f),
						)
					}
				}
			}
		}
	}
}

@Composable
private fun GuideRow(
	channel: BaseItemDto,
	programs: List<BaseItemDto>,
	guideStart: LocalDateTime,
	guideEnd: LocalDateTime,
	horizontalScrollState: ScrollState,
	onChannelFocus: (BaseItemDto) -> Unit,
	onProgramFocus: (BaseItemDto) -> Unit,
	onProgramClick: (BaseItemDto) -> Unit,
	onProgramLongClick: (BaseItemDto) -> Unit,
	onChannelClick: (BaseItemDto) -> Unit,
	onChannelLongClick: (BaseItemDto) -> Unit,
	rowBackground: Color,
	nowMinutesFromStart: Float?,
) {
	val imageHelper = koinInject<ImageHelper>()
	val imageUrl =
		remember(channel) {
			imageHelper.getPrimaryImageUrl(channel, null, ImageHelper.MAX_PRIMARY_IMAGE_HEIGHT)
		}

	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.height(TvSpacing.programCellHeight)
				.background(rowBackground)
				.focusGroup(),
	) {
		// Fixed channel header
		GuideChannelHeaderCell(
			channel = channel,
			imageUrl = imageUrl,
			onFocus = { onChannelFocus(channel) },
			onClick = { onChannelClick(channel) },
			onLongClick = { onChannelLongClick(channel) },
			modifier = Modifier.width(TvSpacing.channelHeaderWidth),
		)

		// Scrollable programs with now line overlay
		val nowLineColor = VegafoXColors.OrangePrimary.copy(alpha = 0.70f)

		Row(
			modifier =
				Modifier
					.weight(1f)
					.fillMaxHeight()
					.horizontalScroll(horizontalScrollState)
					.drawWithContent {
						drawContent()
						if (nowMinutesFromStart != null) {
							val pxPerMin = TvSpacing.guideRowWidthPerMinDp.toPx()
							val nowX = nowMinutesFromStart * pxPerMin
							drawLine(
								color = nowLineColor,
								start = Offset(nowX, 0f),
								end = Offset(nowX, size.height),
								strokeWidth = 2.dp.toPx(),
							)
						}
					},
		) {
			val cells = buildProgramCells(programs, guideStart, guideEnd)
			cells.forEach { cell ->
				GuideProgramCell(
					program = cell.program,
					widthDp = cell.widthDp,
					isFirst = cell.isFirst,
					isLast = cell.isLast,
					onFocus = { onProgramFocus(cell.program) },
					onClick = { onProgramClick(cell.program) },
					onLongClick = { onProgramLongClick(cell.program) },
				)
			}
		}
	}
}

private data class ProgramCellData(
	val program: BaseItemDto,
	val widthDp: Int,
	val isFirst: Boolean,
	val isLast: Boolean,
)

@Composable
private fun buildProgramCells(
	programs: List<BaseItemDto>,
	guideStart: LocalDateTime,
	guideEnd: LocalDateTime,
): List<ProgramCellData> {
	val context = LocalContext.current
	return remember(programs, guideStart, guideEnd) {
		val result = mutableListOf<ProgramCellData>()
		val widthPerMin = TvSpacing.guideRowWidthPerMinDp.value.toInt()

		if (programs.isEmpty()) {
			val totalMinutes = durationMinutes(guideStart, guideEnd)
			var slot = 0
			while (30 * slot < totalMinutes) {
				val slotStart = guideStart.plusMinutes(30L * slot)
				val slotEnd = guideStart.plusMinutes(30L * (slot + 1))
				val empty = createNoProgramDataBaseItem(context, null, slotStart, slotEnd)
				result.add(
					ProgramCellData(
						program = empty,
						widthDp = 30 * widthPerMin,
						isFirst = slot == 0,
						isLast = slot == (totalMinutes / 30) - 1,
					),
				)
				slot++
			}
			return@remember result
		}

		var prevEnd = guideStart
		for (item in programs) {
			var start = item.startDate ?: guideStart
			if (start.isBefore(guideStart)) start = guideStart
			if (start.isAfter(guideEnd)) continue
			if (start.isBefore(prevEnd)) continue

			if (start.isAfter(prevEnd)) {
				val empty = createNoProgramDataBaseItem(context, item.channelId, prevEnd, start)
				val gapMinutes = durationMinutes(prevEnd, start)
				if (gapMinutes > 0) {
					result.add(
						ProgramCellData(
							program = empty,
							widthDp = gapMinutes * widthPerMin,
							isFirst = prevEnd == guideStart,
							isLast = false,
						),
					)
				}
			}

			var end = item.endDate ?: guideEnd
			if (end.isAfter(guideEnd)) end = guideEnd
			prevEnd = end
			val duration = durationMinutes(start, end)
			if (duration > 0) {
				result.add(
					ProgramCellData(
						program = item,
						widthDp = duration * widthPerMin,
						isFirst = start == guideStart,
						isLast = end == guideEnd,
					),
				)
			}
		}

		if (prevEnd.isBefore(guideEnd)) {
			val empty =
				createNoProgramDataBaseItem(
					context,
					programs.firstOrNull()?.channelId,
					prevEnd,
					guideEnd,
				)
			val remaining = durationMinutes(prevEnd, guideEnd)
			if (remaining > 0) {
				result.add(
					ProgramCellData(
						program = empty,
						widthDp = remaining * widthPerMin,
						isFirst = result.isEmpty(),
						isLast = true,
					),
				)
			}
		}

		result
	}
}

@Composable
private fun GuideChannelHeaderCell(
	channel: BaseItemDto,
	imageUrl: String?,
	onFocus: () -> Unit,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	var isFocused by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.02f else 1f,
		animationSpec = spring(stiffness = Spring.StiffnessMedium),
		label = "channelScale",
	)

	Box(
		modifier =
			modifier
				.fillMaxHeight()
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.focusable()
				.onFocusChanged { focusState ->
					isFocused = focusState.isFocused
					if (focusState.isFocused) onFocus()
				}.onPreviewKeyEvent { event ->
					if (event.type == KeyEventType.KeyUp) {
						when (event.key) {
							Key.Enter, Key.DirectionCenter -> {
								if (event.nativeKeyEvent.flags and android.view.KeyEvent.FLAG_CANCELED_LONG_PRESS == 0) {
									onClick()
								}
								true
							}
							else -> false
						}
					} else if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.isLongPress) {
						when (event.key) {
							Key.Enter, Key.DirectionCenter -> {
								onLongClick()
								true
							}
							else -> false
						}
					} else {
						false
					}
				},
	) {
		ChannelHeader(
			channel = channel,
			imageUrl = imageUrl,
			isFavorite = channel.userData?.isFavorite == true,
			isFocused = isFocused,
			accentColor = VegafoXColors.OrangePrimary,
		)
	}
}

@Composable
private fun GuideProgramCell(
	program: BaseItemDto,
	widthDp: Int,
	isFirst: Boolean,
	isLast: Boolean,
	onFocus: () -> Unit,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
) {
	var isFocused by remember { mutableStateOf(false) }

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.02f else 1f,
		animationSpec = spring(stiffness = Spring.StiffnessMedium),
		label = "programScale",
	)

	Box(
		modifier =
			Modifier
				.width(widthDp.dp)
				.fillMaxHeight()
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.focusable()
				.onFocusChanged { focusState ->
					isFocused = focusState.isFocused
					if (focusState.isFocused) onFocus()
				}.onPreviewKeyEvent { event ->
					if (event.type == KeyEventType.KeyUp) {
						when (event.key) {
							Key.Enter, Key.DirectionCenter -> {
								if (event.nativeKeyEvent.flags and android.view.KeyEvent.FLAG_CANCELED_LONG_PRESS == 0) {
									onClick()
								}
								true
							}
							else -> false
						}
					} else if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.isLongPress) {
						when (event.key) {
							Key.Enter, Key.DirectionCenter -> {
								onLongClick()
								true
							}
							else -> false
						}
					} else {
						false
					}
				},
	) {
		ProgramCell(
			program = program,
			isFocused = isFocused,
			accentColor = VegafoXColors.OrangePrimary,
		)
	}
}

private fun durationMinutes(
	start: LocalDateTime,
	end: LocalDateTime,
): Int =
	(
		(
			end.toInstant(ZoneOffset.UTC).toEpochMilli() -
				start.toInstant(ZoneOffset.UTC).toEpochMilli()
		) / 60000
	).toInt()
