package org.jellyfin.androidtv.ui.playback.overlay

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.sdk.model.api.MediaSourceInfo

class SeekProviderTests :
	FunSpec({
		test("SeekProvider.seekPositions with simple duration") {
			val mediaSource =
				mockk<MediaSourceInfo> {
					every { runTimeTicks } returns 300_000_000L // 30000ms
				}
			val playbackController =
				mockk<PlaybackController> {
					every { canSeek() } returns true
					every { currentMediaSource } returns mediaSource
					every { currentlyPlayingItem } returns null
				}
			val seekProvider = SeekProvider(playbackController, mockk(), mockk(), mockk(), false, 10_000)

			seekProvider.getSeekPositions() shouldBe longArrayOf(0L, 10000L, 20000L, 30000L)
		}

		test("SeekProvider.seekPositions with odd duration") {
			val mediaSource =
				mockk<MediaSourceInfo> {
					every { runTimeTicks } returns 450_000_000L // 45000ms
				}
			val playbackController =
				mockk<PlaybackController> {
					every { canSeek() } returns true
					every { currentMediaSource } returns mediaSource
					every { currentlyPlayingItem } returns null
				}
			val seekProvider = SeekProvider(playbackController, mockk(), mockk(), mockk(), false, 10_000)

			seekProvider.getSeekPositions() shouldBe longArrayOf(0L, 10000L, 20000L, 30000L, 40000L, 45000L)
		}

		test("SeekProvider.seekPositions with seek disabled") {
			val playbackController =
				mockk<PlaybackController> {
					every { canSeek() } returns false
				}
			val seekProvider = SeekProvider(playbackController, mockk(), mockk(), mockk(), false, 10_000)

			seekProvider.getSeekPositions().size shouldBe 0
		}
	})
